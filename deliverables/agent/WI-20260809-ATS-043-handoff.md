[WI HEADER]
WI ID: WI-20260809-ATS-043
REQ: REQ-20260809-ATS-001
Agent: se
Depends On: WI-20260809-ATS-036
Blocks: WI-20260809-ATS-057, WI-20260809-ATS-060, WI-20260809-ATS-072

[WI SUMMARY]
Why: `CR-031-002`, `CR-031-005`, `CR-031-047`, and `CR-031-129` identify one bounded routing/recovery group: subscriber and Player guest actions lose their safe origin, SubscriberRoute emits render-time notifications, React Router still reports the v7 transition warning, and a rejected lazy import has no application-owned recovery path.
Scope (in/out):
- In: Give anonymous SubscriberRoute redirects the same validated internal return-target contract as ProtectedRoute, preserving pathname and query while excluding hash under the current policy.
- In: Move SubscriberRoute login/subscription warning side effects out of render and prevent StrictMode or state transitions from duplicating notifications.
- In: Preserve the current safe internal origin when guest Like and Add-to-Playlist actions in desktop and mobile PlayerBar navigate to Login.
- In: Centralize or reuse the existing return-target construction and consumption rules so guards, PlayerBar, and Login cannot drift. Revalidate every consumed target and reject absolute, protocol-relative, malformed, login-loop, or otherwise disallowed destinations.
- In: Adopt the supported React Router future behavior consistently in production and test routers without changing the installed dependency.
- In: Add one localized application-owned lazy-route rejection surface with bounded retry and safe Home/Back recovery. Preserve the current internal URL, do not expose raw import errors, and prevent hard-reload or retry loops.
- In: Add focused and adjacent tests for safe/unsafe origins, query preservation, duplicate warning prevention, desktop/mobile Player actions, import rejection/retry success/repeated failure, route departure, history, and existing 404/500 behavior.
- In: Update current routing/error-recovery/UI documentation and WI evidence when the implementation changes the documented presentation contract.
- Out: OAuth Provider execution, authentication policy, role/access policy, shell-wide keyboard/focus semantics owned by WI-057, page-specific loading recovery, dependency upgrades, backend/API/schema/data changes, and external browser/provider effects.
DoD: All four canonical roots are corrected within the current routing/security policy; redirects are internal and revalidated; render-time store mutations are removed; lazy-route rejection has a bounded product recovery path; focused, adjacent, full frontend gates and documentation validation pass; independent PG review confirms no open-redirect or access-control regression.
Constraints/Forbidden:
- Do not weaken ProtectedRoute, SubscriberRoute, ADMIN, user-type, or subscription enforcement.
- Do not trust query-string return targets, browser referrers, or arbitrary backend input without current-policy validation.
- Do not disclose raw dynamic-import errors, chunk URLs, stack text, tokens, secrets, or local paths.
- Do not add a dependency, upgrade React Router, use unbounded reload loops, or broaden WI-057 shell/accessibility scope.
- Do not perform real OAuth/login, payment, mail, download/export, persistent DB mutation, or inspect ignored local configuration.
- Do not touch, inspect, stage, or delete `output/client-demo-screenshots-20260716-140514.zip` or `output/ui-ux-audit/`.

