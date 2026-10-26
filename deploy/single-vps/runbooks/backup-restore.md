---
version: 1.0
last_updated: 2026-10-03
project: ATS
owner: docops
category: guide
status: draft
dependencies:
  - path: ../../../scripts/reconstruction/README.md
    reason: Private keyring inventory and restored-runtime quarantine
  - path: ../../../docs/design/runtime-storage-operations.md
    reason: Paired DB and public/private storage recovery unit
---

# Consistent Backup and Isolated Restore

> Purpose: Define a recoverable capture and rehearsal without claiming an implemented backup service.

## 2026-10-10 Navigation Addendum

The selected [restic backup plan](restic-backup-plan.md) and shared
[resource baseline](resource-baseline.md) supersede this document's earlier
tool/staging/downtime assumptions. Read them before using the historical
procedure below: direct-file restic capture avoids whole-media archive staging;
30 minutes is not a promised first-full window. Weekly plus prechange independent
copy means up to 7 days planned provider-loss RPO, not the daily provider-copy
objective. Exact schedule/outage/targets remain open. This is navigation only;
the original inventory, containment and approval evidence is preserved.
No installation, backup/restore execution or automatic deletion is approved.

## 1. Policy Decisions Before Execution

**Capture, encryption, off-device upload and restore on the new target: NOT RUN.**
No backup/retention/delete scheduler or script is supplied by this WI. Assign a
backup owner and backup-failure recipient before accepting target writes.

The following are approval proposals, not active policy or measured guarantees:

| Item | Starting proposal | Operator must decide |
|---|---|---|
| Frequency | Daily and immediately before/after deployment or bulk media changes | Exact window, owner and acceptable lost-work interval; a successful daily capture alone implies up to 24h planned RPO, not an achieved RPO |
| Downtime | Cold maintenance window initially budgeted at 30 minutes per capture; close all application access/writes for the full capture interval | Measure dry-run throughput first; approve a longer window or a different consistent method if capture cannot finish; never silently resume writers halfway |
| Retention | Up to 7 daily, 4 weekly, 3 monthly and 2 prechange recovery points | Byte budget, time basis, overlap/deduplication and privacy/retention obligations; do not multiply counts without costing bytes |
| Recovery objective | Rehearsal goal of RTO <= 4 hours | Size, transfer, decryption, schema and controlled-start time; report actual time, not this proposal as a guarantee |
| Location | Encrypted external backup plus separately controlled off-provider copy | Named destinations, access/cost limits and recovery without the failed VPS/provider account |
| Deletion | Operator-reviewed expiry only after a newer verified complete recovery point exists | Exact backup IDs, destination, retention/hold checks and explicit destructive approval; no automatic purge here |

Retain at least one verified recovery point until its replacement passes the
approved checks. A failed/incomplete backup is not that replacement. Never
delete runtime originals, pending inputs, keyring entries or audit records to
meet a backup-space target. Cold downtime affects even playback admission in
this conservative procedure; it must be visible to the operator and users.

## 2. Inventory and Space Gate

Use one private snapshot ID and a versioned manifest tying all members to the
same writer-free capture interval. Shared receipts contain only safe labels,
times, counts, hashes, byte totals and outcomes; detailed paths/rows remain in
the restricted inventory.

| Required member | Capture / restore requirement |
|---|---|
| Database | Schema and all application data, including payment/audit ledgers, due schedules, Track processing state and storage mutation journal; verified dump completion and later import test |
| Public root | All originals, MP3 derivatives, thumbnails, pending/claimed inputs and required `.staging` state, including inactive references; no selective audio-only copy |
| Private root | Company, Notice and Question attachments and relevant staged/journal objects, including an explicitly recorded empty root |
| Exact release | Source revision/dirty disposition, JAR hash, frontend manifest, Java/FFmpeg/MySQL versions and schema contract |
| Settings | Approved external configuration, systemd/Nginx/firewall/TLS-renewal setup, root ownership/modes and mount identity; store sensitive bodies only inside protected encrypted backup |
| Key inventory | JWT and complete billing encryption ring with active and retained key IDs; selected integration credentials and TLS keys according to approved policy; do not confuse active key with the entire ring |
| Recovery access | Independent encryption recovery access and provider/domain access references, held separately from the archive; test without the original machine |

