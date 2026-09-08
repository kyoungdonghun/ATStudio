---
version: 1.0
last_updated: 2026-09-08
project: ATS
owner: PG
category: evidence-pack
status: confirmed
dependencies:
  - path: WI-20260908-ATS-015-handoff.md
    reason: Approved bounded review scope
  - path: ../user/REQ-20260908-ATS-004.md
    reason: Review-only authorization
---

# Evidence Pack: WI-20260908-ATS-015

## Summary
Review complete on main `8161f0a00b088001a37962da719c3ace8fea44ad`: **2 P1 release blockers and 4 P2 concrete defects**. No P0 found. This is static source/test-substance and cached-bytecode evidence, not an executed exploit, fresh test PASS, deployment inspection, or release approval.

## Scope / DoD Check
- [x] Read supplied ordered Tier 0-2 pointers and scoped implementation/test assertions; applied PG role and vercel-react-best-practices (effect ownership/dependencies, existing request coalescing).
- [x] Trace registration fields, refresh/logout/password recovery, OAuth identity, role/ownership, log PII, browser state, XSS and redirects; consider existing defenses for each finding.
- [x] Preserve source/tests/config, Git state, historical deliverables and external data; write only the two contracted outputs.
- [x] Supply bounded existing-test commands and missing behavioral candidates; no Gradle/npm, runtime, provider, SMTP, credentials or subdelegation used.
- P1-01 and P1-02 were reported immediately in commentary after confirmation. `send_input` is not available in this task's tool inventory; no substitute task was created.

## Reference Documents (Tier 0-2)
| Tier | Injected/read context | Use |
|---|---|---|
| 0 | `docs/standards/core-principles.md` | Approval exception for explicit delegation; transparency and scoped work |
| 0 | `docs/standards/documentation-standards.md` | Metadata, English deliverables, traceable pointers |
| 0 | `docs/standards/development-standards.md` | DTO/service/security and evidence boundaries |
| 0 | `docs/standards/glossary.md` | Authentication, Company Certification, Public Listening versus Official Download |
| 1 | `docs/policies/security-policy.md:26-47,96-174,206-208`; `docs/policies/quality-gates.md:27-58` | PII, auth, replay and environment holds |
| 2 | `docs/index.md:76-81`; `docs/SR/SR-93.md:57-74`; `deliverables/user/REQ-20260908-ATS-004.md` | Source baseline and OPEN production gates |
| 2 | `docs/design/api-spec.md:560-620`; `docs/design/usecase/user-info.md:8-174,307-431`; `docs/ui/screen-flow.md:57-95` | Product requirements and UI contracts |
Injection rule source: `.claude/config/context-injection-rules.json`; assignee PG, task security/review, required tiers 0/1 plus supplied Tier 2. Handoff and `.agents/skills/create-wi-evidence-pack/SKILL.md` precondition satisfied; project tag verified in `.claude/config/workspace.json`.

## Findings
All repository pointers below resolve under `C:/Users/jm991/Desktop/project/ATStudio`. "Confirmed" means the stated source path is present; production execution/occurrence is not claimed.

### P1-01: Refresh JWT bypasses API token-purpose and revocation boundaries
- **Path:** `src/main/java/com/atstudio/atstudio/security/JwtAuthenticationFilter.java:34-43` accepts every signature/expiry-valid JWT. `src/main/java/com/atstudio/atstudio/security/JwtTokenProvider.java:39-54,74-81` signs refresh tokens with the same key and validates without purpose. `src/main/java/com/atstudio/atstudio/security/CustomUserDetailsService.java:27-33` checks user existence/deletion, not the stored refresh hash.
- **Trigger/impact:** Obtain a legitimate user's refresh JWT, then submit it as an API Bearer token, including after logout, password reset or replacement login/rotation cleared/replaced its hash. It still authenticates through its own refresh expiry, exposing that user's protected profile/actions, or current ADMIN actions for an ADMIN account. No signature forgery or browser-state modification is needed.
- **Defenses checked:** `src/main/java/com/atstudio/atstudio/service/auth/AuthService.java:91-109,115-118`, `src/main/java/com/atstudio/atstudio/service/EmailService.java:155-160`, `src/main/java/com/atstudio/atstudio/service/UserService.java:257-267` lock/clear/check the hash only on their own paths. DB role reload prevents stale-role escalation; deleted users are refused. None rejects refresh JWTs in the API filter.
- **Evidence/gap:** `src/test/java/com/atstudio/atstudio/security/JwtAuthenticationFilterTest.java:24-28,55-74` mocks VALID; `src/test/java/com/atstudio/atstudio/service/auth/AuthServiceTest.java:366-382` checks only POST-refresh logic after logout. Neither tests a real refresh JWT as API Bearer.
- **Disposition:** Release blocker. Enforce explicit access-token purpose at the API boundary and distinct refresh purpose at refresh validation; negatively test revoked refresh Bearer use. Do not treat normal access-token TTL retention as an additional finding.

