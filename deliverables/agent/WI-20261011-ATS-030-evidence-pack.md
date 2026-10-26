---
version: 1.0
last_updated: 2026-10-11
project: ATS
owner: PG
category: evidence-pack
status: confirmed
dependencies:
  - path: WI-20261011-ATS-030-handoff.md
    reason: Approved local-only review and write boundary
  - path: ../user/REQ-20261011-ATS-004.md
    reason: Local diagnostics authorization without native authority
  - path: WI-20261011-ATS-029-evidence-pack.md
    reason: Completed owner candidate and frozen identities
  - path: WI-20261011-ATS-028-evidence-pack.md
    reason: Historical startup and unresolved cleanup outcome
---

# Evidence Pack: WI-20261011-ATS-030

> Purpose: Independently gate the exact local cleanup-diagnostics delta without authorizing native execution.

## 1. Verdict and Scope

**TL;DR: ALLOW_LOCAL_DIAGNOSTIC_CANDIDATE. No confirmed blocker in the reviewed
delta. PG independently executed 67/67 owner tests and 17/17 new adversarial
tests against the frozen runtime. Native execution is NOT_AUTHORIZED and NOT_RUN.**

Verification owner PG, date 2026-10-11, environment Windows / Python 3.14.3.
The review covers WI029 versus frozen WI026, diagnostics, error projection,
best-effort continuation and mocked integration. It does not reopen the full
application, architecture, deployment recipe or native gate.

- [x] Named command failures and bounded return/timeout metadata retained.
- [x] All 23 cleanup/baseline steps continue after injected failures.
- [x] Unknown/unhashable exception arguments cannot escape safe categorization.
- [x] Masked/failed app state remains accepted by the existing predicate.
- [x] Startup VERIFIED survives a later cleanup failure while overall HOLD stays sticky.
- [x] Native boundaries mocked; no retries, unsafe payload output or altered owner files.
- [x] Frozen six-file identity and 38 historical files plus WI028 receipt verified.

The historical result remains **app_start VERIFIED / cleanup FAILED / overall
HOLD, root cause NOT_DETERMINED**. New passing tests cannot recover missing old
return codes, prove a false positive or turn historical restoration into PASS.

## 2. Evidence Pointers and Findings

All implementation pointers below refer to
[runtime.py](../../build/WI029-cleanup-diagnostics/runtime.py) at the exact hash
in section 3. No implementation edit was made.

| Lines / symbol | Independent review and verification |
| :--- | :--- |
| 72, 112: CommandFailure / safe_error | Exact exception/string type and single-argument allowlist; empty, list, dict, multiple, unknown and string-subclass arguments fail safely. No exception stringification. |
| 177-245: command | Shell disabled, discarded stderr, bounded stdout and existing timeout/reap limits retained. Safe nonzero rc and timeout recorded; primary failure survives a secondary finalizer failure. Remaining descriptor finalizers continue. |
| 749-780: diagnostic_step | Fixed 23-step vocabulary, unique rows, bounded exact numeric types; synthetic command/output/exception payloads never appear in serialized rows. |
| 783-829: cleanup / final_baseline | Each action and predicate has its own capture boundary. Simultaneous failures do not skip later steps, including the final private-env equality check. Inapplicable actions are explicit, not claimed PASS. |
| 832-839: cleanup_result | Previous HOLD/FAILED cannot be promoted by a later clean result; startup identity and metrics are not rewritten. |
| 990-999: run finally | Real candidate orchestration, cleanup and final-baseline functions exercised together under mocks: success, command-cleanup failure, last-baseline failure and earlier startup failure. |
| 1021-1035: main | LOCAL_ONLY rejects all tested argument forms before run/prestart dispatch, including simulated Linux in the owner suite. |

**Findings: no confirmed correctness or disclosure defect within this delta.**
The accepted masked/failed state and synthetic command failure cases demonstrate
current behavior only; neither identifies WI026's historical failure.

Existing containment, env, DB, HTTP and startup predicates were not relaxed.
The owner AST regression independently rerun by PG confirms original functions
outside six intended edit points are unchanged; the reviewed source diff covers
those edit points and added diagnostic helpers. No broader audit claim is made.

## 3. Exact Identities and Preservation

Candidate runtime: **49865 bytes**, SHA-256:

```text
209a124f40e6ad55ffb45231d1048970209c33d2f331babb8b0ae8445e3b0e7b
```

