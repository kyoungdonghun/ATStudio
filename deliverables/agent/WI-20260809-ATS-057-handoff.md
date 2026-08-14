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
    reason: Canonical root ownership, bounds, and sequencing
  - path: WI-20260809-ATS-043-evidence-pack.md
    reason: Stabilized routing and recovery behavior
  - path: WI-20260809-ATS-053-evidence-pack.md
    reason: Stabilized ADMIN functional behavior before shared-shell semantics
---

# WI Handoff: WI-20260809-ATS-057

## WI Header

- **WI ID:** `WI-20260809-ATS-057`
- **REQ:** `REQ-20260809-ATS-001`
- **Agent:** `se`
- **Depends On:** `WI-20260809-ATS-043`, `WI-20260809-ATS-053`
- **Blocks:** `WI-20260809-ATS-076`
- **Canonical findings:** `CR-031-003`, `CR-031-004`, `CR-031-120`,
  `CR-031-125`, `CR-031-128`, `CR-031-131`

## WI Summary

### Why

Make the shared public and ADMIN shells keyboard-safe, remove visually closed
controls from the effective interaction tree, preserve predictable focus when a
dialog opener disappears, localize shell accessible names, and eliminate nested
interactive Header controls.

### Scope In

- Make `MainLayout` playback shortcuts ignore default-prevented or modified
  events and every interactive, editable, contenteditable, dialog, slider, and
  composite-control target. Preserve one positive shortcut path from a
  non-interactive document target and the existing playback command meanings.
- Give the mobile Header menu an Escape close path, exact opener ownership,
  focus restoration, disclosure relationships, and a closed state whose menu
  controls are absent from the effective keyboard/accessibility tree.
- Localize Header theme-toggle accessible names in Korean while preserving
  current light/dark state and visual titles.
- Replace desktop Header `Link > Button` composition with exactly one
  interactive link per account, Login, and subscription route command while
  retaining the existing Button visual variants and sizes.
- Give the ADMIN mobile drawer Escape, opener relationships, initial focus,
  focus containment/restoration, background isolation while open, and a closed
  state with no mobile-drawer controls in the interaction tree. Preserve the
  desktop sidebar and all current routes, active states, mutation boundary, and
  logout behavior.
- Make the collapsed mobile PlayerBar detail controls absent from the effective
  interaction tree. Escape collapses an open detail panel and restores focus to
  its exact expander. Preserve transport, seek, volume, Like, Playlist,
  subscription, and download behavior.
- Extend shared `Modal` focus restoration with a connected/enabled explicit
  fallback and a deterministic main-heading/main fallback when the exact opener
  is removed or disabled. Preserve nested-modal stack order, focus trap, busy
  Escape/backdrop blocking, and connected opener restoration.
- Add focused RED/GREEN tests for keyboard collision, one-node Header commands,
  Korean theme names, closed-tree behavior, Escape, focus trap/restoration,
  removed/disabled opener fallback, and existing adjacent behavior.
- Synchronize the frontend accessibility standard and current shell flow notes.

### Scope Out

- Auth/logout completion semantics, safe-origin policy, Provider/payment/mail,
  API request shapes, backend code, schema/data, dependencies, route topology,
  visual redesign, new playback shortcuts, responsive breakpoint changes, or
  product copy beyond the exact shell accessible labels above.
- Catalog/member page semantics owned by WI-058/WI-059.
- Live native-keyboard acceptance owned by WI-076; this WI provides component
  and jsdom evidence without claiming native browser acceptance.
- Any protected output or historical screenshot inspection.

### Definition of Done

- Focused controls never trigger document playback shortcuts; the positive
  non-interactive shortcut path still works.
- Closed Header, ADMIN mobile drawer, and PlayerBar detail controls are not
  keyboard reachable or exposed as active controls.
- Escape and focus restoration work for each owned shell disclosure.
- ADMIN drawer open state traps focus and isolates background shell content.
- Modal restoration never falls silently to `body` when an opener is removed or
  disabled and a stable fallback exists.
- Desktop Header route commands contain exactly one interactive node.
- Independent PG and QA-FE reviews have no open P0-P3 finding.
- Focused/full frontend quality gates, documentation validation, and diff check
  pass.

## Constraints / Forbidden

- Do not inspect, open, hash, modify, stage, or delete
  `output/client-demo-screenshots-20260716-140514.zip` or `output/ui-ux-audit/`.
- Do not inspect ignored secrets or local environment values.
- Do not execute login/logout, payment, refund, Provider, mail, download/export,
  database-data, or other external effects.
- Do not change auth/session policy, route destinations, API calls, durable
  state, player command meanings, breakpoints, or domain behavior.
- Preserve current Korean source bytes. Do not rewrite unrelated strings or
  entire files because console output appears misdecoded.
- Do not commit, push, merge, delete branches, or deploy.

## Acceptance Criteria

### Functional and Accessibility

- [ ] MainLayout ignores modified/default-prevented events and focus within
      buttons, links, form fields, contenteditable, roles representing controls,
      and dialogs; non-interactive shortcuts retain current behavior.
- [ ] Header mobile menu and ADMIN mobile drawer close on Escape and restore the
      exact surviving opener; closed controls leave the interaction tree.
- [ ] ADMIN open drawer receives focus, traps Tab/Shift+Tab, and isolates
      background content until close.
- [ ] PlayerBar collapsed detail controls are absent/hidden semantically; Escape
      closes an open detail and restores the expander.
- [ ] Modal restores the connected opener first, then an explicit valid fallback,
      then a stable current main heading/main region; nested and busy behavior
      remains unchanged.
- [ ] Theme toggle names are Korean and state-correct.
- [ ] Desktop Header route actions expose one Link and no nested Button.

### Regression Safety

- [ ] Header route-change, overlay, mobile link, search, auth projection, and
      theme behavior remain covered.
- [ ] ADMIN active navigation, desktop sidebar, mutation-owned logout disable,
      and route-change drawer close remain covered.
- [ ] PlayerBar playback, seek, download ownership, and mobile controls remain
      covered without server/external effects.
- [ ] Modal focus trap, nested order, connected opener, busy Escape/backdrop,
      unmount, and new fallback cases pass.

### Quality

- [ ] Focused RED/GREEN tests pass.
- [ ] Independent PG review reports no open P0-P3 auth/control finding.
- [ ] Independent QA-FE review reports no open P0-P3 accessibility regression.
- [ ] Full frontend coverage, typecheck, ESLint, Prettier, and build pass.
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
- `docs/ui/screen-flow.md`

### REQ and Evidence

- `deliverables/user/REQ-20260809-ATS-001.md`
- `deliverables/agent/WI-20260809-ATS-031-consolidated-findings.md`
- `deliverables/agent/WI-20260809-ATS-021-findings.md`
- `deliverables/agent/WI-20260809-ATS-023-findings.md`
- `deliverables/agent/WI-20260809-ATS-030-findings.md`
- `deliverables/agent/WI-20260809-ATS-036-evidence-pack.md`
- `deliverables/agent/WI-20260809-ATS-043-evidence-pack.md`
- `deliverables/agent/WI-20260809-ATS-053-evidence-pack.md`

### Primary Files

- `frontend/src/layouts/MainLayout.tsx`
- `frontend/src/layouts/Header.tsx`
- `frontend/src/layouts/Header.module.css`
- `frontend/src/layouts/AdminLayout.tsx`
- `frontend/src/layouts/AdminLayout.module.css`
- `frontend/src/layouts/PlayerBar.tsx`
- `frontend/src/layouts/PlayerBar.module.css`
- `frontend/src/components/ui/Modal.tsx`
- `frontend/src/components/ui/Button.tsx`
- Existing corresponding tests plus new narrowly owned tests when absent.

## Output Contract

- User-facing: `deliverables/user/WI-20260809-ATS-057-summary.md`
- Agent-facing: `deliverables/agent/WI-20260809-ATS-057-evidence-pack.md`
- Handoff: this file
- Independent PG and QA-FE review records under the same WI prefix.

## Traceability Requirements

- Separate visible/focus state, DOM semantics, keyboard invocation, existing API
  effects, and durable-state non-impact.
- Record exact tests for every closed/open/removed/disabled/nested case and the
  one positive shortcut path.
- State that native browser keyboard evidence remains WI-076 and no external
  effect was executed.
- Rollback is source-control reversion only; no Provider/data rollback applies.
