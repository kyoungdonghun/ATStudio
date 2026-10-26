---
version: 1.0
last_updated: 2026-10-11
project: ATS
owner: SE
category: evidence-pack
status: confirmed
dependencies:
  - path: WI-20261011-ATS-042-handoff.md
    reason: Approved local-only second repair scope and WI043 dependency
  - path: ../user/REQ-20261011-ATS-006.md
    reason: Requirement context; current local-only authorization is recorded in the handoff
  - path: WI-20261011-ATS-039-evidence-pack.md
    reason: Immutable source candidate and inherited tests
  - path: WI-20261011-ATS-040-evidence-pack.md
    reason: Preserved prior independent review, not approval for WI042
---

# Evidence Pack: WI-20261011-ATS-042

> Purpose: Deliver the frozen ordered-Exec parser repair and pre-claim check preparation for independent local review.

**TL;DR:** LOCAL READY ONLY. Python106/106 and PowerShell8/8 PASS. Frozen2026-10-11 16:35:35KST. Native NOT_RUN and NOT_AUTHORIZED; WI043 review is next.

## Scope and DoD

- [x] Exec commands remain ordered lists; no overwriting, sorting, ambiguous joining or deduplication.
- [x] Required scalar and genuine JSON duplicates remain rejected; optional omissions and strict MySQL stage predicates retained.
- [x] Supervisor, Nginx prepare, masked/loaded MySQL and restoration consumers adapted to JSON-stable lists.
- [x] Repo-derived two-reload-command fixtures, full prepare/restore parsing, JSON roundtrip and failure regressions.
- [x] Prepared S2 calls the same read-only Nginx/MySQL validation before approval/claim without prepare or writes.
- [x] Fresh WI042 identifiers/claim/ports; consumed WI035/WI039 supervisors and claims are observed, never reset or reused.
- [x] Frozen runtime, recipe and local evidence;79 prior identities unchanged.
- [x] No remote/SSH/agent/browser/network/service/DB/secret access, app/frontend changes, rebuild, deletion, commit, push or memory update.
- [ ] WI043 exact-byte final review. Any future native execution separately requires new explicit user approval and fresh MA prerequisites.

## Evidence Pointers

| File / location | Evidence |
|---|---|
| [web_session.py](../../build/WI042-restricted-web-check/web_session.py), line67 | Strict scalar map plus ordered Exec lists |
| [web_session.py](../../build/WI042-restricted-web-check/web_session.py), lines103,112,130,206,291 | Masked MySQL, read-only Nginx baseline, prepare, exact single-command loaded MySQL and restoration |
| [semantic-delta.patch](../../build/WI042-restricted-web-check/semantic-delta.patch) | Complete production semantic diff versus namespace-normalized WI039 |
| [test_exec_records.py](../../build/WI042-restricted-web-check/test_exec_records.py), lines65,104,113,176,188 | Record retention, no-write baseline, full prepare/restore, single-command MySQL and S2 AST checks |
| [test_service_shape.py](../../build/WI042-restricted-web-check/test_service_shape.py) | Inherited omission, missing scalar, malformed/unknown, mask, loaded-bound and namespace-only tests |
| [README.md](../../build/WI042-restricted-web-check/README.md), line267 | Prepared S2 pre-claim full service-parser path |
| [local-receipt.json](../../build/WI042-restricted-web-check/local-receipt.json), [local-tests.log](../../build/WI042-restricted-web-check/local-tests.log) | Actual local commands, results and development failure history |
| [preservation-before.json](../../build/WI042-restricted-web-check/preservation-before.json), [preservation-after.json](../../build/WI042-restricted-web-check/preservation-after.json) |79 exact identities before/after, including WI035/039/040 and earlier registered pins |
| [freeze.json](../../build/WI042-restricted-web-check/freeze.json), [recipe-pins.json](../../build/WI042-restricted-web-check/recipe-pins.json) |27 review files,10 runtime files and11 executable blocks |

## Cause and Change

