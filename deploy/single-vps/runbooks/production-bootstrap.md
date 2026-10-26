---
version: 1.0
last_updated: 2026-10-10
project: ATS
owner: SE
category: guide
status: draft
dependencies:
  - path: ../../../deliverables/user/REQ-20261010-ATS-008.md
    reason: Exact production bootstrap authorization and exclusions
  - path: ../CONFIGURATION.md
    reason: Existing environment and resource contract
  - path: ../../../docs/design/db-schema.md
    reason: Fresh-only SQL and unrecorded canonical manifest boundary
  - path: host-hardening.md
    reason: Existing root socket-client and service lifecycle safeguards
---

# Fresh Production Bootstrap

> Purpose: Prepare exactly one approved fresh DB and private incomplete environment,
> with a separately reviewed one-shot operator helper. No app startup authority.

## 1. Status and Scope

WI025 implements a candidate, not a remote-execution receipt. Local synthetic
tests cannot establish Linux/MySQL compatibility, durability or authentication.
MA owns remote actions only after WI027 PG review of the final helper identity.
The helper never starts/stops/enables services, installs packages, runs SSH,
creates an ADMIN, contacts a Provider, copies credentials, or deletes anything.

Fixed targets: `atstudio_prod`, `atstudio_app@127.0.0.1`, and
`/etc/atstudio/atstudio.env`. No CLI switches select another DB/account/socket
or secret destination. Root authenticates through the existing auth_socket
configuration, without a password or authentication-plugin change.

The approved target remains 2vCPU / 8GB / 100GB. Reuse the existing resource
candidate without alteration: Hikari10 with admission2/global,1/IP; app cgroup3GiB;
heap1GiB; MySQL buffer512MiB. These are settings, not measured capacity.

## 2. Offline Preparation

From the reviewed local source tree, run:

```powershell
python -B -m unittest discover -s scripts/deployment -p test_production_bootstrap.py -v
python -B -m unittest discover -s scripts/deployment -p test_validate_bundle.py
python -B scripts/deployment/production-bootstrap.py plan
```

`plan` reads allowlisted public source/templates only and invokes no subprocess,
DB, randomness or target-file creation. The existing `validate_bundle.py`
contract must PASS. Exact raw schema/seed byte hashes are pinned in the helper;
line-ending conversion during transfer invalidates them. It emits an aggregate
`bundle_sha256` and input hash inventory including helper and validator.

Copy only that public inventory into the same relative layout under a new,
root-owned `/root/wi025` staging directory after MA authorizes staging. Preserve
bytes and root-owned, non-group/world-writable ancestry. Do not upload an entire
checkout, local YAML, ignored files, credentials or build logs. A pre-existing
stage is HOLD until MA checks its ownership and exact identity; never overwrite
an unknown stage. Execute no code from an untrusted writable directory.
The runbook/test files may accompany the public inventory for review but are
not imported by the native helper. WI026 separately owns JAR/SPA identity.

## 3. MA Preflight and Approval Record

Recheck strict-pinned SSH, approved existing host/boot, recovery access, user
scope and idle exclusive maintenance. Confirm the existing MySQL drop-in and
effective config, no persisted surprise overrides, and no app/encoder/other DB
writers. The helper does not prove absence of every manually launched writer;
the exclusive maintenance attestation covers independent root processes/jobs.

Before any secret operation, review sudo I/O logging, session recording,
audit/security agents, command/process capture and external log forwarding.
Do not run under shell tracing, `script`, a debugger, pipe/response capture or
an instrumentation wrapper. The helper rejects active MySQL AUDIT plugins,
unrecognized SQL components, raw/general/slow logs and replication channels;
an unknown external capture surface is HOLD, not an automatic logging change.
Performance Schema is not disabled: submitted SQL contains no secret or derived
authentication value. Native-generated password output is confined to a private
pipe. These controls are not a guarantee against privileged memory inspection.

