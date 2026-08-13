---
version: 1.0
last_updated: 2026-08-14
project: ATS
owner: ma
category: wi-review-handoff
status: active
dependencies:
  - path: WI-20260809-ATS-053-handoff.md
    reason: Approved scope and security/privacy acceptance criteria
  - path: WI-20260809-ATS-053-evidence-pack.md
    reason: Implementation and verification evidence under review
---

# PG Review Handoff: WI-20260809-ATS-053

## WI Header

- **WI ID:** `WI-20260809-ATS-053-PG`
- **REQ:** `REQ-20260809-ATS-001`
- **Agent:** `pg`
- **Depends On:** `WI-20260809-ATS-053` implementation
- **Blocks:** independent QA and WI closure

## Review Purpose

Independently verify that stale ADMIN session refresh and the new read-only User
detail surface preserve server authority and bounded PII. This is a review-only
gate: do not edit product files or silently remediate findings.

## Scope In

- `CR-031-094`: one-time current-user refresh after typed
  `ADMIN_ROLE_REQUIRED`/403 rejection, no rejected mutation retry, and canonical
  route-guard behavior after a demotion.
- `CR-031-095`: ADMIN-only User detail invocation, response field boundary,
  loading/error/retry/close ownership, and absence of credential or unintended
  PII exposure.
- Tests and docs that claim the authority/privacy behavior above.

## Scope Out

- Visual design, list request races, plan table content, and settings canonical
  save except where they create an incidental security/privacy defect.
- Product edits, schema/data, dependencies, real external effects, branch work,
  or deployment.

## Acceptance Criteria

- [ ] Refresh updates only the authenticated current-user snapshot and cannot
      grant authority the server did not return.
- [ ] A stale 403 causes exactly one refresh and zero mutation retries.
- [ ] Server ADMIN guards remain the source of authority for User list/detail/update.
- [ ] The detail UI invokes only the existing bounded DTO and does not expose or
      persist password, token, credential, or additional hidden fields.
- [ ] Detail request cancellation/generation prevents cross-user PII display.
- [ ] Tests materially establish the claims rather than only mirror mocks.
- [ ] Result classifies every issue P0-P3 and gives exact file/line evidence;
      PASS requires no open P0-P3 in scope.

## Input Pointers

### Tier 0

- `docs/standards/core-principles.md`

### Tier 1

- `docs/policies/security-policy.md`
- `docs/policies/access-control-policy.md`

### Tier 2 and Evidence

- `docs/design/api-spec.md`
- `docs/design/usecase/user-info.md`
- `deliverables/user/REQ-20260809-ATS-001.md`
- `deliverables/agent/WI-20260809-ATS-053-handoff.md`
- `deliverables/agent/WI-20260809-ATS-053-evidence-pack.md`
- `deliverables/agent/WI-20260809-ATS-028-findings.md:84-105`

### Primary Files

- `frontend/src/api/admin.ts`
- `frontend/src/pages/admin/UserManagePage.tsx`
- `frontend/src/pages/admin/UserManagePage.test.tsx`
- `frontend/src/store/authStore.ts`
- `frontend/src/router/ProtectedRoute.tsx`
- `src/main/java/com/atstudio/atstudio/controller/UserController.java`
- `src/main/java/com/atstudio/atstudio/dto/user/UserDetailResponse.java`
- `src/test/java/com/atstudio/atstudio/controller/UserControllerTest.java`

## Output Contract

- Write `deliverables/agent/WI-20260809-ATS-053-pg-result.md`.
- Include verdict, P0-P3 findings first, authority/PII evidence, tests inspected
  or run, residual risk, and exact remediation if FAIL.
- Do not modify product code, tests, or other WI documents.

## Constraints

- Do not inspect, open, hash, modify, stage, or delete protected output paths.
- Do not inspect ignored secrets/local environment values.
- Do not execute external effects or real data mutations.
- Review the current uncommitted implementation exactly as found.
