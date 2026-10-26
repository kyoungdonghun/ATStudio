---
version: 1.3
last_updated: 2026-10-11
project: ATS
owner: SE
category: work-summary
status: draft
dependencies:
  - path: ../agent/WI-20261011-ATS-066-handoff.md
    reason: Assigned isolated preparation scope
  - path: ../agent/WI-20261011-ATS-066-evidence-pack.md
    reason: Exact manifest, commands, tests, and remaining execution gate
---

# WI066 Launcher Readiness

> Purpose: Report the first authorized attempt and the next prepared candidate without automatic retry.

## Current Result

**TL;DR: Attempt 4 identified host rejection (`JDBC_CONNECT / SQLException / HY000 / 1130`); zero of six transaction cases ran.** All four attempts are preserved. Independent checks confirmed attempt 4's PIDs 2712/50108/31916 exited or absent and no matching IPv4/IPv6 listener or port 60674 listener remained. Native release stays false; this is containment cleanup, not proven graceful SQL shutdown.

The cause is the fresh `root@localhost` account versus IPv4 TCP with name resolution disabled. MA-approved local preparation now supplies an exact fresh-instance init file creating only `root@127.0.0.1` with explicit test privileges. No existing account/password change, wildcard host, authentication bypass, or existing DB/service access is introduced. The SQL is pinned and copied only into a new owned run directory. All identity, version, bind, and process-containment gates remain.

Ten Python tests, Java compilation, and the no-database self-test pass. Current [prepared-loopback-account manifest](../../build/WI066-mysql/prepared-loopback-account/manifest.json):

```text
fdecb0915b6b7974db5cd6dde3e65553e075aae83a14dd44b0b99e8e15941e4d
```

The bootstrap has not run. MA reviews the exact SQL and full hash before issuing a fresh one-use record/run ID. The [current Evidence Pack checkpoint](../agent/WI-20261011-ATS-066-evidence-pack.md#current-checkpoint-attempt-4-host-rejection) preserves diagnostics, cleanup, SQL, and the next command.

## Historical Attempt 2

**TL;DR: Attempt 2 HOLD, zero of six cases executed, containment cleanup confirmed.** The monitor fix passed the launcher's PID gate and Java started. The [actual failure proof](../../build/WI066-mysql/run-042921baab3b4f939b29273404939f71/proof.json) records `cases:0` and `mysql_version:NOT_VERIFIED`. The old harness discarded the exception, so the exact failed startup/identity check remains unknown; no predicate was weakened based on a guess.

Initializer 43504, server 46768, and Java 10556 were independently confirmed exited or absent; no matching IPv4/IPv6 listener or port 56517 listener remained. This was owned Windows Job cleanup, not proven graceful SQL SHUTDOWN. Both consumed attempts are preserved. `NATIVE_RELEASED=false` remains unchanged.

The fresh diagnostic-only candidate records a fixed stage and exception class without raw messages or DSNs, and includes a failed Java proof in the launcher result. Nine offline Python tests, Java compilation, and the no-database self-test pass. Current [prepared-diagnostics manifest](../../build/WI066-mysql/prepared-diagnostics/manifest.json):

```text
a97873edb843c91b27ab683da734dbe4350e21765f0eadcf491daf1486703a5e
```

No retry occurred at this historical checkpoint. MA review of that hash and a fresh one-run record were required. The [historical Evidence Pack checkpoint](../agent/WI-20261011-ATS-066-evidence-pack.md#historical-checkpoint-attempt-2-hold) preserves the then-current command and evidence.

## Historical Attempt 1

**TL;DR: Attempt 1 HOLD; all six database cases NOT RUN; owned processes stopped.** Initialization succeeded, but Windows MySQL launched a monitor PID (50872) and a different server PID (12016). The exact owned-PID checks blocked JDBC before the Java harness started. The [retained result](../../build/WI066-mysql/run-89d252688576471bb5fb95fba34d3621/result.json) records `proof:null`. Independent postchecks confirmed initializer 53704, monitor 50872, and server 12016 exited or absent, with no matching IPv4/IPv6 listener or remaining port 60714 listener.

The first attempt and previous bundles are preserved. The missing `--no-monitor` option is now added after `--no-defaults`; strict PID checks remain. Eight offline tests, compilation, and the no-database Java self-test pass. The current [prepared-monitor-fix manifest](../../build/WI066-mysql/prepared-monitor-fix/manifest.json) has SHA-256:

```text
c0227a69c606c7eae5c028080eeb74a21f7ac30c6eda0a79427f36ccee51c6e3
```

**No retry was performed at this historical checkpoint.** MA review of a new hash/change and a fresh run ID was required. Existing databases/services and production sources remained untouched; `NATIVE_RELEASED=false`. The [historical Evidence Pack checkpoint](../agent/WI-20261011-ATS-066-evidence-pack.md#historical-checkpoint-attempt-1-hold) preserves the logs, hashes, and then-proposed next command.

The following sections preserve the earlier readiness checkpoint and are historical.

## Historical Readiness Summary

**TL;DR: LAUNCHER_READY_NOT_RUN.** Seven offline tests, Java compilation, and the no-database harness self-test pass. The final bundle is [prepared-reviewed](../../build/WI066-mysql/prepared-reviewed/manifest.json), with manifest SHA-256:

```text
5ec8e3248698f2fc7fda7f843c3bd8250d04ecde78475466edd3f8ecedb01855
```

Verification: 2026-10-11, SE, local Windows preparation. No mysqld start, initialization, JDBC connection, database change, service operation, or deletion was performed. Application sources and frozen WI063 sources are unchanged; `NATIVE_RELEASED` remains false.

## Review Boundaries

- Both MySQL commands place `--no-defaults` first after the executable. Future data/log paths are fresh and isolated under `build/WI066-mysql`.
- Independent Windows IPv4 and IPv6 listener queries are implemented and read-only API checks pass. Before JDBC, the owned server must expose exactly `127.0.0.1:<fresh-high-port>`; alternate owned listeners reject the run. Post-stop checks cover both address families.
- Hidden processes use exact executable pins and a kill-on-close Windows Job. Actual process startup, cleanup, and the six database cases are still **NOT RUN**.
- Initialization deliberately creates a temporary passwordless synthetic root. Remote exposure is constrained by loopback and disabled alternate protocols, but another local process can connect during the bounded test window. There is no per-user TCP isolation; MA must accept this risk before execution.
- The installed MySQL is 8.0.45; native requires 8.0.46. The frozen predicate stays intact. A declared test-only adapter handles three identity reads so the real candidate lock/DML/commit/rollback code can be tested. Future success would be transaction-only evidence, not native acceptance.

## Next Gate

MA reviews the final manifest and launcher, explicitly accepts the adapter and local passwordless-root risk, and supplies an expiring one-run review record plus direct execution authorization. The [Evidence Pack](../agent/WI-20261011-ATS-066-evidence-pack.md#exact-next-executable-gate) contains the exact record shape and next command. SE has created no live authorization and is waiting; no disposable proof has run.

## Related Documents

- [WI066 Evidence Pack](../agent/WI-20261011-ATS-066-evidence-pack.md): Exact hashes, checks, commands, and limits.
- [WI066 Handoff](../agent/WI-20261011-ATS-066-handoff.md): Assigned scope and MA process-start gate.
