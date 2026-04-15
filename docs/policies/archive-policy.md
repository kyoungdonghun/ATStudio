---
version: 1.0
last_updated: 2026-04-15
project: system
owner: EO
category: policy
status: stable
dependencies:
  - path: versioning-policy.md
    reason: Lifecycle and deprecation baseline
  - path: ../standards/documentation-standards.md
    reason: Metadata and document structure baseline
  - path: ../standards/glossary.md
    reason: Canonical lifecycle terminology
tier: 1
target_agents:
  - eo
  - docops
task_types:
  - documentation
  - review
---

# Archive and Historical Document Policy

> Purpose: Keep current operational Source of Truth (SoT) clear while preserving historical records, compatibility notes, and archived references without rewriting history.

## 1. Classification

Use the following document classes when deciding how a file should be maintained.

| Class | Meaning | Currentness Requirement | Typical Examples |
|-------|---------|-------------------------|------------------|
| **Live Operational** | Current SoT or active working reference | Must match current implementation and routing rules | `AGENTS.md`, `docs/standards/`, `docs/policies/`, `docs/architecture/`, `docs/design/`, `docs/templates/`, `docs/registry/`, `docs/ui/`, `docs/adr/`, `.agents/skills/`, `.claude/config/` |
| **Compatibility** | Transitional bridge kept so older workflows still resolve correctly | Must clearly point to the primary SoT | `CLAUDE.md` |
| **Historical Record** | Time-bound record of work, findings, or decisions | Preserve original context/date; do not normalize into present tense | `docs/SR/`, `docs/audit/`, `docs/retrospective/`, `deliverables/` |
| **Archived Reference** | Superseded document retained only for background/reference value | Not a current SoT; must carry archive notice and replacement path when available | `docs/design/base-agent.md` |
| **Snapshot / Generated Workspace** | Temporary or copied workspace state | Excluded from live governance and validation | `.claude/worktrees/` |

## 2. Current ATStudio Baseline

- **Primary live operational SoT**: `AGENTS.md`
- **Compatibility bridge**: `CLAUDE.md`
- **Historical record zones**: `docs/SR/`, `docs/audit/`, `docs/retrospective/`, `deliverables/`
- **Archived reference example**: `docs/design/base-agent.md`
- **Excluded snapshot zone**: `.claude/worktrees/`

## 3. Mutation Rules

- **Live Operational** documents must be updated to the latest verified project state.
- **Compatibility** documents may lag in style, but they must explicitly identify the primary SoT and must not contradict it on active rules.
- **Historical Record** documents should not be rewritten just to align wording with today's state.
- **Historical Record** documents may be corrected only for objective reproducibility issues such as broken local links, impossible paths, or missing pointers.
- **Archived Reference** documents must keep their original content but add clear archive metadata and an archive notice.
- **Snapshot / Generated Workspace** material must not be used as evidence for current SoT decisions.

## 4. Metadata Rules for Archived Documents

When a document is intentionally preserved as an archived reference, use:

- `status: archived`
- `archived_date: YYYY-MM-DD`
- `archive_reason: "..."`
- `replacement_path: ...` when a current replacement exists

If a document is only a **historical record**, keep its normal status (`stable`, `deprecated`, etc.) unless it is explicitly retired from active reference use.

## 5. Lifecycle Guidance

Recommended lifecycle for reusable operational documents:

`Draft -> Stable -> Deprecated -> Archived`

Notes:

- **Deprecated** means migration is still in progress and a replacement path is active.
- **Archived** means the file is retained for history/reference only and is no longer part of the active operational baseline.
- Not every historical record needs to pass through `deprecated`; many records are born as historical evidence and remain `stable` in that role.

## 6. Validation and Indexing Rules

- `validate-docs` gates live operational documents strictly.
- Historical record directories may remain indexed for discoverability, but they must never be treated as the primary operational source.
- Snapshot paths such as `.claude/worktrees/` are excluded from live validation.
- Future long-term archive storage under `docs/archive/` should also be excluded from live validation.

## 7. Decision Rule

When deciding whether to edit an old document, use this order:

1. Is it part of the current SoT?
2. Is it only a compatibility bridge?
3. Is it a historical record that should be preserved?
4. Should it be explicitly archived instead of silently drifting?

If the answer is 3 or 4, preserve history first and avoid rewriting the document as if it were authored today.
