---
version: 1.0
last_updated: 2026-10-10
project: ATS
owner: SE
category: guide
status: draft
dependencies:
  - path: ../runbooks/restic-backup-plan.md
    reason: Approved cold-capture and recovery design
  - path: ../../../deliverables/user/REQ-20261010-ATS-006.md
    reason: Local implementation approval only
---

# Guarded Backup Preparation

TL;DR: Local orchestration, protocol guards, synthetic tests and a disabled daily
authorization runner are implemented. **Not ready to activate.** No real host
adapter, DB dump, service control, installed restic, network backup, restore,
external-disk copy or alert delivery has been verified or activated.

## Offline Commands

Run from the repository root with Python 3.10+ and no additional packages:

```text
python -B scripts/deployment/backup/guarded_backup.py
python -B scripts/deployment/backup/guarded_backup.py validate --config deploy/single-vps/backup/config.example.json
python -B -m unittest discover -s scripts/deployment/backup -p test_guarded_backup.py -v
```

Default `plan` prints `NOT_CONFIGURED`, exit 0, without reading config or creating
files/processes. The example config intentionally returns `NOT_CONFIGURED`, exit 2.
`plan --config` and `validate` inspect only the explicit JSON file, do not probe
sources/credentials/hosts and do not contact a repository. A complete config
returns `CONFIG_VALID_ONLY`, never target readiness. Private values belong outside
this checkout. Public outputs contain fixed codes, not paths, JSON payloads or stderr.

## Named-Host Integration Still Required

`host_hooks_unconfigured.py` is only a failure marker, **not a working adapter**.
Every host hook, including logical dump, requires implementation and independent
named-host verification. Do not replace hooks with unconditional success or an
operator-supplied boolean fixture. Pin a reviewed root-owned executable and its
SHA-256 in private config. Its exact interface is:

```text
host-hooks OPERATION ABSOLUTE_PRIVATE_CONFIG_PATH ABSOLUTE_PRIVATE_RUN_DIRECTORY
```

Except for `logical-dump`, return one bounded JSON object with `configured: true`
and the required observed predicates in `HOOK_CHECKS` in
`scripts/deployment/backup/guarded_backup.py`. Unknown/missing predicates or
nonzero exit fail closed. stdout/stderr must not include private diagnostic bodies.
The orchestrator discards stderr and never republishes hook JSON.

| Hook | Actual proof required before returning success |
|---|---|
| `preflight` | Exact source/host/release and schema/version/keyring inventory; private repository permissions; independent recovery access; approved transfer/storage/cost allowance; no conflicting operation |
| `close-admission` | Proxy admission closed, including media and new writes |
| `quiesce` | In-flight work drained/explicitly aborted; sole JVM, all workers and FFmpeg exited; relaunch inhibited |
| `fence` | Other DB/file/schema/key-rotation writers fenced for the entire capture |
| `assert-quiescent` | Re-observe all fences and closed admission; not merely read a marker |
| `logical-dump` | Stream a reviewed logical dump to stdout; exit 0 only on successful completion; use protected `mysql_defaults_file`, never CLI password values |
| `validate-dump` | Inspect this run's `database.partial`; prove expected schema, every application table and complete data inventory; return its exact `dump_sha256` |
| `resume` | Authorized matching release/roots/settings; one writer; restricted health check; controlled reopening after capture validation |

No DB command is invented here. `--single-transaction` alone is not a DB/files
consistency boundary. The host adapter must know all writer/relaunch owners,
version-specific schema expectations, DB privileges and recovery access.
It must not detach helpers, escape the inherited cgroup, background operations,
invoke shell strings built from data, or falsely acknowledge asynchronous work.

The fixed cooperative lock is `/run/atstudio-operations/operations.lock`.
Provision its directory under separately approved host setup, root-owned and not
group/world writable. Deployment, capture, check, copy and future maintenance
must use the same lock and keep its inode stable. Do not remove it on service
exit or blindly clear it. This WI does **not** wire existing deployment tooling
into that lock; the host preflight must establish that integration. Locks in
restic remain enabled, with no retries or automatic unlock.

## Execution Gates and Daily Policy

Manual `execute --config PRIVATE_CONFIG --approval PRIVATE_APPROVAL` requires a
private approval matching the SHA-256 of canonical JSON returned by `validate`,
an offset-aware current maintenance interval and a one-use 32-hex run ID.
Linux/root, exact hostname, root-owned protected config/control paths, pinned
executables, credential-file references, disjoint source/work/cache/temp roots,
reviewed device IDs, repository identity/format 2, finite budgets and live helper
cgroup limits are checked. Credential bodies are not read by the orchestrator.
All private directories must already exist; nothing installs/provisions them.

`scheduled` accepts `recurring-policy.example.json` instead of a one-use approval.
It is **disabled by default**. A future approved policy binds exact config hash,
daily `Asia/Seoul` start time, 1-60 second start grace, bounded maintenance window,
policy start/expiry and explicit resume authority. The full capture window must
fit before expiry and within the same KST day. The daily run ID is deterministic
from the config hash/date, so a second invocation cannot reuse its run directory.
The gate is checked again under the nonblocking operations lock. Outside the
start grace it refuses to run; there is no missed-day/boot catch-up or retry.

The service uses `scheduled`; manual capture remains a separate CLI mode. Both
systemd files have no installation section, and the timer deliberately has no
calendar. They are **unrendered, not installable active scheduling**. Named-host
work must implement/test the adapter, approve the recurring policy, render the
matching calendar, provision private files/lock paths and independently test
systemd behavior before enabling anything. This is recurring authorization
preparation, not successful daily unattended operation. No exact time is chosen
by this WI. Expired policy or failed/incomplete maintenance requires operator action.

## Capture and Evidence Boundary

Close admission -> quiesce/fence -> fresh bounded dump -> reviewed inventory and
manifest -> direct capture of both whole roots, actual release target, settings,
dump and manifest -> exact snapshot verification -> explicitly authorized resume.
An empty private root and required `.staging` directories remain in the inventory.
No media archive, source cleanup, repository initialization or retention deletion
is implemented. Exit 3, unknown/nonzero exit, missing summary/member, wrong
snapshot, hash mismatch, resource breach or deadline rejects the recovery point.

Routine cold acceptance retrieves **only manifest and database dump** and checks
their hashes. Exact full member/type/count/size/owner/ordinary-mode inventory is
compared with `restic ls --json`. Source SHA-256 values are retained privately
for later restore validation; local source inventory is checked again before
resume. Media is **not** downloaded in this window. Full-media/repository reads
belong to a separately approved post-resume check or isolated restore. `ls`
metadata and dump hashes do not prove full repository data integrity or restore.

Internal source symlinks, nested mounts, special files and setuid/setgid/sticky
modes are **fail-closed limitations requiring target review**, not silently skipped.
The release link itself is resolved to the exact separately approved real target.
Restic 0.19.1 `ls` does not emit symlink targets; the code never invents this proof.
Go `os.FileMode` special bits are rejected explicitly. No ACL/xattr/SELinux
restoration or Linux ownership behavior has been exercised on this Windows host.

Private state replaces `attempt` and `last_good` atomically in one `state.json`;
completed runs also keep a `receipt.json`. Failures retain earlier good state and
try to write a fixed-code `failure.json`. A publication failure does not advance
last-good; a private completed receipt can remain unpublished for operator review.
No automatic resume on failure. A killed process can leave a nonterminal stage;
an independent checker reports it. A previous maintenance marker blocks new runs.
State-file/disk failure may leave the last durable stage rather than a final error:
operator inspection and external monitoring remain necessary.

## Resource and Cost Limits

Whole service group: MemoryHigh 768MiB, MemoryMax 1GiB, swap disabled,
CPUQuota 100%, Nice 10. Dump and restic run sequentially. The Linux monitor checks
actual cgroup v2 caps, memory events/swap growth, 1GiB MemAvailable reserve,
filesystem device IDs, free bytes/inodes and cache/temp use. Start requires the
greater of 20% filesystem or 10GiB plus all new dump/metadata/cache/temp/other
growth budgets, conservatively on each filesystem. Existing files already count
against free space. During execution reserve/other growth, dump/stdout byte caps,
cache/temp caps and wall/monotonic deadlines are checked. Only owned process groups
are stopped; no reopening while capture helpers remain.

Monitoring is sampled, not a filesystem quota or real-time stop guarantee. Source
hashing and directory scans consume downtime; target measurements are NOT RUN.
Bounded logical size, configured KiB/s rates, `projected_cost_krw` and
`cost_limit_krw` do **not** enforce provider invoice/storage/request/transfer hard
caps. Metadata reads, retries, pack granularity and retained versions add cost.
`retrieval_bytes` bounds planned logical manifest+dump content, not billed network
bytes. Provider metering/cost controls require separately approved verification.
Never automatically increase limits, buy capacity or prune to fit.

## Independent Status and Recovery Preparation

`status --state PRIVATE_STATE_JSON --copy-receipt PRIVATE_COPY_RECEIPT` reads only
explicit local receipts, independently of the app. Supply `--expected-since` and
`--expected-by` as the latest approved capture start/completion window to detect
a missed capture; without a window it reports `SCHEDULE_NOT_CONFIGURED`.
It reports failed/incomplete/maintenance state, >26h source age and >8d independent
copy age. Age is measured from the original capture boundary, not copy finish.
The copy receipt must name exact source/destination snapshot IDs, `captured_at`
and `independently_verified: true`; it is operator evidence, not a copy verifier.
An independent host/monitor must schedule this checker and an approved sender.
No checker timer, external send, receiver or delivery proof is installed here.

`copy-plan` is a guarded future-operation marker, never a disk copy. Exact disk
identity/mount/capacity, separate password, selected source/destination IDs and
off-provider verification still need implementation and approval. `restore-plan`
validates declarations only: new isolated target/DB/roots, exact snapshot, byte
budget, egress denial, disabled services/timers, no effective LIVE credentials
and proven scheduler suppression. It performs no restore, DB import or JVM start.
There is no universal scheduler-off assertion and no restore-verified claim.

## Related Documents

- [Restic plan](../runbooks/restic-backup-plan.md): Governing consistency design.
- [Resource baseline](../runbooks/resource-baseline.md): Shared-host reserve.
- [Scripting contract](https://restic.readthedocs.io/en/stable/075_scripting.html): Official protocol.
- [Restic manual](https://restic.readthedocs.io/en/stable/manual_rest.html): Command flags.
- [Pinned ls source](https://raw.githubusercontent.com/restic/restic/v0.19.1/cmd/restic/cmd_ls.go): Actual JSON fields.
