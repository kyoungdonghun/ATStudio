---
version: 1.0
last_updated: 2026-08-14
project: ATS
owner: pg
category: wi-review
status: completed
dependencies:
  - path: WI-20260809-ATS-057-handoff.md
    reason: Review scope, acceptance contract, and external-effect boundary
  - path: WI-20260809-ATS-057-pg-review.md
    reason: R1 security and interaction-layer findings
  - path: WI-20260809-ATS-057-pg-r2-review.md
    reason: R2 finding closure baseline
  - path: WI-20260809-ATS-057-qa-fe-r4-review.md
    reason: Latest matchMedia, navigation-focus, and StrictMode correction evidence
  - path: ../../docs/policies/security-policy.md
    reason: Authentication, authorization, protected-download, and sensitive-state baseline
  - path: ../../docs/policies/access-control-policy.md
    reason: Least-privilege, separation-of-duties, and default-deny baseline
---

# WI-20260809-ATS-057 Independent PG R3 Final Review

## Decision

**PG security and authorization decision: PASS**

| Severity | Open PG findings |
|----------|-----------------:|
| P0 | 0 |
| P1 | 0 |
| P2 | 0 |
| P3 | 0 |
| **Total open PG P0-P3** | **0** |

All four R1 PG findings remain CLOSED after the latest responsive viewport and
StrictMode-safe navigation-focus corrections. No new P0-P3 authentication,
authorization, session, protected-action, or sensitive-state finding was
identified in the current diff.

`QA-FE-057-003` remains an independently reported OPEN P3 documentation finding.
It is outside the PG finding count, so the PG R3 decision is PASS while the
overall WI-057 decision remains FAIL pending DocOps synchronization.

## Existing PG Finding Disposition

| Finding | R1 severity | R3 status | Final disposition |
|---------|-------------|-----------|-------------------|
| `PG-057-001` | P2 | **CLOSED, not reopened** | PlayerBar Escape remains scoped to its mobile root and rejects dialog-owned targets; real Header and shared Modal ordering regressions pass. |
| `PG-057-002` | P2 | **CLOSED, not reopened** | Invalid nested targets still restore into the highest surviving Modal before page-main fallback, including a busy parent. |
| `PG-057-003` | P2 | **CLOSED, not reopened** | The real ADMIN Toast control remains inside the drawer's `inert`, `aria-hidden`, and pointer-isolation boundary, including responsive release. |
| `PG-057-004` | P3 | **CLOSED, not reopened** | Escape and overlay dismissals restore the valid opener; accepted navigation uses a separate non-restoring destination-focus path. |

### PG-057-001 - CLOSED - Escape ownership remains layered

`PlayerBar` handles Escape only through its mobile root, requires a target within
that root, and rejects `dialog`, `[role="dialog"]`, and `[aria-modal="true"]`
targets (`frontend/src/layouts/PlayerBar.tsx:90-100,576,869`). It installs no
document-level Escape listener. Header events originate in a sibling surface,
and Modal portal events remain dialog-owned.

The real Header plus PlayerBar and actual shared Modal regressions pass for one
Header Escape, a non-busy Modal, and a busy Modal
(`frontend/src/layouts/PlayerBar.test.tsx:437-478`). The latest navigation-focus
effects do not add keyboard listeners or change this event path.

### PG-057-002 - CLOSED - Modal stack restoration remains intact

The Modal stack still stores live dialog getters. Cleanup tries the exact
opener, explicit fallback, highest remaining Modal, then page main
(`frontend/src/components/ui/Modal.tsx:44-50,153-167`). Removed, disabled,
`aria-disabled`, hidden, and inert child targets, including a busy surviving
parent, remain covered in `frontend/src/components/ui/Modal.test.tsx:229-253`.

Neither `matchMedia` nor `navigationFocus` is imported by Modal, and the current
Modal production diff contains no authentication, route, API, or durable-state
operation.

