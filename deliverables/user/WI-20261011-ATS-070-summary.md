---
version: 0.25.0
last_updated: 2026-10-17
project: ATS
owner: PG
category: work-summary
status: active
dependencies:
  - path: ../agent/WI-20261011-ATS-070-evidence-pack.md
    reason: Independent test details and exact reviewed pins
---

# WI070 Incremental Backup Review

> Purpose: Separate current preparation findings from future native step approval.

## Summary

TL;DR: **ACCEPT_BOUNDED_BACKUP_RESTORE_FOR_AUTH_GATE**. The pre-feature WI060
backup/restore prerequisite is satisfied. The exact reviewed Restore allowance
has been consumed once; no further native execution is authorized by this report.

MA12ca09 reports run `a860c4be941b42189e99d1d6d0ba7727` completed with
BOUNDED_RESTORE_ONLY_PASS, cleanup true/no errors, app not run and no new backup.
Independent poststatea8483e confirms cold MySQL, zero application/DB/FFmpeg
processes, unchanged active nginx/boot identity and95,938,072,576 free bytes.
MAeb59be confirms actual column metadata equality, exactly209 witnessed insertions,
all other bytes equal and complete43-table count-map equality,6 rows on each side.
Unequal raw hashes are preserved, not relabeled equal.

The original repository/snapshot and payload bindings remain exact. Native result
and private-proof hashes are recorded in the [Evidence Pack](../agent/WI-20261011-ATS-070-evidence-pack.md).
PG performed no additional native audit or private-file access. This does not
grant authentication/app/Gmail execution, cover new staged roots or establish
full DR. WI070 remains active with quality-completion limits unchanged.

### Consumed Restore-Only Allowance

TL;DR: **ALLOW_BOUNDED_NATIVE_RESTORE_ONLY**, one new finite attempt using only
`build/WI069-backup/restore-20261017-47a250acccbe429293cf3a61006162c1`.

- Bundle manifest: `2ba25b72174b495216a87338142536b69e67162147d112d5d8bc1ec99b0ddb81`.
- Client manifest: `79232dcfa406ee54056e20a0cb1b46751934c0ef06849e44fb5745b0aa4f4ea8`.
- Frozen Python74/74, PC45/45, identity29/29 pass. F070-10 fixed; inherited
  capture/bootstrap bytes unchanged. No open changed-path execution finding.
- StageInventory and current matching Inventory precede Restore with fresh
  approval. Existing repository/snapshot and original payload/proof only;
  exact source recheck, metadata and209-edit byte proof remain mandatory.
- No init/backup, overwrite, automatic retry, source logical changes, global
  configuration changes, app/Gmail/public release. Earlier Run grants consumed.
- Native result is pending MA. WI070 stays active; critical100% coverage and full
  quality completion are not established. See the [Evidence Pack](../agent/WI-20261011-ATS-070-evidence-pack.md).

### Historical Design Review

TL;DR: **ALLOW_DESIGN_ONLY; HOLD_NATIVE_RESTORE_ONLY**. The narrow existing
repository/snapshot-bound continuation design is accepted for SE069 implementation
review, not execution. See the [design contract](../../build/WI070-review/restore-only-design-review-20261017.md).

Candidate finding F070-10 is open: numeric ordinal encoding conflicts with the
strict metadata decoder. Independent mutable-candidate tests:20/21 pass,1 failure,
0 errors. The fix is explicit decimal-text conversion before hex encoding; keep
the decoder strict. See [findings](../../build/WI070-review/restore-candidate-findings-20261017.md).

Require fresh exact original counts/raw hashes and actual source/restored column
metadata; permit only209 witnessed redundant charset insertions at actual column
DDL positions outside quotes/comments/expressions. All other schema/full-SQL bytes
remain exact. No naive global replace, init, backup, repository overwrite, source
logical mutation or old evidence rewriting. New code, fault tests and immutable
pins must be independently reviewed before native continuation. WI070 stays active.

The snapshot payload and original manifest/proof hashes stay bound to capture
9ef6927..., never the new run path. New IDs name only fresh local destinations
and results. MA has accepted the design; SE implementation is underway. The
MA-supplied MySQL8.0.46 mechanism explains the possibility, not snapshot proof.

MA receipt9fca9d now supplies exact original manifest/proof sizes and SHA-256
bindings, recorded in the design contract and Evidence Pack. Verify restored bytes
before parsing either as authority. Separate new-root JAR staging does not change
the snapshot member set; PG has not accessed private files or approved a new Run.

### Previous Native Restore HOLD

Previous **HOLD_RESTORE_CONTINUATION_PENDING_CAUSE**. MA receipt d35d04, run
`9ef6927ad8674c129729c9720284ff2f`, reached ISOLATED_SQL_RESTORE and failed exact
schema comparison. Query rc0, cleanup true, errors empty, application not run.
MA reports backup/check/exact-file restore passed; full SQL equivalence has not.

