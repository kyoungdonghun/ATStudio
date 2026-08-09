---
version: 5.1
last_updated: 2026-08-09
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

1. Home loads registered and active-result Tag sets separately and displays one
   Usage, Genre, Mood, Instrument module. Usage stays visible; initial selection
   uses the first result-bearing category and falls back to Usage only when all
   categories are empty.
2. Track-list URLs and requests use repeated `usage`, `genre`, `mood`, and
   `instrument` values with AND semantics. Each taxonomy has independent
   loading, error, and manual retry state. Active URL values remain visible and
   removable when their request fails or omits them; Usage adds `#` only while
   rendering and sends the raw value to the URL and Track API.
3. Streaming uses the controller-mediated public stream endpoint; storage keys are not exposed.
4. Album, playlist, likes, downloads, queue, and history map Track data through
   one PlayableTrack contract. Omitted nullable thumbnail and waveform keys
   normalize to explicit `null`. Persisted player/history state stores IDs and
   hydrates at most 100 active Tracks through one public batch request.
5. `waiting`/`stalled` stays pending for 2 seconds before a polite buffering
   status appears. Selecting a Track atomically resets time and adopts that
   Track's duration before metadata can refine the current source. Recovery,
   pause, retry, Track change, or error cancels buffering; actual playback
   errors use a separate assertive message.
6. After playback starts, the SPA records the Track ID in browser
   `localStorage` under `playHistory`.
7. The local list is capped at 100; no server Play History table participates.

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
- Role changes share server-side ADMIN locking and reject self-demotion,
  last-admin removal, and stale actor authority. The UI preserves the list and
  refreshes `/users/me` after a role mutation or ADMIN API 403.
- General subscription correction is a local preview, request, approval, and
  execution workflow. It may change local entitlement/billing-agreement state
  after revalidation; it does not call Toss charge/refund/billing-key deletion.
- Existing Track audio analysis is an ADMIN read-only dry-run. Running a real
  existing-row backfill remains separately approved and outside this flow.
- Track upload/edit requires square new/replacement thumbnails. Existing
  non-square thumbnails remain visible and unchanged until explicit replacement.

## Environment Boundary

Public access is allowed only through an operator-controlled acceptance runtime whose local page, API proxy, and newly issued public URL were verified together. Historical demo URLs and captures are not current runtime evidence. WI-014~021 focused automated evidence is not a substitute for full browser acceptance or production deployment.
