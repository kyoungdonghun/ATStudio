---
version: 1.0
last_updated: 2026-10-11
project: ATS
owner: RE
category: evidence-pack
status: confirmed
dependencies:
  - path: WI-20261011-ATS-019-handoff.md
    reason: Approved independent verification scope
  - path: WI-20261011-ATS-016-evidence-pack.md
    reason: Candidate pins and author baseline to verify independently
  - path: ../user/REQ-20261011-ATS-002.md
    reason: Local-only preparation approval and WI chain
---

# Evidence Pack: WI-20261011-ATS-019

> Purpose: Independently verify the exact WI016 background-task switch candidate without application startup or external dependencies.

## Summary

**Bounded local verification PASS: 87 tests, 0 failures, 0 errors, 0 skipped.**
RE independently reran the nine author-selected suites (68 tests) and added
19 synthetic boundary tests. No source defect or scope blocker was found.
All seven supplied candidate source/test pins match. All 654 captured public
source/helper/retained-artifact entries remained unchanged across execution.
This is not deployment, Linux isolation, capacity, or full-application approval.

## Scope / DoD Check

- [x] Confirm approved REQ002, existing WI019 handoff, and fully injected Tier0.
- [x] Read actual selected test sources before execution; no full application or DB test context.
- [x] Independently verify false, true/default, invalid values, ready hooks, scheduler registration, and private worker lifecycle.
- [x] Preserve manual service access and strict integrity/mandatory billing-key guards.
- [x] Reconcile nine-suite baseline counts; no existing test deletion or modification.
- [x] Preserve old JAR, WI026 manifest and nine WI016 final XML receipts by before/after hashes.
- [x] Record exact command, full output, XML counts/hashes, and source identity.
- [x] Create both deliverable sets; identify WI020 successor without subdelegating.

## Reference Documents (Tier 0-2)

| Tier | Document | Use |
|---|---|---|
| 0 | core-principles.md (STD-001) | Fully injected; approval boundary |
| 0 | documentation-standards.md (STD-004) | Fully injected; evidence format |
| 0 | development-standards.md (STD-002) | Fully injected; independent verification |
| 0 | glossary.md (STD-005) | Fully injected; canonical terms |
| 1 | RE persona, quality-gates.md, security-policy.md | Fully injected; mock-only execution and evidence limits |
| 2 | docs/index.md, latest Current Decision Entry | Read native read-only schema PASS separately from production HOLD |
| 2 | deploy/single-vps/CONFIGURATION.md; runbooks/production-readiness.md | Read relevant worker, bootstrap, strict guard and egress sections; older dated judgments do not supersede latest index |
| 2 | REQ002; WI019 handoff; WI016 completion | Read approval, ownership, candidate contract and hashes |
| 2 | WI015 Evidence Pack | Read current native evidence attribution; no native rerun |
| 2 | WI-20261010-ATS-026-artifact.json | Read public header/boundaries and rehash manifest/JAR |
| Skill | test/SKILL.md; create-wi-evidence-pack/SKILL.md | Read both; handoff precondition satisfied |
| Registry | .claude/config/workspace.json | Confirmed ATS tag |

Injection order: Tier0 -> RE -> quality/security -> handoff -> snapshot.
Rule-source pointer: `.claude/config/context-injection-rules.json`; assignee RE,
testing/review, mandatory Tier0. No subdelegation. Lightweight memory lookup was
navigation only; all present-state conclusions use current source and receipts.

## Evidence Pointers

Production source was inspected, not edited:

- `src/main/java/com/atstudio/atstudio/AtStudioApplication.java:5`: no unconditional scheduling opt-in.
- `config/BackgroundTasksConfiguration.java:14`: off-mode typed scheduler-processor rejection before registration; conditional enable at line26.
- `config/BackgroundTasksEnabledCondition.java:16`: missing defaults true; only case-insensitive true/false accepted.
- `service/audio/TrackAudioProcessingWorker.java:29`: master AND legacy condition precedes bean/private executor creation; readiness at58, stop at70.
- `service/storage/StorageMutationRecoveryService.java:38`: readiness gate; manual recoverBatch at47 remains callable.
- `config/AcceptanceStartupGuard.java:68`: mandatory BillingKeyCrypto validation remains before optional-mode early return.
- `config/StorageIntegrityStartupGuard.java:31`: production strict-audit requirement remains active.
- `src/main/resources/application.yml:19,84`: DDL default remains validate; public master mapping defaults true.

