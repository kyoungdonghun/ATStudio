---
version: 1.9
last_updated: 2026-08-09
project: ATS
owner: PG
category: policy
status: stable
dependencies:
  - path: ../standards/glossary.md
    reason: Standard terminology usage baseline
  - path: ../design/api-spec.md
    reason: Current public listening and official download contract
tier: 1
target_agents:
  - pg
  - cr
task_types:
  - security
  - review
---
# Secrets/Sensitive Information (Secrets/PII) Handling Policy

> Purpose: The most critical incident in operations is "sensitive information leak."
> This document fixes minimum rules for what to classify as secret and how to block/mask/share it.

## 1) Classification

### 1.1 Secrets

- API Key, Access Token, Refresh Token
- DB passwords, SSH keys, certificates/private keys
- Internal network addresses/endpoints, admin accounts

### 1.2 PII (Personal Identifiable Information)

- Direct identifiers: name/phone/email/address/social security number, etc.
- Indirect identifiers: cookies/sessions/device identifiers, etc.

### 1.3 Internal Information (Organizationally Sensitive)

- Undisclosed designs, internal operational policies, customer/partner information

## 2) Storage/Sharing Basic Principles

- **No plain text in repo**: Do not commit Secrets/PII.
- **Use reference approach**: Configuration files like `.claude/config/context.json` should only contain "references" like `auth_ref`, actual keys managed separately via ENV/files.
- **Minimize logging**: Do not include sensitive info in logs (mask if necessary).

## 3) Masking Rules (Recommended)

- Tokens/keys: Show only first 3~4 chars + `...` + last 2~4 chars
- Email: `a***@domain.com`
- Phone: `010-****-1234`

## 4) Commit/PR Gate (Operating Rules)

- **PG (R/A)**: Scan and block/sanitize sensitive info before commit/sharing
- **EO (A)**: Final approval for "can this be shared (commit/push)?"
- **MA (R)**: Execute actual commit/push, block execution if policy violated

## 5) Knowledge Sync Policy Connection

- Default is **Default Deny**
- If sharing needed, allow only `.claude/knowledge/public/`
- Use `private` or `encrypted` mode for sensitive knowledge (key management separate)

## 6) ATStudio-specific Secrets Management

### 6.1 JWT Configuration

| Secret | Environment Variable | Description |
|--------|---------------------|-------------|
| JWT Secret Key | `JWT_SECRET` | Token signing key (HS256+) |
| JWT Expiration | `JWT_EXPIRATION` | Access token TTL (ms) |
| JWT Refresh Expiration | `JWT_REFRESH_EXPIRATION` | Refresh token TTL (ms) |

**Rules:**
- Never hardcode in `application.yml`. Use `${JWT_SECRET}` placeholder.
- Minimum key length: 256 bits for HS256.
- Rotate keys via environment variable update + rolling restart.

### 6.2 Database Credentials

| Secret | Environment Variable | Description |
|--------|---------------------|-------------|
| DB URL | `SPRING_DATASOURCE_URL` | `jdbc:mysql://host:3306/atstudio` |
| DB Username | `SPRING_DATASOURCE_USERNAME` | MySQL user |
| DB Password | `SPRING_DATASOURCE_PASSWORD` | MySQL password |

**Rules:**
- Production DB credentials must never appear in committed files.
- Use `application-local.yml` (gitignored, repository root) for local development only when the operator loads it explicitly.
- The committed base configuration does not import the ignored local file. Use an explicit additional-location argument or equivalent external configuration; never infer or copy local values.
- Production uses environment variables or external secret store.

### 6.3 Spring Security Configuration

- CSRF: Disabled for the JWT-based REST API. The active UI is the React SPA; no server-rendered form path exists.
- CORS: Explicitly configured per environment (dev/staging/prod).
- Password encoding: BCryptPasswordEncoder (strength 10+).

**`/api/users/me` rule ordering (SecurityConfig.java):**
- `GET /api/users/me`, `PUT /api/users/me`, `DELETE /api/users/me`, `PUT /api/users/me/*` are declared with `authenticated()` **before** the `hasRole("ADMIN")` wildcard `/api/users/*` entries. This order was fixed as part of CR-P-001 remediation.

