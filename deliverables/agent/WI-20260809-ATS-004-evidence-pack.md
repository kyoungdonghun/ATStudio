# Evidence Pack: WI-20260809-ATS-004

## Summary (one-liner)

- Completed a backend-only independent review of SR-96/SR-97 privileged mutations and found one authorization BLOCKER, three MAJOR contract/audit defects, and one MINOR locking-index defect.

## Findings

### BLOCKER-001 - Actor authorization can become stale during correction mutations

- **Evidence pointers:**
  - `src/main/java/com/atstudio/atstudio/service/AdminSubscriptionCorrectionService.java:180-188` - request resolves actor before domain lock waits.
  - `src/main/java/com/atstudio/atstudio/service/AdminSubscriptionCorrectionService.java:225-236` - approval resolves the actor through a non-locking lookup inside the transition call.
  - `src/main/java/com/atstudio/atstudio/service/AdminSubscriptionCorrectionService.java:241-288` - execute obtains domain/correction locks, performs a non-locking actor lookup, then mutates entitlement and agreement state.
  - `src/main/java/com/atstudio/atstudio/service/AdminSubscriptionCorrectionService.java:487-493` - `resolveAdminActor` uses `userRepository.findById`, not a locking/current authorization fence.
- **Exact impact:** A concurrent `ADMIN -> USER` change or withdrawal can commit after actor resolution while the stale transaction still creates, approves, or executes a correction.
- **Reasoning:** Ordinary MVCC reads do not conflict with `UserService`'s pessimistic administrator locks. The mutation transaction therefore has no serialization edge against privilege removal.
- **Missing test:** MySQL repeatable-read races for demotion/withdrawal versus each of request, approve, and execute.
- **Recommended repair:** Use a shared actor-row/authorization fence with a documented global order. For the current design, acquire `findByIdForUpdate` after domain/correction locks and immediately recheck active ADMIN before each mutation; verify both race outcomes.

### MAJOR-001 - Backend does not enforce preview before request

- **Evidence pointers:**
  - `src/main/java/com/atstudio/atstudio/controller/AdminUserSubscriptionCorrectionController.java:32-36` - standalone preview endpoint.
  - `src/main/java/com/atstudio/atstudio/controller/AdminUserSubscriptionCorrectionController.java:63-67` - standalone creation endpoint accepts the same request directly.
  - `src/main/java/com/atstudio/atstudio/service/AdminSubscriptionCorrectionService.java:121-136` - preview returns a response only.
  - `src/main/java/com/atstudio/atstudio/service/AdminSubscriptionCorrectionService.java:180-220` - request creation has no preview receipt/state check.
  - `src/test/java/com/atstudio/atstudio/service/AdminSubscriptionCorrectionServiceTest.java:271-285` - focused test creates a correction without preview evidence.
- **Exact impact:** Direct clients bypass the approved preview/confirmation phase while still creating an executable workflow.
- **Reasoning:** Payload revalidation is necessary but does not prove operator review of the exact preview.
- **Missing test:** absent/expired/stale/mismatched preview receipt rejection.
- **Recommended repair:** Bind a short-lived server receipt to actor, source snapshot, target payload, and expiry; require it at request creation and revalidate under locks.

### MAJOR-002 - Request/approval rejections are not durably auditable

- **Evidence pointers:**
  - `src/main/java/com/atstudio/atstudio/service/AdminSubscriptionCorrectionService.java:190-196` - duplicate and validation errors throw directly.
  - `src/main/java/com/atstudio/atstudio/service/AdminSubscriptionCorrectionService.java:225-236` - approval errors throw directly.
  - `src/main/java/com/atstudio/atstudio/service/AdminSubscriptionCorrectionService.java:290-297,517-566` - durable rejection handling exists only for execute.
  - `src/test/java/com/atstudio/atstudio/service/AdminSubscriptionCorrectionServiceTest.java:305-359` - request/approval rejection tests assert errors but no durable audit.
