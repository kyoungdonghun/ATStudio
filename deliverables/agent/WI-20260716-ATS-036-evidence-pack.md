# WI-20260716-ATS-036 Evidence Pack

## 1. Identity and constraints

| Field | Value |
|---|---|
| WI | `WI-20260716-ATS-036` |
| REQ | `REQ-20260716-ATS-004` (approved) |
| Role | Software Architect (`sa`) |
| Audit mode | Static, read-only |
| Repository | `C:/Users/jm991/Desktop/project/ATStudio` |
| Branch observed | `codex/p1-acceptance-hardening` |
| HEAD observed | `a96d2e0c5d249723bbf449b6834299a04cf2ad30` |
| Runtime/DB mutation | None |
| Product/schema/config/Git mutation | None |
| Permitted outputs | This Evidence Pack and the WI user summary only |

This pack classifies candidates for WI-038. It does not authorize or perform remediation.

## 2. Method and reproducibility

The audit used read-only filesystem, text-search, hash, and Git-history commands. Representative commands:

```powershell
rg --files src/main/resources src/main/java/com/atstudio/atstudio scripts src/test
rg -n "CREATE TABLE|@Entity|@Table|ddl-auto|sql.init|bootstrap|application-local|PAYMENT_BILLING_KEY" .
Get-FileHash -Algorithm SHA256 src/main/resources/schema.sql
Get-FileHash -Algorithm SHA256 src/main/resources/seed.sql
Get-ChildItem src/main/resources/db/manual -File
git log -1 --format="%H" -- <path>
git status --short
```

No SQL client, Gradle task, npm task, server process, lifecycle script, or DB connection was invoked.

## 3. Fresh schema and entity inventory

### 3.1 Source facts

| Source | Static result | Evidence |
|---|---|---|
| `src/main/resources/schema.sql` | 1,105 lines; SHA-256 `ADD49AA2C56C7275ADA1B71A224232725EC93ACEB6F633DB5AC23A671DDF0F05` | file hash and line count |
| `src/main/resources/seed.sql` | 548 lines; SHA-256 `082F41980CB3F490D289B3AF092CDA7C1F2116B60D3A1728366BD7E9EF293F16` | file hash and line count |
| Fresh tables | 41 `CREATE TABLE IF NOT EXISTS` statements | `schema.sql` static count |
| JPA table mappings | 41 `@Entity` table mappings | entity source static count |
| Name parity | 41 exact matches; no schema-only or entity-only table | normalized table-name comparison |
| Constraints | 41 PKs, 24 unique keys, 84 FKs, 34 named indexes, 1 CHECK, 59 ENUM declarations | `schema.sql` static count; runtime semantics not proven |

`schema.sql:2` declares schema v13, while `docs/design/db-schema.md:15` declares v20. `schema.sql:1104` says 38 tables although the static count is 41. The design document also has an internal stale heading, `docs/design/db-schema.md:1189`, saying 39 tables after stating 41 entities at line 18.

### 3.2 Exact table-name parity

| Table | Entity |
|---|---|
| `album_likes` | `AlbumLike` |
| `album_tracks` | `AlbumTrack` |
| `albums` | `Album` |
| `answers` | `Answer` |
| `billing_agreements` | `BillingAgreement` |
| `company_certification_audit_logs` | `CompanyCertificationAuditLog` |
| `company_certification_documents` | `CompanyCertificationDocument` |
| `company_certifications` | `CompanyCertification` |
| `download_queue` | `DownloadQueue` |
| `email_verification_tokens` | `EmailVerificationToken` |
| `licenses` | `License` |
| `likes` | `Like` |
| `notice_attachments` | `NoticeAttachment` |
| `notices` | `Notice` |
| `password_reset_tokens` | `PasswordResetToken` |
| `payment_entitlement_corrections` | `PaymentEntitlementCorrection` |
| `payment_operation_audit_logs` | `PaymentOperationAuditLog` |
| `payment_orders` | `PaymentOrder` |
| `payment_receipts` | `PaymentReceipt` |
| `payment_reconciliation_incidents` | `PaymentReconciliationIncident` |
| `payment_refunds` | `PaymentRefund` |
| `payment_settlements` | `PaymentSettlement` |
| `play_histories` | `PlayHistory` |
| `playlist_tracks` | `PlaylistTrack` |
| `playlists` | `Playlist` |
| `question_attachments` | `QuestionAttachment` |
| `questions` | `Question` |
| `site_settings` | `SiteSetting` |
| `social_accounts` | `SocialAccount` |
| `storage_mutations` | `StorageMutation` |
| `subscription_payments` | `SubscriptionPayment` |
| `subscriptions` | `Subscription` |
| `tags` | `Tag` |
| `track_downloads` | `TrackDownload` |
| `track_tags` | `TrackTag` |
| `tracks` | `Track` |
| `user_subscriptions` | `UserSubscription` |
| `users` | `User` |
| `whitelist_channels` | `WhitelistChannel` |
| `whitelist_export_batches` | `WhitelistExportBatch` |
| `whitelist_export_items` | `WhitelistExportItem` |

