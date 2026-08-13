[WI HEADER]
WI ID: WI-20260809-ATS-047-QA-INTEG-REREVIEW
REQ: REQ-20260809-ATS-001
Agent: qa-integ
Depends On: WI-20260809-ATS-047-REMEDIATION
Blocks: WI-20260809-ATS-047 evidence and closure

[WI SUMMARY]
Why: Verify that the three findings from the first independent review are fully corrected without regression or scope drift.
Scope (in): Recheck projection-owned list/mutation behavior, exact status-update response typing and API contract test, corrected ADMIN list documentation surface, original delete/attachment/status requirements, and focused tests.
Scope (out): New findings unrelated to changed Question behavior, License/Track portions of CR-031-096, policy redesign, implementation edits, and full-suite gates.
DoD: PASS or FAIL with exact evidence; explicitly disposition each original finding and state whether WI-047 may proceed to full gates.
Constraints/Forbidden: Read-only. Do not edit files, touch protected output, or invoke real external effects.

[ACCEPTANCE CRITERIA]
- [ ] Original P2: older list responses cannot overwrite newer projections; detached mutation success refreshes the current projection; detached failure does not leak.
- [ ] Original P3: updateQuestionStatus uses an exact response type and API test verifies request plus returned projection.
- [ ] Original P3: QUESTION-007 identifies the ADMIN inquiry list.
- [ ] Original owner-delete, attachment ownership, legal transitions, canonical response, failure/retry, and pending-conflict behaviors remain correct.
- [ ] Focused frontend and QuestionService tests pass.
- [ ] Any residual risk is separated from a blocking defect.

[INPUT POINTERS]
Tier 0:
- docs/standards/core-principles.md
- docs/standards/development-standards.md
Tier 1:
- docs/policies/quality-gates.md
Context:
- deliverables/agent/WI-20260809-ATS-047-handoff.md
- deliverables/agent/WI-20260809-ATS-047-qa-integ-review-handoff.md
- deliverables/agent/WI-20260809-ATS-047-remediation-handoff.md
Files:
- frontend/src/pages/admin/QuestionManagePage.tsx
- frontend/src/pages/subscriber/QuestionDetailPage.tsx
- frontend/src/api/questions.ts
- frontend/src/test/coverage/adminSubscriberPages.coverage.test.tsx
- frontend/src/pages/subscriber/QuestionDetailPage.test.tsx
- frontend/src/api/domainApis.test.ts
- src/main/java/com/atstudio/atstudio/entity/Question.java
- src/main/java/com/atstudio/atstudio/dto/question/QuestionResponse.java
- src/test/java/com/atstudio/atstudio/service/QuestionServiceTest.java
- docs/design/usecase/user-question.md

[OUTPUT CONTRACT]
- No file writes.
- PASS/FAIL, disposition of all original findings, any new findings with file/line, exact test commands/results, and proceed/do-not-proceed verdict.

[TRACEABILITY REQUIREMENTS]
- Re-run the focused frontend set including domainApis.test.ts and QuestionServiceTest.
- Inspect the deferred-race tests rather than relying only on their pass result.
