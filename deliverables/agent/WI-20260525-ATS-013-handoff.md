[WI HEADER]
WI ID: WI-20260525-ATS-013
REQ: REQ-20260525-ATS-005
Agent: qa/qa-integ
Depends On: WI-20260525-ATS-010, WI-20260525-ATS-011, WI-20260525-ATS-012
Blocks: -

[WI SUMMARY]
Why: Entitlement correction touches access state and must pass full regression before commit.
Scope (in/out): Run backend/frontend/docs/diff validation, create evidence pack, and prepare commit. Exclude live Toss/provider tests.
DoD: Required gates pass and generated side effects are cleaned up before commit.
Constraints/Forbidden: Do not commit generated `frontend/tsconfig.tsbuildinfo` side effects.

[ACCEPTANCE CRITERIA]
Functional:
- [ ] Backend tests pass.
- [ ] Docs validation passes.
- [ ] Diff check passes.
Performance:
- [ ] N/A
Quality:
- [ ] Frontend gates pass if frontend is touched; otherwise document skip or run full gate if needed.

[INPUT POINTERS]
Tier 0 (Constitution - Required for all):
- docs/standards/core-principles.md
- docs/standards/development-standards.md

Tier 1 (Policies - Inferred from REQ/WI content):
- docs/policies/quality-gates.md

Tier 2 (Context):
- deliverables/user/REQ-20260525-ATS-005.md
- deliverables/agent/WI-20260525-ATS-010-evidence-pack.md
- deliverables/agent/WI-20260525-ATS-011-evidence-pack.md
- deliverables/agent/WI-20260525-ATS-012-evidence-pack.md

[OUTPUT CONTRACT]
User-facing -> deliverables/user/WI-20260525-ATS-013-summary.md
Agent-facing -> deliverables/agent/WI-20260525-ATS-013-evidence-pack.md
Handoff Packet -> deliverables/agent/WI-20260525-ATS-013-handoff.md

[TRACEABILITY REQUIREMENTS]
Evidence pointers (files/lines/commands/logs): Required
Tests: Required
Rollback: Document commit revert and DB migration rollback
