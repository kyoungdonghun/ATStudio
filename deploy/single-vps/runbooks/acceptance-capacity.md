---
version: 1.0
last_updated: 2026-10-03
project: ATS
owner: docops
category: guide
status: draft
dependencies:
  - path: ../README.md
    reason: Restricted target and purchase decisions
  - path: ../../../docs/SR/SR-93.md
    reason: Remaining production acceptance and release gates
---

# Target Acceptance and Bounded Capacity

> Purpose: Measure a named operating envelope and preserve failures without implying production approval.

## 1. Execution Boundary

**Every target row below starts NOT RUN.** Documentation/static Windows checks
do not prove Linux services, restore, alerts or supported user counts. Keep
existing [TEST evidence](../README.md#4-preserve-existing-evidence); test the new
host's boundary without requiring every historical case to be repeated.

Before any active probe/load, approve target/release/tuple, identities, input
fixtures, external egress scope, recipient, time window, maximum bytes and
operator stop control. No public tunnel load, shared TEST runtime stress,
Provider/payment/refund or real email by implication. A tester's download
requires valid entitlement: arrange approved test identities and local
entitlement setup separately; do not trigger checkout or exhaust a real user's
quota just to create load. Do not enable acceptance bootstrap in production.

## 2. Observation and Alerts

Use already approved target tools first; do not add a monitoring service or
install packages in Stage A. Define the owner/recipient and notification path
before calling any alert operational. Sample at 5-second intervals during load
and record 1-minute summaries, timestamps/zone, warmup exclusion and tool version.

| Signal | Collect / verify | Failure response |
|---|---|---|
| CPU and memory | Host CPU/load/steal, JVM RSS/heap/GC, MySQL and FFmpeg RSS, available RAM, swap activity, OOM/restarts | Stop growing load; identify Java/DB/encoder contention, not just average CPU |
| Disk and inodes | Each DB/media/temp/log/backup filesystem: used/free bytes and inodes, growth and I/O latency | Stop uploads/capture before reserve is consumed; never delete originals to pass |
| Network | Interface rx/tx counter deltas on identified interfaces plus active transfers; record counter resets | Compare rates, retransmits and cumulative bytes; stop at approved allowance |
| HTTP | Client-side fixed route-class status/count, p50/p95, metadata latency, media time-to-first-byte, client start/seek delay, download duration and bytes; aggregate server counters | Separate expected 401/403/416/429 from unexpected errors; label intentional rejection probes |
| Audio | Pending/PROCESSING/FAILED counts, oldest pending age, completion/timeout counts and encoder count | Read status through approved bounded tooling; no storage keys/raw media diagnostics in receipts |
| Backups | Last complete/verified/off-provider snapshot age, bytes, next capture reserve, retrieval/restore result | Notify owner on failure, incomplete point or missed window; no false success when upload alone passed |
| Services/TLS | JVM/MySQL/Nginx availability, restart count, single scheduler owner, certificate expiry/renewal and clock skew | Restricted maintenance/recovery; do not auto-restart multiple writers |

Use aggregate Nginx response-byte/request-time metrics only if their reviewed
log format excludes PII/secrets; do not enable a default raw access log to obtain
metrics. Nginx response bytes omit other traffic and may differ from interface
bytes due to headers/TLS, SSH, backups, retransmission and accounting boundaries.
**Neither is the provider invoice.** Reconcile provider meter deltas only after
its ingress/egress, rounding, reset and attack terms are confirmed. 50Mbps link
copy and request-rate limits are not an enforceable monetary ceiling.

The [journald candidate](../journald/atstudio.conf) starts with `SystemMaxUse=256M`,
`SystemKeepFree=1G`, `RuntimeMaxUse=64M` and at most 7 days. These are host-wide
limits, not a separate per-app budget; review other consumers before applying.
The safe Nginx format omits route/identity, so obtain fixed route-class timing
from the synthetic client harness rather than claiming per-route server logs.
Use shorter bounded target diagnostics with synthetic values if ordinary logs
are disabled. Verify actual journal/logrotate limits and deletion ownership;
no rotation automation is implemented here. Suppress sensitive headers,
queries, payloads, identities and raw Provider/file identifiers at collection,
including error logs, APM and any upstream access logging.

Starting alert proposals: backup overdue 26h, any failed backup/restore, disk or
inode use >= 80%, sustained CPU >= 85% for 5 minutes, available RAM < 15% for
2 minutes, unexpected HTTP errors >= 1% for 2 minutes, certificate expiry < 21
days, or any service/worker crash. Assign acknowledgment target 15 minutes
during the staffed test window and an escalation owner. Outside that window,
agree coverage rather than claim 24/7 response. Delivery stays **NOT RUN** until
an approved synthetic alert reaches the selected recipient, is acknowledged,
and recovery/clear notification is checked. Existing Gmail receipts do not
prove new operational-alert routing; product reconciliation mail is not a
general backup/host alert system.

## 3. Mixed-Load Proposal and Byte Budget

These are **validation scenarios, not forecasts, promises or approved runs**.
Use a controllable existing load method selected for the target; no new load
library, background service or script is supplied. The operator must be able
to stop all clients and new admissions promptly.

| Stage | Concurrent active test users | Suggested mix | Maximum duration / client transfer budget |
|---|---:|---|---|
| Smoke | 1-5 | Browse, one stream/seek and one entitled original download | 5 min / 256MiB |
| A | 20 | Up to 12 listening, 2 downloading; remaining 6 browse/think/seek in a recorded schedule | 10 min / 1GiB |
| B, only after A | 50 | Keep media admission at the measured A envelope; remaining clients browse/think/rotate roles | 10 min / 2GiB |
| C, optional separate go decision | 100 | Same bounded media admission; separately agreed higher active-client demand | 5 min / 2GiB |

Record the **rendered** limits and their hashes first. Current WI001 starting
values are media-location global/per-IP connections 16/4, server connections
32/8, media response rate `512k`, JVM `-Xmx512m` and service `MemoryMax=1536M`.
The cgroup includes the JVM and its encoder child, not just heap. Shared Nginx
connection zones also interact across locations; measure actual concurrent
request admission rather than treating these numbers as independent pools.
The API/media rate settings and bursts are in
[nginx.conf.template](../nginx/nginx.conf.template); location rules are in
[https.conf.template](../nginx/https.conf.template). Starting settings are not
capacity measurements or purchased-host specifications.

Use multiple **approved, genuinely distinct source IPs**, keeping each below
its connection/request budget; multiple accounts behind one NAT do not count
as distinct IPs. Record the count/distribution without publishing addresses.
Alternatively, approve an isolated temporary tuning configuration with its
own limits, byte budget and rollback before testing. No auto-relaxation of
restrictions. A single-IP generator hitting 429 is not proof of VPS shortage.

Separate two tracks: (1) bounded ordinary traffic within the observed admission
envelope; (2) a short preapproved over-limit probe that expects 429 and recovery.
Report offered versus admitted concurrency, server/global versus per-IP control
rejections, request-rate rejections and genuine resource failures separately.
Unexpected throttling in ordinary traffic means the **selected configuration
does not meet that demand**, not necessarily hardware exhaustion. Deliberate
429 probes are excluded from unexpected-error thresholds, but their counts
and impact remain visible. Higher simultaneous media demand requires the
isolated tuning approval, not simply changing the stage label to 50 users.

Active clients here are not 20/50/100 simultaneously admitted streams. Record
the listening/browsing/idle duty cycles and actual request concurrency, and
state that any result is valid only for that measured mixed workload. Paused
or thinking clients must not be presented as sustained full-load users.

An additional operator session uploads at most two approved WAV fixtures per
stage, sequentially, with one conversion worker, while the mixed traffic runs.
Count that session, uploads, retries and all bytes in the receipt. First use
small known fixtures, then a separately budgeted near-limit audio+thumbnail
multipart case; no automatic repetition of rejected or failed requests. Fix
Track sizes/durations, browse think time (initially 5s), bounded seek schedule
and download frequency before a run. Keep identifiers/datasets synthetic and
approved; new WAV activation is an explicit ADMIN operation after READY.

Budget the **complete response** for every original download, complete/Range
stream and upload body, plus connection/headers allowance and observation
traffic. Each stage stops at the first duration, bytes or resource threshold.
Stop new requests while sufficient budget remains for the maximum in-flight
responses; track those responses to completion/termination. Client byte budgets
do not bound unrelated or malicious traffic/provider charges. Confirm a
separate provider-meter allowance and permitted stop response. No run when
either hard budget or provider-cost exposure is unresolved.

For planning only, a 128000bps payload is 16000 bytes/s per steady listener;
12 listeners would be 1.536Mbps **consumption payload only**. Browsers can fetch
ahead at the configured response rate; original downloads, overhead, seeks,
backups and attacks add traffic. This arithmetic proves neither a 50Mbps
service envelope nor support for 100 users. `512k` response shaping also limits
download speed independently of available hardware. Original WAV transfer and
disk/CPU contention must be measured. Pause backup transfer during the first
capacity characterization, then test any intended overlap in a separately
budgeted scenario.

## 4. Acceptance and Stop Thresholds

Confirm or revise these proposed gates **before** each target run, recording
reasons rather than moving thresholds after a failure. Measure p95 per route/
interaction class; report sample counts. A short run with insufficient samples
is inconclusive, not a confident p95/long-term capacity result.

| Measure | Proposed acceptance | Immediate stop / escalation |
|---|---|---|
| Metadata and discovery | p95 <= 750ms after warmup | p95 > 2s for 2 min; sustained unexpected errors >= 1% for 2 min |
| Media start and seek | p95 user-visible start/seek <= 2s; record TTFB separately | Repeated playback stalls or p95 > 5s for 2 min |
| HTTP safety | No unauthorized protected bytes or successful wrong-role mutation | Any disclosure, integrity mismatch or unauthorized monetary/mail effect stops immediately |
| CPU/memory | Stable RSS/heap after settling; no swap pressure/OOM; host and service-cgroup reserves reported | CPU >= 90% for 2 min, available RAM below greater of 10% or 512MiB for 1 min, service approaching its approved cgroup cap, OOM/crash or continued swap growth |
| Disk/inodes | Keep [capture reserve](backup-restore.md#2-inventory-and-space-gate), not only percentage free | Free disk below greater of 20% or 10GiB, inode free < 10%, or predicted next transfer/encode/capture would cross reserve |
| Queue | One encoder; no unexplained failures; queue drains after admissions stop | Oldest pending > 5 min, any timeout/termination failure, second encoder owner, or no drain within approved observation window |
| Transfer | Stop admission early enough for in-flight byte reserve; record network/provider delta | Stage byte/time cap reached, uncertain meter/cost exposure, or saturation causing preceding gates to fail |

Queue drain waiting counts toward the stage time/byte budget. If capped before
drain can be observed, report incomplete and arrange another approved window;
do not extend it silently. Abort only the owned load first, close new uploads/
downloads if authorized, and preserve pending state. JVM/DB restart or forced
termination is not an automatic load-stop action and needs its approved scope.

For any shortfall, report immediately: target/revision and stage; measured
threshold breach; impact; non-paid tuning/deferred load alternatives; additional
resource/cost option if warranted; and decision needed. No hidden retry loop,
silent paid upgrade, narrowed test labeled full PASS or guaranteed user count.

## 5. Minimal Readiness Receipt

| Gate | Current state | Later evidence to attach |
|---|---|---|
| Approved artifact and private settings | NOT RUN | Release hashes/config provenance and required JWT/keyring setup without values |
| Fresh DB / first ADMIN / intended content | NOT RUN | Exact-target approval, new canonical schema evidence, intended principals, no TEST/demo bootstrap |
| Strict paired storage and protected media | NOT RUN | Strict startup, ADMIN audit, public/private path negative probes plus positive public content |
| SSH/firewall/loopback/TLS and renewal | NOT RUN | Independent external tests, spoofed client-header test, renewal rehearsal and console recovery |
| Canonical origin and enabled account flows | NOT RUN | Same-origin `/api`, capability states, CORS, exact callbacks; separately approved recipient send |
| WAV/MP3 and original download | NOT RUN | Target upload/READY, full-length stream/Range/seek, unchanged original bytes, selected phone/desktop flow |
| Single owner, reboot and rollback | NOT RUN | JVM/encoder counts, interrupt/retry, boot and data-compatible rollback receipts |
| Encrypted/off-provider backup and isolated restore | NOT RUN | Complete paired point, recovery access, measured RPO/RTO and runtime containment outcome |
| Logs/metrics and alerts | NOT RUN | Bounded logs, no synthetic secret leakage, actual recipient acknowledgment |
| Mixed load and operating envelope | NOT RUN | Per-stage latency/error/bytes/resources/queue, stops and supported measured conditions |
| Restricted target acceptance | HOLD | Named owner accepts measured limits and remaining gates |
| General public / LIVE payment or refund | HOLD | Separate explicit release and financial approval, never inferred from Stage A |

For every row record **verification date, environment, owner, result, evidence
pointer and unverified limits** separately from document edit date. Existing
source/TEST gates remain credited; missing target evidence stays NOT RUN.
Finishing WI001-WI006 closes purchase-preparation Stage A only, not the overall
REQ or production release.

## Related Documents

- [Deployment](deployment.md): Sequence and authorization.
- [Backup and restore](backup-restore.md): Cold downtime and space gates.
- [Runtime audio contract](../../../docs/design/runtime-storage-operations.md#audio-processing-operations).
- [Payment limits](../../../docs/payment/known-limits-and-next-steps.md): Existing implemented scope and nonmandatory extensions.
