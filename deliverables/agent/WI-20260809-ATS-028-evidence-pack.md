# Evidence Pack: WI-20260809-ATS-028

## Summary (one-liner)

- Closed the bounded ADMIN source/test integration audit with 14 findings (3 P1, 11 P2), one WI-028 sublane pass, anonymous guard proof, and explicit authenticated/live/file blocks.

## Closeout Metadata

| Field             | Value                                                                            |
| ----------------- | -------------------------------------------------------------------------------- |
| WI                | `WI-20260809-ATS-028`                                                            |
| REQ               | `REQ-20260809-ATS-001`                                                           |
| Assignee          | `qa-integ`                                                                       |
| Baseline          | `codex/v1-release-rehearsal-fixes` at `e343c2085fbc82c66b44fb8e5edde35bf920980f` |
| Depends on        | `WI-020`; shared context from `WI-025` through `WI-027`                          |
| Blocks            | `WI-030`                                                                         |
| Product mutation  | None                                                                             |
| Closeout mutation | This Evidence Pack and `deliverables/user/WI-20260809-ATS-028-summary.md` only   |

## Scope / DoD Check

- [x] Audited `ADM-01` through `ADM-11`, `ADM-14`, and `SH-07` without whole-row overstatement.
- [x] Audited active ADMIN pages/wrappers and corresponding controller, DTO, service, entity/repository, audit, and assertion boundaries.
- [x] Separated UI, frontend invocation, server/test-Provider response, and durable-state evidence.
- [x] Classified support APIs and payment/withdrawal scheduler boundaries.
- [x] Inspected relevant assertions before using test results as evidence.
- [x] Ran bounded frontend/backend tests, typecheck, and targeted ESLint; recorded exact commands, counts, durations, and warnings.
- [x] Recorded all 12 anonymous ADMIN guard redirects and neutral browser restoration supplied by main.
- [x] Preserved authenticated ADMIN runtime, live Provider/DB, mutation, responsive, private-file, CSV, and binary lanes as `BLOCKED`.
- [x] Preserved all 14 independent findings and four policy/contract decisions without resolving them.
- [x] Preserved product/runtime/DB/config/secret/git state and left the intentional ZIP uninspected.
- [x] Ran Prettier write over the handoff, findings, Evidence Pack, and summary; exit `0`.
- [x] Ran Prettier check over all four WI-028 documents; exit `0`, all matched files use Prettier code style.
- [x] Ran documentation validation; exit `0`, including Tier 0, internal links, 541 traceability IDs, and document index.
- [x] Ran `git diff --check`; exit `0` with no output.

## Reference Documents (Tier 0-2)

The closeout read only the approved handoff and frozen findings. The following context was injected during the audit and is recorded from the handoff packet.

| Tier | Document                                                                                                                                                | Reason                                                              |
| ---- | ------------------------------------------------------------------------------------------------------------------------------------------------------- | ------------------------------------------------------------------- |
| 0    | `docs/standards/core-principles.md`                                                                                                                     | Project constitution and evidence boundaries                        |
| 0    | `docs/standards/development-standards.md`                                                                                                               | Cross-layer implementation and test standards                       |
| 1    | `docs/policies/security-policy.md`                                                                                                                      | Secrets, PII, Provider, and private-file boundaries                 |
| 1    | `docs/policies/access-control-policy.md`                                                                                                                | ADMIN role and denial contracts                                     |
| 1    | `docs/policies/quality-gates.md`                                                                                                                        | Quality and evidence requirements                                   |
| 1    | `docs/architecture/system-design.md`                                                                                                                    | Layer ownership and orchestration context                           |
| 2    | `docs/standards/frontend-standards.md`                                                                                                                  | Frontend state, accessibility, and request lifecycle                |
| 2    | `.agents/skills/react-best-practices/AGENTS.md`                                                                                                         | React request ownership and rendering audit guidance                |
| 2    | `docs/design/api-spec.md`; `docs/design/db-schema.md`                                                                                                   | API/persistence contracts                                           |
| 2    | `docs/ui/screen-flow.md`; `docs/ui/atstudio-front-list.md`; `docs/ui/modal-list.md`                                                                     | Route, screen, and modal inventory                                  |
| 2    | `docs/design/usecase/company-certification.md`; `docs/design/usecase/whitelist.md`; `docs/design/usecase/user-subscription.md`                          | Assigned domain workflows                                           |
| 2    | `docs/design/payment-operations-runbook.md`                                                                                                             | Payment operations, reconciliation, and withdrawal cleanup contract |
| 2    | `docs/payment/system-overview.md`; `docs/payment/feature-inventory.md`; `docs/payment/operator-guide.md`; `docs/payment/known-limits-and-next-steps.md` | Payment inventory and operational boundaries                        |

