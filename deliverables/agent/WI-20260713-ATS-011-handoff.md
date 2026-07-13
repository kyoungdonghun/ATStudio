[WI HEADER]
WI ID: WI-20260713-ATS-011
REQ: REQ-20260713-ATS-001
Agent: qa-integ
Depends On: WI-20260713-ATS-006, WI-20260713-ATS-007, WI-20260713-ATS-008
Blocks: WI-20260713-ATS-012

[WI SUMMARY]
Why: Verify code, tests, frontend contract, API behavior, persistence model, and approved design agree across all three P0 fixes.
Scope (in/out): Perform 3-way design-code-test comparison and run a combined focused suite. Correct only contract mismatches required by the approved P0 scope.
DoD: A traceability matrix covers every acceptance ID; API/DB counts remain unchanged; no undocumented schema or endpoint change exists.
Constraints/Forbidden: No new feature, broad docs rewrite, external calls, data mutation, or unrelated cleanup.

[ACCEPTANCE CRITERIA]
- [ ] MEDIA-01..05, MAIL-01..02, WITHDRAW-01..04, and REGRESSION-01 map to code and passing tests.
- [ ] Frontend nullable public/admin contract matches backend serialization.
- [ ] Existing DB enum/schema supports the incident behavior.
- [ ] Combined focused suite passes.

[INPUT POINTERS]
Tier 0:
- docs/standards/core-principles.md
- docs/standards/development-standards.md
Tier 1:
- docs/policies/security-policy.md
- docs/policies/quality-gates.md
Tier 2:
- docs/design/p0-release-blocker-remediation-design.md
- docs/design/api-spec.md
- docs/design/db-schema.md
REQ/Context Docs:
- deliverables/agent/WI-20260713-ATS-006-evidence-pack.md
- deliverables/agent/WI-20260713-ATS-007-evidence-pack.md
- deliverables/agent/WI-20260713-ATS-008-evidence-pack.md
Files:
- All P0 implementation and focused test paths

[OUTPUT CONTRACT]
User-facing -> deliverables/user/WI-20260713-ATS-011-summary.md
Agent-facing -> deliverables/agent/WI-20260713-ATS-011-evidence-pack.md
Handoff Packet -> deliverables/agent/WI-20260713-ATS-011-handoff.md

[TRACEABILITY REQUIREMENTS]
Design-code-test matrix, commands/results, mismatches, and residual risk: Required
