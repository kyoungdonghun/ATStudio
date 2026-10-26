---
version: 1.0
last_updated: 2026-10-10
project: ATS
owner: DocOps
category: work-summary
status: stable
dependencies:
  - path: REQ-20261010-ATS-009.md
    reason: Approved bounded scope
  - path: ../agent/WI-20261010-ATS-032-evidence-pack.md
    reason: Attributed receipts and document verification
---

# WI-20261010-ATS-032: JDBC Verification Integration

TL;DR: **Existing app JDBC driver connection and read-only aggregates verified;
host/access cleanup and four-document validation complete. REQ009 closed within
approved scope; production HOLD.**

MA native result on 2026-10-10 at 22:47:49 KST: exact account/server/database,
server read-only session, 43 tables, 6 active plan keys, zero non-plan rows.
Before/after schema/plans, four DML grants and env metadata remained unchanged.
Host closure passed at 22:48:10; exact owned key removal, empty agent before
stop and fresh Stopped/Disabled passed at 22:48:31. All MA sessions ended.

WI029 and independent WI031 each passed 29 synthetic tests; PG final PASS and
actual driver smoke are distinct from the native result. DocOps integrated four
documents only, without SSH, SQL, secret reads or service operations.

Scoped checks passed: 43 supported IDs, no broken file links or validator
errors/warnings; metadata/dependencies/whitespace passed for all four files.
Historical index content, 12 public input hashes and main/HEAD were preserved.
No repository-wide orphan, external-link or fragment-validation claim.

## Limits and Next Decisions

- No Hikari/Hibernate/Spring startup. Hibernate NOT_RUN, canonical manifest
  UNRECORDED and app_runnable=false remain. Whole JAR is root-private verification
  staging only; SPA remains local. No capacity or full security acceptance.
- Configuration audit only: domain/mail placeholders were not applied or cleared.
  Prior development Gmail receipt remains valid; new-host SMTP is unverified.
- Next user decision: confirm `atmusic.co.kr` ownership or approve a specific
  purchase before DNS/HTTPS. No availability/price claim. Mail account/sender and
  private credential entry need separate decisions; fees/input must be announced.
- No DB/grant/secret changes, installation, purchase, public opening, SMTP,
  Provider action, commit or push. Baseline 2vCPU/8GB/100GB and KRW70000 monthly
  budget do not establish operating capacity or a hard billing cap.
- WI032 is the last WI in REQ009; no blocked successor. Future domain/mail or
  activation work needs its own approved scope and MA-issued WI, not an automatic
  operation or a reserved WI number.

## Related Documents

- [Evidence Pack](../agent/WI-20261010-ATS-032-evidence-pack.md): Receipt ledger and checks.
- [Current entry](../../docs/index.md#current-decision-entry): Newest bounded state.
- [Readiness matrix](../../deploy/single-vps/runbooks/production-readiness.md): Remaining gates.
- [JDBC runbook](../../deploy/single-vps/runbooks/jdbc-readiness.md): Probe boundary.
