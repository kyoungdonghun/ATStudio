# User -- Subscription Use Cases

> **API Reference**: `docs/design/api-spec.md` Section 6 (Subscription)
> **DB Reference**: `docs/design/db-schema.md` Section 2 (`subscriptions`, `user_subscriptions`, `subscription_payments`)

---

## PAYMENT-001: Subscribe

| Field | Value |
|-------|-------|
| **Code** | PAYMENT-001 |
| **Version** | 26-02-20 |
| **Description** | Member selects a subscription plan and pays to start a subscription. BUSINESS type members can only subscribe after company certification review approval. |
| **Actor** | User (Member), Backend, Payment Gateway (PG) |
| **Preconditions** | Logged in. At least one subscription plan exists in DB. If BUSINESS type: CC-001~005 review approved (company_certifications.status=APPROVED). |
| **Trigger** | User clicks the 'Subscribe' button on a specific plan in the subscription plan list screen. |
| **Related UC** | PAYMENT-002 (list plans), PAYMENT-006 (view my subscription), CC-001 (apply for company certification) |

**Main Flow**
1. User selects a subscription plan and billing cycle (MONTHLY/YEARLY).
2. Frontend displays the payment screen and initiates PG payment.
3. After payment completion, frontend sends a subscription request including subscriptionId and billingCycle to the backend.
4. Backend verifies member type and company certification approval status.
5. Backend creates a user_subscriptions record (status=ACTIVE, started_at, expires_at set).
6. Backend saves the payment record in subscription_payments.
7. Backend returns a success response (201 Created).

**Exception / Alternative Flow**
- BUSINESS type member without certification approval: 403 `COMPANY_CERTIFICATION_REQUIRED`.

**Postconditions**
- user_subscriptions record created (status=ACTIVE). Payment record saved in subscription_payments.

> **Note**: Upon subscription completion, track usage licenses (licenses table) are NOT issued. Licenses are automatically issued at download time.

---

## PAYMENT-002: List Subscription Plans

| Field | Value |
|-------|-------|
| **Code** | PAYMENT-002 |
| **Version** | 26-02-20 |
| **Description** | User (including non-members) views the subscription plan list. |
| **Actor** | User (including non-members), Backend |
| **Preconditions** | - |
| **Trigger** | User navigates to the subscription plan list screen. |
| **Related UC** | PAYMENT-003 (plan detail) |

**Main Flow**
1. Frontend sends a request with optional userType parameter to the backend.
2. Backend returns the list of subscription plans where is_active=1.

**Postconditions**
- Subscription plan list (name, userType, priceMonthly, priceYearly, downloadPerDay, maxWhitelistChannels) displayed on screen.

---

## PAYMENT-003: View Subscription Plan Detail

| Field | Value |
|-------|-------|
| **Code** | PAYMENT-003 |
| **Version** | 26-02-20 |
| **Description** | User (including non-members) views detailed information of a specific subscription plan. |
| **Actor** | User (including non-members), Backend |
| **Preconditions** | Target subscription plan exists in DB. |
| **Trigger** | User clicks a specific subscription plan. |
| **Related UC** | PAYMENT-001 (subscribe) |

**Main Flow**
1. Frontend sends a request including subscriptionId to the backend.
2. Backend returns the subscription plan detail information.

**Postconditions**
- Subscription plan detail displayed on screen.

---

## PAYMENT-004: List Member Subscriptions (Admin)

| Field | Value |
|-------|-------|
| **Code** | PAYMENT-004 |
| **Version** | 26-02-20 |
| **Description** | Admin views the subscription status list of all members. |
| **Actor** | Admin, Backend |
| **Preconditions** | Admin logged in. |
| **Trigger** | Admin navigates to the subscription management screen. |
| **Related UC** | PAYMENT-005 (view detail), PAYMENT-008 (update), PAYMENT-009 (delete) |

**Main Flow**
1. Frontend sends a request including page parameters to the backend.
2. Backend returns the full user_subscriptions list paginated.

**Postconditions**
- Full subscription status list and pageInfo displayed on screen.

---

## PAYMENT-005: View Member Subscription Detail (Admin)

| Field | Value |
|-------|-------|
| **Code** | PAYMENT-005 |
| **Version** | 26-02-20 |
| **Description** | Admin views detailed subscription information of a specific member. |
| **Actor** | Admin, Backend |
| **Preconditions** | Admin logged in. |
| **Trigger** | Admin clicks a specific record in the subscription list. |
| **Related UC** | PAYMENT-008 (update), PAYMENT-009 (delete) |

**Main Flow**
1. Frontend sends a request including userSubscriptionId to the backend.
2. Backend returns the subscription detail information.

**Postconditions**
- Subscription detail (plan, billing cycle, status, period) displayed on screen.

---

## PAYMENT-006: View My Subscription

