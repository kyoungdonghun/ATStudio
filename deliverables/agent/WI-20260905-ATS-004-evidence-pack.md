---
version: 1.0
last_updated: 2026-09-05
project: ATS
owner: se
category: evidence-pack
status: stable
dependencies:
  - path: WI-20260905-ATS-004-handoff.md
    reason: Approved two-finding remediation and ownership
  - path: WI-20260905-ATS-001-evidence-pack.md
    reason: Original F1 and F2 findings
  - path: ../user/REQ-20260905-ATS-001.md
    reason: Approved release-verification closeout
---

# Evidence Pack: WI-20260905-ATS-004

## Summary

TL;DR: F1 and F2 are fixed and focused regressions pass: 84 frontend tests
and 107 backend tests. No playback-persistence change, full suite, runtime
operation, database operation, staging, or commit was performed by this WI.

Workspace: `C:/Users/jm991/Desktop/project/ATStudio`.
Branch: `codex/v1-release-rehearsal-fixes`. The existing client-feedback
changes were preserved; a HEAD diff also contains work predating this WI.

## Scope / DoD Check

- [x] Same visible drawer tab toggles closed; a different tab switches without closing.
- [x] Real PlayerBar and PlaylistDrawer are tested together with manual child-tab changes.
- [x] Desktop/mobile actions and empty/loaded player rendering branches are covered.
- [x] All three Java nickname DTO setters, service paths, and profile entity mutations use the same edge normalizer.
- [x] Exact ECMAScript whitespace behavior and unchanged internal-space policy are tested.
- [x] Focused tests, frontend typecheck, scoped lint/format, and whitespace checks complete.
- [x] Product work remains limited to the two approved findings.
- [ ] MA performs final full regression and browser/operational closeout on the settled source.

## Implementation Evidence

### F1: Synchronize the visible drawer tab

- `frontend/src/components/player/PlaylistDrawer.tsx:58,81-84,93` adds one
  optional `onTabChange` callback. A manual tab choice updates existing child
  state and notifies the parent in the same event.
- `frontend/src/layouts/PlayerBar.tsx:653,1138` passes its existing
  `setDrawerTab` setter on both drawer-rendering branches. The existing
  request-ID mechanism and same-tab toggle are retained; no new state layer
  or broad abstraction was introduced.
- `frontend/src/layouts/PlayerBar.drawer.test.tsx` renders the actual parent
  and child, using real stores and mocked APIs. It exercises Likes -> manual
  Playlists -> parent Likes, then the reverse direction, and same-tab close.
  The matrix is desktop/mobile actions times empty/loaded player, four cases.
- Existing `PlayerBar.test.tsx` and `playerComponents.test.tsx` were not
  edited by this WI. They passed alongside the new integration test,
  retaining generic drawer reopen and other player regressions.

### F2: Match ECMAScript trim exactly

- `src/main/java/com/atstudio/atstudio/common/validation/ValidationConstants.java:16-37`
  adds a linear edge scan with the ECMAScript WhiteSpace/LineTerminator set.
  It includes U+00A0, U+2007, U+202F, and U+FEFF, and excludes Java-only
  whitespace such as U+001C through U+001F. Null remains null; interior
  characters are never rewritten.
- `RegisterRequest.java:30`, `CompleteProfileRequest.java:28`, and
  `UpdateProfileRequest.java:22` in
  `src/main/java/com/atstudio/atstudio/dto/user/` call that normalizer before
  nickname Bean Validation.
- `UserService.java:36,79,128,236,279,420` in
  `src/main/java/com/atstudio/atstudio/service/` imports the same function for
  registration, completion, update, availability, and duplicate lookup.
  The old private `strip()` helper was removed.
- `src/main/java/com/atstudio/atstudio/entity/User.java:76,114` uses the same
  function for profile update/completion. Registration's builder receives the
  normalized service value. No stored historical row or fixture was repaired.
- The pre-existing `NICKNAME_PATTERN` and frontend `normalizeNickname()`
  implementation are unchanged by this WI. Accepted internal ASCII spaces,
  including repeated spaces, remain intact; non-ASCII/internal control
  whitespace remains invalid.

