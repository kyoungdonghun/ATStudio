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
    reason: R2 finding disposition requiring final independent reinspection
  - path: WI-20260809-ATS-057-pg-r2-review.md
    reason: PG closure claims requiring regression review
  - path: ../../docs/standards/frontend-standards.md
    reason: Current React, routing, and responsive baseline
  - path: ../../docs/ui/screen-flow.md
    reason: Current public and ADMIN layout boundary
---

# WI-20260809-ATS-057 Independent QA-FE R3 Review

## Decision

**FAIL**

| Severity        | Open findings |
| --------------- | ------------: |
| P0              |             0 |
| P1              |             0 |
| P2              |             1 |
| P3              |             1 |
| **Total P0-P3** |         **2** |

`QA-FE-057-001` remains CLOSED. `QA-FE-057-002` remains OPEN because the
module-level intent works for a surviving layout but loses its scheduled focus
when a newly mounted destination layout is replayed by the application's
`StrictMode`. The green cross-layout tests do not model that runtime condition
and synchronously execute `requestAnimationFrame`, masking cancellation.

One new P3 documentation finding is OPEN. The handoff requires synchronization
of the frontend accessibility standard and shell flow notes, but neither
document is changed in the current worktree and neither records the new shell
focus, disclosure, or interaction-tree contract.

This review uses source, jsdom, local lifecycle simulation, and automated-test
evidence only. Native viewport, keyboard, pointer, and browser focus acceptance
remains owned by WI-076.

## Existing Finding Disposition

### QA-FE-057-001 - P2 - CLOSED - Mobile-to-desktop ADMIN drawer release

**Locations:**

- `frontend/src/layouts/AdminLayout.tsx:109-112`
- `frontend/src/layouts/AdminLayout.tsx:153-188`
- `frontend/src/layouts/AdminLayout.tsx:190-235`
- `frontend/src/layouts/AdminLayout.tsx:262-285`
- `frontend/src/layouts/AdminLayout.test.tsx:204-243`

`mobileDrawerOpen` still requires a non-desktop media state. A media transition
above 767px closes without opener restoration, removes the dialog and overlay,
releases the document Tab trap and all background `inert`/`aria-hidden`
boundaries, and preserves the mounted desktop sidebar and active route state.
The focused viewport transition test passes. No pending navigation-focus intent
is requested by this path: `handleMediaChange()` calls only
`closeSidebar(false)`.

### QA-FE-057-002 - P2 - OPEN - Cross-layout intent is cancelled by StrictMode replay

**Locations:**

- `frontend/src/main.tsx:1-9`
- `frontend/src/utils/navigationFocus.ts:42-76`
- `frontend/src/layouts/Header.tsx:138-140`
- `frontend/src/layouts/AdminLayout.tsx:167-169`
- `frontend/src/layouts/navigationFocus.crossLayout.test.tsx:18-43`
- `frontend/src/layouts/navigationFocus.crossLayout.test.tsx:85-90`
- `frontend/src/layouts/navigationFocus.crossLayout.test.tsx:106-153`

**Verified partial correction:** The pending flag now lives at module scope.
`requestNavigationDestinationFocus()` sets it, and the first consumer clears it
before scheduling focus. Same-layout Header and ADMIN route changes therefore
consume one intent, focus the destination H1 or main, remove temporary
`tabindex`, and do not reuse the consumed intent on a later unrelated route.

The cross-layout tests use the actual Header `관리자` link and the actual mobile
ADMIN drawer footer Link to `/`. They assert immediate disclosure removal,
opener non-restoration, destination H1 focus for MainLayout-to-AdminLayout, lazy
main fallback for AdminLayout-to-MainLayout, temporary `tabindex` cleanup, and
one-shot non-reuse.

**Remaining defect:** The application root wraps the router in `StrictMode`.
On cross-layout navigation, the destination layout is newly mounted. Its first
effect setup consumes and clears the intent and returns the scheduled focus
cancellation function. StrictMode immediately runs that cleanup before the
browser animation frame, cancelling focus. The replayed setup then sees the
already-cleared flag and schedules nothing. Focus can remain on `body` after the
source disclosure and its focused Link unmount.

The current cross-layout harness is not wrapped in `StrictMode`. It also mocks
`requestAnimationFrame` by invoking the callback synchronously, so even adding
the wrapper without correcting that mock would not exercise the cancellation
window.

**Independent local reproduction:** A no-file Node/jsdom simulation imported
the actual `navigationFocus.ts`, rendered a surviving layout update under
`StrictMode`, and then rendered a source-to-destination branch replacement
under the same root.

- Same-layout update: destination H1 focused, temporary `tabindex` absent.
- Branch replacement: destination H1 rendered, `document.activeElement`
  remained `BODY`, destination H1 not focused, temporary `tabindex` absent.

**Required correction:** Make intent consumption and scheduled focus resilient
to StrictMode setup/cleanup replay without allowing a cancelled navigation to
leak intent into a later unrelated route. Add a cross-layout regression wrapped
in the actual `StrictMode` boundary with asynchronous animation-frame control.
Retain the existing actual-link, disclosure-close, opener non-restore, H1/main
fallback, cleanup, and one-shot assertions.

