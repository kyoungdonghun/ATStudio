---
version: 1.1
last_updated: 2026-09-09
project: ATS
owner: se
category: evidence-pack
status: stable
dependencies:
  - path: WI-20260908-ATS-003-handoff.md
    reason: Approved ownership and acceptance criteria
  - path: ../user/REQ-20260908-ATS-001.md
    reason: Approved release-closeout scope and branched WI chain
---

# Evidence Pack: WI-20260908-ATS-003

> Purpose: Preserve actual monetary charge history during zero-amount billing-method registration without changing payment or entitlement policy.

## Summary (one-liner)

Fixed lastChargedAt clearing and false updates during non-monetary registration. Focused verification: **83 tests passed, 0 failures, 0 errors, 0 skips**. Product/test code and the external init script are frozen; only WI deliverables were written after the final test run.

## Scope / DoD Check

- [x] Reproduced the defect before product changes: 20 targeted tests, 6 expected assertion failures, 0 errors/skips.
- [x] Preserve both an old non-null timestamp and an initially null timestamp during preparation, key cleanup, zero-amount confirmation and completed replay.
- [x] Exercise an actual application-service prepare failure after its durable H2 claim, then same-key retry with one order and preserved history.
- [x] Preserve active and cancelled grace-period subscription plan, cycle, start/end dates and status during registration.
- [x] Actual positive SUBSCRIBE, UPGRADE and RENEWAL finalization still records a new charge time; completed replay does not rewrite it.
- [x] Keep key erasure, billing status, cancellation, failure count, retry gate and next-period behavior unchanged apart from the historical timestamp.
- [x] Correct only the stale next-billing-date sentence in schema.sql's expires_at COMMENT string. No column/type/key/constraint change or DDL execution.
- [x] Reuse the existing transaction-checking fake recurring provider without editing shared support or either WI-001 test file.
- [x] Create the explicitly authorized repo-external build-output init script; leave product build.gradle and the running JAR unchanged.
- [x] No live DB/provider/mail, persistent environment change, dependency change, restart, commit, nested delegation or full build was performed by this WI.

## Reference Documents (Tier 0-2)

| Tier | Document | Reason |
| --- | --- | --- |
| 0 | docs/standards/core-principles.md (STD-001) | Approved bounded execution and truthful evidence |
| 0 | docs/standards/documentation-standards.md (STD-004) | English two-set deliverables, metadata and pointers |
| 0 | docs/standards/development-standards.md (STD-002) | Java 17, TDD, narrowly scoped changes |
| 0 | docs/standards/glossary.md (STD-005) | Billing and Subscription terminology |
| 1 | .claude/agents/se.md | Implementation responsibilities |
| 1 | docs/policies/security-policy.md | No secrets, provider or live-state mutation |
| 1 | docs/policies/quality-gates.md | Reuse and reproducible regression |
| 2 | docs/payment/acceptance-test-checklist.md | Registration and renewal acceptance boundaries |
| 2 | docs/design/payment-integration-design.md, Authoritative Prepare Intent and Payment-Method Registration | Existing service-enabled subscription derives BILLING_AGREEMENT with amount 0 and unchanged period/access |
| 2 | BillingAgreement.java; PaymentCommandTransactionService.java; schema.sql | Authorized implementation and comment scope |
| 2 | BillingAgreementPrepareTransactionService.java; BillingAgreementApplicationService.java; BillingAgreementCleanupTransactionService.java | Read-only call-path verification |

Tier 0 supplied by MA remains applicable. Rule source: `.claude/config/context-injection-rules.json`; assignee `se`, task `implementation/testing`; required tiers `[0]`, with handoff-specified billing/security context. Skills applied: `.agents/skills/test/SKILL.md` and `.agents/skills/create-wi-evidence-pack/SKILL.md`. The WI-003 handoff existed before execution.

## Exact Cause And Change

1. BillingAgreementPrepareTransactionService.claim calls prepareRegistration for a new registration attempt. That method cleared lastChargedAt even though no charge occurred.
2. The key-cleanup path calls BillingAgreement.clearIssuedKey, which also cleared the historical timestamp.
3. BillingAgreementApplicationService recognizes amount-0 registration and confirms the key without invoking charge. Its local finalizeRegistrationOnly path nevertheless called a shared activateAgreement helper that unconditionally recorded a successful charge.

