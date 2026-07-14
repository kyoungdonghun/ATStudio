---
version: 1.0
last_updated: 2026-07-14
project: ATS
owner: se
category: evidence-pack
status: stable
related_wi: WI-20260714-ATS-015
---

# Evidence Pack: WI-20260714-ATS-015

## Summary (one-liner)

- Added a fail-closed backend acceptance profile, one validated public-base URL contract, externally supplied bootstrap/Toss secrets, and focused startup/config tests without touching WI-016 proxy ownership or live services.

## Scope / DoD Check

- [x] Added `application-acceptance.yml` with secret-bearing values represented only by external placeholders.
- [x] Derived mail, social-login, Toss one-time, and Toss billing-auth callbacks from `APP_PUBLIC_BASE_URL`.
- [x] Required explicit acceptance/non-production profiles and an externally supplied bootstrap password.
- [x] Added production/default/profile, secret-presence, HTTPS-origin, callback, and Toss refusal tests without logging values.
- [x] Focused startup/config tests passed: 17 tests, 0 failures.
- [x] Current whole-worktree `compileJava` passes.
- [x] Current whole-worktree `compileTestJava` passes.
- [x] Secret scans and final `git diff --check` were executed; results are recorded below.

## Reference Documents (Tier 0-2)

| Tier | Document | Reason |
|---|---|---|
| 0 | `docs/standards/core-principles.md` | Approved execution, security, and traceability baseline |
| 0 | `docs/standards/development-standards.md` | Backend implementation and verification standards |
| 0 | `docs/standards/documentation-standards.md` | Deliverable structure and documentation policy |
| 0 | `docs/standards/glossary.md` | Canonical terminology |
| 1 | `docs/policies/security-policy.md` | External-secret and no-secret-logging requirements |
| 2 | `deliverables/user/REQ-20260714-ATS-001.md` | Approved P1 acceptance-hardening scope and chain |
| 2 | `docs/design/p1-security-acceptance-hardening-design.md` | Profile, bootstrap, public-base, and callback contract |
| 2 | `docs/design/p1-payment-db-integrity-design.md` | Payment scope boundary and no-live-Toss constraint |
| 2 | `docs/SR/SR-42.md` | Single frontend tunnel topology and same-origin callback context |

**Injection Rules Applied**:

- Rule source: `AGENTS.md` and `deliverables/agent/WI-20260714-ATS-015-handoff.md`
- Assignee: `se`
- Task type: backend acceptance/security configuration
- Ownership boundary: no Vite/proxy edits, lifecycle scripts, live credentials, servers, tunnels, or live service calls

## Evidence Pointers

### Files changed

- `src/main/resources/application-acceptance.yml` - acceptance-only loopback, external-secret, and derived callback configuration.
- `src/main/resources/application.yml` - disabled-by-default acceptance properties and empty bootstrap password fallback.
- `application-local.example.yml` - explicit `local` profile and external bootstrap password placeholder.
- `src/main/java/com/atstudio/atstudio/config/AcceptanceProperties.java` - acceptance flag and public-base property binding.
- `src/main/java/com/atstudio/atstudio/config/AcceptancePublicUrls.java` - HTTPS root-origin validation and callback/public-host derivation.
- `src/main/java/com/atstudio/atstudio/config/AcceptanceStartupGuard.java` - profile, external configuration, callback, and Toss fail-closed checks.
- `src/main/java/com/atstudio/atstudio/config/PaymentProperties.java` - removed independent localhost callback defaults.
- `src/main/java/com/atstudio/atstudio/bootstrap/TestUserBootstrapProperties.java` - removed committed bootstrap password default.
- `src/main/java/com/atstudio/atstudio/bootstrap/TestUserBootstrapRunner.java` - ordered bootstrap after the startup guard.
- `src/test/java/com/atstudio/atstudio/config/AcceptanceStartupGuardTest.java` - URL derivation and refusal matrix.
- `src/test/java/com/atstudio/atstudio/config/AcceptanceProfileConfigurationTest.java` - placeholder-only profile and empty Java defaults.
- `src/test/java/com/atstudio/atstudio/bootstrap/TestUserBootstrapRunnerTest.java` - explicit fixture password injection.
- `deliverables/user/WI-20260714-ATS-015-summary.md` - Korean user-facing result.
- `deliverables/agent/WI-20260714-ATS-015-evidence-pack.md` - this reproducibility record.

