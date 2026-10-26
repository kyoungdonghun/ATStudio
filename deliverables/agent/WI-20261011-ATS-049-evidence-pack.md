---
version: 1.0
last_updated: 2026-10-11
project: ATS
owner: PG
category: evidence-pack
status: confirmed
dependencies:
  - path: WI-20261011-ATS-049-handoff.md
    reason: Approved independent review boundary
  - path: WI-20261011-ATS-048-evidence-pack.md
    reason: Owner completion and v2 identity
  - path: ../../build/WI048-quarantine-continuation/freeze-v2.json
    reason: Exact reviewed file set
---

# Evidence Pack: WI-20261011-ATS-049

> Purpose: Bind the narrow original-HTTPS continuation verdict to corrected v2 bytes and local evidence.

## Verdict and Scope

**TL;DR: CONDITIONAL_ALLOW for MA's fresh-gated v2 L1/L2, each once. PG native NOT_RUN. No remaining blocking finding.**

- [x] Only nginx.After order normalizes; token multiplicity, ordered Execs and every other field remain exact.
- [x] Exact consumed receipt/claim and both retained originals/durable copies required before new claim; no repeated detachment.
- [x] Original service/TLS checks precede the sole Nginx start; postchecks and failure/receipt safety stop preserved.
- [x] No app/MySQL start, unmask, SQL, secret environment read or prior-record mutation.
- [x] Frozen 14-file set, ordered bundle, two recipe bodies and seven selected prior inputs independently match.
- [x] Independent 9/9 and current owner 21/21 mocked tests independently rerun; actual v2 L1 local path/read/hash passes.

The verdict is permission-limited review evidence, not native success or blanket deployment approval. MA refreshes every target condition below.

## Finding and Delta

**PG049-F01, P2, v1 HOLD:** README.md:61 used the nonexistent local directory `build/WI048-quarantine-recovery/continue_https.py`. Exact L1 local-prefix execution failed before SSH. The [v1 RED receipt](../../build/WI049-continuation-review/v1-recipe-test.json) and [v1 review](../../build/WI049-continuation-review/v1-review-receipt.json) remain unchanged.

**Resolved only in v2:** README-v2.md:65 names the actual `build/WI048-quarantine-continuation/continue_https.py`. Executable L1 differs only by that path; L2 and the 8,634-byte candidate are unchanged. Other README differences are revision/reference prose. No alternative installer or operator-edited block is covered.

Code evidence in [continue_https.py](../../build/WI048-quarantine-continuation/continue_https.py):52 (After comparison), :73 (once-only best-effort safety stop), :107 (old evidence), :117 and :128 (original baseline before claim), :144 (postcheck), :155 (provisional durable receipt), :167 (root/path/argument/signal entry). Required metadata, held-byte imports and fixed safe output remain inherited from the pinned WI046 helper.

## Exact Identity

| Item | SHA256 |
| --- | --- |
| freeze-v2.json | `26c47507668074aea79c5f96fd5d0a99d7dd7d1c86259edb3898baf170240b2e` |
| Ordered 14-file bundle | `b3befcd2a2310ef8c7461800ce35f75f2aad918eb479de12f480433c55fcacfe` |
| continue_https.py | `2d617dc5a09b67a3024f628b90f6465bcac29bd8cce4ac852a0360d75484765a` |
| README-v2.md | `bbc38de19394e990262deccf1da0343b6be3934fc715a2613737b15d10aa9fc9` |
| recipe-pins-v2.json | `52542ba681fe4af2ebbb7a43da98fd6f8fab2fb6252aafee7034d3b055cb2809` |
| L1 | `c2f816ee642a05d1b10b8999cef5b5d0331e8fa3746f85ee5935bb2915bd83ba` |
| L2 | `52ab891a56f1ed6a5881ec42523877f457a2e69e3d8e484a8ddafe69c41ed3b0` |
| WI046 recover.py | `7ab8be7b2b1b94df4eeabbc8be5c03620d0ad219749ce571014d587a92ad1d94` |
| MA-reported old disk receipt | `965d25278854bc72c91c132058adc934de949c9d2d68fce4610148ae0990d8c2` |

The [final receipt](../../build/WI049-continuation-review/final-receipt.json) also lists all three imported WI042 source pins and independent test pins. The manifest includes the preserved v1 freeze/recipe/receipts; it excludes itself. Bundle input is ordered UTF-8 filename, NUL, exact file bytes. Recipe bodies use UTF-8/LF with one final newline, excluding fences. Seven selected old import/recipe/test inputs match [preservation.json](../../build/WI048-quarantine-continuation/preservation.json); no whole-repository/JAR preservation claim is made.

## Tests and Reproduction