Name parity is complete. Full column/default/constraint parity remains an execution proof item because Hibernate `validate` does not prove every MySQL index, CHECK, FK delete rule, ENUM member, or column-order property.

## 4. Manual SQL inventory and disposition

Exactly nine files exist under `src/main/resources/db/manual/`. Every file targets a retained/existing DB. Every resulting DDL shape is represented in the current fresh schema. REQ-004 states that no DB data must be preserved; therefore all nine are `REMOVE` candidates after the clean-V1 proof and active-reference rewrite.

| File | Lines | Prerequisite and data dependency | Current-schema overlap | Classification and retirement proof |
|---|---:|---|---|---|
| `20260615_align_payment_whitelist_schema.sql` | 320 | Assumes earlier payment baseline; changes `tags.type`, adds/backfills whitelist workflow and primary-channel state, creates whitelist export and settlement tables. Backfill is data-dependent. | `schema.sql:84`, `:255-326`, payment settlement section from `:598` | `REMOVE`. Prove fresh schema creates final tag/whitelist/export/settlement shape; prove required clean baseline data without backfill. |
| `20260618_company_certification_documents.sql` | 22 | Creates certification child-document table; retained legacy document disposition is data-dependent. | `schema.sql:164-181` | `REMOVE`. Prove fresh child table/entity mapping and clean certification workflow on a no-legacy DB. |
| `20260714_payment_db_integrity.sql` | 677 | Requires prior two patches; runs ambiguity preflights, adds command/payment/refund/agreement fields and ENUMs, performs bounded repair/backfill, creates unique keys/indexes, can abort with SQLSTATE `45000`. | `schema.sql:489-691` | `REMOVE`. Prove clean ledger schema, exact indexes/ENUMs/FKs, payment race suite, and no retained-data repair requirement. |
| `20260714_storage_mutations_journal.sql` | 37 | Creates storage mutation journal for an existing DB. | `schema.sql:1072-1098` | `REMOVE`. Prove fresh table/entity and storage contract without patch reference. |
| `20260715_track_waveform_data.sql` | 75 | Conditionally adds `tracks.waveform_data`. | `schema.sql:219` | `REMOVE`. Prove fresh column/type/entity mapping and waveform contract without patch reference. |
| `20260716_company_certification_integrity_and_audit.sql` | 74 | Requires company-document patch; adds optimistic version/index and audit ledger. | `schema.sql:144-203` | `REMOVE`. Prove fresh version/index/audit table plus certification concurrency/audit behavior. |
| `20260716_download_atomicity.sql` | 61 | Aborts on duplicate user-track licenses before adding unique key. | `schema.sql:479` | `REMOVE`. Prove fresh unique key and duplicate-race behavior on MySQL. |
| `20260716_payment_reconciliation_indexes.sql` | 106 | Requires payment baseline/integrity patch; adds payment-order and billing-agreement scan indexes and contains `EXPLAIN FORMAT=JSON`. | `schema.sql:521`, `:562` | `REMOVE`. Prove exact index column order and capture fresh-DB EXPLAIN plans. |
| `20260716_whitelist_integrity_and_exports.sql` | 110 | Adds version, export snapshot/filter columns and export-scope index. | `schema.sql:255-326` | `REMOVE`. Prove fresh optimistic-lock/export shape and whitelist race/export tests. |

