[WI HEADER]
WI ID: WI-20260525-ATS-008
REQ: REQ-20260525-ATS-004
Agent: qa/qa-integ
Depends On: WI-20260525-ATS-005, WI-20260525-ATS-006, WI-20260525-ATS-007
Blocks: -

[WI SUMMARY]
Why: Refund backend must pass full regression before commit because it touches financial mutation.
Scope (in/out): Run backend focused tests, full backend tests, docs validation, frontend gates if frontend files change, and diff checks. Produce final evidence and summaries.
DoD: All required verification commands pass and commit-ready diff is reviewed.
Constraints/Forbidden: Do not leave build artifacts staged.

[ACCEPTANCE CRITERIA]
Quality:
- [ ] Focused refund/provider tests pass.
- [ ] `gradlew.bat test` passes.
- [ ] `validate_docs.py` passes.
- [ ] `git diff --check` passes.
- [ ] Frontend gates pass if frontend code/docs inventory changes require it.

[INPUT POINTERS]
Tier 0:
- docs/standards/core-principles.md
- docs/standards/development-standards.md
- docs/policies/quality-gates.md

Tier 2:
- deliverables/user/REQ-20260525-ATS-004.md
- deliverables/agent/WI-20260525-ATS-005-evidence-pack.md
- deliverables/agent/WI-20260525-ATS-007-evidence-pack.md

[OUTPUT CONTRACT]
User-facing -> deliverables/user/WI-20260525-ATS-008-summary.md
Agent-facing -> deliverables/agent/WI-20260525-ATS-008-evidence-pack.md
Handoff Packet -> deliverables/agent/WI-20260525-ATS-008-handoff.md

[TRACEABILITY REQUIREMENTS]
Commands, results, remaining risks, and rollback notes are required.
