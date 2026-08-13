[WI HEADER]
WI ID: WI-20260809-ATS-047-REMEDIATION
REQ: REQ-20260809-ATS-001
Agent: se
Depends On: WI-20260809-ATS-047-QA-INTEG-REVIEW
Blocks: WI-20260809-ATS-047-QA-INTEG-REREVIEW

[WI SUMMARY]
Why: Independent QA found one projection-ownership defect plus an overstated frontend response type and a documentation surface mismatch.
Scope (in): Make the ADMIN Question list latest-request-owned; bind status mutation success/failure to the initiating page/filter projection and refresh the current projection after a detached success; define the exact status-update response type; strengthen API/race tests; correct QUESTION-007's UI surface. This may pre-resolve only the Question slice of CR-031-096 assigned later to WI-053.
Scope (out): License/Track collection ownership, broader WI-053 work, private/public authorization, backend state-machine changes, schema/data, and unrelated UI behavior.
DoD: All three QA findings are fixed with counterexample tests; focused frontend/backend tests pass; no new policy is introduced.
Constraints/Forbidden: Preserve existing endpoint/status policy. Do not edit protected output artifacts, invoke real external side effects, modify schema/data, or perform branch operations.

[ACCEPTANCE CRITERIA]
- [ ] Older ADMIN Question list responses cannot overwrite a newer page/filter projection.
- [ ] A status success/failure commits UI state only to its initiating projection.
- [ ] If a successful status mutation finishes after projection replacement, the current projection is refreshed through a latest-owned request; no stale error is attached to it.
- [ ] The response status remains canonical and conflicting status mutations remain blocked.
- [ ] updateQuestionStatus returns a dedicated type matching QuestionResponse.fromStatusUpdate's JSON projection.
- [ ] API tests assert exact status request and response unwrapping.
- [ ] Documentation identifies the ADMIN inquiry list, not detail, as the status-control surface.
- [ ] Focused Question frontend tests and QuestionServiceTest pass.

[INPUT POINTERS]
Tier 0:
- docs/standards/core-principles.md
- docs/standards/development-standards.md
Tier 1:
- docs/policies/quality-gates.md
- docs/policies/security-policy.md
Tier 2:
- docs/standards/frontend-standards.md
- docs/design/usecase/user-question.md
Context:
- deliverables/agent/WI-20260809-ATS-047-handoff.md
- deliverables/agent/WI-20260809-ATS-047-qa-integ-review-handoff.md
- deliverables/agent/WI-20260809-ATS-031-consolidated-findings.md (CR-031-096 and WI-053 boundary)
Files:
- frontend/src/pages/admin/QuestionManagePage.tsx
- frontend/src/test/coverage/adminSubscriberPages.coverage.test.tsx
- frontend/src/api/questions.ts
- frontend/src/api/domainApis.test.ts
- docs/design/usecase/user-question.md

[OUTPUT CONTRACT]
- Edit implementation/tests/documentation directly.
- Do not create final evidence/summary yet.
- Report changed files, exact test results, and any unresolved finding.

[TRACEABILITY REQUIREMENTS]
- Preserve red/green evidence for filter/page race and exact response type.
- State explicitly whether the Question slice of CR-031-096 is now pre-resolved.