[ACCEPTANCE CRITERIA]
Functional:
- [ ] Anonymous SubscriberRoute navigation redirects to Login with the current internal pathname and query encoded as `returnTo`; the hash remains excluded by current policy.
- [ ] SubscriberRoute warnings are emitted from an effect at most once per redirect reason under StrictMode and never mutate the toast store during render.
- [ ] Guest desktop/mobile Like and Add-to-Playlist actions preserve the current safe internal origin in the Login target.
- [ ] Login consumes only a revalidated safe internal return target; absolute, protocol-relative, malformed, Login-loop, and protected role-mismatch targets fall back safely.
- [ ] React Router's supported transition future behavior is enabled consistently without changing route semantics or browser history.
- [ ] A rejected lazy page import renders localized application recovery UI, retains the internal URL, retries a bounded fresh load, and offers safe Home/Back recovery without exposing debug text.
- [ ] First-rejection/retry-success, repeated rejection, route departure, reload/deep-link, Back/Forward, unknown public route, and unknown ADMIN route behavior are covered at the appropriate component/router boundary.
Performance:
- [ ] One explicit lazy retry produces one fresh loader attempt; no polling, recursive retry, or reload loop is introduced.
- [ ] Return-target construction does not add global state or repeated listeners.
Quality:
- [ ] Focused route, guard, Login, PlayerBar, and lazy-recovery tests prove RED/GREEN behavior and stale/unmount safety.
- [ ] Adjacent ProtectedRoute, OAuth-attempt validation, 404/500, Header link, and router catalog suites pass.
- [ ] Frontend full tests, coverage, typecheck, ESLint, Prettier, and build pass.
- [ ] Current routing/error-recovery/UI documentation matches implementation; docs validation and `git diff --check` pass.
- [ ] Independent PG review returns no P1/P2 redirect, authentication, authorization, or error-disclosure finding.

[INPUT POINTERS]
Tier 0 (Constitution - Required for all):
- docs/standards/core-principles.md
- docs/standards/development-standards.md

Tier 1 (Policies - Inferred from routing/security/quality scope):
- docs/policies/security-policy.md
- docs/policies/access-control-policy.md
- docs/policies/quality-gates.md
- docs/standards/evidence-pack-standard.md

Tier 2 (React and current contracts):
- docs/standards/frontend-standards.md
- .agents/skills/react-best-practices/AGENTS.md
- docs/ui/screen-flow.md
- docs/ui/atstudio-front-list.md
- docs/design/api-spec.md
- docs/design/usecase/user-info.md

REQ/Context Docs:
- deliverables/user/REQ-20260809-ATS-001.md
- deliverables/agent/WI-20260809-ATS-021-findings.md
- deliverables/agent/WI-20260809-ATS-024-findings.md
- deliverables/agent/WI-20260809-ATS-030-findings.md
- deliverables/agent/WI-20260809-ATS-031-consolidated-findings.md
- deliverables/agent/WI-20260809-ATS-036-evidence-pack.md

Files:
- frontend/src/router/index.tsx
- frontend/src/router/SubscriberRoute.tsx
- frontend/src/router/ProtectedRoute.tsx
- frontend/src/pages/auth/LoginPage.tsx
- frontend/src/layouts/PlayerBar.tsx
- frontend/src/utils/oauthAttempt.ts
- frontend/src/pages/error/NotFoundPage.tsx
- frontend/src/pages/error/ServerErrorPage.tsx
- corresponding focused and adjacent test files

Repro/Logs:
- Use Vitest deferred promises, rejected dynamic-import loaders, MemoryRouter/createMemoryRouter, and mocked auth/player stores only.
- Use a safe local pathname/query as the positive origin and absolute, protocol-relative, malformed, Login-loop, and restricted route values as adversarial inputs.
- Do not invoke external OAuth, authenticated ADMIN browser flows, or any server mutation.

[OUTPUT CONTRACT]
User-facing -> deliverables/user/WI-20260809-ATS-043-summary.md:
- Korean summary of safe return navigation, lazy-route recovery, unchanged access policy, verification, and residual boundaries.
Agent-facing -> deliverables/agent/WI-20260809-ATS-043-evidence-pack.md:
- Root-to-code/test evidence, red/green proof, PG review disposition, commands, rollback, and follow-up chain.
Handoff Packet -> deliverables/agent/WI-20260809-ATS-043-handoff.md:
- This packet.

[TRACEABILITY REQUIREMENTS]
Evidence pointers (files/lines/commands/logs): Required for `CR-031-002`, `CR-031-005`, `CR-031-047`, and `CR-031-129`.
Tests: Record focused RED/GREEN, safe/adversarial return-target cases, lazy rejection/retry cases, adjacent routing suites, full quality gates, and independent PG review.
Rollback: Revert return-target helper/consumers, SubscriberRoute effect handling, PlayerBar guest navigation, router future configuration, lazy-recovery boundary, tests, docs, and WI deliverables as one patch. No data rollback is permitted or expected.
