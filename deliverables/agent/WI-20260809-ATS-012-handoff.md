[WI HEADER]
WI ID: WI-20260809-ATS-012
REQ: REQ-20260808-ATS-004
Agent: se
Depends On: WI-20260808-ATS-028 final re-review
Blocks: WI-20260808-ATS-028

[WI SUMMARY]
Why: Remove automatic raw operator free-text duplication from rejection audit rows.
Scope (in/out): Role-change and subscription-correction execution rejection audit payloads plus focused tests/docs. Approved workflow/success notes remain unchanged; no DLP or schema work.
DoD: Every rejection audit stores stable action/target/error/state but null free-text reasonNote; focused tests pass.
Constraints/Forbidden: No schema/data/external calls, secrets/ZIP, unrelated changes, commit, or push.

[ACCEPTANCE CRITERIA]
Functional:
- [ ] Role-change and correction rejection audits omit raw operator text.
- [ ] Original workflow/success notes remain available in their approved records.
Quality:
- [ ] Adversarial note tests prove rejection rows contain no copied text.

[INPUT POINTERS]
- docs/standards/core-principles.md
- docs/standards/development-standards.md
- docs/policies/security-policy.md
- deliverables/user/WI-20260808-ATS-028-summary.md
- UserService, AdminSubscriptionCorrectionService, AdminOperationRejectionAuditService and focused tests/docs

[OUTPUT CONTRACT]
User-facing -> deliverables/user/WI-20260809-ATS-012-summary.md
Agent-facing -> deliverables/agent/WI-20260809-ATS-012-evidence-pack.md
Handoff Packet -> deliverables/agent/WI-20260809-ATS-012-handoff.md

[TRACEABILITY REQUIREMENTS]
Patch, tests, retained/omitted fields, risks, rollback, and WI-028 status are required.
