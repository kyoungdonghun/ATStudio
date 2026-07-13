# Evidence Pack: WI-20260711-ATS-004

## Summary (one-liner)

Static privacy and security review of the current ATStudio code found **3 CRITICAL, 7 HIGH, 3 MEDIUM, and 3 LOW** issues. Release disposition is **BLOCK** until PG-004-01 through PG-004-03 are removed and the P1 upload, abuse-control, and session-lifecycle findings have owners and verified fixes.

No exploit, malicious upload, token replay, external penetration attempt, or production request was performed. Secret values, tokens, billing keys, card data, and personal documents are intentionally omitted.

## Scope / DoD Check

| Acceptance item | Result | Evidence |
|---|---|---|
| Endpoint authorization and ownership | Complete | `SecurityConfig.java:54-132`; controller `@PreAuthorize` checks; service ownership helpers listed under Positive Controls |
| Token/session lifecycle, CORS, callbacks, upload/download controls | Complete | Findings PG-004-01 through PG-004-12 |
| Logs, DTOs, entities, provider payloads, and frontend persistence | Complete | Findings PG-004-02, PG-004-03, PG-004-07, PG-004-09, PG-004-10, PG-004-13 and Positive Controls |
| Exploitable vs. hardening distinction | Complete | Each finding states preconditions and current mitigating boundaries |
| DoS and unbounded-resource controls | Complete | PG-004-05, PG-004-06, PG-004-08, PG-004-11 |
| Exact attack paths and narrow pointers | Complete | Detailed attack paths and finding table below |
| Missing security-test coverage | Complete | Security Test Gaps section |

## Review Baseline

- Workspace: `C:\Users\jm991\Desktop\project\ATStudio`
- Branch: `dev/kyoung`
- HEAD at review: `27d22446e5d21324dadcfcb322dbe51704dfe914`
- Worktree was already dirty with unrelated user/agent changes. None were altered or reverted.
- Authorized writes: this evidence pack and `deliverables/user/WI-20260711-ATS-004-summary.md` only.

## Reference Documents (Tier 0-2)

**Injected Context** (from the WI handoff packet):

| Tier | Document | Use |
|---|---|---|
| 0 | `docs/standards/core-principles.md:183-203` | Marketplace trust, creator assets, upload validation, auditable financial transactions |
| 1 | `docs/policies/security-policy.md:19-45,93-115` | Secret/PII classification, log minimization, current auth boundary |
| 1 | `docs/policies/access-control-policy.md:14-23` | Least privilege and default deny |
| 1 | `docs/policies/quality-gates.md:43-58` | Regression evidence and PG review gate |
| 2 | `docs/design/usecase/sound-track.md:99-175` | Public preview versus subscriber-only original download contract |
| 2 | `docs/design/usecase/company-certification.md:89-92,129-142,182-198` | Sensitive-document metadata and admin-only review contract |
| 2 | `docs/design/api-spec.md:353-400,3113-3125,3226-3239` | Public track DTO, stream/download split, certification response/download contract |
| 2 | `docs/payment/system-overview.md:32-38,153-170` | Server-confirmed payment and forbidden raw payment data |
| Audit | `docs/audit/backend-audit-report.md:35-493` | Historical security findings, re-verified rather than trusted |
| WI | `deliverables/agent/WI-20260711-ATS-004-handoff.md:8-61` | Scope, DoD, output contract, static-only constraint |

**Injection Rules Applied**:

- Rule source: `.claude/config/context-injection-rules.json`
- Assignee: `pg`
- Task type: `security`
- Required tiers: Tier 0 constitution and Tier 1 security/access/quality policies; Tier 2 design, payment, and SR context was loaded as directed by the handoff.

## Evidence Pointers

- Files changed:
  - `deliverables/user/WI-20260711-ATS-004-summary.md` - concise Korean release decision and prioritized findings.
  - `deliverables/agent/WI-20260711-ATS-004-evidence-pack.md` - threat findings, exact evidence, historical re-verification, test gaps, and follow-up inputs.