PG, Windows local, 2026-10-11; test skill applied to this handoff's Python/PowerShell mocked scope. No Gradle/npm, live main, SSH or embedded native body executed.

| Command | Actual result |
| --- | --- |
| `python -I -B build/WI049-continuation-review/test_independent.py` | 9 PASS, 0 failure/error/skip, 0.111s, exit0, output bb62b9 |
| `python -I -B -m unittest discover -s build/WI048-quarantine-continuation -p 'test_*.py' -v` | 21 PASS, 0 failure/error/skip, 0.084s, exit0, output e842c2 |
| `& ./build/WI049-continuation-review/test_recipe_local.ps1 -Readme 'build/WI048-quarantine-continuation/README-v2.md'` | Exact local prefix PASS; 2 PowerShell ASTs PASS; exit0, output e2048a |
| `& ./build/WI049-continuation-review/verify_v2.ps1` | 14 files, ordered bundle, 7 prior inputs, README and 2 normalized recipe pins PASS; exit0, output 8a2b12 |

Independent tests exercise the actual continuation run with mocked effects: 11 comparison cases, missing/changed prior receipt/claim and both kinds of copies, overlay reappearance, claim collision/reuse, pre-start TLS and post-start service drift, ambiguous start, receipt write/fsync failure, failed stop without skipped remaining checks, and prohibited move/delete/old-run/environment/DB calls. Fixtures are synthetic. MA's seven-token equality and prior disk hash are target observations supplied to PG, not PG-native measurements.

The prefix test mocks only SSH command lookup and never invokes transport; file resolution/read/hash are real local operations. It is stronger than AST-only syntax checking but does not prove the real transport array. Owner's 23 inherited regression result remains owner evidence, not an additional 23 tests rerun by PG in this final delta pass. No coverage percentage or native durability claim.

## Mandatory MA Conditions

1. Use only exact README-v2 L1/L2, each once, with all pins rechecked. Use PowerShell7 from repository root. Bind the current strict host-pinned SSH argument array and verify System32 OpenSSH; no forwarding or substitute shell commands.
2. Freshly confirm expected host/boot, management-only network rules/peer and root recovery access. Confirm no competing operation or imminent renewal; preserve original TLS/certificate/provider rules.
3. Before L1 require fresh tools path; before L2 require fresh record/claim. Reconfirm Nginx stopped with no workers, app/MySQL and prior trial supervisors stopped/masked PID0, no outstanding jobs or app/probe listeners. Require exact old evidence, original overlay absence, retained/durable copy pins and root metadata.
4. Allow only original Nginx start. No app/DB start/unmask, secret environment/SQL access, repeated detachment, daemon-reload, repair, deletion or retry. Preserve all failure artifacts.
5. Success requires exit0 and bounded stdout `RECOVERED_ORIGINAL_HTTPS`, `receipt_durability=PASS`, `trial_status=HOLD`. Disk success bytes are intentionally provisional and cannot establish completion. Failure/collision/ambiguous output means HOLD, not fallback or another invocation.
6. MA independently checks final service/listener/TLS state and performs approved access cleanup; WI045 records the actual outcome. No browser/web-trial authority is added.

## Risks, Rollback and Traceability

Withhold before staging if any condition fails. After consumption, preserve records and inspect read-only; do not rerun or reattach old overlays. The 120s alarm and bounded cleanup are not guarantees against SIGKILL, host loss or indefinitely blocked kernel I/O. Mocked PASS does not prove native permissions, Linux durability, SSH/HTTPS reachability or browser functionality.

WI046 remains its consumed HOLD. WI042 remains HOLD/POST_STATE_CHANGED, cause NOT_DETERMINED; the proven After serialization mismatch does not explain that earlier trial. Old disk PENDING and old stdout durability PASS are different observations, not a reason to rewrite history.

Tier0 retained in order: STD-001 core-principles, STD-004 documentation-standards, STD-002 development-standards, STD-005 glossary. Tier1: PG persona and security-policy. Tier2: approved REQ006 amendment, WI048/049 handoffs, WI046/047 evidence, WI048 completion, test and create-wi-evidence-pack skills. Only WI049 review/report files written. No candidate changes, subdelegation, native work, git or memory writes. No running exec sessions at completion.

## Related Documents

- [WI049 Handoff](WI-20261011-ATS-049-handoff.md): Authority and acceptance criteria.
- [WI048 Evidence](WI-20261011-ATS-048-evidence-pack.md): Owner completion.
- [v2 Recipe](../../build/WI048-quarantine-continuation/README-v2.md): Sole byte-bound procedure.
- [Final Receipt](../../build/WI049-continuation-review/final-receipt.json): Machine-readable verdict and tests.
- [User Summary](../user/WI-20261011-ATS-049-summary.md): Concise decision.
