---
version: 1.0
last_updated: 2026-08-08
project: ATS
owner: pg
category: evidence-pack
status: confirmed
dependencies:
  - path: WI-20260808-ATS-003-handoff.md
    reason: Approved Work Item scope and output contract
  - path: ../user/REQ-20260808-ATS-002.md
    reason: Approved request and acceptance criteria
  - path: ../user/WI-20260808-ATS-003-summary.md
    reason: User-facing security and operational decision summary
---
# Evidence Pack: WI-20260808-ATS-003

## Summary (one-liner)

- Confirmed that the current ADMIN user-update path permits self-demotion and last-admin demotion, identified the concurrent cross-demotion and stale frontend-role risks, and defined the server invariant, serialization, session, audit, recovery, and test requirements for SR-96.

## Scope / DoD Check

- [x] Established with frontend, API, controller, service, entity, and repository evidence that `ADMIN -> USER` is currently allowed.
- [x] Analyzed self-demotion, last-admin demotion, and concurrent cross-demotion separately.
- [x] Distinguished backend request-time authority from the access-token role claim and the frontend's persisted role state.
- [x] Defined a backend-authoritative invariant and explained why UI-only checks, a count-only check, or target-row locking alone are insufficient.
- [x] Defined audit and production-recovery requirements without treating the non-production QA bootstrap as a recovery control.
- [x] Listed unit, controller, frontend, token/session, audit, and real-MySQL concurrency tests.
- [x] Kept facts, inferences, and proposals explicit.
- [x] Changed only this WI's user summary and Evidence Pack; product code, SR files, indexes, and DB were not changed.

## Reference Documents (Tier 0-2)

**Injected Context**:

| Tier | Document | Reason |
| --- | --- | --- |
| 0 | `docs/standards/core-principles.md` | Constitution, transparency, simplicity, and approved execution boundary |
| 0 | `docs/standards/development-standards.md` | Spring service transaction, repository, exception, and evidence standards |
| 1 | `docs/policies/security-policy.md` | Current JWT, localStorage, authorization, PII, and audit-data boundaries |
| 1 | `docs/policies/access-control-policy.md` | Least privilege, separation of duties, and default deny |
| 1 | `docs/policies/quality-gates.md` | High-impact security review and regression-evidence expectations |
| 2 | `docs/design/api-spec.md` | Current ADMIN user-update route and SecurityConfig authority |
| Context | `deliverables/user/REQ-20260808-ATS-002.md` | Approved three-SR scope and quality gates |
| Context | `deliverables/agent/WI-20260808-ATS-003-handoff.md` | WI-specific scope, constraints, pointers, and output contract |

**Injection Rules Applied**:

- Rule source: `.claude/config/context-injection-rules.json`
- Assignee: `pg`
- Task type: `security`, `review`, `operation`
- `agent_required_tiers`: `[0, 1]`
- Workspace tag source: `.claude/config/workspace.json` -> `ATS`

## Findings: Facts, Inferences, and Proposals

### Confirmed Facts

1. **The frontend offers both roles for every listed user.**
   - `frontend/src/pages/admin/UserManagePage.tsx:12` defines `USER` and `ADMIN` as the complete selector options.
   - `frontend/src/pages/admin/UserManagePage.tsx:150-169` renders the same selector for every row and does not compare the row to the signed-in user.
   - `frontend/src/pages/admin/UserManagePage.tsx:76-89` submits every changed role and maps any rejection to the page-level `Failed to update role` state.

2. **The API and controller authorize only the request's current ADMIN authority, not the actor-target relationship.**
   - `frontend/src/api/admin.ts:44-51` sends an optional `role` in `PUT /users/{userId}`.
   - `src/main/java/com/atstudio/atstudio/controller/UserController.java:94-101` protects the endpoint with `hasRole('ADMIN')`, but does not receive or pass `CustomUserDetails` to the service.
   - `src/main/java/com/atstudio/atstudio/config/SecurityConfig.java:97-102` also protects the wildcard PUT route as ADMIN-only.

3. **The service and entity accept an ADMIN-to-USER change without a guard.**
   - `src/main/java/com/atstudio/atstudio/service/UserService.java:262-267` loads the target through ordinary `findById`, invokes `updateByAdmin`, and returns the result.
   - `src/main/java/com/atstudio/atstudio/entity/User.java:98-101` assigns any non-null role directly.
   - `src/main/java/com/atstudio/atstudio/dto/user/UserAdminUpdateRequest.java:11-14` contains only optional `role` and `isVerified`; it has no actor, reason, expected version, or invariant field.

