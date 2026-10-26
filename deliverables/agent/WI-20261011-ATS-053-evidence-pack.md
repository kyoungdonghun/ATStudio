---
version: 1.0
last_updated: 2026-10-11
project: ATS
owner: PG
category: evidence-pack
status: active
dependencies:
  - path: WI-20261011-ATS-053-handoff.md
    reason: Authorized independent local diagnostic review
  - path: WI-20261011-ATS-052-evidence-pack.md
    reason: Reviewed v1 candidate and owner evidence
  - path: ../../build/WI053-state-diagnostics-review/review-manifest-v1.json
    reason: Exact reviewed inputs and independent test identities
---

# Evidence Pack: WI-20261011-ATS-053

> Purpose: Return two bounded v1 projection findings for SE repair, preserving independent evidence.

## Findings First

**TL;DR: HOLD for local diagnostic readiness. Review checkpoint complete; WI053 remains open for pinned v2 re-review. NOT_NATIVE_READY.**

### PG053-F1 [P2]: A primary error can retain BOUNDED_PASS

- Location: [runtime.py](../../build/WI052-state-diagnostics/runtime.py):224-235, reached from [web_session.py](../../build/WI052-state-diagnostics/web_session.py):273 and393.
- Reproduction: take the real successful mocked run result, retain its COMPLETE/all-PASS diagnostics and FINISHED view, add `code=POST_STATE_CHANGED`, then call actual `record_result -> attempt.json -> Session.restore -> result.json -> public_result`.
- Actual: public `status=BOUNDED_PASS`, `cleanup=PASS`, and `code=POST_STATE_CHANGED` coexist. Expected: HOLD, retaining the safe primary code. Independent regression: [test_pg_diagnostics.py](../../build/WI053-state-diagnostics-review/test_pg_diagnostics.py):399, failing assertion409.
- Cause: `state_result` only downgrades for incomplete/non-PASS diagnostic rows; it does not reconcile an existing primary error with the enclosing status. The original WI042 projection/restoration also accepts this contradiction, independently reproduced by the same test. This is an inherited validation gap, not a newly weakened policy/BPF/DB comparator or evidence that an ordinary run emits the contradictory receipt.
- Disposition: MA explicitly confirmed this consistency case is actionable within approved diagnostic scope. Fail closed at the bounded final projection while preserving existing safe primary/cleanup categories. Do not repair unrelated restoration behavior.

### PG053-F2 [P2]: NOT_RUN rows can precede the sole observed marker

- Location: [runtime.py](../../build/WI052-state-diagnostics/runtime.py):210-218, especially213-215.
- Reproduction: use all15 unique planned rows with contiguous sequence/increasing time; mark14 NOT_RUN/SKIPPED, place INSTALL_RELOAD/BEFORE/PASS last, and claim INCOMPLETE. Persist and restore through the actual session methods.
- Actual: the malformed chronology survives as INCOMPLETE. Expected: INVALID with HOLD and the existing `COMMAND_TIMEOUT` retained. Independent regression: [test_pg_diagnostics.py](../../build/WI053-state-diagnostics-review/test_pg_diagnostics.py):384, failing assertion397.
- Cause: removing NOT_RUN rows before checking the observed prefix loses their positions. With one observed row, the preceding-PASS slice is empty. The validator therefore permits skip finalization before the first observed event. This case remains HOLD; it is misleading accepted chronology, not a demonstrated security-gate bypass.
- Repair boundary: validate the full row order against STATE_PLAN, with only a contiguous observed prefix followed by NOT_RUN rows; retain current type/time/enum bounds and first-failure behavior.

## BPF Edge Disposition

**No finding for the proposed multiplicity redistribution.** [runtime.py](../../build/WI052-state-diagnostics/runtime.py):165 already compares individual tuple multiplicities with `Counter(before)[row] != Counter(after)[row]`. The actual `attachment_snapshot` parser accepts duplicate direction/ID pairs and preserves them in its sorted tuple.