- **Exact impact:** Operators and reviewers cannot reconstruct rejected request/approval attempts, their phase, actor, target, stable reason, or contemporaneous minimal state.
- **Reasoning:** The correction row explains committed request/approval transitions only; rejected pre-transition work leaves no row/event.
- **Missing test:** `REQUIRES_NEW` request/approval rejection survival after outer rollback.
- **Recommended repair:** Add phase-aware minimal rejection events for request and approval, preserve the original error on audit failure, and exclude raw request data.

### MAJOR-003 - Free-text operator fields are persisted without sensitive-data controls

- **Evidence pointers:**
  - `src/main/java/com/atstudio/atstudio/service/UserService.java:396-404` - role-change reason receives trim/length checks only.
  - `src/main/java/com/atstudio/atstudio/service/AdminSubscriptionCorrectionService.java:301-310,570-578` - correction reason/notes receive blank/length/trim checks only.
  - `src/main/java/com/atstudio/atstudio/service/AdminSubscriptionCorrectionService.java:218,233-235,272` - raw values enter correction history.
  - `src/main/java/com/atstudio/atstudio/service/AdminOperationAuditService.java:42,90` - raw reason is copied into durable audit.
  - `src/main/resources/schema.sql:1042,1077,1083-1084` - durable free-text columns.
  - `docs/policies/security-policy.md:233-240` - raw provider identifiers are forbidden in audit free text.
- **Exact impact:** Emails, phone numbers, tokens, billing keys, or provider identifiers pasted into operator notes become durable and can be returned through administrator history APIs.
- **Reasoning:** Minimal audit-state serialization is safe, but it does not protect separate free-text columns.
- **Missing test:** adversarial reason/approval/execution notes containing PII, JWT-like tokens, billing keys, and raw provider IDs.
- **Recommended repair:** Use allowlisted structured reasons and validated ticket references. Apply one server-side sensitive-data policy before any workflow/audit persistence if free text remains.

### MINOR-001 - Global administrator lock query lacks a compatible index

- **Evidence pointers:**
  - `src/main/java/com/atstudio/atstudio/repository/UserRepository.java:22-26` - pessimistic predicate/order over `role`, `isDeleted`, and `id`.
  - `src/main/resources/schema.sql:24-43` - no role/deleted/index path; only primary, nickname, and email indexes.
  - `src/main/java/com/atstudio/atstudio/service/UserService.java:150,286` - query is used by every withdrawal and every administrator update.
- **Exact impact:** The MySQL locking read requires a broad users scan and can block unrelated user updates/inserts or cause lock-wait timeouts as cardinality grows.
- **Reasoning:** The logical global guard is valid; the physical scan unnecessarily expands its locking footprint.
- **Missing test:** MySQL `EXPLAIN`/index assertion and unrelated user-write contention proof.
- **Recommended repair:** Add a predicate/order-compatible index such as `(role, is_deleted, id)` or serialize through a dedicated guard row, then rerun role/withdrawal races.

## Scope / DoD Check

- DoD items:
  - [x] Reviewed last-admin and role-mutation behavior under the implemented locking strategy.
  - [x] Reviewed preview/request/approve/execute transitions, stale snapshots, authorization rechecks, lock order, and retry behavior.
  - [x] Reviewed audit durability boundaries, minimal-state serialization, free-text handling, and schema support.
  - [x] Reviewed entitlement mutation and local provider/payment isolation.
  - [x] Classified every confirmed finding with severity, file/line evidence, impact, reasoning, missing test, and recommended repair.
  - [x] Recorded residual risks, rollback implications, and downstream block status.

## Reference Documents (Tier 0-2)

**Injected Context** (from WI Handoff Packet):

| Tier | Document | Reason |
|------|----------|--------|
| 0 | `docs/standards/core-principles.md` | Project constitution and approved execution boundary |
| 0 | `docs/standards/development-standards.md` | Java/JPA, critical-path test, and review standards |
| 1 | `docs/policies/security-policy.md` | Audit, secret/PII, and provider-reference constraints |
| 1 | `docs/policies/access-control-policy.md` | Least privilege and separation of duties |
| Context | `deliverables/user/REQ-20260808-ATS-004.md` | Approved requirement and WI dependency plan |
| Context | `docs/SR/SR-96.md` | Last-admin and role-mutation contract |
| Context | `docs/SR/SR-97.md` | Local subscription-correction contract |
| Context | `deliverables/agent/WI-20260808-ATS-028-handoff.md` | Downstream security-review contract |
| Handoff | `deliverables/agent/WI-20260809-ATS-004-handoff.md` | Authoritative backend-only scope and output contract |

