[WI HEADER]
WI ID: WI-20260714-ATS-014
REQ: REQ-20260714-ATS-001
Agent: se
Depends On: WI-20260714-ATS-003, WI-20260714-ATS-011
Blocks: WI-20260714-ATS-020, WI-20260714-ATS-024, WI-20260714-ATS-025

[WI SUMMARY]
Why: Ensure a new social callback authenticates `/users/me` with the newly issued token and never leaves partial login state.
Scope: Social callback ordering, staged token cleanup, strict-mode duplicate protection, and focused frontend tests. No backend OAuth redesign.
DoD: Tokens are staged before profile fetch; profile/token/user commit is coherent; failure clears/revokes partial state; provider/profile-incomplete routes remain correct.
Constraints: Do not edit backend session semantics or unrelated auth UI. Preserve WI-011 changes. No live provider calls.

[ACCEPTANCE CRITERIA]
- [ ] `fetchMe` receives or observes the returned access token on the first request.
- [ ] Failure clears token/user/role/dependent auth state and best-effort logs out server-side.
- [ ] React Strict Mode does not exchange the same callback twice.
- [ ] Focused Vitest, typecheck, ESLint, and diff check pass.

[INPUT POINTERS]
Tier 0: docs/standards/core-principles.md; docs/standards/development-standards.md
Tier 1: docs/policies/security-policy.md
Tier 2: docs/standards/frontend-standards.md; .agents/skills/react-best-practices/AGENTS.md
Context: deliverables/user/REQ-20260714-ATS-001.md; docs/audit/p1-remediation-trace-matrix-20260714.md; docs/design/p1-security-acceptance-hardening-design.md
Files: frontend/src/pages/auth/SocialLoginPage.tsx; frontend/src/api/auth.ts; frontend/src/store/authStore.ts; focused tests

[OUTPUT CONTRACT]
User summary: deliverables/user/WI-20260714-ATS-014-summary.md (Korean)
Evidence Pack: deliverables/agent/WI-20260714-ATS-014-evidence-pack.md
Implementation ownership: social callback page and focused frontend tests; adapt WI-011 auth API/store without reverting it.

[TRACEABILITY REQUIREMENTS]
Evidence/commands/tests/rollback required; no live OAuth.
