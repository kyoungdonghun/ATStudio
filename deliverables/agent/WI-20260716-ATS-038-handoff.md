[WI HEADER]
WI ID: WI-20260716-ATS-038
REQ: REQ-20260716-ATS-004
Agent: qa-integ
Depends On: WI-20260716-ATS-034, WI-20260716-ATS-035, WI-20260716-ATS-036, WI-20260716-ATS-037
Blocks: USER-DESTRUCTIVE-APPROVAL

[WI SUMMARY]
Why: Integrate four independent read-only audits into one evidence-backed V1 residual-code disposition and exact approval manifest without changing product state.
Scope (in/out): Read all WI-034~037 summaries/evidence packs, reconcile overlaps and conflicts, and create only WI-038 user summary and Evidence Pack. No product, config, SQL, existing documentation, Git branch/worktree/tag, DB, generated artifact, or runtime mutation.
DoD: Produce one deduplicated KEEP/REMOVE/REPLACE/ARCHIVE/REVIEW matrix with source finding IDs, exact paths/symbols, callers/consumers, rationale, confidence, dependencies, deletion impact, replacement target, verification gate, and approval bundle. Explicitly list unresolved external-traffic/profile/DB uncertainties and stop at the approval gate.
Constraints/Forbidden: No fixes, deletions, moves, formatting, commits, staging, SQL, branch/worktree operations, runtime start/stop, or speculative promotion from REVIEW to REMOVE. Conflicting findings default to REVIEW unless stronger evidence resolves them. Historical REQ/WI/SR/audit records remain KEEP.

[ACCEPTANCE CRITERIA]
Functional:
- [ ] Every actionable finding from WI-034~037 maps to exactly one integrated disposition row or an explicit deduplication mapping.
- [ ] Cross-layer items such as play history, download queue, payment aliases, acceptance config, seed ownership, manual SQL, and generated artifacts are resolved coherently.
- [ ] Operational integrity controls and non-production acceptance safety tools are explicitly protected from blanket deletion.
- [ ] Exact destructive candidates are grouped into separately approvable bundles: safe dead code, coordinated replacement, DB baseline, docs/artifacts, branches/worktrees, and unresolved review.
Performance:
- [ ] Integration uses existing evidence; no shared runtime or DB inspection is introduced.
Quality:
- [ ] Counts reconcile with source findings and deduplication is documented.
- [ ] Each destructive candidate includes proof-before-delete and proof-after-delete commands/tests.
- [ ] No state changes occur beyond the two WI deliverables.

[INPUT POINTERS]
Tier 0 (Constitution - Required for all):
- docs/standards/core-principles.md
- docs/standards/development-standards.md

Tier 1 (Policies - Inferred):
- docs/policies/security-policy.md
- docs/policies/versioning-policy.md
- docs/policies/archive-policy.md
- docs/policies/execution-policy.md
- docs/policies/quality-gates.md

Tier 2 (Task Context):
- docs/design/api-spec.md
- docs/design/db-schema.md
- docs/design/remaining-remediation-design-20260716.md
- docs/standards/frontend-standards.md
- docs/ui/screen-flow.md

REQ/Context Docs:
- deliverables/user/REQ-20260716-ATS-004.md
- deliverables/user/WI-20260716-ATS-034-summary.md
- deliverables/user/WI-20260716-ATS-035-summary.md
- deliverables/user/WI-20260716-ATS-036-summary.md
- deliverables/user/WI-20260716-ATS-037-summary.md
- AGENTS.md

Evidence Files:
- deliverables/agent/WI-20260716-ATS-034-evidence-pack.md
- deliverables/agent/WI-20260716-ATS-035-evidence-pack.md
- deliverables/agent/WI-20260716-ATS-036-evidence-pack.md
- deliverables/agent/WI-20260716-ATS-037-evidence-pack.md

Repro/Logs:
- Source audit commands embedded in the four Evidence Packs

[OUTPUT CONTRACT]
User-facing -> deliverables/user/WI-20260716-ATS-038-summary.md:
- Korean executive report, integrated counts, approval bundles, explicit stop point, and decisions required from the user
Agent-facing -> deliverables/agent/WI-20260716-ATS-038-evidence-pack.md:
- Full deduplicated disposition table, source mapping, conflicts, exact path/symbol manifest, verification gates, risks/rollback
Handoff Packet -> deliverables/agent/WI-20260716-ATS-038-handoff.md:
- This packet

[TRACEABILITY REQUIREMENTS]
Evidence pointers (files/lines/commands/logs): Required
Tests: Reconciliation/coverage checks over source IDs, `git diff --check` for deliverables, before/after product and Git-state comparison
Rollback: Remove only the two WI-038 deliverable files; no product rollback should be necessary
