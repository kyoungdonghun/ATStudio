[WI HEADER]
WI ID: WI-20260524-ATS-002
REQ: REQ-20260524-ATS-002
Agent: se/docops/pg/qa-integ
Depends On: WI-20260524-ATS-001
Blocks: -

[WI SUMMARY]
Why: Reconciliation currently logs mismatches and exposes on-demand diagnostics, but operators can still miss issues unless incidents are persisted and surfaced.
Scope (in/out): Add persistent reconciliation incidents, admin list/status APIs, optional operator email notification, docs, and tests. Exclude automatic refund/cancel/subscription correction, Slack/SMS, frontend admin UI, multi-server lock, webhook implementation, and multi-PG.
DoD: Scheduler can create/update deduplicated incidents; admins can list and update incident workflow status; optional email is disabled by default; docs/tests pass.
Constraints/Forbidden: No raw billing keys, auth keys, Toss secrets, raw card data, or raw provider payload in incidents/API/emails/logs. Incident workflow must not mutate payment, billing agreement, or subscription state.

[ACCEPTANCE CRITERIA]
Functional:
- [ ] Reconciliation mismatch creates or updates a `payment_reconciliation_incidents` row.
- [ ] Duplicate detection updates occurrence count and last detected timestamp instead of creating duplicate rows.
- [ ] Admin can list incidents with filters.
- [ ] Admin can change status to `OPEN`, `ACKNOWLEDGED`, `RESOLVED`, or `IGNORED`.
- [ ] Optional operator email notification sends only when explicitly enabled and configured.
Quality:
- [ ] Focused backend tests pass.
- [ ] Full backend tests pass.
- [ ] Docs validation passes.

[INPUT POINTERS]
Tier 0:
- docs/standards/core-principles.md
- docs/standards/development-standards.md
- docs/standards/documentation-standards.md
- docs/standards/glossary.md

Tier 1:
- docs/policies/security-policy.md

REQ/Context Docs:
- deliverables/user/REQ-20260524-ATS-002.md
- docs/SR/SR-93.md
- docs/design/payment-operations-runbook.md
- docs/design/payment-integration-design.md
- docs/design/api-spec.md
- docs/design/db-schema.md

Files:
- src/main/java/com/atstudio/atstudio/service/PaymentReconciliationService.java
- src/main/java/com/atstudio/atstudio/controller/AdminPaymentController.java
- src/main/java/com/atstudio/atstudio/service/EmailService.java
- src/main/resources/schema.sql
- src/main/resources/application.yml

[OUTPUT CONTRACT]
User-facing -> deliverables/user/WI-20260524-ATS-002-summary.md
Agent-facing -> deliverables/agent/WI-20260524-ATS-002-evidence-pack.md
Handoff Packet -> deliverables/agent/WI-20260524-ATS-002-handoff.md

[TRACEABILITY REQUIREMENTS]
Evidence pointers: required
Tests: focused tests, full backend test, docs validation
Rollback: revert incident entity/repository/service/controller/docs/schema/config changes

