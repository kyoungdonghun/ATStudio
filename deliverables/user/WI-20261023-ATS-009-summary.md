---
version: 1.1.0
last_updated: 2026-10-23
project: ATS
owner: PG
category: summary
status: stable
related_wi: WI-20261023-ATS-009
---

# WI009: independent read-only recheck review

The exact WI008 candidate passes independent review for the currently approved
account/plan read-only recheck. Its manifest SHA256 is
`448253685c0bd2c2fc7fc3e0d2e70a7c99421dbe9c204bf0ebce2f09b4cdab3c`.
Five files match; 29 focused tests pass (21 implementation, 8 independent).
Source 643, the retained JAR and earlier frozen tools remain unchanged.

The candidate adds plan ID/type/active/quota and each user's total historical
subscription count to the successful read-only procedure. It distinguishes no
effective entitlement from no historical subscription rows. Existing lifecycle,
read-only containment and independent cleanup remain unchanged. New scope/unit
and fixed expiry prevent accidental reuse of the earlier operation.

MA received the exact allowance before execution. PG's work was local only;
the subsequent native and restoration receipts are interpreted below. This
review grants no account/data change, application start, login, mail, fixture
or audio execution.

[Review evidence](../agent/WI-20261023-ATS-009-evidence-pack.md),
[exact receipt](../../build/WI20261023-009-readonly-review/review-receipt.json),
[operator procedure](../../build/WI20261023-008-readonly-recheck/README.md).

## Native observation addendum

MA's approved read-only execution completed, and PG independently interpreted
the local receipts. Inventory passed, all four DB cleanup checks passed, and
the target-bound post-stop probe passed. Provider/SSH restoration is still
awaiting its separate final receipt at this addendum's creation.

ID 1 remains the only observed user: USER, verified, not deleted, password present,
with both total and effective subscription counts 0. Six plans exist. Active
finite INDIVIDUAL plans ID 1 (5 downloads/day) and ID 2 (20/day) meet the frozen
fixture criteria; ID 1 is the smallest eligible quota observed. This does not
select or authorize a grant. Actual credentials/login remain unverified.

The initial probe stopped before DB startup. A measured two-second clock offset
supports the expiry-boundary explanation, but the exact failing guard is not
proven. The same probe's later successful receipt is retained separately.
No account/data change, app start, login, mail or audio execution occurred.
[Native interpretation](../../build/WI20261023-009-readonly-review/native-interpretation.json).

## Final restoration

MA's final receipt confirms original firewall restoration, preservation of
other rules, provider logout and closure of created browser tabs, SSH key removal
with zero remaining identities, and agent Stopped/Disabled. PG checked that local
receipt; MA owns the provider/OS observations. The owned input process/window
closure remains unconfirmed, so this is not a claim that all native windows closed.
[Access interpretation](../../build/WI20261023-009-readonly-review/access-interpretation.json).

WI009 is complete and unblocks WI010. Future fixture approval must still bind
real private inputs, selected plan/date and a fresh WI005 inspection; the current
projection does not supply its private schema/snapshot and collision evidence.
