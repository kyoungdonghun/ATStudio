[WI HEADER]
WI ID: WI-20260809-ATS-047-QA-INTEG-REVIEW
REQ: REQ-20260809-ATS-001
Agent: qa-integ
Depends On: WI-20260809-ATS-047 implementation
Blocks: WI-20260809-ATS-047 evidence and closure

[WI SUMMARY]
Why: Independently challenge the Question UI/backend contract remediation before full gates and commit.
Scope (in): Read-only review of the WI-047 diff, focused tests, legal transition mapping, canonical response use, pending-operation ownership, stale completion suppression, owner/admin delete policy, and documentation consistency.
Scope (out): Product-policy changes, private/public authorization redesign, schema/data changes, unrelated UI semantics, and implementation edits.
DoD: Return PASS or FAIL with severity-ordered, file/line-grounded findings; run focused frontend and backend tests; identify missing counterexample tests or unsupported evidence claims.
Constraints/Forbidden: Do not edit files. Do not inspect or touch protected output artifacts. Do not invoke real download, provider, payment, refund, mail, export, or other external side effects.

[ACCEPTANCE CRITERIA]
Functional:
- [ ] Non-admin delete visibility exactly matches owner + OPEN; ADMIN remains allowed for every status.
- [ ] Attachment request ownership prevents duplicate requests and suppresses stale browser effects/errors after route, owner, refresh, or unmount changes.
- [ ] ADMIN controls expose only the entity's legal outgoing transitions and block conflicting pending mutations.
- [ ] Successful ADMIN mutation uses the response status; failure preserves stable list context and permits retry.
- [ ] Existing create/answer/list/detail/private-public contracts are not weakened.
Quality:
- [ ] Focused frontend tests and QuestionServiceTest pass from a clean command invocation.
- [ ] Tests do not encode impossible backend transitions or rely on vacuous mocks.
- [ ] Use-case documentation states only behavior evidenced by code/tests.

[INPUT POINTERS]
Tier 0:
- docs/standards/core-principles.md
- docs/standards/development-standards.md

Tier 1:
- docs/policies/quality-gates.md
- docs/policies/security-policy.md
- docs/policies/access-control-policy.md

Tier 2:
- docs/standards/frontend-standards.md
- docs/design/usecase/user-question.md
- docs/design/api-spec.md

REQ/Context:
- deliverables/user/REQ-20260809-ATS-001.md
- deliverables/agent/WI-20260809-ATS-047-handoff.md
- deliverables/agent/WI-20260809-ATS-024-findings.md (F-UI-024-006, F-UI-024-011)
- deliverables/agent/WI-20260809-ATS-028-findings.md (F-07)

Files:
- frontend/src/api/questions.ts
- frontend/src/api/domainApis.test.ts
- frontend/src/pages/subscriber/QuestionDetailPage.tsx
- frontend/src/pages/subscriber/QuestionDetailPage.module.css
- frontend/src/pages/subscriber/QuestionDetailPage.test.tsx
- frontend/src/pages/admin/QuestionManagePage.tsx
- frontend/src/pages/admin/QuestionManagePage.module.css
- frontend/src/test/coverage/adminSubscriberPages.coverage.test.tsx
- src/main/java/com/atstudio/atstudio/entity/Question.java
- src/main/java/com/atstudio/atstudio/service/QuestionService.java
- src/test/java/com/atstudio/atstudio/service/QuestionServiceTest.java

[OUTPUT CONTRACT]
- No file writes.
- Final response: PASS/FAIL, findings ordered by severity, exact commands/results, residual risks, and explicit statement of whether WI-047 may proceed to full gates.

[TRACEABILITY REQUIREMENTS]
- Every finding must cite file and line or an exact command/test observation.
- Distinguish implementation defect, missing test, and documentation mismatch.
