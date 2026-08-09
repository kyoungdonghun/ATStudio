# Evidence Pack: WI-20260808-ATS-015

## Summary (one-liner)

- Completed the standalone, local-only administrator subscription-correction workflow across backend, frontend, audit, fresh-schema baseline, current-development-DB application, and disposable-MySQL concurrency proof; WI-016 is unblocked.

## Scope / DoD Check

- DoD items:
  - [x] Kept general administrator subscription correction in `admin_subscription_corrections` and left refund-bound `payment_entitlement_corrections` unchanged.
  - [x] Retired direct ADMIN `PUT`/`DELETE /api/user-subscriptions/{id}` backend mappings and frontend call paths; active direct-ID mutation references are `0` while self-service paths remain.
  - [x] Implemented preview, list, detail, open-by-user-subscription, request, approve, and execute APIs.
  - [x] Implemented the explicit single-operator `preview -> request -> approve -> execute` workflow without two-person approval semantics.
  - [x] Resumed `REQUESTED`, `APPROVED`, and `PROCESSING` corrections after close/reopen or refresh, with stale open-lookup and preview results fenced.
  - [x] Persisted required `reason_note` and optional `approval_note`/`execution_note` values.
  - [x] Kept Toss payment, refund, provider billing-key deletion, and email calls at `0`.
  - [x] Coupled success audit to the outer mutation transaction and isolated rejection audit with failure-safe `REQUIRES_NEW` behavior.
  - [x] Recorded minimal before/after local subscription and billing-agreement state without PII or secrets.
  - [x] Unified request/execute lock order and added duplicate non-terminal current-read protection.
  - [x] Applied the additive table to the current development DB without changing the existing 40-table row-count digest.
  - [x] Passed the live MySQL duplicate-request and execute-versus-request concurrency proofs, then removed the disposable DB and verified absence.
  - [x] Passed final targeted backend and frontend verification within the WI-015 boundary.
  - [x] Documented downstream verification ownership without misrepresenting the earlier full backend suite as a post-refinement run.
  - [x] Marked WI-015 complete and WI-016 unblocked.

## Reference Documents (Tier 0-2)

**Injected Context from the WI Handoff Packet:**

| Tier | Document | Reason |
|---|---|---|
| 0 | `docs/standards/core-principles.md` | Constitution, language, traceability, and domain integrity |
| 0 | `docs/standards/development-standards.md` | Java, transaction, JPA, lock, and test standards |
| 2 | `docs/design/usecase/user-subscription.md` | Subscription lifecycle and prior administrator behavior |
| 2 | `docs/design/payment-operations-runbook.md` | Payment boundary, provider-outcome fence, and lock-order context |
| 2 | `docs/SR/SR-97.md` | Client-requested administrator correction behavior |
| 2 | `deliverables/user/REQ-20260808-ATS-004.md` | Approved scope, work sequence, and downstream quality gates |

**DocOps process references:**

| Tier | Document | Reason |
|---|---|---|
| 0 | `docs/standards/documentation-standards.md` | Evidence structure and English documentation style |
| 0 | `docs/standards/glossary.md` | Canonical terminology |

**Injection Rules Applied:**

- Handoff: `deliverables/agent/WI-20260808-ATS-015-handoff.md`.
- Implementation assignee recorded by the handoff: `se`.
- Final artifact role requested for this pass: `docops`.
- Evidence is pointer-based; supplied execution results are separated from commands run by DocOps.

## Evidence Pointers (required)

### Files changed by this DocOps pass

- `deliverables/user/WI-20260808-ATS-015-summary.md` - replaced stale partial-slice claims with the final completion, DB, test-boundary, rollback, and WI-chain status.
- `deliverables/agent/WI-20260808-ATS-015-evidence-pack.md` - consolidated final backend, frontend, current-DB, disposable-DB, and follow-up evidence.

No code, schema, other documentation, Git state, `application-local.yml`, database state, or output ZIP was changed by DocOps.

### Workflow, API, and retired direct paths

