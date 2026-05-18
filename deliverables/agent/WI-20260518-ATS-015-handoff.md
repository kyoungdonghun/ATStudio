[WI HEADER]
WI ID: WI-20260518-ATS-015
REQ: REQ-20260518-ATS-001
Agent: pg
Depends On: -
Blocks: WI-20260518-ATS-017

[WI SUMMARY]
Why: Review payment UX and operations design for sensitive-data exposure risks before the design is finalized.
Scope (in/out): In scope: authKey, customerKey, billingKey, secret key, raw Toss payload, failure details, logs, admin visibility, and user-facing error copy. Out of scope: new encryption implementation or live credential rotation.
DoD: Security constraints and safe display/logging policy are available for docs.
Constraints/Forbidden: Do not recommend showing raw PG identifiers to ordinary users. Billing key must remain server-only and encrypted at rest.

[ACCEPTANCE CRITERIA]
Functional:
- [ ] User-facing error messages are safe and non-sensitive.
- [ ] Operator-facing diagnostics expose only necessary identifiers and sanitized reasons.
- [ ] Raw billingKey and secret key are never exposed in UI, logs, docs examples, or API responses.
Performance:
- [ ] No runtime performance requirement; this is design-only.
Quality:
- [ ] Output can be reflected into payment design and API docs.

[INPUT POINTERS]
Tier 0 (Constitution - Required for all):
- docs/standards/core-principles.md

Tier 1 (Security):
- docs/policies/security-policy.md

Tier 2 / Context:
- docs/design/payment-integration-design.md
- docs/design/api-spec.md
- deliverables/user/REQ-20260518-ATS-001.md
- deliverables/user/REQ-20260517-ATS-002.md

Files:
- src/main/java/com/atstudio/atstudio/service/payment/billing/BillingKeyCrypto.java
- src/main/java/com/atstudio/atstudio/service/payment/provider/recurring/TossBillingProvider.java
- src/main/java/com/atstudio/atstudio/service/BillingAgreementApplicationService.java
- frontend/src/pages/subscriber/SubscriptionPaymentPage.tsx

[OUTPUT CONTRACT]
User-facing -> deliverables/user/WI-20260518-ATS-015-summary.md :
- Security-facing UX constraints and approved diagnostic boundaries.
Agent-facing -> deliverables/agent/WI-20260518-ATS-015-evidence-pack.md :
- Sensitive-data map, safe/unsafe display policy, and risks.
Handoff Packet -> deliverables/agent/WI-20260518-ATS-015-handoff.md :
- This packet.

[TRACEABILITY REQUIREMENTS]
Evidence pointers (files/lines/commands/logs): Required.
Tests: Design-only; note validation commands if run.
Rollback (if needed): Revert docs tied to this WI.
