---
version: 1.0
last_updated: 2026-08-14
project: ATS
owner: PG
category: audit
status: complete
dependencies:
  - path: WI-20260809-ATS-053-pg-r2-handoff.md
    reason: PG R2 scope, acceptance criteria, constraints, and output contract
  - path: WI-20260809-ATS-053-pg-result.md
    reason: Initial PG FAIL and PG-053-001 finding under re-review
  - path: WI-20260809-ATS-053-remediation-handoff.md
    reason: Approved remediation contract
---

# PG Re-review Result: WI-20260809-ATS-053 R2

## Findings

No open P0, P1, P2, or P3 finding was identified in the assigned session,
authority, and PII scope.

### PG-053-001 [P2] Closed

- `frontend/src/api/admin.ts:82-89` gives `updateUserAdmin` an explicit
  `skipAdminRoleSync` request option.
- `frontend/src/api/client.ts:93-102` reads that option only in the centralized
  ADMIN `403` synchronization branch. The separate `401` branch at `:104-151`
  remains governed by `_retry`, `skipAuthReplay`, and authentication-path
  exclusions, so the new option does not suppress authentication refresh or
  replay.
- `frontend/src/pages/admin/UserManagePage.tsx:221-238` owns the refresh only
  for the exact `403 ADMIN_ROLE_REQUIRED` rejection.
- The integrated production-path regression at
  `frontend/src/pages/admin/UserManagePage.integration.test.tsx:111-160` uses
  the real Axios response interceptor, ADMIN wrapper, auth store, page, and
  `ProtectedRoute`. It proves one PUT, one `GET /users/me`, no PUT replay, the
  exact server-returned `USER` profile in memory and browser storage, and the
  canonical redirect away from the ADMIN route.
- `frontend/src/api/client.test.ts:233-353` independently preserves generic
  ADMIN `403` synchronization, opt-out behavior, original-error preservation,
  and eligible `401` refresh/replay when only `skipAdminRoleSync` is set.

The double-refresh mechanism reported in the initial PG review is no longer
present. `PG-053-001` is closed.

## Verdict

**PASS**

The R2 handoff permits PASS only with no open P0-P3 issue. That condition is
met in the assigned scope.

## Authority And PII Evidence

- `frontend/src/store/authStore.ts:94-138` obtains the current profile through
  `fetchMe`, rejects stale session-generation/user-ID results, and persists only
  the server-returned profile. It cannot synthesize ADMIN authority.
- `frontend/src/router/ProtectedRoute.tsx:29-60` derives access from the current
  auth-store role and redirects a demoted session.
- `src/main/java/com/atstudio/atstudio/config/SecurityConfig.java:93-103` and
  `src/main/java/com/atstudio/atstudio/controller/UserController.java:76-102`
  retain ADMIN guards for User list, detail, and update at both request and
  method boundaries.
- `src/main/java/com/atstudio/atstudio/dto/user/UserDetailResponse.java:9-35`
  remains the bounded profile projection and contains no password, token, or
  credential field.
- `frontend/src/api/admin.ts:41-74` invokes only that detail contract.
  `frontend/src/pages/admin/UserManagePage.tsx:159-191,362-412` aborts and
  retires superseded detail requests and renders only explicit DTO fields. No
  cross-user PII publication, credential rendering, logging, or additional
  browser persistence was found.

## Tests Run

- `npm test -- --run src/pages/admin/UserManagePage.integration.test.tsx src/pages/admin/UserManagePage.test.tsx src/api/client.test.ts src/api/adminContracts.test.ts src/store/authStore.test.ts src/router/ProtectedRoute.test.tsx`
  from `frontend/`: PASS, 6 files and 66/66 tests.
- `.\gradlew.bat test --tests "com.atstudio.atstudio.controller.UserControllerTest" --rerun-tasks`:
  PASS, 10/10 tests with 0 skipped, failures, or errors.
- `npm run typecheck` from `frontend/`: PASS.

## Residual Risk

- No live browser network capture, deployed environment, or production data was
  inspected. The integrated request-count evidence uses the production Axios
  stack with a local test adapter.
- Full frontend coverage, ESLint, Prettier, build, and documentation validation
  results in the Evidence Pack were inspected but not independently rerun by PG.
- The approved ADMIN detail DTO intentionally contains email and phone/profile
  fields. This review confirms authorization, request ownership, and bounded
  rendering of that existing contract; it does not approve expanding the DTO.

## Related Documents

- [PG R2 Handoff](WI-20260809-ATS-053-pg-r2-handoff.md)
- [Initial PG Result](WI-20260809-ATS-053-pg-result.md)
- [Remediation Handoff](WI-20260809-ATS-053-remediation-handoff.md)
- [Evidence Pack](WI-20260809-ATS-053-evidence-pack.md)
- [Security Policy](../../docs/policies/security-policy.md)
- [Access Control Policy](../../docs/policies/access-control-policy.md)
