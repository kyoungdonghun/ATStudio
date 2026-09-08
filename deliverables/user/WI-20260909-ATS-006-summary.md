---
version: 1.0
last_updated: 2026-09-09
project: ATS
owner: qa
category: work-summary
status: stable
related_wi: WI-20260909-ATS-006
dependencies:
  - path: ../agent/WI-20260909-ATS-006-evidence-pack.md
    reason: Exact source artifacts, counts, limitations and execution history
  - path: REQ-20260909-ATS-001.md
    reason: Approved bounded remediation
---

# WI-20260909-ATS-006: Backend Verification Summary

## Result

**WI006 evidence review complete: backend build and unchanged configured coverage gates PASS, with 19 explicit skips.** MA executed the tests/build; QA independently reconciled the final JSON with all 200 JUnit XML files and the JaCoCo XML. QA did not launch a heavy runner or change product code.

| Measure | Final result |
|---|---:|
| Working tree baseline | `main`, HEAD `8161f0a`, with approved uncommitted remediation |
| Spring Boot in tested application contexts | 4.0.8 |
| Final build | SUCCESS in 2m 20s |
| Test suites | 200 |
| Total tests | 1,825 |
| Passed | 1,806 |
| Failures / errors | 0 / 0 |
| Skipped | 19 |
| LINE coverage | 88.5599% against 80% gate |
| METHOD coverage | 86.2736% against 80% gate |
| BRANCH coverage | 74.4033% against 70% gate |
| INSTRUCTION coverage | 88.4602%; no separate Gradle gate |
| Critical classes | All 7 at 100% LINE and METHOD |

The final run executed tests and JaCoCo report/verification. Compilation and packaging were up-to-date; this is not a fresh clean-checkout build. Read-only comparison with `8161f0a` found only the Boot version change in `build.gradle`, with coverage thresholds, critical-class list and task dependencies unchanged.

QA also matched all 620 inputs in MA's post-run backend source manifest and confirmed the current built JAR hash matches the earlier artifact record. MA confirmed no new backend changes; the separate frontend R1 correction does not delay or expand this WI006 completion.

## Skips And Limits

- Eighteen skipped tests are guarded MySQL proofs: settlement concurrency 3, subscription-correction concurrency 2, billing-prepare concurrency 3, payment concurrency 7, schema validation 1, and administrator-role concurrency 2.
- One storage test aborted because symbolic-link creation was unavailable in this environment. `LocalStorageServiceTest` therefore has 13 passed and one skipped, not 14 passed.
- The seven critical classes still have 11 uncovered branch outcomes across four classes. Their existing gate requires 100% LINE/METHOD, not 100% BRANCH; no blanket security-code 100% claim is made.
- MA's supplied setup used isolated H2/fake/temp inputs with MySQL opt-ins disabled. XML corroborates H2 and Boot 4.0.8 in 49 suites. This does not establish real MySQL, Provider/payment/refund, mail, proxy/ACL, browser/mobile or production acceptance. Disposable H2 schema create/drop is distinct from retained-database DDL.

## Execution History

| Checkpoint | Recorded outcome |
|---|---|
| First full build | Failed after 50s: multipart test fixture treated `InputBuffer` as a functional interface; no tests or coverage result |
| Second full build | Failed after 2m 38s: 1,797 passed, 9 multipart fixture NPE failures, 19 skipped; no passed coverage gate |
| Corrected multipart retry | 10 passed, zero failures/errors/skips; SUCCESS in 18s |
| Final full build | 1,806 passed, zero failures/errors, 19 skipped; SUCCESS in 2m 20s including unchanged JaCoCo gates |

Earlier logs, snapshots and WI005's fixture history are preserved. The final run includes the captured-validation-log and security-chain suites absent from WI001's first focused snapshot. Focused test counts are not added to the final total. Compiler notes and the JVM class-data-sharing warning are retained, not presented as a warning-free clean build.

## Scope And Next Step

Only this summary and the WI006 evidence pack were created. No product/test/policy changes, Git writes, real DB/DDL/media/log/secret operations, runtime restart, Provider/mail actions or subdelegation were performed by QA. The original MA shell wrapper is not recorded in the supplied log; the evidence pack identifies the effective task chain and labels minimal rerun commands as such.

**Ready test list: no additional heavy tests requested for WI006.** MA can consume this evidence for WI013 documentation and WI014 integration after their other dependencies. WI007-WI009, independent review acceptance, the parent REQ and deployment remain outside this completion claim.

## Related Documents

- [Detailed WI006 evidence pack](../agent/WI-20260909-ATS-006-evidence-pack.md)
- [Assigned handoff](../agent/WI-20260909-ATS-006-handoff.md)
- [Approved REQ](REQ-20260909-ATS-001.md)
