---
version: 1.0
last_updated: 2026-08-14
project: ATS
owner: docops
category: evidence-pack
status: complete
dependencies:
  - path: WI-20260809-ATS-050-handoff.md
    reason: Approved implementation scope, DoD, and traceability contract
  - path: WI-20260809-ATS-050-qa-integ-review-result.md
    reason: Preserved historical initial QA FAIL and findings 001 through 003
  - path: WI-20260809-ATS-050-qa-integ-rereview-result.md
    reason: Preserved historical rereview FAIL and findings 004 and 005
  - path: WI-20260809-ATS-050-qa-final-review-result.md
    reason: Preserved historical final-review FAIL and findings 006 and 007
  - path: WI-20260809-ATS-050-qa-conclusive-review-result.md
    reason: Conclusive independent QA PASS and finding closure authority
  - path: WI-20260809-ATS-050-finalization-handoff.md
    reason: Authoritative final full-gate metrics and finalization boundary
---

# Evidence Pack: WI-20260809-ATS-050

## Summary (one-liner)

Conclusive independent QA is `PASS` with zero open/new P0-P2 findings, the
authoritative full frontend and backend gates pass, and Notice detail,
create/edit/delete, attachment download, ADMIN authorization, and public/admin
view-count ownership are documented with one residual P3 evidence gap.

## Scope / DoD Check

- [x] Public Notice `404` renders localized missing-state copy and safe Notice-
  list navigation; transient failures remain distinct and expose one manual
  retry.
- [x] Public Notice detail uses latest-target ownership; route replacement and
  unmount retire stale reads and prevent stale UI commits.
- [x] Attachment downloads use immutable per-file ownership, fence a duplicate
  same-file call while pending, keep another file independently available,
  retain local failure, and permit same-file retry without stale browser
  effects.
- [x] ADMIN create/edit use associated Korean controls and the canonical title
  and 1,000-character content boundaries at client and server boundaries.
- [x] Save, Notice delete, attachment add/remove, modal close, navigation,
  browser unload, Logout, and duplicate mutation are coordinated by owned
  operation fences. Authoritative failures allow bounded retry; ambiguous
  outcomes require observation and do not repeat POST/PUT/DELETE.
- [x] ADMIN edit reads use an explicit ADMIN-only projection and perform zero
  public `viewCount` increments; the public detail read retains one increment
  per API invocation.
- [x] Missing, malformed, non-positive, noncanonical, and unsafe Notice edit IDs
  issue zero Notice, attachment, and mutation calls and expose safe navigation.
- [x] WI-039 PRIVATE attachment storage and safe public download headers remain
  green; no new attachment type/count/byte policy was introduced.
- [x] Focused and full frontend verification, frontend static/format/build
  gates, forced backend tests/build/JaCoCo, and conclusive independent QA pass.
- [x] This Evidence Pack and the Korean user summary record exact authority,
  historical failures, residual scope, effect boundaries, and rollback.
- [x] Post-finalization documentation validation and final `git diff --check`
  pass under the final documentation closure handoff authority.

## Reference Documents (Tier 0-3)

**Injected Context** (from the WI handoffs and finalization handoff):

