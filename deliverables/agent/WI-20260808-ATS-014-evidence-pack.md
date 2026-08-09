# Evidence Pack: WI-20260808-ATS-014

## Summary (one-liner)

- WI-014 is complete: SR-96 backend/frontend role safety, durable audit storage, additive development schema application, and live MySQL/InnoDB concurrency verification passed.

## Scope / DoD Check

- [x] Stable self-demotion and last-admin domain errors added.
- [x] Active administrators are pessimistically locked in deterministic ID order.
- [x] Actor DB role is checked immediately before mutation.
- [x] Valid demotion clears the target Refresh Token.
- [x] Deleted administrators are excluded by the shared lock query.
- [x] Withdrawal uses `BillingAgreement -> UserSubscription -> active ADMIN guard -> target User` without a pre-guard user fetch lock.
- [x] Last-admin withdrawal is rejected before billing, subscription, or user mutation; another-admin withdrawal is allowed.
- [x] Deleted administrator targets cannot be mutated through `updateUserByAdmin`.
- [x] Append-only durable audit rows use `USER_ROLE_CHANGE` for both promotion and demotion, and record role-change and admin-withdrawal success/rejection without PII, passwords, or tokens.
- [x] Generic `before_state`, `after_state`, and `reason_code` columns are nullable in both the fresh schema and JPA entity; current services continue to populate them.
- [x] Current administrator self-demotion is disabled and explained in the UI.
- [x] Stable backend errors preserve the list and appear in row/modal feedback.
- [x] Successful mutations and current-user row mismatches refresh `/users/me`.
- [x] Centralized `403` role resynchronization covers all API paths except `/users/me` and auth paths.
- [x] Central `403` synchronization is deduplicated, does not retry, and preserves the original error.
- [x] `/users/me` refresh responses are bound to an internal session generation and user ID; same-session access-token rotation is accepted while logout and re-login responses are rejected as stale.
- [x] Five focused frontend test files pass 70 tests, and frontend typecheck passes.
- [x] MySQL cross-demotion and role-change/withdrawal tests pass against a disposable MySQL 8.0.45/InnoDB database.
- [x] Success audits join the outer transaction and rejection audits commit through a separate `REQUIRES_NEW` Spring bean.
- [x] Additive development DDL preserves the exact row-count digest of all original 39 tables.
- [x] The current development database contains the audit table; no manual migration file was created.

## Reference Documents (Tier 0-2)

| Tier | Document | Reason |
|------|----------|--------|
| 0 | `docs/standards/core-principles.md` | System constitution and testing rules |
| 0 | `docs/standards/development-standards.md` | Java/Spring and frontend implementation standards |
| 1 | `docs/policies/security-policy.md` | Security-sensitive implementation baseline |
| 1 | `docs/policies/access-control-policy.md` | Least privilege and default-deny policy |
| 2 | `docs/SR/SR-96.md` | Administrator role safety acceptance contract |
| 2 | `deliverables/user/REQ-20260808-ATS-004.md` | Approved policy and WI chain |

**Handoff:** `deliverables/agent/WI-20260808-ATS-014-handoff.md`

## Evidence Pointers

### Backend

