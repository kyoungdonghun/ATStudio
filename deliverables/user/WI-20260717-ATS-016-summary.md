# WI-20260717-ATS-016 Summary

## Result

JPA unique-index metadata now matches the canonical `schema.sql` names and no longer declares duplicate unique indexes. Focused regression tests protect the corrected mappings.

The approved local `atstudio` database was fully recreated. All prior local users, media, tags, playlists, subscriptions, and payment records were removed. The deterministic seed restored only six subscription plans.

## Verified Baseline

- 39 tables
- 449 columns
- 153 indexes
- 80 foreign keys
- 0 forbidden legacy tables or columns
- 6 subscription plans
- 9 Toss-only payment provider columns
- Manifest SHA-256: `c48d3c75378aaf2364d89ed06833ba68e27a5a334dbc4670d1443bd938c6c506`
- Hibernate `ddl-auto=validate`: PASS

The local database now exactly matches the V1 fresh baseline.

