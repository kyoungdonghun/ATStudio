---
version: 8.11
last_updated: 2026-08-16
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

| Unit                             | Count | Rule                                                       |
| -------------------------------- | ----: | ---------------------------------------------------------- |
| Path-bearing route objects       |    56 | Every `path:` entry in `frontend/src/router/index.tsx`     |
| Index redirects                  |     1 | `/admin` redirects to `dashboard`                          |
| Routable declarations            |    57 | 56 paths plus 1 index redirect                             |
| Lazy route-level page components |    53 | Every `createLazyPage(...)` declaration                    |
| Distinct visual page UIs         |    53 | Every lazy page is a distinct UI; includes 2 error screens |

`frontend/src/router/index.tsx` points to this inventory instead of embedding a
fixed screen total. This inventory is derived from the route and
`createLazyPage(...)` declarations themselves.

Repeated callback paths do not create new screens. Three checkout paths reuse one `SubscriptionPaymentPage`, and the admin index is a redirect rather than a screen.

## Screen Groups

| Group                 | Distinct UIs | Routes / screens                                                                                                                                                                                               | Main API boundary                                                          |
| --------------------- | -----------: | -------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- | -------------------------------------------------------------------------- |
| Public discovery      |            9 | Home; track list/detail; album image/list/detail; plans; notice list/detail                                                                                                                                    | Tracks, tags, albums, subscriptions, notices                               |
| Authentication        |            6 | Login; signup; email verify; password reset; social login; social profile completion                                                                                                                           | Auth, users, public capabilities                                           |
| Member/subscriber     |           17 | Playlist list/detail/edit; profile; likes; local play history; license list/detail; download history; subscription checkout/manage; whitelist; company certification apply/status; question list/create/detail | User, playlist, download, billing, whitelist, certification, question APIs |
| Error                 |            2 | 404 and server error                                                                                                                                                                                           | No business API                                                            |
| Creator/admin content |            5 | Track upload/edit; album manage/create/edit                                                                                                                                                                    | Track and album admin APIs                                                 |
| Admin operations      |           14 | Dashboard; users; plans; licenses; questions; certifications; tags; tracks; user subscriptions; payments; whitelist; notice create/edit; site settings                                                         | Admin APIs                                                                 |
| **Total**             |       **53** |                                                                                                                                                                                                                |                                                                            |

## Important Current Screen Contracts

### Authentication and Profile

- Login, signup, and both password-reset steps expose authentication methods
  only after public capabilities load successfully. Loading and failure do not
  imply availability. Every failed attempt provides an explicit manual retry
  action, and no automatic retry occurs.
- Password signup requires affirmative Terms and Privacy consent. Marketing is
  optional; signup always transmits the `marketingAgreed` boolean, and only
  `true` creates an affirmative Marketing-consent record. The successful signup
  route gives email-verification guidance without creating an SPA session.
- Password login and refresh reject an unverified account before tokens are
  issued or rotated. The fixed `EMAIL_VERIFICATION_REQUIRED` result guides the
  SPA to email verification and leaves no session persisted.
- Logout callers await one coalesced `POST /api/auth/logout`. Only `204`
  confirms server revocation; a bare `401` or other failure still clears the
  local session and shows the fixed unconfirmed-revocation warning.
- Social login and social profile-completion onboarding are unchanged by WI-068.
- BUSINESS signup, social completion, and profile edit use one required
  `Company name or industry` free-text input backed by existing `companyName`;
  no separate industry field is exposed. INDIVIDUAL members continue to use
  `job`.
- Nickname input trims leading and trailing whitespace while preserving internal
  spaces before validation, availability checks, submission, server uniqueness
  evaluation, and persistence.
- Social profile completion verifies current identity before showing the form.
  Complete profiles move to `/profile?tab=account`; failed identity remains
  non-mutating. One pending fence covers availability checks and profile
  mutation, with all related controls disabled until completion. The
  post-mutation identity refresh is session-generation and user-ID guarded, so
  logout cannot restore a stale session and unmount cannot trigger late
  navigation.
- Profile query panels are `account`, `edit`, `password`, and `subscription`.
  Legacy activity query keys redirect to canonical activity routes; other
  unsupported tabs normalize to `account`. Subscription loading, authoritative
  absence, and retryable failure render separately, and retry clears stale
  subscription content.
- Forgot-password acceptance remains account-enumeration safe. Auth and Profile
  mutation errors render only fixed allowlisted guidance, never arbitrary
  backend message text.
- Protected/subscriber guards and guest Player actions preserve the current
  pathname and query in one validated Login return target and exclude hashes.
  Login revalidates after identity loading and rejects external, malformed,
  auth-loop, and role/user-type-inappropriate destinations.

### Routing and Error Recovery

