---
version: 1.0
last_updated: 2026-09-05
project: ATS
owner: MA
category: evidence-pack
status: stable
dependencies:
  - path: WI-20260905-ATS-002-handoff.md
    reason: Approved runtime and browser scope
  - path: ../user/REQ-20260905-ATS-001.md
    reason: Release-verification closeout boundary
  - path: WI-20260905-ATS-004-evidence-pack.md
    reason: Scoped drawer and nickname corrections
  - path: WI-20260905-ATS-005-evidence-pack.md
    reason: Browser Origin diagnosis
---

# Evidence Pack: WI-20260905-ATS-002

## Summary

Current scoped regression and real-browser playback checks passed on the
development checkout. The pending WI-20260823-ATS-010 refresh scenario is now
verified on the configured localhost Origin. No new playback implementation
change was required. This is not production deployment or provider acceptance.

## Scope / DoD Check

- [x] Verify branch, owned processes, existing DB and explicit storage roots.
- [x] Run final automated checks after the WI-004 corrections.
- [x] Observe actual browser playback, seek, pause, reload, history and next-track behavior.
- [x] Distinguish visible-list end behavior from queue repeat-all behavior.
- [x] Preserve historical missing objects and unrelated worktree artifacts.
- [x] Record remaining external and deployment gates without a production GO claim.

## Reference Documents

The WI handoff's Tier 0 inputs were `docs/standards/core-principles.md`,
`development-standards.md`, `documentation-standards.md`, and `glossary.md`.
Tier 1 inputs were `docs/policies/security-policy.md` and `quality-gates.md`.
Tier 2 inputs were `docs/design/runtime-storage-operations.md`,
`scripts/acceptance/README.md`, `frontend/src/layouts/PlayerBar.tsx`,
`frontend/src/store/playerStore.ts`, and the dated
`deliverables/user/WI-20260823-ATS-010-summary.md`.
The create-wi-evidence-pack skill was applied after reading the existing handoff.

## Runtime Evidence

- Date: 2026-09-05, Asia/Seoul. Workspace:
  `C:/Users/jm991/Desktop/project/ATStudio`.
- Branch: `codex/v1-release-rehearsal-fixes`; pre-commit HEAD
  `69d0226a2656c82c8ecde4b6577c642dc42e12b2` plus the reviewed pending changes.
- Initial development listeners were absent. MA started hidden backend PID
  29008 and Vite PID 28724 from this checkout only. No client runtime or
  Cloudflare tunnel was started or stopped for this verification.
- Backend used the explicit `local` profile and root `application-local.yml`;
  child-process overrides bound public/private roots to this checkout's
  `uploads` and `private-uploads`, disabled QA bootstrap and SQL output, and
  removed `createDatabaseIfNotExist=true` from the effective JDBC URL.
  No ignored configuration file was edited.
- Existing DB `atstudio` was inspected read-only: 43 tables, 511 columns,
  175 distinct indexes (232 index-column entries), 91 FK columns, 6 plans.
  The storage mutation journal had 30 DONE rows and no pending rows before
  startup and on the later read. These aggregates are not a full MySQL
  schema-manifest equality comparison or a backup/restore exercise.
- After the final build, MA verified PID/command-line/listener ownership,
  stopped only backend PID 29008 and started PID 30612 with the same tuple.
  Hibernate validation and application startup passed at 16:47:13 KST.
- Both starts reported 30 checked storage references, 20 available and 10
  missing. The missing set remained Track 1-3 audio/thumbnail, Album 1-3
  thumbnail and NoticeAttachment 1 private attachment. No restoration,
  deletion, copy, schema change or record rewrite was performed.
- Local mode intentionally warns on those historical records. Their presence
  would fail a strict acceptance/production audit; this development tuple is
  not certified as a deployable production data snapshot.
- Final local page and proxied API probes returned HTTP 200.
- Final restarted-server Range probes returned 206 for healthy Track 4 and
  500 for historical missing Track 1. The known missing-object failure was
  preserved, not reported as repaired.
