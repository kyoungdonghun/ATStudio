# Sound -- Tag Use Cases

> **API Reference**: `docs/design/api-spec.md` Section 2 (Tag)
> **DB Reference**: `docs/design/db-schema.md` Section 4.2, 4.3 (`tags`, `track_tags`)

---

## SOUND-003: Create Tag

| Field | Value |
|-------|-------|
| **Code** | SOUND-003 |
| **Version** | 26-08-09 |
| **Description** | Admin creates a new tag. |
| **Actor** | Admin, Backend |
| **Preconditions** | Admin logged in. |
| **Trigger** | Admin clicks the 'Create Tag' button. |
| **Related UC** | SOUND-001 (create track), SOUND-012 (update track) |

**Main Flow**
1. Admin enters the tag name (name) and type (type: MOOD/GENRE/INSTRUMENT/USAGE).
2. Frontend canonicalizes the value for immediate validation and duplicate
   guidance, then sends the unprefixed name and type to the backend.
3. Backend trims edge spaces, collapses internal Unicode space-separator runs,
   normalizes to NFC, validates the 50-character allowlist, and performs the
   global name uniqueness check.
4. Backend creates a `tags` row and returns a 201 `data` response.

**Exception / Alternative Flow**
- Duplicate name, including an exact `uq_tags_name` race: 409
  `TAG_NAME_DUPLICATED`.
- Invalid name: 400 `TAG_NAME_INVALID`.
- The create/edit modal, current input/type, list, and filter remain visible
  after a failed save.

**Postconditions**
- New tag saved in the tags table.

---

## Tag List Query

> **Sub UC** (no separate code): included within SOUND-001 (create track) and SOUND-005 (list tracks).

| Field | Value |
|-------|-------|
| **API** | `GET /api/tags?type={MOOD\|GENRE\|INSTRUMENT\|USAGE}` |
| **Version** | 26-08-09 |
| **Description** | Retrieves the full tag list for filter UI or tag selection popup. Filterable by type parameter. |
| **Actor** | User (including non-members), Backend |
| **Preconditions** | - |
| **Trigger** | Automatically called when tag selection UI loads. |
| **Related UC** | SOUND-001 (create track - tag selection popup), SOUND-005 (list tracks - filter UI) |

**Main Flow**
1. Frontend requests the tag list with an optional type parameter.
2. Backend returns the full tag list matching the criteria in `dataList`.
   There is no pagination or `content` field.

**Postconditions**
- Tag list (id, name, type) returned.

**Usage Guide Tags**
- `USAGE` tags are visible guide/search hashtags for user-facing discovery (for example, `#쇼츠용`, `#유튜브용`, `#릴스용`).
- They are stored in the same `tags` and `track_tags` tables as other tag types.
- `#` is a display prefix and is not stored as part of a new Tag name.
- A Usage Guide Tag describes a discovery context. It is not a License and does
  not grant usage rights.
- They do not replace `artistName`; `artistName` remains uploader nickname metadata.

## Available Tag Query

`GET /api/tags/available` returns `dataList` for Tag choices linked to active
Tracks under the current cross-filter.

- `genre`, `mood`, `instrument`, and `usage` are repeated query parameters.
- Values are canonicalized and de-duplicated, then all selected values are
  combined with AND semantics.
- BPM minimum/maximum participates in the same availability query.
- The backend executes one bounded native query; user values are bound
  parameters rather than SQL fragments.
- If this availability request fails, the Track list keeps all registered Tag
  choices selectable and shows availability as unknown. It does not reuse a
  stale availability set.

---

## SOUND-014: Update Tag

| Field | Value |
|-------|-------|
| **Code** | SOUND-014 |
| **Version** | 26-08-09 |
| **Description** | Admin updates the name or type of an existing tag. |
| **Actor** | Admin, Backend |
| **Preconditions** | Admin logged in. Target tag exists in DB. |
| **Trigger** | Admin clicks the 'Edit' button in the tag list. |
| **Related UC** | - |

**Main Flow**
1. Admin enters the updated name and type (MOOD/GENRE/INSTRUMENT/USAGE).
2. Frontend excludes the edited Tag ID from its duplicate precheck and sends
   tagId plus the changed data.
3. Backend applies the same canonicalization, validation, and uniqueness
   contract used by create.
4. Backend updates the tag and returns the updated tag information.

**Exception / Alternative Flow**
- Unchanged self name is allowed.
- Another Tag's canonical name or an exact DB race returns 409
  `TAG_NAME_DUPLICATED`.

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
