# Evidence Pack: WI-20260809-ATS-047

## Summary (one-liner)

- Completed the Question delete, attachment-download, ADMIN status-transition, and list-projection contract, recovered an uncommitted test-source overwrite, preserved the complete failure/recovery chronology, and received terminal post-correction documentation QA **PASS** with no P0-P2 finding and commit **AUTHORIZED**.

## Scope / DoD Check

- DoD items:
  - [x] A non-ADMIN owner is offered deletion only for an `OPEN` Question; ADMIN deletion remains available for non-`OPEN` Questions.
  - [x] Attachment download owns one request, exposes bounded pending/failure feedback, permits retry, and retires stale completion across route, owner/token, same-detail refresh, and unmount boundaries.
  - [x] ADMIN status controls expose only the backend's legal transitions and block conflicting mutations while one is pending.
  - [x] Status mutation success consumes the canonical response; a status-filtered projection is refreshed when the returned status no longer belongs to the active filter.
  - [x] Latest-owned Question list requests protect `dataList` and `pageInfo` from older responses.
  - [x] `CR-031-043`, `CR-031-048`, and `CR-031-097` are closed.
  - [x] The Question slice of `CR-031-096` is pre-resolved; the License and Track slices remain assigned to WI-20260809-ATS-053.
  - [x] Two independent QA failures were remediated, and the pre-recovery final independent QA returned PASS with no remaining P0-P2 functional finding.
  - [x] Remediation 3 restored `frontend/src/api/domainApis.test.ts` from HEAD after an uncommitted JSON overwrite, retained all 15 baseline test names, and left only a narrow `+18/-15` Question API contract patch in one test block.
  - [x] Focused and recovered full frontend gates passed; the earlier backend, documentation-validation, and diff-validation gates also passed according to the supplied handoffs and persisted reports.
  - [x] Terminal closure PASS is established: post-recovery QA passed functional and recovery integrity but returned overall FAIL and denied commit solely because the two closure documents had not recorded the recovery; after that correction, final documentation QA returned **PASS**, found no P0-P2 issue, and authorized commit.

## Reference Documents (Tier 0-2)

**Injected Context** (from `deliverables/agent/WI-20260809-ATS-047-handoff.md:33-53` and the DocOps closure handoff):

| Tier | Document | Reason |
|------|----------|--------|
| 0 | `docs/standards/core-principles.md` | Constitution required for all agents |
| 0 | `docs/standards/documentation-standards.md` | DocOps structure and language policy |
| 0 | `docs/standards/development-standards.md` | Implementation and test standards |
| 0 | `docs/standards/glossary.md` | Canonical terminology |
| 1 | `docs/policies/quality-gates.md` | Required closure gates |
| 1 | `docs/policies/security-policy.md` | Authorization and sensitive-data boundary |
| 1 | `docs/policies/access-control-policy.md` | Question owner and ADMIN policy boundary |
| 2 | `docs/standards/frontend-standards.md` | Frontend request and projection lifecycle |
| 2 | `docs/design/usecase/user-question.md` | Current Question use-case contract |
| 2 | `docs/design/api-spec.md` | Question API contract |

**Additional closure context**:

- `deliverables/user/REQ-20260809-ATS-001.md`
- `deliverables/agent/WI-20260809-ATS-031-consolidated-findings.md`
- `deliverables/agent/WI-20260809-ATS-024-findings.md`
- `deliverables/agent/WI-20260809-ATS-028-findings.md`
- `deliverables/agent/WI-20260809-ATS-045-evidence-pack.md`
- `deliverables/agent/WI-20260809-ATS-046-evidence-pack.md`

**Injection Rules Applied**:

- Rule source: `.claude/config/context-injection-rules.json`
- Assignee chain: `se` -> `qa-integ` -> `se` -> `qa-integ` -> `se` -> pre-recovery final independent `qa-integ` -> `docops` -> remediation 3 recovery -> post-recovery `qa-integ` -> `docops` correction
- Task type: frontend/backend contract implementation, independent integration review, remediation, recovery integrity review, and documentation correction
- Required tiers: Tier 0, Question-relevant Tier 1 policy, and Question-specific Tier 2 context

