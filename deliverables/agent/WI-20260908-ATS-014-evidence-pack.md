---
version: 1.0
last_updated: 2026-09-08
project: ATS
owner: MA
category: reference
status: active
---

# Evidence Pack: WI-20260908-ATS-014

## Summary

Promoted the verified V1 source to actual branch `main` after remote history preservation; retired the approved obsolete branches and client worktree without merging legacy policies or restarting runtime.

## Scope / DoD Check

- [x] Exact old tips remotely archived before ref deletion.
- [x] Client dirty HomePage files preserved in independent commit `a8854e8ca8ab989df52a30edec8315bee8c14e23` (2 files, 20 additions / 3 deletions).
- [x] Verified source `53284823b2824d04ee364f4c3f0ec9e8adeb4635` promoted; one local/remote `main`, one root worktree.
- [x] No unrelated-history merge, thumbnail-policy import, product change, server restart, DB/media/credential change or external transaction.
- [x] Current-document alignment and scoped validators; publication is verified separately after this evidence is committed.
- [x] Product tree, 125 unrelated untracked files and runtime identities preserved.

## Reference Documents

| Tier | Pointer | Purpose |
|---|---|---|
| 0 | `docs/standards/core-principles.md` | Approved scope and transparent evidence |
| 0 | `docs/standards/documentation-standards.md` | Metadata and current/historical distinction |
| 0 | `docs/standards/development-standards.md` | Scoped verification |
| 0 | `docs/standards/glossary.md` | Canonical terminology |
| 1 | `docs/policies/versioning-policy.md` | Archival traceability |
| 1 | `docs/policies/security-policy.md` | Do not publish runtime secrets |

Context: [REQ003](../user/REQ-20260908-ATS-003.md), [handoff](WI-20260908-ATS-014-handoff.md), [historical inventory](WI-20260908-ATS-013-branch-decision-register.md). DocOps owns the six current-doc edits; MA owns Git execution and integrated evidence.

## Remote Archive Ledger

Every entry below was published as an annotated tag and its peeled commit verified using `git ls-remote --tags origin` before any corresponding branch was removed. These nine tags remain on origin.

| Previous branch/snapshot | Remote tag | Preserved commit |
|---|---|---|
| `master` | `archive/pre-v1-master-20260717` | `5a67f3a6cab964816a8f0afda9732a8e4d4e36f4` |
| `codex-payment-integration-design` | `archive/pre-v1-payment-integration-design-20260717` | `1f1a1f1ee685996be4714e5f4acc36e04248eb4a` |
| `codex-sr-91-tag-taxonomy-layout` | `archive/pre-v1-sr91-tag-layout-20260717` | `d0c17bd375efe5f075f8e5afc8d74ddc72a63afb` |
| README-only `main` | `archive/pre-main-readme-20260908` | `736fdc4261bf741939c95129bade6a8665f2479e` |
| `codex/p1-acceptance-hardening` | `archive/pre-main-p1-hardening-20260908` | `3147873c42bfd7883fdaa92922c0485e5fc72621` |
| `codex/payment-integration-clean` | `archive/pre-main-payment-clean-20260908` | `7053cce472012b46293f1ab972bdd28a8130c216` |
| `dev/kyoung` | `archive/pre-main-dev-kyoung-20260908` | `153ae7e31ef5b85eb57ec619b3e8d6f50159065d` |
| Verified development source | `archive/pre-main-release-verified-20260908` | `53284823b2824d04ee364f4c3f0ec9e8adeb4635` |
| Client snapshot including dirty copy changes | `archive/pre-main-client-with-copy-20260908` | `a8854e8ca8ab989df52a30edec8315bee8c14e23` |

The client archival commit descends from original client tip `c5f83fc1f8f2c3341ee2b26d26086be85bd5cb2d`; its temporary thumbnail policy remains archival only. Two older local `v1-pre-consolidation-*` tags were retained unchanged and were not newly published.

## Execution Evidence