- Route pages use one application-owned lazy loader. Pending UI remains the
  Korean loading state at the same internal URL. A rejected import shows fixed
  Korean recovery copy, allows one fresh retry, and then offers only Home/Back
  without raw error or chunk details and without reload/polling loops.
- The `/error` server-error UI, public wildcard 404 UI, ADMIN index redirect,
  and the absence of an ADMIN wildcard remain unchanged.

### Notice

- Public Notice detail accepts only a canonical positive safe-integer ID and is
  owned by the latest mounted route. A `404` shows fixed Korean missing-state
  copy and Notice-list navigation without retry; transient failures show fixed
  Korean recovery with one manual retry.
- Attachment download state is owned per file. A pending request blocks only a
  same-file duplicate, other files remain independently available, and failure
  remains local and retryable. Retired route downloads cannot trigger a browser
  download effect.
- Admin Notice create/edit forms use associated Korean labels and enforce the
  existing title maximum of 200 characters and canonical content maximum of
  1,000 characters. Existing accepted-file behavior remains unchanged; exact
  attachment type, count, and byte limits remain pending WI-066.
- Create/edit mutations use one current-ref operation fence across submit,
  Notice deletion, attachment changes, duplicate actions, modal close, all
  in-app navigation, plus a browser-unload guard. An accepted mutation is not
  aborted by component cleanup. The fence is released before
  authoritative-success navigation.
- Authoritative validation, authorization, permission, and not-found failures
  preserve current form state for a deliberate retry. Network, server, and
  unknown outcomes show the accessible Korean `처리 결과 확인 필요` state and
  keep the same POST, PUT, or DELETE disabled until the operator leaves for a
  Notice-list observation or completes a fresh non-counting ADMIN edit read.
- Notice edit loads the ADMIN-only minimized projection from
  `GET /api/notices/{noticeId}/admin`, so opening the edit form does not change
  public `viewCount`. Invalid edit IDs make no Notice request and expose safe
  list navigation.

### Play History

`/play-history` reads browser `localStorage` key `playHistory`. It keeps at most
100 de-duplicated Track IDs and records only after playback starts. Current and
legacy entries are hydrated through one public `POST /api/tracks/batch` request;
inactive/missing IDs are omitted and stale results cannot overwrite a newer
storage snapshot. No server Play History table participates.

### Discovery and Playback

- Home contains one accessible Tag module ordered Usage, Genre, Mood,
  Instrument. Usage remains visible even when empty; initial selection falls
  forward to the first category with active-Track results.
- Track-list URLs and API requests preserve repeated values for all four Tag
  types with AND semantics. Each taxonomy loads and fails independently, with
  an error and manual retry scoped to that type. Active URL values remain
  visible as removable fallback chips when a request fails or omits them.
  Selecting a Mood does not hide the remaining valid Mood chips merely because
  the result set narrowed, so multiple Moods remain selectable and visible.
- Genre, Mood, and Instrument labels render their raw values. Usage keeps its
  raw value in URL and API parameters and adds `#` only to the visible label.
- The player shows non-fatal buffering status only after 2 seconds. Actual
  playback errors use a separate assertive state.
- Player actions provide a direct Likes entry adjacent to Play History in both
  desktop and expanded mobile layouts. It opens the shared drawer at Likes;
  ordinary drawer close/reopen preserves a user-selected tab until a new
  explicit action requests a tab.
- Album, playlist, likes, download history, queue, and persisted player state
  use the shared PlayableTrack fields. Omitted nullable thumbnail or waveform
  keys normalize to `null`, and a selected Track's duration becomes the player
  duration in the same transition that resets current time.
- Public Track and Album list URLs use a bounded 1-based `page` and a shared
  page size of 20. Album image/list switches preserve compatible sort and page
  queries, and both views keep one result projection.
- Album list/detail loading is owned by the latest mounted route/query. Retired
  requests cannot commit data, error, empty, or loading state. Missing and
  retryable Track/Album details show fixed Korean Retry, Back, and Home actions
  without raw transport text.
- Album Track positions render one-based while canonical membership order and
  Track IDs remain unchanged. Page-owned visible-list context is released on
  departure without clearing the active Track, queue, shuffle, or repeat mode.
- Restored and seeked progress is finite and clamped to a known duration before
  persistence and rendering. The public player still streams the complete
  active Track; no preview or playback entitlement gate is introduced.
- Playlist, likes, License, Question, and Download History reads are
  latest-owner wins. Relevant route, page, filter, tab, drawer-session, or
  authenticated-owner changes retire stale work and hide its data, dialogs,
  controls, and player context before passive effects run.
- Detail routes accept only canonical positive ASCII decimal safe-integer IDs;
  invalid IDs show fixed Korean list recovery and issue no request. Playlist
  creation requires current-owner list data and positive server-provided
  `maxPlaylists`; loading or retryable failure uses no client fallback.
