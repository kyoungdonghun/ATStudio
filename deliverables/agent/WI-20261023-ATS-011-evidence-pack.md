---
version: 1.0.0
last_updated: 2026-10-23
project: ATS
owner: SE
category: evidence
status: stable
related_wi: WI-20261023-ATS-011
---

# Evidence Pack: WI-20261023-ATS-011

## Summary

Prepared dedicated Windows private capture and a frozen-WI005 inspect-only
staging/transport wrapper. No server contact or real input collection by SE.

## Scope / DoD Check

- [x] Dedicated masked-password/confirmation form, unchanged fixture input
  contract, current-user ACL, outside-repository exclusive storage.
- [x] Technical staging readiness precedes human capture; valid input survives
  expiry during typing without recollection or automatic native retry.
- [x] External MA decision record and exact original-byte pin required;
  target/expiry checks, exact 22-file stage, root0700/0600 private evidence.
- [x] Literal inspect invocation only; no date/apply proposal or business DML.
- [x] Nonidentifying inspection projection distinguishes recorded inspection
  from conditions satisfied, explicitly keeps apply unauthorized.
- [x] Focused synthetic tests, native Windows ACL check, exact manifest and
  two-set evidence. PG012 received the frozen candidate immediately.

## Reference Documents (Tier 0-2)

Injected context: [WI011 handoff](WI-20261023-ATS-011-handoff.md).

| Tier | References | Reason |
| --- | --- | --- |
| 0 | `docs/standards/core-principles.md`, `documentation-standards.md`, `development-standards.md`, `glossary.md` | Scope, language, preservation and evidence |
| 1 | `docs/policies/security-policy.md`, `quality-gates.md` | Private handling and bounded checks |
| 2 | `docs/index.md`; `deliverables/user/REQ-20261011-ATS-007.md`; WI007/WI010 summaries | Current approval and prior observations |

Assignee SE; `.claude/config/context-injection-rules.json` injection principles.
Generated using `.agents/skills/create-wi-evidence-pack/SKILL.md` after verifying
the required skill-generated handoff exists.

## Evidence Pointers

- [Candidate](../../build/WI20261023-011-fixture-inspect/candidate.json): 5 frozen
  files, bundle SHA-256
  `69b0dcd464ff08f690ab7274c744ff617fe70d9a618ea2d9a760fbe807d085a2`.
- [Operator commands](../../build/WI20261023-011-fixture-inspect/README.md):
  external approval record, stage/readiness/capture/inspect, private transport,
  post-stop checks and expiry/input retention.
- [Capture](../../build/WI20261023-011-fixture-inspect/capture.ps1): reuses the
  protected ACL creation pattern from `scripts/deployment/prepare-operator-ssh.ps1`;
  reparse/hardlink/owner/collision checks, stdin validator, no input/digest output.
- [Local builder](../../build/WI20261023-011-fixture-inspect/inspect_tools.py):
  frozen candidate verification, exact operator raw-byte pin, private payload
  plus private payload hash, no networking or automatic authorization issuance.
- [Target wrapper](../../build/WI20261023-011-fixture-inspect/remote_inspect.py):
  root exact staging, current identity/cold guards, immutable collision refusal,
  legacy inspect-record binding, literal inspect, fixed public projection.
- [Test output](../../build/WI20261023-011-fixture-inspect/test-output.txt).

WI005 manifest SHA-256 remains
`3a0f4fd30afc012a8e1a0c46d70b87ed1d2ef5492c2f8143121dc741da35654c`;
all 22 files match. Its previous local-only authorization text remains
historical; current inspect permission is separately recorded under WI011.

## Commands & Outputs

`python -B -m unittest discover -s build/WI20261023-011-fixture-inspect -p test_inspect.py -v`
-> 8 PASS, Windows, 2026-10-23, owner SE. Includes a private-directory
prepare/readiness/payload flow using synthetic `.invalid` identities only.

`powershell.exe -NoProfile -NonInteractive -ExecutionPolicy Bypass -File build/WI20261023-011-fixture-inspect/capture.ps1 -Action SelfTest`
-> `SYNTHETIC_ACL_VALIDATION_COLLISION_PASS`.
Current-user-only ACL creation, synthetic contract validation, invalid input
and overwrite rejection executed natively. Synthetic private files are retained.

The child Python -> Windows PowerShell path exposed missing automatic ACL module
discovery. Explicit import of the installed standard Security module fixed this;
the complete synthetic private flow passed afterward. No dependency installation.
PG feedback also removed silent 72-character truncation and Save-time expiry
rejection, and added exact external approval-byte binding. Python syntax and
manifest/whitespace checks pass. No broad project/auth/audio suite ran.

## Risks / Rollback

Human GUI interaction, real input noncollision, native stage/MySQL syntax,
permissions and restoration are not established by these local tests. Current
user authorization must be verified by MA; a JSON marker cannot prove it.
Private plaintext/payload retention is authorized and ACL-restricted; memory
zeroization is not claimed. No private inputs or their hashes appear in evidence.

Withdrawal means stop using the helper and preserve files. After capture expiry,
MA may stage a fresh authorized private session and copy the existing protected
input without displaying or recollecting it. No silent expiry renewal or native
retry occurs. Any possibly started inspect requires independently observed
DB_SESSION/STOP/PID_ZERO/SOCKET_GONE/BASELINE and post-stop cold verification.
Existing legacy approvals, nonce/log evidence or mismatching files HOLD and are
not overwritten/deleted. No rollback DML or business changes are authorized.

## Follow-ups

WI011 -> WI012 completed with PG's exact
`ALLOW_EXACT_APPROVED_INSPECT_ONLY_PREPARATION_AND_MA_EXECUTION` allowance:
[review receipt](../../build/WI20261023-012-fixture-inspect-review/review-receipt.json).
Candidate manifest SHA-256 is
`21daba86032674c87844d9a584fee5ec42e3c689db806e53c0a961ebfa407245`.
PG reports 10 independent tests PASS, Windows self-test PASS and preservation
of all 643 source entries, retained JAR and 22 WI005 files. MA alone coordinates
authorized stage, technical readiness, one human capture and inspect.
Apply/date/account creation/login/audio remain outside this approval.
