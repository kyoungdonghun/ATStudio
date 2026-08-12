# WI-20260809-ATS-027 Findings

## Scope and Evidence Boundary

- WI: `WI-20260809-ATS-027`
- Matrix rows: `G-PAY`, `PUB-07`, `MEM-10`, `MEM-10S`, `MEM-10F`, `MEM-11`, and `INV-SUB`
- Method: bounded review of frontend source/test evidence, confirmed backend A/B/C source paths, targeted frontend verification, and anonymous/public runtime observations.
- Frontend verification by main: targeted Vitest passed (`9` files, `65` tests, `3.65s`); `npm run typecheck` passed; targeted ESLint passed.
- Anonymous route guards were observed for checkout, success, fail, and manage routes, including their exact encoded `returnTo` values.
- Public `/subscriptions` was observed at `1280x720` through GET, DOM, and screenshot evidence: `output/ui-ux-audit/20260809/WI-027/PUB-07-business-yearly-1280x720-observed.png`.
- Live public API ordering was observed: the unfiltered response returned IDs `1-3` as INDIVIDUAL followed by IDs `4-6` as BUSINESS; the BUSINESS-filtered response returned IDs `4-6`.
- Backend verification by main passed in two targeted runs: run A used `--rerun-tasks` and reported `15` XML suites / `107` tests, `0` failures, `0` errors, `0` skipped, wall time `54.9s`; run B reported `7` suites / `39` tests, `0` failures, `0` errors, `0` skipped, wall time `42.3s`. Combined backend test executions: `146 PASS`.
- Product source, test source, configuration, schema, database, provider state, and the preserved ZIP were not modified.
- Authenticated runtime, live Provider result, and live durable database state are `BLOCKED`; source and test-managed persistence evidence is not reported as observed production state.

## Finding Index

| ID            | Severity         | Matrix row                              | Finding                                                                                                               |
| ------------- | ---------------- | --------------------------------------- | --------------------------------------------------------------------------------------------------------------------- |
| `ATS-027-F01` | **P0 candidate** | `G-PAY`, `MEM-10`, `MEM-10S`, `INV-SUB` | UI-only purpose can advertise zero payment while the server creates a full-price `SUBSCRIBE` order.                   |
| `ATS-027-F02` | **P1**           | `PUB-07`, `MEM-10`, `INV-SUB`           | Name-only cross-audience routing can block BUSINESS checkout; the backend rejects it before side effects.             |
| `ATS-027-F03` | **P1**           | `MEM-10`, `INV-SUB`                     | StrictMode duplicate prepare requests reuse the agreement but create distinct payment orders.                         |
| `ATS-027-F04` | **P1**           | `MEM-10S`, `MEM-11`, `INV-SUB`          | Mutation response loss and reload failure leave financial outcomes ambiguous in the UI.                               |
| `ATS-027-F05` | **P2**           | `PUB-07`                                | Plan loading conflates subscription errors, has no retry/empty state, and has no latest-response fence.               |
| `ATS-027-F06` | **P2**           | `MEM-11`, `INV-SUB`                     | Manage treats every Billing Agreement read error as absence and silently discards preview failures.                   |
| `ATS-027-F07` | **P2**           | `G-PAY`, `MEM-10`                       | Missing or malformed checkout query values are normalized into prepare attempts instead of failing before invocation. |
| `ATS-027-F08` | **P2**           | `MEM-10`, `MEM-10F`                     | Checkout terminal/error states and initial-charge copy do not describe the actual operation consistently.             |
| `ATS-027-F09` | **P2**           | `MEM-11`, `INV-SUB`                     | Reactivation is a one-click financial lifecycle mutation without confirmation.                                        |
| `ATS-027-F10` | **P3**           | `PUB-07`, `MEM-10`, `MEM-11`            | Selection, status, and copy semantics are inconsistent for assistive technology and localization.                     |
| `ATS-027-F11` | **P2**           | Documentation / operator baseline       | Payment documentation has a stale schema table count and an ambiguous official-branch declaration.                    |

## Detailed Findings

### ATS-027-F01 - UI-only purpose can advertise zero payment while the server creates a full-price SUBSCRIBE order

