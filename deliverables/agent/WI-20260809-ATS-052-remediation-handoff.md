---
version: 1.0
last_updated: 2026-08-14
project: ATS
owner: ma
category: remediation-handoff
status: active
dependencies:
  - path: WI-20260809-ATS-052-qa-result.md
    reason: Independent QA defects requiring closure
---

# Remediation Handoff: WI-20260809-ATS-052

## Scope

Close exactly `QA-052-001` and `QA-052-002` from the independent QA result.

### QA-052-001

- Make reactivation confirmation use the same pending-plan and pending-cycle
  precedence as backend renewal pricing.
- Use the loaded plan list to resolve a pending target plan. If the pending
  target cannot be resolved, do not show an invented amount or allow an
  inaccurate confirmation.
- Test pending plan + cycle, pending cycle only, pending plan only if supported,
  no pending change, and unresolved target behavior.

### QA-052-002

- Establish one latest-owner rule for every Billing Agreement projection.
- A mutation/reconciliation canonical read must retire/abort any older standalone
  Billing Agreement retry before committing.
- An older retry success or failure must not overwrite newer canonical state.
- Test both stale retry success and stale retry failure after cancellation or an
  equivalent newer canonical mutation read.

## Constraints

- Do not change product, pricing, renewal, or cancellation policy.
- Do not change backend production code, schema/data, dependencies, provider,
  mail, export/download, branches, deployment, or protected outputs.
- Do not run real external effects or inspect ignored secrets/local env values.
- Do not edit the historical QA result.

## Verification

- Focused Manage tests must pass with explicit reproduction coverage.
- Re-run all four WI-052 focused test files, typecheck, changed-scope ESLint,
  Prettier, and `git diff --check`.
- Report exact changed files and results; do not create final Evidence Pack yet.
