---
version: 1.0
last_updated: 2026-10-29
project: ATS
owner: SE
category: deployment
status: prepared
dependencies:
  - path: ../../../docs/registry/git-checkpoint-20261106.md
    reason: Local implementation and synthetic evidence
---
# Online capture: prepared, not activated

WI-20261029-ATS-008 / REQ-20261029-ATS-003. This separate adapter reuses
`backup_protocol.py` and `backup_runtime.py`; it does not replace the cold host
adapter, scheduled dispatcher, existing approvals, or historical receipts.
Examples remain `configured: false` and contain no valid runtime authority.

## Connected lifecycle

1. Verify exact host/boot/configuration, reviewed source and binary hashes,
   private credential **references**, filesystem identities, shared operations
   lock, resource bounds, fresh target/ACL/recovery/cost proofs, repository ID.
2. Publish one private `FREEZE` request whose UUID equals the capture UUID.
   Java drains file-reference mutations and writes the exact reference manifest.
   Authentication and ordinary read paths are not globally stopped. A blocked
   mutation may receive a retryable response; this is not a zero-impact claim.
3. While FROZEN, run bounded `mysqldump --single-transaction --quick` and validate
   exit, dump hash, and the exact all-InnoDB table inventory before/after.
   No live row-count equality or previous cold fixture total is imposed.
4. Bind capture ID, reference hash and dump hash in `SEAL`. Only confirmed
   durable PINNED state permits the next phase. File mutations resume while
   captured immutable paths remain protected from deletion/replacement.
5. Hash media **after** seal. Select only those exact files, plus reviewed
   release/settings files, SQL and manifests. Reject symlinks, unexpected
   mounts/special files and NUL/CR/LF list injection. Restic receives only files,
   not recursively changing media directories. New media is outside this capture.
6. Transfer once with unique `capture:<UUID>` and `run:<32hex>` tags. Validate
   the returned full snapshot ID, complete inventory and ownership/modes.
   Stream every selected member back through a bounded SHA-256 reader. Recheck
   pinned source hashes and release identity, and confirm owned readers ended.
7. Publish the exact verified receipt and request `RELEASE`. Record `last_good`
   only after RELEASED. `restore_verified` is always **false**: SQL structure and
   full member hashes do not prove isolated MySQL restoration or application use.

## Native prerequisites and scope

The Windows tests execute an actual Java guard/mailbox subprocess with synthetic
reference data; SQL and restic commands are synthetic. Linux ownership, cgroup,
MySQL/restic compatibility, power-loss behavior, provider charging and large-media
latency are **not** accepted by those tests. Native trial requires separate approval.

- Java private mailbox defaults off; app UID owns mailbox, requests, responses,
  claims and captures (0700), with files 0600. Root backup helper publishes files
  as that explicit app UID/GID. Do not loosen permissions or expose HTTP controls.
- Continuity state, mailbox, media and helper runtime roots must be disjoint.
  Run one app writer; database/schema migrations, release replacement, settings/key
  rotation, external file writers and overlapping deployments must be fenced.
  Fresh target evidence must attest these facts, not merely restate assumptions.
- Reuse the original credential references and repository. No repository creation,
  new credentials, prune/forget, retention deletion, restore or timer activation
  is part of this command. The cold profile is not an online approval.
- Review the exact table allowlist; views/non-InnoDB tables block execution.
  The dump remains locally bounded at 2 GiB by the shared validator. JSON wire
  manifests are bounded at 16 MiB; proof/receipt/control files are 64 KiB. Full
  SQL records can approach the dump bound, so large individual BLOB/row behavior
  and memory consumption still need native measurement before production use.
- Reuse the existing resource envelope: cgroup v2 memory.high <=768 MiB,
  memory.max <=1 GiB, CPU <=1 core, nice >=10, disk reserve max(20%,10 GiB),
  inode/memory reserve, upload/download rates and absolute execution deadline.
  Do not infer that these bounds fit a 40–50 GiB catalogue without measurements.

## Configuration and cost proof preparation

Start from `online-config.example.json` and `online-approval.example.json` in
a private staging area only after native execution is approved. Reuse observed
paths and owner identity; do not guess values or copy example placeholders into
production. `hook_binary` must resolve to `online_capture.py`. Pin all three
Python module hashes and actual MySQL/mysqldump/restic binary hashes.

Four independently reviewed JSON proofs are consumed by path and exact SHA-256:
`target`, `repository_acl`, `recovery_access`, `cost`. Each has `version:1`,
`kind`, `scope:"ONE_ONLINE_CAPTURE"`, `subject_sha256` (canonical config excluding
`online.proofs`), exact repository/machine/boot IDs, `source_evidence_sha256`,
`observed_at`, `expires_at`. Validity is at most six hours and must cover the
entire capture window. The tool does not manufacture or verify provider facts.

| Proof | Additional fields |
|---|---|
| target | `app_tuple_matched`, `sole_app_writer`, `all_media_mutations_fenced`, `schema_release_settings_rotation_fenced`, `no_uncoordinated_file_writers`, `no_overlapping_deployment`: all actually evidenced true |
| repository_acl | `access:"PRIVATE_VERIFIED"` |
| recovery_access | `operation:"EXACT_SNAPSHOT_READ_VERIFIED"`, full existing `snapshot_id` |
| cost | Exact `run_id` and `prior_state_sha256`; `upper_bound_krw == limits.projected_cost_krw <= approved_limit_krw == limits.cost_limit_krw`, `unknown_components:[]`, `new_bytes_bound >= source_bytes+dump_bytes+metadata_bytes`, `retrieval_bytes_bound >= limits.retrieval_bytes` |

