---
version: 4.0
last_updated: 2026-07-17
project: ATS
owner: docops
category: design
status: stable
dependencies:
  - path: atstudio-front-list.md
    reason: Current screen count and route inventory
  - path: ../design/api-spec.md
    reason: Current API contract
  - path: ../design/usecase/index.md
    reason: Domain use cases
---

# ATStudio Screen Flows

## Navigation Model

The main layout serves public and authenticated user workflows with the shared player. The admin layout has a separate sidebar/topbar and no player. Route guards preserve safe internal return targets and reject external or protocol-relative redirects.

## Authentication

1. Login/signup screens load public runtime capabilities.
2. Password or enabled social login authenticates the user.
3. A new social account completes the required profile before protected workflows.
4. Logout calls `POST /api/auth/logout` and clears frontend session state.
5. Social-only account withdrawal remains policy-pending; password confirmation is not treated as social proof.

## Discovery And Playback

1. Public users browse tracks, tags, and albums.
2. Streaming uses the controller-mediated public stream endpoint; storage keys are not exposed.
3. After playback starts, the SPA records the track in browser `localStorage` under `playHistory`.
4. The local list is capped at 100; no server Play History API or table participates.

## Playlist

1. A subscriber opens the playlist list.
2. Create actions use the existing modal.
3. Detail/edit pages add, remove, and reorder tracks under current plan limits.

## Subscription And Payment

1. The user selects a plan/cycle on `/subscriptions`.
2. `/subscriptions/checkout` prepares Toss billing auth.
3. New subscription confirmation charges the first period and activates only after server confirmation.
4. Existing subscriber payment-method re-registration prepares `purpose=BILLING_AGREEMENT` with `amount=0`; confirmation updates the method without changing the plan or period.
5. Upgrade charges the prorated difference through the active billing agreement. Downgrade/cycle-only changes remain pending until successful renewal.
6. Cancel stops the next renewal while paid access remains through `expiresAt`; valid grace-period cancellation can be reactivated.
7. Removed payment aliases have no compatibility redirect or mutation path.

## Whitelist And Company Certification

- Users may save up to 100 whitelist channel profiles. Plan limits apply to registration-relevant states, not all drafts.
- Whitelist transitions, primary selection, immutable export snapshots, and CSV formula neutralization follow the current backend contract.
- BUSINESS users upload PDF/JPG/JPEG/PNG certification documents. Images are decoded and stored as canonical JPEG; review reasons are bounded to 500 characters.
- Document metadata never exposes persistence paths. Automatic retention deletion and malware scanning remain policy-pending.

## Admin

- Dashboard loads `GET /api/admin/stats` with totals and five recent users.
- Payment operations separates ledgers/incidents/refunds/settlement from ordinary subscription administration.
- Site settings reads and upserts `COMPANY_CERT_GUIDE`.
- Whitelist and certification review use current transition, audit, export, and private-document boundaries.
- List screens use latest-request-wins behavior so stale responses cannot overwrite current filters/pages.

## Environment Boundary

Public access is allowed only through an operator-controlled acceptance runtime whose local page, API proxy, and newly issued public URL were verified together. Historical demo URLs and captures are not current runtime evidence.
