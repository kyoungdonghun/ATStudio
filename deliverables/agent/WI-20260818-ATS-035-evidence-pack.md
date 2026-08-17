---
version: 1.0
last_updated: 2026-08-18
project: ATS
owner: docops
category: evidence-pack
status: stable
related_wi: WI-20260818-ATS-035
dependencies:
  - path: WI-20260818-ATS-035-handoff.md
    reason: Approved read-only inventory scope, constraints, and output contract
  - path: ../user/REQ-20260817-ATS-010.md
    reason: Release-rehearsal approval boundary referenced by committed WI-033/WI-034 evidence
  - path: ../user/WI-20260817-ATS-033-summary.md
    reason: Committed predecessor result
  - path: ../user/WI-20260817-ATS-034-summary.md
    reason: Committed verification result
---

# Evidence Pack: WI-20260818-ATS-035

## Summary

- Completed a read-only inventory of pre-existing untracked `deliverables/**`
  artifacts and their direct references from committed WI-033/WI-034 records.
- Identified the four-file minimum source-reference candidate set without
  staging, committing, or modifying any pre-existing artifact.

## Scope / DoD Check

- [x] Established tracked/untracked state for REQ-010 and WIs directly named by
  committed WI-033/WI-034 deliverable references.
- [x] Classified direct candidates and all pre-existing untracked deliverables
  into TRACK-CANDIDATE, KEEP-LOCAL, HISTORICAL, or REVIEW dispositions.
- [x] Distinguished the four-file minimum source-reference set from companion
  artifacts that require separate inclusion approval.
- [x] Left every pre-existing untracked deliverable unchanged.
- [x] `git diff --check` passed after the permitted reports were created.

## Reference Documents

| Tier | Document or pointer | Use |
|---|---|---|
| 0 | `docs/standards/core-principles.md` | Approved execution boundary and transparent reporting |
| 0 | `docs/standards/development-standards.md` | Tier 0 confirmation and two-set traceability expectations |
| 0 | `docs/standards/documentation-standards.md` | Deliverable metadata, status, and historical-record handling |
| 0 | `docs/standards/glossary.md` | Canonical WI and archived terminology |
| 1 | `docs/policies/versioning-policy.md` | Historical/archived distinction and preservation rules |
| 1 | `docs/policies/quality-gates.md` | Required WI traceability check |
| REQ | `deliverables/user/REQ-20260817-ATS-010.md` | Approved release-rehearsal scope and approval gates |
| Context | `deliverables/user/WI-20260817-ATS-032-summary.md` | Predecessor local rehearsal boundary |
| Context | `deliverables/agent/WI-20260817-ATS-032-evidence-pack.md` | Predecessor evidence supplied to this inventory |
| Context | `deliverables/user/WI-20260817-ATS-033-summary.md` | Committed lifecycle result |
| Context | `deliverables/agent/WI-20260817-ATS-033-evidence-pack.md` | Direct REQ/WI-021/WI-022/WI-032 references |
| Context | `deliverables/user/WI-20260817-ATS-034-summary.md` | Committed verification dependency declarations |
| Context | `deliverables/agent/WI-20260817-ATS-034-evidence-pack.md` | Direct REQ/WI-032 references |
| Files | `deliverables/user/`, `deliverables/agent/`, `.gitignore` | Artifact inventory and ignore-rule check |

## Git and File Evidence

- Current branch: `codex/v1-release-rehearsal-fixes`.
- HEAD: `58e0c3961cdbdb2bb7c3d1932b84b7a3b61e8448`
  (`fix(acceptance): harden runtime health status`). Its name-status record adds
  the WI-033/WI-034 summaries, evidence packs, and handoffs.
- `deliverables/agent/WI-20260817-ATS-033-evidence-pack.md:34-37` directly
  references REQ-010 and WI-021/WI-022/WI-032 user summaries.
- `deliverables/agent/WI-20260817-ATS-034-evidence-pack.md:45-48` directly
  references REQ-010 and WI-032/WI-033 context artifacts.
- `deliverables/user/WI-20260817-ATS-034-summary.md:10-13` declares REQ-010
  and WI-033 summary dependencies.
- `git ls-files --error-unmatch -- <path>` fails and
  `git log --all -- <path>` returns no commit for each of the four minimum
  candidate paths. Each reports `??` from `git status --short -- <path>`.
