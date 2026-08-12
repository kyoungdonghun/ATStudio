[WI HEADER]
WI ID: WI-20260809-ATS-027
REQ: REQ-20260809-ATS-001
Agent: qa-integ
Depends On: WI-20260809-ATS-020; WI-20260809-ATS-021..026 for cross-entry context only
Blocks: WI-20260809-ATS-030
Baseline: codex/v1-release-rehearsal-fixes @ e343c20

[WI SUMMARY]
Why: Perform the source/read-only/non-destructive integration audit assigned to WI-027 in the master acceptance matrix, correlating subscription/payment UI behavior, frontend requests, server/test-provider responses, and durable local state without executing a real payment side effect.

Scope (in/out):

In:

- Audit matrix rows `G-PAY`, `PUB-07`, `MEM-10`, `MEM-10S`, `MEM-10F`, `MEM-11`, and `INV-SUB`, including the subscription lifecycle scheduled-method boundary assigned to WI-027 in `WI-20260809-ATS-020-acceptance-matrix.md`.
- Cover `/subscriptions`, `/subscriptions/checkout`, `/subscriptions/checkout/success`, `/subscriptions/checkout/fail`, and `/subscriptions/manage`.
- Verify plans, audience, billing-cycle routing, exact USER-only guards, anonymous safe navigation, ADMIN denial target, malformed/missing plan/cycle/callback handling, and no unsafe return target.
- Trace billing-agreement prepare, confirm, fail/cancel, abandoned or expired agreement, re-registration, first charge, and callback replay/mismatch/expiry behavior.
- Trace upgrade, downgrade, billing-cycle change, cancel, reactivate, pending plan/cycle, immediate versus scheduled application, entitlement timing, reload consistency, and stale prepared-order handling.
- Audit the subscription lifecycle scheduler boundary for due renewal, first/recurring charge handoff, retry, grace, expiry, pending-plan application, idempotency, and the ordering/ownership of the scheduled methods. Verify scheduler behavior from source and existing automated tests or a controlled safe fixture only; do not infer production-provider success.
- Correlate frontend request wrappers and page state with provider-test/local ledger/subscription/billing-agreement/reload consistency. Treat UI copy, frontend call, server/provider-test response, and durable state as four independent evidence lanes.
- Use WI-021 through WI-026 findings/evidence only for exact shared route, guard, callback, fixture, browser-restoration, or evidence-boundary context. Re-check every conclusion against current source and the frozen baseline.

Out:

- Real Toss charge, refund, billing-agreement cancellation, provider mutation, live Toss action, external mail, or production-provider execution.
- DB, storage, ledger, subscription, billing-agreement, audit, or fixture mutation; schema changes; cleanup; deletion; reset; migration; or secret inspection.
- Authenticated mutation from a browser or API unless a separately approved safe fixture explicitly authorizes it. Anonymous guard navigation may remain read-only.
- Product code, tests, configuration, fixtures, runtime implementation, branch, unrelated/current product documentation, or unrelated deliverables. Documentation changes are limited to `deliverables/user/WI-20260809-ATS-027-summary.md`, `deliverables/agent/WI-20260809-ATS-027-evidence-pack.md`, `deliverables/agent/WI-20260809-ATS-027-findings.md`, and this handoff. Preserve `output/client-demo-screenshots-20260716-140514.zip` uninspected; verify it by path/status only and do not open, read, hash, or metadata-probe it.

DoD:

- Every in-scope matrix row is classified `PASS`, `FAIL`, `BLOCKED`, or `NOT RUN`, with route/fixture/viewport/state and an exact source, test, screenshot, or log pointer.
- Every scenario records separate statements for: UI copy/state/control; frontend request method/path/payload shape and whether invoked; server or provider-test response/error; and durable local ledger/subscription/billing-agreement/reload state. Mark unavailable lanes `BLOCKED` or `NOT INSPECTED`; never infer one lane from another.
- Findings are split by independent cause or contract and include severity, impact, evidence, and bounded follow-up. Do not convert a source/test-provider result into a claim of real provider success.
- The evidence pack includes the row matrix, source/API/backend/test crosswalk, test-provider behavior, scheduler boundary, screenshots, sanitized logs, limitations, and restoration record.
- The user summary states confirmed findings, blocked coverage, risks, approval points, exact quality results, and whether product code/worktree remained frozen.
- Targeted backend/frontend tests, typecheck, ESLint, output-document Prettier, documentation validation, and `git diff --check` are run only where applicable and recorded with exact command, exit code, counts/duration, warnings, and skipped/blocked reasons. No unexecuted command is reported as passed.
- Any browser session is restored to a neutral public route with dialogs/forms closed, no active checkout or download, no player overlay, and the viewport reset. Record the result.

Constraints/Forbidden:

- Treat `deliverables/agent/WI-20260809-ATS-020-acceptance-matrix.md` as the executable design and verify the branch/HEAD identity before relying on browser evidence.
- No real charge/refund/provider cancellation, no live Toss action, no external mail, no DB/storage mutation, no secret or credential inspection, and no authenticated mutation without separate explicit approval and a safe fixture.
- Test-provider behavior must be verified from source and automated tests or a controlled local fixture. Do not infer real provider success from UI copy, a mocked response, a prepared order, or a local row.
- Do not expose tokens, auth keys, customer keys, credentials, PII, secret configuration, physical storage paths, provider secrets, or sensitive request/response bodies. Sanitize all evidence.
- For `output/client-demo-screenshots-20260716-140514.zip`, verification is path/status-only. Do not open, read, hash, metadata-probe, move, replace, delete, stage, commit, branch, reset, or use it as a fixture.
- Product code is frozen during the audit. Do not edit any source, test, config, schema, fixture, or unrelated file.

[ACCEPTANCE CRITERIA]
Functional:

- [ ] `G-PAY`: verify `/subscriptions/checkout`, `/subscriptions/checkout/success`, and `/subscriptions/checkout/fail` permit only exact USER access; anonymous navigation goes to Login with a safe internal return; ADMIN resolves to `/admin/payments`; malformed or missing plan/cycle/callback parameters fail safely without a payment API invocation.
- [ ] `PUB-07`: verify plans load with deliberate loading/empty/error/retry states; individual/business audience and monthly/yearly cycle selection remain consistent; `fetchSubscriptionPlans` and optional `fetchMySubscription` behavior is explicit; audience mismatch stays safe; a paid USER routes to Manage and an eligible non-paid USER routes to Checkout; guest guard behavior remains distinct from public plan loading.
- [ ] `MEM-10`: verify valid, invalid, missing, stale, and re-registration checkout inputs; exact first-period or zero billing-agreement re-registration amount and purpose; `prepareBillingAgreement` request; recoverability after leaving/returning; no duplicate prepare or false activation.
- [ ] `MEM-10S`: verify prepared, already-confirmed, missing, mismatched, expired, replayed, and provider-test success/failure callback parameters; `confirmBillingAgreement` server-side contract validation and idempotency; UI success copy is separate from server/provider-test response; canonical Subscription and Billing Agreement reload is required before claiming durable success.
- [ ] `MEM-10F`: verify provider cancel/error and missing callback parameters, human-readable failure/cancel copy, no false activation or entitlement, stale prepared-order handling, and retry navigation to a valid plan or billing-method registration path.
- [ ] `MEM-11`: verify current subscription, plans, billing agreement, and change-preview requests; upgrade, downgrade, cycle change, pending-plan/cycle replacement or reversal, cancel, reactivate, expired/failed billing-method re-registration, charge timing, entitlement timing, and reload agreement for `U-I0`, `U-IA`, `U-IC`, `U-IP`, and business variants where fixtures exist. Mark authenticated mutation lanes `BLOCKED` when not separately approved.
- [ ] `INV-SUB`: inventory every subscription/payment frontend request, server endpoint, provider-test adapter/result, local ledger/payment-order/subscription/billing-agreement record, and reload projection used by the named flows. Reconcile the inventory with exact source and tests, including unused or API-only wrappers without deleting them.
- [ ] Scheduler boundary: verify the three scheduled methods in `SubscriptionScheduler` and their delegated services for due renewal, first/recurring charge, retry, grace, expiry, pending-plan application, no-double-charge/idempotency, and ordering. Use `SubscriptionSchedulerTest` and relevant payment/subscription tests or a controlled fixture; do not wait on wall-clock time or invoke production Toss.
- [ ] Cover loading, empty, validation, denied, not-found, provider-test error, server error, retry, duplicate, stale/race, replay, back, refresh, keyboard/focus, and responsive states for applicable rows. Preserve `PASS`, `FAIL`, `BLOCKED`, and `NOT RUN` distinctions.

