---
version: 1.0
last_updated: 2026-10-10
project: ATS
owner: SE
category: evidence-pack
status: stable
dependencies:
  - path: WI-20261010-ATS-011-handoff.md
    reason: Approved disjoint implementation scope
  - path: ../user/REQ-20261010-ATS-006.md
    reason: Local-only implementation approval
  - path: ../../deploy/single-vps/runbooks/resource-baseline.md
    reason: Selected resource and admission design
---

# Evidence Pack: WI-20261010-ATS-011

## Summary

Implemented per-instance storage-mutation admission before servlet body parsing,
explicit admission/SMTP/Hikari/multipart bindings, and 57 new isolated tests.
Final selected suite: **105 passed, 0 failures/errors/skips**, 26-second Gradle run.
Verification: 2026-10-10, Windows local shared workspace, SE; test timestamps
08:49:46-08:49:56 UTC (17:49 KST). Target performance **NOT RUN**, production **HOLD**.
This is implementation evidence for independent WI014 review, not REQ closure.

## Scope / DoD Check

- [x] Global 2 / trusted-client IP 1, immediate 429 with Retry-After 2, no capacity waiting queue.
- [x] All six storage-writing HTTP domain families, including JSON/nonmultipart cleanup paths.
- [x] Servlet registration before FormContentFilter, security proxy, and auto-registered component filters.
- [x] Shared unchanged StrictHttpFirewall rules, existing authentication and protected-media rules preserved.
- [x] Synchronous success/exception/Error cleanup; async timeout/error continuation, redispatch, restart, completion and duplicate completion tests.
- [x] In-flight client counters use process-salted SHA-256 fingerprints; zero-count entries removed.
- [x] Actual Binder targets and JavaMail Session properties tested with synthetic overrides, no external configuration sources.
- [x] Mock-JDBC Spring transaction boundary proof and bounded nested/background consumer inventory.
- [x] Two deliverable sets, exact changed-file identities, commands, failures and limits.
- [ ] WI014 independent review and named-target/container/mixed-load acceptance remain outside this WI.

## Reference Documents (Tier 0-2)

| Tier | Pointer | Use |
|---|---|---|
| Entry | `AGENTS.md` | SE delegation, disjoint scope, language, evidence gates |
| 0 | `docs/standards/core-principles.md` | Approval, least side effects, transparency |
| 0 | `docs/standards/documentation-standards.md` | Metadata and two-set document format |
| 0 | `docs/standards/development-standards.md` | Java 17, transaction and test conventions |
| 0 | `docs/standards/glossary.md` | Canonical domain terminology |
| 1 | `docs/policies/security-policy.md` | Trusted identity, fingerprint privacy, protected media |
| 1 | `docs/policies/access-control-policy.md` | Local-only boundaries |
| 1 | `docs/policies/quality-gates.md` | Reuse, evidence and independent review |
| 2 | `docs/index.md` | Current decision entry and historical evidence separation |
| 2 | `deploy/single-vps/runbooks/resource-baseline.md` | Global2/IP1 design, pool and mail limits |
| 2 | `deploy/single-vps/runbooks/restic-backup-plan.md` | Backup boundary context only, no implementation here |
| 2 | `deliverables/user/REQ-20261010-ATS-006.md` | Approved execution scope and WI014 dependency |
| 2 | `deliverables/user/REQ-20261010-ATS-005.md` | Prior design completion, not target acceptance |
| Skill | `.agents/skills/test/SKILL.md`, `.agents/skills/build-check/SKILL.md` | Focused cached Gradle compilation/tests |
| Skill | `.agents/skills/create-wi-evidence-pack/SKILL.md` | This directly written Evidence Pack |
| Registry | `.claude/config/workspace.json` | ATS project tag |

