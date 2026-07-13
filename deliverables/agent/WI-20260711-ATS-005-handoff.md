[WI HEADER]
WI ID: WI-20260711-ATS-005
REQ: REQ-20260711-ATS-001
Agent: qa-integ
Depends On: -
Blocks: WI-20260711-ATS-006, WI-20260711-ATS-007, WI-20260711-ATS-008

[WI SUMMARY]
Why: Audit integration boundaries across REST contracts, JPA entities, SQL schemas/manual migrations, provider adapters, schedulers, and deployment configuration.
Scope (in/out): Inspect API request/response contracts, schema alignment, migrations, indexes/constraints, environment-variable slots, external provider interfaces, schedulers, reconciliation, imports/exports, and cross-layer failure handling. Do not alter DBs or call live providers.
DoD: Produce an API/DB/operations inventory and evidence-backed contract, migration, consistency, recovery, and deployment findings.
Constraints/Forbidden: Read-only except WI outputs. Do not reveal configured secret values. Do not run SQL or provider mutations.

[ACCEPTANCE CRITERIA]
Functional:
- [ ] Compare JPA entities with `schema.sql` and every relevant manual patch.
- [ ] Check DTO/controller/frontend API shape consistency for high-risk endpoints.
- [ ] Inspect scheduler/reconciliation/import/export idempotency and recovery assumptions.
- [ ] Identify missing constraints, indexes, migrations, callbacks, or deployment prerequisites.
Performance:
- [ ] Flag unbounded scans, missing high-value indexes, oversized payloads, and scheduler contention where evidenced.
Quality:
- [ ] Every finding includes exact files/lines and operational consequence.
- [ ] Separate local-only, production-only, and universal risks.

[INPUT POINTERS]
Tier 0 (Constitution - Required for all):
- docs/standards/core-principles.md
- docs/standards/development-standards.md

Tier 1 (Policies - Inferred):
- docs/policies/security-policy.md
- docs/policies/quality-gates.md

Tier 2 (Integration Context):
- docs/design/api-spec.md
- docs/payment/
- docs/guides/
- docs/registry/

REQ/Context Docs:
- deliverables/user/REQ-20260711-ATS-001.md

Files:
- src/main/java/com/atstudio/atstudio/
- src/main/resources/schema.sql
- src/main/resources/db/manual/
- src/main/resources/application.yml
- frontend/src/api/

Repro/Logs:
- rg --files src/main/resources/db src/main/java frontend/src/api

[OUTPUT CONTRACT]
User-facing -> deliverables/user/WI-20260711-ATS-005-summary.md : concise Korean findings and risks
Agent-facing -> deliverables/agent/WI-20260711-ATS-005-evidence-pack.md : API/schema/operations map, evidence, severity, test gaps, follow-up inputs
Handoff Packet -> deliverables/agent/WI-20260711-ATS-005-handoff.md : this packet

[TRACEABILITY REQUIREMENTS]
Evidence pointers: Required, with narrow file/line references
Tests: Static inspection now; list non-destructive verification commands for later
Rollback: Only remove this WI's newly created summary/evidence files if explicitly requested
