---
version: 1.0
last_updated: 2026-09-09
project: ATS
owner: MA
category: reference
status: active
---

# WI-20260909-ATS-002: Payment Lifecycle and Competing Monetary Intent Fences

[WI HEADER]
WI ID: WI-20260909-ATS-002
REQ: REQ-20260909-ATS-001
Agent: se
Depends On: none
Blocks: 011, 006, 013, 014

[WI SUMMARY]
Scope: PAY-01/02. Fix legitimate expired-history re-subscription finalization/recovery without removing guards for active/replaced states. Fence all unresolved competing monetary upgrades on the same subscription/period, including different next cycles and targets, and verify priced source on finalize. Add deterministic fake-Provider tests for return purchase, durable success recovery, distinct-transaction concurrent/UNKNOWN upgrades and ordinary replay.
Write ownership: PaymentCommandTransactionService.java, BillingAgreementPrepareTransactionService.java, PaymentCommandKeyFactory.java, UserSubscriptionService.java, PaymentOrderRepository.java, BillingAgreementApplicationService.java and existing PaymentOrder fields only if strictly needed; corresponding/new payment service integration tests. No DB/schema changes, no auth/frontend/storage/build files. Use src/main/java/com/atstudio/atstudio/service and repository existing paths.
Constraints: No financial provider calls, keys, real DB actions. Existing current-paid-period upgrade and future billing-cycle policy must stay unchanged. Do not solve by deleting history or turning all re-subscriptions into registration-only. Investigate existing order command key/snapshot fields before inventing new persisted columns. If no safe existing-fields solution, report the precise architectural/DDL boundary instead of silently weakening guards.
Forbidden: touching unrelated changes, DDL/real DB/media deletion or repair, credential inspection, external payments/refunds/mail, runtime restart, Git writes, subdelegation. Product edits via apply_patch only; existing fake-provider/H2/temp-file tests only. MA serializes Gradle/npm runs; send requested command/test list, do not start heavy runners independently.

[ACCEPTANCE CRITERIA]
- [ ] Concrete regression scenarios fail for the original defect (when executed) and pass after the minimal fix; distinguish unrun from passed.
- [ ] Existing policy/authorization/idempotency/strict integrity guards retained; no schema or broad abstraction churn.
- [ ] Shared-file boundaries respected; list exact changed paths and test candidates/risks.
- [ ] Evidence pack and user summary created; hand back to downstream WI rather than declare production GO.

[INPUT POINTERS]
Tier 0: docs/standards/core-principles.md; docs/standards/documentation-standards.md; docs/standards/development-standards.md; docs/standards/glossary.md.
Tier 1: docs/policies/security-policy.md; docs/policies/quality-gates.md.
Tier 2: deliverables/user/REQ-20260909-ATS-001.md; deliverables/agent/WI-20260908-ATS-018-findings.md; WI-20260908-ATS-015/016/017-evidence-pack.md (only relevant scope); docs/design/api-spec.md; current related usecase/design docs. Frontend work: .agents/skills/react-best-practices/SKILL.md.
Snapshot: main 8161f0a; prior SR-93/audit documents already dirty/untracked and protected. No secrets/data reads needed.

[OUTPUT CONTRACT]
Implement directly in the shared workspace, only owned paths. User-facing: deliverables/user/WI-20260909-ATS-002-summary.md.
Agent-facing: deliverables/agent/WI-20260909-ATS-002-evidence-pack.md using create-wi-evidence-pack.
Report exact files, root cause, tests run/not run, deployment impact and remaining dependency. Do not edit this handoff or other agents' reports.

[TRACEABILITY REQUIREMENTS]
MA review return (2026-09-09): WI011 identified an unlocked cancellation writer that can restore the old subscription fields after paid upgrade finalization. UserSubscriptionService was already in the owned boundary. Add deterministic independent-transaction reproduction and align related cancel/reactivate/pending-plan writers with the existing agreement/subscription lock order. No changed cancellation policy, DDL or external action. WI011 must re-review after this correction; initial payment test success does not close that counterexample.

Every relevant code/test change maps to a finding ID. Source pointers and reproducible safe tests required. No test weakening, no historical-data cleanup. English technical reports; Korean concise completion message.
