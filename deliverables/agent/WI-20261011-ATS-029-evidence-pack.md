---
version: 1.0
last_updated: 2026-10-11
project: ATS
owner: SE
category: evidence-pack
status: stable
related_wi: WI-20261011-ATS-029
dependencies:
  - path: WI-20261011-ATS-029-handoff.md
    reason: Narrow delegated implementation scope
  - path: ../user/REQ-20261011-ATS-004.md
    reason: Local-only diagnostic authorization
  - path: WI-20261011-ATS-028-native-result.json
    reason: Preserved historical outcome and unknown cause
---

# Evidence Pack: WI-20261011-ATS-029

> Purpose: Submit safe cleanup diagnostics and local mock evidence for WI030 review.

## 1. Summary and Scope

**TL;DR: Local implementation complete,67/67 mock tests PASS; WI030 independent
review pending. No native execution authorized or performed.**

The separate candidate adds per-step cleanup and final-baseline outcomes without
weakening predicates, retrying operations or exposing underlying evidence. Changes
are confined to `build/WI029-cleanup-diagnostics/` and the WI029 two-set outputs.
No server recipe, deployment unit, new approval or JAR was created/copied.

The historical WI028 result remains **app_start VERIFIED, cleanup FAILED,
overall HOLD**. Root cause **NOT_DETERMINED**; this code cannot recover missing
historical return codes or establish that the old failure was a false positive.

- [x] Distinguishable cleanup actions, postconditions and final baseline checks.
- [x] Fixed labels/codes and bounded numeric return code/timeout metadata only.
- [x] Best-effort continuation, simultaneous failures and sticky HOLD tested.
- [x] Unknown failures, command-finalizer failures and accepted masked/failed app state tested.
- [x] Mocked actual `run()`-finally integration, inherited regressions and compile checks pass.
- [x] Original identities retained; CLI disabled with LOCAL_ONLY; next WI030 required.

## 2. Changes and Evidence Pointers

[runtime.py](../../build/WI029-cleanup-diagnostics/runtime.py):

| Location | Narrow change |
| :--- | :--- |
| `CommandFailure`, line72; `safe_error`,112 | Sanitized metadata; unknown or malformed categories map to UNEXPECTED_FAILURE. |
| `command`,177 | Retain safe nonzero rc/timeout; independently attempt kill/reap/stdout/stdin finalizers. A primary failure survives a later finalizer failure. |
| `DIAGNOSTIC_STEPS`,749; `diagnostic_step`,759 | At most23 unique fixed rows, bounded numeric metadata, no arbitrary text or raw values. |
| `cleanup`,783 | Five actions and eight existing postconditions attempted independently; explicit NOT_APPLICABLE for absent actions. |
| `final_baseline`,809 | Three TLS pins, Nginx/Certbot active state, jobs, PID/unit-state identity, listeners and private env equality checked independently. Values/hashes are never output. |
| `cleanup_result`,832 | Failure remains sticky even if a later caller reports clean; existing HOLD is never promoted. |
| `run`,851, finally at994 | Collect action/check and baseline rows in one receipt; later baseline checks still execute after prior failure. |
| `main`,1021 | Every CLI invocation fails LOCAL_ONLY before run/prestart; retained orchestration is only for mocked integration. |

The existing `fixed_state` preflight and containment/network/env/DB/HTTP predicates
are unchanged. The final comparison is expanded into its individual predicates,
not replaced with weaker acceptance. App `inactive` or `failed` plus masked is
still allowed; persistent MySQL mask-link verification is retained. These facts
do not identify the original failing step.

Per-command budgets, stop35s and final reap2s are retained. Checks previously
skipped after failure now consume their existing individual bounds; total
failure-path duration can increase. No automatic command retry or new aggregate
cleanup deadline is introduced or claimed. A first safe finalizer category is
retained alongside the primary command failure; later finalizers still run.

## 3. Exact Identities and Preservation

Candidate runtime: **49865 bytes**, SHA-256
`209a124f40e6ad55ffb45231d1048970209c33d2f331babb8b0ae8445e3b0e7b`.
[freeze.json](../../build/WI029-cleanup-diagnostics/freeze.json) pins all six Python
candidate/test files, frozen2026-10-11T04:43:39Z. The candidate hash was supplied
to MA for concurrent PG review before completing this report.

| Preserved source | SHA-256 |
| :--- | :--- |
| WI026 runtime | `c61f590efabae4b94196cfde13b1773c67becb04bdfff8b1e954a022a3f08274` |
| WI026 README | `e2305e742db1af5da6a272c31d3b641fc2386e96053f7511fb99035c23c52360` |
| WI028 native receipt | `4bca13c97a12885e55037c3e81bcefe26ebe6b4bb16473255ec06c1e746bfdfa` |

