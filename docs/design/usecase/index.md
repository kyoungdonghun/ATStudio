# Use Case Specification Index

> **Version**: v4 (Confirmed)
> **Confirmed date**: 2026-02-20
> **Reference documents**: `docs/design/db-schema.md` (v4), `docs/design/api-spec.md` (v5)
> **Source**: `docs/check/usecase-spec csv/`

---

## File List

| File | Category | UC Count |
|------|----------|----------|
| `sound-track.md` | Track (create/list/detail/update/delete/play/download) | 7 |
| `sound-tag.md` | Tag (create/list/update/delete) | 4 |
| `sound-playlist.md` | Playlist (create/list/detail/update/delete/add track/remove track) | 7 |
| `sound-playhistory.md` | Play history (save/list/delete) | 3 |
| `user-info.md` | User info (register/login/social login/social profile completion/view/update/withdraw) | 10 |
| `user-subscription.md` | Subscription (subscribe/list/change/cancel/admin management) | 10 |
| `user-license.md` | Track usage license (view) | 4 |
| `user-question.md` | Inquiry (create/list/answer/delete/attachment/admin status change) | 7 |
| `user-notice.md` | Notice (create/list/update/delete) | 5 |
| `likes.md` | Likes (add/list/remove) | 3 |
| `download-queue.md` | Download queue (add/list/remove) | 3 |
| `whitelist.md` | Whitelist channel (register/list/update/delete) | 4 |
| `company-certification.md` | Company certification (apply/view status/admin management) | 5 |
| `util.md` | Utility (duplicate check/token/subscription status/download count, etc.) | 7 |

**Total UC count: 79** (net +2 vs v3: +3 added, -1 removed)

---

## Full UC Code List

### Sound

| Code | Title | File |
|------|-------|------|
| SOUND-001 | Create track | `sound-track.md` |
| SOUND-002 | Create playlist | `sound-playlist.md` |
| SOUND-003 | Create tag | `sound-tag.md` |
| SOUND-004 | Save play history | `sound-playhistory.md` |
| SOUND-005 | List tracks | `sound-track.md` |
| SOUND-006 | View track detail | `sound-track.md` |
| SOUND-007 | List playlists | `sound-playlist.md` |
| SOUND-008 | View playlist detail | `sound-playlist.md` |
| SOUND-009 | View play history | `sound-playhistory.md` |
| SOUND-010 | Play track | `sound-track.md` |
| SOUND-011 | Download track | `sound-track.md` |
| SOUND-012 | Update track | `sound-track.md` |
| SOUND-013 | Update playlist | `sound-playlist.md` |
| SOUND-014 | Update tag | `sound-tag.md` |
| SOUND-015 | Delete play history | `sound-playhistory.md` |
| SOUND-016 | Delete track | `sound-track.md` |
| SOUND-017 | Delete playlist | `sound-playlist.md` |
| SOUND-018 | Delete tag | `sound-tag.md` |
| SOUND-019 | Add track to playlist | `sound-playlist.md` |
| SOUND-020 | Remove track from playlist | `sound-playlist.md` |

### User Info

| Code | Title | File |
|------|-------|------|
| INFO-001 | Register | `user-info.md` |
| INFO-002 | View my info | `user-info.md` |
| INFO-003 | List members (Admin) | `user-info.md` |
| INFO-004 | View member detail (Admin) | `user-info.md` |
| INFO-005 | Update my info | `user-info.md` |
| INFO-006 | Update member info (Admin) | `user-info.md` |
| INFO-007 | Withdraw account | `user-info.md` |
| INFO-008 | Login | `user-info.md` |
| INFO-009 | View my license list | `user-license.md` |
| INFO-010 | View member license list (Admin) | `user-license.md` |
| INFO-011 | View my license detail | `user-license.md` |
| INFO-012 | View member license detail (Admin) | `user-license.md` |
| INFO-013 | Social login | `user-info.md` |
| INFO-014 | Complete social profile | `user-info.md` |

### Payment / Subscription

