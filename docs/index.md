---
version: 1.1
last_updated: 2026-02-12
project: ATS
owner: EO
category: registry
status: stable
---

# Documentation Index

> Purpose: Provide overall documentation structure overview and "starting points"
> **Single operational flow**: `MA + Subagents + Skills` (details: `guides/development-workflow.md`)

## Document Overview by Category

| Category | Document Count | Index File | Description |
| ---------- | ------- | ------------------------------------------ | -------------------- |
| Architecture | 1 | [Architecture Index](architecture/index.md) | High-level design/principles |
| Design | 2 | [Design Index](design/index.md) | Design reference documents |
| Guides | 11 | [Guides Index](guides/index.md) | Execution/operation guides |
| Policies | 7 | [Policies Index](policies/index.md) | Operational policy documents |
| Standards | 9 | [Standards Index](standards/index.md) | Standard documents |
| Templates | 18 | [Templates Index](templates/index.md) | Document/artifact templates |
| Registry | 4 | [Registry Index](registry/index.md) | Asset/context/project registries |
| ADR | 1 | [ADR (Decision Records)](adr/) | Decision records |
| Analysis | 0 | [Analysis Documents](analysis/) | Domain analysis documents |
| Eval | 0 | [Eval Index](eval/index.md) | Evaluation documents |

**Total Document Count**: Managed based on "Document Count" column above (excluding index files).

## Required Documents Mapping by Role

### MA (Main Agent)

- **Required (starting point)**: [Development Workflow](guides/development-workflow.md)
- **Required (design)**: [System Design](architecture/system-design.md)
- **Required (rules)**: `CLAUDE.md` (root) — Orchestration gates, routing rules, Tier 0, workspace structure
- **Workspace configuration**: `.claude/config/workspace.json` — Domain project routing
- **Operational rules**: [Operation Process](guides/operation-process.md)
- **Policies**: [Policy Documents](policies/)
- **Standards (including Tier 0)**: [Standards Documents](standards/)

### Subagents / Skills

- **Subagents (Source of Truth)**: `.claude/agents/` — Context provided via WI handoff packets
- **Skills (Source of Truth)**: `.claude/skills/`

## Project-specific Document Overview

### PRJ-ATS-001: ATStudio (Shorts Music Marketplace)

| Category | Document | Description |
|----------|----------|-------------|
| Standards | [core-principles.md](standards/core-principles.md) | Section 13: ATStudio Domain Principles |
| Standards | [development-standards.md](standards/development-standards.md) | Section 2A: Java/Spring Boot Coding Standards |
| Standards | [glossary.md](standards/glossary.md) | Section 3-A: ATStudio Domain Terms |
| Policies | [security-policy.md](policies/security-policy.md) | Section 6: JWT/MySQL Secrets Management |
| Registry | [project-registry.md](registry/project-registry.md) | PRJ-ATS-001 registration |
| Config | `.claude/config/workspace.json` | ATStudio routing, tech_stack |

- **Tech Stack**: Java 17, Spring Boot 4.x, MySQL 8.x, Thymeleaf (Phase 1), React (Phase 2)
- **Project Registry**: See [Project Registry](registry/project-registry.md)

## Starting Point Guides

### New Users (First Time)

1. **[Development Workflow](guides/development-workflow.md)**: Work workflow guide — **Understand overall context and step-by-step guide** ⭐
2. **[Request Intake (starting point)](guides/request-intake.md)**: Starting point (how to request) — **"How should I ask?"**
3. **[System Design](architecture/system-design.md)**: System design — **Big picture/principles**
4. **[Project Onboarding Checklist](guides/project-onboarding.md)**: Onboarding (optional) — **When setting up new project**

### Document Authors

1. **[Documentation Standards](standards/documentation-standards.md)**: Documentation writing standards
2. **[Glossary](standards/glossary.md)**: Standard glossary
3. **[Glossary Sources (official)](standards/glossary-sources.md)**: Terminology standard sources

### Agent Developers

1. **[System Design](architecture/system-design.md)**: Overall design
2. **[Base Agent Design](design/base-agent.md)**: Architecture design
3. **[Development Standards](standards/development-standards.md)**: Development standards
4. **[Agent Documentation Map](guides/agent-docs-map.md)**: Required reference documents map by agent
5. **[Agent-facing Documents Guide](guides/agent-facing-docs.md)**: 2-set deliverables/pointer/evidence pack operational rules
6. **[Evidence Pack Standard](standards/evidence-pack-standard.md)**: Evidence pack required fields/reproduction/pointer specification

## Document Scope Classification

Documents are classified by scope for context injection to domain projects.

### Meta-only (Exclude for Domain Projects)

These documents describe meta framework operation. **DO NOT inject for ATStudio domain work** unless explicitly needed:

| Category | Documents | Purpose |
|----------|-----------|---------|
| Architecture | `system-design.md` | MA+Subagent architecture |
| Design | `base-agent.md`, `protocols/agent-communication.md` | Agent design/A2A protocol |
| Guides | `development-workflow.md`, `operation-process.md`, `request-intake.md` | Meta workflow/operation |
| Guides | `traceability.md`, `agent-facing-docs.md` | Meta tracking/docs |
| Guides | `agent-docs-map.md`, `agent-evaluation.md`, `eval-golden-set.md` | Agent routing/evaluation |
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
| Policies | `access-control-policy.md`, `versioning-policy.md` | Access/versioning |
| Guides | `actor-guidelines.md`, `project-onboarding.md`, `runbook-postmortem.md` | Collaboration/operation |
| ADR | All decision records | Architectural decisions |

**Rule**: For ATStudio (`ATS`) domain work, exclude meta-only documents from context injection unless explicitly required.

## Document Update Rules

- Document count by category is managed in category indexes
- Required documents mapping by role is managed in this file
- Project-specific documents refer to project sections in category indexes
- **Document scope classification is managed in this file** (meta-only vs universal)
