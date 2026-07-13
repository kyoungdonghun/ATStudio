# WI-20260711-ATS-004 보안·개인정보 검토 요약

## 결론

**판정: BLOCK.** 현재 코드 기준으로 `CRITICAL 3`, `HIGH 7`, `MEDIUM 3`, `LOW 3`을 확인했다. 특히 원본 음원 공개, 같은 출처의 능동 콘텐츠 업로드, 이메일 토큰 로그는 운영 배포 전에 차단해야 한다.

- 검토 기준: branch `dev/kyoung`, HEAD `27d22446e5d21324dadcfcb322dbe51704dfe914`
- 검토 방식: 정적 코드·문서·테스트 검사만 수행했다. 공격 파일 업로드나 토큰 재사용 등 exploit은 실행하지 않았다.
- 기존 사용자 변경은 수정하거나 되돌리지 않았다.

## 최우선 발견

| ID | 등급 | 판정과 공격 경로 | 핵심 근거 |
|---|---|---|---|
| PG-004-01 | **CRITICAL / P0** | 익명 사용자가 공개 트랙 상세의 `audioFile` 값을 `/uploads/...`로 직접 요청하면 원본 음원을 받을 수 있다. 구독 확인, 일일 제한, 다운로드 기록, 라이선스 발급을 모두 우회한다. | `SecurityConfig.java:66-69,131-132`; `TrackResponse.java:18,38`; `TrackService.java:66,79,130-144`; `WebConfig.java:21-24`; `DownloadService.java:40-86`; `docs/design/usecase/sound-track.md:146-175` |
| PG-004-02 | **CRITICAL / P0** | 구독자가 플레이리스트 썸네일로 `.html` 또는 직접 열 수 있는 active SVG를 업로드할 수 있다. 원래 확장자가 보존되고 `/uploads/**`가 같은 출처에서 inline 제공되므로, 피해자가 URL을 열면 스크립트가 실행되어 `localStorage`의 access/refresh token과 사용자 데이터를 읽을 수 있다. 피해자 클릭과 같은 출처 배치가 전제다. | `PlaylistController.java:27-31`; `PlaylistService.java:39-54,179-188`; `LocalStorageService.java:51-70`; `WebConfig.java:21-24`; `SecurityConfig.java:131-132`; `frontend/vite.config.ts:12-24`; `authStore.ts:31-53` |
| PG-004-03 | **CRITICAL / P0** | SMTP 실패 시 수신자와 전체 HTML 본문을 로그에 남긴다. 인증·비밀번호 재설정 본문에는 현재 유효한 토큰 URL이 들어가므로 로그 열람자가 계정 토큰을 탈취할 수 있다. | `EmailService.java:53-65,96-108,163-180`; `docs/policies/security-policy.md:43-45` |
| PG-004-04 | **HIGH / P1** | 회사 인증 문서는 확장자와 크기만 검사하고 MIME·magic byte·파서 검증이 없다. BUSINESS 사용자가 허용 확장자로 위장한 악성 파일을 제출하면 검토 업무상 관리자가 다운로드해 열 가능성이 높다. | `ValidationConstants.java:46-49`; `CompanyCertificationService.java:233-276`; `CompanyCertificationController.java:98-112`; `company-certification.md:129-142,182-198` |
| PG-004-05 | **HIGH / P1** | 공개 회원가입 `POST /api/users`가 rate limiter 대상이 아니며 DB 저장과 인증 메일 발송까지 수행한다. 고유 식별자를 반복 사용하면 계정·메일·DB 자원 남용이 가능하고, 공개 이메일/전화/닉네임 확인 API는 가입 여부 oracle도 제공한다. | `SecurityConfig.java:55,63-65`; `AuthRateLimitFilter.java:58-76`; `UserService.java:36-65,161-170`; `UtilController.java:36-57` |
| PG-004-06 | **HIGH / P1, 배치 조건부** | rate limit 키가 `remoteAddr` 하나에 의존한다. Cloudflare Tunnel/프록시 뒤에서 origin이 단일 프록시 주소를 보면 모든 고객이 같은 버킷을 공유해 소수 요청으로 로그인·재설정·refresh 전체를 잠글 수 있다. 신뢰 프록시의 실제 클라이언트 IP 해석 설정은 찾지 못했다. | `AuthRateLimitFilter.java:44-76`; `AuthRateLimitProperties.java:14-18`; `CorsConfig.java:23-28` |
| PG-004-07 | **HIGH / P1** | 프론트 로그아웃은 로컬 토큰만 지우며, 비밀번호 변경·재설정도 서버 refresh token을 폐기하지 않는다. 탈취된 refresh token은 로그아웃이나 비밀번호 변경 뒤에도 갱신에 사용될 수 있다. | `authStore.ts:49-53`; `UserService.java:149-159`; `EmailService.java:141-159`; `User.java:65-70,103-105`; `AuthService.java:75-110` |
| PG-004-08 | **HIGH / P1** | 문의 첨부는 파일 수·사용자 quota·종류 제한 없이 요청당 최대 60MB를 반복 저장할 수 있고, 문의 삭제 시 DB 행만 지워 실제 파일이 남는다. 인증 사용자 한 명이 저장소를 고갈시킬 수 있다. | `QuestionCreateRequest.java:33-35`; `QuestionService.java:185-188,207-223`; `application.yml:44-46`; `QuestionServiceTest.java:448-500` |

## 요청 항목별 판정

