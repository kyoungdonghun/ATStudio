---
version: 2.0
last_updated: 2026-07-17
project: ATS
owner: docops
category: design
status: stable
dependencies:
  - path: ../api-spec.md
    reason: Current utility route contract
---

# Utility Use Cases

## Current Utility APIs

| Code | Endpoint | Purpose |
|---|---|---|
| `UTIL-002` | `GET /api/utils/check-email` | Email availability |
| `UTIL-003` | `GET /api/utils/check-phone` | Phone availability |
| `UTIL-006` | `GET /api/utils/download-count` | Today's Official Download count and remaining quota |
| `UTIL-012` | `GET /api/utils/check-nickname` | Nickname availability |
| `UTIL-013` | `GET /api/utils/subscription-change-preview` | Prorated/scheduled subscription change preview |
| `UTIL-019` | `GET /api/utils/public-capabilities` | Public authentication/runtime capability flags |

## Rules

### Availability Checks

Email, phone, and nickname values are normalized consistently with registration
and profile mutation. Rate limits apply to trusted-client and normalized
identifier fingerprints. Raw identifiers are not retained in rate-limit keys or
logs.

### Download Count

The endpoint reports the current user's daily Official Download count, plan
limit, remaining count, and next reset time. Existing License re-download rules
remain owned by the download service.

### Subscription Change Preview

The endpoint compares the current subscription with a target plan/cycle and
returns `UPGRADE`, `SCHEDULED_CHANGE`, or `NO_CHANGE` with the amount and
effective-date preview used by the manage screen. It does not mutate
subscription or payment state.

### Public Capabilities

The endpoint reports whether password login, mail-dependent flows, social
providers, and non-production QA bootstrap exposure are available. It does not
return secret values.

Token refresh, email verification, password reset, profile reads, and current
subscription reads are owned by their dedicated auth/user/subscription
controllers, not Utility APIs.
