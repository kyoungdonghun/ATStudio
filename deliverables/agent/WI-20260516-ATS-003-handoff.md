[WI HEADER]
WI ID: WI-20260516-ATS-003
REQ: REQ-20260516-ATS-001
Agent: pg
Depends On: -
Blocks: WI-20260516-ATS-004

[WI SUMMARY]
Why: Confirm that the new payment boundary does not introduce unsafe trust in client data or real-payment leakage.
Scope (in): Review authentication, authorization, amount validation, idempotency, failure handling, and secret-handling boundaries for the mock-first payment flow.
Scope (out): Full OWASP audit, Toss production key review, billing key encryption implementation.
DoD: Security notes are captured and blockers are either fixed or explicitly tracked before integration.
Constraints/Forbidden: Do not add real PG secrets or production credentials.

[ACCEPTANCE CRITERIA]
Functional:
- [ ] Payment confirm requires authenticated user ownership of the order.
- [ ] Client amount is not authoritative.
- [ ] Failed/cancelled orders cannot be confirmed as success later without valid provider result.
- [ ] Legacy direct subscribe endpoint risk is documented.
Performance:
- [ ] No external security service dependency is added.
Quality:
- [ ] Security findings are actionable and tied to files or tests.

[INPUT POINTERS]
Tier 0 (Constitution - Required for all):
- docs/standards/core-principles.md

Tier 1 (Policies - Based on Assignee):
- docs/policies/security-policy.md

Tier 2 (Design/Context):
- docs/design/payment-integration-design.md
- docs/design/api-spec.md

REQ/Context Docs:
- deliverables/user/REQ-20260516-ATS-001.md

Files:
- src/main/java/com/atstudio/atstudio/controller/UserSubscriptionController.java
- src/main/java/com/atstudio/atstudio/service/UserSubscriptionService.java
- src/main/java/com/atstudio/atstudio/service/payment/PaymentService.java
- frontend/src/pages/subscriber/SubscriptionPaymentPage.tsx

Repro/Logs:
- Security review notes in evidence pack.

[OUTPUT CONTRACT]
User-facing -> deliverables/user/WI-20260516-ATS-003-summary.md:
- Summary, accepted risks, approval points.
Agent-facing -> deliverables/agent/WI-20260516-ATS-003-evidence-pack.md:
- Findings, evidence pointers, recommended fixes, residual risks.
Handoff Packet -> deliverables/agent/WI-20260516-ATS-003-handoff.md:
- This packet.

[TRACEABILITY REQUIREMENTS]
Evidence pointers (files/lines/commands/logs): Required.
Tests: Identify missing security regression tests when applicable.
Rollback: Not applicable unless code changes are made.
