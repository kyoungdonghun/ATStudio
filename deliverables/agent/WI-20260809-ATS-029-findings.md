# WI-20260809-ATS-029 Integration Findings

## Checkpoint Status

- Scope: Part A binary/file delivery and storage mutation audit plus the bounded Part B Whitelist export and Settlement import/reconcile/ignore source/assertion audit.
- Checkpoint: Part A exploration stopped on user instruction. Part B source and existing-assertion inspection is complete. QA-integ did not execute tests during the audit; main subsequently supplied the targeted execution evidence recorded below.
- Baseline: the handoff baseline is accepted as an input pointer; branch, HEAD, and worktree state were not re-probed at this checkpoint.
- Evidence type: exact source/existing-assertion inspection plus main-supplied targeted frontend/backend execution results. No browser file action, live upload/download, private/user file access, live DB/storage/provider/mail access, or secret/session inspection occurred.
- Protected artifact: `output/client-demo-screenshots-20260716-140514.zip` was not opened, read, hashed, or metadata-probed.

## Coverage

`COMPLETE` means the bounded source/call-path and existing assertion inspection was completed. `PARTIAL` means live/browser/private-file/production evidence or a bounded product decision remains. `MAIN TARGETED PASS` records the exact isolated suites later run by main; it is not whole-row or live acceptance.

| Scope                                  | Matrix rows / paths                                                             | Source and assertion audit | Runtime evidence                                           | Overall | Checkpoint result                                                                                                                                                                                                                     |
| -------------------------------------- | ------------------------------------------------------------------------------- | -------------------------- | ---------------------------------------------------------- | ------- | ------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| Notice attachment download             | `PUB-09`                                                                        | COMPLETE                   | MAIN TARGETED PASS; live blocked                           | PARTIAL | Public route and composite attachment lookup exist; frontend ignores response filename/headers; controller has no byte/header assertion test.                                                                                         |
| License Track download                 | `MEM-07`                                                                        | COMPLETE                   | MAIN TARGETED PASS; live blocked                           | PARTIAL | Existing License redownload avoids duplicate history/license/count. `user-license.md` confirms automatic issuance and post-expiry visibility, but does not define redownload entitlement, quota, filename, or byte-delivery behavior. |
| Download history/redownload            | `MEM-09`                                                                        | COMPLETE                   | MAIN TARGETED PASS; live blocked                           | PARTIAL | Page and all-ID call paths were traced; all-ID redownload is unbounded and sequential.                                                                                                                                                |
| Question attachment create             | `MEM-16`                                                                        | COMPLETE                   | MAIN TARGETED PASS; live blocked                           | PARTIAL | Multipart storage path exists; authoritative backend attachment limits are absent.                                                                                                                                                    |
| Question attachment download/delete    | `MEM-17`                                                                        | COMPLETE                   | MAIN TARGETED PASS; live blocked                           | PARTIAL | Private storage, safe response headers, and after-commit deletion exist. Public attachments correctly inherit public Question viewing permission; private Questions remain author/admin only. No live byte evidence was run.          |
| Company Certification private document | `ADM-06`                                                                        | COMPLETE                   | MAIN TARGETED PASS; live blocked                           | PARTIAL | Admin-only composite lookup, private root, path hiding, response hardening, validation, and narrow access-grant audit are present. Full-buffer delivery and no live byte proof remain.                                                |
| Notice attachment create               | `ADM-12`                                                                        | COMPLETE                   | MAIN TARGETED PASS; live blocked                           | PARTIAL | Journaled storage exists; backend count/size/empty/name/type enforcement is incomplete.                                                                                                                                               |
| Notice preserve/replace/remove         | `ADM-13`                                                                        | COMPLETE                   | MAIN TARGETED PASS; live blocked                           | PARTIAL | Unmentioned attachments are preserved and selected attachments are deleted after commit; no H2 plus real-files integration proof was found in the inspected tests.                                                                    |
| Shared Track binary contract           | `PUB-02`, `PUB-03`, `PUB-06`, `MEM-02`, `MEM-05`, `MEM-07`, `MEM-09`, PlayerBar | COMPLETE                   | MAIN TARGETED PASS; live blocked                           | PARTIAL | All implemented `downloadTrack` entry points were traced. Album-detail per-Track download remains an unsettled requirement from `WI-20260809-ATS-023-findings.md:52-54`.                                                              |
| Storage journal/recovery/cleanup       | Notice, Question, Company Certification, shared storage services                | COMPLETE                   | MAIN TARGETED PASS (mock/`@TempDir`); restart/live blocked | PARTIAL | Coordinator, journal, retry/recovery, reference-check, and local path defenses exist; no inspected test combines H2 transactions, real temp storage, and restart recovery.                                                            |
| Whitelist immutable CSV                | `ADM-11`                                                                        | COMPLETE                   | MAIN TARGETED PASS; live binary blocked                    | PARTIAL | Applied-scope, authorization, immutable snapshots, headers, filename, BOM bytes, escaping, formula neutralization, bounds, and replay assertions were inspected. Confirmation and unknown-outcome retry defects remain.               |
| Settlement CSV import/reconcile/ignore | `ADM-10`                                                                        | COMPLETE                   | MAIN TARGETED PASS; live import/durable proof blocked      | PARTIAL | UI, multipart API, parser, row validation, duplicate handling, transaction/audit, reconcile, ignore, and reload assertions were inspected. No ATStudio Settlement CSV export or pre-import preview contract exists.                   |

## Checkpoint Counts

Controls are not counted as defects.

| Scope              |  P1 |  P2 | Defects | Control-only independent findings |
| ------------------ | --: | --: | ------: | --------------------------------: |
| Part A             |   2 |   5 |       7 |                         1 (`A02`) |
| Part B             |   6 |   3 |       9 |                                 0 |
| Overall checkpoint |   8 |   8 |      16 |                                 1 |

## Independent Findings

### F-INTEG-029-A01 - P1 / SPECIFICATION GAP - Question backend validation is absent; exact Notice/Question file limits are not canonicalized

**Pointers**

- UI limits: `frontend/src/utils/validation.ts:32-37`.
- Declared backend constants: `src/main/java/com/atstudio/atstudio/common/validation/ValidationConstants.java:39-44`.
- Servlet envelope: `src/main/resources/application.yml:33-37`; unlimited Tomcat part count: `src/main/java/com/atstudio/atstudio/config/AppConfig.java:18-28`.
- Notice persistence: `src/main/java/com/atstudio/atstudio/service/NoticeService.java:167-189`.
- Question persistence: `src/main/java/com/atstudio/atstudio/service/QuestionService.java:217-240`.
- Existing assertion explicitly accepts and silently filters null/empty Notice parts: `src/test/java/com/atstudio/atstudio/service/NoticeServiceBranchCoverageTest.java:70-105`.
- Question use case requires optional attachment upload followed by frontend and backend validation, storage, and attachment-row creation: `docs/design/usecase/user-question.md:20-32`.
- Notice create/update use cases do not define attachment upload mutation, allowed type, count, per-file size, or aggregate size: `docs/design/usecase/user-notice.md:20-27,87-105`. Notice detail only states that attachments are returned: `docs/design/usecase/user-notice.md:61-83`.

