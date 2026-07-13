# Evidence Pack: WI-20260711-ATS-007

## Summary (one-liner)

- Re-verified all Phase 1 whitelist/company-certification claims and produced two design-implementation-client 3-way matrices; both domains are `FAIL` because material security, lifecycle, migration, and concurrency gaps remain.

## Scope / DoD Check

- [x] Re-verified whitelist and certification states against entity, service, API, frontend, DB, and client wording.
- [x] Re-verified whitelist plan limits, primary channel, delete/removal, admin processing, CSV export, and concurrency semantics.
- [x] Re-verified certification apply/resubmit/review/payment-gate, role boundaries, file validation, storage/download, and history semantics.
- [x] Reconciled the five targeted JPA/schema tables and both manual patches without applying SQL.
- [x] Assessed unbounded lists/exports, persistent snapshots, file growth, and failed-delivery/cleanup recovery.
- [x] Separated exploitable security defects, functional defects, policy ambiguities, conditional deployment risks, and intentionally deferred automation.
- [x] Used static/read-only commands only; no real export, upload, download, DB mutation, status mutation, or data inspection occurred.
- [x] Created only the two files owned by this WI.

## Baseline and Constraints

| Field | Value |
|---|---|
| Branch | `dev/kyoung` |
| HEAD | `27d22446e5d21324dadcfcb322dbe51704dfe914` |
| Worktree | Pre-existing dirty/untracked work retained; no existing file was modified by this WI |
| Inspection mode | Static and read-only outside this WI's two outputs |
| Prohibited actions | Real export, document upload/download, SQL execution, state mutation, external transfer, PII value capture |

No PII values, secret values, uploaded-document paths, or live storage locations are recorded in this pack.

## Reference Documents (Tier 0-2)

| Tier | Document | Reason |
|---|---|---|
| 0 | `docs/standards/core-principles.md` | Constitution, language, traceability, sensitive-data principles |
| 0 | `docs/standards/development-standards.md` | Java/service/transaction/security/test standards |
| 0 | `docs/standards/documentation-standards.md` | Evidence and document structure |
| 0 | `docs/standards/glossary.md` | Canonical whitelist/certification terms |
| 1 | `docs/policies/security-policy.md` | Secrets/PII minimization and default-deny baseline |
| 1 | `docs/policies/access-control-policy.md` | Least privilege and read-only execution |
| 1 | `docs/policies/quality-gates.md` | Traceability, review, and regression evidence |
| 2 | `docs/design/usecase/whitelist.md` | Whitelist state and operator workflow contract |
| 2 | `docs/design/usecase/company-certification.md` | Certification state, document, and review contract |
| 2 | `docs/design/api-spec.md` | API and response contract |
| 2 | `docs/design/db-schema.md` | Current DB design contract |
| 2 | `docs/client/1-quick-checklist.md` | Quick client acceptance wording |
| 2 | `docs/client/2-full-feature-checklist.md` | User acceptance wording |
| 2 | `docs/client/3-admin-checklist.md` | Admin acceptance wording |
| REQ | `deliverables/user/REQ-20260615-ATS-001.md` | Whitelist/payment alignment context |
| REQ | `deliverables/user/REQ-20260618-ATS-001.md` | Company-certification delivery context |
| REQ | `deliverables/user/REQ-20260711-ATS-001.md` | Approved audit scope |
| WI | `deliverables/agent/WI-20260711-ATS-001-evidence-pack.md` through `WI-20260711-ATS-005-evidence-pack.md` | Phase 1 claims under re-verification |

Injection order followed the handoff: Tier 0, Tier 1, domain/REQ context, then the current repository snapshot.

## Verdict Scale

- `PASS`: all three lanes describe and enforce the same behavior, with no material gap found.
- `PARTIAL`: the main flow aligns, but one lane omits, weakens, or misrepresents a material edge or control.
- `FAIL`: a P1 issue or a contract-critical P2 issue makes the acceptance claim unsafe or false.

The three lanes are: (1) design/API/DB contract, (2) backend/schema/frontend implementation, and (3) client acceptance wording.

## Phase 1 Claim Re-verification

