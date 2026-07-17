[WI HEADER]
WI ID: WI-20260716-ATS-037
REQ: REQ-20260716-ATS-004
Agent: docops
Depends On: -
Blocks: WI-20260716-ATS-038

[WI SUMMARY]
Why: Identify stale current-state documentation, active-vs-history confusion, backup/generated artifacts, stale worktree registrations, and repository hygiene candidates before V1 consolidation.
Scope (in/out): Read-only inspection of `docs/`, `deliverables/`, registries, scripts/output/log/tmp/attachment paths, `.gitignore`, branches and worktree metadata. Do not delete, archive, move, rewrite, prune, commit, or alter runtime services. Only the required WI summary and Evidence Pack may be created.
DoD: Produce a KEEP/REMOVE/REPLACE/ARCHIVE/REVIEW inventory for documentation and repository artifacts; preserve REQ/WI history while identifying stale current-state claims; enumerate exact branch/worktree/generated-artifact cleanup candidates and validation steps.
Constraints/Forbidden: No `git worktree prune`, branch/tag operation, file cleanup, index rewrite, formatter, generated-output deletion, or public server change.

[ACCEPTANCE CRITERIA]
Functional:
- [ ] Current-state docs are distinguished from historical deliverables and SR records.
- [ ] Stale compatibility/planned/temporary wording is listed with evidence.
- [ ] Worktrees, prunable registrations, logs, screenshots, demo seeds, tmp and attachments are inventoried without deletion.
- [ ] Documentation and artifact archival/removal recommendations follow versioning/archive policy.
Performance:
- [ ] No public/client runtime process is started, stopped, or mutated.
Quality:
- [ ] Findings include exact paths, status, ownership, recommended disposition, and link/index consequences.
- [ ] No product, document, Git, or generated file is modified except the two WI deliverables.

[INPUT POINTERS]
Tier 0 (Constitution - Required for all):
- docs/standards/core-principles.md
- docs/standards/documentation-standards.md
- docs/standards/glossary.md

Tier 1 (Policies - Inferred):
- docs/policies/versioning-policy.md
- docs/policies/archive-policy.md
- docs/policies/execution-policy.md
- docs/policies/quality-gates.md

Tier 2 (Task Context):
- docs/index.md
- docs/registry/project-registry.md
- docs/registry/workboard.md
- docs/SR/index.md
- docs/design/index.md

REQ/Context Docs:
- deliverables/user/REQ-20260716-ATS-004.md
- AGENTS.md

Files:
- docs/
- deliverables/
- .gitignore
- frontend/.gitignore
- scripts/
- output/
- tmp/
- .codex-remote-attachments/
- Git branches/worktree metadata

Repro/Logs:
- `git status`, `git worktree list`, `git branch`, `rg`, docs validation in read-only mode

[OUTPUT CONTRACT]
User-facing -> deliverables/user/WI-20260716-ATS-037-summary.md:
- Korean summary, stale-doc/artifact counts, approval-sensitive cleanup candidates
Agent-facing -> deliverables/agent/WI-20260716-ATS-037-evidence-pack.md:
- Full inventory, evidence pointers, archive/remove rationale, index/link impact, repro commands
Handoff Packet -> deliverables/agent/WI-20260716-ATS-037-handoff.md:
- This packet

[TRACEABILITY REQUIREMENTS]
Evidence pointers (files/lines/commands/logs): Required
Tests: Read-only docs validation and before/after Git status comparison
Rollback: No destructive change; list only the two deliverable files created
