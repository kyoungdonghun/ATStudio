---
version: 1.0
last_updated: 2026-08-14
project: ATS
owner: ma
category: remediation-handoff
status: active
dependencies:
  - path: WI-20260809-ATS-052-qa-r3-result.md
    reason: Open QA-052-004 backend-shape mismatch
---

# Remediation R3 Handoff: WI-20260809-ATS-052

## Scope

Close only `QA-052-004`.

- Resolve `pendingSubscriptionId === sub.subscription.id` from the embedded
  current Subscription plan, independent of the active public plan list.
- Keep other pending target IDs resolved from the loaded plan list and preserve
  fail-closed behavior when a genuinely different target is unavailable.
- Replace the impossible cycle-only fixture (`pendingSubscriptionId: null`) with
  the backend response shape (`pendingSubscriptionId: current plan id`).
- Cover the real cycle-only shape when the current plan is absent from the
  active plan list, plus a genuinely unresolved different pending target.

## Constraints and Verification

- No policy, backend production, schema/data, dependency, provider, mail,
  export/download, branch, deployment, or protected-output changes.
- No real external effects or ignored-secret/local-env inspection.
- Do not alter historical QA results.
- Run Manage and all four WI-052 focused tests, typecheck, changed-scope
  ESLint/Prettier, and `git diff --check`.
