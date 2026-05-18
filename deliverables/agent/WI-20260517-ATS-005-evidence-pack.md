# Evidence Pack: WI-20260517-ATS-005

## Summary (one-liner)
- Defined the Phase C recurring billing architecture boundary for billing agreements, renewal orders, subscription mutation, and failure policy.

## Scope / DoD Check
- DoD items:
  - [x] Defined relationships among `BillingAgreement`, `PaymentOrder`, `SubscriptionPayment`, and `UserSubscription`.
  - [x] Defined initial recurring subscription flow: billing-key registration plus immediate first charge before activation.
  - [x] Defined renewal flow using `RENEWAL` payment orders.
  - [x] Defined automatic renewal cancellation behavior.
  - [x] Defined 3-day grace, 3-retry failure policy.
  - [x] Defined idempotency boundaries for agreement confirmation and renewal charge.

## Reference Documents (Tier 0-2)

**Injected Context**:

| Tier | Document | Reason |
|------|----------|--------|
| 0 | docs/standards/core-principles.md | Financial transaction traceability |
| 0 | docs/standards/development-standards.md | Java/Spring architecture standards |
| 1 | docs/policies/security-policy.md | Payment secret and sensitive data handling |
| 1 | docs/policies/quality-gates.md | HIGH criticality gate expectations |
| 2 | deliverables/user/REQ-20260517-ATS-002.md | Approved recurring billing requirement |
| 2 | docs/design/payment-integration-design.md | Existing payment architecture |
| 2 | docs/design/api-spec.md | API contract baseline |
| 2 | docs/design/db-schema.md | DB schema baseline |
| 2 | docs/design/usecase/user-subscription.md | Subscription use cases |
| 2 | docs/ui/screen-flow.md | Subscription UI flow |
| 2 | docs/ui/modal-list.md | Deferred checkout UX references |
| 2 | docs/SR/SR-92.md | Toss widget UX separation note |

## Evidence Pointers
- Files reviewed:
  - `src/main/java/com/atstudio/atstudio/service/PaymentApplicationService.java:92` - current one-time prepare flow accepts `SUBSCRIBE` and `UPGRADE`.
  - `src/main/java/com/atstudio/atstudio/service/PaymentApplicationService.java:149` - current confirm flow mutates subscription only after provider confirmation.
  - `src/main/java/com/atstudio/atstudio/service/PaymentApplicationService.java:206` - current subscription action switch has no `RENEWAL` branch yet.
  - `src/main/java/com/atstudio/atstudio/service/PaymentApplicationService.java:249` - finalized payment ledger creation is reusable for recurring charges.
  - `src/main/java/com/atstudio/atstudio/entity/PaymentOrder.java:41` - payment attempt lifecycle entity exists.
  - `src/main/java/com/atstudio/atstudio/entity/PaymentOrder.java:119` - `markDone` links the confirmed order to a `UserSubscription`.
  - `src/main/java/com/atstudio/atstudio/entity/UserSubscription.java:89` - existing row can be reused for a new subscription period.
  - `src/main/java/com/atstudio/atstudio/service/SubscriptionScheduler.java:28` - existing scheduler handles expiry/downgrade, but not recurring renewal.
  - `src/main/java/com/atstudio/atstudio/repository/UserSubscriptionRepository.java:29` - expired lookup uses `expiresAt < today`, so renewal on `expiresAt` date can run without same-day expiry collision.
- Architecture decisions:
  - Add `BillingAgreement` as a first-class entity with `READY`, `ACTIVE`, `SUSPENDED`, `CANCELLED`, `EXPIRED`.
  - Add `BillingAgreementApplicationService` for prepare/confirm/cancel agreement flows.
  - Add `RecurringBillingService` or `RecurringBillingScheduler` for renewal charges.
  - Add `RecurringPaymentProvider` separate from the existing one-time `PaymentProvider`.
  - Keep provider adapters mutation-free; application services own `UserSubscription` updates.

## Commands & Outputs
- Commands executed:
  - `Get-Content` and `rg` against payment/subscription code and design documents.
  - Official Toss documentation was reviewed through browser search/open.

## Tests
- Not applicable: design-only WI.
- Downstream verification required in implementation WIs:
  - Backend focused payment tests.
  - Scheduler idempotency tests.
  - Existing Mock/Toss one-time regression tests.

## Risks / Rollback
- Risks:
  - Renewal idempotency needs a DB-level guard, not only Java branching.
  - Existing `PaymentOrder` does not yet have `billing_agreement_id` or period fields, so recurring implementation should add them or use deterministic unique `orderId`.
  - If first charge fails after billing key issuance, agreement and subscription states must not imply paid access.
- Rollback:
  - Disable recurring billing by keeping `TOSS_BILLING` out of provider selection and not scheduling `RecurringBillingScheduler`.
  - Preserve existing `MOCK` and `TOSS` one-time payment paths unchanged.

## Follow-ups
- WI-20260517-ATS-008: `billing_agreements` entity/schema/repository.
- WI-20260517-ATS-009: `RecurringPaymentProvider` and Toss billing adapter.
- WI-20260517-ATS-010: recurring billing prepare/confirm/cancel API.
- WI-20260517-ATS-011: renewal scheduler and failure policy.
