---
version: 2.0
last_updated: 2026-01-26
project: system
owner: MA
category: architecture
status: stable
dependencies:
  - path: ../standards/core-principles.md
    reason: System constitution (Tier 0)
  - path: ../guides/development-workflow.md
    reason: Standard execution flow (REQ→WI→iteration→completion/cleanup)
tier: 1
target_agents:
  - sa
  - eo
  - se
  - cr
task_types:
  - architecture
  - implementation
---

# System Design: MA + Subagents + Skills (Cursor-native)

This document does not implement the "agent team" as a separate framework, but instead **assumes Cursor editor's Subagents/Skills features** to ensure solo development operates **quickly, according to principles, and reusably**.

---

## TL;DR

- **MA (Main Agent)** is the user touchpoint and orchestrator. (Intent understanding → REQ creation → WI distribution → iteration control → final reporting/cleanup)
- **Subagents** handle *noisy tasks* and *specialized perspectives* in **isolated contexts**. (exploration/comparison/review/debugging/testing, etc.)
- **Skills** package *frequently repeated procedures* into **standard packages** for immediate invocation by MA/Subagents. (REQ/WI creation, document index updates, assetization, etc.)

---

## 1. Goals (Why) and Design Principles

### Goals
- **Speed**: Solo developer completes "think→instruct→verify→assetize" with minimal round-trips.
- **Consistency**: Suppress principle (constitution) violations, document drift, and duplicate asset creation.
- **Reusability**: Context/outputs created once are directly reused in next requirements.

### Design Principles
- **MA single touchpoint**: Both users and workers exchange "final agreements/decisions/reports" only through MA.
- **Context isolation first**: Output explosions like exploration/logs/bulk summaries are isolated to Subagents.
- **Promote repetition to Skills**: If repeated 3 times (or high cost/error risk), convert procedure to a Skill.
- **Slim documents**: Keep only "definitions/contracts/indexes", discard/archive excessive explanations/duplicate documents.
- **Constitution baseline injection**: All Subagents assume `docs/standards/core-principles.md` as baseline injection (top priority context) at task start.
- **Dual outputs**: All task outputs are managed as **2 sets**: **User-facing** and **Agent-facing**.
- **docs as injection assets**: All documents in `docs/` are not "documents for my eyes only" but context assets for agents to inject and execute.

---

## 2. System Components

### 2.1 MA (Main Agent)
MA is a **unified role** integrating the traditional **planning/coordination/orchestration** functions.

- **Input**: User requirement utterances (original text), existing context (if needed), policies/standards (Tier 0)
- **Core Responsibilities**
  - **Intent identification**: Confirm Why/Scope/DoD/Constraints through questions
  - **REQ creation/approval induction**: Prohibit speculative execution before approval
  - **Work division**: Split confirmed REQ into WIs (execution units) and assign to appropriate Subagents
  - **Skill invocation**: Handle repetitive procedures as skills (or instruct skill invocation)
  - **Iteration control**: Manage "REQ progress/problems/remaining work" through 4~5 loops
  - **Escalation**: Proceed only after user approval for scope/risk/hard-to-revert changes
  - **Session termination/cleanup**: Create output index + reusable context packet before termination notice

### 2.2 Subagents (Cursor Subagents)
Subagents operate in **isolated contexts** and return summarized results to MA.

- **When to use**
  - Exploration/research/alternative comparison (high output noise)
  - Code/design review (specialized perspective needed)
  - Debugging (lots of logs/stack traces)
  - Test execution/failure analysis (lots of output)
