---
version: 1.0
last_updated: 2026-08-14
project: ATS
owner: ma
category: wi-review-handoff
status: active
dependencies:
  - path: WI-20260809-ATS-054-handoff.md
    reason: Approved functional and quality contract
---

# Independent QA Handoff: WI-20260809-ATS-054

## Assignment

- **Agent:** `qa-fe`
- **Purpose:** independently review pending ADMIN modal ownership and typed
  local-correction execution as one integrated safety contract.
- **Mode:** review-only; do not edit product code, tests, or docs.

## Scope

- `CR-031-099`: User role, Tag create/edit/delete, Track delete, and Company
  Certification review immutable target/generation ownership.
- All pending close paths: Escape, backdrop, header close, owner cancel, and
  any reachable background retarget action.
- Shared `ConfirmDialog` pending ownership and normal pre-submit cancellation.
- `CR-031-100`: execution-only trimmed exact phrase `권한 보정 실행`, wrong and
  correct inputs, duplicate execution, preflight/recovery preservation, and no
  typed phrase on approval.
- Focused regression-test quality and current documentation gap.

## Acceptance Criteria

- [ ] Pending mutations cannot be detached by any visible/keyboard close path.
- [ ] Every request owns an immutable target and generation; late success or
      failure cannot close, overwrite, enable, or annotate a newer target.
- [ ] Any allowed retarget while an older request is pending remains read-only
      until that request settles and cannot invoke a second mutation.
- [ ] `ConfirmDialog` forwards `busy` to shared `Modal` and keeps normal cancel
      behavior before submission.
- [ ] Blank, partial, or different typed text produces zero execute requests;
      whitespace-normalized exact text permits at most one request.
- [ ] Approval remains an ordinary confirmation.
- [ ] Existing correction exact-detail preflight, stale-state rejection,
      ambiguous-result recovery, audit, and no-Provider contract are preserved.
- [ ] Tests exercise actual close/retarget/invocation behavior rather than only
      checking disabled attributes.
- [ ] PASS requires no open P0-P3 finding in WI scope.

## Input Pointers

### Tier 0

- `docs/standards/core-principles.md`
- `docs/standards/development-standards.md`

### Tier 1

- `docs/policies/quality-gates.md`
- `docs/policies/access-control-policy.md`

### Tier 2 and Evidence

- `docs/standards/frontend-standards.md`
- `.agents/skills/react-best-practices/AGENTS.md`
- `docs/ui/modal-list.md`
- `docs/ui/screen-flow.md`
- `docs/design/api-spec.md`
- `docs/design/usecase/user-subscription.md`
- `docs/design/usecase/company-certification.md`
- `docs/payment/admin-operations-guide.md`
- `deliverables/agent/WI-20260809-ATS-054-handoff.md`
- `deliverables/agent/WI-20260809-ATS-028-findings.md`
- `deliverables/agent/WI-20260809-ATS-020-acceptance-matrix.md`
- Current scoped git diff and all changed product/test files.

## Verification Expectations

- Inspect product code and tests independently.
- Run the focused frontend suite for ConfirmDialog and all five owner flows.
- Run typecheck and scoped lint/format checks when useful.
- Treat missing documentation as a closure gap, but separate it from product
  defects in findings.
- Do not rely solely on the implementation agent's pass claims.

## Output Contract

- Write only `deliverables/agent/WI-20260809-ATS-054-qa-result.md`.
- Findings first, P0-P3 classification, verdict, exact test results, evidence,
  residual risk, and precise remediation if FAIL.

## Constraints

- Do not inspect, open, hash, modify, stage, or delete protected output paths.
- Do not inspect ignored secrets/local environment values.
- Do not execute external effects or real data mutations.
- Do not edit product code/tests/docs, schema/data, dependencies, branches, or deployment.
