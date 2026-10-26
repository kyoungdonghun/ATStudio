---
version: 1.0
last_updated: 2026-10-11
project: ATS
owner: SE
category: evidence-pack
status: confirmed
dependencies:
  - path: WI-20261011-ATS-052-handoff.md
    reason: Approved local diagnostic implementation and write boundary
  - path: WI-20261011-ATS-050-evidence-pack.md
    reason: Confirmed comparison and persistence gap
  - path: ../../build/WI052-state-diagnostics/freeze.json
    reason: Exact local candidate identity for independent PG review
---

# Evidence Pack: WI-20261011-ATS-052

> Purpose: Hand off the implemented, locally tested diagnostic candidate to WI053 PG.

## Summary

**TL;DR: SE implementation and local tests COMPLETE; WI053 independent review pending. LOCAL_ONLY / NOT_NATIVE_READY.** Original native cause remains NOT_DETERMINED; original trial remains HOLD. No native follow-up authority is implied.

## Scope / DoD Check

- [x] Fresh candidate only; original runtime/session/freeze/native-receipt pins unchanged.
- [x] Exact policy/BPF/DB gates, comparison order, short-circuit behavior, original errors, time bounds and cleanup retained. Original function/method AST equality is tested outside the explicitly changed orchestration/projection/CLI functions.
- [x] BASELINE, PRESTART, PRE_VIEW and POST_VIEW observations plus BEFORE/AFTER markers around both existing daemon-reloads; no new queries or baseline refresh.
- [x] Bounded PASS/DIFFERENT/ERROR/NOT_RUN rows, monotonic sequence/elapsed time, allowlisted difference labels, strict shape/order/type/size validation and fail-closed missing diagnostics.
- [x] Actual mocked run/session recording, attempt JSON, restore, result JSON and public projection tested end to end. Diagnostic recording/projection faults retain existing safe primary codes.
- [x] Both native CLI entrypoints unconditionally reject LOCAL_ONLY. No transport recipes or native packaging added.
- [x] Two reports and a concise schema/reproduction README delivered. Immediate next step: MA invokes the already prepared WI053 review, then WI054 integration.

## Evidence Pointers

- [Candidate README](../../build/WI052-state-diagnostics/README.md): Schema, timing semantics, commands, limitations.
- [Runtime](../../build/WI052-state-diagnostics/runtime.py):142 difference labels; :173 strict projection; :224 primary-preserving fallback; :238 recording; :1101-1232 actual runtime integration; :1256 unconditional CLI rejection.
- [Session](../../build/WI052-state-diagnostics/web_session.py):251 reload markers; :271 persistence; :298 unchanged restoration consuming the expanded projection; :354 public projection; :396 unconditional CLI rejection.
- [New tests](../../build/WI052-state-diagnostics/test_state_diagnostics.py):150-231 runtime phases/errors/skips/reload/precedence; :261 actual persistence/restore roundtrip; :273-375 hostile/missing/malformed/time/projection cases; :384 unchanged AST; :403-486 real policy/BPF/DB parsers under mocked boundaries.
- [Semantic diff](../../build/WI052-state-diagnostics/semantic-delta.patch): Only runtime/session delta from WI042. Three copied test files have candidate-specific CLI expectations, fixture pointers and diagnostic restoration fixtures; original tests were not edited.

## Tests And Commands

Verification: **2026-10-11, SE, Windows / Python 3.14.3**, local synthetic fixtures and mocked native boundaries. Branch `main`, HEAD `58b1011dad796017e47dbcc82a4225ca449bcdf6`; unrelated dirty work preserved.

| Command from repository root | Final result | Log |
| --- | --- | --- |
| `python -B -m unittest discover -s build/WI052-state-diagnostics -p 'test*.py' -v` | 82 passed, 0 failures/errors/skips, 0.982s | [Candidate log](../../build/WI052-state-diagnostics/local-tests.log) |
| `python -B -m unittest discover -s build/WI042-restricted-web-check -p 'test*.py' -v` | 106 passed, 0 failures/errors/skips, 0.339s | [Original log](../../build/WI052-state-diagnostics/original-tests.log) |

[Local receipt](../../build/WI052-state-diagnostics/local-receipt.json) records these runs. Candidate count is 62 adapted existing tests plus 20 new tests, including parameterized subcases; suites overlap and are not 188 unique tests. Coverage percentage was not measured; no independent PG PASS is claimed.

