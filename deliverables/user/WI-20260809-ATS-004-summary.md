# WI-20260809-ATS-004 Backend Independent Review Summary

## Findings

### BLOCKER-001 - Subscription correction authorization is not serialized with administrator demotion or withdrawal

- **Evidence:** `AdminSubscriptionCorrectionService.java:180-188` resolves the requester before the request-side domain locks; `:225-236` and `:241-273` resolve the approver/executor without locking the actor row; `:487-493` uses ordinary `userRepository.findById` for all three mutation checks.
- **Impact:** An administrator can be demoted or withdrawn after the non-locking read and still create, approve, or execute a correction. Execution can mutate entitlement and the local billing-agreement status after the actor has lost privilege.
- **Reasoning:** The actor read does not conflict with the pessimistic administrator locks used by `UserService`. Proximity to the mutation does not close the race without a shared database lock or equivalent authorization fence.
- **Missing test:** No focused MySQL race covers demotion/withdrawal against `requestCorrection`, `approveCorrection`, or `executeCorrection`.
- **Recommended fix:** Recheck the actor through a pessimistic row lock immediately before each correction mutation, using one global lock order after the correction/domain rows, or introduce an equivalent authorization-fence record. Add repeatable-read races proving the correction loses when privilege is removed first and cannot commit after privilege removal.

### MAJOR-001 - The required preview phase is optional at the backend boundary

- **Evidence:** `AdminUserSubscriptionCorrectionController.java:32-36` exposes preview and `:63-67` exposes an independent request endpoint. `AdminSubscriptionCorrectionService.java:121-136` returns preview data only, while `:180-220` creates a correction from the original request without a preview receipt, version, digest, or persisted preview state. `AdminSubscriptionCorrectionServiceTest.java:271-285` directly creates a request without preview evidence.
- **Impact:** A direct API caller can bypass the approved `preview -> request -> approve -> execute` sequence and omit the explicit preview/confirmation step.
- **Reasoning:** Revalidating the payload at request time protects state validity, but it does not prove that the required preview occurred or that the operator confirmed that exact preview.
- **Missing test:** No test rejects request creation when preview evidence is absent, expired, stale, or payload-mismatched.
- **Recommended fix:** Return a short-lived server-bound preview receipt over the actor, locked source snapshot, target state, and expiry. Require and consume/validate it during request creation, while still revalidating current state under locks.

### MAJOR-002 - Rejected request and approval actions have no durable audit record

- **Evidence:** `AdminSubscriptionCorrectionService.java:190-196` throws for duplicate/invalid requests and `:225-236` throws for invalid approval transitions/notes without recording a rejection. Only execute has the rejection path at `:290-297` and `:517-566`.
- **Impact:** Duplicate, invalid-state, invalid-note, and authorization-rejected request/approval attempts cannot be reconstructed from durable workflow or audit records.
- **Reasoning:** Successful request/approval transitions are represented by the correction row, but exceptions before those transitions roll back without a phase-specific rejection event.
- **Missing test:** Focused tests assert the error for duplicate request and oversized approval note but do not assert a durable rejection audit.
- **Recommended fix:** Add phase-aware, minimal `REQUIRES_NEW` rejection auditing for request and approval failures. Preserve the original business error if audit persistence fails, and avoid storing request bodies or sensitive free text.

### MAJOR-003 - Operator notes can carry secrets or PII into durable workflow and audit storage

