---
version: 1.3
last_updated: 2026-08-09
project: ATS
owner: docops
category: guide
status: stable
dependencies:
  - path: payment-integration-design.md
    reason: Payment state and provider design
  - path: ../payment/system-overview.md
    reason: Current implementation overview
---

# Payment Operations Runbook

> Purpose: Define production-facing operational procedures for Toss billing-key recurring payment reconciliation and incident response.
> Scope: ATStudio subscription payments only. This document covers reconciliation, withdrawal billing-key cleanup, receipt evidence storage, payment operation audit visibility, the admin refund ledger/provider cancel workflow, the separate refund-linked entitlement correction workflow, and settlement import/reconciliation operations. It does not introduce tax invoice workflow, cash receipt issue/cancel automation, automatic entitlement correction, or automatic withdrawal refund. Refund/receipt/settlement/tax invoice policy is defined separately in [Payment Refund, Receipt, Settlement, and Tax Invoice Policy](payment-refund-receipt-settlement-policy.md).
> Last updated: 2026-07-17

## 1. Operating Model

ATStudio subscription payment is recurring-first.

- New subscription: Toss billing auth issues a billing key, then ATStudio immediately charges the first period.
- Upgrade: ATStudio charges the remaining-period difference through the active billing agreement, then applies the higher plan immediately.
- Downgrade or billing-cycle-only change: ATStudio schedules the change for the next renewal without immediate payment.
- Renewal: ATStudio scheduler calls Toss billing charge with the stored encrypted billing key and provider customer key.
- Account withdrawal: ATStudio locks the billing agreement before the subscription, rejects the request while a charge order is awaiting its Provider outcome or local finalization, cancels local renewal eligibility before soft deletion, then attempts Provider billing-key cleanup after the local transaction commits.

Toss does not run ATStudio subscription scheduling. ATStudio owns renewal timing, retry, grace-period handling, and local subscription mutation.

## 2. Sources of Truth

| Source | Role |
|---|---|
| `payment_orders` | Internal payment attempt ledger and merchant `orderId` source |
| `subscription_payments` | Finalized subscription payment records |
| `payment_refunds` | Admin refund request, approval, provider execution, idempotency, and provider cancel result ledger |
| `payment_entitlement_corrections` | Refund-linked admin entitlement correction request, before/target access snapshots, approval, execution, and result ledger |
| `billing_agreements` | Stored provider customer key, encrypted billing key, masked payment method, next billing date, and withdrawal-cleanup retry eligibility |
| `payment_reconciliation_incidents` | Persistent reconciliation mismatch and withdrawal-cleanup Incident state and operator workflow |
| `payment_receipts` | Safe provider receipt/cash receipt evidence captured after successful charges |
| `payment_operation_audit_logs` | Append-only payment operation audit rows for admin/system operations |
| `payment_settlements` | Imported/generated settlement evidence and reconciliation review rows |
| `user_subscriptions` | User access state, current plan, pending plan/cycle |
| Toss payment lookup API | Provider-side payment status comparison by `orderId` |

Local subscription access must not be changed solely from webhook data. Provider-side data is used for reconciliation, support, and compensation decisions.

## 3. Reconciliation Entry Points

### Scheduled

`PaymentReconciliationService` runs daily at 01:00 in the configured payment scheduler zone. The default zone is `Asia/Seoul`. Scheduled `LocalDate` and `LocalDateTime` decisions use the same injected business-zone clock, including renewal due dates and subscription expiration.

It performs:

- Local ledger reconciliation.
- Provider-backed reconciliation for eligible subscription payment orders when a lookup-capable provider is configured.
- Nonterminal/finalization candidates and `ACTIVE` agreement candidates are read with `id > lastSeenID` keyset batches.
- Locally `DONE` provider orders are rechecked only inside `completed-order-lookback-days` and `completed-order-max-per-run`; defaults are 30 days and 500 rows, with runtime caps of 365 days and 5000 rows.
- A locally succeeded refund excludes its original `DONE` order from this payment-state comparison because provider cancellation can legitimately change the original payment status.
- Scheduled local mismatch Incidents are persisted batch by batch. The API response keeps only the configured issue-detail limit and reports `totalIssues` plus `issueDetailsTruncated`; detail truncation never suppresses scheduled Incident persistence.
- If a mismatch is detected, the current implementation writes WARN-level server logs.
- The scheduled job creates or updates `payment_reconciliation_incidents` by deterministic `dedupeKey`.
- Repeated detection increments `occurrenceCount` and updates `lastDetectedAt`.
- A `RESOLVED` incident is reopened if the same mismatch appears again.
- An `IGNORED` incident remains ignored, but occurrence metadata is still updated.
- Optional operator email notification is sent only when `PAYMENT_RECONCILIATION_NOTIFICATION_ENABLED=true` and `PAYMENT_OPERATIONS_OPERATOR_EMAIL` is configured.

