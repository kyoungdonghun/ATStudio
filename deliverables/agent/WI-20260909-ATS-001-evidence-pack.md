---
version: 1.1
last_updated: 2026-09-09
project: ATS
owner: PG
category: reference
status: active
---

# Evidence Pack: WI-20260909-ATS-001

## Summary
Implemented SEC-01/02/03/05 and AUTH-06 within the approved ownership boundary. MA's initial focused compilation/run passed in 1m 7s: nine authentication-related suites, 84 tests, zero failures/errors/skips. This is evidence for that compilation checkpoint only, not the final full/coverage gate. PG started no runner.

## Scope / DoD Check
- [x] Strict access/refresh purpose validation, mandatory refresh issuance identity, no typeless compatibility fallback.
- [x] Validation log minimization, including original/cause/suppressed throwable containment through fallback handlers.
- [x] Password-reset lock ordering and same-transaction token/password/session mutation; existing schema retained.
- [x] Password-user verification preserved; passwordless OAuth lifecycle can refresh without asserting email verification.
- [x] Real signed-token, fixed-clock, captured-log and independent-persistence-context regression tests authored.
- [x] MA initial focused checkpoint verified against its preserved log and exact suite-count JSON.
- [ ] Captured-log regression and remaining requested MVC suite, full unchanged coverage gates, and independent WI010 review executed and recorded.
- [ ] Original-defect negative execution independently established; corrected-token/reset scenarios passed at the initial checkpoint, but no original-source mutation run is claimed.

## Reference Documents (Tier 0-2)
| Tier | Document | Use |
|---|---|---|
| 0 | `docs/standards/core-principles.md` | Approved bounded scope, preservation, simplicity |
| 0 | `docs/standards/development-standards.md` | Java17, service transactions, meaningful regression |
| 0 | `docs/standards/documentation-standards.md` | Metadata, exact evidence and history boundaries |
| 0 | `docs/standards/glossary.md` | Existing streaming/download semantics unchanged |
| 1 | `docs/policies/security-policy.md` | Credentials/PII omission from logs |
| 1 | `docs/policies/quality-gates.md`; `build.gradle:76` | Critical classes and unchanged 100% line/method rules |
| 2 | `deliverables/user/REQ-20260909-ATS-001.md` | Approved implementation; serialized MA runners |
| 2 | `deliverables/agent/WI-20260909-ATS-001-handoff.md` | Existing skill-generated handoff, ownership and output contract |
| 2 | `deliverables/agent/WI-20260908-ATS-018-findings.md`; `WI-20260908-ATS-015-evidence-pack.md` | SEC/AUTH root causes and historical audit boundaries |
| Skill | `.agents/skills/create-wi-evidence-pack/SKILL.md` | Two-set evidence format; handoff existence verified before writing |

Injection: MA supplied Tier 0 summary and PG security scope; relevant live documents/code were checked. No subdelegation or new app task. No current-state claim is derived solely from historical audit evidence.

## Evidence Pointers
All paths below are relative to `C:/Users/jm991/Desktop/project/ATStudio`.

### Exact Product Files Changed
| File | Finding / correction |
|---|---|
| `src/main/java/com/atstudio/atstudio/security/JwtTokenProvider.java:24` | SEC-01/03: existing Spring constructor retained; package-local fixed-clock constructor for deterministic tests |
| `src/main/java/com/atstudio/atstudio/security/JwtAuthenticationFilter.java:34` | SEC-01: API Bearer validates access purpose only; role still resolved through current user details |
| `src/main/java/com/atstudio/atstudio/service/auth/AuthService.java:81` | SEC-01/03, AUTH-06: refresh-purpose validation before repository access; prior stored-hash guard retained; password verification conditional only for refresh of password-bearing accounts |
| `src/main/java/com/atstudio/atstudio/service/EmailService.java:94` | SEC-05: reset issuance locks the user before replacing reset tokens; consumption resolves owner ID, locks user then token, checks used/expiry and mutates atomically |
| `src/main/java/com/atstudio/atstudio/repository/PasswordResetTokenRepository.java:17` | SEC-05: scalar owner lookup avoids preloading stale token; explicit pessimistic token lookup scoped to owner |
| `src/main/java/com/atstudio/atstudio/common/exception/GlobalExceptionHandler.java:33` | SEC-02: bounded allowlisted field names/categories/counts; no original messages, rejected values or throwable arguments in any handler |

JWT details: `JwtTokenProvider.java:44,57,58,90,94,98` supplies `token_use=access/refresh`, refresh UUIDv4 `jti`, typed validators and positive numeric subject/expiry checks. Wrong purpose is INVALID even when expired; matching expired purpose remains EXPIRED. Normal configured TTLs and role issuance are unchanged.

