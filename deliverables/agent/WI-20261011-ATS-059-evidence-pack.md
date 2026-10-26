---
version: 1.0
last_updated: 2026-10-11
project: ATS
owner: SA
category: evidence-pack
status: draft
dependencies:
  - path: WI-20261011-ATS-059-handoff.md
    reason: Local design authority and two-report ownership
  - path: WI-20261011-ATS-057-evidence-pack.md
    reason: Source audit, identity evidence and bounded trial budgets
  - path: ../user/REQ-20261011-ATS-007.md
    reason: Approved phased scope and native execution gates
---

# Evidence Pack: WI-20261011-ATS-059

## Summary

**Local design COMPLETE; implementation and native feature execution NOT RUN.** Recommend one default-off audio-only mode, a separately invoked one-shot JDBC provisioner, and two bounded WAV fixtures. Reuse current Gmail SMTP for the proposed feature trial. Do not implement Naver SENS now, introduce a provider framework, or extend WI055's read-only contract. All changes below are proposals, not applied settings or new execution authority.

## Scope / DoD Check

- [x] Specify first-admin absent/present/idempotency behavior and separate buyer entitlement creation.
- [x] Specify worker isolation, compatibility truth table and required regression cases.
- [x] Define mail transport boundary, explicit mutation ledger, budgets and non-destructive closeout.
- [x] Separate private human inputs, implementation assignments and target release gates.
- [x] Preserve WI055 ownership, prior evidence and existing dirty changes; write only the two WI059 reports.

## Evidence Authority

SA inspected local source on **2026-10-11, Windows/PowerShell**. Scoped Git status shows `main` and pre-existing modified/untracked background-task implementation; these changes were not reverted or edited. WI057's 19:35:26 KST local identity result remains the referenced identity evidence: HEAD `58b1011dad796017e47dbcc82a4225ca449bcdf6`, backend641/frontend336/SPA143 matched. This WI did not rerun that verifier, rebuild artifacts or verify a remote target. Future implementation changes require fresh artifact identity; old pins cannot attest new code.

Latest user/MA direction supersedes only the earlier mail-provider uncertainty: the client has no Naver Cloud account; Gmail is the recommended bounded trial transport, with possible later migration on a client-owned account. The first-admin/signup/reset recipient is already privately designated. Synthetic WAVs are suitable. No additional address, credential, principal identifier or private configuration was requested or recorded. Server authentication/identity validation is MA-supplied evidence; no feature runtime has run. WI055 is still read-only with no SMTP; its PG/native gates and WI058 PASS remain prerequisites for a separate feature window.

**MA update, approximately20:05 KST:** provider outbound UI permits only global TCP80/443/53 and UDP53/123; Gmail TCP587 is absent. Existing locally tested Gmail reference-presence checks are all true, but native credentials were not copied and SEND is NOT RUN. No firewall was changed. These are attributed MA observations, not SA UI/network verification. Mail-stage execution remains HOLD pending reviewed, explicitly authorized SMTP egress at both provider and app layers; existing web/DNS/NTP permissions or local reference presence do not satisfy that gate.

## Source Pointers

Java paths in this report are relative to `src/main/java/com/atstudio/atstudio/`; test paths use `src/test/java/com/atstudio/atstudio/`. Line numbers describe this local inspection, not an immutable release.