Snapshot `12e37d997ba170762201bab32ea5471f8ad374a71a06e122deb7bed2f0d04e45`
exists. The one-use approval is consumed: never rerun first-capture, init or
backup; preserve repository, snapshot, failed workspace and password. MA is
diagnosing reduced schema metadata. After cause, only a separately reviewed
repository/snapshot-bound restore-only continuation may be considered. No
comparison weakening, native success claim or new execution authority. WI070
remains active and PG has not accessed native/private data.

### Consumed Client Compatibility Decision

**ALLOW_BOUNDED_NATIVE_VERIFICATION**, one fresh Run after staging the
new immutable bundle and obtaining fresh Inventory. F070-09 is closed without
weakening default/login-file isolation; F070-08 remains fixed.

- Bundle: `9c4dc2ce1a210107d0cbceec380c7ede92a78e64c5309a03f30742370936c9bd`.
- Client: `837d808034d957b5d1063f7e448d7e6fe6f6a4c31b6874f5f8c96d0de9b8e686`.
- Use `build/WI069-backup/cold-client-20261017-837d8080/invoke-cold.ps1`.
- Frozen301/301, PC26/26 and33 identity bindings pass on unchanged bytes.
- Query/dump/import retain --no-defaults first, clean HOME and a guarded
  MYSQL_TEST_LOGIN_FILE=/dev/null override. Wrong device/parent or caller
  environment/label/options fail before process creation.
- Coverage87.54% statements/81.22% branches, zero exclusions; critical100%
  unestablished. WI completion and native success remain separate and pending.
- Preserve failed runs and password; fresh target/cold/repository/escrow checks,
  Inventory at most600 seconds old and a new finite approval remain required.
  No automatic retry, AppArmor/global changes, app/Gmail/public release or PG native access.

### Previous Client Failure

Previous **HOLD_NEXT_RUN**: F070-09 client-option incompatibility. MA reports StageInventory51e6cd PASS,
then Run `c88e15b448624dcf8e6bf2f34e48bf27` / f8ba30 HOLD at SOURCE_SQL_WINDOW:
MYSQL_QUERY rc2 after socket creation. Cleanup true, no errors/snapshot/init
attempt, application not run. The prior one-use approval is consumed. MA497989
confirms --no-login-paths is unsupported by installed mysql8.0.46; the readiness
hypothesis is not established. MAad181a confirms no-connection compatibility
with the remaining flags and explicit login-file/HOME isolation. Four independent
contract tests fail on the old bytes, zero errors. SE repair and new exact review
are pending. No automatic retry or source change by PG; WI070 remains active.

### Consumed AppArmor Repair Decision

**ALLOW_BOUNDED_NATIVE_VERIFICATION** for one fresh bounded retry after
new-bundle staging and fresh Inventory. F070-08 is closed on these exact pins:

- Bundle: `16ec2e473065ee8e5ce2d7c91430b8cad69f94c531a0a16a255b1232fa7235bf`.
- Client: `6bbf257ef0c489d39beb985c6c36d455f9e67c099dd592c1061cc6415ce25c24`.
- Client path: `build/WI069-backup/cold-client-20261017-6bbf257e/invoke-cold.ps1`.

Standard socket/PID paths, fresh exclusive0600 per-unit log, exact-file write
permission, collision/identity/size guards and bounded private diagnostics are
independently tested. No AppArmor/global MySQL changes or existing log overwrite.
Python285/285, PC26/26 and33 identity checks pass on unchanged frozen bytes.
Frozen coverage: statements86.65%, branches80.62%;0 exclusions. No100% critical
coverage claim and no WI completion. Preserve previous failed run and bootstrap
password. MA cleanup receipt53d993 reports no init/upload/snapshot. A fresh
target/cold-state/repository/escrow check and finite window remain mandatory;
HOLD is never automatic retry. No app start, production restore, Gmail or public
release. PG performed no native, network or private-secret operations.

### Earlier Decisions

Previous **HOLD_NEXT_RUN** for F070-08: MA's first native run consumed the previous
one-use approval and failed before dump/repository initialization. AppArmor denied
the custom MySQL log path; current profile evidence also excludes the custom
socket/PID names. Cleanup succeeded according to MA's sanitized receipt.

The required repair was to fix all three paths together without AppArmor/global changes, preserve
existing logs, and bind a fresh private bounded log. PG has added source/initialize/
restore pathname regressions. This historical HOLD is superseded only by the
new exact-pin verdict above. No automatic retry. WI070 remains active.

Earlier consumed verdict: **ALLOW_BOUNDED_NATIVE_VERIFICATION** for the exact one-use backup and
fresh isolated restore test. F070-08 was subsequently found in native testing.

- Bundle: `6991392ccb8f7e5d1b4c9d68aba86b17376881a83802b3068387bd518137ed79`.
- Client manifest: `dee98efc1c4e13c0e72d8c39aef4ac78a87e737f3772f8aca3a0cfc61c3b1882`.
- MA must use fresh exact target/cold-state/escrow checks and Inventory no older
  than600 seconds. Any HOLD or partial run requires recovery review, no retry.