### P1-02: Validation errors emit raw passwords/PII to application logs
- **Path:** `src/main/java/com/atstudio/atstudio/common/exception/GlobalExceptionHandler.java:71-75` logs both `ex.toString()` and the throwable for validation failures. `src/main/java/com/atstudio/atstudio/dto/user/RegisterRequest.java:33-44`, `src/main/java/com/atstudio/atstudio/dto/user/UpdatePasswordRequest.java:19-21`, `src/main/java/com/atstudio/atstudio/dto/auth/ResetPasswordRequest.java:17-19` put password/email/phone inputs into field validation.
- **Trigger/impact:** A request with a short/overlong password, invalid email or phone fails validation before service execution; the rejected value is serialized into the WARN event/stack trace. This creates an application-level credential/PII disclosure to log consumers, even though the HTTP body is generic.
- **Defenses checked:** Fixed client messages at `src/main/java/com/atstudio/atstudio/common/exception/GlobalExceptionHandler.java:143-150`, frontend `frontend/src/api/authError.ts:12-26`, and mail-only minimization at `src/main/java/com/atstudio/atstudio/service/EmailService.java:165-182` do not sanitize this logger. No claim is made about actual retained production logs or downstream collector redaction.
- **Evidence/gap:** Read-only `javap -c` on cached Spring 7.0.3 confirms `MethodArgumentNotValidException.getMessage()` appends all errors and `FieldError.toString()` includes `rejectedValue`. `src/test/java/com/atstudio/atstudio/common/exception/GlobalExceptionHandlerTest.java:114-120,164-173` asserts safe responses, not captured validation logs.
- **Disposition:** Release blocker. Log bounded error categories/field names without rejected values or raw throwable details; capture a synthetic password/email/phone marker in offline validation tests and assert its absence.

### P2-03: Refresh rotation can reissue the identical capability within one second
- **Path:** `src/main/java/com/atstudio/atstudio/security/JwtTokenProvider.java:39-47` contains only subject/issuedAt/expiry, no per-issuance random ID. Cached JJWT 0.12.5 `JwtDateConverter.applyTo(Date)` divides milliseconds by 1000. With unchanged key/TTL and equal serialized timestamps, the signed token is identical.
- **Trigger/impact:** Issue and rotate for the same user within the same timestamp bucket, or logout and log in again within it. The "old" token can remain/become the current stored capability, so same-second stale refresh replay is accepted. This persists independently after fixing P1-01.
- **Defenses checked:** `src/main/java/com/atstudio/atstudio/service/auth/AuthService.java:91-109` user locking and SHA-256 comparison serialize updates but cannot distinguish identical tokens.
- **Evidence/gap:** `src/test/java/com/atstudio/atstudio/service/auth/AuthServiceTest.java:268-293` hardcodes different old/new strings; `src/test/java/com/atstudio/atstudio/security/JwtTokenProviderTest.java:48-55` only checks a refresh subject. No real-generator uniqueness/rotation assertion.
- **Disposition:** Security correction before auth release; add a unique refresh issuance identifier and a deterministic equal-timestamp uniqueness/replay regression. Source/bytecode confirmed; no token-generation execution performed.

### P2-04: A late refresh result crosses logout/replacement-session boundaries
- **Path:** `frontend/src/api/client.ts:114-151` reads the current refresh token, awaits a global refresh, then unconditionally persists returned tokens and replays queued requests; `:152-158` can also clear a newer session after old refresh failure.
- **Trigger/impact:** Hold session A's refresh response, finish local logout (including unconfirmed-server logout), optionally log in as B, then resolve/reject A's request. Success restores A tokens or combines them with B's displayed identity and replays requests; failure clears B. This is same-browser asynchronous identity confusion, not forged server roles.
- **Defenses checked:** `frontend/src/store/authStore.ts:101-129,172-177` guards current-user reads with session generation/user ID, but the interceptor writes via `setState` outside that guard. One-replay marker, queue coalescing and `skipAuthReplay` do not bind a request to its initiating session.
- **Evidence/gap:** `frontend/src/api/client.test.ts:30-38,369-441` mocks the auth store and tests coalescing without session replacement. `frontend/src/store/authStore.test.ts:198-258` protects fetchMe results, not interceptor token refresh.
- **Disposition:** Correct session ownership for token persistence, replay/queue membership and failure cleanup; add delayed success/failure after logout, account switch and same-user re-login tests.

