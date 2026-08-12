---
version: 1.0
last_updated: 2026-08-10
project: ATS
owner: docops
category: evidence-pack
status: stable
related_wi: WI-20260809-ATS-032
dependencies:
  - path: WI-20260809-ATS-032-handoff.md
    reason: Approved bounded implementation and documentation contract
  - path: WI-20260809-ATS-032-pg-review.md
    reason: Fail-closed security review conditions
  - path: WI-20260809-ATS-032-qa-integ-review.md
    reason: Cross-layer request, response, and evidence matrix
  - path: WI-20260809-ATS-032-re-review.md
    reason: Final bounded source re-review and finding resolutions
---

# Evidence Pack: WI-20260809-ATS-032

## Change Summary

- Closed `CR-031-081` by binding requested payment purpose to the server's
  authoritative subscription-state decision before agreement, order, or
  Provider mutation.
- Closed `CR-031-082` by carrying and resolving exact plan ID, authenticated
  audience, and billing cycle instead of selecting by plan name.
- Added a complete frontend prepare-response gate before an order becomes
  actionable or the Toss SDK can be invoked.

## Scope / DoD Check

- [x] Plan and manage entry points carry exact plan identity and audience.
- [x] Prepare accepts `{subscriptionId, billingCycle, purpose}`; purpose accepts
      only `SUBSCRIBE` or `BILLING_AGREEMENT` and is not authoritative.
- [x] Server purpose/state mismatch fails before billing-agreement mutation,
      payment-order persistence, or recurring Provider prepare.
- [x] Frontend validates the complete prepare response before preserving an
      actionable order or invoking the Toss SDK.
- [x] New subscription and exact-current-plan payment-method re-registration
      behavior are covered, including zero amount for re-registration.
- [x] Same-name cross-audience selection and both purpose mismatch directions
      are covered by focused tests.
- [x] Required product, static, build, coverage, and regression evidence passed
      after the isolated unrelated frontend timing rerun.
- [x] Payment design, screen flow, API specification, Evidence Pack, and Korean
      user summary are updated.
- [x] Focused documentation closeout checks are recorded below.

## Reference Documents

<!-- prettier-ignore -->
| Tier | Document | Use |
|---|---|---|
| 0 | `docs/standards/core-principles.md` | Constitution, language, traceability, and payment integrity |
| 0 | `docs/standards/documentation-standards.md` | Metadata, version, structure, and links |
| 0 | `docs/standards/development-standards.md` | Evidence pointers, tests, and rollback expectations |
| 0 | `docs/standards/glossary.md` | Canonical ATStudio and WI terms |
| 2 | `docs/design/payment-integration-design.md` | Recurring payment boundary |
| 2 | `docs/design/api-spec.md` | Prepare API contract |
| 2 | `docs/ui/screen-flow.md` | Checkout and re-registration flow |
| WI | `deliverables/agent/WI-20260809-ATS-032-handoff.md` | Approved scope, exclusions, and chain rule |
| Review | `deliverables/agent/WI-20260809-ATS-032-pg-review.md` | Fail-closed conditions |
| Review | `deliverables/agent/WI-20260809-ATS-032-qa-integ-review.md` | Cross-layer matrix |
| Review | `deliverables/agent/WI-20260809-ATS-032-re-review.md` | Resolution acceptance |

## Reviewer Decisions

<!-- prettier-ignore -->
| Reviewer | Decision | Completion interpretation |
|---|---|---|
| PG | `APPROVE WITH CONDITIONS`; no blocker | Explicit consistency intent, exact plan/audience binding, pre-side-effect rejection, complete response gate, and negative tests are represented in the bounded diff and evidence. |
| QA-INTEG | `APPROVE WITH CONDITIONS`; no schema, architecture, or policy blocker | Route, request, server decision, response gate, callback, Provider, and durable-evidence lanes match the approved matrix. |
| RE | `ACCEPTED` | Positive-ID validation, response mismatch coverage, and ordering-test fragility were resolved; no remaining finding in those three areas. RE inspected source and tests but did not execute tests. |

## Traceability And Patch Inventory

