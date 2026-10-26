---
version: 1.0
last_updated: 2026-10-11
project: ATS
owner: SA
category: evidence-pack
status: confirmed
dependencies:
  - path: WI-20261011-ATS-057-handoff.md
    reason: Approved read-only audit and two-report ownership
  - path: ../user/REQ-20261011-ATS-007.md
    reason: Approved phased work and unresolved target designations
  - path: ../../docs/index.md
    reason: Current decision entry and preserved dated completions
  - path: WI-20261011-ATS-054-evidence-pack.md
    reason: Latest completed local diagnostics, not native web acceptance
---

# Evidence Pack: WI-20261011-ATS-057

## Summary

**TL;DR: Audit COMPLETE; next-stage preparation identified, execution gated.** Reuse the existing architecture and artifacts. The first-ADMIN/signup/reset recipient is now designated privately and synthetic WAVs are suitable; do not request either designation again. The smallest next WI is a reviewed authentication/mail and first-ADMIN preparation packet after WI058 web/cleanup PASS. WAV testing additionally needs safe worker isolation; subscriber download needs a non-payment entitlement path. Neither is solved by enabling existing test flags. No architecture change or native action was performed.

## Scope / DoD Check

- [x] Read current entry, REQ007, latest relevant completions and current source; distinguish TEST, local preparation and target evidence.
- [x] Identify minimum human inputs, reusable helpers, source-level blockers and bounded test/write estimates.
- [x] Separate backup core from missing adapters, restore/copy/alerts and paid destination decisions.
- [x] Preserve WI055 ownership, all prior evidence and unrelated dirty changes; create only this report and the user summary.
- [x] Return the immediate questions in commentary; hand the successor conditions to MA without subdelegation.

## Observation And Evidence Authority

SA observation: **2026-10-11, Windows/PowerShell, local checkout**, branch `main`, HEAD `58b1011dad796017e47dbcc82a4225ca449bcdf6`. Dirty source/configuration and concurrent work are preserved. Document edit date is not a target verification date.

At **19:35:26.8768466 KST**, SA reran the inspected, read-only `build/WI036-web-contract/verify-identity.ps1`: **PASS_LOCAL_IDENTITY_ONLY**, exit0; 641 backend inputs, 336 frontend inputs, 143 SPA files, four separate historical coverage exclusions matched. The helper's fixed `QA-INTEG / WI036` owner label describes its author, not this SA rerun.

| Current local identity | SHA-256 |
|---|---|
| `build/WI020-release-v3/build/libs/ATStudio-0.0.1-SNAPSHOT.jar`, 70,306,351 bytes | `01d5623972e17d0b6e1d3e532096239cd87e9995a08771c59292c94bf3699dcb` |
| SPA file-set hash, WI026 output | `43a0384c5f48d100e4af6b20d989a2186b34e6900cf4e69c94b010e9671c8647` |
| `build/WI036-web-contract/verify-identity.ps1` | `6fcd7568785a8e25d9391dce09f5a698f7adda13d9ce9fcdbe66c8f679a4ad6f` |

Source/artifact identity is current locally, not a new build, remote identity check or runtime PASS. MA must recheck pins and effective configuration at each execution window; do not rebuild merely because an older runbook asks for one.

Latest in-turn MA-supplied update: the user designated one exact email for first ADMIN, signup and reset-link trials; MA retains it privately. Synthetic WAV fixtures are suitable. MA reports existing server host/key authentication validated and matching641/336/143 source/output identity. This is attributed MA evidence without a supplied observation time, not SA native verification. **No feature runtime has run.** This update supersedes the earlier missing-recipient/principal and media-choice questions only, not the web/safety gates or other inputs below.

