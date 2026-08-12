# WI-20260809-ATS-019 Active Surface Inventory

## 1. Baseline and Counting Contract

| Item                                |                     Verified value | Source                                                    |
| ----------------------------------- | ---------------------------------: | --------------------------------------------------------- |
| Branch                              | `codex/v1-release-rehearsal-fixes` | `git status --short --branch`                             |
| Commit                              |                          `e343c20` | `git rev-parse --short HEAD`                              |
| Path-bearing React route objects    |                                 56 | TypeScript AST walk of `frontend/src/router/index.tsx`    |
| Admin index redirect                |                                  1 | `frontend/src/router/index.tsx:215`                       |
| Routable declarations               |                                 57 | 56 paths plus the index redirect                          |
| Lazy route-level page components    |                                 53 | TypeScript AST walk of `lazyPage(...)` declarations       |
| Distinct visual page UIs            |                                 53 | One per lazy page; checkout callback paths reuse one page |
| Frontend API source modules         |                                 19 | Non-test `.ts` files in `frontend/src/api`                |
| Controller files with HTTP mappings |                                 25 | Mapping annotation scan under the controller package      |
| Method-level HTTP mappings          |                                144 | Spring method-mapping annotation scan                     |
| Scheduled methods                   |                                  6 | `@Scheduled` scan under backend source                    |

This WI inventories declarations. It does not claim that every declaration is
fully usable, correctly authorized, documented semantically, or browser-tested.
Those are downstream acceptance-matrix concerns.

## 2. Active React Route Inventory

### 2.1 Public Routes

| Route                | Page                   | Guard  |
| -------------------- | ---------------------- | ------ |
| `/`                  | `HomePage`             | Public |
| `/tracks`            | `TrackListPage`        | Public |
| `/tracks/:trackId`   | `TrackDetailPage`      | Public |
| `/albums`            | `AlbumListImagePage`   | Public |
| `/albums/list`       | `AlbumListPage`        | Public |
| `/albums/:albumId`   | `AlbumDetailPage`      | Public |
| `/subscriptions`     | `SubscriptionPlanPage` | Public |
| `/notices`           | `NoticeListPage`       | Public |
| `/notices/:noticeId` | `NoticeDetailPage`     | Public |

### 2.2 Authentication Routes

| Route                     | Page                        | Guard                    |
| ------------------------- | --------------------------- | ------------------------ |
| `/login`                  | `LoginPage`                 | Public                   |
| `/signup`                 | `SignupPage`                | Public                   |
| `/email-verify`           | `EmailVerifyPage`           | Public callback          |
| `/password-reset`         | `PasswordResetPage`         | Public token flow        |
| `/social-login/:provider` | `SocialLoginPage`           | Public provider callback |
| `/complete-profile`       | `SocialCompleteProfilePage` | Authenticated USER+      |

### 2.3 Member and Subscriber Routes

| Route                             | Page                      | Guard                                    |
| --------------------------------- | ------------------------- | ---------------------------------------- |
| `/playlists`                      | `PlaylistListPage`        | Service-enabled subscriber               |
| `/playlists/:playlistId`          | `PlaylistDetailPage`      | Service-enabled subscriber               |
| `/playlists/:playlistId/edit`     | `PlaylistEditPage`        | Service-enabled subscriber               |
| `/profile`                        | `ProfilePage`             | Authenticated USER+                      |
| `/likes`                          | `LikeListPage`            | Authenticated USER+                      |
| `/play-history`                   | `PlayHistoryPage`         | Authenticated USER+; browser-local state |
| `/licenses`                       | `LicenseListPage`         | Authenticated USER+                      |
| `/licenses/:licenseId`            | `LicenseDetailPage`       | Authenticated USER+                      |
| `/downloads`                      | `DownloadHistoryPage`     | Service-enabled subscriber               |
| `/subscriptions/checkout`         | `SubscriptionPaymentPage` | USER-only payment route                  |
| `/subscriptions/checkout/success` | `SubscriptionPaymentPage` | USER-only callback reuse                 |
| `/subscriptions/checkout/fail`    | `SubscriptionPaymentPage` | USER-only callback reuse                 |
| `/subscriptions/manage`           | `SubscriptionManagePage`  | Authenticated USER+                      |
| `/whitelist-channels`             | `WhitelistChannelPage`    | Authenticated USER+                      |
| `/company-certification/apply`    | `CompanyCertApplyPage`    | BUSINESS USER-only                       |
| `/company-certification/status`   | `CompanyCertStatusPage`   | BUSINESS USER-only                       |
| `/questions`                      | `QuestionListPage`        | Authenticated USER+; ADMIN redirects     |
| `/questions/new`                  | `QuestionCreatePage`      | Authenticated USER+                      |
| `/questions/:questionId`          | `QuestionDetailPage`      | Authenticated USER+                      |

