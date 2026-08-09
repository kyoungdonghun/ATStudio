[WI HEADER]
WI ID: WI-20260809-ATS-015
REQ: REQ-20260808-ATS-004
Agent: cr
Depends On: WI-20260809-ATS-014
Blocks: WI-20260808-ATS-030

[WI SUMMARY]
Why: Independently verify that the final WI-029 Playlist reorder blocker is fully repaired end to end.
Scope (in/out): Review only the subscriber Playlist editor reorder payload, frontend API contract, backend zero-based validation/persistence, focused regression tests, and current-state docs. Do not broaden into unrelated SR findings or modify implementation.
DoD: Confirm the UI submits a contiguous zero-based `0..n-1` order for non-empty reordered Playlists, the backend accepts and persists that exact contract, tests protect the boundary, and WI-029 receives a final PASS/FAIL disposition with severity counts and evidence.
Constraints/Forbidden: Read-only implementation review. Only WI review deliverables may be edited. Do not inspect secrets or the intentional ZIP. Do not mutate schema/data, call external providers, commit, push, or alter the client branch.

[ACCEPTANCE CRITERIA]
Functional:
- [ ] Trace a non-empty Playlist reorder from rendered controls through `reorderTracks` to backend validation and persistence.
- [ ] Confirm exact zero-based values and contiguous membership semantics.
- [ ] Confirm focused tests would fail for the former one-based payload.
- [ ] Update WI-029 to a final PASS only if no BLOCKER/MAJOR/MINOR findings remain in this narrow rerun.
Performance:
- [ ] Confirm the repair does not add requests, loops over remote calls, or new query behavior.
Quality:
- [ ] Findings include exact file/line evidence and commands inspected or executed.
- [ ] WI-015 summary/evidence and the final WI-029 disposition agree.

[INPUT POINTERS]
Tier 0:
- docs/standards/core-principles.md
- docs/standards/development-standards.md
Tier 1:
- docs/policies/quality-gates.md
Context:
- deliverables/user/REQ-20260808-ATS-004.md
- deliverables/agent/WI-20260808-ATS-029-handoff.md
- deliverables/user/WI-20260808-ATS-029-summary.md
- deliverables/agent/WI-20260808-ATS-029-evidence-pack.md
- deliverables/user/WI-20260809-ATS-014-summary.md
- deliverables/agent/WI-20260809-ATS-014-evidence-pack.md
Files:
- frontend/src/pages/subscriber/PlaylistEditPage.tsx
- frontend/src/pages/subscriber/PlaylistEditPage.test.tsx
- frontend/src/test/coverage/adminSubscriberPages.coverage.test.tsx
- frontend/src/api/playlists.ts
- src/main/java/com/atstudio/atstudio/service/PlaylistService.java
- src/test/java/com/atstudio/atstudio/service/PlaylistServiceTest.java
- docs/design/api-spec.md
- docs/design/usecase/sound-playlist.md

[OUTPUT CONTRACT]
User-facing -> deliverables/user/WI-20260809-ATS-015-summary.md
Agent-facing -> deliverables/agent/WI-20260809-ATS-015-evidence-pack.md
Handoff Packet -> deliverables/agent/WI-20260809-ATS-015-handoff.md
Also update the final disposition in:
- deliverables/user/WI-20260808-ATS-029-summary.md
- deliverables/agent/WI-20260808-ATS-029-evidence-pack.md

[TRACEABILITY REQUIREMENTS]
Evidence pointers, exact contract values, commands/tests, severity counts, accepted residual risks, rollback notes, and WI-030 unblock status are required.
