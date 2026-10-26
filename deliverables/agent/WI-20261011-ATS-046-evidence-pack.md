---
version: 1.0
last_updated: 2026-10-11
project: ATS
owner: SE
category: evidence-pack
status: confirmed
dependencies:
  - path: WI-20261011-ATS-046-handoff.md
    reason: Approved HTTPS-only recovery preparation
  - path: WI-20261011-ATS-045-handoff.md
    reason: Separate original native failure integration
  - path: ../../build/WI046-quarantine-recovery/freeze.json
    reason: Exact candidate and recipe identity
---

# Evidence Pack: WI-20261011-ATS-046

> Purpose: Hand a small, locally tested original-HTTPS recovery tool to PG047 and MA.

## Summary

**TL;DR: LOCAL_READY_FOR_PG047; native NOT_RUN.** One public recovery script,
two exact MA recipe blocks, 23 owner tests and seven unchanged independent
regressions pass. Recovery never starts/unmasks app or MySQL and never rewrites
the failed trial. PG047 final exact-byte verdict and MA current gates remain required.

## Scope and DoD

- [x] Fresh exclusive recovery record; exact host/boot, public import and overlay pins.
- [x] Both durable private copies verified before any same-directory exclusive detach.
- [x] Ordered original service comparison after daemon-reload and before nginx -t/start.
- [x] At most one original Nginx start; uncertain post-start verification or receipt durability invokes one best-effort stop and all safety checks.
- [x] No private environment, SQL, DB contents, raw logs or certificate-content access.
- [x] Mock failures, code/recipe syntax, selected prior-byte preservation and freeze.
- [ ] PG047 final independent review; MA native recovery and separate observation.

## Evidence Pointers

| Pointer | Purpose |
| --- | --- |
| [recover.py](../../build/WI046-quarantine-recovery/recover.py), lines44/62 | Root metadata, held hash-verified public imports; only runtime/web_session/web_policy reused |
| Same file, lines86/109 | renameat2 with same parent FD and RENAME_NOREPLACE; strict stopped/masked checks |
| Same file, lines130/135/225 | Ordered recovery and once-only safety stop, including final receipt failures |
| [README](../../build/WI046-quarantine-recovery/README.md), lines41/97 | L1 single public file staging; L2 pinned held-buffer invocation |
| [Tests](../../build/WI046-quarantine-recovery/test_recover.py) | Actual run control flow with filesystem/service/process effects mocked |
| [Receipt](../../build/WI046-quarantine-recovery/local-receipt.json) | Exact commands, counts, red/green results and syntax boundary |
| [Preservation](../../build/WI046-quarantine-recovery/preservation.json) | Before/after matching selected old hashes |

## Identity

Freeze time: 2026-10-11T17:22:08+09:00. [freeze.json](../../build/WI046-quarantine-recovery/freeze.json)
pins nine files in order, including README, tests and receipts. Its bundle hashes
UTF-8 filename + NUL + exact file bytes; freeze excludes itself.

| Identity | SHA256 |
| --- | --- |
| recover.py | `7ab8be7b2b1b94df4eeabbc8be5c03620d0ad219749ce571014d587a92ad1d94` |
| Bundle | `8a249e7b2628bc7c8bc66628e90e79b820713cb4b7d26909820de4ff1b7e30e4` |
| freeze.json | `90b446e6aa4af89ac7aee184d7b0dc74b258a7d92427e3017d169bbb31bf418b` |
| README | `7449844a217c4f29bffa6661d53709688b5d9ff65b2e91825e0fb34d45f965cc` |
| L1 staging block | `0b5b3e6cb031513eecc99165640a8b9f518cbf4ac56f20d8a9e5a95109818ef7` |
| L2 invocation block | `0b32438b5d104f019b4fac9bdb5efa2fe79caf283a25be613d497624efc9129a` |

Block normalization is UTF-8/LF, one final newline, fences excluded, recorded in
[recipe-pins.json](../../build/WI046-quarantine-recovery/recipe-pins.json).
Only the three used imports, two overlay templates, old WI042 freeze and PG test
were rehashed. This is not a new whole-repository/JAR identity audit. No old file
was edited; no historical pin or failed receipt was replaced.

## Tests and Corrections

Windows local, SE execution on 2026-10-11. Test skill applied to the explicitly
authorized Python unittest scope; no Gradle/npm build or native recipe execution.

