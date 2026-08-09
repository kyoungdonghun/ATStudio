[WI HEADER]
WI ID: WI-20260809-ATS-017
REQ: REQ-20260808-ATS-004
Agent: qa-fe
Depends On: WI-20260809-ATS-013, WI-20260808-ATS-030
Blocks: Final frontend quality gate rerun

[WI SUMMARY]
Why: Repair one stale full-coverage assertion that still expects the pre-WI-013 definite request-failure message for an ambiguous no-response mutation outcome.
Scope (in/out): Update only the affected coverage test scenario in `adminSubscriberGaps.coverage.test.tsx` so it verifies the current ambiguous request outcome contract: one bounded reconciliation read, unknown-outcome warning, duplicate mutation fence, read-only retry availability, and unchanged subscription row. Review adjacent assertions for consistency. Do not change product behavior.
DoD: Focused RED/GREEN evidence demonstrates the old assertion fails and the current policy assertion passes; the exact request/reconciliation/disabled-state behavior is protected; scoped typecheck/lint/Prettier pass; WI-017 summary/evidence are written.
Constraints/Forbidden: Test and WI deliverables only. No product implementation, docs, schema/data, dependency, secret, ZIP, external call, commit, push, branch, or client-branch changes. Preserve all unrelated dirty work.

[ACCEPTANCE CRITERIA]
Functional:
- [ ] Generic no-response request failure is asserted as ambiguous, not definite HTTP rejection.
- [ ] The unknown-outcome warning and one `상태 다시 확인` control are visible.
- [ ] Duplicate request creation remains disabled while the fence is active.
- [ ] The current subscription row remains visible.
- [ ] Existing dedicated 4xx and 5xx/network tests remain semantically consistent.
Performance:
- [ ] No additional runtime request is introduced; tests only observe the existing bounded reconciliation behavior.
Quality:
- [ ] Focused Vitest passes.
- [ ] `npm run typecheck`, `npm run lint`, and scoped Prettier pass.

[INPUT POINTERS]
Tier 0:
- docs/standards/core-principles.md
- docs/standards/development-standards.md
Tier 1:
- docs/policies/quality-gates.md
Context:
- deliverables/user/REQ-20260808-ATS-004.md
- deliverables/user/WI-20260809-ATS-013-summary.md
- deliverables/agent/WI-20260809-ATS-013-evidence-pack.md
- deliverables/user/WI-20260808-ATS-028-summary.md
Files:
- frontend/src/test/coverage/adminSubscriberGaps.coverage.test.tsx:691-717
- frontend/src/pages/admin/UserSubscriptionCorrectionModal.tsx:419-527
- frontend/src/pages/admin/UserSubscriptionManagePage.test.tsx:632-789
Repro:
- `cd frontend; npm run test:coverage`
- Current failure: `adminSubscriberGaps.coverage.test.tsx:712` expects the retired definite-failure message while UI correctly shows the unknown-outcome fence.

[OUTPUT CONTRACT]
User-facing -> deliverables/user/WI-20260809-ATS-017-summary.md
Agent-facing -> deliverables/agent/WI-20260809-ATS-017-evidence-pack.md
Handoff Packet -> deliverables/agent/WI-20260809-ATS-017-handoff.md

[TRACEABILITY REQUIREMENTS]
Include exact old/new assertions, mock call evidence, focused commands/results, risks, rollback, and final full-suite unblock status.
