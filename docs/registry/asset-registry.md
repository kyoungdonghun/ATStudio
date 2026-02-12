---
version: 1.0
last_updated: 2026-01-06
project: system
owner: EO
category: registry
status: stable
dependencies:
  - path: ../guides/traceability.md
    reason: Traceability system reference
  - path: ../standards/glossary.md
    reason: Standard terminology usage criteria
---
# Capability & Asset Registry

> Purpose: **Quickly find** "what already exists" and make reuse/promotion/impact analysis easy.

## 0) Why This Registry Enables "Traceability"

The registry is an "index" that enforces the following **3 linking chains**.

- **Work Item (WI)**: Unit of change (request/impact/verification) — `docs/work-items/`
- **ADR**: Decision rationale (alternatives/risks/rollback) — `docs/adr/`
- **Asset**: Reusable unit (policy/template/code/tool) — This document

Minimum rules:

- **Issue WI ID when there's a change** (File creation recommended for MEDIUM/HIGH, LOW can just have WI-XXX in commit message)
- **Create ADR for MEDIUM/HIGH**
- **Register in registry with Asset ID for new/reused assets, and update Consumers**

For practical flow, see `docs/guides/traceability.md`.

## 1) Rules (Required)

- When adding new features/tools/policies, **must register here**.
- Registration items must have at least **Owner + Interface/Contract + Consumers + Status**.
- "Just similar code" is not an asset. Must have **name/boundary/contract** to be reusable.

## 2) Capability Registry (Work Units)

| Capability ID | Name | Owner(A) | Main Consumers | Contract/Input-Output | Version | Status |
| :------------ | :--------------------- | :------- | :----------------------------------------------- | :----------------------------------------------- | :--- | :----- |
| CAP-001 | Impact Analysis | MA | All change work | `docs/templates/impact-analysis-template.md` | v1.0 | Stable |
| CAP-002 | Asset Promotion | EO | Common assets | `docs/templates/asset-promotion-checklist.md` | v0.5 | Draft |
| CAP-003 | ADR Recording | SA | MEDIUM/HIGH changes | `docs/templates/adr-template.md` | v1.0 | Stable |
| CAP-004 | Registry Maintenance | EO | All work | `docs/registry/asset-registry.md` | v1.0 | Stable |
| CAP-005 | Glossary Maintenance | EO | All work | `docs/standards/glossary.md` | v1.0 | Stable |
| CAP-006 | Execution Policy | EO | All work | `docs/policies/execution-policy.md` | v1.0 | Stable |
| CAP-007 | Quality Gates | EO | All work | `docs/policies/quality-gates.md` | v1.0 | Stable |
| CAP-008 | Reuse-first Decision | MA | All change work | `docs/guides/operation-process.md` | v1.0 | Stable |
| CAP-009 | Domain Fit Gate | PS/SA | When judging reuse | `docs/guides/scope-domain.md` | v1.0 | Stable |
| CAP-010 | Secrets Management | PG | When handling sensitive information | `docs/policies/security-policy.md` | v1.0 | Stable |
| CAP-011 | Version/Deprecation Management | EO | When changing assets | `docs/policies/versioning-policy.md` | v1.0 | Stable |
| CAP-012 | Golden Set Operation | RE | During regression testing | `docs/guides/eval-golden-set.md` | v1.0 | Stable |
| CAP-013 | Project Onboarding | MA | When starting project | `docs/guides/project-onboarding.md` | v1.0 | Stable |
| CAP-014 | Agent Evaluation Operation | RE | All work (especially MEDIUM/HIGH) | `docs/guides/agent-evaluation.md` | v1.0 | Stable |
| CAP-015 | Phase 2+ Reserved Policy | EO | Operational metrics/backup/release/log/remote request definition | `docs/policies/future-policy-stubs.md` | v0.5 | Draft |
| CAP-016 | Runbook/Postmortem Operation | RE | During incident/accident response | `docs/guides/runbook-postmortem.md` | v0.5 | Draft |
| CAP-019 | Access Control Operation | PG | When authority/approval needed | `docs/policies/access-control-policy.md` | v0.5 | Draft |
| CAP-022 | Request Intake | MA | All work (starting point) | `docs/guides/request-intake.md` | v0.5 | Draft |
| CAP-023 | Project List Maintenance | EO | When project selection/mapping needed | `docs/registry/project-registry.md` | v0.5 | Draft |
| CAP-024 | Context Instance Maintenance | EO | When parallel processing/work session needed | `docs/registry/context-registry.md` | v0.5 | Draft |

