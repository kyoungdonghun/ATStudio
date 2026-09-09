---
version: 1.0
last_updated: 2026-08-17
project: ATS
owner: se
category: evidence-pack
status: stable
related_wi: WI-20260817-ATS-010
dependencies:
  - path: WI-20260817-ATS-010-handoff.md
    reason: Required pre-implementation handoff packet
  - path: ../user/REQ-20260817-ATS-007.md
    reason: Approved Inventory implementation scope
---

> Historical public derivative, published in this working tree on 2026-09-09 by WI-20260909-ATS-018. Original dates, language, status, decisions and reported outcomes below are historical, not current approval or reverified recovery/production evidence. The original remains unchanged in private archive `v1-artifacts-20260909-084405`. See [A029: original identity, public SHA-256 and review](../../docs/registry/development-history-recovery-20260909.md#a029). Raw/local-only evidence pointers are lookup keys, not files supplied by a source checkout.

# Evidence Pack: WI-20260817-ATS-010

## Summary

- Added the approved read-only Inventory action to the supported bootstrap,
  including non-DB guards for its bounded query and output contract.

## Scope / DoD Check

- [x] Scanned existing 2026-08-17 ATS WI files and created the next valid
  `WI-20260817-ATS-010` handoff before implementation.
- [x] Added Inventory to the PowerShell and Java action allowlists without
  removing existing actions.
- [x] Retained the existing preconnection database-name, loopback, port, and
  43-table source gates before credential loading, Connector/J loading, or a
  connection.
- [x] Kept Inventory independent from unrecorded manifest refusal while
  retaining the existing Create and Validate restrictions.
- [x] Restricted Inventory to one admin/root connection, one fixed aggregate,
  numeric `inventory.count`, and the exact bounded zero/positive state.
- [x] Added focused non-DB coverage for Inventory acceptance/refusal, wrapper
  allowlist, aggregate safety, output keys/states, no target selection, and
  legacy action behavior.
- [x] Updated only the affected README contract.
- [x] Ran no Inventory invocation, database connection, external bundle,
  application/browser action, external service, data mutation, cleanup,
  staging, commit, or other Git state change.

## Reference Documents (Tier 0-2)

**Injected Context** (from the WI handoff packet):

| Tier | Document | Reason |
| --- | --- | --- |
| 0 | `docs/standards/core-principles.md` | Constitution and approved-scope control |
| 0 | `docs/standards/development-standards.md` | Java and verification discipline |
| 0 | `docs/standards/documentation-standards.md` | Deliverable structure |
| 0 | `docs/standards/glossary.md` | Canonical WI terminology |
| 0 | `docs/standards/evidence-pack-standard.md` | Evidence-pack structure |
| 1 | `docs/policies/security-policy.md` | Secret-safe output boundary |
| 1 | `docs/policies/access-control-policy.md` | Least-privilege database boundary |
| 1 | `docs/policies/quality-gates.md` | Verification mapping |
| 1 | `docs/architecture/system-design.md` | WI traceability model |
| 2 | `.agents/skills/react-best-practices/AGENTS.md` | Workspace injection rule only |
| 2 | `docs/standards/frontend-standards.md` | Workspace injection rule only |
| 2 | `docs/templates/eval-report-template.md` | Testing task-type injection |
| 2 | `docs/templates/requirements-request-template.md` | Workflow task-type injection |
| 2 | `docs/templates/wi-subagent-handoff-template.md` | Workflow task-type injection |

Injection rules applied:
- Rule source: `.claude/config/context-injection-rules.json`
- Assignee: `se`
- Task types: implementation, testing/QA, security, workflow, and documentation
- Required tiers: `0`

## Evidence Pointers

- `deliverables/agent/WI-20260817-ATS-010-handoff.md`: required packet created
  before source changes and linked to `REQ-20260817-ATS-007`.
- `scripts/database/bootstrap-disposable-mysql.ps1`: supported wrapper action
  allowlist adds only `Inventory`; its existing preflight-first sequence is
  unchanged.
- `scripts/database/DisposableMysqlBootstrap.java`: Inventory enum/switch
  route, fixed aggregate constant, and dedicated admin-only aggregate path.
- `scripts/database/test-bootstrap-guards.ps1`: non-DB Inventory contract
  coverage plus retained legacy checks.
- `scripts/database/README.md`: supported behavior and read-only boundary.
- `deliverables/user/WI-20260817-ATS-010-summary.md`: user-facing result.
- `deliverables/agent/WI-20260817-ATS-010-evidence-pack.md`: this evidence.

## Commands And Results

- `powershell.exe -NoProfile -ExecutionPolicy Bypass -File scripts/database/test-bootstrap-guards.ps1`
  -> PASS: 25 named checks. The suite exercises only parsing, source inspection,
  and Java preflight paths; it made no database connection.
- `javac -d <temporary-output-directory> scripts/database/DisposableMysqlBootstrap.java`
  -> PASS: Java source compiled without Connector/J and without a connection.
- `python .agents/skills/validate-docs/scripts/validate_docs.py`
  -> PASS: Tier 0 documents, internal links, traceability IDs, and document
  index coverage.
- `git diff --check -- <WI-010 scoped files>`
  -> PASS: no whitespace errors.

## Risks / Rollback

- Risks: This WI establishes source and non-DB guard evidence only. It does not
  establish any live runtime result, which remains explicitly outside this WI.
- Rollback: Restore the prior versions of the wrapper, Java bootstrap, guard
  suite, and README; remove WI-010 deliverables only through approved follow-up
  work. No schema, data, credential, or environment rollback is required.

## Follow-ups

- None started. Per direction, WI-011 was not created or invoked in this WI.
