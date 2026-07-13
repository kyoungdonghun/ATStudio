# Evidence Pack: WI-20260711-ATS-016

## Summary (one-liner)

- Independently adjudicated injected P0/P1 security and payment findings into a deduplicated release-blocking verdict with strict CONFIRMED, CONDITIONAL, and REJECTED status.
- Count basis: 19 canonical adjudication rows total: 16 CONFIRMED rows (3 P0 plus 13 P1), 3 CONDITIONAL rows (ATS016-SEC-09 through ATS016-SEC-11), 15 immediate release blockers (ATS016-SEC-01 through ATS016-SEC-07 plus ATS016-PAY-01 through ATS016-PAY-08), and ATS016-SEC-08 retained as confirmed P1 but not a standalone release blocker.

## Scope / DoD Check

- [x] Reassessed every P0/P1 security/payment finding in the five injected evidence packs: WI-004, WI-006, WI-008, WI-009, and WI-015.
- [x] Included individual IDs embedded in compound rows, including BE/FE/INT aliases carried by WI-006 and WI-015.
- [x] Re-opened current high-risk source anchors and updated moved file paths/line pointers.
- [x] Deduplicated root causes, endpoint/state-transition failures, and overlapping IDs.
- [x] Separated direct facts, exploit prerequisites, maximum impact, and release-blocker rationale.
- [x] Produced the 15-row immediate release-blocker set, 3-row conditional blocker set, retained non-standalone ATS016-SEC-08 classification, and first-wave remediation order.
- [x] Did not run exploits, HTTP/provider/DB mutation, secret reads, build, or test commands.
- [x] Wrote only the two owned WI-016 output files.
- [x] Verified final git status and confirmed the WI-016 write set contains only the two owned output paths. Final status also showed concurrent/unowned WI-018 outputs not written by this run.

## Reference Documents (Tier 0-2)

**Injected Context** (from WI Handoff Packet):

| Tier | Document | Reason |
|---|---|---|
| 0 | `docs/standards/core-principles.md` | Constitution, platform integrity, payment traceability |
| 0 | `docs/standards/development-standards.md` | Review, transaction, JPA, and evidence standards |
| 1 | `docs/policies/security-policy.md` | Secrets, PII, JWT, CORS, token storage, environment boundary |
| 1 | `docs/policies/quality-gates.md` | High-risk review and evidence requirements |
| 2 | `docs/design/payment-integration-design.md` | Recurring billing, payment-order, subscription-change, and reconciliation design |
| 2 | `docs/design/api-spec.md` | Current API contract baseline |
| REQ | `deliverables/user/REQ-20260711-ATS-001.md` | Approved audit scope |
| Input | `deliverables/agent/WI-20260711-ATS-004-evidence-pack.md` | Security/privacy findings |
| Input | `deliverables/agent/WI-20260711-ATS-006-evidence-pack.md` | Payment 3-way findings |
| Input | `deliverables/agent/WI-20260711-ATS-008-evidence-pack.md` | Non-payment security adjudication |
| Input | `deliverables/agent/WI-20260711-ATS-009-evidence-pack.md` | Backend regression baseline |
| Input | `deliverables/agent/WI-20260711-ATS-015-evidence-pack.md` | Coverage and P0/P1 test-gap map |

**Injection Rules Applied**:

- Rule source: `deliverables/agent/WI-20260711-ATS-016-handoff.md`
- Assignee: `cr`
- Task type: security/payment adjudication
- Applied order: Tier 0 -> Tier 1 -> Tier 2 -> REQ/context/evidence packs -> current source snapshot.

## Evidence Pointers

Files changed:

- `deliverables/user/WI-20260711-ATS-016-summary.md` - user-facing release-blocker summary and remediation order.
- `deliverables/agent/WI-20260711-ATS-016-evidence-pack.md` - this adjudication, source pointers, commands, and rollback.

High-risk source anchors re-opened:

- `src/main/java/com/atstudio/atstudio/config/SecurityConfig.java:55-80,120-132`
- `src/main/java/com/atstudio/atstudio/config/WebConfig.java:17-24`
- `src/main/java/com/atstudio/atstudio/dto/track/TrackResponse.java:18,38`
- `src/main/java/com/atstudio/atstudio/service/TrackService.java:61-79,140-145,155-163`
- `src/main/java/com/atstudio/atstudio/service/DownloadService.java:40-86`
- `src/main/java/com/atstudio/atstudio/service/EmailService.java:51-65,94-108,142-180`
- `src/main/java/com/atstudio/atstudio/service/PlaylistService.java:40-54,179-188`
- `src/main/java/com/atstudio/atstudio/service/storage/LocalStorageService.java:51-70,79-87`
- `src/main/java/com/atstudio/atstudio/security/AuthRateLimitFilter.java:38-46,64-74`
- `src/main/java/com/atstudio/atstudio/service/UserService.java:36-65,104-122,148-159`
- `src/main/java/com/atstudio/atstudio/service/auth/AuthService.java:76-110`
- `src/main/java/com/atstudio/atstudio/service/CompanyCertificationService.java:233-276`
- `src/main/java/com/atstudio/atstudio/controller/CompanyCertificationController.java:100-112,130-135`
- `src/main/java/com/atstudio/atstudio/common/validation/ValidationConstants.java:46-49`
- `src/main/java/com/atstudio/atstudio/service/BillingAgreementApplicationService.java:108-158,161-245,255-281,333-357,413-424`
- `src/main/java/com/atstudio/atstudio/service/UserSubscriptionService.java:118-215,339-373`
- `src/main/java/com/atstudio/atstudio/service/RecurringRenewalService.java:48-52,84-105,124-159,167-197,231-249,276-287,346-347`
- `src/main/java/com/atstudio/atstudio/service/AdminPaymentRefundService.java:89-111,248-262`
- `src/main/java/com/atstudio/atstudio/repository/PaymentRefundRepository.java:45-57`
- `src/main/java/com/atstudio/atstudio/entity/enums/PaymentOperationAuditAction.java:17-19`
- `src/main/java/com/atstudio/atstudio/entity/enums/PaymentOperationAuditTargetType.java:8`
- `src/main/resources/schema.sql:797-815`
- `frontend/src/router/ProtectedRoute.tsx:7-24,37-38`
- `frontend/src/router/index.tsx:153-161`
- `frontend/src/pages/public/SubscriptionPlanPage.tsx:162-178`
- `frontend/src/pages/auth/SocialLoginPage.tsx:42-66`
- `frontend/src/store/authStore.ts:31-59`

## Adjudication Method

- **CONFIRMED:** current repository source directly proves the missing control or unsafe state transition.
- **CONDITIONAL:** source proves a hazardous mechanism, but maximum impact depends on deployment/runtime configuration not proven in the repository.
- **REJECTED:** current source or injected evidence contradicts the P0/P1 claim, or the item is correctly lower than P1.

## Deduplicated P0/P1 Adjudication

