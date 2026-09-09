---
version: 1.1
last_updated: 2026-09-09
project: ATS
owner: re
category: evidence-pack
status: stable
dependencies:
  - path: WI-20260908-ATS-001-handoff.md
    reason: Approved WI scope and ownership
  - path: ../user/REQ-20260908-ATS-001.md
    reason: Approved bounded release closeout
---

# Evidence Pack: WI-20260908-ATS-001

> Purpose: Record isolated renewal terminal-failure and download-entitlement regression evidence.

## Summary (one-liner)

PASS: two new H2 integration scenarios and stronger existing download unit assertions; focused JUnit total increased from 16 to 18, with zero failures, errors, or skips. No product defect was found in the exercised scenarios.

## Scope / DoD Check

- [x] Three deterministic declines use one payment-order row, order reference, billing-period start, and logical command key; only provider attempt identity advances.
- [x] Every provider callback observes a committed PROCESSING command and a null retry gate; both the due-candidate query and a same-day service scan return no work. Repeated scans after failure are also empty.
- [x] Failure counts advance 1, 2, 3; attempts 1 and 2 retain ACTIVE billing with a next-day retry; attempt 3 sets SUSPENDED and clears the retry gate.
- [x] Subscription access remains through the inclusive grace end, due date + 3 days. Its original start date and contractual next-billing date are unchanged.
- [x] Scans on grace end, the following day, and the next monthly date make no further provider calls; one FAILED order and zero paid-ledger rows remain.
- [x] Actual DownloadService + H2 repositories issue licenses/history before and on grace end. A first download the next day fails with NO_ACTIVE_SUBSCRIPTION before storage access or persistence.
- [x] Licensed repeats after expiry return the resource without additional licenses, history, or track counters.
- [x] Only the two owned test files and this WI's evidence/summary were edited. Shared provider support was reused unchanged.
- [x] No product source, build dependencies, live DBs, local configuration, servers, other worktrees, or unrelated files were changed. No commit or nested delegation was performed.

## Reference Documents (Tier 0-2)

Injected context and scope pointers from the handoff were applied as follows. Tier 0 was supplied by MA; the truncated documentation-metadata portion was checked against the local standard.

| Tier | Document | Reason |
| --- | --- | --- |
| 0 | docs/standards/core-principles.md (STD-001) | Constitution, approved execution, truthful boundaries |
| 0 | docs/standards/documentation-standards.md (STD-004) | Metadata, English documents, two-set outputs |
| 0 | docs/standards/development-standards.md (STD-002) | Java 17, reuse-first tests, evidence pointers |
| 0 | docs/standards/glossary.md (STD-005) | Official Download, License, Subscription, WI terminology |
| 1 | .claude/agents/re.md | Independent execution and regression baseline |
| 1 | docs/policies/security-policy.md | No real provider/SMTP/secrets or runtime mutations |
| 1 | docs/policies/quality-gates.md | Scoped reuse and reproducible verification |
| 2 | docs/payment/acceptance-test-checklist.md, sections 6-7 | Grace access and repeated-renewal-failure acceptance |
| 2 | docs/SR/SR-93.md, lines 151-152 and 273-276 | Three-day grace, up to three attempts, stable command |
| 2 | RecurringRenewalService.java; PaymentCommandTransactionService.java; DownloadService.java | Actual orchestration, transactional state, download service |
| 2 | RecurringRenewalCommandIntegrationTest.java; DownloadServiceTest.java; BillingAgreementCommandIntegrationTestSupport.java | Existing test fixtures and transaction-checking fake provider |

Injection rules: `.claude/config/context-injection-rules.json`; assignee `re`; task type `testing-qa`; agent_required_tiers `[0]`, supplemented by the handoff's security and integration context. Project tag ATS was confirmed in `.claude/config/workspace.json`.

Skills used: `.agents/skills/test/SKILL.md` followed by `.agents/skills/create-wi-evidence-pack/SKILL.md`. The required handoff existed before execution.