Lock details: `EmailService.java:146` obtains scalar owner ID without materializing User/PasswordResetToken, acquires existing user lock, then a current pessimistic token read. Used/expiry checks occur only after locks. Issuance follows the same User -> PasswordResetToken order, avoiding a token-delete/FK-lock inversion. No new entity fields, DDL or retained-data repair.

Log details: `GlobalExceptionHandler.java:76,82,133,137,141` logs field names only from a static allowlist, with all other names represented as `other`. Counts and error categories remain. Wrapping validation in a business/technical/unexpected exception cannot reintroduce throwable logging. Response status/error codes/messages retain existing mappings. Infrastructure/APM/access-log behavior is not established here.

### Exact Test Files Changed
| File | Regression contract |
|---|---|
| `src/test/java/com/atstudio/atstudio/security/JwtTokenProviderTest.java` | Existing critical-class cases use access validator; expired access fixture no longer uses refresh and has a nonzero past offset |
| `src/test/java/com/atstudio/atstudio/security/JwtAuthenticationFilterTest.java` | Existing filter mocked contracts now require access validation |
| `src/test/java/com/atstudio/atstudio/service/auth/AuthServiceTest.java` | Existing login/refresh/logout/digest negative coverage retained; mocks use refresh validator |
| `src/test/java/com/atstudio/atstudio/service/EmailServiceTest.java` | Existing synthetic mail cases retained; reset assertions verify owner -> user -> token -> encoder ordering |
| `src/test/java/com/atstudio/atstudio/security/JwtTokenPurposeTest.java:45` | NEW: all roles/TTLs, 100 fixed-instant unique refresh tokens/UUIDs, wrong/absent/numeric purpose, expired wrong purpose, malformed input, absent expiry/ID and signature negatives |
| `src/test/java/com/atstudio/atstudio/security/AuthTokenSecurityIntegrationTest.java:80` | NEW: real JWT generation plus actual MVC security chain; prior typed refresh hash replay, rotation/logout/reset Bearer negatives, normal access retained, exact stored access/typeless hash rejected, unverified social profile lifecycle |
| `src/test/java/com/atstudio/atstudio/service/PasswordResetConsumptionIntegrationTest.java:61` | NEW: H2 independent service transactions, barrier before real user lock, distinct persistence contexts, one successful consume/encode, loser INVALID_TOKEN, rollback/retry and invalid/used/expired negatives |
| `src/test/java/com/atstudio/atstudio/common/exception/SensitiveValidationLogTest.java:62` | NEW: raw Logback arguments/formatted output/throwable proxy checked for synthetic password/email/phone/token/cause markers, dynamic field paths, malformed body and wrapped failures |

The MVC lifecycle suite deliberately mocks the lifecycle UserRepository/OAuth/mail dependencies while using the actual HTTP security chain. The separate H2 suite exercises real transactional reset persistence and repositories; these are complementary, not a claim that MVC reset tests use a real DB or Provider.

### Two-Set Reports
- `deliverables/agent/WI-20260909-ATS-001-evidence-pack.md` (this file).
- `deliverables/user/WI-20260909-ATS-001-summary.md`.

## Commands & Outputs
- Executed: scoped `Get-Content`/`rg` reads, `apply_patch` source/test/report edits, and source-call-site search.
- `rg -n "validateToken\(" src/test src/main/java`: only the private purpose-parameterized helper and its two typed callers remain; no old generic validator consumer.
- No Gradle/npm/compiler/test runner was started by PG. No Git operation, live account/reset/mail/payment/provider action, credential inspection, DB/DDL/runtime change or subdelegation occurred.
- MA received stable-source/test readiness and the exact 10-class request before evidence authoring. No product/test edits were made after that readiness notification; this revision updates WI reports only.

## Tests: MA Initial Checkpoint
Evidence: `output/release-remediation-20260909/focused-initial.log` records `compileJava`, `compileTestJava`, `test` and `BUILD SUCCESSFUL in 1m 7s`; `focused-initial-results.json` in the same directory supplies the following exact suite rows.

