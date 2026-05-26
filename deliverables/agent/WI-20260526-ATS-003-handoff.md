[WI HEADER]
WI ID: WI-20260526-ATS-003
REQ: REQ-20260526-ATS-001
Agent: pg
Depends On: WI-20260526-ATS-001
Blocks: WI-20260526-ATS-005

[WI SUMMARY]
Why: Settlement files are financial evidence and may contain provider identifiers, so import/storage/display boundaries need explicit review.
Scope (in/out): In scope is security review of CSV fields, DTO responses, audit logs, and admin-only boundaries. Out of scope is feature implementation.
DoD: Sensitive data boundaries are documented and reflected in implementation/doc checks.
Constraints/Forbidden: Do not expose or invent secret values. Do not recommend storing raw provider payloads or card data.

[ACCEPTANCE CRITERIA]
Functional:
- [ ] Admin-only access boundary is confirmed.
- [ ] Allowed support-safe settlement fields are listed.
- [ ] Forbidden fields are listed.
Performance:
- [ ] N/A.
Quality:
- [ ] Findings are reflected in docs/evidence.

[INPUT POINTERS]
Tier 0 (Constitution - Required for all):
- docs/standards/core-principles.md

Tier 1 (Policies - Required):
- docs/policies/security-policy.md

Tier 2 (Design):
- docs/design/payment-refund-receipt-settlement-policy.md
- docs/design/api-spec.md
- docs/design/db-schema.md

REQ/Context Docs:
- deliverables/user/REQ-20260526-ATS-001.md
- deliverables/agent/WI-20260526-ATS-001-evidence-pack.md

[OUTPUT CONTRACT]
User-facing -> deliverables/user/WI-20260526-ATS-003-summary.md :
- Security/privacy review summary.
Agent-facing -> deliverables/agent/WI-20260526-ATS-003-evidence-pack.md :
- Findings, accepted mitigations, residual risks.
Handoff Packet -> deliverables/agent/WI-20260526-ATS-003-handoff.md :
- This packet.

[TRACEABILITY REQUIREMENTS]
Evidence pointers: Required.
Tests: N/A unless implementation checks are added.
Rollback: Revert any documentation/security policy edits from this WI.
