---
version: 1.0
last_updated: 2026-09-09
project: ATS
owner: SE
category: agent
status: active
dependencies:
  - path: ../user/REQ-20260909-ATS-005.md
    reason: Approved 100MB WAV and 128kbps MP3 scope
---

# WI-20260909-ATS-023 Handoff

[WI HEADER]
WI ID: WI-20260909-ATS-023
REQ: REQ-20260909-ATS-005
Agent: se
Depends On: none
Blocks: WI-20260909-ATS-024, WI-20260909-ATS-025, WI-20260909-ATS-026

[WI SUMMARY]
Why: Preserve originals while serving full-length MP3, with durable single-worker processing and safe replacement.
Scope: Backend Java and tests, source configuration/schema and scoped manual SQL only. No frontend/product documentation ownership; the API contract must be communicated early for WI024.
DoD: Approved original/derivative behavior, bounded FFmpeg execution, durable restart/retry and stale-work fencing, safe file journal integration, 100MiB audio cap with multipart headroom and unchanged other-domain limits. Focused tests and compile pass.
Forbidden: Real DB/media/secret changes; running-server restart or external side effects; unrelated pending files; commits/push; shared worktree cleanup; dependencies installed globally; preview truncation; changing manual activation policy; automatically processing historical tracks.

[ACCEPTANCE CRITERIA]
- Preserve existing audioFile as original, add explicit stream reference and durable processing state with minimal schema changes. Reuse current storage root and denied tracks/audio prefix, not a publicly served sibling path.
- Existing MP3 uploads and historical rows still work; no automatic backfill. New WAV cannot be manually activated before processing finishes. Track starts inactive today: do not auto-publish it.
- Replacement holds previous audio/stream/duration/waveform until a verified result is committed. Metadata-only edits must not be overwritten by a delayed job. Concurrent replacement/deactivation and old retry/completion cannot resurrect stale state or delete referenced media.
- Use at most one encoding worker in this single-server deployment, short claim/finalize transactions, no long DB transaction around FFmpeg. Persist queue/claim state and recover interrupted work safely. Retained job files are protected by reference and integrity checks and mutation journal.
- Preserve MP3-compatible source sample rates/channels; for valid WAVs outside MP3 constraints, normalize only the derivative to a deterministic supported rate (96kHz to48kHz) and at most two channels. Never change original bytes, volume-normalize or truncate. Do not add an unrequested rejection of valid high-rate/multichannel WAV; cover such inputs with actual encoding tests.
- Decode/validate real content and cap audio at 104857600 bytes independently of global multipart settings. Bounded subprocess timeout/output, argument array, local paths only, no remote protocols or exposed command/stderr/secrets. MP3 CBR 128kbps with no forced shortening/normalization; validate output.
- Include an ADMIN-only status/retry contract with safe error codes, no filesystem keys in public DTOs. Propose exact fields/routes early before frontend delegation.
- Test file-pair rollback, interrupted claims/retry, stale completion, disk/transcode failures, oversize boundary, inactive/activation rules, MP3/legacy behavior, full stream Range and original download protection. Verify new pending references do not confuse startup integrity checks.
- schema.sql plus a non-destructive additive manual SQL source, never apply SQL to a live DB. Defaults/backfill must preserve old rows. New installation and retained-data procedures must be documentable.

[INPUT POINTERS]
Tier 0:
- docs/standards/core-principles.md
- docs/standards/development-standards.md
- docs/standards/documentation-standards.md
- docs/standards/glossary.md
Tier 1:
- docs/policies/security-policy.md
- docs/policies/quality-gates.md
Tier 2:
- docs/design/runtime-storage-operations.md
- docs/design/usecase/sound-track.md
- docs/design/db-schema.md
- docs/design/api-spec.md
- ../user/REQ-20260909-ATS-005.md
Files:
- src/main/java/com/atstudio/atstudio/service/TrackService.java
- src/main/java/com/atstudio/atstudio/service/DownloadService.java
- src/main/java/com/atstudio/atstudio/service/audio/AudioAnalysisService.java
- src/main/java/com/atstudio/atstudio/service/storage/
- src/main/java/com/atstudio/atstudio/entity/Track.java
- src/main/java/com/atstudio/atstudio/controller/TrackController.java
- src/main/resources/application.yml
- src/main/resources/schema.sql
- src/test/java/com/atstudio/atstudio/service/TrackServiceAudioProcessingTest.java
External reference: https://ffmpeg.org/ffmpeg-codecs.html#libmp3lame

[OUTPUT CONTRACT]
- deliverables/user/WI-20260909-ATS-023-summary.md
- deliverables/agent/WI-20260909-ATS-023-evidence-pack.md
- Apply create-wi-evidence-pack skill; record design choices, exact API/DDL, changed paths, executed tests, and outstanding runtime limits. Never claim mock tests are real encoding or a deployment.

[TRACEABILITY REQUIREMENTS]
Use file/method and test pointers. Keep output compact; put build logs in an ignored temporary evidence directory. Rollback must preserve existing media and schema data. Return changes directly in the shared workspace, no Git operations.
