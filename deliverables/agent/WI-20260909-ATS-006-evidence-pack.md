---
version: 1.0
last_updated: 2026-09-09
project: ATS
owner: qa
category: evidence-pack
status: stable
related_wi: WI-20260909-ATS-006
dependencies:
  - path: WI-20260909-ATS-006-handoff.md
    reason: Approved ownership and output contract
  - path: ../user/REQ-20260909-ATS-001.md
    reason: Approved bounded remediation and serialized verification
  - path: ../../docs/standards/development-standards.md
    reason: Coverage expectations and evidence boundaries
---

# Evidence Pack: WI-20260909-ATS-006

## Summary

MA's final backend build and unchanged JaCoCo gates passed on the Boot 4.0.8 working tree: 1,825 tests reported, 1,806 passed, zero failures/errors, and 19 explicitly accounted-for skips. QA independently reconciled all 200 JUnit XML files and the JaCoCo XML against MA's JSON snapshots; no heavy runner was started by QA. This completes WI006 evidence review only, not production acceptance or the parent REQ.

## Scope / DoD Check

- [x] Assigned skill-generated handoff read first; approved REQ and QA ownership confirmed.
- [x] Exact totals, skipped suites, coverage counters, unchanged thresholds and critical-class gates checked against current artifacts.
- [x] Earlier failures retained as separate historical checkpoints; no earlier evidence or raw artifact rewritten.
- [x] Tests executed by MA distinguished from read-only checks executed by QA and checks not run.
- [x] Only this evidence pack and `deliverables/user/WI-20260909-ATS-006-summary.md` created with `apply_patch`.
- [x] Product code, tests, policy files, retained data, secrets, media, logs and runtime left untouched by QA; no Git writes or subdelegation.
- [x] WI013/WI014 dependency returned to MA. WI007/WI008/WI009 remain outside this assignment and are not closed here.

## Reference Documents (Tier 0-2)

| Tier | Document | Applied purpose |
|---|---|---|
| Entry | `AGENTS.md` | Korean conversation, English reports, owned scope and two-set delivery |
| 0 | `docs/standards/core-principles.md` | Approved bounded execution, preservation and transparent evidence |
| 0 | `docs/standards/development-standards.md` | Java 17, service transactions, section 6.3 coverage expectations and H2 limits |
| 0 | `docs/standards/documentation-standards.md` | Metadata, traceable pointers and preservation of historical results |
| 0 | `docs/standards/glossary.md` | Canonical WI, Track, Subscription, License and download terminology |
| 1 | `docs/policies/security-policy.md` | No credential access, sensitive payload disclosure or external operational actions |
| 1 | `docs/policies/quality-gates.md` | Unchanged quality gates and bounded verification |
| 2 | `deliverables/user/REQ-20260909-ATS-001.md` | Approved remediation, serialized MA runners and downstream chain |
| 2 | `deliverables/agent/WI-20260909-ATS-006-handoff.md` | Evidence-only ownership and acceptance criteria |
| 2 | `deliverables/agent/WI-20260908-ATS-018-findings.md` | SEC/AUTH/PAY/DATA/STORAGE/DEP/OPS finding identifiers |
| 2 | `deliverables/agent/WI-20260909-ATS-001-evidence-pack.md`; `WI-20260909-ATS-002-evidence-pack.md`; `WI-20260909-ATS-003-evidence-pack.md`; `WI-20260909-ATS-005-evidence-pack.md` | Implementation/test mappings and historical checkpoint/fixture explanations |
| Standard | `docs/standards/evidence-pack-standard.md` | Reproduction, risk, rollback and result traceability |
| Skills | `.agents/skills/create-wi-evidence-pack/SKILL.md`; `.agents/skills/test-coverage/SKILL.md`; `.agents/skills/build-check/SKILL.md`; `.agents/skills/test/SKILL.md` | Evidence-pack format, test counts, coverage and build reporting; runner steps reserved to MA |

Injection rule source: `.claude/config/context-injection-rules.json`; assignee `qa`, task type `testing`, `agent_required_tiers: [0]`, with the additional security/quality policies explicitly supplied by the handoff. MA supplied the Tier 0 summary; QA checked linked live documents and relevant implementation evidence. `.claude/config/workspace.json` confirms project tag `ATS`. No subagent was invoked.

## Evidence Pointers

Repository root: `C:/Users/jm991/Desktop/project/ATStudio`. Read-only Git inspection confirmed branch `main`, HEAD `8161f0a`, with shared uncommitted changes. Results describe the patched working tree, not the unmodified commit or pinned running backend.

