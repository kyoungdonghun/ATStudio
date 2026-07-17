[WI HEADER]
WI ID: WI-20260717-ATS-013
REQ: REQ-20260716-ATS-004
Agent: docops
Depends On: WI-20260717-ATS-012
Blocks: V1 baseline commit

[WI SUMMARY]
Why: Remove four Markdown EOF-only whitespace violations found by the final staged diff check without altering evidence meaning or immutable generated logs.
Scope (in/out): In scope are EOF newline normalization only in the four listed Markdown files. Out of scope are content rewrites, generated log formatting, source/config/test changes, Git operations, and DB operations.
DoD: Each target ends with exactly one newline; semantic content is unchanged; scoped diff check passes.
Constraints/Forbidden: Do not edit generated proof logs or any file outside the four targets. Do not restage or commit.

[ACCEPTANCE CRITERIA]
Functional:
- [ ] Normalize EOF whitespace in the four target Markdown files only.
- [ ] Preserve all words, headings, tables, and evidence claims.
Performance:
- [ ] Not applicable.
Quality:
- [ ] Scoped `git diff --check` passes after excluding immutable WI-004 generated logs.

[INPUT POINTERS]
Tier 0 (Constitution - Required for all):
- docs/standards/core-principles.md

Tier 0 (Standards - Based on Assignee):
- docs/standards/documentation-standards.md
- docs/standards/glossary.md

Tier 1 (Policies - Inferred from REQ/WI content):
- docs/policies/quality-gates.md

Files:
- deliverables/agent/WI-20260717-ATS-002-evidence-pack.md
- deliverables/agent/WI-20260717-ATS-002-handoff.md
- deliverables/user/WI-20260716-ATS-034-summary.md
- deliverables/user/WI-20260717-ATS-002-summary.md

[OUTPUT CONTRACT]
User-facing -> none; bounded remediation is reported through the parent WI.
Agent-facing -> final response with changed paths and scoped diff-check result.
Handoff Packet -> deliverables/agent/WI-20260717-ATS-013-handoff.md :
- This packet.

[TRACEABILITY REQUIREMENTS]
Evidence pointers: Changed path list required
Tests: Scoped diff check required
Rollback: Restore only the four files from the staged version if normalization changes content
