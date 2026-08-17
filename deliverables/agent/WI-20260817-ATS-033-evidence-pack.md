# Evidence Pack: WI-20260817-ATS-033

## Summary

WI-033 corrects the acceptance lifecycle status false positive recorded in the
handoff: a retained `ready` manifest could be reported healthy after its
frontend/backend had disappeared and a stored backend PID identified an
unrelated Chrome process. The change is limited to acceptance status
classification and its regression coverage.

## Scope / DoD Check

- [x] A `ready` manifest without owned frontend/backend services returns
  `stale`, not `ready`.
- [x] PID reuse fails ownership matching through start time, executable path,
  and command fingerprint before it can support a healthy verdict.
- [x] Owned local runtime evidence and public readiness evidence are separate;
  a temporarily unavailable public tunnel yields `degraded`, not `stale`.
- [x] Lifecycle regression coverage and the existing backend-environment test
  passed in fresh PowerShell invocations.
- [x] No live runtime or forbidden external/resource scope was accessed.
- [x] Final `git diff --check` passed after this evidence pack was created.

## Reference Documents

| Tier | Document | Use |
|---|---|---|
| 0 | `docs/standards/core-principles.md` | Approved-WI boundary and transparent evidence |
| 0 | `docs/standards/development-standards.md` | Two-set deliverables and regression evidence |
| 0 | `docs/standards/documentation-standards.md` | Deliverable structure |
| 0 | `docs/standards/glossary.md` | Canonical Work Item terminology |
| 1 | `docs/policies/quality-gates.md` | Relevant regression and DoD checks |
| 1 | `docs/policies/security-policy.md` | Secret-safe and non-interference boundary |
| REQ | `deliverables/user/REQ-20260817-ATS-010.md` | Approved non-destructive boundary |
| Context | `deliverables/user/WI-20260817-ATS-021-summary.md` | Client runtime was not started after preflight blocker |
| Context | `deliverables/user/WI-20260817-ATS-022-summary.md` | Existing child-environment regression coverage |
| Context | `deliverables/user/WI-20260817-ATS-032-summary.md` | Isolated rehearsal boundary and remaining approval gates |
| Handoff | `deliverables/agent/WI-20260817-ATS-033-handoff.md` | WI scope, observed symptom, and output contract |

## Evidence Pointers

- `scripts/acceptance/AcceptanceLifecycle.psm1`, `Get-AcceptanceStatus`:
  ownership-gated status classification and separate health observations.
- `scripts/acceptance/AcceptanceLifecycle.psm1`,
  `Test-AcceptanceProcessOwnership`: safe process identity checks against the
  recorded start time, executable path, and command fingerprint.
- `scripts/acceptance/test-dry-run.ps1`,
  `$statusLifecycleContract`: synthetic stale-manifest, PID-reuse,
  local-owned/public-unavailable, and fully-ready regression cases.
- `scripts/acceptance/status.ps1`: command entry point that serializes the
  status object without creating or changing a manifest.

## Patch Notes

- Service records are returned for all expected roles with `present` and
  `owned` fields rather than omitting missing records.
- `state: ready` now requires owned frontend, backend, and tunnel records plus
  successful local and public readiness observations.
- `state: stale` is reserved for a retained `ready` manifest whose frontend or
  backend cannot be verified as owned.
- `state: degraded` preserves the active owned-runtime distinction when local
  or public readiness is incomplete.

## Reproduction and Test Results

| Command | Result |
|---|---|
| `& .\scripts\acceptance\test-dry-run.ps1` | PASS. Parser, dry-run, status-no-manifest, lifecycle status classification, readiness HTTP contract, cleanup contract, and secret-free output checks passed. PSScriptAnalyzer was not installed. |
| `& .\scripts\acceptance\test-backend-environment.ps1` | PASS in a fresh PowerShell invocation. Bundle validation, child-process isolation, log drainage, launch order, and fixture cleanup checks passed. |
| `git diff --check` | PASS. No output and successful exit after all WI-033 files were present. |

The first backend-environment invocation during this WI ended with a null-valued
expression at its synthetic child-observation block. An immediate fresh
invocation and three later invocations all emitted `status: passed`. A local
repeat harness did not retain native exit codes after piping output through
`Out-String`, so those three output-only observations are not used as the final
gate; the fresh direct PASS above is the recorded test result. This WI does not
alter that test's fixture implementation because it is outside the status
false-positive correction.

## Runtime Non-Interference

- No status command was run against a real runtime root.
- Synthetic tests used only temporary paths and mocked readiness responses.
- No request was sent to live ports `5173` or `8080`, no current process was
  started/stopped/inspected for ownership, and no client worktree/runtime root
  was accessed.
- No database, Cloudflare, SMTP, payment/provider, backup, secret, or external
  configuration action occurred.

## Rollback

Revert only these WI-033 files:

- `scripts/acceptance/AcceptanceLifecycle.psm1`
- `scripts/acceptance/test-dry-run.ps1`
- `deliverables/user/WI-20260817-ATS-033-summary.md`
- `deliverables/agent/WI-20260817-ATS-033-evidence-pack.md`

No runtime rollback is needed because WI-033 did not mutate a runtime.

## Follow-up WI and Approval Gates

- WI-023 remains blocked pending its separate explicit external-effect action
  approval.
- WI-024 remains blocked pending its separate exact database scope approval.

## Changed Files

- `scripts/acceptance/AcceptanceLifecycle.psm1`
- `scripts/acceptance/test-dry-run.ps1`
- `deliverables/user/WI-20260817-ATS-033-summary.md`
- `deliverables/agent/WI-20260817-ATS-033-evidence-pack.md`
