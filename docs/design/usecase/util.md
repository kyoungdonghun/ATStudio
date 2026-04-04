# Util — Utility Use Cases

> **API Reference**: `docs/design/api-spec.md` Section 14 (Util / Auth)
>
> **Scope of this file**: Utility APIs commonly used across other flows such as registration, login, authentication, and email verification.
> Some of the original UTIL UCs have been merged into parent UCs and are not managed as independent UCs. See the removed list below.

---

## Removed UTIL UCs

| Original Code | Title | Disposition | Reason |
|---------------|-------|-------------|--------|
| UTIL-001 | Token issuance | Merged into INFO-008 (login) | Part of the login flow. No need for independent UC. |
| UTIL-008 | License issuance | Merged into SOUND-011 (download track) | Part of download postconditions. No need for independent UC. |
| UTIL-009 | Input validation (backend) | Removed | Spring Bean Validation standard feature. Inherent in all APIs. |
| UTIL-010 | Input validation (frontend) | Removed | Frontend code level. Outside UC scope. |
| UTIL-011 | File storage | Merged into SOUND-001 (create track) | Part of the track creation flow. No need for independent UC. |

---

## UTIL-002: Check Email Duplicate

| Field | Value |
|-------|-------|
| **Code** | UTIL-002 |
| **Version** | 26-02-20 |
| **Description** | Checks whether an email is already in use during registration. |
| **Actor** | User (non-member), Backend |
| **Preconditions** | - |
| **Trigger** | User clicks the 'Check Email' button on the registration screen. |
| **Related UC** | INFO-001 (register) |

**Main Flow**
1. Frontend sends a request with the email parameter to the backend. (`GET /api/utils/check-email?email=xxx`)
2. Backend queries the users table to check if the email exists.
3. Backend returns the result. (`{ "available": true/false }`)

**Postconditions**
- available=true means the email is available. available=false means the email is already in use.

---

## UTIL-003: Check Phone Duplicate

| Field | Value |
|-------|-------|
| **Code** | UTIL-003 |
| **Version** | 26-02-20 |
| **Description** | Checks whether a phone number is already in use during registration. |
| **Actor** | User (non-member), Backend |
| **Preconditions** | - |
| **Trigger** | User clicks the 'Check Phone Number' button on the registration screen. |
| **Related UC** | INFO-001 (register) |

**Main Flow**
1. Frontend sends a request with the phone parameter to the backend. (`GET /api/utils/check-phone?phone=xxx`)
2. Backend queries the users table to check if the phone number exists.
3. Backend returns the result. (`{ "available": true/false }`)

**Postconditions**
- available=true means the phone number is available. available=false means it is already in use.

---

## UTIL-004: Reissue Token

| Field | Value |
|-------|-------|
| **Code** | UTIL-004 |
| **Version** | 26-02-20 |
| **Description** | Reissues an expired Access Token using a Refresh Token. |
| **Actor** | User (Member), Backend |
| **Preconditions** | Holds a valid Refresh Token. |
| **Trigger** | Frontend automatically sends a reissue request when the Access Token expires. |
| **Related UC** | INFO-008 (login) |

**Main Flow**
1. Frontend sends a request with the Refresh Token from the httpOnly cookie to the backend. (`POST /api/auth/refresh`)
2. Backend validates the Refresh Token (signature and expiry).
3. Backend issues a new Access Token and a new Refresh Token.
4. Backend returns the token info (accessToken, refreshToken, tokenType, expiresIn).

**Exception / Alternative Flow**
- Refresh Token expired or invalid: 401 response. Frontend logs the user out and navigates to the login screen.

**Postconditions**
- New Access Token issued. Frontend stores the new Access Token in memory.

---

## UTIL-005: Check Subscription Tier

| Field | Value |
|-------|-------|
| **Code** | UTIL-005 |
| **Version** | 26-02-20 |
| **Description** | Checks the current subscription tier and benefit info of a logged-in member. Used for permission checks before track download or channel registration. |
| **Actor** | User (Member), Backend |
| **Preconditions** | Logged in. |
| **Trigger** | Frontend automatically calls this when entering a feature that requires permission check. |
| **Related UC** | SOUND-011 (download track), WL-001 (register channel) |

