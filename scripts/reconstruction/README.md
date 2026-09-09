---
version: 1.0
last_updated: 2026-09-09
project: ATS
owner: SE
category: guide
status: active
dependencies:
  - path: ../../docs/policies/security-policy.md
    reason: Secrets and private restoration boundary
  - path: ../../docs/design/runtime-storage-operations.md
    reason: Paired database and storage recovery contract
  - path: ../../docs/SR/SR-93.md
    reason: Separate production approval gates
---

# Development Reconstruction

**TL;DR:** Obtain the approved source revision, read the rules, install the
recorded toolchain, and validate source without private settings. Restoring
the previous development data requires a separate encrypted backup and an
approved isolated restore. Keep the application disconnected from a restored
database until automatic mutations and external actions are demonstrably contained.

## 1. Recover Working Context

1. Clone the approved repository from its owner-provided remote and select the
   approved `main` revision. Record `git rev-parse HEAD` and `git status --short`.
   `52bbdcc0627aff404ad282cdccf4d6ce2418ea84` is the **pre-WI019** source checkpoint,
   not the revision containing this guide. Select the later approved integration
   revision recorded for [REQ004](../../deliverables/user/REQ-20260909-ATS-004.md)
   after WI-20260909-ATS-020 independent review. Until that record is available,
   final integration is pending. Do not recover from an old JAR.
2. Read [AGENTS.md](../../AGENTS.md), [CLAUDE.md](../../CLAUDE.md) for the active
   agent runtime, and the four Tier 0 documents: [constitution](../../docs/standards/core-principles.md),
   [development](../../docs/standards/development-standards.md),
   [documentation](../../docs/standards/documentation-standards.md),
   [glossary](../../docs/standards/glossary.md). Work uses approved REQ -> WI -> delegation.
3. Use the [documentation index](../../docs/index.md) for current design, API,
   schema, UI and [payment](../../docs/payment/index.md) contracts. Read actual
   source/config alongside them. Use the [retention register](../../docs/registry/v1-artifact-retention-20260909.md)
   and [history recovery register](../../docs/registry/development-history-recovery-20260909.md)
   to find preserved/recovered history and its per-document rationale. Private original evidence is not in Git;
   a path/hash entry is not a backup. Old acceptance claims and URLs are dated history.

Keep the tracked [.gitattributes](../../.gitattributes) in every clone/export,
including on a new Windows PC. Its `* text=auto eol=lf` policy (with `.bat` and
`.cmd` using CRLF) preserves the expected checkout line endings for comparison
against the 96 public documents' recorded hashes. Do not override or normalize
those files independently; verify their SHA-256 values against the history register.

Do not copy an external ad-hoc backend launcher, its old provenance hash,
absolute JDK path, current startup JAR, process manifest or Cloudflare URL as
portable SoT. Those can encode an old machine/configuration. Rebuild from
tracked source; select configuration explicitly as described below.

Project skills in `.agents/skills/` and `.claude/skills/` survive Git. Codex/Claude
host-global settings, installed plugins, account authorization and sessions do
not. Reinstall the host tools/plugins and reauthorize required accounts on the
new machine. This procedure does not promise cookie or conversation restoration.

## 2. Toolchain And Dependencies

These are repository-derived requirements at WI019, not claims that the old
machine's tools or versions have been restored. Install tools through trusted
operator-managed distributions; no new package manager or orchestration layer
is needed. Use a local checkout path without symlinks/junctions for this preflight.

