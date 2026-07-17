---
version: 3.0
last_updated: 2026-07-17
project: ATS
owner: SA
category: design
status: stable
dependencies:
  - path: api-spec.md
    reason: Current recurring and admin API contract
  - path: db-schema.md
    reason: Current payment persistence contract
  - path: payment-operations-runbook.md
    reason: Operator recovery procedures
  - path: payment-refund-receipt-settlement-policy.md
    reason: Refund, receipt, settlement, and correction policy
  - path: ../../src/main/java/com/atstudio/atstudio/service/payment/provider/
    reason: Current provider interface and Toss adapter source
---

# Payment Integration Design

## V1 Decision

ATStudio V1 uses **Toss card recurring payment only** for subscription purchase,
payment-method registration, upgrade, renewal, cancellation, refund, and
provider lookup.

- Persisted provider identity: `TOSS`.
- Active adapter: `TossBillingProvider`.
- Active user payment API: billing agreement prepare, confirm, read, and cancel.
- Active frontend routes: `/subscriptions/checkout`, success/fail callbacks,
  and `/subscriptions/manage`.
- Direct subscription creation and legacy payment prepare/confirm/cancel
  contracts are removed.
- No alternate or simulated provider is active or represented in the V1 enum
  or schema.

## Provider-Neutral Boundary

The application keeps three provider interfaces:

| Interface | Responsibility |
|---|---|
| `RecurringPaymentProvider` | Billing auth, billing-key issue, recurring charge, agreement cancellation |
| `PaymentStatusLookupProvider` | Provider payment lookup for reconciliation |
| `PaymentRefundProvider` | Provider cancel/refund execution |

`TossBillingProvider` implements all three. These interfaces preserve a
multi-PG extension boundary without claiming that another provider exists.
Adding a provider requires an approved cross-layer change covering enum,
schema, adapter selection, startup validation, reconciliation, refund,
security, tests, and documentation.

## User Flow

### New Subscription

1. The USER selects a plan and cycle.
2. The backend creates a billing-agreement order.
3. The frontend redirects to Toss billing auth.
4. The success callback sends the returned authorization values to the backend.
5. The backend issues and encrypts the billing key.
6. The backend performs the first recurring charge.
7. Subscription access activates only after durable provider success and local
   finalization.

### Payment-Method Registration

1. An existing subscriber starts billing auth with
   `purpose=BILLING_AGREEMENT` and `amount=0`.
2. Confirmation replaces the encrypted billing key and masked method.
3. The current plan, period, and access are unchanged.

### Upgrade and Scheduled Change

- An upgrade charges the prorated difference through the active billing
  agreement before changing access.
- Downgrade and cycle-only changes remain pending for the next successful
  renewal.
- There is no fallback checkout path.

### Renewal and Cancellation

- Renewal uses the stored billing agreement.
- User cancellation stops the next renewal while paid access remains through
  `expiresAt`.
- A valid cancelled grace-period subscription may be reactivated.
- Account withdrawal cancels local renewal eligibility before provider cleanup.
  Provider cleanup failure creates retryable Incident evidence and does not
  undo local withdrawal.

## Transaction and Recovery Invariants

- Every logical payment command has stable persisted identity.
- Provider mutation runs outside local database transactions.
- Claim, provider-result persistence, and finalization use separate committed
  phases.
- Provider success is durable before subscription/payment finalization.
- Retrying durable success is finalize-only and must not recharge.
- Refund execution reuses one refund row and idempotency key, with processing
  lease and stale-result fencing.
- Reconciliation mutates only from exact provider evidence; uncertainty remains
  Incident-only.
- Direct ADMIN subscription update/cancel remains an authorized emergency
  control and is not the ordinary payment path.

## Data Model

| Table | Responsibility |
|---|---|
| `billing_agreements` | Encrypted key, masked method, agreement state, renewal/cleanup state |
| `payment_orders` | Command, claim, provider attempt, and finalization evidence |
| `subscription_payments` | Finalized recurring charges |
| `payment_refunds` | Refund request, approval, provider execution, lease, and result |
| `payment_entitlement_corrections` | Explicit refund-linked local access correction |
| `payment_settlements` | Settlement import/generated review evidence |
| `payment_reconciliation_incidents` | Persistent payment and cleanup incidents |
| `payment_receipts` | Safe receipt evidence |
| `payment_operation_audit_logs` | Append-only admin/system operation audit |

All provider columns use `TOSS` in V1.

## Billing-Key Security

Billing keys use the V2 key-ID AES-GCM envelope only.

- `app.payment.billing.active-key-id`: write key selection.
- `app.payment.billing.encryption-keys`: active and retained V2 key ring.
- Raw billing keys, authorization values, customer identifiers, card numbers,
  and secret keys never enter frontend/admin DTOs or free-text logs.
- Missing, blank, duplicate, unknown, or placeholder key configuration fails
  closed.

## Configuration

Payment configuration is provider-neutral under `app.payment`:

- `toss.client-key`, `toss.secret-key`, endpoint and timeout settings.
- `billing.active-key-id`, `billing.encryption-keys`.
- `billing.auth-success-url`, `billing.auth-fail-url`.
- reconciliation limits and operator-notification settings.
- `scheduler-zone`, defaulting to `Asia/Seoul`.

The base configuration does not import ignored local configuration. Local
values are loaded explicitly by the operator. Acceptance derives callback URLs
from its declared public base URL and remains fail-closed.

## API Boundary

### USER

- `POST /api/payments/billing-agreements/prepare`
- `POST /api/payments/billing-agreements/confirm`
- `GET /api/payments/billing-agreements/me`
- `DELETE /api/payments/billing-agreements/me`
- `GET|PUT|DELETE /api/user-subscriptions/me`
- `POST /api/user-subscriptions/me/reactivate`

### ADMIN

`/api/admin/payments/**` owns payment ledger reads, reconciliation,
incidents, receipts, audit logs, refunds, entitlement corrections, and
settlements. `PUT|DELETE /api/user-subscriptions/{id}` remains the separate
emergency subscription control.

## Verification Boundary

- Fresh MySQL baseline plus Hibernate `ddl-auto=validate`.
- Full backend tests and payment race suites.
- Provider-identity negative search outside historical/archived records.
- Frontend checkout/manage tests.
- Secret scan that does not read ignored local values.

## Related Documents

- [API Specification](api-spec.md)
- [DB Schema](db-schema.md)
- [Payment Operations Runbook](payment-operations-runbook.md)
- [Payment Refund, Receipt, Settlement, and Tax Invoice Policy](payment-refund-receipt-settlement-policy.md)
