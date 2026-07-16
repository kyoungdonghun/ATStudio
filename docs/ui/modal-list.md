---
version: 2.0
last_updated: 2026-07-16
project: ATS
owner: docops
category: reference
status: stable
dependencies:
  - path: ../../frontend/src/components/ui/Modal.tsx
    reason: Shared modal implementation
  - path: ../../frontend/src/router/index.tsx
    reason: Route adapter boundary
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

## Route Adapter

`/playlists/new` renders `PlaylistCreatePage`, which opens the existing playlist-create modal and returns to `/playlists` when closed. It is counted as a route-level lazy component but not as a second distinct visual screen.

## Verification Command

```powershell
$files = rg -l "<Modal\b" frontend/src -g "*.tsx" -g "!*.test.tsx"
$files.Count
$files | ForEach-Object { rg -o "<Modal\b" $_ } | Measure-Object
```

Re-run this count when modal markup or wrappers change. Do not compare it directly with historical M-xx design IDs; those were planning identifiers rather than current render occurrences.