Injection source is the approved WI011 handoff; assignee SE, implementation/testing,
Tier 0-2 pointers above. Conditional frontend pointers were not loaded: no frontend
work. No new agents were spawned. No Git commands were executed, including status
or branch queries; branch/HEAD/dirty-baseline inventory remains MA-owned. The
source identities below, rather than a historical HEAD, identify this verification.

## Evidence Pointers

Paths below are relative to the repository root.

| Changed file | Purpose |
|---|---|
| `src/main/java/com/atstudio/atstudio/config/StorageMutationAdmissionConfiguration.java:16` | One servlet registration, order `HIGHEST_PRECEDENCE + 10`, REQUEST/ASYNC, async support; shared strict firewall also configured on normal security |
| `src/main/java/com/atstudio/atstudio/config/StorageMutationAdmissionProperties.java:13` | Validated positive numeric limits, enabled by default |
| `src/main/java/com/atstudio/atstudio/security/StorageMutationAdmissionFilter.java:58` | Firewall, classification, admission, body-independent rejection |
| Same filter, `:112`, `:146` | Atomic short counter bookkeeping and idempotent lifecycle lease |
| `src/main/java/com/atstudio/atstudio/config/SecurityConfig.java` | Explanatory early-servlet-registration comment only; authorization/filter chain behavior unchanged |
| `src/main/resources/application.yml:11`, `:36`, `:45`, `:96` | Pool, mail, multipart and admission environment placeholders |
| `src/test/java/com/atstudio/atstudio/security/StorageMutationAdmissionFilterTest.java` | Routing, concurrency, identity, lifecycle, privacy and body guard tests |
| `src/test/java/com/atstudio/atstudio/security/StorageMutationAdmissionServletOrderTest.java` | Boot registration collector, ordered chain including FormContentFilter and existing component copies, actual SecurityConfig authorization |
| `src/test/java/com/atstudio/atstudio/security/StorageMutationAdmissionTransactionTest.java` | Spring JDBC transaction manager with mock connections and actual proxied journal/coordinator; peak two connections, cleanup before permit release |
| `src/test/java/com/atstudio/atstudio/config/StorageMutationAdmissionBindingTest.java` | Shipped YAML defaults/overrides, invalid-limit rejection and JavaMail Session property values |

The filter is not a component or standalone Filter bean, and is not added to the
security chain. This avoids duplicate admission registration. The early guard
validates using the same StrictHttpFirewall instance configured for normal
security, without relaxing its allowed methods, separators, matrix parameters,
percent escapes or dot-segment rules. Allowed encoded letters are decoded once;
context path is excluded. Rejections never log identifiers or access request body,
parts, reader or parameter map. Client identity comes only from the existing
TrustedClientIdentityResolver; raw forwarded headers are not new trust sources.

Async listener registration occurs inside both startAsync overloads before control
returns to application code. Timeout/error notifications do not end ownership:
they may be followed by continued work or dispatch. onStartAsync reattaches to the
new cycle. onComplete marks terminal, and release also waits for active filter
invocations to return. Duplicate terminal notifications cannot decrement twice.
An async request that never completes deliberately keeps its permit, bounded by
the configured admission count; there is no timer-based eviction while work may
still be active. Detached work surviving servlet completion is not supported by
this HTTP-lifecycle contract; current affected controllers are synchronous.

## HTTP And Connection Consumer Inventory

Classification conservatively covers every unsafe method under `/api/tracks`,
`/api/albums`, `/api/playlists`, `/api/notices`, `/api/questions` and
`/api/company-certifications`. It is independent of Content-Type and auth outcome.
GET/HEAD/OPTIONS are unaffected. TRACE remains rejected by StrictHttpFirewall.
Exact POST `/api/tracks/batch` is excluded because it is read-only player hydration.
Other write-family metadata operations share the cap; unrelated reads do not.