- **Severity:** P0 candidate
- **Rows/routes:** `G-PAY`, `MEM-10`, `MEM-10S`, `INV-SUB`; `/subscriptions/checkout`, `/subscriptions/checkout/success`
- **Contract:** `deliverables/agent/WI-20260809-ATS-020-acceptance-matrix.md:167-170,261,272-277`
- **Exact source/test pointers:**
  - `frontend/src/pages/subscriber/SubscriptionPaymentPage.tsx:21-30,121-138,240-275,299-303,345-353,362-374`
  - `frontend/src/api/payments.ts:30-33,75-82`
  - `src/main/java/com/atstudio/atstudio/dto/payment/BillingAgreementPrepareRequest.java:6-9`
  - `src/main/java/com/atstudio/atstudio/service/BillingAgreementApplicationService.java:108-157,437-440`
  - `frontend/src/pages/subscriber/SubscriptionPaymentPage.test.tsx:102-169,238-268`
  - `frontend/src/api/domainApis.test.ts:220-256`
  - `src/test/java/com/atstudio/atstudio/service/BillingAgreementApplicationServiceTest.java:109-199`
- **Evidence lanes:**
  - **UI copy/control - CONFIRMED FROM SOURCE:** Query `purpose=BILLING_AGREEMENT` selects the re-registration UI and displays `즉시 결제 없음`, regardless of the authoritative `paymentOrder.purpose` and `paymentOrder.amount` returned by prepare.
  - **Frontend invocation - CONFIRMED FROM SOURCE:** The prepare body contains only `subscriptionId` and `billingCycle`; query `purpose` is not transmitted. The frontend later appends its own purpose to callback URLs.
  - **Server/Provider - SERVER SOURCE CONFIRMED; PROVIDER BLOCKED:** `prepareBillingAgreement()` derives purpose solely from whether an active subscription exists. A non-subscriber receives `SUBSCRIBE` and the selected monthly/yearly full price. Provider preparation/charge was not executed or observed.
  - **Durable state - BLOCKED / NOT INSPECTED:** Source constructs and saves that full-price `SUBSCRIBE` order at `BillingAgreementApplicationService.java:128-140`; no database row was inspected.
- **Observation:** A non-subscriber can open checkout with `purpose=BILLING_AGREEMENT`, see a zero-payment re-registration message, and receive a server-created full-price `SUBSCRIBE` order. The opposite mismatch also exists: an active subscriber with missing or malformed purpose is rendered as a normal subscription checkout, while the server infers a zero-amount `BILLING_AGREEMENT` order; the frontend then rejects the zero callback amount under its local `SUBSCRIBE` parser before confirmation.
- **Impact:** The UI can materially misstate whether the flow is charge-bearing. A full-price order can be prepared behind zero-payment copy; an active subscriber can complete provider-side billing auth but fail the frontend callback gate. No actual Provider charge is claimed because Provider/runtime evidence is blocked.
- **Bounded follow-up:** Make operation purpose authoritative and state-bound at one contract boundary. Validate it server-side, render the returned server purpose and amount, and stop before billing auth whenever URL intent, user subscription state, server purpose, or amount disagree. Add cross-layer tests for non-subscriber/re-registration and active-subscriber/default-purpose cases.

### ATS-027-F02 - Name-only cross-audience routing can block BUSINESS checkout

- **Severity:** P1
- **Rows/routes:** `PUB-07`, `MEM-10`, `INV-SUB`; `/subscriptions`, `/subscriptions/checkout`
- **Contract:** `deliverables/agent/WI-20260809-ATS-020-acceptance-matrix.md:139,167,261`
- **Exact source/test pointers:**
  - `frontend/src/pages/public/SubscriptionPlanPage.tsx:133-163,167-183,303-375`
  - `frontend/src/api/subscriptions.ts:20-25`
  - `frontend/src/pages/subscriber/SubscriptionPaymentPage.tsx:110-138`
  - `src/main/java/com/atstudio/atstudio/service/SubscriptionService.java:23-38`
  - `src/main/java/com/atstudio/atstudio/repository/SubscriptionRepository.java:14-17`
  - `src/main/java/com/atstudio/atstudio/service/BillingAgreementApplicationService.java:111-124,431-434`
  - `frontend/src/pages/public/SubscriptionPlanPage.test.tsx:51-95`
  - `frontend/src/pages/subscriber/SubscriptionPaymentPage.test.tsx:43-114`