| Canonical ID | Status | Severity | Retained aliases / overlaps | Current source pointers | Direct facts | Preconditions | Maximum impact | Release-blocker rationale |
|---|---|---:|---|---|---|---|---|---|
| ATS016-SEC-01 | CONFIRMED | P0 | PG-004-01; ATS008-01; BE-001; CR-A-009 | `SecurityConfig.java:66-69,80,132`; `WebConfig.java:17-24`; `TrackResponse.java:18,38`; `TrackService.java:66,79,140-145`; `DownloadService.java:40-86`; evidence: `WI-004:72,91-101`; `WI-008:75-88`; `WI-015:90` | Public track responses include the original audio key and the upload root is statically served; authorized download logic is separate. | Active track ID or public detail response. | Original paid media retrieval without subscription, daily quota, download ledger, or license issuance. | Direct bypass of marketplace asset protection and financial/license traceability. |
| ATS016-SEC-02 | CONFIRMED | P0 | PG-004-03; ATS008-02; BE-006 | `EmailService.java:51-65,94-108,142-180`; evidence: `WI-004:74,117-124`; `WI-008:90-97`; `WI-015:96` | Verification/reset URLs are interpolated into HTML bodies; SMTP failure logs recipient, subject, and full body. | SMTP failure plus log-reader access. | Live account verification or password reset capability disclosure with PII. | Capability URLs and PII in logs are release-blocking secret-handling failures. |
| ATS016-PAY-01 | CONFIRMED | P0 | PAY-006-01; BE-002 | `UserService.java:104-122`; `BillingAgreementRepository.java:26-29` per WI-006; `RecurringRenewalService.java:89-159`; evidence: `WI-006:71,89,108,138`; `WI-015:91` | Withdrawal deletes selected child rows and marks user withdrawn; due renewal still scans active agreements and charges after active subscription lookup. | User withdraws while active billing agreement/subscription remains. | Future recurring charge after account withdrawal. | Unauthorized post-withdrawal charging is a payment release blocker. |
| ATS016-SEC-03 | CONFIRMED; P0 impact CONDITIONAL | P1 | PG-004-02; ATS008-03 | `PlaylistService.java:40-54,179-188`; `LocalStorageService.java:51-70`; `WebConfig.java:17-24`; `SecurityConfig.java:132`; `frontend/src/store/authStore.ts:31-59`; evidence: `WI-004:73,103-115`; `WI-008:99-108`; `WI-015:92` | Subscriber thumbnail uploads are stored without content validation under public upload paths; localStorage carries tokens. | Authenticated subscriber; accepted non-empty upload. P0 requires same-origin executable production delivery. | Stored active content; if same-origin executable, token theft and account compromise. | P1 is confirmed. P0 maximum is conditional because production ingress/origin behavior is not proven. |
| ATS016-SEC-04 | CONFIRMED | P1 | PG-004-04 | `ValidationConstants.java:46-49`; `CompanyCertificationService.java:233-276`; `CompanyCertificationController.java:100-112,130-135`; evidence: `WI-004:75,126-136`; `WI-015:98` | Certification document validation checks count, extension, and size; stored content type comes from multipart input and is returned as response content type. | BUSINESS applicant uploads a disguised document; admin reviews/downloads it. | Admin workstation/session exposure through malicious document handling. | Sensitive business-document review requires stronger content validation before release. |
| ATS016-SEC-05 | CONFIRMED | P1 | PG-004-05; ATS008-07 | `SecurityConfig.java:55,63-65`; `AuthRateLimitFilter.java:38-46,64-74`; `UserService.java:36-65`; evidence: `WI-004:76,138-145`; `WI-008:138-147` | Registration and identity checks are public; auth rate limiter covers login/forgot/reset/refresh only. | Password registration and public endpoints enabled. | User DB, token, outbound mail/log capacity abuse plus account enumeration. | Abuse controls are missing on public identity and registration surfaces. |
| ATS016-SEC-06 | CONFIRMED | P1 | PG-004-07; ATS008-04 | `AuthController.java:40-45,67-69`; `AuthService.java:76-110`; `EmailService.java:142-159`; `UserService.java:148-159`; `frontend/src/store/authStore.ts:31-59`; evidence: `WI-004:78`; `WI-008:109-116`; `WI-015:101` | Refresh is validated against one stored token hash; logout is client-side; password reset/change update password but do not clear refresh state. | Attacker already has current refresh token. | Continued session rotation after victim logout/password reset/password change. | Credential lifecycle does not invalidate stolen refresh capability. |
| ATS016-SEC-07 | CONFIRMED | P1 | PG-004-08; ATS008-06; BE-007 | `TrackService.java:61-79,155-163`; `PlaylistService.java:40-54,179-188`; `LocalStorageService.java:79-87`; `CompanyCertificationService.java:287-304`; evidence: `WI-004:79`; `WI-008:127-136`; `WI-015:97` | Files are stored before DB commit in several flows; old-file deletion and cleanup retry behavior is inconsistent. | File create/replace/delete path fails around transaction boundary or storage delete failure. | Orphan files, stale private content, or rows referencing missing files. | Cross-domain file lifecycle can corrupt user-visible and sensitive content state. |
| ATS016-PAY-02 | CONFIRMED | P1 | PAY-006-02; INT-005-01 | `AdminPaymentSettlementService.java:105,217,250`; `PaymentOperationAuditLogService.java:145`; `PaymentOperationAuditAction.java:17-19`; `PaymentOperationAuditTargetType.java:8`; `schema.sql:797-815`; evidence: `WI-006:77,90,115,147` | Java emits settlement audit actions/target; executable DDL enum omits those values. | Fresh or validated MySQL schema uses current `schema.sql`. | Settlement import/reconcile/ignore audit persistence failure. | Payment operations audit trail can fail on release-critical admin workflows. |
| ATS016-PAY-03 | CONFIRMED | P1 | PAY-006-03; BE-003 | `BusinessException.java:7`; `BillingAgreementApplicationService.java:161-182,223-226`; evidence: `WI-006:65,91,109,139`; `WI-015:93` | Billing confirm marks failure and throws runtime `BusinessException` inside default transactional method. | Provider decline/missing billing key during confirm. | Failure state can roll back, leaving failed payment attempt less auditable. | Payment attempts must remain traceable even when provider confirmation fails. |
| ATS016-PAY-04 | CONFIRMED | P1 | PAY-006-04; BE-004; INT-005-02 | `BillingAgreementApplicationService.java:161-245`; `UserSubscriptionService.java:118-215,339-373`; `RecurringRenewalService.java:135-159,346-347`; `schema.sql:518-538`; evidence: `WI-006:65,67,72,92,110,140-142` | Upgrade creates random order IDs and per-order idempotency keys; no finalization uniqueness exists on `subscription_payments.payment_order_id`. | Concurrent confirm/upgrade/renewal command execution. | Duplicate provider charges or duplicate local payment/finalization evidence. | Payment command serialization and idempotency are not release-safe. |
| ATS016-PAY-05 | CONFIRMED | P1 | PAY-006-05 | `RecurringRenewalService.java:48-52,167-197,140-144,276-287`; evidence: `WI-006:72,93,120,143` | Renewal searches READY/IN_PROGRESS/FAILED/DONE and reuses any non-DONE order without period equality. | Old failed renewal order remains after later subscription/agreement recovery. | New subscription can be expired from stale failed order or skipped without a fresh charge. | Renewal state identity is unsafe across periods/subscription lifecycles. |
| ATS016-PAY-06 | CONFIRMED | P1 | PAY-006-06; BE-004; INT-005-04 | `RecurringRenewalService.java:84-105,147-159`; evidence: `WI-006:72,94,110,144` | Scheduler/renewal batch processes all due agreements in one transaction and calls provider inside the loop. | Later item fails after earlier provider success. | Local rollback after external charge success and batch-wide interruption. | External money movement must not be batched in one rollback domain. |
| ATS016-PAY-07 | CONFIRMED | P1 | PAY-006-07; BE-005 | `AdminPaymentRefundService.java:89-111,248-262`; `PaymentRefundRepository.java:45-57`; evidence: `WI-006:75,95,111,145`; `WI-015:95` | Create refund reads aggregate reservation and inserts without locking the source `SubscriptionPayment`; only individual refund lookup is locked. | Concurrent refund requests for the same source payment. | Reserved/refunded amount can exceed original payment. | Refund ledger can authorize over-refund intent. |
| ATS016-PAY-08 | CONFIRMED | P1 | PAY-006-08; FE-002 | `ProtectedRoute.tsx:7-24,37-38`; `router/index.tsx:153-161`; `SubscriptionPlanPage.tsx:162-178`; `BillingAgreementApplicationService.java:108-137`; `TestUserBootstrapRunner.java:54-68`; evidence: `WI-006:64,96,113,146,157`; `WI-015:100` | ADMIN satisfies USER route hierarchy; checkout routes are auth-only; backend prepare checks user type, not role. | ADMIN account with matching user type reaches member checkout or calls prepare. | Admin-created billing agreement/order state in member flow. | Role boundary for payment creation is not enforced end-to-end. |
| ATS016-SEC-08 | CONFIRMED | P1 | ATS008-05; FE-001 | `SocialLoginPage.tsx:42-66`; evidence: `WI-008:118-125`; `WI-015:99` | Social callback calls `fetchMe()` without passing returned access token before committing login. | OAuth provider enabled; fresh callback begins without stored token. | Successful provider auth can fail local session/profile completion. | Core auth journey can fail; retained as P1 security/session correctness, not payment blocker by itself. |
| ATS016-SEC-09 | CONDITIONAL | P1 | PG-004-06 | `AuthRateLimitFilter.java:38-46,64-74`; `CorsConfig.java:23-28`; evidence: `WI-004:77`; `WI-008:151` | Rate-limit key includes `request.getRemoteAddr()`; no trusted forwarded-IP resolver was found in current inspection. | Reverse proxy/tunnel collapses many clients to one remote address. | Shared auth endpoint lockout/availability failure. | Conditional until production ingress topology proves the proxy-address condition. |
| ATS016-SEC-10 | CONDITIONAL | P1 | PG-004-09 | `src/main/resources/application.yml:48-52`; evidence: `WI-004:80,214`; `WI-008:155` | Current runtime uses `${JWT_SECRET}` with no committed fallback in `application.yml`; old residue impact is historical/deployment-dependent. | Former fallback ever deployed or still accepted elsewhere. | JWT integrity compromise in affected environments. | Not a current-code P1, but rotation/history remediation is needed if the condition holds. |
| ATS016-SEC-11 | CONDITIONAL | P1 | PG-004-10 | `TestUserBootstrapProperties.java:17-22`; `TestUserBootstrapRunner.java:36-68`; `src/main/resources/application.yml:87-90`; evidence: `WI-004:81`; `WI-008:152` | Shared bootstrap default exists and runner is property-gated; committed config keeps test users disabled. | `app.bootstrap.test-users.enabled=true` outside protected non-production. | Shared ADMIN fixture access and fixture PII exposure. | Conditional startup hazard; enforce profile/secret guard before release. |

