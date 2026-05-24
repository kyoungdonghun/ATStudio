# Evidence Pack: WI-20260525-ATS-002

## Summary (one-liner)
- Added the payment operations policy for refund, receipt, settlement, and tax invoice boundaries, and synchronized SR/runbook/design indexes.

## Scope / DoD Check
- [x] Refund policy covers full/partial refund, idempotency, entitlement handling, and incident linkage.
- [x] Receipt policy covers provider receipt URL, cash receipt issue/cancel boundaries, and visibility.
- [x] Settlement policy covers Toss settlement lookup, internal reconciliation, and payout/settlement distinction.
- [x] Tax invoice policy covers HomeTax/ASP/manual issuance boundary and required business evidence.
- [x] SR-93 and payment operations runbook reflect policy-design completion but implementation deferral.
- [x] New design document is listed in `docs/design/index.md`.
- [x] `docs/index.md` design count and total count are synchronized.
- [x] Docs validation and diff check pass.

## Reference Documents (Tier 0-2)

**Injected Context**:

| Tier | Document | Reason |
|------|----------|--------|
| 0 | docs/standards/core-principles.md | Constitution and execution gate |
| 0 | docs/standards/development-standards.md | Development and system boundary standards |
| 0 | docs/standards/documentation-standards.md | Documentation consistency |
| 0 | docs/standards/glossary.md | Domain terminology |
| 1 | docs/policies/security-policy.md | Sensitive payment and tax evidence boundary |
| 1 | docs/policies/quality-gates.md | Verification gate |
| 2 | docs/SR/SR-93.md | Payment production readiness tracker |
| 2 | docs/design/payment-operations-runbook.md | Current operations runbook |
| 2 | docs/design/payment-integration-design.md | Payment architecture and decisions |
| 2 | docs/design/api-spec.md | Admin/payment API baseline |
| 2 | docs/design/db-schema.md | Current payment table baseline |
| 2 | docs/design/index.md | Design index |
| 2 | docs/index.md | Root documentation index |
| 2 | deliverables/user/REQ-20260525-ATS-002.md | Approved requirement |
| 2 | deliverables/agent/WI-20260525-ATS-002-handoff.md | WI scope and output contract |

**External Official References**:

| Source | Use |
|---|---|
| Toss Payments cancel payment guide | Refund/cancel policy |
| Toss Payments authorization and headers | Secret-key and idempotency policy |
| Toss Payments payment results guide | Receipt and cash receipt boundary |
| Toss Payments API reference | Cash receipt and settlement API concepts |
| Toss Payments settlement glossary | PG settlement process |
| Toss Payments API keys guide | test/live key and secret handling |
| National Tax Service e-tax invoice guide | tax invoice issuing route boundary |

**Injection Rules Applied**:
- Rule source: `.claude/config/context-injection-rules.json`
- Assignees: sa/docops
- Task type: payment operations policy, documentation, security-sensitive financial evidence

## Evidence Pointers

- `docs/design/payment-refund-receipt-settlement-policy.md:32` — official external basis table.
- `docs/design/payment-refund-receipt-settlement-policy.md:56` — current ATStudio implementation baseline and data gaps.
- `docs/design/payment-refund-receipt-settlement-policy.md:79` — policy principles.
- `docs/design/payment-refund-receipt-settlement-policy.md:93` — refund policy, including cancellation/refund distinction.
- `docs/design/payment-refund-receipt-settlement-policy.md:163` — `payment_refunds` table candidate.
- `docs/design/payment-refund-receipt-settlement-policy.md:186` — receipt and cash receipt policy.
- `docs/design/payment-refund-receipt-settlement-policy.md:239` — settlement policy and payout distinction.
- `docs/design/payment-refund-receipt-settlement-policy.md:307` — tax invoice policy and manual HomeTax/ASP boundary.
- `docs/design/payment-refund-receipt-settlement-policy.md:374` — recommended implementation order.
- `docs/design/payment-refund-receipt-settlement-policy.md:485` — policy decision records.
- `docs/SR/SR-93.md:10` — P2 policy slice added to SR history.
- `docs/SR/SR-93.md:179` — completed P2 policy items added.
- `docs/design/payment-operations-runbook.md:159` — runbook points future implementation to the policy document.
- `docs/design/payment-integration-design.md:682` — payment design links the policy and keeps implementation separate.
- `docs/design/index.md:29` — new design document indexed.
- `docs/index.md:18` — design count updated to 23.

## Commands & Outputs

- `python .agents\skills\validate-docs\scripts\validate_docs.py`
  - Result: all validations passed.
- `git diff --check`
  - Result: no whitespace errors; only CRLF conversion warnings from Git on Windows before final staging.

## Tests

- Documentation validation:
  - Tier 0 docs exist.
  - Internal links pass.
  - Traceability IDs pass.
  - Document index passes.
- No runtime code was changed in this WI, so backend/frontend test suites were not required for this policy-only slice.

## Risks / Rollback

- Risks:
  - Tax invoice policy is a system policy baseline and still requires accountant/tax-operator review before live automation.
  - Refund implementation remains future work; current admin screen still cannot execute provider refund or entitlement correction.
  - Settlement policy covers ATStudio merchant settlement only, not creator payout.
- Rollback:
  - Revert this WI commit.
  - Remove `docs/design/payment-refund-receipt-settlement-policy.md`.
  - Restore `docs/design/index.md`, `docs/index.md`, `docs/SR/SR-93.md`, `docs/design/payment-operations-runbook.md`, and `docs/design/payment-integration-design.md` to the prior state.

## Follow-ups

- P2-A: payment operations audit ledger and receipt evidence storage.
- P2-B: admin refund request/approval/execution workflow with Toss cancel API and idempotency key.
- P2-C: entitlement correction workflow linked to refund decisions.
- P2-D: settlement import and reconciliation.
- P2-E: tax invoice request/admin tracking.