| Phase 1 claim | Current verdict | Current evidence / correction |
|---|---|---|
| Client Markdown and PDF were synchronized for this scope | Confirmed at frozen snapshot | Current SHA-256 values for the quick/full/admin/internal-map sources and PDF match `WI-001`; no PDF regeneration or parsing was needed. |
| Whitelist and certification API request/response shapes align across spec/backend/frontend | Confirmed for shapes | `api-spec.md:2845-3272`; controllers; `frontend/src/api/whitelistChannels.ts:15-68`, `companyCerts.ts:6-39`, `admin.ts:64-191`. Behavioral/security findings below do not change this shape result. |
| Whitelist plan/primary invariants are application-only | Confirmed | `WhitelistChannelService.java:119-145`; `schema.sql:230-252`; no target `@Version`, `@Lock`, or unique-primary invariant. |
| Company state transitions and latest lookup are deterministic | Confirmed for sequential calls | `CompanyCertification.java:50-87`; `CompanyCertificationRepository.java:13-16`. Concurrency is not serialized and is separated below. |
| Certification file rollback/after-commit cleanup is a positive comparator | Confirmed with limitation | `CompanyCertificationService.java:266-307`; callbacks exist, but failed post-commit deletion has no durable retry and tests invoke callbacks without a real transaction. |
| Whitelist ownership and certification document-parent binding are enforced | Confirmed | `WhitelistChannelService.java:81-82,100-101,137-138,152-153,189-192`; `CompanyCertificationService.java:190-200`. |
| Frontend treats whitelist subscription lookup failure as no subscription | Confirmed | `WhitelistChannelPage.tsx:74-98`; all subscription-fetch failures become `null`. |
| INDIVIDUAL users receive the BUSINESS-only certification form | Confirmed | `router/index.tsx:162-164`; `CompanyCertApplyPage.tsx:53-87,133-149`; backend rejects at `CompanyCertificationService.java:59-66`. |
| Certification upload is extension/size/count only and trusts client MIME | Confirmed | `ValidationConstants.java:45-49`; `CompanyCertificationService.java:233-276`; controller reuses stored MIME at `CompanyCertificationController.java:98-112`. |
| `documentPath` metadata is exposed while per-file stored paths are hidden | Confirmed | `CompanyCertificationResponse.java:8-40`; `CompanyCertificationDocumentResponse.java:7-20`; frontend type at `types/index.ts:253-267`. |
| Company static resources and API downloads are admin guarded | Corrected to partial | API download and static `GET` are guarded at `SecurityConfig.java:80,111-117`; static `HEAD` is not matched by the GET-only rule and falls to `anyRequest().permitAll()` at `:131-132`. No test covers GET/HEAD static paths. |
| Whitelist export has no claim/lock and is unbounded | Confirmed | `AdminWhitelistChannelService.java:104-157`; `WhitelistChannelRepository.java:47-48`; `schema.sql:257-294`. |
| Latest certification DDL patch matches current child entity/table shape | Confirmed for DDL shape, incomplete for data migration | `20260618_company_certification_documents.sql:5-22`, `schema.sql:162-179`, `CompanyCertificationDocument.java:7-42`; no legacy metadata backfill exists. |
| Existing-DB migration chain is not reproducible from the repository | Confirmed | `application.yml:16-20`; `db-schema.md:11-19`; only two manual patches and no Flyway/Liquibase dependency were found. |

## 3-Way Matrix A: Whitelist Channels

**Overall verdict: `FAIL`.** Core sequential flows align, but P1 CSV execution risk and an incomplete removal lifecycle invalidate client acceptance; P2 concurrency, export-scope, recovery, and growth gaps remain.