4. **A count helper and a target-row lock exist, but the role-change path uses neither.**
   - `src/main/java/com/atstudio/atstudio/repository/UserRepository.java:17-19` exposes `findByIdForUpdate` for a single user.
   - `src/main/java/com/atstudio/atstudio/repository/UserRepository.java:32-34` exposes active-user and active-role counts.
   - `rg -n "countByIsDeletedFalseAndRole" src/main/java src/test/java` returns only the repository declaration.
   - `rg -n "updateUserByAdmin\\(" src/main/java src/test/java` shows one controller caller and one happy-path service test; the service uses `findById`, not `findByIdForUpdate`.

5. **The database has no aggregate last-admin constraint or role-oriented serialization key.**
   - `src/main/resources/schema.sql:24-46` defines `users.role` as `ENUM('USER','ADMIN')`, unique nickname/email keys, and no constraint that preserves an ADMIN row.
   - The same block has no role/index pair that acts as an explicit role-change mutex.

6. **Backend authority uses the current DB role on each authenticated request.**
   - `src/main/java/com/atstudio/atstudio/security/JwtTokenProvider.java:27-36` embeds the issue-time role in the access token.
   - `src/main/java/com/atstudio/atstudio/security/JwtAuthenticationFilter.java:34-43` validates the token, extracts only the user ID, reloads `UserDetails`, and constructs authorities from that result.
   - `src/main/java/com/atstudio/atstudio/security/CustomUserDetailsService.java:27-33` reloads the user from the repository.
   - `src/main/java/com/atstudio/atstudio/security/CustomUserDetails.java:27-30,44-52` derives `ROLE_*` from the reloaded entity role.
   - Therefore, an access token issued while ADMIN does not retain ADMIN authorization after the DB row becomes USER.

7. **Refresh uses the current DB role, but frontend role state is not refreshed with the token.**
   - `src/main/java/com/atstudio/atstudio/service/auth/AuthService.java:76-109` locks the user, validates the stored Refresh Token hash, and issues the new access token with `user.getRole()`.
   - `frontend/src/store/authStore.ts:8-20,34-37` initializes the frontend role from the persisted user object.
   - `frontend/src/router/ProtectedRoute.tsx:28-40` gates routes from that cached role.
   - `frontend/src/api/client.ts:95-106` replaces the access and Refresh Tokens but updates only `accessToken`, not `user` or `role`.
   - A server-side demotion can therefore leave a stale ADMIN route state in the SPA until profile/session state is explicitly synchronized.

8. **Current automated tests do not exercise the invariant.**
   - `src/test/java/com/atstudio/atstudio/service/UserServiceTest.java:659-675` has one successful role/isVerified update test only.
   - `src/test/java/com/atstudio/atstudio/controller/UserControllerTest.java:88-120` checks unauthenticated, USER, and ADMIN route access, but not self-demotion, last-admin behavior, or actor propagation.
   - `frontend/src/pages/admin/UserManagePage.test.tsx:58-95` covers stale list-response fencing only, not role-change restrictions or error UX.

9. **No dedicated role-change audit or production break-glass procedure was identified.**
   - Focused repository search returned: `NO_MATCH: no dedicated role-change audit, last-admin policy, or break-glass runbook found in searched paths.`
   - `src/main/java/com/atstudio/atstudio/bootstrap/TestUserBootstrapRunner.java:39-69` can create/reset a QA ADMIN only when the test-user bootstrap flag is enabled.
   - `src/main/java/com/atstudio/atstudio/config/AcceptanceStartupGuard.java:54-65` forbids that bootstrap in production profiles and requires an explicit non-production profile.
   - The QA bootstrap is therefore not an authorized production recovery mechanism.

### Security and Operational Inferences

- **Self-demotion is reachable:** the signed-in ADMIN appears in the same list and no UI or server comparison blocks targeting its own ID.
- **Zero-admin state is reachable:** when the only active ADMIN is changed to USER, no code-level or schema-level invariant prevents the commit.
- **A count-only implementation is race-prone:** with two ADMIN rows, two transactions can each observe a count of two and demote different targets.
- **A target-row lock alone is insufficient:** cross-demotions lock different target rows, so they need not serialize on the same resource.
- **Authorization-before-wait can become stale:** a request admitted by Spring Security may wait behind another role transaction. The service must re-read and revalidate the actor after acquiring the shared role-change lock; otherwise a demoted actor may finish an already admitted privileged operation.
- **Current backend privilege revocation is stronger than the UI suggests:** current DB-role reload prevents continued ADMIN authorization, while the SPA can remain visually ADMIN. This is a consistency and operator-feedback defect, not proof of continued backend ADMIN access.
- **Impact:** accidental or concurrent role changes can remove the application's administrative control plane and force an out-of-band database intervention. This supports a HIGH/P1 operational authorization-severity recommendation even though exploitation already requires ADMIN authority.