| Artifact | Role |
|---|---|
| `output/release-remediation-20260909/backend-full-final.log:3` | Final task states; compile/package tasks were up-to-date |
| `output/release-remediation-20260909/backend-full-final.log:1138` | Executed `jacocoTestReport`, `jacocoTestCoverageVerification`, `check`, `build` |
| `output/release-remediation-20260909/backend-full-final.log:1143` | `BUILD SUCCESSFUL in 2m 20s`; 10 actionable tasks, 3 executed and 7 up-to-date |
| `output/release-remediation-20260909/backend-full-final-results.json` | Final total and all 200 suite rows |
| `build/test-results/test/TEST-*.xml` | Independently parsed suite attributes and 1,825 actual testcase nodes |
| `output/release-remediation-20260909/backend-coverage-final.json` | Six bundle counters reconciled against XML |
| `build/reports/jacoco/test/jacocoTestReport.xml` | Direct report counters and all seven exact critical-class counters |
| `build.gradle:76`, `build.gradle:96`, `build.gradle:138` | Critical allowlist, thresholds and `check` dependency |
| `src/test/resources/application.yml:1` | SQL init disabled; disposable test schema uses Hibernate create-drop |

Test-suite start timestamps span `2026-09-08T19:45:17.942Z` to `2026-09-08T19:47:20.466Z`, equivalent to September 9, 04:45:17.942-04:47:20.466 KST. These are suite start timestamps, not an independent wall-clock build duration. The JaCoCo report was written at 04:47:25 KST, before the final log completed at 04:47:26 KST.

### Snapshot Identity

| File | SHA-256 at QA review |
|---|---|
| `output/release-remediation-20260909/backend-full-final.log` | `9CAE86406653CF78BABB158DEF42CB737FEFBE83C9171600A096B47BBACF400C` |
| `output/release-remediation-20260909/backend-full-final-results.json` | `AFD9898B6363AFA04C17320F83C72DCA603D93B72FF7B72542FF080BD5CE4797` |
| `output/release-remediation-20260909/backend-coverage-final.json` | `80E00CBFCDC3815765E333D811F4AD3CCD73012790C78C41F1BD5232C29A2542` |
| `build/reports/jacoco/test/jacocoTestReport.xml` | `3D36F2CF00459BAA65F94E9D859F53EEE2D9924E7CC1353335B99EA922B7DAB9` |
| `build.gradle` | `2B0CBDB0FC2413065E2CEC101501337A8FDE3A475EA02D1C452B8A432EF0CF95` |
| `src/test/java/com/atstudio/atstudio/config/AppConfigMultipartBoundaryTest.java` | `2C7C6ED3D70F1CCD74D40E925E4EC20D5A504D68704E73CF4DC0052935C7E610` |

The multipart source hash matches the corrected MIME-header fixture recorded by WI005. Shared `build/` artifacts are mutable; a later test run can replace them. The historical second-run JSON/log and focused JSON/log remain separate evidence.

MA's subsequent `output/release-remediation-20260909/backend-tested-source-manifest.json`, recorded at `2026-09-09T04:51:26.7349275+09:00`, lists 620 input files at HEAD `8161f0a00b088001a37962da719c3ace8fea44ad`. QA read-only SHA-256 comparison found all 620 present and unchanged. The current `build/libs/ATStudio-0.0.1-SNAPSHOT.jar` hash is `8117DF561E633706C6DA78BDE39BA3B5155982B9AA604BE1413EBB0FE404D7A0`, matching the earlier `output/release-remediation-20260909/built-artifact-dependencies.json` record. This is a post-run manifest and current artifact identity check, not a clean-build provenance claim; MA additionally confirmed no new backend changes. The separate frontend WI010 R1 counterexample and WI004 guard/test correction do not change this WI006 handback or establish WI007-WI009 acceptance.

## Commands & Outputs

### Executed By QA