**REQ and execution context**:

- `AGENTS.md`
- `deliverables/user/REQ-20260809-ATS-001.md`
- `deliverables/agent/WI-20260809-ATS-020-acceptance-matrix.md`
- `deliverables/agent/WI-20260809-ATS-020-evidence-pack.md`
- `deliverables/agent/WI-20260809-ATS-025-findings.md` and its Evidence Pack for shared Tag/Track context
- `deliverables/agent/WI-20260809-ATS-026-findings.md` and its Evidence Pack for shared Company Certification/Whitelist context
- `deliverables/agent/WI-20260809-ATS-027-findings.md` and its Evidence Pack for shared Subscription/Payment context
- `.claude/config/workspace.json`
- `.claude/config/context-injection-rules.json`

**Injection rules applied**:

- Rule source: `.claude/config/context-injection-rules.json`
- Assignee: `qa-integ`
- Task type: bounded ADMIN source/test integration audit
- Tier order: Tier 0, Tier 1, Tier 2/task pointers

## Evidence Pointers

### Files changed for closeout

- `deliverables/agent/WI-20260809-ATS-028-evidence-pack.md` - reproducible agent-facing closeout.
- `deliverables/user/WI-20260809-ATS-028-summary.md` - user-facing disposition and approval summary.

No product, test, configuration, schema, existing documentation, runtime, DB, secret, branch, index, or ZIP file was changed.

### Frozen source of truth

- Handoff and scope: `deliverables/agent/WI-20260809-ATS-028-handoff.md`
- Full finding evidence: `deliverables/agent/WI-20260809-ATS-028-findings.md`
- Finding count: 14 total, 3 P1 and 11 P2.
- Decision-required findings: F-02, F-03, F-13, and F-14.

## Row Disposition Matrix

| Row / boundary                           | Disposition                 | Finding / proof                                                                                                                | Blocked or deferred lane                                                                                                     |
| ---------------------------------------- | --------------------------- | ------------------------------------------------------------------------------------------------------------------------------ | ---------------------------------------------------------------------------------------------------------------------------- |
| `ADM-01` `/admin/dashboard`              | FAIL                        | F-13: undefined fourth total contract                                                                                          | Authenticated/responsive runtime                                                                                             |
| `ADM-02` `/admin/users`                  | FAIL                        | F-04 stale role refresh; F-05 missing detail UI; F-09 pending modal ownership                                                  | Authenticated role mutation/durable audit                                                                                    |
| `ADM-03` `/admin/subscriptions`          | FAIL                        | F-08 omits audience and Playlist limit                                                                                         | Authenticated/responsive runtime                                                                                             |
| `ADM-04` `/admin/licenses`               | FAIL                        | F-06 lacks latest-request ownership and stable selected-User identity                                                          | Authenticated runtime/detail navigation                                                                                      |
| `ADM-05` `/admin/questions`              | FAIL                        | F-06 request ownership; F-07 invalid transition controls/tests                                                                 | Authenticated mutation/durable agreement                                                                                     |
| `ADM-06` `/admin/company-certifications` | FAIL                        | F-09 pending review/modal ownership                                                                                            | Private binary/download remains WI-029/BLOCKED                                                                               |
| `ADM-07` `/admin/tags`                   | FAIL                        | F-09 pending modal ownership; F-10 dependency copy                                                                             | Authenticated mutation/durable effect                                                                                        |
| `ADM-08` `/admin/track-manage`           | FAIL                        | F-02 deletion/retention contract; F-06 request ownership; F-09 modal ownership                                                 | Authenticated destructive mutation                                                                                           |
| `ADM-09` `/admin/user-subscriptions`     | FAIL                        | F-11 lacks typed execution confirmation                                                                                        | Persisted workflow and unknown-outcome recovery pass in source/tests; live durable state blocked                             |
| `ADM-10` `/admin/payments`               | FAIL                        | F-01 ambiguous execute recovery; F-14 reconciliation contract                                                                  | Settlement CSV/binary WI-029; Provider/live DB blocked                                                                       |
| `ADM-11` `/admin/whitelist-channels`     | PASS (WI-028 sublanes only) | Source/control/test sublanes pass: legal transitions, request fencing, immutable snapshots, formula neutralization, lock order | CSV bytes/download WI-029/BLOCKED; broader product row remains governed by `WI-026` findings and is not declared passed here |
| `ADM-14` `/admin/settings`               | FAIL                        | F-12 save ownership/canonical reload                                                                                           | Authenticated mutation/public durable reload                                                                                 |
| `SH-07` confirmations                    | FAIL                        | F-09 raw Modal pending ownership; F-11 missing typed phrase                                                                    | Authenticated interactive variants                                                                                           |
| Payment reconciliation scheduler         | PASS (source/test)          | Bounded keyset/lookback/caps, sanitized logging, persistent Incident handling, no automatic money mutation                     | Live scheduler and durable production state BLOCKED                                                                          |
| Withdrawn-user cleanup                   | FAIL                        | F-03 failed cleanup cannot reach documented retry                                                                              | Policy resolution and live Provider/durable state BLOCKED                                                                    |
| Anonymous ADMIN guards                   | PASS                        | 12 exact encoded local return redirects; neutral restoration                                                                   | None for anonymous guard lane                                                                                                |
| Authenticated ADMIN runtime              | BLOCKED                     | No approved authenticated fixture/session                                                                                      | All authenticated page/API/mutation/responsive variants                                                                      |

