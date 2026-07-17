[WI HEADER]
WI ID: WI-20260717-ATS-006
REQ: REQ-20260716-ATS-004
Agent: qa, qa-fe, qa-integ, cr
Depends On: WI-20260717-ATS-002, WI-20260717-ATS-003, WI-20260717-ATS-004, WI-20260717-ATS-005
Blocks: WI-20260717-ATS-007

[WI SUMMARY]
Why: Independently prove that the complete V1 consolidation is correct, coherent, runnable, documented, and free of unintended feature regression before final Git/worktree cleanup.
Scope (in/out): Three read-only verification tracks: backend/DB/config/payment QA; frontend/UI/API-contract QA; integrated residual-code/security/docs/repository review. Validate the 56-item decision ledger, protected KEEP safeguards, fresh/local DB evidence, full quality gates, current counts, exact negative references, runtime/API/UI smoke, and historical preservation. Out of scope for review agents: product/doc edits, DB mutation beyond isolated test fixtures, branch/worktree/tag deletion, staging/commit/push. Findings are reported only; any repair requires a follow-up WI and resets all affected gates.
DoD: All three independent reports exist; objective gates pass or findings are classified P1/P2/P3 with reproducible evidence; all 56 dispositions are closed/kept/deferred as approved; no unresolved P1/P2 remains; local explicit-config backend and Vite frontend start; key public/auth/subscriber/admin API/UI smoke flows pass; final aggregated evidence and user summary are produced only after any repairs and reruns.
Constraints/Forbidden: Review agents must not edit product, docs, DB baseline, Git refs, staging, secrets, or other agents' reports. Never print application-local.yml values. Do not rely only on prior WI claims: rerun assigned gates. Preserve tsconfig local cache tracking policy and historical records. Do not broaden product policy or add features.

[ACCEPTANCE CRITERIA]
Functional:
- [ ] Backend/DB/config/payment track independently passes full tests, JaCoCo generation, build, schema/entity/provider/config contracts, local ddl-auto validate, and protected financial invariants.
- [ ] Frontend track independently passes typecheck, ESLint, Prettier, coverage-capable Vitest, full Vitest, build, route/API negative searches, and key component/guard/player/payment/whitelist/download regressions.
- [ ] Integration track maps all 56 INT IDs, checks current docs/code/schema/routes/counts, historical preservation, secret exposure, unused references, generated artifacts, and Git/worktree preconditions.
- [ ] Local explicit-config backend and frontend runtime/API/UI smoke is completed after static tracks pass.
Performance:
- [ ] No obvious startup, request, or bundle regression is introduced; record measured build/runtime smoke evidence where available.
Quality:
- [ ] No unresolved P1/P2 findings.
- [ ] Any post-gate code/doc change causes affected focused gates and the entire final gate to rerun.
- [ ] git diff --check and staged/unstaged secret scans pass.

[INPUT POINTERS]
Tier 0:
- docs/standards/core-principles.md
- docs/standards/development-standards.md
- docs/standards/documentation-standards.md
- docs/standards/glossary.md

Tier 1:
- docs/policies/quality-gates.md
- docs/policies/security-policy.md
- docs/policies/access-control-policy.md
- docs/policies/versioning-policy.md

Tier 2:
- docs/design/api-spec.md
- docs/design/db-schema.md
- docs/ui/
- docs/payment/
- docs/client/testing-guide.md
- docs/registry/project-registry.md
- .agents/skills/react-best-practices/AGENTS.md

REQ / Decision / Evidence Sources:
- deliverables/user/REQ-20260716-ATS-004.md
- deliverables/agent/WI-20260717-ATS-001-evidence-pack.md
- deliverables/agent/WI-20260716-ATS-038-evidence-pack.md
- deliverables/agent/WI-20260717-ATS-002-evidence-pack.md
- deliverables/agent/WI-20260717-ATS-003-evidence-pack.md
- deliverables/agent/WI-20260717-ATS-004-evidence-pack.md
- deliverables/agent/WI-20260717-ATS-005-evidence-pack.md

Files:
- src/main/java/
- src/main/resources/
- src/test/java/
- frontend/src/
- frontend/package.json
- docs/
- scripts/
- .gitignore
- AGENTS.md
- CLAUDE.md

[OUTPUT CONTRACT]
Backend QA -> deliverables/agent/WI-20260717-ATS-006/backend-qa.md
Frontend QA -> deliverables/agent/WI-20260717-ATS-006/frontend-qa.md
Integration review -> deliverables/agent/WI-20260717-ATS-006/integration-review.md
Final user-facing -> deliverables/user/WI-20260717-ATS-006-summary.md
Final agent-facing -> deliverables/agent/WI-20260717-ATS-006-evidence-pack.md
Handoff Packet -> deliverables/agent/WI-20260717-ATS-006-handoff.md

[TRACEABILITY REQUIREMENTS]
Every track must list injected docs, commands/results, exact findings with file/line pointers, coverage/skip limits, residual risks, and rollback/repair recommendations. The final aggregator must reconcile disagreements, map all 56 INT IDs, identify rerun resets, and refuse completion while any P1/P2 is open.