| Evidence owner / date / environment | Accepted scope; remaining boundary |
|---|---|
| MA/user, 2026-09-08 development/TEST; `WI-20260908-ATS-012-evidence-pack.md:19,51-60` | Prior three-message Gmail receipt plus website-triggered SMTP SUCCESS, recipient receipt and reset-page opening. No password change; old recipients are not new send authorization. |
| QA-INTEG plus MA/user, 2026-09-09 retained TEST; `WI-20260909-ATS-028-evidence-pack.md:25-47,206-243,311-334` | 22 original/128000bps pairs, bounded UI batch/retry/seek and API original hash; human-assisted phone save/open/play PASS. Not current-host, all-device, phone-local hash or capacity proof. |
| MA, 2026-10-10 target maintenance; `WI-20261010-ATS-024-evidence-pack.md:90-91` | Actual OS reboot/new boot/kernel PASS. Do not call all reboot work untested; post-deployment app/DB/worker/renewal recovery remains a separate test. |
| MA, 2026-10-11 restricted target; `WI-20261011-ATS-005-evidence-pack.md:112-114`; `WI-20261011-ATS-034-evidence-pack.md:121-132`; `WI-20261011-ATS-045-evidence-pack.md:43-55,111` | Restricted TLS/renewal configuration and bounded backend/cleanup success are preserved. Latest completed web attempt had inner HTTP17 PASS but overall HOLD; original HTTPS/access recovery completed. No populated authentication/media acceptance. |
| SE/PG/DocOps, 2026-10-11 Windows synthetic checks; `WI-20261011-ATS-054-evidence-pack.md:25,51-63` | Local diagnostics complete, not native-ready proof. REQ007 now authorizes the next chain; it does not turn REQ006 failures into success. WI055/056/058 outcomes are not asserted here. |

The older `production-readiness.md` and `acceptance-capacity.md` matrices are dated planning snapshots, not live progress. The restic plan's older "no verified universal scheduler-off switch" must be read alongside the current source-scoped switches below; that source improvement is not restore acceptance for an arbitrary restored release.

## Remaining Inputs Beyond The Designated Recipient

Only designations are needed in the report. Actual addresses, identities, credential bodies, reset URLs and private paths belong in the existing protected operator channel, not Git or delegated context.

| Input | Exact decision needed |
|---|---|
| Signup profile and buyer | First-ADMIN identity is designated; do not ask again. User privately completes nickname/password/phone, account type and required job/company fields plus terms/privacy consent. Designate a separate non-ADMIN test buyer and its creation versus existing-account use; do not temporarily demote the sole ADMIN for buyer testing. |
| Test mail transport | Recipient is designated for signup/reset; still confirm the SMTP account/service reference, sender, host/port/TLS mode and who opens the links from the allowed network. Address designation is not SMTP configuration or credential discovery authority. Propose one signup verification and one reset-link message to that recipient; a third signup message only after the separate buyer recipient is designated. No password change or automatic resend. |
| Test entitlement | Synthetic10-second WAVs need no renewed choice. For positive buyer download, designate an existing service-enabled entitlement or the exact non-provider fixture method; specify plan/quota, billing-cycle field, start/expiry and retention/closeout. ADMIN bypass is not buyer proof. |
| Backup and recovery | Existing approved private repository/destination or explicit incremental cost approval; endpoint/region/ACL and credential-reference ownership, independent external-disk identity, isolated Linux restore target/DB/roots and recoverable key custody. Do not request keys in chat. |
| Operations | Staffed maintenance/stop window, independent alert channel and recipient/acknowledger, permitted outage, phase byte limits and current provider allowance. Monthly KRW70,000 is a budget, not an enforced billing cap. |
| Stakeholders | Named invitees, intended roles, designated devices/networks, access duration and revocation owner. No general public opening, arbitrary account creation or whole-network expansion. |

## Minimal Executable-Path Map

Java paths below are relative to `src/main/java/com/atstudio/atstudio/`. All are current source observations, not invoked operations.

