---
version: 1.0
last_updated: 2026-09-09
project: ATS
owner: DocOps
category: agent
status: active
dependencies:
  - path: ../user/REQ-20260909-ATS-005.md
    reason: Approved audio scope
  - path: WI-20260909-ATS-024-evidence-pack.md
    reason: Completed frontend contract
  - path: WI-20260909-ATS-023-handoff.md
    reason: Backend source contract; test closure still pending at dispatch
---

# WI-20260909-ATS-025 Handoff

[WI HEADER]
WI ID: WI-20260909-ATS-025
REQ: REQ-20260909-ATS-005
Agent: docops
Depends On: WI023 implementation contract available (test closure pending); WI024 complete
Blocks: WI-20260909-ATS-026

[WI SUMMARY]
Why: Keep source requirements, API/storage/database/install guidance aligned without confusing a source change with deployment or a verified MySQL manifest.
Scope: Relevant current docs/design/usecase/sound-track.md, docs/design/api-spec.md, docs/design/db-schema.md, docs/design/runtime-storage-operations.md, scripts/database/README.md, necessary index links and one concise audio-processing operating guide if useful. Add current evidence to REQ005 only; preserve unrelated REQ004/reconstructionREADME/WI021022 and frozen external private backup.
DoD: Accurate current implementation and pending deployment matrix, safe FFmpeg provisioning/configuration and bounded processing/retry procedures, script/source manifest caveat, documentation validation.
Forbidden: Product/tests/config/SQL edits, real DB/media/server changes, external effects, commits/push, rewriting historical PASS evidence into new validation.

[ACCEPTANCE CRITERIA]
- 100MiB per audio (104857600 bytes, UI 100MB), multipart total120MB, unrelated domain caps unchanged. MP3 128kbps CBR full length and original download, no short preview.
- Existing audioFile remains original. Nine added Track columns plus queue index; table count43 unchanged. Existing legacy/MP3 rows direct-play with no backfill; new WAV blocked from activation until verified, manual activation retained. Existing media analysis retained during asynchronous replacement; editable title/tags are not overwritten by late completion.
- Exact implemented admin audioProcessing states, generation-fenced retry endpoints, safe error codes, unsupported input policy, polling60 automatic reads at3s then manual, worker single-server limitation and interrupted-job retry behavior.
- Current manual SQL is scripts/database/manual-wi023-audio-processing.sql; source only, never applied to actual MySQL. Existing startup/data/backup tuple preserved. Additive migration on stopped writer with approved target/backup, validate schema and strict integrity before enabling; no need to create extra DBs or rewrite existing originals. Old binary rollback is unsafe for newly stream-required rows; no automatic rollback/deletion.
- Existing43-table/511-column live manifest is historical, new schema expectation UNRECORDED. Do not label old release/recovery proofs as current proof. Current private backup still represents pre-feature schema and same liveDB; it was not changed by this feature.
- FFmpeg binary is an external runtime dependency; locate through explicit app.audio.ffmpeg-path/actual environment mapping. Include startup/provision check, encoder availability, timeout, worker switch, restart/retry/worker-halt procedure. Do not embed private paths or binary in Git.
- Source changes are not deployed to pinned backend; Vite may HMR and show100MB while old backend still enforces30MB. All live 100MB/processing/browser claims remain unverified until DB/server rollout.
- Cloudflare Free/Pro official100MB request limit can reject audio+thumbnail/multipart at boundary. This is a deployment condition, not fixed by backend120MB alone. No chunked-upload/R2/plan changes were implemented.
- Parent native-tool sanity: official-linked Gyan9.0.1 portable ZIP SHA256 FEC81AE03971D9DD4BE3EBE02E263BD2EC1D789483F931BDBA5F5715E65DA2E9 verified. Synthetic5min WAV52920078bytes -> MP34801140bytes,300sec,128000bps,44100Hz/stereo, originalhash unchanged; local conversion1.470s. Near-cap fixture590sec104076078bytes ->9440905bytes,590sec,128000bps, originalhash unchanged,2.805s. These are local synthetic encoder checks, not music listening quality, production capacity, full app browser or DB install evidence. Fixtures under %LOCALAPPDATA%/ATStudio/validation/wav128-20260909/.

[INPUT POINTERS]
Tier 0:
- docs/standards/core-principles.md
- docs/standards/documentation-standards.md
- docs/standards/development-standards.md
- docs/standards/glossary.md
Tier 1:
- docs/policies/security-policy.md
- docs/policies/quality-gates.md
Tier 2:
- docs/design/runtime-storage-operations.md
- docs/design/usecase/sound-track.md
- docs/design/api-spec.md
- docs/design/db-schema.md
- scripts/database/README.md
- .agents/skills/validate-docs/SKILL.md
- .agents/skills/create-wi-evidence-pack/SKILL.md
Source:
- src/main/java/com/atstudio/atstudio/service/audio/
- src/main/java/com/atstudio/atstudio/entity/Track.java
- src/main/java/com/atstudio/atstudio/controller/TrackController.java
- scripts/database/manual-wi023-audio-processing.sql
- scripts/database/DisposableMysqlBootstrap.java
- frontend/src/components/track/AudioProcessingStatus.tsx
- WI023/024 source and evidence
External:
- https://ffmpeg.org/ffmpeg-codecs.html#libmp3lame
- https://ffmpeg.org/download.html
- https://www.gyan.dev/ffmpeg/builds/
- https://developers.cloudflare.com/support/troubleshooting/http-status-codes/4xx-client-error/error-413/

[OUTPUT CONTRACT]
- deliverables/user/WI-20260909-ATS-025-summary.md
- deliverables/agent/WI-20260909-ATS-025-evidence-pack.md
- Apply create-wi-evidence-pack. Record exact source/deployment/validation boundaries. If WI023 tests or parent full suites remain pending, leave them pending; parent will pass final evidence separately.

[TRACEABILITY REQUIREMENTS]
Keep docs current and linkable with appropriate metadata; do not multiply large reports. Preserve dated historical records and unrelated dirty docs. Run validate-docs and diff whitespace check; update only affected index counts. Report modified paths and any genuine policy gap to parent.
