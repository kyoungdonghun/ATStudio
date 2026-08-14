---
version: 1.0
last_updated: 2026-08-14
project: ATS
owner: qa-integ
category: wi-integration-review-result
status: completed
result: FAIL
wi: WI-20260809-ATS-055
---

# Integration QA Result: WI-20260809-ATS-055

## Result

**FAIL**. One P2 cross-entry duplicate-request path and one P3 response-shape
test gap remain. No P0 or P1 finding was identified.

## Findings

### P2 - Download History permits a duplicate Track request across single and bulk actions

- **Pointers:** The single action owns `singleOwnershipRef` and
  `singleControllerRef` at `frontend/src/pages/subscriber/DownloadHistoryPage.tsx:253-305`.
  Selected/all actions instead own `bulkControllerRef` and independently invoke
  `downloadTrack` for every supplied ID at `:309-366`. The selected action does
  not exclude `currentDownloading` at `:369-380`, and its button remains enabled
  while a single request is pending at `:525-537`; the single button only checks
  `currentDownloading` and `bulkBusy` at `:627-636`. The focused test at
  `frontend/src/pages/subscriber/DownloadHistoryPage.test.tsx:272-300` covers
  repeated single activation only, while `:213-242` covers only bulk-ID
  de-duplication.
- **Visible UI:** After starting a single re-download for a selected row, the
  same Track remains eligible for the selected bulk action.
- **Frontend invocation:** The bulk loop creates a second controller and sends a
  second `downloadTrack(trackId, fallback, signal)` before the single promise
  settles. The two refs do not share a `{readKey, trackId}` claim.
- **HTTP response:** This can issue two concurrent
  `GET /api/tracks/{trackId}/download` requests for one Track identity.
- **Authorization:** `TrackController.downloadTrack` delegates each request to
  `DownloadService.download` (`TrackController.java:94-103`;
  `DownloadService.java:44-54`). The service user-row lock and existing-License
  branch remain authoritative, but they do not suppress duplicated byte work.
- **Durable state/audit:** The existing-License path avoids another first-download
  history, License, quota, or count mutation; it does not make the two browser
  download attempts one action. This therefore violates the WI at-most-one
  frontend request condition without changing the held completion semantics.
- **Bounded remediation:** Use one Download History registry keyed by current
  `readKey` and `trackId` across the single and bulk paths. A bulk run must skip
  or wait for an already claimed Track, and a single action must not start a Track
  claimed by bulk. Add deferred tests for single-then-selected and
  single-then-confirmed-all overlap that assert one request for the shared ID,
  preserve distinct-ID progress, and retain existing owner-replacement cleanup.

### P3 - The AxiosHeaders response-header branch has no direct assertion

- **Pointers:** `frontend/src/api/downloads.ts:40-53` supports AxiosHeaders via
  `headers.get(name)` and supports plain records through `Object.entries`.
  `frontend/src/api/downloads.test.ts:13-93` supplies only plain header records;
  API-wrapper mocks likewise use records at
  `frontend/src/api/notices.test.ts:48-62`,
  `frontend/src/api/domainApis.test.ts:92-108`, and
  `frontend/src/api/adminContracts.test.ts:99-105`.
- **Effect:** The implementation's AxiosHeaders-specific path is reasonable by
  source inspection, but no current test exercises the actual Axios response
  header class. A regression in `.get()` handling, case normalization, or its
  returned value would remain invisible while all present plain-record mocks pass.
- **Bounded remediation:** Add one `normalizeBinaryDownload` test using the
  installed Axios `AxiosHeaders` instance with RFC 5987 disposition and content
  type, alongside the existing plain-record matrix. No browser or network action
  is needed.

## Cross-Layer Conclusions

- **Visible UI:** Track list, License, Like, Playlist, PlayerBar, Track detail,
  Notice, and Question use synchronous/ref-backed or operation-owned pending
  fences for their reviewed local actions. Notice and Question retain their
  abort/generation projection guards. The Download History single/bulk overlap
  above remains open.
- **Frontend invocation:** All located `downloadTrack`, Notice attachment,
  Question attachment, Company Certification, and `triggerBlobDownload` callers
  consume `BinaryDownload`. Wrappers pass deterministic fallback names; Notice
  and Question retain AbortSignal propagation. `getBinaryDownload` awaits Axios
  before calling `normalizeBinaryDownload`, so rejected Blob JSON API responses
  remain available to `getApiErrorCode()` rather than being normalized as a
  successful binary body. Existing `frontend/src/api/client.test.ts:597-613`
  directly covers Blob JSON error-code extraction.
- **HTTP response:** The binary helper validates a non-empty Blob before object
  URL creation, parses UTF-8 `filename*` and basic disposition forms, rejects
  malformed/control/traversal names, retains a valid response media type, and
  applies a deterministic fallback. The unresolved test gap is limited to direct
  AxiosHeaders coverage, not a demonstrated parser failure.
- **Authorization:** Question access is checked before private Resource lookup
  (`QuestionService.java:154-163`, `:208-214`). Company Certification remains
  ADMIN-gated before service invocation and resolves its private Resource before
  the stream body (`CompanyCertificationController.java:98-123`,
  `CompanyCertificationService.java:217-237`).
- **Durable state/audit:** Track first-download state remains owned by
  `DownloadService`; Company Certification still writes
  `DOCUMENT_ACCESS_GRANTED` after authorized private-resource resolution. Neither
  is evidence of completed client byte delivery.
- **Streaming:** Question and Company Certification now use
  `StreamingResponseBody`; their MockMvc async tests assert complete synthetic
  bodies and retained disposition, cache, pragma, nosniff, CSP, and no-range
  headers. Controller-direct tests execute the returned stream with synthetic
  resources. No controller byte-array copy is present in the reviewed diff.

## Verification Boundary

- Reviewed the complete current WI-055 uncommitted tracked diff, the new
  `frontend/src/api/downloads.test.ts`, all located binary callers, the relevant
  controller/service paths, canonical handoff, WI-029/WI-030 findings, and PG
  initial/R2 results. Protected outputs, ignored secrets, and private/user files
  were not opened.
- `git diff --check` completed with exit code 0; it emitted only existing
  CRLF-to-LF working-copy warnings for the four changed Java files.
- This independent review did not run frontend/backend tests, a browser download,
  network request, provider/payment operation, database operation, or any other
  external effect. Reported prior test totals are handoff evidence, not rerun
  evidence for this review.
- Held policy questions were not decided: durable byte-completion success, bulk
  download ceiling, and route-lifetime/cancellation ownership remain out of
  scope.

## Rollback

Revert the WI-055 implementation and test changes as one source-control change.
No provider, database, storage, or audit rollback is implied by this review.
