[WI HEADER]
WI ID: WI-20260809-ATS-014
REQ: REQ-20260808-ATS-004
Agent: qa-fe
Depends On: WI-20260808-ATS-029 final re-review
Blocks: WI-20260808-ATS-029

[WI SUMMARY]
Why: Align PlaylistEditPage reorder payload with the backend's zero-based order contract.
Scope (in/out): Frontend reorder mapping, focused component/API contract test, and affected current-state docs only.
DoD: A non-empty reordered playlist sends exactly `0..n-1`; focused test and frontend static gates pass.
Constraints/Forbidden: No backend/schema/data/external calls, secrets/ZIP, unrelated changes, commit, or push.

[ACCEPTANCE CRITERIA]
Functional:
- [ ] Reordered tracks use zero-based contiguous `trackOrder` values.
- [ ] A focused page-level test asserts the exact payload.
Quality:
- [ ] Focused test, typecheck, lint, and scoped Prettier pass.

[INPUT POINTERS]
- docs/standards/core-principles.md
- docs/standards/development-standards.md
- deliverables/user/WI-20260808-ATS-029-summary.md
- frontend/src/pages/subscriber/PlaylistEditPage.tsx
- relevant focused test/API wrapper and playlist API/use-case docs

[OUTPUT CONTRACT]
User-facing -> deliverables/user/WI-20260809-ATS-014-summary.md
Agent-facing -> deliverables/agent/WI-20260809-ATS-014-evidence-pack.md
Handoff Packet -> deliverables/agent/WI-20260809-ATS-014-handoff.md

[TRACEABILITY REQUIREMENTS]
Patch, exact payload, tests, risks, rollback, and WI-029 status are required.