## Evidence Pointers

| Location | Evidence |
| --- | --- |
| [RecurringRenewalCommandIntegrationTest.java](../../src/test/java/com/atstudio/atstudio/service/RecurringRenewalCommandIntegrationTest.java), line 115 | Before each test, assert the actual JDBC URL begins with jdbc:h2:mem:; force embedded test-database replacement |
| Same file, line 265: threeConsecutiveFailuresConsumeRetryGateAndSuspend | Real Spring transaction boundaries, reloaded H2 state, one logical command, consumed retry gate, bounded attempts, terminal suspension, inclusive grace, no subsequent charges or paid ledger |
| Same file, line 351: terminalRenewalGraceBoundaryControlsFirstDownloadButNotLicensedRepeat | Actual DownloadService and repositories exercise first downloads and licensed repeats across expiry; counts remain 2 licenses, 2 history rows, track counters 1/1/0 |
| Same file, line 486: downloadOn | A scoped Mockito static mock pins only LocalDate.now(); all other LocalDate methods call real implementations |
| [DownloadServiceTest.java](../../src/test/java/com/atstudio/atstudio/service/DownloadServiceTest.java), lines 101 and 238 | Existing-license response identity and zero quota/history interactions; denied first download has zero write/storage side effects |
| [BillingAgreementCommandIntegrationTestSupport.java](../../src/test/java/com/atstudio/atstudio/service/BillingAgreementCommandIntegrationTestSupport.java) | Reused fake provider rejects provider calls inside a local transaction; it performs no network calls |
| [PaymentCommandTransactionService.java](../../src/main/java/com/atstudio/atstudio/service/PaymentCommandTransactionService.java), lines 237, 472 and 1095 | Existing claim, deterministic failure and suspension policies, unchanged |
| [UserSubscriptionRepository.java](../../src/main/java/com/atstudio/atstudio/repository/UserSubscriptionRepository.java), line 27 | Inclusive expiresAt >= today entitlement predicate, exercised through H2 |
| [SubscriptionScheduler.java](../../src/main/java/com/atstudio/atstudio/service/SubscriptionScheduler.java), line 86 | Separate expiry-state job, inspected only, not executed |

## Commands & Outputs

Workdir: `C:/Users/jm991/Desktop/project/ATStudio`.
Branch: `codex/v1-release-rehearsal-fixes`.
HEAD observed after focused verification: `2f2e9eccadd9ae9626fe8273bc635068d42b09b0`.
JAVA_HOME was set only in each test command's child shell; no persistent environment file was edited.

Baseline and post-change commands were identical:

```powershell
$env:JAVA_HOME = 'C:/Program Files/Java/jdk-17'
.\gradlew.bat test --offline --no-daemon --console=plain --tests 'com.atstudio.atstudio.service.RecurringRenewalCommandIntegrationTest' --tests 'com.atstudio.atstudio.service.DownloadServiceTest'
```

The offline flag prohibited Gradle dependency downloads. No dependency change or provider/SMTP call was needed. Both runs actually executed `:test`, rather than reporting it UP-TO-DATE. Gradle exit codes were 0; baseline elapsed 32s and final elapsed 34s.

JUnit XML was parsed with PowerShell's XML parser, not console-string test-count matching:

```powershell
Get-ChildItem -LiteralPath build/test-results/test -Filter 'TEST-*.xml' | ForEach-Object {
    [xml]$report = Get-Content -LiteralPath $_.FullName -Raw
    [pscustomobject]@{
        File = $_.Name
        Tests = [int]$report.testsuite.tests
        Failures = [int]$report.testsuite.failures
        Errors = [int]$report.testsuite.errors
        Skipped = [int]$report.testsuite.skipped
        Seconds = $report.testsuite.time
    }
}
```

## Tests

Mode: Java 17 / JUnit5 / Gradle 9.3.0. Final status: PASS.

