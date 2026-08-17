# Disposable MySQL Bootstrap

## Purpose

This directory is the guarded V1 operator utility for preflighting a fresh
ATStudio source snapshot and, only after separate approval and a recorded
current manifest, proving one fresh database without touching the protected
`atstudio` database. The tool can preflight, observe, create, validate, drop,
or inventory possible orphan disposable schemas on loopback MySQL. Every
action requires one explicitly named disposable companion database; Inventory
uses that name only for its preconnection guard and never as a query target. The current
43-table source snapshot has a recorded live/disposable MySQL manifest. Guarded
`Create`, independent `Validate`, and explicit Hibernate validation are the
only runnable current proof actions.

It is not a retained-data migration tool and must not be used against stage,
production, or any remote host.

## Safety Contract

The supported entry point is
`scripts/database/bootstrap-disposable-mysql.ps1`.

- The host must be exactly `localhost`, `127.0.0.1`, `::1`, or `[::1]`.
- The database name must match
  `^ats_disposable_\d{8}_[a-z0-9]{8}$`.
- Protected names such as `atstudio`, MySQL system schemas, preview, stage, and
  production names are refused before credentials are loaded or a connection
  is attempted.
- Every action first parses current `schema.sql`, derives its `CREATE TABLE`
  statement count, and refuses unless the source count is exactly 43. This
  source-level check needs no credentials, connector, or database connection.
- The current live/disposable MySQL manifest expectation is `RECORDED` from the
  approved 43-table observation. `Create` and `Validate` compare every bounded
  manifest field against that current expectation; they cannot use the
  historical WI-067 manifest as a current expectation.
- `Inventory` keeps the same action, loopback, companion-name, and 43-table
  preconnection guards, but is not a manifest operation.
- `Inventory` opens only the root/admin connection, runs one fixed
  `information_schema.schemata` `COUNT(*)` query constrained to
  `^ats_disposable_[0-9]{8}_[a-z0-9]{8}$`, and never selects a target database
  or schema name. Its additional successful output is numeric `inventory.count`
  and `inventory.state`, which is `NO_POSSIBLE_ORPHAN` for zero or
  `POSSIBLE_ORPHAN_EXISTS` for a positive count.
- `Observe` is refused after a manifest is recorded. Nothing in this guide
  authorizes a new observation.
- `Create` applies only
  `src/main/resources/schema.sql` and then
  `src/main/resources/seed.sql`.
- `Create` requires the exact disposable database to be absent.
- A failed create removes only the exact database created by that invocation.
- `Validate` queries only the exact target and its scoped
  `information_schema` rows.
- `Drop` issues `DROP DATABASE IF EXISTS` only for the exact guarded target.
- `HibernateValidate` is explicit opt-in and wrapper-managed. It constructs the
  datasource only from the guarded loopback host, port, and disposable name,
  then runs the targeted `ddl-auto=validate` proof. It does not admit retained,
  remote, stage, or production targets.
- The helper never runs `SHOW DATABASES`, lists schema names, or prints a
  username, password, JDBC URL, connection value, or exact target name.
- Credentials are inherited through process environment variables or read from
  an explicitly supplied repo-external acceptance JSON bundle. They are never
  passed as command-line arguments.

## Current Source And Manifest State

Current `schema.sql` contains 43 derived `CREATE TABLE` statements, and the
current entity source contains 43 JPA entities. `Preflight` enforces the
43-table source-level fact without loading credentials or connecting to MySQL.
The live/disposable MySQL manifest is `RECORDED` from a guarded observation:
43 tables, 511 columns, 175 index rows, 91 foreign keys, 6 plans, 6 plan keys,
zero forbidden tables, zero forbidden columns, and SHA-256
`b177b34780fabc75ea8b4608a0d210167a81d414d2778cc1d1dc5c0e39c8fea4`.
Guarded `Create`, independent `Validate`, and the explicit wrapper-managed
Hibernate proof compare or bind only this current disposable contract.

## Historical WI-067 Evidence (Superseded 42-Table Source Snapshot)

The following values are retained only as WI-067 historical evidence for the
superseded 42-table source snapshot. They are not a current manifest and must
not be used to run current `Create` or `Validate` actions.

| Field | Historical WI-067 value |
|---|---:|
| Tables | 42 |
| Columns | 506 |
| Index rows | 173 |
| Foreign keys | 90 |
| Plans | 6 |
| Plan keys | 6 |
| Forbidden tables / columns | 0 / 0 |
| SHA-256 | `acf28c935bf6107a8f2af431c971ebe0cd3539dba1aa1a941d966dde4a2a7a65` |

The normalized `schema.sql` and `seed.sql` text hashes printed by preflight
identify source inputs only; they are not substitutes for a MySQL manifest.
The predecessor 41-table manifest also remains historical evidence and is not
an active expectation.

## Prerequisites

- Java 17 or later on `PATH`
- Loopback MySQL 8
- A MySQL account with only the permissions required for the approved action;
  Inventory needs metadata access to `information_schema.schemata`, while the
  existing create/drop actions need their current exact disposable permissions
- MySQL Connector/J, either already downloaded by a backend Gradle build or
  supplied with `-ConnectorJarPath`
- Current `schema.sql` and `seed.sql` in the clone

Run the guard suite before using MySQL:

```powershell
.\scripts\database\test-bootstrap-guards.ps1
```

## Credential Sources

The preferred source is the same repo-external JSON bundle used by the
acceptance launcher:

```powershell
$bundle = "$env:LOCALAPPDATA\ATStudio\acceptance-backend-environment.json"
```

