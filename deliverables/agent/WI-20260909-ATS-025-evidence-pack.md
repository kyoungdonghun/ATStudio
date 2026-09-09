---
version: 1.0
last_updated: 2026-09-09
project: ATS
owner: DocOps
category: evidence-pack
status: stable
dependencies:
  - path: WI-20260909-ATS-025-handoff.md
    reason: Skill-generated approved delegation and parent native-tool evidence
  - path: ../user/REQ-20260909-ATS-005.md
    reason: Approved source-only audio scope
  - path: WI-20260909-ATS-024-evidence-pack.md
    reason: Received frontend verification with explicit mock/runtime boundaries
---

# Evidence Pack: WI-20260909-ATS-025

WI Status: **CLOSED (documentation contract and validation only)**.
REQ005 and WI026 final integration/release gates remain open.

## Summary

Aligned current audio API/use cases/schema/operations with WI023/WI024 source,
including parent-directed codec compatibility clarification, while retaining
historical manifests and pending deployment/test gates.

## Scope / DoD Check

- [x] 100MiB audio/120MB multipart, original download, full-length MP3 CBR 128kbps, no preview/backfill/automatic activation.
- [x] Exact ADMIN fields/routes, five states, generation/claim retry, bounded polling and existing-playback replacement behavior.
- [x] Nine Track columns/index and exact additive SQL source; historical 43-table/511-column proof retained, current expectation UNRECORDED.
- [x] External FFmpeg variables, safe errors, timeout/output bounds, single-server halt/restart/manual-retry procedure.
- [x] Revised derivative-only nearest compatible rate (lower on tie), at-most-stereo output and matching worker validation inspected/documented. Earlier rejection is superseded, not approved product policy.
- [x] Parent codec-compatibility focused 61/61 PASS received, including all ten actual app-pipeline cases; no deployment inference.
- [x] WI026 follow-up source inspected: 256MiB estimated full-output budget including 64KiB framing, pre-start AUDIO_OUTPUT_TOO_LARGE, child-environment allowlist and fixed Korean FAILED reasons.
- [x] Parent synthetic benchmark/provenance and Cloudflare cap separated from service installation, MySQL, browser and production evidence.
- [x] Documentation/metadata/whitespace checks PASS; eight protected pre-existing documents byte-identical by SHA-256.
- [x] Parent full frontend Vitest and bounded backend/native app-pipeline results received and distinguished from CLI evidence.
- [x] Parent final SE focused 50/50 PASS received: encoder 28, pipeline 20 including actual FFmpeg 10, configuration 2; output-budget/environment confirmed.
- [x] Parent reports known-ten-code UI component 26/26, typecheck/scoped ESLint/3-file Prettier PASS and favorable CR source review.
- [ ] Parent first full Gradle: 1885 total / 7 failed / 19 skipped; seven concurrency fixtures under investigation. Final full-regression acceptance and frontend build remain PENDING in WI026.
- [ ] Required WI026 independent review is underway, not closed; separately approved runtime rollout remains open, no release GO.

## Reference Documents (Tier 0-2)

| Tier | Document | Applied context |
|---|---|---|
| 0 | `docs/standards/core-principles.md` | Injected STD001 approved scope, preserve prior work, no overclaim |
| 0 | `docs/standards/documentation-standards.md` | Injected STD004 seven metadata fields, links, dated evidence boundaries |
| 0 | `docs/standards/development-standards.md` | Injected STD002 reuse Java/React/API/source contract, no architecture change |
| 0 | `docs/standards/glossary.md` | Injected STD005 canonical WI/REQ, Public Listening vs Official Download |
| 1 | `docs/policies/security-policy.md` | Secret-safe diagnostics and protected Track media inspected |
| 1 | `docs/policies/quality-gates.md` | Approval and evidence-boundary requirements |
| 2 | `docs/design/usecase/sound-track.md` | Create/listen/download/replace/admin behavior |
| 2 | `docs/design/api-spec.md` | Routes, response fields and source counts |
| 2 | `docs/design/db-schema.md` | Track columns and historical manifest |
| 2 | `docs/design/runtime-storage-operations.md` | Environment tuple, integrity, journal and runtime gates |
| 2 | `scripts/database/README.md` | Current UNRECORDED guard and retained-data source-only SQL |
| 2 | `.agents/skills/create-wi-evidence-pack/SKILL.md` | Handoff existence/read precondition satisfied; this pack follows its output sections |
| 2 | `.agents/skills/validate-docs/SKILL.md` | Bundled read-only documentation validator |

