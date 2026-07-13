# Evidence Pack: WI-20260711-ATS-008

## Summary (one-liner)

- Recovered WI-008 and independently adjudicated the current non-payment, non-whitelist, and non-certification code/doc claims: 2 confirmed P0 findings, 5 P1 findings, and explicit reductions or rejections for conditional Phase 1 overstatements.

## Scope / DoD Check

- [x] Read `WI-20260711-ATS-008-handoff.md` completely.
- [x] Used Phase 1 evidence packs WI-002, WI-003, and WI-004 plus completed WI-006 and WI-007 as inputs.
- [x] Re-verified highest-risk auth, email, role, content delivery, upload, storage, inquiry/notice, and admin claims against current code and docs.
- [x] Kept payment, whitelist, and company-certification findings in WI-006/WI-007 ownership; no duplicate final finding was created.
- [x] Applied one severity scale and recorded impact prerequisites.
- [x] Recorded explicit false-positive reductions and rejections.
- [x] Used static proof only and wrote only the two WI-008 output files.

## Baseline and Constraints

| Item | Result |
|---|---|
| Workspace | `C:\Users\jm991\Desktop\project\ATStudio` |
| Branch | `dev/kyoung` |
| HEAD | `27d22446e5d21324dadcfcb322dbe51704dfe914` |
| Initial WI-008 outputs | Both absent before this recovery |
| Worktree | Dirty before WI-008; all existing changes and cloudflared/Vite logs were preserved |
| Execution | Static PowerShell/`rg` inspection only |
| Prohibited operations | No exploit, upload, media/attachment retrieval, email, SQL/DB, provider, admin mutation, secret output, build, or test execution |

## Severity and Disposition Scale

- **P0:** release blocker with a statically reachable path to protected marketplace assets or live authentication capability disclosure.
- **P1:** high-impact security, session, core journey, or durable data-integrity defect requiring first-wave remediation.
- **P2:** material correctness, availability, race, UX, accessibility, or performance issue not proven release-blocking here.
- **Confirmed:** current repository source establishes the path and missing control.
- **Conditional:** source establishes a hazardous path, but deployment/runtime state is required for the stated maximum impact.
- **Rejected:** the asserted path is contradicted or not established by current source.

## Reference Documents (Tier 0-2)

| Tier | Document | Use |
|---|---|---|
| 0 | `docs/standards/core-principles.md` | Marketplace trust, platform integrity, evidence, language |
| 0 | `docs/standards/development-standards.md` | Security, transaction, JPA, test evidence baseline |
| 0 | `docs/standards/documentation-standards.md` | Pointer-first and duplicate-resolution rules |
| 0 | `docs/standards/glossary.md` | Canonical roles, playlist, subscription, and history terms |
| 1 | `docs/policies/security-policy.md` | Token/PII classification, logging minimization, auth baseline |
| 1 | `docs/policies/access-control-policy.md` | Least privilege and default-deny review |
| 1 | `docs/policies/quality-gates.md` | Evidence and regression expectations |
| 2 | `docs/design/api-spec.md` | Track, playlist, auth, history, settings, and admin contracts |
| 2 | `docs/design/usecase/` | Current domain intent and state semantics |
| 2 | `docs/ui/` and `docs/client/` | Active SPA and acceptance wording |
| Input | `deliverables/agent/WI-20260711-ATS-002-evidence-pack.md` | Backend hypotheses |
| Input | `deliverables/agent/WI-20260711-ATS-003-evidence-pack.md` | Frontend hypotheses |
| Input | `deliverables/agent/WI-20260711-ATS-004-evidence-pack.md` | Security/privacy hypotheses |
| Boundary | `deliverables/agent/WI-20260711-ATS-006-evidence-pack.md` | Completed payment adjudication |
| Boundary | `deliverables/agent/WI-20260711-ATS-007-evidence-pack.md` | Completed whitelist/certification adjudication |

Injection order applied: Tier 0 -> Tier 1 -> domain context -> current source snapshot.

## Cross-Domain Matrix

