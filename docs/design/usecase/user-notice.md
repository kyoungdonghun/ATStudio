# User -- Notice Use Cases

> **API Reference**: `docs/design/api-spec.md` Section 9 (Notice)
> **DB Reference**: `docs/design/db-schema.md` Section 12 (`notices`)

---

## ANNOUNCE-001: Create Notice

| Field | Value |
|-------|-------|
| **Code** | ANNOUNCE-001 |
| **Version** | 26-02-20 |
| **Description** | Admin creates a notice. |
| **Actor** | Admin, Backend |
| **Preconditions** | Admin logged in. |
| **Trigger** | Admin clicks the 'Create Notice' button. |
| **Related UC** | ANNOUNCE-004 (update), ANNOUNCE-005 (delete) |

**Main Flow**
1. Admin enters title (title, required), content (content, required), and pinned status (isPinned).
2. Frontend performs validation.
3. Frontend sends data to the backend.
4. Backend verifies authorization, creates a notices record, and returns a 201 response.

**Postconditions**
- Record created in the notices table.

---

## ANNOUNCE-002: List Notices

| Field | Value |
|-------|-------|
| **Code** | ANNOUNCE-002 |
| **Version** | 26-02-20 |
| **Description** | User (including non-members) views the notice list. Pinned notices (is_pinned=1) appear at the top. |
| **Actor** | User (including non-members), Backend |
| **Preconditions** | - |
| **Trigger** | User navigates to the notice list screen. |
| **Related UC** | ANNOUNCE-003 (view detail) |

**Main Flow**
1. Frontend sends a request including page parameters to the backend.
2. Backend returns the notice list. (is_pinned=1 notices sorted to top, then by most recent)

**Postconditions**
- Notice list (title, pinned status, created date) displayed on screen.

---

## ANNOUNCE-003: View Notice Detail

| Field | Value |
|-------|-------|
| **Code** | ANNOUNCE-003 |
| **Version** | 26-02-20 |
| **Description** | User (including non-members) views the full text of a specific notice. |
| **Actor** | User (including non-members), Backend |
| **Preconditions** | Target notice exists in DB. |
| **Trigger** | User clicks a specific notice in the notice list. |
| **Related UC** | - |

**Main Flow**
1. Frontend sends a request including noticeId to the backend.
2. Backend returns the notice detail (title, content, pinned status, created date, updated date).

**Postconditions**
- Full notice text displayed on screen.

---

## ANNOUNCE-004: Update Notice

| Field | Value |
|-------|-------|
| **Code** | ANNOUNCE-004 |
| **Version** | 26-02-20 |
| **Description** | Admin updates an existing notice. |
| **Actor** | Admin, Backend |
| **Preconditions** | Admin logged in. Target notice exists in DB. |
| **Trigger** | Admin clicks the 'Edit' button on the notice detail screen. |
| **Related UC** | ANNOUNCE-003 (view detail) |

**Main Flow**
1. Admin modifies title, content, and isPinned.
2. Frontend sends noticeId and changed data to the backend.
3. Backend verifies authorization, updates the notices record, and returns a 200 response.

**Postconditions**
- Updated notice information reflected in DB.

---

## ANNOUNCE-005: Delete Notice

| Field | Value |
|-------|-------|
| **Code** | ANNOUNCE-005 |
| **Version** | 26-02-20 |
| **Description** | Admin deletes a notice. |
| **Actor** | Admin, Backend |
| **Preconditions** | Admin logged in. Target notice exists in DB. |
| **Trigger** | Admin clicks the 'Delete' button on the notice detail screen. |
| **Related UC** | - |

**Main Flow**
1. Admin clicks the 'Delete' button and confirms.
2. Frontend sends a delete request including noticeId to the backend.
3. Backend verifies authorization, deletes the notices record, and returns 204 No Content.

**Postconditions**
- Notice deleted from DB.
