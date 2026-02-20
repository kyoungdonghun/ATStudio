# ATStudio DB Schema Definition v4 (Confirmed)

> **Status**: v4 Confirmed — Mutual review reflected
> **Base**: v3 + 8 mutual review items confirmed
> **Date**: 2026-02-20

---

## v3 to v4 Change History

| # | Item | Decision |
|---|------|----------|
| 1 | `users.password` | **Allow NULL** — Social login-only accounts have NULL password. Supports two-step registration. |
| 2 | `users.phone_personal` | **Allow NULL** — NULL on social login registration. Entered during profile completion step. |
| 3 | `users.job` | **Allow NULL + DEFAULT NULL** — NULL on social login registration. Entered during profile completion step. |
| 4 | `whitelist_channels.is_active` | **Column removed** — Deletion confirmed as physical delete. No use case for is_active. |
| 5 | `licenses.issued_at` | **Column removed** — Semantically duplicates created_at. Map created_at to issuedAt in API response. |
| 6 | `subscription_payments.user_subscription_id` | **FK added** — Links payment record to a specific subscription session. |
| 7 | Playlist subscription-only | **Only members with an active subscription (ACTIVE)** can use playlist features. No DB changes (app-level permission check). |
| 8 | `users.userType` DEFAULT specified | **DEFAULT 'INDIVIDUAL'** confirmed |

---

## v2 to v3 Change History

| # | Item | Decision |
|---|------|----------|
| 1 | `tracks.preview_file` | **Addition confirmed** — Low-quality file generated asynchronously after upload. Falls back to audio_file for streaming if NULL |

---

## v1 to v2 Change History

| # | Item | Decision |
|---|------|----------|
| 1 | `users.download_remain` | **Removal confirmed** — Replaced with COUNT query approach |
| 2 | Subscription plan structure | **Single record contains both monthly and yearly prices** |
| 3 | Cart purpose | **Redefined as "download queue"** |
| 4 | `tracks.is_active` DEFAULT | **0 (published after review)** |
| 5 | `tracks.play_count` | **Addition confirmed** |
| 6 | Social login providers | **Confirmed 3 providers: Google/Kakao/Naver** |
| 7 | Business document file management | **Single path column** + app-level per-user folder separation |
| 8 | Individual user license | **Separate `licenses` table** added |
| 9 | `track_purchases` | **Removal confirmed** |
| 10 | Inquiry password | **Column removed** — Private inquiries viewable only by author + ADMIN (app-level permission) |

---

## Common Rules

### Base Columns (Common to All Tables)

| Description | Column | Type | NULL | DEFAULT |
|-------------|--------|------|------|---------|
| Created at | `created_at` | DATETIME | NOT NULL | CURRENT_TIMESTAMP |
| Updated at | `updated_at` | DATETIME | NOT NULL | CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP |

### Type Rules

- PK: `BIGINT AUTO_INCREMENT`
- FK: `BIGINT` (same type as PK)
- Boolean: `TINYINT(1)` (0/1)
- Currency: `DECIMAL(10,2)`
- Date: `DATETIME` (includes time) or `DATE` (date only)

---

# 1. Users and Roles

## 1.1 Users (`users`)

| Description | Column | Type | NULL | Constraints | DEFAULT | Notes |
|-------------|--------|------|------|-------------|---------|-------|
| ID | `id` | BIGINT | NOT NULL | PK, AUTO_INCREMENT | | |
| Nickname | `nickname` | VARCHAR(20) | NOT NULL | UNIQUE | | |
| Email | `email` | VARCHAR(100) | NOT NULL | UNIQUE | | |
| Password | `password` | VARCHAR(255) | NULL | | | BCrypt hash stored. NULL for social login-only accounts. |
| Company phone | `phone_company` | VARCHAR(20) | NULL | | | For business members |
| Personal phone | `phone_personal` | VARCHAR(20) | NULL | | | NULL on social login registration. Entered during profile completion step. |
| Account verified | `is_verified` | TINYINT(1) | NOT NULL | | 0 | Email or phone verification |
| Role | `role` | ENUM('USER','ADMIN') | NOT NULL | | 'USER' | |
| Job | `job` | ENUM('EDITOR','ARTIST','FREELANCER') | NULL | | NULL | NULL on social login registration. Entered during profile completion step. |
| User type | `user_type` | ENUM('INDIVIDUAL','BUSINESS') | NOT NULL | | 'INDIVIDUAL' | Individual / Business member |
| Soft delete flag | `is_deleted` | TINYINT(1) | NOT NULL | | 0 | Account deactivation |
| Created at | `created_at` | DATETIME | NOT NULL | | CURRENT_TIMESTAMP | |
| Updated at | `updated_at` | DATETIME | NOT NULL | | CURRENT_TIMESTAMP | |

