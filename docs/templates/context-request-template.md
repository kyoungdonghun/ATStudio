---
version: 1.0
last_updated: 2026-01-29
project: system
owner: MA
category: template
status: stable
dependencies:
  - path: ../guides/request-intake.md
    reason: Request intake guide reference
  - path: ../registry/context-registry.md
    reason: Context registry reference
  - path: ../standards/glossary.md
    reason: Standard term usage criteria
---
# REQUEST: Create Project Context Instance (context-request-template)

> Purpose: Create **context instance (CTX)** for specific work purpose for parallel processing.
> Subsequent requests/WI/confirmations/reports always include **Project ID + Context ID**.

## Metadata (Request)

- **Request ID**: REQ-YYYYMMDD-###
- **Project ID**: (required)
- **Context ID**: (can be left empty. MA will issue as `CTX-...` and register in `docs/registry/context-registry.md`)
- **Repo**: (required, URL or local path)
- **Target Ref**: (optional)
  - `branch:<name>` | `tag:<name>` | `commit:<sha>`
- **Environment**: (optional)
  - dev | stage | prod | local | other
- **Requester**:
- **Requested At**: YYYY-MM-DD
- **Request Status**: Proposed | Requested | Confirmed | Rejected | Cancelled
- **Created By**:
  - type: user | agent
  - id:
  - role: (if agent) MA|SA|PG|RE|SE|PS|PE|UV|EO
- **Created Via**: mobile | web | chat | cli | system
- **Source Ref** (optional):
- **Idempotency Key** (optional):

## 1) Purpose

- **Why create this context?**: (e.g., "Process REQ-...", "Pilot experiment", "Spike")
- **Related Request/WI** (optional):

## 2) Notes (optional)

- Constraints/considerations:
