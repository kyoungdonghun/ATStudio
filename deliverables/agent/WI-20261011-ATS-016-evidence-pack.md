---
version: 1.0
last_updated: 2026-10-11
project: ATS
owner: SE
category: evidence-pack
status: confirmed
dependencies:
  - path: WI-20261011-ATS-016-handoff.md
    reason: Approved scope and output contract
  - path: ../user/REQ-20261011-ATS-002.md
    reason: Local-only preparation approval
---

# Evidence Pack: WI-20261011-ATS-016

> Purpose: Bind the local background-task switch implementation to focused evidence for PG/RE review.

## Summary

Implementation complete; 68 focused tests PASS, no blocker. Exact public contract:
`APP_BACKGROUND_TASKS_ENABLED` maps to `app.background-tasks.enabled`, default
`true`. Explicit `false` suppresses Spring scheduled registration, storage ready
recovery, and audio-worker bean/private-executor creation even when its legacy
flag is true. No runtime deployment or application startup is claimed.

## Scope / DoD Check

- [x] Inventory scheduled, startup, private-executor and transaction-event entries.
- [x] Preserve default behavior and manual service availability.
- [x] Reject malformed master configuration and accidental scheduling in off mode.
- [x] Preserve strict integrity and mandatory billing-key validation.
- [x] Verify explicit true/false/missing/invalid values and public YAML mapping.
- [x] Isolate build output, preserve historical JAR and existing dirty edits.
- [x] Produce both deliverable sets and identify immediate successor WIs.

## Reference Documents (Tier 0-2)

| Tier | Context | Use |
|---|---|---|
| 0 | core-principles.md (STD-001), documentation-standards.md (STD-004), development-standards.md (STD-002), glossary.md (STD-005) | Fully injected by MA, in supplied order |
| 1 | SE persona; docs/policies/security-policy.md | Fully injected; scoped implementation and secret-safe boundaries |
| 1 | docs/policies/quality-gates.md | Read locally; bounded verification and independent review |
| 2 | docs/index.md, latest Current Decision Entry | Read latest native schema PASS and remaining production HOLD |
| 2 | deploy/single-vps/CONFIGURATION.md; runbooks/production-readiness.md | Read relevant worker, bootstrap, key-validation and startup sections |
| 2 | REQ-20261011-ATS-002; WI016 handoff; WI015 Evidence Pack | Read actual approval, ownership, historical evidence boundaries |
| 2 | WI-20261010-ATS-026-artifact.json | Read manifest header/boundary; directly rehashed retained JAR |
| Skill | .agents/skills/test/SKILL.md; create-wi-evidence-pack/SKILL.md | Read and followed; actual handoff exists |
| Registry | .claude/config/workspace.json | Confirmed ATS tag |

Injection order: supplied Tier0 -> SE -> security -> handoff/snapshot.
Handoff rule-source pointer: `.claude/config/context-injection-rules.json`;
assignee SE, implementation/testing, no sub-delegation. A lightweight memory
lookup supplied navigation only; current repository evidence governs all claims.

## Inventory and Design

| Entry | Treatment |
|---|---|
| SubscriptionScheduler:58,63,84 | Three cron methods retained; conditional global scheduling registration |
| PaymentReconciliationService:81 | Reconciliation cron registration suppressed; service retained for AdminPaymentReadService and manual operations |
| WithdrawalBillingCleanupCoordinator:31 | Retry cron registration suppressed; manual and transaction-after-commit semantics retained |
| StorageMutationRecoveryService:39,46 | Ready-event method guarded; fixed-delay registration suppressed; recoverBatch remains callable |
| TrackAudioProcessingWorker:29,40,58,70 | Master condition AND existing legacy flag; off creates neither bean nor private executor; enabled ready/stop behavior unchanged |
| TestUserBootstrapRunner; AcceptanceSubscriptionPlanBootstrapRunner | Existing separate test-user/acceptance controls retained, both public defaults false |
| AcceptanceStartupGuard; StorageIntegrityStartupGuard | Unchanged; mandatory encryption validation and strict integrity still reject unsafe startup |
| JwtConfig; JwtTokenProvider PostConstruct | Unchanged validation/key initialization |
| LocalStorageService PostConstruct | Unchanged root initialization/validation; this switch does NOT promise filesystem-read-only startup |
| PaymentReceiptEvidenceService; WithdrawalBillingCleanupCoordinator transactional listeners | Request/transaction-triggered work preserved; this switch is NOT an egress or read-only-service fence |