## Four-Lane Evidence Matrix

| Row / boundary           | UI/control lane                                                                        | Frontend invocation lane                                                                | Server/test-Provider lane                                                                                        | Durable state/audit lane                                                                  |
| ------------------------ | -------------------------------------------------------------------------------------- | --------------------------------------------------------------------------------------- | ---------------------------------------------------------------------------------------------------------------- | ----------------------------------------------------------------------------------------- |
| `ADM-01`                 | Three totals render; matrix asks for four                                              | `fetchDashboardStats` generation/retry covered                                          | DTO/service return three totals and five recent Users                                                            | Aggregate source/tests only; live totals blocked                                          |
| `ADM-02`                 | Role reason/rejections exist; detail modal absent; raw modal can close pending         | PUT refreshes identity on success, not stale-authority catch                            | ADMIN authorization, locks, self/last-admin checks, success/rejection audit covered                              | H2/mock assertions only; live role/audit blocked                                          |
| `ADM-03`                 | Read-only rows omit `userType` and `maxPlaylists`                                      | Plan list wrapper active                                                                | Response includes both omitted fields                                                                            | Read-only source/test; live projection blocked                                            |
| `ADM-04`                 | Selected-User identity can be absent/stale                                             | User/License requests have no generation fence                                          | Owner-scoped ADMIN list/detail support is active                                                                 | No mutation; live list/detail blocked                                                     |
| `ADM-05`                 | Every status is offered from every state                                               | Unfenced list writes; scalar pending ID                                                 | Entity rejects illegal transitions; broad frontend test mocks one illegal transition as success                  | Rejection/persistence test boundary only; live agreement blocked                          |
| `ADM-06`                 | List/detail privacy controls pass; review Modal can close/retarget pending             | Review completion can refresh an old detail target                                      | Locks, status validation, bounded note, audit, no storage-path DTO leak covered                                  | H2/mock proof only; private bytes and live audit blocked                                  |
| `ADM-07`                 | Duplicate handling passes; deletion copy omits Track associations; raw modals unowned  | CRUD wrappers active; late result can affect newer modal state                          | Tag delete removes `track_tags` then Tag                                                                         | Test asserts cascade; live mutation blocked                                               |
| `ADM-08`                 | Soft-delete copy conflicts with history purge; list/modal races remain                 | Unfenced list; DELETE then reload; dry-run has no SPA caller                            | Track service purges multiple histories before deactivation; dry-run is ADMIN-only/read-only                     | Purge behavior is source/test only; retention policy unresolved; live mutation blocked    |
| `ADM-09`                 | Local/Toss distinction and resumable states pass; execute uses plain confirm           | Preview/request/approve/execute and bounded unknown read recovery active                | ADMIN locks/revalidation/audit/no Toss call covered                                                              | Persisted state machine proven only in isolated tests; live rows blocked                  |
| `ADM-10`                 | Nine tabs and typed payment prompts exist; execute rejection is shown as failure       | Refund/correction execute lacks authoritative detail read; detail GETs have no wrappers | Separate claims/results/audits and idempotency covered with test doubles; reconciliation GET is observation-only | H2 ledgers only; Provider/live DB/CSV blocked; F-14 contract unresolved                   |
| `ADM-11`                 | Status controls and export-scope controls pass for WI-028                              | Latest-request and duplicate-pending controls covered                                   | Legal transitions, immutable snapshots, lock order, formula neutralization covered                               | Isolated persistence only; CSV bytes/download blocked to WI-029                           |
| `ADM-14`                 | Editable empty/present value works; field remains editable during save                 | PUT sends an older draft and does not canonical-read after success                      | Public GET and ADMIN upsert are active; no backend setting test                                                  | Visible text can diverge from persisted value; live DB/public reload blocked              |
| `SH-07`                  | Shared ConfirmDialog focus/pending passes; raw modals and local correction phrase fail | Owning pages make mutations; shared dialog itself does not                              | Server controls remain authoritative                                                                             | Live target/effect/audit blocked                                                          |
| Reconciliation scheduler | No live scheduler UI                                                                   | Scheduled owner, not Incident list or read-only GET                                     | Source/tests prove bounded comparison, Incident idempotency, sanitized logs, no money mutation                   | Test DB only; live Incident state blocked                                                 |
| Withdrawal cleanup       | No manual cleanup UI                                                                   | AFTER_COMMIT plus daily 01:15 candidate processing                                      | Source/tests prove active-user exclusion and claim/result isolation; deterministic failures become stable        | Failed key-retaining rows are excluded from retry; policy and live Provider state blocked |

