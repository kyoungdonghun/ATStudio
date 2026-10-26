---
version: 1.0.0
last_updated: 2026-10-23
project: ATS
owner: SE
category: evidence
status: stable
related_wi: WI-20261023-ATS-014
---

# Evidence Pack: WI-20261023-ATS-014

## Summary

Prepared a separate Korean capture/error-reporting repair with actual synthetic
Save storage tests. The real reported failure's cause remains unconfirmed.

## Scope / DoD Check

- [x] Frozen WI005/WI011 preserved; no real window, input, memory or server access.
- [x] Korean actor/field validation without values; separate fixed runtime and
  storage codes; errors retain entries and existing files.
- [x] Unchanged frozen input contract, masked confirmation, private current-user
  ACL and exclusive storage; no password truncation or silent normalization.
- [x] MA-approved local capture separation from remote target/expiry/staging;
  no new remote authorization, plan, date or write path.
- [x] Exact callback event reaches actual private synthetic storage; manifest,
  command recipe, evidence and immediate PG015 handoff completed.

## Reference Documents (Tier 0-2)

[WI014 handoff](WI-20261023-ATS-014-handoff.md) supplied Tier0 core principles,
development/documentation standards and glossary; Tier1 security/quality policies;
Tier2 `docs/index.md` and approved REQ007. It also supplied WI011/WI012 evidence
and frozen capture/builder sources. Assignee SE; injection principles follow
`.claude/config/context-injection-rules.json`.

This pack uses `.agents/skills/create-wi-evidence-pack/SKILL.md`; required
handoff exists. MA subsequently explicitly approved local capture ordering
under the existing private-input/retention scope, without changing remote gates.

## Evidence Pointers

- [Candidate](../../build/WI20261023-014-capture-repair/candidate.json): 4 members;
  manifest SHA-256 `907206f3e918310b30c70afcd5bdd02f448fbf42cd8446ce6b2b58460e989450`;
  bundle SHA-256 `97965d8016575e148270ddaf3660cf89f5028c29519fab40ab3ca8e97f7c7c0a`.
- [README](../../build/WI20261023-014-capture-repair/README.md): current permission,
  preparation/launch recipe, exact verification and native boundaries.
- [Capture](../../build/WI20261023-014-capture-repair/capture.ps1): Korean UI,
  named Save function called by the actual Click callback, fixed runtime codes,
  protected storage. UTF-8 BOM preserves Windows PowerShell5 Korean source.
- [Validator](../../build/WI20261023-014-capture-repair/capture_validator.py):
  fixed actor/field/code protocol, exact frozen dependency verification and
  final `fixture_core.inputs` enforcement; local readiness only.
- [Reproducer](../../build/WI20261023-014-capture-repair/test_save.ps1) and
  [safe test output](../../build/WI20261023-014-capture-repair/test-output.txt).

## Commands / Tests

`powershell.exe -NoProfile -NonInteractive -STA -ExecutionPolicy Bypass -File build/WI20261023-014-capture-repair/test_save.ps1`
-> 7 PASS on Windows, 2026-10-23, owner SE.

Checks use in-memory synthetic controls and the .NET Click event, without a
visible form or UI automation. New valid Save reaches actual current-user-only
private storage. Nickname, confirmation,73-character password, missing synthetic
Python executable and storage collision tests retain entries, report fixed codes
and avoid value reflection. The original frozen callback and validator also
reach actual protected synthetic storage unchanged. Original positive evidence
therefore does not explain the real failure or blame user input.

The local readiness positive case has an empty protected directory and no target
record/staging receipt. It creates no native authority. Files use synthetic
`.invalid` identities only and are preserved. No real input or input/payload hash
is in logs. Original WI011 manifest remains
`21daba86032674c87844d9a584fee5ec42e3c689db806e53c0a961ebfa407245`; its dependency
checks also verify all 22 WI005 files. No full application test/build was run.

## Risks / Rollback

Visible human interaction remains unverified separately from the event/storage
tests. The actual earlier failure is unresolved; only diagnostic separation and
local ordering are repaired. Do not read real process memory or infer password
length from masking. Memory zeroization is not claimed.

Stop and preserve new files to withdraw the candidate. Never overwrite frozen
tools or existing private input. Local capture does not waive fresh current
target/approval/staging/expiry checks for a later inspect. No new window was
launched; no DB/app/login/DML/audio activity occurred.

## Follow-ups

WI014 -> WI015 completed with `ALLOW_EXACT_LOCAL_CAPTURE_REPAIR_ONLY`:
[PG receipt](../../build/WI20261023-015-capture-review/review-receipt.json), SHA-256
`fc4897a1c981fa495cfe6ab5bab995dc407a5723e990116059c71528502b1c1c`.
PG reran all 7 SE checks and passed 4 independent checks, including invalid input
-> correction -> the same event -> real private storage with all fields matched.
MA may coordinate a new local capture under existing approval; subsequent remote
inspect requires its independent existing gates. This repair is not injected
into an old process and does not automatically transfer previous entries.