Development runs are not hidden: the initial 8-test RED run reported 19 subcase failures and 6 errors before instrumentation; it also exposed a synthetic nonce mismatch, corrected in the fixture. A later 78-test run had one AST-test failure caused by matching duplicate `__init__` names without their class; the test was corrected to use qualified names. Subsequent 78- and 82-test runs passed. These were local development results, not native incident reproduction.

## Identity And Preservation

Exact candidate pins (SHA-256):

| File | SHA-256 |
| --- | --- |
| runtime.py | `72eb5cea07ff344615adb038d7f815e67b6cd2605bfc8b16741cafe7355fd05e` |
| web_session.py | `20a18de83f64356168ba73c06f138a47bc1f0b1541fc3d424360db7f83339f56` |
| test_state_diagnostics.py | `9a9ed310566635fc81c8e2e621547497ae5ebb31ce764a847d8fe07862c49fb2` |
| freeze.json | `b72f029cfc66343eb6bf4dde372a0eb7e4cb269295812efff33321a5f0afbefa` |

The [freeze](../../build/WI052-state-diagnostics/freeze.json) lists all candidate files except itself. SE's [before](../../build/WI052-state-diagnostics/preservation-before.json) and [after](../../build/WI052-state-diagnostics/preservation-after.json) arrays match for four known public artifacts: WI042 runtime `ad3e8a20...a7a7b6`, session `3092ae8c...27257a`, freeze `b92cbe9c...37f6bc`, and WI045 native receipt `aad6797c...3c3f87`. Full digests are in those arrays. MA separately reported exact equality of all 28 WI042-directory before/after SHA-256 entries; that broader check is MA-owned, with no command-receipt ID supplied.

## Risks / Rollback

Observation time is detected-at, not exact change time or proof of cause. Reload markers are not additional BPF observations. Early pre-admission failures remain outside the original result block; abrupt process loss can still leave missing diagnostics. A failed persistence write cannot guarantee an on-disk receipt, but the in-memory primary error remains. Existing receiver-close error precedence, HTTP, cleanup and recovery defects were deliberately not repaired. Future native packaging requires separately scoped work and approval.

No source JAR/SPA, old artifacts, app settings, credentials, private logs, DB, services, cloud or remote system were changed. No app build, installation, purchase, commit, push, memory write or subdelegation. No added cost or manual user action was needed. Rollback is simply not selecting this local candidate; any later deletion requires separate authorization. Historical originals remain available.

## Reference Documents And Related Documents

Injected Tier0 retained: STD-001 core-principles, STD-004 documentation-standards, STD-002 development-standards, STD-005 glossary. Tier1: SE role and security excerpt. Tier2: WI052 handoff, current decision entry/REQ006, WI050/051 findings, pinned WI042 source/tests and WI045 receipt identity. Project tag ATS confirmed in `.claude/config/workspace.json`. Test skill applied only to the authorized isolated Python suite; create-wi-evidence-pack skill applied after confirming the handoff exists. No additional approval requested.

- [WI052 Handoff](WI-20261011-ATS-052-handoff.md): Explicit approval and fixed write set.
- [WI050 Evidence](WI-20261011-ATS-050-evidence-pack.md): Historical cause/evidence boundary.
- [REQ006](../user/REQ-20261011-ATS-006.md): Parent request; amendment integration remains MA/DocOps-owned.
- [WI052 Summary](../user/WI-20261011-ATS-052-summary.md): Concise handoff result.

## V2 Repair Addendum

**2026-10-11, SE: Two confirmed PG findings repaired locally; independent WI053 v2 review remains pending.** This addendum preserves the v1 report and all frozen v1 bytes. The same approved WI052 scope and injected Tier0/security rules apply; MA applied the handoff skill for this continuation. No native/server/app work was authorized or performed.

### Findings And Bounded Changes