- Read-only `Get-Content`, `rg`, directory inspection and SHA-256 reads, limited to governing documents and assigned evidence/source pointers.
- `git --no-optional-locks status --short --branch` and `git --no-optional-locks rev-parse --short HEAD`: confirmed shared working tree and `8161f0a`; no staging, commit, reset, checkout, push or other Git mutation.
- `git --no-optional-locks diff 8161f0a -- build.gradle docs/standards/development-standards.md docs/policies/quality-gates.md src/test/resources/application.yml`: the only displayed change was Boot `4.0.2` to `4.0.8` at `build.gradle:4`. No threshold, critical-class list, task dependency or test configuration change appeared.
- PowerShell `System.Xml.XmlDocument`, with `XmlResolver = $null`, parsed all JUnit XML and JaCoCo XML without external DTD resolution. `ConvertFrom-Json`, `Measure-Object` and `Compare-Object` reconciled counts and counters: zero suite-row differences and zero coverage-counter differences.
- Two exploratory `rg` calls used unsupported shell-style path globs and failed to open those patterns. Both were repeated successfully using `rg -g` against the containing directory. They are inspection-command corrections, not test failures.
- `apply_patch` created the two owned English reports. No Gradle, npm, compiler, test runner, application process or service was launched by QA.
- Post-write checks of both reports found all seven required metadata fields, valid Markdown link/dependency targets, zero trailing-whitespace lines and no UTF-8 replacement characters. All six reported coverage percentages matched recalculated XML values; final log/JSON/JaCoCo hashes were unchanged on re-read. Scoped `git diff --check` exited 0, but these new untracked reports required the separate content checks above. This is bounded report validation, not the repository-wide documentation validator reserved for the downstream chain.

### MA Execution And Reproduction Boundary

The supplied final log establishes the effective Gradle task chain `test -> jacocoTestReport -> jacocoTestCoverageVerification -> check -> build`. It does not contain the original shell invocation or every environment/CLI flag. Do not treat a reconstructed wrapper as captured evidence.

The following are minimal equivalent commands for a future MA-serialized rerun, **not commands executed by QA** and not a verbatim transcript of MA's wrapper:

```powershell
# MA only, after re-establishing the approved H2/fake/temp safe environment.
.\gradlew.bat test --tests "com.atstudio.atstudio.config.AppConfigMultipartBoundaryTest"
.\gradlew.bat build
```

No additional heavy tests are requested for WI006: the focused multipart check and full backend gate are complete. Never use these commands with inherited runtime credentials, real data roots, MySQL opt-ins, enabled Provider/mail operations or an unverified environment. The test schema may be created/dropped in disposable H2; no retained-database DDL is authorized.

## Tests

### Final Totals

| Measure | Final result |
|---|---:|
| JUnit XML suites | 200 |
| Reported tests / testcase nodes | 1,825 / 1,825 |
| Passed, excluding skips | 1,806 |
| Failures | 0 |
| Errors | 0 |
| Skipped | 19 |
| Suite rows differing from final JSON | 0 |

### Skip Inventory

Class names below map exactly to `build/test-results/test/TEST-com.atstudio.atstudio.service.<Class>.xml`, except the storage class, which uses `.service.storage.LocalStorageServiceTest.xml`.

| Class | Skips | Source-backed boundary |
|---|---:|---|
| `AdminPaymentSettlementMysqlConcurrencyIntegrationTest` | 3 | `:73`, environment opt-in `ATSTUDIO_SETTLEMENT_MYSQL_PROOF_ENABLED` |
| `AdminSubscriptionCorrectionMysqlConcurrencyIntegrationTest` | 2 | `:67`, `ATSTUDIO_SUBSCRIPTION_CORRECTION_MYSQL_PROOF_ENABLED` |
| `BillingAgreementPrepareMysqlConcurrencyIntegrationTest` | 3 | `:105`, `ATSTUDIO_MYSQL_PROOF_ENABLED` |
| `PaymentMysqlConcurrencyIntegrationTest` | 7 | `:115`, `ATSTUDIO_MYSQL_PROOF_ENABLED` |
| `PaymentMysqlSchemaValidationTest` | 1 | `:29,41`, explicit opt-in plus disposable-name/JDBC target guard |
| `UserRoleChangeMysqlConcurrencyIntegrationTest` | 2 | `:66`, `ATSTUDIO_ADMIN_ROLE_MYSQL_PROOF_ENABLED` |
| `LocalStorageServiceTest` | 1 | `rejectsDirectoriesAndSymbolicLinks()`, `:113`; XML reports `TestAbortedException: Symbolic links are unavailable in this environment` |
| Total | 19 | 18 guarded MySQL cases plus one environment-aborted storage case |

The MySQL XML entries have empty `<skipped/>` elements; their opt-in explanation is supported by source annotations and MA's supplied safe-environment statement, not an invented XML message. The storage suite reports 14 cases, 13 passed and one skipped. No symbolic-link defense PASS or actual MySQL concurrency/schema proof is claimed.

### Bounded Regression Mapping

Each class below is present in final JUnit XML. Counts are test cases, not separate end-to-end workflows; zero failures/errors apply to every row. These selected rows are part of the 1,825 total, not additional tests.