| Capability | Design / API / DB | Implementation | Client wording | Verdict |
|---|---|---|---|---|
| State vocabulary | Eight states and five plan-counting states are explicit (`whitelist.md:10-21`; `api-spec.md:2951-2954`; `db-schema.md:744-768`) | Enum/entity/schema use the same values (`WhitelistChannelStatus.java:3-11`; `WhitelistChannel.java:38-41,124-130`; `schema.sql:230-252`) | User/admin checklists cover draft, request, processing, registration, revision, rejection, and removal (`2-full-feature-checklist.md:175-187`; `3-admin-checklist.md:82-87`) | `PASS` |
| Save and list drafts | Draft save does not require a subscription; list is ordered primary-first (`whitelist.md:25-72`) | `registerChannel` and `getMyChannels` match (`WhitelistChannelService.java:43-70`) | Draft save and unlimited-by-plan draft wording match (`2-full-feature-checklist.md:175-180`) | `PARTIAL`: no draft cap/pagination; authenticated storage/response growth is unbounded |
| Update/reprocessing | Updating `REGISTERED`, `EXPORTED`, or `REVISION_REQUESTED` requeues to `PENDING` with self-slot exclusion (`whitelist.md:76-104`) | Service/entity match for those states (`WhitelistChannelService.java:75-95,206-210`; `WhitelistChannel.java:66-76`) | Checklist expects reprocessing notice or transition (`2-full-feature-checklist.md:185`) | `PARTIAL`: `REMOVAL_REQUESTED` remains editable in place without a defined contract |
| Registration and plan limit | Active subscription plus counted-state limit; `PENDING` is idempotent; correction state reuses its slot (`whitelist.md:136-164`) | Backend and frontend status sets align (`WhitelistChannelService.java:29-35,98-132`; `WhitelistChannelPage.tsx:35-48,96-98,352-360`) | Subscription/limit outcomes match (`2-full-feature-checklist.md:182-184`) | `PARTIAL`: unlocked count-then-write can exceed the plan under concurrent requests |
| Primary channel | Exactly one representative where possible (`whitelist.md:168-187`; `api-spec.md:2965-2974`) | First-save count and clear-then-set are not locked; DB has no enforceable uniqueness (`WhitelistChannelService.java:43-59,135-145`; `schema.sql:230-252`) | Quick/full checklists state one primary (`1-quick-checklist.md:72-74`; `2-full-feature-checklist.md:181`) | `FAIL`: sequential behavior passes, concurrency can produce multiple primaries |
| Delete and external removal | Local states delete; processed states become `REMOVAL_REQUESTED`; operator should later update status (`whitelist.md:108-132`) | Delete mapping matches initial transition (`WhitelistChannelService.java:150-162`; `WhitelistChannel.java:100-129`) | Checklist expects processed rows to remain as removal requests (`2-full-feature-checklist.md:186-187`) | `FAIL`: no removal-completed state exists; the counted slot can remain occupied indefinitely |
| Repeated removal request | Not defined | Deleting an existing `REMOVAL_REQUESTED` row rewrites the same state/time (`WhitelistChannelService.java:150-162`; `WhitelistChannel.java:100-103`) | UI labels it as delete and reports deletion because only EXPORTED/REGISTERED are recognized as removal flow (`WhitelistChannelPage.tsx:204-219,365-374`) | `FAIL`: user-visible result is false |
| Admin processing | Target statuses are listed, but no source-to-target matrix is defined (`whitelist.md:191-210`; `api-spec.md:3042-3059`) | Backend accepts any source if target is in the mutable set (`AdminWhitelistChannelService.java:40-47,80-101`); UI offers the same targets for every row (`WhitelistChannelManagePage.tsx:27-33,261-296`) | Admin checklist only says status/note are saved (`3-admin-checklist.md:82-87`) | `PARTIAL`: policy ambiguity permits nonsensical recovery transitions |
| CSV contract and ledger | Status-based export, snapshot rows, email/channel fields, PENDING -> EXPORTED (`whitelist.md:214-235`; `api-spec.md:3063-3091`; `db-schema.md:771-803`) | Service creates BOM CSV/ledger and updates PENDING (`AdminWhitelistChannelService.java:104-189`) | Admin checklist says the export includes user email and channel data (`3-admin-checklist.md:84-85`) | `FAIL`: user-controlled cells are quoted but formula-leading values are not neutralized |
| CSV scope shown in UI | Design explicitly says keyword is ignored and export is status-based (`whitelist.md:226-232`; `api-spec.md:3075`) | UI places search/filter and export together without scope disclosure; `ALL` confirms no state change but sends default PENDING, which is changed to EXPORTED (`WhitelistChannelManagePage.tsx:121-145,167-195`) | Client checklist does not warn that search is ignored (`3-admin-checklist.md:83-85`) | `FAIL`: operator can export a wider PII set than the visible search result and receive a false confirmation |
| Export concurrency and recovery | Ledger is described as auditable; batch retrieval/re-download is not specified | No row claim/lock or batch read API; DB commits before response delivery and export is unbounded (`AdminWhitelistChannelService.java:104-157`; export repositories; controller inventory) | Client wording only checks that a file downloads | `FAIL`: overlapping batches and post-commit delivery failure have no deterministic recovery |
| Role and privacy boundary | Admin endpoints are designated ADMIN; export fields are explicitly documented | `/api/admin/**` plus method guards enforce ADMIN (`AdminWhitelistChannelController.java:36-74`; `SecurityConfig.java:127-130`) | Admin-only screen route is enforced (`router/index.tsx:183-206`) | `PARTIAL`: access control passes; external-recipient necessity and retention for identity/subscription snapshots are undecided |
| Schema/manual patch | Three tables and current columns/indexes are documented (`db-schema.md:744-803`) | JPA and fresh DDL match; manual patch adds workflow columns/tables (`schema.sql:230-297`; `20260615_align_payment_whitelist_schema.sql:99-238`) | Not client-facing | `PARTIAL`: patch is manual, full historical chain is missing, and legacy-column convergence is not explicit |

