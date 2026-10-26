---
version: 1.0
last_updated: 2026-10-11
project: ATS
owner: PG
category: evidence-pack
status: confirmed
dependencies:
  - path: WI-20261011-ATS-056-handoff.md
    reason: Approved independent review boundary
  - path: ../user/REQ-20261011-ATS-007.md
    reason: One bounded management-only read-only trial
  - path: WI-20261011-ATS-055-evidence-pack.md
    reason: Final SE candidate and provenance, independently verified below
  - path: ../../build/WI056-restricted-web-review/final-receipt.json
    reason: PG-owned execution receipts and before/after identities
---

# Evidence Pack: WI-20261011-ATS-056

## Summary

**FINAL: ALLOW_ONE_BOUNDED_READ_ONLY_TRIAL.** No open blocking finding remains
in this reviewed scope. The exact execution recipe and conditional restoration
recipe below are admissible for one MA-operated REQ007 trial, after fresh native
preconditions. This is not production approval or a native acceptance result.
PG performed no remote action and set no operator approval flag.

Verification owner: PG056. Final local run: 2026-10-11 20:17:22-20:17:45 KST.
Environment: Windows 11 build 22631, Python 3.14.3, PowerShell 7.6.5.
Branch `main`, HEAD `58b1011dad796017e47dbcc82a4225ca449bcdf6`; pre-existing dirty
worktree preserved. Exact file bytes, not HEAD alone, identify this candidate.

## Exact Pins

| Object | SHA-256 |
| --- | --- |
| Ordered twelve-file runtime bundle | `97c7c6bbc0129787424b794f4dcdbb55efb4ab60246a9ad705d0a9363d6a15a4` |
| WI055 freeze.json | `733af373ed54dec8049bb9b71cc40af3f9f18f530c5af86a7cab204aa5cf43e7` |
| WI055 README.md execution/recovery recipe | `fe45263fbf43645accd3ec23aaebaf4b46ff03c92275e0956fd7e006cdfbb57e` |
| WI055 SE Evidence Pack | `1ba06808aca121c69476a1b773679534ed7e2c686f3f1b0170355eddf1a1bc42` |
| PG final-receipt.json | `920f711830ad0466f37a373fb6400cfcd2e328031da4d5414f52dba684374118` |

[Freeze verification](../../build/WI056-restricted-web-review/freeze-verification.json):
71/71 manifest entries, twelve-file ordered bundle and four held-byte entry imports
match. Historical preservation is 84/84. Full candidate before/after snapshots
are identical. All four communicated final pins match actual files. Any changed
candidate/recipe byte invalidates this verdict; do not reseal after review.

## Findings And Resolution

| Finding | Final disposition and evidence |
| --- | --- |
| F1, P2: transport accepted duplicate status, extra data, evidence-free PASS or stderr | RESOLVED. `public-transport.ps1:15`, `:78`, `:133`, `:173` enforce recursive unique keys, step-specific exact schemas, complete final evidence and nonempty-stderr rejection. PG transport 13/13, HTTP 18/18 and independently rerun step schema 50/50 pass. |
| F2, P2: cleanup claim failure skipped restricted Nginx safety/restoration | RESOLVED. `web_session.py:299` separates unsafe/consumed control from a verified-control fresh write fault. Fixed Nginx/app/DB stops still run; a fresh claim fault continues independent restore checks and forces HOLD/FAILED. Unsafe/consumed/racing claims never authorize detach/start/result writes. PG negative tests and candidate full-restoration cases pass. |
| F3, P2: complete provisional result survived fsync failure and status returned false PASS | RESOLVED. `web_session.py:277`, `:374`, `:384`, `:481`; `durable.py:98`; `S7-postcheck.sh:16`. Attempt and result require exact-byte published digest markers. PG reproducer verifies provisional bytes really contain BOUNDED_PASS/cleanup PASS, injects result fsync failure, then calls actual status and requires exit 2/HOLD. Publication ordering/faults, marker rejection, attempt failure and actual S7 receipt-expression tests pass. |

[Original findings](../../build/WI056-restricted-web-review/early-findings.md) and
[fixture adaptations](../../build/WI056-restricted-web-review/fixture-adaptation.md)
remain available. New transport `Step` argument is fixture plumbing; original
rejection expectations are unchanged. The final F3 fixture adds a valid attempt
marker so rejection cannot pass merely because the attempt is already invalid.

