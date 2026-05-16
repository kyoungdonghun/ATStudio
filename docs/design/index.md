---
version: 1.1
last_updated: 2026-04-04
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
| **[API Specification](api-spec.md)** | REST API spec v7 — 101 endpoints | stable |
| **[DB Schema](db-schema.md)** | Database schema v5 — 28 tables | stable |
| **[Use Case Index](usecase/index.md)** | Domain use case documents | stable |
| **[Payment Integration Design](payment-integration-design.md)** | Mock, Toss one-time payment, and recurring billing provider architecture | draft |

