---
version: 2.1
last_updated: 2026-07-17
project: ATS
owner: docops
category: reference
status: stable
dependencies:
  - path: ../../frontend/src/components/ui/Modal.tsx
    reason: Shared modal implementation
  - path: ../../frontend/src/pages/subscriber/PlaylistListPage.tsx
    reason: Playlist creation modal entry point
  - path: atstudio-front-list.md
    reason: Screen count contract
---

# ATStudio Modal Interaction Inventory

## Current Count Unit

The production frontend contains **23 `<Modal>` render occurrences across 17 non-test TSX files**. This is an implementation-occurrence count, not a count of simultaneously visible dialogs or distinct business workflows.

| Area | Files | Occurrences |
|---|---:|---:|
| Shared wrappers/components | 4 | 4 |
| Admin pages | 6 | 9 |
| Creator pages | 1 | 2 |
| Subscriber pages | 6 | 8 |
| **Total** | **17** | **23** |

Shared wrappers are `TagFilterModal`, `AddToPlaylistModal`, `HistoryModal`, and `ConfirmDialog`. Pages with multiple render occurrences include company certification review, album management, tag management, user subscription management, playlist edit, and playlist list.

## Interaction Rules

- The shared modal provides accessible dialog semantics and a stable close path.
- Destructive operations require explicit confirmation; payment refund/correction execution also uses typed confirmation.
- Loading state disables duplicate submission.
- Closing or completing a modal resets transient form state.
- Reopened playlist create/edit modals refresh current limits and clear stale errors.
- Async list/detail modals must ignore superseded responses.
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