The 19 route paths above render 17 distinct pages because the three checkout
paths reuse `SubscriptionPaymentPage`.

### 2.4 Error Routes

| Route    | Page              | Guard           |
| -------- | ----------------- | --------------- |
| `/error` | `ServerErrorPage` | Public          |
| `*`      | `NotFoundPage`    | Public fallback |

### 2.5 Admin Layout and Creator/Admin Content Routes

The `/admin` parent is ADMIN-only and renders `AdminLayout`. Its index route
redirects to `dashboard` and is not a distinct visual page.

| Effective route               | Page              | Navigation status |
| ----------------------------- | ----------------- | ----------------- |
| `/admin/tracks/upload`        | `TrackUploadPage` | Sidebar entry     |
| `/admin/tracks/:trackId/edit` | `TrackEditPage`   | Contextual only   |
| `/admin/albums`               | `AlbumManagePage` | Sidebar entry     |
| `/admin/albums/new`           | `AlbumCreatePage` | Contextual only   |
| `/admin/albums/:albumId/edit` | `AlbumEditPage`   | Contextual only   |

### 2.6 Admin Operation Routes

| Effective route                 | Page                         | Navigation status |
| ------------------------------- | ---------------------------- | ----------------- |
| `/admin/dashboard`              | `DashboardPage`              | Sidebar entry     |
| `/admin/users`                  | `UserManagePage`             | Sidebar entry     |
| `/admin/subscriptions`          | `SubscriptionManagePage`     | Sidebar entry     |
| `/admin/licenses`               | `LicenseManagePage`          | Sidebar entry     |
| `/admin/questions`              | `QuestionManagePage`         | Sidebar entry     |
| `/admin/company-certifications` | `CompanyCertManagePage`      | Sidebar entry     |
| `/admin/tags`                   | `TagManagePage`              | Sidebar entry     |
| `/admin/track-manage`           | `TrackManagePage`            | Sidebar entry     |
| `/admin/user-subscriptions`     | `UserSubscriptionManagePage` | Sidebar entry     |
| `/admin/payments`               | `PaymentOperationsPage`      | Sidebar entry     |
| `/admin/whitelist-channels`     | `WhitelistChannelManagePage` | Sidebar entry     |
| `/admin/notices/new`            | `NoticeCreatePage`           | Sidebar entry     |
| `/admin/notices/:noticeId/edit` | `NoticeEditPage`             | Contextual only   |
| `/admin/settings`               | `SiteSettingsPage`           | Sidebar entry     |

## 3. Navigation and Guard Inventory

| Surface                       | Declared entries | Current behavior source                    |
| ----------------------------- | ---------------: | ------------------------------------------ |
| Public header navigation      |                5 | `Header.tsx` `PUBLIC_NAV_ITEMS`            |
| Authenticated USER navigation |                5 | `Header.tsx` `USER_NAV_ITEMS`              |
| ADMIN header navigation       |                2 | `Header.tsx` `ADMIN_NAV_ITEMS`             |
| ADMIN sidebar navigation      |               15 | `AdminLayout.tsx` `MENU_ITEMS`             |
| Context-only admin routes     |                4 | Track edit, Album create/edit, Notice edit |

Guard contracts discovered in `frontend/src/router/index.tsx`:

| Guard helper        | Contract requiring browser verification                   |
| ------------------- | --------------------------------------------------------- |
| `authRequired`      | `ProtectedRoute` with minimum role USER                   |
| `subscriberOnly`    | `SubscriberRoute` service-enabled subscription check      |
| `adminOnly`         | `ProtectedRoute` with minimum role ADMIN                  |
| `userPaymentOnly`   | Exact USER role; ADMIN redirects to `/admin/payments`     |
| `businessOnly`      | Exact USER role plus BUSINESS user type                   |
| `/questions` loader | Local user snapshot redirects ADMIN to `/admin/questions` |

