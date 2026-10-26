---
version: 1.2.0
last_updated: 2026-10-24
project: ATS
owner: PG
category: summary
status: stable
related_wi: WI-20261024-ATS-002
---

# WI002 Independent Exact Fixture Review

The final candidate passes local independent review for the current approved
single fixture application on **2026-10-24 Asia/Seoul**, plan1 with daily quota5.
Candidate SHA256:
`f42d9cfe325f8105d908db46f67b5fa93bc72ada6178eb878af8f75a2f5f7719`.

- Independent 8 and implementation 7 focused tests pass. Source643, retained JAR
  and frozen WI005/WI011/WI014 remain unchanged.
- Fresh read-only inspection and all five cleanup checks must pass before one
  apply. Existing ID1 and all old rows/cells remain protected.
- Exact effects are two new users, promotion of only the new admin, one audit
  row and one buyer subscription. The subscription is MONTHLY/CANCELLED with
  start=expiry=2026-10-24 and NULL pending fields; TEST verification creates no
  mail/consent event or billing authority.
- New immutable phase approval paths preserve old approvals. Uncertain commit
  or result storage stops writes and retains known evidence without replay.

PG accessed no real input, UI, server or database. Actual application and access
restoration remain pending MA receipts. No app/login/audio/payment or automatic
date extension is included. See the [evidence pack](../agent/WI-20261024-ATS-002-evidence-pack.md)
and [exact review receipt](../../build/WI20261024-002-fixture-apply-review/review-receipt.json).

## Native Application Addendum: 2026-10-24

MA's safe receipt reports APPLIED with COMMIT ACKNOWLEDGED: new admin user2,
buyer user3, audit1 and grant1, exactly matching day2026-10-24, plan1/quota5 and
the approved changes. Inspection and application each pass all five DB cleanup
checks; a separate cold post-stop probe also passes.

PG interpreted the safe receipts only. Existing data preservation is evidenced
by the reviewed transaction's exact checks and acknowledged COMMIT, without a
new PG row query. TEST verification does not establish email verification,
consent or login. No app/login/audio/payment or date extension occurred under
this operation. Access restoration remains pending its final separate receipt.
See the [native interpretation](../../build/WI20261024-002-fixture-apply-review/native-interpretation.json).

## Restoration Completion Addendum: 2026-10-24

The approved fixture application and restoration are accepted. MA's final
receipt confirms firewall restoration, provider logout, browser-tab cleanup,
SSH key removal, agent Stopped/Disabled, key input window closure and private ACL
verification. PG checked linked receipt hashes and four screenshot byte hashes;
it did not directly inspect UI, OS or DB state.

The prior access-PENDING state is superseded by this completion. App/login/audio,
payment and additional writes remain excluded. The subscription begins and
expires on 2026-10-24 with no automatic extension. See the
[access interpretation](../../build/WI20261024-002-fixture-apply-review/access-interpretation.json).
