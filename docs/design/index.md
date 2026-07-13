---
version: 1.9
last_updated: 2026-07-13
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
| **[API Specification](api-spec.md)** | REST API spec v19 — 147 endpoints | stable |
| **[DB Schema](db-schema.md)** | Database schema v14 — 39 tables | stable |
| **[Use Case Index](usecase/index.md)** | Domain use case documents | stable |
| **[Payment Integration Design](payment-integration-design.md)** | Recurring-first subscription payment, legacy/mock payment compatibility, and provider architecture | draft |
| **[Payment Operations Runbook](payment-operations-runbook.md)** | Toss recurring payment reconciliation and production incident response | draft |
| **[Payment Refund, Receipt, Settlement, and Tax Invoice Policy](payment-refund-receipt-settlement-policy.md)** | Payment operations policy for refund, receipt, settlement, tax invoice, and future admin mutation boundaries | draft |
| **[Payment Settlement Import and Reconciliation Design](payment-settlement-import-design.md)** | Settlement source adapter, CSV-first import, reconciliation, and admin operation design | draft |
| **[P0 Release Blocker Remediation Design](p0-release-blocker-remediation-design.md)** | Protected track media, secret-free mail logging, and withdrawal billing-stop contracts | stable |
