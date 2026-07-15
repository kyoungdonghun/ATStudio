# ATStudio DB Schema Definition v15 (Confirmed)

> **Status**: v15 Confirmed - current Track media semantics with no structural schema change
> **Base**: v14 + REQ-20260715-ATS-001 public full-track listening alignment
> **Date**: 2026-07-15

---

## v14 to v15 Change History

| # | Item | Decision |
|---|------|----------|
| 1 | `tracks.audio_file` / `tracks.preview_file` | **Current contract clarified** - `audio_file` remains a private storage key used only through controller-mediated Public Listening and Official Download. Public DTOs redact the key, the static original route remains denied, and current listening does not depend on `preview_file`. |
| 2 | Table count and structure | **Unchanged** - 39 tables; no column, key, ENUM, seed, or data change. |

---

## v13 to v14 Change History (Historical)

| # | Item | Decision |
|---|------|----------|
| 1 | `tracks.audio_file` / `tracks.preview_file` | **Historical 2026-07-13 contract, superseded for listening behavior by v15** - this version documented an optional dedicated resource and bounded original fallback. That listening decision is preserved only as history and is not current policy. |
| 2 | `billing_agreements` withdrawal lifecycle | **Clarified** — withdrawal cancels locally before soft deletion, then performs Provider key cleanup after commit with retained key material on retryable failure. |
| 3 | `payment_reconciliation_incidents` | **Clarified** — withdrawal cleanup reuses `LOCAL_DONE_PROVIDER_NOT_DONE` with agreement-scoped dedupe and resolves it after convergent cleanup. |
| 4 | Table count and structure | **Unchanged** — 39 tables; no column, key, ENUM, seed, or data change. |

---

## Runtime DB Application Notes

- `src/main/resources/application.yml` defaults to `spring.jpa.hibernate.ddl-auto=validate`.
- `src/main/resources/schema.sql` is the full manual setup/reference schema for a fresh MySQL database. It is not an automatic migration tool for an existing local/staging/production DB.
- Spring Boot does not auto-run `schema.sql` against the external MySQL datasource by default. Test profile explicitly disables SQL init and uses Hibernate `create-drop` for H2.
- Existing DBs must be patched before starting the server with `ddl-auto=validate`; otherwise Hibernate validation can fail when new columns/tables are missing.
- Current manual patch files:
  - `src/main/resources/db/manual/20260615_align_payment_whitelist_schema.sql`
  - `src/main/resources/db/manual/20260618_company_certification_documents.sql`
- Apply the patch only after backup and local/staging rehearsal. Do not execute it blindly against production. MySQL DDL may implicitly commit.
- The patches cover the latest known delta for `tags.type=USAGE`, `payment_settlements`, expanded `whitelist_channels`, `whitelist_export_batches` / `whitelist_export_items`, and `company_certification_documents`. Older DBs that predate previous payment-operation tables must apply those earlier schema changes first or be rebuilt from `schema.sql`.

---

## v12 to v13 Change History

| # | Item | Decision |
|---|------|----------|
| 1 | `company_certification_documents` | **Added** — stores per-file metadata for company certification review documents. |
| 2 | Document access policy | **Updated** — admin review should use authenticated document metadata/download APIs instead of relying on raw storage directory paths. |
| 3 | Table count | **Updated** — Total database tables changed from 38 to 39. |

---

## v11 to v12 Change History

| # | Item | Decision |
|---|------|----------|
| 1 | `whitelist_channels` | **Updated** — Added YouTube handle/channel ID, status workflow, primary flag, requested/exported/processed/removal timestamps, admin note, and processed-by admin reference. |
| 2 | `whitelist_export_batches`, `whitelist_export_items` | **Added** — Admin CSV export batch and item snapshots for manual external YouTube/agency whitelist registration workflow. |
| 3 | Table count | **Updated** — Total database tables changed from 36 to 38. |

---

## v10 to v11 Change History

| # | Item | Decision |
|---|------|----------|
| 1 | `tags.type` | **Updated** — Added `USAGE` for visible guide hashtags and search filters such as `#쇼츠용`, `#유튜브용`, and `#릴스용`. |
| 2 | Table count | **Unchanged** — Total database tables remains 36. |

---

## v9 to v10 Change History

| # | Item | Decision |
|---|------|----------|
| 1 | `payment_settlements` | **Added** — Admin settlement evidence import and reconciliation ledger for PG-to-merchant accounting review. |
| 2 | `payment_operation_audit_logs` settlement actions | **Updated** — Added settlement import/reconcile/ignore action values and `PAYMENT_SETTLEMENT` target type. |
| 3 | Table count | **Updated** — Total database tables changed from 35 to 36. |

---

## v8 to v9 Change History

| # | Item | Decision |
|---|------|----------|
| 1 | `payment_entitlement_corrections` | **Added** — Local admin entitlement correction ledger linked to succeeded refund records, storing before/target subscription state and actor timeline. |
| 2 | `payment_operation_audit_logs` entitlement correction actions | **Updated** — Added entitlement correction workflow action values and `PAYMENT_ENTITLEMENT_CORRECTION` target type. |
| 3 | Table count | **Updated** — Total database tables changed from 34 to 35. |

---

## v7 to v8 Change History

| # | Item | Decision |
|---|------|----------|
| 1 | `payment_refunds` | **Added** — Local admin refund ledger for request, approval, provider execution, idempotency, and provider cancel traceability. |
| 2 | `payment_operation_audit_logs` refund actions | **Updated** — Added explicit refund workflow action values and `PAYMENT_REFUND` target type. |
| 3 | Table count | **Updated** — Total database tables changed from 33 to 34. |

---

## v6 to v7 Change History

| # | Item | Decision |
|---|------|----------|
| 1 | `payment_receipts` | **Added** — Safe receipt/cash receipt evidence rows captured after successful provider charges. |
| 2 | `payment_operation_audit_logs` | **Added** — Append-only payment operation audit rows for reconciliation incident workflow changes and receipt evidence creation. |
| 3 | Sensitive evidence boundary | **Confirmed** — Receipt evidence payload stores sanitized metadata only, never billing keys, auth keys, customer keys, raw card data, or raw provider payloads. |

---

## v5 to v6 Change History

| # | Item | Decision |
|---|------|----------|
| 1 | `users.company_name` | **Added** — VARCHAR(100) NULL. Manually entered company name for BUSINESS members (SR-47). Independent of company_certifications.company_name (which is on the certification document). |
| 2 | `subscriptions.max_playlists` | **Added** — INT NOT NULL DEFAULT 3. Tier-based limit on the number of active playlists a subscriber may hold (SR-55). |
| 3 | `subscriptions` seed data | **Finalized** — Replaced all `[TBD]` prices with confirmed values. Added new `STANDARD/BUSINESS` row. Populated `max_playlists` per tier. |
| 4 | `payment_orders` | **Added** — Mock-first payment attempt ledger for subscription prepare/confirm/cancel flow. |
| 5 | `billing_agreements` | **Added** — Server-side recurring billing agreement and encrypted billing key metadata. |
| 6 | Payment table billing links | **Added** — `payment_orders.billing_agreement_id` and `subscription_payments.billing_agreement_id` for recurring billing traceability. |
| 7 | Subscription change policy | **Updated** — Upgrade charges remaining-period difference through `TOSS_BILLING`, preserves the current billing cycle for the active period, and stores requested future cycle changes as pending; downgrade remains pending for next renewal. |
| 8 | `payment_reconciliation_incidents` | **Added** — Persistent local/provider reconciliation incident workflow with dedupe, occurrence count, optional notification marker, and admin status tracking. |

---

## v4 to v5 Change History

