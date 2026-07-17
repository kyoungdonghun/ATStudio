# Evidence Pack: WI-20260717-ATS-019

## Summary (one-liner)
- Aligned the acceptance backend environment allowlist with the current V2 billing-key and payment scheduler-zone contract while preserving application fail-closed behavior.

## Scope / DoD Check
- DoD items:
  - [x] Allowlisted `PAYMENT_BILLING_KEY_ACTIVE_KEY_ID`, `PAYMENT_BILLING_KEY_0_ID`, `PAYMENT_BILLING_KEY_0_SECRET`, and `APP_PAYMENT_SCHEDULER_ZONE` as optional operator-supplied backend variables.
  - [x] Removed `PAYMENT_BILLING_KEY_ENCRYPTION_SECRET` from the lifecycle allowlist and proved that a bundle containing it is rejected.
  - [x] Proved that all four current names reach only the backend child process and are excluded from tunnel/frontend children.
  - [x] Proved that the launcher process environment restores all four current names after child creation.
  - [x] Kept `BillingKeyCrypto`, `AcceptanceStartupGuard`, application configuration, business code, and database state unchanged.
  - [x] Passed the two PowerShell contract tests, three focused Java test classes, and `git diff --check`.
  - [x] Emitted no operator secret value, external bundle content, or raw runtime URL.

## Reference Documents (Tier 0-2)

**Injected Context** (from WI Handoff Packet):

| Tier | Document | Reason |
|------|----------|--------|
| 0 | `docs/standards/core-principles.md` | Constitution and traceability baseline |
| 0 | `docs/standards/development-standards.md` | Implementation and test standards for assignee `se` |
| 1 | `docs/policies/security-policy.md` | Secret-safe environment and logging requirements |
| 1 | `docs/policies/quality-gates.md` | Verification and rollback requirements |
| 2 | `docs/design/payment-integration-design.md` | Current V2 billing-key configuration contract |
| 2 | `docs/design/payment-operations-runbook.md` | Current scheduler-zone and operator configuration contract |
| 2 | `docs/design/db-schema.md` | Fresh-only V1 database baseline boundary |

**Injection Rules Applied**:
- Rule source: `AGENTS.md` and `deliverables/agent/WI-20260717-ATS-019-handoff.md`.
- Assignee: `se`.
- Task type: implementation, security-sensitive configuration, and testing.
- Scope was limited to the two approved PowerShell files plus this Evidence Pack and the user summary.

## Root Cause Boundary
- Historical database condition: `deliverables/agent/WI-20260717-ATS-015-evidence-pack.md:71` recorded that the then-current database could pass Hibernate validation but was not physically identical to the fresh V1 manifest.
- Database resolution: `deliverables/agent/WI-20260717-ATS-016-evidence-pack.md:4` records recreation of the approved local database to the exact fresh V1 baseline. WI-019 did not inspect or mutate the database.
- WI-019 direct cause: the acceptance launcher still allowlisted the removed legacy billing-key variable and rejected the current V2 active-key/key-ring names. This launcher contract drift prevented the operator-controlled V2 configuration from reaching the acceptance backend child.
- Application boundary: the application-side fail-closed behavior was not the defect and remains unchanged. No default, placeholder, legacy decrypt path, or validation bypass was introduced.
- Evidence safety: no operator bundle content, secret value, external runtime log body, or raw URL was read into or copied into this Evidence Pack.

## Evidence Pointers (required)
- Files changed:
  - `scripts/acceptance/AcceptanceLifecycle.psm1:17-57` - replaced the obsolete optional name with the four current exact names.
  - `scripts/acceptance/test-backend-environment.ps1:123-148` - asserts the four names are optional/allowlisted and the obsolete name is absent.
  - `scripts/acceptance/test-backend-environment.ps1:150-166` - proves a synthetic bundle containing the four current names is accepted.
  - `scripts/acceptance/test-backend-environment.ps1:196-204` - proves a bundle containing the obsolete name is rejected without protected-text disclosure.
  - `scripts/acceptance/test-backend-environment.ps1:229-371` - proves backend-only propagation, tunnel/frontend isolation, and launcher restoration for all four current names.
  - `scripts/acceptance/test-backend-environment.ps1:512-522` - exposes explicit current-name acceptance and obsolete-name rejection result checks.
