---
version: 1.0.0
last_updated: 2026-10-23
project: ATS
owner: SE
category: summary
status: stable
related_wi: WI-20261023-ATS-002
---

# WI-20261023-ATS-002 Summary

Prepared a bounded, DB-only account inspection for MA's approved phase1.
The first call checks the current successful v8r1 evidence and cold/quarantine
state without starting services. A separately pinned call can temporarily
start only the isolated read-only MySQL process, return numeric account IDs,
roles and eligibility, then independently verify restoration.

Sixteen focused local tests pass. Existing source-mode containment disables
scheduled events and replication startup before SQL, keeps networking closed,
and preserves the original MySQL mask. The implementation returns no password,
token, email, nickname, personal name or credential hash.

Historical first-admin designation/APPLIED receipts and the latest successful
signup can establish designated numeric IDs. Other rows remain unbound
candidates even if eligible. ACTIVE and CANCELLED entitlements retain access
through their inclusive expiry; the entitled USER also needs daily quota.
Account login prerequisites do not prove password knowledge or login success.

No server access, database operation or application start was performed by
this agent. Independent WI003 review and MA's exact target observations remain
the next step. The phase1 scope does not approve account/role/entitlement
changes, login, audio execution or payment operations.

See the [Evidence Pack](../agent/WI-20261023-ATS-002-evidence-pack.md) and
[operator recipe](../../build/WI20261023-002-account-preflight/README.md).
