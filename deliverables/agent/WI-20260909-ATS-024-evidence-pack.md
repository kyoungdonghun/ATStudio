---
version: 1.0
last_updated: 2026-09-09
project: ATS
owner: SE
category: agent
status: stable
dependencies:
  - path: WI-20260909-ATS-024-handoff.md
    reason: Approved delegated frontend scope and backend API contract
  - path: ../user/REQ-20260909-ATS-005.md
    reason: Approved original-preserving audio processing requirements
---

# Evidence Pack: WI-20260909-ATS-024

## Summary

Implemented the 100MiB frontend audio limit and administrator processing status,
bounded refresh, generation-fenced retry, and manual activation protection.
Frontend implementation is ready for WI025/WI026; this is not a runtime deployment.

## Scope / DoD Check

- [x] Creation and replacement use `AUDIO_MAX_SIZE_MB=100`; 104857600 bytes accepted and 104857601 rejected using sized File stubs.
- [x] Image, generic attachment, and certification document limits remain 10/20/20MiB.
- [x] Optional/nullable administrator `audioProcessing` supports the supplied five-state contract without breaking historical fixtures or the pinned old API.
- [x] Upload acceptance and replacement acceptance are displayed separately from conversion readiness.
- [x] Pending, processing, failed, cancelled, and ready statuses; retries only when allowed by the server.
- [x] One request per mounted status instance at a time; 3-second delay after each read, at most 60 automatic reads before manual refresh.
- [x] Hidden tabs, disabled rows/forms, unmounts, read errors, terminal states, and obsolete generations stop or fence status work.
- [x] Retry transmits the observed generation and disables automatic authentication replay. An ambiguous retry requires an authoritative read before another mutation.
- [x] `streamReady=false` blocks activation in both the control and submit handler. READY never activates a Track automatically. A replacement with `streamReady=true` keeps existing playback available.
- [x] Existing list-request and delete-generation/committed-delete recovery code is preserved; the status component is suspended for a delete target.
- [x] Focused Vitest, TypeScript, ESLint, and changed-file Prettier checks passed within the boundaries below.
- [ ] Independent integration, full-suite regression/build, and real browser/deployment verification remain with the parent.

## Reference Documents (Tier 0-2)

| Tier | Document | Context |
|---|---|---|
| 0 | `docs/standards/core-principles.md` | Parent-injected STD001: approved scope, Korean conversation, English technical documents, preserve unrelated work |
| 0 | `docs/standards/development-standards.md` | Parent-injected STD002: existing React/TypeScript patterns, risk-scaled regression |
| 0 | `docs/standards/documentation-standards.md` | Parent-injected STD004: seven metadata fields and truthful evidence boundaries |
| 0 | `docs/standards/glossary.md` | Parent-injected STD005: canonical WI/REQ terms |
| 1 | `docs/policies/security-policy.md` | Handoff pointer; safe diagnostics and protected Track media sections inspected |
| 1 | `docs/policies/quality-gates.md` | Handoff quality-gate pointer |
| 2 | `docs/standards/frontend-standards.md` | Local component state, shared Axios client, CSS modules, existing dependencies |
| 2 | `.agents/skills/react-best-practices/AGENTS.md` | Parent-read/injected React guidance; local SKILL.md also read |
| 2 | `docs/design/usecase/sound-track.md` | Track creation, replacement, manual activation, listing and deletion context inspected |
| 2 | `deliverables/user/REQ-20260909-ATS-005.md` | Approved scope read directly |

Injection: delegated frontend `se`, implementation task. Parent supplied the
skill-generated WI packet. No agents were spawned and no private configuration
was read. Skills read/applied: `react-best-practices`, `test`, `typecheck`,
`eslint`, `prettier`, `create-wi-evidence-pack`. The evidence skill's handoff
precondition was checked again with `Test-Path` (True).

## Evidence Pointers

All paths below are relative to `C:\Users\jm991\Desktop\project\ATStudio`.