| # | Item | Decision |
|---|------|----------|
| 1 | `site_settings` table | **Added** — Key-value store for admin-managed site configuration (e.g., company certification guide text). Entity: `SiteSetting`. API: §17.1/17.2. |

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
| 1 | `tracks.preview_file` | **Historical v3 schema addition** - the version associated this nullable column with a separate preview workflow. That behavior is superseded; v15 Public Listening serves the complete active Track through the controller and does not depend on this legacy column. |

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
| Company name | `company_name` | VARCHAR(100) | NULL | | NULL | Company name for BUSINESS members (manual input, SR-47). Independent of company_certifications document field. |
| Soft delete flag | `is_deleted` | TINYINT(1) | NOT NULL | | 0 | Account deactivation |
| Refresh token | `refresh_token` | VARCHAR(512) | NULL | | | SHA-256 hashed refresh token. NULL when logged out. |
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
| Max active playlists | `max_playlists` | INT | NOT NULL | | 3 | Maximum active playlists per subscriber (tier-based limit, SR-55) |
| Active flag | `is_active` | TINYINT(1) | NOT NULL | | 1 | Whether selectable by users |
| Created at | `created_at` | DATETIME | NOT NULL | | CURRENT_TIMESTAMP | |
| Updated at | `updated_at` | DATETIME | NOT NULL | | CURRENT_TIMESTAMP | |

- UNIQUE constraint: (`name`, `user_type`) — prevents duplicate name+type combinations

### Subscription Plan Seed Data

| name | user_type | price_monthly | price_yearly | download_per_day | max_whitelist_channels | max_playlists |
|------|-----------|---------------|--------------|------------------|------------------------|---------------|
| STANDARD | INDIVIDUAL | 9,900 | 99,000 | 5 | 1 | 3 |
| DELUXE | INDIVIDUAL | 19,900 | 199,000 | 20 | 2 | 10 |
| PREMIUM | INDIVIDUAL | 29,900 | 299,000 | -1 | 2 | 10 |
| STANDARD | BUSINESS | 19,900 | 199,000 | 10 | 1 | 3 |
| DELUXE | BUSINESS | 49,900 | 499,000 | 50 | 2 | 10 |
| PREMIUM | BUSINESS | 99,900 | 999,000 | -1 | 2 | 10 |

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
| Pending subscription plan | `pending_subscription_id` | BIGINT | NULL | FK(subscriptions.id) | | Plan/cycle change scheduled for next renewal |
| Pending billing cycle | `pending_billing_cycle` | VARCHAR(10) | NULL | | | MONTHLY or YEARLY for next-renewal change |
| Created at | `created_at` | DATETIME | NOT NULL | | CURRENT_TIMESTAMP | |
| Updated at | `updated_at` | DATETIME | NOT NULL | | CURRENT_TIMESTAMP | |

**Subscription change handling:**
- **Upgrade** (higher price): The higher plan takes effect immediately only after charging the remaining-period price difference through the active billing agreement. The active period keeps the current `billing_cycle` and `expires_at`. If the request changes billing cycle, `pending_subscription_id` and `pending_billing_cycle` store the next-renewal plan/cycle.
- **Downgrade** (lower price): Change is **scheduled** (pending). Current subscription remains active until expiration. `pending_subscription_id` and `pending_billing_cycle` store the target plan/cycle. Actual switch happens at the next successful renewal.

---

# 3. Company Certification

## 3.1 Company Certification (`company_certifications`)

> Business members (over 100 employees) submit documents before purchasing a subscription plan. Admin reviews and approves or rejects.

| Description | Column | Type | NULL | Constraints | DEFAULT | Notes |
|-------------|--------|------|------|-------------|---------|-------|
| ID | `id` | BIGINT | NOT NULL | PK, AUTO_INCREMENT | | |
| Applicant | `user_id` | BIGINT | NOT NULL | FK(users.id) | | Business member |
| Review status | `status` | ENUM('PENDING','APPROVED','REVISION_REQUESTED','REJECTED') | NOT NULL | | 'PENDING' | Pending / Approved / Revision requested / Rejected |
| Admin note | `admin_note` | TEXT | NULL | | | Reason for revision request, etc. |
| Document file path | `document_path` | VARCHAR(500) | NOT NULL | | | Legacy/current directory hint. Per-file metadata is stored in `company_certification_documents`. |
| Certification code | `certification_code` | VARCHAR(50) | NULL | UNIQUE | | UUID-based, issued upon approval |
| Approved at | `approved_at` | DATETIME | NULL | | | Timestamp of approval completion |
| Created at | `created_at` | DATETIME | NOT NULL | | CURRENT_TIMESTAMP | |
| Updated at | `updated_at` | DATETIME | NOT NULL | | CURRENT_TIMESTAMP | |

**File storage policy:**
- App-level creation of per-submission dedicated directory (e.g., `/uploads/company-docs/{user_id}/{request_key}/`)
- `document_path` stores the directory hint for backward compatibility.
- `company_certification_documents.stored_path` stores the StorageService relative path for each file.
- Admin review uses authenticated download API; direct public `/uploads/company-docs/**` access is not the primary document review mechanism.

**Process:**
1. Business member selects subscription plan and goes to document submission page
2. File upload + certification request submission (status: PENDING)
3. Admin review: revision request (REVISION_REQUESTED), rejection (REJECTED), or approval (APPROVED)
4. REVISION_REQUESTED: user replaces documents on the same certification, returning status to PENDING
5. REJECTED: previous record remains as history; user may submit a new application
6. Upon approval: `certification_code` issued, subscription payment enabled
7. After payment, admin provides offline business contract evidence if needed. Tax invoice handling is not part of the current card-only recurring subscription scope.

## 3.2 Company Certification Documents (`company_certification_documents`)

> Per-file metadata for documents submitted to a company certification application. Raw file contents remain in StorageService-managed filesystem storage.

| Description | Column | Type | NULL | Constraints | DEFAULT | Notes |
|-------------|--------|------|------|-------------|---------|-------|
| ID | `id` | BIGINT | NOT NULL | PK, AUTO_INCREMENT | | |
| Certification | `certification_id` | BIGINT | NOT NULL | FK(company_certifications.id), ON DELETE CASCADE | | Parent application |
| Original filename | `original_filename` | VARCHAR(255) | NOT NULL | | | Display name for admin review |
| Stored path | `stored_path` | VARCHAR(500) | NOT NULL | | | StorageService relative path. Not exposed directly to users. |
| Content type | `content_type` | VARCHAR(100) | NULL | | | Browser-provided MIME type |
| Size bytes | `size_bytes` | BIGINT | NOT NULL | | | Uploaded file size |
| Created at | `created_at` | DATETIME | NOT NULL | | CURRENT_TIMESTAMP | |
| Updated at | `updated_at` | DATETIME | NOT NULL | | CURRENT_TIMESTAMP | |

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
| Audio file path | `audio_file` | VARCHAR(255) | NOT NULL | | | Private original storage key used for controller-mediated full Public Listening and protected Official Download; public Track DTOs return `audioFile: null` and `/uploads/tracks/audio/**` is denied |
| Legacy preview file path | `preview_file` | VARCHAR(255) | NULL | | | Nullable legacy column retained for schema compatibility; current Public Listening does not select or depend on it |
| Duration | `duration` | INT | NOT NULL | | 0 | Duration in seconds, auto-extracted from audio file |
| Copyright holder | `user_id` | BIGINT | NOT NULL | FK(users.id) | | Currently only a single admin (artist) uses this |
| Active flag | `is_active` | TINYINT(1) | NOT NULL | | 0 | Published after review (admin activates) |
| Play count | `play_count` | BIGINT | NOT NULL | | 0 | Used for popularity sorting, etc. |
| Like count | `like_count` | BIGINT | NOT NULL | | 0 | Incremented/decremented by like add/remove |
| Download count | `download_count` | BIGINT | NOT NULL | | 0 | Incremented on each successful download |
| Created at | `created_at` | DATETIME | NOT NULL | | CURRENT_TIMESTAMP | |
| Updated at | `updated_at` | DATETIME | NOT NULL | | CURRENT_TIMESTAMP | |

## 4.2 Tags (`tags`)

| Description | Column | Type | NULL | Constraints | DEFAULT | Notes |
|-------------|--------|------|------|-------------|---------|-------|
| ID | `id` | BIGINT | NOT NULL | PK, AUTO_INCREMENT | | |
| Tag name | `name` | VARCHAR(50) | NOT NULL | UNIQUE | | |
| Tag type | `type` | ENUM('MOOD','GENRE','INSTRUMENT','USAGE') | NOT NULL | | | Mood / Genre / Instrument / visible usage guide hashtag |
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

## 6.3 Payment Orders (`payment_orders`)

