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
    reason: Canonical CR ownership, sequencing, and severity
  - path: WI-20260809-ATS-028-findings.md
    reason: Detailed reproduction evidence for modal ownership and typed execution findings
---

# WI Handoff: WI-20260809-ATS-054

## WI Header

- **WI ID:** `WI-20260809-ATS-054`
- **REQ:** `REQ-20260809-ATS-001`
- **Agent:** `se`
- **Depends On:** `WI-20260809-ATS-035`, `WI-20260809-ATS-041`,
  `WI-20260809-ATS-053`
- **Blocks:** `WI-20260809-ATS-057`, `WI-20260809-ATS-058`
- **Canonical findings:** `CR-031-099`, `CR-031-100`

## WI Summary

### Why

Prevent late ADMIN mutation results from closing, overwriting, or attaching an
error to a different modal target, and restore the deliberate operator friction
required before a local subscription correction changes durable entitlement.

### Scope In

- Bind User role, Tag form/delete, Track delete, and Company Certification
  review mutations to an immutable target or request generation.
- Pass each raw modal's pending state to the shared `Modal` so Escape, backdrop,
  header close, and owner cancel cannot detach an in-flight mutation.
- Preserve or add result-owner checks so a late response can update only the
  target and modal generation that created the request.
- Make `ConfirmDialog` forward its `busy` state to `Modal` without otherwise
  changing shared accessibility or focus behavior assigned to WI-057.
- Require the exact normalized phrase `권한 보정 실행` for local subscription
  correction execution only. Approval remains an ordinary confirmation.
- Add focused regression tests for Escape, backdrop, header close, retargeting,
  wrong/correct phrase, and duplicate execution while pending.
- Update current ADMIN correction and modal documentation to match the
  implemented behavior.

### Scope Out

- Shared shell or broad dialog accessibility/focus changes owned by WI-057.
- General copy/semantic normalization owned by WI-058.
- Payment Provider calls, refund execution, billing-key deletion, mail, export,
  download, schema/data, role policy, dependencies, or deployment.
- New confirmation wording or business policy beyond reusing the existing
  canonical phrase `권한 보정 실행`.
- Refactors of ADMIN pages outside pending target ownership.

### Definition of Done

- Every in-scope raw modal blocks all close paths while its mutation is pending.
- A request owns an immutable durable target and modal generation; stale success
  or failure cannot close, overwrite, or annotate a newer target.
- `ConfirmDialog` cannot close through Escape/backdrop/header while `busy`.
- Local correction execution stays disabled until trimmed input exactly equals
  `권한 보정 실행`; approval has no typed-phrase requirement.
- The correct phrase permits at most one execute request, and pending state
  blocks duplicate submission and closing without changing existing recovery.
- Focused tests, frontend full gates, relevant ADMIN/H2 tests, documentation
  validation, diff check, independent review, and Evidence Pack pass.

## Constraints / Forbidden

- Do not inspect, open, hash, modify, stage, or delete
  `output/client-demo-screenshots-20260716-140514.zip` or `output/ui-ux-audit/`.
- Do not inspect ignored secrets or local environment values.
- Do not execute real Toss, payment, refund, mail, export/download, provider, or
  other external side effects.
- Do not change product policy, server authorization, schema/data,
  dependencies, branches, deployment, or protected outputs.
- Preserve the existing correction preflight, unknown-outcome recovery,
  idempotency, audit, and no-Provider boundaries.
- Do not broaden shared `Modal` behavior beyond forwarding existing `busy`
  ownership; WI-057 owns later accessibility/focus work.

## Acceptance Criteria

### Functional

- [ ] User role, Tag form/delete, Track delete, and Company Certification
      review cannot close by Escape, backdrop, header close, or cancel while pending.
- [ ] Late success and failure update only the immutable target and generation
      that initiated the request; a newer modal target remains intact.
- [ ] `ConfirmDialog` forwards pending ownership to `Modal` and still supports
      normal cancellation before submission.
- [ ] Local correction execution requires trimmed exact input
      `권한 보정 실행`; blank, partial, or different text submits no request.
- [ ] Correct input allows one execute request; repeated clicks and close
      mechanisms during pending allow no duplicate and preserve current recovery.
- [ ] Approval remains a normal confirmation and executes no correction itself.

### Security and Durable-State Safety

- [ ] Server-side ADMIN authorization remains authoritative.
- [ ] No Provider call is introduced; correction tests use approved synthetic
      fixtures only.
- [ ] Existing audit, preflight, stale-state, and ambiguous-result recovery
      behavior remains unchanged and covered.

### Quality

- [ ] Focused component/page tests cover every named close and retarget path.
- [ ] Relevant backend ADMIN/H2 tests remain green without external effects.
- [ ] Frontend coverage, typecheck, ESLint, Prettier, and production build pass.
- [ ] Independent QA reports no open P0-P3 finding in WI scope.
- [ ] Documentation validation and `git diff --check` pass.

## Input Pointers

### Tier 0

- `docs/standards/core-principles.md`
- `docs/standards/development-standards.md`

### Tier 1

- `docs/policies/security-policy.md`
- `docs/policies/access-control-policy.md`
- `docs/policies/quality-gates.md`

### Tier 2

- `docs/standards/frontend-standards.md`
- `.agents/skills/react-best-practices/AGENTS.md`
- `docs/ui/modal-list.md`
- `docs/ui/screen-flow.md`
- `docs/design/api-spec.md`
- `docs/design/usecase/user-subscription.md`
- `docs/design/usecase/company-certification.md`
- `docs/payment/admin-operations-guide.md`

### REQ and Evidence

- `deliverables/user/REQ-20260809-ATS-001.md`
- `deliverables/agent/WI-20260809-ATS-031-consolidated-findings.md`
- `deliverables/agent/WI-20260809-ATS-028-findings.md`
- `deliverables/agent/WI-20260809-ATS-020-acceptance-matrix.md`
- `deliverables/agent/WI-20260809-ATS-035-evidence-pack.md`
- `deliverables/agent/WI-20260809-ATS-041-evidence-pack.md`
- `deliverables/agent/WI-20260809-ATS-053-evidence-pack.md`

### Primary Files

- `frontend/src/components/ui/Modal.tsx`
- `frontend/src/components/ui/ConfirmDialog.tsx`
- `frontend/src/components/ui/ConfirmDialog.test.tsx`
- `frontend/src/pages/admin/UserManagePage.tsx`
- `frontend/src/pages/admin/TagManagePage.tsx`
- `frontend/src/pages/admin/TrackManagePage.tsx`
- `frontend/src/pages/admin/CompanyCertManagePage.tsx`
- `frontend/src/pages/admin/UserSubscriptionCorrectionModal.tsx`
- `frontend/src/pages/admin/UserSubscriptionManagePage.test.tsx`
- `frontend/src/pages/admin/PaymentOperationsPage.tsx`

## Output Contract

- User-facing: `deliverables/user/WI-20260809-ATS-054-summary.md`
- Agent-facing: `deliverables/agent/WI-20260809-ATS-054-evidence-pack.md`
- Handoff: this file
- Independent QA result: `deliverables/agent/WI-20260809-ATS-054-qa-result.md`

## Traceability Requirements

- Separate visible modal behavior, request invocation, server response, and
  durable-state implications.
- Record immutable target/generation ownership and exact invocation counts.
- Record commands and exact pass/fail counts, including remediation runs.
- Document rollback as file/commit-level reversion; no live provider or data
  rollback should be required.
