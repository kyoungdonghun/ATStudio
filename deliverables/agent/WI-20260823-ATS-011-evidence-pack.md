# Evidence Pack: WI-20260823-ATS-011

## Scope / DoD

- [x] Updated only the stale SocialLoginPage staged-session failure test.
- [x] Kept the existing failure copy and best-effort logout behavior.
- [x] Verified auth and user-scoped likes clear after staged-session failure.
- [x] Verified a public PlayerBar track, queue, and paused position remain.
- [x] Focused and full frontend test suites passed.
- [x] Typecheck, ESLint, Prettier, build, and diff check passed.

## Patch Rationale

WI-010 established that `authStore.clearSession()` only clears authentication
and user-dependent like state; it must not clear public player state. The
previous SocialLoginPage assertion expected `isPlaying` to become false, which
represented the old coupling rather than the approved contract.

The updated test seeds a typed public playable track and checks the full
PlayerBar-relevant state after a failed staged social session: current track,
queue, current time, duration, and paused status.

## Changed Files

- `frontend/src/pages/auth/SocialLoginPage.test.tsx`
- `deliverables/user/WI-20260823-ATS-011-summary.md`
- `deliverables/agent/WI-20260823-ATS-011-evidence-pack.md`

## Evidence Pointers

- `frontend/src/pages/auth/SocialLoginPage.test.tsx:42-53`: public playable
  track fixture used by the staged-session failure case.
- `frontend/src/pages/auth/SocialLoginPage.test.tsx:229-273`: failure path
  seeds public playback and verifies auth/likes reset without player reset.
- `frontend/src/store/authStore.ts:172-182`: session cleanup preserves public
  playback while clearing auth and user-specific likes.
- `deliverables/agent/WI-20260823-ATS-010-evidence-pack.md`: established
  public-playback persistence boundary.

## Commands And Results

| Command | Result |
| --- | --- |
| `npm test -- src/pages/auth/SocialLoginPage.test.tsx` | PASS: 1 file, 8 tests, 0 failures. |
| `npm test` | PASS: 111 files, 1,449 tests, 0 failures. Vitest emitted one existing jsdom navigation diagnostic. |
| `npm run typecheck` | PASS: `tsc --noEmit` exited 0. |
| `npm run lint` | PASS: ESLint exited 0 with zero warnings allowed. |
| `npm run format` | PASS: Prettier reported all matched files formatted. |
| `npm run build -- --outDir "$env:TEMP\\atstudio-wi-20260823-ats-011-build"` | PASS: `tsc -b` and Vite production build exited 0. |
| `git diff --check -- frontend/src/pages/auth/SocialLoginPage.test.tsx deliverables/user/WI-20260823-ATS-011-summary.md deliverables/agent/WI-20260823-ATS-011-evidence-pack.md` | PASS: no diagnostics. |

## Scope Confirmation And Rollback

No product source, auth API contract, player persistence logic, browser
storage, backend, database, external provider, runtime, or client-acceptance
worktree was changed. Roll back by reverting the test file and removing these
two WI-011 deliverables.