| Code | Title | File |
|------|-------|------|
| PAYMENT-001 | Subscribe | `user-subscription.md` |
| PAYMENT-002 | List subscription plans | `user-subscription.md` |
| PAYMENT-003 | View subscription plan detail | `user-subscription.md` |
| PAYMENT-004 | List member subscriptions (Admin) | `user-subscription.md` |
| PAYMENT-005 | View member subscription detail (Admin) | `user-subscription.md` |
| PAYMENT-006 | View my subscription | `user-subscription.md` |
| PAYMENT-007 | Change my subscription | `user-subscription.md` |
| PAYMENT-008 | Update member subscription (Admin) | `user-subscription.md` |
| PAYMENT-009 | Delete/cancel member subscription (Admin) | `user-subscription.md` |
| PAYMENT-010 | Cancel my subscription | `user-subscription.md` |

### Question / Notice

| Code | Title | File |
|------|-------|------|
| QUESTION-001 | Create inquiry | `user-question.md` |
| QUESTION-002 | Write answer | `user-question.md` |
| QUESTION-003 | List inquiries | `user-question.md` |
| QUESTION-004 | View inquiry detail | `user-question.md` |
| QUESTION-005 | Download attachment | `user-question.md` |
| QUESTION-006 | Delete inquiry | `user-question.md` |
| QUESTION-007 | Change inquiry status (Admin) | `user-question.md` |
| ANNOUNCE-001 | Create notice | `user-notice.md` |
| ANNOUNCE-002 | List notices | `user-notice.md` |
| ANNOUNCE-003 | View notice detail | `user-notice.md` |
| ANNOUNCE-004 | Update notice | `user-notice.md` |
| ANNOUNCE-005 | Delete notice | `user-notice.md` |

### Likes / Download Queue / Whitelist

| Code | Title | File |
|------|-------|------|
| LIKE-001 | Add to likes | `likes.md` |
| LIKE-002 | List likes | `likes.md` |
| LIKE-003 | Remove from likes | `likes.md` |
| DLQ-001 | Add to download queue | `download-queue.md` |
| DLQ-002 | View download queue | `download-queue.md` |
| DLQ-003 | Remove from download queue | `download-queue.md` |
| WL-001 | Register channel | `whitelist.md` |
| WL-002 | List my channels | `whitelist.md` |
| WL-003 | Update channel | `whitelist.md` |
| WL-004 | Delete channel | `whitelist.md` |

### Company Certification

| Code | Title | File |
|------|-------|------|
| CC-001 | Apply for company certification | `company-certification.md` |
| CC-002 | View my application status | `company-certification.md` |
| CC-003 | List applications (Admin) | `company-certification.md` |
| CC-004 | View application detail (Admin) | `company-certification.md` |
| CC-005 | Process review (Admin) | `company-certification.md` |

### Util

| Code | Title | File |
|------|-------|------|
| UTIL-002 | Check email duplicate | `util.md` |
| UTIL-003 | Check phone duplicate | `util.md` |
| UTIL-004 | Reissue token | `util.md` |
| UTIL-005 | Check subscription tier | `util.md` |
| UTIL-006 | Check download count | `util.md` |
| UTIL-007 | Check member type | `util.md` |
| UTIL-012 | Check nickname duplicate | `util.md` |

> New in v3 (vs original)
> New in v4 (cross-review additions)

---

## Change History (v3 to v4)

### UC v4 Modifications (cross-review confirmed)

| # | Field | Value |
|---|-------|-------|
| 1 | INFO-013 Social login | Added isProfileComplete derived field. New sign-ups branch to 2-step profile completion flow. See INFO-014. |
| 2 | INFO-014 Social profile completion | **New** -- `PUT /api/users/me/complete-profile`. Required profile input for first-time social sign-ups. |
| 3 | SOUND-010 Play track | Removed play_histories recording from stream API. Changed to frontend calling SOUND-004 separately. |
| 4 | SOUND-004 Save play history | Trigger changed: "automatic inside SOUND-010" to "frontend explicitly calls when QueBar playback starts". |
| 5 | SOUND-002/007/008/013/017/019/020 | Added "has active subscription (ACTIVE)" to preconditions. All playlist features confirmed subscriber-only. |
| 6 | QUESTION-006 edit removed | Inquiry edit feature removed. No-edit policy -- guide users to delete and rewrite. |
| 7 | QUESTION-006 delete (renumbered) | Former QUESTION-007 (delete) renumbered to QUESTION-006 (delete). |
| 8 | QUESTION-007 status change | **New** -- Admin inquiry status change UC (maps to existing API 8.7 to v4 API 8.6). |
| 9 | PAYMENT-010 Cancel my subscription | **New** -- `DELETE /api/user-subscriptions/me`. Member directly cancels subscription. |
| 10 | BL-001 precondition updated | "No PENDING or APPROVED" to "No PENDING/APPROVED; reapplication allowed after REJECTED/REVISION_REQUESTED". |