## 3) Asset Registry (Reusable Assets)

### 3.1 Policy Assets (Documents/Rules)

| Asset ID | Name | Scope | Boundary/Purpose | Owner(A) | Location | Consumers | Version | Status |
| :---------- | :------------------------ | :------------- | :---------------------------------------------------- | :------- | :----------------------------------------------- | :-------- | :--- | ------ |
| AST-POL-001 | Reuse-first Process | System(Global) | Reuse/change management procedure | EO | `docs/guides/operation-process.md` | All work | v1.0 | Stable |
| AST-POL-002 | Agent Plan | System(Global) | Overall design/roadmap | EO | `docs/architecture/system-design.md` | All actors | v1.0 | Living |
| AST-POL-003 | Traceability Guide | System(Global) | WI/ADR/Asset linking rules | EO | `docs/guides/traceability.md` | All work | v1.0 | Stable |
| AST-POL-004 | Scope & Domain Model | System(Global) | Project↔system scope/domain gate | SA/PS | `docs/guides/scope-domain.md` | MA/SA/PS | v1.0 | Stable |
| AST-POL-005 | Template Governance | System(Global) | Template proliferation prevention/reuse principle | EO | `docs/policies/template-governance.md` | All work | v1.0 | Stable |
| AST-POL-006 | Glossary | System(Global) | Common glossary (synonym/forbidden term management) | EO | `docs/standards/glossary.md` | All work | v1.0 | Stable |
| AST-POL-007 | Glossary Sources | System(Global) | Terminology standard source link/verification date management | EO | `docs/standards/glossary-sources.md` | All work | v1.0 | Stable |
| AST-POL-008 | Execution Policy | System(Global) | Explanation+approval before complex command/tool execution | EO | `docs/policies/execution-policy.md` | All work | v1.0 | Stable |
| AST-POL-009 | Quality Gates | System(Global) | Operational quality gates by criticality | EO | `docs/policies/quality-gates.md` | All work | v1.0 | Stable |
| AST-POL-011 | Versioning & Deprecation | System(Global) | Version/release/deprecation operational rules | EO | `docs/policies/versioning-policy.md` | All work | v1.0 | Stable |
| AST-POL-012 | Secrets & Privacy Policy | System(Global) | Secret/sensitive information handling policy | PG | `docs/policies/security-policy.md` | All work | v1.0 | Stable |
| AST-POL-013 | Eval Golden Set | System(Global) | Eval/regression (golden set) criteria | RE | `docs/guides/eval-golden-set.md` | All work | v1.0 | Stable |
| AST-POL-014 | Project Onboarding | System(Global) | Project startup checklist | MA | `docs/guides/project-onboarding.md` | All work | v1.0 | Stable |
| AST-POL-015 | Agent Evaluation | System(Global) | Agent activity evaluation (improvement/regression prevention) | RE | `docs/guides/agent-evaluation.md` | All work | v1.0 | Stable |
| AST-POL-016 | Future Policy Stubs | System(Global) | Observability/Backup/Release/log/remote request definition (Phase 2+ implementation reserved) | EO | `docs/policies/future-policy-stubs.md` | All work | v0.5 | Draft |
| AST-POL-017 | Runbook & Postmortem | System(Global) | Runbook/postmortem operational definition (lower priority implementation) | RE | `docs/guides/runbook-postmortem.md` | All work | v0.5 | Draft |
| AST-POL-020 | Access Control Policy | System(Global) | Access control/approval/least privilege principle (lower priority implementation) | PG | `docs/policies/access-control-policy.md` | All work | v0.5 | Draft |

