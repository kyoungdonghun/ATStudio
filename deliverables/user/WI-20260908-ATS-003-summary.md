---
version: 1.0
last_updated: 2026-09-08
project: ATS
owner: se
category: work-summary
status: stable
dependencies:
  - path: REQ-20260908-ATS-001.md
    reason: Approved release-closeout scope
  - path: ../agent/WI-20260908-ATS-003-evidence-pack.md
    reason: Implementation and executed verification
---

# WI-20260908-ATS-003: Preserve Actual Charge History

> Purpose: Fix false or erased recent-charge timestamps during zero-amount payment-method registration.

## Result

PASS: **83 focused tests, 0 failures, 0 errors, 0 skips**. The defect was first reproduced with six failing assertions across entity/H2 tests, then corrected.

- Preparation and key cleanup preserve existing lastChargedAt; a never-charged agreement keeps null.
- Zero-amount registration activates the key without recording a monetary charge.
- Positive subscription, upgrade and renewal finalization still update the timestamp; completed replay preserves it.
- Subscription plan, cycle, period, status and key cleanup semantics are preserved.
- Only the stale next-billing-date sentence in schema.sql's expires_at COMMENT was corrected. No DDL or data operation occurred.

## Delivery Boundary

Product/test code and the init script are frozen. WI-001 tests and parallel WI-002 frontend files were not edited. No dependency change, commit, real provider/mail/DB operation or server restart occurred.

The public runtime **still uses the old JAR**. PID 19376 remained running, and the repository JAR hash was unchanged after focused verification.

The authorized external init script is ready at:

`C:/Users/jm991/AppData/Local/ATStudio/remote-development-20260908/closeout-build.gradle`

It redirects root build outputs to the sibling `build-closeout` directory and loads no secrets or environment bundle. Product build.gradle is unchanged. MA reported starting the isolated aggregate full build; SE did not execute that build or claim its result.

## Limits And Next Action

H2/fake-provider results are not real Toss, MySQL, SMTP or production acceptance. This fix does not reconstruct already-corrupted historical timestamps.

**WI-003 complete.** MA may trigger WI-004 independent review after confirming WI-002 is also complete; WI-005 follows review. Further product/test/script changes require notifying MA before editing so aggregate verification can be rerun.

## Related Documents

- [Evidence, Commands and Test Matrix](../agent/WI-20260908-ATS-003-evidence-pack.md)
- [Approved REQ and WI Chain](REQ-20260908-ATS-001.md)
- [WI-003 Handoff](../agent/WI-20260908-ATS-003-handoff.md)
