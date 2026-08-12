---
version: 1.0
last_updated: 2026-08-12
project: ATS
owner: pg
category: agent
status: accepted
dependencies:
  - path: WI-20260809-ATS-034-handoff.md
    reason: Approved security and recovery boundary
  - path: ../../docs/design/api-spec.md
    reason: Final owner-scoped outcome API contract
---

# PG Review: WI-20260809-ATS-034

## Final Decision

**APPROVE**

The recorded PG review initially returned **BLOCK** with four findings. The
final bounded implementation and tests remediate all four, with no remaining
security blocker.

## Review History

| Initial BLOCK finding | Final remediation evidence |
| --- | --- |
| Callback `authKey` and `customerKey` remained in the browser URL | Callback values are captured once and both keys are removed immediately with history replacement before confirmation settles. They are not rendered or returned by outcome APIs. |
| Broad HTTP `4xx` handling could claim authoritative failure | Only narrow local cancel/reactivate business errors are terminal without reads. Every CHANGE error reconciles, and financial failure requires exact terminal order evidence. |
| Subscription `ACTIVE` state alone could be treated as success | `COMMITTED` requires exact target plan/cycle plus order-to-Subscription aggregate linkage; state alone is insufficient. |
| Billing Agreement linkage was not required | Callback and Manage canonical reads require the Billing Agreement to reference the same `userSubscriptionId` aggregate and target identity before success. |

## Security Boundary

- Both outcome endpoints are USER-only and owner-scoped server-side.
- Foreign and absent orders are indistinguishable through the same
  `PAYMENT_ORDER_NOT_FOUND` result.
- Outcome responses expose only purpose, order status, aggregate linkage ID,
  exact target plan ID, and target cycle. They contain no secret, Provider
  payload, payment method, or PII.
- Outcome reads perform zero Provider calls, zero mutations, and zero local
  finalization. Recovery controls never automatically replay a financial
  command.

## Evidence Limit

The final decision relies on the bounded source, automated H2/Test-Provider
tests, and supplied green verification evidence. No real Toss/SDK operation,
charge, refund, mail, retained database, deployment, or secret inspection was
performed.

## Related Documents

- [WI-034 Handoff](WI-20260809-ATS-034-handoff.md)
- [WI-034 Evidence Pack](WI-20260809-ATS-034-evidence-pack.md)
- [API Specification](../../docs/design/api-spec.md)