### Admin Read-only

`GET /api/admin/payments/reconciliation`

This endpoint runs a dedicated observation-only path and returns support-safe counts and issue records. It uses non-claiming reads and Provider lookups but never calls payment finalizers, mutates agreements/subscriptions/orders, or creates, updates, resolves, or reopens Incidents.

Operators can use this endpoint to check the current state on demand. Recovery and Incident persistence remain exclusive to the scheduled reconciliation path.

Safe fields include:

- `orderId`
- provider
- purpose
- local status
- provider status
- local amount and local currency
- provider amount and provider currency
- masked provider support reference (`REF-*`)
- sanitized failure code/message

Forbidden fields:

- raw billing key
- Toss secret key
- raw card number, CVC, expiry
- raw `authKey`
- raw provider payment, refund, receipt, or settlement identifier
- full or partial raw Provider identifiers copied into Incident/audit free text
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
| `LOCAL_DONE_PROVIDER_NOT_DONE` | Local payment state is complete while Provider payment state is not complete, or a withdrawn user's local agreement is `CANCELLED` while Provider billing-key deletion is not complete. | For payment mismatches, verify the Provider dashboard before changing access. For withdrawal cleanup, keep local cancellation in place and allow the targeted cleanup retry; do not restore renewal eligibility. |
| `LOCAL_DONE_PROVIDER_NOT_FOUND` | Local order is `DONE`, but provider lookup by `orderId` failed as not found. | Check provider evidence and local order identity; keep uncertainty Incident-only. |
| `AMOUNT_MISMATCH` | Provider amount differs from local order amount. | Do not mutate subscription until amount source is verified. |
| `PROVIDER_LOOKUP_FAILED` | Provider lookup failed due to config, network, auth, or provider error. | Fix lookup failure and rerun reconciliation. |

## 5. Current Automation and Visibility Boundary

Current automation is limited to detection, persistent incident visibility, optional email notification, explicit admin-approved refund execution, explicit admin-approved local entitlement correction, and admin-triggered settlement evidence import/review. It does not automatically perform entitlement correction, tax invoice mutation, cash receipt issue/cancel, or provider settlement API import.

Application logging is intentionally less detailed than the ADMIN API and Incident ledger. Local/provider reconciliation logs contain bounded aggregate counters only; they do not serialize issue lists, exact provider transaction identifiers, or free-text issue details. Structured Incidents persist deterministic `REF-*` support references, never raw identifier fragments. New audit/Incident free text omits Provider identifiers, and ADMIN serialization sanitizes labelled raw identifiers in retained legacy notes. An unknown Toss cancel transport failure logs only the exception class, never the exception message, stack trace, request URI, or provider payment key.

| Capability | Current state |
|---|---|
| Scheduled execution | Runs daily at 01:00 in `app.payment.scheduler-zone`, default `Asia/Seoul`. |
| Provider comparison | Available when Toss lookup configuration is present. |
| Automatic log output | WARN-level logs are written for detected mismatches. |
| Admin read-only check | `GET /api/admin/payments/reconciliation` returns current mismatch counts and issue records without claims, finalization, entitlement mutation, or Incident writes. |
| Persistent incident storage | Implemented through `payment_reconciliation_incidents`. |
| Receipt evidence storage | Implemented through `payment_receipts` after successful subscription charges when provider receipt fields are present. |
| Refund ledger/provider cancel | Implemented through admin refund APIs backed by `payment_refunds`; provider execution requires an approved refund and reuses the persisted idempotency key. |
| Entitlement correction ledger | Implemented through admin entitlement correction APIs backed by `payment_entitlement_corrections`; execution requires approval and applies only explicit target access state. |
| Settlement import/reconciliation | Implemented through admin settlement APIs backed by `payment_settlements`; current source adapter is CSV/manual import and generated missing-provider review rows. |
| Operation audit logs | Implemented through `payment_operation_audit_logs` for incident workflow changes, receipt evidence creation, refund workflow transitions, entitlement correction workflow transitions, and settlement import/reconcile/ignore transitions. |
| Operator notification | Optional email notification when explicitly enabled and configured. |
| Admin incident workflow | Implemented through incident list/status APIs and the `/admin/payments` incident tab. |
| Auto entitlement correction | Not implemented; refund execution does not change subscription access. Entitlement correction must be created and executed separately. |
| Withdrawal cleanup | Implemented through an ID-only `AFTER_COMMIT` event and agreement-specific `REQUIRES_NEW` Provider cleanup. |
| Withdrawal cleanup retry | Runs daily at 01:15 on one scheduler owner and selects only deleted users with `CANCELLED` agreements and retained encrypted keys. |
| Withdrawal refund | Not implemented by design; account withdrawal never creates or executes a refund automatically. |

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
- expected amount, local currency, and billing cycle
- provider status, provider amount, provider currency, and masked support reference (`REF-*`)
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
- Receipt and settlement evidence require explicit ledgers rather than only sanitized provider payload. Tax invoice tracking should add its own ledger only if future B2B invoice, bank-transfer, postpaid, or contract purchase scope is approved.

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

