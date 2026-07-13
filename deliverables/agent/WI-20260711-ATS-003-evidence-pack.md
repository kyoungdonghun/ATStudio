# Evidence Pack: WI-20260711-ATS-003

## Summary (one-liner)

- Completed a read-only audit of the active React SPA and found 2 P1, 10 P2, and 2 P3 frontend issues across authentication, role isolation, API/state handling, accessibility, performance, tests, and documentation contracts.

## Scope / DoD Check

- [x] Mapped active routes and frontend guards to API modules and backend authorization boundaries.
- [x] Audited GUEST, USER, ADMIN, INDIVIDUAL, and BUSINESS journeys.
- [x] Checked loading, empty, success, error, retry, cancellation, and stale-state behavior.
- [x] Inspected payment, whitelist, company certification, subscription, music/search, profile, and admin screens.
- [x] Reviewed React effects, request deduplication, lazy loading, component size, and list behavior.
- [x] Reviewed existing frontend tests and recorded focused missing tests without claiming unmeasured coverage.
- [x] Ran read-only typecheck, ESLint, and Prettier checks.
- [x] Preserved all existing/user changes and wrote only the two paths owned by this WI.

## Reference Documents (Tier 0-2)

**Injected / Consulted Context**:

| Tier | Document                                                       | Reason                                                          |
| ---- | -------------------------------------------------------------- | --------------------------------------------------------------- |
| 0    | `docs/standards/core-principles.md`                            | Constitution, language, traceability, and active React baseline |
| 0    | `docs/standards/development-standards.md`                      | Evidence-first QA and development rules                         |
| 0    | `docs/standards/documentation-standards.md`                    | Agent-facing deliverable structure                              |
| 0    | `docs/standards/glossary.md`                                   | Canonical subscription, playlist, and role terms                |
| 1    | `docs/policies/quality-gates.md`                               | Review and regression evidence expectations                     |
| 1    | `docs/policies/access-control-policy.md`                       | Least privilege and read-only boundary                          |
| 2    | `.claude/agents/qa-fe.md`                                      | QA-FE role and role-by-screen checklist                         |
| 2    | `docs/standards/frontend-standards.md`                         | React, Zustand, routing, API, and styling baseline              |
| 2    | `.agents/skills/react-best-practices/AGENTS.md`                | Client request deduplication, effect, and rendering guidance    |
| 2    | `docs/ui/atstudio-front-list.md`                               | Screen/API/guard inventory                                      |
| 2    | `docs/ui/screen-flow.md`                                       | Navigation and user-flow contract                               |
| 2    | `docs/design/api-spec.md` and relevant `docs/design/usecase/*` | Endpoint/state contract lookup                                  |
| 2    | `docs/client/_internal-feature-map.md`                         | Current cross-feature audit pointers                            |
| 2    | `docs/client/2-full-feature-checklist.md`                      | Current client-visible behavior expectations                    |
| 2    | `deliverables/user/REQ-20260711-ATS-001.md`                    | Approved audit requirement                                      |
| 2    | `deliverables/agent/WI-20260711-ATS-003-handoff.md`            | WI scope, constraints, DoD, and output contract                 |

**Injection Rules Applied**:

- Assignee: `qa-fe`
- Task type: frontend review / static audit
- Application source of truth: `frontend/src/`; Thymeleaf treated as legacy compatibility only.
- Execution boundary: static and read-only except the two WI deliverables; no payment/admin mutation and no browser action.

## Audit Baseline

| Metric                              |                   Observed value | Reproduction                                                                   |
| ----------------------------------- | -------------------------------: | ------------------------------------------------------------------------------ |
| Router `path:` declarations         |                               62 | `rg -n "path:" frontend/src/router/index.tsx`                                  |
| Production `.ts`/`.tsx` files       |                              112 | PowerShell recursive file count excluding `*.test.*`                           |
| Page components                     |                               54 | PowerShell count under `frontend/src/pages` excluding tests                    |
| Frontend test files                 |                               14 | `rg --files frontend/src -g "*.test.ts" -g "*.test.tsx"`                       |
| Page test files                     |                                9 | PowerShell count under `frontend/src/pages`                                    |
| Largest source component            |                      1,945 lines | `frontend/src/pages/admin/PaymentReadOnlyPage.tsx`                             |
| Existing main JS snapshot           | 320,998 raw / 105,049 gzip bytes | Read-only Node `zlib.gzipSync()` over `frontend/dist/assets/index-D-VZNp2o.js` |
| Existing largest lazy page snapshot |    38,365 raw / 8,915 gzip bytes | `PaymentReadOnlyPage-CI0ndTe_.js` in existing `dist`                           |

