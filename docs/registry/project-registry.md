---
version: 2.3
last_updated: 2026-08-13
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
| PRJ-ATS-001 | ATStudio | `.` (this repository) | active | MA | 2026-08-13 | AT.M customer-facing display brand; ATStudio is the internal project identifier. Official V1 baseline branch: `codex/p1-acceptance-hardening`. |

No separate client-demo branch is maintained for ATStudio V1.

## ATStudio Project Stats (PRJ-ATS-001)

> Last verified from the current working tree: 2026-08-13. These source counts
> include the approved WI-20260808-ATS-014~021 implementation in the shared
> dirty worktree; they are not production-deployment evidence.

| Category | Count | Reference |
| :-- | :-- | :-- |
| Backend REST APIs | 150 method-level mappings | `docs/design/api-spec.md` v30.3 |
| DB Tables / JPA Entities | 42 / 42 | `docs/design/db-schema.md` v24.2 |
| V1 DB Manifest | Current 42/506/173/90/6 fresh-MySQL manifest recorded; DG-067-09B `RUN-PASS-CLEANED`; predecessor 41-table evidence historical only | `docs/design/db-schema.md` v24.2 |
| Frontend Screens | 53 distinct visual page UIs | `docs/ui/atstudio-front-list.md` |
| Agents | 13 | `docs/architecture/system-design.md` §2.4 |
| SR Items completed | 82 | `docs/SR/index.md` |

Current ATStudio work tracking uses `deliverables/user/` for approved REQs and user summaries, and `deliverables/agent/` for WI handoffs and Evidence Packs. Registry/workboard examples are navigation aids, not a replacement source of truth.
