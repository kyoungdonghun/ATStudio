# Evidence Pack: WI-20260519-ATS-004

## Summary (one-liner)
- Aligned payment, API, DB, UI, use case, and SR documentation with the recurring-first subscription policy.

## Scope / DoD Check
- DoD items:
  - [x] SR-92 is marked dropped.
  - [x] SR-93 captures operating-server readiness and follow-up development points.
  - [x] Payment integration design states recurring-first subscription policy.
  - [x] API spec describes upgrade through subscription change endpoint with billing-key charge.
  - [x] DB schema notes recurring upgrade charge and pending downgrade behavior.
  - [x] UI flow no longer points upgrade to one-time checkout.
  - [x] Docs validation passes.

## Reference Documents (Tier 0-2)

**Injected Context** (from WI Handoff Packet):

| Tier | Document | Reason |
|------|----------|--------|
| 0 | `docs/standards/core-principles.md` | Constitution |
| 0 | `docs/standards/documentation-standards.md` | Documentation rules |
| 0 | `docs/standards/glossary.md` | Terminology |
| REQ | `deliverables/user/REQ-20260519-ATS-001.md` | Approved behavior |
| Design | `docs/design/payment-integration-design.md` | Payment design |
| Design | `docs/design/api-spec.md` | API contract |
| Design | `docs/design/db-schema.md` | DB contract |
| Design | `docs/design/usecase/user-subscription.md` | Use case |
| UI | `docs/ui/screen-flow.md` | Flow documentation |
| UI | `docs/ui/modal-list.md` | Modal documentation |
| UI | `docs/ui/atstudio-front-list.md` | Screen/API mapping |
| SR | `docs/SR/index.md` | SR registry |
| SR | `docs/SR/SR-92.md` | Retired SR item |

**Injection Rules Applied**:
- Rule source: `AGENTS.md` and WI handoff packet.
- Assignee: `docops`.
- Task type: documentation alignment.

## Evidence Pointers
- Files changed:
  - `docs/design/payment-integration-design.md:25` - recurring-first design purpose.
  - `docs/design/payment-integration-design.md:599` - one-time checkout path marked legacy/test-only for subscription scope.
  - `docs/design/api-spec.md:1397` - subscription change endpoint upgrade semantics.
  - `docs/design/api-spec.md:2301` - preview semantics.
  - `docs/design/db-schema.md:188` - `user_subscriptions` upgrade/downgrade storage behavior.
  - `docs/design/usecase/user-subscription.md:181` - upgrade use case flow.
  - `docs/ui/screen-flow.md:215` - SR-92 inline one-time widget UX retired.
  - `docs/ui/modal-list.md:97` - billing auth checkout modal mapping.
  - `docs/ui/atstudio-front-list.md` - subscription screen/API mapping updated.
  - `docs/SR/SR-92.md:1` - dropped item.
  - `docs/SR/SR-93.md:1` - new operating-server checklist.
  - `docs/SR/index.md:97` - SR status and registry update.
  - `docs/index.md:27` - SR document count update.

## Commands & Outputs
- `python .agents\skills\validate-docs\scripts\validate_docs.py` - passed.

## Tests
- Documentation validation passed: Tier 0 exists, no broken internal links, 224 traceability IDs matched, all documents listed in index.

## Risks / Rollback
- Risks:
  - SR-93 is a checklist/development-point document, not an implementation of operating-server recovery tooling.
  - One-time provider code remains in the codebase, so future docs must keep distinguishing provider capability from user-facing subscription flow.
- Rollback:
  - Revert the listed documentation files before commit, or revert the final commit after commit.

## Follow-ups
- Convert SR-93 items into future REQ/WI slices when moving toward live operation.