**Lanes**

- UI: the forms apply five-file and 20 MiB checks, but the use-case documents do not establish those exact values as canonical product policy.
- Request: direct multipart callers can bypass the UI.
- HTTP: the infrastructure envelope permits 30 MiB per file, 60 MiB per request, and unlimited part count. Neither service applies domain attachment validation. For Question this fails the documented backend-validation step; for Notice the exact required validation contract is unspecified.
- Headers / filename / bytes: non-empty parts are accepted without a bounded filename or documented type/signature rule; empty parts are silently omitted instead of rejecting the request. Neither corrected use-case document defines allowed types, five-file count, 20 MiB per-file size, or aggregate size.
- Durable storage: accepted non-empty parts are journaled and persisted with caller-provided original-name metadata; silently omitted parts create no attachment row.

**Impact**

API callers can exceed the UI/declaration limits and consume durable storage. A successful create/update can also contain fewer attachments than the submitted request without an explicit rejection. Question has a confirmed implementation/document defect because the documented backend-validation step is absent. Notice has a specification gap: its use case does not describe attachment creation/update validation, so exact type/count/size enforcement cannot be classified against that document.

**Bounded follow-up**

First canonicalize allowed extensions, MIME/signature handling, count, per-file size, aggregate size, filename rules, and empty-part behavior for both domains. Then add one backend validator used before any storage/entity mutation and rejection/rollback tests. At minimum, Question must perform meaningful backend validation as required by `user-question.md`; this audit does not promote the current UI/constants values into policy by assumption.

### F-INTEG-029-A02 - CONTROL / DOCUMENT MATCH - Question attachments inherit Question viewing permission

**Pointers**

- Download and private-root load: `src/main/java/com/atstudio/atstudio/service/QuestionService.java:154-163`.
- Access rule returns immediately for every public Question: `src/main/java/com/atstudio/atstudio/service/QuestionService.java:208-214`.
- Existing test locks access by an unrelated authenticated user for a public Question: `src/test/java/com/atstudio/atstudio/service/QuestionServiceTest.java:353-378`.
- Owner/admin-only behavior is asserted only for private Questions: `src/test/java/com/atstudio/atstudio/service/QuestionServiceTest.java:380-443`.
- Matrix boundary: `deliverables/agent/WI-20260809-ATS-020-acceptance-matrix.md:175-176`.
- Public Questions are visible to regular members, while private Questions are restricted to the author/admin: `docs/design/usecase/user-question.md:65-84,88-109`.
- Attachment permission is explicitly the same as Question viewing permission: `docs/design/usecase/user-question.md:113-134`.

**Lanes**

- UI: an attachment action follows the Question detail returned to a viewer who has Question viewing permission.
- Request: `GET /api/questions/{questionId}/attachments/{attachmentId}` requires authentication.
- HTTP: unauthenticated access is 401; an unrelated authenticated member may receive a public Question attachment; unrelated access to a private Question is denied. This exactly matches `QUESTION-003` through `QUESTION-005`.
- Headers / filename / bytes: the controller returns exact full bytes as `application/octet-stream`, RFC 5987 encoded disposition, `no-store, private`, `nosniff`, sandbox CSP, and `Accept-Ranges: none` (`QuestionController.java:89-113`; assertions at `QuestionControllerTest.java:153-208`).
- Durable storage: the key remains in the PRIVATE root and raw `/uploads/questions/**` access is denied (`QuestionControllerTest.java:211-247`).

**Classification**

Implementation/document match. PRIVATE storage and raw-path denial protect the storage boundary; application-level download authorization intentionally follows Question visibility. The matrix phrase "private file boundary" does not override the explicit use-case rule for public Questions.

**Bounded follow-up**

No product/security decision remains for A02. Preserve the current public/private authorization tests. Clarify the matrix wording only if readers could interpret PRIVATE storage as universal author/admin authorization.

### F-INTEG-029-A03 - P1 / CONTRACT DECISION REQUIRED - First-download state can commit before byte transfer completes

**Pointers**

- First-download history, License, and counter mutation precede Resource resolution: `src/main/java/com/atstudio/atstudio/service/DownloadService.java:43-95`.
- Controller returns the Resource for later HTTP-body transfer: `src/main/java/com/atstudio/atstudio/controller/TrackController.java:94-103`.
- Unit order assertion proves the same sequence using mocks: `src/test/java/com/atstudio/atstudio/service/DownloadServiceTest.java:58-97`.
- Existing-License redownload correctly skips duplicate history, License, subscription/quota, and count mutation: `src/test/java/com/atstudio/atstudio/service/DownloadServiceTest.java:99-117`.
- Controller download test asserts only 401/200, not filename, headers, bytes, or transfer failure: `src/test/java/com/atstudio/atstudio/controller/TrackControllerTest.java:439-457`.
- License use cases confirm automatic License issuance on download and allow issued Licenses to remain viewable after subscription expiry, but do not define Track redownload entitlement, quota treatment, filename, or delivery completion: `docs/design/usecase/user-license.md:6-8,12-32`.

**Lanes**

- UI: named entry points request a Blob and report success after Axios resolves.
- Request: `GET /api/tracks/{trackId}/download` is authenticated and entitlement checks run under a per-user pessimistic lock.
- HTTP: Resource lookup failure inside `DownloadService.download` rolls the transaction back. A stream/socket failure while Spring writes the returned Resource occurs after the transactional service has returned.
- Headers / filename / bytes: the response is octet-stream with a generated storage filename; no inspected test proves non-empty bytes or a failed-transfer result.
- Durable storage: the first request writes `track_downloads`, `licenses`, and `download_count`; the current implementation treats an existing License as redownload with no duplicate durable state. That redownload entitlement behavior is a source fact, not a requirement established by `user-license.md`.

**Impact**

A post-service body-transfer failure can leave history, License, and count state that the client did not receive. Client disconnect and completed delivery cannot be made atomic with the database, so the acceptance meaning of "download success" must be explicit.

**Bounded follow-up**

Define success as either entitlement/resource grant or completed delivery. Add preflight readability and response-body failure tests. If completed delivery is required, model an attempt/grant/completion state or reconciliation contract instead of treating a database transaction as proof that client bytes completed.

### F-INTEG-029-A04 - P2 - Binary filename and byte validation are inconsistent across clients

**Pointers**

