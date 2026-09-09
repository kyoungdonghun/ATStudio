---
version: 1.0
last_updated: 2026-09-09
project: ATS
owner: qa-integ
category: evidence-pack
status: stable
dependencies:
  - path: WI-20260909-ATS-028-handoff.md
    reason: Approved read-only verification and document ownership
  - path: ../user/REQ-20260909-ATS-006.md
    reason: Actual multi-upload acceptance scope
  - path: WI-20260909-ATS-027-evidence-pack.md
    reason: Fixture and probe provenance
---

# Evidence Pack: WI-20260909-ATS-028

## Summary

COMPLETE WITH RECORDED LIMITATIONS. Independently verified all 22 stored runtime pairs, the original
78 files, the bounded original Track projection and the additive schema. MA owns
actual browser actions and process operations. Post-restart checks and API
download artifact hashes pass. List/detail, mouse seek and refresh/resume evidence
has been reviewed. Final post-logout preservation PASS and MA-supplied logout plus
four HTTP 200 results close this bounded WI and REQ006. Browser OS save remains
UNKNOWN; this is not an all-browser PASS or production approval.

## Scope / DoD Check

- [x] Fresh WI028 handoff, approved REQ006 and WI027 evidence read.
- [x] IDs 14-33: real stored originals and full-length 128000bps MP3s, 20/20 PASS.
- [x] Exact existing MySQL independently queried read-only: 20 mappings match.
- [x] Original 78 files: byte counts and SHA-256 unchanged; backup hash matches.
- [x] Original IDs 1-13: seven-field baseline projection corroborated, with the
  three malformed TSV titles checked against the hash-verified SQL dump.
- [x] Nine column types/nullability/defaults and two-column queue index checked.
- [x] Supplied batch, input-boundary and partial-failure snapshots reviewed.
- [x] Supplied interrupted-worker retry TXT and DB snapshots show READY/attempt 2.
- [x] IDs 34/35 actual media verification, 2/2 PASS.
- [x] Supplied playback/seek/refresh and local/public stream reports reviewed.
- [x] API-downloaded original bytes independently match fixture and stored original.
- [x] Post-restart original-file and bounded Track projection recheck.
- [ ] Browser OS-saved download proof: UNKNOWN, not a PASS.
- [x] Final MA detail, mouse seek, refresh/resume and recovered-state viewport.
- [x] MA-supplied final logout/health and independent post-logout preservation.
- [x] Documentation validation and owned-path whitespace checks.

No production approval, fresh database installation, restore rehearsal or strict
integrity PASS is implied. REQ006 is complete only for the recorded bounded
verification. The open OS-save evidence item is an explicitly accepted limitation,
not silently counted as a passing check.

## Reference Documents (Tier 0-2)

| Tier | Document | Use |
|---|---|---|
| 0 | `docs/standards/core-principles.md` | Approval, truthful results, preservation |
| 0 | `docs/standards/development-standards.md` | Scoped verification and traceability |
| 0 | `docs/standards/documentation-standards.md` | English documents, Korean REQ, metadata |
| 0 | `docs/standards/glossary.md` | Public Listening versus Official Download |
| 1 | `docs/policies/security-policy.md` | In-process secrets and private evidence |
| 2 | `docs/design/runtime-storage-operations.md` | Storage tuple and deployment boundaries |
| 2 | `scripts/database/manual-wi023-audio-processing.sql` | Additive retained-data migration |
| 2 | `scripts/validation/wav-browser/README.md` | Five-field pair input and duration tolerance |
| 2 | REQ006, WI027 evidence, WI028 handoff | Approval, upstream evidence and ownership |

MA supplied minimal Tier 0 injection; task-relevant sections were freshly read.
Assignee qa-integ; `.claude/config/context-injection-rules.json` requires tiers
0/1. Project tag ATS confirmed in `.claude/config/workspace.json`.
Skills: create-wi-evidence-pack and validate-docs. No further agents were used.

## Evidence Pointers

Private evidence root, abbreviated `PRIVATE` below:
`%LOCALAPPDATA%/ATStudio/validation/wav-browser-20260909/`.
Runtime log root: `%LOCALAPPDATA%/ATStudio/remote-development-20260908/`.
No credentials, browser tokens, raw object keys or dump contents are reproduced.

