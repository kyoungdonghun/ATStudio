---
version: 1.0
last_updated: 2026-08-14
project: ATS
owner: pg
category: wi-review
status: completed
dependencies:
  - path: WI-20260809-ATS-057-handoff.md
    reason: Review scope and acceptance contract
  - path: WI-20260809-ATS-057-pg-review.md
    reason: R1 findings requiring independent closure review
  - path: ../../docs/policies/security-policy.md
    reason: Authentication, authorization, and protected-download semantics
  - path: ../../docs/policies/access-control-policy.md
    reason: Least-privilege and separation-of-duties baseline
---

# WI-20260809-ATS-057 Independent PG R2 Review

## Decision

**PASS**

| Severity | Open findings |
|----------|--------------:|
| P0 | 0 |
| P1 | 0 |
| P2 | 0 |
| P3 | 0 |
| **Total P0-P3** | **0** |

All four R1 findings are CLOSED. The corrected focused tests reproduce the
original cross-component conditions, the full coverage suite accepts the new
interaction-tree contract, and no new P0-P3 finding was identified in the
reviewed diff.

## R1 Finding Disposition

| Finding | R1 severity | R2 status | Closure basis |
|---------|-------------|-----------|---------------|
| `PG-057-001` | P2 | **CLOSED** | PlayerBar Escape is scoped to its own mobile surface; real Header and actual shared Modal ordering tests pass. |
| `PG-057-002` | P2 | **CLOSED** | Invalid child targets restore inside the highest surviving Modal before any page-main fallback, including a busy parent. |
| `PG-057-003` | P2 | **CLOSED** | The real Toast dismiss control is inside the ADMIN inert, `aria-hidden`, and pointer-isolation boundary while the opener remains usable. |
| `PG-057-004` | P3 | **CLOSED** | Header and ADMIN overlay/Escape dismissal restore the exact opener, while route navigation closes without forced opener focus. |

### PG-057-001 - CLOSED - PlayerBar versus Header/Modal Escape ordering

`PlayerBar` no longer installs a document-level Escape listener. Its handler at
`frontend/src/layouts/PlayerBar.tsx:90-100` is attached only to the mobile player
roots at lines 576 and 869. It requires a target inside that root and rejects a
dialog target before collapsing details. Header events originate in a sibling
surface, while shared Modal events are owned by the portal/dialog layer and do
not pass through the PlayerBar root.

The tests at `frontend/src/layouts/PlayerBar.test.tsx:437-478` now exercise the
previously missing intersections:

- real `Header` plus `PlayerBar`: one Escape closes Header, preserves expanded
  PlayerBar details, and restores the Header opener;
- expanded PlayerBar plus the actual shared `Modal`: non-busy Escape closes only
  the Modal, while busy Escape closes neither layer.

These tests would fail under the R1 document-listener ordering and therefore
provide direct regression protection rather than isolated-handler coverage.

### PG-057-002 - CLOSED - Invalid nested Modal fallback

The Modal stack now stores each entry's live dialog getter. On child cleanup,
focus restoration checks the exact opener, then the explicit fallback, then
`focusHighestRemainingModal()`, and only then the page-main fallback
(`frontend/src/components/ui/Modal.tsx:44-51,163-167`). This keeps focus inside
the surviving parent dialog even when the child opener and explicit fallback
are invalid.

`frontend/src/components/ui/Modal.test.tsx:229-259` covers removed, disabled,
`aria-disabled`, hidden, and inert nested targets, plus a removed-target case
with a busy parent. Every case asserts that the parent survives, active focus is
inside it, and the page heading behind it is not focused. Connected nested
opener order, top-level explicit/main fallback, busy close blocking, focus trap,
and unmount restoration also remain covered.

### PG-057-003 - CLOSED - ADMIN Toast isolation

ADMIN applies the same open-drawer isolation props to topbar content, main
content, and a dedicated Toast boundary
(`frontend/src/layouts/AdminLayout.tsx:202,242,262,266`). That boundary receives
both `inert` and `aria-hidden="true"`; its inert CSS boundary also sets
`pointer-events: none` (`frontend/src/layouts/AdminLayout.module.css:197-199`).
The drawer opener remains outside those isolated roots and stays connected and
enabled.