- Release-blocking locations:
  - `src/main/java/com/atstudio/atstudio/dto/track/TrackResponse.java:18,38` - public DTO carries the original audio path.
  - `src/main/java/com/atstudio/atstudio/config/WebConfig.java:21-24` - the entire upload root is statically exposed.
  - `src/main/java/com/atstudio/atstudio/service/PlaylistService.java:39-54` - subscriber thumbnail reaches generic storage without content validation.
  - `src/main/java/com/atstudio/atstudio/service/EmailService.java:163-180` - SMTP failure logs recipient and full HTML body.
- Policy-decision locations:
  - `docs/design/usecase/sound-track.md:123-175` - preview/original download and licensing contract.
  - `docs/design/usecase/company-certification.md:182-198` - sensitive documents require authenticated admin review.
  - `docs/policies/security-policy.md:43-45` - sensitive values must be minimized in logs.

## Threat Findings

| ID | Severity | Type | Preconditions and affected assets | Current evidence | Remediation direction |
|---|---|---|---|---|---|
| PG-004-01 | **CRITICAL / P0** | Exploitable access-control/business-asset bypass | None beyond knowing an active track ID. Original catalog audio, subscription revenue, download ledger, and license auditability are affected. | `SecurityConfig.java:66-69,131-132`; `TrackController.java:69-75`; `TrackResponse.java:18,38`; `TrackService.java:66,79,130-145`; `WebConfig.java:21-24`; `DownloadService.java:40-86` | Remove original path from public DTO; put originals outside all static roots; serve only generated previews publicly; keep originals behind the authorized download service. |
| PG-004-02 | **CRITICAL / P0** | Exploitable stored active-content / same-origin XSS | Attacker has a subscriber account; victim navigates to the uploaded URL; uploads and SPA share an origin. Victim access/refresh tokens, persisted user data, and potentially admin authority are affected. | `PlaylistController.java:27-31`; `PlaylistService.java:39-54,179-188`; `LocalStorageService.java:51-70`; `WebConfig.java:21-24`; `SecurityConfig.java:131-132`; `frontend/vite.config.ts:12-24`; `frontend/src/store/authStore.ts:31-53` | Reject HTML/SVG; decode/re-encode allowed raster images; isolate uploads on a cookieless origin; fixed media types, CSP, and `Content-Disposition`; move refresh token to an HttpOnly Secure SameSite cookie or equivalent protected session design. |
| PG-004-03 | **CRITICAL / P0** | Exploitable token and PII logging | SMTP send fails and an actor can read application/aggregated logs. Live verification/reset capability URLs and recipient data are affected. | `EmailService.java:53-65,96-108,163-180`; `security-policy.md:43-45` | Never log body or raw recipient; use a non-sensitive event/correlation ID and masked address; make any local mail preview explicitly local-profile-only; add log-capture redaction tests. |
| PG-004-04 | **HIGH / P1** | Exploitable malicious-document upload | Attacker has a BUSINESS account; admin follows the expected review flow and opens the attachment. Admin workstation/session and submitted-document store are affected. | `ValidationConstants.java:46-49`; `CompanyCertificationService.java:233-276`; `CompanyCertificationController.java:98-112`; `company-certification.md:129-142` | Validate extension plus server-detected MIME plus magic/signature/parser result; reject mismatch; quarantine and scan; consider CDR and macro policy; return a safe fixed content type. |
| PG-004-05 | **HIGH / P1** | Exploitable public registration/mail/identity abuse | Anonymous attacker submits unique identities repeatedly. User DB, outbound mail capacity/cost, verification-token storage, and identity privacy are affected. | `SecurityConfig.java:55,63-65`; `AuthRateLimitFilter.java:58-76`; `UserService.java:36-65,161-170`; `UtilController.java:36-57` | Rate-limit registration and availability checks by validated client IP and normalized identifier; genericize identity responses; add challenge/queue controls and expiry cleanup for unverified accounts. |
| PG-004-06 | **HIGH / P1, deployment-conditional** | Exploitable global auth availability failure | Origin receives one proxy/tunnel address for many clients. Login, forgot/reset password, and refresh availability are affected. | `AuthRateLimitFilter.java:44-76`; limits at `AuthRateLimitProperties.java:14-18`; no trusted forwarded-IP resolver found; tunnel origin is always enabled at `CorsConfig.java:23-28` | Resolve client IP only from headers supplied by trusted proxy ranges; combine per-account and per-IP keys; use shared storage for multi-instance deployments; test a Cloudflare/proxy topology. |
| PG-004-07 | **HIGH / P1** | Exploitable session persistence after credential change | Attacker already has a refresh token. Account confidentiality and integrity remain exposed after user logout/password reset/change. | `frontend/src/store/authStore.ts:49-53`; `UserService.java:149-159`; `EmailService.java:141-159`; `User.java:65-70,103-105`; `service/auth/AuthService.java:75-110` | Add server logout/revocation; clear all refresh sessions on password reset/change; use session IDs/token families, reuse detection, and a session-management UI. |
| PG-004-08 | **HIGH / P1** | Exploitable authenticated storage DoS | Any authenticated user repeatedly uploads allowed request-size attachments, then may delete questions without deleting blobs. Storage capacity and application availability are affected. | `QuestionCreateRequest.java:33-35`; `QuestionService.java:185-188,207-223`; `application.yml:44-46`; tests `QuestionServiceTest.java:448-500` | Add count/type/per-file/per-user quotas and rate limits; delete physical blobs transactionally or via durable cleanup; monitor orphan volume and storage headroom. |
| PG-004-09 | **HIGH / P1, validity-conditional** | Repository signing-secret residue | Former runtime fallback was ever used or remains accepted in an environment. JWT integrity and all account roles are affected. | Current runtime is fail closed at `application.yml:48-52` and `JwtConfig.java:23-43`, but the former literal remains in `backend-audit-report.md:108,115`; value not reproduced. | Redact current file and repository history where feasible; rotate every environment that could have used it; revoke sessions; add secret-scanning gates. |
| PG-004-10 | **HIGH / P1, enablement-conditional** | Shared bootstrap admin credential | `app.bootstrap.test-users.enabled` is accidentally true outside a protected non-production profile. ADMIN authority and fixture PII are affected. | Non-empty shared default at `TestUserBootstrapProperties.java:14-22`; property-only activation and ADMIN creation at `TestUserBootstrapRunner.java:33-37,52-68`; no profile guard found | Remove code default; require an external secret; add explicit local/stage profile guard and production startup refusal; avoid logging fixture identifiers. |
| PG-004-11 | **MEDIUM / P2** | Exploitable public resource amplification | Anonymous attacker sends very large positive `size` values; impact scales with table volume and concurrency. DB, heap, CPU, and response bandwidth are affected. | `RequestDTO.java:15-26`; `TrackController.java:43-46`; `TrackService.java:99`; `NoticeController.java:41-46`; `NoticeService.java:62-67`; `AlbumController.java:44-49`; `AlbumService.java:74-76`; public grants at `SecurityConfig.java:66,75,104` | Clamp all pageable requests centrally, for example 1-100; validate DTO/controller parameters; add query timeout, rate limit, and boundary tests. |
| PG-004-12 | **MEDIUM / P2** | Hardening/configuration exposure | Any attacker-controlled `trycloudflare.com` subdomain is trusted in every profile. Current bearer tokens are not ambient credentials, limiting immediate impact; future cookie auth increases it. | `CorsConfig.java:15-32`; bearer header injection at `frontend/src/api/client.ts:14-18` | Use exact environment-provided origins; gate tunnel support to local/stage; fail closed in production; do not combine wildcard origin patterns with credentials. |
| PG-004-13 | **MEDIUM / P2** | Sensitive validation and recipient logging | Invalid registration/reset input or normal/failing email send reaches logs. Passwords, reset tokens, email addresses, and phone numbers can be included. | `GlobalExceptionHandler.java:70-75`; `RegisterRequest.java:28-43`; `ResetPasswordRequest.java:14-19`; `EmailService.java:172-179` | Log error code and field name only; central redaction; never log rejected values, mail body, or raw recipient. |
| PG-004-14 | **LOW / P3** | Sensitive-document metadata minimization | Authenticated applicant or admin receives storage topology. Direct content access still requires ADMIN GET and per-file stored paths are hidden. | `CompanyCertificationResponse.java:18,34`; `/me` and admin mapping at `CompanyCertificationService.java:139-144,181-185`; path includes user ID at `CompanyCertificationService.java:310-315`; protected GET at `SecurityConfig.java:80`; per-file DTO at `CompanyCertificationDocumentResponse.java:7-20` | Remove `documentPath` from both response variants and API spec; expose opaque document IDs and the admin download API only. |
| PG-004-15 | **LOW / P3** | OAuth error-handling availability | Provider returns a non-null response with missing or non-string `access_token`. Social login can fail with null/cast behavior rather than a controlled error. | `OAuth2Service.java:128-131,142-145,156-159`; current tests supply a normal token and null-response cases but no missing/wrong-type field | Validate presence, type, and non-blank value before use; add provider-specific negative tests. |
| PG-004-16 | **LOW / P3** | Authentication semantics | Withdrawn users are converted to `UsernameNotFoundException`, producing 401 rather than the documented deactivated-account 403. No access bypass was found. | `CustomUserDetailsService.java:18-23`; historical item `backend-audit-report.md:485-491` | Map a deactivated account to the dedicated disabled/deactivated path without revealing account existence to anonymous callers. |