- GitHub API returned default `main`, repository admin/push permission, and `main` unprotected. The default name needed no API mutation.
- Remote `main` was replaced only with `--force-with-lease=refs/heads/main:736fdc4261bf741939c95129bade6a8665f2479e`. Live main then matched `5328482`.
- Renamed the root checkout branch to `main`, set upstream `origin/main`, refreshed symbolic `origin/HEAD`.
- Removed the seven non-main remote branches in one atomic push, each with its own exact-tip lease after verifying remote archive coverage.
- Resolved the exact approved client path: `C:/Users/jm991/AppData/Local/ATStudio/worktrees/v1-client-acceptance-20260817`. Clean tracked/untracked status, archived HEAD and no non-PowerShell process command line referencing it were verified. Ignored entries were limited to `.gradle/`, `build/`, `frontend/node_modules/`; no conventional uploads/private-uploads roots existed there.
- `git worktree remove --force` removed only that checked target and generated caches. Deleted its archived local branch and the merged local p1 branch. No blanket clean/reset/prune or root checkout deletion.
- Result: one root worktree at `C:/Users/jm991/Desktop/project/ATStudio`, local `main`, remote `origin/main` and symbolic `origin/HEAD -> origin/main` only.

## Verification Boundary

Before cleanup, local frontend `/`, backend `/api/tracks`, public frontend `/` and public `/api/tracks` each returned HTTP 200. Process identities: Cloudflare PID1888 started 21:00:16, frontend PID16160 started 21:00:21, backend PID24792 started 21:55:09 (KST, 2026-09-08). Neither source checkout renaming nor documentation edits replaces the pinned running JAR.

No application test suite or fresh production deployment was performed for this Git/document-only change. A runtime HTTP 200 check is not full acceptance, security approval or proof of configured external deployment automation. SR-93 remains OPEN.

## Final Receipt

- Current docs: `docs/index.md`, `docs/client/testing-guide.md`, `docs/client/_internal-feature-map.md`, `docs/payment/feature-inventory.md`, `docs/payment/index.md`, `docs/SR/SR-93.md`. The central current baseline is `docs/index.md#current-v1-baseline`; historical inventory/tests remain scoped observations, not fresh validation. [DocOps review](WI-20260908-ATS-014-doc-review.md) records the independent wording check.
- `python .agents/skills/validate-docs/scripts/validate_docs.py`: exit 0, all Tier 0 files / links / index passed, 682 supported traceability IDs. `git diff --check` passed.
- SHA-256 inventory before/after: 125 unrelated untracked files preserved byte-for-byte, zero missing/changed. Stage only the current WI013/WI014/REQ003 records and six named current docs; the earlier 125 artifacts remain untracked.
- `git diff 5328482 --exit-code -- . ':(exclude)docs/**' ':(exclude)deliverables/**'`: exit 0. No tracked product/config/runtime source change.
- Final runtime recheck: same PID1888/16160/24792 and start times, four local/public HTTP 200 responses. Pinned JAR hash remains `5AE38AC932388E24223A723DDF3FC9BD2DB2B017B3ABC6F91496BCE1BC7F37C2`.
- Publication boundary: the consolidation documentation is delivered by the commit containing this record. After committing, compare `git rev-parse HEAD` with `git ls-remote origin refs/heads/main`; the final user response supplies the verified delivery SHA. No self-referential commit hash or speculative production claim is embedded here.

## Risks / Recovery

- To inspect retired work: `git fetch origin --tags`, then `git log <archive-tag>` or `git show <archive-tag>:<path>`. To resume it deliberately, create a new `codex/` branch from that tag in a separate approved worktree. Do not merge the client thumbnail exception into main by accident.
- Old main is recoverable from its tag. Restoring it as default content would discard the V1 baseline and needs explicit approval plus a fresh exact-ref lease; no rollback was executed.
- Local-only untracked outputs are not a remote backup. They are deliberately outside this commit scope and remain available in the root checkout.
- Next: bounded code/security review of the agreed main baseline, then unresolved production environment gates. No additional feature work or production GO is implied.
