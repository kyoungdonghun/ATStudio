# SR-C-04

**Category**: Documentation — Broken Internal Links (DO-1)
**Status**: OPEN
**Priority**: LOW (meta-only docs, not injected for ATStudio domain work)

## Target

`docs/guides/` directory does not exist, but is referenced in 30+ locations across meta-system documents.

## Impact

All broken links are in **meta-only** documents (not injected for ATStudio domain work per `docs/index.md` scope classification). Functional impact on ATStudio operations is zero. However, these broken links degrade document usability for framework-level maintenance.

## Affected Files (high-traffic first)

| File | Broken Reference |
|------|-----------------|
| `docs/registry/asset-registry.md` | ~15 references to `docs/guides/*.md` |
| `docs/standards/documentation-standards.md` | ~8 references to `docs/guides/*.md` |
| `docs/policies/quality-gates.md` | `docs/guides/agent-evaluation.md`, `docs/guides/eval-golden-set.md` |
| `docs/policies/execution-policy.md` | `../guides/operation-process.md` |
| `docs/policies/future-policy-stubs.md` | `docs/guides/runbook-postmortem.md` |
| `docs/registry/workboard.md` | `docs/guides/request-intake.md`, `docs/guides/operation-process.md`, `docs/guides/agent-facing-docs.md` |
| `docs/registry/index.md` | `docs/guides/traceability.md`, `docs/guides/request-intake.md` |
| `docs/templates/adr-template.md` | `../guides/traceability.md` |
| `docs/templates/adr-example.md` | `../guides/operation-process.md`, `../guides/traceability.md` |
| `docs/templates/impact-analysis-template.md` | `../guides/agent-docs-map.md`, `../guides/operation-process.md` |
| `docs/templates/eval-report-template.md` | `../guides/agent-evaluation.md`, `../guides/eval-golden-set.md` |
| `docs/architecture/system-design.md` | `docs/guides/development-workflow.md` (body text line ~162) |
| `docs/adr/ADR-20251230-001-*.md` | `../guides/operation-process.md`, `../guides/traceability.md` |

## Root Cause

The `docs/guides/` directory was planned but never created (or was deleted). Referenced guides include:
- `development-workflow.md`, `operation-process.md`, `request-intake.md`
- `traceability.md`, `agent-facing-docs.md`, `agent-docs-map.md`
- `agent-evaluation.md`, `eval-golden-set.md`, `project-onboarding.md`
- `runbook-postmortem.md`, `scope-domain.md`

## Resolution Options

**Option A (Recommended)**: Accept that these guides are not needed for ATStudio operations. Replace high-impact broken links in Universal-scope documents (quality-gates, execution-policy) with equivalent CLAUDE.md or architecture/system-design.md references.

**Option B**: Create stub `docs/guides/` files with minimal content pointing to the canonical SoT (CLAUDE.md, system-design.md).

> Decision needed from MA/EO before implementation.
