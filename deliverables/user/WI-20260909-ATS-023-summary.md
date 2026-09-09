---
version: 1.4
last_updated: 2026-09-09
project: ATS
owner: SE
category: reference
status: stable
dependencies:
  - path: REQ-20260909-ATS-005.md
    reason: Approved implementation scope
  - path: ../agent/WI-20260909-ATS-023-evidence-pack.md
    reason: Exact source and verification evidence
---

# WI-20260909-ATS-023 Summary

Backend source now preserves WAV originals for entitled downloads and queues
full-length 128kbps MP3 generation. New WAVs remain inactive until ready and still
require explicit activation. Replacement retains the current media pair until a
verified latest result commits. ADMIN status and generation-fenced retry are exposed.

Audio cap is 104857600 bytes, multipart file/request limits are 100MB/120MB, and other
attachment-domain limits are unchanged. Track gains nine additive columns and one
queue index, with no new table and no automatic historical processing. Existing
MySQL manifest evidence is retained as historical; changed-source Create/Validate
is blocked pending a separately approved observation.

Derivative encoding preserves MP3-compatible source rates and mono/stereo. Otherwise
it selects the nearest rate from 16/22.05/24/32/44.1/48kHz (lower on a tie) and uses
standard FFmpeg downmix to at most two channels. Original bytes remain unchanged;
there is no volume normalization or truncation.

The codec-compatibility focused rerun passed all 61 tests in 33 seconds, with
no failures or skips and main/test compilation executed. Ten cases ran actual FFmpeg
through H2/temporary-root application processing, including 96kHz stereo, 44.1kHz
six-channel, 96kHz six-channel and 8kHz mono inputs. Each verified the full original
SHA-256, expected output rate/channels, 128000 bitrate, duration and byte bound.
The preceding broader lifecycle run passed 209 tests with one Windows symlink skip;
that 210-test selection was not repeated after this narrow correction. Non-DB
bootstrap guards passed 26 checks.

Subsequent WI026 hardening adds a 256MiB expected output budget including 64KiB
framing allowance, with `AUDIO_OUTPUT_TOO_LARGE` before process construction when
exceeded, plus an explicit minimal child-environment allowlist. Its final focused
compile/test rerun passed 50/50 in 31 seconds, with no failures or skips: 28 encoder
cases, 20 pipeline cases (including all ten actual encoding cases under the allowlist)
and two configuration cases. Budget boundaries and synthetic secret omission passed;
no actual secret environment was inspected or dumped. WI023 implementation is
complete within its bounded source/test scope; whole-suite validation remains with MA/WI026.

A later parent full build failed (1885 total, seven failures, 19 skips) because the
concurrency test still blocked in audio analysis, now before the row lock. MP3 still
uses `replace`. Only that test file was corrected: the barrier now asserts the held
lock inside replacement, all seven writer/timeouts/media/counter checks remain, and
a new case verifies analysis-phase writers commit and retain their latest values
after replacement. Focused verification passed 9/9 in 27 seconds; no production
source change was required. Full failure and focused evidence were preserved, and
whole-build revalidation remains pending after CR's separate security work.

No actual DB/media migration, deployment/restart, external charge/mail, commit or push
was performed. MA owns frontend/docs coordination, runtime provisioning and final
independent WI026 verification.
