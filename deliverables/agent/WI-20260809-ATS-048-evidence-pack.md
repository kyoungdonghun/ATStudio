# Evidence Pack: WI-20260809-ATS-048

## Summary (one-liner)

- Implemented the bounded Track create/edit/manage and Tag delete-impact acceptance hardening without changing Track deletion policy or taking ownership of WI-053 request ordering.
- Remediated F-QAI-048-001 through F-QAI-048-004; independent QA re-review and the final full gates passed.

## Scope / DoD Check

- [x] Track create and edit advertise MP3/WAV on non-iOS platforms; iOS omits the native picker hint while JavaScript still accepts only MP3/WAV and resets rejected file inputs.
- [x] Track edit validates canonical ID and required metadata before request construction, permits description clearing, and sends explicit Tag replacement intent.
- [x] Backend preserves Tag associations unless `replaceTags=true`; explicit empty and nonempty replacement cases are covered.
- [x] Track management canonicalizes bounded URL state and separates load, delete, and committed-refresh recovery.
- [x] Tag deletion impact is ADMIN-only, bounded to Tag identity plus authoritative association count, and gates destructive confirmation.
- [x] Focused backend/frontend tests, frontend static gates/build, backend build/coverage verification, and docs validation pass.
- [x] Independent QA closed all four findings with no new P0-P2 defect, and the final full gates passed.

## Reference Documents (Tier 0-2)

| Tier | Document | Reason |
|------|----------|--------|
| 0 | `docs/standards/core-principles.md` | System constitution and domain rules |
| 0 | `docs/standards/development-standards.md` | Java/Spring and testing standards |
| 0 | `docs/standards/documentation-standards.md` | Current-state documentation rules |
| 0 | `docs/standards/glossary.md` | Canonical domain terminology |
| 1 | `docs/policies/quality-gates.md` | Required quality gates |
| 1 | `docs/policies/access-control-policy.md` | ADMIN authorization boundary |
| 1 | `docs/policies/security-policy.md` | Security and sensitive-data constraints |
| 2 | `docs/standards/frontend-standards.md` | React/TypeScript implementation rules |
| 2 | `docs/design/usecase/sound-track.md` | Current Track use-case contract |
| 2 | `docs/design/usecase/sound-tag.md` | Current Tag use-case contract |
| 2 | `docs/design/api-spec.md` | Current API inventory and schema boundary |

Additional approved inputs were the parent `deliverables/user/REQ-20260809-ATS-001.md`, the exact CR ranges in WI-031/WI-025/WI-028 findings, WI-039 evidence, and every frontend/backend implementation and test pointer listed in the handoff packet.

## Behavioral Contracts

- Audio: non-iOS inputs use `.mp3,.wav,audio/mpeg,audio/wav,audio/x-wav`; `getAudioAccept()` returns `undefined` on iOS so the native attribute is omitted. Extension validation still accepts only `.mp3` and `.wav`, and rejection clears the native input.
- Track Tag intent: only `Boolean.TRUE.equals(request.getReplaceTags())` replaces associations. True plus no `tagIds` clears all; true plus IDs replaces; omitted/false preserves even if IDs are supplied.
- Track edit: one canonical positive safe integer ID is reused for reads and update. Invalid/missing IDs cause zero Track/Tag requests and expose safe navigation. Title and tonality must be nonblank, BPM must be an integer in `[1, 999]`, and empty description is submitted.
- Track list: malformed URL state is normalized before request; browser navigation restores applied state; failed loads clear stale list state; beyond-last pages canonicalize; delete is single-flight; committed delete recovery retries only the authoritative list read.
- Tag impact: `GET /api/tags/{tagId}/deletion-impact` requires ADMIN and returns only `id`, `name`, `type`, and `trackAssociationCount`. A missing Tag returns the existing stable not-found error before counting.
- Tag delete UI: unused and exact used counts are displayed before confirmation. Invalid/failed impact responses expose retry/close, not destructive confirmation. Existing delete ordering remains unchanged.

## QA Finding Closure Mapping