### 4.1 Chain overlap risk

The retained-DB chain contains historical index convergence rather than a single canonical declaration. The 20260615 patch creates `idx_whitelist_channels_status_requested(status, requested_at)`; the later whitelist patch adds `idx_whitelist_channels_export_scope(status, requested_at, id)`; the fresh schema consolidates the canonical shape as `idx_whitelist_channels_status_requested(status, requested_at, id)`. Keeping all three active indefinitely preserves ambiguity over name and column order.

### 4.2 Active tests that bind to manual file names

These tests must be rewritten to assert the V1 baseline directly before the files can be retired:

- `src/test/java/com/atstudio/atstudio/entity/PaymentDatabaseIntegrityContractTest.java`
- `src/test/java/com/atstudio/atstudio/entity/StorageMutationContractTest.java`
- `src/test/java/com/atstudio/atstudio/entity/TrackWaveformSchemaContractTest.java`
- `src/test/java/com/atstudio/atstudio/entity/CompanyCertificationSchemaContractTest.java`
- `src/test/java/com/atstudio/atstudio/service/DownloadConcurrencyContractTest.java`
- `src/test/java/com/atstudio/atstudio/service/WhitelistConcurrencyContractTest.java`

Current design, payment, audit, SR, and historical WI documents also reference the patch chain. Current-state documents must be rewritten; historical WI evidence should remain immutable or be moved only under the archive policy.

### 4.3 Rollback sources

The observed HEAD, `a96d2e0c5d249723bbf449b6834299a04cf2ad30`, is a complete pre-remediation rollback snapshot. Path history additionally identifies these last-introduction groups:

| Paths | Git source |
|---|---|
| `20260615_align_payment_whitelist_schema.sql` | `4d5a026b...` |
| `20260618_company_certification_documents.sql` | `f1ab10f0...` |
| `20260714_payment_db_integrity.sql` | `103fdf49...` |
| storage mutation and waveform patches | `b2172346...` |
| four 20260716 integrity/index patches | `622828b1...` |

Before implementation, WI-038 should record full hashes or create an approved V1 pre-cleanup tag. No DB rollback is needed for this WI because no DB was touched.

## 5. Fresh-schema residue classification

| Unit | Evidence | Classification | Required action in later WI |
|---|---|---|---|
| `src/main/resources/schema.sql` | All 41 tables use `IF NOT EXISTS`; `FOREIGN_KEY_CHECKS` is disabled/re-enabled at `:21`/`:1100`; version/count comments are stale. | `REPLACE` | Retain the path as SoT, make fresh creation fail closed, correct metadata, and prove dependency order without hidden compatibility behavior. |
| `tracks.preview_file` | `schema.sql:217`; `Track.java:40-41`; production reference in `StorageReferenceChecker.java:23`. Streaming tests now require the full original, and the user rejected preview-only policy. | `REMOVE` | Coordinate with backend WI; prove no storage reference or API contract needs the column. |
| `whitelist_export_items.user_id_snapshot` and `user_nickname_snapshot` | `schema.sql:308,310`; entity fields; current export builder does not populate them and tests expect null. | `REMOVE` | Remove only with entity/schema/test coordination; keep email snapshot and actual export evidence. |
| Billing crypto v1 and `billing.encryption-secret` | `BillingKeyCrypto.java:25-27,67-72,90-99`; `PaymentProperties.java:44-47`. | `REMOVE` | With no retained ciphertext, keep v2 key-ring only; security review must confirm negative startup behavior. |
| `subscription_payments.provider` nullable compatibility | `schema.sql:580`; `SubscriptionPayment.java:58-60`; current builders set provider, but legacy one-time paths remain. | `REVIEW` | Decide after backend WI resolves legacy endpoints; then make provider mandatory if no valid null producer remains. |
| Payment/audit/reconciliation/lease/version fields | Current operational safety and concurrency mechanisms. | `KEEP` | Do not mistake resilience state for legacy compatibility. Preserve unless a separate design proves replacement. |
| Company certification `document_path` | Still written by `CompanyCertificationService` and `TestUserBootstrapRunner`. | `KEEP` | Not a removal candidate in this audit. |

