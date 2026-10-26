---
version: 1.2
last_updated: 2026-10-10
project: ATS
owner: docops
category: registry
status: draft
dependencies:
  - path: ../README.md
    reason: Stage boundary and operator decision inventory
---

# Single-VPS Runbook Index

> Purpose: Locate the ordered procedures and preserve target acceptance boundaries.

## 2026-10-10 Current Implementation Entry

Read the resource baseline and restic plan with
[REQ006](../../../deliverables/user/REQ-20261010-ATS-006.md) and
[final PG review](../../../deliverables/agent/WI-20261010-ATS-014-evidence-pack.md).
Local source/templates and backup core are implemented; target installation,
all host hooks and daily-service integration remain incomplete. Historical
REQ005 design evidence is retained. Stage B and production remain HOLD;
target compatibility, performance, independent copy and restore are NOT RUN.

## Document List

| Document | Purpose | Execution status |
|---|---|---|
| [Initial resource baseline](resource-baseline.md) | Current candidate, admission evidence and target-test gates | Implemented locally; NOT APPLIED |
| [Restic backup plan](restic-backup-plan.md) | Core versus host integration, retention, independent copy and retrieval costs | Core tested with fakes; target NOT RUN; no deletion approval |
| [Backup implementation guide](../backup/README.md) | Default-OFF policy, hook contract, offline CLI and remaining integration | All host hooks unconfigured; no working daily service |
| [Deployment](deployment.md) | Exact target, approval, fresh bootstrap, access/TLS and one-owner rollout | NOT RUN |
| [Backup and isolated restore](backup-restore.md) | Original Stage A recovery procedure; current core status is in the guides above | Target NOT RUN; independent copy/restore not implemented |
| [Acceptance and capacity](acceptance-capacity.md) | Observation, alert ownership, bounded mixed load and public-readiness receipt | NOT RUN; no capacity guarantee |

Read deployment first; approve recovery before admitting target writes; use the
acceptance record for each later target run. Missing target evidence stays HOLD.
Documentation availability never changes a target check to PASS.

## Related Documents

- [Bundle overview](../README.md): Decisions needed now versus on-target.
- [Configuration](../CONFIGURATION.md): Current WI011/012 source/template contract.
- [Approved REQ](../../../deliverables/user/REQ-20261003-ATS-001.md).
