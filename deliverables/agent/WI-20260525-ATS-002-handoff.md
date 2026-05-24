[WI HEADER]
WI ID: WI-20260525-ATS-002
REQ: REQ-20260525-ATS-002
Agent: sa/docops
Depends On: WI-20260525-ATS-001
Blocks: -

[WI SUMMARY]
Why: Refund, receipt, settlement, and tax invoice work moves from payment checkout into money/evidence operations. ATStudio needs policy boundaries before any admin mutation or provider API implementation.
Scope (in/out): Create an operations policy design document, update SR-93/runbook/payment design/docs index, and produce summary/evidence. Exclude actual refund/cash-receipt/settlement/tax-invoice API implementation and any payment/subscription mutation.
DoD: Policy document exists, official references are linked, current implementation limitations are reflected, follow-up implementation candidates are explicit, docs validation passes.
Constraints/Forbidden: Do not expose or invent secret values. Do not implement Toss cancel/refund, cash receipt, settlement, tax invoice, admin payment mutation, or DB schema changes in this WI.

[ACCEPTANCE CRITERIA]
Functional:
- [ ] Refund policy covers full/partial refund, idempotency, entitlement handling, and incident linkage.
- [ ] Receipt policy covers Toss receipt URL, cash receipt issue/cancel boundaries, and visibility.
- [ ] Settlement policy covers Toss settlement lookup, internal reconciliation, and payout/settlement distinction.
- [ ] Tax invoice policy covers HomeTax/ASP/manual issuance boundary and required business evidence.
- [ ] SR-93 and payment operations runbook reflect policy-design completion but implementation deferral.
Quality:
- [ ] New design document is listed in docs/design/index.md.
- [ ] docs/index.md counts are synchronized.
- [ ] `python .agents/skills/validate-docs/scripts/validate_docs.py` passes.
- [ ] `git diff --check` passes.

[INPUT POINTERS]
Tier 0:
- docs/standards/core-principles.md
- docs/standards/development-standards.md
- docs/standards/documentation-standards.md
- docs/standards/glossary.md

Tier 1:
- docs/policies/security-policy.md
- docs/policies/quality-gates.md

Tier 2:
- docs/SR/SR-93.md
- docs/design/payment-operations-runbook.md
- docs/design/payment-integration-design.md
- docs/design/api-spec.md
- docs/design/db-schema.md
- docs/design/index.md
- docs/index.md

REQ/Context Docs:
- deliverables/user/REQ-20260525-ATS-002.md
- deliverables/user/PAYMENT-FINAL-ACCEPTANCE-CHECKLIST-20260525.md
- deliverables/agent/WI-20260525-ATS-001-evidence-pack.md

External Official References:
- Toss Payments cancel payment guide: https://docs.tosspayments.com/guides/v2/cancel-payment
- Toss Payments payment results and cash receipt guide: https://docs.tosspayments.com/guides/v2/learn/payment-results
- Toss Payments API reference: https://docs.tosspayments.com/reference
- Toss Payments settlement glossary: https://docs.tosspayments.com/resources/glossary/settlement
- Toss Payments API keys/security guide: https://docs.tosspayments.com/reference/using-api/api-keys
- National Tax Service e-tax invoice guide: https://www.nts.go.kr/nts/cm/cntnts/cntntsView.do?cntntsId=7788&mi=2462

Files:
- src/main/java/com/atstudio/atstudio/entity/PaymentOrder.java
- src/main/java/com/atstudio/atstudio/entity/SubscriptionPayment.java
- src/main/java/com/atstudio/atstudio/service/payment/provider/recurring/TossBillingProvider.java
- src/main/java/com/atstudio/atstudio/service/PaymentApplicationService.java

[OUTPUT CONTRACT]
User-facing -> deliverables/user/WI-20260525-ATS-002-summary.md
Agent-facing -> deliverables/agent/WI-20260525-ATS-002-evidence-pack.md
Handoff Packet -> deliverables/agent/WI-20260525-ATS-002-handoff.md

[TRACEABILITY REQUIREMENTS]
Evidence pointers: required
Tests: docs validation and diff check required
Rollback: revert policy document, index updates, SR/runbook/payment design updates, and deliverables from this WI
