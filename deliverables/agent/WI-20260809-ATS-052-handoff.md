---
version: 1.0
last_updated: 2026-08-14
project: ATS
owner: ma
category: wi-handoff
status: active
dependencies:
  - path: ../user/REQ-20260809-ATS-001.md
    reason: Approved correction authority and autonomous execution gate
  - path: WI-20260809-ATS-031-consolidated-findings.md
    reason: Canonical CR ownership and severity
  - path: WI-20260809-ATS-027-findings.md
    reason: Detailed reproduction evidence for CR-031-085 through CR-031-089
---

# WI Handoff: WI-20260809-ATS-052

## WI Header

- **WI ID:** `WI-20260809-ATS-052`
- **REQ:** `REQ-20260809-ATS-001`
- **Agent:** `se`
- **Depends On:** `WI-20260809-ATS-032`, `WI-20260809-ATS-033`, `WI-20260809-ATS-034`, `WI-20260809-ATS-051`
- **Blocks:** next open WI in the approved correction chain
- **Canonical findings:** `CR-031-085` through `CR-031-089`

## WI Summary

### Why

Repair recoverability and truthfulness in Subscription Plan, Payment, and Manage
flows. Read failures must not be projected as resource absence, malformed
checkout input must not invoke payment preparation, terminal checkout states
must describe the actual operation, and subscription reactivation must require
explicit confirmation.

### Scope In

- `CR-031-085`: Subscription Plan loading, empty, error/retry, and latest-response ownership.
- `CR-031-086`: typed Billing Agreement absence, visible retryable read failure,
  preview failure recovery, and stale-response ownership in Manage.
- `CR-031-087`: explicit allowlist parsing for required checkout plan, cycle,
  purpose, and callback/query state before any prepare invocation.
- `CR-031-088`: bounded checkout/failure copy, terminal prepare failure with
  retry, and an initial action label that states registration plus immediate
  first charge.
- `CR-031-089`: explicit confirmation before reactivation mutation.
- Focused regression tests and current-contract documentation updates.

### Scope Out

- `CR-031-080` through `CR-031-084` payment integrity work already owned by
  prerequisite WIs.
- `CR-031-090` localization/accessibility normalization and `CR-031-091`
  branch/table-count documentation ownership.
- Pricing, proration, upgrade/downgrade, cancellation, or renewal policy changes.
- Backend production logic, provider adapters, schema/data, dependencies, mail,
  export/download, or real Toss/payment/refund effects unless a concrete
  contract defect is independently proven and escalated first.

### Definition of Done

- Each canonical finding has an exact source/test closure pointer.
- Only explicit documented absence is rendered as absence; transport, auth, and
  server failures remain recoverable errors.
- All async reads use current-request ownership so stale completions cannot
  replace a newer audience, selection, or preview state.
- Missing or invalid required checkout state causes zero prepare calls.
- Failure-query content is bounded product copy and never raw arbitrary text.
- Initial registration CTA discloses the immediate first charge.
- Reactivation has a confirmation step and canceling it causes zero mutations.
- Focused tests, frontend full gates, relevant backend contract tests, document
  validation, diff check, independent QA, and Evidence Pack pass.

## Constraints / Forbidden

- Do not inspect, open, hash, modify, stage, or delete
  `output/client-demo-screenshots-20260716-140514.zip` or `output/ui-ux-audit/`.
- Do not inspect ignored secrets or local environment values.
- Do not execute real Toss, payment, refund, mail, export/download, provider, or
  other external side effects.
- Do not change product policy, subscription prices, schema/data, dependencies,
  branches, deployment, or protected outputs.
- Preserve approved backend idempotency and reconciliation behavior.
- Keep production edits narrowly within the three subscription pages and
  existing API/error helpers unless evidence requires an adjacent helper change.

## Acceptance Criteria

### Functional

- [ ] Plan audience changes cannot be overwritten by a late prior response.
- [ ] Plan error and empty states are distinct and retryable; subscription read
      failures are not silently treated as unsubscribed.
- [ ] Billing Agreement 404/typed absence is distinct from 401/403/5xx/network.
- [ ] Preview failures remain visible and retryable without fabricating absence.
- [ ] Missing/invalid plan, cycle, purpose, or required callback/query state
      makes zero prepare calls.
- [ ] Checkout prepare failure is terminal and retryable; fail callback does not
      display raw or blank provider query text.
- [ ] Initial CTA names both payment-method registration and immediate first charge.
- [ ] Reactivation confirmation approve/cancel paths are both tested.

### Quality

- [ ] Focused frontend tests pass.
- [ ] Frontend coverage, typecheck, ESLint, Prettier, and production build pass.
- [ ] Relevant backend contract tests pass without external effects.
- [ ] Independent QA reports no open P0-P3 finding in WI scope.
- [ ] Documentation validation and `git diff --check` pass.

## Input Pointers

### Tier 0

- `docs/standards/core-principles.md`
- `docs/standards/development-standards.md`

### Tier 1

- `docs/policies/security-policy.md`
- `docs/policies/quality-gates.md`
- `docs/policies/access-control-policy.md`

### Tier 2

- `docs/standards/frontend-standards.md`
- `.agents/skills/react-best-practices/AGENTS.md`
- `docs/design/api-spec.md`
- `docs/design/usecase/user-subscription.md`
- `docs/design/payment-integration-design.md`
- `docs/payment/user-flows.md`

### REQ and Evidence

- `deliverables/user/REQ-20260809-ATS-001.md`
- `deliverables/agent/WI-20260809-ATS-031-consolidated-findings.md`
- `deliverables/agent/WI-20260809-ATS-027-findings.md:127-212`
- `deliverables/agent/WI-20260809-ATS-020-acceptance-matrix.md`

### Primary Files

- `frontend/src/pages/public/SubscriptionPlanPage.tsx`
- `frontend/src/pages/public/SubscriptionPlanPage.test.tsx`
- `frontend/src/pages/subscriber/SubscriptionPaymentPage.tsx`
- `frontend/src/pages/subscriber/SubscriptionPaymentPage.test.tsx`
- `frontend/src/pages/subscriber/SubscriptionManagePage.tsx`
- `frontend/src/pages/subscriber/SubscriptionManagePage.test.tsx`
- `frontend/src/api/payments.ts`
- `frontend/src/api/userSubscriptions.ts`

## Output Contract

- User-facing: `deliverables/user/WI-20260809-ATS-052-summary.md`
- Agent-facing: `deliverables/agent/WI-20260809-ATS-052-evidence-pack.md`
- Handoff: this file
- Independent QA result: `deliverables/agent/WI-20260809-ATS-052-qa-result.md`

## Traceability Requirements

- Preserve separate evidence for UI copy/control, frontend invocation count,
  backend/provider boundary, and durable-state boundary.
- Record commands and exact pass/fail counts.
- Record any flaky rerun transparently instead of hiding the first result.
- Document rollback as a file/commit-level revert; no live provider or data
  rollback should be required.
