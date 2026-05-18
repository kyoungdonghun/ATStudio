# WI-20260518-ATS-013 Summary

- Defined the payment UX state model for one-time Toss checkout and Toss billing auth.
- Recommendation: keep current inline/page-fixed Toss surface only for local/debug use; prefer a dedicated checkout/callback route for recurring billing auth, and allow modal/drawer for one-time Toss widget after viewport checks.
- User-facing states now cover prepare, PG progress, success, failure, cancel, expired/interrupted order, retry, and billing past-due.
- Reflected in `docs/design/payment-integration-design.md`, `docs/ui/screen-flow.md`, `docs/ui/modal-list.md`, and `docs/SR/SR-92.md`.