Original RED evidence is preserved, not overwritten as green:

| Historical PG record | SHA-256 |
| --- | --- |
| transport-results-v1-failed.json, 4 failures of 8 | `15ac169c03e5ab28c893854468fe08ceb16802f709ec8a3b8b65d8a45272dd18` |
| independent-python-v1-failed.log, F2, 1 failure of 15 | `2b515bd5f10772ffffd4c4bf3534071f971a87951e79b476d3fe62860e0aad9c` |
| independent-python-f3-reproduced.log, F3, 1 failure of 16 | `8bc3a47980235cc9987c8dfea46cc3fc4c2e833a615fd104d5211849adb1432e` |

These files are under `build/WI056-restricted-web-review/`; the final receipt checks
their before/after equality. SE's separate pre-publication failure receipt also
remains in the frozen candidate. The annotated `independent-python-fsync-red.log`
is an earlier misnamed F2 copy, NOT the F3 reproduction.

## Scope / DoD Check

- [x] Final immutable candidate and executable recipes reviewed, with exact pins.
- [x] Fresh WI055 names, entry imports/claims and REQ007/WI056 approval binding checked.
- [x] Actual original-to-candidate delta and diagnostic baseline compared.
- [x] Independent negative cases exercise real candidate behavior at mocked native boundaries.
- [x] Real local child-process exit, receipt and raw stdin-byte checks rerun.
- [x] Durable preservation ordering, no-overwrite/link/identity checks and independent cleanup reviewed.
- [x] Runtime policy/BPF/DB comparisons remain unchanged; primary error remains visible.
- [x] Separate native preconditions and execution/restoration verdict supplied.
- [x] Writes restricted to PG056 folder and these two reports; no candidate/history edits.

The Nginx restoration-only `After` token ordering exception preserves multiplicity
and every other field. It is not policy/BPF normalization. AST comparisons of
`state_differences`, `project_state`, `state_result`, `StateDiagnostics` and
`public_result` match WI052/v2; inherited tests also cover read order and failure
short-circuiting. Original POST_STATE_CHANGED cause remains **NOT_DETERMINED**.

## Commands And Tests

Reproduction: `python -I -B build/WI056-restricted-web-review/run_final.py`.
This runner writes only the PG folder; it never calls an operational/native recipe.
[Final receipt](../../build/WI056-restricted-web-review/final-receipt.json) contains
all eight exact commands, exit codes, log hashes, test hashes and candidate snapshots.
Every command exited 0; all Python suites have zero failures/errors/skips.

| Independently executed suite | Result | PG evidence |
| --- | --- | --- |
| PG-owned Python negatives and publication tests | 23/23 PASS | `final-independent-python.log` |
| PG-owned process-boundary transport tests | 13/13 PASS | `final-independent-transport.log` |
| PG-owned HTTP child contract tests | 18/18 PASS | `final-independent-http.log` |
| Preserved WI042 suite | 106/106 PASS | `historical106.log` |
| Preserved WI052/v2 suite | 100/100 PASS | `diagnostic100.log` |
| Final WI055 candidate suite | 130/130 PASS | `candidate.log` |
| Inherited PowerShell harness, PG output destination | 38 assertions PASS | `final-inherited-powershell.log` |
| Actual pinned HTTP wrapper, SelfTest only | 8 assertions PASS | `final-sealed-selftest.log` |
| Inherited step-specific schemas | 50 assertions PASS | `final-inherited-step-schema.log` |

All logs above are in `build/WI056-restricted-web-review/`. Counts overlap and must
not be summed as unique tests. Owner-authored suite execution is distinguished
from PG-authored tests; neither is native acceptance. The inherited PowerShell
harness redirects generated fixtures to PG storage using AST root substitution.
HTTP is synthetic and SSH process construction is replaced, not executed. Three
local raw-stdin roundtrips are included in the 38 assertions, not remote proof.

## Execution Admissibility

**ALLOW for the exact frozen README and S0-S7/L1-L3 sequence, once only.**
MA may now substantiate `pg056_exact_bytes_pass` for these pins. This report does
not assert the other ten operator checks or create S3 approval. MA must personally
verify every check in `runtime.py:57` immediately before native progression:

1. Match all final pins and 71 manifest files; bind the current target/provider
   mapping, pinned SSH host key, boot identity, approved account/key and working
   recovery console. No target inference from historical notes.
