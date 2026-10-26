---
version: 1.1
last_updated: 2026-10-11
project: ATS
owner: SE
category: evidence-pack
status: stable
dependencies:
  - path: WI-20261011-ATS-060-handoff.md
    reason: Approved bounded implementation and review chain
  - path: ../user/REQ-20261011-ATS-007.md
    reason: Active approved requirement and routine repair authority
  - path: WI-20261011-ATS-058-evidence-pack.md
    reason: Historical BPF HOLD and missing final DB comparison
---

# Evidence Pack: WI-20261011-ATS-060

> Purpose: Deliver a frozen local candidate for independent PG061 review, with exact BPF identity preservation and independent final comparisons.

## Summary

**REVISED LOCAL READY FOR WI061; native NOT_RUN by SE; final independent review pending.**
Cloned frozen WI055 into a fresh WI060 identity without modifying its originals.
Moved Nginx overlay preparation before the app security baseline/probe, retained
exact equality predicates, added bounded private BPF event records and independent
comparison groups. Historical WI058 remains HOLD; no cause is established here.

## Scope / DoD Check

- [x] New candidate and operational recipe, with no app source/configuration change.
- [x] Required overlays and reload precede the admitted app policy/BPF baseline.
- [x] BPF IDs, direction and multiplicity remain exact; no replacement acceptance.
- [x] Final policy/BPF/DB readers run independently, including after view failure.
- [x] Root-only bounded snapshots and sanitized public diagnostics; no raw IDs exported.
- [x] Targeted and inherited local tests pass; exact candidate and predecessor hashes checked.
- [x] Two deliverable sets and mandatory WI060 -> WI061 chain recorded.
- [ ] Native proof, independent approval and operational completion: outside SE scope.

## Exact Identity

Candidate root: [WI060](../../build/WI060-restricted-web/README.md).
The [freeze](../../build/WI060-restricted-web/freeze.json) binds 81 files and
the ordered 12-file runtime bundle. The bundle is SHA-256 of each UTF-8 filename,
NUL and exact file bytes concatenated in manifest order.

| Object | SHA-256 |
| --- | --- |
| Ordered runtime bundle | `3f9edd75b5ebc32281cc651d45fe16861a2dd2a19bc8c5dd5deac24494cc4316` |
| freeze.json | `e0896db9fce85f9a1d802b2d85e412c371f3d447805f8b473c6171c85da31d7a` |
| README operational recipe | `2f230fcf6c724ff2df89c9d80e7097987ba8c9abbcee750a1dd1669e40e519bd` |
| runtime.py | `ca5fc578cc51994019273a44213b5df70c3103f676b4064b2fb80f1780a88786` |
| web_session.py | `bd8ab246c9f0973fa8e03d83479c6b7210872cae84079f75df10fe5bdaac2f84` |

These replace the pre-PG061 bundle118555d8... and freeze77813c6f..., which are
retained with their receipt and semantic delta under
`build/WI060-restricted-web/pre-pg061/`. Earlier ready notices were superseded
by PG061's interim HOLD, not execution authorization.

## PG061 Repair

- F061-01: moved the final comparison group into the common finally path,
  guarded by attempted grant publication. Every exit after that boundary attempts
  policy/BPF/DB once before cleanup, including grant write/fsync, POST_GRANT,
  readiness, PRE_VIEW and view failure. Secondary failures never replace the
  first error or skip the remaining readers/cleanup.
- F061-02: set JAVA_RELEASE_POSSIBLE before the potentially visible grant write;
  set JAVA_RELEASED immediately after successful return, before diagnostics.
  Possible includes an early open/write failure and a full write followed by
  fsync failure. Neither label claims successful Java startup. ATTEMPTED remains
  valid only when the grant-writing boundary was not reached.
- N061-01: restored PG056 attribution for inherited F1/F3 fixes. Historical
  PG056 test reruns are explicitly distinct from PG061 independent review.

SE reran the unchanged PG-owned reproducers successfully after repair and added
four own post-grant/partial-write/pregrant regressions. Only PG may issue final
ALLOW against these new pins; this report does not close the review findings
on PG's behalf. No BPF equality or policy gate was weakened.

The artifact is local, not a public/cloud URL. Do not run preparation/seal helpers
or edit reviewed bytes after PG061 binds its verdict. The recipe pins the existing
JAR and WI036 SPA contract; no artifact rebuild or application edit occurred.

## Evidence Pointers

All implementation/test/support changes are under the new
`build/WI060-restricted-web/`; only this report and the
[user summary](../user/WI-20261011-ATS-060-summary.md) are outside that root.
The existing handoff, REQ, index, application sources and historical files were
not edited by SE.