## Evidence Pointers

### Closure files changed

- `deliverables/agent/WI-20260809-ATS-047-final-doc-qa-result.md` - terminal post-correction documentation QA verdict, validations, and commit authorization.
- `deliverables/agent/WI-20260809-ATS-047-evidence-pack.md` - reproducible agent-facing closure evidence.
- `deliverables/user/WI-20260809-ATS-047-summary.md` - Korean user-facing outcome, QA history, verification, residual risk, and follow-up boundary.

### Recovery and post-recovery review

- `deliverables/agent/WI-20260809-ATS-047-remediation-3-handoff.md:8-21` records the accidental JSON overwrite discovery, HEAD restoration, focused/full reruns, isolated DownloadHistoryPage result, and unchanged coverage values.
- `deliverables/agent/WI-20260809-ATS-047-docops-correction-handoff.md:22-30` records the recovered test-source integrity (`+18/-15` in one test block and all 15 baseline test names retained) and the post-recovery QA decision boundary.
- `deliverables/agent/WI-20260809-ATS-047-post-recovery-final-qa-handoff.md:8-29` defines the recovery-integrity and closure-document acceptance criteria applied by the post-recovery review.

### Question API and ADMIN list contract

- `frontend/src/api/questions.ts:41-53` defines the paged list shape and exact status-update response projection.
- `frontend/src/api/questions.ts:108-118` returns the canonical `QuestionStatusUpdateResponse` from `PUT /questions/{id}/status`.
- `frontend/src/pages/admin/QuestionManagePage.tsx:46-63` maps only legal transitions and keys a list projection by page/category/status.
- `frontend/src/pages/admin/QuestionManagePage.tsx:97-148` aborts the prior list request and admits a result only when its generation and projection key remain current.
- `frontend/src/pages/admin/QuestionManagePage.tsx:166-201` locks conflicting mutations, consumes the returned status, refreshes a detached or filter-invalidated projection, and limits failure feedback to the initiating projection.
- `frontend/src/pages/admin/QuestionManagePage.tsx:286-301` disables all status controls during a mutation and disables terminal `CLOSED` rows.
- `src/main/java/com/atstudio/atstudio/entity/Question.java:46-60` is the authoritative transition state machine.
- `src/main/java/com/atstudio/atstudio/service/QuestionService.java:168-174` commits the backend status mutation and returns `QuestionResponse.fromStatusUpdate`.
- `src/main/java/com/atstudio/atstudio/dto/question/QuestionResponse.java:71-84` defines the backend status-update projection consumed by the frontend type.

### Owner deletion and attachment lifecycle

- `frontend/src/pages/subscriber/QuestionDetailPage.tsx:112-171` replaces detail reads and retires attachment ownership on refresh, route, owner/token, and unmount boundaries.
- `frontend/src/pages/subscriber/QuestionDetailPage.tsx:210-255` owns one attachment request, suppresses stale browser effects, exposes retryable failure, and restores controls.
- `frontend/src/pages/subscriber/QuestionDetailPage.tsx:288-296` separates ADMIN deletion from non-ADMIN owner plus `OPEN` gating.
- `frontend/src/pages/subscriber/QuestionDetailPage.tsx:323-350` renders pending and retryable attachment states while disabling duplicate actions.
- `frontend/src/pages/subscriber/QuestionDetailPage.tsx:410-423` renders deletion only when the verified policy permits it.
- `src/main/java/com/atstudio/atstudio/service/QuestionService.java:178-187` preserves the backend owner-`OPEN` or ADMIN deletion rule.

### Focused regression tests

