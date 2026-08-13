---
version: 1.0
last_updated: 2026-08-13
project: ATS
owner: se
category: evidence-pack
status: stable
related_wi: WI-20260809-ATS-042
dependencies:
  - path: WI-20260809-ATS-042-handoff.md
    reason: Approved scope, acceptance criteria, and output contract
  - path: ../../docs/policies/security-policy.md
    reason: Fail-closed capability and bounded error-presentation contract
  - path: ../../docs/standards/frontend-standards.md
    reason: Current React auth and Profile state contract
---

# Evidence Pack: WI-20260809-ATS-042

## Summary

- Corrected the seven bounded auth/Profile roots `CR-031-008` through
  `CR-031-014`, remediated the independent PG findings, and closed the final P3
  inherited-property error-code lookup without weakening fail-closed or
  account-enumeration protections.

## Scope / DoD Check

- [x] Social profile completion fences validation and mutation as one pending
      transaction and disables every related control.
- [x] Public capability loading, ready, and failure states are distinct; failure
      advertises no auth capability and exposes a manual retry after each failed
      attempt without automatic retry.
- [x] Login, Signup, and both Password Reset steps consume the same fail-closed
      capability state.
- [x] Profile panel keys remain canonical, legacy activity query keys redirect
      to activity routes, and unsupported keys normalize to `account`.
- [x] Subscription loading, success, authoritative absence, and retryable
      failure are separate latest-request-owned states.
- [x] Social profile completion revalidates current identity before mutation and
      refreshes it through the auth store's session-generation/user-ID guard
      after mutation.
- [x] Forgot-password receipt/error presentation remains enumeration-safe;
      account mutation and email-verification errors use fixed bounded mappings.
- [x] Focused/adjacent auth/Profile tests and the two named coverage suites pass.
- [x] Typecheck, WI-changed frontend ESLint/Prettier, documentation validation,
      and whitespace validation pass.
- [x] Main's full frontend suite, instrumented coverage, typecheck, lint,
      Prettier, and production build results pass.
- [x] The isolated backend full gate, JaCoCo reports/thresholds, and assemble
      pass.
- [x] Independent final PG re-review is `PASS` with `P1=0` and `P2=0`; the final
      P3 inherited-property lookup issue is closed by focused hardening.

## Reference Documents

| Tier | Document | Use |
| --- | --- | --- |
| 0 | `docs/standards/core-principles.md` | Approval, security, simplicity, and traceability |
| 0 | `docs/standards/development-standards.md` | TDD, frontend quality, and evidence requirements |
| 1 | `docs/policies/security-policy.md` | Authentication and safe error presentation |
| 1 | `docs/policies/access-control-policy.md` | Existing authorization boundaries |
| 1 | `docs/policies/quality-gates.md` | Focused, adjacent, review, and final gates |
| 1 | `docs/standards/frontend-standards.md` | Current React/auth/Profile conventions |
| 1 | `docs/standards/evidence-pack-standard.md` | Evidence structure and metadata |
| Skill | `.agents/skills/react-best-practices/AGENTS.md` | React implementation constraints |
| REQ | `deliverables/user/REQ-20260809-ATS-001.md` | Approved parent request and decision boundaries |
| WI | `deliverables/agent/WI-20260809-ATS-022-findings.md` | Original auth/Profile findings |
| WI | `deliverables/agent/WI-20260809-ATS-031-consolidated-findings.md:572-578` | Canonical roots `CR-031-008` through `CR-031-014` |
| WI | `deliverables/agent/WI-20260809-ATS-042-handoff.md` | Approved implementation/output contract |

## Root-to-Evidence Traceability

