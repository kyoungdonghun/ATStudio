---
version: 1.0
last_updated: 2026-10-10
project: ATS
owner: SA
category: guide
status: draft
dependencies:
  - path: ../../../deliverables/user/REQ-20261010-ATS-009.md
    reason: Local preparation authority and separate sensitive-operation gates
  - path: ../../../deliverables/agent/WI-20261010-ATS-028-evidence-pack.md
    reason: Latest completed native bootstrap and cleanup receipts
  - path: ../CONFIGURATION.md
    reason: Public configuration contract and source mappings
  - path: ../../../docs/policies/security-policy.md
    reason: Secret handling and bounded evidence
---

# Production Configuration Readiness

> Purpose: Separate completed target preparation, local candidates and remaining
> decisions for the existing server without authorizing another operation.

## 1. Evidence Boundary

TL;DR: **BOOTSTRAP_APPLIED_NOT_RUNNABLE; production HOLD.** This WI030 audit is
local-only on 2026-10-10, Windows/PowerShell, `main` at
`58b1011dad796017e47dbcc82a4225ca449bcdf6` with preserved dirty changes. SA read
public sources and completed receipts; SA did not inspect the host or private
configuration. Receipt date/owner/environment below are distinct from this
document's edit date. Recheck later completion addenda before using this snapshot.

