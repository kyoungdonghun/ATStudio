---
version: 1.1
last_updated: 2026-07-16
project: system
owner: EO
category: registry
status: stable
dependencies:
  - path: project-registry.md
    reason: Project registry reference
  - path: ../templates/context-request-template.md
    reason: Context creation request template reference
  - path: ../standards/glossary.md
    reason: Standard terminology usage criteria
---
# Project Context Instances

> Purpose: Optionally identify parallel or long-lived context instances when that extra coordination is useful.
> ATStudio's active REQ/WI flow does not require a CTX ID on every request, confirmation, report, execution, or verification.

## 1) Concept

- **Project (PRJ)**: Stable key at product/service level (e.g., `PRJ-001`)
- **Project Context Instance (CTX)**: Context instance for specific work/purpose (e.g., `CTX-20250101-003`)
  - Multiple CTXs can exist simultaneously for the same PRJ (parallel processing)

> Recommendation: Create CTX only for parallel work where a stable context identifier adds value, and close it when completed. The authoritative ATStudio execution chain remains the approved REQ plus WI handoff/Evidence Pack under `deliverables/`.

## 2) Minimum Fields (Definition)

- **context_id**: `CTX-YYYYMMDD-###`
- **project_id**: `PRJ-...`
- **name/purpose**: (e.g., "Process Request REQ-...", "Pilot experiment", "Refactor spike")
- **repo**: URL or path
- **target_ref**: `branch:<name>` | `tag:<name>` | `commit:<sha>` (recommended: commit for execution/approval)
- **environment**: dev | stage | prod | local | other
- **status**: Active | Closed | Superseded
- **created_by**: user | agent + id + (agent role)
- **created_at**
- **source_ref**: related_request / related_wi / link

## 3) Instance List

| Context ID | Project ID | Purpose | Repo | Target Ref | Env | Status | Created By | Last Updated | Notes |
| :-- | :-- | :-- | :-- | :-- | :-- | :-- | :-- | :-- | :-- |
| CTX-EXAMPLE | PRJ-EXAMPLE | Example | repo-url-or-path | branch:main | dev | Active | user | YYYY-MM-DD | |