- `frontend/src/api/domainApis.test.ts:450-500` verifies the exact status request, response unwrapping, and frontend response type.
- `frontend/src/test/coverage/adminSubscriberPages.coverage.test.tsx:547-691` verifies every legal transition, canonical response use, and the non-vacuous `OPEN`-filter -> `CLOSED` backend refresh including replacement `dataList` and `pageInfo`.
- `frontend/src/test/coverage/adminSubscriberPages.coverage.test.tsx:728-978` verifies pending-conflict locking, latest list ownership, detached-success refresh, and detached-failure suppression.
- `frontend/src/pages/subscriber/QuestionDetailPage.test.tsx:130-168` verifies owner deletion gating and separate ADMIN deletion.
- `frontend/src/pages/subscriber/QuestionDetailPage.test.tsx:170-308` verifies duplicate attachment locking, failure/retry, and stale retirement for route, owner/token, unmount, and same-detail refresh.
- `src/test/java/com/atstudio/atstudio/service/QuestionServiceTest.java:470-516` covers all five legal edges and representative rejected transitions.
- `src/test/java/com/atstudio/atstudio/service/QuestionServiceTest.java:536-630` covers owner-`OPEN`, ADMIN, and rejected non-`OPEN` owner deletion.

### Current-state documentation and finding boundary

- `docs/design/usecase/user-question.md:121-149` records the owned attachment-download lifecycle and retry behavior.
- `docs/design/usecase/user-question.md:155-181` records owner-`OPEN` versus ADMIN deletion.
- `docs/design/usecase/user-question.md:185-214` identifies the ADMIN inquiry list as the status-control surface and records the legal state machine.
- `deliverables/agent/WI-20260809-ATS-031-consolidated-findings.md:607-612` identifies `CR-031-043` and `CR-031-048`; `:660-661` identifies `CR-031-096` and `CR-031-097`.
- `deliverables/agent/WI-20260809-ATS-028-findings.md:108-116` identifies the three `CR-031-096` collections as License, Question, and Track.
- `deliverables/agent/WI-20260809-ATS-031-consolidated-findings.md:987` retains ADMIN License/Question/Track list ownership in WI-053; WI-047 pre-resolves only its Question slice.

## Independent QA and Remediation Chain

| Stage | Decision and evidence |
|------|------------------------|
| Independent QA round 1 | **FAIL**: Question-list projection ownership, the frontend status-response contract, and the documented ADMIN UI surface were defective. See `deliverables/agent/WI-20260809-ATS-047-remediation-handoff.md:8-12`. |
| Remediation 1 | Added latest-owned list/mutation projection handling, an exact response type and API assertion, and corrected QUESTION-007 to the ADMIN inquiry list. This necessarily pre-resolved only the Question slice of `CR-031-096`; License and Track remained out of scope. See `deliverables/agent/WI-20260809-ATS-047-remediation-handoff.md:15-23` and `:38-44`. |
| Independent QA round 2 | **FAIL**: same-projection `OPEN` -> `CLOSED` could leave a `CLOSED` row and stale `pageInfo` inside the active `OPEN` filter. See `deliverables/agent/WI-20260809-ATS-047-remediation-2-handoff.md:8-12`. |
| Remediation 2 | Added a backend refresh whenever the canonical returned status leaves the active filter and a counterexample that verifies replacement `dataList` and `pageInfo`, not local row removal. See `deliverables/agent/WI-20260809-ATS-047-remediation-2-handoff.md:15-21` and `frontend/src/test/coverage/adminSubscriberPages.coverage.test.tsx:628-691`. |
| Pre-recovery final independent QA | **PASS**: no remaining P0-P2 functional finding; full quality gates were authorized. This was the valid decision before the later test-source overwrite was discovered. The decision is recorded in `deliverables/agent/WI-20260809-ATS-047-docops-handoff.md` under `[VERIFIED FACTS]`. |
| Remediation 3: overwrite discovery and recovery | Pre-commit staged-diff review found `frontend/src/api/domainApis.test.ts` replaced by a JSON test-name array. The overwrite was not committed. The source was restored from HEAD, then only the Question API contract assertions were reapplied as a narrow `+18/-15` patch in one test block; all 15 baseline test names were retained. See `deliverables/agent/WI-20260809-ATS-047-remediation-3-handoff.md:8-21` and `deliverables/agent/WI-20260809-ATS-047-docops-correction-handoff.md:22-30`. |
| First post-recovery full coverage attempt | **FAIL**: an unrelated `DownloadHistoryPage` empty-state test observed the loading state while rendering. This was not attributed to the WI-047 Question changes. |
| Isolated DownloadHistoryPage rerun | **PASS**: `npm test -- --run src/pages/subscriber/DownloadHistoryPage.test.tsx` from `frontend/` passed 1 suite and 13/13 tests. |
| Second post-recovery full coverage rerun | **PASS**: 91 files and 1,092 tests passed; coverage remained statements 88.92%, branches 80.95%, functions 89.41%, and lines 91.28%. |
| Post-recovery independent QA | Functional integrity **PASS** and recovered-test-source integrity **PASS**, but overall **FAIL** with commit denied solely because the Evidence Pack and user summary did not yet record remediation 3 and the rerun sequence. This is not a terminal PASS; final post-correction documentation QA is still required. See `deliverables/agent/WI-20260809-ATS-047-docops-correction-handoff.md:28-29`. |
| Final post-correction documentation QA | **PASS**: P0 none, P1 none, and P2 none; all recovery-disclosure criteria passed; `domainApis.test.ts` remained a narrow one-block `+18/-15` patch with all 15 baseline test names preserved; documentation validation and `git diff HEAD --check` passed; commit **AUTHORIZED**. See `deliverables/agent/WI-20260809-ATS-047-final-doc-qa-result.md`. |

