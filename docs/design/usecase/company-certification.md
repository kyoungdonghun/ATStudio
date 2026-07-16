# User — Company Certification Use Cases

> **API Reference**: `docs/design/api-spec.md` Section 13 (Company Certification)
> **DB Reference**: `docs/design/db-schema.md` Section 3 (`company_certifications`, `company_certification_documents`, `company_certification_audit_logs`)
>
> **License Type Distinction**:
> - **Company Certification** (this file): BUSINESS type member submits company documents and admin reviews them before business subscription payment is allowed.
> - **Track Usage License**: `licenses` table. → See `user-license.md`.
>
> **Review Process**: BUSINESS member → submit documents (PENDING) → admin downloads/reviews documents → APPROVED / REVISION_REQUESTED / REJECTED. APPROVED enables business subscription payment. REVISION_REQUESTED allows document replacement on the same application. REJECTED preserves history and allows a new application.
>
> **Frontend Boundary**: `/company-certification/apply` and `/company-certification/status` are available only to an authenticated USER whose `userType` is `BUSINESS`. This route gate is early UX guidance; backend authorization remains the source of truth.

---

## CC-001: Apply for Company Certification [Updated]

| Field | Value |
|-------|-------|
| **Code** | CC-001 |
| **Version** | 26-07-16 |
| **Description** | A BUSINESS type member applies for company certification review before subscribing to a business plan. |
| **Actor** | User (BUSINESS member), Backend |
| **Preconditions** | Logged in. Member with userType=BUSINESS. No existing PENDING, APPROVED, or REVISION_REQUESTED application. REJECTED history does not block a new application. |
| **Trigger** | User opens the company certification application screen from profile, payment gate, or rejected-status CTA. |
| **Related UC** | CC-002 (resubmit), CC-003 (view status), PAYMENT-001 (subscribe) |

**Main Flow**
1. User uploads review document files. Frontend permits PDF/JPG/JPEG/PNG, at most 10 files, 20 MiB per file, 50 MiB in aggregate, 255 characters per filename, and rejects empty files across consecutive selections.
2. Frontend sends the files to the backend as multipart/form-data. These checks are early guidance only; backend validation is authoritative.
3. Backend locks the owning user row before checking open applications, then verifies userType=BUSINESS.
4. Backend rejects any null/empty multipart part and enforces count, per-file/aggregate size, filename length, extension, signature, and MIME rules.
5. Backend stores the document files under a private per-submission directory.
6. Backend creates `company_certifications` (status=PENDING) and `company_certification_documents` metadata rows.
7. Backend returns a success response without a storage directory hint or stored path.

**Exception / Alternative Flow**
- Not a BUSINESS type member: 403 response.
- Existing PENDING, APPROVED, or REVISION_REQUESTED application: 409 Conflict.
- Empty parts, unsupported extension, invalid signature/MIME, filename length, file-size/count, or 50 MiB aggregate violation: validation error.
- PNG input is verified as `image/png` and then canonicalized to private JPEG output with `image/jpeg`; this workflow does not retain submitted PNG bytes.

**Postconditions**
- company_certifications record created (status=PENDING).
- Document metadata rows created. Raw file contents stay in StorageService-managed storage.

---

## CC-002: Resubmit Documents After Revision Request [New]

| Field | Value |
|-------|-------|
| **Code** | CC-002 |
| **Version** | 26-07-16 |
| **Description** | A BUSINESS member replaces documents on the latest REVISION_REQUESTED certification application. |
| **Actor** | User (BUSINESS member), Backend |
| **Preconditions** | Logged in. Latest certification status is REVISION_REQUESTED. |
| **Trigger** | User clicks the resubmit action on the company certification status screen. |
| **Related UC** | CC-003 (view status), CC-006 (process review) |

**Main Flow**
1. User checks the admin note that explains why revision was requested.
2. User uploads replacement documents.
3. Frontend sends the files to `POST /api/company-certifications/me/documents`.
4. Backend locks the owning user row, then locks the latest certification (`createdAt DESC`, then `id DESC` as tie-breaker) before validating member type/status.
5. Backend stores replacement files, clears old document metadata, updates document metadata, and changes status to PENDING without a lost update.
6. Backend returns the updated certification status.

**Exception / Alternative Flow**
- Latest status is not REVISION_REQUESTED: invalid state transition.
- No certification exists: 404 response.
- Invalid files: validation error.

**Postconditions**
- Same certification application returns to PENDING.
- Previous document metadata is removed and replacement metadata becomes the review target.

---

## CC-003: View My Certification Application Status [Updated]

| Field | Value |
|-------|-------|
| **Code** | CC-003 |
| **Version** | 26-07-16 |
| **Description** | A BUSINESS member views the latest certification review status and next action. |
| **Actor** | User (BUSINESS member), Backend |
| **Preconditions** | Logged in. Member with userType=BUSINESS. |
| **Trigger** | User accesses the company certification status screen. |
| **Related UC** | CC-001 (apply), CC-002 (resubmit) |

**Main Flow**
1. Frontend sends a request with the auth token to the backend.
2. Backend extracts userId from the JWT and queries the latest company_certifications record (`createdAt DESC`, then `id DESC`).
3. Backend returns status, adminNote, certificationCode, approvedAt, and submitted document metadata. The response omits `documentPath` and all individual stored paths.

