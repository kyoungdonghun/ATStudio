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
3. Frontend performs real-time input validation.
4. User clicks the 'Check Duplicate' button for email, phone number, and nickname respectively.
   - Email: UTIL-002, Phone: UTIL-003, Nickname: UTIL-012 called
5. Once all duplicate checks pass, the 'Register' button is enabled.
6. User clicks the 'Register' button.
7. Frontend sends the input data to the backend.
8. Backend performs server-side validation.
9. Backend hashes the password with BCrypt and saves to the users table (is_verified=0).
10. Backend returns a success response (201 Created), and the frontend navigates to the login screen.

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
6. Frontend stores the Access Token in memory and the Refresh Token in an httpOnly cookie.

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
2. Frontend obtains the OAuth authorization code (authorizationCode).
3. Frontend sends provider and authorizationCode to the backend.
4. Backend retrieves user information (email, name, etc.) from the social server.
5. Checks for an existing link in the social_accounts table by (provider, provider_id).
6. If linked: logs in as the existing user. isProfileComplete=true. Issues Access/Refresh Token and returns.
7. If not linked (new sign-up):
   a. Creates a users record with minimal information (email, social name only; password=NULL, phonePersonal=NULL, job=NULL allowed).
   b. Creates a social_accounts record and links it.
   c. Issues Access/Refresh Token.
   d. Returns response with isProfileComplete=false.
8. Frontend checks isProfileComplete=false and navigates to the INFO-014 (complete social profile) screen.

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
| **Description** | A member who first signed up via social login enters required profile information (nickname, phone number, job, user type) to complete their profile. |
| **Actor** | User (new social sign-up), Backend |
| **Preconditions** | Logged in. users record exists with isProfileComplete=false. |
| **Trigger** | After receiving isProfileComplete=false response from INFO-013, frontend automatically navigates to the profile completion screen. |
| **Related UC** | INFO-013 (social login), UTIL-003 (phone duplicate check), UTIL-012 (nickname duplicate check) |

**Main Flow**
1. Frontend displays the profile completion screen.
2. User enters nickname (required), personal phone number (required), job (required), and user type (INDIVIDUAL/BUSINESS, required).
3. Frontend performs UTIL-003 (phone duplicate) and UTIL-012 (nickname duplicate) checks.
4. User clicks the 'Complete' button.
5. Frontend sends auth token and profile information to `PUT /api/users/me/complete-profile`.
6. Backend verifies isProfileComplete=false.
7. Backend updates the users record (nickname, phonePersonal, job, userType).
8. Backend returns the updated user information (same format as 5.4 view my info).
9. Frontend navigates to the main screen.

**Exception / Alternative Flow**
- Nickname duplicate: UTIL-012 returns available=false. Prompts re-entry.
- Phone duplicate: UTIL-003 returns available=false. Prompts re-entry.

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
| **Description** | Member updates their own information. Editable fields: nickname, phonePersonal, phoneCompany, job. |
| **Actor** | User (Member), Backend |
| **Preconditions** | Logged in. |
| **Trigger** | User clicks the 'Edit Info' button on the 'My Info' screen. |
| **Related UC** | UTIL-012 (nickname duplicate check) |

**Main Flow**
1. User modifies editable fields (nickname, phonePersonal, phoneCompany, job).
2. Frontend performs real-time validation.
3. If nickname is changed, performs duplicate check via UTIL-012 (nickname duplicate check).
4. User clicks the 'Save' button.
5. Frontend sends the changed data to the backend.
6. Backend performs validation and updates the users record.
7. Returns and displays the updated info on screen.

**Exception / Alternative Flow**
- Nickname duplicate: UTIL-012 returns available=false.

**Postconditions**
- Updated information reflected in DB. Updated info displayed on screen.

> **Note**: email and userType are not editable.

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
| **Version** | 26-02-20 |
| **Description** | Member withdraws their own account. Soft delete (is_deleted=1). |
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
6. Backend sets users.is_deleted=1. (Soft delete. Not a physical deletion.)
7. Backend returns 204 No Content. Frontend logs out and navigates to the main screen.

**Exception / Alternative Flow**
- Password mismatch: 401 response.

**Postconditions**
- users.is_deleted=1 updated. Login with this account is no longer possible.