## 4. Frontend API Module Inventory

Nineteen non-test API modules are active candidates. Module presence alone is
not proof that every exported function has a current UI consumer.

| Feature family                   | Modules                                                   |
| -------------------------------- | --------------------------------------------------------- |
| Shared transport and error state | `client.ts`, `loadError.ts`                               |
| Authentication and account       | `auth.ts`                                                 |
| Catalog and media                | `tracks.ts`, `albums.ts`, `tags.ts`                       |
| Engagement                       | `likes.ts`, `playlists.ts`                                |
| Subscriber records               | `downloads.ts`, `licenses.ts`, `questions.ts`             |
| Subscription and billing         | `subscriptions.ts`, `userSubscriptions.ts`, `payments.ts` |
| Business operations              | `companyCerts.ts`, `whitelistChannels.ts`                 |
| Site content/settings            | `notices.ts`, `settings.ts`                               |
| Consolidated ADMIN operations    | `admin.ts`                                                |

WI-020 must map individual exported functions to rendered controls, mutation
states, and expected API responses. This WI deliberately does not infer that
mapping from filenames.

## 5. Backend Controller Inventory

| Controller                                  | Base path                                  | Mappings |
| ------------------------------------------- | ------------------------------------------ | -------: |
| `AdminPaymentController`                    | `/api/admin/payments`                      |       24 |
| `AdminSettingController`                    | `/api/admin/settings`                      |        1 |
| `AdminStatsController`                      | `/api/admin`                               |        1 |
| `AdminTrackAudioAnalysisController`         | `/api/admin/tracks/audio-analysis`         |        1 |
| `AdminUserSubscriptionCorrectionController` | `/api/admin/user-subscription-corrections` |        7 |
| `AdminWhitelistChannelController`           | `/api/admin/whitelist-channels`            |        4 |
| `AlbumController`                           | `/api/albums`                              |        8 |
| `AuthController`                            | `/api/auth`                                |        7 |
| `CompanyCertificationController`            | `/api/company-certifications`              |        7 |
| `DownloadController`                        | `/api/downloads`                           |        2 |
| `LicenseController`                         | Method-level paths                         |        4 |
| `LikeController`                            | `/api/likes`                               |        6 |
| `NoticeController`                          | `/api/notices`                             |        6 |
| `PaymentController`                         | `/api/payments`                            |        4 |
| `PlaylistController`                        | `/api/playlists`                           |        9 |
| `QuestionController`                        | `/api/questions`                           |        7 |
| `SettingController`                         | `/api/settings`                            |        1 |
| `SpaForwardController`                      | Non-API SPA forwarding                     |        1 |
| `SubscriptionController`                    | `/api/subscriptions`                       |        3 |
| `TagController`                             | `/api/tags`                                |        5 |
| `TrackController`                           | `/api/tracks`                              |       10 |
| `UserController`                            | `/api/users`                               |        9 |
| `UserSubscriptionController`                | `/api/user-subscriptions`                  |        5 |
| `UtilController`                            | `/api/utils`                               |        6 |
| `WhitelistChannelController`                | `/api/whitelist-channels`                  |        6 |
| **Total**                                   | 25 controller files                        |  **144** |

`144` is the current method-level Spring mapping count and includes the one
non-REST SPA forwarding mapping. Documentation uses the same counting contract.

## 6. Scheduled and Non-Navigation Operational Surfaces

| Surface                        | Scheduled operations | Source                                |
| ------------------------------ | -------------------: | ------------------------------------- |
| Payment reconciliation         |                    1 | `PaymentReconciliationService`        |
| Subscription lifecycle         |                    3 | `SubscriptionScheduler`               |
| Withdrawn-user billing cleanup |                    1 | `WithdrawalBillingCleanupCoordinator` |
| Storage mutation recovery      |                    1 | `StorageMutationRecoveryService`      |
| **Total**                      |                **6** | `@Scheduled` source scan              |

Additional high-risk operations not represented by distinct routes include
whitelist CSV export/download, company document download, settlement CSV import,
payment reconciliation, refund request/approval/execution, entitlement
correction, user-subscription correction, Track audio-analysis dry-run, and
file/audio/image upload processing.

