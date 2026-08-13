---
version: 2.7
last_updated: 2026-08-13
project: ATS
owner: docops
category: reference
status: stable
dependencies:
  - path: ../../frontend/src/components/ui/Modal.tsx
    reason: Shared modal implementation
  - path: ../../frontend/src/pages/subscriber/PlaylistListPage.tsx
    reason: Playlist creation modal entry point
  - path: ../../frontend/src/pages/creator/AlbumManagePage.tsx
    reason: Album create and edit modal ownership
  - path: atstudio-front-list.md
    reason: Screen count contract
---

# ATStudio Modal Interaction Inventory

## Current Count Unit

The production frontend contains **22 `<Modal>` render occurrences across 17 non-test TSX files**. This is an implementation-occurrence count, not a count of simultaneously visible dialogs or distinct business workflows.

| Area                       |  Files | Occurrences |
| -------------------------- | -----: | ----------: |
| Shared wrappers/components |      4 |           4 |
| Admin pages                |      6 |           8 |
| Creator pages              |      1 |           2 |
| Subscriber pages           |      6 |           8 |
| **Total**                  | **17** |      **22** |

Shared wrappers are `TagFilterModal`, `AddToPlaylistModal`, `HistoryModal`, and
`ConfirmDialog`. Pages with multiple render occurrences include company
certification review, album management, tag management, playlist edit, and
playlist list. User subscription management now owns one
`UserSubscriptionCorrectionModal` render occurrence plus a nested shared typed
confirmation; the removed direct update/cancel dialogs are not current flows.

## Interaction Rules

- The shared modal provides accessible dialog semantics and a stable close path.
- Destructive operations require explicit confirmation; payment refund/correction execution also uses typed confirmation.
- Loading state disables duplicate submission.
- Closing or completing a modal resets transient form state.
- Reopened playlist create/edit modals refresh current limits and clear stale errors.
- Async list/detail modals must ignore superseded responses.
- The Album edit modal clears title, description, thumbnail, error, and target
  state before each detail read. Only the active Album generation may enable or
  populate the form; close, retry, and target switch retire earlier responses.
- Tag save/delete errors remain modal-local and preserve list/filter/input state.
- The shared modal's optional busy contract exposes `aria-busy`, disables and
  removes the header close action from focus, and suppresses Escape and backdrop
  close through the same state. Notice deletion uses this contract while its
  owned operation is pending.
- An authoritative Notice-delete rejection remains inside the recovered modal
  for an explicit retry without changing the edit form. An ambiguous outcome
  closes the modal, disables another DELETE, and moves to the page-level
  observation-only recovery state.
- The subscription-correction modal resumes persisted open workflow state and
  fences superseded open/preview responses. HTTP 4xx mutation responses retain
  their stable error without reconciliation. Network/timeout/no-response and
  HTTP 5xx failures trigger one bounded state read: request uses the subscription
  open-state endpoint, while approval and execution use correction detail. A
  failed read or request 204 keeps the draft, preview, notes, and known ID,
  blocks duplicate mutation, and exposes one read-only status-retry action.
  Repeated request 204 remains unknown. Browser date bounds do not block server
  preview, and normalized persisted text is shown at preview or confirmation.
  Execute requires typed confirmation.
- Modal labels and buttons must describe the action, not implementation details.

## Playlist Creation Entry Point

The `/playlists` page opens its creation modal from the visible create button or
create card. Playlist creation is a modal interaction owned by
`PlaylistListPage`; it is not a separate route-level page or lazy component.

## Verification Command

```powershell
$files = rg -l "<Modal\b" frontend/src -g "*.tsx" -g "!*.test.tsx"
$files.Count
$files | ForEach-Object { rg -o "<Modal\b" $_ } | Measure-Object
```

Re-run this count when modal markup or wrappers change. Do not compare it directly with historical M-xx design IDs; those were planning identifiers rather than current render occurrences.
