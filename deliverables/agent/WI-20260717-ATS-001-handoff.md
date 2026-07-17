[WI HEADER]
WI ID: WI-20260717-ATS-001
REQ: REQ-20260716-ATS-004
Agent: qa-integ
Depends On: WI-20260716-ATS-038
Blocks: WI-20260717-ATS-002, WI-20260717-ATS-003, WI-20260717-ATS-004

[WI SUMMARY]
Why: Convert the approved V1 residual-code disposition and the user's final policy decisions into an executable implementation ledger before destructive cleanup begins.
Scope (in/out): In scope are the 56 integrated disposition items, the resolved REVIEW decisions, protected KEEP safeguards, implementation bundle order, verification reset rules, and stop/escalation conditions. Out of scope are product code edits, DB mutation, runtime shutdown, branch deletion, and artifact deletion.
DoD: Every integrated disposition item is mapped to an approved action or explicit retention; all former REVIEW items have a recorded decision; implementation WIs have disjoint ownership and ordered dependencies; KEEP safeguards and negative-search/test requirements are explicit.
Constraints/Forbidden: Read-only analysis of product files. Do not edit backend, frontend, DB, active docs, Git refs, worktrees, runtime processes, or local secrets. Never print secret values. Preserve frontend/tsconfig.tsbuildinfo and all unrelated user artifacts.

[ACCEPTANCE CRITERIA]
Functional:
- [ ] Map all 56 integrated disposition IDs to KEEP, REMOVE, REPLACE, ARCHIVE, or an approved deferred action.
- [ ] Record the user's final decisions for legacy APIs, QA bootstrap, payment provider normalization, local configuration isolation, package version, branch-tip preservation, logs, and emergency admin subscription operations.
- [ ] Define implementation WI boundaries with disjoint write ownership and dependency order.
- [ ] Define destructive-operation preflight and rollback evidence without performing destructive actions.
Performance:
- [ ] Not applicable; no runtime path changes.
Quality:
- [ ] No disposition item is omitted or duplicated.
- [ ] KEEP safeguards are explicitly protected.
- [ ] Evidence contains reproducible searches and current Git/runtime pointers.

[INPUT POINTERS]
Tier 0 (Constitution - Required):
- docs/standards/core-principles.md
- docs/standards/development-standards.md

Tier 1 (Policies - Inferred):
- docs/policies/quality-gates.md
- docs/policies/security-policy.md
- docs/policies/versioning-policy.md
- docs/policies/execution-policy.md

Tier 2 (Tech Stack / Current-State Contracts):
- docs/design/api-spec.md
- docs/design/db-schema.md
- docs/index.md

REQ / Context Documents:
- deliverables/user/REQ-20260716-ATS-004.md
- deliverables/user/WI-20260716-ATS-038-summary.md
- deliverables/agent/WI-20260716-ATS-038-evidence-pack.md
- deliverables/agent/WI-20260716-ATS-034-evidence-pack.md
- deliverables/agent/WI-20260716-ATS-035-evidence-pack.md
- deliverables/agent/WI-20260716-ATS-036-evidence-pack.md
- deliverables/agent/WI-20260716-ATS-037-evidence-pack.md

Files / Runtime Pointers:
- AGENTS.md
- src/main/java/com/atstudio/atstudio/
- src/main/resources/application.yml
- src/main/resources/schema.sql
- src/main/resources/db/manual/
- frontend/src/
- frontend/package.json
- git status --short
- git worktree list --porcelain

[OUTPUT CONTRACT]
User-facing -> deliverables/user/WI-20260717-ATS-001-summary.md:
- Concise execution ledger, resolved decisions, protected safeguards, implementation order, and approval linkage.
Agent-facing -> deliverables/agent/WI-20260717-ATS-001-evidence-pack.md:
- Complete 56-item mapping, source pointers, commands, WI decomposition, destructive preflight, verification reset rules, risks, and rollback evidence.
Handoff Packet -> deliverables/agent/WI-20260717-ATS-001-handoff.md:
- This packet.

[TRACEABILITY REQUIREMENTS]
Evidence pointers (files/lines/commands/logs): Required.
Tests: Documentation validation is required; product tests are not applicable to this read-only WI.
Rollback: No product rollback is needed; revert only WI-001 deliverables if their mapping is incorrect.