Java paths above without a root share `src/main/java/com/atstudio/atstudio/`.
Source inventory found six @Scheduled methods, one conditional @EnableScheduling,
the audio private executor and storage ready hook. Separate bootstrap flags,
JWT initialization, local storage initialization and transaction listeners remain.
No claim of a generic future-code sandbox is made.

Only WI019-created files:

- `build/WI019-tests.init.gradle`: isolated build directory, extra synthetic test source directory, test-child environment limited to SystemRoot/WINDIR/TEMP/TMP/PATH.
- `build/WI019-verification-src/com/atstudio/atstudio/config/WI019BackgroundBoundaryTest.java`: 19 RE-authored cases; no app-source/existing-test changes.
- `build/WI019-run.ps1`: fresh-output guards, public-input hashing, exact command/full log/XML receipts.
- `build/WI019-tests/`, `build/WI019-receipts/`: generated local results, not a deployable release.
- This Evidence Pack and the linked user summary.

## Commands and Outputs

Environment: Windows / PowerShell7.6.5, Java17.0.12+8-LTS-286,
Gradle9.3.0, Spring Boot4.0.8. RE execution: **2026-10-11
11:06:11.7922294 through11:07:03.9455598 KST**. Gradle reported51s,
five tasks executed; summed XML suite time7.048s.
Branch `main`, HEAD `58b1011dad796017e47dbcc82a4225ca449bcdf6`, dirty tree retained.

Executed wrapper:

```powershell
.\build\WI019-run.ps1
```

Exact Gradle command generated and executed by that wrapper:

```powershell
.\gradlew.bat --offline --no-daemon --console=plain -I build/WI019-tests.init.gradle test --tests com.atstudio.atstudio.config.BackgroundTasksConfigurationTest --tests com.atstudio.atstudio.config.AudioProcessingConfigTest --tests com.atstudio.atstudio.config.AcceptanceStartupGuardTest --tests com.atstudio.atstudio.config.StorageIntegrityStartupGuardTest --tests com.atstudio.atstudio.service.storage.StorageMutationRecoveryServiceTest --tests com.atstudio.atstudio.service.storage.StorageMutationRecoveryVerificationTest --tests com.atstudio.atstudio.service.SubscriptionSchedulerTest --tests com.atstudio.atstudio.service.PaymentReconciliationServiceTest --tests com.atstudio.atstudio.service.WithdrawalBillingCleanupCoordinatorTest --tests com.atstudio.atstudio.config.WI019BackgroundBoundaryTest
```

Exit0, BUILD SUCCESSFUL. Gradle used a single-use daemon despite --no-daemon
to honor JVM settings, with automatic termination; no reusable daemon or app
was requested. Offline cache sufficed; no installation or dependency change.
Compilation includes the existing test source set, but only named suites run.
Existing deprecation/unchecked compiler warnings and JVM class-sharing warning
remain; no new WI019 compile/test failure occurred.

Full receipts, not summary-only:

- [Exact command](../../build/WI019-receipts/command.txt)
- [Full stdout/stderr](../../build/WI019-receipts/gradle-full.log)
- [Machine-readable results and each XML SHA-256](../../build/WI019-receipts/result.json)
- [Before snapshot](../../build/WI019-receipts/before.json) and [after snapshot](../../build/WI019-receipts/after.json)
- [Initial dirty-tree receipt](../../build/WI019-receipts/git-status-before.txt)
- [JUnit HTML report](../../build/WI019-tests/reports/tests/test/index.html)
- XML: `build/WI019-tests/test-results/test/TEST-*.xml` (ten files).

Other checks: `git status --short --branch`, `git rev-parse HEAD`, scoped
`git diff --numstat` for eight existing suites and both mandatory guards (empty),
`git diff` for changed production files/YAML, source-entry `rg` inventory,
`Get-FileHash`, `java -version`, and structured XML parsing.

