---
version: 0.25.0
last_updated: 2026-10-17
project: ATS
owner: PG
category: evidence-pack
status: active
dependencies:
  - path: WI-20261011-ATS-070-handoff.md
    reason: Independent incremental review scope
  - path: WI-20261011-ATS-069-handoff.md
    reason: Candidate ownership and approved backup boundaries
  - path: ../../docs/policies/security-policy.md
    reason: Private credential and evidence constraints
---

# Evidence Pack: WI-20261011-ATS-070

> Purpose: Record incremental independent review of WI069 without granting untested native steps.

## Summary

TL;DR: **ACCEPT_BOUNDED_BACKUP_RESTORE_FOR_AUTH_GATE**. The pre-feature WI060
backup/restore prerequisite is satisfied by the bounded native evidence below.
The one-use restore allowance is consumed. This acceptance is not authorization
to run authentication/app/Gmail, another restore, or any further mutation. WI070
remains active: full quality completion and critical100% coverage are not established.
There is no full-disaster-recovery claim and no new staged application-root coverage.

### Native Completion And Independent Poststate

Evidence owner: MA; observation date2026-10-17KST; native environment is the
already bound Ubuntu24.04/MySQL8.0.46 target. PG independently reviewed the exact
code and local tests, and accepts the supplied sanitized native receipts. PG did
not connect to the target, access private SQL/credentials, or run an extra audit.

- StageInventory87406c preceded the single reviewed Restore. MA reports matching
  original inventory2908455e... and units861518...; no other mutation/app execution.
- Native receipt12ca09: run `a860c4be941b42189e99d1d6d0ba7727`, stageCOMPLETE,
  statusBOUNDED_RESTORE_ONLY_PASS, cleanup true, cleanup_errors empty,
  app_startNOT_RUN, backupNOT_RUN_EXISTING_SNAPSHOT, full_disaster_recovery false.
- This run used exactly bundle
  `2ba25b72174b495216a87338142536b69e67162147d112d5d8bc1ec99b0ddb81` and client
  `79232dcfa406ee54056e20a0cb1b46751934c0ef06849e44fb5745b0aa4f4ea8` once.
- Original capture remains `9ef6927ad8674c129729c9720284ff2f`, repository
  `36fc435727e7518894734fa2f49eb65481f4b015e91874a4dbfe3f8a2a3172df`, full snapshot
  `12e37d997ba170762201bab32ea5471f8ad374a71a06e122deb7bed2f0d04e45`.
  Original payload manifest/proof bindings and historical failed runs are preserved.
- Independent poststatea8483e: MySQL masked/inactive, PID0; Java, mysqld, mariadb
  and FFmpeg counts all0; nginx active with unchanged PID34885 and boot identity.
  Free bytes95,938,072,576. No further native inspection was requested or performed by PG.
- MA verified-content scalareb59be: actual_column_metadata_equal true,
  all_other_bytes_equal true, edits209, proof
  MYSQL_8046_REDUNDANT_COLUMN_CHARSET_INSERTIONS, raw_hashes_equal false retained.
  Source/restored each have43 tables and6 rows in total; complete count-map equality
  is true. This is the constrained metadata-backed proof, not raw-hash equality
  or unrestricted text replacement. Full snapshot binding/fullDR false remain intact.

| Native file under the successful run root | Bytes | SHA-256 supplied by MA |
| --- | --- | --- |
| public-result.json | 547 | `e56de048c842f8096ebdb44b27756306b49e6b17c265183692dc8caa6f023043` |
| verified-content.json | 11568 | `b82f375f057362dda3c86824b5c4a0cb4e2902a1699e9fb98a9883780bdcd874` |

Together with the original capture's backup/check/exact-file restore and the
reviewed restore-only checks, these close the bounded backup/restore prerequisite
for the separate authentication gate. They do not prove recovery of excluded
runtime secrets, separate new JAR roots, future data or the entire host. Preserve
the repository, snapshot, escrow and private proof. No automatic follow-on is
authorized; authentication execution and its own acceptance remain separately owned.

### Consumed Restore-Only Allowance

TL;DR: **ALLOW_BOUNDED_NATIVE_RESTORE_ONLY** for the exact frozen candidate below.
F070-10 is closed on these bytes. No open changed-path execution blocker remains.
This permits MA to stage this fresh bundle, obtain matching current Inventory,
then perform one finite Restore against the existing exact repository/snapshot.
It is bounded native verification, not WI completion, production release or a
claim of successful native SQL equivalence. PG performed only local review/tests.

| Execution artifact | SHA-256 |
| --- | --- |
| Bundle manifest | `2ba25b72174b495216a87338142536b69e67162147d112d5d8bc1ec99b0ddb81` |
| Client manifest | `79232dcfa406ee54056e20a0cb1b46751934c0ef06849e44fb5745b0aa4f4ea8` |
| Restore dispatcher | `bf2108e2c5e85177bbe6e694899c616512c1b7429dc0cd8f34783ec3778df665` |
| Equivalence module | `31ff368fc4e822fe992c1c0b7870ebb5c1b82a5ae1c99c350133ebc8f8210033` |
| Root transport | `8c60a66246cbd458857f1c3ee7a769edaf3e0c05cc41e977f7c247c3887afe6b` |
| PC client | `0870dd827835c87927d550666776e1945e4ebb13a358747aeec6f13bde5bdb15` |

Candidate: `build/WI069-backup/restore-20261017-47a250acccbe429293cf3a61006162c1`.
Only its invoke-restore.ps1 and bundle are allowed. Earlier4a71f8be/02d56384 remains
superseded local-only; all earlier first-capture Run allowances remain consumed.

Scope binds original capture `9ef6927ad8674c129729c9720284ff2f`, repository
`36fc435727e7518894734fa2f49eb65481f4b015e91874a4dbfe3f8a2a3172df` and snapshot
`12e37d997ba170762201bab32ea5471f8ad374a71a06e122deb7bed2f0d04e45`.
The original payload manifest/proof hashes remain be54ae5f.../bb178204... exactly
as recorded below and in the immutable source. SHA-256 verification occurs before
parsing their contents. Fresh root-private destination, escrow, target/binary,
cold-state, lock, disk/growth and deadline guards remain enforced. Inventory age
must be at most600 seconds and the new approval window at most1,800 seconds.
Source raw counts/dumps must match before isolated DB initialization; actual
metadata and the exact same209-edit proof must pass before final cleanup/fence.
Original raw hashes, failed workspaces and historical HOLD remain unchanged.

Allowed restic operations are the exact config/snapshot/list/check/restore argument
sets only. No init, backup, repository overwrite/adoption, password rotation,
unlock/prune/delete, source logical mutation, global MySQL/AppArmor change, app,
Gmail or public exposure. Separate staged JAR trees remain outside this member set.
Any HOLD consumes the attempt: preserve evidence, independently recheck cold/cleanup
and diagnose; no automatic retry. Final native observations must be supplied by MA.

### Exact-Byte Verification

- Frozen Python74/74 pass:26 independent PG tests plus48 SE changed-path tests,
  independently executed under the review runner. All nine imported production
  modules resolve inside the new frozen bundle/client; before/after bytes match.
  [Receipt](../../build/WI070-review/restore-delta-ec9d7c7a433a4d16925fc8a405fccee1/receipt.json),
  SHA-256 `1506d60a179ec712740510d9e48da4fce7fe708194a657ef7d3bf6f823f81aae`.
- Frozen PC client45/45 pass, same bytes: exact approval pins, URL-safe escrow,
  strict SSH construction without starting a process, action/status/exit matrix,
  old protocol, truncation/trailing bytes and frame limits.
  [Receipt](../../build/WI070-review/pc-restore-e5a2155fd35d4a39ae4eb2c1ebec8bad/receipt.json).
- Independent identity29/29 pass: both exact manifests, all bundle/client bytes,
  unchanged inherited core/bootstrap, no extra bundle members and only reviewed
  pin plus ./bundle substitutions in transported client templates.
  [Receipt](../../build/WI070-review/restore-freeze-check-df50cbf72fd74be08c0e8612d05ad698.json),
  SHA-256 `7eb4c240c5250bd2a4bf8c9a942d77c23c00a27d825840d4166cfc887c19389c`.
