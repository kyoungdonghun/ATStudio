---
version: 1.0
last_updated: 2026-10-11
project: ATS
owner: PG
category: evidence-pack
status: confirmed
dependencies:
  - path: WI-20261011-ATS-007-handoff.md
    reason: Approved independent local-only review contract
  - path: WI-20261011-ATS-006-evidence-pack.md
    reason: Exact v3 implementation candidate
  - path: ../user/REQ-20261011-ATS-001.md
    reason: Approved scope and operational exclusions
---

# Evidence Pack: WI-20261011-ATS-007

> Purpose: Record the independent HOLD decision for the exact v3 schema probe.

## Findings and Verdict

**TL;DR: HOLD. One blocking source-compatible defect, PG-007-01 (P1). Native NOT_RUN by PG.**

`scripts/deployment/RuntimeSchemaProbe.java:59-60` supplies Integer values for
`jakarta.persistence.database-major-version` and `database-minor-version`.
The bundled Hibernate 7.2.24 `HibernateSchemaManagementTool.resolveJdbcContext`
casts those map values to String. The real validator call at probe lines105-106
therefore throws ClassCastException at HibernateSchemaManagementTool.java:280.
Mapping bootstrap succeeds, but schema validation cannot succeed on this candidate,
even with compatible target metadata. The production entry point first obtains
its initial read-only snapshot, then masks this failure as SCHEMA_REJECTED.
Do not interpret it as a target-schema mismatch or change the database.

PG reproduced the failure using the unchanged v3 compiled probe and libraries,
actual Hibernate validator, and synthetic JDBC metadata with Connector/J omitted.
The direct validator reproduction fails with zero connection attempts. A control
changing only these two values to strings in the synthetic harness's in-memory
settings passes matching metadata and rejects missing tables and missing columns.
This is diagnosis, not an implementation fix, replacement candidate or native PASS.

Existing tests miss this path: `RuntimeSchemaProbeHarness.java:168-190` substitutes
both SchemaManagementTool and SchemaValidator. Its routing assertion and offline
mapping smoke are useful but do not exercise resolveJdbcContext.

Required resolution: SE corrects the settings types for this exact library,
adds a bounded real-validator synthetic regression, prepares a fresh unused
candidate, and obtains independent review of its new hashes. Preserve v3 and all
earlier evidence. PG made no implementation edit and created no descendant.

## Scope / DoD Check

- [x] Read injected Tier0, role, security policy, skills, approved REQ and existing handoff.
- [x] Verify current main HEAD, dirty-tree boundary and exact candidate identity.
- [x] Compare backend638, frontend336 and SPA143 records; source/JAR bytes match.
- [x] Review metadata isolation, read-only provider, credential pipe and native procedure.
- [x] Independently run focused synthetic suites and exact-candidate mapping smoke.
- [x] Produce precise blocker, bounded operator conditions and two deliverables.
- [ ] Exact-candidate native permission: blocked by PG-007-01.
- [ ] Target schema validation: NOT_RUN by PG; MA reports native remains off.

PG verification: 2026-10-11, approximately01:49-01:58 KST, Windows/PowerShell,
Java17.0.12. Repository main HEAD58b1011dad796017e47dbcc82a4225ca449bcdf6.
Existing dirty/untracked implementation, documentation and history were preserved.
Only PG's two deliverables and unique synthetic build outputs were written.
No SSH/network/DB/private configuration/browser/native/service operation,
application-source rebuild, installation, commit, push or delegation occurred.

## Reference Documents (Tier 0-2)

| Tier | Document | Use |
|---|---|---|
| 0 | core-principles.md, STD-001 | Approved scope and transparent evidence |
| 0 | documentation-standards.md, STD-004 | English two-set deliverables |
| 0 | development-standards.md, STD-002 | Independent reproducible review |
| 0 | glossary.md, STD-005 | Canonical terms |
| 1 | .claude/agents/pg.md; security-policy.md | Injected PG role and full security policy |
| 2 | create-wi-handoff-packet; create-wi-evidence-pack | Injected skill contracts; existing handoff verified |
| 2 | .agents/skills/test/SKILL.md | Loaded before focused synthetic tests |
| 2 | docs/index.md Current Decision Entry | Current restricted TLS and previous bounded JDBC evidence |
| 2 | REQ001; WI006 evidence; WI007 handoff | Current approval, exact candidate and ownership |
| 2 | release/JDBC helpers, Java probes, tests and runbooks | Local implementation and process review |
| 2 | WI026 artifact; WI027/028/031 evidence | Public artifact and retained-helper provenance |

