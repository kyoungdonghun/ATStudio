---
version: 1.0.0
last_updated: 2026-10-23
project: ATS
owner: SE
category: evidence
status: stable
related_wi: WI-20261023-ATS-008
---

# Evidence Pack: WI-20261023-ATS-008

## Summary

Prepared a minimal, separately frozen account/plan read-only recheck using the
successful WI002 lifecycle. No remote access or DB/app execution by SE.

## Scope / DoD Check

- [x] Existing per-user subscription total, including expired rows, alongside
  effective entitlement; bounded plans ID/type/active/quota projection.
- [x] Separate read-only approval scope, fresh absolute expiry, target-bound
  inventory and post-stop probe; new exclusive unit/log name.
- [x] Original source/JAR/helpers, independent cleanup and historical evidence
  preserved; no fixture inputs, credentials, business/schema DML or app/audio.
- [x] Focused local tests, candidate manifest, operator commands and two-set evidence.
- [x] Exact candidate sent immediately to PG009/MA, unblocking WI009.

## Reference Documents (Tier 0-2)

Injected context comes from the [WI008 handoff](WI-20261023-ATS-008-handoff.md).

| Tier | Document | Reason |
| --- | --- | --- |
| 0 | `docs/standards/core-principles.md`, `development-standards.md`, `documentation-standards.md`, `glossary.md` | Injected scope, preservation, language and traceability |
| 1 | `docs/policies/security-policy.md`, `quality-gates.md` | Privacy, exact evidence and bounded validation |
| 2 | `docs/index.md` Current Decision Entry; `deliverables/user/REQ-20261011-ATS-007.md`; `WI-20261023-ATS-007-summary.md`; `WI-20261023-ATS-003-evidence-pack.md` | Current approval, prior preparation and successful lifecycle |

Injection rules: `.claude/config/context-injection-rules.json`; assignee SE;
local security-sensitive implementation. This pack uses
`.agents/skills/create-wi-evidence-pack/SKILL.md`; the required handoff exists.

## Evidence Pointers

- [Candidate manifest](../../build/WI20261023-008-readonly-recheck/candidate.json):
  5 frozen files. Bundle SHA-256
  `3799c0145883e4fc201b2581f547287ce8f3e7c053a21d8536452e5c7fc76313`.
- [Operator README](../../build/WI20261023-008-readonly-recheck/README.md):
  prerequisites, exact local commands, existing SSH caller pointer, failure cleanup.
- [Projection](../../build/WI20261023-008-readonly-recheck/inventory.py):
  consistent READ ONLY transaction, three SELECTs, ROLLBACK; counts/order/type
  bounds; total >= effective >= eligible; finite plan eligibility vs unlimited -1.
- [Operator](../../build/WI20261023-008-readonly-recheck/account_operator.py):
  only docstring, scope and owned unit name differ from WI002.
- [Payload builder](../../build/WI20261023-008-readonly-recheck/make_payload.py):
  maximum 900-second fixed absolute expiry; complete runtime/source verification;
  optional pinned probe and mandatory pinned inventory.
- [Tests and result](../../build/WI20261023-008-readonly-recheck/test-output.txt).
  No files outside the owned candidate and these two deliverables were modified.

## Commands & Outputs

`python -B -m unittest discover -s build/WI20261023-008-readonly-recheck -p test_preflight.py -v`
-> 21 PASS on Windows, 2026-10-23, owner SE; all native calls mocked.
Sixteen inherited focused checks cover lifecycle/containment/privacy, with five
additional delta checks. This is not a project/auth/audio suite.

Read-only AST/base64 comparison confirmed the successful
`build/WI20261023-phase1-native/{probe,inventory}.py` embedded account operator
and inventory bytes equal the original WI002 files. All original WI002 frozen
hashes match. Tests verify the new operator differs only by the three documented
changes and every embedded helper matches its prior pin. Python source/payload
compilation and changed-document whitespace checks pass.

PG independently reported 8 PASS before freeze; exact final allowance belongs
to WI009, not this pack. MA owns all later target execution and result evidence.

## Risks / Rollback

No rollback or business-row cleanup is needed for local preparation. Discontinue
use of the candidate to withdraw it; preserve historical files and logs.
Read-only SQL does not imply a MySQL startup has no engine/log effects. The
retained helpers disable scheduled events, writes, TCP, persisted overrides,
replication start, binlog and upgrade; the original service remains masked.

After possible start, STOP/PID_ZERO/SOCKET_GONE/BASELINE run independently.
Disconnect/SIGKILL requires MA's measured restoration after the 300-second
systemd lifetime and a target-bound post-stop probe. Partial rows are not PASS.
Existing log collision HOLDs. No deletion, automatic retry with another unit,
scope expansion, fixture creation or credential input is authorized here.

Native current rows, native syntax and current restoration are not verified by
the local tests. Existing phase1 proved effective subscriptions0 only; the new
total field resolves historical-row uncertainty when MA executes the recheck.

## Follow-ups

WI008 -> WI009 exact review is unblocked and notified. MA may then perform the
explicitly authorized read-only lifecycle with current target/expiry and record
sanitized results plus all cleanup evidence. Fixture DML and audio still require
separate future approval; an eligible plan observation grants neither.
