# Evidence Pack: WI-20260517-ATS-012

## Summary (one-liner)
- Connected frontend recurring billing registration and management UX to the new billing agreement backend APIs and Toss Billing SDK flow.

## Scope / DoD Check
- DoD items:
  - [x] Added frontend API types/functions for billing agreement prepare, confirm, current state, and cancel.
  - [x] Added Toss SDK typing for `payment({ customerKey }).requestBillingAuth(...)`.
  - [x] Added `/subscriptions/billing/success` and `/subscriptions/billing/fail` routes.
  - [x] Subscription plan CTA enters recurring billing mode for new subscriptions.
  - [x] Billing success redirect confirms `authKey`, `customerKey`, `orderId`, and amount with backend.
  - [x] Subscription management page shows billing agreement state and automatic renewal cancel action.
  - [x] Existing mock/Toss one-time payment branch remains in `SubscriptionPaymentPage`.
  - [x] Typecheck and ESLint pass.
  - [ ] Focused Vitest and Vite build could not run in this sandbox because esbuild process spawn failed with `EPERM`.

## Reference Documents (Tier 0-2)

**Injected Context**:

| Tier | Document | Reason |
|------|----------|--------|
| 0 | docs/standards/core-principles.md | Constitution and payment traceability principles |
| 0 | docs/standards/development-standards.md | React/TypeScript implementation standards |
| 1 | docs/policies/security-policy.md | Secret and billing-key non-exposure |
| 1 | docs/policies/access-control-policy.md | Authenticated billing routes |
| 1 | docs/policies/quality-gates.md | Frontend verification expectations |
| 2 | .agents/skills/react-best-practices/SKILL.md | React implementation guidance |
| 2 | deliverables/user/REQ-20260517-ATS-002.md | Approved recurring billing requirement |
| 2 | deliverables/agent/WI-20260517-ATS-010-evidence-pack.md | Billing agreement backend API |
| 2 | deliverables/agent/WI-20260517-ATS-011-evidence-pack.md | Renewal backend behavior |
| 2 | docs/design/payment-integration-design.md | Payment architecture baseline |
| 2 | docs/design/api-spec.md | API documentation baseline |
| 2 | docs/ui/screen-flow.md | UI flow baseline |

**External References**:

| Source | Reason |
|--------|--------|
| https://docs.tosspayments.com/guides/v2/billing/integration | Toss Billing `requestBillingAuth` redirect flow |
| https://docs.tosspayments.com/sdk/v2/js | Toss Payments SDK v2 typing reference |

## Evidence Pointers
- Files changed:
  - `frontend/src/api/payments.ts` - billing agreement API types/functions.
  - `frontend/src/utils/tossPayments.ts` - Toss Billing SDK helper typing.
  - `frontend/src/pages/public/SubscriptionPlanPage.tsx` - new-subscription CTA enters recurring billing mode.
  - `frontend/src/pages/subscriber/SubscriptionPaymentPage.tsx` - recurring billing prepare, auth popup, success/fail redirect handling.
  - `frontend/src/pages/subscriber/SubscriptionManagePage.tsx` - billing agreement state display and automatic renewal cancel action.
  - `frontend/src/router/index.tsx` - billing success/fail routes.
  - `frontend/src/pages/subscriber/SubscriptionPaymentPage.test.tsx` - recurring billing prepare/auth/confirm test cases.
  - `frontend/src/pages/subscriber/SubscriptionManagePage.test.tsx` - billing agreement display/cancel test case.
- Key locations:
  - `frontend/src/api/payments.ts:69` - billing prepare request/response types.
  - `frontend/src/api/payments.ts:134` - billing prepare API function.
  - `frontend/src/api/payments.ts:144` - billing confirm API function.
  - `frontend/src/api/payments.ts:154` - current billing agreement API function.
  - `frontend/src/api/payments.ts:161` - cancel billing agreement API function.
  - `frontend/src/utils/tossPayments.ts:20` - `requestBillingAuth` type.
  - `frontend/src/pages/subscriber/SubscriptionPaymentPage.tsx:271` - Toss Billing auth popup call.
  - `frontend/src/pages/public/SubscriptionPlanPage.tsx:177` - recurring mode navigation.
  - `frontend/src/router/index.tsx:154` - billing success/fail routes.
  - `frontend/src/pages/subscriber/SubscriptionManagePage.tsx:305` - automatic renewal state section.
  - `frontend/src/pages/subscriber/SubscriptionManagePage.tsx:334` - automatic renewal cancel action.
  - `frontend/src/pages/subscriber/SubscriptionPaymentPage.test.tsx:257` - recurring billing auth test.
  - `frontend/src/pages/subscriber/SubscriptionPaymentPage.test.tsx:288` - billing success redirect test.
  - `frontend/src/pages/subscriber/SubscriptionManagePage.test.tsx:246` - automatic renewal cancel test.

## Commands & Outputs
- Commands executed:
  - `npm test -- SubscriptionPaymentPage.test.tsx SubscriptionManagePage.test.tsx` -> blocked: Vite config load failed because esbuild child process spawn returned `EPERM` in sandbox.
  - `npm run typecheck` -> pass.
  - `npm run build` -> blocked: Vite build failed because esbuild child process spawn returned `EPERM` in sandbox.
  - `npm run lint` -> pass.

## Tests
- TypeScript typecheck: pass.
- ESLint: pass.
- Focused Vitest: not executed due sandbox `spawn EPERM`.
- Vite build: not completed due sandbox `spawn EPERM`.

## Risks / Rollback
- Risks:
  - Focused frontend tests and Vite build need to be rerun outside the sandbox or after the esbuild spawn permission issue is cleared.
  - Toss Billing auth success URL depends on preserving `orderId` and `amount` query params alongside Toss `authKey` and `customerKey`.
  - SR-92 checkout modal/separate checkout UX remains deferred.
- Rollback:
  - Revert frontend billing additions in `payments.ts`, `tossPayments.ts`, `SubscriptionPlanPage.tsx`, `SubscriptionPaymentPage.tsx`, `SubscriptionManagePage.tsx`, router routes, and related tests.

## Follow-ups
- WI-20260517-ATS-013: update API/DB/UI documentation and rerun frontend tests/build in an environment where Vite/esbuild can spawn.
