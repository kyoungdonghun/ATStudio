# Evidence Pack: WI-20260517-ATS-009

## Summary (one-liner)
- Added a provider-neutral recurring billing interface and a Toss Billing adapter for billing-key issue, recurring charge, and billing-key deletion.

## Scope / DoD Check
- DoD items:
  - [x] Added `RecurringPaymentProvider` with prepare, confirm, charge, and cancel/delete operations.
  - [x] Added command/result records for recurring billing provider calls.
  - [x] Implemented Toss billing-key issue via `POST /v1/billing/authorizations/issue`.
  - [x] Implemented Toss recurring charge via `POST /v1/billing/{billingKey}`.
  - [x] Implemented optional Toss billing-key delete via `DELETE /v1/billing/{billingKey}`.
  - [x] Kept Toss secret key in server-side Basic auth only.
  - [x] Added charge idempotency key header support.
  - [x] Set recurring billing read timeout default to 60000 ms.
  - [x] Sanitized provider payload metadata to avoid returning raw billing key.
  - [x] Added local HTTP-server tests for request body, headers, success, mismatch, and delete mapping.
  - [x] Full backend test suite passes.

## Reference Documents (Tier 0-2)

**Injected Context**:

| Tier | Document | Reason |
|------|----------|--------|
| 0 | docs/standards/core-principles.md | Constitution and financial traceability principles |
| 0 | docs/standards/development-standards.md | Java/Spring implementation standards |
| 1 | docs/policies/security-policy.md | Secret and billing-key handling |
| 1 | docs/policies/quality-gates.md | HIGH criticality verification |
| 2 | deliverables/user/REQ-20260517-ATS-002.md | Approved recurring billing requirement |
| 2 | deliverables/agent/WI-20260517-ATS-005-evidence-pack.md | Architecture decisions |
| 2 | deliverables/agent/WI-20260517-ATS-006-evidence-pack.md | Security decisions |
| 2 | deliverables/agent/WI-20260517-ATS-007-evidence-pack.md | Toss Billing API research |
| 2 | docs/design/payment-integration-design.md | Payment architecture baseline |

**External References**:

| Source | Reason |
|--------|--------|
| https://docs.tosspayments.com/guides/v2/billing/integration | Toss billing integration flow |
| https://docs.tosspayments.com/reference | Toss API reference |
| https://docs.tosspayments.com/en/api-guide | Toss API guide and timeout/idempotency guidance |

## Evidence Pointers
- Files changed:
  - `src/main/java/com/atstudio/atstudio/service/payment/provider/recurring/RecurringPaymentProvider.java` - provider-neutral recurring billing contract.
  - `src/main/java/com/atstudio/atstudio/service/payment/provider/recurring/BillingAgreementPrepareCommand.java` - prepare input.
  - `src/main/java/com/atstudio/atstudio/service/payment/provider/recurring/BillingAgreementPrepareResult.java` - client-safe prepare metadata.
  - `src/main/java/com/atstudio/atstudio/service/payment/provider/recurring/BillingAgreementConfirmCommand.java` - billing auth confirm input.
  - `src/main/java/com/atstudio/atstudio/service/payment/provider/recurring/BillingAgreementConfirmResult.java` - billing key issue result.
  - `src/main/java/com/atstudio/atstudio/service/payment/provider/recurring/BillingChargeCommand.java` - recurring charge input.
  - `src/main/java/com/atstudio/atstudio/service/payment/provider/recurring/BillingChargeResult.java` - recurring charge result.
  - `src/main/java/com/atstudio/atstudio/service/payment/provider/recurring/BillingAgreementCancelCommand.java` - delete input.
  - `src/main/java/com/atstudio/atstudio/service/payment/provider/recurring/BillingAgreementCancelResult.java` - delete result.
  - `src/main/java/com/atstudio/atstudio/service/payment/provider/recurring/TossBillingProvider.java` - Toss Billing HTTP adapter.
  - `src/main/java/com/atstudio/atstudio/config/PaymentProperties.java` - recurring billing URL and timeout config binding.
  - `src/main/resources/application.yml` - billing env bindings.
  - `application-local.example.yml` - local billing config example.
  - `src/test/java/com/atstudio/atstudio/service/payment/provider/recurring/TossBillingProviderTest.java` - local HTTP-server provider tests.
