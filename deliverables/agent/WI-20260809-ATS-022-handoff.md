[WI HEADER]
WI ID: WI-20260809-ATS-022
REQ: REQ-20260809-ATS-001
Agent: qa-fe
Depends On: WI-20260809-ATS-021
Blocks: WI-20260809-ATS-023

[WI SUMMARY]
Why: Audit the complete authentication and account-entry experience after the public-shell baseline, including safe return targets, validation, pending/error recovery, identity refresh, and Profile behavior, without confusing external-mail or account-data side effects with ordinary browser checks.
Scope (in/out): Execute `AUTH-01` through `AUTH-06` and `MEM-04` from the master matrix. Cover anonymous and existing QA identities, Login, Signup validation/availability, email verification callback states, password-reset request/reset states, social-login capability boundaries, complete-profile guards, Profile tabs and safe bounded updates, refresh/back/deep-link behavior, and representative responsive/accessibility states. Recheck `F-UI-021-002` only as an invariant at Login; do not fix it in this frozen audit. Do not send uncontrolled external mail, create an uncontrolled real identity, delete an account, change secrets, inspect browser/session storage, or invoke payment/provider/file effects.
DoD: Every owned row is `PASS`, `FAIL`, `BLOCKED`, or `N/A`; safe and unsafe Login return targets are evidenced; valid and invalid auth states distinguish frontend validation, API response, and persisted/session state; external-mail-dependent paths are proved with an approved receiver or classified `BLOCKED`; responsive, keyboard, duplicate-submit, reload, and error-recovery behavior is recorded; findings have source expectation and no product fix is applied during the initial audit sequence.
Constraints/Forbidden: Keep baseline `e343c20` and the product-code freeze. Never print or inspect passwords, JWTs, verification/reset tokens, ignored environment files, cookies, local storage, or secret configuration. Use only approved QA identities and sanitized evidence. No uncontrolled Gmail delivery, social-provider authorization, account withdrawal, DB/schema operation, provider call, file transfer, branch operation, or runtime deployment. A normal bounded profile update may run only when its original values and restoration evidence are known; otherwise classify it `BLOCKED`.

[ACCEPTANCE CRITERIA]
Functional:

- [ ] Verify current local/public runtime identity before reusing browser evidence.
- [ ] Check Login empty/invalid/valid/unverified states, pending fence, inline error, already-authenticated routing, safe `returnTo`, and rejection of external/protocol-relative return targets.
- [ ] Check Signup individual/business selection, field boundaries, consent, availability checks, race/stale-result handling, duplicate submit prevention, and optional channel guidance without uncontrolled account or mail creation.
- [ ] Check email-verification missing/invalid/expired/reused/success states where current fixtures safely permit; never expose a token in screenshots, logs, or deliverables.
- [ ] Check password-reset request enumeration resistance, field validation, missing/invalid/expired/reused/success callback states where safe fixtures permit, and Login recovery.
- [ ] Check social-login enabled/disabled/unknown provider and malformed callback behavior without starting an uncontrolled provider authorization.
- [ ] Check complete-profile guard, validation, duplicate fields, pending fence, session refresh, and safe next route for a suitable QA fixture.
- [ ] Check Profile account/subscription presentation, tabs, shortcuts, availability checks, rejected update retention, bounded successful update and restoration if safe, password-update boundaries, reload, and USER/ADMIN routing.
- [ ] Recheck 1440x900, 1024x768, 390x844, and 360x800 for the owned auth/account pages, including long errors, input/button fit, focus, keyboard alternatives, PlayerBar/footer overlap, and horizontal scroll.
- [ ] Capture sanitized UI, request status, canonical reload, and persisted-state evidence appropriate to each executed effect.

Performance:

- [ ] Reuse one controlled browser session and bounded screenshots; avoid duplicate mail/provider attempts and unbounded polling.
- [ ] Treat delayed/stale availability responses and duplicate submission as explicit state tests, not repeated manual retries.

Quality:

- [ ] Separate browser/input automation limitations, environment/fixture gaps, policy boundaries, and product defects.
- [ ] No secret, password, token, raw mail link, or browser storage value appears in evidence.
- [ ] No uncontrolled external side effect or irreversible account mutation occurs.
- [ ] WI deliverables pass Prettier, documentation validation, and whitespace checks.

[INPUT POINTERS]
Tier 0:

- docs/standards/core-principles.md
- docs/standards/development-standards.md

Tier 1:

- docs/policies/security-policy.md
- docs/policies/access-control-policy.md
- docs/policies/quality-gates.md

Tier 2:

- .agents/skills/react-best-practices/AGENTS.md
- docs/standards/frontend-standards.md
- docs/ui/atstudio-front-list.md
- docs/ui/screen-flow.md
- docs/design/usecase/user-info.md
- docs/design/api-spec.md
- scripts/acceptance/

REQ/Context:

- deliverables/user/REQ-20260809-ATS-001.md
- deliverables/agent/WI-20260809-ATS-020-acceptance-matrix.md
- deliverables/agent/WI-20260809-ATS-021-findings.md
- deliverables/agent/WI-20260809-ATS-021-evidence-pack.md
- AGENTS.md

Primary code entry points:

- frontend/src/router/index.tsx
- frontend/src/router/ProtectedRoute.tsx
- frontend/src/router/SubscriberRoute.tsx
- frontend/src/pages/auth/
- frontend/src/pages/subscriber/ProfilePage.tsx
- frontend/src/api/auth.ts
- frontend/src/store/authStore.ts
- src/main/java/com/atstudio/atstudio/controller/AuthController.java
- src/main/java/com/atstudio/atstudio/controller/UserController.java
- src/main/java/com/atstudio/atstudio/service/auth/AuthService.java
- src/main/java/com/atstudio/atstudio/service/EmailService.java
- src/main/java/com/atstudio/atstudio/bootstrap/TestUserBootstrapRunner.java

[OUTPUT CONTRACT]
User-facing -> deliverables/user/WI-20260809-ATS-022-summary.md:

- Runtime/fixture boundary, rows tested, pass/fail/blocked totals, material auth/account findings, external-mail limits, cleanup, and next-WI readiness.

Agent-facing -> deliverables/agent/WI-20260809-ATS-022-evidence-pack.md:

- Sanitized environment preflight, scenario results, screenshots/network/API/persistence evidence, reproduction steps without credentials, finding IDs, cleanup/restoration, rollback, and WI-023 trigger.

Findings -> deliverables/agent/WI-20260809-ATS-022-findings.md:

- One row per defect/drift/blocker with expected source, sanitized actual evidence, severity, classification, adjacent scope, and next verification.

Handoff Packet -> deliverables/agent/WI-20260809-ATS-022-handoff.md:

- This packet.

[TRACEABILITY REQUIREMENTS]
Evidence pointers: Required for every matrix result. Record role/fixture class without credentials, route, viewport, expected source, sanitized request status, canonical page/API state, and restoration evidence when mutation runs.
Tests: Browser/API evidence plus focused existing auth/account tests. External-mail or social-provider behavior is not inferred from a mocked response; classify the unexecuted boundary explicitly.
Rollback: Restore any bounded Profile/account field immediately and verify through canonical reload. Do not create data requiring destructive cleanup without a separately approved cleanup scope.