## Source / API / Persistence Crosswalk

| Surface                       | Frontend owner / API                                                                    | Backend owner                                                                     | Persistence/audit boundary                                                                           | Result                                                      |
| ----------------------------- | --------------------------------------------------------------------------------------- | --------------------------------------------------------------------------------- | ---------------------------------------------------------------------------------------------------- | ----------------------------------------------------------- |
| Dashboard                     | `DashboardPage.tsx`; dashboard API in `api/admin.ts`                                    | `AdminStatsController`; `AdminStatsService`                                       | `users`, `tracks`, `user_subscriptions` aggregates                                                   | F-13                                                        |
| Users                         | `UserManagePage.tsx`; User wrappers in `api/admin.ts`                                   | `UserController`; `UserService`                                                   | `users`; `admin_operation_audit_logs`                                                                | F-04/F-05/F-09                                              |
| Plans                         | `SubscriptionManagePage.tsx`; subscription API                                          | `SubscriptionController`; Subscription service                                    | `subscriptions`                                                                                      | F-08                                                        |
| Licenses                      | `LicenseManagePage.tsx`; User/License APIs                                              | `LicenseController`; License service                                              | `licenses`, related User/Track/Subscription                                                          | F-06                                                        |
| Questions                     | `QuestionManagePage.tsx`; `api/questions.ts`                                            | `QuestionController`; `QuestionService`; `Question` state machine                 | `questions`, `answers`, attachment metadata                                                          | F-06/F-07                                                   |
| Company Certification         | `CompanyCertManagePage.tsx`; admin certification API                                    | Company Certification controller/service                                          | certification, document metadata, certification audit                                                | F-09; bytes WI-029                                          |
| Tags                          | `TagManagePage.tsx`; `api/tags.ts`                                                      | `TagController`; `TagService`                                                     | `tags`, `track_tags`                                                                                 | F-09/F-10                                                   |
| Tracks                        | `TrackManagePage.tsx`; `api/tracks.ts`                                                  | `TrackController`; `TrackService`; `AdminTrackAudioAnalysisController/Service`    | `tracks` plus Like/Download/License/Playlist/Album/Tag relations                                     | F-02/F-06/F-09                                              |
| Local subscription correction | `UserSubscriptionManagePage.tsx`; `UserSubscriptionCorrectionModal.tsx`; correction API | `AdminUserSubscriptionCorrectionController`; `AdminSubscriptionCorrectionService` | `admin_subscription_corrections`, Subscription/Billing Agreement, admin audit                        | F-11                                                        |
| Payment operations            | `PaymentOperationsPage.tsx`; payment wrappers in `api/admin.ts`                         | `AdminPaymentController`; read/incident/refund/settlement/correction services     | payment orders, agreements, payments, incidents, receipts, audits, settlements, refunds, corrections | F-01/F-14; binary WI-029                                    |
| Whitelist                     | `WhitelistChannelManagePage.tsx`; whitelist API                                         | `AdminWhitelistChannelController`; `AdminWhitelistChannelService`                 | channels, immutable export batches/items                                                             | WI-028 sublanes PASS; WI-026 broader findings; bytes WI-029 |
| Settings                      | `SiteSettingsPage.tsx`; `api/settings.ts`; public certification consumer                | `SettingController`; `AdminSettingController`; `SiteSettingService`               | `site_settings`                                                                                      | F-12                                                        |

