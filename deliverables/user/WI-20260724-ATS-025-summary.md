---
version: 1.0
last_updated: 2026-07-24
project: ATS
owner: se
category: wi-summary
status: confirmed
related_wi: WI-20260724-ATS-025
---

# WI-20260724-ATS-025 Summary

## Verdict

**PASS**

The account and subscription-payment-failure email templates now HTML-escape
every affected untrusted text value at the output boundary. Verification and
password-reset callback URLs retain their existing HTTPS and token behavior,
and the existing reconciliation escaping remains unchanged.

## Changes

- Escaped verification and password-reset nicknames before HTML insertion.
- Escaped the payment-failure nickname, failure summary, and retry guidance
  after applying the existing fallback text.
- Added adversarial regressions covering tags, attributes, ampersands, double
  quotes, and single quotes.
- Strengthened the reconciliation regression to prove its established escaping
  still works.

No mail workflow, subject, callback route, provider integration, schema,
runtime configuration, or reconciliation template logic changed.

## Verification

- `EmailServiceTest`: 10 tests passed, 0 failed.
- Related authentication, user, renewal, and reconciliation slice: 61 tests
  passed, 0 failed.
- Java main and test compilation: passed.
- Actual `EmailService + JavaMailSenderImpl` loopback SMTP transport:
  4 messages delivered and 17/17 contract checks passed.
- Temporary transport harness and compiled artifacts: removed.
- Raw MIME: never persisted.
- External SMTP and a designated real inbox remain a human/operations gate;
  this Work Item does not claim Internet deliverability.
- Documentation validation, diff checks, and value-suppressing secret scans:
  passed.

No commit was created.

## Evidence

See
`deliverables/agent/WI-20260724-ATS-025-evidence-pack.md`.
