# WI-20260308-ATS-041 Evidence Pack

## WI Summary
- **WI ID**: WI-20260308-ATS-041
- **REQ**: REQ-20260308-ATS-012
- **Agent**: se
- **Task**: Creator pages implementation (Track upload/manage/edit + Album manage)

## Files Created / Modified

### New Files (8)
| File | Type | Lines |
|------|------|-------|
| `frontend/src/pages/creator/TrackUploadPage.tsx` | Page | ~302 |
| `frontend/src/pages/creator/TrackUploadPage.module.css` | Style | ~162 |
| `frontend/src/pages/creator/TrackEditPage.tsx` | Page | ~288 |
| `frontend/src/pages/creator/TrackEditPage.module.css` | Style | ~186 |
| `frontend/src/pages/creator/AlbumManagePage.tsx` | Page | ~237 |
| `frontend/src/pages/creator/AlbumManagePage.module.css` | Style | ~192 |
| `frontend/src/pages/admin/TrackManagePage.module.css` | Style | ~241 |

### Modified Files (3)
| File | Changes |
|------|---------|
| `frontend/src/pages/admin/TrackManagePage.tsx` | Full implementation replacing placeholder (was 4 lines -> ~240 lines) |
| `frontend/src/api/tracks.ts` | Added: `AdminTrackListItem`, `AdminTrackListParams`, `fetchAdminTracks()`, `createTrack()`, `updateTrack()`, `deleteTrack()` |
| `frontend/src/api/albums.ts` | Added: `createAlbum()`, `updateAlbum()`, `deleteAlbum()`, `addTrackToAlbum()`, `removeTrackFromAlbum()` |
| `frontend/src/router/index.tsx` | Added: `AlbumManagePage` import + `/admin/albums` route |

## API Integration Summary

| Page | APIs Used | Method |
|------|-----------|--------|
| TrackUploadPage | `POST /api/tracks`, `GET /api/tags` | multipart/form-data |
| TrackEditPage | `GET /api/tracks/{id}`, `PUT /api/tracks/{id}`, `GET /api/tags` | multipart/form-data |
| TrackManagePage | `GET /api/tracks/admin`, `DELETE /api/tracks/{id}` | JSON |
| AlbumManagePage | `GET /api/albums`, `GET /api/albums/{id}`, `POST /api/albums`, `PUT /api/albums/{id}`, `DELETE /api/albums/{id}` | multipart/form-data |

## Design Decisions

1. **TrackManagePage location**: Implemented in `pages/admin/` (not `pages/creator/`) because the existing router imports from `@/pages/admin/TrackManagePage`. The API (GET /api/tracks/admin) is admin-only.

2. **AlbumManagePage**: Uses modal-based create/edit pattern (ConfirmModal for delete, custom form modal for create/edit) instead of separate pages. This provides better UX for album management which is a simpler entity than tracks.

3. **File upload pattern**: Uses `FormData` with separate `audioFile` and `thumbnail` fields per API spec requirement. The `Content-Type: multipart/form-data` header is explicitly set (Axios auto-detects but explicit is safer).

4. **Router update**: Added `/admin/albums` route for AlbumManagePage. Existing `/admin/albums/new` and `/admin/albums/:albumId/edit` routes preserved for backward compatibility.

5. **CSS Modules**: All styles use only `tokens.css` variables (--bg0, --bg1, --bg2, --bg3, --text0, --text1, --text2, --accent, --accent-dim, --accent-border, --border, --border2). No hardcoded colors except for red (#f87171) for error/danger states and green (#34d399) for success states, consistent with existing Button.module.css patterns.

## Quality Evidence

### Verification Commands Required
```bash
cd frontend
npm run typecheck   # tsc --noEmit: 0 errors expected
npm run lint        # eslint: 0 errors expected
npm run build       # vite build: success expected
```

> **Note**: Bash execution was restricted during this session. The user must run these commands manually to complete DoD verification.

### Manual Code Review Checklist
- [x] All imports resolve to existing modules
- [x] CSS Modules use only tokens.css variables
- [x] No hardcoded API URLs (uses client baseURL)
- [x] Error handling on all async operations
- [x] Loading states on all data-fetching pages
- [x] ConfirmModal used for delete operations
- [x] multipart/form-data for file uploads
- [x] No unused imports (verified by manual inspection)
- [x] Default exports only from page components (react-refresh compliant)

## Reproduction Steps

1. Run `npm run typecheck` in `frontend/` -- expect 0 errors
2. Run `npm run lint` in `frontend/` -- expect 0 errors
3. Run `npm run build` in `frontend/` -- expect success
4. Navigate to `/admin/tracks/upload` -- Track upload form should render
5. Navigate to `/admin/track-manage` -- Track list table with filter/pagination
6. Navigate to `/admin/tracks/1/edit` -- Track edit form with pre-loaded data
7. Navigate to `/admin/albums` -- Album grid with create/edit/delete modals

## Rollback
All new files can be deleted to restore previous state. Modified files (api/tracks.ts, api/albums.ts, router/index.tsx, admin/TrackManagePage.tsx) can be reverted via git.

## Blocks
- WI-20260308-ATS-043 (typecheck)
- WI-20260308-ATS-044 (eslint)
- WI-20260308-ATS-045 (build-check)
