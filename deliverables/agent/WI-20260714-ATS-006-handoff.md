[WI HEADER]
WI ID: WI-20260714-ATS-006
REQ: REQ-20260714-ATS-001
Agent: se
Depends On: WI-20260714-ATS-002, WI-20260714-ATS-004, WI-20260714-ATS-005
Blocks: WI-20260714-ATS-007, WI-20260714-ATS-018, WI-20260714-ATS-023

[WI SUMMARY]
Why: Make charged upgrades converge on one deterministic command, one Provider intent per attempt, and one local finalization.
Scope: Locked upgrade recomputation/claim, canonical command key, persisted attempt key, no-transaction Provider call, durable result, idempotent local finalization, and concurrency/retry tests.
Out: Renewal behavior, zero-amount policy changes, DB application, or live Toss.
DoD: Concurrent/retried upgrade requests cannot create duplicate charges/payments/plan transitions; `PROVIDER_SUCCEEDED` retry finalizes without Provider call.
Constraints: Extend WI-005 helpers instead of duplicating transaction logic. Preserve approved current proration and plan-change policy.

[ACCEPTANCE CRITERIA]
- [ ] Agreement/subscription/order locks follow approved order and all state is revalidated.
- [ ] Stable upgrade command key reuses the same order; failed explicit retry increments persisted attempt.
- [ ] Unique-constraint races converge to the existing command.
- [ ] Focused service and concurrency tests, compile, and diff check pass.

[INPUT POINTERS]
Tier 0: docs/standards/core-principles.md; docs/standards/development-standards.md
Tier 1: docs/policies/quality-gates.md
Context: deliverables/user/REQ-20260714-ATS-001.md; docs/design/p1-payment-db-integrity-design.md; WI-004/WI-005 evidence
Files: UserSubscriptionService; shared payment command helper/key factory; BillingAgreement/UserSubscription/PaymentOrder repositories; focused tests

[OUTPUT CONTRACT]
User summary: deliverables/user/WI-20260714-ATS-006-summary.md (Korean)
Evidence Pack: deliverables/agent/WI-20260714-ATS-006-evidence-pack.md
Implementation ownership: charged upgrade flow and extensions to shared command helper/tests.

[TRACEABILITY REQUIREMENTS]
Provider call count, committed outcome, retry, and rollback evidence required.
