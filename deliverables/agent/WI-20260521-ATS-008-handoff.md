[WI HEADER]
WI ID: WI-20260521-ATS-008
REQ: REQ-20260521-ATS-001
Agent: re
Depends On: WI-20260521-ATS-003, WI-20260521-ATS-004, WI-20260521-ATS-007
Blocks: WI-20260521-ATS-011

[WI SUMMARY]
Why: Strengthen backend regression coverage for payment, renewal, expiration, and subscription access edge cases.
Scope (in/out): In scope: JUnit focused tests and full backend test run. Out of scope: frontend tests.
DoD: Backend changes have targeted tests and full suite passes.
Constraints/Forbidden: Do not weaken existing tests to pass.

[ACCEPTANCE CRITERIA]
Functional:
- [ ] One-time subscription payment rejection is tested.
- [ ] Renewal failure and final state are tested.
- [ ] Order expiration scheduler is tested.
- [ ] Missing billing agreement upgrade guidance/error is tested.
Quality:
- [ ] `gradlew.bat test` passes.

[INPUT POINTERS]
Tier 0:
- docs/standards/core-principles.md
Tier 1:
- docs/policies/quality-gates.md

REQ/Context Docs:
- deliverables/user/REQ-20260521-ATS-001.md

Files:
- src/test/java/com/atstudio/atstudio/service
- src/test/java/com/atstudio/atstudio/controller

[OUTPUT CONTRACT]
User-facing -> deliverables/user/WI-20260521-ATS-008-summary.md
Agent-facing -> deliverables/agent/WI-20260521-ATS-008-evidence-pack.md
Handoff Packet -> deliverables/agent/WI-20260521-ATS-008-handoff.md

[TRACEABILITY REQUIREMENTS]
Evidence pointers: Required
Tests: Include commands and results
Rollback: Document test-only changes
