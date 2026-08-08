---
version: 1.0
last_updated: 2026-08-08
project: ATS
owner: qa-integ
category: evidence-pack
status: confirmed
dependencies:
  - path: WI-20260808-ATS-004-handoff.md
    reason: Approved Work Item scope and output contract
  - path: ../user/REQ-20260808-ATS-002.md
    reason: Approved request and acceptance criteria
  - path: ../user/WI-20260808-ATS-004-summary.md
    reason: User-facing findings and recommendation
---
# Evidence Pack: WI-20260808-ATS-004

## Summary (one-liner)

- Confirmed that the ADMIN subscription editor cannot change plan but can independently persist status, cycle, and expiration without relational validation or billing-side synchronization; recommends routing plan changes through a guarded general entitlement-correction workflow modeled on the existing refund-linked implementation.

## Scope / DoD Check

- [x] Distinguished ADMIN plan-list loading from fields actually accepted by the edit API.
- [x] Determined that current plan replacement is not supported by the screen, TypeScript contract, Java DTO, service, or entity `adminUpdate` path.
- [x] Defined an `ACTIVE`/`CANCELLED`/`EXPIRED` expiration-date matrix consistent with current grace-period semantics.
- [x] Traced local access, pending change, billing agreement, renewal, payment-order, and provider boundaries separately.
- [x] Compared a quick-edit plan selector with a guarded general entitlement-correction workflow and selected the latter.
- [x] Identified the existing refund-linked correction implementation as the closest reusable safety pattern.
- [x] Specified API, domain, UI, concurrency, audit, and regression-test requirements for SR-97.
- [x] Changed only the two WI-004 deliverables; no code, SR, index, DB, billing, or provider state was modified.

## Reference Documents (Tier 0-2)

**Injected Context** (from WI Handoff Packet):

| Tier | Document | Reason |
| --- | --- | --- |
| 0 | `docs/standards/core-principles.md` | Constitution, transparent evidence, and platform financial integrity |
| 0 | `docs/standards/development-standards.md` | Cross-layer and test design standards |
| 1 | `docs/policies/quality-gates.md` | Reuse, domain-fit, traceability, and regression expectations |
| 1 | `docs/policies/security-policy.md` | ADMIN/payment boundary and audit-safe handling |
| 2 | `docs/standards/frontend-standards.md` | React validation and error-state expectations |
| 2 | `.agents/skills/react-best-practices/AGENTS.md` | React implementation context supplied by the handoff |
| 2 | `docs/design/api-spec.md` | ADMIN subscription endpoints are emergency controls |
| 2 | `docs/design/usecase/user-subscription.md` | Service-enabled grace period and paid plan-change lifecycle |
| 2 | `docs/SR/SR-14.md` | Historical request to inspect and change subscription status/product |
| Context | `deliverables/user/REQ-20260808-ATS-002.md` | Approved SR-97 investigation scope |

**Additional reuse candidates inspected:**

| Document / implementation | Reason |
| --- | --- |
| `docs/design/payment-operations-runbook.md` | Existing guarded entitlement-correction operational rules |
| `docs/design/payment-refund-receipt-settlement-policy.md` | Separation of payment mutation and local access correction |
| `docs/design/payment-integration-design.md` | Direct ADMIN update is an emergency, not ordinary payment path |
| `AdminPaymentEntitlementCorrectionService` and UI | Existing preview, validation, locking, approval, audit, and target-plan pattern |

**Injection Rules Applied**:

- Rule source: `.claude/config/context-injection-rules.json`
- Assignee: `qa-integ`
- Task type: integration/payment/validation review
- Injected tiers: Tier 0, relevant Tier 1, React tech-stack context, and task-specific Tier 2/context pointers

## Evidence Pointers (required)

### 1. UI Display and Request Construction

- `frontend/src/pages/admin/UserSubscriptionManagePage.tsx:38-45`
  - Loads plans and maintains only status, cycle, and expiration edit state; there is no target-plan state.
