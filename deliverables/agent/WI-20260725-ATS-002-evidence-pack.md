---
version: 1.0
last_updated: 2026-07-25
project: ATS
owner: qa-fe
category: evidence-pack
status: confirmed
dependencies:
  - path: WI-20260725-ATS-002-handoff.md
    reason: Approved Work Item scope and constraints
  - path: WI-20260725-ATS-001-evidence-pack.md
    reason: Implementation evidence under independent review
  - path: ../user/REQ-20260725-ATS-001.md
    reason: Approved Request and acceptance criteria
---

# Evidence Pack: WI-20260725-ATS-002

## Summary

- Independently reviewed the WI-001 StrictMode correction and passed every
  required frontend quality gate without changing product code.
- Recorded the completed real Gmail human gate as PASS from sanitized
  acceptance facts. The REQ DoD is satisfied.

## Scope / DoD Check

- [x] Confirmed one verification request per mounted page lifecycle under
      StrictMode.
- [x] Confirmed missing-token, success, and server-rejection coverage remains.
- [x] Confirmed no global cache, polling, timer, or retained cross-route token
      state was introduced.
- [x] Confirmed no backend, API contract, single-use-token policy, StrictMode,
      or SMTP configuration change is present.
- [x] Passed the complete frontend Vitest suite.
- [x] Passed TypeScript typecheck.
- [x] Passed ESLint with zero warnings.
- [x] Passed the complete frontend Prettier check.
- [x] Passed the production build.
- [x] Passed `git diff --check`.
- [x] Preserved all WI-001 changes and the untracked screenshot ZIP.
- [x] Confirmed the real Gmail human gate passed without a duplicate
      invalid-token failure.
- [x] Confirmed the second signup, mail outcome, persistent success UI, admin
      verification state, and post-threshold exception count.
- [x] Excluded all Gmail addresses, aliases, tokens, passwords, public token
      URLs, and credentials from the evidence.

## Reference Documents

| Tier    | Document                                                       | Reason                                            |
| ------- | -------------------------------------------------------------- | ------------------------------------------------- |
| 0       | `docs/standards/core-principles.md`                            | Constitution and approved execution boundary      |
| 0       | `docs/standards/development-standards.md`                      | Review, testing, and evidence standards           |
| 0       | `docs/standards/documentation-standards.md`                    | Deliverable structure and language rules          |
| 0       | `docs/standards/glossary.md`                                   | Canonical terms                                   |
| 1       | `docs/policies/security-policy.md`                             | Token, PII, SMTP, and log-safety boundary         |
| 1       | `docs/policies/quality-gates.md`                               | Frontend gate and traceability requirements       |
| 2       | `docs/standards/frontend-standards.md`                         | React, TypeScript, lint, and formatting standards |
| 2       | `.agents/skills/react-best-practices/AGENTS.md`                | Effect and component-local ref review guidance    |
| Context | `deliverables/user/REQ-20260725-ATS-001.md`                    | Approved scope and acceptance criteria            |
| Context | `deliverables/agent/WI-20260725-ATS-001-handoff.md`            | WI-001 implementation contract                    |
| Context | `deliverables/agent/WI-20260725-ATS-001-evidence-pack.md`      | WI-001 implementation and focused evidence        |
| Code    | `frontend/src/main.tsx`                                        | Active StrictMode boundary                        |
| Code    | `frontend/src/pages/auth/EmailVerifyPage.tsx`                  | Corrective implementation                         |
| Code    | `frontend/src/test/coverage/publicAuthShell.coverage.test.tsx` | Regression and state coverage                     |
| Code    | `frontend/src/api/auth.ts`                                     | Unchanged verification API contract               |

## Evidence Pointers

### Independent Diff Review

#### Diff Boundary

- Pre-deliverable `git diff --name-only` listed only:
  - `frontend/src/pages/auth/EmailVerifyPage.tsx`
  - `frontend/src/test/coverage/publicAuthShell.coverage.test.tsx`
- Product diff aggregate:
  - `EmailVerifyPage.tsx`: 4 additions, 2 deletions.
  - `publicAuthShell.coverage.test.tsx`: 9 additions, 3 deletions.
  - Total: 13 additions, 5 deletions across 2 product/test files.
- No backend, router, API, SMTP, or runtime configuration file was changed.

#### Implementation Findings

- `frontend/src/main.tsx:1-9`
  - StrictMode remains the application root boundary.
- `frontend/src/pages/auth/EmailVerifyPage.tsx:14-20`
  - `verificationStartedRef` is component-local and stores only a boolean.
  - The missing-token and duplicate-effect guards execute before the API call.
  - The ref is locked before the promise starts, covering both resolve and
    reject paths during StrictMode effect replay.
- `frontend/src/api/auth.ts:171-174`
  - The existing `GET /api/auth/verify-email` contract is unchanged.
- No concrete defect was found. Per the handoff constraint, WI-002 made no
  product-code edits.

#### Coverage Findings

- `frontend/src/test/coverage/publicAuthShell.coverage.test.tsx:429-443`
  - Covers the missing-token state, renders the success path under StrictMode,
    and asserts exactly one `verifyEmail` call with the test token.
- `frontend/src/test/coverage/publicAuthShell.coverage.test.tsx:448-456`
  - Retains server-rejection reason and failure-state coverage.
