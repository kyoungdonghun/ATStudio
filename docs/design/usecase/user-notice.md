# User -- Notice Use Cases

> **API Reference**: `docs/design/api-spec.md` Section 9 (Notice)
> **DB Reference**: `docs/design/db-schema.md` Section 12 (`notices`)

---

## Notice Attachment Storage and Download Contract

- New Notice attachments retain the current accepted-file behavior and are
  written only to `StorageRoot.PRIVATE`. Exact type, count, and byte limits are
  pending WI-066 and are not defined here.
- Attachment metadata remains part of the public Notice detail response, while
  bytes are delivered only through
  `GET /api/notices/{noticeId}/attachments/{attachmentId}`.
- The download remains public and returns one Resource as a forced
  `application/octet-stream` attachment with `no-store, private`, `nosniff`,
  sandboxed Content Security Policy, and same-origin resource-policy headers.
  Its `filename*` value percent-encodes CRLF and disposition delimiters so they
  cannot inject an additional response header.
- `/uploads/**` maps only the disjoint public root and cannot resolve new Notice
  attachment objects. Retained files are not moved or deleted by this change.

---

## ANNOUNCE-001: Create Notice

| Field             | Value                                        |
| ----------------- | -------------------------------------------- |
| **Code**          | ANNOUNCE-001                                 |
| **Version**       | 26-08-13                                     |
| **Description**   | Admin creates a notice.                      |
| **Actor**         | Admin, Backend                               |
| **Preconditions** | Admin logged in.                             |
| **Trigger**       | Admin clicks the 'Create Notice' button.     |
| **Related UC**    | ANNOUNCE-004 (update), ANNOUNCE-005 (delete) |

**Main Flow**

1. Admin enters title (title, required, at most 200 characters), content
   (content, required, at most 1,000 characters), pinned status (isPinned), and
   optional attachments.
2. Frontend performs the same title/content validation used by the backend.
   One current-ref create operation disables submit and attachment changes,
   blocks all in-app navigation, and installs a browser-unload guard until the
   request settles. Component cleanup does not abort an accepted mutation.
3. Frontend sends data to the backend.
4. Backend verifies authorization, creates a notices record, stores each
   non-empty attachment under PRIVATE storage, and returns a 201 response.

**Exception / Alternative Flow**

- An authoritative validation, authorization, permission, or not-found response
  preserves the form and permits a deliberate retry.
- A network, server, or unknown outcome makes no success/failure claim. The same
  POST remains disabled and fixed Korean recovery directs the Admin to the
  read-only Notice list to observe the result.

**Postconditions**

- Record created in the notices table.
- New attachment metadata is stored in `notice_attachments`; the file key owns
  an object under PRIVATE storage and has no static public URL.

---

## ANNOUNCE-002: List Notices

| Field             | Value                                                                                                                        |
| ----------------- | ---------------------------------------------------------------------------------------------------------------------------- |
| **Code**          | ANNOUNCE-002                                                                                                                 |
| **Version**       | 26-03-29                                                                                                                     |
| **Description**   | User (including non-members) views the notice list. Pinned notices (is_pinned=1) appear at the top. Supports sort parameter. |
| **Actor**         | User (including non-members), Backend                                                                                        |
| **Preconditions** | -                                                                                                                            |
| **Trigger**       | User navigates to the notice list screen.                                                                                    |
| **Related UC**    | ANNOUNCE-003 (view detail)                                                                                                   |

**Main Flow**

1. Frontend sends a request including page parameters and optional `sort` to the backend.
2. Backend applies sort ordering and returns the notice list.

**Sort Parameter**
| Value | Sort behavior |
|-------|--------------|
| `latest` (default) | `isPinned DESC`, `createdAt DESC` |
| `views` | `isPinned DESC`, `viewCount DESC` |

**Response Fields (NoticeListItemResponse)**
Includes `viewCount` field in addition to title, pinned status, and created date.

**Postconditions**

- Notice list (title, pinned status, viewCount, created date) displayed on screen.

---

## ANNOUNCE-003: View Notice Detail

| Field             | Value                                                                                                               |
| ----------------- | ------------------------------------------------------------------------------------------------------------------- |
| **Code**          | ANNOUNCE-003                                                                                                        |
| **Version**       | 26-03-29                                                                                                            |
| **Description**   | User (including non-members) views the full text of a specific notice. Each access increments the notice viewCount. |
| **Actor**         | User (including non-members), Backend                                                                               |
| **Preconditions** | Target notice exists in DB.                                                                                         |
| **Trigger**       | User clicks a specific notice in the notice list.                                                                   |
| **Related UC**    | -                                                                                                                   |

**Main Flow**

1. Frontend sends `GET /api/notices/{id}` to the backend.
2. Backend retrieves the notice record and increments `notices.viewCount` (within the same transaction).
3. Backend returns the notice detail including all attachments.

**Response Fields**
Includes `viewCount` field in addition to title, content, isPinned, createdAt, and updatedAt.

**Postconditions**

- Full notice text displayed on screen.
- `notices.viewCount` incremented by 1 on every access.

**Frontend Recovery and Attachment Download**

- A `404` renders fixed Korean missing-state copy and a safe Notice-list link;
  it does not offer a meaningless retry. Network, timeout, server, and unknown
  failures render fixed Korean recovery with one manual retry action.
- The latest mounted Notice ID owns the detail request. Route replacement or
  unmount aborts the retired request and prevents stale state commits.
- Attachment requests are owned per attachment. A pending file fences only a
  duplicate request for that file, while another attachment remains
  independently available. Failure stays local to the file and permits a
  same-file retry. Route replacement or unmount aborts pending bytes before a
  browser download effect can run.