- `frontend/src/pages/admin/UserSubscriptionManagePage.tsx:88-96`
  - Fetches the complete ADMIN plan list.
- `frontend/src/pages/admin/UserSubscriptionManagePage.tsx:100-125`
  - Initializes and submits only changed status, cycle, and expiration; performs no state/date validation.
- `frontend/src/pages/admin/UserSubscriptionManagePage.tsx:193-197`
  - Uses the plan list only to resolve the pending-plan display name.
- `frontend/src/pages/admin/UserSubscriptionManagePage.tsx:221-275`
  - Edit modal exposes status, billing cycle, and expiration controls only.
- `frontend/src/api/subscriptions.ts:28-33`
  - `fetchAdminSubscriptionPlans` returns all plans, including inactive plans.
- `frontend/src/api/userSubscriptions.ts:102-115`
  - `AdminUpdateSubscriptionRequest` omits `subscriptionId` and sends only status/cycle/expiration.
- `frontend/src/test/coverage/adminSubscriberGaps.coverage.test.tsx:498-525`
  - Existing UI coverage expects `CANCELLED`, `YEARLY`, and a future expiration to be submitted together; no plan field or matrix rejection is asserted.

### 2. API and Persistence Behavior

- `src/main/java/com/atstudio/atstudio/dto/subscription/AdminUpdateSubscriptionRequest.java:8-12`
  - Java request contract has only nullable status, cycle, and expiration fields and no cross-field constraint.
- `src/main/java/com/atstudio/atstudio/controller/UserSubscriptionController.java:58-69`
  - ADMIN endpoint applies `@Valid`, but the DTO has no validation annotations and actor identity is not passed into the service for audit.
- `src/main/java/com/atstudio/atstudio/service/UserSubscriptionService.java:196-202`
  - Loads by ID and directly calls entity `adminUpdate`; no plan lookup, lock, transition/date check, billing check, or audit exists.
- `src/main/java/com/atstudio/atstudio/entity/UserSubscription.java:27-52`
  - Current plan and pending plan/cycle are separate persisted fields that must be considered together.
- `src/main/java/com/atstudio/atstudio/entity/UserSubscription.java:127-135`
  - `adminUpdate` independently assigns provided status/cycle/expiration and leaves current/pending plan state untouched.
- `src/main/java/com/atstudio/atstudio/repository/UserSubscriptionRepository.java:22-41`
  - Service-enabled access is `ACTIVE` or `CANCELLED` with `expiresAt >= today`; expired scanning finds `ACTIVE`/`CANCELLED` with past expiration.
- `src/main/java/com/atstudio/atstudio/repository/UserSubscriptionRepository.java:52-62`
  - Pessimistic-lock methods exist but the current ADMIN update uses unlocked `findById`.
- `src/test/java/com/atstudio/atstudio/service/UserSubscriptionServiceTest.java:639-692`
  - ADMIN tests cover all-field assignment, null no-op, and not-found only. Invalid state/date, plan, pending, billing, audit, and concurrency cases are absent.

### 3. Access and Date Semantics

- `docs/design/usecase/user-subscription.md:44-47`
  - `ACTIVE` and non-expired `CANCELLED` subscriptions are service-enabled.
- `docs/design/usecase/user-subscription.md:69-77`
  - Cancellation preserves paid access through expiration; grace-period reactivation requires reusable billing state.
- `src/test/java/com/atstudio/atstudio/service/UserSubscriptionServiceTest.java:830-888`
  - Tests explicitly establish future-dated `CANCELLED` as a valid grace period and past expiration as non-service-enabled.
- `src/main/java/com/atstudio/atstudio/service/SubscriptionScheduler.java:79-99`
  - A daily job converts past-dated `ACTIVE`/`CANCELLED` rows to `EXPIRED`; direct ADMIN edits can therefore create contradictory interim states until the scheduler runs.
- `src/main/java/com/atstudio/atstudio/service/AdminPaymentEntitlementCorrectionService.java:289-320`
  - Existing guarded correction already rejects `EXPIRED` with future expiration and non-`EXPIRED` with past expiration, plus inactive/wrong-type target plans and no-op targets.
