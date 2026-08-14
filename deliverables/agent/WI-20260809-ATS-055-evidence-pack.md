---
version: 1.0
last_updated: 2026-08-14
project: ATS
owner: docops
category: evidence-pack
status: complete
dependencies:
  - path: WI-20260809-ATS-055-finalization-handoff.md
    reason: Approved finalization scope, result requirements, and validation boundary
  - path: WI-20260809-ATS-055-handoff.md
    reason: Canonical WI scope, acceptance criteria, and held-policy boundary
  - path: WI-20260809-ATS-055-pg-result.md
    reason: Immutable initial PG FAIL history
  - path: WI-20260809-ATS-055-pg-r2-result.md
    reason: PG R2 PASS and finding closure evidence
  - path: WI-20260809-ATS-055-qa-integ-result.md
    reason: Immutable initial QA-INTEG FAIL history
  - path: WI-20260809-ATS-055-qa-integ-r2-result.md
    reason: QA-INTEG R2 PASS and finding closure evidence
---

# Evidence Pack: WI-20260809-ATS-055

## Summary

- Finalized the verified binary-download, duplicate-request fencing, and private-document streaming correction record. WI-055 is complete with no open P0-P3 security, privacy, or integration finding.

## Scope / DoD Check

- [x] Created this Evidence Pack and the user-facing current-state summary only.
- [x] Recorded one `BinaryDownload` contract for a validated non-empty Blob, safe filename, and normalized content type.
- [x] Recorded RFC 5987/basic filename parsing, Unicode category `C`, traversal, separator, blank, and malformed-name fallback behavior.
- [x] Recorded zero-byte and invalid-body rejection before object URL or browser activation.
- [x] Recorded canonical Blob-aware Track error normalization and synchronous same-identity fences.
- [x] Recorded the shared Download History `{readKey, trackId}` owner-token registry across single and bulk actions.
- [x] Recorded Question and Company Certification `StreamingResponseBody` delivery, retained authorization and hardened headers, and unchanged access-grant audit timing.
- [x] Preserved the initial independent PG and QA-INTEG `FAIL` records as immutable history and their R2 `PASS` closure results.
- [x] Recorded the final MA quality gates and the protected-output, secret, synthetic-file, and external-effect boundaries.
- [x] Kept download completion semantics, bulk ceiling, and route-lifetime ownership explicitly held outside WI-055.

## Reference Documents

### Injected Context

| Tier      | Document                                                          | Reason                                                                        |
| --------- | ----------------------------------------------------------------- | ----------------------------------------------------------------------------- |
| 0         | `docs/standards/core-principles.md`                               | Documentation language, traceability, and safe execution principles.          |
| 0         | `docs/standards/documentation-standards.md`                       | Deliverable metadata, structure, links, and validation conventions.           |
| 0         | `docs/standards/glossary.md`                                      | Canonical Work Item and review terminology.                                   |
| WI        | `deliverables/agent/WI-20260809-ATS-055-handoff.md`               | Canonical scope, DoD, acceptance criteria, and held policies.                 |
| WI        | `deliverables/agent/WI-20260809-ATS-055-backend-handoff.md`       | Private Resource streaming implementation boundary.                           |
| WI        | `deliverables/agent/WI-20260809-ATS-055-frontend-handoff.md`      | Binary contract, Track error, and pending-fence implementation boundary.      |
| Review    | `deliverables/agent/WI-20260809-ATS-055-pg-result.md`             | Initial PG `FAIL`: `PG-055-001` and `PG-055-002`.                             |
| Review    | `deliverables/agent/WI-20260809-ATS-055-pg-r2-result.md`          | PG R2 `PASS`: both PG P2 findings closed.                                     |
| Review    | `deliverables/agent/WI-20260809-ATS-055-qa-integ-result.md`       | Initial QA-INTEG `FAIL`: single/bulk overlap P2 and AxiosHeaders test-gap P3. |
| Review    | `deliverables/agent/WI-20260809-ATS-055-qa-integ-r2-result.md`    | QA-INTEG R2 `PASS`: both integration findings closed.                         |
| Portfolio | `deliverables/agent/WI-20260809-ATS-031-consolidated-findings.md` | Canonical roots `CR-031-107`, `CR-031-108`, `CR-031-110`, and `CR-031-127`.   |