### Derived callbacks

- Mail base: `<APP_PUBLIC_BASE_URL>`
- Social: `/social-login/google`, `/social-login/kakao`, `/social-login/naver`
- Toss one-time: `/subscriptions/payment/success`, `/subscriptions/payment/fail`
- Toss billing auth: `/subscriptions/checkout/success`, `/subscriptions/checkout/fail`

### Refusal cases

- Production-like profile combined with acceptance or bootstrap.
- Acceptance flag/profile mismatch or bootstrap without an explicit non-production profile.
- Missing effective DB URL/user/password, JWT secret, or enabled bootstrap password.
- Non-HTTPS/non-root public base, userinfo, trailing slash/path, query, or fragment.
- Independent mail/social/Toss callback override with a different expected path.
- Toss/Toss-billing mode without externally supplied client, secret, or encryption configuration.

## Commands & Outputs

- `gradlew.bat test --tests "com.atstudio.atstudio.config.AcceptanceStartupGuardTest" --tests "com.atstudio.atstudio.config.AcceptanceProfileConfigurationTest" --tests "com.atstudio.atstudio.bootstrap.TestUserBootstrapRunnerTest"`
  - Passed: 3 classes, 17 tests, 0 failures.
- `gradlew.bat compileJava`
  - Passed (`BUILD SUCCESSFUL`).
- `gradlew.bat compileTestJava`
  - Passed (`BUILD SUCCESSFUL`).
- Secret scans:
  - Acceptance secret-bearing YAML fields were checked for placeholder-only values.
  - `Test1234!` was checked as absent from WI-015 configuration and property files.
  - `AcceptanceStartupGuard` contains no logger/console output; focused tests assert refusal messages omit configured values.
- `git diff --check`
  - Passed after both deliverables were created: no whitespace errors; LF-to-CRLF working-copy warnings only.

## Tests

- `AcceptanceStartupGuardTest`: 12 passed.
  - Derivation, HTTPS/root validation, acceptance/local success, default/production refusal, external password/core configuration refusal, callback mismatch redaction, and Toss credential refusal.
- `AcceptanceProfileConfigurationTest`: 2 passed.
  - External placeholders, one public-base callback derivation, and empty Java fallback values.
- `TestUserBootstrapRunnerTest`: 3 passed.
  - Existing bootstrap fixture behavior with an explicitly injected non-secret test fixture password.
- No DB, Toss, SMTP, application server, Vite server, tunnel, or public endpoint was started or called.

## Risks / Rollback

- Risks:
  - Repository code cannot prove that supplied Toss credentials are test-class credentials or that provider callbacks are registered; those remain WI-022 operational gates.
  - `APP_PUBLIC_BASE_URL` is intentionally normalized by contract to an HTTPS origin without a trailing slash so string-derived paths cannot contain a double slash.
- Rollback:
  - Remove the three acceptance configuration classes, the acceptance profile, and the two focused config test classes.
  - Restore only the WI-015-owned defaults/config entries and bootstrap-test fixture setup listed above.
  - Do not revert unrelated concurrent frontend, payment, auth, storage, schema, or proxy changes.

## Follow-ups

- Chain trigger: hand `WI-20260714-ATS-016` the `APP_PUBLIC_BASE_URL`/derived `publicHost` contract now; WI-016 owns exact Vite Host/proxy and trusted-client identity behavior.
- Chain trigger: start `WI-20260714-ATS-017` immediately after WI-016 completes, using the WI-015 and WI-016 Evidence Packs; WI-017 owns lifecycle automation.