## Detailed Attack Paths

### PG-004-01: anonymous original audio retrieval

1. `GET /api/tracks/{trackId}` is public: `SecurityConfig.java:66-69` and `TrackController.java:69-75`.
2. The active track detail is returned through `TrackResponse.from`: `TrackService.java:130-137`.
3. `TrackResponse` includes `audioFile` and maps the master storage path: `TrackResponse.java:18,29-40`.
4. New masters are stored under `tracks/audio` and assigned directly to the entity: `TrackService.java:61-79`.
5. `WebConfig` maps every `/uploads/**` request to the same storage root, while unmatched static resources are permitted: `WebConfig.java:21-24`; `SecurityConfig.java:131-132`.
6. An anonymous caller can therefore request `/uploads/{audioFile}` without entering `DownloadService`.
7. The bypassed service normally checks subscriber/admin status and daily quota, writes `track_downloads`, issues a license, increments download count, and only then loads the original: `DownloadService.java:40-86`.

The documented preview fallback does not reduce severity. Public stream intentionally falls back to the original when no preview exists (`TrackService.java:141-145`; `sound-track.md:123-142`), but direct static retrieval discloses the original even when a preview exists and bypasses all logging/licensing controls. The public frontend declares the field and propagates it into player state (`frontend/src/api/tracks.ts:9-23,68-71`; `frontend/src/pages/public/TrackDetailPage.tsx:141-150`), while actual playback uses `/api/tracks/{id}/stream` (`frontend/src/store/playerStore.ts:173-175`). The public DTO therefore does not need the storage path for playback.