## 3-Way Matrix B: Company Certification

**Overall verdict: `FAIL`.** Sequential state transitions, admin API roles, resubmission, and payment gating align, but the expected admin-opened document path is exposed to a P1 malicious-file chain; role UX, static HEAD handling, migration, audit, validation, and concurrency are incomplete.

| Capability | Design / API / DB | Implementation | Client wording | Verdict |
|---|---|---|---|---|
| Applicant role and initial apply | BUSINESS only; open/approved/revision records block; REJECTED permits a new application (`company-certification.md:14-43`; `api-spec.md:3097-3135`) | Service enforces BUSINESS and sequential duplicate check (`CompanyCertificationService.java:59-95`) | BUSINESS can apply; INDIVIDUAL must be blocked or clearly informed (`2-full-feature-checklist.md:193-194`) | `FAIL`: backend passes, but both routes are only auth-gated and INDIVIDUAL sees the upload form |
| State model and latest status | Latest is deterministic; PENDING can become approved/revision/rejected; terminal states are immutable (`company-certification.md:77-99,146-172`) | Entity and repository match (`CompanyCertification.java:50-87`; `CompanyCertificationRepository.java:13-16`) | Four states and next actions are covered (`2-full-feature-checklist.md:198-201`) | `PASS` for sequential behavior |
| Revision resubmission | Same application replaces documents and returns to PENDING (`company-certification.md:46-74`) | Service clears metadata, adds replacements, transitions to PENDING, and schedules old-file cleanup (`CompanyCertificationService.java:100-134,287-307`) | User sees reason and can submit replacements (`2-full-feature-checklist.md:199`) | `PARTIAL`: concurrent resubmits are not serialized; failed post-commit deletion has no durable retry |
| Rejection and new application | REJECTED remains history and allows a new row (`company-certification.md:14-24,94-99`) | Open-status existence check excludes REJECTED (`CompanyCertificationService.java:46-74`) | New-application CTA exists (`CompanyCertStatusPage.tsx:255-263`) | `PARTIAL`: repeated history/files have no approved retention/deletion policy |
| Admin list/detail/review | ADMIN can list, inspect documents, and process only PENDING (`company-certification.md:102-170`) | Security/method guards and entity transition checks match (`CompanyCertificationController.java:74-127`; `SecurityConfig.java:114-117`; `CompanyCertification.java:77-87`) | Admin checklist covers detail, documents, approval/revision/rejection (`3-admin-checklist.md:93-98`) | `PARTIAL`: revision/rejection note is optional in DTO and UI; reviewer identity/review audit is not persisted |
| Business payment gate | APPROVED enables business subscription payment (`company-certification.md:167-172`) | Both current payment paths require an approved record (`BillingAgreementApplicationService.java:441-447`; `PaymentApplicationService.java:251-257`) | Approved user sees subscription CTA (`2-full-feature-checklist.md:201`; `CompanyCertStatusPage.tsx:266-273`) | `PASS` |
| File count/size/extension | Required files, allowed extensions, per-file size and count are documented (`company-certification.md:26-39`; `api-spec.md:3132-3135`) | Backend/frontend share 20 MB per file and 10 files, but service silently drops empty parts and global request limit is 60 MB (`ValidationConstants.java:45-49`; `CompanyCertificationService.java:233-263`; `application.yml:44-46`; `validation.ts:40-44`) | Checklist expects empty/unsupported files to fail (`2-full-feature-checklist.md:195-197`) | `FAIL`: mixed empty parts can pass; aggregate limit is undisclosed; filename/content-type bounds are not validated |
| File authenticity and malware | Sensitive documents are expected to be opened by an admin, but signature/MIME/scan controls are not specified (`company-certification.md:123-142,176-198`) | Extension only; client MIME is persisted and returned on download (`CompanyCertificationService.java:248-276`; `CompanyCertificationController.java:98-112`) | Admin checklist tells reviewer to download/open submitted documents (`3-admin-checklist.md:93-95`) | `FAIL`: P1 malicious-document path |
| API document authorization | Authenticated admin API and document-parent binding are required (`company-certification.md:176-198`) | `@PreAuthorize`, security matchers, and `findByIdAndCertificationId` enforce it (`CompanyCertificationController.java:98-112`; `SecurityConfig.java:115-117`; `CompanyCertificationService.java:190-200`) | Admin-only download wording matches (`3-admin-checklist.md:94-95`) | `PASS` for API path |
| Static resource boundary | Sensitive docs should not be reviewed via unauthenticated static URLs (`company-certification.md:176-198`; `db-schema.md:295-299`) | Static `GET` is ADMIN, but static `HEAD` reaches final permit-all; generic upload resource handler still serves the tree (`SecurityConfig.java:80,131-132`; `WebConfig.java:20-25`) | Client docs say only admin can receive files (`3-admin-checklist.md:94-95`) | `FAIL`: method-specific boundary is incomplete |
| Metadata minimization | Stored path must not be exposed; directory hint is retained for compatibility (`db-schema.md:289,295-320`) | Per-file stored path is hidden, but `documentPath` is returned to applicant/admin and typed in frontend (`CompanyCertificationResponse.java:8-40`; `types/index.ts:253-267`) | Client docs do not require it | `PARTIAL`: unnecessary topology/user-linked metadata remains in responses |
| Apply/review concurrency | One open/locked certification and one valid review outcome are implied | Existence-check then insert, latest-state resubmit, and review update have no row lock/version or DB invariant (`CompanyCertificationService.java:59-134,206-223`; repositories/entities) | Client wording assumes one authoritative status | `FAIL`: duplicate applications/doc sets or last-writer-wins review are possible |
| Schema/manual migration | Parent and per-file child tables are current; child patch is manual (`db-schema.md:279-323`) | Fresh DDL/JPA/child patch match (`schema.sql:144-179`; entities; `20260618_company_certification_documents.sql:5-22`) | Admin UI acknowledges legacy rows with no per-file metadata (`CompanyCertManagePage.tsx:289-315`) | `FAIL` for retained legacy DB until baseline/backfill is approved and verified |
| Storage growth and audit | REJECTED history is retained; no retention/download-audit contract is stated | Files persist with history; no reviewer/download audit model; cleanup failure is log-only in storage service (`CompanyCertificationService.java:287-307`; `LocalStorageService.java:78-87`) | Client wording expects history and admin review, not retention/accountability controls | `PARTIAL`: policy and operational controls are missing |

