# Util — Utility Use Cases

> **API Reference**: `docs/design/api-spec.md` Section 14 (Util)
>
> **Scope of this file**: Utility APIs commonly used across other flows such as registration, login, and authentication.
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