- F070-10 initial red evidence is preserved in the
  [candidate findings](../../build/WI070-review/restore-candidate-findings-20261017.md).
  The corrected decimal-text CAST keeps strict decoding; no fallback is added.
- SE reports whole-candidate coverage82.62% lines/74.10% branches, zero exclusions.
  PG did not remeasure coverage in this narrow delta run;100% critical-path
  coverage remains unestablished. Quality completion and WI closure remain open,
  separately from this bounded test allowance. No synthetic native PASS claim.

Reproduction commands:

```powershell
python -I -S -B build/WI070-review/verify_restore_freeze.py
python -I -S -B build/WI070-review/run_restore_delta_review.py --include-owner-tests --frozen
pwsh -NoProfile -NonInteractive -File build/WI070-review/test_pc_restore.ps1 -ClientDirectory build/WI069-backup/restore-20261017-47a250acccbe429293cf3a61006162c1
```

All fixtures and receipts stay under WI070-review. Network/process creation is
blocked in Python tests; PC tests use extracted functions and synthetic streams,
never invoke SSH or read actual escrow. The printed argparse rejection of old
action `run` is an expected negative test, not a test-suite failure.

### Historical Design And Mutable-Candidate Review

TL;DR: **ALLOW_DESIGN_ONLY; HOLD_NATIVE_RESTORE_ONLY**. The narrow continuation
proposal is accepted for SE069 implementation review under the
[restore-only design contract](../../build/WI070-review/restore-only-design-review-20261017.md).
No new executable candidate or remote Run is approved. MA73dad9/c0d57f/75ec9e
report209 redundant explicit charset insertions before the existing utf8mb4
collation clause, explaining diagnostic schema/full-SQL differences. PG has not
read those private dumps, and naive replacement is not accepted proof.

Current implementation review: **F070-10 OPEN**, numeric ORDINAL_POSITION is
hex-encoded without conversion to decimal text, conflicting with the strict
metadata decoder. Independent mutable-candidate tests21 total:20 pass,1 failure,
0 errors, same bytes. Minimal repair and exact candidate/receipt pins are in
[restore candidate findings](../../build/WI070-review/restore-candidate-findings-20261017.md).
The source design remains accepted; native restore remains HOLD pending repair,
new immutable pins and independent changed-path review. No native SQL was run.

Required binding: existing repository
`36fc435727e7518894734fa2f49eb65481f4b015e91874a4dbfe3f8a2a3172df`, snapshot
`12e37d997ba170762201bab32ea5471f8ad374a71a06e122deb7bed2f0d04e45`, original run
`9ef6927ad8674c129729c9720284ff2f`. Original source counts/raw hashes must match
again without normalization. Actual column metadata must match; a constrained
CREATE TABLE column lexer may admit only the209 witnessed original-omitted to
restored-added exact charset tokens, outside quotes/comments/expressions, with
all other schema and full-SQL bytes exact. Old proof/hashes/HOLD remain unchanged.
No init/backup/repository overwrite, app/global config change or source logical
mutation. Fresh isolated destination, independent cleanup and new immutable
review/test pins remain necessary. MA should relay the design contract to SE.

Additional binding: snapshot payload remains at original capture
`/var/lib/atstudio-backup/wi069-captures/9ef6927ad8674c129729c9720284ff2f/payload`.
Expected listing and restored manifest.json/sql-proof.json hashes must retain
that original identity. New run IDs affect only fresh local workspace/results,
never expected snapshot members or the imported original logical.sql path.
Wrong prefix, altered proof, absent member or unsafe path must HOLD before import.

MA receipt9fca9d supplies original payload manifest.json35345 bytes, SHA-256
`be54ae5fa44cd4aa5a9ddf047749925a12e4b1f4bc434dc5ab92cee7d00a7bd9`, and
sql-proof.json1126 bytes, SHA-256
`bb1782040da237e23d6b30ef3945f6b4cfa48d9293843e72bd994b49a7a47d0f`.
Verify exact sizes and hashes on restored regular-file bytes before using either
document as authority. These are MA-reported bindings, not PG private-file reads.
MA also reports separate new-root public JAR staging without application/DB
execution, original snapshot payload unchanged. That separate work does not
change this restore-only member set or grant any new execution authority.

MA accepts this design and reports SE implementation in progress. The supplied
MySQL8.0.46 sql_show.cc1932-1957 explanation is mechanism context only; metadata
equality and the209-edit byte witness remain required. MA provider UI12:50KST
reports private9 objects/63,547,447 actual bytes/63,569,920 allocated bytes, no new
paid product. MA dff633 reports zero writers and original nginx quarantine active.
PG did not inspect the provider or target. These observations do not prove snapshot
integrity. Direct SE069 messaging is unavailable in the exposed chat inventory;
the shared design note and this short verdict are the MA relay route.

### Previous Native Restore HOLD

Previous **HOLD_RESTORE_CONTINUATION_PENDING_CAUSE**. The latest one-use Run is
consumed. MA reports run `9ef6927ad8674c129729c9720284ff2f`, receipt `d35d04`,
HOLD at ISOLATED_SQL_RESTORE / SQL_SCHEMA_DRIFT, last query rc0, cleanup true,
errors empty and application not run. Backup, check and exact-file restore
passed earlier in that run according to MA; SQL restore equivalence did not.
PG has not accessed native state, private schemas or repository contents.

The existing full snapshot ID is
`12e37d997ba170762201bab32ea5471f8ad374a71a06e122deb7bed2f0d04e45`.
Repository initialization and backup have now occurred. Do not invoke the
first-capture Run again, reinitialize/adopt/overwrite the repository, rotate its
password, create another backup, unlock, prune or delete. Preserve snapshot,
repository identity, original run workspace, private diagnostics and escrow.

MA is investigating the source-versus-roundtrip schema difference using reduced
metadata. Cause is pending; no harmless-drift, normalization or equivalence claim
is made. No comparison relaxation is approved. Once the cause is established,
PG will review a separate exact repository-ID and full-snapshot-ID bound
restore-only continuation, with a fresh disjoint destination and finite window,
integrity/file/SQL comparisons and independent cleanup. The exact repository ID
has not yet been supplied to this review. No continuation execution is authorized
by this status update; WI070 remains active.

### Consumed Client Compatibility Allowance

**ALLOW_BOUNDED_NATIVE_VERIFICATION**, fresh one-use retry for the
immutable MySQL8.0 client compatibility repair below. F070-09 is closed on these
bytes; F070-08 remains closed. This includes new-bundle StageInventory and fresh
Inventory before Run. WI070 remains active; this is not completion or native PASS.

| Current execution artifact | SHA-256 |
| --- | --- |
| Bundle manifest | `9c4dc2ce1a210107d0cbceec380c7ede92a78e64c5309a03f30742370936c9bd` |
| Client manifest | `837d808034d957b5d1063f7e448d7e6fe6f6a4c31b6874f5f8c96d0de9b8e686` |
| Dispatcher | `d577629170099da3345c528e69a5fb60d7b52892dc20bf2de7b52307a86db766` |
| Root transport | `24c1da2f3f2a30985bfc2a3b02add2fac58437da23fb9cebc602f166d220e990` |
| PC client | `e9261d6c1756889d03e7984c73c4b46534156150aa1f952f1353cd76d7c85695` |
| Capture ZIP | `788c92f38f624cc4b3299c636cb45167a2e45b4c4a59ab6adaeee49a11bde621` |

Client: `build/WI069-backup/cold-client-20261017-837d8080/invoke-cold.ps1`.
Only the incompatible flag is removed from query, full/schema dump and import.
--no-defaults remains first. Central command admission validates the exact
client executable/label and forbids caller environment overrides, then injects
MYSQL_TEST_LOGIN_FILE=/dev/null into the clean environment with HOME=/nonexistent.
Every client launch checks null is a nonlinked root-owned0666 character device,
major1/minor3, with root-owned non-writable-by-others directory ancestors.
Wrong device/type/owner/group/mode/link/parent or label/options fail before Popen.
Ambient HOME/MYSQL_* cannot replace the reviewed values. Unsupported-option
diagnosis adds one fixed category without exposing option text. Existing source,
AppArmor, resource/window, restore isolation and cleanup predicates are unchanged.

