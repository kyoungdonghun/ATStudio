[WI HEADER]
WI ID: WI-20260714-ATS-004
REQ: REQ-20260714-ATS-001
Agent: se
Depends On: WI-20260714-ATS-001, WI-20260714-ATS-002
Blocks: WI-20260714-ATS-005, WI-20260714-ATS-006, WI-20260714-ATS-007, WI-20260714-ATS-018, WI-20260714-ATS-021

[WI SUMMARY]
Why: Establish the Java/MySQL payment-command schema and audit-ENUM foundation required by P1-05 through P1-09.
Scope (in/out):
- In: PaymentOrder command/status fields, finalization uniqueness mappings, exact fresh DDL, ordered manual patch, and focused model/schema-contract tests.
- In: P1-05 audit action/target ENUM alignment.
- Out: Applying DDL to any DB, legacy-row disposition, Provider calls, command orchestration, renewal behavior, and refund behavior.
DoD:
- Java mappings and fresh schema implement the approved fields/statuses/constraints.
- `20260714_payment_db_integrity.sql` is idempotent where practical, preflight-oriented, and never auto-deletes/rewrites ambiguous ledger data.
- Existing H2 tests remain usable; MySQL proof is deferred to WI-021.
Constraints/Forbidden:
- Do not create/delete/connect to a disposable DB or apply schema changes.
- Do not edit runtime logs or expose secrets.
- Do not modify service orchestration owned by WI-005 through WI-007.
- You are not alone in the codebase; never revert concurrent edits.

[ACCEPTANCE CRITERIA]
Functional:
- [ ] `PaymentOrderStatus` supports `PROCESSING`, `PROVIDER_SUCCEEDED`, and `PENDING_PROVIDER_CONFIRMATION`.
- [ ] `PaymentOrder` maps command key, billing period start, provider attempt/idempotency key, and processing start time.
- [ ] `subscription_payments` enforces one payment per order and one local payment per provider transaction when non-null.
- [ ] Fresh DDL audit ENUMs contain every current Java audit action and target.
- [ ] Manual patch has explicit preflight/abort guidance and ordered changes without destructive cleanup.
Quality:
- [ ] Focused entity/repository/schema contract tests pass.
- [ ] `gradlew.bat compileJava` and `git diff --check` pass.

[INPUT POINTERS]
Tier 0:
- docs/standards/core-principles.md
- docs/standards/development-standards.md
Tier 1:
- docs/policies/quality-gates.md
Tier 2 / Context:
- deliverables/user/REQ-20260714-ATS-001.md
- docs/audit/p1-remediation-trace-matrix-20260714.md
- docs/design/p1-payment-db-integrity-design.md
- src/main/resources/schema.sql
- src/main/resources/db/manual/
- src/main/java/com/atstudio/atstudio/entity/PaymentOrder.java
- src/main/java/com/atstudio/atstudio/entity/SubscriptionPayment.java
- src/main/java/com/atstudio/atstudio/entity/enums/PaymentOrderStatus.java
- src/main/java/com/atstudio/atstudio/entity/enums/PaymentOperationAuditAction.java
- src/main/java/com/atstudio/atstudio/entity/enums/PaymentOperationAuditTargetType.java

[OUTPUT CONTRACT]
User-facing -> deliverables/user/WI-20260714-ATS-004-summary.md (Korean)
Agent-facing -> deliverables/agent/WI-20260714-ATS-004-evidence-pack.md
Handoff -> deliverables/agent/WI-20260714-ATS-004-handoff.md
Implementation -> only the owned entity/ENUM/schema/manual-patch and focused test files listed by the worker before editing.

[TRACEABILITY REQUIREMENTS]
Evidence pointers and exact commands: Required
Tests: focused Gradle tests plus compile
Rollback: application mappings first; retain ledger evidence and expanded ENUMs on deployed DBs
