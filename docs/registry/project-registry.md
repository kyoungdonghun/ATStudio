---
version: 1.2
last_updated: 2026-05-16
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
| PRJ-ATS-001 | ATStudio | `C:\Users\jm991\Desktop\project\ATStudio` | active | MA | 2026-05-16 | Shorts Music Marketplace (Java 17 + Spring Boot 4.x + React 18 + TypeScript) |

## ATStudio Project Stats (PRJ-ATS-001)

> Last verified: 2026-05-16

| Category | Count | Reference |
| :-- | :-- | :-- |
| Backend REST APIs | 107 | `docs/design/api-spec.md` v9 |
| DB Tables | 28 | `docs/design/db-schema.md` v6 |
| Frontend Screens | 51 | `docs/ui/atstudio-front-list.md` v6 |
| Agents | 13 | `docs/architecture/system-design.md` §2.4 |
| SR Items completed | 34 | `docs/SR/index.md` |
