# Evidence Pack: WI-20260517-ATS-007

## Summary (one-liner)
- Researched official Toss Payments billing-key API requirements for Phase C implementation.

## Scope / DoD Check
- DoD items:
  - [x] Identified billing-key issuance flow and fields.
  - [x] Identified automatic payment approval flow and fields.
  - [x] Confirmed `customerKey`, `authKey`, `billingKey`, `orderId`, `amount`, and `orderName` use.
  - [x] Confirmed test/live key behavior and local-test safety expectations.
  - [x] Summarized provider error and contract/setup risks.
  - [x] Separated implementation requirements from Phase D hardening.

## Reference Documents (Tier 0-2)

**Injected Context**:

| Tier | Document | Reason |
|------|----------|--------|
| 0 | docs/standards/core-principles.md | Official-source and traceability expectations |
| 2 | deliverables/user/REQ-20260517-ATS-002.md | Approved recurring billing requirement |
| 2 | docs/design/payment-integration-design.md | Existing recurring billing target |
| 2 | docs/design/api-spec.md | Payment API baseline |
| 2 | docs/design/db-schema.md | DB schema baseline |

## Evidence Pointers
- Official sources reviewed on 2026-05-17:
  - `https://docs.tosspayments.com/guides/v2/billing/integration` - billing window integration guide.
  - `https://docs.tosspayments.com/reference` - Core API reference.
  - `https://docs.tosspayments.com/en/api-guide` - Payment API auth, idempotency, and test/live key behavior.
  - `https://docs.tosspayments.com/guides/v2/get-started/llms-quick-reference` - official quick reference for responsibility split and recurring flow.
- Source-backed API findings:
  - Billing auth success redirect returns `customerKey` and one-time `authKey`; `authKey` max length is 300.
  - Issue billing key: `POST /v1/billing/authorizations/issue` with `authKey` and `customerKey`.
  - Billing object includes `billingKey` connected to `customerKey`; `billingKey` max length is 200.
  - Automatic payment approval: `POST /v1/billing/{billingKey}`.
  - Automatic payment approval requires `amount`, `customerKey`, `orderId`, and `orderName`.
  - Toss states scheduling is not provided; ATStudio must implement scheduling.
  - Cancellation can be implemented by not calling the approval API on the next billing date; provider-side delete is `DELETE /v1/billing/{billingKey}`.
  - If `billingKey` and `customerKey` do not match, Toss documents `NOT_MATCHES_CUSTOMER_KEY`.
  - Automatic payment approval can take up to 60 seconds, so provider timeout should be at least 60 seconds.
  - Payment APIs use Basic auth with secret key plus colon encoded in base64.
  - Test API secret keys starting with `test_sk` or `test_gsk` use test mode and do not affect live data/payment methods.
  - Toss idempotency key header can make POST requests return the same result for the same key for 15 days.
- Implementation notes:
  - Billing-key issuance and recurring charge must not use the current `/v1/payments/confirm` one-time payment flow.
  - `TossBillingProvider` should have separate URLs for issue, charge, and delete.
  - `TossBillingProvider` read timeout should be at least 60000 ms for recurring charge.
  - Use deterministic idempotency key per ATStudio renewal period, for example `renewal:{agreementId}:{periodStart}:{periodEnd}`.
  - Store Toss payment response identifiers such as `paymentKey`, `orderId`, `status`, `method`, `approvedAt`, and masked card fields only after sanitization.

## Commands & Outputs
- Commands executed:
  - Official documentation lookup through web search/open.
  - Local code context read with `Get-Content` and `rg`.

## Tests
- Not applicable: research-only WI.
- Downstream verification required:
  - Unit test for billing issue request construction.
  - Unit test for recurring charge request construction, including timeout and Basic auth.
  - Failure tests for Toss error object mapping.

## Risks / Rollback
- Risks:
  - Toss automatic payment API requires an additional contract for production.
  - Direct card-info billing-key issuance requires a separate qualification and should remain out of scope.
  - Docs can change; source URLs should be rechecked before production hardening.
- Rollback:
  - If Toss billing API assumptions change, block `TOSS_BILLING` provider activation and keep `MOCK`/`TOSS` one-time payment only.

## Follow-ups
- WI-20260517-ATS-009 should implement official endpoint mappings.
- WI-20260517-ATS-010 should expose agreement prepare/confirm/cancel using the billing auth flow.
- WI-20260517-ATS-011 should use server-owned scheduling and idempotency.
