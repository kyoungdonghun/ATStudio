---
version: 1.0
last_updated: 2026-07-24
project: ATS
owner: se
category: evidence-pack
status: confirmed
related_wi: WI-20260724-ATS-019
dependencies:
  - path: WI-20260724-ATS-019-handoff.md
    reason: WI execution contract
  - path: ../user/REQ-20260724-ATS-002.md
    reason: Approved release rehearsal scope
  - path: ../../docs/standards/core-principles.md
    reason: Tier 0 constitution
  - path: ../../docs/standards/development-standards.md
    reason: Tier 0 development and test rules
  - path: ../../docs/policies/security-policy.md
    reason: Secret and database safety rules
---

# Evidence Pack: WI-20260724-ATS-019

## Change Summary

- Added the active V1 disposable MySQL bootstrap path.
- Added connection-free guard verification and completed a real loopback
  disposable `Create -> Validate -> Drop` proof.
- Documented the exact current acceptance environment contract and linked both
  operator paths from SR-93.

## Scope / DoD Check

- [x] Bootstrap applies only current `schema.sql`, then current `seed.sql`.
- [x] `Create`, `Validate`, and `Drop` require one exact disposable name.
- [x] Loopback-only host validation occurs before credential access or
  connection.
- [x] Protected and malformed names are refused before connector discovery.
- [x] The current acceptance required and optional JSON keys are documented.
- [x] Obsolete payment environment keys are explicitly rejected.
- [x] Cloudflared, fixed ports, lifecycle commands, bundle ACL, and cleanup are
  documented.
- [x] Guard tests pass without MySQL.
- [x] A safe loopback disposable MySQL proof passed and removed its target.
- [x] Documentation validation passed.

## Reference Documents

| Tier | Document | Reason |
|---|---|---|
| 0 | `docs/standards/core-principles.md` | Execution, transparency, and security baseline |
| 0 | `docs/standards/development-standards.md` | Java, script, testing, and evidence rules |
| 1 | `docs/policies/security-policy.md` | Secret-safe external configuration and fresh-only V1 DB |
| 1 | `docs/policies/quality-gates.md` | Verification and rollback requirements |
| 2 | `docs/standards/evidence-pack-standard.md` | Evidence output contract |
| Context | `docs/SR/SR-42.md` | Historical tunnel topology and current safety addendum |
| Context | `docs/SR/SR-93.md` | Current V1 payment and database production gates |
| Contract | `deliverables/user/REQ-20260724-ATS-002.md` | Approved release rehearsal |
| Contract | `deliverables/agent/WI-20260724-ATS-013-handoff.md` | Disposable MySQL boundary |
| Contract | `deliverables/agent/WI-20260724-ATS-019-handoff.md` | Assigned scope and acceptance criteria |

Every input pointer in the WI-019 handoff was read before editing, including
all files under `scripts/acceptance/`, current `schema.sql`, current `seed.sql`,
and `application-acceptance.yml`. The historical
`DisposableMysqlDatabaseManager.java` was inspected only as a reuse candidate;
its retired manual migration list was not copied into the active path.

## Changed Files and Key Pointers

| File | Key section |
|---|---|
| `scripts/database/DisposableMysqlBootstrap.java:43-46,113-151,285-347` | fixed SQL paths, connection-before guards, and create/validate/drop boundaries |
| `scripts/database/bootstrap-disposable-mysql.ps1:35-149,153-226` | Connector/J discovery, repo-external bundle validation, unconditional preflight, and credential restoration |
| `scripts/database/test-bootstrap-guards.ps1:84-213` | protected/malformed/non-loopback refusal matrix and static source contract checks |
| `scripts/database/README.md:12-139` | safety contract and supported operator workflow |
| `scripts/acceptance/README.md:42-220` | topology, exact JSON allowlist, obsolete keys, ACL, lifecycle, cleanup |
| `docs/SR/SR-93.md:123,169` | active acceptance and disposable bootstrap pointers |
| `deliverables/user/WI-20260724-ATS-019-summary.md` | user-facing result and residual risks |
| `deliverables/agent/WI-20260724-ATS-019-evidence-pack.md` | this evidence pack |

## Safety Contract Evidence

### Database name

Accepted form:

```text
^ats_disposable_\d{8}_[a-z0-9]{8}$
```

Explicit protected-name refusals were exercised for:

```text
atstudio
mysql
information_schema
performance_schema
sys
preview
stage
staging
prod
production
```

Malformed names included short suffixes, uppercase suffixes, extra suffixes,
the historical `ats_wi007_*` form, and hyphenated forms.

### Host

Only these exact literals are accepted:

```text
localhost
127.0.0.1
::1
[::1]
```

Refusal cases exercised `db.example.com`, `0.0.0.0`, `127.0.0.2`, a
host-with-port string, and a JDBC-URL-shaped string.

### Connection ordering

The PowerShell entry point always executes Java `Preflight` before:

1. reading a credential bundle,
2. reading process credentials,
3. discovering Connector/J,
4. loading the JDBC driver, or
5. opening a connection.

The Java implementation repeats all name, host, port, workspace, and fixed
input checks before `Credentials.fromEnvironment()` or
`Class.forName("com.mysql.cj.jdbc.Driver")`.

### Mutation boundary

- `Create` checks exact-target absence, creates only that target, applies only
  `src/main/resources/schema.sql` then
  `src/main/resources/seed.sql`, and validates the V1 manifest.