- Track API discards response headers and returns only Blob: `frontend/src/api/downloads.ts:13-19`.
- Shared trigger trusts a caller filename and immediately revokes its object URL: `frontend/src/api/downloads.ts:79-89`.
- Notice and Question clients trust metadata filenames rather than the response disposition: `frontend/src/api/notices.ts:76-91`; `frontend/src/api/questions.ts:112-128`.
- Company Certification is the sole inspected client that parses `Content-Disposition`: `frontend/src/api/admin.ts:128-145`.
- Track and Notice controllers expose generated Resource filenames: `TrackController.java:94-103`; `NoticeController.java:80-91`.
- Question and Company Certification controllers expose encoded original filenames and hardened headers: `QuestionController.java:89-113`; `CompanyCertificationController.java:99-120`.
- API tests assert caller-supplied filenames/object-URL clicks, not response filename or non-empty payload: `frontend/src/api/domainApis.test.ts:92-129,208-218,443-453`. Company header parsing is asserted at `frontend/src/api/adminContracts.test.ts:131-138`.
- `user-license.md` defines License list/detail fields and ownership but no Track download filename, media extension, response-header, or byte contract: `docs/design/usecase/user-license.md:12-32,57-80`.

**Lanes**

- UI: Track callers synthesize `${title}.mp3`; Notice/Question callers use response metadata; Company Certification uses an HTTP header filename.
- Request: all use `responseType: 'blob'`.
- HTTP headers / filename / bytes: no shared parser, sanitization, content-type check, or `Blob.size > 0` check exists. The Track/Notice browser filename can disagree with the server disposition and actual media type.
- Durable storage: this does not mutate storage, but it can label delivered bytes incorrectly and show a false-success file action for a zero-byte Blob.

**Impact**

The same binary contract produces different filenames by entry point, forces `.mp3` without evidence from the response, and can treat an empty Blob as success. This is an integration consistency/evidence defect; it is not classified as a violation of `user-license.md`, which is silent on filename and byte delivery.

**Bounded follow-up**

Return `{ blob, filename, contentType }` from one shared binary helper, robustly parse and sanitize RFC 5987/basic disposition, reject empty payloads, and use a bounded fallback based on stable ID plus validated media metadata.

### F-INTEG-029-A05 - P2 - Duplicate-request fencing is inconsistent across named download entry points

**Pointers**

- PlayerBar has a synchronous `downloading` fence: `frontend/src/layouts/PlayerBar.tsx:179-204`.
- Track detail sets a download state and refreshes count: `frontend/src/pages/public/TrackDetailPage.tsx:75-102`.
- Track list has no per-row pending fence: `frontend/src/pages/public/TrackListPage.tsx:854-878`.
- License list redownload has no pending identity/fence: `frontend/src/pages/subscriber/LicenseListPage.tsx:48-57`.
- Likes and Playlist detail have no pending fence: `frontend/src/pages/subscriber/LikeListPage.tsx:95-101`; `frontend/src/pages/subscriber/PlaylistDetailPage.tsx:95-102`.
- History has per-row/bulk state and runs bulk IDs sequentially: `frontend/src/pages/subscriber/DownloadHistoryPage.tsx:162-207`.
- Existing Track-detail assertions verify invocation/success/failure, not duplicate-click suppression: `frontend/src/test/coverage/publicAuthShell.coverage.test.tsx:790-839`.

**Lanes**

- UI: some buttons disable while pending; others remain actionable and can issue parallel requests. Notice and Question pending/error omissions are already owned by `WI-20260809-ATS-024-findings.md:127-134` and `WI-20260809-ATS-025-findings.md:145-153` and are not duplicated here.
- Request / HTTP: duplicate GETs can reach the same endpoint. The backend user lock plus existing-License branch protects first-download ledger duplication, but does not prevent duplicate byte work or redownload requests.
- Headers / filename / bytes: every successful duplicate produces another client file action.
- Durable storage: first-download history/License/count duplication is fenced by service locking and the existing-License path; network/storage load remains duplicated.

**Impact**

Rapid actions can create duplicate downloads, load, and conflicting feedback. License/history views are especially likely to produce repeated redownloads while preserving only one durable first-download record.

**Bounded follow-up**

Adopt one per-Track pending registry in the shared download action, disable every matching entry point, and add double-click plus concurrent-entry-point tests. Keep the existing server-side user lock and unique License boundary.

### F-INTEG-029-A06 - P2 - Storage recovery is not proven end-to-end with H2 plus real temp files

**Pointers**

- Real filesystem boundary: `src/test/java/com/atstudio/atstudio/service/storage/LocalStorageServiceTest.java:22-216` uses `@TempDir` for stage/promote/load/delete and path/root defenses.
- Coordinator transaction callbacks and cleanup are Mockito tests: `StorageMutationCoordinatorTest.java:28-193`.
- Journal state transitions/claims/retries are Mockito tests: `StorageMutationJournalServiceTest.java:23-109`.
- Recovery/backoff/reference behavior is Mockito-only: `StorageMutationRecoveryServiceTest.java:21-134`; `StorageMutationRecoveryVerificationTest.java:21-72`.
- Reference-domain routing is Mockito-only: `StorageReferenceCheckerBranchCoverageTest.java:24-55`.

**Lanes**

- UI/request/HTTP: not applicable to the internal storage recovery engine.
- Filename/bytes: local-storage tests exercise generated keys, unsafe paths, roots, staging, promotion, load, and deletion against a temporary directory.
- Durable storage: coordinator/journal/recovery assertions cover rollback cleanup, after-commit old-file deletion, shared-reference retention, retry limits, stale mutations, and idempotency, but not through a real H2 transaction plus real filesystem plus simulated restart in one test.

**Impact**

The important crash windows are represented by separate unit contracts, but transaction propagation, committed journal visibility, filesystem state, and startup replay are not demonstrated together.

**Bounded follow-up**

Add isolated integration tests using H2 and `@TempDir` that interrupt create/replace/delete at prepared, promoted, committed, and cleanup-failed states, instantiate recovery as a restart boundary, and assert database references, journal terminal state, old/new bytes, idempotency, and retry bounds.

### F-INTEG-029-A07 - P2 - Private document controllers buffer the full file in heap

**Pointers**

- Question: `src/main/java/com/atstudio/atstudio/controller/QuestionController.java:89-113`.
- Company Certification: `src/main/java/com/atstudio/atstudio/controller/CompanyCertificationController.java:99-120`.
- Certification per-file maximum is 20 MiB: `ValidationConstants.java:46-54`; Question currently remains under the 30 MiB servlet envelope because finding A01 is open.

**Lanes**

- UI/request: authenticated Question or admin-only Certification GET.
- HTTP headers / filename / bytes: both call `StreamUtils.copyToByteArray` before returning the hardened full-byte response; Range is deliberately disabled.
- Durable storage: read-only; no storage mutation. Company Certification records a narrow access-grant audit before delivery, which the security policy explicitly does not define as byte completion.

**Impact**

Each concurrent download allocates the complete file body in application heap. The effective Question size can currently exceed the intended 20 MiB product limit.

**Bounded follow-up**

