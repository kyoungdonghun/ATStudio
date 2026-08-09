# WI-20260808-ATS-023 Summary

## Status

**PASS.** After the WI-20260809-ATS-002 test-slice repair, the clean backend
full suite, JaCoCo report, bundle thresholds, and critical-security coverage
rules all passed. No product code, test code, schema, database, external
provider, email, or retained-storage action was changed by this WI rerun.

## Execution Result

The requested non-cached clean run executed all three gates in one invocation:

```powershell
$env:GRADLE_OPTS='-Dfile.encoding=UTF-8'
.\gradlew.bat clean test jacocoTestReport jacocoTestCoverageVerification --no-build-cache --rerun-tasks --console=plain
```

Gradle returned `BUILD SUCCESSFUL` in 3 minutes 30 seconds with all eight tasks
executed. Fresh JUnit XML reports 170 suites and exactly `1,357` tests:
`0` failures, `0` errors, and `13` skips. The repaired
`TrackAudioReplacementTransactionIntegrationTest` executed and passed `1/1`.

## Coverage Result

The values below come directly from the fresh clean-run
`build/reports/jacoco/test/jacocoTestReport.xml`. The configured bundle limits
and 100% Line/Method rules for listed critical-security classes passed.

| Metric      | Covered / Total |  Ratio | Threshold | Result |
| ----------- | --------------: | -----: | --------: | ------ |
| Line        |  9,311 / 10,791 | 86.28% |       80% | PASS   |
| Method      |   1,633 / 1,953 | 83.61% |       80% | PASS   |
| Branch      |   3,138 / 4,391 | 71.46% |       70% | PASS   |
| Instruction | 42,118 / 48,917 | 86.10% |       80% | PASS   |

All configured coverage thresholds passed in the same successful Gradle run.

## Core Regression Evidence

| Area                    | Passing evidence from this run                                                                                                              | Opt-in evidence not run                                                                                    |
| ----------------------- | ------------------------------------------------------------------------------------------------------------------------------------------- | ---------------------------------------------------------------------------------------------------------- |
| Admin role              | `UserServiceTest` 41, `UserControllerTest` 10, and `AdminOperationAuditTransactionIntegrationTest` 9 tests passed.                          | 2 MySQL race tests skipped because `ATSTUDIO_ADMIN_ROLE_MYSQL_PROOF_ENABLED` was unset.                    |
| Subscription correction | `AdminSubscriptionCorrectionServiceTest` 31 and `AdminUserSubscriptionCorrectionControllerTest` 10 tests passed.                            | 2 MySQL correction races skipped because `ATSTUDIO_SUBSCRIPTION_CORRECTION_MYSQL_PROOF_ENABLED` was unset. |
| Media analysis          | `AdminTrackAudioAnalysisServiceTest` 3, controller 3, `TrackServiceAudioProcessingTest` 9, and audio replacement transaction 1 test passed. | N/A                                                                                                        |
| Tags                    | `TagNamePolicyTest` 21, constraint translator 4, branch coverage 4, available-tag query 1, and controller 13 tests passed.                  | N/A                                                                                                        |
| PlayableTrack           | `PlayableTrackQueryCountTest` 4 and `TrackControllerTest` 26 tests passed.                                                                  | N/A                                                                                                        |
| Thumbnail               | `CanonicalImageServiceTest` 14 tests passed.                                                                                                | N/A                                                                                                        |

Seven legacy payment MySQL tests and one MySQL schema-validation test were also
skipped because `ATSTUDIO_MYSQL_PROOF_ENABLED` was unset. No opt-in variable
was set and no disposable DB bootstrap, create, validate, cleanup, or retained
DB action was performed. One local storage test was skipped because symbolic
links are unavailable in this environment.

## Completion

WI-20260809-ATS-002 supplied the missing `CanonicalImageService` test double;
this clean rerun independently confirms the repair within the complete backend
suite. WI-023 is complete and may satisfy its dependency for WI-028 through
WI-030, subject to those WIs' remaining prerequisites.