- **Evidence lanes:**
  - **UI copy/control - CONFIRMED FROM SOURCE:** Plans routes carry only `plan=<name>`. The current-plan marker also compares only plan names, so a same-name plan from the other audience can be marked current and disabled.
  - **Frontend invocation - CONFIRMED FROM SOURCE:** Checkout calls unfiltered `fetchSubscriptionPlans()` and selects the first case-insensitive name match. If the first same-name result is INDIVIDUAL, a BUSINESS user sends that INDIVIDUAL plan ID.
  - **Server/Provider - SERVER SOURCE CONFIRMED; PROVIDER NOT INVOKED BY THIS PATH:** `validateSubscriptionUserType()` runs before Agreement preparation, order save, and Provider prepare, then throws `SUBSCRIPTION_USER_TYPE_MISMATCH`.
  - **Durable state - BLOCKED / NOT INSPECTED:** Source ordering shows no Agreement/order persistence on the mismatch path. Runtime and database state were not inspected.
- **Observation:** Audience is lost between Plans and Checkout. Repository methods do not specify ordering, while checkout uses first-name match. Existing frontend tests contain only INDIVIDUAL plans and do not exercise duplicate names across audiences.
- **Impact:** BUSINESS checkout can be blocked by a wrong-audience plan ID even after the user selected a valid BUSINESS card. The backend fails safely before financial side effects, so this is a business-flow blocker rather than a confirmed charge leak.
- **Bounded follow-up:** Carry an immutable plan ID plus audience to checkout, fetch plans with the authenticated audience, verify the selected plan's audience before prepare, and include same-name INDIVIDUAL/BUSINESS cases in frontend and controller/service tests.

### ATS-027-F03 - StrictMode duplicate prepare requests create distinct payment orders

- **Severity:** P1
- **Rows/routes:** `MEM-10`, `INV-SUB`; `/subscriptions/checkout`
- **Contract:** `deliverables/agent/WI-20260809-ATS-020-acceptance-matrix.md:167,261`
- **Exact source/test pointers:**
  - `frontend/src/main.tsx:1-8`
  - `frontend/src/pages/subscriber/SubscriptionPaymentPage.tsx:110-160`
  - `src/main/java/com/atstudio/atstudio/service/BillingAgreementApplicationService.java:108-157,355-375,478-484`
  - `src/main/java/com/atstudio/atstudio/entity/PaymentOrder.java:32-72`
  - `frontend/src/pages/subscriber/SubscriptionPaymentPage.test.tsx:35-40,102-114`
  - `src/test/java/com/atstudio/atstudio/service/BillingAgreementApplicationServiceTest.java:109-199,453-476`
- **Evidence lanes:**
  - **UI copy/control - CONFIRMED FROM SOURCE:** Only the response belonging to the still-active effect updates visible state, so the extra prepared order is not disclosed to the user.
  - **Frontend invocation - CONFIRMED FROM SOURCE:** The application mounts under `StrictMode`. The effect cleanup flips a local `active` flag but does not abort an already-issued prepare POST or use a once-per-intent key.
  - **Server/Provider - SERVER SOURCE CONFIRMED; PROVIDER BLOCKED:** For sequential committed requests, `prepareAgreement()` reuses the READY Agreement, but every call generates a fresh order ID, saves a new `PaymentOrder`, and invokes Provider prepare. Provider runtime was not observed.
  - **Durable state - BLOCKED / NOT INSPECTED:** Source is designed to create two distinct order rows for two valid sequential POSTs; actual row count was not queried.
- **Observation:** Prepare has no semantic command key or existing-order lookup. The inspected duplicate test covers an already ACTIVE Agreement, not two READY prepare requests, and the frontend payment test is not wrapped in `StrictMode` and does not assert one call exactly.
- **Impact:** One user intent can leave multiple live/stale prepared orders tied to one Agreement, with only one shown in the UI. This weakens callback/order attribution and increases unknown-outcome cleanup and reconciliation burden.
- **Bounded follow-up:** Add a server-side prepare idempotency key or reusable nonterminal-order lookup scoped to user, plan, cycle, purpose, and bounded expiry. Add a frontend once-per-intent/abort fence and StrictMode plus concurrent/sequential backend tests.

