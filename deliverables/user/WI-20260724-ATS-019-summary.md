---
version: 1.0
last_updated: 2026-07-24
project: ATS
owner: se
category: work-summary
status: confirmed
dependencies:
  - path: REQ-20260724-ATS-002.md
    reason: Approved release rehearsal scope
  - path: ../agent/WI-20260724-ATS-019-handoff.md
    reason: WI execution contract
  - path: ../agent/WI-20260724-ATS-019-evidence-pack.md
    reason: Reproducible implementation and verification evidence
---

# WI-20260724-ATS-019 Summary

## Verdict

**PASS** - ATStudio now has a guarded, active V1 disposable MySQL bootstrap
path and a current acceptance-environment operator guide.

## What Changed

- Added a supported `Preflight`, `Create`, `Validate`, and `Drop` workflow under
  `scripts/database/`.
- Fixed the executable SQL input contract to current `schema.sql` followed by
  current `seed.sql`; retired manual migrations cannot be selected.
- Added defense-in-depth guards in both the PowerShell entry point and Java
  implementation.
- Added MySQL-free tests for protected names, malformed names, non-loopback
  hosts, output redaction, fixed SQL inputs, and unrelated-database
  enumeration.
- Documented Cloudflare, ports, the current backend JSON allowlist,
  secret-file ACL, lifecycle commands, and cleanup in
  `scripts/acceptance/README.md`.
- Linked the active database and acceptance operator paths from SR-93.

## Operator Workflow

1. Run `scripts/database/test-bootstrap-guards.ps1`.
2. Generate one name matching
   `^ats_disposable_\d{8}_[a-z0-9]{8}$`.
3. Run `Preflight` without credentials or MySQL access.
4. Run `Create` against loopback MySQL with a repo-external credential bundle.
5. Start the isolated backend with `ddl-auto=validate`.
6. Run `Validate` when a manifest recheck is required.
7. Run `Drop` for that exact disposable target after the rehearsal.

The acceptance runtime is operated separately with
`scripts/acceptance/start.ps1`, `status.ps1`, and `stop.ps1` as documented in
the new README.

## Safety Decisions

- Only exact loopback host literals are accepted.
- Protected and malformed database names fail before connector discovery,
  credential loading, or connection.
- The helper never enumerates or drops unrelated databases.
- Credential values are read from process environment or an explicitly
  supplied repo-external bundle and are never command-line arguments or output.
- A failed create attempts cleanup only for the exact database created by that
  invocation.
- Retained-data migration remains outside V1.

## Verification

| Gate | Result |
|---|---|
| Guard suite | PASS |
| Disposable MySQL `Create` | PASS |
| Independent `Validate` | PASS |
| Exact `Drop` | PASS |
| WI-013 MySQL schema/concurrency dependency | PASS, 8/8 |
| V1 manifest | 39 tables, 449 columns, 153 indexes, 80 foreign keys, 6 plans |
| V1 manifest SHA-256 | `c48d3c75378aaf2364d89ed06833ba68e27a5a334dbc4670d1443bd938c6c506` |
| Acceptance dry-run tests | PASS |
| Acceptance bundle tests | PASS |
| Documentation validation | PASS |

## Residual Risks

- WI-013 separately owns and passed the eight MySQL concurrency/schema tests.
  This WI consumed its restricted bundle only as a credential source for a new
  disposable target and did not select or mutate the retained WI-013 runtime
  database.
- The bootstrap requires Java 17, MySQL Connector/J, loopback MySQL 8, and an
  account with create/drop permission for the disposable target.
- Secret-file ACL remains an operator precondition. The guide documents the
  Windows procedure, but an environment-specific ACL cannot be committed.
- Cloudflare access, Toss mutation, mail delivery, and full runtime acceptance
  remain assigned to later rehearsal WIs.

## Outputs

- `scripts/database/DisposableMysqlBootstrap.java`
- `scripts/database/bootstrap-disposable-mysql.ps1`
- `scripts/database/test-bootstrap-guards.ps1`
- `scripts/database/README.md`
- `scripts/acceptance/README.md`
- `docs/SR/SR-93.md`
- `deliverables/agent/WI-20260724-ATS-019-evidence-pack.md`