The fix removes the two historical-value resets. The shared activation helper now performs activation only; the two existing SUBSCRIBE finalization branches explicitly retain their successful-charge recording. UPGRADE and RENEWAL recording remain unchanged. This keeps registration non-monetary without adding an amount-policy branch or a new API.

## Evidence Pointers

| File / Location | Change or evidence |
| --- | --- |
| [BillingAgreement.java](../../src/main/java/com/atstudio/atstudio/entity/BillingAgreement.java), lines 100, 142, 178 | Document timestamp ownership; preparation and issued-key clearing preserve charge history |
| [PaymentCommandTransactionService.java](../../src/main/java/com/atstudio/atstudio/service/PaymentCommandTransactionService.java), lines 559, 600, 764, 1142 | Monetary SUBSCRIBE paths still record charge; registration shares activation without recording a charge |
| Same service, lines 663 and 719 | Existing UPGRADE and RENEWAL successful-charge recording remains unchanged |
| [schema.sql](../../src/main/resources/schema.sql), line 138 | COMMENT text changes from expires_at + 1 day to expires_at; this stale next-billing sentence is on user_subscriptions.expires_at, not the billing_agreements column definition |
| [BillingAgreementStateMachineTest.java](../../src/test/java/com/atstudio/atstudio/entity/BillingAgreementStateMachineTest.java), lines 23, 46, 60 | Replayed preparation, initially null activation, and old/null repeated key cleanup; unrelated state assertions retained |
| [BillingAgreementChargeTimestampIntegrationTest.java](../../src/test/java/com/atstudio/atstudio/service/BillingAgreementChargeTimestampIntegrationTest.java), lines 86, 102, 133 | Six new parameterized H2 cases for non-null/null history, failed prepare and cleanup replay, positive initial charge and completed replay |
| [PaymentCommandIndependentVerificationIntegrationTest.java](../../src/test/java/com/atstudio/atstudio/service/PaymentCommandIndependentVerificationIntegrationTest.java), line 219 | Successful renewal advances old timestamp; finalization replay preserves the resulting timestamp |
| [SubscriptionUpgradeCommandIntegrationTest.java](../../src/test/java/com/atstudio/atstudio/service/SubscriptionUpgradeCommandIntegrationTest.java), line 121 | Forced local finalization rollback preserves old time; successful retry advances it; DONE replay preserves it and the billing date |

New integration tests force embedded test-database replacement and assert the actual JDBC URL starts with jdbc:h2:mem:. They reuse BillingAgreementCommandIntegrationTestSupport's fake provider via a test-local spy for deterministic prepare behavior; shared support and WI-001 tests were not edited.

## Commands & Outputs

Working directory: `C:/Users/jm991/Desktop/project/ATStudio`.
Branch: `codex/v1-release-rehearsal-fixes`.
JAVA_HOME was overridden only in the child command shell; no local environment file was loaded or modified.

### Red Verification

```powershell
$env:JAVA_HOME = 'C:/Program Files/Java/jdk-17'
.\gradlew.bat test --offline --no-daemon --console=plain --tests 'com.atstudio.atstudio.entity.BillingAgreementStateMachineTest' --tests 'com.atstudio.atstudio.service.BillingAgreementChargeTimestampIntegrationTest'
```

Before product fixes: Gradle exit 1, elapsed 35s. XML: state-machine 14 tests / 2 failures; new H2 suite 6 tests / 4 failures; 0 errors or skips. Failures were the old timestamp becoming null during reset/cleanup and initially null history being replaced by a new timestamp during zero-amount finalization. The two positive SUBSCRIBE cases already passed. This was intentional defect reproduction, not a final gate failure.

### Final Focused Verification

```powershell
$env:JAVA_HOME = 'C:/Program Files/Java/jdk-17'
.\gradlew.bat test --offline --no-daemon --console=plain --tests 'com.atstudio.atstudio.entity.BillingAgreementTest' --tests 'com.atstudio.atstudio.entity.BillingAgreementStateMachineTest' --tests 'com.atstudio.atstudio.service.BillingAgreementChargeTimestampIntegrationTest' --tests 'com.atstudio.atstudio.service.BillingAgreementPrepareToConfirmIntegrationTest' --tests 'com.atstudio.atstudio.service.BillingAgreementPrepareIdempotencyIntegrationTest' --tests 'com.atstudio.atstudio.service.BillingAgreementFailurePersistenceIntegrationTest' --tests 'com.atstudio.atstudio.service.PaymentCommandIndependentVerificationIntegrationTest' --tests 'com.atstudio.atstudio.service.SubscriptionUpgradeCommandIntegrationTest' --tests 'com.atstudio.atstudio.service.RecurringRenewalCommandIntegrationTest' --tests 'com.atstudio.atstudio.service.DownloadServiceTest'
```

