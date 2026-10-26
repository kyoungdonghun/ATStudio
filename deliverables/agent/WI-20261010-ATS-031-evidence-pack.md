---
version: 1.0
last_updated: 2026-10-10
project: ATS
owner: PG
category: evidence-pack
status: stable
related_wi: WI-20261010-ATS-031
dependencies:
  - path: WI-20261010-ATS-031-handoff.md
    reason: Skill-generated independent review and exact two-file write scope
  - path: ../user/REQ-20261010-ATS-009.md
    reason: Approved scope and sensitive-operation gates
  - path: WI-20261010-ATS-028-evidence-pack.md
    reason: Completed bootstrap and historical cleanup evidence
  - path: WI-20261010-ATS-030-evidence-pack.md
    reason: Completed configuration-readiness audit
  - path: WI-20261010-ATS-029-evidence-pack.md
    reason: Completed probe preparation and final candidate identities
---

# Evidence Pack: WI-20261010-ATS-031

## Summary

TL;DR: **Final PG PASS for the exact candidate and corrected MA procedure.**
Review owner PG, local Windows/PowerShell, 2026-10-10 approximately 22:35-22:46 KST.
Branch `main`, HEAD `58b1011dad796017e47dbcc82a4225ca449bcdf6`; pre-existing dirty
tracked/untracked changes preserved. This is not server execution or a general
security/capacity certification. WI029 completion, final hashes and the complete
corrected MA sequence match the reviewed scope; no blocking finding remains.
Actual native JDBC/cleanup receipts remain MA-owned and NOT RUN by PG.

Manual write ownership: this Evidence Pack and the WI031 user summary only.
Tests/compilation generated local `build/WI029-*` fixtures and public outputs;
no implementation, handoff, historical receipt, template or Git index was edited.
No remote command, actual SQL, secret read/generation, app startup, installation,
Provider/mail action, descendant delegation, commit or push occurred.

## Scope / DoD Check

- [x] Approved REQ and existing WI031 handoff confirmed; Tier 0 fully injected.
- [x] Candidate source, synthetic tests, WI030 and current chronology reviewed.
- [x] Independent tests, class compilation, driver smoke and archive checks.
- [x] Concrete procedure defects promptly reported to MA.
- [x] Two-set deliverables and explicit WI032 dependency recorded.
- [x] WI029 completion/runbook and final identity reconciliation.
- [x] Corrected exact staging/native/cleanup sequence accepted for final PASS.

## Reference Documents (Tier 0-2)

| Tier | Input and actual use |
|---|---|
| 0 | Fully injected `core-principles.md`, `documentation-standards.md`, `development-standards.md`, `glossary.md` under `docs/standards/`: STD-001/004/002/005. |
| 1 | Injected PG persona and `security-policy.md`; locally read `access-control-policy.md`, `quality-gates.md`. |
| 2 | Read `create-wi-evidence-pack`, `test`, `build-check`, `validate-docs` skills under `.agents/skills/`; handoff-exists gate satisfied. |
| 2 | `docs/standards/evidence-pack-standard.md`, `docs/templates/eval-report-template.md`: required reproduction, risks and bounded evaluation. |
| 2 | `docs/index.md#current-decision-entry`, `deploy/single-vps/CONFIGURATION.md`, deployment/readiness runbooks, reconstruction README and DB-schema current boundary: chronology, startup and recovery limits. |
| Context | Current REQ009, WI031 handoff, WI028 final v3/native cleanup, WI029/WI030 completion and relevant WI014 evidence. MA chat supplies exact corrected staging/envelope and attributed syntax receipts, not a JDBC execution receipt. |
| Source | Probe Python/Java/tests, immutable prior bootstrap helper/validator, schema/seed, application.yml, environment example/contract, service unit and build.gradle. |
| Conditional, not invoked | Frontend standards/React best practices: no frontend implementation or browser workflow in this WI. |

