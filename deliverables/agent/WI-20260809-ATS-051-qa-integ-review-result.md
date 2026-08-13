# QA Integration Review Result: WI-20260809-ATS-051

## Verdict

**FAIL** - 3 open P2 findings remain. No P0 or P1 finding was identified.

## P2 Findings

### ATS-051-QI-01 - Post-review detail refresh can overwrite a newer or closed detail context

- **Type:** Observed implementation defect
- **File / line:** `frontend/src/pages/admin/CompanyCertManagePage.tsx:184-187,189-216`; compare the owned detail loader at `:141-157` and close retirement at `:166-176`.
- **Scenario:** Admin submits a review for certification A. After `processCompanyCert` succeeds, `refreshDetail(A)` starts. The admin closes the detail or opens certification B before that read completes. The late A response calls `setDetail(data)` without checking `detailRequestId`, `selectedDetailId`, or whether the modal still owns A.
- **Expected:** Closing A or selecting B retires every A detail completion, including the post-review refresh.
- **Actual:** Only `loadDetail` is generation-fenced; `refreshDetail` bypasses the fence and can commit stale A data.
- **Impact:** The ADMIN modal can display or expose review controls for the wrong application after a close/reopen race, contradicting the selected-detail ownership documented in `docs/design/api-spec.md:766-767` and `docs/design/usecase/company-certification.md:161-164`.
- **Bounded remediation:** Route the post-review read through the same selected-ID/generation owner as `loadDetail`, and commit only while the modal is open and still owns the initiating certification ID. Add close/reopen tests covering late post-review success and failure.

### ATS-051-QI-02 - Frontend URL parsing still accepts values the backend rejects

- **Type:** Observed cross-layer contract defect
- **File / line:** `frontend/src/utils/safeYoutubeUrl.ts:1-18`; `frontend/src/pages/subscriber/WhitelistChannelPage.tsx:173-180,190-196`; backend authority at `src/main/java/com/atstudio/atstudio/service/WhitelistChannelService.java:205-221`.
- **Scenario:** User enters a browser-normalizable value such as `https:youtube.com/@atm` or a URL containing backslashes. JavaScript `new URL()` normalizes it to an HTTPS YouTube URL, so client validation passes, but `buildRequest()` sends the original trimmed text. Java `URI.create()` yields no allowed host or rejects the backslash form.
- **Expected:** Every URL accepted by the form is accepted by the backend under the same scheme/host/user-info/port contract.
- **Actual:** Browser URL canonicalization is used only as a boolean check, while the non-canonical input is sent to the stricter Java parser and receives a late `400 INVALID_ARGUMENT`.
- **Impact:** CR-031-071 remains partially open: the UI presents some backend-rejected values as valid and invokes create/update unnecessarily.
- **Bounded remediation:** Either reject non-canonical syntax before submission or submit the validated canonical URL returned by a shared frontend helper, then add paired frontend/backend cases for scheme-relative shorthand, missing `//`, and backslashes.

### ATS-051-QI-03 - CR-031-077 was remapped to the wrong admin note surface

- **Type:** Observed scope/traceability defect with user-visible contract impact
- **File / line:** Original root `deliverables/agent/WI-20260809-ATS-026-findings.md:109-119` and canonical mapping `deliverables/agent/WI-20260809-ATS-031-consolidated-findings.md:641`; still-unbounded control at `frontend/src/pages/admin/WhitelistChannelManagePage.tsx:470-482`; backend bound at `src/main/java/com/atstudio/atstudio/dto/whitelist/AdminWhitelistChannelStatusRequest.java:7-9`. The WI instead changes company-certification note handling at `frontend/src/pages/admin/CompanyCertManagePage.tsx:448-467`.
- **Scenario:** ADMIN enters a 501-character Whitelist status note and saves a real status transition.
- **Expected:** The Whitelist UI explains/enforces the existing 500-character `adminNote` boundary and blocks the request at 501 characters.
- **Actual:** The Whitelist textarea still has no `maxLength`, counter, or pre-request length check; the request reaches the backend and is rejected. WI-051's handoff and patch incorrectly describe CR-031-077 as a company-certification review-note finding.
- **Impact:** The accepted Whitelist defect remains open while unrelated certification work is credited against its traceability ID, so completion evidence would be false.
- **Bounded remediation:** Restore CR-031-077 to the ADMIN Whitelist note surface, add 500/501 UI and exact payload/call-count tests there, and correct WI/docs traceability. Keep the certification note changes only as separately justified scope.

## P3 Findings

### ATS-051-QI-04 - Documentation patch contains broad formatting-only churn

- **Type:** Scope-quality finding
- **File / line:** Representative unrelated reformatting at `docs/design/api-spec.md:374-386,587-599,688-696`; pervasive table/list reformatting begins at `docs/design/usecase/company-certification.md:18-28,58-76` and `docs/design/usecase/whitelist.md:18-30,49-76`.
- **Scenario:** Review or merge the scoped current-state documentation update against concurrent work.
- **Expected:** Only paragraphs/rows needed for WI-051 truth synchronization change.
- **Actual:** The three docs contain 484 raw changed lines, while `git diff -w --numstat` leaves 176 semantic changed lines; approximately 308 changed lines are whitespace/formatting-only churn, including unrelated payment tables in `api-spec.md`.
- **Impact:** The patch obscures semantic review, increases conflict risk, and exceeds the scoped documentation update without changing behavior.
- **Bounded remediation:** Revert formatting-only hunks and retain only WI-051 semantic additions in the relevant Whitelist and Company Certification sections.

### ATS-051-QI-05 - The all-status action matrix lacks required API call-count proof

- **Type:** Missing proof, not an observed production defect
- **File / line:** `frontend/src/pages/subscriber/WhitelistChannelPage.test.tsx:123-153,155-202`; acceptance contract at `deliverables/agent/WI-20260809-ATS-051-handoff.md:60-61`.
- **Scenario:** Regress an individual status/action handler while preserving button visibility, such as delete for `DRAFT`, `PENDING`, `REVISION_REQUESTED`, `REJECTED`, or `CANCELLED`, or ordinary update for `DRAFT`, `PENDING`, or `REJECTED`.
- **Expected:** Every status has exact visibility plus positive/negative API invocation-count assertions for edit, request, primary, and delete/removal.
- **Actual:** The table-driven matrix proves visibility and zero calls before interaction, but click/call-count tests cover only primary-eligible statuses, requestable statuses, processed removal, and processed requeue updates. Several visible actions have no invocation proof.
- **Impact:** A handler regression can satisfy the current matrix and leave the DoD overstated.
- **Bounded remediation:** Extend the table-driven cases to click every visible action once, confirm dialogs where applicable, and assert exactly one expected API call and zero ineligible calls per status.
