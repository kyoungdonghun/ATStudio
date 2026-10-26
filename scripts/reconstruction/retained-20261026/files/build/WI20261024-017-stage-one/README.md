# WI-20261024-ATS-017: current backup cost to audio

The current user approved continuing stage one. MA records that actual decision and performs all native operations; SE/PG use synthetic local inputs only. This wrapper adds measured cost metadata and a conservative calculation to the unchanged WI011 backup lifecycle. It does not issue approval, claim current fees, renew dates, modify the JAR or add cloud operations. Reset execution is separately owned by WI020; application browser-player acceptance remains a separate actual test.

## Immutable execution map

| Phase | Executable and required evidence | Stop boundary |
|---|---|---|
| Target | Reviewed WI014 probe and exact boot/machine observation | Cold/identity/expiry failure |
| Inventory | This `stage_one.py inventory` wrapping WI011 candidate `158d2bb3064d112e625b7b8db914925636fe9b72f68161671da862d84dc19839` | No DB/cloud; stages only the unchanged original WI011 files; returns private baseline pins and measured cost metadata |
| Cost | `stage_one.py check-cost` with actual metadata and fresh MA provider facts | Unknown, stale, nonzero tariff or account-wide storage overflow; worksheet is not authority |
| Backup | `stage_one.py capture` with exact genuine WI011 approval/decision/cost/source plus this scope record | Current repository, fixture/grant, cost and source must match; one backup and one exact isolated restore; no replay |
| Audio | Unchanged WI005 `local.py build`, then `execute --execute-approved-native` after genuine backup and exact PG binding | Fixed 2026-10-24 KST, >=600 seconds remaining, actual source/target/actors; no automatic extension |
| Human | Verified one-MP3 playback/seek and API Range evidence, then WI020 buyer3 reset | Neither API/MP3 bytes nor a reset-email HTTP200 proves these human outcomes; application browser-player UX remains separately untested |

MA's WI016 receipts observed repository bytes 63,569,920 and nine objects, included storage 1 GB, temporarily free API, internal provider traffic exemption and PC-to-host password reference match. Those dated observations explicitly did **not** establish future capture cost or current fixture recovery. Refresh the actual account evidence before capture. No price lookup or server/private-file access was performed by SE.

## Native metadata and calculation

The WI011 inventory lifecycle supplies the member map through a narrow in-memory allowlist repair. The unchanged original traversed both releases but allowed only the current JAR and old WI060 frontend, so MA's bounded inventory stopped on the current frontend143 files with `UNREVIEWED_SOURCE_FILE`, before any DB/cloud operation. `install_release_inventory` adds only the current frontend entries from the frozen runtime's `binding/artifact.json` `spa_files`; that manifest is checked against the already pinned runtime freeze. Current JAR, old frontend and runtime roots remain unchanged. The original complete-tree collector still rejects unexpected/missing files and size/hash differences. No target-observed hash becomes new authority, and no file is ignored. The repair applies to inventory and every capture source checkpoint; WI011 bytes remain unchanged.

The new in-memory observer checks every member and ancestor plus the existing future-payload ancestors: no symlink/special file, xattr name list empty, basename UTF-8 <=255 bytes, path <=4096 bytes, stat integers within unsigned64, local passwd/group names <=256 bytes. It never reads xattr values, emits names, calls NSS or starts a service. Unsupported ACL/xattrs fail closed rather than being omitted or changed. Snapshot members retain the original file/type/mode/owner/hash checks.

The returned fields are numeric counts/limits plus private baseline hashes. `manifest_bytes` uses exactly the retained `boot.encoded` compact sorted JSON shape; logical/schema dumps add at most 2 MiB each and SQL proof adds at most 16KiB. Four payload files and three payload/ancestor directories are reserved. `source_unchanged` is wrapped in memory for capture, so the same metadata must still match before the first restic request and at every existing source checkpoint. Original cleanup remains unchanged. A budget/metadata failure after an uncertain backup does not erase snapshot uncertainty or authorize retry/deletion.

The reproducible conservative bound uses no deduplication or compression saving:

- `S = source_bytes + 4 MiB + manifest_bytes + 16 KiB`; `F = observed files + 4`; data blobs `Q = ceil(S / 512 KiB) + F`.
- Zstd encoded size is bounded by `S + 17Q + 3 floor(S / 64 KiB)`. Add encryption32/blob, compressed header41/blob and at most one36-byte pack trailer/blob: data packs <=`S + 126Q + 3 floor(S / 64 KiB)`.
- Tree plaintext <=`8192 * nodes + 67Q + 32 * trees`. The8192 allowance covers field names, basename worst JSON escaping1530, two owner/group strings3072, bounded numeric/time/stat/subtree fields and JSON punctuation. Linux regular files/directories only, empty xattrs, no symlink target/error/generic Windows attributes. Tree count includes all directories and synthetic absolute-path ancestors.
- Index plaintext <=`(512 + 256) * (Q + trees)`: <=512 per JSON blob/pack entry and256 envelope; conservatively one nonempty pack and one index file per blob. Every new pack is stored once in a non-final index; existing loaded indexes are final and not re-issued by normal backup. Empty/final merge does not create additional blob entries.
- Unpacked index files add zstd bound plus33 bytes each (version/encryption). Reserve51 auxiliary snapshot/lock objects, each plaintext <=64KiB: five commands * (two initial saves + six periodic refreshes + two stale-refresh allowances) + one snapshot, across an operation <=1800s. No space saving from lock deletion is assumed.
- Add4095 allocation bytes for **every** bounded new object. Account-wide retained total is repository actual bytes + other already stored account bytes + explicitly reserved concurrent growth + all bounded new bytes. It must remain <= actual included storage bytes. No historical snapshot deletion is assumed, including after the chosen planning horizon. Continuing storage is evaluated under the current contract, not a promise that future tariffs never change.

