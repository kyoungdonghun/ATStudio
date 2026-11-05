---
version: 1.0
last_updated: 2026-10-30
project: ATS
owner: SE
category: guide
status: prepared
---
# Isolated recovery of one online capture

WI-20261030-ATS-004 / REQ-20261030-ATS-002. Local preparation only. The Windows
tests use synthetic receipts, files and backends. No native MySQL, repository
request, server start or real restore was executed in this work.

`scripts/deployment/backup/online_restore.py` reuses the bounded runner, exact
snapshot inventory and hash primitives. Its native entry is
`execute_restore(restore_config, restore_approval, capture_config)`. Default CLI
`plan` reads no input and performs no operation. Native command shape, requiring
separate approval and private files, is:

Use `helper_argv(config, module_path, config_path, approval_path,
capture_config_path)` to prepare the exact argument array for one
`ats-online-restore-helper-<restore_id>.service`. It includes memory.high768MiB,
memory.max1GiB, no swap, CPU100%, Nice10 and finite1900-second runtime. It launches
the reviewed module bundle with a clean environment and exact private file paths.
The helper also explicitly unsets PYTHONPATH, PYTHONHOME, PYTHONSTARTUP,
PYTHONINSPECT and PYTHONSAFEPATH inherited through systemd manager configuration.
Approved `helper_allow_ips` must reuse the current capture helper's exact
repository/DNS host addresses, each /32 or /128 (no subnet). Deny-all plus those
addresses is included in the helper unit; native preflight checks the inherited
unit identity and effective allow/deny properties as well as resource limits.
The native entry independently checks its real inherited cgroup. Plain Python
execution outside that helper is expected to refuse. The function only returns
arguments; the separately authorized dispatcher must first verify that exact
helper unit is absent and that paths/binary pins match the approved execution
plan. Never generate authority from these instructions or examples.

## Inputs and boundaries

- `version:1`, `operation:online-isolated-restore`, a fresh 32-lowercase-hex
  `restore_id`, `target_parent:/var/lib/atstudio-online-restore`, and target root
  exactly that parent plus restore ID. The parent must already be root-private;
  creating this parent is a separately listed native preparation effect.
- Exact `capture_dir`, `online_state_path`, `source_roots`, `schema`, sorted
  unique table names, `repository_id`, `capture_receipt_sha256`, canonical
  `capture_config_sha256`, and actual `restore_module_sha256`.
- Actual raw machine-id/boot-id file hashes, MySQL UID/GID, binary pins for
  `/usr/bin/systemd-run`, `/usr/bin/systemctl`, `/usr/bin/mysql`,
  `/usr/bin/mysqldump`, `/usr/sbin/mysqld`. Do not substitute trimmed UUID hashes.
- `limits`: retrieval bytes <=288MiB, dump <=16MiB, metadata <=16MiB, duration
  <=1800 seconds. `approved_cost_krw` is an exact nonnegative integer.
- `helper_allow_ips`: exact current repository and required DNS host IP prefixes
  from the approved network plan, maximum16 unique /32 or /128 entries. Native
  network enforcement remains part of the actual trial evidence, not these tests.
- Approval binds operation and full canonical restore config hash, not-before
  and not-after with a window of at most 30 minutes. It is independent of capture
  approval. The exclusive target directory consumes the attempt; no reuse or
  deletion to retry.
- `proof:{path,sha256}` references separately reviewed native evidence, maximum
  64KiB, no more than six hours valid and covering the entire restore. It binds
  operation `ONE_ISOLATED_ONLINE_RESTORE`, restore ID, target, repository,
  capture receipt, raw machine/boot identity and `source_evidence_sha256`.
  Actual source-app/DB stop, sole restore ownership, no scheduled relaunch and
  private repository must be evidenced. Cost evidence requires no unknown
  components, bound <= approved KRW, additional retrieval >= restore limit and
  inclusion of the preceding capture and its full-content retrieval.

The caller must not fabricate these facts. A previous successful capture or
rounded billing display cannot issue a fresh zero-cost proof. Capture retrieval
up to288MiB and restore retrieval up to288MiB are cumulative (up to576MiB before
other metadata/provider traffic), not interchangeable allowances.

## Native sequence and effects

1. Acquire the existing shared operations lock. Validate exact online config,
   module/binary pins and root-private credential references without displaying
   their contents. Require no Java/MySQL/FFmpeg process and no `/run/mysqld`.
2. Bind COMPLETE state, run/capture identity, last-good and receipt bytes,
   content manifest, dump hash and disabled restore-preparation authority. HOLD
   captures do not qualify. The capture receipt remains `restore_verified:false`.
3. Create only the exclusive target, owner record and private quarantine files.
   Verify the exact stored snapshot inventory and content-manifest hash, then
   retrieve every selected member with its full original hash. File payloads
   become non-executable numbered blobs with a `restored-map.json`; original
   credentials, directory trees and services are never activated by restoration.
4. Initialize a fresh isolated datadir. Two uniquely named transient MySQL units
   and two exclusive logs under existing `/var/log/mysql` are owned by this run.
   Reuse socket-only, no-defaults, no events/binlog/replication/LOCAL INFILE,
   private-network, AF_UNIX-only and strict filesystem controls. Bind only this
   new datadir at `/var/lib/mysql` in the unit mount namespace. Verify live
   namespace separation and mounted datadir device/inode before import.
5. Import through bounded stdin, then redump this isolated database. Compare the
   original dump's dynamic table list, complete token structure and row multiset.
   Ordinary dump comments/row order and the known redundant utf8mb4 charset
   spelling are the only normalizations. Quoted data remain exact. Unsupported
   programmable SQL/DELIMITER or unexpected DDL blocks execution; there is no
   historical fixed43-table,25-row or209-edit acceptance rule.
6. Stop every owned helper and transient unit, even after partial start or an
   expired work deadline. Cleanup has its own 60-second bound, aggregates errors
   and requires runtime directory disappearance. Preserve failed targets/logs;
   ambiguous cleanup yields HOLD. Never kill foreign units or delete production
   data, pins, claims or failed receipts.

The helper cgroup is <=1GiB with the existing CPU/nice protections; isolated
MySQL is <=768MiB with no swap. Require an additional1GiB available-memory reserve,
disk floor max(20%,10GiB), restore tree <=4GiB and each owned MySQL log <=1MiB.
Source app/DB are already stopped, so their active memory is not double counted.
These limits do not prove throughput or capacity under live user load.

Native success creates a separate private `restore-result.json` only after SQL,
all file bytes and cleanup verification. Synthetic tests emit
`SIMULATED_RESTORE_VERIFIED` with `restore_verified:false`. Neither result changes
the online capture's original receipt. This proves isolated SQL/file recovery,
not restored application startup or full disaster recovery of live credentials.

## Unverified until approved native trial

Linux ownership/AppArmor/systemd mount behavior, MySQL dump grammar for actual
current data, provider API/ACL/egress/account totals, restoration duration,
power loss and the currently absent restore parent remain native prerequisites.
The current native baseline is advisory, not reusable execution authority.
