# Evidence Pack: WI-20260809-ATS-029

## Summary (one-liner)

- Completed the bounded source/assertion audit of WI-029 file, binary, CSV, and storage contracts; recorded 16 defects (`P1` 8, `P2` 8), one non-defect control finding (`A02`), passing targeted suites, and blocked live evidence without product or runtime mutation.

## Scope / DoD Check

- DoD items:
  - [x] Audited `PUB-09`, `MEM-07`, `MEM-09`, `MEM-16/17`, `ADM-06`, `ADM-10/11/12/13`, shared Track download entry points, and storage journal/recovery boundaries from source and existing assertions.
  - [x] Kept UI/control, frontend invocation, HTTP/server response, filename/type/bytes or parser result, and durable storage/domain/audit/reload evidence distinct.
  - [x] Recorded all independent causes in `deliverables/agent/WI-20260809-ATS-029-findings.md:42-480`: 16 defects (`P1` 8, `P2` 8) plus `A02` as a control/document match.
  - [x] Recorded main-supplied frontend, backend, typecheck, and targeted ESLint results with exact limitations.
  - [x] Classified live browser binary/file actions, authenticated/private files, and production DB/storage/Provider/audit proof as blocked.
  - [x] Preserved the frozen product/runtime/config/secret/git boundary and left the intentional ZIP untouched and uninspected.
  - [x] Main completed output-document Prettier write/check, docs validation, and `git diff --check` after the recorded test evidence; no product test or browser action was rerun.

## Reference Documents (Tier 0-2)

**Injected Context** (from WI Handoff Packet):

| Tier | Document                                                  | Reason                                                                            |
| ---- | --------------------------------------------------------- | --------------------------------------------------------------------------------- |
| 0    | `docs/standards/core-principles.md`                       | Constitution required by the handoff.                                             |
| 0    | `docs/standards/development-standards.md`                 | Development and verification boundary required for qa-integ.                      |
| 1    | `docs/policies/security-policy.md`                        | Private-file, secret, and audit boundary.                                         |
| 1    | `docs/policies/access-control-policy.md`                  | User/ADMIN ownership and authorization boundary.                                  |
| 1    | `docs/policies/quality-gates.md`                          | Evidence and quality-result classification.                                       |
| 1    | `docs/architecture/system-design.md`                      | Cross-layer system boundary.                                                      |
| 2    | `docs/standards/frontend-standards.md`                    | Frontend interaction and request-state contract.                                  |
| 2    | `.agents/skills/react-best-practices/AGENTS.md`           | React review context named by the handoff.                                        |
| 2    | `docs/design/api-spec.md`                                 | API route and response contract.                                                  |
| 2    | `docs/design/db-schema.md`                                | Durable schema and relationship contract.                                         |
| 2    | `docs/ui/screen-flow.md`                                  | Named UI entry points and workflow context.                                       |
| 2    | `docs/design/usecase/user-question.md`                    | Question visibility and attachment contract.                                      |
| 2    | `docs/design/usecase/whitelist.md`                        | Whitelist workflow and immutable CSV contract.                                    |
| 2    | `docs/design/usecase/company-certification.md`            | Company Certification private-document contract.                                  |
| 2    | `docs/design/usecase/user-license.md`                     | License issuance and visibility contract.                                         |
| 2    | `docs/design/usecase/user-notice.md`                      | Notice attachment/read contract.                                                  |
| 2    | `docs/design/payment-operations-runbook.md`               | Settlement operator workflow and safety boundary.                                 |
| 2    | `docs/design/payment-refund-receipt-settlement-policy.md` | Settlement API, audit, and unresolved decision record.                            |
| 2    | `docs/payment/operator-guide.md`                          | Handoff pointer was absent during the audit; no substitute document was inferred. |

**Injection Rules Applied**:

- Rule source pointer: `.claude/config/context-injection-rules.json` (named by the handoff; not re-read during closeout).
- Assignee: `qa-integ`.
- Task type: cross-layer file/binary/CSV integration audit.
- `agent_required_tiers`: `[0, 1]`; Tier 2 domain/frontend contracts were explicitly injected by the handoff.
- Handoff/REQ context: `deliverables/agent/WI-20260809-ATS-029-handoff.md:1-203`, `deliverables/user/REQ-20260809-ATS-001.md`, and `deliverables/agent/WI-20260809-ATS-020-acceptance-matrix.md` ranges named at handoff line 113.

## Evidence Pointers (required)

- Files changed:
  - `deliverables/agent/WI-20260809-ATS-029-evidence-pack.md` - reproducible agent-facing closeout evidence.
  - `deliverables/user/WI-20260809-ATS-029-summary.md` - concise user-facing outcome and decisions.
