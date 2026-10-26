---
version: 1.5.0
last_updated: 2026-10-17
project: ATS
owner: SE
category: evidence-pack
status: draft
dependencies:
  - path: WI-20261011-ATS-069-handoff.md
    reason: Implementation scope and output contract
  - path: ../user/REQ-20261011-ATS-007.md
    reason: User-approved execution chain
  - path: WI-20261011-ATS-070-evidence-pack.md
    reason: Independent review remains the release authority
---
# Evidence Pack: WI-20261011-ATS-069

> Purpose: Hand over the bounded first-backup and isolated-restore candidate without claiming native verification from local tests.

## Summary

TL;DR: MA reports bounded native restore-only completion in receipt12ca09 on2026-10-17: BOUNDED_RESTORE_ONLY_PASS, COMPLETE, cleanup true with no cleanup errors. Final MA poststate a8483e and semantic evidence eb59be are observed bounded PASS; PG070 accepts ACCEPT_BOUNDED_BACKUP_RESTORE_FOR_AUTH_GATE. The existing snapshot was restored without new backup or application start. All previous HOLD outcomes remain preserved. Full disaster recovery and complete quality coverage are not claimed. The first-capture path must not run again.

## Native Completion Addendum

Evidence owner/operator: MA. Verification date:2026-10-17, named production target,
bounded isolated restore. SE records the MA relay and the public PG decision;
SE did not execute native commands or read private SQL, secrets or result bodies.

| Evidence | Reported result |
| --- | --- |
| [PG070 exact-byte decision](WI-20261011-ATS-070-evidence-pack.md) | ALLOW_BOUNDED_NATIVE_RESTORE_ONLY for bundle2ba25b72... / client79232dcf... |
| MA StageInventory87406c | PASS; run `0e023ad08de84bbca5bc4b6396f09a2d`; original inventory2908455e... and units861518... unchanged |
| MA native receipt12ca09 | Restore run `a860c4be941b42189e99d1d6d0ba7727` |
| Status / stage | `BOUNDED_RESTORE_ONLY_PASS` / `COMPLETE` |
| Cleanup | `true`; cleanup errors `[]` |
| Application / backup | `NOT_RUN` / `NOT_RUN_EXISTING_SNAPSHOT` |
| Original capture | `9ef6927ad8674c129729c9720284ff2f` |
| Repository | `36fc435727e7518894734fa2f49eb65481f4b015e91874a4dbfe3f8a2a3172df` |
| Full snapshot | `12e37d997ba170762201bab32ea5471f8ad374a71a06e122deb7bed2f0d04e45` |

This is a separate successful continuation, not a rewrite of receipt d35d04's
SQL_SCHEMA_DRIFT or either earlier native HOLD. The exact reviewed adapter's PASS
requires original43-table counts/raw hashes to re-match, complete actual column
metadata to match, and the same209 permitted charset insertions to explain both
dumps with all other bytes identical. It does not relabel unequal source/restored
raw dump hashes as equal. Exact file/mode/owner/empty-directory verification,
owned cleanup and the adapter's final cold-state fence are part of that bounded
result. MA's separate poststate observation was subsequently relayed as a8483e;
the final evidence below resolves this addendum's earlier pending-poststate note.

### Final MA Poststate And Acceptance

MA's final2026-10-17 relay supplies the following bounded observations for the same
restore run `a860c4be941b42189e99d1d6d0ba7727`. These are MA-owned native evidence,
not new SE execution or private-file inspection.

| Evidence | Observed result |
| --- | --- |
| MA a8483e, MySQL | Masked, inactive, MainPID0 |
| MA a8483e, writers | App/java/mysqld/mariadb/ffmpeg all0 |
| MA a8483e, nginx | Original nginx active, PID34885 |
| MA a8483e, boot identity | Unchanged `e8dd67fe-4085-40c9-bb51-23eeabebf91e` |
| MA a8483e, free space | 95,938,072,576 bytes |
| `public-result.json` SHA-256 | `e56de048c842f8096ebdb44b27756306b49e6b17c265183692dc8caa6f023043` |
| `verified-content.json` SHA-256 | `b82f375f057362dda3c86824b5c4a0cb4e2902a1699e9fb98a9883780bdcd874` |
| MA eb59be, actual column metadata | Equal: true |
| MA eb59be, lexical proof | All other bytes equal: true; edits209; raw hashes equal: false |
| MA eb59be, row counts | All43 table count-map entries equal;6 total rows |
| PG070 final verdict, relayed by MA | `ACCEPT_BOUNDED_BACKUP_RESTORE_FOR_AUTH_GATE` |