| Root | Implementation | Focused evidence |
| --- | --- | --- |
| `CR-031-008` | `frontend/src/pages/auth/SocialCompleteProfilePage.tsx:62,154-200` owns the full validation/mutation fence and disables controls while pending. | `frontend/src/pages/auth/SocialCompleteProfilePage.test.tsx:200-227` uses delayed availability validation and proves one validation/mutation entry. |
| `CR-031-009` | `frontend/src/hooks/usePublicCapabilities.ts:7-45` represents explicit state, latest-request ownership, and manual retry; Login/Signup/Reset render fail-closed at `LoginPage.tsx:205-235`, `SignupPage.tsx:174-210`, and `PasswordResetPage.tsx:20-67`. | `usePublicCapabilities.test.tsx:39-87`, `LoginPage.test.tsx:151-172`, `SignupPage.test.tsx:126-145`, and `PasswordResetPage.test.tsx:55-71`. |
| `CR-031-010` | `frontend/src/pages/subscriber/ProfilePage.tsx:57-102,143-152` limits query panels and replace-navigates activity/unsupported keys. | `ProfilePage.test.tsx:259-325` covers unsupported, five activity routes, and back/forward canonical behavior. |
| `CR-031-011` | `frontend/src/pages/subscriber/ProfilePage.tsx:119-192,493-554` separates subscription states, aborts superseded reads, and recognizes only authoritative absence. | `ProfilePage.test.tsx:338-371` covers failure/retry and `NO_ACTIVE_SUBSCRIPTION` empty state. |
| `CR-031-012` | `frontend/src/pages/auth/SocialCompleteProfilePage.tsx:68-98` revalidates identity before showing mutation controls. | `SocialCompleteProfilePage.test.tsx:229-253` covers complete redirect and retryable identity failure. |
| `CR-031-013` | `frontend/src/api/authError.ts:33-46,106-119` and `PasswordResetPage.tsx:110-130,225-230` preserve safe guidance and generic accepted receipts. | `authError.test.ts:18-28`, `PasswordResetPage.test.tsx:73-113`, and `publicAuthShell.coverage.test.tsx:477-531`. |
| `CR-031-014` | `frontend/src/api/authError.ts:48-68` and `ProfilePage.tsx:344-355` map password-update failures to bounded guidance. | `ProfilePage.test.tsx:373-399` and `adminSubscriberGaps.coverage.test.tsx:1451-1486`. |

## Independent PG Finding Disposition

| Finding | Disposition | Evidence |
| --- | --- | --- |
| P2-1: post-mutation `fetchMe` plus manual login could restore a logged-out session | Closed; independent re-review has no P2 finding | `SocialCompleteProfilePage.tsx:48,180-200` uses `refreshCurrentUser()` and checks lifecycle/current session before navigation. `SocialCompleteProfilePage.test.tsx:255-290` clears the session and unmounts while the refresh is deferred, then proves auth/user storage stays empty and the return target is not consumed. |
| P2-2: EmailVerify rendered arbitrary backend message text | Closed; independent re-review has no P2 finding | `authError.ts:123-132`, `EmailVerifyPage.tsx:20-26`, `EmailVerifyPage.test.tsx:17-39`, and `publicAuthShell.coverage.test.tsx:459-475` prove `INVALID_TOKEN` mapping and raw-message suppression. |
| Initial P3: activity query tabs had menu keys but no Profile panels | Closed | `ProfilePage.tsx:57-79,94-102,143-152` and `ProfilePage.test.tsx:269-325` route activity keys to canonical destinations on direct and history traversal. |
| Re-review P3: inherited `__proto__` or `constructor` lookup could return a non-string value | Closed in final hardening | `authError.ts:12-18` requires an own property and a runtime string value. `authError.test.ts:60-71` proves both adversarial codes use the bounded email-verification fallback and return a string. |

Independent final PG re-review verdict: `PASS`, with `P1=0` and `P2=0`. The
final P3 is closed by the own-property/string guard and focused adversarial test
above.

## Additional Changed Files

### Product and Focused Tests

- `frontend/src/api/authError.ts`
- `frontend/src/api/authError.test.ts`
- `frontend/src/hooks/usePublicCapabilities.ts`
- `frontend/src/hooks/usePublicCapabilities.test.tsx`
- `frontend/src/pages/auth/EmailVerifyPage.tsx`
- `frontend/src/pages/auth/EmailVerifyPage.test.tsx`
- `frontend/src/pages/auth/LoginPage.tsx`
- `frontend/src/pages/auth/LoginPage.test.tsx`
- `frontend/src/pages/auth/PasswordResetPage.tsx`
- `frontend/src/pages/auth/PasswordResetPage.test.tsx`
- `frontend/src/pages/auth/SignupPage.tsx`
- `frontend/src/pages/auth/SignupPage.test.tsx`
- `frontend/src/pages/auth/SocialCompleteProfilePage.tsx`
- `frontend/src/pages/auth/SocialCompleteProfilePage.test.tsx`
- `frontend/src/pages/auth/SocialLoginPage.tsx`
- `frontend/src/pages/auth/SocialLoginPage.test.tsx`
- `frontend/src/pages/subscriber/ProfilePage.tsx`
- `frontend/src/pages/subscriber/ProfilePage.test.tsx`
- `frontend/src/test/coverage/adminSubscriberGaps.coverage.test.tsx`
- `frontend/src/test/coverage/publicAuthShell.coverage.test.tsx`