## Rejected or Not Retained as P0/P1

| Claim / ID | Status | Evidence | Reason |
|---|---|---|---|
| PG-004-02 unconditional P0 token theft | CONDITIONAL only | `WI-008:150`; `PlaylistService.java:40-54`; `WebConfig.java:17-24` | Unsafe active-content upload is confirmed, but production same-origin executable delivery is not repo-proven. |
| Broad ADMIN-to-USER non-payment bypass | REJECTED | `WI-008:153`; `ProtectedRoute.tsx:7-24` | Broad non-payment bypass is not proven; retained only as payment checkout role defect ATS016-PAY-08. |
| Public original stream fallback as paid-download bypass | REJECTED | `WI-008:154`; `TrackService.java:140-145` | Stream fallback is documented behavior; static original retrieval is the actual retained bypass. |
| Current JWT fallback still accepted | REJECTED for current runtime | `src/main/resources/application.yml:48-52`; `WI-008:155` | Current config is fail-closed on missing `JWT_SECRET`; only historical/deployment exposure remains conditional. |
| Unbounded page sizes, request races, accessibility, large components as P1 | REJECTED for P0/P1 | `WI-008:157` | These remain P2 unless production exhaustion/security impact is proven. |
| PAY-006-10 API response documentation drift | Not retained as P0/P1 | `WI-006:64,98,118`; `BillingAgreementConfirmResponse.java:10-17`; `BillingAgreementResponse.java:10-18` | Material contract defect, but WI-006 classified it P2. |
| PAY-006-11 reconciliation completeness/performance | Not retained as P0/P1 | `WI-006:99,112` | WI-006 classified P2. |
| PAY-006-12 billing-agreement provider/local recovery | Not retained as P0/P1 | `WI-006:65`; `RecurringPaymentProvider.java:5-15`; `PaymentStatusLookupProvider.java:5-13` | Important recovery defect, but WI-006 classified P2. |
| PAY-006-13 payment admin frontend stale request race | Not retained as P0/P1 | `WI-006:100` | WI-006 classified P2. |
| PAY-006-14 deployment topology/crypto startup | Not retained as P0/P1 | `WI-006:102,117` | External verification/P2 in WI-006; relevant as release hardening, not counted in P0/P1. |
| WI-009 backend test pass closes security/payment risk | REJECTED | `WI-009:5,70-79`; `WI-015:90-101` | Passing 745 tests do not cover the retained P0/P1 gaps or provide coverage metrics. |