- Primary evidence remains unchanged:
  - `deliverables/agent/WI-20260809-ATS-029-findings.md:15-28` - coverage and live-evidence boundary.
  - `deliverables/agent/WI-20260809-ATS-029-findings.md:30-38` - severity totals and A02 control count.
  - `deliverables/agent/WI-20260809-ATS-029-findings.md:42-480` - independent findings with source/test pointers and lane analysis.
  - `deliverables/agent/WI-20260809-ATS-029-findings.md:482-500` - confirmed controls.
  - `deliverables/agent/WI-20260809-ATS-029-findings.md:502-514` - blocked and unproven evidence.
  - `deliverables/agent/WI-20260809-ATS-029-findings.md:516-553` - exact test evidence and missing assertions.
  - `deliverables/agent/WI-20260809-ATS-029-findings.md:555-561` - Settlement scope and policy decisions.

### Finding Register

| ID    | Severity/classification           | Independent cause                                                                          |
| ----- | --------------------------------- | ------------------------------------------------------------------------------------------ |
| `A01` | `P1` / specification gap          | Question backend validation is absent; exact Notice/Question limits are not canonicalized. |
| `A02` | Control / document match          | Public Question attachments inherit Question viewing permission; this is not a defect.     |
| `A03` | `P1` / contract decision required | First-download durable state can commit before HTTP body transfer completes.               |
| `A04` | `P2`                              | Binary filename and byte validation differ across clients.                                 |
| `A05` | `P2`                              | Duplicate-request fencing differs across download entry points.                            |
| `A06` | `P2`                              | Storage recovery lacks one H2 plus real-temp-files restart proof.                          |
| `A07` | `P2`                              | Private-document controllers buffer full files in heap.                                    |
| `A08` | `P2`                              | Download-all has no server or client batch bound.                                          |
| `B01` | `P1`                              | Keyword-only Whitelist confirmation misstates the status mutation.                         |
| `B02` | `P1`                              | Whitelist export has no recoverable operation identity after an unknown response.          |
| `B03` | `P1`                              | Partial Settlement import is reported as success and clears retry context.                 |
| `B04` | `P1`                              | Settlement IGNORE note and retry integrity are enforced only by the UI.                    |
| `B05` | `P1`                              | Settlement CSV decoding/grammar can alter evidence silently.                               |
| `B06` | `P1`                              | Settlement financial/provider fields are not validated to durable representation.          |
| `B07` | `P2`                              | Settlement duplicate handling is sequential, not atomic or file-auditable.                 |
| `B08` | `P2`                              | Settlement reconciliation has no explicit range or row bound.                              |
| `B09` | `P2`                              | Reconciliation summary omits unusable rows from all outcome counters.                      |

### Binary / Parser / Durable Evidence Matrix

| Scope                                      | UI / control                                                                                              | Frontend call                                                                   | Server / HTTP                                                                                                        | Binary or parser                                                                                                     | Durable state / audit / reload                                                                                                                | Outcome                                        |
| ------------------------------------------ | --------------------------------------------------------------------------------------------------------- | ------------------------------------------------------------------------------- | -------------------------------------------------------------------------------------------------------------------- | -------------------------------------------------------------------------------------------------------------------- | --------------------------------------------------------------------------------------------------------------------------------------------- | ---------------------------------------------- |
| Notice attachments (`PUB-09`, `ADM-12/13`) | Public detail and ADMIN create/edit paths traced.                                                         | Blob GET and multipart mutations traced.                                        | Public composite lookup and admin mutation paths exist.                                                              | Frontend filename/header use is inconsistent; domain upload limits are incomplete.                                   | Journaled create/preserve/remove and after-commit cleanup are represented by isolated assertions.                                             | `PARTIAL`; `A01`, `A04`, `A06`.                |
| Track/License/History/shared entry points  | Pending fences differ; bulk flow is unbounded.                                                            | Shared Blob GET call sites inventoried.                                         | Entitlement, per-user lock, and existing-License branch traced.                                                      | No shared response-filename/content-type/non-empty validation.                                                       | First request mutates history/License/count before body completion; redownload skips duplicate durable state.                                 | `PARTIAL`; `A03-A05`, `A08`.                   |
| Question attachments (`MEM-16/17`)         | Create/detail/download/delete controls traced.                                                            | Multipart upload and authenticated download/delete traced.                      | Public/private Question permission contract matches `user-question.md`; hardened download response assertions exist. | PRIVATE storage and exact mocked bytes/headers are asserted; backend upload validation is absent.                    | Attachment rows and journal/after-commit delete paths traced; no live files used.                                                             | `PARTIAL`; `A01`, `A06`, `A07`; `A02` control. |
| Company Certification (`ADM-06`)           | Subscriber metadata and ADMIN document action traced.                                                     | ADMIN Blob request parses response filename.                                    | ADMIN-only composite lookup, path hiding, and hardened response asserted.                                            | Exact test bytes/headers and signature/MIME validation asserted; full body is heap-buffered.                         | PRIVATE root plus narrow access-grant audit traced; no private file accessed.                                                                 | `PARTIAL`; `A06`, `A07`.                       |
| Whitelist CSV (`ADM-11`)                   | Applied scope is distinct from draft input; confirmation defect remains.                                  | Blob export/replay, batch header, filename parsing, and object URL flow traced. | ADMIN routes return CSV disposition/type/batch ID; route-specific auth/header assertions remain incomplete.          | UTF-8 BOM, required headers including `userEmail`, quoting, formula neutralization, and byte-stable replay asserted. | Bounded immutable snapshots and status changes traced; unknown-response recovery and real concurrent/H2 proof absent.                         | `PARTIAL`; `B01-B02`.                          |
| Settlement CSV (`ADM-10`)                  | Import/reconcile/ignore pending and reload paths traced; partial success and UI-only IGNORE guard remain. | Multipart import and JSON reconcile/ignore contracts traced.                    | ADMIN routes exist; no export or preview route is in the current contract.                                           | CSV remains inert, but decoder/dialect and financial/provider validation are lenient.                                | Per-row save/audit, sequential dedup, reconcile, and IGNORE traced; atomic file audit, bounded reconcile, and count completeness remain open. | `PARTIAL`; `B03-B09`.                          |
| Storage journal/recovery                   | Not a UI path.                                                                                            | Not applicable.                                                                 | Internal transaction callbacks only.                                                                                 | `@TempDir` local storage assertions ran; one symlink case skipped because unavailable.                               | Mocked coordinator/journal/recovery contracts passed; no combined H2/files/restart proof or live replay.                                      | `PARTIAL`; `A06`.                              |