**Social login two-step registration flow:**
- On first social login registration: minimal record created with email (from OAuth), nickname (auto-generated or user input), userType=INDIVIDUAL (default)
- `password`, `phone_personal`, `job` = NULL indicates incomplete profile state
- Frontend checks `isProfileComplete` flag (= phone_personal IS NOT NULL AND job IS NOT NULL) and redirects to profile completion screen
- Full feature access available after profile completion

**Daily download limit handling:**
- No `download_remain` column
- Calculated via COUNT query on `track_downloads` table: `WHERE user_id = ? AND DATE(downloaded_at) = CURDATE()`
- Compared against subscription plan's `download_per_day` to enforce limit

## 1.2 Social Login (`social_accounts`)

| Description | Column | Type | NULL | Constraints | DEFAULT | Notes |
|-------------|--------|------|------|-------------|---------|-------|
| ID | `id` | BIGINT | NOT NULL | PK, AUTO_INCREMENT | | |
| User | `user_id` | BIGINT | NOT NULL | FK(users.id) | | |
| Social provider | `provider` | ENUM('GOOGLE','KAKAO','NAVER') | NOT NULL | | | |
| Social unique ID | `provider_id` | VARCHAR(255) | NOT NULL | | | User ID assigned by the social service |
| Created at | `created_at` | DATETIME | NOT NULL | | CURRENT_TIMESTAMP | |
| Updated at | `updated_at` | DATETIME | NOT NULL | | CURRENT_TIMESTAMP | |

- UNIQUE constraint: (`provider`, `provider_id`)
- A single user can link multiple social accounts (1:N)

---

# 2. Subscription Plans

## 2.1 Subscription Plans (`subscriptions`)

| Description | Column | Type | NULL | Constraints | DEFAULT | Notes |
|-------------|--------|------|------|-------------|---------|-------|
| ID | `id` | BIGINT | NOT NULL | PK, AUTO_INCREMENT | | |
| Plan name | `name` | VARCHAR(30) | NOT NULL | | | STANDARD/DELUXE/PREMIUM |
| Description | `description` | TEXT | NULL | | | |
| Target user type | `user_type` | ENUM('INDIVIDUAL','BUSINESS') | NOT NULL | | | Individual / Business distinction |
| Monthly price | `price_monthly` | DECIMAL(10,2) | NOT NULL | | | |
| Yearly price | `price_yearly` | DECIMAL(10,2) | NOT NULL | | | |
| Daily download limit | `download_per_day` | INT | NOT NULL | | | -1 = unlimited |
| Max whitelist channels | `max_whitelist_channels` | INT | NOT NULL | | | Channel limit per tier |
| Active flag | `is_active` | TINYINT(1) | NOT NULL | | 1 | Whether selectable by users |
| Created at | `created_at` | DATETIME | NOT NULL | | CURRENT_TIMESTAMP | |
| Updated at | `updated_at` | DATETIME | NOT NULL | | CURRENT_TIMESTAMP | |

- UNIQUE constraint: (`name`, `user_type`) — prevents duplicate name+type combinations

### Subscription Plan Seed Data

| name | user_type | price_monthly | price_yearly | download_per_day | max_whitelist_channels |
|------|-----------|---------------|--------------|------------------|----------------------|
| STANDARD | INDIVIDUAL | [TBD] | [TBD] | 5 | 1 |
| DELUXE | INDIVIDUAL | [TBD] | [TBD] | 20 | 2 |
| PREMIUM | INDIVIDUAL | [TBD] | [TBD] | -1 | 2 |
| DELUXE | BUSINESS | [TBD] | [TBD] | 50 | 2 |
| PREMIUM | BUSINESS | [TBD] | [TBD] | -1 | 2 |