### PG-004-02: subscriber upload to same-origin token theft

1. An authenticated subscriber submits any `MultipartFile thumbnail`: `PlaylistController.java:27-31`; subscriber gate at `PlaylistService.java:39-43`.
2. The service passes the file directly to generic storage without extension, MIME, magic-byte, or image decoding checks: `PlaylistService.java:51-54,185-188`.
3. Storage strips path components but preserves the original filename and extension after a short random prefix: `LocalStorageService.java:51-70`.
4. Playlist DTOs return the stored path: `PlaylistResponse.java:10-22`; `PlaylistListItemResponse.java:10-20`.
5. The SPA converts it to `/uploads/...`, and the development proxy keeps `/api` and `/uploads` on the SPA origin: `frontend/src/api/client.ts:158-165`; `frontend/vite.config.ts:12-24`.
6. All uploads are statically permitted: `WebConfig.java:21-24`; `SecurityConfig.java:131-132`.
7. Local Spring MVC resource-handler bytecode inspection confirmed filename-derived media type is set on the response; no forced attachment behavior was found. No exploit request was made.
8. Direct navigation to an uploaded HTML document, or an active SVG document opened as a document, can therefore execute same-origin script. Rendering SVG only as an `<img>` is not the asserted execution path.
9. Access token, refresh token, and serialized user data are persisted in `localStorage`: `frontend/src/store/authStore.ts:31-53`; wrapper implementation at `frontend/src/utils/safeStorage.ts:9-35`.

Severity is CRITICAL when uploads share the authenticated application origin because the chain can cross from a subscriber account into any victim account, including an administrator induced to open the URL. It would be reduced if active content were impossible and uploads were isolated on a separate, cookieless origin.

### PG-004-03: SMTP failure to live account token in logs

