---
version: 1.1
last_updated: 2026-10-10
project: ATS
owner: SA
category: guide
status: draft
dependencies:
  - path: ../../../deliverables/user/REQ-20261010-ATS-005.md
    reason: Approved settings decision, not implementation or installation
  - path: ../../../deliverables/agent/WI-20261010-ATS-008-evidence-pack.md
    reason: Code pointers, unchanged-template evidence and limits
  - path: acceptance-capacity.md
    reason: Existing bounded target acceptance and stop procedure
  - path: backup-restore.md
    reason: Consistent backup and space gates; restic detail integrated separately
---

# Initial Resource Baseline

## 1. Decision and State

**Selected values for the 2 vCPU / 8GB / 100GB candidate are implemented locally,
not installed settings or a supported-user guarantee.** WI008 was design-only;
[WI011](../../../deliverables/agent/WI-20261010-ATS-011-evidence-pack.md) and
[WI012](../../../deliverables/agent/WI-20261010-ATS-012-evidence-pack.md) implement
admission, bindings, templates and validator under REQ006. On 2026-10-10 Windows,
[final PG review](../../../deliverables/agent/WI-20261010-ATS-014-evidence-pack.md)
found no new blocking local defect. Reviewed hashes identify dirty-source
candidates, not a deployed artifact. **The old JAR is unchanged: build/hash the
reviewed new source and prove active admission before new env/Hikari10 activation.
Never apply pool10 alone.** Target performance/effective values: NOT RUN; production HOLD.

Units: MiB/GiB are binary; provider GB units and actual Linux `MemTotal`/usable
disk must be observed. A nominal 8 decimal GB is about 7.45GiB, not 8GiB. Review
the budget if effective RAM is below 7GiB; do not silently remove reserve or buy
an upgrade. Preserve September Gmail/WAV/phone TEST success within its old scope.

## 2. Selected Settings

Paths are under `deploy/single-vps/`. Selected values now exist in source/candidates;
all target application and measurements are **NOT RUN**. The first column preserves
the pre-REQ006 comparison, not current values.

| Setting / historical pre-REQ006 template | Current implemented target-test candidate | Reason / remaining gate |
|---|---|---|
| JVM `-Xms128m -Xmx512m` | `-Xms256m -Xmx1024m` | Room for baseline plus two admitted image/document mutations; not permission for unlimited concurrent uploads. |
| `MaxMetaspaceSize=192m` | `256m` | Separate class metadata budget; include JIT/direct buffers, thread stacks and child FFmpeg outside heap. |
| App `MemoryHigh=1G`, `MemoryMax=1536M` | `MemoryHigh=2G`, `MemoryMax=3G` | One cgroup for JVM and encoder, not 3GiB of Java heap. Alert on pressure before hard-cap OOM. |
| `TasksMax=256`, `LimitNOFILE=4096` | Keep 256 / 4096 | Do not expand threads/processes or open-file budget without measurements. |
| MySQL buffer pool 256MiB | `innodb-buffer-pool-size=512M` | Shared-host starting point, not dedicated-DB autosizing; total mysqld is larger. |
| MySQL `max-connections=30` | Keep 30 | Hikari is one subset; leave slots for maintenance/background clients without making 30 a memory guarantee. |
| Hikari max 5; minimum/timeout unpinned | Max 10, min idle 2, connection timeout 5000ms | Modest nested-connection headroom; bounded wait is not a deadlock fix. Admission gate in section 4 is mandatory. |
| Tomcat max threads40 / connections128 / accept20 | Keep 40 / 128 / 20 | Media is synchronous; preserve bounded socket/thread queues while measuring real occupancy. |
| Audio worker enabled, poll5000ms, encode timeout300s | Keep one worker/JVM, 5000ms / 300s, FFmpeg `-threads 1` | Decoder/verification time is additional. No second writer/encoder during deployment/recovery. |
| Multipart100MiB/file / 120MiB request | Keep; explicit file-size threshold 0B and private temp location | Stream/spool audio, not heap. Body size limits are not a concurrent-upload limit. Verify resolver behavior. |
| Image10MiB and4096-square; certification50MiB aggregate | Product limits retained; section 4 admission implemented | Do not reduce valid inputs to pass a small heap. |
| General connection limits8/IP,32/shared | Retained; explicit applicable child-zone composition | Structural checks cover inheritance; native accounting/order remains NOT RUN. |
| Combined stream/download4/IP,16/shared | Keep shared media ceiling4/IP,16/global; separate classification below | Counts active requests, not people; same-NAT users share an IP budget. |
| Both media responses `512k` | Stream `256k`; original download `2m` | 256KiB/s gives fetch-ahead room for 128kbps; direct MP3 must also be tested. No cache/authorization change. |
| No download-only admission | At most2 global and1/IP original responses, within shared media ceiling | New zones/maps and negative tests implemented; hardware capacity unmeasured. |
| Media3r/s burst8; API10r/s burst40 | Keep per-IP starting values | Repeated Range/seek and shared NAT need acceptance; 429 is not proof of hardware shortage. |
| SMTP timeout properties absent | Connect5000ms, read10000ms, write10000ms | Explicit `mail.smtp.connectiontimeout`, `mail.smtp.timeout`, `mail.smtp.writetimeout` mappings and isolated binding tests implemented; real stalls NOT RUN. |