| Tier | Document | Reason |
|---|---|---|
| 0 | `docs/standards/core-principles.md` | Approval, language, safety, and traceability baseline |
| 0 | `docs/standards/development-standards.md` | Java/React implementation and verification baseline from the original handoff |
| 0 | `docs/standards/documentation-standards.md` | Evidence Pack/work-summary structure and historical-record rules |
| 0 | `docs/standards/glossary.md` | WI, ADMIN, Notice, attachment, and `viewCount` terminology |
| 1 | `docs/policies/security-policy.md` | ADMIN projection and public safe-download boundary |
| 1 | `docs/policies/quality-gates.md` | Regression, validation, rollback, and Evidence Pack requirements |
| 1 | `docs/policies/access-control-policy.md` | ADMIN authorization baseline from the original handoff |
| 2 | `docs/standards/frontend-standards.md` | Request ownership, async state, and accessibility baseline |
| 2 | `docs/design/api-spec.md` | Public/admin Notice read and attachment response contracts |
| 2 | `docs/design/usecase/user-notice.md` | Notice create/detail/update/delete behavior and recovery |
| 2 | `docs/ui/screen-flow.md` | Current Notice UI ownership and recovery flow |
| 2 | `docs/ui/atstudio-front-list.md` | Current public and ADMIN Notice behavior |
| 2 | `docs/ui/modal-list.md` | Shared busy Modal and Notice delete behavior |
| 2 | `.agents/skills/react-best-practices/AGENTS.md` | React effect and state ownership guidance |
| 3 | `deliverables/user/REQ-20260809-ATS-001.md` | Approved audit-correction authority |
| 3 | `deliverables/agent/WI-20260809-ATS-031-consolidated-findings.md` | `CR-031-001`, Notice portion of `CR-031-061`, and `CR-031-065` routing |
| 3 | `deliverables/agent/WI-20260809-ATS-039-evidence-pack.md` | PRIVATE attachment-storage dependency |

**Injection Rules Applied**:

- Assignee: `docops`
- Task type: `documentation`
- Finalization source: `deliverables/agent/WI-20260809-ATS-050-finalization-handoff.md`
- Authority order: finalization handoff full-gate record, conclusive independent
  QA, current production/tests/docs, then historical QA records.

## Evidence Pointers

### DoD Traceability

| DoD item | Production/API pointer | Test pointer | Current-document pointer |
|---|---|---|---|
| Public missing vs transient failure | `frontend/src/pages/public/NoticeDetailPage.tsx:62-108`; `frontend/src/pages/public/NoticeDetailPage.tsx:176-194` | `frontend/src/pages/public/NoticeDetailPage.test.tsx:65-103` | `docs/design/usecase/user-notice.md:125-136`; `docs/ui/screen-flow.md:105-114` |
| Latest public target and unmount retirement | `frontend/src/pages/public/NoticeDetailPage.tsx:44-106` | `frontend/src/pages/public/NoticeDetailPage.test.tsx:104-135` | `docs/ui/atstudio-front-list.md:88-95` |
| Per-attachment pending/failure/retry ownership | `frontend/src/pages/public/NoticeDetailPage.tsx:113-155`; `frontend/src/api/notices.ts:98-113` | `frontend/src/pages/public/NoticeDetailPage.test.tsx:137-204`; `frontend/src/api/notices.test.ts:48-58` | `docs/design/api-spec.md:96-103`; `docs/design/usecase/user-notice.md:132-136` |
| Localized controls and canonical content maximum | `frontend/src/utils/validation.ts:25`; `frontend/src/pages/admin/NoticeCreatePage.tsx:146-208`; `frontend/src/pages/admin/NoticeEditPage.tsx:426-486`; `src/main/java/com/atstudio/atstudio/dto/notice/NoticeCreateRequest.java:19-24`; `src/main/java/com/atstudio/atstudio/dto/notice/NoticeUpdateRequest.java:16-20` | `frontend/src/pages/admin/NoticeAdminPages.test.tsx:131-179`; `src/test/java/com/atstudio/atstudio/controller/NoticeControllerTest.java:247-270` | `docs/design/usecase/user-notice.md:40-49`; `docs/ui/screen-flow.md:115-118` |
| Mutation, navigation, Logout, and ambiguous-outcome coordination | `frontend/src/hooks/usePendingMutationGuard.ts:6-28`; `frontend/src/layouts/AdminMutationBoundary.tsx:1-15`; `frontend/src/layouts/AdminLayout.tsx:51-89`; `frontend/src/pages/admin/NoticeCreatePage.tsx:86-136`; `frontend/src/pages/admin/NoticeEditPage.tsx:271-374` | `frontend/src/pages/admin/NoticeAdminPages.test.tsx:180-453`; `frontend/src/pages/admin/NoticeAdminPages.test.tsx:537-758`; `frontend/src/pages/admin/NoticeAdminShellIntegration.test.tsx:165-421` | `docs/design/usecase/user-notice.md:43-57`; `docs/design/usecase/user-notice.md:154-173`; `docs/ui/atstudio-front-list.md:100-109` |
| Busy/non-closable delete Modal | `frontend/src/components/ui/Modal.tsx:29-77`; `frontend/src/components/ui/Modal.tsx:126-155`; `frontend/src/pages/admin/NoticeEditPage.tsx:596-626` | `frontend/src/components/ui/Modal.test.tsx:123-162`; `frontend/src/pages/admin/NoticeAdminPages.test.tsx:606-657` | `docs/ui/modal-list.md:40-59` |
| ADMIN authorization and non-counting read | `frontend/src/api/notices.ts:17-27`; `src/main/java/com/atstudio/atstudio/config/SecurityConfig.java:83`; `src/main/java/com/atstudio/atstudio/controller/NoticeController.java:53-69`; `src/main/java/com/atstudio/atstudio/repository/NoticeRepository.java:19-37`; `src/main/java/com/atstudio/atstudio/service/NoticeService.java:86-117`; `src/main/java/com/atstudio/atstudio/dto/notice/NoticeAdminResponse.java:5-10` | `frontend/src/api/notices.test.ts:29-46`; `src/test/java/com/atstudio/atstudio/controller/NoticeControllerTest.java:210-245`; `src/test/java/com/atstudio/atstudio/service/NoticeServiceTest.java:360-412` | `docs/design/api-spec.md:104-107`; `docs/policies/security-policy.md:390-395`; `docs/design/usecase/user-notice.md:250-277` |
| Invalid ADMIN edit route ID makes zero calls | `frontend/src/pages/admin/NoticeEditPage.tsx:58-105`; `frontend/src/utils/routeId.ts:3-10` | `frontend/src/pages/admin/NoticeAdminPages.test.tsx:484-502` | `docs/design/usecase/user-notice.md:267-277`; `docs/ui/screen-flow.md:129-131` |
| PRIVATE storage and safe public attachment response | `src/main/java/com/atstudio/atstudio/controller/NoticeController.java:97-113`; existing WI-039 storage path retained by `src/main/java/com/atstudio/atstudio/service/NoticeService.java` | `src/test/java/com/atstudio/atstudio/service/NoticeServiceTest.java:237-358`; `src/test/java/com/atstudio/atstudio/controller/NoticeControllerTest.java:163-208` | `docs/design/api-spec.md:96-103`; `docs/policies/security-policy.md:382-397` |

### Contract Lanes and Effects

- **UI behavior:** React tests prove localized loading/missing/error/retry,
  pending and busy controls, focus containment, route retirement, and
  observation-only recovery.
- **API invocation:** Mocks assert exact read/mutation calls. Public detail uses
  `GET /api/notices/{id}`; edit uses `GET /api/notices/{id}/admin`; ambiguous
  recovery performs observation GETs and does not repeat the accepted mutation.
- **Authorization:** `SecurityConfig` and `@PreAuthorize` independently require
  ADMIN for the edit projection; MockMvc verifies anonymous `401`, USER `403`,
  and ADMIN `200`.
- **View-count persistence:** Test-context service evidence proves public detail
  calls `incrementViewCount()` exactly once per invocation. The ADMIN projection
  uses `findAdminEditRowsById` and performs zero public entity/view-count writes.
  No live or operational database row was read or mutated.
- **Attachment storage/download boundary:** Backend tests use PRIVATE temporary
  test storage and MockMvc safe-header checks. Frontend tests mock bytes and the
  browser download helper. No real attachment download or operational storage
  access occurred.
- **Unexecuted effects:** No live ADMIN mutation, browser download, local or
  operational DB/storage/file effect, external/provider effect, secret
  inspection, protected-output access, schema/dependency change, deploy, branch
  action, stage, commit, push, or retained effect occurred.

## QA Remediation Closure

The three prior `FAIL` verdicts are immutable historical records. They are not
the current verdict and were not rewritten during finalization.

