[WI HEADER]
WI ID: WI-20260526-ATS-001
REQ: REQ-20260526-ATS-001
Agent: sa/docops
Depends On: -
Blocks: WI-20260526-ATS-002, WI-20260526-ATS-003

[WI SUMMARY]
Why: Settlement import/reconciliation must be designed before adding accounting-facing tables, APIs, and UI.
Scope (in/out): In scope is settlement source adapter design, CSV-first import policy, matching rules, admin API/UI contract, and documentation updates. Excel sources must be exported to CSV before import. Out of scope is backend/frontend implementation.
DoD: Design docs define source adapters, ledger fields, statuses, reconciliation rules, security boundary, and future Toss Settlement API extension path.
Constraints/Forbidden: Do not implement Toss Settlement API integration. Do not treat settlement data as subscription entitlement source. Do not include raw secrets or raw provider payload examples.

[ACCEPTANCE CRITERIA]
Functional:
- [ ] Settlement source abstraction is documented.
- [ ] CSV import template and validation rules are documented.
- [ ] Matching/mismatch rules are documented.
- [ ] Admin API/UI expectations are documented.
Performance:
- [ ] Design supports paginated admin reads.
Quality:
- [ ] Docs validation passes.
- [ ] `git diff --check` passes.

[INPUT POINTERS]
Tier 0 (Constitution - Required for all):
- docs/standards/core-principles.md
- docs/standards/development-standards.md
- docs/standards/documentation-standards.md
- docs/standards/glossary.md

Tier 1 (Policies - Inferred):
- docs/policies/security-policy.md

Tier 2 (Design/UI):
- docs/design/payment-refund-receipt-settlement-policy.md
- docs/design/payment-integration-design.md
- docs/design/api-spec.md
- docs/design/db-schema.md
- docs/SR/SR-93.md
- docs/ui/atstudio-front-list.md
- docs/ui/screen-flow.md

REQ/Context Docs:
- deliverables/user/REQ-20260526-ATS-001.md

[OUTPUT CONTRACT]
User-facing -> deliverables/user/WI-20260526-ATS-001-summary.md :
- Summary, decisions, risks, next WI trigger.
Agent-facing -> deliverables/agent/WI-20260526-ATS-001-evidence-pack.md :
- Evidence pointers, docs changed, validation commands, rollback.
Handoff Packet -> deliverables/agent/WI-20260526-ATS-001-handoff.md :
- This packet.

[TRACEABILITY REQUIREMENTS]
Evidence pointers: Required.
Tests: docs validation and diff check.
Rollback: Revert settlement design documentation changes.
