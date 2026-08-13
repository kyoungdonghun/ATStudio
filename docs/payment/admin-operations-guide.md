---
version: 1.7
last_updated: 2026-08-14
project: ATS
owner: docops
category: guide
status: stable
dependencies:
  - path: index.md
    reason: Payment documentation navigation
  - path: system-overview.md
    reason: API and table structure
  - path: ../design/payment-operations-runbook.md
    reason: Operational procedure source
---

# Admin Payment Operations Guide

> Purpose: Explain how operators should use the `/admin/payments` payment operations screen and where it differs from general user subscription management.

---

## 1. Screen Boundary

Use `/admin/payments` for payment evidence and payment-operation workflows.

Use `/admin/user-subscriptions` for ordinary subscription state management that is not tied to a specific payment operation.

| Need                                                                                                | Use                         |
| :-------------------------------------------------------------------------------------------------- | :-------------------------- |
| Review payment orders, charges, billing agreements, provider issues, refund, correction, settlement | `/admin/payments`           |
| Manually adjust a user's subscription status, cycle, or expiration without refund evidence          | `/admin/user-subscriptions` |
| Explain a user-facing subscription plan list                                                        | `/admin/subscriptions`      |

## 2. Shared Safety Rules

Operators must treat admin payment operations as audit-sensitive.

- Do not use settlement or reconciliation rows to mutate subscription access.
- Do not run refund execution unless the refund request is approved and the target payment is confirmed.
- Do not run entitlement correction until provider refund has succeeded.
- Do not paste raw billing keys, raw card numbers, Toss secret keys, `authKey`, or `customerKey` into notes.
- Do not paste an exact provider payment key into Incident, refund, settlement, or audit notes. Use the masked/reference fields already shown by the screen.
- Use notes to record support-safe evidence and decision reasons.

## 3. Orders Tab

Purpose:

- Inspect local `payment_orders`.
- Confirm order purpose, status, amount, provider, and linked subscription context.

Use when:

- A checkout callback looked wrong.
- User claims payment did not complete.
- An upgrade or renewal charge needs tracing.

Avoid:

- Treating an `IN_PROGRESS` order as payment success.
- Reusing old Toss redirect URLs from stale orders.

## 4. Automatic Payment Tab

Purpose:

- Inspect `billing_agreements`.
- See provider, status, masked method, next billing date, failure count, and cancellation state.

Use when:

- A user cannot upgrade because billing method is missing or expired.
- A removed billing key error was detected.
- Renewal is not charging as expected.

Expected sensitive-data boundary:

- Masked payment method may appear.
- Raw billing key must not appear.

## 5. Payments Tab

Purpose:

- Inspect finalized `subscription_payments`.
- Start refund preview from a successful payment row.

Use when:

- Support needs to find the exact charge to refund.
- Settlement review requires local finalized charge evidence.

Rule:

- Refundable payment must be `DONE`, provider-backed, linked to a payment order, and have provider payment key metadata.

## 6. Incidents Tab

Purpose:

- Review persisted local/provider reconciliation incidents.
- Update incident status through operational workflow.

Typical statuses:

- `OPEN`
- `ACKNOWLEDGED`
- `RESOLVED`
- `IGNORED`

Use when:

- Scheduled reconciliation found a mismatch.
- On-demand reconciliation finds different local/provider state.
- A withdrawn user's Provider billing-key deletion failed and the daily cleanup retry is pending.

Rule:

- Incident status changes do not change payments, subscriptions, billing agreements, refunds, or provider state.
- Withdrawal cleanup success resolves its matching agreement-scoped Incident automatically. Operators must not restore renewal eligibility or create a refund merely to close this Incident.
- An order in `PROVIDER_SUCCEEDED` is finalize-only. Do not start a replacement charge; keep the Incident open until local finalization converges or an approved disposition is recorded.

## 7. Receipts Tab

Purpose:

- Review safe receipt evidence from successful provider charges.

Use when:

- Support needs receipt URL/key evidence.
- Operators need to confirm whether receipt metadata was captured.

Current scope:

- Receipt evidence capture exists.
- Only normalized absolute HTTPS receipt links without embedded credentials or non-standard ports are clickable.
- A retained unsafe URL appears as a non-clickable support reference or review-needed state. Do not copy it into the address bar or an incident note.
- Cash receipt issue/cancel mutation is not implemented for the current card-only recurring scope.

