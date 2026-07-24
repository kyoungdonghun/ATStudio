# WI-20260724-ATS-007 Final Working Tree Audit Summary

> Date: 2026-07-24
> Role: `cr` (Code Reviewer)
> Scope: Read-only audit of the shared final working tree
> Approved requirement: `REQ-20260724-ATS-001`
> Verdict: **PASS** (`P0 = 0`, `P1 = 0`)

## Findings

### P2-001: The generated PDF records a command that is not reproducible in the current environment

The PDF generator reduces the actual Python executable to a stem and writes that
generic name into the manifest:

- `scripts/docs/generate_client_testing_pdf.py:63-64`
- `scripts/docs/generate_client_testing_pdf.py:442-460`

The current guide and generated manifest consequently state that plain `python`
can regenerate the PDF:

- `docs/client/index.md:38`
- `output/pdf/atstudio-client-testing-guide.manifest.json:12-13`

In the audited environment, plain `python` could not import `reportlab`, while
the existing PDF was generated and verified with the bundled workspace Python.
The PDF itself verifies correctly, but its recorded replay command is not
faithful provenance.

**Required correction:** record a non-secret but accurate runtime identity and
provide a replay command or documented dependency bootstrap that succeeds in a
fresh process. Do not derive executable identity with `Path.stem`.

### P2-002: Six retired rate-limit environment aliases remain active

The committed configuration still accepts old availability-rate-limit names as
nested fallbacks:

- `src/main/resources/application.yml:155-156`
- `src/main/resources/application.yml:162-163`
- `src/main/resources/application.yml:169-170`

No active documentation, test, or known consumer was found for these six old
names. They are hidden compatibility paths and can let stale environment values
silently override the V1 client-limit contract.

**Required correction:** retain only the canonical `*_CLIENT_LIMIT` and
`*_CLIENT_WINDOW_SECONDS` names, then add an absence assertion to the baseline
contract test.

### P2-003: The local database example uses a non-canonical password variable

The local example uses `DB_PASSWORD`:

- `application-local.example.yml:14`

The current security policy defines `SPRING_DATASOURCE_PASSWORD` as the database
password contract:

- `docs/policies/security-policy.md:82-94`

This mismatch can make a policy-following local bootstrap fail or encourage a
second undocumented alias.

**Required correction:** change the example to
`SPRING_DATASOURCE_PASSWORD`, or explicitly authorize and document a local-only
alias. V1 consolidation favors the canonical variable.

## Verified Closure Evidence

- No P0 or P1 security, data-integrity, runtime-path, API-contract, or DB-contract
  defect was identified in the reviewed scope.
- The active React SPA remains the only UI path; the current documentation
  count of 56 routes plus the index route matches the router inventory.
- The backend inventory recount found 23 controllers and 137 mapped API
  methods.
- The database source contract recount found 39 unique `CREATE TABLE`
  statements and 39 JPA entities.
- Current targeted contract checks passed: backend environment contract,
  acceptance dry run, demo-seed contract, V1 backend baseline contract, document
  validation, and client PDF verification.
- Tracked secret-pattern scans did not find a production JWT secret, Toss
  secret, URL credential, or inline MySQL password. The fixed JWT value found
  under `src/test/resources/application.yml:19` is test-only.
- Obsolete payment aliases and retired acceptance identifiers appeared only in
  rejection tests or historical/current-state explanations, not as active
  runtime routes.

## Explicit Classifications

- `C:/Windows/Fonts/...` entries in the PDF manifest are Windows system-font
  provenance, not a user-specific machine path. The generator supports explicit
  font overrides. This is environment-conditional and non-blocking.
- `application-local.yml` is intentionally ignored and was not opened because
  it may contain local secrets. Its contents remain environment-conditional.
- The external acceptance secret bundle was not available for inspection.
  Startup is designed to fail closed when obsolete names are present.
- Retained-data migration is deferred policy. V1 supports a verified-empty
  MySQL database initialized from `schema.sql` and `seed.sql`, as stated at
  `docs/policies/security-policy.md:215`.
- Live Toss validation, production HTTPS/proxy/CORS, backup/restore, monitoring,
  client acceptance, and release approval remain open production gates at
  `docs/SR/SR-93.md:40-46`; they are not defects in this audit.
- Historical audits, archived remediation plans, and historical deliverable
  logs were treated as evidence only, not as current runtime claims.
- Remote branch cleanup, tag publication, and the untracked client screenshot
  archive remain MA decisions outside this WI.

## Unverified Scope

- Git status finalization is delegated to MA. The orphan
  `git status --porcelain` process was stopped and was not awaited.
- A fresh empty-database initialization and live server boot were not rerun in
  WI-007.
- Full backend and frontend suites were not rerun in WI-007; their successful
  results were accepted from WI-004 through WI-006 evidence and supplemented
  with the targeted checks listed above.
- Live infrastructure, external secret values, production data, and third-party
  provider state were not inspected.

## Final Decision

WI-007 passes because the mandatory blocking threshold is `P0/P1 = 0`.
The three P2 findings are classified and reproducible; they should be corrected
before the final remote baseline is declared ready. WI-008 should independently
verify those corrections and the MA-owned Git state.
