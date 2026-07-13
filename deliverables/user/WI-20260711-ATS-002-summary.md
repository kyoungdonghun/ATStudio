# WI-20260711-ATS-002 백엔드 아키텍처 감사 요약

## TL;DR

- 현재 워킹트리를 정적 감사한 결과, 즉시 차단이 필요한 P0 2건과 우선 수정할 P1 5건을 확인했다.
- 가장 큰 위험은 원본 음원의 인증 우회 접근과 회원 탈퇴 후 정기결제 지속 가능성이다.
- 과거 `docs/audit/backend-audit-report.md`의 CRITICAL 5건과 MAJOR 15건은 현재 모두 수정 또는 재설계되었다. MINOR 본문 11건 중 9건은 수정되었고, `CR-A-009`와 `CR-C-013`은 현재도 열려 있다.
- 이번 WI는 코드와 상태를 변경하지 않았고, 지정된 요약 및 Evidence Pack만 생성했다. 테스트는 핸드오프의 정적 검사 제한과 읽기 전용 범위 때문에 실행하지 않았다.

## 감사 기준선

- Branch: `dev/kyoung`
- HEAD: `27d22446e5d21324dadcfcb322dbe51704dfe914`
- 조사 규모: controller 25개, service 하위 Java 64개, entity 하위 Java 74개, repository 41개, DTO 125개, 테스트 71개
- 기존 `docs/client/` 변경과 미추적 WI/REQ/PDF는 사용자 자산으로 유지했으며 수정하지 않았다.

## 최우선 발견사항

| ID | 우선순위 | 분류 | 발견사항 및 영향 | 핵심 근거 |
|---|---|---|---|---|
| BE-001 | P0 | 확인된 결함 | 공개 트랙 상세가 원본 `audioFile` 저장 키를 반환하고 `/uploads/**`가 파일 저장소 전체를 정적 공개한다. 트랙 상세 화면은 이 값을 플레이어 상태에 전달하지만 정상 재생은 스트림 API를 사용한다. 그와 무관하게 익명 사용자가 반환된 저장 키를 `/uploads/{audioFile}`에 직접 붙이면 `DownloadService`의 구독/일일 한도/라이선스 발급을 거치지 않고 원본 파일을 받을 수 있다. | `TrackResponse.java:18,38`, `TrackDetailPage.tsx:139-148`, `playerStore.ts:173-175`, `WebConfig.java:21-24`, `SecurityConfig.java:80,130-132`, `DownloadService.java:51-78` |
| BE-002 | P0 | 확인된 결함 | 회원 탈퇴는 `users.is_deleted`와 일부 부가 데이터만 변경하고 활성 구독 및 빌링 계약을 취소하지 않는다. 갱신 조회도 탈퇴 사용자를 제외하지 않으므로 탈퇴 후 자동 청구가 가능하다. | `UserService.java:104-122`, `User.java:81-84`, `BillingAgreementRepository.java:26-29`, `RecurringRenewalService.java:89-100,123-159` |
| BE-003 | P1 | 확인된 결함 | 최초 빌링 결제 실패 시 주문을 `FAILED`로 바꾸고 실패 횟수를 기록한 뒤 `BusinessException`을 던진다. 기본 트랜잭션 롤백으로 그 실패 기록 자체가 사라져 주문이 `IN_PROGRESS`로 남을 수 있다. | `BillingAgreementApplicationService.java:161-191,212-226`, `BillingAgreementApplicationServiceTest.java:310-349` |
| BE-004 | P1 | 확인된 결함 | 정기결제 대상 전체를 하나의 트랜잭션과 무제한 목록으로 처리하면서 루프 안에서 외부 결제를 호출한다. 후반 한 건의 예외나 커밋 실패가 앞선 로컬 원장을 롤백해 이미 성공한 외부 결제와 분리할 수 있다. | `SubscriptionScheduler.java:32-36`, `RecurringRenewalService.java:89-105,147-159`, `BillingAgreementRepository.java:26-29` |
| BE-005 | P1 | 확인된 동시성 결함 | 환불 요청은 기존 예약 환불액을 합산한 뒤 새 행을 저장하지만 원 결제 행을 잠그지 않는다. 동시 요청이 각각 전액 환불 가능으로 판단해 누적 환불 예약액을 초과할 수 있다. | `AdminPaymentRefundService.java:89-111,248-262`, `PaymentRefundRepository.java:49-57` |
| BE-006 | P1 | 확인된 결함 | SMTP 실패 fallback이 수신자와 이메일 HTML 본문 전체를 로그에 남긴다. 본문에는 이메일 인증 또는 비밀번호 재설정 토큰이 포함되므로 비밀값과 개인정보가 로그로 유출된다. | `EmailService.java:46-65,88-108,163-179`, `security-policy.md:26-35,41-45` |
| BE-007 | P1 | 확인된 공통 결함군 | Track/Album/Playlist/Question/Notice는 새 파일을 DB 커밋 전에 저장하면서 롤백 정리를 등록하지 않는다. Track/Notice는 기존 파일까지 커밋 전에 삭제해 롤백 후 DB가 사라진 파일을 가리킬 수 있고, Album/Playlist의 썸네일 교체와 Question 삭제는 성공해도 이전 파일을 남긴다. Company Certification에만 새 파일 rollback 삭제와 기존 파일 after-commit 삭제가 구현되어 있다. | `TrackService.java:60-88,148-179`, `AlbumService.java:42-60,113-126`, `PlaylistService.java:39-64,178-193`, `QuestionService.java:173-188,207-223`, `NoticeService.java:92-132,159-173`, `CompanyCertificationService.java:266-307` |