| Command | Result |
| --- | --- |
| `python -I -B -m unittest discover -s build/WI046-quarantine-recovery -p 'test_*.py' -v` | 23 PASS, 0 errors/skips, 0.077s, exit0 |
| `python -I -B build/WI047-quarantine-review/test_recovery_review.py` | 7 PASS, 0 errors/skips, 0.024s, exit0 |
| PowerShell AST ParseInput of both README blocks | 2 PASS; bodies not executed |
| Python compile of two files and both embedded Python bodies | 4 PASS; bodies not executed |

Logs: [owner](../../build/WI046-quarantine-recovery/owner-tests.log),
[PG regression rerun](../../build/WI046-quarantine-recovery/pg-regressions.log),
[F01 red reproduction](../../build/WI046-quarantine-recovery/pg-red.log).
The PG rerun is SE reproduction, not a substitute for PG final review.
An initial syntax-check harness command incorrectly allowed PowerShell to expand
`$body`; extraction asserted before embedded execution. A literal here-string
corrected only the checker and passed. This failure is retained in the receipt.

PG047-F01 was reproduced against draft `f2fea7ea87f69074b7dfee12cacb8de3b28b255d8db1eb56d379b0ff7eb79a46`:
seven methods, six passing, two failing subcases, exit1. Receipt creation/fsync
failed after the earlier stop branch, leaving Nginx running. The current candidate
calls the same guarded stop after receipt failure, includes already VERIFIED start,
and never retries a stop. All four safety actions are attempted even after errors.
Disk success bytes are now explicitly PENDING_DURABILITY. Only bounded stdout
with exit0 and receipt_durability PASS can establish that invocation's outcome;
retained partial/provisional bytes cannot. No evidence overwrite/retry was added.

Tests also cover collision, wrong pin/host/boot, symlink/hardlink/owner/mode/parent,
running service/jobs, partial copy/detach, command-order/MySQL mismatch, nginx-t
failure, ambiguous start, stop failure and no environment/DB helper calls.
Distinct-device behavior is modeled; real renameat2 argument construction uses
one mocked directory FD. Neither establishes real Linux durability.

## Observations and Limits

The actual uname correction came from MA's fresh target observation, not a
runtime.HOST constant or provider display name. MA's 17:14:47 root ownership,
mode, link-count and ancestry observation matches the script assumptions, but
the worker performed no target access. Current gates still execute inside L2.
The inherited listener_gate(False) permits original 80/443 and rejects app/DB
ports; recovery additionally compares the original complete listener tuples.

Original trial remains HOLD/POST_STATE_CHANGED, cleanup FAILED; that primary
cause is NOT_DETERMINED. Cross-device EXDEV is strongly supported by MA's device
observations and the old rename implementation, but raw errno was not captured.
Do not equate HTTPS recovery with trial success, unchanged DB data, browser/API
success, new app authorization or public opening. Provider firewall stays unchanged.

## Risks and Rollback

Before execution, rollback is withholding L1/L2. After a claim or partial, retain
all copies, suffix files and receipts; HOLD and inspect read-only through MA.
Never remove a collision, restore the temporary overlays automatically, restart,
reset-failed or rerun the consumed trial. Further repair needs explicit scope.
Primary alarm120s handles INT/TERM/HUP/ALRM; cleanup uses finite command timeouts,
not an absolute guarantee under blocked kernel I/O, SIGKILL or host loss.
Receipt failure may leave provisional/partial bytes; preserve them and follow
stdout HOLD, with no inferred success after transport loss.

## Reference Documents

Tier0 retained in order: core-principles STD-001, documentation-standards STD-004,
development-standards STD-002, glossary STD-005. Tier1 retained SE/security policy.
Tier2: approved REQ006 recovery amendment, WI046/WI045 handoffs, frozen WI042
helpers, test and create-wi-evidence-pack skills. No subdelegation.

## Related Documents

- [WI046 Handoff](WI-20261011-ATS-046-handoff.md): Authority and exact mutation boundary.
- [WI046 Summary](../user/WI-20261011-ATS-046-summary.md): Concise user-facing outcome.
- [PG047 Handoff](WI-20261011-ATS-047-handoff.md): Immediate next independent review.
- [PG047 Draft Finding](../../build/WI047-quarantine-review/draft-findings.md): Preserved F01.
- [WI045 Handoff](WI-20261011-ATS-045-handoff.md): MA native result integration; old failure remains separate.
