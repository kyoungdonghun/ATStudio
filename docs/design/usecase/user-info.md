# User -- Info Use Cases

> **API Reference**: `docs/design/api-spec.md` Section 5 (User Info)
> **DB Reference**: `docs/design/db-schema.md` Section 1 (`users`, `social_accounts`)

---

## INFO-001: Register

| Field | Value |
|-------|-------|
| **Code** | INFO-001 |
| **Version** | 26-02-20 |
| **Description** | A non-member registers as a new member. |
| **Actor** | User (non-member), Backend |
| **Preconditions** | Email must not be registered. Phone number must not be registered. Nickname must not be in use. |
| **Trigger** | User clicks the 'Register' button on the login screen. |
| **Related UC** | UTIL-002 (email duplicate), UTIL-003 (phone duplicate), UTIL-012 (nickname duplicate), INFO-008 (login), INFO-013 (social login) |

**Main Flow**
1. User navigates to the registration screen.
2. User enters metadata (nickname, email, password, personal phone number, job, user type).
3. Frontend performs input validation and availability checks for email, phone number, and nickname before submit.
   - Email: UTIL-002, Phone: UTIL-003, Nickname: UTIL-012 called
4. User clicks the 'Register' button.
5. Frontend sends the input data to the backend.
6. Backend performs server-side validation and repeats the same uniqueness checks for email, phone number, and nickname.
7. Backend hashes the password with BCrypt and saves to the users table (is_verified=0).
8. Backend returns a success response (201 Created), and the frontend navigates to the login screen.

**Exception / Alternative Flow**
- Social login: see INFO-013.

**Postconditions**
- Record created in the users table. Account is ready for login.

---

## INFO-008: Login

| Field | Value |
|-------|-------|
| **Code** | INFO-008 |
| **Version** | 26-02-20 |
| **Description** | Member logs in with email/password and receives an Access Token and Refresh Token. |
| **Actor** | User (Member), Backend |
| **Preconditions** | Account registered with the email exists in the users table. is_deleted=0. |
| **Trigger** | User clicks the 'Login' button. |
| **Related UC** | UTIL-004 (reissue token), INFO-013 (social login) |

**Main Flow**
1. User enters email and password, then clicks the 'Login' button.
2. Frontend sends email and password to the backend.
3. Backend looks up the user by email and verifies the BCrypt password.
4. Backend issues an Access Token (short-lived) and Refresh Token (long-lived).
5. Backend returns token information (accessToken, refreshToken, tokenType, expiresIn).
6. Frontend stores both the Access Token and Refresh Token in browser storage.

**Exception / Alternative Flow**
- Email not found or password mismatch: 401 response.
- Withdrawn account (is_deleted=1): 403 response.

**Postconditions**
- Access Token and Refresh Token issued successfully. Can be used as Bearer token for subsequent authenticated API calls.

---

## INFO-013: Social Login [New]

| Field | Value |
|-------|-------|
| **Code** | INFO-013 |
| **Version** | 26-02-20 |
| **Description** | User logs in via a social account (Google/Kakao/Naver). On first login, a minimal record is created and the user is guided to the profile completion step (INFO-014). |
| **Actor** | User, Social OAuth Server, Backend |
| **Preconditions** | - |
| **Trigger** | User clicks a social login button (Google/Kakao/Naver). |
| **Related UC** | INFO-008 (login), INFO-014 (complete social profile) |

**Main Flow**
1. User clicks a social login button.
2. Before redirecting, frontend validates the requested post-login target as an
   internal path and stores one session-scoped attempt keyed by OAuth `state`.
   The record contains the state, PKCE verifier, validated target, and creation
   time.
3. Frontend obtains the OAuth authorization code (authorizationCode). The
   callback consumes and removes the matching attempt before provider exchange;
   missing, malformed, older-than-10-minute, or replayed attempts fail closed.
4. Frontend sends provider, a required nonblank authorizationCode, and the
   consumed PKCE codeVerifier to the backend.
