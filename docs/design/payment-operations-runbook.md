# Payment Operations Runbook

> Purpose: Define production-facing operational procedures for Toss billing-key recurring payment reconciliation and incident response.
> Scope: ATStudio subscription payments only. This document covers reconciliation, receipt evidence storage, payment operation audit visibility, the admin refund ledger/provider cancel workflow, and the separate refund-linked entitlement correction workflow. It does not introduce settlement import, tax invoice workflow, cash receipt issue/cancel automation, or automatic entitlement correction. Refund/receipt/settlement/tax invoice policy is defined separately in [Payment Refund, Receipt, Settlement, and Tax Invoice Policy](payment-refund-receipt-settlement-policy.md).

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
| `payment_refunds` | Admin refund request, approval, provider execution, idempotency, and provider cancel result ledger |
| `payment_entitlement_corrections` | Refund-linked admin entitlement correction request, before/target access snapshots, approval, execution, and result ledger |
| `billing_agreements` | Stored provider customer key, encrypted billing key, masked payment method, next billing date |
| `payment_reconciliation_incidents` | Persistent reconciliation mismatch incident state and operator workflow |
| `payment_receipts` | Safe provider receipt/cash receipt evidence captured after successful charges |
| `payment_operation_audit_logs` | Append-only payment operation audit rows for admin/system operations |
| `user_subscriptions` | User access state, current plan, pending plan/cycle |
| Toss payment lookup API | Provider-side payment status comparison by `orderId` |

Local subscription access must not be changed solely from webhook data. Provider-side data is used for reconciliation, support, and compensation decisions.

## 3. Reconciliation Entry Points

### Scheduled

`PaymentReconciliationService` runs daily at 01:00 server time.

It performs:

- Local ledger reconciliation.
- Provider-backed reconciliation for recent subscription payment orders when a lookup-capable provider is configured.
- If a mismatch is detected, the current implementation writes WARN-level server logs.
- The scheduled job creates or updates `payment_reconciliation_incidents` by deterministic `dedupeKey`.
- Repeated detection increments `occurrenceCount` and updates `lastDetectedAt`.
- A `RESOLVED` incident is reopened if the same mismatch appears again.
- An `IGNORED` incident remains ignored, but occurrence metadata is still updated.
- Optional operator email notification is sent only when `PAYMENT_RECONCILIATION_NOTIFICATION_ENABLED=true` and `PAYMENT_OPERATIONS_OPERATOR_EMAIL` is configured.

### Admin Read-only

`GET /api/admin/payments/reconciliation`

This endpoint runs the same read-only checks and returns support-safe counts and issue records. It must remain read-only.

Operators can use this endpoint to check the current state on demand. It does not create, acknowledge, or resolve incident records.

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

### Admin Incident Workflow

`GET /api/admin/payments/reconciliation-incidents`

Lists persisted reconciliation incidents. Optional `status` filtering supports `OPEN`, `ACKNOWLEDGED`, `RESOLVED`, and `IGNORED`.

`PUT /api/admin/payments/reconciliation-incidents/{incidentId}/status`

Updates only the reconciliation incident workflow state and note. This endpoint also writes a `payment_operation_audit_logs` row. It does not refund payments, cancel provider charges, change subscriptions, or mutate billing agreements.

Status guidance:

| Status | Meaning |
|---|---|
| `OPEN` | Newly detected or reopened issue that needs operator attention. |
| `ACKNOWLEDGED` | Operator has seen the issue and is investigating. |
| `RESOLVED` | Operator verified that the mismatch is fixed or no longer present. If detected again, the incident reopens automatically. |
| `IGNORED` | Operator intentionally suppresses this known/acceptable mismatch. Repeated detections update occurrence metadata but do not reopen it automatically. |

## 4. Interpreting Reconciliation Issues

| Issue type | Meaning | First response |
|---|---|---|
| `PROVIDER_DONE_LOCAL_NOT_FINALIZED` | Toss shows payment `DONE`, but local order is not `DONE`. | Treat as potential provider success + local persistence failure. |
| `LOCAL_DONE_PROVIDER_NOT_DONE` | Local order is `DONE`, but provider status is not `DONE`. | Verify provider dashboard before granting or extending paid access. |
| `LOCAL_DONE_PROVIDER_NOT_FOUND` | Local order is `DONE`, but provider lookup by `orderId` failed as not found. | Check whether the order was legacy/mock/non-provider data or a provider mismatch. |
| `AMOUNT_MISMATCH` | Provider amount differs from local order amount. | Do not mutate subscription until amount source is verified. |
| `PROVIDER_LOOKUP_FAILED` | Provider lookup failed due to config, network, auth, or provider error. | Fix lookup failure and rerun reconciliation. |

