# WI-20260724-ATS-013 Summary

## Decision

**PASS.** The fresh-clone V1 schema, seed, MySQL validation test, and seven
payment concurrency races passed on guarded disposable MySQL 8 databases.

The verified source snapshot was:

- Clone: `C:\Users\jm991\AppData\Local\ATStudio\release-rehearsal-3147873-20260724`
- Commit: `3147873c42bfd7883fdaa92922c0485e5fc72621`

No active repository source was modified. No commit or push was performed.

## Results

| Gate | Result |
|---|---|
| Database naming | Both targets matched `^ats_wi007_20260724_[a-z0-9]{8}$` before connection |
| SQL inputs | Fresh-clone `schema.sql` only, followed by `seed.sql` only |
| Schema apply | 39 statements; SHA-256 `3cd43f4c46b95b04badea2a086e433c362d7b5b13824e1f3abe9e106f98db31a` |
| Seed apply | 1 statement; 6 plans; SHA-256 `14bbaca1e697e94b98b2f38ff16951cefafafc2c1efa337336ef6d18b2b7900f` |
| V1 manifest | 39 tables, 449 columns, 153 indexes, 80 foreign keys |
| Manifest SHA-256 | `c48d3c75378aaf2364d89ed06833ba68e27a5a334dbc4670d1443bd938c6c506` |
| Hibernate MySQL validation | 1/1 passed |
| MySQL concurrency races | 7/7 passed |
| Combined MySQL tests | 8 passed, 0 failures, 0 errors, 0 skipped |
| Proof DB cleanup | Dropped; absence count `0` |
| Runtime DB | Separately seeded, `ddl-auto=validate` boot passed, retained |
| Backend bundle | 19 current allowlisted values; required 6 present; validation passed |
| Obsolete variables | `APP_PAYMENT_PROVIDER` and `TOSS_CONFIRM_URL` absent |

## Protected Database

The helper never selected or connected directly to protected `atstudio` and
never read its application data. An unselected loopback admin connection read
only `information_schema` metadata before and after the disposable workflow.

The metadata fingerprint was unchanged:

`c684fc803e3c5b3d53e8c1d6fad8bab90c37bfe8d8f766e05e55f58df3b546a7`

## Runtime Handoff

- Runtime pointer:
  `C:\Users\jm991\AppData\Local\ATStudio\release-rehearsal-runtime-3147873-20260724\runtime-pointer.json`
- Restricted backend bundle:
  `C:\Users\jm991\AppData\Local\ATStudio\release-rehearsal-runtime-3147873-20260724\backend-environment-wi013.json`
- Cleanup contract:
  `C:\Users\jm991\AppData\Local\ATStudio\release-rehearsal-runtime-3147873-20260724\cleanup-contract.md`

The actual runtime database name exists only inside the restricted bundle. The
runtime DB is retained for WI-014 through WI-016. WI-017 owns final process,
database, bundle, helper, log, pointer, and runtime-root cleanup.

## Safety Notes

- ACL inheritance is disabled without changing file ownership; each restricted
  artifact has one explicit current-account Full Control rule.
- No raw process logs or Gradle daemon logs remain.
- A value-suppressing scan found zero high-entropy credential values outside
  the restricted bundle and zero runtime database-name occurrences outside it.
- The pre-existing WI-012 tracked PDF manifest replay artifact remained
  byte-identical before and after WI-013.