- `src/main/java/com/atstudio/atstudio/common/exception/BUSINESS_ERROR.java:234` - stable role-protection errors.
- `src/main/java/com/atstudio/atstudio/repository/BillingAgreementRepository.java:47` - agreement-only pessimistic lock; no user fetch join before the ADMIN guard.
- `src/main/java/com/atstudio/atstudio/repository/UserRepository.java:21` - deterministic active-admin pessimistic lock query.
- `src/main/java/com/atstudio/atstudio/service/UserService.java:141` - withdrawal payment-lock prefix, guard membership check, last-admin rejection, and mutation ordering.
- `src/main/java/com/atstudio/atstudio/service/UserService.java:281` - actor/target validation, deleted-target rejection, last-admin guard, token clearing, and durable audit dispatch.
- `src/main/java/com/atstudio/atstudio/entity/AdminOperationAuditLog.java` - immutable, foreign-key-free append-only audit entity with nullable generic state/reason fields.
- `src/main/java/com/atstudio/atstudio/service/AdminOperationAuditService.java` - `MANDATORY` success audit that rolls back with its mutation.
- `src/main/java/com/atstudio/atstudio/service/AdminOperationRejectionAuditService.java` - public `REQUIRES_NEW` rejection audit.
- `src/test/java/com/atstudio/atstudio/service/AdminOperationAuditTransactionIntegrationTest.java` - success rollback and rejection-survival transaction proof, plus role/withdrawal audit state coverage.
- `src/main/java/com/atstudio/atstudio/controller/UserController.java:96` - authenticated actor ID forwarding.
- `src/test/java/com/atstudio/atstudio/service/UserServiceTest.java:150` - last-admin withdrawal leaves agreement, subscription, token, and user unchanged and proves repository call order.
- `src/test/java/com/atstudio/atstudio/service/UserServiceTest.java:824` - deleted retained-ADMIN target mutation rejection.
- `src/test/java/com/atstudio/atstudio/repository/UserRepositoryTest.java:61` - deleted-admin exclusion, deterministic order, and lock annotation test.
- `src/test/java/com/atstudio/atstudio/repository/UserRepositoryTest.java:84` - static contract that pre-guard payment locks do not fetch-join the user.
- `src/test/java/com/atstudio/atstudio/service/UserRoleChangeMysqlConcurrencyIntegrationTest.java:129` - live MySQL cross-demotion and role-change/actor-withdrawal race proof.

### Lock-order analysis

- Withdrawal retains the existing payment prefix: agreement first, then user subscription.
- The agreement query selects only `BillingAgreement`; the subscription query fetches the plan but not its user. Neither query therefore fetch-locks the target user before the administrator guard.
- Both withdrawal and `updateUserByAdmin` acquire the deterministically ID-ordered active-admin set before any separate target-user lock. Their common suffix is `active ADMIN guard -> target User`; no path in this WI acquires the same locks in reverse order.
- For an active administrator, the target user row is already part of the guard set. For a non-admin target, `findByIdForUpdate` runs only after the guard.

### Frontend

- `frontend/src/store/authStore.ts:88` - generation-bound, deduplicated `/users/me` refresh that accepts interceptor token rotation and rejects replaced-session responses before persistence.
- `frontend/src/api/client.ts:49` - centralized `403` exclusions, synchronization deduplication, no-retry behavior, and original rejection preservation.
- `frontend/src/pages/admin/UserManagePage.tsx:14` - domain-error mapping and role synchronization messages.
- `frontend/src/pages/admin/UserManagePage.tsx:74` - listed current-user mismatch detection.
- `frontend/src/pages/admin/UserManagePage.tsx:134` - successful mutation state update followed by `/users/me` refresh.
- `frontend/src/pages/admin/UserManagePage.tsx:215` - current-admin control disabling and row feedback.
- `frontend/src/api/client.test.ts:162` - centralized `403`, exclusion, deduplication, no-retry, and original-error tests.
- `frontend/src/store/authStore.test.ts:130` - persisted refresh, same-session coalescing/token rotation, logout, different-account login, same-user re-login, and returned-user mismatch tests.
- `frontend/src/pages/admin/UserManagePage.test.tsx:162` - self-demotion UI, error feedback, post-success refresh, and route reevaluation tests.

## Commands & Outputs

### Backend

- `.\gradlew.bat test --tests "com.atstudio.atstudio.config.V1BackendBaselineContractTest" --tests "com.atstudio.atstudio.service.UserServiceTest" --tests "com.atstudio.atstudio.repository.UserRepositoryTest" --tests "com.atstudio.atstudio.controller.UserControllerTest" --tests "com.atstudio.atstudio.service.AdminOperationAuditTransactionIntegrationTest" --tests "com.atstudio.atstudio.service.UserRoleChangeMysqlConcurrencyIntegrationTest"`
  - H2 targeted baseline: 72 discovered; 70 passed; 0 failures/errors; 2 gated MySQL tests skipped in this non-MySQL run.
  - `V1BackendBaselineContractTest`: 6 passed; `UserServiceTest`: 41 passed; `UserRepositoryTest`: 7 passed; `UserControllerTest`: 10 passed; `AdminOperationAuditTransactionIntegrationTest`: 6 passed.
- Live gated MySQL run:
  - MySQL 8.0.45, InnoDB.
  - `UserRoleChangeMysqlConcurrencyIntegrationTest`: 2 tests; 2 passed; 0 skipped/failures/errors; 17.153 seconds.
  - Gradle `BUILD SUCCESSFUL`.