- Create and execute acquire the billing-agreement lock before the subscription lock.
- The stored agreement and subscription before-state snapshots must still match at execution; drift rejects execution without applying the target.
- A `SUBSCRIBE`, `UPGRADE`, or `RENEWAL` order in `PROCESSING`, `PROVIDER_SUCCEEDED`, or `PENDING_PROVIDER_CONFIRMATION` rejects correction creation or execution until reconciliation/finalization makes its outcome terminal.
- `EXPIRED` target status must not use a future expiration date.
- `ACTIVE` or `CANCELLED` target status must not use a past expiration date.
- The target plan must match the user's type and be active.
- If execution fails unexpectedly, the transaction rolls back and the correction remains retryable after investigation.

### 6.5 General Local Subscription Correction Boundary

The user-subscription administration screen also provides a general local
correction workflow under `/api/admin/user-subscription-corrections/**`. It is
not tied to a refund row and does not belong to the payment provider ledger.

1. Preview a complete target plan, billing cycle, status, expiration date,
   pending-change policy, and optional local billing-agreement cancellation.
2. Create the `REQUESTED` correction with a required support/operation reason.
3. Explicitly approve it. The current V1 workflow permits the same ADMIN to
   request, approve, and execute; it does not claim two-person approval.
4. Execute once. The service locks BillingAgreement, UserSubscription, target
   Subscription, and correction in the documented order, then revalidates the
   persisted snapshots and non-terminal payment-order boundary.
5. Verify the correction row, local subscription, optional local billing
   agreement, and generic administrator audit. Do not infer provider activity
   from the UI message.

The correction ledger supplies workflow idempotency and resumability. Ordered
locking and snapshot revalidation protect concurrent state. Success audit joins
the local mutation transaction; rejected execution uses independent audit
persistence. These are distinct controls and must not be described as provider
idempotency. No Toss charge, refund, billing-key delete, receipt, settlement,
or email operation is called by this workflow.

### 6.6 Settlement Import/Reconciliation Boundary

ATStudio provides admin APIs and UI for settlement evidence import and settlement review. These APIs operate only on `payment_settlements` and payment operation audit logs.

Admin settlement workflow:

1. Export provider settlement evidence to CSV using the documented settlement template headers.
2. Import the CSV from the `/admin/payments` settlement tab or `POST /api/admin/payments/settlements/import`.
3. Review the import summary: total rows, imported rows, duplicates, failed rows, and status counts.
4. Investigate `MISMATCHED`, `LOCAL_PAYMENT_NOT_FOUND`, and `PROVIDER_SETTLEMENT_NOT_FOUND` rows against local payment/refund ledgers and provider dashboard evidence.
5. Use `POST /api/admin/payments/settlements/reconcile` for a selected period to generate local-payment-without-imported-provider-evidence review rows.
6. Use `PUT /api/admin/payments/settlements/{settlementId}/ignore` only when the row is verified as acceptable or intentionally out of active review.

Settlement safety notes:

- Settlement import accepts CSV. Excel files should be exported to CSV before import.
- Settlement rows must not mutate user subscription access, billing agreements, payment order status, finalized payment status, refund status, or provider state.
- Unknown CSV columns are ignored. Stored `source_payload` is allowlisted and must not contain raw provider payload, card data, billing keys, auth keys, customer keys, or Toss secret keys.
- Generated `PROVIDER_SETTLEMENT_NOT_FOUND` rows are review candidates. They are not proof that Toss failed to settle money until provider evidence is checked.

## 7. Withdrawal Billing-Key Cleanup

### 7.1 Normal Path

