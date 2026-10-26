---
version: 1.0
last_updated: 2026-10-11
project: ATS
owner: SE
category: evidence-pack
status: draft
dependencies:
  - path: WI-20261011-ATS-063-handoff.md
    reason: Bounded sidecar implementation authority
  - path: WI-20261011-ATS-059-evidence-pack.md
    reason: First-admin and fixture design
  - path: ../user/REQ-20261011-ATS-007.md
    reason: Approved local preparation and MA-only native boundary
---

# Evidence Pack: WI-20261011-ATS-063

## Summary

**Local candidate ready for independent review; native NOT RUN and mechanically locked.**
Implemented first-admin inspect/promotion, private one-use receipts and two bounded WAVs.
Buyer entitlement remains deferred. Additional user-authorized focused service tests passed.
This is SE evidence, not independent PG/RE approval or a native PASS.

## Scope / DoD Check

- [x] New files only within the handoff ownership; no application or old-tool edits.
- [x] Strict binary framing, expiring private approval, artifact pins and scoped statuses.
- [x] Inspect, absent/invalid target, existing ADMIN, drift, replay and uncertain-commit branches.
- [x] One conditional role update and one existing-schema audit in the same transaction.
- [x] Deterministic two-file fixtures, decoded byte/duration/hash and no-overwrite tests.
- [x] Java 17 compilation, local harness, Python tests and five focused existing test suites.
- [ ] Independent same-byte review and disposable MySQL concurrency/transaction proof.
- [ ] Native release, actual target execution and post-promotion fresh authentication.

No service commands, DB connections, account creation, mail sends, credential reads,
native approval/claim writes, deletion, commit or push were performed. Test fixtures and
synthetic receipt tests write only new `build/WI063-*` paths. Existing build/test history
is retained. No subdelegation was performed.

## Evidence Owner And Identity

SE inspected and verified locally on 2026-10-11, Windows/PowerShell, Java 17.0.12.
The local reporting checkpoint was 21:17:55 KST; this is not a native execution time.
Branch `main`, HEAD `58b1011dad796017e47dbcc82a4225ca449bcdf6`, pre-existing dirty worktree.
The focused Gradle run compiles the shared checkout as observed during that run; it does
not freeze concurrently owned application work or attest the deployed application.

## Changed Files And Review Pins

All paths below are repository-relative. SHA-256 identifies the reviewed candidate,
not production deployment. Generated directories are ignored build assets.

| New file | SHA-256 |
|---|---|
| `scripts/deployment/feature-provisioning.py` | `4c4ee98261f3f2b26df1f489c876cc24962606f9e091938ba48a9cf22b474619` |
| `scripts/deployment/RuntimeFeatureProvisioner.java` | `ada1ad5ed941b1d578898a1b817b581ff6df6660b868494b16fc88aeb546dd57` |
| `scripts/deployment/test_feature_provisioning.py` | `e7d5173e44af711e724d44b37fe13b130bae9477e4e44b81cc2544f558d69074` |
| `scripts/deployment/RuntimeFeatureProvisionerHarness.java` | `cd01ce6d0aba23d4f14a260716930a3e41fbe999819ddaa8a7b1b1a0f411e8cf` |
| `scripts/validation/feature-trial/fixtures.py` | `5ee908c1e52901d7e4459ad69dd0024b784a9595138cc747b54ffd1a26dfb857` |
| `scripts/validation/feature-trial/test_fixtures.py` | `d517c0ec0687197ba53d06eebde5b2fe14f8cef48c1ea8fdf94e5267b536df2e` |
| `build/WI063-focused-20261011/tests.init.gradle` | `4b7bf136c2fd1387c0d9e786110552115cd78020b18e45ef3585f2f95aa5acc5` |

Other new deliverables are this Evidence Pack and the [WI063 summary](../user/WI-20261011-ATS-063-summary.md).
Local compiled bundle: `build/WI063-provisioner-final/artifact.json`;
bundle SHA-256 `28f91e0ead7440ec798e641c22719ecf1d75ce1459699fca271ee559050ffbdd`.
The manifest pins classes, source dependencies, schema.sql and the local existing JAR.
The local JAR pin is `517b745479792a7eae6ce75357c7d1299ab644b50d5bc5b331fea384ccb10e0a`.
This only identifies the archive used to extract the driver. It does **not** establish
current source-to-JAR correspondence or match MA's current remote web candidate.
MA must resolve that identity before any later native release.

