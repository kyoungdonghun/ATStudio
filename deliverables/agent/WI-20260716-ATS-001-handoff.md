[WI HEADER]
WI ID: WI-20260716-ATS-001
REQ: REQ-20260715-ATS-001
Agent: se
Depends On: WI-20260715-ATS-024
Blocks: stable demo checkpoint refresh

[WI SUMMARY]
Why: Playback time advances, but the Track detail page omits `waveformData` when mapping its API response into the player Track, so `WaveformCanvas` receives an empty peak list and renders only the flat-line fallback with no green progress bars.
Scope (in/out): In: Track detail-to-player mapping, focused regression test, WI summary/evidence. Out: player visual redesign, waveform generation, backend/API changes, duration metadata correction, playback/download policy changes, database/data mutation, broad audit remediation.
DoD: Starting a Track from the detail page forwards its existing `waveformData` unchanged; PlayerBar can render the existing bars and accent progress; a focused test fails if the field is omitted again.
Constraints/Forbidden: Preserve public full-length listening and protected Official Download. Do not synthesize fake peaks, add a new waveform library, change Track API contracts, or modify unrelated UI. You are not alone in the codebase; do not revert unrelated edits or stage runtime logs.

[ACCEPTANCE CRITERIA]
Functional:
- [ ] Detail-page playback passes `track.waveformData` to `usePlayerStore.play`.
- [ ] Existing missing/null waveform data continues to use the flat-line fallback.
- [ ] A real detail-page Track with stored peaks renders waveform bars and accent progress while current time advances.
- [ ] Playback, seek, and Official Download behavior remain unchanged.
Quality:
- [ ] Focused Track detail test asserts the complete mapping contract, including `waveformData`.
- [ ] Frontend typecheck, scoped ESLint, focused/full Vitest as appropriate, Prettier, and diff check pass.

[INPUT POINTERS]
Tier 0 (Constitution - Required for all):
- docs/standards/core-principles.md
- docs/standards/development-standards.md
Tier 1 (Policies):
- docs/policies/quality-gates.md
Tier 2 (Tech Stack / Context):
- .agents/skills/react-best-practices/AGENTS.md
- deliverables/user/REQ-20260715-ATS-001.md
- deliverables/agent/WI-20260715-ATS-019-evidence-pack.md
- deliverables/agent/WI-20260715-ATS-024-evidence-pack.md
- docs/design/usecase/sound-track.md
Files:
- frontend/src/pages/public/TrackDetailPage.tsx
- frontend/src/pages/public/TrackDetailPage.test.tsx (create if no focused test exists)
- frontend/src/layouts/PlayerBar.tsx (read-only unless evidence proves another defect)
- frontend/src/components/player/WaveformCanvas.tsx (read-only unless evidence proves another defect)
Repro/Logs:
- User screenshot: playback time `6:35 / 7:26` advances while the desktop waveform area shows only a flat gray line.
- `TrackDetail.waveformData` exists and Track list mapping forwards it, but `TrackDetailPage` manual player mapping omits it.

[OUTPUT CONTRACT]
User-facing -> deliverables/user/WI-20260716-ATS-001-summary.md
Agent-facing -> deliverables/agent/WI-20260716-ATS-001-evidence-pack.md
Handoff Packet -> deliverables/agent/WI-20260716-ATS-001-handoff.md

[TRACEABILITY REQUIREMENTS]
Evidence pointers: Required for API type, detail mapping, fallback behavior, and regression test.
Tests: Record exact npm commands and outcomes.
Rollback: Describe the mapping/test-only rollback; no data rollback is permitted or required.