---

## ANNOUNCE-004: Update Notice

| Field             | Value                                                       |
| ----------------- | ----------------------------------------------------------- |
| **Code**          | ANNOUNCE-004                                                |
| **Version**       | 26-08-13                                                    |
| **Description**   | Admin updates an existing notice.                           |
| **Actor**         | Admin, Backend                                              |
| **Preconditions** | Admin logged in. Target notice exists in DB.                |
| **Trigger**       | Admin clicks the 'Edit' button on the notice detail screen. |
| **Related UC**    | ANNOUNCE-003 (view detail)                                  |

**Main Flow**

1. The edit page loads `GET /api/notices/{noticeId}/admin`; this ADMIN-only read
   returns the edit projection without incrementing public `viewCount`.
2. Admin modifies title (at most 200 characters), content (at most 1,000
   characters), isPinned, selected attachment deletions, or new attachments.
   Existing attachment deletion remains reversible until save.
3. One current-ref operation fences save, Notice deletion, attachment changes,
   duplicate submit, modal close, all in-app navigation, and browser unload.
   Component cleanup retires UI writes without aborting the accepted mutation.
4. Frontend sends noticeId and changed data to the backend.
5. Backend verifies authorization, stores new attachments under PRIVATE,
   schedules selected PRIVATE objects for after-commit deletion, updates the
   Notice, and returns a 200 response.

**Exception / Alternative Flow**

- An authoritative validation, authorization, permission, or not-found response
  preserves the form and permits a deliberate retry.
- A network, server, or unknown outcome makes no success/failure claim and keeps
  the same PUT disabled. The Admin must use the Notice list or refresh the
  non-counting ADMIN edit projection before another deliberate edit is possible.

**Postconditions**

- Updated notice information reflected in DB.

---

## ANNOUNCE-005: Delete Notice

| Field             | Value                                                         |
| ----------------- | ------------------------------------------------------------- |
| **Code**          | ANNOUNCE-005                                                  |
| **Version**       | 26-08-13                                                      |
| **Description**   | Admin deletes a notice.                                       |
| **Actor**         | Admin, Backend                                                |
| **Preconditions** | Admin logged in. Target notice exists in DB.                  |
| **Trigger**       | Admin clicks the 'Delete' button on the notice detail screen. |
| **Related UC**    | -                                                             |

**Main Flow**

1. Admin clicks the 'Delete' button and confirms.
2. The busy dialog exposes `aria-busy`, disables its header close action, and
   suppresses Escape and backdrop close while the frontend sends a delete
   request including noticeId.
3. Backend verifies authorization, schedules all owned PRIVATE attachment
   objects for after-commit deletion, deletes attachment metadata and the Notice,
   and returns 204 No Content.

**Exception / Alternative Flow**

- An authoritative rejection returns the dialog to a closable, retryable state.
- A network, server, or unknown outcome closes the dialog without claiming
  success or failure, disables another DELETE, and requires a fresh ADMIN edit
  read or Notice-list observation before another deliberate action.

**Postconditions**

- Notice deleted from DB.

---

## ANNOUNCE-006: Download Notice Attachment

| Field             | Value                                                                                       |
| ----------------- | ------------------------------------------------------------------------------------------- |
| **Code**          | ANNOUNCE-006                                                                                |
| **Version**       | 26-08-13                                                                                    |
| **Description**   | Any user downloads an attachment from an existing Notice through the controlled public API. |
| **Actor**         | User (including non-members), Backend                                                       |
| **Preconditions** | Notice and child attachment metadata exist.                                                 |
| **Trigger**       | User selects a Notice attachment.                                                           |
| **Related UC**    | ANNOUNCE-003 (view detail)                                                                  |

**Main Flow**

1. Frontend sends
   `GET /api/notices/{noticeId}/attachments/{attachmentId}`.
2. Backend verifies the Notice and parent/child attachment relationship.
3. Backend resolves the DB-owned key from PRIVATE storage and returns one
   Resource as a forced octet-stream attachment with the fixed safe header set.

**Exception / Alternative Flow**

- Notice or attachment not found, parent mismatch, or unreadable private object:
  existing backend error response; no alternate static path is exposed.

**Postconditions**

- No Notice, attachment metadata, or storage object is mutated.

---

## ANNOUNCE-007: Read Notice Edit Projection

| Field             | Value                                                                             |
| ----------------- | --------------------------------------------------------------------------------- |
| **Code**          | ANNOUNCE-007                                                                      |
| **Version**       | 26-08-13                                                                          |
| **Description**   | Admin loads the minimized Notice edit projection without changing public metrics. |
| **Actor**         | Admin, Backend                                                                    |
| **Preconditions** | Admin logged in. Target notice exists in DB.                                      |
| **Trigger**       | Admin opens a canonical positive safe-integer Notice edit route.                  |
| **Related UC**    | ANNOUNCE-003 (public detail), ANNOUNCE-004 (update)                               |

**Main Flow**

1. Frontend sends `GET /api/notices/{noticeId}/admin` with abort ownership tied
   to the current authenticated ADMIN and route target.
2. Backend enforces ADMIN authorization and executes one read projection for
   title, content, pinned state, and attachment metadata.
3. Backend returns the edit projection without calling the public detail mode.

**Exception / Alternative Flow**

- Missing or noncanonical route IDs issue no Notice, attachment, or mutation
  request and show fixed safe list navigation.
- A missing database row returns the existing not-found error. A transient
  frontend load failure offers a bounded manual retry.

**Postconditions**

- `notices.viewCount` is unchanged.
- No Notice, attachment metadata, or storage object is mutated.
