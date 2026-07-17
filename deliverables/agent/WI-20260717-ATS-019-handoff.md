[WI HEADER]
WI ID: WI-20260717-ATS-019
REQ: REQ-20260716-ATS-004
Agent: se
Depends On: WI-20260717-ATS-017 and WI-20260717-ATS-018 startup finding
Blocks: WI-20260717-ATS-018 acceptance execution resume

[WI SUMMARY]
Why: Align the operator-controlled acceptance lifecycle environment allowlist with the current V1 billing-key V2 configuration contract so the verified application can start fail-closed without relying on a removed legacy secret name.
Scope (in/out): In scope are the acceptance lifecycle backend environment allowlist and its PowerShell contract tests. Add the current required V2 active key ID and first key-ring ID/secret names plus the current optional payment scheduler zone, remove the obsolete legacy billing-key encryption secret name, and prove secret-safe child-process propagation/restoration. Out of scope are payment crypto implementation, application configuration semantics, provider behavior, source schema, product policy, external bundle values, docs unrelated to the exact runtime contract, real payment, branch operations, and push.
DoD: The lifecycle accepts and propagates `PAYMENT_BILLING_KEY_ACTIVE_KEY_ID`, `PAYMENT_BILLING_KEY_0_ID`, `PAYMENT_BILLING_KEY_0_SECRET`, and `APP_PAYMENT_SCHEDULER_ZONE`; it rejects `PAYMENT_BILLING_KEY_ENCRYPTION_SECRET`; required/optional name tests, environment isolation tests, dry-run tests, targeted backend acceptance configuration tests, and diff checks pass; no secret value appears in output.
Constraints/Forbidden: Do not weaken `BillingKeyCrypto` or `AcceptanceStartupGuard`. Do not introduce default/placeholder secrets. Do not print or read the operator external bundle. Do not edit application-local.yml, database, business code, docs beyond an exact contract correction if strictly required, Git refs/index, or unrelated artifacts. You are not alone in the codebase; do not revert other changes.

[ACCEPTANCE CRITERIA]
Functional:
- [ ] Current V2 key ID/key-ring environment names are allowlisted and reach only the acceptance backend child process.
- [ ] `APP_PAYMENT_SCHEDULER_ZONE` is allowlisted for the current payment runtime contract.
- [ ] Removed `PAYMENT_BILLING_KEY_ENCRYPTION_SECRET` is no longer allowlisted and a bundle containing it is rejected.
- [ ] Required application-side fail-closed validation remains unchanged.
Performance:
- [ ] Not applicable.
Quality:
- [ ] `scripts/acceptance/test-backend-environment.ps1` passes with explicit new/removed-name assertions.
- [ ] `scripts/acceptance/test-dry-run.ps1` passes.
- [ ] Focused Java acceptance/billing configuration tests pass.
- [ ] `git diff --check` passes and no secret value is emitted.

[INPUT POINTERS]
Tier 0 (Constitution - Required for all):
- docs/standards/core-principles.md
- docs/standards/development-standards.md

Tier 1 (Policies - Security and quality):
- docs/policies/security-policy.md
- docs/policies/quality-gates.md

Tier 2 (Current design contracts):
- docs/design/payment-integration-design.md
- docs/design/payment-operations-runbook.md
- docs/design/db-schema.md

REQ/Context Docs:
- deliverables/user/REQ-20260716-ATS-004.md
- deliverables/agent/WI-20260717-ATS-018-handoff.md

Files:
- scripts/acceptance/AcceptanceLifecycle.psm1
- scripts/acceptance/test-backend-environment.ps1
- scripts/acceptance/test-dry-run.ps1
- src/main/resources/application.yml
- src/main/resources/application-acceptance.yml
- src/main/java/com/atstudio/atstudio/config/PaymentProperties.java
- src/main/java/com/atstudio/atstudio/config/AcceptanceStartupGuard.java
- src/main/java/com/atstudio/atstudio/service/payment/billing/BillingKeyCrypto.java
- src/test/java/com/atstudio/atstudio/config/AcceptanceStartupGuardTest.java
- src/test/java/com/atstudio/atstudio/config/V1BackendBaselineContractTest.java
- src/test/java/com/atstudio/atstudio/service/payment/billing/BillingKeyCryptoTest.java

Repro/Logs:
- WI-018 first current-DB startup log: repo-external runtime `20260717T063637Z/backend.out.log`; cite sanitized failure class/message only.
- `powershell -ExecutionPolicy Bypass -File scripts/acceptance/test-backend-environment.ps1`
- `powershell -ExecutionPolicy Bypass -File scripts/acceptance/test-dry-run.ps1`
- `gradlew.bat test --tests "com.atstudio.atstudio.config.AcceptanceStartupGuardTest" --tests "com.atstudio.atstudio.config.V1BackendBaselineContractTest" --tests "com.atstudio.atstudio.service.payment.billing.BillingKeyCryptoTest"`

[OUTPUT CONTRACT]
User-facing -> deliverables/user/WI-20260717-ATS-019-summary.md :
- Korean summary of the root cause, exact contract correction, tests, risk, and WI-018 resume state.
Agent-facing -> deliverables/agent/WI-20260717-ATS-019-evidence-pack.md :
- Patch pointers, sanitized failure evidence, test commands/results, negative assertions, rollback, and follow-up WI.
Handoff Packet -> deliverables/agent/WI-20260717-ATS-019-handoff.md :
- This packet.

[TRACEABILITY REQUIREMENTS]
Evidence pointers (files/lines/commands/logs): Required; do not include bundle contents or secret values.
Tests: Record exact PowerShell and Java test results plus negative legacy-name coverage.
Rollback: Revert only WI-019 source/test changes. Never restore the obsolete secret contract as an application runtime dependency.