- PG's [initial log](../../build/WI053-state-diagnostics-review/independent-tests.log) recorded 14 tests with two failures. The [final v1 log](../../build/WI053-state-diagnostics-review/independent-final-v1.log) recorded 15 tests with the same two failures. These v1 findings supersede any inference that the earlier SE PASS settled the projection contract.
- [V2 runtime](../../build/WI052-state-diagnostics/v2/runtime.py):212 now validates the complete persisted row order against STATE_PLAN, including NOT_RUN rows. Filtering away skipped rows can no longer conceal a reordered observation stream.
- The same runtime :233 forces HOLD whenever a primary error `code` exists, preserving that code even with otherwise complete PASS diagnostics.
- [Semantic delta](../../build/WI052-state-diagnostics/v2/semantic-delta.patch) contains only these two function changes. Session bytes, comparators, read order, baseline handling, native guards, time bounds, BPF labels and cleanup remain unchanged. Local fixture paths alone were adjusted for the deeper directory.
- The speculative third BPF finding was withdrawn by MA after the pinned-v1 recheck. V1 already compares shared-tuple multiplicities; PG's real attachment-parser redistribution test passes. No BPF repair was made in v2.

### Verification

| Local command from repository root | Result | Evidence |
| --- | --- | --- |
| `python -B -m unittest discover -s build/WI052-state-diagnostics/v2 -p 'test*.py' -v` | 100 passed, 0 failures/errors/skips, 2.186s | [V2 log](../../build/WI052-state-diagnostics/v2/local-tests.log) |
| `python -B -m unittest discover -s build/WI042-restricted-web-check -p 'test*.py' -v` | 106 passed, 0 failures/errors/skips, 0.333s | [Original rerun](../../build/WI052-state-diagnostics/v2/original-tests.log) |

Owner/environment: SE, Windows/Python 3.14.3, local synthetic fixtures and mocked native boundaries; branch/HEAD unchanged from v1. The 100 tests comprise the prior 82, two focused regression tests, one v1/v2 AST scope test, and 15 copied PG tests. Only ROOT/NEW fixture pointers changed in the PG copy. This SE execution is **not** an independent PG approval. Before the production-code repair, the same suite produced 10 failure records across PG and parameterized focused cases, zero errors; see [RED log](../../build/WI052-state-diagnostics/v2/red-tests.log). No coverage percentage claimed.

Focused tests in [test_state_diagnostics.py](../../build/WI052-state-diagnostics/v2/test_state_diagnostics.py):487 and :507 cover direct public projection and actual mocked record_result -> attempt JSON -> restore -> result JSON -> public_result. They verify retained primary codes, HOLD despite successful cleanup, reordered skip rejection and acceptance of correctly ordered incomplete diagnostics. The :536 AST guard permits only the two named runtime functions to differ from v1.

### Pins And Preservation

| V2 file | SHA-256 |
| --- | --- |
| runtime.py | `ee4c4bc572a7cdbc48d7d1af703a62d25d265b54526a6a8886af4c1a5cbaa09e` |
| web_session.py | `20a18de83f64356168ba73c06f138a47bc1f0b1541fc3d424360db7f83339f56` |
| test_state_diagnostics.py | `017b12ca0b4b5c6876035bf83ff4da2d495858e20adb959698601a592f491399` |
| test_pg_diagnostics.py | `af2ac88a767a51673336cc3d52ad11212f53fb8bd2c11156468b880a0475ef37` |
| freeze.json | `0c007d886750dd4a27bc606c45be62e2e8103db4a475e058db79751d7a8760ec` |

[Before](../../build/WI052-state-diagnostics/v2/preservation-before.json) and [after](../../build/WI052-state-diagnostics/v2/preservation-after.json) match for all 14 v1-root files (including the old freeze/logs/manifest) and four original WI042/WI045 pins. The frozen v1 manifest remains `b72f029cfc66343eb6bf4dde372a0eb7e4cb269295812efff33321a5f0afbefa`. [V2 freeze](../../build/WI052-state-diagnostics/v2/freeze.json) and [receipt](../../build/WI052-state-diagnostics/v2/local-receipt.json) identify the fresh candidate; [report-prefixes](../../build/WI052-state-diagnostics/v2/report-prefixes.json) identifies the pre-addendum report bytes.

### Remaining Boundary

**LOCAL_ONLY / NOT_NATIVE_READY**, native cause NOT_DETERMINED, original trial HOLD. No third behavioral fix, app restart, DB/network/cloud operation, new dependency, cost, manual user step, commit, push or memory write. All earlier persistence/process-loss/coverage limitations remain. Rollback means declining v2; do not delete historical candidates. Return to MA now for WI053 independent v2 review, then WI054 integration.

- [V2 README](../../build/WI052-state-diagnostics/v2/README.md): Reproduction and scope.
- [V2 Summary](../user/WI-20261011-ATS-052-summary.md#v2-repair-addendum): Concise handoff.
