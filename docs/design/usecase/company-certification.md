# User — Company Certification Use Cases

> **API Reference**: `docs/design/api-spec.md` Section 13 (Company Certification)
> **DB Reference**: `docs/design/db-schema.md` Section 3 (`company_certifications`)
>
> **License Type Distinction**:
> - **Company Certification** (this file): `company_certifications` table. BUSINESS type member submits documents → admin reviews.
> - **Track Usage License**: `licenses` table. → See `user-license.md`.
>
> **Review Process**: BUSINESS member → submit documents (PENDING) → admin review → REVISION_REQUESTED or APPROVED / REJECTED → on approval, certification_code issued → subscription payment enabled

---

## CC-001: Apply for Company Certification [New]

| Field | Value |
|-------|-------|
| **Code** | CC-001 |
| **Version** | 26-02-20 |
| **Description** | A BUSINESS type member applies for a company certification review in order to subscribe to a plan. |
| **Actor** | User (BUSINESS member), Backend |
| **Preconditions** | Logged in. Member with userType=BUSINESS. No existing PENDING application. No existing APPROVED application. Initial version: no UI re-application flow after REJECTED or REVISION_REQUESTED. Admin guides the member directly via email or 1:1 inquiry. Automated re-application flow planned after site stabilization. |
| **Trigger** | User clicks the 'Apply for Company Certification Review' button after selecting a subscription plan. |
| **Related UC** | CC-002 (view status), PAYMENT-001 (subscribe) |

**Main Flow**
1. User uploads review document files (documents, required, multiple files allowed).
2. Frontend sends the files to the backend as multipart/form-data.
3. Backend verifies that the member has userType=BUSINESS.
4. Backend stores the document files at `/uploads/company-docs/{userId}/`.
5. Backend creates a company_certifications record (status=PENDING).
6. Backend returns a success response (201 Created, including id/status/documentPath/createdAt).

**Exception / Alternative Flow**
- Not a BUSINESS type member: 403 response.
- Existing PENDING or APPROVED application: 409 Conflict.

**Postconditions**
- company_certifications record created (status=PENDING). Documents saved to the filesystem.

---

## CC-002: View My Certification Application Status [New]

| Field | Value |
|-------|-------|
| **Code** | CC-002 |
| **Version** | 26-02-20 |
| **Description** | A BUSINESS type member views the status of their company certification review application. |
| **Actor** | User (BUSINESS member), Backend |
| **Preconditions** | Logged in. Member with userType=BUSINESS. |
| **Trigger** | User accesses the 'Company Certification Status' screen. |
| **Related UC** | CC-001 (apply) |

**Main Flow**
1. Frontend sends a request with the auth token to the backend.
2. Backend extracts userId from the JWT and queries the corresponding company_certifications record.
3. Backend returns the status (id, status, adminNote, certificationCode, createdAt).

**Postconditions**
- Review status displayed on screen. Returns null if no application exists.

> **status meanings**: PENDING (awaiting review) / APPROVED (approved) / REVISION_REQUESTED (revision requested) / REJECTED (rejected)
> - On REVISION_REQUESTED: adminNote contains the reason for revision.
> - On APPROVED: certificationCode is included (subscription payment now available).

---

## CC-003: List Certification Applications (Admin) [New]

| Field | Value |
|-------|-------|
| **Code** | CC-003 |
| **Version** | 26-02-20 |
| **Description** | Admin retrieves the list of company certification review applications. |
| **Actor** | Admin, Backend |
| **Preconditions** | Admin logged in. |
| **Trigger** | Admin accesses the 'Company Certification Review List' screen. |
| **Related UC** | CC-004 (detail), CC-005 (process review) |

**Main Flow**
1. Frontend sends a request with optional status filter and page parameters to the backend.
2. Backend returns a paginated list of company_certifications records.

**Postconditions**
- Review application list and pageInfo displayed on screen.

---

## CC-004: View Certification Application Detail (Admin) [New]

| Field | Value |
|-------|-------|
| **Code** | CC-004 |
| **Version** | 26-02-20 |
| **Description** | Admin retrieves the detail of a specific company certification application. |
| **Actor** | Admin, Backend |
| **Preconditions** | Admin logged in. Target application exists in DB. |
| **Trigger** | Admin clicks a specific application in the review list. |
| **Related UC** | CC-003 (list), CC-005 (process review) |

**Main Flow**
1. Frontend sends a request with certificationId to the backend.
2. Backend returns the application detail (applicant info, status, documentPath, adminNote, certificationCode, etc.).

**Postconditions**
- Application detail and submitted document path displayed on screen.

---

## CC-005: Process Certification Review (Admin) [New]

| Field | Value |
|-------|-------|
| **Code** | CC-005 |
| **Version** | 26-02-20 |
| **Description** | Admin approves, requests revision, or rejects a company certification application. On approval, certification_code is auto-generated. |
| **Actor** | Admin, Backend |
| **Preconditions** | Admin logged in. Target application exists in DB. |
| **Trigger** | Admin clicks the review result button on the application detail screen. |
| **Related UC** | CC-004 (detail) |

**Main Flow**
1. Admin enters the review result (status: APPROVED/REVISION_REQUESTED/REJECTED) and adminNote.
2. Frontend sends certificationId, status, and adminNote to the backend.
3. Backend verifies authorization and updates the company_certifications record.
   - If APPROVED: auto-generates certification_code (UUID-based), records approved_at.
   - If REVISION_REQUESTED/REJECTED: saves reason in adminNote.
4. Backend returns the result (id, status, certificationCode, approvedAt) and a 200 OK response.

**Postconditions**
- company_certifications record status updated.
- If APPROVED: certification_code issued, approved_at recorded. The BUSINESS member may now proceed with subscription (PAYMENT-001).

> **Integration**: When an approved BUSINESS member initiates PAYMENT-001 (subscribe), the backend verifies that company_certifications.status=APPROVED.
