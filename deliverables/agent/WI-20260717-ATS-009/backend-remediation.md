---
version: 1.0
last_updated: 2026-07-17
project: ATS
owner: se / re
category: remediation-evidence
status: passed
dependencies:
  - ../WI-20260717-ATS-009-handoff.md
  - ../WI-20260717-ATS-008/backend-qa.md
---

# WI-20260717-ATS-009 Backend Remediation

## Verdict

**PASS.** WI-008 P2, "Critical security-path coverage is below the mandatory
100% gate and is not enforced," is **FIXED**. The seven governed classes have
100% executable line and method coverage, the class rule fails closed below
100%, the global 80% line / 80% method / 70% branch rule remains unchanged,
and the required clean Gradle gate passes.

No production class was excluded. No trivial assertion-only test was added.
No `application-local.yml` or secret was read or printed. No Git ref/index or
external database was mutated.

## Governed Critical Path

`docs/standards/development-standards.md:592-603` requires 100% coverage for
security-sensitive authentication, authorization, and data-sanitization code.
The governed list is the exact concrete list identified by the WI-008 P2
finding rather than a package-wide expansion:

| Class | Standard-based justification |
|---|---|
| `config.JwtConfig` | Validates JWT signing-key presence, Base64 format, and minimum strength before startup. The line 588 configuration-file exception does not cover executable Java code that fails closed on an invalid signing key. |
| `security.CustomUserDetailsService` | Resolves authentication principals and rejects missing or deactivated accounts by email and user ID. |
| `security.JwtTokenProvider` | Mints, parses, validates, and classifies access/refresh JWTs, including expired-token subject recovery. |
| `security.JwtAuthenticationFilter` | Converts a validated bearer token into the authorization context and clears stale authentication for expired or invalid tokens. |
| `security.AuthRateLimitFilter` | Enforces anti-abuse controls on authentication/recovery endpoints and normalizes/fingerprints client and identifier data without retaining raw values. |
| `service.auth.AuthService` | Owns password/social authentication, refresh-token hashing/rotation/replay rejection, and logout session invalidation. |
| `service.payment.billing.BillingKeyCrypto` | Encrypts, authenticates, decrypts, fingerprints, and validates key-ring configuration for stored provider billing keys. This is direct cryptographic confidentiality/integrity code. |

Scope decision: package membership alone was not used to classify additional
classes. WI-009 closes the named WI-008 finding without turning this remediation
into a new security-taxonomy review. Exact class includes avoid silent package
growth while still failing closed for every governed class.

## Implementation

### Behavior-focused tests

| Test evidence | Covered behavior |
|---|---|
| `CustomUserDetailsServiceTest.java:29-92` | Six tests cover active, absent, and deactivated users for both username and ID lookup. |
| `JwtAuthenticationFilterTest.java:42-102` | Four tests cover non-bearer bypass, valid authentication, expired-token clearing/header behavior, and invalid-token clearing. |
| `JwtTokenProviderTest.java:92-102` | Covers valid-token use of the expiry-tolerant subject reader and the configured access-token lifetime. |
| `AuthRateLimitFilterTest.java:266-291` | Covers forgot-password, reset-password, and refresh limits plus fail-closed SHA-256 provider failure. |
| `AuthServiceTest.java:111-157,206-221` | Covers social login token hashing/profile response, digest failure without raw-token persistence, and a valid refresh token for a missing user record. |
| `BillingKeyCryptoTest.java:138-281` | Covers blank inputs, malformed/forged ciphertext, nonce validation, malformed/null/duplicate key rings, unavailable active keys, and cipher/MAC/digest provider failures without sensitive-value disclosure. |
| Existing `JwtConfigTest.java:15-64` | Retains missing, malformed, short, and valid signing-key validation coverage. |

Twenty-five behavior tests were added across the six changed test classes; the
existing four `JwtConfigTest` cases already covered its executable lines and
methods.

### Behavior-preserving test seam

`BillingKeyCrypto.java:190-200` now evaluates placeholder detection before the
blank check in its two private configuration validators. Public behavior and
messages are unchanged: null, blank, placeholder, malformed, and unavailable
configuration still fail closed. The order change makes the existing defensive
`isPlaceholder(null)` branch reachable through a public null-configuration
scenario (`BillingKeyCryptoTest.java:216-229`). No other production code changed.

### JaCoCo enforcement

`build.gradle:76-84` declares the seven exact governed classes.
`build.gradle:120-134` adds one `CLASS` rule with:

- `LINE / COVEREDRATIO / minimum = 1.00`
- `METHOD / COVEREDRATIO / minimum = 1.00`

The existing `BUNDLE` rule remains at lines 100-118 with line 0.80, method
0.80, and branch 0.70. `check` still depends on
`jacocoTestCoverageVerification` at lines 138-140. No production exclusion,
generated-test substitution, or threshold reduction was introduced.

The WI contract explicitly defines critical completion as 100% executable
line and method coverage. Branch counters are reported below and remain under
the unchanged global 70% rule; no unrequested per-class branch rule was added.