## Findings

### ATS007-F01 - P1 - Exploitable security defect - Whitelist CSV formula injection

- User-controlled channel fields flow into CSV cells: `WhitelistChannelRequest.java:6-10`; `AdminWhitelistChannelService.java:127-150,200-232`.
- `csv()` performs quote escaping only: `AdminWhitelistChannelService.java:192-198`.
- Static search found zero formula-neutralization branches.
- Impact: opening the export in spreadsheet software can execute attacker-controlled formulas in an admin or external recipient context.
- Required action: neutralize formula-leading/control-prefixed cells before CSV encoding, define the exact safe export contract, and add malicious-cell tests. Quoting alone is not the control.

### ATS007-F02 - P1 - Functional/design defect - Removal lifecycle cannot complete

- `REMOVAL_REQUESTED` counts against the plan: `WhitelistChannel.java:124-130`.
- There is no `REMOVED`/completed state: `WhitelistChannelStatus.java:3-11`; schema enum at `schema.sql:238`.
- Use case says an operator updates status after external removal but does not define the result: `whitelist.md:120-132`.
- Admin can only choose existing generic statuses: `AdminWhitelistChannelService.java:40-47`.
- Impact: a successfully removed external channel can occupy a user slot indefinitely or be forced into a semantically incorrect status.
- Required decision: add a terminal non-counting state (or approved physical-delete/audit model), then define all source-to-target transitions and timestamps.

### ATS007-F03 - P2 - Confirmed functional/privacy defect - Export scope and delivery are misleading

- Keyword filtering applies to list only, while export is status-wide: `WhitelistChannelRepository.java:29-48`; `api-spec.md:3075`.
- UI does not disclose that distinction next to the export action: `WhitelistChannelManagePage.tsx:121-195`.
- Selecting `ALL` confirms no status change but calls the default PENDING export, which changes rows to EXPORTED: `WhitelistChannelManagePage.tsx:121-145`; `AdminWhitelistChannelService.java:110-153`.
- No batch retrieval/re-download endpoint exists; post-commit response failure cannot reproduce the exact batch.
- Impact: an operator can export a broader PII set than the visible result and can lose the exact file after state mutation.