All four Tier0 documents were injected before role/policy/task context. No new
handoff was created. The evidence skill precondition was met by WI007's existing
handoff. Current source and completion evidence, not older planning notes, govern
the decision. This is not a complete application/CVE/security audit.

## Evidence Pointers

| Location | Bounded result |
|---|---|
| release-readiness.py:102-125 | Exact recorded input comparison; frontendCompletion SPA used |
| release-readiness.py:128-154,200-224 | Explicit30-library/model classpath, no app JAR launch; exact staged inventory/hash |
| RuntimeSchemaProbe.java:55-106 | Boot naming,43 entities/tables, metadata bootstrap and explicit validate; blocking version types |
| RuntimeSchemaProbe.java:109-205 | Bounded ordered metadata digests, per-connection read-only/identity checks, close/rollback |
| RuntimeSchemaProbe.java:207-277 | Suppressed library output, fixed outcomes, no SessionFactory/Spring context |
| release-readiness.py:227-300 | New REQ/WI/hash/time attestation, secure paths, bounded secret stdin and child |
| jdbc-readiness.py:79-197,200-259 | Root/no-link/single-link private reads, grammar/framing and child bounds |
| RuntimeJdbcProbe.java:56-85 | Exact account/URL; Connector/J logging, telemetry, reconnect and cancellation controls |
| production-bootstrap.py:89-113,181-196,239-265,338-418 | Public bundle load, SELECT guard/observe/grants; receipt lacks host/server identity hashes |
| RuntimeSchemaProbeHarness.java:168-190 | Validator test double explains regression gap |

Model inspection found only the existing AuditingEntityListener annotations in
BaseEntity/AlbumLike/Like/TrackDownload, no AttributeConverter in this mapping
baseline, and no application lifecycle classes/resources on the selected
classpath. Reviewed model initialization includes enum/constants and ordinary
constructors, not an app startup path. Service-provider entries were inventoried
from all30 pinned libraries: JDBC, Hibernate bytecode/JPA, JAXB/activation, CDI,
Servlet and BlockHound registrations. This metadata-only path does not bootstrap
a servlet/CDI/Spring container or run an application worker. Offline smoke alone
is not a proof about every possible future library/mapping change.

Input enumeration matches the current public baseline, including dirty source
bytes registered in WI026. Four named frontend coverage tests remain historical
exclusions; private env files were neither opened nor hashed. Root build-tool
environment/user initialization and excluded inputs are not full reproducibility
claims. No source rebuild was used to hide changes.

## Exact Identity

Reviewed candidate: `build/WI006-schema-v3`,171 generated files plus artifact.json.
The aggregate identity includes178 staged inputs. It remains blocked:

`f06f7f2de2a891a482515f92abac8dfe75b4dfdddd1718a91f9c9592958a642b`

| File | SHA-256 |
|---|---|
| release-readiness.py | 8eaa586616b667d5069e9f43d09cba6810dd7246cb97bea8a4a108ef1a340f6f |
| RuntimeSchemaProbe.java | b17b29cdffb0ba34b436d15f1dbc7ac7a398cb86040fdb3cd664db445b698aa7 |
| test_release_readiness.py | eb093af015aeadcdd96ab5d410a413b4271329a00ddb84b39f0f197d4653e358 |
| RuntimeSchemaProbeHarness.java | 988de2c9e3e9199c3f4784c85c0a929e919e452cff63a51af7bf994c39479359 |
| build/WI006-schema-v3/artifact.json | ffadd34b644481bed92a7688f740270047adede4da458f9805230e68fb3c7e21 |
| jdbc-readiness.py | 13e2d084bef0e2bf1c6cb9ff64ee0dbae9650b40ccaa1091afc8b75365670051 |
| RuntimeJdbcProbe.java | e2cd662de58fc10945e61e10cd497e2573b82eb8b6b074174c5ea2d4a2a3a8d5 |
| release-readiness.md | 9f2068635b052b4767770c0692ab319633993147e82baa3881d1a98b890c4abc |
| Existing JAR | 517b745479792a7eae6ce75357c7d1299ab644b50d5bc5b331fea384ccb10e0a |
| WI026 artifact.json | fbeb74425ade0fe3882a9dddc4c97f7b67b75dda711ffa37acbd755a02fa4d9c |
| Retained production-bootstrap.py | 7333b068d0264045b8130519afc6e0f62589912ba2ef0b31772ff84033960a1b |
| Retained validate_bundle.py | 19361e515e694718b509e28c6223a3ffc0bf647ddf6bb972f8aa4904cc1cb593 |