### PG-057-003 - CLOSED - ADMIN Toast isolation survives viewport changes

`mobileDrawerOpen` requires `sidebarOpen` and a viewport that is not known to be
desktop. The media-query change handler closes without opener restoration when
the viewport leaves mobile (`frontend/src/layouts/AdminLayout.tsx:109-112,
153,171-188`). The same open-state isolation props cover topbar content, main,
and the real Toast boundary; Toast pointer activation is also blocked by CSS
(`frontend/src/layouts/AdminLayout.tsx:262,304,324,328-330` and
`frontend/src/layouts/AdminLayout.module.css:197-199`).

The responsive regression proves drawer/overlay removal, release of every
isolation boundary, removal of the document Tab trap, and preservation of the
desktop sidebar and active destination
(`frontend/src/layouts/AdminLayout.test.tsx:204-243`). The real Toast dismiss
control isolation and post-close restoration test also passes.

### PG-057-004 - CLOSED - Dismissal and navigation focus remain separate

Header Escape and overlay paths call `closeMobileMenu(true)`. ADMIN Escape and
overlay paths call `closeSidebar(true)`. Route activation calls the corresponding
non-restoring close path and requests one destination-focus intent
(`frontend/src/layouts/Header.tsx:117-180,321-325` and
`frontend/src/layouts/AdminLayout.tsx:138-151,200-204,237-254,271-283`). Route
change effects do not restore stale openers.

The exact-opener, different-path, same-path, Header-to-ADMIN, and
ADMIN-to-public regressions pass. The latest synchronous one-shot consumption
therefore does not reopen the overlay-focus defect.

## Authentication and Authorization Regression Review

### Header logout and ADMIN projection

- Header mobile logout still closes the menu, calls the existing auth-store
  `logout()`, and navigates to `/` with `{ replace: true }`
  (`frontend/src/layouts/Header.tsx:387-394`). The added focus intent neither
  awaits nor replaces the logout operation and does not change its session-clear
  or history-replacement meaning.
- Desktop Header logout retains the same `logout()` plus replacement navigation
  sequence (`frontend/src/layouts/Header.tsx:256-265`).
- ADMIN Header routes remain `/admin/questions` and `/admin/dashboard`, projected
  only for an authenticated ADMIN. The public subscription tab remains excluded
  for ADMIN (`frontend/src/layouts/Header.tsx:35-38,104-112`).
- The Header logout and reduced ADMIN projection regression passes in
  `frontend/src/test/coverage/shellCatalogRouterGaps.coverage.test.tsx:606-638`.

### ADMIN mutation guard and route boundary

- `handleLogout()` still fails closed when any mutation owner is active, then
  otherwise invokes `logout()` and replacement navigation to `/`
  (`frontend/src/layouts/AdminLayout.tsx:115-136,256-260`). The guard is checked
  in the handler as well as represented by the disabled control.
- The regression forcibly removes the DOM `disabled` attribute while a mutation
  owner remains active and proves that neither logout nor navigation occurs;
  normal logout resumes after release
  (`frontend/src/layouts/AdminLayout.test.tsx:304-320`).
- ADMIN menu destinations and the footer public destination `/` are unchanged.
  The mobile footer requests focus only for a normal accepted Router activation;
  it does not clear role/session state or change the destination.
- Router, `ProtectedRoute`, `SubscriberRoute`, auth-store, API, and backend files
  have no current diff. The existing ADMIN route remains wrapped by
  `ProtectedRoute minRole="ADMIN"`; focus is attempted only after a destination
  layout has mounted and cannot authorize a route.

## One-Shot Navigation Intent Review

`frontend/src/utils/navigationFocus.ts:1-53` has no imports. Its module-level
state is one boolean. It contains no user ID, role, token, session generation,
route payload, URL, payment/download identifier, or other sensitive value. It
does not access local/session storage, network/API clients, application stores,
history state, or any durable persistence. It only finds an available `main` or
H1, temporarily adds `tabindex="-1"`, calls `focus()`, and removes the temporary
attribute.