Disposition: observed bounded backup/restore PASS accepted for the auth gate.
This closes the pending MA poststate evidence in this latest addendum only.
Historical HOLDs and their then-pending decisions below remain unchanged. No auth,
Gmail, application, public release or further native action is executed by SE.

Preserve repository/password/snapshot, original payload, old candidates and all
failed receipts. This successful one-use Restore call is consumed; the commands
in its handoff are historical, not authority for another run. No automatic retry,
new backup, application start, Gmail action, public release or cleanup deletion.
The baseline is WI060, not the separately staged newer release. Runtime-key escrow
and full disaster recovery remain outside this completion. Local coverage remains
82.62% lines/74.10% branches; critical100% and full quality completion remain open.

## Restore-Only Candidate

- [Complete execution handoff](../../build/WI069-backup/restore-only-handoff-20261017.md): Exact IDs, original proof hashes, lexical/metadata contract, official8.0.46 source mechanism, tests, bounded transport commands and failure preservation.
- [Immutable freeze](../../build/WI069-backup/restore-20261017-47a250acccbe429293cf3a61006162c1/freeze.json): Bundle `2ba25b72174b495216a87338142536b69e67162147d112d5d8bc1ec99b0ddb81`; client `79232dcfa406ee54056e20a0cb1b46751934c0ef06849e44fb5745b0aa4f4ea8`.
- [Local tests](../../build/WI069-backup/tests-2f81ab0a24b447e5bb4d002ea11529a2/result.json):244 passed; PC restore client48 passed. Test scope is synthetic; no secret/native/real DB access.
- [Coverage](../../build/WI069-backup/coverage-fd07afad78664fd29f27c6e9c2bc7210/measurement.json):82.62% lines,74.10% branches, zero exclusions; security100% not established. Full quality completion remains NOT_MET.
- New code: `restore_only.py`, `restore_equivalence.py`, `restore_entry.py`, `restore_transport.py`, `invoke-restore.ps1`; four focused Python test modules and PC tests; freeze/coverage helpers under the owned build directory.
- Existing capture/core/bootstrap bytes and all four earlier frozen bundles were checked unchanged. Original failures and raw SQL hashes remain historical evidence. No unrelated source edits.
- Initial freeze gate was READY_FOR_PG_BYTE_REVIEW. PG subsequently allowed these exact bytes and MA receipt12ca09 reports bounded native restore-only PASS as recorded above. No earlier HOLD is relabeled.

## Third Native Result

Historical first-capture result and then-pending decisions below are preserved.
The separate restore-only completion addendum above supersedes their pending state.

MA reports PG070v0.18 ALLOW for bundle `9c4dc2ce...` / client `837d8080...`, then StageInventory `c2fffe` (`879817edaf8e49c29138972156a00b88`) with unchanged inventory/unit identities. The finite Run `9ef6927ad8674c129729c9720284ff2f` finished in receipt `d35d04` as HOLD at ISOLATED_SQL_RESTORE / SQL_SCHEMA_DRIFT. Last query rc0; cleanup true, cleanup errors empty, app not run. This supersedes the earlier in-progress observation `e5d523`, not the preserved first two failures.

Exact snapshot: `12e37d997ba170762201bab32ea5471f8ad374a71a06e122deb7bed2f0d04e45`.

Repository initialization, backup, full-data restic check and exact file restore were reached successfully according to MA. SQL restoration acceptance was not achieved. In `backup_checks.py:287`, complete table counts are compared before schema hash; the subsequent full logical-dump hash comparison is not passed when SQL_SCHEMA_DRIFT is raised. Source and recovered schema are compared as raw byte hashes, without normalization. No schema cause is established by local inspection.