MA supplied this native history: WI039 S2 passed; S4 dispatched once at16:15:33; final HOLD/PREPARE/DUPLICATE_JSON, app NOT_RUN, cleanup PASS with15 rows:6 PASS/9 NOT_APPLICABLE. Baseline, release, MySQL overlay and app unit were absent. Nginx emitted two ExecReload property records; supervisor/MySQL omitted optional Exec fields without duplicates; contract JSON had no duplicate keys. These are supplied observations, not new worker target access. DocOps WI041 owns the native record.

[Repository nginx-service.conf](../../deploy/single-vps/tls-quarantine/nginx-service.conf), lines8-10, resets ExecReload and defines two commands: validate the quarantine configuration, then reload it. The prior generic unique() call incorrectly treated these repeated command-property records as duplicate JSON. Prior local and independent tests missed this legitimate shape.

The narrow production change is only in web_session.py:

1. Scalar properties still pass through unique() and strict required-field checks. Each Exec property instead accumulates records in arrival order, including identical commands. Command strings remain separate list elements; the parser does not sort, join or deduplicate them.
2. An omitted optional property or single empty property becomes an empty list. Repeated empties or mixed empty/nonempty records HOLD. Nonmasked ExecStart still requires commands. The existing execution-timestamp removal is applied independently to each command.
3. Masked MySQL requires four empty Exec lists plus the prior exact masked/inactive/dead/PID0/link/Type/drop-in/Restart predicates. Loaded MySQL requires exactly one command matching the unchanged mysqld expression and all earlier executable, state, timeout and containment gates.
4. Read-only nginx_baseline() factors the original Type/drop-in/quarantine-path validation out of prepare. It requires nonempty ExecStart and ExecReload lists and validates the quarantine path in every command. S2 calls this and mysql_baseline(); prepare uses the same methods. S2 never invokes prepare or creates baseline state.
5. Baseline JSON persists lists directly. Restoration and S7 compare exact ordered list contents; no tuple/list mismatch. Supervisor parsing uses the same representation. Its scalar bounds remain unchanged.

runtime.py, web_policy.py, the PC checker and units are namespace-only; contract.json/public.env/env-shape.json are byte-identical. Inherited tests were adapted only for list assertions/fixture encoding and comparison against WI039. New README identifiers/PG043 pins, TCP55142/UDP55143 and both retained historical supervisor/claim checks are explicit. No P2 intermediate-status, catalog, UI, CSP, route, secret, timeout or cleanup redesign.

## Fixture and Preflight Boundary

The two Nginx command strings are **derived from the committed configuration and the MA-reported native record count**, not fabricated raw output capture. Other service scalar/omission shapes retain prior sanitized observations; command wrappers and loaded output remain synthetic fixtures. This distinction is in the test module and recipe.

The full prepare/restore regression leaves production service parsing, JSON state load, cleanup-step execution and final comparisons active. OS/process/filesystem/service effects are mocked; writes are in-memory. Order or content changes produce final HOLD/cleanup FAILED and do not start baseline Nginx; remaining checks continue.

S2 checks the actual existing Nginx and masked MySQL via production methods before S3/claim. An AST regression verifies these exact calls and rejects prepare, run, overlay, exclusive or direct ctl calls in S2. No future target compatibility is inferred from this local check. The earlier public vendor-unit observation does not prove current loaded output.

## Frozen Identity

| Object | SHA256 |
|---|---|
|10-file runtime bundle | `78ce821757111b6f4bd440a79680ed12e4e40b6592616293e4d768ddd6dbe00c` |
|27-file review set | `9019f8d94c0346659bf6f18b8fb1cae7c06550fa5fd19371072a934e443ca316` |
| freeze.json | `b92cbe9cf2b24da99953b9f6c496028205a1a5daab754a915dce30690537f6bc` |
| runtime.py | `ad3e8a20ddb17fd1e3e4878ac2096bfa74a63561b9b1e8572b6bc2a7cda7a7b6` |
| web_session.py | `3092ae8c5598d9b97e1dedc6e0bb74ca9c0d3a8989493465114229d45727257a` |
| README.md | `4d4c9111718845698cc188d21830bfec31b8e6d5764a7b3f9e6a9f317b42bc23` |
| recipe-pins.json | `7000c23f0d353e5e02f92a7d07ec63c215b9d54a513a5b6283d1e472e52d0d21` |
| Unchanged WI039 freeze | `e29624f41ccfd51f43005721d05e1e619bf38cac459f5281707c9dd9530f772e` |
| Unchanged JAR,70306351 bytes | `01d5623972e17d0b6e1d3e532096239cd87e9995a08771c59292c94bf3699dcb` |
| Unchanged QA036 allowlist | `09df13ad175f4a359e6cbc9d9b68d0f6bf8e3c27eb065ca2e8e6de80e757bf12` |

