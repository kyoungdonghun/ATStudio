# WI-20260715-ATS-011 Summary

## Outcome

Implemented the four confirmed payment-integrity gaps from the approved handoff. The implementation and focused regression coverage are complete locally; independent review remains required before closure.

## Changes

| Finding | Result | Traceability |
|---|---|---|
| Refund transaction isolation | Both refund entry points now use `Propagation.NEVER`; proxy calls made inside an active transaction fail before any provider invocation. | WI-009 P1-01; WI-010 P1-EXEC-01 |
| SUBSCRIBE reconciliation eligibility | Initial-subscription reconciliation and finalization require READY status, cleanup NONE, no cancellation, no existing subscription, and retained billing-key material. Eligibility is revalidated after provider lookup and again in the finalizer. | WI-009 P1-02; WI-010 P1-EXEC-02 |
| Renewal retry-date consumption | A retry date is consumed only when an eligible FAILED order is claimed. A later deterministic failure may schedule the next date; an ambiguous result leaves the date null. | WI-010 P2-EXEC-02 |
| Payment-key minimization | Exact payment keys remain in structured transaction ownership only. Lookup payloads omit the key, reconciliation Incident/audit evidence uses a masked identifier, and an unknown cancel log no longer emits the key. | WI-009 P2-01; WI-010 P2-SEC-03 |

## Verification

- Java production and test compilation: passed.
- Focused tests: 7 classes, 67 tests passed; 0 failed, 0 errors, 0 skipped.
- Impacted tests: 7 classes, 33 tests passed; 0 failed, 0 errors, 0 skipped.
- `git diff --check`: passed with no whitespace errors; Git reported only existing LF-to-CRLF working-copy warnings.
- No MySQL proof run, database mutation, schema change, or real provider call was performed.

## Review Boundary

The WI-007 MySQL proof does not require a technical rerun for these changes because no schema, repository query, lock order, or database concurrency primitive changed. Independent review must still validate the implementation and decide final finding closure.

## Outputs

- `deliverables/agent/WI-20260715-ATS-011-evidence-pack.md`
- `deliverables/user/WI-20260715-ATS-011-summary.md`
