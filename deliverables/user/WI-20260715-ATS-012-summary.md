# WI-20260715-ATS-012 Independent Review Summary

## Findings

### 1. WI-009 P1-01 / WI-010 P1-EXEC-01 - Refund transaction boundary

**CLOSED.** Both refund entry points use `Propagation.NEVER`, so an active
caller transaction is rejected at the Spring proxy before claim or provider
execution. The two proxy-based regression tests assert
`IllegalTransactionStateException` and zero provider calls.

### 2. WI-009 P1-02 / WI-010 P1-EXEC-02 - SUBSCRIBE reconciliation state

**CLOSED.** SUBSCRIBE mutation now requires agreement `READY`, cleanup `NONE`,
no cancellation timestamp, no subscription, and retained billing-key
ciphertext/fingerprint. The same predicate is enforced at lookup claim,
locked provider-success persistence, and initial-charge finalization. Tests
cover cancellation before lookup, cancellation after lookup, and cancellation
after provider-success persistence, with no subscription/payment/refund
mutation.

### 3. WI-010 P2-EXEC-02 - Renewal retry-date consumption

**CLOSED.** An eligible `FAILED` retry consumes `renewalRetryAt` in the claim
transaction. The day-two ambiguous-result regression proves the date remains
null, the order stays pending with attempt 2, and day three does not charge.

### 4. WI-009 P2-01 / WI-010 P2-SEC-03 - Payment-key minimization

**CLOSED.** Exact transaction ownership remains in the structured lookup
result/payment order. The lookup payload omits `paymentKey`, Incident and audit
representations are masked, and the unknown cancel log no longer interpolates
the raw key. Tests cover lookup serialization and Incident/audit persistence.
Residual P3 test gap: the unknown-cancel log change is statically verified but
has no dedicated log-appender assertion.

## Verdict

**PASS. No P0 or P1 finding remains in the four-item WI-012 scope.**

- Independent focused rerun: **PASS** (`BUILD SUCCESSFUL` in 33 seconds).
- The impacted suite was not rerun in WI-012 per its narrow review scope;
  WI-011's passing 33-test evidence was reviewed.
- No MySQL rerun is required: `46edd88` changes no schema, repository query,
  lock order, or database concurrency primitive.

## Documentation Update Pointers

- `WI-20260715-ATS-002-evidence-pack.md`: record P2 retry-date closure.
- `WI-20260715-ATS-003-evidence-pack.md`: record strict refund `NEVER` proof.
- `WI-20260715-ATS-006-evidence-pack.md`: record SUBSCRIBE fail-closed and
  payment-key minimization closure.
- `p1-payment-integrity-remediation-design.md`: update lifecycle/slice status
  only when the final quality gate formally closes.
- Preserve WI-009 and WI-010 as historical review evidence; cross-reference
  WI-011 and WI-012 rather than rewriting their original findings.
