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
  - path: WI-20260809-ATS-057-qa-fe-review.md
    reason: R1 QA-FE findings requiring independent closure review
  - path: WI-20260809-ATS-057-pg-r2-review.md
    reason: PG R2 closure claims requiring regression review
  - path: ../../docs/standards/frontend-standards.md
    reason: Current React and responsive baseline
  - path: ../../docs/ui/screen-flow.md
    reason: Current public and ADMIN layout boundary
---

# WI-20260809-ATS-057 Independent QA-FE R2 Review

## Decision

**FAIL**

| Severity        | Open findings |
| --------------- | ------------: |
| P0              |             0 |
| P1              |             0 |
| P2              |             1 |
| P3              |             0 |
| **Total P0-P3** |         **1** |

`QA-FE-057-001` is CLOSED. The responsive ADMIN state now releases the mobile
dialog, overlay, background isolation, and document Tab trap when the viewport
becomes desktop while preserving the mounted desktop sidebar.

`QA-FE-057-002` remains OPEN. Same-path and different-path navigation within a
single surviving layout now close immediately and focus a destination H1/main,
but the pending focus owner is stored inside the layout being left. Mobile
Header navigation to ADMIN and the ADMIN drawer link back to the public site
unmount that owner before its `location.key` effect can schedule destination
focus. The focused and coverage suites do not exercise either cross-layout
activation.

This is source, CSS, jsdom, and automated-test evidence only. Native viewport,
keyboard, pointer, and browser focus acceptance remains owned by WI-076.

## R1 Finding Disposition

### QA-FE-057-001 - P2 - CLOSED - Mobile-to-desktop ADMIN drawer release

**Locations:**

- `frontend/src/layouts/AdminLayout.tsx:106-109`
- `frontend/src/layouts/AdminLayout.tsx:151-188`
- `frontend/src/layouts/AdminLayout.tsx:190-235`
- `frontend/src/layouts/AdminLayout.tsx:262-285`
- `frontend/src/layouts/AdminLayout.module.css:25-32`
- `frontend/src/layouts/AdminLayout.module.css:220-257`
- `frontend/src/layouts/AdminLayout.test.tsx:204-243`

**Closure basis:** `mobileDrawerOpen` now requires that the mobile query is not
known false. A media change to desktop updates that state and closes the drawer
without opener restoration. The conditional dialog and overlay unmount, the
trap effect removes its document listener, and all `inert`/`aria-hidden` props
leave main, topbar content, and the Toast boundary. The desktop sidebar is a
separate always-mounted node.

The transition test opens at mobile width, changes the query to desktop, and
asserts dialog/overlay removal, isolation release, an unprevented document Tab,
and unchanged desktop sidebar identity, active href, and class state.

The no-`matchMedia` path returns before listener registration and retains the
CSS-controlled fallback; jsdom does not provide `matchMedia`, so the ordinary
drawer tests exercise that path. The source also pairs modern
`addEventListener`/`removeEventListener` and legacy `addListener`/`removeListener`
callbacks safely. The legacy branch is not directly exercised by a focused
test, but static review found no open P0-P3 defect in that paired path.

### QA-FE-057-002 - P2 - OPEN - Cross-layout navigation loses destination focus

**Locations:**

- `frontend/src/layouts/Header.tsx:32-34`
- `frontend/src/layouts/Header.tsx:113-140`
- `frontend/src/layouts/Header.tsx:162-180`
- `frontend/src/layouts/Header.tsx:347-367`
- `frontend/src/layouts/AdminLayout.tsx:58-95`
- `frontend/src/layouts/AdminLayout.tsx:112-169`
- `frontend/src/layouts/AdminLayout.tsx:237-254`
- `frontend/src/router/index.tsx:127-130`
- `frontend/src/router/index.tsx:196-200`
- `frontend/src/layouts/Header.test.tsx:117-180`
- `frontend/src/layouts/AdminLayout.test.tsx:259-302`

**Verified correction:** A normal unmodified link activation calls
`beginNavigationFocus()` before React Router handles the Link. This closes the
menu/drawer immediately, does not restore the opener, and records a pending
focus request. A changed `location.key` then schedules focus. The tests prove
different-path and exact same-path cases for `/tracks` inside `MainLayout` and
for `/admin/users`/`/admin/albums` inside `AdminLayout`. Escape and overlay close
still use their separate exact-opener restoration paths.

**Remaining defect:** The pending flag and scheduling effect live in the
current layout component. The mobile Header includes the ADMIN command to
`/admin/dashboard`, while the mobile ADMIN drawer includes the shared footer
Link to `/`. The router declares `MainLayout` and `/admin` `AdminLayout` as
separate branches. Either command unmounts the component holding the pending
flag before its post-navigation effect can run; the destination layout starts
with its own pending flag false. No global route-focus coordinator or transferred
navigation intent exists elsewhere in `frontend/src`. The activated Link is
also removed, so focus can fall to `body` instead of the new H1/main.

**Coverage gap:** Header tests navigate only within the public layout, and
AdminLayout tests navigate only within the ADMIN layout. Router coverage proves
that `/admin/dashboard` renders but does not activate it from an open real
Header disclosure or assert destination focus. No test activates the real
ADMIN drawer's public-site Link.

**Required correction:** Move or transfer navigation-focus ownership to a
coordinator that survives the `MainLayout`/`AdminLayout` branch replacement.
Add focused tests using the actual branch split for Header-to-ADMIN and
ADMIN-to-public activation. Each test must assert immediate disclosure closure,
no opener restoration, focus on the destination H1 or main region, and removal
of any temporary `tabindex`.

## Navigation Focus Helper

**Status: CLOSED for the helper contract.**

- `frontend/src/utils/navigationFocus.ts:1-6` rejects disconnected targets and
  targets in a `hidden`, `aria-hidden`, or `inert` subtree.
- It searches only current main regions and their level-one headings. It does
  not explicitly fall back to `document.body`, and no-destination execution
  leaves existing focus unchanged.
- `frontend/src/utils/navigationFocus.ts:9-18` adds `tabindex="-1"` only when
  required and removes it in `finally`, including unsuccessful focus attempts.
- `frontend/src/utils/navigationFocus.test.ts:10-38` proves H1/main focus,
  removal of temporary `tabindex`, no body fallback, and preservation of an
  existing target when no destination exists.

The helper cannot repair `QA-FE-057-002` after the component that owns the
pending navigation request has unmounted.

## PG R2 Regression Disposition

| Finding      | R2 status  | QA-FE R2 result | Basis |
| ------------ | ---------- | --------------- | ----- |
| `PG-057-001` | **CLOSED** | Not reopened    | PlayerBar Escape is rooted in its mobile surface and defers to Header and real Modal ownership. |
| `PG-057-002` | **CLOSED** | Not reopened    | Invalid child targets focus the highest surviving Modal before page main, including a busy parent. |
| `PG-057-003` | **CLOSED** | Not reopened    | Real Toast controls share the ADMIN inert, aria-hidden, and pointer-isolation boundary. |
| `PG-057-004` | **CLOSED** | Not reopened    | Escape/overlay restore exact openers; route activation uses the separate non-restoring path. |

The remaining cross-layout defect is the unresolved scope of
`QA-FE-057-002`; it does not reverse the four PG-specific corrections.

## Route And Behavior Regression Review

- Header mobile account, Login, and subscription Links retain `/profile`,
  `/login`, and `/subscriptions`. Added click handlers only close the disclosure
  and request focus after in-app navigation.
- Mobile and desktop search retain the existing trimmed query,
  `encodeURIComponent`, `/tracks?keyword=...&page=1` destination, blank-query
  no-op, and post-submit clear. No search API contract changed.
- Mobile logout retains `logout()` followed by replacement navigation to `/`.
  The added focus preparation does not change auth-store or route arguments.
- ADMIN menu destinations, active matching, mutation-owned logout guard,
  `logout()`, and replacement navigation remain unchanged.
- No auth API, logout, search API, download, payment, Provider, mail, or other
  external effect was executed. Relevant effects remained mocked or were
  inspected statically.

## Focused And Coverage Test Review

The edited coverage expectations do not weaken the conditional-DOM contract:

- `publicAuthShell.coverage.test.tsx` closes the isolated ADMIN drawer before
  activating logout; the focused AdminLayout suite separately proves the
  mutation owner and real Toast isolation behavior.
- `shellCatalogRouterGaps.coverage.test.tsx` reopens Header before checking the
  cleared mobile search field and expands PlayerBar before querying detail
  controls. This matches controls being absent while closed rather than
  accepting hidden mounted controls.
- MainLayout retains positive ordinary-content playback paths while focused,
  editable, modified, prevented, dialog, slider, tabbable, and composite-control
  paths remain negative.
- PlayerBar and Modal focused tests retain the real cross-layer PG regressions.

The green suite therefore supports the closed-tree and PG corrections, but it
does not close the cross-layout focus gap described in `QA-FE-057-002`.

## Verification Evidence

| Check                  | Result | Evidence |
| ---------------------- | ------ | -------- |
| Frontend typecheck     | PASS   | `npm run typecheck` |
| ESLint                 | PASS   | `npm run lint`, zero warnings allowed |
| Prettier               | PASS   | `npm run format`, all matched files formatted |
| Focused Vitest         | PASS   | 8 files, 131 tests passed |
| Full frontend coverage | PASS   | 108 files, 1,416 tests passed |
| Coverage thresholds    | PASS   | Statements 90.03%, branches 82.22%, functions 90.77%, lines 92.61% |
| Tracked diff check     | PASS   | `git diff --check` |

Focused command:

```text
npm test -- src/layouts/MainLayout.test.tsx src/layouts/Header.test.tsx src/layouts/AdminLayout.test.tsx src/layouts/PlayerBar.test.tsx src/components/ui/Modal.test.tsx src/utils/navigationFocus.test.ts src/test/coverage/publicAuthShell.coverage.test.tsx src/test/coverage/shellCatalogRouterGaps.coverage.test.tsx
```

No source code was modified, staged, committed, or pushed. Protected output and
secrets were not opened. Native browser acceptance remains explicitly deferred
to WI-076.
