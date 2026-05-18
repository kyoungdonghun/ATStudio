[WI HEADER]
WI ID: WI-20260518-ATS-017
REQ: REQ-20260518-ATS-001
Agent: docops
Depends On: WI-20260518-ATS-014, WI-20260518-ATS-015
Blocks: WI-20260518-ATS-019

[WI SUMMARY]
Why: Reflect payment operations requirements and deferred follow-up scope into API/DB/payment design documentation.
Scope (in/out): In scope: operator read-only needs, payment/billing state definitions, sensitive-data display boundaries, deferred webhook/refund/receipt/settlement/multi-PG backlog. Out of scope: code implementation.
DoD: Design docs identify minimum operations visibility and clearly separate deferred finance/PG extensions.
Constraints/Forbidden: Do not add concrete endpoint guarantees unless they are marked as candidate/planned and not currently implemented.

[ACCEPTANCE CRITERIA]
Functional:
- [ ] `docs/design/payment-integration-design.md` includes Phase D UX/operations stabilization policy.
- [ ] `docs/design/api-spec.md` identifies candidate admin operations endpoints without claiming implementation.
- [ ] `docs/design/db-schema.md` maps existing payment/billing tables to operator needs.
Performance:
- [ ] No runtime performance requirement; this is documentation-only.
Quality:
- [ ] `validate-docs` passes.

[INPUT POINTERS]
Tier 0 (Constitution - Required for all):
- docs/standards/core-principles.md
- docs/standards/documentation-standards.md
- docs/standards/glossary.md

Tier 1 / Context:
- docs/policies/security-policy.md

Tier 2 / Context:
- docs/design/payment-integration-design.md
- docs/design/api-spec.md
- docs/design/db-schema.md
- deliverables/user/REQ-20260518-ATS-001.md
- deliverables/user/REQ-20260517-ATS-002.md

Files:
- src/main/java/com/atstudio/atstudio/entity/PaymentOrder.java
- src/main/java/com/atstudio/atstudio/entity/SubscriptionPayment.java
- src/main/java/com/atstudio/atstudio/entity/BillingAgreement.java

[OUTPUT CONTRACT]
User-facing -> deliverables/user/WI-20260518-ATS-017-summary.md :
- API/DB/operations design summary and deferred follow-up list.
Agent-facing -> deliverables/agent/WI-20260518-ATS-017-evidence-pack.md :
- Changed docs, security boundaries, and validation results.
Handoff Packet -> deliverables/agent/WI-20260518-ATS-017-handoff.md :
- This packet.

[TRACEABILITY REQUIREMENTS]
Evidence pointers (files/lines/commands/logs): Required.
Tests: Run `python .agents/skills/validate-docs/scripts/validate_docs.py`.
Rollback (if needed): Revert design docs tied to this WI.
