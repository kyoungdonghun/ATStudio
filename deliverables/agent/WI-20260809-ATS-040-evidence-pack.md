---
version: 1.2
last_updated: 2026-08-13
project: ATS
owner: se
category: evidence-pack
status: complete
dependencies:
  - path: WI-20260809-ATS-040-handoff.md
    reason: Approved Work Item scope and output contract
  - path: ../../deliverables/user/REQ-20260809-ATS-001.md
    reason: Approved parent Request
---

# Evidence Pack: WI-20260809-ATS-040

## Summary

ADMIN Whitelist export now confirms the applied scope and durable status effect, quarantines stale rows after a current list failure, and recovers an ambiguous POST through an owner-scoped, exact-scope, bounded recent-batch summary read without repeating the export mutation.

## Scope and DoD Check

- [x] Confirmation uses the applied status and keyword and excludes draft keyword text.
- [x] ALL plus keyword includes every matching status and discloses that matching `PENDING` rows become `EXPORTED` while other statuses remain unchanged.
- [x] Explicit `PENDING` and non-`PENDING` confirmation effects match backend behavior.
- [x] A failed current list request clears rows, pagination, and pending edits.
- [x] Stale list success, failure, and completion paths cannot replace newer state.
- [x] Recent history is authenticated-ADMIN-owned, exact normalized scope, service-guarded to a 100-character trimmed keyword, newest-first, and repository-bounded to 10 summaries.
- [x] Recent responses expose only batch ID, filename, item count, recorded status/keyword, and creation time.
- [x] Ambiguous POST handling performs one recent GET and no second export POST, including authentication replay opt-out.
- [x] Definitive 4xx handling remains a normal failure without a commit claim.
- [x] Existing known-batch byte replay and CSV contents are unchanged.
- [x] Focused backend/frontend tests and targeted frontend static checks passed.
- [x] Current API, DB, Whitelist use case, and security documents were updated and validated.
- [x] Independent QA-INTEG review returned PASS with no P1/P2 findings; both P3 recommendations were remediated and the focused backend result became 22/22.
- [x] Main full backend and frontend gates, coverage thresholds, assembly, build, static checks, documentation validation, and diff validation passed.

## Reference Documents

| Tier | Document | Use |
|---|---|---|
| 0 | `docs/standards/core-principles.md` | Approved execution, traceability, and REST-first boundary |
| 0 | `docs/standards/development-standards.md` | Layering, TDD, and test evidence |
| 0 | `docs/standards/documentation-standards.md` | Current-document and deliverable format |
| 0 | `docs/standards/glossary.md` | Canonical ATStudio terms |
| 1 | `docs/policies/security-policy.md` | ADMIN ownership, privacy minimization, and no automatic mutation retry |
| 1 | `docs/policies/quality-gates.md` | Focused validation, independent review result, and final Main gates |
| 1 | `docs/standards/frontend-standards.md` | Axios, local page state, request ownership, and CSS Modules |
| 2 | `docs/design/api-spec.md` | Whitelist export and replay contracts |
| 2 | `docs/design/db-schema.md` | Existing batch metadata ownership; no schema change |
| 2 | `docs/design/usecase/whitelist.md` | WL-008 applied-scope and recovery workflow |

Finding context: `deliverables/agent/WI-20260809-ATS-029-findings.md` B01/B02 and `deliverables/agent/WI-20260809-ATS-031-consolidated-findings.md` roots `CR-031-075`, `CR-031-076`, and `CR-031-112`.

## Design Choices

1. `GET /api/admin/whitelist-channels/exports/recent` derives the owner from `CustomUserDetails`; the client cannot supply an ADMIN ID.
2. Scope normalization preserves the export contract: blank becomes null, surrounding whitespace is trimmed, a non-null trimmed keyword is rejected above 100 characters before any repository call, status must match exactly, and keyword scope is compared case-insensitively after `Locale.ROOT` folding.
3. The repository returns a constructor projection ordered by `createdAt DESC, id DESC` with `PageRequest.of(0, 10)`. It never joins batch items or constructs CSV bytes.
4. The SPA issues no automatic retry. Export opts out of shared 401 authentication replay; an ambiguous rejection triggers one exact-scope recent GET, and download occurs only after an explicit candidate replay action.
5. Current list failure and ambiguous mutation outcome both remove actionable rows. The existing request generation fence prevents stale completions from restoring or clearing newer state.

