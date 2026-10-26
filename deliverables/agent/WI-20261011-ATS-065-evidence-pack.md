---
version: 1.1
last_updated: 2026-10-11
project: ATS
owner: PG
category: evidence-pack
status: confirmed
related_wi: WI-20261011-ATS-065
dependencies:
  - path: WI-20261011-ATS-065-handoff.md
    reason: Independent review ownership and separate slice verdicts
  - path: WI-20261011-ATS-063-evidence-pack.md
    reason: Candidate source, artifact identity and prior test claims
  - path: WI-20261011-ATS-064-handoff.md
    reason: Audio source ownership and acceptance contract
  - path: WI-20261011-ATS-064-evidence-pack.md
    reason: Final applied audio source and local verification identity
  - path: ../user/REQ-20261011-ATS-007.md
    reason: Approved local preparation and MA-only native boundary
---

# Evidence Pack: WI-20261011-ATS-065

## Summary

**WI064: ALLOW_SOURCE_FOR_WI068_BUILD. WI063 helper and fixtures:
ALLOW_LOCAL_REVIEW_ONLY. Native feature release: HOLD.** No new blocking defect
was found in either reviewed slice. Both WI063 native-release constants remain
false. WI065 source review is complete; MA may advance the separately owned
WI068 build using the exact source identity below.

PG independently executed the local checks below on the actual pinned files.
This is neither a native execution approval nor MySQL concurrency proof.
WI061 reports were already completed and were left unchanged.

## Final Audio Source Verdict

**ALLOW_SOURCE_FOR_WI068_BUILD**, bound to the actual applied WI064 bytes.
MA supplied final source readiness and requested this independent review. PG
reviewed the applied patch, all changed predicates, related scheduling/ready
entrypoints and source-preservation evidence, then independently compiled and
executed the127 candidate tests plus20 new PG tests: **147 PASS, zero failures,
errors or skips**,12 suites. No candidate/application source was edited by PG.

| Object | SHA-256 |
| --- | --- |
| 637-file source manifest | `fb367e966e72b6ceb24de6afabde9efb74333275fe41de8aa613eb119979a58e` |
| audio-only-applied.patch | `eb7efd4f25415310e1a9c89755589f3bef530af42e4a828a73e762ef412937c9` |
| SE application-verification.json | `c9f4815ccd1874456343ea1379fdf29d3d5782dbb99c5298699174df780bffd9` |
| PG audio receipt | `bcdb9b9d967f8b75535311a67b28ae7a721f6d20a7040139c1128ff6f3813a44` |
| Equal PG before.json and after.json | `0830a15c88b9c448de4f195ecf0107e4191c643241fa72dfec1da4fcc00bd2a7` |

[Final audio receipt](../../build/WI065-review/audio-20261011T214029320234/receipt.json)
contains all eight applied raw source hashes and all12 suite counts/XML hashes.
The source manifest is SHA-256 of the sorted repository-relative path-to-hash
mapping serialized as compact ASCII JSON with separators comma/colon, no trailing
newline; its mapping is the `source` object in
[before.json](../../build/WI065-review/audio-20261011T214029320234/before.json).

Independent window: **2026-10-11T21:40:33.408669+09:00 through
21:41:37.261058+09:00**, Windows, installed Java17/Gradle9.3.0, owner PG.
All8 applied paths equal SE's frozen copies and manifest. All629 original source
files outside ownership remain unchanged, including worker/transaction algorithms
and startup security guards. The current source set is exactly the635 original
paths plus2 authorized new paths. All660 SE-captured source/build-input/retained
artifact hashes match; the complete PG before/after observations are identical.
Non-mutating reverse patch applicability and whitespace checks passed.

Changed-path conclusions:

- `BackgroundTasksEnabledCondition.java:17`: master remains authoritative;
  audio-only is strictly validated even when master=false. Missing audio-only
  defaults false; mixed-case true/false remain valid, whitespace and other
  boolean-like values fail before bean side effects.
- `ScheduledBackgroundTasksEnabledCondition.java:9` and
  `BackgroundTasksConfiguration.java:15`: scheduling is master && !audioOnly;
  the guard refuses accidental scheduler imports/post-processors before jobs
  register. All six existing schedules remain registered in legacy enabled
  mode and none in audio-only/master-off mode.
- `StorageMutationRecoveryService.java:42`: ready-event recovery is suppressed
  by either master=false or audioOnly=true. Explicit manual service methods
  remain available by design; this is not an authorization bypass or a claim
  that all mutating endpoints are disabled.
- `application.yml:86`: default-off environment mapping is the only YAML
  change. PG added actual SystemEnvironmentPropertySource plus public-YAML
  tests for nine missing/mixed-case combinations, ten malformed values across
  three master states, and accidental scheduling import.20 JUnit cases passed.
- Candidate tests independently rerun include27 master/audio/worker combinations,
  startup integrity/billing guards, storage suppression/manual compatibility,
  three worker-isolation tests and the eight-journal source-level success path.
  Neither the global audio queue nor storage/transaction algorithms were changed.

The eight-journal test verifies4 CREATE plus4 DELETE intents with retained
originals in a synthetic repository/storage model and manually driven transaction
callbacks. It does not prove actual DB rows, Spring transaction commits, WAV
decoding, FFmpeg128kbps output or target delivery. Audio-only retains a global
worker; native operation still needs zero prior queue/claim/recovery debt,
one JVM and fenced writers. No target state was checked by PG.

Reproduce into a fresh owned directory:

```powershell
python -I -B build/WI065-review/run_audio_review.py
```

The [PG runner](../../build/WI065-review/run_audio_review.py) invokes offline
Gradle with an isolated copied dependency cache, fresh user/project home,
literal nonsecret environment, a five-key test environment, and a test-only
task graph. The [init script](../../build/WI065-review/audio-tests.init.gradle)
adds only the [PG test source](../../build/WI065-review/java/com/atstudio/atstudio/config/Pg065AudioModeTest.java)
under owned build output. No JAR/package/clean/app task, private config or
credential, DB, native FFmpeg, SMTP or provider operation was run.
[Stdout](../../build/WI065-review/audio-20261011T214029320234/stdout.log) confirms
BUILD SUCCESSFUL,5 executed tasks; [stderr](../../build/WI065-review/audio-20261011T214029320234/stderr.log)
contains existing deprecated/unchecked/class-sharing warnings. Full suite,
numeric coverage and native performance were not measured. Offline Gradle is
not an OS network-isolation proof.

This verdict clears **only the independent source-review prerequisite for
WI068**. Changed source/build inputs invalidate same-byte acceptance. Native060
BOUNDED_PASS is MA-attributed old-source evidence; it is preserved, not promoted
to acceptance of the new source/JAR. MA owns the new build and later release
contract. Object Storage/restic work and future WI070 remain separate assignments.

## Helper Findings And Exact Identity

No new fix request is open for these WI063 bytes. Existing release prerequisites
remain prerequisites rather than new speculative findings: actual MySQL locking,
transaction/metadata-visibility proof, current source/artifact/target identity,
private designation, fenced writers and the recovery boundary.

| Object | SHA-256 |
| --- | --- |
| Compiled helper bundle | `28f91e0ead7440ec798e641c22719ecf1d75ce1459699fca271ee559050ffbdd` |
| feature-provisioning.py | `4c4ee98261f3f2b26df1f489c876cc24962606f9e091938ba48a9cf22b474619` |
| RuntimeFeatureProvisioner.java | `ada1ad5ed941b1d578898a1b817b581ff6df6660b868494b16fc88aeb546dd57` |
| fixtures.py | `5ee908c1e52901d7e4459ad69dd0024b784a9595138cc747b54ffd1a26dfb857` |
| PG local receipt | `d008fbf858e47b17d12292ece08645eea48539a5b26490448c8612288f21cccc` |

[PG receipt](../../build/WI065-review/run-20261011T213249693820/receipt.json)
contains pre/post source, class, schema, JAR and driver identities; command,
log and reviewer-test hashes; timestamps; and separate execution limits.
The six candidate source/test pins matched the SE table before and after tests.
All four freshly compiled helper classes matched the existing artifact manifest.