| File / location | Meaning |
| --- | --- |
| `web_session.py:Session.prepare` | Render and syntax-check config, record restoration intent, install Nginx overlay before INSTALL_RELOAD and app proof |
| `web_session.py:Session.view` | No overlay installation/global reload; exact BPF checks around Nginx stop/start |
| `runtime.py:STATE_PLAN` | 18 fixed public rows including grant and Nginx event boundaries |
| `runtime.py:StateDiagnostics.private_bpf` | Bounded root-only exact snapshots; exclusive writes and fixed-path metadata |
| `runtime.py:StateDiagnostics.compare_group` | Attempt all group readers; retain first failure, never short-circuit DB |
| `runtime.py:run` | PRESTART/PRE_VIEW groups and common-finally POST_VIEW group on every attempted-grant exit |
| `runtime.py:policy,attachment_snapshot,db_counts,cleanup,final_baseline` | Original predicates retained; inherited AST regression checks |
| `public-transport.ps1:Assert-RemoteDiagnostics` | Exact new plan, fixed labels and complete/all-PASS requirement for success |
| `test_bpf_events.py` | Eleven focused event/privacy/failure/order methods using synthetic boundaries |
| `test_state_diagnostics.py,test_pg_diagnostics.py,test_recipe.py` | Adapted explicit sequence assumptions; original tests preserved separately |
| `S0-current.sh` | Also require previous WI055 app/supervisor inactive or failed with PID0 |
| `seal_patch.py,verify_freeze.py` | Apply-patch-only binding generation and read-only final verification |
| `semantic-delta.patch` | Runtime/session semantic changes against identity-normalized WI055 |
| `preservation-before.json,preservation-after.json` | 161 exact historical file hashes, including the entire WI055 tree |

Preparation deliberately precedes the baseline, not its acceptance criterion.
The existing Nginx process continues its original configuration until the view's
stop/start. The new drop-in becomes effective earlier, so unexpected competing
restart/reload is still prohibited by exclusive maintenance and current gates.
Restoration intent is persisted before the Nginx overlay write; cleanup retains
the inherited independent checks and preservation requirements.

Private evidence is at most 11 files x16KiB (176KiB) under the root0700 WI060
private log directory, each file0600 with no overwrite and fsync. Nine samples
contain the exact effective app-cgroup attachment tuples with phase, elapsed
time and Unix nanosecond observation time. INSTALL_RELOAD BEFORE/AFTER records
have null attachments and scope NOT_SAMPLED_PRESTART. These are timing markers,
not claims of an empty host BPF state. No program/tag equivalence is inferred.
Read or metadata/write failures remain HOLD. Public rows contain fixed labels,
sequence and elapsed_ms only. A sample time brackets observation, not the exact
time or cause of a filter replacement.

Comparison groups continue after mismatch, reader exception and private-record
failure. The final group is attempted after any grant write attempt, even if
Nginx view was never reached. Missing baseline or pregrant failure prevents
grant/view admission; unavailable phases remain NOT_RUN. The fixed POST_VIEW
phase means final pre-cleanup comparison, not proof that view opened; app/view
states and skipped event rows retain that distinction. A primary error is not replaced by a later
comparison error, but individual final rows retain the latter. Any non-PASS
diagnostic prevents BOUNDED_PASS; successful cleanup cannot erase an earlier HOLD.

## Commands And Test Results

Verified locally on 2026-10-11 by SE: Windows11 build22631, Python3.14.3,
PowerShell7. Checkout main, HEAD `58b1011dad796017e47dbcc82a4225ca449bcdf6`,
with extensive pre-existing dirty/untracked work preserved.

`python -B build/WI060-restricted-web/verify_local.py` runs the following suites
without native services, DB/network or credential access. It emits receipt/log
data, which was recorded using apply_patch. See
[local receipt](../../build/WI060-restricted-web/local-receipt.json).

| Suite | Tests / assertions | Result |
| --- | ---: | --- |
| Historical WI042 | 106 | PASS |
| Frozen WI052 diagnostics | 100 | PASS |
| Frozen WI055 | 130 | PASS |
| WI060 candidate | 141 | PASS |
| PowerShell syntax/HTTP child/byte roundtrips | 38 | PASS |
| Sealed HTTP SelfTest | 8 | PASS |
| Historical PG056 Python reproducer against WI055 | 23 | PASS |
| WI060 retargeted transport reproducer | 8 | PASS |
| WI060 retargeted HTTP reproducer | 18 | PASS |
| Step-specific public receipt/schema | 53 | PASS |
| PG061 independent reproducers, rerun by SE | 8 | PASS |
| PG061 requested checks, rerun by SE | 5 | PASS |
| PG061 status schema, rerun by SE | 47 | PASS |

