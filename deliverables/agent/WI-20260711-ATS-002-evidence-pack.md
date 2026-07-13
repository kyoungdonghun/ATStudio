# Evidence Pack: WI-20260711-ATS-002

## TL;DR

- Completed a static, read-only backend architecture and domain audit across the requested domains.
- Confirmed 2 P0 defects, 5 P1 defects, and 5 P2 defects/race windows.
- Re-verified every CRITICAL, MAJOR, and MINOR heading in the historical backend audit. All 5 CRITICAL and all 15 MAJOR items are fixed or reworked. Of 11 MINOR body items, 9 are fixed and 2 remain open.
- No production code, existing user files, APIs, databases, providers, or external services were changed or invoked.

## Summary (one-liner)

- Produced the current backend domain map, evidence-backed defect inventory, historical audit status map, performance risks, policy questions, and focused regression-test inputs.

## Scope / DoD Check

- DoD items:
  - [x] Traced controller -> service -> repository/provider flows for payment/subscription, whitelist, company certification, users/auth, music/search, admin operations, and supporting content/library domains.
  - [x] Reviewed transaction boundaries, idempotency assumptions, state transitions, external-side-effect compensation, scheduler behavior, and exception handling.
  - [x] Identified legacy/unreachable behavior and policy ambiguity separately from confirmed defects.
  - [x] Flagged N+1/unbounded query, scheduler scan, pagination, and payload risks without presenting unverified SQL behavior as a confirmed failure.
  - [x] Re-verified the historical CRITICAL/MAJOR/MINOR findings against current code.
  - [x] Listed focused backend test gaps for later WIs.
  - [x] Used narrow file/line or command evidence for each material conclusion.

## Baseline and Constraints

| Item | Evidence |
|---|---|
| Branch | `git branch --show-current` -> `dev/kyoung` |
| HEAD | `git rev-parse HEAD` -> `27d22446e5d21324dadcfcb322dbe51704dfe914` |
| Working tree | Dirty before this WI. Existing `docs/client/` changes, WI handoffs, REQ, and PDF were treated as user/concurrent-agent assets and left untouched. |
| Existing WI outputs | Both owned output paths were absent before creation. |
| Main inventory | 25 controller files, 64 Java files under the service subtree, 74 under the entity subtree, 41 repositories, 125 DTOs, and 71 test files. |
| Execution mode | Static inspection only. No Gradle test/build, DB, HTTP API, provider, email, or filesystem mutation command was run. |
| Owned writes | `deliverables/user/WI-20260711-ATS-002-summary.md`; `deliverables/agent/WI-20260711-ATS-002-evidence-pack.md` |

## Reference Documents (Tier 0-2)

**Injected Context** (from the WI Handoff Packet):

| Tier | Document | Use |
|---|---|---|
| 0 | `docs/standards/core-principles.md` | Constitution, platform integrity, financial traceability |
| 0 | `docs/standards/development-standards.md` | Layering, transaction, exception, JPA, N+1, and test evidence rules |
| 1 | `docs/policies/security-policy.md` | Secret/PII and logging baseline |
| 1 | `docs/policies/quality-gates.md` | Evidence, regression, risk, and rollback expectations |
| 2 | `docs/design/` | API, state model, payment, DB, and use-case intent |
| 2 | `docs/payment/` | Current payment boundary, operations, known limits, and single-server assumption |
| 2 | `docs/adr/` | Existing decision record inventory; no payment transaction-boundary ADR exists |
| 2 | `docs/guides/` | Handoff pointer is stale: directory does not exist (`Test-Path docs/guides` -> `False`) |
| Context | `deliverables/user/REQ-20260711-ATS-001.md` | Approved full-audit scope and read-only constraints |
| Historical | `docs/audit/backend-audit-report.md` | Historical evidence only; every requested severity item was re-verified |

**Selected current design anchors:**

- `docs/design/payment-integration-design.md:511-545` - renewal sequence, idempotency, and compensation requirements.
- `docs/design/payment-operations-runbook.md:98-110,130-167` - provider/local mismatch detection and manual compensation boundary.
- `docs/payment/system-overview.md:142-151` - scheduler inventory and single-server assumption.
- `docs/payment/known-limits-and-next-steps.md:49-56` - multi-server scheduler lock intentionally deferred.
- `docs/design/usecase/whitelist.md:10-21,136-164` - whitelist state meanings and plan-limit behavior.
- `docs/design/usecase/sound-track.md:119-138` - public preview fallback to original audio.
- `docs/design/usecase/user-info.md:259-281` - withdrawal intent.

