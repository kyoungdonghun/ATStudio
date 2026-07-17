[WI HEADER]
WI ID: WI-20260717-ATS-002
REQ: REQ-20260716-ATS-004
Agent: se
Depends On: WI-20260717-ATS-001
Blocks: WI-20260717-ATS-004, WI-20260717-ATS-005, WI-20260717-ATS-006

[WI SUMMARY]
Why: Remove approved backend residual code while preserving all current V1 safety invariants and recurring-payment behavior.
Scope (in/out): Remove backend portions of INT-R01, R02, R03, R04, R05, R07 and resolved INT-V01 through V05. This includes obsolete server play history, obsolete download queue, deprecated upgrade overload, preview field consumers, whitelist legacy snapshots, stale security matcher, legacy one-time payment/callback/direct-subscription paths, and dormant utility/admin detail endpoints with their tests. Out of scope: schema.sql/manual SQL, application/provider configuration and provider enum normalization (WI-004), frontend files (WI-003), active documentation (WI-005), QA bootstrap, emergency admin subscription update/cancel, recurring checkout/renewal/upgrade/refund/reconciliation/audit/locks/leases/fences/state machines.
DoD: Approved backend targets and tests are removed or updated; exact negative searches pass; backend compile and focused tests pass; no protected KEEP invariant is weakened.
Constraints/Forbidden: Do not edit frontend/, docs/, deliverables other than WI-002 outputs, schema.sql, db/manual/, application*.yml, application-local.yml, provider enum/config selection, Git refs, runtime processes, or secrets. Do not remove recurring payment, refund, reconciliation, audit, locking, lease/fence, recovery, acceptance guard, QA bootstrap, or emergency admin operations. Do not revert unrelated work.

[ACCEPTANCE CRITERIA]
Functional:
- [ ] Remove backend server play-history contract while preserving browser-local history and public full-track playback.
- [ ] Remove backend download-queue contract while preserving download authorization, accounting, licensing, and download history.
- [ ] Remove approved legacy one-time payment/callback and direct subscription-creation paths while preserving recurring checkout and blocked unauthorized mutation behavior where still applicable.
- [ ] Remove dormant subscription-status, user-type, and admin subscription detail endpoints.
- [ ] Remove preview and whitelist snapshot Java consumers without touching schema ownership.
- [ ] Preserve direct admin subscription update/cancel emergency operations.
Performance:
- [ ] No material regression in the remaining request paths.
Quality:
- [ ] Java compilation passes.
- [ ] Focused payment, subscription, playback, download, whitelist, security, and storage tests pass.
- [ ] Exact negative reference searches for removed Java symbols/routes pass.

[INPUT POINTERS]
Tier 0:
- docs/standards/core-principles.md
- docs/standards/development-standards.md

Tier 1:
- docs/policies/security-policy.md
- docs/policies/quality-gates.md

Tier 2:
- docs/design/api-spec.md
- docs/design/payment-integration-design.md
- docs/design/db-schema.md

REQ / Decision Sources:
- deliverables/user/REQ-20260716-ATS-004.md
- deliverables/agent/WI-20260717-ATS-001-evidence-pack.md
- deliverables/agent/WI-20260716-ATS-038-evidence-pack.md
- deliverables/agent/WI-20260716-ATS-034-evidence-pack.md

Files:
- src/main/java/com/atstudio/atstudio/
- src/test/java/com/atstudio/atstudio/

[OUTPUT CONTRACT]
User-facing -> deliverables/user/WI-20260717-ATS-002-summary.md
Agent-facing -> deliverables/agent/WI-20260717-ATS-002-evidence-pack.md
Handoff Packet -> deliverables/agent/WI-20260717-ATS-002-handoff.md

[TRACEABILITY REQUIREMENTS]
Map every edit to an INT ID. Record exact removed symbols/routes, protected safeguards reviewed, commands, results, residual references, risks, and rollback. Use create-wi-evidence-pack after implementation.
