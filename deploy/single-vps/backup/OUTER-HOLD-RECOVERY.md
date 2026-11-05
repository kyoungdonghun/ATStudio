---
version: 1.0
last_updated: 2026-11-06
project: ATS
owner: SA
category: design
status: accepted
dependencies:
  - path: ../../../docs/registry/git-checkpoint-20261106.md
    reason: Approved local design and future execution approval boundaries
  - path: LIVE-SERVICE.md
    reason: Accepted local capture adapter and unimplemented outer recovery
  - path: ONLINE.md
    reason: Existing inner protocol and narrow reconciliation
  - path: ONLINE-RESTORE.md
    reason: Existing isolated restore requires stopped source services
---
# Outer backup HOLD recovery and next native trial

## 1. Decision and current capability

Propose one small, explicitly authorized outer-disposition operation plus the matching
capture/ledger-reader changes. It may make a previously blocked ledger eligible for a
**new** approved capture; it never retries the old capture, manufactures COMPLETE,
proves service continuity or changes a restore receipt. This document is a design,
not an implemented command. Current outer HOLD remains blocking.

Observed on 2026-11-06 KST by SA: local Windows source and WI007/WI012 evidence only.
No native observation, provider query, secret read, build or application test occurred.
Independent [WI002 review: retained local evidence](../../../docs/registry/git-checkpoint-20261106.md#retained-evidence-limits)
accepted the local design after the mutable-state and version-boundary corrections.
Acceptance does not implement or authorize this proposed operation.

| Current source | Consequence for this design |
|---|---|
| `live_service_capture.py:127-141` | `validate_previous` admits only attempt directories with exact COMPLETE results; arbitrary sidecars or RECONCILED results are rejected. A recovery writer alone cannot enable another capture. |
| `live_service_capture.py:160-184,209-233` | mkdir/fsync may leave only `attachment.lock`; attempt mkdir precedes claim write. Uncatchable death may leave no HOLD/result. Cleanup flags cover adapter-owned work, not inner readers. |
| `live_service_capture.py:185-206,270-327` | Capture lies between two source observations. Equal samples do not prove continuous service. There is no durable capture-call marker or complete helper-owner journal in the current outer claim. |
| `online_capture.py:305-401,554-622` | Inner guards may reject before a same-run state exists; after entry, FREEZE, SEAL, transfer, verification and RELEASE have separate effects. Absence of a same-run state is insufficient proof of no effects. |
| `online_capture.py:405-552` | Existing reconcile requires exact HOLD/config/run/old boot and only three resolutions. STATUS publishes mailbox request/response metadata. It is not pure read-only inspection. |
| `backup_runtime.py:120-137,283-321` | The shared flock ends with its owner; its release does not prove all spawned reader groups ended. In-memory `runner.active` is not durable recovery evidence. |
| `online_restore.py:165-182,253-266,382-421` | Existing restore accepts inner COMPLETE, not RECONCILED; it requires source app/DB stopped and rejects any Java/MySQL/FFmpeg process. Outer disposition does not relax this. |

Existing accepted local code and historical results remain intact. Reuse the inner
protocol; do not add transfer discovery, a generalized incident engine, recurring
scheduling, new provider access or automated service restart to this work.

**Version boundary:** the minimum implementation is v2-first. A v2 run starts with
the reviewed gate/recovery-capable bundle already pinned in its immutable config;
its recovery uses that exact bundle/config. Current v1 `execute_reconcile` checks
old config module hashes, while changing those hashes changes the HOLD-bound config
digest (`online_capture.py:413,539-541`). Thus fresh v2 pins do **not** authorize
mutating an unresolved v1 inner HOLD. Such legacy HOLD remains unsupported here.
An already terminal v1 inner COMPLETE/RECONCILED, or positively proved no-entry/no-effect,
may support an outer-only disposition after complete custody/owner evidence. No old
config/claim edit, ungated legacy invocation, pin waiver or upgrade recovery is proposed.
The same restriction applies if a v2 attempt's pinned bundle/config changes after
capture: no inner mutation until the exact original reviewed bundle/config is available
through the approved gated path; otherwise HOLD. Raw state snapshots are byte copies,
never JSON reserialization presented as the original file hash.
Actual server deployment/version is unknown until approved P1 observation.

## 2. Inspection, exact authority and termination evidence

An initial inspection is bounded host/file/process observation only, under separately
approved remote read access. Collect exact bytes/hashes (including malformed bytes),
presence/absence, ownership/device/inode and bounded directory inventories for the
stable ledger, outer attempt, inner state/work directory and related mailbox records.
Bind config/approval/pins by hash and private reference; do not print their bodies.
Capture current machine/boot, source unit/PID/start-ticks/invocation/cgroup/JAR identity,
helper identities and resource/freeze/pin state. Do not send STATUS, repository reads,
kill signals, mutating service lifecycle commands or mutable recovery requests during
read-only preflight.
An actively changing observation is provisional; the final decision rereads under gates.

Future exact recovery approval must bind all of the following:

- Operation/version, unique recovery ID, original ledger absolute path plus device/inode,
  original run/capture IDs when known, raw original claim/HOLD/result hashes or explicit
  missing/malformed states, lock identity, complete original evidence-manifest digest,
  inner state/config hash, current code/binary pins and host/boot identity.
- One named resolution, independent evidence owner/reviewer and evidence hashes; a finite
  window (at most 10 minutes), one disposition, and whether an exact existing inner
  reconcile resolution is included. No capture/restore/service-control authority is implied.
- Separate approval for any needed owned-helper termination, including exact unit invocation,
  cgroup, PID/start ticks, signal/timeout bound and confirmation that source units are excluded.
  If it was already concretely approved, reuse that scope; otherwise hold for it.
- Bounds for local evidence I/O and permitted mailbox effects. Provider observations are a
  separate costed scope; this minimal disposition operation issues no repository commands.

For **same-boot** termination, evidence must connect original launch identities to the
capturer, all dump/restic/verification/show helpers and descendants: machine+boot,
unit invocation, cgroup path and identity, PID/start ticks and executable identity.
Observe the exact approved helper cgroup empty/inactive, no surviving original process
identities or escaped descendants, no pending relaunch/timer/other writer, and no inner
operation owner while the recovery owns the gates. PID absence alone, PID reuse, a
successful `stop_owned`, an expired timer, an empty in-memory map or a boolean supplied
by the caller is insufficient. Recheck immediately before disposition commit.

Future captures must durably record the capturer/helper containment identity before
launch and a create-once `capture-call.json` before calling the inner executor. Record
spawn identities before proceeding to another effect, or rely on an independently
proved non-escaping helper cgroup with complete owner evidence. An interruption between
spawn and durable recording stays HOLD unless containment evidence closes that gap.
Legacy attempts lack some of these records; unsupported evidence is not reconstructed
from absent files or a human assertion.

A verified **boot change** proves the old boot's processes cannot still run only when
host identity and reboot provenance exclude another host, clone or restored image.
It does not prove mailbox release, absence of pins, transfer outcome, no replacement
writer or source consistency. Existing inner reconcile refuses changed boot/config;
do not edit old config or substitute a new boot hash. Cross-boot inner reconciliation
and unknown legacy containment remain outside this minimum implementation and HOLD.

## 3. Failure decision matrix

All proposed dispositions below require the exact authority, custody and termination
evidence above. The word eligible means eligible for **future implementation**, not
supported by today's adapter. Rows invoking inner mutation apply only to an exact
matching v2-origin config/bundle; unresolved v1 HOLD stays blocked as above.
Independent dimensions are recorded separately:
capture outcome, inner protocol, source identity/continuity, active writer/reader,
freeze/pins, cumulative cost and restore status.

| Observed cut/state | Required evidence and permitted action | Postcondition / stop condition |
|---|---|---|
| Rejection before any outer lock/attempt | Stable ledger inventory, exact invoked pins and authoritative pre-entry rejection; no writer or related inner effect | No consumed outer attempt only if established; no recovery write needed. Fresh authority is required for any new capture. Console text alone proves nothing. |
| Lock only; missing/partial claim; incomplete attempt directory | Bind all raw bytes/absence and exact dead owner. Prove a pre-inner-entry barrier and no pending matching mailbox work; future durable call marker helps. Legacy owner/entry uncertainty usually cannot satisfy this. | Proposed `NO_CAPTURE` only with positive no-entry/no-effect evidence. Otherwise HOLD; never synthesize a claim or infer safety from missing state. |
| Claim/before present, no inner same-run state | Exact stopped owner and positive no-entry evidence, or bounded proof that every precondition rejection occurred before any inner mutation; inspect work/mailbox too | Same proposed `NO_CAPTURE`; claim/before alone or an absent inner state is insufficient. |
| Inner HOLD before FREEZE or rejected FREEZE | Existing `CONFIRM_NO_CAPTURE`: exact HOLD, PRECHECK with no requests, or FREEZE_REQUESTED with the exact rejected FREEZE claim; no snapshot/capture directory and STATUS EMPTY | Existing inner reconcile may reach RECONCILED. Then proposed `INNER_RECONCILED` outer disposition; preserve no-success outcome. |
| Inner HOLD with confirmed FROZEN before seal | Existing `ABANDON_BEFORE_SEAL`: failed FREEZE_REQUESTED/DUMP_FROZEN, no SEAL/RELEASE claim, snapshot or seal; all original mutation claims match; exact readers ended | Explicit ABORT/abandon receipt and final EMPTY through existing protocol, then proposed `INNER_RECONCILED`. No ABORT based solely on stage label. |
| SEAL sent/reply lost; PINNED; TRANSFER/VERIFY failure; snapshot unknown; reader still alive | Preserve requests, claims, pins and every transfer/snapshot fact. Missing receipt does not prove failed transfer; application health does not prove safe release. | HOLD. Existing reconcile has no general post-SEAL abandonment or transfer discovery. No new transfer, prune, release or replacement ledger. A separate exact investigation/repair contract is required. |
| Inner HOLD at RELEASE_REQUESTED | Existing `CONFIRM_VERIFIED_RELEASE`: exact verified receipt/content manifest/snapshot/repository/dump/reference bindings, matching published receipt, original mutation claims and exact terminated readers | STATUS plus receipt-bound RELEASE only if PINNED, or existing release marker if EMPTY; final EMPTY and RECONCILED. A lost/partial claim still blocks. Outer disposition does not turn RECONCILED into COMPLETE or make it accepted by current restore. |
| Inner COMPLETE, outer post-observation/HOLD/result missing | Exact inner receipt/state/release evidence and readers ended; source current identity and reason for missing/changed observation independently recorded | Proposed `INNER_COMPLETE`. Preserve outer failure and mark continuity unproven if not established; never write outer result COMPLETE. |
| Inner already RECONCILED | Exact immutable recovery intent/result, original HOLD, matching current inner state and final protocol markers for one of the three supported resolutions | Proposed `INNER_RECONCILED`; no replay of ABORT/RELEASE. If original outcome or evidence chain is incomplete, HOLD. |
| Valid outer COMPLETE but lock remains after failure/kill around unlock | Exact original claim/result, inner complete/release evidence, exact dead owner and current source/freeze/pin checks | Proposed `ORIGINAL_COMPLETE_RECORDED` retires only the stale lock; original COMPLETE is neither recreated nor strengthened. |
| Source PID/start/invocation/JAR changed, authority expired, or post-check absent | Establish inner terminal outcome independently; investigate and record change without restarting source | Terminal inner state can support a disposition with `source_continuity: UNPROVEN` and change evidence; next capture requires fresh source pins/runtime proof. Unexplained active writer/reader or nonterminal inner state remains HOLD. |
| New boot, corrupted custody, unsupported state, unbounded evidence, contradictory markers, uncertain fsync | Preserve all evidence and stop the affected action | HOLD. No timestamp heuristic, process-name kill, claim replacement, force unlock or flag override. |

Any successful inner reconcile still changes inner state/mailbox and needs its exact
authority. It checks termination assertions, not the external evidence itself; the
operator and independent reviewer must establish that evidence. A release ends capture
pins; it does not reclaim already served bytes or prove restored SQL/application use.

## 4. Minimal durable outer disposition contract

### Common gate and compatible reader

Introduce one permanent private `operation.guard` inode in the **existing** outer
ledger, never unlink/recreate it. All eligible capture, recovery, direct inner
capture/reconcile, restore and dispatcher entrypoints that can mutate this state must
obtain that guard first, then the existing shared operations lock. Hold the guard
through the entire outer before/capture/after/result lifecycle. Require current pinned
entrypoints only; legacy binaries/callers that ignore the guard must be excluded and
their lack of active owners proved before migration/recovery. Otherwise HOLD.

Use an explicit internal owned-lock call graph: one acquisition per lock, common native
prechecks retained, capture/reconcile internals receive an unforgeable in-process owner
context. Do not call `execute_reconcile`/`execute` and reacquire their flock on a second
descriptor while already holding it. No public skip-lock flag, stale-lock timeout or
alternate ledger is allowed. Recovery holds both gates during final inspect, any
authorized inner reconcile, decision and durable commit. Source admission/mailbox
workers keep their existing protocol semantics; the outer gate is not a writer fence.

The future validator permits only the specified guard, existing `attachment.lock`,
`attempt-<run>` entries and bounded `recovery/<recovery-id>` records. It accepts each
attempt either through its unchanged valid historical COMPLETE result or exactly one
valid committed disposition. Missing/malformed/unbound/contradictory/new incomplete
attempts still block. Recovery records never satisfy capture success or restore binding.
Current v1 rejects this layout; stage the writer, reader and all affected entrypoints
together with fresh code pins. No native migration is authorized here.

### Append-only intent, disposition and logical lock release

Preserve every original outer claim, result, HOLD and lock directory and all immutable
inner per-capture evidence. Shared `online-state.json` is mutable by design: preserve
its exact raw **before copy**, then permit only the approved inner transition. Existing
reconcile may append bound STATUS/ABORT/RELEASE requests/claims/responses and terminal
markers; it must not alter old immutable request/claim/receipt bytes. These named effects
are distinct from blanket permission to change the inspected tree.
Do not delete or rename `attachment.lock`: a valid committed disposition makes that
exact historical lock **logically retired** for the new validator. Kernel serialization
uses the permanent guard; subsequent runs consume new immutable attempt claims in the
same ledger. Old v1 continues to reject, which is preferable to silently bypassing it.

1. Under both gates, verify authority/time, code/custody and absence of active owners.
   Reread a bounded manifest of original path names, raw hashes, explicit missing entries
   and lock device/inode/mode/owner. Separate immutable-original bindings from mutable
   shared-state bindings. Save exclusive private raw before copies (including the full
   online-state bytes) in this recovery directory, and fsync files/directories. Pending
   unexplained mailbox work blocks; do not classify it as an allowed future addition.
2. Create exclusive `recovery/<id>/intent.json` containing the approval digest, original
   manifest digest, immutable before-copy hashes, expected shared-state hash and the
   enumerated permitted transition/additions for the chosen resolution. File fsync,
   recovery-directory fsync and every
   newly created parent directory fsync must complete before a protocol mutation.
   An existing exact intent may resume under still-valid approval; different bytes block.
3. If explicitly needed and version-compatible, compare current raw shared-state bytes
   to the intent's expected hash and run existing inner reconciliation with all its native
   checks under the owned locks. Permit only its exact capture-bound protocol additions
   and HOLD-to-RECONCILED state transition; bind generated request IDs, bytes and final
   markers in the after inventory. Preserve original inner intent/results and mailbox
   traffic. No outer implementation discovers/retries uncertain transfers.
4. Reread terminal inner evidence, source/reader/containment state, immutable-original
   manifest and authority. Verify the allowed state transition and exact additions;
   any unrelated difference blocks. Save immutable raw terminal after copies and their
   provenance, then write exclusive `disposition.json`, file fsync and all affected parent
   fsyncs. It binds intent/approval, before/after inner evidence, termination and source
   observations, allowed resolution and `restore_verified:false`. It contains
   `capture_success_asserted:false`; original successful records remain separate.
5. Only a fully validated durable disposition makes the covered lock/attempt retired.
   New captures rescan the whole ledger under the same guard and reject any uncovered
   lock, attempt, partial recovery or later mutation. They require new run/approvals,
   current runtime/repository/recovery/cumulative-cost proofs and actual source identity.
   Disposition is not authority to run that capture or a reason to reuse the old run ID.

Historical dispositions are validated against their immutable before/after copies,
original immutable artifacts and exact provenance, **not against online-state.json
forever**. At immediate reentry, current shared state must equal the just-disposed
terminal after hash. A new capture durably binds that raw predecessor hash/copy and the
preceding disposition in its fresh claim before changing shared state; existing inner
`previous-state.json` and per-run cost proof remain required. Later scans validate this
recorded succession and the newest live state. A legitimate new capture's state updates
do not invalidate the old disposition; unexplained replacement or a new incomplete attempt
still blocks. Recovery never overwrites the original inner state's historical before copy.

Crash handling is deliberately small:

- Before intent durability: no authorized protocol action; retained partial files block.
- After intent, before disposition: HOLD. Resume the exact identity only after rereading
  evidence. If inner reconcile completed, validate its terminal records instead of calling
  reconcile again (the existing entry requires HOLD and is not a generic replay endpoint).
- Lost ABORT/RELEASE reply or partial inner record: use only supported existing recovery
  evidence paths; otherwise HOLD. Never issue the mutation solely because outer intent exists.
- Disposition exists but fsync/acknowledgment was uncertain: while holding the gates,
  validate its exact bytes and all bindings, then confirm file/parent durability before
  treating it as committed. This performs no protocol replay. Invalid/partial bytes block.
- After durable disposition but before response/guard release: the next invocation validates
  and reports that same disposition. It does not consume a second recovery/capture approval.
- Conflicting recovery IDs/resolutions or incomplete recovery debris block. The minimum
  tool does not overwrite, delete or auto-supersede them; repair needs a separate contract.

Logical release avoids an unlink-versus-new-capturer crash window. It does not remove the
need for native filesystem/fsync/power-loss and legacy-entrypoint exclusion evidence.

## 5. Minimal implementation and acceptance proposal

Not authorized by this design REQ: one later local WI should implement the outer
inspect/dispose path, shared lock ownership, exact recovery schema and reader changes;
add only the future capture owner/call records needed for recoverability. Reuse current
safe reads, exclusive writes, fsync and inner protocol. Candidate source set includes
`live_service_capture.py`, `online_capture.py`, shared runtime/dispatcher and the restore
entrypoint as needed for the common gate; no provider or application protocol expansion.
Separately review the lock-call graph and final pins as one coherent bundle.

Focused tests must cover no-entry/lock-only/partial claim, capture-call gap, missing
inner state, each supported inner terminal resolution, unsupported post-SEAL states,
same-boot PID reuse/escaped reader, boot change, source drift, expiry, two competing
recoveries, capture-versus-recovery/direct-inner races, legacy entrypoint rejection,
every fsync/acknowledgment crash cut, partial/conflicting records, idempotent disposition
readback, no lock inode replacement and a **new** run accepted only after exact valid
disposition. Verify COMPLETE/restore receipts never change and all old evidence stays.
Include v1 unresolved-HOLD pin incompatibility rejection and a supported v2 inner
reconcile -> disposition -> new capture -> later ledger scan, exercising the exact
allowed shared-state transition while preserving immutable raw history.
Synthetic tests cannot establish native root custody, cgroup behavior or power-loss safety.

The next server sequence, approval payload and stop conditions are in the
[approved planning REQ: retained local evidence](../../../docs/registry/git-checkpoint-20261106.md#retained-evidence-limits).
An outer recovery implementation/review is a prerequisite to operational acceptance;
a design document is not enough to deploy this contract.

## Public checkpoint evidence boundary

This public copy redirects unavailable local evidence links to the shared
checkpoint boundary. The original document bytes and detailed REQ/WI evidence
remain in private local custody. No runtime contract or source code changed.
