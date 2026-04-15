---
version: 1.6
last_updated: 2026-04-15
project: ATS
owner: EO
category: registry
status: stable
---

# Documentation Index

> Purpose: Provide overall documentation structure overview and "starting points"
> **Single operational flow**: `MA + Subagents + Skills` (details: `docs/architecture/system-design.md`, `AGENTS.md`, `CLAUDE.md`)

## Document Overview by Category

| Category | Document Count | Index File | Description |
| ---------- | ------- | ------------------------------------------ | -------------------- |
| Architecture | 1 | [Architecture Index](architecture/index.md) | High-level design/principles |
| Design | 20 | [Design Index](design/index.md) | Meta + ATStudio domain design (API, DB, use cases, protocol references) |
| Policies | 8 | [Policies Index](policies/index.md) | Operational policy documents |
| Standards | 13 | [Standards Index](standards/index.md) | Standard documents and reference assets |
| Templates | 18 | [Templates Index](templates/index.md) | Document/artifact templates |
| Registry | 4 | [Registry Index](registry/index.md) | Asset/context/project registries |
| Audit | 2 | [Audit Index](audit/index.md) | Audit reports and remediation baselines |
| SR | 90 | [SR Index](SR/index.md) | Screen Review items (SR-01~90) |
| Retrospective | 4 | [Retrospective Index](retrospective/index.md) | Engineering lessons, domain design, kick.md, process |
| ADR | 1 | [ADR Index](adr/index.md) | Decision records |
| UI | 3 | [UI Index](ui/index.md) | Screen inventory, flow, and modal planning documents |
| Eval | 0 | [Eval Index](eval/index.md) | Evaluation documents |

**Total Document Count**: Managed based on "Document Count" column above (excluding index files).

## Required Documents Mapping by Role

### MA (Main Agent)

- **Required (design)**: [System Design](architecture/system-design.md)
- **Required (rules)**: `AGENTS.md` (root, primary) — Orchestration gates, routing rules, Tier 0, workspace structure
- **Compatibility**: `CLAUDE.md` (root, transitional) — Legacy Claude-oriented instructions still referenced by older docs/deliverables
- **Policies**: [Policy Documents](policies/)
- **Standards (including Tier 0)**: [Standards Documents](standards/)

### Subagents / Skills

- **Subagents (Source of Truth)**: `.claude/agents/` — Context provided via WI handoff packets
- **Skills (Codex-exposed)**: `.agents/skills/`
- **Skills (Legacy compatibility assets)**: `.claude/skills/`

## Project-specific Document Overview

### PRJ-ATS-001: ATStudio (Shorts Music Marketplace)

| Category | Document | Description |
|----------|----------|-------------|
| Standards | [core-principles.md](standards/core-principles.md) | Section 13: ATStudio Domain Principles |
| Standards | [development-standards.md](standards/development-standards.md) | Section 2A: Java/Spring Boot Coding Standards |
| Standards | [dto-standards.md](standards/dto-standards.md) | Entity/DTO separation, ResponseDTO, RequestDTO |
| Standards | [exception-handling.md](standards/exception-handling.md) | Business/Technic exceptions, GlobalExceptionHandler |
| Standards | [frontend-standards.md](standards/frontend-standards.md) | React + TypeScript architecture (Phase 2 — active) |
| Standards | [glossary.md](standards/glossary.md) | Section 3-A: ATStudio Domain Terms |
| Policies | [security-policy.md](policies/security-policy.md) | Section 6: JWT/MySQL Secrets Management |
| Registry | [project-registry.md](registry/project-registry.md) | PRJ-ATS-001 registration |
| Config | `.claude/config/workspace.json`, `.claude/config/context-injection-rules.json` | ATStudio routing, tech_stack, context injection |

- **Tech Stack**: Java 17, Spring Boot 4.x, MySQL 8.x + React 18, TypeScript 5.6, Vite 6 (Phase 2 — active)
- **Project Stats**: 104 APIs, 28 DB tables, 49 screens, 13 agents
- **Project Registry**: See [Project Registry](registry/project-registry.md)

## Starting Point Guides

### New Users (First Time)

1. **`AGENTS.md`** (root): Current Codex work workflow, orchestration gates, routing — **start here** ⭐
2. **[System Design](architecture/system-design.md)**: Big picture/principles and migration-state architecture
3. **[Kickoff Prompt Template](templates/ma-session-kickoff-prompt.md)**: Copy-paste for new sessions
4. **`CLAUDE.md`** (root): Transitional compatibility reference for older Claude-era docs and deliverables

### Document Authors

1. **[Documentation Standards](standards/documentation-standards.md)**: Documentation writing standards
2. **[Glossary](standards/glossary.md)**: Standard glossary
3. **[Glossary Sources (official)](standards/glossary-sources.md)**: Terminology standard sources

### Agent Developers

1. **[System Design](architecture/system-design.md)**: Overall design (SoT for agent roles)
2. **[Development Standards](standards/development-standards.md)**: Development standards
3. **[Evidence Pack Standard](standards/evidence-pack-standard.md)**: Evidence pack required fields/reproduction/pointer specification
4. **Agent definitions (SoT)**: `.claude/agents/*.md` — 13 agents (ps, eo, sa, se, re, pg, tr, uv, docops, qa, qa-fe, qa-integ, cr)
5. **Codex-exposed skills**: `.agents/skills/*` — Runtime-facing project skills used in current Codex sessions

## Document Scope Classification

Documents are classified by scope for context injection to domain projects.

### Meta-only (Exclude for Domain Projects)

These documents describe meta framework operation. **DO NOT inject for ATStudio domain work** unless explicitly needed:

| Category | Documents | Purpose |
|----------|-----------|---------|
| Architecture | `system-design.md` | MA+Subagent architecture |
| Design | `base-agent.md` (archived), `protocols/agent-communication.md` | Agent design/A2A protocol |
| Standards | `evidence-pack-standard.md`, `evolution-pattern.md` | Meta internal standards |
| Policies | `template-governance.md`, `future-policy-stubs.md` | Meta governance |
| Registry | All (`project-registry.md`, `context-registry.md`, `asset-registry.md`, `workboard.md`) | Meta work management |

### Universal (Apply to All Projects)

These documents apply to all projects including domain projects:

| Category | Documents | Purpose |
|----------|-----------|---------|
| Standards | `core-principles.md`, `development-standards.md`, `documentation-standards.md` | Tier 0 constitution |
| Standards | `prompt-caching-strategy.md`, `glossary.md`, `glossary-sources.md`, `pricing-sources.md` | Standards/references |
| Policies | `security-policy.md`, `execution-policy.md`, `quality-gates.md` | Security/quality |
| Policies | `access-control-policy.md`, `versioning-policy.md`, `archive-policy.md` | Access/versioning/archive lifecycle |
| ADR | All decision records | Architectural decisions |

**Rule**: For ATStudio (`ATS`) domain work, exclude meta-only documents from context injection unless explicitly required.

## Document Update Rules

- Document count by category is managed in category indexes
- Required documents mapping by role is managed in this file
- Project-specific documents refer to project sections in category indexes
- **Document scope classification is managed in this file** (meta-only vs universal)
