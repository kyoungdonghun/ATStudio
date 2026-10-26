---
version: 1.0
last_updated: 2026-10-10
project: ATS
owner: SE
category: evidence-pack
status: stable
dependencies:
  - path: WI-20261010-ATS-029-handoff.md
    reason: Skill-generated ownership and execution boundaries
  - path: ../user/REQ-20261010-ATS-009.md
    reason: Approved preparation and gated native operation
  - path: WI-20261010-ATS-028-evidence-pack.md
    reason: Existing target identity and previous cleanup
---

# Evidence Pack: WI-20261010-ATS-029

## Summary

TL;DR: **Local probe preparation PASS; native NOT RUN by SE; production HOLD.**
Standalone Java17 plus stdlib Python use only the pinned WI026 Connector/J.
No Spring/Hikari/application classpath, actual SQL, secrets or remote operations.
The user later approved this window's sensitive actions, but only MA may execute
after WI031 PG acceptance. MA's22:30:55KST SSH/host receipt is MA-attributed,
not an SE rerun or a JDBC result.

Verification owner SE, local Windows/PowerShell on2026-10-10. Branch main,
HEAD `58b1011dad796017e47dbcc82a4225ca449bcdf6`; dirty/untracked inputs preserved.
Existing v3 bootstrap helper and WI026 manifest were rehashed, not edited.

## Scope / DoD Check

- [x] Existing skill handoff and injected STD-001/004/002/005 confirmed.
- [x] Only the six owned source/runbook/two-set deliverable paths added.
- [x] Exact JAR/driver identity checked before exclusive output creation.
- [x] Java17 compilation and real-driver no-connect smoke PASS.
- [x] Synthetic negative/positive checks, bounded output and failure behavior.
- [x] Explicit native guards, cleanup, limitations and next WI documented.
- [ ] WI031 final acceptance: MA reports PG candidate synthetic checks PASS and
  F1/F2 native-procedure review; final artifact acceptance remains PG-owned.
- [ ] Native JDBC/cleanup receipts: MA/WI032, not executed here.

## Reference Documents (Tier 0-2)

| Tier | Pointer | Use |
|---|---|---|
| 0 | core-principles.md; documentation-standards.md; development-standards.md; glossary.md under docs/standards | Fully injected; language, approval, traceability and standards |
| 1 | .claude/agents/se.md; docs/policies/security-policy.md | Injected persona and secret/output rules |
| 1 | docs/policies/access-control-policy.md; quality-gates.md | Read locally; least privilege and review boundaries |
| 2 | .agents/skills/create-wi-evidence-pack/SKILL.md; test/SKILL.md; build-check/SKILL.md | Read locally; existing-handoff gate, evidence structure and bounded checks |
| 2 | docs/standards/evidence-pack-standard.md; docs/templates/eval-report-template.md | Read locally; reproduction and evaluation |
| 2 | docs/index.md Current Decision Entry; deploy/single-vps/CONFIGURATION.md; runbooks/deployment.md; runbooks/production-bootstrap.md | Current state, configuration provenance and guard/cleanup scope |
| 2 | scripts/reconstruction/README.md; docs/design/db-schema.md | Relevant sections searched; canonical manifest and Hibernate limits |
| Context | REQ009; WI025/026/028 completion evidence; WI014 | Bounded current completion rather than historical pending claims |
| Source | schema.sql; seed.sql; application.yml; env/atstudio.env.example; validate_bundle.py; production-bootstrap.py; build.gradle | Exact URL/driver,43 tables,6 plans and reuse of existing path/approval patterns |
| Conditional | react-best-practices/AGENTS.md; frontend-standards.md | Handoff pointers only; not loaded, no frontend work |

ATS tag and SE required tiers[0] checked in workspace.json and injection rules.
The handoff's remaining env contract/systemd pointers were not separately loaded:
public URL and generated grammar were verified directly in env example and
bootstrap renderer. Test/build skills were scoped to standalone javac/unittest;
Gradle/full-app tests would exceed this WI and risk replacing the exact JAR.
Memory was a navigation hint only; current WI028/source superseded older notes.

## Evidence Pointers

