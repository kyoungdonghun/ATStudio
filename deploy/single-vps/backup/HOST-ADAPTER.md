---
version: 1.0
last_updated: 2026-10-26
project: ATS
owner: SE
category: guide
status: confirmed
dependencies:
  - path: ../../../docs/registry/git-checkpoint-20261106.md
    reason: Approved local cold adapter implementation
---
# Cold-preserve host adapter (WI-20261026-ATS-019)

This is an implemented local candidate, not an installed or native-validated
adapter. Default `python -B scripts/deployment/backup/host_adapter.py` is inert.
The executable hook interface is `host_adapter.py ACTION CONFIG RUN_DIRECTORY`;
the generic capture calls it under its existing operation lock. No user-specific
host, address, secret or schema name is embedded in source.

## Supported lifecycle

Set `maintenance_mode` explicitly to `cold-preserve` in exact-approved config.
Default absent mode remains the original `live-resume` contract; this adapter
rejects live mode. Existing `resume_authorized` means permission to finish the
selected lifecycle: cold mode stops only its owned transient DB and preserves
the original cold baseline. It never starts an app or reopens public admission.
The final generic receipt says VERIFIED_AND_PRESERVED_COLD, while the unchanged
outer COMPLETE receipt still states restore/copy NOT_RUN.

The retained successful WI008 candidate source supplied the exact historical
unit allowlist and masked subset, cold MySQL flags, uniquely owned transient
unit technique, mount identity check, full-table/count queries and deterministic
dump strategy. Original paired files were read as source only and not modified.
Private target constants and original authorization/secret bodies were not copied.

`cold_host` config contains:
- Machine-id and boot-id SHA256; exact baseline unit properties (12 historical
  units); reviewed quarantine file path/hash.
- Artifact pin map covering systemctl, systemd-run, mysqld, mysql, mysqldump and
  reviewed MySQL config, plus any other reviewed release/config artifacts.
- Safe absolute datadir/MySQL config paths; schema identifier and sorted exact
  list of **43** base InnoDB table names.
- Approval-pinned evidence references described below.

Preflight/quiesce observe exact original units, masked writers, nginx baseline,
absence of known writer processes and unmanaged DB socket, plus artifact/host
identity. Fence writes the durable owned-unit claim before systemd-run. It starts
only a uniquely named MySQL source unit with private networking, AF_UNIX only,
event scheduler disabled, no replication/binlog, read_only/super_read_only,
bounded memory/lifetime and a private runtime socket. It verifies actual server
flags and source datadir inode through /proc/PID/root.

Dump observes every table and exact COUNT before/after, includes routines/events/
triggers, checks all CREATE TABLE and one INSERT per row, then validation compares
the entire deterministic second dump byte-for-byte. This is capture completeness,
not an isolated restore. The adapter deliberately caps `dump_bytes` at **16MiB**
because it holds bounded dump buffers; the generic protocol's larger cap is not
supported by this adapter. Exceeding it blocks before execution.

Cold success and any failure preserve original services/files. Cleanup validates
the owned claim and current machine/boot before stopping the unique transient unit,
then verifies zero PIDs, absent owned runtime and original baseline. It never
stops a baseline unit. Generic failure cleanup bypasses the expired capture
monitor using this separately bounded owned-unit-only path. Uncertain cleanup is
reported as COLD_CLEANUP_UNCERTAIN. SIGKILL/power loss still require native cgroup
containment and RuntimeMaxSec recovery; local tests do not prove those semantics.

## External proof contract: no fabricated authority

The adapter itself implements verify_host_receipts; it does not depend on WI020
status transfer. Four protected JSON files are pinned by path and SHA256 in
`cold_host.evidence`: repository_acl, recovery_access, source_inventory and cost.
Every file requires version1, matching kind, repository/machine/boot identity,
source_evidence_sha256, observed_at, expires_at and subject_sha256. The subject is
canonical config excluding **only** cold_host.evidence (avoids a circular hash);
outer exact config approval binds the evidence reference map too.

The proof horizon must cover the full configured execution window, no more than
six hours after observation. Proofs are checked at preflight and immediately
before DB creation. The adapter requires the parent operation flock already held;
direct hook calls without the lock cannot start a DB. The contention check proves
a lock is held, not its PID. The supported entry is the trusted generic executor
that already owns it; malicious root invoking hooks under another owner is outside
this trust model. Linux flock behavior remains a native validation dependency.

Required payloads:
- repository_acl: access=PRIVATE_VERIFIED.
- recovery_access: operation=EXACT_SNAPSHOT_READ_VERIFIED and exact snapshot_id.
- source_inventory: members_sha256 equal to newly collected local source inventory.
- cost: known integer upper_bound_krw/approved_limit_krw matching config, conservative
  new_bytes_bound at least source+dump+metadata limits, scope=ONE_COLD_CAPTURE and
  unknown_components=[]; fractional cost bounds must have been rounded upward.

These files must be independently reviewed source evidence under the protected
approval custody. The source_evidence_sha256 is a provenance pin, **not a proof
by itself**. Changing strings or generating synthetic receipts does not establish
remote ACL, credential recoverability or tariff truth. No receipt issuer, remote
inspection or authority was created by this work. Existing native receipts need
bounded reconciliation into this contract under future approved preparation;
their historical success is not silently rewritten into new native readiness.

## Native limits and tests

Nine new tests use synthetic local files and fake Linux commands, including an
actual generic Capture→ColdHost→fake restic→cold-preserved final receipt and the
same path failing mid-backup with emergency cleanup. External proof parsing/pins
are tested separately with synthetic files; Linux flock/ACL and inventory are
injected seams. Twenty existing protocol/capture tests pass unchanged default-live
behavior. No real service, DB, command, credentials, HTTP, mail or Drive was used.

Quarantine observation currently means pinned reviewed file + exact active nginx
service + no application writer. It does not independently probe HTTP503 or prove
nginx's effective loaded configuration. Linux systemd properties, mount/socket
behavior, mysql dump syntax on the target and actual independent evidence custody
still require bounded native validation before timer activation. No broader live
admission/resume workflow or general-purpose server adapter is claimed.

## Public checkpoint evidence boundary

This public copy redirects unavailable local evidence links to the shared
checkpoint boundary. The original document bytes and detailed REQ/WI evidence
remain in private local custody. No runtime contract or source code changed.
