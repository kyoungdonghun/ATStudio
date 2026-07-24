---
version: 1.0
last_updated: 2026-07-24
project: ATS
owner: qa-integ
category: evidence-pack
status: confirmed
dependencies:
  - path: WI-20260724-ATS-016-handoff.md
    reason: Approved Work Item scope and output contract
  - path: WI-20260724-ATS-024-evidence-pack.md
    reason: Runtime authorization correction prerequisite
---

# Evidence Pack: WI-20260724-ATS-016

## Summary

- **Verdict: FAIL**
- Actual `EmailService` and `JavaMailSenderImpl` transport delivered all four
  representative messages to an isolated loopback SMTP sink.
- Transport, branding, MIME, HTTPS callback, token placement, and log secrecy
  passed.
- Account-mail nickname escaping and payment-failure dynamic-field escaping
  failed. Product code was not changed.
- External SMTP delivery remains a human/operations gate because no approved
  SMTP account and designated test inbox were provided.

## Scope / DoD Check

- [x] Additional-install-free SMTP sink bound only to `127.0.0.1`.
- [x] Signup verification message reached the sink.
- [x] Password-reset message reached the sink.
- [x] Subscription payment-failure message reached the sink.
- [x] Payment reconciliation alert reached the sink.
- [x] Subjects, HTML MIME, AT.M branding, HTTPS base URL, and token placement
  were checked.
- [x] Success and failure logging were checked for secret-safe metadata.
- [x] Raw MIME and temporary test assets were removed.
- [x] Sanitized repo-external assertion evidence was retained.
- [ ] All untrusted dynamic HTML fields are escaped.
- [ ] External provider delivery and designated-inbox receipt are proven.

## Reference Documents

| Tier | Document | Reason |
|---|---|---|
| 0 | `docs/standards/core-principles.md` | Approval, isolation, and traceability |
| 0 | `docs/standards/development-standards.md` | Java and verification standards |
| 0 | `docs/standards/documentation-standards.md` | Evidence structure |
| 0 | `docs/standards/glossary.md` | AT.M display-brand contract |
| 1 | `docs/policies/security-policy.md` | Mail logging and secret-handling contract |
| 1 | `docs/policies/quality-gates.md` | Quality evidence requirements |
| Context | `deliverables/user/REQ-20260724-ATS-002.md` | Approved release rehearsal |
| Context | `deliverables/agent/WI-20260724-ATS-014-evidence-pack.md` | Shared runtime ownership |
| Context | `deliverables/agent/WI-20260724-ATS-024-evidence-pack.md` | Corrected runtime prerequisite |

## Evidence Pointers

### Product Code Reviewed

- `src/main/java/com/atstudio/atstudio/service/EmailService.java:47`
  - Signup verification trigger.
- `src/main/java/com/atstudio/atstudio/service/EmailService.java:89`
  - Password-reset trigger.
- `src/main/java/com/atstudio/atstudio/service/EmailService.java:112`
  - Subscription payment-failure trigger.
- `src/main/java/com/atstudio/atstudio/service/EmailService.java:128`
  - Reconciliation alert trigger.
- `src/main/java/com/atstudio/atstudio/service/EmailService.java:165`
  - Actual JavaMail send and bounded log metadata.
- `src/main/java/com/atstudio/atstudio/service/EmailService.java:201`
  - Unescaped verification nickname insertion.
- `src/main/java/com/atstudio/atstudio/service/EmailService.java:218`
  - Unescaped reset nickname insertion.
- `src/main/java/com/atstudio/atstudio/service/EmailService.java:235`
  - Unescaped payment-failure dynamic fields.
- `src/main/java/com/atstudio/atstudio/service/EmailService.java:253`
  - Reconciliation fields use `escapeHtml`.

### Existing Tests Reviewed

- `src/test/java/com/atstudio/atstudio/service/EmailServiceTest.java`
  - Seven existing tests pass.
  - Existing coverage checks branding and log secrecy but does not assert
    escaping for account-mail nicknames or payment-failure dynamic fields.

### Sanitized Repo-External Evidence

Root:

```text
C:\Users\jm991\AppData\Local\ATStudio\
wi016-mail-evidence-20260724T231700\sanitized
```

| File | Purpose |
|---|---|
| `sink-ready.json` | Loopback bind address, ephemeral port, and readiness |
| `sink-process.json` | Owned temporary sink process metadata |
| `sink-summary.json` | Four connections and four received messages |
| `mail-assertions.json` | Sanitized 17-check result matrix |
| `raw-transient-manifest.json` | Message byte counts and SHA-256 only |
| `sink-stdout.log` | Empty bounded sink output |
| `sink-stderr.log` | Empty bounded sink error output |
| `cleanup.json` | Raw MIME, sink script, process, and harness cleanup state |

No retained evidence contains recipient addresses, message bodies, callback
tokens, or raw MIME.

## Test Design

The temporary JUnit integration harness:

1. Instantiated the product `EmailService`.
2. Supplied a real `JavaMailSenderImpl` configured for an ephemeral loopback
   sink port, no authentication, no STARTTLS, and bounded timeouts.
3. Mocked only repositories, password encoding, and the password-login policy
   so message generation and SMTP transport remained real.
