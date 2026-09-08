---
version: 1.2
last_updated: 2026-09-09
project: ATS
owner: PG
category: work-summary
status: active
dependencies:
  - path: REQ-20260909-ATS-001.md
    reason: Approved bounded authentication review
  - path: ../agent/WI-20260909-ATS-010-evidence-pack.md
    reason: Findings, source pointers and validation provenance
---

# WI-20260909-ATS-010: Independent Authentication Review

## Result

Review delivered with one P2 residual path. No additional concrete backend
defect was established within the assigned fixes. Only this summary and the
WI010 evidence pack were authored; no product or test files were changed and
no heavy verification command was run by PG.

## Finding

**F1 [P2], SEC-04: stale social callback can replace or log out a newer session.**
At `frontend/src/pages/auth/SocialLoginPage.tsx:85-89`, defer A's `fetchMe`,
establish B, then reject A's request: the old callback invokes `logout()` on B.
Resolving A instead commits A's retained tokens/profile at `48-66`, replacing B
or undoing local logout. The effect has no generation/unmount guard. The new
client correctly rejects a stale 401, but this caller's catch still runs.

This is a source-confirmed remaining caller path, not an executed browser
reproduction or a claim that WI004 introduced the old caller. It requires an
accepted OAuth callback or a fake fixture; actual OAuth enablement is not
established. Existing callback tests cover current-session cleanup, not this
session replacement. Return this bounded regression to MA/WI004.

## Validation Boundary

| Evidence | Status |
|---|---|
| Backend auth initial selection | MA: 84 tests passed at that checkpoint |
| Captured validation logs / security chain | MA: 4 + 23 tests passed within the second 158-test selection |
| Frontend client/store focus | MA: 2 files / 63 tests passed |
| First isolated frontend coverage | 108 files / 1,399 tests passed, but **incomplete snapshot, not full pass** |
| Corrected frontend snapshot | MA restored four omitted source test files; 334 inputs, expected 112 test files; rerun pending at latest instruction |
| Frontend typecheck | MA reports pass, independently of the incomplete coverage snapshot |
| Full backend attempt | Failed test compilation on multipart InputBuffer fixture; MA reports correction, rerun pending |
| F1 deferred caller test / live OAuth / browser | Not executed by PG |

Source review confirmed the intended typed JWT/hash/role boundaries,
passwordless OAuth without false verification, User-before-reset-token lock
ordering, and minimized handler logging. H2 transaction tests, mocked MVC
dependencies and fake client adapters remain distinct evidence scopes.

MA owns the exact ready selections in the evidence pack, F1 disposition, and
WI013/WI014 continuation. This review does not close the REQ or approve release.

## Related Documents

- [Evidence pack](../agent/WI-20260909-ATS-010-evidence-pack.md)
- [Handoff](../agent/WI-20260909-ATS-010-handoff.md)

## 2026-09-09 Re-Review, 04:52 KST

The original review above is preserved. The 04:45:20-stable entrypoint fix
blocks the original delayed exchange/profile success and failure paths,
including replacement/same-user login, logout, unmount and StrictMode replay.

**F1 is not fully closed: F1-R1 [P2] remains at
`frontend/src/store/authStore.ts:72-81`.** When staging A triggers its internal
`clearSession()` notification, a one-shot synchronous store subscriber can
complete B's login. Staging then resumes and writes A's tokens over B, leaving
B's user/role with A's credentials. The callback's generation check at
`SocialLoginPage.tsx:83-84` occurs after the overwrite. New reentrancy tests
trigger only after A's token is already set, missing this intermediate null
notification. This is a source-confirmed synthetic counterexample, not a
browser/provider reproduction; its ready fixture is in the evidence pack.

Latest evidence bounds at this checkpoint:
- MA's entrypoint focus: 156 passed / 2 failed / 158 total, five files. Both
  failures are storage-failure expected-copy mismatches; narrow fixture
  correction/retest is pending. F1-R1 is a separate unexecuted schedule.
- Full post-F1 frontend coverage/final gates remain pending. Prior complete
  1,516-test evidence predates this fix; the 1,399-test snapshot is incomplete.
- MA's final backend: 1,825 total / 1,806 passed / 19 skipped / 0 failed;
  JaCoCo gate PASS. JSON/log evidence was read, not rerun. WI001 is unchanged
  per MA; no further backend audit was performed.

Only WI010 reports were updated. Return F1-R1 to MA/WI004; do not close F1 or
the REQ based on the current checkpoints. No source edit or heavy runner by PG.

### 04:54 KST Focused Result Addendum

Actual `frontend-entrypoint-focused-final.log` confirms **5 files / 158 tests
PASS**, start 04:53:51, duration 9.01s, after expected-copy correction and
Prettier. This supersedes the two earlier copy-expectation failures, not their
historical record. The three reviewed product hashes remain unchanged.

**F1-R1 remains open:** the passing suites do not exercise B login during
staging's intermediate null-state notification. Full post-F1 frontend coverage
is running under MA; no full completion or production/OAuth approval is claimed.

## 2026-09-09 Closure, 05:03 KST

**F1, including F1-R1, is CLOSED for this independent scoped review.** The
preceding findings remain as history. `frontend/src/store/authStore.ts:72-75`
now checks the expected generation immediately after the internal clear and
before any persistence. A newer subscriber login or logout therefore prevents
all old staging writes and cleanup. The original delayed callback guards remain.

PG verified actual MA execution logs and matching snapshot hashes:
- RED: original D2784A7... source, all five selected R1 tests failed; 57 other
  cases were excluded by the name filter, not permanent skips.
- GREEN: fixed source, **5 files / 163 tests PASS**, 8.43s, start 05:01:04;
  `frontend-r1-green-focused.log`.
- Tests preserve successor tokens/profile/role/continuation and prevent stale
  profile/logout dispatch or navigation. Normal staging, owned storage-failure
  clearing/throwing, error guidance and StrictMode completion remain tested.

No further counterexample found at this boundary. Post-R1 full frontend
coverage/final gates remain pending with MA; this is not REQ or release approval.
Only WI010 reports were updated. PG ran no test suite and changed no product file.

### 05:05 KST Final Evidence Addendum

Post-R1 logs now confirm **112 files / 1,563 tests PASS**, 79.28s. MA confirms
the coverage gate, typecheck, lint, format and build all PASS; PG read their
named final logs. This supersedes the earlier pending-FE-gates status.

**F1/R1 CLOSED**, supported by the five failing-original cases, 163-test
focused GREEN and current full-suite evidence. No open finding remains at this
reviewed boundary. No additional audit, product edit or runner execution by PG.
MA owns WI013/WI014 and REQ/release closure; no live OAuth/browser claim is made.
