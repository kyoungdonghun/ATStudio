[WI HEADER]
WI ID: WI-20260714-ATS-011
REQ: REQ-20260714-ATS-001
Agent: se
Depends On: WI-20260714-ATS-001, WI-20260714-ATS-003
Blocks: WI-20260714-ATS-014, WI-20260714-ATS-019, WI-20260714-ATS-024, WI-20260714-ATS-025

[WI SUMMARY]
Why: Revoke the current refresh-session capability on logout, password change, password reset, and withdrawal.
Scope (in/out):
- In: Locked refresh/user termination paths, authenticated idempotent logout API, frontend server-logout call, replay tests, and API contract-compatible behavior.
- Out: Multi-session ledger, access-token denylist, JWT key rotation, and social callback ordering.
DoD:
- Old refresh tokens fail after all approved termination events.
- Refresh and termination serialize on one user row without leaking token material.
- Frontend clears local auth state while making a best-effort authenticated server logout.
Constraints/Forbidden:
- Preserve the current single-refresh-session model.
- Do not log token values or hashes.
- Do not edit `SocialLoginPage.tsx`; WI-014 follows this WI.
- You are not alone in the codebase; never revert concurrent edits.

[ACCEPTANCE CRITERIA]
Functional:
- [ ] `POST /api/auth/logout` is authenticated, idempotent, bodyless, and returns 204.
- [ ] Refresh, logout, password change/reset, and withdrawal use compatible locked revocation semantics.
- [ ] Replay after each termination fails with the existing auth error taxonomy.
- [ ] A stale refresh mismatch does not clear a newer valid session.
Quality:
- [ ] Backend auth/service/controller tests and focused frontend logout tests pass.
- [ ] No secret/token appears in tests or logs.
- [ ] Compile/typecheck and `git diff --check` pass for owned files.

[INPUT POINTERS]
Tier 0:
- docs/standards/core-principles.md
- docs/standards/development-standards.md
Tier 1:
- docs/policies/security-policy.md
- docs/policies/access-control-policy.md
Tier 2 / Context:
- deliverables/user/REQ-20260714-ATS-001.md
- docs/audit/p1-remediation-trace-matrix-20260714.md
- docs/design/p1-security-acceptance-hardening-design.md
- docs/standards/frontend-standards.md
- src/main/java/com/atstudio/atstudio/service/auth/AuthService.java
- src/main/java/com/atstudio/atstudio/controller/AuthController.java
- src/main/java/com/atstudio/atstudio/service/UserService.java
- src/main/java/com/atstudio/atstudio/service/EmailService.java
- src/main/java/com/atstudio/atstudio/repository/UserRepository.java
- frontend/src/store/authStore.ts
- frontend/src/api/auth.ts

[OUTPUT CONTRACT]
User-facing -> deliverables/user/WI-20260714-ATS-011-summary.md (Korean)
Agent-facing -> deliverables/agent/WI-20260714-ATS-011-evidence-pack.md
Handoff -> deliverables/agent/WI-20260714-ATS-011-handoff.md
Implementation ownership -> auth/session backend paths, frontend logout API/store path, and focused tests.

[TRACEABILITY REQUIREMENTS]
Evidence pointers and commands: Required
Tests: focused backend and frontend auth tests
Rollback: revert endpoint/client call while preserving safe local logout behavior