## 6. Seed and bootstrap inventory

| Path | Current role and evidence | Classification | V1 target |
|---|---|---|---|
| `src/main/resources/seed.sql` | Mixed destructive/demo seed. It deletes tag relations/tags, inserts six plans although comment says five, and also creates demo tags, tracks, albums, notices, admin/test content. It is not auto-run by main config. | `REPLACE` | Minimal, deterministic baseline-data owner containing only six canonical plans and any proven mandatory site settings. Move demo data to an explicit fixture path. |
| `src/main/java/com/atstudio/atstudio/bootstrap/AcceptanceSubscriptionPlanBootstrapRunner.java` | Re-declares the same six plans when acceptance and bootstrap flags are enabled. | `REPLACE` | Remove duplicate ownership or convert to a validator after baseline data has one owner. |
| `TestUserBootstrapProperties.java` and `TestUserBootstrapRunner.java` | Creates five QA users and expected subscription/certification fixtures only when explicitly enabled; rejects production and requires explicit non-production profile/password. | `KEEP` | Retain as non-production acceptance tooling; keep disabled by default and outside baseline data. |
| `scripts/demo/seed-client-demo.mjs` and `.ps1` | Explicit API-driven client-demo population; not a startup initializer. | `REVIEW` | Keep only if demo capture remains an active workflow; otherwise archive after evidence retention. Never make it baseline data. |
| `deliverables/agent/WI-20260714-ATS-021/*` and `WI-20260715-ATS-007/*` disposable MySQL helpers/logs | Historical proof utilities and immutable evidence, not current application runners. | `ARCHIVE` | Retain as historical evidence under archive policy; do not use as V1 runtime/bootstrap source. |
| `PaymentMysqlSchemaValidationTest.java` and `PaymentMysqlConcurrencyIntegrationTest.java` | Active but tied to WI007 DB names/environment guards and historical helper contract. | `REPLACE` | Generalize into guarded V1 MySQL proof tests while retaining the validated race semantics. |

No Flyway or Liquibase dependency was found in `build.gradle`. The proposed V1 does not require introducing a migration framework because retained DB upgrade is explicitly out of scope.

## 7. Profile, initialization, and consumer inventory

| Path or setting | Evidence | Classification | Finding |
|---|---|---|---|
| `src/main/resources/application.yml` | Imports `optional:file:./application-local.yml` at `:6`; defaults Hibernate to `validate` at `:18`; payment provider defaults to MOCK at `:113`. No main `spring.sql.init.mode` enables schema/seed. | `KEEP` path, targeted `REVIEW` | Valid base profile, but local import and stale provider semantics need explicit resolution. |
| `src/test/resources/application.yml` | H2 `create-drop`; `spring.sql.init.mode=never`. | `KEEP` | Useful unit-test isolation; not MySQL schema proof. |
| `src/main/resources/application-acceptance.yml` | Acceptance profile, `ddl-auto=validate`, bootstrap false by default; legacy billing secret property but no active key ID/key-ring wiring. | `REPLACE` | Isolate it from ignored local config and express complete v2 key-ring inputs. |
| `application-local.example.yml` | Local profile, `ddl-auto=update`, QA bootstrap enabled, MOCK provider, legacy secret only. | `REPLACE` | Default to `validate`, bootstrap false, and a non-secret v2 key-ring example. |
| ignored root `application-local.yml` | Key-only inspection found `ddl-auto=update`, bootstrap enabled, TOSS provider, and no explicit active profile. Secret values were not inspected or recorded. | `REVIEW` | Machine-local stale state; later remediation requires user awareness and must not copy secrets into Git. |
| `AcceptanceProperties.java`, `AcceptancePublicUrls.java`, acceptance host/CORS/trusted-identity consumers | Explicit public URL/host/identity controls for acceptance. | `KEEP` | Acceptance safety tooling, not production bypass. |
| `AcceptanceStartupGuard.java` | Billing validation is conditional on the generic provider selector. | `REPLACE` | Validate the actual active recurring-provider configuration independently of stale one-time selector semantics. |
| `PaymentProperties.provider` and `app.payment.provider` | Defaults MOCK; recurring billing service uses `TOSS_BILLING` directly in `BillingAgreementApplicationService.java:60,132`. | `REVIEW` | Selector does not select the active recurring provider and can suppress startup checks. Rename, narrow, or remove after consumer review. |
| `scripts/acceptance/AcceptanceLifecycle.psm1` and start/status/stop/test scripts | Environment allowlist includes legacy `PAYMENT_BILLING_KEY_ENCRYPTION_SECRET`, omits active key ID/indexed key-ring variables, starts backend from repo root, and can inherit optional local config. | `REPLACE` | Pass complete v2 crypto settings and isolate the acceptance process from ignored root config. Preserve host/CORS/bootstrap safeguards. |
| `build.gradle` DB stack | JPA/MySQL runtime and H2 test dependencies; no migration engine. | `KEEP` | Sufficient for a fresh-only V1 baseline when external schema application is explicit and proved. |

