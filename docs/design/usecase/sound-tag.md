# Sound -- Tag Use Cases

> **API Reference**: `docs/design/api-spec.md` Section 2 (Tag)
> **DB Reference**: `docs/design/db-schema.md` Section 4.2, 4.3 (`tags`, `track_tags`)

---

## SOUND-003: Create Tag

| Field | Value |
|-------|-------|
| **Code** | SOUND-003 |
| **Version** | 26-02-20 |
| **Description** | Admin creates a new tag. |
| **Actor** | Admin, Backend |
| **Preconditions** | Admin logged in. |
| **Trigger** | Admin clicks the 'Create Tag' button. |
| **Related UC** | SOUND-001 (create track), SOUND-012 (update track) |

**Main Flow**
1. Admin enters the tag name (name) and type (type: MOOD/GENRE/INSTRUMENT).
2. Frontend sends the data to the backend.
3. Backend performs validation (name UNIQUE check).
4. Backend creates a record in the tags table and returns a 201 response.

**Exception / Alternative Flow**
- Duplicate name: 409 response.

**Postconditions**
- New tag saved in the tags table.

---

## Tag List Query

> **Sub UC** (no separate code): included within SOUND-001 (create track) and SOUND-005 (list tracks).

| Field | Value |
|-------|-------|
| **API** | `GET /api/tags?type={MOOD\|GENRE\|INSTRUMENT}` |
| **Version** | 26-02-20 |
| **Description** | Retrieves the full tag list for filter UI or tag selection popup. Filterable by type parameter. |
| **Actor** | User (including non-members), Backend |
| **Preconditions** | - |
| **Trigger** | Automatically called when tag selection UI loads. |
| **Related UC** | SOUND-001 (create track - tag selection popup), SOUND-005 (list tracks - filter UI) |

**Main Flow**
1. Frontend requests the tag list with an optional type parameter.
2. Backend returns the full tag list matching the criteria. (No pagination)

**Postconditions**
- Tag list (id, name, type) returned.

---

## SOUND-014: Update Tag

| Field | Value |
|-------|-------|
| **Code** | SOUND-014 |
| **Version** | 26-02-20 |
| **Description** | Admin updates the name or type of an existing tag. |
| **Actor** | Admin, Backend |
| **Preconditions** | Admin logged in. Target tag exists in DB. |
| **Trigger** | Admin clicks the 'Edit' button in the tag list. |
| **Related UC** | - |

**Main Flow**
1. Admin enters the updated name and type.
2. Frontend sends tagId and changed data to the backend.
3. Backend performs validation and UNIQUE check.
4. Backend updates the tag and returns the updated tag information.

**Exception / Alternative Flow**
- Duplicate name: 409 response.

**Postconditions**
- Tag name and type reflected in DB. Tag display on tracks referencing this tag is updated immediately.

---

## SOUND-018: Delete Tag

| Field | Value |
|-------|-------|
| **Code** | SOUND-018 |
| **Version** | 26-02-20 |
| **Description** | Admin deletes a tag. |
| **Actor** | Admin, Backend |
| **Preconditions** | Admin logged in. Target tag exists in DB. |
| **Trigger** | Admin clicks the 'Delete' button in the tag list. |
| **Related UC** | - |

**Main Flow**
1. Admin clicks the 'Delete' button on the target tag.
2. Frontend sends a delete request including tagId to the backend.
3. Backend deletes the tags record. (Associated track_tags are CASCADE-deleted or handled at application level)
4. Backend returns 204 No Content.

**Exception / Alternative Flow**
- -

**Postconditions**
- Tag deleted from the tags table.
