---
version: 1.0.0
last_updated: 2026-10-23
project: ATS
owner: SE
category: summary
status: stable
related_wi: WI-20261023-ATS-005
---

# WI-20261023-ATS-005 Summary

The local preparation tool is implemented. It can build an offline exact plan
and, only under separately recorded future user authorization, inspect/apply/
reconcile the proposed two-account TEST fixture. Its default remains
`LOCAL_PREPARATION_ONLY`; no server, DB, service or application was started.

The proposal preserves every existing row and all ID1 fields. It inserts two
new USER rows, promotes only the designated new row to ADMIN with one audit,
and grants only the new buyer one nonbilling entitlement. It requires an
existing active INDIVIDUAL plan with daily quota>=1; no plan is created.

Verified TEST-fixture flags require explicit future approval and do not claim
email verification or human consent. The tool creates no mail, consent,
payment, token, license or download records. The grant is MONTHLY/CANCELLED
through the same explicit KST date with no pending change. Midnight or a later
audio-test date requires new readiness and approval; there is no auto-extension.

Twelve SE tests pass, including the actual retained JAR's BCrypt10 compatibility
and72-byte limit. Independent review tests cover preservation, SQL-session
failure and commit uncertainty. Native behavior remains untested. A possible
commit is retained for read-only reconciliation and cannot be retried silently.

The candidate is frozen at manifest SHA-256
`3a0f4fd30afc012a8e1a0c46d70b87ed1d2ef5492c2f8143121dc741da35654c`.
WI006 owns the final independent allowance. Current local approval gives no
permission to execute the future native commands or start login/audio work.

[Evidence Pack](../agent/WI-20261023-ATS-005-evidence-pack.md) ·
[Runnable tools and future operator contract](../../build/WI20261023-005-account-fixture/README.md)