Performance:

- [ ] At `1440x900`, `1024x768`, `390x844`, and `360x800`, record stable checkout/manage/plan layouts, copy wrapping, focus visibility, control availability, clipping, overflow, occlusion, and layout shift. Do not invent a response-time SLO.
- [ ] For plan/cycle selection, prepare/confirm/fail/retry, callback replay, manage changes, and scheduler claims, record request ordering and whether late responses can overwrite current state.

Quality:

- [ ] Findings cite the exact WI-020 row and exact frontend page/guard, API method/path, backend controller/service/entity/repository/DTO, provider-test source, automated test, and design contract.
- [ ] Evidence distinguishes UI copy, frontend call, server/provider-test response, and durable state for every operation; unavailable lanes are explicitly blocked or not inspected.
- [ ] No authenticated mutation, real payment result, refund, provider cancellation, durable row, ledger mutation, or scheduler side effect is claimed from source, mock, toast, or another lane alone.
- [ ] Record targeted Vitest and Gradle results, `npm run typecheck`, targeted ESLint, output-document Prettier, docs validation, and `git diff --check` with exact commands and outcomes when run.
- [ ] Record pre/post `git status --short`, branch/HEAD verification, frozen product-code result, ZIP preservation by path/status only, and browser restoration.

[INPUT POINTERS]
Tier 0 (Constitution - required):

- docs/standards/core-principles.md
- docs/standards/development-standards.md

Tier 1 (Policies - required/inferred):

- docs/policies/security-policy.md
- docs/policies/access-control-policy.md
- docs/policies/quality-gates.md
- docs/architecture/system-design.md

Tier 2 (Frontend and domain standards):

- docs/standards/frontend-standards.md
- .agents/skills/react-best-practices/AGENTS.md
- docs/design/api-spec.md
- docs/design/db-schema.md
- docs/design/payment-integration-design.md
- docs/design/p1-payment-db-integrity-design.md
- docs/design/p1-payment-integrity-remediation-design.md
- docs/design/usecase/user-subscription.md
- docs/payment/system-overview.md
- docs/payment/user-flows.md
- docs/payment/acceptance-test-checklist.md
- docs/payment/feature-inventory.md
- docs/payment/known-limits-and-next-steps.md
- docs/design/payment-operations-runbook.md

REQ/Context Docs:

- AGENTS.md
- deliverables/user/REQ-20260809-ATS-001.md
- deliverables/agent/WI-20260809-ATS-020-acceptance-matrix.md:1-3,119-125,139-140,167-170,204-209,457-474,561-569,571-582
- deliverables/agent/WI-20260809-ATS-020-evidence-pack.md
- deliverables/agent/WI-20260809-ATS-021-findings.md and WI-20260809-ATS-021-evidence-pack.md only for exact shared public/guard/browser evidence
- deliverables/agent/WI-20260809-ATS-022-findings.md and WI-20260809-ATS-022-evidence-pack.md only for exact shared auth/return-target evidence
- deliverables/agent/WI-20260809-ATS-023-findings.md and WI-20260809-ATS-023-evidence-pack.md only for exact shared public-shell evidence
- deliverables/agent/WI-20260809-ATS-024-findings.md and WI-20260809-ATS-024-evidence-pack.md only for exact shared subscriber guard/evidence boundary
- deliverables/agent/WI-20260809-ATS-025-findings.md and WI-20260809-ATS-025-evidence-pack.md only for exact shared payment/admin guard or evidence boundary
- deliverables/agent/WI-20260809-ATS-026-findings.md and WI-20260809-ATS-026-evidence-pack.md only for exact shared route/browser-restoration/evidence boundary
- .claude/config/workspace.json
- .claude/config/context-injection-rules.json

Files - frontend routes, guards, pages, APIs, and local-state boundary:

- frontend/src/router/index.tsx:44-70,106-136,151-182
- frontend/src/router/ProtectedRoute.tsx
- frontend/src/router/SubscriberRoute.tsx
- frontend/src/router/index.test.tsx
- frontend/src/router/ProtectedRoute.test.tsx
- frontend/src/router/SubscriberRoute.test.tsx
- frontend/src/pages/public/SubscriptionPlanPage.tsx
- frontend/src/pages/public/SubscriptionPlanPage.test.tsx
- frontend/src/pages/public/SubscriptionPlanPage.module.css
- frontend/src/pages/subscriber/SubscriptionPaymentPage.tsx
- frontend/src/pages/subscriber/SubscriptionPaymentPage.test.tsx
- frontend/src/pages/subscriber/SubscriptionPaymentPage.module.css
- frontend/src/pages/subscriber/SubscriptionPaymentReplay.test.tsx
- frontend/src/pages/subscriber/SubscriptionManagePage.tsx
- frontend/src/pages/subscriber/SubscriptionManagePage.test.tsx
- frontend/src/pages/subscriber/SubscriptionManagePage.module.css
- frontend/src/api/subscriptions.ts
- frontend/src/api/payments.ts
- frontend/src/api/userSubscriptions.ts
- frontend/src/api/domainApis.test.ts
- frontend/src/utils/tossPayments.ts
- frontend/src/utils/tossPayments.test.ts
- frontend/src/api/client.ts
- frontend/src/api/loadError.ts
- frontend/src/stores/ (subscription/auth/toast/local-storage stores used by named pages; list exact files found during audit)

Files - backend controllers, services, provider-test boundary, model, persistence, and scheduler:

- src/main/java/com/atstudio/atstudio/controller/SubscriptionController.java
- src/main/java/com/atstudio/atstudio/controller/PaymentController.java
- src/main/java/com/atstudio/atstudio/controller/UserSubscriptionController.java
- src/main/java/com/atstudio/atstudio/config/PaymentProperties.java
- src/main/java/com/atstudio/atstudio/service/SubscriptionService.java
- src/main/java/com/atstudio/atstudio/service/UserSubscriptionService.java
- src/main/java/com/atstudio/atstudio/service/BillingAgreementApplicationService.java
- src/main/java/com/atstudio/atstudio/service/PaymentCommandTransactionService.java
- src/main/java/com/atstudio/atstudio/service/SubscriptionUpgradePaymentExecutor.java
- src/main/java/com/atstudio/atstudio/service/SubscriptionScheduler.java:1-125
- src/main/java/com/atstudio/atstudio/service/payment/provider/recurring/RecurringPaymentProvider.java
- src/main/java/com/atstudio/atstudio/service/payment/provider/recurring/TossBillingProvider.java
- src/main/java/com/atstudio/atstudio/service/payment/provider/recurring/BillingAgreementPrepareCommand.java
- src/main/java/com/atstudio/atstudio/service/payment/provider/recurring/BillingAgreementConfirmCommand.java
- src/main/java/com/atstudio/atstudio/service/payment/provider/recurring/BillingChargeCommand.java
- src/main/java/com/atstudio/atstudio/entity/Subscription.java
- src/main/java/com/atstudio/atstudio/entity/UserSubscription.java
- src/main/java/com/atstudio/atstudio/entity/BillingAgreement.java
- src/main/java/com/atstudio/atstudio/entity/PaymentOrder.java
- src/main/java/com/atstudio/atstudio/entity/SubscriptionPayment.java
- src/main/java/com/atstudio/atstudio/entity/enums/SubscriptionStatus.java
- src/main/java/com/atstudio/atstudio/entity/enums/BillingAgreementStatus.java
- src/main/java/com/atstudio/atstudio/entity/enums/BillingCycle.java
- src/main/java/com/atstudio/atstudio/entity/enums/PaymentOrderStatus.java
- src/main/java/com/atstudio/atstudio/repository/SubscriptionRepository.java
- src/main/java/com/atstudio/atstudio/repository/UserSubscriptionRepository.java
- src/main/java/com/atstudio/atstudio/repository/BillingAgreementRepository.java
- src/main/java/com/atstudio/atstudio/repository/PaymentOrderRepository.java
- src/main/java/com/atstudio/atstudio/repository/SubscriptionPaymentRepository.java
- src/main/resources/schema.sql

