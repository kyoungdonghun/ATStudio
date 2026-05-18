# Evidence Pack: WI-20260517-ATS-010

## Summary (one-liner)
- Added authenticated billing agreement APIs that prepare Toss Billing auth, confirm billing-key issue, perform the immediate first recurring charge, activate subscription only on charge success, expose current state, and cancel future renewals.

## Scope / DoD Check
- DoD items:
  - [x] Added `POST /api/payments/billing-agreements/prepare`.
  - [x] Added `POST /api/payments/billing-agreements/confirm`.
  - [x] Added `GET /api/payments/billing-agreements/me`.
  - [x] Added `DELETE /api/payments/billing-agreements/me`.
  - [x] Confirm validates authenticated order owner, order state, agreement status, customerKey, amount, expiry, subscription user type, and duplicate active subscription state.
  - [x] Confirm issues billing key, encrypts/fingerprints it, performs immediate billing-key charge, saves payment, and activates subscription only after charge success.
  - [x] Initial charge failure leaves no active subscription, marks the order failed, increments agreement failure count, and attempts provider billing-key delete cleanup.
  - [x] Cancel deletes provider billing key, cancels future renewal, and preserves paid access by marking current subscription `CANCELLED`.
  - [x] API/DTO responses do not expose raw billing keys.
  - [x] Focused service/controller tests and full backend tests pass.

## Reference Documents (Tier 0-2)

**Injected Context**:

| Tier | Document | Reason |
|------|----------|--------|
| 0 | docs/standards/core-principles.md | Constitution and payment traceability principles |
| 0 | docs/standards/development-standards.md | Java/Spring service/controller standards |
| 1 | docs/policies/security-policy.md | Billing key and secret handling |
| 1 | docs/policies/access-control-policy.md | Authenticated user ownership checks |
| 1 | docs/policies/quality-gates.md | HIGH criticality verification |
| 2 | deliverables/user/REQ-20260517-ATS-002.md | Approved recurring billing requirement |
| 2 | deliverables/agent/WI-20260517-ATS-005-evidence-pack.md | Architecture decisions |
| 2 | deliverables/agent/WI-20260517-ATS-006-evidence-pack.md | Security decisions |
| 2 | deliverables/agent/WI-20260517-ATS-007-evidence-pack.md | Toss Billing API research |
| 2 | deliverables/agent/WI-20260517-ATS-008-evidence-pack.md | Billing agreement storage |
| 2 | deliverables/agent/WI-20260517-ATS-009-evidence-pack.md | Recurring provider implementation |
| 2 | docs/design/payment-integration-design.md | Payment architecture baseline |
| 2 | docs/design/api-spec.md | API documentation baseline |

## Evidence Pointers
- Files changed:
  - `src/main/java/com/atstudio/atstudio/service/BillingAgreementApplicationService.java` - billing agreement prepare/confirm/current/cancel application flow.
  - `src/main/java/com/atstudio/atstudio/controller/PaymentController.java` - billing agreement endpoints under `/api/payments`.
  - `src/main/java/com/atstudio/atstudio/dto/payment/BillingAgreementPrepareRequest.java` - prepare request DTO.
  - `src/main/java/com/atstudio/atstudio/dto/payment/BillingAgreementPrepareResponse.java` - prepare response DTO.
  - `src/main/java/com/atstudio/atstudio/dto/payment/BillingAgreementCheckoutResponse.java` - client-safe Toss Billing checkout metadata.
  - `src/main/java/com/atstudio/atstudio/dto/payment/BillingAgreementConfirmRequest.java` - confirm request DTO.
  - `src/main/java/com/atstudio/atstudio/dto/payment/BillingAgreementConfirmResponse.java` - confirm response DTO.
  - `src/main/java/com/atstudio/atstudio/dto/payment/BillingAgreementResponse.java` - current/cancel response DTO.
  - `src/main/java/com/atstudio/atstudio/common/exception/BUSINESS_ERROR.java` - billing agreement business error codes.
  - `src/main/java/com/atstudio/atstudio/entity/BillingAgreement.java` - registration reset, issued-key staging/cleanup, and ownership helper.
  - `src/test/java/com/atstudio/atstudio/service/BillingAgreementApplicationServiceTest.java` - service success/failure/security tests.
  - `src/test/java/com/atstudio/atstudio/controller/PaymentControllerTest.java` - endpoint/auth/response-shape tests.
