---
version: 1.0
last_updated: 2026-10-10
project: ATS
owner: docops
category: work-summary
status: confirmed
dependencies:
  - path: REQ-20261010-ATS-004.md
    reason: Approved audit, not purchase authorization
  - path: ../agent/WI-20261010-ATS-007-evidence-pack.md
    reason: Evidence ownership and validation
  - path: WI-20261010-ATS-004-summary.md
    reason: Current code and sizing
  - path: WI-20261010-ATS-005-summary.md
    reason: Security and recovery gates
  - path: WI-20261010-ATS-006-summary.md
    reason: Official practice comparison
---

# WI-20261010-ATS-007: Final Pre-purchase Audit

## Decision

**Retain the 2 vCPU / 8GB / 100GB candidate for small public operation, subject to the gates below. No inspected finding makes a paid CPU/RAM upgrade, separate database server, CDN or load balancer mandatory before purchase.** This is the integrated audit assessment for MA's decision, not purchase authorization, measured capacity or public-release approval.

**The material caveat is disk and operating configuration, not simply visitor count.** A large opening WAV catalog plus local backup working copies can exceed 100GB. Small heap/pool/admission settings can constrain the application before the purchased machine is exhausted. Provider upload/partial-month/attack-cost terms remain unresolved. Do not automatically buy larger hardware or declare the bill capped.

## Evidence Boundary

