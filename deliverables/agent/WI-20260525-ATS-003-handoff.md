[WI HEADER]
WI ID: WI-20260525-ATS-003
REQ: REQ-20260525-ATS-003
Agent: se/docops
Depends On: WI-20260525-ATS-002
Blocks: Future refund, settlement, tax-invoice, and admin payment mutation WIs

[WI SUMMARY]
Why: ATStudio needs local evidence and audit foundations before implementing any payment refund or financial operation mutation.
Scope (in/out): Add payment receipt evidence persistence, payment operation audit log persistence, admin read-only APIs, safe provider receipt extraction, successful charge integration, reconciliation incident status audit, and docs/deliverables. Exclude refund/cash-receipt/settlement/tax-invoice execution and UI mutation workflows.
DoD: Successful charge paths can store safe receipt evidence without duplicates; admin incident status updates write audit logs; admin can list receipts/audit logs; tests and docs validation pass.
Constraints/Forbidden: Do not store billing keys, auth keys, customer keys, raw card data, or raw provider payload. Do not implement provider refund/cancel or cash receipt issue/cancel.

[ACCEPTANCE CRITERIA]
Functional:
- [ ] `payment_receipts` entity/repository/service exists and records safe receipt evidence.
- [ ] `payment_operation_audit_logs` entity/repository/service exists and records admin payment operation changes.
- [ ] Initial subscription, upgrade charge, and recurring renewal successful charge paths call receipt evidence storage.
- [ ] Toss sanitized charge payload includes safe receipt/cash receipt fields when present.
- [ ] Reconciliation incident status update records actor, before/after status, note, and target linkage.
- [ ] Admin read-only APIs list payment receipts and operation audit logs with pagination.
Quality:
- [ ] Backend focused tests cover receipt extraction and audit logging.
- [ ] API spec, DB schema, runbook, SR-93, and operation policy are synchronized.
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
- docs/design/payment-refund-receipt-settlement-policy.md
- docs/design/payment-operations-runbook.md
- docs/design/payment-integration-design.md
- docs/design/api-spec.md
- docs/design/db-schema.md
- docs/SR/SR-93.md
- docs/index.md
- docs/design/index.md

Code:
- src/main/java/com/atstudio/atstudio/service/BillingAgreementApplicationService.java
- src/main/java/com/atstudio/atstudio/service/UserSubscriptionService.java
- src/main/java/com/atstudio/atstudio/service/RecurringRenewalService.java
- src/main/java/com/atstudio/atstudio/service/AdminPaymentIncidentService.java
- src/main/java/com/atstudio/atstudio/controller/AdminPaymentController.java
- src/main/java/com/atstudio/atstudio/service/payment/provider/recurring/TossBillingProvider.java
- src/main/resources/schema.sql

[OUTPUT CONTRACT]
User-facing -> deliverables/user/WI-20260525-ATS-003-summary.md
Agent-facing -> deliverables/agent/WI-20260525-ATS-003-evidence-pack.md
Handoff Packet -> deliverables/agent/WI-20260525-ATS-003-handoff.md

[TRACEABILITY REQUIREMENTS]
Evidence pointers: required
Tests: focused backend tests, docs validation, diff check required
Rollback: revert WI-20260525-ATS-003 code, schema, docs, and deliverables