1. Verification and reset flows create live token URLs and interpolate them into HTML bodies: `EmailService.java:53-65,96-108`.
2. `sendEmail` catches every SMTP exception instead of failing or using a protected queue: `EmailService.java:163-174`.
3. The fallback logs raw recipient, subject, and the full HTML body: `EmailService.java:175-180`.
4. Anyone with application or log-aggregator read access can recover a still-valid capability URL after a production mail failure and act as the user.

This directly conflicts with log minimization in `docs/policies/security-policy.md:43-45`. The test suite has no log-capture assertion for mail failure: `EmailServiceTest.java:35-55` only covers disabled password-login behavior.

### PG-004-04 and PG-004-14: company-document boundary

- Accepted file controls are count, filename extension, and size only: `ValidationConstants.java:46-49`; `CompanyCertificationService.java:233-263`.
- Client-supplied `MultipartFile.getContentType()` is stored without validation: `CompanyCertificationService.java:266-276`.
- The admin API uses that stored type and forces attachment disposition: `CompanyCertificationController.java:98-112`.
- The document is bound to its certification ID before download: `CompanyCertificationService.java:190-200`.
- Per-document DTO does not expose `storedPath`: `CompanyCertificationDocumentResponse.java:7-20`.
- `CompanyCertificationResponse` nevertheless exposes a directory URL containing user ID and a short submission UUID to applicant and admin: `CompanyCertificationResponse.java:18,34`; `CompanyCertificationService.java:139-144,181-185,310-315`.
- The use-case contract calls these sensitive documents and requires authenticated admin review: `company-certification.md:182-198`.

Decision: extension-only validation is HIGH because a low-trust business applicant can supply a disguised document that an admin is expected to open. Directory metadata exposure is LOW because current Spring GET protection and hidden per-file paths prevent a demonstrated direct download; it still violates least disclosure and should be removed. If a reverse proxy serves the upload directory without Spring Security, both the metadata and content exposure must be reclassified upward.

### PG-004-05, PG-004-06, PG-004-08, PG-004-11: abuse and availability

- Registration is public at `SecurityConfig.java:55`, creates a persistent user and sends mail at `UserService.java:36-65`, but the limiter only maps login/forgot/reset/refresh at `AuthRateLimitFilter.java:58-76`.
- Public availability checks disclose exact email/phone/nickname existence: `SecurityConfig.java:63-65`; `UtilController.java:36-57`; `UserService.java:161-170`.
- Auth windows are keyed by method, URI, and `request.getRemoteAddr()`: `AuthRateLimitFilter.java:44-48`. Behind a local Cloudflare Tunnel or reverse proxy, one origin-side address can become a global bucket.
- Public list sizes have lower bounds but no upper bounds: `RequestDTO.java:15-26`; `TrackService.java:99`; `NoticeService.java:62-67`; `AlbumService.java:74-76`.
- Inquiry uploads are only constrained by global per-request multipart limits and have no persistent quota or physical cleanup: `application.yml:44-46`; `QuestionService.java:185-188,207-223`.

## Upload Trust-Boundary Matrix

| Upload surface | Uploader trust | Validation observed | Delivery boundary | Assessment |
|---|---|---|---|---|
| Playlist thumbnail | Authenticated subscriber | Empty check only | Path returned and same-origin `/uploads/**` public | **CRITICAL active-content chain**, PG-004-02 |
| Company certification | Authenticated BUSINESS user | Count, size, extension only; client MIME persisted | Static GET admin-gated; normal review via ADMIN attachment API | **HIGH malicious-document risk**, plus LOW path metadata |
| Question attachment | Any authenticated user | Empty check only; global multipart limits | Owner/admin API controls observed; stored path not normally returned | **HIGH storage DoS/orphan risk**; active-content direct path is less reachable than playlist path |
| Track audio/thumbnail | ADMIN only | No explicit allowlist/MIME/magic image validation before store | Audio path currently public; thumbnails public | Original-audio disclosure is PG-004-01; upload active-content risk is lower-trust-boundary hardening because uploader is ADMIN |
| Album thumbnail | ADMIN only | Empty check only | Public static thumbnail path | Hardening required, but not the subscriber-to-victim chain |

Relevant role gates: `PlaylistService.java:39-54`; `CompanyCertificationController.java:35-52`; `TrackController.java:28-34,140-146`; `AlbumController.java:28-33,66-71`.

