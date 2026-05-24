[WI HEADER]
WI ID: WI-20260524-ATS-001
REQ: REQ-20260524-ATS-001
Agent: se/docops/pg/qa-integ
Depends On: -
Blocks: -

[WI SUMMARY]
Why: SR-93 P1 production readiness requires provider-backed reconciliation and an operations compensation runbook before Toss recurring billing is treated as production-ready.
Scope (in/out): Implement provider API reconciliation for Toss billing-key payment orders, update operations documentation, and verify sensitive-data boundaries. Do not implement refund automation, admin payment mutations, multi-server scheduler locks, legacy endpoint removal, multi-PG adapters, or DB schema changes without separate approval.
DoD: Provider reconciliation can compare local final payment orders with Toss provider state, provider-success/local-mismatch cases are visible to operators, SR-93 and payment docs are current, backend focused tests pass, docs validation is executed.
Constraints/Forbidden: No raw billing keys, auth keys, Toss secret keys, raw card data, or raw provider payload in logs/API responses/docs. No DB schema migration without explicit approval. No user-facing one-time subscription payment revival.

[ACCEPTANCE CRITERIA]
Functional:
- [ ] Provider reconciliation checks local `payment_orders` against Toss payment state by `paymentKey` or `orderId`.
- [ ] Local DONE-without-subscription-payment and provider DONE-without-local-finalization cases are reported separately.
- [ ] Admin/operator read-only surface or logs expose safe reconciliation evidence without raw sensitive payment data.
- [ ] Provider success plus local persistence failure has a documented runbook.
Performance:
- [ ] Scheduled reconciliation remains bounded to a limited recent order window.
- [ ] Provider lookup timeouts use existing payment timeout conventions.
Quality:
- [ ] Focused backend tests pass.
- [ ] Documentation validation runs.
- [ ] API/design/SR docs reflect the implemented behavior.

[INPUT POINTERS]
Tier 0 (Constitution - Required for all):
- docs/standards/core-principles.md

Tier 0 (Standards - Based on Assignee):
- docs/standards/development-standards.md
- docs/standards/documentation-standards.md
- docs/standards/glossary.md

Tier 1 (Policies - Inferred from REQ/WI content):
- docs/policies/security-policy.md

REQ/Context Docs:
- deliverables/user/REQ-20260524-ATS-001.md
- docs/SR/SR-93.md
- docs/design/payment-integration-design.md
- docs/design/api-spec.md
- docs/design/db-schema.md

Files:
- src/main/java/com/atstudio/atstudio/service/PaymentReconciliationService.java
- src/main/java/com/atstudio/atstudio/service/payment/provider/recurring/TossBillingProvider.java
- src/main/java/com/atstudio/atstudio/config/PaymentProperties.java
- src/main/java/com/atstudio/atstudio/controller/AdminPaymentController.java
- src/test/java/com/atstudio/atstudio/service/PaymentReconciliationServiceTest.java

[OUTPUT CONTRACT]
User-facing -> deliverables/user/WI-20260524-ATS-001-summary.md :
- Summary, risks, approval points
Agent-facing -> deliverables/agent/WI-20260524-ATS-001-evidence-pack.md :
- Evidence pointers, patch notes, repro & tests, follow-up WI
Handoff Packet -> deliverables/agent/WI-20260524-ATS-001-handoff.md :
- This packet

[TRACEABILITY REQUIREMENTS]
Evidence pointers (files/lines/commands/logs): Required
Tests: Include focused backend test command and docs validation command/results.
Rollback (if needed): Revert changed files from this WI and restore SR-93 P1 status.