| HTTP controller/service path | Storage effects covered |
|---|---|
| Track POST root, PUT/DELETE id; POST admin/id/audio-processing/retry | Create/replace audio and thumbnail; update/deactivate pending-audio cleanup; retry is conservatively admitted |
| Album POST root, PUT/DELETE id | Canonical thumbnail create/replace/delete; membership writes conservatively admitted |
| Playlist POST root, PUT/DELETE id | Thumbnail create/replace/delete; membership writes conservatively admitted |
| Notice POST root, PUT/DELETE id | Private attachments, removal IDs on update, delete cleanup |
| Question POST root, DELETE id | Private attachment store/delete; answer/status writes conservatively admitted |
| CompanyCertification POST root, POST me/documents | Private canonical documents and replaced-document cleanup; review PUT conservatively admitted |

Inspected current controllers, the corresponding six services, storage coordinator,
journal, cleanup, reference checker, recovery, and audio worker/transaction code.
No service business rules or transaction annotations were modified.

| Connection consumer | Observed nesting / acquisition / concurrency boundary |
|---|---|
| Six domain service transactions -> StorageMutationCoordinator -> StorageMutationJournalService | Business transaction first; journal prepare/transition/failure methods use REQUIRES_NEW. Journal calls return sequentially; after-commit/rollback callbacks run before outer resource cleanup. Focused real Spring/mock-JDBC proof observes peak 2 and zero connections before admission release. Two admitted HTTP mutations can therefore plan for 4, not reserve 4. |
| StorageCleanupService -> StorageReferenceChecker | Reference checker is REQUIRED read-only; joins an existing transaction or opens its own when none exists. No added REQUIRES_NEW. Cleanup/journal work is sequential. |
| TrackAudioProcessingWorker -> TrackAudioProcessingTransactions | Dedicated single-thread executor, synchronized processing. Claim is short; encoding/generate runs outside business transaction; complete/fail/recoverInterruptedBatch can nest journal work at outer+inner peak. Worker is not HTTP-admitted. |
| StorageMutationRecoveryService | ApplicationReadyEvent plus scheduled recoverBatch. Journal claim transaction returns before cleanup/reference checks and later transitions. These steps are sequential within one invocation. A single runtime/recovery owner is an operational assumption, not a new global lock or reserved connection. |
| PaymentReceiptEvidenceService | AFTER_COMMIT listener + REQUIRES_NEW; originating resource can still be held during receipt persistence. Outside storage admission. |
| PaymentCommandTransactionService / BillingAgreementCleanupTransactionService -> PaymentReconciliationIncidentService | REQUIRES_NEW command/cleanup methods can invoke another REQUIRES_NEW incident operation. This is separate nested demand, not covered by storage permits. |
| PaymentRefundTransactionService, BillingAgreementPrepareTransactionService, PaymentReconciliationTransactionService | Existing REQUIRES_NEW families; orchestration includes NEVER/NOT_SUPPORTED boundaries in payment/reconciliation/refund flows. Concurrent callers still compete for the same pool. |
| AdminPaymentSettlementAttemptTransactionService / AdminPaymentSettlementRowTransactionService | REQUIRES_NEW attempt and row operations, invoked by settlement import outside storage admission. |
| UserService / AdminSubscriptionCorrectionService -> AdminOperationRejectionAuditService | Existing outer transaction can call REQUIRES_NEW rejection audit. Outside storage admission. |
| SubscriptionScheduler / RecurringRenewalService | Renewal at 00:00, expiry 00:10, stale-order expiry 00:30; renewal orchestration uses NEVER with command transactions, expiry methods own transactions. No scheduler was enabled or changed by this WI. |
| PaymentReconciliationService / WithdrawalBillingCleanupCoordinator | Reconciliation 01:00, billing cleanup retry 01:15; withdrawal also has AFTER_COMMIT listener. Cleanup calls REQUIRES_NEW helpers/incident work; retained outer-resource behavior needs target observation. |
| Ordinary controllers, authentication lookups, startup/integrity/bootstrap | May consume pool connections outside this gate. No universal scheduler/startup-off assumption or full application startup used in tests. |

