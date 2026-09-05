---
version: 1.1
last_updated: 2026-08-17
project: ATS
owner: qa-integ
category: evidence-pack
status: complete
related_wi: WI-20260817-ATS-016
dependencies:
  - path: WI-20260817-ATS-016-handoff.md
    reason: Approved execution contract
  - path: ../user/REQ-20260817-ATS-009.md
    reason: Approved release-candidate scope
---

# Evidence Pack: WI-20260817-ATS-016

## Summary

- Earlier supported-wrapper Observe and recovery Inventory safe-capture failures
  are preserved as prior attempts and provided no accepted evidence. The later
  accepted recovery established zero possible orphans, the 43-table manifest,
  the distinct Create/Validate/Hibernate/Drop lifecycle, and final zero-orphan
  Inventory. Local closeout checks remain in progress.

## Scope / DoD Check

- [x] Read the approved handoff and REQ before action.
- [x] Loaded the required Tier 0 documents and the handoff's database,
  security, quality, and predecessor-evidence pointers.
- [x] Kept all retained, remote, production, provider, refund, and email
  systems outside the executed scope.
- [x] Preserved the prior Observe and Inventory capture failures as rejected,
  non-evidence attempts without retaining raw streams.
- [x] Accepted initial recovery Inventory: `PASS`, `count=0`,
  `state=NO_POSSIBLE_ORPHAN`.
- [x] Accepted fresh Observe: safe capture `PASS`, 43/511/175/91/6/6/0/0
  manifest, SHA-256 `b177b34780fabc75ea8b4608a0d210167a81d414d2778cc1d1dc5c0e39c8fea4`,
  expected `MYSQL_MANIFEST_EXPECTATION_UNRECORDED`, and cleanup `PASS`.
- [x] Recorded the current manifest expectation in source, guard tests, and
  current-state documentation.
- [x] Accepted distinct proof: Create `PASS`, independent Validate `PASS`,
  actual Hibernate `ddl-auto=validate` `PASS`, exact Drop `PASS`, and final
  Inventory `PASS` with `count=0` / `state=NO_POSSIBLE_ORPHAN`.
- [x] Passed the required local guard, targeted default-mode, full backend,
  documentation, and diff checks for WI completion.

## Reference Documents (Tier 0-2)

**Injected Context** (from the WI handoff packet):

| Tier | Document | Reason |
| --- | --- | --- |
| 0 | `docs/standards/core-principles.md` | Approved-scope and fail-closed execution control |
| 0 | `docs/standards/development-standards.md` | Verification discipline |
| 0 | `docs/standards/documentation-standards.md` | Deliverable convention |
| 0 | `docs/standards/glossary.md` | Canonical terminology |
| 1 | `docs/policies/security-policy.md` | Opaque secret-input boundary |
| 1 | `docs/policies/quality-gates.md` | Required quality gates |
| 2 | `scripts/database/README.md` | Supported disposable lifecycle |
| 2 | `docs/design/db-schema.md` | Current source-schema context |
| 2 | `docs/SR/SR-93.md` | Active production-readiness boundary |
| 2 | `docs/payment/system-overview.md` | Hibernate baseline context |
| 2 | `docs/payment/feature-inventory.md` | Current manifest status |
| Context | `deliverables/agent/WI-20260817-ATS-015-evidence-pack.md` | Current-state documentation evidence |
| Context | `deliverables/agent/WI-20260817-ATS-012-evidence-pack.md` | Prior bounded inventory evidence |

Injection rules applied:
- Rule source: `.claude/config/context-injection-rules.json`
- Assignee: `qa-integ`
- Required tiers: `0`, plus handoff-selected security, quality, database, and
  release context.

## Evidence Pointers

- `scripts/database/DisposableMysqlBootstrap.java`: active source count guard
  is `43`; active manifest expectation is `RECORDED` as 43/511/175/91/6/6/0/0
  with SHA-256 `b177b34780fabc75ea8b4608a0d210167a81d414d2778cc1d1dc5c0e39c8fea4`.
- `scripts/database/test-bootstrap-guards.ps1`: current preconnection and
  manifest-state guard contract.
- `src/test/java/com/atstudio/atstudio/config/V1BackendBaselineContractTest.java`:
  source baseline assertion for the current 43-table schema.
- `src/test/java/com/atstudio/atstudio/service/PaymentMysqlSchemaValidationTest.java`:
  requires explicit opt-in plus a guarded disposable loopback contract; default
  mode must skip before Spring creates a datasource.
- `deliverables/user/WI-20260817-ATS-016-summary.md`: sanitized user-facing
  execution result.

## Commands & Outputs

| Command | Normalized result |
| --- | --- |
| Earlier Observe and Inventory capture attempts | Preserved; `NOT_ACCEPTED` evidence |
| Accepted initial recovery Inventory | `PASS: count=0, state=NO_POSSIBLE_ORPHAN` |
| Accepted fresh Observe | `PASS`; expected pre-record refusal `MYSQL_MANIFEST_EXPECTATION_UNRECORDED`; cleanup `PASS` |
| Accepted distinct proof lifecycle | `Create PASS`, independent `Validate PASS`, actual Hibernate `ddl-auto=validate PASS`, exact `Drop PASS` |
| Accepted final Inventory | `PASS: count=0, state=NO_POSSIBLE_ORPHAN` |
| `scripts/database/test-bootstrap-guards.ps1` | `PASS` (26 checks) |
| Targeted default non-opt-in `PaymentMysqlSchemaValidationTest` | `PASS`; 1 guarded skip, 0 failures/errors |
| `gradlew.bat --no-daemon test --rerun-tasks` | `PASS` |
| Documentation validation | `PASS` |
| `git diff --check` | `PASS` |

Database lifecycle results:

| Action | Count | Result |
| --- | ---: | --- |
| Prior Observe and Inventory safe-capture attempts | Preserved | `NOT_ACCEPTED` evidence |
| Initial recovery Inventory | Accepted | `PASS: count=0, state=NO_POSSIBLE_ORPHAN` |
| Fresh Observe | Accepted | Safe capture `PASS`; expected refusal and cleanup `PASS` |
| Distinct proof Create / Validate / Hibernate / Drop | Accepted | `PASS / PASS / PASS / PASS` |
| Final Inventory | Accepted | `PASS: count=0, state=NO_POSSIBLE_ORPHAN` |
| Manual or unsupported database query/mutation | Not used | No retained, remote, or production DB action |

Accepted structural values: source baseline count `43`, manifest expectation
`RECORDED`, manifest `43/511/175/91/6/6/0/0`, and SHA-256
`b177b34780fabc75ea8b4608a0d210167a81d414d2778cc1d1dc5c0e39c8fea4`.

## Tests

- `scripts/database/test-bootstrap-guards.ps1` -> `PASS` (26 checks).
- `PaymentMysqlSchemaValidationTest` in default non-opt-in mode -> `PASS`;
  1 guarded skip and 0 failures/errors.
- `gradlew.bat --no-daemon test --rerun-tasks` -> `PASS`.
- Documentation validation -> `PASS`.
- `git diff --check` -> `PASS`.

## Risks / Rollback

- Risk: No retained-data migration, remote/production database proof, provider
  payment/refund behavior, email delivery, or release-readiness claim is
  established by this disposable evidence.
- Rollback: Revert only the WI-016 source-contract and current-state
  documentation changes through approved follow-up work. The accepted lifecycle
  ends with zero possible guarded disposable orphans; no database rollback is
  authorized or required.

## Follow-ups

- WI-016 is complete. The remaining production gates stay in SR-93 and are not
  authorized here.
