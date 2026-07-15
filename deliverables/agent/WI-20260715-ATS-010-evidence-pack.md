# Evidence Pack: WI-20260715-ATS-010

## Summary (one-liner)

- Independent QA integration review returns **FAIL**: F-05 and preview
  isolation pass, but two P1 executable defects remain in refund and SUBSCRIBE
  reconciliation boundaries.

## Scope / DoD Check

- [x] Built a compact F-01 through F-05 design-schema-code-test-evidence matrix.
- [x] Checked persisted columns, indexes, and uniqueness contracts against
  repository/service usage and final Hibernate validation evidence.
- [x] Separated executable defects from documentation drift.
- [x] Treated WI-007 final PASS logs as authoritative and historical
  diagnostics as prior-run evidence only.
- [x] Verified acceptance-preview commit and worktree isolation.
- [x] Independently confirmed the three reported WI-009 risk statements from
  already-reviewed code paths without expanding the review scope.
- [x] Performed no database/provider execution, runtime mutation, or commit.
- [ ] PASS gate: blocked by P1-EXEC-01.

## Reference Documents (Tier 0-2)

| Tier | Document | Use |
|---|---|---|
| 0 | `docs/standards/core-principles.md` | Financial traceability and transparent review |
| 0 | `docs/standards/development-standards.md` | Java transaction, JPA, test, and evidence standards |
| 1 | `docs/policies/quality-gates.md` | High-criticality acceptance gate |
| 2 | `docs/design/p1-payment-integrity-remediation-design.md` | F-01 through F-05 current contract |
| 2 | `docs/design/p1-payment-db-integrity-design.md` | Command/schema/manual-patch baseline |
| Handoff | `deliverables/agent/WI-20260715-ATS-010-handoff.md` | Scope and output contract |
| Evidence | `deliverables/agent/WI-20260715-ATS-001-evidence-pack.md` through `WI-20260715-ATS-008-evidence-pack.md` | Package implementation and final verification evidence |

**Review boundary:** production/test files changed in `103fdf4..830c8dd`, plus
`src/main/resources/schema.sql` and
`src/main/resources/db/manual/20260714_payment_db_integrity.sql`. No unrelated
P1 domain was reviewed.

## Findings

### P1-EXEC-01 - Refund provider boundary permits transaction suspension

- Design: `docs/design/p1-payment-integrity-remediation-design.md:82-85`
  requires `PaymentRefundProvider` callers to use `Propagation.NEVER` or an
  equivalent fail-fast assertion and rejects suspended outer transactions.
- Code: `src/main/java/com/atstudio/atstudio/service/AdminPaymentRefundService.java:149-186`
  annotates both refund execution entry points with
  `Propagation.NOT_SUPPORTED` and then calls `provider.cancelPayment(...)`.
- Test gap: `src/test/java/com/atstudio/atstudio/service/PaymentRefundResilienceIntegrationTest.java:82,733-735`
  starts outside a transaction and checks only
  `isActualTransactionActive() == false`; suspension produces the same
  observation.
- Impact: a future/accidental transactional caller is not rejected before the
  external refund side effect. This violates the approved financial boundary
  and blocks the P0/P1 acceptance gate.
- Required correction: use `NEVER` on both entry points and add a proxy-based
  active-transaction rejection test with zero provider calls.

### P2-EXEC-02 - Retry scheduling state can remain stale after ambiguity

- `PaymentCommandTransactionService.java:292-321` claims a failed renewal
  retry without consuming `renewalRetryAt`.
- `PaymentCommandTransactionService.java:466-469` persists an ambiguous result
  and returns without clearing the retry date.
- `BillingAgreementRepository.java:28-42` prevents another automatic charge by
  requiring an exact eligible order status, limiting current impact.
- `RecurringRenewalCommandIntegrationTest.java:206-228` does not cover an
  ambiguous result after a deterministic day-two retry.
- Required correction: clear/consume the scheduling gate at retry claim and
  add the missing state-parity test.

### P1-EXEC-02 - Cancelled SUBSCRIBE remains reconciliation mutation-eligible

- `PaymentReconciliationTransactionService.java:225-238` treats SUBSCRIBE as
  locally eligible when no subscription exists and billing-key material
  remains; agreement cancellation is not rejected.
- `PaymentReconciliationService.java:149-165,238-253` records exact provider
  success and dispatches the SUBSCRIBE finalizer.
- `PaymentCommandTransactionService.java:1246-1275` repeats relationship/key
  checks but has no explicit expected agreement-state guard in the reviewed
  reconciliation gate.
- Impact: a cancelled local command can cross the detect-only boundary and
  enter financial mutation/finalization. This blocks acceptance.
- Required correction: enforce the expected pre-activation agreement/order
  state in both claim and locked result validation, with a cancelled SUBSCRIBE
  provider-`DONE` Incident-only regression test.

### P2-SEC-03 - Raw Toss paymentKey is retained in audit evidence

- `TossBillingProvider.java:428-439,502-515` extracts the raw `paymentKey` as
  transaction evidence and includes it verbatim in the sanitized payload.
- `PaymentReconciliationService.java:263-284` carries the raw transaction ID
  into the reconciliation issue.