After preserving the no-range and hardened-header contract, use a streaming Resource response with a bounded content length and add concurrent large-file memory tests. Keep access-grant audit wording distinct from completed delivery.

### F-INTEG-029-A08 - P2 - Download-all has no server or client batch bound

**Pointers**

- Client requests every matching Track ID: `frontend/src/api/downloads.ts:69-76`.
- Client then downloads all IDs sequentially: `frontend/src/pages/subscriber/DownloadHistoryPage.tsx:176-236`.
- Backend all-ID route and service path: `src/main/java/com/atstudio/atstudio/controller/DownloadController.java:30-55`; `src/main/java/com/atstudio/atstudio/service/DownloadService.java:104-165`.
- Existing page test covers a small selected set, not a high-cardinality bound: `frontend/src/pages/subscriber/DownloadHistoryPage.test.tsx:170-179`.

**Lanes**

- UI: a confirmation is shown, then the browser performs a sequential loop.
- Request/HTTP: the ID endpoint returns the entire matching set with no requested maximum; each Track generates a separate Blob GET.
- Headers/filename/bytes: filenames are synthesized and every Blob remains subject to finding A04.
- Durable storage: these are existing-License redownloads when history is canonical, so no duplicate first-download record should be added.

**Impact**

A long-lived account can trigger a very large response followed by an unbounded browser workload with no cancellation, resume cursor, or batch ceiling.

**Bounded follow-up**

Define a product maximum, paginate/cursor the ID selection, show the selected count before confirmation, and add cancellation/resume or a bounded batch loop with tests at and beyond the limit.

### F-INTEG-029-B01 - P1 - Keyword-only Whitelist export confirmation misstates the status mutation

**Pointers**

- The use case permits an explicit status and/or applied keyword and requires `PENDING` rows to become `EXPORTED`: `docs/design/usecase/whitelist.md:259-267`.
- Draft and applied keywords are separate, and export sends the applied keyword: `frontend/src/pages/admin/WhitelistChannelManagePage.tsx:66-113,176-185,251-279`.
- The confirmation warns about status transition only when the selected status is exactly `PENDING`; the keyword-only/ALL branch says status will not change: `WhitelistChannelManagePage.tsx:186-191`.
- Backend selection accepts a null status, snapshots every matching status, and marks every matching `PENDING` row exported: `src/main/java/com/atstudio/atstudio/service/AdminWhitelistChannelService.java:144-177,192-218`.
- Existing UI assertions cover `PENDING` export and applied keyword search, but not keyword-only/ALL confirmation: `frontend/src/test/coverage/adminSubscriberGaps.coverage.test.tsx:891-966`.

**Lanes**

- UI: draft filters are correctly excluded until Search/Enter applies them, but `ALL` plus an applied keyword is enabled under misleading confirmation copy.
- Request: the client sends `{ status: undefined, keyword: <applied> }`, so this is not a stale-draft request defect.
- HTTP/status: the ADMIN-only export can return 200 while the confirmation has told the operator that statuses will remain unchanged.
- Headers / filename / bytes: the returned CSV contract remains intact; the defect is the pre-mutation representation of its scope.
- Durable state: all matching rows are snapshotted and matching `PENDING` rows transition to `EXPORTED`, even though other matching statuses remain unchanged.

**Impact**

An operator can confirm a mixed-status keyword export believing it is snapshot-only while registration-request rows irreversibly advance to external-processing state.

**Bounded follow-up**

For keyword-only scope, either require an explicit status or state that all matching statuses are included and matching `PENDING` rows become `EXPORTED`. Add a UI assertion for `ALL` plus applied keyword and verify the exact confirmation before invocation.

### F-INTEG-029-B02 - P1 - Whitelist export has no recoverable operation identity after an unknown response

**Pointers**

- The batch is committed with filters/items and `PENDING` status changes before the service returns: `AdminWhitelistChannelService.java:179-220`.
- Batch identity exists only in the successful response header and body metadata: `src/main/java/com/atstudio/atstudio/controller/AdminWhitelistChannelController.java:62-85`; `frontend/src/api/admin.ts:206-253`.
- The page reports a generic export failure and can replay only a batch ID already known to the operator: `WhitelistChannelManagePage.tsx:193-230`.
- Byte-stable replay is asserted only after a known batch ID is supplied: `src/test/java/com/atstudio/atstudio/service/AdminWhitelistChannelServiceTest.java:406-431`; `frontend/src/api/adminWhitelistChannels.test.ts:60-94`.

**Lanes**

- UI: an interrupted/filtered response yields only a generic error; there is no export history or recovery lookup.
- Request: `POST /export` has no client operation key or retry token.
- HTTP/status: loss after commit but before the response header reaches Axios is indistinguishable from a pre-commit failure.
- Headers / filename / bytes: `X-Whitelist-Export-Batch-Id` is sufficient only when the response arrives; deterministic replay cannot start without it.
- Durable state: the original batch and status transitions can exist. A `PENDING` retry can create a new header-only empty batch; a keyword-only retry can snapshot matching non-pending/current rows again.

**Impact**

The operator can lose both the CSV and its batch ID while channels remain `EXPORTED`, leaving external registration work stalled or causing duplicate export evidence on retry.

**Bounded follow-up**

Add a client operation key with idempotent response replay, or an ADMIN export-history/recent-operation lookup keyed by actor, recorded scope, and time. Assert response-loss recovery, same-key replay, and no second batch/status mutation.

### F-INTEG-029-B03 - P1 - Partial Settlement import is reported as success and clears retry context

**Pointers**

- Row validation exceptions are accumulated while valid rows continue to save and audit in one import response: `src/main/java/com/atstudio/atstudio/service/AdminPaymentSettlementService.java:78-127`.
- The UI unconditionally clears React file state, emits a success toast, and reloads after every 200 response: `frontend/src/pages/admin/PaymentOperationsPage.tsx:426-448`.
- Failed counts and only the first five row errors are rendered after that success: `PaymentOperationsPage.tsx:1048-1065`.
- The file input is uncontrolled, so `setSettlementFile(null)` does not clear its DOM value: `PaymentOperationsPage.tsx:1027-1035`.
- The page test proves one confirmed request and reload for an all-success mock only; the service test uses seven invalid rows and no mixed valid/invalid file: `frontend/src/pages/admin/PaymentOperationsPage.test.tsx:409-443`; `src/test/java/com/atstudio/atstudio/service/AdminPaymentSettlementServiceTest.java:232-263`.

**Lanes**

- UI: `failedRows > 0` still produces full-success feedback, clears the selected-file state, and leaves the underlying file input stale.
- Request: duplicate confirmation clicks are fenced, but a failed-row retry has no retained file/error-row workflow.
- HTTP/status: partial validation is represented as 200 with imported, duplicate, and failed counts; the UI treats only transport rejection as failure.
- Parser/result: row errors are available but truncated to five on screen and not classified as partial success.
- Durable state: valid rows and their audit entries commit; invalid rows do not. Reload therefore shows a partial durable outcome despite the full-success toast.

