# User -- License Use Cases

> **API Reference**: `docs/design/api-spec.md` Section 7 (License)
> **DB Reference**: `docs/design/db-schema.md` Section 11 (`licenses`)
>
> **License type distinction**:
> - **Track usage license** (this file): `licenses` table. Automatically issued on download. UUID-based.
> - **Business review license**: `business_license_requests` table. See `business-license.md`.

---

## INFO-009: View My License List

| Field | Value |
|-------|-------|
| **Code** | INFO-009 |
| **Version** | 26-02-20 |
| **Description** | Logged-in member views their license list for downloaded tracks. |
| **Actor** | User (Member), Backend |
| **Preconditions** | Logged in. |
| **Trigger** | User navigates to the 'My Licenses' screen. |
| **Related UC** | INFO-011 (license detail), SOUND-011 (download track) |

**Main Flow**
1. Frontend sends a request including auth token and page parameters to the backend.
2. Backend extracts userId from JWT and queries the licenses table for the user's license list.
3. Backend returns the license list (id, track info, licenseCode, issuedAt) paginated.

**Postconditions**
- License list displayed on screen. Pagination works correctly.

> **Modification note**: Original precondition "must have active subscription" removed. Previously issued licenses remain viewable even after subscription expires.

---

## INFO-010: View Member License List (Admin)

| Field | Value |
|-------|-------|
| **Code** | INFO-010 |
| **Version** | 26-02-20 |
| **Description** | Admin views the license list of a specific member. |
| **Actor** | Admin, Backend |
| **Preconditions** | Admin logged in. Target member exists in DB. |
| **Trigger** | Admin clicks the 'License List' button on the member detail screen. |
| **Related UC** | INFO-012 (license detail) |

**Main Flow**
1. Frontend sends a request including userId and page parameters to the backend.
2. Backend queries and returns the member's license list.

**Postconditions**
- Member's license list displayed on screen.

---

## INFO-011: View My License Detail

| Field | Value |
|-------|-------|
| **Code** | INFO-011 |
| **Version** | 26-02-20 |
| **Description** | Logged-in member views the detail of a specific license they own. |
| **Actor** | User (Member), Backend |
| **Preconditions** | Logged in. Target license must belong to the user. |
| **Trigger** | User clicks a specific license in the license list. |
| **Related UC** | INFO-009 (license list) |

**Main Flow**
1. Frontend sends a detail request including licenseId to the backend.
2. Backend verifies the license belongs to the user.
3. Backend returns the license detail (id, track info, licenseCode, issuedAt, user info).

**Exception / Alternative Flow**
- Accessing another user's license: 403 response.

**Postconditions**
- License detail displayed on screen.

> **Modification note**: Original code typo `IFNO-011` corrected to `INFO-011`.

---

## INFO-012: View Member License Detail (Admin)

| Field | Value |
|-------|-------|
| **Code** | INFO-012 |
| **Version** | 26-02-20 |
| **Description** | Admin views the detail of a specific license of a specific member. |
| **Actor** | Admin, Backend |
| **Preconditions** | Admin logged in. |
| **Trigger** | Admin clicks a specific license in the member's license list. |
| **Related UC** | INFO-010 (license list) |

**Main Flow**
1. Frontend sends a request including userId and licenseId to the backend.
2. Backend returns the license detail information.

**Postconditions**
- License detail (including licenseCode) displayed on screen.