Reuse is limited to unchanged `jdbc-readiness.py` filesystem/process/env/driver primitives
and `RuntimeJdbcProbe.java` fixed URL/JDBC properties. Neither old entrypoint was run.
Their pins are respectively `13e2d084bef0e2bf1c6cb9ff64ee0dbae9650b40ccaa1091afc8b75365670051`
and `e2cd662de58fc10945e61e10cd497e2573b82eb8b6b074174c5ea2d4a2a3a8d5`.
Driver pin: `0353648eaa1c91e0f4020c959abf756bc866ffd583df22ae6b6f6e0cbd43eb44`.
Do not import an old probe approval, freshness claim or success status into this operation.

## Implementation Contract

| Pointer | Contract |
|---|---|
| `feature-provisioning.py:26`, `RuntimeFeatureProvisioner.java:21` | Both `NATIVE_RELEASED` constants remain false; no CLI flag bypasses them. Private reads and JDBC entry are blocked. |
| `feature-provisioning.py:62` | Exact JSON keys, no duplicates, 8 KiB bound, exact REQ/WI/operation, 15-minute expiry, positive signed-long principal and exact boolean attestations. |
| `feature-provisioning.py:94`, `RuntimeFeatureProvisioner.java:79` | AFP1 length-prefixed ASCII, fixed field bounds, canonical numeric/hash grammar, exact EOF; secrets never in argv. |
| `feature-provisioning.py:122,192` | Durable exclusive permanent claim before mutation; 0600 private files and protected ancestry via existing primitives; no overwrite, cleanup or automatic retry. |
| `RuntimeFeatureProvisioner.java:148` | Fixed loopback/catalog/current user, MySQL 8.0.46/port, server UUID hash, SERIALIZABLE/read-only state, two InnoDB tables, zero relevant triggers and approved metadata hash. |
| `RuntimeFeatureProvisioner.java:165` | Full-primary-index no-ADMIN locking read plus designated user lock; verified, not deleted, refresh token NULL; includes historical deleted ADMINs in refusal. |
| `RuntimeFeatureProvisioner.java:28,31` | Prepared conditional single USER-to-ADMIN update and single USER_ROLE_CHANGE/USER/SUCCEEDED audit, null actor, ROLE_CHANGED, fixed FIRST_ADMIN_BOOTSTRAP reason plus nonce, minimal JSON role/deletion states. |
| `feature-provisioning.py:132,235` | ALREADY_APPLIED only with matching committed private receipt, original claim, current principal/state and exact audit; a claim without receipt requires read-only reconciliation. |
| `fixtures.py:28,50` | Integer PCM synthesis, explicit empty output directory, fixed names, no links/reparse ancestry, traversal and overwrite refusal; no server/upload/FFmpeg invocation. |

`metadata_sha256` is a private approved digest over bounded ordered table engine/collation,
column and index rows for the two affected tables, using the framing in `metadataHash()`.
It is not a complete schema audit or a replacement for source/constraint review.
`principal_sha256` matches SHA2 of the exact stored email; it remains private, as do the
internal user/audit IDs and designation digest. No identity or credential was supplied
to this SE. Approval/receipt content was exercised only with synthetic test values.

Stopped application, fenced writers, backup/recovery boundary, no capture and independent
review/concurrency evidence are required MA attestations, not remotely observed facts.
The wrapper is not a service manager or firewall tool. Driver connect/socket deadlines
are five seconds, statements three seconds, child process 45 seconds, output 128 bytes.
Unknown/ambiguous COMMIT, close or receipt publication failure cannot return success or
retry; the consumed claim remains. Inspect can reconcile but never repairs state.

## Commands And Results

```powershell
python -B scripts/deployment/test_feature_provisioning.py
python -B scripts/validation/feature-trial/test_fixtures.py
javac --release 17 -encoding UTF-8 -Xlint:all -d build/WI063-harness-final scripts/deployment/RuntimeJdbcProbe.java scripts/deployment/RuntimeFeatureProvisioner.java scripts/deployment/RuntimeFeatureProvisionerHarness.java
java -cp build/WI063-harness-final RuntimeFeatureProvisionerHarness
python -B scripts/deployment/feature-provisioning.py prepare --output build/WI063-provisioner-final
python -B scripts/deployment/feature-provisioning.py plan --output build/WI063-provisioner-final
python -B scripts/validation/feature-trial/fixtures.py --output build/WI063-fixtures-final
```

