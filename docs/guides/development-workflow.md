---
version: 3.1
last_updated: 2026-01-29
project: system
owner: MA
category: guide
status: stable
dependencies:
  - path: ../standards/core-principles.md
    reason: Constitution (Tier 0) injection
  - path: ../architecture/system-design.md
    reason: MA/Subagents/Skills system definition
  - path: ../../deliverables/user/REQ-20260129-002-concurrency-control.md
    reason: Concurrency control detailed specification
tier: 2
target_agents:
  - se
  - re
  - docops
  - qa
task_types:
  - implementation
  - testing
---

# Development Workflow (MA + Subagents + Skills)

This document defines only the **standard flow** for running solo development quickly and principled with **User ↔ MA (Main Agent) ↔ (Subagents/Skills)** structure.
Comparisons/mappings, excessive step decomposition, "document calling document" structures are intentionally removed.

---

## 0. Minimum Terms

- **MA (Main Agent)**: User contact + orchestrator. Handles intent understanding, context/prompt engineering, Subagent delegation, skill calling, final reporting/approval.
- **Subagents (Workers)**: Agents specialized in specific roles/perspectives. MA calls in parallel as needed.
- **Skills**: Packaging frequently repeated tasks as "standard procedures". Called at appropriate times by MA/Subagents.
- **REQ (Requirements Definition)**: Document that confirms "what/why/what success criteria is".
- **WI (Work Item)**: **Execution unit** specifying "who/what/input·output·completion criteria". (Official term)

---

## 1. Standard Flow (1~7)

### 1) User Requirements Utterance
- User speaks requirements/problems/goals in natural language.
- **Output**: User utterance (original text)

### 2) MA Intake → Intent Understanding → REQ Draft Creation (Skill) → Request User Review
MA does not start with "direct execution (implementation/document modification/command execution)" but first confirms **intent (Why)·scope (Scope)·success condition (DoD)·constraints (Constraints)**.

- **Output (Required)**: REQ draft + open questions (if needed)
- **Recommended Skill**: `/create-req` (user utterance → REQ draft normalization)
- **Assistant Skill (Recommended)**: `/ce` (Context Engineering: minimal pointer/on-demand injection design)
- **REQ Minimum Inclusions (Recommended)**:
  - Goal/background (Why)
  - Scope (included/excluded)
  - Success criteria (including test/verification)
  - Constraints (technical/schedule/security/cost)
  - Risks/alternatives (if any)

### 3) User REQ Approval
- "Proceed with speculation" prohibited before approval.
- Once approved, REQ becomes **confirmed (locked)** state.
- **Output**: Approved REQ (confirmed version)

### 4) MA Task Division → WI Creation (Skill) → WI Handoff (Instruction) → Task Execution
- Workers mean "actors needed throughout from planning to asset conversion".
- MA must **pre-register predicted workers (Subagents)** and **pre-register frequently used skills (Skills)**.
- MA specifies below for each WI:
  - **Assigned Subagent**
  - **Input context (minimal)**
  - **Deliverables location/format**
  - **Completion condition (DoD)**
  - **Skills to use if needed**

- **Recommended Skill (Handoff Packet Standardization)**: `/create-wi-handoff-packet`
- **Assistant Skill (Recommended)**: `/pe` (Prompt Engineering: strengthen instruction packet for Subagent consumption)
- **Recommended Skill (Verification/Justification Standardization)**: `/create-wi-evidence-pack`
- **Important Gate**:
  - If no WI (no "instruction packet" to transfer to Subagent), **do not start Subagent execution**.
  - Subagents do not communicate directly with each other, **MA instructs with WI packet and collects results**.
- **Output**:
  - WI bundle (divided execution units)
  - Subagent call "instruction packet" (WI handoff message)

### 5) Worker Completion Report → MA Receipt/Normalization
- Subagents report to MA including deliverables + change summary + risks/follow-up suggestions.
- MA renormalizes report from **REQ perspective** for next judgment.
- **Output**: Completion report packet (change summary/justification/risks/follow-up)

### 6) Repeat 4~5 Until Requirements Complete + Immediate User Approval for Important Concerns
- MA updates "progress vs REQ/remaining work/concerns" each iteration.
- **Important concerns (scope change, security/data risks, schedule/cost spikes, hard-to-reverse changes)** do not proceed without user approval.
- **Output**: Iteration unit progress report (brief)

