---
version: 1.0
last_updated: 2026-08-14
project: ATS
owner: qa-fe
category: wi-review
status: completed
dependencies:
  - path: WI-20260809-ATS-057-handoff.md
    reason: Review scope and acceptance contract
  - path: WI-20260809-ATS-057-pg-review.md
    reason: Independent PG R1 findings
  - path: WI-20260809-ATS-057-pg-r2-review.md
    reason: PG closure claims requiring independent QA-FE verification
  - path: ../../docs/standards/frontend-standards.md
    reason: Current React, layout, and responsive standards
  - path: ../../docs/ui/screen-flow.md
    reason: Current public and ADMIN shell flow contract
---

# WI-20260809-ATS-057 Independent QA-FE Review

## Decision

**FAIL**

| Severity        | Open findings |
| --------------- | ------------: |
| P0              |             0 |
| P1              |             0 |
| P2              |             2 |
| P3              |             0 |
| **Total P0-P3** |         **2** |

The focused suite and final full coverage run pass, and the PG R1 defects are
corrected in their isolated cases. Two cross-state defects remain: an open
ADMIN mobile drawer can leave the desktop shell inert after a breakpoint
transition, and Header/ADMIN navigation disclosures do not provide a complete
route-close and destination-focus path.

This review uses source, CSS, jsdom, and static evidence only. Native viewport,
keyboard, pointer, and browser focus acceptance remains owned by WI-076.

## Actionable Findings

### QA-FE-057-001 - P2 - A mobile-to-desktop transition can leave the ADMIN shell inert behind a hidden drawer

**Locations:**

- `frontend/src/layouts/AdminLayout.tsx:144-194`
- `frontend/src/layouts/AdminLayout.tsx:202-225`
- `frontend/src/layouts/AdminLayout.tsx:242-268`
- `frontend/src/layouts/AdminLayout.module.css:25-31`
- `frontend/src/layouts/AdminLayout.module.css:181-183`
- `frontend/src/layouts/AdminLayout.module.css:220-257`
- `frontend/src/layouts/AdminLayout.test.tsx:66-98`

**Evidence:** `sidebarOpen` alone owns background `inert`/`aria-hidden`, the
document-level Tab trap, and the mobile drawer DOM. CSS changes the mobile
drawer and overlay from visible at `max-width: 767px` to `display: none` above
that breakpoint, while restoring the desktop sidebar. No media-query or resize
state transition closes the drawer. If the viewport widens while the drawer is
open, the mobile dialog and overlay become visually absent but `sidebarOpen`
remains true: main, topbar content, and Toast stay isolated, and the document
listener continues redirecting Tab toward focusable links inside the
CSS-hidden mobile drawer. The desktop sidebar remains mounted, so this is a
split visual/interaction state rather than a closed drawer.

**Verification:** The passing AdminLayout tests open the CSS-hidden mobile
toggle directly in jsdom and never change breakpoint state. They prove the
open-mobile and closed initial structures but cannot fail when CSS hides an
already-open drawer at desktop width. Static state/CSS tracing reproduces the
transition without claiming WI-076 native viewport evidence.

**Required correction:** Make mobile drawer ownership responsive, closing and
removing isolation/trap state when the mobile query stops matching. Add a
mocked-media-query component test that opens at mobile width, transitions above
767px, and asserts: no mobile dialog/overlay, no background `inert` or
`aria-hidden`, no active drawer Tab trap, and an unchanged desktop sidebar.

### QA-FE-057-002 - P2 - Navigation disclosures either lose focus on route change or remain open on same-path activation

**Locations:**

- `frontend/src/layouts/Header.tsx:128-132`
- `frontend/src/layouts/Header.tsx:291-377`
- `frontend/src/layouts/Header.test.tsx:86-108`
- `frontend/src/layouts/AdminLayout.tsx:66-77`
- `frontend/src/layouts/AdminLayout.tsx:144-147`
- `frontend/src/layouts/AdminLayout.tsx:211-225`
- `frontend/src/layouts/AdminLayout.test.tsx:152-178`

**Evidence:** Both shells close navigation only from an effect keyed to
`location.pathname`; their route links do not close the disclosure directly.
For a different pathname, the focused Link is conditionally unmounted and no
shell or route-level destination focus policy moves focus to the new page
heading/main. Focus therefore falls to the document body. For the same
pathname, including a Header `/tracks` link selected from `/tracks?...`, the
effect dependency does not change and the open menu/drawer remains mounted.
The current tests cover only a different-path link and assert merely that the
opener is not focused; they do not assert destination focus or same-path close.

**Verification:** Repository focus-path review found no shared route-focus
coordinator outside the shell/modal code. The existing jsdom route cases pass
because they check closure and non-restoration only. A regression test that
asserts active focus on the destination heading/main would fail, and a test
starting on the exact selected href would observe the disclosure still open.
This is a jsdom/static finding, not a native navigation claim.

