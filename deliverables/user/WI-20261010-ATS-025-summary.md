---
version: 1.0
last_updated: 2026-10-10
project: ATS
owner: SE
category: work-summary
status: stable
dependencies:
  - path: REQ-20261010-ATS-008.md
    reason: Approved preparation scope
  - path: ../agent/WI-20261010-ATS-025-evidence-pack.md
    reason: Reproducible checks and source identities
---

# WI025: Production Bootstrap Preparation

> TL;DR: Local candidate complete; native execution and independent PG approval pending.

## Result

Prepared a fixed-target one-shot helper, synthetic tests and an operator runbook
for `atstudio_prod` and `atstudio_app@127.0.0.1`. It refuses existing targets,
files and partial retry; reuses the current deployment contract; provisions only
six plans and DML-only runtime grants. No remote activity, actual SQL, actual
secret generation, app start, package installation, deletion, commit or push
was performed by SE.

Verification: Windows / Python3.14.3, 2026-10-10. New tests32 PASS; existing
deployment tests39 PASS. Offline plan PASS with app_runnable=false and production
HOLD. This is not native MySQL8.0.46 or Python3.12.3 acceptance.

## Risks and Next Gate

MA identified an unsupported routine-privilege view in the draft; replaced with
MySQL8 `mysql.procs_priv` and a regression check. Exact schema grants escape the
underscore to avoid wildcard scope. Root grant inspection does not establish TCP
authentication; that result is explicitly NOT_RUN. Domain/SMTP placeholders and
fresh-manifest UNRECORDED remain. The helper neither starts nor stops MySQL:
MA must separately stop/re-mask it after the approved window, including failures.

Next: MA must trigger WI027 PG review when WI026 is ready. Only reviewed final
bytes and a short-lived target-specific approval may be applied. WI028 integrates
the later actual receipt; this WI does not close REQ008 or authorize runtime.

## Related Documents

- [Operator runbook](../../deploy/single-vps/runbooks/production-bootstrap.md): Gates, commands, partial-failure preservation and cleanup.
- [Evidence Pack](../agent/WI-20261010-ATS-025-evidence-pack.md): Exact commands, limits and hashes.

## Reopened WI025 Completion Addendum (2026-10-10)

MA reported the original native guard stopped before DB/account/approval/env/
claim/receipt creation: disabled admin_address was NULL, not an empty string.
Other tested predicates passed and no33062 listener existed; MySQL was stopped
and re-masked per MA. This replacement SE did not inspect or change the server.

Changed only the helper predicate to `COALESCE(@@global.admin_address,'')=''`.
Local Python3.14.3:34 tests PASS (32 existing plus2 regressions); offline plan
PASS. Exact query contract, rejected0/NULL/empty results and33062 listener
blocking are tested. Native NULL/empty/nonempty semantics remain pending MA's
SELECT-only truth table (NULL, empty, IPv4, IPv6, hostname) and actual predicate.

Helper SHA-256:
`28e473a075e4c48d70b542cbaa36cefbedc48a9f0b5102ffa276430b58750191`.
Bundle SHA-256:
`15aa91639be237e03c71b7f2df2b4923879b849981b9abdf71efda19aedea91d`.

PG accepted the correction concept, not final native acceptance. Next: WI027
re-reviews these bytes before MA resumes. Use fresh real `/root/wi025-v2` with
the direct new helper path and matching root; preserve old `/root/wi025` and
historical hashes. No remote SQL, entropy, deployment, dependency or shared
source change occurred here. Production HOLD and app_runnable=false remain.

## Second Native Correction Completion (2026-10-10)

The approved handoff reports two native ACTIVE AUDIT built-ins with NULL library:
`sha2_cache_cleaner` and `mysqlx_cache_cleaner`. The plugin query now exempts only
these exact names with SQL NULL library. Unknown ACTIVE AUDIT (including other
built-ins), either known name with non-NULL/empty library, and ACTIVE mysqlx or
group_replication still fail closed. Other guards are unchanged.

Verification:37/37 tests PASS, offline plan PASS; local Python3.14.3. Added exact
query/37-case synthetic classification, malformed/ambiguous result and SQL-error
regressions. No native SQL or binary integrity proof is claimed. No remote,
secret or service operations occurred. Existing history/hashes remain intact.

Final helper SHA-256:
`7333b068d0264045b8130519afc6e0f62589912ba2ef0b31772ff84033960a1b`.
Final bundle SHA-256:
`b91766934c522b1c4549b8a3057f95eae94c9a542e107fb9c7c39d7df3b24b9c`.

Next: independent WI027 review, then MA's bounded native verification. Any later
authorized execution uses fresh real `/root/wi025-v3`, its direct helper and
matching root, with a newly pinned archive/approval. Old stages stay immutable.
Production remains HOLD; this does not approve app startup or close REQ008.
