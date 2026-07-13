[WI HEADER]
WI ID: WI-20260713-ATS-001
REQ: REQ-20260713-ATS-001
Agent: eo
Depends On: -
Blocks: WI-20260713-ATS-002

[WI SUMMARY]
Why: Freeze the approved client-document and full-audit work as a clean baseline before P0 source changes.
Scope (in/out): Inspect the dirty worktree, define an explicit stage manifest, validate included docs/PDF/audit deliverables, exclude runtime logs and unrelated files, then record the baseline commit and branch result. Git mutation is executed by MA after manifest review.
DoD: A precise include/exclude list, validation evidence, commit boundary, and target branch name are documented.
Constraints/Forbidden: Do not stage, commit, push, delete, restore, or modify existing files. Do not include cloudflared/Vite logs or unrelated output.

[ACCEPTANCE CRITERIA]
Functional:
- [ ] Every current tracked/untracked path is classified include/exclude.
- [ ] Client PDF is matched to its intended source set.
- [ ] Audit WI-001 through WI-020 and REQ artifacts are complete.
- [ ] Proposed commit does not include runtime logs.
Performance:
- [ ] Not applicable.
Quality:
- [ ] Docs validation and diff-check prerequisites are stated.

[INPUT POINTERS]
Tier 0:
- docs/standards/core-principles.md
- docs/standards/documentation-standards.md
- docs/standards/glossary.md
Tier 1:
- docs/policies/quality-gates.md
Tier 2:
- docs/client/
- docs/audit/
- docs/index.md
REQ/Context Docs:
- deliverables/user/REQ-20260713-ATS-001.md
- deliverables/user/REQ-20260711-ATS-001.md
- deliverables/agent/WI-20260711-ATS-020-evidence-pack.md
Files:
- output/pdf/atstudio-client-testing-guide.pdf
- git status --short

[OUTPUT CONTRACT]
User-facing -> deliverables/user/WI-20260713-ATS-001-summary.md
Agent-facing -> deliverables/agent/WI-20260713-ATS-001-evidence-pack.md
Handoff Packet -> deliverables/agent/WI-20260713-ATS-001-handoff.md

[TRACEABILITY REQUIREMENTS]
Evidence pointers and explicit manifest: Required
Tests: Read-only docs validation/diff-check inventory
Rollback: No Git mutation by agent; MA records commit rollback instructions after execution
