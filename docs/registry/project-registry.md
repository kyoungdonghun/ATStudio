---
version: 1.0
last_updated: 2026-01-06
project: system
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
| PRJ-DDS-001 | dds | https://github.com/hiyong7759/agent-dds.git | active | MA | 2026-01-31 | Deonaeun Design System (KRDS-based) |
| PRJ-USI-001 | usi-groupware | - | active | MA | 2026-01-31 | USI Groupware Test |
| PRJ-POOMACY-001 | poomacy-v2 | - | active | MA | 2026-01-31 | Poomacy V2 (Shadcn/ui) |
| PRJ-POOMACY-DDS-001 | poomacy-dds | - | active | MA | 2026-01-31 | Poomacy V2 (DDS) |
| PRJ-DASHBOARD-001 | meta-dashboard | - | active | MA | 2026-02-01 | Meta Dashboard (Agent Monitoring) |
| PRJ-EXAMPLE | Example | repo-url-or-path | Draft | MA | YYYY-MM-DD | Example row |