Search inventory found 11 existing REQUIRES_NEW service classes (all named above,
including StorageMutationJournalService). This is a bounded source inventory,
not a complete mathematical bound on all application connection demand. Pool10
does not reserve storage4/worker2/background2/ordinary2 partitions, eliminate
starvation, or prove deadlock freedom. Mixed-work latency/heap/pool acceptance,
single worker/recovery ownership and target settings remain NOT RUN.

## Configuration Contract

| Environment variable | Bound property | Default |
|---|---|---|
| APP_STORAGE_MUTATION_ADMISSION_ENABLED | app.storage.mutation-admission.enabled | true |
| APP_STORAGE_MUTATION_ADMISSION_MAX_CONCURRENT | app.storage.mutation-admission.max-concurrent | 2 |
| APP_STORAGE_MUTATION_ADMISSION_MAX_CONCURRENT_PER_IP | app.storage.mutation-admission.max-concurrent-per-ip | 1 |
| APP_STORAGE_MUTATION_ADMISSION_RETRY_AFTER_SECONDS | app.storage.mutation-admission.retry-after-seconds | 2 |
| SPRING_DATASOURCE_HIKARI_MAXIMUM_POOL_SIZE | spring.datasource.hikari.maximum-pool-size | 10 |
| SPRING_DATASOURCE_HIKARI_MINIMUM_IDLE | spring.datasource.hikari.minimum-idle | 2 |
| SPRING_DATASOURCE_HIKARI_CONNECTION_TIMEOUT | spring.datasource.hikari.connection-timeout | 5000 ms |
| MAIL_SMTP_CONNECTION_TIMEOUT | spring.mail.properties.mail.smtp.connectiontimeout | 5000 ms |
| MAIL_SMTP_TIMEOUT | spring.mail.properties.mail.smtp.timeout | 10000 ms |
| MAIL_SMTP_WRITE_TIMEOUT | spring.mail.properties.mail.smtp.writetimeout | 10000 ms |
| SPRING_SERVLET_MULTIPART_FILE_SIZE_THRESHOLD | spring.servlet.multipart.file-size-threshold | 0B |

Final source names were reported to MA during implementation and match the
concurrently edited WI012 environment example on read-only inspection. No WI012
deployment assets were edited here. Mail phase properties are not a total
DNS/retry/delivery deadline. Existing file/request limits remain 100MB/120MB.

## Commands And Tests

Final reproduction command (PowerShell, repository root, existing cached toolchain):

```powershell
.\gradlew.bat --offline --no-daemon test --tests 'com.atstudio.atstudio.security.StorageMutationAdmission*Test' --tests 'com.atstudio.atstudio.config.StorageMutationAdmissionBindingTest' --tests 'com.atstudio.atstudio.security.TrustedClientIdentityResolverTest' --tests 'com.atstudio.atstudio.security.AuthRateLimitFilterTest' --tests 'com.atstudio.atstudio.security.JwtAuthenticationFilterTest' --tests 'com.atstudio.atstudio.security.AcceptanceHostFilterTest' --tests 'com.atstudio.atstudio.service.storage.StorageMutationCoordinatorTest' --tests 'com.atstudio.atstudio.service.storage.StorageMutationJournalServiceTest' --tests 'com.atstudio.atstudio.service.storage.StorageMutationRecoveryServiceTest' --tests 'com.atstudio.atstudio.config.AudioProcessingConfigTest'
```

