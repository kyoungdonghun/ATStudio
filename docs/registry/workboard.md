---
version: 1.0
last_updated: 2026-01-06
project: system
owner: MA
category: registry
status: stable
dependencies:
  - path: project-registry.md
    reason: Project registry reference
  - path: context-registry.md
    reason: Context registry reference
  - path: ../guides/request-intake.md
    reason: Request intake guide reference
  - path: ../guides/operation-process.md
    reason: Work management process reference
  - path: ../templates/impact-analysis-template.md
    reason: Impact analysis template reference
  - path: ../standards/glossary.md
    reason: Standard terminology usage criteria
---
# Workboard — Requirements → WI Decomposition → Progress Tracking

> Purpose: See at a glance "what are the current requirements (REQ) → what work (WI) they decomposed into → how far we've progressed".
> In MVP, operate as **manually updatable single file**, expand to dashboard/automatic aggregation in Phase 2+.

> Scope: This document is the **system-wide (all projects)** workboard.
> Manages REQ, WI, CTX for all projects in this document.

---

## 1) What This Document Covers

- **Requirements/Requests**: `REQ-...` (Request)
- **Work Decomposition Results**: `WI-...` (Work Items, DAG)
- **Parallel Execution Units**: `CTX-...` (Context Instances)
- **Progress Status/Triggers**: What events enable starting next work

> Source of Truth (Principle): Detailed design/decisions/verification are in each WI/ADR/PR, and this document only plays **summary index** role.
> WI documents are located at `docs/project/WI-YYYYMMDD-###.md` in each project repo, and this Workboard integrates WIs from all projects.

---

## 2) Update Rules ("Real-time" Realistic Definition)

In MVP, "real-time" doesn't mean UI streaming, but updating when the following **checkpoint events** occur.

- **REQ intake/normalization complete**: REQ → WI decomposition done
- **WI start/block/unblock**: Ready/Blocked/In Progress transition
- **PR creation/review/merge**: In Review/Merged
- **Artifact Done**: Document/policy/template/index creation, etc. (cases where completion is trigger even without merge)
- **Approval Done**: HITL/EO approval complete
- **Deploy Done / Verify Done**: Environment deployment/independent verification complete

Recommended operation:

- LOW: Updating only "major events" (start/complete) is sufficient
- MEDIUM/HIGH: Record status transitions more densely (Ready→In Progress→In Review→Done)

---

## 3) Status (Recommended)

- **Proposed**: Proposal/draft (not started yet)
- **Ready**: Prerequisites met, can start
- **Blocked**: Prerequisite WI/approval/deliverable not met
- **In Progress**: In progress (CTX fixed)
- **In Review**: Awaiting review/verification/approval
- **Merged**: PR merge complete (when applicable)
- **Done**: Complete (includes Artifact Done)
- **Cancelled**: Cancelled/abandoned

---

## 4) Workboard Format (Recommended)

### 4.1 Requirements / Requests (REQ)

| PRJ | REQ | Requirement (Summary) | Criticality | Status | Related WIs | Trigger to Start Next | Notes |
| :-- | :-- | :-- | :-- | :-- | :-- | :-- | :-- |
| PRJ-EXAMPLE | REQ-EXAMPLE | Example: "Define request intake" | P1 | In Progress | WI-..., WI-... | Artifact Done / Verify Done | |

### 4.2 Work Items (WI) — DAG & Progress

> Evidence Pack operational rules:
> - Standard path: `deliverables/agent/<WI-ID>-evidence-pack.md`
> - Standard document: `docs/standards/evidence-pack-standard.md`

| PRJ | CTX | WI | Title | Status | Depends/Blocked by | Parallelizable with | Trigger event(s) | WI Link | Evidence Pack | PR/Link | Progress |
| :-- | :-- | :-- | :-- | :-- | :-- | :-- | :-- | :-- | :-- | :-- |
| PRJ-EXAMPLE | CTX-EXAMPLE | WI-EXAMPLE | Example: "Expand impact-analysis" | In Review | WI-... | WI-... | PR merge | `{project repo}/docs/.../WI-EXAMPLE.md` | `deliverables/agent/WI-EXAMPLE-evidence-pack.md` | (PR link) | 70% |
| system | CTX-DOCOPS | WI-20260127-001 | Index display name Korean/meaning unification + link consistency check | Done | - | - | Artifact Done | `docs/work-items/WI-20260127-001-index-display-name-koreanize.md` | `deliverables/agent/WI-20260127-001-evidence-pack.md` | - | 100% |
| system | CTX-DOCOPS | WI-20260127-002 | Agent-facing document set organization/operation standardization | In Review | WI-20260127-001 (Done) | - | Approval Done (user approval) → Artifact Done | `docs/work-items/WI-20260127-002-agent-facing-docset-ops-standardization.md` | `deliverables/agent/WI-20260127-002-evidence-pack.md` | - | 80% |

---

## 5) Responsibility (Recommended)

- **MA (R/A)**: Workboard updates (requirements↔WI connection, status updates)
- **EO (A)**: Operational discipline (format/terminology/consistency) approval
- **Workers (I)**: Report status transition events of WIs they're involved in to MA (or leave in PR/WI)

---

## 6) Connected Documents

- Request starting point: `docs/guides/request-intake.md`
- Work management/change management (including orchestration): `docs/guides/operation-process.md`
- Impact/parallel/trigger notation: `docs/templates/impact-analysis-template.md`
- Agent-facing documents guide: `docs/guides/agent-facing-docs.md`
- Evidence Pack standard: `docs/standards/evidence-pack-standard.md`
- Context instance list: `docs/registry/context-registry.md`
- Project list: `docs/registry/project-registry.md`
