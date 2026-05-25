# Evidence Pack: WI-20260525-ATS-004

## Summary
- Defined the refund ledger/provider cancel boundary before implementation.

## Scope / DoD Check
- [x] Refund preview, request, approve, execute, list, and detail boundaries defined.
- [x] Full and partial refund amount rules defined.
- [x] Provider idempotency key persistence required before provider execution.
- [x] Entitlement correction separated from provider refund execution.
- [x] Sensitive-data boundaries preserved.

## Evidence Pointers
- `deliverables/user/REQ-20260525-ATS-004.md` — approved requirement.
- `deliverables/agent/WI-20260525-ATS-004-handoff.md` — design handoff.
- `docs/design/payment-refund-receipt-settlement-policy.md` — policy updated to implemented refund boundary.
- `docs/design/payment-operations-runbook.md` — operational refund execution sequence.

## Verification
- Covered by implementation and validation WIs 005-008.

## Rollback
- Remove P2-B refund design changes from policy/runbook/SR/API/DB docs and keep P2-B as future scope.