### P2-05: Password reset consumes the token outside the locking boundary
- **Path:** `src/main/java/com/atstudio/atstudio/service/EmailService.java:145-160` reads/checks token used/expiry before taking the user lock. `src/main/java/com/atstudio/atstudio/repository/PasswordResetTokenRepository.java:11` is an unlocked read; `src/main/java/com/atstudio/atstudio/entity/PasswordResetToken.java:14-48` has neither versioning nor atomic consume.
- **Trigger/impact:** Two transactions holding the same valid reset token both read used=false before either gets the user lock. They then sequentially acquire that lock and both change the password from their already-validated token state; the later value overwrites the earlier successful reset. Token possession and overlapping requests are prerequisites.
- **Defenses checked:** User `PESSIMISTIC_WRITE` at `src/main/java/com/atstudio/atstudio/repository/UserRepository.java:18-20` protects user writes, not the preceding token decision; used/expiry checks protect sequential replay. The 5-request reset budget does not preclude two requests.
- **Evidence/gap:** `src/test/java/com/atstudio/atstudio/service/EmailServiceTest.java:92-114` uses one token object/one call with mocked repositories. Unsafe ordering is confirmed; no concurrent transaction schedule or production DB was exercised.
- **Disposition:** Make validation and one-time consumption atomic within the same token lock/conditional update; test two independent persistence contexts with exactly one accepted consume. No new database execution is authorized by this report.

### P2-06: The password-verification gate also rejects newly created social sessions
- **Path:** `src/main/java/com/atstudio/atstudio/service/auth/OAuth2Service.java:91-108` creates a password-null user with `src/main/java/com/atstudio/atstudio/entity/User.java:39-41` default isVerified=false. `src/main/java/com/atstudio/atstudio/service/auth/AuthService.java:63-74` issues social tokens, but `:104,121-124` applies the password verification check to every refresh. Profile completion at `src/main/java/com/atstudio/atstudio/entity/User.java:113-120` does not change verification.
- **Trigger/impact:** Create a social user and reach its first refresh, including after completing its profile: it receives EMAIL_VERIFICATION_REQUIRED and the interceptor clears the session. The documented unchanged social flow cannot renew its initial session.
- **Defenses checked:** Required typed provider identity and no automatic email-account linking remain intact. Profile completion and authenticated API access do not supply the missing verification state. Do not "fix" this by blindly treating a provider email as verified.
- **Evidence/gap:** `docs/design/api-spec.md:593-599` describes a password-account gate; `src/test/java/com/atstudio/atstudio/service/auth/AuthServiceTest.java:298-315` only tests an unverified password fixture, while `src/test/java/com/atstudio/atstudio/service/auth/OAuth2ServiceTest.java:247-279` tests existing identity lookup separately.
- **Disposition:** P2 enablement/operability defect, not a claim that a deployed social provider is enabled. Keep password verification and social identity provenance distinct; add social creation/onboarding/refresh lifecycle coverage before enabling that flow.

