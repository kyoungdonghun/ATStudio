---
version: 1.0
last_updated: 2026-10-11
project: ATS
owner: SE
category: evidence-pack
status: confirmed
dependencies:
  - path: WI-20261011-ATS-055-handoff.md
    reason: Approved delegated code and recipe scope
  - path: ../user/REQ-20261011-ATS-007.md
    reason: Approved staged execution and exclusions
  - path: WI-20261011-ATS-054-evidence-pack.md
    reason: Completed WI052 v2 diagnostic baseline
  - path: ../../build/WI055-restricted-web/freeze.json
    reason: Exact review candidate identity
---

# Evidence Pack: WI-20261011-ATS-055

## Summary

**SE LOCAL READY after PG056 F1/F2/F3 repair; final independent re-review required. Native NOT_RUN by SE.**
Fresh WI055 candidate repairs the HTTP process contract and cross-filesystem
overlay preservation, retains WI052/v2 state diagnostics, and supplies frozen
MA-only operation recipes. This is not PG approval, native acceptance or REQ007 closure.

## Scope / DoD Check

- [x] New candidate and two reports only; original WI042/WI046/WI048/WI052 history preserved.
- [x] Explicit checker success exit plus shared child-process exit/receipt contract; absent, malformed, duplicate-key, failed or wrong-mode receipts cannot PASS.
- [x] Exact-byte durable backup precedes same-directory no-replace overlay detachment; partial copy/fsync/identity faults retain HOLD and evidence.
- [x] Independent cleanup steps retained; claim/path failure still attempts Nginx stop and app/DB stop and mask.
- [x] Provisional attempt/result bytes are not completion authority; status and S7 require exact-byte publication markers.
- [x] Fresh claims/services/control/tools, pinned held-byte imports, management-only read contract and lifetime limits.
- [x] Unchanged policy/BPF/DB comparator expressions, BPF IDs, read order/short circuit, bounded diagnostic projection; primary failure preserved.
- [x] Executable public-upload/initial-current-preflight/staging/admission/start/status/HTTP/finish/postcheck recipe with explicit exit handling and byte transport.
- [x] Local regressions, preservation snapshots and final hashes recorded.
- [ ] PG056 review and MA current native execution: separate next gates, not SE-owned work.

## Exact Identity

Candidate: [build/WI055-restricted-web](../../build/WI055-restricted-web/README.md).

| Identity | SHA-256 |
| --- | --- |
| Ordered twelve-file runtime bundle | `97c7c6bbc0129787424b794f4dcdbb55efb4ab60246a9ad705d0a9363d6a15a4` |
| Final freeze.json | `733af373ed54dec8049bb9b71cc40af3f9f18f530c5af86a7cab204aa5cf43e7` |
| Final README.md | `fe45263fbf43645accd3ec23aaebaf4b46ff03c92275e0956fd7e006cdfbb57e` |

This repaired bundle supersedes progress-only bundles
`2dd36d1f76df9c4011b9f90c194f2619fd3f112559a3e1a7ad90fdd6eb349378`
and `387936fcb097f9bc316602564b15f82c9caabe14ec74206281fa84d8fbd4d91e`.
The intermediate freeze `689de142a39afacfe3b9b0d7fbbee6e858c3066431caabc5da9c4353ed74650c`
is superseded by the PG056 repairs below, not authorized for native execution.
The F1/F2 intermediate bundle `47597d5c656f96e5fe8c4a15f79dcd5f6bda730860a94604c09d1dd1b061c0e6`
is superseded by F3 publication completion and is not the final runtime identity.
Use only the final freeze after independent PG acceptance.
The final manifest contains71 supporting/runtime files. Runtime and recipe byte
hashes were compared before/after final evidence sealing and remained unchanged.

Branch/main and HEAD at SE start: `58b1011dad796017e47dbcc82a4225ca449bcdf6`.
The checkout was already dirty; no pre-existing edit was reverted, staged or committed.
[Before](../../build/WI055-restricted-web/preservation-before.json) and
[after](../../build/WI055-restricted-web/preservation-after.json) contain exactly84
equal historical file hashes across WI042, WI046, WI048 and WI052 including v2.

## Evidence Pointers

- [HTTP checker](../../build/WI055-restricted-web/management-check.ps1), [child contract](../../build/WI055-restricted-web/http-child.ps1), [pinned wrapper](../../build/WI055-restricted-web/invoke-http.ps1): real child ExitCode, strict receipt schema/order, no inherited LASTEXITCODE dependence.
- [Durability helper](../../build/WI055-restricted-web/durable.py): root/group ownership, mode/single-link/no-symlink checks, identity before/after, file/directory fsync and renameat2 no-replace. Native Linux semantics remain unproven.
- [Session](../../build/WI055-restricted-web/web_session.py): restore independence, fixed-unit safety cleanup on claim failure, honest final status, bounded diagnostics.
- [Runtime](../../build/WI055-restricted-web/runtime.py): unchanged WI052/v2 comparisons and diagnostic projection, primary-error retention, new packaging identity.
- [Entry](../../build/WI055-restricted-web/entry.py): exact path and held pinned module bytes; no old helper imports or LOCAL_ONLY operational entry.
- [Recipe](../../build/WI055-restricted-web/README.md), [shared shell runner](../../build/WI055-restricted-web/invoke-step.ps1), [transport](../../build/WI055-restricted-web/public-transport.ps1): one ProcessStartInfo transport using exact UTF-8/LF bytes and a final newline. No live transport executed by SE.
- [Semantic delta](../../build/WI055-restricted-web/semantic-delta.patch): runtime/session difference from WI052/v2 after explicit execution-identity normalization. Other packaging/support files are listed in freeze.

