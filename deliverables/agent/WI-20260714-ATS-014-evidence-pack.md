# Evidence Pack: WI-20260714-ATS-014

## Summary (one-liner)

- Staged newly issued social-login tokens before the first authenticated profile request, committed coherent user state, and cleaned up partial sessions without changing WI-011 logout semantics.

## Scope / DoD Check

- [x] Staged access and refresh tokens in auth storage/state before `fetchMe`.
- [x] Sent the newly issued access token on the first `/users/me` request.
- [x] Committed token, user, and role state only after the profile request succeeded.
- [x] Preserved complete and incomplete profile navigation.
- [x] Best-effort revoked the staged server session and always cleared local/dependent state on failure.
- [x] Prevented duplicate callback exchange under React Strict Mode.
- [x] Added focused mocked tests without live OAuth/provider calls.

## Reference Documents (Tier 0-2)

| Tier | Document | Reason |
|---|---|---|
| 0 | `docs/standards/core-principles.md` | System constitution and approved execution baseline |
| 0 | `docs/standards/development-standards.md` | Frontend implementation and verification standards |
| 0 | `docs/standards/documentation-standards.md` | Deliverable and traceability rules |
| 0 | `docs/standards/glossary.md` | Canonical terminology |
| 1 | `docs/policies/security-policy.md` | Token secrecy and failed-session cleanup policy |
| 2 | `docs/standards/frontend-standards.md` | React, Zustand, API, and Vitest conventions |
| 2 | `deliverables/user/REQ-20260714-ATS-001.md` | Approved P1 scope and work chain |
| 2 | `docs/audit/p1-remediation-trace-matrix-20260714.md` | `ATS020-P1-12` closure requirements |
| 2 | `docs/design/p1-security-acceptance-hardening-design.md` | Social callback ordering contract |
| 2 | `deliverables/agent/WI-20260714-ATS-011-evidence-pack.md` | Server logout and `clearSession` behavior that must be preserved |

**Injection Rules Applied**:

- Rule source: `AGENTS.md` and WI handoff INPUT POINTERS
- Assignee: `se`
- Task type: frontend security implementation
- Required context: Tier 0, security policy, frontend standards, approved REQ, audit/design contract, and WI-011 evidence

## Evidence Pointers

- `frontend/src/store/authStore.ts:23-50` - token-staging API, old-session cleanup, all-or-clean storage handling, and single Zustand access-token commit.
- `frontend/src/pages/auth/SocialLoginPage.tsx:44-84` - enforced `exchange -> stage -> fetchMe(token) -> login -> navigate` ordering and failure cleanup branch.
- `frontend/src/store/authStore.ts:64-84` - reused WI-011 server-first logout and unconditional `clearSession`; semantics were not reverted.
- `frontend/src/pages/auth/SocialLoginPage.test.tsx:71-112` - token/order assertions and Strict Mode single exchange.
- `frontend/src/pages/auth/SocialLoginPage.test.tsx:114-133` - profile-incomplete state commit and route assertion.
- `frontend/src/pages/auth/SocialLoginPage.test.tsx:135-174` - staged-token visibility during revoke and unconditional local/dependent-store cleanup after transient logout failure.
- No backend file, live OAuth configuration, or provider endpoint was changed or called.

## Commands & Outputs

- `npm test -- src/pages/auth/SocialLoginPage.test.tsx src/store/authStore.test.ts src/api/auth.test.ts`
  - Passed: 3 files, 9 tests, 0 failures.
- `npm run typecheck`
  - Passed: `tsc --noEmit`.
- `npx eslint src/pages/auth/SocialLoginPage.tsx src/pages/auth/SocialLoginPage.test.tsx src/store/authStore.ts --max-warnings 0`
  - Passed: 0 errors, 0 warnings.
- `git diff --check`
  - Passed for the complete tracked worktree: no whitespace errors; repository LF-to-CRLF notices only.
- `git diff --no-index --check -- NUL <untracked-WI-014-file>` for the new test, Korean summary, and Evidence Pack
  - Passed through a PowerShell wrapper that accepts the expected diff-only status: no whitespace errors; LF-to-CRLF notices only.

## Tests

- Success ordering: callback exchange occurs before staging; `fetchMe` receives the issued access token and observes both staged tokens while user/role remain uncommitted.
- Coherent commit: successful profile retrieval commits user/role before navigation.
- Incomplete profile: committed auth state routes to `/complete-profile`.
- Strict Mode: the callback code exchange runs exactly once.
- Failure cleanup: profile-fetch failure invokes server logout while the staged access token is present, then clears token/user/role/player/like state even when revocation is not confirmed.

## Risks / Rollback

- Risks:
  - A transient logout failure can leave server-side refresh revocation unconfirmed, although all local credentials and dependent state are removed.
  - Tests use mocked OAuth exchange/profile calls; live provider behavior remains intentionally unverified.
  - During the bounded profile request, the access token is present while user is `null` and role is `GUEST`; callback UI exposes no authenticated workflow until final commit.
- Rollback:
  - Remove only `stageTokens` and restore the previous callback ordering in `SocialLoginPage.tsx`; remove the focused callback test and WI-014 deliverables.
  - Preserve WI-011 `logoutSession`, async `logout()`, and `clearSession()` implementation and tests.
  - Do not revert unrelated concurrent frontend, backend, payment, file, CSV, or acceptance-environment edits.

## Follow-ups

- Chain edge released: WI-014 no longer blocks its portion of `WI-020`, `WI-024`, and `WI-025`.
- Trigger `WI-020` after Phase 3 (`WI-015` through `WI-017`) and all remaining Phase 2 dependencies complete.
- Trigger `WI-024` and `WI-025` only after their Phase 4 verification dependencies complete.
