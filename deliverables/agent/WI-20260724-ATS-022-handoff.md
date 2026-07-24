[WI HEADER]
WI ID: WI-20260724-ATS-022
REQ: REQ-20260724-ATS-002
Agent: docops
Depends On: WI-20260724-ATS-021
Blocks: WI-20260724-ATS-020

[WI SUMMARY]
Why: Three current-state documents still claim zero npm advisories, but the 2026-07-24 registry now reports two moderate React Router findings and WI-021 established their current reachability and production-readiness boundary.
Scope (in/out): Correct only current-state dependency-audit claims in `docs/SR/SR-42.md`, `docs/SR/SR-93.md`, and `docs/design/remaining-remediation-design-20260716.md`; link the WI-021 evidence where appropriate. Preserve dated historical evidence and do not rewrite old WI reports.
DoD: Current docs state the locked versions, current advisory count/severity, current non-reachability assessment, non-production rehearsal disposition, and separate approval requirement for a controlled React Router 7 migration.
Constraints/Forbidden: Do not upgrade dependencies, change product code, reinterpret primary advisory details, or bulk-rewrite historical documents.

[ACCEPTANCE CRITERIA]
Functional:
- [ ] All three stale zero-advisory current-state claims are corrected.
- [ ] The docs distinguish current call-path reachability from package vulnerability status.
- [ ] Public acceptance remains conditional and production readiness remains open.
Performance:
- [ ] Not applicable.
Quality:
- [ ] Internal links and traceability validate.
- [ ] `git diff --check` passes.
- [ ] Search finds no remaining zero-advisory current-state claim in active docs.

[INPUT POINTERS]
Tier 0 (Constitution - Required for all):
- docs/standards/core-principles.md
- docs/standards/documentation-standards.md
- docs/standards/glossary.md
Tier 1 (Policies - Inferred):
- docs/policies/security-policy.md
- docs/policies/quality-gates.md
Tier 2 (Task Context):
- docs/standards/evidence-pack-standard.md
REQ/Context Docs:
- deliverables/user/REQ-20260724-ATS-002.md
- deliverables/user/WI-20260724-ATS-021-summary.md
- deliverables/agent/WI-20260724-ATS-021-evidence-pack.md
Files:
- docs/SR/SR-42.md
- docs/SR/SR-93.md
- docs/design/remaining-remediation-design-20260716.md

[OUTPUT CONTRACT]
User-facing -> deliverables/user/WI-20260724-ATS-022-summary.md :
- Corrected current-state claims and remaining production decision
Agent-facing -> deliverables/agent/WI-20260724-ATS-022-evidence-pack.md :
- Exact lines, source evidence, validation commands, rollback
Handoff Packet -> deliverables/agent/WI-20260724-ATS-022-handoff.md :
- This packet

[TRACEABILITY REQUIREMENTS]
Evidence pointers (files/lines/commands/logs): Required
Tests: Validate docs, stale-claim search, and diff check
Rollback: Revert only the three current-state wording updates