**Public endpoints (permitAll) — current state:**
- `POST /api/users` (registration)
- `POST /api/auth/*` (login, social, refresh, forgot/reset password)
- `GET /api/auth/verify-email`
- `GET /api/utils/public-capabilities`
- `GET /api/utils/check-email`, `check-phone`, `check-nickname`
- `GET /api/tracks`, `GET /api/tracks/*`, `GET /api/tracks/*/stream`
- `GET /api/tags`
- `GET /api/subscriptions`, `GET /api/subscriptions/*`
- `GET /api/notices`, `GET /api/notices/*`
- `GET /api/notices/*/attachments/*` (notice attachment download — INT-002)
- `GET /api/albums`, `GET /api/albums/*`
- Swagger UI (`/swagger-ui/**`, `/v3/api-docs/**`) — controlled by `SWAGGER_ENABLED` env var

**Public account and authentication endpoint rate limits (server-side):**
- `POST /api/users`: 5 requests / 15 minutes per trusted client fingerprint
- `POST /api/auth/login`: 10 requests / 60 seconds
- `POST /api/auth/forgot-password`: 5 requests / 15 minutes
- `POST /api/auth/reset-password`: 5 requests / 15 minutes
- `POST /api/auth/refresh`: 30 requests / 60 seconds
- `GET /api/utils/check-email`: 30 requests / 60 seconds per trusted client and 30 requests / 60 seconds per trusted client + normalized email fingerprint
- `GET /api/utils/check-phone`: 30 requests / 60 seconds per trusted client and 30 requests / 60 seconds per trusted client + digits-only phone fingerprint
- `GET /api/utils/check-nickname`: 30 requests / 60 seconds per trusted client and 30 requests / 60 seconds per trusted client + normalized nickname fingerprint
- Exceeded requests return `429 Too Many Requests` with `Retry-After` header and `RATE_LIMIT_EXCEEDED`.
- Registration rate limiting never reads or parses the request body. Availability identifiers are NFKC-normalized; email and nickname are case-folded, and phone values are reduced to digits before fingerprinting.
- Every availability request consumes both its endpoint-wide trusted-client budget and its client + identifier budget, so rotating identifiers cannot bypass the endpoint-wide budget.
- Rate-limit keys contain only endpoint labels, budget scope labels, and process-local salted SHA-256 fingerprints. Raw client IP, email, phone, and nickname values are not stored in the in-memory key or logged.
- A `429` warning records only a fixed endpoint scope and computed retry seconds. It does not record the rate-limit key, direct IP, forwarded client identity, or availability identifier.
- The internal client header is accepted only from one configured loopback proxy peer and only when it contains one validated IP literal. Direct, duplicate, malformed, or spoofed header values fall back to the direct peer identity.

**User payment role boundary:**
- `/api/payments/**` is available only to a `USER` principal that does not also carry `ADMIN`; ADMIN requests return `403 Forbidden` before controller invocation.
- `/api/admin/payments/**` remains the separate ADMIN operations surface. USER recurring billing behavior and already-paid cancellation access are unchanged.

**Token storage:** JWT access token and refresh token are stored in **browser localStorage** (not httpOnly cookie). Frontend reads token from `localStorage` and sends as `Authorization: Bearer <token>` header via Axios interceptor.

**Current state:** `application.yml` no longer carries a fallback JWT secret, and local-only conveniences (DDL auto-update, SQL logging, localhost mail/OAuth redirects) belong in the gitignored root `application-local.yml`.
Local password auth availability is controlled by `APP_AUTH_PASSWORD_LOGIN_ENABLED` / `app.auth.password-login.enabled`. When disabled, `/api/utils/public-capabilities` reports `passwordLoginEnabled=false`, and local email/password login, signup, verification mail, and password reset must be treated as unavailable.

### 6.4 Environment Baseline

Use the committed `application.yml` as the safe shared baseline. Override through environment variables or an explicitly loaded gitignored root `application-local.yml`.

