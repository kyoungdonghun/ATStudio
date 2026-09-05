[WI HEADER]
WI ID: WI-20260823-ATS-009
REQ: REQ-20260823-ATS-001
Agent: qa-integ
Depends On: WI-20260823-ATS-008
Blocks: -

[WI SUMMARY]
Why: Independently prove that the repaired copy regression and retained-demo media restoration support the intended acceptance paths.
Scope (in/out): In: execute the full frontend test suite, typecheck/lint/build, and read-only HTTP verification of all ten scoped AT.M Demo tracks. Out: any source/config/data/storage edits, login, payment/mail/provider operations, client worktree, and process restarts.
DoD: Full Vitest run is green; typecheck/lint/build pass; each scoped stream supports full plus byte-range retrieval and each thumbnail resolves. Any residual blocker is reported precisely.
Constraints/Forbidden: Do not mutate files except this WI's evidence/summary deliverables. Do not use external services or expose secret values.

[ACCEPTANCE CRITERIA]
Functional:
- [ ] Full frontend Vitest suite passes.
- [ ] For each of ten exact AT.M Demo tracks, stream endpoint returns 200/206 and thumbnail returns 200.
Quality:
- [ ] `npm run typecheck`, `npm run lint`, and `npm run build` pass.
- [ ] `git diff --check` passes.

[INPUT POINTERS]
Tier 0 (Constitution - Required for all):
- docs/standards/core-principles.md
- docs/standards/development-standards.md

Tier 1 (Policies):
- docs/policies/quality-gates.md
- docs/policies/security-policy.md

Tier 2 (Tech Stack):
- .agents/skills/react-best-practices/AGENTS.md
- docs/standards/frontend-standards.md

REQ/Context Docs:
- deliverables/user/REQ-20260823-ATS-001.md
- deliverables/agent/WI-20260823-ATS-008-evidence-pack.md

Files:
- frontend/src/pages/public/HomePage.test.tsx
- src/main/java/com/atstudio/atstudio/controller/TrackController.java

[OUTPUT CONTRACT]
User-facing -> deliverables/user/WI-20260823-ATS-009-summary.md
Agent-facing -> deliverables/agent/WI-20260823-ATS-009-evidence-pack.md
Handoff Packet -> deliverables/agent/WI-20260823-ATS-009-handoff.md

[TRACEABILITY REQUIREMENTS]
Evidence pointers: Required
Tests: Full frontend suite, typecheck, lint, build, HTTP status matrix
Rollback: None; verification makes no product/data/configuration changes.
