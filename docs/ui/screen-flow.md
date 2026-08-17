---
version: 6.5
last_updated: 2026-08-14
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

The main layout serves public and authenticated user workflows with the shared
player. The admin layout has a separate sidebar/topbar and no player. Protected
and Subscriber guards plus guest Player Like/Add-to-Playlist actions construct
one safe Login return target from the current pathname and query; hashes are
excluded. Login revalidates every consumed target after identity loading and
falls back to Home for malformed, external, auth-loop, or role/user-type
inappropriate destinations. Subscriber redirect notifications are effect-owned
and emitted once per reason.

Every lazy route keeps the current internal URL while loading. A rejected page
import renders fixed Korean recovery UI without raw error details, permits one
explicit fresh retry, then retains only safe Home/Back recovery after another
failure. It does not poll, recurse, or hard reload. The `/error` server-error
route, public wildcard 404, and ADMIN route matching semantics remain unchanged.

The public shell conditionally mounts its mobile Header menu and PlayerBar
detail only while each surface is open. Escape or overlay dismissal restores
the exact surviving opener for the owned surface; PlayerBar Escape is scoped to
its own mobile surface and leaves Header and shared Modal events to their
owners. A normally accepted mobile navigation command closes the source surface
immediately and requests one destination focus. The destination layout consumes
that one-shot request synchronously across same-layout and public-to-ADMIN or
ADMIN-to-public transitions, including StrictMode replay, then focuses an
available main H1 or the main region. Temporary focusability is removed after
the attempt, and no body fallback is used.

The ADMIN shell keeps a permanent desktop sidebar and conditionally mounts a
mobile navigation dialog. The mobile dialog receives initial focus, traps
Tab/Shift+Tab, and isolates topbar content outside its opener, main content, and
Toast controls with `inert`, `aria-hidden`, overlay coverage, and Toast pointer
blocking. Escape or overlay dismissal returns focus to the exact valid opener.
When `matchMedia` leaves the mobile breakpoint, the dialog, overlay, trap, and
background isolation are released without restoring the mobile opener; the
desktop sidebar and active route remain in place. Accepted ADMIN mobile routes
use the same one-shot destination-focus flow.

## Authentication

1. Login, signup, and password-reset screens load public runtime capabilities.
   Loading and failure expose no capability as enabled. Every failed attempt
   offers an explicit manual retry, and no automatic retry occurs.
2. Password signup requires affirmative Terms and Privacy consent. Marketing
   consent is optional; signup always transmits the `marketingAgreed` boolean,
   while only `true` creates an affirmative Marketing-consent record. A
   successful signup navigates to email verification without creating or
   persisting a password session.
3. Password login and refresh deny an unverified account before credentials are
   issued or rotated. The SPA redirects the fixed
   `EMAIL_VERIFICATION_REQUIRED` error to email verification and persists no
   session for that result. A verified password login retains a structurally safe
   return target, then revalidates it against the authenticated role and user
   type using a percent-decoded, lowercase canonical pathname before navigation.
   Authorized navigation retains the original validated target.
4. Enabled social login and social onboarding are unchanged and remain outside
   the WI-068 scope. A new social account revalidates current identity before
   profile completion.
   Its one-time continuation is bound to the authenticated user ID, replacing
   any old value. Storage failure proceeds without a continuation; consumption
   removes the record even when the refreshed user does not match. Complete
4. Logout calls `POST /api/auth/logout` and clears frontend session state.
5. Social-only account withdrawal remains policy-pending; password confirmation is not treated as social proof.
6. Profile keeps account, edit, password, and subscription query panels. Legacy
   activity query keys redirect to canonical activity routes, while other
   unsupported tabs normalize to `account`. Subscription loading, authoritative
   absence, and retryable failure remain distinct.
7. Forgot-password acceptance is generic for all submitted addresses. Auth and
   account failures use fixed allowlisted guidance rather than backend message
   text.

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
8. Public Track and Album catalogs canonicalize `page` to a bounded 1-based
   value and use 20 results per page. Album grid/list switching preserves sort
   and page query state, so both views retain the same result projection.