Files - backend automated tests:

- src/test/java/com/atstudio/atstudio/controller/SubscriptionControllerTest.java
- src/test/java/com/atstudio/atstudio/controller/PaymentControllerTest.java
- src/test/java/com/atstudio/atstudio/controller/UserSubscriptionControllerTest.java
- src/test/java/com/atstudio/atstudio/entity/BillingAgreementStateMachineTest.java
- src/test/java/com/atstudio/atstudio/entity/PaymentOrderStateMachineTest.java
- src/test/java/com/atstudio/atstudio/entity/UserSubscriptionStateMachineTest.java
- src/test/java/com/atstudio/atstudio/entity/PaymentDatabaseIntegrityContractTest.java
- src/test/java/com/atstudio/atstudio/repository/BillingAgreementRepositoryTest.java
- src/test/java/com/atstudio/atstudio/repository/SubscriptionPaymentRepositoryLockContractTest.java
- src/test/java/com/atstudio/atstudio/service/BillingAgreementApplicationServiceTest.java
- src/test/java/com/atstudio/atstudio/service/BillingAgreementFailurePersistenceIntegrationTest.java
- src/test/java/com/atstudio/atstudio/service/PaymentCommandIndependentVerificationIntegrationTest.java
- src/test/java/com/atstudio/atstudio/service/PaymentCommandTransactionFenceTest.java
- src/test/java/com/atstudio/atstudio/service/PaymentProviderSuccessRecoveryIntegrationTest.java
- src/test/java/com/atstudio/atstudio/service/SubscriptionSchedulerTest.java
- src/test/java/com/atstudio/atstudio/service/SubscriptionServiceTest.java
- src/test/java/com/atstudio/atstudio/service/SubscriptionUpgradeCommandIntegrationTest.java
- src/test/java/com/atstudio/atstudio/service/UserSubscriptionServiceTest.java
- src/test/java/com/atstudio/atstudio/service/payment/provider/recurring/TossBillingProviderTest.java

Repro/Logs:

- Record `git branch --show-current`, `git rev-parse HEAD`, and `git status --short` before and after; confirm expected baseline, frozen product code, no stage/commit, and ZIP preservation by path/status only. Do not open, read, hash, or metadata-probe the ZIP.
- Record source/test commands and exact outputs for provider-test behavior, callback idempotency, lifecycle state machines, scheduler methods, and local ledger/subscription/billing-agreement projection. Do not print secrets or sensitive bodies.
- Record read-only browser route, fixture alias, guard result, viewport, screenshot/DOM/accessibility observation, console/network metadata, and exact redirect. Anonymous guard navigation is the only permitted browser navigation when no separately approved fixture exists.
- Targeted frontend: `cd frontend; npm run test -- src/pages/public/SubscriptionPlanPage.test.tsx src/pages/subscriber/SubscriptionPaymentPage.test.tsx src/pages/subscriber/SubscriptionPaymentReplay.test.tsx src/pages/subscriber/SubscriptionManagePage.test.tsx src/api/domainApis.test.ts src/utils/tossPayments.test.ts src/router/index.test.tsx src/router/ProtectedRoute.test.tsx src/router/SubscriberRoute.test.tsx`; record actual configured command if different.
- Targeted backend: `gradlew.bat test --tests "com.atstudio.atstudio.controller.SubscriptionControllerTest" --tests "com.atstudio.atstudio.controller.PaymentControllerTest" --tests "com.atstudio.atstudio.controller.UserSubscriptionControllerTest" --tests "com.atstudio.atstudio.entity.BillingAgreementStateMachineTest" --tests "com.atstudio.atstudio.entity.PaymentOrderStateMachineTest" --tests "com.atstudio.atstudio.entity.UserSubscriptionStateMachineTest" --tests "com.atstudio.atstudio.service.SubscriptionSchedulerTest" --tests "com.atstudio.atstudio.service.BillingAgreementApplicationServiceTest" --tests "com.atstudio.atstudio.service.UserSubscriptionServiceTest" --tests "com.atstudio.atstudio.service.payment.provider.recurring.TossBillingProviderTest"`; record actual result and limitations.
- Record `npm run typecheck`, targeted ESLint, output-document Prettier/check, documentation validation, and `git diff --check` only when run. Never report an unexecuted command as passed.
- Record browser restoration to a neutral public route, closed dialogs/forms, no checkout/download/player activity, and reset viewport.

