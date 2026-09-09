---
version: 1.4
last_updated: 2026-09-09
project: ATS
owner: SE
category: agent
status: stable
dependencies:
  - path: WI-20260909-ATS-023-handoff.md
    reason: Approved implementation and evidence contract
  - path: ../user/REQ-20260909-ATS-005.md
    reason: Approved original retention and full-length 128kbps streaming
---

# Evidence Pack: WI-20260909-ATS-023

## Summary

Backend source implements a durable, single-runtime WAV processing queue on Track,
full-length verified MP3 streaming, preserved original downloads, protected staged
generation, ADMIN status/retry, and an independent 100MiB audio cap.

## Scope / DoD Check

- Implemented source/tests/schema only, plus explicitly delegated database bootstrap source guards.
- Original reference remains `audio_file`; existing rows default READY/direct listening. No historical backfill.
- Initial WAV remains inactive and cannot activate before verified completion. Worker never activates a Track.
- WAV replacement retains current original/stream/duration/waveform until final commit. Metadata edits survive finalization.
- Generation and claim-token checks fence stale completion/retry; claimed input remains referenced during supersession.
- Claim/finalize use separate short transactions with Track write locks. FFmpeg and generated staging run outside a business transaction.
- Generated output has a durable PREPARED journal owner before bytes exist; rollback/stale output cleanup uses existing recovery.
- Focused compile/tests and non-DB bootstrap guards passed. Codec-compatibility and WI026 output-budget/environment hardening each passed a focused compile/test rerun, including actual high-rate/multichannel encoding. A later parent whole-suite failure exposed an outdated concurrency-test barrier; the test-only correction passed nine focused cases. Whole-suite revalidation remains pending with MA/WI026.
- MySQL DDL, actual server restart/deployment, browser/mobile flows, and independent WI026 QA are not claimed complete.

## Reference Documents (Tier 0-2)

| Tier | Injected/read pointer | Use |
|---|---|---|
| 0 | `docs/standards/core-principles.md` | Approved scope, preservation, truthful evidence |
| 0 | `docs/standards/development-standards.md` | Thin controllers, service transactions, existing patterns |
| 0 | `docs/standards/documentation-standards.md` | Metadata and evidence |
| 0 | `docs/standards/glossary.md` | Public listening versus official download |
| 1 | `docs/policies/security-policy.md` | Denied raw audio/static keys and safe errors |
| 1 | `docs/policies/quality-gates.md` | Risk-scaled verification |
| 2 | `docs/design/runtime-storage-operations.md` | Root tuple, journal ownership, retained references |
| 2 | `deliverables/user/REQ-20260909-ATS-005.md` | Approved 100MiB and 128kbps scope |
| 2 | Handoff pointers to `docs/design/usecase/sound-track.md`, `db-schema.md`, `api-spec.md` | Current implementation cross-checked; document ownership remains MA/WI025 |