## 2.2 User Subscription Status (`user_subscriptions`)

| Description | Column | Type | NULL | Constraints | DEFAULT | Notes |
|-------------|--------|------|------|-------------|---------|-------|
| ID | `id` | BIGINT | NOT NULL | PK, AUTO_INCREMENT | | |
| User | `user_id` | BIGINT | NOT NULL | FK(users.id), UNIQUE | | One subscription per user |
| Subscription plan | `subscription_id` | BIGINT | NOT NULL | FK(subscriptions.id) | | |
| Billing cycle | `billing_cycle` | ENUM('MONTHLY','YEARLY') | NOT NULL | | | |
| Subscription status | `status` | ENUM('ACTIVE','CANCELLED','EXPIRED') | NOT NULL | | 'ACTIVE' | |
| Current period start date | `started_at` | DATE | NOT NULL | | | |
| Current period end date | `expires_at` | DATE | NOT NULL | | | Next billing date = expiration date + 1 day |
| Created at | `created_at` | DATETIME | NOT NULL | | CURRENT_TIMESTAMP | |
| Updated at | `updated_at` | DATETIME | NOT NULL | | CURRENT_TIMESTAMP | |

**Subscription upgrade handling:**
- Changes take effect immediately (usable right away)
- Payment amount = new plan price - prorated remaining amount of current plan
- Detailed calculation logic handled at application level

---

# 3. Business License Review

## 3.1 Business License Application (`business_license_requests`)

> Business members (over 100 employees) submit documents before purchasing a subscription plan. Admin reviews and approves or rejects.

| Description | Column | Type | NULL | Constraints | DEFAULT | Notes |
|-------------|--------|------|------|-------------|---------|-------|
| ID | `id` | BIGINT | NOT NULL | PK, AUTO_INCREMENT | | |
| Applicant | `user_id` | BIGINT | NOT NULL | FK(users.id) | | Business member |
| Review status | `status` | ENUM('PENDING','APPROVED','REVISION_REQUESTED','REJECTED') | NOT NULL | | 'PENDING' | Pending / Approved / Revision requested / Rejected |
| Admin note | `admin_note` | TEXT | NULL | | | Reason for revision request, etc. |
| Document file path | `document_path` | VARCHAR(500) | NOT NULL | | | Stored in per-user dedicated folder |
| License code | `license_code` | VARCHAR(50) | NULL | UNIQUE | | UUID-based, issued upon approval |
| Approved at | `approved_at` | DATETIME | NULL | | | Timestamp of approval completion |
| Created at | `created_at` | DATETIME | NOT NULL | | CURRENT_TIMESTAMP | |
| Updated at | `updated_at` | DATETIME | NOT NULL | | CURRENT_TIMESTAMP | |

**File storage policy:**
- App-level creation of per-user dedicated directory (e.g., `/uploads/business-docs/{user_id}/`)
- `document_path` stores the directory path
- Files within the directory are managed directly on the filesystem

**Process:**
1. Business member selects subscription plan and goes to document submission page
2. File upload + license request submission (status: PENDING)
3. Admin review: revision request (REVISION_REQUESTED) or approval (APPROVED)
4. Upon approval: `license_code` issued, subscription payment enabled
5. After payment, admin provides tax invoice/contract to the business (offline/separate process)

---

# 4. Tracks and Tags

## 4.1 Audio Tracks (`tracks`)

