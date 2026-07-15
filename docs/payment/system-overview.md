---
version: 1.3
last_updated: 2026-07-15
project: ATS
owner: docops
category: guide
status: stable
dependencies:
  - path: index.md
    reason: Payment documentation navigation
  - path: ../design/api-spec.md
    reason: Payment API source of truth
  - path: ../design/db-schema.md
    reason: Payment table source of truth
  - path: ../design/payment-integration-design.md
    reason: Detailed recurring payment design
  - path: ../audit/p1-payment-integrity-closure-20260715.md
    reason: Current payment-integrity code/test closure
---

# Payment System Overview

> Purpose: Explain the technical structure of the ATStudio payment system without requiring the reader to inspect the full source code first.

---

## 1. Operating Model

ATStudio payment is recurring-first for subscription scope.

The active user-facing model is:

1. The frontend prepares a billing agreement order.
2. Toss billing auth returns `authKey` and `customerKey` to the callback route.
3. The backend exchanges those values for a billing key.
4. The backend stores the billing key encrypted and records a billing agreement.
5. For a new subscription, the backend immediately charges the first period.
6. Future renewals and upgrades charge through the stored billing agreement.

The system does not let the frontend directly activate subscriptions after selecting a plan. Subscription access changes only through server-side confirmed payment or explicit admin entitlement correction.

Current payment-integrity rules:

- A logical payment command has a stable persisted command identity and bounded provider-attempt identities.
- Provider mutation runs outside local transactions; claim, result, and finalization use short committed phases.
- Provider success is durable before subscription/payment finalization, and retries from that state are finalize-only.
- Refund recovery retains one refund row and idempotency key, protected by a 15-minute processing lease and stale-result fencing.
- Reconciliation may mutate only after exact provider evidence; every mismatch remains Incident-only.

## 2. Main Layers

| Layer | Current Components | Responsibility |
| :-- | :-- | :-- |
| Frontend subscription UX | `/subscriptions`, `/subscriptions/checkout`, `/subscriptions/manage` | Plan selection, Toss billing auth start/callback, plan change preview, cancel/reactivate actions. |
| Frontend admin UX | `/admin/payments` | Payment order, agreement, payment, incident, receipt, audit, settlement, refund, and entitlement correction operations. |
| User payment APIs | `PaymentController`, `UserSubscriptionController` | Billing agreement prepare/confirm/read/cancel and subscription change/cancel/reactivate. |
| Admin payment APIs | `AdminPaymentController` | Read and mutate admin-only payment operation workflows. |
| Application services | `BillingAgreementApplicationService`, `UserSubscriptionService`, `RecurringRenewalService` | Billing-key registration, initial charge, upgrade charge, renewal, cancellation, and access transitions. |
| Withdrawal cleanup | `UserService`, `WithdrawalBillingCleanupCoordinator`, `WithdrawalBillingCleanupService` | Local-first withdrawal cancellation, ID-only after-commit dispatch, Provider key cleanup, Incident lifecycle, and daily retry. |
| Operations services | `PaymentReconciliationService`, `AdminPaymentRefundService`, `AdminPaymentEntitlementCorrectionService`, `AdminPaymentSettlementService` | Reconciliation, refund, correction, settlement import/review, and audit workflow. |
| Provider adapters | `RecurringPaymentProvider`, `PaymentStatusLookupProvider`, `PaymentRefundProvider`, `TossBillingProvider` | Provider-specific billing auth, charge, lookup, cancel, and refund/cancel calls. |
| Ledgers | Payment tables listed below | Persist source-of-truth local evidence and workflow state. |

## 3. Core Tables

| Table | Role |
| :-- | :-- |
| `payment_orders` | Payment command/order ledger with stable command identity, provider-attempt identity, purpose state, and finalize-only recovery evidence. |
| `billing_agreements` | Recurring agreement state, encrypted billing key, masked method, immutable next billing period, retry gate, and cleanup lease/state. |
| `subscription_payments` | Finalized subscription charge records. |
| `user_subscriptions` | Current user access state, plan, billing cycle, expiration, and pending change state. |
| `payment_reconciliation_incidents` | Persistent local/provider mismatch and withdrawal-cleanup Incident workflow. |
| `payment_receipts` | Safe receipt/cash-receipt evidence captured from successful provider responses. |
| `payment_operation_audit_logs` | Append-only admin/system operation audit log. |
| `payment_refunds` | Admin refund request, approval, provider execution, idempotency, processing lease, and fenced result ledger. |
| `payment_entitlement_corrections` | Refund-linked local access correction workflow. |
| `payment_settlements` | CSV/manual settlement evidence rows and generated reconciliation review rows. |