**Impact**

An operator can leave a financial-evidence import believing it completed, while rows remain absent and selecting the same file for correction/retry can be unreliable.

**Bounded follow-up**

Render zero-failure success separately from partial completion, retain the file/note and complete error list for partial results, and explicitly reset the DOM input only after a fully successful decision. Add a mixed valid/invalid response test that asserts warning/error feedback, retained retry context, one reload, and exact durable counts.

### F-INTEG-029-B04 - P1 - Settlement IGNORE note and retry integrity are enforced only by the UI

**Pointers**

- The runbook permits IGNORE only after verification, and the API/policy describes an operator note plus required actor/status/reason/note audit fields: `docs/design/payment-operations-runbook.md:267-275`; `docs/design/payment-refund-receipt-settlement-policy.md:528-545`.
- The UI trims and requires a note before confirmation: `PaymentOperationsPage.tsx:479-503`; assertion at `frontend/src/test/coverage/adminSubscriberGaps.coverage.test.tsx:1096-1138`.
- The request DTO has only `@Size(max = 500)`, so null, empty, and blank notes pass validation: `src/main/java/com/atstudio/atstudio/dto/payment/AdminPaymentSettlementIgnoreRequest.java:1-8`.
- The service accepts a null request/note, and the entity always rewrites status, ignored actor/time, and note: `AdminPaymentSettlementService.java:237-256`; `src/main/java/com/atstudio/atstudio/entity/PaymentSettlement.java:145-169`.
- Existing tests expressly accept a system actor and omitted note; no repeat-call invariant is asserted: `AdminPaymentSettlementServiceTest.java:410-475`.

**Lanes**

- UI: the normal screen has the required note and a danger confirmation.
- Request: a direct ADMIN request can send `{}`, `null`, or a blank note; a retry can send a different note.
- HTTP/status: the controller still returns success because `@Valid` has no nonblank constraint.
- Headers / filename / bytes: not applicable; this is a ledger mutation endpoint.
- Durable state/audit: each call overwrites `ignoredAt`, actor, and note and records another audit event, including an `IGNORED` to `IGNORED` retry.

**Impact**

Evidence can be removed from active review without the required rationale, and an ambiguous retry can replace the original operator evidence instead of proving the first decision.

**Bounded follow-up**

Require a trimmed nonblank note in DTO and service, define `IGNORED` as an idempotent same-decision no-op or explicit conflict, and preserve the first decision fields. Add direct HTTP blank-note tests and repeated-call audit/state assertions.

### F-INTEG-029-B05 - P1 - Settlement CSV decoding and grammar are lenient enough to alter evidence silently

**Pointers**

- The UI uses only an `accept` hint and performs no filename, media type, byte-size, or encoding validation: `PaymentOperationsPage.tsx:1027-1035`.
- Backend rejects only null/empty files before decoding every input as UTF-8: `AdminPaymentSettlementService.java:78-86,369-399`.
- Header validation is `containsAll`; duplicate headers and row-width mismatches are not rejected: `AdminPaymentSettlementService.java:389-406`.
- Parsing is physical-line based, does not reject an unmatched quote, and cannot preserve a quoted newline: `AdminPaymentSettlementService.java:408-430`.
- Existing tests cover BOM, quoted commas, and doubled quotes, but not malformed UTF-8, duplicate headers, unmatched quotes, quoted newlines, or extra/missing cells: `AdminPaymentSettlementServiceTest.java:201-295`.
- The runbook states CSV input and intentional ignoring of unknown columns, but does not define the filename/MIME/size envelope or CSV dialect: `docs/design/payment-operations-runbook.md:263-280`.

**Lanes**

- UI: browser `accept` is advisory and can be bypassed.
- Request: any non-empty multipart part reaches the parser; original filename and part content type are not acceptance gates.
- HTTP/parser: the standard UTF-8 reader replaces malformed byte sequences, and the custom per-line parser accepts or splits malformed CSV instead of producing a file-level error.
- Filename/bytes: filename is only truncated as metadata; no macro is executed, but byte-to-row fidelity is not strict.
- Durable state: a syntactically accepted, altered identifier/value can be reconciled, persisted, and audited as provider evidence.

**Impact**

Malformed provider evidence can become different durable rows without a deterministic import error, undermining later ledger comparison and duplicate detection.

**Bounded follow-up**

Use a proven CSV parser with a strict UTF-8 decoder, balanced quoting and quoted-newline support, unique normalized headers, and explicit row-width checks while retaining the documented unknown-column behavior. Canonical filename/MIME/byte-size and CSV-dialect policy remains a product/spec decision; do not infer exact limits from the browser hint.

### F-INTEG-029-B06 - P1 - Settlement financial and provider fields are not validated to their durable representation

**Pointers**

- Amount parsing removes every comma and accepts any nonnegative `BigDecimal`; currency accepts any three characters: `AdminPaymentSettlementService.java:440-465`.
- Provider enum, order length, date shape, optional provider identifiers, and silent identifier/file-name truncation are applied in `toSettlement`: `AdminPaymentSettlementService.java:311-365`.
- Durable amount columns are `DECIMAL(15,2)` and provider identifiers are 200 characters: `src/main/java/com/atstudio/atstudio/entity/PaymentSettlement.java:73-120`.
- Current tests reject negative/nonnumeric values, a 65-character order, an invalid date, and four-character currency, but do not cover malformed grouping, exponent notation, precision/scale overflow, non-ISO three-letter currency, or overlong provider IDs: `AdminPaymentSettlementServiceTest.java:232-295`.

**Lanes**

- UI: file contents receive no local field validation.
- Request: fields arrive as untyped CSV strings.
- HTTP/parser: values such as malformed comma grouping or exponent notation can parse; the service does not compare precision/scale with the entity contract.
- Filename/bytes/row: overlong provider IDs are used in the dedup basis and then truncated in persisted evidence, so displayed/stored identity can differ from dedup identity.
- Durable state: the database may round/reject out-of-contract decimals depending on enforcement, and arbitrary three-letter currency values can be stored as if canonical.

**Impact**

Financial amounts can be changed or fail late at persistence, and provider evidence can be stored under an identity representation different from the source and dedup key.

**Bounded follow-up**

Define canonical amount notation, precision/scale, currency set, and provider-identifier lengths, then reject violations before reconciliation or audit. Add boundary assertions against the actual persistence representation; exact accepted values require policy confirmation rather than an invented rule.

### F-INTEG-029-B07 - P2 - Settlement duplicate handling is sequential, not atomic or file-auditable

**Pointers**

