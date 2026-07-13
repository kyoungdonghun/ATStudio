# Evidence Pack: WI-20260713-ATS-001

## Scope

- REQ: `REQ-20260713-ATS-001`
- Purpose: freeze the approved client-document and full-audit baseline before product-source changes.
- The assigned explorer was stopped after repeated timeouts without outputs; MA completed the manifest from the verified WI-20260711 audit evidence and current worktree.

## Include Manifest

- `docs/client/` tracked modifications/deletions and four replacement Markdown files.
- `docs/audit/index.md` and `docs/audit/full-system-audit-20260713.md`.
- `docs/index.md` current user changes plus the additive audit-index values.
- `output/pdf/atstudio-client-testing-guide.pdf`.
- `deliverables/user/REQ-20260711-ATS-001.md`.
- `deliverables/user/WI-20260711-ATS-001-summary.md` through `WI-20260711-ATS-020-summary.md`.
- `deliverables/agent/WI-20260711-ATS-001-handoff.md` through `WI-20260711-ATS-020-handoff.md`.
- `deliverables/agent/WI-20260711-ATS-001-evidence-pack.md` through `WI-20260711-ATS-020-evidence-pack.md`.
- `deliverables/user/REQ-20260713-ATS-001.md` and the three WI-20260713-ATS-001 artifacts.

## Exclude Manifest

- `cloudflared.err.log`
- `cloudflared.out.log`
- `frontend/vite.err.log`
- `frontend/vite.out.log`
- All product source, schemas, data, secrets, generated build trees, and unrelated deliverables.

## Verification Inputs

- `WI-20260711-ATS-001-evidence-pack.md`: client Markdown/PDF substantive content match and render inspection.
- `WI-20260711-ATS-020-evidence-pack.md`: final audit docs validation and diff-check.
- Current `git status --short`: explicit dirty-path inventory.

## Git Execution Contract

- Stage only the include manifest.
- Inspect `git diff --cached --name-status` and reject any path outside the manifest.
- Run docs validation and `git diff --cached --check` before commit.
- Commit message is Korean and limited to the document/audit baseline.
- Create `codex/p0-release-blockers` only after commit succeeds.

## Rollback

- Before push, the baseline commit can be reverted with a normal `git revert <commit>` only when explicitly requested.
- Runtime logs remain untracked and untouched.