Notes:

- The `dist` snapshot was inspected but not regenerated because this WI cannot write build output. It is supporting evidence, not a fresh build result.
- Route-level lazy loading is implemented at `frontend/src/router/index.tsx:24-32,38-101`; no bundle-size release blocker was asserted.
- `git status --short` showed pre-existing concurrent changes under `docs/client/`, `docs/index.md`, handoff files, and `output/`. None were changed or reverted.

## Route / Role / Endpoint Map

| Surface                           | Active route(s)                                                                                         | Frontend gate                                                                       | Primary endpoint or state contract                                                                              | Evidence                                                                                                               |
| --------------------------------- | ------------------------------------------------------------------------------------------------------- | ----------------------------------------------------------------------------------- | --------------------------------------------------------------------------------------------------------------- | ---------------------------------------------------------------------------------------------------------------------- |
| Home                              | `/`                                                                                                     | Public                                                                              | `GET /tracks`, `/albums`, `/tags`                                                                               | `router/index.tsx:124`; `HomePage.tsx:44-79`                                                                           |
| Track discovery/detail            | `/tracks`, `/tracks/:trackId`                                                                           | Public; auth-dependent actions in page                                              | `GET /tracks`, `/tracks/{id}`, `/tags`, `/tags/available`; authenticated likes, playlist, download, count calls | `router/index.tsx:125-126`; `TrackListPage.tsx:124-208,500-576`; `tracks.ts:44-77`                                     |
| Album discovery/detail            | `/albums`, `/albums/list`, `/albums/:albumId`                                                           | Public; auth-dependent likes/playlist actions                                       | `GET /albums`, `/albums/{id}`, `/likes/albums`, `/likes`                                                        | `router/index.tsx:127-129`; `AlbumDetailPage.tsx:24-65,180-297`; `albums.ts:44-53`                                     |
| Subscription plans                | `/subscriptions`                                                                                        | Public                                                                              | `GET /subscriptions`; authenticated page also calls `GET /user-subscriptions/me`                                | `router/index.tsx:130`; `SubscriptionPlanPage.tsx:122-178`                                                             |
| Notices                           | `/notices`, `/notices/:noticeId`                                                                        | Public                                                                              | `GET /notices`, `/notices/{id}`, attachment download                                                            | `router/index.tsx:131-132`; `notices.ts:13-21,86-90`                                                                   |
| Password authentication           | `/login`, `/signup`, `/email-verify`, `/password-reset`                                                 | Public                                                                              | `/auth/login`, `/users`, `/users/me`, `/auth/verify-email`, forgot/reset password, public capabilities          | `router/index.tsx:135-138`; `auth.ts:89-117,145-165`                                                                   |
| Social authentication             | `/social-login/:provider`                                                                               | Public                                                                              | `POST /auth/social/{provider}` then authenticated `GET /users/me`                                               | `router/index.tsx:139`; `SocialLoginPage.tsx:21-74`; `auth.ts:99-103,120-131`                                          |
| Social profile completion         | `/complete-profile`                                                                                     | `authRequired(USER+)`                                                               | `PUT /users/me/complete-profile`, availability checks                                                           | `router/index.tsx:140`; `ProtectedRoute.tsx:7-45`                                                                      |
| Playlists                         | `/playlists`, `/playlists/:playlistId`, `/playlists/new`, `/playlists/:playlistId/edit`                 | `SubscriberRoute`                                                                   | CRUD/reorder under `/playlists`; plan limit from `/user-subscriptions/me`                                       | `router/index.tsx:143-146`; `playlists.ts:33-106`; `PlaylistListPage.tsx:35-70`                                        |
| Profile                           | `/profile`                                                                                              | `authRequired(USER+)`                                                               | `GET/PUT /users/me`, password update, subscription read                                                         | `router/index.tsx:147`; `ProfilePage.tsx:102-145,163-269`                                                              |
| Likes                             | `/likes`                                                                                                | `authRequired(USER+)`                                                               | `/likes`, `/likes/albums`; playlist/download actions                                                            | `router/index.tsx:148`; `LikeListPage.tsx:43-156`                                                                      |
| Play history                      | `/play-history`                                                                                         | `authRequired(USER+)`                                                               | Current page/store use localStorage; `/play-histories` client/backend remains unused by SPA                     | `router/index.tsx:149`; `PlayHistoryPage.tsx:1,31-77`; `playerStore.ts:50-98,189-190`; `playHistory.ts:4-25`           |
| Licenses                          | `/licenses`, `/licenses/:licenseId`                                                                     | `authRequired(USER+)`                                                               | `GET /licenses/me`, `/licenses/{id}`; track re-download                                                         | `router/index.tsx:150-151`; `licenses.ts:36-60`                                                                        |
| Download history                  | `/download-queue`                                                                                       | `SubscriberRoute`                                                                   | `/downloads/history`, `/downloads/history/track-ids`, `/utils/download-count`, `/tracks/{id}/download`          | `router/index.tsx:152`; `DownloadQueuePage.tsx:65-100,351-453`; `downloads.ts:15-77`                                   |
| Subscription checkout/callback    | checkout, legacy payment, and billing success/fail aliases                                              | `authRequired(USER+)`                                                               | `/payments/billing-agreements/prepare`, `/confirm`                                                              | `router/index.tsx:153-160`; `SubscriptionPaymentPage.tsx:47-172`; `payments.ts:70-87`                                  |
| Subscription management           | `/subscriptions/manage`                                                                                 | `authRequired(USER+)`; page effect redirects ADMIN                                  | `/user-subscriptions/me`, change preview/reactivate/cancel, `/payments/billing-agreements/me`                   | `router/index.tsx:161`; `SubscriptionManagePage.tsx:183-248,277-302`; `userSubscriptions.ts:49-129`                    |
| Whitelist channels                | `/whitelist-channels`                                                                                   | `authRequired(USER+)`; registration request requires active subscription in service | `/whitelist-channels*`, `/user-subscriptions/me`                                                                | `router/index.tsx:162`; `WhitelistChannelPage.tsx:74-225`; `whitelistChannels.ts:15-68`                                |
| Company certification             | `/company-certification/apply`, `/status`                                                               | `authRequired(USER+)`; no frontend BUSINESS gate                                    | `/company-certifications`, `/me`, `/me/documents`; backend write requires BUSINESS                              | `router/index.tsx:163-164`; `CompanyCertApplyPage.tsx:53-87,133-149`; `CompanyCertificationService.java:59-66,100-107` |
| Questions                         | `/questions`, `/questions/new`, `/questions/:questionId`                                                | `authRequired(USER+)`; list loader sends ADMIN to admin screen                      | `/questions*`; admin answer/status operations                                                                   | `router/index.tsx:165-174`; `QuestionListPage.tsx:65-83`; `QuestionDetailPage.tsx:68-113`                              |
| Error routes                      | `/error`, `*`                                                                                           | Public                                                                              | Static 500/404 surfaces                                                                                         | `router/index.tsx:176-178`                                                                                             |
| Admin content creation            | `/admin/tracks/*`, `/admin/albums*`, admin notice routes                                                | Parent `adminOnly(ADMIN)`                                                           | Admin track/album/notice APIs                                                                                   | `router/index.tsx:184-193,207-208`; `SecurityConfig.java:94-110`                                                       |
| Admin users/subscriptions/content | dashboard, users, plans, user subscriptions, licenses, questions, certification, tags, track management | Parent `adminOnly(ADMIN)`                                                           | `/admin/stats`, `/users*`, `/subscriptions/admin`, `/user-subscriptions*`, admin-protected domain endpoints     | `router/index.tsx:195-204`; `SecurityConfig.java:90-128`; `AdminStatsController.java:13-27`                            |
| Admin payment operations          | `/admin/payments`                                                                                       | Parent `adminOnly(ADMIN)`                                                           | Orders, agreements, payments, incidents, receipts, audit, settlement, refunds, entitlement correction           | `router/index.tsx:205`; `admin.ts:506-768`; `PaymentReadOnlyPage.tsx:120-267,321-573`                                  |
| Admin whitelist/settings          | `/admin/whitelist-channels`, `/admin/settings`                                                          | Parent `adminOnly(ADMIN)`                                                           | `/admin/whitelist-channels*`, `/admin/settings*`                                                                | `router/index.tsx:206,209`; `admin.ts:155-191`; `SecurityConfig.java:128`                                              |

