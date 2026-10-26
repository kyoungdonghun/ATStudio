---
version: 1.0
last_updated: 2026-10-11
project: ATS
owner: SE
category: evidence-pack
status: confirmed
dependencies:
  - path: WI-20261011-ATS-039-handoff.md
    reason: Approved bounded repair and write scope
  - path: ../user/REQ-20261011-ATS-006.md
    reason: Explicit repair amendment and fresh single-attempt authority
  - path: WI-20261011-ATS-038-evidence-pack.md
    reason: Preserved native HOLD and service-shape observations
  - path: WI-20261011-ATS-036-evidence-pack.md
    reason: Unchanged read-only contract and public artifact identities
---

# Evidence Pack: WI-20261011-ATS-039

> Purpose: Hand off the isolated service-shape repair, exact recipe and local evidence to WI040.

**TL;DR:** Local implementation complete; Python95/95 and PowerShell8/8 PASS. Candidate frozen at2026-10-11 16:01:54 KST. Native NOT_RUN; WI040 final exact-byte review remains required.

## Scope and DoD

- [x] Fresh WI039 paths, units, upload, approval, claim, control, logs and releases; PG040 gate.
- [x] Normalize only optional empty Exec properties; preserve strict required scalar parsing and timestamp removal.
- [x] Separate exact masked baseline from loaded, bounded, pre-start MySQL checks.
- [x] Target-shaped positive/negative fixtures, stage failure and all inherited regressions.
- [x] Preserve WI03524 files before/after and18 additional existing public identity pins.
- [x] Freeze ten runtime files, all26 review files and all11 executable README blocks.
- [x] No remote, service, network, secret, database, rebuild, deletion, commit or push operation.
- [ ] WI040 final review and MA current native acceptance: outside this worker's completed local scope.

## Evidence Pointers

| Pointer | Evidence |
|---|---|
| [web_session.py](../../build/WI039-restricted-web-check/web_session.py), lines66,87,96,114,195,279 | Parser, strict stopped-state projection, masked baseline, prepare, loaded gate and restoration |
| [semantic-delta.patch](../../build/WI039-restricted-web-check/semantic-delta.patch) | Complete production semantic diff after normalizing only WI035 namespace |
| [test_service_shape.py](../../build/WI039-restricted-web-check/test_service_shape.py), lines80-217 | Ten focused tests including missing scalar subcases and namespace-only assertions |
| [test_runtime.py](../../build/WI039-restricted-web-check/test_runtime.py), line454 | Mocked run/finally stage failures, including post-unmask gate failure before MySQL start |
| [README.md](../../build/WI039-restricted-web-check/README.md), lines25,259,336,383,469 | Repair limits, S2 preflight, S3 one-use approval, S4 once-only dispatch, S7 postcheck |
| [local-receipt.json](../../build/WI039-restricted-web-check/local-receipt.json), [local-tests.log](../../build/WI039-restricted-web-check/local-tests.log) | Exact local commands, outcomes, compile checks and test history |
| [preservation-before.json](../../build/WI039-restricted-web-check/preservation-before.json), [preservation-after.json](../../build/WI039-restricted-web-check/preservation-after.json) |24 WI035 files unchanged;42 total after-check matches including JAR and prior diagnostics |
| [freeze.json](../../build/WI039-restricted-web-check/freeze.json), [recipe-pins.json](../../build/WI039-restricted-web-check/recipe-pins.json) | Every candidate file and executable README block identity |

## Cause and Narrow Repair

The preserved [WI038 native receipt](WI-20261011-ATS-038-native-receipt.json) reports HOLD/PREPARE/WEB_SERVICE, app NOT_RUN, cleanup PASS: exactly15 rows,6 PASS and9 NOT_APPLICABLE. This was before baseline creation or app/DB/web activation; it is not a successful restoration-cycle test.

MA observed the same missing properties with and without `--all`. The supervisor omitted ExecStartPre/ExecReload/ExecStop. Masked MySQL omitted those plus ExecStart and reported empty Type. All required scalar keys were present. WI035 required the absent keys and later required a loaded Type at the masked stage. Adding `--all` alone does not repair either condition.

Changes relative to WI035:

1. `Session.service` permits absent ExecStartPre/ExecReload/ExecStop only as empty. ExecStart may be absent only for mysql.service with masked UnitFileState and empty Type. All scalar keys remain required; unknown, malformed and duplicate properties reject. Nonmasked ExecStart must be nonempty. Existing execution-timestamp stripping is retained.
2. `mysql_stopped` strictly parses exactly five state fields. `mysql_baseline` requires masked/inactive/dead/PID0, exact /dev/null mask link, empty Type/four Exec values, no drop-ins and Restart=no. Prepare and final restoration use this masked snapshot.
3. Post-unmask `mysql_gate` requires loaded/inactive/dead/PID0 before start. Added explicit checks cover packaged FragmentPath, exact mysqld execution projection and trusted executable metadata. Prior loaded Type, overlay, runtime, start/stop, KillMode, SendSIGKILL, Restart and BindsTo predicates remain. Network/listener admission remains after MySQL start and before Java.
4. Other production runtime, policy, PC checker, units and public files are namespace-only or byte-identical, verified by test. The new test fixture and two existing mock adaptations cover the changed stage boundary without changing old test files.
5. README retargets namespace/pins/ports55139/55140, checks the retained failed WI035 supervisor without resetting it, and validates vendor public unit before approval/claim. It retains prior transport conditions, pins the QA identity helper and checks the Windows SSH executable using case-insensitive Windows path comparison.

**Measured versus inferred:** MA's2026-10-11 15:55:08KST pinned read-only observation confirmed root-owned /usr/lib/systemd/system/mysql.service, vendor Type=notify, exact ExecStart=/usr/sbin/mysqld and Restart=on-failure, plus the exact masked state/link. This worker made no native observation. The loaded systemctl ExecStart rendering in the regression fixture remains inferred, not measured. S2 rechecks the vendor evidence before consuming new authority; actual post-unmask mismatch must HOLD and clean up, never weaken or retry.

PG037's intermediate deactivating-status P2 and catalog ordering are deliberately unchanged. The earlier WI026 cleanup failure remains NOT_DETERMINED. Successful later history does not retroactively explain it.

## Frozen Identity

| Object | SHA256 |
|---|---|
| Ten-file runtime bundle | `fb643ff04e5c0f1e90f1b110eafa312c311a3d560a822769ff43f5ed432d379b` |
|26-file review set | `1a19015eb8e890b02d6973331dfb2ef7fd7458821694e929ba425cd1ab3c1572` |
| freeze.json | `e29624f41ccfd51f43005721d05e1e619bf38cac459f5281707c9dd9530f772e` |
| runtime.py | `07392a980ab6a029a73e27d0ce7e1c00d8c296964d47adc5344020029d57877f` |
| web_session.py | `d2aba516a7e9850ebfe18e5e48f1adcc22fc103845a1372e0e05f244ff8dc2cc` |
| README.md | `580e38a29533e0c4c366304d72b7fc725ff4bf0c25457e8c64d4ed2c03e85d51` |
| recipe-pins.json | `a8e2f7b5d164b7372e277117515a3a4000ca83f428badc5ecb93a13b3ad90aef` |
| Unchanged JAR,70306351 bytes | `01d5623972e17d0b6e1d3e532096239cd87e9995a08771c59292c94bf3699dcb` |
| Unchanged QA036 allowlist | `09df13ad175f4a359e6cbc9d9b68d0f6bf8e3c27eb065ca2e8e6de80e757bf12` |

Bundle algorithm: ordered UTF-8 filename, NUL, exact file bytes. Review set uses the same algorithm over freeze.files insertion order. Freeze and deliverables are excluded to avoid self-reference. Eleven command blocks L1/S1/S2/S3/S4/S5/L2/L3/S6/S7/L4 are individually hashed as UTF-8/LF with one final newline. Old WI035 freeze remains `66843a26f4681a11cd48e9bde461170a29d4bd807a186822495b112d6a160d1f`.

Source context: main at `58b1011dad796017e47dbcc82a4225ca449bcdf6`; this is not a clean-worktree or fresh full application identity claim. Only approved new candidate/report paths were written. Existing source/JAR/SPA identity belongs to QA036 and current MA recheck; no rebuild was performed.