## Commands and Tests

```powershell
python -B scripts/deployment/release-readiness.py check
python -B scripts/deployment/release-readiness.py plan --output build/WI006-schema-v3
python -B scripts/deployment/release-readiness.py smoke --output build/WI006-schema-v3
python -B build/WI007-pg-20261011-0153/run_review_tests.py
python -B build/WI007-pg-20261011-0153/reproduce_validator.py
```

| Check | Actual result |
|---|---|
| Public check/plan | INPUTS_MATCH / exact v3 PLAN_ONLY, exit0 |
| Exact v3 mapping smoke | MAPPING_READY, exit0; no connection |
| Focused Python/Java suites |48/48 PASS,0 failures/errors/skips,76.045s |
| Real validator, exact v3 |ClassCastException, direct validator connections0; harness exit1 |
| Synthetic string control | Matching metadata PASS; missing table/column rejected; exit0 |

The suite runner changes only test setup to reuse existing v3 compiled probes.
It compiles synthetic drivers/harnesses, never application or candidate sources.
All callable valid-credential synthetic JDBC runs omit Connector/J from classpath.
Malformed framing tests with the real library terminate before connecting.

- [48-test log](../../build/WI007-pg-20261011-0153/run-298605262e2d4dcc9dfab69ae1a50b5b/unittest.log).
- [Exact failure](../../build/WI007-pg-20261011-0153/validator-e5431f52de7f45f19463ba183848ad33/exact-candidate.log).
- [String control](../../build/WI007-pg-20261011-0153/validator-e5431f52de7f45f19463ba183848ad33/string-control.log).
- [Bounded reproducer](../../build/WI007-pg-20261011-0153/reproduce_validator.py).
- [Actual validator harness](../../build/WI007-pg-20261011-0153/ActualValidatorHarness.java).

The initial combined runner exits1 after its48 tests pass because the added real
validator fails; its first validator stdout log is empty because stderr was
suppressed. A synthetic-only diagnostic run exposed the ClassCastException; the
bounded reproducer now records only its class/location/types/count and verifies
the expected failure plus a control. Preserve the initial failed run.

Local javap inspection of the pinned Hibernate confirms String casts at bytecode
147/177 and the exact Jakarta version keys. The synthetic control also observes
Hibernate committing its read-only metadata session before enabling autocommit,
then restoring autocommit and rolling back/closing. It never removes read-only.
Describe this as read-only data access with JDBC transaction-control traffic,
not a wire-level SELECT-only/no-COMMIT guarantee. No schema/data DDL/DML was run.

No coverage measurement, whole-app tests/build, Linux filesystem test or native
MySQL compatibility proof. Synthetic matched columns are not canonical fresh DDL.

Final01:58 KST plan recheck returned the unchanged v3 bundle. Both PG documents
passed direct metadata, final-newline, trailing-whitespace and local-link checks;
scoped git diff --check exited0. Direct checks include these untracked documents,
which git diff alone does not validate. All PG command sessions completed.

## MA Procedure Review

**Answer: retained `load_bundle(Path('/root/wi025-v3'))` plus `observe(bundle)`
before/after is acceptable as the proposed bounded read-only reuse, conditional
on the following gates. It does not override the current candidate HOLD.**

