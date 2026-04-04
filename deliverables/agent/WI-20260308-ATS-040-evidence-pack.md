# WI-20260308-ATS-040 Evidence Pack

## WI Summary
- **WI ID**: WI-20260308-ATS-040
- **REQ**: REQ-20260308-ATS-012
- **Agent**: se (Software Engineer)
- **Scope**: Subscriber pages implementation (PlaylistListPage, PlaylistDetailPage, LicenseListPage, LicenseDetailPage, ProfilePage, SubscriptionManagePage)

---

## Files Created / Modified

### API Layer (3 new files)
| File | Lines | Description |
|------|-------|-------------|
| `frontend/src/api/playlists.ts` | 86 | Playlist CRUD API (fetchMyPlaylists, fetchPlaylistDetail, createPlaylist, deletePlaylist, addTrackToPlaylist, removeTrackFromPlaylist) |
| `frontend/src/api/licenses.ts` | 52 | License API (fetchMyLicenses, fetchLicenseDetail) |
| `frontend/src/api/userSubscriptions.ts` | 82 | User subscription API (fetchMySubscription, changeMySubscription, cancelMySubscription, fetchSubscriptionChangePreview) |

### Pages (6 pages updated from stubs + 6 CSS Modules)
| File | Lines | Description |
|------|-------|-------------|
| `frontend/src/pages/subscriber/PlaylistListPage.tsx` | ~210 | Playlist card grid, 3-limit enforcement, create/delete modals |
| `frontend/src/pages/subscriber/PlaylistListPage.module.css` | ~310 | Card grid, mosaic thumb, plan notice bar, modal form |
| `frontend/src/pages/subscriber/PlaylistDetailPage.tsx` | ~150 | Playlist detail with track table, remove track modal |
| `frontend/src/pages/subscriber/PlaylistDetailPage.module.css` | ~140 | Track table, back link, action buttons |
| `frontend/src/pages/subscriber/LicenseListPage.tsx` | ~140 | License table with pagination |
| `frontend/src/pages/subscriber/LicenseListPage.module.css` | ~140 | Table, pagination, code display |
| `frontend/src/pages/subscriber/LicenseDetailPage.tsx` | ~85 | License detail card (code, track, owner) |
| `frontend/src/pages/subscriber/LicenseDetailPage.module.css` | ~80 | Detail card, info rows |
| `frontend/src/pages/subscriber/ProfilePage.tsx` | ~190 | Account info, nickname edit, password change |
| `frontend/src/pages/subscriber/ProfilePage.module.css` | ~150 | Section cards, form inputs, danger zone |
| `frontend/src/pages/subscriber/SubscriptionManagePage.tsx` | ~260 | Current plan display, upgrade/downgrade with preview, cancel with grace period |
| `frontend/src/pages/subscriber/SubscriptionManagePage.module.css` | ~280 | Plan card, status badges, plan grid, preview box, cancel section |

---

## Acceptance Criteria Verification

### Functional
- [x] PlaylistListPage: card grid with mosaic thumbnails, count display (N / 3)
- [x] PlaylistListPage: "New Playlist" button hidden when count >= 3 (canCreate = count < MAX_PLAYLISTS)
- [x] PlaylistListPage: GET /api/playlists (fetchMyPlaylists), POST /api/playlists (createPlaylist)
- [x] PlaylistListPage: Delete playlist with confirmation modal
- [x] PlaylistDetailPage: playlist title + track count, track table with order/title/BPM/key
- [x] PlaylistDetailPage: GET /api/playlists/{playlistId}, DELETE /api/playlists/{playlistId}/tracks/{trackId}
- [x] LicenseListPage: license table (track title, license code, issued date) with pagination
- [x] LicenseListPage: GET /api/licenses/me with page/size params
- [x] LicenseDetailPage: GET /api/licenses/{licenseId} with full detail
- [x] ProfilePage: account info display (email, userType, job, createdAt)
- [x] ProfilePage: nickname edit (PUT /api/users/me), password change (PUT /api/users/me/password)
- [x] SubscriptionManagePage: current plan display with status badge (ACTIVE/CANCELLED/EXPIRED)
- [x] SubscriptionManagePage: GET /api/user-subscriptions/me
- [x] SubscriptionManagePage: plan change with preview (GET /api/utils/subscription-change-preview)
- [x] SubscriptionManagePage: upgrade (immediate) / downgrade (pending) semantics displayed
- [x] SubscriptionManagePage: cancel with grace period notice (DELETE /api/user-subscriptions/me)

### Quality
- [ ] npm run lint 0 errors -- **PENDING** (Bash permission required)
- [ ] npm run typecheck 0 errors -- **PENDING** (Bash permission required)
- [ ] npm run build success -- **PENDING** (Bash permission required)

---

## Design Decisions

1. **PurchaseHistoryPage**: Not created as a separate page. The router has no route for it, and the License list (`/licenses`) effectively serves as purchase history since licenses are auto-issued on download. The handoff mentions it but the existing router structure does not include it.

2. **Existing stub mapping**: Used existing file names from router imports rather than handoff names:
   - LicensePage -> `LicenseListPage.tsx` (matches router import)
   - MyAccountPage -> `ProfilePage.tsx` (matches router import)

3. **CSS composes removed**: Inlined all styles instead of using CSS Modules `composes` to avoid potential build compatibility issues.

4. **3-playlist limit**: Implemented as `canCreate = count < MAX_PLAYLISTS` controlling both the header button and the add-new card visibility. The plan notice bar shows a progress bar with percentage fill.

5. **Subscription change preview**: Uses the `GET /api/utils/subscription-change-preview` endpoint (api-spec 14.8) to show financial impact before confirming a plan change.

---

## Reproduction Steps

```bash
cd frontend
npm run typecheck   # Verify TypeScript compilation
npm run lint        # Verify ESLint 0 errors
npm run build       # Verify production build
npm run dev         # Start dev server, navigate to subscriber pages
```

### Manual Verification Routes
- `/playlists/list` -- Playlist list (login required)
- `/playlists/:id` -- Playlist detail
- `/licenses` -- License list
- `/licenses/:id` -- License detail
- `/profile` -- My account
- `/subscriptions/manage` -- Subscription management

---

## Traceability

| Item | Reference |
|------|-----------|
| REQ | REQ-20260308-ATS-012 |
| WI | WI-20260308-ATS-040 |
| API Spec | docs/design/api-spec.md (Section 3, 5, 6, 7, 14.8) |
| Mockup | docs/ui/mockup/playlist.html |
| Rollback | Delete created/modified files; stubs can be restored from git |
