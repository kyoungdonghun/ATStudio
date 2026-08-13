[WI HEADER]
WI ID: WI-20260809-ATS-047-REMEDIATION-3
REQ: REQ-20260809-ATS-001
Agent: MA emergency recovery, followed by qa-integ verification
Depends On: WI-20260809-ATS-047-FINAL-QA
Blocks: WI-20260809-ATS-047 post-recovery final QA and commit

[WI SUMMARY]
Why: The pre-commit staged-diff review found that frontend/src/api/domainApis.test.ts had been replaced by JSON test-name output after the prior final QA. The prior full frontend gate therefore could not be accepted without recovery and rerun.
Scope (in): Restore the tracked test source from HEAD, reapply only the WI-047 Question status-response and attachment-blob assertions, rerun focused/full frontend gates, and independently inspect the recovered diff.
Scope (out): Any production behavior change beyond WI-047, unrelated flaky-test repair, schema/data, protected outputs, external side effects, branch operations, or deployment.
DoD: domainApis.test.ts retains the existing 15 domain contract tests; its WI-047 diff is narrow; focused 75 tests and a complete frontend gate pass; an independent reviewer finds no P0-P2 issue or accidental test deletion.

[RECOVERY EVIDENCE]
- Pre-commit cached diff exposed a 659-line TypeScript test replaced by a 242-line JSON array of test names (242 insertions, 659 deletions).
- The overwrite was not an intended WI change and was caught before commit.
- The file was restored from HEAD and only the exact Question status-update response and Blob-returning attachment contract assertions were reapplied.
- Focused rerun: 3 suites, 75 tests passed.
- First full coverage rerun: one unrelated DownloadHistoryPage empty-state test observed a loading projection and failed; isolated rerun passed 13/13.
- Second full coverage rerun: 91 files, 1,092 tests passed; coverage returned to statements 88.92%, branches 80.95%, functions 89.41%, lines 91.28%.
- Typecheck, ESLint, Prettier, and production build passed after recovery.

[INPUT POINTERS]
- deliverables/agent/WI-20260809-ATS-047-handoff.md
- deliverables/agent/WI-20260809-ATS-047-final-qa-handoff.md
- frontend/src/api/domainApis.test.ts
- frontend/src/api/questions.ts
- frontend/src/pages/admin/QuestionManagePage.tsx
- frontend/src/pages/subscriber/QuestionDetailPage.tsx
- frontend/src/test/coverage/adminSubscriberPages.coverage.test.tsx
- frontend/src/pages/subscriber/QuestionDetailPage.test.tsx
- docs/design/usecase/user-question.md

[OUTPUT CONTRACT]
- Independent QA must inspect git diff HEAD, not only the index, and confirm no test suite/source was deleted or replaced.
- Do not use JSON/JUnit reporters with outputFile and do not write any test output into source paths.
- Read-only verification only; no file edits, Git writes, protected-output access, or external side effects.
