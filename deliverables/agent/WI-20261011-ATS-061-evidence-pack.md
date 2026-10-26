---
version: 1.1
last_updated: 2026-10-11
project: ATS
owner: PG
category: evidence-pack
status: confirmed
related_wi: WI-20261011-ATS-061
dependencies:
  - path: WI-20261011-ATS-061-handoff.md
    reason: Approved PG ownership and independent review scope
  - path: ../user/REQ-20261011-ATS-007.md
    reason: Approved bounded repair and exact-byte execution gates
  - path: WI-20261011-ATS-058-evidence-pack.md
    reason: Preserved completed HOLD and unknown BPF cause
---

# Evidence Pack: WI-20261011-ATS-061

## Summary

**ALLOW_ONE_BOUNDED_READ_ONLY_TRIAL**, restricted to the exact final WI060
freeze/bundle below and the existing MA current-target/maintenance/recovery gates.
F061-01/F061-02 are closed on these bytes; N061-01 attribution is corrected.
Nine independently executed local verification groups passed with unchanged
pre/post identities. Native remains NOT_RUN by PG; SMTP is outside this trial.

## Final Same-Byte Verdict

Final-ready authority: MA relayed SE060 FINAL FROZEN with the two exact pins and
MA's independent81-file match receipt `f1fff1`. PG independently hashed the actual
local bytes before and after tests, not merely the MA/SE receipt. Final local
review window: **2026-10-11T21:19:41.559059+09:00 through
21:20:02.873428+09:00**, Windows11 build22631, Python3.14.3, PowerShell7.6.5.

| Object | SHA-256 |
| --- | --- |
| freeze.json | `e0896db9fce85f9a1d802b2d85e412c371f3d447805f8b473c6171c85da31d7a` |
| Ordered runtime bundle | `3f9edd75b5ebc32281cc651d45fe16861a2dd2a19bc8c5dd5deac24494cc4316` |
| README operational recipe | `2f230fcf6c724ff2df89c9d80e7097987ba8c9abbcee750a1dd1669e40e519bd` |
| PG final-receipt.json | `5a56b058e5a87c159a3ade0b11452d5d9712e2ab6ce28c45255f22ef22b5af6c` |

[Final receipt](../../build/WI061-review/final-receipt.json) records all command
group results, log hashes, both identity observations, timestamps and limitations.
[PG verifier](../../build/WI061-review/final_review.py) reproduces the checks:

```powershell
python -I -B build/WI061-review/final_review.py
```

No candidate helper that writes/reseals the candidate was run. All generated
review logs/receipt are under build/WI061-review. Native shell recipes were
parsed/read but never executed; HTTP tests use SelfTest or synthetic boundaries.

| Independent execution by PG | Cases / assertions | Result |
| --- | ---: | --- |
| PG061 original defect regressions | 8 | PASS |
| PG061 requested schema/overlay/pregrant checks | 5 | PASS |
| WI060 candidate Python suite | 141 | PASS |
| PG061 PowerShell schema | 47 | PASS |
| Candidate PowerShell parser/child/byte checks | 38 | PASS |
| Candidate step schema, including POSSIBLE state | 53 | PASS |
| Sealed HTTP SelfTest | 8 | PASS |
| Inherited retargeted transport reproducer | 8 | PASS |
| Inherited retargeted HTTP reproducer | 18 | PASS |

Counts overlap; do not sum as unique coverage. All nine commands exited0.
Exact verification covered81 candidate files,12 ordered runtime files,32
recipe/transport hash bindings, the four loader module pins and161 preserved
historical files. Nine original security/cleanup function ASTs matched WI055
after the WI identity rename: policy, attachment_snapshot, db_counts, cleanup,
final_baseline, secure, read, exclusive, compare_probe. Protected comparison
logic remains exact; the reviewed control flow changes do not rebaseline IDs.

Closure rationale:

- F061-01: runtime.py:1265 performs the one final POLICY/BPF/DB group in the
  common finally whenever grant publication was attempted. Grant write/fsync,
  POST_GRANT, readiness, PRE_VIEW and view failures are covered. Each reader is
  attempted; secondary failures use setdefault and cannot replace the primary
  error. Cleanup proceeds after final observation errors. Pregrant failures do
  not claim release or fabricate an unavailable final comparison.
