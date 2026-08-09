[WI HEADER]
WI ID: WI-20260809-ATS-018
REQ: REQ-20260808-ATS-004
Agent: docops
Depends On: WI-20260808-ATS-030
Blocks: Final commit

[WI SUMMARY]
Why: Remove three staged Markdown EOF blank-line violations exposed by `git diff --cached --check`.
Scope (in/out): Fix only the terminal blank-line formatting in the three named summaries and write WI-018 traceability outputs. No content rewrite.
DoD: The three summaries and WI-018 outputs pass scoped Prettier; staged diff check can pass after MA restages them.
Constraints/Forbidden: Do not alter implementation, meaning, schema/data, secrets, ZIP, branches, staging, commit, or push.

[ACCEPTANCE CRITERIA]
Functional:
- [ ] Remove only the extra blank line at EOF in each reported summary.
Quality:
- [ ] Scoped Prettier passes for all edited/new WI-018 Markdown files.
- [ ] Worktree diff check for the three summaries passes.

[INPUT POINTERS]
Tier 0:
- docs/standards/core-principles.md
- docs/standards/documentation-standards.md
- docs/standards/glossary.md
Context:
- deliverables/user/REQ-20260808-ATS-004.md
Files:
- deliverables/user/WI-20260808-ATS-016-summary.md:83
- deliverables/user/WI-20260808-ATS-017-summary.md:101
- deliverables/user/WI-20260809-ATS-006-summary.md:66
Repro:
- `git diff --cached --check`

[OUTPUT CONTRACT]
User-facing -> deliverables/user/WI-20260809-ATS-018-summary.md
Agent-facing -> deliverables/agent/WI-20260809-ATS-018-evidence-pack.md
Handoff Packet -> deliverables/agent/WI-20260809-ATS-018-handoff.md

[TRACEABILITY REQUIREMENTS]
Record exact files, no-content-change confirmation, format commands/results, and rollback.
