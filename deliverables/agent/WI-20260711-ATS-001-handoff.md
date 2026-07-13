[WI HEADER]
WI ID: WI-20260711-ATS-001
REQ: REQ-20260711-ATS-001
Agent: docops
Depends On: -
Blocks: WI-20260711-ATS-006, WI-20260711-ATS-007, WI-20260711-ATS-008

[WI SUMMARY]
Why: Establish the immutable audit baseline and assess documentation structure, traceability, counts, links, and current-state claims.
Scope (in/out): Inspect all documentation categories, registries, REQ/WI/SR references, the uncommitted `docs/client/` rebuild, and the generated client PDF. Do not modify existing docs, code, or PDF.
DoD: Produce a documentation inventory, stale/missing/conflicting claims list, baseline snapshot, and evidence-backed recommendations.
Constraints/Forbidden: Read-only except for this WI's summary and evidence pack. Preserve all user changes. Do not infer implementation behavior from docs alone.

[ACCEPTANCE CRITERIA]
Functional:
- [ ] Record branch `dev/kyoung`, baseline HEAD `27d22446e5d21324dadcfcb322dbe51704dfe914`, and full dirty-worktree inventory.
- [ ] Map document entry points, category indexes, traceability IDs, and current-state counts.
- [ ] Inspect the client Markdown set and `output/pdf/atstudio-client-testing-guide.pdf` for source/PDF drift.
- [ ] Report every finding with exact file pointers and confidence.
Performance:
- [ ] Avoid broad pasted file contents; use paths, headings, and concise evidence.
Quality:
- [ ] Run documentation validation read-only and record the exact result.
- [ ] Distinguish confirmed defect, stale statement, ambiguity, and unverified external dependency.

[INPUT POINTERS]
Tier 0 (Constitution - Required for all):
- docs/standards/core-principles.md

Tier 0 (Standards - Based on Assignee):
- docs/standards/documentation-standards.md
- docs/standards/glossary.md

Tier 1 (Policies - Inferred):
- docs/policies/quality-gates.md

Tier 2 (Documentation Context):
- docs/index.md
- docs/client/
- docs/design/
- docs/payment/
- docs/SR/
- docs/registry/
- deliverables/user/
- deliverables/agent/

REQ/Context Docs:
- deliverables/user/REQ-20260711-ATS-001.md

Files:
- output/pdf/atstudio-client-testing-guide.pdf

Repro/Logs:
- git status --short --branch
- python .agents/skills/validate-docs/scripts/validate_docs.py

[OUTPUT CONTRACT]
User-facing -> deliverables/user/WI-20260711-ATS-001-summary.md : concise Korean findings and risks
Agent-facing -> deliverables/agent/WI-20260711-ATS-001-evidence-pack.md : inventory, evidence pointers, commands, findings, follow-up WI inputs
Handoff Packet -> deliverables/agent/WI-20260711-ATS-001-handoff.md : this packet

[TRACEABILITY REQUIREMENTS]
Evidence pointers (files/lines/commands/logs): Required
Tests: Record docs validation without changing content
Rollback: Only remove this WI's newly created summary/evidence files if explicitly requested