**Injection Rules Applied:**

- Assignee: `sa`
- Task type: architecture/review
- Required tiers: Tier 0 plus inferred Tier 1 and domain Tier 2
- Ordering: Tier 0 -> policies -> domain context -> current source snapshot

## Static Domain Map

| Domain | Entry and orchestration | Persistence/provider path | State/transaction notes |
|---|---|---|---|
| Users and auth | `AuthController.java:14-19`; `UserController.java:17-21`; `AuthService.java:28-36`; `UserService.java:20-34` | `UserRepository.java:13-37`; token repositories; `OAuth2Service.java:60-103`; `EmailService.java:46-108` | Login/refresh/profile/withdrawal are transactional service operations; OAuth and email cross external boundaries. |
| Tracks, search, stream, download | `TrackController.java:20-26,43-94`; `TrackService.java:42-59,92-145`; `DownloadService.java:27-40` | `TrackRepository.java:16-30`; `TrackTagRepository.java:13-23`; license/download repositories; `StorageService` | Public metadata/stream path is separate from subscriber download/license path. File storage is not transactional with JPA. |
| Tags and discovery | `TagController.java:18-22,36-57`; `TagService.java:19-45` | `TagRepository`; `TrackSpecification.java:31-92` | Specification-based multi-tag filtering; response-shape standard currently conflicts with API/code. |
| Library and engagement | `PlaylistController.java:19-23`; `PlayHistoryController.java:17-21`; `LikeController.java:18-23`; `AlbumController.java:20-24`; `DownloadQueueController.java:16-20`; `LicenseController.java:16-18` | Playlist/track, history, like, album, queue, and license repositories | Subscriber checks live in services. Several list endpoints are unbounded; derived counters/limits use read-then-write patterns. |
| Subscription and billing | `UserSubscriptionController.java:15-20`; `PaymentController.java:31-36`; `UserSubscriptionService.java:54-74`; `BillingAgreementApplicationService.java:108-245` | User subscription, payment order, subscription payment, billing agreement repositories; recurring provider interfaces | First charge, upgrade, cancellation/reactivation, and renewal combine local state with provider calls. |
| Renewal and reconciliation | `SubscriptionScheduler.java:21-75`; `RecurringRenewalService.java:84-164`; `PaymentReconciliationService.java:48-195` | Billing agreement/payment order/subscription payment repositories; provider lookup | Renewal scans all due agreements in one transaction. Reconciliation checks only the latest 100 orders and all active agreements. |
| Payment admin operations | `AdminPaymentController.java:56-64,66-268` | Read, refund, entitlement correction, incident, settlement, audit, and receipt services/repositories | Refund/provider execution has a ledger and idempotency key, but aggregate refund reservation is not locked. |
| Whitelist | `WhitelistChannelController.java:18-22,26-90`; `AdminWhitelistChannelController.java:30-61` | `WhitelistChannelService.java:24-39`; `AdminWhitelistChannelService.java:35-53`; whitelist/export repositories | User request states and admin export states are persisted. Limit/primary invariants are application-only. |
| Company certification | `CompanyCertificationController.java:27-31,35-117`; `CompanyCertificationService.java:41-55,57-223` | Certification/document/user repositories plus storage | Entity transition validation exists; file cleanup uses transaction synchronization. |
| Inquiry, notice, settings, stats | `QuestionController.java:21-25`; `NoticeController.java:22-26`; settings controllers; `AdminStatsController.java:14-20` | Question/answer/attachment, notice/attachment, settings, aggregate repositories | Inquiry and notice attachments expose inconsistent DB/file compensation behavior. |

## Severity and Classification

- **P0:** release-blocking loss of financial control, paid-content control, or secret confidentiality.
- **P1:** high-impact correctness/recovery defect requiring the first remediation wave.
- **P2:** material edge-case, concurrency, availability, or maintainability defect for planned remediation.
- **P3:** low-risk cleanup or explicit deprecation.
- **Confirmed defect:** current control flow and state effect are directly established by source/configuration.
- **Improvement:** risk is real but no current incorrect result was proven from static inspection alone.
- **Policy ambiguity:** code is internally consistent enough to run, but the intended product/operating rule is incomplete or conflicting.

## Confirmed Defects

### BE-001 - Anonymous raw original-audio access bypasses the paid download path