## Specific Findings Included

**F055-01, restoration only:** WI048 already established the Nginx `After` token-order
exception after the earlier recovery failure. WI055 explicitly reuses that comparator
for restoration/postcheck, preserving token multiplicity and every other property,
including ordered Exec lists. No runtime security comparison is normalized.

**F055-02, primary error:** the inherited receiver-close finally path overwrote the
primary error. WI055 retains an existing primary code with `setdefault` while still
forcing HOLD. A real mocked run tests DB_AGGREGATES surviving receiver-close failure.

**F055-03, transport:** MA reported a current PowerShell/heredoc failure before Python,
with CRLF/final newline suspected, not established. Shared byte transport replaces
text-pipeline dispatch. Three local raw-stdin roundtrips prove emitted/received
bytes locally only, not the current SSH or remote-shell outcome.

## Commands And Tests

Owner/date/environment: SE, 2026-10-11, Windows11 build22631, Python3.14.3,
PowerShell7. Test skill applied to these Python/PowerShell maintenance tools;
no Gradle/frontend build/test or clean was needed or authorized.

`python -B build/WI055-restricted-web/verify_local.py` captures each local command,
exit status and logs. [Local receipt](../../build/WI055-restricted-web/local-receipt.json).

| Suite | Result | Log |
| --- | --- | --- |
| Preserved historical WI042 | 106/106 PASS | [historical106](../../build/WI055-restricted-web/historical106-tests.log) |
| Preserved WI052/v2 | 100/100 PASS | [diagnostic100](../../build/WI055-restricted-web/diagnostic100-tests.log) |
| WI055 candidate | 130/130 PASS | [candidate](../../build/WI055-restricted-web/candidate-tests.log) |
| PowerShell parse/process/receipt/byte transport | 38 assertions PASS | [PowerShell](../../build/WI055-restricted-web/powershell-tests.log) |
| Sealed HTTP wrapper SelfTest | 8 assertions PASS | [SelfTest](../../build/WI055-restricted-web/sealed-selftest-tests.log) |
| PG-authored Python reproduction, SE rerun | 16/16 PASS | [PG Python](../../build/WI055-restricted-web/pg-python-recheck-tests.log) |
| PG-authored transport reproduction, SE rerun | 8/8 PASS | [PG transport](../../build/WI055-restricted-web/pg-transport-recheck-tests.log) |
| PG-authored HTTP reproduction, SE rerun | 18/18 PASS | [PG HTTP](../../build/WI055-restricted-web/pg-http-recheck-tests.log) |
| Additional step-specific receipt contracts | 50 assertions PASS | [Step schemas](../../build/WI055-restricted-web/step-schema-tests.log) |

Python runs have zero failures/errors/skips. Candidate130 contains100 retargeted
diagnostic methods including15 previously PG-authored cases;30 new methods cover
preservation/low-level wrapper paths, loader entry, recovery, recipe compilation,
identity and AST comparator order. Counts overlap and are not summed as unique tests.
The actual HTTP checker Allowed/Denied main paths run in real PowerShell children
with only HTTP replaced by pure fixtures. Native socket/process/filesystem boundaries
in Python are mocked; no Linux fsync, renameat2, cross-device, timer/BPF or server proof.

During retargeting, initial copied fixtures produced12 failure records: old finish
markers/unit names and missing synthetic After fields. Those fixtures were corrected;
a later new entry-dispatch test needed an absolute Windows test path. Neither run
was a native failure or permission to relax a production comparator. Final logs above
are separate evidence; prior original failure records/files remain unchanged.

## PG056 Repair Addendum

PG's [early findings](../../build/WI056-restricted-web-review/early-findings.md)
and [freeze request](../../build/WI056-restricted-web-review/freeze-request.md)
are preserved. Initial PG transport4/8 and Python14/15 results remain historical
review evidence, not rewritten by these SE reruns.

**F1:** public-transport now rejects duplicate keys before conversion, applies an
explicit receipt contract per S0-S7/L1 step, rejects unknown/private fields recursively,
requires complete success evidence, and rejects nonempty stderr without exporting it.
The public codes/steps/diagnostic plan whitelist is generated from the candidate's
AST by the local sealer and is pinned with the transport. Safe HOLD diagnostics are
validated and serialized, never forwarded as unchecked raw JSON. READY is the exact
five-field PENDING envelope; it is no longer incorrectly projected as a final missing-
diagnostics HOLD. This is result-handling only, not a state-comparison relaxation.

