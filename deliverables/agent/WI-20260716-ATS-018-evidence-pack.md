---
version: 1.0
last_updated: 2026-07-16
project: ATS
owner: se
category: evidence-pack
status: complete
related_wi: WI-20260716-ATS-018
dependencies:
  - path: ../user/REQ-20260716-ATS-002.md
    reason: Approved development-branch remediation scope
  - path: WI-20260716-ATS-018-handoff.md
    reason: Execution and output contract
  - path: WI-20260716-ATS-017-evidence-pack.md
    reason: Prior closure format and baseline
---

# Evidence Pack: WI-20260716-ATS-018

## Scope / DoD Check

- [x] Provider-neutral receipt URL contract is absolute HTTPS, credential-free, and limited to the default or 443 port.
- [x] Provider receipt URL ingestion/response behavior does not expose unsafe values as actionable links.
- [x] Frontend defense-in-depth renders unsafe retained legacy receipt URLs as non-clickable text.
- [x] Reconciliation logs use counts and correlation-safe aggregate fields rather than full provider result serialization.
- [x] Toss cancel unknown-outcome logging records exception-class metadata only.
- [x] Focused backend and frontend verification passed independently.
- [ ] Complete final WI gates remain outstanding and were not run for this closure-only action.

## Closure Evidence

| Area | Evidence | Result |
|---|---|---|
| Receipt URL contract | Absolute HTTPS only; no credentials; default/443 port only | CLOSED |
| Retained legacy receipt data | Frontend revalidation; unsafe values are text/reference state, not anchors | CLOSED |
| Reconciliation logging | Aggregate counts/correlation fields only; exact provider transaction identifiers are not serialized | CLOSED |
| Toss cancel logging | Exception class only; no exception message or stack | CLOSED |

## Verification Commands / Results

The following results were supplied as independently completed checks; they were not rerun during this documentation-only closure.

| Check | Result |
|---|---|
| Backend focused Gradle group (four classes) | PASS; exit 0; 23.5 seconds |
| Frontend focused Vitest group (two files) | PASS; 20 tests; 2.50 seconds |
| Scope diff-check | PASS; line-ending warnings only |
| `frontend/tsconfig.tsbuildinfo` integrity | PASS; 5,421 bytes; SHA-256 `B6A42AD2CD32A5AD04D06C55B8B1B26DD9B1894AFDAF7B508DFF16EF0C60F22A` |

## Changed Files / Provenance

### Backend implementation

| Path | Evidence role |
|---|---|
| `src/main/java/com/atstudio/atstudio/common/validation/ProviderReceiptUrlPolicy.java` | Provider-neutral absolute-HTTPS/no-credentials/default-or-443-port receipt URL policy. |
| `src/main/java/com/atstudio/atstudio/service/PaymentReceiptEvidenceService.java` | Applies the receipt URL policy when provider receipt evidence is ingested. |
| `src/main/java/com/atstudio/atstudio/dto/payment/AdminPaymentReceiptResponse.java` | Revalidates retained receipt URLs at the ADMIN response boundary and suppresses unsafe legacy values. |
| `src/main/java/com/atstudio/atstudio/service/PaymentReconciliationService.java` | Emits bounded aggregate/correlation reconciliation log fields without serializing issue details or exact provider transaction identifiers. |
| `src/main/java/com/atstudio/atstudio/service/payment/provider/recurring/TossBillingProvider.java` | Logs unknown cancel transport failures using exception-class metadata only. |

### Backend tests

| Path | Evidence role |
|---|---|
| `src/test/java/com/atstudio/atstudio/service/PaymentReceiptEvidenceServiceTest.java` | Covers valid HTTPS receipt URLs and rejects/suppresses unsafe schemes, credentials, malformed values, and non-standard ports. |
| `src/test/java/com/atstudio/atstudio/dto/payment/AdminProviderIdentifierContractTest.java` | Proves the ADMIN receipt response boundary suppresses unsafe retained URLs and preserves provider-identifier exposure constraints. |

### Frontend implementation and tests

| Path | Evidence role |
|---|---|
| `frontend/src/utils/safeReceiptUrl.ts` | Revalidates the provider-neutral safe receipt URL contract in the browser. |
| `frontend/src/utils/safeReceiptUrl.test.ts` | Covers safe, unsafe, malformed, credential-bearing, and non-standard-port receipt URL cases. |
| `frontend/src/pages/admin/PaymentReadOnlyPage.tsx` | Renders safe receipt URLs as links and unsafe retained values as non-clickable text/reference state. |
| `frontend/src/pages/admin/PaymentReadOnlyPage.test.tsx` | Verifies valid links remain actionable and unsafe legacy values do not render as anchors. |

### Documentation and deterministic client PDF

Git status and content diff confirm the following WI-018 contract/provenance paths:

| Path | Evidence role |
|---|---|
| `docs/design/api-spec.md` | ADMIN receipt response URL contract and aggregate-only reconciliation logging contract. |
| `docs/design/payment-operations-runbook.md` | Operator guidance for non-clickable unsafe receipt evidence, aggregate logs, masked Incidents, and bounded Toss cancel logging. |
| `docs/design/payment-refund-receipt-settlement-policy.md` | Provider-neutral receipt URL ingestion/read policy and provider-evidence logging exclusions. |
| `docs/payment/admin-operations-guide.md` | Admin UI acceptance rule for clickable receipt links. |
| `docs/payment/system-overview.md` | Current-state receipt evidence trust boundary and reconciliation/Toss logging behavior. |
| `docs/policies/security-policy.md` | Normative receipt URL and provider log safety requirements. |
| `output/pdf/atstudio-client-testing-guide.pdf` | Regenerated deterministic client-facing acceptance guide containing the updated source contract. |
| `output/pdf/atstudio-client-testing-guide.manifest.json` | Generated-source/hash provenance for the deterministic client PDF. |

This closure correction modifies only this Evidence Pack section. It does not modify any implementation, test, source documentation, PDF, manifest, or generated file listed above.

## Remaining Gates and Boundaries

Full backend/frontend/docs/PDF/generated-file/diff/client-worktree gates remain required for the final WI. Provider, retained database, deployment/proxy/secret, filesystem, and frozen-client-branch evidence remains environment-conditional. No stage, commit, push, DB operation, provider operation, client operation, runtime restart, or deletion was performed.

## Rollback / Follow-up

No implementation rollback is required for this documentation-only closure. If the implementation is later rolled back, revert the backend logging/receipt enforcement group and frontend renderer/tests as coherent groups without reverting prior WIs. No follow-up WI is created here; the outstanding full gates belong to the final WI release-readiness decision.

## Final Integrity Statement

Exactly the two WI-018 deliverables specified by the handoff were created. Tests were not run, code was not modified, and files were not staged, committed, pushed, deleted, or used to restart any runtime.