Injection order is Tier0 -> PG -> task context -> snapshot. Workspace JSON confirms
ATS; context-injection rules require PG tiers [0,1] and security/testing policies.
Memory was a navigation hint only; current candidate and completion receipts
supersede older resource-design notes. No memory or policy change was made.

## Findings and Procedure Review

### F1: Cleanup Must Remask Even When Stop Fails (Resolved)

Initial MA chat draft `cleanup_mysql`: after the bounded stop and retry loop,
the non-inactive branch emits `MYSQL_CLEANUP=HOLD` and exits92 before
`systemctl mask mysql.service`. This leaves the previously unmasked unit exposed
to restart precisely on a failure path. The final complete function attempts
mask unconditionally, checks its exit and exact link, checks inactive state,
captures jobs/listeners with command status, and exits nonzero on any cleanup
failure before its PASS marker. Original probe status is preserved on successful
cleanup. This is static control-flow review, not a native failure observed by PG.

### F2: Isolate Every Root Python Entrypoint (Resolved)

Initial staging/envelope commands use `python3 -E -s -B -`. `-E -s` still permits
imports from the working directory. Imported code runs with root privilege before
the probe's own secure-path checks. Use `-I -B` and a trusted cwd for staging,
offline native plan and the envelope; insert only the reviewed root-private prior
helper directory explicitly. Local demonstrations show empty-path import present
with `-E -s` and absent with `-I`. No malicious module was created or executed.
MA's final staging, plan and envelope use `-I -B`, clean env and `cd /root`;
MA explicitly confirms the same correction for the capture preflight. The current
runbook agrees. No ambient current-directory import remains in these entrypoints.

### Final Exact Sequence Acceptance

An intermediate MA chat revision was malformed: a replacement-string expansion
duplicated the envelope inside an unterminated listener regex. It was refused,
never executed per MA, and superseded by the complete direct-slice reconstruction.
Final review uses that complete single-cleanup/single-envelope message only.

MA's attributed target `bash -n` receipts identify LF script bodies without a
trailing newline; the invoker adds one final LF, with contents otherwise exact:

| Accepted script | MA-reported SHA-256 / syntax |
|---|---|
| `WI029stageCode` | `dd85449d88ed35c7e7dc2a2fdf6a1cd51b99a37fedbe53ef5ca36a750fd78d7a` / PASS |
| `WI029nativeCode` | `815cefb6b9df58894ca5807ba07f573d41ced26f21beac88c7590bd87ae3a37e` / PASS |

PG reviewed the supplied complete bodies, but did not run target syntax or hash
MA's in-memory script strings. The public archive was independently inspected.
Acceptance is limited to fresh `/root/wi029`, output `build/WI029-jdbc-v1`, bundle
and six-file archive below, and the original approved target/current window.
Old stages are preserved. Whole JAR staging is not deployment/start authority.

The final shell registers cleanup before unmask/start, preserves the approved
boot/config/helper identities, and bounds the native envelope with120s plus5s
kill escalation. Historical identity comes from the protected old approval;
the old expired authority is not reused. New approval is exclusive root0600,
ten-minute expiry and current operator checks. Before/after old read-only guard,
grants, observation and env-metadata checks wrap one `j.main(probe)` call.
No bootstrap replay, mutation, secret-body transfer or direct Java invocation.
MA still owns fresh capture checks, pinned upload/access and final host/agent
cleanup receipts; this review does not certify their future outcome.

### Reviewed Boundaries

- `jdbc-readiness.py:95,114,161`: root-owned/no-link/single-link bounded private
  reads; strict generated env grammar; exact DB target and short-lived approval.
  Duplicate keys, unknown connection/approval keys, altered transport settings
  and nonboolean checks fail; unrelated generated env values are discarded.
- `jdbc-readiness.py:200,207,253,261`: clean child environment, binary stdin
  framing, bounded stdout, discarded stderr, fixed status protocol, bounded child
  deadline and reap; JVM dump/perf/attach/logging controls. No secret argv/env.