### ATS-027-F04 - Mutation response loss and reload failure leave financial outcomes ambiguous

- **Severity:** P1
- **Rows/routes:** `MEM-10S`, `MEM-11`, `INV-SUB`; success callback and `/subscriptions/manage`
- **Contract:** `deliverables/agent/WI-20260809-ATS-020-acceptance-matrix.md:168,170,261,274-277`
- **Exact source/test pointers:**
  - `frontend/src/pages/subscriber/SubscriptionPaymentPage.tsx:71-94`
  - `frontend/src/pages/subscriber/SubscriptionManagePage.tsx:219-248,355-445,481-486`
  - `frontend/src/api/userSubscriptions.ts:154-175`
  - `frontend/src/pages/subscriber/SubscriptionPaymentPage.test.tsx:222-254`
  - `frontend/src/pages/subscriber/SubscriptionPaymentReplay.test.tsx:23-51`
  - `frontend/src/pages/subscriber/SubscriptionManagePage.test.tsx:124-225,554-598`
  - `src/main/java/com/atstudio/atstudio/service/PaymentCommandTransactionService.java:69-163,168-228,518-719`
  - `src/main/java/com/atstudio/atstudio/service/UserSubscriptionService.java:195-217,241-258,324-398`
  - `src/test/java/com/atstudio/atstudio/service/PaymentProviderSuccessRecoveryIntegrationTest.java:50-130`
  - `src/test/java/com/atstudio/atstudio/service/PaymentCommandIndependentVerificationIntegrationTest.java:69-140`
  - `src/test/java/com/atstudio/atstudio/service/SubscriptionUpgradeCommandIntegrationTest.java:117-266`
  - `src/test/java/com/atstudio/atstudio/service/UserSubscriptionServiceTest.java:639-738`
- **Evidence lanes:**
  - **UI copy/control - CONFIRMED FROM SOURCE:** Confirm shows success and navigates without first proving canonical Subscription and Billing Agreement reload. Manage can replace a successful mutation message with a full-page load error.
  - **Frontend invocation - CONFIRMED FROM SOURCE:** Change, cancel, and reactivate are followed by `load()`. `load()` catches its own failure and sets page error, so the mutation handler cannot represent `mutation succeeded; reload failed`. A lost/5xx response is shown as ordinary failure without an unknown-outcome state.
  - **Server/Provider - SERVER SOURCE AND TARGETED BACKEND TESTS CONFIRMED; LIVE PROVIDER BLOCKED:** Billing confirm and charged upgrade use persisted command/status fences: completed commands return or finalize locally, provider-success/local-failure retries are finalize-only, and PROCESSING or ambiguous PENDING outcomes are not blindly charged again. Cancel and reactivate re-calls converge through local no-op state checks and do not contain a charge call. Main's targeted backend runs reported `146 PASS`; no live Provider operation was observed.
  - **Durable state - TEST-MANAGED PERSISTENCE CONFIRMED; LIVE DATABASE BLOCKED:** Existing integration-test paths persist `PROVIDER_SUCCEEDED`, finalize to one `DONE` order/payment/subscription, and retain one charge across retry. No live Subscription, Billing Agreement, PaymentOrder, payment, or ledger row was inspected.
- **Observation:** The frontend still has only binary success/error presentation around operations that may have committed before a response or reload failure. Backend callback and upgrade paths now have confirmed replay/idempotency evidence, while cancel/reactivate source paths are no-charge no-op re-calls; the UI does not expose that canonical distinction to the user.
- **Impact:** A user still cannot tell whether an upgrade, scheduled change, cancellation, or reactivation took effect, so the P1 unknown-outcome finding remains. Confirmed backend fences reduce duplicate-charge risk for callback and upgrade retries, and cancel/reactivate re-calls converge without charging, but the UI can still prompt unnecessary retry behavior and hide a committed success. Live Provider and production durable outcomes remain blocked.
- **Bounded follow-up:** Introduce a distinct `outcome unknown` state, retain operation context, reload canonical resources before success claims, and expose the backend's safe status/reconciliation path. Preserve the confirmed command fences and add explicit response-write-loss/post-commit 5xx coverage for the local cancel/reactivate paths.

