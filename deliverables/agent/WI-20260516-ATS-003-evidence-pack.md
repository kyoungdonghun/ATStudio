# Evidence Pack: WI-20260516-ATS-003

## Summary (one-liner)
- Reviewed and implemented security checks for Mock-first payment ownership and confirm boundaries.

## Scope / DoD Check
- DoD items:
  - [x] Authenticated user must own the payment order.
  - [x] Client amount must match stored server amount.
  - [x] Provider type must match stored order provider.
  - [x] Expired, failed, and cancelled states are blocked from successful confirm.
  - [x] Real secrets and billing keys are out of scope.

## Reference Documents (Tier 0-2)

| Tier | Document | Reason |
|------|----------|--------|
| 0 | docs/standards/core-principles.md | Constitution |
| 1 | docs/policies/security-policy.md | Security policy |
| 2 | docs/design/payment-integration-design.md | Payment design |

## Evidence Pointers
- Files changed:
  - `src/main/java/com/atstudio/atstudio/service/PaymentApplicationService.java`
  - `src/main/java/com/atstudio/atstudio/entity/PaymentOrder.java`
  - `src/main/java/com/atstudio/atstudio/common/exception/BUSINESS_ERROR.java`

## Commands & Outputs
- `./gradlew.bat test` -> passed.

## Risks / Rollback
- Risks:
  - Legacy direct subscribe/change endpoint remains for compatibility; user-facing frontend no longer uses it for purchase/upgrade.
- Rollback:
  - Revert payment service/controller/model additions.
