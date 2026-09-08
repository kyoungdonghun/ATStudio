---
version: 1.2
last_updated: 2026-09-09
project: ATS
owner: qa-integ
category: summary
status: active
---

# WI-20260909-ATS-011: Independent Payment Review

> Current disposition, 2026-09-09 executed-evidence update: F1 CLOSED in the bounded local/fake-provider/H2 scope. New regression 10/10 and selected related classes 132/132 passed; no new payment blockers. The full backend build remains FAILED on an unrelated fixture, with full-build/coverage acceptance outstanding. Original findings and interim checkpoints below remain historical evidence; the final update is current.

## Result

Bounded PAY-01/02 review completed. One P1 residual needs MA's focused reproduction/disposition before unconditional payment acceptance.

**F1: local self-cancellation can overwrite a completed upgrade using an earlier unlocked subscription read.** `UserSubscriptionService.java:199-206` reads without a lock/version. If an upgrade finalizes after that read but before cancellation commits, the stale entity update can restore the old tier while the upgrade order/payment remain DONE. This is a source-grounded counterexample, not an executed H2 or MySQL failure. The existing source-change tests commit the change before finalization and do not cover this opposite commit order.

The bounded remedy is consistent locking and paid-tier preservation, not a cancellation/refund policy change. Product code was not changed by this reviewer.

## Confirmed Scope

- Returning expired subscriptions are source-bound and reuse the retained row while preserving existing payment records.
- Competing target/cycle upgrades are fenced while the first command is unresolved; durable success and UNKNOWN use guarded recovery.
- Source plan/period/cycle/status/prices, exact command-family lookup, ambiguity rejection and read-only DONE replay were checked.
- The old private `createUpgradeOrder` / `chargeUpgrade` methods have no callers and were not reported as bypasses.
- Existing cancellation, renewal and ADMIN correction interactions were traced only where they meet the changed source/finalization boundary.
- Old-format unfinished commands require the documented admission/drain/reconciliation boundary. No deployment or historical repair occurred.

## Verification Boundary

| Evidence | Result |
| --- | --- |
| Existing MA payment-initial result inspected | 7 suites, 75 tests, 72 passed, 3 guarded MySQL skips, 0 failures/errors |
| WI011 execution | Read-only source/diff/test/report inspection and two assigned report files only |
| New F1 regression | Reproduction sequence supplied; not authored or executed here |
| MySQL / real Provider / runtime / deployment | Not verified |

The observed local fake-provider/H2 results do not establish MySQL concurrency or production correctness. No Gradle/npm runner, real DB, Provider/mail operation, Git write, restart or product edit was performed by WI011.

## Handoff

MA should return F1 to the implementation owner, serialize its isolated regression and relevant payment compatibility runs, then update WI013/WI014 with the disposition. WI011 review work is complete with a finding open; REQ/payment acceptance is not unconditionally closed.

Details, exact source pointers, reviewed hashes, counterexample steps and test readiness: [WI011 Evidence Pack](../agent/WI-20260909-ATS-011-evidence-pack.md).

## Re-review: 2026-09-09

**F1 implementation is addressed; no new blockers found in the bounded correction.** The current local cancellation/reactivation/pending/zero-amount paths acquire agreement before subscription and reject unresolved monetary commands before mutation. This prevents both the original stale overwrite and cancellation during an in-flight/UNKNOWN payment stranding paid finalization. Exact paid finalize-only recovery remains reachable. DONE still permits ordinary cancellation with the paid tier/period retained, and definitive FAILED permits cancellation without a refund.

The new `SubscriptionMutationFenceIntegrationTest` was read in full: it authors an unsafe legacy-SQL control, independent transaction/commit-order checks, both lock orders, held fake Provider and UNKNOWN guards, exact recovery and terminal behavior. The zero-amount branch was reviewed statically rather than directly exercised by that new integration enum.

At 04:37:47 +09:00, no new mutation-suite XML result was available. MA's partial full-build log already contained failures from another domain, without a terminal outcome in the inspected tail; those failures were not investigated here. The earlier 72 passes do not cover this follow-up correction. No H2/JUnit/Gradle/npm runner or product write was performed by WI011, and MySQL/real Provider/deployment remain unverified.

MA should collect the serialized regression/build results and propagate this superseding code-review disposition to WI013/WI014. F1's original finding remains in the evidence pack; unconditional release/test acceptance is not claimed.

## Executed Evidence Update: 2026-09-09

**F1 closed for the approved bounded correction.** WI011 independently inspected MA's `backend-full-second-results.json`, terminal log and per-class XML, including all ten new mutation-test cases. The unsafe legacy overwrite control and corrected actual service interleavings executed successfully; ordinary cancellation after DONE preserves paid access and exact recovery does not charge twice.

| Scope | Executed MA result, inspected by WI011 |
| --- | --- |
| New SubscriptionMutationFenceIntegrationTest | 10/10 passed, no skips/errors/failures |
| Selected related payment/F1 classes | 132/132 passed across 14 classes |
| Full backend second run | 1,825 tests: 1,797 passed, 9 failed, 19 skipped; BUILD FAILED |
| Outstanding failure | Unrelated AppConfig multipart fixture; full build and coverage gate still need acceptance |

No new payment blockers were found. This closes the original F1 within the local source/H2/fake-provider evidence boundary, not MySQL runtime, real Provider or deployment acceptance. MA should carry this payment closure to WI013/WI014 while retaining the overall build failure and remaining quality gates. WI011 executed no heavy runner and changed only its reports.