- F061-02: runtime.py:1226 records JAVA_RELEASE_POSSIBLE before the grant write,
  then line1230 records JAVA_RELEASED immediately on successful return, before
  diagnostics. A visible grant followed by fsync failure remains POSSIBLE/HOLD;
  neither label asserts Java startup. The new enum is admitted as failure
  evidence by both schemas, while final success still requires VERIFIED and
  every required diagnostic PASS.
- N061-01: README now attributes inherited F1/F3 to PG056 and distinguishes
  historical reproducer execution from this PG061 final review.
- The requested no-false-PASS, syntax/intent-before-overlay, no view reload,
  and independent pregrant cleanup checks passed again on the final bytes.

Scope: **one new MA-operated management-only read-only web trial**, with fresh
current target/boot/SSH peer/firewall/maintenance/recovery checks and the pinned
recipe. Any changed byte requires re-review; no prior claim may be reused.
No auth/write/mail/payment, public access, extra cause probe, package, source
rebuild or destructive action is admitted. Historical WI058 remains HOLD and
its BPF cause/semantic equivalence remain NOT_DETERMINED. Local synthetic tests
do not establish native crash recovery, fsync durability, HTTP/mobile/capacity,
SMTP delivery or production acceptance.

Latest separate MA update: provider outboundTCP587 was explicitly user-approved
and UI save succeeded; inbound was reported unchanged with fresh verification
pending. This is MA-owned state, not a PG observation, and does not enable SMTP
in the frozen web trial. All existing application mail/egress restrictions remain.

Chain: WI061 final review is complete. MA may perform the next bounded trial only
after its current gates; WI062 receives actual runtime/cleanup receipts. This
does not close REQ007 or unblock feature runtime without the trial result.
No subdelegation or direct message to another task was made.

## Initial Findings (Historical)

The following interim findings and initial test failures are preserved as the
review history. Their final status is superseded only by the exact-byte closure
above; they are not open against the approved final candidate.

### F061-01: Post-Grant Failure Skips The Independent Final DB Comparison

Severity: medium, blocks final ALLOW until repaired or explicitly bounded by MA.
`build/WI060-restricted-web/runtime.py:1223` can raise after `grant.json` is
published. The final group at line1258 is inside the later view block, so this
new failure path exits with POST_VIEW/DB NOT_RUN. Java admission has already been
released; the prestart helper may consume the grant before the BPF failure.
The receipt cannot establish an unchanged DB over that admitted interval.

Reproducer: `test_independent.py::test_post_grant_fault_attempts_independent_final_db`.
Inject exact BPF ID drift only at POST_GRANT. Observed: grant exists, HOLD with
POST_STATE_CHANGED, final DB NOT_RUN. Preserve the primary failure and exact
comparison; attempt the independent final DB observation on all post-grant exit
paths where a DB baseline exists. A secondary observation failure must not skip
cleanup or replace the primary failure. Do not convert missing evidence to PASS.

### F061-02: Post-Grant Error Underreports Released Java Admission

Severity: medium. At runtime.py:1221-1225, `app_start='JAVA_RELEASED'` is assigned
only after the newly added POST_GRANT snapshot. If snapshot collection or its
private write fails, a complete grant exists but the result still says ATTEMPTED.
This misstates the last completed admission action, even though HOLD is retained.

Reproducer: `test_independent.py::test_post_grant_fault_cannot_underreport_java_release`.
Inject private POST_GRANT snapshot write failure. Observed: grant exists,
app_start ATTEMPTED, HOLD. Record released/possibly-released admission before the
next fallible diagnostic step; account for a grant write that succeeds before a
later fsync failure, without claiming that Java actually started.

### N061-01: Preserve Historical Review Attribution

The in-progress README at lines69/73 calls inherited WI056 F1/F3 repairs
"PG061". Those findings and their original review authority belong to WI056.
The local verifier also still points to the historical WI056 test directory.
Correct the description and distinguish inherited reruns from independent WI061
tests before the final freeze. This is not an assertion that unfinished pins or
copied test counts are final SE claims.

## Scope / DoD Check