Passing tests preserve the findings: a passing click/mock/header assertion is not proof of completed browser bytes, a live private-file response, or production durable state.

## Commands & Outputs (if any)

Commands were run by main after qa-integ completed source/assertion inspection.

**Frontend targeted tests** (`frontend`):

```powershell
npx vitest run src/api/domainApis.test.ts src/api/adminWhitelistChannels.test.ts src/api/adminContracts.test.ts src/pages/subscriber/CompanyCertStatusPage.test.tsx src/pages/subscriber/CompanyCertApplyPage.test.tsx src/pages/subscriber/DownloadHistoryPage.test.tsx src/pages/public/NoticeListPage.test.tsx src/pages/admin/CompanyCertManagePage.test.tsx src/pages/subscriber/WhitelistChannelPage.test.tsx src/pages/admin/PaymentOperationsPage.test.tsx src/pages/admin/WhitelistChannelManagePage.test.ts src/pages/admin/WhitelistChannelManagePage.render.test.tsx src/test/coverage/publicAuthShell.coverage.test.tsx src/test/coverage/adminSubscriberPages.coverage.test.tsx src/test/coverage/adminSubscriberGaps.coverage.test.tsx
```

- Output: exit 0; 15 files and 159 tests passed; 0 failed/skipped; Vitest duration 9.58s; wall 11.4s.

**Backend targeted tests** (repository root):

```powershell
.\gradlew.bat test --rerun-tasks --tests "com.atstudio.atstudio.controller.NoticeControllerTest" --tests "com.atstudio.atstudio.controller.LicenseControllerTest" --tests "com.atstudio.atstudio.controller.QuestionControllerTest" --tests "com.atstudio.atstudio.controller.CompanyCertificationControllerTest" --tests "com.atstudio.atstudio.controller.AdminWhitelistChannelControllerTest" --tests "com.atstudio.atstudio.controller.AdminPaymentControllerTest" --tests "com.atstudio.atstudio.controller.TrackControllerTest" --tests "com.atstudio.atstudio.service.NoticeServiceTest" --tests "com.atstudio.atstudio.service.NoticeServiceBranchCoverageTest" --tests "com.atstudio.atstudio.service.LicenseServiceTest" --tests "com.atstudio.atstudio.service.DownloadServiceTest" --tests "com.atstudio.atstudio.service.DownloadConcurrencyContractTest" --tests "com.atstudio.atstudio.service.QuestionServiceTest" --tests "com.atstudio.atstudio.service.CompanyCertificationServiceTest" --tests "com.atstudio.atstudio.service.CompanyCertificationSecurityVerificationTest" --tests "com.atstudio.atstudio.service.AdminWhitelistChannelServiceTest" --tests "com.atstudio.atstudio.service.WhitelistConcurrencyContractTest" --tests "com.atstudio.atstudio.service.AdminPaymentSettlementServiceTest" --tests "com.atstudio.atstudio.service.storage.LocalStorageServiceTest" --tests "com.atstudio.atstudio.service.storage.StorageCleanupServiceTest" --tests "com.atstudio.atstudio.service.storage.StorageMutationJournalServiceTest" --tests "com.atstudio.atstudio.service.storage.StorageMutationCoordinatorTest" --tests "com.atstudio.atstudio.service.storage.StorageMutationRecoveryServiceTest" --tests "com.atstudio.atstudio.service.storage.StorageReferenceCheckerBranchCoverageTest" --tests "com.atstudio.atstudio.service.storage.StorageMutationRecoveryVerificationTest" --tests "com.atstudio.atstudio.entity.StorageMutationContractTest"
```