Next decision requires MA's sanitized structural difference and exact recorded repository ID. Any later approved continuation must bind that repository, existing bootstrap/password reference and this full snapshot; omit init/backup, use a fresh disjoint restore destination, preserve existing evidence/data, retain cold-source fencing and cleanup. No automatic retry, reinitialization, deletion, existing-data overwrite or original source restart is authorized. SE performed only local source/test reads and this report update; frozen executable bytes remain unchanged.

## Consumed Compatibility Candidate

MA first Run `9d020f` held on the AppArmor error-log pathname. Standard socket/PID and a fresh bounded private log repaired that contract; PG independently passed285 Python tests and approved bundle `16ec2e47...` / client `6bbf257e...`. Second native Run `f8ba30` (`c88e15b448624dcf8e6bf2f34e48bf27`) held at MYSQL_QUERY rc2. Both report cleanup true, no snapshot, no app start and no repository init. MA `497989` proved the pinned8.0.46 mysql does not accept `--no-login-paths`; `ad181a` verified query/dump flags without it under `--help`, with no DB connection.

Current [compatibility handoff](../../build/WI069-backup/mysql-client-compat-20261017.md) supplies official sources, exact contracts, full pins, commands and preserved failures. [PC client](../../build/WI069-backup/cold-client-20261017-837d8080/invoke-cold.ps1) contains the new immutable bundle.

| Current artifact | SHA-256 |
| :--- | :--- |
| Bundle manifest | `9c4dc2ce1a210107d0cbceec380c7ede92a78e64c5309a03f30742370936c9bd` |
| Client manifest | `837d808034d957b5d1063f7e448d7e6fe6f6a4c31b6874f5f8c96d0de9b8e686` |
| Dispatcher | `d577629170099da3345c528e69a5fb60d7b52892dc20bf2de7b52307a86db766` |

Query, full/schema dump and import keep first-option `--no-defaults` and explicit socket/root. The unsupported flag is removed. The command boundary fixes MYSQL_TEST_LOGIN_FILE=/dev/null in a clean non-inherited environment and validates the root-owned1:3 character device and non-writable/non-linked ancestors before spawn and inventory. It rejects environment overrides and binary/label mismatches. MYSQL_UNSUPPORTED_OPTION is a fixed diagnostic category, never raw stderr. No existing config, AppArmor policy, shared core or old frozen bytes changed. The source-based login-file reasoning and MA help-only compatibility evidence are distinct from native access tracing.

Current tests: [196/196 SE/core PASS](../../build/WI069-backup/tests-ac73d08149154dd5a386a5388874410b/result.json), 2.428209 seconds; [48 PC cases PASS](../../build/WI069-backup/cold-client-tests-ad853ff638ab423a9b7bf6d047827f4c.json), no native/SSH/secret reads. Nine compatibility regressions include actual synthetic import selection; nine path/log regressions remain. PG's earlier285-test PASS applies only to the previous path-repair candidate, not this new manifest.

Current [Coverage.py7.16.2 measurement](../../build/WI069-backup/coverage-9234c6e3228b4194a1859ca7d0c3d967/measurement.json): lines1530/1809=84.58%, branches305/400=76.25%, zero code exclusions, all nine production paths included, test exit0. Official wheel was used locally without installation/network, branch=True, Python tracer. Critical-path100% remains NOT_ESTABLISHED and the completion quality gate remains open. Windows/Linux mocked boundaries do not become native proof. One intermittent unchanged-core Windows state-write test failure remains preserved and unexplained; stale local bundle-fixture errors were corrected only in owned tests. See handoff for all receipts.

## Scope and DoD