- [x] Read skill-generated handoff; confirm injected Tier0 and current approval.
- [x] Read current security policy before any verdict.
- [x] Inspect actual WI055/WI060 diffs and changed failure paths.
- [x] Write independent tests only under build/WI061-review.
- [x] Preserve exact BPF ID/direction/multiplicity rejection and old evidence.
- [x] Receive SE final-ready/freeze/pins through MA.
- [x] Resolve findings and rerun independent final-byte regressions.
- [x] Verify manifest, loader, transport and recipe pins against final bytes.
- [x] Issue final freeze-bound ALLOW and notify MA of the next gate.

## Initial Evidence Pointers

- [Independent tests](../../build/WI061-review/test_independent.py): eight cases;
  both findings are failing desired-behavior assertions.
- [Initial test log](../../build/WI061-review/initial-independent-tests.log):
  8 run, 6 passed, 2 failed, exit1.
- [Initial observed hashes](../../build/WI061-review/initial-input-hashes.json):
  after-test local observation only, not an immutable snapshot or final freeze.
- `runtime.py::StateDiagnostics.compare_group`: later group checks run despite
  earlier error/drift, preserving the first exception.
- `web_session.py::Session.prepare/view`: overlay creation moves to preparation;
  the view path no longer calls daemon-reload. Fixed maintenance/preflight gates
  still apply; this does not prove any native BPF cause or semantic equivalence.

## Commands & Outputs

Review environment: Windows, Python3.14.3, PowerShell7.6.5. Local observation
timestamp2026-10-11T21:05:07+09:00; test execution preceded this timestamp. Branch
main, HEAD58b1011dad796017e47dbcc82a4225ca449bcdf6, extensive pre-existing dirty
state. HEAD is not the candidate or runtime identity.

```powershell
python -I -B -m unittest discover -s build/WI061-review -p test_independent.py -v
```

Test skill applied to the scoped Python unittest suite, not unrelated Gradle or
frontend suites. All target process/network/filesystem boundaries in the runtime
fixture are mocked; Python bytecode writes are disabled. Test counts are not
coverage or native systemd/BPF/fsync evidence. No external tool is installed.

Other commands were targeted local Get-Content/rg, git status/rev-parse,
git diff --no-index, Get-FileHash and version/date queries. Git no-index exit1
means differences, not a failed runtime test. Manual writes use apply_patch.

## Initial MA Receipt Boundary

The latest message reports current provider inbound management22/443 and
80/ACME, outboundTCP80/443/53 and UDP53/123, verified SSH host pin/key/sudo,
fresh root recovery-console login, unchanged native JAR01d562..., and local
641/336/143 identity at20:59:32. These are MA-owned bounded observations, not
PG target checks or trial evidence; per-receipt precise timestamps were not
provided. No native WI060 action has occurred; SE ready remains pending.
The candidate is WI060, not WI066. No private values were inspected or copied.

The supplied target baseline is Ubuntu24.04, systemd255.4-1ubuntu8.17,
kernel6.8.0-146-generic, app/DB off and masked, no Java, Nginx active. It is not
an independent observation by PG and must be rechecked by MA at execution.

## Initial Requested Focused Checks

MA subsequently requested specific checks of the relaxed diagnostic prefix rule,
early Nginx overlay activation risk, and independent pregrant restoration.
These are scoped to the changed architecture; no unrelated gate was introduced.

- [Requested Python tests](../../build/WI061-review/test_requested.py):5 methods
  pass, including per-row ERROR/NOT_RUN cases, malformed/reordered/duplicate rows,
  configuration/marker/overlay fault points and pregrant restoration faults.
  [Rerun log](../../build/WI061-review/requested-python-rerun.log).
- [Independent PowerShell schema tests](../../build/WI061-review/test_status_schema.ps1):
  47 assertions PASS against the then-current sealed schema.
  [Log](../../build/WI061-review/requested-schema-tests.log).
- A failed observation followed by a later successful DB observation is retained
  as HOLD evidence. No tested ERROR, NOT_RUN, missing step, boolean sequence,
  unknown field or contradictory final status can become BOUNDED_PASS.
  This is structural validation, not authentication of fabricated all-PASS data.
