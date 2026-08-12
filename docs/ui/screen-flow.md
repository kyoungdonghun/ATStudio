---
version: 5.6
last_updated: 2026-08-12
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

1. The user selects an exact plan ID, audience, and billing cycle on
   `/subscriptions`. Plan names remain display-only.
2. Checkout requires `planId`, `userType`, `billingCycle`, and `purpose`. It
   verifies route audience against the authenticated user, loads the matching
   audience plan list, and resolves the exact ID before prepare.
3. Checkout creates one lowercase canonical UUIDv4 attempt key for the exact
   purpose/plan/audience/cycle context and stores it in `sessionStorage`. React
   StrictMode remount, reload, network retry, and same-attempt retry reuse the
   stored key.
4. Checkout sends the key only in the required `Idempotency-Key` header and
   sends only `{subscriptionId, billingCycle, purpose}` in the body to
   `POST /api/payments/billing-agreements/prepare`. `purpose` is either
   `SUBSCRIBE` or `BILLING_AGREEMENT` and is a consistency claim.
5. The server rejects an absent or noncanonical key before DB/Provider work,
   derives an owner-scoped opaque digest for valid input, and never persists or
   logs the raw key. The same owner/key/exact tuple reuses the same order; a
   same-owner tuple mismatch returns
   `409 PAYMENT_PREPARE_ATTEMPT_CONFLICT`; another owner has an independent
   namespace.
6. The server derives authoritative purpose from current subscription state
   and rejects any purpose, audience, current-plan, or current-cycle mismatch
   before billing-agreement mutation, order persistence, or Provider prepare.
7. The returned order becomes actionable only after checkout validates the
   order ID, Provider, purpose, agreement status, exact plan and cycle, exact
   server amount, `KRW`, expiry, checkout type, `CARD`, keys, and absolute
   HTTP(S) callbacks. UI copy and callbacks use the validated response values;
   failure keeps billing auth disabled and does not invoke the Toss SDK.
8. A corrupt local record or API result
   `PAYMENT_PREPARE_IDEMPOTENCY_KEY_INVALID`, `PAYMENT_ORDER_EXPIRED`, or
   `PAYMENT_ORDER_TERMINAL` enables a user-visible new-attempt action. The key
   changes only after that click. Tuple conflict, arbitrary `409`, Provider or
   network error, unknown result, and in-flight order state keep the key and
   expose only same-attempt retry where applicable.
9. A new subscription uses `SUBSCRIBE` and the exact server plan price.
   Confirmation charges the first period and activates only after server
   confirmation.
10. Payment-method re-registration carries the exact current plan ID,
   authenticated audience, and current cycle, uses `BILLING_AGREEMENT`, and
   returns `amount=0`. Confirmation updates the method without changing the
   plan or period.
11. Re-registration entered from an upgrade preview preserves exact return plan
   identity and audience, then returns to the preview without executing the
   upgrade. Upgrade itself charges the prorated difference through the active
   billing agreement; downgrade/cycle-only changes remain pending until
   successful renewal.
12. Cancel stops the next renewal while paid access remains through `expiresAt`;
   valid grace-period cancellation can be reactivated.
13. A callback captures its values once and immediately removes `authKey` and
    `customerKey` from the visible URL with history replacement. The success
    path confirms at most once, and both success and fail paths use the
    USER-only owner-scoped outcome read before deciding the result.
14. `COMMITTED` requires a `DONE` order where applicable plus fresh canonical
    Subscription and Billing Agreement reads that prove exact target identity
    and the same aggregate linkage. Only then may the UI show success or
    navigate.
15. `FAILED` requires terminal command evidence or a narrow terminal error for
    local-only cancel/reactivate. A lost response, timeout, `5xx`, or reload
    failure does not prove failure.
16. `RELOAD_FAILED` preserves the successful mutation result when canonical
    reload fails. `UNKNOWN` warns that processing may already have completed.
    Both states expose `status again`, which performs outcome/canonical reads
    only and never replays the mutation.
17. Manage disables plan change, cancel, reactivate, and payment-method mutation
    controls together during `UNKNOWN` or `RELOAD_FAILED`. Rapid recovery reads
    are fenced so stale results cannot overwrite newer state.
18. Scheduled change and downgrade recovery require the original Subscription
    aggregate, source plan/cycle, and exact pending target plan/cycle. Charged
    upgrade recovery additionally requires the exact current-period command
    outcome and matching canonical aggregate linkage; it never guesses the
    latest payment.
19. Removed payment aliases have no compatibility redirect or mutation path.