- Deliberately unchanged:
  - `scripts/acceptance/test-dry-run.ps1`.
  - `src/main/java/com/atstudio/atstudio/config/AcceptanceStartupGuard.java`.
  - `src/main/java/com/atstudio/atstudio/service/payment/billing/BillingKeyCrypto.java`.
  - Application configuration, schema, business code, Git refs/index, and repo-external runtime data.
- Patch size:
  - `scripts/acceptance/AcceptanceLifecycle.psm1`: 4 insertions, 1 deletion.
  - `scripts/acceptance/test-backend-environment.ps1`: 82 insertions, 9 deletions.

## Commands & Outputs
- `powershell -ExecutionPolicy Bypass -File scripts/acceptance/test-backend-environment.ps1`
  - Result: PASS, `status=passed`.
  - Explicit checks include `current-v2-and-scheduler-name-acceptance`, `obsolete-billing-key-name-rejection`, child-process isolation, and backend-environment restoration.
- `powershell -ExecutionPolicy Bypass -File scripts/acceptance/test-dry-run.ps1`
  - Result: PASS, `status=passed`.
  - The analyzer reported `not-installed`; parser and all runtime contract checks still passed, including secret-free dry-run output.
- `.\gradlew.bat test --tests "com.atstudio.atstudio.config.AcceptanceStartupGuardTest" --tests "com.atstudio.atstudio.config.V1BackendBaselineContractTest" --tests "com.atstudio.atstudio.service.payment.billing.BillingKeyCryptoTest"`
  - Result: PASS, `BUILD SUCCESSFUL` in 7 seconds.
  - Focused classes: 3; Gradle tasks: 5 actionable, 1 executed, 4 up-to-date.
- `git diff --check`
  - Result: PASS, exit code 0; no whitespace error.
  - Git reported line-ending normalization warnings for existing working-tree files; no file was rewritten to address those unrelated warnings.

## Tests
- PowerShell contract tests: 2/2 passed.
- Focused Java test classes: 3/3 passed.
- Negative coverage explicitly rejects `PAYMENT_BILLING_KEY_ENCRYPTION_SECRET`.
- Isolation coverage explicitly proves the four current names are present only for the backend spawn and restored in the launcher afterward.
- Secret-safety coverage used synthetic markers only and did not print their values in the pass output.

## Risks / Rollback
- Risks:
  - This patch corrects launcher allowlisting only; it does not prove a complete live acceptance run or production readiness.
  - A real acceptance restart still depends on a valid operator-managed external bundle and the already established fresh V1 database baseline.
  - Concurrent unrelated working-tree changes remain outside WI-019 and were not reverted.
- Rollback:
  - Reverse only the WI-019 hunks in `scripts/acceptance/AcceptanceLifecycle.psm1` and `scripts/acceptance/test-backend-environment.ps1` after confirming the current diff.
  - Do not revert unrelated changes or modify application fail-closed validation.
  - A rollback would re-block WI-018 acceptance startup at the launcher contract; it must not be interpreted as restoring the obsolete name as an application runtime dependency.

## Follow-ups
- Resume `WI-20260717-ATS-018` acceptance execution using the current operator-managed V2 bundle and fresh V1 database baseline.
- WI-018 must still verify startup readiness, local/public proxy behavior, role/API/UI smoke coverage, secret-safe logs, and final runtime ownership/cleanup state.
- No real payment, provider mutation, production deployment, or production-readiness claim is authorized by WI-019.
