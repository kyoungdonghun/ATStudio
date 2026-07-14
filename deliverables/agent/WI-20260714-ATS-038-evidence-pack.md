---
version: 1.0
last_updated: 2026-07-15
project: ATS
owner: se
category: agent
status: stable
dependencies:
  - path: WI-20260714-ATS-038-handoff.md
    reason: Approved WI scope and output contract
---

# Evidence Pack: WI-20260714-ATS-038

## Summary

- Added an acceptance-only, fail-closed bootstrap for the six canonical subscription plans before QA user fixtures.

## Scope / DoD Check

- [x] A fresh repository result produces exactly six canonical `INDIVIDUAL`/`BUSINESS` plans.
- [x] Matching active canonical rows are preserved without another write.
- [x] Inactive, property-conflicting, or duplicate canonical rows refuse startup before inserts.
- [x] The plan runner is ordered before `TestUserBootstrapRunner`.
- [x] The runner requires the explicit `acceptance` profile plus both acceptance and QA bootstrap flags.
- [x] Focused unit/configuration and existing QA bootstrap regression tests pass.
- [x] No schema, retained DB, payment/provider, email, runtime log, or tunnel operation occurred.

## Reference Documents

| Tier | Document | Reason |
|---|---|---|
| 0 | `docs/standards/core-principles.md` | Approved execution, language, and traceability baseline |
| 0 | `docs/standards/development-standards.md` | Java/Spring implementation and focused-test standards |
| 0 | `docs/standards/documentation-standards.md` | Deliverable metadata and structure |
| 0 | `docs/standards/glossary.md` | Canonical WI terminology |
| 1 | `docs/policies/security-policy.md` | Secret and logging boundary |
| 1 | `docs/policies/quality-gates.md` | Regression, rollback, and evidence requirements |
| 2 | `deliverables/user/REQ-20260714-ATS-001.md` | Approved acceptance-hardening scope |
| 2 | `deliverables/agent/WI-20260714-ATS-015-evidence-pack.md` | Acceptance profile and bootstrap guard contract |
| 2 | `deliverables/agent/WI-20260714-ATS-017-evidence-pack.md` | No-live-lifecycle boundary |
| 2 | `deliverables/agent/WI-20260714-ATS-022-evidence-pack.md` | Fresh-schema subscriber blocker evidence |
| 2 | `docs/design/db-schema.md` | Canonical plan values |
| WI | `deliverables/agent/WI-20260714-ATS-037-evidence-pack.md` | Existing QA bootstrap/logging state |

**Injection rules applied**:

- Rule source: `AGENTS.md` and `deliverables/agent/WI-20260714-ATS-038-handoff.md`
- Assignee: `se`
- Ownership: new bootstrap fixture class, new focused tests, and WI-038 summary/evidence only

## Evidence Pointers

- `src/main/java/com/atstudio/atstudio/bootstrap/AcceptanceSubscriptionPlanBootstrapRunner.java`
  - `CANONICAL_PLANS`: six deterministic plan definitions from `docs/design/db-schema.md`.
  - `run`: validates every existing canonical key before saving only missing rows.
  - `validateExistingPlan`: fail-closed inactive/property checks.
  - Class annotations: acceptance profile, both property guards, transaction, and pre-QA ordering.
- `src/test/java/com/atstudio/atstudio/bootstrap/AcceptanceSubscriptionPlanBootstrapRunnerTest.java`
  - Fresh seed values and QA target keys, restart idempotency, inactive/conflict/duplicate refusal, and runner order.
- `src/test/java/com/atstudio/atstudio/bootstrap/AcceptanceSubscriptionPlanBootstrapConfigurationTest.java`
  - Actual Spring context activation matrix for the profile and both flags.
- `deliverables/user/WI-20260714-ATS-038-summary.md`
  - Korean behavior, verification, limitation, and next-WI report.
- `deliverables/agent/WI-20260714-ATS-038-evidence-pack.md`
  - This reproducibility record.

## Behavior Evidence

- Canonical order is `STANDARD`, `DELUXE`, `PREMIUM` for `INDIVIDUAL`, then the same order for `BUSINESS`.
- Existing candidates are normalized only for conflict detection; exact canonical spelling and all seeded numeric properties must match.
- Validation completes for all found canonical rows before `saveAll`, so a detected conflict cannot leave a partial seed from this runner.
- `@Transactional` commits the plan seed when the runner returns; `@Order(LOWEST_PRECEDENCE - 1)` places it before the existing QA runner at `LOWEST_PRECEDENCE`.
- QA target keys covered by the fresh seed are `DELUXE/INDIVIDUAL`, `STANDARD/INDIVIDUAL`, and `PREMIUM/BUSINESS`.

## Commands & Outputs

- Focused tests:
  - `.\gradlew.bat test --tests "com.atstudio.atstudio.bootstrap.AcceptanceSubscriptionPlanBootstrapRunnerTest" --tests "com.atstudio.atstudio.bootstrap.AcceptanceSubscriptionPlanBootstrapConfigurationTest" --tests "com.atstudio.atstudio.bootstrap.TestUserBootstrapRunnerTest" --console=plain`
  - Result: `BUILD SUCCESSFUL`; 10 tests, 0 failures, 0 errors, 0 skipped.
- Scoped diff/whitespace verification:
  - `git diff --check -- <five WI-038-owned paths>` plus an explicit trailing-whitespace scan for the new untracked files.
  - Result: PASS; no whitespace errors in the five WI-038-owned files.

## Limitations / Residual Risk

- No disposable MySQL or application context with a real database was started; persistence and fresh-schema navigation remain for WI-039 acceptance verification.
- The runner reads the small reference table with `findAll`; this avoids a shared repository edit but assumes subscription-plan cardinality remains bounded.
- Concurrent application instances bootstrapping the same empty database rely on the documented unique `(name, user_type)` constraint; this WI does not change schema or add distributed startup coordination.

## Rollback

- Remove `AcceptanceSubscriptionPlanBootstrapRunner.java` and its two focused test classes.
- Remove the WI-038 user summary and Evidence Pack.
- Do not perform data rollback: this WI does not run against a database, and operational rollback is application-code removal only.
- Do not revert shared `SubscriptionRepository`, `TestUserBootstrapRunner`, payment, schema, acceptance lifecycle, or unrelated concurrent changes.

## Follow-up

- `WI-20260714-ATS-039` should rerun the fresh disposable acceptance flow and verify subscriber/grace/business behavior without payment/provider mutation.