| Description | Column | Type | NULL | Constraints | DEFAULT | Notes |
|-------------|--------|------|------|-------------|---------|-------|
| ID | `id` | BIGINT | NOT NULL | PK, AUTO_INCREMENT | | |
| Merchant order ID | `order_id` | VARCHAR(64) | NOT NULL | UNIQUE | | Sent to provider-facing checkout/confirm flow |
| User | `user_id` | BIGINT | NOT NULL | FK(users.id) | | Authenticated order owner |
| Purpose | `purpose` | ENUM('SUBSCRIBE','UPGRADE','RENEWAL','BILLING_AGREEMENT') | NOT NULL | | | SUBSCRIBE for initial recurring charge, UPGRADE for billing-key plan change charge, RENEWAL for automatic billing, BILLING_AGREEMENT for zero-amount payment method re-registration |
| Provider | `provider` | ENUM('MOCK','TOSS','TOSS_BILLING','KAKAOPAY') | NOT NULL | | | TOSS_BILLING for subscription scope; MOCK/TOSS one-time are not user-facing subscription flows |
| Status | `status` | ENUM('READY','IN_PROGRESS','DONE','FAILED','CANCELLED','EXPIRED') | NOT NULL | | 'READY' | Attempt lifecycle |
| Subscription plan | `subscription_id` | BIGINT | NOT NULL | FK(subscriptions.id) | | Target plan |
| User subscription | `user_subscription_id` | BIGINT | NULL | FK(user_subscriptions.id) | | Set for upgrade or after confirm |
| Billing agreement | `billing_agreement_id` | BIGINT | NULL | FK(billing_agreements.id) | | Set for billing registration and renewal orders |
| Billing cycle | `billing_cycle` | ENUM('MONTHLY','YEARLY') | NOT NULL | | | |
| Amount | `amount` | DECIMAL(10,2) | NOT NULL | | | Server-calculated charge amount |
| Currency | `currency` | VARCHAR(3) | NOT NULL | | 'KRW' | |
| PG transaction ID | `pg_transaction_id` | VARCHAR(200) | NULL | | | Provider transaction key after confirmation |
| Provider payload | `provider_payload` | TEXT | NULL | | | Sanitized metadata only |
| Failure code | `failure_code` | VARCHAR(100) | NULL | | | |
| Failure message | `failure_message` | VARCHAR(500) | NULL | | | |
| Expires at | `expires_at` | DATETIME | NOT NULL | | | Prevents stale confirmation |
| Confirmed at | `confirmed_at` | DATETIME | NULL | | | |
| Created at | `created_at` | DATETIME | NOT NULL | | CURRENT_TIMESTAMP | |
| Updated at | `updated_at` | DATETIME | NOT NULL | | CURRENT_TIMESTAMP | |

## 6.4 Subscription Payment Records (`subscription_payments`)

| Description | Column | Type | NULL | Constraints | DEFAULT | Notes |
|-------------|--------|------|------|-------------|---------|-------|
| ID | `id` | BIGINT | NOT NULL | PK, AUTO_INCREMENT | | |
| User | `user_id` | BIGINT | NOT NULL | FK(users.id) | | |
| Subscription record | `user_subscription_id` | BIGINT | NOT NULL | FK(user_subscriptions.id) | | Links to which subscription session this payment belongs to |
| Subscription plan | `subscription_id` | BIGINT | NOT NULL | FK(subscriptions.id) | | |
| Payment order | `payment_order_id` | BIGINT | NULL | FK(payment_orders.id) | | Links finalized payment to attempted order |
| Billing agreement | `billing_agreement_id` | BIGINT | NULL | FK(billing_agreements.id) | | Links recurring billing charges to the agreement |
| Billing cycle | `billing_cycle` | ENUM('MONTHLY','YEARLY') | NOT NULL | | | |
| Provider | `provider` | ENUM('MOCK','TOSS','TOSS_BILLING','KAKAOPAY') | NULL | | | Null for legacy direct records |
| Payment amount | `amount` | DECIMAL(10,2) | NOT NULL | | | Remaining-period difference for upgrades; full period amount for initial/renewal charges |
| Payment status | `payment_status` | ENUM('READY','DONE','REFUND') | NOT NULL | | 'READY' | |
| PG transaction ID | `pg_transaction_id` | VARCHAR(100) | NULL | | | Used for PG provider integration |
| Created at | `created_at` | DATETIME | NOT NULL | | CURRENT_TIMESTAMP | |
| Updated at | `updated_at` | DATETIME | NOT NULL | | CURRENT_TIMESTAMP | |

## 6.5 Payment Refunds (`payment_refunds`)

| Description | Column | Type | NULL | Constraints | DEFAULT | Notes |
|-------------|--------|------|------|-------------|---------|-------|
| ID | `id` | BIGINT | NOT NULL | PK, AUTO_INCREMENT | | |
| Subscription payment | `subscription_payment_id` | BIGINT | NOT NULL | FK(subscription_payments.id), INDEX | | Original finalized payment |
| Payment order | `payment_order_id` | BIGINT | NOT NULL | FK(payment_orders.id) | | Original provider order |
| User | `user_id` | BIGINT | NOT NULL | FK(users.id), INDEX(user_id,created_at) | | Payment owner |
| Provider | `provider` | ENUM('MOCK','TOSS','TOSS_BILLING','KAKAOPAY') | NOT NULL | | | Current executable provider is `TOSS_BILLING` |
| Status | `status` | ENUM('REQUESTED','APPROVED','PROCESSING','SUCCEEDED','FAILED','PENDING_PROVIDER_CONFIRMATION','CANCELLED') | NOT NULL | INDEX(status,created_at) | 'REQUESTED' | Refund workflow state |
| Amount | `amount` | DECIMAL(10,2) | NOT NULL | | | Full or partial refund amount |
| Currency | `currency` | VARCHAR(3) | NOT NULL | | 'KRW' | |
| Reason code | `reason_code` | ENUM('CUSTOMER_REQUEST','DUPLICATE_PAYMENT','PAYMENT_ERROR','SERVICE_ISSUE','ADMIN_ADJUSTMENT','OTHER') | NOT NULL | | | Controlled admin refund reason |
| Reason note | `reason_note` | VARCHAR(500) | NULL | | | Operator note |
| Idempotency key | `idempotency_key` | VARCHAR(100) | NOT NULL | UNIQUE | | Stable provider retry key |
| Provider payment key | `provider_payment_key` | VARCHAR(200) | NOT NULL | | | Toss `paymentKey` or equivalent provider payment identifier |
| Provider refund transaction ID | `provider_refund_transaction_id` | VARCHAR(200) | NULL | | | Toss cancel transaction key when returned |
| Provider payload | `provider_payload` | TEXT | NULL | | | Sanitized cancel response metadata only |
| Failure code | `failure_code` | VARCHAR(100) | NULL | | | Sanitized provider/local code |
| Failure message | `failure_message` | VARCHAR(500) | NULL | | | Sanitized message |
| Requested by | `requested_by` | BIGINT | NULL | FK(users.id) | | Admin actor |
| Approved by | `approved_by` | BIGINT | NULL | FK(users.id) | | Admin actor |
| Executed by | `executed_by` | BIGINT | NULL | FK(users.id) | | Admin actor |
| Approved at | `approved_at` | DATETIME | NULL | | | |
| Executed at | `executed_at` | DATETIME | NULL | | | |
| Created at | `created_at` | DATETIME | NOT NULL | | CURRENT_TIMESTAMP | |
| Updated at | `updated_at` | DATETIME | NOT NULL | | CURRENT_TIMESTAMP | |

- Refund requests are created locally before any provider cancel call.
- The same `idempotency_key` must be reused when retrying the same refund execution.
- `REQUESTED`, `APPROVED`, `PROCESSING`, `SUCCEEDED`, and `PENDING_PROVIDER_CONFIRMATION` rows count against remaining refundable amount.
- Provider cancel success does not automatically mutate `user_subscriptions` or `billing_agreements`; entitlement correction is handled by the separate `payment_entitlement_corrections` workflow.
- `provider_payload` must contain only allowlisted provider response metadata such as payment key, order ID, status, amount, cancel amount, cancel reason, cancel status, canceled timestamp, and cancel transaction key. It must not contain raw card data, raw provider payload, billing keys, auth keys, customer keys, or Toss secret keys.

