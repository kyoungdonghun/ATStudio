---
version: 1.0.0
last_updated: 2026-10-23
project: ATS
owner: SE
category: summary
status: stable
related_wi: WI-20261023-ATS-011
---

# WI-20261023-ATS-011 Summary

The private Windows input form and inspect-only staging wrapper are prepared
and frozen. Eight SE synthetic tests, ten independent PG tests and the native
Windows ACL self-test pass. PG012 allowed the exact inspect-only candidate.
SE performed no server access, real input collection, DB/app start or DML.

MA first verifies the current approval and successful technical staging. The
user then enters both identities and masked passwords once in a dedicated
window. Input is validated against unchanged WI005 rules and saved outside the
repository with current-user-only ACL. Expiry during typing preserves the input;
it does not trigger recollection or automatic remote retry.

The wrapper stages the exact unchanged 22-file WI005 candidate and invokes only
inspect with target/expiry and external approval-byte binding. Output separates
inspection recorded from identity collision, ADMIN, ID1, work and plan conditions.
No input/target/snapshot/payload digest is sent to public output. No date or
apply plan is created; account writes and audio remain unauthorized.

WI011 bundle SHA-256:
`69b0dcd464ff08f690ab7274c744ff617fe70d9a618ea2d9a760fbe807d085a2`.
See [manifest](../../build/WI20261023-011-fixture-inspect/candidate.json),
[exact commands](../../build/WI20261023-011-fixture-inspect/README.md) and
[evidence](../agent/WI-20261023-ATS-011-evidence-pack.md).
The [PG012 receipt](../../build/WI20261023-012-fixture-inspect-review/review-receipt.json)
pins manifest SHA-256 `21daba86032674c87844d9a584fee5ec42e3c689db806e53c0a961ebfa407245`.
Native staging, real-row collisions, human capture and restoration remain
MA-owned verification boundaries.
