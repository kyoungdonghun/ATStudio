[WI HEADER]
WI ID: WI-20260725-ATS-001
REQ: REQ-20260725-ATS-001
Agent: se
Depends On: -
Blocks: WI-20260725-ATS-002

[WI SUMMARY]
Why: A real Gmail acceptance run verified the user successfully, but React
StrictMode executed the email-verification effect again and the second
single-use-token failure replaced the successful UI state.
Scope (in/out): Prevent duplicate verification requests within the same
EmailVerifyPage lifecycle and add a StrictMode regression test. Do not disable
StrictMode, change backend token semantics, edit SMTP configuration, or broaden
the authentication workflow.
DoD: StrictMode rendering sends exactly one verification request, preserves the
success state, and retains missing-token and provider-failure behavior.
Constraints/Forbidden: Do not log or persist raw tokens, Gmail addresses, or
credentials. Do not edit backend code, router contracts, mail templates,
runtime secrets, or unrelated UI. Preserve the running acceptance environment.

[ACCEPTANCE CRITERIA]
Functional:
- [ ] A token-bearing EmailVerifyPage rendered under StrictMode calls
      `verifyEmail` exactly once.
- [ ] The first successful response remains a success UI state.
- [ ] Missing-token and rejected-token states remain supported.
- [ ] React StrictMode remains enabled.
Performance:
- [ ] No global token cache or retained cross-page state is introduced.
Quality:
- [ ] Focused Vitest coverage passes.
- [ ] Changed files pass Prettier and ESLint.
- [ ] TypeScript typecheck passes.

[INPUT POINTERS]
Tier 0 (Constitution - Required):
- docs/standards/core-principles.md
- docs/standards/development-standards.md

Tier 1 (Policies - Inferred):
- docs/policies/security-policy.md
- docs/policies/quality-gates.md

Tier 2 (React):
- docs/standards/frontend-standards.md
- .agents/skills/react-best-practices/AGENTS.md

REQ/Context Docs:
- deliverables/user/REQ-20260725-ATS-001.md
- docs/design/db-schema.md

Files:
- frontend/src/main.tsx
- frontend/src/pages/auth/EmailVerifyPage.tsx
- frontend/src/test/coverage/publicAuthShell.coverage.test.tsx
- frontend/src/api/auth.ts

Repro/Logs:
- Real acceptance result: registration HTTP 201, secret-free mail outcome
  SUCCESS, admin lookup `isVerified=true`, followed by repeated invalid-token
  responses from the same verification page.
- `frontend/src/main.tsx` wraps App with StrictMode.
- `EmailVerifyPage` currently invokes `verifyEmail(token)` directly in an
  effect without a duplicate-execution guard.

[OUTPUT CONTRACT]
User-facing -> deliverables/user/WI-20260725-ATS-001-summary.md:
- User-visible behavior, files changed, focused verification, residual risks.
Agent-facing -> deliverables/agent/WI-20260725-ATS-001-evidence-pack.md:
- Evidence pointers, patch rationale, commands and results, rollback, and the
  next blocked WI.
Handoff Packet -> deliverables/agent/WI-20260725-ATS-001-handoff.md:
- This packet.

[TRACEABILITY REQUIREMENTS]
Evidence pointers (files/lines/commands/logs): Required.
Tests: Include the focused StrictMode regression command and result.
Rollback: Document how to revert only this corrective patch.
