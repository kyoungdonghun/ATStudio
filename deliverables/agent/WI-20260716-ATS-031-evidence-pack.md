---
version: 1.0
last_updated: 2026-07-16
project: ATS
owner: se
category: evidence-pack
status: complete
dependencies:
  - path: WI-20260716-ATS-031-handoff.md
    reason: Approved WI scope and constraints
  - path: WI-20260716-ATS-030-evidence-pack.md
    reason: Reopened finding evidence
---

# Evidence Pack: WI-20260716-ATS-031

## Summary

- Closed `F-025-03`, `F-025-05`, and `F-027-03` with a no-schema revision fence, response-boundary Provider identifier sanitization, focused regression tests, and corrected `expiresAt` documentation.

## Scope / DoD Check

- [x] A completed same-status billing-agreement replacement makes an older correction stale.
- [x] Stale execution is rejected before subscription, agreement, correction-status, or audit mutation.
- [x] An unchanged agreement with a strictly older revision still permits a valid correction.
- [x] Null and equal revision timestamps fail closed.
- [x] `transactionId`, `paymentKey`, and `orderId` labels using `=` or `:` with reasonable whitespace serialize without full or partial raw values.
- [x] Both affected documents use `expiresAt`.
- [x] Focused JUnit, documentation validation, terminology search, and owned-slice whitespace checks pass.
- [x] No schema, migration, data, Provider, secret, client runtime/worktree, Git index/history, or generated frontend metadata mutation was performed.

## Reference Documents

| Tier | Document | Reason |
|---|---|---|
| 0 | `docs/standards/core-principles.md` | Constitution and traceability |
| 0 | `docs/standards/development-standards.md` | Java and test standards |
| 0 | `docs/standards/documentation-standards.md` | Deliverable standards |
| 0 | `docs/standards/glossary.md` | Canonical terminology |
| 1 | `docs/policies/security-policy.md` | ADMIN Provider identifier boundary |
| 1 | `docs/policies/quality-gates.md` | Verification requirements |
| 2 | `docs/design/payment-integration-design.md` | Billing agreement and correction invariants |
| 2 | `docs/design/usecase/sound-track.md` | Service-enabled access wording |
| Context | `deliverables/user/REQ-20260716-ATS-002.md` | Approved remediation scope |
| Context | `deliverables/user/WI-20260716-ATS-030-summary.md` | Reopened finding summary |
| Context | `deliverables/agent/WI-20260716-ATS-030-evidence-pack.md` | Reproduction and source pointers |
| Context | `deliverables/agent/WI-20260716-ATS-028-evidence-pack.md` | Prior backend implementation evidence |
| Context | `deliverables/agent/WI-20260716-ATS-029-evidence-pack.md` | Prior frontend/document implementation evidence |

Injection source: `deliverables/agent/WI-20260716-ATS-031-handoff.md`; assignee `se`; task type implementation/test/document remediation.

## Evidence Pointers

### F-025-03 - Same-status agreement revision fence

- Execution checks the locked subscription and agreement snapshots before any processing mutation: `src/main/java/com/atstudio/atstudio/service/AdminPaymentEntitlementCorrectionService.java:209-217`.
- Agreement state requires status equality, non-null timestamps, and strict `updatedAt < createdAt`: `src/main/java/com/atstudio/atstudio/service/AdminPaymentEntitlementCorrectionService.java:347-362`.
- The unchanged valid correction path remains executable: `src/test/java/com/atstudio/atstudio/service/AdminPaymentEntitlementCorrectionServiceTest.java:207-249`.
- Completed `ACTIVE -> re-registration -> ACTIVE` replacement rejection and non-cancellation: `src/test/java/com/atstudio/atstudio/service/AdminPaymentEntitlementCorrectionServiceTest.java:304-341`.
- Equal and missing timestamp fail-closed coverage: `src/test/java/com/atstudio/atstudio/service/AdminPaymentEntitlementCorrectionServiceTest.java:342-390`.
- Deterministic before/after timestamp fixtures: `src/test/java/com/atstudio/atstudio/service/AdminPaymentEntitlementCorrectionServiceTest.java:62-65,474-477,567-576`.

No changes were required in `PaymentEntitlementCorrection` or `BillingAgreement`; both already inherit audited `createdAt` and `updatedAt` from `BaseEntity`.

### F-025-05 - Retained labelled Provider identifiers

- Supported labels now include `orderId`; supported separators are `=` and `:` with surrounding whitespace: `src/main/java/com/atstudio/atstudio/service/payment/ProviderSupportReference.java:13-16`.
- Sanitization preserves the label/separator and replaces the complete value with a deterministic support reference: `src/main/java/com/atstudio/atstudio/service/payment/ProviderSupportReference.java:39-53`.
- Audit and Incident DTO response boundaries use this sanitizer: `src/main/java/com/atstudio/atstudio/dto/payment/AdminPaymentOperationAuditLogResponse.java:49-53`; `src/main/java/com/atstudio/atstudio/dto/payment/AdminPaymentReconciliationIncidentResponse.java:62-70`.
- Utility coverage for all three labels, both separators, whitespace, and fragment removal: `src/test/java/com/atstudio/atstudio/dto/payment/ProviderSupportReferenceTest.java:37-73`.
- ADMIN audit/Incident serialization sentinels cover full and partial retained values: `src/test/java/com/atstudio/atstudio/dto/payment/AdminProviderIdentifierContractTest.java:167-211`.

### F-027-03 - Implemented expiry field

