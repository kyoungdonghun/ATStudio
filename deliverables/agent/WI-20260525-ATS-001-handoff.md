[WI HEADER]
WI ID: WI-20260525-ATS-001
REQ: REQ-20260525-ATS-001
Agent: se/docops/qa-fe
Depends On: WI-20260524-ATS-002
Blocks: -

[WI SUMMARY]
Why: Payment reconciliation incidents are persisted and workflow APIs exist, but operators still need direct API calls. The admin payment screen needs an incident operations tab.
Scope (in/out): Add incident list/filter/status update UI to existing `/admin/payments`, add frontend API types/functions, update SR/UI docs and deliverables. Exclude payment/refund/subscription mutation, webhook, Slack/SMS/in-app alerts, multi-server lock, multi-PG, backend schema/API additions.
DoD: Admin can view persisted incidents, filter by status, change workflow status with a note, and no sensitive payment credentials are displayed. Frontend typecheck/lint/build and docs validation pass.
Constraints/Forbidden: Do not expose raw billing keys, auth keys, customer keys, Toss secrets, raw card data, or raw provider payloads. Do not add UI actions that mutate payment orders, billing agreements, subscriptions, or provider state.

[ACCEPTANCE CRITERIA]
Functional:
- [ ] `/admin/payments` includes an incident tab.
- [ ] Incident tab lists persisted reconciliation incidents from `/api/admin/payments/reconciliation-incidents`.
- [ ] Status filter supports all statuses and all incidents.
- [ ] Status update calls `/api/admin/payments/reconciliation-incidents/{incidentId}/status`.
- [ ] Incident rows show support-safe issue, severity, order, provider, local/provider state, amount, occurrence, timestamps, and note fields.
Quality:
- [ ] `npm run typecheck` passes.
- [ ] `npm run lint` passes.
- [ ] `npm run build` passes.
- [ ] Docs validation passes.

[INPUT POINTERS]
Tier 0:
- docs/standards/core-principles.md
- docs/standards/development-standards.md
- docs/standards/documentation-standards.md
- docs/standards/glossary.md

Tier 1:
- docs/policies/security-policy.md

Tier 2:
- docs/standards/frontend-standards.md
- .agents/skills/react-best-practices/SKILL.md
- docs/design/api-spec.md
- docs/SR/SR-93.md
- docs/ui/screen-flow.md

REQ/Context Docs:
- deliverables/user/REQ-20260525-ATS-001.md
- deliverables/user/REQ-20260524-ATS-002.md
- deliverables/agent/WI-20260524-ATS-002-evidence-pack.md

Files:
- frontend/src/pages/admin/PaymentReadOnlyPage.tsx
- frontend/src/pages/admin/PaymentReadOnlyPage.module.css
- frontend/src/api/admin.ts
- frontend/src/layouts/AdminLayout.tsx

[OUTPUT CONTRACT]
User-facing -> deliverables/user/WI-20260525-ATS-001-summary.md
Agent-facing -> deliverables/agent/WI-20260525-ATS-001-evidence-pack.md
Handoff Packet -> deliverables/agent/WI-20260525-ATS-001-handoff.md

[TRACEABILITY REQUIREMENTS]
Evidence pointers: required
Tests: frontend typecheck, lint, build; docs validation
Rollback: revert UI/API/doc/deliverable changes from this WI
