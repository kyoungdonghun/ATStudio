# WI-20260715-ATS-010 Independent QA Integration Summary

## Verdict

**FAIL** - no P0 was found, but two P1 executable cross-layer inconsistencies
remain. F-05 and acceptance-preview isolation are independently verified.

| Classification | Count |
|---|---:|
| P0 executable defect | 0 |
| P1 executable defect | 2 |
| P2 executable/security defect | 2 |
| Documentation drift groups | 3 |

## Severity-Ordered Findings

### P1-EXEC-01 - Refund execution suspends an outer transaction instead of rejecting it

The remediation design requires every `PaymentRefundProvider` caller to use
`Propagation.NEVER` (or an equivalent fail-fast assertion) and explicitly
rejects transaction suspension. `AdminPaymentRefundService.executeRefund()`
and `executeRefundAt()` use `Propagation.NOT_SUPPORTED`, so an accidental
outer transaction is suspended and the provider refund still runs.

- Contract: `docs/design/p1-payment-integrity-remediation-design.md:82-85`
- Executable behavior: `src/main/java/com/atstudio/atstudio/service/AdminPaymentRefundService.java:149-186`
- Test gap: `src/test/java/com/atstudio/atstudio/service/PaymentRefundResilienceIntegrationTest.java:82` and `:733-735` prove only that the provider sees no active transaction; they cannot distinguish `NEVER` from suspension.

Required correction: change both refund entry points to `Propagation.NEVER`
and add a proxy-based test that invokes refund execution from an active
transaction, expects `IllegalTransactionStateException`, and proves zero
provider calls. This blocks final payment-integrity acceptance.

### P1-EXEC-02 - Cancelled SUBSCRIBE orders remain mutation-eligible in reconciliation

The SUBSCRIBE reconciliation gate checks for no local subscription and retained
billing-key material, but it does not reject a cancelled billing agreement.
The downstream reconciliation validation repeats the same incomplete gate, so
exact provider `DONE` evidence can enter `PROVIDER_SUCCEEDED` and purpose
finalization instead of remaining detect-only.

- Eligibility gap: `PaymentReconciliationTransactionService.java:225-238`
- Mutation/finalizer path: `PaymentReconciliationService.java:149-165,238-253`
- Downstream validation gap: `PaymentCommandTransactionService.java:1246-1275`
- Missing negative scenario: `PaymentReconciliationRecoveryIntegrationTest.java:119-147` proves supported-purpose success but not cancelled SUBSCRIBE detect-only behavior.

Required correction: require the expected pre-activation agreement/order state
for SUBSCRIBE in both claim eligibility and locked mutation validation. Add a
cancelled-agreement provider-`DONE` test that proves Incident-only handling,
unchanged order/agreement/subscription/payment state, and zero charge calls.

### P2-EXEC-02 - An ambiguous day-two renewal can retain a consumed retry date

On a deterministic retry, the order moves from `FAILED` to `PROCESSING`
without clearing `renewalRetryAt`. If that provider result becomes ambiguous,
the pending-result branch returns without clearing it. The exact repository
status predicates prevent another automatic charge, so this is not a P1 money
movement defect, but persisted state does not match the design's null-state
contract.

- Claim/result behavior: `PaymentCommandTransactionService.java:292-321` and `:466-469`
- Duplicate-charge protection: `BillingAgreementRepository.java:28-42`
- Missing scenario: `RecurringRenewalCommandIntegrationTest.java:206-228` covers first-attempt ambiguity, not ambiguity after a deterministic retry.
- Prior disclosure: `deliverables/agent/WI-20260715-ATS-002-evidence-pack.md` already records this residual risk.

Required follow-up: consume/clear the retry gate when a retry is claimed and
add a day-two ambiguous-result test asserting unchanged period/order identity,
one provider attempt per claim, `PENDING_PROVIDER_CONFIRMATION`, and null
`renewalRetryAt`.

### P2-SEC-03 - Raw provider paymentKey reaches reconciliation audit evidence

The Toss lookup adapter treats `paymentKey` as the authoritative transaction ID
and also includes it verbatim in the sanitized lookup payload. Reconciliation
then carries that raw value into Incident/audit evidence. This is operationally
useful but does not meet the design's sanitized-transaction evidence boundary.

- Raw extraction/payload: `TossBillingProvider.java:428-439,502-515`
- Incident value propagation: `PaymentReconciliationService.java:263-284`
- Audit-note sink: `PaymentReconciliationIncidentService.java:306-321`

Required follow-up: retain the exact value only in the protected financial
owner fields needed for matching; hash or mask it in Incident/audit notes and
add a negative assertion that the raw `paymentKey` is absent.