## Release Blockers

Immediate release blockers are:

1. ATS016-SEC-01 - protected original audio can be retrieved through public static upload paths.
2. ATS016-SEC-02 - live verification/reset capability URLs and PII can be logged on SMTP failure.
3. ATS016-PAY-01 - withdrawn active subscribers remain renewable/chargeable.
4. ATS016-PAY-03 through ATS016-PAY-06 - recurring billing confirm/upgrade/renewal lacks durable failure persistence, command idempotency, period identity, and transaction isolation.
5. ATS016-PAY-07 - refund reservation can overrun the original payment under concurrency.
6. ATS016-PAY-02 - payment settlement audit enum mismatch can break MySQL payment-operations audit writes.
7. ATS016-SEC-03 through ATS016-SEC-07 - upload/document/session/identity/file-lifecycle controls remain high-impact security blockers.
8. ATS016-PAY-08 - member payment creation is not explicitly protected from ADMIN role misuse.

Conditional release blockers that require environment verification:

- ATS016-SEC-09 - proxy/tunnel topology can turn rate limiting into shared auth lockout.
- ATS016-SEC-10 - any environment that ever used the historical JWT fallback needs rotation/history cleanup.
- ATS016-SEC-11 - any environment with bootstrap test users enabled outside protected non-production must be blocked.

## First-Wave Remediation Order

1. **Private paid media and regression tests**
   - Remove original audio paths from public DTOs.
   - Move originals outside static roots.
   - Deny `/uploads/tracks/audio/**` and serve originals only through `DownloadService`.

2. **Mail logging and session revocation**
   - Stop logging email bodies, token URLs, raw recipients, and subjects containing sensitive flow context.
   - Add server logout and refresh-token/session invalidation on password reset/change.