1. Verify root-private ancestry, helper and validate_bundle pins above, every
   retained public bundle input and historical receipt provenance before import.
   Import only that trusted dependency; load_bundle performs public file checks,
   not SQL application. Preserve old inputs/approvals/receipts.
2. Use the existing JDBC approval only for historical identity: exact
   REQ-20261010-ATS-009, WI-20261010-ATS-031 and bundle
   6c1ad23065f89bc0776cb628322941b166dc82dbf4f56ee15c052aa2a1860199.
   Check root0600/single-link shape, current machine and database UUID internally;
   corroborate with the separately verified bootstrap approval. The bootstrap
   receipt contains observation/public hashes, not these identities. Its expired
   authority cannot authorize new execution. Missing/mismatch is HOLD.
3. Before/after call only database_guard(fresh=False), verify_grants(locked=False)
   and observe(bundle); compare complete observations to each other and the
   verified historical bootstrap receipt['observation']. No apply, preflight,
   native_guard, host_guard, mutate, secret generation or schema repair.
   These fixed root-socket SELECT checks are distinct from the app-account
   server-read-only JDBC sessions. Keep rows/metadata/identity hashes internal.
4. Stage an exclusive root-private exact input list, no archive/overwrite; verify
   candidate and procedure identities before importing or running. Offline Linux
   smoke is useful but does not cure PG-007-01 or prove actual validation.
5. After a corrected candidate receives PG PASS, create a new exclusive root0600
   approval with current REQ/WI/hash, real operator checks and <=30-minute UTC TTL.
   Use the documented env-i Python-I-B wrapper only; never direct Java, sourced
   env, secret argv/env/logging or dumped exceptions. Recheck approval before use.
6. Arm cleanup before unmask/start. A plain Python try/finally is not sufficient
   for default SIGHUP/SIGTERM or a parent-only kill. Handle interrupt/termination,
   track/reap the exact wrapper/Java process group on timeout, and retain an
   independent recovery-console cleanup path for connection loss/SIGKILL.
   Bound each stop/mask command; always attempt mask even when stop fails/times
   out. Confirm inactive persistent mask, no DB ports/jobs/probe processes.
7. Recheck restricted Nginx/TLS pins/listeners/no app upstream and renewal timer;
   never reuse host_guard's obsolete Nginx-off assumption. Recheck private env
   metadata without exporting body/hash. Close this window's sessions/key/agent
   with exact ownership and verify empty agent then Stopped/Disabled.

MA's01:52 capture refinement is attributed input, not PG observation: nop tracer,
events disabled, empty events/probes/no instances, no reported active recording/
forwarding units; transient psimon absent in the next sample. tracing_on1 alone
does not establish active capture. Recheck effective state at use and HOLD for
new/unexplained capture. These bounded checks do not promise protection against
privileged host/provider memory capture; no requirement to prove that impossible
negative or weaken controls is introduced.

The supplied narrative is an acceptable design subject to these conditions;
PG has not seen or approved an exact executable native lifecycle implementation.
The runbook's probe command remains **NOT APPROVED for v3**. No target-native
result/cleanup receipt is produced by this review.

## Risks / Rollback / WI Chain

Stop using v3 for native validation. Preserve it and the reproduction. No local
implementation rollback or target rollback is needed because PG changed neither.
Do not reset/delete dirty history, replay bootstrap, alter schema/grants/secrets,
activate the application or expand into a general dependency audit.

Return PG-007-01 to MA immediately for SE correction and exact-candidate re-review.
WI007's independent review is complete with HOLD, not acceptance PASS. MA owns
WI008 integration of this HOLD and actual execution/cleanup status; no descendant
is created here. REQ remains active. Canonical fresh manifest, full application
behavior, capacity, public launch and production readiness remain unproven.

## Related Documents

- [Handoff](WI-20261011-ATS-007-handoff.md): Scope and write ownership.
- [User summary](../user/WI-20261011-ATS-007-summary.md): Decision and next action.
- [SE evidence](WI-20261011-ATS-006-evidence-pack.md): Original exact candidate.
- [Release runbook](../../deploy/single-vps/runbooks/release-readiness.md): Reviewed proposed procedure.
- [Current entry](../../docs/index.md#current-decision-entry): Dated operational evidence.