Finite mail timeouts cover connection/read/write phases, not a single total
deadline including DNS/retries. Mappings and validator checks are present;
verify each actual stalled phase separately on the approved target.
SMTP sending/credentials/firewall egress still require exact-target approval.

## 3. Memory and Backup Envelope

| Consumer | Planning allocation | Important boundary |
|---|---:|---|
| App including native decoder/FFmpeg | 3GiB hard cgroup cap; 2GiB pressure threshold | Includes 1GiB heap, 256MiB metaspace and remaining native/stack/JIT/encoder margin. Components do not each receive the full cap. |
| Entire MySQL process | 1.25GiB observation envelope | 512MiB buffer pool plus connections/temporary tables/log buffers/native overhead; not a mysqld hard limit. Investigate sustained RSS above this envelope. |
| Backup helper process group | 1GiB maximum starting allowance | restic plus dump/helper processes, not each process. WI013 candidate `MemoryHigh=768M`, `MemoryMax=1G`, CPUQuota100%, Nice10; not installed. |
| OS, Nginx, small monitoring processes | 0.75GiB planning allowance | Must be checked on selected image; agent/software additions consume this budget. |
| Available-memory reserve | At least1GiB | Above allocations sum6GiB; effective RAM above7GiB leaves additional reclaimable cache/margin. Do not count page cache twice. |

Initial restic capture uses the separately selected **cold consistent** procedure:
close admission and stop the application/encoder before the DB/files capture.
No first-run backup, prune, restore or check overlaps mixed-load characterization;
no two backup helpers run together. Budget still includes a backup allowance so
later intentional overlap cannot be assumed free. Restic index/cache memory
can exceed guesses; OOM/nonzero exit means failed/incomplete backup, not success.
Do not auto-raise the cap or delete recovery points. WI013 implements this group
envelope and synthetic guards; native sufficiency and all host hooks remain
unverified/unconfigured. Compatibility, retention, upload rate and disk/cache
choices remain subject to the backup plan and named-target acceptance.

Image inputs are fully read; a 4096-square four-byte raster alone is 64MiB and a
2048-square RGB output is 16MiB, before input/codec/output buffers. PDFs retain up
to 50MiB per certification request. Two admitted heavy mutations reduce this risk;
heap/GC measurements must still pass. Audio itself uses bounded application
buffers and file-backed responses; no invented 100MiB heap allocation per listener.

## 4. Implemented Admission and Target Gate

**Historical motivation retained:** increasing Hikari from 5 to 10 alone does not fix nesting.
An outer upload transaction has acquired a connection before journal
`REQUIRES_NEW` requests another. Ten outer callers can still consume all ten.
UI sequential upload, rates of 3/10r/s and Nginx media limits alone do not bound
simultaneous storage mutations or large multipart memory use.