Runtime DB note:

- The backend defaults to `spring.jpa.hibernate.ddl-auto=validate`.
- `src/main/resources/schema.sql` is a full fresh-DB reference, not an automatic migration runner for existing MySQL databases.
- Existing local/staging/production databases must be patched before server startup when payment or whitelist tables/columns are missing. The ordered references include `20260615_align_payment_whitelist_schema.sql` and `20260714_payment_db_integrity.sql`; rehearse them on an approved copied database before any shared DB. The final fresh disposable MySQL run passed schema creation, Hibernate validation, and 7/7 races, but it does not prove a retained database.

## 4. User APIs

| API | Purpose |
| :-- | :-- |
| `POST /api/payments/billing-agreements/prepare` | Create a Toss billing auth order for new subscription or billing method re-registration. |
| `POST /api/payments/billing-agreements/confirm` | Confirm Toss billing auth, store billing key, and charge the first period for new subscription. |
| `GET /api/payments/billing-agreements/me` | Return current user's payment method status without exposing secrets. |
| `DELETE /api/payments/billing-agreements/me` | Cancel current billing agreement and subscription renewal. |
| `PUT /api/user-subscriptions/me` | Change plan or billing cycle. Upgrade may charge immediately. Downgrade/cycle-only change is scheduled. |
| `DELETE /api/user-subscriptions/me` | Cancel subscription renewal while keeping access until expiration. |
| `POST /api/user-subscriptions/me/reactivate` | Reactivate a cancelled grace-period subscription. |
| `DELETE /api/users/me` | Withdraw the account, cancel local renewal eligibility, and request Provider billing-key cleanup after commit. No refund is created. |

Legacy one-time subscription payment APIs exist in controller shape, but subscription `SUBSCRIBE` and `UPGRADE` direct confirmation is blocked for the current recurring-subscription scope.

## 5. Admin APIs

| API Group | Purpose |
| :-- | :-- |
| `GET /api/admin/payments/orders` | List local payment orders. |
| `GET /api/admin/payments/billing-agreements` | List billing agreement state and masked method metadata. |
| `GET /api/admin/payments/subscription-payments` | List finalized subscription charges. |
| `GET /api/admin/payments/reconciliation` | Run on-demand local/provider reconciliation summary. |
| `GET /api/admin/payments/reconciliation-incidents` | List persisted reconciliation incidents. |
| `PUT /api/admin/payments/reconciliation-incidents/{incidentId}/status` | Update incident workflow status. |
| `GET /api/admin/payments/receipts` | List safe receipt evidence. |
| `GET /api/admin/payments/operation-audit-logs` | List payment operation audit events. |
| `GET /api/admin/payments/refund-preview/{subscriptionPaymentId}` | Preview refundable amount. |
| `POST /api/admin/payments/refunds` | Create refund request ledger row. |
| `POST /api/admin/payments/refunds/{refundId}/approve` | Approve refund request. |
| `POST /api/admin/payments/refunds/{refundId}/execute` | Execute Toss cancel/refund request. |
| `POST /api/admin/payments/entitlement-correction-preview` | Preview explicit local access correction after refund. |
| `POST /api/admin/payments/entitlement-corrections` | Create correction request. |
| `POST /api/admin/payments/entitlement-corrections/{correctionId}/approve` | Approve correction request. |
| `POST /api/admin/payments/entitlement-corrections/{correctionId}/execute` | Execute local access correction. |
| `POST /api/admin/payments/settlements/import` | Import settlement CSV evidence. |
| `GET /api/admin/payments/settlements` | List settlement rows with filters. |
| `POST /api/admin/payments/settlements/reconcile` | Generate missing-provider settlement review rows. |
| `PUT /api/admin/payments/settlements/{settlementId}/ignore` | Mark settlement row ignored with note. |

## 6. Provider Boundary

The current provider is `TOSS_BILLING` for user-facing recurring subscription payment.

Provider-facing operations are isolated through interfaces:

- `RecurringPaymentProvider`: billing auth prepare/confirm, recurring charge, billing agreement cancel.
- `PaymentStatusLookupProvider`: provider payment lookup for reconciliation.
- `PaymentRefundProvider`: provider cancel/refund execution.

This keeps the current Toss implementation open to future provider adapters without making user subscription logic depend directly on provider-specific code.

Provider mutation/lookup methods covered by the current remediation use
`Propagation.NEVER` or an equivalent fail-fast boundary. An active caller
transaction is rejected instead of suspended. Local claim, result, and
finalization state is committed in separate short transactions.

## 7. Configuration Boundary

Payment configuration is under `app.payment`.

Important fields:

| Property Area | Purpose |
| :-- | :-- |
| `app.payment.provider` | Legacy one-time provider selector. Current subscription billing uses `TOSS_BILLING` provider flow. |
| `app.payment.toss.client-key` | Toss client key used by frontend billing auth metadata. |
| `app.payment.toss.secret-key` | Server-side Toss API secret key. Must never be exposed to frontend. |
| `app.payment.billing.encryption-secret` | Secret used to encrypt stored billing keys. |
| `app.payment.billing.auth-success-url` | Toss billing auth success callback URL. |
| `app.payment.billing.auth-fail-url` | Toss billing auth failure callback URL. |
| `app.payment.operations.reconciliation-notification-enabled` | Enables optional operator notification for reconciliation incidents. |
| `app.payment.operations.operator-email` | Operator email target when notification is enabled. |

## 8. Schedulers

| Schedule | Method | Purpose |
| :-- | :-- | :-- |
| 00:00 daily | `SubscriptionScheduler.processRecurringRenewals()` | Process due recurring charges. |
| 00:10 daily | `SubscriptionScheduler.processExpiredPaymentOrders()` | Expire stale `READY` and `IN_PROGRESS` payment orders. |
| 00:30 daily | `SubscriptionScheduler.processExpiredSubscriptions()` | Expire subscriptions after renewal/grace handling. |
| 01:00 daily | `PaymentReconciliationService.scheduledReconciliation()` | Compare local/provider ledgers and persist incidents when mismatches are found. |
| 01:15 daily | `WithdrawalBillingCleanupCoordinator.retryFailedCleanups()` | Retry only deleted-user `CANCELLED` agreements that still retain encrypted key material. |

Current deployment assumption is single server. Multi-server scheduler locking is intentionally deferred.

## 9. Security Rules

The following values must never be returned to frontend/admin screens or stored as raw public evidence:

- Toss secret key
- Raw billing key
- Raw `authKey`
- Raw `customerKey`
- Raw card number
- Raw provider payload containing sensitive fields
- Mail recipient, subject/body, verification/reset URL or token, raw delivery exception message, or stack trace

Allowed support-safe values include:

- Order ID
- Masked provider transaction reference for support. The exact provider transaction ID remains only in protected structured ownership fields and is excluded from serialized lookup evidence and Incident/audit free text.
- Masked payment method
- Receipt URL or receipt key when provider returns it
- Reconciliation status, incident metadata, and audit workflow fields

Withdrawal-specific boundaries:

- Local agreement/subscription cancellation completes before user soft deletion and does not depend on Provider cleanup success.
- Provider failure keeps encrypted key material for retry and creates/updates an agreement-scoped `WARNING` Incident.
- Provider success or `ALREADY_REMOVED_BILLING_KEY` clears issued-key material and resolves the matching Incident.
- Deleted users are excluded by both renewal selection and service guard before charge work.
- Account withdrawal never creates an automatic refund. Refund remains a separate approved admin workflow.

## Related Documents

### Required References

- [Payment Feature Inventory](feature-inventory.md): Complete capability list.
- [API Spec](../design/api-spec.md): Endpoint source of truth.
- [DB Schema](../design/db-schema.md): Table source of truth.
- [Payment Integration Design](../design/payment-integration-design.md): Detailed implementation design.

### Reference Documents

- [Admin Operations Guide](admin-operations-guide.md): Admin screen usage.
- [Payment Operations Runbook](../design/payment-operations-runbook.md): Operational response procedures.
- [P1 Payment Integrity Closure](../audit/p1-payment-integrity-closure-20260715.md): Current code/test evidence and remaining gates.