Consumption clears the boolean synchronously before attempting focus. Under the
real `StrictMode` wrapper, effect replay therefore sees an already consumed
intent and has no scheduled frame or cleanup callback to cancel. The
cross-layout tests deliberately provide a non-executing `requestAnimationFrame`
mock and prove that it is never called
(`frontend/src/layouts/navigationFocus.crossLayout.test.tsx:65-155`).

If no destination exists, the intent is still consumed once and existing focus
is preserved. A later main region and unrelated route cannot reuse it
(`frontend/src/utils/navigationFocus.test.ts:44-60` and
`frontend/src/layouts/navigationFocus.crossLayout.test.tsx:138-155`).

Header and ADMIN mobile Link handlers return before the sole request edge for a
default-prevented, non-primary, Alt, Ctrl, Meta, or Shift activation
(`frontend/src/layouts/Header.tsx:168-180` and
`frontend/src/layouts/AdminLayout.tsx:242-254`). Rejected activation therefore
creates neither focus intent nor application navigation state. Non-navigation
dismissals and the ADMIN mobile-to-desktop release also have no request edge.

## Preserved Adjacent Boundaries

- **MainLayout shortcuts:** default-prevented, modified, interactive, editable,
  contenteditable, dialog, slider, tabbable, and composite-control targets are
  rejected before player-store access. Positive ordinary-content play/pause and
  previous/next behavior remains covered.
- **PlayerBar subscription/download:** `fetchMySubscription`, role projection,
  structured subscription failures, `downloadTrack`, ownership fencing,
  `triggerBlobDownload`, and subscription routing are unchanged. Detail controls
  are conditionally absent only while collapsed. Focused tests mock every API
  and blob action; no download was executed.
- **ADMIN Toast:** the real dismiss control is isolated while the mobile drawer
  is open and becomes interactive again after close. The opener remains outside
  the isolated roots.
- **Modal stack:** topmost Escape, busy close blocking, focus trap, nested order,
  connected opener restoration, invalid-target fallback, and unmount restoration
  remain covered.

## New P0-P3 Findings

None.

## Verification Evidence

| Check | Result | Evidence |
|-------|--------|----------|
| Focused Vitest | PASS | 9 files, 135 tests passed |
| StrictMode cross-layout regressions | PASS | Main-to-ADMIN H1, ADMIN-to-lazy-public main, and one-shot non-reuse; `requestAnimationFrame` unused |
| Frontend typecheck | PASS | `npm run typecheck` |
| Tracked diff whitespace check | PASS | `git diff --check` |
| Auth/router/API diff review | PASS | No diff in router, auth store, API, backend, schema, or durable-state modules |

Focused command:

```text
npm test -- src/layouts/MainLayout.test.tsx src/layouts/Header.test.tsx src/layouts/AdminLayout.test.tsx src/layouts/PlayerBar.test.tsx src/components/ui/Modal.test.tsx src/utils/navigationFocus.test.ts src/layouts/navigationFocus.crossLayout.test.tsx src/test/coverage/publicAuthShell.coverage.test.tsx src/test/coverage/shellCatalogRouterGaps.coverage.test.tsx
```

No real login, logout, API, payment, Provider, mail, download/export, database,
or other external effect was executed. Protected output and ignored secrets were
not opened or modified. Nothing was staged, committed, or pushed. Native browser
keyboard and pointer acceptance remains owned by WI-076.

## Final Status

- **PG R3 security/authorization:** PASS, 0 open P0-P3 findings.
- **Existing PG findings:** `PG-057-001` through `PG-057-004` CLOSED and not reopened.
- **New PG findings:** none.
- **Overall WI-057:** FAIL only because `QA-FE-057-003` remains OPEN P3 as a
  documentation-only finding pending DocOps.