### Current Diff Inputs

- Current tracked WI-055 diff: 40 files, 1,494 insertions, and 312 deletions.
- Current-behavior documentation reviewed:
  - `docs/design/api-spec.md`
  - `docs/design/usecase/company-certification.md`
  - `docs/design/usecase/download-queue.md`
  - `docs/design/usecase/user-license.md`
  - `docs/design/usecase/user-question.md`
- The reviewed implementation and tests are limited to the tracked backend and frontend paths in that diff. This finalization did not inspect untracked content, protected output, ignored secrets, or private files.

## Evidence Pointers

### Contract and Client Behavior

- `frontend/src/api/downloads.ts:14-174` defines `BinaryDownload`, validates the response body, parses server headers, sanitizes response/fallback names, and normalizes content type.
- `frontend/src/api/downloads.ts:62-99` rejects a complete filename candidate containing a Unicode category `C` character after RFC 5987 decoding; it selects the deterministic fallback instead of retaining an attacker-shaped remainder.
- `frontend/src/api/downloads.ts:140-148` rejects non-Blob and zero-byte bodies before a browser action; `frontend/src/api/downloads.ts:230-239` triggers only a validated `BinaryDownload` and revokes the object URL after activation.
- `frontend/src/api/admin.ts`, `frontend/src/api/notices.ts`, and `frontend/src/api/questions.ts` use the same normalizer while preserving caller abort signals.
- `frontend/src/layouts/PlayerBar.tsx:189-217`, `frontend/src/pages/public/TrackDetailPage.tsx:102-136`, and the Track list, License list, Like list, and Playlist detail paths synchronously fence their Track identity before the first await.
- `frontend/src/pages/subscriber/DownloadHistoryPage.tsx:120-142` shares one owner-token registry keyed by `readKey` and Track ID between single and bulk actions. Exact-owner finalizers retain distinct Track progress and prevent stale cleanup from releasing newer work.
- `frontend/src/pages/subscriber/DownloadHistoryPage.test.tsx` covers same-identity rapid activation, single-to-selected-bulk, single-to-all-bulk, bulk-to-single, cancellation, and retry behavior.
- `frontend/src/api/downloads.test.ts` is the focused BinaryDownload test owner cited by the independent review; QA-INTEG R2 confirms direct installed Axios `AxiosHeaders.get()` coverage together with the existing plain-header matrix.

### Server Delivery and Current Documentation

- `src/main/java/com/atstudio/atstudio/controller/QuestionController.java:88-116` and `src/main/java/com/atstudio/atstudio/controller/CompanyCertificationController.java:98-123` return `StreamingResponseBody`, transfer the service Resource, and close the input stream without a controller-sized intermediate byte array.
- `src/test/java/com/atstudio/atstudio/controller/QuestionControllerTest.java` and `src/test/java/com/atstudio/atstudio/controller/CompanyCertificationControllerTest.java` cover synthetic streaming bodies, async dispatch, hardened headers, and no-range behavior.
- The five current-behavior documents listed above record the normalized client contract, private streaming headers, Download History owner registry, and the held policy boundary.
- `QuestionService.java:154-166` and `:208-214` retain Question authorization before private Resource resolution. `CompanyCertificationController.java:98-108` remains ADMIN-only, and `CompanyCertificationService.java:217-236` retains `DOCUMENT_ACCESS_GRANTED` at authorization/resource resolution.

### Cross-Layer State

