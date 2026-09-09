---
version: 1.0
last_updated: 2026-09-09
project: ATS
owner: MA
category: reference
status: draft
dependencies:
  - path: ../user/REQ-20260909-ATS-006.md
    reason: Approved live browser verification
---

# WI-20260909-ATS-027: Runtime Test Fixtures and Probes

[WI HEADER]
REQ: REQ-20260909-ATS-006
Agent: se
Depends On: none
Blocks: WI-20260909-ATS-028

[WI SUMMARY]
Prepare real deterministic WAV fixtures and minimal reproducible runtime probes while MA handles the immediate backup/deployment/browser path. Do not operate the browser or modify production source unless an evidenced defect is assigned.

[INPUT POINTERS]
Tier 0: docs/standards/core-principles.md; docs/standards/development-standards.md; docs/standards/documentation-standards.md; docs/standards/glossary.md.
Tier 1: docs/policies/security-policy.md.
Tier 2: docs/design/runtime-storage-operations.md; scripts/database/manual-wi023-audio-processing.sql; deliverables/agent/WI-20260909-ATS-023-evidence-pack.md; approved REQ006.

[SCOPE / OWNERSHIP]
Own new helper source under scripts/validation/wav-browser/ and private generated fixtures/evidence under LOCALAPPDATA/ATStudio/validation/wav-browser-20260909/. Own this WI summary/evidence. No existing unrelated files, DB rows, runtime processes, stored media or credentials may be modified. MA owns runtime side effects and browser.

[ACCEPTANCE CRITERIA]
- Generate 20 distinct valid stereo 44.1kHz PCM WAVs about 10MiB each with stable WI027-prefixed filenames; record byte size, duration, SHA256. Also one small valid WAV and one deliberately corrupt WAV for per-item failure testing. Add a valid near-100MiB WAV and an over-100MiB WAV for boundary tests if economical.
- Supply a probe that checks stored original/derivative pairs from a later MA-provided sanitized record list, SHA256, full duration and 128000bps via ffprobe. No direct unapproved database writes.
- Reuse known portable FFmpeg; do not install system packages. Keep synthetic data private except explicit UI uploads.
- If helpful, identify minimal existing DB backup/query helper paths by filename, without exposing secrets or duplicating MA runtime work.
- Provide exact paths and commands as soon as fixtures are available; continue independent preparation while MA tests.

[OUTPUT CONTRACT]
Use create-wi-evidence-pack skill. Produce deliverables/user/WI-20260909-ATS-027-summary.md and matching agent evidence with reproducible commands, actual results, limits, rollback (new fixtures retained; no existing data removal), changed files. Notify MA immediately when critical fixtures exist.

[PROHIBITIONS]
No new DB, deletion, payment/mail, commits/push, secret output, browser control or server restart. No sparse fake-size fixtures. Preserve all 79 baseline dirty files.
