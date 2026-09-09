---
version: 1.2.0
last_updated: 2026-08-17
project: ATS
owner: qa-integ
category: evidence-pack
status: blocked
related_wi: WI-20260816-ATS-001
dependencies:
  - path: WI-20260816-ATS-001-handoff.md
    reason: Approved scope, safety gates, and output contract
  - path: ../user/REQ-20260816-ATS-001.md
    reason: Approved execution authority
---

> Historical public derivative, published in this working tree on 2026-09-09 by WI-20260909-ATS-018. Original dates, language, status, decisions and reported outcomes below are historical, not current approval or reverified recovery/production evidence. The original remains unchanged in private archive `v1-artifacts-20260909-084405`. See [A009: original identity, public SHA-256 and review](../../docs/registry/development-history-recovery-20260909.md#a009). Raw/local-only evidence pointers are lookup keys, not files supplied by a source checkout.

# Evidence Pack: WI-20260816-ATS-001

## Summary

- **BLOCKED.** The current authorized single-use rerun reached preflight and
  stopped with `PREFLIGHT_BUNDLE_INVALID`. The source audit and Connector/J
  discovery passed, but the required in-memory preflight did not establish
  permission to connect.
- `PATCH_APPLIED_ONCE=NO`; no connection, metadata access, DDL, DML, data-row
  query, rollback, repair, or drop occurred in the current rerun.
- `WI-20260816-ATS-002` remains blocked. This WI does not start it.

## Current Single-Use Rerun Result

- `CURRENT_PATCH_TEXT_AUDIT=PASS`: the approved source was classified as one
  `CREATE TABLE user_consents` statement with no DML, `DROP`, or `ALTER`.
- `CURRENT_CONNECTOR_J_DISCOVERY=PASS`: one Connector/J candidate was located
  without retaining its location.
- `CURRENT_PREFLIGHT=PREFLIGHT_BUNDLE_INVALID`: the private input was parsed
  only in memory but did not satisfy every required validation condition. No
  identity, value, raw diagnostic, or parser detail was retained.
- `DB_CONNECTION=NOT_RUN`, `PRE_STRUCTURE_CHECK=NOT_RUN`,
  `PATCH_APPLICATION=NOT_RUN`, `POST_STRUCTURE_CHECK=NOT_RUN`, and
  `DATA_ROWS_QUERIED=0`.
- The current one-time attempt is complete. It does not authorize a retry,
  fallback input, repair, rollback, compensating SQL, or scope expansion.

## Scope / DoD Check

- [x] Read the approved handoff, REQ, WI-068 decision/evidence/summary, patch,
  operator guides, and required Tier 0-2 references.
- [x] Audited the repository patch as one permitted `CREATE TABLE user_consents`
  DDL with no DML, `DROP`, or `ALTER`.
- [x] Confirmed an existing Connector/J candidate without modifying source.
- [x] Performed the fixed expected-bundle existence gate only in the required
  runtime family; no broad search or fallback credential source was used.
- [x] Stopped before connection when `EXPECTED_BUNDLE=FAIL`.
- [x] Preserved the original fixed-path result as history, then performed the
  amended path/name-only regular-file discovery only in the approved bounded
  root.
- [x] Opaque-validated discovered candidates one at a time through the existing
  allowlisted bundle parser and internal datasource classifier.
- [x] Stopped before target-scope validation, metadata access, or DDL when
  `OPAQUE_BUNDLE_VALIDATION=FAIL_ZERO_VALID`.
- [x] Performed no DDL, DML, rollback, repair, drop, application start,
  acceptance lifecycle, browser, provider, mail, OAuth, payment/refund, or Git
  operation.
- [x] Ran the current source audit and Connector/J discovery before attempting
  the private-input preflight.
- [x] Stopped fail-closed at `PREFLIGHT_BUNDLE_INVALID` before connection,
  metadata access, data-row query, or SQL execution.
- [ ] Successful current pre-structure, patch application, and post-structure
  evidence: unavailable because the one-time rerun is blocked.

## Reference Documents (Tier 0-2)

| Tier | Document | Use |
| --- | --- | --- |
| 0 | `docs/standards/core-principles.md` | Approved execution and traceability boundary |
| 0 | `docs/standards/development-standards.md` | QA evidence and safety requirements |
| 0 | `docs/standards/documentation-standards.md` | Deliverable conventions |
| 0 | `docs/standards/glossary.md` | Canonical terminology |
| 0 | `docs/standards/evidence-pack-standard.md` | Evidence-pack structure |
| 1 | `docs/policies/quality-gates.md` | Verification gate requirements |
| 1 | `docs/policies/security-policy.md` | Sensitive-information boundary |
| 1 | `docs/policies/access-control-policy.md` | Least-privilege execution boundary |
| 1 | `docs/architecture/system-design.md` | WI traceability model |
| 2 | `.agents/skills/react-best-practices/AGENTS.md` | Required injected context |
| 2 | `docs/standards/frontend-standards.md` | Required injected context |
| 2 | `docs/templates/eval-report-template.md` | Required injected context |
| 2 | `docs/templates/requirements-request-template.md` | Required injected context |
| 2 | `docs/templates/wi-subagent-handoff-template.md` | Required injected context |

## Evidence Pointers

- `deliverables/agent/WI-20260816-ATS-001-handoff.md`: approved database-only
  scope, fail-closed preconditions, and output contract.
- `deliverables/user/REQ-20260816-ATS-001.md`: approved single-target authority
  and quality gates.
- `scripts/database/patches/WI-20260809-ATS-068-user-consents.sql:1-17`:
  audited source for the only permitted DDL; it was not executed in the current
  rerun.
- Current in-memory operator action labels: `CURRENT_PATCH_TEXT_AUDIT`,
  `CURRENT_CONNECTOR_J_DISCOVERY`, and `CURRENT_PREFLIGHT`; only sanitized
  result codes are retained in this pack.
- `scripts/acceptance/AcceptanceLifecycle.psm1:108-177`: historical parser
  context for the prior opaque validation; no bundle content is reproduced.
- `deliverables/user/WI-20260809-ATS-068-summary.md`: WI-068 confirms this
  patch was previously created but unapplied.
- `deliverables/agent/WI-20260809-ATS-068-decision-register.md`: accepted
  existing-database patch and current-manifest boundary.
- `deliverables/agent/WI-20260809-ATS-068-evidence-pack.md`: predecessor
  source-only evidence and non-execution boundary.
- `scripts/acceptance/README.md` and `scripts/database/README.md`: opaque
  credential handling and local-MySQL safety context.

## Sanitized Attempt History

| Action label | Result | Sensitive output retained |
| --- | --- | --- |
| `FIXED_EXPECTED_BUNDLE_EXISTENCE` | `FAIL` | No |
| `FIXED_PATCH_TEXT_AUDIT` | `PASS` | No |
| `FIXED_CONNECTOR_J_DISCOVERY` | `PASS` | No |
| `BOUNDED_NAME_METADATA_DISCOVERY` | `PASS` | No |
| `OPAQUE_BUNDLE_VALIDATION` | `FAIL_ZERO_VALID` | No |
| `TARGET_SCOPE_VALIDATION` | `NOT_RUN` | No |
| `CURRENT_PATCH_TEXT_AUDIT` | `PASS` | No |
| `CURRENT_CONNECTOR_J_DISCOVERY` | `PASS` | No |
| `CURRENT_PREFLIGHT` | `PREFLIGHT_BUNDLE_INVALID` | No |
| `DB_CONNECTION` | `NOT_RUN` | No |
| `PRE_STRUCTURE_CHECK` | `NOT_RUN` | No |
| `PATCH_APPLICATION` | `NOT_RUN` | No |
| `POST_STRUCTURE_CHECK` | `NOT_RUN` | No |

The original fixed expected bundle was absent. The amended recovery then
inspected only bounded path/name metadata and performed opaque in-memory
validation. No candidate met every parser and internal classification condition,
so execution stopped without selecting an alternative.

The current single-use rerun does not revise that history. It stopped at its
in-memory preflight and did not establish permission to connect. No input was
substituted and no further execution is authorized within this WI.

## Historical Fixed-Path Result

| Action label | Result | Sensitive output retained |
| --- | --- | --- |
| `PATCH_TEXT_AUDIT` | `PASS` | No |
| `CONNECTOR_J_DISCOVERY` | `PASS` | No |
| `EXPECTED_BUNDLE_EXISTENCE` | `FAIL` | No |
| `OPAQUE_BUNDLE_VALIDATION` | `NOT_RUN` | No |
| `URL_SCOPE_VALIDATION` | `NOT_RUN` | No |
| `DB_CONNECTION` | `NOT_RUN` | No |
| `PRE_STRUCTURE_CHECK` | `NOT_RUN` | No |
| `PATCH_APPLICATION` | `NOT_RUN` | No |
| `POST_STRUCTURE_CHECK` | `NOT_RUN` | No |

The original fixed-path attempt stopped at its existence gate. The amended
recovery attempt stopped at opaque validation. Neither attempt retained a
bundle path or contents, target identifier, connection detail, credential,
raw error, query result, or application data in this evidence.

## Verification

- Product tests: not run; no application operation was authorized.
- Historical documentation validation: PASS.
- Historical diff whitespace check: PASS.
- Current database reproduction: N/A. The one-time execution stopped before
  connection, so repeating it is not authorized.
- Structural verification: not run; current preflight did not authorize it.
- Documentation validation and diff whitespace checks: executed after this
  evidence update and reported separately from database execution evidence.

## Dependency And Follow-Up

- `WI-20260816-ATS-002`: **BLOCKED**. Required successful current predecessor
  evidence is absent; do not begin WI-002 activity.
- The current rerun is complete in a blocked state and may not be broadened,
  repaired, or repeated. A future recovery requires separate approval.

## Risks / Rollback

- Risk: the approved patch remains unapplied and no current structural proof
  exists because preflight stopped before connection.
- Rollback: none. No DDL was attempted; no table, database, or data may be
  dropped or deleted.