Bundle algorithm is ordered UTF-8 filename, NUL, exact bytes. Review set uses files-object insertion order; freeze and deliverables are excluded to avoid self-reference. The11 executable README blocks have separate UTF-8/LF hashes with one final newline. Freeze does not confer execution authority. All79 exact prior paths/hashes, including WI040 review sources/evidence and repository Nginx configuration, are listed in preservation-after.json.

## Test Evidence

The test skill was applied to the operator-scoped unittest and synthetic PowerShell suites. No Gradle/npm rebuild or application suite was invoked.

```powershell
python -I -B -m unittest discover -s build/WI042-restricted-web-check -p 'test_*.py' -v
& ./build/WI042-restricted-web-check/management-check.ps1 -Mode SelfTest
```

| Check | Result |
|---|---|
| Python |106 total:95 inherited +11 added;106 passed,0 failed,0 skipped;0.594s; exit0 |
| PowerShell |8 assertions PASS; network NOT_RUN; exit0 |
| Python compilation |12 candidate files +5 embedded recipe snippets =17 in-memory compile units PASS |
| PowerShell recipe syntax |4 blocks parsed PASS;7 shell blocks never dispatched |
| Preservation |79/79 exact matches |
| Coverage |Not measured; no percentage claim |
| Native / browser / nginx / systemd |NOT_RUN, NOT_AUTHORIZED |

Initial red run:9 focused tests,1 PASS/8 errors, including7 DUPLICATE_JSON reproductions and1 missing new helper. First full run after repair:104 tests with3 fixture failures because a global boot read_text mock shadowed the expected fixture-source read. The expected command list is now captured before that mock. This correction did not change production code. Final106/106 results include the loaded single-command and S2 regressions. Tool-output truncation/store recovery had no native effects; local-receipt records it.

## Risks, Rollback and Next Gate

- Known P2 intermediate status behavior remains unchanged. An intermediate deactivating HOLD is not a final receipt; no final failure may be reinterpreted as success.
- Maximum30min design, independent app/DB caps, supervisor cleanup limit, on-host IPv4/BPF proof, safe diagnostics, no retries and all endpoint fences remain. SIGKILL/host loss still limit restoration guarantees.
- Neither ordered parsing nor local mocks establish successful real startup, restoration, browser access, populated data, public exposure or external network denial.
- Historical WI035/WI039 consumed failures are preserved. The unrelated earlier WI026 cleanup cause remains undetermined.
- Rollback is withholding WI042, not editing/deleting evidence or reusing claims. No server operation is currently authorized, including the prepared S2.
- **Immediate next gate: WI043 independent local exact-byte review.** MA owns that handoff and any later request for native authority; REQ006 remains incomplete.

## References and Skills

Retained injected context: STD-001 core-principles, STD-004 documentation-standards, STD-002 development-standards, STD-005 glossary; SE role; security/quality/access-control policies; WI042 handoff and stated Tier2 pointers. The existing skill-generated handoff was read and honored without overwriting it. Skills applied: create-wi-handoff-packet consumption, test, create-wi-evidence-pack. DocOps owns REQ/index integration.

## Related Documents

- [WI042 handoff](WI-20261011-ATS-042-handoff.md): current local-only authority.
- [REQ006](../user/REQ-20261011-ATS-006.md): requirement and amendment history.
- [User summary](../user/WI-20261011-ATS-042-summary.md): concise outcome.
- [WI039 evidence](WI-20261011-ATS-039-evidence-pack.md): immutable source candidate.
- [WI040 evidence](WI-20261011-ATS-040-evidence-pack.md): prior review, not new approval.
- [WI041 handoff](WI-20261011-ATS-041-handoff.md): separate native-history integration.
