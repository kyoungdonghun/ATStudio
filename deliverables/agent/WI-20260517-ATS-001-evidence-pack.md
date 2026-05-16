# Evidence Pack: WI-20260517-ATS-001

## Summary (one-liner)
- Added backend Toss one-time payment configuration, checkout metadata, and server-side confirm provider.

## Scope / DoD Check
- DoD items:
  - [x] `app.payment.provider` selects `MOCK` or `TOSS`, with `MOCK` as default.
  - [x] Toss prepare returns client-safe checkout metadata.
  - [x] Toss confirm sends `paymentKey`, `orderId`, and server-authoritative amount to Toss confirm URL.
  - [x] Toss failure returns provider failure and prevents subscription mutation through the existing confirm guard.
  - [x] Secret key remains server-side and environment-driven.

## Reference Documents (Tier 0-2)

**Injected Context**:

| Tier | Document | Reason |
|------|----------|--------|
| 0 | docs/standards/core-principles.md | Constitution and financial traceability |
| 0 | docs/standards/development-standards.md | Java/Spring Boot implementation standards |
| 1 | docs/policies/security-policy.md | Payment secret handling |
| 1 | docs/policies/quality-gates.md | Verification requirements |
| 2 | docs/design/payment-integration-design.md | Payment architecture and Phase B plan |
| 2 | docs/design/api-spec.md | Payment API contract |

## Evidence Pointers
- Files changed:
  - `src/main/java/com/atstudio/atstudio/config/PaymentProperties.java` - payment provider and Toss env-backed config.
  - `src/main/java/com/atstudio/atstudio/common/exception/BUSINESS_ERROR.java` - provider configuration error code.
  - `src/main/java/com/atstudio/atstudio/service/payment/provider/TossPaymentProvider.java` - Toss prepare/confirm provider.
  - `src/main/java/com/atstudio/atstudio/service/PaymentApplicationService.java` - provider selection and checkout response mapping.
  - `src/main/java/com/atstudio/atstudio/dto/payment/PaymentCheckoutResponse.java` - Toss checkout metadata fields.
  - `src/main/java/com/atstudio/atstudio/dto/payment/PaymentConfirmRequest.java` - Toss `paymentKey` field.
  - `src/main/resources/application.yml` - `APP_PAYMENT_PROVIDER`, `TOSS_*` configuration.
  - `src/test/java/com/atstudio/atstudio/service/PaymentApplicationServiceTest.java` - Toss prepare coverage.
  - `src/test/java/com/atstudio/atstudio/service/payment/provider/TossPaymentProviderTest.java` - local HTTP-server confirm coverage.
- Key locations:
  - `src/main/java/com/atstudio/atstudio/service/PaymentApplicationService.java:117` - configured provider selection.
  - `src/main/java/com/atstudio/atstudio/service/payment/provider/TossPaymentProvider.java:26` - Toss provider entry point.
  - `src/main/java/com/atstudio/atstudio/service/payment/provider/TossPaymentProvider.java:101` - Toss confirm request construction.

## Commands & Outputs
- `./gradlew.bat test --tests "com.atstudio.atstudio.service.PaymentApplicationServiceTest" --tests "com.atstudio.atstudio.service.payment.provider.TossPaymentProviderTest"` -> pass.
- `./gradlew.bat test` -> pass.

## Tests
- Backend full suite: pass.
- Focused backend payment suite: pass.

## Risks / Rollback
- Risks:
  - Real Toss API was not called with live credentials; local HTTP-server tests cover request/response mapping.
  - `PAYMENT_PROVIDER_NOT_CONFIGURED` is represented as a 4xx business error to match the project's current error-code contract.
  - Webhook/refund/transaction reconciliation remain Phase D hardening.
- Rollback:
  - Revert the files listed under Evidence Pointers and restore `PaymentApplicationService` provider selection to `MOCK`.

## Follow-ups
- Implement compensating cancel/refund handling after PG success but local mutation failure.