## 6.6 Payment Entitlement Corrections (`payment_entitlement_corrections`)

| Description | Column | Type | NULL | Constraints | DEFAULT | Notes |
|-------------|--------|------|------|-------------|---------|-------|
| ID | `id` | BIGINT | NOT NULL | PK, AUTO_INCREMENT | | |
| Payment refund | `payment_refund_id` | BIGINT | NOT NULL | FK(payment_refunds.id), INDEX | | Succeeded refund that justifies the correction |
| Subscription payment | `subscription_payment_id` | BIGINT | NOT NULL | FK(subscription_payments.id) | | Original finalized payment |
| Payment order | `payment_order_id` | BIGINT | NOT NULL | FK(payment_orders.id) | | Original order |
| User subscription | `user_subscription_id` | BIGINT | NOT NULL | FK(user_subscriptions.id) | | Local access row to correct |
| User | `user_id` | BIGINT | NOT NULL | FK(users.id), INDEX(user_id,created_at) | | Payment/access owner |
| Provider | `provider` | ENUM('MOCK','TOSS','TOSS_BILLING','KAKAOPAY') | NOT NULL | | | Copied from refund/provider context |
| Status | `status` | ENUM('REQUESTED','APPROVED','PROCESSING','SUCCEEDED','FAILED','CANCELLED') | NOT NULL | INDEX(status,created_at) | 'REQUESTED' | Local correction workflow state |
| Action | `action` | ENUM('SET_SUBSCRIPTION_STATE') | NOT NULL | | 'SET_SUBSCRIPTION_STATE' | Current correction action type |
| Before subscription | `before_subscription_id` | BIGINT | NOT NULL | FK(subscriptions.id) | | Snapshot before execution |
| Before billing cycle | `before_billing_cycle` | ENUM('MONTHLY','YEARLY') | NOT NULL | | | Snapshot before execution |
| Before status | `before_status` | ENUM('ACTIVE','CANCELLED','EXPIRED') | NOT NULL | | | Snapshot before execution |
| Before expires at | `before_expires_at` | DATE | NOT NULL | | | Snapshot before execution |
| Before pending subscription | `before_pending_subscription_id` | BIGINT | NULL | FK(subscriptions.id) | | Snapshot before execution |
| Before pending billing cycle | `before_pending_billing_cycle` | ENUM('MONTHLY','YEARLY') | NULL | | | Snapshot before execution |
| Target subscription | `target_subscription_id` | BIGINT | NOT NULL | FK(subscriptions.id) | | Explicit target plan |
| Target billing cycle | `target_billing_cycle` | ENUM('MONTHLY','YEARLY') | NOT NULL | | | Explicit target billing cycle |
| Target status | `target_status` | ENUM('ACTIVE','CANCELLED','EXPIRED') | NOT NULL | | | Explicit target access status |
| Target expires at | `target_expires_at` | DATE | NOT NULL | | | Explicit target expiration date |
| Clear pending change | `clear_pending_change` | BOOLEAN | NOT NULL | | FALSE | Whether to null out pending plan/cycle |
| Cancel billing agreement | `cancel_billing_agreement` | BOOLEAN | NOT NULL | | FALSE | Local-only cancellation of future automatic charges |
| Before billing agreement status | `before_billing_agreement_status` | ENUM('READY','ACTIVE','SUSPENDED','CANCELLED','EXPIRED') | NULL | | | Snapshot before execution |
| After billing agreement status | `after_billing_agreement_status` | ENUM('READY','ACTIVE','SUSPENDED','CANCELLED','EXPIRED') | NULL | | | Result after execution |
| Reason note | `reason_note` | VARCHAR(500) | NULL | | | Operator note |
| Failure code | `failure_code` | VARCHAR(100) | NULL | | | Reserved for failed local correction tracking |
| Failure message | `failure_message` | VARCHAR(500) | NULL | | | Reserved for failed local correction tracking |
| Requested by | `requested_by` | BIGINT | NULL | FK(users.id) | | Admin actor |
| Approved by | `approved_by` | BIGINT | NULL | FK(users.id) | | Admin actor |
| Executed by | `executed_by` | BIGINT | NULL | FK(users.id) | | Admin actor |
| Approved at | `approved_at` | DATETIME | NULL | | | |
| Executed at | `executed_at` | DATETIME | NULL | | | |
| Created at | `created_at` | DATETIME | NOT NULL | | CURRENT_TIMESTAMP | |
| Updated at | `updated_at` | DATETIME | NOT NULL | | CURRENT_TIMESTAMP | |

- Entitlement correction is created only after a succeeded refund record is selected by an admin operation.
- The system does not infer a previous plan rollback from payment history. Operators must provide the target plan, billing cycle, status, expiration date, pending clear option, and local billing agreement cancellation option.
- Execution mutates `user_subscriptions` and optionally marks the local `billing_agreements` row cancelled. It does not call a provider billing-key delete/cancel API.
- Unexpected execution failure rolls back the local transaction so the approved correction can be retried after investigation.
- This table must never store raw billing keys, raw `authKey`, raw `customerKey`, raw card data, Toss secret keys, or raw provider payloads.

## 6.7 Payment Settlements (`payment_settlements`)

| Description | Column | Type | NULL | Constraints | DEFAULT | Notes |
|-------------|--------|------|------|-------------|---------|-------|
| ID | `id` | BIGINT | NOT NULL | PK, AUTO_INCREMENT | | |
| Source | `source` | ENUM('CSV_MANUAL','TOSS_API','SYSTEM_RECONCILIATION') | NOT NULL | | | Current import source is `CSV_MANUAL`; generated missing-provider rows use `SYSTEM_RECONCILIATION` |
| Provider | `provider` | ENUM('MOCK','TOSS','TOSS_BILLING','KAKAOPAY') | NOT NULL | | | Current expected provider is `TOSS_BILLING` |
| Status | `status` | ENUM('IMPORTED','MATCHED','MISMATCHED','LOCAL_PAYMENT_NOT_FOUND','PROVIDER_SETTLEMENT_NOT_FOUND','IGNORED') | NOT NULL | INDEX(status,created_at) | 'IMPORTED' | Settlement reconciliation workflow state |
| Deduplication key | `deduplication_key` | VARCHAR(64) | NOT NULL | UNIQUE | | Deterministic row key from provider settlement ID or normalized row contents |
| Import batch key | `import_batch_key` | VARCHAR(64) | NOT NULL | | | Batch identifier returned to admin UI |
| Source file name | `source_file_name` | VARCHAR(255) | NULL | | | Uploaded CSV file name or `system-reconciliation` |
| Source row number | `source_row_number` | INT | NULL | | | CSV row number when imported |
| Provider settlement ID | `provider_settlement_id` | VARCHAR(200) | NULL | | | Provider row identifier when available |
| Provider payment key | `provider_payment_key` | VARCHAR(200) | NULL | INDEX | | Toss payment key or equivalent support-safe provider identifier |
| Order ID | `order_id` | VARCHAR(64) | NOT NULL | INDEX | | Merchant order ID |
| Payment order | `payment_order_id` | BIGINT | NULL | FK(payment_orders.id) | | Matched local order when found |
| Subscription payment | `subscription_payment_id` | BIGINT | NULL | FK(subscription_payments.id) | | Matched finalized payment when found |
| User | `user_id` | BIGINT | NULL | FK(users.id) | | Payment owner when resolvable |
| Gross amount | `gross_amount` | DECIMAL(15,2) | NOT NULL | | | Provider gross amount or local payment amount for generated rows |
| Refund amount | `refund_amount` | DECIMAL(15,2) | NOT NULL | | 0 | Succeeded local refund sum or provider evidence |
| Fee amount | `fee_amount` | DECIMAL(15,2) | NOT NULL | | 0 | Provider fee evidence |
| VAT amount | `vat_amount` | DECIMAL(15,2) | NOT NULL | | 0 | Fee/tax evidence |
| Net settlement amount | `net_settlement_amount` | DECIMAL(15,2) | NOT NULL | | | `gross - refund - fee - VAT` under current policy |
| Currency | `currency` | VARCHAR(3) | NOT NULL | | 'KRW' | CSV import validates a 3-letter code |
| Settlement base date | `settlement_base_date` | DATE | NOT NULL | INDEX | | Provider settlement sales/base date or selected scan end date |
| Settlement payout date | `settlement_payout_date` | DATE | NULL | | | Expected/actual provider payout date |
| Provider status | `provider_status` | VARCHAR(100) | NULL | | | Provider status text when present |
| Mismatch reason | `mismatch_reason` | VARCHAR(500) | NULL | | | Human-readable reconciliation reason |
| Operator note | `operator_note` | VARCHAR(500) | NULL | | | Import or ignore note |
| Source payload | `source_payload` | TEXT | NULL | | | Allowlisted source fields only; no raw provider payload |
| Reconciled at | `reconciled_at` | DATETIME | NULL | | | Internal reconciliation timestamp |
| Ignored by | `ignored_by` | BIGINT | NULL | FK(users.id) | | Admin actor who ignored the row |
| Ignored at | `ignored_at` | DATETIME | NULL | | | Ignore timestamp |
| Created at | `created_at` | DATETIME | NOT NULL | | CURRENT_TIMESTAMP | |
| Updated at | `updated_at` | DATETIME | NOT NULL | | CURRENT_TIMESTAMP | |

