
> Historical public derivative, published in this working tree on 2026-09-09 by WI-20260909-ATS-018. Original dates, language, status, decisions and reported outcomes below are historical, not current approval or reverified recovery/production evidence. The original remains unchanged in private archive `v1-artifacts-20260909-084405`. See [A004: original identity, public SHA-256 and review](../../docs/registry/development-history-recovery-20260909.md#a004). Raw/local-only evidence pointers are lookup keys, not files supplied by a source checkout.

[WI HEADER]
WI ID: WI-20260809-ATS-068
REQ: REQ-20260809-ATS-001
Agent: se
Depends On: -
Blocks: -
Execution State: READY

[WI SUMMARY]

Approval Status:

- Parent REQ `REQ-20260809-ATS-001` is approved.
- The user has approved the consent-persistence schema and the existing DB patch
  for this WI only. Do not apply the patch to any database as part of this WI.

Why:

- Password-account registration needs durable evidence of required Terms and
  Privacy consent, an optional Marketing consent, and a consistent contract
  across registration, verification, session issuance, refresh, and logout.

Scope (in):

- Persist the user-consent schema through the approved existing DB patch and
  matching application persistence model.
- Extend the password-registration request and validation contract with required
  Terms and Privacy consent plus optional Marketing consent, and persist the
  accepted values atomically with registration.
- Block password-session issuance and refresh for an unverified account. Use
  the established error-handling pattern without leaking tokens or account
  state beyond the approved contract.
- Keep the browser change limited to the email-verification registration flow:
  collect the approved consents, navigate to the existing email-verification
  UI after registration, and do not establish a password session before
  verification.
- Correct logout so the server revocation result is obtained before local
  session cleanup, and update only the allowed callers to consume the safe
  result consistently.

Scope (out):

- All social-login, OAuth, social-profile-completion, and social-login caller
  modifications, including `SocialLoginPage`.
- Real email delivery, external OAuth or Provider calls, production/staging
  database work, database patch application, deployment, and any other real
  external side effect.
- Secrets, ignored local configuration, dependencies, architecture changes,
  unrelated authentication behavior, and product or documentation changes
  outside the implementation and evidence deliverables required by this WI.

DoD:

- [ ] The approved consent schema and existing DB patch are represented by the
      entity/repository contract without executing the patch against a database.
- [ ] Registration rejects absent or false required Terms/Privacy consent,
      accepts optional Marketing consent, and persists the approved values
      atomically with the new password user.
- [ ] An unverified password user cannot obtain an access/refresh token at
      login or refresh; verified password users preserve the established token
      contract.
- [ ] The registration UI remains email-verification-only and does not create
      a password session before successful verification.
- [ ] Logout attempts server-side revocation first, exposes a safe outcome to
      its allowed callers, and leaves no local session after the defined terminal
      outcome.
- [ ] No social-login source, real external service, secret, or database is
      touched.

[ACCEPTANCE CRITERIA]

Functional:

- [ ] The backend registration request exposes required Terms and Privacy
      consent and optional Marketing consent using the approved names/types;
      invalid required consent follows the existing validation/error contract.
- [ ] Consent persistence is covered for both accepted and omitted Marketing
      consent and for rejected required consent.
- [ ] Password login checks verified status before generating or storing any
      refresh-token material; refresh applies the same verified-status gate
      before rotation.
- [ ] The signup page submits the approved consent contract, prevents submission
      until required consent is present, and routes a successful registration to
      the existing email-verification experience without staging credentials.
- [ ] Logout callers in scope distinguish the server-confirmed outcome from a
      failed server request while applying the approved local cleanup behavior.
- [ ] Social login/OAuth code and its tests remain unchanged.

Performance:

- [ ] Consent validation and verified-status checks add no network call or
      unbounded processing to registration, login, refresh, or logout.
- [ ] The existing one-user persistence and refresh-token rotation boundaries
      remain intact.

Quality:

- [ ] Focused backend controller/service/entity tests cover registration
      consent, unverified login, unverified refresh, verified refresh, and
      logout outcomes.
- [ ] Focused frontend API/store/signup/email-verification tests cover consent
      payload and validation, no pre-verification session, and allowed logout
      caller behavior.
- [ ] Backend and frontend quality commands specified by the parent REQ pass,
      with exact commands and results recorded in the Evidence Pack.
- [ ] `git diff --check` passes; test evidence distinguishes repository tests
      from any non-run external or database operation.

[INPUT POINTERS]

Tier 0 (Constitution - Required):

- `docs/standards/core-principles.md`

Tier 0 (Standards - Required for se):

- `docs/standards/development-standards.md`

Tier 1 (Policies - Inferred):

- `docs/policies/security-policy.md`
- `docs/policies/access-control-policy.md`
- `docs/policies/quality-gates.md`

Tier 2 (Technology / Domain - Inferred):

- `.agents/skills/react-best-practices/AGENTS.md`
- `docs/standards/frontend-standards.md`
- `docs/design/api-spec.md`
- `docs/design/db-schema.md`

REQ / Context:

- `deliverables/user/REQ-20260809-ATS-001.md`
- `deliverables/agent/WI-20260809-ATS-067-handoff.md` (previous completed WI;
  no implementation dependency)

Implementation Roots:

- `src/main/resources/schema.sql`
- `src/main/java/com/atstudio/atstudio/entity/User.java`
- `src/main/java/com/atstudio/atstudio/dto/user/RegisterRequest.java`
- `src/main/java/com/atstudio/atstudio/controller/UserController.java`
- `src/main/java/com/atstudio/atstudio/service/UserService.java`
- `src/main/java/com/atstudio/atstudio/controller/AuthController.java`
- `src/main/java/com/atstudio/atstudio/service/auth/AuthService.java`
- `frontend/src/api/auth.ts`
- `frontend/src/store/authStore.ts`
- `frontend/src/pages/auth/SignupPage.tsx`
- `frontend/src/pages/auth/EmailVerifyPage.tsx`

Test Roots:

- `src/test/java/com/atstudio/atstudio/controller/AuthControllerTest.java`
- `src/test/java/com/atstudio/atstudio/service/auth/AuthServiceTest.java`
- `src/test/java/com/atstudio/atstudio/service/UserServiceTest.java`
- `frontend/src/api/authContracts.test.ts`
- `frontend/src/api/auth.test.ts`
- `frontend/src/store/authStore.test.ts`
- `frontend/src/pages/auth/SignupPage.test.tsx`
- `frontend/src/pages/auth/EmailVerifyPage.test.tsx`

Repro / Logs:

- Do not run or apply a DB patch. Record the patch as repository evidence only.
- Record focused test commands and results, followed by the applicable parent
  REQ quality commands.

[OUTPUT CONTRACT]

User-facing -> `deliverables/user/WI-20260809-ATS-068-summary.md`:

- Summary of consent contract, verification/session behavior, logout outcome,
  residual risks, and any required approval point.

Agent-facing -> `deliverables/agent/WI-20260809-ATS-068-evidence-pack.md`:

- File and line evidence for the schema patch, persistence, API/UI contract,
  token gates, allowed logout callers, test commands/results, external-side-
  effect non-execution, rollback, and follow-up WI.

Handoff Packet -> `deliverables/agent/WI-20260809-ATS-068-handoff.md`:

- This packet for traceability.

[TRACEABILITY REQUIREMENTS]

Evidence pointers (files/lines/commands/logs): Required.

Tests:

- Record focused backend and frontend tests plus all applicable parent REQ
  quality gates, including exact commands, results, and any justified omission.

Rollback:

- Revert only the WI-specific repository patch. Do not use a database rollback
  because this WI must not apply the DB patch.

External-side-effect boundary:

- Record `NOT RUN` for database patch application, real email delivery, OAuth,
  social login, and other external calls. Never expose secrets.