### Proposed SR-96 Requirements

1. **Policy**
   - Reject self `ADMIN -> USER` demotion at the backend.
   - Allow another-admin demotion only when the actor is still a non-deleted ADMIN and at least one non-deleted ADMIN remains after the change.
   - Treat one remaining ADMIN as the hard data invariant; separately recommend two operationally usable ADMIN accounts for recovery redundancy.

2. **Transaction and concurrency**
   - Introduce one shared serialization point for all role mutations before locking actor/target rows.
   - Acceptable design candidates are a dedicated singleton governance/guard row locked with `PESSIMISTIC_WRITE`, or a deterministic lock of the full active-ADMIN set backed by a reviewed query/index contract.
   - Under the shared lock, reload and validate the actor, reload the target, evaluate the post-change ADMIN count, apply the mutation, clear session capability according to policy, and persist the success audit in one transaction.
   - Do not accept only a frontend check, only `countByIsDeletedFalseAndRole`, or only `findByIdForUpdate(targetId)` as closure.

3. **Error and UI contract**
   - Add explicit machine-readable errors such as `SELF_ADMIN_DEMOTION_FORBIDDEN` and `LAST_ADMIN_REQUIRED` rather than collapsing the result into a generic page failure.
   - Keep the user list and modal mounted and show the conflict beside the affected action.
   - Disable self-demotion in the UI with an explanation, while preserving the backend as the authoritative control.
   - Re-fetch `/users/me` and re-evaluate routes when the current user's server role changes or an ADMIN API returns role-loss `403`.

4. **Session policy**
   - Preserve the existing request-time DB-role authorization as the authoritative control.
   - Prefer clearing the demoted user's stored Refresh Token and requiring reauthentication so browser identity state and backend authority converge.
   - Explicitly test and document whether the existing access token remains usable only as USER until expiry or whether a future token/session-version mechanism terminates it completely.

5. **Audit and privacy**
   - Persist success evidence with actor ID, target ID, before/after role, result, reason code, and server timestamp.
   - Emit denied attempts through a non-rollback security-event path with the same minimal identifiers.
   - Do not record email, phone, password, access/Refresh Token, raw request bodies, or profile snapshots.
   - This aligns with the OWASP Logging Cheat Sheet's treatment of privilege changes and administrator actions as security-relevant events.

6. **Recovery**
   - Define a production-only out-of-band recovery runbook; do not expose a public recovery endpoint and do not enable the QA bootstrap in production.
   - Require a named incident/change ticket, two-person authorization, pre-change backup/snapshot, exact before/after ADMIN counts, a bounded promotion of one existing verified operator account, audit evidence, credential/session rotation, and post-recovery access verification.
   - Decide separately whether to maintain a sealed recovery ADMIN account or an offline promotion command. Either option needs owner, credential custody, periodic verification, and revocation rules.

## Required Test Matrix

| Layer | Scenario | Required result |
| --- | --- | --- |
| Service unit | One ADMIN self-demotes | Explicit invariant error; no mutation; denied audit event |
| Service unit | Another actor demotes the last ADMIN | Explicit invariant error; no mutation |
| Service unit | At least two ADMINs; one demotes another | Success; final eligible ADMIN count remains at least one |
| Service unit | Actor was demoted while request waited | Service-side actor recheck returns forbidden; no privileged mutation |
| Service unit | Deleted ADMIN exists | Deleted row does not satisfy the availability invariant |
| Service unit | USER promotion, unchanged role, isVerified-only update | Existing supported behavior remains explicit and regression-tested |
| Controller/security | Unauthenticated/USER/ADMIN callers | `401`/`403`/authorized as specified; actor ID reaches the service |
| MySQL concurrency | Two ADMINs concurrently demote each other | At most one demotion commits; final active ADMIN count is at least one |
| MySQL concurrency | Multiple different-target role changes | Shared lock serializes decisions without silent invariant loss; deadlock handling is verified |
| Session | Existing access token after demotion | Next ADMIN request is `403`; any permitted USER behavior matches policy |
| Session | Refresh after demotion | Rejected if refresh is revoked, otherwise issued only with current USER role according to the approved policy |
| Frontend | Self row and last-admin conflict | Self option is disabled/explained; server conflict stays in modal/row; list remains visible |
| Frontend | Current session demoted by another ADMIN | Profile/role is refreshed and ADMIN route exits cleanly |
| Audit | Success and denied attempts | Actor/target/before/after/result/reason/time present; prohibited secrets/PII absent |
| Recovery drill | No usable ADMIN in a disposable copy | Approved procedure restores one ADMIN, records evidence, and verifies login/API access |