**Postconditions**
- PENDING: review status displayed.
- APPROVED: certification code and subscription CTA displayed.
- REVISION_REQUESTED: admin note and resubmission form displayed.
- REJECTED: rejection note and new application CTA displayed.

---

## CC-004: List Certification Applications (Admin) [Updated]

| Field | Value |
|-------|-------|
| **Code** | CC-004 |
| **Version** | 26-07-16 |
| **Description** | Admin retrieves the list of company certification review applications. |
| **Actor** | Admin, Backend |
| **Preconditions** | Admin logged in. |
| **Trigger** | Admin accesses the company certification review screen. |
| **Related UC** | CC-005 (detail), CC-006 (process review) |

**Main Flow**
1. Frontend sends a request with optional status filter and page parameters.
2. Backend returns a paginated list of certification records with applicant email and company name; page size is bounded to 100.

**Postconditions**
- Review list and pageInfo displayed on screen.

---

## CC-005: View Certification Application Detail and Documents (Admin) [Updated]

| Field | Value |
|-------|-------|
| **Code** | CC-005 |
| **Version** | 26-07-16 |
| **Description** | Admin retrieves the detail of a certification application and downloads submitted documents for review. |
| **Actor** | Admin, Backend |
| **Preconditions** | Admin logged in. Target application exists in DB. |
| **Trigger** | Admin clicks the detail action in the review list. |
| **Related UC** | CC-004 (list), CC-006 (process review), CC-007 (document download) |

**Main Flow**
1. Frontend requests certification detail by certificationId.
2. Backend returns applicant info, status, adminNote, certificationCode, and document metadata.
3. Admin clicks a document download action.
4. Backend resolves the private file through an authenticated admin-only API and records narrow access-grant evidence (actor, time, action, certification ID, opaque document ID) before the controller streams bytes.

**Postconditions**
- Admin can review actual submitted documents before processing the application.

---

## CC-006: Process Certification Review (Admin) [Updated]

| Field | Value |
|-------|-------|
| **Code** | CC-006 |
| **Version** | 26-07-16 |
| **Description** | Admin approves, requests revision, or rejects a company certification application. |
| **Actor** | Admin, Backend |
| **Preconditions** | Admin logged in. Target application exists and is PENDING. |
| **Trigger** | Admin clicks the review result button on the application detail screen. |
| **Related UC** | CC-005 (detail) |

**Main Flow**
1. Admin enters the review result and applicant-visible note. `REVISION_REQUESTED` and `REJECTED` require a trimmed nonblank reason; `APPROVED` may omit its note. Every supplied note is limited to 500 characters.
2. Frontend sends certificationId, status, and adminNote to the backend.
3. Backend locks the owning user row and then the target certification row, validates the transition and conditional reason before any state/audit mutation, and updates the company_certifications record.
   - APPROVED: auto-generates certification_code, records approved_at.
   - REVISION_REQUESTED: stores reason in adminNote. User may resubmit documents through CC-002.
   - REJECTED: stores reason in adminNote. User may submit a new application through CC-001.
4. Backend records reviewer identity, timestamp, and from/to statuses without storing document contents or paths, then returns the updated certification.

**Postconditions**
- If APPROVED: certification_code issued, approved_at recorded, and business subscription payment becomes available.
- If REVISION_REQUESTED: same application waits for user resubmission.
- If REJECTED: application remains as immutable review history.

> **Integration**: When an approved BUSINESS member initiates PAYMENT-001 (subscribe), the backend verifies that company_certifications.status=APPROVED.

---

## CC-007: Protect Company Certification Documents [New]

| Field | Value |
|-------|-------|
| **Code** | CC-007 |
| **Version** | 26-07-16 |
| **Description** | Company certification documents are treated as sensitive files and reviewed through authenticated APIs. |
| **Actor** | Admin, Backend |
| **Preconditions** | Admin logged in. Document belongs to the requested certification. |
| **Trigger** | Admin downloads a document from the certification detail screen. |
| **Related UC** | CC-005 (detail) |

**Main Flow**
1. Admin requests `GET /api/company-certifications/{certificationId}/documents/{documentId}`.
2. Backend verifies admin authority and document ownership.
3. Backend streams the file with safe content disposition.
4. Backend records an authorization-and-private-resource-resolution access grant, not byte-delivery completion, without persisting file bytes, storage paths, filenames, notes, profile snapshots, tokens, or raw request data.

**Exception / Alternative Flow**
- Non-admin access: 403 response.
- Document not found or not owned by certification: 404 response.

**Postconditions**
- Sensitive company documents are not reviewed through unauthenticated static URLs.

---

## Policy Boundary

- Certification documents remain in private storage and are delivered only through the guarded attachment endpoint.
- `POLICY-PENDING`: this workflow does not introduce automatic retention deletion, withdrawal deletion, a retention scheduler, or a retention duration.
- `ENVIRONMENT-CONDITIONAL`: retained MySQL databases need the dated manual patch and rehearsal/backfill assessment before Hibernate validation. No DDL is run by this use case.
- `POLICY-PENDING`: signature and canonical-image validation reduce malformed-content risk but are not malware scanning. No scanner dependency is selected or required here.
