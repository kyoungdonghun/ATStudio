---
version: 8.0
last_updated: 2026-07-17
project: ATS
owner: docops
category: reference
status: stable
dependencies:
  - path: ../../frontend/src/router/index.tsx
    reason: Current route and lazy-page source
  - path: ../design/api-spec.md
    reason: Current API mapping source
  - path: screen-flow.md
    reason: Workflow details
---

# ATStudio Frontend Screen Inventory

## Count Contract

The stable product count is **53 distinct visual page UIs**.

| Unit | Count | Rule |
|---|---:|---|
| Path-bearing route objects | 56 | Every `path:` entry in `frontend/src/router/index.tsx` |
| Index redirects | 1 | `/admin` redirects to `dashboard` |
| Routable declarations | 57 | 56 paths plus 1 index redirect |
| Lazy route-level page components | 53 | Every `lazyPage(...)` declaration |
| Distinct visual page UIs | 53 | Every lazy page is a distinct UI; includes 2 error screens |

`frontend/src/router/index.tsx` points to this inventory instead of embedding a
fixed screen total. This inventory is derived from the route and
`lazyPage(...)` declarations themselves.

Repeated callback paths do not create new screens. Three checkout paths reuse one `SubscriptionPaymentPage`, and the admin index is a redirect rather than a screen.

## Screen Groups

| Group | Distinct UIs | Routes / screens | Main API boundary |
|---|---:|---|---|
| Public discovery | 9 | Home; track list/detail; album image/list/detail; plans; notice list/detail | Tracks, tags, albums, subscriptions, notices |
| Authentication | 6 | Login; signup; email verify; password reset; social login; social profile completion | Auth, users, public capabilities |
| Member/subscriber | 17 | Playlist list/detail/edit; profile; likes; local play history; license list/detail; download history; subscription checkout/manage; whitelist; company certification apply/status; question list/create/detail | User, playlist, download, billing, whitelist, certification, question APIs |
| Error | 2 | 404 and server error | No business API |
| Creator/admin content | 5 | Track upload/edit; album manage/create/edit | Track and album admin APIs |
| Admin operations | 14 | Dashboard; users; plans; licenses; questions; certifications; tags; tracks; user subscriptions; payments; whitelist; notice create/edit; site settings | Admin APIs |
| **Total** | **53** | | |

## Important Current Screen Contracts

### Play History

`/play-history` reads browser `localStorage` key `playHistory`. It keeps at most 100 de-duplicated tracks and records only after playback starts. No server Play History API or table participates in this screen.

### Subscription Checkout

`/subscriptions/checkout` and its success/fail callbacks render one `SubscriptionPaymentPage`. New subscription checkout charges the first period after billing-key issue. Payment-method re-registration uses `purpose=BILLING_AGREEMENT` and `amount=0`, so it does not charge or change the current subscription.

### Admin Dashboard

`/admin/dashboard` calls `GET /api/admin/stats` and displays `totalUsers`, `totalTracks`, `totalSubscribers`, and the five most recent users.

### Site Settings

`/admin/settings` reads `GET /api/settings/COMPANY_CERT_GUIDE` and updates it through `PUT /api/admin/settings/COMPANY_CERT_GUIDE`. The public read returns an empty value when the key is absent; the admin update is an upsert.

### Role Boundaries

- Public discovery does not require login.
- Profile, likes, play history, licenses, questions, and subscription management require authentication.
- Playlist creation/editing and official subscriber workflows use subscriber gating.
- Company certification application/status is BUSINESS-only.
- Subscription payment routes are USER-only; ADMIN is redirected to `/admin/payments`.
- Admin routes require ADMIN.

## Freshness Boundary

The React/Vite SPA is Phase 2 active on `codex/p1-acceptance-hardening`. The current install resolves Vite 6.4.3. A public URL is current only after the operator-controlled acceptance lifecycle verifies that exact runtime.