## Three-Way Requirement / Code / Test Map
| Requirement | Implementation checked | Existing test substance / conclusion |
|---|---|---|
| Registration consent; type-specific profile; no client privilege/certification grant (`docs/design/api-spec.md:575-599`) | `src/main/java/com/atstudio/atstudio/dto/user/RegisterRequest.java:22-66`; `src/main/java/com/atstudio/atstudio/service/UserService.java:78-110`; `src/main/java/com/atstudio/atstudio/common/validation/RegisterProfileValidator.java:19-38`: no role/approval field; forced USER; verification defaults false | `src/test/java/com/atstudio/atstudio/service/UserServiceTest.java:315-345,374-399,449-532`; `src/test/java/com/atstudio/atstudio/controller/UserContractIntegrationTest.java:55-96`: consent/profile negatives, but no extra-field injection assertion. No confirmed mass assignment; broader certification authorization belongs to WI017. |
| Short access / refresh / logout / password gate (`docs/design/api-spec.md:593-599`) | AuthService, JwtTokenProvider, JwtAuthenticationFilter; actual DB role reload | AuthServiceTest stale/expired/hash/deleted checks are meaningful service evidence, but real-token cross-layer gaps expose P1-01/P2-03/P2-06. `docs/design/usecase/user-info.md:82` ambiguously calls both tokens Bearer; it is not evidence that revocation bypass is safe. |
| Reset/verification and PII (`docs/policies/security-policy.md:26-47,223-227`) | `src/main/java/com/atstudio/atstudio/service/EmailService.java:69-83,89-109,141-182,265-274`: UUID tokens, expiry/used checks, generic forgot acceptance, HTML escaping, minimal mail logs | `src/test/java/com/atstudio/atstudio/service/EmailServiceTest.java:119-207` actually captures logs/HTML. Missing token concurrency and endpoint log checks are P2-05/P1-02, not proof from coverage counts. |
| Roles and ownership (`docs/policies/security-policy.md:102-104`; `docs/design/api-spec.md:601-619`) | `src/main/java/com/atstudio/atstudio/controller/UserController.java:34-72,76-101`; `src/main/java/com/atstudio/atstudio/config/SecurityConfig.java:94-103,130-139`; `src/main/java/com/atstudio/atstudio/service/UserService.java:337-359`: principal-derived me ID, ADMIN guards/recheck | `src/test/java/com/atstudio/atstudio/controller/UserControllerTest.java:45-80`; `src/test/java/com/atstudio/atstudio/controller/SecurityFilterChainTest.java:316-400`; `src/test/java/com/atstudio/atstudio/service/UserServiceTest.java:1045-1087`: non-ADMIN denial, me ordering, demotion hash clear, stale-actor rejection. No browser-role authority bypass found. |
| Abuse/PII budgets (`docs/policies/security-policy.md:120-134`) | `src/main/java/com/atstudio/atstudio/security/AuthRateLimitFilter.java:76-99,157-177,202-207`; `src/main/java/com/atstudio/atstudio/security/TrustedClientIdentityResolver.java:26-53`: salted keys, two budgets, trusted-loopback-only validated header | `src/test/java/com/atstudio/atstudio/security/AuthRateLimitFilterTest.java:62-111,159-169,199-236`: spoof fallback, body-not-read, normalization, rotating identifiers, captured minimal logs. Single-server policy fits; actual proxy identity remains unverified. |
| Browser auth/replay/redirect safety (`docs/policies/security-policy.md:140-174`; `docs/ui/screen-flow.md:59-95`) | `frontend/src/utils/loginReturn.ts:36-103`; `frontend/src/utils/oauthAttempt.ts:47-62,87-102`; `frontend/src/pages/auth/LoginPage.tsx:187-222`; `frontend/src/api/authError.ts:12-26`; `frontend/src/hooks/usePublicCapabilities.ts:12-38` | `frontend/src/utils/loginReturn.test.ts:16-31,48-93`, `frontend/src/utils/oauthAttempt.test.ts:41-116`, `frontend/src/api/client.test.ts:369-495`: unsafe URLs rejected, single-use state, one replay; P2-04 is not covered. No raw HTML sink found in reviewed auth paths. |
| OAuth identity (`docs/design/usecase/user-info.md:104-127`) | `src/main/java/com/atstudio/atstudio/service/auth/OAuth2Service.java:64-83,233-337`: server code exchange, required typed ID, provider+ID lookup, duplicate-email rejection; provider email-verification flags are not interpreted | `src/test/java/com/atstudio/atstudio/service/auth/OAuth2ServiceTest.java:55-239,284-314`: null/blank/error/wrong-type rejection. No concrete existing-account email takeover path established; provider PKCE/verified-email behavior and disabled-provider deployment remain environment-conditional. |

