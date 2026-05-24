---
version: 1.3
last_updated: 2026-05-24
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
| **[API Specification](api-spec.md)** | REST API spec v11 — 121 endpoints | stable |
| **[DB Schema](db-schema.md)** | Database schema — 31 tables | stable |
| **[Use Case Index](usecase/index.md)** | Domain use case documents | stable |
| **[Payment Integration Design](payment-integration-design.md)** | Recurring-first subscription payment, legacy/mock payment compatibility, and provider architecture | draft |
| **[Payment Operations Runbook](payment-operations-runbook.md)** | Toss recurring payment reconciliation and production incident response | draft |

