---
version: 1.0
last_updated: 2026-11-06
project: ATS
owner: SE
category: guide
status: local-candidate-under-independent-review
---
# Local v2 outer HOLD candidate

WI-20261106-ATS-003 implements the accepted [design](OUTER-HOLD-RECOVERY.md).
The accepted design and historical evidence are unchanged. This guide describes
local source, not a deployed capability or native acceptance.

## Version and custody boundary

The Java mailbox protocol remains `ats-online-v1`. A new immutable online config
opts into the reviewed executor bundle with `recovery_gate.version=2`. Its
`online.module_sha256` must contain exactly `outer_recovery.MODULES` (eight files),
with current source hashes. `recovery_gate` also binds the exact Python and
systemctl binary hashes, one helper unit, and distinct `source_units.app` and
`source_units.db` units. The disabled fragment alongside this guide is not a
usable config, authority or deployment instruction.

All actual v1 recovery (including terminal/no-entry attempts without original
compatible config and custody records) and changed v2 code/config pins are
unsupported in this candidate. The design's conditional future outer-only legacy
qualification path is not implemented or authorized here. Do not
edit an old config or invoke a legacy binary to reconcile it. Native capture
requires the existing full runtime/config/approval/proof checks. The gate adds
checks; it does not waive them. The source bundle must have no executable Python
cache. Use `python -B` in a separately approved, frozen source-only candidate.

## Entry points and lock order

`outer_recovery.gate` owns the permanent `live-service-ledger/operation.guard`
inode and then the existing shared operations flock. It passes an opaque,
process/thread/config-bound in-memory capability to nested calls. The underlying
flock reports its descriptor's device/inode; both named lock identities remain
bound to the capability. No CLI skip-lock or force option exists.

Live capture holds both locks for observation, claim, inner capture, cleanup and
result. Direct inner capture makes its own v2 ledger attempt under the same gate.
Direct v2 reconciliation requires the outer disposition call graph. Restore
holds the gate and validates the ledger before preserving its existing isolated
restore checks. Online dispatcher holds the gate for its whole durable lifecycle
and passes its owner to the inner callback. A retained dispatch HOLD itself is
not cleared by an outer disposition. Cold `guarded_backup` rejects a state root
with the v2 permanent guard. Cold dispatcher/host adapter and legacy PHASE-B
entrypoints likewise reject a guarded state root before their mutating lifecycle;
PHASE-B cannot accept a v2 online config. Unmodified legacy executables remain outside this
trust boundary: deployment must exclude them and establish no legacy owner.

## Read-only inspection and exact disposition

The default `outer_recovery.py plan` is inert. Native `inspect --config ...
--run-id ...` performs bounded file observation only and emits a provisional
manifest hash; no STATUS request, repository operation, kill, or service lifecycle
command occurs. The guard must already exist for this recovery inspection.

Native `dispose --config ... --approval ...` requires Linux/root and exact private
input references. The exact authority schema is `validate_authority` in
`outer_recovery.py`: one UUID recovery ID/run/capture, canonical config hash,
complete original manifest digest, named resolution, optional exact existing
inner approval, independent evidence owner/reviewer and hashed private evidence
references, at most ten minutes, and finite byte/entry limits. A review evidence
hash is not a substitute for native containment observation.

The native command pins and rereads its raw config/authority, source bundle,
machine/boot, original helper containment, and current source unit/process
snapshots. It repeats these checks after archival and immediately before commit.
Source continuity is explicitly `UNPROVEN` in a disposition even if observations
look unchanged.

Only the existing inner `CONFIRM_NO_CAPTURE`, `ABANDON_BEFORE_SEAL`, and
`CONFIRM_VERIFIED_RELEASE` resolutions can mutate the mailbox. Unknown transfer,
post-SEAL abandonment, unclaimed mailbox work, mismatched raw state, conflicting
recovery IDs, partial bytes and unexplained immutable changes remain HOLD.

## Durable records and succession

V2 capture creates an immutable owner record and claim before effects, and an
exclusive `capture-call.json` before entering the inner executor. Missing owner,
partial claim and lock-only failures remain HOLD; file absence alone cannot
establish no capture. A complete v2 pre-call barrier can support `NO_CAPTURE`.
The claim binds the owner raw digest, canonical config snapshot and permanent
ledger/guard/attachment-lock identities; the call binds the original raw claim
and owner. A newly approved evidence manifest cannot override a contradiction
between those original records. Reentry rejects a replaced ledger or guard inode.

Recovery saves exact raw config/authority and original evidence copies, including
malformed originals and raw `online-state.json`, before its append-only intent.
The shared state is a separate CAS domain. The approved HOLD-to-RECONCILED change
and only the enumerated capture-bound mailbox/inner-recovery additions are
allowed. Before and after manifests and raw copies remain immutable.

The disposition never writes outer COMPLETE, changes original claim/HOLD/result,
or deletes `attachment.lock`. It logically retires that exact lock identity.
Successful v2 attempts also bind the retained lock. Future claims bind the raw
predecessor state hash plus the exact predecessor record hash. Later scans follow
that causal chain and validate historical copies rather than comparing every
old disposition with today's mutable state. An unexplained tip or fork blocks.

An exact existing intent can resume under still-valid authority. If the inner
reconciliation has already reached the bound RECONCILED state, recovery validates
its artifacts without replay. A complete disposition can be read back and
fsynced after acknowledgment loss; different or partial bytes are not overwritten.
An ABORT response lost after a consumed request is supported only with the same
approved inner intent and exact bound abandon receipt/abort marker. The existing
inner reconciler observes EMPTY and does not send ABORT again. Lost RELEASE replies
likewise retain the receipt/marker checks; neither path retries a transfer.

## Deliberate native limits

Native process custody currently supports only same-boot attempts whose original
helper systemd invocation and cgroup inode are still available. The cgroup must
be empty, with no relaunch/timer trigger, no delegation, read-only control groups,
NoNewPrivileges, control-group kill mode and a capability bounding set limited to
CHOWN/DAC_OVERRIDE/DAC_READ_SEARCH/FOWNER. Capturer Python identity is pinned;
source cgroups must be disjoint. Children inherit that containment. This is not
a per-child PID journal or a general escape detector.

If systemd has removed the empty cgroup, reset the invocation, or changed the
boot, the current tool returns HOLD. It does not reconstruct custody from PID
absence. Namespace/cgroup enforcement, power-loss durability, deployment-wide
legacy exclusion, provider behavior, service continuity and actual restore are
not established by Windows fixture tests. No server/service/DB/provider/backup/
restore operation was performed for this WI.

The source/config template must be reviewed and populated for a specific native
environment under separate authority. No installation, migration, service-control
or native recovery is authorized by the local candidate.
