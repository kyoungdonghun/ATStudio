# Evidence Pack: WI-20260724-ATS-002

## Summary (one-liner)

- Removed the stale machine-specific demo seed credential default and aligned
  the direct Node and PowerShell wrapper contracts without executing live demo
  operations.

## Scope / DoD Check

- [x] No user-specific or retired runtime path remains in active demo seed
      source.
- [x] Direct non-dry-run `seed`, `verify`, and `cleanup` fail closed without an
      explicit credentials path.
- [x] PowerShell non-dry-run execution fails closed without
      `RuntimeCredentialsPath`.
- [x] `Seed -DryRun` and `Cleanup -DryRun` remain secret-free and usable.
- [x] Direct and wrapper `Verify -DryRun` reject the misleading combination
      consistently.
- [x] Explicit missing external input is forwarded by both entry points instead
      of falling back to a hidden location.
- [x] Focused checks and owned-file whitespace validation pass.
- [x] No credentials content was read and no destructive operation ran.

## Reference Documents (Tier 0-2)

**Injected Context** (from WI Handoff Packet):

| Tier | Document                                            | Reason                                                  |
| ---- | --------------------------------------------------- | ------------------------------------------------------- |
| 0    | `docs/standards/core-principles.md`                 | Constitution and approved execution boundary            |
| 0    | `docs/standards/development-standards.md`           | `se` implementation and test standards                  |
| 1    | `docs/policies/security-policy.md`                  | Secret, external configuration, and logging constraints |
| REQ  | `deliverables/user/REQ-20260724-ATS-001.md`         | Approved scope and success criteria                     |
| WI   | `deliverables/agent/WI-20260724-ATS-002-handoff.md` | Mandatory task and output contract                      |

**Injection Rules Applied:**

- Assignee: `se`
- Task type: implementation and focused verification
- Owned slice: demo seed CLI/default credentials path only

## Evidence Pointers

### Files changed

- `scripts/demo/seed-client-demo.mjs`
  - `parseArgs`: credential default is `null`.
  - CLI validation: non-dry-run operations require `--credentials`.
  - CLI validation: `verify --dry-run` is rejected.
  - `main`: cleanup dry-run loads only the local manifest and returns before
    credentials, login, API, or mutation paths.
- `scripts/demo/seed-client-demo.ps1`
  - Wrapper rejects `Verify -DryRun`.
  - Wrapper requires `RuntimeCredentialsPath` for every non-dry-run mode.
- `scripts/demo/test-seed-client-demo.ps1`
  - Temporary, process-level contract checks for direct and wrapper entry
    points.
  - Static source scan for retired runtime and user-specific paths.
  - Output scan for secret-bearing labels and retired runtime references.
- `scripts/demo/README.md`
  - Operator-facing credential, dry-run, and focused-test contract.
- `deliverables/user/WI-20260724-ATS-002-summary.md`
- `deliverables/agent/WI-20260724-ATS-002-evidence-pack.md`

### Preserved behavior

- Live `seed` still follows tag, track, playlist, manifest, and final verify
  paths after explicit credential loading.
- Live `verify` still performs authenticated read-only verification after
  explicit credential loading.
- Live `cleanup` still performs manifest-scoped deletion only after explicit
  credential loading and login.
- Seed dry-run still reports the planned 36 tags, 36 tracks, and 9 playlists.
- Cleanup dry-run still reports manifest-scoped target counts and now does so
  without the accidental hidden credential dependency.

## CLI Cases Tested

| Entry point        | Case                                                 | Expected result                               | Result |
| ------------------ | ---------------------------------------------------- | --------------------------------------------- | ------ |
| Direct Node        | `seed --dry-run` without credentials                 | Plan only, exit 0                             | PASS   |
| Direct Node        | `cleanup --dry-run` without credentials              | Preview only, exit 0                          | PASS   |
| Direct Node        | live `seed`, `verify`, `cleanup` without credentials | Fail closed before I/O                        | PASS   |
| Direct Node        | `verify --dry-run`                                   | Reject ambiguous combination                  | PASS   |
| Direct Node        | explicit nonexistent external input                  | Use input and fail with `ENOENT`; no fallback | PASS   |
| PowerShell wrapper | `Seed -DryRun` without credentials                   | Plan only, exit 0                             | PASS   |
| PowerShell wrapper | `Cleanup -DryRun` without credentials                | Preview only, exit 0                          | PASS   |
| PowerShell wrapper | live `Seed` without credentials                      | Fail closed before Node                       | PASS   |
| PowerShell wrapper | `Verify -DryRun`                                     | Reject ambiguous combination                  | PASS   |
| PowerShell wrapper | explicit nonexistent external input                  | Forward input; no fallback                    | PASS   |

## Output Redaction Behavior

- Dry-run output contains plan/preview metadata only.
- Expected error output contains no password, secret, or retired acceptance
  runtime reference.
- The test does not create or read any credential fixture.
- The test uses one nonexistent synthetic external path solely to prove
  explicit argument forwarding.

## Commands & Outputs

- `powershell -NoProfile -ExecutionPolicy Bypass -File scripts/demo/test-seed-client-demo.ps1`
  - PASS: 14 named checks.
- `node --check scripts/demo/seed-client-demo.mjs`
  - PASS.
- PowerShell AST parse for `scripts/demo/seed-client-demo.ps1` and
  `scripts/demo/test-seed-client-demo.ps1`
  - PASS: zero parser errors.
- `frontend\node_modules\.bin\prettier.cmd --check scripts/demo/seed-client-demo.mjs scripts/demo/README.md`
  - PASS: all matched files use Prettier style.
- Stale path scan over `scripts/demo`
  - PASS: zero matches for the retired runtime identifier and user-specific
    absolute path.
- `git diff --check` for WI-owned paths, including no-index checks for new files
  - PASS.

## Validation Incidents

- The first focused-test run stopped because Windows PowerShell promoted
  expected native stderr to a terminating `NativeCommandError`. The test harness
  was changed to capture native stdout/stderr through `.NET Process`.
- The next run showed that wrapper exception formatting includes the current
  script path. Source portability and output secrecy were split into separate
  checks: source rejects user-specific paths; output rejects secret-bearing
  labels and retired runtime references.
- Both incidents were test-harness defects. No live seed, cleanup, credential,
  API, database, or storage operation occurred.

## Risks / Rollback

- Risks:
  - Operators that relied on the accidental hidden Node default must now pass
    the external credentials path explicitly.
  - `Verify -DryRun` now fails clearly instead of silently acting as live
    verification through the hidden default.
- Rollback:
  - Revert only the six WI-owned paths listed above.
  - Do not restore the retired personal default. If compatibility is required,
    introduce a separately approved runtime-root discovery contract rather than
    embedding a machine path.

## Follow-ups

- `WI-20260724-ATS-004`: backend and acceptance verification.
- `WI-20260724-ATS-006`: documentation consistency validation.