| Pointer | Verified fact driving the design |
|---|---|
| `config/BackgroundTasksEnabledCondition.java:16`; `config/BackgroundTasksConfiguration.java:14,26` | Strict master flag defaults true; false suppresses Spring scheduling and rejects accidentally registered scheduler post-processors. |
| `service/audio/TrackAudioProcessingWorker.java:31,40,58,82`; `service/audio/TrackAudioProcessingTransactions.java:25,65` | Worker needs master plus audio flag; it owns a single-thread executor. Startup recovers all claimed Tracks and polling selects global pending work, not just test-owned rows. |
| `service/storage/StorageMutationRecoveryService.java:38,46`; `service/SubscriptionScheduler.java:58,63,84`; `service/PaymentReconciliationService.java:81`; `service/WithdrawalBillingCleanupCoordinator.java:31` | Six scheduled registrations include billing/local state and storage recovery. Storage also has a separate ready-event recovery path. Network denial alone cannot prevent their local writes. |
| `service/UserService.java:78,299,439`; `dto/user/RegisterRequest.java:24`; `config/AcceptanceStartupGuard.java:49` | Signup creates USER and validates profile/consents; role update requires an existing ADMIN. Production must not use acceptance/test bootstrap. |
| `service/AdminOperationAuditService.java:25`; `entity/enums/AdminOperationAuditAction.java:3`; `src/main/resources/schema.sql:1087` | Existing role-change audit can represent USER-to-ADMIN with nullable actor. Existing correction actions do not represent a first membership grant. |
| `entity/UserSubscription.java:23`; `repository/UserSubscriptionRepository.java:23`; `service/AdminSubscriptionCorrectionService.java:121,274` | One subscription row per user; ACTIVE or CANCELLED through inclusive expiry permits service. Local Subscription Correction requires an existing row. |
| `service/DownloadService.java:44`; `service/EmailService.java:47,89,144,170`; `service/auth/AuthService.java:39,119` | First Official Download writes accounting; ADMIN bypasses buyer checks. Mail sends create tokens and swallow delivery exceptions; password-reset submission changes credentials; login/logout update refresh-token state. |
| `service/TrackService.java:66`; `entity/Track.java:136,172`; `service/storage/StorageMutationCoordinator.java:74,100,122,170,250` | WAV upload/processing changes Track state, files and durable journals; synchronous transaction callbacks still run when scheduled recovery is disabled. |
| `scripts/deployment/RuntimeJdbcProbe.java:28`; `scripts/deployment/jdbc-readiness.py:78,146,200,277`; `scripts/deployment/production-bootstrap.py` | Reusable standalone JDBC, private-input framing, environment restriction, pinned driver, approval/one-shot receipt patterns. Existing read-only probe and production bootstrap must remain unchanged and must not be repurposed by rerunning them. |

## A. Selective Worker Isolation

**Recommendation:** add `app.background-tasks.audio-only`, environment mapping `APP_BACKGROUND_TASKS_AUDIO_ONLY`, default `false`. Preserve `app.background-tasks.enabled` as the hard master stop and `app.audio.worker-enabled` as the worker stop. No new queue, executor, scheduler library or worker algorithm. The new flag is a suppression mode, not permission to expose the application.

| Master | Audio-only | Audio worker | Worker | Six scheduled jobs / ready-event storage recovery |
|---|---|---|---|---|
| false | false or true | any | OFF | OFF / OFF |
| true or missing | false or missing | true or missing | ON | Existing behavior / ON |
| true or missing | false or missing | false | OFF | Existing behavior / ON |
| true | true | true | ON | OFF / OFF |
| true | true | false | OFF | OFF / OFF |

Implement strict validation of both master and new mode before dependent beans, even when master=false; malformed/empty/unresolved new values fail startup. Existing worker-flag semantics remain unchanged. The effective scheduled-work condition is `master && !audioOnly`. Apply it both to `@EnableScheduling` registration/accidental-post-processor guard and the storage ready listener. Keep manual service methods callable for compatibility; the later operation contract must separately exclude billing, correction, withdrawal and recovery requests. Keep strict storage and acceptance/billing-key startup guards enabled.

**Important boundary:** audio-only still runs `recoverInterruptedBatch()` and `claimNext()` against the global audio queue. Require a private, bounded preflight proving zero pending/processing/claimed work and no unresolved storage-recovery debt, plus one JVM and fenced competing writers, before enabling it. Admit only the two new synthetic uploads afterwards. Any existing work, unexpected writer or failed claim makes the trial HOLD; do not drain it, retry it or add a general per-tenant scheduler framework. Failed owned work is retained for separate review. The synchronous coordinator's owned staging cleanup is not disabled by this mode.

Required tests extend `config/BackgroundTasksConfigurationTest.java` and `service/storage/StorageMutationRecoveryServiceTest.java`: all truth-table rows; master=false with audio-only=true; malformed mode before dependencies; zero scheduler post-processors and zero ready-recovery calls in audio-only; worker ready/claim path still available; worker=false still wins; accidental `@EnableScheduling` and custom post-processor rejected; legacy six schedules preserved; manual services and strict startup guards unchanged. Add a controlled worker test for startup/global-queue behavior rather than implying row ownership isolation exists.