- `F-QAI-048-001`: remediated by render-time iOS picker omission in `frontend/src/utils/validation.ts:145`, shared use in both Track forms, and the iOS omission plus M4A rejection/reset test at `frontend/src/pages/creator/TrackUploadPage.test.tsx:112`. Desktop MP3/WAV hint assertions remain.
- `F-QAI-048-002`: remediated in `docs/design/usecase/sound-track.md:337` with exact request query `is_active`, distinct response field `isActive`, and all 13 `AdminTrackListItemResponse` fields including `tags: List<TagResponse>`. `docs/design/api-spec.md` now states 151 consistently, and source recount returned GET 76, POST 41, PUT 20, DELETE 14, total 151.
- `F-QAI-048-003`: remediated by restoring established Korean Track management, registration, search, empty, load/delete, confirmation, and invalid-ID recovery copy. State-machine behavior and WI-053 deferral are unchanged.
- `F-QAI-048-004`: remediated by the direct explicit-false preservation test at `src/test/java/com/atstudio/atstudio/service/TrackServiceTest.java:645` and the deferred A/B stale-impact test at `frontend/src/pages/admin/TagManagePage.test.tsx:249`.

Independent QA re-review accepted all four remediation closures and found no new P0-P2 defect. WI-053 remains deferred for generic Track-list latest-request ownership.

## Evidence Pointers

### Backend

- `src/main/java/com/atstudio/atstudio/dto/track/TrackUpdateRequest.java:34` - nullable `replaceTags` multipart field.
- `src/main/java/com/atstudio/atstudio/service/TrackService.java:205` - explicit true replacement gate.
- `src/main/java/com/atstudio/atstudio/dto/tag/TagDeletionImpactResponse.java:7` - bounded response record.
- `src/main/java/com/atstudio/atstudio/repository/TrackTagRepository.java:19` - authoritative association count.
- `src/main/java/com/atstudio/atstudio/service/TagService.java:56` - not-found-first impact lookup and count.
- `src/main/java/com/atstudio/atstudio/controller/TagController.java:63` - ADMIN deletion-impact route.
- `src/test/java/com/atstudio/atstudio/service/TrackServiceTest.java:603` - explicit empty clear; omitted intent preservation begins at line 620.
- `src/test/java/com/atstudio/atstudio/controller/TrackControllerTest.java:193` - multipart intent binding.
- `src/test/java/com/atstudio/atstudio/service/TagServiceTest.java:147` - bounded count and missing-Tag behavior.
- `src/test/java/com/atstudio/atstudio/controller/TagControllerTest.java:150` - 401/403/ADMIN bounded response tests.

### Frontend

- `frontend/src/utils/validation.ts:137` - MP3/WAV extension, label, and picker contract.
- `frontend/src/pages/creator/TrackUploadPage.tsx:121` - rejection/reset handling.
- `frontend/src/pages/creator/TrackEditPage.tsx:56` - canonical route ID; validation and explicit intent are at lines 173-204.
- `frontend/src/pages/admin/TrackManagePage.tsx:20` - URL canonicalization; delete commit/recovery state begins at line 62.
- `frontend/src/api/tags.ts:61` - impact API client.
- `frontend/src/types/index.ts:119` - impact response type.
- `frontend/src/pages/admin/TagManagePage.tsx:145` - impact lookup, response validation, and confirmation gate.
- `frontend/src/pages/creator/TrackEditPage.test.tsx:159` - empty Tag and description-clear request contract.
- `frontend/src/pages/admin/TrackManagePage.test.tsx:121` - load retry, pending delete, and committed-refresh recovery suite.
- `frontend/src/pages/admin/TagManagePage.test.tsx:206` - unused, used-count, and impact-failure confirmation suite.

### Current-State Documentation

- `docs/design/usecase/sound-track.md:230` - update intent and validation; admin list recovery and WI-053 boundary at line 328.
- `docs/design/usecase/sound-tag.md:118` - deletion-impact preflight and confirmation contract.
- `docs/design/api-spec.md:21` - 151-mapping inventory and new request/response contracts.

## Remediation Changed Files