- Key locations:
  - `src/main/java/com/atstudio/atstudio/service/payment/provider/recurring/RecurringPaymentProvider.java:5` - interface declaration.
  - `src/main/java/com/atstudio/atstudio/service/payment/provider/recurring/RecurringPaymentProvider.java:9` - prepare agreement contract.
  - `src/main/java/com/atstudio/atstudio/service/payment/provider/recurring/RecurringPaymentProvider.java:11` - confirm agreement contract.
  - `src/main/java/com/atstudio/atstudio/service/payment/provider/recurring/RecurringPaymentProvider.java:13` - recurring charge contract.
  - `src/main/java/com/atstudio/atstudio/service/payment/provider/recurring/RecurringPaymentProvider.java:15` - cancel/delete contract.
  - `src/main/java/com/atstudio/atstudio/service/payment/provider/recurring/TossBillingProvider.java:41` - client-safe prepare metadata.
  - `src/main/java/com/atstudio/atstudio/service/payment/provider/recurring/TossBillingProvider.java:67` - billing-key issue request.
  - `src/main/java/com/atstudio/atstudio/service/payment/provider/recurring/TossBillingProvider.java:95` - recurring charge request.
  - `src/main/java/com/atstudio/atstudio/service/payment/provider/recurring/TossBillingProvider.java:121` - billing-key delete request.
  - `src/main/java/com/atstudio/atstudio/service/payment/provider/recurring/TossBillingProvider.java:183` - charge idempotency header.
  - `src/main/java/com/atstudio/atstudio/service/payment/provider/recurring/TossBillingProvider.java:198` - server-side Basic auth header.
  - `src/main/java/com/atstudio/atstudio/service/payment/provider/recurring/TossBillingProvider.java:260` - sanitized agreement payload excludes raw billing key.
  - `src/main/java/com/atstudio/atstudio/service/payment/provider/recurring/TossBillingProvider.java:268` - sanitized charge payload.
  - `src/main/java/com/atstudio/atstudio/service/payment/provider/recurring/TossBillingProvider.java:369` - billing read timeout source.
  - `src/test/java/com/atstudio/atstudio/service/payment/provider/recurring/TossBillingProviderTest.java:34` - prepare metadata test.
  - `src/test/java/com/atstudio/atstudio/service/payment/provider/recurring/TossBillingProviderTest.java:62` - confirm success and sanitization test.
  - `src/test/java/com/atstudio/atstudio/service/payment/provider/recurring/TossBillingProviderTest.java:103` - charge success and idempotency test.
  - `src/test/java/com/atstudio/atstudio/service/payment/provider/recurring/TossBillingProviderTest.java:152` - mismatch guard test.
  - `src/test/java/com/atstudio/atstudio/service/payment/provider/recurring/TossBillingProviderTest.java:184` - delete request test.

## API Mapping
- Toss billing auth prepare:
  - Provider returns client key, customer key, success URL, fail URL, and `method=CARD` metadata.
  - No Toss secret key or billing key is exposed to frontend prepare metadata.
- Toss billing-key issue:
  - Method: `POST`
  - Endpoint: `/v1/billing/authorizations/issue`
  - Inputs: `authKey`, `customerKey`
  - Output: raw billing key remains a transient result for WI-010 encrypted storage.
- Toss recurring charge:
  - Method: `POST`
  - Endpoint: `/v1/billing/{billingKey}`
  - Inputs: `customerKey`, `amount`, `orderId`, `orderName`, optional `customerEmail`, optional `customerName`
  - Header: optional `Idempotency-Key`
  - Guard: response `orderId` and `totalAmount` must match request.
- Toss billing-key delete:
  - Method: `DELETE`
  - Endpoint: `/v1/billing/{billingKey}`

## Commands & Outputs
- Commands executed:
  - `./gradlew.bat test --tests "com.atstudio.atstudio.service.payment.provider.recurring.TossBillingProviderTest"` -> pass.
  - `./gradlew.bat test --tests "com.atstudio.atstudio.service.payment.provider.recurring.TossBillingProviderTest" --tests "com.atstudio.atstudio.service.payment.billing.BillingKeyCryptoTest" --tests "com.atstudio.atstudio.service.payment.billing.BillingCustomerKeyGeneratorTest" --tests "com.atstudio.atstudio.entity.BillingAgreementTest" --tests "com.atstudio.atstudio.repository.BillingAgreementRepositoryTest" --tests "com.atstudio.atstudio.service.payment.provider.TossPaymentProviderTest"` -> pass.
  - `./gradlew.bat test` -> pass.
  - `python .agents\skills\validate-docs\scripts\validate_docs.py` -> pass.
  - `git diff --check` -> pass with CRLF warnings only.

## Tests
- Focused Toss Billing adapter tests: pass.
- Existing billing storage/crypto tests: pass.
- Existing one-time Toss payment provider tests: pass.
- Full backend suite: pass.

## Risks / Rollback
- Risks:
  - No controller/service flow calls this provider yet; WI-010 must encrypt and persist the returned billing key immediately.
  - No renewal scheduler calls charge yet; WI-011 must enforce grace/retry rules and idempotency keys.
  - Provider error payloads are intentionally summarized; deeper operational diagnostics may need structured safe logging later.
- Rollback:
  - Revert the recurring provider package and `TossBillingProviderTest`.
  - Revert billing URL/timeout additions in `PaymentProperties`, `application.yml`, and `application-local.example.yml`.
  - WI-008 storage can remain unused because WI-009 adds no schema mutation by itself.

## Follow-ups
- WI-20260517-ATS-010: connect billing agreement prepare/confirm/cancel APIs and encrypted storage.
- WI-20260517-ATS-011: connect renewal scheduler, retry policy, grace-period state handling, and renewal payment records.