- Output: exit 0; `BUILD SUCCESSFUL` in 54s; wall 55.4s; 26 explicit class filters; 5 tasks executed.
- XML aggregate: 38 suites, 278 tests, 0 failures, 0 errors, 1 skipped.
- Skip: `LocalStorageServiceTest.rejectsDirectoriesAndSymbolicLinks`; symbolic links were unavailable in this environment.
- Non-failing warnings: unchecked/unsafe operations, CDS boot-loader sharing, incubating problems report, and configuration-cache suggestion.

**Frontend typecheck** (`frontend`):

```powershell
npm run typecheck
```

- Output: exit 0; PASS in 6.3s; no diagnostics.

**Targeted ESLint** (`frontend`):

- Exact command text was not supplied by main. Reported scope: named APIs/pages with `--max-warnings 0`.
- Output: exit 0; PASS in 3.1s; 0 warnings.

**Final document checks** (completed after the test evidence; no product tests or browser actions were rerun):

- Prettier write over the handoff, findings, evidence pack, and summary: exit 0. Per-file results: handoff unchanged in 48ms; findings formatted in 87ms; evidence pack formatted in 27ms; summary formatted in 13ms.
- Prettier check over the same four files: exit 0; all matched files use Prettier code style.
- Docs validation: exit 0; Tier 0 documents, internal links, 541 traceability IDs, and the document index passed; all validations passed.
- `git diff --check`: exit 0; no output.

## Tests (if any)

| Test lane                | Result                                                            | Evidence boundary                                                                      |
| ------------------------ | ----------------------------------------------------------------- | -------------------------------------------------------------------------------------- |
| Frontend targeted Vitest | PASS - 15 files/159 tests; 0 fail/skip                            | jsdom/mocked frontend contracts; no browser file action or real bytes.                 |
| Backend targeted Gradle  | PASS - 38 suites/278 tests; 0 failures/errors; 1 environment skip | Mock/MockMvc/contract and isolated `@TempDir`; no production DB/storage/Provider.      |
| TypeScript typecheck     | PASS - exit 0; 6.3s                                               | No diagnostics.                                                                        |
| Targeted ESLint          | PASS - exit 0; 3.1s; 0 warnings                                   | Exact command unavailable; result cannot be reproduced exactly until main supplies it. |

No screenshots were captured or generated. No browser file chooser, upload, download, import, export, or authenticated/private-file action occurred.

## Risks / Rollback

- Risks:
  - All 16 defects remain open despite passing targeted suites; see the register above and `findings.md:42-480`.
  - Live HTTP headers/bytes, completed client-body delivery, authenticated/private files, and production durable state/audit remain unproven.
  - The skipped symbolic-link case leaves one environment-specific local-storage defense unexecuted in this run.
  - Exact attachment limits, download-success meaning, Settlement CSV dialect/envelope, amount/currency/provider-ID bounds, and future accounting export destination require explicit policy decisions.
  - Settlement export and pre-import preview are not current requirements; they must not be treated as missing defects without a new requirement.
- Frozen-state record:
  - No product source, runtime, test, config, schema, current product docs, DB, storage, Provider, audit, secret/session, browser, or git state was mutated by this closeout.
  - `output/client-demo-screenshots-20260716-140514.zip` remained untouched and uninspected: no open/read/hash/metadata probe/move/replace/delete occurred.
- Rollback:
  - Documentation-only closeout. If withdrawn, remove or restore only `deliverables/agent/WI-20260809-ATS-029-evidence-pack.md` and `deliverables/user/WI-20260809-ATS-029-summary.md`; no product rollback or data cleanup is required.

## Follow-ups (optional)

- Next WI candidates:
  - Final document-quality checks are complete; preserve their post-test, documentation-only evidence boundary.
  - Obtain product decisions for the five unresolved policy areas before selecting exact validation/remediation values.
  - Split remediation by independent cause, prioritizing the eight `P1` findings before the eight `P2` findings.
  - Add the missing controller, mixed-result, H2/concurrency, transaction/audit, and restart assertions listed at `findings.md:543-551`.
  - Run separately approved live acceptance only in an authorized environment; WI-029 itself performed no live/browser file action.
  - Handoff dependency: WI-029 blocks `WI-20260809-ATS-030` (`WI-20260809-ATS-029-handoff.md:5-7`).