## Support API Classification

| API / wrapper                                                         | Classification                                                       | Evidence and consequence                                                                                             |
| --------------------------------------------------------------------- | -------------------------------------------------------------------- | -------------------------------------------------------------------------------------------------------------------- |
| `GET /api/admin/payments/reconciliation`                              | Ambiguous contract; active ADMIN-only observation API                | No SPA control. Runbook/code/test say no Incident write; matrix requires durable Incident. F-14 requires a decision. |
| `GET /api/admin/tracks/audio-analysis/dry-run`                        | Intentional API-only support; PASS in source/tests                   | ADMIN-only, bounded, deterministic, report-only, and no-save assertions. It was not invoked live.                    |
| `GET /api/admin/payments/refunds/{refundId}`                          | Active support API but missing recovery product call site            | Needed for F-01 authoritative unknown-outcome recovery; no frontend wrapper/caller.                                  |
| `GET /api/admin/payments/entitlement-corrections/{correctionId}`      | Active support API but missing recovery product call site            | Needed for F-01 authoritative unknown-outcome recovery; no frontend wrapper/caller.                                  |
| `GET /api/users/{userId}/licenses/{licenseId}`                        | Intentional API-only ADMIN detail support                            | Owner-scoped read support; current ADMIN product surface is list-by-User. Absence alone is not a defect.             |
| `GET /api/users/{userId}`                                             | Missing contracted product surface                                   | Minimized ADMIN detail API is active, but `ADM-02` explicitly requires a detail modal. F-05.                         |
| Subscription-correction history / `fetchAdminSubscriptionCorrections` | Backend API-only operational history; frontend wrapper is unconsumed | No non-test importer; not evidence of a visible history surface.                                                     |

## Scheduler Classification

| Scheduler                      | Source/test result                                                                                                                           | Live/durable result                                                                          | Disposition                            |
| ------------------------------ | -------------------------------------------------------------------------------------------------------------------------------------------- | -------------------------------------------------------------------------------------------- | -------------------------------------- |
| Payment reconciliation         | Daily zone-bound owner; bounded keyset/lookback/caps; persistent Incident idempotency; aggregate sanitized logs; no automatic money mutation | Not invoked live; no production Provider/Incident rows inspected                             | PASS source/test; BLOCKED live/durable |
| Withdrawn-user billing cleanup | AFTER_COMMIT plus daily 01:15 owner; bounded candidates; active Users excluded; claim/result transactions and safe observability covered     | No live Provider/key/Incident state inspected; deterministic `FAILED` is excluded from retry | FAIL F-03; policy/live state BLOCKED   |

## Commands & Outputs

This final documentation patch did not rerun tests or browser checks. The test commands below are frozen evidence from the findings; only the final documentation quality checks recorded after them were run for closeout.

### Frontend targeted tests

```powershell
npm test -- src/router/ProtectedRoute.test.tsx src/router/index.test.tsx src/components/ui/Modal.test.tsx src/components/ui/ConfirmDialog.test.tsx src/api/adminContracts.test.ts src/api/adminWhitelistChannels.test.ts src/pages/admin/DashboardPage.test.tsx src/pages/admin/UserManagePage.test.tsx src/pages/admin/TagManagePage.test.tsx src/pages/admin/CompanyCertManagePage.test.tsx src/pages/admin/PaymentOperationsPage.test.tsx src/pages/admin/UserSubscriptionManagePage.test.tsx src/pages/admin/WhitelistChannelManagePage.test.ts src/pages/admin/WhitelistChannelManagePage.render.test.tsx src/pages/subscriber/CompanyCertApplyPage.test.tsx src/test/coverage/adminSubscriberPages.coverage.test.tsx src/test/coverage/adminSubscriberGaps.coverage.test.tsx src/test/coverage/shellCatalogRouterGaps.coverage.test.tsx
```

