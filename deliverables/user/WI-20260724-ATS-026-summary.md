---
version: 1.0
last_updated: 2026-07-25
project: ATS
owner: qa-integ
category: wi-summary
status: confirmed
related_wi: WI-20260724-ATS-026
---

# WI-20260724-ATS-026 Summary

## Verdict

**PASS with cleanup pending**

The Toss test-only recurring-payment acceptance flow completed through an
owned temporary HTTPS origin. The rehearsal covered initial subscription,
two immediate upgrades, cancellation, reactivation, full refunds for the two
rehearsal upgrade payments, entitlement correction, receipts, audit records,
and local/Provider reconciliation.

The final user state was restored to `STANDARD`, `MONTHLY`, and `ACTIVE`, with
the Billing Agreement still `ACTIVE` and the next billing date set to
2026-08-24.

## Verified Flow

- The test-key gate classified both Toss keys as test-only without retaining
  raw values.
- Only the isolated frontend was exposed through the owned temporary HTTPS
  origin. The backend and MySQL were not exposed directly.
- Local and public frontend/API readiness checks returned `200`.
- A new Billing Auth and the first `STANDARD` monthly recurring charge
  succeeded.
- Earlier incomplete orders were changed to `EXPIRED` through the supported
  expiration path, without direct database deletion.
- `STANDARD` to `DELUXE` and `DELUXE` to `PREMIUM` immediate upgrades
  succeeded.
- Cancellation into the grace period and reactivation both succeeded.
- Only the two upgrade payments created by this Work Item were fully refunded.
- One approved Entitlement Correction restored the final subscription state.
- The final user UI confirmed the restored plan and active recurring-payment
  state.

## Ledger and Reconciliation

| Evidence | Result |
|---|---:|
| Issued receipt records | 3 |
| Successful refund ledgers | 2 |
| Payment operation audit logs | 15 |
| Successful entitlement corrections | 1 |
| Reconciliation Incidents | 0 |
| Local reconciliation issues | 0 |
| Provider reconciliation issues | 0 |

Local reconciliation checked three orders and one Billing Agreement with no
mismatch. Provider reconciliation checked one eligible payment and safely
skipped two fully refunded payments; it reported no lookup failure,
not-found result, finalization mismatch, or amount mismatch.

## Security and Scope

- Restricted-value log scans found no bearer token, Auth Key, Billing Key,
  Customer Key, raw card value, or secret label.
- No live key, real-money payment, production database, or client-branch
  change was used.
- Exact Provider identifiers, payment order identifiers, tunnel details,
  runtime process identifiers, account identifiers, and QA credentials are
  excluded from retained documentation.

## Remaining Gates

- Browser automation could exercise the ADMIN request and approval layers, but
  the native typed confirmation prompt was automatically cancelled. The
  authenticated local ADMIN API completed the execute steps. A human must
  still verify the typed-prompt UI interaction during acceptance testing.
- Runtime, tunnel, and disposable-database cleanup belongs to
  `WI-20260724-ATS-017`.
- External SMTP and real-inbox delivery remain the separate operations gate
  identified by `WI-20260724-ATS-025`.

This Work Item proves the isolated test-only payment rehearsal. It does not
claim production release completion.

## Related Documents

- [WI-026 Handoff](../agent/WI-20260724-ATS-026-handoff.md)
- [WI-026 Evidence Pack](../agent/WI-20260724-ATS-026-evidence-pack.md)
- [WI-015 Evidence Pack](../agent/WI-20260724-ATS-015-evidence-pack.md)
- [WI-024 Evidence Pack](../agent/WI-20260724-ATS-024-evidence-pack.md)
- [WI-025 Summary](WI-20260724-ATS-025-summary.md)
