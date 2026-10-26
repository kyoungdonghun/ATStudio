# WI-20261024-ATS-011: existing-repository current capture preparation

**Local preparation only. Native execution is not authorized.** `entry.py` defaults to `plan`, which reads no private input and performs no service, DB, network or file mutation. A later explicit user decision, exact PG-reviewed candidate, fresh target/inventory and independently verified zero-additional-paid-cost evidence are mandatory for capture. No current approval/cost receipt is generated here.

## Implementation and limits

`adapter.ExistingCapture` reuses frozen WI069 command, MySQL, restic, file verification and strict SQL-equivalence helpers. Eleven unchanged public helper files are in `lib/`. It does not invoke the consumed first-capture or old restore entry. The new fixed stages are existing repository/config and snapshot anchor; read-only source SQL capture; one new backup; exact new-snapshot restore into a new private directory; isolated MySQL initialization/import; full SQL verification; independent cleanup.

- No restic init, forget, prune, unlock, delete, automatic retry, original-DB restore, app/login or business DML. `--initialize-insecure` is allowed only for the fresh isolated database mount; repository initialization is never allowed.
- Exact original repository ID/bootstrap password reference is retained. Credentials remain host-private and must match the existing immutable bootstrap stage plus the MA's PC escrow digest. No key/password replacement is provided.
- Current source roots: public/private storage, app temp, current WI067 JAR release, retained WI060 frontend and pinned WI067 public runtime. The old backend and 144-file/71,500,702-byte aggregate are not used as current coverage. Frontend entries alone are read from the pinned original allowlist. Current member/byte totals are measured.
- Source/JAR/runtime pins are retained; source processes must be cold. Existing MySQL stays masked. Source-mode MySQL is read-only/super-read-only, networking disabled, events DISABLED, persisted globals/replica startup disabled. Old bounded command/cgroup/disk-floor controls are retained; the overall approval window is at most 1,800 seconds.
- Source snapshot includes all 43 table counts and both complete deterministic dumps, at most 2 MiB each. Actual column metadata is observed, compared and retained in the private verification workspace; it is not included in the snapshot payload. Current actors USER1/ADMIN2/USER3, active INDIVIDUAL plan1 quota5, exact approved grant1 and expected row counts must match. Grant dates are observation preconditions, never instructions to change them. Expected admin-audit count may be 1 or 2 only when explicitly bound by the future decision; this supports a separately approved completed correction without silently applying it.
- Recovered files must match every expected hash/type/mode/owner/empty directory. Recovered SQL must have identical full table counts/column metadata and either exact raw dump identities or the retained narrow 209-column charset-insertion proof with every other byte unchanged. No generic text normalization is used.
- Verification covers the **new snapshot only**. There is no repository-wide `check --read-data`, health scan or claim that every old snapshot was verified. Runtime secrets/credentials remain excluded; `full_disaster_recovery` stays false.
- Snapshot outcome is NOT_ATTEMPTED/POSSIBLE/IDENTIFIED. Lost return/result publication after native dispatch becomes UNKNOWN_POSSIBLE, never “no backup happened.” Preserve fresh paths and use a separately authorized read-only reconciliation; do not retry capture or remove partial artifacts.
- Cleanup records STOP, PID_ZERO, SOCKET_GONE and COLD_BASELINE independently, including already completed units. Cleanup does not require unexpired operation/cost approval. Source/DB artifacts and all prior evidence are preserved.

## Future authorization records

The exact schemas and constraints are executable in `backup_plan.authority`; `test_preparation.contract` contains synthetic examples for tests only. Do not use those examples as real authorization.

The approval has `wi`, `scope`, `mode`, `allow_native`, candidate/decision/cost pins, fresh run ID, issued/expiry times, exact boot/machine, repository/bootstrap references, escrow pin, inventory/unit pins, expected fixture counts and the complete safe grant projection. The separately supplied **raw decision file SHA** must match both the caller pin and approval. Its exact fields record the actual latest user decision by MA, including candidate/run/target/window/cost/baseline/expected state and exclusions. Pretty-printed decision bytes are preserved unchanged.

For initial `inventory`, baseline/units/escrow/cost pins are null and cost bodies are absent. It reads current cold metadata only, without DB or cloud access, and returns the baseline pins needed for capture. This prevents a circular prerequisite. For `capture`, all real pins are mandatory. MA must recompute current inventory and bind it in the later record. New stage directories include mode and fresh run ID; capture's work directory is exclusive and cannot be replayed.

