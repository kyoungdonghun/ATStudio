---
version: 1.0
last_updated: 2026-04-15
project: ATS
owner: docops
category: audit
status: stable
dependencies:
  - path: ../policies/archive-policy.md
    reason: Live/compatibility/historical classification baseline
  - path: ../index.md
    reason: Current documentation entrypoint and category counts
  - path: ../../AGENTS.md
    reason: Primary Codex SoT for the migration baseline
---

# Codex Migration Baseline Report

> Purpose: Freeze the current Claude-to-Codex migration baseline and separate it from unrelated working-tree changes.

## 1. Baseline Decision

As of **2026-04-15**, the recommended separation is:

- **Core migration baseline**: Files that define the active Codex-oriented operational SoT
- **Supporting dependencies**: Files that are not the main migration target but should travel with the baseline to keep links, validation, or compatibility intact
- **Out-of-scope changes**: Working-tree changes that should not be mixed into the migration baseline commit

## 2. Core Migration Baseline

These files define the Codex migration baseline directly and should be treated as the primary staging set.

| Scope | Paths |
|------|-------|
| Root entrypoints | `AGENTS.md`, `CLAUDE.md` |
| Runtime config | `.claude/config/workspace.json`, `.claude/config/context-triggers.json`, `.claude/config/context-injection-rules.json` |
| Codex skill surface | `.agents/skills/**/*` |
| Documentation entrypoints | `docs/index.md`, `docs/architecture/system-design.md` |
| Policies | `docs/policies/index.md`, `docs/policies/execution-policy.md`, `docs/policies/versioning-policy.md`, `docs/policies/archive-policy.md` |
| Registry | `docs/registry/index.md`, `docs/registry/asset-registry.md`, `docs/registry/workboard.md` |
| Standards | `docs/standards/index.md`, `docs/standards/development-standards.md`, `docs/standards/documentation-standards.md`, `docs/standards/frontend-standards.md`, `docs/standards/glossary.md`, `docs/standards/prompt-caching-strategy.md` |
| Templates | `docs/templates/index.md`, `docs/templates/impact-analysis-template.md`, `docs/templates/ma-session-kickoff-prompt.md` |
| Supplemental indexes | `docs/adr/index.md`, `docs/ui/index.md`, `docs/audit/index.md` |
| Validation-supporting doc fix | `docs/design/usecase/index.md`, `deliverables/agent/WI-20260307-ATS-012-evidence-pack.md` |

### Notes

- `docs/design/usecase/index.md` is included because the migration cleanup fixed live index coverage.
- `deliverables/agent/WI-20260307-ATS-012-evidence-pack.md` is included only as a reproducibility/link repair exception. It is **not** part of the live SoT.

## 3. Supporting Dependencies

These files are not the main migration target, but excluding them from the same baseline handoff may reintroduce broken references or validation drift.

| Type | Paths | Why They Matter |
|------|-------|-----------------|
| Historical audit asset | `docs/audit/backend-audit-report.md` | Linked by `docs/audit/index.md`; required for a self-consistent audit index |
| Reference asset set | `docs/standards/public_data/**/*` | Referenced from `docs/standards/index.md`; keeps standards index and reference assets consistent |

## 4. Explicitly Out of Scope

These working-tree changes should be handled separately from the migration baseline.

| Type | Paths | Reason |
|------|-------|--------|
| Frontend implementation changes | `frontend/src/layouts/Header.tsx`, `frontend/src/layouts/Header.module.css` | Product/UI work, not migration governance |
| Generated frontend artifact | `frontend/tsconfig.tsbuildinfo` | Build artifact |
| Historical client-doc deletion set | `docs/client/**/*` | Destructive change set unrelated to Codex migration baseline |
| Snapshot worktrees | `.claude/worktrees/**/*` | Temporary/generated snapshot area excluded from live validation |
| Historical SR additions | `docs/SR/SR-70.md` through `docs/SR/SR-78.md` | Historical record additions, not required for migration baseline |
| Runtime logs | `bootRun*.log`, `server*.log`, `server-err.log`, `bootRun-err.log` | Ephemeral logs |
| Static output tree | `src/main/resources/static/**/*` | Application/runtime asset output, not migration documentation work |

## 5. Staging Recommendation

If the goal is a **migration-only baseline commit**, stage:

1. All files in **Core Migration Baseline**
2. All files in **Supporting Dependencies**

Do **not** stage the **Out-of-Scope** set in the same commit.

## 6. Operational Meaning of This Report

This document freezes the answer to a practical question:

> "Which current changes belong to the Codex migration baseline, and which do not?"

Use this report as the staging boundary until a later cleanup or historical curation phase is explicitly approved.
