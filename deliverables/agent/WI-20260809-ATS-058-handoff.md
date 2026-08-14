---
version: 1.0
last_updated: 2026-08-14
project: ATS
owner: ma
category: wi-handoff
status: active
dependencies:
  - path: ../user/REQ-20260809-ATS-001.md
    reason: Approved autonomous correction authority
  - path: WI-20260809-ATS-031-consolidated-findings.md
    reason: Canonical finding ownership and sequencing
  - path: WI-20260809-ATS-042-evidence-pack.md
    reason: Auth and account functional behavior
  - path: WI-20260809-ATS-048-evidence-pack.md
    reason: Track and Tag functional behavior
  - path: WI-20260809-ATS-051-evidence-pack.md
    reason: Whitelist and certification functional behavior
  - path: WI-20260809-ATS-052-evidence-pack.md
    reason: Subscription and payment functional behavior
---

# WI Handoff: WI-20260809-ATS-058

## WI Header

- **WI ID:** `WI-20260809-ATS-058`
- **REQ:** `REQ-20260809-ATS-001`
- **Agent:** `se`
- **Depends On:** `WI-20260809-ATS-042`, `WI-20260809-ATS-048`,
  `WI-20260809-ATS-051`, `WI-20260809-ATS-052`, `WI-20260809-ATS-057`
- **Blocks:** `WI-20260809-ATS-076`
- **Canonical findings:** `CR-031-015`, `CR-031-030`, `CR-031-040`,
  `CR-031-051`, `CR-031-062`, `CR-031-080`, `CR-031-090`

## WI Summary

### Why

Make existing forms, dialogs, status surfaces, selectable options, and Korean
operational copy understandable and operable to assistive technology without
changing domain behavior or API contracts.

### Scope In

- Normalize labels, descriptions, validation relationships, loading/failure
  announcements, and status semantics for authentication and account fields.
- Give the Tag filter an explicit accessible name, selection state, and a
  recoverable availability-error presentation.
- Complete accessible dialog, keyboard, selected-item, and live-status
  semantics for the Playlist Drawer while preserving its already-correct
  mutation behavior.
- Normalize member-facing loading and status labels, Track-form control names
  and recovery affordances, Whitelist/Certification state wording, and
  subscription/payment selection and status copy in Korean.
- Add narrow tests that prove accessible names, roles, selected state, live
  regions, keyboard operation, and error/retry semantics without making live
  Provider, mail, export, download, or mutation calls.
- Update the frontend accessibility standard/current screen-flow documentation
  only where implementation-visible behavior changes.

### Scope Out

- API request shapes, backend logic, database/schema/data, payment policy,
  Provider calls, new routes, visual redesign, responsive breakpoint changes,
  role authorization, and product-policy copy not grounded in existing
  behavior.
- Functional fixes already owned by WI-042/WI-048/WI-051/WI-052 and catalog
  card/list semantics owned by WI-059.
- Native browser/physical keyboard acceptance evidence, which remains WI-076.

### Definition of Done

- Each owned form, filter, drawer, and selection/status surface exposes an
  understandable programmatic name/state and non-silent loading/error outcome.
- Keyboard operation follows the already implemented functional transition and
  does not submit or mutate a request unexpectedly.
- Korean user-facing copy has no stale English or legacy wording in the owned
  surfaces.
- Existing request payloads and domain state transitions remain unchanged.
- Focused and full frontend quality gates, documentation validation, and diff
  check pass; independent accessibility review leaves no open P0-P3 finding.

## Constraints / Forbidden

- Do not inspect, open, hash, modify, stage, or delete
  `output/client-demo-screenshots-20260716-140514.zip` or `output/ui-ux-audit/`.
- Do not inspect ignored secrets or local environment values.
- Do not execute login/logout, payment, refund, Provider, mail, download/export,
  database-data, or other external effects.
- Preserve request payloads, API calls, role checks, backend validation, current
  Korean source bytes, and functional state machines.
- Do not invent product policy or alter cancellation, billing, certification, or
  whitelist business semantics.
- Do not commit, push, merge, delete branches, or deploy.

## Acceptance Criteria

### Functional and Accessibility

- [ ] Auth/account, Track, Whitelist/Certification, and payment controls have
      explicit accessible names, descriptions, validation/error relationships,
      and state-correct status text.
- [ ] Tag filtering and plan/option selection expose selected state and remain
      keyboard-operable without unrequested request dispatch.
- [ ] Playlist Drawer retains dialog semantics, clear focus/keyboard ownership,
      selected-item meaning, and announced recoverable outcomes.
- [ ] Owned load, empty, error, and retry states are perceivable and use current
      Korean terminology.
- [ ] Existing request shapes, transition guards, and functional-domain tests
      remain unchanged or gain only accessibility-focused assertions.

### Quality

- [ ] Focused RED/GREEN tests and relevant domain regression tests pass.
- [ ] Independent QA-FE review reports no open P0-P3 accessibility regression.
- [ ] Full frontend coverage, typecheck, ESLint, Prettier, build, documentation
      validation, and `git diff --check` pass.

## Input Pointers

### Tier 0

- `docs/standards/core-principles.md`
- `docs/standards/development-standards.md`

### Tier 1

- `docs/policies/quality-gates.md`
- `docs/policies/security-policy.md`
- `docs/policies/access-control-policy.md`

### Tier 2

- `.agents/skills/react-best-practices/AGENTS.md`
- `docs/standards/frontend-standards.md`
- `docs/ui/screen-flow.md`
- `docs/ui/modal-list.md`

### REQ and Evidence

- `deliverables/user/REQ-20260809-ATS-001.md`
- `deliverables/agent/WI-20260809-ATS-031-consolidated-findings.md`
- `deliverables/agent/WI-20260809-ATS-042-evidence-pack.md`
- `deliverables/agent/WI-20260809-ATS-048-evidence-pack.md`
- `deliverables/agent/WI-20260809-ATS-051-evidence-pack.md`
- `deliverables/agent/WI-20260809-ATS-052-evidence-pack.md`
- `deliverables/agent/WI-20260809-ATS-057-evidence-pack.md`

## Output Contract

- User-facing: `deliverables/user/WI-20260809-ATS-058-summary.md`
- Agent-facing: `deliverables/agent/WI-20260809-ATS-058-evidence-pack.md`
- Handoff: this file
- Independent QA-FE review record under the same WI prefix.

## Traceability Requirements

- Separate visible text, semantic state, keyboard invocation, API dispatch, and
  durable-state non-impact in evidence.
- Record exact tests for selected/unselected, loading/error/retry, and dialog
  keyboard paths.
- State that native browser keyboard evidence remains WI-076 and that no
  external effect was executed.
- Rollback is source-control reversion only; no Provider/data rollback applies.
