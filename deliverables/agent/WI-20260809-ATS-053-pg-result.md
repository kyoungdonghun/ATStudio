---
version: 1.0
last_updated: 2026-08-14
project: ATS
owner: PG
category: audit
status: active
dependencies:
  - path: WI-20260809-ATS-053-pg-handoff.md
    reason: PG review scope, acceptance criteria, constraints, and output contract
  - path: WI-20260809-ATS-053-evidence-pack.md
    reason: Implementation and verification claims under independent review
---

# PG Review Result: WI-20260809-ATS-053

## Findings

### PG-053-001 [P2] A typed stale-ADMIN 403 performs two sequential current-user refreshes

- `frontend/src/api/client.ts:92-100` already awaits the centralized ADMIN 403
  role synchronization before rejecting the original response to the caller.
- `frontend/src/pages/admin/UserManagePage.tsx:221-238` receives that same
  rejection and invokes `refreshRoleSnapshot()` again when the response is a
  typed `ADMIN_ROLE_REQUIRED`/403.
- `frontend/src/store/authStore.ts:103-138` coalesces only an in-flight refresh.
  Because the interceptor awaits the first refresh before the page catch runs,
  the page call starts a second `fetchMe()` instead of joining the first one.
- `frontend/src/pages/admin/UserManagePage.test.tsx:18-22` replaces the real
  ADMIN API module, so the test at `:341-363` exercises only the page-owned
  refresh. Separately, `frontend/src/api/client.test.ts:233-245` exercises only
  the interceptor-owned refresh. Both pass while the combined production path
  violates the exactly-once claim.
- Impact: one rejected mutation causes two sequential `GET /api/users/me`
  requests, with the second potentially starting after the first refresh has
  demoted the session and unmounted the ADMIN page. The mutation itself is not
  replayed, and no privilege escalation was found; severity is P2.
- Required remediation: establish one refresh owner for this mutation. The
  bounded option is to add a `skipAdminRoleSync` Axios request option, honor it
  in the centralized 403 branch, set it only on `updateUserAdmin`, and retain
  the page's typed `ADMIN_ROLE_REQUIRED`/403 refresh so its success/failure UI
  remains accurate. Add a regression that drives the role-change PUT through
  the real response interceptor and auth store, then proves one PUT, one
  current-user GET, zero PUT replays, the server-returned `USER` role, and the
  canonical `ProtectedRoute` redirect.

No P0, P1, or P3 finding was identified in the assigned session, authority, and
PII scope.

## Verdict

**FAIL**

`PG-053-001` is an open P2 finding. The handoff permits PASS only when no open
P0-P3 finding remains.

## Authority And PII Evidence

- Current-user refresh is server-derived: `frontend/src/store/authStore.ts:94-138`
  calls `fetchMe()`, requires the initiating session generation and user ID to
  remain current, requires the response user ID to match, and persists only the
  returned current-user snapshot. It cannot synthesize ADMIN authority.
- Canonical client routing is role-derived:
  `frontend/src/router/ProtectedRoute.tsx:29-60` observes the auth-store role and
  redirects a demoted session from an ADMIN route.
- Server authority remains primary:
  `src/main/java/com/atstudio/atstudio/config/SecurityConfig.java:93-103`
  protects User list, detail, and update routes with ADMIN authority, while
  `src/main/java/com/atstudio/atstudio/controller/UserController.java:76-101`
  repeats method-level ADMIN guards for list, detail, and update.
- The detail response is bounded by
  `src/main/java/com/atstudio/atstudio/dto/user/UserDetailResponse.java:9-35`.
  It contains the contracted identity/profile fields and no password, access
  token, refresh token, credential, or entity projection.
- `frontend/src/api/admin.ts:41-74` types and invokes only that detail contract.
  `UserManagePage.tsx:169-191` aborts and retires old requests, and `:362-412`
  renders only explicit DTO fields without browser storage or logging.
- `LicenseManagePage.tsx:36-74,166-173` also consumes the contracted ADMIN detail
  read for canonical deep-link identity, publishes only the latest generation,
  and renders only ID, nickname, and email. No cross-user PII publication was
  found in that incidental path.

## Tests Inspected Or Run

- `npm test -- --run src/pages/admin/UserManagePage.test.tsx src/api/client.test.ts src/store/authStore.test.ts src/api/adminContracts.test.ts`
  from `frontend/`: PASS, 4 files and 55/55 tests.
- `npm test -- --run src/pages/admin/LicenseManagePage.test.tsx` from
  `frontend/`: PASS, 1 file and 1/1 test.
- `.\gradlew.bat test --tests "com.atstudio.atstudio.controller.UserControllerTest"`:
  PASS, 10/10 tests with 0 skipped, failures, or errors.
- The focused tests establish separate page, interceptor, session-generation,
  DTO-wrapper, authorization, and stale-detail behavior. They do not establish
  the combined exactly-once 403 path described in `PG-053-001`.
- The Evidence Pack's broader frontend and backend gate results were inspected
  but were not independently rerun by PG.

## Residual Risk

- No live browser session, network capture, production database, or deployed
  authorization behavior was inspected. This verdict is based on current
  source, uncommitted diff, and bounded automated tests.
- ADMIN User detail intentionally includes email and phone/profile fields. The
  review confirms the approved DTO and latest-request UI boundary, not a future
  product decision to further minimize that existing ADMIN contract.

## Related Documents

- [PG Review Handoff](WI-20260809-ATS-053-pg-handoff.md)
- [WI Handoff](WI-20260809-ATS-053-handoff.md)
- [Evidence Pack](WI-20260809-ATS-053-evidence-pack.md)
- [Security Policy](../../docs/policies/security-policy.md)
- [Access Control Policy](../../docs/policies/access-control-policy.md)
