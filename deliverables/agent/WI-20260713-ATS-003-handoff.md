[WI HEADER]
WI ID: WI-20260713-ATS-003
REQ: REQ-20260713-ATS-001
Agent: se
Depends On: WI-20260713-ATS-002
Blocks: WI-20260713-ATS-006

[WI SUMMARY]
Why: Prevent public retrieval of original paid track assets while preserving bounded preview playback and protected subscriber download.
Scope (in/out): Implement public/admin TrackResponse separation, deny static original-audio routes, add bounded original-backed preview behavior, update directly affected frontend types, and add focused tests. Do not move stored files, mutate DB data, add transcoder dependencies, or change subscriber entitlement policy.
DoD: Public metadata contains no original key; USER and ADMIN cannot access `/uploads/tracks/audio/**`; preview-file Range behavior remains; original fallback is bounded and out-of-bound ranges are rejected; subscriber download regression tests pass.
Constraints/Forbidden: Follow `docs/design/p0-release-blocker-remediation-design.md`. Do not edit mail, withdrawal, billing, or reconciliation files. Do not touch runtime logs.

[ACCEPTANCE CRITERIA]
Functional:
- [ ] Public track detail has `audioFile: null`, while admin responses retain the key.
- [ ] Static original-audio route is denied to anonymous, USER, and ADMIN callers.
- [ ] Missing preview file never exposes the complete original through the stream endpoint.
- [ ] Subscriber download keeps its existing checks and original-resource result.
Performance:
- [ ] Range handling remains O(1) and does not copy an entire media file into memory.
Quality:
- [ ] Focused service/controller/security tests pass.
- [ ] Frontend typecheck and affected tests pass.

[INPUT POINTERS]
Tier 0:
- docs/standards/core-principles.md
- docs/standards/development-standards.md
Tier 1:
- docs/policies/security-policy.md
- docs/policies/quality-gates.md
Tier 2:
- docs/design/p0-release-blocker-remediation-design.md
- docs/design/api-spec.md
- docs/design/usecase/sound-track.md
REQ/Context Docs:
- deliverables/user/REQ-20260713-ATS-001.md
- deliverables/agent/WI-20260713-ATS-002-evidence-pack.md
Files (owned):
- src/main/java/com/atstudio/atstudio/config/SecurityConfig.java
- src/main/java/com/atstudio/atstudio/dto/track/TrackResponse.java
- src/main/java/com/atstudio/atstudio/service/TrackService.java
- src/main/java/com/atstudio/atstudio/controller/TrackController.java
- frontend/src/api/tracks.ts
- directly affected track frontend call sites
- focused track/security/download tests

[OUTPUT CONTRACT]
User-facing -> deliverables/user/WI-20260713-ATS-003-summary.md
Agent-facing -> deliverables/agent/WI-20260713-ATS-003-evidence-pack.md
Handoff Packet -> deliverables/agent/WI-20260713-ATS-003-handoff.md

[TRACEABILITY REQUIREMENTS]
Evidence pointers and exact test commands: Required
Rollback: Revert only this WI's owned product/test files; no stored-file rollback is needed.