- Preparation writes web.conf and passes nginx syntax testing before recording
  nginx-touched and installing the overlay. No service start/reload is called by
  prepare itself. A syntax/config/marker failure does not install the overlay.
  The only normal start is in view, after admission; Restart=no remains set.
- After an early prepared-overlay/pregrant failure, restore attempts all36
  cleanup rows, including independent app/DB stop/mask and both overlay-retention
  steps. Failed overlay retention, baseline-byte validation or configuration test
  prevents the original Nginx start. Primary BPF failure is retained. A partial
  unknown overlay is containment/HOLD, not permission to restart it.

The first requested-suite run had a PG harness assertion error: injected
MYSQL_STOP failure replaces the action itself, so checking for a successful mock
ctl invocation was incorrect. The test now checks that row's FAILED status and
continued MYSQL_MASK. Initial failed log is retained; no candidate fix was made.

Upstream v255.4 documentation was checked for semantics, not native verification:
[systemctl manager reload](https://raw.githubusercontent.com/systemd/systemd-stable/v255.4/man/systemctl.xml)
describes unit/dependency reload, distinct from service reload;
[ExecStopPost](https://raw.githubusercontent.com/systemd/systemd-stable/v255.4/man/systemd.service.xml)
covers unexpected exits and incomplete startup; and
[BindsTo with After](https://raw.githubusercontent.com/systemd/systemd-stable/v255.4/man/systemd.unit.xml)
ties the dependent service to its supervisor's active lifetime. Thus the source
ordering does not request invalid-config activation after a pre-view crash.
This is a code/document inference, not a proof of Ubuntu's patched package or
crash recovery on the current target. Existing exclusive maintenance, current
target/recovery and exact-byte gates remain necessary. No claim is made that
unrelated root operations cannot restart/reload a service.

Final-freeze binding and reruns are still required. The two reproduced post-grant
findings above are not superseded by these passing focused checks.

## Reference Documents (Tier 0-2)

Handoff precondition passed. Applied create-wi-evidence-pack skill to this report.
All four Tier0 documents were fully injected by the delegating user:
STD-001 core-principles v2.0, STD-004 documentation-standards v1.5,
STD-002 development-standards v2.3, STD-005 glossary v1.6. AGENTS.md was also read.

Tier1 read: PG role, security-policy v2.8, access-control-policy, quality-gates,
evidence-pack-standard. Tier2 read: create-wi-evidence-pack/test/validate-docs
skills, current decision entry, REQ007, WI060/WI061 handoffs, relevant WI058
completion and WI062 current receipt projections. WI059 design is a separate
feature successor, not authority for this trial. Conditional frontend material
and handoff templates were not needed; no delegation occurred.
Rule source remains .claude/config/context-injection-rules.json as stated in
the handoff. Assignee PG; task security/review; required tiers0/1.

## Interim Risks / Rollback (Historical)

Current findings do not show a false security PASS; fail-closed rejection remains.
They identify missing final evidence and underreported admission in new error paths.
No diagnosis of WI058's old BPF ID change is possible from its absent raw snapshot.
Do not treat ID churn as harmless or replace exact equality with tag equivalence.

PG owns only build/WI061-review and the two WI061 reports. Candidate/history,
application source, native host, credentials and unrelated files remain untouched.
Rollback means withholding this preliminary approval and having SE repair its own
candidate, then obtain fresh pins and independent review. Do not rerun an old
claim, reseal on PG's behalf, overwrite historical results, or delete data.
A minimal no-app cause probe requires a separate concrete SE proposal and review;
none has been authorized by this report.

## Interim Follow-ups / Chain Check (Historical)

WI060 remains the producer. MA relays these findings and then final-ready/pins.
WI061 continues to block the MA native trial. WI062 may record this interim HOLD
but must not close WI061/REQ007 or imply feature-runtime permission. No direct
message or subdelegation was sent to another task.

## Related Documents

- [Owned handoff](WI-20261011-ATS-061-handoff.md)
- [User summary](../user/WI-20261011-ATS-061-summary.md)
- [Approved REQ007](../user/REQ-20261011-ATS-007.md)
- [Prior completed HOLD](WI-20261011-ATS-058-evidence-pack.md)
- [Final SE evidence](WI-20261011-ATS-060-evidence-pack.md)
