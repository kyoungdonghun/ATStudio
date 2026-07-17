[WI HEADER]
WI ID: WI-20260716-ATS-035
REQ: REQ-20260716-ATS-004
Agent: qa-fe
Depends On: -
Blocks: WI-20260716-ATS-038

[WI SUMMARY]
Why: Identify frontend legacy routes, API wrappers, response-compatibility branches, dead components, fallback UI, demo/acceptance bypass, and backup artifacts before V1 consolidation.
Scope (in/out): Read-only inspection of `frontend/`, matching API specifications and UI documents. Do not edit frontend/product code, dependencies, generated files, Git state, DB state, branches, or worktrees. Only the required WI summary and Evidence Pack may be created.
DoD: Produce a path/symbol-level KEEP/REMOVE/REPLACE/ARCHIVE/REVIEW inventory with import/call evidence, route reachability, backend-contract dependency, user-visible impact, and verification method.
Constraints/Forbidden: Do not run formatters that write files. Preserve `frontend/tsconfig.tsbuildinfo`. Do not classify defensive error handling as obsolete without proving the represented failure is impossible.

[ACCEPTANCE CRITERIA]
Functional:
- [ ] Routes, API clients, Zustand state, components, tests, feature/config branches, legacy response adapters, debug/demo paths, and backup-like files are searched.
- [ ] Each candidate has import/reference and runtime reachability evidence.
- [ ] Public listening, authentication, subscription, payment, whitelist, and company-certification flows are checked for compatibility dependencies.
Performance:
- [ ] Inspection does not start or modify public/client runtime services.
Quality:
- [ ] Findings include confidence, false-positive risk, and negative-search commands.
- [ ] No product file or generated-file changes occur.

[INPUT POINTERS]
Tier 0 (Constitution - Required for all):
- docs/standards/core-principles.md
- docs/standards/development-standards.md

Tier 1 (Policies - Inferred):
- docs/policies/quality-gates.md
- docs/policies/versioning-policy.md

Tier 2 (Tech Stack / Task Context):
- docs/standards/frontend-standards.md
- .agents/skills/react-best-practices/AGENTS.md
- docs/design/api-spec.md
- docs/ui/screen-flow.md
- docs/ui/atstudio-front-list.md

REQ/Context Docs:
- deliverables/user/REQ-20260716-ATS-004.md
- AGENTS.md

Files:
- frontend/src/
- frontend/package.json
- frontend/vite.config.ts
- frontend/index.html

Repro/Logs:
- `rg`/TypeScript import graph/route and API contract inspection; read-only commands only

[OUTPUT CONTRACT]
User-facing -> deliverables/user/WI-20260716-ATS-035-summary.md:
- Korean summary, top risks, candidate counts, approval-sensitive items
Agent-facing -> deliverables/agent/WI-20260716-ATS-035-evidence-pack.md:
- Full inventory, evidence pointers, commands, disposition rationale, verification notes
Handoff Packet -> deliverables/agent/WI-20260716-ATS-035-handoff.md:
- This packet

[TRACEABILITY REQUIREMENTS]
Evidence pointers (files/lines/commands/logs): Required
Tests: Static/read-only commands and before/after Git status/hash comparison
Rollback: No product mutation; list any deliverable files created
