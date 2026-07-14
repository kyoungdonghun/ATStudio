---
version: 1.0
last_updated: 2026-07-14
project: ATS
owner: PG
category: design
status: draft
dependencies:
  - path: ../../deliverables/user/REQ-20260714-ATS-001.md
    reason: Approved P1 remediation and acceptance-environment scope
  - path: ../audit/full-system-audit-20260713.md
    reason: Canonical P1 and conditional findings
  - path: ../policies/security-policy.md
    reason: Secrets, PII, token, and protected-resource rules
  - path: api-spec.md
    reason: Current API baseline
  - path: ../SR/SR-42.md
    reason: Existing single-frontend-tunnel acceptance topology
---

# P1 Security and Acceptance Hardening Design

> Purpose: Define implementation-ready security contracts for WI-20260714-ATS-003 without claiming that any control is implemented or live-verified.

## 1. Scope and Current Evidence

This design covers `ATS020-P1-01` through `P1-04`, `P1-11`, `P1-12`, and acceptance-environment `ATS020-X-02`/`X-04`. It does not migrate legacy files, inspect submitted document bodies, rotate historical JWT keys, call live services, or change runtime state.

| Current behavior | Code evidence | Security consequence |
|---|---|---|
| Playlist thumbnails are passed directly to generic storage. | `PlaylistService.java:52-54,186-188`; `LocalStorageService.java:51-70` | Client bytes can become same-origin active content. |
| Certification validation uses extension and size; stored MIME comes from the client. | `CompanyCertificationService.java:233-276` | Format identity is not established server-side. |
| Certification files are represented under `/uploads/`; download reuses stored client MIME. | `CompanyCertificationService.java:310-315`; `CompanyCertificationController.java:98-112` | The private-review boundary is incomplete despite ADMIN authorization. |
| One refresh-token hash is stored per user and rotated on refresh. | `AuthService.java:76-110`; `User.java:62-70` | A single-session revocation model is available, but termination paths are incomplete. |
| Password change and reset update the password without clearing the refresh hash; no logout endpoint exists. | `UserService.java:183-193`; `EmailService.java:141-159`; `AuthController.java:21-73` | Existing refresh capability survives those termination events. |
| File delete can occur before DB commit and delete failures are absorbed. | `TrackService.java:164-183`; `NoticeService.java:92-132`; `LocalStorageService.java:78-87` | Rollback can leave missing files; failed cleanup has no durable evidence. |
| Question deletion removes attachment rows without deleting their files. | `QuestionService.java:173-189` | Orphan files can remain indefinitely. |
| CSV values are quoted but formula-leading text is unchanged. | `AdminWhitelistChannelService.java:171-198` | Spreadsheet software may execute exported cells as formulas. |
| Social callback calls `fetchMe()` before committing issued tokens. | `SocialLoginPage.tsx:42-59`; `auth.ts:99-103` | The first authenticated request can be sent without the new token. |
| Rate limiting keys directly on `getRemoteAddr()`. | `AuthRateLimitFilter.java:43-48` | A local proxy can collapse public clients to one key. |
| Vite accepts every Host and proxies `/api` and `/uploads` to localhost. | `vite.config.ts:12-24` | The current acceptance ingress has no exact Host boundary. |
| Test-user bootstrap is property-gated but has a committed default password and no profile guard. | `TestUserBootstrapRunner.java:33-52`; `TestUserBootstrapProperties.java:14-22`; `application.yml:87-90` | Enabling the flag in the wrong profile is unsafe. |
| Public and Toss callback URLs default independently to localhost. | `application.yml:93-108`; `PaymentProperties.java:22-41` | An ephemeral public URL can produce inconsistent callbacks. |

## 2. Control Ownership