Guard observations:

- `ProtectedRoute` defines `GUEST(0) < USER(1) < ADMIN(2)`, so ADMIN satisfies every `authRequired(USER)` route: `frontend/src/router/ProtectedRoute.tsx:7-24`.
- `SubscriberRoute` uses API success as access and redirects every rejection to plans: `frontend/src/router/SubscriberRoute.tsx:20-34,44-57`.
- Public API permits and admin restrictions are declared at `src/main/java/com/atstudio/atstudio/config/SecurityConfig.java:55-130`.

## User-Visible State Matrix

| Area                  | Loading | Empty | Success  | Error | Retry                             | Cancellation / stale control                                                                 | Result                                        |
| --------------------- | ------- | ----- | -------- | ----- | --------------------------------- | -------------------------------------------------------------------------------------------- | --------------------------------------------- |
| Social auth           | Yes     | N/A   | Intended | Yes   | Back to login                     | Processed ref only                                                                           | FAIL: access token omitted before `/users/me` |
| Track/search          | Yes     | Yes   | Yes      | Yes   | No command                        | No latest-request guard for list or available tags                                           | FAIL                                          |
| Albums/notices        | Yes     | Yes   | Yes      | Yes   | No command                        | No cancellation on list/filter requests                                                      | PARTIAL                                       |
| Playlists/download    | Yes     | Yes   | Yes      | Yes   | Mostly no command                 | Guard, PlayerBar, and page refetch subscription; modal request/timer cleanup missing         | FAIL                                          |
| Subscription/payment  | Yes     | N/A   | Yes      | Yes   | Back navigation only              | Checkout effect has active flag; subscription lookup error classification is wrong elsewhere | PARTIAL                                       |
| Whitelist             | Yes     | Yes   | Yes      | Yes   | No command                        | Channel list then subscription read is sequential; sub failure becomes `null`                | FAIL                                          |
| Company certification | Yes     | Yes   | Yes      | Yes   | No command                        | Main fetches use cancellation booleans                                                       | FAIL: INDIVIDUAL gate missing                 |
| Profile               | Yes     | N/A   | Yes      | Yes   | No load retry                     | Main fetch has cancellation boolean                                                          | FAIL: auth user remains stale                 |
| Admin                 | Yes     | Yes   | Yes      | Yes   | Settings only has explicit reload | Most list/tab requests lack latest-request guard                                             | PARTIAL                                       |