## B. Minimal First-Admin And Buyer Provisioning

**Recommendation:** a separate operator wrapper plus standalone Java JDBC helper using existing patterns, never an HTTP endpoint, ApplicationRunner, acceptance flag, seed replay or automatic startup feature. It exposes only read-only `inspect`, `promote-first-admin`, and `grant-test-entitlement`. No arbitrary SQL, DDL, grants, password generation or provider call. Signup and real email verification remain normal application flows with user-supplied profile and consent.

Common contract: MA only; stopped application and fenced other writers; verified recoverable prechange boundary; exact target/source/schema and private principal match; existing approved credential references; loopback connection; private input rather than process arguments; allowlisted environment; bounded timeouts/output; protected no-symlink/no-overwrite receipt directory. Each mutation consumes a distinct expiring approval and durable exclusive claim before opening a write transaction. Pin helper/driver/artifact identities. Prepared statements, expected-before-state checks and affected-row counts are mandatory. No principal/token/SQL/driver diagnostics in public output. Private records retain the existing designation reference, internal row identity and nonsecret state, not copies of contacts or credentials.

| Operation / observed state | Required result |
|---|---|
| First admin: designated account absent, deleted, unverified, wrong role, ambiguous match, or active refresh session | HOLD, zero writes. Do not auto-create, verify, resurrect or silently clear sessions. Register/verify/logout through the normal flow first. |
| First admin: exact verified active USER, no existing ADMIN row | In one transaction, promote exactly one user and append one existing `USER_ROLE_CHANGE` / `USER` / `SUCCEEDED` audit. Use nullable actor for the external operator, fixed first-admin reason and minimal role/deletion before/after states; no invented ADMIN actor or personal reason text. |
| First admin: any ADMIN row already exists, including deleted historical ADMIN | No fresh bootstrap. Only an exact committed receipt plus matching target/audit/current state returns ALREADY_APPLIED with zero new writes. Every other case is HOLD for existing-admin recovery, not another promotion. This is deliberately stricter than the normal active-admin API. |
| Buyer: distinct verified active USER, approved existing active plan matching account type, no membership, no provider/billing/payment history, no active refresh session | Insert exactly one `user_subscriptions` row with approved plan, billing-cycle label, start/expiry and ACTIVE status; pending fields null. Do not modify plan price/quota, mark payment success or create an agreement/order/payment/receipt. |
| Buyer: membership already exists | Exact prior operator receipt plus matching row gives ALREADY_APPLIED without DML; otherwise HOLD. A reviewed adjustment of an existing row can use Local Subscription Correction, but the new helper must not upsert or overwrite it. |
| Either operation: lock timeout, competing request, ambiguous COMMIT, crash before receipt publication, or state drift | Consume/retain the claim, fail closed and do not automatically retry. Allow read-only reconciliation against private intent, row state and role audit; no second mutation under the same claim. Absence after an uncertain attempt requires MA review and new authority. |

First-admin uniqueness needs a transaction-level no-ADMIN predicate lock and target lock under verified MySQL isolation, not a count followed by an unlocked update. Fence all non-tool writers and serialize the helper; prove with two independent DB connections/competing principals that only one promotion commits. Avoid relying on mocked JDBC or a local lock alone. The membership unique-user constraint prevents duplicate inserts but does not replace state/plan locking. Do not release native use until the actual lock/query strategy passes a disposable MySQL concurrency test.

Audit decision: reuse the existing transactional database audit for role promotion. For first entitlement creation, keep a durable private operator intent/commit/reconciliation receipt with exact inserted-row state and zero provider deltas; do **not** invent a correction record or misuse `USER_SUBSCRIPTION_CORRECTION`. No audit enum/schema extension is proposed. PG must accept this narrow operator-audit coverage before native use; application correction history does not claim to cover this initial grant. Failure to publish the receipt produces HOLD, not success. Neither helper manufactures identity, consent or entitlement dates.

