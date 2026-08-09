[WI HEADER]
WI ID: WI-20260809-ATS-013
REQ: REQ-20260808-ATS-004
Agent: qa-fe
Depends On: WI-20260808-ATS-028 final re-review
Blocks: WI-20260808-ATS-028

[WI SUMMARY]
Why: Correct ambiguous-versus-definite mutation outcome classification and preserve request fencing after inconclusive 204 reconciliation.
Scope (in/out): Correction modal error classification, request open-state null handling, explicit retry, focused tests/docs. No backend correlation/idempotency protocol is added.
DoD: Definite 4xx errors retain stable error messages without reconciliation; network/timeout/5xx outcomes reconcile; a null open read remains unknown and duplicate-fenced; focused tests pass.
Constraints/Forbidden: No backend/schema/data/external real calls, secrets/ZIP, unrelated UI redesign, commit, or push.

[ACCEPTANCE CRITERIA]
Functional:
- [ ] Definite client/business rejections do not enter unknown-outcome reconciliation.
- [ ] Ambiguous request plus 204 keeps unknown state and one read-only retry.
- [ ] Known correction detail can still resolve approval/execution terminal state.
Quality:
- [ ] Focused tests cover 4xx, no-response/timeout, 5xx, and 204 retry behavior.

[INPUT POINTERS]
- docs/standards/core-principles.md
- docs/standards/development-standards.md
- deliverables/user/WI-20260808-ATS-028-summary.md
- frontend UserSubscriptionCorrectionModal and focused tests/API error helpers/current-state docs

[OUTPUT CONTRACT]
User-facing -> deliverables/user/WI-20260809-ATS-013-summary.md
Agent-facing -> deliverables/agent/WI-20260809-ATS-013-evidence-pack.md
Handoff Packet -> deliverables/agent/WI-20260809-ATS-013-handoff.md

[TRACEABILITY REQUIREMENTS]
Patch, classification table, tests, risks, rollback, and WI-028 status are required.
