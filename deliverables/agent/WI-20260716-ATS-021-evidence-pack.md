---
version: 1.0
last_updated: 2026-07-16
project: ATS
owner: se
category: evidence
status: complete
related_wi: WI-20260716-ATS-021
---

# Evidence Pack: WI-20260716-ATS-021

## Summary

- Closed F-020-01 by translating service-internal provider reconciliation issues into an ADMIN-safe nested DTO with deterministic support references.

## Scope / DoD Check

- [x] ADMIN reconciliation provider issues expose `providerReference` and no `providerTransactionId` field.
- [x] `ProviderSupportReference.from(raw)` produces deterministic `REF-*` values that do not contain the raw identifier.
- [x] Safe issue fields and local/provider aggregate and truncation fields remain available.
- [x] Service-internal exact provider evidence and Incident persistence remain unchanged.
- [x] Mapping is an in-memory pass over the existing bounded issue list and adds no DB or provider call.
- [x] Record-contract, direct JSON serialization, controller JSON, and reconciliation regression tests pass.
- [x] Focused `git diff --check` gate passes.

## Reference Documents (Tier 0-2)

| Tier | Document | Reason |
|---|---|---|
| 0 | `docs/standards/core-principles.md` | Constitution and payment traceability baseline |
| 0 | `docs/standards/development-standards.md` | Java DTO and testing standards |
| 0 | `docs/standards/documentation-standards.md` | Closure document standards |
| 0 | `docs/standards/glossary.md` | Canonical terminology |
| 1 | `docs/policies/security-policy.md` | ADMIN payment support-reference boundary |
| 1 | `docs/policies/quality-gates.md` | Focused regression and traceability gates |
| 2 | `docs/design/api-spec.md` | Authoritative reconciliation response contract |
| 2 | `docs/design/payment-operations-runbook.md` | ADMIN read-only safe/forbidden evidence boundary |
| Context | `deliverables/user/REQ-20260716-ATS-002.md` | Approved remediation scope |
| Context | `deliverables/agent/WI-20260716-ATS-020-evidence-pack.md` | F-020-01 finding and affected path |

**Injection rules applied:**

- Source: `deliverables/agent/WI-20260716-ATS-021-handoff.md`
- Assignee: `se`
- Task type: implementation and focused contract testing

## Evidence Pointers

- `src/main/java/com/atstudio/atstudio/dto/payment/AdminPaymentReconciliationResponse.java:16-42` preserves aggregate fields and maps each provider issue through `ProviderIssue::from`.
- `src/main/java/com/atstudio/atstudio/dto/payment/AdminPaymentReconciliationResponse.java:56-106` defines the safe nested issue record and converts only the raw transaction identifier to `providerReference`.
- `src/main/java/com/atstudio/atstudio/dto/payment/ProviderSupportReference.java:15-23` is the reused deterministic SHA-256 `REF-*` helper; WI-021 did not modify it.
- `src/main/java/com/atstudio/atstudio/service/PaymentReconciliationService.java:524-546,568-613` retains exact internal provider evidence and result records; WI-021 made no edit there.
- `src/test/java/com/atstudio/atstudio/dto/payment/AdminProviderIdentifierContractTest.java:57-135` proves the nested record contract, aggregate preservation, deterministic mapping, and absence of the raw field name and sentinel value in direct JSON.
- `src/test/java/com/atstudio/atstudio/controller/AdminPaymentControllerTest.java:86-138` proves the actual ADMIN endpoint JSON contains the safe reference and no raw field name/value.
- `deliverables/user/WI-20260716-ATS-021-summary.md` records the user-facing closure.

## Commands & Outputs

### Test-first signal

- Command: `gradlew.bat test --tests "com.atstudio.atstudio.dto.payment.AdminProviderIdentifierContractTest" --tests "com.atstudio.atstudio.controller.AdminPaymentControllerTest"`
- Before implementation: expected `compileTestJava` failure with two missing-symbol errors for `ProviderIssue` and `providerReference()`.
- After implementation: `BUILD SUCCESSFUL` in 31 seconds.

### Final focused regression

- Command: `gradlew.bat test --tests "com.atstudio.atstudio.dto.payment.AdminProviderIdentifierContractTest" --tests "com.atstudio.atstudio.controller.AdminPaymentControllerTest" --tests "com.atstudio.atstudio.controller.PaymentControllerTest" --tests "com.atstudio.atstudio.service.PaymentReconciliationServiceTest"`
- Result: exit 0 in 48.7 seconds.

| Test class | Tests | Failures | Errors | Skipped | Suite time |
|---|---:|---:|---:|---:|---:|
| `AdminProviderIdentifierContractTest` | 5 | 0 | 0 | 0 | 0.384s |
| `AdminPaymentControllerTest` | 7 | 0 | 0 | 0 | 24.315s |
| `PaymentControllerTest` | 7 | 0 | 0 | 0 | 2.897s |
| `PaymentReconciliationServiceTest` | 14 | 0 | 0 | 0 | 0.619s |
| **Total** | **33** | **0** | **0** | **0** | **28.215s** |

### Diff integrity

- `git diff --check`: exit 0, no whitespace errors; cumulative LF-to-CRLF conversion warnings only.
- Scoped tracked DTO check: exit 0, no whitespace errors.
- Trailing-whitespace scan for the two untracked focused test files: no matches.

## Risks / Rollback

- Risk: The deterministic support reference is intentionally one-way and cannot replace the protected raw identifier for provider operations. Exact evidence remains in the service and Incident paths.
- Performance: The added mapping is O(issue details) and remains bounded by the existing issue-detail limit.
- Rollback: Revert the ADMIN response nested DTO mapping and its focused tests together. Do not alter service-internal reconciliation records or Incident evidence.

## Follow-up

- Run final development-branch release-readiness re-verification now that F-020-01 is closed.