## New Finding

### QA-FE-057-003 - P3 - OPEN - Required shell accessibility documentation is not synchronized

**Locations:**

- `deliverables/agent/WI-20260809-ATS-057-handoff.md:50-72`
- `docs/standards/frontend-standards.md:408-411`
- `docs/standards/frontend-standards.md:494-502`
- `docs/ui/screen-flow.md:19-34`

The handoff explicitly includes synchronization of the frontend accessibility
standard and current shell flow notes. The current worktree has no modification
to either required document. Their current text records only the MainLayout and
AdminLayout split, the generic breakpoint definitions, and route-loading
recovery. It does not record the new closed-tree disclosure contract, shell
Escape/opener behavior, responsive ADMIN isolation release, one-shot route
destination focus, PlayerBar detail semantics, or Modal fallback order.

**Required correction:** After the interaction contract is corrected, update
both documents in English with the stable behavior and preserve the WI-076
native acceptance boundary. Run documentation validation and diff checks.

## Non-Intent Dismissal And Event Guards

- Header and ADMIN mobile Link handlers return before requesting intent for a
  default-prevented event, a non-primary button, or Alt/Ctrl/Meta/Shift
  activation (`Header.tsx:168-180`, `AdminLayout.tsx:242-254`).
- Header Escape and overlay dismissal call `closeMobileMenu(true)` only. ADMIN
  Escape and overlay dismissal call `closeSidebar(true)` only. These paths
  restore the exact surviving opener and do not request route focus.
- ADMIN viewport close calls `closeSidebar(false)` only. It neither restores the
  mobile opener nor creates an intent.
- MainLayout separately rejects default-prevented and modified playback shortcut
  events before reading player state. The focused negative test passes.

No P0-P3 defect was found in these guarded paths. The route-focus suites do not
directly expose the private pending flag for every dismissal case, but static
call-path review shows no request edge from them.

## PG R2 Regression Disposition

| Finding      | R2 status  | QA-FE R3 result | Basis                                                                                                    |
| ------------ | ---------- | --------------- | -------------------------------------------------------------------------------------------------------- |
| `PG-057-001` | **CLOSED** | Not reopened    | PlayerBar Escape remains scoped to its mobile root and defers to real Header and Modal ownership.        |
| `PG-057-002` | **CLOSED** | Not reopened    | Invalid child targets still focus the highest surviving Modal before page main, including a busy parent. |
| `PG-057-003` | **CLOSED** | Not reopened    | Real Toast controls remain inside the ADMIN inert, aria-hidden, and pointer-isolation boundary.          |
| `PG-057-004` | **CLOSED** | Not reopened    | Escape/overlay restore exact openers; accepted route activation uses the separate non-restoring path.    |

The StrictMode defect is the unresolved cross-layout scope of
`QA-FE-057-002`; it does not reverse the four PG-specific corrections.

## Route And Behavior Regression Review

- Header account, Login, subscription, public, member, and ADMIN destinations
  retain their existing paths. ADMIN menu and footer destinations are unchanged.
- Search still trims the query, rejects blank input, applies
  `encodeURIComponent`, navigates to `/tracks?keyword=...&page=1`, and clears the
  field after accepted navigation.
- Header and ADMIN logout still call the existing store action and replace to
  `/`; ADMIN retains mutation-owned logout blocking. No real logout was run.
- Router topology, guard semantics, auth projection, playback command meanings,
  API shapes, and durable state code are unchanged in the reviewed diff.

## Verification Evidence

| Check                                                   | Result | Evidence                                             |
| ------------------------------------------------------- | ------ | ---------------------------------------------------- |
| Selected lifecycle regressions                          | PASS   | 7 files, 24 passed, 56 skipped                       |
| Full focused frontend suite                             | PASS   | 9 files, 134 tests passed                            |
| Frontend typecheck                                      | PASS   | `npm run typecheck`                                  |
| ESLint                                                  | PASS   | `npm run lint`, zero warnings allowed                |
| Prettier                                                | PASS   | `npm run format`, all matched files formatted        |
| Tracked diff whitespace check                           | PASS   | `git diff --check`                                   |
| Actual helper, StrictMode same-layout simulation        | PASS   | Destination H1 focused once; no temporary `tabindex` |
| Actual helper, StrictMode branch replacement simulation | FAIL   | Destination rendered; active element remained `BODY` |

Full focused command:

```text
npm test -- src/layouts/MainLayout.test.tsx src/layouts/Header.test.tsx src/layouts/AdminLayout.test.tsx src/layouts/PlayerBar.test.tsx src/components/ui/Modal.test.tsx src/utils/navigationFocus.test.ts src/layouts/navigationFocus.crossLayout.test.tsx src/test/coverage/publicAuthShell.coverage.test.tsx src/test/coverage/shellCatalogRouterGaps.coverage.test.tsx
```

The StrictMode simulations were passed through stdin to Node and created no
file. No production source, test, standard, or flow document was modified. Only
this requested review record was added. No real login, logout, API, payment,
Provider, mail, download/export, database, or other external effect occurred.
Protected output and secrets were not opened. Nothing was staged, committed, or
pushed. Native browser acceptance remains explicitly deferred to WI-076.
