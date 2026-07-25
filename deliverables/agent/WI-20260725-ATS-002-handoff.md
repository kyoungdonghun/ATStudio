[WI HEADER]
WI ID: WI-20260725-ATS-002
REQ: REQ-20260725-ATS-001
Agent: qa-fe
Depends On: WI-20260725-ATS-001
Blocks: -

[WI SUMMARY]
Why: Independently verify that the StrictMode duplicate-request correction is
minimal, policy-preserving, and compatible with the complete frontend quality
baseline before the MA repeats the real Gmail acceptance link.
Scope (in/out): Review the WI-001 diff, run the full frontend test, typecheck,
ESLint, Prettier, and production build gates, and document results. Do not
modify product code unless a concrete defect is found and reported first. Do
not send email, inspect runtime credentials, consume a real token, or stop the
running acceptance environment.
DoD: All frontend gates pass, the focused StrictMode regression still proves
one request, and the implementation retains component-local scope without
global token caching or backend policy changes.
Constraints/Forbidden: Never read or print SMTP credentials, Gmail addresses,
or verification tokens. Preserve the existing untracked screenshot ZIP and all
WI-001 changes. Do not change StrictMode or backend code.

[ACCEPTANCE CRITERIA]
Functional:
- [ ] Independent review confirms one verification request per mounted page
      lifecycle under StrictMode.
- [ ] Missing-token, success, and server-rejection states remain covered.
- [ ] No backend single-use-token behavior or SMTP setting changed.
Performance:
- [ ] No global cache, polling, timer, or retained cross-route token state was
      introduced.
Quality:
- [ ] Full Vitest suite passes.
- [ ] TypeScript typecheck passes.
- [ ] ESLint passes with zero warnings.
- [ ] Prettier check passes.
- [ ] Production build passes.
- [ ] `git diff --check` passes.

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
- deliverables/agent/WI-20260725-ATS-001-handoff.md
- deliverables/agent/WI-20260725-ATS-001-evidence-pack.md

Files:
- frontend/src/main.tsx
- frontend/src/pages/auth/EmailVerifyPage.tsx
- frontend/src/test/coverage/publicAuthShell.coverage.test.tsx
- frontend/src/api/auth.ts

Repro/Logs:
- WI-001 focused regression reproduced two calls before the patch and exactly
  one call after the patch.
- The first real Gmail run resulted in `isVerified=true` while the UI displayed
  a second-call invalid-token failure.

[OUTPUT CONTRACT]
User-facing -> deliverables/user/WI-20260725-ATS-002-summary.md:
- Independent verdict, complete gate results, and the remaining real-Gmail
  human gate.
Agent-facing -> deliverables/agent/WI-20260725-ATS-002-evidence-pack.md:
- Diff review, exact commands/results, residual risks, and rollback notes.
Handoff Packet -> deliverables/agent/WI-20260725-ATS-002-handoff.md:
- This packet.

[TRACEABILITY REQUIREMENTS]
Evidence pointers (files/lines/commands/logs): Required.
Tests: Record exact aggregate counts and command exit results.
Rollback: Confirm the patch can be reverted independently from SMTP/runtime
configuration.
