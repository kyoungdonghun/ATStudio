# Evidence Pack: WI-20260521-ATS-005

## Summary
- Converted user-facing subscription checkout to recurring billing auth only.

## Scope / DoD Check
- [x] New subscription CTA routes to `/subscriptions/checkout`.
- [x] Checkout page prepares billing agreement and calls Toss `requestBillingAuth`.
- [x] `/subscriptions/checkout/success` confirms billing agreement and first charge.
- [x] Legacy `/subscriptions/payment/*` redirect is blocked with safe user copy.
- [x] Upgrade route does not prepare one-time checkout.

## Evidence Pointers
- `frontend/src/pages/public/SubscriptionPlanPage.tsx`
- `frontend/src/pages/subscriber/SubscriptionPaymentPage.tsx`
- `frontend/src/router/index.tsx`
- `frontend/src/api/payments.ts`
- `frontend/src/pages/public/SubscriptionPlanPage.test.tsx`
- `frontend/src/pages/subscriber/SubscriptionPaymentPage.test.tsx`
- `src/main/resources/application.yml`
- `src/main/java/com/atstudio/atstudio/config/PaymentProperties.java`

## Validation
- `npm test -- SubscriptionPaymentPage.test.tsx SubscriptionPlanPage.test.tsx`: passed, 2 files / 6 tests.

## Risks / Rollback
- Bookmarked one-time subscription URLs no longer attempt PG confirmation.
- Rollback by restoring the previous payment page and route defaults.