## Evidence Pointers / Commands & Outputs
- Created only `deliverables/agent/WI-20260908-ATS-015-evidence-pack.md` and `deliverables/user/WI-20260908-ATS-015-summary.md` with apply_patch. Review-only rollback means reverting these outputs, not product/data changes.
- Executed read-only `rg`, bounded line-numbered `Get-Content`, `git rev-parse HEAD`, `git status --short --branch --untracked-files=no`, and cached-JAR `jar tf` / `javap -c`. Initial bare javap/jar calls were unavailable on PATH; explicit `C:/Program Files/Java/jdk-17/bin/` tools succeeded.
- Cached bytecode targets: Spring 7.0.3 `FieldError.toString` / `MethodArgumentNotValidException.getMessage`; JJWT 0.12.5 `JwtDateConverter.applyTo`. Only inspected bytecode; did not run application code, generate JWTs or run tests.
- Baseline HEAD matches REQ004; tracked worktree was clean at entry. Final scoped checks: see output verification below; unrelated untracked history is intentionally excluded.

## MA-Reported Central Evidence
- MA's final in-task update: fresh `Gradle test --rerun-tasks` on isolated H2/temp roots: 1,708 total, 1,689 passed, 19 skips (18 gated MySQL + 1 platform symlink), 0 failures. Frontend: 112 files, 1,493 passed. These are MA-reported central results; PG did not independently inspect XML/logs.
- MA reports anonymous current-user/admin-reconciliation requests returned 401; exact allowed local/public CORS origins returned 200 with credentials; hostile-origin backend OPTIONS and public GET returned 403. These checks support anonymous/origin rejection, not token-purpose or authenticated session guarantees.
- Public Vite OPTIONS 204 **without ACAO is not a CORS bypass**. Backend `/v3/api-docs` 200 is current DEV Swagger; public-path 200 SPA HTML is not exposed OpenAPI. No authenticated mutation or production proof; no CORS/Swagger finding is raised from those observations.

## Tests (Candidates for MA; Not Executed Here)
Existing isolated units/standalone MockMvc, with repositories/provider/mail mocked where relevant:
`./gradlew.bat test --tests "*.JwtTokenProviderTest" --tests "*.JwtAuthenticationFilterTest" --tests "*.CustomUserDetailsServiceTest" --tests "*.AuthRateLimitFilterTest" --tests "*.AuthServiceTest" --tests "*.OAuth2ServiceTest" --tests "*.EmailServiceTest" --tests "*.UserServiceTest" --tests "*.GlobalExceptionHandlerTest"`
Existing frontend synthetic API/storage tests:
`npm --prefix frontend run test -- src/api/client.test.ts src/store/authStore.test.ts src/api/auth.test.ts src/api/authError.test.ts src/utils/loginReturn.test.ts src/utils/oauthAttempt.test.ts src/pages/auth/SocialLoginPage.test.tsx src/pages/auth/SocialCompleteProfilePage.test.tsx src/hooks/usePublicCapabilities.test.tsx`
- After MA's test-environment preflight, existing `AuthControllerTest`, `UserControllerTest`, `UserContractIntegrationTest`, `SecurityFilterChainTest` add MVC wiring evidence; these load Spring contexts and are not provider/production acceptance.
- Missing candidates, not created: real refresh-as-Bearer after each revocation (P1-01); captured validation markers (P1-02); same-timestamp real refresh uniqueness (P2-03); deferred interceptor completion after session replacement (P2-04); two-transaction reset consume plus invalid/expired/used verification/reset negatives (P2-05); new social-user refresh lifecycle and provider identity/email-negative fixtures (P2-06).
- Current existing suites may pass despite all six findings; mocked generator/user-store/repository boundaries explain why. MA owns execution and any independently reproduced results.

## Risks / Follow-ups
- **Gate:** Resolve P1-01/P1-02 before security release approval. P2-03/04/05 are bounded auth correctness corrections; P2-06 blocks enabling the affected social lifecycle, not a mandatory new launch feature. No optional style cleanup or library/feature redesign proposed.
- Production host/HTTPS/CORS, exact proxy deployment, key rotation/session expiry, access-log/APM redaction, social-provider compatibility and SMTP remain unverified; do not infer production safety or an existing incident from static findings.
- Public full-song listening versus entitled downloads, card-only Toss, single server and retained annual period/monthly reservation policy are unchanged. Detailed financial UI/logic comparison belongs to WI016/MA and is not claimed by WI015.
- WI015 is complete and supplies WI018's dependency. MA owns WI018 integration/central tests and any separate remediation requirement; REQ004 is not closed by this reviewer.

## Output Verification
- Output line counts: evidence 122/150, summary 34/40. Final validation checks file/line pointers, required metadata, whitespace and unchanged tracked state. No source/test/config/Git writes or historical-file deletion.