## Coverage Evidence

Source: `build/reports/jacoco/test/jacocoTestReport.xml` from the required clean
gate.

| Governed class | WI-008 line | Final line | WI-008 method | Final method | Final branch |
|---|---:|---:|---:|---:|---:|
| `CustomUserDetailsService` | 0/10 | **10/10 (100%)** | 0/4 | **4/4 (100%)** | 4/4 (100%) |
| `JwtTokenProvider` | 34/36 | **36/36 (100%)** | 8/9 | **9/9 (100%)** | n/a |
| `JwtAuthenticationFilter` | 21/22 | **22/22 (100%)** | 2/2 | **2/2 (100%)** | 10/10 (100%) |
| `AuthRateLimitFilter` | 90/95 | **95/95 (100%)** | 17/17 | **17/17 (100%)** | 36/40 (90%) |
| `AuthService` | 35/44 | **44/44 (100%)** | 4/6 | **6/6 (100%)** | 10/10 (100%) |
| `BillingKeyCrypto` | 80/97 | **97/97 (100%)** | 17/17 | **17/17 (100%)** | 45/50 (90%) |
| `JwtConfig` | 10/10 | **10/10 (100%)** | 2/2 | **2/2 (100%)** | 5/6 (83.33%) |

Global counters:

| Counter | Covered | Total | Coverage | Gate |
|---|---:|---:|---:|---:|
| Instructions | 38,778 | 45,263 | 85.67% | informational |
| Lines | 8,528 | 9,948 | **85.73%** | 80% PASS |
| Methods | 1,511 | 1,822 | **82.93%** | 80% PASS |
| Branches | 2,830 | 3,948 | **71.68%** | 70% PASS |

## Execution Evidence

Required command:

```powershell
.\gradlew.bat clean build jacocoTestReport jacocoTestCoverageVerification --console=plain
```

Result: **PASS**, exit code 0, wall time 155.6 seconds. Gradle reported 11/11
tasks executed. Test-suite header aggregation reports 158 suites, 1,206 tests,
0 failures, 0 errors, and 9 skipped tests.

The skipped set is unchanged in nature: seven MySQL concurrency tests, one
MySQL schema-validation test, and one Windows symbolic-link test. The requested
command used ephemeral test databases only; no external MySQL test was forced
and no external DB was mutated.

Focused commands also passed after remediation:

```powershell
.\gradlew.bat test --tests "com.atstudio.atstudio.security.CustomUserDetailsServiceTest" `
  --tests "com.atstudio.atstudio.security.JwtAuthenticationFilterTest" `
  --tests "com.atstudio.atstudio.security.JwtTokenProviderTest" `
  --tests "com.atstudio.atstudio.security.AuthRateLimitFilterTest" `
  --tests "com.atstudio.atstudio.service.auth.AuthServiceTest" `
  --tests "com.atstudio.atstudio.service.payment.billing.BillingKeyCryptoTest" `
  --tests "com.atstudio.atstudio.config.JwtConfigTest" jacocoTestReport --console=plain
```

## Time and Blockers

Measured Gradle execution time was 273.9 seconds total:

| Activity | Time | Outcome |
|---|---:|---|
| Initial test compilation | 8.8 s | Found AssertJ single-argument API mismatch; fixed without behavior change. |
| First focused test run | 23.5 s | Found two test-fixture issues: a null mock header and over-broad JDK static mocking. |
| First passing focused test/report | 53.8 s | Confirmed the behavior tests and exposed the single unreachable null-defense line. |
| Post-seam focused test/report | 32.2 s | Confirmed `BillingKeyCrypto` at 97/97 lines and 17/17 methods. |
| Required full clean gate | 155.6 s | PASS; produced final test and coverage evidence. |

Most measured time was spent in the mandatory full gate and iterative coverage
report generation. Additional unmeasured time was spent reading the handoff and
standards, mapping the WI-008 class list, reviewing missed source lines, and
writing this report.

There is **no remaining WI-009 backend blocker**. Resolved intermediate
blockers were test-only. Some Gradle test-result files contain system output
that PowerShell's XML parser rejects; final totals were therefore aggregated
from the Gradle-generated `<testsuite>` header attributes. This evidence
extraction issue did not affect Gradle/JUnit execution or JaCoCo verification.

## Finding Closure and Rollback

| Finding | Status | Closure evidence |
|---|---|---|
| WI-008 P2 critical security-path coverage | **FIXED** | Seven exact classes are 100% line/method; `CLASS` verification enforces 1.00/1.00; global 80/80/70 remains; 1,206-test clean gate passes. |

Rollback is limited to removing the new test classes, reverting the focused
test additions, removing the `criticalSecurityClasses`/`CLASS` rule from
`build.gradle`, and restoring the two private validation-expression orders in
`BillingKeyCrypto`. No DB, Git, frontend, policy, or secret rollback applies.

## Final Status

**PASS**