- Settlement rows are accounting evidence and operator review records. They must not automatically mutate `user_subscriptions`, `billing_agreements`, `payment_orders`, `subscription_payments`, provider state, or refund state.
- `CSV_MANUAL` import requires stable headers and ignores unknown columns. Current implementation accepts CSV; Excel files should be exported to CSV before import.
- `SYSTEM_RECONCILIATION` rows represent local finalized payments with no imported provider settlement evidence for the selected period.
- `source_payload` stores only allowlisted support-safe values such as order ID, provider payment key, amount fields, dates, provider status, and currency. It must not store raw card data, raw provider payload, billing keys, auth keys, customer keys, or Toss secret keys.

## 6.8 Billing Agreements (`billing_agreements`)

| Description | Column | Type | NULL | Constraints | DEFAULT | Notes |
|-------------|--------|------|------|-------------|---------|-------|
| ID | `id` | BIGINT | NOT NULL | PK, AUTO_INCREMENT | | |
| User | `user_id` | BIGINT | NOT NULL | FK(users.id) | | Agreement owner |
| Provider | `provider` | ENUM('TOSS_BILLING','KAKAOPAY') | NOT NULL | UNIQUE(user_id, provider) | | KAKAOPAY reserved for future expansion |
| Status | `status` | ENUM('READY','ACTIVE','SUSPENDED','CANCELLED','EXPIRED') | NOT NULL | INDEX(status,next_billing_at) | 'READY' | Billing agreement lifecycle |
| Provider customer key | `provider_customer_key` | VARCHAR(300) | NOT NULL | UNIQUE(provider, provider_customer_key) | | Server-generated customer key sent to provider |
| Billing key ciphertext | `billing_key_ciphertext` | VARCHAR(1000) | NULL | | | Encrypted billing key; never store plain text |
| Billing key fingerprint | `billing_key_fingerprint` | VARCHAR(128) | NULL | | | Non-secret fingerprint for diagnostics |
| Pay method | `pay_method` | VARCHAR(50) | NULL | | | Example: CARD |
| Masked method | `masked_method` | VARCHAR(100) | NULL | | | Safe display value only |
| Next billing date | `next_billing_at` | DATE | NULL | | | Scheduler target date |
| Last charged at | `last_charged_at` | DATETIME | NULL | | | Last successful automatic charge |
| Failure count | `failure_count` | INT | NOT NULL | | 0 | Retry counter within grace policy |
| Cancelled at | `cancelled_at` | DATETIME | NULL | | | Set when automatic renewal is cancelled |
| Created at | `created_at` | DATETIME | NOT NULL | | CURRENT_TIMESTAMP | |
| Updated at | `updated_at` | DATETIME | NOT NULL | | CURRENT_TIMESTAMP | |

- Billing keys are server-only encrypted credentials.
- User-facing and operator-facing UI may show `status`, `pay_method`, `masked_method`, `next_billing_at`, `failure_count`, and `cancelled_at`.
- Raw billing key material and Toss secret keys must never appear in API responses, logs, screenshots, or documents.
- User-facing subscription cancellation changes the agreement state but retains the encrypted billing key for possible reactivation before `user_subscriptions.expires_at`.
- Provider-level billing agreement cancellation clears issued-key fields and requires payment-method re-registration before future automatic charges.
- Neither cancellation path removes already-paid subscription access before `user_subscriptions.expires_at`.
- Account withdrawal is a separate local-first path: a non-terminal agreement becomes `CANCELLED` before `users.is_deleted=1`, and the ACTIVE subscription is cancelled without creating a refund.
- Withdrawal Provider cleanup runs after commit. Success clears all issued-key fields and `next_billing_at`; failure retains encrypted key material for the deleted-user cleanup retry.
- The cleanup retry query selects only deleted users with `CANCELLED` agreements and nonblank `billing_key_ciphertext`. Deleted users are independently excluded from renewal selection.

## 6.9 Payment Reconciliation Incidents (`payment_reconciliation_incidents`)

| Description | Column | Type | NULL | Constraints | DEFAULT | Notes |
|-------------|--------|------|------|-------------|---------|-------|
| ID | `id` | BIGINT | NOT NULL | PK, AUTO_INCREMENT | | |
| Dedupe key | `dedupe_key` | VARCHAR(255) | NOT NULL | UNIQUE | | Deterministic issue key such as issue type plus order ID |
| Issue type | `issue_type` | ENUM('DONE_ORDER_WITHOUT_PAYMENT','ACTIVE_AGREEMENT_WITHOUT_SUBSCRIPTION','PROVIDER_DONE_LOCAL_NOT_FINALIZED','LOCAL_DONE_PROVIDER_NOT_FOUND','LOCAL_DONE_PROVIDER_NOT_DONE','AMOUNT_MISMATCH','PROVIDER_LOOKUP_FAILED') | NOT NULL | | | Local/provider mismatch category |
| Status | `status` | ENUM('OPEN','ACKNOWLEDGED','RESOLVED','IGNORED') | NOT NULL | INDEX(status,last_detected_at) | 'OPEN' | Operator workflow state |
| Severity | `severity` | ENUM('WARNING','CRITICAL') | NOT NULL | | 'WARNING' | Used for triage and optional email notification |
| Payment order | `payment_order_id` | BIGINT | NULL | FK(payment_orders.id) | | Set when the issue maps to a payment order |
| Billing agreement | `billing_agreement_id` | BIGINT | NULL | FK(billing_agreements.id) | | Set when the issue maps to a billing agreement |
| User | `user_id` | BIGINT | NULL | FK(users.id) | | Support lookup only |
| Order ID | `order_id` | VARCHAR(64) | NULL | INDEX | | Merchant order ID |
| Provider | `provider` | ENUM('MOCK','TOSS','TOSS_BILLING','KAKAOPAY') | NULL | | | |
| Purpose | `purpose` | ENUM('SUBSCRIBE','UPGRADE','RENEWAL','BILLING_AGREEMENT') | NULL | | | |
| Local status | `local_status` | VARCHAR(50) | NULL | | | Local order/agreement status snapshot |
| Provider status | `provider_status` | VARCHAR(50) | NULL | | | Provider lookup status snapshot |
| Local amount | `local_amount` | DECIMAL(10,2) | NULL | | | |
| Provider amount | `provider_amount` | DECIMAL(10,2) | NULL | | | |
| Provider transaction ID | `provider_transaction_id` | VARCHAR(200) | NULL | | | Provider payment key/transaction identifier only |
| Failure code | `failure_code` | VARCHAR(100) | NULL | | | Sanitized provider/local code |
| Failure message | `failure_message` | VARCHAR(500) | NULL | | | Sanitized message |
| Occurrence count | `occurrence_count` | INT | NOT NULL | | 1 | Incremented on repeated detection |
| First detected at | `first_detected_at` | DATETIME | NOT NULL | | | |
| Last detected at | `last_detected_at` | DATETIME | NOT NULL | | | Updated on repeated detection |
| Notified at | `notified_at` | DATETIME | NULL | | | Set when optional operator email notification is sent |
| Resolved at | `resolved_at` | DATETIME | NULL | | | Set for `RESOLVED` or `IGNORED` |
| Resolution note | `resolution_note` | VARCHAR(500) | NULL | | | Operator note |
| Created at | `created_at` | DATETIME | NOT NULL | | CURRENT_TIMESTAMP | |
| Updated at | `updated_at` | DATETIME | NOT NULL | | CURRENT_TIMESTAMP | |

