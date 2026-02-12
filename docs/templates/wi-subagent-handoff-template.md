---
version: 1.0
last_updated: 2026-01-29
project: system
owner: MA
category: template
audience: both
keywords: ["WI", "work item", "handoff", "subagent", "instruction packet", "injection", "traceability", "evidence"]
dependencies:
  - path: ../standards/core-principles.md
    reason: Tier 0 constitution (base injection/violation prohibited)
  - path: ../standards/glossary.md
    reason: Term (WI/REQ etc.) consistency
  - path: ../guides/agent-facing-docs.md
    reason: Agent-facing document set (2-set deliverables) definition
  - path: ../standards/evidence-pack-standard.md
    reason: Evidence Pack location/required fields/reproduction specification
  - path: ../guides/agent-docs-map.md
    reason: Routing/instruction packet standard
status: stable
---

# WI Template: Subagent Instruction Packet (=injection) Standard

> Purpose: WI template for MA to deliver context to Subagent **in always same format** when calling (=injection).
>
> Key: Subagent starts with new context.
> Therefore "injection" means **including this instruction packet in Subagent call prompt**.

---

## WI Header

- **WI ID**: `WI-YYYYMMDD-###`
- **Related REQ**: `REQ-...` (if none, `TBD`)
- **Owner(MA)**: `MA`
- **Assignee(Subagent)**: `ps|eo|sa|se|re|pg|tr|uv|docops`
- **Criticality**: `HIGH|MEDIUM|LOW`
- **Status**: `draft|in_progress|blocked|done`

---

## 1) WI Summary (minimum)

- **Purpose (Why)**:
- **Scope**:
  - Included:
  - Excluded:
- **DoD (success criteria)**:
- **Constraints / Forbidden**:
  - (e.g., use only WI terms, sensitive info/secrets prohibited, approval conditions, etc.)

---

## 2) Input Context (pointer-centric)

> Principle: "Full original text injection prohibited". Links/paths first priority, excerpts minimal.

### 2.1 Required Documents (pointers)

- Tier 0 (assume base injection):
  - `docs/standards/core-principles.md`
  - `docs/standards/development-standards.md`
  - `docs/policies/security-policy.md`
  - `docs/standards/documentation-standards.md`
  - `docs/architecture/system-design.md`
- Work-related (add only needed):
  - `docs/...`

### 2.2 File/Code Pointers

- Files:
  - `path:line-range` or "file path + related function/class name"
- Related logs/output:
  - `path/to/log` or "reproduction command"

### 2.3 (Optional) On-demand Injection Bundle

- Trigger rules: `.claude/config/context-triggers.json`
- Bundle generator: `.claude/scripts/context_manager.py`

Example:

```text
python3 .claude/scripts/context_manager.py --text "<WI summary sentence>" --include-tier0
```

> Include generated bundle **as-is in Subagent call prompt**.

---

## 3) Output Contract — 2 sets mandatory

### 3.1 User-facing (for approval/decision, brief)

- **Content**: change summary / impact / risks / approval points (if any)
- **Format**: Markdown
- **Save location (recommended)**:
  - `deliverables/user/<WI-ID>-summary.md`

### 3.2 Agent-facing (for tracking/reuse, pointer-centric)

- **Content**:
  - Evidence pointers (file/line/command/log)
  - Detailed changes (patch summary)
  - Reproduction/test procedures (if possible)
  - Follow-up WI suggestions (if any)
- **Format**: Markdown (+ log/patch files if needed)
- **Standard**: `docs/standards/evidence-pack-standard.md`
- **Save location (recommended)**:
  - `deliverables/agent/<WI-ID>-evidence-pack.md`

---

## 4) Evidence/Traceability Requirements

- **Evidence pointers required**:
  - Changed file paths
  - Key line ranges (if possible)
  - Executed commands/output (if possible)
- **Test/validation**:
  - Execution command + result summary (pass/fail, failure cause)
- **Rollback/risk** (if applicable):
  - How to revert (minimum)

---

## 5) Final "call message" to deliver to Subagent (for copy-paste)

> Copy block below as-is and use in Subagent call prompt.

```text
[WI HEADER]
WI ID: ...
REQ: ...
Assignee(Subagent): ...
Criticality: ...

[WI SUMMARY]
Why:
Scope(in/out):
DoD:
Constraints/Forbidden:

[INPUT POINTERS]
Docs:
Files:
Repro/Logs:

[OUTPUT CONTRACT]
User-facing -> deliverables/user/... :
Agent-facing -> deliverables/agent/... :

[TRACEABILITY REQUIREMENTS]
Evidence pointers (files/lines/commands/logs):
Tests:

[OPTIONAL: ON-DEMAND DOC BUNDLE]
<context_manager.py output paste here>
```
