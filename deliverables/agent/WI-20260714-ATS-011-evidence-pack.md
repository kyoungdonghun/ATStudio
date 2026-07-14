# Evidence Pack: WI-20260714-ATS-011

## Summary (one-liner)

- Implemented locked single-session refresh revocation for logout and account termination paths, plus the frontend server-logout flow.

## Scope / DoD Check

- [x] Added authenticated, idempotent, bodyless `POST /api/auth/logout` returning 204.
- [x] Serialized refresh, logout, password change, password reset, and withdrawal on the user row.
- [x] Revoked the stored refresh hash in the same transaction as successful termination changes.
- [x] Preserved a newer refresh session when an older refresh hash mismatches.
- [x] Called server logout before local frontend cleanup and retained transient-failure visibility.
- [x] Added focused backend repository/service/controller tests and frontend API/store tests.
- [ ] Real concurrent refresh-versus-termination and end-to-end replay matrix remains assigned to blocking follow-up `WI-20260714-ATS-019`.

## Reference Documents (Tier 0-2)

| Tier | Document | Reason |
|---|---|---|
| 0 | `docs/standards/core-principles.md` | System constitution and security lifecycle baseline |
| 0 | `docs/standards/development-standards.md` | Java/Spring and frontend implementation standards |
| 0 | `docs/standards/documentation-standards.md` | Evidence Pack structure and traceability |
| 0 | `docs/standards/glossary.md` | Canonical terminology |
| 1 | `docs/policies/security-policy.md` | Token secrecy and authentication policy |
| 1 | `docs/policies/access-control-policy.md` | Least-privilege endpoint policy |
| 2 | `deliverables/user/REQ-20260714-ATS-001.md` | Approved scope and work chain |
| 2 | `docs/audit/p1-remediation-trace-matrix-20260714.md` | `ATS020-P1-03` evidence and follow-up test ownership |
| 2 | `docs/design/p1-security-acceptance-hardening-design.md` | Refresh-session revocation contract |
| 2 | `docs/standards/frontend-standards.md` | Auth API, Zustand, and Axios conventions |

**Injection Rules Applied**:

- Rule source: `AGENTS.md` and WI handoff INPUT POINTERS
- Assignee: `se`
- Task type: security implementation
- Required context: Tier 0, security/access policies, approved REQ, audit matrix, security design, frontend standards

## Evidence Pointers

- `src/main/java/com/atstudio/atstudio/repository/UserRepository.java:16-19` - pessimistic user-row lookup.
- `src/main/java/com/atstudio/atstudio/service/auth/AuthService.java:75-116` - locked refresh rotation, stale mismatch preservation, and idempotent logout.
- `src/main/java/com/atstudio/atstudio/controller/AuthController.java:51-56` - bodyless 204 logout endpoint.
- `src/main/java/com/atstudio/atstudio/config/SecurityConfig.java:59` - explicit authenticated logout rule.
- `src/main/java/com/atstudio/atstudio/service/UserService.java:112-193` - locked withdrawal/password change revocation.
- `src/main/java/com/atstudio/atstudio/service/EmailService.java:140-161` - locked password-reset revocation.
- `frontend/src/api/auth.ts:156-168` - bodyless logout request and confirmed-401 handling.
- `frontend/src/store/authStore.ts:47-74` - server-first logout, confirmation result, and local cleanup.
- `frontend/src/api/client.ts:68-71,103-108` - refresh failure uses local cleanup without recursive server logout.
- Focused tests: `AuthServiceTest.java`, `AuthControllerTest.java`, `UserServiceTest.java`, `EmailServiceTest.java`, `UserRepositoryTest.java`, `frontend/src/api/auth.test.ts`, `frontend/src/store/authStore.test.ts`, `frontend/src/api/client.test.ts`.
- Forbidden-file check: `frontend/src/pages/auth/SocialLoginPage.tsx` has no WI-011 diff.

## Commands & Outputs

- `gradlew.bat test --tests "com.atstudio.atstudio.service.auth.AuthServiceTest" --tests "com.atstudio.atstudio.service.UserServiceTest" --tests "com.atstudio.atstudio.service.EmailServiceTest" --tests "com.atstudio.atstudio.repository.UserRepositoryTest" --tests "com.atstudio.atstudio.controller.AuthControllerTest"`
  - Passed: 5 classes, 50 tests, 0 failures.
- `npm test -- src/api/auth.test.ts src/store/authStore.test.ts src/api/client.test.ts`
  - Passed: 3 files, 9 tests, 0 failures.
- `gradlew.bat compileJava compileTestJava`
  - Passed: `BUILD SUCCESSFUL`.
- `npm run typecheck`
  - Passed: `tsc --noEmit`.
- Owned-path `git diff --check` command recorded below.
  - Passed with no whitespace errors; Git emitted only the repository's LF-to-CRLF worktree notices.

## Risks / Rollback

- Risks:
  - Access JWTs remain valid until their existing expiry; immediate access-token revocation is out of scope.
  - Focused tests prove lock declaration and service behavior, but real refresh/termination races and end-to-end replay require `WI-20260714-ATS-019`.
  - On a transient server-logout failure, the frontend intentionally clears local state and returns `false`; server revocation is not falsely confirmed.
- Rollback:
  - Revert the locked repository lookup and backend endpoint/service changes together.
  - Revert the frontend API call while preserving `clearSession` behavior so local logout remains available.
  - Do not revert unrelated concurrent payment, CSV, file, or acceptance-environment changes.

## Follow-ups

- Immediate chain trigger: `WI-20260714-ATS-014` (handoff already present).
- Blocked verification/review chain: `WI-20260714-ATS-019`, `WI-20260714-ATS-024`, `WI-20260714-ATS-025` when their phase dependencies are satisfied.