Prefer a new, explicitly designated INDIVIDUAL buyer if the user selects that classification; this avoids BUSINESS certification prerequisites, not identity requirements. Require plan quota sufficient for the approved two first downloads, with exact dates supplied privately. LocalDate expiry is day-granular and inclusive, not a 15-minute TTL. CANCELLED still permits access until expiry; subscription expiry alone also does not revoke existing Licenses. A customer account or classification must not be silently changed for convenience.

## C. Mail Boundary And Fixtures

**Current trial:** leave `EmailService` and `JavaMailSender` transport unchanged; privately reuse the approved, previously tested Gmail configuration reference after MA verifies effective sender/TLS/timeouts/origin. Before any signup/reset action that sends mail, require a separately reviewed mail-stage contract for destination/port-scoped SMTP egress at **both provider and app layers**, finite duration/counters and restoration of their prior rules. TCP587 is currently absent at the provider layer; do not assume app permission exists, open SMTP globally, tunnel it through443 or move firewall work into WI055. Only MA may perform an explicitly authorized change later. Historical Gmail receipt does not establish new-host delivery. Accept only correlation-only send outcome plus recipient acknowledgment and correct-origin verification/reset page. Signup success alone is insufficient because send exceptions are caught. No reset submission, resend loop, SMTP debug logs or token-bearing screenshots. The buyer verification message requires a separately designated recipient; the existing first-admin designation is not reusable authority for another account.

**Provider-neutral boundary:** token persistence, expiry, verification/reset rules and HTML content stay in `EmailService`; only the private `sendEmail` transport seam changes if a provider is later selected. At that time, extract a single composed delivery interface with an SMTP implementation preserving behavior and one SENS implementation, not a provider registry, fallback router, outbox or automatic retries. No interface or Naver implementation is needed for the current Gmail trial. A later API adapter requires explicit request/authentication/error/timeout/redaction tests; it is not an environment-only SMTP replacement. Do not silently fail over between providers or create duplicate deliveries.

