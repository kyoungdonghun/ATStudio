---
version: 28.4
last_updated: 2026-08-09
project: ATS
owner: SA
category: design
status: confirmed
dependencies:
  - path: ../../src/main/java/com/atstudio/atstudio/controller/
    reason: Authoritative method-level mapping source
  - path: ../../src/main/java/com/atstudio/atstudio/config/SecurityConfig.java
    reason: Authoritative route authorization source
  - path: ../../frontend/src/router/index.tsx
    reason: Active SPA route consumers
  - path: db-schema.md
    reason: Current persistence contract
---

# ATStudio API Specification v28.4

## Current Contract

The current V1 backend exposes **144 method-level mappings across 25 controller
classes**. This count is derived from the current Java source by counting
method-level `@GetMapping`, `@PostMapping`, `@PutMapping`,
`@PatchMapping`, and `@DeleteMapping` annotations. Class-level
`@RequestMapping` declarations are not counted.

| Verb | Count |
|---|---:|
| GET | 69 |
| POST | 41 |
| PUT | 20 |
| DELETE | 14 |
| PATCH | 0 |
| **Total** | **144** |

`SecurityConfig` is authoritative for authorization. Controller annotations are
authoritative for paths and verbs. OpenAPI output generated from the running
application is authoritative for request and response schemas.

## V1 Boundaries

- The React SPA is the only active UI. `SpaForwardController` provides SPA
  deep-link forwarding; it is not an SSR or Thymeleaf compatibility layer.
- Public Listening streams the complete active Track through
  `GET /api/tracks/{trackId}/stream`. Official Download remains a separate,
  authorized path with License, quota, history, and atomic count behavior.
- Play History is browser-local under `localStorage` key `playHistory`.
  There is no server Play History API or persistence contract.
- Download History uses `/api/downloads/history`; there is no Download Queue
  API or persistence contract.
- Subscription payment is card recurring-only through Toss billing agreement
  endpoints. Persisted provider identity is `TOSS`.
- The recurring, status-lookup, and refund provider interfaces remain
  provider-neutral extension boundaries. No second provider is active in V1.
- Direct user subscription creation and legacy payment prepare/confirm/cancel
  contracts do not exist.
- Direct ADMIN update/cancel mappings under `/api/user-subscriptions/{id}` are
  retired. General local entitlement correction uses the explicit ADMIN
  preview, request, approval, and execution workflow and never charges or
  refunds through a payment provider.
- Each correction mutation pessimistically reloads the actor after its existing
  domain/correction locks and immediately before changing state. Role-change,
  ADMIN-withdrawal, and correction request/approval/execution rejections create
  minimal audits in an independent transaction without replacing the API's
  original stable business error.
- Rejection audits retain stable action, target, actor when available, error
  code, and bounded state while persisting a null `reasonNote`. Required
  operator text remains in successful role-change audit and the authoritative
  correction workflow/success context.
- Existing-Track audio analysis is exposed only as a read-only ADMIN dry-run.
  Applying a duration backfill remains a separately approved operation.

## Controller Inventory

| Controller | Mappings | Current boundary |
|---|---:|---|
| `AdminPaymentController` | 24 | ADMIN payment ledgers, incidents, settlement, refund, and entitlement correction |
| `AdminSettingController` | 1 | ADMIN site-setting upsert |
| `AdminStatsController` | 1 | ADMIN dashboard statistics |
| `AdminTrackAudioAnalysisController` | 1 | ADMIN read-only existing-Track audio-analysis dry-run |
| `AdminUserSubscriptionCorrectionController` | 7 | ADMIN local subscription correction workflow |
| `AdminWhitelistChannelController` | 4 | ADMIN whitelist review and export |
| `AlbumController` | 8 | Public album reads and ADMIN mutations |
| `AuthController` | 7 | Login, logout, refresh, social auth, email and password flows |
| `CompanyCertificationController` | 7 | BUSINESS submission and ADMIN review/document access |
| `DownloadController` | 2 | Current user's download history and downloaded Track IDs |
| `LicenseController` | 4 | User and ADMIN License reads |
| `LikeController` | 6 | Track and album likes |
| `NoticeController` | 6 | Public notice reads and ADMIN mutations |
| `PaymentController` | 4 | USER-only recurring billing agreement lifecycle |
| `PlaylistController` | 9 | Subscriber playlist CRUD and Track membership |
| `QuestionController` | 7 | Inquiry, answer, attachment, status, and deletion |
| `SettingController` | 1 | Public site-setting read |
| `SpaForwardController` | 1 | SPA deep-link forwarding |
| `SubscriptionController` | 3 | Public active plans and ADMIN all-plan read |
| `TagController` | 5 | Public reads and ADMIN mutations |
| `TrackController` | 10 | Public Track reads/listening/batch hydration and protected create/update/download/admin reads |
| `UserController` | 9 | Registration, profile, password, withdrawal, and ADMIN user operations |
| `UserSubscriptionController` | 5 | My subscription lifecycle plus ADMIN list read |
| `UtilController` | 6 | Availability, download count, change preview, and public capabilities |
| `WhitelistChannelController` | 6 | User whitelist draft/request/primary lifecycle |