- After verification, MA rechecked process command lines and listeners and
  stopped only its backend PID 30612 and frontend PID 28724. Ports 8080 and
  5173 had no listeners afterward. The user-owned browser tab was retained.

## HTTP Evidence

Logs are local-only under
`C:/Users/jm991/AppData/Local/ATStudio/release-check-20260905/`.
They are not tracked artifacts or claimed remotely downloadable attachments.

| Probe                                          | Actual result                                                                                         | Boundary                                                                                          |
| ---------------------------------------------- | ----------------------------------------------------------------------------------------------------- | ------------------------------------------------------------------------------------------------- |
| Existing AT.M Demo Track IDs 4-13              | All ten full streams 200, Range streams 206, thumbnails 200                                           | `media-http.json`; Range bytes 0-1023/112044; current fixture availability, not historical repair |
| ADMIN storage integrity                        | 200, counts 30/20/10 before and after final restart                                                   | Existing QA login; role ADMIN confirmed from `/api/users/me`; token revoked via logout afterward  |
| USER / anonymous storage integrity             | 403 / 401                                                                                             | `http-security.json`; no privilege bypass                                                         |
| CORS preflight                                 | localhost Origin 200 with matching allow-Origin; 127.0.0.1 and untrusted Origin 403                   | Same log; no allowlist expansion                                                                  |
| Public `POST /api/tracks/batch`, `{"ids":[4]}` | localhost Origin 200; 127.0.0.1 Origin 403 `Invalid CORS request`                                     | A successful no-Origin request does not certify browser access                                    |
| Nickname availability after restart            | Existing canonical nickname and the same nickname wrapped in NBSP/BOM both returned `available=false` | Read-only public lookup; raw nickname/credentials not recorded; no profile mutation               |

Normal QA API login/logout changes authentication session state. Browser
playback can update browser history and ordinary playback counters. Therefore
this is not described as a zero-write exercise; no account/catalog/payment
business data editing or migration was intentionally performed.

## Actual Browser Evidence

The existing Codex in-app tab was used through CUA. No hidden browser state was
injected, no player functions were invoked directly, and no media events were
simulated. Screenshots and accessibility observations are in this task's tool
history; no separate screenshot file is claimed.

| Scenario                           | Observed outcome                                                                                                                                                                                                         |
| ---------------------------------- | ------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------ |
| Initial 127.0.0.1 refresh          | Empty player reproduced. POST lookup was rejected by the configured backend Origin allowlist. Browser-tool localStorage visibility was inconclusive and was not treated as proof of unavailable page storage.            |
| Documented localhost Origin        | Guest Track 4 plays, displays a green waveform, pauses, seeks with the slider and restores the same Track at 0:05 / 0:07, paused, after reload.                                                                          |
| Final code and restarted backend   | The same Track 4 play -> pause -> slider Home/ArrowRight -> reload sequence passed again after all source edits and final build/coverage. Screenshot shows the green five-second fill and Play button.                   |
| Playback history                   | After final reload, the dialog retained the actual September 5 entries for Tracks 4, 7, 12, 11, 10 and 9. No history was cleared.                                                                                        |
| Natural next-track on visible list | With repeat off and ten healthy demo rows, playback starting at Track 9 automatically advanced through 10, 11 and 12; paused Track 12 at about 4.26 seconds. No HMR occurred during this sequence.                       |
| Visible-list end with repeat all   | Last visible Track 7 stopped at 0:07 despite the repeat-all label. Source `playerStore.ts` explicitly prioritizes visible-list end stopping over queue repeat. This existing behavior is NOT recorded as list-wrap PASS. |
| Queue repeat outside list          | After navigating to Home, resuming the ended Track 7 wrapped the queue to Track 4 and continued playing; paused at about one second. Queue repeat-all PASS.                                                              |
| Likes drawer, desktop              | Parent Likes -> child Playlists -> parent Likes kept the drawer open on Likes; same visible tab action then closed it. Actual shared components were used.                                                               |
| Multiple Mood filters              | Calm then Bright remained selected and visible even with zero matching AND results. URL retained both `mood` parameters.                                                                                                 |
| Home copy                          | Creator wording and footer were visible; focused and full tests also assert the complete intended strings.                                                                                                               |
| Mobile 390 x 844                   | Expanded player waveform, volume and action buttons were visible without overlap. Volume Home/End interaction completed; manual drawer-tab/parent-Likes sequence passed. Viewport override was reset afterward.          |