### Current Documentation and WI Deliverables

- `docs/design/index.md`
- `docs/design/usecase/user-info.md`
- `docs/design/usecase/util.md`
- `docs/policies/security-policy.md`
- `docs/standards/frontend-standards.md`
- `docs/ui/atstudio-front-list.md`
- `docs/ui/screen-flow.md`
- `deliverables/agent/WI-20260809-ATS-042-evidence-pack.md`
- `deliverables/user/WI-20260809-ATS-042-summary.md`

Main corrected `docs/design/index.md` from stale API Specification v30.0 / 149
method-level mappings to current v30.3 / 150. The WI handoff remained unchanged,
and protected `output/` entries were untouched.

## Red / Green Evidence

1. Initial RED command:
   `cd frontend; npm test -- --run src/hooks/usePublicCapabilities.test.tsx src/api/authError.test.ts src/pages/auth/LoginPage.test.tsx src/pages/auth/SignupPage.test.tsx src/pages/auth/PasswordResetPage.test.tsx src/pages/auth/SocialCompleteProfilePage.test.tsx src/pages/subscriber/ProfilePage.test.tsx`
   - 7 files, 39 tests: 23 passed and 16 failed before implementation.
2. Initial GREEN command: same seven-file command.
   - 7 files, 42 tests: 42 passed and 0 failed after the seven root fixes.
3. PG remediation focused command:
   `cd frontend; npm test -- --run src/pages/auth/SocialCompleteProfilePage.test.tsx src/pages/auth/EmailVerifyPage.test.tsx src/pages/subscriber/ProfilePage.test.tsx src/api/authError.test.ts`
   - 4 files, 28 tests: 28 passed and 0 failed.

The RED/GREEN evidence used deterministic Vitest promises and MemoryRouter only;
no live auth, OAuth, mail, or persistent data was used.

## Final SE Verification

| Lane | Exact command | Result |
| --- | --- | --- |
| Focused + adjacent auth/Profile | `cd frontend; npm test -- --run src/store/authStore.test.ts src/hooks/usePublicCapabilities.test.tsx src/router/ProtectedRoute.test.tsx src/api/authError.test.ts src/pages/subscriber/ProfilePage.test.tsx src/pages/auth/SocialLoginPage.test.tsx src/pages/auth/SocialCompleteProfilePage.test.tsx src/pages/auth/SignupPage.test.tsx src/pages/auth/PasswordResetPage.test.tsx src/pages/auth/LoginPage.test.tsx src/pages/auth/EmailVerifyPage.test.tsx` | PASS: 11 files, 77 tests |
| Named coverage files | `cd frontend; npm test -- --run src/test/coverage/adminSubscriberGaps.coverage.test.tsx src/test/coverage/publicAuthShell.coverage.test.tsx` | PASS: 2 files, 52 tests |
| TypeScript | `cd frontend; npm run typecheck` | PASS: exit 0 |
| Changed-file ESLint | `cd frontend; npx eslint <20 WI-changed TS/TSX files> --max-warnings 0` | PASS: 0 errors, 0 warnings, exit 0; exact file list is the Product and Focused Tests list above |
| Changed-file Prettier | `cd frontend; npx prettier --check <20 WI-changed TS/TSX files>` | PASS: all matched files formatted, exit 0 |
| Final P3 focused | `cd frontend; npm test -- --run src/api/authError.test.ts` | PASS: 1 file, 5 tests |
| Final P3 typecheck | `cd frontend; npm run typecheck` | PASS: exit 0 |
| Final P3 ESLint | `cd frontend; npx eslint src/api/authError.ts src/api/authError.test.ts --max-warnings 0` | PASS: 0 errors, 0 warnings, exit 0 |
| Final P3 Prettier | `cd frontend; npx prettier --check src/api/authError.ts src/api/authError.test.ts` | PASS: all matched files formatted, exit 0 |
| Documentation | `python .agents/skills/validate-docs/scripts/validate_docs.py` | PASS: Tier 0, internal links, 579 traceability IDs, and document index; exit 0 |
| Whitespace | `git diff --check` | PASS: exit 0; non-blocking existing CRLF-to-LF warnings only |