- `src/test/java/com/atstudio/atstudio/service/AdminPaymentEntitlementCorrectionServiceTest.java:506-586`
  - Existing tests cover owner mismatch, wrong user type, inactive plan, future-dated `EXPIRED`, past-dated `ACTIVE`, and no-op correction.

### 4. Billing Agreement, Renewal, and Pending-Change Effects

- `src/main/java/com/atstudio/atstudio/service/UserSubscriptionService.java:205-225`
  - `adminCancel` and `adminUpdate(CANCELLED)` change only the subscription. In contrast, self-cancel also marks the local billing agreement cancelled.
- `src/main/java/com/atstudio/atstudio/entity/BillingAgreement.java:94-108,202-219,248-257`
  - Agreement maintains an independent `nextBillingAt` and state lifecycle; ADMIN subscription edits do not synchronize either.
- `src/main/java/com/atstudio/atstudio/repository/BillingAgreementRepository.java:23-42`
  - Renewal eligibility depends on active agreement date plus an `ACTIVE` current subscription; local subscription and agreement state can drift without immediate charging.
- `src/main/java/com/atstudio/atstudio/service/PaymentCommandTransactionService.java:251-276`
  - Renewal claims lock the active subscription and use the billing agreement's next billing date.
- `src/main/java/com/atstudio/atstudio/service/PaymentCommandTransactionService.java:763-807`
  - Renewal order plan, cycle, and amount come from current/pending subscription state; a direct plan/cycle mutation therefore changes future charge inputs without a corresponding payment decision record.
- `src/main/java/com/atstudio/atstudio/service/PaymentCommandTransactionService.java:1070-1079`
  - Pending plan/cycle override current values for renewal, so an ADMIN cycle edit can be superseded by an existing pending change.
- `src/main/java/com/atstudio/atstudio/service/PaymentCommandTransactionService.java:667-715`
  - Successful renewal atomically applies target plan/cycle/period and updates agreement `nextBillingAt`; the direct ADMIN editor bypasses this lifecycle.
- `docs/design/payment-integration-design.md:95-106`
  - Provider mutation requires committed command phases; direct ADMIN update remains an emergency control, not an ordinary payment path.

### 5. Existing Guarded Correction Pattern Available for Reuse

- `frontend/src/pages/admin/PaymentOperationsPage.tsx:728-752`
  - Builds an explicit target request with refund, plan, cycle, status, expiration, pending-clear, agreement-cancel, and reason fields.
- `frontend/src/pages/admin/PaymentOperationsPage.tsx:1209-1334`
  - Provides target plan selection, explicit local-access boundary, pending-change and agreement controls, and an operator note.
- `src/main/java/com/atstudio/atstudio/dto/payment/AdminPaymentEntitlementCorrectionRequest.java:10-18`
  - Models the complete target state with required fields.
- `src/main/java/com/atstudio/atstudio/service/AdminPaymentEntitlementCorrectionService.java:118-169`
  - Creation locks billing/subscription state, rejects duplicate non-terminal requests and provider-outcome races, snapshots before/target state, and writes an audit event.
- `src/main/java/com/atstudio/atstudio/service/AdminPaymentEntitlementCorrectionService.java:195-258`
  - Execution rechecks the before-state, applies the exact target, optionally cancels the local agreement, and records result audit.
- `src/main/java/com/atstudio/atstudio/service/AdminPaymentEntitlementCorrectionService.java:372-379`
  - Blocks correction while charge purposes have unresolved provider outcomes.
- `docs/design/payment-operations-runbook.md:213-236`
  - Documents preview/request/approve/execute, locks, drift detection, pending-provider fencing, date matrix, target-plan validation, and rollback.
- `docs/design/payment-refund-receipt-settlement-policy.md:121-130`
  - Separates provider refund from local entitlement correction and requires explicit target access state.

### 6. Existing Design and Historical Requirement

- `docs/SR/SR-14.md:5-13`
  - Historical SR requested ADMIN status/product inspection and change with side-effect review; product change remains unimplemented in this screen.
