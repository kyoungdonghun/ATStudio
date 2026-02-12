---
version: 1.0
last_updated: 2026-01-29
project: system
owner: MA
category: template
status: deprecated
dependencies:
  - path: ../registry/workboard.md
    reason: Replacement document
---
# Project Workboard — DEPRECATED

> ⚠️ **This template is no longer used.**
> All work status is now managed in integrated fashion at system level: `agentic-subagent-team/docs/registry/workboard.md`

- **System-level Workboard**: `agentic-subagent-team/docs/registry/workboard.md`
- **WI documents**: `agentic-subagent-team/docs/work-items/WI-YYYYMMDD-###.md`

---

## Deprecation Reason

- Integrated workboard management at system level ensures consistency
- WI and Workboard at same level makes reuse discovery easier
- Eliminates duplicate management per project

## Alternative

All project REQ, WI, CTX are managed integrated in the following documents:

- **System-level Workboard**: `agentic-subagent-team/docs/registry/workboard.md`
- **WI documents**: `agentic-subagent-team/docs/work-items/WI-YYYYMMDD-###.md`
- **Deliverables**: PR/files/repos in project repo (connected via Workboard's PR/Link field)

---

## 2) Table (template)

### 2.1 Requirements / Requests (REQ)

| REQ | Requirement (summary) | Criticality | Status | Related WIs | Trigger to Start Next | Notes |
| :-- | :-- | :-- | :-- | :-- | :-- | :-- |
| REQ-YYYYMMDD-### |  | P0/P1/P2/P3 | Proposed/Ready/In Progress/In Review/Done | WI-..., WI-... | Artifact Done / Approval Done / Deploy Done / Verify Done / PR merge |  |

### 2.2 Work Items (WI) — DAG & Progress

| CTX | WI | Title | Status | Depends/Blocked by | Parallelizable with | Trigger event(s) | PR/Link | Progress |
| :-- | :-- | :-- | :-- | :-- | :-- | :-- | :-- | :-- |
| CTX-YYYYMMDD-### | WI-YYYYMMDD-### |  | Ready/Blocked/In Progress/In Review/Merged/Done | WI-... | WI-... |  |  |  |
