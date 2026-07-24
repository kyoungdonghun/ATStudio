---
version: 1.0
last_updated: 2026-07-24
project: ATS
owner: qa-integ
category: work-summary
status: confirmed
dependencies:
  - path: ../agent/WI-20260724-ATS-016-handoff.md
    reason: Approved Work Item scope
  - path: ../agent/WI-20260724-ATS-024-evidence-pack.md
    reason: Corrected runtime prerequisite
---

# WI-20260724-ATS-016 Mail Delivery Verification Summary

> Purpose: Verify representative AT.M mail generation and transport without
> contacting real members or changing the shared acceptance runtime.

## Verdict

**FAIL due to two product defects.**

The loopback SMTP transport and secret-safe logging contract passed. Four
representative messages were delivered through the actual `EmailService` and
`JavaMailSenderImpl`, but three HTML escaping assertions failed.

## Passed

- Loopback-only SMTP sink received 4/4 messages:
  - Signup email verification
  - Password reset
  - Subscription payment failure
  - Payment reconciliation alert
- Every recipient used the reserved `.invalid` top-level domain.
- All four subjects used the `[AT.M]` brand.
- All four messages contained an HTML MIME part.
- Verification and reset links used the configured HTTPS public base URL.
- Verification and reset tokens appeared only as part of their callback URLs.
- Reconciliation summary and detail text were HTML escaped.
- Success logs contained only a random `deliveryId` and `outcome=SUCCESS`.
- Failure logs contained only a random `deliveryId`, `outcome=FAILURE`, and the
  exception class.
- Recipient, subject, body, callback URL, token, and test secret markers were
  absent from success and failure logs.

## Product Defects

### WI016-DEFECT-01: Account mail nickname is not HTML escaped

Signup verification and password-reset templates insert the member nickname
directly into HTML.

Pointers:

- `src/main/java/com/atstudio/atstudio/service/EmailService.java:201`
- `src/main/java/com/atstudio/atstudio/service/EmailService.java:218`

### WI016-DEFECT-02: Payment-failure dynamic text is not HTML escaped

The subscription payment-failure template inserts nickname, failure summary,
and retry guidance directly into HTML. The reconciliation template already
uses `escapeHtml` and passed the same adversarial input.

Pointers:

- `src/main/java/com/atstudio/atstudio/service/EmailService.java:235`
- `src/main/java/com/atstudio/atstudio/service/EmailService.java:253`

No product code was changed as part of WI-016.

## External Delivery Gate

No approved external SMTP account or designated test inbox was available.
External provider delivery and human inbox receipt therefore remain a
**human/operations gate**. Local-sink evidence must not be interpreted as proof
of Internet SMTP deliverability, provider authentication, spam placement, or
inbox rendering.

## Isolation and Cleanup

- Shared port `8080`, its database, and port `15173` were not restarted,
  reconfigured, or intentionally mutated by WI-016 commands.
- The sink listened only on an ephemeral `127.0.0.1` port.
- Raw MIME existed only in a current-user-only repo-external directory during
  assertions and was deleted afterward.
- The temporary SMTP sink script and JUnit harness were deleted.
- Sanitized evidence remains under:
  `C:\Users\jm991\AppData\Local\ATStudio\wi016-mail-evidence-20260724T231700\sanitized`

## Related Documents

- [WI-016 Handoff](../agent/WI-20260724-ATS-016-handoff.md)
- [WI-016 Evidence Pack](../agent/WI-20260724-ATS-016-evidence-pack.md)
- [WI-024 Evidence Pack](../agent/WI-20260724-ATS-024-evidence-pack.md)
