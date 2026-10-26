---
version: 1.2.0
last_updated: 2026-10-24
project: ATS
owner: se
category: evidence-pack
status: sql-context-repair-reviewed-native-outcome-pending
---
# Evidence Pack: WI-20261024-ATS-017

## Current fixed-schema repair checkpoint

MA's [capture result](../../build/WI20261024-stage-one-native/capture-result.json) and [safe diagnostics](../../build/WI20261024-stage-one-native/capture-diagnostics.json) record `SOURCE_SQL_WINDOW` HOLD, SQL1046, snapshot `NOT_ATTEMPTED`, no payload files, and STOP/PID_ZERO/SOCKET_GONE/COLD_BASELINE all PASS. Existing repository config/snapshot reads and MySQL source start/containment checks succeeded before the account SELECT failed. These facts do not establish a successful capture; the failed attempt remains preserved.

The [current candidate](../../build/WI20261024-017-stage-one/candidate.json) is `18d4b2208da305acceaafe0f8b2eac438f8ae11f1f96f372b14b10a9a88e2221`, eight members; canonical hash-map bundle `e4771cbc008f52817adbc58a6550483aa1e209a1444bb5caeb458f264661ab2b`. The only new execution change is an eight-line fixed-schema wrapper plus one capture-only installation call: `accounts.queries(day)` gains ``USE `atstudio_prod`;`` before its byte-identical read-only transaction. The generic mysql query/health checks are unchanged, so isolated pre-import checks do not require a database before it is created. The original WI011 tree remains immutable.

[Focused SQL-context output](../../build/WI20261024-017-stage-one/sql-context-test-output.txt): three affected checks PASS. The actual retained stages, query-file/stdin protocol and account parser run against synthetic responses, reproduce missing-schema refusal, then pass account admission and stop before any dump. Fixed-date inputs avoid clock dependence; invalid schema and generic SELECT1 behavior are checked. The remaining protocol review confirms information_schema table/column queries, schema-qualified row counts, `mysqldump --databases atstudio_prod` and isolated import of the complete database dump. No other occurrence of this context omission was found; native dump/restore compatibility remains unproven. Cost and prior release-inventory suites were not repeated.

PG additive exact review PASS: [SQL context repair receipt](../../build/WI20261024-018-stage-one-review/sql-context-repair-review-receipt.json), SHA-256 `b5b73ba2fa73d8713d10e6891abdeab1d9bf1a594ed82ddcbd80446f0e38fc09`. Two independent checks confirm the minimal AST delta, unchanged inventory/cost code, actual payload build/compile, query-file/stdin prefix, original transaction/ROLLBACK and health-query behavior, plus schema/day rejection. No old suite was rerun. Preserve the prior [846998 candidate and complete tree](../../build/WI20261024-017-stage-one-history/84699881827f8ef9ba8493b326598db595ae0bacb3332f61053e9a9a2567a1e0/candidate.json), all failed attempts and independent receipts. No automatic native retry is authorized by this repair. MA must bind a fresh one-use run/decision/scope to this exact candidate, maintain actual cost/authority validity and obtain its bounded resume decision. SE performed no server, DB, cloud or private-input operation.

## Inventory repair checkpoint (historical)

The initial local review below is retained as historical evidence. MA subsequently reported a pre-stage clock/authority HOLD and, after an explicitly approved bounded retry, `NATIVE_AUTHORITY_OR_DISPATCH_HOLD` with snapshot `NOT_ATTEMPTED`. Read-only diagnosis located `UNREVIEWED_SOURCE_FILE`: the retained adapter traversed the whole current release but omitted its143 frontend files from the allowlist. No DB/cloud/capture operation was reported for either inventory attempt. SE accessed no server or private input.

The inventory-repaired [candidate](../../build/WI20261024-017-stage-one-history/84699881827f8ef9ba8493b326598db595ae0bacb3332f61053e9a9a2567a1e0/candidate.json) is `84699881827f8ef9ba8493b326598db595ae0bacb3332f61053e9a9a2567a1e0`, seven members; canonical filename/hash-map bundle `6560b48fc4a43479c38e783fd47860f7b3432ab798b76dee4721ae1348c659ab`. `cost_metadata.install_release_inventory` adds only `r.RELEASE/frontend` entries from the frozen runtime's pinned `binding/artifact.json`; the original roots, old frontend, JAR/runtime members and complete-tree collector remain. The overlay is installed in inventory and capture memory, without changing WI011. The source manifest is the retained v8r1 `spa_files` map, not hashes learned from the target.

