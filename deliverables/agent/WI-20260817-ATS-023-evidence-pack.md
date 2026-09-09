
> Historical public derivative, published in this working tree on 2026-09-09 by WI-20260909-ATS-018. Original dates, language, status, decisions and reported outcomes below are historical, not current approval or reverified recovery/production evidence. The original remains unchanged in private archive `v1-artifacts-20260909-084405`. See [A050: original identity, public SHA-256 and review](../../docs/registry/development-history-recovery-20260909.md#a050). Raw/local-only evidence pointers are lookup keys, not files supplied by a source checkout.

# Evidence Pack: WI-20260817-ATS-023

## Summary

- Provisioned and validated one isolated client acceptance database without altering existing databases, then proved the actual acceptance runtime against it.

## Scope / DoD Check

- [x] Read-only recovery examined only aggregate eligible-disposable metadata.
- [x] One fresh guarded client database passed Preflight, Create, and Validate.
- [x] `schema.sql` then `seed.sql` applied to the new target.
- [x] Separate client environment bundle created with protected current-user-only ACL.
- [x] Actual acceptance runtime reached `ready` with `ddl-auto=validate`.
- [x] Local and public frontend/API endpoints returned HTTP 200.
- [x] Existing databases, provider actions, email delivery, source secrets, and original bundle were not modified.

## Reference Documents

| Tier | Document | Reason |
|---|---|---|
| 0 | `docs/standards/core-principles.md` | Project constitution |
| 0 | `docs/standards/development-standards.md` | Runtime and source script verification |
| 1 | `docs/policies/security-policy.md` | External bundle and secret handling |
| 1 | `docs/policies/quality-gates.md` | Guarded validation evidence |
| 2 | `scripts/database/README.md` | Disposable MySQL contract |
| 2 | `scripts/acceptance/README.md` | Acceptance runtime contract |

## Evidence Pointers

- `scripts/database/bootstrap-disposable-mysql.ps1`: guarded loopback-only source/seed/bootstrap contract.
- `scripts/acceptance/new-isolated-backend-environment.ps1`: user-only protected bundle copier; source and target paths remain outside the repository.
- `$USERPROFILE\AppData\Local\ATStudio\acceptance-client-20260817-r2\runtime-manifest.json`: current acceptance runtime manifest. Do not commit or disclose environment bundle values.

## Commands And Safe Results

- `scripts/database/test-bootstrap-guards.ps1` -> passed.
- Guarded `Preflight` -> current source contains 43 `CREATE TABLE` statements; current manifest expectation recorded.
- Guarded `Create` -> schema and seed applied; manifest passed with 43 tables, 511 columns, 175 indexes, 91 foreign keys, 6 plans, and 6 plan keys.
- Guarded `Validate` -> same manifest passed independently.
- `scripts/acceptance/test-backend-environment.ps1` -> passed all ten environment-isolation checks.
- `scripts/acceptance/test-dry-run.ps1` -> passed parser, URL, readiness, cleanup, and secret-free output checks.
- Acceptance `start.ps1` -> runtime `ready`; local/public frontend and `/api/tracks` each returned HTTP 200.
- `new-isolated-backend-environment.ps1` parser and `git diff --check` -> passed.

## Risks And Rollback

- The acceptance database and copied bundle are intentionally retained for client testing.
- A prior unqualified disposable candidate remains untouched; cleanup requires a separately approved exact-target decision.
- Rollback requires explicit approval to use the guarded exact-target `Drop` action and to remove the copied bundle. Never use this utility against development, retained, stage, or production data.
