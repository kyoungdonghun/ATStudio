---
version: 1.5
last_updated: 2026-09-09
project: system
owner: EO
category: registry
status: stable
---

# Registry Document Index

> Purpose: Detailed list of registry documents

## Document List

| Document Name | Project | Description | Status |
|--------|----------|------|------|
| [Capability & Asset Registry](asset-registry.md) | system | Capability & Asset Registry | stable |
| [Project Registry](project-registry.md) | system | Project Registry | stable |
| [Project Context Instances](context-registry.md) | system | Project Context Registry | stable |
| [Workboard (Work Status Board)](workboard.md) | system | Workboard Registry | stable |
| [V1 Artifact Retention Register](v1-artifact-retention-20260909.md) | ATS | Exact historical path/hash dispositions and private local recovery boundary | stable |
| [Development History Recovery Register](development-history-recovery-20260909.md) | ATS | Full 96-document semantic review, original/public hashes and current decision entry points | stable |

## Inter-document Dependencies

- **asset-registry.md**: Asset and capability registry, criteria for all asset registration
- **project-registry.md**: Project ID issuance and management criteria
- **context-registry.md**: Optional project context instance management
- **workboard.md**: Advisory cross-project work summary; current ATStudio tracking SoT remains `deliverables/`
- **v1-artifact-retention-20260909.md**: Historical artifact availability only; current operational SoT remains the design, policy and runbook documents. Archived raw evidence is not downloadable from a remote checkout.
- **development-history-recovery-20260909.md**: Privacy-reviewed historical documents at original paths; original dates/claims remain historical, and private backup or production recovery is not implied.

## Related Guides

- Asset registration and traceability rules: See Work Tracking section in `AGENTS.md` / `CLAUDE.md` according to the active runtime
- Project creation and request intake: See REQ-Based Single Gate section in `AGENTS.md` / `CLAUDE.md` according to the active runtime