**Injection Rules Applied:**

- Assignee: `cr`
- Task type: independent backend code/security review
- Scope restriction: only handoff-listed backend files, schema portions, and corresponding focused tests; no frontend inspection.

## Reviewed Symbols

| File | Reviewed symbols / portions |
|------|-----------------------------|
| `src/main/java/com/atstudio/atstudio/service/UserService.java` | `withdraw`, `updateUserByAdmin`, actor/target guards, success/rejection audit calls, reason normalization |
| `src/main/java/com/atstudio/atstudio/repository/UserRepository.java` | `findByIdForUpdate`, `findActiveAdminsForRoleChange` |
| `src/main/java/com/atstudio/atstudio/controller/UserController.java` | administrator update actor forwarding and endpoint authorization |
| `src/main/java/com/atstudio/atstudio/service/AdminSubscriptionCorrectionService.java` | all public workflow methods; lock order; actor resolution; state/provider validation; idempotency; audit helpers |
| `src/main/java/com/atstudio/atstudio/controller/AdminUserSubscriptionCorrectionController.java` | preview/request/approve/execute endpoint contract and class-level authorization |
| `src/main/java/com/atstudio/atstudio/service/AdminOperationAuditService.java` | role, withdrawal, and correction success audit persistence |
| `src/main/java/com/atstudio/atstudio/service/AdminOperationAuditState.java` | minimal user/subscription state serialization |
| `src/main/java/com/atstudio/atstudio/entity/AdminSubscriptionCorrection.java` | persisted snapshots, actors/notes, and status transitions |
| `src/main/java/com/atstudio/atstudio/repository/AdminSubscriptionCorrectionRepository.java` | graph queries, execution projection, correction/non-terminal pessimistic locks |
| `src/main/resources/schema.sql` | `users`, `user_subscriptions`, `admin_operation_audit_logs`, `admin_subscription_corrections` |

## Focused Tests Inspected

- `src/test/java/com/atstudio/atstudio/service/UserServiceTest.java`
- `src/test/java/com/atstudio/atstudio/service/UserRoleChangeMysqlConcurrencyIntegrationTest.java`
- `src/test/java/com/atstudio/atstudio/repository/UserRepositoryTest.java`
- `src/test/java/com/atstudio/atstudio/controller/UserControllerTest.java`
- `src/test/java/com/atstudio/atstudio/service/AdminSubscriptionCorrectionServiceTest.java`
- `src/test/java/com/atstudio/atstudio/service/AdminSubscriptionCorrectionMysqlConcurrencyIntegrationTest.java`
- `src/test/java/com/atstudio/atstudio/controller/AdminUserSubscriptionCorrectionControllerTest.java`
- `src/test/java/com/atstudio/atstudio/repository/AdminSubscriptionCorrectionRepositoryContractTest.java`
- `src/test/java/com/atstudio/atstudio/service/AdminOperationAuditTransactionIntegrationTest.java`
- Targeted schema assertions in `src/test/java/com/atstudio/atstudio/config/V1BackendBaselineContractTest.java`
- Targeted current-principal reload evidence in `src/test/java/com/atstudio/atstudio/controller/SecurityFilterChainTest.java`

## Confirmed Controls / No Confirmed Finding

