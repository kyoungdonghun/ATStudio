# Evidence Pack: WI-20260517-ATS-011

## Summary (one-liner)
- Added recurring renewal processing with due agreement lookup, renewal order idempotency, immediate provider charge, success extension, 3-day grace, 3-retry failure handling, and scheduler ordering.

## Scope / DoD Check
- DoD items:
  - [x] Query due `ACTIVE` billing agreements by `nextBillingAt <= today`.
  - [x] Skip cancelled/non-Toss/missing-key/missing-active-subscription cases without charging.
  - [x] Create `RENEWAL` payment orders and reuse open renewal orders to avoid duplicate charges.
  - [x] Call `RecurringPaymentProvider.charge` with decrypted billing key and attempt-scoped idempotency key.
  - [x] On success, save `SubscriptionPayment`, extend `UserSubscription`, update `nextBillingAt`, and reset failure count.
  - [x] On failure, mark order failed, increment failure count, extend access to 3-day grace, and schedule retry inside grace.
  - [x] After 3 failures or grace expiry, suspend billing agreement and expire subscription only after grace access ends.
  - [x] Existing expiry scheduler now runs after the renewal job to reduce renewal/expiry race risk.
  - [x] Focused renewal/scheduler tests and full backend tests pass.

## Reference Documents (Tier 0-2)

**Injected Context**:

| Tier | Document | Reason |
|------|----------|--------|
| 0 | docs/standards/core-principles.md | Constitution and payment traceability principles |
| 0 | docs/standards/development-standards.md | Java/Spring service and scheduler standards |
| 1 | docs/policies/quality-gates.md | HIGH criticality verification |
| 1 | docs/policies/security-policy.md | Billing key handling |
| 2 | deliverables/user/REQ-20260517-ATS-002.md | Approved recurring billing requirement |
| 2 | deliverables/agent/WI-20260517-ATS-005-evidence-pack.md | Architecture decisions |
| 2 | deliverables/agent/WI-20260517-ATS-006-evidence-pack.md | Security decisions |
| 2 | deliverables/agent/WI-20260517-ATS-007-evidence-pack.md | Toss Billing API research |
| 2 | deliverables/agent/WI-20260517-ATS-008-evidence-pack.md | Billing agreement storage |
| 2 | deliverables/agent/WI-20260517-ATS-009-evidence-pack.md | Recurring provider implementation |
| 2 | deliverables/agent/WI-20260517-ATS-010-evidence-pack.md | Billing agreement API flow |
| 2 | docs/design/payment-integration-design.md | Payment architecture baseline |
| 2 | docs/design/db-schema.md | DB schema baseline |

## Evidence Pointers
- Files changed:
  - `src/main/java/com/atstudio/atstudio/service/RecurringRenewalService.java` - renewal processing, success/failure policy, grace/retry handling.
  - `src/main/java/com/atstudio/atstudio/service/SubscriptionScheduler.java` - scheduled renewal before expiration processing.
  - `src/main/java/com/atstudio/atstudio/repository/PaymentOrderRepository.java` - renewal order idempotency lookup.
  - `src/main/java/com/atstudio/atstudio/entity/BillingAgreement.java` - failed-charge retry date helper.
  - `src/test/java/com/atstudio/atstudio/service/RecurringRenewalServiceTest.java` - renewal success/failure/idempotency tests.
  - `src/test/java/com/atstudio/atstudio/service/SubscriptionSchedulerTest.java` - scheduler delegation and existing expiry behavior tests.
