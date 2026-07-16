---
version: 1.0
last_updated: 2026-07-16
project: ATS
owner: re
category: work-summary
status: stable
dependencies:
  - path: REQ-20260716-ATS-002.md
    reason: Approved remediation and verification scope
  - path: ../agent/WI-20260716-ATS-013-handoff.md
    reason: Backend regression verification contract
  - path: ../agent/WI-20260716-ATS-013-evidence-pack.md
    reason: Reproducible commands, metrics, and findings
---

# WI-20260716-ATS-013 Summary

## Outcome

The current development branch completes a clean backend regression, JaCoCo report generation, and application build. The final JUnit result is 146 suites / 1,046 tests: 1,037 executed successfully, 0 failures, 0 errors, and 9 skipped. The assembled Spring Boot artifact was also created successfully.

No P0/P1 correctness regression was found. Cross-layer review may proceed, but the environment-specific skipped tests and risk-heavy coverage gaps below must remain visible to WI-015 through WI-017. This WI does not claim retained-MySQL, live Toss/OAuth, deployed secret/proxy, or production-runtime proof.

## Coverage Observation

Coverage is an observation under the approved REQ, not an enforced release threshold. The current `build.gradle` has report generation but no `jacocoTestCoverageVerification` rule.

| Metric | Covered | Missed | Total | Coverage |
|---|---:|---:|---:|---:|
| Instructions | 34,615 | 10,842 | 45,457 | 76.15% |
| Branches | 2,226 | 1,544 | 3,770 | 59.05% |
| Lines | 7,653 | 2,311 | 9,964 | 76.81% |
| Complexity | 2,060 | 1,738 | 3,798 | 54.24% |
| Methods | 1,451 | 444 | 1,895 | 76.57% |
| Classes | 341 | 48 | 389 | 87.66% |

The most important residual test gaps are:

- `StorageMutationJournalService`: 11.11% lines / 0% branches, with no direct test report. This is a compensation and cleanup-journal path.
- `AdminPaymentController` and `AdminPaymentReadService`: 0% lines. Lower services have focused coverage, but the aggregate HTTP/read wiring does not.
- `PaymentApplicationService`: 26.97% lines / 25% branches. Most uncovered methods are retained legacy one-time subscription internals; current tests prove that new one-time subscription preparation/confirmation is blocked and legacy terminal behavior is bounded.
- `DownloadService`: 54.39% lines / 41.67% branches. Entitlement, quota, first-license, re-download, and atomicity paths are covered; download-history read methods remain uncovered.
- `OAuth2Service`: 67.86% lines / 50% branches. Strict provider parsing is covered, while real provider fetch compatibility remains environment-conditional.

## Skipped Tests

- 8 tests require `ATSTUDIO_MYSQL_PROOF_ENABLED=true`: seven disposable-MySQL payment concurrency races and one Hibernate schema-validation gate. They do not weaken local unit/H2 conclusions, but retained/fresh MySQL concurrency and validation remain `ENVIRONMENT-CONDITIONAL`.
- 1 local-storage symbolic-link rejection test was aborted because symbolic links are unavailable in this Windows environment. Directory rejection executed; the symbolic-link branch remains environment-conditional.

## Policy Regression Check

- Public tracks remain fully streamable. Full-resource and Range tests use the original complete resource even when a legacy preview key exists.
- Official downloads remain authenticated and enforce active subscription, plan quota, one user/track license, and atomic download counting.
- Recurring card payment remains the billing-agreement/billing-key flow. Provider failure, reconciliation, encryption, scheduler, renewal, and withdrawal cleanup areas are represented in the suite.
- Single-server scheduling remains unchanged. No distributed-lock dependency or topology change was introduced.

## Residual Findings

| Severity | Finding | Disposition |
|---|---|---|
| P2 / Medium | Risk-heavy backend paths have incomplete direct/branch coverage, especially storage compensation and admin payment HTTP/read wiring. | Add risk-based tests in WI-017; do not chase percentage alone. |
| P2 / Medium | Eight MySQL-only payment race/schema tests were skipped by their explicit environment gate. | Keep X-01 environment-conditional until disposable/copied MySQL evidence exists. |
| P3 / Low | One symbolic-link rejection branch could not execute on this host. | Re-run in an environment that permits safe temporary symbolic links. |
| P3 / Low | JaCoCo requires the configured 1 GiB test heap, and the complete clean run takes about two minutes. | Keep CI timeout above the observed 115-second wall time and monitor test memory. |

## Verification

- `gradlew.bat clean test jacocoTestReport --console=plain`: PASS in 1m 54s; seven tasks executed.
- `gradlew.bat build --console=plain`: PASS in 3s; Spring Boot JAR assembled.
- JUnit report: `build/reports/tests/test/index.html`.
- JaCoCo XML/HTML: `build/reports/jacoco/test/jacocoTestReport.xml`, `build/reports/jacoco/test/html/index.html`.
- `git diff --check`: PASS; only non-failing working-tree line-ending warnings.
- `frontend/tsconfig.tsbuildinfo`: untouched; SHA-256 remained `B6A42AD2CD32A5AD04D06C55B8B1B26DD9B1894AFDAF7B508DFF16EF0C60F22A`.

WI-013 changed only this summary and its Evidence Pack. It did not modify product/test code, database/data, client branch/runtime, frontend generated state, Git staging, commits, or remotes.