### ATS007-F04 - P2 - Confirmed concurrency defects - Plan, primary, and export invariants

- Plan limit is count-then-write: `WhitelistChannelService.java:119-132`.
- First-primary and clear-then-set are read-then-write: `WhitelistChannelService.java:43-59,135-145`.
- Export selects all rows before mutation and has no claim: `AdminWhitelistChannelService.java:104-157`.
- Schema has indexes but no constraint/claim key for these invariants: `schema.sql:230-294`.
- Static scan found no `@Version`, `@Lock`, pessimistic lock, or `FOR UPDATE` in targeted entities/repositories.
- Impact: concurrent requests can exceed plan slots, produce multiple primaries, or create overlapping export batches.

### ATS007-F05 - P2 - Availability/storage risk - Unbounded whitelist growth and export

- Authenticated users can create unlimited drafts by design and list all rows: `whitelist.md:31-49`; `WhitelistChannelService.java:43-70`.
- Export loads the full selected status and builds rows/items/content in memory: `AdminWhitelistChannelService.java:104-157,171-189`.
- Admin list size has a lower bound but no upper cap and performs active-subscription lookup per row: `AdminWhitelistChannelService.java:55-77,160-162`.
- Required action: bound drafts or paginate user lists, clamp admin pages, claim/page/chunk export, and remove N+1 subscription lookup.

### ATS007-F06 - P1 - Exploitable security defect - Malicious certification document path

- Server validation checks nonempty count, extension, and size only: `CompanyCertificationService.java:233-263`.
- Client-supplied content type is persisted: `CompanyCertificationService.java:266-276`.
- Admin UI intentionally downloads the submitted file: `CompanyCertManagePage.tsx:289-315`.
- Required action: server-side signature/MIME/parser verification, quarantine and malware scanning, macro/active-content policy, safe fixed response type, and isolated private storage.

### ATS007-F07 - P2 - Access-control/UX defect - BUSINESS gate and static HEAD are incomplete

- Apply/status routes use only USER-level authentication: `router/index.tsx:162-164`.
- INDIVIDUAL users interpret no-record 404 as permission to render the form: `CompanyCertApplyPage.tsx:53-87`.
- Backend write enforcement is correct: `CompanyCertificationService.java:59-66,100-107`.
- Static resource protection names GET only, while the final matcher permits remaining requests: `SecurityConfig.java:80,131-132`.
- Required action: add a user-type route/page gate, keep backend enforcement, and deny all methods to the sensitive static tree (prefer removing it from the static resource tree entirely).

### ATS007-F08 - P2 - Functional validation defect - Review reason and upload contract

- Revision/rejection contracts require a reason visible to the applicant: `company-certification.md:158-170`; client checks at `2-full-feature-checklist.md:199-200`.
- Backend DTO accepts null/unbounded note and frontend only shows a placeholder: `CompanyCertificationReviewRequest.java:6-9`; `CompanyCertManagePage.tsx:127-143,341-370`.
- Mixed empty file parts are silently removed instead of rejecting the request: `CompanyCertificationService.java:233-263`.
- Per-file/count UI allows a theoretical 200 MB selection while global request size is 60 MB: `validation.ts:40-44`; `application.yml:44-46`.
- Required action: conditional note validation, explicit aggregate size contract, all-part validation, and filename/content-type length bounds.

### ATS007-F09 - P2 - Concurrency/audit defect - Certification decisions and documents are not serialized

- Apply uses existence-check then insert; resubmit and review load mutable rows without lock/version: `CompanyCertificationService.java:59-134,206-223`.
- Repositories have no locked lookup: `CompanyCertificationRepository.java:13-16`.
- Two admins can both receive success from PENDING and commit last-writer-wins outcomes; two resubmits can retain conflicting metadata/files.
- Reviewer identity, review timestamp for non-approval, and document-download access are not persisted in this domain.
- Required action: lock/version the aggregate, add enforceable open-application invariant or serialized user lock, and persist review/download audit events without document contents.

### ATS007-F10 - P1 conditional - Existing-DB migration/backfill is not release-verifiable

- Runtime defaults to schema validation and manual patches are not auto-run: `application.yml:16-20`; patch headers.
- Current child DDL matches JPA, but no legacy per-file metadata backfill exists: `20260618_company_certification_documents.sql:5-22`.
- Admin UI explicitly handles legacy certifications with no document rows: `CompanyCertManagePage.tsx:289-315`.
- Repository has no complete ordered migration chain or migration framework.
- Impact is conditional on the retained DB baseline; production state was not accessed. Release remains blocked until a copied DB proves schema and legacy-document recoverability.