All **38** files under WI024/WI026, including history, matched their initial
[preservation manifest](../../build/WI029-cleanup-diagnostics/source-preservation.json)
after implementation. The WI028 receipt and all six candidate/test pins also
passed local hash checks. No old test source was edited. JAR was neither read,
written nor rebuilt by SE; its unchanged `01d5623972e17d0b6e1d3e532096239cd87e9995a08771c59292c94bf3699dcb`
identity is MA-supplied, not a fresh JAR audit by this WI.

## 4. Reproduction and Results

Owner **SE**, verification date **2026-10-11**, environment **Windows / Python
3.14.3**. From repository root:

```powershell
python -I -B -m unittest discover -s build/WI029-cleanup-diagnostics -p 'test_*.py' -v
```

**67 tests,67 passed,0 failures/errors/skips;0.223s; exit0.**
[Full log](../../build/WI029-cleanup-diagnostics/test-output.log) and
[machine result](../../build/WI029-cleanup-diagnostics/test-result.json).

| Suite | PASS | Main evidence |
| :--- | :--- | :--- |
| `test_cleanup_diagnostics.py` |10/10 | Every one of23 steps injected separately; command primary/finalizer failures, timeout, unknown text, sticky HOLD, simultaneous run-finally failures, disabled CLI. |
| `test_runtime.py` |42/42 | Inherited guards, one start, no retry, BPF, HTTP deadlines, SIGHUP and mocked orchestration. |
| `test_runtime_preliminary.py` |8/8 | Adapted PG AST regressions, including required diagnostic dependencies. |
| `test_http_lifetime.py` |3/3 | Actual stdlib HTTP parser with inert bytes; no network. |
| `test_log_parent.py` |4/4 | Existing ancestry acceptance/rejection and unchanged ancestry functions. |

The actual candidate `run()` integration test mocks all native boundaries but
executes its real cleanup/final-baseline functions. It records APP_STOP timeout,
MYSQL_MASK unknown failure, three TLS-read failures, listener drift and env drift
simultaneously, verifies all23 outcomes exist, and retains HOLD. All stop/mask
actions are attempted once. Synthetic private text/command payloads are excluded
from serialized output. Masked/failed app acceptance has a separate passing test.

Six Python sources compile in memory without pyc/native execution. AST comparison
confirms all original functions outside the six intended edit points remain
unchanged. Fixture/AST wiring adaptations exist only in new copied test files;
the parent delta assertion now checks unchanged ancestry functions because the
candidate intentionally changes diagnostic logic. The first integration run
exposed a missing original-app LoadState in its mock fixture; correcting that
fixture produced the final passing result, without loosening runtime checks.

Tests never invoke run/main/systemctl/SSH without mocks. No actual socket, DB,
secret, service, upload, installation, app build, full application suite or native
deadline/restoration verification occurred. Suites overlap;67 is not67 independent
native proofs. SE execution of PG-derived tests is not WI030 independent approval.

## 5. Limits, Rollback and Chain

No app rerun or external publication is authorized. The historical startup and
failed-cleanup receipt remain untouched. Future native diagnosis may still need
fresh user approval and current exact-byte review. No historical cause is fixed
or inferred here. SIGKILL, host loss or uninterruptible IO can prevent cleanup;
local mocks cannot prove target restoration.

Rollback is to **withhold this candidate**, not delete old evidence or reuse
WI026's exhausted claim. CLI is deliberately inert. Existing WI026 constants
inside the candidate are retained historical test context, not a new deployment.

**WI029 implementation complete; next is WI030 independent PG review immediately,
then WI031 DocOps integration.** MA owns that chain; this worker did not
subdelegate or perform native operations. No docs/index/REQ edits, commit or push.

## 6. Reference Traceability

Tier0 STD-001/004/002/005 and security context retained from injection. Tier1
quality gates retained; approved REQ004, actual WI029 handoff and WI028 evidence
and sanitized receipt read. Applied `test` for scoped Python regression execution
and `create-wi-evidence-pack` after verifying the MA-created handoff. No broad
architecture investigation was needed.

## Related Documents

- [WI029 handoff](WI-20261011-ATS-029-handoff.md): Approved boundaries.
- [REQ004](../user/REQ-20261011-ATS-004.md): Local-only user authorization.
- [WI028 evidence](WI-20261011-ATS-028-evidence-pack.md): Preserved actual outcome.
- [Local README](../../build/WI029-cleanup-diagnostics/README.md): Diagnostic contract and mock command only.
- [User summary](../user/WI-20261011-ATS-029-summary.md): Concise result and review boundary.
