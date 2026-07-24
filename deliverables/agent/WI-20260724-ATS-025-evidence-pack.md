---
version: 1.0
last_updated: 2026-07-24
project: ATS
owner: se
category: evidence-pack
status: confirmed
dependencies:
  - path: WI-20260724-ATS-025-handoff.md
    reason: Approved Work Item scope and output contract
  - path: WI-20260724-ATS-016-evidence-pack.md
    reason: Failed baseline and exact 17-check transport contract
---

# Evidence Pack: WI-20260724-ATS-025

## Summary

- **Verdict: PASS**
- Corrected `WI016-DEFECT-01` and `WI016-DEFECT-02` within the approved two-file
  product/test boundary.
- Account-mail nicknames and all subscription-payment-failure dynamic text are
  HTML-escaped exactly once at their template output boundaries.
- The four-message `JavaMailSenderImpl` loopback transport contract now passes
  17/17 checks.

## Scope / DoD Check

- [x] Verification and password-reset nicknames are HTML-escaped.
- [x] Payment-failure nickname, failure summary, and retry guidance are
  HTML-escaped.
- [x] Verification and reset HTTPS callback links retain their token query
  parameters.
- [x] Existing reconciliation escaping remains unchanged and passing.
- [x] Four representative messages reached a loopback SMTP sink.
- [x] Adversarial unit regressions pass.
- [x] Related backend tests and Java compilation pass.
- [x] Success and failure logs remain secret-safe.
- [x] Raw MIME was never persisted.
- [x] The temporary transport harness and its compiled/result artifacts were
  removed.
- [x] Documentation, diff, and secret-scan gates pass.

## Reference Documents

| Tier | Document | Reason |
|---|---|---|
| 0 | `docs/standards/core-principles.md` | Approval, isolation, and traceability |
| 0 | `docs/standards/development-standards.md` | Java and verification standards |
| 0 | `docs/standards/documentation-standards.md` | Deliverable structure |
| 0 | `docs/standards/glossary.md` | AT.M display-brand and Work Item terminology |
| 1 | `docs/policies/security-policy.md` | Mail logging and secret-handling contract |
| 1 | `docs/policies/quality-gates.md` | Test and evidence gates |
| 2 | `docs/standards/evidence-pack-standard.md` | Evidence and rollback format |
| Context | `deliverables/user/REQ-20260724-ATS-002.md` | Approved release rehearsal |
| Context | `deliverables/agent/WI-20260724-ATS-016-handoff.md` | Original mail transport scope |
| Context | `deliverables/agent/WI-20260724-ATS-016-evidence-pack.md` | Failed 14/17 baseline |

## Evidence Pointers

### Product and Regression Files

- `src/main/java/com/atstudio/atstudio/service/EmailService.java`
  - Verification and reset nicknames pass through the existing `escapeHtml`
    helper at the template boundary.
  - Payment-failure fallback values pass through the same helper once.
  - Callback URL insertion and reconciliation escaping were not changed.
- `src/test/java/com/atstudio/atstudio/service/EmailServiceTest.java`
  - Adds adversarial verification, reset, and payment-failure regressions.
  - Extends reconciliation coverage to assert the established escaping.

### Sanitized Repo-External Evidence

```text
C:\Users\jm991\AppData\Local\ATStudio\
wi025-mail-evidence-20260724T234433\sanitized
```

| File | Purpose |
|---|---|
| `mail-assertions.json` | Boolean-only 17-check result matrix |
| `cleanup.json` | Harness, raw-MIME, compiled-artifact, and sink cleanup state |

The evidence directory contains no recipient, body, token, secret, or raw MIME.
Its inherited ACL was disabled and access was granted only to the current
Windows account.

## Implementation Details

| Template | Dynamic values | Encoding |
|---|---|---|
| Verification | nickname | `escapeHtml` once before text-node insertion |
| Password reset | nickname | `escapeHtml` once before text-node insertion |
| Payment failure | nickname, failure summary, retry guidance | `defaultText`, then `escapeHtml` once |
| Reconciliation | summary, details | Existing `escapeHtml(defaultText(...))` preserved |

The existing helper encodes `&`, `<`, `>`, `"`, and `'`. No dependency,
template engine, or alternate escaping path was added.

## Adversarial Cases

- Element injection: `img`, `svg`, `script`, `a`, and `strong` tags.
- Attribute injection: `onerror`, `onload`, `onclick`, and `href`.
- Delimiter coverage: ampersand, double quote, and single quote.
- Context break attempts: leading quote/angle bracket and closing paragraph.
- Callback preservation: exact HTTPS verification/reset `href` values and
  token query parameters.
- Negative assertions: the original adversarial values do not occur in the
  generated HTML.

