# Evidence Pack: WI-20260517-ATS-006

## Summary (one-liner)
- Defined billing-key security controls for encryption, customerKey generation, masking, local safety, and cancellation.

## Scope / DoD Check
- DoD items:
  - [x] Defined encryption-at-rest requirements for `billing_key_ciphertext`.
  - [x] Defined HMAC-based non-reversible fingerprint rules.
  - [x] Confirmed `PAYMENT_BILLING_KEY_ENCRYPTION_SECRET` as the required env var.
  - [x] Defined masking and response rules.
  - [x] Defined frontend-safe billing agreement fields.
  - [x] Defined local/test safety rules.

## Reference Documents (Tier 0-2)

**Injected Context**:

| Tier | Document | Reason |
|------|----------|--------|
| 0 | docs/standards/core-principles.md | Security and transparency principles |
| 1 | docs/policies/security-policy.md | Secrets/PII handling |
| 1 | docs/policies/access-control-policy.md | Endpoint exposure and permission baseline |
| 1 | docs/policies/quality-gates.md | HIGH criticality quality expectations |
| 2 | deliverables/user/REQ-20260517-ATS-002.md | Approved recurring billing requirement |
| 2 | docs/design/payment-integration-design.md | Billing agreement design |
| 2 | docs/design/db-schema.md | Billing agreement table draft |
| 2 | docs/design/api-spec.md | Payment API baseline |

## Evidence Pointers
- Files reviewed:
  - `docs/policies/security-policy.md` - no plain text secrets, minimize logging, masking rules.
  - `application-local.example.yml` - local payment provider guidance exists and should add billing encryption guidance.
  - `src/main/resources/application.yml` - shared safe baseline should receive env placeholders only.
  - `src/main/java/com/atstudio/atstudio/config/PaymentProperties.java:12` - payment provider defaults to `MOCK`.
  - `src/main/java/com/atstudio/atstudio/service/payment/provider/TossPaymentProvider.java:98` - existing Toss one-time provider uses Basic auth with server-side secret key.
  - `src/main/java/com/atstudio/atstudio/service/payment/provider/TossPaymentProvider.java:161` - current `customerKey` is predictable and should not be reused for billing agreements.
  - `src/main/java/com/atstudio/atstudio/entity/PaymentOrder.java:86` - provider payload field must stay sanitized.
- Security controls:
  - Store billing key only as encrypted ciphertext, preferably AES-GCM with a random nonce per value.
  - Store ciphertext with a versioned envelope format, for example `v1:<nonce>:<ciphertext>`, to allow later rotation.
  - Derive separate encryption/fingerprint material from `PAYMENT_BILLING_KEY_ENCRYPTION_SECRET`.
  - Store `billing_key_fingerprint` using HMAC-SHA256 over the billing key, not raw SHA hashing.
  - Generate `provider_customer_key` as random UUID/ULID-like value; do not use user id, email, phone, or nickname.
  - Return only status, provider, masked method, `nextBillingAt`, `lastChargedAt`, and failure count to the frontend.
  - Never return or log `billingKey`, encryption secret, Toss secret key, or raw provider response.
  - In local/test profiles, reject `live_sk`/`live_ck` unless an explicit production/live flag is enabled.

## Commands & Outputs
- Commands executed:
  - `Get-Content` and `rg` against payment config, provider, and security policy files.
  - Official Toss documentation was reviewed through browser search/open.

## Tests
- Not applicable: security-design WI.
- Downstream verification required:
  - Unit tests for encrypt/decrypt and fingerprint determinism.
  - API tests proving no billing key appears in responses.
  - Diff/log secret scan before commit.

## Risks / Rollback
- Risks:
  - Key rotation is not fully solved by this WI; versioned ciphertext only keeps the path open.
  - If `provider_customer_key` is predictable, billing-key misuse risk rises.
  - Provider payload sanitization must be actively tested because Toss responses contain nested card and payment fields.
- Rollback:
  - Set provider back to `MOCK` or `TOSS` one-time only.
  - Disable recurring scheduler and block billing agreement endpoints.
  - If leakage is suspected, cancel/delete provider billing keys and rotate `PAYMENT_BILLING_KEY_ENCRYPTION_SECRET`.

## Follow-ups
- WI-20260517-ATS-008 must add encrypted storage and fingerprint columns.
- WI-20260517-ATS-009 must sanitize Toss billing responses before persisting provider payload.
- WI-20260517-ATS-013 final review must include secret exposure checks.