| Finding | Historical state | Conclusive state and evidence |
|---|---|---|
| `F-QA-INTEG-050-001` (P2) | Initial, rereview, and final-review failures exposed mutation abandonment, remount duplication, and Logout bypass. | `CLOSED BY EXECUTION`: synchronous AdminLayout owner boundary and composed edit-shell tests; `qa-conclusive-review-result.md:73` |
| `F-QA-INTEG-050-002` (P2) | Initial FAIL exposed an enabled but inert busy Modal close action. | `CLOSED BY EXECUTION`: one busy contract covers disabled close, Escape, backdrop, `aria-busy`, and focus; `qa-conclusive-review-result.md:76` |
| `F-QA-INTEG-050-003` (P3) | Initial/rereview evidence gaps covered missing lifecycle and destination-read schedules. | `CLOSED BY EXECUTION`: public lifecycle and GET-only destination recovery schedules pass; `qa-conclusive-review-result.md:77` |
| `F-QA-INTEG-050-004` (P2) | Rereview FAIL found idle `beforeunload` registration. | `CLOSED BY EXECUTION`: registration follows pending ownership and settles on every terminal path; `qa-conclusive-review-result.md:78` |
| `F-QA-INTEG-050-005` (P2) | Rereview FAIL found focus escape from an all-disabled busy Modal. | `CLOSED BY EXECUTION`: Tab/Shift+Tab retain dialog focus and recovery restores the ordinary cycle; `qa-conclusive-review-result.md:79` |
| `F-QA-INTEG-050-006` (P2) | Final-review FAIL found same-user token rotation could retire mutation settlement. | `CLOSED BY EXECUTION`: owner key uses user ID and role, and update/delete refresh replay schedules pass; `qa-conclusive-review-result.md:74` |
| `F-QA-INTEG-050-007` (P3) | Final-review FAIL retained a fence after remove-only storage failure. | `CLOSED BY EXECUTION`: boolean removal and fail-closed later-clear retry pass; `qa-conclusive-review-result.md:75` |
| `F-QA-INTEG-050-008` (P2) | Conclusive-run execution-environment blocker required actual rerun evidence. | `CLOSED`: mandatory frontend composition and forced focused backend rerun executed successfully; `qa-conclusive-review-result.md:5-44` |
| `F-QA-INTEG-050-009` (P3) | Not previously present. | **OPEN evidence gap only**: no separate real AdminLayout + NoticeCreate Logout composition test. Shared source boundary, isolated create tests, and real edit-shell tests pass; no implementation defect was observed. |

## Final Authoritative Results

The values below come from
`deliverables/agent/WI-20260809-ATS-050-finalization-handoff.md:19-28` and are
the current authority. DocOps did not rerun tests or builds during finalization.

| Gate | Authority or exact command | Result |
|---|---|---|
| Conclusive independent QA | `WI-20260809-ATS-050-qa-conclusive-review-result.md` | `PASS`; open/new P0-P2: 0; findings `001` through `008` closed |
| Frontend full coverage run | Finalization handoff full-gate record | `PASS`: 100 files, 1,186 tests, 0 failures |
| Frontend coverage | Finalization handoff full-gate record | Statements 89.38% (9831/10999); branches 81.57% (6409/7857); functions 90.11% (2252/2499); lines 91.86% (9062/9864) |
| Frontend static/format/build | Finalization handoff full-gate record | Typecheck `PASS`; full ESLint `PASS`; full Prettier `PASS`; production build `PASS`; 292 Vite modules transformed |
| Forced backend final gate | `.\gradlew.bat test jacocoTestReport jacocoTestCoverageVerification assemble --rerun-tasks --no-daemon --max-workers=1 --console=plain` | `BUILD SUCCESSFUL` in 3m16s |
| Backend tests | Forced backend final-gate record | 184 suites; 1,595 tests; 0 failures; 0 errors; 19 skipped |
| JaCoCo | Forced backend final-gate record | Instruction 87.048%; branch 72.295%; line 87.318%; method 84.898%; verification `PASS` |
| Documentation validation | `python .agents/skills/validate-docs/scripts/validate_docs.py` | `PASS`: Tier 0 exists, no broken internal links, 585 supported traceability IDs, and all documents are indexed |
| Final diff check | `git diff --check -- . ':(exclude)output/**'` | `PASS` (exit 0); only CRLF-to-LF working-copy notices for pre-existing line-ending normalization candidates were emitted |