## F-01 Through F-05 Matrix

| Finding | Contract and implementation trace | Test/evidence trace | Result |
|---|---|---|---|
| F-01 Renewal identity | Fresh/manual DDL: `schema.sql:472,527-529`; manual patch `:326-497`. Exact candidate/claim/finalize: `BillingAgreementRepository.java:23-65`; `PaymentCommandTransactionService.java:234-321,439-485,646-692`. | `RecurringRenewalCommandIntegrationTest.java:159-228`; WI-002. | P0/P1 PASS; P2-EXEC-02 remains. |
| F-02 Transaction boundaries | Cancellation/cleanup, charged upgrade, and reconciliation use `NEVER` orchestrators plus short result phases: `BillingAgreementApplicationService.java:326-352`; `BillingAgreementCleanupProviderExecutor.java:39-77`; `UserSubscriptionService.java:123-139`; `SubscriptionUpgradePaymentExecutor.java:26-36`; `PaymentReconciliationService.java:37-56`. | WI-004, WI-005, WI-006 focused transaction evidence. | PASS for the four F-02 flows. |
| F-03 Refund lease/recovery | DDL: `schema.sql:651,660`; manual patch `:461-497,631-634`. Lease, same-key replay, and fencing: `PaymentRefundTransactionService.java:50-193,264-314`; `PaymentRefundRepository.java:47-62`. | `PaymentRefundResilienceIntegrationTest.java:114-423`; WI-003. | **P1 FAIL** due to refund provider boundary. |
| F-04 Provider-DONE recovery | Unique ownership: `schema.sql:527-529,556-557`. Strict evidence gate/finalize-only: `PaymentReconciliationTransactionService.java:97-217`; `PaymentCommandTransactionService.java:377-405,645-692,1147-1275`. | `PaymentReconciliationRecoveryIntegrationTest.java:119-290`; WI-006 and WI-008. | **P1 FAIL** for cancelled SUBSCRIBE; P2 audit sanitization open. |
| F-05 MySQL concurrency | Seven bounded races and exact loser rules: `PaymentMysqlConcurrencyIntegrationTest.java:230-594`; `MysqlRaceTestSupport.java:32-145`. | Final WI-007 logs: schema PASS, Hibernate PASS, races PASS, cleanup count 0. | PASS. |

## Final Evidence Boundaries

- Authoritative WI-007 result: `run-summary.log` reports
  `schemaCreate=PASS`, `hibernateValidate=PASS`, `mysqlRaces=PASS`,
  `diagnostics=NOT_REQUIRED`, `drop=PASS`, `cleanupDatabaseExists=0`, and
  `result=PASS`. Historical diagnostics remain prior-run evidence only.
- Current development branch: `codex/p1-acceptance-hardening@830c8dd`.
- Acceptance preview: clean `codex/acceptance-preview@b217234`; it does not
  contain `830c8dd`. No preview file or runtime was changed.
- WI-010 performed no database execution, provider call, server action, or
  commit.

## Documentation Drift and Exact Updates

1. After P1-EXEC-01 is fixed, update WI-003 evidence to state strict
   no-suspension proof and cite the active-transaction fail-fast test.
2. After P1-EXEC-02 and P2-SEC-03 are fixed, update WI-006 evidence with the
   cancelled-SUBSCRIBE detect-only test and raw-payment-key redaction proof.
3. After P2-EXEC-02 is fixed, close the residual-risk entry in WI-002 evidence
   and add the day-two ambiguous retry test pointer.
4. Update `p1-payment-integrity-remediation-design.md` frontmatter from
   `proposed` only after both executable follow-ups close; replace Section 12's
   unallocated A-G slices with WI-001 through WI-007 plus WI-008 correction,
   and record the final WI-007 PASS in Section 10.
5. Mark `p1-payment-db-integrity-design.md` as a historical/superseded baseline
   for behavior, identify the remediation design as current, and label Section
   2 gaps and Section 12 blockers as pre-remediation history. Preserve its
   manual-patch baseline and retained-database uncertainty.
6. Do not rewrite WI-007 diagnostics or PASS evidence; its current separation
   of historical failure evidence from the final PASS is correct.

## Required Next Work

- Create focused implementation/test WIs for P1-EXEC-01 and P1-EXEC-02 before
  final quality gate approval.
- Track P2-EXEC-02 as a separate narrow state-parity follow-up.
- Track P2-SEC-03 as a narrow audit-evidence redaction follow-up.
- Re-run independent integration review after those corrections; no new MySQL
  race run is required unless the fixes touch locking, schema, or race paths.
