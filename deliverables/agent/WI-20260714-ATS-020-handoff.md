[WI HEADER]
WI ID: WI-20260714-ATS-020
REQ: REQ-20260714-ATS-001
Agent: qa-fe
Depends On: WI-20260714-ATS-014, WI-20260714-ATS-015, WI-20260714-ATS-016
Blocks: WI-20260714-ATS-022, WI-20260714-ATS-025, WI-20260714-ATS-029, WI-20260714-ATS-034

[WI SUMMARY]
Why: Verify that social-login/session and acceptance Vite ingress changes remain coherent for real SPA navigation and proxy behavior.
Scope: Social callback success/failure/Strict Mode tests, logout/refresh client behavior, exact Host/proxy header tests, route/build checks, and targeted browser-ready flow assertions.
Out: Starting a public tunnel, visual redesign, live OAuth providers, live Toss, or broad snapshot churn.
DoD: Partial login state cannot survive callback failure, server logout is used without interceptor recursion, and Vite accepts only approved Hosts while producing one trusted internal client identity.
Constraints: Preserve relative `/api` and `/uploads`; do not modify backend trust policy except for a reproduced contract mismatch.

[ACCEPTANCE CRITERIA]
Functional:
- [ ] Social callback fetches the user with staged credentials before committing login/navigation.
- [ ] Callback/logout/refresh failures clear all dependent state without recursive logout.
- [ ] Strict Mode does not duplicate exchanges or navigation.
- [ ] Vite Host and forwarding-header contracts cover local and acceptance modes.
Quality:
- [ ] Focused Vitest, typecheck, lint, build, and diff check pass.

[INPUT POINTERS]
Tier 0:
- docs/standards/core-principles.md
- docs/standards/development-standards.md
Tier 2:
- docs/standards/frontend-standards.md
- .agents/skills/react-best-practices/AGENTS.md
Context:
- deliverables/user/REQ-20260714-ATS-001.md
- deliverables/agent/WI-20260714-ATS-014-evidence-pack.md
- deliverables/agent/WI-20260714-ATS-015-evidence-pack.md
- deliverables/agent/WI-20260714-ATS-016-evidence-pack.md
Files:
- frontend/src/pages/auth/SocialLoginPage.tsx
- frontend/src/store/authStore.ts
- frontend/src/api/auth.ts
- frontend/src/api/client.ts
- frontend/vite.config.ts
- related frontend tests/routes

[OUTPUT CONTRACT]
User-facing -> deliverables/user/WI-20260714-ATS-020-summary.md (Korean)
Agent-facing -> deliverables/agent/WI-20260714-ATS-020-evidence-pack.md
Handoff -> deliverables/agent/WI-20260714-ATS-020-handoff.md

[TRACEABILITY REQUIREMENTS]
Test commands/counts, state-transition assertions, route/proxy evidence, rollback, and browser-smoke prerequisites are required.
