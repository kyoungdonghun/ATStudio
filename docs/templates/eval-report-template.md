---
version: 1.0
last_updated: 2026-01-29
project: system
owner: RE
category: template
status: stable
dependencies:
  - path: ../guides/agent-evaluation.md
    reason: Evaluation guide reference
  - path: ../policies/quality-gates.md
    reason: Quality gate reference
  - path: ../standards/glossary.md
    reason: Standard term usage criteria
---
# Eval Report (Post-work Evaluation Report)

> Purpose: Fix "what went well/what was problematic/what to improve" within 10-20 minutes after work completion.
> Reference: `docs/guides/agent-evaluation.md`

## Metadata

- **Eval ID**: EVAL-YYYYMMDD-###
- **Work Item**: WI-YYYYMMDD-###
- **Criticality**: HIGH | MEDIUM | LOW
- **Actors involved**: MA / SA / SE / PG / RE / EO
- **Related ADR** (MEDIUM/HIGH): `docs/adr/ADR-...` (or link)

## 1) Result

- **PASS/FAIL**: (based on quality-gates criteria)
- **What changed**: (3-5 line summary of file/asset/policy/tool changes)

## 2) Findings (positives / problems)

### Positives (1-2 items)

-

### Problems (1-3 items, no excessive listing)

- **Finding**:
  - **Category**: Reuse | Impact | Traceability | HITL | Secrets | Domain Fit | Regression | Other
  - **Evidence**: (WI/PR/commit/log evidence link)
  - **Root cause (estimated)**:

## 3) Actions (1-3 improvement actions)

- **Action 1**:
  - Owner:
  - Due:
  - Done definition:

## 4) Regression Lock (regression prevention fix)

- **Golden Set update needed?**: Yes / No
  - If Yes: Add case to `docs/guides/eval-golden-set.md` (ID: G-XXX-###)
- **Policy/template modification needed?**: Yes / No
  - If Yes: Target files and summary of changes
