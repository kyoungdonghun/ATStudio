---
version: 1.0
last_updated: 2026-08-12
project: ATS
owner: qa-integ
category: agent
status: accepted
dependencies:
  - path: WI-20260809-ATS-034-handoff.md
    reason: Approved cross-layer acceptance contract
  - path: ../../docs/design/payment-integration-design.md
    reason: Final recovery invariants
---

# QA-INTEG Review: WI-20260809-ATS-034

## Final Decision

**APPROVE**

The recorded QA-INTEG review initially returned **BLOCK** with four findings.
All four are remediated in the final bounded implementation and regression
evidence.

## Review History

| Initial BLOCK finding | Final remediation evidence |
| --- | --- |
| Fail callback did not reconcile the owner order | Success and fail callbacks both invoke the owner-scoped read when `orderId` is present; fail callback does not infer failure from the route. |
| Scheduled-change recovery lacked source invariants | Canonical proof binds source aggregate ID, source plan/cycle, and exact pending target plan/cycle for scheduled change and downgrade. |
| Manage success did not require aggregate linkage | Charged upgrade requires exact `DONE` outcome, non-null `userSubscriptionId`, matching canonical Subscription ID, and Billing Agreement linkage. Local operations also require canonical Subscription/Billing Agreement agreement. |
| Recovery race could allow stale overwrite or duplicate action | Mutation and recovery in-flight fences deduplicate rapid actions, and version checks prevent stale recovery results from overwriting newer state. |

## Cross-Layer Decision

| Lane | Final contract |
| --- | --- |
| UI | `UNKNOWN` and `RELOAD_FAILED` retain context, disable all Manage mutations, and expose read-only status recheck. |
| API | Two USER-only owner-scoped GETs return the minimal five-field outcome DTO. |
| Server | Callback uses exact `orderId`; upgrade uses the deterministic current-period command key and never a latest-order guess. |
| Provider | Recovery reads and UI retries invoke no Provider or SDK mutation. |
| Durable state | `COMMITTED` requires order plus canonical Subscription/Billing Agreement aggregate proof; ambiguous reads stay `UNKNOWN`. |

## Evidence Limit

The final decision is based on source, automated tests, and supplied full-suite
results. It is not live Toss, deployed browser, production database, or
retained-state evidence.

## Related Documents

- [WI-034 Handoff](WI-20260809-ATS-034-handoff.md)
- [WI-034 Evidence Pack](WI-20260809-ATS-034-evidence-pack.md)
- [Payment Integration Design](../../docs/design/payment-integration-design.md)
