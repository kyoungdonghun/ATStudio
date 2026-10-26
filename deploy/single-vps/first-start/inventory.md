---
version: 1.0
last_updated: 2026-10-11
project: ATS
owner: SA
category: reference
status: draft
dependencies:
  - path: README.md
    reason: Candidate boundaries and target proof gates
  - path: ../../../src/main/resources/application.yml
    reason: Actual property names and defaults
---

# First-Start Effect Inventory

> Purpose: Separate automatic execution, request-driven work, startup validation and external transport.

## Verification Boundary

SA inspected public local source on2026-10-11, Windows/PowerShell, branch `main`,
HEAD `58b1011dad796017e47dbcc82a4225ca449bcdf6`, dirty tree. Paths below are relative
to `src/main/java/com/atstudio/atstudio/`. WI016 is the parallel implementation
owner of the new master flag; this inventory describes existing behavior and
the required suppression contract, not target execution or a deployed default.
WI018/019 must verify final source and WI020 artifact identity together.

## Automatic and Startup Entries

| Source / entry | Normal effect | Required first-start boundary |
|---|---|---|
| `AtStudioApplication`; `@EnableScheduling` (relocated by WI016) | Registers Spring scheduled methods | Master false must prevent schedule registration; do not invent `spring.task.scheduling.enabled` |
| `service/SubscriptionScheduler.processRecurringRenewals` | 00:00 Asia/Seoul; Provider renewal, DB state, possible payment-failure mail | No scheduled execution with master false |
| Same class `processExpiredPaymentOrders` | 00:10; expires local payment orders | No scheduled execution |
| Same class `processExpiredSubscriptions` | 00:30; expires local subscriptions | No scheduled execution |
| `service/PaymentReconciliationService.reconcilePaymentLedgersOnSchedule` | 01:00; ledger/provider reconciliation, recovery/Incident writes, optional mail | No scheduled execution; notification false alone is insufficient |
| `service/WithdrawalBillingCleanupCoordinator.retryFailedCleanups` | 01:15; claim/retry rows, Provider billing-key deletion | No scheduled execution |
| Same class `cleanupAfterCommit` | AFTER_COMMIT event from request-driven withdrawal; immediate Provider cleanup | Not a startup scheduler. No write requests/events; master flag is not an API authorization change |
| `service/storage/StorageMutationRecoveryService.recoverOnStartup`, `recoverBatch` | ApplicationReadyEvent plus60s delay; journal claims/transitions/retries and orphan cleanup/deletion | Master false must suppress BOTH readiness entry and scheduled registration |
| `service/audio/TrackAudioProcessingWorker.onApplicationReady`, executor | Independent executor, interrupted-work recovery, immediate poll then5000ms; original/derivative IO, DB changes, FFmpeg child | Master false plus audio false; audio false alone does not cover Spring jobs |
| `bootstrap/TestUserBootstrapRunner.run` | Writes QA users, subscriptions and fixtures | test-users false; no acceptance profile |
| `bootstrap/AcceptanceSubscriptionPlanBootstrapRunner.run` | Acceptance+test-user conditional canonical plan inspection, refuses mismatches | acceptance false AND test-users false; prod profile |
| `config/AcceptanceStartupGuard.run/validate` | Rejects prod/acceptance conflicts; always validates BillingKeyCrypto config before early return | Retain; blank Toss keys do NOT waive independent billing encryption keyring |
| `config/JwtConfig.validate`; `security/JwtTokenProvider.init` | JWT key validation and signing-key initialization | Retain; real existing key use needs later approval |
| `service/storage/LocalStorageService.init/initializeRoot` | Validate explicit disjoint real roots; `Files.createDirectories`; symlink/permission checks | Retain; may create directories despite workers OFF; approve local writes |
| `config/StorageIntegrityStartupGuard.run`; `StorageIntegrityService.inspect` | DB reference inspection and filesystem checks; prod requires strict audit | audit=true AND strict=true, fail closed |
| Spring datasource/JPA/container | Pool connections/metadata/Hibernate validate, temp files/logs/listener; MySQL itself writes engine/log state | SQL init never; DDL validate; exact existing DB/paths/logs approved later; not read-only proof |