Gradle exit 0; `BUILD SUCCESSFUL in 1m 2s`; `5 actionable tasks: 4 executed, 1 up-to-date`. The selected test task executed, not UP-TO-DATE. Test XML timestamps span `2026-09-08T09:05:28.633Z` to `2026-09-08T09:06:00.207Z` (18:05-18:06 KST).

Compiler note: the unchanged AdminPaymentSettlementImportIntegrationTest uses unchecked operations. The JVM CDS warning and Git CRLF-to-LF notices were informational. No failing compilation or whitespace checks remained.

### XML Parsing

```powershell
$rows = @(Get-ChildItem -LiteralPath build/test-results/test -Filter 'TEST-*.xml' | ForEach-Object {
    [xml]$report = Get-Content -LiteralPath $_.FullName -Raw
    [pscustomobject]@{
        File = $_.Name
        Tests = [int]$report.testsuite.tests
        Failures = [int]$report.testsuite.failures
        Errors = [int]$report.testsuite.errors
        Skipped = [int]$report.testsuite.skipped
        Seconds = [double]$report.testsuite.time
    }
})
$rows
$rows | Measure-Object Tests,Failures,Errors,Skipped -Sum
```

## Tests

| Suite | Tests | Failures | Errors | Skipped | Seconds |
| --- | ---: | ---: | ---: | ---: | ---: |
| BillingAgreementStateMachineTest | 14 | 0 | 0 | 0 | 0.651 |
| BillingAgreementTest | 6 | 0 | 0 | 0 | 0.006 |
| BillingAgreementChargeTimestampIntegrationTest | 6 | 0 | 0 | 0 | 19.52 |
| BillingAgreementFailurePersistenceIntegrationTest | 4 | 0 | 0 | 0 | 2.253 |
| BillingAgreementPrepareIdempotencyIntegrationTest | 20 | 0 | 0 | 0 | 2.613 |
| BillingAgreementPrepareToConfirmIntegrationTest | 3 | 0 | 0 | 0 | 1.585 |
| DownloadServiceTest | 10 | 0 | 0 | 0 | 0.926 |
| PaymentCommandIndependentVerificationIntegrationTest | 7 | 0 | 0 | 0 | 1.976 |
| RecurringRenewalCommandIntegrationTest | 8 | 0 | 0 | 0 | 1.956 |
| SubscriptionUpgradeCommandIntegrationTest | 5 | 0 | 0 | 0 | 1.337 |
| Total | 83 | 0 | 0 | 0 | - |

Nine JUnit invocations were added: six in the new integration class and three in the state-machine class. Existing tests were strengthened, not deleted. WI-001's 18 tests also passed unchanged within this run.

Full per-test results, exceptions and stdout/stderr are in the generated XML:
- `build/test-results/test/TEST-com.atstudio.atstudio.entity.BillingAgreementStateMachineTest.xml`
- `build/test-results/test/TEST-com.atstudio.atstudio.entity.BillingAgreementTest.xml`
- `build/test-results/test/TEST-com.atstudio.atstudio.service.BillingAgreementChargeTimestampIntegrationTest.xml`
- `build/test-results/test/TEST-com.atstudio.atstudio.service.BillingAgreementFailurePersistenceIntegrationTest.xml`
- `build/test-results/test/TEST-com.atstudio.atstudio.service.BillingAgreementPrepareIdempotencyIntegrationTest.xml`
- `build/test-results/test/TEST-com.atstudio.atstudio.service.BillingAgreementPrepareToConfirmIntegrationTest.xml`
- `build/test-results/test/TEST-com.atstudio.atstudio.service.DownloadServiceTest.xml`
- `build/test-results/test/TEST-com.atstudio.atstudio.service.PaymentCommandIndependentVerificationIntegrationTest.xml`
- `build/test-results/test/TEST-com.atstudio.atstudio.service.RecurringRenewalCommandIntegrationTest.xml`
- `build/test-results/test/TEST-com.atstudio.atstudio.service.SubscriptionUpgradeCommandIntegrationTest.xml`

