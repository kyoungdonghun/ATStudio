---
version: 1.0
last_updated: 2026-07-16
project: ATS
owner: qa-integ
category: evidence
status: complete
related_wi: WI-20260716-ATS-020
---

# Evidence Pack: WI-20260716-ATS-020

## Summary

- Independent read-only review found one repository-level blocker. Final judgment: `NEEDS_FOLLOW_UP_WI`.

## Finding

### F-020-01 - P2 Medium - Raw provider transaction identifier in ADMIN reconciliation response

**Reachable response path**

- `src/main/java/com/atstudio/atstudio/controller/AdminPaymentController.java:253-256`: ADMIN endpoint calls the read service.
- `src/main/java/com/atstudio/atstudio/service/AdminPaymentReadService.java:73-80`: reconciliation results are passed to the response mapper.
- `src/main/java/com/atstudio/atstudio/service/PaymentReconciliationService.java:524-546`: issue construction copies `providerResult.transactionId()`.
- `src/main/java/com/atstudio/atstudio/service/PaymentReconciliationService.java:597-613`: the issue record exposes the field as `providerTransactionId`.
- `src/main/java/com/atstudio/atstudio/dto/payment/AdminPaymentReconciliationResponse.java:25-36,50-61`: the provider issue list is returned directly, with no support-reference transformation.

**Contradicted contracts**

- `docs/policies/security-policy.md:233-237`: exact provider identifiers must remain in protected server/entity fields; ADMIN APIs must expose deterministic masked `REF-*` references.
- `docs/design/api-spec.md:1619`: the endpoint must return support-safe issue records with no raw provider secrets.
- `docs/design/api-spec.md:2202-2204`: the endpoint contract requires support-safe on-demand mismatch results.
- `docs/design/api-spec.md:2232-2247`: the documented issue shape contains `providerReference`, not `providerTransactionId`.

**Coverage gap explaining the passing gates**

- `src/test/java/com/atstudio/atstudio/dto/payment/AdminProviderIdentifierContractTest.java:19-43`: `providerTransactionId` is forbidden, but `AdminPaymentReconciliationResponse` is absent from the response-type list.
- `src/test/java/com/atstudio/atstudio/service/PaymentReconciliationServiceTest.java:224-269`: raw identifier and failure evidence are checked only against emitted logs, not the serialized ADMIN response.

## Judgment

- `NEEDS_FOLLOW_UP_WI`
- The supplied deterministic gates passed, but none of the cited assertions closes this ADMIN response-contract gap.
- WI-020 made no implementation change, client propagation, stage, commit, push, deletion, DB/provider mutation, or runtime change.

## Follow-Up Boundary

- Create a remediation WI that maps reconciliation issues to an ADMIN-safe DTO using deterministic `providerReference` values and adds serialized response-contract coverage that rejects raw provider identifier fields.
