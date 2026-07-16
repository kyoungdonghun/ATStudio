---
version: 1.3
last_updated: 2026-07-16
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

## Inter-document Dependencies

- **asset-registry.md**: Asset and capability registry, criteria for all asset registration
- **project-registry.md**: Project ID issuance and management criteria
- **context-registry.md**: Optional project context instance management
- **workboard.md**: Advisory cross-project work summary; current ATStudio tracking SoT remains `deliverables/`

## Related Guides

- Asset registration and traceability rules: See Work Tracking section in `AGENTS.md` / `CLAUDE.md` according to the active runtime
- Project creation and request intake: See REQ-Based Single Gate section in `AGENTS.md` / `CLAUDE.md` according to the active runtime
