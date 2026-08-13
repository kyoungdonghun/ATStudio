[WI HEADER]
WI ID: WI-20260809-ATS-047-POST-RECOVERY-QA
REQ: REQ-20260809-ATS-001
Agent: qa-integ
Depends On: WI-20260809-ATS-047-REMEDIATION-3
Blocks: WI-20260809-ATS-047 closure correction and commit

[WI SUMMARY]
Why: Re-establish an independent final verdict after the previous QA process accidentally overwrote a tracked test source.
Scope (in): Entire WI-047 diff against HEAD, recovered domainApis.test.ts integrity, all prior functional findings, focused tests, and current closure-document accuracy.
Scope (out): Full backend/frontend suite rerun already completed by MA, unrelated DownloadHistoryPage flake repair, or any file edit.
DoD: PASS only when no P0-P2 defect remains, domainApis.test.ts still contains all 15 baseline contract tests plus narrow WI assertions, and no accidental deletion/output artifact is staged as source.
Constraints/Forbidden: Read-only. No outputFile/report-file arguments, file edits, Git writes, protected output access, external side effects, or branch operations.

[ACCEPTANCE CRITERIA]
- [ ] `git diff HEAD -- frontend/src/api/domainApis.test.ts` is a narrow contract-test patch, not a replacement or deletion.
- [ ] Focused frontend command passes 3 suites and 75 tests without writing reporter output.
- [ ] Question owner deletion, attachment lifecycle, legal transitions, canonical response, list ownership, and filter/pageInfo refresh remain correct.
- [ ] Evidence Pack and user summary will be corrected to disclose the overwrite recovery and transient unrelated full-suite retry before commit.
- [ ] No P0-P2 issue remains.

[INPUT POINTERS]
- deliverables/agent/WI-20260809-ATS-047-remediation-3-handoff.md
- deliverables/agent/WI-20260809-ATS-047-evidence-pack.md
- deliverables/user/WI-20260809-ATS-047-summary.md
- frontend/src/api/domainApis.test.ts
- frontend/src/api/questions.ts
- frontend/src/pages/admin/QuestionManagePage.tsx
- frontend/src/pages/subscriber/QuestionDetailPage.tsx
- frontend/src/test/coverage/adminSubscriberPages.coverage.test.tsx
- frontend/src/pages/subscriber/QuestionDetailPage.test.tsx
- docs/design/usecase/user-question.md

[OUTPUT CONTRACT]
- PASS/FAIL, new findings by severity, recovered-file integrity verdict, exact commands/results, residual risks, and commit authorization.
