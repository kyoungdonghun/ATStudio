---
version: 1.0
last_updated: 2026-10-28
project: ATS
owner: SE
category: guide
status: confirmed
dependencies:
  - path: ../../../docs/registry/git-checkpoint-20261106.md
    reason: Approved bounded native trial preparation
---
# Native trial assembly and read-only probe

WI022 prepares the **new generic cold adapter**, not another run of the old paired
capture. Old successful source/evidence remains reusable and unchanged. These
commands have no SSH implementation and never execute a server command locally.
Default native_trial.py prints DISABLED. MA owns remote execution and private inputs.

## Local commands

```text
python -B scripts/deployment/backup/native_trial.py package --output FRESH_BUNDLE --remote-root /root/atstudio-trials/wi022-FRESH_ID
python -B scripts/deployment/backup/native_trial.py probe-payload --bundle FRESH_BUNDLE --input PRIVATE_PROBE_SPEC --output PRIVATE_PROBE_PAYLOAD
python -B scripts/deployment/backup/native_trial.py assemble --output FRESH_BUNDLE --input PRIVATE_ASSEMBLY_SPEC
```

Package emits six LF-normalized modules, a source/payload SHA256 manifest, and
probe-bootstrap.py. The hook's derived shebang is `/usr/bin/python3 -B`; its
derived hash, not the original CRLF hash, is put into config. Deploy hook mode0700,
other code/input files0600, directories0700. Do not broaden modes to fix access.
On Windows, MA must first establish and verify owner-only inherited NTFS ACLs on
the existing private parent; POSIX chmod bits alone do not establish Windows ACLs.

MA can run `/usr/bin/python3 -B -c BOOTSTRAP_TEXT` through the existing SSH stdin
channel and provide PRIVATE_PROBE_PAYLOAD as stdin. This loads the six pinned
modules into memory: **no remote deployment or directory creation is needed for
the initial probe**. Store stdout directly as private evidence; it includes target
identity/path hashes and must not be echoed into chat. Use argument arrays/existing
safe transport, not string interpolation of private payload into shell commands.

## Probe spec: exact keys

- `target_host`, `machine_sha256`: retained approved target identity.
- `remote_root`: exact fresh path above; `phase`: preflight (default).
- `https_host`: reviewed existing TLS hostname. Fixed curl uses verified HTTPS,
  `--resolve HOST:443:127.0.0.1`, no proxy, no redirect, no certificate bypass and
  expects the exact approved `expected_http_status` (integer404 or503); it contacts
  only loopback. Provide `quarantine_file` and `quarantine_sha256`; the actual
  trusted config bytes must match that pin as well as the response code.
- `source_paths`: existing approved public/private/release roots and additional
  approved nonsecret settings roots. `approved_files`: exact absolute path→SHA256
  map from retained reviewed current_members. New files or changed hashes HOLD;
  a newly scanned superset does not become approved scope.
- `artifact_paths`: reviewed executables/configs to hash; include `mysql_config`.
  `mysql_config`: original source DB config. Probe checks mysql-user readability
  with fixed runuser/test, and rejects include directives pending explicit review.
- `credential_references`: existing private control paths; metadata-only checks,
  never contents. No new credential input or copy.
- `filesystem_paths`: exact sources/settings and future state/work/cache/temp
  paths. Existing nearest parents establish expected device IDs before creation.
- `probe_limits`: max_entries, source_bytes, metadata_bytes and
  projected_local_growth (dump+metadata+cache+temp+other growth bounds).

Probe verifies cold units/process/socket, the exact bound quarantine response, artifact pins, exact approved
file set, root-private credential metadata, disk/inode reserve, MemAvailable>=1GiB
and cgroupv2 availability. It starts no DB/app/restic and writes no files. Actual
root/socket DB authentication is retained proof, not a new stopped-host test.

## Assembly spec and preserved originals

`config` is the full generic config with explicit cold-preserve and complete
cold_host schema (machine/boot,43 sorted table names, safe schema/datadir/config/
quarantine paths, exact historical unit baseline, executable/config pins).
Assembly rejects incomplete host config before remote use. Add
`repository_layout: existing-bucket-root` only for the exact existing bucket-root
repository; default bucket-prefix remains unchanged. Existing repository_id and
actual restic cat-config checks still apply. No init/new-prefix command is added.
For the existing IDC endpoint, set `s3_bucket_lookup: path` and `region: default`
from retained successful configuration. This exact optional setting adds only
`-o s3.bucket-lookup=path -o s3.region=default`; region uses a restricted token
grammar, never arbitrary CLI text. Omitting the setting preserves prior behavior.

Also provide `expected_http_status` (the same exact reviewed status as the probe),
and `run_id` (32 lowercase hex), `approved_source_paths` equal to the
selected source list, `observed_source_bytes`, `observed_members_sha256`, and
`observed_originals` mapping repository_acl/recovery_access/source_inventory/cost
to `{path,sha256}`. Config expected_filesystems must cover all source/settings and
the four derived runtime paths. Settings must stay in the prior approved source
allowlist; do not add application.env, a keyring or new quarantine file to make
the generic schema pass. Generic source_bytes covers **base sources plus dump**.

Each original is an MA-reviewed projection with record_type=MA_OBSERVED_ORIGINAL,
classification=OBSERVED, synthetic=false; kind, repository_id, machine_sha256,
boot_sha256, observed_at and expires_at; and **originrefs [{path,sha256}]** pointing
to separately preserved actual raw SSH JSON or private provider UI AX/screenshot
evidence. Source URL/time/observed values belong in that original custody record.
Assembly reads and verifies origin hashes separately; labels alone are not proof.
It never creates observed facts or authenticates arbitrary statements.

Payload fields match HOST-ADAPTER.md: ACL access, recovery operation/snapshot_id,
source members_sha256, cost upper_bound_krw/approved_limit_krw/new_bytes_bound/scope/
unknown_components. This trial requires both costs exactly numeric0, no unknown
components and conservative bytes. Freshness covers the whole configured window.
Assembly outputs proofs/config and custody.json, but **never issues approval**.
MA separately validates originals and creates the exact config-hash/run/time
approval after current target/cost checks. Preserve raw originals and projections.

## Deployment and execution boundaries

Before copying, use the probe to confirm the exact remote trial root is absent.
Create that root exclusively (mkdir without overwrite), then code/inputs/state/
work/cache/temp directories0700. Upload only manifest-listed normalized files and
new assembled inputs with exclusive creation; verify hashes and modes on target.
Preserve any partial failed directory and choose a new reviewed destination instead
of overwriting. No old path/service/credential is replaced. If the shared operation
lock is absent, its protected parent/file needs explicit in-scope provisioning and
verification; do not silently use another lock path.

execution-plan.json contains exact argv for systemd-run → new guarded_backup.py
execute and a separate bounded owned-unit cleanup invocation. It declares
MemoryHigh768MiB, MemoryMax1GiB, MemorySwapMax0, CPUQuota100%, Nice10 and group cleanup.
The monitor verifies actual limits before effects. The config window is at most
1800s; assembly requires conservative upload/retrieval time plus360s control/dump
allowance. Source DB RuntimeMax is window+120s, outer trial window+180s. These are
bounded stop limits, not permission for app/data changes. Source traffic overhead
or actual provider speed can still cause a bounded HOLD; no automatic backup retry.

## Final independent probe and reconciliation

Use the **same pinned in-memory probe** with phase=final and exactly the
execution-plan's final_probe_binding merged into the original probe spec. It
accepts the deployed root only after verifying the complete deployed code pin set,
same boot/original unit baseline, zero owned transient DB PIDs, absent runtime
socket/directory and the exact bound quarantine response. Do not invent another fresh root to bypass a check.

For ambiguous execution, read the new private state.json, receipt.json/failure.json
and owned claim plus systemd unit state before deciding. Do not run execute again.
The final probe proves cold state, not whether a snapshot was committed; retain
the current attempt and use bounded read-only repository/receipt evidence under
the existing cost scope to reconcile that separate question. No deletion/prune.

Local synthetic tests do not prove native kernel/service/mysql/curl/provider
behavior. Actual authoritative observations and final access restoration belong
to WI024 and must remain separately recorded.

### v3 native preflight correction

The MA-observed retained quarantine intentionally returns404; v2 incorrectly
required503. v3 binds the actual approved status and pinned quarantine file rather
than accepting arbitrary non-success responses. Preflight, cold config and final
probe retain that binding. Prior failed probes/packages remain preserved.

### v4 existing S3 transport compatibility

v3 read-only probe passed under MA ownership before deployment/DB/new backup.
v4 adds the explicitly config-bound path-style/region options used by the existing
working repository. This does not create a prefix/repository or issue new authority;
actual backend access still needs the bounded native validation.

### v5 service-owned source metadata and early diagnostics

MA diagnosed PATH_OWNERSHIP before operations_lock, run/state creation, DB or
repository access. The already approved settings source `/var/lib/atstudio/tmp`
is service-owned mode0700. Do not chmod or relocate it. Only cold-preserve may
bind `cold_host.source_ownership` with that single path mapped to integer
uid/gid/mode/device/inode (mode448 =0700). Ancestors retain the original root-owned,
no-group/world-write and no-symlink rule. The leaf must remain a directory with
exact metadata. Extra paths, live mode and changed/symlink leaves are rejected.
Control/runtime/code/credential guards are unchanged. Capture checks the binding
at entry and every inventory boundary, preserving original inventory metadata.

Early stdout/stderr now append to the fresh root-private state directory at
executor.stdout and executor.stderr. Precreate both files exclusively mode0600
before launch; preserve existing evidence, never truncate. Guarded CLI diagnostics
are fixed codes, and child stderr suppression remains. v4 deployment/failed attempt
remain historical evidence and are not overwrite targets.

### v6r1 retained MySQL paths and private hook diagnostic

The v5 native attempt reached repository inspection but failed MySQL startup on
an AppArmor-denied runtime log path, before dump/new backup. Reuse the successful
source layout without changing policy: `/run/mysqld/mysqld.sock`,
`/run/mysqld/mysqld.pid`, and a unique unit-named log under `/var/log/mysql`.
Preflight/start/final require the shared runtime directory absent; an existing
empty directory is also HOLD. Only the claimed running unit may satisfy the
owned-runtime branch. Cleanup stops that unique unit; it never deletes historical
logs or changes the existing log directories.

Required private `cold_host` fields are `mysql_identity` (integer uid/gid),
`mysql_log_directory` (integer uid/gid/mode/device/inode, mode488 =0750), and
`mysql_log_parent` (same fields, uid0 and mode509 =0775). Bind fresh observed
metadata; these are not instructions to chmod/chown those directories. The root
and `/var` ancestors retain root-trusted checks. Require the actual AppArmor
profile in artifact_pins. The unique logfile uses exclusive/no-follow creation
and assigns ownership only to that newly created file after identity validation.

Hook failure writes fixed code/cleanup classification to exclusive private
`run_dir/cold-hook-diagnostic.json`; existing diagnostics are not overwritten.
This supplements executor stderr, since generic hook stderr is suppressed. It
does not contain MySQL log bodies, credentials, or successful cleanup claims
without checks. Preserve that file and the unique MySQL log when reconciling a
failed attempt. Final cold PASS does not turn a failed backup into success.

The current local package is `candidate-v6r1`, destination
`/root/atstudio-trials/wi022-20261026-v6r1`; retain v1 through v6. Exact hashes and
focused test history are in WI022 evidence. Native deployment/execution remains
MA-owned and is not established by local tests or this guide.

### WI-20261028-ATS-005 local assembly custody

New assembly specs must explicitly provide `credential_references` with exactly
`password_file` and `aws_credentials_file`. These must match the config's existing
canonical absolute references and must not point inside a trial directory. The
assembler does not read their contents. Never globally replace old trial roots
in serialized configuration: only `state_dir`, `work_dir`, `cache_dir`, `temp_dir`,
`mysql_defaults_file`, the executable hook and its hash are rebound by named fields.
The unused MySQL defaults reference is exactly `<new-root>/inputs/mysql-unused.cnf`.
Source references and credentials remain unchanged. Old candidates are preserved.

On Windows run assembly from the source tree with `private-custody.ps1` adjacent
to `native_trial.py`. Packaging creates fresh directories with a protected DACL
allowing only the current SID FullControl. Before writing input contents, the
assembler checks both the directory and the exclusively created file using the
actual Windows ACL. Reparse ancestors, extra access rules and wrong owners fail
closed. A failed file check may leave an empty exclusive file; preserve it and
use a fresh destination after diagnosing the failure. Existing permissions are
never repaired automatically. Python `mkdir(mode=0o700)` alone is not accepted as
proof of this owner-only boundary. This local helper is intentionally not part
of the six-module Linux deployment closure.

For a separately prepared probe payload, use an existing verified private bundle
directory or create a new synthetic/output directory with the helper's `Create`
action; its `Verify` action is read-only. Do not use real private data in tests.
Windows synthetic ACL tests establish local custody behavior only, not target
Linux permissions, observation truth, recurring approval or native execution.
Recurring v2 manifests and policy authority remain owned by the scheduled policy
path; this cold single-run assembler does not issue or fabricate that authority.

### WI-20261028-ATS-008 explicit one-attempt scheduled assembly

`native_trial.py assemble-scheduled --output BUNDLE --input SPEC` is a separate,
explicit local preparation entry. Default invocation remains disabled and the
existing `assemble` entry is unchanged. Run against a fresh packaged bundle only.
This action creates one-attempt policy artifacts from MA-supplied approved inputs;
it performs no dispatch, probe, remote ledger creation or timer activation.

Use the normal assembly spec and add `scheduled` with exactly:

| Field | Required meaning |
|-------|------------------|
| `policy_id` | Fixed MA-selected ID, letters/digits/underscore/hyphen, 1–64 characters |
| `kst_date`, `start_hhmm` | Exact approved YYYY-MM-DD and HH:MM in Asia/Seoul; future start |
| `start_grace_seconds`, `window_seconds` | Integer grace 1–60, grace shorter than window; window within config limit and same KST date |
| `not_before`, `expires_at` | Explicit canonical ISO timestamps with +09:00; exactly start and start plus window |
| `python` | Exactly `resolved_path` and `sha256` from native observation, not the local Windows executable |
| `code_pins` | Exact map of all six package remote paths to actual LF payload hashes plus observed resolved Python hash; no omissions/extras |
| `budget` | Exactly the eight integers listed below |

Budget keys: `max_attempts` (exactly 1), `per_attempt_bytes`,
`per_attempt_cost_krw`, `existing_bytes`, `reserve_bytes`, `capacity_bytes`,
`cost_cap_krw`, `max_output_bytes` (1–1048576). All are
nonnegative integers, with storage/cost bounds checked against the config.
Numeric zero is not provider cost evidence. Original evidence must remain valid
through the entire scheduled window and still passes the existing custody gates.

The run ID is derived first from canonical `[policy_id, kst_date,
"daily-cold-capture"]`, SHA256 truncated to 32 characters. Omit the normal spec's
`run_id`, or provide that exact derived value. Work path, direct plan, scheduled
plan, cleanup and final probe bind the same ID. No caller-generated random ID is
substituted after assembly.

Outputs add `inputs/scheduled-policy.json` (v2), `scheduled-budget.json`,
`scheduled-pins.json`, `scheduled-manifest.json` and
`scheduled-execution-plan.json`. The last plan retains the bounded systemd wrapper
but calls `scheduled_dispatch.py --execute`; its `dispatcher_argv` matches the
native in-process guarded scheduled tuple. Config/policy and full executable
closure hashes bind the manifest. Original `execution-plan.json` is preserved;
no `approval.json` is issued for its direct execution path.

The plan fixes the ledger at `<config.state_dir>/scheduled-ledger`. Existing local
inputs, plans, custody or state cause HOLD before assembly. The native gate must
separately establish no prior scheduled policy/claims **across old roots**;
absence of this new remote root does not prove that. Do not mint a new ID/root to
evade a consumed or uncertain claim. Preserve failed artifacts; reconcile before
any new authority. The plan explicitly leaves native prerequisites unverified.
This entry supports an initial limited trial, not replacement of an existing
scheduled authority or ongoing unattended schedule provisioning.

## Public checkpoint evidence boundary

This public copy redirects unavailable local evidence links to the shared
checkpoint boundary. The original document bytes and detailed REQ/WI evidence
remain in private local custody. No runtime contract or source code changed.