### ATS-027-F05 - Plan loading has incomplete recovery and stale-response handling

- **Severity:** P2
- **Rows/routes:** `PUB-07`; `/subscriptions`
- **Contract:** `deliverables/agent/WI-20260809-ATS-020-acceptance-matrix.md:139,437`
- **Exact source/test pointers:**
  - `frontend/src/pages/public/SubscriptionPlanPage.tsx:127-163,190-209,291-378`
  - `frontend/src/pages/public/SubscriptionPlanPage.test.tsx:51-95`
- **Evidence lanes:**
  - **UI copy/control - CONFIRMED FROM SOURCE:** Plan failure renders only a terminal message with no retry. An empty active-plan result has no deliberate empty state. Loading copy is the English `Loading...`.
  - **Frontend invocation - CONFIRMED FROM SOURCE:** Audience changes issue independent requests without AbortSignal or request generation. Any `fetchMySubscription` error is swallowed as if no active subscription, and prior `mySub` is not cleared before a new load.
  - **Server/Provider - NOT INSPECTED:** Plan/subscription runtime responses were not observed; Provider is not applicable to this read state.
  - **Durable state - BLOCKED / NOT INSPECTED:** Subscription truth was not read from the database; stale UI state is not treated as canonical evidence.
- **Observation:** A late plan response can overwrite a newer audience selection. A transient/authorization/server failure from `fetchMySubscription` can retain stale subscription state or route the user as unsubscribed. The existing test covers one happy INDIVIDUAL subscription-start path only.
- **Impact:** Users can see the wrong plan audience/current marker, lose a recoverable path, or be routed to Checkout instead of Manage based on stale or swallowed read failure.
- **Bounded follow-up:** Add loading/empty/error/retry states, AbortSignal or latest-generation fencing, typed handling for only the documented no-active-subscription error, and explicit clearing/reconciliation of prior subscription state.

### ATS-027-F06 - Manage hides Billing Agreement and preview read failures

- **Severity:** P2
- **Rows/routes:** `MEM-11`, `INV-SUB`; `/subscriptions/manage`
- **Contract:** `deliverables/agent/WI-20260809-ATS-020-acceptance-matrix.md:170,261,456`
- **Exact source/test pointers:**
  - `frontend/src/pages/subscriber/SubscriptionManagePage.tsx:219-302,481-506,566-613,695-803`
  - `frontend/src/api/payments.ts:95-100`
  - `frontend/src/api/userSubscriptions.ts:287-296`
  - `frontend/src/pages/subscriber/SubscriptionManagePage.test.tsx:72-132,337-552`
- **Evidence lanes:**
  - **UI copy/control - CONFIRMED FROM SOURCE:** Every Billing Agreement GET failure becomes `null` and is presented as no registered payment method. Preview failure removes the preview with no error or retry.
  - **Frontend invocation - CONFIRMED FROM SOURCE:** Billing GET uses a catch-all; preview uses a catch-all that sets `preview=null`. The main `load()` has no AbortSignal/generation fence.
  - **Server/Provider - NOT INSPECTED:** 404, authorization, transport, and 5xx Billing Agreement responses were not distinguished at runtime. Provider state was not observed.
  - **Durable state - BLOCKED / NOT INSPECTED:** Billing Agreement and pending-change records were not inspected; the UI null projection is not proof of absence.
- **Observation:** The existing tests default every Billing Agreement failure to a mocked not-found condition and cover expired/READY states, but do not distinguish not-found from server/transport failure or exercise preview failure and stale load completion.
- **Impact:** A healthy payment method can be falsely shown as absent, prompting unnecessary re-registration; a failed preview can look like no action is available.
- **Bounded follow-up:** Map only the explicit not-found contract to absence, expose recoverable read errors, retain the last confirmed projection with a stale warning, and test 404/401/403/5xx/network cases independently.

### ATS-027-F07 - Missing or malformed checkout query values can still invoke prepare

- **Severity:** P2
- **Rows/routes:** `G-PAY`, `MEM-10`; `/subscriptions/checkout`
- **Contract:** `deliverables/agent/WI-20260809-ATS-020-acceptance-matrix.md:123,167`
- **Exact source/test pointers:**
  - `frontend/src/pages/subscriber/SubscriptionPaymentPage.tsx:20-30,110-160,345-353`
  - `frontend/src/pages/subscriber/SubscriptionPaymentPage.test.tsx:215-268`
