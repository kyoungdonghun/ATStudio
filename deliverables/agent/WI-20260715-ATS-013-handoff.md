[WI HEADER]
WI ID: WI-20260715-ATS-013
REQ: REQ-20260714-ATS-001
Agent: docops
Depends On: WI-20260715-ATS-012
Blocks: WI-20260715-ATS-014 final quality gate

[WI SUMMARY]
Why: Align current payment-integrity, production-readiness, audit, and client-acceptance documentation with the implemented and independently reviewed state without rewriting historical evidence.
Scope (in): Create one current payment-integrity closure report; update the active remediation design and superseded DB-integrity design status; append current closure evidence to the P1 trace matrix; align current payment overview, operations, limits, SR-93, and acceptance/client checklists only where verified by WI-007 through WI-012.
Scope (out): Product code, tests, schema, provider/live configuration, production-readiness closure, unrelated P1 findings, PDF regeneration, historical WI evidence edits, and speculative future features.
DoD: Current documents distinguish closed payment-integrity findings F-01 through F-05 from still-open live deployment, retained-DB, client-acceptance, and non-payment gates; historical records remain intact; client checks are easy Korean instructions; all changed documents validate and cross-link to reproducible evidence.
Constraints/Forbidden: Do not erase or silently rewrite historical FAIL/baseline evidence. Do not mark SR-93 or overall production readiness closed. Do not claim live Toss, retained production DB, production deployment, or client acceptance was verified. Do not expose credentials, payment keys, billing keys, card data, secrets, or runtime URLs. Do not edit runtime logs or implementation. You are not alone in this repository; preserve concurrent/user changes and do not revert them.

[ACCEPTANCE CRITERIA]
Functional:
- [ ] Add a dated closure report mapping F-01 through F-05 and WI-009/WI-010 follow-up findings to exact WI, commit, test, and MySQL proof evidence.
- [ ] Mark `p1-payment-integrity-remediation-design.md` implemented/current and map Packages A-G plus corrections to WI-001 through WI-012.
- [ ] Mark `p1-payment-db-integrity-design.md` historical/superseded while preserving its original baseline and migration cautions.
- [ ] Preserve the P1 trace matrix baseline and append an explicit current closure section for payment rows `ATS020-P1-05` through `ATS020-P1-10`.
- [ ] Update active payment documentation and SR-93 with stable command identity, strict provider transaction boundaries, retry-gate consumption, refund lease fencing, finalize-only reconciliation, sensitive payment-key minimization, and disposable MySQL 7/7 proof.
- [ ] Update acceptance/client checklists only with user/operator-observable checks and clear Korean wording; implementation-only checks must point to agent evidence instead of asking the client to inspect internals.
- [ ] Keep remaining production readiness gates open and accurately named.
Quality:
- [ ] `validate-docs` passes.
- [ ] Documentation index is synchronized if file counts or links change.
- [ ] `git diff --check` passes.
- [ ] User summary and agent evidence pack list every changed document and the verification commands/results.

[INPUT POINTERS]
Tier 0 (Constitution - Required for all):
- docs/standards/core-principles.md

Tier 0 (Standards - docops):
- docs/standards/documentation-standards.md
- docs/standards/glossary.md

Tier 1 (Policies):
- docs/policies/quality-gates.md
- docs/policies/archive-policy.md

REQ/Context Docs:
- deliverables/user/REQ-20260714-ATS-001.md
- docs/design/p1-payment-integrity-remediation-design.md
- docs/design/p1-payment-db-integrity-design.md
- docs/audit/p1-remediation-trace-matrix-20260714.md
- docs/SR/SR-93.md
- docs/payment/index.md
- docs/payment/system-overview.md
- docs/payment/feature-inventory.md
- docs/payment/admin-operations-guide.md
- docs/payment/known-limits-and-next-steps.md
- docs/payment/acceptance-test-checklist.md
- docs/client/index.md
- docs/client/1-quick-checklist.md
- docs/client/2-full-feature-checklist.md
- docs/client/3-admin-checklist.md

Evidence:
- deliverables/agent/WI-20260715-ATS-007-evidence-pack.md
- deliverables/agent/WI-20260715-ATS-008-evidence-pack.md
- deliverables/agent/WI-20260715-ATS-009-evidence-pack.md
- deliverables/agent/WI-20260715-ATS-010-evidence-pack.md
- deliverables/agent/WI-20260715-ATS-011-evidence-pack.md
- deliverables/agent/WI-20260715-ATS-012-evidence-pack.md
- commits `1ecfe5c`, `830c8dd`, `3f18fed`, `46edd88`, `14053e6`

[OUTPUT CONTRACT]
User-facing -> deliverables/user/WI-20260715-ATS-013-summary.md
Agent-facing -> deliverables/agent/WI-20260715-ATS-013-evidence-pack.md
Handoff -> deliverables/agent/WI-20260715-ATS-013-handoff.md
Current closure report -> docs/audit/p1-payment-integrity-closure-20260715.md

[TRACEABILITY REQUIREMENTS]
For each current-state claim, cite the exact WI evidence, commit, test class/command, or MySQL proof artifact. Separate historical baseline, current code closure, and remaining deployment/acceptance risk. Record changed file paths, validation commands/results, rollback guidance, and any intentionally unchanged document with the reason.
