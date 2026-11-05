---
version: 1.0
last_updated: 2026-10-26
project: ATS
owner: se
category: guide
status: local-candidate
---
# Authenticated status file handoff

REQ-20261026-ATS-007 / WI-20261026-ATS-020. Implementation: `scripts/deployment/backup/status_transfer.py`. Import/default CLI is inert. No SSH, HTTP, Drive, polling daemon, or installed schedule is supplied. An authorized operator/transport must move the private exported bytes between separately owned source staging and monitor directories; this work did not do so.

## Trust and provisioning boundaries

NativeStore opens a private POSIX directory through no-follow directory descriptors, validates owners/modes and path identity, and restricts references to simple filenames. Private regular files must be owned by the current effective user, have a single hardlink and no group/world permissions or special mode bits. Reads are bounded, with before/after descriptor and named-file identity checks. Writes use an exclusive temporary file, file fsync, atomic rename and directory fsync. Windows ACL verification is unsupported and fails closed; synthetic Windows temporary-file tests do not establish native Linux permission or power-loss guarantees. Same-privilege/root writers remain trusted.

Each side needs its own approved protected store and a shared random HMAC key of 32–128 bytes under an explicit key reference. No real key was created or copied. Provision and recover that key separately from public evidence. Anyone holding it, including a compromised source or monitor, can forge assertions; authentication proves possession, not truth of the database/capture or safety of host hooks. Target/source IDs must be pinned outside the received artifact. This protocol never grants ACL, restore, cost, target-operation or execution approval. Key rotation and lost/rolled-back replay journals require a separately reconciled epoch migration; do not delete/reset journals to bypass rejection.

The monitor SMTP reference is a private canonical JSON file containing only `username` and `password`; the resolver returns these in memory to the existing verified-TLS SMTP adapter. Reuse approved Gmail/ID1 references when separately activated. No credential value, reference path, snapshot hash or authenticated artifact is printed by the default CLI/public monitor result. Callers must keep returned artifact bytes and private store files out of public reports.

## Source, publication and receiver

Bind `export(..., source_store=NativeStore(actual_capture_state_directory), state_ref="state.json", ...)` explicitly to the real guarded capture state directory; its protected bounded reader consumes the existing canonical `state.json` directly, without a manual copy. The transfer/key journal store remains a separate argument. Do not manufacture state or update its `last_good.captured_at`. Export retains only fixed attempt fields and original last-good identity/time, and reads the actual scheduler ledger through `ledger_failure_code`. Supply the real export timestamp; directory binding/clock still require trusted deployment configuration and are not independently established by a signed assertion. Close opened stores when the authorized caller finishes.

`export` authenticates a canonical bounded payload containing version, source, target, monotonically increasing sequence, export timestamp, sanitized capture state and fixed dispatch status. The combined `export-record` is both sequence journal and exact private artifact: publication never increments a separate counter without its payload. The returned bytes are the explicit handoff artifact.

`receive` verifies canonical form, fixed fields, HMAC, source/target, integer sequence and original capture/export times before accepting. Future export/capture, older sequence, conflicting same-sequence bytes, decreasing export time or decreasing/removing a previously known capture time are rejected. The single atomic `accepted-record` stores both replay high-water mark and exact authenticated payload. A duplicate with identical bytes is returned for checking again: an unresolved failure is not discarded as already seen.

An exclusive durable transfer lock guards each journal transaction. A failure after lock acquisition conservatively retains the lock, including rejected sequence transitions, until the outcome is reconciled. No automatic unlock or journal reset is implemented. If publication fails, a previously accepted failure remains available for the monitor; the next receiving attempt holds rather than guessing whether a write occurred. Root/owner deletion, rollback of the whole store, disk failure, or external untrusted clocks are outside this local guarantee.

## Checker and notification

