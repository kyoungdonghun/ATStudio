# WI011 private input and inspect-only preparation

Prepared locally on 2026-10-23. MA alone owns current authorized staging,
private input handling, server inspection and restoration. SE did not contact
the server or collect real inputs. PG012 must allow this exact candidate first.
The 22 frozen WI005 files plus their manifest remain unchanged; manifest SHA-256
is `3a0f4fd30afc012a8e1a0c46d70b87ed1d2ef5492c2f8143121dc741da35654c`.

Only `stage` and literal `inspect` payloads can be built. No apply, plan,
reconcile, entitlement date or SQL input is accepted. The inspection request
has no day field. The legacy scope name remains frozen, but both approval
records and native invocation are fixed to `mode=inspect`; the external operator
record independently requires `allow_writes=false`.

## Prepare before inviting human input

1. MA checks the actual current user decision covers staging, private inputs
   and evidence retention, read-only DB inspection/start/stop and approved TEST
   semantics without a consent/mail event. Review the WI011/PG012 code pins and
   current source/JAR/transport evidence. No input is requested yet.
2. Create a new single-level directory beneath the existing local application
   data directory, outside the repository. Never use a shared/synced folder.
   The script uses the existing SSH preparation tool's `CreateDirectoryW`
   protected current-user-only ACL pattern. Existing paths are refused.

   ```powershell
   $inspectPrivate = Join-Path $env:LOCALAPPDATA ('ATStudio-WI011-'+[Guid]::NewGuid().ToString('N'))
   powershell.exe -NoProfile -NonInteractive -ExecutionPolicy Bypass -File build/WI20261023-011-fixture-inspect/capture.ps1 -Action Prepare -PrivateDirectory $inspectPrivate
   ```

3. **MA**, outside this tool, records the actual decision in
   `<private-directory>/operator-record.json` and its exact raw-byte SHA-256 in
   `operator-record.sha256`. Write both using exclusive create and inherited
   private directory permissions. Read fresh target boot/machine values from
   private target evidence, never from chat or assumptions. These exact fields
   are required; placeholders below are invalid:

   ```json
   {"wi":"WI-20261023-ATS-011","scope":"WI011_INSPECT_ONLY_STAGE_PRIVATE_INPUTS_DB_READ_STOP","mode":"inspect","decision_reference":"CURRENT_EXPLICIT_USER_INSPECT_ONLY_APPROVAL","candidate_sha256":"3a0f4fd30afc012a8e1a0c46d70b87ed1d2ef5492c2f8143121dc741da35654c","boot_id":"<fresh-private-boot>","machine_id_sha256":"<fresh-private-machine-pin>","nonce":"<new-32-lowercase-hex>","issued_at":0,"expires_at":0,"verified_test_semantics_approved":true,"allow_writes":false}
   ```

   Use actual Unix seconds with a comfortable validity interval, normally 900
   seconds (maximum 900). The raw-byte pin is independently supplied by MA;
   builder/capture/target verify it and preserve the original bytes. A JSON
   marker is not proof of human authority: MA must verify the real decision.
   Neither this tool nor WI005 invents that decision. The wrapper only binds
   this record to the private request in the legacy frozen approval format.

4. Build/send the private staging payload with the existing successful
   `recheck_prefix`/strict SSH stdin caller. No credentials are in this payload.

   ```powershell
   python -I -S -B build/WI20261023-011-fixture-inspect/inspect_tools.py stage --private-directory $inspectPrivate
   ```

   `stage-payload.py` and `stage-payload.sha256` are written privately. The
   wrapper stages exactly the frozen 22 files and manifest under
   `/opt/atstudio/wi20261023-005-fixture`, root0700 directories/root0600 files.
   Existing byte-identical files may be reused; extra paths, nonmatching bytes,
   owners/modes, symlinks and collisions HOLD, without overwrite or deletion.
   The evidence parent is `/var/lib/atstudio/wi20261023-005-fixture`, root0700.
   Candidate/runtime/source/JAR, source MySQL binaries/configs, target and cold
   baseline are checked. Existing legacy approval files or nonce evidence HOLD
   before human capture. Staging starts no DB/app.

5. Require `INSPECT_STAGING_READY`; store its exact JSON response privately as
   `stage-result.json` (exclusive create). Then invite the user to the dedicated
   window. Do not run it inside the agent's captured terminal. Visible launch is
   intentional for the requested human input window:

   ```powershell
   $captureScript = (Resolve-Path build/WI20261023-011-fixture-inspect/capture.ps1).Path
   Start-Process powershell.exe -WindowStyle Normal -ArgumentList @('-NoProfile','-STA','-ExecutionPolicy','Bypass','-File',('"'+$captureScript+'"'),'-Action','Capture','-PrivateDirectory',('"'+$inspectPrivate+'"'))
   ```

