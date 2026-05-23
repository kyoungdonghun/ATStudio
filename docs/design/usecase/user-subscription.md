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
2. Frontend displays the recurring subscription payment screen and initiates Toss billing auth.
3. After billing auth success, frontend sends `orderId`, `authKey`, `customerKey`, and amount to the backend billing agreement confirm API.
4. Backend verifies member type, company certification approval status, order ownership, amount, and billing agreement state.
5. Backend issues/stores the billing key server-side, performs the first charge, then creates or updates `user_subscriptions` only after charge success.
6. Backend saves the payment record in `subscription_payments`.
7. Backend returns a success response.

**Exception / Alternative Flow**
- BUSINESS type member without certification approval: 403 `COMPANY_CERTIFICATION_REQUIRED`.
- Stale clients calling `POST /api/user-subscriptions` directly: 410 `SUBSCRIPTION_CHECKOUT_REQUIRED`. Subscription mutation is only allowed after recurring billing checkout confirmation and first charge success.

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

## PAYMENT-002A: List All Subscription Plans (Admin) [New]

| Field | Value |
|-------|-------|
| **Code** | PAYMENT-002A |
| **Version** | 26-03-14 |
| **Description** | Admin views the full subscription plan list including inactive (is_active=0) plans. Unlike PAYMENT-002 which only returns active plans, this endpoint returns all plans for management purposes. |
| **Actor** | Admin, Backend |
| **Preconditions** | Admin logged in. |
| **Trigger** | Admin navigates to the subscription plan management screen. |
| **Related UC** | PAYMENT-002 (public list) |

**Main Flow**
1. Frontend sends a request to the backend. (`GET /api/subscriptions/admin`)
2. Backend returns the full list of subscription plans (both active and inactive).

**Postconditions**
- Full subscription plan list displayed, including is_active field for each plan.

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
2. Backend returns the user's current `user_subscriptions` record when the subscription is service-enabled.
   - Service-enabled means `status=ACTIVE`, or `status=CANCELLED` while `expiresAt` is still in the future (grace period).

**Exception / Alternative Flow**
- No active / grace-period subscription: backend returns 403 `NO_ACTIVE_SUBSCRIPTION`. The frontend may interpret this as "no current subscription" and show a CTA back to the subscription plan page.

**Postconditions**
- Current subscription plan, billing cycle, status, expiration date, etc. displayed on screen when a service-enabled subscription exists.

---

## PAYMENT-007: Change My Subscription

| Field | Value |
|-------|-------|
| **Code** | PAYMENT-007 |
| **Version** | 26-05-22 |
| **Description** | Member changes their current subscription plan or billing cycle. UPGRADE is applied immediately after billing-key prorated charge success. Lower-tier and billing-cycle-only changes are deferred to the end of the current billing period. Selecting the current plan/current cycle clears a pending change. |
| **Actor** | User (Member), Backend, Payment Gateway (PG) |
| **Preconditions** | Logged in. Has active subscription, or a CANCELLED grace-period subscription before expiresAt. |
| **Trigger** | User clicks the 'Change Subscription' button. |
| **Related UC** | PAYMENT-006 (view my subscription), UTIL-013 (subscription change preview) |

**Main Flow — UPGRADE (new plan price > current plan price)**
1. User selects the new subscription plan and billing cycle.
2. Frontend calls UTIL-013 to calculate and display `proratedAmount`, next billing date, and next billing amount.
3. User confirms the preview.
4. Frontend sends a change request including subscriptionId and billingCycle to the backend.
5. Backend requires a reusable billing agreement and immediately charges whole-KRW `proratedAmount` with the stored billing key when the amount is greater than `0`.
6. If the provider reports the stored billing key as removed, not found, or invalid, backend marks the local billing agreement `EXPIRED`, clears issued-key metadata, returns `BILLING_AGREEMENT_REAUTH_REQUIRED`, and leaves the subscription unchanged.
7. If `proratedAmount = 0`, backend skips the provider charge but still keeps the reusable billing agreement requirement.
8. After charge success or zero-amount skip, backend updates `user_subscriptions` to the new plan while preserving the current `billingCycle` and `expiresAt`.
9. Backend saves the prorated payment record in `subscription_payments` only when a provider charge is attempted and succeeds.
10. Backend returns the updated subscription information (including proratedAmount).
   - New plan services (downloadPerDay, maxWhitelistChannels) are applied immediately.
   - The active period keeps its current billingCycle and existing `expiresAt`.
   - The selected billingCycle is stored as pending when it differs from the current cycle and is used by the next renewal charge.

**Main Flow — SCHEDULED_CHANGE (new plan price <= current plan price, or billing-cycle-only change)**
1. User selects the new (lower-tier) subscription plan and billing cycle.
2. Frontend calls UTIL-013 to display effectiveDate (end of current billing period).
3. User confirms the deferred change.
4. Frontend sends a change request including subscriptionId and billingCycle to the backend.
5. Backend stores pendingSubscriptionId and pendingBillingCycle on the current user_subscriptions record.
6. Backend does NOT change the current plan. No PG payment at this point.
7. At the next successful renewal charge, the system applies the pending plan/cycle (replaces subscriptionId/billingCycle, resets expiresAt).
8. Backend returns a 200 response confirming the scheduled change.
   - Current plan services remain until expiresAt.

