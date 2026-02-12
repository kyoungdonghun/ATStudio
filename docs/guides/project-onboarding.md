---
version: 1.0
last_updated: 2026-01-06
project: system
owner: MA
category: guide
status: stable
dependencies:
  - path: ../standards/glossary.md
    reason: Standard terminology usage criteria
  - path: ../templates/domain-context-template.md
    reason: Domain context template reference
---
# Project Onboarding/Startup Checklist

> Purpose: Fix checklist for "what to prepare on first deployment" to prevent omissions.

## 1) Before Project Deployment (Required)

- [ ] **Goal/Scope**: Project goals and success criteria organized in one paragraph
- [ ] **Constraints**: Security/budget/deadline/performance constraints specified
- [ ] **Criticality Criteria**: HIGH/MEDIUM/LOW judgment criteria shared

## 2) Domain Context (Required)

- [ ] Domain summary/invariants/terms organized based on `docs/templates/domain-context-template.md` (Owner: PS)
- [ ] Project glossary initialization complete: `docs/project/glossary.md` (minimum 5 core terms)
- [ ] Domain invariants specified (minimum 3)
- [ ] Reuse prohibition/caution items organized

## 5) Execution/Approval (HITL) (Required)

- [ ] Complex commands/destructive work receive explanation + approval before execution: `docs/policies/execution-policy.md`

## 6) Optional: Regression Criteria (Golden Set)

- [ ] Minimum Golden Set categories (G-SEC/G-TOOL/G-TRACE/G-QUALITY) decided: `docs/guides/eval-golden-set.md`