The regression test at `frontend/src/layouts/AdminLayout.test.tsx:100-119` uses
the real Toast store and `ToastContainer`, confirms that the real dismiss button
has the inert and `aria-hidden` ancestor while the drawer is open, confirms the
opener has no inert ancestor, then proves dismiss behavior returns after the
drawer closes. This directly covers the live control omitted by the R1 mock.

### PG-057-004 - CLOSED - Exact opener restoration without route override

Header records the button that actually opened the menu and restores only that
connected/enabled target for Escape or overlay dismissal
(`frontend/src/layouts/Header.tsx:111-126,133-151,292-297`). Its pathname effect
closes the menu and clears opener ownership without focusing it. The test at
`frontend/src/layouts/Header.test.tsx:86-113` proves overlay restoration and
same-menu route navigation non-restoration; the account-opener test separately
proves exact opener identity.

ADMIN similarly routes Escape and overlay dismissal through
`closeSidebar(true)` but keeps pathname closure as a direct state update
(`frontend/src/layouts/AdminLayout.tsx:122-138,147-149,157-194,213`). The test at
`frontend/src/layouts/AdminLayout.test.tsx:152-180` proves both branches and
exact opener focus after overlay activation.

## New P0-P3 Findings

None.

## Regression and Security Boundaries

- The reviewed production diff changes only frontend shell/modal interaction
  behavior and CSS. It does not modify auth/API modules, backend code, route
  topology, or durable-state code.
- Header retains `/profile`, `/login`, `/subscriptions`, search navigation, and
  `logout()` followed by replacement navigation to `/`. ADMIN retains all menu
  destinations, active-route matching, mutation-owned logout blocking,
  `logout()`, and replacement navigation to `/`.
- PlayerBar retains the existing `fetchMySubscription`, `downloadTrack`,
  `triggerBlobDownload`, structured error classification, subscription routing,
  and download ownership logic. The detail controls are conditionally absent
  only while collapsed; their command meanings are unchanged when expanded.
- MainLayout preserves the positive non-interactive play/pause and previous/next
  paths while rejecting default-prevented, modified, interactive, editable,
  dialog, slider, tabbable, and composite-control targets.
- The edited existing coverage expectations match the new contract: ADMIN closes
  the drawer before invoking the now-isolated logout control; Header search
  verifies the unmounted closed control and then its cleared value after reopen;
  theme names use the Korean state labels; PlayerBar coverage explicitly expands
  details before querying controls that are no longer present while collapsed.

No login, logout, API, payment, mail, download/export, Provider, database, or
other external effect was executed. All such behavior remained mocked or was
verified by static diff review. Protected output and ignored secrets were not
opened or modified.

## Verification Evidence

| Check | Result | Evidence |
|-------|--------|----------|
| Focused Vitest | PASS | 7 files, 125 tests passed |
| Full frontend coverage | PASS | 107 files, 1,410 tests passed |
| Coverage thresholds | PASS | Statements 90.01%, branches 82.21%, functions 90.75%, lines 92.61% |
| Frontend typecheck | PASS | `npm run typecheck` |
| Tracked diff whitespace check | PASS | `git diff --check` |

Focused command:

```text
npm test -- src/layouts/MainLayout.test.tsx src/layouts/Header.test.tsx src/layouts/AdminLayout.test.tsx src/layouts/PlayerBar.test.tsx src/components/ui/Modal.test.tsx src/test/coverage/publicAuthShell.coverage.test.tsx src/test/coverage/shellCatalogRouterGaps.coverage.test.tsx
```

Full coverage command:

```text
npm run test:coverage
```

The coverage run emitted Vitest/jsdom's non-failing `Not implemented:
navigation to another Document` diagnostic and exited successfully. Native
browser keyboard and pointer acceptance remains owned by WI-076; this R2 review
does not claim that evidence.
