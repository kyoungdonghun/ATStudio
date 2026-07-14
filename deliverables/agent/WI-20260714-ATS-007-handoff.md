[WI HEADER]
WI ID: WI-20260714-ATS-007
REQ: REQ-20260714-ATS-001
Agent: se
Depends On: WI-20260714-ATS-002, WI-20260714-ATS-004, WI-20260714-ATS-005, WI-20260714-ATS-006
Blocks: WI-20260714-ATS-018, WI-20260714-ATS-023, WI-20260714-ATS-025

[WI SUMMARY]
Why: Bind renewal orders to one agreement/subscription/billing period and isolate each Provider call/local outcome from every other agreement.
Scope: Keyset due-ID scan, exact period order identity, per-agreement claim/outcome/finalize transactions, scheduler transaction removal, stale/ambiguous recovery rule, and focused batch/period tests.
Out: Multi-server scheduler lock, distributed leases, live Toss, and DB application.
DoD: Old period/subscription orders are never reused; one agreement failure cannot roll back another; no transaction spans all due Provider calls.
Constraints: Extend shared WI-005/WI-006 helper and preserve single-server operation. Never blind replay stale processing; use provider lookup/reconciliation.

[ACCEPTANCE CRITERIA]
- [ ] Renewal lookup includes agreement, user subscription, purpose, and billing period start.
- [ ] Candidate IDs use bounded keyset pages and each agreement commits independently.
- [ ] First provider success remains committed if a later agreement fails.
- [ ] Retry/grace/date behavior matches current approved policy.
- [ ] Focused renewal/batch/concurrency tests, compile, and diff check pass.

[INPUT POINTERS]
Tier 0: docs/standards/core-principles.md; docs/standards/development-standards.md
Tier 1: docs/policies/quality-gates.md
Context: deliverables/user/REQ-20260714-ATS-001.md; docs/design/p1-payment-db-integrity-design.md; WI-004 through WI-006 evidence
Files: RecurringRenewalService; SubscriptionScheduler; shared command helper/key factory; BillingAgreement/PaymentOrder repositories; focused tests

[OUTPUT CONTRACT]
User summary: deliverables/user/WI-20260714-ATS-007-summary.md (Korean)
Evidence Pack: deliverables/agent/WI-20260714-ATS-007-evidence-pack.md
Implementation ownership: renewal/scheduler flow and shared command helper extensions/tests.

[TRACEABILITY REQUIREMENTS]
Per-agreement transaction and old-period non-reuse evidence required; no multi-server claim.