| Finding / gate | Final class | Passed | Skipped |
|---|---|---:|---:|
| SEC-01/03 | `security.JwtTokenPurposeTest` | 17 | 0 |
| SEC-01, AUTH-06 | `security.AuthTokenSecurityIntegrationTest` | 6 | 0 |
| SEC-02 | `common.exception.SensitiveValidationLogTest` | 4 | 0 |
| Security-chain compatibility | `controller.SecurityFilterChainTest` | 23 | 0 |
| SEC-05 | `service.PasswordResetConsumptionIntegrationTest` | 3 | 0 |
| PAY-01 | `service.PaymentReturningSubscriberIntegrationTest` | 12 | 0 |
| PAY-02 | `service.SubscriptionUpgradeCommandIntegrationTest` | 21 | 0 |
| PAY-02 / WI011 F1 follow-up | `service.SubscriptionMutationFenceIntegrationTest` | 10 | 0 |
| Payment recovery compatibility | `service.PaymentReconciliationRecoveryIntegrationTest` | 9 | 0 |
| DATA-01/02, STORAGE-04 | `service.storage.RetainedHistoryStorageIntegrationTest` | 22 | 0 |
| DATA-03 | `service.TrackMutationConcurrencyIntegrationTest` | 8 | 0 |
| Download lock contract | `service.DownloadConcurrencyContractTest` | 3 | 0 |
| STORAGE-04 | `service.storage.StorageMutationCoordinatorTest` | 11 | 0 |
| STORAGE-04 | `service.storage.StorageMutationJournalServiceTest` | 6 | 0 |
| Storage compatibility | `service.storage.LocalStorageServiceTest` | 13 | 1 |
| OPS-01 | `config.AppConfigMultipartBoundaryTest` | 10 | 0 |

Prefix every qualified class above with `com.atstudio.atstudio.` for the exact class/XML identity. Final execution now includes the captured-log and security-chain suites absent from WI001's first focused snapshot. This report adds that later evidence without changing the initial snapshot's truthful limitations. Regression execution is not a replacement for WI010/WI011/WI012 independent review.

### Coverage And Unchanged Gates

Counters below are direct children of the JaCoCo `report` element; summing package/class/sourcefile counters together would double-count them. Percentages are rounded to four decimal places.

| Counter | Covered | Missed | Total | Coverage | Existing Gradle bundle gate |
|---|---:|---:|---:|---:|---|
| LINE | 10,559 | 1,364 | 11,923 | 88.5599% | >=80%: PASS |
| METHOD | 1,873 | 298 | 2,171 | 86.2736% | >=80%: PASS |
| BRANCH | 3,741 | 1,287 | 5,028 | 74.4033% | >=70%: PASS |
| INSTRUCTION | 47,527 | 6,200 | 53,727 | 88.4602% | No instruction gate configured |
| COMPLEXITY | 3,221 | 1,484 | 4,705 | 68.4591% | No complexity gate configured |
| CLASS | 422 | 19 | 441 | 95.6916% | No bundle class gate configured |

Instruction coverage also exceeds the development standard's 80% statements/instructions expectation, but it is not an additional Gradle-enforced limit. JaCoCo METHOD is the Java function metric; INSTRUCTION is bytecode instruction coverage, not a source-statement count.

The exact existing `criticalSecurityClasses` list has seven entries. Each has 100% LINE and METHOD coverage, as required by `build.gradle:120`. Branch counters are shown separately because the class-level gate does not enforce 100% branches.

| Critical class | LINE covered/total | METHOD covered/total | BRANCH covered/total | Existing class gate |
|---|---:|---:|---:|---|
| `config.JwtConfig` | 10/10 | 2/2 | 5/6 | PASS |
| `security.AuthRateLimitFilter` | 95/95 | 17/17 | 36/40 | PASS |
| `security.CustomUserDetailsService` | 10/10 | 4/4 | 4/4 | PASS |
| `security.JwtAuthenticationFilter` | 22/22 | 2/2 | 10/10 | PASS |
| `security.JwtTokenProvider` | 58/58 | 14/14 | 13/14 | PASS |
| `service.auth.AuthService` | 50/50 | 7/7 | 14/14 | PASS |
| `service.payment.billing.BillingKeyCrypto` | 97/97 | 17/17 | 45/50 | PASS |

All class names use the same `com.atstudio.atstudio.` prefix. Eleven branch outcomes remain uncovered across four listed classes. The broader critical-path expectation in development standards is not proof that every security-related class or branch is covered. This WI certifies the unchanged configured gates and reports the gap; it neither relaxes thresholds nor claims universal security-code 100% coverage.

### Preserved Execution History