| Setting | Local | Stage | Production | Notes |
|--------|-------|-------|------------|-------|
| `SPRING_JPA_HIBERNATE_DDL_AUTO` | `validate` | `validate` | `validate` | V1 uses an externally applied fresh schema; runtime must not auto-mutate it |
| `SPRING_JPA_SHOW_SQL` | `true` optional | `false` | `false` | SQL logs increase noise and leakage risk |
| `SWAGGER_ENABLED` | `true` allowed | `false` by default | `false` | If temporarily enabled outside local, protect separately |
| `MAIL_HOST` | `localhost` / MailHog | stage SMTP | production SMTP | Do not rely on localhost outside local dev |
| `APP_BASE_URL` | `http://localhost:5173` | stage frontend URL | production frontend URL | Email links and redirects must match deployed frontend |
| `APP_AUTH_PASSWORD_LOGIN_ENABLED` | `true` | `true` or `false` per environment | `true` unless explicitly social-only | Disables local email/password login, signup, and reset flows when false |
| `GOOGLE_REDIRECT_URI`, `KAKAO_REDIRECT_URI`, `NAVER_REDIRECT_URI` | local SPA callback | stage SPA callback | production SPA callback | Must match provider console exactly |
| `APP_SECURITY_RATE_LIMIT_ENABLED` | `true` | `true` | `true` | Local may relax limits in `application-local.yml` only |
| `APP_SECURITY_RATE_LIMIT_REGISTRATION_LIMIT` | `10` recommended | `5` | `5` | Registration body is never parsed by the filter |
| `APP_SECURITY_RATE_LIMIT_LOGIN_LIMIT` | `30` recommended | `10` | `10` | Stage/prod should stay aligned unless capacity review says otherwise |
| `APP_SECURITY_RATE_LIMIT_FORGOT_PASSWORD_LIMIT` | `10` recommended | `5` | `5` | Password-reset endpoints are abuse targets |
| `APP_SECURITY_RATE_LIMIT_RESET_PASSWORD_LIMIT` | `10` recommended | `5` | `5` | Match forgot-password policy |
| `APP_SECURITY_RATE_LIMIT_REFRESH_LIMIT` | `60` recommended | `30` | `30` | Keep refresh higher than login to avoid false positives |
| `APP_SECURITY_RATE_LIMIT_EMAIL_AVAILABILITY_CLIENT_LIMIT` | `60` recommended | `30` | `30` | Endpoint-wide trusted-client fingerprint budget |
| `APP_SECURITY_RATE_LIMIT_EMAIL_AVAILABILITY_IDENTIFIER_LIMIT` | `60` recommended | `30` | `30` | Trusted-client + normalized email fingerprint budget |
| `APP_SECURITY_RATE_LIMIT_PHONE_AVAILABILITY_CLIENT_LIMIT` | `60` recommended | `30` | `30` | Endpoint-wide trusted-client fingerprint budget |
| `APP_SECURITY_RATE_LIMIT_PHONE_AVAILABILITY_IDENTIFIER_LIMIT` | `60` recommended | `30` | `30` | Trusted-client + digits-only phone fingerprint budget |
| `APP_SECURITY_RATE_LIMIT_NICKNAME_AVAILABILITY_CLIENT_LIMIT` | `60` recommended | `30` | `30` | Endpoint-wide trusted-client fingerprint budget |
| `APP_SECURITY_RATE_LIMIT_NICKNAME_AVAILABILITY_IDENTIFIER_LIMIT` | `60` recommended | `30` | `30` | Trusted-client + normalized nickname fingerprint budget |
| `APP_WHITELIST_EXPORT_MAX_ITEMS` | `500` | `500` | `500` | Bound both new export selection and immutable batch re-download |
| `APP_WHITELIST_MAX_SAVED_CHANNELS` | `100` | `100` | `100` | High technical row-safety cap; keep separate from subscription registration slots |

**Operational rule:** The committed [application-local.example.yml](../../application-local.example.yml) is a developer bootstrap example only. Stage and production must use environment-variable driven values managed outside the repository.

