---
version: 1.0
last_updated: 2026-08-17
project: ATS
owner: qa-integ
category: evidence-pack
status: blocked
related_wi: WI-20260816-ATS-002
dependencies:
  - path: WI-20260816-ATS-002-handoff.md
    reason: Approved execution contract and output boundary
  - path: ../user/REQ-20260816-ATS-001.md
    reason: Original approved isolated database proof
  - path: ../user/WI-20260817-ATS-007-summary.md
    reason: Documented predecessor operator exception
  - path: WI-20260817-ATS-007-evidence-pack.md
    reason: Sanitized operator-exception evidence
---

> Historical public derivative, published in this working tree on 2026-09-09 by WI-20260909-ATS-018. Original dates, language, status, decisions and reported outcomes below are historical, not current approval or reverified recovery/production evidence. The original remains unchanged in private archive `v1-artifacts-20260909-084405`. See [A011: original identity, public SHA-256 and review](../../docs/registry/development-history-recovery-20260909.md#a011). Raw/local-only evidence pointers are lookup keys, not files supplied by a source checkout.

# Evidence Pack: WI-20260816-ATS-002

## Summary

- The WI-007 operator-exception addendum was accepted as the documented
  predecessor readiness result while preserving WI-001's historical blocked
  record.
- The current non-database guard suite passed all 20 checks, including the
  43-table source check, current `UNRECORDED` manifest expectation, guarded
  observation path, exact-target cleanup preservation, and no unrelated
  database enumeration.
- The actual generated target reached a fail-closed preflight-capture boundary
  before any database action. No manifest observation, schema/seed application,
  creation, validation, focused test, or drop was attempted.

## Scope / DoD Check

- [x] Read the approved WI handoff, original REQ, WI-007 addendum, database
  README, bootstrap implementation, and guard suite.
- [x] Confirmed the documented predecessor result without altering historical
  WI-001 records.
- [x] Ran the current non-database guard suite and recorded its pass result.
- [x] Confirmed the current source-level 43-table check and fixed
  `schema.sql->seed.sql` order through the guard suite.
- [x] Preserved the current `UNRECORDED` manifest state; no guard was changed
  or bypassed.
- [x] Kept the generated target name, connection information, opaque input,
  and raw command output out of deliverables.
- [x] Stopped before all database and prohibited actions when complete
  target-preflight evidence was unavailable.
- [ ] Observed a bounded current manifest: not reached.
- [ ] Created, independently validated, and dropped an isolated proof target:
  forbidden by the unchanged `UNRECORDED` guard state and not attempted.

## Reference Documents (Tier 0-2)

| Tier | Document | Use |
| --- | --- | --- |
| 0 | `docs/standards/core-principles.md` | Approved execution and fail-closed boundary |
| 0 | `docs/standards/development-standards.md` | QA traceability and safety requirements |
| 0 | `docs/standards/documentation-standards.md` | Deliverable conventions |
| 0 | `docs/standards/glossary.md` | Canonical WI terminology |
| 0 | `docs/standards/evidence-pack-standard.md` | Evidence-pack structure |
| 1 | `docs/policies/quality-gates.md` | Guard and quality-gate context |
| 1 | `docs/policies/security-policy.md` | Opaque-input and secret boundary |
| 1 | `docs/policies/access-control-policy.md` | Isolated-access boundary |
| 1 | `docs/architecture/system-design.md` | WI traceability model |
| 2 | `scripts/database/README.md` | Guarded disposable-database procedure |
| 2 | `scripts/acceptance/README.md` | Runtime boundary; not executed |

## Evidence Pointers

- `deliverables/agent/WI-20260816-ATS-002-handoff.md`: exact one-target,
  `UNRECORDED`-manifest, prohibited-action, and output constraints.
- `deliverables/user/REQ-20260816-ATS-001.md`: original approved sequential
  proof and no-runtime/no-browser boundary.
- `deliverables/user/WI-20260817-ATS-007-summary.md:32-53`: documented
  operator-exception result and WI-002 readiness statement.
- `deliverables/agent/WI-20260817-ATS-007-evidence-pack.md:63-107`: sanitized
  operator-exception checks and superseding predecessor effect.
- `scripts/database/test-bootstrap-guards.ps1`: 20 current non-database
  checks passed.
- `scripts/database/bootstrap-disposable-mysql.ps1:192-204`: preflight runs
  before opaque credential loading or connector discovery.
- `scripts/database/DisposableMysqlBootstrap.java:47-52,146-154,343-401`:
  current 43-table source guard, unrecorded-manifest restrictions, guarded
  observation path, and exact-target cleanup behavior.
- `src/main/resources/schema.sql` and `src/main/resources/seed.sql`: the only
  eligible current inputs; neither was applied.
- `scripts/database/patches/WI-20260809-ATS-068-user-consents.sql`: explicitly
  excluded and not run against a disposable target.

## Commands And Sanitized Results

| Step | Result |
| --- | --- |
| First non-database guard-suite stream | `RESULT_NOT_RETAINED`; no active guard process remained |
| Recorded non-database guard suite | `PASS` (20 checks) |
| Generated-target `Preflight` | `FAIL_CLOSED_CAPTURE_INCOMPLETE` |
| Current source `CREATE TABLE` count | `43` / `PASS` |
| Current manifest expectation | `UNRECORDED` |
| Guarded `Observe` | `NOT_RUN` |
| Guarded `Create` | `NOT_RUN_UNRECORDED_MANIFEST` |
| Guarded `Validate` | `NOT_RUN_UNRECORDED_MANIFEST` |
| Focused test | `NOT_RUN_NOT_SPECIFIED` |
| Explicit guarded `Drop` | `NOT_RUN_NO_CREATED_TARGET` |

The attempted target is represented only by non-reversible run reference
`f05239cef2babde5`. The exact target name, opaque input, connection details,
raw output, and manifest rows were not retained.

## Reproduction / Verification

```powershell
# Non-database guard suite only
.\scripts\database\test-bootstrap-guards.ps1
```

The guarded target sequence is intentionally not reproducible from this WI:
the one generated target's complete preflight record is unavailable and the
current manifest remains `UNRECORDED`. A new sequence requires a separately
approved WI with a new isolated target; no existing database may be used.

## Risks / Rollback

- Risk: the current isolated 43-table proof and bounded manifest observation
  are incomplete.
- Rollback: not applicable. No database object or product/source file was
  changed; no manual cleanup is authorized or required.

## Follow-up Status

- No follow-up action is authorized by this blocked WI. Any new isolated proof
  must be separately approved and must preserve the current guard contract.