- PASS, exit `0`.
- 18 files; 181 tests; 0 failures/skips reported.
- Vitest `10.75s`; wrapper `11.887s`; no warning reported.
- Passing assertions preserve known defects in F-04, F-07, F-08, and F-11; count is not whole-row proof.

### Typecheck

```powershell
npm run typecheck
```

- PASS, exit `0`; `tsc --noEmit` emitted no diagnostics.
- Wrapper `6.374s`; no warning reported.

### Targeted ESLint

```powershell
npx eslint --max-warnings 0 src/router/index.tsx src/router/ProtectedRoute.tsx src/layouts/AdminLayout.tsx src/components/ui/Modal.tsx src/components/ui/ConfirmDialog.tsx src/api/admin.ts src/api/settings.ts src/api/questions.ts src/api/licenses.ts src/api/tags.ts src/api/tracks.ts src/api/subscriptions.ts src/api/userSubscriptions.ts "src/pages/admin/**/*.{ts,tsx}" src/pages/subscriber/CompanyCertApplyPage.tsx
```

- PASS, exit `0`; 0 warnings.
- Wrapper `3.236s`.

### Backend targeted tests

```powershell
.\gradlew.bat test --rerun-tasks --tests "com.atstudio.atstudio.controller.AdminPaymentControllerTest" --tests "com.atstudio.atstudio.controller.AdminTrackAudioAnalysisControllerTest" --tests "com.atstudio.atstudio.controller.AdminUserSubscriptionCorrectionControllerTest" --tests "com.atstudio.atstudio.controller.AdminWhitelistChannelControllerTest" --tests "com.atstudio.atstudio.controller.UserControllerTest" --tests "com.atstudio.atstudio.controller.SubscriptionControllerTest" --tests "com.atstudio.atstudio.controller.LicenseControllerTest" --tests "com.atstudio.atstudio.controller.QuestionControllerTest" --tests "com.atstudio.atstudio.controller.CompanyCertificationControllerTest" --tests "com.atstudio.atstudio.controller.TagControllerTest" --tests "com.atstudio.atstudio.controller.TrackControllerTest" --tests "com.atstudio.atstudio.service.AdminPaymentReadServiceTest" --tests "com.atstudio.atstudio.service.AdminPaymentIncidentServiceTest" --tests "com.atstudio.atstudio.service.AdminPaymentRefundServiceTest" --tests "com.atstudio.atstudio.service.AdminPaymentEntitlementCorrectionServiceTest" --tests "com.atstudio.atstudio.service.AdminPaymentSettlementServiceTest" --tests "com.atstudio.atstudio.service.AdminSubscriptionCorrectionServiceTest" --tests "com.atstudio.atstudio.service.AdminWhitelistChannelServiceTest" --tests "com.atstudio.atstudio.service.AdminTrackAudioAnalysisServiceTest" --tests "com.atstudio.atstudio.service.AdminOperationAuditTransactionIntegrationTest" --tests "com.atstudio.atstudio.service.PaymentReconciliationServiceTest" --tests "com.atstudio.atstudio.service.PaymentReconciliationServiceEdgeCaseTest" --tests "com.atstudio.atstudio.service.PaymentReconciliationTransactionServiceTest" --tests "com.atstudio.atstudio.service.PaymentReconciliationRecoveryIntegrationTest" --tests "com.atstudio.atstudio.service.PaymentReconciliationIncidentServiceTest" --tests "com.atstudio.atstudio.service.PaymentRefundResilienceIntegrationTest" --tests "com.atstudio.atstudio.service.PaymentReceiptEvidenceServiceTest" --tests "com.atstudio.atstudio.service.PaymentOperationAuditLogServiceTest" --tests "com.atstudio.atstudio.service.WithdrawalBillingCleanupServiceTest" --tests "com.atstudio.atstudio.service.WithdrawalBillingCleanupCoordinatorTest" --tests "com.atstudio.atstudio.service.WithdrawalBillingCleanupTransactionIntegrationTest" --tests "com.atstudio.atstudio.service.BillingAgreementCleanupProviderExecutorTest" --tests "com.atstudio.atstudio.service.BillingAgreementFailurePersistenceIntegrationTest" --tests "com.atstudio.atstudio.service.BillingAgreementCancellationTransactionIntegrationTest" --tests "com.atstudio.atstudio.repository.BillingAgreementRepositoryTest" --tests "com.atstudio.atstudio.service.CompanyCertificationServiceTest" --tests "com.atstudio.atstudio.service.CompanyCertificationSecurityVerificationTest" --tests "com.atstudio.atstudio.service.UserServiceTest" --tests "com.atstudio.atstudio.service.SubscriptionServiceTest" --tests "com.atstudio.atstudio.service.LicenseServiceTest" --tests "com.atstudio.atstudio.service.QuestionServiceTest" --tests "com.atstudio.atstudio.service.TagServiceTest" --tests "com.atstudio.atstudio.service.TrackServiceTest" --tests "com.atstudio.atstudio.service.WhitelistChannelServiceTest"
```