5. Backend exchanges the code and reads user information through provider-specific typed response records, not a raw response map.
6. The provider access token and required provider identity are mandatory. Text fields must be nonblank; Kakao's documented integral identity is normalized, while other wrong-type required fields fail closed. Provider errors and malformed, missing, or blank required fields return `SOCIAL_AUTH_FAILED`; no local session is issued.
7. The backend does not log or retain real authorization codes, PKCE verifiers, provider access tokens, provider identifiers, or raw provider response bodies as error evidence. Tests use synthetic scalar fixtures only, never captured provider payloads or secrets.
8. Checks for an existing link in the social_accounts table by (provider, provider_id).
9. If linked: logs in as the existing user. isProfileComplete=true. Issues Access/Refresh Token and returns.
10. If not linked (new sign-up):
   a. Creates a users record with minimal information (email, social name only; password=NULL, phonePersonal=NULL, job=NULL allowed).
   b. Creates a social_accounts record and links it.
   c. Issues Access/Refresh Token.
   d. Returns response with isProfileComplete=false.
11. For a complete profile, frontend revalidates and navigates to the consumed
    internal target. For an incomplete profile, it stores a separate one-time
    profile-continuation target and navigates to INFO-014.

**Environment Boundary**
- Google, Kakao, and Naver happy paths are locally verified with typed-response tests. Real-provider payload compatibility remains `ENVIRONMENT-CONDITIONAL` until an approved environment run; no live provider call is part of this flow's evidence.

**Postconditions**
- Tokens issued.
- For new users: users (minimal info) and social_accounts records created. isProfileComplete=false.
- For existing users: isProfileComplete=true.

---

## INFO-014: Complete Social Profile [New]

| Field | Value |
|-------|-------|
| **Code** | INFO-014 |
| **Version** | 26-02-20 |
| **Description** | A member who first signed up via social login enters required profile information to complete their profile. `INDIVIDUAL` members provide job, while `BUSINESS` members provide companyName. |
| **Actor** | User (new social sign-up), Backend |
| **Preconditions** | Logged in. users record exists with isProfileComplete=false. |
| **Trigger** | After receiving isProfileComplete=false response from INFO-013, frontend automatically navigates to the profile completion screen. |
| **Related UC** | INFO-013 (social login), UTIL-003 (phone duplicate check), UTIL-012 (nickname duplicate check) |

**Main Flow**
1. Frontend displays the profile completion screen.
2. User enters nickname (required), personal phone number (required), user type (INDIVIDUAL/BUSINESS, required), and the user-type-specific profile field.
   - `INDIVIDUAL`: job (required)
   - `BUSINESS`: companyName (required)
3. Frontend performs UTIL-003 (phone duplicate) and UTIL-012 (nickname duplicate) checks before submit.
4. User clicks the 'Complete' button.
5. Frontend sends auth token and profile information to `PUT /api/users/me/complete-profile`.
6. Backend verifies isProfileComplete=false and repeats the uniqueness checks for nickname and phone number.
7. Backend updates the users record (nickname, phonePersonal, userType, and either job or companyName depending on member type).
8. Backend returns the updated user information (same format as 5.4 view my info).
9. Frontend consumes the one-time profile-continuation target and navigates to
   that revalidated internal path. Missing, stale, malformed, or replayed
   continuation data falls back to `/`.

**Exception / Alternative Flow**
- Nickname duplicate: UTIL-012 returns available=false. Prompts re-entry.
- Phone duplicate: UTIL-003 returns available=false. Prompts re-entry.
- Required profile field missing for the selected user type: backend returns `INVALID_VALID`.

**Postconditions**
- Required profile information saved in the users record. isProfileComplete=true going forward.

---

## INFO-002: View My Info

| Field | Value |
|-------|-------|
| **Code** | INFO-002 |
| **Version** | 26-02-20 |
| **Description** | Logged-in user views their own account information. |
| **Actor** | User (Member), Backend |
| **Preconditions** | Logged in. |
| **Trigger** | User navigates to the 'My Info' screen. |
| **Related UC** | INFO-005 (update my info), INFO-007 (withdraw account) |

**Main Flow**
1. Frontend sends a request including auth token to the backend.
2. Backend extracts userId from the JWT and queries the users table.
3. Backend returns user information (id, nickname, email, phonePersonal, phoneCompany, job, userType, role, isVerified, createdAt).

**Postconditions**
- My info displayed on screen.

---

## INFO-003: List Members (Admin)

