# Evidence Pack: WI-20260516-ATS-002

## Summary (one-liner)
- Replaced direct subscription purchase UI with Mock prepare/confirm/cancel payment flow.

## Scope / DoD Check
- DoD items:
  - [x] Frontend payment API client added.
  - [x] `SubscriptionPaymentPage` calls prepare before confirm.
  - [x] Mock success confirms and navigates to manage page.
  - [x] Mock failure/cancel close the order without success navigation.
  - [x] Direct `subscribe()` call removed from payment page.

## Reference Documents (Tier 0-2)

| Tier | Document | Reason |
|------|----------|--------|
| 0 | docs/standards/core-principles.md | Constitution |
| 0 | docs/standards/development-standards.md | Development standards |
| 2 | docs/standards/frontend-standards.md | Frontend standards |
| 2 | .agents/skills/react-best-practices/SKILL.md | React guidance |
| 2 | docs/design/payment-integration-design.md | Target design |
| 2 | docs/ui/screen-flow.md | UI flow |

## Evidence Pointers
- Files changed:
  - `frontend/src/api/payments.ts`
  - `frontend/src/pages/subscriber/SubscriptionPaymentPage.tsx`
  - `frontend/src/pages/subscriber/SubscriptionPaymentPage.module.css`
  - `frontend/src/pages/subscriber/SubscriptionPaymentPage.test.tsx`

## Commands & Outputs
- `npm test -- SubscriptionPaymentPage.test.tsx` -> passed.
- `npm run typecheck` -> passed.

## Risks / Rollback
- Risks:
  - Later combined Vitest rerun was blocked by tool usage limit, so only the earlier payment page test pass is recorded.
- Rollback:
  - Revert `frontend/src/api/payments.ts` and payment page files.