## Findings

### FE-001 - P1 - Social login fetches the current user without the new token

Evidence:

- `frontend/src/pages/auth/SocialLoginPage.tsx:44-47` receives `res.accessToken`, calls `fetchMe()` without it, and only then calls `authLogin()`.
- `frontend/src/api/auth.ts:99-103` attaches an Authorization header only when `fetchMe(accessToken)` receives an argument.
- `frontend/src/api/client.ts:16-18` otherwise reads the access token from persisted storage.
- `frontend/src/store/authStore.ts:36-46` persists the new token only inside `authLogin()`, after the failing call.
- `frontend/src/pages/auth/LoginPage.tsx:127-142` shows the working password-login sequence: `fetchMe(tokens.accessToken)` before store commit.

Impact:

- Any enabled Google/Kakao/Naver callback in a fresh session can fail at `/users/me` with 401 and show the generic social-login error.

Required follow-up:

- Pass `res.accessToken` to `fetchMe`, then commit the matching token/user pair.
- Add a `SocialLoginPage` test with empty storage, successful social token response, and asserted Authorization header/user commit.

### FE-002 - P1 - ADMIN can reach and prepare member recurring billing

Evidence:

- Policy states ADMIN is not a subscription customer and direct URL access must be blocked: `docs/SR/SR-28.md:13,25-27`.
- Header hides the subscription nav for ADMIN at `frontend/src/layouts/Header.tsx:84-92`, but `/subscriptions` remains public at `frontend/src/router/index.tsx:130`.
- Checkout/callback/manage paths use `authRequired(USER+)` at `frontend/src/router/index.tsx:153-161`; ADMIN passes the hierarchy at `frontend/src/router/ProtectedRoute.tsx:7-24`.
- Plan selection checks authentication and user type, not role, then navigates to checkout: `frontend/src/pages/public/SubscriptionPlanPage.tsx:162-178`.
- Checkout prepares a recurring billing order on mount: `frontend/src/pages/subscriber/SubscriptionPaymentPage.tsx:122-150`.
- The backend billing preparation validates subscription user type and certification but not ADMIN exclusion in the shown path: `src/main/java/com/atstudio/atstudio/service/BillingAgreementApplicationService.java:109-137`; unmatched API routes require only authentication at `SecurityConfig.java:129-130`.
- Subscriber actions also leak into ADMIN catalog UI because `TrackListPage.tsx:102,521-532` uses only `isAuthenticated`; its subscription-required callback routes to `/subscriptions` at `TrackListPage.tsx:567-575`.

