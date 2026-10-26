---
version: 1.2.0
last_updated: 2026-10-24
project: ATS
owner: DocOps
category: summary
status: stable
related_wi: WI-20261024-ATS-003
---

# WI-20261024-ATS-003 Summary

## Final: Exact Fixture Applied and Restored

APPLIED / COMMIT ACKNOWLEDGED, observed by MA at 2026-10-24 00:47:20 KST.
New ADMIN user 2 and buyer USER 3 were created, with audit 1 and grant 1.
The exact approved effects are two user inserts, one new-admin update, one
audit insert and one subscription insert.

Buyer 3 has plan 1 at 5/day, MONTHLY/CANCELLED, start=expiry=2026-10-24 and
NULL pending fields. **The entitlement is for 2026-10-24 KST only; no date
extension is authorized.** TEST verified flags do not prove email verification,
consent or login.

Existing ID 1 and all pre-existing rows/cells are preserved according to the
reviewed exact-delta transaction and COMMIT ACK. There was no separate SQL reread
after commit. Inspection and apply each passed all five DB cleanup checks, and
the separate cold post-probe passed. Original TCP22 was restored, 80/443 stayed
unchanged, provider logout/tabs zero/key removal/identities zero/SSH agent
Stopped/Disabled/key-window closure/private ACL PASS were confirmed.

PG accepted the bounded applied/restored outcome from MA's safe receipts. No
app/login/audio, mail/consent, payment, schema change, deletion or automatic retry
occurred. WI003 is complete; REQ007 remains active for separate app/login/audio.
See [final evidence](../agent/WI-20261024-ATS-003-evidence-pack.md) and
[restoration receipt](../../build/WI20261024-fixture-apply-native/final-restoration.json).

## Historical: Local Review Complete, Native Execution Pending

SE001 preparation and PG002 exact review are complete with
ALLOW_EXACT_APPROVED_SINGLE_FIXTURE_APPLY. SE seven and PG eight focused checks
passed for candidate `f42d9cfe325f8105d908db46f67b5fa93bc72ada6178eb878af8f75a2f5f7719`.
Source643/JAR and frozen candidates are preserved. MA local preflight also
confirmed private-input storage/ACL and SSH agent Stopped/Disabled.

Provider login and the TCP22 proposal are prepared; actual rule submission,
SSH, DB and writes have not occurred. Current user confirmation for the specific
temporary TCP22 security action is pending. WI003 and REQ007 remain active.

MA still needs fresh target/approval gates and inspection with all five cleanup
checks before a single bounded apply. The exact 2026-10-24 KST effects and
exclusions below are unchanged. No actual account creation or restoration is
claimed from local checks. See [current evidence](../agent/WI-20261024-ATS-003-evidence-pack.md),
[PG receipt](../../build/WI20261024-002-fixture-apply-review/review-receipt.json) and
[local preflight](../../build/WI20261024-fixture-apply-native/local-preflight.json).

## Historical Initial Approval Checkpoint

The user approved the exact account/entitlement writes for **2026-10-24 KST**,
after local preparation, independent review and a fresh passing inspection,
with DB shutdown and access restoration afterward. Preparation/review and
native results are pending; no new execution success is claimed. WI003 and
REQ007 remain active. Historical WI013 remains complete.

Preserve ID 1 and every pre-existing row/column. Approved effects are two new
USER accounts, one new uploader's ADMIN promotion, one audit row and one
new-buyer subscription using active INDIVIDUAL plan 1 at 5/day. Verified flags
are TEST-only. Subscription is MONTHLY/CANCELLED with start=expiry=2026-10-24
and pending fields NULL, without billing. Stop if the KST date changes; no
automatic date extension.

The chain is SE001 local preparation -> PG002 exact review -> MA fresh inspect,
bounded apply and DB/access cleanup -> Doc003 result integration. Saved private
inputs are reused; existing evidence/approvals and frozen candidates are preserved.
No app/startup, login, audio, mail/consent event, payment or schema change is
included. An uncertain commit stops writes without automatic replay/deletion.

See [verbatim approval](REQ-20261011-ATS-007.md),
[governing handoff](../agent/WI-20261024-ATS-003-handoff.md) and
[initial evidence](../agent/WI-20261024-ATS-003-evidence-pack.md).