| Finding / contract | Security invariant | Enforcement layer | Implementation WI | Verification / review |
|---|---|---|---|---|
| P1-01 playlist images | Only canonical server-generated JPEG bytes are publicly served. | Backend image pipeline and static response policy | WI-009 | WI-019, WI-024 |
| P1-02 certification documents | Files are signature-verified, privately quarantined, and downloaded only as attachments. | Backend validation, private storage, ADMIN controller | WI-010 | WI-019, WI-024 |
| P1-03 refresh sessions | Logout, password change, and password reset invalidate the stored refresh capability atomically. | Auth/User/Email services and locked repository query | WI-011 | WI-019, WI-024 |
| P1-04 file/DB lifecycle | New files are rollback-cleaned; old files are deleted only after commit; failures are durable and retryable. | Storage coordinator and mutation journal | WI-012 | WI-019, WI-021, WI-025 |
| P1-11 CSV | Every user-controlled formula-leading cell is neutralized before RFC 4180 quoting. | Export service | WI-013 | WI-019, WI-024 |
| P1-12 social callback | Token state exists before `/users/me`; any failed completion removes partial state. | Frontend auth store/API/page | WI-014 | WI-020, WI-025 |
| X-04 profile, bootstrap, callbacks | Acceptance starts only with explicit non-production mode, external secrets, and one validated public base URL. | Startup guard and configuration | WI-015 | WI-020, WI-022 |
| X-02 proxy and Host | Only the loopback Vite proxy can assert one bounded client-IP value; Hosts are exact. | Vite proxy and backend filters | WI-016 | WI-020, WI-022, WI-024 |
| Cloudflare lifecycle | A launcher owns, verifies, and tears down only its processes and ephemeral URL. | Acceptance PowerShell lifecycle | WI-017 | WI-022, WI-025 |

## 3. Untrusted Image Contract

### 3.1 Input and canonicalization

- Accept only JPEG (`FF D8 FF`) and PNG (`89 50 4E 47 0D 0A 1A 0A`) signatures. Extension is ignored. A supplied client MIME must be absent, `application/octet-stream`, or match the verified format.
- Preserve the current per-image limit of 10 MiB (`ValidationConstants.IMAGE_MAX_SIZE_BYTES`). Reject dimensions outside `1..4096` or more than 16,777,216 pixels before full decode.
- Use JDK `ImageIO`; do not add a decoder dependency. Reject no-reader, truncated, multi-frame/animated, APNG, or unsupported color-model inputs.
- Decode once, downscale without upscaling to at most 2048 by 2048, render into a new RGB buffer, flatten alpha onto white, and encode a new JPEG at quality 0.90. Metadata, original names, profiles, scripts, and trailing polyglot bytes are not copied.
- Store only an opaque generated `<uuid>.jpg` key under `playlists/thumbnails/`. Never store the submitted bytes in a public root.

### 3.2 Serving and failure

- Serve canonical files as `image/jpeg` with `X-Content-Type-Options: nosniff`, `Content-Security-Policy: default-src 'none'; sandbox`, and `Cross-Origin-Resource-Policy: same-origin`.
- Replacement creates a new immutable key. The previous thumbnail is deleted through the after-commit lifecycle in Section 6.
- Unsupported or malformed input returns `400 INVALID_VALID`; size violations return `413 IO_LARGE`; storage failures return the existing generic server error. No failed input path becomes active.
- Evidence must include renamed SVG/HTML, GIF/WebP, truncated signatures, oversized dimensions, APNG, decompression-boundary, and JPEG/PNG-with-trailing-payload cases. A valid polyglot may be accepted only if the canonical output contains no appended payload.

## 4. Certification Quarantine Contract

### 4.1 Baseline format matrix

| Submitted format | Required verification | Stored form | Stored MIME |
|---|---|---|---|
| JPEG | Section 3 signature and decode rules | Canonical JPEG | `image/jpeg` |
| PNG | Section 3 signature and decode rules | Canonical JPEG | `image/jpeg` |
| PDF | `%PDF-` at byte zero and final non-whitespace bytes ending in `%%EOF`; no leading/trailing payload | Original verified bytes in quarantine | `application/pdf` |
| HWP/HWPX/DOC/DOCX | Not accepted in this baseline | None | None |

