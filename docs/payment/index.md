---
version: 1.10
last_updated: 2026-09-08
project: ATS
owner: docops
category: guide
status: stable
dependencies:
  - path: ../design/payment-integration-design.md
    reason: Primary payment integration design source
  - path: ../design/payment-refund-receipt-settlement-policy.md
    reason: Refund, receipt, settlement, and tax invoice policy source
  - path: ../design/payment-operations-runbook.md
    reason: Payment operations and incident response source
  - path: ../SR/SR-93.md
    reason: Production payment checklist and SR handoff source
  - path: ../audit/p1-payment-integrity-closure-20260715.md
    reason: Current payment-integrity code/test closure and remaining gates
---

# Payment Documentation Pack

> Purpose: Provide a readable entry point for the ATStudio payment system, connecting implemented behavior, operations, client-facing explanation, acceptance testing, and future extension points.

---

## 1. Scope

This directory explains the ATStudio payment system as of 2026-09-08.

The current payment system is recurring-subscription first:

- Users subscribe through Toss billing-key based automatic payment.
- The first subscription charge happens immediately after billing-key registration.
- Upgrades are charged immediately for the remaining-period difference.
- Downgrades and billing-cycle-only changes are scheduled for the next renewal.
- Account withdrawal cancels local renewal eligibility before soft deletion, then attempts Provider billing-key cleanup after commit.
- Withdrawal cleanup failure is visible as a deduplicated Incident and retried daily; withdrawal never creates an automatic refund.
- Admins can review payment ledgers, reconciliation incidents, receipt evidence, refund workflow, entitlement correction workflow, and settlement reconciliation from `/admin/payments`.
- Existing subscribers re-register a payment method through a zero-amount `BILLING_AGREEMENT` order; registration itself does not charge or change the current plan.
- V1 starts from the fresh-only `schema.sql` plus the six-plan `seed.sql`, then runs with `ddl-auto=validate`; see [System Overview](system-overview.md) and [DB Schema](../design/db-schema.md).

This directory is a guide layer. Detailed source-of-truth design documents remain in `docs/design/`.

## 2. Current Closure Decision

The three 2026-07-13 P0 behaviors are implemented and focused-test verified in implementation commit `d11c62d`: protected Track media, secret-free mail delivery logs, and post-withdrawal renewal stop. This statement is limited to the P0 remediation slice.

The later payment-integrity findings F-01 through F-05 were closed at their dated repository code/test boundary. Packages A-G, the WI-008/WI-011 corrections, WI-012 independent PASS, and the disposable MySQL 7/7 proof are mapped in [P1 Payment Integrity Closure](../audit/p1-payment-integrity-closure-20260715.md).