## Exact Changed Paths

Ten existing code/test files changed relative to the assigned dirty baseline:

```text
frontend/src/components/player/PlaylistDrawer.tsx
frontend/src/layouts/PlayerBar.tsx
frontend/src/utils/validationHelpers.test.ts
src/main/java/com/atstudio/atstudio/common/validation/ValidationConstants.java
src/main/java/com/atstudio/atstudio/dto/user/RegisterRequest.java
src/main/java/com/atstudio/atstudio/dto/user/CompleteProfileRequest.java
src/main/java/com/atstudio/atstudio/dto/user/UpdateProfileRequest.java
src/main/java/com/atstudio/atstudio/entity/User.java
src/main/java/com/atstudio/atstudio/service/UserService.java
src/test/java/com/atstudio/atstudio/service/UserServiceTest.java
```

Four files created:

```text
frontend/src/layouts/PlayerBar.drawer.test.tsx
src/test/java/com/atstudio/atstudio/common/validation/NicknameNormalizationTest.java
deliverables/agent/WI-20260905-ATS-004-evidence-pack.md
deliverables/user/WI-20260905-ATS-004-summary.md
```

`frontend/src/store/playerStore.ts`, `frontend/src/utils/validation.ts`,
other product behavior, client worktree, database/schema, runtime configuration,
and prior/peer WI reports were not edited by this WI. The two new test files
must be included in MA's eventual explicit stage candidates; no staging occurred.

## Tests and Commands

All executions below are current, on 2026-09-05. Commands are relative to the
workspace root except frontend commands, whose working directory is `frontend/`.

### Red Evidence

- After correcting the new test harness to await subscription readiness and
  address the CSS-hidden mobile controls, the real-drawer test failed in all
  four cases at the assertion immediately after manual tab switch -> parent
  Likes. Frontend whitespace tests already passed, as expected.
- `gradlew.bat test --tests "com.atstudio.atstudio.common.validation.NicknameNormalizationTest" --console=plain`
  ran 33 tests before the Java fix: six failed. Failures covered the exact
  whitespace set, four specified Unicode edges, and all-whitespace input.
- The initial TypeScript check found an unsupported `exact` option in the
  new role queries. That test-only option was removed before final checks;
  no product change was made to accommodate the test harness.

### Final Green Evidence

| Command / scope                                                                                                                                                                                        | Result                                                                                                     |
| ------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------ | ---------------------------------------------------------------------------------------------------------- |
| `npm test -- src/layouts/PlayerBar.drawer.test.tsx src/layouts/PlayerBar.test.tsx src/components/player/playerComponents.test.tsx src/utils/validationHelpers.test.ts`                                 | PASS: 4 files, 84 tests. Final run started 16:36:45 KST and took 4.37 seconds.                             |
| `gradlew.bat test --tests "com.atstudio.atstudio.common.validation.NicknameNormalizationTest" --tests "com.atstudio.atstudio.service.UserServiceTest" --console=plain`                                 | PASS: BUILD SUCCESSFUL; 107 tests, zero failures/errors/skips; 26 seconds. Only two classes were selected. |
| `npm run typecheck`                                                                                                                                                                                    | PASS after the test-query correction, exit 0.                                                              |
| `node node_modules/eslint/bin/eslint.js src/layouts/PlayerBar.tsx src/components/player/PlaylistDrawer.tsx src/layouts/PlayerBar.drawer.test.tsx src/utils/validationHelpers.test.ts --max-warnings 0` | PASS, exit 0.                                                                                              |
| `node node_modules/prettier/bin/prettier.cjs --check src/layouts/PlayerBar.tsx src/components/player/PlaylistDrawer.tsx src/layouts/PlayerBar.drawer.test.tsx src/utils/validationHelpers.test.ts`     | PASS, exit 0; only the two WI-owned frontend test files required formatting.                               |
| `git diff --check -- <ten existing changed paths above>`                                                                                                                                               | PASS, exit 0; Git reports existing CRLF conversion warnings, no whitespace errors.                         |

Parsed JUnit evidence:

