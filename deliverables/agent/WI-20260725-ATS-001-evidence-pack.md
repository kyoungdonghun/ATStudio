---
version: 1.0
last_updated: 2026-07-25
project: ATS
owner: se
category: evidence-pack
status: confirmed
dependencies:
  - path: WI-20260725-ATS-001-handoff.md
    reason: Approved Work Item scope and constraints
  - path: ../user/REQ-20260725-ATS-001.md
    reason: Approved Request and acceptance criteria
---

# Evidence Pack: WI-20260725-ATS-001

## Summary

- Added a component-local, StrictMode-safe email verification request guard and
  a focused regression assertion that permits exactly one request.

## Scope / DoD Check

- [x] A token-bearing `EmailVerifyPage` rendered under StrictMode calls
      `verifyEmail` exactly once.
- [x] The first successful response remains in the success UI state.
- [x] Missing-token and provider-rejected behavior remains covered.
- [x] React StrictMode remains enabled.
- [x] No global token cache or retained cross-page state was introduced.
- [x] No backend, SMTP, runtime secret, router, mail-template, or unrelated UI
      file was changed.
- [x] Focused Vitest, changed-file Prettier/ESLint, and TypeScript typecheck
      passed.

## Reference Documents

| Tier    | Document                                        | Reason                                               |
| ------- | ----------------------------------------------- | ---------------------------------------------------- |
| 0       | `docs/standards/core-principles.md`             | Constitution and approved execution boundary         |
| 0       | `docs/standards/development-standards.md`       | Implementation and regression-test standards         |
| 0       | `docs/standards/documentation-standards.md`     | Deliverable structure and language rules             |
| 0       | `docs/standards/glossary.md`                    | Canonical terms                                      |
| 1       | `docs/policies/security-policy.md`              | Token, PII, and secret-handling boundary             |
| 1       | `docs/policies/quality-gates.md`                | Regression and traceability gates                    |
| 2       | `docs/standards/frontend-standards.md`          | React effect and local-state conventions             |
| 2       | `.agents/skills/react-best-practices/AGENTS.md` | Effect dependency and ref guidance                   |
| Context | `deliverables/user/REQ-20260725-ATS-001.md`     | Approved scope and quality gates                     |
| Context | `docs/design/db-schema.md`                      | Single-use email verification persistence boundary   |
| Code    | `frontend/src/main.tsx`                         | StrictMode remains enabled                           |
| Code    | `frontend/src/api/auth.ts`                      | Existing verification API contract remains unchanged |

## Evidence Pointers

### Implementation

- `frontend/src/pages/auth/EmailVerifyPage.tsx:1`
  - Imports `useRef` for lifecycle-local request state.
- `frontend/src/pages/auth/EmailVerifyPage.tsx:14-19`
  - Rejects missing-token and duplicate-effect execution before calling the API,
    then locks the guard before the promise starts.
- Patch rationale:
  - React StrictMode replays the committed effect during development.
  - A component-local boolean ref survives that replay without retaining the
    raw token or suppressing verification across page lifecycles.
  - Locking before `verifyEmail` starts prevents both success and rejection
    paths from issuing a second single-use-token request.

### Regression Test

- `frontend/src/test/coverage/publicAuthShell.coverage.test.tsx:2`
  - Imports `StrictMode` from React.
- `frontend/src/test/coverage/publicAuthShell.coverage.test.tsx:429-443`
  - Keeps the missing-token assertion, renders the success path under
    StrictMode, and asserts one call with the test token.
- `frontend/src/test/coverage/publicAuthShell.coverage.test.tsx:448-456`
  - Retains provider-rejection coverage.

### Preserved State

- Pre-edit branch: `codex/v1-release-rehearsal-fixes`.
- Pre-edit tracked and staged diff: empty.
- Preserved pre-existing untracked files:
  - `deliverables/agent/WI-20260725-ATS-001-handoff.md`
  - `deliverables/user/REQ-20260725-ATS-001.md`
  - `output/client-demo-screenshots-20260716-140514.zip`

## Commands and Results

| Command                                                                                                                                                                                                                      | Result                                                                                  |
| ---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- | --------------------------------------------------------------------------------------- |
| `git status --short --branch`                                                                                                                                                                                                | PASS; current branch confirmed and three pre-existing untracked files identified        |
| `git diff --no-ext-diff --stat; git diff --no-ext-diff; git diff --no-ext-diff --cached --stat; git diff --no-ext-diff --cached`                                                                                             | PASS; no tracked or staged pre-edit diff                                                |
| `npm test -- src/test/coverage/publicAuthShell.coverage.test.tsx -t "handles missing links and verifies successfully once under StrictMode"` before implementation                                                           | Expected RED; 1 failed, 27 skipped; `verifyEmail` called twice                          |
| Same focused command after implementation                                                                                                                                                                                    | PASS; 1 passed, 27 skipped                                                              |
| `npm test -- src/test/coverage/publicAuthShell.coverage.test.tsx`                                                                                                                                                            | PASS; 28 passed                                                                         |
| `npx prettier --check src/pages/auth/EmailVerifyPage.tsx src/test/coverage/publicAuthShell.coverage.test.tsx`                                                                                                                | PASS; all matched files use Prettier style                                              |
| `npx eslint src/pages/auth/EmailVerifyPage.tsx src/test/coverage/publicAuthShell.coverage.test.tsx --max-warnings 0`                                                                                                         | PASS; zero warnings/errors                                                              |
| `npm run typecheck`                                                                                                                                                                                                          | PASS; `tsc --noEmit` exited 0                                                           |
| `npx prettier --check src/pages/auth/EmailVerifyPage.tsx src/test/coverage/publicAuthShell.coverage.test.tsx ../deliverables/user/WI-20260725-ATS-001-summary.md ../deliverables/agent/WI-20260725-ATS-001-evidence-pack.md` | PASS after formatting the Evidence Pack; all four WI-changed files match Prettier style |

## Reproduction

```powershell
Set-Location frontend
npm test -- src/test/coverage/publicAuthShell.coverage.test.tsx
npx prettier --check src/pages/auth/EmailVerifyPage.tsx src/test/coverage/publicAuthShell.coverage.test.tsx
npx eslint src/pages/auth/EmailVerifyPage.tsx src/test/coverage/publicAuthShell.coverage.test.tsx --max-warnings 0
npm run typecheck
```

## Risks / Rollback

### Risks

- Automated coverage does not replace real Gmail and acceptance-environment
  verification.
- The boolean guard intentionally limits one mounted page lifecycle to one
  verification attempt. Opening another verification link should create a
  fresh page lifecycle.

### Rollback

- Revert only the `useRef` import, `verificationStartedRef` declaration, guard
  condition, and pre-request assignment in
  `frontend/src/pages/auth/EmailVerifyPage.tsx`.
- Revert only the StrictMode import/wrapper and one-call assertion in
  `frontend/src/test/coverage/publicAuthShell.coverage.test.tsx`.
- Remove this WI summary and Evidence Pack only if the corrective patch is
  intentionally abandoned.
- Do not revert the pre-existing REQ, handoff, or demo ZIP.

## Follow-up

- WI-20260725-ATS-002 is unblocked for frontend quality-gate confirmation and
  secret-safe real Gmail link verification.

## Related Documents

- [WI-001 Handoff](WI-20260725-ATS-001-handoff.md)
- [WI-001 User Summary](../user/WI-20260725-ATS-001-summary.md)
- [Approved REQ](../user/REQ-20260725-ATS-001.md)
