# Evidence Pack: WI-20260517-ATS-008

## Summary (one-liner)
- Added billing agreement persistence, billing-key encryption/fingerprint support, random customerKey generation, schema/config hooks, and focused tests.

## Scope / DoD Check
- DoD items:
  - [x] Added `BillingAgreement` entity mapped to `billing_agreements`.
  - [x] Added `BillingAgreementStatus` enum.
  - [x] Added `BillingAgreementRepository` with lookup methods for confirm and renewal flows.
  - [x] Added encrypted billing key handling using `PAYMENT_BILLING_KEY_ENCRYPTION_SECRET`.
  - [x] Added random provider customer key generation.
  - [x] Added nullable `billingAgreement` links to `PaymentOrder` and `SubscriptionPayment`.
  - [x] Added tests for encryption/decryption, fingerprint determinism, random customerKey generation, entity behavior, repository lookup, and default values.
  - [x] Existing payment tests and full Gradle test suite pass.

## Reference Documents (Tier 0-2)

**Injected Context**:

| Tier | Document | Reason |
|------|----------|--------|
| 0 | docs/standards/core-principles.md | Financial traceability and security principles |
| 0 | docs/standards/development-standards.md | Java/Spring/JPA implementation standards |
| 1 | docs/policies/security-policy.md | Secret and sensitive data handling |
| 1 | docs/policies/quality-gates.md | HIGH criticality verification |
| 2 | deliverables/user/REQ-20260517-ATS-002.md | Approved recurring billing requirement |
| 2 | deliverables/agent/WI-20260517-ATS-005-evidence-pack.md | Architecture decisions |
| 2 | deliverables/agent/WI-20260517-ATS-006-evidence-pack.md | Security decisions |
| 2 | docs/design/payment-integration-design.md | Payment architecture baseline |
| 2 | docs/design/db-schema.md | DB schema baseline |

## Evidence Pointers
- Files changed:
  - `src/main/java/com/atstudio/atstudio/entity/BillingAgreement.java` - billing agreement domain state and transitions.
  - `src/main/java/com/atstudio/atstudio/entity/enums/BillingAgreementStatus.java` - agreement lifecycle enum.
  - `src/main/java/com/atstudio/atstudio/repository/BillingAgreementRepository.java` - user/provider/customerKey/due-date lookups.
  - `src/main/java/com/atstudio/atstudio/service/payment/billing/BillingKeyCrypto.java` - AES-GCM encryption, HMAC fingerprint, decrypt support.
  - `src/main/java/com/atstudio/atstudio/service/payment/billing/BillingCustomerKeyGenerator.java` - Toss-compatible random customer key generation.
  - `src/main/java/com/atstudio/atstudio/config/PaymentProperties.java` - billing encryption config binding.
  - `src/main/java/com/atstudio/atstudio/entity/PaymentOrder.java` - nullable billing agreement trace link.
  - `src/main/java/com/atstudio/atstudio/entity/SubscriptionPayment.java` - nullable billing agreement trace link.
  - `src/main/resources/application.yml` - `PAYMENT_BILLING_KEY_ENCRYPTION_SECRET` binding.
  - `application-local.example.yml` - local placeholder for billing encryption secret.
  - `src/main/resources/schema.sql` - `billing_agreements` table and FK links.
  - `src/test/java/com/atstudio/atstudio/service/payment/billing/BillingKeyCryptoTest.java` - crypto tests.
  - `src/test/java/com/atstudio/atstudio/service/payment/billing/BillingCustomerKeyGeneratorTest.java` - customerKey tests.
  - `src/test/java/com/atstudio/atstudio/entity/BillingAgreementTest.java` - entity transition tests.
  - `src/test/java/com/atstudio/atstudio/repository/BillingAgreementRepositoryTest.java` - repository tests.
  - `src/test/java/com/atstudio/atstudio/entity/EntityDefaultValueTest.java` - default value coverage.
- Key locations:
  - `src/main/java/com/atstudio/atstudio/entity/BillingAgreement.java:49` - new entity.
  - `src/main/java/com/atstudio/atstudio/entity/BillingAgreement.java:96` - activation requires ciphertext and fingerprint.
  - `src/main/java/com/atstudio/atstudio/entity/BillingAgreement.java:116` - successful renewal state update.
  - `src/main/java/com/atstudio/atstudio/entity/BillingAgreement.java:140` - due-date chargeability rule.
  - `src/main/java/com/atstudio/atstudio/service/payment/billing/BillingKeyCrypto.java:30` - encrypt and protect raw billing key.
  - `src/main/java/com/atstudio/atstudio/service/payment/billing/BillingKeyCrypto.java:53` - decrypt support for provider charge use.
  - `src/main/java/com/atstudio/atstudio/service/payment/billing/BillingKeyCrypto.java:77` - deterministic HMAC fingerprint.
  - `src/main/java/com/atstudio/atstudio/service/payment/billing/BillingKeyCrypto.java:105` - missing `PAYMENT_BILLING_KEY_ENCRYPTION_SECRET` blocks crypto use.
  - `src/main/java/com/atstudio/atstudio/service/payment/billing/BillingCustomerKeyGenerator.java:16` - Toss-compatible random customerKey.
  - `src/main/java/com/atstudio/atstudio/repository/BillingAgreementRepository.java:17` - user/provider lookup.
  - `src/main/java/com/atstudio/atstudio/repository/BillingAgreementRepository.java:20` - provider customerKey lookup.
  - `src/main/java/com/atstudio/atstudio/repository/BillingAgreementRepository.java:25` - due renewal lookup.
  - `src/main/resources/schema.sql:382` - `billing_agreements` manual schema.
  - `src/main/resources/schema.sql:418` - `payment_orders.billing_agreement_id`.
  - `src/main/resources/schema.sql:448` - `subscription_payments.billing_agreement_id`.

## Commands & Outputs
- Commands executed:
  - `./gradlew.bat test --tests "com.atstudio.atstudio.service.payment.billing.BillingKeyCryptoTest" --tests "com.atstudio.atstudio.service.payment.billing.BillingCustomerKeyGeneratorTest" --tests "com.atstudio.atstudio.entity.BillingAgreementTest" --tests "com.atstudio.atstudio.repository.BillingAgreementRepositoryTest" --tests "com.atstudio.atstudio.entity.EntityDefaultValueTest"` -> pass.
  - `./gradlew.bat test --tests "com.atstudio.atstudio.service.PaymentApplicationServiceTest" --tests "com.atstudio.atstudio.service.payment.provider.TossPaymentProviderTest"` -> pass.
  - `./gradlew.bat test` -> pass.

## Tests
- Focused billing storage and crypto tests: pass.
- Existing payment application/provider tests: pass.
- Full backend suite: pass.

## Risks / Rollback
- Risks:
  - Manual `schema.sql` is updated, but production-grade migration remains outside this WI.
  - `docs/design/db-schema.md` still needs semantic update in the planned docops WI.
  - Key rotation is not implemented; ciphertext version prefix keeps the rotation path open.
- Rollback:
  - Revert the files listed under Evidence Pointers.
  - Remove `billing_agreement_id` columns and `billing_agreements` table from manual schema if rolling back DB changes.
  - Keep `app.payment.provider=MOCK` or `TOSS`; no recurring billing endpoint has been enabled by this WI.

## Follow-ups
- WI-20260517-ATS-009: implement `RecurringPaymentProvider` and Toss billing adapter.
- WI-20260517-ATS-010: implement billing agreement APIs.
- WI-20260517-ATS-011: implement renewal scheduler and failure policy.
