---
version: 1.0
last_updated: 2026-01-06
project: system
owner: SA
category: design
status: stable
dependencies:
  - path: ../../architecture/system-design.md
    reason: Reference for overall collaboration system
---

# Agent-to-Agent Communication Protocol (A2A Protocol)

> Purpose: Define communication conventions for efficient and safe collaboration between agents and optimize context

---

## 1. Overview

**A2A (Agent-to-Agent) Protocol** is a standard convention for achieving context isolation and token reduction in inter-agent communication. Each agent has an independent context and operates efficiently even in large-scale systems by transmitting only final results to other agents.

---

## 2. Core Principles

1. **Context Isolation**: Each agent includes only its own tools in context, and returns only final results after task completion.
2. **Agent Schema Unnecessary**: Inter-agent communication is performed through direct message/Artifact delivery, not tool invocation. By not including detailed tool schemas of each agent in other agents' contexts, tokens are dramatically reduced.
3. **Task Delegation**: Main Agent (MA) does not need the entire tool list, but forwards requests to sub-agents capable of performing specific tasks.

---

## 3. Agent Cards

When MA selects agents for collaboration, it uses lightweight metadata **Agent Cards** instead of full schemas.

### 3.1 Structure (Implemented as Markdown)
- **Location (SoT):** `.claude/agents/*.md`
- **Composition:**
    - `Role`: Role definition
    - `Responsibilities`: Key responsibilities
    - `Tools`: Allowed tool list

### 3.2 Effects
- **Context Injection:** By injecting only minimum context as "instruction packet (context packet)" when needed, unnecessary agent information does not occupy context.
- **Explicit Definition:** Defined as documents rather than code, allowing even users (non-developers) to modify roles.

---

## 4. State Exchange (State Exchange via Artifacts)

### 4.1 Asynchronous Handoff
Instead of direct messaging between agents, exchange state through **files (Artifacts)**.

- **Request:** Update `task.md` or `implementation_plan.md`.
- **Context:** Load required documents (`Tier 0`, `Tier 2`).
- **Response:** Generate outputs (`write_to_file`) and invoke `notify_user`.

### 4.2 Handoff Protocol
1. **Agent A:** Write results to `docs/work-items/` or temporary file.
2. **Supervisor/User:** After confirming task completion, load Agent B's persona (`view_file`).
3. **Agent B:** Read documents left by previous agent as context and perform task.

---

## 5. Protocol Comparison (Tool vs Coordination)

| Category | Tool Protocol (Tool Layer) | A2A (Coordination Layer) |
| :--- | :--- | :--- |
| **Target** | Tools (FileSystem, Git) | Agents (MA, SE) |
| **Method** | Standardized JSON-RPC | File-based Context Handoff |
| **Advantages** | Compatibility, Reusability | Token efficiency, State preservation |

---

## Related Documents
- [System Design](../../architecture/system-design.md): Overall collaboration system and architecture
- [BaseAgent Design](../base-agent.md) (archived): Historical MCP-era design reference