| Domain | Design/client intent | Current implementation | Final disposition |
|---|---|---|---|
| Auth/social/email | Social login issues tokens and completes profile; password reset is single-use; token/PII logging is forbidden (`user-info.md:40-97`; `util.md:255-312`; `security-policy.md:26-45`) | Social callback omits the new token for `/users/me`; SMTP failure logs recipient/body; password changes do not revoke refresh state | `FAIL` - ATS008-02, ATS008-04, ATS008-05 |
| User/profile/withdrawal | Generic authenticated withdrawal and password change (`user-info.md:259-321`) | Social-only user cannot satisfy password proof; profile state can remain stale; payment withdrawal defect owned by WI-006 | `PARTIAL` - P2 user-contract gaps; no duplicate P0/P1 |
| Roles/routes | ADMIN routes are exclusive; member routes use auth/subscriber guards | `/api/admin/**` and stats have ADMIN gates; frontend role hierarchy admits ADMIN to USER routes, but non-payment service ownership/subscription checks remain | `PARTIAL` - no broad non-payment P1 bypass confirmed |
| Tracks/search/stream/download/license | Public preview stream; subscriber-only original download with ledger/license (`sound-track.md:117-175`; `api-spec.md:353-400`) | Public DTO exposes master key and generic static mapping serves it outside `DownloadService` | `FAIL` - ATS008-01 |
| Playlists/albums/likes/history | Subscriber-owned playlists and optional thumbnail; public albums; persisted play-history contract | Subscriber file type is unchecked and publicly served; file compensation is inconsistent; SPA play history is localStorage while server docs/API describe persistence | `FAIL` - ATS008-03, ATS008-06; history drift is P2/P3 |
| Inquiry/notice | Owner/admin inquiry access; public notice reads/attachments | Ownership/parent binding exists; file create/delete is not transaction-safe and inquiry files are not deleted physically | `FAIL` - ATS008-06 |
| Settings/stats/admin | Public setting reads, ADMIN writes/stats | `SecurityConfig.java:78-80,127-130`; stats controller and SPA consumer align | `PASS` for P0/P1; page caps/retry/races remain P2 |
| Shared frontend/performance | Stable loading/error/role flows and bounded lists | stale-request races, deep-link loss, retry/accessibility gaps, unbounded page sizes/lists, and large components exist | `PARTIAL` - all adjudicated P2/P3 here |

## Adjudicated P0/P1 Findings

### ATS008-01 - P0 - Anonymous original-audio static retrieval

- **Class:** confirmed access-control/business-asset bypass.
- **Prerequisite:** know an active track ID; no authentication required.
- **Static chain:**
  1. Public track detail: `SecurityConfig.java:66-69`; `TrackService.java:130-137`.
  2. DTO returns the master storage key: `TrackResponse.java:10-39`.
  3. Masters are stored under `tracks/audio`: `TrackService.java:60-89`; `LocalStorageService.java:50-70`.
  4. The entire storage root is mapped to `/uploads/**`: `WebConfig.java:17-25`.
  5. Only company-document GET has a special static rule; remaining static requests are permitted: `SecurityConfig.java:80,129-132`.
  6. The bypassed download path enforces subscription/quota and writes download/license evidence: `DownloadService.java:39-86`.
- **Impact:** original paid media retrieval without subscription, quota, download history, or license issuance.
- **Doc adjudication:** public original fallback streaming is explicitly intended (`sound-track.md:117-142`; `api-spec.md:384-391`). It does not justify exposing the reusable master path or bypassing the separate subscriber download contract (`sound-track.md:146-175`).
- **Required fix:** remove `audioFile` from public DTOs, move masters outside static roots, and expose only generated previews publicly.

### ATS008-02 - P0 - Live verification/reset capability and PII logged on SMTP failure

- **Class:** confirmed secret/PII disclosure.
- **Prerequisites:** password login enabled; verification/reset mail is attempted; SMTP throws; attacker can read application or aggregated logs.
- **Evidence:** token URLs are created at `EmailService.java:46-65,88-108`; reset mutates the password at `:141-159`; every send exception logs recipient, subject, and full HTML body at `:163-180`.
- **Impact:** log readers can obtain live verification or password-reset capabilities and recipient PII. The exception is swallowed, so the API reports the normal success flow.
- **Policy conflict:** access/refresh tokens are Secrets and email is PII; sensitive logging must be minimized (`security-policy.md:26-45`).
- **Required fix:** never log recipient/body/token, emit a non-sensitive event/correlation ID, and return or persist an explicit delivery result. Any mail-preview sink must be profile-isolated.

### ATS008-03 - P1 confirmed, P0 deployment escalation - Subscriber-uploaded active content on public storage

