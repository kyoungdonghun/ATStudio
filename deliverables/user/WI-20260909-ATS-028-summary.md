---
version: 1.0
last_updated: 2026-09-09
project: ATS
owner: qa-integ
category: work-summary
status: stable
dependencies:
  - path: REQ-20260909-ATS-006.md
    reason: Approved actual upload acceptance
  - path: ../agent/WI-20260909-ATS-028-evidence-pack.md
    reason: Independent and supplied evidence boundaries
---

# WI-20260909-ATS-028 Summary

Current phone acceptance and cleanup boundary: see the
[dated user-acceptance follow-up](#user-acceptance-follow-up-2026-09-09).
The initial closeout below is preserved as historical evidence.

Status: COMPLETE WITH RECORDED LIMITATIONS. REQ006 is closed for this bounded
verification, not an all-browser PASS or production approval.

| Check | Result |
|---|---|
| Independent actual stored media | IDs 14-35, 22/22 PASS: fixture-identical originals and 128000bps MP3; batch 59.442834s, small 2s, near-cap about 594.407s |
| Independent existing MySQL | All 22 mappings/READY states match; ID 35 attempt 2; small/near-cap each one row, corrupt zero; nine additive column definitions and queue index match |
| Independent retained files | 78/78 original sizes/hashes unchanged; SQL backup size/hash unchanged |
| Original Tracks | IDs 1-13 bounded seven-field projection preserved; three TSV titles contain encoding replacement characters, while all 13 titles match the frozen SQL dump |
| MA actual UI batch | Supplied capture/snapshot: 20 accepted/READY, no alerts; 294s observed upper bound includes other work |
| MA invalid input/submission retry | Maximum-20 and over-100MiB guards observed; corrupt input rejected, successful ID 34 not duplicated on retry |
| MA near-cap/restart | Public no-thumbnail 104853504-byte upload accepted as ID 35; PROCESSING interrupted, restart recovered FAILED with interrupted code and claim cleared |
| Supplied worker retry | TXT/DB evidence shows READY, attempt 2, error cleared and stream present; later recovered viewport usable. Earlier identical/blank transition PNGs retained as an evidence limitation |
| Post-restart independent preservation | 78/78 file sizes/hashes and 13/13 bounded original projections preserved; playback statistics excluded |
| MA playback/seek/refresh | Reviewed viewport and snapshots support ID 33 playback, about 40s seek and paused 25s restoration |
| MA detail mouse seek/resume | Actual mouse seek 29.607330s -> 31.416236s; refresh 31.472557s -> 31.472544s, manual resume -> 33.000957s |
| MA local/public MP3 transport | 200 audio/mpeg; Range 206 exact slice; invalid Range 416. Reported SHA-256 matches independently probed stored MP3 |
| API original download | Separate public API artifact independently verified: 10485760 bytes and fixture/original-identical SHA-256 |
| Browser original save | UNKNOWN: success toast observed, native event timed out after 60s and expected Downloads file absent. API artifact does not prove browser OS save |
| Final records/UI cleanup | One admin license, download row and download counter for ID 33; all 22 new Tracks READY/inactive after MA UI deactivation, media retained |
| Document quality | validate-docs PASS, 721 supported IDs; scoped diff and new-document whitespace PASS |
| Final closure | Independent post-logout preservation PASS; MA confirms fresh DOM logout, four local/public HTTP 200 checks and unchanged listener PIDs 18924/25196. No pending MA UI work |

Initial JAR/startup evidence was independently read, but browser and process
operations were performed by MA. Schema validate is not strict integrity PASS:
the existing ten missing references remain, with strict startup disabled.

Record-only maintenance: narrow header logout overflow, stale over-limit alert,
English corrupt-file detail under Korean guidance, and unrelated historical
missing-thumbnail/error-serialization logs. No source fixes were made.

Global disposable MySQL manifest remains UNRECORDED. No fresh-install/restore,
audio plus 10MiB-thumbnail multipart, actual mobile, automatic orphan-process
cleanup or production approval is claimed. Native event timeout is not a proven
product bug. No DB or media writes, process/browser control, payment/mail, new DB, commit or push were
performed by qa-integ. Details and private report pointers are in the
[evidence pack](../agent/WI-20260909-ATS-028-evidence-pack.md).

## User-Acceptance Follow-up (2026-09-09)

Under the same approved REQ006/WI028, authorized step 3 now closes only the
human-assisted actual-phone download-save/open-play check: **PASS**. MA's visual
review of the supplied smartphone screenshot identifies `qa_admin`, public
`/tracks/33` / `WI027-sine-20`, the site toast and a native download-complete
notification for `2ae25c112fd34162b1e9a2b1874707c8.wav` with `파일 열기`.
The user then explicitly confirmed local playback: `재생되네 굳`.

Browser brand/version, phone-local bytes/SHA and whole 59-second playback/duration
remain independently unverified. No desktop Chrome/Edge or full mobile regression
PASS is claimed. Earlier in-app native-event UNKNOWN and separate API SHA PASS
remain distinct historical evidence, not a current blanket missing-save check.
MA confirmed follow-up **test33-only deactivation and logout** through fresh CUA
DOM; public `/api/tracks/33` now returns the intended 404. Three other local/public
health checks returned 200. MA's unchanged 10485760-byte original/SHA check is for
the repository original, NOT the phone copy. Admin toast `0/0` is record-only debt.
qa-integ changed only six named documents, with no runtime/DB/browser actions,
code/test changes or commit/push; MA's active-state/UI cleanup is recorded above.
No release approval. See the [follow-up evidence](../agent/WI-20260909-ATS-028-evidence-pack.md#user-acceptance-follow-up-2026-09-09)
for provenance, document checks and remaining boundaries.