| Description | Column | Type | NULL | Constraints | DEFAULT | Notes |
|-------------|--------|------|------|-------------|---------|-------|
| ID | `id` | BIGINT | NOT NULL | PK, AUTO_INCREMENT | | |
| Track title | `title` | VARCHAR(100) | NOT NULL | | | |
| Thumbnail filename | `thumbnail` | VARCHAR(255) | NULL | | | |
| BPM | `bpm` | INT | NOT NULL | | | |
| Key/Tonality | `tonality` | VARCHAR(10) | NOT NULL | | | e.g., C, Am, F#m |
| Description | `description` | TEXT | NULL | | | |
| Audio file path | `audio_file` | VARCHAR(255) | NOT NULL | | | Original file (for download) |
| Preview file path | `preview_file` | VARCHAR(255) | NULL | | | Low-quality converted file (for streaming). Falls back to audio_file if NULL |
| Copyright holder | `user_id` | BIGINT | NOT NULL | FK(users.id) | | Currently only a single admin (artist) uses this |
| Active flag | `is_active` | TINYINT(1) | NOT NULL | | 0 | Published after review (admin activates) |
| Play count | `play_count` | BIGINT | NOT NULL | | 0 | Used for popularity sorting, etc. |
| Created at | `created_at` | DATETIME | NOT NULL | | CURRENT_TIMESTAMP | |
| Updated at | `updated_at` | DATETIME | NOT NULL | | CURRENT_TIMESTAMP | |

## 4.2 Tags (`tags`)

| Description | Column | Type | NULL | Constraints | DEFAULT | Notes |
|-------------|--------|------|------|-------------|---------|-------|
| ID | `id` | BIGINT | NOT NULL | PK, AUTO_INCREMENT | | |
| Tag name | `name` | VARCHAR(50) | NOT NULL | UNIQUE | | |
| Tag type | `type` | ENUM('MOOD','GENRE','INSTRUMENT') | NOT NULL | | | Mood / Genre / Instrument |
| Created at | `created_at` | DATETIME | NOT NULL | | CURRENT_TIMESTAMP | |
| Updated at | `updated_at` | DATETIME | NOT NULL | | CURRENT_TIMESTAMP | |

## 4.3 Track-Tag Mapping (`track_tags`)

| Description | Column | Type | NULL | Constraints | DEFAULT | Notes |
|-------------|--------|------|------|-------------|---------|-------|
| Track | `track_id` | BIGINT | NOT NULL | PK, FK(tracks.id) | | |
| Tag | `tag_id` | BIGINT | NOT NULL | PK, FK(tags.id) | | |

- Composite PK: (`track_id`, `tag_id`)
- When tags are modified, `tracks.updated_at` is also updated

---

# 5. Playlists

## 5.1 Playlists (`playlists`)

| Description | Column | Type | NULL | Constraints | DEFAULT | Notes |
|-------------|--------|------|------|-------------|---------|-------|
| ID | `id` | BIGINT | NOT NULL | PK, AUTO_INCREMENT | | |
| Title | `title` | VARCHAR(50) | NOT NULL | | | |
| Description | `description` | TEXT | NULL | | | |
| Thumbnail | `thumbnail` | VARCHAR(255) | NULL | | | |
| Creator | `user_id` | BIGINT | NOT NULL | FK(users.id) | | |
| Active flag | `is_active` | TINYINT(1) | NOT NULL | | 1 | |
| Created at | `created_at` | DATETIME | NOT NULL | | CURRENT_TIMESTAMP | |
| Updated at | `updated_at` | DATETIME | NOT NULL | | CURRENT_TIMESTAMP | |

## 5.2 Playlist-Track Mapping (`playlist_tracks`)

| Description | Column | Type | NULL | Constraints | DEFAULT | Notes |
|-------------|--------|------|------|-------------|---------|-------|
| Playlist | `playlist_id` | BIGINT | NOT NULL | PK, FK(playlists.id) | | |
| Track | `track_id` | BIGINT | NOT NULL | PK, FK(tracks.id) | | |
| Track order | `track_order` | INT | NOT NULL | | | |

- Composite PK: (`playlist_id`, `track_id`)
- When modified, `playlists.updated_at` is also updated

---

# 6. History / Logs

## 6.1 Track Download History (`track_downloads`)