**Required correction:** Close on navigation-command activation even when the
pathname is unchanged, and give successful SPA navigation a deterministic
destination focus target such as the current page H1 or main region. Preserve
exact opener restoration for non-navigation Escape/overlay close. Add Header
and ADMIN tests for both different-path destination focus and exact same-path
closure.

## Verified Review Areas

- **MainLayout shortcuts:** default-prevented and modified events, native form
  controls, links/buttons, contenteditable descendants, dialog/composite roles,
  sliders, and tabbable custom controls are excluded. Ordinary-content
  play/pause, previous/next, and first-Track ArrowDown paths remain positive.
- **Header:** closed mobile controls are absent from the DOM; both openers expose
  `aria-controls`/`aria-expanded`; Escape and overlay restore the exact opener;
  Korean theme names are state-correct; desktop account, Login, and subscription
  commands each use one styled Link with no nested Button. Route behavior is
  limited by QA-FE-057-002.
- **ADMIN:** desktop and conditional mobile sidebars are distinct; initial
  focus, Tab/Shift+Tab containment, Escape/overlay restoration, real Toast
  ancestry under `inert`/`aria-hidden`, mutation-owned logout blocking, active
  routes, and desktop sidebar preservation pass at the tested fixed state.
  Breakpoint and route transitions remain open in QA-FE-057-001/002.
- **PlayerBar:** both no-Track and active-Track mobile detail panels are
  conditionally mounted; collapsed panels and stale `aria-controls` are absent.
  Escape is scoped to the mobile root, restores its exact expander, and defers
  to sibling Header and portal Modal ownership. Transport, seek, volume,
  history/playlist actions, Like, subscription projection, and mocked download
  ownership remain present on their intended paths.
- **Modal:** restoration order is connected opener, valid explicit fallback,
  highest surviving parent Modal, then current main H1/main. Removed, disabled,
  `aria-disabled`, hidden, inert, nested, busy-parent, unmount, focus-trap, busy
  Escape/backdrop, and temporary `tabindex` cleanup cases pass.
- **Responsive DOM/focus ordering:** desktop/mobile duplicate shells are split
  with CSS and conditional mobile overlay DOM. No nested interactive Header
  command was introduced. The open ADMIN breakpoint transition is the remaining
  material responsive defect.

## Coverage-Test Review

The changed coverage expectations do not hide the intended closed-tree
contract:

- `publicAuthShell.coverage.test.tsx` closes the now-modal ADMIN drawer before
  invoking the isolated logout control. Focused AdminLayout tests separately
  retain the mutation-owned logout guard and real Toast isolation assertions.
- `shellCatalogRouterGaps.coverage.test.tsx` now reopens Header before checking
  the cleared mobile search value and expands PlayerBar before querying mobile
  detail controls. Those changes match conditional DOM behavior rather than
  accepting a hidden-but-mounted control.
- The new focused tests reproduce the original PG Escape, nested fallback,
  exact opener, and real Toast cases. They do not model breakpoint transitions,
  same-path navigation, or destination focus, which is why both open findings
  survive with a green suite.

## Verification Evidence

| Check                             | Result     | Evidence                                                           |
| --------------------------------- | ---------- | ------------------------------------------------------------------ |
| Focused Vitest                    | PASS       | 7 files, 125 tests passed                                          |
| Full frontend coverage, first run | FLAKY FAIL | 1 unchanged catalog test timed out at 5 seconds; 1,409 passed      |
| Full frontend coverage, one rerun | PASS       | 107 files, 1,410 tests passed                                      |
| Coverage thresholds               | PASS       | Statements 90.01%, branches 82.21%, functions 90.75%, lines 92.61% |
| Frontend typecheck                | PASS       | `npm run typecheck`                                                |
| ESLint                            | PASS       | `npm run lint`, zero warnings allowed                              |
| Prettier                          | PASS       | `npm run format`, all matched files formatted                      |
| Tracked diff whitespace check     | PASS       | `git diff --check`                                                 |

Focused command:

```text
npm test -- src/layouts/MainLayout.test.tsx src/layouts/Header.test.tsx src/layouts/AdminLayout.test.tsx src/layouts/PlayerBar.test.tsx src/components/ui/Modal.test.tsx src/test/coverage/publicAuthShell.coverage.test.tsx src/test/coverage/shellCatalogRouterGaps.coverage.test.tsx
```

The first coverage run timed out only at
`shellCatalogRouterGaps.coverage.test.tsx:870`; the same file passed in the
focused run and the full suite passed on the single rerun. The timeout is
recorded as a test-runtime stability signal, not as a separate P0-P3 UI finding.

No login, logout, API, payment, Provider, mail, download/export, database, or
other external effect was executed. Protected output and secrets were not
opened. No source file was modified, staged, committed, or pushed.
