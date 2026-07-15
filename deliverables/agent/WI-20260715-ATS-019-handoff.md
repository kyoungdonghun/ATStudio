[WI HEADER]
WI ID: WI-20260715-ATS-019
REQ: REQ-20260715-ATS-001
Agent: se
Depends On: -
Blocks: WI-20260715-ATS-021, WI-20260715-ATS-022

[WI SUMMARY]
Why: Make player state reflect real HTMLAudioElement outcomes so playback failures cannot appear as successful playback.
Scope (in/out): In: player store state/error lifecycle, PlayerBar feedback if required, focused frontend tests. Out: backend stream behavior, visual redesign, preview/transcoding features.
DoD: play/resume success sets playing state after promise resolution; rejected playback and media error/stalled states reset playing state and expose a useful user-visible error; time/duration/seek behavior remains stable.
Constraints/Forbidden: Preserve the existing player design and user workflows. Do not introduce new media libraries or change download/subscription policy. You are not alone in the codebase; do not revert unrelated edits and accommodate concurrent changes.

[ACCEPTANCE CRITERIA]
Functional:
- [ ] `isPlaying` becomes true only after `audio.play()` resolves.
- [ ] Rejected play/resume and media failures leave a coherent non-playing state.
- [ ] A user-visible Korean failure message is available without persistent false success.
- [ ] `loadedmetadata`, `timeupdate`, seek, next, repeat, and persisted queue behavior remain compatible.
Quality:
- [ ] Focused player tests cover play resolution, rejection, time progression, and error reset.
- [ ] Typecheck and scoped ESLint pass.

[INPUT POINTERS]
Tier 0 (Constitution - Required for all):
- docs/standards/core-principles.md
- docs/standards/development-standards.md
Tier 1 (Policies):
- docs/policies/quality-gates.md
Tier 2 (Tech Stack / Context):
- .agents/skills/react-best-practices/AGENTS.md
- deliverables/user/REQ-20260715-ATS-001.md
- docs/ui/index.md
- docs/design/usecase/sound-track.md
Files:
- frontend/src/store/playerStore.ts
- frontend/src/layouts/PlayerBar.tsx
- frontend/src/store/playerStore.test.ts
- frontend/src/layouts/PlayerBar.test.tsx
Repro/Logs:
- Browser observation: UI entered playing state immediately while currentTime stayed 0:00 and the progress bar did not move.

[OUTPUT CONTRACT]
User-facing -> deliverables/user/WI-20260715-ATS-019-summary.md
Agent-facing -> deliverables/agent/WI-20260715-ATS-019-evidence-pack.md
Handoff Packet -> deliverables/agent/WI-20260715-ATS-019-handoff.md

[TRACEABILITY REQUIREMENTS]
Evidence pointers: Required for audio event and UI feedback paths.
Tests: Record exact npm commands and outcomes.
Rollback: Describe the minimal player-only rollback.