- Priority/class: **P0 / confirmed defect**
- Evidence:
  - Public track detail is permitted: `SecurityConfig.java:66-69`.
  - Detail DTO returns the storage key: `TrackResponse.java:10-20,29-39`.
  - Track detail passes `audioFile` into client player state: `frontend/src/pages/tracks/TrackDetailPage.tsx:139-148`.
  - Normal client playback nevertheless builds the public stream URL from the track ID, not `audioFile`: `frontend/src/store/playerStore.ts:173-175`. This limits the frontend observation but does not close the independently callable static-resource route.
  - Storage keys are relative `tracks/audio/...` values: `LocalStorageService.java:51-70`.
  - `/uploads/**` maps directly to the storage root: `WebConfig.java:20-25`.
  - Only `/uploads/company-docs/**` is protected; non-API fallback is `permitAll`: `SecurityConfig.java:80-82,129-132`.
  - The intended paid path applies subscription, quota, ledger, and license checks: `DownloadService.java:48-78`.
- Static reproduction:
  1. Anonymous caller reads `GET /api/tracks/{id}`.
  2. Caller takes `data.audioFile`, for example `tracks/audio/key.mp3`.
  3. Caller requests `/uploads/{audioFile}` and bypasses `DownloadService.download()`.
- Impact: original paid media is retrievable without subscription, quota accounting, download history, or license issuance.
- Recommended action: remove original storage keys from public DTOs; stop mapping protected media through generic static resources; serve preview and download through separately authorized endpoints or signed URLs.
- Missing test: anonymous `MockMvc`/resource-handler integration test proving `/uploads/tracks/audio/**` is denied while public thumbnails/previews remain available.

### BE-002 - Account withdrawal does not stop automatic renewal

- Priority/class: **P0 / confirmed defect**
- Evidence:
  - Withdrawal deletes selected auxiliary rows and only calls `user.withdraw()`: `UserService.java:104-122`.
  - `User.withdraw()` sets `isDeleted` and clears refresh token only: `User.java:81-84`.
  - The normal cancellation path cancels both subscription and local billing agreement: `UserSubscriptionService.java:247-259`.
  - Due agreements are queried without a deleted-user predicate: `BillingAgreementRepository.java:26-29`.
  - Renewal accepts the agreement's active subscription and calls the provider: `RecurringRenewalService.java:89-100,123-159`.
- Static reproduction: withdraw a password user with an ACTIVE subscription/agreement before `nextBillingAt`; the user becomes non-loginable but the agreement remains ACTIVE and is still selected by the renewal job.
- Impact: a withdrawn customer can be charged after account closure.
- Recommended action: make withdrawal orchestrate stop-renewal atomically, define provider-key retention/deletion policy, and add a defensive `user.isDeleted=false` renewal predicate.
- Missing test: transaction/integration scenario `withdraw -> due scheduler` asserting zero provider calls and CANCELLED/non-renewable local states.

### BE-003 - Initial billing failure ledger changes roll back with the reported error

- Priority/class: **P1 / confirmed defect**
- Evidence:
  - `confirmBillingAgreement()` is a default rollback transaction: `BillingAgreementApplicationService.java:161-172`.
  - On provider charge failure it marks order/agreement failure, attempts key cleanup, then throws `BusinessException`: `BillingAgreementApplicationService.java:212-226`.
  - Unlike the upgrade path, it does not use `noRollbackFor = BusinessException.class`: compare `UserSubscriptionService.java:118-180`.
  - Existing test is Mockito-only and observes in-memory state without a Spring transaction: `BillingAgreementApplicationServiceTest.java:310-349`.
  - Design requires failed orders to be persisted: `docs/design/payment-integration-design.md:521-540,652-660`.
- Impact: provider-declined initial charges can leave the durable order at `IN_PROGRESS`, failure count unchanged, and operations evidence inconsistent with the API error.
- Recommended action: persist provider result in a separate transaction/state transition before returning the business error, or use an explicit no-rollback outcome contract limited to expected provider failure.
- Missing test: Spring transaction integration test that reloads order/agreement after the exception.

### BE-004 - Renewal batch combines unbounded work and external charges in one transaction

- Priority/class: **P1 / confirmed defect**
- Evidence:
  - Scheduler starts a transaction around the delegated run: `SubscriptionScheduler.java:32-36`.
  - Renewal service joins/starts one transaction, loads all due agreements, and loops them: `RecurringRenewalService.java:84-105`.
  - Repository returns an unpaged `List`: `BillingAgreementRepository.java:26-29`.
  - Provider charge occurs inside that loop/transaction: `RecurringRenewalService.java:147-159`.
  - `subscription_payments.payment_order_id` has no unique constraint: `schema.sql:519-538`; entity mapping is many-to-one: `SubscriptionPayment.java:36-42`.
