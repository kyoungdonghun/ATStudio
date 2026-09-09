
> Historical public derivative, published in this working tree on 2026-09-09 by WI-20260909-ATS-018. Original dates, language, status, decisions and reported outcomes below are historical, not current approval or reverified recovery/production evidence. The original remains unchanged in private archive `v1-artifacts-20260909-084405`. See [A044: original identity, public SHA-256 and review](../../docs/registry/development-history-recovery-20260909.md#a044). Raw/local-only evidence pointers are lookup keys, not files supplied by a source checkout.

# Evidence Pack: WI-20260817-ATS-019

## Summary (one-liner)
- Completed a read-only branch and runtime inventory for client-acceptance and operational-rehearsal separation; no runtime, Git, database, provider, or email state changed.

## Scope / DoD Check
- [x] Recorded current branch, remote relationships, and single-worktree state.
- [x] Distinguished branches already merged into the current release candidate from branches with commits not yet contained in it.
- [x] Established that the existing local frontend process is not a complete acceptance runtime.
- [x] Identified independent runtime boundaries and approval gates.

## Reference Documents (Tier 0-2)

| Tier | Document | Reason |
|------|----------|--------|
| 0 | docs/standards/core-principles.md | Constitution |
| 0 | docs/standards/development-standards.md | Integration/runtime work |
| 1 | docs/policies/quality-gates.md | Verification evidence |
| 1 | docs/policies/security-policy.md | Secret and external-system boundary |
| 2 | docs/design/api-spec.md | API health boundary |
| 2 | docs/guides/ | Runtime/release context when relevant |

## Evidence Pointers
- Current tracked release candidate: `codex/v1-release-rehearsal-fixes` at `13fc37e8c74d3c17c8759ef7e65932b2db731f50`, aligned with `origin/codex/v1-release-rehearsal-fixes`.
- `origin/codex/p1-acceptance-hardening`, `origin/codex/payment-integration-clean`, and `origin/dev/PRIVATE-CONTRIBUTOR` are ancestors of the current HEAD; they are preservation-tag/delete-review candidates, not immediate deletion targets.
- `origin/master`, `origin/codex-payment-integration-design`, and `origin/codex-sr-91-tag-taxonomy-layout` still have commits absent from HEAD and require content disposition before any deletion.
- `scripts/acceptance/README.md`: fixed-port client acceptance lifecycle, environment-bundle contract, and external-effect boundaries.
- `src/main/resources/application-acceptance.yml`: acceptance profile environment-variable boundary.

## Commands & Outputs
- `git rev-list --count HEAD..<ref>` and `<ref>..HEAD` plus `git merge-base --is-ancestor <ref> HEAD`
  - Confirmed ancestry and unique-commit relationships for all current origin branches.
- `git worktree list --porcelain`
  - One current repository worktree only.
- `Get-CimInstance Win32_Process -Filter 'ProcessId = 24452'` and `Get-NetTCPConnection -State Listen -LocalPort 5173,8080`
  - A Vite process from the current worktree owns `127.0.0.1:5173`; no listener exists on `8080`.
- `GET http://127.0.0.1:5173/`
  - Frontend root responded, but no backend/tunnel was established by this inventory.

## Tests
- Non-mutating frontend root health check completed.
- No full acceptance health check was attempted because the mandatory backend/tunnel processes were absent.

## Risks / Rollback
- Risks:
  - The local `5173` Vite process prevents the documented fixed-port acceptance launcher from starting.
  - A current-worktree Vite process is not proof that the frozen release candidate is the source being served.
  - A Cloudflare quick-tunnel URL is ephemeral and cannot prove a stable production callback configuration.
- Rollback:
  - No mutation occurred; no rollback action is needed.

## Follow-ups
- WI-20260817-ATS-020: create separated source/runtime boundaries and a client-acceptance launch plan after the exact `5173` process action is approved.
- WI-20260817-ATS-021: operational configuration/rehearsal checklist without secrets.
- Obtain an explicit branch deletion list only after preservation tags and unique-commit disposition are approved.
