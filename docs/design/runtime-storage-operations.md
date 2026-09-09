---
version: 1.5
last_updated: 2026-09-09
project: ATS
owner: SA
category: design
status: stable
dependencies:
  - path: ../../src/main/java/com/atstudio/atstudio/service/storage/StorageIntegrityService.java
    reason: Read-only persisted-reference audit implementation
  - path: ../../scripts/acceptance/AcceptanceLifecycle.psm1
    reason: Acceptance runtime bundle enforcement
  - path: ../../docs/policies/security-policy.md
    reason: Public/private storage and key-exposure constraints
  - path: api-spec.md
    reason: Administrator audio processing status and retry contract
  - path: ../../scripts/database/manual-wi023-audio-processing.sql
    reason: Additive audio rollout SQL applied to the retained TEST DB under REQ006
  - path: ../../deliverables/agent/WI-20260909-ATS-028-evidence-pack.md
    reason: Authoritative actual-runtime evidence and remaining limits
---

# Runtime Storage Operations

**Current checkpoint (WI029, 2026-09-09):** WI026 completed source review;
REQ006/WI028 subsequently verified actual application to the retained TEST
runtime. Start with [rollout gates](#audio-processing-rollout-gates) and the
[actual-runtime addendum](#actual-retained-runtime-verification-addendum-2026-09-09).
Retained DB: **43 tables / 520 columns**; fresh-bootstrap manifest: **UNRECORDED**.
The [phone acceptance follow-up](#user-acceptance-follow-up-2026-09-09) closes only
human-assisted download-save/open-play; earlier in-app native-event UNKNOWN is
environment-specific. MA confirmed test33-only follow-up deactivation and logout. Ten
historical missing references remain with strict startup false, and production
remains **HOLD**. Earlier dated
source-only evidence is preserved, not the current deployment status.

## Purpose

A runnable ATStudio environment is one explicit tuple:

```text
database + public storage root + private storage root
```

The tuple is an operational contract. Restarting an application against the
same database but different roots is an environment change, not an equivalent
restart. Persisted object references can otherwise remain valid database text
while the corresponding file is unavailable.

## Root Rules

- Public and private roots must be distinct, non-nested real directories.
- Acceptance requires explicit absolute `APP_STORAGE_PUBLIC_PATH` and
  `APP_STORAGE_PRIVATE_PATH` values. It never falls back to repository-relative
  `uploads` or `private-uploads` paths.
- The acceptance backend environment bundle treats both root variables as
  required, validates them before any tunnel/backend/frontend child process is
  launched, and injects them only into the backend process.
- Explicit local configuration remains untracked. It may use local roots, but
  an operator must keep them paired with the database that owns their file
  references.

## Integrity Audit

`StorageIntegrityService` is read-only. It checks whether each persisted
reference resolves through `StorageService` without changing data or files.

| Domain | Root | Checked reference |
|---|---|---|
| Track | PUBLIC | original `AUDIO`; optional `STREAM_AUDIO`, `PENDING_AUDIO`, `CLAIMED_AUDIO`, `THUMBNAIL` |
| Album | PUBLIC | optional thumbnail |
| Playlist | PUBLIC | optional thumbnail |
| Company Certification Document | PRIVATE | document |
| Notice Attachment | PRIVATE | attachment |
| Question Attachment | PRIVATE | attachment |

The report contains aggregate checked/available/missing counts and at most 100
opaque issues. An issue includes only `domain`, `storageRoot`, `recordId`, and
`referenceType`. It never serializes a storage key, original filename, file
bytes, credential, or repair instruction.

WI023 includes original, derivative, pending and claimed audio keys in both
integrity auditing and reference-aware cleanup, including inactive Tracks.
Counts are references, not unique files: the initial WAV may be referenced as
both original and pending/claimed input. An absent nullable derivative while
work is pending is not reported missing; this availability audit is not a
codec/bitrate/processing-state verification or a substitute for activation guards.

### Startup Behavior

| Runtime | Audit | Result on missing reference |
|---|---|---|
| Base/local default | enabled, non-strict | safe aggregate warning; no automatic data/file mutation |
| Explicit runtime with `APP_STORAGE_INTEGRITY_AUDIT_ON_STARTUP=false` | disabled | operator must invoke the ADMIN inspection explicitly |
| Any explicit runtime with `APP_STORAGE_INTEGRITY_AUDIT_ON_STARTUP=true` and strict mode disabled | enabled | warning; no automatic data/file mutation |
| `acceptance` | enabled and strict | startup fails before readiness |
| `prod`/`production` deployment | enabled and strict required | startup refuses a missing audit/strict setting or a missing reference |

Production profiles also require explicit absolute roots before the local
storage service initializes. These guards turn an omitted deployment storage
contract into a failed startup, rather than a server that silently points at a
new repository-relative directory.

The ADMIN endpoint is:

```text
GET /api/admin/storage-integrity
```

It is ADMIN-only and returns a report, not a repair operation.

## Recovery Boundary

### Prospective Mutation Contract (2026-09-09)

- Track deactivation preserves media, Licenses and download events; retained
  events remain quota input. It does not authorize inactive-Track download or
  recovery of previously deleted history.
- Album/Playlist deletion captures the old thumbnail key and clears the parent
  reference transactionally before after-commit cleanup. Rollback retains the
  original object/reference. The reference checker protects shared objects
  across catalog domains, including retained inactive rows. Integrity audit
  continues to inspect those rows rather than hiding historical mismatches.
- A storage batch registers in-flight ownership and durably prepares all target
  keys before staging bytes. A partially written failing stage therefore has
  a journal owner. Failed cleanup remains recoverable through the journal;
  recovery does not race a currently registered operation in this single-server
  model. This is not a multi-writer or distributed recovery guarantee.
- Track media/metadata mutations and child/counter writers follow the reviewed
  write-lock protocol. This avoids stale managed-row restoration of replaced
  media keys; it adds no DDL or blanket rewrite of retained rows.

The [bounded WI013 closure evidence](../../deliverables/agent/WI-20260909-ATS-013-evidence-pack.md)
maps temporary-storage/H2 tests and their limitations. At that checkpoint the
patched source/sample artifact was **not deployed**, and no real-root
reconciliation, retained-file repair or startup was performed by that work.
Later TEST application is recorded separately in the current checkpoint above;
it does not convert those historical tests into live or repair evidence.

### Existing Mismatch Recovery

The audit does not copy, relocate, delete, regenerate, or rewrite database
references. An identified mismatch requires an approved recovery decision:

1. identify the owning environment tuple and a verified source object;
2. decide whether to restore the object or re-upload/recreate it;
3. take a backup or reversible snapshot before a data/file mutation;
4. apply the approved repair to the exact target only;
5. repeat the integrity inspection and the affected user flow.

This is deliberately separate from the storage mutation journal. The journal
recovers an interrupted staged mutation; it cannot infer which external root
should own an already-committed historical reference.

## Backup and Restore Boundary

Any environment backup/recovery exercise treats the tuple as one unit:

1. take a database backup and public/private root snapshots from the same
   declared recovery point;
2. record only the environment label, backup time, and non-secret storage
   locations in the operator evidence, never credentials or object keys;
3. restore into a separate isolated database and separate public/private roots;
4. start with schema validation and the strict integrity audit enabled;
5. smoke-test the affected file flow before a release or cutover decision.

The repository does not yet provide a production backup service, retention
system, or general retained-data migration tool. WI023 provides only the
reviewed additive audio SQL source, not an automated migration runner. This section defines the minimum
operator boundary for a future approved backup/recovery rehearsal; it does not
claim production recovery is complete.

## Audio Processing Operations

This is the WI023/WI024 **source contract**, documented by WI025 under
REQ-20260909-ATS-005 and closed by WI026. REQ006/WI028 subsequently verified
actual retained TEST application; the coverage below remains bounded and is
not production approval.

- `audio_file` remains the untouched original for Official Download. Public
  Listening uses a verified full-length MP3 CBR 128kbps derivative for new WAV,
  not a short preview. Legacy rows/new MP3 retain direct playback, with no
  automatic historical conversion. New WAV is inactive until manually activated
  after readiness; worker completion never activates a Track.
- Originals, derivatives and pending inputs stay under the existing PUBLIC
  root's denied `tracks/audio` prefix, not a publicly served sibling directory.
  `SecurityConfig` denies both `/uploads/.staging/**` and
  `/uploads/tracks/audio/**` case-insensitively, including Windows case aliases.
  Generated staging is never a public listening route; ordinary public
  thumbnails and the controller-mediated stream remain unchanged.
  No new storage service or root is introduced. WAV replacement retains the
  previous original/stream/duration/waveform until verified completion; only
  audio fields are switched, preserving concurrent title/tag/manual-state edits.
- The durable queue lives on Track. Claims and finalization use short locked
  transactions, fenced by generation and claim token; FFmpeg runs outside them.
  Reference checking and the mutation journal protect staged output and retained
  pending/claimed inputs. Cleanup of old pairs is after-commit/reference-aware,
  not authorization for operators to delete files manually.
- The worker owns one private scheduled executor and one encoder thread per
  application process; it is not a shared Spring scheduler/executor bean, so
  unrelated scheduled jobs retain their scheduler. Shutdown interrupts the
  private executor and awaits termination for up to 10s. Startup recovery
  assumes the previous worker is gone. This is a
  **single-server** contract, not multi-node leasing or distributed recovery.
  Do not overlap old/new workers against one tuple during restart/rollout.

### FFmpeg Provisioning and Configuration

FFmpeg is an external executable, not bundled in the application or Git. The
[official download page](https://ffmpeg.org/download.html) links to
[Gyan Windows builds](https://www.gyan.dev/ffmpeg/builds/). Provision an approved
version outside the repository, verify its publisher checksum, and restrict
binary replacement permissions. Do not copy installation/private paths into
public evidence. The [libmp3lame documentation](https://ffmpeg.org/ffmpeg-codecs.html#libmp3lame)
requires a build with that encoder; an executable being present is insufficient.

Actual source mappings in `src/main/resources/application.yml` are:

| Property | Environment variable | Default / source constraint |
|---|---|---|
| `app.audio.ffmpeg-path` | `APP_AUDIO_FFMPEG_PATH` | `ffmpeg`; use the approved executable's absolute path in external configuration |
| `app.audio.timeout-seconds` | `APP_AUDIO_TIMEOUT_SECONDS` | 300; constructor accepts 1 through 1800 seconds |
| `app.audio.worker-enabled` | `APP_AUDIO_WORKER_ENABLED` | `true`; `false` disables worker/executor creation, not upload acceptance |
| `app.audio.poll-interval-ms` | `APP_AUDIO_POLL_INTERVAL_MS` | 5000; worker clamps scheduled delay to at least 1000ms |

Before an approved startup, verify the **effective service identity's** binary,
permissions and encoder without printing its environment bundle:

```powershell
# $ffmpeg is the approved absolute executable path held only in the operator session.
& $ffmpeg -version
& $ffmpeg -hide_banner -encoders
& $ffmpeg -hide_banner -h encoder=libmp3lame
```

Require exit 0, the approved version and `libmp3lame` availability, then perform
an approved synthetic encode outside runtime media. These are operator
instructions, not commands executed by WI025. Backend startup does not run an
automatic native-binary/encoder capability probe: constructor validation only
checks a nonblank/NUL-free executable string and timeout range. Missing or
unlaunchable FFmpeg becomes `FFMPEG_NOT_FOUND` when a job runs.

### Encoder Input and Output Policy

- Java Sound/mp3spi must first decode the MP3/WAV. Invalid/unsupported decoding
  at upload returns 400 `AUDIO_ANALYSIS_FAILED` before storage/DB mutation.
  Extension selection alone cannot establish that a WAV is decodable.
- Preserve original bytes. Revised `FfmpegAudioEncoder.outputFormat` chooses
  the nearest rate from **16000, 22050, 24000, 32000, 44100, 48000Hz**, choosing
  the lower rate on an exact tie, and `min(inputChannels, 2)`. Compatible rates
  and mono/stereo are unchanged. Examples: 96000Hz to 48000Hz; 8000Hz to
  16000Hz; 23025Hz to 22050Hz; six channels to stereo using FFmpeg's standard
  downmix. This is deterministic **derivative-only** MP3-128kbps compatibility,
  not an original-file rewrite, extra quality enhancement or volume normalization.
- The earlier rate/channel rejection was an MA-added restriction, not approved
  product policy, and is superseded. Valid high-rate/multichannel WAV is not
  rejected merely for exceeding 48kHz or two channels. Existing Java Sound
  decoding and positive-rate/1-to-32-channel validation still apply; this is
  not a promise that every WAV encoding/container variant is decodable.
- Fixed options include `-c:a libmp3lame -b:a 128k -abr 0 -threads 1`, explicit
  derivative `-ar`/`-ac`, first audio stream only, and no volume normalization
  or shortening. Derivative-only resampling/downmixing for compatibility is
  allowed by the clarified REQ. MP3 is lossy; rate/channel preservation where
  compatible does not promise bit-identical sound or unchanged bandwidth.
- Execution uses `ProcessBuilder` argument entries, no shell, local file-only
  protocol, forced WAV demuxer, distinct absolute paths and a non-existing
  target. Metadata is removed. stdout/stderr are discarded, not returned to API
  clients or retained as unbounded logs.
- The child environment retains only these case-insensitive names:
  `SYSTEMROOT`, `WINDIR`, `SYSTEMDRIVE`, `PATH`, `PATHEXT`, `TEMP`, `TMP`,
  `LANG`, `LC_ALL`, `LC_CTYPE`, `TZ`. Other server environment variables,
  including credential/config variables, `FFREPORT` and `JAVA_TOOL_OPTIONS`,
  are removed before `start()`. The allowlist does not grant OS-level isolation;
  service identity and filesystem permissions still matter. No environment
  values are shown in public evidence.
- The full output estimate is `ceil(decodedInputSeconds * 16000) + 65536`
  bytes, calculated with quotient/remainder arithmetic. It must be **at most
  256MiB (268435456 bytes), including the 64KiB framing allowance**; an estimate
  above that budget fails with `AUDIO_OUTPUT_TOO_LARGE` **before encoder start**.
  The cap is not applied by shortening the Track, and original bytes are retained.
  A low-rate 100MiB PCM original whose full MP3 estimate is about 200MiB remains
  eligible; the derivative budget is not the 100MiB upload limit.
- FFmpeg receives the computed accepted estimate as `-fs`. Hitting that bound
  is invalid even if FFmpeg exits 0. The worker re-decodes output and checks
  nominal 128000bps, the selected compatible output rate/channels, non-empty output and duration
  difference no greater than 0.25 seconds. These source checks are not a new
  listening-quality, capacity or runtime-media verification result. The worker
  uses that same `outputFormat` rule to validate the derivative, not equality
  against an incompatible original rate/channel count. Parent reports the
  codec-compatibility focused suite 61/61 PASS, including ten actual native
  app-pipeline cases. The later final SE focused run is 50/50 PASS, including
  output-budget/environment guards; this remains local test evidence only.

### Audio Failure and Recovery

The [ADMIN API](api-spec.md#admin-track-audio-processing-contract) exposes only
fixed codes. The SPA displays state labels, fixed Korean explanations for
known FAILED codes and a safe generic fallback for unknown codes, plus bounded
refresh/retry errors. It never renders raw codes/native diagnostics. Distinguish upload 413 `IO_LARGE`, upload
400 `AUDIO_ANALYSIS_FAILED`, activation 409 `AUDIO_STREAM_NOT_READY`, and retry
409 `AUDIO_PROCESSING_CONFLICT` from asynchronous processing failures below.

| Processing code | Implemented meaning / operator boundary |
|---|---|
| `FFMPEG_NOT_FOUND` | Process could not start; verify executable/path/permissions, not only installation |
| `AUDIO_FORMAT_UNSUPPORTED` | Invalid encoder analysis input: null/non-WAV, non-positive rate/channel count, or channels above the existing 32-channel bound. Valid 96kHz/six-channel WAV is not rejected by this guard |
| `AUDIO_TRANSCODE_TIMEOUT` | Configured encode timeout elapsed; inspect capacity before any approved timeout change |
| `AUDIO_TRANSCODE_FAILED` | Native nonzero exit, including a missing encoder; verify approved binary capabilities |
| `AUDIO_OUTPUT_INVALID` | Output size/decoded media/rate/channel/bitrate/duration check failed |
| `AUDIO_OUTPUT_TOO_LARGE` | Estimated complete MP3 including 64KiB framing exceeds 256MiB; rejected before encoder start, never delivered as a truncated preview |
| `AUDIO_INPUT_UNAVAILABLE` | Missing/oversize/unreadable worker input or worker-side analysis failure |
| `AUDIO_STORAGE_FAILED` | Other runtime/storage failure; inspect safe integrity/journal evidence |
| `AUDIO_PROCESSING_INTERRUPTED` | Interrupted encode or restart recovery of an abandoned PROCESSING claim |
| `AUDIO_PROCESS_TERMINATION_FAILED` | Worker halts if a child remains alive after forced termination and up to 5s wait; not persisted as a normal FAILED/retryable error |

1. Read current status. `retryAllowed` means FAILED with a retained pending input
   and no active claim; it does not mean the root cause is repaired. Record only
   Track ID, generation, state, attempt count and fixed code, never file keys,
   native command/stderr, credentials or private paths.
2. Correct only an approved configuration/input/storage cause. Do not repeatedly
   retry an unresolved failure or bypass the derivative guard by
   altering DB fields. Worker-disabled runtimes still accept pending WAVs, so
   keep uploads operationally paused during maintenance.
3. For restart/halt, stop the owned application/worker under separate approval,
   verify the old encoder and descendants are actually gone, preserve the same
   tuple and journal, and then restart only one worker. The termination-failure
   path retains the claim/PROCESSING state and blocks further worker iterations;
   do not clear claims, delete inputs or launch another worker while ownership
   is uncertain.
4. Startup recovery processes claimed rows in batches of at most 50, marks
   abandoned PROCESSING work FAILED with `AUDIO_PROCESSING_INTERRUPTED`, and
   releases stale claims with reference-aware cleanup. Existing PENDING work
   can be claimed normally; interrupted FAILED work is **not auto-retried**.
5. After a fresh GET reports retry allowed, an explicit POST sends exactly the
   observed generation. An accepted retry advances the generation and returns
   PENDING. After a lost/aborted/ambiguous POST, read authoritative status before
   another POST. Do not infer that abort cancelled server-side work.
6. Wait for current readiness, then manually activate when intended. Browser
   polling is 3 seconds after each read, single flight per mounted instance,
   max 60 automatic reads per cycle then manual refresh. Check read-only strict
   integrity and the affected full listening/Range/original-download flow as
   separately approved validation, not as part of a retry-button success claim.

### Audio Processing Rollout Gates

| Boundary | Current evidence / required next step |
|---|---|
| Source | REQ005 source implementation COMPLETE after WI026 review PASS; 100MiB audio, 120MB multipart total, queue/derivative/admin UI implemented. Applied separately to retained TEST under REQ006/WI028 |
| Frontend | Final parent full Vitest 114 files / 1600 tests PASS, 0 failed, 236.59s, `--maxWorkers=1`; final `npm.cmd run build` PASS (Vite phase 2.40s). CR independently read both final logs. Parent final global lint/typecheck and 17-file Prettier PASS. Mock/jsdom and build evidence, not browser deployment |
| Backend | Parent final full Gradle PASS in 2m16s: 204 suites, 1912 total, 1893 passed, 0 failures/errors, 19 skipped (18 opt-in MySQL, one Windows symlink). JaCoCo LINE 88.625%, METHOD 86.625%, BRANCH 74.216%; unchanged coverage gates PASS. CR independently audited XML/log. Earlier failed build and 9/9 concurrency / 65/65 security follow-ups remain separately recorded in WI026 |
| Native app pipeline | All 10 actual FFmpeg app cases PASS within the final pipeline 20 (96kHz/stereo, 44.1kHz/six-channel, 96kHz/six-channel and 8kHz/mono included). Separate from CLI benchmark and deployed MySQL/browser proof |
| Native tool | Historical parent synthetic checks below remain distinct from WI028's independent verification of 22 actual stored original/128000bps MP3 pairs with full decoded lengths |
| Database | Retained TEST is 43 tables / 520 columns per WI029 handoff/REQ007; WI028 corroborated nine added definitions/defaults and queue index after MA's migration. Startup under `ddl-auto=validate` verified. Retained ALTER ordinals differ from fresh source; fresh-bootstrap manifest stays UNRECORDED |
| Existing runtime | MA applied the new JAR with explicit FFmpeg and the same DB/public/private roots; WI028 verified startup logs, JAR hash and post-restart preservation. Not a fresh startup check by WI029 |
| Actual UI / transport | WI028 reviewed MA's 20-item UI batch, interrupted-processing retry, list/detail playback, keyboard/mouse seek and refresh/manual resume. Independent 22-pair checks and separate API original SHA-256 PASS; MA's local/public stream and Range probes are attributed evidence. Later human-assisted phone download-save/open-play PASS is recorded in the dated follow-up below; earlier in-app native-event UNKNOWN remains environment-specific |
| Remaining target gates | Ten historical missing references untouched; strict-on-startup=false, not strict integrity PASS. Full audio-plus-thumbnail multipart boundary, mobile scenarios beyond the recorded phone download/open/play check, broad queue/next behavior, automatic OS orphan cleanup, fresh install and restore remain unverified. Phone browser brand/version, local bytes/SHA and whole-duration playback are independently unverified; desktop Chrome/Edge coverage is not claimed |
| Review / release | WI026 source review PASS and WI028 bounded TEST verification COMPLETE WITH RECORDED LIMITATIONS. Source findings resolved; production HOLD and target-specific approval remain. WI029 documentation/MA Git delivery are not another deployment or acceptance run |

For a future separately approved retained-data target, stop the owning writer, confirm the
exact existing DB plus absolute public/private roots and a matching backup,
review/apply once only
[manual-wi023-audio-processing.sql](../../scripts/database/manual-wi023-audio-processing.sql),
then validate actual schema and strict integrity with the worker disabled
before enabling it and admitting new WAVs. The named REQ006 TEST application
already applied this SQL; do not replay it. Its retained historical missing
references and explicit non-strict startup do not satisfy this strict target
gate or authorize weakening it. This does not require creating a
new database. Never apply fresh schema/seed over retained data. Source changes
alone do **not** migrate live MySQL. Existing originals are neither rewritten
nor backfilled. The [schema and bootstrap caveat](db-schema.md#current-source-and-mysql-verification-boundary)
preserves the historical pre-feature 43-table/511-column manifest; unchanged
table count does not validate the nine added fields. The frozen private backup
was not changed and cannot be described as a new-schema restore proof.

Rollback must preserve columns, media and the matching tuple. Old binaries
may stream a newly required WAV original without the derivative guard and are
unsafe over those rows. Do not automatically drop columns/delete files or
switch binaries; require a separately approved data-aware recovery decision.

Cloudflare documents **100MB** maximum uploads for Free/Pro, with a zone setting
that can be lower ([Error 413](https://developers.cloudflare.com/support/troubleshooting/http-status-codes/4xx-client-error/error-413/),
checked 2026-09-09). The application's 100MiB audio plus thumbnail/multipart
overhead can exceed the effective request limit. Raising backend total to
120MB does not raise a proxy cap. WI028 records one accepted 104853504-byte
public WAV request without a thumbnail, not universal proxy-cap proof. The
audio-plus-10MiB-thumbnail multipart boundary remains an unverified target gate;
no chunked upload, R2, DNS bypass or plan change was implemented or authorized.

### WI026 Static-Access Review (2026-09-09)

Using actual temporary files with production `WebConfig` and `SecurityConfig`,
CR reproduced HTTP 200 containing synthetic audio bytes for `.staging` and
`.STAGING` across anonymous/USER/ADMIN. After closing staging, the same fixture
reproduced six unauthorized successes for final `TRACKS/AUDIO` and mixed-case
`TrAcKs/AuDiO` paths. Windows case-insensitive storage must not rely on a
case-sensitive URL deny rule or on random file keys as authorization.

The correction is limited to those two denied prefixes. Final focused
verification passes 65 tests: 26 new static-access cases, three existing
thumbnail cases and 36 existing Track controller cases. Anonymous protected
requests return 401; USER/ADMIN return 403. Positive public-thumbnail requests
still return their exact bytes with 200. Encoded dot/slash requests return 400
from the existing security firewall in MockMvc. This is direct temporary-file
Spring MVC/security evidence, not live Tomcat, reverse-proxy or deployed browser
verification. Other upload domains were not changed or comprehensively audited.

Red and green logs/XML and the subsequent full-build result are tracked in
[WI026 evidence](../../deliverables/agent/WI-20260909-ATS-026-evidence-pack.md).
The source verdict is PASS following the final full backend/coverage and
frontend test/build runs in the rollout table. REQ005 source implementation is
complete. At WI026 closeout, runtime migration, FFmpeg configuration, restart
and actual browser/proxy verification were still separate deployment work.
REQ006/WI028 later closed the named TEST scope with recorded limitations; it
did not repeat this static-access matrix against live Tomcat/proxy paths.

### Parent Native-Tool Evidence

Historical receipt record: the observations and then-pending review statements
below preserve the WI025 handoff timeline. They are not the current source
completion status, which is recorded in the rollout table and WI026 closeout
above. Earlier failures are not retrospectively relabeled as passing runs.

The [WI025 handoff](../../deliverables/agent/WI-20260909-ATS-025-handoff.md)
supplied these local synthetic results. WI025 did not rerun encoding or inspect
the private runtime. Parent verified the official-linked Gyan 9.0.1 portable
ZIP SHA-256 `FEC81AE03971D9DD4BE3EBE02E263BD2EC1D789483F931BDBA5F5715E65DA2E9`;
WI025 independently checked the [publisher checksum](https://www.gyan.dev/ffmpeg/builds/packages/ffmpeg-9.0.1-essentials_build.zip.sha256)
and official download link, not the downloaded archive's local bytes.

| Synthetic input | WAV bytes | MP3 bytes | Full duration | Reported bitrate | Conversion time | Original hash |
|---|---:|---:|---:|---:|---:|---|
| Five-minute WAV | 52920078 | 4801140 | 300s | 128000bps | 1.470s | Unchanged |
| Near-cap WAV | 104076078 | 9440905 | 590s | 128000bps | 2.805s | Unchanged |

The five-minute result reports 44100Hz/stereo. Parent fixture pointer:
`%LOCALAPPDATA%/ATStudio/validation/wav128-20260909/` (synthetic validation only,
not either runtime root). These are local native-tool sanity checks, not music
listening quality, production throughput, full-app browser/Range behavior,
actual database installation or runtime backup verification.

Later parent verification (received during WI025): full frontend Vitest passed
114 files / 1584 tests with 0 failed in 239.13s using `--maxWorkers=1`; log
pointer `%LOCALAPPDATA%/ATStudio/validation/wav128-20260909/frontend-full-tests.log`.
Parent also reports native **application pipeline** PASS for six original
MP3-compatible rate cases, and the last focused backend run before a worker-private-executor adjustment as
210 total / 209 passed / 1 Windows symlink skip. These supplied results were
not rerun or independently log-audited by WI025. They do not convert the CLI
benchmark into an app test, nor close frontend build, final backend full BUILD,
runtime deployment or WI026 review gates.

Subsequent parent result: codec-compatibility focused **61/61 PASS**, including
all ten actual application-pipeline cases. This supersedes the earlier
six-case-only compatibility evidence, not the separate CLI benchmark.
WI026 then confirmed output-budget and child-environment issues. WI025 inspected
the subsequent 256MiB pre-encode budget and minimal child-environment allowlist
in source, and known-code Korean FAILED reasons in the SPA. Parent then reported
CR source review favorable and UI component **26/26**, typecheck, scoped ESLint
and three-file Prettier PASS for the ten known-code messages. Final SE focused
**50/50 PASS** followed: encoder 28, pipeline 20 (including actual FFmpeg 10),
configuration 2; output-budget and environment guards confirmed. Parent first
full Gradle run reported **1885 total / 7 failed / 19 skipped**; all seven
failures are concurrency fixtures under SE cause/test-consistency investigation.
Do not presume fixture-only causes or final PASS. Full-regression closure and
frontend build remain pending in
[WI026 evidence](../../deliverables/agent/WI-20260909-ATS-026-evidence-pack.md).
WI025 documentation is CLOSED; that does not mean the application was deployed
or REQ005/WI026 was accepted.

## Acceptance Readiness

The acceptance lifecycle is ready only after its strict backend startup audit
passes, in addition to its existing local/public HTTP probes. A status `ready`
therefore proves the configured acceptance DB and both configured roots agree
for all supported persisted file-reference domains. It does not prove a
production backup, restore, retained-data migration, or production deployment
process; those remain separate release gates.

## Browser-Origin Preflight

Before diagnosing missing player state as missing storage, record the exact
browser origin (scheme, host and port) and confirm that it is explicitly
allowed by the running backend's effective `cors.allowed-origins`. Record only
non-secret origin values; do not print local configuration or environment bundles.

For the existing local configuration, use `http://localhost:5173` throughout
the browser scenario. The [local configuration example](../../application-local.example.yml)
already warns against `http://127.0.0.1:5173` unless explicitly allowed. A Vite
listener bound to 127.0.0.1 does not authorize that browser Origin. The two
origins also have separate browser storage; do not copy, clear or migrate
history/tokens to make a test appear to pass.

1. Confirm the owned development runtime and an existing public Track ID.
2. Check the public read-only lookup `POST /api/tracks/batch`, JSON body
   `{"ids":[4]}`, with `Content-Type: application/json` and the **actual browser
   Origin**. ID 4 is the recorded demo fixture; use an already-confirmed public
   ID for another environment, without creating test data for this probe.
3. Require the allowed-Origin request to succeed and an untrusted Origin to
   remain rejected. Record status and a safe result summary only. A GET health
   response or a POST without Origin is insufficient to validate browser CORS.
4. On that same browser origin, play, pause, seek and reload through the actual
   UI. Record restored Track/time/paused state separately from the controlled
   request comparison. Do not infer the browser's network trace from a shell probe.

[WI-005](../../deliverables/agent/WI-20260905-ATS-005-evidence-pack.md)
records MA's 2026-09-05 comparison against the same local batch target:
127.0.0.1 Origin returned 403 `Invalid CORS request`; localhost Origin and an
omitted Origin returned 200. The actual localhost browser then restored Track 4
at 5 seconds, paused, without a player fix or CORS allowlist expansion. This
single-track test does not establish list or queue repeat behavior; subsequent
MA observations are complete in
[WI-002](../../deliverables/agent/WI-20260905-ATS-002-evidence-pack.md) and
[SR-93](../SR/SR-93.md#2026-09-05-local-verification). On 2026-09-05, MA reported
document validation PASS (665 IDs, links and index) and `git diff --check` PASS.
MA verified the owned backend 30612 and frontend 28724 stopped and their ports
released. These are dated local observations, not current service availability
or production GO; only scoped staging and commit remain MA-owned for this
local closeout.

For deployment, explicitly configure and verify the real approved origin and
callbacks. Do not introduce wildcard origins or production localhost aliases
to work around a test-harness mismatch. Origin correctness does not replace
the storage integrity, backup/restore or release gates above.

## Actual Retained-Runtime Verification Addendum (2026-09-09)

This dated REQ006/WI028 addendum supersedes earlier source-only statements as
the current TEST deployment status while preserving their historical evidence.
Status: COMPLETE WITH RECORDED LIMITATIONS. WI029 aligns the entry points with
this report; it does not rerun the checks. See
[WI028 evidence](../../deliverables/agent/WI-20260909-ATS-028-evidence-pack.md)
for private report pointers, first-run failures and final coverage updates.

MA applied the approved nine-column/one-index retained migration to the exact
existing database, now 43 tables / 520 columns per the WI029 handoff/REQ007,
retained both storage roots, and launched the new JAR with
explicit FFmpeg and `ddl-auto=validate`. WI028 independently checked the JAR
hash, successful 13:51:51 KST startup log, exact additive column definitions and
queue index. This is retained-runtime evidence, not fresh schema/seed verification.
Retained ALTER column ordinals must not populate the global disposable manifest;
its new-schema expectation remains UNRECORDED.

MA's actual public-origin browser batch completed 20/20 selected 10MiB WAVs as
READY. WI028 independently probed all 20 stored originals/derivatives and matched
their MySQL identities: original hashes preserved, stereo/44100Hz MP3 at
128000bps, full decoded duration 59.442834 seconds. The 294-second batch timing
includes unrelated operator work and is only an observed upper bound.

The original 78 files retain matching sizes/SHA-256 and the SQL backup hash is
unchanged. Original Track IDs 1-13 retain the bounded baseline projection and
direct-playback defaults. The frozen TSV has three malformed titles; all 13
titles match the hash-verified SQL dump. The failed TSV comparison is retained,
not silently converted to PASS. Full-row equality and restore validity are not
established by these checks.

MA also observed maximum-20 rejection, frontend over-limit rejection, corrupt
input rejection, and submission retry without duplicating successful ID 34.
The 104853504-byte no-thumbnail public upload was accepted as ID 35; MA stopped
the owned backend and its FFmpeg child while PROCESSING. After restart, the
supplied DB evidence shows FAILED/AUDIO_PROCESSING_INTERRUPTED with no claim.
Subsequent supplied TXT/DB snapshots show actual retry completed READY at attempt
2; the corresponding identical/blank PNGs are not usable transition evidence.
WI028 independently verified IDs 34/35 after retry: both original hashes match,
MP3 bitrate is 128000bps, and complete lengths are 2s and about 594.407s. A
post-restart recheck confirms the original 78 file hashes, 13 bounded Track
projections and all 22 new READY mappings. Playback statistics are excluded from
the retained-metadata projection.

Supplied MA viewport/snapshots show ID 33 playback, about 40s seek and paused 25s
restoration after refresh. MA's separate local/public transport probes return
200 audio/mpeg matching the stored MP3 hash, 206 for an exact byte slice and 416
for invalid Range. WI028 independently matched the API-downloaded original's
10485760 bytes and SHA-256 to the fixture/original; it did not issue that request.
The actual browser download showed a success toast, but its native event timed
out after 60s and no expected Downloads file was found. Browser OS save remains
UNKNOWN; the separate API artifact does not close that evidence gap and the
native event timeout alone is not a product defect. Final detail-route evidence
also covers actual mouse seek and resumed time progression after paused reload.
MA deactivated ID 33, the only new Track activated for testing; all 22 new rows
are READY/inactive with originals/MP3s retained. After MA's UI logout, WI028's
final preservation checkpoint passed for the original 78 files, 22 new media
pairs, bounded original Track projection and frozen backup hashes. MA then
confirmed the public Login header with no admin identity, four local/public
HTTP 200 checks and unchanged listener PIDs 18924/25196. These browser/HTTP
observations are MA-supplied, not qa-integ actions. REQ006 and WI028 are closed
for the named bounded verification, with browser OS-save UNKNOWN preserved.

The ten historical missing references remain; strict-on-startup is false.
Successful schema validation/HTTP startup is not strict integrity readiness.
MA's explicit child cleanup does not prove automatic orphan containment, and one
no-thumbnail near-cap request does not establish every proxy/thumbnail boundary.
Audio plus 10MiB-thumbnail multipart and actual mobile acceptance were not run.
UI/log maintenance observations are recorded in WI028 without source fixes.
No fresh DB, global manifest update, restore rehearsal or production GO follows.

## User-Acceptance Follow-up (2026-09-09)

The same approved REQ006/WI028 step 3 closes only the human-assisted actual-phone
download-save/open-play check: **PASS**. MA reviewed the supplied smartphone
native download-complete notification for the test33 WAV with `파일 열기`, and
the user explicitly confirmed local playback (`재생되네 굳`). See
[WI028 follow-up evidence](../../deliverables/agent/WI-20260909-ATS-028-evidence-pack.md#user-acceptance-follow-up-2026-09-09)
for the exact filename, account/Track context and evidence attribution.
This supersedes only the earlier blanket missing-save/mobile limitation; the
in-app native-event timeout remains historical UNKNOWN for that environment.
Separate API bytes/SHA evidence is unchanged. Browser brand/version, phone-local
bytes/SHA and whole 59-second playback/duration are independently unverified;
no desktop Chrome/Edge or full mobile regression PASS is claimed.
MA confirmed follow-up test33-only deactivation and logout through fresh CUA DOM;
the public Track API now returns the intended 404. Three other local/public
health checks returned 200. MA's unchanged original byte/SHA check concerns the
repository original, NOT the phone copy. Admin toast `0/0` is record-only display
debt. qa-integ changed documents only, with no runtime/DB/browser actions or
code changes; MA's active-state/UI cleanup is recorded above. No new WI,
expanded acceptance or production approval.