Injection: parent-supplied skill-generated WI025 packet and Tier0/security
constraints; assignee `docops`, documentation task. Loaded governing docs and
matching source as needed. No agents spawned. No private configuration,
external private backup or runtime media inspected. Memory was used only to
orient the initial search; current files, not older proposed column names,
determine the contract.

## Evidence Pointers

| Changed path | Change |
|---|---|
| `docs/design/usecase/sound-track.md` | Metadata, create/replace/listen/readiness and admin feedback |
| `docs/design/api-spec.md` | 155 routes, status/retry contract, processing/public projection and historical manifest caveat |
| `docs/design/db-schema.md` | Exact nine-column/index source, direct legacy defaults, pending migration |
| `docs/design/runtime-storage-operations.md` | FFmpeg/input/errors/recovery, deployment matrix, parent native-tool provenance |
| `scripts/database/README.md` | Metadata, UNRECORDED current guard, preserved historical values and additive retained-data procedure |
| `docs/design/index.md` | v2.7 metadata/links/status; API v30.14 and 155 recounted mappings, DB v24.4/UNRECORDED, operations v1.4/runtime pending |
| `deliverables/user/REQ-20260909-ATS-005.md` | Korean WI025 progress and open gates only; approval/scope preserved |
| `deliverables/agent/WI-20260909-ATS-025-evidence-pack.md` | This skill-standardized evidence |
| `deliverables/user/WI-20260909-ATS-025-summary.md` | Compact parent/user handoff |

No new document under `docs/` was introduced; category file counts are unchanged.
Parent added the stale design index to this WI's scope; its version links,
source counts and runtime status were aligned. The root index's explicitly
labeled pre-WI014 historical snapshot was left intact. The two new deliverables
link to the existing packet and REQ. Only the nine paths above were patched.

Authoritative source inspected (not modified):

- `src/main/java/com/atstudio/atstudio/entity/Track.java`: readiness, generation, claim ownership, retry/cancel/direct behavior.
- `src/main/java/com/atstudio/atstudio/controller/TrackController.java`: ADMIN GET 200/retry 202, existing stream Range and original download route.
- `src/main/java/com/atstudio/atstudio/dto/track/AudioProcessingResponse.java` and `AudioProcessingRetryRequest.java`: exact fields and request validation.
- `src/main/java/com/atstudio/atstudio/dto/track/TrackResponse.java`, `service/TrackService.java`, `service/DownloadService.java`: projections, locked replacement and unchanged original download.
- `src/main/java/com/atstudio/atstudio/service/audio/FfmpegAudioEncoder.java`, `TrackAudioProcessingWorker.java`, `TrackAudioProcessingTransactions.java`, `AudioAnalysisService.java`, `AudioTranscodeException.java`: encoder policy/errors, decoded checks and durable recovery.
- `src/main/java/com/atstudio/atstudio/service/audio/TrackAudioProcessingWorker.java`, `src/main/resources/application.yml`: latest worker-owned private executor (not a Spring scheduler bean), shutdown wait and public-source environment mappings; no local/private values read. The earlier separate AudioProcessingConfig source was superseded during parent integration.
- `src/main/java/com/atstudio/atstudio/service/storage/StorageIntegrityService.java`, `StorageReferenceChecker.java`: all four audio references retained/audited, nullable pending derivative distinction.
- `src/main/resources/schema.sql`, `scripts/database/manual-wi023-audio-processing.sql`, `scripts/database/DisposableMysqlBootstrap.java`: nine-column/index parity and current UNRECORDED vs historical pre-feature values.
- `frontend/src/api/tracks.ts`, `frontend/src/components/track/AudioProcessingStatus.tsx`, `frontend/src/utils/audioProcessing.ts`, frontend/backend validation constants: optional compatibility, poll bounds, fixed UI labels and exact cap.
- `src/test/java/com/atstudio/atstudio/service/audio/FfmpegAudioEncoderTest.java` and `src/test/java/com/atstudio/atstudio/service/storage/TrackAudioPipelineIntegrationTest.java`: revised mapping test source and ten environment-gated native cases inspected only. No test execution inferred from source.

## Commands & Outputs

Workspace: repository root. Only source/document reads, patching allowed docs,
read-only status/validation and public reference browsing were used.