## Evidence Pointers

### Backend

- `src/main/java/com/atstudio/atstudio/dto/whitelist/AdminWhitelistExportSummaryResponse.java` - Summary-only wire contract.
- `src/main/java/com/atstudio/atstudio/repository/WhitelistExportBatchRepository.java:14-35` - Owner/scope projection query, deterministic order, and pageable bound.
- `src/main/java/com/atstudio/atstudio/service/AdminWhitelistChannelService.java:226-248` - Scope validation including the 100-character service guard, normalization, authenticated owner ID, and maximum 10.
- `src/main/java/com/atstudio/atstudio/controller/AdminWhitelistChannelController.java:78-86` - ADMIN-only recent endpoint and `dataList` response.

### Frontend

- `frontend/src/api/admin.ts:206-232` - Summary type, export authentication-replay opt-out, and recent-summary GET.
- `frontend/src/pages/admin/WhitelistChannelManagePage.tsx:68-85` - Applied-scope confirmation and definitive-4xx classification.
- `frontend/src/pages/admin/WhitelistChannelManagePage.tsx:109-139` - Latest-request ownership and failed-list quarantine.
- `frontend/src/pages/admin/WhitelistChannelManagePage.tsx:204-258` - Single export POST, ambiguous recovery GET, and zero automatic retry.
- `frontend/src/pages/admin/WhitelistChannelManagePage.tsx:350-385` - Candidate identity/time/count display and explicit replay only.
- `frontend/src/pages/admin/WhitelistChannelManagePage.module.css` - Responsive, unframed recovery list styling.

### Tests

- `src/test/java/com/atstudio/atstudio/repository/WhitelistExportBatchRepositoryTest.java:35-121` - Actor/scope isolation, null ALL scope, case normalization, `createdAt` then ID ordering, projection, and 10-row bound under H2.
- `src/test/java/com/atstudio/atstudio/service/AdminWhitelistChannelServiceTest.java:287-332` - ALL plus keyword mixed-status regression: only `PENDING` transitions and each item snapshots its status at export.
- `src/test/java/com/atstudio/atstudio/service/AdminWhitelistChannelServiceTest.java:437-496` - Owner ID, exact normalized scope, repository bound, unscoped rejection, 101-character rejection, and no batch/item repository access on invalid scope.
- `src/test/java/com/atstudio/atstudio/controller/AdminWhitelistChannelControllerTest.java:76-115` - Summary-only JSON and non-ADMIN rejection.
- `frontend/src/api/adminWhitelistChannels.test.ts:25-104` - Exact request scope, `skipAuthReplay`, summary GET, and existing replay identity.
- `frontend/src/pages/admin/WhitelistChannelManagePage.render.test.tsx:109-316` - Request races, current-failure quarantine, applied/draft scope, mutation disclosure, ambiguous recovery, explicit replay, and definitive 4xx behavior.
- `frontend/src/test/coverage/adminSubscriberGaps.coverage.test.tsx:887-974` - Existing adjacent export, replay, failure, status, and search regressions.

### Current Documentation

- `docs/design/api-spec.md:395-440` - Mapping inventory and complete recovery contract.
- `docs/design/db-schema.md` Whitelist Export section - Existing metadata-only read and no schema change.
- `docs/design/usecase/whitelist.md:257-302` - Applied confirmation, durable mutation, response-loss recovery, and residual ambiguity.
- `docs/policies/security-policy.md:207-214` - Owner-scoped summary minimization and no automatic POST replay.
- `docs/index.md` and `docs/registry/project-registry.md` - Current REST mapping count and API/DB specification versions synchronized.

## Red and Green Proof

### Red

Backend focused command before implementation failed in `compileTestJava` with 16 expected missing-symbol errors for the summary DTO, repository query, and service method; `BUILD FAILED in 12s`.

Frontend focused RED ran 21 tests with 5 expected failures: missing recent API, absent current-failure quarantine, old confirmation copy, and absent ambiguous recovery UI.

### Green Backend

```powershell
.\gradlew.bat test --tests "com.atstudio.atstudio.service.AdminWhitelistChannelServiceTest" --tests "com.atstudio.atstudio.repository.WhitelistExportBatchRepositoryTest" --tests "com.atstudio.atstudio.controller.AdminWhitelistChannelControllerTest"
```

Result after independent-review remediation: `BUILD SUCCESSFUL in 33s`; 22 tests passed, 0 failed, 0 skipped.