### ATS007-F11 - P3 - Sensitive metadata minimization

- Full certification responses include a directory-hint field: `CompanyCertificationResponse.java:8-40`; API example at `api-spec.md:3108-3129`.
- Per-file stored paths are correctly omitted: `CompanyCertificationDocumentResponse.java:7-20`.
- Required action: remove the directory hint from applicant/admin response contracts and use opaque document IDs plus the guarded download API.

## Policy Ambiguities Requiring Approval

| ID | Decision required | Why it cannot be inferred safely |
|---|---|---|
| POL-007-01 | Whitelist source-to-target admin matrix and removal-completed model | Current design lists targets but not valid sources or the post-removal terminal state. |
| POL-007-02 | Whitelist external export minimum fields, recipient, retention, and deletion | Current export persists identity/subscription snapshots and sends them externally, but necessity and retention are undocumented. |
| POL-007-03 | Certification document collection notice, retention by outcome, withdrawal handling, and failed-delete reconciliation | Rejected history is retained and documents are sensitive; no lifecycle duration or deletion owner exists. |
| POL-007-04 | Certification reviewer/download audit requirements | Access is role-gated, but accountability and review attribution are absent. |
| POL-007-05 | Legacy certification file migration/backfill or explicit archival disposition | DDL alone cannot reconstruct historical per-file metadata. |

## Intentionally Deferred Automation

- External YouTube/agency registration and removal remain manual by design. The defect is the incomplete local state/audit model, not the absence of YouTube automation.
- Certification review remains human-driven. OCR and automatic approval are outside the approved scope.
- Malware prevention is not categorized as optional automation; it is a required control before admins open untrusted files.

## Positive Controls

- Whitelist mutation ownership checks are present: `WhitelistChannelService.java:81-82,100-101,137-138,152-153,189-192`.
- Strict YouTube host parsing rejects suffix-spoofed hosts: `WhitelistChannelService.java:166-176`.
- Plan-counting states and self-slot exclusion align across backend and frontend: `WhitelistChannelService.java:29-35,119-132`; `WhitelistChannelPage.tsx:35-48,96-98,352-360`.
- Certification entity transition validation and deterministic latest lookup are present: `CompanyCertification.java:50-87`; `CompanyCertificationRepository.java:13-16`.
- Admin certification API methods have method-level guards and path-level guards: `CompanyCertificationController.java:74-127`; `SecurityConfig.java:114-117`.
- Document download binds document ID to certification ID: `CompanyCertificationService.java:190-200`.
- New certification files are deleted on rollback and replaced files after commit: `CompanyCertificationService.java:266-307`.
- Business payment preparation requires an approved certification in both payment paths: `BillingAgreementApplicationService.java:441-447`; `PaymentApplicationService.java:251-257`.

## Commands and Outputs

All commands were read-only/static.

- `git branch --show-current`; `git rev-parse HEAD`; `git status --short --branch`
  - Result: baseline recorded above; pre-existing work was not changed.
- `Get-FileHash -Algorithm SHA256` for the three client checklists, internal feature map, and PDF
  - Result: all hashes match the Phase 1 frozen values.
- Static schema/entity/table audit
  - Result: `SCHEMA_TABLE_COUNT=39`, `ENTITY_COUNT=39`; all five targeted tables found exactly once.
- Static lock/version scan over targeted entities/repositories
  - Result: `TARGET_LOCK_OR_VERSION_MATCHES=0`.
- Targeted frontend test inventory
  - Result: `TARGET_FRONTEND_TEST_FILES=0` for whitelist/company-certification pages.
- Migration dependency scan
  - Result: `MIGRATION_FRAMEWORK_MATCHES=0`.
- Export formula-neutralization scan
  - Result: `CSV_FORMULA_NEUTRALIZATION_MATCHES=0`.
- Controller/frontend API search for whitelist batch retrieval/re-download
  - Result: no batch retrieval/re-download API found.
- Certification review/download audit-model search
  - Result: no certification reviewer/download audit field/model found.
- `python .agents/skills/validate-docs/scripts/validate_docs.py`
  - Result: Tier 0, internal links, 288 traceability IDs, and document index all passed; exit code 0.
- Output-only whitespace/sensitive-value scan
  - Result: trailing whitespace 0, tabs 0, prohibited value-pattern matches 0.

## Tests

### Executed