## 5. Current Automation and Visibility Boundary

Current automation is limited to detection, persistent incident visibility, optional email notification, explicit admin-approved refund execution, and explicit admin-approved local entitlement correction. It does not automatically perform entitlement correction, settlement mutation, tax invoice mutation, or cash receipt issue/cancel.

| Capability | Current state |
|---|---|
| Scheduled execution | Runs daily at 01:00 server time. |
| Provider comparison | Available when Toss lookup configuration is present. |
| Automatic log output | WARN-level logs are written for detected mismatches. |
| Admin read-only check | `GET /api/admin/payments/reconciliation` returns current mismatch counts and issue records. |
| Persistent incident storage | Implemented through `payment_reconciliation_incidents`. |
| Receipt evidence storage | Implemented through `payment_receipts` after successful subscription charges when provider receipt fields are present. |
| Refund ledger/provider cancel | Implemented through admin refund APIs backed by `payment_refunds`; provider execution requires an approved refund and reuses the persisted idempotency key. |
| Entitlement correction ledger | Implemented through admin entitlement correction APIs backed by `payment_entitlement_corrections`; execution requires approval and applies only explicit target access state. |
| Operation audit logs | Implemented through `payment_operation_audit_logs` for incident workflow changes, receipt evidence creation, refund workflow transitions, and entitlement correction workflow transitions. |
| Operator notification | Optional email notification when explicitly enabled and configured. |
| Admin incident workflow | Implemented through incident list/status APIs and the `/admin/payments` incident tab. |
| Auto entitlement correction | Not implemented; refund execution does not change subscription access. Entitlement correction must be created and executed separately. |

This means the system can detect, persist, and expose mismatches. Operator notification still depends on email configuration or external log monitoring; there is no Slack/SMS/in-app push channel yet.

## 6. Provider Success + Local Failure Compensation

Use this path when Toss has charged the customer but ATStudio did not finalize the subscription state.

### 6.1 Evidence Collection

Collect only support-safe evidence:

- `orderId`
- user ID and email
- payment purpose (`SUBSCRIBE`, `UPGRADE`, `RENEWAL`)
- local order status
- local subscription status and plan
- expected amount and billing cycle
- provider status, amount, and `paymentKey`
- receipt URL or cash receipt key if `GET /api/admin/payments/receipts` has a matching row
- audit log rows from `GET /api/admin/payments/operation-audit-logs` when an operator status change occurred
- relevant application log timestamps

Do not request or store raw card information, raw billing keys, `authKey`, or Toss secret keys.

### 6.2 Decision Path

1. Open the `/admin/payments` incident tab or `GET /api/admin/payments/reconciliation-incidents?status=OPEN`, then acknowledge the incident if investigation starts.
2. Confirm provider status from `GET /api/admin/payments/reconciliation` and the Toss dashboard.
3. Confirm whether the user received the paid entitlement:
   - New subscription: active subscription exists for the paid plan and period.
   - Upgrade: plan changed immediately and expiration date remained unchanged.
   - Renewal: subscription period extended to the next cycle.
4. If the user did not receive the entitlement, choose one operational resolution:
   - Admin refund request/approval/execution through `payment_refunds` if the original payment is refundable.
   - Admin entitlement correction through `payment_entitlement_corrections` after a refund succeeds and support approves the exact local access outcome.
5. Record external support-tracker details if a refund, entitlement correction, or customer contact is needed.
6. Rerun reconciliation and mark the incident `RESOLVED` or `IGNORED` with a short note.

### 6.3 Refund/Cancellation Boundary

ATStudio provides admin backend APIs for refund preview, request, approval, and provider execution. These APIs operate on the refund ledger and provider cancel API only; they do not automatically change local subscription entitlement.

Admin refund workflow:

1. Preview refundable amount with `GET /api/admin/payments/refund-preview/{subscriptionPaymentId}`.
2. Create a local request with `POST /api/admin/payments/refunds`.
3. Approve the request with `POST /api/admin/payments/refunds/{refundId}/approve`.
4. Execute provider cancel with `POST /api/admin/payments/refunds/{refundId}/execute`.
5. If the result is `PENDING_PROVIDER_CONFIRMATION`, verify the Toss dashboard/provider lookup before retrying or taking further action.
6. If entitlement must be changed after refund, handle it through the separate entitlement-correction procedure below. Do not edit subscription tables ad hoc without approval, backup, and a linked incident/support record.

Refund policy anchors:

- User-facing subscription cancellation is not refund.
- Provider refund and entitlement correction are separate audited operations.
- Refund provider requests persist an idempotency key before execution and must reuse it for the same refund request.
- Receipt, settlement, and tax invoice evidence require explicit ledgers rather than only sanitized provider payload.