## 7. Same Behavior, Different Entry Points

These families require separate acceptance rows even when they share a Store,
DTO, or component:

| Family                     | Discovered entry points                                                                                           |
| -------------------------- | ----------------------------------------------------------------------------------------------------------------- |
| Track playback             | Track list, Track detail, Album detail, Like list, Playlist detail, Download history, Player queue, History modal |
| Track projection/hydration | Public list/detail responses, Album Track items, Playlist Track items, Likes, Downloads, browser-local history    |
| Search and Tag filtering   | Header keyword search, Track list keyword query, visible Tag chips/modal, ADMIN Tag mutations                     |
| Image/thumbnail display    | Home/catalog cards, Track detail, Album pages, upload/edit previews, missing-image fallback                       |
| Subscription state         | Plan page, checkout callbacks, manage page, route guards, download/playlist entitlement, ADMIN views              |
| Question flow              | USER list/create/detail and ADMIN list/answer/status handling                                                     |
| Whitelist flow             | USER draft/request/primary/delete and ADMIN review/export/download                                                |
| Company certification      | BUSINESS apply/status/document replacement and ADMIN review/document download                                     |

The exact action/API/state matrix for each family is deferred to WI-020.

## 8. High-Risk State-Machine Families

| Family                              | Required downstream evidence chain                                                                           |
| ----------------------------------- | ------------------------------------------------------------------------------------------------------------ |
| Recurring Subscription and billing  | UI message -> request -> Toss test boundary -> local order/agreement/payment/subscription -> reload          |
| Plan change and cancellation        | Preview -> confirmation -> immediate or scheduled transition -> entitlement -> reversal/retry                |
| ADMIN payment operations            | Read -> request -> approve -> execute -> audit/reconciliation -> reload                                      |
| ADMIN local subscription correction | Preview -> request -> approve -> execute -> concurrent/stale-state handling -> audit                         |
| Whitelist                           | Draft -> request -> review/revision/register/removal -> immutable CSV batch                                  |
| Company certification               | Submit -> document replacement -> review/reject/approve -> BUSINESS subscription gate                        |
| Upload and media analysis           | Local validation -> multipart request -> audio/image analysis -> persistence -> rendered playback/preview    |
| CRUD ordering                       | Album/Playlist membership add/remove/reorder -> API payload -> persisted order -> all entry-point renderings |

## 9. Mechanical Documentation Alignment

| Contract                |                                       Code value |                                             Document value | WI-019 disposition                          |
| ----------------------- | -----------------------------------------------: | ---------------------------------------------------------: | ------------------------------------------- |
| Path-bearing routes     |                                               56 |                                                         56 | Aligned                                     |
| Routable declarations   |                                               57 |                                                         57 | Aligned                                     |
| Lazy/distinct page UIs  |                                               53 |                                                         53 | Aligned                                     |
| Method-level mappings   |                                              144 |                                                        144 | Aligned                                     |
| Controller inventory    |                                         25 files |                                                    25 rows | Aligned                                     |
| V1 schema               |                 Not recounted in this bounded WI |                                         41 tables/entities | Deferred                                    |
| Official branch wording | Audit runs on `codex/v1-release-rehearsal-fixes` | Current docs still identify the prior promoted V1 baseline | Not automatically a defect before promotion |

The route and API count documents are mechanically current at baseline
`e343c20`. Semantic claims, authorization, UI consumers, state handling, and DB
contracts remain unverified and must not be inferred from count alignment.

## 10. Explicit WI-020 Inputs and Deferrals

WI-020 must add, for every active route and operation:

- Authorized and denied roles.
- Entry path or contextual trigger.
- Primary read and mutation actions.
- Frontend API function and backend mapping.
- Loading, empty, success, validation, authorization, not-found, infrastructure,
  and unknown-outcome UI expectations.
- Persistent DB owner or browser-local state owner.
- Desktop/mobile viewports and keyboard/focus checks.
- Cross-entry-point invariants and adjacent regression scope.
- External side-effect boundary and approval requirement.

The following remain explicitly unclassified in WI-019: exported API functions
without UI consumers, controller methods without SPA consumers, SecurityConfig
authorization details, schema/entity ownership, exact modal/drawer inventory,
all visual states, and browser/runtime outcomes.
