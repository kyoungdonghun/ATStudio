# Evidence Pack: WI-20260525-ATS-001

## Summary (one-liner)
- Added an admin reconciliation incident tab to the payment operations screen and synchronized payment operations documentation.

## Scope / DoD Check
- [x] `/admin/payments` includes a reconciliation incident tab.
- [x] Incident tab lists persisted reconciliation incidents from `/api/admin/payments/reconciliation-incidents`.
- [x] Status filter supports all statuses and all incidents.
- [x] Status update calls `/api/admin/payments/reconciliation-incidents/{incidentId}/status`.
- [x] Incident rows show support-safe issue, severity, order, provider, local/provider state, amount, occurrence, timestamps, and note fields.
- [x] No raw billing keys, auth keys, customer keys, Toss secrets, raw card data, or raw provider payloads are displayed.
- [x] Frontend typecheck, lint, test, build, and docs validation pass.

## Reference Documents (Tier 0-2)

**Injected Context**:

| Tier | Document | Reason |
|------|----------|--------|
| 0 | docs/standards/core-principles.md | Constitution and execution gate |
| 0 | docs/standards/development-standards.md | Frontend/backend development standards |
| 0 | docs/standards/documentation-standards.md | Documentation consistency |
| 0 | docs/standards/glossary.md | Domain terminology |
| 1 | docs/policies/security-policy.md | Sensitive payment data boundary |
| 2 | docs/standards/frontend-standards.md | React SPA implementation standard |
| 2 | .agents/skills/react-best-practices/SKILL.md | React component guidance |
| 2 | docs/design/api-spec.md | Admin incident API contract |
| 2 | docs/SR/SR-93.md | Production readiness source SR |
| 2 | docs/ui/screen-flow.md | Admin screen flow inventory |
| 2 | deliverables/user/REQ-20260525-ATS-001.md | Approved requirement |
| 2 | deliverables/agent/WI-20260525-ATS-001-handoff.md | WI scope and output contract |

**Injection Rules Applied**:
- Rule source: `.claude/config/context-injection-rules.json`
- Assignees: se/docops/qa-fe
- Task type: frontend implementation, payment operations, documentation sync

## Evidence Pointers

- `frontend/src/api/admin.ts:223` — incident list client calls `/admin/payments/reconciliation-incidents` with conditional status params.
- `frontend/src/api/admin.ts:239` — incident workflow status update client calls `/admin/payments/reconciliation-incidents/{incidentId}/status`.
- `frontend/src/pages/admin/PaymentReadOnlyPage.tsx:43` — incident status filter state defaults to `OPEN` for operations triage.
- `frontend/src/pages/admin/PaymentReadOnlyPage.tsx:134` — admin payment screen title changed to `결제 운영`.
- `frontend/src/pages/admin/PaymentReadOnlyPage.tsx:159` — `대사 Incident` tab added.
- `frontend/src/pages/admin/PaymentReadOnlyPage.tsx:339` — incident table renders support-safe workflow fields and note/status controls.
- `frontend/src/pages/admin/PaymentReadOnlyPage.module.css:38` — incident filter layout.
- `frontend/src/pages/admin/PaymentReadOnlyPage.module.css:191` — incident action control layout.
- `frontend/src/layouts/AdminLayout.tsx:20` — admin menu label updated to `결제 운영`.
- `docs/SR/SR-93.md:45` — SR reflects `/admin/payments` incident operations visibility.
- `docs/SR/SR-93.md:147` — multi-server scheduler lock documented as excluded while single-server deployment remains true.
- `docs/design/payment-operations-runbook.md:74` — runbook admin incident workflow.
- `docs/design/payment-operations-runbook.md:141` — operator decision path now points to the `/admin/payments` incident tab.
- `docs/design/payment-integration-design.md:667` — payment design reflects persisted incident workflow and admin visibility.
- `docs/ui/atstudio-front-list.md:86` — UI inventory includes incident list/update APIs.
- `docs/ui/screen-flow.md:397` — screen flow marks payment operations incident workflow.

## Commands & Outputs

- `npm run typecheck`
  - Result: passed.
- `npm run lint`
  - Result: passed.
- `npm test`
  - Result: passed, 14 files / 51 tests.
- `npm run build`
  - Result: passed.
- `python .agents\skills\validate-docs\scripts\validate_docs.py`
  - Result: all validations passed.
- `git diff --check`
  - Result: no whitespace errors; only CRLF conversion warnings from Git on Windows.
- `Invoke-WebRequest -UseBasicParsing http://127.0.0.1:5173/admin/payments -TimeoutSec 10`
  - Result: HTTP 200 from the Vite dev server.
- `npm run format`
  - Result: failed due existing repository-wide Prettier baseline warnings in 151 files; not part of this WI gate and not auto-formatted to avoid unrelated churn.

## Tests

- Frontend TypeScript check:
  - `npm run typecheck`
- Frontend ESLint:
  - `npm run lint`
- Frontend Vitest:
  - `npm test`
- Frontend production build:
  - `npm run build`
- Docs validation:
  - Tier 0 docs, internal links, traceability IDs, and document index all passed.

## Risks / Rollback

- Risks:
  - Incident status changes are operational workflow metadata only; operators still need a separate approved process for refund, entitlement correction, or subscription mutation.
  - The admin screen depends on the backend admin incident APIs introduced in WI-20260524-ATS-002.
  - Repository-wide Prettier formatting remains a separate baseline issue.
- Rollback:
  - Revert this WI commit.
  - Restore the admin menu label and payment screen to the previous payment read-only tabs.
  - Restore SR/UI/runbook wording that described incident workflow as API-only if the frontend tab is rolled back.

## Follow-ups

- Refund/receipt/settlement/tax invoice operations remain separate REQ/SR scope.
- Audited admin payment/subscription mutation tools remain separate REQ/SR scope.
- Toss webhook or deeper provider event ingestion remains separate REQ/SR scope.
- Multi-PG expansion remains separate REQ/SR scope.
- Multi-server scheduler lock should be reopened only before more than one application instance can execute renewal/reconciliation schedulers.
