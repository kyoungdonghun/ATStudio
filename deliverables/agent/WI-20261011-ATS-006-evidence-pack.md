---
version: 1.0
last_updated: 2026-10-11
project: ATS
owner: SE
category: evidence-pack
status: confirmed
dependencies:
  - path: WI-20261011-ATS-006-handoff.md
    reason: Approved delegated local-only ownership and acceptance criteria
  - path: ../user/REQ-20261011-ATS-001.md
    reason: Requirements and WI007 dependency
  - path: WI-20261010-ATS-026-artifact.json
    reason: Immutable release comparison baseline
---

# Evidence Pack: WI-20261011-ATS-006

> Purpose: Identify the exact local-only candidate for independent PG review.

## Summary

TL;DR: local preparation complete; native NOT_RUN; review PENDING WI007.
SE verified on Windows/PowerShell, Java17.0.12, 2026-10-11 approximately
01:40-01:48 KST. Source HEAD remained main/58b1011dad796017e47dbcc82a4225ca449bcdf6.
No app source, old helpers/manifests, schema, REQ/index or unrelated dirty files
were edited. No new dependency, download, DB, private env, network, remote,
browser, service, commit, push or descendant operation was performed.

## Scope / DoD

- [x] Existing release identity checked before preparation and again afterward.
- [x] Exact-JAR metadata bootstrap with Boot naming and no application lifecycle.
- [x] Explicit validate-only path, read-only verified provider, bounded structural snapshot.
- [x] Existing private-path/env/framing/process guards reused unchanged.
- [x] Local compilation, actual offline mapping smoke and focused synthetic tests.
- [x] TLS-preserving MA-only procedure and exact candidate identity recorded.
- [ ] Independent PG review and target-native verification: WI007/MA, not this WI.

## Reference Documents

| Tier | Injected document | Use |
|---|---|---|
| 0 | docs/standards/core-principles.md (STD-001) | Approved scope and transparency |
| 0 | docs/standards/development-standards.md (STD-002) | Implementation and focused tests |
| 0 | docs/standards/documentation-standards.md (STD-004) | Two-set deliverables |
| 0 | docs/standards/glossary.md (STD-005) | Canonical terms |
| 1 | .claude/agents/se.md | Delegated SE role, no descendants |
| 1 | docs/policies/security-policy.md | Full reinjected policy superseded truncated excerpt |
| 2 | create-wi-handoff-packet, create-wi-evidence-pack skills | Existing handoff and this output contract |
| 2 | test / build-check skills | Scoped unittest/javac instead of a whole app build |
| 2 | docs/index.md Current Decision Entry; WI032 evidence; application.yml; db-schema.md | Injected baseline, current bounded state and mapping limits |
| 2 | jdbc-readiness.py / RuntimeJdbcProbe.java / production-bootstrap.py | Reused guards and actual receipt fields |
| 2 | WI026 artifact; release/JDBC/production readiness runbooks | Artifact and operator boundary |

All four Tier0 documents were injected before role/context. Handoff skill output
already existed and was checked; no new handoff or governance framework was
created. Evidence skill output is this pack. No complete repository audit.

## Evidence Pointers

| File / location | Behavior |
|---|---|
| scripts/deployment/release-readiness.py:102 | Recorded input hash and exact enumeration comparison |
| scripts/deployment/release-readiness.py:128 | Only selected hash-pinned JAR libraries/model classes |
| scripts/deployment/release-readiness.py:158 | Exclusive new output, javac17, unchanged inputs rechecked |
| scripts/deployment/release-readiness.py:200 | Exact file inventory and hashes, no extra class accepted |
| scripts/deployment/release-readiness.py:227 | Current REQ/WI007 required before reusing old shape/time parser |
| scripts/deployment/release-readiness.py:256 | Safe env subset, mapping/config overrides fail closed |
| scripts/deployment/release-readiness.py:266 | Linux/root, secure paths, new approval, sanitized child/secret pipe |
| scripts/deployment/RuntimeSchemaProbe.java:55 | Actual bundled Boot naming; automatic DDL and callbacks off |
| scripts/deployment/RuntimeSchemaProbe.java:79 | Explicit43 entities and43 expected tables, no component scan |
| scripts/deployment/RuntimeSchemaProbe.java:98 | SchemaValidator only, no SessionFactory/EMF |
| scripts/deployment/RuntimeSchemaProbe.java:109 | Ordered metadata, count/field bounds, length-framed null-safe digest |
| scripts/deployment/RuntimeSchemaProbe.java:142 | Every connection verifies server read-only and exact identity |
| scripts/deployment/RuntimeSchemaProbe.java:207 | Suppression before libraries load, fixed failures, equal before/after snapshot |
| scripts/deployment/test_release_readiness.py; RuntimeSchemaProbeHarness.java |19 tests plus Java synthetic cases |
| deploy/single-vps/runbooks/release-readiness.md | Preflight, historical identity provenance and independent stop/remask |

