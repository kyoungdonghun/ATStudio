# User -- Question Use Cases

> **API Reference**: `docs/design/api-spec.md` Section 8 (Question)
> **DB Reference**: `docs/design/db-schema.md` Section 10, 13 (`questions`, `answers`, `question_attachments`)

---

## QUESTION-001: Create Inquiry

| Field | Value |
|-------|-------|
| **Code** | QUESTION-001 |
| **Version** | 26-02-20 |
| **Description** | Logged-in member creates a post on the inquiry board. |
| **Actor** | User (Member), Backend |
| **Preconditions** | Logged in. |
| **Trigger** | User clicks the 'Create Inquiry' button. |
| **Related UC** | QUESTION-004 (inquiry detail) |

**Main Flow**
1. User enters inquiry content.
   - Title (title, required), content (content, required), category (category: DOWNLOAD/PAYMENT/COPYRIGHT/PRODUCTION/OTHER, required), visibility (isPublic, required)
2. Uploads attachments (optional).
3. Frontend performs validation.
4. Frontend sends data as multipart/form-data to the backend.
5. Backend performs validation.
6. Backend creates a questions record (status=OPEN).
7. If attachments exist, saves files to storage and creates question_attachments records.
8. Backend returns a success response (201 Created).

**Postconditions**
- questions record (status=OPEN) created. If attachments exist, saved in question_attachments.

---

## QUESTION-002: Write Answer

| Field | Value |
|-------|-------|
| **Code** | QUESTION-002 |
| **Version** | 26-02-20 |
| **Description** | Inquiry author or admin writes an answer to an inquiry. On the admin's first answer, the inquiry status automatically changes from OPEN to IN_PROGRESS. |
| **Actor** | User (question author or Admin), Backend |
| **Preconditions** | Logged in. Inquiry exists. Members can only answer their own inquiries; admins can answer all. |
| **Trigger** | User clicks the 'Write Answer' button on the inquiry detail screen. |
| **Related UC** | QUESTION-004 (inquiry detail) |

**Main Flow**
1. User enters the answer content and clicks the 'Submit' button.
2. Frontend sends questionId and content to the backend.
3. Backend checks access permissions. (Own inquiry or ADMIN)
4. Backend creates a record in the answers table.
5. If admin writes the first answer: changes questions.status from OPEN to IN_PROGRESS.
6. Updates questions.updated_at.
7. Backend returns a success response (201 Created).

**Exception / Alternative Flow**
- Non-author member accessing another's inquiry: 403 response.

**Postconditions**
- answers record created. If admin's first answer, inquiry status=IN_PROGRESS.

---

## QUESTION-003: List Inquiries

| Field | Value |
|-------|-------|
| **Code** | QUESTION-003 |
| **Version** | 26-08-14 |
| **Description** | Logged-in user views the inquiry list. Regular members: public inquiries + own private inquiries. Admin: all. |
| **Actor** | User (Member or Admin), Backend |
| **Preconditions** | Logged in. |
| **Trigger** | User navigates to the inquiry list screen. |
| **Related UC** | QUESTION-004 (inquiry detail) |

**Main Flow**
1. Frontend sends criteria (category, status, mine flag) and page parameters to the backend.
2. Backend filters the inquiry list based on user role and returns paginated results.
   - Regular member: is_public=1 OR (is_public=0 AND user_id=self)
   - Admin: all
3. Filter, page, tab, route, or authenticated-owner changes abort and retire
   the prior request and advance the request generation. List/detail data,
   loading, errors, forms, attachment controls, and confirmation targets render
   only for the current projection; detached controls and mutation
   continuations revalidate it. A stale success or failure cannot overwrite the
   newer filter or page.

**Postconditions**
- Inquiry list matching access permissions and pageInfo displayed on screen.

---

## QUESTION-004: View Inquiry Detail

| Field | Value |
|-------|-------|
| **Code** | QUESTION-004 |
| **Version** | 26-02-20 |
| **Description** | Logged-in user views inquiry detail (including answers). Private inquiries are viewable only by the author and admins. |
| **Actor** | User (Member or Admin), Backend |
| **Preconditions** | Logged in. Inquiry exists in DB. |
| **Trigger** | User clicks a specific inquiry in the inquiry list. |
| **Related UC** | QUESTION-002 (write answer), QUESTION-005 (download attachment) |