**Environment-conditional evidence:**
- `ATS020-X-02` remains `ENVIRONMENT-CONDITIONAL`. Source and tests establish the trusted-peer fallback contract, but deployment-specific trusted proxy CIDRs and independent multi-egress client identity still require named environment evidence.
- `ATS020-X-03` remains `ENVIRONMENT-CONDITIONAL`. The committed configuration has no JWT fallback, but prior fallback use, key rotation, and deployed-session expiry cannot be closed without secret-safe environment inspection and rotation evidence.

### 6.5 Protected Track Media

- Public Track detail responses must not expose the original `audio_file` storage key. The compatibility field is returned as `audioFile: null`.
- Admin create, update, and detail responses may retain the original key for the existing admin edit workflow.
- `/uploads/tracks/audio/**` is denied before static-resource resolution for anonymous, USER, and ADMIN requests. Encoded and traversal variants must also fail before resource delivery.
- Public Listening uses `GET /api/tracks/{trackId}/stream` and serves the complete active Track resource only through the controller. A no-Range request returns the complete representation; one valid Range is resolved against the full resource length; malformed, multiple, or unsatisfiable Ranges return `416` with `Content-Range: bytes */{fullLength}`.
- Public Listening does not disclose the original storage key or static URL and does not create a download record or License. It is not official file-download entitlement.
- Official Download uses `GET /api/tracks/{trackId}/download`. A first download requires an active Subscription and available plan quota, then records download history and issues a License. An existing License permits entitled re-download without duplicate issuance or another daily-count entry.

### 6.6 Mail Delivery Logging

Email delivery logs are correlation metadata, not a payload fallback.

- Generate one random `deliveryId` per delivery attempt.
- Success: log only `deliveryId` and `outcome=SUCCESS`.
- Failure: log only `deliveryId`, `outcome=FAILURE`, and the exception class name.
- Never log recipient address, subject, HTML/plain-text body, verification/reset URL or token, raw exception message, or stack trace.
- Delivery exceptions remain absorbed where the external contract is intentionally generic. In particular, password-reset requests must not reveal account existence or mail delivery success.

### 6.7 Whitelist Export Privacy

- Whitelist CSV export is ADMIN-only and requires an explicit status and/or keyword scope. An unscoped export is rejected.
- `APP_WHITELIST_EXPORT_MAX_ITEMS` is a mandatory server-side bound for each export. An over-limit request creates no batch, item snapshot, or channel status mutation.
- Persisted export items are immutable operational snapshots. New snapshots retain the channel identifier, email, channel metadata, subscription metadata, export order, and timestamps required for external registration; they do not retain a user ID or nickname copy.
- CSV output contains only the minimum operational fields. It must not add user IDs, nicknames, phone numbers, addresses, tokens, or payment identifiers.
- Every text cell remains CSV-formula neutralized before quoting. Re-download must reconstruct the file only from the stored batch/item snapshot in its original order.
- Export filters, CSV contents, email values, and item snapshots must not be written to application logs. Operational logs may use only the batch identifier, result category, and aggregate count.

### 6.8 Company Certification Documents and Accountability

- Company certification files are private StorageService objects. `/uploads/company-docs/**` remains denied before static-resource delivery; only the authenticated ADMIN attachment endpoint may deliver a document.
- The React apply/status routes require an authenticated USER with `userType=BUSINESS` and redirect other roles/types with the existing access-denied UX. This is defense-in-depth guidance only; controller/service authorization remains authoritative.
- Download responses preserve `attachment`, `no-store, private`, `nosniff`, sandboxed CSP, and no-Range headers. API responses omit `documentPath` and individual stored paths.
- Review and guarded-access audit records retain only the authenticated ADMIN actor reference, timestamp, action, certification ID, opaque document ID when applicable, and review from/to statuses. They must not contain file bytes, storage paths, filenames, notes, emails, phones, profile snapshots, tokens, raw request data, or document contents. A `DOCUMENT_ACCESS_GRANTED` row means authorization and private-resource resolution succeeded; it is not a byte-delivery completion record.
- Applicant-visible review reasons are normalized before mutation. `REVISION_REQUESTED` and `REJECTED` require a nonblank reason of at most 500 characters; an `APPROVED` note is optional but has the same bound. Illegal transitions are rejected before status or review-audit mutation.
- Upload validation accepts only PDF/JPG/JPEG/PNG, rejects null or empty multipart parts, enforces count/size/filename bounds, and checks extension, signature, and compatible MIME. PNG input is verified as `image/png`, then decoded and canonicalized to private JPEG output with `image/jpeg`; submitted PNG bytes are not retained. Canonical image handling remains part of validation; it is not malware scanning.
- `POLICY-PENDING`: no automatic certification-document purge, withdrawal deletion, retention duration, or scheduler is authorized. A retention policy must name owner, duration, legal/operational basis, and failed-delete handling before implementation.
- The V1 database baseline is fresh-only: apply `schema.sql` and `seed.sql` once to a verified-empty MySQL 8 database, then start with `ddl-auto=validate`. Retained-data migration is outside the V1 operator path and requires a separate approved requirement, rehearsal, and rollback plan.