MA/user supplied the 2026-09-17 SENS MAIL integration direction and these official references: [integration guide](https://guide.ncloud-docs.com/docs/en/sens-integrationguide), [mail request API](https://api.ncloud-docs.com/docs/sens-mail-request-create). They were **not fetched in this no-remote WI**. No legacy Cloud Outbound Mailer implementation is proposed; a future selected-provider WI must freeze the actual endpoint/authentication contract from current official documentation. No free-quota, pricing, setup-only cost or account-transfer guarantee is made. Client-owned account creation, activation and subscription are outside authority.

Reuse deterministic PCM generation and pair-check logic from `scripts/validation/wav-browser/generate_fixtures.py` and `probe_pairs.py` through a small new local helper with an explicit empty output directory, fixed two-file cap, no overwrite and no server launcher. Do not run those historical scripts unchanged: WI057 records their large default set and hard-coded prior roots. Generate two 10-second stereo 44,100Hz/16-bit WAVs; use synthetic title/BPM/tonality, no thumbnail/tags or personal metadata. Verify original hash, full decoded duration, READY and 128,000bps stream before explicit activation.

## Explicit Mutation Ledger For A Future Window

These are proposed expected deltas, **not approval to perform them**. The later MA packet must bind private object identities, before/after counts and the exact request allowlist. No unrestricted browser crawl or side-effecting GET may be treated as read-only.

| Step | Allowed bounded delta / retention and failure rule |
|---|---|
| Signup, at most two accounts | At most two USER rows; two mandatory consent records per new account, marketing only after explicit opt-in. At most two verification-token inserts and two verification emails; normal token replacement deletes only previous verification tokens for that exact account. Fresh accounts should have none. No fabricated profile or assent. |
| Verification and reset-link trial | At most two verification-token used flags plus two user verified flags. One reset-token insert/one reset email for the designated first-admin recipient; normal replacement deletes that user's earlier reset tokens only. Expect none initially or HOLD for revised delta. Verification expiry is24h; reset expiry is1h. Do not submit/reset password, consume reset token, or delete retained evidence. |
| Authentication and promotion | At most two successful logins and corresponding logouts per principal: bounded refresh-hash writes/clears, no automatic refresh/retry. One role-row update plus one role audit and private operator receipt. Fresh post-promotion authentication and `/api/users/me` must show ADMIN; no token contents retained. Logout does not promise immediate access-JWT revocation. |
| Buyer entitlement | At most one new membership row and private operator receipt, zero plan/correction/payment/billing-agreement mutations. Test the distinct USER's unentitled denial before grant if this coverage is selected. No positive buyer test using ADMIN. |
| Two new WAV uploads and processing | Two Track inserts, at most two original and two stream objects; pending/claim/attempt/analysis/waveform/READY field updates. For the success-only fresh-upload path, source predicts eight journal inserts: four CREATE and four DELETE-intent records from old-original/claim release. The latter refer to retained originals and must finish as shared-reference retention, not actual original deletion. Pin this prediction in local tests before the operation contract; unexpected count/state is HOLD. Journals persist even when a business transaction rolls back. |
| Activation and Public Listening | At most one activation and one closeout deactivation for each owned Track. Full200/single-Range206/invalid416 observations produce zero License, Download History or downloadCount deltas. No catalog publication to general public access. |
| Official Download | At most two first downloads: two License rows, two Download History rows and two downloadCount increments in total. At most two re-downloads with zero new accounting. Accounting can commit before the client finishes receiving bytes: reconcile before any replay. Denied anonymous/unentitled access must produce zero accounting. Verify original SHA-256 privately without recording storage keys. |
| Failure and closeout | Stop new admission; preserve accounts, Licenses, membership, originals, journals and audits. Existing synchronous cleanup may remove only operation-owned uncommitted/staging output under the reviewed lifecycle; this is not a purge instruction. No user/Track DELETE, schema reset, history removal, source deletion or broad orphan recovery. Retain uncertain output and claims for separate review. |

Carry WI057 ceilings without expanding them: **<=15min, <=64MiB aggregate wire, stop admission at48MiB with16MiB in-flight reserve; <=3 emails, two accounts, one promotion, one buyer grant, two Tracks, two first downloads and two re-downloads.** Each WAV is1,764,044bytes; originals total3,528,088bytes; allow <=256KiB per MP3. Cap media/spool/temp growth32MiB and DB/log growth32MiB. One JVM/encoder; no backup or load-test overlap. Free memory>=1GiB, free disk>=max(20%,10GiB)+next admitted work, free inodes>=10%; retain WI057 resource/error stop conditions. Limits are proposals until enforced by the reviewed runtime contract, not operating capacity or a hard provider invoice cap. Mixed smoke/MIXED-A, full backup/restore and invitations are separate later windows.

## Implementation-Ready Split And Gates

The following are bounded successor assignments for MA to turn into skill-created handoffs, **not new WI numbers or subdelegation by SA**. Package roots are defined under Source Pointers above. No overlap with WI055 tooling.

| Owner / slice | Exact proposed file scope | Required local acceptance |
|---|---|---|
| SE-A: audio-only suppression | Edit `config/BackgroundTasksEnabledCondition.java`, `config/BackgroundTasksConfiguration.java`, `service/storage/StorageMutationRecoveryService.java`, `src/main/resources/application.yml`; add `config/ScheduledBackgroundTasksEnabledCondition.java`. Extend corresponding `config/BackgroundTasksConfigurationTest.java` and `service/storage/StorageMutationRecoveryServiceTest.java`; add `service/audio/TrackAudioProcessingWorkerIsolationTest.java` under test root. | Truth table and malformed-config fail-closed tests; no scheduled/ready recovery in audio-only; existing worker/master behavior and strict guards unchanged. Leave worker/transaction algorithm unchanged; test global-queue prerequisite and eight-journal success prediction. SE-A alone owns application.yml. |
| SE-B: offline provisioning | Add `scripts/deployment/feature-provisioning.py`, `RuntimeFeatureProvisioner.java`, `test_feature_provisioning.py`, `RuntimeFeatureProvisionerHarness.java` within `scripts/deployment/`. Existing probe/bootstrap/readiness files remain reference-only. | Approval/path/pin/expiry/claim/redaction tests; absent/present/drift/replay branches; exactly-one-row/audit assertions; no Spring start/provider call; injected transaction/receipt failures; two-connection disposable MySQL concurrency and unique-user tests. Do not claim native safety from mocks alone. |
| SE-C: small media helper and trial preparation | Add `scripts/validation/feature-trial/fixtures.py` and `test_fixtures.py`; no frontend, SMTP or Naver source change. MA separately authors the expected-delta request/closeout packet with private references. | Exactly two deterministic WAVs, fixed byte counts/duration/hash, no overwrite/path escape, pair-check refusal on invalid output. Reuse focused `EmailServiceTest`, `UserServiceTest`, `service/auth/AuthServiceTest`, `DownloadServiceTest`, `DownloadConcurrencyContractTest`, and `AdminSubscriptionCorrectionServiceTest` as regression targets, not new provider work. |
| RE + PG, then MA | Independent review/test evidence for A/B/C; separately scoped artifact rebuild/pinning and future target contract. | Review DML and audit coverage, concurrency, global worker scope, token/mail privacy, exact egress/admission/stop controls. Native requires WI055 safety review and WI058 PASS, fresh pins, approved data deltas, private inputs and recoverable prechange boundary. MA alone handles native operations. |

Human inputs still needed: private signup profile/consents; a distinct buyer designation and account type; existing plan/quota and exact entitlement dates/retention; SMTP configuration/sender reference owner and allowed-network link opener; staffed finite window, stop/closeout owner and prechange recovery evidence. Do not re-request the already designated first-admin recipient or synthetic-media choice. No Naver account, payment commitment or provider selection is needed to proceed with these local successor designs. Business classification, consent, dates, third recipient and recovery retention are not technical defaults.

## Risks / Rollback

Rollback here means withholding the proposal; only two reports were created. For future runtime, stop admission and the owned application/worker, fence ingress and restore the reviewed quarantine settings with **master=false first**. Merely removing audio-only while master=true would re-enable all scheduled work. Confirm no encoder survives before release/rollback, and never auto-recover a failed queue. Preserve older artifacts/pins and require a fresh check for any replacement release.

Retain trial data and receipts; do not automatically demote the only ADMIN, delete accounts/Licenses, or overwrite the database from backup. Logout/expiry are not complete revocation, and CANCELLED membership remains service-enabled through expiry. Restricted ingress/application stop is the immediate containment; later restart requires approved deactivation of only the two owned Tracks and rechecked authentication/access. A future destructive data rollback or retention purge needs separate authority. This initial feature window is not backup/restore, billing, capacity or stakeholder acceptance.

## Commands, Tests And References

- Executed only local `Get-Content`, targeted `rg`/`rg --files` and scoped `git --no-optional-locks status --short --branch`; read the handoff, prior evidence, governing projections/policies, skill and relevant source/tests. No private credential/configuration body was inspected.
- Applied `.agents/skills/create-wi-evidence-pack/SKILL.md` after the WI059 handoff existence check. Only this report and the linked summary were written. Product tests, fixture generation, builds, DB, SMTP, provider, browser, SSH and other native/network operations are **NOT RUN**.
- Scoped direct checks: two reports,14 required metadata fields,5 local links and6 dependency paths; zero issues. Terminal newline, trailing whitespace, ASCII and absence of literal email addresses checked. `git diff --check -- <two owned paths>` exited0; both files are untracked, so direct content checks supply their actual validation. No whole-repository document or product-test PASS is claimed.
- Tier0 reused in the injected order: STD-001 core-principles, STD-004 documentation-standards, STD-002 development-standards, STD-005 glossary. Tier1: SA role/security projection, access-control-policy and quality-gates. Tier2: current decision entry, approved REQ007, WI057 source evidence, WI059 handoff and evidence-pack skill. No new architecture/library/provider decision, context bundle or subagent invocation.

**Chain check:** WI059 supplies the A/B/C implementation packets and independent review inputs; it does not unblock native feature execution by itself. MA should dispatch the bounded local successors using the handoff skill and retain the separate WI055 -> review -> WI058 native gate. REQ007 and its later operational phases remain open.

## Related Documents

- [User summary](../user/WI-20261011-ATS-059-summary.md)
- [Owned handoff](WI-20261011-ATS-059-handoff.md)
- [Source audit and budget baseline](WI-20261011-ATS-057-evidence-pack.md)
- [Approved REQ007](../user/REQ-20261011-ATS-007.md)
