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
  - path: ../registry/project-registry.md
    reason: Project registry reference
  - path: ../standards/glossary.md
    reason: Standard term usage criteria
---
# REQUEST: Create New Project (project-request-template)

> This single template fixes the "project starting point".
> Fill in only minimum fields, and `MA/PS/SA` will supplement empty parts with questions.

## 0) Quick Rules (avoid confusion)

- This document is a **request** to "create the project (product/service) itself".
- **Project ID (`PRJ-...`) is issued by EO** and registered in `docs/registry/project-registry.md`.
- If repo doesn't exist yet, proceed with "TBD" (create/select during initial bootstrapping phase).

## Metadata (Request)

- **Request ID**: REQ-YYYYMMDD-###
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
- **Criticality Hint** (optional): LOW | MEDIUM | HIGH

## 1) Project Identity (new project "identity")

- **Proposed Project Name/Alias**: (required)
- **Repo (URL or local path)**: (required. If none yet, leave as `TBD` and confirm during bootstrapping phase)
- **Context ID**: (optional, if initial work context needed, MA issues `CTX-...`)
- **Owner (MA/PS)**: (optional)

## 2) Intent (what/why)

- **Problem**:
- **Success Criteria**: (quantitative/qualitative, 1-3 items. "What is success in Pilot")

## 3) Constraints

- **Deadline/Timebox**:
- **Budget/Cost constraints**:
- **Security/Privacy constraints**:
- **Must-not-do**: (absolutely forbidden actions)

## 4) Domain Context (minimum input)

- **Users**:
- **Core workflow**: (summary)
- **Key invariants**: (at least 1)
- **Key terminology**: (at least 3 words)

## 5) Tech/Environment (optional)

- **Preferred stack**:
- **Repo/Org**: (if possible)
- **Target platform**: web | mobile | backend | data | other

## 6) Output Expectation (scope fixation)

- **MVP scope**:
- **Out-of-scope**:
