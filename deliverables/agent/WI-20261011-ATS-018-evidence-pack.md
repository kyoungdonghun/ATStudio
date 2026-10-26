---
version: 1.0
last_updated: 2026-10-11
project: ATS
owner: PG
category: evidence-pack
status: confirmed
dependencies:
  - path: WI-20261011-ATS-018-handoff.md
    reason: Approved independent review and file ownership
  - path: ../user/REQ-20261011-ATS-002.md
    reason: Local-only scope and WI chain
  - path: WI-20261011-ATS-016-evidence-pack.md
    reason: Implementation identity and attributed Java evidence
  - path: WI-20261011-ATS-017-evidence-pack.md
    reason: Candidate and original static-check evidence
---

# Evidence Pack: WI-20261011-ATS-018

> Purpose: Record independent source review and adversarial candidate-validation findings without starting the application.

## Summary

**LOCAL VALIDATION HOLD: two scoped P2 findings, eight surviving unsafe
mutations. WI020 is not cleared by this review.** The unchanged WI017 suite
passes 12/12; PG's independent red-team suite returns 19 PASS / 8 FAIL / 0 ERROR /
0 SKIP across 27 cases. A failing rejection test means the original twelve
assertions accepted a deliberately unsafe in-memory candidate.

No defect was found in the exact WI016 source's intended automatic-work
suppression or in the reviewed original candidate values. This is a candidate
validation defect, not evidence that those mutations exist in the actual unit,
that any command executed, or that a remote system was compromised. The
unconditional false gate remains intact. Linux/runtime proof remains separately
NOT RUN and is not the reason for this local HOLD.

## Scope / DoD Check

- [x] Actual WI018 handoff and approved REQ read; four Tier0 documents fully injected.
- [x] Source entry inventory independently checked, including manual/event boundaries.
- [x] Strict storage, integrity, JWT and billing-keyring protections inspected.
- [x] Original candidate, precedence assumptions, finite cleanup and relay limits reviewed.
- [x] Entire original test suite exercised with in-memory adversarial candidates.
- [x] Exact source/candidate/test hashes and historical artifact pins recorded.
- [x] Both PG deliverables and bounded reproduction supplied.
- [ ] Candidate validation closes both findings and rejects all eight mutations.
- [ ] PG clearance for WI020; must follow owner correction and independent rerun.

## Findings and Required Owner Action

### PG018-01 (P2): Duplicate directives disappear in the static parser

Evidence: `deploy/single-vps/first-start/test_candidate.py:28` uses
`ConfigParser(interpolation=None, strict=False)`; its last-value projection
discards earlier occurrences. Assertions at lines75,141 and155 therefore do not
inspect the complete input. Four independently reproduced mutations survive:

| Mutation | In-memory input alteration | Result |
|---|---|---|
| additive_allowlist | Prepend `IPAddressAllow=0.0.0.0/0 ::/0` before the expected loopback line | All original 12 tests PASS |
| additive_writes | Prepend `ReadWritePaths=/etc` before the expected three paths | All original 12 tests PASS |
| prestart_command | Prepend `ExecStartPre=/bin/sh -c synthetic_never_execute` before `/usr/bin/false` | All original 12 tests PASS |
| extra_envfile | Append `EnvironmentFile=/tmp/unreviewed.env` after the public overlay | All original 12 tests PASS |

These directives cannot safely be treated as interchangeable last-wins scalars.
In particular the test's false-gate assertion does not establish that no earlier
prestart command exists. The two legitimate EnvironmentFile entries already
require ordered repeated-value handling, not a blanket ban on all repetition.

**SA action:** use an occurrence-preserving parser for the deliberately restricted
candidate grammar, or reject all unapproved duplicate directives before scalar
parsing. Require the exact ordered two EnvironmentFiles, exactly one false
prestart command, exact IP/write lists, and no unreviewed command hooks.
Do not implement a complete remote operator or alter the application for this
fix. Keep native syntax and effective drop-in/BPF validation as future gates.

### PG018-02 (P2): Executable, SMTP host and storage roots are not asserted

Evidence: `test_candidate.py:36` parses only Spring arguments;
`test_exact_new_artifact_placeholder_no_old_fallback:80` checks the JAR token
but not the executable/prefix. `assert_off_contract:48` checks SMTP port but
not host. Guard checks at124 assert root-policy booleans, not actual roots.
Four independently reproduced mutations survive:

| Mutation | In-memory replacement | Result |
|---|---|---|
| java_wrapper | `ExecStart=/tmp/unreviewed-java ...` | All original 12 tests PASS |
| privileged_exec | `ExecStart=+/usr/bin/java ...` | All original 12 tests PASS |
| external_smtp | `--spring.mail.host=smtp.example.invalid` | All original 12 tests PASS |
| public_root | `--app.storage.public-path=/tmp/unreviewed` | All original 12 tests PASS |

These are missing contract assertions, not proof that the original candidate
makes external connections. A synthetic hostname was only parsed, never resolved.

**SA action:** require the exact unprefixed Java executable/argument shape,
literal numeric SMTP host and port, and exact reviewed public/private/temp
paths. Reject shell wrappers, privilege prefixes, expansion and unexpected
command hooks. Add regression cases for the eight survivors; rerun the original
suite plus PG reproduction, then provide updated hashes for independent review.

## Source and Candidate Review

Java prefix below: `src/main/java/com/atstudio/atstudio/`.

| Entry / pointer | Independent assessment |
|---|---|
| `config/BackgroundTasksConfiguration.java:14` | Static BeanFactoryPostProcessor rejects typed scheduled processors when master is off; only nested conditional configuration enables scheduling at28. No unconditional application scheduling annotation remains. |
| `config/BackgroundTasksEnabledCondition.java:16` | Missing defaults true; case-insensitive true/false only; empty/other/unresolved values reject. No secret values in the error. |
| `service/audio/TrackAudioProcessingWorker.java:29` | Master condition AND legacy flag precede bean construction, so disabled candidate creates neither worker nor its private executor at40. No other production caller requires this bean. |
| `service/storage/StorageMutationRecoveryService.java:39` | Ready-event returns before journal work when off; manual recoverBatch at47 is retained. Scheduled registration is independently disabled. |
| `service/SubscriptionScheduler.java:58,63,84`; `PaymentReconciliationService.java:81`; `WithdrawalBillingCleanupCoordinator.java:31` | Six total Spring scheduled entries including storage recovery; service beans and explicit manual methods retained. |
| `WithdrawalBillingCleanupCoordinator.java:19`; `PaymentReceiptEvidenceService.java:56` | Transaction AFTER_COMMIT listeners remain. They are not automatic startup jobs; request-driven Provider/DB work remains possible and excluded by the future runbook. |
| `bootstrap/AcceptanceSubscriptionPlanBootstrapRunner.java:24`; `TestUserBootstrapRunner.java` | Separate acceptance/test-user controls remain; candidate explicitly disables both and selects prod. Master flag alone is not a bootstrap fence. |
| `config/AcceptanceStartupGuard.java:68` | BillingKeyCrypto validation precedes ordinary-mode early return. Blank Provider keys do not bypass encryption configuration. |
| `config/StorageIntegrityStartupGuard.java:32`; `config/JwtConfig.java:23`; `service/storage/LocalStorageService.java:63` | Integrity, JWT validation and explicit disjoint-root initialization remain independent of the master flag. Root creation is a permitted-by-code local write, not suppressed by OFF. |
| Candidate unit:14-18 | Exactly two ordered EnvironmentFiles, injection-removal list, literal false prestart, direct Java invocation and unresolved new-JAR placeholder in actual reviewed bytes. No shell/source operation. |
| Candidate unit:19-75 | Canonical CLI settings pin config location, prod, loopback, OFF flags, strict roots/audit, DDL validate, SQL init never, numeric SMTP, empty Provider credentials. Secrets/keyring are not invented or put on CLI. |
| Candidate unit:76-85,102-103 | No restart; finite180s lifetime,30s stop, cgroup cleanup, only loopback IP allowances. These are static values, not proven Linux behavior. |

Source inventory command covered Scheduled/EnableScheduling, event/transaction
listeners, PostConstruct/PreDestroy, runners, ApplicationListener,
InitializingBean, SmartLifecycle, Executors/new Thread, Async, CompletableFuture
and Timer. Only the documented automatic source entries were found. This is
not a generic sandbox for future code, third-party initialization or arbitrary
custom property sources. Scheduler guard type inspection is not a universal
defense against every possible late/dynamic custom scheduler registration.

Read the actual new focused Java test source: isolated contexts remove real
system properties/environment and mock side effects; six schedules/defaults,
invalid values, accidental registration, audio exclusion, manual methods and
mandatory guards are covered. SE's68 tests are attributed evidence, **not
rerun or counted as PG tests**. Before final delivery PG read the newly completed
WI019 Evidence Pack: RE reports87/87 PASS (original68 plus19 new synthetic
cases), matching seven pins and no source defects. That clears the attributed
Java regression gate; it does not cover the two Python candidate-validation
findings above. WI020 still awaits their local closure, not a remote operator.

Ordinary Spring CLI precedence is the design assumption, not proved by Python's
dictionary merge. Unknown environment keys, profile groups, mail JNDI/session
settings, custom logging/APM, JDBC alternatives and effective unit/drop-ins need
the future secret-safe preflight already required by the README. UnsetEnvironment
is not an allowlist. No private file, actual environment body, or credentials
were read to claim those gates passed.

## Test Evidence and Reproduction

Environment: Windows/PowerShell, Python3.14.3,2026-10-11; evidence owner PG.
Actual Git identity: `main`, HEAD
`58b1011dad796017e47dbcc82a4225ca449bcdf6`, pre-existing dirty worktree.

| Command | Result |
|---|---|
| `python -B deploy/single-vps/first-start/test_candidate.py` | exit0;12 tests,12 passed,0 failed/errors/skipped;0.007s |
| `python -B build/WI018-review/test_redteam.py` | exit1;27 cases,19 passed,8 failed,0 errors/skipped; first run0.281s, final run0.234s |
| `git log -1 --format="%H %D"`, scoped `rg` and `Get-Content` | Source/evidence inspected; no application or configuration edits |
| `Get-FileHash -Algorithm SHA256` over exact public paths below | WI016 seven source/test pins match SE; retained manifest/JAR pins match historical evidence |

The PG harness imports the unchanged WI017 module and replaces only reads of
the unit candidate with an in-memory mutation via unittest.mock. Each outer case
runs all original12 tests. Nested runs are not counted as independent runtime
tests. The27 outer cases comprise one baseline plus26 mutations:18 rejected,
8 accepted. Missing/true values for all six OFF flags, restart, infinite runtime,
partial child cleanup, broad replacement allowlist, public bind and disabled
strict guard are rejected. The eight surviving cases above remain ordinary
failing assertions, not suppressed/expected failures.

No synthetic command was executed and no unsafe unit or env file was written.
The test remains under ignored `build/WI018-review/`; durable findings, mutation
details and exact hash are recorded here. Keep this local reproduction available
for the SA correction and PG rerun. Coverage thresholds/performance/capacity
are not measured; no full-suite or target assurance is implied.

## Exact Reviewed Hashes

SHA-256 binds dirty-worktree bytes, not a new complete release manifest.

| Path | SHA-256 |
|---|---|
| `src/main/java/com/atstudio/atstudio/AtStudioApplication.java` | `8b9e8bba491dab5d8ae999c37c6ed93c7dd2c7740470b06ac8c65ba8ac018ae8` |
| `src/main/java/com/atstudio/atstudio/config/BackgroundTasksConfiguration.java` | `bfd71bf159c7d3fc91d1f6fbfda72d01120e73a8bdeb855958bf65f7c83993b2` |
| `src/main/java/com/atstudio/atstudio/config/BackgroundTasksEnabledCondition.java` | `0562767a065abefe6496b9a9065e5f7cf0188875ce7d93541c6b8eeb4f50259b` |
| `src/main/java/com/atstudio/atstudio/service/audio/TrackAudioProcessingWorker.java` | `dfa81ed8bfed5338de9be1976f9f16d2fdce97164977591bf8bbc3558c8fe5b9` |
| `src/main/java/com/atstudio/atstudio/service/storage/StorageMutationRecoveryService.java` | `b72d7d07840b09473c9c7e3d53925bf04b861d6e87be3a8e5fc6fe64af1bb575` |
| `src/main/resources/application.yml` | `c3bd0e1571ffbe9e35ec5b77e6197fb91ca9baa602fb952609de4c07efd4bf55` |
| `src/test/java/com/atstudio/atstudio/config/BackgroundTasksConfigurationTest.java` | `a3321d53afe3771cf257db3ab238b0ad7c1663c0b525c2258704f0cd75939cbf` |
| `src/main/java/com/atstudio/atstudio/config/AcceptanceStartupGuard.java` | `1e6c106857526f70e1a80458eecf982e8c2231ccf3b983509f8e6198e5da63bd` |
| `src/main/java/com/atstudio/atstudio/config/StorageIntegrityStartupGuard.java` | `21aa147684f6408e1cc8790e71303fe115000b4b07d61853fdea3c455bf2edd4` |
| `src/main/java/com/atstudio/atstudio/service/storage/LocalStorageService.java` | `191fbfab92f5e554e53457b6716029d8334cf6c756e5885d4d06e3cf6c2e50dd` |
| `deploy/single-vps/first-start/atstudio-first-start.service.candidate` | `09d691d4b0c90813d7fdc6922cb15b11c0f58fb162d588cf8d88019c1da7941d` |
| `deploy/single-vps/first-start/first-start-public.env.example` | `624a45c6c14d8d0e665bb4c7988f7834b33463ffa2aa5470678c4bc128a24fe6` |
| `deploy/single-vps/first-start/test_candidate.py` | `0ac8e5d94d14ded2b6da8d861aeaf269400d94f75ba078022d1e5c51f46f75db` |
| `deploy/single-vps/first-start/README.md` | `5b72d08383bea126c78789d85b02277ca373dac71a07e758d8c8de9f227f0388` |
| `deploy/single-vps/first-start/inventory.md` | `dad6059b727d438a4993b245446e32054e9e448239cc175e8a9b03662b758c49` |
| `deploy/single-vps/systemd/atstudio.service` | `118805e388b24e013148a796aed41b70f596ada18ff627f8417b6b7b6262e8d1` |
| `deliverables/agent/WI-20261010-ATS-026-artifact.json` | `fbeb74425ade0fe3882a9dddc4c97f7b67b75dda711ffa37acbd755a02fa4d9c` |
| `build/libs/ATStudio-0.0.1-SNAPSHOT.jar` | `517b745479792a7eae6ce75357c7d1299ab644b50d5bc5b331fea384ccb10e0a` |
| `build/WI018-review/test_redteam.py` | `f1757259863c02254d00e806884e62b93aaf122f7e70d36e408f3f5af18f3888` |

The retained JAR and WI026 manifest match the supplied historical pins; no
new JAR was built. Hashes do not establish source-to-running-artifact identity.

Final rehash returned PIN_MATCH19/19. Direct checks of both owned Markdown
files and the synthetic test passed whitespace/EOF; both reports passed required
metadata and relative link-target existence checks. Scoped git diff --check
returned exit0; direct checks were necessary because the reports are untracked.
These are not whole-repository documentation or link-anchor checks.

## Reference Documents (Tier 0-2)

| Tier | Document / input | Use |
|---|---|---|
| 0 | core-principles STD-001; documentation-standards STD-004; development-standards STD-002; glossary STD-005 | Fully injected by MA in supplied order |
| 1 | PG persona; security-policy | Fully injected; private-data, fail-closed and scoped review boundaries |
| 1 | quality-gates | Read actual local document |
| 2 | WI018 handoff; approved REQ002; WI016/017 evidence | Exact authority, ownership and source/candidate claims |
| 2 | docs/index.md latest Current Decision Entry; WI015 evidence | Latest native read-only schema PASS and restored baseline attributed to MA, not PG runtime checks |
| 2 | CONFIGURATION.md; production-readiness.md | Relevant public mappings/guards; dated readiness superseded by latest index |
| 2 | WI026 artifact manifest | Historical SHA-256 preserved |
| Skills | test; create-wi-evidence-pack | Both read; actual handoff precondition passed; Python scoped harness instead of duplicating WI019 Gradle work |
| Context | .claude/config/workspace.json | ATS tag confirmed |

Injection order: Tier0 -> PG/security -> handoff/evidence. Rule-source pointer:
`.claude/config/context-injection-rules.json`; assignee PG; task security/review.
Memory was a navigation hint only; current source and dated evidence governed
all conclusions. No sub-delegation or cross-chat dispatch occurred.

## Remaining Runtime Risks (Not Local Finding Closure Requirements)

- Keep the literal false gate until separately approved same-unit fail-closed
  prestart proof exists. Native systemd syntax/effective cgroup/BPF controls,
  inherited allow rules and actual IPv4/IPv6 denial remain NOT RUN.
- Positive controlled external TCP/UDP observations, loopback DB/HTTP reachability
  and absence of SMTP/DNS/HTTP/SOCKS/Unix relays are essential future gates.
  Loopback IP rules do not restrict ports or AF_UNIX; local relays can escape.
- Off flags do not prohibit request-triggered writes, local callers, directory/
  temp/log creation, connection-pool activity or MySQL engine writes.
- Finite timeouts are not proof of readiness or successful termination of a
  kernel-stuck child. Exact cgroup/port/job cleanup must be observed.
- Future baseline restoration must return MySQL inactive/persistently masked,
  app absent with no DB/app listeners/jobs, and TLS/Certbot unchanged. WI015 is
  dated MA evidence, not a fresh target observation from this review.
- No secret-use approval, server contact, whole-app start, DB service, network
  probe, Provider/SMTP/IPv6 request, install, purchase, deletion, commit or push
  occurred. No production or supported-user claim.

## Rollback and WI Chain

PG changed only this Evidence Pack, its user summary and the synthetic test.
No application/configuration rollback is needed. Retain/supersede PG evidence;
deletion would need explicit approval. Do not revert unrelated dirty work,
historical manifests, old JARs or other agents' files.

**MA next action:** return the two scoped validation findings to WI017/SA under
the existing approved local scope; obtain corrected test evidence/hashes and
PG rerun. No remote operator expansion is required. WI018 remains uncleared;
do not dispatch WI020 based on the original12 PASS. After PG closure AND WI019
regression clearance, WI020 packaging is the immediate successor, then WI021.
REQ002 remains open. No sub-delegation was authorized or performed.

## Related Documents

- [WI018 handoff](WI-20261011-ATS-018-handoff.md): Scope and output contract.
- [PG summary](../user/WI-20261011-ATS-018-summary.md): Compact findings.
- [REQ002](../user/REQ-20261011-ATS-002.md): Approved scope and WI chain.
- [Candidate runbook](../../deploy/single-vps/first-start/README.md): Future proof gates.

## 2026-10-11 Independent Repair Retest: Local PASS

**Superseding verdict: bounded LOCAL candidate-validation PASS. PG018-01 and
PG018-02 are CLOSED for the exact repaired validator below. WI018 clears WI020
local packaging together with the already completed WI019 87/87 regression.**
The original HOLD, failed19/27 result and historical hashes above are preserved;
they describe the earlier validator and are not the current clearance status.

### Authority and Scope

User approved the validator repair and re-review. PG read the latest WI017
Approved Validation Repair Addendum and WI018 Approved Retest Addendum. The
previously loaded test and create-wi-evidence-pack skills and injected Tier0
remain in force; the actual WI018 handoff exists. No new audit or remote
operator was introduced. PG reviewed only the repaired checker, reran the two
bounded Python suites, and rechecked the existing public pins. Java87 was not
rerun and is not added to PG's test count.

Verification: PG, Windows/PowerShell, Python3.14.3,2026-10-11 around11:18 KST;
branch main, HEAD58b1011dad796017e47dbcc82a4225ca449bcdf6. Existing dirty work
remains intact. Only this addendum and the PG summary addendum were written.

### Finding Closure Review

| Finding | Reviewed implementation | Independent result |
|---|---|---|
| PG018-01 | `test_candidate.py:86` retains ordered occurrences; `:126` checks exact sections/directives, two ordered EnvironmentFiles, singleton counts and literal false prestart before scalar projection | Duplicate/reset/additive rules, extra env file and prestart commands rejected; all four former survivors rejected |
| PG018-02 | `test_candidate.py:151` requires exact unprefixed Java/JVM/JAR prefix and complete fixed CLI map; `:170` checks SMTP and filesystem targets | Wrapper/privilege prefix, external SMTP and root changes rejected; all four former survivors rejected |

Expanded owner regression methods at307,313,323,334,344,359,370 exercise
occurrence order, every singleton duplicate/reset, env-file order/count, hooks,
executable/argument drift, target paths and ambiguous grammar. Review found no
new scoped blocker. The parser intentionally accepts only the candidate subset;
this result does not claim full systemd grammar equivalence or protect a future
unreviewed rendered unit/drop-in automatically.

### Independent Commands and Results

| Command | PG result |
|---|---|
| `python -B deploy/single-vps/first-start/test_candidate.py` | exit0;19 tests,19 PASS,0 failures/errors/skips;0.054s |
| `python -B build/WI018-review/test_redteam.py` | exit0;27 cases,27 PASS,0 failures/errors/skips;0.333s |
| `Get-FileHash -Algorithm SHA256` against the original19-row pin table, replacing only the checker expectation |19/19 match: repaired checker exact; other18 inputs unchanged |

The PG harness is byte-for-byte unchanged. Its27 cases comprise the accepted
original candidate and26 rejected mutations, including all8 earlier survivors.
Each outer case loads the current owner suite; nested methods/subtests are not
additional independent tests. Malformed candidates can reject during class
setup; source review confirmed the restricted validation paths are responsible,
and the unmodified candidate separately passes all19 owner methods.

| Item | SHA-256 |
|---|---|
| Repaired `test_candidate.py` | `43f48bf958cbb9221e70fe66af28caad128a4aafb5350e9ab8f566c5c0d535a5` |
| Unchanged PG `test_redteam.py` | `f1757259863c02254d00e806884e62b93aaf122f7e70d36e408f3f5af18f3888` |
| Unchanged first-start unit | `09d691d4b0c90813d7fdc6922cb15b11c0f58fb162d588cf8d88019c1da7941d` |
| Unchanged first-start public env | `624a45c6c14d8d0e665bb4c7988f7834b33463ffa2aa5470678c4bc128a24fe6` |

All other18 pins in the earlier exact-reviewed table remain authoritative,
including README/inventory, six application implementation/configuration paths,
focused Java test, three guard/storage sources, baseline unit, historical JAR
and WI026 manifest, and PG harness. No original candidate or Java source changed.

### Remaining Boundaries and Chain

The false prestart gate and unresolved new-JAR placeholder remain. Native Linux
syntax, effective BPF/IPv4/IPv6/relay enforcement, actual Spring binding, real
secret use, app/DB startup and cleanup are NOT RUN and need separate approval.
These intended future gates do not block this bounded local packaging verdict.
No runtime, DB, network/Provider/SMTP probe, install, purchase, deletion, commit
or push occurred. No production or capacity clearance is implied.

**Immediate successor: MA may dispatch WI020 under the existing approved local
REQ scope, using WI018 PASS plus WI019 87/87. WI021 follows packaging.** No
sub-delegation was performed by PG and REQ002 is not closed here. If the checker
is reverted or candidate bytes change, re-review the affected scope; do not
reuse this exact-hash clearance. Preserve both the original HOLD and this
superseding result. No rollback operation was performed.

