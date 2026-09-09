---
version: 1.0
last_updated: 2026-08-17
project: ATS
owner: qa-integ
category: evidence-pack
status: stable
related_wi: WI-20260817-ATS-012
dependencies:
  - path: WI-20260817-ATS-012-handoff.md
    reason: Required corrected runtime-execution handoff packet
  - path: ../user/REQ-20260817-ATS-008.md
    reason: Approved corrected one-time read-only Inventory scope
  - path: ../user/WI-20260817-ATS-011-summary.md
    reason: Historical one-group gate result preserved without modification
---

> Historical public derivative, published in this working tree on 2026-09-09 by WI-20260909-ATS-018. Original dates, language, status, decisions and reported outcomes below are historical, not current approval or reverified recovery/production evidence. The original remains unchanged in private archive `v1-artifacts-20260909-084405`. See [A033: original identity, public SHA-256 and review](../../docs/registry/development-history-recovery-20260909.md#a033). Raw/local-only evidence pointers are lookup keys, not files supplied by a source checkout.

# Evidence Pack: WI-20260817-ATS-012

## Summary

- Completed one corrected read-only Inventory wrapper run. The in-memory parser
  accepted the required internal preflight group followed by the Inventory
  group, retaining only `count=0` and `NO_POSSIBLE_ORPHAN`.

## Scope / DoD Check

- [x] Created WI-012 handoff after confirming the next ATS WI sequence and
  before database access.
- [x] Generated one fresh guard-valid companion name in process memory only;
  it was not an Inventory target, was not printed or persisted, and was
  discarded.
- [x] Invoked the supported wrapper exactly once with Inventory and the opaque
  private bundle as its only private input.
- [x] Held the complete wrapper stream only in memory and accepted exactly two
  ordered standard groups: internal preflight first and Inventory second.
- [x] Required all fixed common safe values, format-validated both normalized
  source hashes without retaining them, required terminal PASS for both groups,
  and rejected unknown, duplicate, malformed, or out-of-order fields.
- [x] Required exactly one nonnegative count and its exact mapped state in the
  Inventory group; retained only `count=0` and `NO_POSSIBLE_ORPHAN`.
- [x] Stopped with no retry, cleanup, deletion, additional query, schema, seed,
  patch, manifest action, DDL, DML, application, browser, external-service,
  Git, source, configuration, README, policy, or historical-WI action.
- [x] Created the required user summary and this Evidence Pack.

## Reference Documents (Tier 0-2)

**Injected Context** (from the WI Handoff Packet):

| Tier | Document | Reason |
| --- | --- | --- |
| 0 | `docs/standards/core-principles.md` | Constitution and approved-scope control |
| 0 | `docs/standards/development-standards.md` | Runtime verification discipline |
| 0 | `docs/standards/documentation-standards.md` | Deliverable structure |
| 0 | `docs/standards/glossary.md` | Canonical WI terminology |
| 0 | `docs/standards/evidence-pack-standard.md` | Evidence-pack structure |
| 1 | `docs/policies/security-policy.md` | Secret-safe output boundary |
| 1 | `docs/policies/access-control-policy.md` | Least-privilege execution boundary |
| 1 | `docs/policies/quality-gates.md` | Verification mapping |
| 1 | `docs/architecture/system-design.md` | WI traceability model |
| 2 | `.agents/skills/react-best-practices/AGENTS.md` | Workspace injection rule only |
| 2 | `docs/standards/frontend-standards.md` | Workspace injection rule only |
| 2 | `docs/templates/eval-report-template.md` | Testing task-type injection |
| 2 | `docs/templates/requirements-request-template.md` | Workflow task-type injection |
| 2 | `docs/templates/wi-subagent-handoff-template.md` | Workflow task-type injection |

Injection rules applied:

- Rule source: `.claude/config/context-injection-rules.json`
- Assignee: `qa-integ`
- Task types: testing/QA, security, workflow, and documentation
- Required tiers: `0`, `1`

## Evidence Pointers

- `deliverables/agent/WI-20260817-ATS-012-handoff.md`: corrected two-group
  parser contract, output boundary, and prohibited actions.
- `deliverables/user/REQ-20260817-ATS-008.md`: approved single-run corrected
  Inventory scope and stop rules.
- `deliverables/user/WI-20260817-ATS-011-summary.md` and
  `deliverables/agent/WI-20260817-ATS-011-evidence-pack.md`: preserved
  historical one-group gate outcome.
- `scripts/database/bootstrap-disposable-mysql.ps1`: only supported entrypoint
  invoked by this WI.
- `scripts/database/DisposableMysqlBootstrap.java`: source-defined two-group
  safe-field and Inventory-output contract reviewed before the one invocation.
- `deliverables/user/WI-20260817-ATS-012-summary.md`: user-facing sanitized
  terminal result and action counts.

## Commands And Sanitized Results

- Supported-wrapper Inventory invocation: exactly `1`.
  - Internal preflight groups accepted: `1`.
  - Safe-gate result: PASS.
  - Retained runtime result: `count=0`, `NO_POSSIBLE_ORPHAN`.
  - Raw stream, hashes, companion name, bundle detail, connection data, rows,
    errors, and diagnostics: not retained.

## Prohibited Action Counts

- Separate Preflight, Observe, Create, Validate, and Drop invocations: `0`
- Additional Inventory invocations and retries: `0`
- Other database queries: `0`
- Schema, seed, patch, manifest, DDL, DML, cleanup, and deletion actions: `0`
- Application, browser, external-service, and Git actions: `0`
- Source, configuration, README, policy, and historical-WI changes: `0`

## Test Evidence

- `python .agents/skills/validate-docs/scripts/validate_docs.py` -> PASS:
  Tier 0 presence, internal links, 610 supported traceability IDs, and document
  index coverage all passed after the WI-012 deliverables were created.

## Risks / Rollback

- Risks: Zero count means only that the supported fixed Inventory predicate
  found no possible orphan. It does not authorize cleanup or establish a wider
  environment conclusion.
- Rollback: No runtime mutation occurred. Revert only WI-012 deliverables
  through approved follow-up work; no database rollback or cleanup is needed
  or authorized.

## Follow-ups

- None. This REQ permits no further WI. Any destructive cleanup, new
  investigation, or additional runtime execution requires separate approval.