- `jdbc-readiness.py:277,309,335,350`: pinned JAR/driver and exact class inventory;
  source/manifest integrity; trusted Java link-chain checks; Linux/root-only
  native path; approval and driver-load checks before private env consumption.
- `RuntimeJdbcProbe.java:56,88,122`: driver logging/telemetry disabled, output
  redirected before driver loading, no Spring classpath or application launch.
  Read-only/autocommit-off session checked before fixed SELECTs, target identity,
  exact table inventory and bounded scalar aggregates. Success rolls back/closes;
  exceptions/causes/suppressed errors never serialize. Session setup/rollback are
  JDBC control traffic, not a claim that the wire contains SELECT packets only.
- SQL table names derive from a fixed code list, independently matched to all43
  source CREATE TABLE names. No user-supplied SQL or mutation path is present.
- Native operator checks are attestations, not automated infrastructure proofs.
  MA must repeat capture, same-target/boot, service/job/listener, exclusive window
  and immutable-byte gates before setting their flags. Privileged capture and
  persistence of immutable strings in process memory are not disproved by tests.
- MA corrected historical identity provenance to root-protected bootstrap
  approval metadata, not the receipt. The old expired approval is never execution
  authority. The old receipt is used only for before/after observation comparison.
- The proposed envelope calls only old `load_bundle`, `host_guard`,
  `database_guard(fresh=False)`, `verify_grants(locked=False)` and `observe` paths.
  No old `apply`, `preflight` or `native_guard` rerun; no existing secret body read
  outside the new checker. Fresh JDBC approval is exclusive root0600,10-minute
  expiry, with current authority and exact bundle/target identity kept internal.
- Proposed archive handling verifies bounded root-private copy, whole SHA,
  exactly six regular allowlisted members and every member SHA before execution;
  no extractall, link/directory member, overwrite or arbitrary destination.
  Whole app JAR is verification-only staging, not `/opt` release or app startup.

## Commands and Test Evidence

Tools: Python3.14.3, PowerShell7.6.5, Git2.53.0.windows.1; preparation manifest
and independent compilation use javac17.0.12. No dependency was downloaded.
Test/build skills are scoped to the standalone probe; no Gradle app rebuild or
full application suite was run, preserving the approved executable JAR.

| Check | Independent PG result |
|---|---|
| `git status --short --branch`; `git rev-parse HEAD` | Shared dirty main/HEAD above; no Git writes. |
| `python -B -m unittest discover -s scripts/deployment -p test_jdbc_readiness.py -v` | First in-progress fixture snapshot:29 tests,1 CRLF/LF assertion failure,59.229s. After SE correction:29/29 PASS,0 failures/errors/skips,59.210s; exit0. |
| `python -I -B scripts/deployment/jdbc-readiness.py prepare --output build/WI029-pg-v1` | PREPARED; independent class and artifact manifest byte-identical to SE output; exit0. |
| `python -I -B scripts/deployment/jdbc-readiness.py plan --output build/WI029-jdbc-v1` | PLAN_ONLY; exact bundle below; exit0. |
| `python -I -B scripts/deployment/jdbc-readiness.py smoke --output build/WI029-jdbc-v1` | DRIVER_LOADED; no connection path; exit0. |
| `python -B scripts/deployment/validate_bundle.py --json` | Template PASS, errors empty; deployment_ready=false and production HOLD; exit0. Generic target flags are not live progress. |
| `python -E -s -B -c "import sys; print('' in sys.path)"` versus `python -I -B -c "import sys; print('' in sys.path)"` | True versus False; both exit0. Demonstrates F2 without external access. |
| Read-only Python/std-library ZIP, SHA and schema-name inspection | Pinned JAR, one driver entry/size/SHA and43 exact table names match; exit0. |
| Read-only Python tarfile member streams and SHA | Archive size/SHA and exactly six regular member bytes match plan; no extraction; exit0. |
| Five additional in-memory native-order cases | Expired/wrong-bundle/capture-unapproved reject before child or env read; bad smoke rejects before env read; accepted synthetic path reads approval then synthetic env and returns PASS. All pass; exit0. |

