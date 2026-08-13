# Remediation R3 Handoff: WI-20260809-ATS-050

[WI HEADER]

- WI ID: `WI-20260809-ATS-050-REMEDIATION-R3`
- REQ: `REQ-20260809-ATS-001` (`approved`)
- Agent: `se`
- Depends On: WI-050 final QA review
- Blocks: WI-050 conclusive QA, finalization, full gates, commit, and push

[WI SUMMARY]

## Why

Close the remaining `F-QA-INTEG-050-001`, new `F-QA-INTEG-050-006`, and P3 `-007` cross-layer schedules without changing auth/provider policy, schema, dependencies, Notice endpoints, or attachment policy.

## Required Direction

### Admin-shell mutation boundary

- Add a narrowly scoped Admin mutation boundary owned by the real `AdminLayout` and usable by child ADMIN pages.
- Acquiring the boundary must be synchronous before the Notice API invocation so same-tick Logout cannot start first.
- While any Notice mutation owner is active, disable Logout programmatically and make `handleLogout` perform zero logout/session/navigation effects even if invoked. Preserve existing sidebar route blocking from the page guard.
- Release the exact owner on every authoritative success/failure, ambiguous terminal outcome, and settled unmounted operation. Do not let one owner release another.
- Default behavior for isolated page tests must be safe, but add a real ProtectedRoute + AdminLayout + Notice integration schedule proving pending Logout has zero effects and becomes available after success, authoritative 4xx, and ambiguous settlement.
- Do not globally modify `authStore.logout` policy or other layouts.

### Same-user token rotation

- Notice edit read/mutation ownership must survive access-token A -> B rotation for the same authenticated ADMIN user. Token refresh is not a principal/target change.
- Retire work when user ID/role or Notice target changes, not merely because the access-token string rotates.
- An accepted update/delete operation must settle ref and React state together after replay: authoritative success navigates; authoritative 4xx permits retry; ambiguous response-loss enters observation-only recovery.
- Add integrated client/page schedules (or a faithful composed harness) for A -> wire 401 -> refresh -> B -> replay success and replay response-loss for update and delete. Record application mutation count, wire request/replay count, ADMIN GETs, navigation, pending/listener state.
- Preserve existing API interceptor behavior; do not add `skipAuthReplay` unless evidence proves replay is unsafe and requires a policy decision. Current required direction is to support same-user replay.

### Fence clear availability

- Make safe session-storage removal return success/failure without breaking existing callers.
- Clear the in-memory fence only when removal succeeds or no stored fence exists. On remove-only failure, remain fail-closed and permit a later successful list observation to retry clearing.
- Add remove-only failure/recovery tests.

[ACCEPTANCE CRITERIA]

- [ ] Pending Notice mutation + Logout produces zero logout/session/navigation side effects and one Notice mutation.
- [ ] Logout is available after each terminal settlement and existing normal logout behavior remains green.
- [ ] Same-user token rotation does not reset read/form ownership or deadlock mutation state.
- [ ] Update/delete replay success navigates exactly once; replay response-loss exposes observation recovery; app mutation remains one.
- [ ] Different user/role/target still retires stale work.
- [ ] Remove-only storage failure remains non-crashing/fail-closed and a later successful observation clears it.
- [ ] Focused/integration/adjacent tests, typecheck, full ESLint, changed Prettier, docs validation, and diff check pass.

[CONSTRAINTS]

- No schema/dependency/new endpoint/attachment policy/provider/security-policy change.
- No live browser mutation, DB/storage/file/download/external effect, secret/protected-output access, staging, commit, push, branch action, or deploy.
- Do not create final evidence/summary; finalization follows an independent PASS.

[INPUT POINTERS]

- `deliverables/agent/WI-20260809-ATS-050-qa-final-review-result.md`
- `deliverables/agent/WI-20260809-ATS-050-remediation-r2-handoff.md`
- `docs/standards/core-principles.md`
- `docs/standards/development-standards.md`
- `docs/policies/quality-gates.md`
- `frontend/src/layouts/AdminLayout.tsx`
- `frontend/src/router/ProtectedRoute.tsx`
- `frontend/src/store/authStore.ts`
- `frontend/src/api/client.ts`
- `frontend/src/pages/admin/NoticeCreatePage.tsx`
- `frontend/src/pages/admin/NoticeEditPage.tsx`
- `frontend/src/utils/noticeCreateObservationFence.ts`
- `frontend/src/utils/safeStorage.ts`
- Current WI-050 diff excluding `output/**`.

[OUTPUT CONTRACT]

- Implement directly in the shared workspace.
- Report every changed file, red/green exact results, and unresolved blockers.