| JSON suite (verbatim) | Tests | Failures | Errors | Skipped |
|---|---:|---:|---:|---:|
| GlobalExceptionHandler 통합 테스트 | 9 | 0 | 0 | 0 |
| JwtConfig validation | 4 | 0 | 0 | 0 |
| com.atstudio.atstudio.security.AuthTokenSecurityIntegrationTest | 6 | 0 | 0 | 0 |
| com.atstudio.atstudio.security.JwtAuthenticationFilterTest | 4 | 0 | 0 | 0 |
| JwtTokenProvider 단위 테스트 | 9 | 0 | 0 | 0 |
| com.atstudio.atstudio.security.JwtTokenPurposeTest | 17 | 0 | 0 | 0 |
| AuthService 단위 테스트 | 17 | 0 | 0 | 0 |
| EmailService 단위 테스트 | 15 | 0 | 0 | 0 |
| com.atstudio.atstudio.service.PasswordResetConsumptionIntegrationTest | 3 | 0 | 0 | 0 |
| Authentication-related subtotal | 84 | 0 | 0 | 0 |

- Full initial JSON: 17 suites, 184 tests, zero failures/errors, one skip (LocalStorageServiceTest, outside this WI); therefore 183 non-skipped tests, not 184 passed tests.
- `SensitiveValidationLogTest` and `SecurityFilterChainTest` are absent from this initial snapshot. In particular, `GlobalExceptionHandler*Test` does not select `SensitiveValidationLogTest`; SEC-02's new captured-log assertions remain unverified by this run. MA must include both missing classes in the next serialized run or full suite.
- Corrected source's real-token purpose/rotation/revocation/social lifecycle and H2 reset consumption/rollback scenarios passed as listed. Actual MySQL, external services, final source state and aggregate coverage are not proved by this checkpoint.

### Original Requested Selection
The following ten-class request remains a reproduction/remaining-selection pointer, not a claim that MA ran this exact command in the initial checkpoint:
```powershell
.\gradlew.bat test --tests "*.JwtTokenProviderTest" --tests "*.JwtTokenPurposeTest" --tests "*.JwtAuthenticationFilterTest" --tests "*.AuthServiceTest" --tests "*.AuthTokenSecurityIntegrationTest" --tests "*.EmailServiceTest" --tests "*.PasswordResetConsumptionIntegrationTest" --tests "*.SensitiveValidationLogTest" --tests "*.GlobalExceptionHandlerTest" --tests "*.SecurityFilterChainTest"
```
- Subsequent unchanged full backend gate: MA owns `test`, `jacocoTestReport`, `jacocoTestCoverageVerification` and build orchestration. A focused subset alone cannot establish the bundle gate.
- `build.gradle` remains untouched: JwtTokenProvider/JwtAuthenticationFilter/AuthService 100% LINE and METHOD requirements are not relaxed. Existing expiry-tolerant reader, digest-unavailable and session-negative tests remain.
- Original-defect sensitivity: absent purpose/refresh-as-Bearer tests expose SEC-01; fixed-clock uniqueness exposes SEC-03; captured rejected-field/throwable markers expose SEC-02; the barrier puts both old reset transactions after stale token reads and before the user lock, exposing SEC-05; passwordless unverified refresh exposes AUTH-06. This is test-design reasoning, not an executed mutation test.

## Risks / Rollback
- Deployment intentionally rejects old typeless access AND refresh tokens. Existing sessions must log in again; permitting legacy tokens would reopen SEC-01. The pinned running backend was not replaced or restarted.
- Newly issued access tokens keep normal TTL and current DB-role semantics. Logout/reset revoke refresh capability, not otherwise-valid access tokens; no new global-session revocation policy was introduced.
- Social refresh relies on the existing `password == null` OAuth-account representation plus signed refresh and exact stored hash. It neither accepts an email as identity nor marks provider email verified, auto-links accounts, or enables providers. Password login remains verification-gated.
- Reset issuance holds its existing transaction's user lock while synthetic/current mail delivery runs; slow SMTP can prolong that lock. No asynchronous mail architecture is introduced in this bounded fix.
- The executed H2 reset results do not prove actual MySQL isolation/interleavings, SMTP, social providers, deployed proxy, or infrastructure log collection.
- Rollback requires MA's scoped patch handling and verification; never restore typeless acceptance as a compatibility workaround. No data rollback or history cleanup is needed because no retained runtime data was touched.

## Follow-ups / Chain
- No unresolved implementation choice within PG ownership. Initial selected tests have no failures; MA retains missing-suite/full-gate/coverage feedback and independent review responsibility. Do not weaken tests/gates.
- Handoff lists Blocks 010/012/014; approved REQ specifically joins WI001 + WI004 before WI010 authentication review. MA owns scheduling those dependencies, WI006 backend gates and WI014 completion.
- This is implementation delivery pending verification, not production GO or closure of the parent REQ.

## Revision History
- 1.0: implementation delivered with unexecuted test request.
- 1.1: recorded MA initial focused checkpoint from preserved log/JSON; explicitly retained missing-suite, final-gate and coverage boundaries.
