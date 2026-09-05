# WI Handoff Packet: WI-20260823-ATS-007

[WI HEADER]
WI ID: WI-20260823-ATS-007
REQ: REQ-20260823-ATS-001
Agent: qa-integ
Depends On: WI-20260823-ATS-006
Blocks: -

[WI SUMMARY]
Why: Close the client-feedback remediation with independent final verification after the server-boundary correction.

Scope (in):
- Verify BUSINESS plus `job` is rejected on register, complete-profile, and update-profile direct payload paths; validate business null-job and individual job controls remain accepted.
- Recheck the approved implementation/test/doc evidence for mood selection, nickname normalization, Play all, Likes drawer request/reopen state, and FAB layout contract.
- Re-run relevant frontend/backend quality gates and current-document validation when feasible.
- Classify the known excluded HomePage test mismatch and media/storage mismatch separately from this REQ.

Scope (out):
- No product/source/doc/config/data/schema/storage edits except required WI-007 evidence and summary.
- No client worktree access, protected user mutation, payment/refund/mail/provider, or media playback.

DoD:
- Final PASS/FAIL matrix exists for all REQ criteria and WI-006 regression.
- Failures, if any, distinguish release-blocking scope defects from explicitly excluded worktree/environment conditions.
- Quality evidence is reproducible.

[ACCEPTANCE CRITERIA]
- [ ] All three BUSINESS-with-job server-boundary tests are independently verified.
- [ ] Full backend test result and frontend quality results are recorded.
- [ ] Docs validation result is recorded.
- [ ] No scope or policy regression is found.

[INPUT POINTERS]
Tier 0:
- docs/standards/core-principles.md
- docs/standards/development-standards.md

Tier 1:
- docs/policies/quality-gates.md
- docs/policies/security-policy.md

Tier 2:
- .agents/skills/react-best-practices/AGENTS.md
- docs/standards/frontend-standards.md
- docs/design/api-spec.md
- docs/design/usecase/user-info.md
- docs/design/usecase/sound-playlist.md
- docs/ui/atstudio-front-list.md
- docs/ui/screen-flow.md

REQ/Context Docs:
- deliverables/user/REQ-20260823-ATS-001.md
- deliverables/agent/WI-20260823-ATS-002-evidence-pack.md
- deliverables/agent/WI-20260823-ATS-003-evidence-pack.md
- deliverables/agent/WI-20260823-ATS-004-evidence-pack.md
- deliverables/agent/WI-20260823-ATS-005-evidence-pack.md
- deliverables/agent/WI-20260823-ATS-006-evidence-pack.md

[OUTPUT CONTRACT]
User-facing -> deliverables/user/WI-20260823-ATS-007-summary.md
Agent-facing -> deliverables/agent/WI-20260823-ATS-007-evidence-pack.md
Handoff Packet -> deliverables/agent/WI-20260823-ATS-007-handoff.md

[TRACEABILITY REQUIREMENTS]
- No source fixups. Report defects for a new WI if found.
- State exact count and reason for any excluded test failure.