## One private capture, then inspect

The form accepts admin/buyer email, nickname and password plus confirmation.
Passwords are masked; the unchanged `fixture_core.inputs` contract validates
all fields through a private stdin pipe. Email <=100 ASCII characters;
nickname `ats_test_` plus 1..11 ASCII letters/digits/underscores;
password 12..72 printable ASCII bytes, no spaces. Longer entries are rejected,
not silently truncated to72. Case-insensitive identity conflicts between the
two proposed accounts are rejected locally. Only a real target inspection can
establish noncollision with existing database rows.

Capture first requires fresh staged readiness. If approval expires while the
user types, valid input is still preserved locally; native building/execution
will HOLD. `inputs.json` is exclusive-create, explicitly protected current-user
ACL, outside the repository. No password, email, nickname, input digest or
private payload digest is printed, put in argv/env, or written to public logs.
Memory zeroization is not claimed. Private plaintext retention is explicitly
authorized; do not display/read it through agent output or screenshots.

```powershell
python -I -S -B build/WI20261023-011-fixture-inspect/inspect_tools.py inspect --private-directory $inspectPrivate
```

The private payload contains the input, so **both it and its digest stay private**.
Send exactly its bytes through the already approved SSH connection. The caller
must read/compare the private `.sha256` file without printing either digest:

```python
raw = (private_dir / (mode + '-payload.py')).read_bytes()  # mode stage/inspect only
assert hashlib.sha256(raw).hexdigest() == (private_dir / (mode + '-payload.sha256')).read_text().strip()
# existing_recheck_prefix is MA's already pinned private SSH argv, ending in
# sudo -n /usr/bin/python3 -I -S -B -. No new host/key discovery or shell quoting.
proc = subprocess.run(existing_recheck_prefix, input=raw, capture_output=True, timeout=360)
assert len(proc.stdout) <= 65536
result = json.loads(proc.stdout)
# Store result privately with exclusive create. Never print stderr/exception,
# raw payload, input, input/target/payload digest, or the operator record.
```

The existing caller uses `-T`, BatchMode, IdentitiesOnly, StrictHostKeyChecking,
the established known-hosts/identity and finite connection/liveness limits.
An SSH exit/timeout is not restoration proof. If interrupted, do not retry;
use the independent 300-second unit bound and MA's exact cold-state post-probe.

On target the wrapper retains root0600 input/request/original operator record
and pin under `private-inputs-<nonce>`, preserves matching fixed legacy approval
files, and invokes **only** frozen `native('inspect',...)`. Runtime checks and
read-only flags/event/network controls remain WI005. No BCrypt helper, app,
login or business DML is invoked. Native private snapshot/inspection evidence
remains on host. The public projection returns only fixed counts, ID1 boolean,
plan numeric ID/type/active/quota and fixed HOLD reasons. It omits all cell,
schema, server, target and identity hashes.

Require `INSPECTION_RECORDED` plus DB_SESSION/STOP/PID_ZERO/SOCKET_GONE/BASELINE
all PASS and an independently observed post-stop cold probe. Then separately
read `inspection.readiness`: it is HOLD for identity collisions, existing ADMIN,
existing work, invalid ID1 state or no eligible finite plan. Native inspection
recorded does not imply those conditions passed. Even if they pass,
`apply_authorized=false` and `date_selected=false` remain explicit.

## Expiry or failure: preserve, do not recollect or replay

No automatic retry/expiry renewal exists. If expiry occurs before native inspect,
preserve the captured file. MA can, within the still-authorized inspect-only
operation, create a fresh private directory/nonce and actual new operator record,
repeat stage guards, then copy the existing `inputs.json` file into that new
private directory without displaying it. Verify both directory ACLs first;
copy exclusive (destination absent), verify the resulting ACL, and use inspect.
Do not open the capture form again. No date/apply approval is created.

After a possibly started inspect, preserve claim, logs, approvals, inputs and
snapshots; verify restoration before any further decision. Fixed approval-file
or nonce collisions intentionally HOLD. This helper does not replace existing
nonmatching approvals or delete evidence to force progress. Stop for MA review.

## Local verification

`python -B -m unittest discover -s build/WI20261023-011-fixture-inspect -p test_inspect.py -v`
passes 8 synthetic tests, including a complete private prepare/readiness/payload
flow without human or network input. `capture.ps1 -Action SelfTest` passes actual Windows
current-user ACL creation, synthetic stdin contract validation, exclusive-create
collision and invalid-input rejection. Synthetic test files remain private.
No human capture/GUI interaction, remote staging, MySQL syntax/current-row
collision or native cleanup has been exercised by SE. The old fixture manifest
remains unchanged. Independent exact-byte review belongs to WI012.
