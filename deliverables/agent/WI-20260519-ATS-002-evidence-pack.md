# Evidence Pack: WI-20260519-ATS-002

## Summary (one-liner)
- Implemented backend recurring-billing upgrade charges and aligned preview calculation.

## Scope / DoD Check
- DoD items:
  - [x] Active billing agreement is required for upgrade.
  - [x] Upgrade amount is calculated from the remaining current period.
  - [x] Upgrade order uses `PaymentPurpose.UPGRADE` and `PaymentProviderType.TOSS_BILLING`.
  - [x] Subscription mutates only after provider charge success.
  - [x] Upgrade preserves current `expiresAt` and stores requested `billingCycle` for future renewal.
  - [x] Downgrade remains pending-only.

## Reference Documents (Tier 0-2)

**Injected Context** (from WI Handoff Packet):

| Tier | Document | Reason |
|------|----------|--------|
| 0 | `docs/standards/core-principles.md` | Constitution |
| 0 | `docs/standards/development-standards.md` | Backend implementation standard |
| 1 | `docs/policies/security-policy.md` | Billing key and secret handling |
| 1 | `docs/policies/quality-gates.md` | Verification gate |
| REQ | `deliverables/user/REQ-20260519-ATS-001.md` | Approved behavior |
| Design | `docs/design/payment-integration-design.md` | Target payment architecture |
| Design | `docs/design/api-spec.md` | Subscription change contract |

**Injection Rules Applied**:
- Rule source: `AGENTS.md` and WI handoff packet.
- Assignee: `se`.
- Task type: backend implementation.

## Evidence Pointers
- Files changed:
  - `src/main/java/com/atstudio/atstudio/entity/UserSubscription.java:68` - added `upgradeKeepingPeriod`.
  - `src/main/java/com/atstudio/atstudio/service/UserSubscriptionService.java:198` - upgrade now calculates amount, resolves billing agreement, creates order, charges, then applies.
  - `src/main/java/com/atstudio/atstudio/service/UserSubscriptionService.java:248` - active billing agreement lookup.
  - `src/main/java/com/atstudio/atstudio/service/UserSubscriptionService.java:257` - remaining-period upgrade amount calculation.
  - `src/main/java/com/atstudio/atstudio/service/UserSubscriptionService.java:275` - upgrade payment order creation.
  - `src/main/java/com/atstudio/atstudio/service/UserSubscriptionService.java:299` - recurring provider charge.
  - `src/main/java/com/atstudio/atstudio/service/UtilService.java` - preview calculation aligned with backend charge policy.
  - `src/test/java/com/atstudio/atstudio/service/UserSubscriptionServiceTest.java:338` - recurring upgrade success test.
  - `src/test/java/com/atstudio/atstudio/service/UserSubscriptionServiceTest.java:416` - missing billing agreement failure test.
  - `src/test/java/com/atstudio/atstudio/service/UtilServiceTest.java:254` - preview upgrade amount test.

## Commands & Outputs
- `.\gradlew.bat test --tests "com.atstudio.atstudio.service.UserSubscriptionServiceTest" --tests "com.atstudio.atstudio.service.UtilServiceTest" --tests "com.atstudio.atstudio.service.BillingAgreementApplicationServiceTest" --tests "com.atstudio.atstudio.controller.PaymentControllerTest"` - passed.

## Tests
- Focused backend tests: passed.
- Full backend tests: covered in WI-20260519-ATS-005.

## Risks / Rollback
- Risks:
  - Provider charge can succeed while later local persistence fails; SR-93 tracks operational compensation/reconciliation hardening.
  - Current implementation charges the prorated difference for the existing active billing cycle, while selected billing cycle is used for next renewal.
- Rollback:
  - Revert the listed backend implementation and test files before commit, or revert the final commit after commit.

## Follow-ups
- Add production reconciliation/refund playbook under SR-93 before live operation.