| Evidence | Provenance and result |
|---|---|
| `PRIVATE/wi028-pair-results-01.json` | Independent helper run, exit 0, 20 actual runtime pairs PASS |
| `PRIVATE/wi028-pair-results-extra-01.json` | Independent helper run, exit 0, real IDs 34/35 PASS |
| `PRIVATE/stored-pairs.json` | MA mapping; independently matched to existing MySQL IDs 14-33 |
| `PRIVATE/wi028-db-crosscheck-01.json` | First read-only run, exit 1: three baseline title mismatches, otherwise pair mappings/defaults PASS; failure retained |
| `PRIVATE/wi028-db-crosscheck-02.json` | Follow-up, exit 0: all 13 titles match the hash-verified pre-migration SQL dump |
| `PRIVATE/wi028-baseline-01.json` | Independent 78-file size/hash, dump size/hash and nine-column definition checks PASS |
| `PRIVATE/wi028-final-baseline-01.json` | Post-restart independent read-only recheck: 78 files, 13 original projections, 22 READY mappings, ID 35 attempt 2, valid/near-cap each one row and corrupt zero |
| `PRIVATE/wi028-final-records-02.json`, `wi028-final-records-03.json` | Independent counts/preservation snapshot and explicit verdict correction: one admin license/download/counter for ID 33; zero new active Tracks is expected MA cleanup |
| `PRIVATE/wi028-closeout-preservation.json` | Final independent post-MA-logout checkpoint: all 78 originals, all 22 new media pairs, original bounded projection and frozen backup hashes unchanged; 22 READY/inactive |
| `PRIVATE/wi028-ma-closeout-receipt.json` | Attributed receipt of MA's final CUA logout, four HTTP 200 checks and unchanged listener PIDs; not a new qa-integ browser/HTTP run |
| `PRIVATE/private-backup/baseline.json` | Frozen 78-file manifest and 131717-byte, 43-table SQL backup reference |
| `PRIVATE/private-backup/original-tracks.tsv` | Frozen seven-field projection; titles for IDs 1/2/3 contain U+FFFD |
| `PRIVATE/private-backup/migration-observed.txt` | MA migration evidence; definitions and queue index subsequently queried independently |
| `PRIVATE/browser-batch20.json`, `.txt`, `browser-batch20-complete.png` | MA CUA evidence reviewed, including full-page image: 20 accepted/READY, no alerts |
| `PRIVATE/browser-over-limit.txt` | Supplied snapshot reviewed: over-limit input rejected before selection |
| `PRIVATE/browser-mixed-first.txt`, `browser-mixed-retry.txt` | Supplied snapshots reviewed: valid item READY, corrupt item HTTP 400 |
| `PRIVATE/interrupted-worker.json`, `recovery-before-retry.tsv` | MA-observed PROCESSING interruption and subsequent FAILED/claim-clear state |
| `PRIVATE/browser-interrupted-failed.txt`, `browser-retry-complete.txt`, `recovery-after-retry.tsv` | Supplied snapshots independently read: Korean interruption guidance/retry control, then READY/attempt 2 and stream present |
| `PRIVATE/browser-recovered-track35-viewport.png` | Later supplied usable viewport independently reviewed: near-cap Track edit page shows playback READY and inactive state |
| `PRIVATE/browser-playback.png`, `browser-seek.json`, `browser-refresh.json` | Supplied viewport reviewed: green player progress; MA player-time/seek evidence and paused 25-second restoration snapshot |
| `PRIVATE/browser-download-toast.png` | Supplied viewport reviewed: success toast; does not establish browser OS save |
| `PRIVATE/api-downloaded-original-33.wav.json`, `api-downloaded-original-33.wav` | MA public API transfer metadata and downloaded bytes, not browser download output |
| `PRIVATE/wi028-api-download-check-01.json` | Independent local API-artifact bytes/hash comparison against the fixture/original; PASS |
| `PRIVATE/stream-range-probes.json` | MA loopback/public HTTP evidence; reported hashes independently matched to the previously probed stored MP3 |
| `PRIVATE/browser-mouse-seek.json`, `browser-detail-seek.png`, `browser-refresh-resume.json` | Final supplied detail-route mouse-seek, restored-time and manual-resume evidence independently read/viewed |

The same-named interrupted/retry PNGs are byte-identical (SHA-256
`8393C42F6BA43EFB16AD391FBF3C32BB9C54D3BDE8E91EE6B712C2BA59FA15A2`).
The inspected image shows a header and blank content, not the Track state.
These PNGs cannot prove the failure-to-READY transition; rely on the attributed
TXT/DB observations, and retain this capture limitation without rewriting images.
The later recovered-state viewport is usable evidence of the final READY state;
it does not retroactively repair the earlier transition PNGs.