No AttributeConverter exists in the current source scan. Extracted model
dependencies include BaseEntity, two exception types and four storage enums.
AuditingEntityListener annotations require existing spring-aspects/AspectJ at
metadata load, but no listener execution or bean creation occurs.

## Release Identity and Exclusions

Backend638 and frontend336 recorded inputs all match their recorded byte sizes
and SHA-256. Exact backend enumeration also matches. Isolated SPA143 files and
its full enumeration match `frontendCompletion`, not the older pending frontend
snapshot. Existing JAR SHA-256 remains
`517b745479792a7eae6ce75357c7d1299ab644b50d5bc5b331fea384ccb10e0a`.
WI026 manifest SHA-256 is
`fbeb74425ade0fe3882a9dddc4c97f7b67b75dda711ffa37acbd755a02fa4d9c`.

WI026's recorded verification command excluded every coverage directory. The
initial stricter enumeration detected these four files outside its336 records:

```text
frontend/src/test/coverage/adminSubscriberGaps.coverage.test.tsx
frontend/src/test/coverage/adminSubscriberPages.coverage.test.tsx
frontend/src/test/coverage/publicAuthShell.coverage.test.tsx
frontend/src/test/coverage/shellCatalogRouterGaps.coverage.test.tsx
```

They are **historically excluded test inputs**, not a backend/JAR/SPA hash drift.
Their contents/history were not verified, and no complete frontend source or
typecheck equivalence claim is made. This implementation excludes exactly those
four paths, not a broad coverage glob; an additional `Unexpected.tsx` under that
directory is tested to fail enumeration. Private `.env` files are never opened
or hashed. No React code changed and no rebuilding was used to hide differences.

## Exact Candidate

Final public candidate: `build/WI006-schema-v3`.
Bundle: `f06f7f2de2a891a482515f92abac8dfe75b4dfdddd1718a91f9c9592958a642b`.
`artifact.json` contains every generated file hash and compiler hash; `load_plan`
adds source/JAR/WI026 hashes for exact staging. v1/v2 and test outputs are retained
local history and are not the final reviewed candidate. No overwrite/deletion.

| Public file | SHA-256 |
|---|---|
| release-readiness.py | 8eaa586616b667d5069e9f43d09cba6810dd7246cb97bea8a4a108ef1a340f6f |
| RuntimeSchemaProbe.java | b17b29cdffb0ba34b436d15f1dbc7ac7a398cb86040fdb3cd664db445b698aa7 |
| test_release_readiness.py | eb093af015aeadcdd96ab5d410a413b4271329a00ddb84b39f0f197d4653e358 |
| RuntimeSchemaProbeHarness.java | 988de2c9e3e9199c3f4784c85c0a929e919e452cff63a51af7bf994c39479359 |
| build/WI006-schema-v3/artifact.json | ffadd34b644481bed92a7688f740270047adede4da458f9805230e68fb3c7e21 |
| unchanged jdbc-readiness.py | 13e2d084bef0e2bf1c6cb9ff64ee0dbae9650b40ccaa1091afc8b75365670051 |
| release-readiness.md (corrected historical identity source) | 9f2068635b052b4767770c0692ab319633993147e82baa3881d1a98b890c4abc |

Runbook identity is separate from executable bundle identity; PG must review
both, including its correction that the bootstrap receipt lacks identity hashes.
The candidate inventory contains171 generated files plus its artifact manifest.

## Commands and Test Evidence

