[WI HEADER]
WI ID: WI-20260526-ATS-005
REQ: REQ-20260526-ATS-001
Agent: qa/qa-integ/docops
Depends On: WI-20260526-ATS-003, WI-20260526-ATS-004
Blocks: -

[WI SUMMARY]
Why: Settlement import/reconciliation touches financial evidence and admin UI, so final verification must compare code, design, and docs.
Scope (in/out): In scope is backend/frontend/doc validation, API/DB/UI doc sync, final summaries/evidence, and commit readiness. Out of scope is new feature expansion.
DoD: All required tests and docs validation pass, final acceptance checklist is updated, and remaining follow-ups are documented.
Constraints/Forbidden: Do not add new scope during verification. Do not mark Toss Settlement API as implemented.

[ACCEPTANCE CRITERIA]
Functional:
- [ ] Settlement flows pass backend/frontend verification.
- [ ] Docs match implemented code.
- [ ] Remaining tax invoice/Toss API follow-ups remain separate.
Performance:
- [ ] N/A.
Quality:
- [ ] backend tests pass.
- [ ] frontend typecheck/lint/build pass.
- [ ] docs validation passes.
- [ ] `git diff --check` passes.

[INPUT POINTERS]
Tier 0 (Constitution - Required for all):
- docs/standards/core-principles.md
- docs/standards/development-standards.md
- docs/standards/documentation-standards.md
- docs/standards/glossary.md

Tier 1 (Policies):
- docs/policies/security-policy.md
- docs/policies/quality-gates.md

Tier 2 (Design/API/UI):
- docs/design/payment-refund-receipt-settlement-policy.md
- docs/design/payment-integration-design.md
- docs/design/api-spec.md
- docs/design/db-schema.md
- docs/SR/SR-93.md
- docs/ui/atstudio-front-list.md
- docs/ui/screen-flow.md

REQ/Context Docs:
- deliverables/user/REQ-20260526-ATS-001.md
- deliverables/agent/WI-20260526-ATS-001-evidence-pack.md
- deliverables/agent/WI-20260526-ATS-002-evidence-pack.md
- deliverables/agent/WI-20260526-ATS-003-evidence-pack.md
- deliverables/agent/WI-20260526-ATS-004-evidence-pack.md

[OUTPUT CONTRACT]
User-facing -> deliverables/user/WI-20260526-ATS-005-summary.md :
- Final verification summary and remaining work.
Agent-facing -> deliverables/agent/WI-20260526-ATS-005-evidence-pack.md :
- Validation commands, outputs, diff summary, rollback.
Handoff Packet -> deliverables/agent/WI-20260526-ATS-005-handoff.md :
- This packet.

[TRACEABILITY REQUIREMENTS]
Evidence pointers: Required.
Tests: backend tests, frontend typecheck/lint/build, docs validation, diff check.
Rollback: Revert REQ-20260526-ATS-001 implementation commit.