## External Official References

- OWASP Authorization Cheat Sheet: `https://cheatsheetseries.owasp.org/cheatsheets/Authorization_Cheat_Sheet.html`
  - Supports default deny, server-side permission validation on every request, appropriate logging, and authorization test coverage.
- OWASP Logging Cheat Sheet: `https://cheatsheetseries.owasp.org/cheatsheets/Logging_Cheat_Sheet.html`
  - Identifies user privilege changes and administrator actions as security-relevant audit events and recommends recording when, where, who, what, result, and reason without logging tokens or unnecessary PII.
- OWASP Business Logic Security Cheat Sheet: `https://cheatsheetseries.owasp.org/cheatsheets/Business_Logic_Security_Cheat_Sheet.html`
  - Supports rechecking current authorization and business rules on the server rather than trusting UI controls or previously observed state.

## Commands & Outputs

| Command | Result |
| --- | --- |
| `rg -n -C 5 "updateUserByAdmin|updateByAdmin|countByIsDeletedFalseAndRole|findByIdForUpdate|updateUserAdmin" frontend/src src/main/java src/test/java` | Located the complete frontend-to-entity role mutation path and the unused count/lock helpers |
| Numbered reads of `UserManagePage.tsx`, `admin.ts`, `UserController.java`, `UserService.java`, `User.java`, `UserRepository.java`, and role tests | Confirmed no self, last-admin, actor, locking, audit, or recovery guard in the mutation path |
| Numbered reads of JWT filter/provider/details, `AuthService`, `authStore.ts`, `ProtectedRoute.tsx`, and `client.ts` | Confirmed current DB-role backend authority and stale frontend-role risk |
| `rg -n "countByIsDeletedFalseAndRole" src/main/java src/test/java` | Only the repository declaration matched; no guard uses it |
| `rg -n "updateUserByAdmin\\(" src/main/java src/test/java` | One production caller and one happy-path service test; no invariant tests |
| Focused search for `UserRoleChange|AdminRole|ROLE_CHANGE|last admin|break-glass` | No dedicated role-change audit, last-admin policy, or production break-glass runbook found |
| Read `schema.sql:24-46` | Role ENUM exists; no aggregate ADMIN-preservation constraint or shared role-change guard |
| Official OWASP search/read | Authorization, logging, and business-logic controls cross-checked on 2026-08-08 |

## Tests

- No product test suite was run because this WI was an approved read-only investigation and no product code changed.
- No live role mutation or DB write was performed.
- Required future tests are specified in the matrix above, including real MySQL concurrency proof; mock-only tests cannot close the race condition.

## Risks / Rollback

### Risks

- The exact serialization mechanism is an architecture decision. A lock-all-admin query can have database/index and lock-order implications; a dedicated guard row adds schema and operational ownership. SR-96 should require the invariant and proof without prematurely choosing the implementation.
- Clearing only the Refresh Token does not invalidate an already issued stateless access token. Current DB-role reload still removes ADMIN authority, but full session termination would require an explicit token/session-version design or acceptance of access-until-expiry as USER.
- Counting only `isDeleted=false AND role=ADMIN` matches current executable-role logic. If future policy requires verified, enabled, credential-recoverable, or tenant-scoped administrators, the eligibility predicate must evolve explicitly.
- A persistent break-glass account creates standing privilege risk; an offline recovery command creates process and credential-custody risk. Either requires a separately approved operational decision.

### Rollback

- Documentation-only output: remove only `deliverables/user/WI-20260808-ATS-003-summary.md` and `deliverables/agent/WI-20260808-ATS-003-evidence-pack.md` if this investigation is abandoned.
- No application, database, SR, index, runtime, or external state needs rollback.

## Follow-ups

- `WI-20260808-ATS-006` should consume this Evidence Pack when drafting `docs/SR/SR-96.md`.
- A later approved implementation REQ should assign architecture, backend, frontend, reliability, security-review, and production-runbook ownership separately.
- The implementation cannot be accepted without actual MySQL cross-demotion concurrency evidence.

## Related Documents

- [WI-003 Handoff](WI-20260808-ATS-003-handoff.md)
- [WI-003 User Summary](../user/WI-20260808-ATS-003-summary.md)
- [Approved REQ](../user/REQ-20260808-ATS-002.md)
