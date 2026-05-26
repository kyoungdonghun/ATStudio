# Evidence Pack: WI-20260526-ATS-001

## Summary

- Added settlement source adapter and CSV-first import/reconciliation design for REQ-20260526-ATS-001.

## Scope / DoD Check

- [x] Settlement source abstraction is documented.
- [x] CSV import template and validation rules are documented.
- [x] Matching/mismatch rules are documented.
- [x] Admin API/UI expectations are documented.
- [x] Future Toss Settlement API adapter path remains open but not implemented.

## Reference Documents

| Tier | Document | Reason |
|------|----------|--------|
| 0 | docs/standards/core-principles.md | Constitution |
| 0 | docs/standards/development-standards.md | Settlement design consistency |
| 0 | docs/standards/documentation-standards.md | Documentation standard |
| 0 | docs/standards/glossary.md | Domain terminology |
| 1 | docs/policies/security-policy.md | Sensitive data boundary |
| 2 | docs/design/payment-refund-receipt-settlement-policy.md | Existing payment operations policy |
| 2 | docs/SR/SR-93.md | Production readiness tracker |

## Evidence Pointers

- `deliverables/user/REQ-20260526-ATS-001.md` - approved settlement REQ and scope.
- `docs/design/payment-settlement-import-design.md` - new detailed settlement import/reconciliation design.
- `docs/design/payment-refund-receipt-settlement-policy.md` - source adapter policy and decision record updated.
- `docs/design/index.md` - design index updated with the new settlement design document.
- `docs/SR/SR-93.md` - settlement slice marked as in progress and scoped to CSV/manual adapter first.

## Commands & Outputs

- `python .agents/skills/validate-docs/scripts/validate_docs.py` -> passed.
- `git diff --check` -> passed with CRLF warnings only.

## Risks / Rollback

- Risk: actual Toss settlement export columns may differ from the initial ATStudio template and may require an adapter patch.
- Risk: fee/VAT/net formula may vary by provider contract, so mismatch policy should remain warning-first until live data confirms the contract behavior.
- Rollback: revert `docs/design/payment-settlement-import-design.md` and related updates to `REQ-20260526-ATS-001`, `payment-refund-receipt-settlement-policy.md`, `docs/design/index.md`, and `SR-93.md`.

## Follow-ups

- WI-20260526-ATS-002: Backend settlement ledger/import/reconciliation API.
- WI-20260526-ATS-003: Security/privacy boundary review.