## Outbound Transports and Defaults

| Path / triggers | Actual destinations or target configuration | Candidate treatment |
|---|---|---|
| `service/EmailService.sendVerificationEmail/sendPasswordResetEmail` from registration/auth | JavaMailSender, `spring.mail.host/port`, YAML defaults localhost:1025 | Password login false gates these flows; force host127.0.0.1/port1, username/password empty, no connection test |
| `EmailService.sendSubscriptionPaymentFailureEmail`; `RecurringRenewalService` | Same SMTP transport, not guarded by password-login policy | Prevent automatic renewal; no request-driven payment. Actual egress fence still required |
| `EmailService.sendPaymentReconciliationIncidentAlert`; `PaymentReconciliationIncidentService` | Same SMTP, optional operator email | Notification false and empty operator address, plus disabled schedule; not universal mail off |
| `service/auth/OAuth2Service`; `config/AppConfig.restClient` | Google `https://oauth2.googleapis.com/token`, `https://www.googleapis.com/oauth2/v2/userinfo`; Kakao `https://kauth.kakao.com/oauth/token`, `https://kapi.kakao.com/v2/user/me`; Naver `https://nid.naver.com/oauth2.0/token`, `https://openapi.naver.com/v1/nid/me` | All client IDs/secrets blank through CLI; no social request; endpoint strings remain in source |
| `service/payment/provider/recurring/TossBillingProvider` | Java HttpClient; default `https://api.tosspayments.com/v1/billing/authorizations/issue`, `/v1/billing/{billingKey}` for charge/delete, `/v1/payments/{paymentKey}/cancel`, `/v1/payments/orders/{orderId}` for lookup | Both Toss keys blank through CLI; no schedule or payment request; URLs persist in PaymentProperties and YAML, defaults are NOT inert |
| JDBC/Hikari | Existing `SPRING_DATASOURCE_URL`, username/password; base URL fallback localhost:3306/atstudio | Reject fallback/unknown target in later private preflight; only exact approved existing loopback DB; no disclosure here |
| DNS and host agents | Transport name resolution and possible loopback/Unix relays | Exact-loopback allowance is not a port/protocol restriction; audit relays and prove IPv4/IPv6 deny before Java |
| `service/audio/FfmpegAudioEncoder` | Local ProcessBuilder child; stderr/stdout discarded; reads/writes audio | Worker absent; no upload/explicit work; cgroup stop covers descendants if launched unexpectedly |

Source scan covered `@Scheduled`, `@PostConstruct`, ApplicationRunner,
ApplicationReadyEvent, event listeners, executors, HTTP client constructors,
JavaMailSender, ProcessBuilder and URL literals. It is not a proof about native
libraries, private effective config, previously deployed bytes or all possible
network syscalls. `PaymentReceiptEvidenceService` is an AFTER_COMMIT database
evidence listener, not a URL-fetching receipt client; request writes are excluded.
Callback/base/CORS URLs generate navigation links, not proof of external
connection or authority to expose the service. Existing public origin strings
in the overlay do not install a proxy or change TLS.

## Probe Contract

`controller/UtilController.getPublicCapabilities` ->
`service/UtilService.getPublicCapabilities` derives capability booleans from
configuration; no mail/OAuth call. `controller/TagController` ->
`service/TagService.getAllTags` reads repository tags under read-only transaction.
Finite request/body limits and exclusions are specified in the runbook. These
GETs do not establish scheduling absence, startup-event completion, egress denial,
or read-only behavior of the whole application. Verify those separately.

## Related Documents

- [Runbook](README.md): Target approval, finite requests and cleanup.
- [Public configuration](first-start-public.env.example): No secret values.
- [Security policy](../../../docs/policies/security-policy.md): Data/logging boundaries.
- [Application config](../../../src/main/resources/application.yml): Authoritative bindings.
