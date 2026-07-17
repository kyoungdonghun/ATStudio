---
version: 27.0
last_updated: 2026-07-17
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

# ATStudio API Specification v27

## Current Contract

The current V1 backend exposes **137 method-level mappings across 23 controller
classes**. This count is derived from the current Java source by counting
method-level `@GetMapping`, `@PostMapping`, `@PutMapping`,
`@PatchMapping`, and `@DeleteMapping` annotations. Class-level
`@RequestMapping` declarations are not counted.

| Verb | Count |
|---|---:|
| GET | 65 |
| POST | 36 |
| PUT | 21 |
| DELETE | 15 |
| PATCH | 0 |
| **Total** | **137** |

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
- ADMIN subscription update and cancel mappings remain documented emergency
  operations.

## Controller Inventory

| Controller | Mappings | Current boundary |
|---|---:|---|
| `AdminPaymentController` | 24 | ADMIN payment ledgers, incidents, settlement, refund, and entitlement correction |
| `AdminSettingController` | 1 | ADMIN site-setting upsert |
| `AdminStatsController` | 1 | ADMIN dashboard statistics |
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
| `TrackController` | 9 | Public Track reads/listening and protected create/update/download/admin reads |
| `UserController` | 9 | Registration, profile, password, withdrawal, and ADMIN user operations |
| `UserSubscriptionController` | 7 | My subscription lifecycle plus ADMIN emergency list/update/cancel |
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

### Albums, Tracks, Tags, and Playlists (31)

- `GET|POST /api/albums`
- `GET|PUT|DELETE /api/albums/{id}`
- `POST|PUT /api/albums/{id}/tracks`
- `DELETE /api/albums/{id}/tracks/{trackId}`
- `GET|POST /api/tracks`
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

### Subscription and Recurring Payment (14)

- `GET /api/subscriptions`
- `GET /api/subscriptions/{subscriptionId}`
- `GET /api/subscriptions/admin`
- `POST /api/payments/billing-agreements/prepare`
- `POST /api/payments/billing-agreements/confirm`
- `GET|DELETE /api/payments/billing-agreements/me`
- `GET /api/user-subscriptions`
- `GET|PUT|DELETE /api/user-subscriptions/me`
- `POST /api/user-subscriptions/me/reactivate`
- `PUT|DELETE /api/user-subscriptions/{id}`

The last two `{id}` operations are ADMIN emergency controls. They are not a
general user checkout path.

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

## Verification

Recount from source:

```powershell
$controllers = Get-ChildItem src/main/java/com/atstudio/atstudio/controller -Filter *.java
($controllers | Select-String '^\s*@(Get|Post|Put|Patch|Delete)Mapping\b').Count
```

Expected result: `137`.
