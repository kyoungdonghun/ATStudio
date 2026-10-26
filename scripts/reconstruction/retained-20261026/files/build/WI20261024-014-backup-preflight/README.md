# WI-20261024-ATS-014: fixed backup-reference preflight

This candidate implements only the user's current read-only preflight scope. MA alone may transmit it after exact PG review. SE/PG performed synthetic local checks only. It does not stage remote files, start any service/DB/app, run restic, contact a repository/cloud API, create a backup, restore, change grants or assert a billing allowance.

## Reused boundary and exact reads

`make_payload.py` verifies the four immutable helper source pins and the retained WI008 builder pin. Its stdin bootstrap verifies the existing `/opt/atstudio/wi067-tools` frozen tree before imports, then loads the five embedded modules in memory with bytecode disabled. It calls only retained `account_operator.identity()` and `cold()`, never `run()`, DB helpers or capture entries. Current runtime/source/JAR pins and historical successful runtime receipts remain enforced. Cold state is observed before and after; reference failure still attempts the final read-only cold check. No cleanup mutation is required because nothing is started.

`preflight.py` reads exactly six root-private, bounded files using the retained WI069 no-link/stable-inode reader:

- The fixed bootstrap run's `stage.json`, `install.json`, `aws-credentials` and `repository-password`; its directory must be root0700 and `failure.json` absent.
- The original capture's `repository.json` and the established restore run's `public-result.json`.

Exact run/repository/snapshot references are constants sourced from retained `restore_only.py` and the reviewed WI011 adapter. The stage credential/password hashes are compared internally; none escape. The immutable install state is historical installation evidence, not a claim that restic has never run. Repository/result agreement is historical on-host binding only: live cryptographic repository identity, accessibility, current contents and current fixture recovery remain unverified. No broad root scan or newer-backup search is performed. Existing runtime-tree traversal is confined to the previously reviewed fixed tool root.

## MA local preparation and execution

Use the actual `%LOCALAPPDATA%/Python/pythoncore-3.14-64/python.exe`, not WindowsApps aliases. Store payloads and responses in the existing owner-only private execution directory outside this repository. Verify this candidate manifest against the exact PG allowance first. The local builder checks all listed member hashes; it opens each output exclusively and never overwrites an existing payload. It prints only `LOCAL_PAYLOAD_BUILT` or a fixed HOLD code.

1. Build `probe --expires-at <UTC epoch now + 570> --output <new private probe.py>`. MA pipes its bytes to the strict SSH argv below. Capture stdout/stderr privately with finite timeout/output bounds, never stream raw diagnostics. The initial probe may return `target.boot_id` and `target.machine_id_sha256`; keep them private.
2. Build `inspect --boot <observed boot> --machine <observed machine pin> --expires-at <fresh now + 570> --output <new private inspect.py>`. Absolute expiry must have 1..600 seconds remaining at build and each execution gate. Both target fields are mandatory for inspect. This records the already approved read-only scope; it is not backup/audio/app authority or a new approval receipt.
3. Optional PC escrow comparison: MA securely reads the already designated PC escrow file, computes its SHA-256 internally, and sends that lowercase 64-character digest (optionally one LF) to the local builder's stdin with `--escrow-pin-stdin`. Do not put the digest in argv, output or chat. No agent discovers or reads the PC file. The resulting private payload contains that digest, so do not publish its body or whole-payload hash. Omit this option to obtain `pc_escrow=NOT_CHECKED`. A mismatch is HOLD, never fallback to unchecked. Matching bytes alone do not prove recoverability or live repository decryption.
4. MA transmits the inspect bytes once. A final `probe --boot ... --machine ...` with a fresh expiry can independently reconfirm cold state using the same approved read-only scope. Any HOLD/timeout/unknown return stops further work; preserve private evidence and diagnose locally. There is no automatic retry, renew, delete, capture or restore.

Strict retained command, with only the known dedicated host/key/known-hosts paths supplied privately by MA:

```text
ssh.exe -F NUL -4 -T -a -x -o BatchMode=yes -o StrictHostKeyChecking=yes -o UserKnownHostsFile=<dedicated-known> -o GlobalKnownHostsFile=NUL -o HostKeyAlgorithms=ssh-ed25519 -o UpdateHostKeys=no -o ClearAllForwardings=yes -o ConnectTimeout=15 -o IdentityAgent=//./pipe/openssh-ssh-agent -o IdentitiesOnly=yes -i <dedicated-key> -o AddKeysToAgent=no -o PasswordAuthentication=no -o KbdInteractiveAuthentication=no -o PreferredAuthentications=publickey -o ServerAliveInterval=10 -o ServerAliveCountMax=2 -o LogLevel=ERROR atadmin@<exact-known-host> sudo -n /usr/bin/python3 -I -S -B -
```

Do not interpolate payload text into a shell command. Use a subprocess byte stdin and captured byte stdout/stderr as in the retained MA transport, with a 120-second timeout and 64KiB response cap. Never read the private key, start an agent, unlock, alter firewall rules or contact the server from SE/PG. MA restores its separately authorized temporary access after its session.

## Output and limits

Success is `PREFLIGHT_PASS` or `REFERENCES_OBSERVED`. The fixed public projection includes `cold_before/cold_after`, `HOST_REFERENCE_MATCH`, `HISTORICAL_BINDING_MATCH`, optional PC `REFERENCE_MATCH/NOT_CHECKED`, and explicit `NOT_QUERIED/NOT_CHECKED/NOT_VERIFIED_BY_THIS_PROBE/NOT_ESTABLISHED/NOT_RUN` boundaries. Apart from the initial probe's private target binding, no host path, address, repository/snapshot/run identifier, secret, secret hash or raw exception is output. A successful historical binding is not current backup coverage or a cost PASS. Provider/account billing observations belong to MA's separate evidence.

## Focused reproduction

```text
<actual-python> -B -m unittest discover -s build/WI20261024-014-backup-preflight -p test_preflight.py -v
```

Six local tests use synthetic in-memory references and injected cold/identity helpers. No native server behavior is claimed. Candidate bundle algorithm: SHA-256 of the UTF-8 compact sorted JSON map of member filename to SHA-256. Preserve all original WI008/WI011 bytes and prior evidence. Rollback means declining this candidate; there is no remote state to undo from local preparation.