- Impact: one unexpected runtime/flush/commit failure can roll back local rows for earlier successful provider charges. Large due sets create long transactions and provider latency amplifies lock/connection pressure.
- Recommended action: page/claim due agreements, process each agreement in an isolated transaction, persist an attempt before the provider call, finalize by idempotent order/period key, and add a unique local finalization invariant.
- Policy note: multi-server scheduler lock is explicitly deferred under a single-server assumption (`docs/payment/known-limits-and-next-steps.md:49-56`). It becomes a defect as soon as more than one scheduler instance is deployed.
- Missing tests: two-agreement run where the second throws after the first provider success; concurrent duplicate-run integration test.

### BE-005 - Concurrent refund requests can over-reserve one payment

- Priority/class: **P1 / confirmed race window**
- Evidence:
  - Create flow loads a payment, validates available amount, then inserts a random-idempotency refund: `AdminPaymentRefundService.java:89-111`.
  - Availability is computed by aggregate sum with no lock: `AdminPaymentRefundService.java:248-262`.
  - Repository locks individual refund rows only during later execution; aggregate sum query is unlocked: `PaymentRefundRepository.java:45-57`.
- Impact: two requests can both observe the same refundable balance and reserve more than the original payment. Distinct idempotency keys make them distinct provider operations.
- Recommended action: lock the source `SubscriptionPayment` while reserving, or maintain an atomic reserved/refunded amount invariant with a database check/serializable command.
- Missing test: concurrent create requests against a real DB, followed by execution eligibility checks.

### BE-006 - SMTP fallback logs verification/reset tokens and PII

- Priority/class: **P1 / confirmed defect**
- Evidence:
  - Verification/reset URLs embed raw tokens: `EmailService.java:46-65,88-108`.
  - On any send exception, recipient, subject, and full HTML body are logged: `EmailService.java:163-179`.
  - Tokens are secrets and sensitive logging must be minimized: `docs/policies/security-policy.md:26-35,41-45`.
- Impact: anyone with application log access can take over email verification or password reset, and recipient PII is disclosed. The exception is swallowed, so the API can claim delivery while only logging the token.
- Recommended action: remove body/recipient fallback logging, return/record a safe delivery outcome, and use a local-only mail sink under an explicit profile.
- Missing test: captured-log assertion that no token, email, or HTML body appears on mail failure.

### BE-007 - File storage and DB state use inconsistent compensation rules

- Priority/class: **P1 / confirmed defect family**
- Verified scenarios:

| Scenario | Classification and evidence |
|---|---|
| New-file orphan after rollback or partial failure | **Confirmed compensation gap.** Track stores audio/thumbnail before duration, DB, and tag operations (`TrackService.java:60-88,248-253`). Album and Playlist store thumbnails before DB persistence/dirty-check completion (`AlbumService.java:42-60,113-126`; `PlaylistService.java:39-64,178-193`). Question and Notice store each attachment before its row save, with no cleanup of already stored files if a later store/save/commit fails (`QuestionService.java:49-66,207-223`; `NoticeService.java:43-59,92-116,159-173`). Filesystem writes are outside the DB transaction, so rollback cannot undo them. |
| Rollback data loss / broken DB reference | **Confirmed ordering defect.** Track replaces the entity path and immediately deletes the old audio/thumbnail before waveform, tag, flush, or commit completion (`TrackService.java:148-179`). Notice update/delete deletes attachment files before attachment/notice rows commit (`NoticeService.java:92-132`). A later runtime/flush/commit failure rolls the DB back to rows that reference files already removed. |
| Orphan on successful operation | **Confirmed lifecycle defect.** Album and Playlist replacement stores a new thumbnail and updates the entity but never deletes the previous path (`AlbumService.java:113-126`; `Album.java:47-51`; `PlaylistService.java:178-193`; `Playlist.java:36-40`). Question deletion removes attachment rows but never the files (`QuestionService.java:173-188`). |
| Positive comparator | Company Certification deletes already stored files on partial batch failure, registers new paths for rollback cleanup, and defers prior-path deletion until after commit (`CompanyCertificationService.java:266-307`). Unit tests manually exercise after-commit and rollback callbacks (`CompanyCertificationServiceTest.java:202-260`). |

