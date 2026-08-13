[WI HEADER]
WI ID: WI-20260809-ATS-047
REQ: REQ-20260809-ATS-001
Agent: se
Depends On: WI-20260809-ATS-045
Blocks: WI-20260809-ATS-059

[WI SUMMARY]
Why: The Question UI currently exposes owner deletion and ADMIN status mutations that the backend rejects, while attachment downloads have no owned pending/failure lifecycle.
Scope (in): Question detail owner-delete visibility, attachment download pending/error/request ownership, ADMIN legal status controls, canonical status-update response mapping, focused frontend/backend contract tests, and current-state Question use-case documentation.
Scope (out): Question list/navigation behavior, private/public authorization rules (CR-031-105), attachment access policy, the backend Question entity state machine, keyboard semantics assigned to WI-059, schema/data changes, and real external side effects.
DoD: UI controls exactly match the existing backend state machine; owner deletion appears only for an owner in OPEN; one attachment request is owned at a time and visibly recovers from failure; ADMIN state updates consume the canonical response and cannot conflict; focused, adjacent, and full quality gates pass; documentation reflects the verified current contract.
Constraints/Forbidden: Do not weaken authorization, disclose private attachment data, change the legal transition policy, perform database mutations, touch protected output artifacts, merge/delete branches, deploy, or invoke real payment/refund/mail/export/provider/download side effects.

[ACCEPTANCE CRITERIA]
Functional:
- [ ] A non-ADMIN owner sees Delete only while the loaded Question is OPEN; ADMIN policy remains separate.
- [ ] Attachment download awaits completion, disables duplicate submission, shows bounded pending/failure feedback, and ignores stale completion after route/owner/projection replacement.
- [ ] ADMIN status controls offer only OPEN -> IN_PROGRESS/CLOSED, IN_PROGRESS -> RESOLVED/CLOSED, RESOLVED -> CLOSED, and no mutation from CLOSED.
- [ ] While one ADMIN status mutation is pending, conflicting status mutations are unavailable.
- [ ] A successful status mutation applies the status returned by updateQuestionStatus rather than assuming the requested value.
- [ ] Rejected mutations retain the stable row and expose a retryable error without reloading or losing context.
- [ ] Existing Question create, answer, list, detail, private/public authorization, and navigation behavior remains unchanged.
Performance:
- [ ] No additional background polling or unbounded request retention is introduced.
Quality:
- [ ] Focused Question frontend tests pass, including legal/illegal transitions, owner delete gating, duplicate attachment clicks, failure recovery, and stale completion.
- [ ] QuestionService status-transition tests cover every legal edge and representative rejected edges.
- [ ] Frontend typecheck, ESLint, Prettier, build, and full Vitest coverage gates pass.
- [ ] Backend test, JaCoCo verification, and assemble gates pass.
- [ ] Documentation validation and git diff --check pass.

[INPUT POINTERS]
Tier 0 (Constitution - Required for all):
- docs/standards/core-principles.md
- docs/standards/development-standards.md

Tier 1 (Policies):
- docs/policies/quality-gates.md
- docs/policies/security-policy.md
- docs/policies/access-control-policy.md

Tier 2 (Frontend and domain contract):
- docs/standards/frontend-standards.md
- docs/design/usecase/user-question.md
- docs/design/api-spec.md

REQ/Context Docs:
- deliverables/user/REQ-20260809-ATS-001.md
- deliverables/agent/WI-20260809-ATS-031-consolidated-findings.md (CR-031-043, CR-031-048, CR-031-097; WI-047 row)
- deliverables/agent/WI-20260809-ATS-024-findings.md (F-UI-024-006, F-UI-024-011)
- deliverables/agent/WI-20260809-ATS-028-findings.md (F-07)
- deliverables/agent/WI-20260809-ATS-045-evidence-pack.md

Files:
- frontend/src/pages/subscriber/QuestionDetailPage.tsx
- frontend/src/pages/subscriber/QuestionDetailPage.module.css
- frontend/src/pages/subscriber/QuestionDetailPage.test.tsx
- frontend/src/pages/admin/QuestionManagePage.tsx
- frontend/src/pages/admin/QuestionManagePage.module.css
- frontend/src/test/coverage/adminSubscriberPages.coverage.test.tsx
- frontend/src/api/questions.ts
- src/main/java/com/atstudio/atstudio/entity/Question.java
- src/main/java/com/atstudio/atstudio/service/QuestionService.java
- src/test/java/com/atstudio/atstudio/service/QuestionServiceTest.java

Repro/Logs:
- npm test -- --run frontend/src/pages/subscriber/QuestionDetailPage.test.tsx frontend/src/test/coverage/adminSubscriberPages.coverage.test.tsx
- .\gradlew.bat test --tests "com.atstudio.atstudio.service.QuestionServiceTest" --rerun-tasks --no-daemon --max-workers=1 --console=plain

[OUTPUT CONTRACT]
User-facing -> deliverables/user/WI-20260809-ATS-047-summary.md:
- Summarize behavior corrected, retained policy, verification, risks, and follow-up.
Agent-facing -> deliverables/agent/WI-20260809-ATS-047-evidence-pack.md:
- Record evidence pointers, patch notes, exact commands/results, rollback, residual risks, and next WI.
Handoff Packet -> deliverables/agent/WI-20260809-ATS-047-handoff.md:
- This packet.

[TRACEABILITY REQUIREMENTS]
Evidence pointers (files/lines/commands/logs): Required.
Tests: Record focused red/green evidence, adjacent regression, and complete quality-gate results.
Rollback: Revert the WI-047 commit; no schema/data rollback is expected.