Before every capture, measure used/free bytes and inodes on **each** involved
filesystem. Budget live DB/media + logs/temp + next upload/derivative headroom
+ dump + plaintext staging/archive + encrypted staging + prior locally retained
backups. Include temporary multipart copies, processing output and DB growth.
WAV is already large; compression savings and remote deduplication are unknown
until measured. A 100GB VPS is not assumed to fit full media plus both plaintext
and encrypted archives.

```text
additional_local_peak = dump + plaintext_staging + encrypted_staging
                     + required_temp_growth + planned_local_retention
required_free = additional_local_peak + operating_reserve
remote_budget = sum(retained_encrypted_sizes) + next_backup + verification_copy
```

Avoid double counting files already included in measured used bytes; record
which buffers coexist. Starting reserve proposal: the greater of 20% of the
filesystem or 10GiB, plus explicitly budgeted concurrent operations. This is
not a measured capacity setting. If free space/inodes cannot support the
approved peak, **block local full-archive creation**. Seek separate approval
for encrypted streaming or off-host capture with verified completeness,
encryption, transport, interrupted-upload and snapshot-consistency semantics.
No unencrypted cloud upload, unreviewed pipeline or silent paid disk expansion.
A remote transfer that extends cold capture also extends approved downtime.

## 3. Cold Consistent Capture

These are future operator steps, not executed commands. Do not claim that a
`mysqldump --single-transaction` alone atomically captures database and files.

1. Approve exact source tuple, snapshot destination, secret handling and stop/
   start window. Verify release identity, space gate, backup-tool provenance,
   destination permissions and independently recoverable encryption access.
2. Close admission at the proxy, drain or explicitly abort approved in-flight
   activity, and disable automatic restart/relaunch for the capture window.
   Stop the sole application writer and **all** workers. Verify JVM, private
   audio executor and every FFmpeg child have exited. Fence other sessions,
   timers, imports, maintenance jobs and external DB/filesystem writers. No
   catalog edits, schema changes or key rotation during the interval.
3. Record the quiescent interval start. An interrupted job may remain durably
   pending/PROCESSING; preserve its DB and referenced input/staging bytes as a
   pair. Do not delete its claim or retry it during capture.
4. Take a reviewed logical DB backup with an appropriately scoped account and
   record completion/exit status. MySQL may remain running for that read, but
   all business/schema writers must stay fenced. Do not copy a live MySQL data
   directory as an equivalent physical backup. A physical method needs its
   own approved database-consistency procedure.
5. Capture **both complete storage roots**, matched external config/key
   inventory and release manifest while the same writer-free boundary holds.
   Validate dump completion, file count/size/SHA-256 inventory, capture member
   completeness and metadata needed to restore permissions. Record failures;
   no best-effort partial success label. Hash equivalence is not import proof.
6. Close the capture interval only after an immutable complete local or remote
   snapshot is secured and verified. Encrypt with an operator-approved portable
   tool; verify archive integrity/decryptability privately without revealing
   secrets. If encryption/transfer occurs after writers resume, it must read
   the captured immutable set, never live roots. Streaming capture must finish
   and verify before resumption. Failed capture is not a recovery point; decide
   explicitly whether to extend downtime or resume without a new backup.
7. Upload only encrypted bytes to the approved external and off-provider
   locations; verify transferred lengths/hash and a retrieval test. Keep the
   decryption key separate. Provider snapshots alone are not an off-provider
   backup. Off-device transfer/retrieval failure triggers the named owner.
