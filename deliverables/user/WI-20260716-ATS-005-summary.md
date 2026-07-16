# WI-20260716-ATS-005 Summary

## Security Behavior

- Public registration remains available. `POST /api/users` now has an independent 5-request/900-second trusted-client budget, and the filter does not read or parse the registration body.
- Email, phone, and nickname availability checks now consume two independent budgets on every request: 30 requests/60 seconds per endpoint + trusted client and the preserved 30 requests/60 seconds per endpoint + trusted client + normalized identifier fingerprint. Rotating identifiers therefore still exhausts the endpoint-wide client budget.
- Rate-limit map keys contain only endpoint/budget labels and process-local salted SHA-256 fingerprints. They contain no raw client IP, email, phone, or nickname.
- A rate-limit warning records only a fixed endpoint scope and computed retry seconds. It does not record a key, direct IP, forwarded client identity, or raw identifier.
- Requests over budget return `429 Too Many Requests`, `Retry-After`, and `RATE_LIMIT_EXCEEDED`. Idle key cleanup uses the configured maximum window instead of retaining keys for twice that duration.
- Direct, duplicate, malformed, or spoofed internal client headers continue to converge on the direct peer identity.

## Role Boundary and Compatibility

- `/api/payments/**` now requires `USER` and explicitly denies any principal carrying `ADMIN`, including mixed `USER`+`ADMIN` authority, before controller invocation.
- All user checkout and callback routes under `/subscriptions/checkout*`, legacy `/subscriptions/payment*`, and `/subscriptions/billing/*` are USER-only. ADMIN is redirected to `/admin/payments`.
- Existing USER billing-agreement prepare, confirm, read, and cancel controller tests remain green. Payment provider calls and payment policy were not changed.

## Verification

| Check | Result |
|---|---|
| Focused backend security/payment tests | PASS, 43 tests, 0 failures/errors/skips |
| Frontend route tests | PASS, 2 files / 13 tests |
| Frontend typecheck | PASS |
| Changed-file ESLint | PASS, 4 router files, 0 errors/warnings |
| Changed-file Prettier | PASS, 4 router files |
| Documentation validator | PASS, Tier 0, links, 398 traceability IDs, and index coverage |
| `git diff --check` | PASS; only repository line-ending conversion warnings were emitted |

One initial focused-backend command invocation was terminated by its 5-second execution timeout before a result. The identical command was rerun with a 120-second timeout and completed successfully.

## Remaining Environment Evidence

- `ATS020-X-02` remains `ENVIRONMENT-CONDITIONAL`: deployment-specific trusted proxy CIDRs and independent multi-egress client identity require named environment evidence.
- `ATS020-X-03` remains `ENVIRONMENT-CONDITIONAL`: prior JWT fallback use, deployed key rotation, and session expiry require secret-safe environment inspection and rotation evidence.

The client worktree, payment provider configuration/calls, runtime logs, secrets, and other workers' changes were not modified.
