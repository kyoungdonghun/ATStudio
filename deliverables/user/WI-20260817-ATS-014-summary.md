---
version: 1.0
last_updated: 2026-08-17
project: ATS
owner: se
category: work-summary
status: complete
related_wi: WI-20260817-ATS-014
dependencies:
  - path: ../agent/WI-20260817-ATS-014-handoff.md
    reason: Approved React Router V6-to-V7 compatibility scope and quality gates
  - path: REQ-20260817-ATS-009.md
    reason: Approved dependency-audit remediation request
---

# WI-20260817-ATS-014 Summary

## Result

Upgraded the existing Vite React SPA from React Router 6.30.4 to the pinned
secure V7 release 7.18.2. The existing browser data router, route guards,
redirects, loaders, lazy routes, and navigation-blocking behavior remain in
place; this WI did not convert the application to framework mode or change any
backend, database, API, secret, or UI contract.

## Compatibility Outcome

- The selected V7.18.2 release is the audit-provided remediation version.
  Registry verification confirmed Node `>=20` and React/ReactDOM `>=18` peer
  requirements, which match the execution host (Node 24.14.0 and React 18.3.1).
- Removed V6-only `future` props. Their formerly opted-in behavior is standard
  V7 behavior, so `RouterProvider`, `MemoryRouter`, and test data routers now
  use their V7-compatible interfaces.
- The SSR-only test now imports `StaticRouter` from the V7-supported
  `react-router-dom` re-export instead of the removed `react-router-dom/server`
  subpath.
- Retired the unused V6 future-flag module. No active obsolete future property
  or removed router import remains under `frontend/src`.

## Audit And Verification

- Before: `npm audit --omit=dev` reported 2 moderate production React Router
  vulnerability records; its remediation was React Router DOM 7.18.2.
- After: `npm audit --omit=dev` reported 0 vulnerabilities.
- Passed: `npm run typecheck`, `npm run lint`, `npm run format`,
  `npm test -- --run` (111 files, 1,440 tests), `npm run build`, and
  `git diff --check`.

## Risk And Rollback

The project had unrelated uncommitted changes before this WI. They were not
altered, and the full-suite result reflects the current combined worktree.
Rollback is one atomic source/dependency revert: restore `frontend/package.json`,
`frontend/package-lock.json`, the retired future-flag module, and this WI's
narrow router/test compatibility hunks. No database or data rollback is needed.