- Repeated detections update the same row by `dedupe_key`.
- `RESOLVED` incidents reopen automatically if the same mismatch appears again.
- `IGNORED` incidents remain ignored while occurrence metadata continues to update.
- Withdrawal billing-key cleanup failures reuse `LOCAL_DONE_PROVIDER_NOT_DONE` with `local_status=CANCELLED`, `provider_status=BILLING_KEY_DELETE_FAILED`, `WARNING` severity, and a dedupe key scoped to `billing_agreement_id`. Provider cleanup success, including `ALREADY_REMOVED_BILLING_KEY`, resolves the matching row.
- This table must never store raw billing keys, raw `authKey`, raw `customerKey`, raw card data, Toss secret keys, or raw provider payloads.

---

## 6.10 Payment Receipts (`payment_receipts`)

| Description | Column | Type | NULL | Constraints | DEFAULT | Notes |
|-------------|--------|------|------|-------------|---------|-------|
| ID | `id` | BIGINT | NOT NULL | PK, AUTO_INCREMENT | | |
| Payment order | `payment_order_id` | BIGINT | NOT NULL | FK(payment_orders.id), UNIQUE(payment_order_id,type) | | Related payment attempt |
| Subscription payment | `subscription_payment_id` | BIGINT | NOT NULL | FK(subscription_payments.id) | | Related finalized subscription payment |
| User | `user_id` | BIGINT | NOT NULL | FK(users.id), INDEX(user_id,created_at) | | Payment owner |
| Provider | `provider` | ENUM('MOCK','TOSS','TOSS_BILLING','KAKAOPAY') | NOT NULL | | | |
| Receipt type | `type` | ENUM('PAYMENT_RECEIPT','CASH_RECEIPT') | NOT NULL | UNIQUE(payment_order_id,type) | | One row per receipt type per order |
| Status | `status` | ENUM('ISSUED','CANCELLED','PARTIAL_CANCELLED','FAILED') | NOT NULL | | 'ISSUED' | Evidence lifecycle; refund/cancel automation remains future work |
| Provider payment key | `provider_payment_key` | VARCHAR(200) | NULL | INDEX | | Toss payment key or equivalent provider transaction ID |
| Receipt key | `receipt_key` | VARCHAR(200) | NULL | | | Cash receipt key when provider returns one |
| Receipt URL | `receipt_url` | VARCHAR(1000) | NULL | | | Provider receipt/cash receipt URL |
| Issued at | `issued_at` | DATETIME | NULL | | | Provider-approved or requested timestamp when available |
| Cancelled at | `cancelled_at` | DATETIME | NULL | | | Reserved for future refund/cash receipt cancellation |
| Evidence payload | `evidence_payload` | TEXT | NULL | | | Minimal sanitized metadata only |
| Created at | `created_at` | DATETIME | NOT NULL | | CURRENT_TIMESTAMP | |
| Updated at | `updated_at` | DATETIME | NOT NULL | | CURRENT_TIMESTAMP | |

- Receipt rows are created after the successful payment transaction commits.
- Missing receipt fields are skipped; charge completion does not depend on provider returning a receipt URL.
- `evidence_payload` must not contain raw billing keys, raw `authKey`, raw `customerKey`, raw card data, Toss secret keys, or raw provider payloads.
- Current supported types are `PAYMENT_RECEIPT` and `CASH_RECEIPT`; tax invoice tracking remains on hold unless ATStudio later approves B2B invoice, bank-transfer, postpaid, or contract purchase payments.

## 6.11 Payment Operation Audit Logs (`payment_operation_audit_logs`)

| Description | Column | Type | NULL | Constraints | DEFAULT | Notes |
|-------------|--------|------|------|-------------|---------|-------|
| ID | `id` | BIGINT | NOT NULL | PK, AUTO_INCREMENT | | |
| Action | `action` | ENUM('RECONCILIATION_INCIDENT_STATUS_UPDATE','RECEIPT_EVIDENCE_CREATED','PAYMENT_REFUND_REQUESTED','PAYMENT_REFUND_APPROVED','PAYMENT_REFUND_PROCESSING','PAYMENT_REFUND_SUCCEEDED','PAYMENT_REFUND_FAILED','PAYMENT_REFUND_PENDING_PROVIDER_CONFIRMATION','PAYMENT_ENTITLEMENT_CORRECTION_REQUESTED','PAYMENT_ENTITLEMENT_CORRECTION_APPROVED','PAYMENT_ENTITLEMENT_CORRECTION_PROCESSING','PAYMENT_ENTITLEMENT_CORRECTION_SUCCEEDED','PAYMENT_ENTITLEMENT_CORRECTION_FAILED','PAYMENT_SETTLEMENT_IMPORTED','PAYMENT_SETTLEMENT_RECONCILED','PAYMENT_SETTLEMENT_IGNORED') | NOT NULL | | | Audit action kind |
| Target type | `target_type` | ENUM('RECONCILIATION_INCIDENT','PAYMENT_RECEIPT','PAYMENT_REFUND','PAYMENT_ENTITLEMENT_CORRECTION','PAYMENT_SETTLEMENT') | NOT NULL | INDEX(target_type,target_id) | | |
| Target ID | `target_id` | BIGINT | NULL | INDEX(target_type,target_id) | | ID in the target table |
| Actor user | `actor_user_id` | BIGINT | NULL | FK(users.id), INDEX(actor_user_id,created_at) | | Admin actor; NULL for system-created logs |
| Target user | `target_user_id` | BIGINT | NULL | FK(users.id) | | Payment owner when resolvable |
| Payment order | `payment_order_id` | BIGINT | NULL | FK(payment_orders.id) | | |
| Subscription payment | `subscription_payment_id` | BIGINT | NULL | FK(subscription_payments.id) | | |
| Reconciliation incident | `reconciliation_incident_id` | BIGINT | NULL | FK(payment_reconciliation_incidents.id) | | |
| Provider | `provider` | ENUM('MOCK','TOSS','TOSS_BILLING','KAKAOPAY') | NULL | | | |
| Order ID | `order_id` | VARCHAR(64) | NULL | INDEX | | Merchant order ID |
| Provider transaction ID | `provider_transaction_id` | VARCHAR(200) | NULL | | | Provider payment key/transaction identifier only |
| Before status | `before_status` | VARCHAR(60) | NULL | | | |
| After status | `after_status` | VARCHAR(60) | NULL | | | |
| Reason code | `reason_code` | VARCHAR(100) | NULL | | | Incident type or receipt type |
| Note | `note` | VARCHAR(500) | NULL | | | Operator/system note |
| Created at | `created_at` | DATETIME | NOT NULL | | CURRENT_TIMESTAMP | |
| Updated at | `updated_at` | DATETIME | NOT NULL | | CURRENT_TIMESTAMP | |