| Step | Reusable path and exact missing gate |
|---|---|
| Authentication and mail | `controller/UserController.java:23-35` -> `service/UserService.java:78-109` creates USER, consent rows and a verification token/send. `controller/AuthController.java:58-69` -> `service/EmailService.java:47-83` verifies once; `service/auth/AuthService.java:39-58,125` requires password-login enabled and a verified user. Login -> fresh token -> `/api/users/me`; `frontend/src/pages/auth/LoginPage.tsx:159-161` already passes the fresh token. MA must privately confirm effective SMTP/TLS/origin and allow only designated SMTP egress; `application.yml:34-38` maps finite mail timeouts, not delivered-mail proof. |
| Mail success criterion | `service/EmailService.java:170-187` catches send exceptions. Signup/API success alone cannot pass delivery. Require correlation-only SUCCESS, human receipt and correct-origin verification/reset page; never export address/body/token. Reset submission is excluded by REQ007. |
| First ADMIN | `controller/UserController.java:94-95` and `service/UserService.java:299-357` require an existing active ADMIN and reason to change roles. No first-admin path is supplied by this API. `deploy/single-vps/runbooks/deployment.md:110-120` requires a reviewed one-time provisioning method, exact principal, audit and rollback; test/acceptance bootstrap stays OFF. Do not replay schema/seed or improvise SQL. |
| WAV worker prerequisite | `service/audio/TrackAudioProcessingWorker.java:31-32` requires **both** background tasks and audio worker enabled. `config/BackgroundTasksConfiguration.java:14-29` registers scheduling when background tasks are enabled; `service/SubscriptionScheduler.java:58-89` and `service/PaymentReconciliationService.java:81` have scheduled payment/local-write work. Therefore `BACKGROUND=false, AUDIO=true` will not transcode; `BACKGROUND=true` is not audio-only isolation. SE/PG must supply a bounded selective execution/suppression method before WAV runtime testing, without casually enabling unrelated jobs. |
| Upload -> READY -> Public Listening | `controller/TrackController.java:34-45,85-97` requires ADMIN for upload/status/retry; `service/TrackService.java:66-105` stores original and enqueues WAV. `dto/track/TrackCreateRequest.java:21-38` permits synthetic title/BPM/tonality without optional tags or thumbnail. `service/audio/FfmpegAudioEncoder.java:19-35,53-68` uses libmp3lame128k, one thread, finite timeout and 256MiB output ceiling; worker has one executor per JVM (`:40`). Prove one JVM/encoder, READY, full decode/duration/bitrate, and explicit activation. `TrackController.java:121-164` implements full200/single-Range206/invalid416. Check list/detail seek and no License/download-row change from Public Listening. |
| Official Download | `controller/TrackController.java:109-119` -> `service/DownloadService.java:44-95`: first non-ADMIN download requires active Subscription/quota, creates Download History and License and increments the count; existing License skips new accounting; ADMIN bypasses entitlement. Test denied anonymous/unentitled requests, one buyer first download, re-download and original SHA-256. `service/AdminSubscriptionCorrectionService.java:121-127,274-302` adjusts an **existing** subscription, not a fresh entitlement seed. Do not invoke checkout/provider or create fake payment success to obtain one. |
| Guarded backup core | `scripts/deployment/backup/guarded_backup.py:23-33,117-213,229-258`: close admission, quiesce/fence, logical dump/inventory, capture, verify, approved resume with a shared operations lock. `backup_protocol.py:203-243` binds one-use/recurring authority. `deploy/single-vps/backup/host_hooks_unconfigured.py:7` still fails closed. Implement actual observed host hooks, logical dump/completeness checks and cross-operation lock integration; do not return fabricated true predicates. |
| Restore, copy and alerts | `guarded_backup.py:164-189` retrieves only dump+manifest, not media restore. `:301-306` exposes copy/restore planning markers only. `backup_protocol.py:246-254` checks declarations, while `:257-291` detects failed/incomplete/missed/26h/8d states and reports external send NOT_IMPLEMENTED. Need actual isolated full restore/hash/permission/strict-media checks, independent disk copy and outside-app checker/sender/acknowledgment. Keep exact restored-source background/worker switches OFF, services/timers and egress fenced before startup; do not equate declarations with enforcement. |
| Reboot and stakeholder handoff | `deploy/single-vps/systemd/atstudio.service:20-30` supplies restart/cgroup candidates, not boot proof. After backup/restore and alert acceptance, MA performs a guarded single reboot/window if still in scope: fresh boot identity, restricted ingress, one JVM/encoder/job owner, matching release/DB/roots, renewal schedule, strict storage and original-hash checks, recovery and access closeout. Only then prepare designated stakeholder access. Human use must be separately observed, never marked done by provisioning. |

