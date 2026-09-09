
> Historical public derivative, published in this working tree on 2026-09-09 by WI-20260909-ATS-018. Original dates, language, status, decisions and reported outcomes below are historical, not current approval or reverified recovery/production evidence. The original remains unchanged in private archive `v1-artifacts-20260909-084405`. See [A048: original identity, public SHA-256 and review](../../docs/registry/development-history-recovery-20260909.md#a048). Raw/local-only evidence pointers are lookup keys, not files supplied by a source checkout.

# Evidence Pack: WI-20260817-ATS-022

## Summary (one-liner)

- Repaired the acceptance launcher so child-specific backend environment isolation is enforced at the real process boundary while stdout and stderr remain continuously available in runtime log files.

## Scope / DoD Check

- [x] Root cause documented with source-level evidence.
- [x] Tunnel and frontend synthetic child processes were proven free of every backend-only variable name.
- [x] The backend synthetic child process was proven to receive every supplied backend variable value.
- [x] The launcher process environment was proven unchanged after all child spawns.
- [x] Existing stdout/stderr log files were proven to receive output while children were running.
- [x] Log handles were proven released after natural and forced synthetic child exit.
- [x] Probe observation publication was proven atomic and complete before parent assertions.
- [x] `scripts/acceptance/test-backend-environment.ps1` passed in 5 consecutive fresh PowerShell invocations.
- [x] `scripts/acceptance/test-dry-run.ps1` passed.
- [x] `git diff --check` passed.

## Reference Documents (Tier 0-2)

| Tier | Document | Reason |
|---|---|---|
| 0 | `docs/standards/core-principles.md` | Sustainable execution and security boundary |
| 0 | `docs/standards/development-standards.md` | Focused implementation and verification standard |
| 1 | `docs/policies/quality-gates.md` | Required regression and evidence checks |
| 1 | `docs/policies/security-policy.md` | Secret handling and environment boundary |
| 2 | `scripts/acceptance/README.md` | Acceptance lifecycle and log contract |
| Context | `deliverables/user/REQ-20260817-ATS-010.md` | Approved operational-rehearsal scope |
| Context | `deliverables/agent/WI-20260817-ATS-021-evidence-pack.md` | Prior acceptance preflight blocker |
| Context | `src/main/resources/application-acceptance.yml` | Acceptance profile input pointer |

**Injection Rules Applied**:
- Assignee: `se`
- Task type: focused acceptance lifecycle repair
- External backend bundle values, current environment values, and provider credentials were excluded.

## Evidence Pointers

- `scripts/acceptance/AcceptanceLifecycle.psm1`: `Start-AcceptanceOwnedProcess` now creates an explicit `ProcessStartInfo.EnvironmentVariables` map, removes every backend-only name, and overlays only intended child values.
- `scripts/acceptance/AcceptanceLifecycle.psm1`: `AcceptanceProcessLogPump` drains stdout/stderr asynchronously into the supplied log paths, flushes per line, removes event handlers, and closes writers after `WaitForExit()` drains pending output.
- `scripts/acceptance/AcceptanceLifecycle.psm1`: stream-attachment failure kills the just-started process tree when supported, waits best-effort, then disposes the pump while preserving the original failure.
- `scripts/acceptance/AcceptanceLifecycle.psm1`: `ProcessStartInfo.ArgumentList` is used when available; the fallback quotes arguments for older runtimes.
- `scripts/acceptance/test-backend-environment.ps1`: a temporary `pwsh` probe enumerates every backend-only name and writes only synthetic observations to a temporary fixture.
- `scripts/acceptance/test-backend-environment.ps1`: the probe publishes its completed JSON by same-directory temporary-file rename; the parent waits for a valid object containing every backend-only property before dereferencing it.
- `scripts/acceptance/test-backend-environment.ps1`: the probe emits fixed stdout/stderr markers while alive; the test verifies their log delivery and exclusive file access after natural and forced exit.

## Synthetic Reproduction

1. Import `scripts/acceptance/AcceptanceLifecycle.psm1` through the documented preflight.
2. Build the required and current optional backend bundle with synthetic marker values only.
3. Spawn tunnel, backend, and frontend `pwsh` probes through `Start-AcceptanceOwnedProcess`.
4. Publish each observation only after a completed JSON file is atomically renamed into place.
5. Wait until the observation has every backend-only property before inspecting its values.
6. Assert tunnel/frontend observe zero names from the complete backend-only allowlist.
7. Assert the backend observes exactly the supplied synthetic names and values.
8. Assert parent values are unchanged, fixed stdout/stderr markers reach both log files while the probe is alive, and log handles release after exit.

## Commands & Outputs

- `scripts/acceptance/test-backend-environment.ps1` -> PASS in 5 consecutive fresh PowerShell invocations.
  - Completed checks include atomic complete-observation publication, all-name child isolation, synthetic value propagation, parent-environment preservation, continuous log drainage, forced-exit handle release, and fixture cleanup.
- `scripts/acceptance/test-dry-run.ps1` -> PASS.
  - Parser, dry-run, readiness, cleanup, and secret-free output checks passed; PSScriptAnalyzer was not installed.
- `git diff --check` -> PASS before deliverable creation.

## Security and External-Action Assertion

- No actual backend environment bundle was read, supplied, or printed.
- No secret value was added to source, command-line arguments, test output, or this evidence pack.
- No server, tunnel, database, payment/refund, provider, SMTP, or external action was started.
- The frozen client-acceptance worktree at `$USERPROFILE\AppData\Local\ATStudio\worktrees\v1-client-acceptance-20260817` was not modified.

## Risks / Rollback

- Risks:
  - The asynchronous log pump is process-local runtime support; its intentional scope is acceptance-owned child processes only.
  - Actual acceptance startup remains a separate authorized operation and was not performed by this WI.
- Rollback:
  - Revert only `scripts/acceptance/AcceptanceLifecycle.psm1` and `scripts/acceptance/test-backend-environment.ps1`.
  - Remove only this WI's two deliverables if the focused patch is reverted.

## Follow-ups

- WI-20260817-ATS-023 may consume this repaired preflight only after its separate external-action approval gate is met.
