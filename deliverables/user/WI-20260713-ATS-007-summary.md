# WI-20260713-ATS-007 Mail Logging Reliability Summary

## Outcome

- Independently verified the WI-004 secret-free mail logging change.
- Success and failure output contains only a unique `deliveryId`, the delivery outcome, and the exception class name on failure.
- Recipient, subject, nickname, verification/reset token and URL, provider message, fallback payload labels, and stack-trace frames were absent from captured output.
- No mail implementation or test defect was found, so WI-007 made no corrective code or test edit.
- SMTP failure absorption and the generic password-reset behavior remain unchanged.

## Verification

- `gradlew.bat test --tests "com.atstudio.atstudio.service.EmailServiceTest"`
  - Result: 4 tests passed, 0 failed, 0 errors, 0 skipped.
- Captured-output inspection of the JUnit XML:
  - Result: 2 mail log lines, 2 unique delivery IDs, and 0 forbidden-value matches.
- Mail/auth regression selection:
  - Result: 57 tests passed across `EmailServiceTest`, `AuthServiceTest`, `UserServiceTest`, and `UtilServiceTest`.
- Source scans found no console fallback payload logging and no token-bearing log statement.
- `JavaMailSender` was mocked; no SMTP endpoint or network service was called.

## Changed Paths

- `deliverables/user/WI-20260713-ATS-007-summary.md`
- `deliverables/agent/WI-20260713-ATS-007-evidence-pack.md`

Media, billing, security, and unrelated documentation changes in the shared worktree were preserved.

## Rollback

- Remove only the two WI-007 output files. No production or test code rollback is required.
