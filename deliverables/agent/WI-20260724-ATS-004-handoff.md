[WI HEADER]
WI ID: WI-20260724-ATS-004
REQ: REQ-20260724-ATS-001
Agent: qa
Depends On: WI-20260724-ATS-001, WI-20260724-ATS-002, WI-20260724-ATS-003
Blocks: WI-20260724-ATS-007

[WI SUMMARY]
Why: Prove the backend and acceptance lifecycle at the final residual-cleanup snapshot.
Scope (in/out): Read-only verification plus evidence documents. No product fixes.
DoD: Full backend test/coverage/build and acceptance contract tests pass, or failures are precisely classified.
Constraints/Forbidden: No live Provider calls, DB mutation, secret reading, or source edits beyond evidence deliverables.

[ACCEPTANCE CRITERIA]
Quality:
- [ ] Full Gradle tests pass.
- [ ] JaCoCo gates pass.
- [ ] Backend build passes.
- [ ] Acceptance backend-environment and dry-run tests pass.

[INPUT POINTERS]
Tier 0:
- docs/standards/core-principles.md
- docs/standards/development-standards.md
Tier 1:
- docs/policies/quality-gates.md
REQ/Context:
- deliverables/user/REQ-20260724-ATS-001.md
- deliverables/agent/WI-20260724-ATS-001-evidence-pack.md
- deliverables/agent/WI-20260724-ATS-002-evidence-pack.md
- deliverables/agent/WI-20260724-ATS-003-evidence-pack.md

[OUTPUT CONTRACT]
User-facing -> deliverables/user/WI-20260724-ATS-004-summary.md
Agent-facing -> deliverables/agent/WI-20260724-ATS-004-evidence-pack.md

[TRACEABILITY REQUIREMENTS]
Record exact commands, counts, coverage, warnings/skips, and PASS/FAIL.
