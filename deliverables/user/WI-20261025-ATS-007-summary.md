---
version: 1.0
last_updated: 2026-10-25
project: ATS
owner: DOCOPS
category: reference
status: completed
---
# WI-20261025-ATS-007 Summary

## Prepared Locally
Account/grant application remains complete as recorded in [WI003](WI-20261025-ATS-003-summary.md).
The subsequent provider/operating preparation is local only; stage two as a whole
remains open. The user confirms a registered sole proprietor and will ask the
client to handle NCP and Toss enrollment together. The earlier immediate NCP
signup request is withdrawn; no new signup prompt is pending in this session.

- [Combined client handoff draft](../../build/WI-20261025-ATS-004/client-signup-handoff-ko.md)
  and [policy content/input draft](../../build/WI-20261025-ATS-004/policy-content-and-inputs-ko.md)
  are ready for the user's review, not sent or published. Existing six plans,
  recurring/cancellation/refund evidence and ID1 reply/operations mailbox are reused.
- [Operating modes](../../build/WI-20261025-ATS-005/README.md) define cold/audio-only
  modes, seeding off, a single application scheduler owner and config rollback.
  [Current check](../../build/WI-20261025-ATS-005/local-check.json) passed 11 source
  pins after preserving the prior manifest. These are prepared, not server-applied.
  Audio-only does not block synchronous payment APIs or withdrawal-triggered work.
- [Optional NCP mail implementation](../../build/WI-20261025-ATS-008/README.md)
  retains default SMTP, uses existing dependencies, and avoids automatic resend
  or SMTP fallback after an ambiguous provider response. Initial focused 40 tests
  passed; after the NCP interpolation repair, 16 NCP tests passed and the unchanged
  26 earlier tests were reused. This is not a fresh whole-42-test run.
  [New isolated JAR/source pins](../../build/WI-20261025-ATS-008/checks.json) record
  SHA256 `23a312c331d10d2875308ddea7e8034fb6e13ea48291027064565e833dc80d15`.
  It is NOT_DEPLOYED; historical `f3b748...` and first candidate `0ced47...` remain.

## Remaining Boundaries
Client-owned provider identity/terms and the actual billing contract, numerical
NCP MAIL pricing, domain authority, sender/DNS setup and private key handoff remain.
Published Toss standard fees are not the actual merchant contract. No new ATStudio
accounts are needed. Provider signup is a direct client handoff, not authority for
MA to accept contracts, add payment methods or enable billable services.

Public policies still need operator-specific decisions/finalization and UI/publication;
provider review needs an approved restricted-access plan. Actual target deployment,
delivery and LIVE billing tests are not completed. New costs and real payments
require prior confirmation. Reuse existing credentials/references; do not request
duplicates. No server/private-body/paid operation occurred in this preparation.

Final independent [WI006 evidence](../agent/WI-20261025-ATS-006-evidence-pack.md)
confirms no remaining blocker for this local preparation scope and matching public
hashes/operating checks. This documentation WI is complete; the broader REQ stays open.