The pair helper report's embedded WI is WI027, its implementation owner. This
WI028 evidence records the independent invocation against actual stored media;
the helper and its schema were not edited or relabeled.

## Commands & Outputs

```powershell
$private = Join-Path $env:LOCALAPPDATA 'ATStudio/validation/wav-browser-20260909'
python -B scripts/validation/wav-browser/probe_pairs.py `
  --records "$private/stored-pairs.json" `
  --output "$private/wi028-pair-results-01.json"
```

Use a fresh output filename on repetition; existing reports must not be replaced.
The actual run exited 0 at approximately 14:01 KST. All 20 originals are
10485760 bytes with fixture-identical SHA-256. All MP3s are stereo/44100Hz,
128000bps. Original and derivative full-decode duration is 59.442834 seconds;
the fixture duration is 59.442834467 seconds. All checks passed within the
declared 0.25-second tolerance. This does not prove per-frame CBR, perceptual
identity or browser/Official Download transport.

Read-only database reproduction: parse ignored `application-local.yml` with
existing `frontend/node_modules/js-yaml` inside the Node process; reject any
target other than `localhost:3306/atstudio`; call the installed MySQL client
with `--no-defaults`, explicit TCP/host/port/schema and UTF-8. Credentials remain
in process/child environment, never output. Each bounded SELECT sequence uses
`SET SESSION TRANSACTION READ ONLY; START TRANSACTION READ ONLY; ...; ROLLBACK;`.
Queries cover only IDs 1-13, IDs 14-33 and the named information_schema entries.
Client stderr and exceptions are not printed with secret-bearing details.

Compare `id,title,audio_file,stream_audio_file,thumbnail,is_active,
audio_processing_state` against the frozen TSV after newline normalization.
The first comparison failed only for titles at IDs 1/2/3. A bounded in-memory
MySQL literal scan of the frozen dump's single `tracks` INSERT verified its
13 rows and matched all 13 titles against a read-only JSON projection. No dump
SQL was executed and no backup was corrected. All other TSV fields matched;
all 13 original rows retain READY/direct playback, generation/attempt zero and
null pending/claim/error fields. This is not equality of every Track column.

At 14:02 KST and in `wi028-baseline-01.json`, all 78 original paths exist with
matching sizes and SHA-256. The backup remains 131717 bytes with SHA-256
`34845d56244c3169de1eeebe99e786da16d87e25a3f99f86ca1000867bc81465`.
Hash-checking a backup does not establish restore validity.

The additional actual-media invocation used the same helper with
`--records "$private/stored-pairs-extra.json"` and fresh output
`--output "$private/wi028-pair-results-extra-01.json"`: exit 0, 2/2 PASS.

| Track | Original bytes | MP3 bytes | Original decode seconds | MP3 decode seconds | Bitrate |
|---|---:|---:|---:|---:|---:|
| 34 | 352844 | 33061 | 2.000000 | 2.000000 | 128000bps |
| 35 | 104853504 | 9511540 | 594.407347 | 594.407370 | 128000bps |

Both original SHA-256 values match their fixtures. The long fixture's declared
duration is 594.407369615 seconds; both complete decodes meet the same 0.25-second
tolerance. The independent post-restart audit repeats original-file hashes,
frozen-backup hashes, TSV non-title comparisons and dump-title comparisons, then
queries all new IDs 14-35 and the exact synthetic title counts. Result: 78/78
files unchanged, 13/13 original bounded projections preserved, 22/22 new mappings
READY, ID 35 attempt 2, small-valid/near-cap each one row and corrupt zero rows.
The original TSV/manifest bytes remain unchanged. Playback statistics are not
part of the seven-field preservation projection; MA's actual playback may change
them. No equality of every historical database column is asserted.

Independent final record query confirms ID 33 has one ADMIN license, one ADMIN
download-history row and download_count 1. MA supplied the temporal observation
that the separate API re-download did not increment the count; qa-integ confirms
the final counts, not an independently observed before/after download sequence.
All 22 new Tracks are READY and inactive after MA deactivated ID 33 through UI,
the only new Track ever activated. All original and derivative files are retained.