2. Reconfirm management-/32 restrictions on 22/443, unchanged 80/ACME policy,
   no forwarding or usable loopback relay, Nginx without app proxy, no competing
   jobs/renewal/maintenance, and private logs without export/capture.
3. Confirm existing secret-use and incidental engine-write authority, unchanged
   WI036 artifact/read-only contract and accepted retained browser-context limits.
   Read-only describes application operations, not a claim of zero filesystem or
   database-engine writes. No login, mail, payment, jobs, catalog mutation,
   Public Listening or Official Download is admitted.
4. Run S0 read-only before writes. Only after PASS: L1 exclusive public upload,
   S1 staging, S2 current preflight. No new JAR, package, app build or secret upload.
5. Only after all eleven real checks: S3 once, current-boot approval expires in
   600 seconds; S4 once. No ambiguous start/approval retry. PENDING/DISPATCHED is
   not readiness; S5 must report admission PASS, app VERIFIED and view OPEN.
6. L2 Allowed and L3 Denied once each, normal TLS/no proxy, fence GET plus 17 cases;
   then only WI036 B01-B07 observations. Stop on any failure. S5 polls are bounded
   to every 5 seconds, at most 60 per phase. Do not broaden access for a denied probe.
7. S6 finish once, including early HTTP/browser failure when applicable; never
   repeat an ambiguous marker. End browsing/HTTP. Observe bounded S5 completion,
   then S7 and separate normal-TLS/no-proxy original-quarantine GET with exact body.

Any earlier HTTP/browser failure, timeout, ambiguous dispatch, missing/invalid
receipt or failed final/external check makes the overall trial HOLD even if later
internal cleanup reports PASS. Do not use S7 to erase an earlier failure.

## Restoration Admissibility And Limits

**ALLOW the frozen supervisor-owned conditional restoration, not arbitrary recovery.**
Supervisor 24 minutes, app/DB 25 minutes, view 23 minutes and restoration 180 seconds
bound the intended trial below 30 minutes under working kernel/systemd conditions.
No local synthetic result proves timers or target behavior.

Fixed app/DB stop and mask and restricted Nginx stop remain independent. Original
Nginx may start only after no-workers, exact-byte durable overlay retention,
same-directory no-replace detachment, reload, original configuration/service
comparison and Nginx configuration test succeed. Every independent cleanup check
must still be attempted. An unsafe/consumed claim permits safety stops only.

Final success requires the matching publication marker, complete diagnostics,
36 cleanup rows, FINISHED view, stopped/masked app and DB with PID0, exact original
service/TLS/listener/environment checks, and separate external quarantine HTTPS.
S7 uses the same `read_published` gate as status. A full provisional JSON file is
not completion authority. No reconstruction, overwrite or automatic retry of a
missing marker is allowed.

Publication guarantees are narrow: record fsync and durable staged digest/parent
sync precede final no-replace marker rename. The final marker name itself is not
directory-fsynced afterward; loss of that name after power loss is HOLD. This is
not proof of power-loss durability, Linux fsync/renameat2/cross-filesystem behavior,
SSH delivery, BPF/timer behavior or recovery on host/kernel loss. These remain
native verification limits, not open local F1/F2/F3 defects.

Local rollback is non-use while preserving evidence. Failed/unknown native cleanup
means HOLD and approved recovery-channel read-only inspection; no new recovery
action, permission repair, reset-failed, claim reuse, baseline replacement, restart
retry or history deletion is authorized by this report. If an observed limit is
exceeded, stop progression and retain fixed safe diagnostics.

## Reference Documents

Tier0 injected/checked: STD-001 core-principles, STD-004 documentation-standards,
STD-002 development-standards, STD-005 glossary. Tier1: security-policy projection,
access-control-policy, quality-gates, PG role. Tier2: approved REQ007, WI055/WI056
handoffs, WI045 actual failure, WI048/049 recovery, WI052/v2 and WI053 review,
final WI055 source/recipe/evidence. Skills: test and create-wi-evidence-pack.
Current source/evidence takes precedence over progress shorthand and old run logs.

## Handoff

WI056 is complete. It unblocks the **PG review gate only** for WI058 MA's one
bounded trial, conditional on the current target checks above. MA owns the native
sequence, result collection, access closure and downstream REQ tracking.
PG did not contact a remote target, inspect private configuration, change an
approval, stage/commit/push, modify memory or subdelegate.

[User summary](../user/WI-20261011-ATS-056-summary.md).