- `src/test/java/com/atstudio/atstudio/service/TrackServiceTest.java`
- `frontend/src/utils/validation.ts`
- `frontend/src/utils/validationHelpers.test.ts`
- `frontend/src/pages/creator/TrackUploadPage.tsx`
- `frontend/src/pages/creator/TrackUploadPage.test.tsx`
- `frontend/src/pages/creator/TrackEditPage.tsx`
- `frontend/src/pages/creator/TrackEditPage.test.tsx`
- `frontend/src/pages/admin/TrackManagePage.tsx`
- `frontend/src/pages/admin/TrackManagePage.test.tsx`
- `frontend/src/pages/admin/TagManagePage.test.tsx`
- `frontend/src/test/coverage/adminSubscriberPages.coverage.test.tsx`
- `docs/design/usecase/sound-track.md`
- `docs/design/api-spec.md`
- `deliverables/agent/WI-20260809-ATS-048-evidence-pack.md`
- `deliverables/user/WI-20260809-ATS-048-summary.md`

## Exact Changed Files

- `src/main/java/com/atstudio/atstudio/controller/TagController.java`
- `src/main/java/com/atstudio/atstudio/dto/tag/TagDeletionImpactResponse.java`
- `src/main/java/com/atstudio/atstudio/dto/track/TrackUpdateRequest.java`
- `src/main/java/com/atstudio/atstudio/repository/TrackTagRepository.java`
- `src/main/java/com/atstudio/atstudio/service/TagService.java`
- `src/main/java/com/atstudio/atstudio/service/TrackService.java`
- `src/test/java/com/atstudio/atstudio/controller/TagControllerTest.java`
- `src/test/java/com/atstudio/atstudio/controller/TrackControllerTest.java`
- `src/test/java/com/atstudio/atstudio/service/TagServiceTest.java`
- `src/test/java/com/atstudio/atstudio/service/TrackServiceTest.java`
- `frontend/src/api/domainApis.test.ts`
- `frontend/src/api/tags.ts`
- `frontend/src/pages/admin/TagManagePage.test.tsx`
- `frontend/src/pages/admin/TagManagePage.tsx`
- `frontend/src/pages/admin/TrackManagePage.module.css`
- `frontend/src/pages/admin/TrackManagePage.test.tsx`
- `frontend/src/pages/admin/TrackManagePage.tsx`
- `frontend/src/pages/creator/TrackEditPage.test.tsx`
- `frontend/src/pages/creator/TrackEditPage.tsx`
- `frontend/src/pages/creator/TrackUploadPage.test.tsx`
- `frontend/src/pages/creator/TrackUploadPage.tsx`
- `frontend/src/test/coverage/adminSubscriberPages.coverage.test.tsx`
- `frontend/src/types/index.ts`
- `frontend/src/utils/validation.ts`
- `frontend/src/utils/validationHelpers.test.ts`
- `docs/design/api-spec.md`
- `docs/design/usecase/sound-tag.md`
- `docs/design/usecase/sound-track.md`
- `deliverables/agent/WI-20260809-ATS-048-evidence-pack.md`
- `deliverables/user/WI-20260809-ATS-048-summary.md`

## Commands & Results

### Final Independent QA And Full Gates (Authoritative)

- Independent QA re-review - PASS; F-QAI-048-001 through F-QAI-048-004 closed, with no new P0-P2 defect.
- Frontend coverage - PASS; 92 files and 1,109 tests. Statements 88.97%, branches 81.11%, functions 89.64%, and lines 91.34%.
- Frontend static/build gates - PASS; typecheck, ESLint with zero warnings, full Prettier, and production build with 286 transformed modules.
- Backend final build - PASS in 3m44s; 184 suites, 1,586 tests, 0 failures, 0 errors, and 19 skipped.
- JaCoCo - PASS; instruction 87.022%, branch 72.251%, line 87.294%, and method 84.862%.
- Documentation validation - PASS; 585 traceability matches. Final diff check - PASS.

### Remediation Focused Verification