- **Class:** confirmed unsafe upload/delivery path; Phase 1 P0 maximum impact is conditional.
- **Prerequisites for confirmed P1:** authenticated subscriber and accepted non-empty thumbnail.
- **Evidence:** playlist multipart upload is accepted at `PlaylistController.java:27-37`; the service performs no extension, MIME, signature, or image decode validation (`PlaylistService.java:39-64,178-193`); storage preserves the sanitized original extension (`LocalStorageService.java:50-70`); DTOs return the path (`PlaylistResponse.java:9-25`; `PlaylistListItemResponse.java:9-23`); `/uploads/**` is public (`WebConfig.java:20-25`; `SecurityConfig.java:129-132`).
- **Confirmed impact:** low-trust users can store and distribute active/non-image content from the application upload tree.
- **P0 escalation condition:** SPA and uploads share an authenticated production origin and browser delivery permits document execution. The repository proves same-origin Vite proxying only for development (`frontend/vite.config.ts:12-24`), not production ingress.
- **Credential impact if escalated:** access/refresh tokens and user data are in localStorage (`authStore.ts:31-53`).
- **Required fix:** decode/re-encode allowed raster images, reject SVG/HTML and mismatches, use fixed media types/disposition, and isolate uploads on a separate cookieless origin.

### ATS008-04 - P1 - Credential changes and logout do not revoke refresh sessions

- **Class:** confirmed session-lifecycle defect.
- **Prerequisite:** attacker already possesses the current refresh token.
- **Evidence:** frontend logout only clears browser stores (`authStore.ts:49-59`); no logout endpoint exists in `AuthController.java:21-74`; password change only updates the hash (`UserService.java:148-159`); reset only updates the hash (`EmailService.java:141-159`); refresh remains valid when its hash matches the single stored value (`AuthService.java:75-110`).
- **Impact:** an attacker can continue rotating a stolen refresh token after victim logout, password change, or password reset. Existing access tokens also remain valid until expiry.
- **Doc drift:** UI flow explicitly describes client-only logout with no server invalidation (`screen-flow.md:66-69`), while password docs promise only that future logins require the new password (`user-info.md:288-321`); neither defines session revocation.
- **Required fix:** add server logout/revocation and clear/version refresh sessions on password change/reset; define access-token invalidation expectations.

### ATS008-05 - P1 conditional on provider enablement - Social callback uses the old/no token

- **Class:** confirmed core-journey defect.
- **Prerequisite:** at least one OAuth provider is enabled and callback begins with no prior valid access token in storage.
- **Evidence:** callback receives `res.accessToken`, calls `fetchMe()` without it, and only then persists login (`SocialLoginPage.tsx:42-66`); `fetchMe` only adds Authorization when given a token (`auth.ts:99-103`); Axios otherwise reads persisted auth; password login demonstrates the correct order (`LoginPage.tsx:125-144`).
- **Impact:** normal fresh social login can fail at `/users/me` with 401 after provider authentication.
- **Contract conflict:** INFO-013 requires token issue followed by profile completion/navigation (`user-info.md:68-97`).
- **Required fix:** call `fetchMe(res.accessToken)` and atomically commit the matching token/user pair.

### ATS008-06 - P1 - Cross-domain file/DB compensation failures

- **Class:** confirmed durable data-integrity defect family.
- **Evidence by failure mode:**
  - New files are stored before DB completion with no rollback cleanup: track `TrackService.java:60-89`; playlist `PlaylistService.java:39-64`; album `AlbumService.java:42-61`; question `QuestionService.java:49-66,207-223`; notice `NoticeService.java:43-59,159-174`.
  - Old files are deleted before DB commit: track replacement `TrackService.java:148-179`; notice update/delete `NoticeService.java:92-132`.
  - Successful replacement/deletion leaks old files: playlist update `PlaylistService.java:178-193`; album update `AlbumService.java:113-126`; question delete removes DB rows but not blobs `QuestionService.java:173-188`.
  - Storage deletion failure is log-only with no durable retry: `LocalStorageService.java:78-87`.
- **Impact:** rollback can restore rows referencing deleted files; failed operations create orphan content; successful replacement/deletion can retain stale/private content.
- **Required fix:** one transaction-aware file mutation coordinator with partial-batch cleanup, rollback deletion of new paths, after-commit deletion of old paths, and durable retry/reconciliation.

### ATS008-07 - P1 when password registration is enabled - Public identity/mail abuse lacks coverage