| File / function | Change and rationale |
|---|---|
| scripts/deployment/jdbc-readiness.py: parse_env, frame | Exact generated one-line quoting, duplicate rejection including unrelated keys, fixed URL/account,32-byte password,108-byte private frame |
| same: secure_path, read_private, parse_approval, native_java | Root ownership/ancestry, no links/hardlinks,0600/0700,descriptor identity/metadata checks; typed/time-bound exact review/bundle/target approval |
| same: prepare, load_plan | Exclusive local output; exact driver extraction; source/JAR/driver/class/artifact-manifest identity; exact class/output entry set |
| same: run_child, checked_status, native_probe | No inherited Java/integration env,128-byte output bound,stderr discard,45s deadline,kill/reap,allowlisted status,approval before env read |
| scripts/deployment/RuntimeJdbcProbe.java: main, properties, scalar | Framing before driver load,read-only server verification,fixed SELECTs,rollback/close,no raw exceptions/logging |
| scripts/deployment/test_jdbc_readiness.py |29 tests; Java fake driver compiled only in fresh test directories, no real connection |
| deploy/single-vps/runbooks/jdbc-readiness.md | Exact six-file native layout,manual capture/host/DB guards,approval and cleanup |
| deliverables/user/WI-20261010-ATS-029-summary.md | Compact user-facing outcome and remaining gates |

No generic deployment framework or imported bootstrap mutation API was added.
Small path/approval patterns match the existing validator/bootstrap conventions;
copying their bounded checks avoids loading mutation-capable helpers in this
secret-reading wrapper. Native operator guards selectively reuse old read-only
functions only, with original helper identity checked first.

## Artifact Identity

Output directory: `build/WI029-jdbc-v1`. Compiler `javac17.0.12`, Java feature17,
class-major61; Python3.14.3 local. No new dependency/install/download. Only the
exact nested `BOOT-INF/lib/mysql-connector-j-9.7.0.jar`,2607237bytes, was extracted.

| Object | SHA-256 |
|---|---|
| Approved executable JAR |517b745479792a7eae6ce75357c7d1299ab644b50d5bc5b331fea384ccb10e0a |
| Extracted Connector/J |0353648eaa1c91e0f4020c959abf756bc866ffd583df22ae6b6f6e0cbd43eb44 |
| jdbc-readiness.py |13e2d084bef0e2bf1c6cb9ff64ee0dbae9650b40ccaa1091afc8b75365670051 |
| RuntimeJdbcProbe.java |e2cd662de58fc10945e61e10cd497e2573b82eb8b6b074174c5ea2d4a2a3a8d5 |
| RuntimeJdbcProbe.class |452a9b652b88ee1e19c5001586b8ccbe9725087f96d4828603bdd1c82af1c493 |
| artifact.json |a356405db6372484a0bb0bfcd5491c5b60bfc2e9f2332eb426f13a7ef4603a51 |
| test_jdbc_readiness.py |6f2ed77e42a36ffb93c2bd87bb0e29a290a71c4abcabd2f699ab5b3004dc502c |
| Final jdbc-readiness.md runbook |cfdc4b2b3c769ee48635566411f0a53b67bf2ecd3c4c1888d064c245b11a0b3b |
| Unchanged WI026 artifact manifest |fbeb74425ade0fe3882a9dddc4c97f7b67b75dda711ffa37acbd755a02fa4d9c |
| Unchanged WI025 v3 helper |7333b068d0264045b8130519afc6e0f62589912ba2ef0b31772ff84033960a1b |

Plan identity:
`6c1ad23065f89bc0776cb628322941b166dc82dbf4f56ee15c052aa2a1860199`.
It is SHA-256 of ASCII `json.dumps(input_sha256, sort_keys=True)`, using the six
relative path/hash pairs returned by plan. It includes both production source
files, exact original JAR,driver,class and local artifact manifest. Absolute
Windows/Linux staging prefixes are excluded; relative output name is fixed.
Tests/runbook are review evidence outside the runtime classpath, not part of
that six-file executable identity. No private file/password/hash is included.

Final MA stage is `/root/wi029`, preserving `build/WI029-jdbc-v1` below it.
The `queryTimeoutKillsConnection=true` property was added before the first
prepare, is present in the source/class hashes above and asserted by the fake
driver. No production code changed after prepare. The runbook alone was aligned
with MA's reviewed stage, isolated Python `-I -B`, trusted working directory and
unconditional mask attempt/independent cleanup verification. Existing MA archives
were not read, changed or transferred by SE; MA must compare their six entries
against the final plan before any transfer.

## Commands and Test Evidence

```powershell
git status --short --branch
git rev-parse HEAD
javac -version
python --version
python -B scripts/deployment/jdbc-readiness.py prepare --output build/WI029-jdbc-v1
python -B scripts/deployment/jdbc-readiness.py smoke --output build/WI029-jdbc-v1
python -B -m unittest discover -s scripts/deployment -p test_jdbc_readiness.py -v
python -B scripts/deployment/jdbc-readiness.py plan --output build/WI029-jdbc-v1
```