9. Album list/detail requests are latest-owner wins. Route, query, view, or
   unmount retirement prevents stale data, error, empty, or loading commits.
   Missing and recoverable Track/Album detail states use fixed Korean Retry,
   Back, and Home recovery without transport text.
10. A page that publishes a visible Track-list context releases only that
    context on departure. The active Track and durable queue/shuffle/repeat
    state remain intact. Album positions display as one-based while stored
    order remains zero-based.
11. Restored and seeked playback time is finite and clamped between zero and a
    known positive duration. Time and waveform UI never exceed that duration.
    Public playback continues to stream the complete active Track.

## Notice

1. Public Notice detail accepts only a canonical positive ASCII decimal
   safe-integer ID. The latest mounted route owns the request; route replacement
   and unmount abort retired work and prevent stale data or error commits.
2. A missing Notice shows fixed Korean missing-state copy and a safe Notice-list
   action without retry. Network, timeout, server, and unknown failures show
   fixed Korean recovery with one manual retry.
3. Each attachment owns its download state. A pending request blocks only a
   duplicate for the same file, another file remains available, and a failure
   stays local to that file with a same-file retry. Route replacement and
   unmount abort retired downloads before any browser download effect.
4. Admin create/edit forms expose associated Korean labels and enforce the
   current title maximum of 200 characters and content maximum of 1,000
   characters on both client and server. Existing accepted-file behavior is
   unchanged; exact attachment type, count, and byte policy remains WI-066.
5. One current-ref create/edit operation fences save, Notice deletion,
   attachment changes, duplicate submission, modal close, and all in-app
   navigation, while a separate guard covers browser unload. Component cleanup
   retires UI writes but does not abort an accepted mutation. Authoritative
   success releases the fence before destination navigation.
6. Authoritative validation, authorization, permission, and not-found failures
   preserve form state for a deliberate retry. Network, server, and unknown
   outcomes make no success/failure claim, keep the same mutation disabled, and
   require a Notice-list observation or a fresh non-counting ADMIN edit GET before
   another deliberate edit is possible.
7. Edit loading uses `GET /api/notices/{noticeId}/admin`, an ADMIN-only minimized
   projection that does not increment public `viewCount`. Invalid edit IDs issue
   no Notice read or mutation request and render safe list navigation.

## Playlist

1. A subscriber opens the playlist list.
2. Create actions use the existing modal.
3. Detail/edit pages add, remove, and reorder tracks under current plan limits.
4. Playlist list and capacity load independently. Create remains hidden until
   current-owner list data and a positive server `maxPlaylists` value are
   known; failure offers retry and never uses a fallback limit.
5. Detail/edit accepts only canonical positive ASCII decimal safe-integer IDs.
   Invalid IDs show fixed Korean list recovery and make no request.
6. Drawer playlist deletion and Track removal require target-specific
   confirmation. One pending owner fences duplicates; failure stays visible
   with a same-target retry, and success reloads the authoritative list or
   detail projection.
7. The fixed Playlist Drawer is a named dialog while open. Entry focus,
   Tab/Shift+Tab containment, Escape dismissal, and valid-opener focus return
   are local UI transitions only; they do not dispatch playlist mutations.
   Playlist, detail, and like read failures expose their existing scoped retry.
8. Add-to-Playlist renders list loading, fixed failure with manual retry, and
   an explicit subscription-required result when no parent callback is
   available. Close/reopen, Track replacement, stale responses, and old success
   timers cannot alter the current lifecycle.
8. Playlist create/edit revoke only locally created thumbnail preview object
   URLs at replacement, removal, close, route/owner replacement, and unmount
   boundaries. Backend URLs remain untouched.

## Member Read Recovery

Playlist, likes, License, Question, drawer, and Download History reads retire
stale work when their relevant route, page, filter, tab, drawer session, or
authenticated owner changes. Only the current owner projection may render or
commit data, fixed errors, loading, dialogs, controls, or player context.

