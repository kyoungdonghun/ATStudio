# Evidence Pack: WI-20260525-ATS-009

## Summary
- Defined the refund-linked entitlement correction boundary as an explicit admin target-state workflow.

## Scope / DoD Check
- [x] Correction requires a linked refund and explicit target subscription state.
- [x] Preview/request/approve/execute boundaries are defined.
- [x] Local billing agreement cancellation is separated from provider billing-key deletion.
- [x] Previous-plan inference rollback is forbidden.

## Evidence Pointers
- `deliverables/user/REQ-20260525-ATS-005.md` — approved scope.
- `src/main/java/com/atstudio/atstudio/entity/PaymentEntitlementCorrection.java` — ledger shape.
- `docs/design/payment-refund-receipt-settlement-policy.md` — policy boundary.

## Verification
- Design was implemented and validated through WI-010 through WI-013.

## Rollback
- Revert entitlement correction code/docs and return refund policy to refund-only backend scope.