Inventory search covered `@Scheduled`, `@EnableScheduling`, `@EventListener`,
`@PostConstruct`, `@PreDestroy`, ApplicationRunner/CommandLineRunner,
ApplicationReadyEvent, Executors/new Thread/ExecutorService/TaskScheduler,
`@Async`, TransactionalEventListener, InitializingBean, SmartLifecycle,
ApplicationListener, CompletableFuture and Timer. No other autonomous production
entry was found in this scoped source search. Future new private executors or
startup hooks still require explicit review; the scheduler guard is not a generic
sandbox for arbitrary future code.

Reuse-first: retained existing audio ConditionalOnProperty and storage ready
method. New condition accepts case-insensitive true/false only, with missing
default true; empty/invalid values fail startup. New static BeanFactoryPostProcessor
rejects typed scheduled-annotation processors while off, before registration.
The nested conditional scheduling configuration replaces the root annotation.
It does not disable domain-service beans required by controllers.

## Evidence Pointers

Changed production files (repository-relative):

- `src/main/java/com/atstudio/atstudio/AtStudioApplication.java:5`: removed unconditional scheduling opt-in.
- `src/main/java/com/atstudio/atstudio/config/BackgroundTasksConfiguration.java:12`: conditional registration and fail-closed scheduler guard.
- `src/main/java/com/atstudio/atstudio/config/BackgroundTasksEnabledCondition.java:16`: explicit master parsing.
- `src/main/java/com/atstudio/atstudio/service/audio/TrackAudioProcessingWorker.java:29`: conjunctive master condition; no production callers require this worker bean.
- `src/main/java/com/atstudio/atstudio/service/storage/StorageMutationRecoveryService.java:23`: ready-entry gate, manual recovery unchanged.
- `src/main/resources/application.yml:84`: only this WI's two-line mapping addition; existing Hikari, mail-timeout, multipart and admission edits preserved.

Focused tests: `src/test/java/com/atstudio/atstudio/config/BackgroundTasksConfigurationTest.java:33`.
Twenty cases use isolated ApplicationContextRunner configurations and mock domain
dependencies, not SpringBootTest or the full application. Real inherited system
properties/environment sources are removed from these new contexts. Enabled
audio-ready cases run the private worker only against mocks and close the context;
no audio file, FFmpeg, provider or DB action occurs. Mock TaskScheduler records
all six registrations without executing cron/fixed-delay work.

## Commands and Test Evidence

Environment: Windows/PowerShell, Java17 toolchain, Gradle9.3.0, Boot4.0.8,
2026-10-11 around 11:01 KST. Evidence owner: SE. Branch `main`, HEAD
`58b1011dad796017e47dbcc82a4225ca449bcdf6`, existing dirty tree retained.

WI-local init script `build/WI016-background-tests.init.gradle` contains:

```groovy
allprojects {
    layout.buildDirectory = file(providers.gradleProperty('wi016BuildDir').getOrElse('build/WI016-background-tests'))
}
```

Final executed command (use a NEW output path on future reproduction):

```powershell
.\gradlew.bat --offline --no-daemon -I build/WI016-background-tests.init.gradle -Pwi016BuildDir=build/WI016-background-tests-final-v2 test --tests com.atstudio.atstudio.config.BackgroundTasksConfigurationTest --tests com.atstudio.atstudio.config.AudioProcessingConfigTest --tests com.atstudio.atstudio.config.AcceptanceStartupGuardTest --tests com.atstudio.atstudio.config.StorageIntegrityStartupGuardTest --tests com.atstudio.atstudio.service.storage.StorageMutationRecoveryServiceTest --tests com.atstudio.atstudio.service.storage.StorageMutationRecoveryVerificationTest --tests com.atstudio.atstudio.service.SubscriptionSchedulerTest --tests com.atstudio.atstudio.service.PaymentReconciliationServiceTest --tests com.atstudio.atstudio.service.WithdrawalBillingCleanupCoordinatorTest
```

Result: exit0, BUILD SUCCESSFUL in54s, five tasks executed. XML totals:
68 tests, 0 failures, 0 errors, 0 skipped. Nine suites:

| Suite | Tests |
|---|---:|
| BackgroundTasksConfigurationTest | 20 |
| AudioProcessingConfigTest | 2 |
| AcceptanceStartupGuardTest | 14 |
| StorageIntegrityStartupGuardTest | 4 |
| StorageMutationRecoveryServiceTest | 4 |
| StorageMutationRecoveryVerificationTest | 1 |
| SubscriptionSchedulerTest | 6 |
| PaymentReconciliationServiceTest | 15 |
| WithdrawalBillingCleanupCoordinatorTest | 2 |