| Field | Value |
|-------|-------|
| **Code** | PAYMENT-006 |
| **Version** | 26-02-20 |
| **Description** | Logged-in member views their current subscription status. |
| **Actor** | User (Member), Backend |
| **Preconditions** | Logged in. |
| **Trigger** | User navigates to the 'My Subscription' screen. |
| **Related UC** | PAYMENT-007 (change subscription) |

**Main Flow**
1. Frontend sends a request including auth token to the backend.
2. Backend returns the user's user_subscriptions record.

**Postconditions**
- Current subscription plan, billing cycle, status, expiration date, etc. displayed on screen. Returns null if no subscription.

---

## PAYMENT-007: Change My Subscription (Upgrade/Downgrade)

| Field | Value |
|-------|-------|
| **Code** | PAYMENT-007 |
| **Version** | 26-02-20 |
| **Description** | Member changes their current subscription plan to a different plan. Applied immediately with prorated amount payment. |
| **Actor** | User (Member), Backend, Payment Gateway (PG) |
| **Preconditions** | Logged in. Has active subscription (user_subscriptions.status=ACTIVE). |
| **Trigger** | User clicks the 'Change Subscription' button. |
| **Related UC** | PAYMENT-006 (view my subscription) |

**Main Flow**
1. User selects the new subscription plan and billing cycle.
2. The prorated amount is calculated and displayed on screen (proratedAmount).
3. Frontend initiates PG payment.
4. After payment completion, frontend sends a change request including subscriptionId and billingCycle to the backend.
5. Backend immediately updates user_subscriptions (new plan, new cycle, new expiration date).
6. Backend saves the prorated payment record in subscription_payments.
7. Backend returns the updated subscription information (including proratedAmount).

**Postconditions**
- user_subscriptions updated. Payment record saved in subscription_payments.
- Changed plan services (downloadPerDay, maxWhitelistChannels) applied immediately.

> **Note**: Subscription changes do NOT affect track usage licenses (licenses table). Previously issued licenses are retained as-is.

---

## PAYMENT-008: Update Member Subscription (Admin)

| Field | Value |
|-------|-------|
| **Code** | PAYMENT-008 |
| **Version** | 26-02-20 |
| **Description** | Admin updates a specific member's subscription information. |
| **Actor** | Admin, Backend |
| **Preconditions** | Admin logged in. Target subscription record exists. |
| **Trigger** | Admin clicks the 'Edit' button on the subscription detail screen. |
| **Related UC** | PAYMENT-005 (view detail) |

**Main Flow**
1. Admin modifies the subscription information and submits.
2. Frontend sends userSubscriptionId and changed data to the backend.
3. Backend verifies authorization, updates the user_subscriptions record, and returns a 200 response.

**Postconditions**
- Subscription information reflected in DB.

---

## PAYMENT-009: Delete/Cancel Member Subscription (Admin)

| Field | Value |
|-------|-------|
| **Code** | PAYMENT-009 |
| **Version** | 26-02-20 |
| **Description** | Admin deletes (cancels) a specific member's subscription. |
| **Actor** | Admin, Backend |
| **Preconditions** | Admin logged in. Target subscription record exists. |
| **Trigger** | Admin clicks the 'Delete' button on the subscription detail screen. |
| **Related UC** | PAYMENT-005 (view detail) |

**Main Flow**
1. Admin clicks the 'Delete' button and confirms.
2. Frontend sends a delete request including userSubscriptionId to the backend.
3. Backend verifies authorization, deletes the user_subscriptions record (or sets status=CANCELLED), and returns 204 No Content.

**Postconditions**
- Member's subscription cancelled.

---

## PAYMENT-010: Cancel My Subscription [New]

| Field | Value |
|-------|-------|
| **Code** | PAYMENT-010 |
| **Version** | 26-02-20 |
| **Description** | Member directly cancels their own active subscription. Immediate cancellation (status=CANCELLED). |
| **Actor** | User (Member), Backend |
| **Preconditions** | Logged in. Has active subscription (user_subscriptions.status=ACTIVE). |
| **Trigger** | User clicks the 'Cancel Subscription' button on the 'My Subscription' screen. |
| **Related UC** | PAYMENT-006 (view my subscription) |

**Main Flow**
1. User clicks the 'Cancel Subscription' button.
2. Frontend displays the cancellation notice (service restriction advisory after cancellation).
3. User clicks the 'Confirm' button.
4. Frontend sends a cancellation request including auth token to the backend. (`DELETE /api/user-subscriptions/me`)
5. Backend checks for an active subscription.
6. Backend updates user_subscriptions.status to CANCELLED and returns 204 No Content.

**Exception / Alternative Flow**
- No active subscription: 404 `SUBSCRIPTION_NOT_FOUND`.

**Postconditions**
- user_subscriptions.status=CANCELLED updated. Subscription benefits (downloads, channel registration, playlists) no longer available.