- `src/main/java/com/atstudio/atstudio/controller/AdminUserSubscriptionCorrectionController.java:24-90` - ADMIN authorization and all seven workflow endpoints, including open lookup `200/204` behavior.
- `src/main/java/com/atstudio/atstudio/service/AdminSubscriptionCorrectionService.java:167-297` - open lookup and single-operator request, approve, and execute transitions.
- `src/main/java/com/atstudio/atstudio/controller/UserSubscriptionController.java:22-76` - self-service `/me` paths remain; direct ADMIN ID mutation mappings are absent.
- `frontend/src/api/userSubscriptions.ts:141-282` - retained self-service API calls and the correction preview/list/detail/open/request/approve/execute client contracts.
- `frontend/src/pages/admin/UserSubscriptionManagePage.tsx:10-198` - administrator list launches the correction workflow instead of direct mutation calls.
- `frontend/src/pages/admin/UserSubscriptionCorrectionModal.tsx:174-180` - open statuses are `REQUESTED`, `APPROVED`, and `PROCESSING`.
- `frontend/src/pages/admin/UserSubscriptionCorrectionModal.tsx:250-340` - open lookup, request-generation fence, abort handling, and stale-result rejection.
- `frontend/src/pages/admin/UserSubscriptionCorrectionModal.tsx:408-490` - explicit request, approve, and execute actions.
- `src/main/java/com/atstudio/atstudio/dto/subscription/AdminSubscriptionCorrectionRequest.java:9-20` - required, maximum-500-character `reasonNote`.
- `src/main/java/com/atstudio/atstudio/dto/subscription/AdminSubscriptionCorrectionApproveRequest.java:5-7` and `AdminSubscriptionCorrectionExecuteRequest.java:5-7` - optional, maximum-500-character workflow notes.

Active-path source check:

- `rg -n "PutMapping|DeleteMapping|client\\.put|client\\.delete" src/main/java/com/atstudio/atstudio/controller/UserSubscriptionController.java frontend/src/api/userSubscriptions.ts`
- Result: only `PUT /me` and `DELETE /me` mappings/calls remain; direct ADMIN ID mutation mappings/calls: `0`.

### Schema, persistence, audit, and lock order

- `src/main/resources/schema.sql:667-711` - existing refund-bound `payment_entitlement_corrections` contract.
- `src/main/resources/schema.sql:1053-1113` - standalone `admin_subscription_corrections` fresh-baseline DDL.
- `src/main/java/com/atstudio/atstudio/entity/AdminSubscriptionCorrection.java:123-166` - persisted reason, approval, and execution notes plus workflow transitions.
- `src/main/java/com/atstudio/atstudio/repository/AdminSubscriptionCorrectionRepository.java:21-31` - non-locking execute ID projection.
- `src/main/java/com/atstudio/atstudio/repository/AdminSubscriptionCorrectionRepository.java:59-97` - non-terminal open lookup, final correction lock, and duplicate non-terminal current-read lock.
- `src/main/java/com/atstudio/atstudio/service/AdminSubscriptionCorrectionService.java:179-221` - request lock order: `BillingAgreement -> UserSubscription -> target Subscription -> non-terminal correction current-read`.
- `src/main/java/com/atstudio/atstudio/service/AdminSubscriptionCorrectionService.java:240-297` - execute ID observation, the same ordered domain locks, final correction lock, locked-state revalidation, local mutation, and success audit.
- `src/main/java/com/atstudio/atstudio/service/AdminOperationAuditService.java:60-91` - success audit with `Propagation.MANDATORY` in the outer transaction.
- `src/main/java/com/atstudio/atstudio/service/AdminOperationRejectionAuditService.java:62-79` - rejection audit with `Propagation.REQUIRES_NEW`.
- `src/main/java/com/atstudio/atstudio/service/AdminSubscriptionCorrectionService.java:517-568` - rejection-audit failure is suppressed and cannot mask the original `BusinessException`.
- `src/main/java/com/atstudio/atstudio/service/AdminOperationAuditState.java:31-53` - minimal subscription, pending-change, and local billing-agreement status JSON; no PII, provider identifier, token, or billing-key field.
- `src/test/java/com/atstudio/atstudio/config/V1BackendBaselineContractTest.java:51-166` - 41-table fresh baseline, separate correction contracts, and no provider/refund/HTTP/payment-audit dependency.

### Resume, stale-response, and concurrency tests