- **Class:** confirmed missing abuse controls; runtime enablement affects exposure.
- **Prerequisites:** password login/registration enabled and public endpoint reachable.
- **Evidence:** registration is public (`SecurityConfig.java:55`) and persists a user before sending verification mail (`UserService.java:36-66`); exact email/phone/nickname availability is public (`SecurityConfig.java:62-65`; `UtilController.java:36-57`); the limiter covers only login, forgot/reset, and refresh (`AuthRateLimitFilter.java:58-76`). Password login defaults enabled (`application.yml:81-90`).
- **Impact:** account enumeration plus repeated unique registration can consume DB, token, and outbound-mail/log capacity.
- **Required fix:** genericize identity responses where feasible, rate-limit/challenge registration and checks by normalized identifier and validated client identity, and expire unverified accounts.

## Downsized / Rejected Phase 1 Claims

| Claim | Final adjudication | Reason |
|---|---|---|
| `PG-004-02` is unconditional P0 same-origin token theft | **Downsized to P1; P0 conditional** | Unsafe storage/public delivery is proven. Production same-origin ingress and exact document response behavior are not repository-proven. |
| `PG-004-06` proves a global auth outage behind a proxy/tunnel | **External verification required** | Keying by `request.getRemoteAddr()` is proven (`AuthRateLimitFilter.java:46-48`), but whether all clients collapse to one origin address depends on trusted-proxy/runtime topology. No cloudflared log was inspected. |
| Bootstrap shared ADMIN is currently exposed | **Conditional startup hazard, not confirmed compromise** | A shared default exists and runner lacks a profile guard (`TestUserBootstrapProperties.java:14-22`; `TestUserBootstrapRunner.java:33-68`), but bootstrap defaults disabled (`application.yml:87-90`). Verify state without printing values. |
| ADMIN satisfying USER routes is a broad non-payment P1 bypass | **Rejected as broad claim** | Frontend role hierarchy is inclusive (`ProtectedRoute.tsx:7-24`), but admin APIs are exclusive and playlist/question/license services enforce subscription/ownership. The confirmed payment-role defect remains WI-006. |
| Public original stream fallback is itself the paid-download bypass | **Rejected; policy ambiguity only** | Design and code deliberately allow original fallback (`TrackService.java:140-145`; `sound-track.md:117-142`). ATS008-01 is the independent raw static retrieval bypass. |
| Current runtime still uses the historical JWT fallback | **Rejected for current config** | `application.yml:48-52` requires `${JWT_SECRET}`. No historical secret value or repository history was read in this WI. Rotation/history cleanup remains a separate security operation if applicable. |
| Wildcard tunnel CORS currently enables credential theft | **Downsized to P2 hardening** | Pattern plus credentials is present (`CorsConfig.java:15-29`), but bearer Authorization/localStorage credentials are not ambient cross-origin credentials. Risk increases if cookie auth is introduced. |
| Unbounded pages/lists, request races, accessibility, and large components are P1 | **Downsized to P2** | Source proves missing caps/cancellation and maintainability risk, but no release-blocking failure or measured exhaustion was established statically. |

## P2/P3 Follow-up Inventory

- OAuth provider token payload lacks typed required-field validation: `OAuth2Service.java:121-159`.
- Social-only accounts cannot satisfy password-based withdrawal: `UserService.java:104-112`; generic withdrawal contract `user-info.md:259-284`.
- Subscriber infrastructure failures are treated as no subscription: `SubscriberRoute.tsx:20-57`.
- Track/admin list requests can commit stale responses: `TrackListPage.tsx:151-208`; payment subset remains WI-006.
- Login loses the documented return target: `ProtectedRoute.tsx:37-42`; `screen-flow.md:71-73,395-403`.
- Profile update leaves persisted/global auth user stale: `ProfilePage.tsx:216-224`; `authStore.ts:22-46`.
- Playback promises, playlist modal timers, `/playlists/new`, retry UX, and accessibility gaps remain as WI-003 P2 items.
- Album `trackCount` sorting occurs only within a page: `AlbumService.java:74-94`.
- Malformed stream Range can become a generic 500: `TrackController.java:90-137`.
- Public/admin sizes lack a central upper bound: `RequestDTO.java:15-26`; `TrackService.java:92-121`; `NoticeService.java:62-77`; `AlbumService.java:74-94`; `UserService.java:173-180`.
- Play-history docs/API describe server persistence while the active SPA uses localStorage; resolve as intentional compatibility or dead code before editing docs.
- Admin stats are implemented and role-gated: `AdminStatsController.java:13-28`; `admin.ts:13-25`; `DashboardPage.tsx:7-90`; `api-spec.md:3647-3661`. Earlier “undefined API” wording is stale.

## Test Evidence and Gaps

### Executed

- No Gradle, npm, browser, HTTP, DB, provider, mail, upload, or media tests were run. The handoff requires static proof and permits writes only to the two WI outputs.