### 6.4 Entitlement Correction Boundary

ATStudio provides admin backend APIs for refund-linked entitlement correction. These APIs operate only on local subscription access state and local billing agreement state; they do not move money and do not call provider billing-key delete/cancel APIs.

Admin entitlement correction workflow:

1. Confirm the related refund is `SUCCEEDED`.
2. Decide the exact target access state from the support ticket and operational evidence. Do not infer a previous plan rollback automatically from payment history.
3. Preview the target with `POST /api/admin/payments/entitlement-correction-preview`.
4. Create a local correction request with `POST /api/admin/payments/entitlement-corrections`.
5. Approve the request with `POST /api/admin/payments/entitlement-corrections/{correctionId}/approve`.
6. Execute local access correction with `POST /api/admin/payments/entitlement-corrections/{correctionId}/execute`.
7. Confirm `user_subscriptions` matches the target plan, billing cycle, status, expiration date, and pending-change clearing policy.
8. If `cancelBillingAgreement=true`, confirm only the local billing agreement was marked cancelled. Provider billing-key deletion remains a separate operation.

Execution safety notes:

- `EXPIRED` target status must not use a future expiration date.
- `ACTIVE` or `CANCELLED` target status must not use a past expiration date.
- The target plan must match the user's type and be active.
- If execution fails unexpectedly, the transaction rolls back and the correction remains retryable after investigation.

## 7. Production Configuration Checklist

Before enabling live Toss recurring billing:

- Configure Toss live `clientKey` and `secretKey` through environment variables or a secret manager.
- Confirm test keys are not present in production.
- Set billing auth success/fail URLs to the production frontend origin.
- Confirm production CORS allows only intended frontend origins.
- Set `PAYMENT_BILLING_KEY_ENCRYPTION_SECRET` through a secret manager or environment variable.
- Confirm `TOSS_PAYMENT_LOOKUP_BY_ORDER_ID_URL` or `app.payment.billing.payment-lookup-by-order-id-url` points to the Toss production API.
- Confirm `TOSS_CANCEL_URL` or `app.payment.toss.cancel-url` points to the Toss production cancel API.
- If email notification is desired, set `PAYMENT_RECONCILIATION_NOTIFICATION_ENABLED=true` and `PAYMENT_OPERATIONS_OPERATOR_EMAIL`.
- Confirm application logs do not include raw billing keys, raw provider payloads, or raw card data.
- Confirm admin payment pages are restricted to `ROLE_ADMIN`.
- Confirm WARN-level reconciliation logs are collected by the production log monitoring system.
- Run `GET /api/admin/payments/reconciliation` after a staging payment rehearsal.
- Run `GET /api/admin/payments/reconciliation-incidents?status=OPEN` after a staging payment rehearsal.
- Run `GET /api/admin/payments/receipts` after a successful staging charge and confirm only safe receipt evidence is returned.
- Run `GET /api/admin/payments/operation-audit-logs` after changing a reconciliation incident status and confirm an audit row exists.
- For refund rehearsal in a safe Toss test/staging environment, run preview → create → approve → execute and confirm `payment_refunds` plus audit logs update without exposing raw provider/card data.
- For entitlement correction rehearsal, use a safe succeeded refund row and run preview → create → approve → execute. Confirm `payment_entitlement_corrections`, `user_subscriptions`, optional local `billing_agreements`, and audit logs update without provider billing-key delete calls.
- Keep the deployment on one application scheduler instance. Scheduler lock remains out of active scope unless more than one application instance will run.

## 8. Webhook Boundary

Webhook can be added later as an auxiliary event channel for supported Toss events.

Do not treat webhook payloads as the only source of truth for subscription activation or renewal. For ATStudio recurring billing, provider API response and provider lookup reconciliation remain the primary verification path.

If webhook is introduced later:

- Accept only HTTPS endpoint traffic in production.
- Return HTTP 200 only after the payload is safely parsed and recorded.
- Verify any provider-provided secret or signature mechanism available for the event type.
- Store only sanitized event metadata.
- Re-query provider state before mutating paid access.

## 9. Follow-up Scope

Separate REQ/SR items are still needed for:

- Slack/SMS/in-app operator notification channels.
- First-class admin refund/entitlement correction UI tabs if operators need UI-driven operations instead of API calls.
- Settlement import/reconciliation and tax invoice request implementation based on the payment operations policy.
- Legacy endpoint removal.
- KakaoPay, NaverPay, or other PG adapters.

Cash receipt issue/cancel automation is intentionally on hold while ATStudio uses card-only recurring billing. Reopen it only if a cash-like payment method or a separate cash receipt request flow is approved.