- **Evidence lanes:**
  - **UI copy/control - CONFIRMED FROM SOURCE:** Missing cycle silently becomes MONTHLY. Missing/malformed purpose silently becomes SUBSCRIBE. Missing plan eventually renders `선택한 플랜이 없습니다`.
  - **Frontend invocation - CONFIRMED FROM SOURCE:** A missing cycle still sends prepare with `MONTHLY`; an arbitrary cycle string is type-cast and can be sent. Missing plan and `purpose=UPGRADE` return before prepare.
  - **Server/Provider - PARTIAL SOURCE ONLY / OTHERWISE NOT INSPECTED:** Purpose is ignored by the prepare DTO as established in F01. Handling of every malformed cycle/request variant and any Provider invocation was not runtime-inspected.
  - **Durable state - BLOCKED / NOT INSPECTED:** A defaulted MONTHLY request reaches the persistence path, but no resulting row was inspected.
- **Observation:** The route contract requires malformed or missing plan/cycle/callback parameters to fail safely without payment invocation. Missing cycle instead becomes a valid financial choice and starts prepare.
- **Impact:** A malformed/deep-linked checkout can create an unintended monthly prepared order rather than requiring explicit cycle selection.
- **Bounded follow-up:** Parse plan, cycle, purpose, and callback fields with explicit allowlists before any fetch/POST; reject missing financial choices, and test zero prepare calls for each malformed/missing query variant.

### ATS-027-F08 - Checkout terminal/error states and initial-charge copy are inconsistent

- **Severity:** P2
- **Rows/routes:** `MEM-10`, `MEM-10F`; checkout and fail callback
- **Contract:** `deliverables/agent/WI-20260809-ATS-020-acceptance-matrix.md:167,169`
- **Exact source/test pointers:**
  - `frontend/src/pages/subscriber/SubscriptionPaymentPage.tsx:47-57,139-151,208-220,240-275,299-339`
  - `frontend/src/pages/subscriber/SubscriptionPaymentPage.test.tsx:102-169`
- **Evidence lanes:**
  - **UI copy/control - CONFIRMED FROM SOURCE:** Fail callback displays the raw `message` query value; `message=` produces blank visible/toast content. A terminal prepare error still labels Provider state `PREPARING` and says the order is being prepared. The initial charge CTA says only `카드 등록하기` although adjacent copy says the first charge follows registration.
  - **Frontend invocation - CONFIRMED FROM SOURCE:** Prepare failure leaves `paymentOrder=null` and disables the button, with only back navigation and no retry. Fail callback makes no server confirmation call.
  - **Server/Provider - BLOCKED / NOT INSPECTED:** Fail-query provenance, Provider cancel/error, and server state were not observed.
  - **Durable state - BLOCKED / NOT INSPECTED:** No order was checked for READY/IN_PROGRESS/EXPIRED state after failure or abandonment.
- **Observation:** Existing tests assert `카드 등록하기`, thereby preserving the current initial-charge command wording, but do not cover raw/blank fail messages, terminal prepare state, or retry recovery.
- **Impact:** Users can receive blank or unlocalized failure information, mistake a terminal failure for ongoing work, or consent to card registration without a command label that clearly names the immediate first charge.
- **Bounded follow-up:** Map provider codes to bounded product copy, treat blank as missing, render a terminal failed state with retry, and label the initial action to include both payment-method registration and the immediate first charge.

### ATS-027-F09 - Reactivation is a one-click financial lifecycle mutation

- **Severity:** P2
- **Rows/routes:** `MEM-11`, `INV-SUB`; `/subscriptions/manage`
- **Contract:** `deliverables/agent/WI-20260809-ATS-020-acceptance-matrix.md:170,261,276`
- **Exact source/test pointers:**
  - `frontend/src/pages/subscriber/SubscriptionManagePage.tsx:420-445,807-858`
  - `frontend/src/api/userSubscriptions.ts:170-175`
  - `frontend/src/pages/subscriber/SubscriptionManagePage.test.tsx:554-598`