## 8. Audit Log Tab

Purpose:

- Review append-only payment operation audit events.

Use when:

- Need to know who changed an incident status.
- Need refund/correction/settlement workflow trace.
- Need to confirm receipt evidence creation was logged.

Rule:

- Audit logs are evidence, not workflow controls.

## 9. Settlement Tab

Purpose:

- Import settlement CSV evidence.
- Compare provider settlement evidence with local payment/refund ledgers.
- Generate missing-provider settlement review rows.
- Ignore reviewed rows with a note.

Current source adapters:

| Source                  | Status         |
| :---------------------- | :------------- |
| `CSV_MANUAL`            | Implemented    |
| `SYSTEM_RECONCILIATION` | Implemented    |
| `TOSS_API`              | Future adapter |

Operator workflow:

1. Select a nonempty `.csv` file with a filename of at most 255 characters,
   allowed/blank CSV media type, and size at most 5 MiB. Optionally enter an
   import note of at most 500 characters, then confirm once. Never enter PII,
   credentials, payment keys, Provider identifiers, tokens, or other sensitive
   information. The UI warns about this, but free text has no DLP guarantee.
2. The screen creates one lowercase UUIDv4 and sends it only in the
   `Idempotency-Key` header. The file and trimmed nonblank note are multipart
   parts; the note is not a query parameter. Do not put the key in a URL,
   query, note, ticket, screenshot, or log.
3. Treat HTTP `200` with `failedRows > 0` as partial completion. Review every
   returned row error; the screen retains the exact selected `File`, DOM file
   input, and note for correction.
4. For every normal import result, confirm
   `totalRows == importedRows + skippedDuplicateRows + failedRows` and
   `sum(statusCounts) == importedRows`. Import returns all errors within the
   1,000-row ceiling and reports `omittedErrorCount=0`.
5. Expect one import POST and one Settlement-list reload after a returned
   result. A transport error triggers one read-only same-key recovery GET.
   There is no automatic second POST or polling.
6. When recovery reports `PROCESSING`, keep the file and note and use the
   manual `import result recovery` action later. A pending attempt blocks a new
   import. When recovery is terminal, review the aggregate; per-row errors are
   available only from the original POST response.
7. On zero failed rows, the screen clears the React selected file and DOM file
   input only after the required list reload succeeds.
8. On list-reload failure, keep the retained file and note and resolve the
   uncertainty before another explicit action. The screen does not claim full
   success.
9. To IGNORE a reviewed row, enter a note that remains nonblank after trimming
   and is at most 500 characters, then use the existing danger confirmation.
   No typed phrase is part of Settlement IGNORE.
10. For missing-provider reconciliation, use an omitted 30-day default or an
    inclusive range of at most 90 days. At most 5,000 finalized payments are
    processed. Review the first 200 returned errors and any additional count in
    `omittedErrorCount`; there is no automatic retry or recovery operation.

Current strict CSV policy:

- Encoding is UTF-8 only, with one optional leading BOM. The dialect uses comma,
  double-quote fields, doubled-quote escapes, quoted LF/CRLF newlines, and no
  bare CR.
- Headers are trim-plus-lowercase normalized, unique, allowlisted, and
  order-independent. Required fields are `provider`, `order_id`,
  `gross_amount`, `net_settlement_amount`, and `settlement_base_date`.
  Unknown headers are rejected, and every row must have exact header width.
- At most 1,000 nonblank logical data records are accepted. Header and blank
  records do not count; invalid and duplicate records do count.
- V1 accepts exact `TOSS`, exact `KRW`, order ID up to 64 characters, provider
  identifiers up to 200, and provider status up to 100. Evidence values are not
  truncated and reject controls, newline separators, and edge whitespace.
- Amounts use plain nonnegative decimal notation, scale at most 2, and must fit
  `DECIMAL(15,2)`. Values canonicalize to scale 2; no rounding occurs. Dates are
  strict `yyyy-MM-dd`, and payout date cannot precede base date.
- Excel sources must be exported to CSV before import.

Settlement statuses:

| Status                          | Meaning                                                                                       |
| :------------------------------ | :-------------------------------------------------------------------------------------------- |
| `MATCHED`                       | Local payment/refund data aligns with settlement evidence.                                    |
| `MISMATCHED`                    | Local record exists but amount/refund/fee/VAT/net comparison needs review.                    |
| `LOCAL_PAYMENT_NOT_FOUND`       | Provider settlement evidence has no matching local payment.                                   |
| `PROVIDER_SETTLEMENT_NOT_FOUND` | Local finalized payment has no imported provider settlement evidence for the selected period. |
| `IGNORED`                       | Operator reviewed and intentionally ignored the row.                                          |

Rule:

- Settlement operations do not change subscriptions, billing agreements, payment status, refund status, or provider state.
- A same-key POST never processes the file again. `PROCESSING`, `COMPLETED`,
  and `FAILED` conflicts are recovered through the same header; a new key is
  created only for a new explicit operator action after terminal review.
- Every accepted CSV claim, including all-duplicate and orchestration-failed
  attempts, is retained in the dedicated import-attempt ledger. List and
  numeric detail are ADMIN-only; same-key recovery is owner-scoped.
- A duplicate Settlement is counted only for the exact deduplication unique
  constraint plus winner confirmation. Unrelated integrity failures are not
  relabelled as duplicates.
- Reconciliation has no import-attempt recovery key. An orderless finalized
  payment is counted once as failed with bounded error evidence.
- The server revalidates the IGNORE note, authenticated ADMIN principal, and
  locked authoritative active ADMIN user before it locks or mutates the
  Settlement. The first actor, time, normalized note, status, and audit row are
  retained. Every otherwise-valid repeat returns `INVALID_STATE_TRANSITION`
  with no overwrite and no new audit.
- Duplicate atomicity, durable CSV attempt evidence, orderless accounting, and
  count conservation are implemented at the source/H2 boundary by WI-056.
- Strict CSV dialect/field/range policy and reconciliation ceilings are
  implemented under WI-20260809-ATS-067 and accepted by QA-INTEG v1.2 and PG
  v1.1 at the repository/non-database boundary. DG-067-09B is
  `RUN-PASS-CLEANED`: the recorded 42/506/173/90/6 fresh-MySQL manifest matched
  independent validation, all three isolated settlement concurrency tests
  passed under `ddl-auto=validate`, and both exact disposable databases were
  dropped. This does not authorize a new database run or establish production
  readiness. In the separate general local-Subscription correction flow,
  execute alone requires the trimmed exact phrase `권한 보정 실행`; approval
  remains an ordinary confirmation.

## 10. Refund Tab

Purpose:

- Preview refundability.
- Create refund request.
- Approve refund request.
- Execute provider cancel/refund through Toss.

Workflow:

1. Enter subscription payment ID and preview.
2. Confirm refundable amount.
3. Create refund request with reason code and note.
4. Approve request.
5. Enter the required confirmation text. The screen reads the exact refund
   detail and sends one execute POST only when that fresh row is `APPROVED`.
6. If execute delivery is rejected or its response is lost, let the screen run
   its one bounded exact-detail recovery read. Do not submit another execute.
7. Review the durable status and the separate recovery outcome.

Important statuses:

- `REQUESTED`
- `APPROVED`
- `PROCESSING`
- `SUCCEEDED`
- `FAILED`
- `PENDING_PROVIDER_CONFIRMATION`
- `CANCELLED`

Rule:

- Refund execution calls provider cancel/refund and records the result. It does not automatically modify subscription access.
- A fresh `PROCESSING` refund is already owned by a claim. A stale or ambiguous row must retain the same refund ID and idempotency key; do not create a replacement request to bypass it.

## 11. Entitlement Correction Tab

Purpose:

- Apply explicit local subscription state after a succeeded refund.

Workflow:

1. Start from a succeeded refund.
2. Preview target subscription, billing cycle, status, expiration, and pending-change behavior.
3. Create correction request.
4. Approve correction request.
5. Enter the required confirmation text. The screen reads the exact correction
   detail and sends one execute POST only when that fresh row is `APPROVED`.
6. If execute delivery is rejected or its response is lost, use the resulting
   read-only status recovery. Do not repeat correction execute.

Rule:

- Entitlement correction is separate from refund.
- Provider billing-key delete is not called by entitlement correction.
- Local billing agreement cancellation can be selected when the support-approved outcome requires it.

## 12. Shared Execute Recovery

Exact recovery reads:

- Refund: `GET /api/admin/payments/refunds/{refundId}`
- Entitlement correction:
  `GET /api/admin/payments/entitlement-corrections/{correctionId}`

The screen uses four outcomes in addition to the domain status:

| Outcome         | Operator interpretation                                                                                                              |
| --------------- | ------------------------------------------------------------------------------------------------------------------------------------ |
| `COMMITTED`     | The exact durable refund or correction detail is `SUCCEEDED`.                                                                        |
| `FAILED`        | The exact execute/detail result is terminal `FAILED` or `CANCELLED`.                                                                 |
| `RELOAD_FAILED` | Execute returned `SUCCEEDED`, but the required detail or committed-result list reload failed. The execute result remains successful. |
| `UNKNOWN`       | The durable result is in flight or unreadable, so neither success nor terminal failure is proved.                                    |

Operator controls:

- Refund `PROCESSING`/`PENDING_PROVIDER_CONFIRMATION` and correction
  `PROCESSING` rows are hydrated as `UNKNOWN` after a list or browser reload.
- `status again` performs the exact detail GET only. It does not approve,
  execute, call Toss, or mutate local state.
- An `UNKNOWN` row unlocks before execution only when its exact detail returns
  `REQUESTED` or `APPROVED`. `REQUESTED` restores approval only. A later
  execute from `APPROVED` still needs typed confirmation and another fresh
  exact-detail preflight.
- `UNKNOWN` and `RELOAD_FAILED` lock the exact operation and linked refund or
  correction mutations. A refund and its corrections cannot bypass each
  other's ambiguity.
- Pending execute owns status reads from preflight through recovery. Pending
  status reads are deduplicated and keep execute blocked until they finish.
- Current intent/read/view generations discard stale detail and list results;
  an old tab, page, success, or failure cannot replace newer authoritative
  state.
- Refund and correction execute POSTs are excluded from authentication replay.
  A `401` cannot be refreshed and replayed as a second mutation.
- Automatic refund execute retry, automatic correction execute retry, and
  recovery-read Provider calls are all zero. A committed execute with a failed
  reload shows reload-specific feedback rather than a mutation-failed message.

## 13. Suggested Support Triage

| User Report                                              | First Check                                                   | Next Check                                                                     |
| :------------------------------------------------------- | :------------------------------------------------------------ | :----------------------------------------------------------------------------- |
| "I paid but subscription did not activate."              | Orders tab                                                    | Payments tab, incidents tab                                                    |
| "I cannot upgrade."                                      | Automatic payment tab                                         | Manage page billing method re-registration flow                                |
| "I was charged twice."                                   | Payments tab                                                  | Refund preview, orders tab                                                     |
| "I cancelled but still have access."                     | User subscription status                                      | This is expected until `expiresAt`                                             |
| "Payment failed during renewal."                         | Automatic payment tab                                         | Orders tab, renewal failure email/logs                                         |
| "A renewal retry looks duplicated."                      | Orders tab; compare billing period and command/order identity | Payments tab; confirm only one finalized payment exists                        |
| "Provider and local numbers do not match."               | Incidents tab                                                 | Settlement tab, runbook                                                        |
| "A refund is stuck in progress."                         | Refund tab; keep the same refund row                          | Incident/audit evidence; do not submit a replacement refund                    |
| "I withdrew, but automatic payment may still be active." | Automatic payment tab                                         | Incidents tab; verify local `CANCELLED`, deleted user, and cleanup retry state |

## Related Documents

### Required References

- [System Overview](system-overview.md): API and ledger structure.
- [Payment Operations Runbook](../design/payment-operations-runbook.md): Production response procedures.

### Reference Documents

- [Acceptance Test Checklist](acceptance-test-checklist.md): Admin operation acceptance checks.
- [Payment Refund, Receipt, Settlement, and Tax Invoice Policy](../design/payment-refund-receipt-settlement-policy.md): Detailed policy.
- [P1 Payment Integrity Closure](../audit/p1-payment-integrity-closure-20260715.md): Technical proof and open production gates.
