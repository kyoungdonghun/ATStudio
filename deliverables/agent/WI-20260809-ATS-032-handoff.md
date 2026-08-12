[WI HEADER]
WI ID: WI-20260809-ATS-032
REQ: REQ-20260809-ATS-001
Session ID: none
Agent: se
Mandatory Reviewers: pg, qa-integ
Depends On: WI-20260809-ATS-031
Blocks: WI-20260809-ATS-033, WI-20260809-ATS-034
Baseline: codex/v1-release-rehearsal-fixes @ e343c2085fbc82c66b44fb8e5edde35bf920980f

[WI SUMMARY]

Why:

- `CR-031-081` shows that URL-only payment purpose can render a zero-payment registration flow while the server prepares a full-price subscription order, or render a paid subscription flow while the server prepares a zero-amount payment-method registration order.
- `CR-031-082` shows that plan-name-only routing loses the selected plan identity and audience. A BUSINESS user can therefore resolve an INDIVIDUAL plan with the same name and be blocked by the backend.
- Payment purpose, amount, selected plan identity, billing cycle, and authenticated audience must agree before Toss billing authorization can be opened.

Scope (in):

- Carry an immutable subscription plan ID and the selected audience from the plan/manage entry point into checkout while retaining the plan name only as display context.
- Resolve checkout plans by exact ID inside the authenticated user's audience, not by the first case-insensitive name match.
- Make the requested operation intent explicit at the billing-agreement prepare boundary.
- Have the backend compare requested intent with the authoritative intent derived from current subscription state before agreement preparation, order persistence, or Provider preparation.
- Treat the successful prepare response as authoritative for purpose, amount, subscription ID, billing cycle, currency, and checkout metadata.
- Reject and visibly recover before billing authorization if entry context, authenticated audience, selected plan, requested intent, expected price/zero amount, or prepare response disagree.
- Use the validated server-returned purpose and amount for checkout copy and callback URLs.
- Update the plan-selection and payment-method re-registration entry points, frontend API contract, backend request contract/service validation, and focused tests required by this boundary.
- Preserve correct new-subscription and payment-method re-registration behavior.

Scope (out):

- Prepare idempotency or duplicate-order control (`WI-033`).
- Callback response-loss, committed/unknown outcome recovery, or reload reconciliation (`WI-034`).
- General malformed checkout-query UX beyond what the new exact identity/intent contract necessarily rejects.
- Plan loading retry/latest-response behavior, payment terminal-state copy cleanup, accessibility/localization cleanup, or reactivation confirmation.
- Payment policy, pricing policy, subscription-change policy, Provider selection, schema changes, data mutation, live Provider calls, real charges/refunds, deployment, or branch operations.

DoD:

- [ ] PG records a pre-implementation review of the fail-closed boundary and confirms that no client-controlled purpose, audience, plan, or amount can open billing authorization after disagreement.
- [ ] QA-INTEG records a UI -> request -> server decision -> response -> checkout matrix for new subscription and payment-method re-registration.
- [ ] Plan and manage entry points send exact plan identity and audience; checkout does not select by name alone.
- [ ] Backend prepare rejects an intent/state mismatch before agreement preparation, order save, and Provider prepare.
- [ ] Frontend validates the prepare response before storing an actionable payment order or invoking the Toss SDK.
- [ ] Checkout copy, amount, and callback context use the validated response rather than URL-only purpose.
- [ ] Same-name INDIVIDUAL/BUSINESS plans are covered by tests.
- [ ] Non-subscriber `SUBSCRIBE`, active-subscriber `BILLING_AGREEMENT`, and both mismatch directions are covered across frontend and backend tests.
- [ ] Focused, adjacent, and complete applicable quality gates pass without a real Provider side effect.
- [ ] Evidence Pack and Korean user summary are written; current payment/design docs are updated only after verified behavior is stable.

Constraints / Forbidden:

- Use one Subagent at a time with `fork_context=false`; close each immediately after its bounded phase.
- Review order is PG, then QA-INTEG, then SE implementation. SE must not begin until both reviews record an acceptable bounded contract.
- No real Toss billing auth, billing-key issue, charge, refund, cancellation, mail, private-file, or production-data action. Use mocks, test Provider, H2, and synthetic fixtures only.
- No schema or architecture change. Stop and escalate if correct intent binding cannot be achieved without one.
- Do not broaden into `WI-033`, `WI-034`, or lower-severity payment findings.
- Do not weaken backend user-type validation, current-subscription validation, amount confirmation, or Provider isolation.
- Never open, read, hash, metadata-probe, move, replace, delete, stage, or use `output/client-demo-screenshots-20260716-140514.zip`.
- Never inspect ignored secrets, `application-local.yml`, `.env` files, credentials, tokens, keys, cookies, sessions, or environment secret values.
- No Git mutation until the WI is fully reviewed and verified by main.
- Stop and report immediately for a credible live-charge path, unauthorized audience access, data-loss risk, secret disclosure, or a required policy/security/architecture decision.

[ACCEPTANCE CRITERIA]

Functional:

- [ ] A valid INDIVIDUAL or BUSINESS plan card routes with its exact plan ID and audience.
- [ ] Checkout fetches and resolves the exact plan for the authenticated audience; a same-name plan in another audience cannot be selected or prepared.
- [ ] A non-subscriber can prepare only `SUBSCRIBE`; an active/grace-period subscriber can prepare only `BILLING_AGREEMENT` for the current plan/cycle.
- [ ] Both requested-purpose/state mismatch directions fail before agreement/order/Provider side effects.
- [ ] A response subscription ID, cycle, purpose, amount, currency, or audience/plan mismatch leaves no enabled billing-auth control and does not call `requestBillingAuth`.
- [ ] Correct `SUBSCRIBE` response shows the exact first-payment amount returned by the server.
- [ ] Correct `BILLING_AGREEMENT` response shows zero immediate payment and carries server purpose/amount into success/fail callback URLs.
- [ ] Existing upgrade payment-method re-registration returns to the selected change preview without performing the upgrade in this WI.

Performance:

- [ ] No additional unbounded list request or retry loop is introduced.
- [ ] The correction does not add a second Provider call or additional prepare call to a valid checkout attempt.

Quality:

- [ ] Focused frontend page/API tests pass.
- [ ] Focused backend prepare/service/controller tests pass.
- [ ] Adjacent subscription manage and payment replay tests pass.
- [ ] Full frontend tests, typecheck, ESLint, Prettier, and build pass.
- [ ] Full backend tests/build and applicable coverage gates pass.
- [ ] Docs validation and `git diff --check` pass.
- [ ] PG and QA-INTEG reviews explicitly distinguish UI copy, API invocation, server decision, Provider non-invocation, and durable-state evidence.

[INPUT POINTERS]

Tier 0 (Constitution and implementation standards):

- `docs/standards/core-principles.md`
- `docs/standards/development-standards.md`

Tier 1 (Security, access, and quality):

- `docs/policies/security-policy.md`
- `docs/policies/access-control-policy.md`
- `docs/policies/quality-gates.md`

Tier 2 (Frontend and payment contracts):

- `.agents/skills/react-best-practices/AGENTS.md`
- `docs/standards/frontend-standards.md`
- `docs/standards/evidence-pack-standard.md`
- `docs/design/payment-integration-design.md`
- `docs/design/api-spec.md`
- `docs/ui/screen-flow.md`

REQ / audit evidence:

- `deliverables/user/REQ-20260809-ATS-001.md`
- `deliverables/agent/WI-20260809-ATS-027-findings.md:34-77`
- `deliverables/agent/WI-20260809-ATS-027-evidence-pack.md`
- `deliverables/agent/WI-20260809-ATS-031-consolidated-findings.md:645-646,959-963,1176-1188`

Primary product sources:

- `frontend/src/pages/public/SubscriptionPlanPage.tsx`
- `frontend/src/pages/subscriber/SubscriptionManagePage.tsx`
- `frontend/src/pages/subscriber/SubscriptionPaymentPage.tsx`
- `frontend/src/api/subscriptions.ts`
- `frontend/src/api/payments.ts`
- `src/main/java/com/atstudio/atstudio/dto/payment/BillingAgreementPrepareRequest.java`
- `src/main/java/com/atstudio/atstudio/dto/payment/BillingAgreementPrepareResponse.java`
- `src/main/java/com/atstudio/atstudio/service/BillingAgreementApplicationService.java`

Focused tests:

- `frontend/src/pages/public/SubscriptionPlanPage.test.tsx`
- `frontend/src/pages/subscriber/SubscriptionManagePage.test.tsx`
- `frontend/src/pages/subscriber/SubscriptionPaymentPage.test.tsx`
- `frontend/src/pages/subscriber/SubscriptionPaymentReplay.test.tsx`
- `frontend/src/api/domainApis.test.ts`
- `src/test/java/com/atstudio/atstudio/service/BillingAgreementApplicationServiceTest.java`
- Relevant payment controller/API validation tests discovered during implementation.

[OUTPUT CONTRACT]

PG review -> `deliverables/agent/WI-20260809-ATS-032-pg-review.md`:

- Threat boundary, fail-closed conditions, side-effect ordering, and required negative tests.

QA-INTEG review -> `deliverables/agent/WI-20260809-ATS-032-qa-integ-review.md`:

- Cross-layer intent/audience/amount matrix, exact contract recommendation, and regression lanes.

User-facing -> `deliverables/user/WI-20260809-ATS-032-summary.md`:

- Korean summary of the defect, correction, test evidence, remaining limits, and follow-up WI.

Agent-facing -> `deliverables/agent/WI-20260809-ATS-032-evidence-pack.md`:

- Review decisions, evidence pointers, patch inventory, tests, Provider/durable evidence boundary, rollback, and next-WI trigger.

Handoff Packet -> `deliverables/agent/WI-20260809-ATS-032-handoff.md`:

- This packet.

[TRACEABILITY REQUIREMENTS]

- Map every product/test change to `CR-031-081` or `CR-031-082`; do not silently absorb another root.
- Record pre-side-effect backend ordering with source and focused-test evidence.
- Record frontend non-invocation proof for every mismatch condition.
- Separate UI copy/control, frontend request, server result, Provider action/non-action, and durable-state evidence.
- Do not claim live Provider or production durable-state evidence.
- Rollback must identify the bounded code/test/doc files changed by this WI and require no data rollback.
- On completion, trigger `WI-20260809-ATS-033` immediately unless an escalation blocks the payment command identity boundary.