### 3.2 Templates

| Asset ID | Name | Purpose | Owner(A) | Location | Version | Status |
| :---------- | :------------------------ | :--------------------------------- | :------- | :-------------------------------------------- | :--- | :----- |
| AST-TPL-001 | Impact Analysis Sheet | New/change impact analysis sheet | MA | `docs/templates/impact-analysis-template.md` | v1.0 | Stable |
| AST-TPL-002 | ADR Template | Decision record | SA | `docs/templates/adr-template.md` | v1.0 | Stable |
| AST-TPL-011 | DB Design Template | Schema, ERD, index strategy design | SA | `docs/templates/db-design-template.md` | v1.0 | Stable |
| AST-TPL-012 | Interface Design Template | API/Tool contract and data contract definition | SA | `docs/templates/interface-design-template.md` | v1.0 | Stable |
| AST-TPL-013 | IA Design Template | Data flow and logic layer design | PS/SA | `docs/templates/ia-design-template.md` | v1.0 | Stable |
| AST-TPL-014 | Process Design Template | Business logic and workflow definition | SA/SE | `docs/templates/process-design-template.md` | v1.0 | Stable |
| AST-TPL-003 | Asset Promotion Checklist | Asset promotion check | EO | `docs/templates/asset-promotion-checklist.md` | v0.5 | Draft |
| AST-TPL-004 | Project Domain Context | Project domain/invariants/terminology | PS/SA | `docs/templates/domain-context-template.md` | v0.5 | Draft |
| AST-TPL-005 | Project Glossary Template | Project glossary template | PS | `docs/templates/glossary-template.md` | v0.5 | Draft |
| AST-TPL-006 | Eval Report | Post-evaluation report template | RE | `docs/templates/eval-report-template.md` | v0.5 | Draft |
| AST-TPL-007 | Postmortem Template | Postmortem (RCA) template | RE | `docs/templates/postmortem-template.md` | v0.5 | Draft |
| AST-TPL-008 | Request: Project Create | New project creation request template | MA | `docs/templates/project-request-template.md` | v0.5 | Draft |
| AST-TPL-009 | Request: Requirements Add | Requirements addition request template | MA | `docs/templates/requirements-request-template.md` | v0.5 | Draft |
| AST-TPL-010 | Request: Context Create | Context instance creation request template | MA | `docs/templates/context-request-template.md` | v0.5 | Draft |

## 4) Version/Stability Standards

- **Draft**: Experimental/frequent changes, consumers use with limitations
- **Stable**: Interface/behavior stabilized, backward compatibility principle applied
- **Deprecated**: Alternative path provided, deprecation schedule/migration plan required
- **Debt**: Technical debt identified, refactoring priority management needed

## 5) Technical Debt Registry (Technical Debt Management)

| ID | Related Asset/Capability | Debt Details (Reason/Risk) | Identified | Resolution Criticality | Resolution Plan/WI |
| :------ | :-------------------- | :---------------------------------------------- | :--------- | :------------ | :-------------- |
| DBT-001 | (Example) CAP-005 | Some XLSX parsing exception handling incomplete during automatic term extraction | 2025-12-31 | Low | WI-20251231-999 |

---

## Related Documents

### Required References

- [Traceability Guide](../guides/traceability.md): WI ↔ ADR ↔ Asset linking rules
- [Operation Process](../guides/operation-process.md): Reuse-first and asset promotion process

### Reference Documents

- [Development Workflow (Lifecycle)](../guides/development-workflow.md): Overall process flow overview
- [System Design](../architecture/system-design.md): Overall design and operational structure
- [Asset Promotion Checklist](../templates/asset-promotion-checklist.md): Template used for asset promotion
