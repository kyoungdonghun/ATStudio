[WI HEADER]
WI ID: WI-20260809-ATS-008
REQ: REQ-20260808-ATS-004
Agent: se
Depends On: WI-20260809-ATS-004
Blocks: WI-20260808-ATS-028

[WI SUMMARY]
Why: Repair confirmed privileged-backend race and rejection-audit gaps from WI-004.
Scope (in/out): Pessimistically recheck correction actors against role/withdrawal mutations; add durable minimal rejection audit for request/approval failures; focused tests and current-state docs. Preview receipts, free-text DLP, and schema/index changes are explicitly out of scope.
DoD: Actor privilege cannot be used after a serialized demotion/withdrawal; request/approval rejections have durable phase-aware audit evidence; focused and affected backend tests pass.
Constraints/Forbidden: No data/schema mutation, external provider/email/payment calls, secrets/ZIP, unrelated refactor, commit, or push.

[ACCEPTANCE CRITERIA]
Functional:
- [ ] Request, approval, and execution lock/recheck the actor at the privileged mutation boundary.
- [ ] Request and approval business rejections record minimal durable phase-aware audit without replacing the original error.
- [ ] Local correction still performs zero external payment/provider actions.
Performance:
- [ ] Lock ordering is documented and avoids a new correction/user deadlock cycle.
Quality:
- [ ] Focused unit and concurrency-contract tests cover the repaired behavior.

[INPUT POINTERS]
Tier 0:
- docs/standards/core-principles.md
- docs/standards/development-standards.md
Tier 1:
- docs/policies/security-policy.md
- docs/policies/access-control-policy.md
Context:
- deliverables/user/REQ-20260808-ATS-004.md
- deliverables/user/WI-20260809-ATS-004-summary.md
- deliverables/agent/WI-20260809-ATS-004-evidence-pack.md
- docs/SR/SR-96.md
- docs/SR/SR-97.md
Files:
- AdminSubscriptionCorrection service/controller/repository/entity/audit paths and focused tests
- UserService/UserRepository role and withdrawal lock paths and focused tests
- current-state API/use-case/SR docs affected by the repair

[OUTPUT CONTRACT]
User-facing -> deliverables/user/WI-20260809-ATS-008-summary.md
Agent-facing -> deliverables/agent/WI-20260809-ATS-008-evidence-pack.md
Handoff Packet -> deliverables/agent/WI-20260809-ATS-008-handoff.md

[TRACEABILITY REQUIREMENTS]
Patch, lock order, tests, risks, rollback, and WI-028 unblock status are required.