| Suite | Result |
|---|---:|
| `AdminWhitelistChannelServiceTest` | 16/16 passed |
| `WhitelistExportBatchRepositoryTest` | 2/2 passed |
| `AdminWhitelistChannelControllerTest` | 4/4 passed |

### Independent Review P3 Remediation

Independent QA-INTEG review returned PASS with no P1/P2 findings and two P3 recommendations:

1. Recent lookup previously relied on request-side parity without its own 100-character keyword bound. The service now trims first and throws the existing `BusinessException(BUSINESS_ERROR.INVALID_ARGUMENT)` for 101 or more characters before either export repository is touched.
2. The production ALL plus keyword status behavior was already correct but lacked a direct mixed-candidate service regression. The added test proves a matching `PENDING` channel becomes `EXPORTED`, a matching `REGISTERED` channel remains `REGISTERED`, and both immutable export items capture their original statuses. Production status logic was not changed.

### Green Frontend

```powershell
npm test -- --run src/api/adminWhitelistChannels.test.ts src/pages/admin/WhitelistChannelManagePage.render.test.tsx src/test/coverage/adminSubscriberGaps.coverage.test.tsx
```

Result: 3 files, 45 tests passed, 0 failed.

Targeted static checks:

- `npx tsc --noEmit` - PASS.
- Changed-file `npx eslint ... --max-warnings 0` - PASS.
- Changed-file `npx prettier --check ...` - PASS.

Vitest printed the existing JSDOM note `Not implemented: navigation to another Document` while Blob-download tests passed; it is not a test failure.

## Final Main Gate Results

The first Main full backend rerun ended only in a Gradle `NoSuchFileException` for a temporary test binary file under `build/test-results`; it did not report a test or coverage failure. After closing agents, stopping Gradle daemons, and using a single-worker isolated rerun, Main ran:

```powershell
.\gradlew.bat test jacocoTestReport jacocoTestCoverageVerification assemble --rerun-tasks --no-daemon --max-workers=1 --console=plain
```

Result: PASS in `2m29s`.

- Backend: 1,568 tests, 0 failures, 19 skipped.
- JaCoCo verification: PASS.
- Instruction coverage: 86.957%.
- Branch coverage: 72.251%.
- Line coverage: 87.228%.
- Method coverage: 84.730%.
- Class coverage: 94.824%.

Frontend final gates also passed:

- Tests: 74 files, 843 tests passed.
- Statement coverage: 88.64%.
- Branch coverage: 79.88%.
- Function coverage: 88.18%.
- Line coverage: 90.87%.
- Typecheck, ESLint, Prettier, and production build: PASS.
- Vitest emitted only the existing JSDOM navigation note; it was not a failure.

## Documentation and Diff Validation

- Source mapping recount: `150`, matching API specification v30.3.
- `python .agents/skills/validate-docs/scripts/validate_docs.py` - PASS; Tier 0, internal links, 578 supported traceability IDs, and indexes passed.
- `git diff --check` - exit 0 with no whitespace errors. Git emitted only working-copy CRLF-to-LF normalization warnings for `docs/index.md`, `docs/policies/security-policy.md`, and `docs/registry/project-registry.md`.

## Safety and Unchanged Boundaries

- No schema, DDL, data migration, dependency, policy decision, CSV header/row, export maximum, or status transition changed.
- The reported gates are automated code tests and static checks; no live export, external handoff, Provider action, or persistent/live database validation occurred.
- Commit and push remain pending for Main after this final evidence update.
- Batch replay continues to rebuild bytes only from immutable stored items through the existing endpoint.

## Residual Risks

- Recent exact-scope history is recovery evidence, not an operation key. Multiple matching batches may appear, so the UI keeps the result `unknown` and does not claim which candidate belongs to the interrupted POST.

## Rollback

Revert the recent-summary DTO/controller/service/repository query, frontend API/recovery UI/list quarantine/confirmation changes, focused tests, current docs, and both WI deliverables as one patch. No data rollback is required because this WI changed neither schema nor runtime data.

## Follow-up Chain

WI-040 implementation, independent QA-INTEG PASS, P3 remediation, and final Main gates are complete. Main retains ownership of the pending commit/push and the `WI-20260809-ATS-051` and `WI-20260809-ATS-063` chain decisions; this SE task does not create, delegate, commit, or push follow-up work.
