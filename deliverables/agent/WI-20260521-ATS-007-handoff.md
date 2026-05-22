[WI HEADER]
WI ID: WI-20260521-ATS-007
REQ: REQ-20260521-ATS-001
Agent: se
Depends On: WI-20260521-ATS-002
Blocks: WI-20260521-ATS-008, WI-20260521-ATS-010

[WI SUMMARY]
Why: Add user email notifications for payment or renewal failure using the existing mail infrastructure.
Scope (in/out): In scope: failure email trigger, safe template/content, duplicate guard where feasible. Out of scope: marketing emails and broad notification center.
DoD: Payment failure states notify the affected user without exposing sensitive data.
Constraints/Forbidden: No raw authKey, customerKey, billing key, or provider payload in email.

[ACCEPTANCE CRITERIA]
Functional:
- [ ] Renewal failure sends a safe user email.
- [ ] Final grace failure email copy is distinct from retry email where feasible.
- [ ] Email sending failures do not break payment transaction state.
Quality:
- [ ] Unit tests cover email trigger boundary.

[INPUT POINTERS]
Tier 0:
- docs/standards/core-principles.md
- docs/standards/development-standards.md
Tier 1:
- docs/policies/security-policy.md

REQ/Context Docs:
- deliverables/user/REQ-20260521-ATS-001.md
- docs/SR/SR-93.md

Files:
- src/main/java/com/atstudio/atstudio/service/RecurringRenewalService.java
- src/main/java/com/atstudio/atstudio/service
- src/main/resources/application.yml

[OUTPUT CONTRACT]
User-facing -> deliverables/user/WI-20260521-ATS-007-summary.md
Agent-facing -> deliverables/agent/WI-20260521-ATS-007-evidence-pack.md
Handoff Packet -> deliverables/agent/WI-20260521-ATS-007-handoff.md

[TRACEABILITY REQUIREMENTS]
Evidence pointers: Required
Tests: Include email-focused tests
Rollback: Document mail trigger changes
