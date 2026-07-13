[WI HEADER]
WI ID: WI-20260711-ATS-003
REQ: REQ-20260711-ATS-001
Agent: qa-fe
Depends On: -
Blocks: WI-20260711-ATS-006, WI-20260711-ATS-007, WI-20260711-ATS-008

[WI SUMMARY]
Why: Audit the active React SPA for route, authorization, API-contract, state, UX, accessibility, and maintainability gaps.
Scope (in/out): Inspect frontend routes, pages, components, stores, API clients, tests, and user-visible states across all roles and core features. Treat Thymeleaf as legacy compatibility only. Do not edit frontend code.
DoD: Produce a role-by-screen map and evidence-backed defects, missing states, stale copy, contract drift, and performance/accessibility findings.
Constraints/Forbidden: Read-only except WI outputs. Do not trigger destructive admin/payment actions in browser tests.

[ACCEPTANCE CRITERIA]
Functional:
- [ ] Map routes and role gates to backend endpoints.
- [ ] Check loading, empty, success, error, retry, cancellation, and stale-state behavior.
- [ ] Inspect payment, whitelist, company certification, subscription, music/search, profile, and admin screens.
- [ ] Report exact file/line evidence and affected user journey.
Performance:
- [ ] Identify unnecessary re-fetching, unstable effects, large bundles/components, and list-render risks where evidenced.
Quality:
- [ ] Review React patterns against active project standards.
- [ ] Identify missing frontend tests and accessibility issues without speculative claims.

[INPUT POINTERS]
Tier 0 (Constitution - Required for all):
- docs/standards/core-principles.md
- docs/standards/development-standards.md

Tier 1 (Policies - Inferred):
- docs/policies/quality-gates.md
- docs/policies/access-control-policy.md

Tier 2 (Tech Stack and UI Context):
- .agents/skills/react-best-practices/AGENTS.md
- docs/standards/frontend-standards.md
- docs/ui/
- docs/design/
- docs/client/

REQ/Context Docs:
- deliverables/user/REQ-20260711-ATS-001.md

Files:
- frontend/src/
- frontend/package.json
- frontend/vite.config.ts

Repro/Logs:
- rg --files frontend/src

[OUTPUT CONTRACT]
User-facing -> deliverables/user/WI-20260711-ATS-003-summary.md : concise Korean findings and risks
Agent-facing -> deliverables/agent/WI-20260711-ATS-003-evidence-pack.md : route/role map, evidence, severity, test gaps, follow-up inputs
Handoff Packet -> deliverables/agent/WI-20260711-ATS-003-handoff.md : this packet

[TRACEABILITY REQUIREMENTS]
Evidence pointers: Required, with narrow file/line references
Tests: Static inspection now; list focused browser/unit tests required later
Rollback: Only remove this WI's newly created summary/evidence files if explicitly requested
