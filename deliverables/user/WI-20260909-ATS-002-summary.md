---
version: 1.1
last_updated: 2026-09-09
project: ATS
owner: se
category: summary
status: verification-pending
---

# WI-20260909-ATS-002: Payment Lifecycle Fences

Implemented PAY-01/02 and the WI011 F1 follow-up in five owned product files and seven test files. No schema/entity fields, runtime, Provider operations, real database actions or Git commands were involved.

- Returning subscribers retain historical rows and purchase at full price. New orders bind the retained expired source; claim, finalization and recovery reject an active or replaced source.
- Competing upgrade targets/cycles cannot acquire a second monetary intent while another upgrade is unresolved. Finalization checks its original priced source; current paid periods and future billing-cycle choices remain unchanged.
- Existing command fields hold the source hash. Price scale is normalized; real source-price changes fail closed. Request-key recovery recognizes bound keys and explicitly rejects multiple matching snapshots instead of choosing one.
- Fake-Provider/H2 tests cover returning purchase, durable/UNKNOWN recovery, independent-context competing requests with distinct transaction IDs, source mutations, legacy pending orders and DONE replay.
- WI011 F1: cancellation/reactivation/pending mutations now lock agreement then subscription before reading state. Unresolved monetary commands reject these mutations as busy, preventing both stale post-DONE overwrites and ordinary cancellation stranding an in-flight paid upgrade. Exact finalize-only retries and DONE-aftercare policy remain unchanged.
- Ten new deterministic cases cover the legacy stale-write control, four actual mutation interleavings, the opposite lock order, held fake-Provider success/UNKNOWN, finalize-only retry and cancellation after definitive failure.

## Verification

Java/test source is stable. Tests were **not run by this assignee**; no original-code red result or production success is claimed. WI011 reported an earlier MA run with 72 passes and three guarded MySQL skips; that historical result does not validate this follow-up. F1 closure requires current MA execution and WI011 re-review.

First follow-up: SubscriptionMutationFenceIntegrationTest and UserSubscriptionServiceTest. Rerun PaymentReturningSubscriberIntegrationTest, SubscriptionUpgradeCommandIntegrationTest, PaymentCommandKeyFactoryTest and PaymentReconciliationRecoveryIntegrationTest. MA has readiness for the full BE build; exact focused commands are in the Evidence Pack.

## Deployment Boundary

Old unfinished unbound upgrades and returning-purchase orders without source evidence cannot be safely retrofitted. Inventory and drain/reconcile them in a separately approved pre-deployment scope; already charged legacy returning orders may need explicit reviewed remediation. Do not mix old/new command writers or blindly downgrade after creating bound commands. Legacy DONE replay stays read-only without another charge.

No prospective DDL or product-policy decision remains. Legacy-order disposition and test/review acceptance are still open; this report is not deployment GO.

## Handoff

MA should return this correction to WI011, then coordinate WI006/WI013/WI014. Exact changed paths, implementation pointers, pending tests, risks and rollback boundaries are recorded in [WI002 Evidence Pack](../agent/WI-20260909-ATS-002-evidence-pack.md).
