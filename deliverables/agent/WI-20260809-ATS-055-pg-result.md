---
version: 1.0
last_updated: 2026-08-14
project: ATS
owner: pg
category: wi-security-review-result
status: completed
result: FAIL
wi: WI-20260809-ATS-055
---

# Privacy and Security Review Result: WI-20260809-ATS-055

## Result

**FAIL**. Two open P2 findings remain. No P0 or P1 finding was identified.

## Findings

### P2 - Filename sanitizer permits Unicode format-control extension spoofing

- **Evidence:** `frontend/src/api/downloads.ts:25` and
  `frontend/src/api/downloads.ts:69-73` remove only Unicode `Cc` characters.
  The decoded RFC 5987 name is then accepted by
  `frontend/src/api/downloads.ts:89-97` and assigned to the browser download
  action at `frontend/src/api/downloads.ts:141-146` and `:231-236`.
  `frontend/src/api/downloads.test.ts:43-58` covers traversal plus NUL, but
  not a decoded Unicode format-control value such as `U+202E`.
- **Effect:** A response filename such as
  `filename*=UTF-8''invoice%E2%80%AEgpj.exe` retains the right-to-left override
  in `a.download`. On affected file managers this can visually disguise the
  executable extension. This violates the required control-character filename
  hardening even though CRLF header injection remains contained.
- **Bounded remediation:** Reject or remove Unicode format-control characters
  (at least `Cf`, preferably all relevant Unicode `C` categories) after RFC
  5987 decoding and before the final length check. Add decoded bidi-control and
  zero-width-format filename cases that assert fallback selection.

### P2 - Three retained Track entry points can still start duplicate requests

- **Evidence:** `frontend/src/layouts/PlayerBar.tsx:188-211` guards only with
  React state, so two activations in one render see the same stale
  `downloading=false` value before `setDownloading(true)` commits.
  `frontend/src/pages/public/TrackDetailPage.tsx:101-130` has no pending guard
  before calling `downloadTrack`. `frontend/src/pages/subscriber/DownloadHistoryPage.tsx:250-272`
  aborts and replaces the prior controller, but it starts the replacement
  `downloadTrack` request; an already-dispatched first request can still reach
  the server. The added synchronous-fence tests cover the newly changed list,
  License, Like, and Playlist callers (for example,
  `frontend/src/pages/public/TrackListPage.test.tsx:243-264`), not these three
  entry points.
- **Effect:** Rapid activation of the same visible Track action can invoke the
  protected download endpoint more than once. Backend authorization remains
  authoritative, but the client-side at-most-one-request acceptance condition
  is not met and duplicate durable download-side effects remain possible until
  server controls resolve them.
- **Bounded remediation:** Use a synchronous ref-backed per-Track ownership
  fence for PlayerBar, Track detail, and single History re-download. Keep the
  existing UI disabled state and generation/abort ownership, and add
  same-render double-activation tests that assert one `downloadTrack` call and
  release after settlement.

## Verified Controls

- **Visible UI:** The canonical result carries a sanitized filename and
  non-empty Blob before browser activation. The newly added pending state is
  visibly disabled for the Track list, License list, Like list, and Playlist
  detail; the three retained entry points above are the exception.
- **Frontend request invocation:** Question, Notice, and Company Certification
  wrappers now call the shared binary helper with a deterministic fallback and
  preserve abort signals. Track callers use the existing Blob-aware
  `getApiErrorCode()` path; the helper returns only `errorCode`, not Blob error
  bodies.
- **HTTP/body headers:** `QuestionController.java:88-116` and
  `CompanyCertificationController.java:98-123` retain
  `application/octet-stream`, encoded attachment disposition, `no-store,
  private`, pragma, nosniff, sandbox CSP, and `Accept-Ranges: none` while
  streaming through a closed input stream. No controller-sized byte-array copy
  remains in the reviewed diff.
- **Authorization:** Question authorization remains in
  `QuestionService.java:154-166` and `:208-214` before the Resource reaches the
  streaming lambda. Company Certification retains controller-level
  `@PreAuthorize("hasRole('ADMIN')")` at
  `CompanyCertificationController.java:98-104`; its unchanged service lookup
  and private-resource resolution occur before streaming begins.
- **Durable state and audit:** The reviewed controller diff does not alter
  services or audit calls. `CompanyCertificationService.java:217-236` still
  records `DOCUMENT_ACCESS_GRANTED` after authorization/resource resolution;
  this is an access-grant audit, not byte-completion proof.

## Test and Policy Boundary

- Inspected controller tests include asynchronous body dispatch, no-Range,
  hardened headers, CRLF disposition encoding, private static-path denial, and
  USER-to-ADMIN authorization negatives. The supplied handoff reports 53/53
  focused controller tests and 1,351 full frontend tests passing; this review
  did not execute tests or any external operation.
- The zero-byte/non-Blob checks run before object-URL creation in
  `frontend/src/api/downloads.ts:127-147`; the focused test is
  `frontend/src/api/downloads.test.ts:69-76`.
- Held policies were not decided: durable byte-completion success, bulk
  download ceiling, and route-lifetime/cancellation ownership remain outside
  WI-055. No private/user file, protected output, secret, network operation,
  provider, database, or browser download was accessed.
