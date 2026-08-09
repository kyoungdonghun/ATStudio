[WI HEADER]
WI ID: WI-20260809-ATS-006
REQ: REQ-20260808-ATS-004
Agent: cr
Depends On: WI-20260808-ATS-023~027
Blocks: WI-20260808-ATS-029

[WI SUMMARY]
Why: Review backend media, tag, image, and playable-track changes without mixing frontend behavior into the same context.
Scope (in/out): Tag normalization and collision handling, search/filter contracts, audio analysis atomicity, image normalization, PlayableTrack batching/query shape, authorization, and persistence consistency. Frontend UX is out of scope.
DoD: Evidence-backed BLOCKER/MAJOR/MINOR findings or explicit no-findings, with tight file-line pointers and residual test risks.
Constraints/Forbidden: Read-only review. Do not modify code, tests, schema, data, secrets, the intentional ZIP, or external state. Do not commit or push.

[ACCEPTANCE CRITERIA]
Functional:
- [ ] Review SR-94, SR-95, SR-98, SR-99, SR-100, and backend portions of SR-101.
- [ ] Verify transaction, validation, response, and persistence contracts.
Performance:
- [ ] Check batching/N+1 and media-memory risks where supported by code evidence.
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
- docs/SR/SR-99.md
- docs/SR/SR-100.md
- docs/SR/SR-101.md
- deliverables/agent/WI-20260808-ATS-029-handoff.md

Files:
- TagController/TagService/TagNamePolicy/TagNameConstraintTranslator and focused tests
- TrackController/TrackService/AdminTrackAudioAnalysisService/audio package and focused tests
- CanonicalImageService and focused tests
- PlayableTrackService and query-count/focused tests
- affected repositories, DTOs, entity fields, and schema portions only

[OUTPUT CONTRACT]
User-facing -> deliverables/user/WI-20260809-ATS-006-summary.md
Agent-facing -> deliverables/agent/WI-20260809-ATS-006-evidence-pack.md
Handoff Packet -> deliverables/agent/WI-20260809-ATS-006-handoff.md

[TRACEABILITY REQUIREMENTS]
Reviewed symbols, findings, residual risks, rollback implications, and WI-029 block status are required.