- PASS, exit `0`; `BUILD SUCCESSFUL in 1m 17s`; wrapper `77.361s`.
- 44 explicit class filters; 5 actionable tasks executed with `--rerun-tasks`.
- Read-only regex aggregate: 63 `TEST-*.xml` suites; 554 tests; 0 failures; 0 errors; 0 skipped.
- Regexing `testsuite` attributes was used because encoding-sensitive/malformed suite-name text invalidated PowerShell XML parsing; noisy failed parser output is intentionally omitted.
- Boundary: isolated H2/test contexts and test doubles only, never live DB/Provider proof.
- Warnings: unchecked/unsafe Java operations; `-Xlint:unchecked` suggestion; JVM CDS boot-loader-sharing warning; Gradle configuration-cache suggestion and incubating problems-report location.

## Final Documentation Quality Checks

### Prettier write

- Scope: `WI-20260809-ATS-028-handoff.md`, `WI-20260809-ATS-028-findings.md`, `WI-20260809-ATS-028-evidence-pack.md`, and `WI-20260809-ATS-028-summary.md`.
- Result: PASS, exit `0`.
- Per-file output: handoff unchanged in `56ms`; findings unchanged in `62ms`; Evidence Pack processed in `48ms`; summary processed in `13ms`.

### Prettier check

- Scope: all four WI-028 documents above.
- Result: PASS, exit `0`.
- Output: `All matched files use Prettier code style.`

### Documentation validation

```powershell
python .agents/skills/validate-docs/scripts/validate_docs.py
```

- Result: PASS, exit `0`.
- Output: Tier 0 validation, internal links, 541 traceability IDs, document index, and all validations passed.

### Diff whitespace check

```powershell
git diff --check
```

- Result: PASS, exit `0`; no output.

## Anonymous Guard Evidence

All checks passed and preserved the complete internal path/query in an encoded `returnTo`.

| Input                                      | Observed redirect                                                  |
| ------------------------------------------ | ------------------------------------------------------------------ |
| `/admin/dashboard?from=audit`              | `/login?returnTo=%2Fadmin%2Fdashboard%3Ffrom%3Daudit`              |
| `/admin/users?from=audit`                  | `/login?returnTo=%2Fadmin%2Fusers%3Ffrom%3Daudit`                  |
| `/admin/subscriptions?from=audit`          | `/login?returnTo=%2Fadmin%2Fsubscriptions%3Ffrom%3Daudit`          |
| `/admin/licenses?from=audit`               | `/login?returnTo=%2Fadmin%2Flicenses%3Ffrom%3Daudit`               |
| `/admin/questions?from=audit`              | `/login?returnTo=%2Fadmin%2Fquestions%3Ffrom%3Daudit`              |
| `/admin/company-certifications?from=audit` | `/login?returnTo=%2Fadmin%2Fcompany-certifications%3Ffrom%3Daudit` |
| `/admin/tags?from=audit`                   | `/login?returnTo=%2Fadmin%2Ftags%3Ffrom%3Daudit`                   |
| `/admin/track-manage?from=audit`           | `/login?returnTo=%2Fadmin%2Ftrack-manage%3Ffrom%3Daudit`           |
| `/admin/user-subscriptions?from=audit`     | `/login?returnTo=%2Fadmin%2Fuser-subscriptions%3Ffrom%3Daudit`     |
| `/admin/payments?from=audit`               | `/login?returnTo=%2Fadmin%2Fpayments%3Ffrom%3Daudit`               |
| `/admin/whitelist-channels?from=audit`     | `/login?returnTo=%2Fadmin%2Fwhitelist-channels%3Ffrom%3Daudit`     |
| `/admin/settings?from=audit`               | `/login?returnTo=%2Fadmin%2Fsettings%3Ffrom%3Daudit`               |

