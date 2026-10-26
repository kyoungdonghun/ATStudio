---
version: 1.0
last_updated: 2026-10-11
project: ATS
owner: SE
category: evidence-pack
status: confirmed
dependencies:
  - path: WI-20261011-ATS-013-handoff.md
    reason: Approved local-only operator preparation
  - path: WI-20261011-ATS-012-evidence-pack.md
    reason: Exact v5 candidate approval and unchanged wrapper
  - path: WI-20261011-ATS-010-evidence-pack.md
    reason: Preserved revision3 source and lifecycle review
---

# Evidence Pack: WI-20261011-ATS-013

> Purpose: Pin the minimal v5 lifecycle operator for independent WI014 review.

## Summary and Scope

TL;DR: new operator prepared;19/19 offline tests PASS; WI014 pending.
Native/app/DB execution by SE: NOT_RUN. SE verified on2026-10-11 through10:11 KST,
Windows/PowerShell; HEAD58b1011dad796017e47dbcc82a4225ca449bcdf6. No remote,
private file, service/agent operation, installation, candidate regeneration,
commit/push/deletion or descendant. Only new build/WI013-native-v5 files and
this WI013 deliverable pair were written; shared runbook/REQ/index unchanged.

- [x] Exact v5 root/output/bundle/wrapper and WI012 approval binding.
- [x] Preserved lifecycle/identity/TLS guards; narrow diff verified byte-for-byte.
- [x] Mocked cleanup, failure/timeout and nonzero HOLD, exact hashes and commands.
- [ ] WI014 approval and fresh MA native gates remain separate.

## Delta and Pins

[Operator](../../build/WI013-native-v5/ma-native-operator-v5.py):4-7 changes root
to `/root/wi011`, output to `build/WI011-schema-v5` and pins to the reviewed v5.
Line116 changes approval review to WI012. Line165 adds exit0 only for report
PASS, otherwise exit2, satisfying the WI013 nonzero-HOLD requirement. Rev3
reported HOLD but had no explicit exit-status mapping. Those five replacements
and one appended statement are the entire executable delta; no style refactor.

Lines54,126,143,147 retain owned process-group TERM/KILL/wait,210-second child
deadline, cleanup signal masking and independent stop90s/mask20s attempts.
The inherited command/probe output length checks remain post-capture checks;
this change does not introduce a streaming memory bound. Historical approvals,
exclusive0600 creation, private-path/identity/TLS pins and boot pin are unchanged.
The WI012 check marker is obtained from the hash-pinned wrapper's CHECKS; the
real wrapper parser accepts the synthesized operator approval in the test.
WI014 is the separate operator review, not a replacement for review_wi=WI012.

| File / identity | SHA-256 |
|---|---|
| New ma-native-operator-v5.py | 64dc16d9b74315647497b9d335e5e309e27f544e03a5f76df35cb66fe0288136 |
| New test_native_operator.py | db84b04c2dbe3fd12bec6df7a4ad0e7093a5dfdcea61b41d7bdcd48383f494e0 |
| New test-output-final-20261011.log | 404a95581d31c4cb05ea052efe8286581f14157df8320e40e99fc538fce74450 |
| Preserved revision3 operator | 85384f2c118119e96e2166f8456448dcf16ad5a915798b0be211db4f246b5189 |
| Unchanged release-readiness.py | 5221e4c80ebc7a82aaade27e27b1c7f91cb6cea37980f44f3a73592c3ca4c7cd |
| Unchanged v5 artifact.json | 6749b2667e903635c0fe1849e66f7a261b0499a7ed29a19daa5e4da248836ba8 |
| Unchanged v5 bundle | 463c95667ee51e5fe2f01303a43ccebe0c0547375a374730c511c924212c7e54 |

## Tests and Reproduction

```powershell
python -B -m unittest discover -s build/WI013-native-v5 -p test_native_operator.py -v
git diff --no-index -- build/WI010-pg-20261011-0210/ma-native-operator-revision3.py build/WI013-native-v5/ma-native-operator-v5.py
```

[19-test log](../../build/WI013-native-v5/test-output-final-20261011.log):19 passed,
0 failed/errors/skips,0.515s (earlier same-suite runs0.449s/0.453s). Diff exit1 means the
expected six-line delta, not test failure. Existing55-test candidate results
remain WI011 evidence; they were not rerun or counted as WI013 tests.
Direct checks passed on both new sources and both documents: whitespace/final
newline, document metadata and local links. Scoped git diff --check exited0;
direct checks cover untracked/ignored files that Git's check alone omits.

