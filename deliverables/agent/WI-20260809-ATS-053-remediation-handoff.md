---
version: 1.0
last_updated: 2026-08-14
project: ATS
owner: ma
category: wi-remediation-handoff
status: active
dependencies:
  - path: WI-20260809-ATS-053-pg-result.md
    reason: Independent PG P2 finding requiring closure
  - path: WI-20260809-ATS-053-handoff.md
    reason: Original scope and exactly-once authority-refresh acceptance criterion
---

# Remediation Handoff: WI-20260809-ATS-053

## Assignment

- **Agent:** `se`
- **Finding:** `PG-053-001` P2
- **Purpose:** make the typed stale-ADMIN role mutation have one refresh owner.

## Required Change

- Add an explicit Axios request option such as `skipAdminRoleSync` and honor it
  only in the centralized ADMIN 403 synchronization branch.
- Set the option on `updateUserAdmin` so the page-owned typed
  `ADMIN_ROLE_REQUIRED`/403 refresh remains the single owner and can report its
  success/failure accurately.
- Preserve centralized ADMIN 403 synchronization for every request that does
  not explicitly opt out.
- Preserve all 401 replay, `skipAuthReplay`, auth exclusion, coalescing, and
  original-error behavior.
- Add an integration-level regression through the real response interceptor,
  auth store, mutation wrapper, page, and route guard proving one PUT, one
  `/users/me` GET, zero PUT replays, server-returned `USER`, and redirect.
- Update API/frontend security documentation and WI evidence/summary only as
  needed to describe the explicit ownership boundary and PG remediation.

## Acceptance Criteria

- [ ] Typed stale-ADMIN role mutation: one PUT, one identity refresh, zero PUT retries.
- [ ] Generic ADMIN 403 requests still use centralized role synchronization.
- [ ] Non-403 and untyped/other-code failures do not trigger page-owned refresh.
- [ ] Demoted server profile becomes canonical and the ADMIN route guard redirects.
- [ ] Focused integration/unit tests, typecheck, ESLint, Prettier, docs validation,
      and diff check pass.
- [ ] No schema/data/dependency/policy/external-effect change.

## Input Pointers

### Tier 0

- `docs/standards/core-principles.md`
- `docs/standards/development-standards.md`

### Tier 1

- `docs/policies/security-policy.md`
- `docs/policies/access-control-policy.md`
- `docs/policies/quality-gates.md`

### Evidence and Primary Files

- `deliverables/agent/WI-20260809-ATS-053-pg-result.md`
- `deliverables/agent/WI-20260809-ATS-053-evidence-pack.md`
- `frontend/src/api/client.ts`
- `frontend/src/api/client.test.ts`
- `frontend/src/api/admin.ts`
- `frontend/src/pages/admin/UserManagePage.tsx`
- `frontend/src/pages/admin/UserManagePage.test.tsx`
- `frontend/src/store/authStore.ts`
- `frontend/src/router/ProtectedRoute.tsx`

## Output Contract

- Edit the current implementation/tests/docs in place; do not commit or push.
- Append or revise the existing Evidence Pack and user summary to record the
  initial PG FAIL and remediation transparently.
- Do not create the PG re-review result; that remains reviewer-owned.

## Constraints

- Do not inspect, open, hash, modify, stage, or delete protected output paths.
- Do not inspect ignored secrets/local environment values.
- No external effects, schema/data, dependencies, product policy, branch, or
  deployment changes.
