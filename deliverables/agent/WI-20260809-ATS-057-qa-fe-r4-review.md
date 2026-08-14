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
    reason: R1 QA-FE findings and original closure requirements
  - path: WI-20260809-ATS-057-qa-fe-r2-review.md
    reason: R2 cross-layout finding requiring closure review
  - path: WI-20260809-ATS-057-qa-fe-r3-review.md
    reason: R3 StrictMode defect and documentation finding requiring final reinspection
  - path: WI-20260809-ATS-057-pg-review.md
    reason: Four PG findings requiring reopening review
  - path: WI-20260809-ATS-057-pg-r2-review.md
    reason: PG closure evidence requiring independent regression review
  - path: ../../docs/standards/frontend-standards.md
    reason: Current React, layout, and responsive baseline
  - path: ../../docs/ui/screen-flow.md
    reason: Current public and ADMIN shell flow contract
---

# WI-20260809-ATS-057 Independent QA-FE R4 Final Code Reinspection

## Decision

**Overall WI decision: FAIL**

**Code decision: PASS. Open code findings: 0.**

| Finding class          |    P0 |    P1 |    P2 |    P3 | Total |
| ---------------------- | ----: | ----: | ----: | ----: | ----: |
| Code                   |     0 |     0 |     0 |     0 | **0** |
| Documentation          |     0 |     0 |     0 |     1 | **1** |
| **Overall open P0-P3** | **0** | **0** | **0** | **1** | **1** |

`QA-FE-057-001` remains CLOSED. `QA-FE-057-002` is CLOSED in R4. The
module-level navigation intent is now consumed synchronously, so StrictMode
effect replay has no scheduled frame to cancel. MainLayout-to-AdminLayout,
AdminLayout-to-MainLayout with a lazy-route main fallback, and same-layout
navigation all focus a valid destination and do not reuse the consumed intent.

`QA-FE-057-003` remains OPEN P3 as a documentation-only finding pending the
DocOps transition. It is not counted as a code finding. The handoff requires
the frontend accessibility standard and shell flow notes to be synchronized,
but neither document is changed in the current worktree.

This review uses source, CSS, jsdom, and local automated-test evidence only.
Native viewport, keyboard, pointer, and browser focus acceptance remains owned
by WI-076.

## Existing Finding Disposition

### QA-FE-057-001 - P2 - CLOSED - Mobile-to-desktop ADMIN drawer release

**Locations:**

- `frontend/src/layouts/AdminLayout.tsx:138-188`
- `frontend/src/layouts/AdminLayout.tsx:190-235`
- `frontend/src/layouts/AdminLayout.tsx:262-328`
- `frontend/src/layouts/AdminLayout.module.css:22-31`
- `frontend/src/layouts/AdminLayout.module.css:197-199`
- `frontend/src/layouts/AdminLayout.module.css:220-257`
- `frontend/src/layouts/AdminLayout.test.tsx:204-243`

`mobileDrawerOpen` still requires a non-desktop media state. A transition above
767px closes the drawer without opener restoration, removes the dialog and
overlay, releases the document Tab trap and all background `inert` and
`aria-hidden` boundaries, and preserves the mounted desktop sidebar and active
route state. The focused breakpoint-transition regression passes.

### QA-FE-057-002 - P2 - CLOSED - StrictMode-safe one-shot destination focus

**Locations:**

- `frontend/src/main.tsx:1-9`
- `frontend/src/utils/navigationFocus.ts:5-53`
- `frontend/src/utils/navigationFocus.test.ts:13-60`
- `frontend/src/layouts/Header.tsx:132-180`
- `frontend/src/layouts/AdminLayout.tsx:162-169`
- `frontend/src/layouts/AdminLayout.tsx:237-254`
- `frontend/src/layouts/navigationFocus.crossLayout.test.tsx:18-45`
- `frontend/src/layouts/navigationFocus.crossLayout.test.tsx:65-155`

**Closure basis:**

- `requestNavigationDestinationFocus()` sets one module-level pending boolean.
- `consumeNavigationDestinationFocus()` returns when no intent exists; otherwise
  it clears the boolean before calling `focusNavigationDestination()` in the
  same effect setup. It creates no timer, animation frame, or cleanup callback.
- The first destination layout effect therefore completes focus before
  StrictMode performs its setup/cleanup replay. The replay sees a consumed
  intent and cannot cancel or reuse it.
- MainLayout-to-AdminLayout focuses the destination H1. The reverse branch with
  a permanently pending lazy page focuses the surviving `main` fallback.
- Same-layout Header and ADMIN tests focus changed-path and exact same-path
  destinations. The cross-layout suite then proves that a later unrelated route
  does not reuse the consumed intent.
- Temporary `tabindex="-1"` is removed after every focus attempt.

The cross-layout harness is wrapped in `StrictMode`. Its
`requestAnimationFrame` mock returns an ID without invoking a callback, and all
three regressions assert that the API was not called. It therefore cannot hide
the R3 cancellation window through synchronous frame execution.

## Intent Negative Paths

### No destination

`focusNavigationDestination()` searches available main regions and their H1
targets only. It has no `document.body` fallback. The helper regression requests
one intent with no destination, consumes it while preserving an existing
focused button, adds a main afterward, and proves a second consume neither
changes focus nor adds a temporary `tabindex`. The missing destination therefore
consumes only the intent.

### Modified or prevented navigation