| Artifact | SHA-256 |
| :--- | :--- |
| Owner freeze.json | `bbacb2c1475312e686b3abc509f7f7c64d5839bdbad14d9590b9e430d82da28c` |
| Owner README.md | `47da76a06f7e7698b0026c69f1537cbc86547bae3dbd1e987373d0af53688729` |
| Owner preservation manifest | `cb961a74681ca0e51a7542a0aed5ff944f0479156bf6d7734671ca0cdd929a14` |
| PG test_diagnostics.py | `f68b032c206b02237539cd8db53f1ce79818b4b128426fb9f8b2aa09c3875ccf` |
| Preserved WI026 runtime | `c61f590efabae4b94196cfde13b1773c67becb04bdfff8b1e954a022a3f08274` |
| Preserved WI028 native receipt | `4bca13c97a12885e55037c3e81bcefe26ebe6b4bb16473255ec06c1e746bfdfa` |

All six candidate/test files matched the owner's freeze, including byte counts.
All 38 WI024/WI026 preservation entries and the WI028 receipt matched. Candidate
runtime was pinned at test load and rechecked at test exit, then rehashed with
freeze/README after MA's completion notification. The
[machine review result](../../build/WI030-review/test-results.json) records every
candidate/test pin, metadata hashes and command receipts. Hash verification
receipts: `19da43`, `93c655`.

JAR bytes were not read or rebuilt; no fresh JAR verification is claimed. PG
wrote only its review directory and WI030 evidence/summary. Historical bytes,
owner candidate and owner evidence were not edited.

## 4. Tests and Reproduction

Apply the scoped Python test procedure from the `test` skill. From repository root:

```powershell
python -I -B -m unittest discover -s build/WI029-cleanup-diagnostics -p 'test_*.py' -v
python -I -B build/WI030-review/test_diagnostics.py
```

| Independently executed by PG | Result | Duration | Receipt |
| :--- | :--- | :--- | :--- |
| Owner frozen regression suite | 67/67 PASS, exit0 | 0.207s | `6e265b` |
| New PG adversarial suite | 17/17 PASS, exit0 | 0.073s | `8f713f` |

Both runs followed publication of the owner freeze and completion evidence;
subsequent MA notification confirmed the identical runtime pin. Earlier
preliminary executions also passed and are not added to these totals. No test
session remains running. Suites overlap; these are local test executions, not
84 distinct native proofs. No application build or broad application suite ran.

[PG tests](../../build/WI030-review/test_diagnostics.py) cover unhashable/unknown
error args; bool, nonfinite and out-of-range scalar rejection; foreign
CalledProcessError/TimeoutExpired payload suppression; nonzero/timeout/unknown/
output-bound command failures; primary-plus-finalizer and finalizer-only
failures; all 23 steps failing concurrently; optional-step accounting; and four
actual mocked run-finally outcomes. Native syscall/process/socket guards are
also asserted unused, so a swallowed guard exception cannot silently pass.
Only synthetic private-looking values were used.

## 5. Limits, Conditions and Rollback

- Allow only local inspection, mocked regression and WI031 evidence integration
  for these exact bytes. This is not staging or native approval.
- Keep LOCAL_ONLY intact. Retained WI026 paths and approval fields are historical
  test context, not a fresh namespace or permission to reuse the consumed claim.
- Existing per-command/output bounds remain. Additional previously skipped
  checks may lengthen failure cleanup; no whole-cleanup deadline is claimed.
- Mocks cannot establish systemd outcomes, native timing, signal/host-loss
  restoration, secret-capture infrastructure safety or internet isolation.
- Keep original startup VERIFIED, cleanup FAILED and unresolved HOLD separate
  from this local acceptance. No root-cause claim follows from synthetic tests.
- Rollback: withhold this local candidate. No native rollback, deletion or retry
  was performed. Any future execution needs separate user authorization and
  exact-byte review of its execution artifact.

**WI030 complete. Next blocked work is WI031 DocOps integration, owned by MA.**
No subdelegation, SSH, native command, secret use, upload, install, billing,
commit or push occurred.

## 6. Reference Traceability

Retained Tier0 injection: STD-001 core-principles, STD-004 documentation-standards,
STD-002 development-standards and STD-005 glossary. Retained Tier1: PG persona,
security-policy and quality-gates. Tier2: actual MA-created WI030 handoff,
approved REQ004, WI029 handoff/completion, WI028 evidence, and the `test` and
`create-wi-evidence-pack` skills. The actual handoff precondition was checked
before generating this two-set deliverable. Write scope excludes shared indexes
and REQ updates; WI031 owns their integration.

## Related Documents

- [WI030 handoff](WI-20261011-ATS-030-handoff.md): Review contract.
- [REQ004](../user/REQ-20261011-ATS-004.md): Local-only authority.
- [WI029 evidence](WI-20261011-ATS-029-evidence-pack.md): Completed frozen candidate.
- [WI028 evidence](WI-20261011-ATS-028-evidence-pack.md): Historical outcome and unknown cause.
- [Local README](../../build/WI029-cleanup-diagnostics/README.md): Diagnostic contract and limits.
- [Review result](../../build/WI030-review/test-results.json): Exact pins and independent results.
- [User summary](../user/WI-20261011-ATS-030-summary.md): Concise local gate.