## Commands & Outputs

The execution results below are the completed WI and remediation 3 results supplied to DocOps. This documentation correction did not rerun build, mutation, Git, browser, provider, mail, or download commands.

| Command | Result |
|---------|--------|
| `npm test -- src/api/domainApis.test.ts src/test/coverage/adminSubscriberPages.coverage.test.tsx src/pages/subscriber/QuestionDetailPage.test.tsx` from `frontend/` | PASS: 3 suites, 75 tests |
| `.\gradlew.bat test --tests "com.atstudio.atstudio.service.QuestionServiceTest" --rerun-tasks --no-daemon --max-workers=1 --console=plain` | PASS: 37 tests |
| First post-recovery `npm run test:coverage` from `frontend/` | FAIL: unrelated `DownloadHistoryPage` empty-state test rendered the loading state |
| `npm test -- --run src/pages/subscriber/DownloadHistoryPage.test.tsx` from `frontend/` | PASS: 1 suite, 13/13 tests |
| Second post-recovery `npm run test:coverage` from `frontend/` | PASS: 91 test files, 1,092 tests; coverage values unchanged |
| `npm run typecheck` | PASS |
| `npm run lint` | PASS with zero warnings |
| `npm run format` | PASS |
| `npm run build` | PASS |
| `.\gradlew.bat test jacocoTestReport jacocoTestCoverageVerification assemble --rerun-tasks --no-daemon --max-workers=1 --console=plain` | PASS: 1,577 tests, 0 failures, 19 skipped; verification and assemble passed |
| `python .agents/skills/validate-docs/scripts/validate_docs.py` | PASS: Tier 0, internal links, 585 traceability IDs, and document index |
| `git diff --check` | PASS; only a CRLF-to-LF warning for `QuestionServiceTest.java` was reported |
| Terminal QA: `python .agents/skills/validate-docs/scripts/validate_docs.py` | PASS: Tier 0, internal links, 585 traceability IDs, and document index |
| Terminal QA: `git diff HEAD --check` | PASS: exit 0, no output or warning |

### Closure-time report reconciliation

- `build/reports/tests/test/index.html:41-54` reports 1,577 tests, 0 failures, and 19 skipped. The `build/test-results/test/TEST-*.xml` aggregate is 1,558 passed, 19 skipped, 0 failures, and 0 errors.
- The seven `build/test-results/test/TEST-com.atstudio.atstudio.service.QuestionServiceTest*.xml` suites total 37 passed, 0 skipped, 0 failures, and 0 errors.
- `build/reports/jacoco/test/jacocoTestReport.xml` counters reproduce the percentages below.
- `frontend/coverage/coverage-summary.json` reproduces the frontend coverage percentages and covered/total counts below.
- The focused/full Vitest results, isolated 13/13 rerun, typecheck, ESLint, Prettier, build, documentation validation, and diff-check outcomes are supplied results recorded in the original DocOps and remediation 3 handoffs; no persistent Vitest execution report was declared as an input pointer.

