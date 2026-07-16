# WI-20260716-ATS-015 통합 정합성 검토 요약

## 결론

- P0/P1 결함은 발견하지 않았습니다.
- 릴리스 준비 전에 WI-017에서 처리해야 할 P2 7건과 P3 6건을 확정했습니다.
- 결제, 화이트리스트, 기업 인증, 인증/OAuth, 다운로드/라이선스, 카탈로그/재생목록, 공개 전체 재생의 핵심 계약은 대체로 일치합니다. 아래 findings는 그중 남아 있는 역할 경계, 오류 상태, 비동기 경쟁, 운영 식별자 노출 계약, 검증 공백, 문서 오기입입니다.
- 코드, 기존 문서, DB, 외부 Provider, 비밀값, 클라이언트 브랜치/런타임은 변경하지 않았습니다.

## Findings

### P2-01 기업 인증 사용자 API의 역할 경계가 프론트/문서보다 넓음

- 승인된 화면/문서 계약: 일반 `USER`이면서 `BUSINESS`인 회원만 신청/재신청/내 상태를 사용합니다.
- 프론트: `frontend/src/router/index.tsx:131-137,203-204`
- 문서: `docs/design/usecase/company-certification.md:12`, `docs/design/api-spec.md:3330`
- 백엔드: `src/main/java/com/atstudio/atstudio/config/SecurityConfig.java:120-126`은 사용자 API를 `authenticated()`로만 제한하고, `CompanyCertificationService.java:72-77,112-117,275-288`은 `BUSINESS`만 확인합니다.
- 영향: `ADMIN` 계정이 `BUSINESS` 속성을 가지면 사용자용 신청/조회 API를 호출할 수 있어 역할 계약이 어긋납니다.
- WI-017: 사용자용 세 엔드포인트를 명시적으로 `ROLE_USER`에 한정하고, `BUSINESS ADMIN` 거부 테스트를 추가합니다.

### P2-02 관리자 결제 식별자 표시와 클라이언트 보안 문구가 충돌함

- 클라이언트 문서: `docs/client/0-site-policy.md:46`, `docs/client/testing-guide.md:66`은 결제 키를 화면/캡처에 노출하지 말라고 안내합니다.
- 화면: `frontend/src/pages/admin/PaymentReadOnlyPage.tsx:1469,1577,1918`은 `providerPaymentKey` 또는 Provider transaction ID를 그대로 표시할 수 있습니다.
- 계약/API: `frontend/src/api/admin.ts:276,321,379,397-399` 및 `AdminPaymentReceiptResponse.java:20`, `AdminPaymentSettlementResponse.java:18`, `AdminPaymentRefundPreviewResponse.java:18`, `AdminPaymentRefundResponse.java:25`에 Provider 식별자가 포함됩니다.
- 영향: Toss `paymentKey`는 카드번호나 secret key와 같지는 않지만, 현재 문서가 이를 구분하지 않아 운영자/클라이언트가 금지된 값으로 이해하거나 캡처에 포함할 수 있습니다.
- WI-017: 관리자 화면/API의 기본 표시를 마스킹된 지원 참조값으로 제한할지, 보호된 관리자 식별자로 허용할지 하나로 결정하고 코드·보안 정책·결제 운영 문서·클라이언트 문구를 동일하게 맞춥니다. 환불 Provider 호출에 필요한 서버 저장 원문은 유지합니다.

### P2-03 PlayerBar가 구독 조회 장애를 미구독으로 오판함

- 코드: `frontend/src/layouts/PlayerBar.tsx:86-94`는 모든 조회 실패를 `hasSubscription=false`로 바꾸며, `:277-350,760-779,993-1010`이 이 값으로 다운로드/구독 UI를 결정합니다.
- 기준 계약: `docs/design/usecase/user-subscription.md:163`과 `frontend/src/router/SubscriberRoute.tsx:45-63`은 구조화된 `403 + NO_ACTIVE_SUBSCRIPTION`만 미구독으로 분류합니다.
- 영향: 타임아웃, 5xx, 오프라인, 오래된 응답이 정상 구독자의 기능을 숨기고 재구독을 유도할 수 있습니다.
- WI-017: loading/active/inactive/error 상태를 분리하고, AbortController와 generation fence, 재시도 UX 및 오류/경쟁 테스트를 추가합니다.

### P2-04 네 화면에 latest-request-wins 보장이 없음

