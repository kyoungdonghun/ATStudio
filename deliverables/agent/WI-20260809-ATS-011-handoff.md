[WI HEADER]
WI ID: WI-20260809-ATS-011
REQ: REQ-20260808-ATS-004
Agent: qa-fe
Depends On: WI-20260809-ATS-007, WI-20260809-ATS-010
Blocks: WI-20260808-ATS-029

[WI SUMMARY]
Why: Repair confirmed PlayableTrack, duration, and taxonomy-state gaps from WI-007.
Scope (in/out): Normalize omitted nullable aggregate fields, reset duration on track change, preserve visible active URL filters when taxonomy loading fails or omits values, and add focused tests/docs. Playback/download policy and visual redesign are out of scope.
DoD: Omitted nullable keys cannot break playback; selected track duration is immediately correct; active URL/API filters remain visible and removable during taxonomy failure; focused tests pass.
Constraints/Forbidden: No backend/schema/data mutation, external calls, secrets/ZIP, unrelated UI redesign, commit, or push.

[ACCEPTANCE CRITERIA]
Functional:
- [ ] Mapper accepts omitted nullable thumbnail/waveform keys and normalizes them to null.
- [ ] `play()` updates duration with current Track identity before media metadata arrives.
- [ ] Taxonomy requests fail independently and selected URL values remain visible with Usage display-only `#`.
Performance:
- [ ] No new per-track request fan-out or unbounded taxonomy retries.
Quality:
- [ ] Focused mapper/store/list tests cover all repaired cases.

[INPUT POINTERS]
Tier 0:
- docs/standards/core-principles.md
- docs/standards/development-standards.md
Context:
- deliverables/user/WI-20260809-ATS-007-summary.md
- deliverables/agent/WI-20260809-ATS-007-evidence-pack.md
- docs/SR/SR-100.md
- docs/SR/SR-101.md
Files:
- frontend playableTrack utility/types/tests
- frontend playerStore/PlayerBar/tests
- frontend TrackListPage/tag API/filter components/tests
- affected current-state UI docs

[OUTPUT CONTRACT]
User-facing -> deliverables/user/WI-20260809-ATS-011-summary.md
Agent-facing -> deliverables/agent/WI-20260809-ATS-011-evidence-pack.md
Handoff Packet -> deliverables/agent/WI-20260809-ATS-011-handoff.md

[TRACEABILITY REQUIREMENTS]
Patch, tests, failure-state behavior, risks, rollback, and WI-029 unblock status are required.