| Tool | Reconstruction contract and source |
|---|---|
| Git | Needed to obtain/identify source; no exact version pinned. |
| PowerShell | 7.2+ (`pwsh`) for these scripts and structured lockfile parsing; Windows PowerShell 5.1 is insufficient. |
| JDK | JDK **17**, including `javac`; [build.gradle](../../build.gradle) selects toolchain 17 and Spring Boot 4.0.8. Set `JAVA_HOME` to the installed JDK and its `bin` on PATH. Do not reuse an old absolute path. |
| Gradle | Use tracked [wrapper](../../gradle/wrapper/gradle-wrapper.properties), **9.3.0**. No global Gradle installation required. First invocation may download dependencies and write local caches. |
| Node.js | Use **22.13.0+ within 22.x, or 24.x**, compatible with the current lockfile engines. Vite alone admits older versions; the test stack is stricter. Node 18 and 22.0 are not sufficient for the whole frontend. |
| npm | Use npm supplied with the selected Node installation; no exact npm version is pinned. Commit uses lockfileVersion 3. Run `npm ci`, not an opportunistic lockfile upgrade. |
| Frontend | [package.json](../../frontend/package.json) plus [lockfile](../../frontend/package-lock.json): React 18.3.1, Vite 6.4.3, TypeScript 5.6.3, Vitest 4.1.4, jsdom 29.0.2, Router 7.18.2 at this checkpoint. The lockfile remains authoritative. |
| Python | 3.10+ for `.claude/scripts/` tooling; not needed by this PowerShell preflight. Inspect the particular validator's imports before use; do not blanket-install dependencies. |
| MySQL | 8.x for a separately authorized real-DB runtime/proof; not required for source checks. `mysql` CLI presence does not prove server availability, grants or schema compatibility. |
| FFmpeg | **Not required by this source snapshot.** [AudioAnalysisService](../../src/main/java/com/atstudio/atstudio/service/audio/AudioAnalysisService.java) uses Java Sound with the MP3 SPI dependencies in `build.gradle`. No current FFmpeg executable/version setting is defined. Historical derivative-media discussions do not introduce a setup dependency. |
| cloudflared | Optional, only for approved external acceptance. The [tracked launcher](../acceptance/README.md) uses PATH or `-CloudflaredPath`; no exact version is pinned. Local source/build work needs neither a tunnel nor its old URL. |

After installation, check versions locally with `java -version`, `javac -version`,
`node --version`, `npm --version`, `python --version`, and `git --version`.
`./gradlew.bat --version` additionally verifies the selected wrapper/JVM but may
download the wrapper. Keep absolute machine paths out of shared evidence.

## 3. Source-Only Validation

From a **clean source-only checkout**, without copied ignored files or inherited
private runtime settings, run:

```powershell
pwsh -NoProfile -File ./scripts/reconstruction/Test-DevelopmentRecovery.ps1 -AsJson
pwsh -NoProfile -File ./scripts/reconstruction/Test-DevelopmentRecovery.ps1 -CheckTools -AsJson
pwsh -NoProfile -File ./scripts/reconstruction/test-development-recovery.ps1
```

An explicit `-RepositoryRoot` supports another local source copy. Exit **0**
means the selected checks passed; exit **1** means a required check failed.
The default checks named nonempty source files, core `.claude/config/` rules,
REQ/WI/Evidence Pack skills, the documentation validator, both history registries,
and the parsed package/lock
root identity, direct dependency pairing and expected command names. It does
not execute those commands or fully validate a dependency graph. Missing,
empty, malformed, oversized or reparse-point source inputs fail closed.
Only the two public JSON manifests are read as content; templates are checked
for presence, not parsed as effective settings. Output contains fixed labels,
no raw parser exception, submitted root path or manifest value.

`-CheckTools` performs PATH discovery only, with no child-process execution.
`PRESENT_VERSION_UNVERIFIED` is not a compatible-version claim. Missing Git,
Java/javac, Node/npm or Python fails that mode; missing mysql/cloudflared
is advisory. Windows app-execution aliases can appear present but still fail
manual version checks. Private settings always report `NOT_READ`, DB
`NOT_CONNECTED`, restore `NOT_RUN`, and runtime `NOT_STARTED_OR_VERIFIED`.

After tool installation and approval to download build dependencies, in that
clean copy:

```powershell
npm --prefix frontend ci
npm --prefix frontend run typecheck
npm --prefix frontend run lint
npm --prefix frontend test
npm --prefix frontend run build
./gradlew.bat compileJava compileTestJava bootJar -x test
```

These commands create dependencies/build output, but do not start the product
or establish runtime readiness. `npm ci` may run dependency lifecycle scripts;
review source and use a non-privileged session. Full backend tests/coverage are
a separate reviewed gate: [test configuration](../../src/test/resources/application.yml)
uses H2/create-drop for test fixtures, while MySQL-specific proofs require
their explicit guarded disposable target. Never inherit real-DB test switches
or treat an H2 result/skip as a MySQL restore result. REQ004 assigns clean
staged-source validation to MA, followed by WI-20260909-ATS-020 independent
review; WI019's focused tests are synthetic. Record results in the corresponding
WI Evidence Pack under `deliverables/agent/` before claiming either gate passed.

