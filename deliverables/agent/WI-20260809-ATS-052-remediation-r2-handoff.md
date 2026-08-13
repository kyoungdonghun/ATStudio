---
version: 1.0
last_updated: 2026-08-14
project: ATS
owner: ma
category: remediation-handoff
status: active
dependencies:
  - path: WI-20260809-ATS-052-qa-r2-result.md
    reason: Open QA-052-003 contract defect
---

# Remediation R2 Handoff: WI-20260809-ATS-052

## Scope

Close only `QA-052-003`.

- Match the displayed reactivation date to `UserSubscriptionService.reactivate`:
  a cancelled Billing Agreement resumed by reactivation uses the Subscription
  `expiresAt`; an agreement already active retains its canonical `nextBillingAt`.
- Do not silently invent a date if the required canonical input is unavailable.
- Add a regression with a grace-extended Subscription expiry later than the
  stale pre-grace Billing Agreement date, plus the already-active agreement
  branch if it is a supported UI state.
- Ensure documentation describes the branch accurately.

## Constraints and Verification

- No product-policy, backend production, schema/data, dependency, provider,
  mail, export/download, branch, deployment, or protected-output changes.
- No real external effects or ignored-secret/local-env inspection.
- Keep previous QA records unchanged.
- Run Manage focused tests, all four WI-052 test files, typecheck, changed-scope
  ESLint/Prettier, and `git diff --check`.
