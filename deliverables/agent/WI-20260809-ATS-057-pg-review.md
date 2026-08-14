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
  - path: ../user/REQ-20260809-ATS-001.md
    reason: Approved authority and external-effect boundary
  - path: ../../docs/policies/security-policy.md
    reason: Authentication, authorization, and protected-download semantics
  - path: ../../docs/policies/access-control-policy.md
    reason: Least-privilege and separation-of-duties baseline
---

# WI-20260809-ATS-057 Independent PG Review

## Decision

**FAIL**

| Severity | Open findings |
|----------|--------------:|
| P0 | 0 |
| P1 | 0 |
| P2 | 3 |
| P3 | 1 |
| **Total P0-P3** | **4** |

The focused tests, TypeScript typecheck, and diff whitespace check pass, but they
do not cover the cross-component Escape ordering, nested invalid-opener fallback,
or the complete ADMIN background interaction surface described below.

## Actionable Findings

### PG-057-001 - P2 - PlayerBar consumes Escape before the owning Header or Modal layer

**Locations:**

- `frontend/src/layouts/PlayerBar.tsx:99-110`
- `frontend/src/layouts/PlayerBar.tsx:1055-1093`
- `frontend/src/layouts/Header.tsx:122-132`
- `frontend/src/components/ui/Modal.tsx:122-137`
- `frontend/src/layouts/PlayerBar.test.tsx:135-145`

**Evidence:** `PlayerBar` registers a document-level listener whenever mobile
details are expanded. It handles every non-prevented Escape without checking the
event target or whether a higher interaction layer owns the key, immediately
prevents the event, collapses the details, and focuses the expander. The Header
listener runs later at `window` and explicitly ignores a default-prevented event.
Therefore, with both surfaces open, Escape from Header mobile search collapses
PlayerBar and leaves the Header menu open. A History `Modal` opened from the
expanded panel is mounted after the PlayerBar listener; Escape consequently
collapses the underlying player before the Modal processes the same key. A busy
Modal remains open while its underlying opener panel is still removed.

The PlayerBar test replaces `HistoryModal`, `PlaylistDrawer`, and
`AddToPlaylistModal` with null components, so its passing Escape test proves only
the isolated panel path and cannot detect this ordering defect.

**Reproduction / verification:**

1. Render the real Header and PlayerBar in the public shell.
2. Expand mobile PlayerBar details, open the Header mobile menu, and focus its
   search input.
3. Dispatch one bubbling Escape from the input.
4. Observe that PlayerBar collapses while the Header menu remains mounted; a
   second Escape is required to close the Header menu.
5. Separately, open History from expanded PlayerBar details and dispatch Escape.
   Verify that only the top interaction layer changes. The current code also
   collapses the underlying player; with a busy Modal, the Modal remains while
   its underlying panel is removed.

**Required correction:** Scope PlayerBar Escape ownership to its own active
surface and defer to the topmost menu/dialog layer, or use one shared top-layer
keyboard coordinator. Add a real Header plus PlayerBar test and a PlayerBar plus
Modal busy/non-busy ordering test.

### PG-057-002 - P2 - Invalid nested Modal opener falls through to page main behind the parent Modal

**Locations:**

- `frontend/src/components/ui/Modal.tsx:26-37`
- `frontend/src/components/ui/Modal.tsx:122-151`
- `frontend/src/components/ui/Modal.test.tsx:152-170`
- `frontend/src/components/ui/Modal.test.tsx:191-234`

**Evidence:** The Modal stack stores symbols only. When a child Modal closes and
its opener and explicit fallback are removed, disabled, hidden, or inert, the
cleanup always calls `focusMainFallback()`. It does not first restore focus into
the still-open parent Modal. The resulting active element can be an `h1` or
`main` behind the parent `aria-modal="true"` dialog, breaking the surviving
Modal's focus boundary. Existing tests cover a connected nested opener and
top-level invalid-opener fallbacks separately, but not their intersection.

The new target validator correctly rejects disconnected, disabled,
`aria-disabled`, hidden, `aria-hidden`, and inert targets. The defect is the
next fallback tier while another Modal remains open, not those checks.

**Reproduction / verification:** Extend the nested harness so the child opener
can be removed or disabled while the child Modal stays open. Close only the
child. Assert that the parent dialog remains and that `document.activeElement`
is inside it. The current implementation focuses the page main heading/region
instead.