| Layer                   | Verified current state                                                                                                                                                                                                                                                                 |
| ----------------------- | -------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| Visible UI              | Same visible Track identity is pending/disabled while its synchronous claim is held. Distinct Track identities may continue.                                                                                                                                                           |
| Frontend invocation     | A same identity invokes at most one request until the exact owner settles. Track failures use the existing asynchronous Blob-aware `getApiErrorCode()` path.                                                                                                                           |
| HTTP body and headers   | Valid binary responses become `BinaryDownload`; invalid and zero-byte bodies fail before object URL creation. Private responses retain attachment disposition, `application/octet-stream`, `no-store, private`, `Pragma: no-cache`, `nosniff`, sandbox CSP, and `Accept-Ranges: none`. |
| Authorization           | Existing Question ownership/access checks and Company Certification ADMIN authorization remain authoritative before streaming.                                                                                                                                                         |
| Durable state and audit | Existing Track License/count/history mutation timing is unchanged. `DOCUMENT_ACCESS_GRANTED` remains access-grant evidence, not proof of completed client byte delivery.                                                                                                               |

## Immutable Review History

- **PG initial result: `FAIL`.** `PG-055-001` found Unicode format-control filename spoofing; `PG-055-002` found retained PlayerBar, Track detail, and Download History entry points without synchronous ownership.
- **PG R2 result: `PASS`.** Both P2 findings are closed. No open P0-P3 security or privacy finding remains.
- **QA-INTEG initial result: `FAIL`.** The P2 finding identified separate Download History single and bulk ownership; the P3 finding identified no direct installed Axios `AxiosHeaders.get()` assertion.
- **QA-INTEG R2 result: `PASS`.** The shared owner-token registry and AxiosHeaders test close both findings. No open P0-P3 integration finding remains.

## Commands and Final MA Gates

| Gate                       | Result                                                                                                                                                                                                             |
| -------------------------- | ------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------ |
| Frontend coverage          | PASS - 105 files, 1,366/1,366 tests; statements 89.94%, branches 82.32%, functions 90.54%, lines 92.51%. The run emitted the existing non-failing jsdom `Not implemented: navigation to another Document` message. |
| Frontend quality           | PASS - typecheck, ESLint, Prettier, and production build; 292 modules transformed.                                                                                                                                 |
| Backend quality            | PASS - 186 suites, 1,608 tests, failures/errors 0, skipped 19; JaCoCo line 87.454%, method 85.102%, branch 72.358%, instruction 87.142%; coverage verification and build passed.                                   |
| Documentation validation   | PASS - 586 traceability IDs.                                                                                                                                                                                       |
| Diff whitespace validation | PASS - `git diff --check` reported only existing CRLF-to-LF working-copy warnings.                                                                                                                                 |

## Boundaries, Risks, and Rollback

- **Protected output:** `output/client-demo-screenshots-20260716-140514.zip` and `output/ui-ux-audit/` were not opened, hashed, changed, staged, or deleted.
- **Secrets and private data:** Ignored secrets, local environment values, and private/user files were not inspected. Tests use synthetic in-memory or temporary safe resources only.
- **External effects:** No browser download, network request, Provider, payment, refund, mail, export, database-data action, or other external effect was executed.
- **Held decisions:** WI-055 does not decide whether durable grant or completed client bytes define successful download, a bulk-download ceiling, or route-lifetime operation ownership/cancellation. The shared registry prevents duplicate invocation only.
- **Residual risk:** Browser download activation and transfer completion remain verified through unit/integration seams, not by a live private-file download.
- **Rollback:** Revert the WI-055 tracked implementation, tests, current-behavior documentation, and these two finalization records as one source-control change. No Provider, database, storage, audit, or external-effect rollback is implied.

## Follow-up

- WI-055 releases the next approved portfolio work. The separately approved decision work for completion semantics, bulk ceiling, and route-lifetime ownership remains in its own scope.

## Related Documents

- [Finalization Handoff](WI-20260809-ATS-055-finalization-handoff.md): Output contract and final MA gates.
- [Canonical WI Handoff](WI-20260809-ATS-055-handoff.md): Original scope and acceptance criteria.
- [PG R2 Result](WI-20260809-ATS-055-pg-r2-result.md): Closed privacy and security findings.
- [QA-INTEG R2 Result](WI-20260809-ATS-055-qa-integ-r2-result.md): Closed integration findings.