- Impact: failed writes accumulate orphan files; rollback can restore DB rows whose files were permanently deleted; successful replacement/deletion leaks stale or private content. Local deletion failures are only logged (`LocalStorageService.java:79-87`), so an after-commit callback alone also needs an operational retry path.
- Shared remediation: introduce one transaction-aware file mutation coordinator used by every domain. It must (1) immediately clean up a partially stored batch, (2) register every new path for deletion on rollback, (3) defer superseded/deleted-path removal until after commit, and (4) persist or reconcile failed post-commit deletions. Keep domain retention rules explicit for soft-deleted Track/Album/Playlist media; do not duplicate callback code in each service.
- Missing tests: real Spring transaction tests covering create/replace/delete commit and rollback in each domain, partial multi-file failure, DB flush/commit failure, and retry/reconciliation after post-commit delete failure. Existing Company Certification tests invoke synchronization callbacks manually and are not transaction integration tests.

### BE-008 - Social-only users cannot self-withdraw

- Priority/class: **P2 / confirmed unreachable behavior**
- Evidence:
  - Social signup intentionally stores `password=null`: `OAuth2Service.java:81-94`.
  - Withdrawal rejects null password before any alternative proof: `UserService.java:104-112`.
  - Withdrawal contract applies to a generic authenticated member and does not document a social exclusion: `docs/design/usecase/user-info.md:259-281`.
- Impact: social-only members have no successful self-service withdrawal path.
- Recommended action: add recent re-authentication/provider proof or a separate authenticated confirmation policy for social-only accounts.
- Missing test: social user withdrawal success and re-authentication requirements.

### BE-009 - OAuth token response does not validate the required access token

- Priority/class: **P2 / confirmed defect; historical CR-C-013 remains open**
- Evidence: provider response Map is null-checked, but `response.get("access_token")` is returned without null/blank/type validation for all three providers: `OAuth2Service.java:121-159`.
- Impact: malformed provider responses propagate null into user-info calls and become an unrelated provider/client exception or generic 500 rather than safe `SOCIAL_AUTH_FAILED`.
- Recommended action: parse provider responses into typed DTOs and validate required fields before use.
- Missing test: non-null Map with missing/null/non-string `access_token` for Google/Kakao/Naver.

### BE-010 - Album track-count sorting is page-local, not globally ordered

- Priority/class: **P2 / confirmed functional defect**
- Evidence:
  - DB first returns a page ordered by creation date, then only that page is sorted by `trackCount`: `AlbumService.java:74-87`.
  - Contract says active albums are sorted by the selected mode: `docs/design/usecase/sound-album.md:41-56`.
- Impact: albums with the highest track count can appear on later pages; each page is internally sorted but the overall result is not.
- Recommended action: query/group/order at DB level, materialize a maintained count, or fetch all only if a deliberately small bounded catalog policy is approved.
- Missing test: at least two pages with cross-page track-count inversion.

### BE-011 - Malformed HTTP Range input can become a 500

- Priority/class: **P2 / confirmed input-handling defect**
- Evidence: stream endpoint manually parses numbers and does not validate start/end bounds; only `IOException` is caught: `TrackController.java:90-137`.
- Impact: malformed or out-of-range public requests can throw `NumberFormatException`/`IllegalArgumentException` and reach the generic 500 handler.
- Recommended action: use Spring `HttpRange` parsing, validate satisfiable ranges, and return 400/416.
- Missing test: malformed, suffix, multi-range, start-after-EOF, and end-before-start requests.

### BE-012 - Business limits are enforced by unlocked count-then-write sequences

- Priority/class: **P2 / confirmed race windows**
- Evidence:
  - Playlist limit: `PlaylistService.java:39-63`.
  - Whitelist plan slots: `WhitelistChannelService.java:119-132`.
  - Daily download quota: `DownloadService.java:58-78`.
  - Whitelist primary selection also has no uniqueness/lock: `WhitelistChannelService.java:135-145`; schema has only non-unique indexes: `schema.sql:230-252`.
- Impact: concurrent requests can exceed plan/quota limits or leave multiple primary channels.
- Recommended action: serialize on user/subscription, use atomic counters or enforceable DB constraints, and keep application checks for friendly errors.
- Missing tests: concurrent integration tests for each invariant.

## Improvements and Performance Risks

