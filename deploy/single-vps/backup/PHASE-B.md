---
version: 1.0
last_updated: 2026-10-30
project: ATS
owner: SE
category: guide
status: prepared
dependencies:
  - path: ../../../docs/registry/git-checkpoint-20261106.md
    reason: Local assembly implementation and tests
  - path: ONLINE.md
    reason: Existing capture authority and HOLD semantics
---
# Online native trial preparation

This is a local preparation result, not server execution authority. No server,
MySQL, application, storage API, backup or restore was run for WI003. Reuse the
existing final JAR (94b2c668…; 70,356,576 bytes). Python module hashes change with
this preparation; do not reuse earlier module-pinned online configuration.

## Local manifest and exact inputs

`python -B scripts/deployment/backup/phase_b_trial.py prepare-manifest --jar
<existing-final-jar>` emits only a deployment manifest. It reads the JAR and seven
reviewed modules, records hashes/modes and required private input categories, and
sets `execution_authorized:false`. It neither copies secrets nor emits authority.
The preparation at `build/WI-20261030-ATS-003/prepared-manifest.json` is a retained
local result; regenerate after any reviewed module changes.

Trial configuration is validated by `plan(config)` in `phase_b_trial.py`; the
synthetic `configuration()` in its focused test documents every structural field
using non-routable examples. It must not be submitted as an actual configuration.
Native facts required are exact raw **and** canonical machine/boot hashes, current
baseline unit properties, mysql identity/log-directory metadata, binary/settings
pins, the approved source settings list and repository/credential references,
private helper endpoint/DNS host addresses, and approved loopback ports. The
historical native probe hashes raw identity bytes, whereas online capture hashes
stripped bytes. `identity_pair(raw)` explicitly records both; never interchange
them or manufacture a canonical hash from a raw digest.

The upload stage uses a new `/var/lib/atstudio-online-trial/<run-id>` root with
root:root 0751 traversal, root-owned `release` and `code` directories (0555), and
root-only `inputs` (0700). JAR is 0444; Python modules 0555. An already reviewed
strict existing upload mechanism must verify this manifest and exclusive target.
The adapter does not provide SSH, invent connection credentials or upload files.
Private environment content is extracted from existing authorized references,
not requested again from the user. It must contain only the allowlisted one-line
variables in `NativeTrial.preflight`; JVM injection and alternate Spring config
variables are rejected. Unknown needed variables require a scoped tool review,
not a new account or plaintext secret in chat.
App/helper units also explicitly remove manager-provided JVM/Spring injection and
Python search-path/interactive variables via `UnsetEnvironment`; actual unit
property enforcement is a native check, not inferred from the Windows tests.

The `capture_contract.settings` list includes the retained frontend release
`/opt/atstudio/releases/wi060-web-20261011`, `/opt/atstudio/wi067-tools`, the exact
app-owned tmp directory and all separately reviewed required setting/key paths.
Public/private referenced media and the new trial JAR release are separate sources.
The old deployed JAR remains untouched; its old backup is not re-labelled as the
new candidate. The75 MB historical source observation and70 MB candidate JAR are
different sets. Exact selected inventory and total bytes must be rechecked. The
manifest alone does not prove coverage of all settings/keys or a full recovery set.

## Approved native sequence (not executed here)

1. Fresh host/source/ACL/cost checks and the separate user's phase-B approval.
   Record original firewall/access state. Existing proof data is not current
   merely because it remains within a file; no estimates become final invoices.
2. Upload the pinned artifact manifest to the fresh target; place reviewed private
   trial config, approval and proof there. `phase_b_trial.py plan --config ...`
   performs validation only. Outer approval binds the exact configuration and
   proof hash; proof references real observation evidence, never generated flags.
3. `stage --config ... --approval ... --proof ...` creates exclusive control and
   helper leaves, the trial-local release link and root-only socket options file.
   It never changes `/opt/atstudio/current`, media ownership or existing state.
4. `start` with the same arguments claims the exact run and starts only its source
   DB and app units. Failure attempts owned cleanup. Success is explicitly
   `STARTED_AWAITING_ACTUAL_CAPTURE_EVIDENCE`, not capture success. It retains a
   private ready receipt with process PID/start ticks/netns and an absolute deadline.
5. MA checks the actual started target, table engine inventory, pinned settings,
   helper network enforcement and current cumulative cost. Assemble the existing
   online config/approval/proofs against these facts. The static outer config
   fixes source roots/schema/settings/repository references, while the inner
   capture proof can be prepared only now. No further user approval is required
   for these steps inside a separately approved B scope.
6. `prepare-capture-binding --config ... --ready ... --online-config ...
   --online-approval ...` checks the existing inner authority and returns a hash
   binding with `approval_issued:false`. Preserve it privately as
   `inputs/capture-stage-binding.json`. It does not issue capture authority.
7. `capture --config ... --approval ... --proof ...` verifies the same live tuple,
   inner bindings and shared remaining deadline, claims one attempt, invokes the
   existing online executor in the bounded helper, and always attempts helper,
   app and DB stop—even if an inner input is stale or the capture claim exists.
   Preserve HOLD and pins on unknown outcomes. Do not replay a consumed attempt.
8. The separately approved WI004 restore adapter runs only after confirmed source
   app/DB/helper stop, against the exact completed capture. Use its bounded helper
   command, independent restore authority and fresh isolated root. The capture
   receipt remains `restore_verified:false`; restore writes its separate receipt.
9. `stop --config ...` is an evidence-bound cleanup entry, valid even after the
   execution window expires. It stops only units bearing the matching generated
   name/description, attempts every owned unit despite individual failures, saves
   each cleanup result and preserves all control/claim/failed/snapshot evidence.
   Restore firewall/key/agent/provider access using the existing MA procedure.

The outer approval lasts at most60 minutes; start records a shared deadline at
most38 minutes from startup, and unit RuntimeMax is40 minutes. Capture itself is
at most30 minutes. Restore has its separately approved30-minute bound after source
shutdown. These are different bounds, not a promise that the entire work takes30 minutes.

Each native phase requires Linux/root. Local tests use injected fakes. Plan output
and fixture booleans never count as actual host, isolation, provider or approval
evidence. Pending execution facts stop that phase; they do not justify widening
permissions or using a different JAR.

## Isolation and limits

DB uses the retained AppArmor runtime `/run/mysqld`, old datadir and log-directory
contract, but a uniquely named transient unit. Network is enabled only within a
new private network namespace, bound to 127.0.0.1; events, replication, upgrades,
local infile and business writes are disabled. App joins this exact namespace.
The adapter checks equal app/DB namespace IDs, different host namespace, loopback
as the only interface, and exact executable/process-start identities. It exposes
no port to Nginx or the host namespace. Root helper uses the AF_UNIX MySQL socket
and separately allowlisted repository/DNS egress; native enforcement still needs
fresh proof before capture.

App CLI forces SQL initialization off, schema validation, control-only polling,
all ordinary background jobs/audio/bootstrap off and strict startup storage audit.
The source DB remains super-read-only; compatibility is a native trial condition.
The new `.continuity-owner` under private media and its matching state/mailbox are
proposed technical writes requiring separate phase-B execution approval. Once
created under that approval, they must survive cleanup.

Peak pre-reserved memory is 5.75 GiB: app3 GiB + DB768 MiB + helper1 GiB + reserve1 GiB.
Disk requires max(20%,10 GiB) plus6 GiB headroom and10% free inodes. Restore follows
source app/DB shutdown rather than adding another simultaneous DB peak. Each
capture is at most1800 seconds,256 MiB source,16 MiB dump,16 MiB metadata,288 MiB
full-content retrieval and1 MiB/s transfer in either direction. Restore adds up to
another288 MiB full-content retrieval. Provider accounting must additionally cover
metadata/protocol/request overhead and cumulative prior transfers;576 MiB is not
an exact network invoice. The shared trial deadline cannot be reset by the capture
phase. Unit runtime caps are a backstop, not proof that cleanup succeeded.

Only `/var/lib/atstudio/tmp` may use the new online settings-source exception,
bound to exact app UID/GID0700/device/inode. Control, code and credential paths
remain root-trusted. The cold adapter's original exception and behavior are unchanged.

## Public checkpoint evidence boundary

This public copy redirects unavailable local evidence links to the shared
checkpoint boundary. The original document bytes and detailed REQ/WI evidence
remain in private local custody. No runtime contract or source code changed.