- Verification: 2026-10-10 KST; SA inspected source/artifact metadata, PG inspected controls/templates, TR researched official practices, MA inspected the live unsubmitted console and provider pages. Docops consolidated those completed records; no independent target-host test occurred.
- Local identity: Windows, `main`, HEAD `58b1011dad796017e47dbcc82a4225ca449bcdf6`, with preserved pre-existing dirty/untracked work. This is not a deployed artifact identity.
- September 8 Gmail receipt/link success and September 9 WAV/128kbps/phone TEST success remain valid within their original scope. October 3 template/synthetic PASS is not Linux/VPS acceptance. See the [current entry](../../docs/index.md#current-decision-entry).
- No server exists from this application draft; no inquiry, purchase, new mail, DB operation, build or load test was performed by this audit. Stage B and public/LIVE operation remain pending.

## Actual Application and Sizing

The current design runs Nginx/static React, Java 17/Spring Boot 4.0.8, MySQL 8.x and external FFmpeg on one host. There is no required always-running Vite/Node server. Source stores audio as files, not DB blobs; Object Storage is a backup destination proposal, not an implemented primary-media backend. The existing JAR contains Spring Security 7.0.7, so the overview's Security 6 label is stale. These are source/archive observations, not installed server versions. [Code audit](WI-20261010-ATS-004-summary.md)

| Verified finding | Decision consequence |
|---|---|
| New WAV conversion uses 128kbps CBR, one worker per JVM and FFmpeg `-threads 1`. Existing direct MP3 can have another bitrate. | Keep one writer/encoder owner. Test mixed work; 128kbps describes output bytes, not CPU needed. |
| Audio analysis uses bounded buffers; playback/download are file-backed. Whole audio is not allocated as an application byte array. | Do not justify a RAM upgrade using a nonexistent whole-audio-buffer claim. Native/framework allocations still need measurement. |
| Image decoding and certification uploads can allocate complete buffers/rasters. Candidate heap is 512MiB; systemd max is 1.5GiB including FFmpeg. | Exercise concurrent large image/document uploads and tune heap plus process-group budget together. 8GB host RAM does not override these settings. |
| Hikari maximum 5 and nested `REQUIRES_NEW` can compete for connections during simultaneous mutations. | A code-derived, unreproduced contention risk, not an observed deadlock. Test/admit mutations deliberately; adding CPU alone is not a fix. |
| Candidate media limits are 4/client and 16/shared server; general limits 8/client and 32/shared server. Transfer shaping is 512KiB/s per response. | These are inactive initial admission settings, not 16 users or proven capacity. A 50MB download needs at least about 95 seconds at that cap. Tune usability and shared-IP behavior on target. |

Detailed code pointers, metadata and calculations are in the [SA Evidence Pack](../agent/WI-20261010-ATS-004-evidence-pack.md). The local fixture directory is about 0.62GB, not a production catalog or demand sample.

### Catalog and Backup Space

Decimal GB/MB below; examples exclude headers, images, DB and temporary/replacement files. Formula: `WAV = sample_rate * bit_depth/8 * channels * seconds`; 128kbps MP3 is approximately `16000 * seconds` bytes.

| 500 WAV + MP3 pairs | Live media GB |
|---|---:|
| 44.1kHz, 16-bit stereo, 3 minutes each | 17.316 |
| Same, 5 minutes | 28.860 |
| 48kHz, 24-bit stereo, 3 minutes | 27.360 |
| Same, 5 minutes | 45.600 |

Illustration, not an actual allocation: `(100GB - 15GB other use - 20GB reserve) / 3 coexisting media copies = 21.67GB live-media budget`. The three copies represent live files plus plaintext and encrypted backup archives. Therefore even the 27.36GB example may not fit this backup workflow although live media alone fits. Confirm intended catalog formats and peak working copies before bulk ingestion; prefer an approved bounded streaming/off-host backup design or smaller opening catalog before paid expansion. No compression saving is assumed. [Space gate](../../deploy/single-vps/runbooks/backup-restore.md#2-inventory-and-space-gate)

### Transfer Scenarios, Not Demand Forecasts

128kbps listening is approximately 0.96MB/minute. Downloads below average 50MB each. Values exclude site assets, retries, higher-bitrate direct MP3s, protocol overhead and applicable backup traffic; they establish neither concurrency nor CPU capacity.

| Visits/day | Listening minutes/visit | Original downloads/day | Media GB/30 days |
|---:|---:|---:|---:|
| 50 | 15 | 10 | 36.6 |
| 200 | 20 | 50 | 190.2 |
| 500 | 30 | 100 | 582.0 |
| 1,000 | 30 | 300 | 1,314.0 |

The 500-visit scenario leaves finite transfer headroom against the selected full-month 1,000GB display. The last exceeds it before other traffic: illustrative 314GB first-tier excess costs 24,178 KRW including VAT. This is arithmetic under stated assumptions, not a predicted invoice. Public listening and licensed re-downloads still consume real bytes.

## Industry-practice Comparison

The comparison is to authoritative design guidance, not a survey of competitors' private systems or a universal server-size standard. A single host trades lower cost for shared failure and maintenance downtime. [AWS guidance](https://docs.aws.amazon.com/lightsail/latest/userguide/understanding-lightsail-load-balancers.html) supports a staged approach; it does not validate this VPS's throughput.

| Keep initially | Required discipline / later trigger |
|---|---|
| Application and MySQL together | Explicit shared-memory budgets; do not copy dedicated-DB autosizing. Separate after measured contention or availability requirements. [MySQL](https://dev.mysql.com/doc/refman/8.0/en/innodb-dedicated-server.html) |
| Local protected media | Authorization precedes original download. Optional Nginx internal offload needs a separately reviewed implementation; a public storage alias is not equivalent. [NGINX](https://nginx.org/en/docs/http/ngx_http_core_module.html#internal) |
| External backups instead of a standby server | Define acceptable data loss/downtime, then retrieve and restore DB, media and settings/keys together. Backup success is not recovery proof. [Recovery testing](https://docs.aws.amazon.com/wellarchitected/latest/framework/rel_backing_up_data_periodic_recovery_testing_data.html) |
| No immediate CDN/LB requirement | Monitor latency, errors, bytes and saturation; expand only for an identified bottleneck or availability target. [Google SRE](https://sre.google/sre-book/monitoring-distributed-systems/) |

**Version check:** Oracle MySQL 8.0 entered Sustaining Support in April 2026, but Ubuntu documented 24.04 backported security fixes in August. Do not label all 8.0 installations unsupported or require a paid DB/automatic 8.4 migration. Verify target APT source, candidate and update policy. Ubuntu 24.04 defaults to Java 21; explicitly select the approved Java 17 runtime and verify FFmpeg/libmp3lame. [Oracle notice](https://www.mysql.com/support/eol-notice.html), [Ubuntu patch notice](https://ubuntu.com/security/notices/USN-8700-1), [TR lifecycle detail](WI-20261010-ATS-006-summary.md#version-gate)

## Selected Product and Real Cost

MA observed the following in the [unsubmitted confirmation form](https://console.iwinv.kr/instance/request/confirm). Login-dependent state can change; its privacy-cropped local evidence is recorded in the handoff.

| Item | Observed draft |
|---|---|
| Product/location | `hpa_2.8-g2`, KR1-Z03; one `atstudio-prod-01` |
| OS/resources | Ubuntu 24.04 LTS 64bit; 2 vCPU, 8GB RAM, NVMe 100GB; 10Gbps port |
| Security selections | `atstudio-admin-20261010` key; `atstudio-bootstrap` firewall; ICMP No |
| Optional selections | Script unused; no NAT gateway or additional block storage selected |
| Server price | 48,200 KRW/month or 1,780/day excluding VAT; 53,020/month or 1,958/day including VAT |

Public product pricing matches. One public/private IP and ELCAP are included for this plan. A 10Gbps port is not unlimited transfer or guaranteed application throughput. Power-off remains billable; resizing can reboot and changes billing. [Product terms](https://www.iwinv.kr/server/server)

**Traffic discrepancy:** the selected console tooltip says **33GB/day and 1,000GB/month**; the public Gen2 table says 30GB/day and 1,000GB/month, while generic text includes 600GB/month. Selected full-month 1,000GB is corroborated; exact partial-month allowance and 30-versus-33 remain unresolved. The older calculation example aggregates period usage less daily allowance times days, but its old 20GB example does not settle this plan. [Older help](https://help.iwinv.kr/manual/605)

**First month is not necessarily 53,020 KRW.** Application starts a billable day; 27 or more days in a calendar month use the monthly rate. For an unchanged server opened October 10 and retained through October 31, the conditional 22-day example is `22 * 1780 * 1.1 = 43,076 KRW`. Card collection is described for the following month's first day; delayed collection does not delay liability. Sales/billing consultation is free in office hours, while technical support can be charged. [Billing policy](https://docs.iwinv.kr/docs/billing/iwinv_price_policy/)

| Full-month base + average backup storage | VAT-inclusive KRW/month |
|---|---:|
| Server only | 53,020 |
| Server + 50GB backup storage | 54,120 |
| Server + 100GB backup storage | 55,220 |
| Server + 200GB backup storage | 57,420 |

Object Storage is 20 KRW/GB/month excluding VAT on average retained bytes; versions/copies count. Seven retained full 50GB copies at steady state mean 350GB and 7,700 KRW storage, not 1,100. Requests are temporarily free; external retrieval and other API charges must remain separate. The published console/API upload limits also require tooling/chunk-size verification. These totals exclude domain, mail, excess transfer and external recovery download. [Storage pricing](https://www.iwinv.kr/storage/obj)

The console's first excess-transfer band is 70 KRW/GB excluding VAT: +100GB is 7,700 KRW and +1,000GB is 77,000. Higher bands decline, but neither rate nor local request shaping creates a hard spending cap. Billing notifications may lag one hour; mobile Alimtalk/LMS is listed at 50 KRW/message with VAT treatment unspecified. Automatic provider cost blocking was **not confirmed**. [Billing alert](https://docs.iwinv.kr/docs/billing/bill-alert/)

Free provider monitoring is available, but port monitoring starts OFF and must be configured; application, disk and backup-failure checks need explicit coverage. [Monitoring](https://docs.iwinv.kr/service/monitoring/resource-port-monitoring/info-resource-port-monitoring/) Same-provider copies do not cover loss of that account. Non-payment rules specify calendar month 4th suspension and 10th deletion. Keep a separately controlled encrypted copy and reachable billing contact. [Non-payment](https://docs.iwinv.kr/docs/billing/non-payment/)

## Remaining Provider Questions

**Unsent sales/billing inquiry draft; no paid technical consultation authorized:**

1. For `hpa_2.8-g2`, please confirm the binding full/partial-month outbound allowance and calculation, including the console's 33GB/day versus website's 30GB/day and concentration of usage within the month.
2. For normal initial WAV catalog uploads, what measurement window/burst treatment applies to the inbound 50Mbps condition? Does older inbound-greater-than-outbound wording still govern this product? Please confirm ordinary upload use without an additional contract. 50Mbps is 6.25MB/s, not 50MB/s or an automatic throttle.
3. Is a customer-selectable automatic traffic/cost stop available, with what delay and billing treatment during attacks or provider blocking? Please also confirm recovery-console access when SSH is unavailable and any SMTP port restrictions for external authenticated mail.

The current product text allows a separate contract for inbound at/above 50Mbps and restriction during attacks; the older help is stricter. No clear burst definition or attack-charge waiver was found. Do not resolve this contradiction by assumption. [Current terms](https://www.iwinv.kr/server/server), [older help](https://help.iwinv.kr/manual/605)

## Execution Gates

| Stage | Required action / status |
|---|---|
| Before purchase | MA obtains/accepts a specific provider-risk decision; confirms opening catalog and backup peak plan, retention byte budget, recovery owner and single-host downtime model. Final application/purchase requires separate user approval. |
| Before first target startup | Select supported package sources/Java/FFmpeg; fresh schema/seed and audited first ADMIN; independent external JWT/billing keys; explicit storage roots, least privileges, one writer and canonical origins. No TEST DB deletion/migration implied. |
| Before invited access | Bootstrap is SSH-only ingress and lacks SMTP 587/465 egress. Configure web/TLS and exact mail transport separately, finite SMTP timeouts, fresh-host receipt/link tests, client-IP trust and protected paths. Existing Gmail success is not revoked. |
| Before valuable writes | Implement complete encrypted external and independently controlled backups, failure alerts and isolated restore. Open outbound 443 is not restore isolation; disabling notifications does not disable schedulers/local recovery writes. |
| Before small public operation | Mixed browsing/search/playback/seek/download/WAV conversion and large-image/document tests; observe p95/errors/429, CPU/steal, memory/GC, DB pool, I/O, queue age and bytes. Verify shared-IP admission, download usability, reboot/rollback, TLS renewal, outside alerts and recovery time. |
| When a shortfall appears | Report evidence, impact, no-cost mitigation, priced expansion option and required decision immediately. No silent upgrade; do not confuse a candidate limit with hardware exhaustion. |

Detailed implementation gates remain in the [PG report](WI-20261010-ATS-005-summary.md), [capacity runbook](../../deploy/single-vps/runbooks/acceptance-capacity.md) and [deployment runbook](../../deploy/single-vps/runbooks/deployment.md). Buying a server completes none of those tests. Provider SLA terms are not application HA. [SLA](https://www.iwinv.kr/treaty/treaty/sla)

## Closeout

WI004/005/006 inputs are complete and incorporated; WI007 completes this document-only audit. There is no downstream audit WI. REQ004 can close without closing the existing deployment REQ, purchasing resources or accepting production. MA retains the final user-facing decision. No code, prior report, secret, DB, service or cloud state was changed.