- Every import creates a random batch key, checks `existsByDeduplicationKey`, then saves each row: `AdminPaymentSettlementService.java:78-109`.
- The repository check and insert are separate operations: `src/main/java/com/atstudio/atstudio/repository/PaymentSettlementRepository.java:16-20`.
- A database unique constraint is the final duplicate fence: `PaymentSettlement.java:30-36`.
- The existing duplicate test mocks a prior row and proves sequential skip only: `AdminPaymentSettlementServiceTest.java:156-173`.
- The handoff-named `PaymentMysqlConcurrencyIntegrationTest` and `PaymentMysqlSchemaValidationTest` contain no Settlement entity/repository/service references and therefore establish no import race or transaction behavior.

**Lanes**

- UI: one page instance fences duplicate confirmation, but separate tabs/operators remain concurrent.
- Request: there is no file hash, idempotency key, or durable import-attempt ID supplied by the client.
- HTTP/result: two concurrent files can both pass the existence check; one can receive a generic unique-constraint failure rather than duplicate-row counts.
- Parser/rows: a later sequential replay is row-idempotent, but an all-duplicate file creates a new response batch key with no new rows carrying it.
- Durable state/audit: the unique constraint prevents duplicate rows, while the losing transaction can roll back wholesale and duplicate-file attempts have no durable batch-level audit record.

**Impact**

Concurrent or unknown-outcome imports are durable-row safe but operationally ambiguous: an operator can receive failure for evidence already imported elsewhere and cannot identify a durable file-level attempt.

**Bounded follow-up**

Make dedup insertion atomic with a classified duplicate outcome and add a durable import-batch record keyed by client operation ID or approved file fingerprint. Add a concurrent database integration test that asserts one row, deterministic results for both requests, and audit/batch consistency.

### F-INTEG-029-B08 - P2 - Settlement reconciliation has no explicit range or row bound

**Pointers**

- Omitted dates default to 30 days, but explicit dates have no maximum span: `AdminPaymentSettlementService.java:147-161`.
- All matching `DONE` payments are loaded into a `List` and processed with per-row lookup/save/audit inside one transaction: `AdminPaymentSettlementService.java:162-235`.
- UI confirmation and pending state do not impose a date or result cap: `PaymentOperationsPage.tsx:454-477`.
- Existing tests cover inverted and omitted boundaries only, not a maximum span/cardinality or interrupted retry: `AdminPaymentSettlementServiceTest.java:361-386`.

**Lanes**

- UI: an operator can submit an arbitrarily wide explicit date range.
- Request: reconcile has no page, cursor, max rows, or operation key.
- HTTP/status: a large synchronous request can time out with an unknown transaction outcome.
- Headers / filename / bytes: not applicable; the response is a JSON aggregate.
- Durable state/audit: one transaction may create and audit every missing-evidence row, with check-then-insert races against another reconciliation/import.

**Impact**

Historical volume can produce a long transaction, elevated memory/query load, lock contention, and ambiguous retry behavior.

**Bounded follow-up**

Set an approved maximum date span and row cap, page with a stable cursor, commit bounded batches, and expose operation progress/replay. Add at-limit, over-limit, concurrent-run, and interrupted-retry tests.

### F-INTEG-029-B09 - P2 - Reconciliation summary silently loses unusable rows from all outcome counters

**Pointers**

- A `DONE` payment without an order is skipped without incrementing duplicate or failed counters: `AdminPaymentSettlementService.java:167-172`.
- Response `totalRows` still uses the complete payment-list size while `failedRows` is fixed to zero: `AdminPaymentSettlementService.java:225-233`.
- The existing assertion locks `totalRows=3`, `importedRows=1`, and `skippedDuplicateRows=1` for a list containing one orderless payment: `AdminPaymentSettlementServiceTest.java:321-359`.
- The UI displays total/imported/duplicates/failed as the operator reconciliation summary: `PaymentOperationsPage.tsx:1048-1055`.

**Lanes**

- UI: the displayed totals do not explain every examined row.
- Request: the selected period can include unusable local payment records.
- HTTP/result: the successful JSON response violates the natural invariant that all total rows have an outcome.
- Headers / filename / bytes: not applicable.
- Durable state/audit: the orderless row creates neither review evidence nor audit evidence and is invisible after the aggregate response.

**Impact**

Operators cannot distinguish an intentionally skipped record from an implementation omission, weakening the completeness claim of the missing-provider-evidence review.

**Bounded follow-up**

Classify orderless rows as failed/unusable with a bounded error detail or create a separate review incident. Assert `total = imported + duplicate + failed/unusable` for every response.

## Confirmed Controls

