# WI-20260713-ATS-004 Secret-Free SMTP Logging Summary

## Outcome

- Every mail delivery attempt now receives one random `deliveryId`.
- Success logs contain only the delivery ID and `SUCCESS` outcome.
- Failure logs contain only the delivery ID, `FAILURE` outcome, and exception class name.
- Recipient addresses, subjects, bodies, nicknames, verification/reset URLs and tokens, provider messages, and stack traces are no longer logged.
- Mail construction, SMTP invocation through `JavaMailSender`, token generation and lifetime, templates, and exception absorption behavior remain unchanged.

## Verification

- `gradlew.bat test --tests "com.atstudio.atstudio.service.EmailServiceTest"`
  - Result: `BUILD SUCCESSFUL`; 4 tests passed, 0 failed, 0 skipped.
- `gradlew.bat test --tests "com.atstudio.atstudio.service.EmailServiceTest" --tests "com.atstudio.atstudio.service.auth.AuthServiceTest" --tests "com.atstudio.atstudio.service.UserServiceTest" --tests "com.atstudio.atstudio.service.UtilServiceTest"`
  - Result: `BUILD SUCCESSFUL`; 55 tests passed, 0 failed, 0 skipped.
- Tests use a mocked `JavaMailSender`; no SMTP endpoint was called.
- A source scan confirmed that `EmailService` is the only `JavaMailSender` owner and that no Java log statement contains `token`.

## Changed Paths

- `src/main/java/com/atstudio/atstudio/service/EmailService.java`
- `src/test/java/com/atstudio/atstudio/service/EmailServiceTest.java`
- `deliverables/user/WI-20260713-ATS-004-summary.md`
- `deliverables/agent/WI-20260713-ATS-004-evidence-pack.md`

## Rollback

- Revert only the two EmailService files and these two WI-004 outputs.