## Test and Coverage Results

### Frontend

- Focused completion result: 3 suites, 75 tests, all passed.
- First post-recovery full coverage attempt: one unrelated `DownloadHistoryPage` empty-state test failed after observing the loading state.
- Isolated `DownloadHistoryPage` rerun: `npm test -- --run src/pages/subscriber/DownloadHistoryPage.test.tsx` from `frontend/` passed 1 suite and 13/13 tests.
- Second post-recovery full coverage rerun: 91 test files, 1,092 tests, all passed with unchanged coverage values.
- Coverage source: `frontend/coverage/coverage-summary.json`.

| Metric | Result |
|--------|--------|
| Statements | 88.92% (9,056/10,184) |
| Branches | 80.95% (5,914/7,305) |
| Functions | 89.41% (2,137/2,390) |
| Lines | 91.28% (8,333/9,129) |

### Backend

- Full test source: `build/reports/tests/test/index.html` and `build/test-results/test/TEST-*.xml`.
- Result: 1,577 total; 1,558 passed; 19 skipped; 0 failures; 0 errors.
- Focused `QuestionServiceTest`: 37/37 passed across seven XML suites.
- Coverage source: `build/reports/jacoco/test/jacocoTestReport.xml`.

| Metric | Result |
|--------|--------|
| Instruction | 86.957% (45,427 covered / 6,814 missed) |
| Branch | 72.251% (3,463 covered / 1,330 missed) |
| Line | 87.228% (10,108 covered / 1,480 missed) |
| Method | 84.730% (1,770 covered / 319 missed) |
| Class | 94.824% (403 covered / 22 missed) |

## Risks / Residuals / Rollback

- Residuals:
  - The status mutation and its subsequent filtered-list refresh are separate HTTP requests, not one transaction. If the mutation commits and the refresh then fails, backend state remains committed while the UI shows a retryable list-load error. A retry can re-read authoritative `dataList` and `pageInfo`; the frontend cannot roll back the committed backend mutation.
  - `CR-031-096` is not globally closed: only the Question slice is pre-resolved here. License and Track latest-request ownership remains in WI-20260809-ATS-053.
  - Question keyboard/render semantics remain assigned to WI-20260809-ATS-059.
  - No live browser/UAT, production-readiness, deployment, external provider, mail, or real browser-download verification is claimed.
  - No schema change, data mutation, real external side effect, merge, or branch deletion occurred.
  - Protected output artifacts remained intentionally untouched and untracked.
  - Post-recovery QA did not authorize commit: although functional and recovery integrity passed, overall QA remained FAIL until these documents were corrected. Final post-correction documentation QA subsequently passed with no P0-P2 finding and authorized commit.
- Rollback:
  - Revert the WI-047 implementation/test/documentation patch as one unit using its eventual WI commit.
  - Remove `deliverables/agent/WI-20260809-ATS-047-evidence-pack.md` and `deliverables/user/WI-20260809-ATS-047-summary.md` if the closure documents are rolled back.
  - No schema, database, provider, or persisted-data rollback is required for this documentation closure.

## Follow-ups

- Final post-correction documentation QA is complete with terminal **PASS** and commit **AUTHORIZED**; the permanent result is `deliverables/agent/WI-20260809-ATS-047-final-doc-qa-result.md`.
- Complete the License and Track slices of `CR-031-096` under WI-20260809-ATS-053 without reopening the pre-resolved Question slice.
- Continue Question keyboard/render semantics under WI-20260809-ATS-059.
- Preserve the non-transactional mutation/refresh failure mode as an explicit operational risk in later ADMIN list work.
