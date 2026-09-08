---
version: 1.0
last_updated: 2026-09-08
project: ATS
owner: MA
category: reference
status: active
dependencies:
  - path: ../user/REQ-20260908-ATS-003.md
    reason: Approved non-destructive baseline assessment
  - path: WI-20260908-ATS-013-handoff.md
    reason: Git and documentation ownership split
---

# WI-20260908-ATS-013: Branch Decision Register

## Verified Baseline

- Mail closeout commit: `53284823b2824d04ee364f4c3f0ec9e8adeb4635`, exactly seven WI011/WI012/REQ002 documentation paths. `git push origin HEAD:refs/heads/codex/v1-release-rehearsal-fixes` succeeded; live `ls-remote` matched HEAD.
- Source checkout: `C:/Users/jm991/Desktop/project/ATStudio`, branch `codex/v1-release-rehearsal-fixes`. No tracked product modification or staged change remains after that commit. New assessment documents and 125 pre-existing untracked artifacts are separate, not a clean-all-files claim.
- Fresh remote inspection: `git fetch origin` without prune; `git ls-remote --symref origin HEAD` reports `refs/heads/main` at `736fdc4261bf741939c95129bade6a8665f2479e`. Eight remote branch heads, three local branches and two worktrees exist.
- Default `main` contains only `README.md` with two lines. `git merge-base HEAD origin/main` returned no common ancestor (exit 1). It does not contain the application. Neither its name nor GitHub default status makes it the tested code baseline.
- No branch, tag, worktree, default-branch setting, runtime or database was changed by this assessment. The fetch updated remote-tracking references only.

## Candidate Table

Counts are `git rev-list --left-right --count HEAD...REF` at `5328482`: current-only / other-only. Patch count is `--cherry-pick --right-only --no-merges`, not proof that current product functionality is missing. L = local; R = remote.

| Branch | Location / tip | Counts | Other unique non-merge patches | Proposed handling, NOT executed |
|---|---|---:|---:|---|
| `codex/v1-release-rehearsal-fixes` | L + R / `5328482` | 0 / 0 | 0 | KEEP as the verified baseline; recommend making it the GitHub default |
| `codex/p1-acceptance-hardening` | L + R / `3147873` | 56 / 0 | 0 | Fully included; tag/push preservation, then remove both refs after approval |
| `codex/payment-integration-clean` | R / `7053cce` | 124 / 0 | 0 | Fully included; preserve tip, then remove remote ref after approval |
| `dev/kyoung` | R / `153ae7e` | 93 / 0 | 0 | Fully included; preserve tip, then remove remote ref after approval |
| `master` | R / `5a67f3a` | 179 / 3 | 1 | Existing exact-tip local archive tag; push tag first. Do not merge old instructions into current code |
| `codex-payment-integration-design` | R / `1f1a1f1` | 131 / 10 | 8 | Existing exact-tip local archive tag; preserve historical payment/tag work rather than blindly replaying it |
| `codex-sr-91-tag-taxonomy-layout` | R / `d0c17bd` | 131 / 3 | 3 | Existing exact-tip local archive tag; preserve historical taxonomy/UI work; deletion requires approval |
| `main` | R / `736fdc4` | 240 / 1 | 1 | Unrelated README-only history. Preserve tip, change default first, then optional deletion. No force push or unrelated-history merge proposed |
| `codex/v1-client-acceptance-20260817` | L / `c5f83fc` | 11 / 2 | 2 | HOLD until tip AND dirty files are preserved; do not merge temporary policy overrides |

## Unique History And Preservation

| Existing local annotated tag | Exact preserved tip | Published remotely? |
|---|---|---|
| `archive/pre-v1-master-20260717` | `5a67f3a6cab964816a8f0afda9732a8e4d4e36f4` | No |
| `archive/pre-v1-payment-integration-design-20260717` | `1f1a1f1ee685996be4714e5f4acc36e04248eb4a` | No |
| `archive/pre-v1-sr91-tag-layout-20260717` | `d0c17bd375efe5f075f8e5afc8d74ddc72a63afb` | No |

`git ls-remote --tags origin` succeeded with no tag refs. Two additional older local tags (`v1-pre-consolidation-client-20260716`, `v1-pre-consolidation-dev-20260716`) exist but do not preserve the current client tip. Local tags alone are not remote backup.

- `master`'s patch-unique non-merge commit is `510cfe4`, changing only `CLAUDE.md`. Two additional graph-unique commits are merges. This is retained historical context, not an unmerged payment implementation to replay.
- Payment-design history covers initial mock/one-time/Toss recurring paths, expired subscription handling, payment design/UX docs and older SR-91 taxonomy work. Graph/patch differences must not be interpreted as a missing current feature. Exact-tip archival preserves it without reviving intentionally retired paths.
- SR-91 unique commits: `f521a6e`, `c3b2ad1`, `d0c17bd`, covering tag layout and contemporary document alignment.
- These observations classify preservation risk, not a new full functional audit of old commits.

## Client Worktree

Path: `C:/Users/jm991/AppData/Local/ATStudio/worktrees/v1-client-acceptance-20260817`.

