---
version: 1.0
last_updated: 2026-09-09
project: ATS
owner: SE
category: work-summary
status: stable
dependencies:
  - path: REQ-20260909-ATS-006.md
    reason: Approved WAV browser acceptance scope
  - path: ../agent/WI-20260909-ATS-027-evidence-pack.md
    reason: Detailed commands, observations and boundaries
---

# WI-20260909-ATS-027 Summary

## Delivered

- Twenty unique real stereo 44100Hz PCM16 WAVs, each exactly 10MiB and
  59.442834467 seconds. Small valid, corrupt, near-limit and over-limit files bring
  the total to 24 fixtures / 419783295 bytes.
- Private manifest with all exact paths, sizes, frame counts, durations and
  SHA-256 values. Fixture root:
  `%LOCALAPPDATA%/ATStudio/validation/wav-browser-20260909/`.
- Read-only stored-pair probe under `scripts/validation/wav-browser/`, accepting
  an explicit sanitized MA record list. Original hashes, MP3 nominal 128000bps,
  format and complete decode duration are checked without DB/API access.
- Requested guarded launcher passed syntax parsing and CheckOnly (exit 0,
  port 8080 FREE). It preserves the existing DB/config/storage tuple, explicitly
  enables the portable FFmpeg worker, and removes historical database creation
  only from its child CLI URL. MA now owns the launcher; SE did not start it.

## Verified

Nine synthetic helper tests passed. They cover all fixture hashes/metadata,
boundary full decoding, corruption, wrong bitrate, shortened derivatives,
wrong originals and input/path guards. Generator rerun correctly refuses to
overwrite existing artifacts. The initial one-sample timestamp assertion failure
and corrected passing run are both retained in private evidence.

## Not Claimed

SE has not inspected an actual uploaded Track pair because no MA record list was
provided. Browser upload, queue/retry, activation, public playback/seek, Official
Download and restart persistence remain MA/WI028 work. Existing dirty files,
database/schema/global manifests and old runtime artifacts were not edited by SE.
MA confirmed its attempted launcher patch made no change, so there was no MA
source intervention to record.

## Handoff

Usage and fixture table: [Helper README](../../scripts/validation/wav-browser/README.md).
Evidence: [WI027 Evidence Pack](../agent/WI-20260909-ATS-027-evidence-pack.md).
WI027 preparation is ready for MA to trigger WI-20260909-ATS-028 / qa-integ.
REQ006 remains open for actual runtime/browser acceptance.
