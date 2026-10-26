---
version: 1.2
last_updated: 2026-10-11
project: ATS
owner: SE
category: guide
status: draft
dependencies:
  - path: ../../../deliverables/user/REQ-20261011-ATS-001.md
    reason: Approved preparation and separately gated native execution
  - path: ../../../deliverables/agent/WI-20261011-ATS-011-evidence-pack.md
    reason: Candidate identity and local-only test evidence
  - path: jdbc-readiness.md
    reason: Reused private-path, credential framing and process safeguards
---

# Release and Isolated Schema Readiness

> Purpose: Compare WI026 public artifacts and validate entity mappings without
> starting Spring or changing the existing database.

## 1. Local Candidate

TL;DR: WI011 corrects the exact packaged-configuration line rejected by v4.
Native retry is not authorized by this local correction. It requires WI012 PG
PASS, fresh access/approval and a separately reviewed exact MA lifecycle procedure.
MA reports the v4 operator stopped at ENV_KEYS before childprobe or approval
creation, then stopped/masked MySQL and restored key/agent state. This is
attributed operator evidence, not an SE native observation or schema result.
Preserve v3/v4, their operators and WI006/007/009/010 evidence. No startup claim.

```powershell
python -B -m unittest discover -s scripts/deployment -p test_release_readiness.py -v
python -B -m unittest discover -s scripts/deployment -p test_jdbc_readiness.py -v
python -B scripts/deployment/release-readiness.py check
python -B scripts/deployment/release-readiness.py plan --output build/WI011-schema-v5
python -B scripts/deployment/release-readiness.py smoke --output build/WI011-schema-v5
```

The fresh candidate is `build/WI011-schema-v5`; its exact bundle SHA-256 is
recorded in [WI011 evidence](../../../deliverables/agent/WI-20261011-ATS-011-evidence-pack.md).
Never overwrite it. Changes require a fresh unused output name and renewed PG review.
The candidate's `artifact.json` records checked input digests, compiler identity,
exact extracted files and class hashes. `load_plan()` returns `input_sha256` for
the exact public staging file list; the CLI emits only the aggregate bundle hash.
Stage those files preserving relative names under a new root-private directory.
No archive extraction or service change is performed by the wrapper.

The JAR is only a pinned input; it is never launched or put on the classpath.
Classpath is an explicit list of 30 existing nested libraries, selected model
classes and the two probe classes. No wildcard, app configuration, controllers,
repositories, application main, Hikari, logging backend, web server, schema/seed
resource or runtime Spring context is present. Four storage enums and two
exception types are model dependencies, not application services. There are no
AttributeConverters in this source baseline. Any future mapping/input change
requires a new review. Spring audit listener annotations are resolved using
existing Spring Data/aspects libraries; listeners are not instantiated or run.

The bundled Boot4.0.8 `HibernateProperties$Naming` bytecode establishes
`SpringImplicitNamingStrategy` and Hibernate's
`PhysicalNamingStrategySnakeCaseImpl`. Hibernate7.2.24 metadata uses these exact
classes, explicit MySQL8.0 dialect settings and the verified production catalog.
The Jakarta database-major/minor-version settings are strings (`"8"`, `"0"`),
as required by this bundled validator's `resolveJdbcContext`. Regression uses
the real validator, matching synthetic metadata and missing-table/column cases,
with Connector/J absent and no settings overrides. It is not native proof.
JDBC metadata access during bootstrap, automatic DDL, validation callbacks and
archive autodetection are disabled. Only `SchemaValidator.doValidation()` is
called explicitly; no SessionFactory or EntityManagerFactory is built.

## 2. MA Preflight and Approval

1. Recheck exact pinned host/boot, exclusive maintenance, recovery access,
   approved current-window secret use, trusted interpreter/stage ancestry and
   absence of I/O recording, tracing, debugger/pipe capture and external SQL or
   process capture. Unknown capture state means HOLD. Do not weaken capture
   controls to proceed.
2. Preserve the active restricted TLS Nginx and Certbot timer. Independently
   verify the reviewed quarantine configuration hash, drop-in, restricted
   listeners and absence of upstream/app routes. Keep configuration bodies and
   actual allowline private. **Do not invoke the old `host_guard()`**, which
   requires Nginx masked/inactive. Do not stop TLS to satisfy it or modify it.
3. App remains absent/off; no Java, FFmpeg, workers, app listeners or pending
   jobs. Check MySQL loopback-only configuration and capture/plugin/replication
   restrictions before temporary activation. Arm cleanup with recovery access
   before `systemctl unmask mysql.service` and `systemctl start mysql.service`.
