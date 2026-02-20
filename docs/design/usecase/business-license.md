# User — Business License Use Cases

> **API Reference**: `docs/design/api-spec.md` Section 13 (Business License Review)
> **DB Reference**: `docs/design/db-schema.md` Section 3 (`business_license_requests`)
>
> **License Type Distinction**:
> - **Business Review License** (this file): `business_license_requests` table. BUSINESS type member submits documents → admin reviews.
> - **Track Usage License**: `licenses` table. → See `user-license.md`.
>
> **Review Process**: BUSINESS member → submit documents (PENDING) → admin review → REVISION_REQUESTED or APPROVED / REJECTED → on approval, license_code issued → subscription payment enabled

---

## BL-001: Apply for Business License [New]

| Field | Value |
|-------|-------|
| **Code** | BL-001 |
| **Version** | 26-02-20 |
| **Description** | A BUSINESS type member applies for a business license review in order to subscribe to a plan. |
| **Actor** | User (BUSINESS member), Backend |
| **Preconditions** | Logged in. Member with userType=BUSINESS. No existing PENDING application. No existing APPROVED application. (Reapplication allowed after REJECTED or REVISION_REQUESTED.) |
| **Trigger** | User clicks the 'Apply for Business License Review' button after selecting a subscription plan. |
| **Related UC** | BL-002 (view status), PAYMENT-001 (subscribe) |

**Main Flow**
1. User uploads review document files (documents, required, multiple files allowed).
2. Frontend sends the files to the backend as multipart/form-data.
3. Backend verifies that the member has userType=BUSINESS.
4. Backend stores the document files at `/uploads/business-docs/{userId}/`.
5. Backend creates a business_license_requests record (status=PENDING).
6. Backend returns a success response (201 Created, including id/status/documentPath/createdAt).

**Exception / Alternative Flow**
- Not a BUSINESS type member: 403 response.
- Existing PENDING or APPROVED application: 409 Conflict.

**Postconditions**
- business_license_requests record created (status=PENDING). Documents saved to the filesystem.

---

## BL-002: View My License Application Status [New]

| Field | Value |
|-------|-------|
| **Code** | BL-002 |
| **Version** | 26-02-20 |
| **Description** | A BUSINESS type member views the status of their business license review application. |
| **Actor** | User (BUSINESS member), Backend |
| **Preconditions** | Logged in. Member with userType=BUSINESS. |
| **Trigger** | User accesses the 'Business License Status' screen. |
| **Related UC** | BL-001 (apply) |

**Main Flow**
1. Frontend sends a request with the auth token to the backend.
2. Backend extracts userId from the JWT and queries the corresponding business_license_requests record.
3. Backend returns the status (id, status, adminNote, licenseCode, createdAt).

**Postconditions**
- Review status displayed on screen. Returns null if no application exists.

> **status meanings**: PENDING (awaiting review) / APPROVED (approved) / REVISION_REQUESTED (revision requested) / REJECTED (rejected)
> - On REVISION_REQUESTED: adminNote contains the reason for revision.
> - On APPROVED: licenseCode is included (subscription payment now available).

---

## BL-003: List License Applications (Admin) [New]

| Field | Value |
|-------|-------|
| **Code** | BL-003 |
| **Version** | 26-02-20 |
| **Description** | Admin retrieves the list of business license review applications. |
| **Actor** | Admin, Backend |
| **Preconditions** | Admin logged in. |
| **Trigger** | Admin accesses the 'Business License Review List' screen. |
| **Related UC** | BL-004 (detail), BL-005 (process review) |

**Main Flow**
1. Frontend sends a request with optional status filter and page parameters to the backend.
2. Backend returns a paginated list of business_license_requests records.

**Postconditions**
- Review application list and pageInfo displayed on screen.

---

## BL-004: View License Application Detail (Admin) [New]

| Field | Value |
|-------|-------|
| **Code** | BL-004 |
| **Version** | 26-02-20 |
| **Description** | Admin retrieves the detail of a specific business license application. |
| **Actor** | Admin, Backend |
| **Preconditions** | Admin logged in. Target application exists in DB. |
| **Trigger** | Admin clicks a specific application in the review list. |
| **Related UC** | BL-003 (list), BL-005 (process review) |

**Main Flow**
1. Frontend sends a request with requestId to the backend.
2. Backend returns the application detail (applicant info, status, documentPath, adminNote, licenseCode, etc.).

**Postconditions**
- Application detail and submitted document path displayed on screen.

---

## BL-005: Process License Review (Admin) [New]

| Field | Value |
|-------|-------|
| **Code** | BL-005 |
| **Version** | 26-02-20 |
| **Description** | Admin approves, requests revision, or rejects a business license application. On approval, license_code is auto-generated. |
| **Actor** | Admin, Backend |
| **Preconditions** | Admin logged in. Target application exists in DB. |
| **Trigger** | Admin clicks the review result button on the application detail screen. |
| **Related UC** | BL-004 (detail) |

**Main Flow**
1. Admin enters the review result (status: APPROVED/REVISION_REQUESTED/REJECTED) and adminNote.
2. Frontend sends requestId, status, and adminNote to the backend.
3. Backend verifies authorization and updates the business_license_requests record.
   - If APPROVED: auto-generates license_code (UUID-based), records approved_at.
   - If REVISION_REQUESTED/REJECTED: saves reason in adminNote.
4. Backend returns the result (id, status, licenseCode, approvedAt) and a 200 OK response.

**Postconditions**
- business_license_requests record status updated.
- If APPROVED: license_code issued, approved_at recorded. The BUSINESS member may now proceed with subscription (PAYMENT-001).

> **Integration**: When an approved BUSINESS member initiates PAYMENT-001 (subscribe), the backend verifies that business_license_requests.status=APPROVED.