Implemented per-JVM design: **2 storage-mutation requests globally, 1 trusted IP,
no waiting queue**, 429 with Retry-After2s. Registration precedes FormContentFilter
and security with the unchanged strict firewall. All unsafe methods in six domain
families are covered except exact read-only POST `/api/tracks/batch`; unrelated
reads are unaffected. Async timeout/error retains ownership until completion and
dispatch exit. The original requirements below are locally implemented/tested;
real container/proxy/mixed-load proof remains pending:

1. An application admission boundary before affected service transactions and
   before multipart materialization/image decoding. Classify every HTTP entry
   into Track, Album, Playlist, Notice, Question and CompanyCertification storage
   writes/replacements/deletes, including non-multipart changes invoking cleanup.
   Do not acquire the permit inside `StorageMutationCoordinator`: that is too
   late, after the caller can hold its transaction connection.
2. Reject excess immediately with 429 and a short `Retry-After` (selected 2s),
   without automatic client retry or a business/DB/file mutation. Release permits
   only after request processing/transaction cleanup; cover cancellation,
   exception, disconnect and any async completion. Failed auth is not exempt
   from protection against pre-controller body allocation.
3. Add proxy pre-body multipart admission, same global 2 / per-IP 1 ceiling, as defense
   in depth; it cannot replace application classification because paths, methods
   and Content-Type can vary. Preserve current path normalization, identity trust,
   protected-file rejection, request-body and timeout checks.
4. Inventory all background/HTTP nested-transaction callers in that implementation
   WI. Keep one audio worker and one storage-recovery owner. Prove maximum nesting,
   acquisition order and permit release with focused tests; do not globally
   serialize unrelated metadata reads or disable required recovery to hide a risk.

Connection budget rationale, not a reservation guarantee: two admitted storage
requests can use 4 connections at outer+nested peak; reserve a planning 2 for the
single worker finalization and 2 for recovery/other background work, leaving 2
of 10 for ordinary API operations. Work phases may use less, but other payment/
HTTP transactions can compete; Hikari does not reserve these partitions.
WI011 records a bounded HTTP/background caller inventory and mock-JDBC cleanup
proof. Payment/audit/scheduler/startup consumers remain outside the gate.
**Residual starvation risk remains open until target mixed-work acceptance.**
If assumptions fail, reduce admitted mutations to 1,
refactor transaction boundaries under approval, or revise the budget explicitly.
Do not label the plan a proven pool-locking solution or buy CPU to solve it.

## 5. Transfer, Exposure and Operational Defaults

At 2MiB/s, an ideal 50MB original takes `50000000 / 2097152 = 23.84s`; 100MiB
takes 50s. Both exclude latency/contention. Historical 512KiB/s takes 95.37s/200s.
At 256KiB/s a 128kbps stream can fetch ahead; playback bitrate is not its wire rate.
With 14 stream responses plus 2 downloads, nominal shaped media payload can reach
`(14*256KiB + 2*2MiB)*8 = 62.91Mbps`; sustained for a day this is 679.48GB.
Non-media/other traffic adds more. **No cost cap, no promised bandwidth/user count.**
This outbound calculation is not the provider's inbound 50Mbps contract clause.

Both media types remain controller-authorized/file-backed, no public audio alias,
no `X-Accel-Redirect` change, no new cache or whole-upload proxy buffering.
Keep API/DB bound to 127.0.0.1, MySQL X disabled, external 22 from approved admin
IP only; 80/443 serve the approved audience/TLS after separate firewall work.
SSH management restriction must not become an accidental public website ACL.
Keep JVM/DB/scheduler Asia/Seoul; one app/worker owner; valid Java 17 and FFmpeg.

Keep journald 256MiB persistent / 64MiB runtime / 7 days as an initial host-wide
budget, with 1GiB keep-free. Verify MySQL binary/error logs and non-journal logs
separately; retention is not automatic deletion approval. Warn at disk 70% or
projected reserve breach; stop new uploads/backup capture before free space falls
below `max(20% of filesystem,10GiB)` plus next admitted work/capture needs.
Warn inode use 80%, stop below 10% free. Never delete originals to satisfy a gate.

