# Evidence Pack: WI-20260516-ATS-007

## Summary (one-liner)
- Updated API, DB, and UI docs for Mock-first payment implementation.

## Scope / DoD Check
- DoD items:
  - [x] API spec includes new payment endpoints.
  - [x] DB schema includes payment order ledger.
  - [x] UI docs no longer describe M-26/M-27 as fully blocked PG items.
  - [x] Docs validation passes.

## Reference Documents (Tier 0-2)

| Tier | Document | Reason |
|------|----------|--------|
| 0 | docs/standards/core-principles.md | Constitution |
| 0 | docs/standards/documentation-standards.md | Documentation standards |
| 0 | docs/standards/glossary.md | Glossary |
| 2 | docs/design/payment-integration-design.md | Payment design |
| 2 | docs/design/api-spec.md | API spec |
| 2 | docs/ui/screen-flow.md | UI flow |
| 2 | docs/ui/modal-list.md | Modal list |

## Evidence Pointers
- Files changed:
  - `docs/design/api-spec.md`
  - `docs/design/db-schema.md`
  - `docs/ui/screen-flow.md`
  - `docs/ui/modal-list.md`

## Commands & Outputs
- `python .agents/skills/validate-docs/scripts/validate_docs.py` -> passed.

## Risks / Rollback
- Risks:
  - API spec count updated to 110; future API additions should preserve count consistency.
- Rollback:
  - Revert the four doc files.
