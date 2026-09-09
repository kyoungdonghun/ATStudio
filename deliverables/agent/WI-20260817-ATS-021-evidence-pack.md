
> Historical public derivative, published in this working tree on 2026-09-09 by WI-20260909-ATS-018. Original dates, language, status, decisions and reported outcomes below are historical, not current approval or reverified recovery/production evidence. The original remains unchanged in private archive `v1-artifacts-20260909-084405`. See [A046: original identity, public SHA-256 and review](../../docs/registry/development-history-recovery-20260909.md#a046). Raw/local-only evidence pointers are lookup keys, not files supplied by a source checkout.

# Evidence Pack: WI-20260817-ATS-021

## Summary (one-liner)
- Created the frozen client-acceptance branch and source worktree, then stopped before lifecycle startup because a required backend-environment preflight failed; no external bundle, runtime, or acceptance-owned process was used.

## Scope / DoD Check
- [x] `codex/v1-client-acceptance-20260817` resolves to `13fc37e8c74d3c17c8759ef7e65932b2db731f50`.
- [x] The dedicated source worktree is distinct from the development worktree and has the frozen commit checked out.
- [x] The documented dry-run contract preflight passed without secret-bearing output.
- [x] The documented backend-environment contract preflight was executed and its failure was treated as a launch blocker.
- [x] No Vite process was terminated after the blocker; therefore the required immediate-before-stop identity re-check was not applicable.
- [x] No lifecycle-owned process, runtime manifest, local/public endpoint, payment/refund action, SMTP send, or manual database operation was created.
- [ ] Acceptance launcher readiness: blocked by the failed preflight.

## Reference Documents (Tier 0-2)

| Tier | Document | Reason |
|---|---|---|
| 0 | `docs/standards/core-principles.md` | Runtime isolation and approval boundary |
| 0 | `docs/standards/development-standards.md` | Integration/runtime execution standard |
| 1 | `docs/policies/quality-gates.md` | Evidence and execution quality gates |
| 1 | `docs/policies/security-policy.md` | External-bundle and sensitive-information boundary |
| 2 | `scripts/acceptance/README.md` | Mandatory lifecycle, readiness, and stop contract |
| 2 | `scripts/database/README.md` | Acceptance-database guardrail |
| 2 | `docs/SR/SR-93.md` | Related acceptance context |

**Injection Rules Applied**:
- Assignee: `qa-integ`
- Task type: acceptance runtime verification
- Bundle values and contents were excluded from this evidence.

## Evidence Pointers (required)
- Branch: `codex/v1-client-acceptance-20260817` -> `13fc37e8c74d3c17c8759ef7e65932b2db731f50`.
- Source worktree: `$USERPROFILE\AppData\Local\ATStudio\worktrees\v1-client-acceptance-20260817` -> `13fc37e8c74d3c17c8759ef7e65932b2db731f50`.
- Runtime root: `$USERPROFILE\AppData\Local\ATStudio\acceptance-client-20260817` did not exist after the blocker.
- `scripts/acceptance/status.ps1 -RuntimeRoot <approved-runtime-root>` returned `not-started` with no services.
- Listener check after the blocker: only the pre-existing `127.0.0.1:5173` listener owned by PID 24452 remained; no `8080` listener existed.

## Commands & Outputs
- `git branch codex/v1-client-acceptance-20260817 13fc37e8c74d3c17c8759ef7e65932b2db731f50` -> passed.
- `git worktree add <approved-source-worktree> codex/v1-client-acceptance-20260817` -> passed.
- `scripts/acceptance/test-dry-run.ps1` -> passed; parser, dry-run, readiness, cleanup, and secret-free-output contract checks passed. Script analyzer was not installed.
- `scripts/acceptance/test-backend-environment.ps1` -> failed; seven child-process environment-isolation assertions failed. No external bundle was supplied to this test or its result.
- `scripts/acceptance/status.ps1 -RuntimeRoot <approved-runtime-root>` -> `not-started`; no owned services.

## Tests
- `scripts/acceptance/test-dry-run.ps1` -> PASS.
- `scripts/acceptance/test-backend-environment.ps1` -> FAIL; launch blocker under WI-021.
- `scripts/acceptance/start.ps1 -RuntimeRoot <approved-runtime-root> -DryRun` -> not run after required preflight failure.
- `scripts/acceptance/start.ps1 -RuntimeRoot <approved-runtime-root> -BackendEnvironmentPath <redacted>` -> not run.

## Risks / Rollback
- Risks:
  - The failing environment-isolation preflight must be diagnosed and corrected before this lifecycle can launch safely.
  - PID 24452 remains active and continues to reserve `127.0.0.1:5173`; it was intentionally preserved because the lifecycle did not pass preflight.
- Rollback / stop:
  - No acceptance-owned process or runtime manifest exists, so no rollback command is required for this attempt.
  - For any later successful documented launch, use only `scripts/acceptance/stop.ps1 -RuntimeRoot "$USERPROFILE\AppData\Local\ATStudio\acceptance-client-20260817"`.
  - Do not manually terminate acceptance processes. Re-verify the exact approved Vite process identity and loopback listener immediately before its separately authorized termination.

## Follow-ups
- Resolve the `test-backend-environment.ps1` child-process environment-isolation regression, then re-run the complete documented preflight and dry run before any launch attempt.
- WI-20260817-ATS-023 remains blocked because WI-021 has no live client-acceptance runtime.