A same-PC source export can still use installed tools and cached dependencies.
It is useful isolation evidence, **not a new-PC installation or an off-device
restore rehearsal**. Record the revision, commands, checks/skips and that
limitation without claiming more than was exercised.

## 4. Choose The Runtime Path Explicitly

| Path | Required next step; not executed by this guide/preflight |
|---|---|
| Fresh local development | Obtain separate exact-target approval for a fresh DB/schema/seed and new paired storage roots. Supply selected local secrets, validate schema, then authorize runtime startup. No existing DB is disposable by default. |
| Test-only acceptance | Follow the [guarded database guide](../database/README.md) and [tracked acceptance lifecycle](../acceptance/README.md) only after approval. It requires a disposable naming guard, prepared fresh schema, explicit roots, test credentials and opt-in QA bootstrap. It creates/resets test users/plans; it is not equivalent to the prior local runtime. |
| Retained development data | Use the encrypted paired backup and the quarantine gates below. V1's `schema.sql`/`seed.sql` are fresh-only, not migrations. Do not apply them, `ddl-auto=update`, or `create-drop` to a retained DB. Stop for a separate migration/compatibility decision if schema validation fails. |

For an approved local runtime, prepare ignored root `application-local.yml`
privately from [application-local.example.yml](../../application-local.example.yml)
without overwriting an existing file. Replace its placeholder DB user, select
the exact authorized DB URL, and supply only the needed secret references.
Set explicit absolute `APP_STORAGE_PUBLIC_PATH` and `APP_STORAGE_PRIVATE_PATH`
to distinct, non-nested real directories paired with that DB. Never accept the
base configuration's default DB/root paths as evidence that the intended tuple
was selected. Current base [application.yml](../../src/main/resources/application.yml)
does not automatically import the ignored local file.

The tracked Gradle build/`bootRun` task is the ordinary local execution path.
**Only after separate startup authorization and all applicable quarantine gates**,
load the private file explicitly in the dedicated runtime session:

```powershell
$env:SPRING_CONFIG_ADDITIONAL_LOCATION = 'file:./application-local.yml'
./gradlew.bat bootRun
```

The required `file:` location deliberately fails if absent. It is not a
source-only check and must never be run against restored data before isolation.
Use the tracked `npm --prefix frontend run dev` only when frontend startup is
also authorized and its port is free; do not stop somebody else's process.
No ad-hoc launcher is required. Do not assume that a generated JAR or a Vite
build includes private settings or data. Keep `frontend/.env` local; use
[.env.example](../../frontend/.env.example) only for public OAuth client IDs.
Never place backend secrets in a `VITE_` variable.

The external acceptance JSON bundle is **not** a local configuration import.
Recover only selected necessary secrets into the approved local secret source,
never the entire bundle as environment variables. Keep
`APP_ACCEPTANCE_ENABLED=false` and `APP_BOOTSTRAP_TEST_USERS_ENABLED=false`
for retained data; do not select profile `acceptance`. Review all effective
overrides privately because environment variables can override the local YAML.
The tracked acceptance tool deliberately requires bootstrap `true`; do not
weaken that guard to use it as a retained-runtime launcher.

For enabled integrations, verify exact CORS browser origin, mail base URL and
OAuth/Toss callbacks. `localhost` and `127.0.0.1` are different origins and
browser stores. Do not assume old tunnel callback registrations still work.
Keep mail on an isolated sink and Providers inaccessible until separately approved.

## 5. Private Recovery Inventory And Keyring

Git alone cannot reconstruct these assets. The authorized operator must name an
approved **off-device encrypted** backup destination, backup owner, recovery
point and retention policy. No destination is yet approved for REQ004; no
backup upload, dump, secret inspection or full restore is claimed here.

