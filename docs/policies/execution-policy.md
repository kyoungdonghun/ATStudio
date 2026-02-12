---
version: 1.0
last_updated: 2026-01-06
project: system
owner: MA
category: policy
status: stable
dependencies:
  - path: ../standards/glossary.md
    reason: Standard terminology usage baseline
tier: 1
target_agents:
  - "*"
task_types:
  - security
  - implementation
---

# Execution Policy (HITL): Explanation + Approval Before Command/Tool Execution

> Purpose: Ensure users can understand and approve what **complex executions (commands/scripts/bulk processing)** will do before agents execute them.

## 1) Basic Principles

- **Explain Then Execute**: Explain "why/what/how/risks/rollback" before executing tools/commands.
- **Default = Ask**: When in doubt, ask first.
- **Non-destructive First**: Start with read/analysis (read-only), get approval for write/changes later.

> **Note: Distinction from Business Approval (Milestone)**
> This document addresses the safety of "command execution." For approving project phase direction (planning/design/implementation), follow **Milestone Gates (M1, M2, M3)** in [operation-process.md](../guides/operation-process.md).

### 1.1 Pre-flight Check (Required Before Script/Tool Execution)
Perform **integrity checks** before all script or tool execution to prevent failures.

- **Dependency Identification (Static Check)**: Identify configuration files, environment variables, and libraries referenced in the code.
- **Existence Verification**: Verify that identified dependencies (files, etc.) **actually exist on disk** using commands (`ls`, etc.).
  - **If missing?** Do not execute, **immediately pause** and report or find a solution (file creation).
  - **Strictly Prohibited**: Executing blindly assuming "it should be there" and causing crashes.
- **Dry Run**: For destructive operations, check impact with `--dry-run` first when possible.

### 1.2 Hybrid Execution Strategy (Dual-Track)
For cost and efficiency, we operate execution in two distinct tracks. (2026-01-22 strategy)

1. **Track A (Script - Zero Cost)**:
   - **Target**: Status updates, linting, formatting, simple replacements.
   - **Policy**: Delegate all pattern-based tasks to Python scripts to achieve **zero cost**.
2. **Track B (Agent - Paid)**:
   - **Target**: Judgment, reasoning, unstructured data processing.
   - **Policy**: Use LLM tools only when human intelligence is required.

## 2) When Confirmation is Required (Recommended)

Get user approval first if any of the following apply.

### 2.1 Approval Levels — Criteria to Avoid "Confirming Everything"

Approval levels are divided based on the nature and importance of the execution.

- **A0 (No-Approval)**: No approval needed (basically read-only / documentary proposals/drafts)
- **A1 (Notify-Only)**: **Post-notification** only after execution (or when PR is created)
- **A2 (User Approval)**: **User approval (HITL)** required before execution
- **A3 (Dual Control)**: **User approval + 2-person review (e.g., PG/RE)** required before execution (HIGH + risky execution)

> Principle: To avoid exhausting users, **A2/A3 are used only for "genuinely risky executions"**.

### 2.2 Approval Matrix (Recommended Defaults)

| Execution/Action Type                                  | LOW | MEDIUM | HIGH |
| :----------------------------------------------------- | :-- | :----- | :--- |
| Read-only (view/search/file read)                      | A0  | A0     | A0   |
| Documentation/design/draft creation (low risk)         | A1  | A1     | A1   |
| Bulk changes (multiple file modifications/creation)    | A1  | A2     | A2   |
| Git write (commit/push/rebase, etc.)                   | A2  | A2     | A3   |
| Dependency installation/environment changes            | A2  | A2     | A3   |
| Network (external download/upload/API calls)           | A2  | A2     | A3   |
| Destructive (deletion/initialization/bulk overwriting) | A2  | A3     | A3   |
| Production impact (environment/deployment/data changes)| A3  | A3     | A3   |

### 2.3 Operating Rules to Reduce "Approval Burden" (Recommended)

- **Batch Approval**: Bundle executions with the same purpose/scope into a single approval (e.g., formatting cleanup in the same directory).
- **Post-notification (A1)**: For LOW/MEDIUM non-risky changes, handle with post-notification by noting "what changed" in PR/WI.
- **Sampling Review**: For LOW tasks, approve/review only samples like 1 out of 10 (recommended).

- **Bulk execution/auto-generation**: Tasks that may create/modify many files
- **External network**: Downloads/uploads/external API calls
- **Dependency installation**: Package installation, environment changes
- **Repository state changes**: Git operations like branching/committing/pushing/rebasing
- **Ambiguous but complex scripts**: Commands where "what will happen" is not immediately clear

## 3) Confirmation Request Template (Agent Always Includes)

