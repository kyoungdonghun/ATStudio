# Disposable MySQL Bootstrap

## Purpose

This directory is the supported V1 operator path for preflighting and, only
after separate approval, proving a fresh ATStudio database without touching the
protected `atstudio` database. The tool preflights, observes, creates, validates,
or drops one explicitly named disposable database on loopback MySQL.

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
  statement count, and refuses unless the source count is exactly 42. This
  source-level check needs no credentials, connector, or database connection.
- The current MySQL manifest expectation is `RECORDED` from the approved
  DG-067-09B observation. `Create` and `Validate` compare the exact target
  against that recorded manifest.
- `Observe` is only a first-pass action while a manifest is unrecorded. It is
  now refused before credentials with
  `MYSQL_MANIFEST_OBSERVATION_NOT_REQUIRED`.
- `Create` applies only
  `src/main/resources/schema.sql` and then
  `src/main/resources/seed.sql`.
- `Create` requires the exact disposable database to be absent.
- A failed create removes only the exact database created by that invocation.
- `Validate` queries only the exact target and its scoped
  `information_schema` rows.
- `Drop` issues `DROP DATABASE IF EXISTS` only for the exact guarded target.
- The helper never runs `SHOW DATABASES`, scans unrelated databases, or prints
  a username, password, JDBC URL, or exact target name.
- Credentials are inherited through process environment variables or read from
  an explicitly supplied repo-external acceptance JSON bundle. They are never
  passed as command-line arguments.

## Current Baseline State

Current `schema.sql` contains 42 derived `CREATE TABLE` statements, and
`Preflight` enforces that source-level fact. The separately observed and proven
current MySQL manifest is:

| Field | Recorded value |
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
identify source inputs only; they are not substitutes for this MySQL manifest.
The predecessor 41-table manifest remains historical evidence and is not an
active expectation.

## Prerequisites

- Java 17 or later on `PATH`
- Loopback MySQL 8
- A MySQL account allowed to create and drop the one disposable database
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
`source.schema.createTableStatements=42`,
`source.schema.createTableStatementsCheck=PASS`, and
`mysql.manifest.expectation=RECORDED`.

## Recorded WI-067 Two-Pass Rehearsal

DG-067-09B was separately approved and completed on 2026-08-13. The one-use
approval covered only `ats_disposable_20260813_wi067obs` and
`ats_disposable_20260813_wi067prf` on loopback MySQL. It is exhausted and does
not authorize reusing either name or running a new disposable proof.

1. Generate a unique first-pass observation name and run `Preflight`.

2. `Observe` created the exact absent observation database, applied
   `schema.sql` then `seed.sql`, emitted the current manifest above, failed
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

3. The approved tooling update recorded only the emitted values, retained the
   plan-key equality and forbidden-object zero guards, passed all 20 guard
   checks, and produced a `RECORDED` preflight result.

4. The distinct proof database passed `Create` and independent `Validate`; both
   matched the recorded manifest exactly.

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

5. `AdminPaymentSettlementMysqlConcurrencyIntegrationTest` ran against only the
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

## Future Reproduction

The commands above document the completed WI-067 evidence. Any future run must
obtain a new immediate destructive/test approval, generate new exact disposable
names, confirm loopback scope, and preserve the same exact-target cleanup and
secret-safe credential handling. `Observe` is not applicable while the current
manifest remains recorded; use guarded `Create`, independent `Validate`, an
explicitly approved isolated test, and exact `Drop`.

If a valid proof cannot run because credentials or Connector/J are unavailable,
record it as an environment-conditional block. Do not weaken a guard, use the
protected database, or add a retired migration to make the proof pass.

## Retained Data

V1 is fresh-only. Existing data requires a separately approved migration,
backup, rehearsal, and rollback plan. Files under historical WI evidence are
not active bootstrap tools and must not be copied into this execution path.
