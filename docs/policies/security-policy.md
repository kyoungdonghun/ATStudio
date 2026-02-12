---
version: 1.0
last_updated: 2026-01-06
project: system
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

## 6) Exception Handling (If Necessary)

If sharing is exceptionally needed:
- What to share (scope)
- Why needed (justification)
- Duration/expiration
- Masking/sanitization method
Record in WI/ADR, then approve.


