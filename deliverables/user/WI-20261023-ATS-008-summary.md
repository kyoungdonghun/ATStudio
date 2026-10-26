---
version: 1.0.0
last_updated: 2026-10-23
project: ATS
owner: SE
category: summary
status: stable
related_wi: WI-20261023-ATS-008
---

# WI-20261023-ATS-008 Summary

The local read-only account/plan recheck is prepared and frozen. It adds total
subscription rows per user, including expired rows, and existing plan
ID/type/active/quota to the successful WI002 projection. No remote operation,
DB/app start, login or business change was performed by SE.

The distinct read-only scope has a fixed expiry, target binding and a fresh
unit/log name. Original tools, source/JAR and independent cleanup remain intact.
Twenty-one focused local tests pass; PG reported eight independent tests PASS.
WI009 owns the exact final allowance. MA owns the approved server read and
temporary DB start/stop, with all four cleanup results and a post-stop probe.

Candidate bundle SHA-256:
`3799c0145883e4fc201b2581f547287ce8f3e7c053a21d8536452e5c7fc76313`.
See [manifest](../../build/WI20261023-008-readonly-recheck/candidate.json),
[commands and boundaries](../../build/WI20261023-008-readonly-recheck/README.md)
and [evidence](../agent/WI-20261023-ATS-008-evidence-pack.md).

The earlier effective-subscription count0 did not establish absence of historical
rows. Current total counts and actual plan suitability remain pending MA's
native recheck. Unlimited -1 plans are distinguished from the finite plans
accepted by the future fixture tool. No account input, verification claim,
fixture write, plan creation, login or audio is authorized by this recheck.
