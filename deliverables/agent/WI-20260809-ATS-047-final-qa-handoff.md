[WI HEADER]
WI ID: WI-20260809-ATS-047-FINAL-QA
REQ: REQ-20260809-ATS-001
Agent: qa-integ
Depends On: WI-20260809-ATS-047-REMEDIATION-2
Blocks: WI-20260809-ATS-047 full gates and closure

[WI SUMMARY]
Why: Make the final independent blocking/non-blocking verdict after two remediation rounds.
Scope (in): Entire WI-047 diff and all previously reported findings, especially active status-filter membership/pageInfo after mutation, projection ownership, exact response contract, owner/delete and attachment lifecycle tests, and documentation.
Scope (out): Unrelated known roots and full-suite execution.
DoD: PASS only if no P0-P2 defect remains; report any P3/residual risk separately and authorize or deny full gates.
Constraints/Forbidden: Read-only; no file edits, protected output access, real side effects, or git writes.

[ACCEPTANCE CRITERIA]
- [ ] OPEN-filter -> CLOSED refreshes backend dataList and pageInfo; no mismatched row remains.
- [ ] Latest list ownership and detached mutation behavior remain non-vacuously tested.
- [ ] Exact response type and ADMIN list documentation remain correct.
- [ ] Owner OPEN deletion, ADMIN deletion, attachment duplicate/failure/route/owner/unmount/refresh retirement, legal transitions, canonical response, conflict lock, and retry all remain correct.
- [ ] Focused frontend and QuestionService tests pass.

[INPUT POINTERS]
- docs/standards/core-principles.md
- docs/standards/development-standards.md
- docs/policies/quality-gates.md
- docs/standards/frontend-standards.md
- docs/design/usecase/user-question.md
- deliverables/agent/WI-20260809-ATS-047-handoff.md
- deliverables/agent/WI-20260809-ATS-047-qa-integ-review-handoff.md
- deliverables/agent/WI-20260809-ATS-047-remediation-handoff.md
- deliverables/agent/WI-20260809-ATS-047-qa-integ-rereview-handoff.md
- deliverables/agent/WI-20260809-ATS-047-remediation-2-handoff.md
- frontend/src/api/questions.ts
- frontend/src/pages/admin/QuestionManagePage.tsx
- frontend/src/pages/subscriber/QuestionDetailPage.tsx
- frontend/src/api/domainApis.test.ts
- frontend/src/test/coverage/adminSubscriberPages.coverage.test.tsx
- frontend/src/pages/subscriber/QuestionDetailPage.test.tsx
- src/main/java/com/atstudio/atstudio/entity/Question.java
- src/main/java/com/atstudio/atstudio/dto/question/QuestionResponse.java
- src/test/java/com/atstudio/atstudio/service/QuestionServiceTest.java

[OUTPUT CONTRACT]
- PASS/FAIL, disposition of every previous finding, new findings by severity, exact commands/results, residual risks, and full-gate authorization.
- No file writes.

[TRACEABILITY REQUIREMENTS]
- Inspect the OPEN-filter counterexample for backend refresh and pageInfo assertion.
- Run the three focused frontend suites plus QuestionServiceTest and diff check.