- **Purpose**: Why is this needed?
- **Execution Plan (Summary of Key Commands)**: What commands/tools will run on what scope? (Bundle repetitive queries into one-line summary)
  - **Project Context**: `project_id (PRJ-...)` + `context_id (CTX-...)` (prevent parallel processing/misexecution)
  - **Commands (Planned)**: 1~N commands in the following format
    - `Command`: …
    - **Intent/Effect**: What will it change or check?
    - **Target Scope**: Path/branch/environment/target resource (clear like "entire repo", "docs/**", "single file")
    - **Destructiveness**: Read-only | Write | Destructive (deletion/bulk change) | Network
    - **Expected Changes**: Files to be created/modified/deleted (or "no changes" expected)
  - **Validation Plan (Minimum)**: How to determine success/failure after execution? (e.g., `git status`, smoke test, specific file diff check)
  - **Abort Criteria**: What output/state will trigger immediate halt and re-reporting?
  - **Secrets/Sensitive Info Check**: Any possibility of tokens/passwords/personal info in command/logs/output? If yes, what's the masking/replacement plan?
- **Expected Output**: What files/logs/output will be produced?
- **Risks**: What are the failure/side-effect possibilities?
- **Rollback/Recovery**: How to revert?
- **Approval Question**: "May I proceed now? (Y/N)"

### Example

- **Purpose**: Extract synonyms/forbidden terms from public data standard XLSX to create searchable index (JSON).
- **Execution Plan (Summary of Key Commands)**:
  - **Commands (Planned)**:
    - `(example) python3 <TBD: pds7 index generation script/skill>`
    - **Intent/Effect**: Read XLSX to regenerate standard terminology index (JSON) in a reproducible manner
    - **Target Scope**: Input `docs/standards/public_data/공공데이터_공통표준7차_제·개정(2024.11월).xlsx`, Output `docs/standards/public_data/standard_glossary/PDS7_INDEX.json`
    - **Destructiveness**: Write (create/update)
    - **Expected Changes**: `PDS7_INDEX.json` content updated (no other file changes expected)
  - **Validation Plan (Minimum)**: After execution, check with `git status` that only `PDS7_INDEX.json` changed, verify file size/top-level key structure (terms/words/domains) is normal
  - **Abort Criteria**: If unexpected files change extensively or output JSON is abnormal (empty file/unparseable/broken structure), halt immediately and report
  - **Secrets/Sensitive Info Check**: Uses only public data files (no tokens/personal info input). Only paths/filenames remain in logs
- **Expected Output**: `PDS7_INDEX.json` file created/updated
- **Risks**: Generated file size may be large (increases repo size)
- **Rollback/Recovery**: `git checkout -- docs/standards/public_data/standard_glossary/PDS7_INDEX.json` or `git revert`
- **Approval Question**: May I proceed now? (Y/N)

## 4) Streamlined Approval Policy (User-Centric)

This section defines a simplified approval workflow optimized for users who understand development concepts but delegate implementation details.

### 4.1 REQ-Based Approval (Single Gate)

| Approval Point | Required? | Rationale |
|----------------|-----------|-----------|
| **REQ Approval** | **YES** | User approves scope, direction, and work breakdown |
| WI Generation | NO | Derived from approved REQ - auto-proceed |
| WI Execution | NO | Already planned in REQ - auto-proceed |
| Implementation Details | NO | Trust agent expertise |

### 4.2 Exception Cases (Approval Required)

Even after REQ approval, stop and ask user when:

| Situation | Action |
|-----------|--------|
| **Destructive operations** | `rm`, file deletion, database changes → Ask first |
| **Requirement changes needed** | Scope/direction differs from REQ → Re-confirm |
| **Critical decision points** | Architecture choices, library selection → Consult user |
| **Unexpected blockers** | Cannot proceed as planned → Report and ask |
| **Security/sensitive data** | Credentials, PII, production access → Always ask |

### 4.3 Auto-Proceed Operations

These operations proceed without asking (after REQ approval):

- Git operations: `status`, `diff`, `log`, `add`, `commit`, `push`
- Build/test: `npm install`, `npm run build`, `npm test`
- Quality checks: `/typecheck`, `/eslint`, `/prettier`, `/test`
- File creation/modification: As specified in WI
- Subagent delegation: As planned in REQ EXECUTION STRATEGY

### 4.4 User Profile Consideration

This policy is optimized for:
- **Non-developer users** with development knowledge (e.g., PM, technical planners)
- Users who understand "what" but delegate "how"
- Focus on business decisions, not implementation details

## 5) Exceptions (When Confirmation Not Needed)

- Simple queries (e.g., file reading, short grep) that are **clearly non-destructive and small in scope**

