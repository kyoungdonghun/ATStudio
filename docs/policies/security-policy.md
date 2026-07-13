---
version: 1.3
last_updated: 2026-07-13
project: ATS
owner: PG
category: policy
status: stable
dependencies:
  - path: ../standards/glossary.md
    reason: Standard terminology usage baseline
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
- Use `application-local.yml` (gitignored, repository root) for local development.
- `src/main/resources/application.yml` imports `optional:file:./application-local.yml`, so the local override is applied automatically when present.
- Production uses environment variables or external secret store.

### 6.3 Spring Security Configuration

- CSRF: Disabled for all endpoints (REST API + JWT-based; Thymeleaf pages are legacy dev-only scaffolding).
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

**Public auth endpoint rate limits (server-side, IP + path):**
- `POST /api/auth/login`: 10 requests / 60 seconds
- `POST /api/auth/forgot-password`: 5 requests / 15 minutes
- `POST /api/auth/reset-password`: 5 requests / 15 minutes
- `POST /api/auth/refresh`: 30 requests / 60 seconds
- Exceeded requests return `429 Too Many Requests` with `Retry-After` header and `RATE_LIMIT_EXCEEDED`.

**Token storage:** JWT access token and refresh token are stored in **browser localStorage** (not httpOnly cookie). Frontend reads token from `localStorage` and sends as `Authorization: Bearer <token>` header via Axios interceptor.

**Current state:** `application.yml` no longer carries a fallback JWT secret, and local-only conveniences (DDL auto-update, SQL logging, localhost mail/OAuth redirects) belong in the gitignored root `application-local.yml`.
Local password auth availability is controlled by `APP_AUTH_PASSWORD_LOGIN_ENABLED` / `app.auth.password-login.enabled`. When disabled, `/api/utils/public-capabilities` reports `passwordLoginEnabled=false`, and local email/password login, signup, verification mail, and password reset must be treated as unavailable.

### 6.4 Environment Baseline

Use the committed `application.yml` as the safe shared baseline, and override per environment through environment variables or the gitignored root `application-local.yml`.

| Setting | Local | Stage | Production | Notes |
|--------|-------|-------|------------|-------|
| `SPRING_JPA_HIBERNATE_DDL_AUTO` | `update` allowed | `validate` | `validate` | Non-local environments must not auto-mutate schema |
| `SPRING_JPA_SHOW_SQL` | `true` optional | `false` | `false` | SQL logs increase noise and leakage risk |
| `SWAGGER_ENABLED` | `true` allowed | `false` by default | `false` | If temporarily enabled outside local, protect separately |
| `MAIL_HOST` | `localhost` / MailHog | stage SMTP | production SMTP | Do not rely on localhost outside local dev |
| `APP_BASE_URL` | `http://localhost:5173` | stage frontend URL | production frontend URL | Email links and redirects must match deployed frontend |
| `APP_AUTH_PASSWORD_LOGIN_ENABLED` | `true` | `true` or `false` per environment | `true` unless explicitly social-only | Disables local email/password login, signup, and reset flows when false |
| `GOOGLE_REDIRECT_URI`, `KAKAO_REDIRECT_URI`, `NAVER_REDIRECT_URI` | local SPA callback | stage SPA callback | production SPA callback | Must match provider console exactly |
| `APP_SECURITY_RATE_LIMIT_ENABLED` | `true` | `true` | `true` | Local may relax limits in `application-local.yml` only |
| `APP_SECURITY_RATE_LIMIT_LOGIN_LIMIT` | `30` recommended | `10` | `10` | Stage/prod should stay aligned unless capacity review says otherwise |
| `APP_SECURITY_RATE_LIMIT_FORGOT_PASSWORD_LIMIT` | `10` recommended | `5` | `5` | Password-reset endpoints are abuse targets |
| `APP_SECURITY_RATE_LIMIT_RESET_PASSWORD_LIMIT` | `10` recommended | `5` | `5` | Match forgot-password policy |
| `APP_SECURITY_RATE_LIMIT_REFRESH_LIMIT` | `60` recommended | `30` | `30` | Keep refresh higher than login to avoid false positives |

**Operational rule:** The committed [application-local.example.yml](../../application-local.example.yml) is a developer bootstrap example only. Stage and production must use environment-variable driven values managed outside the repository.

### 6.5 Protected Track Media

- Public Track detail responses must not expose the original `audio_file` storage key. The compatibility field is returned as `audioFile: null`.
- Admin create, update, and detail responses may retain the original key for the existing admin edit workflow.
- `/uploads/tracks/audio/**` is denied before static-resource resolution for anonymous, USER, and ADMIN requests. Encoded and traversal variants must also fail before resource delivery.
- Public playback uses `GET /api/tracks/{trackId}/stream`. A valid dedicated preview must normalize under `tracks/preview/` and differ from the original key.
- When no valid dedicated preview exists, the endpoint may read the original only through the controller and expose a bounded prefix. It must never expose the complete original through repeated or malformed Range requests.
- Entitled subscribers obtain the original only through `GET /api/tracks/{trackId}/download` after the existing subscription, quota, history, and license controls.

### 6.6 Mail Delivery Logging

Email delivery logs are correlation metadata, not a payload fallback.

- Generate one random `deliveryId` per delivery attempt.
- Success: log only `deliveryId` and `outcome=SUCCESS`.
- Failure: log only `deliveryId`, `outcome=FAILURE`, and the exception class name.
- Never log recipient address, subject, HTML/plain-text body, verification/reset URL or token, raw exception message, or stack trace.
- Delivery exceptions remain absorbed where the external contract is intentionally generic. In particular, password-reset requests must not reveal account existence or mail delivery success.

---

## 7) Exception Handling (If Necessary)

If sharing is exceptionally needed:
- What to share (scope)
- Why needed (justification)
- Duration/expiration
- Masking/sanitization method
Record in WI/ADR, then approve.