| ID | Priority | Evidence | Assessment and action |
|---|---|---|---|
| IMP-001 | P2 systemic availability/performance | `AdminPaymentReadService.java:84`; `AdminPaymentRefundService.java:280`; `AdminPaymentSettlementService.java:137`; `CompanyCertificationService.java:151`; `UserService.java:174`; `UserSubscriptionService.java:95`; `TrackService.java:99,209`; `LicenseService.java:57` | Many pageable services use `Math.max(1, size)` or pass caller size without an upper bound. Public endpoints make direct abuse possible; authenticated/admin endpoints still permit accidental or privileged resource exhaustion. Treat this as one shared policy gap: enforce a centrally configured maximum in a common `Pageable` factory/argument validator (with one documented clamp-or-400 rule), then migrate callers instead of duplicating endpoint fixes. |
| IMP-002 | P2 | `LikeService.java:51-55`; `DownloadQueueService.java:60-64`; `PlaylistService.java:69-88`; `SubscriptionService.java:42-45` | Several growing user/admin collections are returned as full lists. Introduce pagination or a documented small bounded invariant. |
| IMP-003 | P2 | `PaymentReconciliationService.java:58-83,109-132`; `BillingAgreementRepository.java:34-35` | Reconciliation checks only the latest 100 orders but scans every ACTIVE agreement and performs one active-subscription lookup per agreement. Use time/cursor windows and batch joins; record the coverage watermark. |
| IMP-004 | P2, verify with SQL | `TrackRepository.java:18-20`; `TrackService.java:99-116` | A pageable specification query entity-fetches a collection. Hibernate may need duplicate elimination/in-memory pagination. Add a real JPA query-count/SQL test and prefer page IDs plus batch tag fetch if confirmed. |
| IMP-005 | P3 | `PaymentApplicationService.java:91-96,99-139`; `UserSubscriptionService.java:76-82`; controller routes in `PaymentController.java:38-61` and `UserSubscriptionController.java:24-34` | Compatibility endpoints remain exposed but always reject current subscription flows. Mark them deprecated with a removal version or remove after consumer verification. |

## Policy Ambiguity / Contract Questions

| ID | Evidence | Question and required decision |
|---|---|---|
| POL-001 | `TrackService.java:140-145`; `Track.java:37-41`; `docs/design/usecase/sound-track.md:119-138`; `frontend/src/store/playerStore.ts:173-175` | The normal player uses the public stream endpoint, whose documented implementation deliberately falls back to original audio when `previewFile` is absent; no current code writes `previewFile`. Whether full-original public **streaming** is acceptable is a policy ambiguity. It does not downgrade BE-001: obtaining the raw file through public `TrackResponse.audioFile` plus `/uploads/**` bypasses the separately modeled subscriber download/license flow and is a confirmed direct-download bypass. If full-original streaming is not intended, preview generation must be a publication precondition and range delivery must not expose the original. |
| POL-002 | `docs/payment/system-overview.md:142-151`; `docs/payment/known-limits-and-next-steps.md:49-56` | Scheduler locking is deferred for a single-server deployment. Record the deployment invariant and make multi-instance rollout fail a release gate until locking/claiming exists. |
| POL-003 | `AdminWhitelistChannelService.java:41-47,80-101`; `WhitelistChannel.java:105-114`; `docs/design/usecase/whitelist.md:191-210` | Admin update validates target status only, not source -> target transition. Decide whether operators need unrestricted recovery or a strict workflow matrix. |
| POL-004 | `TagController.java:50-57`; `docs/design/api-spec.md:524-545`; `docs/standards/development-standards.md:176-189` | Code and API spec use `ResponseDTO`, but the development standard names `GET /api/tags` as a raw-array exception. Align the standard; current code/API agree. |

## Historical Audit Re-verification

Historical source: `docs/audit/backend-audit-report.md`. Its summary says MINOR total is 10 (`:39-44`), but the MINOR section contains 11 unique body items when `CR-P-005` is included (`:320-435`). Every body item is mapped below; summary totals were not trusted as current evidence.

Current comparison outcome:

- **Remain fixed/reworked; historical defect now stale as a live finding: 29.** This comprises all 5 CRITICAL, all 15 MAJOR, and 9 MINOR body items.
- **Regressed after a recorded resolution: 0 confirmed.** No item represented as resolved in the historical remediation status was found reverted in current code.
- **Remain open/escalated: 2.** `CR-A-009` remains open and is escalated to BE-001; `CR-C-013` remains open as BE-009. Neither is labeled a regression because current evidence does not show an intervening verified fix.
- **Historical status metadata became stale.** The report still marks `CR-P-004` as a deferred development fallback and `CR-B-003` as verification-pending (`backend-audit-report.md:35-44`); current configuration removes the JWT fallback (`application.yml:49-52`) and subscription changes now implement upgrade-only immediate charge with scheduled downgrade (`UserSubscriptionService.java:148-223`). Its MINOR total also undercounts its own body by one.

