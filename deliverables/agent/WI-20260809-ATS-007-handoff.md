[WI HEADER]
WI ID: WI-20260809-ATS-007
REQ: REQ-20260808-ATS-004
Agent: cr
Depends On: WI-20260809-ATS-006
Blocks: WI-20260808-ATS-029

[WI SUMMARY]
Why: Review frontend media, tag, image, and playback contracts separately from backend implementation.
Scope (in/out): Usage-tag display and URL state, image upload/preview, PlayableTrack hydration, player buffering/error/race behavior, and catalog integration. Backend internals are covered by WI-006.
DoD: Evidence-backed BLOCKER/MAJOR/MINOR findings or explicit no-findings, with tight file-line pointers and residual browser/test risks.
Constraints/Forbidden: Read-only review. Do not modify code, tests, data, secrets, the intentional ZIP, or external services. Do not commit or push.

[ACCEPTANCE CRITERIA]
Functional:
- [ ] Review frontend portions of SR-94, SR-95, SR-98, SR-100, and SR-101.
- [ ] Verify URL/API/UI state, playback race handling, and failure-state accuracy.
Performance:
- [ ] Check request fan-out and render/media costs where supported by evidence.
Quality:
- [ ] Every finding has severity, evidence, impact, and a recommended repair/test.

[INPUT POINTERS]
Tier 0:
- docs/standards/core-principles.md
- docs/standards/development-standards.md

Context:
- deliverables/user/REQ-20260808-ATS-004.md
- docs/SR/SR-94.md
- docs/SR/SR-95.md
- docs/SR/SR-98.md
- docs/SR/SR-100.md
- docs/SR/SR-101.md
- deliverables/user/WI-20260809-ATS-006-summary.md

Files:
- frontend TagManage/TagFilter/Home/TrackList tag-related code and focused tests
- frontend TrackUpload/TrackEdit/TrackThumbnailField and focused tests
- frontend playerStore/PlayerBar/PlayableTrack utilities and focused tests
- catalog/history/playlist/like/download pages changed by PlayableTrack hydration
- frontend API wrappers and exact backend response DTO signatures

[OUTPUT CONTRACT]
User-facing -> deliverables/user/WI-20260809-ATS-007-summary.md
Agent-facing -> deliverables/agent/WI-20260809-ATS-007-evidence-pack.md
Handoff Packet -> deliverables/agent/WI-20260809-ATS-007-handoff.md

[TRACEABILITY REQUIREMENTS]
Reviewed components/contracts, findings, residual risks, rollback implications, and WI-029 block status are required.