`wi028-final-records-02.json` initially returned false because qa-integ had assumed
one new Track would remain active. That was not an approved final-state criterion.
`wi028-final-records-03.json` explicitly corrects the verdict on the same observed
snapshot; the initial report is retained. MA subsequently confirmed intentional
UI deactivation. This is a verifier assumption correction, not a product defect.

The final post-logout checkpoint rehashed all 78 original files and every new
original/MP3 pair against the successful probe reports, verified the frozen
baseline/TSV/dump hashes, and compared the original seven-field projection with
the earlier dump-corroborated DB projection. All passed. Final independent DB
counts: new rows 22, READY/inactive without pending claims/errors 22, ID 35 attempt
2, ID 33 license/download-history/download-counter each 1. No historical playback
statistics were asserted unchanged. The checkpoint predates the later MA final
health receipt; its then-pending health field is preserved as historical context.

## Runtime / Browser Coverage

| Scenario | Current result and boundary |
|---|---|
| Initial deployment | MA applied retained migration and launched PID 27188. Independent log read confirms JPA initialization at 13:51:42.657 and app start at 13:51:51.260 KST; launcher specifies `ddl-auto=validate` |
| Runtime JAR | Independently hashed `ATStudio-wav128-20260909.jar`: `E7802311EE2F25762A8F601D5F9A9CAA2954DFB817D904DB351B0E0DB7B72784` |
| Integrity | Initial log line 242: 30 checked references, 10 missing. Launcher uses strict-on-startup false. Schema validate/startup is not strict integrity PASS |
| Batch 20 | MA actual public-origin CUA: all 20 accepted/READY. 294 seconds is an observed upper bound including other work, not exact transfer latency or throughput |
| Selection cap | MA reports 21st file rejected by Korean maximum-20 message; existing completed items unchanged |
| File cap | 104861696-byte input rejected before selection; no server/proxy rejection claim |
| Mixed input/retry | MA reports small valid ID 34 READY, corrupt input HTTP 400 with no row, repeated actual `2곡 업로드` leaves valid count at one. This is submission retry, separate from worker retry |
| Near-cap/restart | MA reports 104853504-byte WAV accepted through the public route without a thumbnail, ID 35. On PROCESSING/attempt 1, MA stopped owned backend 27188 and one FFmpeg child |
| Recovery | MA restarted exact JAR/config/roots as PID 18924 and reports schema validate/HTTP 200. Independently read JPA initialization at 14:06:04.717 and app start at 14:06:13.327 KST; 75 checked references, same 10 missing. Supplied DB snapshot records FAILED/AUDIO_PROCESSING_INTERRUPTED, attempt 1, claimed 0 |
| Worker retry | MA reports actual refresh and retry click. Supplied TXT shows Korean interrupted guidance/retry control, then READY; independent DB recheck confirms READY, attempt 2, no pending claim/error. Extra-pair probe PASS |
| List playback/seek | MA actual ID 33 playback advanced from about 13s to 33.6s; Home plus eight ArrowRight presses moved the slider to about 40s. Supplied seek JSON records 33.604852 -> 40.005572. Reviewed viewport shows the expected Track and green progress |
| Refresh | Supplied snapshot has beforeReload 25, afterReload 25, expected Track title and Play control: paused restoration evidence, not automatic resumed playback |
| Detail mouse seek/resume | MA clicked waveform center: 29.607330s -> 31.416236s after about 1.8s. Supplied viewport shows detail/player progress. Paused 31.472557s -> reload 31.472544s -> manual resume 33.000957s after about 1.8s, then paused |
| MP3 transport | MA reports loopback and public GET: 200 audio/mpeg, 952154 bytes; SHA-256 matches independently probed ID 33 derivative. Both Range responses: 206, bytes 10000-19999/952154, exact slice; invalid Range 416. Requests were not made by qa-integ |
| Browser download | MA clicked the actual browser control; reviewed viewport shows a success toast. Native download-event wait timed out at 60s and expected Downloads file was absent. Browser OS save/bridge remains UNKNOWN, not PASS |
| Separate API download | MA authenticated public GET for ID 33 returned 200, attachment, application/octet-stream with WAV bytes. Independent saved-artifact hash/length check PASS: 10485760 bytes, fixture/original-identical SHA-256. Not evidence of a browser-saved file |
| UI cleanup | MA deactivated only new ID 33 via UI; independent query confirms all 22 new Tracks inactive. New rows/media remain, with no old-row deletion |
| Final MA closeout | MA confirms fresh public HomePage DOM has Login and no admin identity after actual UI logout. Local 5173 root, local 8080 Track API, public root and public Track API all HTTP 200; listener PIDs 18924/25196 unchanged. Independent post-logout preservation PASS. No broad queue regression or independent token-revocation proof |