- `PaymentReconciliationIncidentService.java:306-321` writes transaction
  evidence into the audit note.
- Required correction: preserve exact matching only in protected owner fields;
  hash/mask Incident and audit-note representations and test that raw provider
  keys are absent.

## F-01 Through F-05 Traceability

| ID | Schema/manual patch | Code | Test/evidence | Decision |
|---|---|---|---|---|
| F-01 | `schema.sql:472,527-529`; manual patch `:326-497` | `BillingAgreementRepository.java:23-65`; `PaymentCommandTransactionService.java:234-321,439-485,646-692` | `RecurringRenewalCommandIntegrationTest.java:159-228`; WI-002 | P0/P1 PASS; P2 open |
| F-02 | Cleanup and upgrade columns at `schema.sql:476-483,510` | Cancellation, cleanup, upgrade, and reconciliation `NEVER` boundaries | WI-004, WI-005, WI-006 | PASS |
| F-03 | `schema.sql:651,660`; manual patch `:461-497,631-634` | `PaymentRefundTransactionService.java:50-193,264-314`; `AdminPaymentRefundService.java:149-204` | `PaymentRefundResilienceIntegrationTest.java:114-423`; WI-003 | **P1 FAIL** |
| F-04 | `schema.sql:527-529,556-557` | `PaymentReconciliationTransactionService.java:97-217`; `PaymentCommandTransactionService.java:377-405,645-692,1147-1275` | `PaymentReconciliationRecoveryIntegrationTest.java:119-290`; WI-006/WI-008 | **P1 FAIL**; P2 redaction open |
| F-05 | Fresh schema is InnoDB; unique/index contracts above | Seven-race MySQL suite and strict race helper | WI-007 final logs and evidence | PASS |

## WI-007 Final PASS Verification

- `deliverables/agent/WI-20260715-ATS-007/run-summary.log:6-12`:
  schema create PASS, Hibernate validate PASS, MySQL races PASS,
  diagnostics not required, drop PASS, cleanup database count 0, final PASS.
- `mysql-races.log:18`: `BUILD SUCCESSFUL in 48s`.
- `hibernate-validate.log:18`: `BUILD SUCCESSFUL in 42s`.
- `database-drop.log:5,7`: drop OK and PASS.
- `database-absent.log:6`: PASS.
- Historical `failure-diagnostics.log` and
  `manager-diagnostics-process.log` remain earlier 5/7-run audit evidence and
  are not evidence against the authoritative final run.

## Acceptance-Preview Isolation

- Development checkout: `codex/p1-acceptance-hardening@830c8dd`.
- Preview checkout: clean `codex/acceptance-preview@b2172346f9c8202abe56ec44b458cd0a493fa232`.
- `git merge-base --is-ancestor 830c8dd HEAD` in the preview checkout returned
  false; current development changes are not present in the preview baseline.
- Preview diff was empty. No preview file, process, database, or server was
  changed by WI-010.

## Documentation Drift

1. The remediation design remains `status: proposed` and Section 12 still
   describes unallocated slices despite WI-001 through WI-008 completion.
2. The database-integrity design remains `status: draft`; its confirmed gaps
   and blocker table are pre-remediation history but are not labeled as such.
3. WI-003 evidence overstates closure until strict no-suspension refund proof
   exists. WI-006 evidence requires cancelled-SUBSCRIBE and raw-key redaction
   corrections. WI-002 correctly discloses P2-EXEC-02 and should be updated
   only when that follow-up closes.

Exact documentation actions are listed in the user-facing summary. WI-007
requires no correction.

## Commands & Outputs

- Read-only range and pointer inspection: `git diff`, `git show`, `rg`, and
  numbered file reads restricted to the approved payment scope.
- Final log verification: PASS values listed above.
- Preview verification: clean `b217234`; current development head absent.
- Whitespace verification:
  `git diff --no-index --check -- NUL <each WI-010 output>` - PASS.

## Tests

- No test suite, database, provider, server, or preview runtime was executed by
  WI-010, per the handoff constraint.
- Existing focused H2 and final MySQL results were reviewed as recorded
  evidence; they were not replayed.

## Risks / Rollback

Risks:

- Final acceptance remains blocked until P1-EXEC-01 and P1-EXEC-02 are
  corrected and independently rechecked.
- P2-EXEC-02 does not currently authorize a duplicate charge because exact
  status predicates exclude it, but stale scheduling state can mislead
  recovery/operations.
- P2-SEC-03 exposes a raw provider transaction identifier in operational
  evidence beyond the intended sanitized representation.
- Retained-database applicability remains unproven until an separately
  approved copied-database rehearsal; WI-010 makes no claim beyond the final
  disposable MySQL proof.

Rollback:

- WI-010 creates only this Evidence Pack and its user-facing summary. Remove
  those two files to roll back the review artifacts.
- No code, schema, database, provider, preview, or runtime rollback is needed.

## Follow-ups

1. Create focused SE/RE correction WIs for strict refund `NEVER` enforcement
   and cancelled-SUBSCRIBE detect-only reconciliation.
2. Create narrow follow-ups for renewal retry-state parity and audit-evidence
   payment-key redaction.
3. Re-run WI-010-equivalent integration acceptance after corrections and then
   apply the exact documentation updates from the summary.
