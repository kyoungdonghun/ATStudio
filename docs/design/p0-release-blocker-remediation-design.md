---
version: 1.0
last_updated: 2026-07-13
project: ATS
owner: SA
category: design
status: stable
dependencies:
  - path: ../audit/full-system-audit-20260713.md
    reason: Source audit and confirmed P0 findings
  - path: ../policies/security-policy.md
    reason: Secret and protected-resource handling rules
  - path: api-spec.md
    reason: Current API contract baseline
---

# P0 Release Blocker Remediation Design

> Purpose: Define the implementation and verification contracts for the three P0 findings confirmed by the 2026-07-13 full-system audit.

## 1. Scope and Invariants

This design covers only the following release blockers:

1. Public exposure of original track storage keys and files.
2. Verification and password-reset secrets written to SMTP failure logs.
3. Recurring charges remaining possible after account withdrawal.

The following invariants must remain true:

- Public users can play a bounded preview.
- Eligible subscribers can download the original through the existing authenticated download API.
- Password-reset requests retain their generic external response behavior.
- Account withdrawal does not trigger an automatic refund.
- Local renewal blocking does not depend on a successful Toss billing-key deletion.
- No live Toss or SMTP call is used for verification.
- This remediation introduces no database schema change.

## 2. Protected Track Media Contract

### 2.1 Public metadata

`GET /api/tracks/{trackId}` must not expose `tracks.audio_file` or another original-object storage key. The current response shape remains compatible, but `audioFile` is nullable and is `null` in public responses.

Admin create, update, and detail responses may retain the original storage key because the admin edit screen currently uses it as operational metadata. This is implemented through explicit public and admin response factories rather than a caller-controlled flag.

### 2.2 Static-resource routing

Every request under `/uploads/tracks/audio/**` is denied before static-resource resolution, including requests made with USER or ADMIN authentication. Thumbnail and other explicitly supported static-resource routes retain their current behavior.

The original file remains in its current physical location during this WI. Moving existing files or changing stored paths is a separate, destructive migration that requires explicit approval. The route denial is therefore the immediate enforcement boundary.

### 2.3 Public stream compatibility

The public stream endpoint must never fall back to serving the complete original file.

- If `preview_file` exists, the endpoint serves that resource with the existing Range behavior.
- If `preview_file` is absent, the endpoint exposes a bounded prefix of the original as a compatibility preview.
- The compatibility boundary is the smaller of 30 seconds and 50 percent of the track duration, estimated proportionally from resource length. If duration is unavailable, 25 percent of the resource is used.
- At least one byte remains outside the public boundary whenever the resource contains more than one byte.
- A Range request starting at or beyond the preview boundary returns `416 Range Not Satisfiable`.
- A request without a Range header returns only the bounded preview region.

This fallback closes full-original retrieval without introducing a new transcoder dependency. Dedicated low-quality preview generation remains a separate follow-up; current documents that claim it already runs asynchronously must be corrected during the documentation WI.

### 2.4 Subscriber download

`GET /api/tracks/{trackId}/download` continues to resolve `tracks.audio_file` only after the existing authentication, subscription, quota, download-history, and license checks. The static-resource denial does not affect this controller-mediated download.

## 3. Secret-Free Mail Logging Contract

### 3.1 External behavior

Verification and password-reset flows retain their current response and exception behavior. An SMTP delivery failure is not returned as account-existence evidence.

### 3.2 Internal logging

Each delivery attempt receives a random `deliveryId`. Logs may contain:

- the `deliveryId`;
- the delivery outcome;
- the exception class name on failure.

Logs must not contain:

- recipient email address;
- subject;
- HTML or plain-text body;
- verification or reset URL/token;
- raw exception message or stack trace from the mail provider.

The service continues to absorb delivery exceptions to preserve the generic external contract. Operations can correlate a request and failure by `deliveryId` without storing the secret payload.

## 4. Withdrawal and Recurring Billing Contract

### 4.1 Local transaction

Within the account-withdrawal transaction, the service performs the following order:

1. Authenticate the withdrawal request.
2. Load the current Toss billing agreement, if present.
3. Mark every non-terminal agreement `CANCELLED` locally before the user is marked deleted.
4. Mark an ACTIVE user subscription `CANCELLED`; no refund is created.
5. Publish a cleanup event containing only the billing-agreement ID when encrypted billing-key material exists.
6. Delete the existing user-owned transient records and mark the user deleted.