| Control                                            | Evidence                                                                                                                                                                                         | Status at checkpoint                                                                                                                                                                                                                                                                                |
| -------------------------------------------------- | ------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------ | --------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| Notice attachment ownership/composite binding      | `NoticeService.java:145-153`; `NoticeServiceBranchCoverageTest.java:153-176`                                                                                                                     | Confirmed by source/inspected assertion; main's targeted backend suite passed.                                                                                                                                                                                                                      |
| Notice preserve/remove behavior                    | `NoticeService.java:110-123`; `NoticeServiceBranchCoverageTest.java:127-150`                                                                                                                     | Unmentioned attachment preserved; selected attachment schedules after-commit deletion.                                                                                                                                                                                                              |
| Question static-path denial and hardened response  | `QuestionControllerTest.java:153-247`                                                                                                                                                            | Exact byte, disposition, cache, nosniff, CSP, no-range, and raw-path assertions inspected.                                                                                                                                                                                                          |
| Question delete cleanup boundary                   | `QuestionService.java:178-198`; `QuestionServiceTest.java:556-581`                                                                                                                               | Owner/status/admin guard plus PRIVATE after-commit cleanup is asserted.                                                                                                                                                                                                                             |
| Company Certification private contract             | `CompanyCertificationController.java:99-120`; `CompanyCertificationControllerTest.java:251-343`; `CompanyCertificationServiceTest.java:742-769`                                                  | Admin-only, composite document ownership, exact full bytes, safe headers, raw static denial, and narrow access-grant audit are asserted.                                                                                                                                                            |
| Company Certification validation                   | `CompanyCertificationService.java:303-403`                                                                                                                                                       | Count, per-file/aggregate size, filename, extension, signature/MIME, and PNG canonicalization controls are present.                                                                                                                                                                                 |
| Track first-download serialization                 | `DownloadService.java:43-95`; `DownloadServiceTest.java:58-117`; `docs/design/usecase/user-license.md:6-8,12-32`                                                                                 | Per-user lock and existing-License branch prevent duplicate first-download durable state. Automatic issuance is documented; post-license redownload entitlement/quota behavior is implementation-defined by the inspected use case.                                                                 |
| Storage mutation boundaries                        | `StorageMutationCoordinatorTest.java:50-193`; `StorageMutationRecoveryServiceTest.java:39-134`; `LocalStorageServiceTest.java:22-216`                                                            | Rollback/new cleanup, after-commit old cleanup, reference retention, bounded retry/recovery, idempotency, and local path defenses are represented by isolated tests.                                                                                                                                |
| Whitelist scope, bound, and lock order             | `AdminWhitelistChannelService.java:144-177`; `AdminWhitelistChannelServiceTest.java:131-152,374-404`; `WhitelistConcurrencyContractTest.java:23-60`                                              | Explicit status/applied-keyword scope, maximum-plus-one rejection before mutation, stable user/channel lock order, and source-level pessimistic/version fences are present.                                                                                                                         |
| Whitelist immutable batch                          | `AdminWhitelistChannelService.java:179-247`; `WhitelistExportItem.java:23-67`; `WhitelistExportItemRepository.java:13-23`; `schema.sql:276-315`; `AdminWhitelistChannelServiceTest.java:406-449` | Batch rows hold ordered snapshots, replay avoids current channel re-query, and nullable channel linkage with `ON DELETE SET NULL` preserves replay after later user edit/delete. No H2 edit/delete proof was run.                                                                                   |
| Whitelist CSV and response contract                | `AdminWhitelistChannelService.java:299-357`; `AdminWhitelistChannelController.java:62-85`; `AdminWhitelistChannelServiceTest.java:80-129,154-251`; `adminWhitelistChannels.test.ts:23-94`        | UTF-8 BOM, `userEmail` and channel/subscription headers, quoting, formula neutralization, deterministic bytes, batch header, encoded filename parsing, and replay-ID validation are asserted. Controller byte/header assertions remain incomplete.                                                  |
| Settlement ADMIN and mutation boundary             | `AdminPaymentController.java:106-149`; `payment-operations-runbook.md:263-280`; `AdminPaymentSettlementService.java:78-256`                                                                      | List/import/reconcile/ignore routes are ADMIN-only in source and mutate Settlement/audit lanes rather than entitlement, payment, refund, or Provider state. Route-specific authorization assertions were not found.                                                                                 |
| Settlement baseline parsing and row controls       | `AdminPaymentSettlementService.java:61-127,311-478`; `AdminPaymentSettlementServiceTest.java:82-319`                                                                                             | 1,000-row cap, required headers, provider enum, order length, nonnegative numeric values, ISO date shape, sequential duplicate skip, BOM, quoted commas/quotes, and reconciliation outcomes have inspected Mockito assertions. Findings B05-B07 bound the missing strictness and concurrency proof. |
| Settlement inert formula/macro boundary            | `AdminPaymentSettlementService.java:369-430,515-536`; `PaymentOperationsPage.tsx:1048-1065`; `payment-refund-receipt-settlement-policy.md:528-531`                                               | CSV is parsed as inert text, stored payload is allowlisted, React renders text, and no ATStudio Settlement export route exists. No formula or macro was evaluated.                                                                                                                                  |
| Settlement UI duplicate fence and canonical reload | `PaymentOperationsPage.test.tsx:409-460`; `adminSubscriberGaps.coverage.test.tsx:971-1138`; `adminContracts.test.ts:189-243`                                                                     | One confirmed import request, pending disable, Settlement-only reload, reconcile failure without reload, trimmed UI IGNORE note, and exact API contracts are asserted. Partial-success behavior remains B03.                                                                                        |

## Blocked and Unproven Evidence

- Live browser downloads, uploads, deletes, and byte/header capture were forbidden, so no E2/E3/E4/E5 live evidence was produced.
- No private/user file, live database, live storage, provider, mail, session, or secret state was accessed.
- No live destructive cleanup or live storage recovery replay was run.
- No end-to-end H2 plus real-temp-directory crash/restart test was found in the inspected storage test classes.
- Corrected use-case pointers are present. `user-question.md` resolves A02; `user-question.md` and `user-notice.md` leave exact attachment type/count/size rules unspecified; `user-license.md` leaves redownload entitlement, quota, filename, and byte-delivery behavior unspecified.
- The Album-detail per-Track download requirement remains a review item; no implementation was inferred or requested.
- QA-integ did not execute Part B tests during source audit. Main later ran the exact targeted frontend/backend commands below; no Whitelist or Settlement live browser action, private file, production database/storage, Provider, or production audit evidence was produced.
- Part B frontend execution remains Vitest/jsdom mock evidence. Backend service evidence remains primarily Mockito; controller evidence is MockMvc with mocked services; `WhitelistConcurrencyContractTest` is a reflection/source contract rather than a concurrent database test.
- No Part B H2 integration test was found. Main ran isolated `LocalStorageServiceTest` temporary-directory coverage, with one symbolic-link case skipped because symbolic links were unavailable. The handoff-named `PaymentMysqlConcurrencyIntegrationTest` and `PaymentMysqlSchemaValidationTest` are environment-gated MySQL tests and contain no Settlement references; they were not run.
- `AdminWhitelistChannelControllerTest.java:26-68` does not assert ADMIN denial, content type, disposition/filename, or exact bytes. `AdminPaymentControllerTest.java:59-72,196-210` does not establish route-specific import/reconcile/ignore authorization or multipart/result behavior.
- The exact handoff pointer `docs/payment/operator-guide.md` was absent. No substitute document was inspected.

## Exact Test Commands and Results

QA-integ did not issue these commands. Main supplied the following post-audit execution evidence.

