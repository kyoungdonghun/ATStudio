# Evidence Pack: WI-20260517-ATS-004

## Summary (one-liner)
- Completed backend, frontend, formatting, lint, and documentation verification for Toss Phase B.

## Scope / DoD Check
- DoD items:
  - [x] Backend full test suite passes.
  - [x] Focused frontend payment tests pass.
  - [x] Frontend typecheck passes.
  - [x] Frontend lint passes.
  - [x] Touched frontend files pass Prettier.
  - [x] Documentation validation passes.
  - [x] Diff contains no real Toss secret values.

## Reference Documents (Tier 0-2)

**Injected Context**:

| Tier | Document | Reason |
|------|----------|--------|
| 0 | docs/standards/core-principles.md | Quality and traceability |
| 0 | docs/standards/development-standards.md | Test standards |
| 1 | docs/policies/quality-gates.md | Verification gate |
| 3 | deliverables/agent/WI-20260517-ATS-001-evidence-pack.md | Backend evidence |
| 3 | deliverables/agent/WI-20260517-ATS-002-evidence-pack.md | Frontend evidence |
| 3 | deliverables/agent/WI-20260517-ATS-003-evidence-pack.md | Documentation evidence |

## Evidence Pointers
- Verification commands:
  - `./gradlew.bat test`
  - `npm test -- SubscriptionPaymentPage.test.tsx SubscriptionManagePage.test.tsx`
  - `npm run typecheck`
  - `npm run lint`
  - `npx prettier --check src/api/payments.ts src/utils/tossPayments.ts src/pages/subscriber/SubscriptionPaymentPage.tsx src/pages/subscriber/SubscriptionPaymentPage.module.css src/pages/subscriber/SubscriptionPaymentPage.test.tsx src/pages/subscriber/SubscriptionManagePage.tsx src/pages/subscriber/SubscriptionManagePage.test.tsx`
  - `python .agents/skills/validate-docs/scripts/validate_docs.py`
- Secret scan:
  - `git diff --stat` and manual diff review showed only env placeholders such as `TOSS_SECRET_KEY`, not real key values.

## Commands & Outputs
- `./gradlew.bat test` -> pass.
- `npm test -- SubscriptionPaymentPage.test.tsx SubscriptionManagePage.test.tsx` -> pass, 2 files / 8 tests.
- `npm run typecheck` -> pass.
- `npm run lint` -> pass.
- `npx prettier --check ...` -> pass.
- `python .agents/skills/validate-docs/scripts/validate_docs.py` -> pass.

## Tests
- Backend: full Gradle suite pass.
- Frontend: focused payment Vitest pass, typecheck pass, lint pass.
- Docs: validation pass.

## Risks / Rollback
- Risks:
  - Vitest required sandbox escalation because esbuild spawn failed with `EPERM` in the sandbox.
  - Manual browser verification with Toss test keys is still recommended before calling Phase B UX fully accepted.
- Rollback:
  - Revert files changed in WI-001, WI-002, and WI-003.

## Follow-ups
- User/manual verification with Toss test keys.
- Phase C: Toss recurring billing/billing-key design implementation.
