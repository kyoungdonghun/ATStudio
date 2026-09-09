---
version: 1.6
last_updated: 2026-09-09
project: ATS
owner: SE
category: work-summary
status: active
dependencies:
  - path: REQ-20260909-ATS-001.md
    reason: Approved bounded remediation
  - path: ../agent/WI-20260909-ATS-004-evidence-pack.md
    reason: Implementation and validation evidence
---

# WI-20260909-ATS-004: Frontend Session Ownership

> Archive pointer correction (2026-09-09, WI016): Log and snapshot links below
> resolve to the [private local archive register](../../docs/registry/v1-artifact-retention-20260909.md),
> not downloadable originals. Historical execution claims and dates are unchanged.

## Summary

SEC-04 now includes MA's authorized F1 entrypoint lifecycle fix and F1-R1
internal staging fence. WI010 found that staging's synchronous clear notification
could establish B before old A token persistence resumed. Five regressions
were added and announced before the minimal store correction. MA reproduced
all five failures on the old source, then passed **163/163 focused tests in
8.43 seconds** on the fix. Source/tests are stable for the running full suite
and WI010 re-review. Earlier focused 158/158 and
full 1,558/1,558 results predate R1 and do not verify the current patch.

## Changes

- Record the existing session generation at request dispatch and retain it
  through refresh, queued resolution and replay. A late A request cannot use
  B's refresh credentials or replay its write as B.
- Bind refresh persistence, failure cleanup and shared-flight release to the
  owning session; retain coalescing, one replay and `skipAuthReplay` protection.
- Invalidate old refresh at logout start and guard logout completion against
  newer sessions. Keep the existing server-logout-before-token-removal order.
- Add real-authStore deferred regressions for replacement accounts, identical
  same-user login, newer refresh/logout flights and lazy-import boundaries.
- Guard SocialLoginPage exchange/profile success and failure, retaining
  StrictMode single exchange and profile/return-target behavior.
- Fix the equivalent LoginPage late-response/navigation/finally race and
  guard the existing social-start PKCE continuation.
- Fence stageTokens after its internal clear, before any old persistence or
  commit can affect a subscriber-created replacement session. Keep existing
  current-owner storage-failure behavior and add five real-store regressions.

Product/test ownership stayed within `frontend/src/api/client.ts`,
`frontend/src/api/client.test.ts`, `frontend/src/store/authStore.ts`, and
`frontend/src/store/authStore.test.ts` initially. MA then explicitly extended
the lifecycle scope to `frontend/src/pages/auth/SocialLoginPage.tsx`,
`SocialLoginPage.test.tsx`, `LoginPage.tsx` and `LoginPage.test.tsx` in that
directory. The existing LoginPage coverage fixture
`frontend/src/test/coverage/publicAuthShell.coverage.test.tsx` received only
the required authStore mock-export adaptation. No CSS, UI copy, provider
enablement or dependency files were changed by WI004.

## Verification

| Check | Status |
|---|---|
| Owned-file `git diff --check` | Executed by SE; exit 0 |
| Initial focused client/authStore tests | Executed by MA; 2 files, 63/63 PASS, 3.53 seconds |
| Complete patched-dependency isolated suite before F1 fix | Executed by MA; 112 files, 1,516/1,516 PASS |
| Pre-F1 coverage | Statements 90.27%; lines 92.85%; functions 91.19%; branches 82.86% |
| Pre-F1 typecheck, lint and build | PASS in MA's isolated snapshot |
| Prettier | SE executed authorized installed formatter and check on all nine owned frontend files; PASS |
| Pre-R1 entrypoint/client/authStore focused tests | MA: 5 files, 158/158 PASS, 9.01 seconds; initial 156/158 failure retained in evidence |
| Pre-R1 full frontend coverage | MA: 112 files, 1,558/1,558 PASS, 78.36 seconds; statements 90.26%, lines 92.84%, functions 91.21%, branches 82.82% |
| R1 three-file formatting/whitespace checks | SE: installed Prettier check and git diff --check PASS; no formatter write |
| R1 RED | MA: 5/5 selected cases FAIL on old source, 57 filtered skips, 3.57 seconds |
| R1 GREEN focused | MA: 5 files, 163/163 PASS, 8.43 seconds |
| R1 full suite and final quality gates | Full coverage running under MA; completion pending |
| Independent authentication review | WI010 F1-R1 scoped re-review resumed; disposition pending |
| Executed pre-fix failure baseline / live browser verification | R1 RED established; live browser not run |

The focused run used existing installed dependencies without modifying them.
SE read the [MA test log](../../docs/registry/v1-artifact-retention-20260909.md#a128)
and did not execute a test runner. SE also read the
[expanded focused log](../../docs/registry/v1-artifact-retention-20260909.md#a122):
156 passed, two failed in 8.80 seconds. Both storage-failure fixtures throw an
Error without a response, which the existing mapper classifies as a network
error. Test expectations now match that existing copy; production behavior
and UI text are unchanged. The failed run remains recorded. SE then read the
[final focused log](../../docs/registry/v1-artifact-retention-20260909.md#a121):
5/5 files, 158/158 PASS in 9.01 seconds, starting at 04:53:51 after MA's 04:53:50
snapshot refresh. The later R1 store/test changes are not covered by that run.

SE executed the authorized existing Prettier write/check on all nine owned
frontend files: both exit 0. Only `LoginPage.test.tsx` needed final formatting;
its before/after diff was line wrapping/indentation and an optional trailing
comma, with no semantic changes. No package installation was performed.

The [final coverage log](../../docs/registry/v1-artifact-retention-20260909.md#a130)
is the complete 1,516-test run. The earlier 1,399-test snapshot was incomplete
and must not be described as full verification. The complete 1,516-test run
also predates the additional F1 fix. SE subsequently read the
[pre-R1 full coverage log](../../docs/registry/v1-artifact-retention-20260909.md#a131):
1,558/1,558 PASS. That complete newer checkpoint still predates the R1
internal-clear regression, so another focused/full run is required.

For [RED](../../docs/registry/v1-artifact-retention-20260909.md#a140), MA copied
only `authStore.test.ts` and `SocialLoginPage.test.tsx` to the isolated pre-R1
source and selected `WI010-F1-R1`: all five new cases failed. The
[RED snapshot](../../docs/registry/v1-artifact-retention-20260909.md#a139)
preserves the original source hash. MA then copied fixed `authStore.ts` and
passed the broader five-file [GREEN focused selection](../../docs/registry/v1-artifact-retention-20260909.md#a138):
163/163 tests. SE read both execution logs. Old/fixed hashes and the isolated
read pointer are preserved in the evidence pack. Only these three files
changed for R1; no caller edits were needed. The coverage mock adaptation
remains in WI004's nine-file ownership. No runner was started by SE, and no
source/test edits followed the stable signal. Final full-suite and independent
review results remain pending, not inferred from GREEN.

## Impact and Handoff

This frontend-only change does not alter entitlement policy, backend schema,
token format or existing data. It does not provide new cross-tab coordination
or cancel writes already sent to the server. No provider/mail operation,
runtime restart, installed-dependency mutation or Git write was performed.

Unmount guards stop late completions but do not add cancellation/revocation
or purge already-staged local tokens solely on component cleanup.

Implementation slot returns to MA for refreshed verification, WI010 F1-R1
re-review and WI014 integration. This report does not close the REQ or declare
production GO.

## Related Documents

- [Evidence pack](../agent/WI-20260909-ATS-004-evidence-pack.md)
- [Approved REQ](REQ-20260909-ATS-001.md)