The existing 20 MiB per-file and 10-file limits remain, with a 50 MiB aggregate application limit below the current 60 MiB servlet request limit. Signature validation does not establish that a PDF is malware-free; it establishes only format plausibility. Complex office/HWP formats remain blocked until a parser or isolated-review approach is separately approved.

### 4.2 Storage and download

- Add a private storage root configured externally. It must be absolute, must not equal or nest inside the public `uploads` root, and must not be exposed by `WebConfig`.
- Store generated keys only. Keep a sanitized original filename as display/download metadata; persist verified server MIME, never `MultipartFile.getContentType()`.
- Remove or null public `documentPath` from API responses. Existing legacy rows are not migrated by this WI.
- ADMIN download continues through `/api/company-certifications/{certificationId}/documents/{documentId}` after the existing parent/child lookup. It must return `Content-Type: application/octet-stream`, attachment-only UTF-8 disposition, `Cache-Control: no-store, private`, `Pragma: no-cache`, `X-Content-Type-Options: nosniff`, `Content-Security-Policy: default-src 'none'; sandbox`, and no Range support.
- The admin UI must not embed, preview, or auto-open a document. The operator workflow must state that quarantine validation is not a clean-malware verdict.
- Reject mismatch, malformed PDF, active-image format, path-like filename, or aggregate overflow before DB mutation. Logs may contain an operation ID and reason code, never filename, content, applicant PII, token, or storage path.

## 5. Refresh-Session Revocation Contract

- Preserve the current single-refresh-session model. Add an authenticated, idempotent `POST /api/auth/logout` returning `204`; it clears `users.refresh_token` and accepts no token in the request body.
- Add a pessimistic user-row lookup used by refresh, logout, password change, and password reset. All four operations serialize on the same row.
- Refresh validates JWT structure, locks the user, compares the SHA-256 hash, checks account state, then rotates. A hash mismatch returns `401 REFRESH_TOKEN_INVALID` without clearing a different current session.
- Successful logout, password change, password reset, and withdrawal set the refresh hash to `null` in the same transaction as their primary state change.
- Frontend normal logout calls the server before clearing local storage. A confirmed invalid/revoked session is treated as logout success; a transient network failure must not display a false server-logout success.
- Token values and token hashes must never be logged. Access JWTs remain valid until their existing expiry; immediate access-token revocation is outside this REQ.
- Tests must replay the pre-event refresh token after each of the three required termination paths, test repeated logout, test old-token replay after rotation, and race refresh against each termination operation.

## 6. File and DB Mutation Contract

### 6.1 Coordinator and journal

`StorageMutationCoordinator` becomes the only write/delete path for Track, Playlist, Album, Company Certification, Notice, and Question files. `StorageService` must expose typed public/private roots, staged write, atomic promote, strict load, and deletion that returns failure instead of swallowing it.

A new durable `storage_mutations` journal is the selected implementation. Each file row records an opaque operation ID, domain, `CREATE|REPLACE|DELETE`, generated new/old relative keys, `PREPARED|COMMITTED|ROLLBACK_CLEANUP|AFTER_COMMIT_DELETE|RETRY|DONE|FAILED`, attempt count, next attempt, reason code, and timestamps. It stores no original filename, content, PII, or raw exception. Applying this schema requires the separate DB approval listed in Section 11.

### 6.2 Ordering

