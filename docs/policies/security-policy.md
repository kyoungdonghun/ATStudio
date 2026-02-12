---
version: 1.0
last_updated: 2026-02-12
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
- Use `application-local.yml` (gitignored) for local development.
- Production uses environment variables or external secret store.

### 6.3 Spring Security Configuration

- CSRF: Disabled for REST API endpoints (JWT-based), enabled for Thymeleaf form pages.
- CORS: Explicitly configured per environment (dev/staging/prod).
- Password encoding: BCryptPasswordEncoder (strength 10+).

---

## 7) Exception Handling (If Necessary)

If sharing is exceptionally needed:
- What to share (scope)
- Why needed (justification)
- Duration/expiration
- Masking/sanitization method
Record in WI/ADR, then approve.