[Tests](../../build/WI013-native-v5/test_native_operator.py):42 validates the
entire exact source delta;60 rehashes the candidate and calls its actual approval
parser;80 exercises extracted probe/handler/finally/exit AST with inert boundaries.
Success, schema/protocol rejection, malformed JSON, launch failure, timeout,
stop/mask nonzero or timeout, reap/state/TLS failure and late interrupt all have
asserted outcomes. Every touched case still attempts stop then mask, including
stop failure/timeout, and every HOLD exits2. START_ATTEMPTED survives launch-stage
failure; observed parsed completion remains RUN.

Line228 exercises actual group_alive/reap function AST with fake clock, process
and killpg: TERM success, KILL escalation, group remaining and wait timeout.
Line266 checks retained handler registration and calls the inert interrupt handler.
Adapted from PG's [rev3 AST checks](../../build/WI010-pg-20261011-0210/review_operator_revision3.py),
without importing or running the native operator's top-level setup/preflight.
Tests never execute a real command, send a real signal, open private paths or
spawn the wrapper/Java. This is not Linux signal delivery, orphan adoption/reaping,
SSH loss, filesystem permission, service or full-preflight integration proof.
Recovery console remains essential for SIGKILL, host/connection loss or failure
to confirm process-group cleanup. No subreaper or new supervisor architecture added.

## Exact MA Staging and Invocation

Only after WI014 PASS and fresh MA gates, stage the178 exact public v5 plan
inputs under root-owned0700 `/root/wi011`, preserving relative names, regular
single-link0600 files and trusted ancestry. Keep existing candidate/operator
directories unchanged. If the destination exists, HOLD for exact MA disposition;
do not overwrite it or silently repoint the operator.

Stage only the new operator bytes separately at
`/root/wi013/ma-native-operator-v5.py`, root0700 parent/root0600 single-link file,
using exclusive creation. Verify its hash above, all candidate pins and the exact
plan before use. Tests/logs are local review evidence, not execution dependencies.
The wrapper approval remains `WI-20261011-ATS-012` with
`pg_wi012_exact_bundle_pass`; never change it to WI014. Its20-minute approval
creation remains exclusive and cannot overwrite a prior approval.

Future invocation, **NOT RUN by SE**, only under that fresh reviewed MA window:

```sh
/usr/bin/env -i PATH=/usr/sbin:/usr/bin:/sbin:/bin HOME=/root LC_ALL=C /usr/bin/python3 -I -B /root/wi013/ma-native-operator-v5.py
```

MA must freshly confirm pinned host/boot
`e8dd67fe-4085-40c9-bb51-23eeabebf91e`, root recovery, explicit current secret use,
exclusive access and capture absence, historical identity provenance, app absent,
MySQL masked/inactive and restricted TLS/timer unchanged. The retained boot pin is
not a current observation. Consume structured report and exit status together;
HOLD or unconfirmed cleanup never authorizes automatic retry. MA owns the final
exact-key/session/agent cleanup, which this remote operator does not implement.

## Risks, Rollback and References

No local implementation blocker remains; exact WI014 review is still required.
On mismatch/failure, stop selecting the new operator and preserve evidence; do
not restore an old operator as a fallback, replay bootstrap, edit environment
or change schema. No target rollback was performed or needed by this local WI.

Injected Tier0: core-principles STD-001, development STD-002, documentation STD-004,
glossary STD-005; SE role and full security policy. Existing skill-generated
handoff checked first; test skill read and applied as scoped Python unittest,
create-wi-evidence-pack used for this pair. Current runbook and WI012 evidence
read; WI010 supplies preserved source/test provenance. Additional handoff policy
pointers were not broadly reloaded. Return immediately to MA for WI014; no delegation.

## Related Documents

- [Handoff](WI-20261011-ATS-013-handoff.md): Allowed paths and acceptance.
- [Summary](../user/WI-20261011-ATS-013-summary.md): Bounded result.
- [WI012 review](WI-20261011-ATS-012-evidence-pack.md): Exact candidate PASS, not operator authority.
- [WI010 review](WI-20261011-ATS-010-evidence-pack.md): Unchanged safeguards and original revision3.
- [Runbook](../../deploy/single-vps/runbooks/release-readiness.md): Target gates and recovery boundary.
