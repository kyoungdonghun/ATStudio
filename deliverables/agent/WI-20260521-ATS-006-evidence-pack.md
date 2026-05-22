# Evidence Pack: WI-20260521-ATS-006

## Summary
- Added read-only admin payment operations API and UI.

## Scope / DoD Check
- [x] Admin can list payment orders.
- [x] Admin can list billing agreements with masked method only.
- [x] Admin can list finalized subscription payment rows.
- [x] UI is read-only and does not expose raw billing keys, auth keys, customer keys, Toss secrets, or raw provider payloads.

## Evidence Pointers
- `src/main/java/com/atstudio/atstudio/controller/AdminPaymentController.java`
- `src/main/java/com/atstudio/atstudio/service/AdminPaymentReadService.java`
- `src/main/java/com/atstudio/atstudio/dto/payment/AdminPaymentOrderResponse.java`
- `src/main/java/com/atstudio/atstudio/dto/payment/AdminBillingAgreementResponse.java`
- `src/main/java/com/atstudio/atstudio/dto/payment/AdminSubscriptionPaymentResponse.java`
- `frontend/src/pages/admin/PaymentReadOnlyPage.tsx`
- `frontend/src/pages/admin/PaymentReadOnlyPage.module.css`
- `frontend/src/api/admin.ts`
- `frontend/src/layouts/AdminLayout.tsx`
- `frontend/src/router/index.tsx`

## Validation
- Backend focused tests passed.
- Frontend focused tests passed.
- Full backend tests, full frontend tests, typecheck, lint, backend/frontend builds, and docs validation passed in the final gate.

## Risks / Rollback
- API currently lists latest records without filtering; search filters are future enhancements.
- Rollback by reverting admin payment controller/service/DTOs and frontend admin route/page changes.