- Key locations:
  - `src/main/java/com/atstudio/atstudio/service/RecurringRenewalService.java:83` - due agreement query entrypoint.
  - `src/main/java/com/atstudio/atstudio/service/RecurringRenewalService.java:107` - per-agreement skip/charge policy.
  - `src/main/java/com/atstudio/atstudio/service/RecurringRenewalService.java:124` - renewal order find/create idempotency.
  - `src/main/java/com/atstudio/atstudio/service/RecurringRenewalService.java:143` - provider charge idempotency key.
  - `src/main/java/com/atstudio/atstudio/service/RecurringRenewalService.java:187` - successful renewal subscription/payment updates.
  - `src/main/java/com/atstudio/atstudio/service/RecurringRenewalService.java:215` - failed renewal retry/grace policy.
  - `src/main/java/com/atstudio/atstudio/service/RecurringRenewalService.java:235` - final failure/grace expiry policy.
  - `src/main/java/com/atstudio/atstudio/service/RecurringRenewalService.java:249` - missing active subscription suspension/expiry guard.
  - `src/main/java/com/atstudio/atstudio/service/RecurringRenewalService.java:317` - renewal run result record.
  - `src/main/java/com/atstudio/atstudio/service/SubscriptionScheduler.java:22` - recurring renewal schedule.
  - `src/main/java/com/atstudio/atstudio/service/SubscriptionScheduler.java:34` - expiry schedule after renewal window.
  - `src/main/java/com/atstudio/atstudio/repository/PaymentOrderRepository.java:18` - renewal order lookup.
  - `src/main/java/com/atstudio/atstudio/entity/BillingAgreement.java:169` - failed-charge retry date update.
  - `src/test/java/com/atstudio/atstudio/service/RecurringRenewalServiceTest.java:77` - success test.
  - `src/test/java/com/atstudio/atstudio/service/RecurringRenewalServiceTest.java:118` - duplicate done order test.
  - `src/test/java/com/atstudio/atstudio/service/RecurringRenewalServiceTest.java:142` - transient failure test.
  - `src/test/java/com/atstudio/atstudio/service/RecurringRenewalServiceTest.java:172` - third failure test.
  - `src/test/java/com/atstudio/atstudio/service/RecurringRenewalServiceTest.java:204` - cancelled agreement skip test.
  - `src/test/java/com/atstudio/atstudio/service/RecurringRenewalServiceTest.java:221` - due-date boundary test.
  - `src/test/java/com/atstudio/atstudio/service/RecurringRenewalServiceTest.java:235` - missing active after grace test.
  - `src/test/java/com/atstudio/atstudio/service/SubscriptionSchedulerTest.java:36` - scheduler delegation test.

## Commands & Outputs
- Commands executed:
  - `./gradlew.bat test --tests "com.atstudio.atstudio.service.RecurringRenewalServiceTest" --tests "com.atstudio.atstudio.service.SubscriptionSchedulerTest"` -> pass.
  - `./gradlew.bat test --tests "com.atstudio.atstudio.service.RecurringRenewalServiceTest" --tests "com.atstudio.atstudio.service.SubscriptionSchedulerTest" --tests "com.atstudio.atstudio.service.BillingAgreementApplicationServiceTest" --tests "com.atstudio.atstudio.controller.PaymentControllerTest" --tests "com.atstudio.atstudio.service.PaymentApplicationServiceTest" --tests "com.atstudio.atstudio.service.UserSubscriptionServiceTest" --tests "com.atstudio.atstudio.service.payment.provider.recurring.TossBillingProviderTest" --tests "com.atstudio.atstudio.service.payment.provider.TossPaymentProviderTest" --tests "com.atstudio.atstudio.entity.BillingAgreementTest" --tests "com.atstudio.atstudio.repository.BillingAgreementRepositoryTest"` -> pass.
  - `./gradlew.bat test` -> pass.

## Tests
- Recurring renewal service unit tests: pass.
- Subscription scheduler unit tests: pass.
- Billing agreement API/service tests: pass.
- Existing payment/subscription provider/service tests: pass.
- Full backend suite: pass.

## Risks / Rollback
- Risks:
  - Renewal idempotency uses the existing `PaymentOrder` table and renewal order status lookup; there is no dedicated renewal period column yet.
  - Provider charge success followed by local DB failure remains a distributed transaction risk shared with one-time payment flows.
  - Webhook reconciliation and refund automation are intentionally outside this WI.
  - Frontend billing UX is still pending.
- Rollback:
  - Revert `RecurringRenewalService`, `SubscriptionScheduler` renewal scheduling changes, `PaymentOrderRepository` renewal lookup, `BillingAgreement.recordFailedCharge(LocalDate)`, and the new renewal/scheduler tests.
  - Existing billing agreement API and provider layers can remain unused if the scheduler is rolled back.

## Follow-ups
- WI-20260517-ATS-012: update docs/API specs/schema docs and connect frontend billing registration UX.
- Add provider webhook reconciliation in a later REQ if production payment reconciliation becomes in scope.