## 추가 확인된 결함

- **P2 - 소셜 회원 탈퇴 불가:** 소셜 가입은 `password=null`인데 탈퇴는 항상 비밀번호 검증을 요구한다 (`OAuth2Service.java:81-94`, `UserService.java:104-112`).
- **P2 - OAuth 응답 검증 누락:** 토큰 응답 Map은 null 검사하지만 필수 `access_token` 값은 검사하지 않는다 (`OAuth2Service.java:121-159`). 과거 `CR-C-013`이 아직 열려 있다.
- **P2 - 앨범 정렬 오류:** `trackCount` 정렬이 DB 페이지 조회 후 현재 페이지만 정렬하므로 전체 결과 순서가 틀린다 (`AlbumService.java:74-87`).
- **P2 - Range 오류 처리:** 공개 스트림의 Range 값을 직접 파싱하고 `IOException`만 처리해 잘못된 값이나 범위 초과가 4xx 대신 500으로 번질 수 있다 (`TrackController.java:90-137`).
- **P2 - 한도 경쟁 조건:** 플레이리스트 수, 화이트리스트 슬롯, 일일 다운로드 한도가 `count -> compare -> write`로 구현되어 동시 요청에서 초과될 수 있다 (`PlaylistService.java:39-63`, `WhitelistChannelService.java:119-132`, `DownloadService.java:58-78`).

## 개선 및 정책 결정 항목

- **공통 성능/가용성 개선:** `AdminPayment*`, Company Certification, User/Subscription, Track, License 등 여러 서비스가 `Math.max(1, size)`로 최솟값만 보정하고 상한은 두지 않는다 (`AdminPaymentReadService.java:84`, `AdminPaymentRefundService.java:280`, `CompanyCertificationService.java:151`, `UserSubscriptionService.java:95`, `TrackService.java:99,209`, `LicenseService.java:57`). 엔드포인트별 패치를 반복하지 말고 공통 `Pageable` 생성기 또는 요청 검증 정책에서 중앙 최대 크기를 강제해야 한다.
- **개선:** 결제 reconciliation은 최근 100개 주문만 확인하고 활성 빌링 계약은 전체 스캔 후 건별 구독 조회한다. 시간/커서 기반 배치와 일괄 조회가 필요하다.
- **개선:** 항상 실패하도록 남겨둔 과거 구독 생성/일회성 결제 엔드포인트는 명시적 deprecation 또는 제거가 필요하다.
- **정책 모호성:** 설계는 `preview_file`이 없으면 공개 스트림으로 원본 `audio_file`을 제공한다고 명시하지만 실제 preview 생성 구현은 없다. 이 스트림 fallback의 허용 여부는 정책 결정 대상이다. 반면 공개 DTO와 `/uploads/**`를 조합한 원본 직접 다운로드는 해당 정책과 별개인 **확인된 우회(BE-001)** 다.
- **정책 모호성:** 화이트리스트 관리자 상태 변경은 허용 대상 상태만 검사하고 출발 상태별 전이 규칙은 없다. 운영자가 임의 상태 복구를 할 수 있어야 하는지 상태 머신을 확정해야 한다.
- **문서 불일치:** `GET /api/tags`의 코드와 API 명세는 `ResponseDTO` 래퍼를 사용하지만 개발 표준은 raw array 예외로 적혀 있다.

## 과거 감사 재검증

- **CRITICAL 5/5:** 모두 현재 코드에서 수정 확인.
- **MAJOR 15/15:** 모두 수정 또는 현재 결제/구독 정책으로 재설계 확인.
- **MINOR:** 과거 보고서 요약은 10건이라고 쓰지만 본문에는 11개 고유 항목이 있다. 본문 11개를 모두 재검증했고 9개 수정, 2개 열림으로 판정했다.
- **열림:** `CR-A-009`는 저장 키 노출과 정적 파일 공개가 결합되어 BE-001로 심각도가 상승했다. `CR-C-013`은 필수 OAuth 토큰 필드 검증이 여전히 없다.
- **상태 비교:** 31개 본문 항목 중 29개는 수정/재설계되어 현행 결함으로는 stale, 2개는 계속 open/escalated이며, 과거에 해결된 것으로 기록된 항목의 확인된 회귀는 0개다. 과거 상태표의 `CR-P-004` 보류, `CR-B-003` 검증 대기, MINOR 10건 표기는 현재 코드/본문과 맞지 않아 상태 메타데이터가 stale하다.
- 전체 항목별 `fixed / reworked / open / escalated` 매핑은 Evidence Pack에 기록했다.

## 필수 후속 검증

1. BE-001과 BE-002를 배포 차단 항목으로 즉시 분리한다.
2. 결제 트랜잭션을 외부 호출 전후의 영속 상태, 건별 격리 트랜잭션, idempotency/보상 작업으로 재설계한다.
3. 실제 Spring 트랜잭션 통합 테스트로 최초 결제 실패 기록, 다건 갱신 중간 실패, 탈퇴 후 갱신 제외를 검증한다.
4. 익명 `/uploads/tracks/audio/**` 접근, 동시 환불/한도 요청, 파일 롤백/커밋 경계를 통합 테스트로 추가한다.
5. Company Certification의 보상 규칙을 공통 파일 변경 컴포넌트로 승격해 신규 파일은 rollback 후, 교체/삭제 대상은 commit 후 정리하고 삭제 실패 재시도 경로를 둔다.

상세 근거: `deliverables/agent/WI-20260711-ATS-002-evidence-pack.md`