- Canonical SubscriberRoute definition uses `expiresAt`: `docs/standards/glossary.md:93`.
- SOUND-019 precondition uses `expiresAt`: `docs/design/usecase/sound-track.md:213-216`.

## Changed-File Inventory

| Path | WI-031 change |
|---|---|
| `src/main/java/com/atstudio/atstudio/service/AdminPaymentEntitlementCorrectionService.java` | Added strict no-schema agreement revision fence. |
| `src/main/java/com/atstudio/atstudio/service/payment/ProviderSupportReference.java` | Added `orderId`, colon separator, whitespace preservation, and complete-value replacement. |
| `src/test/java/com/atstudio/atstudio/service/AdminPaymentEntitlementCorrectionServiceTest.java` | Added same-status replacement and null/equal timestamp regression coverage; timestamped valid fixture. |
| `src/test/java/com/atstudio/atstudio/dto/payment/ProviderSupportReferenceTest.java` | Added label/separator/whitespace and raw-fragment coverage. |
| `src/test/java/com/atstudio/atstudio/dto/payment/AdminProviderIdentifierContractTest.java` | Added ADMIN audit/Incident serialization sentinels for colon and order labels. |
| `docs/standards/glossary.md` | Replaced `currentPeriodEnd` with `expiresAt`; updated document metadata. |
| `docs/design/usecase/sound-track.md` | Replaced `currentPeriodEnd` with `expiresAt`; updated SOUND-019 version. |
| `deliverables/user/WI-20260716-ATS-031-summary.md` | User-facing closure report. |
| `deliverables/agent/WI-20260716-ATS-031-evidence-pack.md` | Reproducible evidence and rollback. |

This inventory attributes only WI-031-authored changes. Existing unrelated and concurrent worktree changes were preserved and are not claimed here.

## Commands and Test Evidence

### Red reproduction

```powershell
.\gradlew.bat test --tests "com.atstudio.atstudio.service.AdminPaymentEntitlementCorrectionServiceTest" --tests "com.atstudio.atstudio.dto.payment.ProviderSupportReferenceTest" --tests "com.atstudio.atstudio.dto.payment.AdminProviderIdentifierContractTest"
```

- Expected pre-implementation result: 24 tests, 5 failed.
- Failures were the three new revision-fence tests, the utility colon/order-label test, and the ADMIN DTO retained-label test.

### Green verification

The same command passed after implementation:

| Class | Tests | Failures | Errors | Skipped |
|---|---:|---:|---:|---:|
| `AdminPaymentEntitlementCorrectionServiceTest` | 13 | 0 | 0 | 0 |
| `ProviderSupportReferenceTest` | 5 | 0 | 0 | 0 |
| `AdminProviderIdentifierContractTest` | 6 | 0 | 0 | 0 |
| **Total** | **24** | **0** | **0** | **0** |

- Gradle result: `BUILD SUCCESSFUL in 14s`.
- Main and test compilation completed; no retained/external database or Provider was used.

### Documentation validation

```powershell
python .agents\skills\validate-docs\scripts\validate_docs.py
```

- PASS: all Tier 0 documents exist.
- PASS: no broken internal links.
- PASS: 419 traceability IDs matched supported formats.
- PASS: all documents are listed in the index.

```powershell
rg -n "currentPeriodEnd" docs/standards/glossary.md docs/design/usecase/sound-track.md
```

- PASS: no match in the two owned documentation paths.

### Whitespace checks

- `git diff --check -- <owned tracked paths>`: exit 0; only LF-to-CRLF working-copy warnings, no whitespace error.
- `git -c core.autocrlf=false diff --no-index --check -- NUL <owned untracked path>`: no whitespace diagnostics for each of the three untracked source/test additions. Exit 1 is the expected no-index addition result.
- The first no-index wrapper treated Git's LF-to-CRLF warnings as failures; the warning-neutral rerun above resolved the checker issue without changing files.

## Risks and Rollback

### Risks

- Equal persisted timestamps are intentionally ambiguous and rejected. A new correction must be created after the agreement revision is strictly older.
- The fence depends on existing JPA auditing for `updatedAt`; retained-MySQL behavior was not exercised because database mutation is outside this WI.
- Sanitization covers supported labelled legacy forms. It does not infer identifiers from arbitrary unlabelled prose.
- Verification is focused. Full-backend, retained DB, live Provider, frontend, and client runtime gates remain outside this WI.

### Rollback

1. Remove only the WI-031 timestamp comparison and its added tests; preserve pre-existing WI-028 lock/status fences in the same files.
2. Remove only the `orderId`/colon separator expansion and the added sanitizer assertions; preserve centralized `REF-*` generation and existing equals-label behavior.
3. Revert only the two `expiresAt` wording/version hunks and remove the paired WI-031 deliverables.
4. Apply rollback as file-scoped hunks. Do not restore whole files because the worktree contains unrelated and concurrent changes.

No schema, data, Provider, runtime, secret, client worktree, or Git-index rollback is required.

## Follow-up

- WI-031 no longer blocks `WI-20260716-ATS-032`; MA can trigger its approved verification handoff.

## Related Documents

- [WI-031 Handoff](WI-20260716-ATS-031-handoff.md): Scope and acceptance criteria.
- [WI-031 Summary](../user/WI-20260716-ATS-031-summary.md): User-facing closure report.
- [WI-030 Evidence Pack](WI-20260716-ATS-030-evidence-pack.md): Reopened finding evidence.