Cost records are **external MA-reviewed evidence**, not provider billing enforcement. The code checks their raw pins, freshness (observed within 600 seconds), coverage through the approval expiry, exact operation scope and zero additional paid KRW. Source evidence must identify the same repository, measured existing bytes within the approved bound, capture-byte bound, account/rates/request/egress/tax evidence and continuing storage after the chosen horizon. References are evidence hashes, not fabricated provider statements. A positive boolean is valid only when the underlying account/provider evidence actually supports it.

No current rates, account free allowance, request ceiling or extra charge is known from this local preparation. Unknown/stale evidence, a nonzero upper charge, unsupported request/egress bound, or storage cost after the planning horizon must HOLD. The default additional paid budget is **0 KRW**. Do not interpret upload/download rate 4096, command deadlines, source/work size or `max_repository_bytes` as a provider billing cap. Existing repository size is supplied by the later approved account/evidence inspection; this candidate does not independently query billed S3 bytes. No deletion is assumed when `retention_days` ends, and no automatic retention job is installed.

The later cost method is: refresh the actual account's remaining storage/request/egress allowances and current rates/tax; measure current repository bytes/object count using separately authorized bounded metadata; bound new snapshot upload/metadata/list requests and one exact restore including retry behavior; account for simultaneous retained snapshots and continued storage beyond the planning horizon. Preserve those sources and calculation privately. If a finite zero-charge bound cannot be established, do not issue the capture decision. No purchase or provider setting change follows from this recipe.

## Local checks and later MA transport

```powershell
$py = Join-Path $env:LOCALAPPDATA 'Python/pythoncore-3.14-64/python.exe'
& $py -B build/WI20261024-011-backup-preparation/entry.py
& $py -B -m unittest discover -s build/WI20261024-011-backup-preparation -p test_preparation.py -v
```

After separate future approval, MA writes real records in the existing owner-only private execution directory. The serializer is local-only and exclusive-output; it does not start SSH. For initial inventory omit `--cost` and `--cost-source`:

```text
<python> -B make_payload.py inventory --candidate-sha256 <reviewed pin> --approval <private file> --decision <raw private file> --decision-sha256 <exact raw pin> --output <new private stdin payload>
<python> -B make_payload.py capture --candidate-sha256 <same pin> --approval <new exact private file> --decision <new raw private file> --decision-sha256 <exact raw pin> --cost <private cost record> --cost-source <private cost source record> --output <new private stdin payload>
```

Remote bootstrap verifies every decoded member, exact authority/cost/current target **before staging**, then exclusively creates `/opt/atstudio/wi011-backup-<run>-<mode>` (root0700; files0600), preserves raw authority bytes and invokes the fixed entry. `make_payload` does not emit the private frame/hash. Do not print approval, escrow, inventory, source-proof or transport contents into chat. Store bounded stdout/stderr privately and report fixed status/phase, safe counts and cleanup only. The current inventory response's hashes are private inputs to the next MA decision.

MA reuses the successful argv exactly, with payload bytes via stdin and no shell interpolation:

```text
ssh.exe -F NUL -4 -T -a -x -o BatchMode=yes -o StrictHostKeyChecking=yes
 -o UserKnownHostsFile=<dedicated known_hosts.first-host> -o GlobalKnownHostsFile=NUL
 -o HostKeyAlgorithms=ssh-ed25519 -o UpdateHostKeys=no -o ClearAllForwardings=yes
 -o ConnectTimeout=15 -o IdentityAgent=//./pipe/openssh-ssh-agent -o IdentitiesOnly=yes
 -i <dedicated key> -o AddKeysToAgent=no -o PasswordAuthentication=no
 -o KbdInteractiveAuthentication=no -o PreferredAuthentications=publickey
 -o ServerAliveInterval=10 -o ServerAliveCountMax=2 -o LogLevel=ERROR
 atadmin@<exact dedicated known host> sudo -n /usr/bin/python3 -I -S -B -
```

No agent/key auto-start, private-key read, password fallback or native operation is authorized now. Following eventual capture, require the safe PASS result and all four cleanup checks, a separately approved cold post-observation and access restoration. Then independently assess whether this actual recovery evidence satisfies a later audio authority; it does not automatically authorize audio or repair old receipts.

## Current state and proposal

The sanitized WI010 native receipt observed 2026-10-24: users3, plans6, grant/subscription1, audit1, work quiet; grant1/user3/plan1 is MONTHLY/CANCELLED, start=expiry=2026-10-24, pending NULL. Old backup covers 43 tables/6 rows before these users. See [grant-date proposal](grant-date-proposal.md). No real state was re-observed by SE and no date change is implemented.

Focused synthetic tests cover default deny, positive future contracts, cost HOLDs, exact restic admission, isolation, independent cleanup, dependency preservation and actual serialized stdin syntax. These do not prove native/cloud execution, real pricing, fresh coverage or real recovery. Independent PG WI012 exact review is required before local preparation is complete.
