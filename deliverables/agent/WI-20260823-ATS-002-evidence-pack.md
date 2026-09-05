# Evidence Pack: WI-20260823-ATS-002

## Work Item

- WI: `WI-20260823-ATS-002`
- REQ: `REQ-20260823-ATS-001`
- Agent: `qa-integ`
- Branch: `codex/v1-release-rehearsal-fixes`
- Baseline reviewed: `3ea2781`
- Result: **FAIL - remediation required**

## Review Boundary

- Read-only review. The only created files are this evidence pack and the paired
  user summary.
- Explicitly excluded from analysis and changed-file formatting scope:
  `frontend/src/pages/public/HomePage.tsx` and
  `frontend/src/pages/public/HomePage.test.tsx`.
- No client-acceptance worktree, login/signup submission, profile mutation, playlist
  mutation, payment/refund, mail, provider, or real media-playback flow was invoked.
- Existing local Vite and backend processes were observed but not restarted or stopped.

## Success-Criteria Matrix

| REQ success criterion | Status | Independent evidence |
| --- | --- | --- |
| Selected mood keeps other mood chips visible; repeated `mood` contract remains intact | PASS | `TrackListPage.tsx:232-243,711-719`; focused `TrackListPage.test.tsx` passed. Browser public check selected `mood01` then `mood02`, yielding `http://127.0.0.1:5173/tracks?mood=mood01&mood=mood02&page=1`; both chips were pressed and `mood03` remained visible. |
| BUSINESS descriptor remains existing `companyName`; INDIVIDUAL job remains separate | PASS | `SignupPage.tsx:105-113,166-174,370-402`; `ProfilePage.tsx:275-307,442-460,632-666`; focused signup/social/profile/admin tests passed. API documentation already lists `companyName` and `job` as existing response fields at `docs/design/api-spec.md:584-593`. |
| Nickname edge trim/internal-space rules are consistent | PASS | `validation.ts:84-94`, `auth.ts:101-104,126-130`, `UserService.java:77-90,121-124,229-241,271-272,412-421`, and `ValidationConstants.java:12-14`; forced backend `UserServiceTest` and focused frontend tests passed. |
| Playlist Play all starts first Track and keeps playlist order in queue | PASS | `PlaylistDetailPage.tsx:241-244,299-304` invokes `playerStore.ts:689-695`; focused playlist tests passed. |
| Likes entry exists beside history on desktop and expanded mobile | FAIL | `PlayerBar.tsx:92-95,571-583,621-639,645-651,862-880,1093-1111,1128-1134` correctly wires the entry. `PlaylistDrawer.tsx:67-71` resets tab state on every open and breaks the existing reopen/async-likes contract. |
| Question FAB clears PlayerBar and expanded mobile player | BLOCKED | `QuestionListPage.tsx:268-271`; `QuestionListPage.module.css:223-252,272-280`; focused component test passed. Browser verification requires the protected Question list and was not attempted under this WI's no-login rule. |
| Safe billing-key local example exposes no secret and keeps keyring shape | PASS | The WI-owned example diff is limited to `application-local.example.yml`; no ignored local config or secret was read or emitted. |
| Full quality gate | FAIL | See command outcomes and confirmed findings below. Documentation validation is deliberately deferred to WI-003 per the handoff packet. |

## Confirmed Findings

### P2: Drawer reopening regresses the selected Likes tab

- Source: `frontend/src/components/player/PlaylistDrawer.tsx:67-71`
- Symptom: a user who selects Likes, closes the generic drawer, and reopens it sees
  Playlists; the latest Likes response is not shown.
- Cause: the new layout effect overwrites state with default `initialTab = 'playlists'`
  whenever `open` changes to true.
- Reproduction:
  `npm run test -- src/components/player/playerComponents.test.tsx -t "prevents an earlier likes response from populating a reopened drawer"`
- Result: failed independently, `1 failed | 28 skipped`, at
  `frontend/src/components/player/playerComponents.test.tsx:607` because `Current Like`
  is absent and the Playlists tab is selected.
- Remediation scope: preserve the user-selected tab for generic reopen while retaining
  explicit PlayerBar entry intent; update/extend the existing stale-response test.

### P2: Broad coverage test was not aligned to the approved BUSINESS descriptor

- Test: `frontend/src/test/coverage/publicAuthShell.coverage.test.tsx:620-621`
- Source behavior: `frontend/src/pages/auth/SignupPage.tsx:105-106,374-380`
- Symptom: the broad suite still searches for the former company-only label/error.
- Reproduction: `npm run test`
- Result: one failure in the public-auth coverage test. The source displays the
  approved combined `Company name or industry` descriptor; this is a stale test
  expectation, not a request to restore the old UI.
