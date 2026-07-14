# WI-20260714-ATS-024 보안 독립 검토 요약

## 판정

- **보안 게이트: BLOCK**
- Critical은 확인되지 않았지만, 같은 origin의 활성 콘텐츠로 이어질 수 있는 **High 1건**이 미해결 상태다.
- Medium 1건은 acceptance bootstrap 로그의 이메일 식별자 노출이다.
- 따라서 현재 상태는 release/client-share 보안 승인 조건을 충족하지 않는다.

## Findings

### HIGH - 사용자 문의 첨부가 공개 정적 콘텐츠로 직접 노출된다

- `src/main/java/com/atstudio/atstudio/service/QuestionService.java:216-227`은 인증 사용자가 제출한 첨부 원본을 `StorageRoot.PUBLIC`의 `questions/attachments`에 저장한다.
- `src/main/java/com/atstudio/atstudio/service/storage/LocalStorageService.java:59-62`는 제출 파일의 확장자를 생성 키에 유지한다.
- `src/main/java/com/atstudio/atstudio/config/WebConfig.java:20-24`는 PUBLIC 루트 전체를 `/uploads/**`로 정적 제공한다.
- `src/main/java/com/atstudio/atstudio/config/SecurityConfig.java:83-84,133-136`은 Track 원본과 기업 인증 문서만 정적 차단하고, 나머지 정적 요청은 `permitAll`로 둔다.
- `src/main/java/com/atstudio/atstudio/config/PublicThumbnailHeaderFilter.java:17,25-29`의 강제 JPEG/CSP 헤더는 Playlist 경로에만 적용된다.

결과적으로 사용자가 `.html` 등 활성 확장자의 내용을 문의 첨부로 올리면 `/uploads/questions/attachments/**`에서 인증·소유권 검사 없이 같은 origin 콘텐츠로 열릴 수 있다. 프론트 토큰이 브라우저 저장소에 존재하므로 세션 탈취까지 이어질 수 있어 client-share/release 차단 등급이다.

### MEDIUM - acceptance bootstrap 로그가 이메일 식별자를 출력한다

- `src/main/java/com/atstudio/atstudio/bootstrap/TestUserBootstrapRunner.java:138-144`는 구성된 다섯 QA 계정의 이메일을 성공 로그에 출력한다.
- 같은 파일 `:173-175,194-202`도 구독 정렬/생략 로그에 사용자 이메일을 출력한다.

Fixture 계정이라도 환경에서 구성 가능한 이메일은 PII다. 로그에는 역할별 성공 여부와 reason code만 남기고 이메일은 제거해야 한다.

## 이상 없음

- 기업 인증 문서: 서명/MIME 검증 후 PRIVATE 저장, 경로 redaction, ADMIN attachment-only 다운로드가 확인됐다.
- 세션/로그아웃/소셜: refresh 회전·폐기, logout, password change/reset 폐기, social token staging/실패 cleanup 순서가 확인됐다.
- CSV: 사용자 제어 텍스트의 formula-leading 값이 CSV quoting 전에 중립화된다.
- Host/CORS/proxy/rate-limit: exact Host, forwarding-header 제거, loopback proxy 신뢰, 단일 IP literal, explicit CORS 경계가 확인됐다.
- acceptance secret/bootstrap guard: 명시 프로필, 외부 값, callback 일치 검사가 fail-closed다. 위 이메일 로그 결함은 별도다.
- lifecycle/teardown: PID/start-time/command fingerprint 소유권 확인, tunnel -> frontend -> backend 정리, 실패 시 `finally`, 반복 종료가 확인됐다.

## WI-022 제한 판정

- **두 외부 egress 미검증:** 보안 결함이 아니라 **잔여 acceptance 제한**이다. 단일 egress에서 spoof header가 rate-limit bucket을 우회하지 못한 증거는 있으나, 실제 두 클라이언트 분리는 아직 X-02 운영 증명으로 닫을 수 없다. 이것만으로 제한된 client share를 막지는 않지만, 다중 클라이언트 rate-limit 분리 보장은 공지할 수 없다.
- **active subscriber 성공 경로 미검증:** 보안 결함이 아니라 **fixture/acceptance 제한**이다. 다만 승인된 인수 범위에 subscriber 흐름이 포함되므로 현재 acceptance URL을 클라이언트에게 전달하는 데에는 차단 요소다. 앱의 security release blocker는 아니지만 client-share readiness blocker다.

## 필요한 최소 보정

1. `/uploads/questions/attachments/**`를 정적 경로에서 모든 역할에 `denyAll`하고, 권한 확인이 있는 `/api/questions/{id}/attachments/{id}` 다운로드만 유지한다.
2. 생성 HTML fixture로 anonymous/USER/ADMIN 정적 접근 차단과 권한 있는 API attachment 응답을 focused 테스트한다.
3. Bootstrap 로그에서 이메일/사용자 식별자를 제거하고 로그 캡처 단위 테스트를 추가한다.

이번 WI-024에서는 사용자 중단 지시에 따라 코드 수정이나 테스트 명령을 실행하지 않았다.