3. **Withdrawal billing stop**
   - Define billing-key retention/deletion policy.
   - Stop local renewal state during withdrawal and add a due-renewal test proving zero provider calls.

4. **Payment transaction/idempotency core**
   - Persist billing confirm failures in a committed transaction.
   - Add command-level idempotency and row claim/locks for confirm, upgrade, and renewal.
   - Add unique finalization constraints for payment order/payment evidence where valid.

5. **Renewal isolation and stale-period identity**
   - Scope renewal order reuse to billing agreement plus exact billing period.
   - Process each due agreement in its own transaction after a claim step.

6. **Refund and settlement audit integrity**
   - Lock source subscription payment or enforce atomic reservation invariant.
   - Align Java audit enums with fresh DDL and ordered existing-DB migration.

7. **Upload/document/file lifecycle**
   - Decode/re-encode image thumbnails; reject SVG/HTML/polyglots and MIME/signature mismatches.
   - Validate certification documents by server-detected type/magic/parser where feasible.
   - Add transaction-aware file mutation coordinator and durable cleanup retry.

8. **Role and abuse gates**
   - Reject ADMIN in member billing prepare/confirm at backend and route level.
   - Rate-limit/challenge registration and identity availability checks by normalized identifier and trusted client identity.

## Commands & Outputs

Read-only commands executed from repository root:

- `git status --short --branch`
  - Baseline: branch `dev/kyoung`, ahead 3, with pre-existing modified/deleted docs, untracked WI outputs, logs, and output directory.
- `Get-Content -Raw .agents/skills/create-wi-evidence-pack/SKILL.md`
  - Result: confirmed required Evidence Pack sections.
- Targeted `rg` and `Select-String` over injected evidence packs and current source files.
  - Result: current file paths corrected for moved classes such as `service/storage/LocalStorageService.java`, `security/AuthRateLimitFilter.java`, `bootstrap/TestUserBootstrapRunner.java`, and `frontend/src/router/ProtectedRoute.tsx`.
- Numbered source excerpts for high-risk anchors.
  - Result: source pointers listed above.
- Final `git status --short --branch`
  - Result: these two paths were added by this WI-016 run:
    - `deliverables/user/WI-20260711-ATS-016-summary.md`
    - `deliverables/agent/WI-20260711-ATS-016-evidence-pack.md`
  - Note: final status also showed untracked `deliverables/agent/WI-20260711-ATS-018-evidence-pack.md` and `deliverables/user/WI-20260711-ATS-018-summary.md`. They were not created, edited, staged, or otherwise touched by this run.

No secret values, raw tokens, live provider data, DB rows, HTTP responses, uploaded files, or logs containing sensitive values were read or reproduced.

## Tests

- Executed in WI-016: none.
- Reason: this handoff is read-only security/payment adjudication and forbids exploit, HTTP/provider/DB mutation, and source changes. Running Gradle/Vitest could write build/cache artifacts and was not necessary to verify static source anchors.
- Existing baseline consumed:
  - WI-009 reports `gradlew.bat test --rerun-tasks --console=plain` passed 745/745 JUnit tests with 0 failures/errors/skips.
  - WI-015 reports Java/frontend coverage is not measurable with current repository tooling and maps the retained high-risk paths to missing focused tests.

## Risks / Limitations

- Production ingress, upload origin, reverse-proxy forwarded-IP behavior, deployed JWT history, bootstrap flags, runtime schema state, MySQL migration history, scheduler replica count, and Toss runtime behavior were not available.
- Static review proves code-level paths and missing controls; it does not quantify production frequency, data volume, or concurrent race probability.
- Some aliases are inherited from injected evidence packs that referenced earlier WI-002/WI-003 source claims through WI-015; this WI retained them only where current source anchors were re-opened.
- Line numbers reflect the current dirty worktree snapshot during this run and may drift with concurrent edits.

## Rollback

If explicitly requested, remove only these two files:

- `deliverables/user/WI-20260711-ATS-016-summary.md`
- `deliverables/agent/WI-20260711-ATS-016-evidence-pack.md`

No source, test, config, schema, dependency, git staging, commit, provider, DB, or runtime state was changed.

## Follow-ups

- **Next WI:** `WI-20260711-ATS-020`
- WI-020 should consume this deduplicated blocker set as the security/payment release gate input and avoid double-counting aliases.
- Required implementation WIs before release should begin with ATS016-SEC-01, ATS016-SEC-02, ATS016-PAY-01, and ATS016-PAY-03 through ATS016-PAY-06.