## Historical Verification Evidence

- Initial independent QA returned `FAIL` with `F-QA-INTEG-050-001` and `-002`
  at P2 plus `-003` at P3. Its focused commands passed, but the counterexamples
  blocked completion.
- Independent rereview returned `FAIL`: `-001` remained open, `-004` and `-005`
  were new P2 findings, and `-003` remained an evidence gap.
- Independent final review returned `FAIL`: earlier `-002` through `-005` were
  closed, but Logout kept `-001` open, token rotation introduced P2 `-006`, and
  remove-only storage failure introduced P3 `-007`.
- Conclusive QA executed the mandatory composition and focused backend tests,
  closed `-001` through `-008`, and returned `PASS` with zero open/new P0-P2.
- The historical final-review production build transformed 291 modules. It is
  superseded for final-gate reporting by the finalization handoff's 292-module
  full build after R3; these are different execution points, not contradictory
  current results.

## Current WI-050 Workspace Inventory

The following inventory was obtained by read-only tracked/untracked path
inspection with `output/**` excluded. No Git state was changed.

### Production and styles

- `frontend/src/api/loadError.ts`
- `frontend/src/api/notices.ts`
- `frontend/src/components/ui/Modal.module.css`
- `frontend/src/components/ui/Modal.tsx`
- `frontend/src/hooks/usePendingMutationGuard.ts`
- `frontend/src/layouts/AdminLayout.module.css`
- `frontend/src/layouts/AdminLayout.tsx`
- `frontend/src/layouts/AdminMutationBoundary.tsx`
- `frontend/src/pages/admin/NoticeCreatePage.module.css`
- `frontend/src/pages/admin/NoticeCreatePage.tsx`
- `frontend/src/pages/admin/NoticeEditPage.module.css`
- `frontend/src/pages/admin/NoticeEditPage.tsx`
- `frontend/src/pages/public/NoticeDetailPage.module.css`
- `frontend/src/pages/public/NoticeDetailPage.tsx`
- `frontend/src/pages/public/NoticeListPage.tsx`
- `frontend/src/utils/noticeCreateObservationFence.ts`
- `frontend/src/utils/safeStorage.ts`
- `src/main/java/com/atstudio/atstudio/config/SecurityConfig.java`
- `src/main/java/com/atstudio/atstudio/controller/NoticeController.java`
- `src/main/java/com/atstudio/atstudio/dto/notice/NoticeAdminResponse.java`
- `src/main/java/com/atstudio/atstudio/repository/NoticeRepository.java`
- `src/main/java/com/atstudio/atstudio/service/NoticeService.java`

### Tests

- `frontend/src/api/domainApis.test.ts`
- `frontend/src/api/notices.test.ts`
- `frontend/src/components/ui/Modal.test.tsx`
- `frontend/src/pages/admin/NoticeAdminPages.test.tsx`
- `frontend/src/pages/admin/NoticeAdminShellIntegration.test.tsx`
- `frontend/src/pages/public/NoticeDetailPage.test.tsx`
- `frontend/src/test/coverage/adminSubscriberPages.coverage.test.tsx`
- `frontend/src/test/coverage/publicAuthShell.coverage.test.tsx`
- `frontend/src/utils/noticeCreateObservationFence.test.ts`
- `frontend/src/utils/safeStorage.test.ts`
- `src/test/java/com/atstudio/atstudio/controller/NoticeControllerTest.java`
- `src/test/java/com/atstudio/atstudio/service/NoticeServiceTest.java`

### Current-state documents

- `docs/design/api-spec.md`
- `docs/design/usecase/user-notice.md`
- `docs/policies/security-policy.md`
- `docs/ui/atstudio-front-list.md`
- `docs/ui/modal-list.md`
- `docs/ui/screen-flow.md`