Reports: `build/WI016-background-tests-final-v2/reports/tests/test/index.html`
and `build/WI016-background-tests-final-v2/test-results/test/TEST-*.xml`.
The historical independent-verification-named test above was rerun by SE;
this is NOT the new WI019 independent review.

Development sequence retained: initial compile RED (new configuration not yet
implemented); initial green63/63 in `build/WI016-background-tests`; expanded
run67/68 in `build/WI016-background-tests-final` failed only because the new
test expected a dotted property name while the existing guard reports the
environment-variable name. Corrected that assertion only, then final68/68 above.
Earlier output trees are preserved. Compiler deprecation/unchecked warnings in
existing tests and JVM class-sharing warning remain; no dependency upgrade.
No clean, bootJar, full suite, coverage threshold or production sizing claim.
Scoped `git diff --check` passed; YAML emitted only a CRLF-normalization notice.

## Minimal Review Hash Set

SHA-256 below binds these exact dirty-worktree bytes, not a complete artifact
manifest. Prefix for Java paths: `src/main/java/com/atstudio/atstudio/`.

| Path | SHA-256 |
|---|---|
| AtStudioApplication.java | 8b9e8bba491dab5d8ae999c37c6ed93c7dd2c7740470b06ac8c65ba8ac018ae8 |
| config/BackgroundTasksConfiguration.java | bfd71bf159c7d3fc91d1f6fbfda72d01120e73a8bdeb855958bf65f7c83993b2 |
| config/BackgroundTasksEnabledCondition.java | 0562767a065abefe6496b9a9065e5f7cf0188875ce7d93541c6b8eeb4f50259b |
| service/audio/TrackAudioProcessingWorker.java | dfa81ed8bfed5338de9be1976f9f16d2fdce97164977591bf8bbc3558c8fe5b9 |
| service/storage/StorageMutationRecoveryService.java | b72d7d07840b09473c9c7e3d53925bf04b861d6e87be3a8e5fc6fe64af1bb575 |
| src/main/resources/application.yml | c3bd0e1571ffbe9e35ec5b77e6197fb91ca9baa602fb952609de4c07efd4bf55 |
| src/test/java/com/atstudio/atstudio/config/BackgroundTasksConfigurationTest.java | a3321d53afe3771cf257db3ab238b0ad7c1663c0b525c2258704f0cd75939cbf |
| build/WI016-background-tests.init.gradle | dd2fbb0601415d09d68c09506fecedd1822c34659bf6418f7b81f975b927a37e |

Retained `build/libs/ATStudio-0.0.1-SNAPSHOT.jar` SHA-256 checked before and
after focused builds: `517b745479792a7eae6ce75357c7d1299ab644b50d5bc5b331fea384ccb10e0a`.
No new JAR was created. Old class/XML locations were not targeted by the isolated
builds; this WI did not exhaustively rehash all historical receipts.

## Risks / Rollback / Follow-ups

Default true preserves production behavior; the first-start candidate must
explicitly set false. Wrong/missing candidate configuration remains an operator
risk. Off does not disable bootstrap flags, request-triggered operations,
transaction after-commit work, storage-root creation or safety validation.
Private-start egress fencing and ingress/access controls belong to WI017 and
target verification, not this property.

Actual application startup, DB/remote/secret/provider/mail/FFmpeg operations,
Linux scheduling/egress/stop behavior and production capacity: NOT RUN.
Independent PG/RE review and candidate packaging remain pending. No install,
purchase, commit, push, deletion or private configuration read occurred.

Proposed local rollback only: reverse this WI's scheduling opt-in relocation,
worker condition, recovery ready gate and two YAML lines, then separately review
removal of the new classes/test. Do not revert the whole dirty YAML or unrelated
changes. No rollback operation or deletion has been executed.

WI chain: MA can start WI019 immediately against the hash set above. WI018
depends on both WI016 and WI017. No sub-delegation performed. REQ002 stays open;
WI020 packaging requires the independent gates, then WI021 integration.

## Related Documents

- [WI016 handoff](WI-20261011-ATS-016-handoff.md): Assigned ownership and bounds.
- [REQ002](../user/REQ-20261011-ATS-002.md): Approved local-only work and WI chain.
- [User summary](../user/WI-20261011-ATS-016-summary.md): Compact outcome.
- [Current decision entry](../../docs/index.md#current-decision-entry): Existing target evidence and production limits.
