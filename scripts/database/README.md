# Disposable MySQL Bootstrap

## Purpose

This directory is the supported V1 operator path for proving a fresh ATStudio
database without touching the protected `atstudio` database. The tool creates,
validates, or drops one explicitly named disposable database on loopback MySQL.

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

The V1 validation contract is:

| Metric | Expected |
|---|---:|
| Tables | 39 |
| Columns | 449 |
| Indexes | 153 |
| Foreign keys | 80 |
| Seeded subscription plans | 6 |
| Manifest SHA-256 | `c48d3c75378aaf2364d89ed06833ba68e27a5a334dbc4670d1443bd938c6c506` |

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

## Operator Workflow

Generate one exact disposable name. Do not reuse it:

```powershell
$database = "ats_disposable_20260724_a1b2c3d4"
```

1. Verify guards and current SQL inputs without loading credentials:

```powershell
.\scripts\database\bootstrap-disposable-mysql.ps1 `
  -Action Preflight `
  -DatabaseName $database `
  -HostName 127.0.0.1
```

2. Create the empty database, apply `schema.sql` then `seed.sql`, and validate
   the exact V1 manifest:

```powershell
.\scripts\database\bootstrap-disposable-mysql.ps1 `
  -Action Create `
  -DatabaseName $database `
  -HostName 127.0.0.1 `
  -BackendEnvironmentPath $bundle
```

3. Re-run the manifest check at any time:

```powershell
.\scripts\database\bootstrap-disposable-mysql.ps1 `
  -Action Validate `
  -DatabaseName $database `
  -HostName 127.0.0.1 `
  -BackendEnvironmentPath $bundle
```

4. Point an isolated backend process at the disposable database and require
   `SPRING_JPA_HIBERNATE_DDL_AUTO=validate`. The application must never be
   allowed to update the schema during this proof.

5. Drop the exact disposable database when the rehearsal ends:

```powershell
.\scripts\database\bootstrap-disposable-mysql.ps1 `
  -Action Drop `
  -DatabaseName $database `
  -HostName 127.0.0.1 `
  -BackendEnvironmentPath $bundle
```

If a valid proof cannot run because credentials or Connector/J are unavailable,
record it as an environment-conditional block. Do not weaken a guard, use the
protected database, or add a retired migration to make the proof pass.

## Retained Data

V1 is fresh-only. Existing data requires a separately approved migration,
backup, rehearsal, and rollback plan. Files under historical WI evidence are
not active bootstrap tools and must not be copied into this execution path.