<!-- prettier-ignore -->
| Changed product/test file | Root | Evidence pointer and bounded effect |
|---|---|---|
| `frontend/src/api/payments.ts` | `CR-031-081` | `BillingAgreementPrepareRequest` and response types require bounded purpose, `KRW`, and `CARD` (`:20-47`). |
| `frontend/src/api/subscriptions.ts` | `CR-031-082` | Plan audience is typed as authenticated `UserType` (`:1-9`). |
| `frontend/src/pages/public/SubscriptionPlanPage.tsx` | `CR-031-082` | `handleSubscribe` carries exact ID/audience/cycle and current-plan comparison uses ID (`:167-190,318-320`). |
| `frontend/src/pages/subscriber/SubscriptionManagePage.tsx` | `CR-031-081`, `CR-031-082` | Re-registration and upgrade-return context carry exact current/return IDs, audience, and cycle with `BILLING_AGREEMENT` (`:178-183,349-365,794-798`). |
| `frontend/src/pages/subscriber/SubscriptionPaymentPage.tsx` | `CR-031-081`, `CR-031-082` | Route identity checks, exact-ID resolution, explicit prepare purpose, response gate, validated callback values, and absolute HTTP(S) validation (`:38-64,140-209,250-278,527-565`). |
| `src/main/java/com/atstudio/atstudio/common/exception/BUSINESS_ERROR.java` | `CR-031-081` | Stable `PAYMENT_PURPOSE_MISMATCH` error (`:133-136`). |
| `src/main/java/com/atstudio/atstudio/dto/payment/BillingAgreementPrepareRequest.java` | `CR-031-081`, `CR-031-082` | Positive exact ID and supported-purpose validation (`:9-20`). |
| `src/main/java/com/atstudio/atstudio/service/BillingAgreementApplicationService.java` | `CR-031-081`, `CR-031-082` | Exact plan audience validation and authoritative purpose comparison precede agreement/order/Provider effects (`:108-140,389-393`). |
| `frontend/src/api/domainApis.test.ts` | `CR-031-081` | Serializes the exact prepare body with purpose (`:220-251`). |
| `frontend/src/pages/public/SubscriptionPlanPage.test.tsx` | `CR-031-082` | Asserts exact-ID/audience/cycle subscription routing (`:87-98`). |
| `frontend/src/pages/subscriber/SubscriptionManagePage.test.tsx` | `CR-031-081`, `CR-031-082` | Asserts exact current re-registration route and immutable upgrade-return identity (`:377-421,466-553`). |
| `frontend/src/pages/subscriber/SubscriptionPaymentPage.test.tsx` | `CR-031-081`, `CR-031-082` | Covers request intent, invalid route/audience, duplicate-name exact ID, complete response mismatch matrix, zero re-registration amount, and SDK non-invocation (`:124-509`). |
| `frontend/src/pages/subscriber/SubscriptionPaymentReplay.test.tsx` | `CR-031-081`, `CR-031-082` | Keeps callback-history replay protection compatible with authenticated audience state (`:35-57`). |
| `src/test/java/com/atstudio/atstudio/controller/PaymentControllerTest.java` | `CR-031-081`, `CR-031-082` | Rejects non-positive ID and missing/malformed/`UPGRADE` purpose before service invocation (`:91-143`). |
| `src/test/java/com/atstudio/atstudio/service/BillingAgreementApplicationServiceTest.java` | `CR-031-081`, `CR-031-082` | Covers valid price/zero paths, both purpose mismatch directions, current plan/cycle mismatch, both same-name audience directions, grace state, BUSINESS path, no-side-effect rejection, and valid ordering (`:113-480,836-839`). |

Documentation closeout files:

- `docs/design/payment-integration-design.md`
- `docs/ui/screen-flow.md`
- `docs/design/api-spec.md`
- `deliverables/agent/WI-20260809-ATS-032-evidence-pack.md`
- `deliverables/user/WI-20260809-ATS-032-summary.md`

## Cross-Layer Evidence

<!-- prettier-ignore -->
| Lane | Confirmed bounded evidence | Evidence limit |
|---|---|---|
| UI/control | Exact identity is required; copy and amount use a validated response; mismatch leaves billing auth disabled. | Automated React evidence only, not browser acceptance. |
| Frontend request | Prepare body is exactly `{subscriptionId, billingCycle, purpose}` with no client amount or audience authority. | Route `userType` remains an untrusted consistency claim cross-checked locally and by server plan validation. |
| Server decision | Authenticated audience, exact plan, service-enabled state, derived purpose, current plan/cycle, and server amount are checked before effects. | Source and mock interaction evidence, not a deployed runtime observation. |
| Provider | Rejected service paths verify no recurring Provider prepare; valid paths use one test double. Frontend mismatch verifies no SDK load or `requestBillingAuth`. | No real Toss Provider or SDK operation was run. A frontend response mismatch occurs after prepare, so SDK non-invocation does not prove absence of a server order or Provider prepare. |
| Durable state | Rejected paths verify no agreement/order repository interaction; valid paths capture isolated test orders with server purpose and amount. | No real database or production durable state was inspected. |

