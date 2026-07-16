---
version: 1.0
last_updated: 2026-07-16
project: ATS
owner: se
category: work-summary
status: complete
dependencies:
  - path: ../agent/WI-20260716-ATS-031-handoff.md
    reason: Approved WI scope and constraints
  - path: ../agent/WI-20260716-ATS-030-evidence-pack.md
    reason: Reopened finding evidence
---

# WI-20260716-ATS-031 Summary

> Purpose: Report closure of the three findings reopened by WI-030.

## 1. Findings Closed

| Finding | Result | Closure |
|---|---|---|
| `F-025-03` | `CLOSED` | Entitlement correction execution now requires the locked agreement revision to predate correction creation. A completed `ACTIVE -> re-registration -> ACTIVE` replacement is stale and remains uncancelled. |
| `F-025-05` | `CLOSED` | Retained `transactionId`, `paymentKey`, and `orderId` labels using `=` or `:` with surrounding whitespace are replaced with deterministic `REF-*` values before ADMIN serialization. |
| `F-027-03` | `CLOSED` | The glossary and Track use case now use the implemented `expiresAt` wire field. |

## 2. Implementation

- Reused `BillingAgreement.updatedAt` and `PaymentEntitlementCorrection.createdAt`; no schema field or migration was added.
- Agreement timestamps must be non-null and strictly ordered: `agreement.updatedAt < correction.createdAt`.
- Equal or missing timestamps are rejected as stale because they cannot prove that the agreement predates the correction.
- Existing valid corrections continue to execute when the agreement status matches and its revision predates correction creation.
- Existing ADMIN DTO mappers continue to use the centralized `ProviderSupportReference` sanitizer; no DTO shape changed.

## 3. Verification

Focused JUnit command:

```powershell
.\gradlew.bat test --tests "com.atstudio.atstudio.service.AdminPaymentEntitlementCorrectionServiceTest" --tests "com.atstudio.atstudio.dto.payment.ProviderSupportReferenceTest" --tests "com.atstudio.atstudio.dto.payment.AdminProviderIdentifierContractTest"
```

- PASS: 3 classes, 24 tests, 0 failures, 0 errors, 0 skipped.
- PASS: documentation validation; Tier 0 documents, internal links, 419 supported traceability IDs, and document index.
- PASS: `currentPeriodEnd` is absent from the two owned documentation paths.
- PASS: tracked and untracked owned-slice whitespace checks produced no errors.

## 4. Changed Paths

- `src/main/java/com/atstudio/atstudio/service/AdminPaymentEntitlementCorrectionService.java`
- `src/main/java/com/atstudio/atstudio/service/payment/ProviderSupportReference.java`
- `src/test/java/com/atstudio/atstudio/service/AdminPaymentEntitlementCorrectionServiceTest.java`
- `src/test/java/com/atstudio/atstudio/dto/payment/ProviderSupportReferenceTest.java`
- `src/test/java/com/atstudio/atstudio/dto/payment/AdminProviderIdentifierContractTest.java`
- `docs/standards/glossary.md`
- `docs/design/usecase/sound-track.md`
- `deliverables/user/WI-20260716-ATS-031-summary.md`
- `deliverables/agent/WI-20260716-ATS-031-evidence-pack.md`

## 5. Residual Risks

- Timestamp equality is deliberately fail-closed. A legitimate correction created in the same persisted timestamp unit as the latest agreement update must be recreated after the revision is unambiguous.
- Focused unit and documentation verification does not replace retained-MySQL or live Provider evidence. Those environments were outside this WI.
- Unlabelled arbitrary legacy prose is not heuristically scrubbed. New free text remains prohibited from retaining raw Provider identifiers, and the supported retained-label paths are covered here.

No schema, migration, data, Provider state, secret, client worktree/runtime, Git index/history, or `frontend/tsconfig.tsbuildinfo` change was made.

## Related Documents

- [WI-031 Handoff](../agent/WI-20260716-ATS-031-handoff.md): Scope and acceptance criteria.
- [WI-031 Evidence Pack](../agent/WI-20260716-ATS-031-evidence-pack.md): Reproducible source and test evidence.
- [WI-030 Evidence Pack](../agent/WI-20260716-ATS-030-evidence-pack.md): Reopened findings.