- `docs/design/api-spec.md:53-60`
  - Current subscription payment is Toss recurring-only, and ADMIN update/cancel are documented emergency operations.
- `docs/design/usecase/user-subscription.md:49-66`
  - Ordinary member plan changes have upgrade charge or deferred renewal semantics, while ADMIN mappings are emergency controls rather than checkout/payment substitutes.
- `docs/design/payment-refund-receipt-settlement-policy.md:46-55`
  - Ordinary status/cycle/expiration edits remain in user subscription admin, whereas audited entitlement correction is currently refund-linked.

### 7. Input Pointer Discrepancy

- `src/main/java/com/atstudio/atstudio/entity/Payment.java` does not exist.
- Relevant current entities are `PaymentOrder`, `SubscriptionPayment`, `BillingAgreement`, and `PaymentEntitlementCorrection`; the investigation used these actual paths.

## Recommended State/Date Matrix

| Target status | Allowed `expiresAt` | Reject / normalize | Billing and pending implications |
| --- | --- | --- | --- |
| `ACTIVE` | `>= businessToday` | Reject past date | Do not infer renewable billing; verify or display agreement status, reconcile `nextBillingAt`, and decide pending state |
| `CANCELLED` | `>= businessToday` | Past date should be rejected or explicitly normalized to `EXPIRED` | Future/today is valid paid grace; stop renewal locally and define whether the retained billing key remains reusable |
| `EXPIRED` | `<= businessToday` | Reject future date | Default to clearing pending change and require explicit local agreement termination/preservation decision |

Additional rules:

- Use one injected `Clock`/configured business zone for validation and scheduler semantics rather than scattered `LocalDate.now()` calls.
- Keep frontend checks advisory; backend service/domain validation is authoritative.
- Reject an empty or no-op mutation with a stable business error or return an explicit `NO_CHANGE` result.
- Changing status, cycle, expiration, plan, pending change, and billing disposition must be evaluated as one target state, not independent patches.

## Alternatives and Recommendation

### Alternative A: Add plan selection to the existing quick editor

**Benefits**:

- Minimal navigation and a familiar operator workflow.

**Required safeguards**:

- Add `subscriptionId`, `clearPendingChange`, billing-agreement disposition, and mandatory reason/confirmation to the request.
- Validate active target plan and exact user-type match server-side.
- Lock billing agreement before subscription, reject unresolved charge orders, snapshot/recheck before-state, and audit actor/before/after/reason.
- Explicitly define whether the operation is immediate local access correction, deferred paid change, or a provider-mutating payment command.

**Risk**:

- A superficial select-plus-DB-update implementation can silently change the next renewal plan/amount while leaving `nextBillingAt`, pending changes, and financial evidence inconsistent.

### Alternative B: Route plan change through a general guarded entitlement-correction workflow — recommended

- Retain or narrow quick edit to a hardened, limited status/date correction entry point.
- Generalize the existing refund-linked correction pattern for non-refund support/operations cases, using a support ticket/incident/reason as the authoritative basis.
- Reuse preview, exact target state, active/wrong-type plan validation, lock ordering, unresolved-provider fence, pending-change choice, local agreement choice, confirmation, approval where required, and audit ledger behavior.
- Keep local entitlement correction provider-neutral and non-financial. Any charge, refund, or provider billing-key deletion remains an explicitly separate workflow.

This option satisfies the user's need for plan selection without misrepresenting an unaudited local database update as a paid subscription-plan change.

## SR-97 Required Requirements

