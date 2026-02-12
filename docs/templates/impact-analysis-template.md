---
version: 1.0
last_updated: 2026-01-29
project: system
owner: MA
category: template
status: stable
dependencies:
  - path: ../guides/operation-process.md
    reason: Impact analysis process reference
  - path: ../standards/glossary.md
    reason: Standard term usage criteria
---
# Impact Analysis Sheet

> Purpose: Fix "what/why/what to reuse/where impact is/how to validate and rollback" in one sheet for both new and change work.

## Metadata

- **Work Item ID**: WI-YYYYMMDD-###
- **Project ID**: {project identifier} (e.g., AMS, ECP)
- **Repository**: {project repo URL or path}
- **Location**: `docs/project/WI-YYYYMMDD-###.md` (within project repo)
- **Requester**:
- **Initiator**: user | agent (who started this change?)
- **Related Request** (optional): REQ-YYYYMMDD-### (if WI normalized from request)
- **Criticality**: HIGH | MEDIUM | LOW
- **Type**: New Feature | Change | Refactor | Bugfix | Docs | Ops
- **Target Layer**: Local | Module | Package | System Tool | Global Asset
- **Tags/Keywords**: (use Key from `docs/standards/glossary.md` when possible. e.g., auth, billing, wi, adr)

## Reference Context

> Purpose: Record related documents explored according to Section 5 (dynamic context exploration) of [agent-docs-map.md](../guides/agent-docs-map.md) before work.

- **Essential Standards/Guides**: (e.g., `development-standards.md`, `execution-policy.md`)
- **Related ADRs**: (e.g., `ADR-20251230-001`)
- **Similar/Past Work Items**: (e.g., `WI-20251230-001`)
- **Other Resources**: (e.g., related external docs, specific asset IDs, etc.)

## Goal (what/why)

- **Problem**:
- **Success Criteria**:

## Reuse-first (reuse candidates)

- **Existing candidates** (from `docs/registry/asset-registry.md`):
  - AST-…:
- **Decision**: Reuse as-is | Add extension point | Refactor/cleanup | Replace (Deprecated)
- **If not reusing, why**:

## Impact Analysis

- **Consumers**: Who/where uses this feature/contract?
- **Contracts**: What input/output/format/behavior changes?
- **Backward compatibility**: What's the backward compatibility strategy?
- **Risk**: What could break?
- **Rollback**: How to revert if it fails?

## Related Work Items (optional, recommended)

> No need to list "all WIs with impact". Just link **1-5 most related** and the map connects.

- **Similar/Related**: WI-…
- **Depends on / Blocked by**: WI-…
- **Blocks**: WI-…
- **Follow-ups**: WI-…
- **Supersedes / Replaces**: WI-…
- **Superseded by**: WI-…

## Parallelization & Triggers — for work management

> Purpose: Reveal at WI level "what work can be done in parallel / what are prerequisites / what triggers next work start".

- **Parallelizable with** (parallel candidates): WI-…
- **Work coordination note** (overlap/conflict risk): (e.g., possible same file modification, contract change)
- **Trigger event(s)** (follow-up start conditions):
  - PR merge | Artifact Done | Approval Done(HITL/EO) | Deploy Done | Verify Done
  - Details: (e.g., "ADR approval complete", "dev deployment complete", "PDS7 index creation complete")

## Implementation Notes (summary)

- **Approach**:
- **Files/Modules likely touched**:

## Validation

- **Tests**:
- **Manual checks**:
- **Observability hooks** (logs/metrics/alerts):

## Related ADR (MEDIUM/HIGH)

- ADR:
