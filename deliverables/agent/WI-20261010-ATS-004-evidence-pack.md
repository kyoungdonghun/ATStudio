---
version: 1.0
last_updated: 2026-10-10
project: ATS
owner: SA
category: evidence-pack
status: stable
dependencies:
  - path: WI-20261010-ATS-004-handoff.md
    reason: Approved bounded source-sizing assignment
  - path: ../user/REQ-20261010-ATS-004.md
    reason: Parent investigation approval and exclusions
  - path: ../user/WI-20261010-ATS-004-summary.md
    reason: Detailed findings, calculations and decision gates
---

# Evidence Pack: WI-20261010-ATS-004

## Summary

Completed read-only code/resource sizing for the single-VPS candidate. See the
[substantive report](../user/WI-20261010-ATS-004-summary.md) for workload tables
and risk classification. No unavoidable pre-purchase CPU/RAM upgrade identified;
heap/pool/admission limits and backup workspace need explicit target validation.
**No user-count or throughput guarantee. Operating target NOT RUN.**

## Scope / DoD Check

- [x] Current branch/HEAD, dirty-work boundary and latest relevant evidence checked.
- [x] Actual backend/frontend/native dependencies distinguished from overview labels.
- [x] WAV-to-128kbps implementation, worker bounds and local file paths inspected.
- [x] Whole-file buffering, CPU work, DB connection nesting and synchronous media examined.
- [x] Disk/temporary/backup peaks and realistic transfer scenarios calculated with units.
- [x] Purchase decision, pre-invitation tasks and later scaling separated.
- [x] Only two assigned new documentation files written using `apply_patch`.
- [ ] Target performance, effective Linux configuration and production acceptance: NOT RUN.

## Reference Documents (Tier 0-2)

Injected by MA as content summaries and explicit file pointers, followed within
the saved handoff. Role SA; source review and bounded evidence documentation.

| Tier | Document | Reason |
|---|---|---|
| 0 | `docs/standards/core-principles.md` | Scope approval, Korean conversation / English documentation |
| 0 | `docs/standards/development-standards.md` | Current layered Java/runtime sources, preservation |
| 0 | `docs/standards/documentation-standards.md` | Metadata, pointers, two deliverables |
| 0 | `docs/standards/glossary.md` | Public Listening versus Official Download |
| 1 | `docs/policies/security-policy.md` | No secrets, private configuration or user data access |
| 2 | `docs/index.md#current-decision-entry` | Latest bounded completion, not stale defaults |
| 2 | `docs/design/runtime-storage-operations.md` | Audio queue/local storage and historical verification |
| 2 | `deploy/single-vps/README.md` | Single-host candidate and unexecuted target boundary |
| 2 | `deploy/single-vps/runbooks/acceptance-capacity.md` | Mixed-load admission and acceptance/stop proposals |
| Context | WI028 September 9 and WI005 October 3 Evidence Packs | Existing TEST media PASS and template-only QA respectively |
| Context | REQ004 and WI004 handoff | Exact owned paths, exclusions and Blocks 007 |

## Identity and Metadata Observations

Verification date/environment/owner: **2026-10-10 / local Windows checkout / SA**.
This is separate from artifact write dates and September TEST verification.

| Observation | Result |
|---|---|
| Branch / HEAD | `main...origin/main` / `58b1011dad796017e47dbcc82a4225ca449bcdf6`; no fetch |
| Existing tracked dirt | AGENTS, CLAUDE, README, docs index/payment index/kickoff prompt and reconstruction README; preserved |
| Existing untracked work | Prior REQ/WI deliverables, deployment bundle, guides/scripts/PDF and concurrent review packets; preserved |
| Backend archive | `build/libs/ATStudio-0.0.1-SNAPSHOT.jar`, 70,293,748 bytes, dated September 9 |
| Backend SHA-256 | `E7802311EE2F25762A8F601D5F9A9CAA2954DFB817D904DB351B0E0DB7B72784`, matches WI028 historical runtime archive hash |
| Archive entry versions | Boot 4.0.8; Security 7.0.7; MVC 7.0.9; Tomcat 11.0.24; Hibernate 7.2.24.Final; HikariCP 7.0.2; Connector/J 9.7.0 |
| Frontend locked versions | React/ReactDOM 18.3.1; Vite 6.4.3; TypeScript 5.6.3; Zustand 5.0.11 |
| Entity source count | 43 types with line-start `@Entity`; no DB queried |
| Frontend dist | 143 files, 1,194,351 bytes; freshness/runtime matching not established |
| Repo uploads root | 122 files, 619,625,312 bytes; zero enumerated reparse files; metadata only |
| Audio MP3 group | 49 files, 152,565,724 bytes; min 10 bytes / max 17,863,782 bytes, including fixtures |
| Audio WAV group | 42 files, 387,484,428 bytes; min 112,044 bytes / max 104,853,504 bytes |