**Main Flow**
1. Frontend sends a request with the auth token to the backend. (`GET /api/utils/subscription-status`)
2. Backend queries the user's active subscription info.
3. Backend returns the subscription info (hasSubscription, planName, userType, downloadPerDay, maxWhitelistChannels).

**Postconditions**
- Subscription tier and benefit info returned. If no subscription exists, hasSubscription=false.

---

## UTIL-006: Check Download Count

| Field | Value |
|-------|-------|
| **Code** | UTIL-006 |
| **Version** | 26-02-20 |
| **Description** | Checks the logged-in member's today's download count and remaining downloads. |
| **Actor** | User (Member), Backend |
| **Preconditions** | Logged in. |
| **Trigger** | Frontend automatically calls this before a download or when entering the download screen. |
| **Related UC** | SOUND-011 (download track) |

**Main Flow**
1. Frontend sends a request with the auth token to the backend. (`GET /api/utils/download-count`)
2. Backend counts today's downloads in the track_downloads table.
   (`COUNT(*) WHERE user_id=? AND DATE(downloaded_at)=CURDATE()`)
3. Backend returns the result (todayDownloads, dailyLimit, remaining, nextResetAt).
   - `nextResetAt`: tomorrow 00:00:00 as LocalDateTime (the point at which the daily count resets).

**Postconditions**
- Today's download count info returned. Frontend uses `remaining` to decide whether to enable the download button. Frontend may display `nextResetAt` as a countdown or reset time indicator.

---

## UTIL-007: Check Member Type

| Field | Value |
|-------|-------|
| **Code** | UTIL-007 |
| **Version** | 26-02-20 |
| **Description** | Checks the logged-in member's user type (userType) and job. |
| **Actor** | User (Member), Backend |
| **Preconditions** | Logged in. |
| **Trigger** | Frontend calls this when it needs to render different UI depending on the member type. |
| **Related UC** | CC-001 (apply for company certification), PAYMENT-001 (subscribe) |

**Main Flow**
1. Frontend sends a request with the auth token to the backend. (`GET /api/utils/user-type`)
2. Backend extracts userId from the JWT and queries userType and job from the users table.
3. Backend returns the result (userType, job).

**Postconditions**
- userType (INDIVIDUAL/BUSINESS) and job info returned.

---

## UTIL-012: Check Nickname Duplicate [New]

| Field | Value |
|-------|-------|
| **Code** | UTIL-012 |
| **Version** | 26-02-20 |
| **Description** | Checks whether a nickname is already in use during registration or profile update. |
| **Actor** | User, Backend |
| **Preconditions** | - |
| **Trigger** | User clicks the 'Check Nickname' button on the registration or profile update screen. |
| **Related UC** | INFO-001 (register), INFO-005 (update my info) |

**Main Flow**
1. Frontend sends a request with the nickname parameter to the backend. (`GET /api/utils/check-nickname?nickname=xxx`)
2. Backend queries the users table to check if the nickname exists.
3. Backend returns the result. (`{ "available": true/false }`)

**Postconditions**
- available=true means the nickname is available. available=false means the nickname is already in use.

---

## UTIL-013: Subscription Change Preview [New]

| Field | Value |
|-------|-------|
| **Code** | UTIL-013 |
| **Version** | 26-03-07 |
| **Description** | Returns a preview of the financial and scheduling impact before the member confirms a subscription plan change. Used by the frontend to display proratedAmount (UPGRADE) or effectiveDate (DOWNGRADE) before initiating payment. |
| **Actor** | User (subscriber), Backend |
| **Preconditions** | Logged in. Has active subscription (user_subscriptions.status=ACTIVE). |
| **Trigger** | Frontend calls this automatically when the user selects a new plan on the subscription change screen. |
| **Related UC** | PAYMENT-007 (change my subscription) |