- 코드: `TrackDetailPage.tsx:36-45`, `UserManagePage.tsx:29-38`, `UserSubscriptionManagePage.tsx:50-59`, `DownloadQueuePage.tsx:69-90`
- 문서 충돌: `docs/ui/screen-flow.md:68`은 목록 화면이 latest-request-wins를 사용한다고 넓게 선언합니다.
- 영향: 오래된 상세/검색/페이지/정렬 응답이나 오류가 최신 화면을 덮을 수 있고, 관리자가 현재 필터와 맞지 않는 자료를 기준으로 작업할 수 있습니다.
- WI-017: 네 화면에 취소+generation fence를 적용하고 out-of-order 테스트를 추가합니다. 범위를 줄인다면 문서 선언도 함께 좁힙니다.

### P2-05 관리자 결제 mutation/read HTTP wiring의 회귀 증거가 부족함

- 프론트 구현: `PaymentReadOnlyPage.tsx:403-432,481-641,2014-2016`에 정산 import/reconciliation과 환불/권한 보정 preview-request-approve-execute가 있습니다.
- 프론트 테스트: `PaymentReadOnlyPage.test.tsx:195-291`은 stale/read/incident 흐름 4건만 다루고 mutation은 검증하지 않습니다.
- 백엔드 커버리지: WI-013 JaCoCo에서 `AdminPaymentController`와 `AdminPaymentReadService`가 0%였습니다 (`WI-20260716-ATS-013-evidence-pack.md:143-149`).
- 영향: 고위험 운영 mutation의 request body, 확인 입력, 상태 전이, 실패/재시도, 권한 및 응답 연결이 회귀해도 현재 검증이 놓칠 수 있습니다.
- WI-017: 환불·권한보정·정산 mutation의 프론트 상호작용 테스트와 관리자 Controller/read 계약 테스트를 우선 추가합니다.

### P2-06 저장소 보상 및 일부 위험 경로의 직접 검증이 얇음

- WI-013 근거: `StorageMutationJournalService` 11.11% lines/0% branches, payment audit·legacy internals·download read·OAuth transport·playlist 일부 분기가 얇습니다 (`WI-20260716-ATS-013-evidence-pack.md:143-150`).
- 영향: DB rollback/commit 뒤 파일 정리, cleanup journal 재시도, 운영 read/audit 변형의 회귀 위험이 남습니다.
- WI-017: 임의 커버리지 수치가 아니라 rollback/after-commit/retry/idempotency와 운영 audit/read 계약을 위험 기반 테스트로 보강합니다.

### P2-07 유지 MySQL 정합성/경쟁 증거는 아직 환경 조건부임

- WI-013에서 MySQL 동시성 7건과 schema validation 1건이 `ATSTUDIO_MYSQL_PROOF_ENABLED=true` 조건 때문에 skipped였습니다 (`WI-20260716-ATS-013-evidence-pack.md:112-116,169-170`).
- 영향: H2/소스 검증을 실제 유지 MySQL schema·lock·race 증거로 주장할 수 없습니다.
- WI-017: 승인된 disposable/copied MySQL 환경에서 8건을 실행하고 결과를 릴리스 증거에 포함합니다. 운영 DB나 현재 데이터에는 직접 적용하지 않습니다.

### P3-01 소셜 로그인은 검증된 return target을 잃음

- `LoginPage.tsx:137,228-262`은 password 로그인에만 안전한 `returnTo`를 사용하고, `SocialLoginPage.tsx:72-77` 및 `SocialCompleteProfilePage.tsx:140`은 `/`로 이동합니다.
- WI-017: OAuth 시도별 session 저장, 1회 소비, 외부/권한 경로 거부 테스트를 추가합니다.

### P3-02 재생목록 삭제 유스케이스가 물리 삭제로 오기입됨

- 구현/승인 근거: `PlaylistService.java:254-262`, `Playlist.java:42-44`, `PlaylistServiceTest.java:465-492`, `REQ-20260221-ATS-004.md:25`는 track 관계 삭제 후 playlist 소프트 삭제입니다.
- 오기입: `docs/design/usecase/sound-playlist.md:178-181`은 playlists 레코드까지 삭제한다고 적습니다.
- WI-017: `playlist_tracks` 물리 삭제 + `playlists.is_active=false`로 바로잡습니다.

### P3-03 활성 프론트 표준/router 주석과 문서 인덱스 수치가 오래됨

- `docs/standards/frontend-standards.md:305-313`은 49 screens, `frontend/src/router/index.tsx:139`는 `49 screens + 2 error pages`라고 적습니다.
- 현재 단위: 62 path routes + 1 index redirect, 54 lazy page components, `/playlists/new` modal adapter를 제외한 53 distinct visual UIs입니다.
- `docs/index.md:21-40`은 Standards 12개/총 193개로 적지만, 현재 index 제외 Standards는 13개이고 관리 문서 합계는 194개입니다.
- WI-017: 화면 고정 숫자를 현재 단위로 수정하거나 `docs/ui/atstudio-front-list.md`의 카운트 계약으로 연결하고, 문서 인덱스를 Standards 13/총 194로 동기화합니다.

