---
version: 1.0
last_updated: 2026-10-11
project: ATS
owner: docops
category: work-summary
status: confirmed
dependencies:
  - path: ../agent/WI-20261011-ATS-015-evidence-pack.md
    reason: Attributed MA receipts and scoped verification
  - path: REQ-20261011-ATS-001.md
    reason: Approved scope and completion boundary
---

# WI-20261011-ATS-015: Native Result Integration

> Purpose: Report the completed read-only verification window without claiming deployment completion.

**Native schema PASS; REQ001 approved scope complete; full production HOLD.**

MA's Linux target receipt at2026-10-11 01:34:46 UTC /10:34:46 KST reports Hibernate
VALIDATE_PASS: **43 tables, 520 columns, 234 index entries, 91 FK columns**.
Before/after bootstrap observations matched; probe exit0, stop0/mask0 and cleanup
verified. Independent MA postcheck passed at01:35:19 UTC /10:35:19 KST.

MySQL is inactive/persistently masked, app absent, no DB/app ports/jobs or probe
workers, restricted TLS/Nginx/Certbot timer retained per MA. Root tty1=0 at10:36:14
KST; exact key removal exit0 and empty-agent rc1 preceded restore exit0 and Windows
Stopped/Disabled at10:36:26 KST; later fresh recheck remained restored. Owned
window/console23 closed, instance22 handoff and user tab9 preserved; original18
closed outside this cleanup.

Exact v5/operator/preflight pins and receipts are in the [Evidence Pack](../agent/WI-20261011-ATS-015-evidence-pack.md).
PG's final preflight52/52 is local synthetic evidence, separate from MA native
PASS. Earlier SCP-mode/preflight HOLDs and candidate/operator tests remain
historical; the actual operator ran once after staging, plan and no-connect mapping.

DocOps changed only current index/REQ, this pair and own handoff status. Old
NOT_RUN/HOLD checkpoints remain intact with explicit supersession. Scoped local
document validation covers those five files; no remote or private-data access,
application build/test rerun, commit/push or deletion by DocOps.

**Remaining gates:** canonical fresh manifest UNRECORDED, app_runnable=false;
NS, expiry alerts, unattended renewal/actual reboot, backup/restore, mail/ADMIN,
worker/egress fencing and full startup remain separate. No schema/DML/secret
change, extra purchase/install, app launch or publication occurred. No further
WI is blocked by WI015 in this approved scope; no new operational approval is implied.

## Related Documents

- [REQ001](REQ-20261011-ATS-001.md): Current progress and bounded completion.
- [Evidence Pack](../agent/WI-20261011-ATS-015-evidence-pack.md): Public pins, MA receipt ledger and validation limits.
- [Current decision entry](../../docs/index.md#current-decision-entry): Latest result above preserved history.