- Audit rows are append-only in the application API surface. No update/delete API exists.
- Current admin mutation coverage is reconciliation incident status update, refund request/approval/execution workflow, refund-linked entitlement correction request/approval/execution workflow, and settlement import/reconcile/ignore workflow.
- System-generated receipt evidence creation logs have `actor_user_id = NULL`.
- Future tax invoice workflows, if reopened for B2B/non-card payment scope, should add new action values rather than overloading the current ones.

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
| Channel URL | `channel_url` | VARCHAR(255) | NOT NULL | | | YouTube channel URL. Validated at app level: host must be `youtube.com` or end with `.youtube.com`. |
| Channel name | `channel_name` | VARCHAR(100) | NOT NULL | | | Display name |
| YouTube handle | `youtube_handle` | VARCHAR(100) | NULL | | | User-facing YouTube handle, e.g. `@channel` |
| YouTube channel ID | `youtube_channel_id` | VARCHAR(100) | NULL | | | Canonical YouTube channel ID, e.g. `UC...` |
| Status | `status` | ENUM('DRAFT','PENDING','EXPORTED','REGISTERED','REVISION_REQUESTED','REJECTED','CANCELLED','REMOVAL_REQUESTED') | NOT NULL | | 'DRAFT' | Whitelist request workflow state |
| Primary flag | `is_primary` | TINYINT(1) | NOT NULL | | 0 | Representative channel for the user |
| Requested at | `requested_at` | DATETIME | NULL | | | User registration request timestamp |
| Exported at | `exported_at` | DATETIME | NULL | | | Admin CSV export timestamp |
| Processed at | `processed_at` | DATETIME | NULL | | | Admin status processing timestamp |
| Removal requested at | `removal_requested_at` | DATETIME | NULL | | | User-requested external removal timestamp |
| Admin note | `admin_note` | VARCHAR(500) | NULL | | | Operator note displayed to user/admin |
| Processed by | `processed_by` | BIGINT | NULL | FK(users.id) | | Admin who processed the current workflow state |
| Created at | `created_at` | DATETIME | NOT NULL | | CURRENT_TIMESTAMP | |
| Updated at | `updated_at` | DATETIME | NOT NULL | | CURRENT_TIMESTAMP | |

- Users may save draft channels without active subscription.
- `subscriptions.max_whitelist_channels` applies when requesting registration, not when saving drafts.
- Plan-counting statuses: `PENDING`, `EXPORTED`, `REGISTERED`, `REVISION_REQUESTED`, `REMOVAL_REQUESTED`.
- Local-only statuses (`DRAFT`, `PENDING`, `REVISION_REQUESTED`, `REJECTED`, `CANCELLED`) can be physically deleted by the user. `EXPORTED`/`REGISTERED` deletion becomes `REMOVAL_REQUESTED`.
- If a physically deleted local-only channel was primary, the service promotes another saved channel as primary when one remains.

## 9.2 Whitelist Export Batches (`whitelist_export_batches`)

| Description | Column | Type | NULL | Constraints | DEFAULT | Notes |
|-------------|--------|------|------|-------------|---------|-------|
| ID | `id` | BIGINT | NOT NULL | PK, AUTO_INCREMENT | | |
| File name | `file_name` | VARCHAR(255) | NOT NULL | | | Generated CSV file name |
| Item count | `item_count` | INT | NOT NULL | | | Number of exported channel rows |
| Exported by | `exported_by` | BIGINT | NULL | FK(users.id) | | Admin who generated the export |
| Note | `note` | VARCHAR(500) | NULL | | | Export note |
| Created at | `created_at` | DATETIME | NOT NULL | | CURRENT_TIMESTAMP | Export created time |
| Updated at | `updated_at` | DATETIME | NOT NULL | | CURRENT_TIMESTAMP | |

## 9.3 Whitelist Export Items (`whitelist_export_items`)

| Description | Column | Type | NULL | Constraints | DEFAULT | Notes |
|-------------|--------|------|------|-------------|---------|-------|
| ID | `id` | BIGINT | NOT NULL | PK, AUTO_INCREMENT | | |
| Batch | `batch_id` | BIGINT | NOT NULL | FK(whitelist_export_batches.id) | | Export batch |
| Channel | `whitelist_channel_id` | BIGINT | NULL | FK(whitelist_channels.id), ON DELETE SET NULL | | Source channel row; snapshots remain even if a locally deletable channel is later deleted |
| Status at export | `status_at_export` | ENUM('DRAFT','PENDING','EXPORTED','REGISTERED','REVISION_REQUESTED','REJECTED','CANCELLED','REMOVAL_REQUESTED') | NOT NULL | | | Status before export mutation |
| User ID snapshot | `user_id_snapshot` | BIGINT | NOT NULL | | | User ID at export time |
| User email snapshot | `user_email_snapshot` | VARCHAR(100) | NOT NULL | | | Included in CSV for external processing |
| User nickname snapshot | `user_nickname_snapshot` | VARCHAR(20) | NOT NULL | | | User nickname at export time |
| Channel name snapshot | `channel_name_snapshot` | VARCHAR(100) | NOT NULL | | | Channel display name at export time |
| YouTube handle snapshot | `youtube_handle_snapshot` | VARCHAR(100) | NULL | | | Handle at export time |
| Channel URL snapshot | `channel_url_snapshot` | VARCHAR(255) | NOT NULL | | | Channel URL at export time |
| YouTube channel ID snapshot | `youtube_channel_id_snapshot` | VARCHAR(100) | NULL | | | Channel ID at export time |
| Plan name snapshot | `plan_name_snapshot` | VARCHAR(30) | NULL | | | Active subscription plan at export time |
| Billing cycle snapshot | `billing_cycle_snapshot` | ENUM('MONTHLY','YEARLY') | NULL | | | Active subscription cycle at export time |
| Requested at snapshot | `requested_at_snapshot` | DATETIME | NULL | | | User request timestamp at export time |
| Exported at snapshot | `exported_at_snapshot` | DATETIME | NOT NULL | | | CSV export timestamp |
| Created at | `created_at` | DATETIME | NOT NULL | | CURRENT_TIMESTAMP | |
| Updated at | `updated_at` | DATETIME | NOT NULL | | CURRENT_TIMESTAMP | |

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
| View count | `view_count` | BIGINT | NOT NULL | | 0 | Incremented on each detail view |
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
| Updated at | `updated_at` | DATETIME | NOT NULL | | CURRENT_TIMESTAMP ON UPDATE | Common column (BaseEntity) |

- One inquiry can have multiple attachments (1:N)

---

## 13.2 Notice Attachments (`notice_attachments`)

> Notice file attachments (public download)

| Description | Column | Type | NULL | Constraints | DEFAULT | Notes |
|-------------|--------|------|------|-------------|---------|-------|
| ID | `id` | BIGINT | NOT NULL | PK, AUTO_INCREMENT | | |
| Notice | `notice_id` | BIGINT | NOT NULL | FK(notices.id) | | |
| Original filename | `original_name` | VARCHAR(255) | NOT NULL | | | Original filename at upload |
| Stored file path | `file_path` | VARCHAR(500) | NOT NULL | | | Server storage path |
| File size | `file_size` | BIGINT | NOT NULL | | | In bytes |
| Created at | `created_at` | DATETIME | NOT NULL | | CURRENT_TIMESTAMP | |
| Updated at | `updated_at` | DATETIME | NOT NULL | | CURRENT_TIMESTAMP ON UPDATE | Common column (BaseEntity) |

- One notice can have multiple attachments (1:N)
- Cascade delete when parent notice is deleted

---

# 14. Albums

## 14.1 Albums (`albums`)

> Admin-curated album collections

| Description | Column | Type | NULL | Constraints | DEFAULT | Notes |
|-------------|--------|------|------|-------------|---------|-------|
| ID | `id` | BIGINT | NOT NULL | PK, AUTO_INCREMENT | | |
| Title | `title` | VARCHAR(100) | NOT NULL | | | |
| Description | `description` | TEXT | | | | |
| Thumbnail | `thumbnail` | VARCHAR(500) | | | | URL |
| Created by | `created_by` | BIGINT | NOT NULL | FK(users.id) | | ADMIN |
| Active flag | `is_active` | TINYINT(1) | NOT NULL | | 1 | Soft delete |
| Like count | `like_count` | BIGINT | NOT NULL | | 0 | Incremented/decremented by album like add/remove |
| Created at | `created_at` | DATETIME | NOT NULL | | CURRENT_TIMESTAMP | |
| Updated at | `updated_at` | DATETIME | NOT NULL | | CURRENT_TIMESTAMP | |

## 14.2 Album-Track Mapping (`album_tracks`)

| Description | Column | Type | NULL | Constraints | DEFAULT | Notes |
|-------------|--------|------|------|-------------|---------|-------|
| Album | `album_id` | BIGINT | NOT NULL | PK, FK(albums.id) | | |
| Track | `track_id` | BIGINT | NOT NULL | PK, FK(tracks.id) | | |
| Track order | `track_order` | INT | NOT NULL | | 0 | |