8. Obtain restart authorization as planned, restore exactly one writer with
   matching roots/settings, strict integrity enabled and restricted admission.
   Record downtime and safe health checks. Do not delete plaintext staging or
   old archives except under the separately approved exact-target cleanup.

A capture receipt records start/end, actual downtime, source/release/snapshot
labels, bytes/counts/hash outcomes, destination verification, encryption/recovery
access result and exceptions. A backup is not marked restore-verified until
the next section succeeds in its expressly bounded scope.

## 4. Isolated Restore Rehearsal

1. Approve exact **new isolated** DB and public/private root destinations, port/
   host identity, snapshot ID, restore privileges and storage budget. Never
   restore over a retained TEST or production tuple by inference. Verify archive
   SHA and all decrypted members against the restricted manifest.
2. Establish default-deny egress at the isolation boundary before importing
   anything: deny Provider, SMTP, public/tunnel ingress and production resources.
   Allow only explicitly required local DB/storage access and controlled
   administration. Verify rules with harmless synthetic/local destinations,
   not trial real payment/mail calls. Record firewall evidence without addresses.
3. Keep application services, timers and worker launchers stopped/disabled.
   Do not install imported LIVE/SMTP credentials as effective restore settings.
   Select only required DB/JWT/billing keyring material privately; a retained
   encrypted billing key still requires its historical ring even without Toss
   access. Never substitute newly generated keys for retained ciphertext.
4. Restore DB/media and review pending monetary/audio/storage state offline
   using approved operator tools. Do not execute fresh schema/seed over the
   restored DB. Validate schema, file hashes/ownership and missing-reference
   identities; preserve pending schedules and original evidence unchanged.
5. **Stop before application startup.** There is no verified universal
   scheduler-off switch. `APP_AUDIO_WORKER_ENABLED=false` only removes the
   audio worker; notification=false only removes notifications. SQL init
   `never`, `ddl-auto=validate` and long poll intervals do not suppress local
   business writes, startup recovery, renewal, expiry or withdrawal cleanup.
   Egress denial blocks external effects, not local mutations. A separate WI
   must prove revision-specific suppression or approve an instrumented,
   disposable rehearsal copy and its exact expected local side effects before
   any JVM startup. Do not infer this authority from the restore approval.
6. Once that containment/startup gate is satisfied, run strict storage audit
   and selected read/file-flow checks on the isolated tuple, with exactly one
   approved writer. Require zero missing references for the fresh production
   recovery point. Preserve the historical TEST baseline separately; a retained
   comparison with known exceptions is not production integrity PASS.
7. Record actual recovery time, provenance/hash/schema/key checks, isolation
   proof, startup effects, selected browser/download checks and untested limits.
   A static import-only rehearsal is `IMPORT VERIFIED / RUNTIME NOT RUN`, not
   complete recovery proof. Releasing restored data, enabling external jobs or
   deleting the rehearsal target requires separate approval.

## 5. Historical Boundary

The [2026-09-09 local handoff](../../../scripts/reconstruction/README.md#5-private-recovery-inventory-and-keyring)
completed source/history and private local staging. It is a **non-atomic,
pre-WAV** point, not a current post-WAV backup. This runbook neither resets that
achievement nor makes it portable-encrypted/off-device/restore-verified.
Missing historical media must not be hidden by deleting records or disabling
strict checks. Repair of old TEST data is not an extra first-deployment task.

## Related Documents

- [Deployment](deployment.md#6-rollout-reboot-and-rollback): Compatibility-aware rollback.
- [Acceptance and capacity](acceptance-capacity.md): Alert and load evidence.
- [Reconstruction quarantine](../../../scripts/reconstruction/README.md#6-retained-db-quarantine-and-restore-gates).
- [BillingKeyCrypto](../../../src/main/java/com/atstudio/atstudio/service/payment/billing/BillingKeyCrypto.java).
