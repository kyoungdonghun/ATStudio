# Evidence Pack: WI-20260516-ATS-001

## Summary (one-liner)
- Implemented backend Mock-first payment order, API, provider, and subscription confirm flow.

## Scope / DoD Check
- DoD items:
  - [x] Payment order entity/repository added.
  - [x] Prepare/confirm/cancel endpoints added.
  - [x] Mock provider validates deterministic confirm token.
  - [x] SUBSCRIBE and UPGRADE apply only through successful confirm in the new flow.
  - [x] Failed/cancelled orders do not mutate subscriptions.

## Reference Documents (Tier 0-2)

| Tier | Document | Reason |
|------|----------|--------|
| 0 | docs/standards/core-principles.md | Constitution |
| 0 | docs/standards/development-standards.md | Backend implementation standards |
| 1 | docs/policies/security-policy.md | Payment/security boundary |
| 1 | docs/policies/quality-gates.md | Verification |
| 2 | docs/design/payment-integration-design.md | Target design |
| 2 | docs/design/api-spec.md | API contract |
| 2 | docs/design/db-schema.md | DB contract |

## Evidence Pointers
- Files changed:
  - `src/main/java/com/atstudio/atstudio/entity/PaymentOrder.java`
  - `src/main/java/com/atstudio/atstudio/entity/enums/PaymentOrderStatus.java`
  - `src/main/java/com/atstudio/atstudio/entity/enums/PaymentProviderType.java`
  - `src/main/java/com/atstudio/atstudio/entity/enums/PaymentPurpose.java`
  - `src/main/java/com/atstudio/atstudio/repository/PaymentOrderRepository.java`
  - `src/main/java/com/atstudio/atstudio/service/PaymentApplicationService.java`
  - `src/main/java/com/atstudio/atstudio/controller/PaymentController.java`
  - `src/main/java/com/atstudio/atstudio/service/payment/provider/*`
  - `src/main/java/com/atstudio/atstudio/entity/SubscriptionPayment.java`
  - `src/main/java/com/atstudio/atstudio/common/exception/BUSINESS_ERROR.java`
  - `src/main/resources/schema.sql`

## Commands & Outputs
- `./gradlew.bat test --tests "com.atstudio.atstudio.service.PaymentApplicationServiceTest"` -> passed.
- `./gradlew.bat test --tests "com.atstudio.atstudio.service.PaymentApplicationServiceTest" --tests "com.atstudio.atstudio.service.UserSubscriptionServiceTest"` -> passed.
- `./gradlew.bat test` -> passed.

## Risks / Rollback
- Risks:
  - Legacy `POST /api/user-subscriptions` still supports direct upgrade if called manually.
  - Production DB requires applying the new `payment_orders` table and `subscription_payments` column changes.
- Rollback:
  - Revert the payment API/model files and schema additions together.
