---
version: 1.0
last_updated: 2026-09-09
project: ATS
owner: SE
category: agent
status: active
dependencies:
  - path: ../user/REQ-20260909-ATS-005.md
    reason: Approved upload and processing UX scope
  - path: WI-20260909-ATS-023-handoff.md
    reason: Backend API contract
---

# WI-20260909-ATS-024 Handoff

[WI HEADER]
WI ID: WI-20260909-ATS-024
REQ: REQ-20260909-ATS-005
Agent: se
Depends On: WI-20260909-ATS-023 API contract (received; implementation continues)
Blocks: WI-20260909-ATS-025, WI-20260909-ATS-026

[WI SUMMARY]
Why: Let administrators upload 100MB audio and distinguish uploaded bytes from a playable completed derivative.
Scope: frontend/src API wrapper, upload/edit/manage pages, narrowly scoped styles/helpers and matching tests only. Backend and product documentation are owned elsewhere.
DoD: 100MiB file checks in upload/edit, truthful queued/processing/failed/ready states, bounded cancellable refresh and generation-fenced retry, manual activation protection; focused tests/typecheck/lint/changed Prettier.
Forbidden: New packages, player redesign, subscription policy, backend edits, real account/catalog mutations, server restart, Git operations, touching baseline eight dirty docs or private handoff assets. Current Vite may hot reload changed source; do not call it a backend deployment.

[ACCEPTANCE CRITERIA]
- AUDIO_MAX_SIZE_MB=100 (104857600 bytes) used consistently by creation and replacement, exactly100 accepted/one byte over rejected. Existing image/document caps unchanged.
- Administrator API gets audioProcessing object: {trackId:number,state:'READY'|'PENDING'|'PROCESSING'|'FAILED'|'CANCELLED',generation:number,streamReady:boolean,retryAllowed:boolean,attemptCount:number,errorCode:string|null,updatedAt:string|null}. Public TrackResponse receives null. Preserve current AdminTrackDetail.audioFile.
- GET /api/tracks/admin/{trackId}/audio-processing returns ResponseDTO.data as above. POST same route plus /retry with {generation:number} returns 202 ResponseDTO.data. Conflict codes AUDIO_PROCESSING_CONFLICT and AUDIO_STREAM_NOT_READY; use safe human-facing Korean messages.
- New WAV stays inactive, streamReady:false until conversion. READY never auto-publishes. Replacement can be PROCESSING while streamReady:true; preserve usable current audio and metadata. Hide/disable activation only when no ready stream.
- Upload request success is not conversion success. Lists/edit show appropriate status and expose retry only when allowed. Prevent duplicate clicks and stale route/row responses; preserve existing deletion safety patterns. Limit status fetching to relevant visible records, stop polling when idle/unmounted/error/hidden as reasonable.
- Undefined processing info on historical frontend test fixtures or pinned old API must not crash the screen or label ordinary historical tracks as broken. Do not invent new processing success.
- Tests for 100MiB boundary using sized File stubs, queued success, failure/retry/conflict, replacement still playable, manual activation guard, stale response/poll cleanup. Existing upload/edit/manage tests remain valid.

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
- docs/standards/frontend-standards.md
- .agents/skills/react-best-practices/AGENTS.md
- docs/design/usecase/sound-track.md
- ../user/REQ-20260909-ATS-005.md
Files:
- frontend/src/api/tracks.ts
- frontend/src/utils/validation.ts
- frontend/src/pages/creator/TrackUploadPage.tsx
- frontend/src/pages/creator/TrackEditPage.tsx
- frontend/src/pages/admin/TrackManagePage.tsx
- Corresponding tests and CSS modules

[OUTPUT CONTRACT]
- deliverables/user/WI-20260909-ATS-024-summary.md
- deliverables/agent/WI-20260909-ATS-024-evidence-pack.md
- Apply create-wi-evidence-pack skill. Record exact diff/test commands and counts, API assumptions, unperformed live UI/encoding/deployment limits.

[TRACEABILITY REQUIREMENTS]
Use exact file/method and test pointers, minimal rollback advice without deleting unrelated work. No package dependency changes. Use existing components/icons and restrained operational UI; no explanatory marketing text. Request foreground test coordination before whole-suite Vitest; no overlapping heavy Gradle/Vitest jobs.
