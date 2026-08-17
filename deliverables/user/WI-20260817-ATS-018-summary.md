---
version: 1.0
last_updated: 2026-08-17
project: ATS
owner: cr
category: work-summary
status: complete
related_wi: WI-20260817-ATS-018
dependencies:
  - path: REQ-20260817-ATS-009.md
    reason: Approved V1 release-candidate scope
  - path: WI-20260817-ATS-013-summary.md
    reason: Consent and session-contract predecessor
  - path: WI-20260817-ATS-014-summary.md
    reason: Router remediation predecessor
  - path: WI-20260817-ATS-015-summary.md
    reason: Current-document reconciliation predecessor
  - path: WI-20260817-ATS-016-summary.md
    reason: Guarded manifest predecessor
  - path: WI-20260817-ATS-017-summary.md
    reason: Independent release-audit predecessor
---

# WI-20260817-ATS-018 Summary

## Result

The approved `REQ-20260817-ATS-009` release-candidate assembly is recorded as
six ordered, reviewable commits. This assembly did not run a database,
provider, refund, email, or external production operation.

## Commit Record

| Order | Commit | Category | Attributable release evidence |
| --- | --- | --- | --- |
| 1 | `9acf2d5` | `feat(auth): record consent and enforce verified password sessions` | WI-013 |
| 2 | `eca987e` | `fix(frontend): make logout confirmation explicit` | WI-013 |
| 3 | `7ea025c` | `chore(frontend): upgrade React Router to 7.18.2` | WI-014 |
| 4 | `44e5074` | `chore(database): record guarded 43-table disposable manifest` | WI-016 |
| 5 | `1058cea` | `docs(release): synchronize V1 current-state boundaries` | WI-015, WI-017, WI-018 |
| 6 | `HEAD` | `docs(work): record REQ-20260817-ATS-009 WI evidence` | WI-018 |

`HEAD` in row 6 is this self-recording commit and resolves to its immutable
commit ID after creation. The preceding five IDs are literal because they were
available before this document was committed.

## Approved Documentation Correction

- `docs/design/db-schema.md` remains the authoritative v24.3 source.
- `docs/design/index.md` and `docs/registry/project-registry.md` now cite
  v24.3 while preserving the current 43-table/43-entity and recorded-manifest
  facts.
- Commit 4 was not amended or rewritten.

## Validation Evidence

- WI-017 records passing backend, frontend, source-guard, dependency-audit,
  documentation, and whitespace gates for this release-candidate scope.
- This continuation changed documentation only; implementation test suites were
  not rerun.
- Commit 5 and Commit 6 each had scoped staged path and staged-diff review;
  `git diff --cached --check` passed before their commits.
- The final post-assembly checks are reproducible with:

```powershell
git log --oneline -6
git status --short --branch
git diff --check HEAD
python .agents/skills/validate-docs/scripts/validate_docs.py
```

## Residual Worktree Boundary

The release assembly leaves only untracked, out-of-scope categories: 45
agent-facing deliverable entries, 29 user-facing deliverable entries, 2 output
entries, and 1 database-patch directory. None is part of these six commits.

## External Production Gates

- Approve a retained-data migration and rollback design.
- Run an explicitly approved live Toss rehearsal and reconcile local persisted
  state separately from provider results.
- Verify SMTP delivery, HTTPS/proxy/CORS/callback origins, and the intended
  production configuration.
- Rehearse backup/restore, logging, monitoring, alerting, scheduler ownership,
  and incident procedures.
- Complete acceptance verification, client acceptance, and explicit release
  approval.

## Rollback

Revert the assembly in reverse order without reset, checkout, or amend:

```powershell
git revert HEAD
git revert 1058cea
git revert 44e5074
git revert 7ea025c
git revert eca987e
git revert 9acf2d5
```
