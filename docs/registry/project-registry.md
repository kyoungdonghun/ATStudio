---
version: 1.3
last_updated: 2026-05-25
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
| PRJ-ATS-001 | ATStudio | TBD | active | MA | 2026-05-25 | Shorts Music Marketplace (Java 17 + Spring Boot 4.x + React 18 + TypeScript) |

## ATStudio Project Stats (PRJ-ATS-001)

> Last verified: 2026-05-25

| Category | Count | Reference |
| :-- | :-- | :-- |
| Backend REST APIs | 123 | `docs/design/api-spec.md` v12 |
| DB Tables | 33 | `docs/design/db-schema.md` |
| Frontend Screens | 52 | `docs/index.md` |
| Agents | 13 | `docs/architecture/system-design.md` §2.4 |
| SR Items completed | 82 | `docs/SR/index.md` |