**Main Flow**
1. Frontend sends a request with subscriptionId (target new plan) and billingCycle (MONTHLY/YEARLY) as query parameters. (`GET /api/utils/subscription-change-preview?subscriptionId=X&billingCycle=Y`)
2. Backend retrieves the current user_subscriptions record and the target subscription plan.
3. Backend determines change type by comparing plan prices:
   - If new plan daily rate > current plan daily rate → changeType=UPGRADE
   - Otherwise → changeType=DOWNGRADE
4. For UPGRADE: Backend calculates proratedAmount = (newDailyRate - oldDailyRate) × remainingDays.
   For DOWNGRADE: proratedAmount = 0. effectiveDate = current expiresAt.
5. Backend returns the preview response.

**Response Fields**
- `changeType`: UPGRADE or DOWNGRADE
- `proratedAmount`: Amount to be charged now (UPGRADE) or 0 (DOWNGRADE)
- `effectiveDate`: Date the new plan becomes active (UPGRADE: today, DOWNGRADE: current expiresAt)
- `newPlanName`: Display name of the target plan
- `newBillingCycle`: Billing cycle selected (MONTHLY/YEARLY)

**Exception / Alternative Flow**
- No active subscription: 404 `SUBSCRIPTION_NOT_FOUND`.
- Target plan not found: 404 `SUBSCRIPTION_NOT_FOUND`.

**Postconditions**
- Preview data returned. No DB state changes. Frontend uses this to render the confirmation screen before the user initiates the actual change (PAYMENT-007).

---

## UTIL-014: Verify Email [New]

| Field | Value |
|-------|-------|
| **Code** | UTIL-014 |
| **Version** | 26-03-14 |
| **Description** | Verifies a user's email address via a token sent by email. The user clicks the verification link in the email, and the frontend automatically calls this API with the token from the URL query parameter. |
| **Actor** | User (non-member or member), Backend |
| **Preconditions** | User has registered and received a verification email containing a UUID token link. Token is valid (not expired, not already used). |
| **Trigger** | User clicks the email verification link. Frontend page mounts and reads `?token=` from URL. |
| **Related UC** | INFO-001 (register) |

**Main Flow**
1. User clicks the verification link in their email (e.g., `https://host/email-verify?token=UUID`).
2. Frontend extracts the `token` query parameter and sends a request to the backend. (`GET /api/auth/verify-email?token=xxx`)
3. Backend looks up the `email_verification_tokens` record by token.
4. Backend validates: token exists, not used (`used=false`), not expired (`expires_at > now`).
5. Backend marks the token as used (`used=true`).
6. Backend sets `users.is_verified = true` for the associated user.
7. Backend returns 200 OK with success message.

**Exception / Alternative Flow**
- Token not found or already used: 400 `INVALID_TOKEN`.
- Token expired (24 hours): 401 `TOKEN_EXPIRED`.

**Postconditions**
- `users.is_verified = true`. Token marked as used. User can now log in with full verified status.

---

## UTIL-015: Request Password Reset [New]

| Field | Value |
|-------|-------|
| **Code** | UTIL-015 |
| **Version** | 26-03-14 |
| **Description** | Sends a password reset email to the specified address. Always returns 200 regardless of whether the email exists in the system (prevents account enumeration). |
| **Actor** | User (any), Backend |
| **Preconditions** | - |
| **Trigger** | User clicks 'Forgot Password' on the login screen and submits their email. |
| **Related UC** | INFO-008 (login), UTIL-016 (reset password) |

**Main Flow**
1. User enters their email on the password reset request screen.
2. Frontend sends a request with the email to the backend. (`POST /api/auth/forgot-password`)
3. Backend looks up the user by email.
4. If user exists: Backend deletes any existing password_reset_tokens for this user, generates a new UUID token (1-hour expiry), saves it, and sends a password reset email with a link (e.g., `https://host/password-reset?token=UUID`).
5. If user does NOT exist: Backend does nothing (no email sent).
6. Backend always returns 200 OK with "비밀번호 재설정 이메일이 발송되었습니다." (regardless of email existence).