- A failed create drops only the exact database that invocation created.
- `Validate` scopes all metadata queries to `DATABASE()` and checks exact
  counts plus the canonical column manifest hash.
- `Drop` executes one guarded `DROP DATABASE IF EXISTS` and verifies absence.
- No active file references `src/main/resources/db/manual`.
- No active helper executes `SHOW DATABASES`.

## Reproduction / Verification

### Guard suite

```powershell
powershell.exe -NoProfile -ExecutionPolicy Bypass `
  -File .\scripts\database\test-bootstrap-guards.ps1
```

Result:

```text
status=passed
checks=9
```

Checks:

- PowerShell parser
- valid preflight without MySQL
- protected-name refusal before connector
- malformed-name refusal before connector
- non-loopback refusal before connector
- exact target-name redaction
- fixed current SQL inputs
- retired migration absence
- unrelated database enumeration absence

### Disposable MySQL proof

Preconditions were checked without disclosing values:

```text
WI-013 restricted repo-external bundle=present
MySQL Connector/J=present
MySQL80 service=running
host class=loopback
```

The proof generated one unique conforming name in memory and did not record the
exact name:

```powershell
$database = "ats_disposable_20260724_" +
  ([guid]::NewGuid().ToString("N").Substring(0, 8))

.\scripts\database\bootstrap-disposable-mysql.ps1 `
  -Action Create `
  -DatabaseName $database `
  -HostName 127.0.0.1 `
  -BackendEnvironmentPath $bundle

.\scripts\database\bootstrap-disposable-mysql.ps1 `
  -Action Validate `
  -DatabaseName $database `
  -HostName 127.0.0.1 `
  -BackendEnvironmentPath $bundle

.\scripts\database\bootstrap-disposable-mysql.ps1 `
  -Action Drop `
  -DatabaseName $database `
  -HostName 127.0.0.1 `
  -BackendEnvironmentPath $bundle
```

Exact safe result:

| Step | Result |
|---|---|
| Preflight before create | PASS |
| `schema.sql` apply | PASS |
| `seed.sql` apply | PASS |
| Create-time manifest | PASS |
| Independent validate | PASS |
| Drop and absence check | PASS |
| Duplicate create | REFUSED |
| Existing target after duplicate refusal | Preserved and validation PASS |

Manifest evidence:

| Metric | Result |
|---|---:|
| Tables | 39 |
| Columns | 449 |
| Indexes | 153 |
| Foreign keys | 80 |
| Plans | 6 |
| SHA-256 | `c48d3c75378aaf2364d89ed06833ba68e27a5a334dbc4670d1443bd938c6c506` |

No credentials, JDBC URL, username, password, or exact disposable name appeared
in the captured result.

A second disposable run created one valid target, repeated `Create`, confirmed
that the duplicate request was refused, validated the original target
unchanged, and then dropped it. This proves the absence precondition does not
turn a repeated create into replacement or cleanup of a pre-existing target.

### Acceptance launcher regression

```powershell
powershell.exe -NoProfile -ExecutionPolicy Bypass `
  -File .\scripts\acceptance\test-dry-run.ps1

powershell.exe -NoProfile -ExecutionPolicy Bypass `
  -File .\scripts\acceptance\test-backend-environment.ps1
```

Results:

- Dry-run/lifecycle suite: PASS, 10 checks.
- External-bundle/isolation suite: PASS, 9 checks.
- README-to-module comparison: required 6/6 and optional 37/37 names matched
  exactly, with no missing or extra optional name.
- PSScriptAnalyzer: not installed; parser checks passed.

### Documentation

```powershell
python .agents/skills/validate-docs/scripts/validate_docs.py
```

Result:

- Tier 0 documents: PASS.
- Internal links: PASS, 0 broken.
- Traceability IDs: PASS, 468 matched.
- Document index: PASS, 0 orphaned.

## Results

**WI verdict: PASS.**

The active helper meets every WI-019 functional safety criterion and completed
the available real MySQL proof. WI-013 independently reports PASS for its
schema validation 1/1 and concurrency races 7/7. WI-019 used the restricted
WI-013 bundle only as a credential source for a separately named target; it did
not select or mutate the retained WI-013 runtime database.

## Risk / Rollback

Risks:

- Java 17, Connector/J, loopback MySQL 8, and create/drop permission remain
  operator prerequisites.
- ACL correctness and credential provenance are environment evidence. They
  cannot be guaranteed by committed documentation alone.
- The canonical manifest constants must be intentionally updated with a future
  approved V1 replacement baseline; a mismatch fails closed.
- This tool is fresh-only and cannot migrate retained data.

Rollback:

1. Stop any process that is using the exact disposable database.
2. Use the guarded `Drop` action if that exact database still exists.
3. Remove the four files under `scripts/database/`.
4. Remove `scripts/acceptance/README.md`.
5. Revert the two SR-93 link bullets.
6. Remove the two WI-019 deliverables.

Rollback does not require and must not perform any mutation of `atstudio` or an
unrelated database.

## Follow-ups

- WI-20260724-ATS-020 should independently review this helper, rerun the guard
  and documentation gates, and repeat the disposable proof if its approved
  environment remains available.
- WI-013 remains the owner of its passed eight MySQL concurrency/schema tests
  and retained runtime database bundle.
