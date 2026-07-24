[WI HEADER]
WI ID: WI-20260724-ATS-012
REQ: REQ-20260724-ATS-002
Agent: docops
Depends On: WI-20260724-ATS-010
Blocks: WI-20260724-ATS-014

[WI SUMMARY]
Why: Verify that documentation and the client PDF can be reproduced from the pushed clone.
Scope (in/out): Run document validation and the documented PDF replay recipe in the fresh clone, compare hashes, and inspect install/run instructions for hidden local assumptions. Do not rewrite docs in this WI.
DoD: Validators pass; the PDF replay is deterministic; any missing prerequisite is explicitly classified.
Constraints/Forbidden: No source/doc correction, commit, push, or personal absolute path in evidence. Values of runtime executable paths must not be persisted.

[ACCEPTANCE CRITERIA]
- [ ] Tier 0, internal links, traceability, and index validation pass.
- [ ] The exact documented PDF replay succeeds and hashes match.
- [ ] Fresh DB and acceptance instructions are checked against available repository tooling.
- [ ] Historical runners are not presented as active V1 bootstrap tools.

[INPUT POINTERS]
Tier 0:
- docs/standards/core-principles.md
- docs/standards/documentation-standards.md
- docs/standards/glossary.md
Tier 1:
- docs/policies/quality-gates.md
- docs/policies/security-policy.md
REQ/Context:
- deliverables/user/REQ-20260724-ATS-002.md
- docs/client/index.md
- docs/SR/SR-93.md
- src/main/resources/schema.sql
- src/main/resources/seed.sql

[OUTPUT CONTRACT]
User-facing -> `deliverables/user/WI-20260724-ATS-012-summary.md`
Agent-facing -> `deliverables/agent/WI-20260724-ATS-012-evidence-pack.md`

[TRACEABILITY REQUIREMENTS]
Record validation counts, exact portable replay command, hashes, prerequisite gaps, and no-change verdict.