- `npm test -- src/utils/validationHelpers.test.ts src/pages/creator/TrackUploadPage.test.tsx src/pages/creator/TrackEditPage.test.tsx src/pages/admin/TrackManagePage.test.tsx src/pages/admin/TagManagePage.test.tsx src/test/coverage/adminSubscriberPages.coverage.test.tsx` from `frontend/` - PASS; 6 files, 83 tests.
- `.\gradlew.bat test --tests "com.atstudio.atstudio.service.TrackServiceTest" --init-script build/tmp/wi048-no-test-reports.init.gradle` - PASS; focused class only. The temporary init script disabled Gradle test HTML/XML reports and was removed after the run. Gradle's separately generated problems HTML was also removed; pre-existing test report timestamps remained unchanged.
- `npm run typecheck` from `frontend/` - PASS; `tsc --noEmit`.
- `npm run lint` from `frontend/` - PASS; zero warnings.
- Targeted `npx prettier --check` over the 10 remediation frontend files - PASS.
- `python .agents/skills/validate-docs/scripts/validate_docs.py` - PASS; Tier 0, internal links, 585 traceability matches, and index checks.
- Focused controller mapping recount - PASS; GET 76, POST 41, PUT 20, DELETE 14, total 151.
- DTO/document semantic cross-check - PASS; all 13 `AdminTrackListItemResponse` fields match and `api-spec.md` contains no stale current/recount value of 150.
- No-Git exact-file static check - PASS; all 15 remediation files decode as strict UTF-8, contain no trailing whitespace, and end with a newline. This replaces `git diff --check` because every Git command is forbidden by the remediation handoff.
- No full suite, coverage run, or build was run as part of remediation; the later authoritative final gate results are recorded above. No external service, real Track/Tag deletion, schema/data mutation, or Git command was run for remediation.

### Original WI-048 Verification (Historical)

The results in this subsection preserve earlier execution history. The final full-gate results above are authoritative.

- `.\gradlew.bat test --tests "com.atstudio.atstudio.service.TrackServiceTest" --tests "com.atstudio.atstudio.controller.TrackControllerTest" --tests "com.atstudio.atstudio.service.TagServiceTest" --tests "com.atstudio.atstudio.service.TagServiceBranchCoverageTest" --tests "com.atstudio.atstudio.controller.TagControllerTest"` - PASS; 5 classes, 101 tests, 0 failures/errors/skips.
- `npm test -- src/utils/validationHelpers.test.ts src/pages/creator/TrackUploadPage.test.tsx src/pages/creator/TrackEditPage.test.tsx src/pages/admin/TrackManagePage.test.tsx src/pages/admin/TagManagePage.test.tsx src/test/coverage/adminSubscriberPages.coverage.test.tsx src/api/domainApis.test.ts` - final PASS; 7 files, 96 tests.
- `npm run typecheck` - PASS.
- `npm run lint` - PASS with zero warnings.
- Targeted `npx prettier --check` over all changed frontend files - PASS after formatting seven changed files.
- `npm run build` - PASS; Vite production build transformed 286 modules.
- `.\gradlew.bat build` - PASS on the bounded retry, 190.5 seconds; 184 suites, 1,585 tests, 0 failures/errors, 19 skipped. The first run hit the 124-second command timeout without a test failure.
- `.\gradlew.bat jacocoTestCoverageVerification` - PASS; configured report and threshold tasks were up to date and no custom reporter output was written.
- `python .agents/skills/validate-docs/scripts/validate_docs.py` - PASS; Tier 0, links, 585 traceability matches, and index checks all passed.

The first frontend invocation used nonexistent `src/test/...` locations and returned "No test files found". The corrected paths above are authoritative. During red/green iteration, three test-selector/native-constraint assumptions failed, the tests were narrowed to the existing UI contract, and the final 96-test run passed.

## Risks / Rollback

- Risks: generic Track-list latest-request ownership remains absent by design and is tracked by WI-053. Independent QA re-review and the final full gates passed; no other intentional Track-list deferral remains in this WI.
- Rollback: revert the listed source/test/doc files as one WI unit. No schema or data rollback is required because this WI adds no migration and performs no real Track/Tag deletion.
- Protected state: protected output remained untouched, and no ignored secrets or local environment values were inspected. No real Track/Tag deletion, schema migration, provider/mail/download action, or other external effect occurred; no Git operation was performed.

## CR Disposition

- CR-031-056: implemented by shared MP3/WAV picker/validation/reset behavior.
- CR-031-057: implemented by explicit Track edit validation, description clearing, Tag replacement intent, and canonical ID handling.
- CR-031-061 Track slice: implemented for Track ID only; Album/Notice ID slices remain in their assigned WIs.
- CR-031-063 bounded non-latest-request portion: implemented for URL normalization and load/delete recovery. Generic latest-request ownership remains WI-053.
- CR-031-064: implemented through the bounded ADMIN impact DTO and informed confirmation gate.
- CR-031-054 Track deletion policy: unchanged.