## 6. Target Tests and Escalation

All tests below are **NOT RUN** and require named-target/test-data/byte-budget
approval. No real payment, unrestricted mail, expensive auto-retry or production
DB modification is implied. Reuse [bounded stages](acceptance-capacity.md#3-mixed-load-proposal-and-byte-budget):
initial normal mixed stage10min/1GiB, stop admission with room for in-flight bytes.
Different approved source IPs are required; deliberate429 probes are separate.

| Test | Named condition and pass requirement |
|---|---|
| BASELINE | Effective Java/FFmpeg/MySQL versions, property/cgroup values and `MemTotal`; no worker duplication; startup and idle heap/RSS fit budget. |
| MIXED-A | 20 active test clients: up to12 listeners,2 original transfers,6 browsing; one operator sequential WAV upload and at most one other heavy mutation. Metadata p95<=750ms; start/seek p95<=2s; no unexpected429, pool timeouts, OOM or unauthorized bytes. Not20 simultaneous streams. |
| MUTATION-2 | Two concurrent near-cap audio/image or50MiB document requests plus worker/recovery; record connection-acquire latency, peak heap/RSS, queue drain. Selected acquire p95<200ms, no5s acquisition timeout; validate originals/derivatives and transaction outcomes. |
| ADMISSION-3 | Third storage mutation from distinct IP rejected429 before body allocation/transaction; second from same IP rejected. Repeat exception/disconnect and normalized route variants; permits return to zero with no writes by rejected requests. |
| DOWNLOAD-2 | Two50MB originals while seeking/listening; target completion<=45s under normal test conditions, correct bytes; third global/second same-IP transfer predictably429 then recovers. This is a selected acceptance goal, not measured speed. |
| MAIL-STALL | Inject connect/read/write stalls using an approved non-delivering fixture; each observed phase fails within selected timeout+5s tolerance, threads recover. DNS/total deadline separately observed, not assumed. |
| BACKUP-COLD | Application/encoder stopped, one1GiB-budget helper, separately bounded bytes/time; complete restic capture and isolated restore per WI009, actual resource/time receipt. No live-load overlap in first acceptance. |

Immediate stop/escalation: any disclosure, unintended effect, OOM/restart or
pool acquisition timeout; CPU>=90% for2min; available RAM<1GiB for1min;
app cgroup>=90% of3GiB for1min; continued swap growth; normal error rate>=1%
for2min or metadata p95>2s for2min; queue oldest>5min or failure to drain after
admission stops; disk/inode/approved byte/time limit. For sustained mysqld
RSS>1.25GiB, pause load growth and revise the whole budget. Bulk catalog ingest
may need a separately approved longer queue window, never relabel a failed test.

## 7. Implementation Map and Rollback

- Local implementation: WI011/012 source bindings/admission, service/env/MySQL
  candidates, contract/validator, split media/download and multipart zones;
  SE/MA Java105 and settings39 PASS; PG independently Java57/settings39 PASS.
- Preserve security path/identity rules, same-origin/TLS, journal semantics and
  existing approved hashes as historical evidence. Re-run static/synthetic
  validation before named-target installation; new results supersede, not erase.
- Runtime installation, restic execution, DB work and target tests need their own
  exact scope. No cost or capacity upgrade is authorized by this plan.
- Rollback before adoption means retaining the prior runtime and withholding these
  candidates. Source rollback needs approved owned inverse hunks; never reset
  unrelated work or retain pool10 while disabling admission.

## Related Documents

- [Configuration](../CONFIGURATION.md): Current source/env mappings.
- [WI015 integration](../../../deliverables/agent/WI-20261010-ATS-015-evidence-pack.md): Receipts and checks.
- [WI008 historical design](../../../deliverables/agent/WI-20261010-ATS-008-evidence-pack.md): Original selection checkpoint.
