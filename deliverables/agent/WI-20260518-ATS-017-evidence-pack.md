# Evidence Pack: WI-20260518-ATS-017

## Summary (one-liner)
- Updated API/DB/payment design docs for billing agreements and payment operations candidates.

## Scope / DoD Check
- DoD items:
  - [x] Added billing agreement API entries.
  - [x] Updated API total from 110 to 114.
  - [x] Added `billing_agreements` to DB docs and updated table total from 29 to 30.
  - [x] Marked admin payment operations endpoints as candidates only.

## Reference Documents (Tier 0-2)
- `docs/standards/core-principles.md`
- `docs/standards/documentation-standards.md`
- `docs/standards/glossary.md`
- `docs/policies/security-policy.md`
- `docs/design/payment-integration-design.md`
- `docs/design/api-spec.md`
- `docs/design/db-schema.md`

## Evidence Pointers
- Files changed:
  - `docs/design/api-spec.md`
  - `docs/design/db-schema.md`
  - `docs/design/payment-integration-design.md`
- Key locations:
  - `docs/design/api-spec.md:1231` — prepare billing agreement.
  - `docs/design/api-spec.md:1265` — confirm billing agreement.
  - `docs/design/api-spec.md:1353` — operations candidates.
  - `docs/design/db-schema.md:368` — billing agreements table.
  - `docs/design/db-schema.md:693` — 30-table summary.

## Commands & Outputs
- `python .agents/skills/validate-docs/scripts/validate_docs.py` — passed.

## Risks / Rollback
- Risks: candidate endpoints must not be mistaken for implemented endpoints.
- Rollback: revert design/API/DB docs and this WI output.

## Follow-ups
- Build admin read-only operations APIs only after a separate implementation approval.