New timestamp-suite SHA-256: `22F4530B62DF84462EE2068A57B6F8F3A110DD6777A6E5A9DEFAC86C40D169D5`.
State-machine-suite SHA-256: `63D62F65DC77243CBAD2BBDCF68A0A98BFB22DB339AD170057DE645A291A70C0`.
HTML Test Report: `build/reports/tests/test/index.html`.

Availability correction (2026-09-09, REQ-20260909-ATS-003): These generated,
Git-ignored output paths are not shipped with a source checkout or included in
the 205-artifact archive. Rerunning the recorded commands generates new evidence,
not this historical run's original reports. The dated execution claims are unchanged.

The tool truncated repeated Hibernate shutdown text in the console response; full-console retention is not claimed. XML remains the authoritative count and test-log evidence. Later normal test runs can replace these generated reports.

## Runtime And Isolated Build Handoff

Generated artifact, explicitly added to WI ownership by MA:

`C:/Users/jm991/AppData/Local/ATStudio/remote-development-20260908/closeout-build.gradle`

The script changes only root project `layout.buildDirectory`, before project evaluation, to:

`C:/Users/jm991/AppData/Local/ATStudio/remote-development-20260908/build-closeout`

It guards the canonical root project path so it is usable only for the named main worktree. It reads no environment bundle, secret, credential or local YAML and changes no dependencies. Product `build.gradle` is unchanged (`git diff --exit-code -- build.gradle` exited 0).

MA's aggregate build command, **not executed by this WI**:

```powershell
$env:JAVA_HOME = 'C:/Program Files/Java/jdk-17'
.\gradlew.bat -I 'C:/Users/jm991/AppData/Local/ATStudio/remote-development-20260908/closeout-build.gradle' build --offline --no-daemon --console=plain
```

The script was created/read back and handed off. MA subsequently reported starting the isolated aggregate build; its outcome is separate evidence and is not claimed here. Do not run a normal bootJar that would replace the JAR used by the public runtime.

The user-identified public Java PID `19376` was observed still running with start time `2026-09-08 08:03:01 KST`. The repository JAR at `build/libs/ATStudio-0.0.1-SNAPSHOT.jar` retained the same SHA-256 before and after focused testing:

`94A3324A6D1F4E2F414A84363AF5554D810FAA5627F6A7D4558790D71B4DFC60`

**Runtime remains the old JAR.** No bootJar, package replacement, restart or deployment was performed. Source/test compilation success is not runtime deployment proof.

## Risks / Rollback

- H2 and fake provider evidence only: no real Toss TEST/LIVE, MySQL transaction/locking, SMTP, browser or production acceptance.
- No full backend build or coverage gate was executed by SE. MA owns the isolated aggregate build and its result.
- This preserves history prospectively. It does not recover timestamps already lost or overwritten in historical rows; no real data was modified.
- Monetary finalization timing remains the existing local successful-finalization time, not a newly adopted provider timestamp policy.
- Code freeze: no product/test/init-script changes after the passing focused run and init-script handoff. Any subsequent need to change those inputs must be reported to MA before editing so aggregate verification can be rerun.
- Roll back only this WI's narrow entity/service changes, SQL COMMENT text and corresponding test hunks using a reviewed reverse patch. Preserve WI-001 and concurrent WI-002 work. Retire the external init script only with explicit approval and after MA's build is finished; no broad reset or delete.

## Follow-ups

WI-003 is complete. It releases its dependency on **WI-20260908-ATS-004**. MA should start WI-004 independent review after confirming both WI-002 frontend and WI-003 backend are complete, using create-wi-handoff-packet. WI-004 then releases WI-005 documentation closeout. No nested agent was launched and the overall REQ remains open.

## Related Documents

- [WI-003 Handoff](WI-20260908-ATS-003-handoff.md)
- [Approved REQ and Branched WI Chain](../user/REQ-20260908-ATS-001.md)
- [WI-003 Summary](../user/WI-20260908-ATS-003-summary.md)
- [Payment Integration Design](../../docs/design/payment-integration-design.md)