Initial startup log:
`backend-wav128-20260909-135133-949-7ee104e248e54269914e9c83f23d9f03.out.log`,
lines 38, 41-42 and 242. The additive SQL contains only the nine ADD COLUMN
operations and queue index, not destructive DDL or backfill. Independent queries
confirmed exact types/nullability/defaults and index order
`audio_processing_state,id`. Retained ALTER ordinals differ from a fresh schema;
`DisposableMysqlBootstrap.java` remains UNRECORDED and was not changed.

Restart log:
`backend-wav128-20260909-140555-659-ca46d204cd094d69aef8c8401b03cd45.out.log`,
lines 38, 42 and 174. Process stop/start was performed by MA, not qa-integ.

API downloaded original SHA-256:
`418ebe155705b5c463462f4ff9c8d0e7aa3a477646d73854a5a0c374ab84956c`.
The success toast cannot promote native browser-download uncertainty to PASS.
MA reports that the list subsequently advanced from ID 33 through demo Tracks to
historical missing Track 3; its later playback-error toast is not attributed to
new synthetic-file corruption. Broad next/queue behavior is not independently
tested here.

## Maintenance Observations

MA-supplied observations only, not independently reproduced browser defects:

- Header at 1094px: logout offscreen; keyboard Enter still works.
- Previous over-limit alert remains after a valid selection until submission.
- Corrupt-file item shows English `Request failed with status code 400` under
  Korean top-level guidance; visible in supplied mixed-input snapshots.
- Historical missing-thumbnail ResourceNotFound followed by
  HttpMessageNotWritable logs is not attributed to this audio feature.

These are record-only maintenance items, not authorization for source fixes.

## Tests / Quality

No Java/React/helper source changes and no new build or test-suite execution.
WI027's nine synthetic helper tests remain upstream evidence, distinct from the
independent actual-media probe here.

- `python -B .agents/skills/validate-docs/scripts/validate_docs.py`: exit 0;
  all Tier 0, links, 721 supported traceability IDs and index checks PASS.
- `git diff --check --` the two WI028 outputs, REQ006 and runtime-storage
  document: exit 0. This does not inspect untracked additions.
- Separate direct trailing-whitespace scan of both new documents: zero findings.

Exactly four repository documents were changed by qa-integ:

- `deliverables/agent/WI-20260909-ATS-028-evidence-pack.md` (new).
- `deliverables/user/WI-20260909-ATS-028-summary.md` (new).
- `deliverables/user/REQ-20260909-ATS-006.md` (bounded closure).
- `docs/design/runtime-storage-operations.md` (dated append-only addendum).

Generated private JSON reports are individually listed in Evidence Pointers.
No other repository file or private-backup content was changed by qa-integ.

## Risks / Rollback

- MA performed CUA and process actions. Reviewing supplied captures is not an
  independently executed browser scenario.
- The original 79 dirty changes are a supplied starting boundary, not a
  byte-frozen shared-worktree claim. No unrelated source or prior evidence edits.
- No DB/schema writes, browser control, runtime process control, payment/mail,
  new DB, media mutation, agent delegation, commit or push by qa-integ.
- MA explicitly stopped the child during interruption; automatic OS orphan
  containment is not proven. Full-size uploads with thumbnails/other proxy
  configurations are not established by the no-thumbnail near-cap sample.
- No fresh DB, audio plus 10MiB-thumbnail multipart boundary or actual mobile
  acceptance was run. The native browser download event timeout is an evidence
  gap, not a proven product bug; browser OS save remains UNKNOWN.
- Keep all new reports, including the failed first projection comparison. Do
  not rewrite private-backup, global manifest, media or old evidence; no
  destructive rollback or old-binary deployment is authorized.

## Follow-ups

WI028 blocks no further WI; WI027 and WI028 are complete and REQ006 is closed.
MA explicitly authorized bounded closure after final logout/health and the
preservation checkpoint, with browser OS-save UNKNOWN retained. The final health
observations are attributed to MA, not independently executed by qa-integ.
No pending MA UI work remains for this WI. Any further OS-save investigation,
maintenance fix or expanded acceptance scope needs separate authorization.
