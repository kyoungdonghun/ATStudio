# Evidence Pack: WI-20260713-ATS-004

## Summary

- Replaced SMTP payload logging with random delivery correlation metadata and added captured-log coverage for verification success and password-reset failure.

## Scope / DoD Check

- [x] Generated one random `deliveryId` per delivery attempt.
- [x] Preserved mail construction, send invocation, and absorbed-failure behavior.
- [x] Logged only `deliveryId` and outcome on success.
- [x] Logged only `deliveryId`, outcome, and exception class name on failure.
- [x] Proved recipient, subject, body values, URL, token, raw provider message, and stack trace are absent from captured logs.
- [x] Confirmed password-login-policy regression tests pass.
- [x] Avoided live SMTP calls and API, token, template, and SMTP configuration changes.

## Reference Documents

| Tier | Document | Reason |
|---|---|---|
| 0 | `docs/standards/core-principles.md` | Constitution and secret-handling baseline |
| 0 | `docs/standards/development-standards.md` | Java service, test, and traceability standards |
| 0 | `docs/standards/documentation-standards.md` | Documentation format baseline |
| 0 | `docs/standards/glossary.md` | Canonical terminology baseline |
| 1 | `docs/policies/security-policy.md` | Secrets/PII logging minimization |
| 1 | `docs/policies/quality-gates.md` | Regression and evidence requirements |
| 2 | `docs/design/p0-release-blocker-remediation-design.md` | Canonical secret-free mail logging contract |
| Context | `deliverables/user/REQ-20260713-ATS-001.md` | Approved scope and success criteria |
| Context | `deliverables/agent/WI-20260713-ATS-002-evidence-pack.md` | Approved implementation contract evidence |
| Handoff | `deliverables/agent/WI-20260713-ATS-004-handoff.md` | Ownership, DoD, output, and rollback contract |

Injection source: WI-004 handoff `INPUT POINTERS`; assignee `se`; task type `security/implementation`.

## Evidence Pointers

- `src/main/java/com/atstudio/atstudio/service/EmailService.java:163-180`
  - Creates one UUID before the send attempt and emits secret-free success/failure metadata without passing the throwable to SLF4J.
- `src/test/java/com/atstudio/atstudio/service/EmailServiceTest.java:39-46`
  - Enables Spring Boot output capture and defines the UUID correlation pattern.
- `src/test/java/com/atstudio/atstudio/service/EmailServiceTest.java:81-107`
  - Captures verification-success logs and rejects recipient, subject, nickname, generated token, and verification URL.
- `src/test/java/com/atstudio/atstudio/service/EmailServiceTest.java:109-146`
  - Captures password-reset SMTP failure logs and rejects recipient, subject, nickname, generated token, reset URL, provider message, and stack trace.
- `build/test-results/test/TEST-com.atstudio.atstudio.service.EmailServiceTest.xml`
  - Focused JUnit XML: 4 tests, 0 failures, 0 errors, 0 skipped.

## Commands & Outputs

- `gradlew.bat test --tests "com.atstudio.atstudio.service.EmailServiceTest"`
  - Initial attempt: failed during `compileTestJava` on 8 unresolved `TrackService.StreamResource` references in concurrent WI-003 test edits.
  - Final attempt after the concurrent WI-003 production edit landed: `BUILD SUCCESSFUL in 14s`; 4 passed, 0 failed, 0 errors, 0 skipped.
- `gradlew.bat test --tests "com.atstudio.atstudio.service.EmailServiceTest" --tests "com.atstudio.atstudio.service.auth.AuthServiceTest" --tests "com.atstudio.atstudio.service.UserServiceTest" --tests "com.atstudio.atstudio.service.UtilServiceTest"`
  - Result: `BUILD SUCCESSFUL in 7s`; 55 passed, 0 failed, 0 errors, 0 skipped.
  - Suite counts: EmailService 4, AuthService 8, UserService 26, UtilService 17.
- `rg -n "log\.(info|warn|error|debug|trace)" src/main/java/com/atstudio/atstudio/service/EmailService.java`
  - Result: only the success log at line 174 and failure log at line 176.
- `rg -n -i "log\.(trace|debug|info|warn|error).*token" src/main/java/com/atstudio/atstudio`
  - Result: no matches.
- `rg -n "JavaMailSender|mailSender\.send" src/main/java/com/atstudio/atstudio`
  - Result: only `EmailService` owns and invokes `JavaMailSender`.

No test invoked an SMTP endpoint. `JavaMailSender.send` was mocked for both the successful and throwing paths.

## Risks / Rollback

- Risk: Operators must correlate delivery outcomes by `deliveryId`; payload reconstruction from application logs is intentionally unavailable.
- Residual scope: An unrelated renewal-service catch block logs its own exception with an order ID, but the centralized `EmailService` absorbs SMTP exceptions, and the source scan found no token logging outside this service.
- Rollback: Revert only `EmailService.java`, `EmailServiceTest.java`, and the two WI-004 output files.

## Follow-ups

- WI-20260713-ATS-007 can independently verify the captured-log contract and cross-flow regressions.