4. After startup, independently verify only the approved loopback MySQL listener.
   Reuse only `database_guard(expected_server_hash, fresh=False)` and
   `verify_grants(locked=False)` from the exact retained WI025-v3 helper
   (`7333b068d0264045b8130519afc6e0f62589912ba2ef0b31772ff84033960a1b`)
   and its reviewed dependencies. Never run old `host_guard`, `native_guard`,
   bootstrap/preflight/apply/mutate or secret generation. Obtain expected
   machine/server identity internally from retained root0600 single-link
   `/etc/atstudio/jdbc-readiness-approval.json`, after verifying provenance as
   the prior successful WI031-reviewed run: REQ009, review WI031 and bundle
   `6c1ad23065f89bc0776cb628322941b166dc82dbf4f56ee15c052aa2a1860199`.
   Its exact IDs are `REQ-20261010-ATS-009` / `WI-20261010-ATS-031`.
   Check its `machine_id_sha256` against current machine-id, and its
   `server_uuid_sha256` against MySQL, internally. The old expired approval is
   only historical identity evidence, never current execution authority.
   The retained `/etc/atstudio/production-bootstrap-approval.json` can supply
   an independent same-identity cross-check after its own provenance check.
   Missing/mismatched/untrusted historical evidence is HOLD; never adopt the
   currently observed UUID as the expected identity. The bootstrap receipt at
   `/etc/atstudio/production-bootstrap-receipt.json` has observations and public
   input hashes but **does not contain these identity hashes** (`safe_status`,
   `apply` in the original helper). Do not export any private file or hash.
5. Stage every `input_sha256` file with root ownership, no links, trusted
   non-writable ancestry and no extras in the candidate directory. Compare the
   public `plan` hash to WI011/WI012's exact reviewed hash. Confirm public baseline
   inputs again locally before transfer; remote `plan` verifies staged artifacts,
   not a remote source checkout or deployed SPA.
6. Create a fresh root0600 single-link approval at
   `/etc/atstudio/release-readiness-approval.json`, under existing root0700
   `/etc/atstudio`. Do not overwrite old approvals/receipts. Exact keys are
   `req`, `review_wi`, `bundle_sha256`, `machine_id_sha256`,
   `server_uuid_sha256`, `issued_at`, `expires_at`, `operator_checks`.
   Set REQ to `REQ-20261011-ATS-001`, review to `WI-20261011-ATS-012`, and
   bundle to the independently reviewed candidate hash. Host/server identities
   stay internal; timestamps are UTC `YYYY-MM-DDTHH:MM:SSZ`, validity <=30min.
   Every check below must be a literal JSON boolean `true`, never `1` or text:

```text
explicit_user_secret_use
pinned_ssh_same_boot
exclusive_maintenance
no_session_io_recording
no_debugger_or_process_capture
no_sql_or_external_capture
mysql_guards_rechecked
mysql_temporarily_loopback
app_proxy_workers_fenced
stop_remask_on_exit
exact_key_agent_cleanup
pg_wi012_exact_bundle_pass
restricted_tls_timer_unchanged
quarantine_has_no_app_upstream
no_mapping_overrides
```

`app_proxy_workers_fenced` means app routes/workers are fenced, **not** that
restricted Nginx is stopped. These are operator attestations, not automatic
host/firewall/capture verification. Approval parsing reuses WI029's strict
shape/time validator after checking the new REQ, review and extended checks;
the old approval cannot authorize this probe.
WI007 and WI010 are explicitly rejected; correcting only the bundle hash in an
old approval does not authorize v5. Preserve all prior approval files and evidence.

## 3. Native Invocation and Cleanup

After PG012 approval and all preceding checks, launch only through MA's newly
reviewed exact signal-aware lifecycle supervisor, from a fresh trusted stage.
No native operator is prepared or modified by WI011. The following future payload
must be bound to that separately reviewed procedure; it is not a retry instruction:

```sh
/usr/bin/env -i PATH=/usr/bin:/bin HOME=/root LC_ALL=C /usr/bin/python3 -I -B /root/wi011/scripts/deployment/release-readiness.py plan --root /root/wi011 --output build/WI011-schema-v5
/usr/bin/env -i PATH=/usr/bin:/bin HOME=/root LC_ALL=C /usr/bin/python3 -I -B /root/wi011/scripts/deployment/release-readiness.py probe --root /root/wi011 --output build/WI011-schema-v5
```

