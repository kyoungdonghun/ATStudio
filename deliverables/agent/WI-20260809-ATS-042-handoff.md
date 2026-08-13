[WI HEADER]
WI ID: WI-20260809-ATS-042
REQ: REQ-20260809-ATS-001
Agent: se
Depends On: WI-20260809-ATS-036
Blocks: WI-20260809-ATS-058, WI-20260809-ATS-060, WI-20260809-ATS-072

[WI SUMMARY]
Why: `CR-031-008` through `CR-031-014` show seven bounded authentication/account-state defects: complete-profile duplicate submission during async validation, capability discovery presented fail-open, invalid Profile tabs rendering no panel, subscription errors presented as authoritative absence, completed profiles entering a dead-end completion route, and safe backend guidance being discarded in password-reset and password-update failures.
Scope (in/out):
- In: Fence the entire Social complete-profile submit transaction before any async validation and keep all relevant controls pending until completion.
- In: Represent public capability loading, success, and failure explicitly. On capability failure, do not advertise password login, signup, password reset, email verification, social providers, or QA bootstrap as known available. Preserve backend enforcement and provide bounded retry/unavailable guidance.
- In: Normalize unsupported Profile `tab` query values to a canonical valid tab without a blank panel, including back/forward navigation.
- In: Separate Profile subscription loading, authoritative empty, and retryable failure states. Do not report a failed request as no subscription.
- In: Keep `/complete-profile` usable only for an authenticated incomplete profile. Redirect a known complete profile to the canonical Profile destination and preserve a safe loading/error state while identity is unresolved; do not weaken the existing authentication guard.
- In: Preserve only safe, bounded backend guidance for forgot-password and Profile password-update failures. Keep accepted forgot-password responses enumeration-safe and do not reveal account existence.
- In: Add focused and adjacent tests for delayed validation, capability failure/retry, invalid tab navigation, subscription failure/retry/empty, complete-profile route states, and safe error mapping.
- In: Update current auth/Profile/security/UI documentation and WI evidence where implementation changes the documented presentation contract.
- Out: Consent policy, whether unverified users may login, auth return-origin policy, logout ordering, backend password policy, rate-limit thresholds, token/storage architecture, OAuth Provider execution, actual email delivery, dependency changes, schema/data changes, and WI-058 accessibility/localization work.
DoD: All seven canonical roots are corrected within existing policy; no failure state is rendered as known availability or known absence; duplicate mutation windows are closed; focused, adjacent, full frontend/backend gates and documentation validation pass; independent PG review confirms no authentication/security regression.
Constraints/Forbidden:
- Do not change backend authentication, verification, password, consent, or rate-limit policy.
- Do not expose raw backend payloads, stack text, tokens, addresses, account-existence signals, or secrets.
- Do not perform real login against external OAuth, send mail, mutate persistent DB, or inspect ignored local configuration.
- Do not implement WI-060 policy decisions or WI-058 accessibility/localization scope.
- Do not touch, inspect, stage, or delete `output/client-demo-screenshots-20260716-140514.zip` or `output/ui-ux-audit/`.

[ACCEPTANCE CRITERIA]
Functional:
- [ ] A second complete-profile submit cannot enter while any validation or mutation from the first submit is pending.
- [ ] Capability discovery loading, known result, and failure are distinct; failure advertises no capability as known enabled and offers a bounded retry/unavailable state.
- [ ] Login, Signup, and Password Reset render consistently from the shared capability state without weakening backend authorization.
- [ ] Unsupported Profile tabs normalize to a canonical valid tab and browser navigation never leaves a blank panel.
- [ ] Profile subscription load failure is distinct from an authoritative empty result and supports retry without stale-state restoration.
- [ ] A known complete profile cannot remain in the completion workflow; unresolved or failed identity does not incorrectly redirect or allow mutation.
- [ ] Forgot-password failures preserve safe rate-limit/server guidance without account enumeration; Profile password-update failures preserve approved bounded guidance.
Performance:
- [ ] Capability retry and Profile retry issue only one request per explicit action and do not create request loops.
- [ ] No new global state, dependency, or redundant polling is introduced.
Quality:
- [ ] Focused page/hook/API tests cover delayed promises, stale/race responses, error/empty separation, route normalization, and safe guidance mapping.
- [ ] Adjacent authStore, guards, Login/Signup/PasswordReset/Profile tests pass.
- [ ] Frontend full tests, coverage, typecheck, ESLint, Prettier, and build pass.
- [ ] Backend full tests/JaCoCo/assemble pass if backend or shared contract files change; otherwise record why frontend-only verification is sufficient before final repository gate.
- [ ] Current auth/Profile/security/UI documentation matches implementation; docs validation and `git diff --check` pass.
- [ ] Independent PG review returns no P1/P2 security finding.

[INPUT POINTERS]
Tier 0 (Constitution - Required for all):
- docs/standards/core-principles.md
- docs/standards/development-standards.md

Tier 1 (Policies - Inferred from auth/security/quality scope):
- docs/policies/security-policy.md
- docs/policies/access-control-policy.md
- docs/policies/quality-gates.md
- docs/standards/evidence-pack-standard.md

Tier 2 (React and current contracts):
- docs/standards/frontend-standards.md
- .agents/skills/react-best-practices/AGENTS.md
- docs/design/api-spec.md
- docs/design/usecase/user-info.md
- docs/ui/atstudio-front-list.md

REQ/Context Docs:
- deliverables/user/REQ-20260809-ATS-001.md
- deliverables/agent/WI-20260809-ATS-022-findings.md
- deliverables/agent/WI-20260809-ATS-022-evidence-pack.md
- deliverables/agent/WI-20260809-ATS-031-consolidated-findings.md
- deliverables/agent/WI-20260809-ATS-036-evidence-pack.md

Files:
- frontend/src/hooks/usePublicCapabilities.ts
- frontend/src/pages/auth/LoginPage.tsx
- frontend/src/pages/auth/SignupPage.tsx
- frontend/src/pages/auth/PasswordResetPage.tsx
- frontend/src/pages/auth/SocialCompleteProfilePage.tsx
- frontend/src/pages/auth/SocialLoginPage.tsx
- frontend/src/pages/subscriber/ProfilePage.tsx
- frontend/src/router/index.tsx
- frontend/src/router/ProtectedRoute.tsx
- frontend/src/store/authStore.ts
- frontend/src/api/auth.ts
- frontend/src/api/client.ts
- corresponding focused and adjacent test files

Repro/Logs:
- Use Vitest deferred promises and MemoryRouter only for mutation/race/route cases.
- Use mocked capability and subscription responses; do not call live auth, OAuth, mail, or persistent data.

[OUTPUT CONTRACT]
User-facing -> deliverables/user/WI-20260809-ATS-042-summary.md:
- Korean summary of the seven corrections, unchanged policy boundaries, verification, and residual decision-held items.
Agent-facing -> deliverables/agent/WI-20260809-ATS-042-evidence-pack.md:
- Root-to-code/test evidence, red/green proof, PG review disposition, commands, rollback, and follow-up chain.
Handoff Packet -> deliverables/agent/WI-20260809-ATS-042-handoff.md:
- This packet.

[TRACEABILITY REQUIREMENTS]
Evidence pointers (files/lines/commands/logs): Required for each `CR-031-008` through `CR-031-014`.
Tests: Record focused RED/GREEN, adjacent auth/account suites, full quality gates, and independent PG review.
Rollback: Revert capability state/presentation, submit fence, Profile route/load/error handling, safe guidance mapping, tests, docs, and WI deliverables as one patch. No data rollback is permitted or expected.