- No Gradle/npm/browser tests were executed. They create build/cache artifacts outside this WI's owned paths, while the handoff restricts the audit to read-only/static work outside the two outputs.
- No SQL, export, upload, download, or live data operation was executed.

### Existing coverage inspected

- `WhitelistChannelServiceTest.java`: sequential URL, subscription, limit, revision self-slot, ownership, update, and primary-promotion cases.
- `AdminWhitelistChannelServiceTest.java`: normal export snapshot/status and invalid target status; no formula, concurrency, scope, chunk, or recovery cases.
- `CompanyCertificationServiceTest.java`: sequential apply/resubmit/status/review and manually invoked cleanup callbacks; no real transaction, MIME/signature, mixed-empty, aggregate-size, concurrency, or audit cases.
- `CompanyCertificationTest.java`: valid/invalid sequential entity transitions.
- Controller tests: API role checks for normal methods; no sensitive static-resource GET/HEAD test.
- Frontend: no target page test files found.

### Focused follow-up tests

1. Whitelist state-table test covering every user/admin source-to-target pair, including removal completion and repeated removal request.
2. MySQL/Testcontainers two-worker tests for plan limit, first/set-primary, certification apply/resubmit/review, and export claim.
3. CSV adversarial-cell tests for formula-leading and control-prefixed values; verify neutralized output after decoding CSV.
4. Export UI tests for PENDING, non-PENDING, ALL, and keyword-filter scenarios; assert exact scope, confirmation copy, side effects, and no over-export.
5. Export delivery-failure and deterministic batch re-download/recovery test.
6. Whitelist draft/list/export volume boundaries and query-count tests.
7. Company route tests for BUSINESS, INDIVIDUAL, ADMIN, and direct URLs before any file selection/upload.
8. Security-chain tests for unauthenticated/user/admin `GET` and `HEAD` on the sensitive static tree and guarded API download.
9. Certification upload tests for extension/MIME/signature mismatch, malicious document quarantine, mixed empty parts, filename/content-type bounds, aggregate request size, partial store failure, rollback, and failed post-commit delete retry.
10. Certification review tests requiring a reason for revision/rejection and persisting reviewer/audit metadata.
11. Copied-MySQL migration test applying the approved ordered chain, Hibernate validation, legacy certification metadata/backfill, and all targeted enum values. Never run against a live DB.

## Evidence Pointers

- Files created:
  - `deliverables/user/WI-20260711-ATS-007-summary.md` - Korean verdict and approval-focused summary.
  - `deliverables/agent/WI-20260711-ATS-007-evidence-pack.md` - re-verification, matrices, findings, commands, tests, and follow-ups.
- Primary whitelist implementation:
  - `WhitelistChannelService.java:29-217`
  - `AdminWhitelistChannelService.java:40-234`
  - `WhitelistChannel.java:38-130`
  - `WhitelistChannelPage.tsx:24-379`
  - `WhitelistChannelManagePage.tsx:15-307`
- Primary certification implementation:
  - `CompanyCertificationService.java:46-330`
  - `CompanyCertification.java:30-87`
  - `CompanyCertificationController.java:35-139`
  - `CompanyCertApplyPage.tsx:52-245`
  - `CompanyCertStatusPage.tsx:43-275`
  - `CompanyCertManagePage.tsx:78-372`
- Primary schema/migration:
  - `schema.sql:144-179,230-297`
  - `db-schema.md:279-323,744-803`
  - `20260615_align_payment_whitelist_schema.sql:99-238`
  - `20260618_company_certification_documents.sql:5-22`

## Risks / Limitations / Rollback

- No production/local DB, reverse proxy, storage volume, malware scanner, external agency process, or real data was inspected. Conditional findings remain explicitly conditional.
- Static analysis establishes reachable paths and absent controls but does not measure production volume or prove a particular spreadsheet/runtime exploit execution.
- The `HEAD` conclusion follows the current method-specific Spring Security matcher plus final permit-all rule; a regression test is required before remediation is closed.
- Concurrent repository changes may continue outside these two files; all pointers were checked against the recorded HEAD/worktree snapshot.
- Rollback: remove only this WI's two created outputs, and only if explicitly requested.

## Follow-ups / WI Chain

- This WI blocks `WI-20260711-ATS-009` and `WI-20260711-ATS-017` per the handoff.
- `WI-009` should consume both `FAIL` verdicts and add the focused backend/security tests above without live data.
- `WI-017` should review the chosen transition, upload, privacy-retention, audit, and migration decisions after implementation evidence exists.
- A separate approved remediation REQ/WI is required before code, schema, or policy files are changed.
