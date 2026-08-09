# WI-20260808-ATS-014 Summary

## Status

WI-20260808-ATS-014 is complete. Backend role protection, durable audit storage, frontend role synchronization, additive schema application, and live MySQL concurrency verification all passed their acceptance checks.

REQ-20260808-ATS-004 remains in progress because its later WIs are outside WI-014. No manual migration file was created.

## Completed

### Backend

- Added stable domain errors for self-demotion, last-admin protection, and stale actor authority.
- Added a shared role-change guard that locks all active `ADMIN` rows with `PESSIMISTIC_WRITE` in deterministic ID order.
- Re-checks the actor's locked DB role immediately before applying an administrator-controlled update.
- Rejects `ADMIN -> USER` self-demotion and changes that would remove the last active administrator.
- Clears the demoted administrator's Refresh Token after a valid demotion.
- Serializes withdrawal through `BillingAgreement -> UserSubscription -> active ADMIN guard -> target User`. The agreement lock no longer fetch-locks the user before the shared guard, so role change and withdrawal retain the same `ADMIN guard -> target User` suffix.
- Rejects withdrawal of the last active administrator before billing, subscription, or user mutation; withdrawal remains allowed when another active administrator is locked in the guard set.
- Rejects direct administrator updates to deleted targets, including users whose retained role is `ADMIN`.
- Persists append-only `admin_operation_audit_logs` rows without foreign keys. Success rows join the mutation transaction; rejection rows use a separate public `REQUIRES_NEW` bean method so they survive the outer rollback. Stored state is limited to role and `isDeleted`; it excludes PII, passwords, and tokens.
- Requires a nonblank, maximum-500-character operator reason for an actual role change in both DTO and service validation. Verification-only and unchanged-role requests do not require a reason or emit a role audit.

### Frontend

- Disables the current administrator's role control and explains the restriction in the user row.
- Shows administrator-demotion impact in the confirmation modal.
- Maps `SELF_ADMIN_DEMOTION_FORBIDDEN`, `LAST_ADMIN_REQUIRED`, and `ADMIN_ROLE_REQUIRED` into target-row and modal feedback without replacing the user list.
- Refreshes `GET /users/me` into the auth store after every successful role mutation and when the listed current-user role differs from local state.
- Centrally resynchronizes local `ADMIN` sessions after a `403` from any non-auth, non-`/users/me` API request.
- Central `403` handling deduplicates concurrent synchronization, never retries the failed request, and preserves the original rejection even if synchronization fails.
- Binds `/users/me` refreshes to a session generation and user ID: interceptor token rotation remains valid, while logout or re-login makes an old response stale.
- Updated auth-store state causes existing menus and `ProtectedRoute` checks to reevaluate immediately.

## Verification

### Backend

- Command: `.\gradlew.bat test --tests "com.atstudio.atstudio.config.V1BackendBaselineContractTest" --tests "com.atstudio.atstudio.service.UserServiceTest" --tests "com.atstudio.atstudio.repository.UserRepositoryTest" --tests "com.atstudio.atstudio.controller.UserControllerTest" --tests "com.atstudio.atstudio.service.AdminOperationAuditTransactionIntegrationTest" --tests "com.atstudio.atstudio.service.UserRoleChangeMysqlConcurrencyIntegrationTest"`
- H2 targeted baseline: 72 discovered; 70 passed; 0 failures/errors; 2 gated MySQL tests skipped in the non-MySQL run.
- Live MySQL 8.0.45/InnoDB gated run: `UserRoleChangeMysqlConcurrencyIntegrationTest` 2 tests, 2 passed, 0 skipped/failures/errors, 17.153 seconds; Gradle `BUILD SUCCESSFUL`.
- The Spring audit test proves success rollback coupling and `REQUIRES_NEW` rejection-audit survival. The live MySQL run proves both guarded concurrency scenarios against InnoDB.

### Frontend

- Targeted files: `adminContracts`, `client`, `authStore`, `UserManagePage`, and `adminSubscriberGaps`.
- Result: 5 test files passed; 70 tests passed, 0 failed.
- Command: `npm run typecheck`
- Result: passed (`tsc --noEmit`).
- `git diff --check`: passed; only existing Java line-ending conversion warnings were reported.

## Database Verification

- Current development database additive DDL: tables increased from 39 to 40; the audit table has 12 columns, 4 indexes, 0 foreign keys, and 0 audit rows.
- Exact row-count digest for the original 39 development tables was unchanged after the additive DDL.
- Disposable database `ats_disposable_20260808_8e84782e`: fresh `schema.sql` plus `seed.sql` produced 40 tables and exactly one audit table.
- The disposable database was dropped after verification; `information_schema` confirmed `absent=true`.
- The audit table is now applied to the current development database. No manual migration file was created.

## Audit Completion

`admin_operation_audit_logs` is a dedicated fresh-baseline table with snapshot identifiers rather than foreign keys. It records action, target, nullable actor, outcome, minimal before/after state, reason code/note, and timestamps. The entity is immutable and exposes no mutation setters. Payment and company-certification audit tables were not reused.

## Completion Boundary

WI-014 is complete with no remaining acceptance blocker. REQ-20260808-ATS-004 continues through its remaining planned WIs; this status does not close the overall REQ.
