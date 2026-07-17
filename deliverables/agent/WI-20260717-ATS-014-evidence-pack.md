---
version: 1.0
last_updated: 2026-07-17
project: ATS
owner: Documentation Ops
category: evidence-pack
status: confirmed
dependencies:
  - path: WI-20260717-ATS-014-handoff.md
    reason: Authorized scope, acceptance criteria, and final cleanup state
  - path: ../user/REQ-20260716-ATS-004.md
    reason: Approved V1 consolidation requirement
  - path: ../user/WI-20260717-ATS-012-summary.md
    reason: V1 readiness and cleanup preflight summary
  - path: WI-20260717-ATS-012-evidence-pack.md
    reason: Detailed preflight and preservation evidence
---

# Evidence Pack: WI-20260717-ATS-014

## Summary

- Recorded the approved post-baseline local branch/worktree cleanup, preserved recovery tags, and final repository connectivity audit.

## Scope / DoD Check

- [x] Recorded removal of 2 auxiliary worktrees and 35 prunable registrations.
- [x] Recorded removal of 5 ordinary, 35 Claude, and 3 archive-backed local branches.
- [x] Recorded the single remaining local branch and worktree.
- [x] Recorded both pre-consolidation tags and all three archive tags with exact commit mappings.
- [x] Recorded the intended untracked screenshot ZIP and distinguished WI documentation entries.
- [x] Recorded that remote refs were untouched and no push occurred.
- [x] Recorded connectivity-only fsck exit `0` and distinguished dangling objects from connectivity errors.
- [x] Confirmed that no dangling object prune was performed.

## Reference Documents (Tier 0-2)

| Tier | Document | Reason |
|---|---|---|
| 0 | `docs/standards/core-principles.md` | Constitution and transparent traceability |
| 0 | `docs/standards/documentation-standards.md` | Deliverable structure and metadata |
| 0 | `docs/standards/glossary.md` | Canonical WI and repository terminology |
| 1 | `docs/policies/versioning-policy.md` | Tag preservation and version recovery |
| 1 | `docs/policies/execution-policy.md` | Approved destructive-operation boundaries |
| 1 | `docs/policies/quality-gates.md` | Verification and PASS/BLOCK disposition |

Injected work context: `deliverables/user/REQ-20260716-ATS-004.md`, `deliverables/user/WI-20260717-ATS-012-summary.md`, and `deliverables/agent/WI-20260717-ATS-012-evidence-pack.md`. Assignee: `docops`; task type: documentation closeout.

## Exact Removed Inventory

### Worktrees and Registrations

- Auxiliary worktrees removed: 2 (`codex/acceptance-preview`, `codex/client-demo-stable`).
- Prunable registrations removed: 35, corresponding exactly to the Claude branches below.

### Ordinary Local Branches (5)

`codex/acceptance-preview`, `codex/client-demo-stable`, `codex/p0-release-blockers`, `codex/payment-integration-clean`, `dev/kyoung`.

### Claude Local Branches (35)

`claude/adoring-agnesi`, `claude/brave-nobel`, `claude/brave-yalow`, `claude/charming-chandrasekhar`, `claude/distracted-bhabha`, `claude/dreamy-perlman`, `claude/elastic-mestorf`, `claude/epic-ptolemy`, `claude/exciting-ellis`, `claude/friendly-rhodes`, `claude/funny-albattani`, `claude/gifted-khorana`, `claude/hardcore-rubin`, `claude/heuristic-ride`, `claude/infallible-northcutt`, `claude/inspiring-leakey`, `claude/interesting-matsumoto`, `claude/jolly-poincare`, `claude/laughing-cartwright`, `claude/loving-villani`, `claude/lucid-swanson`, `claude/musing-goldwasser`, `claude/musing-morse`, `claude/nervous-khorana`, `claude/nice-shamir`, `claude/pensive-ritchie`, `claude/quizzical-snyder`, `claude/recursing-diffie`, `claude/sad-cerf`, `claude/strange-moser`, `claude/stupefied-herschel`, `claude/sweet-ride`, `claude/vigilant-varahamihira`, `claude/wizardly-golick`, `claude/youthful-tesla`.

### Archive-Backed Local Branches (3)