- Remediation scope: align the coverage expectation and accessible-label lookup.

### P2: Changed-file Prettier check fails

- Reproduction: `npx prettier --check` over the WI-owned frontend changed-file list,
  excluding both HomePage files.
- Result: non-zero exit. Files reported: `src/pages/auth/SignupPage.test.tsx`,
  `src/pages/auth/SocialCompleteProfilePage.test.tsx`,
  `src/pages/public/TrackListPage.tsx`, and
  `src/pages/subscriber/ProfilePage.test.tsx`.
- Remediation scope: format only the reported WI-owned files and rerun the same check.

## Quality Command Outcomes

| Command | Outcome |
| --- | --- |
| `git branch --show-current` | PASS: `codex/v1-release-rehearsal-fixes`. |
| `git diff --name-status 3ea2781` and scoped numstat | PASS: WI-001 paths match the evidence pack; the two HomePage files were separately identified and excluded. |
| `npm run typecheck` | PASS. |
| `npm run lint` | PASS with zero warnings permitted by the script. |
| Scoped `npx prettier --check` for all WI-owned changed frontend files | FAIL: four files listed above. |
| `git diff --check 3ea2781 -- <HomePage exclusions>` | PASS. Git printed CRLF-to-LF warnings for existing Java/example working-tree files; no whitespace errors. |
| Focused `npm run test --` with the nine WI suites | PASS: `9 files / 131 tests`. |
| `npm run test` | FAIL: `111 files`, `1447 tests`; `108 files / 1444 tests` passed and three failed. Two failures are the confirmed WI-001 findings above; HomePage is explicitly excluded. |
| Targeted Likes regression command | FAIL independently: `1 failed / 28 skipped`, as recorded above. |
| `npm run build` | PASS: TypeScript project build and Vite production build completed. |
| `gradlew.bat test --tests "com.atstudio.atstudio.service.UserServiceTest" --console=plain` | NOT EXECUTED: PowerShell did not resolve bare current-directory executable. No test started. |
| `.\\gradlew.bat test --tests "com.atstudio.atstudio.service.UserServiceTest" --rerun-tasks --console=plain` | PASS: forced execution completed successfully. |
| `.\\gradlew.bat test --rerun-tasks --console=plain` | PASS: forced full backend suite completed successfully. Hibernate cleanup messages are for test-context tables, not application schema/data operations. |
| `.\\gradlew.bat build --console=plain` | PASS: `BUILD SUCCESSFUL`; test task was up-to-date after the forced full test run. |
| Documentation validation | NOT RUN by design: WI-002 handoff requires it after WI-003 documentation alignment. |

## Browser Evidence and Limits

- Public, non-mutating browser verification used only `http://127.0.0.1:5173/tracks`.
  Multi-mood selection passed as recorded above.
- The catalog rendered Track metadata but its visible cover fallback reported that
  media covers could not load. No console errors were captured and no audio control was
  used. Treat this as the known development media/storage mismatch, not a WI-001 source
  regression.
- Protected registration, social completion, profile, playlist, Likes, PlayerBar, and
  Question screens were not browser-tested because the review was prohibited from login,
  signup, or mutation flows. The Question FAB browser check is therefore BLOCKED rather
  than failed.

## Documentation Handoff for WI-003

- `docs/design/usecase/user-info.md:20-29,133-152,257-279`: describe the combined
  BUSINESS descriptor and normalization before frontend/backend availability, duplicate,
  response, and storage paths.
- `docs/design/api-spec.md:562-593,894-903`: state normalization for the user-write
  and nickname-availability contract and clarify that `companyName` remains the
  existing field.
- `docs/design/usecase/sound-playlist.md`, `docs/ui/atstudio-front-list.md`, and
  `docs/ui/screen-flow.md`: align Play all queue behavior, direct Likes entry beside
  history, and Question FAB/player clearance.
- Review the current safe configuration documentation alongside
  `application-local.example.yml`; retain keyring terminology and placeholders only.

## Remaining User Verification

After remediation, verify authenticated BUSINESS and INDIVIDUAL form behavior, edge
trim persistence for a nickname with internal spaces, playlist order during actual next/
previous navigation, Likes drawer reopen behavior in desktop and expanded mobile, and
Question FAB clearance in all specified layouts. These checks must use an approved test
account and must not treat unavailable development media as an application-code failure.