[OUTPUT CONTRACT]
User-facing -> deliverables/user/WI-20260809-ATS-027-summary.md:

- Scope and row outcomes; confirmed findings; blocked/not-run coverage; risks and approval points; four-lane evidence summary; targeted quality results; screenshot inventory; browser restoration; and frozen-worktree/ZIP status.

Agent-facing -> deliverables/agent/WI-20260809-ATS-027-evidence-pack.md:

- Evidence index; row/scenario outcomes; UI/request/server-provider-test/durable-state lanes; source/API/backend/DB/provider/test crosswalk; scheduler boundary; screenshots and sanitized logs; test commands/results; limitations; cross-entry references; and rollback/no-mutation statement.

Findings -> deliverables/agent/WI-20260809-ATS-027-findings.md:

- One finding per independent cause or contract, with severity, affected matrix row/route, reproduction or source/test evidence, UI/request/server-provider-test/durable-state classification, impact, and bounded follow-up. Preserve `BLOCKED`, `NOT RUN`, and `NOT INSPECTED` distinctions.

Screenshot root -> output/ui-ux-audit/20260809/WI-027/:

- Use stable names containing row/scenario, viewport, and outcome. Do not overwrite prior WI evidence or open, read, hash, metadata-probe, or use the preserved demo ZIP.

Handoff Packet -> deliverables/agent/WI-20260809-ATS-027-handoff.md:

- This packet only.

[TRACEABILITY REQUIREMENTS]

Evidence pointers:

- Link every result to the exact WI-020 row, route/scenario, role/fixture alias, viewport, source/API/backend/test/design pointer, and screenshot or log path when available.
- For each prepare, confirm, fail, charge, upgrade, downgrade, cycle change, cancel, reactivate, retry, grace, expiry, and pending application operation, record four independent statements: UI copy/control; frontend invocation; server/provider-test response; durable local ledger/subscription/billing-agreement/reload state.
- Mark any unavailable authenticated, provider, DB, storage, ledger, or durable-state lane `BLOCKED` or `NOT INSPECTED`; do not substitute source or a mock for an observed runtime result.
- Reconcile frontend request paths and response types with backend controller mappings, DTOs, service transitions, provider-test adapter behavior, entity state machines, repository locking/idempotency, schema columns, and reload projections.
- For scheduler evidence, name the scheduled method, delegated command/service, clock/fixture assumptions, candidate selection, transition, retry/grace/expiry decision, pending-plan application rule, idempotency fence, and test pointer.
- Sanitize screenshots and logs. Never include auth keys, customer keys, tokens, secrets, PII, physical storage paths, or full sensitive bodies.

Tests:

- Include exact commands, exit codes, duration/counts, warnings, skips, environment/fixture limitations, and the source/test files that establish test-provider behavior. Distinguish automated test proof from browser observation and from production-provider behavior.

Rollback:

- No product rollback is expected because this WI is read-only and produces only the declared handoff outputs. If an audit artifact is accidentally created outside the declared output paths, stop, report it, and remove it only after explicit approval; do not alter product code, fixtures, DB, storage, or the preserved ZIP.
