# WI-20260308-ATS-037 Evidence Pack

## Header
- **WI ID**: WI-20260308-ATS-037
- **REQ**: REQ-20260308-ATS-012
- **Agent**: se
- **Status**: COMPLETED
- **Date**: 2026-03-08

---

## Changes Summary

### New Files Created

| File | Purpose |
|------|---------|
| `frontend/src/pages/public/HomePage.tsx` | Home page: Hero, new albums carousel, popular albums grid, genre tag explorer, footer |
| `frontend/src/pages/public/HomePage.module.css` | CSS Module for HomePage |
| `frontend/src/pages/public/TrackListPage.tsx` | Track list: filter bar (genre/mood/BPM), sort, track table, pagination |
| `frontend/src/pages/public/TrackListPage.module.css` | CSS Module for TrackListPage |
| `frontend/src/pages/public/AlbumListPage.tsx` | Album list (list type L-2): table view with pagination |
| `frontend/src/pages/public/AlbumListPage.module.css` | CSS Module for AlbumListPage |
| `frontend/src/pages/public/AlbumListImagePage.module.css` | CSS Module for AlbumListImagePage (L-1) |
| `frontend/src/api/tags.ts` | Tag API module: `fetchTags(type?)` |

### Modified Files

| File | Change |
|------|--------|
| `frontend/src/types/index.ts` | Updated `PagedResponse` to match backend (`dataList` + `pageInfo`), added `PageInfo`, `TagType`, `TrackListItem`, updated `Album` (`thumbnailUrl`), updated `Track` (`thumbnail`, `tonality`) |
| `frontend/src/api/tracks.ts` | Added `fetchTracks()` with filter params, kept existing `fetchTrackDetail()` |
| `frontend/src/api/albums.ts` | Added `fetchAlbums()` with pagination params, kept existing `fetchAlbumDetail()` |
| `frontend/src/components/album/AlbumCard.tsx` | `coverImageUrl` -> `thumbnailUrl` (type alignment) |
| `frontend/src/components/track/TrackRow.tsx` | Refactored to use `TrackListItem`, extract genre/mood from `tags[]`, use `thumbnail` and `tonality` |
| `frontend/src/layouts/PlayerBar.tsx` | `coverImageUrl` -> `thumbnail` (type alignment) |
| `frontend/src/pages/public/AlbumListImagePage.tsx` | Full implementation: album card grid with pagination, view toggle |
| `frontend/src/router/index.tsx` | Wrapped all routes in `MainLayout` (Header + Outlet + PlayerBar), added `MainLayout` import |

---

## Acceptance Criteria Verification

### Functional

- [x] **HomePage**: Hero section (badge/title/desc/CTA buttons/album stacks), new album carousel (7 items), popular album 6-column grid, genre tag filter
- [x] **HomePage**: `GET /api/albums` (latest 7 carousel) + `GET /api/albums?sort=popular` (6 grid) + `GET /api/tags?type=GENRE`
- [x] **TrackListPage**: Genre/mood/BPM filter chips, sort dropdown (latest/popular), TrackRow table, pagination
- [x] **TrackListPage**: `GET /api/tracks?page&size&genre&mood&bpmMin&bpmMax&sort` integration
- [x] **AlbumListPage**: Album table (list view L-2) + AlbumListImagePage (card grid L-1), pagination
- [x] **AlbumListPage**: `GET /api/albums?page&size` integration
- [x] **Loading**: Skeleton placeholders for HomePage carousel/grid, "Loading..." text for list pages
- [x] **Error**: Error message displayed on API failure

### Quality

- [x] `npm run typecheck` (tsc --noEmit): 0 errors
- [x] `npm run lint` (eslint --max-warnings 0): 0 errors
- [x] `npm run build` (tsc -b && vite build): success, 992ms, dist/ generated

---

## Design Decisions

1. **Type alignment**: Updated `PagedResponse<T>` to match actual backend format (`dataList` + `pageInfo` with `total/start/end/prev/next`) instead of old Spring-style (`data[]` + `totalElements/totalPages`)
2. **TrackListItem vs Track**: Separated list item type (`TrackListItem`) from detail type (`Track`) since the list API returns a subset of fields
3. **Album thumbnailUrl**: Aligned with API response field name (`thumbnailUrl` not `coverImageUrl`)
4. **URL-driven filters**: TrackListPage uses `useSearchParams` for filter state, enabling deep linking and back/forward navigation
5. **MainLayout wrapper**: Added router-level layout wrapper to ensure Header/PlayerBar appear on all pages (was missing)
6. **Reused components**: AlbumCard, TrackRow, Button, Badge, Tag, FilterChip all reused from WI-036

---

## Reproduction Steps

```bash
# TypeScript check
cd frontend && node node_modules/typescript/bin/tsc --noEmit

# ESLint check
node node_modules/eslint/bin/eslint.js src --ext .ts,.tsx --max-warnings 0

# Build
node node_modules/typescript/bin/tsc -b && node node_modules/vite/bin/vite.js build
```

---

## Rollback

All changes are additive. Rollback by reverting the modified files and deleting new files.