**F2:** control validation and pre-existing/racing claim detection are separate from
a fresh claim write/fsync failure. Verified-control fresh write failure no longer
short-circuits Nginx stop or remaining independent restore checks; final status stays
HOLD/cleanup FAILED even when safe restoration succeeds. Unsafe control, consumed
claim and FileExistsError race paths attempt fixed Nginx/app/DB safety only, never
detach, start or result writes. Original primary error still wins.

**F3:** the full-result-write/fsync failure was reproduced by PG and in the SE
full run (15/16 PASS). That failure is preserved separately in
[failed log](../../build/WI055-restricted-web/pre-publication-fix-pg-python-recheck-tests.log)
and [failed receipt](../../build/WI055-restricted-web/pre-publication-fix-local-receipt.json).
Neither complete result bytes nor in-memory HOLD alone establish finalization.
The writer now fsyncs the record, rereads exact bytes, durably saves a staged
SHA-256 marker including parent-directory fsync, rechecks record bytes, then
publishes that marker by final no-replace rename. No fallible I/O follows that
boundary. Status and S7 call the same `read_published` exact-byte gate. The attempt
record uses the same rule so its write failure cannot later become a successful
final result. Missing/partial/mismatched markers and any prior publication failure
fail closed without overwrite/retry. Failed bytes and staged markers remain evidence.

Seven added methods cover publication ordering/faults, marker validity, full result
write failure followed by real status, unpublished attempts, attempt fsync failure,
and evaluation of the actual S7 receipt expression. Successful publication remains
covered, not only failure. Candidate fixtures model the new completion marker;
historical test sources and PG reproducers remain unchanged. The final marker name
is deliberately not directory-fsynced after publication: power-loss loss of that
name yields HOLD. This establishes bounded completion authority, not native
power-loss durability or automatic recovery of a lost marker.

Reproductions: PG Python methods run via unittest discovery, avoiding the original
script's report write. PG PowerShell scripts are copied under `pg-recheck` with only
candidate/output path retargeting. No PG-owned file is modified. Their embedded
PG056 author label is source attribution; SE owns this rerun, not a new PG verdict.
Three added Python methods exercise full36-step claim-fault restoration, unsafe/
consumed/racing claim denial, and exact READY CLI projection. Step-schema50 tests
cover good phase-specific receipts and nested/missing/contradictory failure inputs.

## MA Observations And Limits

MA separately reported refreshed current host trust/atadmin/sudo, the sole scoped
key, live noVNC recovery, provider22/443 current-source restrictions, original
quarantine200 and inactive masked DB/no app. These are attributed MA observations,
not SE verification or a new target write. The recipe still requires a fresh current
preflight and PG056 exact-byte review. Mail-sender discussion does not expand this
read-only trial. Old POST_STATE_CHANGED cause remains NOT_DETERMINED.

## Risks / Rollback

- Linux durability and actual original HTTPS recovery require MA target testing;
  internal restore/status never substitutes for certificate-validated external HTTPS.
- The inherited strict post-view BPF identity check may HOLD after daemon-reload;
  retain diagnostic evidence without suppression or baseline replacement.
- Supervisor24min, app/DB25min, view23min and restore180s remain. Host/kernel loss
  or slow cleanup can prevent restoration/completion. A missing receipt is HOLD.
- Receipt durability, complete write audit, non-management vantage, browser network
  completeness, real-device behavior, functional accounts/media and production load
  are not established. No actual billing/public opening is permitted.
- Local rollback is non-use of this new candidate, preserving it and history.
  Native cleanup only follows the reviewed once-only recipe; no delete/reset/retry,
  new recovery, permission repair or secret rewrite is authorized by this report.

No remote/browser/SSH/cloud/DB/secret access, app start/build, purchases,
commits/pushes, history deletion, memory update, subdelegation or shared-doc edit by SE.

## Reference Documents And Handoff

Tier0 injected: STD-001 core-principles, STD-004 documentation-standards,
STD-002 development-standards, STD-005 glossary. Tier1: security-policy,
quality-gates and SE role. Tier2: approved REQ007/WI055, WI054 completion,
WI052/v2 sources/tests, WI042 sources/recipes, WI046 and WI048 recovery code.
Applied test and create-wi-evidence-pack skills; handoff existence verified first.
Memory was used only for navigation and older approval-boundary caution; current
REQ007/handoff/source evidence governs this task.

**Chain:** WI055 candidate is ready for WI056 independent PG review; only reviewed
exact bytes and current preflight permit MA WI058 trial. No subdelegation was performed.
PG should verify freeze, source/recipe pins and84 historical hashes, review F055-01/02/03 and PG F1/F2/F3,
rerun tests without executing native recipes, and explicitly gate Linux-only limits.

- [WI055 Summary](../user/WI-20261011-ATS-055-summary.md): User-facing result.
- [WI055 Handoff](WI-20261011-ATS-055-handoff.md): Exact permitted write scope.
