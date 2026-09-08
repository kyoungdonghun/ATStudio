---
version: 1.0
last_updated: 2026-09-08
project: ATS
owner: re
category: work-summary
status: stable
dependencies:
  - path: REQ-20260908-ATS-001.md
    reason: Approved release-closeout request
  - path: ../agent/WI-20260908-ATS-001-evidence-pack.md
    reason: Executed test results and limitations
---

# WI-20260908-ATS-001: Terminal Renewal Failure Verification

> Purpose: Report the bounded reliability verification and release the next WI.

## Result

PASS. Focused Gradle/JUnit execution: **18 tests passed, 0 failures, 0 errors, 0 skips**. Baseline: 16 passed. Two H2 integration scenarios were added; two existing unit tests gained stronger side-effect assertions.

| Verified behavior | Result |
| --- | --- |
| Three declines of the same renewal | One order and command, three distinct attempt identities, zero paid-ledger rows |
| Same-day retry protection | Committed retry gate consumed before provider call; duplicate scans make no new charge |
| Terminal failure | Billing agreement SUSPENDED after the third decline; no later automatic provider calls |
| Grace boundary | New downloads allowed before and on due + 3 days; denied the following day |
| Existing license after expiry | Re-download returns the resource with no additional license, history, or counter changes |

## Impact

Only RecurringRenewalCommandIntegrationTest.java, DownloadServiceTest.java and this WI's evidence/summary were changed. Existing provider support was reused unchanged. No product defect was found in the exercised scope. Product source, dependencies, persistent configuration, real DBs/accounts, servers and other worktrees were untouched; no commit was made.

## Limits

This is real service/repository execution against isolated H2 with a fake provider and mocked SMTP/storage boundaries. It is not real Toss, MySQL, email delivery, browser or production proof. Dates were controlled inside tests; no real midnight elapsed. The separate scheduled EXPIRED status transition was not run: entitlement rejection after the date boundary was verified independently of that job. Full-suite/coverage gates were not rerun.

## Next Action

**WI-001 complete: trigger WI-20260908-ATS-002.** MA should generate its handoff via create-wi-handoff-packet and delegate the approved small release-safety fixes. This result does not close the overall REQ or authorize policy changes.

## Related Documents

- [Detailed Evidence and Reproduction](../agent/WI-20260908-ATS-001-evidence-pack.md)
- [Approved REQ and WI Chain](REQ-20260908-ATS-001.md)
- [WI-001 Handoff](../agent/WI-20260908-ATS-001-handoff.md)
