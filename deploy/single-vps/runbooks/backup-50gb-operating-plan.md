---
version: 1.1
last_updated: 2026-10-29
project: ATS
owner: SA
category: guide
status: proposed
dependencies:
  - path: ../../../docs/registry/git-checkpoint-20261106.md
    reason: Approved local planning and independent review only
  - path: ../../../docs/registry/git-checkpoint-20261106.md
    reason: Exact scope and accepted evidence boundaries
  - path: restic-backup-plan.md
    reason: Historical topology, retention and recovery design
---

# 50GB backup operating proposal

## Decision and evidence boundary

Keep application media on the existing Linux server and encrypted backups in the existing private object repository. Prepare a small server result reporter and provider infrastructure monitoring first. Do not activate daily cold capture merely by adding a timer. This is a proposed operating policy and patch plan, not authorization, installation or a capacity guarantee.

Evidence reviewed on 2026-10-28, author checks completed on 2026-10-29 KST: local `main` HEAD `f0c73d449e4f2773ad91cb2fb34093ddd57cd741`, with substantial existing uncommitted/untracked work. Those working-tree source bytes, not HEAD alone, were inspected. The evidence pack records their identities. No current remote runtime was inspected during this WI.

| Evidence / owner | What it establishes | What it does not establish |
|---|---|---|
| [October28 WI010: retained local evidence](../../../docs/registry/git-checkpoint-20261106.md#retained-evidence-limits), native observations owned by MA | One scheduled dispatch at 22:46 KST; 75,030,120 base-source bytes / 332 files; SQL 43 tables / 25 rows; 402 snapshot members / 333 files; retrieved SQL/manifest hashes and inventory; COMPLETE and preserved cold | `restore_verified=false`; no full media retrieval, new isolated restore, full secrets recovery or unattended service |
| Same WI010 final addendum, MA | App/DB/timers inactive, original firewall restored, key removed, logout and SSH agent Stopped/Disabled; physical repository 67,198,976 bytes (64.0859MiB), growth 28,672 bytes | Final invoice, independent mail receipt, 50GB throughput or effective production limits |
| [October26 WI012: retained local evidence](../../../docs/registry/git-checkpoint-20261106.md#retained-evidence-limits), MA receipts integrated by CR | Earlier bounded exact-snapshot isolated restore and scoped bytes/DB verification passed | Full configuration/key recovery, a different target or a future snapshot's restoration |
| Source and old design | Existing guards and proposed resource envelope | Deployment, 50GB load, live stop/resume, public opening or new permission |

The old October10 plan contains historical NOT RUN statements. Use the later dated evidence above for completed bounded operations; retain the old document rather than rewriting history. [REQ003: retained local evidence](../../../docs/registry/git-checkpoint-20261106.md#retained-evidence-limits) still holds its independent TEST mail task. A future source-host message cannot close it.

## Units, tariffs and scenario arithmetic

Assume 50 decimal GB = 50,000,000,000 bytes = 46.5661GiB of unique payload. This is a scenario, not a measured present dataset. It excludes SQL, metadata, old retained bytes, failed uploads and format overhead. Do not assume compression or a fixed deduplication ratio. MP3 and compressed files may save little.

Public tariffs checked 2026-10-28: storage 20KRW/GB/month excluding VAT, hourly weighted monthly average; add 10% VAT in these illustrations. Below 1GB is a free threshold, not a 1GB deduction for a 50GB store. External outbound first band is 60KRW/GB excluding VAT; API fees are temporarily zero. These are public terms, not a fresh account invoice. [Official pricing](https://www.iwinv.kr/storage/obj)

Inbound is free and the documented IDC endpoint excludes eligible internal traffic; confirm actual route/account treatment before any paid-scale run. Off-provider downloads are external. Do not relabel a generic internet path as internal. [Official object documentation](https://docs.iwinv.kr/service/storage/object-storage/object-overview/)

For a 30-day month starting at day 0, additions at days 7, 14, 21 and 28 remain for 23, 16, 9 and 2 days respectively. Baseline is present for the whole month; an initial midmonth upload has a smaller first invoice. Table values are payload-only, before overhead and existing unrelated retained bytes.

| Scenario | End stored payload | Monthly average | Storage incl. VAT, rounded up | Meaning |
|---|---:|---:|---:|---|
| First 50GB baseline held all month | 50GB | 50GB | 1,100KRW | First upload transfers approximately the unique payload plus overhead |
| Daily captures, unchanged payload | 50GB | 50GB | 1,100KRW + changing SQL/metadata | New snapshots do not necessarily upload another 50GB; local full reads remain |
| Add 1GB weekly at the four stated times | 54GB | 51.6667GB | 1,137KRW | End-of-month run-rate is 1,188KRW/month, not the first month's average |
| Replace all 50GB at day15, keep old data | 100GB | 75GB | 1,650KRW | Next full month at 100GB costs 2,200KRW before overhead |
| Replace all 50GB weekly, retain all versions | 250GB | 133.3333GB | 2,934KRW | Next full month at 250GB costs 5,500KRW; retention is material |

Formulas: `average = sum(stored_GB * hours_at_that_size) / month_hours`; `storage_KRW = ceil(average * 20 * 1.10)`. Weekly additions: `50 + (23+16+9+2)/30`; weekly replacement: `50 + 50*(23+16+9+2)/30`.

Actual billed volume also includes unique older chunks still referenced by retained snapshots, dump/release changes, snapshot/index/pack metadata, incomplete uploads and any repack peak. A proposed 10GB contingency held all month adds 220KRW; it is a planning allowance, not proof that overhead fits. The observed 28KiB increment on a 75MB trial cannot be scaled as a guaranteed ratio. Restic distinguishes processed logical bytes from repository additions. [Backup semantics](https://restic.readthedocs.io/en/stable/040_backup.html)

One actual external 50GB read costs about 3,300KRW incl. VAT at the first band; four cost 13,200KRW. A full integrity read, off-provider copy and restore can each create traffic. Budget actual transferred bytes, including repeated reads and partial retries, rather than counting only selected logical new files. These figures exclude server fees and destination capacity. The proposed ordinary budget below excludes a new full external read unless its exact operation and cost are explicitly approved and allocated within that budget; exceeding the approved total requires an additional cost decision.

## Server space and resource envelope

The nominal 100GB server disk is separate from object storage. Putting 50GB in the object repository does not free the original 50GB on the server. Use actual filesystem bytes/inodes and current usage before admitting growth; the nominal disk may not equal usable filesystem capacity.

For an illustrative usable 100 decimal GB filesystem, reserve is `max(20%,10GiB)=20GB`. The generic protocol/design maximum allowances for new dump 2GiB, metadata 0.25GiB, cache 1GiB and temporary files 1GiB total 4.5634GB. Therefore:

`50GB sources + existing OS/DB/releases/logs/old dumps + 4.5634GB next growth + other admitted growth + 20GB reserve <= 100GB`.

The native cold adapter is narrower: it currently caps the dump at16MiB (`COLD_DUMP_MEMORY_CAP`); the2GiB generic reserve is a conservative planning illustration, not a currently admissible native dump. Larger operational DBs need separate bounded-memory implementation and review.

Only 25.4366GB remains in this illustration for all existing non-source usage plus other growth. Count old retained dumps once as existing usage; do not subtract the current source twice. If a future approved implementation uses the generic2GiB dump ceiling,30 retained new dumps alone could add64.4245GB; the present16MiB path cannot accept such dumps. Local evidence/dump cleanup requires a reviewed exact-file policy; no cleanup is authorized here. Warn at 70% filesystem use or projected reserve breach, and require at least 10% free inodes. Do not make full media archives locally.

A 50GB source plus a separate 50GB restored copy cannot fit on this nominal 100GB disk with OS, DB and reserve. A later isolated restore needs a separately selected, measured destination. An unverified Drive quota or an unspecified disk is not capacity evidence.

Preserve the candidate whole-helper envelope: MemoryHigh768MiB, MemoryMax1GiB, MemorySwapMax0, CPUQuota100%, Nice10; MemAvailable at least1GiB. The cold DB has its own native unit (MemoryMax768MiB), outside the backup helper cgroup; account for both and the rest of the host. No dump/restic parallelism, ingest, encoder, prune or load-test overlap. These are source limits/initial allocations, not a measured 50GB memory requirement. The existing [resource baseline](resource-baseline.md) remains a candidate.

## Read I/O, time and the 30-minute implementation limit

`guarded_backup.py` hashes sources before and after capture and invokes restic with `--force`. `host_adapter.verify_host_receipts` also hashes the source inventory at preflight and again immediately before starting the owned DB. Consequently one execution entails at least five source-sized reads (four application inventories plus restic's read), approximately 250GB for a 50GB source; SQL, metadata and separate pre/final probes add more. Deduplicated upload is not deduplicated local read I/O.

| Pure throughput illustration | Time, excluding other work |
|---|---:|
| 50GB upload at 2MiB/s | 397.36min |
| 50GB upload at 10MiB/s | 79.47min |
| 50GB upload at 20MiB/s | 39.74min |
| 250GB local reads at 50MiB/s | 79.47min |
| 250GB local reads at 100MiB/s | 39.74min |
| 250GB local reads at 200MiB/s | 19.87min |

Formula: `bytes / (MiB_per_second * 2**20) / 60`. None is a measured rate. Restic reading and uploading can overlap, so adding these rows is not measured wall-clock duration. CPU hashing/encryption, metadata, DB dumps, control checks and retrieval add work. Record each phase separately before projecting an outage.

Current blockers are explicit: `ColdHost` permits only60–1800 seconds; `native_trial` requires `360 + ceil((source_bytes+metadata_bytes)/(1024*upload_kib)) + ceil(retrieval_bytes/(1024*download_kib)) <= seconds <=1800`. Even the 20MiB/s upload-only illustration exceeds1800 seconds. The static service template still uses1080 seconds; changing only a config or only that service would be insufficient. The assembler floor also omits the full hash cost, so passing it would not prove a window fits.

The adapter requires an already-cold baseline and preserves it. Its `resume` hook stops its owned transient DB; it does not restart the app. It is not a live daily backup implementation. No current code claim supports zero downtime, an automatic restart or a guaranteed 30-minute operating window.

A separate cost blocker exists: `native_trial.assemble` currently requires both observed/approved costs and configured costs to equal0 (`ZERO_COST_ONLY`). A paid-scale pilot therefore needs an explicitly scoped, independently reviewed paid-cost authority/assembler extension; changing numeric inputs alone is rejected. Preserve the existing zero-cost route.

Proposed measurement sequence: use a separately approved existing representative subset capped at1GB and one attempt, within the current 30-minute envelope, if fresh native size/rate/resource checks admit it. This is a scoped measurement capture, not a complete application recovery point; required recovery members must not be silently omitted from a full backup. No generated test data or new test upload is included, and the source allowlist must not expand implicitly. Record preflight hash, DB-start recheck, dump, prehash, restic, verify and cleanup times/bytes, cgroup peaks and effective rates. Next select at most5GB only if the conservative projection plus25% time margin fits and the previous attempt was reconciled. A failed cause is diagnosed locally before another finite attempt; a larger test is not automatic.

Only then propose the full50GB window, including actual cold duration, data-independent failure stops and operator availability. A two-hour first baseline is merely an option to assess, not an accepted window. Any enlargement must consistently review adapter, assembler, approval expiry, service timeout, owned DB timeout and proof freshness; it must not silently remove ceilings. If a daily30-minute outage cannot fit, keep automatic capture off and choose a longer explicit window or a separately designed consistent online-backup architecture.

## Conservative admission versus expected savings

The current dispatcher reserves each claimed attempt, including failures, at `existing + reserve + attempts * per_attempt_bytes`. Its minimum per attempt is `source_bytes + dump_bytes + metadata_bytes`; `source_bytes` already covers base sources plus dump. This deliberate extra dump allowance cannot be subtracted because the estimate predicts deduplication. Cost authority covers the whole finite horizon and includes retained baseline costs, requests, traffic and VAT.

Example solely for generic design sizing, not accepted by the16MiB native adapter:50GB fixed base, dump reserve2GiB, metadata0.25GiB makes the byte floor54.5634GB. Select60GB per attempt only if verified format/other overhead fits the remaining margin. With the last observed67,198,976 existing bytes, 10GB reserve and30 attempts, the bound is1,810.067198976GB. Pricing all of it for a full30-day month conservatively gives39,822KRW incl. VAT, before any external traffic. Weekly source growth requires an enlarged source bound and recalculation; this fixed50GB example cannot authorize54GB.

Thus the expected1,100–1,400KRW ordinary storage case does not make a30-attempt policy admissible under a proposed5,000KRW budget. Never reduce authority to expected changed bytes, reset a consumed ledger, omit failures, refresh old evidence timestamps or delete backups to force admission.

Keep the existing guard for finite initial trials. A later inexpensive unattended policy requires an independently reviewed accounting change: reconcile durable consumed claims with fresh provider retained-byte observations, preserve uncertainty as worst-case outstanding consumption, reserve a full next-attempt bound, account for storage accrued and remaining-horizon cost, and reject stale/unknown tariffs or observations. There is no current automated trusted observation issuer. If this cannot be implemented and proved narrowly, remain operator-supervised or explicitly accept the larger worst-case budget; do not promise a cheap daily service.

Application admission limits are not provider billing caps. Already retained data continues accruing storage charges after new captures stop. Before budget exhaustion, report options with time/cost impact; no automatic deletion, purchase or increased cost authority.

## Proposed operating decisions

These values are recommendations to select after review, not approved runtime settings.

| User decision | Recommended value and consequence | Alternative if unacceptable |
|---|---|---|
| Cadence and outage | Desired daily03:00KST capture, maximum30-minute routine outage, no catch-up; <=24h planned provider-copy RPO only after successful daily captures and measured fit. Initial baseline remains attended, duration chosen after1GB/5GB measurements. Until live stop/resume and evidence issuance exist, no unattended activation. | If30min is too disruptive, or measured time exceeds it, choose a longer named window or separate online consistency design. Weekly backup means up to7days planned source-loss exposure and is not a substitute for a24h requirement. |
| Ordinary cost and retention | Proposed5,000KRW/month incl.VAT incremental backup budget, excluding existing server fees. Warn at3,500KRW projected month cost or100GB retained; require actual+next-attempt+reserve <=125GB and <=5,000KRW before new capture.125GB held all month is2,750KRW storage; rest is finite operational headroom, not guaranteed allowance. Keep7daily/4weekly/3monthly +2prechange selected-ID union and protected verified/incident points. | Frequent complete replacements can break125GB or the cost bound; pause new capture and choose more budget or an explicitly reviewed retention change. No automatic pruning/deletion. The current30-attempt guard needs the accounting work above; these thresholds alone do not satisfy it. |
| Failure coverage and recovery | Start with source-host success/failure result mail plus provider port/resource monitoring. Accept the explicit gap for a missed backup while the host/port stays healthy. Defer independent-copy/full-DR completion to a separate scoped stage before claiming provider-loss recoverability. | If every missed backup or provider/account loss must be covered, require independent execution/receipt checking and an off-provider recoverable copy before activation. Windows is not mandatory; no host, disk or Drive purchase/access is implied. |

The125GB admission ceiling includes the next attempt and reserve. With the illustrative60GB attempt and10GB reserve, existing retained bytes must be at most55GB; this gate binds before the100GB standalone warning. Alert when the projected next attempt cannot fit, without waiting for100GB, and report the narrow remaining growth margin around a50GB baseline. These are application admission checks, not a provider spending cap.

Missing daily capture becomes attention at the end of its expected window and age warning at26h; neither proves independent detection. No new daily schedule should be enabled before the applicable remaining gates below. Retention is a selection proposal only. Per-run dump/manifest paths make default host+paths grouping unsafe for assuming7/4/3 across runs. Select explicit snapshot IDs by stable host/class, union calendar selections and protected points, then review deletion candidates. Forget does not itself reclaim chunks; prune can require extra space and locks. [Restic retention](https://restic.readthedocs.io/en/stable/060_forget.html)

## Reporting and monitoring design

Use three visibly distinct status fields: capture outcome, report delivery, and recovery/copy verification. A success mail states that capture and bounded metadata checks succeeded, while `restore_verified=false` and independent-copy status remain unchanged. SMTP acceptance is not recipient receipt. Keep detailed journal/receipt paths private and exclude secrets, addresses, snapshot IDs and source paths from public summaries.

Source-host reporter proposal: read original guarded state, result and dispatcher ledger through existing protected readers; write a durable result record first; send one fixed success/failure/HOLD summary for a stable run/event identity. Read-only reporting must not initiate capture or restart services. A local omission checker may report a missed window only while that host runs. SMTP reservation persists before sending; unknown outcomes stay HOLD, no automatic resend or claim deletion. Delivery failure does not invalidate an otherwise accepted capture; it is a separate result needing attention. Finite mail timeout and result-journal capacity must not stretch the capture's cold window. Keep test keys/stores/events and TEST wording separate from production.

Do not repurpose `alert_delivery.deliver`: it explicitly expects an independent host and returns NO_ALERT for a healthy report. Do not fake different machine IDs or weaken `monitor_runner`'s source/monitor distinction. A separate narrowly named `result_reporter.py` and disabled service is the minimal implementation; protected storage and low-level durability patterns may be reused without changing independent-monitor semantics. Validate any shared refactor against existing notifier tests.

Provider monitoring is a separate infrastructure signal, not backup correctness or full independence from the provider. Official docs describe default-OFF port monitoring at5-minute intervals with3 consecutive failures, and CPU/memory threshold monitoring. Public docs describe the console features as free and email/phone notification; exact account channel settings, charges and receipt remain unverified. [Monitoring overview](https://docs.iwinv.kr/service/monitoring/resource-port-monitoring/info-resource-port-monitoring/)

Before activation select an already-authorized reachable service port and check the provider probe source ACL. A successful TCP connection does not prove HTTP, login, backup or DB health. Do not expose MySQL or widen management access; a restricted443 rule may require a narrowly approved probe rule, while80 eligibility is only a future check. Preserve the intentional quarantine response. Maintenance suppression must be explicit, narrowly timed and recover automatically as designed; do not disable all monitoring to conceal expected app-off alerts. Provider tests and mail receipt require finite separately authorized actions, not an assumed successful console toggle.

The source can fail before writing a result or sending mail. A healthy port can coexist with a broken timer, SMTP failure or stalled backup; same-host omission checks and provider port monitoring do not close this gap. Existing independent status-transfer/checker work remains available but deferred, with REQ003 unchanged.

## Minimal next implementation and finite validation

The following is a future work chain, not work authorized by REQ004. Prepare and independently review local changes before asking for runtime presence.

| Step | Existing files / smallest new boundary | Focused proof |
|---|---|---|
| 1. Reporting only | Read `guarded_backup.py` state/receipt, `scheduled_dispatch.ledger_failure_code`, `backup_runtime`/`status_transfer` protected I/O. Add separate `result_reporter.py`, its focused tests and a disabled result service; preserve `alert_delivery.py` and `monitor_runner.py` independent behavior. | Synthetic COMPLETE/FAILED/UNKNOWN/missing receipt, preserved last-good, stable event dedup, ambiguous SMTP HOLD, private fields omitted, TEST separation, no backup/DB start or restart calls. |
| 2. Measurement and admission | `native_trial.py`, `host_adapter.py`, `backup_runtime.py`, `SCHEDULE.md` and disabled backup service. Add bounded phase/resource accounting; report native window/dump-cap mismatches before dispatch. A paid trial additionally needs explicit paid-cost authority/assembler support while preserving ZERO_COST_ONLY behavior for the existing route. Keep current finite guard and exact approved-file inventory. | Small representative calculation fixtures, timeout/cgroup/expiry and unchanged existing candidates. No fake native evidence or automatic proof renewal. |
| 3. Finite native exercise | Fresh reviewed package and exact target/byte/cost authority for at most1GB, one capture; separate TEST reporter invocation and recipient check. Independently verify current input scope, ledger, cold state and cleanup. | Measured times/bytes/resources, actual private filesystem behavior, one consumed claim, no catch-up, mailbox receipt versus SMTP acceptance. If failed, reconcile without backup replay. A5GB step or50GB step requires its own measured fit/finite scope. |
| 4. Operating policy, only if needed | `scheduled_dispatch.py`, `readiness_plan.py` and authenticated observation supply for retained bytes/accrued cost; separate reviewed live-stop/resume adapter if operating writers will run. Change no old adapter contract silently. | Budget preservation across failures/reboots, stale observations HOLD, proof expiry/boot changes, source allowlist versus evolving inventory, no reset or hidden auto-resume. Independent review before installation. |

Step4 is the larger unresolved engineering boundary and can remain deferred while supervised finite capture is used. Fixed source-member hashes, six-hour proof lifetime, no automated provider evidence and no live stop/resume mean a timer alone cannot work unattended. Do not convert one successful scheduled dispatch into month-long readiness.

For a later50GB run, exact source/restore target capacity, runtime window, full cost envelope, code pins, protected references and operator stop responsibility must be accepted together. On deadline, resource breach, unknown outcome or nonzero exit, stop owned helpers through the existing containment/cleanup, verify cold state, preserve snapshot candidates/claims/failures and prior last-good; do not resume writers under an active capture. An operator decision to reopen without a new recovery point requires its own authorized live-service procedure, currently absent.

Unattended activation additionally requires proven live consistency/recovery behavior, fresh observation issuance, durable accounting, reviewed local retention/capacity, working report receipt, selected infrastructure signal, and an explicit accepted monitoring gap or functioning independent monitor. No timer enablement, restart, secret retrieval, mail, deletion or purchase is performed by this plan.

## Deferred full recovery and independent copy

Capture inventory still lacks proof that every required runtime setting and retained encryption key can be recovered without this host or the original PC. Use [READINESS](../../../scripts/deployment/backup/READINESS.md) for the nonexhaustive protected-reference inventory: DB/JWT, all retained billing encryption keys and effective integration/service/TLS/proxy settings. Do not infer completeness from a current active key or existing password escrow.

Before claiming full recovery: select a new isolated target with enough media/dump/DB/temp/reserve capacity; restore one exact snapshot; validate hashes/permissions and DB/file consistency; prove full keyring access; deny live outbound integrations and suppress timers/schedulers before any workload startup. Application acceptance and actual RTO remain separate from importing SQL. A <=4h RTO is a rehearsal target, not a measured promise.

An off-provider copy requires separate custody/password recovery, destination capacity/quota, cost/transfer budget and exact-snapshot verification. A weekly successful copy yields up to7days planned provider-loss RPO; currently no bounded RPO is proven for provider/account loss. Provider object replication is not that independent copy. Restic copy/complete read semantics remain the reference, not evidence of a performed operation. [Repository operations](https://restic.readthedocs.io/en/stable/045_working_with_repos.html)

No Drive connection, external disk operation, new server, secret recovery or deletion has been requested or carried out in this WI.

## Public checkpoint evidence boundary

This public copy redirects unavailable local evidence links to the shared
checkpoint boundary. The original document bytes and detailed REQ/WI evidence
remain in private local custody. No runtime contract or source code changed.
