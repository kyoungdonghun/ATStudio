# WI-20260724-ATS-002 Summary

## Status

- **Result:** Complete
- **Blocker:** None
- **Scope:** Demo seed CLI, PowerShell wrapper, focused contract test, and CLI
  documentation
- **Product behavior:** No backend, frontend, database, payment, subscription,
  media, or demo-data behavior changed

## Completed Work

- Removed the retired user-specific acceptance credentials default from the
  direct Node CLI.
- Made credentials mandatory and explicit for live `Seed`, `Verify`, and
  destructive `Cleanup` operations.
- Kept `Seed -DryRun` and `Cleanup -DryRun` usable without credentials, API
  calls, or data mutation.
- Rejected `Verify -DryRun` consistently in the wrapper and direct CLI because
  `Verify` is already non-destructive and still requires authenticated API
  access.
- Added a focused contract test covering direct and wrapper execution,
  fail-closed behavior, explicit missing runtime input forwarding, secret-safe
  output, and temporary fixture cleanup.
- Added a short operator README that documents the same credential and dry-run
  contract.

## Files Changed

- `scripts/demo/seed-client-demo.mjs`
- `scripts/demo/seed-client-demo.ps1`
- `scripts/demo/test-seed-client-demo.ps1`
- `scripts/demo/README.md`
- `deliverables/user/WI-20260724-ATS-002-summary.md`
- `deliverables/agent/WI-20260724-ATS-002-evidence-pack.md`

## Verification

- Focused demo seed contract test: PASS, 14 checks.
- Direct Node syntax check: PASS.
- PowerShell parser check for wrapper and test: PASS.
- Prettier check for JavaScript and Markdown: PASS.
- Retired runtime and user-specific source-path scan: 0 matches.
- Owned-file whitespace check: PASS.

No credentials file was read. No live seed, verify, cleanup, API, database, or
storage operation was executed.

## Follow-up

This WI unblocks `WI-20260724-ATS-004` and `WI-20260724-ATS-006` according to
the approved REQ work plan.
