---
version: 2.0
last_updated: 2026-01-15
project: system
owner: SA
category: design
status: archived
archived_date: 2026-02-07
superseded_by: ../architecture/system-design.md
archive_reason: "MCP-era design document. Current system uses Claude Code native tools (Read/Write/Bash/Task) instead of BaseAgent class hierarchy. Preserved for historical design intent reference."
dependencies:
  - path: ../architecture/system-design.md
---

> **ARCHIVE NOTICE**: This document describes the MCP-era agent design pattern using Python class hierarchies and programmatic LLM calls. The current system uses Claude Code's native Subagent/Skill architecture as described in [System Design](../architecture/system-design.md). Preserved for historical reference only.

# Base Agent (Persona & Tool Client)

> **Core Principle**: Agents are not "servers" but combinations of **Persona (document)** and **Tool Client (Python Script)**.

## 1. Architecture Overview

The system uses **Composition-based Python Runtime** to construct agents. Agents do not run as independent processes but are collections of **configuration (Context)** and **tools (Tools)** loaded by runtime.

```mermaid
graph TD
    User[User/Supervisor] -->|Trigger| Runtime[Agent Runtime]
    Runtime -->|Load| Persona[Identity (Markdown)]
    Runtime -->|Load| Clients[Tool Clients (Python)]
    Clients -->|Invoke| Tools[System Tools (File/Git)]
```

### 1.1 Components

1.  **Identity (Context)**: Subagent/persona defined in `.claude/agents/*.md` (SoT).
2.  **Runtime (Executor)**: (Reference concept) In Cursor Subagents-based system, "MA injecting instruction packet on invocation" replaces runtime role.
3.  **Clients (Utilities)**: `.claude/scripts/*.py` (standardized tools/bundle generation scripts, etc.).

### 1.2 Dual-Track Runtime Support (Hybrid)
The system supports two execution modes for cost efficiency.

- **Track A (Direct Execution)**: `Runtime` directly invokes `Tool Client` without LLM (Zero Cost).
- **Track B (Agentic Execution)**: `Runtime` goes through `Persona` + `LLM` to select and invoke tools (Reasoning).

---

## 2. Standard Tool Clients

All agents interact with the system through these standard clients.

### 2.1 FileSystem Client
- **Role**: Safe file read/write.
- **Key Features**:
    - `view_file(path)`: Read file content (Line limit applied).
    - `write_to_file(path, content)`: Create/modify file (Automated backup).
    - `list_dir(path)`: Directory exploration.

### 2.2 Artifact Handoff Client
- **Role**: State transfer between agents.
- **Key Features**:
    - `task_boundary(status)`: Update work status and display in UI.
    - `notify_user(msg)`: Request user intervention (HITL).

### 2.3 Memory Client
- **Role**: Short-term/long-term memory management.
- **Key Features**:
    - `save_snapshot()`: Save current context.
    - `restore_snapshot()`: Work recovery.

---

## 3. Implementation Pattern (Python)

```python
# scripts/core/base_agent.py (Pseudo-code)

class BaseAgent:
    def __init__(self, role: str):
        self.role = role
        self.context = self.load_persona(role)
        self.tools = self.load_tools(role)

    def load_persona(self, role):
        # Load .claude/agents/{role}.md (SoT)
        return read_markdown(f".claude/agents/{role}.md")

    def run(self, user_request):
        # 1. Build context
        system_prompt = self.context + load_tier0_manifest()

        # 2. LLM invocation
        response = llm.chat(system_prompt, user_request, tools=self.tools)

        # 3. Execute tools and feedback results
        # 3. Execute tools and feedback results
        return self.execute_tool(response)

    def run_script(self, script_name, args):
        # Track A: LLM Bypass (Direct Execution)
        return self.tools[script_name].execute(args)
```



---

## 5. Security & Governance

- **Read-Only Default**: All agents start read-only by default.
- **Explicit Write**: Write operations only possible through explicit tool invocation (`write_to_file`), logged.
- **Tier 0 Validation**: Constitution (`core-principles.md`) loading validation required at runtime start.