- **`CompanyCertificationResponse.documentPath`: LOW / P3 정책 충돌.** `/me`와 관리자 상세에 사용자 ID와 제출 디렉터리를 포함한 공개 URL 형태의 경로를 반환한다(`CompanyCertificationResponse.java:18,34`; `CompanyCertificationService.java:139-144,181-185,310-315`). 이는 민감 문서를 admin API로만 검토한다는 최소 공개 원칙(`company-certification.md:182-198`)과 맞지 않는다. 다만 개별 `storedPath`는 DTO에서 숨겨지고(`CompanyCertificationDocumentResponse.java:7-20`), 현재 Spring 체인에서 `GET /uploads/company-docs/**`는 ADMIN으로 제한된다(`SecurityConfig.java:80`). 따라서 현재 근거만으로 문서 내용 직접 유출은 아니며, 경로 제거와 opaque document ID 사용이 적절하다.
- **회사 문서 extension-only 검증: HIGH / P1.** 확장자·크기·개수 외에 서버 탐지 MIME, magic byte, 문서 파서, AV/CDR 검사가 필요하다. 클라이언트 `Content-Type`을 신뢰해 되돌려 주지 않아야 한다.
- **무제한 `size`: MEDIUM / P2.** 공개 track/notice/album 목록이 상한 없이 `PageRequest`에 전달되어 큰 DB 조회·직렬화를 유발할 수 있다(`RequestDTO.java:15-26`; `TrackService.java:99`; `NoticeService.java:62-67`; `AlbumService.java:74-76`). 전역 최대값을 강제해야 한다.
- **trycloudflare CORS: MEDIUM / P2.** 모든 `https://*.trycloudflare.com`을 운영 프로필과 무관하게 credentialed origin으로 허용한다(`CorsConfig.java:15-32`). 현재 토큰은 origin별 `localStorage`에서 Authorization 헤더로 붙기 때문에 이 설정만으로 즉시 토큰이 자동 전송되지는 않지만, 환경별 정확한 allowlist 원칙을 위반하고 cookie 인증 도입 시 위험이 커진다.
- **플레이리스트 XSS trust boundary:** 구독자 입력이므로 P0이다. 반면 album/track 업로드는 controller와 security 양쪽에서 ADMIN 전용이어서 같은 저권한 공격 경로는 아니지만, 동일 저장소·정적 공개 구조를 사용하므로 MIME/magic 검사와 이미지 재인코딩을 적용해야 한다(`AlbumController.java:28-33,66-71`; `TrackController.java:28-34,140-146`).
- **원본/preview 정책:** 공개 preview가 없을 때 원본으로 fallback하는 정책 자체도 원본 공개 위험을 만든다(`TrackService.java:141-145`; `sound-track.md:123-142`). 그러나 `audioFile` 직접 경로는 preview 존재 여부와 무관하게 원본을 노출하고 다운로드 통제를 우회하므로 CRITICAL 판정은 유지된다.

## 추가 위험

- 과거 JWT fallback 런타임 코드는 제거되고 현재는 secret 미설정·짧은 키를 fail closed 처리한다(`application.yml:48-52`; `JwtConfig.java:23-43`). 그러나 과거 감사 보고서 `backend-audit-report.md:108,115`에 이전 literal 값이 남아 있다. 값은 이 보고서에 재기재하지 않았다. 사용 이력이 배제되지 않으면 **HIGH / P1 조건부**로 즉시 회전·기록 삭제가 필요하다.
- QA bootstrap은 기본 비활성이지만 공유 기본 암호가 코드에 있고 profile guard 없이 속성 하나로 ADMIN 계정을 만든다. 운영에서 활성화되면 **HIGH / P1 조건부**다(`TestUserBootstrapProperties.java:14-22`; `TestUserBootstrapRunner.java:33-37,52-68`).
- 검증 예외 전체 문자열은 거부된 이메일·전화·비밀번호·reset token을 포함할 수 있어 **MEDIUM / P2**다(`GlobalExceptionHandler.java:70-75`; `RegisterRequest.java:28-43`; `ResetPasswordRequest.java:14-19`).

## 조치 순서

1. 원본 음원을 web root 밖으로 이동하고 공개 DTO에서 `audioFile`을 제거한다. 공개 stream은 생성된 preview만 허용하고 원본은 권한 검사 download API로만 제공한다.
2. `/uploads/**` 전체 공개를 폐지하고 공개 이미지 전용 저장소만 허용한다. 서버에서 이미지를 decode/re-encode하고 HTML/SVG를 금지하며 CSP와 고정 `Content-Type`을 적용한다.
3. 이메일 fallback의 본문·수신자 로그를 제거하고 비밀정보 비포함 여부를 log-capture 테스트로 고정한다.
4. 회사 문서를 격리 저장소에 두고 MIME/magic/parser/AV 검증을 적용한다. `documentPath`는 응답과 API 명세에서 제거한다.
5. 가입·중복확인·auth 경로에 분산 rate limiting을 적용하고, 신뢰 프록시에서만 검증한 실제 IP와 계정 식별자를 함께 키로 사용한다.
6. 서버 logout·전체 세션 폐기를 구현하고 비밀번호 변경/재설정 시 refresh token을 즉시 무효화한다.

상세 공격 경로, 과거 감사 재검증 표, 확인된 양호 통제, 테스트 공백은 `deliverables/agent/WI-20260711-ATS-004-evidence-pack.md`에 기록했다.