- `src/test/java/com/atstudio/atstudio/controller/AdminUserSubscriptionCorrectionControllerTest.java:151-202` - open lookup `200`, `204`, stable not-found, and ADMIN authorization.
- `src/test/java/com/atstudio/atstudio/repository/AdminSubscriptionCorrectionRepositoryContractTest.java:17-52` - deterministic, graph-complete, non-locking open lookup.
- `src/test/java/com/atstudio/atstudio/service/AdminSubscriptionCorrectionServiceTest.java:217-269` - all non-terminal open statuses, terminal empty result, and unknown-subscription handling.
- `src/test/java/com/atstudio/atstudio/service/AdminSubscriptionCorrectionServiceTest.java:271-321` - request lock order and duplicate non-terminal rejection.
- `src/test/java/com/atstudio/atstudio/service/AdminSubscriptionCorrectionServiceTest.java:365-630` - execute lock order, idempotency, snapshot/ID revalidation, provider-outcome fence, and rejection audit behavior.
- `src/test/java/com/atstudio/atstudio/service/AdminSubscriptionCorrectionMysqlConcurrencyIntegrationTest.java:68-210` - guarded MySQL 8/InnoDB `REPEATABLE READ` duplicate-request and execute-versus-request races.
- `frontend/src/pages/admin/UserSubscriptionManagePage.test.tsx:283-472` - lookup failure fence, retry, `REQUESTED`/`APPROVED`/`PROCESSING` resume, and stale cross-row lookup rejection.
- `frontend/src/pages/admin/UserSubscriptionManagePage.test.tsx:689-719` - stale preview cannot overwrite the current target.
- `frontend/src/api/domainApis.test.ts:258-354` - correction API contracts and open lookup `200/204` mapping.
- `frontend/src/test/coverage/adminSubscriberGaps.coverage.test.tsx:591-689` - complete local correction UI flow and rejection branches.

## External Payment Zero Proof

- General administrator correction is independent from `payment_entitlement_corrections`, which remains bound to refund evidence.
- The correction service has no Toss payment, refund execution, provider billing-key deletion, email, provider HTTP, or payment-operation-audit execution path.
- Its payment-ledger interaction is a read-only unresolved-provider-outcome fence; local agreement cancellation invokes only local entity state change.
- Recorded external calls during the workflow: Toss payment `0`, refund `0`, provider billing-key deletion `0`, email `0`.
- Preview reports `externalPaymentExecuted=false`.

## Database Application Evidence

### Current development DB

| Check | Result |
|---|---|
| MySQL version | `8.0.45` |
| Table count | `40 -> 41` |
| New table | `admin_subscription_corrections` |
| New-table columns | `32` |
| Physical indexes | `11`, including InnoDB foreign-key indexes |
| Foreign keys | `9` |
| Correction rows at verification | `0` |
| Existing-data digest before | `4995fa10e08421b24ffa302822b8611b37f1516fdfbf68e1f3095ed7cb4811bd` |
| Existing-data digest after | `4995fa10e08421b24ffa302822b8611b37f1516fdfbf68e1f3095ed7cb4811bd` |
| Digest comparison | `unchanged=true` |

The application was additive. Existing 40-table row counts did not change. `src/main/resources/schema.sql` is the sole V1 fresh baseline; `rg --files -g '*.sql'` returns only `schema.sql` and `seed.sql`, so WI-015 has no manual migration file.

### Disposable MySQL proof DB

| Check | Result |
|---|---|
| Database | `ats_disposable_20260808_f1eb1b33` |
| Fresh load | `schema.sql` plus `seed.sql` |
| Tables | `41` |
| Seed plans | `6` |
| Engine | InnoDB |
| Correction rows before proof | `0` |
| Concurrency test | `2` tests, `2` passed, `0` skipped/failures/errors, `17.857s` |
| Covered races | Duplicate request; approved execute versus new request |
| Cleanup | Database removed; absence verified |

Connection details and secrets are intentionally excluded from this Evidence Pack.

## Commands & Outputs

The implementation and verification results below were supplied as final WI-015 evidence and reconciled against current source/test pointers. DocOps did not rerun product tests or access either database.

- Earlier full backend suite:
  - `.\gradlew.bat test --rerun-tasks`
  - Result: `1,272` discovered, `1,260` passed, `12` gated skipped, `0` failures, `0` errors.
  - Boundary: this run predates the final lock/open refinements and is not the WI-023 final full-suite rerun.
- Post-refinement targeted lock cohort:
  - `.\gradlew.bat test --rerun-tasks --tests "com.atstudio.atstudio.service.AdminSubscriptionCorrectionServiceTest" --tests "com.atstudio.atstudio.service.AdminSubscriptionCorrectionMysqlConcurrencyIntegrationTest" --tests "com.atstudio.atstudio.service.AdminOperationAuditTransactionIntegrationTest"`
  - Result: `37` discovered, `35` passed, `2` MySQL-gated skipped, `0` failures/errors.
- Open-endpoint targeted cohort:
  - Reproduction cohort: `AdminUserSubscriptionCorrectionControllerTest`, `AdminSubscriptionCorrectionRepositoryContractTest`, and `AdminSubscriptionCorrectionServiceTest`.
  - Result: `42/42` passed. The current local XML report set under `build/test-results/test/` contains those three classes and the same totals.