| Field | Value |
|-------|-------|
| **Code** | INFO-003 |
| **Version** | 26-02-20 |
| **Description** | Admin views the full member list. |
| **Actor** | Admin, Backend |
| **Preconditions** | Admin logged in. |
| **Trigger** | Admin navigates to the member list screen. |
| **Related UC** | INFO-004 (view member detail), INFO-006 (update member info) |

**Main Flow**
1. Frontend sends criteria (keyword: nickname/email, userType, etc.) and page parameters to the backend.
2. Backend returns a paginated list of members where is_deleted=0.

**Postconditions**
- Member list matching criteria and pageInfo displayed on screen.

---

## INFO-004: View Member Detail (Admin)

| Field | Value |
|-------|-------|
| **Code** | INFO-004 |
| **Version** | 26-02-20 |
| **Description** | Admin views detailed information of a specific member. |
| **Actor** | Admin, Backend |
| **Preconditions** | Admin logged in. Target member exists in DB. |
| **Trigger** | Admin clicks a specific member in the member list. |
| **Related UC** | INFO-006 (update member info) |

**Main Flow**
1. Frontend sends a detail request including userId to the backend.
2. Backend returns the member's information.

**Postconditions**
- Member's detailed information displayed on screen.

---

## INFO-005: Update My Info

| Field | Value |
|-------|-------|
| **Code** | INFO-005 |
| **Version** | 26-02-20 |
| **Description** | Member updates their own information. Editable fields: nickname, phonePersonal, phoneCompany, job, companyName (BUSINESS only). |
| **Actor** | User (Member), Backend |
| **Preconditions** | Logged in. |
| **Trigger** | User clicks the 'Edit Info' button on the 'My Info' screen. |
| **Related UC** | UTIL-012 (nickname duplicate check) |

**Main Flow**
1. User modifies editable fields (nickname, phonePersonal, phoneCompany, job, companyName when applicable).
2. Frontend performs real-time validation.
3. If nickname is changed, performs duplicate check via UTIL-012 (nickname duplicate check).
4. User clicks the 'Save' button.
5. Frontend sends the changed data to the backend.
6. Backend resolves the effective final state by combining omitted fields with the current stored profile.
7. Backend validates the effective state against the current member type (`userType` from the authenticated user):
   - `phonePersonal` must remain present
   - `INDIVIDUAL` members must retain `job`
   - `BUSINESS` members must retain non-blank `companyName`
8. Backend updates the users record.
9. Returns and displays the updated info on screen.

**Exception / Alternative Flow**
- Nickname duplicate: UTIL-012 returns available=false.
- Effective profile state invalid for the current member type: backend returns `INVALID_ARGUMENT`.

**Postconditions**
- Updated information reflected in DB. Updated info displayed on screen.

> **Note**: email and userType are not editable. `userType` is inferred from the authenticated user, not sent by the update request.

---

## INFO-006: Update Member Info (Admin)

| Field | Value |
|-------|-------|
| **Code** | INFO-006 |
| **Version** | 26-02-20 |
| **Description** | Admin updates a specific member's role and isVerified. |
| **Actor** | Admin, Backend |
| **Preconditions** | Admin logged in. Target member exists in DB. |
| **Trigger** | Admin clicks the 'Edit' button on the member detail screen. |
| **Related UC** | INFO-004 (view member detail) |

**Main Flow**
1. Admin modifies role (USER/ADMIN) and isVerified.
2. Frontend sends userId and changed data to the backend.
3. Backend verifies authorization, updates the users record, and returns a 200 response.

**Postconditions**
- Member's role and isVerified reflected in DB.

---

## INFO-007: Withdraw Account

| Field | Value |
|-------|-------|
| **Code** | INFO-007 |
| **Version** | 26-07-13 |
| **Description** | Member withdraws their own account. The backend stops local recurring renewal before soft deletion, then cleans up Provider billing-key material after commit. Withdrawal does not create an automatic refund. |
| **Actor** | User (Member), Backend |
| **Preconditions** | Logged in. |
| **Trigger** | User clicks the 'Withdraw' button on the 'My Info' screen. |
| **Related UC** | - |

