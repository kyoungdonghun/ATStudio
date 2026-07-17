# Evidence Pack: WI-20260717-ATS-015

## Summary (one-liner)
- Completed the final read-only V1 cross-layer audit and found a local MySQL manifest drift plus current-state documentation drift.

## Scope / DoD Check
- DoD items:
  - [x] Compared the source schema, seed, runtime configuration, current MySQL manifest, active APIs, frontend routes, and current-state documentation.
  - [x] Distinguished structural schema drift from row-level test/demo data.
  - [x] Classified findings as FIX, DOCUMENT, ACCEPT, or PRODUCTION-GATE.
  - [x] Performed no source, database, Git ref, runtime, or remote mutation.
  - [ ] Final V1 closure is blocked until the schema drift and documentation findings are corrected.

## Reference Documents (Tier 0-2)

**Injected Context**:

| Tier | Document | Reason |
|------|----------|--------|
| 0 | `docs/standards/core-principles.md` | Constitution |
| 0 | `docs/standards/development-standards.md` | Integration audit standard |
| 0 | `docs/standards/documentation-standards.md` | Current-state documentation review |
| 0 | `docs/standards/glossary.md` | Terminology |
| 1 | `docs/policies/quality-gates.md` | Quality evidence |
| 1 | `docs/policies/security-policy.md` | Secret-safe database inspection |
| 1 | `docs/policies/versioning-policy.md` | V1 baseline status |
| 1 | `docs/policies/execution-policy.md` | Read-only execution boundary |

**Injection Rules Applied**:
- Rule source: `.claude/config/context-injection-rules.json`
- Assignee: `qa-integ`
- Task type: final V1 cross-layer audit

## Evidence Pointers
- Handoff: `deliverables/agent/WI-20260717-ATS-015-handoff.md`
- Recreated V1 manifest: `deliverables/agent/WI-20260717-ATS-004/run-20260717-021207-9953beed/local-manifest-final.log`
- Source baseline: `src/main/resources/schema.sql`, `src/main/resources/seed.sql`
- Drift source:
  - `src/main/java/com/atstudio/atstudio/entity/PaymentSettlement.java:29-38,63`
  - `src/main/java/com/atstudio/atstudio/entity/SocialAccount.java:8-15`
  - `src/main/resources/schema.sql:104,591`
- Stale current-state documents:
  - `docs/SR/SR-93.md:29-34,64-67,160-163,176-178,224`
  - `docs/SR/SR-42.md:7`
  - `docs/SR/SR-92.md:9`
  - `docs/design/payment-operations-runbook.md:347`
  - `docs/client/_internal-feature-map.md:47`
  - `docs/index.md:22,34,73`

## Commands & Outputs
- Read-only live manifest through `V1MysqlProofManager --mode manifest`:
  - Current: 39 tables, 449 columns, 155 indexes, 80 foreign keys.
  - Forbidden tables: 0; forbidden columns: 0.
  - Subscription plans: 6; provider columns: 9; Toss-only provider columns: 9.
  - Current manifest SHA-256: `910ed7a3e8444ee54f570968becc52cfba306e817d5e0986fe61f669783e76d8`.
- Recreated V1 evidence:
  - 39 tables, 449 columns, 153 indexes, 80 foreign keys.
  - Manifest SHA-256: `c48d3c75378aaf2364d89ed06833ba68e27a5a334dbc4670d1443bd938c6c506`.
- Static residual searches:
  - Active retired legacy API/service references: 0.
  - Manual migration SQL files: 0.
  - Active inventory: 137 APIs, 39 tables/entities, 53 screens.
- Exact additional indexes:
  - `payment_settlements.idx_payment_settlements_dedup`.
  - `social_accounts.uq_social_accounts_provider_provider_id`.
- Column manifest drift:
  - MySQL ENUM definitions were reordered from the source semantic order to the Hibernate-generated alphabetical order.

## Tests
- `gradlew.bat test --tests "com.atstudio.atstudio.service.PaymentMysqlSchemaValidationTest" --no-daemon` with the guarded local proof target: PASS.
- Interpretation: Hibernate can validate and start against the current schema, but the physical schema is not identical to the V1 fresh baseline.

## Risks / Rollback
- Risks:
  - Treating Hibernate validation as exact baseline proof would leave two redundant indexes and noncanonical ENUM definitions in the local database.
  - Row-level test/demo data was intentionally not inspected by the manifest audit.
  - Current-state documents still direct operators to deleted migration files and retired paths.
- Rollback:
  - No rollback is required because this WI was read-only.

## Follow-ups
- `WI-20260717-ATS-016`: Align JPA unique-index declarations with `schema.sql`, then recreate the approved local V1 database and verify the exact manifest.
- Documentation closeout WI: update SR/current-state documents after the corrected manifest is proven.