The approved 2026-09-08 source work and WI009 development-runtime adoption are
complete. WI010's bounded checks and approved source/test/documentation scope
were committed and pushed as `7eae086`; the limited work is complete. Actual
user/Toss TEST/Gmail acceptance and the subsequent isolated source verification
are recorded separately in the [dated acceptance record](acceptance-test-checklist.md#2026-09-08-acceptance-record).
Production readiness remains OPEN under the [remaining production gates](../SR/SR-93.md#remaining-production-gates).

### 2026-09-08 Source And Runtime

The current source baseline is `main`; use the central
[current V1 baseline](../index.md#current-v1-baseline) for Git designation.
This section separates dated runtime observations from later mail closeout.
Git promotion leaves the public runtime unchanged and is not production or
security approval. The table below preserves MA's WI009/WI010 checkpoint,
including the 2026-09-08 20:11+ KST observation in
[WI009](../../deliverables/agent/WI-20260908-ATS-009-evidence-pack.md#latest-2026-09-08-2011-kst--restart-and-http-adoption-complete).
WI012's later backend/mail evidence follows the table. DocOps has not
independently rechecked processes, HTTP, browser state or recipient images.

| Boundary | Dated WI009/WI010 evidence |
| :-- | :-- |
| Source and Git | MA committed/pushed exactly 70 approved source/test/documentation paths on the then-current `codex/v1-release-rehearsal-fixes` as `7eae086c899cd4534be69da03bbc1cc55fe4349d` and confirmed the exact live remote SHA. The 27 product hashes remained unchanged. The later documentation closeout is recorded separately below. The client worktree was excluded at this checkpoint; earlier cached-ref comparisons below remain historical. |
| Running backend artifact | WI009 launched the unchanged WI008-tested `ATStudio-20260908-copy-polish.jar` from `C:/Users/jm991/AppData/Local/ATStudio/remote-development-20260908`; SHA-256 `5AE38AC932388E24223A723DDF3FC9BD2DB2B017B3ABC6F91496BCE1BC7F37C2`. Restart and HTTP adoption are complete; historical charge timestamps were not repaired. |
| Development runtime | Backend PID 20860 started at 20:08:51; startup confirmed at 20:09:06. Frontend PID 20468 started at 20:11:07. Preserved Cloudflare PID 12512 serves the [development runtime](https://debian-reliable-round-responses.trycloudflare.com). These are dated identities, not permanent process ownership. |
| Preserved environment | Same local profile/MySQL, `ddl-auto=validate`, bootstrap off, public/private storage roots, non-strict audit and Gmail/Toss TEST settings. Callback/mail/CORS origins were updated only in the processes. No DB/schema/source changes or real mail/provider calls occurred during restart. |
| HTTP and browser boundary | WI009's five local/public GETs and exact-public-origin backend OPTIONS passed 200; changed Vite-source delivery and basic public HomePage rendering passed. WI010 MA recheck confirmed unchanged PID/artifact ownership and three HTTP 200 checks. Real authenticated admin payment tabs/receipts/correction guards and the second `/admin/user-subscriptions` correction modal passed the observed desktop checks. Screenshots had no overlapping labels. No stubs, receipt-link navigation or mutations. Exact checks and user-flow limits are in WI010. |
| Checkpoint limits | Fresh SMTP delivery and inbox placement were untested at WI009/WI010; the later scoped mail evidence follows below. Startup audit checked 30 / missing 10; visible cover fallbacks were not diagnosed or causally tied to those references. MA reports fresh WI010 focused Vitest 5 files / 354 passed in 11.42s, typecheck, lint and full Prettier PASS; backend/full frontend suites were not rerun. External/production gates and SR-93 remain OPEN. |
| Operating pointers | Use WI009's artifact/log paths and verify actual PID ownership before operations. The old `runtime-manifest.json` is stale and intentionally unchanged. [WI010 evidence](../../deliverables/agent/WI-20260908-ATS-010-evidence-pack.md) records completed bounded checks, documentation review and verified Git results separately from OPEN target-production gates. |

#### WI011/WI012 Mail And Documentation Closeout (2026-09-08)

| Boundary | Later supplied evidence |
| :-- | :-- |
| WI011 Korean mail | [WI011 evidence](../../deliverables/agent/WI-20260908-ATS-011-evidence-pack.md#current-recipient-evidence-wi012-follow-up) records one standalone batch, SMTP accepted 3 / failed 0 / unknown 0, followed by user confirmation that all three Korean test messages were received, readable and in Inbox. Two subject groups contain three messages. This does not prove scheduler execution or future deliverability. |
| WI012 website mail and runtime | [WI012 evidence](../../deliverables/agent/WI-20260908-ATS-012-evidence-pack.md#ma-execution-record-2026-09-08-kst) records backend Gmail restoration using the unchanged JAR, backend PID24792 starting at 21:55:09 KST, and unchanged frontend16160/Tunnel1888 with 21:00 start times. One actual website forgot-password request produced backend mail SUCCESS; the user confirmed receipt and opening the reset page. No password-change submission or separate Inbox/Spam verification occurred for WI012. These dated identities supersede the earlier runtime checkpoint; verify live ownership before any operation. |
| Documentation and current source | The mail closeout was committed/pushed as `53284823b2824d04ee364f4c3f0ec9e8adeb4635` across seven WI011/WI012/REQ002 documentation paths before main unification, as recorded in [REQ003](../../deliverables/user/REQ-20260908-ATS-003.md). The product delivery remains `7eae086`; the current source branch is `main`. This is distinct from a new product release, runtime deployment or security approval. |

#### Historical WI005 Snapshot (Before REQ002)

The table below preserves the earlier checkout/runtime comparison. It is not
the current runtime state; WI009 above supersedes its pre-restart boundary.

| Boundary | Verified state |
| :-- | :-- |
| Main source at WI005 | `C:/Users/jm991/Desktop/project/ATStudio`, branch `codex/v1-release-rehearsal-fixes`, HEAD `2f2e9eccadd9ae9626fe8273bc635068d42b09b0` plus uncommitted fixes. No product/test changes between WI004 review and that WI005 snapshot. |
| Client worktree | `v1-client-acceptance-20260817` still exists at ref `c5f83fc`; only its existing `HomePage.tsx` and `HomePage.test.tsx` changes were observed. This round left it untouched. |
| Cached refs | No local `master`. Cached `origin/master` is `5a67f3a`; `origin/master...HEAD` has 3 / 176 unique commits. No fetch or current-remote claim; deployment branch is not designated. |
| Built versus running backend at WI005 | Full build used repo-external `closeout-build.gradle` and `build-closeout/`. Public backend then used the unchanged original `build/libs` JAR; the new backend had not yet been restarted and old charge timestamps were not repaired. |
| TEST access at WI005 | MA then observed local/public frontend and API HTTP 200. Direct-backend CORS preflight allowed the trusted public Origin (200, exact allow-origin/headers) and rejected an untrusted Origin (403). The browser used the same-origin `/api` proxy; public proxy preflight was not cross-origin or production-topology proof. PIDs 19932/19376/2372 were unchanged at that snapshot and are now historical. |
| Evidence | [WI005 Evidence Pack](../../deliverables/agent/WI-20260908-ATS-005-evidence-pack.md) holds JAR identity, artifact paths, aggregate results, browser-fixture boundaries and the actual acceptance timeline. |

The later [REQ002 copy follow-up](acceptance-test-checklist.md#req002-copy-follow-up)
implements Korean payment/reconciliation mail, `구독 이용권 조정` in both admin
entry points, `결제 점검 이슈`, and receipt evidence clarity. WI006/007 source
work and WI008 validation are complete, with final MA aggregate and
synthetic-browser results recorded there. WI009 subsequently completed
development-runtime adoption as summarized above. WI010's bounded desktop,
focused-test, documentation and scoped Git work is complete at `7eae086`.
The later WI011/WI012 mail results above close only their tested scope; no
future deliverability guarantee or production GO is claimed.

Closed scope:

- Card-based Toss recurring subscription checkout.
- User subscription lifecycle and plan-change policy.
- Renewal, failure, retry, and expiration handling.
- Stable payment command identity, strict Provider transaction boundaries, refund lease fencing, and finalize-only reconciliation.
- Local-first account-withdrawal cancellation, after-commit Provider cleanup, durable Incident/retry handling, already-removed convergence, and no-auto-refund separation.
- Admin payment operations for ledgers, incidents, receipts, audit logs, refunds, entitlement correction, and CSV/manual settlement review.

Not blockers for closure:

- Toss webhook hardening.
- Toss Settlement API adapter.
- Multi-PG expansion.
- A future provider adapter, if selected by an approved product requirement.
- Additional operator notification channels.

Removed payment aliases and direct-subscription creation are absent, not V1
compatibility paths. The earlier `codex/p1-acceptance-hardening` baseline and its
dated Vite 6.4.3 observation are historical, not the current checkout designation.

On hold under the current card-only recurring subscription premise:

- Tax invoice request/admin workflow.
- Cash receipt issue/cancel mutation.
- B2B invoice, bank-transfer, postpaid, or contract purchase payment flows.

## 3. Reading Order

| Reader Goal | Start Here | Then Read |
| :-- | :-- | :-- |
| Understand what was added | [Feature Inventory](feature-inventory.md) | [System Overview](system-overview.md) |
| Understand user behavior | [User Flows](user-flows.md) | [Acceptance Test Checklist](acceptance-test-checklist.md) |
| Operate admin payment workflows | [Admin Operations Guide](admin-operations-guide.md) | [Payment Operations Runbook](../design/payment-operations-runbook.md) |
| Explain the feature to a client | [Client Brief](client-brief.md) | [Acceptance Test Checklist](acceptance-test-checklist.md) |
| Plan the next payment work | [Known Limits and Next Steps](known-limits-and-next-steps.md) | [SR-93](../SR/SR-93.md) |
| Verify the payment-integrity decision | [P1 Payment Integrity Closure](../audit/p1-payment-integrity-closure-20260715.md) | [P1 Trace Matrix](../audit/p1-remediation-trace-matrix-20260714.md) |

## 4. Document List

| Document | Description | Status |
| :-- | :-- | :-- |
| [Feature Inventory](feature-inventory.md) | Complete current feature list grouped by user, backend, admin, operations, and future extension area. | stable |
| [System Overview](system-overview.md) | Technical architecture, core tables, APIs, provider boundaries, schedulers, and security rules. | stable |
| [User Flows](user-flows.md) | User-facing subscription, billing method, plan change, cancellation, reactivation, and failure flows. | stable |
| [Admin Operations Guide](admin-operations-guide.md) | Admin `/admin/payments` tab guide and operational usage boundaries. | stable |
| [Acceptance Test Checklist](acceptance-test-checklist.md) | Current acceptance checklist for local and client-adjacent payment testing. | stable |
| [Client Brief](client-brief.md) | Client-facing draft explanation of the payment system without internal implementation noise. | draft |
| [Known Limits and Next Steps](known-limits-and-next-steps.md) | Planned, deferred, and out-of-scope payment capabilities. | stable |

## 5. Maintenance Rule

When a new payment feature is added, update this directory in the same commit or the immediately following documentation commit.

At minimum:

- Add the feature to [Feature Inventory](feature-inventory.md).
- Add or update affected flows in [User Flows](user-flows.md) or [Admin Operations Guide](admin-operations-guide.md).
- Add acceptance checks to [Acceptance Test Checklist](acceptance-test-checklist.md).
- Move the item from [Known Limits and Next Steps](known-limits-and-next-steps.md) to implemented status when complete.

## Related Documents

### Required References

- [Payment Integration Design](../design/payment-integration-design.md): Detailed subscription payment design and implementation decisions.
- [Payment Refund, Receipt, Settlement, and Tax Invoice Policy](../design/payment-refund-receipt-settlement-policy.md): Detailed operating policy for refund, receipt, settlement, and tax invoice scope.
- [Payment Operations Runbook](../design/payment-operations-runbook.md): Production-facing incident and operations procedures.
- [SR-93](../SR/SR-93.md): Production payment checklist and SR tracking source.
- [P1 Payment Integrity Closure](../audit/p1-payment-integrity-closure-20260715.md): Current code/test closure, exact evidence, and open production gates.

### Reference Documents

- [API Spec](../design/api-spec.md): REST API source of truth.
- [DB Schema](../design/db-schema.md): Payment table source of truth.
- [Original Final Acceptance Checklist](../../deliverables/user/PAYMENT-FINAL-ACCEPTANCE-CHECKLIST-20260525.md): Historical checklist that this pack normalizes into current guide form.
