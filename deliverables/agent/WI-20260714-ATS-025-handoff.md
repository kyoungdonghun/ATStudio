[WI HEADER]
WI ID: WI-20260714-ATS-025
REQ: REQ-20260714-ATS-001
Agent: qa-integ
Depends On: WI-20260714-ATS-018, WI-20260714-ATS-019, WI-20260714-ATS-020, WI-20260714-ATS-021, WI-20260714-ATS-022, WI-20260714-ATS-023, WI-20260714-ATS-024, WI-20260714-ATS-035
Blocks: WI-20260714-ATS-026, WI-20260714-ATS-027, WI-20260714-ATS-034

[WI SUMMARY]
Why: Reconcile approved design, backend, frontend, schema, operational scripts, and Phase 4/5 evidence before documentation is declared current.
Scope: Three-way contract matrix, API/DTO/route/schema/config alignment, acceptance topology, deferred legacy/operator work, and closure status for every P1 audit item.
Out: New feature implementation, live client handoff, broad refactor, or undocumented assumptions.
DoD: Every P1 audit ID maps to approved design, current code, passing evidence, documentation impact, and a clear closed/deferred/blocked status.
Constraints: Do not paper over contradictions. Confirm with code or evidence, and mark unsupported claims as gaps.

[ACCEPTANCE CRITERIA]
- [ ] Payment/API/entity/schema states and counts align.
- [ ] File/storage/auth/frontend/proxy contracts align across layers.
- [ ] Runtime and disposable DB evidence are separated from untested operating assumptions.
- [ ] Legacy migration, live-provider, and client-sharing gaps are explicit.
- [ ] Trace matrix is ready for docops without unresolved critical/high contradictions.

[INPUT POINTERS]
Tier 0:
- docs/standards/core-principles.md
- docs/standards/development-standards.md
Tier 1:
- docs/policies/quality-gates.md
Context:
- deliverables/user/REQ-20260714-ATS-001.md
- docs/audit/p1-remediation-trace-matrix-20260714.md
- docs/design/p1-payment-db-integrity-design.md
- docs/design/p1-security-acceptance-hardening-design.md
- deliverables/agent/WI-20260714-ATS-018-evidence-pack.md
- deliverables/agent/WI-20260714-ATS-019-evidence-pack.md
- deliverables/agent/WI-20260714-ATS-020-evidence-pack.md
- deliverables/agent/WI-20260714-ATS-021-evidence-pack.md
- deliverables/agent/WI-20260714-ATS-022-evidence-pack.md
- deliverables/agent/WI-20260714-ATS-023-evidence-pack.md
- deliverables/agent/WI-20260714-ATS-024-evidence-pack.md
- deliverables/agent/WI-20260714-ATS-035-evidence-pack.md
Files:
- docs/design/api-spec.md
- src/main/resources/schema.sql
- backend controllers/dtos/entities/services/config
- frontend routes/api/types/pages
- scripts/acceptance/

[OUTPUT CONTRACT]
User-facing -> deliverables/user/WI-20260714-ATS-025-summary.md (Korean)
Agent-facing -> deliverables/agent/WI-20260714-ATS-025-evidence-pack.md
Handoff -> deliverables/agent/WI-20260714-ATS-025-handoff.md

[TRACEABILITY REQUIREMENTS]
Three-way matrix, exact code/doc/evidence pointers, contradictory-claim list, closure status, rollback, and docops change list are required.