The visible-list-end/repeat-label mismatch is a bounded maintenance/product
clarification item, not a storage failure or a newly introduced regression.
This WI does not silently change the existing SR-83 navigation policy.
Authenticated profile, inquiry FAB, subscriber purchase and administrator
mutation workflows were not newly browser-retested in this narrow pass;
their prior evidence and current automated regression remain separate.

## Commands and Final Results

Commands ran from the development checkout; npm commands ran in `frontend/`.
Final runs below followed the WI-004 source freeze. Earlier green runs are not
substituted for these results.

| Command                                                                | Result / local log                                                                                                   |
| ---------------------------------------------------------------------- | -------------------------------------------------------------------------------------------------------------------- |
| `gradlew.bat build --console=plain`                                    | PASS, 2m55s; `backend-final.log`; JUnit XML: 1689 total, 1670 executed successfully, 19 skipped, 0 failures/errors   |
| JaCoCo gates in build                                                  | PASS; lines 87.374%, methods 85.261%, branches 72.338%                                                               |
| `npm run test:coverage -- --maxWorkers=2`                              | PASS, 112 files / 1458 tests, 234.78s; `frontend-coverage-final.log`                                                 |
| Vitest coverage                                                        | statements 90.10%, lines 92.66%, functions 91.04%, branches 82.38%; thresholds passed                                |
| `npm run typecheck`, `npm run lint`, `npm run format`, `npm run build` | Each exit 0; `frontend-quality-final.log`                                                                            |
| `scripts/acceptance/test-dry-run.ps1`                                  | PASS; synthetic/parser/ownership checks, not a real public runtime; `acceptance-dry-run.log`                         |
| `scripts/acceptance/test-backend-environment.ps1`                      | PASS, ten checks; `acceptance-environment.log`                                                                       |
| `scripts/database/test-bootstrap-guards.ps1`                           | PASS; `database-guards.log`; no actual DB created                                                                    |
| `bootstrap-disposable-mysql.ps1 -Action Preflight`                     | PASS, source count 43; `database-preflight.log`; returned before DB credentials/connection; no disposable DB created |

Eighteen skipped JUnit cases require separately enabled disposable-MySQL
proof environments. One LocalStorageService symlink case was aborted because
symbolic links were unavailable on this Windows environment. They are not
reported as passed tests. Current real-DB startup validation is separate from
those concurrency/proof suites. Hibernate table-drop statements in test logs
belong to test fixtures, not deletion of the development MySQL database.

Vite watched generated coverage HTML during the full suite and reloaded the
open page. Browser progression evidence was gathered in a source-stable window,
and the final reload evidence was repeated after coverage completed.

## Risks / Rollback / Follow-up

- No playback, storage, DB, CORS or ignored-config correction was made by MA.
  WI-004 owns the two scoped code corrections and their rollback boundary.
- Preserve unrelated client worktree, historical evidence, ZIP, audit output,
  patch directory, local logs and secrets. Commit only reviewed explicit paths.
- WI-003 updates the active operational evidence. Actual Toss recurring/refund
  and delivery flows, enabled OAuth callbacks, target HTTPS/proxy/secret setup,
  DB-plus-media recovery, single scheduler ownership and operator response
  still require their own closing evidence. No production GO is issued here.
- Document validation passed with 665 supported traceability IDs and no broken
  links/index errors; `git diff --check` passed. Scoped staged review and
  commit remain the final MA closeout gates.