### P3-04~06 WI-013 운영성 잔여사항

- symlink 생성이 불가능한 호스트에서 `LocalStorageServiceTest.java:68-88`의 symlink 분기가 abort되었습니다.
- 전체 JaCoCo는 1 GiB heap과 약 2분이 필요해 120초 외부 wrapper에 민감합니다 (`build.gradle:67-83`).
- Gradle이 파일 위치 없는 unchecked/unsafe compiler warning을 출력합니다.
- WI-017: symlink-capable 환경 증거를 추가하고, 검증 timeout 예산을 명시하며, compiler warning의 발생 파일을 식별해 별도 정비 여부를 결정합니다.

## High-Risk Matrix

| 영역 | 결과 | 근거 요약 |
|---|---|---|
| 결제/정기결제/0원 재등록 | 조건부 적합 | `BillingAgreementApplicationService.java:107-157`, `PaymentCommandTransactionService.java:493-523`, `api-spec.md:1439-1523`; P2-02/P2-05 제외 |
| 환불/권한보정/정산/대사 | 조건부 적합 | `AdminPaymentController.java:55-280`, `frontend/src/api/admin.ts:216-801`; mutation 회귀 증거 보강 필요 |
| 화이트리스트/CSV export | 적합 | `AdminWhitelistChannelService.java:48-65`, `WhitelistChannelService.java:85-200`, `whitelistStatusTransitions.ts:3-12`, `api-spec.md:3248-3289` |
| 기업 인증 | 역할 경계 외 적합 | DTO/업로드/감사 계약은 일치; P2-01 필요 |
| 인증/보호 경로/OAuth | 조건부 적합 | password safe return과 role guard는 적합; P3-01 필요 |
| 다운로드/라이선스 | 적합, MySQL 증거 조건부 | `TrackController.java:82-91`, `DownloadService.java:40-91`, `api-spec.md:503-515`; P2-07 필요 |
| 카탈로그/재생목록 | 기능 적합 | 소유권·한도·잠금·정렬 계약 일치; P3-02 문서 정정 필요 |
| 공개 전체 재생 | 적합 | `SecurityConfig.java:73-76,87`, `TrackController.java:94-152`, `TrackService.java:151-160`, `api-spec.md:488-501`; 별도 preview/cutoff 없음 |
| 프론트 오류/경쟁 상태 | 미완 | P2-03/P2-04 |
| 클라이언트/PDF 안내 | 조건부 적합 | 전체 재생·다운로드·결제 환경 경계는 정확; P2-02 문구 충돌이 PDF에도 전파됨 |

## 수치 계약

| 항목 | 현재 단위와 수치 |
|---|---|
| REST API | method-level mapping 149개; `SpaForwardController` 1개 제외 |
| DB | `CREATE TABLE` 41개 / JPA `@Entity` 41개 |
| Router/UI | path 62개 + index redirect 1개 / lazy page 54개 / visual UI 53개 |
| Modal | `<Modal` 호출 23개 / non-test TSX 17개 |
| Agent | `.claude/agents/*.md` 13개 |
| SR | 92개: DONE 82 / OPEN 7 / NOT CONFIRMED 2 / DROPPED 1 |
| Managed docs | index 제외 카테고리 합계 194개; `docs/index.md`의 193은 1개 부족 |

## WI-013 / WI-014 처분

- WI-013 P2 coverage debt: **유지 및 P2-05/P2-06으로 구체화**.
- WI-013 MySQL skipped: **유지, P2-07 환경 조건부**.
- WI-013 symlink/heap/warning: **유지, P3-04~06**.
- WI-014 F-014-01~03: **유지, P2-03~05**.
- WI-014 F-014-04: **유지, P3-01**.
- WI-014 F-014-05: **유지 및 활성 frontend standard까지 범위 확정, P3-03**.
- 이번 검토 신규: **P2-01, P2-02, P3-02**.

## WI-017 우선순위

1. 기업 인증 USER 역할 강제와 테스트.
2. Provider 결제 식별자 표시/문서 계약 단일화.
3. PlayerBar 구독 상태 분리와 네 화면의 비동기 경쟁 차단.
4. 관리자 결제 mutation/read 및 저장소 보상 위험 테스트.
5. disposable/copied MySQL 증거 실행.
6. OAuth return, 플레이리스트 삭제 문서, 화면 수 문구 및 P3 운영성 잔여사항 정리.

WI-017 완료 전에는 현재 상태를 “릴리스 준비 완료”로 닫지 않는 것이 타당합니다.