**Main Flow**
1. User clicks the 'Withdraw' button.
2. Frontend displays the withdrawal notice screen.
3. User enters their password and clicks the 'Confirm' button.
4. Frontend sends the withdrawal request including the password to the backend.
5. Backend verifies the password.
6. Backend loads the user's Toss billing agreement. A non-terminal agreement is marked `CANCELLED` locally.
7. Backend marks an ACTIVE `user_subscriptions` row `CANCELLED`. No refund is created.
8. If encrypted billing-key material exists, Backend publishes a cleanup event containing only `billingAgreementID`.
9. Backend removes the existing user-owned transient records and sets `users.is_deleted=1` (soft delete, not physical deletion).
10. After the local transaction commits, a separate cleanup transaction asks the registered Provider to remove the billing key.
11. Backend returns 204 No Content. Frontend logs out and navigates to the main screen.

**Exception / Alternative Flow**
- Password mismatch: 401 response.
- Social-only withdrawal is `POLICY-PENDING`. The existing password-only flow remains unchanged and cannot be used as a substitute for social proof. A future social-only path requires user approval of fresh provider reauthentication and linked provider-ID matching before implementation.
- Provider cleanup fails or throws: withdrawal remains complete and local renewal remains blocked. The encrypted key is retained for retry and a deduplicated `WARNING` Incident is recorded.
- Provider reports `ALREADY_REMOVED_BILLING_KEY`: cleanup converges to success, local key fields are cleared, and the matching Incident is resolved.

**Postconditions**
- `users.is_deleted=1`; login is no longer possible.
- Due-renewal selection excludes the deleted user, and a second service guard prevents key decryption, order creation, or Provider charge.
- A daily 01:15 single-server retry processes only deleted users with `CANCELLED` agreements and retained encrypted key material.
- Refund, when required, remains a separate support-approved admin workflow.

---

## INFO-015: Change Password

| Field | Value |
|-------|-------|
| **Code** | INFO-015 |
| **Version** | 26-03-08 |
| **Description** | Logged-in member changes their account password by providing the current password and a new password. |
| **Actor** | User (Member), Backend |
| **Preconditions** | Logged in. Account was registered via email/password (non-social). |
| **Trigger** | User clicks the 'Change Password' button on the personal info page (Screen 10). |
| **Related UC** | INFO-002 (view my info) |

**Main Flow**
1. User clicks the 'Change Password' button on the personal info page.
2. Frontend displays an input modal (M-01 InputModal) prompting for currentPassword and newPassword.
3. User enters the current password and the new password, then submits.
4. Frontend sends a request to `PUT /api/users/me/password` with `{ currentPassword, newPassword }` and the auth token.
5. Backend extracts userId from the JWT and retrieves the users record.
6. Backend verifies the currentPassword against the stored BCrypt hash.
7. Backend hashes the newPassword with BCrypt and updates the users record.
8. Backend returns 204 No Content.
9. Frontend closes the modal and displays a success toast.

**Request Body**
| Field | Type | Required | Description |
|-------|------|----------|-------------|
| currentPassword | String | Yes | Current account password |
| newPassword | String | Yes | New password to set |

**Exception / Alternative Flow**
- Current password mismatch: 400 Bad Request.

**Postconditions**
- Password updated in the users record. Subsequent logins require the new password.

---

## INFO-016: View Admin Dashboard Stats [New]

| Field | Value |
|-------|-------|
| **Code** | INFO-016 |
| **Version** | 26-03-14 |
| **Description** | Admin views the dashboard statistics including total users, total tracks, total subscribers, and recent user registrations. |
| **Actor** | Admin, Backend |
| **Preconditions** | Admin logged in. |
| **Trigger** | Admin navigates to the dashboard screen. |
| **Related UC** | INFO-003 (list members) |

**Main Flow**
1. Frontend sends a request to the backend. (`GET /api/admin/stats`)
2. Backend aggregates statistics:
   - `totalUsers`: COUNT of users WHERE is_deleted=false
   - `totalTracks`: COUNT of tracks WHERE is_active=true
   - `totalSubscribers`: COUNT of user_subscriptions WHERE status=ACTIVE
   - `recentUsers`: Latest 5 registered users (same format as admin user list item)
3. Backend returns the aggregated stats response.

**Postconditions**
- Dashboard statistics displayed on the admin dashboard screen.
