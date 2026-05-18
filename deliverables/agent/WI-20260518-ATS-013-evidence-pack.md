# Evidence Pack: WI-20260518-ATS-013

## Summary (one-liner)
- Defined user-facing payment UX state transitions and checkout surface recommendation.

## Scope / DoD Check
- DoD items:
  - [x] Covered one-time Toss and Toss billing auth user flows.
  - [x] Captured page-fixed, modal/drawer, and dedicated route tradeoffs.
  - [x] Separated user-facing safe copy from debug/operator metadata.

## Reference Documents (Tier 0-2)
- `docs/standards/core-principles.md`
- `.agents/skills/react-best-practices/SKILL.md`
- `docs/ui/screen-flow.md`
- `docs/ui/modal-list.md`
- `docs/SR/SR-92.md`
- `docs/design/payment-integration-design.md`

## Evidence Pointers
- Files changed:
  - `docs/design/payment-integration-design.md` — added Phase D checkout UX and state guidance.
  - `docs/ui/screen-flow.md` — added Toss billing auth and UX target notes.
  - `docs/ui/modal-list.md` — updated M-26 and added M-31.
  - `docs/SR/SR-92.md` — added REQ-20260518 decision notes.
- Key locations:
  - `docs/design/payment-integration-design.md:622` — Phase D start.
  - `docs/ui/screen-flow.md:212` — UX target.
  - `docs/SR/SR-92.md:59` — current decision.

## Commands & Outputs
- `python .agents/skills/validate-docs/scripts/validate_docs.py` — passed.
- `git diff --check` — passed with CRLF warnings only.

## Risks / Rollback
- Risks: modal/drawer still needs real viewport and Toss iframe verification before implementation.
- Rollback: revert the four documentation files above plus this WI output.

## Follow-ups
- Implement checkout presentation split in a later implementation WI.