Evidence precedence: [current entry](../../../docs/index.md#current-decision-entry)
and the latest relevant completion receipt, then matching artifacts/source,
then dated planning documents. In particular:

- [WI028 final receipt](../../../deliverables/agent/WI-20261010-ATS-028-evidence-pack.md#final-v3-native-result-and-cleanup)
  supersedes earlier native HOLDs: MA applied once at 22:17:00 KST; host cleanup
  at 22:17:35 and Windows cleanup at 22:17:53 completed. Do not repeat bootstrap.
- [WI026 completion](../../../deliverables/agent/WI-20261010-ATS-026-evidence-pack.md#completion-addendum-approved-isolated-frontend-build)
  supersedes the earlier frontend HOLD and the older "new JAR build needed"
  checkpoint. JAR and isolated SPA are locally prepared, not deployed.
- [WI021 accepted query](../../../deliverables/agent/WI-20261010-ATS-021-evidence-pack.md)
  observed MySQL buffer 536870912 bytes and max connections 30 on the target.
  Older blanket "no settings applied" text is not the current MySQL state.
- `acceptance-capacity.md` section 3 still contains WI001's old 512k media,
  512MiB heap and 1536MiB cgroup examples. Use the current candidate below for
  any future test plan; neither set is measured operating capacity.

No existing template, historical record, manifest or deployment instruction is
rewritten here. The [WI030 Evidence Pack](../../../deliverables/agent/WI-20261010-ATS-030-evidence-pack.md)
pins local inputs and verification commands. A template validator PASS remains
`deployment_ready=false`; its generic target gates are not a live progress ledger.

## 2. Readiness Matrix

`APPLIED` means a dated MA target receipt, not a fresh SA observation.
`PREPARED` means local implementation/artifact only. `PENDING` means no completed
target proof in the inspected evidence. `DECISION` requires named user/operator
input and, where applicable, separate approval.

| Area | Status and current evidence | Remaining gate / owner |
|---|---|---|
| Host and maintenance | APPLIED: WI021/024/028 record the existing Ubuntu 24.04 host, hardening, OS maintenance and final cleanup. Baseline remains 2vCPU/8GB/100GB. | MA rechecks named host/boot/recovery access in each approved window. No continuous availability or capacity claim. |
| DB and runtime account | APPLIED: WI028 reports 43 tables, 6 exact plans, 0 non-plan rows; only SELECT/INSERT/UPDATE/DELETE, nongrantable, on the fixed fresh DB. | Existing DB/account must be preserved. JDBC authentication/read is WI029/031/032, not proven by root socket reads or DML grants. No SQL replay. |
| Private configuration | APPLIED, incomplete: WI028 records server-only DB/JWT/billing secret creation; root0700 parent, root0600 single-link files. | Source `production-bootstrap.py:render_env` replaces DB/key fields only; domain/SMTP remain unresolved by design. No private body was read here. No rotation, copying or regeneration. |
| Schema compatibility | PENDING: target-specific structural observation exists; canonical fresh manifest remains UNRECORDED. | Separately approved canonical observation/reconciliation and isolated Hibernate proof. Never bypass disposable-tool guards or use them on production. JDBC PASS cannot close either gate. |
| Release identity | PREPARED: WI026 JAR and 143-file isolated SPA match local SHA-256 evidence. Bundled driver is mysql-connector-j 9.7.0. | MA must preserve matched release/DB/storage identity and verify staged bytes; build success and ZIP inspection do not prove deployed admission or runtime. No rebuild required solely because older notes say so. |
| Domain, DNS and TLS | DECISION: `atmusic.co.kr` is the requested domain, not confirmed ownership, availability or current price. Nginx remains masked/inactive in WI028. | User confirms ownership or approves exact registrar/term/total/renewal and enters account/payment/registrant details privately; purchase before DNS/HTTPS. DNS, ACME method, any installation, exposure and renewal/reload test need separate scope. |
| Origin, CORS and SPA | PREPARED: same-origin SPA `/api`; template requires one exact HTTPS origin for `APP_BASE_URL` and `CORS_ALLOWED_ORIGINS`. | After domain control is confirmed, align hostname, Nginx fixed host, certificate and origin. No wildcard, old tunnel or localhost production origin; no new frontend API-host variable. |
| OAuth and billing callbacks | PREPARED: Google/Kakao/Naver `/social-login/{provider}` and Toss `/subscriptions/checkout/success` and `/fail` use the same origin placeholders; credentials empty in the example. | Set callback strings only after origin decision. Console registration, enabling a Provider, TEST calls and LIVE billing are distinct approvals. Keep unused Providers disabled; local billing encryption is still mandatory. |
| SMTP and password auth | DECISION: SMTP host/port/sender unresolved; example credentials empty, auth/STARTTLS false, password login false. Timeouts are mapped at 5000/10000/10000ms. | User selects existing-account reuse or another mail service, sender, permitted recipient and private credential entry. Confirm provider-required auth/TLS and effective settings, then approved new-host delivery and links before activation. False flags are not a mail recipe. |
| Prior mail evidence | VERIFIED HISTORICALLY: 2026-09-08 development/TEST Gmail receipt evidence remains valid, per current entry and WI011/012. | Do not infer missing Gmail capability from packaged localhost defaults. New-host egress, SMTP and origin checks remain PENDING; no provider change selected. |
| App resources and admission | PREPARED: heap256-1024MiB, metaspace256MiB, cgroup high2GiB/max3GiB, Hikari10/idle2/acquire5000ms, mutation admission2 global/1 trusted IP. WI014 independent local review. | Activate only the reviewed matching artifact/settings together and prove effective pre-body/pre-transaction admission. No pool-only increase, deadlock guarantee or supported-user claim. |
| MySQL resources | APPLIED, dated: WI021 observed buffer512MiB, max connections30, loopback only; WI024/028 preserve configuration and final stopped/masked state. | These are configuration receipts, not throughput/RSS proof or a currently running service. Revalidate in the approved window; no tuning here. |
| Nginx, storage and logs | PREPARED: stream256KiB/s; download2MiB/s; media4/IP,16/global; download1/IP,2/global; multipart1/IP,2/global. Java owns disjoint storage roots; proxy never serves original/private storage directly. | Exact render, native syntax, trusted-client spoof checks, protected-path probes, safe log collection and independent external IPv4/IPv6 checks remain gated. No actual proxy traffic accepted in WI028. |
| Startup, jobs and egress | PENDING: app unit absent and no app/DB/proxy listeners at WI028 final receipt; reviewed unit permits IPv4/IPv6 and automatic failure restart. | Before any future app start, approve one scheduler/worker owner, relaunch control and verified external-network fence. Ingress denial, blank credentials and notification=false do not fence outbound traffic. See section 3. |
| First ADMIN | DECISION/PENDING: seed has plans only, no users/ADMIN. Acceptance/test-user bootstrap stays false. | Named principal, account verification and reviewed one-time role provisioning/audit/rollback require a separate WI/approval. No invented password, QA bootstrap or ad hoc SQL. |
| Secrets and data recovery | PREPARED only: restic cold-capture core/guards exist, daily authorization defaults OFF; host hooks, independent copy and actual restore are unfinished. | Approve exact repositories, independent disk, separate recovery access, downtime/schedule and owner. Preserve full required billing keyring, DB and both roots together. Root0600 is not encrypted off-host recovery. |
| Alerts and capacity | DECISION/PENDING: monitoring/stop thresholds documented; independent alert transport and recipient are unconfigured. | Name owner/coverage/recipient, prove synthetic delivery and acknowledgment independently of the app. Measure bounded mixed workload, disk/RAM reserve and costs before release; monthly KRW70000 is a budget, not a hard cap. |

## 3. Startup Is Not a Read-Only Probe

Source inventory under `src/main/java/com/atstudio/atstudio/`:

| Source | Effect requiring an explicit start boundary |
|---|---|
| `AtStudioApplication.java`; `service/SubscriptionScheduler.java` | Scheduling enabled; renewal at 00:00, order expiry at 00:10, subscription expiry at 00:30, Asia/Seoul. Can write data or attempt Provider work. |
| `service/PaymentReconciliationService.java`; `WithdrawalBillingCleanupCoordinator.java` | Reconciliation 01:00 and cleanup retries 01:15. Notification flag is not scheduler suppression. |
| `service/storage/StorageMutationRecoveryService.java` | Startup recovery and 60-second repeated recovery may update journals and clean files. |
| `service/audio/TrackAudioProcessingWorker.java` | Default-enabled private executor starts on readiness, recovers interrupted work and polls immediately/then every 5000ms; can write and launch FFmpeg. Its flag does not disable other jobs. |
| `config/AcceptanceStartupGuard.java`; `StorageIntegrityStartupGuard.java` | Billing encryption is validated even without Providers; explicit roots and strict integrity are required. No waiver to make startup pass. |
| `systemd/atstudio.service` | `Restart=on-failure`; `Type=simple` is not readiness. AF_INET/AF_INET6 allowance is not an egress policy. |

Do not invent a universal scheduler-off flag. Egress denial prevents external
effects but does not prevent local mutations; future Hibernate-only/quarantine
procedures must separately prove all workers/jobs/relaunch paths suppressed.
The current REQ permits only the reviewed JDBC-only program after its own gates,
never Spring startup, even if the database is presently empty of user data.

## 4. Decisions and Exact Next Sequence

Recommendation: reuse the selected single-VPS architecture and completed
artifacts. No new ADR, library, provider selection or configuration change is
made by this audit. The safe alternative is to retain the completed local
preparation and defer native verification until approval; this delays evidence
but changes no service or data. Full app startup is outside REQ009 scope.

1. **Now, local preparation only:** preserve fixed DB/account/secrets and public
   candidate values; keep domain/SMTP unresolved and Providers disabled. Record
   identities and decisions, not private values. No server settings are applied
   by this WI. MA combines WI029 + WI030 and immediately issues WI031 to PG.
2. **Limited JDBC gate:** PG must pass exact JAR/driver/probe bytes and procedure.
   MA separately obtains explicit temporary SSH-agent and existing-secret-use
   approval. Only then may MA perform the reviewed loopback read-only JDBC check,
   with bounded errors, no app startup, no DB/account/grant/secret mutation and no
   secret-bearing command/output. Stop/remask MySQL, release only the owned key,
   confirm empty agent and Stopped/Disabled, and close sessions before reporting.
   Otherwise record NOT RUN with the exact missing gate. WI032 integrates actual
   MA receipts; JDBC success never changes UNRECORDED or authorizes deployment.
3. **Domain and mail decisions:** domain choice can be discussed now; before any
   charge, freshly confirm availability/ownership, exact cost/term/renewal and
   private user input through a separately approved action. Secure domain control
   before DNS/HTTPS. Select mail account/sender/auth method without disclosing
   credentials; announce fees or required user interaction first. This WI does
   not refresh prices, inspect accounts, purchase, change DNS or send mail.
4. **Separate deployment preparation:** reconcile canonical schema/Hibernate
   gates; approve isolated proof, exact staged release/roots, origin/callbacks,
   private configuration completion, capture-safe logs, egress controls and
   first-start/stop policy. No bootstrap repeat or retained TEST import. Preserve
   encrypted recoverable secrets and a consistent initial recovery baseline;
   implement and independently verify host backup adapters and restore before
   valuable writes. Preserve historical keys; never regenerate to fix recovery.
5. **Separate restricted activation:** after the relevant runtime/DNS/TLS scopes
   are approved, validate complete configuration and restricted proxy/certificate
   setup, start only one approved writer, and verify Hibernate/strict integrity,
   effective admission and permitted health checks. Keep Provider/SMTP egress
   closed except named approved tests. A deliberately isolated non-mail first
   start is a possible later scope, not granted here; it proves no login flow.
6. **Before usable/public operation:** complete named ADMIN provisioning, selected
   mail receipt/link and auth activation checks, TLS renewal/reload and expiry
   alerts, backup/isolated restore/independent-copy proof, automatic-start/reboot
   and single-owner checks, external access/privacy probes and bounded mixed
   load. Obtain release authority separately; no payment charge/refund/LIVE action
   follows from JDBC, login or restricted-access success.

At every shortfall, report evidence, impact, options, incremental cost (or
explicitly unknown cost), and the needed user decision. Never auto-expand,
purchase, prune, relax security or substitute reduced tests for the intended
small-production workload. Provider allowances/attack billing/hard-cap terms
remain unresolved risk; the earlier waived inquiry is not terms confirmation.

## 5. Stop and Rollback

Missing approval, identity mismatch, unexpected secret output/capture, uncertain
write, missing recovery access or a failed native prerequisite means HOLD.
Do not retry bootstrap, print exception payloads, rotate keys or relax guards.
MA records bounded failure and cleanup facts separately from the failed check.
This WI changes documentation only: a correction supersedes the affected
statement through a scoped edit while retaining dated receipts. No DB, file,
secret, service or provider rollback is performed or newly authorized.

## Related Documents

- [WI030 summary](../../../deliverables/user/WI-20261010-ATS-030-summary.md): User decisions and result.
- [Deployment](deployment.md): Detailed gated activation, ADMIN and rollback procedure; read chronology above first.
- [Environment contract](../env/contract.json): Unrendered example rules, not private-runtime validation.
- [Guarded backup](../backup/README.md): Implemented core and missing host integration.
- [Restic design](restic-backup-plan.md): Consistency, independent copy and retention proposal, not deletion approval.
- [Acceptance and capacity](acceptance-capacity.md): Observation/alert method; old resource examples are superseded above.
- [Reconstruction](../../../scripts/reconstruction/README.md#5-private-recovery-inventory-and-keyring): Complete private recovery inventory.
- [Schema boundary](../../../docs/design/db-schema.md#current-source-and-mysql-verification-boundary): Canonical manifest and Hibernate limits.
