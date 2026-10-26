---
version: 1.0
last_updated: 2026-10-11
project: ATS
owner: SE
category: evidence-pack
status: confirmed
dependencies:
  - path: WI-20261011-ATS-048-handoff.md
    reason: Fresh HTTPS-only continuation authority
  - path: ../../build/WI048-quarantine-continuation/freeze-v2.json
    reason: Active corrected recipe and immutable candidate identities
---

# Evidence Pack: WI-20261011-ATS-048

> Purpose: Hand the narrow original-HTTPS continuation to PG049 with reproducible local evidence.

## Summary and DoD

**TL;DR: LOCAL_READY_FOR_PG049, native NOT_RUN. Use README-v2.md only.**

- [x] Only nginx.After uses token-multiset comparison; every other value and Exec order remains exact.
- [x] Prior consumed receipt/claim, durable copies and retained siblings checked; no move, deletion, daemon-reload or old run invocation.
- [x] Fresh WI048 tools/record, original TLS/service checks, one Nginx start and once-only safety stop including receipt failure.
- [x] App/MySQL stay masked/stopped; no SQL, environment, logs or certificate contents read.
- [x] 21 current owner tests plus 23 unchanged inherited regressions PASS; exact v2 recipe/freeze and old-byte preservation.
- [ ] Independent PG049 final review and MA-only native recovery.

## Evidence and Changes

[continue_https.py](../../build/WI048-quarantine-continuation/continue_https.py)
loads only held, hash-verified WI046 bytes. That helper verifies its three WI042
imports. The new nginx_equal function compares sorted After token lists without
deduplication, copying or normalizing any other field. The old helper run/restore
paths are never called. The new run checks the exact pinned prior HOLD receipt
and claim, absent original overlay paths, both retained files and durable copies
before any claim/start; it rechecks this evidence and original baseline after start.

The sole operational delta from WI046 is continuation from completed detachment:
no copy/detach/reload, a fresh record, and After-only normalization. Original
Nginx/MySQL baseline comparison now occurs before the fresh claim. Stop/receipt
failure handling retains the F01 correction: one stop, all safety checks attempted,
provisional disk bytes, and stdout HOLD if write/fsync fails. No automatic retry.

MA's17:32:55 evidence identified nginx.After as the only differing property,
with equal seven-token multisets. At17:35:30 MA supplied root metadata and exact
prior receipt SHA256. SE did not observe the target. The disk receipt contains
receipt_durability PENDING, whereas prior completed stdout reported PASS; the
candidate deliberately validates the supplied historical disk bytes, not an
invented PASS field. Synthetic tests use seven named placeholder tokens, not a
claimed raw service capture. Original WI042 POST_STATE_CHANGED remains undetermined.

## Recipe Revision and Pins

MA caught a pre-native v1 L1 path defect: local input incorrectly pointed to
WI048-quarantine-recovery instead of WI048-quarantine-continuation. No attempt
was consumed. v2 changes only that executable path plus version/reference prose.
L2 and Python candidate are byte-identical. All v1 README/freeze/pins/receipts stay
unchanged and are included in the v2 manifest as historical, non-executable authority.

| Identity | SHA256 |
| --- | --- |
| Python candidate | `2d617dc5a09b67a3024f628b90f6465bcac29bd8cce4ac852a0360d75484765a` |
| Active v2 bundle | `b3befcd2a2310ef8c7461800ce35f75f2aad918eb479de12f480433c55fcacfe` |
| freeze-v2.json | `26c47507668074aea79c5f96fd5d0a99d7dd7d1c86259edb3898baf170240b2e` |
| README-v2.md | `bbc38de19394e990262deccf1da0343b6be3934fc715a2613737b15d10aa9fc9` |
| L1 v2 | `c2f816ee642a05d1b10b8999cef5b5d0331e8fa3746f85ee5935bb2915bd83ba` |
| L2 unchanged | `52ab891a56f1ed6a5881ec42523877f457a2e69e3d8e484a8ddafe69c41ed3b0` |

[freeze-v2.json](../../build/WI048-quarantine-continuation/freeze-v2.json) records
the17:43:54+09:00 freeze and ordered filename+NUL+exact-byte bundle definition.
[recipe-pins-v2.json](../../build/WI048-quarantine-continuation/recipe-pins-v2.json)
pins both blocks as UTF-8/LF with one final newline, fences excluded.
[preservation.json](../../build/WI048-quarantine-continuation/preservation.json)
records seven selected old public input identities; this is not a whole-source/JAR audit.

## Tests and Commands

Windows local, SE, 2026-10-11. Test skill applied to the handoff's Python-only
mock scope; no Gradle/npm build and no native body or main executed.

| Command or check | Result |
| --- | --- |
| `python -I -B -m unittest discover -s build/WI048-quarantine-continuation -p 'test_*.py' -v` | v1 18 PASS/0 errors/skips, 0.067s; v2 21 PASS/0 errors/skips, 0.068s; exit0 |
| `python -I -B -m unittest discover -s build/WI046-quarantine-recovery -p 'test_*.py' -v` | 23 PASS/0 errors/skips, 0.078s, exit0; old source untouched |
| PowerShell AST parse, active recipe | 2 blocks PASS; none executed |
| Python compile, v2 | 3 files and 2 embedded bodies PASS; none executed |

[v2 receipt](../../build/WI048-quarantine-continuation/local-receipt-v2.json),
[v2 log](../../build/WI048-quarantine-continuation/v2-tests.log),
[inherited log](../../build/WI048-quarantine-continuation/inherited-tests.log).
The three recipe regressions preserve/reproduce v1's invalid path, resolve every
v2 local recipe input to the actual candidate hash, and assert that L1 changes only
that path while L2 is identical. Syntax alone was insufficient to detect v1.
Other tests cover added/removed/duplicated After tokens, other-property/Exec drift,
prior evidence and claim changes, reappearing overlays, TLS failure, post-start
drift, ambiguous start, receipt write/fsync failure, stop failure, and no stop retry.

## Risks, Rollback and Next Step

Withhold the candidate before MA execution. After a claim, partial or uncertain
result, retain all evidence and inspect read-only; do not rerun, remove records,
reattach temporary overlays or start app/DB. Only stdout recovery success plus
exit0 and receipt_durability PASS establishes this invocation's result. Disk bytes
remain provisional. Primary alarm120s and bounded cleanup do not cover SIGKILL,
host loss or indefinitely blocked kernel I/O. Local mocks prove neither real
Linux durability nor live HTTPS/browser/DB business-state correctness.

PG049 follows immediately using v2 exact pins, then MA alone may perform the
approved continuation. WI045 owns native integration. Existing trial failure
and prior recovery receipt remain separate historical facts.

## References and Related Documents

Tier0 retained in order: STD-001 core-principles, STD-004 documentation-standards,
STD-002 development-standards, STD-005 glossary. SE and security-policy retained.
Tier2: approved REQ006 amendment, WI048 handoff, WI046 evidence/frozen helpers,
test and create-wi-evidence-pack skills. No subdelegation or shared-doc edits.

- [WI048 Handoff](WI-20261011-ATS-048-handoff.md): Authority and DoD.
- [Active MA Recipe](../../build/WI048-quarantine-continuation/README-v2.md): Only approved-review candidate procedure.
- [Summary](../user/WI-20261011-ATS-048-summary.md): User-facing result.
- [WI046 Evidence](WI-20261011-ATS-046-evidence-pack.md): Preserved predecessor.