| Changed path | Purpose |
|---|---|
| `frontend/src/api/tracks.ts:6` | `AudioProcessing`, nullable compatibility fields, GET status and POST retry wrappers; existing `AdminTrackDetail.audioFile` retained |
| `frontend/src/api/tracks.audioProcessing.test.ts` | ResponseDTO unwrapping, cancellation, generation body, mutation replay opt-out |
| `frontend/src/utils/validation.ts:33` | Shared audio cap, other caps unchanged |
| `frontend/src/utils/validation.test.ts` | Exact/over boundary and unchanged non-audio limits |
| `frontend/src/utils/audioProcessing.ts` | Pending-state predicate and fixed Korean conflict messages |
| `frontend/src/components/track/AudioProcessingStatus.tsx:74` | Single-flight status/retry, request and generation ownership, interruption recovery |
| `frontend/src/components/track/AudioProcessingStatus.tsx:127` | Hidden-tab cancellation, timer/listener cleanup |
| `frontend/src/components/track/AudioProcessingStatus.module.css` | Token-based unframed status, wrapping diagnostics, fixed-size symbol buttons and focus outline |
| `frontend/src/components/track/AudioProcessingStatus.test.tsx` | State, retry, legacy projection, stale/hidden/unmount and bounded polling regression |
| `frontend/src/pages/creator/TrackUploadPage.tsx:265` | Sequential upload lifecycle guard and completed-row preservation; accepted processing stays visible |
| `frontend/src/pages/creator/TrackUploadPage.test.tsx` | 100MiB boundary, queued acceptance, duplicate-submit protection plus existing thumbnail tests |
| `frontend/src/pages/creator/TrackUploadPage.module.css:285` | Wrapping actions for the added management navigation |
| `frontend/src/pages/creator/TrackEditPage.tsx:95` | Manual activation guard from latest processing status; keyed edit route and stale-save protection |
| `frontend/src/pages/creator/TrackEditPage.test.tsx` | Readiness guard, replacement playback, boundary, safe conflict, stale-route response |
| `frontend/src/pages/creator/TrackEditPage.module.css:214` | Wrapping post-acceptance actions |
| `frontend/src/pages/admin/TrackManagePage.tsx:340` | Status only for current rendered rows, suspended during target deletion |
| `frontend/src/pages/admin/TrackManagePage.test.tsx` | Visible-row-only refresh and obsolete retry isolation plus original delete/list tests |
| `frontend/src/pages/admin/TrackManagePage.module.css:188` | Stable status-column width and compact-screen action wrapping |

The component has two evidence locations in this table; there are **17 unique
frontend source/test/CSS files**, plus this evidence pack and the WI024 summary.
No backend, package/lock, baseline REQ004/WI021/WI022/reconstruction, or other
deliverable files were edited by this assignee.

## Commands & Outputs

Workspace: `C:\Users\jm991\Desktop\project\ATStudio\frontend` unless noted.

```powershell
npm.cmd test -- src/components/track/AudioProcessingStatus.test.tsx src/api/tracks.audioProcessing.test.ts src/pages/creator/TrackUploadPage.test.tsx src/pages/creator/TrackEditPage.test.tsx src/pages/admin/TrackManagePage.test.tsx src/utils/validation.test.ts --maxWorkers=1
npm.cmd run typecheck
npm.cmd run lint
node node_modules/prettier/bin/prettier.cjs --check src/api/tracks.ts src/api/tracks.audioProcessing.test.ts src/utils/validation.ts src/utils/validation.test.ts src/utils/audioProcessing.ts src/components/track/AudioProcessingStatus.tsx src/components/track/AudioProcessingStatus.test.tsx src/components/track/AudioProcessingStatus.module.css src/pages/creator/TrackUploadPage.tsx src/pages/creator/TrackUploadPage.test.tsx src/pages/creator/TrackUploadPage.module.css src/pages/creator/TrackEditPage.tsx src/pages/creator/TrackEditPage.test.tsx src/pages/creator/TrackEditPage.module.css src/pages/admin/TrackManagePage.tsx src/pages/admin/TrackManagePage.test.tsx src/pages/admin/TrackManagePage.module.css
```

