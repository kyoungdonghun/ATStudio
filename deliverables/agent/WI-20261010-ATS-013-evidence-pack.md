---
version: 1.0
last_updated: 2026-10-10
project: ATS
owner: SE
category: evidence-pack
status: draft
dependencies:
  - path: WI-20261010-ATS-013-handoff.md
    reason: Approved assignment and injected context
  - path: ../user/REQ-20261010-ATS-006.md
    reason: Local preparation approval, not execution
---

# Evidence Pack: WI-20261010-ATS-013

## Summary

TL;DR: Implemented guarded cold-capture orchestration, disabled recurring
authorization, private evidence/state handling and 35 passing synthetic tests.
Local implementation is ready for WI014 review; **target activation is not ready**.
Host hooks, including logical dump, remain unconfigured. No real backup occurred.

## Scope / DoD Check

- [x] Only new assigned backup paths, backup unit templates and own two-set deliverables changed.
- [x] Default plan and validation are offline; example config/policy fail closed.
- [x] Exact-target/hash/window approvals, bounded helper runner, cold capture and failure state implemented.
- [x] JSON-lines parsing, exact snapshot and full member inventory; only manifest+dump remote hashes before resume.
- [x] Earlier last-good preserved on failure; no automatic resume after failed capture; partial snapshot rejected.
- [x] Disabled daily policy: exact config hash, explicit KST start grace/window/expiry, daily run ID, no catch-up.
- [x] Synthetic success/failure/timeout/path/lock/resource/privacy/publication tests and file-hash inventory.
- [ ] Named-host adapter and daily systemd/monitor integration: future approved target work.
- [ ] Actual Linux/restic/S3/DB/disk/cgroup/restore/copy/performance proof: NOT RUN.
- [ ] Independent PG acceptance: WI014, owned by MA's existing chain, not claimed by SE.

## Reference Documents (Tier 0-2)

The approved handoff was read before editing. Project tag ATS was confirmed in
`.claude/config/workspace.json`; injection reference was
`.claude/config/context-injection-rules.json`, assignee SE, implementation/testing,
required Tier 0 plus the security/quality context supplied by the handoff.

| Tier | Injected pointer | Use |
|---|---|---|
| 0 | `docs/standards/core-principles.md` | Approval, sustainability, privacy |
| 0 | `docs/standards/documentation-standards.md` | Metadata and two-set documentation |
| 0 | `docs/standards/development-standards.md` | Standard-library Python, tests and evidence |
| 0 | `docs/standards/glossary.md` | Canonical WI/REQ/recovery terminology |
| 1 | `docs/policies/security-policy.md` | Reference-only credentials, fixed diagnostic codes |
| 1 | `docs/policies/access-control-policy.md` | Read-only defaults, least privilege |
| 1 | `docs/policies/quality-gates.md` | Reuse, verification boundaries and independent review |
| 2 | `docs/index.md` | Current dated decision entry and preserved TEST evidence |
| 2 | `deploy/single-vps/runbooks/resource-baseline.md` | Whole helper group and reserves |
| 2 | `deploy/single-vps/runbooks/restic-backup-plan.md` | Capture consistency and retained snapshot contract |
| 2 | `deliverables/user/REQ-20261010-ATS-006.md` | Local implementation authority and WI chain |
| 2 | `deliverables/user/REQ-20261010-ATS-005.md` | Design-only predecessor, preserved historical scope |
| 2 | `.agents/skills/create-wi-evidence-pack/SKILL.md` | This Evidence Pack format and precondition |
| 2 | `.agents/skills/test/SKILL.md` | Test result reporting; scoped Python exception to Java suite |
| 2 | `.agents/skills/build-check/SKILL.md` | Build boundary; no application build needed/run |

Conditional frontend pointers (`docs/standards/frontend-standards.md` and
`.agents/skills/react-best-practices/AGENTS.md`) were not applicable: no frontend
work. Additional inspected references: root `AGENTS.md`, existing
`scripts/deployment/validate_bundle.py` (offline CLI pattern reuse candidate),
`deploy/single-vps/runbooks/backup-restore.md` (scheduler/egress quarantine).
No existing validator, application/env/contract or Java file was edited.

Primary restic references checked on 2026-10-10:

- [Scripting](https://restic.readthedocs.io/en/stable/075_scripting.html): JSON-lines and fail-closed exit handling.
- [Manual](https://restic.readthedocs.io/en/stable/manual_rest.html): Backup/limit/cache command flags.
- [Pinned 0.19.1 ls source](https://raw.githubusercontent.com/restic/restic/v0.19.1/cmd/restic/cmd_ls.go): Actual node fields and Go mode representation.

These checks are documentation/source inspection, not installed-binary verification.

## Evidence Pointers

| Pointer | Evidence |
|---|---|
| `scripts/deployment/backup/backup_protocol.py:65` | Zero-only exit acceptance, JSON-lines summary and exact 64-hex ID |
| `scripts/deployment/backup/backup_protocol.py:89` | Modern/deprecated ls message discriminator; no JSON-array assumption |
| `scripts/deployment/backup/backup_protocol.py:108` | Exact source membership and supported metadata; special-mode/symlink rejection |
| `scripts/deployment/backup/backup_protocol.py:142` | Config/path/repository/credential/budget validation |
| `scripts/deployment/backup/backup_protocol.py:213` | Disabled-by-default expiring daily authorization/no-catch-up logic |
| `scripts/deployment/backup/backup_protocol.py:257` | Independent failure/missed/26h/8d status reporting |
| `scripts/deployment/backup/backup_runtime.py:56` | Atomic attempt+last-good replacement |
| `scripts/deployment/backup/backup_runtime.py:70` | Cooperative nonblocking Linux flock |
| `scripts/deployment/backup/backup_runtime.py:106` | Complete private source hash inventory, empty roots and staging |
| `scripts/deployment/backup/backup_runtime.py:166` | Live cgroup/resource monitor |
| `scripts/deployment/backup/backup_runtime.py:233` | Bounded shell-free helpers and owned process-group stop |
| `scripts/deployment/backup/guarded_backup.py:109` | Cold state machine and failure receipts |
| `scripts/deployment/backup/guarded_backup.py:229` | Linux/root/private path/pinned binary/exact host execution gate |
| `deploy/single-vps/backup/README.md:39` | Unconfigured hook contract and remaining integration |
| `deploy/single-vps/backup/README.md:82` | Manual versus daily authorization and disabled scheduling |

The new shared interface uses `/run/atstudio-operations/operations.lock`.
Other operations must adopt that same stable lock under later host integration.
The service entrypoint is `scheduled --config ... --approval ...recurring-policy.json`;
manual `execute` retains its exact one-use approval contract. MA was notified of
both interfaces in progress commentary. No notification tool or child agent was used.

## Commands & Outputs

Environment: **2026-10-10 Windows/local, SE, Python 3.14.3**. Execution wrapper:
PowerShell. Git actions were prohibited, so branch/HEAD/dirty enumeration was not
re-run; dated HEAD references in input documents are not a fresh checkout claim.
Public primary-document browsing was the only network research; no runtime
repository/cloud/DB/remote-host call was performed.

```text
python --version
Python 3.14.3

python -B -m unittest discover -s scripts/deployment/backup -p test_guarded_backup.py -v
Ran 35 tests in 0.759s
OK

python -B scripts/deployment/backup/guarded_backup.py
{"side_effects": false, "status": "NOT_CONFIGURED"}

python -B scripts/deployment/backup/guarded_backup.py validate --config deploy/single-vps/backup/config.example.json
{"status": "BLOCKED", "code": "NOT_CONFIGURED"}

python -B scripts/deployment/backup/guarded_backup.py scheduled --config deploy/single-vps/backup/config.example.json --approval deploy/single-vps/backup/recurring-policy.example.json
{"status": "BLOCKED", "code": "NOT_CONFIGURED"}

python -B scripts/deployment/backup/guarded_backup.py status
ATTENTION: CAPTURE_26H_STALE, SCHEDULE_NOT_CONFIGURED, INDEPENDENT_COPY_8D_STALE
external_send: NOT_IMPLEMENTED

python -B scripts/deployment/backup/guarded_backup.py copy-plan
NOT_CONFIGURED / FUTURE_GUARDED_OPERATION
```

Guarded error/attention paths return nonzero (`main` returns 2; this PowerShell
tool wrapper reported 1 for nonzero native commands). Default plan and tests
reported process exit 0. No configured `execute` or configured `scheduled` CLI
was invoked. Five Python files passed `ast.parse(..., feature_version=(3,10))`
without writing bytecode. ASCII/trailing-whitespace `rg` scan found no matches
across the implementation paths. Full Gradle/npm build, project-wide validators
and Linux systemd syntax checks were **NOT RUN** in this disjoint Python WI.

Final scoped PowerShell document check: 3 documents, 8 relative local links,
seven required metadata fields per document PASS. Three JSON examples parse.
All 16 SHA-256 rows below (12 new assets plus four existing preservation samples)
match current files. Final ASCII/trailing-whitespace scan also covers both
deliverables and returns no matches. This is a scoped check, not the global
documentation validator or an independent PG acceptance.

## Tests and Corrections

35 unittest methods, 0 failures, 0 skips; additional negative cases use subtests.
Cases cover success and both roots/staging, partial/unknown exits, missing/wrong
inventory, hash mismatch, stale dump/run reuse, unfinished maintenance, dump/hook
failure, deadlines, owned group stop, incomplete stop rejection, lock conflict,
ancestor/source symlink escape, low disk/inodes, output caps, exact approval,
restore containment declaration, daily policy expiry/no catch-up, default no action,
private-output suppression and atomic last-good publication failure.

Fake subprocess calls never start real children; mocked Linux-only signal/flock/
statvfs behavior is not a Linux integration test. Generated temporary files contain
synthetic text only. Windows fixture paths are aliased around the actual ls parser;
ordinary metadata/membership checks remain active. Fake ls rows use actual CLI
fields, not invented SHA/symlink-target output. No remote media retrieval is allowed
by the fake runner; successful capture proves exactly two retrieval requests.

Observed development corrections:

1. First run: 26 methods, 24 PASS and 2 ERROR because Windows has no SIGKILL.
   Added a Linux-signal test fixture; production remains Linux-only. Next run 26 PASS.
2. MA review rejected all-media retrieval inside the cold window. Changed to
   manifest+dump only, retained private source hashes and added a regression assertion.
3. MA supplied pinned ls schema/mode evidence. Removed invented symlink-target
   verification, reject unverified symlinks/special modes, and test Go directory bits.
4. MA noted that one-use approvals alone did not prepare unattended daily capture.
   Added explicit default-OFF expiring daily policy/runner and four focused tests.
5. Final expanded run: 35 PASS; no real side-effect command was used to fix tests.

Coverage percentages/branch thresholds are **not measured or claimed**. In
particular, real cgroup setup, signal delivery, flock semantics, filesystem crash
durability and host adapter predicates require independent named-host tests.

## Changed-Hash Inventory

All 11 implementation/template/guide paths and both deliverables were absent
when checked before editing (`Get-Item ... -ErrorAction SilentlyContinue` returned
no assigned assets). Before hash = **ABSENT**, not an existing file's replacement.
SHA-256 below was obtained with `Get-FileHash -Algorithm SHA256` after verification.
The Evidence Pack excludes its own hash to avoid a circular self-hash.

| New path | After SHA-256 |
|---|---|
| `scripts/deployment/backup/backup_protocol.py` | `A890D997B49FD6CE1D997A95951087279F2246C7D0E71A1E035254061547FD43` |
| `scripts/deployment/backup/backup_runtime.py` | `50463B30802A24396F36F102952CB494433D4BBA67DF637026EEC1E14835CC72` |
| `scripts/deployment/backup/guarded_backup.py` | `F741105DF211D462177DB4F4D140C862E924D6BA1CB2E729620E6C949AA1269F` |
| `scripts/deployment/backup/test_guarded_backup.py` | `4D457CBC181E67EEEED3228CA969A8D5A037E99D2E3A2A0F03CD38B62D8AD755` |
| `deploy/single-vps/backup/approval.example.json` | `5D4AC4419D8F8CB54AB892AF3EC2C6DAB9CD898A1B5736B9C32EE5D9434EE7AC` |
| `deploy/single-vps/backup/config.example.json` | `F0F1EB36F3430BF30B93C0BD4E0224ABF35DE7BA29DE50F542B2881546BD1F33` |
| `deploy/single-vps/backup/host_hooks_unconfigured.py` | `E214EB39D180529457A685EFC973810B9297292A7FF3CD38BC3DB80AB1E4E99B` |
| `deploy/single-vps/backup/README.md` | `AEBB5BEB1B5F189F270D32E4CFA3E77CEE7C3198AEE20FE74CC7FC80F71D987C` |
| `deploy/single-vps/backup/recurring-policy.example.json` | `31A2F8A9C40FDABB9182CDE95D004A4D3F8743A749D6250D87902D544952616D` |
| `deploy/single-vps/systemd/atstudio-backup.service` | `3B2895D2C955AFFB3E50DEF05E4E4245676A55AFB4FD5919FC23BC60F426D09A` |
| `deploy/single-vps/systemd/atstudio-backup.timer` | `97B9AA513397E6A6115F7295524476B03AB18FD31B627C40984B24B0F94D4703` |
| `deliverables/user/WI-20261010-ATS-013-summary.md` | `059D24850BAA679E62E8A61899363B7C3D08F0CBA284E31120B43BC4DF782A65` |

Before/after inspected-source preservation samples:

| Existing pointer | Before = after SHA-256 |
|---|---|
| `AGENTS.md` | `6F0A0DF8A7BAF1257D84B859A01D0706B8F15673AA46F2C12A980147A18FA8D7` |
| `deploy/single-vps/runbooks/restic-backup-plan.md` | `2C1C3CB3CEEED3C9FC399DA441BB1FD421848FF8069879A0AB109916CCB66FF3` |
| `deploy/single-vps/runbooks/resource-baseline.md` | `A96D5D05D52DBA66D351C4B871642A33BDA08242E3D316B28956263691F23C05` |
| `deliverables/agent/WI-20261010-ATS-013-handoff.md` | `F2C50B60DD4B2AC8AD28402647B4C2B8A6CA650562CD4A1FB36CCCCFB80C58C5` |

Shared-workspace observation: REQ006 changed from
`F59291DFB2ECFE2AA627EA78E9F04BF0BEE522CCC5F1BB2989F884A4BA28ABE4`
to `E6089FCBE6F712CBF847775AB7D3A02BD20B108774207B7B77133612826EE468`.
Re-read its new progress section: MA recorded WI012 completion, WI013 in progress
and WI014 review already underway. This SE did not edit/revert that file. These
samples are not a whole-worktree preservation hash claim.

## Risks / Rollback

- Host hooks are all **unconfigured**, including DB dump/schema validation,
  quiesce/fence/relaunch, ACL/recovery/budget verification and controlled resume.
  The implementation is not ready-to-activate or a proven daily unattended service.
- Timer calendar/installation, exact target/credential provisioning, shared-lock
  adoption by other operations and independent checker/sender remain named-host work.
- Internal source symlinks/nested mounts/special files/special mode bits are rejected;
  target inventory must be reviewed. No preservation/restore claim for ACLs/xattrs.
- Routine retrieval is only manifest+dump; full-media/full-repository reads, actual
  restore and independent external-disk copy are deferred. `restore-plan` validates
  declarations, never establishes scheduler suppression or actual isolation.
- Source limits/rate settings/projected cost do not enforce provider invoice caps.
  Sampled resource guards are not quotas or real-time guarantees; capacity/performance
  and actual billed storage/read/request usage remain unverified.
- Failure receipt writes can themselves fail; last durable stage and prior good state
  remain the conservative basis. No automatic cleanup or emergency reopening exists.
- Rollback: do not adopt these new assets. After exact approval, revert only this WI's
  listed additions; do not reset shared work, erase historical evidence, remove a live
  operation lock, delete captures or alter databases. No rollback was performed.

## Follow-ups

WI013 **Blocks WI014**. MA already has WI014 underway per refreshed REQ006;
provide these final artifacts/hashes to that existing independent PG review. Its
completion then releases WI015. This SE did not spawn agents, contact another
chat, commit/push, close the overall REQ, or update memory.

## Related Documents

- [User summary](../user/WI-20261010-ATS-013-summary.md): Concise scope and limitations.
- [Implementation guide](../../deploy/single-vps/backup/README.md): Operational interfaces and gates.
- [Handoff](WI-20261010-ATS-013-handoff.md): Approved ownership and output contract.