[Repair test output](../../build/WI20261024-017-stage-one/repair-test-output.txt): four affected checks PASS. The actual retained collector reproduces the original failure on a synthetic current143/old143 tree, then accepts the repaired exact tree and rejects extra/missing files, size/hash mismatches and manifest-pin changes. Inventory payload compilation and the capture installation hook also pass. Cost arithmetic is unchanged and its old suite was not repeated. Exact tree verification: repaired7, unchanged WI01119 and [preserved initial6](../../build/WI20261024-017-stage-one-history/80523a46ee7b3c0738443c00e866b2b21c901c8616bb97e7a56e826325726fa3/candidate.json) PASS; no bytecode was emitted.

PG additive exact review PASS: [inventory repair receipt](../../build/WI20261024-018-stage-one-review/inventory-repair-review-receipt.json), SHA-256 `ffcb0b809accf3f65b7a213129849f0ce5e988349cb800214b78e2808da32755`. Three independent focused checks cover actual collector refusal/acceptance, frozen provenance and exact builder compilation/common installation for both modes. PG reused only the synthetic filesystem fixture, without rerunning the SE suite or old PG suite. Native remains on HOLD until MA's bounded resume authorization. Resume requires new immutable run/records bound to the repaired candidate, actual server epoch >=issued_at and valid remaining expiry; failed runs are never replayed. Fresh inventory success and actual cost evidence are still required before capture. Local repair does not establish native success or stage-one completion.

## Initial local checkpoint (historical)

## Summary

Prepared the minimal current-cost metadata/calculation wrapper around frozen WI011, with an actionable actual-backup-to-frozen-audio recipe. Reset execution was separated to WI020; no server/provider/private input was accessed by SE.

## Scope / DoD Check

- [x] Reuse unchanged WI011 capture/isolated restore, source/JAR and WI005 audio contracts; no new operational framework.
- [x] Measure bounded metadata before interpreting current free storage; reject unsupported xattr/ACL rather than inventing missing bounds.
- [x] Account for SQL payload, encrypted/compressed data/tree/index/lock/snapshot overhead, allocation and all account storage without deduplication/deletion savings.
- [x] Bind real MA raw decision/scope/cost-input/source evidence; no cost or user approval is issued by the tool.
- [x] Preserve original cold/fixture/grant/source checks, one-use paths, uncertain outcomes and independent cleanup.
- [x] Provide exact inventory/capture records and audio evidence linkage; keep real human playback/seek and reset outcomes distinct.
- [x] SE6 focused synthetic tests PASS; no historical/product suite rerun.
- [x] PG WI018 exact independent review: four focused checks PASS; no local blockers.
- [ ] Actual stage-one completion: MA-owned backup/audio/reset/human outcomes remain unobserved by this WI.

## Reference Documents (Tier 0-2)

MA injected the governing context through [WI017 handoff](WI-20261024-ATS-017-handoff.md).

| Tier | References | Purpose |
|---|---|---|
| 0 | `docs/standards/core-principles.md`, `development-standards.md`, `documentation-standards.md`, `glossary.md` | Current approved scope, privacy, minimal tracked changes and language |
| 1 | `docs/policies/security-policy.md` | Sensitive input/evidence boundaries |
| 2 | REQ-20261011-ATS-007, `docs/index.md`, WI016 evidence | Actual current authorization and prior read-only observations |
| 2 | WI011/012 backup, WI005/006 audio, WI20261021-009 evidence | Frozen mechanics and historical reset-mail-only limit |
| 2 | `EmailService`, reset DTO/UI, frozen audio native adapter | Exact reset effects and absence of browser-player admission |

Evidence format follows `.agents/skills/create-wi-evidence-pack/SKILL.md`; injected rule source is `.claude/config/context-injection-rules.json`.

## Exact Evidence Pointers

- [Initial candidate](../../build/WI20261024-017-stage-one-history/80523a46ee7b3c0738443c00e866b2b21c901c8616bb97e7a56e826325726fa3/candidate.json): SHA-256 `80523a46ee7b3c0738443c00e866b2b21c901c8616bb97e7a56e826325726fa3`, six members.
- Bundle `814a3292edfdd3135ceee67c3268c100a167818329432529291d92cb9e8f9274`: SHA-256 of the compact sorted JSON filename/hash map.
- [stage_one.py](../../build/WI20261024-017-stage-one/stage_one.py): original serializer/authority/tree reuse, raw scope and cost-input binding, in-memory source-check extension; no new provider command.
- [cost_metadata.py](../../build/WI20261024-017-stage-one/cost_metadata.py): measured source/member/ancestor/manifest bounds and xattr absence checks; no secret metadata output.
- [cost_bound.py](../../build/WI20261024-017-stage-one/cost_bound.py): reproducible conservative bound and fresh external provider facts; `authorization=NOT_ISSUED`.
- [README](../../build/WI20261024-017-stage-one/README.md): source-backed arithmetic, exact MA commands/schema, actual backup provenance to audio, reset and stage-five UX boundary.
- [Focused tests](../../build/WI20261024-017-stage-one/test_stage_one.py), [test output](../../build/WI20261024-017-stage-one/test-output.txt).
- [Earlier d6fd manifest](../../build/WI20261024-017-stage-one-history/candidate-d6fd3ca9b2b5172e6d3da6f69227a3948d310406211e2de9e1621505a331ed11.json) preserved externally. Final80523 change was README only: align WI020's no-new-password-storage/stale-marker contract and distinguish stage-one MP3/Range from stage-five SPA UX. Other five member hashes are unchanged; no suite rerun for the documentation correction.