| Suite (short name) | Tests | Result |
|---|---:|---|
| StorageMutationAdmissionFilterTest | 46 | PASS |
| StorageMutationAdmissionServletOrderTest | 4 | PASS |
| StorageMutationAdmissionTransactionTest | 2 | PASS |
| StorageMutationAdmissionBindingTest | 5 | PASS |
| TrustedClientIdentityResolverTest | 5 | PASS |
| AuthRateLimitFilterTest | 13 | PASS |
| JwtAuthenticationFilterTest | 4 | PASS |
| AcceptanceHostFilterTest | 3 | PASS |
| StorageMutationCoordinatorTest | 11 | PASS |
| StorageMutationJournalServiceTest | 6 | PASS |
| StorageMutationRecoveryServiceTest | 4 | PASS |
| AudioProcessingConfigTest | 2 | PASS |
| Total | 105 | 0 failed / 0 errors / 0 skipped |

Compilation of production/test source succeeded. Full build/check/coverage suites
were not run. Expected JVM CDS/bootstrap-classpath warning from Mockito was present.
No @SpringBootTest, JPA slice, H2 or MySQL was started. Test resource location is
`src/test/resources/application.yml`; binding tests explicitly read only the
shipped main YAML, removing system environment/properties first. JavaMail Session
creation is local, with no send/testConnection/connect. HikariConfig is bound
without creating HikariDataSource or a pool. Transaction tests use mock JDBC
connections and mock storage/repositories, not real DB/files. No real FFmpeg path.

Servlet-order proof uses Boot ServletContextInitializerBeans and the real
OrderedFormContentFilter, SecurityConfig, JWT/auth/host filters. The security proxy
registration in this isolated test mirrors Boot order -100 explicitly. It is not
a listening embedded server or actual deployment. The collected chain then rejects
urlencoded PUT/PATCH/DELETE, multipart and JSON before body access or user lookup.
This extends direct filter mocks but does not claim target-container/proxy proof.

Iterations before final PASS: first 47 tests had 2 failures because encoded test
URIs incorrectly reused raw spelling as decoded servletPath; fixture corrected to
container semantics. One compile attempt failed on AssertJ wildcard Class varargs;
assertions were made type-compatible. Next 53-test run had one assertion expecting
literal FilterChainProxy rather than Spring's CompositeFilterChainProxy subtype;
changed to assignability/order assertions. None required weakening production
security. Initial security-chain-only design was replaced before completion after
MA identified FormContentFilter ordering and premature timeout/error release.
Incidental read-only command failures: PowerShell does not expand bash brace globs;
retried using rg file globs. `jar` was absent from PATH; cached JAR entries were read
with .NET ZIP APIs instead. A nonexistent PaymentRefundService read was corrected
to the actual AdminPaymentRefundService inventory. No downloads/install occurred.

Reports are mutable Gradle output at `build/reports/tests/test/index.html` and
`build/test-results/test/TEST-<fully-qualified-suite>.xml`. Final new-suite XML hashes:

| Suite | SHA-256 |
|---|---|
| Binding | `1C36F60EF45178D8A39C19EF694D64CDFB5E238E585B4073BB0C3AFDB37C2278` |
| Filter | `4768FA397E16408CF0C9A0D8EE178E18EB1A83851494324433674F00AC0FFAE3` |
| ServletOrder | `40075885B796C3AF965E7829DAFF1D4637B19418D8BB50F3D46FF769EF4F3C76` |
| Transaction | `28F353C1F271D0CFBB80A9948046C14843157F8004C39F3C912C3C0508F154E9` |

## Changed-Hash Inventory

SHA-256, captured before own edits and after final tested source. NEW means a new
WI011 path. Existing dirty content was preserved; no checkout/reset/stage/commit.

