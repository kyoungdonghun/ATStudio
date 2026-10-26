---
version: 1.0
last_updated: 2026-10-10
project: ATS
owner: SE
category: guide
status: draft
dependencies:
  - path: ../../../deliverables/user/REQ-20261010-ATS-009.md
    reason: Local preparation and separately approved sensitive execution
  - path: ../../../deliverables/agent/WI-20261010-ATS-029-evidence-pack.md
    reason: Exact candidate identity and local verification
  - path: production-bootstrap.md
    reason: Preserved bootstrap guards and cleanup requirements
---

# Read-Only JDBC Readiness

> Purpose: Check the existing production account with the exact WI026 driver,
> without starting Spring, changing secrets or altering database data.

## 1. Status and Local Reproduction

TL;DR: local preparation only. Native execution belongs exclusively to MA after
WI031 PG PASS for the exact bytes. The user subsequently approved this window's
temporary agent and confined existing-secret use; this does not waive PG or
operator guards. SE performed no native operation.

```powershell
python -B -m unittest discover -s scripts/deployment -p test_jdbc_readiness.py -v
python -B scripts/deployment/jdbc-readiness.py plan --output build/WI029-jdbc-v1
python -B scripts/deployment/jdbc-readiness.py smoke --output build/WI029-jdbc-v1
```

Preparation already ran once with `prepare --output build/WI029-jdbc-v1`.
Never overwrite that directory. A changed candidate requires a new unused
`build/WI029-*` directory, new hashes and fresh PG review. Tests generate only
public synthetic fixtures in new `build/WI029-tests-*` directories. They use
an in-memory fake JDBC driver, never a socket or actual env file.

## 2. Exact Staging and MA-Only Gates

MA must stage the following six reviewed public files under a fresh
`/root/wi029`, preserving these relative names. No symlinks, hardlinks,
non-root owners or group/other-writable ancestors/files are accepted. Do not
replace an existing application release. Preserve all WI025 stages and receipts.

```text
scripts/deployment/jdbc-readiness.py
scripts/deployment/RuntimeJdbcProbe.java
build/libs/ATStudio-0.0.1-SNAPSHOT.jar
build/WI029-jdbc-v1/artifact.json
build/WI029-jdbc-v1/classes/RuntimeJdbcProbe.class
build/WI029-jdbc-v1/mysql-connector-j-9.7.0.jar
```

Classpath is **only** the `classes` directory and the extracted Connector/J JAR,
separated by `:` on Linux. The application JAR is a hash input, never executed
or included on the classpath. No wildcard, Hikari, Spring or application class
is loaded. The OS-managed `/usr/bin/java` alternatives chain must be root-owned
with trusted ancestry and resolve to Java 17; there is no install fallback.

Before reading secrets, MA must independently confirm and record bounded facts:

1. Strict-pinned SSH, same approved boot/host, explicit current-window approval,
   exclusive maintenance, no new paid resource or external Provider access.
2. No sudo/session I/O recording, shell tracing, debugger, process/pipe capture,
   instrumentation wrapper or external log forwarding that could capture secret
   material. An unknown capture surface is HOLD, not permission to disable it.
3. App and Nginx remain absent/masked inactive; no Java/FFmpeg/app writers and no
   public app listeners. Start MySQL only temporarily on the approved loopback.
4. Rehash immutable WI025 v3 helper
   `7333b068d0264045b8130519afc6e0f62589912ba2ef0b31772ff84033960a1b`
   and its reviewed dependencies. Selectively reuse only `host_guard()`,
   `database_guard(expected_server_hash, fresh=False)` and
   `verify_grants(locked=False)` from that trusted stage. These check the exact
   server, loopback listener, capture/plugin/component/replication restrictions,
   other clients and original four nongrantable DML grants. **Never call old
   `native_guard`, `preflight`, `apply`, `mutate` or secret generation.**
5. Confirm recovery access and a finally-style stop/remask and agent cleanup
   procedure, including on probe failure, interruption or SSH loss.

These external operator checks are mandatory attestations, not checks performed
by the new wrapper. PG must review their execution sequence as well as code.

## 3. Approval and Invocation

MA creates a fresh root0600 single-link
`/etc/atstudio/jdbc-readiness-approval.json` under the existing root0700 directory
after guards and PG PASS. Do not reuse/overwrite WI025 approval or receipts.
All fields below are required; timestamps are UTC, validity at most 30 minutes,
and booleans must be JSON `true`, not strings or integers. Host/server hashes
stay in the server-side approval and need not appear in shared reports.

