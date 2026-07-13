[WI HEADER]
WI ID: WI-20260711-ATS-020
REQ: REQ-20260711-ATS-001
Agent: eo
Depends On: WI-20260711-ATS-016, WI-20260711-ATS-017, WI-20260711-ATS-018, WI-20260711-ATS-019
Blocks: -

[WI SUMMARY]
Why: Integrate the complete audit into one defensible release verdict and remediation roadmap.
Scope (in/out): Deduplicate all evidence, resolve severity conflicts, build the 3-way matrix, summarize quality gates, and propose follow-up REQs. No source fixes.
DoD: Produce the final user report, agent evidence pack, and canonical English audit document under `docs/audit/`.
Constraints/Forbidden: Do not claim unverified runtime facts; do not modify product source, schemas, data, secrets, client docs, or unrelated worktree files.

[ACCEPTANCE CRITERIA]
Functional:
- [ ] One final confirmed/conditional/rejected inventory exists.
- [ ] Release verdict, P0/P1 owners, remediation waves, and acceptance gates are explicit.
- [ ] Design-code-doc matrix and quality results are integrated.
- [ ] Follow-up REQ candidates are bounded and ordered.
Performance:
- [ ] Not applicable.
Quality:
- [ ] Final docs validate and `git diff --check` passes after creation.

[INPUT POINTERS]
Tier 0:
- docs/standards/core-principles.md
- docs/standards/development-standards.md
- docs/standards/documentation-standards.md
- docs/standards/glossary.md
Tier 1:
- docs/policies/security-policy.md
- docs/policies/quality-gates.md
Tier 2:
- docs/audit/
REQ/Context Docs:
- deliverables/user/REQ-20260711-ATS-001.md
- deliverables/agent/WI-20260711-ATS-001-evidence-pack.md through WI-20260711-ATS-019-evidence-pack.md

[OUTPUT CONTRACT]
User-facing -> deliverables/user/WI-20260711-ATS-020-summary.md
Agent-facing -> deliverables/agent/WI-20260711-ATS-020-evidence-pack.md
Canonical report -> docs/audit/full-system-audit-20260713.md
Handoff Packet -> deliverables/agent/WI-20260711-ATS-020-handoff.md

[TRACEABILITY REQUIREMENTS]
Evidence pointers: Required
Tests: Record all WI-009 through WI-015 and final docs validation/diff-check results
Rollback: Remove only WI-020 outputs/report/index entries if explicitly requested