For independent reruns use fresh `build/WI063-*` output names for compilation/preparation
and fixtures. Never delete/overwrite previous evidence to reuse these commands.

| Verification | Result |
|---|---|
| Wrapper unittest | 12 tests passed; subcases cover approval scope/time/identity/checks, exact framing/status, exclusive claims, replay/drift, no retry after uncertain commit or receipt failure, dormant-native refusal and redaction. Native flow tests replace filesystem/OS/process/credential operations with mocks. |
| Fixture unittest | 7 tests discovered: 6 passed, 1 skipped because Windows symlink privilege was unavailable. Synthetic reparse-attribute rejection passed. |
| Java compiler | Exit 0 with `--release 17 -Xlint:all`; no compiler diagnostics. |
| Java JDBC harness | 52 cases passed; no driver loaded and no connection opened. Mock SQL/locking/rollback ordering is not real MySQL atomicity or concurrency proof. |
| Offline prepare | Exit 0, PREPARED/native NOT_RUN, exact final bundle above. |
| Offline plan | Exit 0, PLAN_ONLY/native NOT_RUN; final source/class/driver pins still match. |
| Fixture generation | Two 10-second stereo 44.1 kHz/16-bit PCM WAVs, 1,764,044 bytes each; hashes and full decoded 1,764,000-byte PCM payload verified in fixture tests. |
| Scoped document/whitespace checks | Both WI063 reports pass existing validator Tier 0/link/traceability checks when its file enumeration is restricted to those two files. All nine new text files pass `git diff --no-index --check -- NUL <path>` (exit 1 for a new-file diff is expected; no diagnostics). |
| Coverage | Numeric line/branch coverage NOT MEASURED; do not claim policy coverage thresholds or independent acceptance. |

Initial fixture and wrapper red tests failed because the not-yet-implemented modules
were absent; both became green after implementation. SE review also tightened successful
Java status handling to the requested operation (a fresh promotion cannot accept READY).
No test failures remain; the Windows real-symlink test remains explicitly skipped.

The documentation validator does not implement `--help`; that invocation ran the full
read-only validator and exited 1 with six out-of-scope link errors. They are in
`deploy/single-vps/runbooks/first-host-setup.md:175`, `WI-20261011-ATS-021-handoff.md:48`,
`build/WI046-quarantine-recovery/README.md:130`, the WI048 continuation README files
at line 144/140, and `build/WI024-private-start/history/v1/README.md:253`.
These existing files were not changed. The first imported scoped-validator call hit
Windows cp949 output encoding before checking links; rerunning with `python -B -X utf8`
passed. No validator code or global configuration was modified.

Generated pair under `build/WI063-fixtures-final/`:

| File | SHA-256 |
|---|---|
| `WI063-tone-a.wav` | `d7b1011a45c84eb0c0d0308cc744289aa277b8b43ff0a4b97d6c59a1254ffa13` |
| `WI063-tone-b.wav` | `8d8d993e59a8ea7c80d5b06d4a9560e66c677635cf87728542bc9d13ead948df` |

Titles/BPM/tonality are synthetic fixture labels, not music-analysis results. App upload,
READY processing, 128 kbps stream, activation, listening and Official Download were NOT RUN.

## Additional Focused Service Verification

The user's follow-up explicitly authorized a new isolated init and existing focused
Mockito/static-contract tests. `build/WI063-focused-20261011/tests.init.gradle` redirects
all build output and sets the test JVM environment to only SystemRoot/WINDIR/TEMP/TMP/PATH,
with a pre-test allowlist assertion. No old verification source directory is added.
No Spring context, application, DB or real mail transport is started by the five suites.
This is test-process environment isolation, not an OS network sandbox.

```powershell
.\gradlew.bat --no-daemon --project-cache-dir build/WI063-focused-20261011/cache --init-script build/WI063-focused-20261011/tests.init.gradle test --tests com.atstudio.atstudio.service.EmailServiceTest --tests com.atstudio.atstudio.service.UserServiceTest --tests com.atstudio.atstudio.service.auth.AuthServiceTest --tests com.atstudio.atstudio.service.DownloadServiceTest --tests com.atstudio.atstudio.service.DownloadConcurrencyContractTest --console=plain
```

BUILD SUCCESSFUL in 48 seconds, five tasks executed. XML was parsed from
`build/WI063-focused-20261011/results/test-results/test/TEST-*.xml`:

| XML class suffix | Tests | Failures | Errors | Skipped |
|---|---:|---:|---:|---:|
| `service.EmailServiceTest` | 15 | 0 | 0 | 0 |
| `service.UserServiceTest` | 74 | 0 | 0 | 0 |
| `service.auth.AuthServiceTest` | 17 | 0 | 0 | 0 |
| `service.DownloadServiceTest` | 10 | 0 | 0 | 0 |
| `service.DownloadConcurrencyContractTest` | 3 | 0 | 0 | 0 |
| **Total** | **119** | **0** | **0** | **0** |

Compilation emitted existing deprecation/unchecked test-source notes and the standard
Mockito/bootstrap-classpath VM sharing warning. No application source was edited to
address these unrelated notes. No `clean`, Boot JAR build or previous-result overwrite.
The static concurrency-contract tests inspect source/annotations; they do not exercise
simultaneous transactions or prove download correctness on the native target.

## Risks, Rollback And Next Executable Gate

1. **MA: create and delegate a same-byte PG/RE review WI now**, attaching this pack,
   the seven-file pin table and `build/WI063-provisioner-final/artifact.json`. Reviewer
   can reproduce the Python tests and compile/run the in-memory harness in fresh
   `build/WI063-*` directories without credentials or DB access. Revalidate all hashes.
2. **After review, separately authorize an exact disposable MySQL proof target.** A new
   test harness should call `transact()` directly on that isolated instance, keeping
   both native entrypoint gates false. Its fixed target contract requires isolation of
   the loopback/schema/user namespace; do not repurpose or rename any existing DB.
   Test two independent connections/different designated USERs racing from zero ADMIN:
   exactly one promotion and one audit may commit. Also prove lock timeout/rollback,
   audit insertion failure, affected-row drift, COMMIT uncertainty, receipt failure
   and zero-write replay. Inspect metadata visibility, constraints and lock execution
   plans. No disposable DB/schema creation is authorized to this SE.
3. **Only then MA may propose a separately reviewed release with new pins**, current
   source/artifact/schema identity, fenced writers, recoverable prechange boundary,
   privately designated verified/logged-out USER and fresh expiring one-use approval.
   Neither changing a boolean nor checking an approval box alone satisfies that gate.

Local rollback is simply not invoking these additive files; preserve them and the build
evidence. No DB rollback was performed or is needed for this local work. After any future
uncertain attempt retain claim, receipt and audit, keep writers fenced and reconcile
read-only; do not delete claims, auto-demote ADMIN or retry a mutation as cleanup.
REQ007 stays active. WI063 hands off to independent review and then MA's separately gated
feature trial; WI060 diagnostics and WI061 review remain unrelated ownership.

## Reference Documents (Tier 0-2)

Tier 0 STD-001/004/002/005 was supplied in the delegation and confirmed loaded.
Input pointers were followed under the generated WI063 handoff; rule source is
`.claude/config/context-injection-rules.json`, assignee SE, ATS project.

| Tier | Documents | Use |
|---|---|---|
| 0 | `docs/standards/core-principles.md`, `documentation-standards.md`, `development-standards.md`, `glossary.md` | Delegated scope, approval, code and document standards, canonical terms |
| 1 | `docs/policies/security-policy.md`, `access-control-policy.md`, `quality-gates.md` | Least privilege, no sensitive diagnostics, independent review boundary |
| 1 | `docs/standards/evidence-pack-standard.md` | Reproduction, evidence owner and rollback |
| 2 | `.agents/skills/test/SKILL.md`, `.agents/skills/create-wi-evidence-pack/SKILL.md`, `.agents/skills/validate-docs/SKILL.md` | Scoped tests, this Evidence Pack and validation |
| 2 | `docs/templates/eval-report-template.md`, `docs/templates/wi-subagent-handoff-template.md` | Evaluation and result contract |

Reuse review chose small existing primitives without extending old operational tools.
The useful regression lesson is to bind successful child statuses to operation/receipt
state, not merely to an allowlist of individually valid statuses. No global policy,
retrospective, index, memory or workboard change is made by this bounded sidecar.

## Related Documents

- [WI063 Handoff](WI-20261011-ATS-063-handoff.md): Ownership and output contract.
- [WI059 Design](WI-20261011-ATS-059-evidence-pack.md): Provisioning and fixture constraints.
- [REQ007](../user/REQ-20261011-ATS-007.md): Approved local preparation, current MA gates.
- [WI063 Summary](../user/WI-20261011-ATS-063-summary.md): Concise result and next gate.