The tiny MP3 fixture demonstrates why file count is not playable catalog count.
Neither DB contents nor private runtime configuration was inspected. The local
`private-uploads` directory was not present in the initial metadata listing;
no inference about the retained runtime's external private root follows.

## Evidence Pointers

Java source prefix below: `src/main/java/com/atstudio/atstudio/`.

| Pointer | Supported observation |
|---|---|
| `build.gradle:4,13,30-58`; `gradle/wrapper/gradle-wrapper.properties:3` | Boot/Java/Gradle, JPA/MVC/mail/audio dependencies; S3 dependency commented out |
| `frontend/package-lock.json`; `deploy/single-vps/nginx/https.conf.template:53` | Locked frontend packages and static frontend serving |
| `common/validation/ValidationConstants.java:63-72`; `application.yml:35-37` | 100MiB audio, 10MiB image, certification 20MiB/file and 50MiB aggregate, multipart limits |
| `service/TrackService.java:65-99,159-174,177-200` | Synchronous upload analysis, WAV enqueue, stored-file playback and direct MP3 replacement |
| `service/audio/AudioAnalysisService.java:21-24,53,137-167,251-310` | Streaming decode, bounded waveform buckets; no full audio application array |
| `service/audio/FfmpegAudioEncoder.java:20,50-74,114-132` | CBR128, thread bound, file output cap, timeout and derivative rate selection |
| `service/audio/TrackAudioProcessingWorker.java:37,58-65,85-101,128-143` | Single worker, 5s default fixed delay, source/output analysis |
| `service/audio/TrackAudioProcessingTransactions.java:24-69` | Short claim/complete/failure transactions; encoding outside those methods |
| `entity/Track.java:128-143,172-207` | Derivative versus direct listening; generation/claim readiness; retained pending files |
| `service/storage/LocalStorageService.java:89-124,133-143,163-173` | Stream-to-stage, atomic move and file-backed resource |
| `controller/TrackController.java:109-162` | Synchronous Resource/ResourceRegion response including full/single-Range transfer |
| `service/DownloadService.java:45-96` | User/track lock before entitlement, existing license skips first-download quota; resource returned before response bytes |
| `service/image/CanonicalImageService.java:35-37,69,138-159,186-223` | Whole encoded input, full decoded raster, bounded resize and output byte array |
| `service/CompanyCertificationService.java:303-323,369-400,413-415` | Validated input list retains PDF byte arrays; image canonicalization also allocates |
| `service/storage/StorageMutationJournalService.java:38-63`; `StorageMutationCoordinator.java:74` | REQUIRES_NEW nested journal operations after caller transaction started |
| `service/storage/StorageIntegrityService.java:46-65`; `repository/spec/TrackSpecification.java:30-50` | Whole-entity audit scans and contains-search/subquery growth |
| `deploy/single-vps/env/atstudio.env.example:9-12` | Tomcat max40, max connections128, accept20, Hikari max5 |
| `deploy/single-vps/systemd/atstudio.service:20,28-30`; `mysql/atstudio.cnf:10-11` under that bundle | Heap512MiB, metaspace192MiB, cgroup1/1.5GiB; DB30 connections, buffer pool256MiB |
| `deploy/single-vps/nginx/https.conf.template:51-52,68-74`; `proxy.conf.template:29-33` | Shared admission limits, 512KiB/s shaping, cache/buffering off |
| `frontend/src/pages/creator/TrackUploadPage.tsx:268-306` | Sequential requests within one UI batch, not global concurrency enforcement |
| `frontend/src/api/downloads.ts:152-170`; `frontend/src/store/playerStore.ts:7,137` | Browser Blob download versus Audio stream; client Blob does not mean server whole-file heap |
| `deploy/single-vps/runbooks/backup-restore.md:60-83` | Coexisting live/dump/plaintext/encrypted workspace and reserve gate |

