---
version: 1.1
last_updated: 2026-11-06
project: ATS
owner: SE
category: guide
status: local-candidate-only
dependencies:
  - path: ../../../docs/registry/git-checkpoint-20261106.md
    reason: Approved local implementation only
  - path: ONLINE.md
    reason: Existing capture protocol and native prerequisites
  - path: PHASE-B.md
    reason: Trial ownership is deliberately not reused
---
# Bounded capture attached to an existing public service

`live_service_capture.py` is a local candidate, not native acceptance or execution authority.
Default invocation prints DISABLED and performs no service operation. It has no service
start/stop/restart API. The existing PHASE-B wrapper remains unchanged and must not be
used as an always-on maintenance runner.

## Exact boundary

A separately approved native run attaches to two already active source units, app and DB.
It uses only fixed `systemctl show` properties for source observations. Expected unit,
PID, start ticks, invocation ID, cgroup, executable hash, command-line hash, unit fragment
hash, UID/GID and network namespace inode must match before and after capture. The app
must run the exact pinned `-jar` in the approved release. No command-line contents or
private input bodies are printed. RuntimeMax must be infinity; source PartOf/BindsTo
relationships are conservatively rejected. A trial unit or unresolved dependency needs
separate preparation, not an implicit exemption.

The caller must already run in its own bounded helper cgroup, disjoint from source
cgroups. Existing online execution validates inherited cgroup memory/CPU/nice and disk,
inode, time, transfer and cost bounds. No unit is created or managed here. The root-trusted
source-only bundle, Python executable and five modules are pinned; ambiguous .pyc caches
are rejected. Parent/paths, ownership and private modes remain native checks.

The adapter invokes existing `online_capture.execute` exactly once. That executor retains
its operations lock, fresh target/repository/recovery/cumulative-cost proofs, private
mailbox FREEZE/dump/SEAL/immutable pins/full-content verification/RELEASE and owned-reader
cleanup. The adapter itself only owns synchronous read-only show subprocesses; their
10-second timeout cleanup cannot stop the source services. It does not weaken the
capture protocol or issue any proof. Source identity is observed before/after, not
continuously monitored; matching observations do not prove uninterrupted availability.

## Required private inputs

Use `live-service-inputs.example.json` only as a structural guide. It is disabled and
contains synthetic values, false assertions and no authority. It is not a CLI input file.
Prepare separate root-owned private files only under a concrete future approval:

- Existing online configuration and fresh exact online-capture approval. The table set
  must contain `site_settings`, `admin_operation_audit_logs` and every other actual table;
  the existing executor checks the complete InnoDB inventory before/after dump.
- Attachment: exact online config/approval hashes, source identities, app JAR and systemctl
  hashes, code-pins digest and runtime-proof reference/digest.
- Runtime proof from independently observed evidence: exact run/host/boot/services/subject,
  source-evidence digest, observation no older than ten minutes before run start and expiry
  covering the whole run. It binds effective DATABASE catalog, listening+audio-worker,
  audio-only background mode, private capture mailbox, no bootstrap/capture-control-only
  trial mode, sole writer, catalog/worker admission, dump coverage and accepted reader impact.
  These booleans are evidence requirements, not facts the tool discovers or manufactures.
- Outer authority: `live-service-capture`, exact attachment hash, same run/window as inner
  approval, `max_attempts:1`, `service_control_authorized:false`. Window at most30 minutes.
- Code pins: exact absolute paths of the five named modules and resolved Python binary.

The stable dedicated ledger is `<online state_dir>/live-service-ledger`, prepared as
root-owned0700. It cannot be relocated per attempt to bypass a HOLD. Every capture gets
a new run and fresh approvals/proofs; the inner proof binds cumulative cost/prior state.
No timer, recurring policy, mail, credentials, actual IDs or provider settings are supplied.

Command shape, **not executed here**; run only inside the separately prepared bounded
helper with approved private inputs:

```sh
/usr/bin/python3 -B /approved/code/live_service_capture.py --execute   --config /private/online.json --online-approval /private/online-approval.json   --attachment /private/attachment.json --authority /private/attachment-authority.json   --pins /private/code-pins.json
```

## Failure and reconciliation

A durable exclusive claim precedes source observation/capture. Missing pre-observation,
post-observation failure or PID drift, expired authority during capture, changed code,
unknown reply or failed cleanup retains the attempt and `attachment.lock`, with a HOLD
record. Even an inner COMPLETE is not outer service-continuity acceptance when the source
check fails. Pre-claim validation rejection has no consumed attempt; inspect the ledger
rather than inferring this from a console message. Failed evidence is never replaced.

There is deliberately no automatic outer reconciliation, force unlock or retry command.
The existing online reconcile operation applies only to its inner protocol. Its success
**does not authorize deleting the outer lock/claim**. A separate exact reconciliation
scope is required: bind original claim/HOLD bytes and inner run/capture state, prove all
original readers ended on the exact boot, reconcile uncertain transfer/release through the
existing inner protocol, and inspect source identity/availability plus the reason for any
change. Preserve original files and establish an append-only reviewed disposition before
any explicitly authorized future ledger recovery. This adapter does not implement that
outer recovery action; an outer HOLD blocks later adapter runs. Do not create a fresh
ledger or edit COMPLETE to bypass it. This conservative recovery limitation is a native
operating gate, not a successful recovery claim.

The [2026-11-06 outer recovery design](OUTER-HOLD-RECOVERY.md) specifies proposed
append-only dispositions, common serialization and compatible ledger-reader changes.
It passed independent local design review, is not implemented and supplies no native
authority. Unsupported legacy evidence,
post-SEAL transfer uncertainty and cross-boot inner recovery remain HOLD.

## Operational limits

FREEZE can temporarily reject upload/edit/publication writes; SEAL resumes admitted writes
while captured immutable media stays pinned. The catalog row and audit must be covered by
the same DB snapshot, and Java admission integration must be independently accepted.
Listening can contend for CPU/I/O/DB resources. No zero-impact, no-downtime or unlimited
concurrent-write promise is made. In-flight streams and saved client copies remain outside
publication withdrawal. Successful capture keeps `restore_verified:false`; isolated restore
has its own target/authority. This capture adapter does not stop the public source.
The current `online_restore.py` separately **requires source app/DB stopped** and rejects
Java/MySQL/FFmpeg processes; it cannot be run against an active public source on the same
host. The [next-server proposal: retained local evidence](../../../docs/registry/git-checkpoint-20261106.md#retained-evidence-limits)
uses an explicitly approved stop/restore/restart window before public opening. A separate
target or a maintenance window for an already public service needs its own concrete scope.

Local tests inject observations/capture and check failure/replay/cleanup boundaries. They
are not Linux systemd/procfs/cgroup, real MySQL/collation, provider, audio/device, current
catalog recovery or physical-failure proof. Native acceptance must test source identity
through capture and failure, actual reader/freeze impact, helper termination, exact settings,
capacity/cumulative cost, and compatible isolated restoration before public operation.

## Public checkpoint evidence boundary

This public copy redirects unavailable local evidence links to the shared
checkpoint boundary. The original document bytes and detailed REQ/WI evidence
remain in private local custody. No runtime contract or source code changed.