The additional ordering checks mock resource/prctl/umask, platform/euid, secure
paths, Java resolution, machine bytes, load_plan, read_private and run_child.
They prove Python ordering only, not Linux permissions, actual DB, OS capture or
signal/process-group behavior. Public archive reproduction: load_plan for the
exact SE output; require stated whole length/SHA; open `tarfile` in `r:` mode;
require six unique names exactly equal to input_sha256 keys and isfile for every
member; stream each extractfile through SHA-256 and compare length/hash.
An initial PowerShell-quoted inspection snippet failed parsing before execution;
the corrected literal-stdin form passed without a source change.

Synthetic fixture source intentionally injects sentinel stdout/stderr, connection
errors/causes/suppressed errors, bad identity/read-only/table/row results and stalls.
Tests use an in-memory fake JDBC driver, never a real connection. No total across
independent reruns is presented as distinct test coverage. Coverage was not measured.

## Public Identities

SHA-256 values identify inspected local bytes only; no secret/env hash recorded.

| Object | SHA-256 |
|---|---|
| SE plan bundle, output `build/WI029-jdbc-v1` | `6c1ad23065f89bc0776cb628322941b166dc82dbf4f56ee15c052aa2a1860199` |
| Public archive `build/WI029-public-v1.tar`,72954880 bytes | `a777353c974948c6eae0face78e48115141799c8e5a1d10ca6916fc2c6613cb5` |
| `build/libs/ATStudio-0.0.1-SNAPSHOT.jar`,70303135 bytes | `517b745479792a7eae6ce75357c7d1299ab644b50d5bc5b331fea384ccb10e0a` |
| `scripts/deployment/jdbc-readiness.py` | `13e2d084bef0e2bf1c6cb9ff64ee0dbae9650b40ccaa1091afc8b75365670051` |
| `scripts/deployment/RuntimeJdbcProbe.java` | `e2cd662de58fc10945e61e10cd497e2573b82eb8b6b074174c5ea2d4a2a3a8d5` |
| SE/PG `RuntimeJdbcProbe.class`,9232 bytes | `452a9b652b88ee1e19c5001586b8ccbe9725087f96d4828603bdd1c82af1c493` |
| Bundled/extracted mysql-connector-j-9.7.0.jar,2607237 bytes | `0353648eaa1c91e0f4020c959abf756bc866ffd583df22ae6b6f6e0cbd43eb44` |
| SE/PG artifact.json,611 bytes | `a356405db6372484a0bb0bfcd5491c5b60bfc2e9f2332eb426f13a7ef4603a51` |
| `scripts/deployment/test_jdbc_readiness.py` after SE fixture correction | `6f2ed77e42a36ffb93c2bd87bb0e29a290a71c4abcabd2f699ab5b3004dc502c` |
| Prior `production-bootstrap.py` | `7333b068d0264045b8130519afc6e0f62589912ba2ef0b31772ff84033960a1b` |
| Prior `validate_bundle.py` | `19361e515e694718b509e28c6223a3ffc0bf647ddf6bb972f8aa4904cc1cb593` |
| `deploy/single-vps/runbooks/production-readiness.md` | `18f49d2433e3b0124542da725fc1fc91b3a7556f332461ac6592f6900ecec34a` |
| WI030 Evidence Pack | `c11c7bc1b7e927ce53eac3673bd9d5657fa4fcdefc28b6107309e99e7e79e4ff` |
| Completed WI029 Evidence Pack | `97246c7ad2fe63665868cb7a337afe992e1e465f9d4921e4baf2cfc032103a41` |
| Final `deploy/single-vps/runbooks/jdbc-readiness.md` | `cfdc4b2b3c769ee48635566411f0a53b67bf2ecd3c4c1888d064c245b11a0b3b` |

