[WI HEADER]
WI ID: WI-20260717-ATS-017
REQ: REQ-20260716-ATS-004
Agent: docops
Depends On: WI-20260717-ATS-016 and final quality metrics supplied by MA
Blocks: Unified-branch acceptance testing

[WI SUMMARY]
Why: Close current-state documentation after V1 code/schema consolidation and exact local database restoration.
Scope (in/out): In scope are active current-state indexes, SRs, payment runbooks/inventory, client internal feature maps, branch/runtime wording, quality evidence, and references to removed migration/API paths. Historical REQ/WI evidence and clearly archived design records remain historical. Out of scope are source code, tests, database, local config, Git refs/index, generated client PDFs, runtime startup, remote operations, and push.
DoD: No active document instructs operators to use deleted SQL, removed legacy payment endpoints, or a deleted client-demo branch; V1 is described as the official baseline rather than a candidate; document counts and final quality metrics are current; SR-93 remains open only for genuine production-readiness gates; docs validation and diff checks pass.
Constraints/Forbidden: Do not rewrite historical REQ/WI evidence as if it were current-state documentation. Do not revive SR-92. Do not mark SR-93 closed. Do not invent production approval, live Toss validation, retained-data migration, client acceptance, or deployment evidence. Do not edit code, DB, local config, generated PDFs, Git refs/index, or remote state. You are not alone in the codebase; do not revert other changes.

[ACCEPTANCE CRITERIA]
Functional:
- [ ] Remove active instructions for deleted manual SQL files.
- [ ] Replace descriptions of removed legacy APIs with the current absence contract.
- [ ] Remove current-state claims that `codex/client-demo-stable` still exists.
- [ ] Replace `official V1 branch candidate` with the official V1 baseline wording.
- [ ] Update Standards and total Markdown counts in `docs/index.md`.
- [ ] Update stale quality metrics with the final verified results supplied by MA.
- [ ] Keep true production-readiness items open in SR-93.
Performance:
- [ ] Not applicable.
Quality:
- [ ] Documentation validation passes.
- [ ] Internal links and traceability remain valid.
- [ ] `git diff --check` passes.

[INPUT POINTERS]
Tier 0 (Constitution - Required for all):
- docs/standards/core-principles.md

Tier 0 (Standards - Based on Assignee):
- docs/standards/documentation-standards.md
- docs/standards/glossary.md

Tier 1 (Policies - Inferred from REQ/WI content):
- docs/policies/versioning-policy.md
- docs/policies/quality-gates.md
- docs/policies/execution-policy.md

REQ/Context Docs:
- deliverables/user/REQ-20260716-ATS-004.md
- deliverables/agent/WI-20260717-ATS-015-evidence-pack.md
- deliverables/agent/WI-20260717-ATS-016-evidence-pack.md
- docs/design/api-spec.md
- docs/design/db-schema.md
- docs/payment/known-limits-and-next-steps.md

Files:
- docs/index.md
- docs/SR/SR-42.md
- docs/SR/SR-92.md
- docs/SR/SR-93.md
- docs/design/payment-operations-runbook.md
- docs/client/_internal-feature-map.md
- docs/client/testing-guide.md
- docs/payment/
- docs/registry/project-registry.md

Repro/Logs:
- Official branch: `codex/p1-acceptance-hardening` at the current working HEAD plus approved uncommitted closeout changes.
- Exact DB baseline: 39 tables, 449 columns, 153 indexes, 80 FKs, manifest `c48d3c75378aaf2364d89ed06833ba68e27a5a334dbc4670d1443bd938c6c506`.
- Active inventory: 137 APIs, 39 tables/entities, 53 screens.
- Final backend/frontend quality metrics: supplied by MA after the full gate run; do not retain older numbers.

[OUTPUT CONTRACT]
User-facing -> return a concise document closeout and validation summary to MA; MA will standardize it.
Agent-facing -> return exact changed docs, decisions, validation commands, and remaining production gates; MA will generate the Evidence Pack.
Handoff Packet -> deliverables/agent/WI-20260717-ATS-017-handoff.md :
- This packet.

[TRACEABILITY REQUIREMENTS]
Evidence pointers: Exact document paths and lines required
Tests: Report documentation validation and diff-check commands
Rollback: Revert only current-state documentation changes; preserve historical evidence
