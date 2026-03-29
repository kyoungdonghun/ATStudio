---
version: 1.1
last_updated: 2026-03-29
project: system
owner: EO
category: policy
status: stable
dependencies:
  - path: ../standards/glossary.md
    reason: Standard terminology usage baseline
  - path: execution-policy.md
    reason: Execution policy reference
tier: 1
target_agents:
  - eo
  - re
  - qa
  - cr
task_types:
  - testing
  - review
---
# Quality Gates (Operational Checklist)

> Purpose: If rules exist only as "recommendations" in documents, they get missed. Enforce minimum checks before PR/review/execution to maintain operational quality.

## 1) Common (Always)

- [ ] **Reuse-first**: Verified at least 1 existing asset/pattern candidate for reuse
- [ ] **Domain Fit**: Confirmed no conflicts with domain invariants/terminology
- [ ] **Traceability**: No missing connections among WI/ADR/Asset (registry) where required
- [ ] **HITL (only when necessary)**: If there's Destructive/Network/Install/Git write/production impact, explained purpose/risks/rollback and got approval before execution (`docs/policies/execution-policy.md`)

## 2) Gates by Criticality

### LOW

- [ ] WI ID issued (commit message includes `WI-YYYYMMDD-###`)
- [ ] Recorded Goal/Reuse/Impact in 3 lines in commit/PR description (or brief impact analysis)
- [ ] (Exception) N/A allowed for trivial changes only (typos/formatting)

### MEDIUM

- [ ] Impact/rollback/validation organized at `docs/templates/impact-analysis-template.md` level
- [ ] Minimum regression verification (test/scenario) exists
- [ ] Registry Consumers updated (required when modifying assets, responsibility: worker→MA confirm→EO approve)
- [ ] **Post-evaluation (recommended→operational mandatory)**: After work completion, verify DoD criteria are met and document 1-3 lessons in `docs/retrospective/` if reusable.

### HIGH

- [ ] ADR required (including alternatives/risks/rollback)
- [ ] PG/RE perspective review recorded (security/reliability)
- [ ] If contract change, backward compatibility/versioning policy specified
- [ ] **Post-evaluation (required)**: Verify DoD, record lessons in `docs/retrospective/engineering.md` or `kick.md`, create evidence pack.

> Reference policies:
> - Versioning/deprecation: `docs/policies/versioning-policy.md`
> - Sensitive info: `docs/policies/security-policy.md`


