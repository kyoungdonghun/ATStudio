# WI-20260716-ATS-006 Summary

## Runtime Behavior

- Local reconciliation now scans every eligible `DONE` payment order and `ACTIVE` billing agreement through configurable ID-keyset batches. It no longer reads only the latest 100 orders or materializes every active agreement.
- Scheduled local mismatch incidents are persisted per batch. Full mismatch counters remain accurate while API issue details are independently capped by configuration and expose `totalIssues` and `issueDetailsTruncated`.
- The configured reconciliation batch size is clamped to `1..1000`; issue detail retention is clamped to `0..500`.
- Provider reconciliation uses the same bounded batch setting. Existing exact-evidence mutation gates and Incident-only mismatch handling remain intact.
- All five payment cron methods now declare `${app.payment.scheduler-zone:Asia/Seoul}`. The approved deployment remains single-server, so no distributed scheduler lock was added.

## Billing-Key Compatibility

- New billing-key ciphertext uses `v2:<keyId>:<nonce>:<ciphertext>`, with the key ID authenticated through AES-GCM AAD.
- Existing `v1:<nonce>:<ciphertext>` remains decryptable through the retained legacy secret.
- Active-key rotation can decrypt ciphertext written by retained v2 keys and writes new ciphertext with the configured active key ID. Missing, removed, duplicate, blank, placeholder, or invalid key configuration fails without exposing key material.
- When `app.payment.provider=TOSS_BILLING`, startup validates the legacy v1 secret, active key ID, and every configured decryption key in every profile. MOCK and non-recurring provider paths retain their existing startup policy.

## Schema and Operations

- Fresh schema and JPA metadata now include `idx_payment_orders_local_reconciliation (status, id, purpose)` and `idx_billing_agreements_local_reconciliation (status, id)`.
- `src/main/resources/db/manual/20260716_payment_reconciliation_indexes.sql` is an additive, guarded patch with index inventory and reproducible MySQL 8 `EXPLAIN FORMAT=JSON` statements.
- API, DB, payment design, system overview, limits, and operations runbook documentation now describe the bounded scan, capped details, key ring, startup guard, scheduler zone, and single-server policy.

## Verification

| Check | Result |
|---|---|
| Focused reconciliation/crypto/scheduler/startup/schema tests | PASS, 69 tests, 0 failures/errors/skips |
| Related payment/billing/renewal tests | PASS, 214 tests, 0 failures/errors; 8 MySQL proof tests skipped by their environment gate |
| Billing key-ring environment binding test | PASS |
| Documentation validator | PASS, Tier 0, links, 399 traceability IDs, and index coverage |
| `git diff --check` | PASS; only repository line-ending conversion warnings were emitted |

No live Toss request or retained/production MySQL operation was executed.

## Remaining Environment Evidence

- `ATS020-X-01` remains `ENVIRONMENT-CONDITIONAL`. The additive patch and query plans must still be rehearsed on an approved retained-database copy with representative row counts, followed by Hibernate validation.
- Secret rotation execution is outside this WI. Operators must retain the legacy v1 secret and every v2 decryption key still referenced by ciphertext before changing the active key.

WI-005 security changes, client worktree files, runtime logs, and unrelated worker changes were not reverted or modified by WI-006.