[Independent test](../../build/WI053-state-diagnostics-review/test_pg_diagnostics.py):262 feeds mocked bpftool JSON for egress11 twice plus egress12 once versus egress11 once plus egress12 twice, with ingress13 unchanged. Real run/record/restore/public projection returns BPF DIFFERENT with only MULTIPLICITY_CHANGED, POST_STATE_CHANGED/HOLD, and skips the last DB read. An unchanged/reordered control passes. No raw IDs are included in diagnostic details. These are synthetic identities, not native observations.

## Scope / DoD Check

- [x] Independent harness, no SE fixture/assertion reuse; actual candidate run, view, record_result, restore and public_result executed under native mocks.
- [x]30 differential failure subcases compare actual original/candidate read traces and primary codes: policy/BPF/DB errors, unexpected exceptions, mismatches and skipped calls; original baseline/short-circuit behavior retained.
- [x] Reload markers, same-rule/new-ID HOLD, deny-order HOLD, fixed labels, hostile fields/types/size/time, missing diagnostics, primary preservation under recording/projection/write/cleanup failure and constructor-clock failure covered.
- [x] Both CLI functions return LOCAL_ONLY before side effects with Linux/root guards spoofed; no actual native entrypoint invocation.
- [x] Qualified ASTs outside explicitly changed functions equal the originals. Candidate README retains LOCAL_ONLY/NOT_NATIVE_READY and no new remote recipe.
- [ ] Invalid full-row ordering and enclosing status/error combinations rejected: blocked by F1/F2.
- [ ] Independent owner/full-original rerun: not completed; enhanced runner stopped during import, before candidate test execution. SE82/106 remain owner-reported only.

## Commands And Test Evidence

Verified2026-10-11 by independent PG, local Windows/Python3.14.3; branch main, HEAD `58b1011dad796017e47dbcc82a4225ca449bcdf6`. Existing unrelated dirty files were preserved.

```powershell
python -B -m unittest discover -s build/WI053-state-diagnostics-review -p 'test_pg_diagnostics.py' -v
```

Final [log](../../build/WI053-state-diagnostics-review/independent-final-v1.log): **15 test methods,13 PASS,2 FAIL,0 errors/skips,1.046s**, exit1. Parameterized subcases are not separate method counts. Native/file reads and writes inside scenarios are mocked, including attempt/result JSON bytes stored only in an in-memory mapping; this proves persistence logic, not disk durability or native restoration.

Earlier [14-test run](../../build/WI053-state-diagnostics-review/independent-tests.log):12 PASS/2 FAIL,0.928s. A separate experimental audit runner rejected import-time file access: [independent](../../build/WI053-state-diagnostics-review/independent-guarded-tests.log)1 loader error, [candidate](../../build/WI053-state-diagnostics-review/owner-candidate-rerun.log)4, [original](../../build/WI053-state-diagnostics-review/original-rerun.log)9. These are runner setup errors, not candidate regressions or successful suite reruns. The audit runner is retained as unsuccessful evidence, not recommended for v2 reproduction. No further broad audit was performed after MA's return-for-repair instruction. Coverage percentages were not measured.

## Identity And Preservation

[Review manifest](../../build/WI053-state-diagnostics-review/review-manifest-v1.json) records exact SHA-256/size for all43 reviewed input files and all7 review test/log files. Before/after arrays match exactly: WI05214 files, original WI04228 files, and the redacted WI045 receipt. No candidate or original bytes were changed by PG.

| Candidate v1 file | SHA-256 |
| --- | --- |
| runtime.py | `72eb5cea07ff344615adb038d7f815e67b6cd2605bfc8b16741cafe7355fd05e` |
| web_session.py | `20a18de83f64356168ba73c06f138a47bc1f0b1541fc3d424360db7f83339f56` |
| freeze.json | `b72f029cfc66343eb6bf4dde372a0eb7e4cb269295812efff33321a5f0afbefa` |
| Independent test_pg_diagnostics.py | `294cee532425c4683a45052c4e2e7d7598196bb9343213d66ba908e6d9d89717` |