## Test Evidence

`test` skill applied to the scoped Python unittest and PowerShell synthetic suites; Gradle/npm application suites were intentionally not invoked for this operator-only change.

```powershell
python -I -B -m unittest discover -s build/WI039-restricted-web-check -p 'test_*.py' -v
& ./build/WI039-restricted-web-check/management-check.ps1 -Mode SelfTest
```

| Check | Result |
|---|---|
| Python unittest |95 total,95 passed,0 failed,0 skipped;0.406s; exit0 |
| PowerShell SelfTest |8 synthetic assertions PASS; network NOT_RUN; exit0 |
| Compile |11 candidate Python files plus5 embedded recipe Python snippets:16 in-memory compile units PASS |
| PowerShell recipe parser |4 executable blocks parsed without errors |
| Native shell/systemd/nginx execution |NOT_RUN; shell blocks were not dispatched |
| Preservation |24 WI035 before/after matches;18 additional prior pins match, total42 |
| Coverage metrics |Not measured; test count is not a coverage percentage |

The ten new tests include both observed omission shapes, explicit-empty equivalence, every missing scalar, duplicate/unknown/malformed fields, conflicting masked state/Type/Exec/drop-ins/mask, loaded bounds and executable rejection, timestamp normalization and prepare without start/unmask. Existing tests retain all cleanup-step failures, simultaneous failures, command finalizer failures, sticky HOLD, BPF absence, HTTP lifetime/deadline, SIGHUP, path ancestry, method/IP/Host/SNI/auth/cookie fences, immutable contract and expiry/client-loss cases.

Development history is retained in the receipt: the initial focused red run had25 subcase errors across9 tests, including unavailable new helper methods; a later mock required WEB_SERVICE registration matching the real Session. A final ad-hoc compile extractor initially overmatched snippets; only that extractor was corrected. These are not native results. MA reports PG040 preliminary6/6 PASS; SE did not rerun or count those as independent acceptance. Final PG ownership includes post-unmask cleanup and exact recipe review.

## Limits, Rollback and Handoff

- Six GET paths and17 approved query combinations,143 manifest files, no auth/cookie forwarding or write probes remain unchanged. Browser/UI observations are separate from bounded HTTP assertions.
- Existing supervisor24min, app/DB25min, view23min, admission4min and independent stop limits remain below1800s under functioning systemd/kernel. No absolute cleanup guarantee under SIGKILL, frozen kernel or host loss.
- Actual Linux loaded rendering, BPF/network enforcement, timers, nginx configuration, TLS/browser and restoration are NOT_RUN for WI039. Local PASS grants no public release, populated media/login or load-test claim.
- Rollback before dispatch is withholding the new candidate. Preserve all old/new claims, partials and receipts; no deletion, reset-failed, repair, automatic retry or reuse of consumed WI035 authority.
- Any new target predicate mismatch or failed final cleanup stays HOLD. MA owns recovery decisions and current access; the worker neither opened SSH nor used secrets.
- **Immediate next WI: WI040 exact-byte independent review** of this frozen candidate, recipe and post-unmask failure cleanup. Only MA may execute after review and fresh live prerequisites. REQ006 is not closed by this Evidence Pack.

## Reference Documents

Injected context retained in order: STD-001 core-principles, STD-004 documentation-standards, STD-002 development-standards, STD-005 glossary; SE role; security/quality/access policies; approved handoff and dated work evidence. Skills used: test and create-wi-evidence-pack. No additional architecture or policy decision was introduced.

## Related Documents

- [WI039 handoff](WI-20261011-ATS-039-handoff.md): exact scope and WI040 dependency.
- [REQ006](../user/REQ-20261011-ATS-006.md): approved repair amendment.
- [User summary](../user/WI-20261011-ATS-039-summary.md): concise local completion.
- [WI038 evidence](WI-20261011-ATS-038-evidence-pack.md): preserved consumed attempt.
- [WI037 evidence](WI-20261011-ATS-037-evidence-pack.md): prior conditional review, not new approval.
- [WI036 evidence](WI-20261011-ATS-036-evidence-pack.md): unchanged contract and identity provenance.
