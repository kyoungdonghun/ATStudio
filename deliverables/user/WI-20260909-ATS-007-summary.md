---
version: 1.0
last_updated: 2026-09-09
project: ATS
owner: qa-fe
category: work-summary
status: stable
related_wi: WI-20260909-ATS-007
dependencies:
  - path: ../agent/WI-20260909-ATS-007-evidence-pack.md
    reason: Exact results, snapshot identity and limitations
---

# WI-20260909-ATS-007: Frontend Regression Summary

## Result

**PASS: post-R1 frontend tests and unchanged aggregate coverage gates.** MA executed the isolated runs; QA reviewed the completed artifacts without launching tests or changing product code.

| Final measure | Result |
|---|---:|
| Test files | 112/112 passed |
| Tests | 1,563/1,563 passed |
| Failures / test skips | 0 / 0 reported |
| Duration | 79.28s |
| Statements | 90.26% against 80% |
| Lines | 92.84% against 80% |
| Functions | 91.21% against 80% |
| Branches | 82.83% against 70% |
| Source snapshot | All 334 inputs match shared and isolated copies |

All four `src/test/coverage` test files are present. Earlier 108-file/1,399-test evidence was incomplete; 112-file/1,516-test evidence was pre-F1; 1,558-test evidence was pre-R1. Only `frontend-r1-full-final-coverage.log` and `frontend-coverage-final.json` establish the current result.

R1 history is retained: all five selected tests failed against the old store; the other 57 were deselected by `-t`, not environment skips. The corrected focused run passed 163 tests in five files before the final full run. WI010 independently closed F1/R1.

## Limits And Handoff

Coverage thresholds/exclusions are unchanged. Aggregate PASS does not mean every authentication path is 100% covered; the detailed report lists remaining per-file gaps. No real account, browser/mobile, OAuth/provider/mail, DB/DDL, runtime or production behavior was validated by QA.

Only the WI007 evidence pack and this summary were created. No extra heavy tests are requested. Return to MA for WI013/WI014 with WI008/WI009; the parent REQ and deployment are not closed here.

## Related Documents

- [Detailed evidence and execution history](../agent/WI-20260909-ATS-007-evidence-pack.md)
- [Assigned handoff](../agent/WI-20260909-ATS-007-handoff.md)