- Read-only `git status --short --branch`: `main`; existing shared changes observed and left intact. No Git state mutation.
- PowerShell source recount repeated after the index request: 26 controllers, 155 mappings (GET 79, POST 42, PUT 20, DELETE 14, PATCH 0), TrackController 12; 43 CREATE TABLE statements, 43 JPA entities. The index's old 150 was already stale, so 152 is not the current count. Static source counts only.
- `python -B .agents/skills/validate-docs/scripts/validate_docs.py`: exit 0; Tier0 files, internal links, 718 traceability IDs and document index PASS, no warnings/errors.
- Scoped `git diff --check` requested by WI025 handoff: exit 0 for all nine changed paths. Git warned that existing CRLF working copies of `design/index.md` and `sound-track.md` will normalize to LF on a future Git write. No staging/commit/reset/push or normalization operation performed.
- PowerShell seven-field metadata presence, dependency-path existence and trailing-whitespace checks: 9 files, 0 errors. The whitespace check includes new/untracked deliverables that ordinary `git diff` does not cover. This is not a general YAML schema validator.
- SHA-256 before/after comparison: 8/8 protected files unchanged: REQ004, reconstruction README, WI021/WI022 handoff/evidence/summary. Private backup/config were not read or touched; no hash claim is made for those external private assets.

Public primary references checked 2026-09-09:

- [FFmpeg download](https://ffmpeg.org/download.html) links Gyan Windows binaries; [publisher builds](https://www.gyan.dev/ffmpeg/builds/) identifies 9.0.1.
- [Publisher ZIP checksum](https://www.gyan.dev/ffmpeg/builds/packages/ffmpeg-9.0.1-essentials_build.zip.sha256) matches the parent-provided `FEC81AE03971D9DD4BE3EBE02E263BD2EC1D789483F931BDBA5F5715E65DA2E9`. This verifies public provenance text, not local downloaded bytes.
- [libmp3lame](https://ffmpeg.org/ffmpeg-codecs.html#libmp3lame): build dependency/CBR/ABR options; supported application rates are sourced from Java, not inferred from library capabilities.
- [Cloudflare Error 413](https://developers.cloudflare.com/support/troubleshooting/http-status-codes/4xx-client-error/error-413/): Free/Pro 100MB, potentially lower configured zone cap. No proxy changes/network upload test.

## Tests

No Java, frontend, SQL, database, encoder, guard-suite or runtime test was run
by WI025. Received WI024 evidence reports focused Vitest **6 files / 45 tests**,
TypeScript, ESLint and 17-file formatting PASS. Those are mock/jsdom/sized File
checks, not uploaded 100MiB payloads.

Later parent results received during WI025, not rerun or log-audited here:

| Evidence | Supplied result | Boundary |
|---|---|---|
| Full frontend Vitest | 114 files / 1584 tests, 0 failed, 239.13s, `--maxWorkers=1` | Before WI026 friendly-reason changes; unit/jsdom, not actual browser/large payload deployment |
| Earlier broader backend run | 210 total / 209 passed / 1 Windows symlink skip | Before worker-private-executor adjustment; historical test boundary, not final closure |
| Codec-compatibility focused run | 61/61 PASS, including all ten actual app-pipeline cases | Parent-reported, not DocOps-rerun; final output-budget/environment fixes came afterward |
| Final SE focused run | 50/50 PASS: encoder 28, pipeline 20 including actual FFmpeg 10, configuration 2 | Parent confirms output-budget/environment guards; focused local tests, not full Gradle/deployment |
| Known-code Korean UI reasons | Component 26/26, typecheck/scoped ESLint/3-file Prettier PASS | Latest parent-reported focused UI checks; no browser deployment claim |
| Frontend build | PENDING | Parent reports finishing, no final result received |
| First backend full Gradle | 1885 total / 7 failed / 19 skipped | Parent-reported; seven concurrency fixtures under SE cause/test-consistency investigation, final acceptance not granted |

Full frontend log pointer supplied by parent:
`%LOCALAPPDATA%/ATStudio/validation/wav128-20260909/frontend-full-tests.log`.
Do not infer final build/closure from these bounded results.

After the parent's codec clarification, the revised source was directly
re-read: output rate is nearest to 16000/22050/24000/32000/44100/48000Hz (lower
on exact tie), channels `min(inputChannels, 2)`, original unchanged. The ten
native app-pipeline test cases now include 96kHz/stereo, 44.1kHz/six-channel,
96kHz/six-channel and 8kHz/mono. They check retained original hash, READY,
output rate/channels, 128000bps, size and full duration, and require
`ATS_TEST_FFMPEG_PATH`. Parent subsequently reported all ten cases PASS within
the 61/61 focused run. That supplied result, not source presence or the older
six-case run, is the execution evidence. Parent subsequently supplied the
known-code UI component 26/26 and typecheck/scoped ESLint/3-file Prettier PASS.
Final SE focused 50/50 PASS then confirmed the output-budget/environment fixes,
including all ten actual FFmpeg cases. Full Gradle/frontend-build results are
still pending in [WI026](WI-20260909-ATS-026-evidence-pack.md). Parent's first
full Gradle result is 1885 total / 7 failed / 19 skipped; the seven concurrency
fixture failures require investigation, not an assumption that the source is
correct or that the fixtures alone are wrong.

Post-WI026 source correction inspected: `outputByteLimit` rejects an estimated
complete derivative above 268435456 bytes (including 65536 framing bytes)
before starting FFmpeg, using overflow-safe quotient/remainder arithmetic.
`AUDIO_OUTPUT_TOO_LARGE` is not truncation or a new original upload cap. The
child environment retains only SYSTEMROOT/WINDIR/SYSTEMDRIVE/PATH/PATHEXT/
TEMP/TMP/LANG/LC_ALL/LC_CTYPE/TZ, removing other inherited server variables,
FFREPORT and JAVA_TOOL_OPTIONS. `audioProcessingFailureMessage` maps known
FAILED codes to Korean reasons and unknown codes to a safe generic fallback.
Parent reports favorable CR source review and final SE guard/environment
coverage PASS within the 50/50 run. UI focused evidence is the separate 26/26 row.

Parent native-tool results are preserved in
[operations](../../docs/design/runtime-storage-operations.md#parent-native-tool-evidence):
52920078 -> 4801140 bytes, 300s, 128000bps, 44100Hz/stereo, 1.470s; near-cap
104076078 -> 9440905 bytes, 590s, 128000bps, 2.805s. Both original hashes
unchanged. These are supplied local synthetic checks, not WI025 reruns,
music listening quality, capacity, application browser or MySQL installation.

## Risks / Rollback

- New-schema manifest is UNRECORDED even at 43 tables. Historical 511-column
  proof/private backup is pre-feature; no invented replacement values or PASS.
- Pinned backend remains old; Vite HMR can show 100MB while backend still uses
  30MB. Cloudflare request overhead and service FFmpeg capabilities need their
  own rollout evidence. Worker startup is not an encoder capability preflight.
- No blanket retry for an unresolved failure or missing input. Valid high-rate/
  multichannel WAV is not excluded by product policy; revised derivative-only
  compatibility normalization is now in source and parent reports ten native
  app cases plus final resource/environment guard tests PASS. A child-termination
  failure halts the worker and retains its claim, not a normal persisted FAILED
  error; old worker/child ownership must be resolved before restart.
- Old binary rollback over stream-required rows is unsafe. Real deployment
  needs approved stopped-writer additive SQL plus tuple/backup preservation,
  actual Hibernate/strict integrity and browser validation. No new DB is
  required to retain/install into the existing service.
- Document rollback: only reverse this WI's approved hunks after comparison,
  preserving concurrent edits and historical records. Never reset the shared
  worktree. No database/media/config/runtime rollback is caused by WI025.

## Follow-ups

Required next WI is **WI-20260909-ATS-026 independent review**, now underway
according to the parent. Parent owns its handoff/delegation, finding closure
and pending full-Gradle/frontend-build evidence in
[WI026's evidence pack](WI-20260909-ATS-026-evidence-pack.md).
The parent explicitly requested WI025 documentation CLOSED now; remaining
full-test/build results may be collected only in WI026 without reopening or
misrepresenting the historical validation snapshots in this pack.
The `/uploads/.staging/**` security candidate and any resulting protected-path
documentation addendum are explicitly transferred to CR/WI026; WI025 does not
claim that candidate verified or fixed. Parent records final full-build results
once in WI026 and then updates REQ005 closure when justified. WI025 will not
wait for those results or perform deployment.
WI025 does not spawn agents, close REQ005 or claim full release GO. The reviewer
should check exact encoder-policy/error semantics, schema/history boundaries,
reference/claim recovery and source-vs-runtime claims.

## Related Documents

- [WI025 handoff](WI-20260909-ATS-025-handoff.md)
- [WI025 summary](../user/WI-20260909-ATS-025-summary.md)
- [REQ005](../user/REQ-20260909-ATS-005.md)