Original runtime `ad3e8a20ddb17fd1e3e4878ac2096bfa74a63561b9b1e8572b6bc2a7cda7a7b6`, session `3092ae8c5598d9b97e1dedc6e0bb74ca9c0d3a8989493465114229d45727257a`, freeze `b92cbe9cf2b24da99953b9f6c496028205a1a5daab754a915dce30690537f6bc`, and native receipt `aad6797c5f1bfc0ad4a1e8f64df9254359fe2023e39310cbd077f948df3c3f87` match the supplied pins. This verifies local preservation, not current target identity.

## Risks / Rollback / Next Step

Return to MA now for **bounded SE052 v2 repair -> resumed independent PG053 -> WI054 integration**. Preserve this v1 manifest/logs and record the later disposition additively. Do not mark WI053 or WI054 ready before re-review. Rollback is non-selection of the candidate; no deletion is authorized or needed.

No SSH, network, private/secret reads, app/server/service/SQL actions, install/purchase, commit/push, memory writes or separate agents. No added cost or user input was needed. Existing receiver-close precedence, HTTP and cross-filesystem restoration defects remain untouched. Original cause stays NOT_DETERMINED; server execution, capacity acceptance and public opening remain withheld. Neither a local PASS after repair nor this review would grant a native trial.

## Reference Documents And Related Documents

Tier0 injection retained: STD-001, STD-004, STD-002, STD-005. Tier1: PG role and security-policy. Tier2: WI052/053 handoffs, WI052 evidence, WI050 diagnosis, REQ006/current entry, preserved WI042 source/tests and WI045 receipt identity. ATS tag confirmed from workspace.json. Test skill applied to authorized isolated Python tools only; evidence-pack skill used after confirming the WI053 handoff. No subdelegation.

- [WI053 Handoff](WI-20261011-ATS-053-handoff.md): Scope and acceptance criteria.
- [WI052 Evidence](WI-20261011-ATS-052-evidence-pack.md): Owner results, distinct from this review.
- [WI050 Evidence](WI-20261011-ATS-050-evidence-pack.md): Historical diagnosis boundary.
- [WI053 Summary](../user/WI-20261011-ATS-053-summary.md): Concise HOLD and repair handoff.

## V2 Independent Re-review Addendum

**2026-10-11, PG: LOCAL_DIAGNOSTICS_READY for the exact frozen v2 below. PG053-F1 and PG053-F2 CLOSED for v2; no remaining finding within the reviewed local diagnostic scope.** This supersedes the v1 local-readiness HOLD only for these v2 bytes. All preceding findings, logs and v1 candidate records remain historical evidence. NOT_NATIVE_READY and original trial HOLD remain unchanged.

### Findings Disposition

- **F1 closed:** [V2 runtime](../../build/WI052-state-diagnostics/v2/runtime.py):233 now forces HOLD whenever a primary code exists, without replacing that code. The original failing [independent assertion](../../build/WI053-state-diagnostics-review/v2/test_pg_diagnostics.py):409 now passes through actual record_result, attempt JSON, Session.restore, result JSON and final public_result.
- **F2 closed:** The same runtime :212 validates the complete row order against STATE_PLAN. The original malformed NOT_RUN-before-observed sequence now projects INVALID while retaining HOLD and its primary error; [independent assertion](../../build/WI053-state-diagnostics-review/v2/test_pg_diagnostics.py):397 passes.
- **BPF edge remains non-finding:** [Independent parser test](../../build/WI053-state-diagnostics-review/v2/test_pg_diagnostics.py):262 passes again. Tuple multiplicity redistribution remains DIFFERENT/MULTIPLICITY_CHANGED and POST_STATE_CHANGED/HOLD, with the last DB read skipped. No BPF comparison change was made.
- Actual v1/v2 diff contains only project_state and state_result changes. Session bytes are unchanged; the candidate AST scope regression also passes. Security readers, comparison order, original failure categories, baselines, native CLI rejection, timing and cleanup remain unchanged.

