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
  - path: impact-analysis-template.md
    reason: Impact analysis template reference
  - path: ../standards/glossary.md
    reason: Standard term usage criteria
---
# REQUEST: Add/Change Requirements (requirements-request-template)

> Starting point for "adding requirements" to existing project.
> MA normalizes this request to WI (`impact-analysis-template.md`), leading to ADR if MEDIUM/HIGH.

## 0) Quick Rules (avoid confusion)

- This request applies **only to "existing projects"** → `Project ID + Repo` required.
- branch/env is **target context**.
- For execution/approval-needing steps (deployment/destructive changes), recommend fixing to `commit:<sha>` in WI/HITL after request.

## Metadata (Request)

- **Request ID**: REQ-YYYYMMDD-###
- **Project ID**: (required, "project (product/service) identifier")
- **Context ID**: (recommended, parallel processing context: `CTX-...`)
- **Project Name/Alias**: (optional)
- **Repo**: (required, URL or local path)
- **Target Ref**: (optional, "what code state is this based on")
  - `branch:<name>` | `tag:<name>` | `commit:<sha>`
  - Note: branches can be merged/deleted anytime, so `commit:<sha>` recommended for "fixed approval/execution"
- **Environment**: (optional, execution/validation target)
  - dev | stage | prod | local | other
- **Requester**:
- **Requested At**: YYYY-MM-DD
- **Request Status**: Proposed | Requested | Confirmed | Rejected | Cancelled
- **Created By**:
  - type: user | agent
  - id:
  - role: (if agent) MA|SA|PG|RE|SE|PS|PE|UV|EO
- **Created Via**: mobile | web | chat | cli | system
- **Source Ref** (optional): (related conversation/ticket/previous request/link)
- **Idempotency Key** (optional): (duplicate issuance prevention)
- **Criticality Hint** (optional): LOW | MEDIUM | HIGH (can leave empty if unknown)

## 0) Project Context (context fixation)

> This request must be fixed to "which project" it applies to.
> Fix context based on `Project ID + Context ID (CTX)` for parallel processing.

- **Active Project Context check**: Which project is this request for? (clarify with Project ID/Repo)
- **Target Context check (recommended)**: What code state (`Target Ref`) and what environment (`Environment`) is the change/validation/deployment based on?
- **Related Context (optional)**:
  - **Last Request ID**:
  - **Related WI**:
  - **Related ADR**:

## 1) Requirement

- **Change Type**: Add | Modify | Remove
- **Criticality**: P0 | P1 | P2 | P3
- **What**: (what to add/change?)
- **Why**: (why is it needed?)
- **Acceptance Criteria**: (2-5 items in testable form)
- **Non-functional Requirements** (optional): performance/security/operations/UX, etc.
- **Out-of-scope** (optional): excluded from this request scope

## 2) Constraints

- **Deadline/Timebox**:
- **Risk constraints**: (security/operations/performance, etc.)
- **Must-not-do**:

## 3) Impact Hint (optional, helpful if provided)

- **Likely affected modules/areas**:
- **Known consumers**:
- **Data/contract changes**: (if any)

## 4) Open Questions (optional)

- Q1:
- Q2:
