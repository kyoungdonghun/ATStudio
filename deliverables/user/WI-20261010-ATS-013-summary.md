---
version: 1.0
last_updated: 2026-10-10
project: ATS
owner: SE
category: work-summary
status: draft
dependencies:
  - path: REQ-20261010-ATS-006.md
    reason: Approved local implementation
  - path: ../agent/WI-20261010-ATS-013-handoff.md
    reason: Assigned disjoint scope and acceptance boundary
---

# WI-20261010-ATS-013: Guarded Backup Preparation

TL;DR: Implemented local orchestration, offline guards, disabled recurring policy
and fake failure verification. **35/35 tests PASS. Not ready to activate.**
Independent WI014 review remains required. Production remains HOLD.

## Delivered

- Python-standard-library CLI, protocol validation and bounded Linux helper
  runner under `scripts/deployment/backup/`; no package installation.
- Explicit cold-capture state machine, shared operation lock, private fresh dump,
  atomic receipts/last-good preservation, disk/inode/memory/time/byte gates and
  owned-helper cancellation. Default plan/validation makes no runtime changes.
- Full source manifest and exact snapshot member/type/count/size/owner/mode
  validation. Routine remote retrieval is **only manifest and DB dump**. Local
  source hashes are retained for later isolated restore verification.
- Disabled daily policy bound to an exact config hash, explicit KST start grace,
  capture window and expiry; deterministic daily run ID, no catch-up or overlap.
- Unrendered service/timer: whole helper group 768MiB high / 1GiB max, CPU100%,
  Nice10; no active calendar or installation section. No activation performed.
- Independent read-only status checker for failure, incomplete maintenance,
  missed windows, >26h capture age and >8d independent-copy age. No external send.

## Verification and Limits

SE verification on 2026-10-10, Windows, Python 3.14.3: 35 tests PASS in 0.759s;
Python 3.10 grammar parse PASS for five Python files. Tests use generated local
fixtures and mocked helpers/process groups, never real DB/restic/network/services.
Initial 26-test run had two Windows SIGKILL fixture errors; mocks were corrected.

Host hooks, including logical dump, are **all unconfigured**. Source fencing,
dump/schema completeness, actual service restart, repository permissions and
budget evidence still need named-host implementation and validation. The daily
authorization runner is prepared, but the calendar, host adapter, monitoring
schedule and alert sender remain unapplied integration work. Filling values and
enabling the current timer is not sufficient.

Internal source symlinks, nested mounts, special files and setuid/setgid/sticky
modes are rejected pending target review. The real release target is explicit.
No invented restic `ls` symlink-target field is used. Full-media/repository reads,
actual restore and external-disk copy remain separately approved future work.
Projected cost and logical-byte/rate limits do **not** enforce invoice hard caps.

No real credentials, DB, remote host, cloud repository, payment/mail operation,
purchase/install, existing-data deletion or Git action was performed. No agent
was spawned. Existing Java/env/contract/validator files were outside this WI.

## Next Gate

MA should pass this implementation and its final hashes to WI014's independent
review, then follow the REQ chain to WI015. This SE assignment does not start a
new agent or close REQ006. Target performance, provider compatibility, Linux
resource enforcement, restore success and supported-user capacity are NOT RUN.

## Related Documents

- [Implementation guide](../../deploy/single-vps/backup/README.md): Interfaces and open integration gates.
- [Evidence Pack](../agent/WI-20261010-ATS-013-evidence-pack.md): Commands, tests and changed hashes.
- [Approved restic design](../../deploy/single-vps/runbooks/restic-backup-plan.md): Governing recovery boundary.