4. Sent all four messages to `.invalid` recipients.
5. Parsed the received MIME with Jakarta Mail.
6. Captured generated verification/reset tokens only in test-process memory.
7. Asserted subject, HTML, callback URL, token placement, escaping, and log
   secrecy.
8. Exercised a closed loopback port to verify bounded failure logging.
9. Wrote only boolean and aggregate assertion results.
10. Deleted the harness and raw MIME after inspection.

## Result Matrix

| Check group | Result |
|---|---|
| SMTP messages received | 4/4 PASS |
| `.invalid` recipient boundary | PASS |
| `[AT.M]` branded subjects | 4/4 PASS |
| HTML MIME part | 4/4 PASS |
| HTTPS verification/reset base URL | 2/2 PASS |
| Token appears only inside callback URL | 2/2 PASS |
| Verification nickname escaping | FAIL |
| Reset nickname escaping | FAIL |
| Payment-failure dynamic-field escaping | FAIL |
| Reconciliation dynamic-field escaping | PASS |
| Success log secrecy | PASS |
| Failure log secrecy | PASS |

Aggregate sanitized harness result: **14/17 checks passed**.

## Commands and Outcomes

### Shared Runtime Preflight

```powershell
Get-NetTCPConnection -State Listen |
  Where-Object { $_.LocalPort -in 8080,15173 }
```

- Both shared listeners were present before and after WI-016.
- WI-016 issued no process stop/start command for either listener and made no
  database call.
- During the independent WI-016 run, the observed `8080` listener process
  identity changed from an earlier runtime PID to a new descendant. WI-016 did
  not issue that restart; the concurrent shared-runtime owner must retain
  ownership of that lifecycle.

### Temporary Transport Harness

```powershell
.\gradlew.bat test `
  --tests "com.atstudio.atstudio.service.Wi016EmailTransportIntegrationTest" `
  --no-daemon --console=plain
```

- Result: 1 test, 1 failure.
- Failure reason: three escaping assertions were false.
- Transport completed before the contract assertion.
- Sanitized result: `mail-assertions.json`.

### Existing Product Test

```powershell
.\gradlew.bat test `
  --tests "com.atstudio.atstudio.service.EmailServiceTest" `
  --no-daemon --console=plain
```

- Result: PASS.
- Existing suite: 7 tests, 0 failures.

## Defects

### WI016-DEFECT-01

**Account mail inserts unescaped nickname HTML.**

- Verification:
  `EmailService.java:201`
- Password reset:
  `EmailService.java:218`
- Risk: a stored nickname containing HTML can alter the generated email body.
- Required follow-up: HTML-escape nickname at template boundaries and add
  verification/reset regression assertions.

### WI016-DEFECT-02

**Subscription payment-failure mail inserts unescaped dynamic fields.**

- Location:
  `EmailService.java:235`
- Affected values: nickname, failure summary, and retry guidance.
- Existing contrast:
  reconciliation summary/detail already pass through `escapeHtml` at
  `EmailService.java:253-254`.
- Required follow-up: apply the same output-encoding rule and add adversarial
  regression assertions.

## Secret and Privacy Assessment

- Sink bind address: loopback only.
- Recipients: reserved `.invalid` addresses only.
- External SMTP connections: 0.
- Raw MIME: deleted.
- Tokens in retained evidence: 0.
- Recipient addresses in retained evidence: 0.
- Email bodies in retained evidence: 0.
- Success log fields: delivery ID and outcome only.
- Failure log fields: delivery ID, outcome, and exception class only.
- Evidence directory ACL: inherited access disabled; current account has full
  control.

## External Delivery Condition

**Conditionally blocked: human/operations gate.**

No approved external SMTP credentials or designated test inbox were available.
Therefore WI-016 does not claim:

- SMTP provider authentication,
- Internet delivery,
- SPF/DKIM/DMARC alignment,
- spam-folder placement,
- external link rewriting behavior,
- or human inbox rendering.

These checks require an operator-provided non-production SMTP configuration and
explicit test inbox.

## Risks / Rollback

### Risks

- The two HTML escaping defects remain in product code by instruction.
- Local-sink PASS does not prove external provider deliverability.
- The shared acceptance runtime is concurrently owned by another WI; its
  lifecycle must remain under that owner's cleanup contract.

### Rollback and Cleanup

- No product code or persistent database state changed.
- The temporary harness was removed from `src/test`.
- The temporary sink script and process were removed.
- Raw MIME was deleted after its size and SHA-256-only manifest was written.
- Removing the sanitized repo-external evidence directory is sufficient to
  discard the remaining WI-016 runtime artifact.

## Follow-ups

1. Approve a corrective WI for `WI016-DEFECT-01` and
   `WI016-DEFECT-02`.
2. Rerun this four-message transport contract after correction.
3. Complete the external SMTP and designated-inbox human/operations gate before
   production release.

## Related Documents

- [WI-016 Handoff](WI-20260724-ATS-016-handoff.md)
- [WI-024 Evidence Pack](WI-20260724-ATS-024-evidence-pack.md)
- [WI-016 User Summary](../user/WI-20260724-ATS-016-summary.md)