Source checkout: `main`, HEAD `58b1011dad796017e47dbcc82a4225ca449bcdf6`, dirty
shared worktree. PG did not change candidate, application or historical files.
Verification owner PG; local Windows/Python3.14.3/Java17 compilation, observed
2026-10-11 at approximately21:32 KST. Precise times are in the receipt.

The manifest's JAR pin is
`517b745479792a7eae6ce75357c7d1299ab644b50d5bc5b331fea384ccb10e0a`.
It identifies the existing local archive used to obtain the driver, not current
application-source correspondence and not the MA native web JAR. The driver pin
is `0353648eaa1c91e0f4020c959abf756bc866ffd583df22ae6b6f6e0cbd43eb44`.
The schema pin is
`6fb7df1518394371bbfc5513df5ebe2c97e067e7f77a1f20661afb1c00b63fb7`.
No historical source/JAR acceptance was imported into the new helper verdict.

## Changed Risk Paths Reviewed

| Pointer | Result and boundary |
| --- | --- |
| `scripts/deployment/feature-provisioning.py:26,194,262`; Java:21,246 | Python and Java entry gates remain false. Actual entrypoint checks reject before private reads/driver/connection; local Java emitted only HOLD_NATIVE_GATE:0 with exit2. In-memory flow simulations temporarily patch a module variable under mocked OS/private/process boundaries; no on-disk constant changes. |
| Python:62,103,132,238 | Exact approval fields/booleans/time/hash grammar, duplicate-key rejection, numeric bounds and operation/receipt-bound status acceptance. Independent cross-product tests reject false READY/APPLIED/ALREADY_APPLIED across fresh, claimed, receipted and orphan-receipt states. |
| Python:122,192 | Exclusive permanent claim precedes child. Claim publication failure prevents child invocation; child uncertainty or receipt failure cannot return success/retry. Full claim retained after directory-sync failure; another publication refuses overwrite. Claim-only inspect stays HOLD_RECONCILE; promotion cannot reuse it. |
| Java:24,28,31,165 | Fixed full-primary-index no-ADMIN locking query precedes designated target lock. Only one conditional verified, nondeleted, logged-out USER promotion and one existing-schema audit are issued. No account creation, consent/password change, entitlement, DDL or arbitrary SQL. Actual lock coverage remains WI066 proof. |
| Java:148,206,210,227,231 | Session/target/schema metadata checks precede DML; inspect/replay roll back without mutations. COMMIT ambiguity remains HOLD_UNKNOWN even after synthetic commit effects; rollback errors cannot produce success. Close/query/update/key/commit failures were injected before and after each reachable JDBC operation. |
| `scripts/validation/feature-trial/fixtures.py:50` | Exactly two deterministic10-second stereo44.1kHz/16-bit PCM WAVs; full decode, amplitude/channel equality, fixed hashes, traversal/reparse rejection and no overwrite. Fsync failure retains partial files and a subsequent generation refuses them. No upload/server/FFmpeg invocation. |

Approval/private receipt contents were synthetic only. Real credentials, private
designation, environment files and account values were not read. Public test
outputs contain fixed statuses and synthetic labels; no raw JDBC diagnostic is
used as an operator success message. Reused subprocess primitive bounds stdout,
discards stderr and supplies an allowlisted environment; original entrypoints
from the older JDBC tool were not invoked.

## Independent Tests

Reproduction, with fresh retained outputs below the owned review directory:

```powershell
python -I -B build/WI065-review/run_review.py
```

| Group | Result |
| --- | --- |
| PG Python risk-path suite | 9 methods PASS, including24 operation/claim/receipt/success-status combinations |
| Candidate Python suites independently rerun | 19 discovered,18 PASS,1 SKIP; real Windows symlink privilege unavailable; synthetic reparse rejection PASS |
| Java17 compiler, --release17 and -Xlint:all | Exit0, no compiler diagnostics |
| Candidate JDBC contract harness independently rerun | 52 synthetic cases PASS |
| PG JDBC fault harness | 1,278 synthetic cases PASS across inspect/promotion and fresh/replay |
| Actual locked Java entrypoint | Expected exit2 and exact HOLD_NATIVE_GATE:0; no JDBC driver on classpath |
| Pre/post identity and rebuilt class identity | PASS |

