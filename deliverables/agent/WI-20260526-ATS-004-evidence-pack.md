# Evidence Pack: WI-20260526-ATS-004

## Summary

- Implemented the admin settlement operations UI and frontend API client for REQ-20260526-ATS-001.

## Scope / DoD Check

- [x] Settlement tab is visible in `/admin/payments`.
- [x] CSV import form calls the admin settlement import API and shows result counts.
- [x] Settlement list shows status, order, provider payment key, user, source, amounts, dates, local mapping, and mismatch reason.
- [x] Status/source/base-date filters use the backend paginated list API.
- [x] Missing-provider candidate scan and IGNORE action are available from the admin UI.
- [x] Raw secrets and raw provider payload are not displayed.

## Reference Documents

| Tier | Document | Reason |
|------|----------|--------|
| 0 | docs/standards/core-principles.md | Constitution |
| 0 | docs/standards/development-standards.md | Frontend implementation standards |
| 2 | docs/design/api-spec.md | Admin settlement API contract |
| 2 | docs/ui/atstudio-front-list.md | Admin payment screen inventory |
| 2 | docs/ui/screen-flow.md | Admin payment screen flow |
| 2 | deliverables/user/REQ-20260526-ATS-001.md | Approved scope |
| 2 | deliverables/agent/WI-20260526-ATS-002-evidence-pack.md | Backend API evidence |

## Evidence Pointers

- `frontend/src/api/admin.ts` - settlement response/import types and `fetchAdminPaymentSettlements`, `importAdminPaymentSettlements`, `reconcileAdminPaymentSettlements`, `ignoreAdminPaymentSettlement`.
- `frontend/src/pages/admin/PaymentReadOnlyPage.tsx` - settlement tab, filters, import/reconcile/ignore handlers, operation panel, and settlement table.
- `frontend/src/pages/admin/PaymentReadOnlyPage.module.css` - file input, error list, and settlement status badge styles.

## Commands & Outputs

- `npm run typecheck` -> passed.
- `npm run lint` -> passed.
- `npx prettier --check src\api\admin.ts src\pages\admin\PaymentReadOnlyPage.tsx src\pages\admin\PaymentReadOnlyPage.module.css` -> passed.
- `npm run build` -> passed.

## Risks / Rollback

- Risk: Actual Toss settlement exports may require a CSV column mapping adapter after the first live sample.
- Risk: The first admin UI is dense and operational; future UX refinement may be useful after operators use real data.
- Rollback: Revert the settlement additions in `frontend/src/api/admin.ts`, `PaymentReadOnlyPage.tsx`, and `PaymentReadOnlyPage.module.css`.

## Follow-ups

- WI-20260526-ATS-005: final doc/API/DB/UI sync and validation.