Impact:

- An ADMIN can enter the member purchase flow and create billing/order state, including via a subscriber action on the public catalog.

Required follow-up:

- Add an exclusive USER/member route guard, hide member-only catalog controls for ADMIN, and enforce server-side ADMIN denial on billing/subscription commands.
- Test direct URLs, catalog CTA paths, prepare API authorization, and no-admin-order persistence.

### FE-003 - P2 - Infrastructure failures are treated as no subscription

Evidence:

- Every `fetchMySubscription()` rejection becomes `inactive` in `SubscriberRoute.tsx:25-33` and redirects at `52-57`.
- PlayerBar converts every rejection to `hasSubscription=false`: `frontend/src/layouts/PlayerBar.tsx:80-88`.
- Subscription plans swallow every rejection as no active subscription: `frontend/src/pages/public/SubscriptionPlanPage.tsx:141-147`.
- Whitelist does the same at `frontend/src/pages/subscriber/WhitelistChannelPage.tsx:78-84`.
- The established correct distinction exists in `frontend/src/pages/subscriber/SubscriptionManagePage.tsx:226-242`, which checks `isSubscriptionRequired(err)` and rethrows other errors.
- Current guard test encodes a generic `Error('inactive')` as no subscription: `frontend/src/router/SubscriberRoute.test.tsx:76-85`; it has no network/500 case.

Impact:

- Transient network, timeout, 401-refresh, or 5xx failures can redirect active subscribers, hide subscriber controls, or present a new-purchase path.

Required follow-up:

- Classify the domain error explicitly; render a retryable error for infrastructure failures.
- Deduplicate subscription state through an existing-pattern store/query layer with explicit stale/refresh semantics.

### FE-004 - P2 - INDIVIDUAL users receive a BUSINESS-only application form

Evidence:

- Both company routes are only auth-gated at `frontend/src/router/index.tsx:163-164`.
- Apply-page status 404 is interpreted as permission to render the form at `frontend/src/pages/subscriber/CompanyCertApplyPage.tsx:53-87`.
- Submit calls the write API at `CompanyCertApplyPage.tsx:133-149`.
- The service rejects non-BUSINESS users at `src/main/java/com/atstudio/atstudio/service/CompanyCertificationService.java:60-66`.
- Current acceptance wording requires INDIVIDUAL users to be blocked or clearly informed: `docs/client/2-full-feature-checklist.md:193-194`.

Impact:

- Personal members can complete document selection and only discover the restriction after a failed upload.

Required follow-up:

- Add a user-type route/page gate before fetching or rendering the form; preserve backend enforcement.
- Test direct access for INDIVIDUAL, BUSINESS, and ADMIN.

### FE-005 - P2 - Filter/tab requests can commit stale responses

Evidence:

- Track list requests update state with no cancellation or request sequence check at `frontend/src/pages/public/TrackListPage.tsx:151-186`.
- Available-tag recombination has the same race at `TrackListPage.tsx:188-208`.
- Admin payment tab/page/filter requests share global loading/error/pageInfo and have no cleanup at `frontend/src/pages/admin/PaymentReadOnlyPage.tsx:179-248`.
- Album, notice, question, and several admin list effects use the same uncancelled pattern.

Impact:

- A slower old request can overwrite a newer filter result; an old payment-tab request can clear loading or replace page metadata while the new tab is still loading.

Required follow-up:

- Use `AbortController`/Axios signal or a monotonic request ID and commit only the latest request.
- Add deferred-promise tests that resolve requests out of order.

### FE-006 - P2 - Login loses the protected deep-link target

Evidence:

- `ProtectedRoute.tsx:37-42` and `SubscriberRoute.tsx:36-57` navigate without pathname/search state.
- Already-authenticated and successful password login both navigate to `/`: `frontend/src/pages/auth/LoginPage.tsx:77-79,127-145`.
- The screen contract promises returnUrl preservation at `docs/ui/screen-flow.md:72,402`.

Impact:

- Users following a profile, license, question, or checkout deep link must rediscover the destination after login.

Required follow-up:

- Carry a same-origin `from` location through the guard and consume it after login, with an open-redirect-safe fallback.

### FE-007 - P2 - Profile success leaves global user state stale

Evidence:

- Profile update only calls local `setProfile(response.data.data)`: `frontend/src/pages/subscriber/ProfilePage.tsx:216-224`.
- Header reads `useAuthStore().user.nickname`: `frontend/src/layouts/Header.tsx:80-83,162-166`.
- `AuthState` has no user-update command, and persistence only occurs in `login`: `frontend/src/store/authStore.ts:22-29,36-46`.

Impact:

- A saved nickname is not reflected in Header and the persisted bootstrap remains old after refresh until a later login.

Required follow-up:

- Add an atomic auth-user update that writes Zustand and storage, then cover Profile-to-Header and reload behavior.

### FE-008 - P2 - Shared accessibility primitives are incomplete

Evidence:

- Toast items are clickable `div` elements without a live region, keyboard action, or button semantics: `frontend/src/components/ui/ToastContainer.tsx:18-27`.
- Pagination arrow buttons have no accessible names and the active page lacks `aria-current`: `frontend/src/components/ui/Pagination.tsx:30-62`.
- Header search inputs rely on placeholder text without a label: `frontend/src/layouts/Header.tsx:123-132,233-242`.
- Mobile player click targets are non-keyboard `div` elements: `frontend/src/layouts/PlayerBar.tsx:498-516`.
- Pagination is imported by 16 page components; no Modal/Pagination/Toast accessibility tests were found.

Impact:

- Screen-reader users may miss success/error state and cannot identify pagination arrows; keyboard-only users cannot activate some player targets.

Required follow-up:

- Add appropriate `role=status/alert` and `aria-live`, semantic buttons/links, labels, `aria-current`, focus-visible behavior, and automated accessibility tests.

### FE-009 - P2 - Add-to-playlist request and close timer outlive the modal lifecycle

Evidence:

- The load effect has no cancellation/ignore cleanup: `frontend/src/components/playlist/AddToPlaylistModal.tsx:28-53`.
- Success schedules `setTimeout(() => onClose(), 800)` without retaining/clearing the timer: `AddToPlaylistModal.tsx:55-75`.
- The only component test covers generic load error, not close/reopen or timer cleanup: `AddToPlaylistModal.test.tsx:18-39`.

Impact:

- Closing and reopening quickly can show a prior request's playlist data or allow an old success timer to close a new modal.

Required follow-up:

- Ignore/abort stale loads and clear the close timer on close/unmount; add fake-timer and deferred-request tests.

### FE-010 - P2 - Playback failures are displayed as successful playback

Evidence:

- `audio.play()` rejections are swallowed while state is immediately set to playing: `frontend/src/store/playerStore.ts:173-190`.
- `resume()` repeats the behavior at `playerStore.ts:198-200`.

Impact:

- Stream/network/codec/autoplay failures can leave pause icons and `isPlaying=true` while no audio is playing.

Required follow-up:

- Commit playing state after the play promise resolves, handle audio error/stalled events, and expose a retryable toast/state.

### FE-011 - P2 - `/playlists/new` is a no-op creation route

Evidence:

- Router exposes a subscriber-only creation path at `frontend/src/router/index.tsx:145`.
- The page only redirects to `/playlists`: `frontend/src/pages/subscriber/PlaylistCreatePage.tsx:4-9`.
- Playlist list initializes `showCreate=false` and opens it only from an internal handler: `frontend/src/pages/subscriber/PlaylistListPage.tsx:25,80-85`.
- The UI inventory calls it a playlist creation screen: `docs/ui/atstudio-front-list.md:54`.

Impact:

- Direct creation links land on the list without opening or rendering the create workflow.

Required follow-up:

- Render the create screen, or redirect with explicit state/query consumed by the list; test the deep link.

### FE-012 - P2 - Core load failures do not provide an in-screen retry

