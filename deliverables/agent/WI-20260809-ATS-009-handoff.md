[WI HEADER]
WI ID: WI-20260809-ATS-009
REQ: REQ-20260808-ATS-004
Agent: qa-fe
Depends On: WI-20260809-ATS-005, WI-20260809-ATS-008
Blocks: WI-20260808-ATS-028

[WI SUMMARY]
Why: Repair confirmed admin UI/API resilience gaps from WI-005.
Scope (in/out): Reconcile ambiguous correction mutations through open/detail reads, provide status retry, make browser-date checks advisory to server preview, tighten admin wire types, show normalized notes where confirmed; focused tests and current-state docs. New backend preview receipts are out of scope.
DoD: Lost responses cannot leave a silently stale workflow; valid server-date payloads reach preview; admin wire types match DTOs; focused frontend tests pass.
Constraints/Forbidden: No backend/state/schema/data mutation, external calls beyond mocked tests, secrets/ZIP, unrelated redesign, commit, or push.

[ACCEPTANCE CRITERIA]
Functional:
- [ ] Request/approve/execute failures reconcile current server state and preserve retry context.
- [ ] Date validation does not block before authoritative server preview.
- [ ] Exact admin list/detail/assignable-role wire types exclude frontend-only roles.
- [ ] Normalized persisted notes are visible at confirmation or controlled-value boundary.
Performance:
- [ ] Reconciliation performs bounded follow-up reads only after ambiguous failures or explicit retry.
Quality:
- [ ] Focused component/API tests cover lost-response, date-boundary, type, and note behavior.

[INPUT POINTERS]
Tier 0:
- docs/standards/core-principles.md
- docs/standards/development-standards.md
Tier 1:
- docs/policies/security-policy.md
- docs/policies/access-control-policy.md
Context:
- deliverables/user/WI-20260809-ATS-005-summary.md
- deliverables/agent/WI-20260809-ATS-005-evidence-pack.md
- docs/SR/SR-96.md
- docs/SR/SR-97.md
Files:
- frontend admin API/types, UserManagePage, UserSubscriptionManagePage, UserSubscriptionCorrectionModal, focused tests
- exact backend controller DTO signatures only
- affected current-state UI/API docs

[OUTPUT CONTRACT]
User-facing -> deliverables/user/WI-20260809-ATS-009-summary.md
Agent-facing -> deliverables/agent/WI-20260809-ATS-009-evidence-pack.md
Handoff Packet -> deliverables/agent/WI-20260809-ATS-009-handoff.md

[TRACEABILITY REQUIREMENTS]
Patch, tests, ambiguous-outcome behavior, risks, rollback, and WI-028 unblock status are required.