### Database

- Current development database additive DDL:
  - Before: 39 tables; after: 40 tables.
  - Audit table: 12 columns, 4 indexes, 0 foreign keys, 0 audit rows.
  - Exact row-count digest for the original 39 tables remained unchanged.
  - `admin_operation_audit_logs` is applied to the current development database.
- Disposable database `ats_disposable_20260808_8e84782e`:
  - Fresh `schema.sql` plus `seed.sql` produced 40 tables and exactly one audit table.
  - Live MySQL concurrency tests passed before cleanup.
  - Database was dropped; `information_schema` confirmed `absent=true`.
- No manual migration file was created.

### Frontend

- Targeted verification: `adminContracts`, `client`, `authStore`, `UserManagePage`, and `adminSubscriberGaps`.
  - 5 files passed; 70 tests passed, 0 failed.
- `npm run typecheck`
  - Passed.
- `git diff --check`
  - Passed with existing Java CRLF-to-LF conversion warnings only.

## Changed Files

- `src/main/java/com/atstudio/atstudio/common/exception/BUSINESS_ERROR.java`
- `src/main/java/com/atstudio/atstudio/controller/UserController.java`
- `src/main/java/com/atstudio/atstudio/dto/user/UserAdminUpdateRequest.java`
- `src/main/java/com/atstudio/atstudio/entity/AdminOperationAuditLog.java`
- `src/main/java/com/atstudio/atstudio/entity/enums/AdminOperationAuditAction.java`
- `src/main/java/com/atstudio/atstudio/entity/enums/AdminOperationAuditOutcome.java`
- `src/main/java/com/atstudio/atstudio/entity/enums/AdminOperationAuditTargetType.java`
- `src/main/java/com/atstudio/atstudio/repository/AdminOperationAuditLogRepository.java`
- `src/main/java/com/atstudio/atstudio/repository/BillingAgreementRepository.java`
- `src/main/java/com/atstudio/atstudio/repository/UserRepository.java`
- `src/main/java/com/atstudio/atstudio/service/AdminOperationAuditService.java`
- `src/main/java/com/atstudio/atstudio/service/AdminOperationAuditState.java`
- `src/main/java/com/atstudio/atstudio/service/AdminOperationRejectionAuditService.java`
- `src/main/java/com/atstudio/atstudio/service/UserService.java`
- `src/main/resources/schema.sql`
- `src/test/java/com/atstudio/atstudio/config/V1BackendBaselineContractTest.java`
- `src/test/java/com/atstudio/atstudio/controller/UserControllerTest.java`
- `src/test/java/com/atstudio/atstudio/repository/UserRepositoryTest.java`
- `src/test/java/com/atstudio/atstudio/service/AdminOperationAuditTransactionIntegrationTest.java`
- `src/test/java/com/atstudio/atstudio/service/UserServiceTest.java`
- `src/test/java/com/atstudio/atstudio/service/UserRoleChangeMysqlConcurrencyIntegrationTest.java`
- `frontend/src/api/admin.ts`
- `frontend/src/api/adminContracts.test.ts`
- `frontend/src/api/client.ts`
- `frontend/src/api/client.test.ts`
- `frontend/src/store/authStore.ts`
- `frontend/src/store/authStore.test.ts`
- `frontend/src/pages/admin/UserManagePage.tsx`
- `frontend/src/pages/admin/UserManagePage.module.css`
- `frontend/src/pages/admin/UserManagePage.test.tsx`
- `frontend/src/test/coverage/adminSubscriberGaps.coverage.test.tsx`
- `deliverables/user/WI-20260808-ATS-014-summary.md`
- `deliverables/agent/WI-20260808-ATS-014-evidence-pack.md`

## Risks / Rollback

- No WI-014 acceptance blocker remains. The targeted frontend result is 5 files/70 tests; the broad REQ-level frontend suite remains owned by later quality-gate WIs.
- The current development database contains an empty additive audit table. Any rollback of that table would be a separately approved destructive DDL operation; none was performed here.

## Completion Boundary

- WI-014 is complete.
- REQ-20260808-ATS-004 remains in progress through its remaining planned WIs; WI-014 completion does not close the overall REQ.