**Do not extend WI055's read-only trial in place.** Its handoff limits it to management-only read-only work with unchanged state/security comparisons. Authentication, email verification and Official Download write data; they need a fresh scoped operation contract and expected-delta ledger, independent review and named inputs. Keep WI055's six-GET/17-query history, credential rejection and prior failures intact.

### First-ADMIN And Membership Mapping

These are three separate concepts, not one "member role":

| Concept | Source-grounded next action |
|---|---|
| App role (`users.role`) | `entity/User.java:41-46,100-110`: default unverified USER; verification and ADMIN role are separate mutations. Register the designated principal through the normal API, complete actual email verification, then use the independently reviewed one-use first-admin operator method. Guard exact target/user, expected verified USER state and observed initial-admin state, retain an audit/rollback receipt, expire provisioning authority, then fresh login and `/api/users/me` to confirm ADMIN. This operator method is still to be implemented/reviewed; the existing ADMIN-only role API cannot bootstrap itself. |
| Account classification (`users.user_type`) | `entity/enums/UserType.java:3-4` is INDIVIDUAL/BUSINESS, unrelated to ADMIN. `dto/user/RegisterRequest.java:24-64` requires nickname, email/password, phone, type and terms/privacy consent; `service/UserService.java:439-460` requires job for INDIVIDUAL or company name/no job for BUSINESS. Email alone is insufficient signup input; MA must not invent personal details or assent on the user's behalf. |
| Download membership (`user_subscriptions`) | `entity/UserSubscription.java:23-44`: unique user, plan, billing cycle, status, start and expiry. `repository/UserSubscriptionRepository.java:23-28` accepts ACTIVE or CANCELLED through expiry; `DownloadService.java:64-69` applies daily plan quota. For an existing row, reuse `/api/admin/user-subscription-corrections/preview` -> request -> `/{id}/approve` -> `/{id}/execute` (`controller/AdminUserSubscriptionCorrectionController.java:25-87`) under exact reviewed scope. A new buyer has no such row: the source exposes no first-entitlement creation through this correction flow. Prepare a bounded, audited, non-provider fixture provisioner with exact plan/dates and no payment/billing records, or leave positive buyer coverage HOLD. Do not relabel ADMIN download as equivalent. |

Reuse focused `UserServiceTest`, `AuthServiceTest`, `EmailServiceTest`, `AdminSubscriptionCorrectionServiceTest`, `DownloadServiceTest` and `DownloadConcurrencyContractTest` in the implementation WI; none was executed by this audit. Account provisioning still needs reviewed preservation/rollback and a recoverable prechange boundary; prior schema bootstrap is not a backup to replay.

## Helpers And Small Test Budgets

Reuse the read-only WI036 identity helper before each phase. `scripts/validation/wav-browser/generate_fixtures.py:12-46,56-84` provides deterministic, no-overwrite PCM generation/hash patterns, but its unchanged default produces **419,783,295 bytes** in a historical fixed root. Do not run it for the small trial. `probe_pairs.py:13,65-87,123-142` supplies original/hash/128000bps/full-decode checks but hard-codes the old local storage/private root. A later SE WI must parameterize a new bounded helper or reuse its checked functions with a reviewed exact-target wrapper, not redirect it to arbitrary host paths. Do not run the old backend launcher on the new server.

Proposed ceilings below are **planning estimates, not configured enforcement, accepted load capacity or provider terms**. MA must measure initial free space/inodes, actual objects and provider allowance, then freeze byte/time/write limits in the next packet. Include upload/download directions, browser fetch-ahead, retries, static assets, control/observation traffic and in-flight response budgets. Stop on the first limit; no automatic retry or extension.