**Main Flow — NO_CHANGE (clear pending change)**
1. User selects the current plan and current billing cycle while a pending change exists.
2. Frontend calls UTIL-013 and shows that no immediate charge is required.
3. User confirms clearing the reservation.
4. Backend clears pendingSubscriptionId and pendingBillingCycle.
5. Current plan, current billing cycle, and expiresAt remain unchanged.

**Alternative Flow — CANCELLED grace-period subscription**
- If the subscription is CANCELLED but still within expiresAt, a confirmed plan change reactivates the subscription first.
- Reactivation reuses the stored billing key when available. If the billing agreement is missing or invalid, backend returns a billing agreement business error and no subscription change is applied.

**Alternative Flow — billing key removed by provider**
- If Toss returns `ALREADY_REMOVED_BILLING_KEY` or another removed/not-found/invalid billing-key failure, the backend expires the local billing agreement and asks the frontend to start payment-method re-registration.
- If a previous payment-method registration was interrupted and the billing agreement remains `READY`, the frontend treats it as incomplete and offers the same re-registration action.
- The user can re-register a payment method through the current plan/current cycle checkout with `purpose=BILLING_AGREEMENT`.
- Re-registration stores a new encrypted billing key without charging the card or changing the current subscription period.
- When re-registration was started from an upgrade preview, frontend preserves the selected target plan/cycle and expected prorated amount as return context, then reopens the same preview after registration succeeds.

**Postconditions**
- UPGRADE: billing-key charge succeeds first when `proratedAmount > 0`, then `user_subscriptions.subscription_id` is updated immediately. Payment record is saved in `subscription_payments` only for a real charge. New plan benefits are active immediately, while current `billingCycle` and next billing date are preserved for the active period. If the requested billing cycle differs, pendingSubscriptionId and pendingBillingCycle are saved for the next renewal.
- SCHEDULED_CHANGE: pendingSubscriptionId and pendingBillingCycle saved or overwritten. Current plan active until expiresAt. New plan/cycle is applied only after the next successful renewal charge; if renewal is cancelled or fails through grace, the subscription expires without applying pending changes.
- NO_CHANGE: pendingSubscriptionId and pendingBillingCycle are cleared. Current plan remains active through the original expiresAt.

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
| **Version** | 26-05-22 |
| **Description** | Member directly cancels their own active subscription. This means "stop the next renewal": status is set to CANCELLED immediately, local billing agreement renewal is stopped, but service remains available until expiresAt. Benefits terminate automatically at expiresAt unless the member reactivates before then. |
| **Actor** | User (Member), Backend |
| **Preconditions** | Logged in. Has active subscription (user_subscriptions.status=ACTIVE). |
| **Trigger** | User clicks the 'Cancel Subscription' button on the 'My Subscription' screen. |
| **Related UC** | PAYMENT-006 (view my subscription) |

**Main Flow**
1. User clicks the 'Cancel Subscription' button.
2. Frontend displays the cancellation notice (advisory: service remains available until expiresAt, no refund).
3. User clicks the 'Confirm' button.
4. Frontend sends a cancellation request including auth token to the backend. (`DELETE /api/user-subscriptions/me`)
5. Backend checks for an active subscription.
6. Backend updates user_subscriptions.status to CANCELLED and locally marks the recurring billing agreement as CANCELLED without clearing the encrypted billing key.
7. Backend returns 204 No Content.
   - expiresAt is NOT changed. Service access continues until the original expiresAt.

**Exception / Alternative Flow**
- No active subscription: 404 `SUBSCRIPTION_NOT_FOUND`.

**Postconditions**
- user_subscriptions.status=CANCELLED. Service (downloads, channel registration, playlists) remains available until expiresAt. At expiresAt, subscription expires automatically and all benefits terminate.

---

## PAYMENT-011: Reactivate My Subscription

| Field | Value |
|-------|-------|
| **Code** | PAYMENT-011 |
| **Version** | 26-05-22 |
| **Description** | Member reverses a previous subscription cancellation before expiresAt. The existing billing agreement is reactivated using the stored billing key, so the member does not need to register the card again when the key is still valid. |
| **Actor** | User (Member), Backend |
| **Preconditions** | Logged in. Has CANCELLED grace-period subscription before expiresAt. Billing agreement has a stored billing key. |
| **Trigger** | User clicks the 'Keep subscription' button on the 'My Subscription' screen. |
| **Related UC** | PAYMENT-006 (view my subscription), PAYMENT-010 (cancel my subscription) |

**Main Flow**
1. Frontend sends a reactivation request. (`POST /api/user-subscriptions/me/reactivate`)
2. Backend finds the current grace-period subscription.
3. Backend reactivates the billing agreement and sets nextBillingAt to the current subscription expiresAt.
4. Backend updates user_subscriptions.status to ACTIVE.
5. Backend returns the updated subscription response.

**Exception / Alternative Flow**
- No active/grace subscription: 404 `NO_ACTIVE_SUBSCRIPTION`.
- Missing or invalid billing agreement: billing agreement business error. User must register a payment method again in a future billing-method replacement flow.

**Postconditions**
- user_subscriptions.status=ACTIVE. Automatic renewal resumes from the original expiresAt.
