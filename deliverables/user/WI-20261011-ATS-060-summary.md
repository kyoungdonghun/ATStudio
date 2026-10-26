---
version: 1.1
last_updated: 2026-10-11
project: ATS
owner: SE
category: work-summary
status: stable
dependencies:
  - path: ../agent/WI-20261011-ATS-060-evidence-pack.md
    reason: Exact candidate identity and bounded local test evidence
  - path: REQ-20261011-ATS-007.md
    reason: Active approved bounded repair scope
---

# WI060 Local Candidate Ready

> Purpose: Hand a fresh exact-byte candidate to PG061 without weakening BPF identity checks.

**Post-review repair complete locally; final PG061 re-review pending; native NOT_RUN by SE.**

- Nginx config/overlays and installation reload now precede the app security
  baseline/probe. The view performs stop/start without a global reload.
- Exact BPF IDs, direction/multiplicity, policy values and DB comparisons remain
  required. Final policy/BPF/DB checks run independently on every exit after a
  grant-write attempt, including readiness/diagnostic/view and grant fsync failure.
- Grant attempts now report JAVA_RELEASE_POSSIBLE until successful publication,
  then JAVA_RELEASED before the next diagnostic. Neither claims Java startup.
- Root-only bounded phase/time snapshots retain exact operands. Public diagnostics
  expose only fixed labels and elapsed time. ID drift or missing evidence is HOLD.
- Candidate141 tests passed, alongside frozen WI055130 and inherited regressions;
  all thirteen local suites passed, including unchanged PG061 reproducers rerun
  by SE. Counts overlap. Exact verification covers81 candidate
  files and161 unchanged historical files. No app source or original WI055 edit.

The [frozen recipe](../../build/WI060-restricted-web/README.md) provides the
one-use MA sequence and containment limits. The
[Evidence Pack](../agent/WI-20261011-ATS-060-evidence-pack.md) records full hashes,
commands, test counts and validation boundaries. Do not edit or reseal reviewed bytes.
The earlier ready/freeze notice is superseded by this F061-01/02 repair; its
pre-review freeze/receipt/delta remain retained. Final ALLOW belongs to PG061.

The prior reload cause remains a hypothesis. No separate NO-APP cause probe was
added, because its global reload adds operational scope and cannot prove the
historical cause. Native timing, BPF behavior and Linux durability remain untested
by SE. Successful local tests are not production or native approval.

**Next:** MA obtains WI061's final verdict on the exact freeze, then follows the
MA-only native gate if allowed. REQ007 remains active; WI058 remains historical HOLD.

## Related Documents

- [WI060 Evidence Pack](../agent/WI-20261011-ATS-060-evidence-pack.md)
- [WI061 handoff](../agent/WI-20261011-ATS-061-handoff.md)
- [REQ007](REQ-20261011-ATS-007.md)