### Existing coverage inspected

- Track tests currently assert the exposed `audioFile` path: `TrackServiceTest.java:62-84,288-302`.
- Auth refresh tests cover mismatch/rotation, not logout/password-change/reset revocation: `AuthServiceTest.java:159-176` and related suite.
- `EmailServiceTest.java:35-55` covers disabled password login only; no SMTP exception log-capture test exists.
- `UserServiceTest.java:199-267` covers password update outcomes, not refresh-token invalidation.
- Playlist/question/notice tests cover normal storage/repository calls but no real transaction rollback or active-content rejection.
- No `SocialLoginPage` test file/match was found.
- No security-chain test covers anonymous `/uploads/tracks/audio/**` or active playlist upload delivery.

### Required focused tests

1. Anonymous resource-handler integration test: public previews allowed, `/uploads/tracks/audio/**` denied, public DTO omits master path.
2. Captured-log mail-failure tests: no token, URL, email, nickname, subject/body, or raw exception payload.
3. Playlist upload tests: reject HTML/SVG, MIME/signature mismatch, non-image bytes, oversized dimensions, and polyglots; verify decoded derivative output.
4. Session integration tests: logout, password change, and password reset invalidate the prior refresh token; define access-token expiry behavior.
5. Social callback test with empty storage: `/users/me` receives returned access token and the same token/user pair is committed.
6. Real Spring transaction tests for file create/replace/delete commit and rollback across track, playlist, album, question, and notice; include durable failed-delete retry.
7. Registration/availability abuse tests for per-IP/account/normalized-identifier keys, generic responses, proxy handling, and unverified-account cleanup.
8. Boundary tests for centrally clamped page sizes and deferred-response latest-request-wins behavior.

## Commands and Outputs

All commands were static/read-only except creation of the two owned outputs.

- `git branch --show-current`; `git rev-parse HEAD`; `git status --short`
  - Result: baseline recorded above; pre-existing modifications, deletions, untracked WI files, logs, and output directories were not altered.
- Complete reads of WI-008 handoff and WI-002/003/004/006/007 evidence packs.
  - Result: Phase 1 overlap was de-duplicated and payment/whitelist/certification ownership preserved.
- Numbered `Get-Content` and targeted `rg` over current security config, storage, controllers, services, DTOs, React routes/stores/pages, design docs, client docs, and tests.
  - Result: findings and exact pointers above.
- Static test inventory search.
  - Result: no SocialLoginPage test and no targeted static-resource/session-revocation/log-redaction integration coverage found.

## Evidence Pointers

- Files created:
  - `deliverables/user/WI-20260711-ATS-008-summary.md` - Korean cross-domain verdict.
  - `deliverables/agent/WI-20260711-ATS-008-evidence-pack.md` - this matrix, adjudication, evidence, and test pack.
- Highest-risk source anchors:
  - `src/main/java/com/atstudio/atstudio/config/SecurityConfig.java:53-132`
  - `src/main/java/com/atstudio/atstudio/config/WebConfig.java:17-25`
  - `src/main/java/com/atstudio/atstudio/dto/track/TrackResponse.java:10-39`
  - `src/main/java/com/atstudio/atstudio/service/EmailService.java:46-180`
  - `src/main/java/com/atstudio/atstudio/service/PlaylistService.java:39-64,178-193`
  - `src/main/java/com/atstudio/atstudio/service/auth/AuthService.java:38-110`
  - `frontend/src/pages/auth/SocialLoginPage.tsx:21-74`
  - `frontend/src/store/authStore.ts:31-59`

## Risks / Limitations / Rollback

- Production ingress, upload origin, reverse-proxy forwarding, runtime feature flags, log-reader population, storage volume, and provider state were not available and are not claimed as verified.
- Static proof establishes reachable control flow and absent controls; it does not execute an exploit or quantify production frequency.
- File/line pointers match the recorded HEAD and current dirty worktree snapshot and may drift with concurrent edits.
- No secrets, raw media, attachments, PII values, or log contents were read into this pack.
- Rollback, only if explicitly requested: remove the two WI-008 output files and no others.

## Follow-ups / WI Chain

- Handoff declares WI-008 blocks WI-009 and WI-018.
- WI-009 should consume the focused backend/security tests above, beginning with ATS008-01, ATS008-02, ATS008-04, and ATS008-06.
- WI-018 should consume the adjudicated remediation review after code/design changes exist.
- Any production code, schema, deployment, or policy remediation requires its own approved REQ/WI chain.