Cost/capacity evidence is one-attempt authority. Under the shared operations lock,
the executor hashes the exact prior `online-state.json` bytes and compares that
hash with the cost proof. With no prior file, the binding is SHA-256 of zero bytes.
The proof's observation must be later than the previous attempt's `updated_at`,
including failed/no-effect and RECONCILED attempts. A six-hour validity period
does not permit reusing the same cost proof for another run. Recalculate current
repository usage, remaining storage allowance and cumulative request/transfer
charges before preparing the next proof; the tool cannot independently derive
provider billing from a previous receipt.

The cost estimate must cover cumulative stored bytes and retention, requests,
upload and **full-content retrieval** bandwidth, not only two metadata files.
Retrieval is streamed to hashes, not a second on-disk 50 GiB restore, but it
still transfers all selected bytes and consumes server/provider resources.
Unknown prices, unapproved costs or inadequate bounds block before the first
repository request. Source hashing and post-verification hashing add disk reads;
the backup reader and retrieved-content verification are additional work.
No incremental-saving ratio, zero-won guarantee or performance guarantee is made.

Prepared command shapes (not executed in this work):

```sh
/opt/atstudio-backup/bin/online_capture.py validate --config /private/online.json
/opt/atstudio-backup/bin/online_capture.py execute --config /private/online.json --approval /private/online-approval.json
```

Run the executable inside the separately approved bounded native helper group.
Default `plan` performs no service access. Validation confirms schema only, not
target availability, cost accuracy, native compatibility or approval to execute.

## HOLD and explicit reconciliation

Never delete `online-state.json`, capture directories, claims, responses, failed
requests or consumed approvals to retry. Unknown outcomes stay HOLD. The original
run ID is consumed by its immutable work directory; a new run requires a new ID.
`previous-state.json` archives the predecessor when a later capture starts.

1. Preserve private work/state/mailbox evidence. Independently establish that
   the original helper process groups and all readers ended on the exact boot.
   Lack of output is not termination evidence. HALTED uncertain native writers
   require a separate investigation; this helper will not clear HALTED.
2. Prepare `online-reconcile.example.json`: exact config/run/capture, **raw HOLD
   state hash**, a fresh recovery UUID and <=10-minute approval window, and
   timestamped termination evidence bound to capture/machine/boot. There is no
   network/repository scan in reconciliation and no automatic approval issuance.
3. The helper preserves an immutable recovery intent containing the original
   HOLD state and approval, verifies original request/claim byte identity and
   existing response digests, then requests STATUS for the exact capture.

| Resolution | Permitted evidence and effect |
|---|---|
| `CONFIRM_NO_CAPTURE` | PRECHECK with no FREEZE request, or exact FREEZE request/claim plus explicit REJECTED response; EMPTY and no capture directory. A queued/lost-response FREEZE plus EMPTY is insufficient. No ABORT is issued. |
| `ABANDON_BEFORE_SEAL` | Failed FREEZE/reference or dump phase, no SEAL/RELEASE request, no seal/snapshot evidence, exact FROZEN capture, proven readers ended. Publish private abandonment receipt and ABORT. Lost ABORT response is reconciled only with exact `abort.json` receipt binding and EMPTY. |
| `CONFIRM_VERIFIED_RELEASE` | Failure occurred at RELEASE_REQUESTED, complete existing verified receipt and exact dump/reference/content/snapshot bindings. PINNED may receive the same evidence-bound RELEASE; EMPTY requires exact `release.json`. This never upgrades a partial or unknown transfer to success. |

```sh
/opt/atstudio-backup/bin/online_capture.py reconcile --config /private/online.json --approval /private/online-reconcile.json
```

After a lost reconciliation reply, the **same unchanged intent/approval** may be
reconciled within its window; existing intent does not force a duplicate ABORT.
On success an immutable `recovery/<UUID>/result.json` is stored before state becomes
RECONCILED. Failure fields and prior `last_good` are preserved; successful exact
release confirmation may publish the already verified receipt as `last_good`.
Only COMPLETE/RECONCILED permit a later new capture.

REFERENCES can re-obtain a manifest while the exact capture remains FROZEN; it
does not justify a new FREEZE or resume a failed full pipeline automatically.
For transfer unknown, missing verified receipt, pending claim, expired evidence,
different capture, corrupt state or HALTED, retain HOLD/pins. A separately reviewed
read-only repository reconciliation can use unique capture/run tags to identify
candidates, but must verify exact inventory/content and termination before any
future release. That discovery/resume workflow is not implemented here.

## Restore and rollback boundary

`restore-preparation.json` records the exact snapshot and hashes, with execution
not authorized. Actual restoration must use a fresh isolated DB and file roots,
egress denial, disabled services/timers/schedulers, no effective live credentials
and explicitly approved targets. Do not use the existing cold row totals as live
acceptance criteria.

No deployment occurred. Keep the bridge disabled and retain the cold deployment
to decline adoption. After a real capture starts, rollback must first reconcile
its durable guard and readers; disabling the bridge or deleting state is not a
recovery procedure.

## Public checkpoint evidence boundary

This public copy redirects unavailable local evidence links to the shared
checkpoint boundary. The original document bytes and detailed REQ/WI evidence
remain in private local custody. No runtime contract or source code changed.