## Commands and Calculations

Read-only commands used: `git status --short --branch`, `git rev-parse HEAD`,
scoped `rg -n` / `rg --files`, `Get-Content` selected source/doc ranges,
`Get-ChildItem -File -Recurse` and `Measure-Object Length` on identified local
artifact/media directories, `Get-FileHash` for the known JAR, and
`[System.IO.Compression.ZipFile]::OpenRead` to list selected `BOOT-INF/lib` names.
No native media probing, application, build, install, DB query or load was run.

Frontend lock parsing used `ConvertFrom-Json -AsHashtable` because npm's root
package key is empty. An initial non-hashtable parse failed; its blank output
was discarded. A few broad path lookups named absent folders/wildcards and
were corrected by scoped existing paths; these were inspection errors, not
application failures. Metadata grouping was re-emitted as JSON to preserve
extension names hidden by mixed PowerShell table formatting.

Reproducible arithmetic, calculated with PowerShell numeric expressions:

```text
W = sample_rate * bit_depth / 8 * channels * seconds
M = 16000 * seconds
catalog_500_GB = 500 * (W + M) / 1e9
catalog_500_GiB = 500 * (W + M) / 1073741824
media_GB_per_day = visits * minutes * 60 * 16000 / 1e9
                   + original_downloads * 50e6 / 1e9
media_GB_per_30_days = 30 * media_GB_per_day
ideal_50MB_download_seconds = 50e6 / (512 * 1024) = 95.3674
ideal_100MiB_download_seconds = 100 * 1048576 / (512 * 1024) = 200
16_response_rate_Mbps = 16 * 512 * 1024 * 8 / 1e6 = 67.108864
16_response_GB_per_day = 16 * 512 * 1024 * 86400 / 1e9 = 724.7757312
three_copy_catalog_budget_GB = (100 - 15 - 20) / 3 = 21.6667
500_job_inter_job_gaps_seconds = 499 * 5 = 2495
```

Provider input attribution: MA's in-task message reports live hpa_2.8-g2 tooltip
33GB/day and 1,000GB/month; it also reports contradicting public 30GB/day and
generic 600GB figures. No provider page was independently researched by SA.
The summary uses 1,000GB solely for theoretical full-month scenario comparison.
First partial month, exact accounting and cost cap remain MA/provider questions.

## Tests and Limits

No product tests, new measurements, server probes, encoding or load executed.
Historical PASS results were read, not rerun. Link/metadata inspection and
whitespace checks on the two new reports are documentation checks only.
Scoped PowerShell validation passed (exit 0): both frontmatter blocks present,
zero trailing-whitespace lines and zero missing linked files. This check verifies
linked file existence, not Markdown anchor correctness. Git status shows only
the two owned paths as newly untracked within the scoped query. MA's
WI007 integration can run the repository-wide documentation validator once all
parallel outputs are present. No document-only result implies production PASS.

## Risks / Rollback

- CPU/heap/DB saturation, actual raster allocations, I/O and queue service time
  remain unmeasured. Conditions describe risks, not observed incidents.
- The 8GB candidate has headroom to review conservative application caps, but
  no tuning change, new library, separate service or paid upgrade is approved here.
- Catalog size and backup coexisting copies can invalidate nominal 100GB even
  when current TEST media occupies less than 1GB. Do not extrapolate TEST size.
- Only these two new reports were created. No operational rollback is required;
  any later removal of reports still follows explicit deletion approval.

## Follow-ups and Chain

WI004 source-sizing work is **complete**. It Blocks WI-20261010-ATS-007; MA
coordinates the prepared integration handoff after WI005/WI006/provider evidence.
Do not start target work or purchase based on this subagent result alone.