Keep private backups **outside the repository**. `/private-uploads/` and
`uploads/` are ignored as a last-line safeguard, not a backup destination.
Windows DPAPI / `Export-Clixml` SecureString protection tied to one Windows
user/machine is **not sufficient as the sole portable secret backup**. Use an
operator-approved portable encrypted backup with independently recoverable
decryption access that works without the old PC; this WI chooses no encryption
tool and implements no cryptography.

| Private asset | Preserve and restore contract |
|---|---|
| Local configuration and selected secrets | Explicit local YAML/environment configuration; DB URL/user/password; JWT signing key; selected mail/OAuth/Toss credentials and approved endpoints. Protect ACLs and select environment/test-vs-live intent. Never paste values into chat or evidence. |
| Billing encryption keyring | Preserve `app.payment.billing.active-key-id` and **every** `encryption-keys` ID/secret pair needed by retained ciphertext, not just the active key. Base environment mapping is `PAYMENT_BILLING_KEY_ACTIVE_KEY_ID`, `PAYMENT_BILLING_KEY_0_ID`, `PAYMENT_BILLING_KEY_0_SECRET`; additional retained keys require an explicit complete external keyring. |
| Database | Consistent backup including billing/payment/receipt/reconciliation/audit state, storage mutation journal and any pending delivery/retry/outbox state in the selected revision. Treat all row-level dumps as private. Record matching schema/revision metadata privately. |
| Public and private storage | Same recovery point as DB: Track audio/thumbnails, Album/Playlist images, company certification documents, Notice/Question attachments and required journal staging state. Public-serving objects still belong in the backup, not Git. |
| Original evidence and backup manifest | Required private originals referenced by the retention register, paired snapshot IDs/times and SHA-256 inventory. A public sanitized historical document does not replace its private original. |
| Backup decryption access | User-owned password manager/off-device recovery mechanism kept **separate** from the encrypted archive; confirm access without the old PC. Do not include archive passwords or recovery codes in the archive or Git. |

[BillingKeyCrypto](../../src/main/java/com/atstudio/atstudio/service/payment/billing/BillingKeyCrypto.java)
uses a `v2:<key-id>:...` envelope and resolves that ID in the configured ring.
Missing or changed historical key material prevents decryption even if DB and
files restore perfectly. A new JWT key, Toss key, or regenerated billing
encryption secret cannot substitute for the original ring. Do not silently
rotate/re-key, remove encrypted rows or re-register billing as a recovery fix.
The obsolete `PAYMENT_BILLING_KEY_ENCRYPTION_SECRET` is not a replacement.

## 6. Retained-DB Quarantine And Restore Gates

The minimum recovery unit is **DB + public root + private root**, accompanied
by the matching secret keyring. Review restoration with the operator in this order:

1. Approve exact isolated DB and root destinations, consistent snapshot point,
   rollback copies, backup decryption access and the non-public restore session.
   Verify encrypted archive SHA-256 and restored member hashes against the
   private inventory. Stop on a mismatch or unpaired snapshot; never overwrite
   the existing tuple or infer success from an archive filename alone.
2. Keep **all application/worker processes disconnected** from the restored DB
   and roots. Restore/inspect offline with separately scoped operator tools.
   Deny Provider/SMTP egress and public/tunnel access. Egress blocking alone
   does not prevent local writes and is not sufficient to authorize startup.
3. Do not run `bootRun`, a JAR, acceptance or a general application-context
   test against restored data until separately approved controls demonstrably
   suppress all scheduler/startup/after-commit/retry actions. There is **no
   verified all-actions-off configuration switch** in this revision:

| Actual setting or code | What it does, and what it does not do |
|---|---|
| `SPRING_JPA_HIBERNATE_DDL_AUTO=validate`, `SPRING_SQL_INIT_MODE=never` | Prevent automatic schema mutation/SQL initialization, not business writes. |
| `APP_BOOTSTRAP_TEST_USERS_ENABLED=false`, `APP_ACCEPTANCE_ENABLED=false` | Avoid opt-in user/plan bootstrap; do not disable other workers. |
| `APP_AUTH_PASSWORD_LOGIN_ENABLED=false` | Disables password-account flows, not all mail, jobs or Providers. |
| `PAYMENT_RECONCILIATION_NOTIFICATION_ENABLED=false` | Disables optional reconciliation notifications, **not reconciliation itself**. |
| `APP_PAYMENT_SCHEDULER_ZONE` | Time zone only; `@EnableScheduling` and fixed cron jobs still run. No verified `APP_PAYMENT_SCHEDULER_ENABLED` kill switch exists. |
| `APP_STORAGE_RECOVERY_INTERVAL_MS` | Repeat interval only; storage recovery also runs on `ApplicationReadyEvent`. Extending the interval does not disable startup cleanup. |
| `APP_STORAGE_INTEGRITY_AUDIT_ON_STARTUP=true`, `APP_STORAGE_INTEGRITY_STRICT_ON_STARTUP=true`, `APP_STORAGE_REQUIRE_EXPLICIT_ROOTS=true` | Required integrity/containment checks for a later approved restored runtime; **not an isolation mechanism** for other tasks. |

