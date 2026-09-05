[WI HEADER]
WI ID: WI-20260817-ATS-016
REQ: REQ-20260817-ATS-009
Agent: qa-integ
Depends On: WI-20260817-ATS-015, explicit DB execution confirmation
Blocks: WI-20260817-ATS-017

[WI SUMMARY]
Why: Establish the current 43-table fresh-install MySQL manifest as reproducible evidence, then restore the guarded Create/Validate lifecycle without touching retained or production data.

Scope (in/out):
- In: One guarded observation disposable DB, current-manifest constants/guard tests, one distinct guarded proof disposable DB, Hibernate `ddl-auto=validate` integration verification, exact cleanup, and current-state documentation updates caused by the recorded evidence.
- Out: `atstudio` or any retained DB, remote/stage/production MySQL, user data, provider payment/refund calls, email delivery, live secrets, retained-data migrations, application feature changes.

DoD:
- Exactly one fresh `Observe` run uses a unique guarded loopback `ats_disposable_YYYYMMDD_xxxxxxxx` name, applies only current `schema.sql`/`seed.sql`, emits the current structural manifest, fails closed as expected under `UNRECORDED`, and leaves no observation DB.
- The observed 43-table manifest is recorded in the bootstrap expectation and guard tests/documentation are updated without storing credentials or target names.
- Exactly one distinct guarded proof database passes `Create`, independent `Validate`, and targeted Hibernate `ddl-auto=validate` verification.
- Exact `Drop` removes only the proof DB and a read-only bounded inventory confirms no possible guarded disposable orphan remains.
- No existing DB, external Provider, or mail transport is queried or changed.

Constraints/Forbidden:
- Run only after a new explicit execution confirmation that covers ephemeral DB creation and cleanup.
- Accept credentials only from the existing repo-external acceptance environment bundle; never print, inspect, copy, commit, or persist its values/path.
- The source guard must reject any host other than loopback and any name outside the disposable pattern before credential load/connection.
- Do not use manual SQL against a retained DB. The bootstrap itself is the only supported DDL entry point.
- Persist only structural counts/hash and normalized PASS/FAIL codes, never a JDBC URL, username, host, port, database name, raw SQL, or raw rows.

[ACCEPTANCE CRITERIA]
Functional:
- [x] Current observation returns 43 tables and the required schema/seed structural manifest before its expected fail-closed cleanup.
- [x] Recorded manifest causes guarded `Create` and separate `Validate` to pass for a distinct fresh proof DB.
- [x] Targeted MySQL integration test starts against only the proof DB with Hibernate `ddl-auto=validate` and no external side effect.
- [x] Guarded exact Drop and bounded Inventory leave/report zero possible disposable orphan schemas.
Quality:
- [x] `scripts/database/test-bootstrap-guards.ps1` passes with current manifest expectations.
- [x] Backend targeted test plus `gradlew.bat test` pass after any source updates.
- [x] `validate_docs.py` and `git diff --check` pass.

[INPUT POINTERS]
Tier 0 (Constitution - Required):
- docs/standards/core-principles.md
- docs/standards/development-standards.md

Tier 1 (Policies):
- docs/policies/security-policy.md
- docs/policies/quality-gates.md

Tier 2 (Database and operations):
- scripts/database/README.md
- docs/design/db-schema.md
- docs/SR/SR-93.md
- docs/payment/system-overview.md
- docs/payment/feature-inventory.md

REQ/Context Docs:
- deliverables/user/REQ-20260817-ATS-009.md
- deliverables/agent/WI-20260817-ATS-015-evidence-pack.md
- deliverables/agent/WI-20260817-ATS-012-evidence-pack.md

Files:
- scripts/database/bootstrap-disposable-mysql.ps1
- scripts/database/DisposableMysqlBootstrap.java
- scripts/database/test-bootstrap-guards.ps1
- scripts/database/README.md
- src/test/java/com/atstudio/atstudio/config/V1BackendBaselineContractTest.java
- src/main/resources/schema.sql
- src/main/resources/seed.sql

[OUTPUT CONTRACT]
User-facing -> deliverables/user/WI-20260817-ATS-016-summary.md:
- Structural evidence only, exact scope, cleanup state, test results, residual production gates.
Agent-facing -> deliverables/agent/WI-20260817-ATS-016-evidence-pack.md:
- Source/output pointers, commands, normalized results, rollback.
Handoff Packet -> deliverables/agent/WI-20260817-ATS-016-handoff.md:
- This packet.

[TRACEABILITY REQUIREMENTS]
Evidence pointers (files/lines/commands/logs): Required.
Tests: Include source guards, Observe/Create/Validate/Drop/Inventory normalized outputs, targeted MySQL validation, backend test, docs validation.
Rollback: Restore pre-recorded bootstrap constants/docs; disposable DBs must already be absent. No retained DB rollback path is permitted because retained DBs are out of scope.

[RECOVERY AMENDMENT - 2026-08-17]
Approval: The user explicitly approved this recovery amendment for the existing
WI-20260817-ATS-016. This amendment neither creates nor renumbers a WI.

Decision gate:
- First run exactly one guarded, read-only `Inventory` through the supported
  wrapper. Retain and report only its disposable-pattern count and normalized
  state. Do not retain a database name or any raw output.
- If the result is one or more possible orphans, stop immediately. Do not
  delete, enumerate, identify, or issue any further database query. Report the
  normalized state and propose a separately approved cleanup WI.
- Only if the result is zero possible orphans, repair the safe-output capture
  approach, not bootstrap behavior, so it accepts the supported wrapper's
  ordered `Preflight` and requested-action blocks. Then run one fresh
  `Observe` through that wrapper and proceed only when complete safe structural
  evidence and automatic cleanup are both confirmed.

Preserved prohibitions:
- Do not print, inspect, copy, or persist the opaque acceptance bundle path or
  contents; use the default opaque local bundle internally only.
- Do not use manual SQL; do not access an existing, remote, or production
  database; do not invoke provider payment, refund, or email behavior.
- Keep every runtime record to the permitted structural values and normalized
  statuses only. Never record a database name, JDBC data, credentials, raw
  streams, raw rows, or diagnostics.

Recovery outcome (accepted evidence):
- The earlier Observe and Inventory safe-capture failures are preserved as
  prior attempts only; neither supplied accepted evidence.
- The later recovery Inventory passed with `count=0` and
  `state=NO_POSSIBLE_ORPHAN`.
- Fresh Observe safe capture passed and established the 43-table manifest:
  43 tables, 511 columns, 175 indexes, 91 foreign keys, 6 plans, 6 plan keys,
  zero forbidden tables/columns, and SHA-256
  `b177b34780fabc75ea8b4608a0d210167a81d414d2778cc1d1dc5c0e39c8fea4`.
  The expected pre-record refusal was
  `MYSQL_MANIFEST_EXPECTATION_UNRECORDED`, and cleanup passed.
- A distinct proof lifecycle passed Create, independent Validate, actual
  Hibernate `ddl-auto=validate`, exact Drop, and final Inventory with
  `count=0` / `state=NO_POSSIBLE_ORPHAN`.
- No retained, remote, or production database, provider payment/refund, or
  email was touched.
- WI status may become `complete` only after every unchecked quality criterion
  above passes in this closeout.