Preparation exit0: PREPARED; `javac --release17 -encoding UTF-8 -Xlint:all`.
Actual pinned driver smoke exit0: DRIVER_LOADED. Diagnostic stderr is deliberately
discarded; compilation success does not assert a zero-warning compiler run.
No online dependency resolution or Gradle task ran.

Final unittest result: **29 tests,29 PASS,0 failures/errors/skips,59.130s**.
Five Java integration-style synthetic methods include15 error-stage scenarios,
seven malformed-frame scenarios,driver-load/no-connect,read-only SELECT success,
rollback/close and a stalled fake-driver deadline. Twenty-four Python methods
cover94 printable-password offsets,env quoting/duplicates/target rejection,
strict approval,private-path rejection,child env/args,overflow/deadline,exception
suppression and public JAR identity. Subcases are not added to the29-test total.

The fake driver logs synthetic password/URL data and throws causes/suppressed
exceptions; the wrapper receives only fixed codes. It asserts no TLS/auth
override,NullLogger,instrumentation-off,timeout properties,server-read-only
before reads,and connection close on all reachable failure paths. This is not
a MySQL protocol evaluator or native secret-file access proof.

TDD history: first run failed to import the intentionally absent implementation.
Next20-test run exposed a33-character synthetic password; fixture corrected to32.
First expanded29-test run had one Windows CRLF expectation mismatch; corrected
the synthetic child's binary output. Final29-test run passed. No production
rule was relaxed to make a test pass. All spawned test processes completed or
were deadline-killed/reaped; no server was started.

Full Gradle suite,coverage,SAST,native Linux guards,real TLS/authentication and
actual DB reads are NOT RUN. Security coverage thresholds are not marked passed.

Final local checkpoint22:43:27KST: plan reproduced the same identity; HEAD
unchanged; class major61 confirmed from bytes. Scoped `git diff --check` exited0.
Because these files are untracked, a separate direct six-file check also passed
trailing-whitespace checks, all seven metadata fields on all three documents,
metadata dependency existence and nine local Markdown links. The runbook hash
above includes the final MA-requested stage and F1/F2 cleanup corrections.

## Risks / Rollback / Evaluation

Native root/capture/SSH/MySQL guards and independent PG review remain mandatory.
The wrapper validates attestations, not privileged provider monitoring. The
Python nondumpable flag does not survive Java exec; inherited core limits and
JVM output/attach controls reduce exposure, not privileged memory access.
Password strings/driver copies are not guaranteed zeroized. Native stdout is
bounded to128 accepted bytes,stderr discarded,and arbitrary errors are not
returned. Java and driver may issue session-control/initialization commands;
only application queries are SELECT. Driver cancellation aborts its connection.

PASS checks43 exact table names,zero non-plan rows and six active plan key pairs,
not every price/column/index. It never changes canonical UNRECORDED or Hibernate
NOT_RUN. Existing URL TLS/auth defaults are preserved, not asserted sufficient
for general production security. A connection failure remains HOLD without
adding public-key retrieval or disabling TLS. No capacity claim is made.

Rollback is non-destructive: do not deploy/run this candidate,retain its public
evidence and existing bootstrap/JAR artifacts. Native execution writes no env,
DB or approval; MA creates approval separately and owns stop/remask/access
cleanup. No automatic deletion or restoration over retained data is needed.

Evaluation: exact hash extraction avoided new dependencies; fail-closed fake
drivers made error leakage/closure testable without a socket; Windows byte-level
output requires explicit newline handling. No policy/library change proposed.

## Follow-ups

WI029 blocks WI031, together with WI030. MA must continue the existing chain
immediately; PG review is already reported started. This SE creates no descendant
and does not close REQ009. After exact-candidate PG PASS, MA performs the bounded
native action and cleanup; WI032 integrates receipts and updates the runbook
index/current entry as appropriate. Those indexes are outside this SE write set.

## Related Documents

- [Handoff](WI-20261010-ATS-029-handoff.md): Unchanged ownership and constraints.
- [Summary](../user/WI-20261010-ATS-029-summary.md): User-facing result.
- [Runbook](../../deploy/single-vps/runbooks/jdbc-readiness.md): Exact native command and MA guards.
- [WI028](WI-20261010-ATS-028-evidence-pack.md): Previous actual target preparation and cleanup.
- [WI026](WI-20261010-ATS-026-evidence-pack.md): Exact application-JAR provenance.