### Independent Execution

PG reran these three suites in separate ordinary Python processes on Windows/Python3.14.3, main/HEAD `58b1011dad796017e47dbcc82a4225ca449bcdf6`. The unsuccessful v1 experimental audit runner was not used. Actual methods use mocked native boundaries and in-memory attempt/result storage, not real service/file/SQL execution.

| Command from repository root | Result | PG log |
| --- | --- | --- |
| `python -B -m unittest discover -s build/WI053-state-diagnostics-review/v2 -p 'test_pg_diagnostics.py' -v` | 15/15 PASS;1.061s | [Independent15](../../build/WI053-state-diagnostics-review/v2/independent-tests.log) |
| `python -B -m unittest discover -s build/WI052-state-diagnostics/v2 -p 'test*.py' -v` | 100/100 PASS;2.551s | [Candidate100](../../build/WI053-state-diagnostics-review/v2/candidate-rerun.log) |
| `python -B -m unittest discover -s build/WI042-restricted-web-check -p 'test*.py' -v` | 106/106 PASS;0.422s | [Original106](../../build/WI053-state-diagnostics-review/v2/original-rerun.log) |

Every run exited0, with0 failures/errors/skips. The independent v2 copy changes only ROOT and NEW constants; NEW explicitly targets `build/WI052-state-diagnostics/v2`, and no assertion was weakened. Candidate100 includes15 copied PG tests, and original suites overlap: these counts are not221 unique tests or native proofs. Coverage percentages remain unmeasured.

### Exact Identity And Preservation

Final SE freeze supplied by MA and independently verified: **all16 entries match both SHA-256 and size**. All7 tested Python source files still match their pre-test pins. The [PG v2 manifest](../../build/WI053-state-diagnostics-review/v2/review-manifest.json) contains the complete candidate identities, command results, review-log hashes and preserved report-prefix identities.

| File | SHA-256 |
| --- | --- |
| v2/runtime.py | `ee4c4bc572a7cdbc48d7d1af703a62d25d265b54526a6a8886af4c1a5cbaa09e` |
| v2/web_session.py | `20a18de83f64356168ba73c06f138a47bc1f0b1541fc3d424360db7f83339f56` |
| v2/freeze.json | `0c007d886750dd4a27bc606c45be62e2e8103db4a475e058db79751d7a8760ec` |
| PG v2/test_pg_diagnostics.py | `945b57559016d684da4fcfa74e79b847ca116067f6d4b3007e7247c7dadea745` |

Before/after checks retain all50 earlier recorded files unchanged: candidate v1 14, original WI042 28, native receipt1, and PG v1 test/log/runner files7. The v1 review manifest remains pinned separately. This addendum and the summary addendum are appended after the original report bytes; their prior sizes/hashes are recorded in the v2 manifest. Candidate files were never edited by PG.

### Completion And WI054 Handoff

**WI053 local diagnostic review is complete for the pinned v2. MA: immediately trigger the already prepared WI054 documentation integration.** Integrate F1/F2 closure, the distinct PG/owner counts and exact freeze; preserve v1 failures and native history. No further SE repair is requested within this review.

This is not server-execution authority, deployment/trial readiness, capacity acceptance, original-cause resolution or permission for public opening. Native cause remains NOT_DETERMINED. Existing process-loss, storage-durability, receiver-close precedence, HTTP and restoration limits remain. No app/server/SSH/network/private-secret/SQL activity, installation/purchase, commit/push, memory write or separate agent occurred. Rollback remains non-selection of v2, with no deletion. No added cost or manual user action.

- [SE v2 Addendum](WI-20261011-ATS-052-evidence-pack.md#v2-repair-addendum): Repair and owner evidence.
- [V2 README](../../build/WI052-state-diagnostics/v2/README.md): Local-only reproduction and limits.
- [WI054 Handoff](WI-20261011-ATS-054-handoff.md): Next documentation-only work item.