### Historical CRITICAL (5/5 fixed)

| Historical ID | Current status | Current evidence |
|---|---|---|
| CR-P-001 - `/api/users/me` blocked | **FIXED** | Explicit authenticated rules precede admin wildcard: `SecurityConfig.java:83-93`. |
| CR-P-004 - JWT fallback secret | **FIXED** | Required environment placeholder only: `application.yml:49-52`. |
| CR-C-001 - question delete FK failure | **FIXED** | Attachments and answers deleted before question: `QuestionService.java:173-188`. |
| CR-C-002 - auth class transaction defaults | **FIXED** | `AuthService.java:27-30`; `OAuth2Service.java:24-27` use class-level read-only with mutating overrides. |
| CR-A-001 - unmapped `trackTags` join | **FIXED** | Mapping exists at `Track.java:72-74`; specification joins it at `TrackSpecification.java:67-76`. |

### Historical MAJOR (15/15 fixed or reworked)

| Historical ID | Current status | Current evidence |
|---|---|---|
| CR-A-002 - DownloadService transaction default | **FIXED** | `DownloadService.java:27-40` |
| CR-A-003 - unlimited download blocked | **FIXED** | `downloadPerDay != -1` guard: `DownloadService.java:58-64` |
| CR-A-004 - track-tag cleanup | **FIXED** | `TrackService.java:184-198` |
| CR-A-005 - license N+1 | **FIXED** | `@EntityGraph(track)`: `LicenseRepository.java:19-23` |
| CR-A-006 - playlist count N+1 | **FIXED** | Batch aggregate and map: `PlaylistService.java:72-87`; `PlaylistTrackRepository.java:19-20` |
| CR-B-001 - admin cancel 200 | **FIXED** | `UserSubscriptionController.java:99-107` returns 204 |
| CR-B-002 - self cancel 200 | **FIXED** | `UserSubscriptionController.java:109-117` returns 204 |
| CR-B-003 - downgrade sign/absolute amount | **FIXED / REWORKED** | Upgrade-only immediate charge; downgrade is scheduled: `UserSubscriptionService.java:148-223` |
| CR-C-003 - withdrawn users in admin list | **FIXED** | `UserRepository.java:30-37` filters `isDeleted=false` |
| CR-C-004 - duplicate status 400 | **FIXED** | `BUSINESS_ERROR.java:22-25` maps to 409 |
| CR-C-005 - latest certification nondeterminism | **FIXED** | Deterministic latest query: `CompanyCertificationRepository.java:13-16` |
| CR-C-006 - certification transition validation | **FIXED** | `CompanyCertification.java:50-87` |
| CR-C-007 - question transition validation | **FIXED** | `Question.java:46-60` |
| CR-C-008 - production TestController | **FIXED** | `rg --files src/main/java | rg 'TestController\.java$'` -> no match |
| CR-C-009 - duplicate of JWT fallback | **FIXED** | Same evidence as CR-P-004: `application.yml:49-52` |

### Historical MINOR body items (9 fixed, 2 open)

| Historical ID | Current status | Current evidence / note |
|---|---|---|
| CR-A-007 / CR-P-006 - raw tag list | **FIXED** | Code and API now use `ResponseDTO`: `TagController.java:50-57`; `api-spec.md:536-545`. Development standard is stale; see POL-004. |
| CR-A-008 - playlist-track cleanup | **FIXED** | `PlaylistService.java:235-243` |
| CR-A-009 - audio storage path exposure | **OPEN / ESCALATED** | `TrackResponse.java:18,38` plus static `/uploads/**` mapping creates BE-001. `TrackDetailPage.tsx:139-148` carries the value, although `playerStore.ts:173-175` uses the stream API for normal playback; direct static retrieval remains independently available. |
| CR-A-010 - missing stream resource check | **FIXED** | Central storage loader validates existence: `LocalStorageService.java:90-109` |
| CR-B-004 - invalid subscription userType 500 | **FIXED** | Enum parse translated to `INVALID_ARGUMENT`: `SubscriptionService.java:23-31` |
| CR-B-005 - YouTube URL `contains` bypass | **FIXED** | URI host validation: `WhitelistChannelService.java:166-176` |
| CR-C-010 - no phone uniqueness check | **FIXED at service level** | Register/profile availability check: `UserService.java:46-50,197-214`. Atomic DB/concurrency enforcement remains absent. |
| CR-C-011 - attachment missing BaseEntity | **FIXED** | `QuestionAttachment.java:7-13` |
| CR-C-012 - certification no-record returns null | **FIXED** | Explicit not-found error: `CompanyCertificationService.java:139-145` |
| CR-C-013 - OAuth token fields not validated | **OPEN** | `OAuth2Service.java:121-159`; see BE-009. |
| CR-P-005 - expired refresh accepted | **FIXED** | Explicit expired/invalid rejection: `AuthService.java:75-85` |