- [Frozen301/301 PASS](../../build/WI070-review/run-a27a13f37fda4dd0a6cdc6d57941513d/receipt.json),
  zero failures/errors/skips, same bytes; SHA
  `a4ed9b469f83e3ba3613ea69bffec0bd3e74cba6bc5dec01b3f08fda482c0aae`.
  Seven new PG tests cover actual client launch environment, all call paths,
  ambient injection, malformed device/parent and incompatible caller contracts.
- [Frozen PC26/26 PASS](../../build/WI070-review/pc-cold-776acf72dd744437b4b77c28fcee6dc2/receipt.json).
- [All33 frozen identity bindings PASS](../../build/WI070-review/freeze-check-44e46dd7dcb24f6bbb31351db8836cba.json),
  SHA `6ee7b5329e3bf7430de07371e614b09a7b0cf19f910e17092ec28ff0a3f104b5`.
- [Current frozen coverage](../../build/WI070-review/coverage-643490d822a14866aaaf67a1d1e6c04a/summary.json):
  statements1391/1589 =87.54%; branches294/362 =81.22%;0 exclusions;301/301
  measurement rerun PASS. Critical-path100% remains NOT_ESTABLISHED and separate
  from this bounded native-verification decision.

MA must confirm fresh exact target/cold/quarantine state, new destination absence,
unchanged bootstrap/escrow, first-use private repository and Inventory age at
most600 seconds. Use a new finite one-use window; do not reuse old receipts or
rotate the existing password. Preserve both failed captures and old tool bundles.
Any HOLD requires diagnosis, never automatic retry. No AppArmor/global MySQL
change, production overwrite, application start, prune/delete, Gmail or public
release. PG used only local public code and synthetic tests, no native/network/secrets.

### Prior Client Failure And Diagnosis

Previous **HOLD_NEXT_RUN**: F070-09 confirmed client-option incompatibility. MA reports new-bundle StageInventory
`51e6cd` PASS and Run `c88e15b448624dcf8e6bf2f34e48bf27`, receipt `f8ba30`, HOLD
after approximately4 seconds at SOURCE_SQL_WINDOW / COMMAND_EXIT / MYSQL_QUERY
rc2. Socket creation succeeded; diagnostic code/category lists were empty;
cleanup true, errors empty, snapshot null, application not run, no init attempt.
These are MA observations, not PG native access. The one-use allowance below is
consumed and cannot authorize another attempt. Preserve both failed captures.

MA subsequently confirmed cause in receipt `497989`: installed mysql8.0.46
rejects `--no-defaults --no-login-paths --version` and --help with rc2 and an
unknown-option diagnosis. The previous readiness hypothesis is not established.
Query, dump and import all carry the same unsupported option. MA receipt `ad181a`
reports mysql and mysqldump --help with the full applicable flag sets, minus that
option and with MYSQL_TEST_LOGIN_FILE=/dev/null, HOME=/nonexistent, return0 with
no stderr or DB connection. MA supplied the official MySQL8.0 option-file
contract: --no-defaults still permits the login file, whose path can be overridden
with MYSQL_TEST_LOGIN_FILE. Removing the option alone is insufficient.

Independent contract tests reproduce four failures, zero errors, on unchanged
dispatcher34575965...: [red receipt](../../build/WI070-review/client-compat-ca4d1c3ef4a541dcbcae175153bdc66d/receipt.json),
SHA `03c1f5f853c75de514512edbd7bb5df9c2192eb90f539535fbbc074eb256b8bb`.
SE owns a scoped compatible pattern with explicit login-file isolation and clean
HOME; PG is reviewing that delta and will bind a new immutable retry verdict.

### Consumed AppArmor Repair Allowance

**ALLOW_BOUNDED_NATIVE_VERIFICATION**, reviewed2026-10-17 after the
AppArmor repair. F070-08 is closed for the new immutable bytes below. Permit
staging this new bundle, fresh Inventory, and one fresh bounded Run; this is
not reuse of the consumed first-run approval. WI070 remains active.

| Current execution artifact | SHA-256 |
| --- | --- |
| Bundle manifest | `16ec2e473065ee8e5ce2d7c91430b8cad69f94c531a0a16a255b1232fa7235bf` |
| Client manifest | `6bbf257ef0c489d39beb985c6c36d455f9e67c099dd592c1061cc6415ce25c24` |
| `cold_capture.py` | `34575965ed865b99d50c80a8e633df39d801ab04387bfe1fb1a351f84ac20118` |
| `cold_transport.py` | `9a9d3b73842cb0d21c583320e3bffddf33a5256d8b4dfc99bc8be33fa0582671` |
| `invoke-cold.ps1` | `167ef188fae37a41398ae6a80124979ed10dbe671ed62b8af879ee39b88acbc5` |
| Capture ZIP | `87701590b20361066a42c5ef21a8c6f3a19149f3b0bfcb1b7437d50c8eae1513` |

Execution client: `build/WI069-backup/cold-client-20261017-6bbf257e/invoke-cold.ps1`.
The new tool destination is the new manifest directory; preserve the old staged
bundle, failed capture and private diagnostics. Do not reinstall bootstrap or
regenerate its password. MA confirms first-run cleanup with receipt `53d993`
and reports no repository initialization, upload or snapshot.

New delta is limited to standard MySQL socket/PID paths, private log lifecycle,
and client/transport binding to the repaired bundle. Source, initialize and
restore use standard `mysqld.sock`/`mysqld.pid`; the implicit socket lock stays
beside the standard socket. Masked original mysql.service and absent runtime
directory are required. A unit-specific log is precreated exclusively with
no-follow, mode0600, uid110/gid112 beneath the observed uid110/gid4/mode0750
directory. Existing files, including error.log, are neither truncated nor
repermissioned. ProtectSystem=strict permits only the exact fresh log file,
besides the previously reviewed data/runtime paths. No AppArmor or global
MySQL configuration changes are introduced.

Log growth has a sampled1MiB guard, not a kernel-enforced hard byte cap.
The finite300-second owned unit and disk reserve remain. Failure diagnosis
copies at most65536 bytes to root-private capture storage and exports only
allowlisted codes/counts, not raw text. Log collision, changed parent inode,
ownership/mode/link drift and size excess fail closed. Log handling does not
bypass independent owned-unit cleanup or final source fencing.

- [Frozen Python285/285 PASS](../../build/WI070-review/run-a65c4d4170d241589eec7c561eb444b3/receipt.json),
  zero failures/errors/skips, before/after bytes equal; SHA
  `d8d767af4e5939507e2abb089f0b2a83049d1218c94b0d01dd04daf31015a4e1`.
  Includes15 independent AppArmor/log tests and9 SE log-boundary tests.
- [Frozen PC26/26 PASS](../../build/WI070-review/pc-cold-06d33596ca024e0cae0420c4afadfbaa/receipt.json),
  no SSH invocation or private-file access.
- [All33 exact identity bindings PASS](../../build/WI070-review/freeze-check-351ea952a81d4aa8bc02f1601d4e8e22.json),
  SHA `3db5c1c522438148104cd16f66f054df316ee4037a8046f63f0c91bdc4d5eb62`.
- [New frozen coverage](../../build/WI070-review/coverage-d0e8de565ee641b6a040d9d0c8a9f062/summary.json):
  statements1363/1573 =86.65%; branches287/356 =80.62%;0 exclusions;
  the measurement rerun also passes285/285. Security-critical100% remains
  NOT_ESTABLISHED, separately from this bounded execution decision.

MA must retain exact target/SSH/bootstrap/escrow/repository checks, cold source
and quarantine, absent new destinations, Inventory age at most600 seconds and
a new finite approval window. This permits the already reviewed first backup
and isolated restore test only. Any HOLD requires review, never automatic retry.
No production restore/overwrite, app start, pruning/deletion, Gmail, public
release or WI completion is authorized. PG made no native or secret accesses.