- [x] Narrow WI-local adapter for the cold-stopped target, complete approved roots, logical MySQL capture, exact restic snapshot, isolated restore and SQL comparison.
- [x] Actual upload/root staging/inventory/private approval/run client with pinned SSH and no manual secret conversion.
- [x] Preserve shared backup core, WI064 and existing dirty work; protected 24-file baseline matched at freeze.
- [x] Source gates, fixed resource/deadline limits, restricted diagnostics and owned-process cleanup.
- [x] Default actions are inert; local tests block subprocess/network use.
- [x] PG070v0.18 capture ALLOW and subsequent exact-byte restore-only ALLOW; both one-use native calls are consumed.
- [x] MA-reported bounded authenticated backup/exact file restore followed by isolated SQL semantic verification: original d35d04 capture plus separate12ca09 restore-only PASS. Historical raw-hash HOLD retained.
- [x] Independent MA poststate a8483e after12ca09; semantic evidence eb59be and PG070 bounded acceptance recorded in the latest addendum.
- [x] Current measured minimum80% lines/70% branches established without code exclusions.
- [ ] Critical-path100% coverage and complete quality gates established.
- [ ] Full disaster recovery key escrow. This pre-feature capture excludes runtime key classes.

## Reference Documents

Tier 0 was supplied in order: STD-001 core principles, STD-004 documentation standards, STD-002 development standards, STD-005 glossary. The handoff supplies Tier 1 security-policy, quality-gates and evidence-pack-standard pointers; Tier 2 test/evidence skills, restic plan and backup README. Current work reread the WI069 handoff, `/test` and `/create-wi-evidence-pack` instructions and followed their bounded test/report contracts. No subdelegation occurred.

The unchanged `scripts/deployment/backup/backup_protocol.py` is reused byte-for-byte. This dedicated adapter does not pretend to satisfy the generic resume-admission/full-runtime-key contract, and does not change that core. Existing WI060 patterns informed source-unit, mount identity and cleanup handling.

## Historical First Artifacts

| Artifact | Identity |
| :--- | :--- |
| Current capture manifest | `6991392ccb8f7e5d1b4c9d68aba86b17376881a83802b3068387bd518137ed79` |
| Capture dispatcher | `e022af011226aa4e01be3756cea79844b817509c57f355672e7152420f76d4b1` |
| Capture ZIP | `717ccb657c38a568f5d2449dd1ef05b1681beeee73a73a85b76201412e22f076` |
| Current client manifest | `dee98efc1c4e13c0e72d8c39aef4ac78a87e737f3772f8aca3a0cfc61c3b1882` |
| PC client | `9b3b1d0a1c9e8ea350a3a07ce24ef972168d4ce66bed8c30a8b4be48674919f7` |
| Root transport | `2abd30bf85dac3bd6402d6e4bcef23afb2d5c9eedf3bc6f615a2aa98a31a1c1e` |
| Unchanged bootstrap | `f2f7c90c609a3a0689153c3bb40201cec684974e53ae6d6990d47eec86f98fe8` |
| Unchanged shared protocol | `a890d997b49fd6ce1d997a95951087279f2246c7d0e71a1e035254061547fd43` |

Historical [first client](../../build/WI069-backup/cold-client-20261017-dee98efc/invoke-cold.ps1) includes the original frozen runtime bundle. [Original execution handoff](../../build/WI069-backup/cold-handoff-20261017.md) preserves that attempt's commands and failure instructions; use the current compatibility handoff above for the new review.

Older candidates and all failed test logs are preserved. In particular capture `4e1238...` and client `cf7ef94b...` are superseded, not executable release candidates.

## Historical Test Evidence

Environment: Windows, Python 3.14.3 and PowerShell 7.6.5, October 17, evidence owner SE. These tests simulate Linux ownership, MySQL, restic and systemd; they do not prove native compatibility. Only synthetic local Windows ACL files are actually created for ACL tests.