MA independently reported exact final WI017 six-member and prior WI011 nineteen-member checks PASS. The unchanged backup manifest remains `158d2bb3064d112e625b7b8db914925636fe9b72f68161671da862d84dc19839`; frozen audio remains `cd32ca3fb64e84b324d95c1172041395d6413c7648be4995cec198835489ff92`. No product/JAR or prior tool bytes were edited.

## Commands & Results

Local Windows interpreter: `%LOCALAPPDATA%/Python/pythoncore-3.14-64/python.exe`.

```text
<actual-python> -B -m unittest discover -s build/WI20261024-017-stage-one -p test_stage_one.py -v
```

SE6 PASS: formula/account-wide storage; false/stale/type/format gates; measured metadata and xattr rejection; raw scope/cost-input binding; original inventory payload plus extension compilation; native-shaped capture hook using the real inherited guard and rejection before original stages on metadata drift. All records are synthetic; tests never read real private files, launch services, call a provider or execute native backup/audio/reset. Original suites were not repeated.

PG WI018: four independent checks PASS plus actual inventory/capture builder compilation in synthetic memory and raw cost-input substitution rejection. [Exact cost review receipt](../../build/WI20261024-018-stage-one-review/cost-review-receipt.json), SHA-256 `12e3d7c8900b0af9dd4a0fddb14a0afbb2a1ae38cb5ec50aac169b6249085d6a`, verdict `ALLOW_EXACT_CURRENT_APPROVED_INVENTORY_AND_GATED_CAPTURE`. All six final member hashes match. This is permission to use the exact current approved flow with its actual gates, not evidence of current cost0 or recovery success. Audio/reset retain separate exact allowance and actual-outcome requirements.

The actual inherited `ExistingCapture.guard` exists and delegates to the retained `Capture.guard`; that relationship was exercised with injected observations. Native dispatch still awaits exact review and real evidence. Source identity is retained from the existing reviewed candidates, not claimed as a new full source643/JAR test.

MA read official restic0.19.1 and klauspost/compress1.18.6 sources on2026-10-24 and supplied the relevant facts; the README links the exact primary sources. SE did not browse. The data bound includes zstd expansion because restic does not fall back to raw when compression expands. Index non-final/new-pack behavior and five-minute lock refresh are explicitly accounted for. Current fees/allowances must still be re-observed by MA; the old provider receipt is not relabeled a capture-cost PASS.

## Limits / Rollback

Native state remains NOT_RUN for this implementation. The calculator is an evidence worksheet, not a provider billing cap; the additional paid budget is0KRW. All account storage and concurrent growth must be covered by actual evidence. Metadata must match again before the existing cloud stage and at source checkpoints. Unknown facts or overflow HOLD; no metadata stripping, deletion, retry or grant extension follows.

Backup acceptance requires actual current full-file/SQL equivalence and independent cleanup. Only that actual run can support the audio backup receipt. The current user decision authorizes the work, but it is never replaced by a fabricated code-generated approval.

The frozen audio admission is supervisor loopback only (`audio_native.open_admission`), with no public listener/nginx overlay. Stage-one API Range plus human playback/seek on the verified MP3 must be reported exactly; SPA/web-player UX remains stage-five scope. Reset execution is WI020: buyer3 after audio, user direct new-password entry, actual mail/link/consume/new-login/ME/reuse rejection, no new password retained and old-credential stale marker. ID1 remains outside reset changes. Old access JWTs are not proven immediately invalidated.

Rollback means declining this wrapper and preserving all originals/partial evidence. Actual uncertain snapshot or audio outcomes prohibit automatic replay. Original independent cleanup and MA access restoration remain mandatory, including after expiry.

## Follow-up

WI017 blocked WI018; its exact cost/inventory/capture review is complete and was immediately sent to MA. MA owns actual inventory/cost/backup/audio and the separately delegated WI020 reset chain. Completion of this local packet is not completion of stage one.