Download History keeps one owner/read key and abort signal through Track-ID
preparation, confirmation, download iterations, browser effects, feedback,
count refresh, and cleanup. Retirement stops remaining effects.

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
- Settlement file preflight mirrors the server envelope: a present `.csv`
  filename of at most 255 characters, an allowed or blank CSV MIME value, and
  nonempty content of at most 5 MiB. Browser checks and the file `accept` value
  are advisory; server validation remains authoritative.
- WI-067 automated UI/API contract evidence is complete, including
  multipart-only note transport, partial-warning behavior, and bounded omitted
  error reporting. Its separate MySQL proof is `RUN-PASS-CLEANED`; no manual
  operator/client acceptance or production screen readiness is inferred.
- The optional Settlement import note is limited to 500 characters and has a
  visible warning against PII, credentials, payment keys, and other sensitive
  values. It is plain user text with no free-text DLP guarantee. A nonblank
  trimmed value is sent as the optional multipart `note` part, never as a query
  parameter.
- After explicit confirmation, Settlement import creates one lowercase UUIDv4,
  stores one pending recovery record in browser `sessionStorage`, sends one
  POST with the key only in `Idempotency-Key`, and disables authentication
  replay for that POST.
- A Settlement import that resolves with `failedRows > 0` is visibly partial,
  renders every returned row error, retains the exact React `File`, DOM file
  input, and note, and makes one import call plus one Settlement-list reload.
  Its result satisfies total-count conservation, while status counts describe
  only imported rows. Import returns all row errors within the 1,000-logical-row
  ceiling and shows `omittedErrorCount=0`.
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
- Reconciliation has no import-attempt key, recovery state, cursor, polling, or
  automatic retry. The server defaults to 30 inclusive days, accepts at most 90
  days and 5,000 selected finalized payments, returns at most 200 error details,
  and reports any additional failed-row details through `omittedErrorCount`.
  The result panel displays that omitted count. Any failed, returned-error, or
  omitted-error result is visibly partial; an orderless finalized payment is
  represented once in `failedRows`, and normal results conserve total counts.
- Settlement IGNORE requires a trimmed nonblank note of at most 500 characters
  and the existing danger confirmation. The server requires current
  authenticated and authoritative active ADMIN authority; the first decision
  and audit stay immutable, and every otherwise-valid repeat returns
  `INVALID_STATE_TRANSITION` with no new mutation or audit. No typed phrase was
  added to this flow. In the separate general local-Subscription correction
  flow, execute alone requires the trimmed exact phrase `권한 보정 실행`;
  approval remains an ordinary confirmation with no typed phrase.
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
- Album management uses 20 active Albums per 1-based URL page. Invalid and
  beyond-last pages normalize with bounded replace navigation, every list read
  is latest-owner wins, and list retry is manual.
- Album edit modals clear prior target state before an abortable detail read;
  loading or failure cannot submit, and close/retry/target switch retires stale
  responses. A blank edit description is sent as an explicit clear.
- Album thumbnail create/edit entry points share JPEG/PNG, 10 MiB, 4096 pixel
  per-dimension, and 16,777,216 decoded-pixel browser checks. Pending or failed
  validation blocks submit, each local preview URL is released at lifecycle
  boundaries, and server validation remains authoritative.
- Album Track search uses title plus Usage Guide Tag, latest-request ownership,
  and keyboard-operable combobox/listbox state. Membership mutation success and
  follow-up read failure are displayed separately; refresh retry never repeats
  the mutation. Invalid Album edit IDs issue no protected request.

## Environment Boundary

Public access is allowed only through an operator-controlled acceptance runtime whose local page, API proxy, and newly issued public URL were verified together. Historical demo URLs and captures are not current runtime evidence. WI-014~021 focused automated evidence is not a substitute for full browser acceptance or production deployment.

WI-057 shell behavior is recorded from current source, CSS, jsdom, and local
automated-test evidence. Native viewport, keyboard, pointer, and browser-focus
acceptance remains owned by WI-076 and is not established by this document.