| Phase | Proposed starting envelope / write budget |
|---|---|
| Account/media feature window | <=15min and <=64MiB aggregate wire allowance; stop new requests at48MiB, reserving16MiB for bounded in-flight work. <=2 accounts, <=1 first-admin promotion, <=3 explicit test messages, <=2 new Tracks, <=2 first-download/License pairs and <=2 re-downloads. One buyer entitlement only through the separately reviewed method. Preserve expected consent/token/audit/journal updates; no destructive cleanup. |
| Synthetic media size | Per10s WAV: `44 + 44100*2*2*10 = 1,764,044` bytes; two originals3,528,088 bytes. MP3 payload estimate160,000 bytes each; provision <=256KiB each including headers. Raw retained media estimate <4MiB. Allow32MiB media/spool/temp plus32MiB DB/log growth (64MiB observed growth ceiling); stop if actual behavior exceeds it. This small fixture does not test the100MiB product boundary. |
| First mixed smoke | After feature and operational-safety gates: 1-5 controlled clients, <=5min/256MiB. One stream/seek, one entitled download and browsing; at most two additional small WAVs, one encoder. Retained test media/temp <=32MiB and total observed disk growth <=64MiB. No mail/payment/backup overlap. |
| Later MIXED-A only after smoke | Existing candidate10min/1GiB, <=20 active clients comprising up to12 listeners,2 downloads,6 browsing, plus the separately counted upload operator; not20 simultaneous streams. At most two small uploads and <=128MiB total disk growth. Same-NAT limits constrain this mix: download1/IP and shared media4/IP mean one management IP cannot prove two-download/many-stream concurrency. Obtain designated test-network identities or explicitly label the reduced topology; never relax limits to make the test pass. |
| First cold backup/restore estimate | Only if measured complete recovery set <=256MiB and dump<=64MiB: propose15min capture window, manifest8MiB, cache128MiB, temp64MiB, other growth64MiB, sequential dump/restic under the existing1GiB helper cap. Stage up to328MiB new local budget plus256MiB for isolated restored files, beyond the disk reserve. Provisional network allowances: capture upload512MiB, cold verification retrieval128MiB, then independent copy and isolated restore512MiB each in separate windows. Actual repository pack/metadata/request costs and restored-DB expansion must be measured and budgeted before dispatch. If inputs exceed any assumption, HOLD/re-estimate; never omit required recovery members, prune or purchase automatically. |

Resource/stop baseline: `deploy/single-vps/runbooks/resource-baseline.md:47-61,74-82,165-191`; current app unit and `StorageMutationAdmissionProperties.java:15-21` implement local candidates (heap1GiB, app+encoder cgroup3GiB, mutation2global/1IP, Hikari10 prerequisite), not fresh effective settings. Require MemAvailable>=1GiB, disk free>=max(20%,10GiB)+next admitted work, inode free>=10%; retain one encoder and no backup/load overlap. Sample resources and interface deltas at5s (`acceptance-capacity.md:38-49`). Stop immediately for disclosure, unintended writes/mail/payment, OOM/restart or pool timeout; stop/escalate CPU>=90% for2min, RAM<1GiB or cgroup>=90% of3GiB for1min, swap growth, unexpected errors>=1% or metadata p95>2s for2min, queue age>5min/non-drain, disk/inode/time/byte breach. Metadata p95<=750ms and start/seek<=2s are candidate goals, not measured results.

`backup_runtime.py:166-221` provides sampled reserve/cgroup checks and `:335-341` rate flags; neither these nor Nginx256KiB/s stream/2MiB/s download shaping is a hard invoice/total-byte cap. Preserve fail-closed stop/recovery authority and provider-meter uncertainty. External storage purchase/installation/initialization/permission changes remain MA-target-gated; no provider terms were researched or guaranteed here. Weekly independent copy implies up to7days planned provider-loss RPO, not24h; <=4h RTO is only a rehearsal goal (`restic-backup-plan.md:141,188`). No retention deletion is authorized.

## Commands, Tests And Preservation

