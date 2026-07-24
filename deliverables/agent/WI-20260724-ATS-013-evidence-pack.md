# Evidence Pack: WI-20260724-ATS-013

## Summary

- **Verdict: PASS**
- Rehearsed the current V1 fresh schema and seed on two separately created,
  regex-guarded loopback MySQL databases.
- Passed Hibernate schema validation 1/1 and payment concurrency races 7/7.
- Dropped the proof DB and retained the separately seeded runtime DB for
  WI-014 through WI-016.
- Created and validated a restricted repo-external backend environment bundle.

## Scope / DoD Check

- [x] Used fresh clone
  `C:\Users\jm991\AppData\Local\ATStudio\release-rehearsal-3147873-20260724`
  at commit `3147873c42bfd7883fdaa92922c0485e5fc72621`.
- [x] Required loopback MySQL and exact
  `^ats_wi007_20260724_[a-z0-9]{8}$` target names before any target connection.
- [x] Applied only fresh-clone `src/main/resources/schema.sql`, then
  `src/main/resources/seed.sql`, exactly once per database.
- [x] Did not run the historical WI-007 manager unchanged or apply retired
  manual SQL.
- [x] Matched the V1 manifest contract.
- [x] Passed MySQL schema validation 1/1 and concurrency races 7/7.
- [x] Dropped the proof DB and proved absence count `0`.
- [x] Proved a separately seeded runtime DB starts with
  `spring.jpa.hibernate.ddl-auto=validate`.
- [x] Retained the runtime DB and restricted bundle for WI-014 through WI-016.
- [x] Did not modify active repository source, commit, or push.

## Reference Documents

| Tier | Document | Reason |
|---|---|---|
| 0 | `docs/standards/core-principles.md` | System constitution and approval/security rules |
| 0 | `docs/standards/development-standards.md` | QA and database verification standards |
| 0 | `docs/standards/documentation-standards.md` | Evidence-pack structure and traceability |
| 0 | `docs/standards/glossary.md` | Canonical ATStudio terminology |
| 1 | `docs/policies/security-policy.md` | Secret handling and fresh-only V1 DB baseline |
| 1 | `docs/policies/quality-gates.md` | Database and release quality gates |
| Context | `deliverables/user/REQ-20260724-ATS-002.md` | Approved release rehearsal |
| Context | `deliverables/agent/WI-20260724-ATS-013-handoff.md` | Corrected WI scope and output contract |
| Context | `docs/SR/SR-93.md` | Remaining production-readiness boundary |
| Context | `docs/audit/p1-payment-integrity-closure-20260715.md` | Historical 7-race MySQL proof contract |

## Evidence Pointers

### Official Outputs

- `deliverables/user/WI-20260724-ATS-013-summary.md`
- `deliverables/agent/WI-20260724-ATS-013-evidence-pack.md`

### Repo-External Runtime

- Runtime root:
  `C:\Users\jm991\AppData\Local\ATStudio\release-rehearsal-runtime-3147873-20260724`
- Successful run:
  `C:\Users\jm991\AppData\Local\ATStudio\release-rehearsal-runtime-3147873-20260724\20260724T043001Z-0a78ecbc`
- Redacted run summary:
  `...\20260724T043001Z-0a78ecbc\run-summary.json`
- Redacted test log:
  `...\20260724T043001Z-0a78ecbc\mysql-proof-tests.log`
- Redacted validate-boot log:
  `...\20260724T043001Z-0a78ecbc\validate-boot.log`
- Bundle validation:
  `...\20260724T043001Z-0a78ecbc\bundle-validation.log`
- Runtime pointer:
  `...\runtime-pointer.json`
- Cleanup contract:
  `...\cleanup-contract.md`
- Restricted secret bundle:
  `...\backend-environment-wi013.json`

The restricted bundle is a required runtime secret artifact, not committed
evidence. Its values were never copied into this pack or any redacted log.

## Helper Contract

The transient helper is:

- `...\ReleaseRehearsalMysqlHelper.java`
- SHA-256:
  `0739959fe4f42c52dc77517972fda7e8d2be8217274d6967eb07194ca358c809`

