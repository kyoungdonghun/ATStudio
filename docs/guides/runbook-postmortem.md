---
version: 1.0
last_updated: 2026-01-06
project: system
owner: RE
category: guide
status: stable
dependencies:
  - path: ../standards/glossary.md
    reason: Standard terminology usage criteria
  - path: ../templates/postmortem-template.md
    reason: Postmortem template reference
---
# Runbook & Postmortem Guide (Definition-First, Implementation Later)

> Purpose: Fix incident/accident response as **reproducible procedure**.
> In Phase 1, prepare only "format/procedure (definition)", and proceed with operational application (on-call/training/automation) in Phase 2+.

## 1) When Runbook Needed

- Tools/workflows included in MEDIUM/HIGH Criticality flow
- Areas that repeatedly fail (2+ times) or same type FAIL repeats in post-evaluation

## 2) Runbook Standard Format (Required Items)

- **Symptoms**: Phenomena observed in user/logs
- **Impact**: Scope/risk/criticality
- **Immediate Action (Containment)**: Prevent additional damage (snapshot/block/stop)
- **Diagnosis**: Reproduction procedure, verification commands (read-only priority)
- **Action**: Action steps (including approval need)
- **Verification**: Independent verification (RE or field actor)
- **Rollback**: How to revert if fail
- **Prevention**: Add Golden Set case/supplement policy/issue WI

## 3) Postmortem Process (Definition)

- **Trigger**: HIGH incident, or repeated FAIL/recurrence in MEDIUM
- **Deliverable**: `docs/templates/postmortem-template.md`
- **Connection Rules**:
  - Postmortem → **Issue WI (Required)** (recurrence prevention action)
  - Postmortem → Add/supplement Golden Set case (recommended)
  - Postmortem → Update related ADR/registry (if needed)

## 4) Implementation Order (Explicit)

- Phase 1 (MVP): Fix **format only** of runbook/postmortem, actual operations minimal (manual)
- Phase 2+: Apply on-call/training/automation (alarm→runbook link, template auto-generation etc.)
