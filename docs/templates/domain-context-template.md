---
version: 1.0
last_updated: 2026-01-29
project: system
owner: PS
category: template
status: stable
dependencies:
  - path: ../guides/scope-and-domain.md
    reason: Scope & Domain guide reference
  - path: ../work-items/examples/project-domain-context-example.md
    reason: Example reference
  - path: ../standards/glossary.md
    reason: Standard term usage criteria
---
# Project Domain Context

> Purpose: Prevent meaning/invariant conflicts from "reusing by rules" without domain understanding.
>
> **Example:** See `docs/work-items/examples/project-domain-context-example.md`

## Metadata

- **Project ID/Name**:
- **Domain Owner** (PS):
- **Last Updated**: YYYY-MM-DD

## 1) Domain Summary (one paragraph)

What does this project (product/service) do, who are the users, and what are the success criteria?

## 2) Core Entities

- Entity:
  - Meaning:
  - Key fields:

## 3) Domain Invariants

> List of "this must never be violated". Reference point for reuse/refactoring decisions.

- INV-001:
- INV-002:

## 4) Terminology

> Fix terms so the same word doesn't get used with different meanings.

| Term | Meaning | Notes |
| :--- | :------ | :---- |
|      |         |       |

### Glossary Rules (required)

- Project terms are based on **project glossary**: `docs/project/glossary.md` (create in project repo)
- Avoid conflicts with System (Global) terms from `agentic-subagent-team/docs/standards/glossary.md`
- If conflict is unavoidable, specify "meaning in this project" here (project domain context) and reflect in project glossary

## 5) Policies & Constraints

- Security/Privacy:
- Compliance:
- Performance/SLO:
- Cost constraints:

## 6) Reuse Constraints (forbidden/caution)

> If applicable below, extension/fork/replacement may be needed.

- Forbidden/caution items:

## 7) Acceptance Tests (domain perspective validation scenarios)

- Scenario 1:
- Scenario 2:
