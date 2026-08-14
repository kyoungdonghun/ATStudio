# User -- License Use Cases

> **API Reference**: `docs/design/api-spec.md` Section 7 (License)
> **DB Reference**: `docs/design/db-schema.md` Section 11 (`licenses`)
>
> **License type distinction**:
>
> - **Track usage license** (this file): `licenses` table. Automatically issued on download. UUID-based.
> - **Company certification**: `company_certifications` table. See `company-certification.md`.

---

## INFO-009: View My License List

| Field             | Value                                                            |
| ----------------- | ---------------------------------------------------------------- |
| **Code**          | INFO-009                                                         |
| **Version**       | 26-08-14                                                         |
| **Description**   | Logged-in member views their license list for downloaded tracks. |
| **Actor**         | User (Member), Backend                                           |
| **Preconditions** | Logged in.                                                       |
| **Trigger**       | User navigates to the 'My Licenses' screen.                      |
| **Related UC**    | INFO-011 (license detail), SOUND-011 (download track)            |

**Main Flow**

1. Frontend sends a request including auth token and page parameters to the backend.
2. Backend extracts userId from JWT and queries the licenses table for the user's license list.
3. Backend returns the license list (id, track info, licenseCode, issuedAt) paginated.
4. Page, route, or authenticated-owner changes clear and retire the prior
   request. Rows, pagination, detail dialog, license values, and controls
   render only for the current owner-and-page projection; detached controls
   revalidate it before acting.
5. A re-download synchronously claims the current list projection and Track
   identity before requesting the binary. The list-row and detail-dialog action
   for that identity share the pending claim, so a rapid repeat invokes at most
   one Track request until it settles; the exact claim release then permits a
   retry. The binary result uses the server filename and content type when
   valid, and Track failures use the canonical Blob-aware API error code path.

**Postconditions**

- License list displayed on screen. Pagination works correctly.
- A browser download is triggered only from the validated non-empty binary
  result; that client action is not a durable server assertion of completed
  byte delivery.

> **Modification note**: Original precondition "must have active subscription" removed. Previously issued licenses remain viewable even after subscription expires.

---

## INFO-010: View Member License List (Admin)

| Field             | Value                                                               |
| ----------------- | ------------------------------------------------------------------- |
| **Code**          | INFO-010                                                            |
| **Version**       | 26-08-14                                                            |
| **Description**   | Admin views the license list of a specific member.                  |
| **Actor**         | Admin, Backend                                                      |
| **Preconditions** | Admin logged in. Target member exists in DB.                        |
| **Trigger**       | Admin clicks the 'License List' button on the member detail screen. |
| **Related UC**    | INFO-012 (license detail)                                           |

**Main Flow**

1. User search publication is owned by its request generation, normalized
   submitted keyword, current input, and current URL context. Input edits,
   canonical User selection, URL/User context changes, and unmount abort and
   retire pending search work; a retired response cannot publish rows,
   dropdown, loading, or error state.
2. The positive `userId` in the URL is the canonical selected-member key.
3. Frontend requests both `GET /api/users/{userId}` for the visible canonical
   member identity and the paginated license list for that exact user and page.
4. A user, page, or route change aborts and retires the prior context, clears
   prior identity/list state, and starts one newer context.
5. Only the current context may publish identity, rows, pageInfo, loading, or
   error state. A late User A response cannot overwrite User B.
6. Backend enforces ADMIN authority and returns the member detail and licenses.

**Postconditions**

- The deep-linked member's canonical visible identity and license list are
  displayed for the same latest-owned context.

---

## INFO-011: View My License Detail

| Field             | Value                                                             |
| ----------------- | ----------------------------------------------------------------- |
| **Code**          | INFO-011                                                          |
| **Version**       | 26-02-20                                                          |
| **Description**   | Logged-in member views the detail of a specific license they own. |
| **Actor**         | User (Member), Backend                                            |
| **Preconditions** | Logged in. Target license must belong to the user.                |
| **Trigger**       | User clicks a specific license in the license list.               |
| **Related UC**    | INFO-009 (license list)                                           |

**Main Flow**

1. Frontend sends a detail request including licenseId to the backend.
2. Backend verifies the license belongs to the user.
3. Backend returns the license detail (id, track info, licenseCode, issuedAt, user info).

**Exception / Alternative Flow**

- Accessing another user's license: 403 response.
- Detail accepts only a canonical ASCII decimal `licenseId` matching
  `[1-9][0-9]*` and a safe integer. Invalid values render fixed Korean list
  recovery with no request; route or owner changes retire and hide the prior
  projection and its completions.

**Postconditions**

- License detail displayed on screen.

> **Modification note**: Original code typo `IFNO-011` corrected to `INFO-011`.

---

## INFO-012: View Member License Detail (Admin)

| Field             | Value                                                              |
| ----------------- | ------------------------------------------------------------------ |
| **Code**          | INFO-012                                                           |
| **Version**       | 26-02-20                                                           |
| **Description**   | Admin views the detail of a specific license of a specific member. |
| **Actor**         | Admin, Backend                                                     |
| **Preconditions** | Admin logged in.                                                   |
| **Trigger**       | Admin clicks a specific license in the member's license list.      |
| **Related UC**    | INFO-010 (license list)                                            |

**Main Flow**

1. Frontend sends a request including userId and licenseId to the backend.
2. Backend returns the license detail information.

**Postconditions**

- License detail (including licenseCode) displayed on screen.