- Playlist Drawer delete/remove requests start only after target-specific
  confirmation. Pending ownership fences duplicates and stale projections;
  fixed failure remains retryable for the same target, while success reloads
  the authoritative list or detail.
- Add-to-Playlist always renders loading while its list is pending, exposes a
  fixed manual-retry failure, and keeps subscription-required feedback visible
  when no parent outcome callback exists. Retired responses and timers cannot
  affect a later open or Track lifecycle.
- Playlist create/edit revoke each locally created thumbnail preview URL once
  on its lifecycle boundaries and never revoke backend media URLs.
- Playlist detail `Play all` starts the first displayed Track and establishes
  the displayed playlist order as the active player queue. `Add all to queue`
  adds those Tracks without starting playback.
- The Question list exposes a dedicated create-question FAB. Its responsive
  bottom clearance keeps the action reachable above the fixed PlayerBar and the
  expanded mobile player.
- Download History binds ID preparation, confirmation, download iterations,
  browser effects, feedback, count refresh, and cleanup to the initiating
  owner/read key and abort signal.

### Track Authoring

New and replacement Track thumbnails require a square JPEG/PNG. The field shows
the selected image in the same 1:1 centered `cover` viewport as the card. An
existing non-square thumbnail is shown with a replacement recommendation and is
not uploaded, rewritten, or removed unless the operator selects a new file.

### Album Authoring

- Album management pages through all active Albums with a 1-based URL page and
  20 rows per request. Invalid and beyond-last pages normalize with replace
  navigation, retired list responses cannot commit, and list failure exposes
  one manual retry action without a request loop.
- An Album edit modal owns one immutable target while detail loads. Close,
  retry, or target switch retires the prior request; loading and error states
  cannot submit. Blank edit descriptions are sent explicitly and clear the
  stored value.
- Create, route edit, and modal edit share JPEG/PNG, 10 MiB, 4096-by-4096, and
  decoded-pixel thumbnail checks. Pending or failed validation blocks submit,
  and locally created preview URLs are released on every lifecycle boundary.
- Album Track search matches title plus Usage Guide Tag, is latest-request
  owned, and exposes combobox/listbox keyboard selection. A committed membership
  mutation whose detail refresh fails remains visible as partial success; its
  retry reads membership only.
- Album edit route IDs must be canonical positive decimal safe integers.
  Invalid IDs render management/Home recovery and issue no Album request.

### Subscription Checkout

`/subscriptions/checkout` and its success/fail callbacks render one `SubscriptionPaymentPage`. New subscription checkout charges the first period after billing-key issue. Payment-method re-registration uses `purpose=BILLING_AGREEMENT` and `amount=0`, so it does not charge or change the current subscription.

### Admin Dashboard

`/admin/dashboard` calls `GET /api/admin/stats` and displays `totalUsers`, `totalTracks`, `totalSubscribers`, and the five most recent users.

### Admin User and Subscription Safety

- User management disables self-demotion, requires an operator reason for role
  changes, shows stable row/modal errors, and refreshes current session role
  state after mutation or ADMIN API 403. List rows use the exact ADMIN list DTO,
  while update results use the detail DTO; only `USER` and `ADMIN` are
  assignable.
- User subscription management opens one resumable local correction modal. Its
  visible stages are preview, request, approve, and execute. This is not a Toss
  charge/refund UI and does not imply provider success. An HTTP 4xx response is
  a definite rejection that keeps its stable error without reconciliation.
  Network/timeout/no-response failures and HTTP 5xx responses are ambiguous and
  trigger one open/detail read. A request read returning 204 remains unknown,
  preserves the draft and preview, blocks duplicate mutation, and keeps one
  read-only status-retry action; repeated 204 remains unknown. Known-ID approval
  and execution reads may restore terminal state. Browser checks cover only
  required and calendar-valid date input; server preview owns Seoul business
  date validation. Normalized reason and stage notes are visibly confirmed.

### Site Settings

`/admin/settings` reads `GET /api/settings/COMPANY_CERT_GUIDE` and updates it through `PUT /api/admin/settings/COMPANY_CERT_GUIDE`. The public read returns an empty value when the key is absent; the admin update is an upsert.

### Role Boundaries

- Public discovery does not require login.
- Profile, likes, play history, licenses, questions, and subscription management require authentication.
- Playlist creation/editing and official subscriber workflows use subscriber gating.
- Company certification application/status is BUSINESS-only.
- Subscription payment routes are USER-only; ADMIN is redirected to `/admin/payments`.
- Admin routes require ADMIN.
- The public PlayableTrack batch is intentionally public but returns active
  Track display/playback metadata only; it does not expose storage keys.

## Freshness Boundary

The React/Vite SPA is Phase 2 active on `codex/p1-acceptance-hardening`. The current install resolves Vite 6.4.3. A public URL is current only after the operator-controlled acceptance lifecycle verifies that exact runtime.