| Suite | Baseline tests | Final tests | Passed | Failures | Errors | Skipped | Final XML seconds |
| --- | ---: | ---: | ---: | ---: | ---: | ---: | ---: |
| DownloadServiceTest | 10 | 10 | 10 | 0 | 0 | 0 | 3.435 |
| RecurringRenewalCommandIntegrationTest | 6 | 8 | 8 | 0 | 0 | 0 | 15.221 |
| Total | 16 | 18 | 18 | 0 | 0 | 0 | 18.656 |

Baseline XML also reported zero failures, errors and skips (suite seconds: 3.585 and 16.050). No existing tests were removed.

Full generated test logs, including Hibernate stdout/stderr and individual test cases:
- Renewal JUnit XML: `build/test-results/test/TEST-com.atstudio.atstudio.service.RecurringRenewalCommandIntegrationTest.xml`, timestamp `2026-09-08T08:55:25.725Z`, SHA-256 `4A7C11104DF87D1E547E33F9D93669B05BE4CBED4BB07D39A32EB7988D862E17`.
- Download JUnit XML: `build/test-results/test/TEST-com.atstudio.atstudio.service.DownloadServiceTest.xml`, timestamp `2026-09-08T08:55:22.227Z`, SHA-256 `F4BB83AE0E6C43F7FE0C7F9FED72D3376BABC97C26DE6ACD1B02BAC6A6233527`.
- Gradle HTML report: `build/reports/tests/test/index.html`.

Availability correction (2026-09-09, REQ-20260909-ATS-003): These are generated,
Git-ignored local output paths, not files included in a source checkout or the
205-artifact archive. The commands above can produce new reports, not recover
this historical run's original bytes. The dated results and hashes are unchanged.

Generated build reports are overwritten by later Gradle runs; the counts, timestamps, hashes and complete final console output below preserve this run's identity.

Additional checks: `git diff --check` exited 0. Git's CRLF-to-LF notice for DownloadServiceTest.java was informational; no line-ending-only rewrite was performed. Before deliverable creation, `git diff --name-only` listed only the two owned test files. All relative Markdown links in the two owned deliverables resolved successfully in a scoped read-only link check.

## Risks / Rollback

- This is H2 in-memory evidence with a fake provider, mocked crypto/email/receipt services, and in-memory storage resources. It is not real Toss TEST/LIVE, MySQL locking/concurrency, SMTP delivery, browser HTTP transfer, or production acceptance.
- Renewal business dates are passed explicitly. Only the no-argument date lookup inside the download call is pinned; audit timestamps and scheduler wall clocks are not advanced. No real midnight or elapsed multi-day test was performed.
- The test proves entitlement rejection after expiresAt even while the row still carries ACTIVE status. The separate SubscriptionScheduler.processExpiredSubscriptions job and its EXPIRED state transition were not executed or claimed as verified.
- The provider-time callback and reentrant scan prove committed gate visibility and sequential duplicate suppression. They do not constitute a multi-process race or multi-server scheduler proof.
- EmailService invocation count is verified as three, but mail wording and actual delivery are not a new acceptance result.
- Only these two focused classes were run; no whole-suite coverage, frontend or production readiness claim is made.
- No product defect was discovered in these scenarios. Policy and product implementation remain unchanged.
- Rollback: remove only the two newly added integration methods, their owned fixture/import/configuration additions, and the added unit assertions using a reviewed test-only reverse patch. Preserve concurrent changes and all pre-existing untracked artifacts. This WI's two documents can be retired separately if approved; no blanket checkout/reset/delete is appropriate.

## Follow-ups

WI-001 is complete and releases its dependency on **WI-20260908-ATS-002**. MA must now generate the WI-002 handoff with create-wi-handoff-packet and delegate its approved small release-safety changes. RE performed no nested delegation and did not open WI-002 or close the overall REQ. No additional product-policy decision is required by this WI's results.

