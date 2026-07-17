---
version: 1.8
last_updated: 2026-07-17
project: ATS
owner: EO
category: registry
status: stable
dependencies:
  - path: ../templates/project-request-template.md
    reason: Project creation request template reference
  - path: ../standards/glossary.md
    reason: Standard terminology usage criteria
---
# Project Registry

> Purpose: **Source of truth for project selection** to clarify "which project is the request for?"
> Request/requirements addition aligns consistency based on `Project ID` in this list.

## Rules (Minimum)

- When new project request (`project-request-template.md`) is approved, issue **Project ID (`PRJ-...`)** and register here. (Responsible: EO)
- New projects may not have Repo yet, so register as `TBD`, then update when Repo is confirmed.
- Requirements addition for existing projects (`requirements-request-template.md`) must match **Project ID + Repo**.

## Project List

| Project ID | Name/Alias | Repo (URL or path) | Status | Owner | Last Updated | Notes |
| :-- | :-- | :-- | :-- | :-- | :-- | :-- |
| PRJ-ATS-001 | ATStudio | `.` (this repository) | active | MA | 2026-07-17 | AT.M customer-facing display brand; ATStudio is the internal project identifier. Official V1 baseline branch: `codex/p1-acceptance-hardening`. |

No separate client-demo branch is maintained for ATStudio V1.

## ATStudio Project Stats (PRJ-ATS-001)

> Last verified from the current working tree: 2026-07-17

| Category | Count | Reference |
| :-- | :-- | :-- |
| Backend REST APIs | 137 method-level mappings | `docs/design/api-spec.md` v27 |
| DB Tables / JPA Entities | 39 / 39 | `docs/design/db-schema.md` v21.1 |
| V1 DB Manifest | 39 tables, 449 columns, 153 indexes, 80 foreign keys | `docs/design/db-schema.md` v21.1 |
| Frontend Screens | 53 distinct visual page UIs | `docs/ui/atstudio-front-list.md` |
| Agents | 13 | `docs/architecture/system-design.md` §2.4 |
| SR Items completed | 82 | `docs/SR/index.md` |

Current ATStudio work tracking uses `deliverables/user/` for approved REQs and user summaries, and `deliverables/agent/` for WI handoffs and Evidence Packs. Registry/workboard examples are navigation aids, not a replacement source of truth.