Final scoped test total is **129/129** across 13 distinct files: 77 focused/
adjacent tests plus 52 tests in the two explicitly requested coverage files.

An additional non-gate probe that included the six repository Markdown files in
frontend Prettier returned exit 1 because those documents do not match frontend
Prettier's whole-file style. No document reformat was applied. The configured
frontend changed-file Prettier check and the dedicated documentation validator
both pass.

## Final Full Gates

The following final full-gate results include the P3 hardening and are recorded
separately from SE's focused reruns:

| Gate | Result |
| --- | --- |
| Full frontend tests | PASS: 78 files, 871 tests, 0 failures |
| Instrumented coverage | PASS: statements 88.68% (7872/8876), branches 80.19% (4959/6184), functions 88.31% (1965/2225), lines 90.93% (7237/7958) |
| Typecheck | PASS |
| ESLint | PASS |
| Prettier | PASS |
| Production build | PASS |
| Backend isolated full gate | `.\gradlew.bat test jacocoTestReport jacocoTestCoverageVerification assemble --rerun-tasks --no-daemon --max-workers=1 --console=plain` — PASS: 1568 tests, 0 failures/errors, 19 skipped; instruction 86.957%, branch 72.251%, line 87.228%, method 84.730%, class 94.824%; assemble and thresholds pass |
| Documentation | PASS: 579 traceability IDs, Tier 0, internal links, and document index |
| Whitespace | PASS: `git diff --check`; existing CRLF warnings only |

No backend product file changed; the isolated backend command was a final
repository-quality gate only.

## Current Documentation

- `docs/standards/frontend-standards.md:227-237,350-369` defines fail-closed
  capability retry, guarded profile completion refresh, canonical activity
  routes, and subscription state separation.
- `docs/policies/security-policy.md:152-164` defines unknown capability failure,
  bounded error mappings, session-generation persistence, and lifecycle-owned
  navigation.
- `docs/design/usecase/user-info.md:51-54,129-151,170-183` aligns login, social
  completion, and Profile query behavior.
- `docs/design/usecase/util.md:49-61` defines manual retry after each failed
  capability attempt and no automatic retry.
- `docs/design/index.md:28` records current API Specification v30.3 and 150
  method-level mappings.
- `docs/ui/atstudio-front-list.md:51-69` and `docs/ui/screen-flow.md:23-42`
  reflect the current visible auth/Profile behavior.

## Risks / Rollback

- Deterministic tests prove the requested races locally, but no deployed browser,
  real OAuth Provider, mail delivery, live authentication, or persistent DB was
  exercised; all are explicitly outside this WI.
- The unmount/session regression combines session clear and unmount during the
  deferred refresh. The auth store generation guard prevents repopulation after
  session clear; the page lifecycle guard prevents late navigation after
  unmount.
- Rollback: revert the WI-scoped capability hook/presentation, safe error helper,
  auth/Profile page changes, focused/coverage tests, seven current documents, and
  both WI deliverables as one patch. No schema, data, Provider, mail, dependency,
  or backend rollback is required.

## Decisions / Escalations

- No new policy decision or escalation was required.
- WI-060 consent/unverified-login/return-origin decisions were not made or
  implemented.
- The independent re-review's only remaining P3 required no product-policy
  decision and is closed by the bounded lookup guard.

## Side-Effect and Git Record

- No secret/local configuration, ignored file, live auth/OAuth/mail/DB, backend
  policy, schema, dependency, deployment, protected `output/` artifact, commit,
  stage, or push was inspected or changed.
- Protected `output/` artifacts remained untouched throughout finalization.
- No file was deleted and no persistent state was mutated.

## Follow-Up Chain

- Handoff-blocked WIs: `WI-20260809-ATS-058`, `WI-20260809-ATS-060`, and
  `WI-20260809-ATS-072`.
- WI-042 implementation, independent PG review, and final quality gates are
  complete. Main retains orchestration ownership for releasing or sequencing
  blocked WIs.

## Related Documents

- [WI-042 Handoff](WI-20260809-ATS-042-handoff.md)
- [WI-042 User Summary](../user/WI-20260809-ATS-042-summary.md)
- [Frontend Standards](../../docs/standards/frontend-standards.md)
- [Security Policy](../../docs/policies/security-policy.md)
