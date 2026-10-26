---
version: 1.0
last_updated: 2026-10-10
project: ATS
owner: SE
category: work-summary
status: draft
dependencies:
  - path: REQ-20261010-ATS-007.md
    reason: Approved Ubuntu package maintenance continuation
  - path: ../agent/WI-20261010-ATS-022-handoff.md
    reason: Local preparation scope and WI023 review gate
---

# WI022 Package Maintenance Preparation

> TL;DR: Local runbook prepared for independent WI023 PG review. No remote package update, reboot, key operation or service change was performed by SE.

## Changes and Boundaries

- Added [os-maintenance.md](../../deploy/single-vps/runbooks/os-maintenance.md): official Noble source/trust checks, idle package-manager and space gates, protected configuration copies, exact simulation review, removal-free upgrade and one controlled reboot.
- Reused completed WI021 access/policy/layout checks. MySQL/Nginx remain masked and stopped; no SQL, app activation, account change, source modification, deletion or purchase.
- Included Ubuntu 24.04 automatic service-restart behavior, conffile preservation limits, fresh recovery/admin proof, changed boot ID, postboot policy checks and exact temporary-agent cleanup.
- Existing host-hardening, helpers, templates and historical evidence were preserved.

## Verification and Risks

Local syntax/document results and hashes are recorded in the [Evidence Pack](../agent/WI-20261010-ATS-022-evidence-pack.md). They do not establish target command compatibility, a safe package set, actual space sufficiency or successful reboot.

Configuration copies are not OS/package/kernel rollback snapshots. Package hooks may restart services; unknown changes, unavailable recovery, removal proposals or insufficient boot space stop execution. No full vulnerability-remediation or production-capacity claim is made.

## Handoff Status

| Stage | Status |
|---|---|
| Local runbook and two deliverables | Prepared; see Evidence Pack for local checks |
| WI023 independent PG review | Required before MA application |
| New-window pinned admin/sudo and root-console authentication | MA reports PASS at20:59KST; recheck before mutation/reboot; not independently verified by SE |
| Source refresh, exact package simulation and measured space approval | NOT RUN by SE |
| Upgrade, reboot, postboot checks and this window's agent cleanup | NOT RUN by SE |

MA should trigger WI023 using this package. Previous WI021 runtime/cleanup PASS remains historical and cannot close the new maintenance window.

The20:59 MA survey reports635,641,856 free bytes on the separate boot filesystem.
Actual kernel/initramfs peak-space review remains HOLD until the package plan is
known; three retained kernel sets may not be removed under this approval.

## Related Documents

- [WI021 final evidence](../agent/WI-20261010-ATS-021-evidence-pack.md): Previous completed baseline.
- [REQ007](REQ-20261010-ATS-007.md): Current authority and remaining deployment gates.
