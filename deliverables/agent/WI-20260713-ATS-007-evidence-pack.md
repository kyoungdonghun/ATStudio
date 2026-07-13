# Evidence Pack: WI-20260713-ATS-007

## Summary

- Independently verified WI-004 captured mail output and source logging paths; no mail or test correction was required.

## Scope / DoD Check

- [x] Reviewed the WI-004 implementation and captured-output tests independently.
- [x] Confirmed success output retains a unique `deliveryId` and `SUCCESS` outcome.
- [x] Confirmed failure output retains a unique `deliveryId`, `FAILURE` outcome, and exception class name.
- [x] Confirmed recipient, subject, body sentinels, nickname, URL, token, provider message, and stack-trace frames are absent.
- [x] Confirmed no fallback payload logger remains in the verification/reset mail path.
- [x] Confirmed SMTP failure remains absorbed and generic external behavior is unchanged.
- [x] Used mocked `JavaMailSender` only; no SMTP call occurred.
- [x] Preserved all media, billing, security, and unrelated documentation edits.

## Reference Documents

| Tier | Document | Reason |
|---|---|---|
| 0 | `docs/standards/core-principles.md` | Constitution and independent-verification baseline |
| 0 | `docs/standards/development-standards.md` | RE testing and evidence requirements |
| 0 | `docs/standards/documentation-standards.md` | Documentation structure and traceability baseline |
| 0 | `docs/standards/glossary.md` | Canonical WI and RE terminology |
| 1 | `docs/policies/security-policy.md` | Secrets and PII logging minimization |
| 1 | `docs/policies/quality-gates.md` | Regression and Evidence Pack requirements |
| 2 | `docs/design/p0-release-blocker-remediation-design.md` | Secret-free mail logging and acceptance-test contract |
| Context | `deliverables/agent/WI-20260713-ATS-004-evidence-pack.md` | Implementation claims and prior evidence under review |
| Handoff | `deliverables/agent/WI-20260713-ATS-007-handoff.md` | Scope, DoD, constraints, and output contract |

Injection source: WI-007 handoff `INPUT POINTERS` plus repository-required Tier 0 documents; assignee `re`; task type `testing/review`.

## Independent Findings

- No corrective defect found in the WI-004 implementation or tests.
- `EmailService.sendEmail` generates the delivery ID before the attempt and never passes the throwable to SLF4J.
- The success test captures the generated verification token and rejects recipient, subject, nickname, token, and URL. The nickname and URL are body sentinels, so full-body fallback logging would fail the test.
- The failure test captures the generated reset token and rejects recipient, subject, nickname, token, URL, provider message, and stack-trace frame markers.
- The broad mail-related log scan also found QA bootstrap email-address logs and a billing renewal exception log. Neither is in the verification/reset delivery path; both were left untouched under WI-007 scope and shared-worktree constraints.

## Evidence Pointers

- `src/main/java/com/atstudio/atstudio/service/EmailService.java:163-180`
  - Mail construction and send remain unchanged; logging is limited to delivery correlation metadata.
- `src/test/java/com/atstudio/atstudio/service/EmailServiceTest.java:81-107`
  - Verification success captured-output assertions and dynamic secret capture.
- `src/test/java/com/atstudio/atstudio/service/EmailServiceTest.java:109-146`
  - Password-reset failure captured-output assertions, provider-message rejection, and stack-trace rejection.
- `build/test-results/test/TEST-com.atstudio.atstudio.service.EmailServiceTest.xml`
  - Final focused run: 4 tests, 0 failures, 0 errors, 0 skipped.
  - Captured success delivery ID: `6d9ccd6f-2304-4c49-95db-638c7da503c9`.
  - Captured failure delivery ID: `96342336-d983-4bdb-9d80-74bdf27aa9fd`.

## Commands & Outputs

- `gradlew.bat test --tests "com.atstudio.atstudio.service.EmailServiceTest"`
  - Baseline result: `BUILD SUCCESSFUL`; 4 passed, 0 failed, 0 errors, 0 skipped.
  - Final focused result after concurrent Gradle activity: exit code 0; XML confirms 4 passed, 0 failed, 0 errors, 0 skipped.
- `gradlew.bat test --tests "com.atstudio.atstudio.service.EmailServiceTest" --tests "com.atstudio.atstudio.service.auth.AuthServiceTest" --tests "com.atstudio.atstudio.service.UserServiceTest" --tests "com.atstudio.atstudio.service.UtilServiceTest"`
  - Result: `BUILD SUCCESSFUL`; 57 passed, 0 failed, 0 errors, 0 skipped.
  - Suite counts: EmailService 4, AuthService 8, UserService 28, UtilService 17.
- JUnit XML captured-output assertion script
  - Result: `CAPTURE CHECK PASS`; 2 lines, 2 unique delivery IDs, 0 forbidden matches.
  - Required metadata observed: `outcome=SUCCESS`, `outcome=FAILURE`, and `exceptionClass=org.springframework.mail.MailSendException`.
- `rg -n "JavaMailSender|mailSender\.(createMimeMessage|send)|sendVerificationEmail|sendPasswordResetEmail" src/main/java/com/atstudio/atstudio`
  - Result: `EmailService` is the only `JavaMailSender` owner and send site; callers are `AuthController` and `UserService`.
- `rg -n -i "EMAIL FALLBACK|Falling back to console|(^|\W)(To|Subject|Body):|htmlBody|getMessage\(\)|printStackTrace" ...`
  - Result: only the `htmlBody` parameter and `helper.setText` assignment; no fallback or payload log site.
- `rg -n -i "log\.(trace|debug|info|warn|error).*token|token.*log\.(trace|debug|info|warn|error)" src/main/java/com/atstudio/atstudio`
  - Result: no matches.
- `rg -n "log\.(trace|debug|info|warn|error)|e\.getMessage\(|printStackTrace" src/main/java/com/atstudio/atstudio/service/EmailService.java`
  - Result: only the success and failure metadata log statements; no raw message or stack-trace rendering call.

## Execution Notes

- The first standalone XML check used an incorrect newline split and failed with one parsed line; the corrected check passed.
- A corrected retry briefly found the XML replaced by a concurrent media test run. A `--rerun-tasks` attempt then timed out while Gradle work overlapped; its two WI-007 wrapper processes were stopped, and no daemon or unrelated process was terminated.
- The normal focused rerun completed successfully and supplied the final captured-output evidence above.

## Risks / Rollback

- Risk: Delivery troubleshooting intentionally cannot reconstruct recipient or payload data from application logs; operators must correlate by `deliveryId`.
- Residual scope: This WI does not claim that every application log is PII-free; it verifies the verification/reset mail path defined by the handoff.
- Rollback: Remove only `deliverables/user/WI-20260713-ATS-007-summary.md` and `deliverables/agent/WI-20260713-ATS-007-evidence-pack.md`. No source or test rollback is needed.

## Follow-ups

- WI-20260713-ATS-009 and WI-20260713-ATS-013 are unblocked by this verification result.