PG's separate output-directory bundle is
`fe1924decd8e4927e65e3dc1fdd9ab8e71f141f1918aa422ab41af21c77800fc`:
its path-dependent identity is expected to differ despite identical class/manifest
bytes. It is not the selected native bundle. Final WI029/runbook reconciliation
passed; no substitution of this PG directory is approved. MA's f49500/74e922
local rehash receipts independently agree with these same executable identities.

## Documentation Verification

Scoped skill validation used `DocValidator.validate_tier0_docs`, internal links
and traceability IDs with `Path.rglob` restricted to the two owned documents:
exit0, no errors/warnings. No repository-wide index/orphan/remote-link claim.
Direct ASCII, seven metadata fields, dependency existence, trailing-whitespace
and final-newline checks passed. Scoped `git diff --check` exited0; because the
files are untracked, the direct checks provide their whitespace evidence.
Final closure validation repeated these checks:6 supported IDs,0 errors/warnings,
all five final WI029 source/test/runbook/evidence hashes matched, HEAD unchanged.
No other path is edited.

## WI030 Reconciliation and Limits

Readiness matrix correctly distinguishes dated MA-applied MySQL/bootstrap from
local-only app/proxy candidates, retains valid earlier development Gmail evidence,
and leaves new-host SMTP, domain ownership/purchase, DNS/TLS, ADMIN and first app
start separately gated. Current app resource candidates are not the older design
snapshot; source/template audit passes but proves no native runtime/capacity.
No new provider, architecture, purchase, allowance or pricing claim was made.
Baseline remains2vCPU/8GB/100GB; monthly KRW70000 is a budget, not a hard cap.

JDBC authentication/read success cannot close Hibernate NOT_RUN or canonical
fresh-manifest UNRECORDED. App startup can launch workers/schedulers and is wholly
outside this operation. Cleanup must include host services/jobs/processes/listeners,
closed sessions and exact owned Windows key removal with empty-agent and fresh
Stopped/Disabled evidence before MA reports the native window complete.

## Risks / Rollback / Evaluation

Native behavior is NOT RUN by PG. Source/fixture PASS is not privilege enforcement,
native MySQL semantics, transport encryption, target signal cleanup or production
approval. Preserve default JDBC transport settings: failure is HOLD, not permission
to enable public-key retrieval, disable TLS, recreate credentials or rerun bootstrap.

No product rollback is needed for this documentation-only review. Correct these
two files in scope and preserve historical observations. Do not delete fixtures,
old staging, approval records, DB rows, keys or other agents' work. An incomplete
native stage/approval is not authority for retry or arbitrary cleanup.

Evaluation: freezing byte identities separates a real fixture correction from
implementation approval; cleanup failure paths and Python entrypoint isolation
need review outside the Java happy path. No policy/retrospective edit authorized.

## Follow-ups / WI Chain

WI031 is complete with bounded final PASS. MA must now immediately issue the
WI032 skill handoff and delegate
DOCOPS, integrating only actual MA receipts or an exact NOT_RUN reason. PG cannot
run remote operations, delegate descendants or close REQ009. Production remains
HOLD independently of this bounded review.

## Related Documents

- [WI031 handoff](WI-20261010-ATS-031-handoff.md): Exact scope and role.
- [REQ009](../user/REQ-20261010-ATS-009.md): Authority and exclusions.
- [User summary](../user/WI-20261010-ATS-031-summary.md): Findings and disposition.
- [WI030](WI-20261010-ATS-030-evidence-pack.md): Readiness evidence.
- [WI029](WI-20261010-ATS-029-evidence-pack.md): Completed probe and final identities.
- [WI028](WI-20261010-ATS-028-evidence-pack.md): Completed bootstrap, not current-window cleanup.
- [Current entry](../../docs/index.md#current-decision-entry): Dated chronology.