Counts overlap and are not line/branch coverage. Numeric coverage NOT_MEASURED.
The PG Java harness reuses SE's synthetic result model, independently wraps every
Connection/PreparedStatement/ResultSet call, and injects SQLException before and
after each baseline operation. This proves status/flow handling against that
model, not server SQL execution, optimizer behavior, deadlock or atomicity.

The candidate Python test modules were loaded unchanged; only their test output
roots were redirected in memory into this review directory. An outer process
guard rejects any unmocked process launch while those suites run. All test data,
classes, logs and receipts remain under build/WI065-review. No app/DB/service
process, credential, provider, account, network, commit or push action was run.

Evidence:

- [PG Python tests](../../build/WI065-review/test_review.py) and [log](../../build/WI065-review/run-20261011T213249693820/pg-python.log).
- [PG JDBC fault harness](../../build/WI065-review/Pg065FaultHarness.java) and [log](../../build/WI065-review/run-20261011T213249693820/pg-java.log).
- [Candidate Python rerun](../../build/WI065-review/run-20261011T213249693820/candidate-python.log).
- [Candidate Java rerun](../../build/WI065-review/run-20261011T213249693820/candidate-java.log).
- [Compiler log](../../build/WI065-review/run-20261011T213249693820/compile.log).
- [Locked Java entry log](../../build/WI065-review/run-20261011T213249693820/native-entry-lock.log).
- [Pinned verifier](../../build/WI065-review/run_review.py).

## Scope And DoD

- [x] Read approved skill-generated handoff, current security policy and WI063 evidence.
- [x] Review first-admin DML/audit/claim/receipt/secret/status paths and fixture bounds.
- [x] Independently test scoped failures and revalidate exact source/artifact identity.
- [x] Keep native constants false and distinguish synthetic from actual MySQL proof.
- [x] Preserve candidate source, dirty application work and WI061/historical evidence.
- [x] Review final WI064 audio source/test bytes after MA supplies final-code readiness.
- [x] Complete audio slice verdict and full WI065 source-review closeout.

## Limits, Rollback And Next Chain

Native remains **HOLD / NOT_RUN**. Local ALLOW only clears the reviewed helper
and fixture source slice for the separately owned WI066 disposable proof work.
It does not authorize starting any database here, changing a release boolean,
creating a private account/password/consent or executing a native mutation.
The final audio source verdict separately clears WI068's source-review gate;
PG did not perform a JAR build. WI065 is complete; MA advances WI068 and the
separate WI066 proof chain. REQ007 remains active.

WI066 must retain both native gates false and prove actual two-connection
competition, transaction rollback/uncertainty and metadata visibility on its
separately approved disposable target. The existing exact native target,
private designation, release and recoverability gates remain outside mock
acceptance. Windows tests do not establish Linux filesystem/fsync durability.

No production rollback is needed: candidate/native files were not changed.
After any future uncertain promotion, preserve claim/audit/receipt and reconcile
read-only; never auto-retry, delete the claim or demote an account as cleanup.

## Reference Documents

Tier0 STD-001/004/002/005 remains injected and loaded. Tier1 used:
`docs/policies/security-policy.md`, `access-control-policy.md`, `quality-gates.md`,
`docs/standards/evidence-pack-standard.md`. Tier2 skills applied: `test`,
`create-wi-evidence-pack`, `validate-docs`; existing scoped WI061 document
validator was reused as a pattern in the owned review directory. Rule source
`.claude/config/context-injection-rules.json`, PG/security/review/testing, ATS.
No subdelegation or shared index/workboard/policy update was performed.

## Related Documents

- [WI065 Handoff](WI-20261011-ATS-065-handoff.md): Approved review boundaries.
- [WI063 Evidence](WI-20261011-ATS-063-evidence-pack.md): Candidate and earlier SE tests.
- [WI059 Design](WI-20261011-ATS-059-evidence-pack.md): First-admin and fixture contract.
- [WI064 Handoff](WI-20261011-ATS-064-handoff.md): Completed second-slice contract.
- [WI064 Evidence](WI-20261011-ATS-064-evidence-pack.md): Applied candidate and SE test evidence.
- [WI065 Summary](../user/WI-20261011-ATS-065-summary.md): Concise decisions.