1. State current facts explicitly: plans are loaded for pending-name display, plan replacement is unsupported, and only status/cycle/expiration are independently mutable.
2. Enforce the proposed state/date matrix on the backend and mirror it in UI guidance.
3. Preserve `CANCELLED` + current/future expiration as a valid grace-period state.
4. Do not add a plan selector without target-plan activity/user-type validation and full target-state preview.
5. Surface both pending plan and pending billing cycle; require an explicit clear/preserve decision during correction.
6. Separate local access, local billing-agreement state, provider billing-key state, payment/refund actions, and persisted audit evidence in the UI confirmation and response.
7. Lock/recheck the affected subscription and billing agreement and block unresolved `SUBSCRIBE`/`UPGRADE`/`RENEWAL` orders.
8. Require operator reason, actor identity, before/after state, timestamp, and result audit for non-trivial ADMIN corrections.
9. Prefer a generalized guarded correction workflow modeled on the existing refund-linked implementation; label it as local entitlement correction, not ordinary paid plan change.

## Commands & Outputs

| Command | Result |
| --- | --- |
| Focused full-file reads of ADMIN subscription React/API, Java DTO/controller/service/entity/repository, tests, agreement, scheduler, and payment transaction code | PASS; cross-layer behavior mapped to exact lines |
| `rg -n "AdminUpdateSubscriptionRequest\|adminUpdate\|fetchAdminSubscriptionPlans\|pendingSubscription\|expiresAt\|EXPIRED\|CANCELLED" ...` | Completed with one expected path error because handoff referenced nonexistent `entity/Payment.java`; actual payment entities were resolved with `rg --files` |
| `rg -n "applyEntitlementCorrection\|EntitlementCorrection\|correction" src/main/java src/test/java frontend/src docs` | PASS; located the existing refund-linked guarded correction implementation and policy |
| Focused reads of `BillingAgreementRepository`, `PaymentCommandTransactionService`, and `SubscriptionScheduler` | PASS; confirmed renewal and expiration side effects |

## Tests

- No application tests were run because WI-004 is a read-only investigation and prohibits product changes.

Required implementation regression coverage:

- Backend matrix tests for every status with past/today/future dates, including valid `CANCELLED` grace.
- Backend plan tests for missing, inactive, wrong-user-type, same-plan/no-op, and valid target plans.
- Integration tests for lock order, stale before-state, concurrent corrections, and unresolved payment-order fencing.
- Tests for pending plan/cycle clear versus preserve behavior.
- Tests for agreement status and `nextBillingAt` synchronization/disposition, including no unintended provider call.
- Audit tests for actor, reason, before/target/result state, and failure rollback.
- Controller contract tests for stable 4xx error codes and target-state response.
- Frontend tests for plan loading failure, active/user-type filtering, state/date validation, today boundary, pending-state warning, preview/confirmation, server rejection, and successful refresh.
- Contract test proving that local correction does not imply a charge, refund, or provider billing-key deletion.

## Files Changed

- `deliverables/user/WI-20260808-ATS-004-summary.md`
  - User-facing current-state verdict, matrix, alternatives, and recommendation.
- `deliverables/agent/WI-20260808-ATS-004-evidence-pack.md`
  - This traceability and reproducibility record.

No code, SR, index, DB, local billing agreement, payment ledger, or external provider state was changed.

## Risks / Rollback

### Risks

- A quick plan selector could mutate future renewal price without payment evidence or customer intent.
- Direct status changes can leave access, billing-agreement status, and `nextBillingAt` inconsistent even if the current scheduler does not immediately charge a non-`ACTIVE` subscription.
- Existing pending change can override a direct current plan/cycle edit at renewal.
- Generalizing the refund-linked correction ledger requires an approved non-refund evidence model; making `paymentRefundId` nullable without a replacement support/incident reference would weaken traceability.
- Date validation based on different host zones could disagree at midnight; use the configured business clock.

### Rollback

- Remove only `deliverables/user/WI-20260808-ATS-004-summary.md` and this Evidence Pack if the investigation record is abandoned.
- Preserve the handoff, approved REQ, code, SRs, indexes, DB, billing/payment state, and unrelated user files.

## Follow-ups

- `WI-20260808-ATS-006` should use this pack when drafting SR-97 and keep current behavior, recommendation, and unapproved policy choices visibly separated.
- Before implementation, approve whether general ADMIN plan correction requires single-operator confirmation or request/approve separation, and choose the non-refund evidence reference type.