Evidence:

- Track search renders only an error `div`: `frontend/src/pages/public/TrackListPage.tsx:491-497`.
- Dashboard returns only an English error panel: `frontend/src/pages/admin/DashboardPage.tsx:21-34`.
- Notice list has the same terminal error state: `frontend/src/pages/public/NoticeListPage.tsx:33-58`.
- Whitelist renders error text but no retry command: `frontend/src/pages/subscriber/WhitelistChannelPage.tsx:301-302`.

Impact:

- Transient failures require browser refresh/back-navigation, and several Korean screens expose English or transport-oriented messages.

Required follow-up:

- Standardize retryable load-state UI and user-facing Korean error copy; distinguish empty, authorization, not-found, validation, and infrastructure failures.

### FE-013 - P3 - Screen/API documentation is stale against the active SPA

Evidence:

- Current play history is explicitly localStorage-based at `frontend/src/pages/subscriber/PlayHistoryPage.tsx:1,31-77` and `playerStore.ts:50-98,189-190`.
- The unused client still defines server play-history APIs at `frontend/src/api/playHistory.ts:4-25`; no import was found in `frontend/src`.
- UI inventory still maps the screen to GET/DELETE play-history APIs at `docs/ui/atstudio-front-list.md:65`.
- UI inventory says admin stats API is undefined at `atstudio-front-list.md:137`, while frontend and backend implement it at `DashboardPage.tsx:12-18`, `admin.ts:22-24`, and `AdminStatsController.java:13-27`.
- Route counts differ among `router/index.tsx:117`, `frontend-standards.md:305`, and `atstudio-front-list.md:161`; active router search finds 62 `path:` declarations.

Impact:

- QA and client acceptance can test the wrong persistence or API behavior and miss implemented admin coverage.

Required follow-up:

- Feed these pointers to DocOps/QA Integration and decide whether server play-history assets are retained compatibility or dead code before editing.

### FE-014 - P3 - Formatting and maintainability baseline is not enforced

Evidence:

- `npm run format` reports 143 files requiring Prettier formatting.
- `frontend-standards.md:147` prohibits native confirm, but payment/whitelist code uses it, including `PaymentReadOnlyPage.tsx:326,347,372,435,458,517,534,1943` and `WhitelistChannelManagePage.tsx:99,122`.
- Source sizes: `PaymentReadOnlyPage.tsx` 1,945 lines; `SubscriptionManagePage.tsx` 878; `api/admin.ts` 769; `ProfilePage.tsx` 636; `PlayerBar.tsx` 620; `TrackListPage.tsx` 579.

Impact:

- High-risk payment workflows, API types, and rendering logic have broad change/test surfaces; native dialogs bypass the reusable accessible modal system.

Required follow-up:

- Establish an approved formatting-baseline WI rather than mixing 143 files into feature fixes.
- Split payment operations by tab/domain hook and replace native dialogs with the existing Modal/ConfirmDialog patterns.

## Commands & Outputs

| Command                                                     | Result                                                                      |
| ----------------------------------------------------------- | --------------------------------------------------------------------------- |
| `rg --files frontend/src`                                   | Completed; active SPA inventory collected.                                  |
| `rg -n "path:" frontend/src/router/index.tsx`               | 62 path declarations including layout, aliases, and wildcard.               |
| `npm run typecheck`                                         | PASS; exit 0, no TypeScript diagnostics.                                    |
| `npm run lint`                                              | PASS; exit 0, no warnings under `--max-warnings 0`.                         |
| `npm run format`                                            | FAIL; exit 1, 143 files reported by Prettier. No files were rewritten.      |
| PowerShell production/page/test counts                      | 112 production TS/TSX, 54 pages, 14 tests, 9 page tests.                    |
| Read-only Node gzip calculation over existing `dist/assets` | Main 105,049 gzip bytes; largest lazy page 8,915 gzip bytes.                |
| `git status --short` before and after checks                | Pre-existing dirty tree unchanged; only WI outputs added during this audit. |

Not executed:

- `npm test`: handoff specifies static inspection now; Vitest may write cache and this WI owns only two output paths.
- `npm run build`: writes `dist` and TypeScript build artifacts, outside this WI's ownership.
- Browser automation/dev server: not required for the static handoff and could create Vite cache; no destructive payment/admin action was triggered.

