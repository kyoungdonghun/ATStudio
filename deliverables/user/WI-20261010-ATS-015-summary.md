---
version: 1.0
last_updated: 2026-10-10
project: ATS
owner: docops
category: work-summary
status: stable
dependencies:
  - path: ../agent/WI-20261010-ATS-015-evidence-pack.md
    reason: Integration checks and verification boundaries
---

# WI-20261010-ATS-015: Local Preparation Integration

Current-entry/configuration/runbook documentation now distinguishes implemented
local candidates from historical design and untested target operation. Final
PG014 receipt received from MA; no new blocking defect within local preparation.
REQ006 local preparation is closed after the documentation checks below;
target activation remains unapproved and unverified.

2026-10-10 Windows receipts: SE/MA selected Java105 (57 new), settings39 and
backup35 PASS. MA Java rerun: 12 suites, zero failures/errors/skips, 43s, five
tasks executed; settings15.496s, backup0.739s. PG independently passed
Java57/settings39/backup35 plus seven failure probes. These are attributed
receipts, not DOCOPS test reruns or full-build/target proof.

**Production HOLD.** The old JAR was not updated: reviewed-source build/hash and
effective admission proof are required before new env/Hikari10 activation.
Backup core and default-OFF recurring policy exist, but every host hook is
unconfigured; daily-service/lock integration, independent alert transport,
actual external copy and restore are not implemented. Native Linux/restic/S3,
services/DB/load/restore are NOT RUN. Cold checks retrieve dump+manifest only.
Per-run snapshot evidence paths require explicit future retention grouping/ID
review; local runs accumulate without cleanup and can exhaust reserve. No deletion.
No new purchase precondition, supported-user promise or cost cap is introduced.

## Checks and Delivery

- Documentation validator exit0; focused links/anchors/metadata/newlines and
  `git diff --check` PASS. No application test reruns by DOCOPS.
- Seven current-entry/guide files, REQ006 and these two deliverables only; root
  README and application/templates/historical evidence/unrelated dirty files preserved.
- [Evidence Pack](../agent/WI-20261010-ATS-015-evidence-pack.md): Exact paths, checks and identities.
- [REQ006](REQ-20261010-ATS-006.md): Local scope and closure record.

## Related Documents

- [Current decision entry](../../docs/index.md#current-decision-entry).
- [Final independent review](../agent/WI-20261010-ATS-014-evidence-pack.md).