WI-033 prepare replay and WI-034 outcome recovery are both current behavior.
In-flight and unknown-outcome states never enable a replacement prepare or
automatic financial retry. Verification used automated UI/backend tests, H2,
and Provider doubles, with no real Toss/SDK, charge, refund, mail, retained
database, deployment, schema, policy, or secret action.

## Whitelist And Company Certification

- Users may save up to 100 whitelist channel profiles. Plan limits apply to registration-relevant states, not all drafts.
- Whitelist transitions, primary selection, immutable export snapshots, and CSV formula neutralization follow the current backend contract.
- BUSINESS users upload PDF/JPG/JPEG/PNG certification documents. Images are decoded and stored as canonical JPEG; review reasons are bounded to 500 characters.
- Document metadata never exposes persistence paths. Automatic retention deletion and malware scanning remain policy-pending.

## Admin

- Dashboard loads `GET /api/admin/stats` with totals and five recent users.
- Payment operations separates ledgers/incidents/refunds/settlement from ordinary subscription administration.
- The optional Settlement import note is limited to 500 characters and has a
  visible warning against PII, credentials, payment keys, and other sensitive
  values. It is plain user text with no free-text DLP guarantee.
- After explicit confirmation, Settlement import creates one lowercase UUIDv4,
  stores one pending recovery record in browser `sessionStorage`, sends one
  POST with the key only in `Idempotency-Key`, and disables authentication
  replay for that POST.
- A Settlement import that resolves with `failedRows > 0` is visibly partial,
  renders every returned row error, retains the exact React `File`, DOM file
  input, and note, and makes one import call plus one Settlement-list reload.
  Its result satisfies total-count conservation, while status counts describe
  only imported rows.
- A transport failure makes one read-only recovery GET with the same key. A
  `PROCESSING` outcome keeps the key and correction context and exposes manual
  recovery. The screen never polls or submits a second POST; a pending or
  corrupt stored attempt blocks a new import.
- Terminal recovery clears the pending key. `FAILED` requires a new explicit
  action. `COMPLETED` reloads the Settlement list, but recovered row errors are
  unavailable because only aggregate attempt evidence is durable.
- A zero-failure Settlement import clears React and DOM file state only after
  the required reload succeeds. Partial completion or reload failure retains
  correction context and never reports full success.
- Reconciliation has no import-attempt key or recovery state. An orderless
  finalized payment is represented once in `failedRows` with bounded error
  evidence; normal reconciliation results conserve total counts.
- Settlement IGNORE requires a trimmed nonblank note of at most 500 characters
  and the existing danger confirmation. The server requires current
  authenticated and authoritative active ADMIN authority; the first decision
  and audit stay immutable, and every otherwise-valid repeat returns
  `INVALID_STATE_TRANSITION` with no new mutation or audit. No typed phrase was
  added to this flow; typed confirmation for the separate general local-
  subscription correction flow remains assigned to WI-20260809-ATS-054.
- Settlement operations do not mutate payment, refund, subscription,
  billing-agreement, receipt/mail, or Provider state. CSV import writes only
  attempt, Settlement, and row-audit evidence; reconciliation writes only
  Settlement and row-audit evidence.
- Refund and entitlement-correction execute each freeze an exact domain/durable
  intent after typed confirmation, read the matching ADMIN detail endpoint, and
  send one execute POST only when the fresh same-ID row is `APPROVED`.
- A rejected or lost execute response triggers one bounded exact detail GET,
  never another execute. The execute POSTs opt out of authentication replay, so
  a `401` cannot enter token refresh or replay the mutation.
- Exact `SUCCEEDED` maps to `COMMITTED`; exact `FAILED`/`CANCELLED` maps to
  `FAILED`; an explicitly successful execute with failed required detail/list
  reload maps to `RELOAD_FAILED`; every unproved or in-flight result maps to
  `UNKNOWN`. Success and reload-failure feedback remain visibly distinct.
- Refund `PROCESSING`/`PENDING_PROVIDER_CONFIRMATION` and correction
  `PROCESSING` rows hydrate as `UNKNOWN` after list or browser reload. Their
  only recovery control is read-only `status again`.
- A manual status read cannot approve or execute. An `UNKNOWN` exact detail of
  `REQUESTED` or `APPROVED` may clear the pre-execution lock; `REQUESTED`
  restores approval only, while a later `APPROVED` execute still requires typed
  confirmation and a new exact preflight.
- `UNKNOWN` and `RELOAD_FAILED` lock the exact row and linked refund/correction
  mutations across domains. Execute and status-read ownership prevent rapid
  double actions; intent/read/view generations discard stale detail or list
  success/failure from an older operation, tab, or page.
- Automatic refund execute retry, automatic correction execute retry, recovery
  mutation, and recovery Provider-call counts are all zero.
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
