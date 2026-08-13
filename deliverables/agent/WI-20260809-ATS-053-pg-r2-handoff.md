---
version: 1.0
last_updated: 2026-08-14
project: ATS
owner: ma
category: wi-review-handoff
status: active
dependencies:
  - path: WI-20260809-ATS-053-pg-result.md
    reason: Initial PG FAIL and PG-053-001 evidence
  - path: WI-20260809-ATS-053-remediation-handoff.md
    reason: Required remediation contract
---

# PG Re-review Handoff: WI-20260809-ATS-053 R2

## Assignment

- **Agent:** `pg`
- **Purpose:** independently verify closure of `PG-053-001` and re-audit the
  original session/authority/PII boundary after remediation.
- **Mode:** review-only; do not edit product code or tests.

## Acceptance Criteria

- [ ] `updateUserAdmin` has one explicit refresh owner for typed stale-ADMIN 403.
- [ ] Integrated production path performs one PUT, one `/users/me` GET, zero PUT
      replays, persists only the server-returned profile, and redirects a demoted role.
- [ ] Generic ADMIN 403 synchronization remains active unless explicitly opted out.
- [ ] `skipAdminRoleSync` does not suppress or alter 401 refresh/replay behavior.
- [ ] Original User detail PII/request-ownership review remains clean.
- [ ] PASS requires no open P0-P3 issue in the assigned scope.

## Input Pointers

### Tier 0/1

- `docs/standards/core-principles.md`
- `docs/policies/security-policy.md`
- `docs/policies/access-control-policy.md`

### Evidence and Files

- `deliverables/agent/WI-20260809-ATS-053-pg-result.md`
- `deliverables/agent/WI-20260809-ATS-053-remediation-handoff.md`
- `deliverables/agent/WI-20260809-ATS-053-evidence-pack.md`
- `frontend/src/api/client.ts`
- `frontend/src/api/client.test.ts`
- `frontend/src/api/admin.ts`
- `frontend/src/api/adminContracts.test.ts`
- `frontend/src/pages/admin/UserManagePage.tsx`
- `frontend/src/pages/admin/UserManagePage.integration.test.tsx`
- `frontend/src/store/authStore.ts`
- `frontend/src/router/ProtectedRoute.tsx`

## Output Contract

- Write only `deliverables/agent/WI-20260809-ATS-053-pg-r2-result.md`.
- Findings first with P0-P3; state explicit closure or persistence of
  `PG-053-001`, verdict, tests, and residual risk.

## Constraints

- Do not inspect, open, hash, modify, stage, or delete protected output paths.
- Do not inspect ignored secrets/local environment values.
- No external effects, product edits, schema/data, dependencies, branches, or deployment.