- **Last ADMIN:** `UserRepository.java:22-26` plus `UserService.java:150-159,286-323` serialize the active-admin decision; focused MySQL tests at `UserRoleChangeMysqlConcurrencyIntegrationTest.java:110-223` cover cross-demotion and demotion/withdrawal. No confirmed invariant defect was found in this path.
- **Impossible status/date states:** `AdminSubscriptionCorrectionService.java:331-349,352-363` rejects inactive/wrong-type plans and invalid `EXPIRED`/`ACTIVE`/`CANCELLED` date combinations at request and execute.
- **Stale/provider state:** `AdminSubscriptionCorrectionService.java:263-269,391-422` rechecks subscription/agreement snapshots and provider-outcome-pending orders before mutation.
- **Provider isolation:** The reviewed correction service contains repository reads and local `agreement.cancel()` at `:280-284`; no provider client, charge, refund, billing-key deletion, email call, or payment-ledger write appears in scope.
- **Retry/idempotency:** `AdminSubscriptionCorrectionRepository.java:85-88` pessimistically locks the correction; `AdminSubscriptionCorrectionService.java:257-259` short-circuits `SUCCEEDED`; `AdminSubscriptionCorrectionServiceTest.java:394-421` statically verifies a sequential retry records one success audit.
- **Audit transaction boundary:** `AdminOperationAuditService` uses `MANDATORY` for success; focused integration-test source at `AdminOperationAuditTransactionIntegrationTest.java:53-203` covers commit/rollback and durable rejection behavior for the implemented execute/role paths.

## Commands & Outputs

- Commands executed were limited to targeted read-only inspection:
  - `Get-Content -LiteralPath 'deliverables/agent/WI-20260809-ATS-004-handoff.md'`
  - `rg -n -i -C 3 '<approved-invariant terms>' <handoff-listed policy/context files>`
  - `git diff --name-only -- <handoff-listed backend paths and focused tests>`
  - `git diff --unified=80 -- '.../UserService.java' '.../UserRepository.java' '.../UserController.java'`
  - `Get-Content -LiteralPath <each handoff-listed backend file>`
  - `rg --files src/main/java/com/atstudio/atstudio src/test/java/com/atstudio/atstudio | rg '<focused symbol patterns>'`
  - `rg -n '<symbol/evidence patterns>' <handoff-listed files and focused tests>`
  - `Get-Content -LiteralPath <focused-test> | Select-Object -Skip <n> -First <n>`
- Outputs:
  - Confirmed findings: `1 BLOCKER / 3 MAJOR / 1 MINOR`.
  - Confirmed no external/provider call was made during review.
  - No full-suite or focused test process was launched; test source was inspected only.
  - No secrets or intentional ZIP contents were read.

## Tests

- **Not executed.** The injected review constraint limited commands to read-only `rg`, `Get-Content`, and `git diff` and prohibited data/external changes.
- Static test evidence is not reported as a current pass result.
- Required repair tests:
  - actor demotion/withdrawal versus request/approve/execute under MySQL repeatable read;
  - request without valid preview receipt;
  - durable request/approval rejection audit after outer rollback;
  - sensitive note rejection/redaction across role and correction paths;
  - unrelated user write during active-admin guard;
  - concurrent execute/execute single-application proof.

## Residual Risks

- Current MySQL behavior was not re-executed in this WI; environment-dependent lock timing remains unverified live.
- Focused correction concurrency tests cover request/request and execute/request, not execute/execute or privilege-removal races.
- No `EXPLAIN` or lock-wait measurement was permitted for the unindexed administrator guard.
- The literal handoff wildcard covered `AdminOperationAuditService` and `AdminOperationAuditState`; rejection durability was assessed through listed callers and focused transaction tests without expanding into unrelated files.

## Risks / Rollback

- **Review risk:** Findings are source- and contract-backed; runtime timing evidence remains a repair-test obligation.
- **Product changes:** None. Code, tests, schema, data, provider state, secrets, ZIP, and Git history were untouched.
- **Review deliverables changed:**
  - `deliverables/user/WI-20260809-ATS-004-summary.md`
  - `deliverables/agent/WI-20260809-ATS-004-evidence-pack.md`
- **Rollback:** Revert only the two review documents to undo this WI's filesystem effect. Any product rollback/endpoint containment is outside this read-only WI and must keep controller, service, entity/repository, audit, and schema changes consistent.

## Downstream Status

- `WI-20260808-ATS-028` **remains blocked**.
- Unblock condition: repair BLOCKER-001 and all MAJOR findings, add the focused tests above, and produce current passing evidence. MINOR-001 requires repair or explicit risk acceptance backed by MySQL evidence.
