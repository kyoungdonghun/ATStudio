---
version: 1.1
last_updated: 2026-01-29
project: system
owner: DocOps
category: guide
audience: both
keywords: ["agent-facing", "evidence-pack", "traceability", "injection", "handoff", "WI", "REQ"]
dependencies:
  - path: ../standards/core-principles.md
    reason: Tier 0 constitution (approval/speculative execution prohibition, transparency)
  - path: ../standards/documentation-standards.md
    reason: Document metadata/structure standards
  - path: ../standards/glossary.md
    reason: Terminology (WI/REQ etc.) consistency
  - path: ../templates/wi-subagent-handoff-template.md
    reason: Subagent instruction packet (=injection) standard template
  - path: ../standards/evidence-pack-standard.md
    reason: Evidence Pack path/required field standards
status: stable
---

# Agent-facing Document Set (Definition) — "Detailed Tracking Possible with Summary Only"

> Purpose: Fix definition/purpose/composition/operation rules of "Agent-facing document set" so that when MA calls/verifies/reuses Subagents, **reading summary only** brings along justification, pointers, and reproduction.

---

## 1) Definition

- **Agent-facing Document Set**: Sum of **agent-friendly document bundle** + (per-task) **justification/reproduction bundle (Evidence Pack)** that agents (MA + Subagents) reference to "do better work"
  - Core: (A) **Injection-optimized context assets** (guides/policies/standards/checklists/rules) + (B) **Per-task tracking/reproduction** (Evidence Pack)
- **User-facing Deliverables**: Minimum information needed for user approval/decisions (summary/impact/risks/options)

> Rule: Always create "User-facing 1 + Agent-facing 1 (=Evidence Pack)" as **pair** for same task (WI).

---

## 2) Contents (Minimum Composition)

Task unit is **WI**. Each WI has minimum of below.

### 2.1 WI Document (Task Contract)

- Location: `docs/work-items/WI-YYYYMMDD-###-*.md`
- Role: Fix purpose/scope/DoD/constraints/input pointers/deliverables contract.
- Required: Include **Evidence Pack pointer** (path)

### 2.2 Evidence Pack (Agent-facing Core)

- Location (standard): `deliverables/agent/<WI-ID>-evidence-pack.md`
- Role: Fix **justification/reproduction/results** that satisfy "detailed tracking possible with summary only".
- Standard: Follow `standards/evidence-pack-standard.md`.

### 2.3 Workboard Entry (Summary Index)

- Location: `docs/registry/workboard.md`
- Role: Show only "how far we've come" (details link to WI/Evidence Pack)
- Required: Write path in **Evidence Pack** column of WI row.

---

## 3) Operation Rules (Non-negotiables)

- **Task Tracking Unit**: WI
- **Prohibition of Speculative Execution Before REQ Approval**
  - Mark unclear parts as **assumptions/needs decision**, perform only "progressable scope".
- **Pointer-First**
  - Rather than attaching long excerpts/logs to document body, connect with file path/line range/reproduction command.
- **Include Reproduction Info**
  - If possible: reproduction command + result (summary/key log)
  - If not possible: why not possible + alternative verification method

---

## 4) What to Enforce in MA → Subagent Instruction Packet (=Injection) (Checklist)

When calling Subagent, MA **fixes as packet** below.

- [ ] **WI Summary**: Why / Scope (in/out) / DoD / Constraints (including prohibitions)
- [ ] **Input Pointers**: Related document/file paths (minimal excerpt if needed)
- [ ] **Deliverables Contract (2 sets)**:
  - User-facing: `deliverables/user/...`
  - Agent-facing (Evidence Pack): `deliverables/agent/<WI-ID>-evidence-pack.md`
- [ ] **Evidence Requirements**: Changed file/line/command/test result pointers

---

## 5) Directory/SoT (Single Standard)

| Category | Purpose | Single Standard (SoT) |
|---|---|---|
| `docs/` | Documentation (single structure) | All rules/policies/guides |
| `deliverables/user/` | User approval/decision (per session) | User-facing deliverables |
| `deliverables/agent/` | Justification/reproduction/verification (per session) | Evidence Pack and auxiliary logs |

### SoT (Single Standard) Note

- SoT doesn't mean "eliminate duplication", but **even if duplication occurs, specify where the final standard is**.
- docs/ is the sole documentation SoT.
