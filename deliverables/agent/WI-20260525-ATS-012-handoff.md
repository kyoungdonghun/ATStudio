[WI HEADER]
WI ID: WI-20260525-ATS-012
REQ: REQ-20260525-ATS-005
Agent: docops
Depends On: WI-20260525-ATS-010
Blocks: WI-20260525-ATS-013

[WI SUMMARY]
Why: Entitlement correction changes API, DB, SR, runbook, policy, and acceptance documentation.
Scope (in/out): Update docs for correction APIs/table/audit actions/runbook/SR/checklist. Exclude documenting an admin UI tab that is not implemented.
DoD: API count, DB table count, SR-93, runbook, policy, and acceptance checklist match code.
Constraints/Forbidden: Do not claim user-facing refund request or admin UI exists.

[ACCEPTANCE CRITERIA]
Functional:
- [ ] API spec lists correction endpoints.
- [ ] DB schema lists correction table and audit actions.
- [ ] SR-93 marks P2-C backend completed.
- [ ] Runbook explains correction procedure.
Performance:
- [ ] N/A
Quality:
- [ ] docs validation passes.

[INPUT POINTERS]
Tier 0 (Constitution - Required for all):
- docs/standards/core-principles.md
- docs/standards/documentation-standards.md
- docs/standards/glossary.md

Tier 1 (Policies - Inferred from REQ/WI content):
- docs/policies/quality-gates.md

Tier 2 (Context):
- deliverables/user/REQ-20260525-ATS-005.md
- docs/design/api-spec.md
- docs/design/db-schema.md
- docs/design/payment-refund-receipt-settlement-policy.md
- docs/design/payment-operations-runbook.md
- docs/SR/SR-93.md
- deliverables/user/PAYMENT-FINAL-ACCEPTANCE-CHECKLIST-20260525.md

[OUTPUT CONTRACT]
User-facing -> deliverables/user/WI-20260525-ATS-012-summary.md
Agent-facing -> deliverables/agent/WI-20260525-ATS-012-evidence-pack.md
Handoff Packet -> deliverables/agent/WI-20260525-ATS-012-handoff.md

[TRACEABILITY REQUIREMENTS]
Evidence pointers (files/lines/commands/logs): Required
Tests: docs validation required
Rollback: Document doc count/version rollback