- **Core Rules**
  - Subagent final outputs are **renormalized by MA from REQ perspective** before reporting to user.
  - Subagents have "expert opinion/output production" role, not "decision authority" (final judgment is MA's).

### 2.3 Skills (Cursor Agent Skills)
Skills **package repetitive work procedures**. (knowledge + optionally scripts/templates)

- **When to use**
  - Document generation template application like REQ/WI/reports
  - Document index/registry updates
  - Release/assetization/cleanup checklist execution
  - "Always do the same way" operational routines
- **Core Rules**
  - Skills contain "standard procedures executed identically every time".
  - Exploratory/judgment tasks go to Subagents, standardized/repetitive tasks go to Skills.

---

## 2.4 Subagent Candidates (Role List) — "Pre-identified/registered"

Previously identified roles (PS/EO/SA/SE/RE/PG/TR/PE/UV/DocOps) are **not for disposal**, but **Subagent candidates (specialized workers)** that MA invokes as needed.

> Principle: "Maintain roles, but converge user touchpoint/decision authority to MA."

| Role (Subagent candidate) | Purpose (one-line) | Subagent file (SoT) |
|---|---|---|
| **PS** | User utterance → REQ draft | `.claude/agents/ps.md` |
| **EO** | Governance/routing/gates | `.claude/agents/eo.md` |
| **SA** | Architecture/ADR/structure decisions | `.claude/agents/sa.md` |
| **SE** | Implementation/refactoring/patches | `.claude/agents/se.md` |
| **RE** | Testing/independent verification | `.claude/agents/re.md` |
| **PG** | Security/sensitive information check | `.claude/agents/pg.md` |
| **TR** | Technology research/alternative comparison | `.claude/agents/tr.md` |
| **PE** | Context/prompt design support | (Operated as Skill) |
| **UV** | Design system operations/UX gate | `.claude/agents/uv.md` |
| **DocOps** | Documentation operations (injection/drift/index) | `.claude/agents/docops.md` |

---

## 3. File/Structure: What is Source of Truth

### 3.1 Subagents Definition Location
- **Project Subagents**: `.claude/agents/*.md`
  - YAML frontmatter (`name`, `description`, etc.) + prompt body
  - MA invokes as `/name` when needed

### 3.1.1 `.claude/agents` (SoT) vs Reference Documents (optional)

- **SoT (Source of Truth)**: `.claude/agents/*.md`
  - Actual subagent definitions used in invocation/operations
- **Reference (design/reference)**: (optional) Role description documents (external/separate location)
  - Not required for operations, referenced only when needed.

**Migration Rules (Operational Standard):**
1. Roles actually invoked in operations **must have** `.claude/agents/<role>.md` file.
2. Even if role description document (reference) exists, operational norms/behavior follows `.claude/agents/<role>.md`.
3. Role description documents (reference) are not required for operations (not SoT). Storage can be managed in external/separate location.
4. When adding new role:
   - First create `.claude/agents/<role>.md` (operational ready state).
   - If needed, expand "design reference document" in external/separate location.

### 3.2 Skills Definition Location
- **Project Skills**: `.claude/skills/<skill-name>/SKILL.md`
  - May include `scripts/`, `references/`, `assets/` as needed

### 3.3 Role of Documents (`docs/`)
`docs/` is **human-readable documentation** while simultaneously being **context assets (definitions/contracts/indexes)** injected by MA/Subagents/Skills when needed.
However, session-specific outputs like "approval/decision reports" are separated into User-facing outputs like `deliverables/user/`.

- **standards/**: Constitution/standards (absolute reference)
- **architecture/**: System structure/principles (design reference)
- **guides/**: Flow/operations (procedures)
- **policies/**: Prohibitions/permissions/gates (policies)
- **templates/**: Output formats (REQ/WI/ADR, etc.)
- **registry/**: Directory/indexes (assets/contexts/projects)

---

## 4. Standard Execution Model (REQ→WI Loop)

Standard workflow follows `docs/guides/development-workflow.md`.
What this document fixes is only "who owns what, and what units convert to what".

- **Input**: User utterance
- **Output 1**: REQ (before confirmation: draft / after confirmation: locked)
- **Output 2**: WI bundle (Subagent unit execution units)
- **Iteration**: WI execution/reporting → MA integrates from REQ perspective → issue additional WIs if needed
- **Termination**: Final completion report + output index + reusable context packet

---

## 4.1 Dual Output Set Rule (User-facing / Agent-facing)

All Subagents create the following 2 sets when producing results.

- **User-facing**: Minimum information needed for decision-making/approval/next actions
- **Agent-facing**: Detailed context for reproduction/tracking/reuse (rationale, alternatives, failure logs, links/paths)

Recommended format (minimum):
- User-facing: "Summary + risks + alternatives + next action"
- Agent-facing: "Evidence pointers (file/log/reference) + detailed notes + checklist + follow-up WI suggestions"

---

## 4.2 Design System Operations Rules (UV ↔ SE)

- **UV** creates and oversees design system operations.
- **SE** **prioritizes reuse** of design system during implementation (prohibit "create new every time").
- When SE needs UI elements not in design system, immediately **request from UV**,
  ensuring design system is continuously supplemented (prevent drift).

---

## 5. Context/Prompt Engineering (Core Competency)

MA's core skill is "reducing context and maximizing effect".

- **Context Packet Principles**
  - Deliver only "decisions/constraints/interfaces/success criteria", not full source text
  - Reference with links/paths, minimize body text
  - Give Subagents only minimum input needed for that task
- **Noise Offloading**
  - Keep long logs/dumps/exploration results in Subagent context,
  - Elevate only summary (conclusion/rationale/next action) to MA

### 5.1 MA → Subagent "Instruction Packet" IS the Injection

Subagents **always start from fresh context**.
Therefore, "injection" is not separate magic, but **MA including 'instruction packet' in Subagent invocation prompt**.

Instruction packet minimum composition:
- **WI summary**: Purpose/scope/DoD/constraints (including prohibitions)
- **Required input pointers**: Related documents/files/paths (short excerpts if needed)
- **Output contract**: User-facing/Agent-facing 2 sets + storage location/format
- **Traceability requirements**: Change rationale (pointers), test/reproduction procedures, follow-up WI suggestions

Recommended (prevent omissions): **Trigger-based on-demand injection**
- Trigger definition: `.claude/config/context-triggers.json`
- Context bundle generator: `.claude/scripts/context_manager.py`

---

## 6. Cleanup (Document/Source Slimming) Criteria

Documents made unnecessary in this system are not "deleted" but first **classified as candidates by cleanup criteria**.

- **Maintain**: "Contract/reference/index" roles like Tier 0/policies/templates/registries
- **Consolidate**: Guides/duplicate standards with same purpose
- **Archive**: Past design history (learning value but operationally unnecessary)
- **Delete**: Duplicate/drift-causing documents that only harm operations

---

## 7. Next Tasks (Actual Cleanup Required by This Document)

To ensure this document doesn't end with just words, the following must be performed as "cleanup tasks".

- **Confirm Subagents Source of Truth**: Converge role definitions based on `.claude/agents/`
- **Define Skills Basic Set**: Fix repetitive procedures like REQ/WI/session cleanup/registry updates as skills
- **Cleanup Reference Documents**: Classify reference documents not directly used in operations as candidates for consolidation/archive/deletion