- Live MySQL gated cohort:
  - Test class: `AdminSubscriptionCorrectionMysqlConcurrencyIntegrationTest`, with its explicit proof gate enabled in the disposable environment.
  - Result: `2/2` passed in `17.857s`; the disposable DB was then removed and absence-checked.
- Frontend targeted cohort from `frontend/`:
  - Reproduction command: `npm test -- src/pages/admin/UserSubscriptionManagePage.test.tsx src/api/domainApis.test.ts src/test/coverage/adminSubscriberGaps.coverage.test.tsx`
  - Result: `3` files, `52/52` passed: page/modal `14`, domain API `14`, coverage cohort `24`.
- Frontend static checks from `frontend/`:
  - `npm run typecheck` -> passed.
  - `npm run lint` -> passed.
  - `npm run format` -> passed.
  - `npm run build` -> not run in WI-015; assigned to WI-027.
- SQL baseline inventory:
  - `rg --files -g '*.sql'`
  - Result: `src/main/resources/schema.sql` and `src/main/resources/seed.sql` only; no manual migration file.
- Whole-worktree implementation check:
  - `git diff --check`
  - Result: passed; CRLF conversion warnings only.
- Final DocOps scoped check:
  - `git diff --check -- deliverables/user/WI-20260808-ATS-015-summary.md deliverables/agent/WI-20260808-ATS-015-evidence-pack.md`
  - Result: passed after the final documentation update; the two untracked files were also checked directly for diff whitespace errors.

## Tests

| Suite | Tests | Passed | Skipped | Failures / Errors | Completion meaning |
|---|---:|---:|---:|---:|---|
| Earlier backend full suite | 1,272 | 1,260 | 12 | 0 / 0 | Pre-final-refinement baseline only |
| Post-refinement lock cohort | 37 | 35 | 2 | 0 / 0 | Lock order, audit transaction, gated-test compile path |
| Open-endpoint backend cohort | 42 | 42 | 0 | 0 / 0 | Final targeted open/resume backend behavior |
| Live MySQL concurrency cohort | 2 | 2 | 0 | 0 / 0 | Duplicate request and execute/request race on MySQL 8/InnoDB |
| Frontend final targeted cohort | 52 | 52 | 0 | 0 / 0 | Correction page/modal, domain API, and coverage-path behavior |

The final post-refinement full backend suite and coverage belong to WI-023. Frontend full tests/coverage belong to WI-024. The frontend build belongs to WI-027.

## Risks / Rollback

### Risks and completion boundary

- WI-015 targeted evidence is complete, but REQ-level broad regression, coverage, build, security, and cross-layer gates remain with WI-023 through WI-030 as listed below.
- The earlier `1,272`-test backend run must not be reused as evidence that the final source state passed the full suite.
- `FAILED`, `CANCELLED`, and failure fields remain reserved workflow vocabulary; V1 rejection keeps the current non-terminal state and writes rejection audit evidence.
- The current DB application was additive and preserved the exact existing-data digest, but any future correction/audit rows become retention evidence and affect DB rollback decisions.

### Code rollback

- Revert the correction controller, DTO/entity/repository/service, frontend workflow, tests, and `schema.sql` source changes as one compatible code rollback.
- Do not restore direct ADMIN `PUT`/`DELETE /api/user-subscriptions/{id}` behavior without a separate approved product/security decision.
- An additive current-DB table may remain unused after code rollback; code rollback does not require destructive DDL.

### Database rollback

- Database rollback is distinct from code rollback.
- Dropping `admin_subscription_corrections` is destructive. Before any drop, confirm correction row count is `0`, confirm no related generic audit evidence must be retained, define the exact residual schema, and obtain explicit destructive approval.
- If correction or audit rows exist, preserve them or obtain a separately approved retention/removal decision before DDL.
- No payment, refund, provider, billing-key, or email rollback is required because external calls were `0`.

The intentional untracked artifact `output/client-demo-screenshots-20260716-140514.zip` remains untouched.

## Follow-ups

- WI-015 status: **complete**.
- WI-016 status: **unblocked**; approved handoff: `deliverables/agent/WI-20260808-ATS-016-handoff.md`.
- Remaining verification:
  - WI-023: final full backend tests and coverage.
  - WI-024: final full frontend tests and coverage.
  - WI-025: final frontend typecheck gate.
  - WI-026: final frontend ESLint and Prettier gates.
  - WI-027: backend and frontend builds.
  - WI-028: administrator and payment security review.
  - WI-030: final cross-layer audit.
