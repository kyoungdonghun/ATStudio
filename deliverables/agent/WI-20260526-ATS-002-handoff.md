[WI HEADER]
WI ID: WI-20260526-ATS-002
REQ: REQ-20260526-ATS-001
Agent: se
Depends On: WI-20260526-ATS-001
Blocks: WI-20260526-ATS-004

[WI SUMMARY]
Why: Backend settlement ledger/import/reconciliation APIs are required before admin UI can replace manual spreadsheet checks.
Scope (in/out): In scope is settlement entity/enums/repository/DTO/service/controller, CSV import parsing, duplicate detection, matching against payment/refund records, admin list APIs, and backend tests. Out of scope is Toss Settlement API integration, tax invoice, cash receipt mutation, payout, and subscription entitlement mutation.
DoD: Admin can import settlement CSV, list settlement rows, update ignored status if needed, and see reconciliation status without mutating payment/subscription state.
Constraints/Forbidden: Do not store raw card data, Toss secret, billing key, authKey, customerKey, or raw provider payload. Do not call provider APIs. Do not delete imported settlement rows as the primary correction path.

[ACCEPTANCE CRITERIA]
Functional:
- [ ] Settlement ledger persists imported rows.
- [ ] Duplicate rows are skipped or rejected deterministically.
- [ ] Reconciliation links rows to local payment/refund records where possible.
- [ ] Mismatch statuses are queryable by admin.
Performance:
- [ ] Admin list endpoints are paginated.
Quality:
- [ ] Targeted backend tests pass.
- [ ] Existing payment operation tests still pass.

[INPUT POINTERS]
Tier 0 (Constitution - Required for all):
- docs/standards/core-principles.md
- docs/standards/development-standards.md

Tier 1 (Policies - Inferred):
- docs/policies/security-policy.md

Tier 2 (Design/API/DB):
- docs/design/payment-refund-receipt-settlement-policy.md
- docs/design/payment-integration-design.md
- docs/design/api-spec.md
- docs/design/db-schema.md

REQ/Context Docs:
- deliverables/user/REQ-20260526-ATS-001.md
- deliverables/agent/WI-20260526-ATS-001-evidence-pack.md

Files:
- src/main/java/com/atstudio/atstudio/controller/AdminPaymentController.java
- src/main/java/com/atstudio/atstudio/service/AdminPaymentReadService.java
- src/main/java/com/atstudio/atstudio/service/PaymentOperationAuditLogService.java
- src/main/java/com/atstudio/atstudio/entity/SubscriptionPayment.java
- src/main/java/com/atstudio/atstudio/entity/PaymentRefund.java
- src/main/java/com/atstudio/atstudio/repository/SubscriptionPaymentRepository.java
- src/main/java/com/atstudio/atstudio/repository/PaymentRefundRepository.java

[OUTPUT CONTRACT]
User-facing -> deliverables/user/WI-20260526-ATS-002-summary.md :
- Summary, API behavior, risks, tests.
Agent-facing -> deliverables/agent/WI-20260526-ATS-002-evidence-pack.md :
- Files changed, test commands, rollback.
Handoff Packet -> deliverables/agent/WI-20260526-ATS-002-handoff.md :
- This packet.

[TRACEABILITY REQUIREMENTS]
Evidence pointers: Required.
Tests: targeted settlement tests and payment operation regression tests.
Rollback: Revert settlement backend files and schema additions.