1. `DELETE /api/users/me` authenticates the submitted password before billing locks or mutation.
2. The local transaction locks the Toss billing agreement, subscription, and user in that order, then revalidates the password against the locked user.
3. If a `SUBSCRIBE`, `UPGRADE`, or `RENEWAL` order is `PROCESSING`, `PROVIDER_SUCCEEDED`, or `PENDING_PROVIDER_CONFIRMATION`, withdrawal returns `PAYMENT_ORDER_INVALID_STATE` and makes no cancellation/deletion change. Retry only after reconciliation or finalization reaches a terminal outcome.
4. After the fence passes, the transaction marks a non-terminal agreement and an ACTIVE subscription `CANCELLED`, publishes an event containing only `billingAgreementID` when encrypted key material exists, removes transient user-owned rows, and soft-deletes the user.
5. Only after commit, `WithdrawalBillingCleanupCoordinator` starts agreement-specific cleanup through `WithdrawalBillingCleanupService` in `REQUIRES_NEW`.
6. Provider success clears the encrypted key and related issued-key metadata, including `next_billing_at`, and resolves the matching Incident if one exists.

Local renewal blocking does not wait for Provider cleanup. The due-renewal repository query excludes deleted users. Renewal claim and an immediate pre-Provider authorization both require an ACTIVE agreement, non-deleted user, ACTIVE subscription, and `PROCESSING` renewal order. Provider-success recording preserves Provider evidence but refuses local finalization after cancellation/deletion, and the finalizer repeats the fence so it cannot reactivate cancelled access.

### 7.2 Failure and Retry Path

On Provider configuration failure, decryption exception, empty response, or ordinary Provider failure:

- Keep the local agreement `CANCELLED` and the user deleted.
- Retain encrypted key material so cleanup remains retryable.
- Create or update one agreement-scoped `WARNING` Incident with issue type `LOCAL_DONE_PROVIDER_NOT_DONE`, `localStatus=CANCELLED`, and `providerStatus=BILLING_KEY_DELETE_FAILED`.
- Do not log the decrypted key, raw Provider payload, or raw exception message.
- Let the daily 01:15 job retry only repository-selected deleted/CANCELLED/key-retaining agreements. One agreement failure does not stop the remaining candidates.

If the Provider returns `ALREADY_REMOVED_BILLING_KEY`, treat the objective as complete: clear local issued-key material and resolve the matching Incident. This is the idempotent convergence path for Provider success followed by an earlier local persistence failure.

### 7.3 Operator Procedure

1. Filter `/admin/payments` Incidents or `GET /api/admin/payments/reconciliation-incidents` for `LOCAL_DONE_PROVIDER_NOT_DONE` and the affected billing agreement.
2. Confirm the user is deleted, the agreement is `CANCELLED`, and no renewal order/charge was created after withdrawal.
3. Review only support-safe failure code/message and timestamps. Never copy billing-key ciphertext into notes or tickets.
4. Allow the scheduled retry to run after Provider configuration/network recovery. There is no public/admin manual cleanup endpoint in the current implementation.
5. After cleanup succeeds, confirm issued-key fields are cleared and the matching Incident is `RESOLVED`.
6. Do not create a refund merely because the account was withdrawn. Use the separate refund request/approval/execution workflow only when support and policy approve a refund for a specific finalized payment.

## 8. Production Configuration Checklist

Before enabling live Toss recurring billing:

- Configure Toss live `clientKey` and `secretKey` through environment variables or a secret manager.
- Confirm test keys are not present in production.
- Set billing auth success/fail URLs to the production frontend origin.
- Confirm production CORS allows only intended frontend origins.
- Set `PAYMENT_BILLING_KEY_ACTIVE_KEY_ID` to the key ID used for new v2 ciphertext.
- Configure one or more `app.payment.billing.encryption-keys` entries through the secret manager. With Spring environment binding, list entries use names such as `APP_PAYMENT_BILLING_ENCRYPTIONKEYS_0_ID` and `APP_PAYMENT_BILLING_ENCRYPTIONKEYS_0_SECRET`. Never place the secret values in repository files or command output.
- Confirm the active key ID exists in the key ring and every retained V2 ciphertext key ID remains available. TOSS recurring startup fails on blank, placeholder, duplicate, unknown, or missing key configuration without printing secret values.
- Treat any retained pre-V2 ciphertext as a production data-strategy blocker requiring a separately approved migration; the fresh-only V1 baseline does not supply or require a legacy decryption path.
- Set `APP_PAYMENT_SCHEDULER_ZONE` only when the approved payment business zone differs from `Asia/Seoul`.
- Verify renewal, reconciliation, and expiration boundary tests against the configured business-zone clock when changing `APP_PAYMENT_SCHEDULER_ZONE`; host-default midnight must not determine payment dates.
- Tune `PAYMENT_RECONCILIATION_BATCH_SIZE` and `PAYMENT_RECONCILIATION_ISSUE_DETAIL_LIMIT` within operational memory/query budgets. Runtime caps are 1000 rows per batch and 500 returned issue details.
- Set `PAYMENT_RECONCILIATION_COMPLETED_ORDER_LOOKBACK_DAYS` and `PAYMENT_RECONCILIATION_COMPLETED_ORDER_MAX_PER_RUN` for the bounded recent-`DONE` verification path. Defaults are 30 and 500; runtime caps are 365 and 5000.
- Confirm `TOSS_PAYMENT_LOOKUP_BY_ORDER_ID_URL` or `app.payment.billing.payment-lookup-by-order-id-url` points to the Toss production API.
- Confirm `TOSS_CANCEL_URL` or `app.payment.toss.cancel-url` points to the Toss production cancel API.
- If email notification is desired, set `PAYMENT_RECONCILIATION_NOTIFICATION_ENABLED=true` and `PAYMENT_OPERATIONS_OPERATOR_EMAIL`.
- Confirm application logs do not include raw billing keys, exact provider transaction identifiers, provider request URIs, full reconciliation issue objects, transport exception messages/stacks, raw provider payloads, or raw card data.
- Confirm admin payment pages are restricted to `ROLE_ADMIN`.
- Confirm WARN-level reconciliation aggregate logs are collected by the production log monitoring system, and use masked structured Incidents rather than logs for issue-level investigation.
- Confirm the 01:15 withdrawal cleanup retry has exactly one scheduler owner and its aggregate result logs are monitored.
- Rehearse withdrawal with a test user and Provider test double: local cancellation must remain effective during simulated cleanup failure, and a later success or already-removed result must clear key material and resolve the Incident.
- Run `GET /api/admin/payments/reconciliation` after a staging payment rehearsal.
- Run `GET /api/admin/payments/reconciliation-incidents?status=OPEN` after a staging payment rehearsal.
- Run `GET /api/admin/payments/receipts` after a successful staging charge and confirm only normalized absolute HTTPS receipt links are actionable. Credential-bearing, malformed, non-HTTPS, and non-standard-port retained values must return `null` or remain non-clickable.
- Run `GET /api/admin/payments/operation-audit-logs` after changing a reconciliation incident status and confirm an audit row exists.
- For refund rehearsal in a safe Toss test/staging environment, run preview → create → approve → execute and confirm `payment_refunds` plus audit logs update without exposing raw provider/card data.
- For entitlement correction rehearsal, use a safe succeeded refund row and run preview → create → approve → execute. Confirm `payment_entitlement_corrections`, `user_subscriptions`, optional local `billing_agreements`, and audit logs update without provider billing-key delete calls.
- For settlement rehearsal, import a safe CSV and confirm `payment_settlements`, settlement status counts, ignore workflow, and audit logs update without changing subscription/payment/refund/provider state.
- Keep the deployment on one application scheduler instance. Scheduler lock remains out of active scope unless more than one application instance will run.
- V1 uses only the fresh `schema.sql` baseline. Retained-data migration or query-plan rehearsal is outside the operator path and requires a separate approved migration requirement.

## 9. Webhook Boundary

Webhook can be added later as an auxiliary event channel for supported Toss events.

Do not treat webhook payloads as the only source of truth for subscription activation or renewal. For ATStudio recurring billing, provider API response and provider lookup reconciliation remain the primary verification path.

If webhook is introduced later:

- Accept only HTTPS endpoint traffic in production.
- Return HTTP 200 only after the payload is safely parsed and recorded.
- Verify any provider-provided secret or signature mechanism available for the event type.
- Store only sanitized event metadata.
- Re-query provider state before mutating paid access.

## 10. Follow-up Scope

Separate REQ/SR items are still needed for:

- Slack/SMS/in-app operator notification channels.
- Tax invoice request implementation only after B2B invoice, bank-transfer, postpaid, or contract purchase scope is approved.
- Toss Settlement API adapter automation, if manual CSV import becomes insufficient.
- Any additional PG adapter selected by a future approved product requirement.

Cash receipt issue/cancel automation is intentionally on hold while ATStudio uses card-only recurring billing. Reopen it only if a cash-like payment method or a separate cash receipt request flow is approved.