| Command | Result | Evidence |
| :--- | :--- | :--- |
| `python -I -S -B build/WI069-backup/run_local_tests.py` | 194 passed, 0 failed/errors/skips, 2.137101 seconds | [Result](../../build/WI069-backup/tests-2395a49ddcb44ea4a5dd3da43fa491d7/result.json), [Log](../../build/WI069-backup/tests-2395a49ddcb44ea4a5dd3da43fa491d7/tests.log) |
| `pwsh -NoProfile -File build/WI069-backup/test-cold-client.ps1` | 48 pure cases passed, parser errors 0; no secret reads/SSH | [Result](../../build/WI069-backup/cold-client-tests-cd05f1472aed4c8b862669798b329710.json) |
| `pwsh -NoProfile -File build/WI069-backup/test-bootstrap-client.ps1` | 14 passed including synthetic protected/non-inheriting ACL behavior | [Result](../../build/WI069-backup/client-tests-eeeb360d86eb4061b0fe4b7771de89f7.json) |
| `python -B build/WI069-backup/freeze_candidate.py` | 24 protected files matched; exact manifest verification | [Freeze](../../build/WI069-backup/cold-20261017-aee886390451432a89de618d49410b4e/freeze.json) |

The 194 total includes 23 PG-authored capture and 16 PG-authored transport tests replayed by SE. Their replay is not an independent ALLOW. The audit hook rejects real subprocess launches, shell commands, socket connects/binds and DNS in the Python suites. Mocked pipeline success is explicitly synthetic. No Java/frontend build or actual MySQL/restic invocation occurred in SE work.

Fix traceability: F070-01 duplicate JSON/bool-owner protection is local to the wire wrapper; F070-02 requires inheritable owner-only ACL and checks the empty new child before password writes; F070-03/04 fixes preserve a 4 GiB growth reserve above the max(20%,10 GiB) floor and recheck expiry after preflight; F070-05 flat-bundle import precedence preserves the exact protocol; F070-06 accepts the actual URL-safe password format; F070-07 binds action-specific success to exit 0 and HOLD to exit 2 on both ends.

Earlier failures were retained: Windows-only file metadata/flag shims were corrected in tests, a malformed synthetic file-size expectation was corrected, and one unchanged-core Windows `os.replace` permission failure was retained before a later successful rerun. Source gates were not relaxed to make a native test pass.

## Native Evidence Boundary

MA-reported October 17 observations: `270804` target match/cold source/96,621,436,928 bytes free; `dadd91` MySQL/config/quarantine/restic pins and absent runtime socket directory; `32ab08` installed pinned restic and matching private PC password copy for bootstrap run `b374c1371f3c4f9994b3b55dab829b15`; `9c22ca` anonymous TLS endpoint-root HEAD 200. None proves authenticated bucket operations or backup/restore. Earlier October 11 observations are historical and not used as fresh admission evidence.

## Risks and Rollback

- Linux systemd/AppArmor/socket startup, authenticated provider compatibility, restore ownership and full SQL round-trip remain native gates. A failure is HOLD, not permission to weaken them.
- The current runtime baseline is WI060, not prepared WI068. Complete logical database data is included, but SMTP/JWT/billing/runtime secrets are not escrowed here; full disaster recovery remains incomplete.
- Disk floor includes growth reserve; source/repository size projection is below 1 GB, not a hard provider billing cap.
- Bootstrap and existing source are never rolled back by deletion. Stop only dispatcher-owned processes, restore/verify the original cold fence, preserve private evidence and partial repository state.
- After any init attempt, no blind retry. Existing-repository/restore-only continuation requires an exact repository ID, unchanged password reference, exact full snapshot and separately reviewed continuation. The first-capture tool deliberately does not implement adoption or automatic resume.
- Coverage measurements must remain explicit. Local test success does not close the quality gate or replace native verification.

## Follow-ups

WI069 blocks WI070 independent release review, then MA native backup/isolated restore, then WI067 feature work. SE has not delegated, executed native operations, or begun Gmail work. MA owns subsequent gates and current progress updates.

## Related Documents

- [WI069 Handoff](WI-20261011-ATS-069-handoff.md): Approved ownership.
- [WI070 Evidence](WI-20261011-ATS-070-evidence-pack.md): Independent decision.
- [REQ007](../user/REQ-20261011-ATS-007.md): Approved chain.
- [User Summary](../user/WI-20261011-ATS-069-summary.md): Concise status.
- [Execution Handoff](../../build/WI069-backup/cold-handoff-20261017.md): Exact runnable procedure.
- [Current Compatibility Handoff](../../build/WI069-backup/mysql-client-compat-20261017.md): New candidate, official evidence, current tests and invocation.
