# Evidence Pack: WI-20260516-ATS-004

## Summary (one-liner)
- Aligned backend payment DTOs and frontend payment API/types.

## Scope / DoD Check
- DoD items:
  - [x] Endpoint paths match frontend API client.
  - [x] Request fields match backend records.
  - [x] Response wrapper shape uses `data.data` in frontend.
  - [x] `SUBSCRIBE` and `UPGRADE` purposes are represented.
  - [x] Deprecated direct subscribe path is removed from user-facing payment page.

## Reference Documents (Tier 0-2)

| Tier | Document | Reason |
|------|----------|--------|
| 0 | docs/standards/core-principles.md | Constitution |
| 0 | docs/standards/development-standards.md | Development standards |
| 1 | docs/policies/security-policy.md | Payment authorization |
| 2 | docs/design/payment-integration-design.md | API design |
| 2 | docs/design/api-spec.md | API spec |

## Evidence Pointers
- Files changed:
  - `src/main/java/com/atstudio/atstudio/dto/payment/*`
  - `src/main/java/com/atstudio/atstudio/controller/PaymentController.java`
  - `frontend/src/api/payments.ts`
  - `frontend/src/pages/subscriber/SubscriptionPaymentPage.tsx`
  - `frontend/src/pages/subscriber/SubscriptionManagePage.tsx`

## Commands & Outputs
- `npm run typecheck` -> passed.
- `./gradlew.bat test` -> passed.

## Risks / Rollback
- Risks:
  - Combined Vitest rerun after manage-page test addition was blocked by tool usage limit.
- Rollback:
  - Revert backend DTO/controller and frontend API/page changes together.