## Historical Audit Re-verification

The remediation table in `docs/audit/backend-audit-report.md:35-78` was treated as untrusted history. Security-relevant CRITICAL/MAJOR/MINOR entries were checked against current code.

| Historical ID | Current result | Current evidence |
|---|---|---|
| CR-P-001 `/api/users/me` wildcard | **Fixed** | User `/me` matchers precede admin wildcard: `SecurityConfig.java:83-93`; positive role tests: `SecurityFilterChainTest.java:135-204` |
| CR-P-004 / CR-C-009 JWT fallback | **Runtime fixed; repository residue open** | Environment-only secret at `application.yml:48-52`; fail-closed Base64/256-bit checks at `JwtConfig.java:23-43`; former literal remains in audit lines `108,115`, not reproduced here |
| CR-C-001 question child delete | **DB issue fixed; physical-file cleanup still open** | Child rows deleted at `QuestionService.java:185-188`; no `storageService.delete` in that path; tests `QuestionServiceTest.java:448-500` only assert repository deletion |
| CR-C-003 deleted users in admin list | **Fixed** | Query requires `u.isDeleted = false`: `UserRepository.java:30-37` |
| CR-C-008 public TestController | **Fixed in current tree** | No `TestController` file or route found; unmatched API paths require auth at `SecurityConfig.java:129-130` |
| CR-A-009 internal audio path | **Open and escalated to CRITICAL** | `TrackResponse.java:18,38`; public endpoint/static root chain in PG-004-01; tests still assert path exposure at `TrackServiceTest.java:79-84,298-301` |
| CR-B-005 YouTube `contains()` bypass | **Fixed** | Parsed URI host exact/suffix validation: `WhitelistChannelService.java:166-176` |
| CR-C-013 OAuth token field null check | **Open, LOW** | All providers cast `response.get("access_token")` directly: `OAuth2Service.java:128-131,142-145,156-159` |
| CR-P-005 expired refresh accepted | **Fixed** | Expired token rejected in `AuthService.java:79-85`; regression test `AuthServiceTest.java:130-146` |
| CR-C-014 AccessDenied catch-all | **Fixed** | Dedicated handler at `GlobalExceptionHandler.java:47-50` |
| CR-C-015 withdrawn login status | **Open, LOW semantic issue** | Withdrawn account is mapped to `UsernameNotFoundException`: `CustomUserDetailsService.java:18-23` |

## Positive Controls Verified

- Security is stateless and CSRF is disabled for bearer-token APIs: `SecurityConfig.java:35-38`.
- Admin API catch-all is role-gated before authenticated API catch-all: `SecurityConfig.java:127-130`.
- Playlist mutations bind the playlist to the current owner: `PlaylistService.java:93-104,270-279`.
- Question read/download paths check private-question owner/admin access and bind attachment to question: `QuestionService.java:138-158,198-205`.
- Whitelist mutations bind channel ownership: `WhitelistChannelService.java:189-192`.
- User license reads use current-user-bound repository methods: `LicenseService.java:42-50`.
- Certification download binds both document and certification IDs: `CompanyCertificationService.java:190-200`.
- Payment confirmation and cancellation use owned orders: `PaymentApplicationService.java:100-126,142-148,242-248`; recurring confirmation validates owned order and persisted amount: `BillingAgreementApplicationService.java:161-175,310-348,413-424`.
- Billing keys are encrypted with authenticated encryption and separately fingerprinted: `BillingKeyCrypto.java:30-49,77-88,105-110`; DTOs omit raw key material: `BillingAgreementResponse.java:10-18`; `AdminBillingAgreementResponse.java:10-22`.
- Toss provider evidence is reduced to explicit support-safe fields and masked method data: `TossBillingProvider.java:448-468,541-598`, aligned with `docs/payment/system-overview.md:153-170`.
- Swagger is disabled by default through configuration: `application.yml:73-78`.

## Security Test Gaps