## Commands and Results

### Focused Test

```powershell
.\gradlew.bat test `
  --tests "com.atstudio.atstudio.service.EmailServiceTest" `
  --no-daemon --console=plain
```

- Result: PASS.
- Tests: 10, failures: 0, errors: 0, skipped: 0.

### Related Backend Slice and Compilation

```powershell
.\gradlew.bat compileJava compileTestJava test `
  --tests "com.atstudio.atstudio.service.EmailServiceTest" `
  --tests "com.atstudio.atstudio.service.PaymentReconciliationIncidentServiceTest" `
  --tests "com.atstudio.atstudio.service.RecurringRenewalServiceTest" `
  --tests "com.atstudio.atstudio.service.UserServiceTest" `
  --tests "com.atstudio.atstudio.controller.AuthControllerTest" `
  --no-daemon --console=plain
```

- Result: PASS.
- Tests: 61, failures: 0, errors: 0, skipped: 0.
- `compileJava` and `compileTestJava`: PASS.

### Actual Loopback SMTP Transport

A temporary JUnit harness:

1. Bound an SMTP subset sink only to `127.0.0.1` on an ephemeral port.
2. Instantiated the product `EmailService` with a real
   `JavaMailSenderImpl`.
3. Sent verification, password-reset, payment-failure, and reconciliation
   messages only to reserved `.invalid` recipients.
4. Parsed each received MIME message in process.
5. Exercised a closed loopback port for the failure-log contract.
6. Wrote only boolean aggregate results.
7. Removed the harness and left no raw MIME, compiled class, or harness test
   report.

- JUnit transport test: PASS.
- SMTP messages: 4/4.
- Contract checks: 17/17.
- External SMTP connections: 0.

Exact checks:

1. Four messages received.
2. Every recipient uses the `.invalid` TLD.
3. Every subject uses AT.M branding.
4. Every message contains HTML.
5. Verification uses the HTTPS base URL.
6. Password reset uses the HTTPS base URL.
7. Verification token occurs only inside its callback URL.
8. Reset token occurs only inside its callback URL.
9. Verification nickname is escaped.
10. Reset nickname is escaped.
11. All payment-failure dynamic fields are escaped.
12. Reconciliation dynamic fields remain escaped.
13. Four success delivery IDs are logged.
14. Success logs contain only safe metadata.
15. Failure outcome and delivery ID are logged.
16. Failure exception class is logged.
17. Failure logs contain only safe metadata.

### Harness Diagnostics

- The first transport command did not enter tests because PowerShell/Gradle
  parsed a `-D` path as a task. Evidence-path transfer was changed only in the
  temporary harness to an environment variable.
- The next temporary compile exposed Java 17 incompatibility with
  `Thread.ofPlatform`; the harness used a standard Java 17 `Thread` instead.
- Neither diagnostic affected product code or the final verification result.

### Final Quality Gates

```powershell
python .agents/skills/validate-docs/scripts/validate_docs.py
git diff --check
```

- Documentation validation: PASS.
- Diff check: PASS.
- High-confidence added-line and deliverable secret scan: 0 unresolved events.
- Temporary WI-025 source/class/result residual search: 0.

## Secret and Privacy Assessment

- Real recipients: 0.
- External SMTP connections: 0.
- Raw MIME persisted: no.
- Callback tokens in retained evidence: 0.
- Recipient addresses in retained evidence: 0.
- Message bodies in retained evidence: 0.
- Application or database runtime changes: 0.
- Commit or push: 0.

## External Delivery Condition

External SMTP authentication, Internet delivery, SPF/DKIM/DMARC alignment,
spam placement, link rewriting, and human inbox rendering remain a separate
human/operations gate. WI-025 proves local generation, transport, encoding, and
log safety only.

## Risks / Rollback

### Residual Risks

- External provider deliverability is not proven without an approved
  non-production SMTP account and designated test inbox.
- The callback base URL remains environment configuration and must be checked
  again in the deployed environment.

### Rollback

- Revert the `escapeHtml` calls added to the three affected template paths.
- Revert the four adversarial regression changes in `EmailServiceTest`.
- Remove the two WI-025 deliverables.
- No database, runtime, provider, or external-mail rollback is required.

## Follow-ups

- WI-025 unblocks the mail side of `WI-20260724-ATS-017`.
- The external inbox gate remains an operations prerequisite before production
  release.

## Related Documents

- [WI-025 Handoff](WI-20260724-ATS-025-handoff.md)
- [WI-016 Evidence Pack](WI-20260724-ATS-016-evidence-pack.md)
- [WI-025 User Summary](../user/WI-20260724-ATS-025-summary.md)