## Route Inventory

### Admin Payment (24)

- `GET /api/admin/payments/orders`
- `GET /api/admin/payments/billing-agreements`
- `GET /api/admin/payments/subscription-payments`
- `GET /api/admin/payments/reconciliation`
- `GET /api/admin/payments/reconciliation-incidents`
- `PUT /api/admin/payments/reconciliation-incidents/{incidentId}/status`
- `GET /api/admin/payments/receipts`
- `GET /api/admin/payments/operation-audit-logs`
- `GET /api/admin/payments/refund-preview/{subscriptionPaymentId}`
- `GET /api/admin/payments/refunds`
- `GET /api/admin/payments/refunds/{refundId}`
- `POST /api/admin/payments/refunds`
- `POST /api/admin/payments/refunds/{refundId}/approve`
- `POST /api/admin/payments/refunds/{refundId}/execute`
- `POST /api/admin/payments/entitlement-correction-preview`
- `GET /api/admin/payments/entitlement-corrections`
- `GET /api/admin/payments/entitlement-corrections/{correctionId}`
- `POST /api/admin/payments/entitlement-corrections`
- `POST /api/admin/payments/entitlement-corrections/{correctionId}/approve`
- `POST /api/admin/payments/entitlement-corrections/{correctionId}/execute`
- `GET /api/admin/payments/settlements`
- `POST /api/admin/payments/settlements/import`
- `POST /api/admin/payments/settlements/reconcile`
- `PUT /api/admin/payments/settlements/{settlementId}/ignore`

### Settings, Dashboard, and Whitelist Admin (6)

- `PUT /api/admin/settings/{key}`
- `GET /api/admin/stats`
- `GET /api/admin/whitelist-channels`
- `PUT /api/admin/whitelist-channels/{channelId}/status`
- `POST /api/admin/whitelist-channels/export`
- `GET /api/admin/whitelist-channels/exports/{batchID}`

### ADMIN Subscription Correction and Track Analysis (8)

- `POST /api/admin/user-subscription-corrections/preview`
- `GET|POST /api/admin/user-subscription-corrections`
- `GET /api/admin/user-subscription-corrections/open`
- `GET /api/admin/user-subscription-corrections/{correctionId}`
- `POST /api/admin/user-subscription-corrections/{correctionId}/approve`
- `POST /api/admin/user-subscription-corrections/{correctionId}/execute`
- `GET /api/admin/tracks/audio-analysis/dry-run`

### Albums, Tracks, Tags, and Playlists (32)

- `GET|POST /api/albums`
- `GET|PUT|DELETE /api/albums/{id}`
- `POST|PUT /api/albums/{id}/tracks`
- `DELETE /api/albums/{id}/tracks/{trackId}`
- `GET|POST /api/tracks`
- `POST /api/tracks/batch`
- `GET|PUT|DELETE /api/tracks/{trackId}`
- `GET /api/tracks/{trackId}/stream`
- `GET /api/tracks/{trackId}/download`
- `GET /api/tracks/admin`
- `GET /api/tracks/admin/{trackId}`
- `GET|POST /api/tags`
- `PUT|DELETE /api/tags/{tagId}`
- `GET /api/tags/available`
- `GET|POST /api/playlists`
- `GET|PUT|DELETE /api/playlists/{playlistId}`
- `POST|PUT /api/playlists/{playlistId}/tracks`
- `DELETE /api/playlists/{playlistId}/tracks/{trackId}`
- `POST /api/playlists/{playlistId}/tracks/batch`

### Authentication and Users (16)

- `POST /api/auth/login`
- `POST /api/auth/logout`
- `POST /api/auth/refresh`
- `POST /api/auth/social/{provider}`
- `GET /api/auth/verify-email`
- `POST /api/auth/forgot-password`
- `POST /api/auth/reset-password`
- `GET|POST /api/users`
- `GET|PUT /api/users/{userId}`
- `GET|PUT|DELETE /api/users/me`
- `PUT /api/users/me/complete-profile`
- `PUT /api/users/me/password`

`GET /api/users` returns paginated `UserListItemResponse` rows with exactly
`id`, `nickname`, `email`, `userType`, `role`, `isVerified`, and `createdAt`.
`GET|PUT /api/users/{userId}` returns `UserDetailResponse`, which additionally
contains `phonePersonal`, `phoneCompany`, `job`, and `companyName`. ADMIN role
assignment accepts only backend roles `USER` and `ADMIN`; frontend-only `GUEST`
is never an assignable wire value.

