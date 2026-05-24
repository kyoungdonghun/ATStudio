# Evidence Pack: WI-20260524-ATS-002

## Summary (one-liner)
- Added persistent payment reconciliation incidents, admin workflow APIs, optional operator email notification, and synchronized payment operations documentation.

## Scope / DoD Check
- [x] Scheduled reconciliation creates or updates `payment_reconciliation_incidents`.
- [x] Duplicate detection updates occurrence count and last detected timestamp instead of creating duplicate rows.
- [x] Admins can list incidents with optional status filtering.
- [x] Admins can change status to `OPEN`, `ACKNOWLEDGED`, `RESOLVED`, or `IGNORED`.
- [x] Optional operator email notification is disabled by default and requires explicit configuration.
- [x] Focused backend tests, full backend tests, and docs validation pass.

## Reference Documents (Tier 0-2)

**Injected Context**:

| Tier | Document | Reason |
|------|----------|--------|
| 0 | docs/standards/core-principles.md | Constitution and execution gate |
| 0 | docs/standards/development-standards.md | Java/Spring implementation standard |
| 0 | docs/standards/documentation-standards.md | Documentation consistency |
| 0 | docs/standards/glossary.md | Domain terminology |
| 1 | docs/policies/security-policy.md | Sensitive payment data boundary |
| 2 | deliverables/user/REQ-20260524-ATS-002.md | Approved requirement |
| 2 | docs/SR/SR-93.md | Production readiness source SR |
| 2 | docs/design/payment-operations-runbook.md | Operations runbook |
| 2 | docs/design/payment-integration-design.md | Payment design |
| 2 | docs/design/api-spec.md | API contract |
| 2 | docs/design/db-schema.md | DB contract |

**Injection Rules Applied**:
- Rule source: `.claude/config/context-injection-rules.json`
- Assignees: se/docops/pg/qa-integ
- Task type: backend implementation, payment operations, docs sync

## Evidence Pointers

- `src/main/java/com/atstudio/atstudio/entity/PaymentReconciliationIncident.java:49` — new incident entity with workflow state, severity, dedupe key, occurrence metadata, notification and resolution timestamps.
- `src/main/java/com/atstudio/atstudio/entity/PaymentReconciliationIncident.java:134` — repeated detection update and resolved-incident reopen behavior.
- `src/main/java/com/atstudio/atstudio/service/PaymentReconciliationService.java:50` — scheduled reconciliation now records incident issues after local/provider checks.
- `src/main/java/com/atstudio/atstudio/service/PaymentReconciliationService.java:283` — support-safe local/provider issue records include IDs and non-sensitive status/amount fields.
- `src/main/java/com/atstudio/atstudio/service/PaymentReconciliationIncidentService.java:40` — local/provider results are persisted through the incident upsert path.
- `src/main/java/com/atstudio/atstudio/service/PaymentReconciliationIncidentService.java:203` — optional operator email notification guard and safe details payload.
- `src/main/java/com/atstudio/atstudio/controller/AdminPaymentController.java:63` — admin incident list endpoint.
- `src/main/java/com/atstudio/atstudio/controller/AdminPaymentController.java:72` — admin incident status update endpoint.
- `src/main/java/com/atstudio/atstudio/service/AdminPaymentIncidentService.java:18` — admin list/update application service.
- `src/main/java/com/atstudio/atstudio/service/EmailService.java:128` — operator incident email method.
- `src/main/java/com/atstudio/atstudio/config/PaymentProperties.java:48` and `src/main/resources/application.yml:115` — operations notification configuration, disabled by default.
- `src/main/resources/schema.sql:466` — manual schema for `payment_reconciliation_incidents`.
- `docs/design/api-spec.md:1360` — admin incident API contract entries.
- `docs/design/db-schema.md:397` — DB schema documentation for the incident table.
- `docs/design/payment-operations-runbook.md:76` — runbook admin incident workflow.
- `docs/SR/SR-93.md:47` — SR current implementation snapshot updated for incident storage and notification.

## Commands & Outputs

- `.\gradlew.bat test --tests "com.atstudio.atstudio.service.PaymentReconciliationServiceTest" --tests "com.atstudio.atstudio.service.PaymentReconciliationIncidentServiceTest"`
  - Result: `BUILD SUCCESSFUL in 6s`
- `.\gradlew.bat test`
  - Result: `BUILD SUCCESSFUL in 55s`
- `python .agents\skills\validate-docs\scripts\validate_docs.py`
  - Result: `All validations passed`
- `git diff --check`
  - Result: no whitespace errors; only CRLF conversion warnings from Git on Windows

## Tests

- Focused JUnit tests:
  - `PaymentReconciliationServiceTest`
  - `PaymentReconciliationIncidentServiceTest`
- Full backend JUnit test suite:
  - `.\gradlew.bat test`
- Docs validation:
  - Tier 0 docs, internal links, traceability IDs, and document index all passed.

## Risks / Rollback

- Risks:
  - New table requires production migration before enabling incident persistence in a manually managed DB.
  - API-only incident workflow may be enough for operations tooling but no dedicated admin frontend exists yet.
  - Email notification depends on SMTP and explicit operations environment variables.
- Rollback:
  - Revert this WI commit.
  - Drop `payment_reconciliation_incidents` only if already applied to a non-production DB and after confirming no required incident data must be retained.
  - Restore API/DB/runbook/SR counts to the previous 119 API / 30 table state if the feature is rolled back.

## Follow-ups

- Admin frontend UI for reconciliation incident workflow.
- Slack/SMS/in-app operations notification channels if email is insufficient.
- Refund/cancel/entitlement correction runbooks and audited admin mutation APIs.
- Multi-server scheduler lock before running more than one application instance.