### Removed UC (v3 to v4)

| Original Code | Reason |
|---------------|--------|
| QUESTION-006 (edit inquiry) | Inquiry edit feature removed. Frontend shows no-edit notice and guides to delete and rewrite. |

---

## Change History (original to v3)

### Major modifications from original

| # | Field | Value |
|---|-------|-------|
| 1 | INFO-001 | Fixed duplicate flow numbers, added job/userType fields, added nickname duplicate check step |
| 2 | INFO-005 | Specified editable fields (nickname/phonePersonal/phoneCompany/job), marked email/userType as immutable |
| 3 | INFO-007 | Changed to soft delete (is_deleted=1), added password re-confirmation |
| 4 | INFO-009 | Fixed description error ("specific member's" to "own"), removed active subscription precondition |
| 5 | INFO-011 | Fixed code typo (IFNO-011 to INFO-011) |
| 6 | SOUND-001 | Updated file upload flow (multipart sent directly to backend), added async preview_file generation |
| 7 | SOUND-002 | Fixed actor (admin/artist to User (Member)) |
| 8 | SOUND-004/009/015 | Renamed "playlog" to "play history (play_histories)" |
| 9 | SOUND-010 | Added play_histories recording + play_count increment as postconditions |
| 10 | SOUND-011 | Updated preconditions (active subscription + COUNT query check), changed download count deduction to COUNT-based approach, added automatic license issuance |
| 11 | SOUND-016 | Fixed flow numbers, explicitly included track_tags deletion |
| 12 | PAYMENT-001 | Added billingCycle, added business license approval precondition, removed track usage license confusion |
| 13 | PAYMENT-007 | Removed license change content from postconditions |
| 14 | QUESTION-001 | Added category/isPublic/attachment fields |
| 15 | QUESTION-002 | Specified who can answer (member: own inquiry only, admin: all inquiries) |

### Newly Added UC (19)

| Code | Title | Reason |
|------|-------|--------|
| INFO-013 | Social login | Present in API spec section 5.3 but missing from original |
| SOUND-019 | Add track to playlist | Present in API spec section 4.5 but missing from original |
| SOUND-020 | Remove track from playlist | Present in API spec section 4.6 but missing from original |
| LIKE-001~003 | Likes CRUD | DB likes table + API section 10 present but missing from original |
| DLQ-001~003 | Download queue | DB download_queue table + API section 11 present but missing from original |
| WL-001~004 | Whitelist channels | DB whitelist_channels table + API section 12 present but missing from original |
| BL-001~005 | Business license review | DB business_license_requests table + API section 13 present but missing from original |
| UTIL-012 | Check nickname duplicate | Required by INFO-001/005 but missing from original. Added as API 14.7 |

### Removed UC (5)

| Original Code | Reason |
|---------------|--------|
| UTIL-001 (token issuance) | Merged into INFO-008 (login) |
| UTIL-008 (license issuance) | Merged into SOUND-011 (track download) |
| UTIL-009 (input validation - BE) | Spring Bean Validation standard feature |
| UTIL-010 (input validation - FE) | Frontend code level, outside UC scope |
| UTIL-011 (file storage) | Merged into SOUND-001 (create track) |

### DB/API Spec Changes (v2 to v3)

| # | Field | Value |
|---|-------|-------|
| 1 | `tracks.preview_file` column added | Low-quality file generated asynchronously after upload. If NULL, fallback to audio_file for streaming |
| 2 | `GET /api/utils/check-nickname` API added | Nickname duplicate check API (UTIL-012) |
| 3 | `track_tags` join table | Physical deletion confirmed when track is soft-deleted |