- **Evidence lanes:**
  - **UI copy/control - CONFIRMED FROM SOURCE:** Cancellation has a confirmation modal, but `구독 유지하기` immediately calls reactivation even though the copy says automatic renewal will resume.
  - **Frontend invocation - CONFIRMED FROM SOURCE:** One click sends `POST /user-subscriptions/me/reactivate`; there is no confirmation, undo state, or typed unknown-outcome recovery.
  - **Server/Provider - NOT INSPECTED:** Reactivation server idempotency and Provider implications were not re-established within the allowed A/B/C backend scope.
  - **Durable state - BLOCKED / NOT INSPECTED:** Subscription/Billing Agreement state after reactivation was not observed.
- **Observation:** The existing test clicks once and asserts only that the API wrapper was called; it does not require confirmation or verify canonical reload.
- **Impact:** An accidental click can restore future automatic renewal, while response loss can leave the user unsure whether renewal was restored.
- **Bounded follow-up:** Require explicit confirmation that names the next billing date/amount, disable repeat submission, and show canonical reloaded status or an unknown-outcome recovery state.

### ATS-027-F10 - Selection, status, and copy semantics are inconsistent

- **Severity:** P3
- **Rows/routes:** `PUB-07`, `MEM-10`, `MEM-11`
- **Contract:** `deliverables/agent/WI-20260809-ATS-020-acceptance-matrix.md:139,167-170`
- **Exact source/test pointers:**
  - `frontend/src/pages/public/SubscriptionPlanPage.tsx:190-203,257-288,303-375`
  - `frontend/src/pages/subscriber/SubscriptionPaymentPage.tsx:198-220,299-323`
  - `frontend/src/pages/subscriber/SubscriptionManagePage.tsx:473-486,626-690,695-803`
  - `frontend/src/pages/public/SubscriptionPlanPage.test.tsx:51-95`
  - `frontend/src/pages/subscriber/SubscriptionPaymentPage.test.tsx:43-269`
  - `frontend/src/pages/subscriber/SubscriptionManagePage.test.tsx:72-598`
- **Evidence lanes:**
  - **UI copy/control - CONFIRMED FROM SOURCE:** Audience tabs lack programmatic selected state; the billing toggle has English `aria-label="Toggle billing cycle"` without exposed checked/pressed state; Manage cycle buttons expose styling but no selected semantics. Dynamic loading/error/success/provider states lack explicit live status semantics. English `Loading...`, `READY`, and `PREPARING` appear in an otherwise Korean flow.
  - **Frontend invocation - NOT APPLICABLE:** These defects do not independently change request payloads; related financial requests are covered by F01-F09.
  - **Server/Provider - NOT INSPECTED:** No server/provider defect is claimed for this finding.
  - **Durable state - NOT APPLICABLE / NOT INSPECTED:** Accessibility and copy observations are not durable-state evidence.
- **Observation:** The inspected page tests focus on visible text and click behavior; they do not assert audience/cycle selected semantics, live-region announcements, or localized accessible names/statuses.
- **Impact:** Keyboard and assistive-technology users receive incomplete selection and status feedback, and mixed-language/internal provider state weakens comprehension during a financial flow.
- **Bounded follow-up:** Use tab/segmented-control or pressed/checked semantics, add live status/error announcements, localize accessible names and user-facing states, and retain exact request behavior tests separately.

### ATS-027-F11 - Payment documentation has a stale schema count and ambiguous official-branch authority

- **Severity:** P2
- **Rows/routes:** Documentation / release-operator baseline
- **Exact source/docs pointers:**
  - `src/main/resources/schema.sql` - `41` actual `CREATE TABLE` statements
  - `docs/payment/feature-inventory.md:150-152`
  - `docs/design/index.md:29`
  - `docs/registry/project-registry.md:41-43`
  - `docs/payment/known-limits-and-next-steps.md:44`
  - Official-branch declarations verified by main in the payment index, payment checklist, payment inventory, payment known-limits document, and project registry
