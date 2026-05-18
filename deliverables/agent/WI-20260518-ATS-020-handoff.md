[WI HEADER]
WI ID: WI-20260518-ATS-020
REQ: REQ-20260518-ATS-001
Agent: qa-integ
Depends On: WI-20260518-ATS-018, WI-20260518-ATS-019
Blocks: WI-20260518-ATS-021

[WI SUMMARY]
Why: Check that UX, API, DB, security, and test-planning decisions agree before final design review.
Scope (in/out): In scope: cross-document consistency for checkout states, billing agreement states, operator read-only requirements, and deferred follow-up items. Out of scope: implementation.
DoD: No obvious mismatch remains between UI flow, payment design, API candidates, DB mapping, and security constraints.
Constraints/Forbidden: Do not claim candidate endpoints are implemented unless verified in code.

[ACCEPTANCE CRITERIA]
Functional:
- [ ] User-facing states map to backend payment/billing states.
- [ ] Operator-facing requirements map to existing entities or clearly marked future candidates.
- [ ] Deferred items are consistently named across docs.
Performance:
- [ ] No runtime performance requirement; this is integration design QA.
Quality:
- [ ] Documentation validation passes.

[INPUT POINTERS]
Tier 0 (Constitution - Required for all):
- docs/standards/core-principles.md
- docs/standards/development-standards.md

Tier 1 / Context:
- docs/policies/security-policy.md
- docs/policies/quality-gates.md

Tier 2 / Context:
- docs/design/payment-integration-design.md
- docs/design/api-spec.md
- docs/design/db-schema.md
- docs/ui/screen-flow.md
- docs/ui/modal-list.md
- deliverables/user/REQ-20260518-ATS-001.md

[OUTPUT CONTRACT]
User-facing -> deliverables/user/WI-20260518-ATS-020-summary.md :
- Cross-layer consistency result and remaining risks.
Agent-facing -> deliverables/agent/WI-20260518-ATS-020-evidence-pack.md :
- Cross-check matrix, validation results, and follow-up recommendations.
Handoff Packet -> deliverables/agent/WI-20260518-ATS-020-handoff.md :
- This packet.

[TRACEABILITY REQUIREMENTS]
Evidence pointers (files/lines/commands/logs): Required.
Tests: Run docs validation; list relevant future code tests.
Rollback (if needed): Revert docs tied to this WI.
