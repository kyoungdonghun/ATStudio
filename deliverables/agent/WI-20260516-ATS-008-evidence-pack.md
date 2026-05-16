# Evidence Pack: WI-20260516-ATS-008

## Summary (one-liner)
- Completed final review of Mock-first payment code, docs, and verification evidence.

## Scope / DoD Check
- DoD items:
  - [x] New subscription payment page uses prepare/confirm.
  - [x] Upgrade path uses prepare/confirm before applying upgrade.
  - [x] Backend stores payment order lifecycle.
  - [x] Failure/cancel do not mutate subscription state.
  - [x] Backend full test suite passes.
  - [x] Frontend typecheck passes.
  - [x] Docs validation passes.

## Reference Documents (Tier 0-2)

| Tier | Document | Reason |
|------|----------|--------|
| 0 | docs/standards/core-principles.md | Constitution |
| 0 | docs/standards/development-standards.md | Development standards |
| 1 | docs/policies/security-policy.md | Security policy |
| 1 | docs/policies/quality-gates.md | Quality policy |
| 2 | docs/design/payment-integration-design.md | Payment design |

## Evidence Pointers
- Changed areas:
  - Backend payment model/API/service/tests under `src/main/java` and `src/test/java`.
  - Frontend payment API and subscriber pages under `frontend/src`.
  - Schema/docs under `src/main/resources`, `docs/design`, and `docs/ui`.
  - Traceability deliverables under `deliverables/user` and `deliverables/agent`.

## Commands & Outputs
- `./gradlew.bat test` -> passed.
- `npm run typecheck` -> passed.
- `python .agents/skills/validate-docs/scripts/validate_docs.py` -> passed.
- `npm test -- SubscriptionPaymentPage.test.tsx` -> passed.
- `npm test -- SubscriptionPaymentPage.test.tsx SubscriptionManagePage.test.tsx` -> blocked by tool usage limit during escalation review.

## Risks / Rollback
- Risks:
  - Legacy direct `POST /api/user-subscriptions` and `PUT /api/user-subscriptions/me` still exist and can mutate subscriptions if called directly.
  - Real Toss integration and recurring billing are still future phases.
  - Manage-page Vitest case was added but not executed due tool usage limit.
- Rollback:
  - Revert this implementation commit. DB rollback must drop `payment_orders` and remove nullable payment linkage columns from `subscription_payments` if already applied.