1. Validate all inputs, stage all new files under a private `.staging/<operationId>/` area, and durably record `PREPARED` before changing domain rows.
2. Promote each staged file to a generated immutable final key using an atomic move on the same filesystem. Any promotion failure aborts the DB mutation.
3. Update domain rows to the new key and commit. Old files remain readable until commit.
4. On rollback, delete every new/staged file. A failed cleanup becomes `RETRY` in an independent transaction.
5. After commit, delete replaced/deleted old files only after confirming that no live DB row references the key. A failed delete does not roll back the successful business request; it remains durable `RETRY` evidence.
6. Startup and scheduled recovery claim bounded pending rows, use exponential retry, and stop at `FAILED` for operator action. A stale `PREPARED` row is resolved by checking current DB references before deleting either side.

Create is all-or-nothing for multi-file requests. Playlist/album soft delete schedules thumbnail cleanup; notice/question hard delete schedules attachment cleanup; certification resubmission schedules previous documents; track replacement schedules replaced audio/thumbnail cleanup. Track soft-delete retention and physical source-audio relocation remain unchanged and out of scope.

### 6.3 Read boundary

- A controller may load only a DB-owned generated relative key from its declared storage root after authorization/ownership checks.
- Reject absolute paths, `..`, backslashes, colon/NUL, symlinks, non-regular files, and any normalized path outside the configured root.
- Private files are never reachable through `/uploads/**`. Download responses use attachment disposition and `nosniff`; public canonical images use only the Section 3 policy.
- Lifecycle tests must cover create, partial multi-file failure, DB rollback, replacement commit, deletion commit, process interruption represented by stale journal states, retry success/failure, shared-reference protection, and traversal/symlink reads.

## 7. CSV Neutralization Contract

- Apply neutralization to every user-controlled textual cell before existing CSV quote escaping. Numeric IDs and server-generated timestamps remain typed server values.
- If the first non-BOM code point after leading ASCII spaces or tabs is `=`, `+`, `-`, or `@`, or if the cell begins with tab/CR/LF, prefix one apostrophe to the entire original value. Do not trim, remove, normalize, or otherwise rewrite user data.
- Then apply the existing RFC 4180 behavior: double embedded quotes and quote the complete cell. Preserve the UTF-8 BOM and current column order.
- Tests cover each formula prefix, leading whitespace/control characters, embedded quotes/newlines, already-apostrophe-prefixed text, legitimate negative-looking text, null/empty values, and UTF-8 Korean data. The exported snapshot and CSV must contain the same original business value; neutralization is an output transformation only.

## 8. Social Callback Ordering Contract

1. Validate provider, authorization code, state, and PKCE material as today.
2. Exchange the code. Atomically stage the returned access and refresh tokens in auth storage/state before calling any authenticated API.
3. Call `fetchMe()` through the authenticated client, then atomically commit user and role and navigate.
4. If profile fetch or final commit fails, best-effort revoke the staged server session through logout, then always clear staged tokens, user, role, and dependent stores. Never leave a half-authenticated UI.
5. The callback effect remains single-shot under React Strict Mode. Tests assert request ordering, Authorization header presence, rollback on profile failure, incomplete-profile navigation, and no duplicate exchange.

## 9. Acceptance Ingress and Startup Contract

### 9.1 Trusted client identity and Host

- Bind Spring and Vite to loopback in acceptance. Cloudflare connects to Vite; only Vite proxies `/api` and `/uploads` to Spring.
- Vite removes inbound `Forwarded`, `X-Forwarded-*`, and `X-ATStudio-Client-IP`. In acceptance mode it derives one client IP from the Cloudflare-provided connecting-IP header and writes a single internal header. Direct local mode uses the socket peer and ignores forwarding headers.
- Spring trusts the internal header only when the immediate peer is an explicitly configured loopback proxy. The value is limited to 64 bytes and must parse as exactly one IPv4/IPv6 literal with no list, port, zone, or whitespace. Otherwise reject the header and use the direct peer; never trust arbitrary `Forwarded`/`X-Forwarded-For`.
- Rate-limit key is method + normalized route + effective client IP. Authenticated limits may additionally include account ID, but account identity must not replace the IP abuse boundary.
- Replace `allowedHosts: true` with exact `localhost`, `127.0.0.1`, and the one launcher-injected public hostname. Spring accepts only exact configured Host names; the Vite proxy's `changeOrigin: true` means Spring normally receives its local host.
- Remove the global `https://*.trycloudflare.com` CORS pattern. Same-origin public traffic needs no CORS; any separate origin must be explicitly injected.