| Description | Column | Type | NULL | Constraints | DEFAULT | Notes |
|-------------|--------|------|------|-------------|---------|-------|
| ID | `id` | BIGINT | NOT NULL | PK, AUTO_INCREMENT | | |
| User | `user_id` | BIGINT | NOT NULL | FK(users.id) | | |
| Track | `track_id` | BIGINT | NOT NULL | FK(tracks.id) | | |
| Downloaded at | `downloaded_at` | DATETIME | NOT NULL | | CURRENT_TIMESTAMP | |

- Same track can be downloaded multiple times
- Daily download limit: `COUNT(*) WHERE user_id = ? AND DATE(downloaded_at) = CURDATE()`
- INDEX: (`user_id`, `downloaded_at`) — ensures daily count query performance

## 6.2 Play History (`play_histories`)

| Description | Column | Type | NULL | Constraints | DEFAULT | Notes |
|-------------|--------|------|------|-------------|---------|-------|
| ID | `id` | BIGINT | NOT NULL | PK, AUTO_INCREMENT | | |
| User | `user_id` | BIGINT | NOT NULL | FK(users.id) | | |
| Track | `track_id` | BIGINT | NOT NULL | FK(tracks.id) | | |
| Played at | `played_at` | DATETIME | NOT NULL | | CURRENT_TIMESTAMP | |

- Each repeated play of the same track is recorded (history)
- Record created when played from the Que bar
- Linked with `tracks.play_count` increment (app-level)

## 6.3 Subscription Payment Records (`subscription_payments`)

| Description | Column | Type | NULL | Constraints | DEFAULT | Notes |
|-------------|--------|------|------|-------------|---------|-------|
| ID | `id` | BIGINT | NOT NULL | PK, AUTO_INCREMENT | | |
| User | `user_id` | BIGINT | NOT NULL | FK(users.id) | | |
| Subscription record | `user_subscription_id` | BIGINT | NOT NULL | FK(user_subscriptions.id) | | Links to which subscription session this payment belongs to |
| Subscription plan | `subscription_id` | BIGINT | NOT NULL | FK(subscriptions.id) | | |
| Billing cycle | `billing_cycle` | ENUM('MONTHLY','YEARLY') | NOT NULL | | | |
| Payment amount | `amount` | DECIMAL(10,2) | NOT NULL | | | Prorated amount for upgrades |
| Payment status | `payment_status` | ENUM('READY','DONE','REFUND') | NOT NULL | | 'READY' | |
| PG transaction ID | `pg_transaction_id` | VARCHAR(100) | NULL | | | Used for PG provider integration |
| Created at | `created_at` | DATETIME | NOT NULL | | CURRENT_TIMESTAMP | |
| Updated at | `updated_at` | DATETIME | NOT NULL | | CURRENT_TIMESTAMP | |

---

# 7. Likes

## 7.1 Likes (`likes`)

| Description | Column | Type | NULL | Constraints | DEFAULT | Notes |
|-------------|--------|------|------|-------------|---------|-------|
| User | `user_id` | BIGINT | NOT NULL | PK, FK(users.id) | | |
| Track | `track_id` | BIGINT | NOT NULL | PK, FK(tracks.id) | | |
| Created at | `created_at` | DATETIME | NOT NULL | | CURRENT_TIMESTAMP | |

- Composite PK: (`user_id`, `track_id`)

---

# 8. Download Queue

> Since there is no purchase concept, "cart" has been redefined as **"download queue"**.
> Used for collecting multiple tracks and downloading them in bulk.

## 8.1 Download Queue (`download_queue`)

| Description | Column | Type | NULL | Constraints | DEFAULT | Notes |
|-------------|--------|------|------|-------------|---------|-------|
| User | `user_id` | BIGINT | NOT NULL | PK, FK(users.id) | | |
| Track | `track_id` | BIGINT | NOT NULL | PK, FK(tracks.id) | | |
| Created at | `created_at` | DATETIME | NOT NULL | | CURRENT_TIMESTAMP | |

- Composite PK: (`user_id`, `track_id`)
- Table renamed from `cart_items` to `download_queue` (matches actual purpose)

---

# 9. Whitelist Channels

## 9.1 Whitelist Channels (`whitelist_channels`)

