---
version: 2.8
last_updated: 2026-09-09
project: system
owner: SA
category: registry
status: stable
dependencies:
  - path: api-spec.md
    reason: Current source API inventory
  - path: db-schema.md
    reason: Current source schema and historical manifest boundary
  - path: runtime-storage-operations.md
    reason: Audio deployment and verification gates
---

# Design Documentation Index

> Purpose: Entry point for design documents (`docs/design/`).
> Principle: Operational norms (SoT) are in `docs/architecture/system-design.md` and `.claude/agents/`. This directory contains **design references**.

## Document List

### System Design (Meta)

| Document | Description | Status |
|---|---|---|
| **[Base Agent Design](base-agent.md)** | Base Agent design (MCP-era Python class concept) — superseded by `.claude/agents/` native Subagents | archived |
| **[Agent Communication Protocol](protocols/agent-communication.md)** | Agent-to-agent communication protocol (A2A) — reference | stable |

### ATStudio Domain Design (PRJ-ATS-001)

| Document | Description | Status |
|---|---|---|
| **[API Specification](api-spec.md)** | REST API spec v30.14 - 155 source method-level mappings / 26 controllers, including ADMIN audio status and retry | source review PASS; bounded TEST application verified in WI028 |
| **[DB Schema](db-schema.md)** | Database schema v24.5 - 43 source tables / 43 JPA entities; retained TEST DB 43 tables / 520 columns after nine Track columns and queue index; fresh-bootstrap manifest UNRECORDED | retained application/schema validation verified in WI028; not fresh-bootstrap proof |
| **[Use Case Index](usecase/index.md)** | Domain use case documents | stable |
| **[Payment Integration Design](payment-integration-design.md)** | TOSS recurring-only V1 with provider-neutral extension interfaces | stable |
| **[Payment Operations Runbook](payment-operations-runbook.md)** | Toss recurring payment reconciliation, strict settlement import, and production incident response | stable |
| **[Remaining Remediation Design (2026-07-16)](remaining-remediation-design-20260716.md)** | Completed REQ-002 remediation design retained in place; current contracts are in API/DB/UI/payment SoT | archived |
| **[Payment Refund, Receipt, Settlement, and Tax Invoice Policy](payment-refund-receipt-settlement-policy.md)** | Implemented refund/receipt/settlement policy; tax invoice boundary remains deferred | stable |
| **[Payment Settlement Import and Reconciliation Design](payment-settlement-import-design.md)** | Implemented DG-067 strict CSV settlement import and bounded reconciliation design | stable |
| **[P0 Release Blocker Remediation Design](p0-release-blocker-remediation-design.md)** | Protected track media, secret-free mail logging, and withdrawal billing-stop contracts | stable |
| **[P1 Payment Integrity Remediation Design](p1-payment-integrity-remediation-design.md)** | Implemented payment command, transaction, refund recovery, reconciliation, lock-order, and MySQL proof contract | stable |
| **[P1 Payment and Database Integrity Design](p1-payment-db-integrity-design.md)** | Superseded 2026-07-14 payment/DB baseline retained for migration cautions and historical reference | archived |
| **[P1 Security and Acceptance Hardening Design](p1-security-acceptance-hardening-design.md)** | Pre-implementation hardening contract retained in place; current controls live in security/API/operations SoT | archived |
| **[Runtime Storage Operations](runtime-storage-operations.md)** | v1.5 - DB and absolute public/private root tuple, audio recovery, WI028 actual TEST evidence and remaining target gates | bounded TEST verification complete with limits; production HOLD |

WI025 directly recounted 79 GET + 42 POST + 20 PUT + 14 DELETE = **155**;
PATCH is zero and TrackController has 12 mappings. The prior 150-entry index
was stale before the two new ADMIN routes, so adding two to that old total is
not the current count. See [current API verification](api-spec.md#verification).
The [pre-feature 43-table/511-column manifest and hash](db-schema.md#current-source-and-mysql-verification-boundary)
are retained as historical observations, not rewritten as a current schema PASS.
The [WI026 source review](../../deliverables/agent/WI-20260909-ATS-026-evidence-pack.md)
and [WI028 actual retained-runtime verification](../../deliverables/agent/WI-20260909-ATS-028-evidence-pack.md)
are complete within their separate scopes. See [audio rollout gates](runtime-storage-operations.md#audio-processing-rollout-gates)
for 22 verified media pairs, MA-supplied UI coverage, browser OS-save UNKNOWN,
ten historical missing references with strict startup false, and production HOLD.
