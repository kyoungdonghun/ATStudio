[WI HEADER]
WI ID: WI-20260715-ATS-006
REQ: REQ-20260714-ATS-001
Agent: se
Depends On: WI-20260715-ATS-002
Blocks: WI-20260715-ATS-007

[WI SUMMARY]
Why: Close scheduled reconciliation F-02 and F-04 by turning exact provider-DONE evidence into purpose-specific local finalize-only recovery without issuing a new charge.
Scope (in): Package F only: non-transactional scheduled orchestration, new short reconciliation transaction service, strict provider evidence gate, richer lookup result/adapter evidence where required, `PROVIDER_SUCCEEDED` persistence, purpose dispatch for `SUBSCRIBE`/`UPGRADE`/`RENEWAL`, Incident resolve/open behavior, and focused tests.
Scope (out): Normal payment command ownership in B, cancellation, upgrade orchestration, refund recovery, MySQL proof, UI, live provider mutation, and mismatched-evidence auto-correction.
DoD: Scheduled provider lookup runs without a broad transaction; exact DONE evidence finalizes once with no charge; mismatches remain Incident-only; provider success/local failure resumes later; focused tests and two-set deliverables pass.
Constraints/Forbidden: Never auto-finalize on amount/order/currency/transaction mismatch. Do not change provider mutation methods or call live Toss. Do not mutate retained DB. Preserve concurrent work and preview runtime.

[ACCEPTANCE CRITERIA]
Functional:
- [ ] Lookup evidence includes exact order ID, amount, currency, provider status, and authoritative transaction ID.
- [ ] Only eligible `PROCESSING` stale or `PENDING_PROVIDER_CONFIRMATION` commands can transition to `PROVIDER_SUCCEEDED`.
- [ ] Purpose-specific finalizer creates at most one payment/entitlement effect and never calls charge.
- [ ] Missing/mismatched evidence opens or retains an Incident without local financial mutation.
- [ ] Repeated reconciliation is idempotent and resolves the matching Incident after successful finalization.
Quality:
- [ ] Transaction-observing lookup fake and SUBSCRIBE/UPGRADE/RENEWAL recovery tests pass.
- [ ] Java compile and `git diff --check` pass.

[INPUT POINTERS]
Tier 0:
- docs/standards/core-principles.md
- docs/standards/development-standards.md
Tier 1:
- docs/policies/security-policy.md
- docs/policies/quality-gates.md
Context:
- deliverables/user/REQ-20260714-ATS-001.md
- docs/design/p1-payment-integrity-remediation-design.md
- deliverables/agent/WI-20260714-ATS-036-evidence-pack.md
- deliverables/agent/WI-20260715-ATS-002-evidence-pack.md
Files:
- src/main/java/com/atstudio/atstudio/service/PaymentReconciliationService.java
- src/main/java/com/atstudio/atstudio/service/PaymentReconciliationTransactionService.java
- src/main/java/com/atstudio/atstudio/service/PaymentReconciliationIncidentService.java
- src/main/java/com/atstudio/atstudio/service/payment/provider/recurring/ProviderPaymentLookupResult.java
- src/main/java/com/atstudio/atstudio/service/payment/provider/recurring/PaymentStatusLookupProvider.java
- src/main/java/com/atstudio/atstudio/service/payment/provider/recurring/TossBillingProvider.java
- src/test/java/com/atstudio/atstudio/service/PaymentReconciliationServiceTest.java
- src/test/java/com/atstudio/atstudio/service/PaymentReconciliationIncidentServiceTest.java
- src/test/java/com/atstudio/atstudio/service/payment/provider/recurring/TossBillingProviderTest.java
- new focused finalize-only integration tests if needed

[OUTPUT CONTRACT]
User-facing -> deliverables/user/WI-20260715-ATS-006-summary.md
Agent-facing -> deliverables/agent/WI-20260715-ATS-006-evidence-pack.md
Handoff -> deliverables/agent/WI-20260715-ATS-006-handoff.md

[TRACEABILITY REQUIREMENTS]
Record evidence-gate matrix, no-charge assertion, transaction observation, all three purpose recoveries, Incident behavior, tests, rollback, and residual provider-lookup limits.