- Tip commits unique to client: `c807a3f` (temporary non-square thumbnail allowance) and `c5f83fc` (resolution guidance/prevalidation and evidence). The user explicitly described the thumbnail-policy exception as temporary client-test work; do not promote it as a new V1 product policy.
- Dirty paths: `frontend/src/pages/public/HomePage.tsx`, `frontend/src/pages/public/HomePage.test.tsx` (20 insertions / 3 deletions against client HEAD).
- Comparison against the current verified branch: HomePage product code has no diff; test assertions/naming differ. A tag of client HEAD would NOT preserve either dirty file; store and verify a dirty patch or a dedicated archival commit before removal.
- Ignored paths reported by Git: `.gradle/`, `build/`, `frontend/node_modules/`. Conventional `uploads` and `private-uploads` directories are absent in this worktree. This limited check is not approval to delete ignored data.
- No non-PowerShell process command line referenced this client path during inspection. This is a command-line scan, not an exhaustive open-handle audit.

## Runtime Boundary

The active backend is PID24792 (21:55), frontend PID16160 (21:00), Tunnel PID1888 (21:00). The configured source/runtime belongs to the development checkout, not the client worktree; these process identities/start times were unchanged by this assessment. The pinned test JAR remains separate from Git ref names. No restart, SMTP or financial request was made in stage 2.

## Deployment And Current Documents

The independent [DocOps evidence](WI-20260908-ATS-013-evidence-pack.md) and [summary](../user/WI-20260908-ATS-013-summary.md) complete the documentation portion. No repository-owned deployment CI/configuration was found in the bounded inventory of 3,203 tracked paths; this does not establish whether an external host has its own deployment pipeline. The acceptance launcher derives its checkout from its own location and starts Gradle/Vite there, rather than pinning a named Git branch. It is not a production pipeline.

Six current files contain seven inspected correction locations (the internal feature map has both baseline and quality-boundary wording):

| Current file | Correction target |
|---|---|
| `docs/index.md:73` | Unqualified old official p1 baseline and no-client claim |
| `docs/client/testing-guide.md:28` | Current testing baseline and checkout/worktree distinction |
| `docs/client/_internal-feature-map.md:60` | Old baseline/no-client claim and dated focused-only quality boundary |
| `docs/payment/feature-inventory.md:184` | Old branch/dependency observation presented as current |
| `docs/payment/index.md:60` | Distinguish dated product commit/receipt from later mail/documentation closeout; keep historical WI005 table |
| `docs/SR/SR-93.md:92` | Same dated receipt distinction, without closing remaining production gates |

These are independently inspected correction candidates, not completed edits. When updating the mail-related receipt sections, link the already verified WI011/WI012 outcomes precisely. Preserve dated REQ/WI/audit records, old SHAs and historical cached-ref counts. Six sampled evidence links were present and tracked; this was not a fresh-clone test or a complete audit of the 125 preserved untracked artifacts.

## Initial Recommendation (Superseded By Subsequent Approval)

1. Keep `codex/v1-release-rehearsal-fixes` as the sole maintained source baseline; recommend changing GitHub default to it. Do not force-push over the unrelated `main`, and do not merge old payment paths merely to make the graph connected.
2. Publish the three existing exact-tip archive tags; create/publish explicit preservation tags for remaining retired tips and preserve client dirty files separately. Verify live remote peeled tag SHAs before any deletion.
3. After exact-scope approval, remove fully included and archived obsolete refs. Client worktree removal requires its dirty/ignored-content check and explicit approval. No generic `git clean` or blanket worktree deletion.
4. Align current documentation with the confirmed branch and distinguish it from historical REQ/WI/audit snapshots. Preserve historical records rather than mass-rewriting their old SHAs or URLs.
5. Freeze the agreed audit baseline only after the decision and cleanup. Production host/domain/secrets/DB-media provisioning and the bounded security review remain separate later stages.

## Validation And Decisions Pending At WI013 Completion

- Mail commit passed docs validation (679 IDs), exact seven-file staged-scope check, staged whitespace check and focused secret-pattern checks; no product tests rerun for documentation-only changes.
- Stage 2 uses live refs, commit/tree/patch comparisons, worktree status and process ownership observations. A default-branch mismatch is confirmed; whether any external hosting provider auto-deploys `main` is not known from these Git facts.
- Independent doc validation passed (681 IDs, exit 0); combined final whitespace checks passed. Assessment outputs are separate uncommitted additions after the already-pushed mail closeout. No product tests were rerun.
- Approval needed: default-branch change, exact obsolete ref deletion, tag publication plan, client dirty-change preservation/removal. No production GO or completed physical cleanup is claimed.

## Subsequent Approved Execution

The user subsequently approved using the actual name `main`, remote preservation and removal of obsolete branches/client worktree. This supersedes item 1 of the initial recommendation; it does not retroactively change the read-only scope, counts or observations above. [REQ003](../user/REQ-20260908-ATS-003.md) records the exact approval. [WI014 execution evidence](WI-20260908-ATS-014-evidence-pack.md) records the completed promotion, remote archives and cleanup. Production deployment and the bounded security audit remain separate.
