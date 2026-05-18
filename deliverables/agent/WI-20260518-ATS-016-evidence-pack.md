# Evidence Pack: WI-20260518-ATS-016

## Summary (one-liner)
- Updated UI docs for checkout separation and subscription management auto-renewal display.

## Scope / DoD Check
- DoD items:
  - [x] Screen flow includes Toss billing auth and billing confirm.
  - [x] Subscription manage flow includes auto-renewal status, masked method, next billing date, grace state, and cancel auto-renewal.
  - [x] Modal list includes M-31 for billing auth checkout.

## Reference Documents (Tier 0-2)
- `docs/standards/core-principles.md`
- `docs/standards/documentation-standards.md`
- `docs/standards/glossary.md`
- `docs/ui/screen-flow.md`
- `docs/ui/modal-list.md`
- `docs/SR/SR-92.md`

## Evidence Pointers
- Files changed:
  - `docs/ui/screen-flow.md`
  - `docs/ui/modal-list.md`
  - `docs/SR/SR-92.md`
- Key locations:
  - `docs/ui/screen-flow.md:207` — `TOSS_BILLING` flow.
  - `docs/ui/screen-flow.md:224` — subscription manage auto-renewal states.
  - `docs/ui/modal-list.md:102` — M-31 billing auth entry.

## Commands & Outputs
- `python .agents/skills/validate-docs/scripts/validate_docs.py` — passed.
- `git diff --check` — passed with CRLF warnings only.

## Risks / Rollback
- Risks: future UI implementation must still be verified visually on desktop/mobile.
- Rollback: revert UI docs and this WI output.

## Follow-ups
- Use Browser/Playwright-style visual verification when implementing checkout modal/route.