The event is handled only after the local transaction commits. A repeated withdrawal or cleanup request is safe: terminal local states and an already-cleared billing key are skipped.

### 4.2 Provider cleanup

After commit, the cleanup handler decrypts the stored billing key and invokes the agreement's registered recurring-payment provider. Provider success clears encrypted key material and `next_billing_at`.

Provider failure does not restore local renewal eligibility. The cancelled agreement retains encrypted key material so cleanup can be retried.

### 4.3 Durable incident and retry

Cleanup failure is stored in `payment_reconciliation_incidents` using the existing `LOCAL_DONE_PROVIDER_NOT_DONE` issue type:

- local status: `CANCELLED`;
- provider status: `BILLING_KEY_DELETE_FAILED`;
- billing agreement and user references: populated;
- failure code and a truncated provider message: populated;
- severity: `WARNING`;
- dedupe boundary: billing-agreement ID.

This reuses the existing operational incident schema and admin view. A daily single-server retry scans only deleted users whose agreements are `CANCELLED` and still contain encrypted key material. Success clears the key and resolves the matching incident; another failure increments the existing incident.

### 4.4 Renewal defense in depth

The due-renewal query excludes deleted users at the database query boundary. `RecurringRenewalService` also checks `user.isDeleted()` before decrypting a key or creating an order. If a deleted user reaches this second boundary, the agreement is cancelled and the provider is not called.

## 5. Failure and Transaction Matrix

| Failure point | Local user state | Agreement state | Provider call | Operational result |
|---|---|---|---|---|
| Password mismatch | unchanged | unchanged | none | existing credential error |
| Local withdrawal transaction fails | unchanged by rollback | unchanged by rollback | none | request fails |
| Provider deletion succeeds | deleted | CANCELLED, key cleared | once | cleanup incident resolved if present |
| Provider deletion returns failure | deleted | CANCELLED, key retained | attempted | open deduplicated incident |
| Crypto/provider throws | deleted | CANCELLED, key retained | zero or attempted | open deduplicated incident without secret data |
| Retry succeeds later | deleted | CANCELLED, key cleared | once per retry run | incident resolved |

## 6. Acceptance-Test Matrix

| ID | Verification |
|---|---|
| MEDIA-01 | Public track detail returns `audioFile: null`; admin detail retains the key. |
| MEDIA-02 | Authenticated USER and ADMIN requests to `/uploads/tracks/audio/**` are denied. |
| MEDIA-03 | A stream backed by `preview_file` preserves normal Range behavior. |
| MEDIA-04 | A stream falling back to the original never returns a region beyond the bounded preview and rejects an out-of-bound Range. |
| MEDIA-05 | Subscriber download still returns the original after existing entitlement checks. |
| MAIL-01 | Simulated SMTP failure logs a delivery ID and outcome. |
| MAIL-02 | Captured logs do not contain recipient, subject, body, token, URL, provider exception message, or stack trace. |
| WITHDRAW-01 | Withdrawal marks the user, active subscription, and agreement locally cancelled before the cleanup event. |
| WITHDRAW-02 | Provider cleanup failure leaves the user deleted and agreement non-chargeable, and creates one deduplicated incident. |
| WITHDRAW-03 | Cleanup retry success clears key material and resolves the incident. |
| WITHDRAW-04 | Due-renewal query and service guard both exclude deleted users; provider charge invocation count is zero. |
| REGRESSION-01 | Existing public listing, preview playback, subscriber download, password-reset generic response, and normal renewal tests pass. |

## 7. Rollback and Migration

- Code rollback uses a normal revert of the implementation commits.
- No automatic file move or database data rewrite is part of this remediation.
- Existing original paths remain valid for the controller-mediated subscriber download.
- A future storage migration may move originals outside the public resource root after a separate backup, dry run, and explicit approval.

## Related Documents

### Required References

- [Full-System Audit](../audit/full-system-audit-20260713.md): Confirmed P0 findings and evidence.
- [Security Policy](../policies/security-policy.md): Protected-resource and secret-handling requirements.

### Reference Documents

- [API Specification](api-spec.md): Current public and admin API baseline.
- [Payment Integration Design](payment-integration-design.md): Recurring billing architecture.
- [Payment Operations Runbook](payment-operations-runbook.md): Reconciliation and incident operations.