| Lane               | Workdir         | Exact command                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                            | Result                                                                                                                                                                     |
| ------------------ | --------------- | ---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- | -------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| Frontend targeted  | `frontend`      | `npx vitest run src/api/domainApis.test.ts src/api/adminWhitelistChannels.test.ts src/api/adminContracts.test.ts src/pages/subscriber/CompanyCertStatusPage.test.tsx src/pages/subscriber/CompanyCertApplyPage.test.tsx src/pages/subscriber/DownloadHistoryPage.test.tsx src/pages/public/NoticeListPage.test.tsx src/pages/admin/CompanyCertManagePage.test.tsx src/pages/subscriber/WhitelistChannelPage.test.tsx src/pages/admin/PaymentOperationsPage.test.tsx src/pages/admin/WhitelistChannelManagePage.test.ts src/pages/admin/WhitelistChannelManagePage.render.test.tsx src/test/coverage/publicAuthShell.coverage.test.tsx src/test/coverage/adminSubscriberPages.coverage.test.tsx src/test/coverage/adminSubscriberGaps.coverage.test.tsx`                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                  | `PASS` - exit 0; 15 files, 159 tests passed; Vitest duration 9.58s; wall 11.4s; no skipped tests or failures reported.                                                     |
| Backend targeted   | repository root | `.\gradlew.bat test --rerun-tasks --tests "com.atstudio.atstudio.controller.NoticeControllerTest" --tests "com.atstudio.atstudio.controller.LicenseControllerTest" --tests "com.atstudio.atstudio.controller.QuestionControllerTest" --tests "com.atstudio.atstudio.controller.CompanyCertificationControllerTest" --tests "com.atstudio.atstudio.controller.AdminWhitelistChannelControllerTest" --tests "com.atstudio.atstudio.controller.AdminPaymentControllerTest" --tests "com.atstudio.atstudio.controller.TrackControllerTest" --tests "com.atstudio.atstudio.service.NoticeServiceTest" --tests "com.atstudio.atstudio.service.NoticeServiceBranchCoverageTest" --tests "com.atstudio.atstudio.service.LicenseServiceTest" --tests "com.atstudio.atstudio.service.DownloadServiceTest" --tests "com.atstudio.atstudio.service.DownloadConcurrencyContractTest" --tests "com.atstudio.atstudio.service.QuestionServiceTest" --tests "com.atstudio.atstudio.service.CompanyCertificationServiceTest" --tests "com.atstudio.atstudio.service.CompanyCertificationSecurityVerificationTest" --tests "com.atstudio.atstudio.service.AdminWhitelistChannelServiceTest" --tests "com.atstudio.atstudio.service.WhitelistConcurrencyContractTest" --tests "com.atstudio.atstudio.service.AdminPaymentSettlementServiceTest" --tests "com.atstudio.atstudio.service.storage.LocalStorageServiceTest" --tests "com.atstudio.atstudio.service.storage.StorageCleanupServiceTest" --tests "com.atstudio.atstudio.service.storage.StorageMutationJournalServiceTest" --tests "com.atstudio.atstudio.service.storage.StorageMutationCoordinatorTest" --tests "com.atstudio.atstudio.service.storage.StorageMutationRecoveryServiceTest" --tests "com.atstudio.atstudio.service.storage.StorageReferenceCheckerBranchCoverageTest" --tests "com.atstudio.atstudio.service.storage.StorageMutationRecoveryVerificationTest" --tests "com.atstudio.atstudio.entity.StorageMutationContractTest"` | `PASS` - exit 0; `BUILD SUCCESSFUL` in 54s; wall 55.4s; 26 explicit class filters; 5 tasks executed. XML aggregate: 38 suites, 278 tests, 0 failures, 0 errors, 1 skipped. |
| Frontend typecheck | `frontend`      | `npm run typecheck`                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                      | `PASS` - exit 0; 6.3s; no diagnostics.                                                                                                                                     |
| Targeted ESLint    | `frontend`      | Exact command text was not supplied by main; scope was the named APIs/pages with `--max-warnings 0`.                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                     | `PASS` - exit 0; 3.1s; 0 warnings.                                                                                                                                         |

Backend skipped case: `LocalStorageServiceTest.rejectsDirectoriesAndSymbolicLinks`; reason: symbolic links were unavailable in this environment. Backend warnings were unchecked/unsafe operations, CDS boot-loader sharing, an incubating problems report, and a configuration-cache suggestion; none failed the command.

**Existing assertions covered by the executed commands**

| File/class                                                         | Assertions main should run                                                                                                                                           |
| ------------------------------------------------------------------ | -------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| `frontend/src/api/adminWhitelistChannels.test.ts`                  | Exact applied scope body; batch header and filename; replay fallback and mismatched batch rejection.                                                                 |
| `frontend/src/test/coverage/adminSubscriberGaps.coverage.test.tsx` | `PENDING` export/replay/error states, applied keyword search, Settlement filters, and UI IGNORE-note guard.                                                          |
| `frontend/src/pages/admin/PaymentOperationsPage.test.tsx`          | One confirmed import despite duplicate clicks, pending disable, Settlement-only reload, and reconcile failure without reload.                                        |
| `frontend/src/api/adminContracts.test.ts`                          | Settlement multipart/note, filter, reconcile, and IGNORE request contracts.                                                                                          |
| `AdminWhitelistChannelServiceTest`                                 | `userEmail`/headers/BOM/escaping/formula handling, status effects, lock order, maximum rejection, byte-stable replay, and replay bound.                              |
| `AdminWhitelistChannelControllerTest`                              | Export and stored-batch 200 plus batch identity; missing byte/header/auth assertions remain below.                                                                   |
| `WhitelistConcurrencyContractTest`                                 | Pessimistic lock declarations, version fence, and schema integrity columns; no real race.                                                                            |
| `AdminPaymentSettlementServiceTest`                                | Match/mismatch, sequential duplicate, baseline file/row validation, quoted evidence, provider-key fallback, reconcile, audit invocation, and nullable-note behavior. |
| `AdminPaymentControllerTest`                                       | Generic admin-read authorization and Settlement IGNORE forwarding only.                                                                                              |

**Required targeted assertions not currently present**

- `WhitelistChannelManagePage` coverage: keyword-only/ALL confirmation must disclose `PENDING -> EXPORTED`; response-loss retry must recover the original batch without another mutation.
- `AdminWhitelistChannelControllerTest`: 401/403, exact `text/csv;charset=UTF-8`, disposition filename, batch header, and exact non-empty/BOM bytes for export and replay.
- New isolated Whitelist integration test: H2 edit/delete after export, immutable byte replay, and two concurrent exports with bounded deterministic outcome.
- `PaymentOperationsPage.test.tsx`: mixed imported/failed response must not show full success or lose retry context; exact file-input reset and complete error access.
- `AdminPaymentControllerTest`: Settlement import/reconcile/ignore 401/403, multipart forwarding, blank-note rejection, and response count serialization.
- `AdminPaymentSettlementServiceTest`: malformed UTF-8, duplicate headers, unmatched/quoted-newline CSV, row width, amount precision/scale/grouping/exponent, currency/provider-ID limits, mixed partial rows, repeated IGNORE, reconcile cap, and count-sum invariant.
- New isolated Settlement database integration test: concurrent duplicate file/row imports, unique-race classification, transaction/audit rollback, durable batch identity, and retry outcome. No current `PaymentMysql*` test supplies this proof.

These passing suites execute the cited mock/MockMvc/contract/`@TempDir` assertions but do not close the 16 findings or prove whole-row acceptance. No H2, MySQL, live browser, private-file, production database/storage, Provider, or production audit result is claimed.

## Part B Scope and Decisions

- Part B source and existing-assertion audit is `COMPLETE`; main's targeted suites are `PASS`; live/browser/private-file/production database/storage/Provider evidence remains `BLOCKED`.
- ATStudio has no Settlement CSV export endpoint or UI in the inspected API map/controller path. Runbook step 1 refers to exporting evidence from the Provider before manual upload (`payment-operations-runbook.md:269-270`). The future accounting-system export recipient remains an unresolved product choice (`payment-refund-receipt-settlement-policy.md:614-615`); no current export defect is invented.
- ATStudio has no pre-import preview endpoint. The documented current contract is import followed by a summary (`payment-operations-runbook.md:270-272`), so absence of preview is not counted as a defect. Adding one requires a new requirement.
- Exact Settlement filename/MIME/byte-size/CSV-dialect rules and canonical amount/currency/provider-ID bounds remain policy/spec decisions. B05/B06 classify the current silent-integrity risks without choosing those values.
- Whitelist confirmation/recovery, Settlement partial-success feedback, and mandatory/idempotent IGNORE behavior are implementation/document defects and do not require a policy decision to classify.