Read [SubscriptionScheduler](../../src/main/java/com/atstudio/atstudio/service/SubscriptionScheduler.java),
[PaymentReconciliationService](../../src/main/java/com/atstudio/atstudio/service/PaymentReconciliationService.java),
[WithdrawalBillingCleanupCoordinator](../../src/main/java/com/atstudio/atstudio/service/WithdrawalBillingCleanupCoordinator.java)
and [StorageMutationRecoveryService](../../src/main/java/com/atstudio/atstudio/service/storage/StorageMutationRecoveryService.java).
Renewals/order expiry/subscription expiry, provider reconciliation, withdrawal
cleanup and storage journal recovery are active paths. Do not invent a generic
Spring scheduling flag or outbox disable flag; no dedicated outbox worker/switch
was found in this source scan. Preserve pending state and reassess all workers
if restoring another revision. Missing kill switches require separate isolation
work/approval, not a change to product code under this WI.

4. Once isolation is proven and startup separately authorized, validate schema,
   run strict storage integrity inspection and approved file-flow checks on the
   restored tuple. The [read-only ADMIN audit](../../docs/design/runtime-storage-operations.md)
   reports missing objects; it does not repair them and calling it requires a
   running, already isolated application. Record each validation boundary.
5. [SR-93](../../docs/SR/SR-93.md#remaining-production-gates) records **10 historical
   missing references**, not a fresh count. Separate those known baseline
   exceptions from **new losses introduced during restoration**. Compare the
   paired backup/restored state, member hashes and missing-reference identities;
   matching counts alone cannot establish equivalence. If they agree with no
   new loss, report **baseline-equivalent with known missing exceptions**, not
   zero-missing healthy or production-ready. New losses block that equivalence
   claim and require investigation within a separately approved recovery scope.
   The approved source/environment recovery intentionally leaves historical
   records unrepaired; repairing those 10 references is not a new prerequisite
   for that recovery. This distinction does not relax strict checks or authorize
   startup: retain the isolation and exact-target startup approval gates above,
   and report any strict-audit failure honestly. Do not delete records, fabricate
   placeholders, hide issues or silently disable strict checks. Historical
   repair/re-upload requires its own approval; production steps 2-4 are unchanged.
6. Keep one approved scheduler owner. Re-enable any automatic job, Provider,
   mail delivery or public access only after an exact-target release decision.

## 7. Completion Boundaries

| Evidence | What may be claimed |
|---|---|
| Source preflight and synthetic failure-path tests | Named source inputs and tested tool contracts; no secrets, DB or services checked. |
| Same-PC clean-source build/tests | Reproducible source at that revision with recorded tools/caches; no new-PC/fullrestore claim. |
| Approved isolated real-DB restore plus paired media/keyring checks | Only the measured restore target and tested flows; not production readiness. **Not run by WI019.** |
| Production tasks 2-4 | Target deployment/HTTPS/secret distribution; chosen data strategy and backup/restore/operational ownership; target acceptance and explicit final release approval remain separately gated by SR-93. This guide neither chooses the production target nor closes those gates. |

## Related Documents

- [Root README](../../README.md): First checkout entry point.
- [Security policy](../../docs/policies/security-policy.md): Secret handling.
- [Archive policy](../../docs/policies/archive-policy.md): Current versus historical records.
- [REQ004](../../deliverables/user/REQ-20260909-ATS-004.md): Approved scope, MA integration/backup ownership and WI020 independent review.
