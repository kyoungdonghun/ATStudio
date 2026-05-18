# Evidence Pack: WI-20260518-ATS-018

## Summary (one-liner)
- Identified frontend impact and future validation scope for payment UX implementation.

## Scope / DoD Check
- DoD items:
  - [x] Identified likely affected frontend files.
  - [x] Listed future tests for one-time and recurring payment UX.
  - [x] Preserved current inline debug behavior as temporary.

## Reference Documents (Tier 0-2)
- `docs/standards/core-principles.md`
- `docs/standards/development-standards.md`
- `.agents/skills/react-best-practices/SKILL.md`
- `docs/standards/frontend-standards.md`
- `docs/ui/screen-flow.md`
- `docs/ui/modal-list.md`

## Evidence Pointers
- Files changed:
  - `docs/ui/screen-flow.md`
  - `docs/ui/modal-list.md`
  - `deliverables/user/WI-20260518-ATS-018-summary.md`
- Future affected files:
  - `frontend/src/pages/subscriber/SubscriptionPaymentPage.tsx`
  - `frontend/src/pages/subscriber/SubscriptionPaymentPage.test.tsx`
  - `frontend/src/pages/subscriber/SubscriptionManagePage.tsx`
  - `frontend/src/pages/subscriber/SubscriptionManagePage.test.tsx`
  - `frontend/src/router/index.tsx`

## Commands & Outputs
- `python .agents/skills/validate-docs/scripts/validate_docs.py` — passed.

## Tests
- Future implementation should run `npm test`, focused payment page tests, `npm run typecheck`, `npm run lint`, and `npm run build`.

## Risks / Rollback
- Risks: modal implementation may conflict with Toss iframe sizing and mobile return behavior.
- Rollback: revert docs and later frontend changes if implemented.

## Follow-ups
- Split future frontend implementation into checkout surface and subscription management display WIs.
