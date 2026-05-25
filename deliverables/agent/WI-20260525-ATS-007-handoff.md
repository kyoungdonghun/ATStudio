[WI HEADER]
WI ID: WI-20260525-ATS-007
REQ: REQ-20260525-ATS-004
Agent: docops
Depends On: WI-20260525-ATS-005
Blocks: WI-20260525-ATS-008

[WI SUMMARY]
Why: Refund implementation changes API, DB, runbook, SR, and final acceptance documentation.
Scope (in/out): Update docs for `payment_refunds`, admin refund APIs, runbook, SR-93, policy, index counts, and acceptance checklist. Exclude UI docs for a tab that is not implemented.
DoD: Docs accurately reflect implemented backend behavior and deferred scopes.
Constraints/Forbidden: Do not describe admin UI as implemented unless frontend code exists.

[ACCEPTANCE CRITERIA]
Functional:
- [ ] API spec lists refund preview/request/approve/execute/list/detail endpoints.
- [ ] DB schema lists `payment_refunds`.
- [ ] SR-93 marks refund ledger/provider cancel backend as completed and entitlement correction as follow-up.
Quality:
- [ ] `validate_docs.py` passes.

[INPUT POINTERS]
Tier 0:
- docs/standards/core-principles.md
- docs/standards/documentation-standards.md
- docs/standards/glossary.md

Tier 2:
- docs/design/api-spec.md
- docs/design/db-schema.md
- docs/design/payment-refund-receipt-settlement-policy.md
- docs/design/payment-operations-runbook.md
- docs/SR/SR-93.md
- docs/index.md
- docs/design/index.md
- deliverables/user/REQ-20260525-ATS-004.md

[OUTPUT CONTRACT]
User-facing -> deliverables/user/WI-20260525-ATS-007-summary.md
Agent-facing -> deliverables/agent/WI-20260525-ATS-007-evidence-pack.md
Handoff Packet -> deliverables/agent/WI-20260525-ATS-007-handoff.md

[TRACEABILITY REQUIREMENTS]
Doc pointers, changed counts, validation command, and rollback notes are required.
