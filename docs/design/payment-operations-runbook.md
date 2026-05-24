# Payment Operations Runbook

> Purpose: Define production-facing operational procedures for Toss billing-key recurring payment reconciliation and incident response.
> Scope: ATStudio subscription payments only. This document does not introduce refund automation, settlement, tax invoice, or admin mutation features.

## 1. Operating Model

ATStudio subscription payment is recurring-first.

- New subscription: Toss billing auth issues a billing key, then ATStudio immediately charges the first period.
- Upgrade: ATStudio charges the remaining-period difference through the active billing agreement, then applies the higher plan immediately.
- Downgrade or billing-cycle-only change: ATStudio schedules the change for the next renewal without immediate payment.
- Renewal: ATStudio scheduler calls Toss billing charge with the stored encrypted billing key and provider customer key.

Toss does not run ATStudio subscription scheduling. ATStudio owns renewal timing, retry, grace-period handling, and local subscription mutation.

## 2. Sources of Truth

| Source | Role |
|---|---|
| `payment_orders` | Internal payment attempt ledger and merchant `orderId` source |
| `subscription_payments` | Finalized subscription payment records |
| `billing_agreements` | Stored provider customer key, encrypted billing key, masked payment method, next billing date |
| `user_subscriptions` | User access state, current plan, pending plan/cycle |
| Toss payment lookup API | Provider-side payment status comparison by `orderId` |

Local subscription access must not be changed solely from webhook data. Provider-side data is used for reconciliation, support, and compensation decisions.

## 3. Reconciliation Entry Points

### Scheduled

`PaymentReconciliationService` runs daily at 01:00 server time.

It performs:

- Local ledger reconciliation.
- Provider-backed reconciliation for recent subscription payment orders when a lookup-capable provider is configured.

### Admin Read-only

`GET /api/admin/payments/reconciliation`

This endpoint runs the same read-only checks and returns support-safe counts and issue records. It must remain read-only.

Safe fields include:

- `orderId`
- provider
- purpose
- local status
- provider status
- local/provider amount
- provider transaction ID
- sanitized failure code/message

Forbidden fields:

- raw billing key
- Toss secret key
- raw card number, CVC, expiry
- raw `authKey`
- raw provider payload

## 4. Interpreting Reconciliation Issues

| Issue type | Meaning | First response |
|---|---|---|
| `PROVIDER_DONE_LOCAL_NOT_FINALIZED` | Toss shows payment `DONE`, but local order is not `DONE`. | Treat as potential provider success + local persistence failure. |
| `LOCAL_DONE_PROVIDER_NOT_DONE` | Local order is `DONE`, but provider status is not `DONE`. | Verify provider dashboard before granting or extending paid access. |
| `LOCAL_DONE_PROVIDER_NOT_FOUND` | Local order is `DONE`, but provider lookup by `orderId` failed as not found. | Check whether the order was legacy/mock/non-provider data or a provider mismatch. |
| `AMOUNT_MISMATCH` | Provider amount differs from local order amount. | Do not mutate subscription until amount source is verified. |
| `PROVIDER_LOOKUP_FAILED` | Provider lookup failed due to config, network, auth, or provider error. | Fix lookup failure and rerun reconciliation. |

## 5. Provider Success + Local Failure Compensation

Use this path when Toss has charged the customer but ATStudio did not finalize the subscription state.

### 5.1 Evidence Collection

Collect only support-safe evidence:

- `orderId`
- user ID and email
- payment purpose (`SUBSCRIBE`, `UPGRADE`, `RENEWAL`)
- local order status
- local subscription status and plan
- expected amount and billing cycle
- provider status, amount, and `paymentKey`
- relevant application log timestamps

Do not request or store raw card information, raw billing keys, `authKey`, or Toss secret keys.

### 5.2 Decision Path

1. Confirm provider status from `GET /api/admin/payments/reconciliation` and the Toss dashboard.
2. Confirm whether the user received the paid entitlement:
   - New subscription: active subscription exists for the paid plan and period.
   - Upgrade: plan changed immediately and expiration date remained unchanged.
   - Renewal: subscription period extended to the next cycle.
3. If the user did not receive the entitlement, choose one operational resolution:
   - Manual refund/cancel through Toss dashboard.
   - Manual entitlement correction only after separate operational approval and backup.
4. Record the incident in the support tracker with the safe evidence above.
5. Rerun reconciliation and confirm the issue is either resolved or intentionally tracked.

### 5.3 Refund/Cancellation Boundary

ATStudio does not yet provide an admin refund or force-cancel API.

Until that feature exists, refund or cancellation must be handled in Toss operations tooling and recorded externally. Do not add ad-hoc database edits without approval, backup, and a linked incident.

## 6. Production Configuration Checklist

Before enabling live Toss recurring billing:

- Configure Toss live `clientKey` and `secretKey` through environment variables or a secret manager.
- Confirm test keys are not present in production.
- Set billing auth success/fail URLs to the production frontend origin.
- Confirm production CORS allows only intended frontend origins.
- Set `PAYMENT_BILLING_KEY_ENCRYPTION_SECRET` through a secret manager or environment variable.
- Confirm `TOSS_PAYMENT_LOOKUP_BY_ORDER_ID_URL` or `app.payment.billing.payment-lookup-by-order-id-url` points to the Toss production API.
- Confirm application logs do not include raw billing keys, raw provider payloads, or raw card data.
- Confirm admin payment pages are restricted to `ROLE_ADMIN`.
- Run `GET /api/admin/payments/reconciliation` after a staging payment rehearsal.

## 7. Webhook Boundary

Webhook can be added later as an auxiliary event channel for supported Toss events.

Do not treat webhook payloads as the only source of truth for subscription activation or renewal. For ATStudio recurring billing, provider API response and provider lookup reconciliation remain the primary verification path.

If webhook is introduced later:

- Accept only HTTPS endpoint traffic in production.
- Return HTTP 200 only after the payload is safely parsed and recorded.
- Verify any provider-provided secret or signature mechanism available for the event type.
- Store only sanitized event metadata.
- Re-query provider state before mutating paid access.

## 8. Follow-up Scope

Separate REQ/SR items are still needed for:

- Refund automation.
- Receipt, settlement, and tax invoice operations.
- Admin payment mutation APIs.
- Multi-server scheduler lock.
- Legacy endpoint removal.
- KakaoPay, NaverPay, or other PG adapters.