| Checkpoint | Result | Evidence / disposition |
|---|---|---|
| First patched full build | FAILED in 50s at `compileTestJava`; no tests/coverage result | `backend-full-first.log:12`: `InputBuffer is not a functional interface` at the original multipart fixture line 196. Compile/package work completed before test compilation failed. |
| Second patched full build | FAILED in 2m 38s; 200 suites, 1,825 total, 1,797 passed, 9 failed, 0 errors, 19 skipped | `backend-full-second.log:23`, `:1174`, `:1189`; `backend-full-second-results.json`. All nine failures were multipart fixture NPEs at line 195. No successful JaCoCo gate from this run. |
| Corrected multipart focused retry | SUCCESS in 18s; 10 tests passed, 0 failures/errors/skips | `backend-multipart-focused.log`; `backend-multipart-focused-results.json`. Separate snapshot; do not add these 10 to the full-run total. |
| Final full backend gate | SUCCESS in 2m 20s; 1,806 passed and 19 skipped; report and verification tasks executed | `backend-full-final.log`; final suite JSON and current JUnit/JaCoCo XML. Compilation and packaging were up-to-date, not a fresh clean build. |

All four log/JSON filename references in this table are under `output/release-remediation-20260909/`. WI005 records the two distinct fixture corrections: implement both `InputBuffer.doRead` and `available`, then initialize the real MIME `content-type` header instead of calling `setContentType(String)` on an uninitialized cache. Current test source at `src/test/java/com/atstudio/atstudio/config/AppConfigMultipartBoundaryTest.java:195` agrees. The second-run raw XML has since been replaced by final XML; the preserved second-run log/JSON and WI005's contemporaneous explanation support its history. QA does not claim to have re-inspected an archived failing XML that is no longer present.

The final log retains the JVM class-data-sharing warning. Earlier compile/focused logs also retain deprecated-API notes; the second full compile includes unchecked-operation notes. These did not fail the final gate; no warning-free clean compilation is claimed. Fixture failures are not original-product-defect red runs. Passing in-test legacy/unlimited controls are not a full checkout mutation/red run.

## Risks / Rollback

- MA explicitly supplied isolated H2, synthetic keys, fake Provider/mocked mail, temp storage and disabled MySQL opt-ins. Read-only XML `system-out` parsing found H2 in-memory connection evidence and Boot 4.0.8 banners in 49 suites, and no MySQL JDBC URL text. Along with the skipped guards, this corroborates the supplied environment; absence of URL text is not an independent network/process audit.
- H2 create/drop statements in the final test log belong to disposable test schemas under the supplied setup. This is not a claim of zero DDL anywhere; no real or retained database access/DDL, payment/refund, Provider call, mail delivery, real media operation, secret read or runtime restart was performed by QA.
- Not run by QA: every Gradle/npm/test command, real MySQL concurrency/schema validation, Windows symbolic-link acceptance, real SMTP/OAuth/payment-provider acceptance, deployed proxy/ACL/load testing, browser/mobile end-to-end flows, clean-checkout build, dependency vulnerability rescan and original-source mutation testing. The MA-run build is artifact-reviewed evidence, not an independently rerun build.
- Product policies and preserved historical data were not re-audited exhaustively by this evidence-only role. No current runtime health or deployment acceptance is inferred from test results. Old typeless sessions and pending old-format payment commands retain the implementation/review packs' deployment cautions; this WI authorizes no session, ledger or runtime remediation.
- All shared implementation changes, SR-93 and pre-existing untracked artifacts were left in place. Rollback of WI006 concerns only the two report files: use a scoped correction patch, or obtain explicit approval before removing them. Never reset the shared working tree or remove raw evidence, tests or product changes as a documentation rollback.

## Follow-ups / Chain

- **Ready test list to MA: none outstanding for WI006.** Preserve the final artifacts; any later backend source/test/gate change requires MA to assess and serialize the appropriate rerun before reusing this snapshot.
- Return WI006 PASS-with-explicit-skips evidence to WI013 for current documentation/result-table synthesis, then WI014 for integration and preservation review. Other dependencies and independent reviewer acceptance remain MA-owned.
- WI007, WI008 and WI009 are not assigned or accepted in this report. No frontend log, coverage result, typecheck, lint, formatting or build is closed here, even if those files already exist.
- Parent REQ remains open until MA completes its remaining chain. No subdelegation, workboard mutation, deployment, commit or push was performed.

## Related Documents

- [Assigned WI006 handoff](WI-20260909-ATS-006-handoff.md)
- [Approved parent REQ](../user/REQ-20260909-ATS-001.md)
- [WI006 user summary](../user/WI-20260909-ATS-006-summary.md)
- [Initial finding register](WI-20260908-ATS-018-findings.md)
- [Multipart fixture history](WI-20260909-ATS-005-evidence-pack.md)
