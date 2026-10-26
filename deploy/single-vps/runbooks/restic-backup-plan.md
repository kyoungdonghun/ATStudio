---
version: 1.1
last_updated: 2026-10-10
project: ATS
owner: PG
category: guide
status: draft
dependencies:
  - path: ../../../deliverables/user/REQ-20261010-ATS-005.md
    reason: Approved restic design, not execution
  - path: backup-restore.md
    reason: Complete recovery inventory and isolation contract
  - path: resource-baseline.md
    reason: WI008 shared-host resource envelope
---

# Restic Backup and Recovery Plan

## 1. Status and Execution Gates

Restic is selected; **guarded capture core and default-OFF recurring policy are
implemented locally**, not an operational backup service. See
[WI013](../../../deliverables/agent/WI-20261010-ATS-013-evidence-pack.md),
[final PG review](../../../deliverables/agent/WI-20261010-ATS-014-evidence-pack.md)
and the [host integration contract](../backup/README.md).
Installation, binary execution, repository creation, DB dump, transfer, deletion and restore: **NOT RUN**.
Historical WI009/010 design-only/unchanged-template evidence is superseded by
REQ006 local implementation, not overwritten. SE/MA/PG each passed 35 synthetic
backup tests; PG added seven failure probes on 2026-10-10 Windows. No new blocking
local defect found; production HOLD.

Every host hook is unconfigured: repository/recovery preflight, close/drain,
JVM/encoder stop and relaunch inhibition, external writer/schema/key fences,
logical dump/completeness, repeated quiescence, matching resume and restricted
health. `host_hooks_unconfigured.py` is a failure marker, not an adapter.
Daily-service/lock integration and an independent checker/sender need future
implementation. Alert transport, external-disk copy and actual restore are not
implemented. Default CLI reports `side_effects:false`, `NOT_CONFIGURED`.