Confirm `/etc/atstudio` is root0700; environment, permanent claim and receipt
below must all be absent, including dangling links. Root `.my.cnf` and
`.mylogin.cnf` must be absent and inspectable. Their contents are never read.
Every mysql child uses the established absence guard and a fresh minimal
environment, HOME=/root, no inherited MYSQL variables, and history=/dev/null.
`--no-defaults` is first, socket/protocol/user are explicit, password prompting,
local infile and reconnect are disabled. No `--no-login-paths` is used.

MA temporarily unmasks/starts MySQL only under REQ008, following the bounded
start/failure-stop procedure in [Host Hardening](host-hardening.md#5-mysql-validate-bounded-internal-probe-stop-again).
Keep Nginx masked/inactive; the app unit must be absent or masked/inactive.
Require native MySQL8.0.46, loopback3306 only, no X/admin listener, no anonymous
account and existing root auth_socket. Socket client uses
`/run/mysqld/mysqld.sock`; the server may advertise that path or its Ubuntu
`/var/run` alias. Do not change socket/auth config to make a failed gate pass.

PG and MA approve the exact final bundle before MA creates a root0600 regular,
single-link file `/etc/atstudio/production-bootstrap-approval.json` in root0700.
This is a local administrative attestation, not a signed approval service.
It must contain exactly these fields, no secret material:

```json
{
  "req": "REQ-20261010-ATS-008",
  "review_wi": "WI-20261010-ATS-027",
  "bundle_sha256": "<64 lowercase hex from the reviewed plan>",
  "machine_id_sha256": "<SHA256 of stripped /etc/machine-id bytes>",
  "server_uuid_sha256": "<SHA2(@@server_uuid,256) from approved socket query>",
  "issued_at": "<UTC YYYY-MM-DDTHH:MM:SSZ>",
  "expires_at": "<UTC, at most 30 minutes after issued_at>",
  "operator_checks": {
    "pinned_ssh_target": true,
    "exclusive_maintenance": true,
    "no_session_io_recording": true,
    "no_sql_response_capture": true,
    "no_external_secret_export": true,
    "mysql_temporarily_loopback": true,
    "stop_remask_on_exit": true,
    "root_recovery_ready": true
  }
}
```

Placeholders intentionally fail. Derive host hashes privately on the approved
server; no raw machine ID/server UUID or private configuration belongs in chat.
No approval record is automatically generated by the helper. Exact review WI
must exist and be accepted by MA; a JSON string alone does not supply approval.
An expired preflight can receive a separately reviewed new approval only before
any claim/DB/account/file exists. Never renew to retry a partial application.

## 4. Separately Executed Target Phases

The following commands are future MA actions, **NOT RUN by WI025**. Invoke from
the approved root context without secret arguments or environment inheritance.
Use the installed Python3.12.3; local tests used Python3.14.3.

```bash
env -i HOME=/root PATH=/usr/sbin:/usr/bin:/sbin:/bin LC_ALL=C /usr/bin/python3 -E -s -B /root/wi025/scripts/deployment/production-bootstrap.py plan --root /root/wi025
env -i HOME=/root PATH=/usr/sbin:/usr/bin:/sbin:/bin LC_ALL=C /usr/bin/python3 -E -s -B /root/wi025/scripts/deployment/production-bootstrap.py preflight --root /root/wi025
```

Preflight does not create files, generate secrets, execute DDL/DML, or alter
MySQL global settings. Its process-local umask/core-dump restrictions do change
its own process state. It queries only bounded configuration/account metadata.
It refuses any case-variant existing target database or username at any host.
It also requires `partial_revokes=OFF`, empty mandatory roles, no additional
foreground MySQL clients and the logging/auth/network gates above. A failed
gate means stop and review, not changing global settings or dropping users.

Only after preflight PASS and MA's apply decision:

```bash
env -i HOME=/root PATH=/usr/sbin:/usr/bin:/sbin:/bin LC_ALL=C /usr/bin/python3 -E -s -B /root/wi025/scripts/deployment/production-bootstrap.py apply --root /root/wi025
```

Apply rechecks preflight, creates and fsyncs the permanent root0600
`production-bootstrap.claim`, exclusively reserves root0600 `atstudio.env`,
rechecks fresh target state, then performs these bounded stages:

1. Create the exact database, recheck empty tables, apply pinned schema then seed
   exactly once. No `IF NOT EXISTS`, upsert, migration or disposable tool.
2. Require the exact 43 table names, six exact plan configurations, and zero rows
   in every other table, including users/ADMIN/Tracks/payments. Collect bounded
   structure counts and a target-specific observation digest.
3. Create only the loopback runtime account, initially locked, using
   `IDENTIFIED WITH caching_sha2_password BY RANDOM PASSWORD`, length32.
   Validate the single result shape internally; never print/hash/log that result.
4. Generate two independent 32-byte OS CSPRNG values on the server, Base64 encode
   JWT and billing material, and append the private incomplete environment.
   No actual random values were generated by local tests. Flush/fsync before
   granting or unlocking. The billing key ID is `prod-bootstrap-20261010`.
5. Grant only SELECT/INSERT/UPDATE/DELETE on escaped `atstudio\_prod`.*. MySQL DB
   grants interpret an unescaped underscore as a wildcard when partial revokes
   is OFF. Verify the escaped schema identity by HEX, exactly four nongrantable
   DML privileges, no global/dynamic/table/column/routine/proxy/role grants and
   the exact account state. Routine checks use `mysql.procs_priv`.
6. Recheck host/database safety, unlock only the new account, recheck grants,
   and exclusively create a root0600 safe receipt. Expiry is checked before
   every mutation connection, not only at initial preflight.

Each mutation connection disables its own binary logging before any DDL so
password hashes are not propagated into binlogs. There is no global logging
change. No raw password enters SQL, arguments, child environment or reports.
stdout is captured internally and stderr is discarded; timeout/native errors
produce fixed HOLD codes. The raw child-output size check is post-capture;
queries are fixed and bounded, not an arbitrary command execution interface.

## 5. Acceptance Limits and Mandatory Cleanup

Successful output is `BOOTSTRAP_APPLIED_NOT_RUNNABLE`, never deployment PASS.
The receipt contains public input hashes, table/row/metadata counts and a
structural observation digest; it contains no environment hash or key/password
hash. It is distinct from the canonical disposable manifest format and does not
accept/rewrite its `UNRECORDED` expectation. No schema/app-source fix or
Hibernate validation is performed by this helper.

Root privilege inspection **does not prove runtime TCP authentication**. Receipt
field `runtime_tcp_authentication=NOT_RUN` preserves this boundary. A separately
reviewed password-safe TCP/JDBC read-only check remains required before startup;
this candidate does not create/delete a temporary password client file.

SMTP and domain tokens deliberately remain unresolved. MAIL_PORT is unresolved,
password login/QA bootstrap/Swagger remain disabled and Provider keys remain
empty. An environment file's existence is not a runnable configuration receipt.
Do not source it, print it, launch the app to test it, or enable services.

In success, failure, timeout or interruption, MA must stop MySQL with the
existing bounded lifecycle procedure, confirm inactive/dead, no pending start
job and no listeners, then re-mask and verify the exact mask. Confirm Nginx
unchanged and app never started. The helper intentionally does not own this
service lifecycle; its receipt says `MA_MUST_STOP_AND_REMASK`, not cleanup PASS.
Finally remove only the approved SSH key from the temporary local agent and
restore Stopped/Disabled, with a new cleanup receipt.

No automatic retry, overwrite, DROP, key rotation, password reset, claim removal
or file deletion is provided. DDL is nontransactional across stages. A failure
may leave a partial DB, a locked account, an incomplete env, or an unlocked
account if the last receipt operation fails. Preserve all artifacts; stop and
re-mask MySQL; report only the fixed failure stage/code and bounded metadata.
Losing a generated password before fsync requires new exact recovery approval,
not a silent re-generation. These rules also apply after host power loss.

Remaining gates: exact WI026 JAR/SPA identity, native metadata compatibility,
runtime TCP/JDBC authentication, atmusic.co.kr purchase then DNS/HTTPS/renewal,
SMTP/provider approval, first ADMIN, encrypted external secret recovery,
backup/restore, single-worker/scheduler checks, alerts and bounded mixed load.

## Related Documents

- [WI025 packet](../../../deliverables/agent/WI-20261010-ATS-025-handoff.md): Ownership and exclusions.
- [WI025 evidence](../../../deliverables/agent/WI-20261010-ATS-025-evidence-pack.md): Local results and final identities.
- [Deployment gates](deployment.md): Separate runtime/release approval.
- [MySQL 8.0 password management](https://dev.mysql.com/doc/refman/8.0/en/password-management.html): Native random password output and binary-log behavior; checked 2026-10-10.
- [MySQL 8.0 grant tables](https://dev.mysql.com/doc/refman/8.0/en/grant-tables.html): Supported routine/role/proxy privilege stores; checked 2026-10-10.
- [MySQL 8.0 GRANT](https://dev.mysql.com/doc/refman/8.0/en/grant.html): Database wildcard escaping; checked 2026-10-10.

## 6. Native Finding and Reopened WI025 Addendum (2026-10-10)

This append-only correction supersedes the staging path and helper identity
above; the original instructions and registered hashes remain historical.
MA reported that the initial native configuration guard returned NULL because
disabled `@@global.admin_address` was NULL: its empty-string comparison returned
NULL and ISNULL returned1. Other tested configuration predicates returned1,
and no33062 listener was observed. The failure preceded DB/account/approval/env/
claim/receipt creation. MA reported MySQL stopped and re-masked. These are
MA-attributed observations, not remote verification by this replacement SE.

User approved the narrow correction and re-review. The only helper change is
`COALESCE(@@global.admin_address,'')=''`. NULL and empty are intended to pass;
nonempty addresses must fail. Independent listener checks are unchanged.
Local regression34 PASS and offline plan PASS are not native SQL semantic proof.
Tests assert the exact SQL contract, refuse0/NULL/empty result with HOLD before
writes/entropy, and reject33062 alone or alongside permitted loopback3306.

Current helper SHA-256:
`28e473a075e4c48d70b542cbaa36cefbedc48a9f0b5102ffa276430b58750191`.
Current bundle SHA-256:
`15aa91639be237e03c71b7f2df2b4923879b849981b9abdf71efda19aedea91d`.
PG accepted the bounded correction concept; final WI027 identity re-review and
native verification remain pending. No startup/apply approval is implied.

MA selected a fresh real directory `/root/wi025-v2`; preserve `/root/wi025`
unchanged. Do not use a symlink, overwrite the old stage, change MySQL globals,
or reuse the old bundle approval. Following final WI027 review, invoke the new
helper directly with its matching root (future MA commands, NOT RUN here):

```bash
env -i HOME=/root PATH=/usr/sbin:/usr/bin:/sbin:/bin LC_ALL=C /usr/bin/python3 -E -s -B /root/wi025-v2/scripts/deployment/production-bootstrap.py plan --root /root/wi025-v2
env -i HOME=/root PATH=/usr/sbin:/usr/bin:/sbin:/bin LC_ALL=C /usr/bin/python3 -E -s -B /root/wi025-v2/scripts/deployment/production-bootstrap.py preflight --root /root/wi025-v2
```

During the next approved bounded native preflight, before apply, MA must verify
this SELECT-only truth table and the actual global predicate using the reviewed
socket-client safeguards. This SQL is recorded for review only, NOT RUN by SE:

```sql
SELECT COALESCE(NULL,'')='', COALESCE('','')='',
       COALESCE('127.0.0.1','')='', COALESCE('::1','')='',
       COALESCE('example.invalid','')='';
SELECT COALESCE(@@global.admin_address,'')='';
```

Expected truth table:1,1,0,0,0. The actual predicate must return1 and independent
listener checks must still pass. Any discrepancy is HOLD, not permission to
change configuration. Any subsequently authorized apply must also use the direct
`/root/wi025-v2/scripts/deployment/production-bootstrap.py` path and
`--root /root/wi025-v2`, after a matching new approval and all original gates.
Preserve artifacts and stop/re-mask on failure; no automatic retry or deletion.
Native semantics, TCP authentication, canonical manifest and startup remain
unverified. Next owner: WI027 PG, then MA; WI028 integrates actual receipts.

## 7. Exact Built-in Classification Addendum (2026-10-10)

This approved second correction supersedes the prior blanket ACTIVE AUDIT
rejection and the v2 staging instructions only. All original history and hashes
remain preserved. The updated MA handoff reports native MySQL8.0.46 ACTIVE
AUDIT entries `sha2_cache_cleaner` and `mysqlx_cache_cleaner`, both with SQL NULL
PLUGIN_LIBRARY. PG's pinned-source advisory supports this exact classification;
SE did not independently inspect the server or vendor source in this correction.

The only new helper change is the plugin count predicate. Within ACTIVE AUDIT,
only those two byte-exact names with `PLUGIN_LIBRARY IS NULL` are exempt.
`BINARY PLUGIN_NAME` prevents case/trailing-space aliases from gaining the
exception; COALESCE treats an unknown NULL name as unapproved. Every other
ACTIVE AUDIT entry remains rejected, including other built-ins. Either known
name with any non-NULL library, even an empty string, remains rejected.
ACTIVE `mysqlx` and `group_replication` remain independently rejected regardless
of type/library. Component, listener, recording and all other gates are unchanged.
NULL-library metadata is a classification signal, not binary integrity proof.

Local verification:37 tests PASS and offline plan PASS on Python3.14.3.
The exact-query contract is paired with37 synthetic classification cases;
negative/ambiguous responses and native-client error handling remain fail-closed.
These are mock/oracle checks, not native SQL evaluation. No real SQL, entropy,
remote staging, service changes or secrets were accessed by SE.

Final candidate helper SHA-256:
`7333b068d0264045b8130519afc6e0f62589912ba2ef0b31772ff84033960a1b`.
Final candidate bundle SHA-256:
`b91766934c522b1c4549b8a3057f95eae94c9a542e107fb9c7c39d7df3b24b9c`.
WI027 must independently re-review these exact bytes before MA resumes.

Preserve `/root/wi025` and `/root/wi025-v2` unchanged. MA must use a new real
`/root/wi025-v3`, a newly pinned archive, direct helper path and matching root;
never overwrite either old stage or reuse its approval digest. Future MA
preparation commands, NOT RUN here, after the original staging/review gates:

```bash
env -i HOME=/root PATH=/usr/sbin:/usr/bin:/sbin:/bin LC_ALL=C /usr/bin/python3 -E -s -B /root/wi025-v3/scripts/deployment/production-bootstrap.py plan --root /root/wi025-v3
env -i HOME=/root PATH=/usr/sbin:/usr/bin:/sbin:/bin LC_ALL=C /usr/bin/python3 -E -s -B /root/wi025-v3/scripts/deployment/production-bootstrap.py preflight --root /root/wi025-v3
```

Any separately authorized apply must likewise use that direct v3 helper and
`--root /root/wi025-v3`. Native classification and all target checks must pass
before apply; SQL errors, unexpected results or unapproved plugins mean HOLD,
not uninstalling plugins or changing MySQL globals to make the check pass.
Preserve artifacts and original failure cleanup rules. Production HOLD and
app_runnable=false remain; WI028 integrates actual native/cleanup receipts.
