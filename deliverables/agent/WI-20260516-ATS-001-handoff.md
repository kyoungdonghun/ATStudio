[WI HEADER]
WI ID: WI-20260516-ATS-001
REQ: REQ-20260516-ATS-001
Agent: se
Depends On: -
Blocks: WI-20260516-ATS-004

[WI SUMMARY]
Why: Establish the backend Mock-first payment contract so subscription creation and upgrade can happen only after payment confirmation.
Scope (in): Add payment order state/model, payment prepare/confirm/cancel API, Mock provider, and backend subscription application flow.
Scope (out): Toss API calls, billing key storage, webhooks, refund automation, admin payment audit UI.
DoD: Backend can prepare a mock payment order, confirm success idempotently, record failure/cancel states, and apply subscription changes only after successful confirm.
Constraints/Forbidden: Do not store real PG secrets. Do not remove the legacy `POST /api/user-subscriptions` endpoint in this WI. Do not implement Toss live integration.

[ACCEPTANCE CRITERIA]
Functional:
- [ ] `POST /api/payments/subscriptions/prepare` creates a READY payment order.
- [ ] `POST /api/payments/confirm` validates user, orderId, purpose, subscriptionId, billingCycle, amount, and provider token.
- [ ] Successful SUBSCRIBE confirmation creates `user_subscriptions`, `subscription_payments`, and the default playlist.
- [ ] Successful UPGRADE confirmation applies the upgrade and records payment.
- [ ] Failed/cancelled mock orders do not mutate subscriptions.
- [ ] Confirm is idempotent for an already DONE order.
Performance:
- [ ] No external network call is required in MOCK mode.
Quality:
- [ ] Backend compiles.
- [ ] Relevant backend tests pass.

[INPUT POINTERS]
Tier 0 (Constitution - Required for all):
- docs/standards/core-principles.md

Tier 0 (Standards - Based on Assignee):
- docs/standards/development-standards.md

Tier 1 (Policies - Inferred):
- docs/policies/security-policy.md
- docs/policies/quality-gates.md

Tier 2 (Design/Context):
- docs/design/payment-integration-design.md
- docs/design/api-spec.md
- docs/design/db-schema.md
- docs/design/usecase/user-subscription.md

REQ/Context Docs:
- deliverables/user/REQ-20260516-ATS-001.md

Files:
- src/main/java/com/atstudio/atstudio/service/UserSubscriptionService.java
- src/main/java/com/atstudio/atstudio/controller/UserSubscriptionController.java
- src/main/java/com/atstudio/atstudio/service/payment/PaymentService.java
- src/main/java/com/atstudio/atstudio/service/payment/MockPaymentServiceImpl.java
- src/main/java/com/atstudio/atstudio/entity/SubscriptionPayment.java
- src/main/java/com/atstudio/atstudio/repository/SubscriptionPaymentRepository.java
- src/test/java/com/atstudio/atstudio/service/UserSubscriptionServiceTest.java
- src/test/java/com/atstudio/atstudio/controller/UserSubscriptionControllerTest.java

Repro/Logs:
- `./gradlew.bat test --tests "com.atstudio.atstudio.service.UserSubscriptionServiceTest"`

[OUTPUT CONTRACT]
User-facing -> deliverables/user/WI-20260516-ATS-001-summary.md:
- Summary, changed behavior, risks, approval points.
Agent-facing -> deliverables/agent/WI-20260516-ATS-001-evidence-pack.md:
- Evidence pointers, patch notes, repro and test results, rollback notes.
Handoff Packet -> deliverables/agent/WI-20260516-ATS-001-handoff.md:
- This packet.

[TRACEABILITY REQUIREMENTS]
Evidence pointers (files/lines/commands/logs): Required.
Tests: Include command and result for backend unit/controller tests.
Rollback: Document how to revert payment API/model additions.
