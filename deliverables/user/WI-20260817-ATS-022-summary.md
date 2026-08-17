# WI-20260817-ATS-022 Summary

## Result

- Repaired child-process backend-environment isolation in the PRIMARY development worktree only.
- Tunnel and frontend child processes now receive no backend-only variable names. The backend receives only the supplied synthetic backend bundle values plus the launcher-owned child values.
- Child stdout and stderr are asynchronously drained into the existing runtime log files while the child is running. Log handles close after both natural exit and forced owned-process exit.

## Root Cause

- `Start-AcceptanceOwnedProcess` temporarily changed the current PowerShell process environment before calling `Start-Process`.
- A real synthetic child-process probe showed that `Start-Process` did not honor those temporary removals at the child boundary. Tunnel and frontend inherited backend-only names from the launcher process.
- PowerShell 7 `Start-Process -Environment` was also rejected as the production path because it overlays inherited values rather than removing omitted names.
- The first synthetic probe contract published directly to its observation path. The parent could observe path creation before the JSON write completed and then dereference a null or incomplete observation.

## Security Property Preserved

- The launcher never mutates the current process environment.
- It builds an explicit child environment from the current process baseline, removes every allowlisted backend-only name, overlays only the intended child values, and assigns that exact map to `ProcessStartInfo.EnvironmentVariables`.
- The backend bundle path and bundle values are not sent as command-line arguments, logged by the test, or written to these deliverables.
- The probe writes its complete synthetic JSON to a temporary path and atomically renames it. The parent accepts it only after every backend-only property is present.

## Verification

- `scripts/acceptance/test-backend-environment.ps1` -> PASS 5/5 consecutive fresh PowerShell invocations: external-bundle validation, required and optional allowlist coverage, all-name child-process isolation, synthetic backend value propagation, unchanged parent environment, continuous stdout/stderr drainage, natural/forced-exit log-handle cleanup, atomic observation publication, launch order, and temporary fixture cleanup.
- `scripts/acceptance/test-dry-run.ps1` -> PASS: parser, dry-run, readiness, cleanup, and secret-free-output checks. PSScriptAnalyzer was not installed.
- `git diff --check` -> PASS.

## Preserved Boundaries

- No acceptance server, tunnel, runtime manifest, external backend bundle, database, payment/refund action, SMTP action, provider action, or client-acceptance worktree change occurred.

## Rollback

- Revert only the focused changes to `scripts/acceptance/AcceptanceLifecycle.psm1` and `scripts/acceptance/test-backend-environment.ps1`.
