# Evidence Pack: WI-20260519-ATS-003

## Summary (one-liner)
- Updated frontend subscription plan change flow to call the subscription change API instead of one-time checkout for upgrades.

## Scope / DoD Check
- DoD items:
  - [x] Upgrade confirm calls `changeMySubscription`.
  - [x] Upgrade success copy explains immediate prorated charge and preserved next billing date.
  - [x] Downgrade pending flow remains on the manage page.
  - [x] Direct `purpose=UPGRADE` payment route is guarded.
  - [x] Focused frontend tests pass.

## Reference Documents (Tier 0-2)

**Injected Context** (from WI Handoff Packet):

| Tier | Document | Reason |
|------|----------|--------|
| 0 | `docs/standards/core-principles.md` | Constitution |
| 0 | `docs/standards/development-standards.md` | Development rules |
| 2 | `.agents/skills/react-best-practices/AGENTS.md` | React UI guidance |
| 2 | `docs/standards/frontend-standards.md` | Frontend standard |
| REQ | `deliverables/user/REQ-20260519-ATS-001.md` | Approved behavior |
| UI | `docs/ui/screen-flow.md` | Flow source |
| UI | `docs/ui/modal-list.md` | Confirm modal source |

**Injection Rules Applied**:
- Rule source: `AGENTS.md` and WI handoff packet.
- Assignee: `se`.
- Task type: frontend implementation.

## Evidence Pointers
- Files changed:
  - `frontend/src/pages/subscriber/SubscriptionManagePage.tsx:152` - plan change handler uses `changeMySubscription`.
  - `frontend/src/pages/subscriber/SubscriptionManagePage.tsx:425` - preview label distinguishes upgrade charge difference.
  - `frontend/src/pages/subscriber/SubscriptionPaymentPage.tsx:166` - direct upgrade payment route is blocked.
  - `frontend/src/pages/subscriber/SubscriptionManagePage.test.tsx:201` - upgrade asserts API call and no navigation.
  - `frontend/src/pages/subscriber/SubscriptionPaymentPage.test.tsx:238` - direct upgrade route guard test.

## Commands & Outputs
- `npm test -- SubscriptionManagePage.test.tsx SubscriptionPaymentPage.test.tsx` - passed.
- `npm run typecheck` - passed.
- `npm run lint` - passed.

## Tests
- Focused Vitest: passed, 2 files / 12 tests.
- Typecheck: passed.
- ESLint: passed.

## Risks / Rollback
- Risks:
  - Existing bookmarked upgrade payment URLs now show a guard message instead of starting checkout.
  - The payment page still contains legacy one-time `purpose=UPGRADE` display branches for callback compatibility; it does not prepare a user-facing upgrade checkout.
- Rollback:
  - Revert the listed frontend implementation and test files before commit, or revert the final commit after commit.

## Follow-ups
- Production UX can later improve the payment method missing case and recovery from billing charge failure.