`monitor` calls receive, or reuses the last authenticated record if no new handoff exists. Invalid new artifacts do not clear previous failures and add a fixed incomplete-capture alert. It reuses the existing checker for original capture age/missing schedules and dispatch uncertainty. An export older than `max_export_age_seconds` also adds an incomplete-capture alert and returns `transfer: STALE`; the default is one hour, with an explicit positive maximum of 24 hours. This is a configurable checker threshold, not an approved production schedule. Merely delivering/re-exporting an old capture cannot refresh its capture age.

The existing notifier derives a stable event from window/config/fixed codes, reserves before sending and never automatically retries ambiguous submission. Caller-supplied window IDs must remain stable; no scheduled-window generator or event-reconciliation service is installed. SMTP accepted is not recipient received. The monitor currently supplies no independent-copy receipt, so independent-copy freshness remains unverified and alerts accordingly. Corrupt existing trusted storage or unavailable key material may fail closed before notification; an independent supervisor for checker failure remains an activation requirement.

## Verification boundary

Tests use synthetic real temporary files, a test store adapter and fake SMTP. They cover authenticated handoff, duplicate/rollback/conflict handling, wrong target/key/forgery, malformed/noncanonical/oversized input, future/old times, failed publication preservation, protected reference validation and scheduler-ledger-to-mail-code integration. They do not test a real secret, production filesystem ownership, network transfer, delivery, background worker or recovery. Google Drive remains only a separately authorized off-provider storage candidate, not an execution host.

## Explicit runner candidate (WI-20261028-ATS-004)

`monitor_runner.py` is inert without `--execute`. Authorized deployment entrypoints are `python3 -B monitor_runner.py --execute --action export --config <protected-config>` on the source and the same command with `--action monitor` on an independent monitor. No host or schedule has been provisioned. Export writes a private `outbox/handoff.json`; monitoring reads private `inbox/handoff.json`. **These commands do not move files between hosts.** An approved authenticated transport/operator, atomic inbox replacement, cadence and availability supervision still need binding and native verification. Synthetic tests copy bytes in memory only.

The protected canonical JSON config requires: `version:1`, `enabled:true`, `mode:production|test`, absolute `root`, distinct `source_machine_sha256` and `monitor_machine_sha256`, UTC `not_before`/`expires_at`, `timezone:Asia/Seoul`, `start_hhmm`, `window_seconds` (60–1800), `key_ref`, `max_export_age_seconds` (1–86400). Machine pins are SHA256 of the exact `/etc/machine-id` bytes, including any newline. Independently verify machines are distinct: different identifiers alone do not establish independent failure domains. Config and directories must meet existing NativeStore protections; provisioning is not automatic.

Production also requires `capture_directory` and `dispatch_directory` outside `root`, bound to the approved actual capture state and scheduler ledger. The runner uses `<root>/production/{journal,inbox,outbox,alerts}`. Test instead requires an alphanumeric/underscore/hyphen `test_id` of 1–40 characters and uses `<root>/test/<id>/{journal,inbox,outbox,alerts,capture,dispatch}`; production source-directory fields are rejected. The protected journal stores an immutable host/namespace binding; do not clear journals to resolve a mismatch. Provision separate HMAC keys and stores on each side; never copy production records into a test namespace.

Monitor additionally requires `send_authorized:true` and `smtp` with `host`, TLS `port:465`, `sender`, `recipient`, `credential_ref`. This field is an operator configuration gate, not authorization by itself. Key and SMTP private references are resolved from the role's journal store; no literal secret belongs in the config/public evidence. Test mail is visibly prefixed `[TEST <id>]`; production SMTP config without a test ID preserves its existing event identity. Windows native invocation remains unsupported/fail closed.

The expected window uses the most recent configured KST start and a stable namespace/date identity; timestamp freshness remains based on the original authenticated capture time. Polling does not refresh it. Default CLI exits 0 without opening stores; explicit healthy/export returns 0, attention/unknown-delivery/held events return 2. Unexpected checked failures emit fixed `MONITOR_RUNNER_FAILED` with `supervisor_attention:true` and exit 2, without exception details. An independent external supervisor must observe nonzero exits and missed invocations. It is not installed by this candidate. The disabled service bounds runtime to 60 seconds; its timer has no schedule or install section.
