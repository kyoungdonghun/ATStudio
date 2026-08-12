---
version: 2.6
last_updated: 2026-08-13
project: system
owner: SA
category: registry
status: stable
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
| **[API Specification](api-spec.md)** | REST API spec v30.0 - 149 current method-level mappings | confirmed |
| **[DB Schema](db-schema.md)** | Database schema v24.0 - 42 source tables / 42 JPA entities; current 42/506/173/90/6 MySQL manifest recorded and DG-067-09B `RUN-PASS-CLEANED` | confirmed |
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