- Restoration URL: `http://127.0.0.1:5173/`.
- Neutral state: 0 dialogs, 0 file inputs, active element `BODY`, no horizontal overflow.
- No authenticated/ADMIN API call or mutation occurred.

## Findings Index

| ID   | Severity | Affected contract                      | Closeout                                      |
| ---- | -------- | -------------------------------------- | --------------------------------------------- |
| F-01 | P1       | Payment execute response-loss recovery | Open defect; WI-030 input after prerequisites |
| F-02 | P1       | Track soft-delete retention            | Policy/contract decision required             |
| F-03 | P1       | Withdrawal cleanup retryability        | Policy decision required                      |
| F-04 | P2       | Stale ADMIN session refresh            | Open defect                                   |
| F-05 | P2       | ADMIN User detail surface              | Open defect                                   |
| F-06 | P2       | Latest-request ownership               | Open defect                                   |
| F-07 | P2       | Question transition contract           | Open defect                                   |
| F-08 | P2       | Subscription policy projection         | Open defect                                   |
| F-09 | P2       | Pending raw Modal ownership            | Open defect                                   |
| F-10 | P2       | Tag dependency confirmation            | Open defect                                   |
| F-11 | P2       | Typed local-correction confirmation    | Open defect                                   |
| F-12 | P2       | Settings save/canonical reload         | Open defect                                   |
| F-13 | P2       | Dashboard fourth total                 | Contract decision required                    |
| F-14 | P2       | Reconciliation GET Incident semantics  | Contract decision required                    |

Detailed causes, exact source/test pointers, impacts, and bounded follow-ups remain frozen in `deliverables/agent/WI-20260809-ATS-028-findings.md`.

## Screenshots / Visual Evidence

- WI-028 screenshot count: 0.
- No screenshot is referenced or claimed by the findings.
- No responsive live-width evidence was collected beyond the current neutral browser state.
- `1440x900`, `1024x768`, `390x844`, and `360x800` authenticated/responsive variants are `BLOCKED/NOT RUN`.

## Limitations

- Authenticated ADMIN runtime and USER denial runtime were not exercised.
- No live ADMIN mutation or canonical reload was observed.
- No live/test Provider call, charge, refund, cancellation, or reconciliation lookup was executed.
- No live/direct DB row, audit row, scheduler result, storage state, or secret was inspected.
- No private Company Certification document, settlement file, CSV bytes, import/export/download, or upload was accessed.
- Automated persistence evidence is H2/test-double evidence only.
- No screenshots or multi-width responsive proof exist for WI-028.
- Passing automated tests do not override findings where assertions encode or omit the defect.
- The intentional `output/client-demo-screenshots-20260716-140514.zip` remained preserved and uninspected.
- Tests and browser checks were not rerun by the final documentation patch. Only the recorded Prettier write/check, documentation validation, and `git diff --check` quality checks ran.

## Risks / Approval Points

- F-02: approve Track retention/deletion semantics before implementation.
- F-03: approve deterministic versus unknown withdrawal-cleanup retry/disposition policy.
- F-13: define the dashboard fourth aggregate or correct the executable matrix to three.
- F-14: choose read-only reconciliation GET versus a separately approved audited mutation contract.
- Do not infer production readiness from anonymous guards or isolated tests.

## Rollback

- No product rollback is required because this WI changed no product/runtime/DB/configuration state.
- Documentation rollback, if explicitly requested, is limited to removing the two WI-028 closeout files created in this step.
- No file was staged, committed, pushed, or otherwise changed through git.

## Follow-ups

1. `WI-20260809-ATS-029`: complete private binary, CSV, import/export/download, and file-content evidence without treating WI-028 control-source evidence as byte proof.
2. Resolve F-02, F-03, F-13, and F-14 through explicit policy/contract approval; retain the decisions as WI-030 inputs.
3. `WI-20260809-ATS-030`: integrate WI-021 through WI-029, rerun authenticated/responsive/runtime regression with approved fixtures, and verify fixes for the remaining WI-028 findings across all four lanes.
