# Evidence Pack: WI-20260521-ATS-010

## Summary
- Updated payment design, API, UI, SR, scenario, DB notes, and project index documents for the 2026-05-21 hardening slice.

## Scope / DoD Check
- [x] SR-92 remains dropped and SR-93 now records the implemented slice.
- [x] API spec reflects one-time subscription block, checkout callback URLs, and admin payment APIs.
- [x] UI docs point to `/subscriptions/checkout` and `/admin/payments`.
- [x] Payment design distinguishes implemented local reconciliation from future provider webhook/API reconciliation.

## Evidence Pointers
- `docs/SR/SR-93.md`
- `docs/design/api-spec.md`
- `docs/design/payment-integration-design.md`
- `docs/design/db-schema.md`
- `docs/ui/atstudio-front-list.md`
- `docs/ui/screen-flow.md`
- `docs/ui/modal-list.md`
- `docs/client/1-scenarios.md`
- `docs/index.md`
- `docs/SR/SR-38.md`

## Validation
- `python .agents/skills/validate-docs/scripts/validate_docs.py`: passed.

## Risks / Rollback
- API count is updated to 117 because three read-only admin payment APIs were added.
- Rollback by reverting the listed documentation updates.