### Prior Failed Run And Consumed Pins

MA native evidence: first Run `9d020f`, run ID `6c92c40bb05744bcbaa8c52c41b9faa6`,
failed at SOURCE_SQL_WINDOW with MYSQL_READY_TIMEOUT, MYSQL_SOURCE_START rc0,
cleanup true, no cleanup errors and no snapshot. MA reports no SQL dump or
repository initialization. Kernel evidence `562b37` records six AppArmor mknod
denials for the custom error-log pathname at03:24:25Z; source journal exit1.
PG did not access the server, journal or private files.

Policy observation `6911c5` additionally identifies the exact allowed standard
socket/PID names. The log denial is native-observed; socket/PID conflicts are
source-versus-observed-policy findings, not claims of additional native events.

The superseded candidate `e022af011226aa4e01be3756cea79844b817509c57f355672e7152420f76d4b1`
fails the independent pathname contract in source, initialize and restore modes:
[receipt](../../build/WI070-review/apparmor-d9e108224a354711b5acb20a84c1043c/receipt.json),
SHA `0223cc7e9f0469d25f41dec9bf4607ade6c14e6d3cb63a58d8a57daf2a932c14`.
Four test methods: three pathname methods fail, ten failing assertions including
subtests, zero errors; the unchanged isolation check passes. An earlier PG fixture
used Windows path semantics and produced eight fixture errors; preserved at
`build/WI070-review/apparmor-9726d6638ad143d2966da78892c4ba0c` and corrected only
inside the PG test to model POSIX absolute paths. No candidate code was changed.

Earlier consumed verdict: **ALLOW_BOUNDED_NATIVE_VERIFICATION**, exact pins below.
F070-01 through F070-07 were closed by repairs and independent regressions;
F070-08 was subsequently found in the first native test. This was
not WI completion, production acceptance, public release or Gmail authority.

| Reviewed execution artifact | SHA-256 |
| --- | --- |
| Bundle manifest | `6991392ccb8f7e5d1b4c9d68aba86b17376881a83802b3068387bd518137ed79` |
| Client manifest | `dee98efc1c4e13c0e72d8c39aef4ac78a87e737f3772f8aca3a0cfc61c3b1882` |
| `cold_capture.py` | `e022af011226aa4e01be3756cea79844b817509c57f355672e7152420f76d4b1` |
| `cold_transport.py` | `2abd30bf85dac3bd6402d6e4bcef23afb2d5c9eedf3bc6f615a2aa98a31a1c1e` |
| `invoke-cold.ps1` | `9b3b1d0a1c9e8ea350a3a07ce24ef972168d4ce66bed8c30a8b4be48674919f7` |

Execution client: `build/WI069-backup/cold-client-20261017-dee98efc/invoke-cold.ps1`.
Rechecked all33 identity bindings after the execution-scope clarification:
[receipt](../../build/WI070-review/freeze-check-715d30872cb345949951c67f37838337.json),
SHA `326ec26bd7888d3f521ea103a9f688b6e06d836aef4c54ca3ef8a780dd8d66d7`.

Allowed: one freshly approved Run of the exact adapter, including finite source
MySQL read-only/socket window, complete logical dump, first private encrypted
repository initialization, bounded backup, full-data check, fresh isolated file
and SQL restore comparison, and independent owned-process cleanup/final fencing.
Production database restore/overwrite, application start, pruning/deletion,
runtime-key backup, SMTP/Gmail and public exposure remain excluded.

MA execution prerequisites remain enforced, not waived: pinned SSH/target,
unchanged cold units and quarantine, matching installed artifacts and escrow,
private first-use repository, absent fresh run destinations and Inventory age
at most600 seconds. Refresh Inventory when older; use one new finite approval.
Capacity guard remains `max(20% filesystem,10GiB)+4GiB`, with bounded work and
no process admission after expiry. Any HOLD/partial result requires recovery
review; never automatically retry or reinterpret it as PASS.

**Correction of prior quality HOLD:** PG incorrectly extended the general
completion gate to the bounded native verification itself. STD-002 sections
6.3.3 and6.6.2 govern WI completion/quality and commit readiness; they do not
make this approved, still-active-WI native test a public release. The user
explicitly clarified this distinction. That execution HOLD is withdrawn;
coverage gaps remain honestly open for WI completion, not silently waived.

**WI quality completion remains open.** No100% security-critical coverage claim
is made. The scoped execution ALLOW does not convert synthetic results into
native PASS. Source ownership remains SE069; execution ownership remains MA.

MA reports native StageInventory
`ebe3c8` OBSERVED_NOT_CAPTURED, run `7c31762de56c4a2788f6b7824bacf043`, with
the exact bundle staged; no DB or restic execution. This is MA evidence, not PG access.

The preceding independent measurement used the MA-provided coverage7.16.2 wheel after
verifying SHA `11d28e9123a9156cb405d8d27b44256c9a58fb5decc2073a8f17862057e3aa0f`,
without installation or network. Frozen Python bundle and transport only:

- **Prior261/261 PASS**,0 failures/errors/skips, frozen modules imported directly,
  same bytes: [receipt](../../build/WI070-review/run-44c8745b8a2e40c09f542bee3eed5954/receipt.json),
  SHA `339aefb85e7c2270b56eb290e962d9cc54c9375257d119751cb73c048b0133f3`.
  [Coverage](../../build/WI070-review/coverage-dce822ca7b4e407a974109a5c3e4964f/summary.json):
  statements1302/1533 =84.93%; branches274/352 =77.84%;0 exclusions.
  No100% critical-path or function-coverage claim. These scoped metrics exclude
  unrelated source and the separately tested PowerShell client.
  SE's two Windows fixtures are corrected; the native guards were not weakened.
  Added owned-work byte/entry/link bounds and sanitized result-frame tests pass.
- Reproduce: `python -I -S -B build/WI070-review/measure_frozen_coverage.py`;
  `pwsh -NoProfile -NonInteractive -File build/WI070-review/test_pc_cold.ps1 -Frozen`;
  `python -I -S -B build/WI070-review/verify_cold_freeze.py`.
- [Prior243/243 PASS](../../build/WI070-review/run-85437c7434e4459db50d8e62c5f242d8/receipt.json),
  [coverage](../../build/WI070-review/coverage-a05130b3b47b4434ba4ea28f64727361/summary.json):
  statements80.17%, branches73.01%,0 exclusions. General thresholds met;
  security-critical100% is NOT established.
- Prior increment including SE's new admission suite:254 tests,252 PASS,2 fixture errors,0 failures,
  same bytes. [Receipt](../../build/WI070-review/run-7a22e0c0b4854e908bcd0e61c8533654/receipt.json),
  SHA `4c3b4f3d828f6b1360705170344ef1329fa88ce0281117309f24ac6b7a1be651`.
  [Coverage](../../build/WI070-review/coverage-aeefc0b798974c65abca31fb1755d6ed/summary.json):
  statements82.39%, branches75.57%,0 exclusions; not a passing test gate.
- Then-required SE fixture repairs, not native guard changes: `test_cold_admission.py:101`
  uses Windows `str(path)` against POSIX pin keys; use the modeled POSIX path.
  The simulated-root metadata at lines143-145 retains Windows writable mode
  bits, so `cold_entry.read` correctly rejects it. Model private POSIX modes.
  [Exact error log](../../build/WI070-review/run-7a22e0c0b4854e908bcd0e61c8533654/test_cold_admission.log).
- Independent PG admission tests now cover exact escrow and credential binding,
  prior bootstrap failure, wrong binary/run, expiry, lock contention and final
  inventory drift. All nine pass; failure cases never enter Capture.
- Frozen PC client26/26 PASS, including strict SSH options without starting a
  process: [receipt](../../build/WI070-review/pc-cold-20a04ea8e93747d4858ad6b1573f1d9f/receipt.json).