| Removed branch | Preserved tag | Preserved commit |
|---|---|---|
| `master` | `archive/pre-v1-master-20260717` | `5a67f3a6cab964816a8f0afda9732a8e4d4e36f4` |
| `codex-payment-integration-design` | `archive/pre-v1-payment-integration-design-20260717` | `1f1a1f1ee685996be4714e5f4acc36e04248eb4a` |
| `codex-sr-91-tag-taxonomy-layout` | `archive/pre-v1-sr91-tag-layout-20260717` | `d0c17bd375efe5f075f8e5afc8d74ddc72a63afb` |

## Preserved Inventory and Rollback Mapping

| Purpose | Preserved ref | Commit |
|---|---|---|
| Client pre-consolidation recovery | `v1-pre-consolidation-client-20260716` | `cd876fcf84b3cb2490c27420c6c53a87a35b982d` |
| Development pre-consolidation recovery | `v1-pre-consolidation-dev-20260716` | `a96d2e0c5d249723bbf449b6834299a04cf2ad30` |
| Archived master tip | `archive/pre-v1-master-20260717` | `5a67f3a6cab964816a8f0afda9732a8e4d4e36f4` |
| Archived payment-design tip | `archive/pre-v1-payment-integration-design-20260717` | `1f1a1f1ee685996be4714e5f4acc36e04248eb4a` |
| Archived SR-91 layout tip | `archive/pre-v1-sr91-tag-layout-20260717` | `d0c17bd375efe5f075f8e5afc8d74ddc72a63afb` |

Never invent deleted branch tips. Recovery must use the exact preserved tag/commit mapping above.

## Evidence Pointers

| Evidence | Pointer | Result |
|---|---|---|
| Authorized final state | `deliverables/agent/WI-20260717-ATS-014-handoff.md` | Baseline commit, final branch/worktree count, tags, status, ports, and fsck result |
| Exact Claude inventory | `deliverables/agent/WI-20260716-ATS-037-evidence-pack.md` | Exact 35 registrations/branch names |
| Bounded target groups | `deliverables/agent/WI-20260717-ATS-010/repository-readiness.md` | Ordinary, auxiliary, Claude, and archive-backed cleanup sets |
| Preflight result | `deliverables/agent/WI-20260717-ATS-012-evidence-pack.md` | Approved exact cleanup scope and preservation requirements |

## Commands and Observed Results

| Command | Observed result |
|---|---|
| `git rev-parse HEAD` | `37e8f94d85549ca41986ed09c0a15ddecc0276b6` |
| `git branch --show-current` | `codex/p1-acceptance-hardening` |
| `git branch --format='%(refname:short)'` | One local branch: `codex/p1-acceptance-hardening` |
| `git worktree list --porcelain` | One worktree at `C:/Users/jm991/Desktop/project/ATStudio`, on the official branch and baseline HEAD |
| `git tag --list --format='%(refname:short) %(objectname)' 'v1-pre-consolidation-*' 'archive/pre-v1-*'` plus `git rev-parse <tag>^{}` | All five preservation tags resolve; exact commit mappings are recorded above |
| `git status --short` | Cleanup-result residue is only `output/client-demo-screenshots-20260716-140514.zip`; the handoff and WI-014 outputs are additive documentation entries |
| `Get-NetTCPConnection -State Listen -LocalPort 8080,5173` | No listeners |
| `git fsck --connectivity-only --no-reflogs` | **PASS**, exit `0`; dangling local trees, blobs, and commits were reported but are not connectivity failures |

Remote refs were untouched, and no push occurred. The remote refs listed locally remained outside the local cleanup scope.

## Connectivity and Object Retention

- Exit `0` from `git fsck --connectivity-only --no-reflogs` confirms that all referenced objects required for repository connectivity are present.
- The command also reported dangling local objects. A dangling object is unreachable from current refs; it is not, by itself, a missing-object or connectivity error.
- No `git prune`, garbage collection, or equivalent object-deletion operation was run. The dangling objects were intentionally left in the local object database.

## Risks / Rollback

Risks:

- Deleted local branch names no longer provide recovery points; only the exact preserved tags and commits above are authoritative.
- Dangling objects may later be removed by an independent Git maintenance operation, but WI-014 did not prune them.
- WI documentation files add status entries after the cleanup snapshot; they must not be misclassified as cleanup residue.

Rollback:

- Recreate a needed local branch only from its corresponding preservation tag or exact commit listed above.
- Use `v1-pre-consolidation-client-20260716` or `v1-pre-consolidation-dev-20260716` for pre-consolidation recovery.
- No remote rollback is required because remote refs were untouched and no push occurred.

## Final Status

**PASS**
