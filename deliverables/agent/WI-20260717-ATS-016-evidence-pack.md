# Evidence Pack: WI-20260717-ATS-016

## Summary (one-liner)
- Aligned JPA unique-index metadata with the canonical V1 schema and recreated the approved local database to the exact fresh V1 manifest.

## Scope / DoD Check
- DoD items:
  - [x] `PaymentSettlement` declares one canonical named unique constraint for `deduplication_key`.
  - [x] Duplicate `@Index(unique = true)` and `@Column(unique = true)` declarations were removed.
  - [x] `SocialAccount` uses the canonical schema constraint name.
  - [x] A focused baseline contract test protects both mappings.
  - [x] The approved local `atstudio` database was recreated from `schema.sql` and `seed.sql`.
  - [x] Hibernate validation and the exact manifest comparison passed.

## Reference Documents (Tier 0-2)

**Injected Context**:

| Tier | Document | Reason |
|------|----------|--------|
| 0 | `docs/standards/core-principles.md` | Constitution |
| 0 | `docs/standards/development-standards.md` | JPA and test standards |
| 1 | `docs/policies/quality-gates.md` | Verification requirements |
| 1 | `docs/policies/security-policy.md` | Secret-safe local DB execution |

**Injection Rules Applied**:
- Rule source: `.claude/config/context-injection-rules.json`
- Assignee: `se`
- Task type: JPA/schema metadata alignment

## Evidence Pointers
- Handoff: `deliverables/agent/WI-20260717-ATS-016-handoff.md`
- Changed source:
  - `src/main/java/com/atstudio/atstudio/entity/PaymentSettlement.java`
  - `src/main/java/com/atstudio/atstudio/entity/SocialAccount.java`
  - `src/test/java/com/atstudio/atstudio/config/V1BackendBaselineContractTest.java`
- Recreate proof:
  - `deliverables/agent/WI-20260717-ATS-004/run-20260717-150232-452cd063/`
- Canonical inputs:
  - `src/main/resources/schema.sql`
  - `src/main/resources/seed.sql`

## Commands & Outputs
- Focused entity/schema contract verification:
  - V1 baseline contract: 6/6 PASS.
  - V1 baseline plus payment DB integrity: 13/13 PASS.
  - `compileJava` and `compileTestJava`: PASS.
  - `git diff --check`: PASS.
- Approved destructive local recreation:
  - `run-v1-mysql-proof.ps1 -Mode RecreateLocal`: PASS.
  - Runtime ports 5173/8080 and Cloudflare process: absent.
  - Manual migration SQL count: 0.
  - Hibernate `ddl-auto=validate`: PASS.
- Independent exact manifest check:
  - Tables: 39.
  - Columns: 449.
  - Indexes: 153.
  - Foreign keys: 80.
  - Forbidden tables/columns: 0/0.
  - Plans: 6.
  - Provider columns/Toss-only columns: 9/9.
  - Manifest SHA-256: `c48d3c75378aaf2364d89ed06833ba68e27a5a334dbc4670d1443bd938c6c506`.

## Tests
- Focused backend contract tests: PASS.
- Guarded local MySQL Hibernate validation: PASS.
- Full backend/frontend quality gates are deferred to the final closeout verification WI.

## Risks / Rollback
- Risks:
  - Local users, tracks, playlists, tags, subscriptions, and payment records were intentionally removed by the approved fresh recreation.
  - Only the six deterministic subscription plans remain seeded.
- Rollback:
  - Revert the three assigned source/test files to restore the previous JPA metadata.
  - Database row data is intentionally not recoverable under the approved fresh-only V1 policy.

## Follow-ups
- Update current-state documentation to remove deleted SQL/API/branch instructions and publish the corrected V1 baseline.
- Run full automated quality gates and unified-branch acceptance testing.

