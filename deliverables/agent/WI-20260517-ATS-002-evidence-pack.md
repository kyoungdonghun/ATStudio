# Evidence Pack: WI-20260517-ATS-002

## Summary (one-liner)
- Added frontend Toss widget loading, Toss success/fail redirect handling, and upgrade routing through the payment page.

## Scope / DoD Check
- DoD items:
  - [x] Payment page branches by `checkout.type`.
  - [x] Toss widget uses server-issued client key, customer key, order name, success URL, and fail URL.
  - [x] Toss success redirect confirms backend payment with `paymentKey`.
  - [x] Toss fail redirect closes the order when `orderId` exists.
  - [x] Upgrade flow routes to the central payment page with `purpose=UPGRADE`.

## Reference Documents (Tier 0-2)

**Injected Context**:

| Tier | Document | Reason |
|------|----------|--------|
| 0 | docs/standards/core-principles.md | Constitution and user-facing safety |
| 0 | docs/standards/development-standards.md | Implementation and testing standards |
| 1 | docs/standards/frontend-standards.md | React/Vite frontend conventions |
| 2 | .agents/skills/react-best-practices/AGENTS.md | React performance and third-party script guidance |
| 2 | docs/design/payment-integration-design.md | Payment frontend flow |
| 2 | docs/ui/screen-flow.md | Subscription flow |

## Evidence Pointers
- Files changed:
  - `frontend/src/utils/tossPayments.ts` - Toss V2 script loader and widget types.
  - `frontend/src/api/payments.ts` - Toss checkout and confirm request fields.
  - `frontend/src/pages/subscriber/SubscriptionPaymentPage.tsx` - Mock/Toss payment branching, redirect confirm/fail handling.
  - `frontend/src/pages/subscriber/SubscriptionPaymentPage.module.css` - Toss widget container spacing.
  - `frontend/src/pages/subscriber/SubscriptionManagePage.tsx` - upgrade route transition to payment page.
  - `frontend/src/router/index.tsx` - Toss success/fail callback routes.
  - `frontend/src/pages/subscriber/SubscriptionPaymentPage.test.tsx` - Toss widget and redirect tests.
  - `frontend/src/pages/subscriber/SubscriptionManagePage.test.tsx` - upgrade routing test.
- Key locations:
  - `frontend/src/pages/subscriber/SubscriptionPaymentPage.tsx:63` - Toss success query extraction.
  - `frontend/src/pages/subscriber/SubscriptionPaymentPage.tsx:147` - Toss widget render branch.
  - `frontend/src/pages/subscriber/SubscriptionPaymentPage.tsx:203` - Toss `requestPayment` branch.
  - `frontend/src/pages/subscriber/SubscriptionManagePage.tsx:147` - upgrade payment route.

## Commands & Outputs
- `npm test -- SubscriptionPaymentPage.test.tsx SubscriptionManagePage.test.tsx` -> pass, 2 files / 8 tests.
- `npm run typecheck` -> pass.
- `npm run lint` -> pass.
- `npx prettier --check ...touched frontend files...` -> pass.

## Tests
- Focused Vitest payment flow tests: pass.
- TypeScript typecheck: pass.
- ESLint: pass.
- Prettier touched-file check: pass.

## Risks / Rollback
- Risks:
  - Browser-side real Toss widget behavior still needs manual verification with Toss test keys.
  - Redirect URLs currently default to localhost and should be configured per environment.
- Rollback:
  - Revert the frontend files listed above and remove `/subscriptions/payment/success|fail` routes.

## Follow-ups
- Add a browser/manual QA checklist for Toss test-key checkout.
