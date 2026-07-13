# WI-20260711-ATS-015 Coverage Audit Summary

## Verdict

- **GAP - COVERAGE NOT MEASURABLE**
- Java has JUnit tests but no repository-configured JaCoCo plugin, report task, verification task, or JaCoCo report.
- Frontend has Vitest tests but no coverage script, coverage provider dependency, installed coverage provider, coverage configuration, or coverage report.
- No line, branch, method/function, statement, or instruction percentage is reported. Tooling absence is an instrumentation gap, not `0%` coverage.

## Current Evidence

| Area | Regression baseline | Test inventory | Coverage result |
|---|---:|---:|---|
| Java backend | 745/745 tests passed in WI-009 | 363 production Java files / 71 test files | Not measurable |
| React frontend | 51/51 tests passed in WI-010 | 111 production TS/TSX files / 14 test files | Not measurable |

- The passing regression suites prove that configured tests pass; they do not prove coverage completeness.
- No coverage command was run because neither area has a configured, runnable coverage command. Elapsed time is therefore not applicable.

## Highest-Risk Test Gaps

The following paths were identified by prior audits as P0/P1 risks without the focused integration, concurrency, security-chain, or UI tests needed to prove them:

| Priority | Risk path | Prior finding |
|---|---|---|
| P0 | Anonymous access to original paid audio through `/uploads/**` | BE-001 / PG-004-01 |
| P0 | Account withdrawal followed by automatic renewal and provider charge | BE-002 |
| P0 | Active-content playlist thumbnail upload on the application origin | PG-004-02 |
| P1 | Billing failure persistence, renewal transaction isolation, and concurrent refund reservation | BE-003 / BE-004 / BE-005 |
| P1 | Verification/reset token and PII leakage on mail failure | BE-006 / PG-004-03 |
| P1 | Malicious company-document upload and admin review boundary | PG-004-04 |
| P1 | Social login callback using the returned token for `/users/me` | FE-001 |
| P1 | ADMIN entering member recurring-billing preparation | FE-002 |
| P1 | Session invalidation after logout or credential change | PG-004-07 |

## Required Follow-up

1. Treat Java and frontend percentage coverage as unknown until dedicated coverage tooling is approved and configured in a separate implementation WI.
2. Prioritize focused tests for the P0/P1 paths above before using a future percentage threshold as a release signal.
3. Feed this evidence into WI-016, WI-017, and WI-018; a passing suite must not close the mapped security, payment, backend, or frontend risks.

Detailed commands, configuration checks, evidence pointers, and rollback information are in `deliverables/agent/WI-20260711-ATS-015-evidence-pack.md`.
