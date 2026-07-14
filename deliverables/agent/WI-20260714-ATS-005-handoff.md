[WI HEADER]
WI ID: WI-20260714-ATS-005
REQ: REQ-20260714-ATS-001
Agent: se
Depends On: WI-20260714-ATS-002, WI-20260714-ATS-004
Blocks: WI-20260714-ATS-006, WI-20260714-ATS-007, WI-20260714-ATS-018, WI-20260714-ATS-023

[WI SUMMARY]
Why: Persist initial billing-confirm provider failure/success evidence independently from the API exception and local subscription finalization.
Scope: Non-transactional orchestrator, new proxied command transaction service for initial confirm, deterministic billing-confirm command identity, durable billing-key/charge outcomes, finalization retry, incident-based cleanup failure, and tests.
Out: Upgrade/renewal/refund execution, live Toss, DB application, or automatic stale replay.
DoD: Provider calls have no active local transaction; failure survives thrown BusinessException; provider success is committed before finalization; retry never charges again after success.
Constraints: Preserve encrypted key material until tracked Toss cleanup succeeds. Fifteen-minute stale claims become pending/reconciliation, never blind replay. No secret payloads in logs/tests.

[ACCEPTANCE CRITERIA]
- [ ] Separate Spring bean applies `REQUIRES_NEW` claim/outcome/finalize methods.
- [ ] Initial issue/charge failure reloads as durable failure in a new transaction.
- [ ] Provider success/local failure reloads as `PROVIDER_SUCCEEDED` and retry finalizes locally only.
- [ ] Cleanup failure is recoverable evidence and does not erase the failed command.
- [ ] Focused tests, compile, and diff check pass.

[INPUT POINTERS]
Tier 0: docs/standards/core-principles.md; docs/standards/development-standards.md
Tier 1: docs/policies/security-policy.md; docs/policies/quality-gates.md
Context: deliverables/user/REQ-20260714-ATS-001.md; docs/design/p1-payment-db-integrity-design.md; WI-004 evidence
Files: BillingAgreementApplicationService; PaymentOrder/BillingAgreement repositories and entities; payment provider recurring interfaces; reconciliation incident service; focused tests

[OUTPUT CONTRACT]
User summary: deliverables/user/WI-20260714-ATS-005-summary.md (Korean)
Evidence Pack: deliverables/agent/WI-20260714-ATS-005-evidence-pack.md
Implementation ownership: initial billing-confirm orchestration, shared transaction helper initial methods/key factory, and focused tests.

[TRACEABILITY REQUIREMENTS]
Reproducible provider-call ordering and committed-reload evidence required; no live calls.
