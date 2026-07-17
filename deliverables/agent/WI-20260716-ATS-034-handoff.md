[WI HEADER]
WI ID: WI-20260716-ATS-034
REQ: REQ-20260716-ATS-004
Agent: cr
Depends On: -
Blocks: WI-20260716-ATS-038

[WI SUMMARY]
Why: Identify backend legacy, compatibility, fallback, demo/acceptance bypass, backup, and dead-code candidates before V1 consolidation.
Scope (in/out): Read-only inspection of `src/main/java`, `src/main/resources`, backend configuration, backend tests, API consumers, and relevant design/security documents. Do not edit product code, configuration, SQL, Git state, DB state, branches, or worktrees. Only the required WI summary and Evidence Pack may be created.
DoD: Produce a symbol/path-level inventory classified as KEEP/REMOVE/REPLACE/ARCHIVE/REVIEW with call sites, runtime/profile reachability, safety role, deletion impact, and verification method. Explicitly distinguish operational integrity controls from obsolete compatibility code.
Constraints/Forbidden: No deletion, refactor, formatting, dependency change, test mutation, DB access mutation, branch operation, or speculative fix. Do not infer dead code from names alone.

[ACCEPTANCE CRITERIA]
Functional:
- [ ] Spring endpoints, services, DTO aliases, properties, providers, bootstraps, legacy SSR paths, fallbacks, and backup-like files are searched.
- [ ] Each removal candidate has reference and reachability evidence.
- [ ] Payment idempotency, audit, reconciliation, locking, lease, state-transition, and optimistic-lock controls receive an explicit KEEP/REVIEW assessment.
Performance:
- [ ] Static inspection avoids starting or mutating shared runtime services.
Quality:
- [ ] Findings include severity, confidence, false-positive risks, and negative-search commands.
- [ ] No product file or Git/DB state changes occur.

[INPUT POINTERS]
Tier 0 (Constitution - Required for all):
- docs/standards/core-principles.md
- docs/standards/development-standards.md

Tier 1 (Policies - Inferred):
- docs/policies/security-policy.md
- docs/policies/versioning-policy.md
- docs/policies/quality-gates.md

Tier 2 (Task Context):
- docs/design/api-spec.md
- docs/design/db-schema.md
- docs/design/remaining-remediation-design-20260716.md
- docs/payment/system-overview.md

REQ/Context Docs:
- deliverables/user/REQ-20260716-ATS-004.md
- AGENTS.md

Files:
- src/main/java/
- src/main/resources/
- src/test/java/
- build.gradle

Repro/Logs:
- `rg`/`git grep`/dependency and reference inspection commands only

[OUTPUT CONTRACT]
User-facing -> deliverables/user/WI-20260716-ATS-034-summary.md:
- Korean summary, top risks, candidate counts, approval-sensitive items
Agent-facing -> deliverables/agent/WI-20260716-ATS-034-evidence-pack.md:
- Full inventory, evidence pointers, commands, disposition rationale, rollback/verification notes
Handoff Packet -> deliverables/agent/WI-20260716-ATS-034-handoff.md:
- This packet

[TRACEABILITY REQUIREMENTS]
Evidence pointers (files/lines/commands/logs): Required
Tests: Static/read-only commands and before/after Git status comparison
Rollback: No product mutation; list any deliverable files created
