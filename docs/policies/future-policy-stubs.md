---
version: 1.0
last_updated: 2026-01-28
project: system
owner: EO
category: policy
status: draft
dependencies:
  - path: execution-policy.md
    reason: HITL approval policy (currently in operation)
  - path: quality-gates.md
    reason: Quality gates (currently in operation)
---

# Future Policy Stubs (Phase 2+ Reserved Policies)

> Purpose: In the current Phase 1 (MVP) stage, we only fix definitions, and actual implementation of these policies will begin in Phase 2+. This file consolidates them in one place for management.
> Each section of this file is a candidate to grow into an independent policy.

---

## 1. Backup / DR Policy

- **RTO/RPO**: Defined per project in `domain-context-template.md`
- **Recovery Procedures**: Use runbook in `docs/guides/runbook-postmortem.md`
- **Current Operation**: git revert / snapshots are sufficient. Backup retention/rehearsal deferred to Phase 2+

## 2. Log Retention Policy

- **Classification**: Execution Log / Approval Log (HITL) / Audit Log
- **Sensitive Info**: Do not log tokens/passwords (refer to `security-policy.md`)
- **Retention**: Default 30 days / incident-related 90 days (recommended)
- **Current Operation**: Format defined only. Storage/search/disposal automation deferred to Phase 2+

## 3. Observability / SLO Policy

- **Core SLI**: Tool call success rate / HITL compliance rate / work lead time
- **Recommended SLO**: Zero HITL approval omissions / 98%+ success rate
- **Tracking Principle**: Major events must be connected by IDs (left as evidence pointers)
- **Current Operation**: Manual measurement only. Collection/dashboard/alerting automation deferred to Phase 2+

## 4. Release / Deployment Policy

- **Release Gate**: MEDIUM/HIGH changes require passing `quality-gates.md`
- **Rollback Capability**: All deployments must be reversible
- **Hotfix**: Treated as HIGH (minimum WI/verification/rollback required)
- **Current Operation**: Definition fixed only. Deployment pipeline integration deferred to Phase 2+

## 5. Remote Request Policy

- **Concept**: Request issuance from web/mobile + HITL approval processing
- **Request Schema Essentials**: request_id, project_id, type, intent (goal/constraints/success_criteria)
- **Approval**: Strongly enforced at HITL stage only (simple requests can be post-notified)
- **Current Operation**: CLI single entry point. Web/mobile intake deferred to Phase 2+

---

> **Note**: The timing for each section to grow into an independent policy will be determined when implementing the corresponding feature.