### 6.9 Whitelist Channel URL Safety

- The backend accepts only an absolute HTTPS `channelUrl` with no user info and
  no explicit port other than standard HTTPS port 443.
- After URI parsing, the normalized lowercase host must be exactly
  `youtube.com` or a subdomain ending in `.youtube.com`; suffix lookalikes are
  rejected.
- Subscriber and ADMIN screens apply a defense-in-depth safe-link predicate
  before rendering an `href`. A persisted value that fails the current rule is
  rendered as non-clickable text so historical unsafe data cannot become a
  navigation target.
- Read paths do not silently rewrite retained rows. Operators must review and
  correct historical values through the normal channel workflow.

### 6.10 Admin Payment Support References

- Exact provider payment, refund, settlement, and receipt identifiers remain in
  protected server/entity fields only where provider operations require them.
- ADMIN API responses and screens expose deterministic masked `REF-*` support
  references instead of raw provider identifiers. Audit and Incident free text
  must not be used as a fallback channel for the raw value.
- Reconciliation Incidents persist only the deterministic `REF-*` value in the
  structured provider-transaction field. New Incident and audit free text must
  omit full and partial raw identifiers, including prefixes and suffixes.
- ADMIN response mapping must sanitize labelled provider identifiers retained in
  legacy Incident/audit notes before serialization. Sanitization must replace the
  complete value with a deterministic `REF-*` reference rather than preserve a
  raw fragment.
- Order IDs, masked payment methods, provider status, sanitized failure details,
  and support references remain allowed operational evidence.

### 6.11 Receipt URL and Provider Log Safety

- Provider receipt URLs are untrusted external evidence. An actionable receipt
  URL must be absolute HTTPS, contain no user info, and use no explicit port
  other than 443. The contract is provider-neutral and does not use a Toss-only
  host allowlist.
- New unsafe receipt URLs are suppressed before persistence. ADMIN response
  mapping reapplies the same rule so a retained legacy value cannot become an
  actionable link. The frontend validates again and shows only a non-clickable
  reference/review state when the value is unsafe.
- Reconciliation application logs contain aggregate counts only. They must not
  serialize result issue lists, exact provider transaction identifiers, or
  issue free text. Detailed investigation belongs in masked structured
  Incidents and support-safe ADMIN responses.
- Unknown provider cancel transport failures log only a bounded exception class
  name. Exception messages, stack traces, request URIs, and provider payment
  keys are forbidden because transport errors can embed the request URI.

### 6.12 Administrator Rejection Audit Minimization

- Rejected administrator role changes, last-ADMIN withdrawals, and local
  subscription-correction request/approval/execution attempts persist a null
  `reason_note`. Their audit rows retain only stable action, target, actor when
  available, outcome, error code, and equal bounded before/after state.
- Required role-change and correction operator text remains in the successful
  role-change audit or authoritative correction workflow/success audit. This
  boundary prevents automatic duplication into rejection rows; it does not add
  free-text DLP or change retention in those approved source contexts.

---

## 7) Exception Handling (If Necessary)

If sharing is exceptionally needed:
- What to share (scope)
- Why needed (justification)
- Duration/expiration
- Masking/sanitization method
Record in WI/ADR, then approve.
