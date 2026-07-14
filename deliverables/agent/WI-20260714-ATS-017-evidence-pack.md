---
version: 1.1
last_updated: 2026-07-15
project: ATS
category: evidence-pack
status: stable
related_wi: WI-20260714-ATS-017
---

# Evidence Pack: WI-20260714-ATS-017

## Summary (one-liner)

- Calibrated the WI-017 acceptance lifecycle review by tightening HTTP readiness to 200-399, moving abnormal-start cleanup to a `completedSuccessfully` + `finally` guarantee, and replacing the host-killing cleanup test with a catchable mock failure plus static structure assertions.

## Scope / DoD Check

- [x] `Test-AcceptanceUrlReady` now treats only HTTP 200-399 as ready.
- [x] `Start-AcceptanceEnvironment` now performs owned-process cleanup from `finally` on abnormal exit, with tunnel -> frontend -> backend ordering.
- [x] Cleanup remains idempotent because repeated passes route through `Stop-AcceptanceOwnedService`, which no-ops for non-owned or already-stopped processes.
- [x] Verification stayed within parser/dry-run/status/stop no-manifest boundaries; no live server or tunnel was started.
- [x] WI-017 evidence and summary were updated to match the corrected implementation and the actual verification method.

## Reference Documents (Tier 0-2)

| Tier | Document | Reason |
|---|---|---|
| 0 | `docs/standards/core-principles.md` | Traceability and execution-discipline baseline |
| 0 | `docs/standards/development-standards.md` | Script/test evidence expectations |
| 0 | `docs/standards/documentation-standards.md` | Deliverable update format |
| 0 | `docs/standards/glossary.md` | Canonical WI/evidence terminology |
| WI | `deliverables/agent/WI-20260714-ATS-017-handoff.md` | Scope, constraints, and output contract |

## Evidence Pointers

### Files changed

- `scripts/acceptance/AcceptanceLifecycle.psm1`
  - Added reusable cleanup helpers.
  - Changed readiness success range from `< 500` to `< 400`.
  - Reworked startup cleanup to use `$completedSuccessfully` with `finally`.
- `scripts/acceptance/test-dry-run.ps1`
  - Added mocked HTTP-status contract coverage for 200/399 success and 403/404 failure.
  - Added no-manifest `status.ps1` / `stop.ps1` assertions.
  - Added catchable abnormal-start cleanup verification.
  - Added static assertions for the `completedSuccessfully` + `finally` structure.
- `deliverables/agent/WI-20260714-ATS-017-evidence-pack.md`
  - Updated to reflect the corrected implementation and verification.
- `deliverables/user/WI-20260714-ATS-017-summary.md`
  - Updated the Korean user-facing summary with the corrected claims.

### Behavioral pointers

- Readiness:
  - `Test-AcceptanceUrlReady` now returns success only for HTTP 200-399.
  - 403/404 no longer produce false-ready results.
- Abnormal startup cleanup:
  - `Start-AcceptanceEnvironment` sets `$completedSuccessfully = $false` before startup.
  - On any non-success path, `finally` calls `Invoke-AcceptanceServiceCleanup`.
  - Cleanup order is fixed as `tunnel`, `frontend`, `backend`.
- Test posture:
  - The suite does not try to throw `PipelineStoppedException` anymore because that can terminate the host before the test script can assert outcomes.
  - Instead, the suite uses a catchable startup exception to exercise the `finally` path and static source assertions to confirm the `completedSuccessfully` guard exists.

## Commands & Outputs

- `powershell -NoProfile -ExecutionPolicy Bypass -File .\scripts\acceptance\test-dry-run.ps1`
  - Passed.
  - Checks:
    - `parser`
    - `quick-tunnel-url-parser`
    - `public-base-url-validation`
    - `dry-run-contract`
    - `status-no-manifest`
    - `stop-no-manifest`
    - `readiness-http-status-contract`
    - `abnormal-start-cleanup-contract`
    - `start-finally-structure`
    - `secret-free-dry-run-output`
  - PSScriptAnalyzer status: `not-installed`
- `powershell -NoProfile -ExecutionPolicy Bypass -File .\scripts\acceptance\status.ps1 -RuntimeRoot "$env:TEMP\atstudio-acceptance-empty-check"`
  - Passed.
  - Output state: `not-started`
- `powershell -NoProfile -ExecutionPolicy Bypass -File .\scripts\acceptance\stop.ps1 -RuntimeRoot "$env:TEMP\atstudio-acceptance-empty-check"`
  - Passed.
  - Output state: `not-started`
- `powershell -NoProfile -ExecutionPolicy Bypass -File .\scripts\acceptance\start.ps1 -RuntimeRoot "$env:TEMP\atstudio-acceptance-dry-run-check" -DryRun`
  - Passed.
  - Output confirms `dryRun: true`, repo-external manifest path, and planned commands only.

## Tests

- Executed:
  - `test-dry-run.ps1`
  - `status.ps1` with no manifest
  - `stop.ps1` with no manifest
  - `start.ps1 -DryRun`
- Not executed:
  - Spring Boot
  - Vite
  - `cloudflared`
  - DB/Toss/SMTP/network tunnel
- Mocked contract coverage:
  - HTTP 200, 399 => ready
  - HTTP 403, 404 => not ready
  - thrown request exception => not ready
  - catchable startup failure => `finally` cleanup order verified
- Static contract coverage:
  - `$completedSuccessfully = $false`
  - `finally { if (-not $completedSuccessfully) { ... } }`
  - `Invoke-AcceptanceServiceCleanup -ServicesByName $servicesByName`

## Risks / Rollback

- Risks:
  - The code path intended to protect Ctrl+C / `PipelineStoppedException` is now implemented through `finally`, but the dry-run suite does not simulate host-level interruption directly.
  - Live tunnel/public endpoint behavior remains intentionally unverified in WI-017.
- Rollback:
  - Revert `scripts/acceptance/AcceptanceLifecycle.psm1`.
  - Revert `scripts/acceptance/test-dry-run.ps1`.
  - Revert this evidence pack and the paired user summary.
  - Leave unrelated application/payment/storage/image/auth files and existing untracked logs untouched.

## Follow-ups

- A later approved live-ops WI can verify real Ctrl+C/operator interruption behavior in a dedicated environment.