| Path (under repository root) | Before | After |
|---|---|---|
| src/main/java/com/atstudio/atstudio/config/SecurityConfig.java | `0842746FA3C6D46003E2822BF39EADFA4DC227230C2A7D973819A235D72A3A24` | `47676EBEB550C839F24FF0085D74339C191EB6AA277BC578EEA5A654B514B5CD` |
| src/main/resources/application.yml | `394BDA0E0AF515BBF9D80D90FFF163D7F54E513EC788F9532205C959C6E449FD` | `A493FFE54DA9DA77B56009338438F9C0718A2CA45CAD1C21241548E1052C9709` |
| src/main/java/com/atstudio/atstudio/config/StorageMutationAdmissionConfiguration.java | NEW | `138ACC117253DC28E51383DDA82CAC40E61FF311FA20C630AB49C1FDA3775F72` |
| src/main/java/com/atstudio/atstudio/config/StorageMutationAdmissionProperties.java | NEW | `CD2D4230D0AB3CEE563C5D64EE9550C1C82813CB17151A71244DB158310F82AE` |
| src/main/java/com/atstudio/atstudio/security/StorageMutationAdmissionFilter.java | NEW | `2CF36E355ED1FA29147022B2802EEC23AE55F978B9AE37943A4CB9CCA9C5CD48` |
| src/test/java/com/atstudio/atstudio/config/StorageMutationAdmissionBindingTest.java | NEW | `DBEF991D9E02D7D0373579A34153D974ED42BB316C073DA91608256387CA0EF8` |
| src/test/java/com/atstudio/atstudio/security/StorageMutationAdmissionFilterTest.java | NEW | `929EE41382727AE799C5F835F7552ABBC81AFFA97BAC5FBBD945088645C4D47A` |
| src/test/java/com/atstudio/atstudio/security/StorageMutationAdmissionServletOrderTest.java | NEW | `344A462F6A23A440A81B8CDEC945491FD0783D85BC559A989A8075FB2AE3CBB5` |
| src/test/java/com/atstudio/atstudio/security/StorageMutationAdmissionTransactionTest.java | NEW | `0F3D5FCE89A12D773C529D22DD809F3BAADC0F1EFDA1E27C5EF3C352ADB1D776` |

New documents: this Evidence Pack and
`deliverables/user/WI-20261010-ATS-011-summary.md`. Their self-referential hashes
are intentionally not embedded. Historical handoff/REQ/deliverables were not edited.
All manual edits used apply_patch; Gradle generated only normal build/cache output.
Final PowerShell checks: required metadata, relative Markdown links and trailing
whitespace PASS for both documents; Get-FileHash comparison against the table PASS
9/9. All SE Gradle sessions have exited; Java build/test ownership is released to
WI014. No further SE Gradle run is planned during independent review.

## Risks / Rollback

- Per-JVM admission and shared-NAT limits are not cluster-wide or per-user guarantees.
- Hung async work holds one of the bounded permits until servlet completion; forcing
  early release would admit overlap. No detached storage task may outlive completion.
- No actual Tomcat/proxy request, SMTP stall, Hikari pool exhaustion, Linux resource,
  target heap/GC, mixed workload or backup/restore acceptance was run.
- No real services were started/stopped, external settings/secrets read, real DB or
  retained media accessed, network backup/install/purchase/destructive/Git action performed.
- Rollback requires an approved scoped patch removing only WI011 additions and YAML
  keys/comment, preserving all other workers' changes. Do not reset the shared tree.
  Disabling admission while keeping the larger pool is not a validated alternative.
- Before deployment, WI012 settings/validator and this application change must be
  reviewed together; target single-worker/recovery and auth/proxy identity evidence
  are separate gates. Pool10 is not a mathematical deadlock cure.

## Follow-ups

WI011 blocks WI014. MA should immediately trigger the approved independent WI014
review when WI012/WI013 dependencies are also available. SE did not spawn descendants,
modify another worker's scope, or close REQ006. Review early filter ordering, shared
firewall behavior, async completion and the external connection-consumer inventory.

## Related Documents

- [Approved handoff](WI-20261010-ATS-011-handoff.md)
- [REQ006](../user/REQ-20261010-ATS-006.md)
- [User summary](../user/WI-20261010-ATS-011-summary.md)
- [Resource baseline](../../deploy/single-vps/runbooks/resource-baseline.md)