The runner is:

- `...\run-wi013-release-rehearsal.ps1`
- SHA-256:
  `94dff0f8e54f7d086b780f0cd45102c7648b921b9ca74ca08aa47032d58abcc7`

Guard behavior:

1. Parse only datasource values for MySQL helper operations.
2. Require source URL host `localhost`, `127.0.0.1`, or `[::1]`.
3. Open admin connections without a selected database.
4. Require the exact WI-013 regex before every disposable target connection.
5. Refuse protected/system database names.
6. Permit only fresh-clone `schema.sql` and `seed.sql` as SQL inputs.
7. Require absence before create and verify absence after drop.
8. Log only aliases, counts, hashes, SQL state/error code, and PASS/FAIL.

## Schema / Seed / Manifest

| Evidence | Result |
|---|---|
| Schema statements | 39 |
| Schema SHA-256 | `3cd43f4c46b95b04badea2a086e433c362d7b5b13824e1f3abe9e106f98db31a` |
| Seed statements | 1 |
| Seed SHA-256 | `14bbaca1e697e94b98b2f38ff16951cefafafc2c1efa337336ef6d18b2b7900f` |
| Seed plan rows / distinct keys | 6 / 6 |
| Tables | 39 |
| Columns | 449 |
| Indexes | 153 |
| Foreign keys | 80 |
| Forbidden tables / columns | 0 / 0 |
| Provider columns / Toss-only | 9 / 9 |
| Manifest SHA-256 | `c48d3c75378aaf2364d89ed06833ba68e27a5a334dbc4670d1443bd938c6c506` |

Both proof and runtime DB creation logs report the same schema, seed, and
manifest results.

## Tests

Sanitized command contract:

```powershell
$env:ATSTUDIO_MYSQL_PROOF_ENABLED = "true"
$env:ATSTUDIO_MYSQL_PROOF_TARGET = "disposable"
# Datasource values were injected in process and never printed.
gradlew.bat --no-daemon cleanTest test --rerun-tasks `
  --tests "com.atstudio.atstudio.service.PaymentMysqlSchemaValidationTest" `
  --tests "com.atstudio.atstudio.service.PaymentMysqlConcurrencyIntegrationTest"
```

| Suite | Tests | Failures | Errors | Skipped |
|---|---:|---:|---:|---:|
| `PaymentMysqlSchemaValidationTest` | 1 | 0 | 0 | 0 |
| `PaymentMysqlConcurrencyIntegrationTest` | 7 | 0 | 0 | 0 |
| **Total** | **8** | **0** | **0** | **0** |

- Gradle marker: `BUILD SUCCESSFUL`
- JUnit XML:
  `build/test-results/test/TEST-com.atstudio.atstudio.service.PaymentMysqlSchemaValidationTest.xml`
- JUnit XML:
  `build/test-results/test/TEST-com.atstudio.atstudio.service.PaymentMysqlConcurrencyIntegrationTest.xml`
- Measured test duration: 56.421 seconds

The proof DB was then dropped and the separate absence check returned `0`.

## Validate Boot

Sanitized runtime contract:

```text
profile=acceptance
spring.jpa.hibernate.ddl-auto=validate
spring.sql.init.mode=never
server.port=ephemeral
datasource.hostClass=loopback
datasource.alias=RUNTIME
```

Result:

- `Started AtStudioApplication` marker: present.
- Schema-failure marker: absent.
- Process tree stopped after proof: yes.
- Raw process output retained: no.
- Boot duration: 19.54 seconds.
- Boot-output SHA-256:
  `4fe4538f2df25d3cac2f941fc3c1d02c25115bf014913cc70e84d0512cfe9139`.
- Runtime manifest remained
  `c48d3c75378aaf2364d89ed06833ba68e27a5a334dbc4670d1443bd938c6c506`
  after boot.

The runtime DB remains available through the restricted bundle for the next
three WIs. Its actual name is not recorded here.

## Protected Database Proof

No JDBC connection selected protected `atstudio`, and no application table was
read or mutated. The helper used an unselected loopback admin connection and
read only `information_schema` metadata.