Earlier step-specific verdict: **ALLOW_STAGE_INVENTORY**, reviewed on2026-10-17.
Only fresh root-private tool staging and read-only inventory are allowed under
MA's fresh target and absent-destination checks. **Run, DB start and restic
execution are NOT yet approved.** Full capture review continues.

- Bundle manifest: `6991392ccb8f7e5d1b4c9d68aba86b17376881a83802b3068387bd518137ed79`.
- Client manifest: `dee98efc1c4e13c0e72d8c39aef4ac78a87e737f3772f8aca3a0cfc61c3b1882`.
- Frozen client directory: `build/WI069-backup/cold-client-20261017-dee98efc`.
- Transport: `2abd30bf85dac3bd6402d6e4bcef23afb2d5c9eedf3bc6f615a2aa98a31a1c1e`.
- PC client: `9b3b1d0a1c9e8ea350a3a07ce24ef972168d4ce66bed8c30a8b4be48674919f7`.
- [Python221/221](../../build/WI070-review/run-9322f207738449e1a3476523658a9f2b/receipt.json),
  SHA `731779c9516b13ef39f0529040edfb37a453343db2b51909d1e3e9ad4639bd81`.
- [PC25/25](../../build/WI070-review/pc-cold-7d3f65316e0a400291ec4132cd36adba/receipt.json), same bytes.
- [Exact frozen identity](../../build/WI070-review/freeze-check-2c636a8ad87149cb9a3e6bf9e45261d6.json),
  SHA `efa0cb863967f99edb2f4b86cd82005c1d0c2814aaa34a545bf70e9cd92d4928`.
  All33 checked inputs match: tested working files, both frozen bundle copies,
  ZIP members/archive, client files and manifests. Stage request fits its bound.
- F070-06 and F070-07 are repaired and independently closed on these client bytes.
  Any HOLD, partial run, existing staging destination or identity drift requires
  MA recovery review, not an automatic retry. No findings are waived.

Earlier bootstrap-only verdict, renewed local review on 2026-10-17:
F070-02 is closed:35/35 PC checks and147/147 Python checks pass on unchanged
bytes. This approves only the exact Stage/StageAndInstall transport and private
PC escrow below, subject to MA's fresh execution prerequisites. Expired target
observations and execution gates are not renewed by these tests. Capture/restore
review remains active and unapproved. No native, cloud, private-file, credential
or source operation was performed by PG.

**Prior capture HOLD: F070-06 PC escrow repair awaiting retest and F070-07 transport
action/status/exit consistency. F070-03, F070-04 and F070-05 closed.**
Prior independent run:221 tests,217 PASS,4 FAIL,0 errors, same bytes.
[Latest receipt](../../build/WI070-review/run-072285a3c1584a808c2c4b1e40318b87/receipt.json),
SHA-256 `9ff3337e9e0cde472a2361de901ffb9744ae33094231b7d6f4b1a990e0d47300`.
Cold candidate pin `e022af011226aa4e01be3756cea79844b817509c57f355672e7152420f76d4b1`.
The four failures reproduce false success acceptance in the new transport.
MA can relay these concrete fixes while PG continues same-byte client/bundle review.
No additional full-system audit is requested.

Prior increment:204 tests,202 PASS,2 FAIL,0 errors, same bytes. Base
disk floor, SE test fixture portability and bundle import precedence were repaired.
[Latest receipt](../../build/WI070-review/run-f778c347919d46de8846d7256a451c3e/receipt.json),
SHA-256 `75a23cf8f7aecbaf1f8e3c691f97acd8416db9fda1adae720155d16d907b4c9b`.
Prior cold candidate pin
`ab913a4c3d7971d9840a0811e253b3679ddd138a680d7729bb439e84650f9fe3`.
This prior receipt remains historical evidence; the newer findings supersede its HOLD reasons.

## Current Bootstrap Verdict

| Reviewed artifact | SHA-256 |
| --- | --- |
| `bootstrap-release.json` | `840ebbb7aeec7537d8a53205791fd28d2c5190905ea48edbe2b131e32ec48558` |
| `bootstrap.py` | `f2f7c90c609a3a0689153c3bb40201cec684974e53ae6d6990d47eec86f98fe8` |
| `bootstrap_transport.py` | `2a6e9eff6ff5a6f542454fdebc834b13a5c8f116042e649ef14bed5d3a434849` |
| `invoke-bootstrap.ps1` | `03681b54d1ba27abc96e2dabff613298354f3854ab96f303818ccabcc3c175f5` |

Independent commands, rerun locally on 2026-10-17 around11:47 KST:

- `pwsh -NoProfile -NonInteractive -File build/WI070-review/test_pc_helpers.ps1`:
  [35/35 PASS, same bytes](../../build/WI070-review/pc-helpers-627e547f85c2407aab0435380156c71e/receipt.json).
- `python -I -S -B build/WI070-review/run_review.py`:
  [147/147 PASS, same bytes](../../build/WI070-review/run-77c4e0d1604d4a43ad571fa355334778/receipt.json).
  Receipt SHA-256 `f6aae64cf1c93b0b4a5e204ac4964511bc2c8acf0c09900d74b0cd1aa64c9656`.
  Release manifest matches every listed file; transport-to-bootstrap binding matches.

The F070-02 repair rejects missing ObjectInherit before creating the child and
checks its ACL before the first write. The valid inheriting synthetic folder,
CreateNew collision, framed response faults, strict JSON conversion and private
transport-option checks all pass. These are local synthetic/helper tests, not
actual credential upload or native escrow evidence.

Allowed scope: the fixed transport paths, Stage or StageAndInstall, pinned
binary installation without execution, and exact private PC escrow. MA must
refresh target/SSH identity, active-key reference, ACL and absent-destination
prerequisites and use fresh approvals. Do not bypass a changed identity or
HOLD. Partial runs require MA recovery review before retry; no deletion is
authorized. Repository init, restic execution, capture, restore, services,
Gmail, WI067 and public exposure are excluded. Earlier sections below preserve
dated intermediate findings and do not override this pin-specific verdict.

## Findings

### F070-09: Preserve Login-File Isolation With Compatible Client Options

- Severity: P2; blocks another Run of bundle16ec2e..., whose one-use approval
  was consumed. F070-08 remains fixed; no AppArmor regression is established.
- Resolution: CLOSED for bundle9c4dc2... and client837d8080..., exact pins and
  independent frozen evidence above. Native retry outcome remains pending.
- Frozen cold_capture.py:355,550,730 uses --no-login-paths in dump, query and
  import. Installed mysql8.0.46 rejects it per MA497989; no repository init occurred.
- Retain --no-defaults as the first option, fixed root Unix socket and all
  existing dump/import protections. Use an explicitly reviewed login-file
  override plus clean HOME, independent of ambient HOME/MYSQL_* values.
- Review fixed null-device identity or protected absent-path predicates as
  implemented; never inspect or adopt the operator's actual login/config files.
  Query, full/schema dumps and restored SQL import must share this protection.
- [Independent tests](../../build/WI070-review/test_pg_client_compat.py), command
  `python -I -S -B build/WI070-review/run_client_compat_review.py`.