**Required correction:** Before page-main fallback, restore focus to a valid
target in the highest remaining Modal stack entry. Add removed, disabled,
hidden, and inert child-opener cases plus a busy-parent case.

### PG-057-003 - P2 - ADMIN drawer leaves Toast controls outside background isolation

**Locations:**

- `frontend/src/layouts/AdminLayout.tsx:188-188`
- `frontend/src/layouts/AdminLayout.tsx:232-256`
- `frontend/src/layouts/AdminLayout.test.tsx:18-18`
- `frontend/src/components/ui/ToastContainer.tsx:19-37`
- `frontend/src/components/ui/ToastContainer.module.css:1-6`

**Evidence:** The drawer applies `inert` and `aria-hidden` only to
`topbarContent` and `main`. `ToastContainer` is a sibling outside both isolated
roots. A rendered toast includes a focusable dismiss button, and its `z-index`
is 9999 versus the ADMIN overlay/drawer values of 99/100. It therefore remains
an exposed and pointer-actionable control while the drawer claims
`aria-modal="true"`. The ADMIN test mocks `ToastContainer` to null, so the
isolation assertion omits this live interaction surface.

**Reproduction / verification:** Seed one toast, render `AdminLayout`, and open
the mobile drawer. The toast dismiss button has no inert or aria-hidden ancestor
and can be clicked above the overlay. A regression test should assert that every
focusable element outside the drawer and its intentional opener is isolated.

**Required correction:** Include the interactive toast surface in the drawer's
isolation boundary, or render an intentionally non-interactive live announcement
inside the permitted layer while preserving dismiss behavior after close. Test
with the real `ToastContainer`.

### PG-057-004 - P3 - Overlay dismissal does not restore focus to the surviving opener

**Locations:**

- `frontend/src/layouts/Header.tsx:281-285`
- `frontend/src/layouts/Header.test.tsx:86-102`
- `frontend/src/layouts/AdminLayout.tsx:199-203`
- `frontend/src/layouts/AdminLayout.test.tsx:133-154`

**Evidence:** Both overlay handlers only set their open state to false. If focus
is in Header mobile search or on an ADMIN drawer link, pointer activation of the
non-focusable overlay does not move focus before the focused subtree unmounts.
Focus consequently falls to the document body instead of the exact surviving
opener. The overlay tests assert closure and `aria-expanded`, but do not assert
post-close focus.

**Reproduction / verification:** Open each surface, focus an element inside it,
click the overlay, and assert that the exact opener has focus. This assertion
fails for both layouts.

**Required correction:** Route non-navigation dismissal through a shared close
function that restores a connected, enabled opener. Keep route-change handling
separate so destination focus policy can own navigation.

## Verified Boundaries

- Header account, Login, and subscription commands retain `/profile`, `/login`,
  and `/subscriptions`; each desktop command is now one Link node with the prior
  Button visual classes.
- Header role projection is unchanged: ADMIN still excludes the public
  subscription tab and retains the existing ADMIN destinations.
- Header logout still invokes the auth-store logout action and replaces the
  route with `/`.
- ADMIN menu destinations, active-route matching, mutation-owned logout guard,
  logout invocation, and route replacement are unchanged.
- MainLayout blocks default-prevented, modified, interactive, editable,
  contenteditable, dialog, slider, and composite-control targets. Its positive
  non-interactive play/pause and previous/next paths remain covered.
- Closed Header menu, ADMIN mobile drawer, and PlayerBar detail controls are
  conditionally absent from the DOM. The ADMIN toast exception is recorded as
  PG-057-003.
- PlayerBar download gating, subscription projection, download ownership,
  download API call, and blob-trigger semantics are unchanged by this diff and
  remain mocked in the focused tests. No download was executed.

## Verification Evidence

| Check | Result | Evidence |
|-------|--------|----------|
| Focused Vitest | PASS | 6 files, 89 tests passed |
| Frontend typecheck | PASS | `npm run typecheck` |
| Scoped diff whitespace check | PASS | `git diff --check -- <reviewed tracked files>` |

Focused test command:

```text
npm test -- src/layouts/MainLayout.test.tsx src/layouts/Header.test.tsx src/layouts/AdminLayout.test.tsx src/layouts/PlayerBar.test.tsx src/components/ui/Modal.test.tsx src/test/coverage/publicAuthShell.coverage.test.tsx
```

No login, logout, payment, mail, download/export, API, database, or other
external effect was executed. Protected output and ignored secrets were not
opened. Native-browser keyboard acceptance remains owned by WI-076.
