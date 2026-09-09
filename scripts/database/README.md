---
version: 1.1
last_updated: 2026-09-09
project: ATS
owner: DocOps
category: guide
status: active
dependencies:
  - path: ../../docs/design/db-schema.md
    reason: Current DDL and historical manifest boundary
  - path: ../../docs/design/runtime-storage-operations.md
    reason: Approved retained-data rollout and environment tuple
  - path: manual-wi023-audio-processing.sql
    reason: Apply-once retained audio migration, applied to TEST under REQ006
  - path: ../../deliverables/agent/WI-20260909-ATS-028-evidence-pack.md
    reason: Retained-runtime verification distinct from fresh-bootstrap proof
---

# Disposable MySQL Bootstrap

## Purpose

This directory is the guarded V1 operator utility for preflighting a fresh
ATStudio source snapshot and, only after separate approval and a recorded
current manifest, proving one fresh database without touching the protected
`atstudio` database. The tool can preflight, observe, create, validate, drop,
or inventory possible orphan disposable schemas on loopback MySQL. Every
action requires one explicitly named disposable companion database; Inventory
uses that name only for its preconnection guard and never as a query target. The current
43-table source snapshot has an `UNRECORDED` MySQL manifest expectation after
WI023 added Track columns/indexes. `Create`, `Validate` and `HibernateValidate`
are blocked until a separately approved observation is recorded. The historical
43-table/511-column proof remains historical, not a current PASS.

It is not a retained-data migration tool and must not be used against stage,
production, or any remote host.

REQ005 does **not** require another database for the user's existing service.
The retained-data route is the separate additive SQL described below. This
guide does not authorize any DB creation, observation, DDL or server restart.

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
- The current fresh/disposable MySQL manifest expectation is `UNRECORDED`.
  `Create`, `Validate` and `HibernateValidate` fail closed with
  `MYSQL_MANIFEST_EXPECTATION_UNRECORDED`; neither the historical pre-WI023
  43-table manifest nor WI-067 may substitute for a new observation.
- `Inventory` keeps the same action, loopback, companion-name, and 43-table
  preconnection guards, but is not a manifest operation.
- `Inventory` opens only the root/admin connection, runs one fixed
  `information_schema.schemata` `COUNT(*)` query constrained to
  `^ats_disposable_[0-9]{8}_[a-z0-9]{8}$`, and never selects a target database
  or schema name. Its additional successful output is numeric `inventory.count`
  and `inventory.state`, which is `NO_POSSIBLE_ORPHAN` for zero or
  `POSSIBLE_ORPHAN_EXISTS` for a positive count.
- `Observe` is refused after a manifest is recorded. In the current unrecorded
  state it remains a separately approved, database-creating observation,
  not a read-only inspection of the retained database. Nothing here authorizes it.
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
WI023 adds nine Track columns and a queue index, leaving 43 tables but invalidating
the previous expectation. Current `mysql.manifest.expectation=UNRECORDED`.
Separately, the WI029 handoff / approved REQ007 records the retained TEST DB as
**43 tables / 520 columns**. [WI028](../../deliverables/agent/WI-20260909-ATS-028-evidence-pack.md#runtime--browser-coverage)
corroborates the nine additive column definitions/defaults, queue index and
successful retained startup under `ddl-auto=validate`. Its ALTER column
ordinals differ from fresh source, so this does not record a fresh-bootstrap
manifest or authorize `Observe`/a new DB. The manual SQL was applied once under
REQ006; do not replay it against that database. Other target applications remain
separately approved work, not actions performed by this disposable utility.

The historical pre-feature live/disposable MySQL observation remains:
43 tables, 511 columns, 175 index rows, 91 foreign keys, 6 plans, 6 plan keys,
zero forbidden tables, zero forbidden columns, and SHA-256
`b177b34780fabc75ea8b4608a0d210167a81d414d2778cc1d1dc5c0e39c8fea4`.
Those values and SHA-256 are not recalculated or labeled current. The frozen
external private backup remains pre-feature; WI025 neither inspected nor updated
it. The retained DB later changed under REQ006/WI028, so the old private handoff
is not a current runtime snapshot. WI028 preserved its separate pre-migration
backup, without claiming restore validity; WI029 read no private backup contents.
Editing Java,
DDL or this guide did not apply SQL, restart a server or revalidate that backup.

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
`mysql.manifest.expectation=UNRECORDED`. These are expected source-guard labels,
not a report that WI025 ran the bootstrap or connected to MySQL.

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
current 43-table proof procedure. The new source reports `UNRECORDED`. A future
fresh-disposable proof would need separately approved exact targets, a bounded
observation, recording of actual observed fields/hash, and only then guarded
`Create`, independent `Validate`, the wrapper-managed Hibernate proof and
exact-target cleanup/inventory. No observation is authorized by REQ005 and no
new database is needed merely to document or retain the current service.

If a valid proof cannot run because credentials or Connector/J are unavailable,
record it as an environment-conditional block. Do not weaken a guard, use the
protected database, or add a retired migration to make the proof pass.

## Retained Data

The bootstrap remains fresh-only. For retained data, REQ005 provides
[manual-wi023-audio-processing.sql](manual-wi023-audio-processing.sql), an
apply-once additive `ALTER TABLE tracks` source with nine columns and
`idx_tracks_audio_queue (audio_processing_state, id)`. MA applied it to the
existing TEST MySQL database under REQ006; WI028 independently checked the
additive definitions/index and successful startup under `ddl-auto=validate`.
Do not replay it there. Review the [exact schema](../../docs/design/db-schema.md#wi023-additive-track-columns)
and [rollout gates](../../docs/design/runtime-storage-operations.md#audio-processing-rollout-gates)
before any separately approved application to another retained target:

1. Confirm the owning existing database and absolute public/private roots as
   one tuple, the backup/recovery point and approval; stop its writer.
2. Inspect the target columns/index so a partial or repeated application fails
   closed. Apply only the reviewed additive SQL once, not fresh schema/seed.
3. Validate the actual schema with `ddl-auto=validate` and strict storage
   integrity before enabling new processing. Record new evidence independently
   from the historical manifest. No originals are rewritten or backfilled.
4. Preserve added columns and media on rollback. An old binary is unsafe for
   newly stream-required rows; use a separately approved data-aware recovery,
   never automatic DROP or file deletion.

The named TEST application is complete with WI028's recorded limitations,
not four pending steps. Its ten historical missing references and non-strict
startup do not satisfy the strict target gate above. Fresh installation,
restore rehearsal and production approval remain open. Historical SQL/proofs
and frozen pre-feature backups are not new-schema restore evidence; the global
fresh-bootstrap manifest remains UNRECORDED.