- **Evidence lanes:**
  - **Schema inventory - CONFIRMED:** Main counted `41` `CREATE TABLE` statements in `src/main/resources/schema.sql`.
  - **Documentation count - CONFIRMED:** Feature inventory, design index, and project registry report `41`, while `known-limits-and-next-steps.md:44` reports `39`.
  - **Branch/baseline metadata - CONFIRMED:** The payment index/checklist/inventory/known-limits documents and project registry identify `codex/p1-acceptance-hardening` as the official branch, while the audit baseline is `codex/v1-release-rehearsal-fixes@e343c20`.
  - **Operator intent - BLOCKED:** The inspected documentation does not distinguish whether `codex/p1-acceptance-hardening` is intentional release history or a current operator instruction. No branch operation or release action was performed.
- **Observation:** One payment document retains a stale `39`-table count against the schema and four other `41`-table declarations. Separately, the term `official branch` is presented without a time/version qualifier even though the audit uses a different branch and commit baseline.
- **Impact:** Operators can mis-scope schema verification and can mistake historical release provenance for the branch on which current audit, recovery, or release work must run. This weakens reproducibility even when the underlying product behavior is unchanged.
- **Bounded follow-up:** Update the stale count to `41` or generate all table counts from one authoritative inventory. Split branch metadata into explicitly dated `release history` and `current operator baseline`, identify the authoritative commit as well as branch, and cross-link the audit baseline instead of using an unqualified `official branch` label.

## Blocked and Not Inspected

| Evidence area                                                           | Status                   | Reason                                                                                                                                                                                                                        |
| ----------------------------------------------------------------------- | ------------------------ | ----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| Targeted frontend Vitest                                                | `PASSED`                 | Main ran `9` targeted files / `65` tests successfully in `3.65s`.                                                                                                                                                             |
| Frontend typecheck                                                      | `PASSED`                 | Main ran `npm run typecheck` successfully.                                                                                                                                                                                    |
| Targeted frontend ESLint                                                | `PASSED`                 | Main ran the targeted ESLint check successfully.                                                                                                                                                                              |
| Targeted backend tests                                                  | `PASS (146)`             | Run A: `--rerun-tasks`, `15` XML suites / `107` tests, `0` failures/errors/skipped, `54.9s`; run B: `7` suites / `39` tests, `0` failures/errors/skipped, `42.3s`.                                                            |
| Anonymous route guards                                                  | `OBSERVED`               | Checkout, success, fail, and manage guards preserved the exact encoded `returnTo` values.                                                                                                                                     |
| Public `/subscriptions` browser state                                   | `OBSERVED`               | Main observed GET and DOM state at `1280x720` and captured `output/ui-ux-audit/20260809/WI-027/PUB-07-business-yearly-1280x720-observed.png`.                                                                                 |
| Live public plan API ordering                                           | `OBSERVED`               | Unfiltered IDs were `1-3` INDIVIDUAL then `4-6` BUSINESS; the BUSINESS-filtered response returned `4-6`.                                                                                                                      |
| Authenticated browser reproduction                                      | `BLOCKED`                | Only anonymous guards and the public `/subscriptions` route were observed; authenticated runtime was not exercised.                                                                                                           |
| Provider prepare/auth/confirm/charge/cancel result                      | `BLOCKED`                | Provider execution was forbidden; source or mock is not Provider evidence.                                                                                                                                                    |
| PaymentOrder, Billing Agreement, Subscription, payment, and ledger rows | `BLOCKED`                | Database and durable-state inspection were forbidden.                                                                                                                                                                         |
| Response-loss/post-commit 5xx behavior                                  | `PARTIAL / LIVE BLOCKED` | Callback and upgrade idempotency are confirmed by source and targeted backend tests; cancel/reactivate no-charge re-call behavior is confirmed from source. Live transport, Provider, and durable outcomes were not observed. |
| Static schema inventory                                                 | `CONFIRMED`              | Main counted `41` `CREATE TABLE` statements in `src/main/resources/schema.sql`; no schema execution or database inspection was performed.                                                                                     |
| Configuration, secrets, and preserved ZIP                               | `NOT INSPECTED`          | Explicitly outside the allowed boundary.                                                                                                                                                                                      |

## Frozen Product Statement

Across the full WI, main performed the frontend verification, anonymous/public runtime observations, and the two targeted backend test runs recorded above (`146 PASS`). Authenticated runtime, live Provider operations, and live durable database inspection were not performed. During this resumed correction, only this findings document was modified, and no new exploration, test, browser, API, database, Provider, or ZIP operation was performed.