## 8. Decision ledger

These are decision units, not a count of unique files. A retained file may contain a setting classified separately.

| Classification | Count | Units |
|---|---:|---|
| `KEEP` | 5 | Base application path; test application path; Gradle DB stack; acceptance URL/host/CORS support; explicit non-production QA bootstrap |
| `REMOVE` | 12 | Nine manual SQL files; preview-file compatibility; two legacy whitelist snapshot fields as one unit; billing crypto v1/legacy secret path |
| `REPLACE` | 8 | Fresh schema; mixed seed; acceptance profile; local example; startup guard; plan bootstrap; acceptance lifecycle; WI-specific MySQL proof tests |
| `ARCHIVE` | 1 | Historical disposable MySQL helper/log bundles |
| `REVIEW` | 4 | Generic payment provider selector; ignored local config; demo seed scripts; nullable subscription-payment provider compatibility |
| **Total** | **30** | To be reconciled with WI-034, WI-035, and WI-037 by WI-038 |

## 9. Clean V1 fresh-DB proof plan

This is a future execution plan. Every destructive or DB-mutating step requires the approval gate specified by REQ-004.

### Phase A: approved source baseline

1. Reconcile WI-034 through WI-037 in WI-038 and obtain user approval for the exact `REMOVE`, `REPLACE`, and `ARCHIVE` set.
2. Record a full pre-cleanup commit/tag and full path hashes.
3. Implement a fail-closed `schema.sql`, one minimal baseline-data owner, profile isolation, and generalized MySQL proof tests.
4. Rewrite active tests/current-state documents that bind to the nine manual filenames. Preserve historical WI evidence.

### Phase B: disposable target guard

1. Create a unique MySQL 8 database named like `ats_v1_proof_<UTC>_<random>` using credentials outside the repository.
2. Enforce an exact database-name allow pattern before create, apply, or drop.
3. Query `information_schema.tables` and require zero tables before applying anything.
4. Abort if the target is shared, retained, non-empty, or lacks an explicit disposable marker.

### Phase C: single-source schema creation

1. Apply only `src/main/resources/schema.sql`; do not apply any `db/manual/*.sql` file.
2. Stop on the first SQL error and capture a secret-free execution log.
3. If a new baseline-data file is approved, apply only that file after schema success.
4. Assert exactly six canonical subscription plans and no QA/demo users, tracks, albums, tags, or notices in the production baseline.

### Phase D: structural proof beyond Hibernate

Compare `information_schema` against an approved manifest:

- exact table set against the JPA table set;
- engine and collation;
- column type, precision, nullability, default, generated behavior, and order where contractual;
- PK, unique key, and ordinary index name plus column order;
- FK columns, referenced targets, and delete/update rules;
- exact MySQL ENUM members against Java enums;
- CHECK constraints;
- absence of helper procedures left by schema execution;
- negative assertions for approved removed columns and legacy config paths;
- `EXPLAIN FORMAT=JSON` evidence for payment reconciliation scan indexes.