- **Evidence:** `UserService.java:396-404` and `AdminSubscriptionCorrectionService.java:301-310,570-578` only trim and length-check notes. Raw values are persisted at `AdminSubscriptionCorrectionService.java:218,233-235,272` and copied into audit at `AdminOperationAuditService.java:42,90`. Storage columns are present at `schema.sql:1042,1077,1083-1084`.
- **Impact:** An operator can place an email, phone number, token, billing key, or raw provider identifier in a reason/note and make it durable in correction history and/or administrator audit logs.
- **Reasoning:** Minimal state JSON is appropriately bounded, but free-text handling has no allowlist, structured reason code, redaction, or sensitive-value rejection.
- **Missing test:** Existing audit tests check that state JSON omits sensitive field names, but no test injects sensitive content through reason, approval, or execution notes.
- **Recommended fix:** Prefer allowlisted reason codes plus a validated internal ticket reference. If free text remains, pass every note through one server-side sensitive-data policy before either workflow or audit persistence, and add adversarial note tests.

### MINOR-001 - The active-admin locking query has no supporting predicate index

- **Evidence:** `UserRepository.java:22-26` performs a pessimistic locking read over `role`, `isDeleted`, and `id`. The `users` table has only primary, nickname, and email indexes at `schema.sql:24-43`. Every withdrawal invokes the query at `UserService.java:150`, including ordinary users.
- **Impact:** MySQL must scan a broad `users` range for a locking read, increasing contention and lock-wait timeouts for unrelated user mutations as the table grows.
- **Reasoning:** The correctness guard is intentionally global, but the current physical access path makes its lock footprint much larger than the active administrator set.
- **Missing test:** No focused MySQL test checks `EXPLAIN`/index use or proves that an unrelated user write remains unblocked during the guard query.
- **Recommended fix:** Add and verify a composite index compatible with the predicate and order, such as `(role, is_deleted, id)`, or replace the scan with a dedicated serialized guard row. Re-run the existing role races plus an unrelated-write contention test.

## Confirmed Controls

- No confirmed last-admin invariant defect was found in the reviewed role/withdrawal path: the active administrator set is pessimistically locked in deterministic ID order before demotion/removal decisions.
- Server-side plan activity, user-type, status/date, stale-snapshot, and in-flight provider-outcome checks are present in the correction service.
- No provider client, refund, charge, billing-key deletion, email call, or payment-ledger mutation appears in the reviewed correction path. `agreement.cancel()` is a local entity mutation.
- Execution acquires a pessimistic correction lock and returns immediately for `SUCCEEDED`; the focused unit test checks a sequential retry applies the success audit once.

## Residual Risks

- Focused tests were inspected but not executed because this review was restricted to read-only `rg`, `Get-Content`, and `git diff` evidence. Runtime results from earlier WIs were not treated as current execution evidence.
- The focused MySQL suite covers concurrent request/request and execute/request, but not concurrent execute/execute or correction mutation versus actor demotion/withdrawal.
- The broad lock-footprint impact is established from query/index structure; no current `EXPLAIN` or lock-wait measurement was permitted.

## Review Decision

- **Result:** 1 BLOCKER, 3 MAJOR, 1 MINOR.
- **WI-20260808-ATS-028:** **REMAINS BLOCKED** until BLOCKER-001 and the MAJOR findings are repaired and covered by focused tests. MINOR-001 should be resolved or explicitly accepted with MySQL evidence before promotion.
- **Rollback implications:** No product, test, schema, data, provider, secret, ZIP, Git commit, or push change was made. This WI adds review documentation only; any product rollback or endpoint containment remains a separately approved operation and should keep controller, service, entity/repository, audit, and schema changes aligned.

## Reviewed Surface

- Symbols: `UserService.withdraw`, `UserService.updateUserByAdmin`, `UserRepository.findActiveAdminsForRoleChange`, all `AdminSubscriptionCorrectionService` workflow methods and lock/validation/audit helpers, `AdminSubscriptionCorrection` transitions, `AdminSubscriptionCorrectionRepository` lock queries, both reviewed controllers, `AdminOperationAuditService`, and `AdminOperationAuditState`.
- Schema portions: `users`, `user_subscriptions`, `admin_operation_audit_logs`, and `admin_subscription_corrections`.
- Commands: targeted `rg`, `Get-Content`, and scoped `git diff` only; no test or full-suite command was run.
