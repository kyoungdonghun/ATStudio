# Evidence Pack: WI-20260526-ATS-005

## Summary

- Completed final code/design/document verification for REQ-20260526-ATS-001 settlement import/reconciliation.

## Scope / DoD Check

- [x] Backend settlement flow passed targeted regression tests.
- [x] Frontend settlement admin UI passed typecheck, lint, formatting check, and build.
- [x] API, DB, SR, runbook, UI inventory, and acceptance checklist were synchronized with the implementation.
- [x] Toss Settlement API adapter remains documented as future, not implemented.
- [x] Tax invoice and cash receipt mutation remain outside this REQ.
- [x] `frontend/tsconfig.tsbuildinfo` cache change was restored.

## Reference Documents

| Tier | Document | Reason |
|------|----------|--------|
| 0 | docs/standards/core-principles.md | Constitution |
| 0 | docs/standards/development-standards.md | Development standards |
| 0 | docs/standards/documentation-standards.md | Documentation standards |
| 0 | docs/standards/glossary.md | Shared terminology |
| 1 | docs/policies/security-policy.md | Sensitive data boundary |
| 1 | docs/policies/quality-gates.md | Verification expectations |
| 2 | docs/design/payment-refund-receipt-settlement-policy.md | Payment operations policy |
| 2 | docs/design/payment-integration-design.md | Payment architecture context |
| 2 | docs/design/api-spec.md | API contract |
| 2 | docs/design/db-schema.md | Database contract |
| 2 | docs/SR/SR-93.md | Production readiness tracking |
| 2 | docs/ui/atstudio-front-list.md | Screen/API inventory |
| 2 | docs/ui/screen-flow.md | Screen flow |
| 2 | deliverables/user/REQ-20260526-ATS-001.md | Approved scope |

## Evidence Pointers

- `docs/design/api-spec.md` - v15, 139 endpoints, admin settlement API documentation.
- `docs/design/db-schema.md` - v10, 36 tables, `payment_settlements` and settlement audit enum updates.
- `docs/design/payment-settlement-import-design.md` - CSV-first source adapter, matching rules, security boundary, acceptance checklist.
- `docs/design/payment-refund-receipt-settlement-policy.md` - implemented settlement ledger/API/UI policy state.
- `docs/design/payment-operations-runbook.md` - settlement operating workflow and production rehearsal checklist.
- `docs/SR/SR-93.md` - P2-D settlement slice applied and completed.
- `docs/ui/atstudio-front-list.md`, `docs/ui/screen-flow.md` - `/admin/payments` settlement tab and workflow.
- `deliverables/user/PAYMENT-FINAL-ACCEPTANCE-CHECKLIST-20260525.md` - final acceptance checklist now includes settlement checks.

## Commands & Outputs

- `gradlew.bat test --tests "com.atstudio.atstudio.service.AdminPaymentSettlementServiceTest" --tests "com.atstudio.atstudio.service.AdminPaymentRefundServiceTest" --tests "com.atstudio.atstudio.service.AdminPaymentEntitlementCorrectionServiceTest" --tests "com.atstudio.atstudio.service.PaymentOperationAuditLogServiceTest"` -> passed.
- `npm run typecheck` -> passed.
- `npm run lint` -> passed.
- `npx prettier --check src\api\admin.ts src\pages\admin\PaymentReadOnlyPage.tsx src\pages\admin\PaymentReadOnlyPage.module.css` -> passed.
- `npm run build` -> passed.
- `python .agents\skills\validate-docs\scripts\validate_docs.py` -> passed.
- `git diff --check` -> passed after trailing whitespace cleanup.

## Risks / Rollback

- Risk: Real provider settlement exports may require adding a Toss-specific CSV column mapping or a Toss Settlement API adapter.
- Risk: Fee/VAT/net formula may need tuning after real settlement contract samples are reviewed.
- Rollback: Revert the REQ-20260526-ATS-001 implementation commit, including backend settlement files, frontend settlement UI additions, and documentation updates.

## Follow-ups

- Tax invoice request/admin workflow.
- Optional Toss Settlement API adapter automation.
- Webhook hardening and multi-PG adapters as separate SR/REQ items.