```powershell
python -B -m unittest discover -s scripts/deployment -p test_release_readiness.py -v
python -B -m unittest discover -s scripts/deployment -p test_jdbc_readiness.py -v
python -B scripts/deployment/release-readiness.py check
python -B scripts/deployment/release-readiness.py plan --output build/WI006-schema-v3
python -B scripts/deployment/release-readiness.py smoke --output build/WI006-schema-v3
```

- New suite:19/19 PASS in30.407s. Existing JDBC suite:29/29 PASS in56.209s.
- Actual Hibernate7.2.24/Boot4.0.8/entity metadata smoke: MAPPING_READY, no
  connection, final candidate hash above. Compilation javac17.0.12 exit0.
  Compiler stderr is discarded; no zero-warning claim.
- Synthetic provider tests: read-only/session order, account/database/server/
  version rejection, connect failure, rollback/close on success and failure.
  The synthetic harness classpath **omits Connector/J**, so failed fake-driver
  attempts cannot fall through to a real driver/socket.
- Metadata snapshot tests: stable digest, NULL vs empty, wrong table, field and
  row bounds. Validator routing double rejects every schema tool except validate.
  This is not actual target schema validation and is not described as such.
- CLI/protocol/env/approval/path/source/inventory rejection and invalid framing
  tests pass. Old suite rechecks secret-safe subprocess timeouts, output bounds,
  no-follow/ownership/mode/hardlink checks, sanitized exceptions and env filtering.
- Initial offline mapping failed on omitted spring-aspects dependency; existing
  JAR's spring-aspects/AspectJ added, then actual mapping passed. First new-suite
  failure was only harness CRLF vs LF output; explicit LF corrected and all19
  retested. Runtime protocol remained strict throughout.
- No coverage measurement, whole-app build/test, mobile/browser, native Linux
  filesystem test or live DB proof. No dependency installation or downloads.
- Final `check` returned INPUTS_MATCH and `plan` returned the exact v3 bundle.
  Scoped content checks on all seven new files passed final newline/trailing
  whitespace, document metadata and local Markdown links; scoped git diff
  --check exit0. Direct content checks cover untracked files, which git diff
  alone does not cover. The whole-repository validator was intentionally not run.

## Native Boundary and Corrections

MA supplied remote preflight facts; SE did not independently execute/verify them
and they are not WI006 native receipts. Restricted TLS/timer must stay active.
Old production-bootstrap `host_guard()` is incompatible and explicitly forbidden.
Only separately reviewed `database_guard(fresh=False)` / `verify_grants` may be
reused under MA authority and exact retained source verification.

Source inspection at production-bootstrap.py:107 and:441 confirms receipt
construction omits machine/server UUID hashes. Runbook now identifies the prior
root-private JDBC approval (REQ009/WI031/exact prior bundle) as historical pinned
identity evidence, with optional bootstrap-approval corroboration. Its old
authority is expired; a fresh WI007-reviewed approval is mandatory. Missing,
mismatched or untrusted provenance is HOLD, never trust-on-first-observation.
No private file was accessed to establish this code-level correction.

## Risks / Rollback / Follow-up

Native schema validation NOT_RUN. Structural counts/digest only establish the
target snapshot; canonical fresh-schema equivalence remains UNRECORDED.
Read-only is a session guarantee with operator-enforced exclusive maintenance,
not a permanently SELECT-only account: existing write grants are not changed.
Hibernate validation coverage and resource bounds are documented in the runbook.
Credential strings/driver copies are not guaranteed zeroized or protected from
privileged capture. Full application startup/publication is not approved by PASS.

Rollback: stop using this candidate; use a scoped patch only to these new WI006
files if correction is needed, generate a new candidate and repeat PG review.
Do not reset dirty files, replay bootstrap, delete stages or alter DB/config.

**WI chain:** WI006 Blocks WI-20261011-ATS-007. Hand back immediately to MA for
PG delegation; SE creates no descendants. WI008 waits for review plus actual MA
execution/cleanup receipts. REQ remains active; site is not live.

## Related Documents

- [Handoff](WI-20261011-ATS-006-handoff.md): Scope and approved ownership.
- [Summary](../user/WI-20261011-ATS-006-summary.md): User-facing result.
- [Runbook](../../deploy/single-vps/runbooks/release-readiness.md): Native procedure.
- [WI026](WI-20261010-ATS-026-artifact.json): Historical artifact authority.
