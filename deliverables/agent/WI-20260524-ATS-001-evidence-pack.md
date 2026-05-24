# Evidence Pack: WI-20260524-ATS-001

## Summary (one-liner)

- Implemented SR-93 P1 payment operations readiness: Toss provider API reconciliation, admin read-only reconciliation endpoint, production runbook, and current-state documentation sync.

## Scope / DoD Check

- DoD items:
  - [x] Provider reconciliation checks local subscription payment orders against Toss provider state by `orderId`.
  - [x] Local ledger and provider ledger mismatches are reported separately.
  - [x] Admin/operator read-only endpoint exposes support-safe reconciliation evidence.
  - [x] Provider success plus local persistence failure runbook is documented.
  - [x] Current visibility boundary is documented: WARN logs and on-demand admin checks only, no persistent incidents or operator notifications yet.
  - [x] No DB schema migration was introduced.
  - [x] Focused backend tests pass.
  - [x] Full backend tests pass.
  - [x] Docs validation passes.

## Reference Documents (Tier 0-2)

**Injected Context**:

| Tier | Document | Reason |
|---|---|---|
| 0 | docs/standards/core-principles.md | Constitution |
| 0 | docs/standards/development-standards.md | Backend implementation standards |
| 0 | docs/standards/documentation-standards.md | Documentation update standards |
| 0 | docs/standards/glossary.md | Terminology |
| 1 | docs/policies/security-policy.md | Payment secret and sensitive-data boundary |
| 2 | docs/SR/SR-93.md | Production readiness source item |
| 2 | docs/design/payment-integration-design.md | Payment architecture |
| 2 | docs/design/api-spec.md | API contract |
| 2 | docs/design/db-schema.md | Schema boundary confirmation |

**Injection Rules Applied**:

- Rule source: `.claude/config/context-injection-rules.json`
- Assignee: se/docops/pg/qa-integ
- Task type: payment operations, reconciliation, production readiness
- agent_required_tiers: [0, 1]

## Evidence Pointers

- Files changed:
  - `src/main/java/com/atstudio/atstudio/service/payment/provider/recurring/PaymentStatusLookupProvider.java` — provider-neutral lookup contract.
  - `src/main/java/com/atstudio/atstudio/service/payment/provider/recurring/ProviderPaymentLookupResult.java` — sanitized provider lookup result model.
  - `src/main/java/com/atstudio/atstudio/service/payment/provider/recurring/TossBillingProvider.java` — Toss lookup by `orderId`.
  - `src/main/java/com/atstudio/atstudio/service/PaymentReconciliationService.java` — local/provider reconciliation counts and issue types.
  - `src/main/java/com/atstudio/atstudio/dto/payment/AdminPaymentReconciliationResponse.java` — admin response DTO.
  - `src/main/java/com/atstudio/atstudio/controller/AdminPaymentController.java` — `GET /api/admin/payments/reconciliation`.
  - `src/main/java/com/atstudio/atstudio/service/AdminPaymentReadService.java` — read-only reconciliation facade.
  - `src/main/java/com/atstudio/atstudio/config/PaymentProperties.java` and `src/main/resources/application.yml` — provider lookup URL config.
  - `docs/design/payment-operations-runbook.md` — production incident response and compensation runbook.
  - `docs/SR/SR-93.md` — P1 completion and remaining P2/P3 sync.
  - `docs/design/api-spec.md` — admin reconciliation API and 119 endpoint count.
  - `docs/design/payment-integration-design.md` — provider reconciliation and runbook sync.
  - `docs/index.md`, `docs/design/index.md`, `docs/registry/project-registry.md` — counts and index sync.
  - `docs/ui/modal-list.md` — future admin reconciliation presentation wording.
  - `deliverables/user/REQ-20260524-ATS-001.md` — approved REQ.
  - `deliverables/agent/WI-20260524-ATS-001-handoff.md` — WI handoff.
  - `deliverables/user/WI-20260524-ATS-001-summary.md` — user-facing summary.

- Key locations:
  - `src/main/java/com/atstudio/atstudio/service/PaymentReconciliationService.java` — `reconcileProviderLedger()`.
  - `src/main/java/com/atstudio/atstudio/service/payment/provider/recurring/TossBillingProvider.java` — `findPaymentByOrderId()`.
  - `src/main/java/com/atstudio/atstudio/controller/AdminPaymentController.java` — admin reconciliation endpoint.
  - `docs/design/payment-operations-runbook.md` — provider success/local failure runbook.
  - `docs/SR/SR-93.md` — completed P1 readiness status.

## Commands & Outputs

- `.\gradlew.bat test --tests "com.atstudio.atstudio.service.PaymentReconciliationServiceTest" --tests "com.atstudio.atstudio.service.payment.provider.recurring.TossBillingProviderTest"`
  - Result: BUILD SUCCESSFUL
- `.\gradlew.bat test`
  - Result: BUILD SUCCESSFUL
- `python .agents\skills\validate-docs\scripts\validate_docs.py`
  - Result: All validations passed

## Tests

- Focused tests:
  - `PaymentReconciliationServiceTest`
  - `TossBillingProviderTest`
- Full backend test suite:
  - `.\gradlew.bat test` passed

## Risks / Rollback

- Risks:
  - Provider lookup depends on Toss secret-key configuration and network reachability.
  - `GET /api/admin/payments/reconciliation` performs external provider reads when configured; keep it admin-only.
  - Reconciliation reports issues but does not automatically refund, cancel, or mutate subscriptions.
  - Reconciliation issues are not yet persisted or pushed to operators; production use needs log monitoring until the follow-up incident workflow exists.
- Rollback:
  - Revert this WI's code and docs files.
  - Remove `GET /api/admin/payments/reconciliation` from API spec and controller.
  - Restore SR-93 P1 provider reconciliation/runbook items to remaining status.

## Follow-ups

- Persistent reconciliation incident storage and operator notification.
- Refund/receipt/settlement/tax invoice operations.
- Admin payment mutation and audit policy.
- Multi-server scheduler lock.
- Legacy endpoint removal.
- Multi-PG provider adapters.