**Main Flow**
1. Frontend sends a request including questionId to the backend.
2. Backend checks the inquiry's visibility and access permissions.
3. Backend returns inquiry detail (title, content, category, visibility, status, author, attachment list, answer list).

**Exception / Alternative Flow**
- Non-author accessing a private inquiry: 403 response.
- Detail accepts only a canonical ASCII decimal `questionId` matching
  `[1-9][0-9]*` and a safe integer. Invalid values render fixed Korean list
  recovery with no request; valid route changes retire and hide the prior
  projection, dialogs, controls, and stale completions.

**Postconditions**
- Inquiry detail and answer list displayed on screen.

---

## QUESTION-005: Download Attachment

| Field | Value |
|-------|-------|
| **Code** | QUESTION-005 |
| **Version** | 26-02-20 |
| **Description** | Downloads an inquiry attachment. Access permissions are the same as inquiry viewing (private: author + admin only). |
| **Actor** | User (Member or Admin), Backend |
| **Preconditions** | Logged in. Attachment exists in DB. Has inquiry viewing permission. |
| **Trigger** | User clicks an attachment on the inquiry detail screen. |
| **Related UC** | QUESTION-004 (inquiry detail) |

**Main Flow**
1. Frontend starts one owned attachment request including questionId and attachmentId.
   While it is pending, all attachment actions are disabled and the active
   attachment displays bounded progress feedback.
2. Backend checks inquiry viewing permissions.
3. Backend retrieves the file from storage and returns it (Content-Disposition: attachment).
4. Frontend triggers the browser download only if the initiating authenticated
   owner, route, and detail projection are still current. Route, owner,
   projection replacement, or unmount aborts and retires the request.

**Exception / Alternative Flow**
- No permission: 403 response.
- A non-cancellation failure retains the detail projection, displays a fixed
  retryable error, and restores all attachment actions.

**Postconditions**
- Attachment downloaded to user's device.

---

> **Inquiry edit policy**: Inquiry editing is not supported. Frontend displays a no-edit notice and guides users to delete and rewrite.

## QUESTION-006: Delete Inquiry

| Field | Value |
|-------|-------|
| **Code** | QUESTION-006 |
| **Version** | 26-02-20 |
| **Description** | Inquiry author (if status=OPEN) or admin deletes an inquiry. |
| **Actor** | User (question author or Admin), Backend |
| **Preconditions** | Logged in. Own inquiry (must be status=OPEN) or admin. |
| **Trigger** | User clicks the 'Delete' button on the inquiry detail screen. |
| **Related UC** | - |

**Main Flow**
1. A non-admin owner sees the 'Delete' button only while the loaded inquiry is
   OPEN. An admin's delete policy is evaluated separately and is not restricted
   by inquiry status.
2. User clicks the 'Delete' button and confirms.
3. Frontend sends a delete request including questionId to the backend.
4. Backend checks deletion permissions. (Own + OPEN or ADMIN)
5. Backend deletes associated answers and question_attachments, then deletes the questions record.
6. Backend returns 204 No Content.

**Exception / Alternative Flow**
- Regular member attempting to delete a non-OPEN inquiry: 403 response.

**Postconditions**
- Inquiry and related records (answers, attachments) deleted.

---

## QUESTION-007: Change Inquiry Status (Admin) [New]

| Field | Value |
|-------|-------|
| **Code** | QUESTION-007 |
| **Version** | 26-02-20 |
| **Description** | Admin changes the processing status of an inquiry. |
| **Actor** | Admin, Backend |
| **Preconditions** | Admin logged in. Inquiry exists in DB. |
| **Trigger** | Admin selects a legal target status on the ADMIN inquiry list screen. |
| **Related UC** | QUESTION-002 (write answer), QUESTION-003 (inquiry list) |

**Main Flow**
1. Admin selects one legal target for the inquiry's current status. While one
   status request is pending, every status control is unavailable.
2. Frontend sends questionId and status to the backend. (`PUT /api/questions/{questionId}/status`)
3. Backend verifies admin authorization.
4. Backend validates the transition, updates questions.status, and returns a 200 response.
5. Frontend applies the status returned by the response. A rejection retains
   the existing row and exposes a retryable fixed error without reloading the
   collection.

**Status Flow**
- OPEN -> IN_PROGRESS or CLOSED
- IN_PROGRESS -> RESOLVED or CLOSED
- RESOLVED -> CLOSED
- CLOSED has no outgoing transition

**Postconditions**
- questions.status updated.