1. No test proves a public track detail omits the original storage path or that `/uploads/tracks/audio/**` is denied. Existing tests assert the insecure field: `TrackServiceTest.java:79-84,298-301`.
2. No test rejects HTML, SVG, MIME mismatch, or non-image bytes for playlist thumbnails. Current playlist tests exercise normal CRUD only.
3. No security-chain test covers `/uploads/**`, including company-document GET/HEAD behavior and public media allowlists.
4. Company certification tests assert `documentPath` exposure (`CompanyCertificationServiceTest.java:87`) and do not cover magic-byte/MIME mismatch or malicious document quarantine.
5. `EmailServiceTest.java:35-55` has no SMTP-exception log-capture test and no assertion that token URLs, body, recipient, and nickname are absent from logs.
6. Rate-limit tests use a single explicit `remoteAddr` (`SecurityFilterChainTest.java:90-104,255-258`) but do not model multiple users behind one proxy, trusted forwarding headers, signup, or identity-check endpoints.
7. No boundary tests reject `size=0`, negative values consistently, or very large public page sizes.
8. No test proves logout/password change/password reset clears all server refresh sessions.
9. No CORS test proves production rejects wildcard tunnel origins while allowing an exact environment origin.
10. Question deletion tests assert DB repository calls but not physical blob deletion: `QuestionServiceTest.java:448-500`.
11. OAuth tests do not cover a non-null token response with missing, null, blank, or wrong-type `access_token`.
12. No startup test prevents QA bootstrap activation in a production profile or rejects an absent externally supplied bootstrap password.

## Commands & Outputs

All commands were read-only and run from the workspace root.

- `git status --short --branch` showed branch `dev/kyoung`, three commits ahead, and pre-existing unrelated changes/untracked files.
- `git rev-parse HEAD` returned `27d22446e5d21324dadcfcb322dbe51704dfe914`.
- Targeted `rg -n -C` searches traced `permitAll`, role gates, DTO path fields, `MultipartFile` storage, email logging, rate-limit keys, page construction, session refresh, and payment-data handling.
- Negative searches found no trusted forwarded-IP resolver (`ForwardedHeaderFilter`, `CF-Connecting-IP`, `X-Forwarded-For`, remote-IP valve configuration), no CSP configuration, no server logout endpoint, and no current `TestController`.
- Local `javap` inspection of Spring MVC `ResourceHttpRequestHandler` confirmed filename-derived media type and inline resource response behavior. This was framework bytecode inspection only, not an HTTP exploit.
- A targeted secret-signature review did not reproduce any value in this pack. It identified configuration placeholders, test fixtures, and the historical JWT fallback residue described in PG-004-09. This was not a full SCA or repository-history scan.

## Tests

- **Executed:** none.
- **Reason:** handoff requires static inspection and permits writes only to the two WI outputs. Build/test execution could write build artifacts and was unnecessary to establish the static attack paths.
- **Inspected:** relevant JUnit tests for security chain, auth refresh, OAuth, email, track, playlist, company certification, and question service/controller behavior.
- **Not covered by this WI:** dynamic penetration testing, browser exploit execution, production/reverse-proxy configuration verification, malware scanning validation, dependency CVE/SCA, and git-history secret removal verification.

## Risks / Rollback

- Findings that depend on deployment topology are explicitly marked conditional. Production ingress and upload-host origin configuration were not available in the repository.
- Static analysis establishes reachable code paths but does not quantify production data volume, log-reader population, SMTP failure frequency, or storage headroom.
- Rollback is limited to removing these two newly created files, only if explicitly requested:
  - `deliverables/user/WI-20260711-ATS-004-summary.md`
  - `deliverables/agent/WI-20260711-ATS-004-evidence-pack.md`

## Follow-ups

1. **WI-20260711-ATS-006:** Treat PG-004-01, PG-004-02, and PG-004-03 as release-blocking P0 defects with regression tests.
2. **WI-20260711-ATS-007:** Add upload-policy architecture: private originals, public image derivative store, MIME/magic/decoder validation, quarantine/AV, CSP, and explicit content-disposition rules.
3. **WI-20260711-ATS-008:** Add abuse/session controls: proxy-aware distributed rate limiting, signup and enumeration protection, page caps, storage quotas/cleanup, server logout, and refresh-session revocation.
4. Confirm production ingress behavior for `/uploads/**`, trusted proxy headers, upload origin, and static-resource bypasses before downgrading any conditional finding.
5. Rotate the former JWT fallback anywhere it may have been used, redact it without copying it into work items, and verify removal with a repository-history secret scanner.