- Composite PK: (`album_id`, `track_id`)

## 14.3 Album Likes (`album_likes`)

> User likes on albums. Mirrors the `likes` table but targets albums instead of tracks.

| Description | Column | Type | NULL | Constraints | DEFAULT | Notes |
|-------------|--------|------|------|-------------|---------|-------|
| User | `user_id` | BIGINT | NOT NULL | PK, FK(users.id) | | |
| Album | `album_id` | BIGINT | NOT NULL | PK, FK(albums.id) | | |
| Created at | `created_at` | DATETIME | NOT NULL | | CURRENT_TIMESTAMP | |

- Composite PK: (`user_id`, `album_id`)
- On add: `albums.like_count` incremented. On remove: `albums.like_count` decremented (floor 0).

---

# 15. Email / Password Tokens

## 15.1 Email Verification Tokens (`email_verification_tokens`)

> Token-based email verification. Sent on user registration. Valid for 24 hours, single use.

| Description | Column | Type | NULL | Constraints | DEFAULT | Notes |
|-------------|--------|------|------|-------------|---------|-------|
| ID | `id` | BIGINT | NOT NULL | PK, AUTO_INCREMENT | | |
| User | `user_id` | BIGINT | NOT NULL | FK(users.id) | | |
| Token | `token` | VARCHAR(255) | NOT NULL | UNIQUE | | UUID |
| Expiration | `expires_at` | DATETIME | NOT NULL | | | Registration time + 24h |
| Used flag | `used` | TINYINT(1) | NOT NULL | | 0 | Single-use token |
| Created at | `created_at` | DATETIME | NOT NULL | | CURRENT_TIMESTAMP | |
| Updated at | `updated_at` | DATETIME | NOT NULL | | CURRENT_TIMESTAMP | |

## 15.2 Password Reset Tokens (`password_reset_tokens`)

> Token-based password reset. Sent on forgot-password request. Valid for 1 hour, single use.

| Description | Column | Type | NULL | Constraints | DEFAULT | Notes |
|-------------|--------|------|------|-------------|---------|-------|
| ID | `id` | BIGINT | NOT NULL | PK, AUTO_INCREMENT | | |
| User | `user_id` | BIGINT | NOT NULL | FK(users.id) | | |
| Token | `token` | VARCHAR(255) | NOT NULL | UNIQUE | | UUID |
| Expiration | `expires_at` | DATETIME | NOT NULL | | | Request time + 1h |
| Used flag | `used` | TINYINT(1) | NOT NULL | | 0 | Single-use token |
| Created at | `created_at` | DATETIME | NOT NULL | | CURRENT_TIMESTAMP | |
| Updated at | `updated_at` | DATETIME | NOT NULL | | CURRENT_TIMESTAMP | |

---

# 16. Site Configuration

## 16.1 Site Settings (`site_settings`)

> Admin-managed key-value configuration store. Used for dynamic content that must be editable without code deployment (e.g., company certification guide text).

| Description | Column | Type | NULL | Constraints | DEFAULT | Notes |
|-------------|--------|------|------|-------------|---------|-------|
| ID | `id` | BIGINT | NOT NULL | PK, AUTO_INCREMENT | | |
| Setting key | `setting_key` | VARCHAR(100) | NOT NULL | UNIQUE | | Logical identifier (e.g., `company_cert_guide`) |
| Setting value | `setting_value` | TEXT | NOT NULL | | | Content up to ~65 535 bytes |
| Created at | `created_at` | DATETIME | NOT NULL | | CURRENT_TIMESTAMP | |
| Updated at | `updated_at` | DATETIME | NOT NULL | | CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP | |

- Upsert pattern: backend uses `save()` after `findBySettingKey()` (or `saveAndFlush` with UNIQUE key conflict).
- No FK references to other tables; standalone configuration store.
- API: `GET /api/settings/{key}` [PUBLIC], `PUT /api/admin/settings/{key}` [ADMIN].

---

# Table Relationship Diagram

```
users ─┬─< social_accounts
       ├─< user_subscriptions ──> subscriptions
       ├─< billing_agreements
       ├─< payment_orders ──> subscriptions
       ├─< subscription_payments ──> subscriptions
       ├─< payment_refunds ──> subscription_payments / payment_orders
       ├─< payment_entitlement_corrections ──> payment_refunds / user_subscriptions
       ├─< payment_settlements ──> payment_orders / subscription_payments
       ├─< payment_reconciliation_incidents ──> payment_orders / billing_agreements
       ├─< payment_receipts ──> payment_orders / subscription_payments
       ├─< payment_operation_audit_logs ──> payment_orders / subscription_payments / payment_reconciliation_incidents / payment_refunds / payment_entitlement_corrections / payment_settlements(target_id)
       ├─< company_certifications
       ├─< track_downloads ──> tracks
       ├─< play_histories ──> tracks
       ├─< likes ──> tracks
       ├─< album_likes ──> albums
       ├─< download_queue ──> tracks
       ├─< whitelist_channels ─< whitelist_export_items >─ whitelist_export_batches
       ├─< licenses ──> tracks
       ├─< playlists ─< playlist_tracks ──> tracks
       ├─< albums ─< album_tracks ──> tracks
       ├─< questions ─┬─< answers
       │              └─< question_attachments
       ├─< email_verification_tokens
       ├─< password_reset_tokens
       └─< notices (ADMIN only)

tracks ─< track_tags ──> tags

site_settings (standalone — no FK)
```

---

# Complete Table List (39 Tables)

| # | Table Name | Description | Type |
|---|------------|-------------|------|
| 1 | `users` | Users | Master |
| 2 | `social_accounts` | Social login | Master |
| 3 | `subscriptions` | Subscription plan definitions | Master |
| 4 | `user_subscriptions` | User subscription status | Transaction |
| 5 | `company_certifications` | Company certification | Transaction |
| 6 | `company_certification_documents` | Company certification document metadata | Transaction |
| 7 | `tracks` | Audio tracks | Master |
| 8 | `tags` | Tags | Master |
| 9 | `track_tags` | Track-tag mapping | Mapping |
| 10 | `playlists` | Playlists | Master |
| 11 | `playlist_tracks` | Playlist-track mapping | Mapping |
| 12 | `track_downloads` | Download history | Log |
| 13 | `play_histories` | Play history | Log |
| 14 | `billing_agreements` | Recurring billing agreement credentials and state | Transaction |
| 15 | `payment_orders` | Payment attempt ledger | Transaction |
| 16 | `subscription_payments` | Subscription payment records | Transaction |
| 17 | `payment_refunds` | Admin refund request/approval/execution ledger | Transaction |
| 18 | `payment_entitlement_corrections` | Admin refund-linked entitlement correction ledger | Transaction |
| 19 | `payment_settlements` | Admin settlement import/reconciliation ledger | Transaction |
| 20 | `payment_reconciliation_incidents` | Payment reconciliation incident workflow | Transaction |
| 21 | `payment_receipts` | Payment receipt/cash receipt evidence | Transaction |
| 22 | `payment_operation_audit_logs` | Payment operation audit trail | Log |
| 23 | `likes` | Track likes | Mapping |
| 24 | `album_likes` | Album likes | Mapping |
| 25 | `download_queue` | Download queue | Mapping |
| 26 | `whitelist_channels` | Whitelist channels | Master |
| 27 | `whitelist_export_batches` | Admin whitelist CSV export batches | Transaction |
| 28 | `whitelist_export_items` | Admin whitelist CSV export item snapshots | Transaction |
| 29 | `questions` | Inquiries | Transaction |
| 30 | `answers` | Inquiry answers | Transaction |
| 31 | `licenses` | Track usage licenses | Transaction |
| 32 | `notices` | Notices | Master |
| 33 | `question_attachments` | Inquiry attachments | Transaction |
| 34 | `notice_attachments` | Notice attachments | Transaction |
| 35 | `albums` | Curated albums | Master |
| 36 | `album_tracks` | Album-track mapping | Mapping |
| 37 | `email_verification_tokens` | Email verification tokens | Transaction |
| 38 | `password_reset_tokens` | Password reset tokens | Transaction |
| 39 | `site_settings` | Site configuration key-value store | Master |

Total **39 tables**