- [Official option-file reference](https://dev.mysql.com/doc/refman/8.0/en/option-files.html):
  contract excerpt supplied by MA; PG performed no external request. Native
  no-connection option compatibility is MA evidence, not synthetic proof of DB access.
- No automatic startup retry, mask/security relaxation, new app or global
  configuration change is authorized. Fresh candidate/pins and one-use window required.

### F070-08: Repair All AppArmor-Constrained MySQL Paths Together

- Severity: P2; blocks another native Run of the old bundle.
- Resolution: CLOSED for bundle `16ec2e...` and client `6bbf257e...`, exact pins
  and same-byte evidence in the current Summary. No policy weakening or waived
  predicate. Native retry outcome remains unobserved by PG.
- `cold_capture.py:37,291-292,476` uses custom socket, PID and error-log names.
  The log pathname caused the observed native failure; the other two names also
  fall outside the exact names supplied by MA from the current profile.
- All source/initialize/restore modes must use `/run/mysqld/mysqld.sock` and
  `/run/mysqld/mysqld.pid`; retain masked original mysql.service, absent runtime
  directory, unique transient units, operation lock, finite window and cleanup.
- Error log must resolve inside the existing `/var/log/mysql/**` allowance,
  using a fresh per-run/per-mode private file or a reviewed equivalent private
  bind. Existing `/var/log/mysql/error.log` must not be overwritten or deleted.
- Review the existing log-directory metadata (MA:uid110/gid4/mode0750), exclusive
  no-link creation, private file ownership/mode, bounded size/lifetime, collision
  rejection and restrictive `ReadWritePaths` for only the needed new log file.
  Keep source/restore data writes isolated as previously reviewed.
- Failure diagnostics must follow the new per-mode log reference and retain only
  bounded fixed codes in reports. No raw log body, credentials or SQL is exported.
- No AppArmor disable/reload/profile edit, global permission change or broad new
  writable log directory is authorized. Do not weaken a guard to obtain readiness.
- [Independent regression](../../build/WI070-review/test_pg_apparmor_paths.py),
  command `python -I -S -B build/WI070-review/run_apparmor_review.py`.
- SE069 owns implementation; PG owns only review/tests/reports. Retest the full
  three-mode repair and error lifecycle against a new frozen manifest/client
  before another one-use native window. Source-cold confirmation remains MA-owned.

### F070-07: Bind Result Status To Action And Exit Code

- Severity: P2; capture HOLD, bootstrap verdict unaffected.
- `cold_transport.py`, end of `dispatch`, independently accepts exit0/2 and
  any of OBSERVED/HOLD/PASS without checking their relationship to the action.
- Four independent tests reproduce: Run PASS with exit2; Inventory OBSERVED
  with exit2; Inventory accepting backup PASS; Run accepting only OBSERVED.
- `invoke-cold.ps1` repeats this permissive predicate and can print PASS despite
  process exit2. This is response-contract acceptance, not evidence of a native failure.
- Minimal fix: Inventory/StageInventory success means OBSERVED plus exit0;
  Run success means backup PASS plus exit0. HOLD must remain failure. Apply
  the same action/status/exit check at both transport and PC receipt boundaries.
- Reproduction: [independent transport tests](../../build/WI070-review/test_pg_cold_transport.py),
  four failing assertions in the latest receipt. All subprocesses were mocked.
- Tested transport pin: `6610942f2a030f9c667c4755c5bc2c9bcbc2a496cdd78221d8141b063495bfda`.
- Tested client pin: `876a4a48753f3f30b451012f2e9a5782c689921366d21e10d711d834e9a70463`.

Closure2026-10-17: transport `2abd30bf85dac3bd6402d6e4bcef23afb2d5c9eedf3bc6f615a2aa98a31a1c1e`
and client `9b3b1d0a1c9e8ea350a3a07ce24ef972168d4ce66bed8c30a8b4be48674919f7`
bind success to action and exit0, and HOLD to exit2. All four transport rejection
cases and corresponding in-memory PC frame cases pass. No actual subprocess or SSH.

### F070-06: Accept The Existing Bootstrap Password Contract

- Severity: P2; capture HOLD, bootstrap verdict unaffected; MA also relayed it.
- `invoke-cold.ps1`, Run escrow predicate, accepts only64 lowercase hex characters.
  The reviewed `bootstrap.py:285` generates `secrets.token_urlsafe(48)` followed
  by a newline:64 base64url characters, then one LF,65 bytes total.
- Preserve the already generated password. Accept the exact64-character
  `[A-Za-z0-9_-]` alphabet and final LF, and retain SHA-256 binding to the native
  private copy. Do not regenerate or expose an existing password to work around it.
- Source-contract comparison only at this increment; no private escrow read.
- The earlier hex-only source was observed before the following pin was taken.
  SE's new `Validate-Escrow` implements the base64url repair at client pin
  `876a4a48753f3f30b451012f2e9a5782c689921366d21e10d711d834e9a70463`;
  independent PC retest is in progress. Do not attribute the old regex to this new pin.

Closure2026-10-17: the25-test PC run accepts all64 base64url alphabet characters
and rejects missing/extra LF, CRLF, short input, padding, slash, plus, embedded LF
and non-ASCII bytes. The SHA-256 binding and one-use approval window remain intact.
Final tested client pin: `9b3b1d0a1c9e8ea350a3a07ce24ef972168d4ce66bed8c30a8b4be48674919f7`.

### F070-05: Recheck Expiry After Preflight Before Launch

- Severity: P2; capture HOLD, bootstrap verdict unaffected.
- `cold_capture.py`, `Capture.command`: `guard()` checks time before potentially
  expensive unit/process/disk inspection. On return, the command calculates an
  already-expired deadline but opens output/input and calls `Popen` anyway.
- Independent test advances both deadlines during the mocked preflight. The
  process-launch mock is reached after expiry. No subprocess was actually run.
- The later loop check and owned-unit cleanup do not prevent admission of a
  new transient unit after the approval has expired.
- Minimal repair: recheck monotonic and wall-clock expiry after preflight and
  before file/process creation. Keep cleanup independent of the expired guard.
- Reproduction: `test_command_cannot_start_if_preflight_consumes_remaining_window`
  in [independent tests](../../build/WI070-review/test_pg_capture.py).
- Bound to current cold pin `ab913a4c3d7971d9840a0811e253b3679ddd138a680d7729bb439e84650f9fe3`.

Closure2026-10-17: cold pin `e022af011226aa4e01be3756cea79844b817509c57f355672e7152420f76d4b1`
rechecks both clocks after preflight and immediately before Popen. The independent
slow-preflight regression passes; cleanup remains independent.

### F070-03: Preserve The Approved Disk Reserve And Growth Budget

- Severity: P2; capture HOLD, bootstrap verdict unaffected.
- `cold_capture.py`, `Capture.guard`, pin
  `de448903fdeb577a34c007236b7f519a0bf00efa6d4c38447d5817926858b144`,
  admits5GiB free while the approved restic plan requires
  `max(20% filesystem, 10GiB) + additional dump/metadata/cache/temp/other growth`.
- Three independent regressions reproduce acceptance at9GiB on20GiB,19GiB
  on100GiB, and20GiB on100GiB without any additional growth reserve.
- Restore the agreed base floor and reserve remaining admitted growth without
  double-counting already occupied work bytes. Historical high free space is
  not permission to weaken the generic guard.

Update2026-10-17: base floor fixed; both below10GiB and below20% regressions now
pass. The exact-floor-without-growth-reserve regression still fails. Restoring
the base floor alone does not close this finding.

Closure2026-10-17: cold pin `e022af011226aa4e01be3756cea79844b817509c57f355672e7152420f76d4b1`
adds a conservative4GiB growth reserve, matching the bounded work budget, above
`max(20% filesystem,10GiB)`. All three rejection cases and high-headroom acceptance
pass. Reserving the complete budget is conservative; a tighter remaining-growth
calculation is not required to approve this fixed operation.

### F070-04: Keep Runtime Imports Inside The Verified Bundle

- Severity: P2; closed by the203-test same-byte independent retest.
- `backup_checks.py:14-16` prepends an external core directory ahead of the
  flat bundle verified by `cold_entry.py`. In native layout it is under
  `/var/lib/atstudio-backup/scripts/deployment/backup`, outside the manifest.
- A synthetic same-layout lookup selects an unreviewed `backup_protocol.py`
  there instead of the verified adjacent copy. No unreviewed code was executed.
- Minimal repair: prefer and constrain the verified adjacent protocol in the
  native flat bundle; keep local repository test reuse explicit. Do not claim
  all imported executable bytes are pinned while an outside path has precedence.
- Reviewed `backup_checks.py` pin:
  `025154c8a7912a8c12e0f0e25874cd9e543171c564657413ca94535ceb9e564e`.
- Reviewed `cold_entry.py` pin:
  `01eadc16dba2eb37ff0b954e7558771c915fa98ed238da7582d4ca7e6f7c91a2`.

Closure2026-10-17: the native flat bundle now selects its verified adjacent
protocol before any external repository path. The independent shadow-path
regression passes. Corrected `backup_checks.py` pin:
`1b2881a8fb583af76f706d2b60ac6ab54546b807600ebbf8f4ff337f99dae5d2`.
New diagnostic filtering, wrong MySQL mount rejection before query, and source
read-only/network containment checks also pass independent tests.

Interim independent evidence:
[receipt](../../build/WI070-review/run-37384d9b14f944adbdfa0402499fc7ab/receipt.json),
SHA-256 `630aa98ba64ed1fa2d0079470536edf9425d9ecbc8c99a8aedcc45c813da8efe`.
195 tests:189 pass,4 failures above,2 SE test errors. The test errors are a
13-versus14-byte synthetic fixture and missing Windows `O_NONBLOCK` mock,
not native candidate findings. Earlier PG adaptation errors (Windows path
semantics and SE's renamed intermediate receipt) are preserved in the first
capture receipt and corrected in subsequent tests. Candidate bytes did not
change during each cited test window; this is an interim pin, not final freeze.

Source socket arguments use fixed AF_UNIX/no-network/read-only settings;
restore uses a separate data-directory bind and ignores source config. Tests
exercise actual import argv selection, complete SQL comparison calls, partial
failure cleanup and rejection after failed final fences. They simulate external
commands and do not establish Linux bind/socket/UID behavior. The newly added
source ancestor-link check passes the independent same-device alias regression.

## MA Native Bootstrap Observation

MA reports2026-10-17 around11:56 KST, sanitized receipt `32ab08`:
`INSTALLED_NOT_EXECUTED`, pinned executable `20d4142678d0d95ec11a4759def1b73fd9190abc9ca19e4b62d067c0b387e639`,
`PC_COPY_HASH_MATCHED`, repository `NOT_INITIALIZED`. MA reports all three
destinations absent at `8f9351`, and cold inventory `270804` with masked source,
zero writer processes and96.6GB free. These are attributed MA observations,
not PG native inspections. Capture remains unapproved pending exact final review.

## Historical Findings

### F070-01: Reject Ambiguous Or Mistyped Snapshot Evidence

- Severity: P2; reproduced in the first candidate, now closed at the pin below.
- Entry point: [backup_checks.py](../../build/WI069-backup/backup_checks.py),
  lines139-145, `verify_snapshot`.
- Inherited cause: [backup_protocol.py](../../scripts/deployment/backup/backup_protocol.py),
  lines55-57 parse duplicate JSON keys with last-value wins; lines123-126 use
  numeric equality that accepts boolean owners as integers.
- Reproductions: a summary containing `error_count:1` then `error_count:0`, a
  node containing `uid:9` then `uid:0`, and a node with `uid:false` all return
  the exact synthetic snapshot ID instead of rejecting the malformed evidence.
- Impact: conflicting read-error/owner evidence can be labeled verified by the
  new wrapper. This is a parser-contract defect, not evidence that the pinned
  restic binary emits these responses or that a native snapshot is corrupt.
- Minimal repair: WI-local strict JSON-lines validation on backup output and
  listing, with duplicate-key rejection and exact types for consumed fields,
  before delegating to the unchanged core. Keep documented omitted-zero
  semantics; do not require unrelated fields or edit shared core speculatively.
- Scope: blocks snapshot acceptance using this wrapper, not a separate
  metadata-only observation or bootstrap that does not consume it.
- Independent regressions: [test_pg_review.py](../../build/WI070-review/test_pg_review.py),
  `test_duplicate_error_count_must_not_erase_a_failure`,
  `test_duplicate_owner_must_not_erase_drift`,
  `test_wire_boolean_owner_is_not_integer_zero`.

Closure22:24 KST: `backup_checks.py`
`6d822adbb3c2618a116725be5eb346bc45c9b2192b6413245a9747dc00d83c13`
uses WI-local strict JSON-lines/type validation before the unchanged core.
All three independent regressions now pass;147 Python tests pass in
[receipt](../../build/WI070-review/run-7c24c1b270c749a4a12d7c95d5dd1134/receipt.json),
SHA-256 `0aef812dab3bfdd4427b9058eb14f85e90594e7a4e18712af744346a2de267d1`.
The release manifest and transport-to-bootstrap hash binding also match. This
closes the parser finding, not the unfinished native capture/restore review.

### F070-02: Enforce Private File ACL Before Writing Secret Bytes

- Severity: P1 for the generic escrow helper; closed by the 2026-10-17 retest above.
- Entry: `invoke-bootstrap.ps1`, `Private-New`, lines71-79 in client pin
  `289177dccf5f1b7d5c4e25d424856c0791c71b49bfb13feff9d7ed127219d3a5`.
- The parent is checked as user-only/protected, but inheritable file permissions
  are not required. `Private-New` writes/flushes the password before checking
  the newly created file's ACL.
- Independent Windows reproduction: a fresh synthetic parent, current-user
  owner, protected ACL, one user-only FullControl ACE with no inheritance, passes
  the parent guard. The child contains14 synthetic bytes and has2 non-user Allow
  rules when the later file check raises HOLD. No actual password was used.
- Minimal repair: reject a parent that cannot produce user-only file inheritance
  before creation, or atomically create the file with an explicit private DACL.
  A post-write permission check is insufficient. Retain CreateNew/no-overwrite.
- [First reproduction](../../build/WI070-review/pc-helpers-45dfe2091bcc47aea096a9483184c351/receipt.json).
- [Expanded PC review](../../build/WI070-review/pc-helpers-38878d08355c45b18765db61f760afa7/receipt.json):
  35 tests,34 pass, only this regression fails. Original functions and response
  block extracted by PowerShell AST; synthetic owned files only; no SSH process.
- MA clarification `032b4f`/`d41c45`: actual recovery parent has sole current-user
  FullControl with ContainerInherit,ObjectInherit, PropagationNone, explicit ACE,
  correct owner and disabled inheritance. This makes that observed folder
  compatible; it is MA evidence, not a PG private-folder inspection. MA asks SE
  to fix the generic pre-write guard and PG to retest exact corrected bytes.

The same PC review also tests strict in-memory JSON conversion, duplicate/extra/
mistyped/malformed/oversize inputs, exact response identity/hash, truncation,
trailing bytes, remote failure, escrow collision and successful Stage and
StageAndInstall responses. Successful synthetic files remain user-only; no
password or password hash enters their public receipt. Strict SSH pipe/agent/
identity options are checked without launching SSH.

### N070-01: Exact Target Correction Already Relayed By MA

The first reviewed inventory compared the console label at
`native_inventory.py:133`. MA subsequently supplied the observed OS hostname
and SHA-256 of stripped machine-id, plus the existing strict SSH pin/provider
identity. SE is already correcting the mismatch. Review the replacement exact
identity predicate, including rejection tests, before releasing that probe.
Removing the hostname gate without an exact replacement is not accepted.
PG has not inspected the server or `/etc/machine-id`.

Update22:07 KST: inventory now uses the exact OS hostname plus stripped-byte
machine-id hash. Bootstrap deliberately uses the separately supplied raw-byte
hash plus the fixed SSH host-key fingerprint. The conventions are distinct,
not interchangeable; PG verifies that the stripped hash cannot satisfy the
bootstrap config. The known label mismatch is corrected in the reviewed drafts.

## Bootstrap Increment

At22:07:34-22:07:35 KST, same-byte local review passed all18 SE bootstrap tests
and14 independent PG bootstrap tests. Tested archive/credential rejection,
partial-stage failure markers, invalid install pins, failed-stage refusal,
pairwise path overlap, synthetic symlink/hardlink/permission rejection,
bounded FIFO-only stdin/timeout, private-error suppression and failed install
readback before execute mode. Native ownership and filesystem primitives are
mocked; these results do not prove target ACLs, transfer or persistence.

| Input / receipt | SHA-256 |
| --- | --- |
| `bootstrap.py` | `f2f7c90c609a3a0689153c3bb40201cec684974e53ae6d6990d47eec86f98fe8` |
| updated `native_inventory.py` | `2e277895c8fd844c12763f39bd79db7b4d92d04fcb51dc5a6752f7f71eee06de` |
| PG receipt | `b2e6cd9a1dfd5a90808d390d2ea0938cad9d005d5d7883651426bb4fba550efa` |

[Bootstrap increment receipt](../../build/WI070-review/run-d0bf2a4915e84fbc82a5e42e7e8e235b/receipt.json)
records121 total tests:118 passed and the same3 F070-01 failures;0 errors/skips.
[PG bootstrap log](../../build/WI070-review/run-d0bf2a4915e84fbc82a5e42e7e8e235b/test_pg_bootstrap.log).
An earlier preserved bootstrap run had6 PG harness errors after SE changed the
stage signature to stdin; these were test adaptation errors, not findings:
[earlier receipt](../../build/WI070-review/run-bafd8ffc50b24856a6f297aab63cd864/receipt.json).

The draft accepts configurable secure parents rather than one fixed install
parent. Final ALLOW must bind reviewed exact config bytes/paths; it cannot grant
arbitrary-directory installation. Config/approval references and the final
transport/PC escrow tool must also be reviewed before release. No evidence yet
shows successful independently recoverable password escrow. The bootstrap keeps
`capture_ready=false`, repository not initialized and restic not executed.

## Server Transport Increment

At22:11:09-22:11:10 KST, the server transport draft passed10 independent PG
tests: default inertness, TTY refusal, strict bounded headers, bad script/archive
before writes, credentials only through child stdin, fixed clean environment,
nonzero/unsafe child status, input EOF/deadline, sanitized failure frame and exact
private binary response framing. All inputs and password bytes were synthetic.

- Transport SHA-256: `a78db928da0fbe10dd98536dc89f986121e2e72bb3651132ac57823625189484`.
- Same-byte receipt SHA-256: `60f63fa6dcb712d1fdb3deedab90afb1265be2528b44197273005518c119f3d5`.
- [Receipt](../../build/WI070-review/run-cb22d5ea1d674db4ab8eda3d21ecb9a6/receipt.json):
  131 tests,128 passed,3 F070-01 failures,0 errors/skips; same bytes true.
- [Transport test log](../../build/WI070-review/run-cb22d5ea1d674db4ab8eda3d21ecb9a6/test_pg_transport.log).

**Bootstrap execution HOLD, not final candidate rejection:** the transport still
contains `BOOTSTRAP_SHA256 = "PENDING_FINAL_PIN"`, and the PC-side credential
upload/password escrow receiver is not supplied. The server-side `isatty`
guard alone does not establish that a receiving pipe is private. The final
client must keep response/password bytes out of tool logs and argv, verify the
exact host and frame, create a fresh user-only recovery file, and read it back
before claiming escrow. No raw password/hash is exported by PG evidence.

Transport-generated paths are fixed to `/var/lib/atstudio-backup/wi069-input`,
`/var/lib/atstudio-backup/wi069-runs` and `/opt/atstudio-backup/bin`. Config and
approval files are in the per-run input directory, separate from credential
stage/install targets. No existing file is replaced; secure ancestry and fresh
run paths are checked. These exact paths, final code pins, selected action and
client-side private handling must be part of the eventual step-specific ALLOW.

## Test Evidence And Exact Pins

Command: `python -I -S -B build/WI070-review/run_review.py`

- Reviewer/environment: PG, local Windows, Python3.14.3.
- Window: 2026-10-11 22:01:34-22:01:35 KST; duration1.023s.
- Same listed candidate/core bytes before and after: true.
- Candidate inventory10/10 and preparation30/30 passed.
- Reused core35/35 passed.
- Independent PG14 tests:11 passed,3 failed as F070-01;0 errors,0 skipped.
- Total89 tests:86 passed,3 failed. Coverage percentage not measured.
- A process-wide audit hook blocks real child processes and network operations.
  Test writes are confined to `build/WI070-review`; synthetic proc fixtures were
  redirected there. No candidate runner output was written into WI069.

| Input / receipt | SHA-256 |
| --- | --- |
| `backup_checks.py` | `c5d1a89f2c4621bb5eeda7ce8ff067aebea647ccf037e4acb7792c91c77b8dd7` |
| `native_inventory.py` | `cc610d6b4d604431a10226b934a21277a61694b00bc5f9c962c9575ffb53956d` |
| inherited `backup_protocol.py` | `a890d997b49fd6ce1d997a95951087279f2246c7d0e71a1e035254061547fd43` |
| PG receipt | `49832e3ef4b60d5b351da4705e0d34659def5b361c1d227a919410240c4709c8` |

Full13-file pre/post pins, counts and timestamps:
[receipt.json](../../build/WI070-review/run-d9d1faf9b1834085839ba3b26a670c43/receipt.json).
Failure details:
[independent test log](../../build/WI070-review/run-d9d1faf9b1834085839ba3b26a670c43/test_pg_review.log).
These are interim observed pins, not an SE final freeze.

## Scope / DoD Check

- [x] Read WI070/WI069 handoffs, existing backup contract and current preparation.
- [x] Independently test current changed risk predicates without external effects.
- [x] Preserve prior receipts and relay concrete findings before final candidate.
- [x] Recheck corrected exact host identity in current drafts.
- [x] Retest fixed snapshot parser and close F070-01.
- [x] Independently test current first bootstrap draft and record observed pins.
- [x] Independently test current server transport draft and record observed pins.
- [x] Review final bootstrap config, transport/escrow and immutable freeze.
- [x] Retest F070-02 before granting bootstrap-only execution ALLOW.
- [x] Review real cold-stopped adapter, credential confinement and failure cleanup.
- [x] Review fresh isolated restore implementation, complete files and actual SQL import command path.
- [x] Bind step-specific execution verdicts to final immutable pins.
- [ ] Complete remaining WI quality metrics; do not claim100% critical coverage.
- [ ] Receive MA native backup/restore result; PG performs no native operation.

## Architecture Boundaries

The current draft correctly distinguishes `pre-feature-no-runtime-keys` from
full disaster recovery, and refuses to pretend that old `admission_reopened`
hooks prove a stopped/masked final state. The pure SQL checks require complete
table counts plus schema and full logical-dump hashes. At the original
preparation checkpoint these predicates did not implement source fencing,
repository initialization, process isolation, actual SQL import or resource
enforcement. The real cold adapter is now independently reviewed and approved
only for the exact bounded native verification above. Native outcome remains separate.

Bootstrap review will be step-specific: exact target and artifact, private path
ownership, absent-target/non-overwrite behavior, bounded child/resource handling,
and failure containment. A bootstrap ALLOW would not approve repository init,
backup, restore, an app start, SMTP, WI067 or public release.

## References And Traceability

Tier0 loaded: STD-001 core-principles; STD-004 documentation-standards; STD-002
development-standards; STD-005 glossary. PG role and current security policy
remain applicable. References: WI070/WI069 handoffs; backup README; restic plan;
quality gates and Evidence Pack standard; `/test` and
`/create-wi-evidence-pack` skills. Assignee PG; task type security/review/testing.

## Risks / Rollback / Next Owner

PG performed no target mutation. MA has separately reported bootstrap and tool
staging. Preserve all prior failed-test receipts and native history. PG edits
only its review directory and two reports; candidate and shared core belong to
SE/MA. Native failures retain private diagnostics and stop only owned transient
units; source storage is not deleted or overwritten. Cleanup/fence failure is
HOLD requiring MA recovery review, not a rollback/delete license. WI quality
completion remains open; later feature/Gmail work stays separately scoped.

## Related Documents

- [WI070 handoff](WI-20261011-ATS-070-handoff.md)
- [WI069 handoff](WI-20261011-ATS-069-handoff.md)
- [Current preparation specification](../../build/WI069-backup/cold-stopped-preparation.md)
- [Backup contract](../../deploy/single-vps/backup/README.md)
- [Restic plan](../../deploy/single-vps/runbooks/restic-backup-plan.md)
- [Security policy](../../docs/policies/security-policy.md)
- [User summary](../user/WI-20261011-ATS-070-summary.md)