| Check | Before | After | Result |
|---|---|---|---|
| Selected database | none | none | PASS |
| Direct protected connection | false | false | PASS |
| Protected data read | false | false | PASS |
| Metadata fingerprint | `c684fc803e3c5b3d53e8c1d6fad8bab90c37bfe8d8f766e05e55f58df3b546a7` | same | PASS |
| Schema SHA-256 | `c48d3c75378aaf2364d89ed06833ba68e27a5a334dbc4670d1443bd938c6c506` | same | PASS |
| Table-metadata SHA-256 | `ac14ba1af84620ab50f600fcaec20ac459cd51b734eead4e9b7d6fb4f58616a0` | same | PASS |

This is metadata-only non-mutation proof, not protected application-data
inspection.

## Sanitized Bundle Validation

| Check | Result |
|---|---|
| Flat repo-external JSON | PASS |
| Current allowlisted values copied from old bundle | 19 |
| Current required names present | 6/6 |
| Non-allowlisted names | 0 |
| `APP_PAYMENT_PROVIDER` | absent |
| `TOSS_CONFIRM_URL` | absent |
| Datasource host | loopback |
| Datasource target | guarded runtime alias |
| Inheritance disabled | yes |
| Explicit ACL rules | 1 current-account Full Control |
| Bundle SHA-256 | `489eaffd01b95af9404f68981b7f9f9c226b446aebe52386c20d6fa51cf19751` |

Post-run scan:

- High-entropy datasource/password/secret/key values scanned: 7.
- Matching files outside the restricted bundle: 0.
- Runtime database-name matching files outside the restricted bundle: 0.
- Raw process artifacts: 0.
- Gradle daemon logs: 0.

## Repository Integrity

- HEAD remained `3147873c42bfd7883fdaa92922c0485e5fc72621`.
- The only pre-existing tracked diff was the WI-012 PDF manifest replay
  artifact:
  `output/pdf/atstudio-client-testing-guide.manifest.json`.
- Before/after artifact SHA-256:
  `80ba87fb4ded8a548a4660458c946e274fef1509089246f91505bede003588a4`.
- WI-013 changed no tracked file in the fresh clone.
- No commit or push was performed.

## Diagnostic History

Before the final PASS run:

1. The first runner attempt failed before DB access because the initial ACL
   implementation attempted owner-aware `Set-Acl` behavior requiring
   `SeSecurityPrivilege`.
2. ACL handling was replaced with `icacls /inheritance:r /grant:r` for the
   current account only. No ownership operation remains.
3. One proof run produced Gradle `BUILD SUCCESSFUL` and JUnit 8/8 but the
   wrapper checked an unavailable process exit value before XML parsing. The
   proof DB still dropped and returned absence `0`.
4. One runtime attempt hit Windows sharing on the temporary boot log. Both
   proof and runtime DBs were cleaned to absence; the reader was replaced with
   `FileShare.ReadWrite`, and raw files were removed.
5. The final run completed every gate. These diagnostics did not reuse a
   partially seeded DB.

## Cleanup Contract

Owner: `WI-20260724-ATS-017`.

1. Stop only processes proven owned by WI-014 through WI-016 manifests.
2. Read the runtime datasource URL from the restricted bundle in process.
3. Assert loopback host and exact
   `^ats_wi007_20260724_[a-z0-9]{8}$` database name.
4. Pass the target through `ATSTUDIO_WI013_DATABASE`.
5. Run helper mode `drop`, then helper mode `absence`.
6. Reconfirm protected metadata fingerprint without selecting `atstudio`.
7. Remove the restricted bundle, helper source/classes, redacted logs, pointer,
   cleanup contract, and runtime root only after DB absence proof.

## Risks / Boundaries

- PASS proves the fresh-only V1 baseline and disposable MySQL behavior. It does
  not prove retained-data migration or production deployment.
- The runtime DB is intentionally retained and contains QA bootstrap state after
  validate boot.
- Provider calls, real payments, refunds, external SMTP, and production secrets
  were not exercised.
- Production readiness remains open under `SR-93`.