- Full-suite execution confirms these tests coexist with the complete frontend
  baseline.

### Human Gate Evidence

- Evidence source: sanitized acceptance facts supplied by the MA/user on
  2026-07-25. Sensitive raw artifacts were intentionally not included or
  re-read.
- Second real Gmail signup: HTTP 201.
- Secret-free mail outcome: `SUCCESS`.
- Human interaction: the newest mail was opened.
- Persistent UI result: `인증 완료 / 이메일 인증이 완료되었습니다`.
- Admin API lookup for the test nickname: `Found=true`,
  `IsVerified=true`.
- Runtime `BusinessException` events after `2026-07-25T14:30:00`: 0.
- Result: no duplicate invalid-token failure occurred during the live retest.

## Commands and Results

All npm commands ran from `frontend/`. Git commands ran from the repository
root.

| Command                                                                                                                                      | Exit | Exact result                                                                               |
| -------------------------------------------------------------------------------------------------------------------------------------------- | ---- | ------------------------------------------------------------------------------------------ |
| `npm test`                                                                                                                                   | 0    | 63/63 test files passed; 468/468 tests passed; 0 failed; 0 skipped; Vitest duration 25.30s |
| `npm test -- src/test/coverage/publicAuthShell.coverage.test.tsx -t "handles missing links and verifies successfully once under StrictMode"` | 0    | 1/1 file passed; 1 passed; 27 skipped; 28 tests in the selected file; duration 2.72s       |
| `npm run typecheck`                                                                                                                          | 0    | `tsc --noEmit`; 0 diagnostics                                                              |
| `npm run lint`                                                                                                                               | 0    | `eslint src --ext .ts,.tsx --max-warnings 0`; 0 errors; 0 warnings                         |
| `npm run format`                                                                                                                             | 0    | `prettier --check . --ignore-unknown`; all matched files use Prettier style                |
| `npm run build`                                                                                                                              | 0    | `tsc -b && vite build`; 266 modules transformed; Vite build completed in 2.37s             |
| `git diff --check`                                                                                                                           | 0    | No output; 0 whitespace errors                                                             |

## Aggregate Counts

- Required quality gates: 6 passed / 6 total.
- Complete Vitest suite: 63 passed files / 63 total.
- Complete Vitest tests: 468 passed / 468 total; 0 failed; 0 skipped.
- Focused supporting run: 1 passed; 27 skipped; 0 failed.
- Static diagnostics: 0 TypeScript diagnostics; 0 ESLint errors; 0 ESLint
  warnings; 0 Prettier mismatches reported.
- Build: 266 modules transformed; 0 build errors.
- Diff integrity: 0 whitespace errors.
- Real Gmail human gate: 1 passed / 1 completed live retest.
- Post-threshold duplicate-failure signal: 0 `BusinessException` events.
- REQ DoD: satisfied.

## Preserved State and Security Boundary

- Branch: `codex/v1-release-rehearsal-fixes`.
- Existing WI-001 product changes remain present and unchanged.
- Existing WI-001/REQ/handoff deliverables remain present.
- `output/client-demo-screenshots-20260716-140514.zip` remains untracked and
  present at 700,703 bytes.
- No runtime SMTP file was read.
- No Gmail address, verification token, credential, or secret was read,
  printed, logged, or written to either deliverable.
- The qa-fe automated verification did not send email, consume a real token,
  inspect runtime SMTP files, or change an acceptance-environment process.
- The subsequent human gate is recorded only through the supplied sanitized
  facts; raw sensitive acceptance artifacts remain excluded.

## Risks / Rollback

### Residual Risks

- The completed human gate is one live acceptance sample. Future runtime,
  provider, or deployment changes require fresh acceptance evidence.
- The boolean ref intentionally permits one attempt per mounted page lifecycle.
  A different link should be opened in a fresh page lifecycle.
- Sanitized evidence preserves privacy but intentionally omits raw
  reproduction artifacts.

### Rollback

- WI-002 changed only:
  - `deliverables/user/WI-20260725-ATS-002-summary.md`
  - `deliverables/agent/WI-20260725-ATS-002-evidence-pack.md`
- These two verification documents can be removed independently if WI-002 is
  abandoned.
- The WI-001 frontend patch can be reverted independently by reverting only its
  component and regression-test edits. No SMTP or runtime configuration rollback
  is coupled to that patch.
- Preserve all pre-existing deliverables and
  `output/client-demo-screenshots-20260716-140514.zip`.

## Human Gate Result

- **Status: PASS.**
- Second real Gmail signup: HTTP 201.
- Secret-free mail outcome: `SUCCESS`.
- Newest-mail click result remained
  `인증 완료 / 이메일 인증이 완료되었습니다`.
- Admin API lookup: `Found=true`, `IsVerified=true`.
- Runtime `BusinessException` events after `2026-07-25T14:30:00`: 0.
- Duplicate invalid-token failure during the retest: not observed.
- **REQ DoD: satisfied.**

## Related Documents

- [WI-002 Handoff](WI-20260725-ATS-002-handoff.md)
- [WI-002 User Summary](../user/WI-20260725-ATS-002-summary.md)
- [WI-001 Evidence Pack](WI-20260725-ATS-001-evidence-pack.md)
- [Approved REQ](../user/REQ-20260725-ATS-001.md)