## Test Inventory and Gaps

### Existing coverage shape

- `rg` inventory: 71 test files.
- Annotation count: 18 `@SpringBootTest`, 4 `@DataJpaTest`, 35 Mockito-extension test classes, and no `@WebMvcTest`.
- Repository integration tests are limited to:
  - `BillingAgreementRepositoryTest.java`
  - `LikeRepositoryTest.java`
  - `TrackTagRepositoryTest.java`
  - `UserRepositoryTest.java`
- Payment orchestration tests such as `BillingAgreementApplicationServiceTest`, `RecurringRenewalServiceTest`, and `AdminPaymentRefundServiceTest` are Mockito unit tests. They do not verify commit/rollback, row locking, constraints, or concurrent execution.

### Required focused tests

| Priority | Test | Covers |
|---|---|---|
| P0 | Anonymous protected-media resource integration test | BE-001 |
| P0 | Withdrawal with active subscription/agreement followed by due renewal | BE-002 |
| P1 | Reload order/agreement after provider-decline exception in a real transaction | BE-003 |
| P1 | Two-agreement renewal with second-item failure and first provider success | BE-004 |
| P1 | Concurrent refund reservation against one `SubscriptionPayment` | BE-005 |
| P1 | Mail failure captured-log secret/PII assertion | BE-006 |
| P1 | File create/replace/delete commit and rollback matrix | BE-007 |
| P2 | Social-only account withdrawal | BE-008 |
| P2 | Missing/null/wrong-type OAuth `access_token` | BE-009 |
| P2 | Cross-page album `trackCount` ordering | BE-010 |
| P2 | Range parser 400/416 matrix | BE-011 |
| P2 | Concurrent playlist/whitelist/download-limit enforcement | BE-012 |
| P2 | Real JPA track specification plus pageable query-count/SQL behavior | IMP-004 and historical CR-A-001 regression |

## Commands & Outputs

- `git branch --show-current` -> `dev/kyoung`
- `git rev-parse HEAD` -> `27d22446e5d21324dadcfcb322dbe51704dfe914`
- `git status --short --untracked-files=all` -> dirty pre-existing client-doc/WI/REQ/PDF baseline; no user asset was reverted.
- `rg --files src/main/java src/test/java` -> source/test inventory used for scope.
- `rg` line searches over controllers, services, repositories, entities, DTOs, schedulers, exception handling, and tests -> evidence pointers in this pack.
- `Test-Path docs/guides` -> `False`.
- `rg --files src/main/java | rg 'TestController\.java$'` -> no match.
- Tests/builds: **not run**. Handoff specified static inspection, and running Gradle would write outside the two owned output paths.

## Risks / Limitations / Rollback

- Static inspection did not verify live Toss state, production configuration, real DB schema drift, SQL query counts, or actual HTTP resource-handler behavior.
- BE-001 is classified as confirmed from unambiguous Spring security/resource mappings and DTO flow, but no live download request was executed.
- Concurrency findings identify source-level race windows; their frequency depends on deployment/request concurrency.
- The workspace is shared and dirty. File/line pointers match the inspected snapshot and can drift if another agent changes backend files later.
- Rollback: only remove the two WI output files if explicitly requested:
  - `deliverables/user/WI-20260711-ATS-002-summary.md`
  - `deliverables/agent/WI-20260711-ATS-002-evidence-pack.md`

## Follow-up Inputs

- **WI-20260711-ATS-006 (payment 3-way):** BE-002 through BE-005, IMP-003, POL-002, payment test gaps.
- **WI-20260711-ATS-007 (whitelist/company certification 3-way):** BE-007, BE-012, POL-003, company-certification cleanup pattern as reuse candidate.
- **WI-20260711-ATS-008 (users/subscription/music/search/admin 3-way):** BE-001, BE-006, BE-008 through BE-012, IMP-001/002/004/005, POL-001/POL-004.
- **Testing WIs:** prioritize transaction integration, protected-media access, concurrency, and file lifecycle before broad coverage metrics.
- **Architecture decision input:** create a payment transaction-boundary ADR before remediation because provider calls, durable attempt state, compensation, scheduler claiming, and idempotent finalization require one coherent design.