The bootstrap reads only these properties from that bundle:

- `SPRING_DATASOURCE_URL`
- `SPRING_DATASOURCE_USERNAME`
- `SPRING_DATASOURCE_PASSWORD`

The JDBC URL must identify one loopback MySQL host and the selected port. Its
database path is a credential-source hint only; the bootstrap never connects
to or mutates that path. The explicit guarded `-DatabaseName` remains the sole
target.

When no bundle is supplied, set only the username and password in the current
process:

```powershell
$env:SPRING_DATASOURCE_USERNAME = "<local MySQL operator>"
$env:SPRING_DATASOURCE_PASSWORD = "<local MySQL password>"
```

Do not echo these values or store them in the repository. Clear process values
after the proof if they were set manually.

## Non-Database Preflight

Generate one exact disposable name. Do not reuse it:

```powershell
$database = 'ats_disposable_' + (Get-Date -Format 'yyyyMMdd') + '_' + ([guid]::NewGuid().ToString('N').Substring(0, 8))
```

Verify guards and current SQL inputs without loading credentials:

```powershell
.\scripts\database\bootstrap-disposable-mysql.ps1 `
  -Action Preflight `
  -DatabaseName $database `
  -HostName 127.0.0.1
```

The successful output must include
`source.schema.createTableStatements=43`,
`source.schema.createTableStatementsCheck=PASS`, and
`mysql.manifest.expectation=RECORDED`.

## Read-Only Inventory

`Inventory` is a separately approved read-only operation. After its identical
preconnection guard, it uses the supplied disposable name only as a companion
syntax check, queries neither that name nor the protected `atstudio` database,
and reports only the possible-orphan count and its bounded state. A positive
count does not authorize cleanup.

## Historical WI-067 Two-Pass Rehearsal (Superseded 42-Table Snapshot)

DG-067-09B was separately approved and completed on 2026-08-13 against the
then-current 42-table source snapshot. The one-use approval covered only
`ats_disposable_20260813_wi067obs` and
`ats_disposable_20260813_wi067prf` on loopback MySQL. It is exhausted and does
not authorize reusing either name, running a new disposable proof, or treating
the historical command transcript below as a current procedure.

1. Generate a unique first-pass observation name and run `Preflight`.

2. `Observe` created the exact absent observation database, applied
   `schema.sql` then `seed.sql`, emitted the then-current historical manifest
   above, failed
   closed as expected with `MYSQL_MANIFEST_EXPECTATION_UNRECORDED`, and reported
   `cleanupAfterFailure=PASS`. A follow-up exact `Drop` also reported `PASS`.

```powershell
.\scripts\database\bootstrap-disposable-mysql.ps1 `
  -Action Observe `
  -DatabaseName $observationDatabase `
  -HostName 127.0.0.1 `
  -BackendEnvironmentPath $bundle
```

The bounded observation output was limited to `manifest.tables`,
`manifest.columns`, `manifest.indexes`, `manifest.foreignKeys`,
`manifest.plans`, `manifest.planKeys`, `manifest.forbiddenTables`,
`manifest.forbiddenColumns`, and `manifest.sha256`; no secret was printed.

3. The approved tooling update recorded only the emitted WI-067 values,
   retained the plan-key equality and forbidden-object zero guards, passed all
   20 guard checks, and produced a `RECORDED` preflight result for that
   historical source snapshot.

4. For that historical source snapshot, the distinct proof database passed
   `Create` and independent `Validate`; both matched the recorded manifest
   exactly. The following is a historical command transcript, not a runnable
   current `Create`/`Validate` procedure.

```powershell
.\scripts\database\bootstrap-disposable-mysql.ps1 `
  -Action Create `
  -DatabaseName $proofDatabase `
  -HostName 127.0.0.1 `
  -BackendEnvironmentPath $bundle

.\scripts\database\bootstrap-disposable-mysql.ps1 `
  -Action Validate `
  -DatabaseName $proofDatabase `
  -HostName 127.0.0.1 `
  -BackendEnvironmentPath $bundle
```

5. During that historical proof,
   `AdminPaymentSettlementMysqlConcurrencyIntegrationTest` ran against only the
   proof database with Hibernate `ddl-auto=validate`: 3 tests passed with zero
   failures, errors, or skips. The cases covered different operation keys with
   one deduplication key, the same owner and operation key, and concurrent
   `IGNORE`.

6. Exact-target `Drop` for the proof database reported `PASS`.

```powershell
.\scripts\database\bootstrap-disposable-mysql.ps1 `
  -Action Drop `
  -DatabaseName $proofDatabase `
  -HostName 127.0.0.1 `
  -BackendEnvironmentPath $bundle
```

The verified residual state is no database with either exact disposable name.
The proof did not access an existing database or invoke Provider, payment,
refund, or mail behavior.

## Future Evidence Procedure

The commands above preserve completed WI-067 evidence only. They are not the
current 43-table proof procedure. The current source now reports its recorded
manifest expectation and may use only a separately approved fresh disposable
run with guarded `Create`, independent `Validate`, the wrapper-managed
Hibernate proof, exact `Drop`, and a final read-only `Inventory`.

If a valid proof cannot run because credentials or Connector/J are unavailable,
record it as an environment-conditional block. Do not weaken a guard, use the
protected database, or add a retired migration to make the proof pass.

## Retained Data

V1 is fresh-only. Existing data requires a separately approved migration,
backup, rehearsal, and rollback plan. Files under historical WI evidence are
not active bootstrap tools and must not be copied into this execution path.