## Full Final Console Output

```text
To honour the JVM settings for this build a single-use Daemon process will be forked. For more on this, please refer to https://docs.gradle.org/9.3.0/userguide/gradle_daemon.html#sec:disabling_the_daemon in the Gradle documentation.
Daemon will be stopped at the end of the build
> Task :compileJava UP-TO-DATE
> Task :processResources UP-TO-DATE
> Task :classes UP-TO-DATE
> Task :compileTestJava
> Task :processTestResources UP-TO-DATE
> Task :testClasses
Java HotSpot(TM) 64-Bit Server VM warning: Sharing is only supported for boot loader classes because bootstrap classpath has been appended
2026-09-08T17:55:40.970+09:00  INFO 12832 --- [ionShutdownHook] j.LocalContainerEntityManagerFactoryBean : Closing JPA EntityManagerFactory for persistence unit 'default'
Hibernate: drop table if exists admin_operation_audit_logs cascade
Hibernate: drop table if exists admin_subscription_corrections cascade
Hibernate: drop table if exists album_likes cascade
Hibernate: drop table if exists album_tracks cascade
Hibernate: drop table if exists albums cascade
Hibernate: drop table if exists answers cascade
Hibernate: drop table if exists billing_agreements cascade
Hibernate: drop table if exists company_certification_audit_logs cascade
Hibernate: drop table if exists company_certification_documents cascade
Hibernate: drop table if exists company_certifications cascade
Hibernate: drop table if exists email_verification_tokens cascade
Hibernate: drop table if exists licenses cascade
Hibernate: drop table if exists likes cascade
Hibernate: drop table if exists notice_attachments cascade
Hibernate: drop table if exists notices cascade
Hibernate: drop table if exists password_reset_tokens cascade
Hibernate: drop table if exists payment_entitlement_corrections cascade
Hibernate: drop table if exists payment_operation_audit_logs cascade
Hibernate: drop table if exists payment_orders cascade
Hibernate: drop table if exists payment_receipts cascade
Hibernate: drop table if exists payment_reconciliation_incidents cascade
Hibernate: drop table if exists payment_refunds cascade
Hibernate: drop table if exists payment_settlement_import_attempts cascade
Hibernate: drop table if exists payment_settlements cascade
Hibernate: drop table if exists playlist_tracks cascade
Hibernate: drop table if exists playlists cascade
Hibernate: drop table if exists question_attachments cascade
Hibernate: drop table if exists questions cascade
Hibernate: drop table if exists site_settings cascade
Hibernate: drop table if exists social_accounts cascade
Hibernate: drop table if exists storage_mutations cascade
Hibernate: drop table if exists subscription_payments cascade
Hibernate: drop table if exists subscriptions cascade
Hibernate: drop table if exists tags cascade
Hibernate: drop table if exists track_downloads cascade
Hibernate: drop table if exists track_tags cascade
Hibernate: drop table if exists tracks cascade
Hibernate: drop table if exists user_consents cascade
Hibernate: drop table if exists user_subscriptions cascade
Hibernate: drop table if exists users cascade
Hibernate: drop table if exists whitelist_channels cascade
Hibernate: drop table if exists whitelist_export_batches cascade
Hibernate: drop table if exists whitelist_export_items cascade
> Task :test

BUILD SUCCESSFUL in 34s
5 actionable tasks: 2 executed, 3 up-to-date
Consider enabling configuration cache to speed up this build: https://docs.gradle.org/9.3.0/userguide/configuration_cache_enabling.html
```

## Related Documents

- [WI-001 Handoff](WI-20260908-ATS-001-handoff.md)
- [Approved REQ](../user/REQ-20260908-ATS-001.md)
- [WI-001 User Summary](../user/WI-20260908-ATS-001-summary.md)
- [Payment Acceptance Checklist](../../docs/payment/acceptance-test-checklist.md)
- [SR-93](../../docs/SR/SR-93.md)