| Description | Column | Type | NULL | Constraints | DEFAULT | Notes |
|-------------|--------|------|------|-------------|---------|-------|
| ID | `id` | BIGINT | NOT NULL | PK, AUTO_INCREMENT | | |
| User | `user_id` | BIGINT | NOT NULL | FK(users.id) | | |
| Channel URL | `channel_url` | VARCHAR(255) | NOT NULL | | | YouTube channel URL |
| Channel name | `channel_name` | VARCHAR(100) | NOT NULL | | | Display name |
| Created at | `created_at` | DATETIME | NOT NULL | | CURRENT_TIMESTAMP | |
| Updated at | `updated_at` | DATETIME | NOT NULL | | CURRENT_TIMESTAMP | |

- Maximum registerable channels limited by `subscriptions.max_whitelist_channels`
- App checks current active channel count on registration

---

# 10. Board (Inquiries / Answers)

## 10.1 Inquiries (`questions`)

| Description | Column | Type | NULL | Constraints | DEFAULT | Notes |
|-------------|--------|------|------|-------------|---------|-------|
| ID | `id` | BIGINT | NOT NULL | PK, AUTO_INCREMENT | | |
| Author | `user_id` | BIGINT | NOT NULL | FK(users.id) | | |
| Title | `title` | VARCHAR(200) | NOT NULL | | | |
| Content | `content` | TEXT | NOT NULL | | | |
| Category | `category` | ENUM('DOWNLOAD','PAYMENT','COPYRIGHT','PRODUCTION','OTHER') | NOT NULL | | | Download / Payment / Copyright / Music Production / Other |
| Public/private flag | `is_public` | TINYINT(1) | NOT NULL | | 0 | |
| Inquiry status | `status` | ENUM('OPEN','IN_PROGRESS','RESOLVED','CLOSED') | NOT NULL | | 'OPEN' | |
| Created at | `created_at` | DATETIME | NOT NULL | | CURRENT_TIMESTAMP | |
| Updated at | `updated_at` | DATETIME | NOT NULL | | CURRENT_TIMESTAMP | |

**Private inquiry access policy:**
- No `password` column
- Inquiries with `is_public = 0`: viewable only by **the author + ADMIN**
- Permission check at application level (Spring Security)