- No production overwrite, app start, deletion/pruning, Gmail or public release.

PG withdraws the prior quality-only execution HOLD: STD-002 WI completion and
commit/release thresholds do not themselves prohibit this approved native test
while the WIs remain active. **WI quality completion remains open**; no coverage
requirement or real predicate finding is waived, and native PASS is not claimed.

Prior independent frozen-code measurement: **261/261 PASS, statements84.93%,
branches77.84%,0 exclusions**. Security-critical100% is not established. The
earlier two Windows fixture errors are fixed without weakening native guards.
Frozen PC client26/26 passes. MA reports StageInventory success without DB/restic.

Earlier stage verdict: **ALLOW_STAGE_INVENTORY**, reviewed2026-10-17. Only fresh private tool
staging and read-only inventory are allowed; **Run, DB start and restic execution
remain NOT approved** pending the full capture review. Python221/221 and PC25/25
pass on unchanged bytes. All33 frozen-code identity checks pass.

- Bundle manifest: `6991392ccb8f7e5d1b4c9d68aba86b17376881a83802b3068387bd518137ed79`.
- Client manifest: `dee98efc1c4e13c0e72d8c39aef4ac78a87e737f3772f8aca3a0cfc61c3b1882`.
- F070-01 through F070-07 are closed; none waived. MA must use fresh target
  and absent-destination checks. HOLD or partial operation requires recovery review.

Earlier bootstrap-only verdict, reviewed again on2026-10-17: F070-02 is
fixed and independently closed:35/35 PC tests and147/147 Python tests pass with
unchanged candidate bytes. Parent inheritance and child ACL are checked before
the first private write. F070-01 remains closed.

The verdict covers only the frozen Stage/StageAndInstall transport, binary
installation without execution, and private PC escrow. MA must refresh target,
SSH identity, active-key reference, ACL and absent-destination prerequisites;
old execution gates are expired. This is not native success evidence and does
not allow repository initialization, restic execution, capture/restore, app,
SMTP/Gmail or public exposure. Cold capture review continues under WI070.

**Prior capture HOLD:**221 local tests,217 PASS,4 FAIL,0 errors. Then-required repairs:
accept the existing base64url escrow format (F070-06), and reject incompatible
action/status/exit combinations in the transport and PC client (F070-07).
Disk reserve (F070-03), bundle import precedence (F070-04), and post-preflight
expiry (F070-05) are fixed and independently closed on the new bytes.

## Current Pins And Tests

- Manifest: `840ebbb7aeec7537d8a53205791fd28d2c5190905ea48edbe2b131e32ec48558`.
- Bootstrap: `f2f7c90c609a3a0689153c3bb40201cec684974e53ae6d6990d47eec86f98fe8`.
- Transport: `2a6e9eff6ff5a6f542454fdebc834b13a5c8f116042e649ef14bed5d3a434849`.
- PC client: `03681b54d1ba27abc96e2dabff613298354f3854ab96f303818ccabcc3c175f5`.
- [PC35/35 receipt](../../build/WI070-review/pc-helpers-627e547f85c2407aab0435380156c71e/receipt.json).
- [Python147/147 receipt](../../build/WI070-review/run-77c4e0d1604d4a43ad571fa355334778/receipt.json).
- No secrets, private inputs, native commands, network or source changes by PG.

## Historical Evidence

Earlier capture review remained HOLD for F070-03 (approved disk floor plus
remaining growth) and F070-04 (verified-bundle import precedence). Concrete
reproductions and pins are in the Evidence Pack; implementation remains SE-owned.
MA reports native bootstrap receipt `32ab08` as installed-not-executed with
PC copy hash matched and repository not initialized. PG did not inspect native
state or private inputs. The following entries preserve earlier local history.

- Tested2026-10-11 22:01:34-22:01:35 KST on Windows; before/after bytes matched.
- `backup_checks.py`: `c5d1a89f2c4621bb5eeda7ce8ff067aebea647ccf037e4acb7792c91c77b8dd7`.
- Receipt: `49832e3ef4b60d5b351da4705e0d34659def5b361c1d227a919410240c4709c8`.
- No secrets, private inputs, native commands, network or source changes by PG.
- Bootstrap tested22:07:34-22:07:35 KST, same bytes:
  `f2f7c90c609a3a0689153c3bb40201cec684974e53ae6d6990d47eec86f98fe8`.
- Bootstrap increment receipt:
  `b2e6cd9a1dfd5a90808d390d2ea0938cad9d005d5d7883651426bb4fba550efa`.
- Server transport: `a78db928da0fbe10dd98536dc89f986121e2e72bb3651132ac57823625189484`.
- Latest receipt: `60f63fa6dcb712d1fdb3deedab90afb1265be2528b44197273005518c119f3d5`.

## Related Documents

- [Evidence Pack](../agent/WI-20261011-ATS-070-evidence-pack.md)
- [WI070 handoff](../agent/WI-20261011-ATS-070-handoff.md)
