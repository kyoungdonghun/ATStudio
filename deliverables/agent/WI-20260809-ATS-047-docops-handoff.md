[WI HEADER]
WI ID: WI-20260809-ATS-047-DOCOPS
REQ: REQ-20260809-ATS-001
Agent: docops
Depends On: WI-20260809-ATS-047-FINAL-QA and full quality gates
Blocks: WI-20260809-ATS-047 commit and WI-048

[WI SUMMARY]
Why: Close WI-047 with reproducible evidence and an accurate user-facing summary.
Scope (in): Create only the WI-047 Evidence Pack and user summary from the completed diff, QA history, and verified command results.
Scope (out): Any implementation, test, design-document, index, REQ, protected output, or Git modification.
DoD: Both documents exist, agree with the implementation and test reports, disclose residual risk and follow-up boundaries, and pass documentation validation.
Constraints/Forbidden: Do not access output/client-demo-screenshots-20260716-140514.zip or output/ui-ux-audit/. Do not inspect secrets. Do not run real external side effects. Do not edit any file other than the two declared outputs.

[INPUT POINTERS]
- docs/standards/core-principles.md
- docs/standards/documentation-standards.md
- docs/standards/development-standards.md
- docs/standards/glossary.md
- docs/policies/quality-gates.md
- deliverables/agent/WI-20260809-ATS-047-handoff.md
- deliverables/agent/WI-20260809-ATS-047-qa-integ-review-handoff.md
- deliverables/agent/WI-20260809-ATS-047-remediation-handoff.md
- deliverables/agent/WI-20260809-ATS-047-qa-integ-rereview-handoff.md
- deliverables/agent/WI-20260809-ATS-047-remediation-2-handoff.md
- deliverables/agent/WI-20260809-ATS-047-final-qa-handoff.md
- deliverables/agent/WI-20260809-ATS-046-evidence-pack.md
- deliverables/user/WI-20260809-ATS-046-summary.md
- docs/design/usecase/user-question.md
- frontend/src/api/questions.ts
- frontend/src/pages/admin/QuestionManagePage.tsx
- frontend/src/pages/subscriber/QuestionDetailPage.tsx
- frontend/src/api/domainApis.test.ts
- frontend/src/test/coverage/adminSubscriberPages.coverage.test.tsx
- frontend/src/pages/subscriber/QuestionDetailPage.test.tsx
- src/test/java/com/atstudio/atstudio/service/QuestionServiceTest.java
- build/reports/tests/test/index.html
- build/reports/jacoco/test/jacocoTestReport.xml
- frontend/coverage/coverage-summary.json

[VERIFIED FACTS]
- Primary findings closed: CR-031-043, CR-031-048, CR-031-097.
- The Question-list slice of CR-031-096 was pre-resolved because projection ownership was necessary for this WI. License and Track slices remain assigned to WI-053.
- Owner deletion is offered only for OPEN questions; ADMIN deletion remains available for non-OPEN questions.
- Attachment download has single-request locking, retryable error, and stale-completion retirement for route, owner/token, same-detail refresh, and unmount changes.
- ADMIN legal transitions are OPEN -> IN_PROGRESS/CLOSED, IN_PROGRESS -> RESOLVED/CLOSED, RESOLVED -> CLOSED, CLOSED -> none.
- Canonical mutation responses are used. If a status-filtered row leaves the active filter, the list is refreshed so both dataList and pageInfo are authoritative.
- Independent QA round 1 found projection ownership, response-contract, and documentation-surface defects. Remediation closed them.
- Independent QA round 2 found the OPEN-filter -> CLOSED membership/pageInfo counterexample. Remediation added backend refresh and a non-vacuous regression test.
- Final independent QA: PASS, no remaining P0-P2, full gates authorized.
- Focused frontend: 3 suites, 75 tests passed. Focused backend QuestionServiceTest: 37 tests passed.
- Full frontend: 91 files, 1,092 tests passed. Coverage: statements 88.92%, branches 80.95%, functions 89.41%, lines 91.28%.
- Frontend typecheck, ESLint with zero warnings, Prettier, and production build passed.
- Full backend: 1,577 tests, 0 failures, 19 skipped. JaCoCo: instruction 86.957%, branch 72.251%, line 87.228%, method 84.730%; verification and assemble passed.
- Documentation validation passed with 585 traceability IDs; git diff --check passed with only a CRLF-to-LF warning for QuestionServiceTest.java.
- No schema change, data mutation, real provider/mail/download side effect, deployment, merge, or branch deletion occurred.
- Protected output artifacts remain intentionally untouched and untracked.
- Residual risk: a status mutation and its subsequent filtered-list refresh are not one transaction. If refresh fails after backend mutation, the UI shows a retryable load error while the backend state remains committed.

[OUTPUT CONTRACT]
- Create deliverables/agent/WI-20260809-ATS-047-evidence-pack.md in the create-wi-evidence-pack structure, with concrete pointers, exact commands/results, risk, rollback, and follow-ups.
- Create deliverables/user/WI-20260809-ATS-047-summary.md in Korean, concise but complete, matching nearby WI summaries.
- Do not claim live browser/UAT, production readiness, or external provider verification.
- Report the two changed output files and any fact that could not be independently verified.
