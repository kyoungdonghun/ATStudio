---
version: 1.0
last_updated: 2026-07-17
project: ATS
owner: Documentation Ops
category: remediation
status: complete
dependencies:
  - path: ../WI-20260717-ATS-011-handoff.md
    reason: Authorized scope and output contract
---

# WI-20260717-ATS-011 Remediation

## Changes

- `application-local.example.yml` now keeps the three credential fields reference-only, with no literal fallback.
- `V1BackendBaselineContractTest` now loads the example YAML and enforces the exact reference-only contract for each field.
- No production code, runtime product configuration, ignored local configuration, or unrelated test was changed by Documentation Ops.

## Environment Variable References

| Configuration field | Required reference |
|---|---|
| `spring.datasource.password` | `DB_PASSWORD` |
| `jwt.secret` | `JWT_SECRET` |
| `app.payment.billing.encryption-keys[0].secret` | `PAYMENT_BILLING_KEY_0_SECRET` |

All three references were verified without reading, recording, or emitting credential values.

## Focused Verification

Command:

```text
gradlew.bat test --tests "com.atstudio.atstudio.config.V1BackendBaselineContractTest.localExampleCredentialFieldsAreReferenceOnly" --console=plain
```

Result: **PASS** (`BUILD SUCCESSFUL`). The focused contract test proves that all three fields contain only their required environment-variable references and have no fallback.

## Remaining Candidate Classification

The remaining value-suppressing scan candidates are resolved by role and path without reproducing candidate values:

| Classification | Paths | Disposition |
|---|---|---|
| Runtime variable references | `deliverables/agent/WI-20260717-ATS-004/run-v1-mysql-proof.ps1` | Non-literal runtime input references; retained. |
| Test fixtures | `src/test/java/com/atstudio/atstudio/security/JwtTokenProviderTest.java`, `frontend/src/api/authContracts.test.ts`, `frontend/src/test/coverage/publicAuthShell.coverage.test.tsx` | Test-only fixture material; retained and isolated from production configuration. |
| Isolated disposable-DB proof fixture | `deliverables/agent/WI-20260717-ATS-004/V1MysqlProofManager.java` | Proof-only fixture for an isolated disposable database; retained outside runtime product code. |

The prior unresolved events in `application-local.example.yml` are remediated by the three reference-only placeholders above. No unresolved candidate remains within this WI classification scope.

## Rollback

To roll back this WI, restore the pre-WI versions of `application-local.example.yml` and `src/test/java/com/atstudio/atstudio/config/V1BackendBaselineContractTest.java`, then remove this remediation record. Do not restore any credential literal; if rollback is required, preserve the reference-only secret policy or obtain a separately approved secure replacement. No database, runtime, Git ref, or production-secret rollback applies.

PASS
