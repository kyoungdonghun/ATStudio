---
version: 1.0
last_updated: 2026-10-10
project: ATS
owner: SA
category: work-summary
status: stable
dependencies:
  - path: REQ-20261010-ATS-004.md
    reason: Approved read-only purchase due diligence
  - path: ../agent/WI-20261010-ATS-004-evidence-pack.md
    reason: Source identity, commands and reproducible evidence
---

# WI-20261010-ATS-004: Code-Based VPS Sizing

## Verdict

**The 2 vCPU / 8GB RAM / nominal 100GB disk candidate is a reasonable starting
point for small public operation, conditional on target acceptance. No source
finding establishes an unavoidable paid CPU/RAM upgrade before purchase.** This
is not a measured capacity result or a guarantee of any concurrent user count.
The initial configuration can impose smaller limits than the purchased machine.
Catalog format, local backup workspace, concurrent uploads and download volume
are more important sizing variables than registered member count.

Source review complete, 2026-10-10, Windows/local, owner SA. Named operating
target: **NOT RUN**. No code, existing document, database, running service,
cloud setting or purchase was changed. MA owns provider verification and WI007
integration; PG/TR own their security and general-guidance reviews.

## Current Evidence and Runtime

- Checkout: `main`, `58b1011dad796017e47dbcc82a4225ca449bcdf6`; pre-existing
  dirty/untracked work preserved. This is source identity, not deployed identity.
- Existing September 9 TEST evidence remains valid: 22 original/128000bps MP3
  pairs, bounded batch/retry/seek/resume and the later user-assisted phone
  download-save/open-play follow-up. The recorded 294-second batch observation
  is not a throughput benchmark. See [WI028](../agent/WI-20260909-ATS-028-evidence-pack.md).