### Subscription and Recurring Payment (12)

- `GET /api/subscriptions`
- `GET /api/subscriptions/{subscriptionId}`
- `GET /api/subscriptions/admin`
- `POST /api/payments/billing-agreements/prepare`
- `POST /api/payments/billing-agreements/confirm`
- `GET|DELETE /api/payments/billing-agreements/me`
- `GET /api/user-subscriptions`
- `GET|PUT|DELETE /api/user-subscriptions/me`
- `POST /api/user-subscriptions/me/reactivate`

### Download, License, and Likes (12)

- `GET /api/downloads/history`
- `GET /api/downloads/history/track-ids`
- `GET /api/licenses/me`
- `GET /api/licenses/{licenseId}`
- `GET /api/users/{userId}/licenses`
- `GET /api/users/{userId}/licenses/{licenseId}`
- `GET /api/likes`
- `POST|DELETE /api/likes/{trackId}`
- `GET /api/likes/albums`
- `POST|DELETE /api/likes/albums/{albumId}`

### Questions and Notices (13)

- `GET|POST /api/questions`
- `GET|DELETE /api/questions/{questionId}`
- `POST /api/questions/{questionId}/answers`
- `GET /api/questions/{questionId}/attachments/{attachmentId}`
- `PUT /api/questions/{questionId}/status`
- `GET|POST /api/notices`
- `GET|PUT|DELETE /api/notices/{noticeId}`
- `GET /api/notices/{noticeId}/attachments/{attachmentId}`

### Company Certification and User Whitelist (13)

- `GET|POST /api/company-certifications`
- `GET|PUT /api/company-certifications/{certificationId}`
- `GET /api/company-certifications/{certificationId}/documents/{documentId}`
- `GET /api/company-certifications/me`
- `POST /api/company-certifications/me/documents`
- `GET|POST /api/whitelist-channels`
- `PUT|DELETE /api/whitelist-channels/{channelId}`
- `PUT /api/whitelist-channels/{channelId}/primary`
- `POST /api/whitelist-channels/{channelId}/request`

### Utilities and SPA (8)

- `GET /api/utils/check-email`
- `GET /api/utils/check-phone`
- `GET /api/utils/check-nickname`
- `GET /api/utils/download-count`
- `GET /api/utils/subscription-change-preview`
- `GET /api/utils/public-capabilities`
- `GET /api/settings/{key}`
- `GET /{path:^(?!api|uploads|swagger-ui|v3|oauth2|assets|.*\\..*).*$}/**`

## Configuration and Data Rules

- Runtime schema validation uses `spring.jpa.hibernate.ddl-auto=validate`.
- The committed base configuration does not import ignored local configuration.
  Local configuration is loaded only when explicitly supplied by the operator.
- `src/main/resources/schema.sql` and `seed.sql` are fresh-database inputs,
  not migration scripts.
- API examples and generated OpenAPI output must serialize payment provider as
  `TOSS`.
- Paginated application responses use `dataList` plus `pageInfo`; non-paginated
  collection responses such as Tag reads and PlayableTrack hydration use
  `dataList` without inventing a `content` field.
- Public Track search accepts `page >= 1` and `1 <= size <= 100`. Invalid
  pagination returns 400 `INVALID_ARGUMENT` before repository access, while
  `pageInfo.page` remains 1-based.
- `genre`, `mood`, `instrument`, and `usage` search values are repeated query
  parameters. Values are canonicalized, de-duplicated, and combined with AND
  semantics within and across Tag types. Commas and `#` remain part of one Tag
  value rather than acting as a CSV delimiter.
- `POST /api/tracks/batch` is public, accepts 1 to 100 positive Track IDs,
  de-duplicates in first-requested-ID order, returns active Tracks only, and
  preserves that requested order in `dataList`.
- Playlist list counts, detail rows, and owner reorder requests use active Track
  memberships only. Reorder payloads contain every visible active Track exactly
  once with zero-based contiguous orders `0..n-1`; inactive membership rows
  remain persisted and are assigned deterministic orders after the active rows.
- Public Album `trackCount`, detail rows, and `trackCount` sorting use active
  Track memberships only. All-membership counts used by administrative
  mutation paths remain separate.
- The audio-analysis dry-run is ordered by Track ID, accepts `page >= 1` and
  `1 <= size <= 100`, and returns report rows only. It exposes no storage key
  and has no update/backfill side effect.

## Verification

Recount from source:

```powershell
$controllers = Get-ChildItem src/main/java/com/atstudio/atstudio/controller -Filter *.java
($controllers | Select-String '^\s*@(Get|Post|Put|Patch|Delete)Mapping\b').Count
```

Expected result: `144`.
