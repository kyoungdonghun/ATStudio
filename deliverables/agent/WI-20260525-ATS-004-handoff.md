[WI HEADER]
WI ID: WI-20260525-ATS-004
REQ: REQ-20260525-ATS-004
Agent: sa/pg
Depends On: REQ approval
Blocks: WI-20260525-ATS-005, WI-20260525-ATS-006

[WI SUMMARY]
Why: Refund execution moves real money and needs explicit ledger, idempotency, and sensitive-data boundaries before implementation.
Scope (in/out): Define backend refund ledger/API/provider boundaries for Toss cancel. Exclude admin UI, entitlement correction, settlement, tax invoice, and cash receipt mutation.
DoD: Refund state model, provider cancel contract, security boundaries, and test expectations are clear enough for implementation.
Constraints/Forbidden: Do not store raw provider payload, raw card data, billing keys, auth keys, customer keys, or secrets.

[ACCEPTANCE CRITERIA]
Functional:
- [ ] Refund request, approve, execute, read, and preview boundaries are defined.
- [ ] Full and partial refund rules are defined.
- [ ] Entitlement correction remains a separate workflow.
Quality:
- [ ] Implementation can be verified through provider, service, and controller tests.
- [ ] Documentation update scope is known.

[INPUT POINTERS]
Tier 0:
- docs/standards/core-principles.md
- docs/standards/development-standards.md
- docs/policies/security-policy.md

Tier 2:
- docs/design/payment-refund-receipt-settlement-policy.md
- docs/design/payment-operations-runbook.md
- docs/design/payment-integration-design.md
- docs/design/api-spec.md
- docs/design/db-schema.md
- docs/SR/SR-93.md
- deliverables/user/REQ-20260525-ATS-004.md

[OUTPUT CONTRACT]
User-facing -> deliverables/user/WI-20260525-ATS-004-summary.md
Agent-facing -> deliverables/agent/WI-20260525-ATS-004-evidence-pack.md
Handoff Packet -> deliverables/agent/WI-20260525-ATS-004-handoff.md

[TRACEABILITY REQUIREMENTS]
Evidence pointers, implementation assumptions, provider API constraints, tests, and rollback notes are required.