Counts overlap and are not additive coverage. New focused tests prove independent
final reads after multiple simultaneous failures, failed private snapshot writes,
view event drift/errors and Nginx start failure; exact snapshots remain private.
New post-review tests also inject grant failures before/after visible bytes,
post-grant readiness failure and simultaneous final-reader/receiver failure;
pregrant failure never claims release.
Initial inherited sequence assertions required adaptation to the requested new
flow; no policy equality relaxation or skipped failing test was used. Original
policy, parser and cleanup function AST checks continue to pass.

`python -B build/WI060-restricted-web/verify_freeze.py` -> PASS_LOCAL_EXACT_BYTES,
81 candidate files and161 preserved historical files. Receipt `d6629f`.
This verifies local hashes only. Test method logs are named by suite inside the
candidate; cloned pre-publication-fix artifacts remain inherited context, not
current results. No Gradle/npm run or coverage percentage is claimed because
application sources were out of scope. Linux syntax/service execution, fsync
behavior, startup latency and actual BPF identity behavior remain unverified here.

## Reference Documents (Tier 0-2)

Scoped document validation receipt28c93b passed for this Evidence Pack, the user
summary and frozen README:3 documents,21 metadata fields,8 supported IDs,
zero errors/warnings/direct issues. The repository validator was loaded with
runpy and its root Markdown enumeration limited to those three paths; only
Tier0, links and traceability checks ran, plus direct metadata/dependency/whitespace
checks. Use Python `-X utf8 -B` on this Windows console; the first cp949-output
attempt failed on the validator's checkmark, then the UTF-8 rerun passed.
Scoped git diff --check receipt39ae0a exited0; direct checks cover untracked files.
Final read-only verification receipt6830de reconfirmed the same81-file freeze and
161 preserved files. No candidate bytes changed after that freeze.

Rule source: `.claude/config/context-injection-rules.json`; assignee SE,
implementation/security/testing, required tiers0 and1. Project ATS confirmed in
`.claude/config/workspace.json`. The corrected human Tier0 injection superseded
the initial undefined placeholders.

| Tier | References / use |
| --- | --- |
| 0 | STD-001 core-principles, STD-004 documentation-standards, STD-002 development-standards, STD-005 glossary: approved scope, minimal changes, language, evidence boundaries |
| 1 | security-policy, access-control-policy, quality-gates, evidence-pack-standard: private records, separation of duties, exact review gate |
| 2 | create-wi-evidence-pack and test skills applied; validate-docs skill for scoped report checks; docs/index.md Current Decision Entry |
| Context | Approved REQ007, WI060 handoff, WI058 completed HOLD evidence, WI059 local design, WI055 runtime/recipe/tests, WI061 handoff |
| Review | WI061 interim Evidence Pack and unchanged independent/requested/schema reproducers: F061-01/02 repair and N061-01 attribution |
| Conditional / not needed | Frontend standards/React guidance and design templates: no frontend, architecture or new handoff created |

## Risks / Rollback

- The reload hypothesis remains unproven. MA supplied current readonly receipt
  81a9b0 (systemd255.4-1ubuntu8.17/kernel6.8.0-146-generic, stopped DB/app, active
  Nginx); this is MA-owned evidence, not an SE target measurement. Later access
  and source/artifact receipts0ab7e7/f086fd also do not replace PG061's exact review.
- No separate NO-APP cause probe is included. It could isolate current target
  reload behavior, but adds a global reload and teardown surface, and cannot
  reconstruct WI058's unpersisted historical operands. Keep it separate if needed.
- Additional bounded BPF reads/fsyncs must fit inherited startup deadlines; local
  mocks do not prove target latency. Timeouts remain HOLD; no budget extension.
- App/DB25min, supervisor24min/view23min, restoration180s and existing command
  limits remain. Kernel/system failure can still prevent recovery; no durability
  or production-capacity guarantee is made.
- Rollback before native execution means withhold this candidate. No deletion,
  rollback command or automatic retry is authorized here. After an MA trial,
  follow the frozen cleanup/containment recipe, preserve claims/evidence/data and
  use the approved recovery channel if final checks fail. Never reuse WI055/WI060
  consumed claims or modify old evidence to show success.

## Follow-ups / Chain Check

WI060 **Blocks WI061**, not immediate native execution. MA has already assigned
PG061 concurrently; the next action is final independent review of the exact
bundle/freeze above and recipe, then an MA-only one-use trial if allowed and
current gates pass. No subdelegation or task messaging was performed by SE.
REQ007 remains active, and functional/backup/load/invitation work remains separate.

## Related Documents

- [WI060 summary](../user/WI-20261011-ATS-060-summary.md)
- [WI060 handoff](WI-20261011-ATS-060-handoff.md)
- [WI061 review gate](WI-20261011-ATS-061-handoff.md)
- [Frozen operational recipe](../../build/WI060-restricted-web/README.md)
- [REQ007](../user/REQ-20261011-ATS-007.md)
