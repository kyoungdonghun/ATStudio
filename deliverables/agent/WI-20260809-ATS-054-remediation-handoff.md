---
version: 1.0
last_updated: 2026-08-14
project: ATS
owner: ma
category: wi-remediation-handoff
status: active
dependencies:
  - path: WI-20260809-ATS-054-handoff.md
    reason: Approved implementation contract
  - path: WI-20260809-ATS-054-qa-result.md
    reason: Independent FAIL findings requiring remediation
---

# Remediation Handoff: WI-20260809-ATS-054

## Assignment

- **Agent:** `se`
- **Findings:** `QA-FE-054-001` through `QA-FE-054-004`
- **Goal:** close all four findings without changing policy or expanding scope.

## Required Corrections

1. Lift local-correction mutation ownership to
   `UserSubscriptionManagePage`. The parent must synchronously reject every row
   retarget while request, approval, execution, or their bounded recovery owns
   the current target. Do not abort or discard an accepted mutation because a
   different row was activated.
2. Add deferred retarget tests for request, approval, and execution. Verify
   exact mutation counts, target A preservation, target B rejection, and the
   existing bounded reconciliation-read count for ambiguous outcomes.
3. Reject `CompanyCertManagePage.openDetail` while review ownership is pending.
   Preserve the original detail/list convergence and target-specific failure;
   add a test proving another row cannot replace A before the submitted review
   and authoritative refresh settle.
4. Update current English docs. Remove language saying WI-054 is still pending;
   record exact trimmed phrase `권한 보정 실행`, execute-only use, normal
   approval confirmation, shared busy close blocking, and immutable owner rules
   for User role, Tag, Track, Company review, and subscription correction.
5. Format all scoped files and rerun focused tests, typecheck, ESLint, scoped
   Prettier, and diff check.

## Constraints

- Follow `WI-20260809-ATS-054-handoff.md` and its Tier 0/1/2 pointers.
- Preserve exact-detail preflight, unknown-outcome recovery, audit, and no-
  Provider behavior.
- Do not inspect or modify protected output paths or ignored secrets.
- Do not execute external effects, change schema/data/dependencies/policy,
  create final evidence, or modify QA result history.

## Output

- Edit only product/tests/docs needed to close the findings.
- Report changed files, focused commands and exact results, and any residual
  issue in the final response.