- Existing September 8 Gmail delivery is credited; this task neither reads
  private mail settings nor tests the new host. See [current entry](../../docs/index.md#current-decision-entry).
- October 3 QA's 30/30 PASS is template/synthetic validation, not Linux or VPS
  performance. See [WI005](../agent/WI-20261003-ATS-005-evidence-pack.md).

| Layer | Actual requirement / observed version | Sizing significance |
|---|---|---|
| Backend | Java 17, Spring Boot 4.0.8, servlet MVC/JPA, Gradle wrapper 9.3.0 | One JVM; build outside the production host when possible. Gradle's test heap is not the service heap. |
| Built backend dependencies | Existing JAR: Spring Security 7.0.7, Spring MVC 7.0.9, Tomcat 11.0.24, Hibernate 7.2.24.Final, HikariCP 7.0.2, JDBC driver 9.7.0 | AGENTS' Security 6 label is stale. JDBC driver version is not the MySQL server version. |
| Database | MySQL 8.x; 43 current entity source types; exact target version/schema not observed | No Oracle requirement. DB storage contains metadata, not audio file blobs. Table count does not measure RAM or live DB bytes. |
| Frontend | Lockfile: React 18.3.1, Vite 6.4.3, TypeScript 5.6.3, Zustand 5.0.11 | Nginx serves the built SPA. An always-running Vite/Node server is not required by this deployment design. |
| Audio | External FFmpeg with `libmp3lame`; exact target native version unpinned | CPU and child-process memory must be included; Java Sound analysis is additional work, not the encoder. |
| Storage | `LocalStorageService`, local public/private roots; audio protected through controllers | No active S3 SDK implementation. Simply moving media to Object Storage would not work with local `getFile()`/atomic promotion assumptions. |

Local metadata observations: executable JAR 70,293,748 bytes; frontend dist
143 files / 1,194,351 bytes; repository `uploads` 122 files / 619,625,312 bytes.
Audio directory contains 49 MP3 files / 152,565,724 bytes and 42 WAV files /
387,484,428 bytes. These are physical TEST/fixture files, **not active catalog
counts or production demand**; no media content or private-root content was read.
The JAR SHA-256 matches WI028's historical tested JAR, not proof of a running
process or newly built release. A production DB size was not measured.

## Workload and Bottlenecks

| Finding | Current source evidence | Consequence / action before invitations |
|---|---|---|
| WAV conversion exists and is bounded | `service/audio/FfmpegAudioEncoder.java:20,50-74`; `TrackAudioProcessingWorker.java:37,58,85-101` | One private scheduled worker per JVM, one encoder thread, CBR 128k, default encode timeout 300s, output budget at most 256MiB. Keep one application/worker owner. |
| Whole audio is not held in an application byte array | `AudioAnalysisService.java:21-24,53,137-167`; `LocalStorageService.java:89-124,163-173`; `controller/TrackController.java:109-162` | Audio analysis uses small read buffers and a bounded waveform accumulator. Storage copies streams; playback/download return file-backed Resource/ResourceRegion, not an audio byte array. Codec/framework/native allocations still require measurement. |
| Upload and conversion are not the same operation | `TrackService.java:65-99`; `TrackAudioProcessingWorker.java:90-95,128-143` | Initial full audio analysis is synchronous in the upload request; the worker analyzes the original again, invokes FFmpeg, then decodes/verifies the MP3. The 300s bound covers the native encode wait, not every analysis stage or the entire job. |
| The encoder does not globally lock playback | `TrackAudioProcessingTransactions.java:24-69`; `TrackService.java:159-174` | Claim/finalization use short transactions, encoding occurs outside them. Playback reads the stored derivative, not live transcoding. Shared CPU/disk/DB contention can still slow playback. |
| The queue has a deliberate inter-job delay | `TrackAudioProcessingWorker.java:58-65`; `application.yml:80` | Approximately sum(job times) + `(N-1)*5s` for an already available batch, plus initial poll timing. 500 jobs incur about 41m35s of inter-job gaps even before processing time. This is queue latency, not 500 simultaneous encoders. |
| 20-file selection is not 20 concurrent encodes | `frontend/src/pages/creator/TrackUploadPage.tsx:268-306`; `ValidationConstants.java:67` | UI sends files sequentially. Multiple browsers/admin sessions remain possible; frontend sequencing is not a global upload concurrency limit. |
| 128kbps is not universal for every stored audio | `entity/Track.java:128-143,199-207`; `TrackService.java:92-94,186-200` | New WAV gets a derivative; direct MP3 retains its own bitrate. Transfer estimates below apply to WAV-derived 128kbps listening only. |
| Current heap is small relative to bought RAM | `deploy/single-vps/systemd/atstudio.service:20,28-30` | Heap 512MiB, metaspace cap 192MiB, cgroup high 1GiB/max 1.5GiB, tasks 256. The cgroup also includes FFmpeg. 8GB host RAM does not override these caps. No measured heap/RSS baseline yet. |
| Non-audio uploads can allocate whole files/images | `CanonicalImageService.java:35-37,69,138-159,186-190`; `CompanyCertificationService.java:303-323,369-400,413-415` | Thumbnail input up to 10MiB is read whole; a 4096-square image can need about 64MiB for one 4-byte raster plus 16MiB for a 2048-square RGB destination, input and codec buffers. Exact raster allocation varies. Certification PDFs are held in memory, up to 50MiB aggregate per request. Several concurrent large requests can exhaust 512MiB despite host headroom. |
| Connection pool can stall before hardware is full | `TrackService.java:65-72`; `StorageMutationCoordinator.java:74`; `StorageMutationJournalService.java:38-51`; env example lines 9-12 | Hikari maximum 5 versus Tomcat 40 threads. Upload's outer transaction already queries a user; journal `REQUIRES_NEW` needs another connection while the outer transaction remains. If all 5 are held by outer mutations, nested work can wait/time out. This is a code-derived risk, not a reproduced deadlock. Test simultaneous mutations or constrain admissions; review pool/transaction settings before assuming a CPU upgrade helps. |
| Slow media transfers occupy request resources | `TrackController.java:109-162`; nginx proxy template lines 29-33; HTTPS template lines 68-74 | Synchronous MVC file responses, no proxy cache/response buffering. Download entitlement transaction returns before body transfer, so a DB connection need not be held for the whole download; servlet/file/connection resources can be held much longer. |
| Deployment admission is smaller than vague user totals | HTTPS template lines 51-52,72-74; env example lines 9-12 | Media limits 4/IP and 16/shared server zone; server limits 8/IP and 32/shared zone. Zones interact, not independent pools. A 429 may mean selected limits, not VPS exhaustion; a shared-office NAT is especially relevant. |
| Search and integrity work grow with data | `TrackService.java:109-145`; `repository/spec/TrackSpecification.java:30-50`; `StorageIntegrityService.java:46-65` | Public search caps pages at 100, but contains-search, joins and sorts need target query profiling as catalog grows. Integrity inspection loads each repository's full entity list; do not infer unlimited startup scalability from bounded issue output. |

Java paths above are relative to `src/main/java/com/atstudio/atstudio/` unless
otherwise qualified. These are risks and tuning gates, not instructions to
modify the approved templates now. Keep finite mutation concurrency initially,
verify large image/certification cases as well as audio, and review heap plus
cgroup together. Increasing only `-Xmx` can worsen cgroup pressure. Likewise,
raising Hikari alone without reserving MySQL connections is not a complete fix.

The MySQL candidate has only a 256MiB InnoDB buffer pool and 30 max connections;
the buffer pool is not total mysqld RSS. A provisional 8GB-host planning envelope
can reserve roughly 2-3GiB for JVM/native/encoder, 1-1.5GiB for total MySQL,
0.75-1GiB for OS/proxy/agents, leaving the remainder for filesystem cache and
free margin. This is a **budget proposal**, not an applied configuration or
minimum requirement; choose compatible values after measuring effective RAM.
With 2 vCPU, simultaneous encoding and Java decode/upload work are the likely
CPU contention test. A separate DB/worker server is not established as necessary.

## Disk: 500 Tracks Is Not a Single Size

Decimal units: MB = 1,000,000 bytes; GB = 1,000,000,000 bytes.
MiB/GiB use powers of 1024. For uncompressed PCM and a derived MP3:

```text
WAV bytes ~= sample_rate * (bit_depth / 8) * channels * duration_seconds
MP3 bytes ~= 16000 * duration_seconds
catalog bytes ~= track_count * (WAV bytes + MP3 bytes)
```

Headers, thumbnails, private documents, DB, pending replacements and backups
are excluded below. Audio upload limit is 100MiB/file, not 100 decimal MB.

| PCM format / duration | Original MB/track | MP3 MB/track | 500 pairs GB | 500 pairs GiB |
|---|---:|---:|---:|---:|
| 44.1kHz / 16-bit / stereo / 1 min | 10.584 | 0.960 | 5.772 | 5.376 |
| Same / 3 min | 31.752 | 2.880 | 17.316 | 16.127 |
| Same / 5 min | 52.920 | 4.800 | 28.860 | 26.878 |
| 48kHz / 24-bit / stereo / 3 min | 51.840 | 2.880 | 27.360 | 25.481 |
| Same / 5 min | 86.400 | 4.800 | 45.600 | 42.468 |
| 96kHz / 24-bit / stereo / 3 min | 103.680 | 2.880 | 53.280 | 49.621 |

96kHz/24-bit/stereo at 5 minutes would be 172.8MB for the WAV and is rejected
by the current per-file cap; more server disk does not change that product limit.
Low-rate PCM can produce a derivative larger than its original; 100MiB original
size is not a universal total-pair bound. FFmpeg's separate 256MiB output cap
still applies. Measure the intended catalog's actual formats before bulk import.

Nominal 100 decimal GB is about 93.13GiB before formatting, if the provider uses
decimal disk units. Leave OS/packages/releases/DB/logs/temp space and the agreed
operating reserve. During upload, servlet multipart temp plus staged original
may coexist. Atomic promotion avoids a second final-file copy, but replacement
keeps the old pair while new input/output is pending; retained failed work can
grow disk. Reserve separately for these peaks, not only final active tracks.

**Backup workspace is a pre-purchase design decision for a large opening catalog.**
The current [runbook](../../deploy/single-vps/runbooks/backup-restore.md#2-inventory-and-space-gate)
does not promise that live media + full plaintext archive + encrypted archive
fit. Illustration: nominal 100GB minus 15GB other allocations minus 20GB reserve
leaves 65GB. If three near-equal media copies coexist, only about 21.67GB of live
media fits, before additional dump/temp/retention costs. The 28.86GB and larger
catalog scenarios fail this illustration even though live files alone fit.
No WAV compression saving or deduplication is assumed. Approve a bounded
streaming/off-host capture design or a smaller opening catalog before choosing
paid expansion; backup implementation/restore is still NOT RUN.

## Transfer and Realistic Scenarios

For 128kbps payload: 16,000 bytes/sec = 0.96MB/min = 57.6MB/hour.
An ordinary listening session is not the same as a persistent concurrent
transfer: browsers can fetch ahead and seek. Cold assets, repeated fetches,
direct higher-bitrate MP3s, protocol overhead, failed downloads and backups add
traffic. Re-downloads remain real bytes even when entitlement quota is not
charged again (`DownloadService.java:45-96`).

| Hypothetical visits/day | Listening minutes/visit | 50MB originals downloaded/day | Media GB/day | Media GB/30 days |
|---:|---:|---:|---:|---:|
| 50 | 15 | 10 | 1.22 | 36.6 |
| 200 | 20 | 50 | 6.34 | 190.2 |
| 500 | 30 | 100 | 19.40 | 582.0 |
| 1,000 | 30 | 300 | 43.80 | 1,314.0 |

These are workload scenarios, not visitor forecasts or proven CPU capacity.
For comparison only, MA reported the live selected-plan tooltip on 2026-10-10
as 33GB/day and 1,000GB/month. SA did not independently browse that screen.
The public-page 30GB/day and generic 600GB table conflict, and partial-first-month
allowance remains unresolved; MA owns provider reconciliation. Use 1,000GB only
as an illustrative full-month allowance, never a cost cap. The last scenario
already exceeds it before non-media traffic; the 500-visit case leaves material
but finite headroom. Count remote backup traffic according to verified provider
rules, not an assumption that every backup transfer is free.

Current `limit_rate 512k` is approximately 512KiB/sec per response: an ideal
50MB download takes at least about 95.4s; 100MiB takes about 200s, before latency
or contention. This is a user-visible configuration limit, not proof of poor
VPS bandwidth. A 16-response media envelope could theoretically send about
67.1Mbps / 724.8GB/day at that cap continuously. It is **not** a monthly billing
ceiling. That outbound arithmetic must not be confused with the provider's
separate inbound 50Mbps contract condition. Exact Nginx admission and shaping
behavior remains a Linux target test.

## Decision Gates

| Stage | Required outcome |
|---|---|
| Purchase decision | No mandatory paid compute upgrade identified. Confirm intended opening catalog size/format and backup peak plan if using a large catalog; MA resolves provider allowance/overage exposure, PG resolves recovery/access readiness. |
| Before invitations | Exact Java/MySQL/FFmpeg service identity; fresh schema; one worker; bounded multi-upload and large image/certification tests; verify heap/cgroup/pool behavior, slow transfer admission and download usability; complete backup-space and restore gates. |
| Small public operation | Run the approved mixed workload with browsing/search, seek, original download and sequential WAV upload together. Record offered/admitted concurrency, p95, errors/429, CPU/steal, heap/RSS/GC, available RAM, I/O, queue age and bytes. Existing [capacity runbook](../../deploy/single-vps/runbooks/acceptance-capacity.md) provides bounded stages and stop criteria. |
| Deferred scaling | First tune limits and schedule bulk ingest/backup away from peak traffic. Only measured CPU, memory, I/O, storage or transfer pressure justifies a paid move. Object Storage/CDN or additional replicas need architecture work; local files and single-worker recovery cannot simply be duplicated behind a load balancer. |

Report a shortfall immediately with evidence, impact, non-paid mitigation,
additional-cost option and decision needed. This review approves neither target
testing nor public release. WI004 is complete and unblocks MA's coordination of
WI-20261010-ATS-007 after the other evidence streams are ready.