Final scoped checks: required metadata and whitespace PASS for both new docs;
all 15 Markdown file-link targets exist (anchor semantics not validated).
All 68 original XML testcase class/name identities match, not merely counts.
All 654 snapshot entries still match after deliverable creation. Receipt:
`build/WI019-receipts/final-checks.json`. Scoped `git diff --check` exit0;
only the existing YAML CRLF normalization notice remains. Direct document
checks cover the new untracked files that Git diff alone does not inspect.

## Test Evidence

| Suite | WI016 baseline | Independent WI019 | Fail / error / skip |
|---|---:|---:|---|
| BackgroundTasksConfigurationTest | 20 | 20 | 0 / 0 / 0 |
| AudioProcessingConfigTest | 2 | 2 | 0 / 0 / 0 |
| AcceptanceStartupGuardTest | 14 | 14 | 0 / 0 / 0 |
| StorageIntegrityStartupGuardTest | 4 | 4 | 0 / 0 / 0 |
| StorageMutationRecoveryServiceTest | 4 | 4 | 0 / 0 / 0 |
| StorageMutationRecoveryVerificationTest | 1 | 1 | 0 / 0 / 0 |
| SubscriptionSchedulerTest | 6 | 6 | 0 / 0 / 0 |
| PaymentReconciliationServiceTest | 15 | 15 | 0 / 0 / 0 |
| WithdrawalBillingCleanupCoordinatorTest | 2 | 2 | 0 / 0 / 0 |
| WI019BackgroundBoundaryTest | N/A | 19 | 0 / 0 / 0 |
| Total | 68 | 87 | 0 / 0 / 0 |

Baseline counts were independently parsed from the nine preserved
`build/WI016-background-tests-final-v2/test-results/test/TEST-*.xml` files.
The historically named StorageMutationRecoveryVerificationTest is one reused
test, not nineteen new WI019 tests. No assertions-count claim.

| Contract | Evidence |
|---|---|
| Master false, legacy true/false/missing | Original three off-context cases: no scheduled processor or audio bean; no ready-event dependency interactions |
| Default/true behavior | Six registrations, ready storage claim, audio recovery and mock claimNext; legacy audio false still wins |
| YAML/environment mapping | Synthetic APP_BACKGROUND_TASKS_ENABLED false/true/missing; bootstrap defaults false unchanged |
| Invalid configuration | Five original context-failure cases plus eight RE parser boundaries; empty, unresolved, nonboolean and padded values reject |
| Case handling | Four RE mixed-case literal cases; one independent missing-default case |
| Accidental scheduling | Additional @EnableScheduling and typed custom scheduler processor both fail closed while off |
| Private executor lifecycle | RE test line57 observes mock-only ready work then shutdown and termination after context close |
| Manual paths | Original storage/renewal calls remain callable off; RE line86 explicitly invokes cleanup handler off |
| Mandatory guards | Original off integrity/key cases plus RE line104 across prod/production/prod-eu/production-eu reject weak guard settings and missing key config |
| Neighbor behavior | Recovery retain/retry cases, reconciliation caps/privacy/finalization mocks, scheduler dates and cleanup annotation/delegation tests |

RE checklist: 65 direct unit-style cases, 19 isolated Spring component cases,
3 static source/YAML checks, 0 E2E. The selective mix is intentionally not the
70/20/10 whole-product pyramid; E2E is excluded by the handoff. REST contract
and 401/403 tests are N/A for this scheduling-only change. No real transactional
rollback was exercised: repository/provider/storage effects are mocked. The
cleanup test confirms direct invocation and the existing annotation test
confirms AFTER_COMMIT metadata, not actual transaction delivery.

Coverage report/threshold tasks: **NOT RUN**. Gradle's configured JaCoCo test
agent may produce execution data, but it is not measured coverage evidence.
Full suite, actual app startup, DB, retained data, Provider/mail/OAuth, FFmpeg,
Linux scheduling/egress/stop enforcement and target acceptance: **NOT RUN**.
Performance, supported-user counts and production capacity: **NOT MEASURED**.

## Exact Identity

SHA-256 pins match the supplied WI016 completion and before/after snapshots:

| Path | SHA-256 |
|---|---|
| AtStudioApplication.java | 8b9e8bba491dab5d8ae999c37c6ed93c7dd2c7740470b06ac8c65ba8ac018ae8 |
| config/BackgroundTasksConfiguration.java | bfd71bf159c7d3fc91d1f6fbfda72d01120e73a8bdeb855958bf65f7c83993b2 |
| config/BackgroundTasksEnabledCondition.java | 0562767a065abefe6496b9a9065e5f7cf0188875ce7d93541c6b8eeb4f50259b |
| service/audio/TrackAudioProcessingWorker.java | dfa81ed8bfed5338de9be1976f9f16d2fdce97164977591bf8bbc3558c8fe5b9 |
| service/storage/StorageMutationRecoveryService.java | b72d7d07840b09473c9c7e3d53925bf04b861d6e87be3a8e5fc6fe64af1bb575 |
| src/main/resources/application.yml | c3bd0e1571ffbe9e35ec5b77e6197fb91ca9baa602fb952609de4c07efd4bf55 |
| src/test/java/com/atstudio/atstudio/config/BackgroundTasksConfigurationTest.java | a3321d53afe3771cf257db3ab238b0ad7c1663c0b525c2258704f0cd75939cbf |

First five paths share `src/main/java/com/atstudio/atstudio/`.
Snapshot scope is public tracked src/build inputs plus untracked nonignored
src files, three WI019 helpers, retained JAR/WI026 manifest and nine prior XMLs.
It does not attest every file elsewhere in the worktree or historical outputs.

| Evidence item | SHA-256 |
|---|---|
| Retained build/libs/ATStudio-0.0.1-SNAPSHOT.jar | 517b745479792a7eae6ce75357c7d1299ab644b50d5bc5b331fea384ccb10e0a |
| before.json = after.json | 986e1c755306d68ee05cdb7bbea9c1c149cabeb9cc271b6532c327388e972484 |
| result.json | 937d32b7323c6ba09ddf50080a3e0b7e6a0412a9ccc0cafd68907a03ebb8b3b2 |
| gradle-full.log | 6b0067da9e45024a2d0a576eb90cfdf9416fba4a29a3d2b5916cf93289c29f76 |
| WI019-tests.init.gradle | 94220103b9f04ab347ddfdb151a6632ea41f7aa991e8cd14aba739a2eed6fe53 |
| WI019-run.ps1 | b55b54bffb1e1973320b144e561595e15b2ef20d5dfff00c3e99b7cdb225e10b |
| WI019BackgroundBoundaryTest.java | 4da40f64b5eaeb25333f63bb7aa8b7e5b4960cf21f261365eaa21d546cd70d30 |

No clean/build/bootJar/package task, JAR execution, private configuration read,
secret use, remote service operation, install, Git write or deletion occurred.

## Risks / Rollback / Follow-ups

- No source defect found in this bounded review. Default true is deliberate;
  first-start configuration must explicitly select false and match the packaged source.
- Off is not a read-only or egress boundary: manual/transaction paths, separate
  bootstrap controls, local root initialization and mandatory checks remain.
- Existing deployment prose predating WI016 does not establish the new candidate
  contract. WI017/021 own candidate configuration and current-entry integration;
  this RE did not edit deployment files or assert their completion.
- Reproduction requires a fresh isolated output/receipt path: preserve these
  receipts and create separately reviewed WI-local helper copies with new paths.
  The saved wrapper deliberately refuses rerunning over existing outputs.
- Local rollback needs no application-source reversal: WI019 changed only its
  helpers/receipts and two deliverables. Keep evidence; any later removal requires
  explicit approval. Do not revert dirty YAML/source or remove historical outputs.
- WI019 is complete and unblocks its side of **WI020**. MA must also confirm
  WI018 before packaging and bind that package to the same source hashes.
  No subdelegation or packaging by RE; REQ002 remains open through WI020/021.

## Related Documents

- [WI019 handoff](WI-20261011-ATS-019-handoff.md): Scope and ownership.
- [WI016 completion](WI-20261011-ATS-016-evidence-pack.md): Candidate baseline.
- [REQ002](../user/REQ-20261011-ATS-002.md): Approval and successor chain.
- [User summary](../user/WI-20261011-ATS-019-summary.md): Compact result.
- [Current decision entry](../../docs/index.md#current-decision-entry): Separate target evidence and production limits.