- `build/test-results/test/TEST-com.atstudio.atstudio.common.validation.NicknameNormalizationTest.xml`:
  33 tests, zero failures/errors/skips, timestamp `2026-09-05T07:36:00.266Z`.
- `build/test-results/test/TEST-com.atstudio.atstudio.service.UserServiceTest.xml`:
  74 tests, zero failures/errors/skips, timestamp `2026-09-05T07:36:02.127Z`.
- Gradle compiled application and test classes as prerequisites for these
  focused tests. It printed an unchecked-operations compiler note and a JVM
  class-sharing warning; neither failed the selected tests. No `build`, full
  `test`, coverage, or new Gradle task was run after that focused command.

The new Java test exhaustively checks all 65,536 BMP code units against the
25-character ECMAScript edge set and covers each accepted edge through JSON
deserialization, nickname property validation, and entity profile changes.
It uses the repository's existing Jackson test dependency and Bean Validation
without a Spring application or database. Frontend tests check the same full
BMP boundary and the four specified edge/interior cases. Service tests inject
raw values past DTO setters to independently prove lookup, persistence,
response normalization, and canonical duplicate rejection using mocks.

## Browser Coordination and Evidence Limits

- MA reported a 1,449-test frontend/full-quality run and backend build before
  this patch. Those results do not certify this new patch and were not
  duplicated here. Current evidence from this WI is the focused green matrix.
- MA reported a successful localhost five-second reload before frontend HMR
  during this work. That is MA evidence, not an independently repeated test.
- Frontend settled notice was sent after final checks around 16:36:50 KST.
  No frontend source/test writes were made afterward. MA requested a minimum
  three-minute browser window and no further frontend edits until that pass
  completes; that freeze remains respected. Backend tests were already finished.
- No browser, playback, storage, or beforeunload fix was attempted. No claim
  is made that this WI resolves the separate playback-persistence investigation.

## Reference Documents (Tier 0-2)

| Tier   | Document                                                                                                             | Reason                                                               |
| ------ | -------------------------------------------------------------------------------------------------------------------- | -------------------------------------------------------------------- |
| 0      | `docs/standards/core-principles.md` / STD-001                                                                        | Injected approval and scope boundaries                               |
| 0      | `docs/standards/documentation-standards.md` / STD-004                                                                | Injected metadata and deliverable rules                              |
| 0      | `docs/standards/development-standards.md` / STD-002                                                                  | Injected implementation and testing conventions                      |
| 0      | `docs/standards/glossary.md` / STD-005                                                                               | Injected canonical terminology                                       |
| 1      | `docs/policies/security-policy.md`, `docs/policies/quality-gates.md`                                                 | Existing safety and verification rules                               |
| 2      | `docs/design/api-spec.md`, `docs/ui/screen-flow.md`                                                                  | Nickname and explicit drawer-action contracts                        |
| 2      | `deliverables/user/REQ-20260823-ATS-001.md`                                                                          | Existing client-feedback intent                                      |
| 2      | `deliverables/agent/WI-20260905-ATS-001-evidence-pack.md`                                                            | Confirmed F1/F2 scope                                                |
| WI     | `deliverables/agent/WI-20260905-ATS-004-handoff.md`                                                                  | Read before implementation; two-set output contract                  |
| Skills | `react-best-practices`, `test`, `typecheck`, `eslint`, `prettier`, `create-wi-evidence-pack` under `.agents/skills/` | Relevant implementation, focused verification, and evidence workflow |

Tier 0 was retained from the injected context; the approved se assignment and
WI-004 pointers followed it. No new subagent or handoff was created.

## Risks / Rollback / Follow-up

- No unresolved blocker remains within F1/F2 after focused verification.
  MA still owns final full regression, real-browser verification, and release approval.
- The existing dirty changes are not this WI's rollback unit. Restore only
  this WI's callback wiring and replacement of Java `strip()` calls, and
  amend/remove only its new regression additions if rollback is approved.
  Never restore whole dirty files from HEAD or discard unrelated work.
- Chain: return results to MA for WI-20260905-ATS-002 final regression and
  WI-20260905-ATS-003 closeout. Do not close the overall REQ or stage/commit here.