WI009's dated document check and WI013/014 protocol review reference restic
0.19.1; this is not an installed-version or compatibility claim.
Pin and verify the selected Linux/Windows binaries and repository format before use. [Official installation](https://restic.readthedocs.io/en/stable/020_installation.html)

Approve the exact private bucket/prefix, endpoint/region, identities, credential references, filesystem paths, owner, alert recipient, byte budget and maintenance window before execution.
MA's provider research documents API endpoint plus Access Key ID/Secret Key ID; its Ubuntu 20.04 example uses region `default`, not proof for the new host. [iwinv example](https://help.iwinv.kr/manual/795), [migration reference](https://docs.iwinv.kr/reference/storage/object-migrate/)
Do not invent an IDC endpoint or accept restic's default region as correct. Target TLS/private ACL/list/put/get/delete tests require a separately approved disposable prefix and exact deletion scope.
S3 compatibility is a documented candidate, **not a successful restic/iwinv test**. No provider price re-audit is performed here.

## 2. Topology and Recovery Unit

VPS files -> encrypted restic format-2 repository in private S3-compatible storage -> independently controlled encrypted repository on the operator's external disk.
Back up files directly; do not create full plaintext or encrypted media archives on the 100GB host. Restic does not create a consistent database/file boundary itself.
Use explicit HTTPS repository configuration and provider-approved region; map the provider keys privately to restic's `AWS_ACCESS_KEY_ID` / `AWS_SECRET_ACCESS_KEY` inputs. Use a protected password-file reference, never command-line secret values. [Repository preparation](https://restic.readthedocs.io/en/stable/030_preparing_a_new_repo.html)

| Member of one capture | Requirement |
|---|---|
| Logical MySQL dump | Schema and all application rows, including payment/audit/schedules, audio state and storage mutation journal; successful dump receipt and SHA-256. Never copy live MySQL data files. |
| Entire public storage root | Originals, MP3 derivatives, images, inactive references, pending/claimed inputs and required staging; no audio-only subset. |
| Entire private storage root | Company, Notice and Question attachments and staging; explicitly record an empty root. |
| Exact release | Source revision/dirty disposition, actual JAR/frontend bytes and hashes, schema and Java/FFmpeg/MySQL versions. Resolve the release symlink and include its real target. |
| Settings and keys | Approved configuration/service/proxy/firewall/TLS inventory, permissions, JWT and complete retained billing keyring; only in protected encrypted backup. |
| Independent recovery access | Repository passwords, provider/domain recovery and key inventory references available without the VPS or original PC; not solely inside the repository they unlock. |

The detailed inventory remains private. General restic symlink behavior is not
the candidate's acceptance policy: internal symlinks, nested mounts, special
files/modes are rejected, not omitted. Resolve the release link to its approved
real target. Linux owner/mode and ACL/xattr/SELinux restore are unverified.
[Backup semantics](https://restic.readthedocs.io/en/stable/040_backup.html)
Allowlist settings instead of copying all of `/etc`; exclude repository/cache/temp directories from source roots. Keep required application staging included.
Separate app and backup identities. Restrict credentials to the approved repository as provider capabilities permit; test permissions including lock cleanup. Encryption is not deletion protection against a compromised account.
Use root/operator-only local dump/config access, no public upload path, no repo commits or plaintext secrets/PII in logs. Repository/password paths remain execution inputs.

## 3. Space, Memory and Duration

On every involved filesystem measure free bytes/inodes, existing local captures and the next operation's peak. Do not double-count already occupied bytes.
Required additional free space = `max(20% filesystem, 10GiB) + new dump + new metadata + cache/temp growth + other admitted growth`.
Initial test budgets: logical dump <=2GiB, manifest <=256MiB, cache <=1GiB, temp <=1GiB, plus the reserve. These are proposed ceilings, not measured requirements or restic-enforced quotas.
Projected-growth checks and sampled stops are implemented, not native-tested or
filesystem quotas; count an old dump if it must coexist. Never truncate a dump or silently delete originals/old backups to fit.
If estimated dump/index/cache needs exceed the allowance, stop and revise the plan. No paid disk expansion or fixed compression/deduplication saving is assumed.

Match [WI008](resource-baseline.md#3-memory-and-backup-envelope): the **whole helper group** gets MemoryHigh768MiB, MemoryMax1GiB, CPUQuota100%, Nice10; not 1GiB per process.
Run dump and restic sequentially, with app/FFmpeg stopped. No helper/helper, encoder, large ingest, prune or load-test overlap initially. Keep >=1GiB MemAvailable reserve.
Restic candidate knobs: `GOMAXPROCS=1`, read concurrency1, S3 connections2, compression auto, target pack16MiB; retain data verification. These are local candidates, not execution.
Pack buffering needs temporary space; file cache also affects cgroup memory. Disk-cache allowance is not a RAM allowance. Observe group peak, pressure and OOM events; 1GiB sufficiency is unverified. [Tuning](https://restic.readthedocs.io/en/stable/047_tuning_parameters.html)
MemoryHigh pressure, projected disk breach or rising swap prompts operator review; OOM, nonzero exit or reserve breach fails the run. Do not auto-raise limits.

`transfer minutes = transferred bytes / sustained bytes per second / 60`; add drain, dump, hashing, scans, retries and completion checks.
For **50 decimal GB** uploaded: 20MiB/s gives39.7min; 2MiB/s gives397.4min. Neither rate is measured or promised. They are not the provider's inbound 50Mbps contract threshold.
Choose an explicitly budgeted transfer ceiling using restic's KiB/s upload/download limits and measure the effective rate; rate limiting does not cap total billed bytes. [Manual](https://restic.readthedocs.io/en/stable/manual_rest.html)
Complete the first full baseline and recovery rehearsal **before admitting valuable writes**. A 30-minute initial cold capture is not promised; later downtime depends on changed bytes and actual performance.

## 4. Cold Capture State Machine

All steps below require future exact-target execution approval. Keep a private run receipt with source tuple, capture interval, inventory/hash outcomes, repository/snapshot IDs and status.

1. **PRECHECK:** Confirm approved targets, capacity, credentials, prior good snapshot and recovery access; acquire one orchestration lock shared with deployment/backup/check/copy/prune. Never bypass repository locks.
2. **QUIESCENT:** Close proxy admission, drain or explicitly abort in-flight work, disable automatic relaunch, stop the sole JVM and every FFmpeg/worker. Fence other DB/file writers, schema changes and key rotation. Record the boundary.
3. **DUMP COMPLETE:** MySQL may run for the logical read. Write a bounded private temporary dump, require exit0 and expected schema/content inventory, hash it, then publish it as this run's completed dump. Never reuse a stale dump after failure. A raw dump-to-stdin pipe is not this plan.
4. **CAPTURE:** While the same boundary holds, generate the manifest and capture the dump, both roots, real release target and reviewed settings/key set in one restic snapshot. Preserve pending state and referenced staging; no recovery worker runs during capture.
5. **CAPTURE COMPLETE:** Require backup exit0, exact new snapshot ID, decryptable manifest/dump hash retrieval, exact member/type/size/ordinary-mode/owner inventory and no read errors. Retrieve only dump+manifest in this window, never all media. Hash local sources before/after capture and retain hashes privately for later restore. This costs at least two application-side full source reads plus restic's local read; I/O and cold-window duration are unmeasured. Metadata/dump checks are not full remote-data or application restore proof.
6. **RESUME:** Only after step5, resume one writer with the matching release/roots and restricted health checks under the approved restart plan. Record downtime. No need to keep writers stopped for the independent copy or full repository read if those read only the committed snapshot.
7. **VERIFY/COPY:** Schedule heavy post-capture checks/copy away from ingest/load testing; preferably operate from the operator PC. Mark source snapshot, independently copied snapshot and restore-verified point separately. Do not delete source captures or local dump without exact cleanup approval.

Keep a stable logical host and media-root list with `ats-prod,cold-v1` plus exactly
one class, `routine` or `prechange`. The full snapshot path list is not stable:
the implementation includes per-run `work_dir/run_id/database.sql` and
`manifest.json`, so default host+paths retention grouping splits every run,
even when the release is unchanged. A release-path change also affects grouping.
Before enabling any retention operation, separately review explicit grouping
and exact snapshot-ID selection across runs; do not infer effective 7/4/3
retention from stable media roots or tags. No retention/deletion is enabled here.
One capture may satisfy more than one retention purpose through the reviewed retained-ID union; do not treat a tag as an immutable hold.

| Failure / overrun | Required outcome |
|---|---|
| Dump failure, absent member, hash mismatch | No valid recovery point; preserve earlier good point, alert owner, diagnose privately. |
| Exit3 / unreadable file | Partial snapshot may exist; reject it, record its ID, do not advance last-good or automatically delete it. |
| Other nonzero/unknown exit, lock conflict, OOM, network/quota failure | Fail closed for backup status; no blind retry/init/unlock/prune. Uploaded partial packs may still cost storage. |
| Deadline reached | Operator explicitly extends the window, or cancels owned capture, waits for all readers/helpers to exit, and authorizes reopening without a new point. Never resume live writers under an ongoing direct capture. |
| Off-provider copy fails | Source capture can remain valid, but independent-copy status/RPO remains stale; alert and keep the source protected. |

After reopening, any retry needs a new writer-free DB/file capture; do not combine yesterday's dump with today's files. Unknown exit codes are failures. [Exit and scripting contract](https://restic.readthedocs.io/en/stable/075_scripting.html)
The implementation parses bounded receipt fields and rejects unknown/partial
results without publishing raw stderr/private payloads. Failed capture preserves
prior last-good and never automatically resumes. Atomic publication and owned
helper cleanup are fake-tested; native crash/signal/flock durability is NOT RUN.
An independent approved alert channel is still unimplemented.

## 5. Schedule and Retention Proposal

Daily cold capture remains selected; recurring authorization is implemented but
default OFF. It binds exact config digest/target, KST start, 1-60s grace, bounded
same-day window wholly before expiry, deterministic config/date run ID and a
non-reusable run directory; approval is rechecked under the operations lock.
No catch-up or automatic retry. Exact time, outage, owner and receiver remain
unresolved. The timer has no calendar/install section and `Persistent=false`;
the service is not installed. Other operations do not yet share
`/run/atstudio-operations/operations.lock`. Manual prechange capture retains
separate one-use approval. This is not successful daily unattended backup.
Daily success targets <=24h planned provider-copy RPO. Proposed off-provider copy is weekly plus prechange; that means up to7days planned provider-loss RPO, **not24h**. Approve a daily off-provider cadence instead if that loss is unacceptable.
Alert immediately on failed/missed capture, at >26h since accepted daily capture and >8days since independent copy; thresholds do not turn missed runs into success. No unseen catch-up run may stop the site automatically.
The local status evaluator covers those states; independent scheduling and alert
transport/delivery are not implemented. Daily success never implies full-media
download, independent-copy success or restore-verified status.

| Retained selection | Starting proposal |
|---|---|
| Routine | 7 daily, 4 weekly, 3 monthly selections; union, not14 additional full copies. |
| Prechange | Last2 accepted prechange points, separately selected. |
| Protection | Last restore-verified point, unresolved incident/legal/operator holds, and required not-yet-copied source points stay protected. |

Calendar retention counts nonempty periods; default grouping is host plus paths. Filter the exact project/class, review every group, and union protected IDs before approving deletion. [Retention semantics](https://restic.readthedocs.io/en/stable/060_forget.html)
Local per-run dumps, manifests and receipts are intentionally retained without
cleanup. Recurring operation therefore also needs a separately approved bounded
local cleanup policy, preserving required recovery/evidence; otherwise accumulated
files eventually cause the reserve gate to stop captures. No deletion is authorized.
Future forget/prune must first produce reviewed dry-run results, exact candidate IDs, replacement proof and independent-copy receipts. **This document approves no deletion or automatic prune.**
Forget removes snapshot references; prune reclaims unreferenced data and may repack with temporary remote/local growth. Budget time, helper memory, reads/writes, requests and peak storage; dry-run a bounded repack plan, then check integrity after separately approved execution.
No bucket lifecycle rule may expire shared restic pack objects by age. Versioning/Object Lock can affect deletion and billed bytes; neither compatibility nor immutability is assumed. Never hand-delete repository objects.

## 6. Independent Copy and Cost

The Windows host, external disk identity, exact mounted path and free-space limit must be approved. Fail if the intended disk is absent; do not fall back to a local C: directory. Keep the disk offline after verified copying.
`copy-plan` currently returns a future-operation marker, not a working copy tool.
Initialize a separate destination repository/password in a later approved operation; copying chunker parameters aids compatibility of deduplication, **not shared encryption keys**. Copy exact accepted source snapshot IDs, record destination IDs and verify required members. [Repository copy/check](https://restic.readthedocs.io/en/stable/045_working_with_repos.html)
Run copy on the operator machine so cloud reads go to that machine and encrypted destination writes go to its disk. This is not a free server-side copy. Do not mirror source deletion or share the only password/recovery access with the VPS account.
Keep passwords separately recoverable, outside this Git repo and not solely on that disk; test access without the original PC. An unplugged or stale disk cannot receive new snapshots. Maintain independent retention review.

Monitor **actual billed retained bytes**: live referenced chunks plus old versions, metadata, incomplete uploads and repack peaks. Logical snapshot sizes and snapshot counts are not an invoice; no guaranteed deduplication ratio.
Use monthly average stored GB, actual external retrieved GB and request usage separately, including retries, off-provider copies and remote verification/restore reads.
MA's dated2026-10-10 audit records storage20KRW/GB/month and external outbound first band60KRW/GB, both exVAT; request pricing was temporarily free. [Dated audit P8](../../../deliverables/agent/WI-20261010-ATS-007-handoff.md)
Illustrations only: average50GB storage =1,100KRW inclVAT; **50GB actually retrieved externally =3,300KRW inclVAT**, additional to storage/server. Four such full reads =13,200KRW if all remain in that band. API, other transfers and future tariffs remain separate.
The earlier54,120KRW server-plus50GB-storage figure is **not the whole backup bill**. Verify account billing/IDC treatment before execution; do not assume every copy costs only changed logical file bytes or that traffic is free.
Record projected and observed storage/read costs before each large check/copy. If budget is exceeded, pause the operation and escalate; never silently prune protected data or buy capacity.

## 7. Restore Gate Before Invitations

`restore-plan` validates declarations only; it performs no restore/import/start
or actual scheduler-suppression/isolation proof. All steps below remain NOT RUN.

1. Approve an exact new isolated Linux target, new DB/roots and byte/time budget. No overwriting TEST/production, no paid disposable server by inference. The Windows disk stores the encrypted repository; it is not proof of Linux permission restoration.
2. Verify repository metadata plus a complete data read for the selected rehearsal repository; `check` alone is structural, and a sampled read is not complete coverage. Budget external reads when using the cloud repository. Restore exact snapshot IDs, not ambiguous `latest`.
3. Restore files to the new target, compare manifest/SHA-256, ownership/modes, symlinks, dump and release identity; import only into the approved fresh DB. Prove full keyring access and zero missing required media references. DB import alone is not runtime PASS.
4. Before importing/running restored workloads, establish default-deny egress and block real provider/SMTP/public access. Keep timers/services off; do not install real integration credentials as effective settings.
5. **Stop before JVM startup:** no verified universal scheduler-off switch exists. Separately prove source-specific suppression, or obtain explicit approval for an instrumented disposable copy's exact local side effects. Egress denial does not stop renewal, expiry, startup storage recovery or local writes. [Existing containment gate](backup-restore.md#4-isolated-restore-rehearsal)
6. After that gate, verify controlled login, strict storage integrity, public stream/Range, authorized original download/hash, private-file denial and DB/file consistency without real mail/payment. Verify external-disk recovery without the failed VPS/provider account.
7. Record actual RPO from the capture boundary and RTO through functional acceptance, including transfer/import/startup. Existing <=4h RTO is a rehearsal goal, not a guarantee. Preserve the receipt and independently recoverable last-good point; cleanup/release needs separate approval.

Restic restore can overwrite existing targets, so always use a reviewed new destination. [Restore semantics](https://restic.readthedocs.io/en/stable/050_restore.html)
Historical Gmail Sep8 and WAV/128kbps/phone Sep9 TEST evidence remains valid;
none establishes this target's backup/restore success. WI009 -> WI010 is the
preserved design chain; WI013 -> WI014 -> WI015 records current local preparation.
Deployment readiness remains HOLD. Local completion creates no new purchase
precondition; actual service application/invitation gates remain in force.

## Related Documents

- [Backup interfaces](../backup/README.md): Current executable boundary and all host hooks.
- [WI015 integration](../../../deliverables/agent/WI-20261010-ATS-015-evidence-pack.md): Owners, identities and checks.
- [WI009 historical design](../../../deliverables/agent/WI-20261010-ATS-009-evidence-pack.md): Preserved design evidence.
