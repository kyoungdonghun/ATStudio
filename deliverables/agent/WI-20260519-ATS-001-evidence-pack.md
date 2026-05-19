# Evidence Pack: WI-20260519-ATS-001

## Summary (one-liner)
- Confirmed recurring-first subscription change policy and retired the user-facing one-time upgrade path.

## Scope / DoD Check
- DoD items:
  - [x] Upgrade policy uses an active billing agreement and immediate recurring charge.
  - [x] Upgrade policy preserves the current next billing date.
  - [x] Downgrade policy remains pending-only with no immediate charge.
  - [x] Plan change UI remains preview-first and confirm-driven.
  - [x] SR-92 retirement is tracked through documentation updates.

## Reference Documents (Tier 0-2)

**Injected Context** (from WI Handoff Packet):

| Tier | Document | Reason |
|------|----------|--------|
| 0 | `docs/standards/core-principles.md` | Constitution |
| 0 | `docs/standards/development-standards.md` | Development rules |
| REQ | `deliverables/user/REQ-20260519-ATS-001.md` | Approved scope |
| Design | `docs/design/payment-integration-design.md` | Payment policy source |
| Design | `docs/design/api-spec.md` | API contract |
| Design | `docs/design/db-schema.md` | Persistence contract |
| Design | `docs/design/usecase/user-subscription.md` | Subscription use case |
| UI | `docs/ui/screen-flow.md` | User flow |
| UI | `docs/ui/modal-list.md` | Modal and confirm flow |
| SR | `docs/SR/SR-92.md` | Retired SR item |

**Injection Rules Applied**:
- Rule source: `AGENTS.md` and WI handoff packet.
- Assignee: `sa`.
- Task type: policy/design alignment.

## Evidence Pointers
- Files changed:
  - `deliverables/user/REQ-20260519-ATS-001.md` - approved policy and scope.
  - `docs/design/payment-integration-design.md:25` - recurring-first payment purpose.
  - `docs/design/payment-integration-design.md:99` - one-time subscription payment reduced to legacy/test-only.
  - `docs/design/payment-integration-design.md:476` - upgrade recurring charge process.
  - `docs/SR/SR-92.md:5` - SR-92 replacement status.
  - `docs/SR/SR-93.md:1` - production readiness follow-up item.

## Commands & Outputs
- No standalone command was required for policy confirmation.

## Tests
- Covered by WI-20260519-ATS-005 verification gates.

## Risks / Rollback
- Risks:
  - Existing users without billing agreements cannot upgrade until a payment method registration path is used.
  - One-time payment provider code still exists for legacy/test/provider capability and must not be reintroduced into the subscription upgrade UI.
- Rollback:
  - Revert `REQ-20260519-ATS-001` deliverables and the policy portions of the changed payment/UI/SR docs.

## Follow-ups
- Use SR-93 for operating-server readiness and recovery hardening.