### Phase E: production-shaped startup

Start the backend against the disposable DB with all of these explicit:

```text
SPRING_JPA_HIBERNATE_DDL_AUTO=validate
SPRING_SQL_INIT_MODE=never
APP_BOOTSTRAP_TEST_USERS_ENABLED=false
APP_ACCEPTANCE_ENABLED=false
```

The launch must be isolated from root `application-local.yml`. Prove application readiness and a read-only `GET /api/tracks` response. Do not treat H2 startup as equivalent evidence.

### Phase F: separate acceptance proof

1. Recreate a second disposable DB from the same schema and minimal baseline.
2. Enable only the acceptance profile and explicit test-user bootstrap inputs.
3. Prove exactly five expected QA accounts and their intended fixtures are created.
4. Prove public URL, HTTPS callback, allowed-host, CORS, trusted-client, and secret/key-ring startup guards.
5. Prove production profile plus test bootstrap is rejected.

### Phase G: behavior and repeatability

1. Run generalized MySQL schema validation.
2. Run the retained seven payment/concurrency race scenarios plus certification, download, whitelist, storage, and waveform focused contracts.
3. Run the full backend test/build gates owned by the later implementation WI.
4. Re-run `schema.sql` against the now non-empty proof DB and require failure. This proves the baseline is not acting as a migration or silently accepting stale state.
5. Search active source/current docs for all nine retired filenames and require zero references; historical evidence paths may remain.

### Phase H: cleanup and rollback

1. Capture redacted proof artifacts and exact source commit.
2. Drop only the disposable databases after the exact-name guard succeeds.
3. On source failure, restore the approved pre-cleanup commit/tag.
4. On DB proof failure, discard the disposable DB and fix source; never repair a retained DB as part of this plan.

## 10. Risks and approval-sensitive decisions

| Risk/decision | Impact | Required gate |
|---|---|---|
| Removing the manual chain before active tests/docs are rewritten | Broken contracts and misleading runbooks | WI-038 scope approval, then coordinated implementation |
| Keeping `IF NOT EXISTS` | A stale non-empty DB can appear successfully initialized | Approve fail-closed fresh-only V1 semantics |
| Removing preview and legacy snapshot columns | Cross-WI product/entity/storage impact | Backend and integration review |
| Removing billing v1 crypto | Old ciphertext becomes unreadable | Confirm no retained DB/ciphertext, then security review |
| Replacing local/acceptance config | Developer and client-demo startup behavior changes | Explicit profile/config approval; no secret capture |
| Applying or dropping a proof DB | Destructive infrastructure operation | Separate user approval immediately before execution |

## 11. Inspection completeness and limits

### Exhaustively inspected in requested surface

- All 9 files under `src/main/resources/db/manual/`.
- All 41 fresh-schema table names and all 41 JPA entity table names.
- The schema/seed entry points, two committed main application profiles, test profile, root local example, ignored local key structure, three bootstrap classes, three acceptance config classes, six acceptance scripts, two demo seed scripts, and active MySQL proof tests.
- Direct source/test/current-document references to the nine manual SQL filenames.

### Could not be exhaustively proved without violating the WI

- Actual local, shared, staging, or production MySQL schema/data/history was not inspected because no DB connection was allowed.
- Full MySQL execution semantics for all columns, defaults, ENUMs, CHECKs, indexes, FKs, and DDL ordering were not proved because SQL and Hibernate validation were forbidden.
- Secret values in ignored `application-local.yml` were intentionally not inspected or reported; only key shape and non-secret mode flags were considered.
- Product code outside DB/config/bootstrap consumers and the frontend were not exhaustively reviewed; WI-034 and WI-035 own those surfaces.
- Runtime acceptance environment inheritance, callback reachability, and provider credentials were not exercised.

These are explicit proof gaps, not evidence of success. They are covered by the clean V1 proof plan or by blocked WI-038.

## 12. Files created by this WI

1. `deliverables/user/WI-20260716-ATS-036-summary.md`
2. `deliverables/agent/WI-20260716-ATS-036-evidence-pack.md`

No other file was intentionally created or modified.