Cloudflare header overwrite behavior and multi-client separation are deployment facts not proven by repository code. WI-022 must verify them publicly before X-02 can close.

### 9.2 Profile, bootstrap, and callbacks

- Acceptance startup requires explicit `spring.profiles.active=acceptance`; default or production profiles fail when acceptance/bootstrap flags are enabled.
- Remove the bootstrap password default. Enabling test users requires an externally supplied nonblank password and external JWT/DB secrets. The guard validates presence without logging values and refuses startup on any production profile.
- One validated `APP_PUBLIC_BASE_URL` is the acceptance source of truth: absolute HTTPS, no userinfo/query/fragment, root path only, and hostname equal to the injected Vite Host.
- Derive mail links, social redirect URIs, Toss one-time success/fail URLs, and Toss billing-auth success/fail URLs from that base. Acceptance startup rejects independent callback overrides that do not share the same origin and expected path.
- Toss calls in acceptance require separately supplied test credentials. This design does not claim that credential class can be proven from repository data or that provider-console callbacks are registered; those are WI-022 operational preconditions.

## 10. Cloudflare Lifecycle Contract

- WI-017 adds a Windows PowerShell launcher that uses an already installed `cloudflared`; it does not install software, create a persistent service, or change tunnel provider.
- Runtime state, redirected output, PID, start time, and command fingerprints live under a per-run temporary directory outside the repository. No secret is written there.
- Start a quick tunnel to `http://127.0.0.1:5173`, wait with a fixed timeout for exactly one validated `https://<host>.trycloudflare.com` URL, inject its base/host into Spring and Vite, then start the two services.
- Publish the URL to the operator only after local frontend, local API, public frontend, and public API checks pass. Public media and Toss test-callback checks are WI-022 gates, not launcher assumptions.
- On timeout, child exit, Ctrl+C, or failed readiness, stop the tunnel first, then Vite, then Spring. Stop only processes whose PID, start time, executable, and command fingerprint match the run manifest; teardown is idempotent.
- Verify ports are closed and the public URL is unreachable before deleting temporary state. Sharing the URL with a client remains a separate final approval point.

## 11. Approval Points and Rollback

1. **Certification compatibility:** The fail-closed baseline rejects HWP/HWPX/DOC/DOCX. Product approval is required before WI-010 changes the current accepted-format behavior. Supporting them requires separately approved parsing or isolated review; extension/MIME checks are insufficient.
2. **Durable file journal:** The `storage_mutations` table and disposable-MySQL rehearsal require the REQ's separate schema/environment approval before WI-012/WI-021 apply DDL.
3. **External acceptance:** Test Toss credentials, provider callback registration, public Cloudflare smoke, and client URL sharing require separate operational approval. No live credential or service was inspected here.

Code/config rollback reverts each implementation WI. New image/document uploads must be disabled before rolling back their validators. Journal rollback must first drain or preserve non-`DONE` rows; it must never delete files blindly. Legacy certification paths and historical files remain unchanged until a separately approved migration.

## Related Documents

### Required References

- [Approved REQ](../../deliverables/user/REQ-20260714-ATS-001.md): Scope, gates, and WI chain.
- [Full-System Audit](../audit/full-system-audit-20260713.md): Canonical findings.
- [Security Policy](../policies/security-policy.md): Sensitive-data and environment rules.

### Reference Documents

- [API Specification](api-spec.md): Current endpoint and response baseline.
- [SR-42](../SR/SR-42.md): Existing single-frontend-tunnel topology.
