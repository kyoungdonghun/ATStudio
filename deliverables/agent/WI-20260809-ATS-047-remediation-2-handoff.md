[WI HEADER]
WI ID: WI-20260809-ATS-047-REMEDIATION-2
REQ: REQ-20260809-ATS-001
Agent: se
Depends On: WI-20260809-ATS-047-QA-INTEG-REREVIEW
Blocks: WI-20260809-ATS-047 final QA

[WI SUMMARY]
Why: Rereview found that same-projection status mutations can leave a row whose canonical returned status no longer matches the active status filter.
Scope (in): Refresh the current latest-owned projection when a successful mutation changes status outside the active status filter; preserve canonical response handling for unfiltered/matching projections; add an OPEN-filter -> CLOSED counterexample test. Add direct attachment owner-change and unmount stale-effect tests if they require test-only changes.
Scope (out): New policy, License/Track list ownership, API/state-machine changes, schema/data, and unrelated pages.
DoD: Filtered projection remains semantically valid including pageInfo/list refill; focused tests pass; previous fixes remain intact.
Constraints/Forbidden: No protected output, external effects, branch operations, schema/data, or final evidence documents.

[ACCEPTANCE CRITERIA]
- [ ] In status=OPEN, a successful OPEN -> CLOSED does not leave a CLOSED row in the OPEN projection.
- [ ] The current projection is re-read so list membership and pageInfo come from the backend rather than local removal only.
- [ ] Unfiltered and matching-filter success continues to consume the canonical returned status.
- [ ] Detached success/failure, latest list ownership, pending conflict, legal transitions, exact API type, owner delete, and attachment behavior remain green.
- [ ] Focused frontend and QuestionService tests pass.
- [ ] If added, owner/token replacement and direct unmount tests prove no stale browser download/error effect.

[INPUT POINTERS]
- docs/standards/core-principles.md
- docs/standards/development-standards.md
- docs/policies/quality-gates.md
- docs/standards/frontend-standards.md
- deliverables/agent/WI-20260809-ATS-047-handoff.md
- deliverables/agent/WI-20260809-ATS-047-remediation-handoff.md
- deliverables/agent/WI-20260809-ATS-047-qa-integ-rereview-handoff.md
- frontend/src/pages/admin/QuestionManagePage.tsx
- frontend/src/test/coverage/adminSubscriberPages.coverage.test.tsx
- frontend/src/pages/subscriber/QuestionDetailPage.test.tsx

[OUTPUT CONTRACT]
- Edit implementation/tests only within scope.
- Report exact red/green and focused validation results; no final evidence/summary yet.

[TRACEABILITY REQUIREMENTS]
- The new status-filter test must fail against the pre-fix behavior and verify the post-success backend refresh result, not merely local row deletion.