### Coordination, historical QA, and final deliverables

- `deliverables/agent/WI-20260809-ATS-050-handoff.md`
- `deliverables/agent/WI-20260809-ATS-050-remediation-handoff.md`
- `deliverables/agent/WI-20260809-ATS-050-remediation-r2-handoff.md`
- `deliverables/agent/WI-20260809-ATS-050-remediation-r3-handoff.md`
- `deliverables/agent/WI-20260809-ATS-050-qa-integ-review-handoff.md`
- `deliverables/agent/WI-20260809-ATS-050-qa-integ-review-result.md`
- `deliverables/agent/WI-20260809-ATS-050-qa-integ-rereview-handoff.md`
- `deliverables/agent/WI-20260809-ATS-050-qa-integ-rereview-result.md`
- `deliverables/agent/WI-20260809-ATS-050-qa-final-review-handoff.md`
- `deliverables/agent/WI-20260809-ATS-050-qa-final-review-result.md`
- `deliverables/agent/WI-20260809-ATS-050-qa-conclusive-review-handoff.md`
- `deliverables/agent/WI-20260809-ATS-050-qa-conclusive-review-result.md`
- `deliverables/agent/WI-20260809-ATS-050-finalization-handoff.md`
- `deliverables/agent/WI-20260809-ATS-050-evidence-pack.md`
- `deliverables/user/WI-20260809-ATS-050-summary.md`

## Commands & Outputs

- The initial DocOps finalization executed no tests, builds, documentation
  validator, `git diff --check`, live effects, or Git mutation commands.
- The final documentation closure handoff records the post-finalization
  documentation validator as `PASS`: Tier 0 exists, no broken internal links,
  585 supported traceability IDs, and all documents are indexed.
- The same closure authority records final `git diff --check -- .
  ':(exclude)output/**'` as `PASS` with exit 0. Its only output was CRLF-to-LF
  working-copy notices for pre-existing line-ending normalization candidates.
- Read-only workspace inventory:
  - `git diff --name-only -- . ':(exclude)output/**'`
  - `git ls-files --others --exclude-standard -- . ':(exclude)output/**'`
- Inventory result: the production, test, current-document, and untracked WI
  paths listed above. Existing CRLF-to-LF working-copy warnings were observed
  for `security-policy.md` and several backend Notice files; no content or Git
  state was changed by the reads.

## Risks / Rollback

### Risks and residual boundaries

- `F-QA-INTEG-050-009` remains P3: a separate real AdminLayout +
  NoticeCreate Logout composition test is absent. This is an evidence gap, not
  an observed defect; the shared boundary is source-verified, isolated create
  tests pass, and the real edit-shell composition passes.
- WI-055 owns broader binary response, filename, byte, and download-helper
  normalization.
- WI-059 owns public catalog keyboard, heading, and fallback work.
- WI-066 owns canonical Notice/Question attachment type, count, and byte policy.
- WI-070 owns broader creator/ADMIN page coverage.
- Automated mock/test-context evidence does not establish live browser,
  production DB, retained storage object, real attachment transfer, or external
  acceptance.
- Post-finalization documentation validation and final diff check both pass;
  no current documentation-closure gate remains open.

### Rollback

- Revert only the scoped production/style, test, current-state document, and
  WI-050 deliverable paths listed in this Evidence Pack.
- Preserve the historical QA result files as historical evidence unless the
  entire WI record is intentionally removed under a separately approved scope.
- No database, storage, attachment, provider, browser, or other external/data
  rollback is required because no live or retained effect occurred.

## Stale or Contradictory Claim Check

- No unresolved production/test/current-document contradiction was found.
- The historical QA `FAIL` verdicts are intentionally retained and are
  superseded only as current authority by the conclusive QA `PASS`; they are not
  rewritten as passes.
- The historical 291-module build and final authoritative 292-module build are
  separate execution points. The 292-module value is used for final reporting.
- Historical documentation-validation and diff-check passes that predate these
  two finalization files remain historical. The later post-finalization
  validation and diff check are the current authority, and both are `PASS`.
