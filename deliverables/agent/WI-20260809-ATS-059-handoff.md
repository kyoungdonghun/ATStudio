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
  - path: WI-20260809-ATS-044-evidence-pack.md
    reason: Catalog functional behavior
  - path: WI-20260809-ATS-046-evidence-pack.md
    reason: Playlist functional behavior
  - path: WI-20260809-ATS-047-evidence-pack.md
    reason: Question functional behavior
  - path: WI-20260809-ATS-058-evidence-pack.md
    reason: Shared semantic baseline
---

# WI Handoff: WI-20260809-ATS-059

## WI Header

- **WI ID:** `WI-20260809-ATS-059`
- **REQ:** `REQ-20260809-ATS-001`
- **Agent:** `se`
- **Depends On:** `WI-20260809-ATS-044`, `WI-20260809-ATS-046`,
  `WI-20260809-ATS-047`, `WI-20260809-ATS-058`
- **Blocks:** `WI-20260809-ATS-076`
- **Canonical findings:** `CR-031-027`, `CR-031-029`, `CR-031-044`,
  `CR-031-124`, `CR-031-126`, `CR-031-130`

## WI Summary

### Why

Make existing catalog and member rows/cards discoverable and operable by
keyboard, expose existing playback actions without hover dependence, recover
from nonempty broken image URLs, and give public detail titles semantic heading
structure.

### Scope In

- Replace mouse-only Album, Playlist, create-card, and Question entry behavior
  with native semantic controls or keyboard-equivalent behavior while retaining
  nested action ownership and current destinations.
- Ensure a Track play command remains visible and accessible outside hover and
  already-playing states, without changing player command behavior.
- Apply the existing bounded placeholder/alt pattern when nonempty Track/Album
  images fail to load; no new media asset or external fetch is introduced.
- Apply semantic headings to public Track and Album titles without changing
  visual hierarchy or route data.
- Add focused tests for keyboard activation, nested-action non-propagation,
  visible play control, image error fallback, and headings.
- Update narrow frontend/screen-flow documentation only if externally visible
  behavior changes.

### Scope Out

- APIs, request shapes, backend, database/schema/data, playback policy,
  download behavior, routing destinations, authorization, visual redesign,
  responsive breakpoints, dependencies, and product policy.
- Functional loading/retry work owned by prior WIs and native browser acceptance
  evidence owned by WI-076.

### Definition of Done

- Owned entries can be reached and activated by keyboard without double
  navigation or accidental nested action dispatch.
- Owned Track play control is perceivable and operates through the existing
  player path.
- Broken nonempty owned images transition to the existing safe fallback.
- Public Track/Album titles are semantic headings.
- Focused/full frontend quality gates, documentation validation, and diff check
  pass; independent QA-FE review has no open P0-P3 finding.

## Constraints / Forbidden

- Do not inspect, open, hash, modify, stage, or delete
  `output/client-demo-screenshots-20260716-140514.zip` or `output/ui-ux-audit/`.
- Do not inspect ignored secrets or local environment values.
- Do not execute login/logout, payment, refund, Provider, mail, download/export,
  database-data, or other external effects.
- Preserve routes, request payloads, player command meanings, nested action
  behavior, current Korean source bytes, and existing placeholder conventions.
- Do not commit, push, merge, delete branches, or deploy.

## Acceptance Criteria

- [ ] Album, Playlist, create-card, and Question entries have keyboard-safe
      navigation with nested action isolation.
- [ ] Track play stays exposed and keyboard-operable in non-hover state.
- [ ] Nonempty broken images display an existing safe fallback and meaningful
      alternative text.
- [ ] Public Track/Album titles expose correct heading semantics.
- [ ] Focused tests, independent QA-FE review, full quality gates, docs
      validation, and `git diff --check` pass.

## Input Pointers

### Tier 0

- `docs/standards/core-principles.md`
- `docs/standards/development-standards.md`

### Tier 1

- `docs/policies/quality-gates.md`

### Tier 2

- `.agents/skills/react-best-practices/AGENTS.md`
- `docs/standards/frontend-standards.md`
- `docs/ui/screen-flow.md`

### REQ and Evidence

- `deliverables/user/REQ-20260809-ATS-001.md`
- `deliverables/agent/WI-20260809-ATS-031-consolidated-findings.md`
- `deliverables/agent/WI-20260809-ATS-044-evidence-pack.md`
- `deliverables/agent/WI-20260809-ATS-046-evidence-pack.md`
- `deliverables/agent/WI-20260809-ATS-047-evidence-pack.md`
- `deliverables/agent/WI-20260809-ATS-058-evidence-pack.md`

## Output Contract

- User-facing: `deliverables/user/WI-20260809-ATS-059-summary.md`
- Agent-facing: `deliverables/agent/WI-20260809-ATS-059-evidence-pack.md`
- Handoff: this file
- Independent QA-FE review record under the same WI prefix.

## Traceability Requirements

- Separate keyboard invocation, nested action dispatch, player/API invocation,
  image fallback, and durable-state non-impact in evidence.
- State that native browser keyboard evidence remains WI-076 and no external
  effect was executed.
- Rollback is source-control reversion only.