```json
{
  "req": "REQ-20261010-ATS-009",
  "review_wi": "WI-20261010-ATS-031",
  "bundle_sha256": "6c1ad23065f89bc0776cb628322941b166dc82dbf4f56ee15c052aa2a1860199",
  "machine_id_sha256": "<SHA-256 of stripped existing /etc/machine-id>",
  "server_uuid_sha256": "<SHA2 of the approved existing MySQL server_uuid>",
  "issued_at": "<YYYY-MM-DDTHH:MM:SSZ>",
  "expires_at": "<YYYY-MM-DDTHH:MM:SSZ>",
  "operator_checks": {
    "explicit_user_secret_use": true,
    "pinned_ssh_same_boot": true,
    "exclusive_maintenance": true,
    "no_session_io_recording": true,
    "no_debugger_or_process_capture": true,
    "no_sql_or_external_capture": true,
    "mysql_guards_rechecked": true,
    "mysql_temporarily_loopback": true,
    "app_proxy_workers_fenced": true,
    "stop_remask_on_exit": true,
    "exact_key_agent_cleanup": true
  }
}
```

In the approved root session, use a trusted root-owned working directory and
Python `-I -B` (not `-E -s`). Run the public-only `plan` first and compare its
bundle hash with this exact reviewed value; then invoke `probe` once:

```sh
cd /root/wi029
/usr/bin/env -i PATH=/usr/bin:/bin HOME=/root LC_ALL=C /usr/bin/python3 -I -B /root/wi029/scripts/deployment/jdbc-readiness.py plan --root /root/wi029 --output build/WI029-jdbc-v1
/usr/bin/env -i PATH=/usr/bin:/bin HOME=/root LC_ALL=C /usr/bin/python3 -I -B /root/wi029/scripts/deployment/jdbc-readiness.py probe --root /root/wi029 --output build/WI029-jdbc-v1
```

Do not execute the Java class directly. Do not source/cat/hash/export the private
env, send it to the PC, or put credentials in arguments, environment, SQL,
temporary files or logs. Never run this through a generic Gradle/Spring launcher.

## 4. Results and Cleanup

Only `status=PASS` and exit0 establish this bounded JDBC check. PASS means the
exact account/schema/server matched, server session read-only was verified,
the 43 known tables were visible, all non-plan tables were empty and six active
plan key pairs existed. It does **not** compare every plan price/column/index,
validate Hibernate, update the canonical manifest or approve full app startup.

Application SELECTs are fixed; table identifiers come only from the compiled
43-name list. JDBC additionally performs protocol/session setup and transaction
control (`setReadOnly`, `setAutoCommit`, rollback, close); this is not a claim
that every wire operation is literally SELECT. No DDL/DML is issued. Connection
and socket bounds are5s, statement timeout3s, child deadline45s. Statement
timeout aborts the connection, rather than opening another cancellation session.

The URL is byte-exact with the generated production URL. No TLS/authentication
relaxation or `allowPublicKeyRetrieval` is added. A TLS/auth/network error remains
`JDBC_CONNECT_FAILED`: no retry, raw error inspection or automatic correction.
Duplicate env keys, non-generated quoting and connection-property additions
fail closed. Other generated env values are discarded, not applied or audited.

Driver/JUL/stdout/stderr logging is suppressed; only allowlisted final statuses
are accepted. Parent captures at most129 stdout bytes and discards stderr.
Core limits are inherited; JVM heap/core/error-file/performance/attach output
is disabled. Python is nondumpable, but Linux exec resets that flag for Java;
this is **not** protection against privileged memory inspection or a full
zeroization claim. Strings and the driver may retain password copies in memory
until process exit. Nothing is persisted by the native probe.

For every outcome, MA attempts MySQL stop **and independently attempts mask even
when stop fails**. Do not chain mask behind stop success or let an early error
skip cleanup. Independently verify stop result/inactive state, exact mask,
no pending job and no3306/33060/33062 listener; each failed/unknown check is HOLD.
MA keeps app/Nginx/public listeners fenced, closes
the session, removes only the approved SSH key, confirms the empty agent and
restores Stopped/Disabled. Failed cleanup means the operational window is still
open. WI032 records actual probe and cleanup receipts without rewriting WI028.

## Related Documents

- [WI029 evidence](../../../deliverables/agent/WI-20261010-ATS-029-evidence-pack.md): Hashes and tests.
- [WI028 evidence](../../../deliverables/agent/WI-20261010-ATS-028-evidence-pack.md): Previous actual state, not this window's proof.
- [Connector/J performance properties](https://dev.mysql.com/doc/connector-j/en/connector-j-connp-props-performance-extensions.html): Read-only propagation; checked2026-10-10.
- [Connector/J logging properties](https://dev.mysql.com/doc/connector-j/en/connector-j-connp-props-debugging-profiling.html): Default stderr logger and instrumentation controls; checked2026-10-10.
- [Connector/J statement properties](https://dev.mysql.com/doc/connector-j/en/connector-j-connp-props-statements.html): Timeout connection-abort behavior; checked2026-10-10.
