
> Historical public derivative, published in this working tree on 2026-09-09 by WI-20260909-ATS-018. Original dates, language, status, decisions and reported outcomes below are historical, not current approval or reverified recovery/production evidence. The original remains unchanged in private archive `v1-artifacts-20260909-084405`. See [A092: original identity, public SHA-256 and review](../../docs/registry/development-history-recovery-20260909.md#a092). Raw/local-only evidence pointers are lookup keys, not files supplied by a source checkout.

# WI-20260817-ATS-023 Summary

## Result

Completed. The client acceptance runtime now uses a separate, current disposable MySQL database and a separate protected backend environment bundle.

## What Changed

- Read-only recovery found one older disposable candidate, but it did not meet the current 43-table and seed expectations. It was not inspected by name, changed, or deleted.
- Created one new client-only disposable database using the guarded bootstrap path.
- Applied the current `schema.sql` and `seed.sql` to that database.
- Created a separate protected bundle at `$USERPROFILE\AppData\Local\ATStudio\acceptance-client-20260817-backend-environment.json`.
- Added `scripts/acceptance/new-isolated-backend-environment.ps1` to create future isolated bundles without printing bundle values. The client application snapshot itself was not modified.

## Verification

- Guarded source schema: 43 tables.
- Database manifest: 43 tables, 511 columns, 175 indexes, 91 foreign keys, 6 plans, and 6 plan keys.
- Bundle structure and required datasource fields: passed without printing values.
- Bundle access: one current-user access rule with protected ACL.
- Spring acceptance runtime: ready with Hibernate validation enabled by the acceptance profile.
- Local frontend/API and public frontend/API: HTTP 200.

## Not Performed

- No development, `atstudio`, staging, production, or existing disposable database was changed.
- No Toss payment/refund, SMTP delivery, or production deployment was executed.
- No secret, credential, JDBC URL, provider payload, or disposable database name was recorded.

## Retention And Rollback

Keep the new disposable database and client bundle while client acceptance is active. Dropping that database or deleting the protected bundle requires a new explicit approval.