- Key locations:
  - `src/main/java/com/atstudio/atstudio/controller/PaymentController.java:72` - prepare endpoint.
  - `src/main/java/com/atstudio/atstudio/controller/PaymentController.java:85` - confirm endpoint.
  - `src/main/java/com/atstudio/atstudio/controller/PaymentController.java:97` - current agreement endpoint.
  - `src/main/java/com/atstudio/atstudio/controller/PaymentController.java:107` - cancel endpoint.
  - `src/main/java/com/atstudio/atstudio/service/BillingAgreementApplicationService.java:106` - prepare flow.
  - `src/main/java/com/atstudio/atstudio/service/BillingAgreementApplicationService.java:147` - confirm flow.
  - `src/main/java/com/atstudio/atstudio/service/BillingAgreementApplicationService.java:157` - confirm validation gate.
  - `src/main/java/com/atstudio/atstudio/service/BillingAgreementApplicationService.java:191` - initial charge failure cleanup.
  - `src/main/java/com/atstudio/atstudio/service/BillingAgreementApplicationService.java:209` - current state lookup.
  - `src/main/java/com/atstudio/atstudio/service/BillingAgreementApplicationService.java:217` - cancel flow.
  - `src/main/java/com/atstudio/atstudio/service/BillingAgreementApplicationService.java:245` - duplicate/retry agreement preparation guard.
  - `src/main/java/com/atstudio/atstudio/service/BillingAgreementApplicationService.java:303` - provider delete cleanup after failed initial charge.
  - `src/main/java/com/atstudio/atstudio/service/BillingAgreementApplicationService.java:336` - subscription payment record with billing agreement trace.
  - `src/main/java/com/atstudio/atstudio/entity/BillingAgreement.java:117` - registration reset.
  - `src/main/java/com/atstudio/atstudio/entity/BillingAgreement.java:134` - encrypted billing-key staging.
  - `src/main/java/com/atstudio/atstudio/entity/BillingAgreement.java:149` - issued-key cleanup.
  - `src/main/java/com/atstudio/atstudio/common/exception/BUSINESS_ERROR.java:143` - billing agreement errors.
  - `src/test/java/com/atstudio/atstudio/service/BillingAgreementApplicationServiceTest.java:103` - prepare success test.
  - `src/test/java/com/atstudio/atstudio/service/BillingAgreementApplicationServiceTest.java:147` - confirm success test.
  - `src/test/java/com/atstudio/atstudio/service/BillingAgreementApplicationServiceTest.java:202` - initial charge failure test.
  - `src/test/java/com/atstudio/atstudio/service/BillingAgreementApplicationServiceTest.java:246` - owner mismatch test.
  - `src/test/java/com/atstudio/atstudio/service/BillingAgreementApplicationServiceTest.java:272` - customerKey mismatch test.
  - `src/test/java/com/atstudio/atstudio/service/BillingAgreementApplicationServiceTest.java:322` - cancel success test.
  - `src/test/java/com/atstudio/atstudio/controller/PaymentControllerTest.java:47` - unauthenticated prepare test.
  - `src/test/java/com/atstudio/atstudio/controller/PaymentControllerTest.java:87` - no raw billing key in confirm response.

## Commands & Outputs
- Commands executed:
  - `./gradlew.bat test --tests "com.atstudio.atstudio.service.BillingAgreementApplicationServiceTest" --tests "com.atstudio.atstudio.controller.PaymentControllerTest"` -> pass.
  - `./gradlew.bat test --tests "com.atstudio.atstudio.service.BillingAgreementApplicationServiceTest" --tests "com.atstudio.atstudio.controller.PaymentControllerTest" --tests "com.atstudio.atstudio.service.PaymentApplicationServiceTest" --tests "com.atstudio.atstudio.service.payment.provider.recurring.TossBillingProviderTest" --tests "com.atstudio.atstudio.service.payment.provider.TossPaymentProviderTest" --tests "com.atstudio.atstudio.service.payment.billing.BillingKeyCryptoTest" --tests "com.atstudio.atstudio.entity.BillingAgreementTest" --tests "com.atstudio.atstudio.repository.BillingAgreementRepositoryTest"` -> pass.
  - `./gradlew.bat test` -> pass.

## Tests
- Billing agreement service tests: pass.
- Payment controller billing agreement endpoint tests: pass.
- Existing one-time payment service/provider tests: pass.
- Billing storage/crypto/provider tests: pass.
- Full backend suite: pass.

## Risks / Rollback
- Risks:
  - The billing agreement prepare step uses `PaymentOrder` as the server-side registration intent to avoid adding new DB columns in this WI.
  - External Toss charge success followed by local DB failure still has the same distributed-transaction limitation as one-time payment confirm.
  - Frontend routes for billing success/fail pages are not implemented in this WI.
  - Renewal scheduling and retry/grace policy are still pending in WI-20260517-ATS-011.
- Rollback:
  - Revert `BillingAgreementApplicationService`, billing agreement DTOs, `PaymentController` billing endpoints, billing agreement error codes, and the new tests.
  - Revert `BillingAgreement` helper methods if no longer needed by WI-011.
  - Existing WI-008 storage and WI-009 provider code can remain unused if only WI-010 is rolled back.

## Follow-ups
- WI-20260517-ATS-011: implement recurring renewal scheduler, retry count, 3-day grace policy, renewal payment/order records, and operational tests.
- WI-20260517-ATS-012: update API/design docs and frontend billing registration UX after scheduler behavior is fixed.
