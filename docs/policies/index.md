---
version: 1.1
last_updated: 2026-01-06
project: system
owner: EO
category: registry
status: stable
---

# Policy Documentation Index

> Purpose: Detailed list of operational policy documents

## Document List

| Document Name | Project | Description | Status |
|---------------|---------|-------------|--------|
| [Execution Policy (HITL)](execution-policy.md) | system | Execution policy (HITL): Explanation + approval before command/tool execution | stable |
| [Quality Gates](quality-gates.md) | system | Quality gates (operational checklist) | stable |
| [Versioning/Release/Deprecation Rules](versioning-policy.md) | system | Versioning/release/deprecation policy | stable |
| [Template Governance](template-governance.md) | system | Template governance policy | stable |
| [Secrets/Sensitive Info Policy](security-policy.md) | system | Secrets/sensitive information policy | stable |
| [Access Control Policy](access-control-policy.md) | system | Access control policy | stable |
| [Phase 2+ Reserved Policies (Stubs)](future-policy-stubs.md) | system | Backup/DR, log retention, observability, release, remote request definitions (Phase 2+ implementation reserved) | draft |

## Document Dependencies

### Execution and Approval Policies

- **execution-policy.md**: HITL approval policy (approval matrix, approval levels)
  - Core policy: Baseline for all execution policies
- **access-control-policy.md**: Permission classification definition (Read-only, Write, Destructive, Network)
  - HITL approval details: refer to execution-policy.md

### Quality and Governance

- **quality-gates.md**: Quality gates by Criticality
  - Related: execution-policy.md, operation-process.md
- **template-governance.md**: Template governance policy
  - Related: asset-registry.md
- **versioning-policy.md**: Versioning/release/deprecation policy
  - Related: quality-gates.md

### Security and Privacy

- **security-policy.md**: Secrets/sensitive information policy

### Phase 2+ Reserved Policies

- **future-policy-stubs.md**: Backup/DR, log retention, observability/SLO, release/deployment, remote request definitions fixed only
  - Each section expected to grow into independent policy in Phase 2+