Assignee: SE. Tier 0/security/runtime constraints were injected by MA, with the saved
handoff inspected first. No further agents were spawned. Quality/evidence skills used:
`.agents/skills/test`, `.agents/skills/build-check`, `.agents/skills/create-wi-evidence-pack`.
Official native codec/protocol references inspected:
[libmp3lame](https://ffmpeg.org/ffmpeg-codecs.html#libmp3lame),
[protocol whitelist](https://ffmpeg.org/ffmpeg-protocols.html#Protocol-Options).

## API Contract

`AudioProcessingResponse`:

```text
trackId: number
state: READY | PENDING | PROCESSING | FAILED | CANCELLED
generation: number
streamReady: boolean
retryAllowed: boolean
attemptCount: number
errorCode: string | null
updatedAt: LocalDateTime | null
```

- ADMIN list and ADMIN detail/create/update add `audioProcessing`. Public TrackResponse has null `audioProcessing` and null `audioFile`; no new storage keys are exposed. Existing ADMIN `audioFile` compatibility remains.
- `GET /api/tracks/admin/{trackId}/audio-processing`: 200, existing `ResponseDTO.data` envelope.
- `POST /api/tracks/admin/{trackId}/audio-processing/retry`: JSON `{ "generation": 1 }`; 202 with the updated state. Latest FAILED generation with retained pending reference and no active claim only; otherwise `AUDIO_PROCESSING_CONFLICT` (409).
- `AUDIO_STREAM_NOT_READY` (409) rejects activation without a required derivative. Existing `IO_LARGE` (413) rejects audio over 104857600 bytes.
- Safe processing errors: `FFMPEG_NOT_FOUND`, `AUDIO_TRANSCODE_TIMEOUT`, `AUDIO_TRANSCODE_FAILED`, `AUDIO_OUTPUT_INVALID`, `AUDIO_OUTPUT_TOO_LARGE`, `AUDIO_INPUT_UNAVAILABLE`, `AUDIO_STORAGE_FAILED`, `AUDIO_PROCESSING_INTERRUPTED`, `AUDIO_FORMAT_UNSUPPORTED`.
- `AUDIO_PROCESS_TERMINATION_FAILED` halts the worker and retains the claim instead of releasing its input; safe operational log only. Operator attention is required.
- Replacement can be PENDING/PROCESSING/FAILED while `streamReady=true` for the retained current pair. No auto-activation.
- Deactivating an active Track cancels pending replacement. An unchanged false flag submitted for an already-inactive Track does not cancel its initial processing. DELETE always cancels pending work and preserves the current original/stream/license history.
- Restart recovery marks interrupted PROCESSING work FAILED/manual-retry, releases obsolete claims safely, and resumes only persisted PENDING work. READY historical rows are never queued.

## Data And Runtime Contract

`schema.sql` and `scripts/database/manual-wi023-audio-processing.sql` add nine Track columns:
`stream_audio_file`, `stream_required`, `audio_processing_state`, `audio_generation`,
`pending_audio_file`, `claimed_audio_file`, `audio_claim_token`, `audio_attempt_count`,
`audio_error_code`; index `idx_tracks_audio_queue(audio_processing_state,id)`.
No new table. The additive manual SQL is one-time source, not executed here.

The bootstrap helper's current expectation is now UNRECORDED. Historical observed
43 tables/511 columns/175 index rows/91 foreign keys/hash remain under
`PRE_WI023_MYSQL_MANIFEST`, not an assertion about changed source. Create/Validate/
HibernateValidate refuse before credentials until a separately approved new observation.

Configuration in `src/main/resources/application.yml`:

| Setting | Default |
|---|---|
| multipart file/request | 100MB / 120MB (Spring binary DataSize) |
| `APP_AUDIO_WORKER_ENABLED` | true |
| `APP_AUDIO_FFMPEG_PATH` | ffmpeg |
| `APP_AUDIO_TIMEOUT_SECONDS` | 300, accepted 1..1800 |
| `APP_AUDIO_POLL_INTERVAL_MS` | 5000, effective minimum 1000 |

The worker owns a private single-thread executor, not a Spring scheduler/executor
bean, so unrelated scheduled jobs keep their existing scheduler. On orderly shutdown
it interrupts work and waits at most ten seconds. Native child reap is bounded to
five seconds and preserves interruption status. This is a single-runtime assumption,
not a distributed worker guarantee or an OS-level parent-death job object.

FFmpeg uses a fixed argument list, local file protocol and WAV demuxer, no shell,
libmp3lame 128k CBR, and discarded stdout/stderr. Only the derivative is adapted for
codec compatibility: retain rates in {16000, 22050, 24000, 32000, 44100, 48000} Hz;
otherwise select the nearest rate by absolute Hz difference, choosing the lower rate
on an exact tie. Retain mono/stereo; greater channel counts use FFmpeg's standard
downmix to stereo. For example, 96000 Hz / 6 channels becomes 48000 Hz / 2 channels.
Original bytes remain unchanged. No volume normalization or truncation is requested.
Invalid/non-WAV input fails closed; the existing decoded-WAV maximum of 32 channels
is retained rather than introducing a new high-rate/multichannel rejection policy.

Output byte limit is derived from exact source duration at 16000 bytes/sec plus
65536 bytes of framing allowance. A total expected limit above 268435456 bytes
(256MiB, including framing allowance) fails with `AUDIO_OUTPUT_TOO_LARGE` before
ProcessBuilder construction. Overflow-safe integer quotient/remainder arithmetic
preserves exact ceiling behavior; the exact 256MiB boundary is accepted. The limit
is never clamped to silently shorten an oversized source. Hitting the accepted
file limit is rejected even when FFmpeg exits zero. Generated MP3 is decoded and
checked for nominal 128000 bitrate, the
expected derivative rate/channels, and duration difference from the source <=0.25 sec
(codec frame padding allowance). A low-rate mono original can be smaller than its
128kbps derivative; the safety condition is the explicit byte bound, not compression
relative to every possible original.

Before native launch, the child environment retains only case-insensitive names
`SystemRoot`, `WINDIR`, `SystemDrive`, `PATH`, `PATHEXT`, `TEMP`, `TMP`, `LANG`,
`LC_ALL`, `LC_CTYPE`, `TZ`; all other inherited entries are removed. Environment
values are neither logged nor returned. This removes server-specific secrets,
`JAVA_TOOL_OPTIONS` and `FFREPORT` without changing the parent environment.

## Evidence Pointers

Existing source changed:

- `src/main/java/com/atstudio/atstudio/entity/Track.java`: durable audio state and media selection.
- `src/main/java/com/atstudio/atstudio/repository/TrackRepository.java`: bounded pending/claim IDs and existing row locks.
- `src/main/java/com/atstudio/atstudio/service/TrackService.java`: upload cap, enqueue/replacement, listening key, activation/retry/cancellation.
- `src/main/java/com/atstudio/atstudio/controller/TrackController.java`: two ADMIN routes; existing full ResourceRegion path reused.
- `src/main/java/com/atstudio/atstudio/dto/track/TrackResponse.java`, `AdminTrackListItemResponse.java`: safe state projection.
- `src/main/java/com/atstudio/atstudio/common/exception/BUSINESS_ERROR.java`, `common/validation/ValidationConstants.java`: errors and independent cap.
- `src/main/java/com/atstudio/atstudio/service/audio/AudioAnalysisService.java`: reject truncated WAV decoded-frame mismatch.
- `src/main/java/com/atstudio/atstudio/service/storage/StorageService.java`, `LocalStorageService.java`, `StorageMutationCoordinator.java`: durably owned generated staging and transaction attachment.
- `src/main/java/com/atstudio/atstudio/service/storage/StorageReferenceChecker.java`, `StorageIntegrityService.java`: stream/pending/claimed reference coverage.
- `src/main/resources/application.yml`, `schema.sql`.
- `scripts/database/DisposableMysqlBootstrap.java`, `test-bootstrap-guards.ps1`: historical manifest fence and non-DB regression.

New source:

- `src/main/java/com/atstudio/atstudio/entity/enums/AudioProcessingState.java`.
- `src/main/java/com/atstudio/atstudio/dto/track/AudioProcessingResponse.java`, `AudioProcessingRetryRequest.java`.
- `src/main/java/com/atstudio/atstudio/service/audio/AudioTranscodeException.java`, `FfmpegAudioEncoder.java`, `TrackAudioProcessingTransactions.java`, `TrackAudioProcessingWorker.java`.
- `scripts/database/manual-wi023-audio-processing.sql`.

Test pointers:

- `src/test/java/com/atstudio/atstudio/service/storage/TrackAudioPipelineIntegrationTest.java`: H2/temp-root journal, file-pair rollback, stale claim, cancellation, restart/retry, denied first download, licensed original bytes, Range derivative, real encoder variants.
- `src/test/java/com/atstudio/atstudio/service/audio/FfmpegAudioEncoderTest.java`: argument array, output-bound hit, timeout/reap, configuration, invalid format, 16 deterministic output-format policy cases, exact output-budget boundary/pre-launch rejection and synthetic child-environment allowlist cases.
- `src/test/java/com/atstudio/atstudio/config/AudioProcessingConfigTest.java`: enabled/disabled lifecycle, no scheduler-bean takeover, source multipart/domain caps.
- `src/test/java/com/atstudio/atstudio/service/TrackServiceTest.java`: cap minus-one/equal/plus-one and validation-before-lock adjustments.
- `src/test/java/com/atstudio/atstudio/service/TrackMutationConcurrencyIntegrationTest.java`: lock-phase barrier corrected without removing seven writer regressions; added analysis-phase concurrent metadata/counter preservation case.
- `src/test/java/com/atstudio/atstudio/service/audio/AudioAnalysisServiceTest.java`: corrupt/truncated WAV rejection.
- `src/test/java/com/atstudio/atstudio/controller/TrackControllerTest.java`: ADMIN status/retry authorization, required generation, public static denial, existing stream/download regressions.

`DownloadService` itself is unchanged: it still selects `audioFile` after existing
entitlement checks. Frontend, product design docs and unrelated baseline documents
are owned by MA/other WIs and were not edited by SE.

## Commands And Results

Run from the repository root. Heavy executions were serialized with MA/WI024.

```powershell
$env:ATS_TEST_FFMPEG_PATH = '<explicit portable ffmpeg.exe path supplied by MA>'
.\gradlew.bat test --tests '*TrackAudioPipelineIntegrationTest' --tests '*TrackServiceTest' --tests '*TrackServiceAudioProcessingTest' --tests '*TrackAudioReplacementTransactionIntegrationTest' --tests '*TrackControllerTest' --tests '*DownloadServiceTest' --tests '*Storage*Test' --tests '*FfmpegAudioEncoderTest' --tests '*AudioProcessingConfigTest' --tests '*AudioAnalysisServiceTest' --tests '*RetainedHistoryStorageIntegrationTest' --tests '*V1BackendBaselineContractTest' --console=plain --max-workers=1
.\scripts\database\test-bootstrap-guards.ps1
git -c core.safecrlf=false diff --check -- src/main src/test scripts/database
```

| Run | Executed evidence |
|---|---|
| Focused 1 | 174 total, 7 failures, 1 skip. Five mono/stereo mock-fixture mismatches and two obsolete strict-mock stubs. Real application encoder case passed. Log retained, not a PASS claim. |
| Focused 2 | 184 total, 1 failure, 1 skip. Remaining test confused live thread count with configured pool size. Audio pipeline cases passed. |
| Focused 3 | PASS, 50s, 210 total, 209 passed, 0 failures/errors, 1 Windows symbolic-link-gated skip. Includes 16 pipeline cases with six actual FFmpeg variants. Compiled main/test source. |
| Bootstrap guards | PASS, 26 checks; no MySQL connection/observation/DDL. |
| Scoped diff whitespace | PASS. |
| Executor/lifecycle follow-up | PASS, 48s, 210 total, 209 passed, 0 failures/errors, 1 Windows symbolic-link-gated skip. Main/test compilation executed; all 16 pipeline cases (six actual FFmpeg cases), two lifecycle/configuration tests and five encoder process tests passed. This predates the codec-compatibility correction. |
| Codec-compatibility follow-up | PASS, 33s, 61 total/passed, 0 failures/errors/skips. Main/test compilation executed. Includes 20 pipeline cases (ten actual FFmpeg cases), 21 encoder unit cases (16 output-format cases), two lifecycle/configuration cases, and 18 audio analysis/service cases. This predates the output-budget/environment correction. |
| WI026 output-budget/environment follow-up | PASS, 31s, 50 total/passed, 0 failures/errors/skips. Main/test compilation executed. Includes 28 encoder unit cases, 20 pipeline cases (ten actual FFmpeg cases under the environment allowlist) and two lifecycle/configuration cases. This remains the latest encoder-source verification. |
| Parent whole build, first attempt | FAILED, 2m17s, 1885 total, 1859 passed, 7 failures, 0 errors, 19 skips. Counts independently read from preserved XML. All seven failures are the concurrency-test barrier mismatch below; this is not a whole-build PASS. |
| Concurrency-test follow-up | PASS, 27s, 9 total/passed, 0 failures/errors/skips. Test compilation executed; main compilation was up-to-date. Only the named concurrency test file changed for this follow-up. |

Logs: `build/wi023-evidence/focused-1.log`, `focused-2.log`, `focused-3.log`,
`focused-final.log`, `bootstrap-guards.log`, `codec-compat-focused.log`,
`output-env-focused.log`. Snapshots:
`build/wi023-evidence/focused-3-xml/` and `build/wi023-evidence/focused-final-xml/`
protect the earlier JUnit XML from later test task overwrites. The executor/lifecycle
run uses the same focused command above. Its sole skip is
`LocalStorageServiceTest.rejectsDirectoriesAndSymbolicLinks()` on this Windows host.

The codec-compatibility run is preserved separately in
`build/wi023-evidence/codec-compat-focused-xml/`. Exact test command:

```powershell
$env:ATS_TEST_FFMPEG_PATH = 'C:\Users\jm991\AppData\Local\ATStudio\tools\ffmpeg-128k-20260909\unpacked\ffmpeg-9.0.1-essentials_build\bin\ffmpeg.exe'
.\gradlew.bat test --tests '*TrackAudioPipelineIntegrationTest' --tests '*FfmpegAudioEncoderTest' --tests '*AudioProcessingConfigTest' --tests '*AudioAnalysisServiceTest' --tests '*TrackServiceAudioProcessingTest' --console=plain --max-workers=1 *> build/wi023-evidence/codec-compat-focused.log
```

The ten actual application-pipeline cases retain the six supported-rate cases and add
96000 Hz stereo -> 48000 Hz stereo, 44100 Hz / 6 channels -> 44100 Hz stereo,
96000 Hz / 6 channels -> 48000 Hz stereo, and 8000 Hz mono -> 16000 Hz mono.
Every case asserts READY without activation, full retained-original SHA-256 equality,
decoded expected rate/channels, nominal 128000 bitrate, two-second source duration
within 0.25 seconds, and a positive output below the 97536-byte source-derived bound.

The final output-budget/environment run uses the same explicit test-process binary
environment variable above and is preserved in
`build/wi023-evidence/output-env-focused-xml/`. Exact command:

```powershell
.\gradlew.bat test --tests '*FfmpegAudioEncoderTest' --tests '*TrackAudioPipelineIntegrationTest' --tests '*AudioProcessingConfigTest' --console=plain --max-workers=1 *> build/wi023-evidence/output-env-focused.log
git -c core.safecrlf=false diff --check -- src/main src/test scripts/database deliverables/agent/WI-20260909-ATS-023-evidence-pack.md deliverables/user/WI-20260909-ATS-023-summary.md
```

Seven added encoder unit cases cover budget-minus-one and exact-budget acceptance;
one-byte-over, 1Hz/100MiB and extreme-frame-count rejection before any ProcessBuilder
construction with unchanged source bytes and no output; the 8kHz/mono/8-bit 100MiB
WAV metadata model (44-byte header, expected 209780648-byte limit); and the exact
case-insensitive environment allowlist at mocked process start. The size-boundary
cases model decoded metadata, not multi-hour native encoding or large-file writes.
The environment test uses only synthetic values for DB/TOSS/MAIL/JWT secrets,
`JAVA_TOOL_OPTIONS`, `FFREPORT` and mixed-case denied names. Actual secrets were
not inspected or dumped. All ten real encoding cases were rerun successfully with
the product environment filter active. Final scoped whitespace check also passed.

Actual encoder tests use only `ATS_TEST_FFMPEG_PATH` in the test process, never a
hardcoded product path or PATH/global install. MA provisioned the binary separately.
These tests use synthetic two-second audio, H2 and temporary roots. They are actual
encoder application-integration tests, not mocks or production checks. MA's separate
five-minute direct CLI benchmark is not included as application-pipeline evidence.

## Concurrency Follow-up

The parent's first full-build log was copied from
`C:/Users/jm991/AppData/Local/ATStudio/validation/wav128-20260909/backend-full-build.log`
to `build/wi023-evidence/parent-full-failed.log`; the complete pre-rerun JUnit output
was copied to `build/wi023-evidence/parent-full-failed-xml/` before focused execution.

Root cause: `TrackService.updateTrack` analyzes input before `findTrackForUpdate`.
The existing test blocked in `audio.analyze`, incorrectly treating that pre-lock
phase as a held replacement lock. Six writers therefore completed without the
expected timeout; DOWNLOAD read the unstubbed old media and failed its non-null
assertion. The MP3 fixture still uses `mutations.replace`, not the new WAV store
path. No production defect or need to lengthen the lock scope was established.

Test-only correction in `TrackMutationConcurrencyIntegrationTest`:

- Move the barrier to the audio `mutations.replace` callback, assert that the managed
  Track already holds `PESSIMISTIC_WRITE`, and check old audio/thumbnail key arguments.
- Preserve all seven writer cases, the `second.get(250ms)` timeout requirement,
  final audio/thumbnail/duration/waveform, metadata, counters and activation assertions.
- Preserve the unlocked stale-media-resurrection control.
- Add `analysisAllowsWritersAndReplacementPreservesTheirLatestMetadataAndCounters`:
  while analysis remains blocked, metadata/like/download/play changes commit; after
  replacement resumes, the new media and all those latest values remain present.

```powershell
.\gradlew.bat test --tests '*TrackMutationConcurrencyIntegrationTest' --console=plain --max-workers=1 *> build/wi023-evidence/concurrency-focused.log
git -c core.safecrlf=false diff --check -- src/test/java/com/atstudio/atstudio/service/TrackMutationConcurrencyIntegrationTest.java
```

Both commands passed. The test run took 27 seconds with 9/9 PASS and no skips.
XML is preserved in `build/wi023-evidence/concurrency-focused-xml/`. This is real H2
transaction/lock integration with mocked storage/audio, not native encoding, MySQL
or real media IO. No production source changed in this follow-up. SE reported the
execution ended before CR's separate security-focused work; parent whole-build retry
must follow that work and is not claimed here. Rollback for this follow-up is limited
to the named test edits and WI023 evidence updates, with no data/schema/media action.

## Risks / Rollback

- Not deployed. No existing database rows/schema, actual media roots, running JAR/processes, charges/mail, commits or pushes were changed.
- Final independent review and whole-suite/build/browser validation remain WI026/MA-owned. A real crash/restart and MySQL `ddl-auto=validate` have not been executed here.
- Native OS-level orphan handling after a hard JVM/OS failure is not implemented; an operator must ensure an old owned encoder has exited before restarting the single runtime. A child termination failure deliberately halts processing with its source claim retained.
- A database outage while failure finalization runs can retain PROCESSING until controlled recovery/restart. Do not manually delete the claimed input to clear it.
- Historical manifest remains historical. Re-observation/new MySQL proof and application of additive SQL require separate approved actions.
- External deployment request caps are independent of backend 120MiB headroom; Cloudflare constraints are MA/WI025-owned. No chunked upload/R2 expansion was added.
- Rollback source changes only after identifying these WI-owned paths; preserve all unrelated changes. Retain added columns/media. Never drop columns/files or deploy an old binary over new stream-required WAV rows without an approved data-aware rollback plan.

## Follow-ups

WI023 unblocks WI024 frontend, WI025 documentation and WI026 independent QA. MA
owns this chain and REQ closure. No additional delegation occurred in this SE task.