Header and ADMIN mobile Link handlers reject `defaultPrevented`, non-primary,
Alt, Ctrl, Meta, and Shift activations before calling the sole intent-request
edge (`Header.tsx:168-180`, `AdminLayout.tsx:242-254`). React Router receives
normal unmodified commands only after this guard. No intent is created by the
rejected paths.

### Non-navigation dismissal

- Header Escape and overlay paths call only `closeMobileMenu(true)`.
- ADMIN Escape and overlay paths call only `closeSidebar(true)`.
- ADMIN desktop-breakpoint release calls only `closeSidebar(false)`.
- These paths restore or preserve focus according to their own close contract
  and have no call edge to `requestNavigationDestinationFocus()`.

The modified/prevented and dismissal conclusions are direct source call-path
evidence. The focused suite does not expose the private module boolean solely
to assert each rejected modifier variant; the guards are side-effect-free early
returns and no ambiguous request edge was found.

## PG Finding Reopening Review

| PG finding   | R4 disposition           | Independent basis                                                                                                                                      |
| ------------ | ------------------------ | ------------------------------------------------------------------------------------------------------------------------------------------------------ |
| `PG-057-001` | **CLOSED, not reopened** | PlayerBar Escape is rooted at its mobile surface, rejects dialog targets, and the real Header plus real shared Modal ordering regressions pass.        |
| `PG-057-002` | **CLOSED, not reopened** | Invalid child opener and fallback targets focus the highest surviving Modal before page main, including a busy parent.                                 |
| `PG-057-003` | **CLOSED, not reopened** | The real Toast dismiss control remains within the ADMIN `inert`, `aria-hidden`, and pointer-isolation boundary while the drawer opener remains usable. |
| `PG-057-004` | **CLOSED, not reopened** | Escape and overlay dismissal restore the exact valid opener; accepted route navigation uses a separate non-restoring destination-focus path.           |

Relevant regression pointers are
`frontend/src/layouts/PlayerBar.tsx:90-100`,
`frontend/src/layouts/PlayerBar.test.tsx:419-478`,
`frontend/src/components/ui/Modal.tsx:44-50,153-167`,
`frontend/src/components/ui/Modal.test.tsx:209-253`, and
`frontend/src/layouts/AdminLayout.test.tsx:145-182`.

## New Code Findings

None. The R4 current-diff inspection identified **0 new P0-P3 code findings**.

The reviewed production change remains limited to shared frontend shell, Modal,
focus, and CSS behavior. No auth or API module, router topology, backend code,
schema, durable-state contract, playback command meaning, or external-effect
implementation is changed.

## Documentation Finding

### QA-FE-057-003 - P3 - OPEN - Required shell accessibility documentation is not synchronized

**Finding class:** Documentation only, pending DocOps. This finding does not
change the R4 code PASS or the zero code-finding count.

**Locations:**

- `deliverables/agent/WI-20260809-ATS-057-handoff.md:50-72`
- `docs/standards/frontend-standards.md:408-411`
- `docs/standards/frontend-standards.md:494-502`
- `docs/ui/screen-flow.md:19-34`

The handoff requires synchronization of the frontend accessibility standard
and current shell flow notes. Neither required document is modified in the
current worktree, and the cited sections still omit the closed-tree disclosure,
Escape/opener restoration, responsive ADMIN isolation release, one-shot route
destination focus, PlayerBar detail, and Modal fallback contracts.

**Required DocOps closure:** Update both English documents with the stable
contract, retain the WI-076 native-acceptance boundary, and run documentation
validation plus diff checks.

## Verification Evidence

| Check                                   | Result | Evidence                                                                           |
| --------------------------------------- | ------ | ---------------------------------------------------------------------------------- |
| Focused Vitest                          | PASS   | 9 files, 135 tests passed                                                          |
| StrictMode cross-layout regressions     | PASS   | Main to ADMIN H1, ADMIN to lazy Main main fallback, one-shot non-reuse; rAF unused |
| Missing-destination one-shot regression | PASS   | Existing focus preserved; later main not focused by a second consume               |
| Frontend typecheck                      | PASS   | `npm run typecheck`                                                                |
| ESLint                                  | PASS   | `npm run lint`, zero warnings allowed                                              |
| Prettier                                | PASS   | `npm run format`, all matched files formatted                                      |
| Tracked diff whitespace check           | PASS   | `git diff --check`                                                                 |

Focused command:

```text
npm test -- src/layouts/MainLayout.test.tsx src/layouts/Header.test.tsx src/layouts/AdminLayout.test.tsx src/layouts/PlayerBar.test.tsx src/components/ui/Modal.test.tsx src/utils/navigationFocus.test.ts src/layouts/navigationFocus.crossLayout.test.tsx src/test/coverage/publicAuthShell.coverage.test.tsx src/test/coverage/shellCatalogRouterGaps.coverage.test.tsx
```

## Final Status

- **Code:** PASS, 0 open P0-P3 findings.
- **Documentation:** FAIL, 1 open P3 finding (`QA-FE-057-003`).
- **Overall WI-057:** FAIL, 1 total open P0-P3 finding, pending DocOps only.

No source, test, standard, or flow document was modified. Only this requested
R4 review record was added. No real login, logout, API, payment, Provider, mail,
download/export, database, or other external effect was executed. Protected
output and secrets were not opened. Nothing was staged, committed, or pushed.
Native browser acceptance remains explicitly deferred to WI-076.
