---
version: 1.1
last_updated: 2026-01-06
project: system
owner: EO
category: guide
status: stable
dependencies:
  - path: ../architecture/system-design.md
    reason: Overall design reference
  - path: ../standards/glossary.md
    reason: Standard terminology usage criteria
---

# Agent Documentation Map

> **@fileoverview**: Agent-specific documentation and reference guide map
> **Created**: 2026-01-06
> **Purpose**: Provide a documentation map for each agent to quickly find needed documents.
>
> This document is a **routing map** for MA to quickly decide "which Subagent to call in what situation".
> The source of truth for Subagent definitions is `.claude/agents/`, and this document organizes call criteria and "required input documents".

---

## 1. MA → Subagents Routing Map (Table)

> Rule: Subagents separate noise (exploration/logs/bulk output), and MA only sees **summary + justification pointers**.

| Task Type/Trigger | Subagent MA Calls | Required Input (Documents) | Expected Output (Summary) |
|---|---|---|---|
| **User Utterance Received** (requirements/problem/goal) | `ps` | `docs/standards/core-principles.md` | REQ draft + open questions + justification links |
| **Task Breakdown After REQ Confirmation** | (MA + Skills) | `docs/guides/development-workflow.md`, `docs/guides/operation-process.md`, `.claude/skills/create-wi-handoff-packet/SKILL.md` | WI list (with dependencies/priorities) + work instruction packet |
| **Project/Context Routing** (which project/gate?) | `eo` | `docs/standards/core-principles.md`, `docs/policies/*` | Routing decision + gate/risks |
| **Technical Alternative Comparison/Research** | `tr` | (minimal needed) Related REQ/WI summary | Alternative comparison table + recommendation + justification links |
| **Architecture Decision/ADR Needed** | `sa` | Related REQ/WI, `docs/templates/adr-template.md` | Design proposal + ADR draft + risks |
| **Implementation/Refactoring** | `se` | Related WI, `docs/standards/development-standards.md` | Change summary + test/risks + justification pointers |
| **Testing/Independent Verification** | `re` | Related WI, `docs/guides/eval-golden-set.md` | Test result summary + failure cause/actions |
| **Security/Sensitive Information Check** | `pg` | `docs/policies/security-policy.md` | Risk summary + block/mitigation plan |
| **Design System Enhancement/UX Gate** | `uv` | Related WI, design system status (if exists) | Design system change proposal + application guide |
| **Prompt/Context Optimization** | (Skill) | Related REQ/WI summary | Context packet improvement plan (performed via skill) |
| **Document Drift/Duplication/Index Breakage** | `docops` | `docs/standards/documentation-standards.md`, `docs/standards/glossary.md` | Drift report + fix plan (per file) |

> Agent-facing deliverables/evidence pack specification (SoT):
> - `docs/guides/agent-facing-docs.md`
> - `docs/standards/evidence-pack-standard.md`

---

## 2. MA → Subagent Instruction Packet (Context Injection) Standard

> Core: Subagents **always start in new context**.
> So "injection" is not the act of viewing a document mapping table, but **MA includes minimal context as 'packet' in Subagent call prompt**.

### 2.1 Packet Composition (Minimal)

- **WI Summary**: Purpose/scope/DoD (success criteria)/constraints (including prohibitions)
- **Input Context (Minimal)**:
  - Tier 0 considered "default injection" (especially constitution/standards/security)
  - Document/file paths (links/pointers) needed for this task
  - "Excerpts" briefly if needed (full text prohibited)
- **Deliverables Contract**:
  - Output file path/format (where to leave what)
  - **User-facing / Agent-facing 2 sets**
  - Agent-facing (evidence pack) standard: `docs/standards/evidence-pack-standard.md`
- **Evidence (Traceability) Requirements**:
  - Change justification (file path/line/command/log) pointers
  - Reproduction procedure/test results (if possible)
  - Evidence Pack pointer (path): `deliverables/agent/<WI-ID>-evidence-pack.md`

### 2.2 On-demand Injection (Recommended: Trigger-based)

If document mapping is "all managed by hand", it eventually gets omitted.
Therefore, MA includes documents in packet **only when needed** with the following devices:

- Trigger definition: `.claude/config/context-triggers.json`
- Bundle generator: `.claude/scripts/context_manager.py`

Example (optional):

```text
python3 .claude/scripts/context_manager.py --text "<WI/REQ summary sentence>" --include-tier0
```

> This output is not "auto-injection", but **context bundle generation to attach to Subagent call prompt**.

---

## 3. Subagents Source of Truth (Files)

| Subagent | File | Status |
|---|---|---|
| `ps` | `.claude/agents/ps.md` | ✅ |
| `eo` | `.claude/agents/eo.md` | ✅ |
| `sa` | `.claude/agents/sa.md` | ⏳ |
| `se` | `.claude/agents/se.md` | ⏳ |
| `re` | `.claude/agents/re.md` | ⏳ |
| `pg` | `.claude/agents/pg.md` | ⏳ |
| `tr` | `.claude/agents/tr.md` | ⏳ |
| `uv` | `.claude/agents/uv.md` | ⏳ |
| `docops` | `.claude/agents/docops.md` | ✅ |

> Skills (SoT):
> - `/create-req`: `.claude/skills/create-req/SKILL.md`
> - `/create-wi-handoff-packet`: `.claude/skills/create-wi-handoff-packet/SKILL.md`
> - `/create-wi-evidence-pack`: `.claude/skills/create-wi-evidence-pack/SKILL.md`
> - `/ce`: `.claude/skills/ce/SKILL.md`
> - `/pe`: `.claude/skills/pe/SKILL.md`