### 7) MA Final Completion Report + Deliverables/Context Organization + Session End Notification (Reusable Form)
- MA reports final results to user,
- Organizes deliverables and core context created during REQ execution as "reusable form".
- Finally clearly notifies user "this session can be ended".
- **Output (Required)**:
  - Final completion report (including REQ success criteria satisfaction)
  - Deliverables index (what is where)
  - Reuse context packet (summary/links to load directly in next requirement)

---

## 2. Concurrency Control (Multi-session Work)

Guide for preventing conflicts when working simultaneously in multiple Claude sessions.

### 2.1 Session Management

**Session Start**:
```bash
python3 .claude/scripts/session_manager.py start --req REQ-20260129-002
```
- Automatically creates Git branch (`session/SES-{random}`)
- Saves session metadata (`.claude/sessions/`)
- Returns session ID (e.g., `SES-20260129-1432-a7f3`)

**Check Current Active Sessions**:
```bash
python3 .claude/scripts/session_manager.py active
```

**Generate WI-ID** (session-specific unique ID):
```bash
python3 .claude/scripts/session_manager.py wi <SESSION_ID>
```
- Format: `WI-YYYYMMDD-SES-{random}-001`
- Counter independent per session

**Session End**:
```bash
python3 .claude/scripts/session_manager.py end <SESSION_ID>
```
- Automatic lock cleanup
- Return to original branch
- Can auto-merge with `--auto-merge` option

### 2.2 File Locking (Optional)

Can acquire lock before modifying important files to prevent conflicts.

**Acquire Lock**:
```bash
python3 .claude/scripts/lock_manager.py acquire <FILE_PATH> <SESSION_ID>
```

**Release Lock**:
```bash
python3 .claude/scripts/lock_manager.py release <FILE_PATH> <SESSION_ID>
```

**Check Conflicts**:
```bash
python3 .claude/scripts/lock_manager.py check
```

### 2.3 Git Branch Strategy

- **Independent branch per session**: `session/SES-{random}`
- **Merge after task completion**: Merge to `main` branch
- **Commit Message**: Recommended to include WI-ID (`WI-YYYYMMDD-SES-{random}-001: ...`)

**Reference**: See `deliverables/user/REQ-20260129-002-concurrency-control.md` for details

---

## 3. Operational Principles (Minimum)

- **MA's role is to reduce communication costs**: Questions short and core only, deliverables in standard format.
- **Subagents separate noisy work**: Offload exploration/alternative comparison/bulk summary/log analysis to Subagents.
- **Repeated tasks elevated to Skills**: Treat as skill candidate if repeated 3 times (or if costly).

### Workspace Structure (Meta + Domain)

- **Meta Framework**: This workspace (general orchestration)
  - 9 general Subagents: ps, eo, sa, se, re, pg, tr, uv, docops
  - 6 general Skills: /create-req, /create-wi-handoff-packet, /create-wi-evidence-pack, /ce, /pe, /ma-session-kickoff
  - Tier 0 principles, orchestration gates, routing rules (CLAUDE.md)

- **Domain Projects**: `projects/` directory
  - Add domain-specific agents/skills (e.g., design-system, api-platform)
  - Automatically inherit meta rules (can use both meta + domain tools)
  - Routing: EO agent reads `.claude/config/workspace.json` and decides project based on keywords

- **Execution**: Users always work in meta framework, domain context loaded as "addition" (not replacement)

---

## 4. Next Documents (Minimum Links)

- `docs/standards/core-principles.md` (constitution, Tier 0)
- `docs/architecture/system-design.md` (MA/Subagents/Skills design principles)
- `deliverables/user/REQ-20260129-002-concurrency-control.md` (concurrency control details)

---

## 5. Workflow Compliance Check (Quick Check)

Before calling Subagent, MA checks below by itself.

- [ ] **Is REQ in user-approved (locked) state?** (if not, execution prohibited)
- [ ] **Does WI exist as document**, and are assignee/input/deliverables/DoD specified?
- [ ] **Is WI handoff packet** prepared? (can copy-paste directly to transfer to Subagent)
- [ ] **Are deliverables 2-set paths** fixed?
  - User-facing: `deliverables/user/...`
  - Agent-facing: `deliverables/agent/<WI-ID>-evidence-pack.md`
- [ ] (If needed) **Enforced standard format with skill use**? (`/create-req`, `/create-wi-handoff-packet`, `/create-wi-evidence-pack`)