Do not launch Java directly. No private env argument, shell sourcing, cat/hash,
export, temp copy or logging. The original env parser enforces the exact DB URL
and account and returns only the password, which passes over the bounded pipe.
The wrapper additionally rejects JPA/Hibernate/config overrides except these
three exact double-quoted lines: `SPRING_JPA_HIBERNATE_DDL_AUTO="validate"`,
`SPRING_JPA_SHOW_SQL="false"`, `SPRING_CONFIG_LOCATION="classpath:/"`.
The last line is the existing [public packaged-only contract](../CONFIGURATION.md#2-paths-ownership-and-configuration-sources),
not an external location exemption; the probe does not load Spring configuration.
Other locations, additional-location/import keys, unknown restricted overrides,
duplicate keys and malformed grammar still fail closed. Full public bootstrap
rendering with synthetic secrets is covered locally, including v4 ENV_KEYS
reproduction. Other env settings are not loaded into Hibernate or fully audited;
full startup configuration remains a separate gate. Do not edit the production
environment to bypass a verification-tool failure.

Before MySQL unmask/start, MA must arm handlers for SIGINT, SIGTERM and SIGHUP,
an overall deadline and an independent recovery-console path. Launch the exact
wrapper into its own new session/process group and retain its exact PGID; Java
children inherit that group. On timeout or termination, signal the whole owned
group, bound the grace period, escalate to SIGKILL if necessary, and wait/reap
owned children (including orphan descendants through the supervisor/subreaper
where necessary). Confirm no group member or probe process remains. A parent-only
kill or ordinary try/finally with default SIGHUP/SIGTERM is insufficient. SIGKILL,
SSH loss or supervisor failure still require the independent console cleanup.
This wrapper and the old JDBC helper do not implement a lifecycle supervisor;
MA owns its separate implementation and PG review, not SE's local test result.

For every result including timeout, interruption and connection loss, attempt
`systemctl stop mysql.service` and **independently** attempt
`systemctl mask mysql.service`; bound each command separately, and failure or
timeout of stop must not skip mask. Cleanup failure remains HOLD. Confirm
inactive, exact persistent mask, no jobs and no3306/33060/33062 listeners. Recheck
restricted TLS/renewal timer unchanged, app absent and no probe/writer process.
Close sessions, remove only this window's owned key, verify empty agent, then
restore Stopped/Disabled. Unknown/failed cleanup is HOLD, not window closure.
No cleanup command, service activation, retry or remote action is built into the
probe itself. Preserve existing stages and historical evidence.

## 4. Meaning and Limits

`MAPPING_READY` is offline mapping bootstrap only, with a provider that rejects
every connection. `PASS`/exit0 proves only Hibernate validation plus equal
before/after target metadata snapshots within this bounded run. Every connection
is read-only with server-session read-only, account, database, server and version
checks before metadata use; at most four connection attempts, no pool/retry.
Connection/socket timeouts5s; explicit observation statement timeout3s;
whole child90s; heap256MiB; stdout<=256bytes. Connector/J timeout aborts its
connection instead of opening a cancellation session.
Hibernate may commit its already read-only metadata session before enabling
autocommit, then restore autocommit and rollback/close. The synthetic real-validator
regression observes this without removal of read-only. This is read-only data
access with JDBC transaction-control traffic, not a wire-level SELECT-only or
no-COMMIT promise; no schema/data DDL/DML is part of the probe.

The snapshot uses ordered tables, columns/defaults/types, index entries and
foreign-key columns, max4096 rows per projection and8192 UTF-8 bytes per field.
Only counts and a length-framed structural digest are emitted, never metadata
values, data rows, credentials, server hashes or exceptions. NULL and empty are
distinct. Hibernate's internal JDBC metadata calls are bounded by socket/child
limits; they do not share the explicit observation statement timeout.

Hibernate validate does not prove every default/index/FK/check rule, grant,
trigger, collation or data invariant. The digest is a **target snapshot**, not
equality with canonical fresh DDL or the old WI025 digest (different encoding and
projections). No fresh-schema DB or SQL execution occurred locally. Native
`canonical_fresh_manifest=UNRECORDED`, `app_runnable=false`, production HOLD
remain even on PASS. No migration, schema update or bootstrap replay on failure.
No public exposure, startup, recovery, backup, SMTP, ADMIN or capacity proof.

Driver/JUL and stdout/stderr are suppressed before Hibernate loads; only fixed
codes or the bounded PASS record survive. Core/heap/error dumps, attach and JVM
performance output are disabled, inherited env is cleared. This does not stop
privileged memory capture or guarantee credential zeroization; strings/driver
copies may remain in memory until exit. Python nondumpable does not survive exec
as a Java nondumpability guarantee. External capture preflight remains essential.

## Related Documents

- [WI011 evidence](../../../deliverables/agent/WI-20261011-ATS-011-evidence-pack.md): Exact v5 identity and public-environment regression.
- [WI011 summary](../../../deliverables/user/WI-20261011-ATS-011-summary.md): Local correction and WI012 handoff.
- [WI009 evidence](../../../deliverables/agent/WI-20261011-ATS-009-evidence-pack.md): Corrected candidate and regression.
- [WI009 summary](../../../deliverables/user/WI-20261011-ATS-009-summary.md): Handoff and exclusions.
- [WI007 review](../../../deliverables/agent/WI-20261011-ATS-007-evidence-pack.md): Preserved v3 HOLD and lifecycle conditions.
- [JDBC procedure](jdbc-readiness.md): Reused guards, with Nginx preflight superseded here.
- [Production readiness](production-readiness.md): Separate activation gates.
