---
version: 1.0
last_updated: 2026-09-09
project: ATS
owner: SE
category: guide
status: stable
dependencies:
  - path: ../../../deliverables/agent/WI-20260909-ATS-027-handoff.md
    reason: Helper and private fixture ownership
  - path: ../../../deliverables/user/REQ-20260909-ATS-006.md
    reason: Approved bounded runtime acceptance
---

# WI027 WAV Browser Acceptance Helpers

These helpers prepare synthetic inputs and inspect explicitly mapped local media.
They do not access MySQL, submit HTTP requests, upload files, or prove browser
acceptance. The launcher is the sole exception to the process boundary: MA owns
its invocation and it can start the backend only when invoked without CheckOnly.
SE handed launcher ownership to MA after a successful CheckOnly and will not edit
it while that runtime is in use.

## Private Fixtures

Root: `%LOCALAPPDATA%\ATStudio\validation\wav-browser-20260909\`.

| Relative path | Count | Bytes per file | Duration seconds |
|---|---:|---:|---:|
| `batch20/WI027-sine-01.wav` through `WI027-sine-20.wav` | 20 | 10485760 | 59.442834467 |
| `edge/WI027-small-valid.wav` | 1 | 352844 | 2 |
| `edge/WI027-corrupt.wav` | 1 | 51 | Not decodable |
| `edge/WI027-near-100MiB.wav` | 1 | 104853504 | 594.407369615 |
| `edge/WI027-over-100MiB.wav` | 1 | 104861696 | 594.453809524 |

Valid inputs are stereo, 44100Hz, signed 16-bit little-endian PCM WAVs. The batch
uses twenty distinct sine frequencies and SHA-256 values. Total fixture bytes:
419783295. Boundary files are real encoded PCM, 4096 bytes below/above the
104857600-byte limit. The corrupt input is intentionally not a RIFF/WAVE container.
It tests upload analysis failure, not necessarily asynchronous worker retry.

`fixture-manifest.json` records each exact path, size, frame count, duration and
SHA-256, plus FFmpeg version/hash and generation arguments. Generation reads all
PCM frames to verify payload length. It refuses existing targets, including an
existing manifest; it does not overwrite or remove partial/previous runs.

```powershell
# Already executed successfully. Repeating it now intentionally exits 1.
python -B scripts/validation/wav-browser/generate_fixtures.py
```

The known portable executables are under
`%LOCALAPPDATA%\ATStudio\tools\ffmpeg-128k-20260909\unpacked\ffmpeg-9.0.1-essentials_build\bin\`.
No package installation is performed. FFmpeg children receive an OS/locale-only
environment allowlist, excluding application secrets and FFREPORT.

## Stored Pair Probe

MA provides a private UTF-8 JSON array with exactly these keys per record:

```json
[
  {
    "track_id": 123,
    "fixture_name": "WI027-sine-01.wav",
    "original_relative_path": "tracks/audio/REPLACE_WITH_REAL_ORIGINAL.wav",
    "stream_relative_path": "tracks/audio/REPLACE_WITH_REAL_DERIVATIVE.mp3",
    "state": "READY"
  }
]
```

This is a schema example, not an observed Track. Do not include credentials,
emails, account details or additional DB columns. Resolve storage keys against
the existing repository `uploads` root. Only `tracks/audio/` relative keys and
known valid WI027 fixtures are accepted. Empty lists, duplicate Track/fixture
identities, unknown fields and non-READY records fail closed. The supplied state
is not independently queried from MySQL.

```powershell
$private = Join-Path $env:LOCALAPPDATA 'ATStudio/validation/wav-browser-20260909'
python -B scripts/validation/wav-browser/probe_pairs.py `
  --records "$private/ma-records.json" `
  --output "$private/pair-results-01.json"
```

Both input and output must stay below the private root. Use a fresh report name;
existing reports are never overwritten. The probe checks source-manifest and
stored-original SHA-256, reports derivative SHA-256, requires MP3 nominal audio
stream bitrate 128000bps and stereo/44100Hz, and compares both ffprobe duration
and complete FFmpeg decode duration with the fixture (0.25-second tolerance).
Full decode rejects command errors and detects shortened media even when header
duration claims are stale. It does not assert per-frame CBR or perceptual identity
of two different same-length derivatives.

Exit 0 means all listed local pairs passed; exit 1 means one or more pair failures;
exit 2 means invalid input/invocation. Reports omit actual storage keys and native
stderr. No received MA record list has been executed by SE at handoff time.
Official Download bytes, UI queue behavior, activation, playback/seek, public
proxy limits and restart persistence remain MA/WI028 evidence responsibilities.

## Launcher Handoff

`start-backend-wav128.ps1` derives from the preserved external
`remote-development-20260908/start-backend-remediation.ps1`, which was not modified.
It requires PowerShell 7.4+, the exact new runtime JAR path, caller-supplied JAR
and local-config hashes, an HTTPS public origin, approved path ownership/no
reparse points, a clean inherited app/JVM environment, and a free 8080 port.

It retains root `application-local.yml`, localhost:3306/atstudio, the existing
uploads/private-uploads pair, loopback binding, bootstrap false, schema validate,
existing CORS/callback origins and external SMTP configuration. It removes the
historical `createDatabaseIfNotExist` query key only from the explicit child JDBC
URL, retains credential-query rejection, and never rewrites the local config.
It explicitly selects the portable FFmpeg and enables the audio worker. New
runtime logs remain outside the repository. No old-JAR/hash-baseline dependency
is needed; the exact new JAR and reviewed local-config hashes are guarded.

MA owns final single-writer/job disposition and uses `-CheckOnly` before launch.
`-RuntimeSafetyReviewed` acknowledges MA's evidence; it does not suppress
schedulers, recovery or SMTP startup checks. SE's CheckOnly returned
`CHECKS_ONLY_NOT_APPLIED` with port FREE and no process started.

## Verification And Retention

```powershell
python -B scripts/validation/wav-browser/test_helpers.py
python -B .agents/skills/validate-docs/scripts/validate_docs.py
git diff --check -- scripts/validation/wav-browser
```

The nine helper tests use synthetic media in a fresh private `self-test/<id>/`
directory and retain their JSON evidence and MP3s. They do not read or mutate
runtime storage or query DB rows. Existing fixtures, runtime media, configuration,
DB objects and old launchers are never deleted. Stop using the helpers to roll
back preparation; retain all generated evidence. Removing artifacts or reverting
a running backend requires a separately approved MA operation.
