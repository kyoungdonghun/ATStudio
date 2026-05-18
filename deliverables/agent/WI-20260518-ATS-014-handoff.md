[WI HEADER]
WI ID: WI-20260518-ATS-014
REQ: REQ-20260518-ATS-001
Agent: sa
Depends On: -
Blocks: WI-20260518-ATS-017

[WI SUMMARY]
Why: Define the minimum operator-facing payment and subscription state requirements needed to support payment troubleshooting and customer support.
Scope (in/out): In scope: admin visibility requirements for payment orders, subscription payment records, billing agreements, renewal failures, grace windows, and cancellation status. Out of scope: implementation, refund workflow, accounting settlement, webhook ingestion.
DoD: Operator state model and API/UI candidate list are ready for design documentation.
Constraints/Forbidden: Do not introduce live secret handling or implementation changes. Keep operations scope minimal and support-oriented.

[ACCEPTANCE CRITERIA]
Functional:
- [ ] Operator can identify a user's latest payment attempt and billing agreement state.
- [ ] Renewal failure, grace period, suspended billing, and cancelled auto-renewal are defined for support use.
- [ ] Minimum API/UI candidate list is separated from deferred finance operations.
Performance:
- [ ] No runtime performance requirement; this is design-only.
Quality:
- [ ] Output can be reflected into `docs/design/api-spec.md` and `docs/design/db-schema.md`.

[INPUT POINTERS]
Tier 0 (Constitution - Required for all):
- docs/standards/core-principles.md
- docs/standards/development-standards.md

Tier 1 (Architecture / Policies):
- docs/architecture/system-design.md
- docs/policies/quality-gates.md

Tier 2 / Context:
- docs/design/payment-integration-design.md
- docs/design/api-spec.md
- docs/design/db-schema.md
- docs/ui/screen-flow.md
- deliverables/user/REQ-20260518-ATS-001.md
- deliverables/user/REQ-20260517-ATS-002.md

Files:
- src/main/java/com/atstudio/atstudio/entity/PaymentOrder.java
- src/main/java/com/atstudio/atstudio/entity/SubscriptionPayment.java
- src/main/java/com/atstudio/atstudio/entity/BillingAgreement.java
- src/main/java/com/atstudio/atstudio/service/RecurringRenewalService.java

[OUTPUT CONTRACT]
User-facing -> deliverables/user/WI-20260518-ATS-014-summary.md :
- Operator requirements summary and deferred operations list.
Agent-facing -> deliverables/agent/WI-20260518-ATS-014-evidence-pack.md :
- State model, API/UI candidates, source pointers, and risks.
Handoff Packet -> deliverables/agent/WI-20260518-ATS-014-handoff.md :
- This packet.

[TRACEABILITY REQUIREMENTS]
Evidence pointers (files/lines/commands/logs): Required.
Tests: Design-only; note validation commands if run.
Rollback (if needed): Revert docs tied to this WI.