- Read-only `git status --short --branch`, `git rev-parse HEAD`, targeted `rg`, `Get-Content` and `Get-FileHash`; no private configuration or credential bodies opened. Some initial guessed paths were absent; actual paths were located from `rg --files`, not invented.
- Inspected then invoked the existing WI036 identity script and projected its JSON in memory: exit0, `PASS_LOCAL_IDENTITY_ONLY` at the time above. No output file was created by it; no source/artifact/runbook changed.
- Backup source SHA-256 checks matched the existing WI013 inventory for `backup_protocol.py` (`a890d997...fd43`), `backup_runtime.py` (`50463b30...cc72`) and the unconfigured hook (`e214eb39...e99b`); current `guarded_backup.py` SHA-256 is `f741105df211d462177db4f4d140c862e924d6ba1cb2e729620e6c949aa1269f`. These reads do not rerun WI013's35 synthetic tests or prove native adapters.
- Product tests/builds, browser/SSH/cloud/network/DB, media generation, SMTP sends, provider/payment actions, backup/copy/restore/reboot: **NOT RUN by SA**. No installation, purchase, subdelegation, commit or push. Report checks are local document checks only.
- Scoped validation after the recipient update: two reports,14 required metadata fields,9 local links and6 dependency targets; zero issues. Direct reads also checked terminal newline, trailing whitespace, ASCII and absence of literal email addresses. `git diff --check -- <two owned paths>` exited0; because both files are untracked, the direct checks provide their actual content validation. Applied the validate-docs skill's bounded checks without its repository-wide crawler; no global index/traceability or whole-repository PASS is claimed.

## Risks / Rollback / Next WI

Recommendation: retain the existing architecture and move in small executable phases. Alternative: keep the recovered restricted quarantine state while designations are pending; this avoids side effects but leaves functional readiness open. No new ADR/library/provider decision is made. Rollback for this audit is to withhold the proposals; preserve both reports and all historical evidence, with no source/runtime rollback or deletion.

**Chain check:** WI057 unblocks preparation of subsequent feature/backup WIs, not execution by SA. MA should use `create-wi-handoff-packet` for the smallest successor: **designated-account authentication/mail + one-time first-ADMIN preparation**, with exact expected writes, secret-reference-only inputs, finite counters, fail-closed expiry, safe closeout and PG review. Native execution depends on WI055 -> WI056 -> WI058 web and recovery PASS plus private signup profile/consent, effective SMTP and reviewed provisioning. The recipient/principal and synthetic-media choices are already supplied; do not ask again. Do not allocate a competing WI number here or touch WI055.

Then separate the worker/scheduler isolation and buyer-entitlement prerequisites before a two-WAV feature window. Prepare actual backup adapters/independent alerts/isolated restore, verify guarded reboot, run smoke then bounded MIXED-A, and finally prepare designated stakeholder access. No valuable/private catalog ingestion or invitations before recovery/safety acceptance. Any new important design choice or extra cost returns to the user; routine already-approved scoped work does not need repeated general approval. REQ007 remains active.

## Reference Documents (Tier 0-2)

| Tier | References / use |
|---|---|
| 0 | Injected STD-001 core-principles, STD-004 documentation-standards, STD-002 development-standards, STD-005 glossary; current files checked for governing rules and canonical terms. |
| 1 | Injected security-policy projection plus current policy sections; `.claude/agents/sa.md`; no secrets/PII, no native actions, evidence-first options. |
| 2 | WI057 handoff, approved REQ007, current decision entry/latest topic completions, current source and cited deployment runbooks. |
| Skill/config | `.agents/skills/create-wi-evidence-pack/SKILL.md` applied after handoff existence check; `.claude/config/workspace.json` confirms ATS and context-injection-rules declares SA tiers0/1. MA context order is Tier0 -> security/role -> task -> snapshot; no subagents called. |

## Related Documents

- [User summary](../user/WI-20261011-ATS-057-summary.md): Immediate decisions and successor conditions.
- [Handoff](WI-20261011-ATS-057-handoff.md): Exact ownership and exclusions.
- [REQ007](../user/REQ-20261011-ATS-007.md): Approved chain and remaining designations.
- [Backup integration boundary](../../deploy/single-vps/backup/README.md): Implemented core versus missing target operations.
- [Resource baseline](../../deploy/single-vps/runbooks/resource-baseline.md): Current candidate thresholds, not proven capacity.