## Commands And Results

The results below are the supplied implementation/main verification evidence.
DocOps did not rerun product tests.

<!-- prettier-ignore -->
| Command or verification lane | Exact recorded result |
|---|---|
| `npm test -- src/api/domainApis.test.ts src/pages/public/SubscriptionPlanPage.test.tsx src/pages/subscriber/SubscriptionManagePage.test.tsx src/pages/subscriber/SubscriptionPaymentPage.test.tsx src/pages/subscriber/SubscriptionPaymentReplay.test.tsx` | Focused frontend: 5 files, 77 tests PASS. |
| `.\gradlew.bat test --tests "com.atstudio.atstudio.controller.PaymentControllerTest" --tests "com.atstudio.atstudio.service.BillingAgreementApplicationServiceTest" --console=plain` | Focused backend `PaymentControllerTest` + `BillingAgreementApplicationServiceTest`: `BUILD SUCCESSFUL`. |
| `.\gradlew.bat test --console=plain` | Full backend test: exit 0; 1,400 tests per implementation evidence, no failures/errors, 13 skipped; main rerun exit 0. |
| `npm test` (first full run) | 629/630 with one `TrackEditPage` thumbnail timing failure unrelated to WI-032. |
| `npm test -- src/pages/creator/TrackEditPage.test.tsx` | Isolated `TrackEditPage` rerun: 3/3 PASS. |
| `npm test` (full rerun) | 71 files, 630/630 PASS. |
| `npm run typecheck`; `npm run lint`; `npm run format`; `npm run build` | Frontend typecheck, ESLint, Prettier, and build PASS. |
| `.\gradlew.bat build`; `.\gradlew.bat jacocoTestReport jacocoTestCoverageVerification` | Backend build and JaCoCo verification PASS. |
| `git diff --check` | No whitespace errors; only CRLF conversion warnings. |

### DocOps Documentation Closeout

- Focused Prettier check over the five write-scope files: PASS; all matched
  files use Prettier code style.
- Project documentation validator: the initial run exited 1 after exposing 22
  line-suffixed absolute-link formatting errors in
  `WI-20260809-ATS-032-re-review.md`. The link targets were repaired to use
  repository-relative paths without line suffixes, and the final run exited 0:
  Tier 0 documents, internal links, 545 supported traceability IDs, and document
  index coverage all PASS.
- Focused relative-link validation over the five write-scope files: PASS; 0
  broken links.
- Metadata/H1 check: PASS; existing documents are `3.2`, `5.2`, and `28.5`,
  both new deliverables are `1.0`, all dates are `2026-08-10`, and the API H1
  is `v28.5`.
- Scoped `git diff --check` over the five write-scope files: PASS with no
  output.

## Residual Limits

- `WI-20260809-ATS-033` still owns prepare idempotency and duplicate-order
  control.
- `WI-20260809-ATS-034` still owns callback response-loss, unknown-outcome,
  reload, and recovery behavior.
- No real Provider, Toss SDK authorization, database, runtime, deployment,
  charge, refund, or production-state evidence was produced.
- This WI changes no payment policy, pricing policy, architecture, schema, or
  retained data.

## Risks / Rollback

- Risk: query identity remains client-controlled input; safety depends on the
  implemented frontend consistency gate plus independent authenticated server
  plan/state validation.
- Risk: a malformed prepare response is rejected after the prepare call, so the
  frontend can prove only SDK non-invocation for that lane.
- Rollback is bounded to the eight product files, seven test files, and five
  documentation closeout files listed in this Evidence Pack. Revert only the
  WI-032 hunks/files; preserve unrelated shared-worktree changes and the
  handoff/review records as audit history.
- No schema or retained data changed, so no data rollback, migration rollback,
  Provider reversal, charge reversal, or refund is required.

## Follow-Up Chain

WI-032 completion immediately triggers `WI-20260809-ATS-033` for prepare
idempotency and duplicate-order control. `WI-20260809-ATS-034` remains the next
separate outcome-recovery boundary and is not absorbed into WI-033.

## Related Documents

- [Payment Integration Design](../../docs/design/payment-integration-design.md)
- [API Specification](../../docs/design/api-spec.md)
- [Screen Flows](../../docs/ui/screen-flow.md)
- [WI-032 User Summary](../user/WI-20260809-ATS-032-summary.md)
