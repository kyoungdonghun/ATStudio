[WI HEADER]
WI ID: WI-20260823-ATS-011
REQ: REQ-20260823-ATS-002
Agent: se
Depends On: WI-20260823-ATS-010
Blocks: -

[WI SUMMARY]
Why: The full frontend suite found one stale SocialLoginPage expectation that conflicts with the approved public-playback persistence boundary established by WI-010.
Scope (in/out): Update only the affected test expectation and its test setup as required. Do not alter application behavior, authentication API contracts, player persistence logic, browser storage, DB, external providers, or the client acceptance worktree.
DoD: The social-login failure test verifies that auth and user-scoped likes reset while public playback is preserved; the focused test and full frontend suite pass.
Constraints/Forbidden: Preserve the existing user-facing failure message and best-effort logout behavior. No product code change unless the test exposes a real contract mismatch.

[ACCEPTANCE CRITERIA]
Functional:
- [ ] A social login callback that cannot load the user clears the staged auth session and user-scoped likes.
- [ ] The same failure does not clear unrelated public PlayerBar playback.
Quality:
- [ ] Focused SocialLoginPage test passes.
- [ ] Full frontend test suite passes.
- [ ] Typecheck, lint, Prettier, build, and diff check pass after the final change.

[INPUT POINTERS]
Tier 0 (Constitution - Required for all):
- docs/standards/core-principles.md

Tier 0 (Standards - Based on Assignee):
- docs/standards/development-standards.md

Tier 1 (Policies):
- docs/policies/quality-gates.md

REQ/Context Docs:
- deliverables/user/REQ-20260823-ATS-002.md
- deliverables/agent/WI-20260823-ATS-010-evidence-pack.md

Files:
- frontend/src/pages/auth/SocialLoginPage.test.tsx:216-257
- frontend/src/store/authStore.ts:172-182

Repro/Logs:
- `npm test -- --run` result: SocialLoginPage stale expectation at line 256.

[OUTPUT CONTRACT]
User-facing -> deliverables/user/WI-20260823-ATS-011-summary.md:
- Summary, changed files, and validation result.
Agent-facing -> deliverables/agent/WI-20260823-ATS-011-evidence-pack.md:
- Patch rationale, tests, scope confirmation, and rollback.
Handoff Packet -> deliverables/agent/WI-20260823-ATS-011-handoff.md:
- This packet.

[TRACEABILITY REQUIREMENTS]
Evidence pointers, focused/full test results, and rollback guidance are required.
