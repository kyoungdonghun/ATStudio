# Evidence Pack: WI-20260525-ATS-015

## Summary

- Added first-class admin payment operations UI for receipts, audit logs, refunds, and refund-linked entitlement corrections.

## Scope / DoD Check

- [x] Added admin API client types and calls for receipt/audit/refund/entitlement correction operations.
- [x] Added `/admin/payments` tabs: `영수증`, `감사로그`, `환불`, `권한 보정`.
- [x] Refund workflow keeps preview, request, approve, and execute separate.
- [x] Entitlement correction workflow keeps preview, request, approve, and execute separate.
- [x] Provider refund execution and local entitlement correction execution require typed confirmation.
- [x] Admin UI boundary remains explicit: ordinary subscription edits stay in user subscription management, while payment-backed refund/correction operations stay in payment operations.
- [x] No raw billing key, authKey, customerKey, Toss secret, raw card data, or raw provider payload fields were added to UI state.

## Reference Documents

| Tier | Document | Reason |
|------|----------|--------|
| 0 | docs/standards/core-principles.md | Constitution |
| 0 | docs/standards/development-standards.md | Frontend implementation standards |
| 1 | docs/policies/security-policy.md | Sensitive data boundary |
| 2 | docs/design/api-spec.md | Admin payment API contract |
| 2 | docs/design/payment-refund-receipt-settlement-policy.md | Refund/entitlement policy |
| 2 | docs/SR/SR-93.md | Payment operation SR scope |

## Evidence Pointers

- `frontend/src/api/admin.ts` - added admin payment receipt, audit, refund, and entitlement correction client contracts.
- `frontend/src/pages/admin/PaymentReadOnlyPage.tsx` - added tabs, tables, preview panels, and mutation controls.
- `frontend/src/pages/admin/PaymentReadOnlyPage.module.css` - added compact operation panel/table controls.

## Commands & Outputs

- `npm run typecheck` -> passed.
- `npm run lint` -> passed.
- `npm run build` -> passed.

## Risks / Rollback

- Risk: admin refund and entitlement mutation flows need careful manual testing with safe Toss/staging data before production use.
- Rollback: revert `frontend/src/api/admin.ts`, `frontend/src/pages/admin/PaymentReadOnlyPage.tsx`, and `frontend/src/pages/admin/PaymentReadOnlyPage.module.css`.