- `git check-ignore -v -- <candidate paths>` returns no matching ignore rule.
  `.gitignore` contains no `deliverables/` ignore pattern.

## Classification Inventory

| Classification | Count | Paths / deterministic definition | Evidence-based disposition |
|---|---:|---|---|
| TRACK-CANDIDATE | 4 | `deliverables/user/REQ-20260817-ATS-010.md`; `deliverables/user/WI-20260817-ATS-{021,022,032}-summary.md` | Minimum direct-reference set absent from committed Git history. Future inclusion requires explicit approval. |
| REVIEW | 6 | `deliverables/agent/WI-20260817-ATS-{021,022,032}-{evidence-pack,handoff}.md` | Existing companion records are not directly referenced by committed WI-033/WI-034 files; preserve local pending an inclusion decision. |
| REVIEW | 3 | `deliverables/user/WI-20260817-ATS-023-summary.md`; `deliverables/agent/WI-20260817-ATS-023-{evidence-pack,handoff}.md` | WI-023 remains a future external-effect gate in committed records, not a completed predecessor dependency. |
| REVIEW | 7 | `deliverables/user/REQ-20260816-ATS-001.md`; `deliverables/user/WI-20260816-ATS-{001,002}-summary.md`; `deliverables/agent/WI-20260816-ATS-{001,002}-{evidence-pack,handoff}.md` | Different-REQ records with blocked states and no direct committed WI-033/WI-034 reference. |
| HISTORICAL | 9 | `deliverables/user/WI-20260809-ATS-068-summary.md`; eight `deliverables/agent/WI-20260809-ATS-068-*` artifacts | Prior accepted/completed/passed closure sequence with no direct committed WI-033/WI-034 reference. Inventory classification only; no archive metadata was changed. |
| KEEP-LOCAL | 76 | Every other path from the 105-file pre-write untracked-deliverables baseline | Neither direct reference nor ownership/commit eligibility was established. Preserve without change. |

WIs 024 and 025 are named as future gates but have no matching current
summary, evidence-pack, or handoff path. WIs 033 and 034 are fully tracked by
HEAD. These observations do not add file candidates.

## Commands and Results

| Command | Result |
|---|---|
| `git branch --show-current; git status --short; git rev-parse --show-toplevel` | Confirmed the required branch and revealed pre-existing untracked artifacts. |
| `git show --format=fuller --name-status --summary 58e0c3961cdbdb2bb7c3d1932b84b7a3b61e8448` | Confirmed WI-033/WI-034 deliverables were added by HEAD. |
| `rg -n "REQ-20260817-ATS-010|WI-20260817-ATS-(021|022|023|024|025|032|033|034)|deliverables/(user|agent)/" <committed WI-033/WI-034 files>` | Identified exact direct path references and future-gate-only IDs. |
| `git ls-files --error-unmatch -- <path>` and `git log --all -- <path>` | Four direct-reference paths are untracked and absent from all reachable Git history. |
| `git check-ignore -v -- <candidate paths>` | Exit 1 with no matching rule; candidates are not ignored. |
| Deterministic SHA-256 over sorted `path:file-sha256` lines for pre-existing untracked deliverables | 105 paths; baseline `0a446d6466d6ba295ded1db1bef2b60d1fa6356b2b8648b748153e7fac4e9496`. |

## Tests

- `git diff --check` -> PASS: no output and zero exit status after both WI-035
  reports were created.

## No-Mutation Proof

- The two output paths did not exist before this WI.
- The pre-existing untracked-deliverables baseline contained 105 paths and the
  SHA-256 listed above. The same 105-path manifest and SHA-256 were reproduced
  after the permitted writes.
- `git diff --name-only` and `git diff --cached --name-only` remained empty.
- No `git add`, commit, push, reset, restore, delete, rename, runtime, port,
  database, secret, or external-system command was used.

## Risks / Rollback

- Risk: a future broad cleanup or bulk staging operation could include
  preserve-local artifacts whose ownership was not established by this WI.
- Required approval: explicitly approve the four TRACK-CANDIDATE paths before
  staging/committing them, and separately decide whether any REVIEW artifact is
  part of a complete predecessor bundle.
- Rollback: if superseded, remove only this WI's two reports under a separately
  authorized cleanup; do not alter any pre-existing artifact.