**Exception / Alternative Flow**
- SMTP failure: Backend logs the email content to console as fallback (development environment).

**Postconditions**
- If user exists: `password_reset_tokens` record created. Email sent (or console fallback). No user state changes yet.
- If user does not exist: No action taken. Same 200 response returned (security: prevents account enumeration).

---

## UTIL-016: Reset Password [New]

| Field | Value |
|-------|-------|
| **Code** | UTIL-016 |
| **Version** | 26-03-14 |
| **Description** | Resets the user's password using a token from a password reset email. Token is single-use, valid for 1 hour. |
| **Actor** | User (any), Backend |
| **Preconditions** | User has received a password reset email and clicked the link. Token is valid (not expired, not already used). |
| **Trigger** | User clicks the password reset link in their email, enters a new password on the reset form, and submits. |
| **Related UC** | UTIL-015 (request password reset), INFO-008 (login) |

**Main Flow**
1. User clicks the reset link in their email (e.g., `https://host/password-reset?token=UUID`).
2. Frontend displays the new password form (password + confirmation).
3. User enters and confirms the new password (minimum 8 characters).
4. Frontend sends a request with token and newPassword to the backend. (`POST /api/auth/reset-password`)
5. Backend looks up the `password_reset_tokens` record by token.
6. Backend validates: token exists, not used (`used=false`), not expired (`expires_at > now`).
7. Backend marks the token as used (`used=true`).
8. Backend encodes the new password (BCrypt) and updates `users.password`.
9. Backend returns 200 OK with success message.

**Exception / Alternative Flow**
- Token not found or already used: 400 `INVALID_TOKEN`.
- Token expired (1 hour): 401 `TOKEN_EXPIRED`.

**Postconditions**
- `users.password` updated with new BCrypt hash. Token marked as used. User can log in with the new password.

---

## UTIL-017: Get Site Setting [New]

| Field | Value |
|-------|-------|
| **Code** | UTIL-017 |
| **Version** | 26-04-04 |
| **Description** | Retrieves a single site configuration value by key. Used by frontend to load dynamic content without code redeployment (e.g., company certification guide text). |
| **Actor** | User (any), Backend |
| **Preconditions** | - |
| **Trigger** | Frontend page loads and requires dynamic content (e.g., CompanyCertApplyPage mounts). |
| **Related UC** | UTIL-018 (update site setting), CC-001 (company certification) |

**Main Flow**
1. Frontend sends `GET /api/settings/{key}` with the desired setting key.
2. Backend looks up the `site_settings` record by `setting_key`.
3. Backend returns the key and value.

**Exception / Alternative Flow**
- Key not found: 404 `RESOURCE_NOT_FOUND`.

**Postconditions**
- No state changes. Setting value returned to frontend.

---

## UTIL-018: Update Site Setting (Admin) [New]

| Field | Value |
|-------|-------|
| **Code** | UTIL-018 |
| **Version** | 26-04-04 |
| **Description** | Admin creates or updates a site configuration value by key (upsert). Allows changing dynamic content (guide texts, notices) without code deployment. |
| **Actor** | Admin, Backend |
| **Preconditions** | Logged in as ADMIN. |
| **Trigger** | Admin navigates to site settings management page and submits updated content. |
| **Related UC** | UTIL-017 (get site setting) |

**Main Flow**
1. Admin enters the setting key and new value.
2. Frontend sends `PUT /api/admin/settings/{key}` with the value in the request body.
3. Backend performs upsert: creates a new `site_settings` record if key does not exist, otherwise updates the existing value.
4. Backend returns the updated key-value pair.

**Exception / Alternative Flow**
- Value is blank or exceeds max length (5000 chars): 400 `INVALID_ARGUMENT`.
- Not authenticated as ADMIN: 403 Forbidden.

**Postconditions**
- `site_settings` record created or updated. Change takes effect immediately on next frontend fetch.