**Status flow:**
- OPEN -> IN_PROGRESS (on admin's first answer) -> RESOLVED (resolved) -> CLOSED (closed)
- OPEN -> CLOSED (admin closes without answering)

## 10.2 Inquiry Answers (`answers`)

| Description | Column | Type | NULL | Constraints | DEFAULT | Notes |
|-------------|--------|------|------|-------------|---------|-------|
| ID | `id` | BIGINT | NOT NULL | PK, AUTO_INCREMENT | | |
| Inquiry | `question_id` | BIGINT | NOT NULL | FK(questions.id) | | |
| Author | `user_id` | BIGINT | NOT NULL | FK(users.id) | | Inquiry author or admin |
| Content | `content` | TEXT | NOT NULL | | | |
| Created at | `created_at` | DATETIME | NOT NULL | | CURRENT_TIMESTAMP | |
| Updated at | `updated_at` | DATETIME | NOT NULL | | CURRENT_TIMESTAMP | |

- Conversational structure between inquiry author and admin (1:N)
- Author distinguished by `user_id` role (USER/ADMIN)
- `questions.updated_at` updated when a new answer is posted

---

# 11. Licenses (For Individual Users)

## 11.1 Track Usage License (`licenses`)

> UUID-based license code issued for downloaded tracks

| Description | Column | Type | NULL | Constraints | DEFAULT | Notes |
|-------------|--------|------|------|-------------|---------|-------|
| ID | `id` | BIGINT | NOT NULL | PK, AUTO_INCREMENT | | |
| User | `user_id` | BIGINT | NOT NULL | FK(users.id) | | |
| Track | `track_id` | BIGINT | NOT NULL | FK(tracks.id) | | |
| License code | `license_code` | VARCHAR(50) | NOT NULL | UNIQUE | | UUID-based |
| Created at | `created_at` | DATETIME | NOT NULL | | CURRENT_TIMESTAMP | Mapped as issuedAt in API response |
| Updated at | `updated_at` | DATETIME | NOT NULL | | CURRENT_TIMESTAMP | |

- Automatically issued on download (existing license retained on re-download of same track; no duplicate issuance)
- UNIQUE constraint: (`user_id`, `track_id`) — one license per user per track
- Serves as a copyright proof identifier
- Detailed legal format to be implemented in future guidance

---

# 12. Notices

## 12.1 Notices (`notices`)

> Added per API specification

| Description | Column | Type | NULL | Constraints | DEFAULT | Notes |
|-------------|--------|------|------|-------------|---------|-------|
| ID | `id` | BIGINT | NOT NULL | PK, AUTO_INCREMENT | | |
| Author | `user_id` | BIGINT | NOT NULL | FK(users.id) | | ADMIN |
| Title | `title` | VARCHAR(200) | NOT NULL | | | |
| Content | `content` | TEXT | NOT NULL | | | |
| Pinned flag | `is_pinned` | TINYINT(1) | NOT NULL | | 0 | Pinned to top |
| Created at | `created_at` | DATETIME | NOT NULL | | CURRENT_TIMESTAMP | |
| Updated at | `updated_at` | DATETIME | NOT NULL | | CURRENT_TIMESTAMP | |

---

# 13. Inquiry Attachments

## 13.1 Inquiry Attachments (`question_attachments`)

> Added per API specification

| Description | Column | Type | NULL | Constraints | DEFAULT | Notes |
|-------------|--------|------|------|-------------|---------|-------|
| ID | `id` | BIGINT | NOT NULL | PK, AUTO_INCREMENT | | |
| Inquiry | `question_id` | BIGINT | NOT NULL | FK(questions.id) | | |
| Original filename | `original_name` | VARCHAR(255) | NOT NULL | | | Original filename at upload |
| Stored file path | `file_path` | VARCHAR(500) | NOT NULL | | | Server storage path |
| File size | `file_size` | BIGINT | NOT NULL | | | In bytes |
| Created at | `created_at` | DATETIME | NOT NULL | | CURRENT_TIMESTAMP | |

- One inquiry can have multiple attachments (1:N)

---

# Table Relationship Diagram

```
users ─┬─< social_accounts
       ├─< user_subscriptions ──> subscriptions
       ├─< subscription_payments ──> subscriptions
       ├─< business_license_requests
       ├─< track_downloads ──> tracks
       ├─< play_histories ──> tracks
       ├─< likes ──> tracks
       ├─< download_queue ──> tracks
       ├─< whitelist_channels
       ├─< licenses ──> tracks
       ├─< playlists ─< playlist_tracks ──> tracks
       ├─< questions ─┬─< answers
       │              └─< question_attachments
       └─< notices (ADMIN only)

tracks ─< track_tags ──> tags
```

---

# Complete Table List (21 Tables)

| # | Table Name | Description | Type |
|---|------------|-------------|------|
| 1 | `users` | Users | Master |
| 2 | `social_accounts` | Social login | Master |
| 3 | `subscriptions` | Subscription plan definitions | Master |
| 4 | `user_subscriptions` | User subscription status | Transaction |
| 5 | `business_license_requests` | Business license review | Transaction |
| 6 | `tracks` | Audio tracks | Master |
| 7 | `tags` | Tags | Master |
| 8 | `track_tags` | Track-tag mapping | Mapping |
| 9 | `playlists` | Playlists | Master |
| 10 | `playlist_tracks` | Playlist-track mapping | Mapping |
| 11 | `track_downloads` | Download history | Log |
| 12 | `play_histories` | Play history | Log |
| 13 | `subscription_payments` | Subscription payment records | Transaction |
| 14 | `likes` | Likes | Mapping |
| 15 | `download_queue` | Download queue | Mapping |
| 16 | `whitelist_channels` | Whitelist channels | Master |
| 17 | `questions` | Inquiries | Transaction |
| 18 | `answers` | Inquiry answers | Transaction |
| 19 | `licenses` | Track usage licenses | Transaction |
| 20 | `notices` | Notices | Master |
| 21 | `question_attachments` | Inquiry attachments | Transaction |

Total **21 tables**