## Test Gaps / Focused Follow-up Tests

Existing tests cover selected auth forms, guards, subscription/payment screens, download history, playlist list, profile request payloads, API refresh exclusions, and auth-store logout. Critical gaps:

| Priority | Required test                                                                                                                 | Finding |
| -------- | ----------------------------------------------------------------------------------------------------------------------------- | ------- |
| P1       | Social callback with empty storage must call `/users/me` using the returned access token and commit the same user/token pair. | FE-001  |
| P1       | ADMIN direct URL and catalog CTA must not reach plan checkout; billing prepare must reject ADMIN and persist no order.        | FE-002  |
| P2       | Subscriber guard: domain no-subscription vs 401/timeout/500 must produce different outcomes and retry behavior.               | FE-003  |
| P2       | INDIVIDUAL/BUSINESS/ADMIN matrix for company apply/status routes.                                                             | FE-004  |
| P2       | Deferred out-of-order responses for track filters and admin payment tab/page changes.                                         | FE-005  |
| P2       | Protected deep-link login round trip preserves pathname and query safely.                                                     | FE-006  |
| P2       | Profile nickname save updates Header and persisted bootstrap.                                                                 | FE-007  |
| P2       | Axe/keyboard tests for Toast, Pagination, Header search, Modal focus return, and PlayerBar controls.                          | FE-008  |
| P2       | Modal close/reopen and fake-timer cleanup around add-to-playlist.                                                             | FE-009  |
| P2       | Rejected `audio.play()` leaves `isPlaying=false` and shows recoverable feedback.                                              | FE-010  |
| P2       | `/playlists/new` opens/renders creation workflow.                                                                             | FE-011  |
| P2       | Retry command reruns failed list/admin loads without duplicating requests.                                                    | FE-012  |
| P3       | Contract test or generated inventory keeps route count/API mapping current.                                                   | FE-013  |

No percentage coverage claim is made because coverage instrumentation was not run in this WI.

## Positive Evidence

- Route pages use `React.lazy()` and `Suspense`: `frontend/src/router/index.tsx:24-32,38-101`.
- Checkout uses an active flag before committing non-redirect preparation state: `SubscriptionPaymentPage.tsx:122-172`.
- Subscription management distinguishes the no-subscription domain error from real failures: `SubscriptionManagePage.tsx:226-247`.
- Company certification fetches use cancellation booleans: `CompanyCertApplyPage.tsx:31-87`, `CompanyCertStatusPage.tsx:43-70`.
- Zustand Set mutations clone before mutation: `likeStore.ts:27-55`, `albumLikeStore.ts:27-55`.
- Modal implements Escape handling and a focus trap: `components/ui/Modal.tsx:16-60,76-95`.
- Payment refund and entitlement execution require typed confirmation: `PaymentReadOnlyPage.tsx:473-490,552-572,1942-1945`.

## Files Changed

- `deliverables/user/WI-20260711-ATS-003-summary.md` - concise Korean QA result and priority order.
- `deliverables/agent/WI-20260711-ATS-003-evidence-pack.md` - this route/role/API/state evidence pack.

No source, test, configuration, existing documentation, generated output, or unrelated user file was modified.

## Risks / Rollback

Risks:

- This is static evidence. Runtime provider availability, live API responses, browser focus behavior, responsive layout, and production bundle output were not executed in this WI.
- Existing `dist` measurements can drift from source; they are explicitly not a fresh build result.
- Backend pointers were read only to verify role and endpoint semantics; backend correctness outside the mapped frontend journeys belongs to the backend/security/integration WIs.

Rollback:

- No application rollback is required.
- If explicitly requested, remove only:
  - `deliverables/user/WI-20260711-ATS-003-summary.md`
  - `deliverables/agent/WI-20260711-ATS-003-evidence-pack.md`

## Follow-up Inputs

- `WI-20260711-ATS-006` (payment 3-way): FE-002, FE-003, payment-tab stale request evidence, and typed-confirm positive evidence.
- `WI-20260711-ATS-007` (whitelist/company certification 3-way): FE-003, FE-004, and whitelist sequential/error-state evidence.
- `WI-20260711-ATS-008` (member/subscription/search/admin 3-way): FE-001, FE-002, FE-005 through FE-014.
- Frontend test/type/lint/build WIs should consume the focused test table and preserve this WI's static/read-only limitations in their own results.