- Final focused Vitest: exit 0, **6 files / 45 tests passed**, 0 failed/skipped; 15.99 seconds. Initial run: 40 passed / 5 failed due solely to ambiguous new file-input test selectors; selectors were narrowed to `input`, then all 45 passed.
- TypeScript: exit 0 before tests and again after test additions/formatting.
- ESLint: exit 0, zero errors and warnings (`--max-warnings 0`).
- Final Prettier: exit 0 for all 17 changed frontend files. Initial check flagged 8 edited files; only those files were formatted with the local Prettier binary using `--write`, then rechecked.
- Final styling-only changes add action wrapping and the status-column class/width after the focused run. Their formatting passed; visual layout was not exercised in a browser.
- Test-load preflight used `Get-CimInstance Win32_Process`, filtering `node.exe`/`java.exe`, and printed only PID/parent/name plus classified test kind. No Vitest/Gradle test workers were reported at that preflight. Existing process command lines were not printed, and no process was stopped.
- Parent was informed when the focused run ended. After the parent announced backend Gradle/FFmpeg/H2 tests, no additional Vitest or build was started.
- No Git command was executed, including `git diff --check`, as explicitly prohibited. Change inventory comes from this assignee's patch operations and direct source inspection, not a whole-worktree diff or baseline byte comparison.

## Tests

| Suite | Passed | Failed |
|---|---:|---:|
| AudioProcessingStatus | 10 | 0 |
| tracks.audioProcessing API | 2 | 0 |
| TrackUploadPage | 6 | 0 |
| TrackEditPage | 12 | 0 |
| TrackManagePage | 9 | 0 |
| validation | 6 | 0 |
| Total | 45 | 0 |

These are jsdom/unit tests with mocked API calls and sized File stubs; no 100MiB
payload was allocated or posted. They do not establish actual encoder,
Cloudflare, database, storage, player, seek, waveform or original-download hash
behavior. No full-suite Vitest, frontend build, live browser capture, new dev
server, deployment or actual account/catalog mutation was performed.

## API Assumptions / Limitations

- The exact contract is the WI024 packet supplied by backend SE: `audioProcessing`
  with track ID, five-state enum, generation, streamReady, retryAllowed,
  attemptCount, errorCode and updatedAt. GET/POST responses unwrap `data`.
- Undefined/null processing info is silent and causes no status requests. It
  does not assert READY, failure, or a newly converted derivative. Legacy
  activation remains available; authoritative backend activation checks still
  decide the result.
- Polls replace only processing status. Existing edit form fields are not
  overwritten by polling; reopening edit loads the current detail/filename.
- Aborting a retry transport is not proof the server mutation was cancelled.
  Refresh-first recovery handles that ambiguity; obsolete UI responses cannot
  update another row/route.
- Source may HMR in the existing Vite process while the pinned backend remains
  old. No claim is made that the new backend contract is applied at runtime.
- Parent-provided deployment caveat: backend total request cap is 120MB and
  audio cap is 100MiB, but a Cloudflare 100MB total-request cap can reject a
  boundary upload plus multipart/thumbnail overhead. This was not independently
  network-tested here. No chunking is included; WI025 owns deployment guidance.

## Risks / Rollback

- Full-regression and browser/mobile accessibility/layout evidence remain open;
  CSS was only statically checked. Changes to the backend contract must be
  coordinated before deployment, particularly nullable historical responses and
  generation handling.
- Roll back only this WI's frontend hunks/new status/API tests with parent
  approval, preserving concurrent backend work and unrelated user changes.
  Do not reset the shared worktree or delete protected documents. No database,
  storage or process rollback is needed from these frontend-only edits.

## Follow-ups

- Notify parent that WI024 frontend work is ready; release dependent WI025 and
  WI026 through the parent's orchestration chain. No child agents were spawned.
- Coordinate with backend SE before additional focused/full Vitest or build.
  Parent should run complete regression, independent integration and deployment
  validation after backend changes are ready. REQ005 is not closed by this WI.

## Related Documents

- [WI024 handoff](WI-20260909-ATS-024-handoff.md)
- [REQ005](../user/REQ-20260909-ATS-005.md)
- [WI024 summary](../user/WI-20260909-ATS-024-summary.md)