Source-backed format evidence was read by MA on2026-10-24 for actual retained restic0.19.1: [repository format](https://restic.readthedocs.io/en/stable/100_references.html), [Node fields](https://github.com/restic/restic/blob/v0.19.1/internal/data/node.go), [compression behavior](https://github.com/restic/restic/blob/v0.19.1/internal/repository/repository.go), [index lifecycle](https://github.com/restic/restic/blob/v0.19.1/internal/repository/index/master_index.go), [index encoding](https://github.com/restic/restic/blob/v0.19.1/internal/repository/index/index.go), [repository lock](https://github.com/restic/restic/blob/v0.19.1/internal/repository/lock.go), [lock replacement](https://github.com/restic/restic/blob/v0.19.1/internal/restic/lock.go), [zstd1.18.6 MaxEncodedSize](https://github.com/klauspost/compress/blob/v1.18.6/zstd/encoder.go). Compression can enlarge data; raw fallback is not assumed. Current API/internal-network zero tariffs must cover metadata/list/backup/restore requests and transport retries. Unknown endpoint/account coverage is HOLD. Upload/download4096 limits are throughput controls, not spending caps.

## Concrete MA preparation

Use `%LOCALAPPDATA%/Python/pythoncore-3.14-64/python.exe`, owner-only private files outside the repository and the retained strict SSH stdin transport from [WI011 README](../WI20261024-011-backup-preparation/README.md). No shell interpolation, raw stderr streaming, agent auto-start or key discovery is added.

Initial inventory uses the **existing** WI011 exact approval/decision schemas (`backup_plan.authority`). Set mode`inventory`, candidate`158d2bb...`, fresh32hex run, actual boot/machine, issued/expiry<=1800s, expected counts users3/subscriptions1/admin-audit1 and the exact already observed grant1/user3/plan1 MONTHLY/CANCELLED with dates2026-10-24 and pendingNULL. Inventory/unit/escrow/cost pins are null; omit cost bodies. This is an MA record of the current real user decision, never a synthetic test record.

The additional raw scope record has exactly:

```text
wi: WI-20261024-ATS-017
source: CURRENT_EXPLICIT_USER_DECISION_RECORDED_BY_MA
scope: STAGE_ONE_CURRENT_BACKUP_COST_METADATA
action: inventory | capture
candidate_sha256: exact reviewed WI017 candidate.json hash
backup_candidate_sha256: 158d2bb3064d112e625b7b8db914925636fe9b72f68161671da862d84dc19839
approval_sha256: exact original WI011 approval-file bytes hash
issued_at / expires_at: identical to that approval
cost_input_sha256: null for inventory; raw cost-input.json hash for capture
no_business_mutation: true
no_app: true
no_additional_provider_operation: true
```

The last flag means this extension adds no operation beyond the already approved frozen backup/restore commands. It does not describe capture as cloud-free. Preserve the exact raw scope and decision bytes, including formatting.

```text
<python> -I -S -B stage_one.py inventory --candidate <WI017 pin> --approval <private approval> --decision <private raw decision> --decision-pin <raw pin> --scope-record <private raw scope> --scope-pin <raw pin> --output <new private stdin payload>
```

Send bytes once via retained SSH to `sudo -n /usr/bin/python3 -I -S -B -`. The original frozen tree and records stage exclusively under their new original run/mode path. WI017 modules execute only in memory. The scope/raw/code pins are checked before staging. Output includes `cost_metadata` and private inventory/unit pins; keep the whole response private and publicly project counts/status only. Do not replay a staged run. No capture occurs during inventory.

For the repair, preserve both failed attempts and staged files. After exact independent review and MA's bounded resume authorization, use a new run and new immutable records bound to the repaired candidate. The first bootstrap failure occurred while the host clock lagged the PC; confirm the fresh server epoch is at least `issued_at` before dispatch and that the remaining approved window is valid. Do not change clocks or relax authority checks. A successful inventory is still required before cost/capture admission; local regression PASS does not establish native success.

The later first capture reached `SOURCE_SQL_WINDOW` and held on SQL1046: the retained account query used unqualified table names while its mysql client had no selected database. The public MA receipts record snapshot `NOT_ATTEMPTED`, no payload files and STOP/PID_ZERO/SOCKET_GONE/COLD_BASELINE all PASS; existing repository config/snapshot reads had already succeeded. The source SQL was read-only; no dump, new snapshot or business mutation was reported. Preserve this attempt as a separate failure, not a successful capture.

`install_account_query_context` now prefixes only the account inventory generator with the fixed ``USE `atstudio_prod`;`` statement in capture memory. Its original READ ONLY transaction, SELECT projections and ROLLBACK are otherwise byte-identical. The generic query method and initial/isolated health checks remain unchanged: selecting a schema globally would fail before the isolated dump creates it. Subsequent table/column inventory uses `information_schema`; row counts fully qualify the schema; complete/schema dumps use `--databases atstudio_prod`, and isolated import consumes that complete database dump with its CREATE/USE context. No schema choice comes from input. This local integration review found no other occurrence of this missing-context defect; it does not prove native dump/restore success. A new actual attempt still requires exact review, fresh one-use records/cost window and MA's bounded resume decision; no automatic replay occurs.

Create `cost-input.json` with exactly `metadata` (the actual returned map), `provider` (fresh actual facts), and `format_proof` (the reviewed constants in `cost_bound.calculate`). The executable test's `sample()` is synthetic shape documentation only. Provider required keys are currencyKRW, observed_at/expiry, repository_actual_bytes, **account_other_stored_bytes**, storage_included_bytes, other_growth_reserved_bytes, concurrent_writers_absent, account_allowance_verified, api_currently_free, same_provider_internal_traffic_free, same_internal_endpoint_verified, tax_on_zero_is_zero, retained_storage_current_terms_continue. Unknown facts must not be filled with optimistic values. Refresh observations within600s of admission and cover the entire operation expiry; ongoing checks use that original admitted time, never renew it.

```text
<python> -I -S -B stage_one.py check-cost --cost-input <private measured cost-input.json>
```

Only `WITHIN_CURRENT_INCLUDED_STORAGE` permits MA to finish its external zero-cost record. It remains `authorization=NOT_ISSUED` and is not a provider hard cap. Bind the actual provider observation, reviewed format evidence and raw cost-input hashes in the existing cost-source `evidence_references` (<=8). Its observed_repository_bytes must equal the provider measured repository bytes; max_capture_bytes must equal cost.max_source_bytes and cover calculated source_payload_bytes_bound. The original cost window must cover the capture approval and has additional paid upper bound0KRW. No deletion or perpetual-price guarantee is implied.

Create a fresh capture run/approval/decision/scope using the measured baseline/unit pins and internally matched PC escrow hash. Preserve old records. Supply original cost/cost-source bytes and the exact cost-input bound by the scope. The calculator and metadata guard run again before original stages/cloud work.

```text
<python> -I -S -B stage_one.py capture --candidate <WI017 pin> --approval <new capture approval> --decision <new raw decision> --decision-pin <raw pin> --scope-record <new raw scope> --scope-pin <raw pin> --cost <actual cost record> --cost-source <actual source record> --cost-input <same bound measured input> --output <new private stdin payload>
```

## Actual backup evidence to unchanged audio

Require actual `CURRENT_CAPTURE_ISOLATED_VERIFY_PASS`, identified new snapshot and all STOP/PID_ZERO/SOCKET_GONE/COLD_BASELINE PASS. Preserve the actual private `verified-content.json` and result; do not publish SQL/cell/input hashes or snapshot IDs. MA/PG must verify the same run's complete43table counts, expected fixture/grant, full file/SQL equivalence and current target/source pins, then reobserve cold state. Only that actual evidence can support WI005's `backup-restore-source.bin` and its matching evidence/provenance hashes and current_fixture_data_covered claim. An old success string, opaque hash or local PG allowance is insufficient.

Use the existing saved three-actor private input, not a new capture. The current user decision must explicitly bind the frozen audio PLAN, including all three login refresh_token/updated_at effects, one track/source/MP3/activation/download/license and retained trial data. PG supplies the genuine exact native audio allowance. Build all four genuine source receipts and native approval per [WI005 README](../WI20261024-005-audio-wiring/README.md), fixed cd32 candidate and day2026-10-24. The legacy schema label `EXPLICIT_FUTURE_AUDIO_SERVER_DATA_APPROVAL` can record the actual current decision; it is not a reason to fabricate a receipt or request routine permission again. Stage/start only once; no relaunch after uncertain return. Complete cleanup before one-MP3 transfer and human listening.

## Reset and browser acceptance remain distinct

WI020 owns the separate reset runtime. Prefer buyer3 after audio, preserving all ID1 fields outside the already approved audio login effect. Existing v8r1 permits RESET_LINK receipt only and explicitly forbids opening/submitting; frozen audio disables SMTP and forbids mail in its PLAN. Neither can be relabeled a completed reset.

Actual reset acceptance requires the designated mailbox receiving a new message, the user opening its link and directly entering a new password, one successful consume, new-password LOGIN/ME to ID3, and rejection of consumed-token reuse. `EmailService` catches SMTP failure, so HTTP200 alone is insufficient. Expected DB effects are replacement of that actor's old reset-token rows, one token's used flag, that actor's password and refresh-token clearing plus timestamps. Existing access JWTs are not proven immediately revoked. DTO is8..100characters; retained BCrypt also imposes72UTF-8bytes. No password/token/URL may enter chat/logs. WI020 retains no new password: after successful reset it writes only a stale-credential marker, preserving historical saved inputs and preventing their automatic reuse. A future credential capture, if actually needed, is a separate operation; it is not performed or claimed here.

The frozen audio result explicitly leaves `human_playback=NOT_RUN` and `browser_player_ux=NOT_TESTED`. Stage-one playback/seek evidence consists of actual human playback/seek on the verified MP3 plus the separately observed API Range behavior. The real application browser-player UX remains untested and belongs to its separate acceptance scope; a standalone player must not be described as that UI. Stage one is not complete until its actual required audio, reset and cleanup evidence is recorded.

## Local verification and rollback

Run only `test_stage_one.py` under actual Python. Six focused tests cover formula/account scope, false/stale/type gates, real metadata shape/xattr rejection, raw scope/input binding, exact retained inventory payload compilation and the capture hook using the real inherited guard shape. Existing full suites are not rerun. No native operation/private input is used. Rollback: decline the new wrapper, preserve all candidates and partial native evidence; never delete/retry an uncertain run. MA restores temporary access after its approved session.

The allowlist repair reruns only four affected tests: two-release143+143 reproduction/repair, unexpected/missing/size/hash/manifest refusal, inventory payload compilation and the capture installation hook. See `repair-test-output.txt`. The six-test cost result above remains historical and is not counted as a new full-suite run. The prior exact candidate and all its bytes are preserved outside this tree under `../WI20261024-017-stage-one-history/80523a46ee7b3c0738443c00e866b2b21c901c8616bb97e7a56e826325726fa3/`.

The fixed-schema repair runs only three affected checks, recorded in `sql-context-test-output.txt`: the real retained source-stage/query/stdin/parser path with synthetic responses (1046 reproduction, then account admission and stop before dump), explicit schema contracts for the remaining SQL path, and capture-hook installation. Generic `SELECT 1` remains unchanged. Previous exact candidate846998 and its complete seven-member tree are preserved in the corresponding external history directory. Cost and earlier release-inventory suites are not rerun.
