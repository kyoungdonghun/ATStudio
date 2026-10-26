---
version: 1.0
last_updated: 2026-10-11
project: ATS
owner: PG
category: evidence-pack
status: confirmed
dependencies:
  - path: WI-20261011-ATS-012-handoff.md
    reason: Approved independent local delta review
  - path: WI-20261011-ATS-011-evidence-pack.md
    reason: Corrected v5 candidate and regression scope
  - path: WI-20261011-ATS-010-evidence-pack.md
    reason: Preserved historical review and unchanged safeguard evidence
  - path: ../user/REQ-20261011-ATS-001.md
    reason: Local correction approval and separate future native authority
---

# Evidence Pack: WI-20261011-ATS-012

> Purpose: Independently review exact v5 compatibility with the full public bootstrap environment.

## Findings and Verdict

**TL;DR: scoped PASS for the exact v5 code/candidate below; native NOT_RUN. No remaining material blocker was found in this delta. No operator or remote retry is approved.**

The previous PG review missed an integration defect: v4 rejected the existing packaged-only `SPRING_CONFIG_LOCATION` produced by the public bootstrap renderer. This is now regression-covered using the full 83-key public fixture: pinned v4 rejects with `ENV_KEYS`, while v5 accepts. Public source and synthetic evidence establish that incompatibility; they do not identify an exact offending line in the private remote file. MA's historical `ENV_KEYS` diagnostic and cleanup remain separately attributed.

| Evidence pointer | Independent conclusion |
|---|---|
| `release-readiness.py:256` | Adds only the exact byte line `SPRING_CONFIG_LOCATION="classpath:/"` to the existing two-line allowlist. Restricted prefixes and the strict JDBC parser are unchanged. No prefix-wide exception. |
| `test_release_readiness.py:37,151,160` | Hash-pinned actual bootstrap renderer; example, contract and validator key-set/value checks; all 83 keys retained with deterministic synthetic secrets. Existing v4 regression uses its pinned staged wrapper. |
| `test_release_readiness.py:174,184,202` | Other locations, additional/import/unknown restricted overrides, unsafe DDL/logging settings, JVM injection, duplicates and malformed syntax rejected. |
| `release-readiness.py:62,139,227` and tests:64,80 | WI011 output namespace, WI012 review/check binding; WI007/WI010, old marker, wrong bundle/identity, invalid times/shape/booleans rejected. |
| `release-readiness.py:267` and unchanged Java/JDBC pins | Private-path handling, sanitized child execution, classpath selection, read-only validation and absence of application startup remain unchanged. |

## Exact Candidate and Pins

Candidate: `build/WI011-schema-v5`; bundle SHA-256:
`463c95667ee51e5fe2f01303a43ccebe0c0547375a374730c511c924212c7e54`.

| Public file | Independently verified SHA-256 |
|---|---|
| scripts/deployment/release-readiness.py | 5221e4c80ebc7a82aaade27e27b1c7f91cb6cea37980f44f3a73592c3ca4c7cd |
| scripts/deployment/test_release_readiness.py | bd8b085440767204f87545a3147ec0f55cde0c1f0c06d5a394f9d1860ac158a5 |
| build/WI011-schema-v5/artifact.json | 6749b2667e903635c0fe1849e66f7a261b0499a7ed29a19daa5e4da248836ba8 |
| deploy/single-vps/env/atstudio.env.example | 412e838b532c7249d0bbb9ca44eb114a659f7a78f1e2675bf87a676392fa18b0 |
| deploy/single-vps/env/contract.json | 9d02a317e3de26896dcfb8877b8fb5b51af7059b1c04117fa9ff170c72ec557d |
| deploy/single-vps/runbooks/release-readiness.md | 3168e220bbe382ddb45e76b5a8873fbfb5b26e4d309fba511aa7b47309be6338 |
| scripts/deployment/jdbc-readiness.py, unchanged | 13e2d084bef0e2bf1c6cb9ff64ee0dbae9650b40ccaa1091afc8b75365670051 |
| scripts/deployment/RuntimeJdbcProbe.java, unchanged | e2cd662de58fc10945e61e10cd497e2573b82eb8b6b074174c5ea2d4a2a3a8d5 |
| scripts/deployment/RuntimeSchemaProbe.java, unchanged | 0a9c8676e2cdaa2379fc578bebf6d161dad3f9cf8c9c5dc4eacb6c9f1411ac1a |
| scripts/deployment/RuntimeSchemaValidatorHarness.java, unchanged | 1a626af0f9ca3a53b842713f13d120c97f81196ed625825702761ed33b6a2587 |
| scripts/deployment/production-bootstrap.py, unchanged | 7333b068d0264045b8130519afc6e0f62589912ba2ef0b31772ff84033960a1b |
| scripts/deployment/validate_bundle.py, unchanged | 19361e515e694718b509e28c6223a3ffc0bf647ddf6bb972f8aa4904cc1cb593 |

Both v4/v5 manifests and every recorded generated file were rehashed. All 171 generated files match by relative name and content across v4/v5; v5 has 178 staged inputs. Original v4 manifest remains `b2fc53ff9f8f3b25a8ea687cde69196a140083182d6bc02c49c1a30ab4ac6cba`. The wrapper diff from pinned v4 contains the one allowed line plus WI/docstring/output/review identifiers only. No Java, private-path, child-process or SQL change is concealed by this correction.

## Independent Test Evidence

PG verified on Windows/PowerShell, Java17.0.12, 2026-10-11 02:33 KST; HEAD `58b1011dad796017e47dbcc82a4225ca449bcdf6`. Existing prepared v5 was reused. The review runner replaces only test setup, explicitly fails any `prepare()` call, and confirms identical plans before/after. Only synthetic harnesses are compiled.

| Check | Result |
|---|---|
| Exact-v5 focused suite | 26/26 PASS in 33.317s; zero failures/errors/skips |
| Full public renderer fixture | 83 keys; v5 accepts; pinned v4 reproduces ENV_KEYS; private reads and child execution prohibited in fixture validation |
| Negative matrix | 12 alternative locations, 13 override/injection lines, two unsafe settings, two duplicate forms and nine malformed forms rejected |
| Real validator, exact v5 libraries/classes | Matching synthetic metadata accepted; missing table/column rejected; Connector/J excluded and no real driver registered |
| Original v3 real validator | Preserved type failure reproduced after original inventory rehash |
| Offline mapping | MAPPING_READY without connection, included in the 26 tests |
| Exact-bundle approval check | Actual v5 bundle accepted; old v4 bundle rejected for the v5 identity |
| Release check / final plan | Backend638/frontend336/SPA143 and JAR pin match; exact v5 PLAN_ONLY |
| Unchanged JDBC guard suite | Prior PG007 independent 29/29 retained on matching source pins; not rerun or added to WI012 test count. SE011's 29/29 is separate evidence. |

```powershell
python -B build/WI012-pg-review-7cb4a8e2/run_review.py
python -B scripts/deployment/release-readiness.py plan --output build/WI011-schema-v5
```

[Runner](../../build/WI012-pg-review-7cb4a8e2/run_review.py), [26-test log](../../build/WI012-pg-review-7cb4a8e2/focused-106c38e5d917419ca2347ae6663b6370.log), [bounded result](../../build/WI012-pg-review-7cb4a8e2/result-106c38e5d917419ca2347ae6663b6370.json). The result records 33.714s including final identity checking. Synthetic fixture contents are neither persisted nor printed.

## Limits, Rollback and Handoff

- [x] Exact-value correction, complete public fixture, negative matrix and legacy failure independently verified.
- [x] Exact compiled v5, WI012 approval binding and unchanged safety pins checked.
- [x] Only own WI012 pair and unique synthetic build outputs written; sources, candidates, operators, shared REQ/index and historical evidence preserved.
- [ ] Future target validation and exact operator review: NOT_RUN / not requested in WI012.

This PASS covers code/candidate only. The old rev3 operator and old approvals are invalid for v5; do not substitute new paths/hashes silently. Any future native check needs new explicit access/secret-use approval, fresh target/capture/identity gates and a separately reviewed exact signal-aware lifecycle with independent bounded stop/mask, process-group cleanup and recovery access. No private environment edit or DB change is authorized to bypass a failure.

No private configuration, real secret, DB, network, SSH, browser, installation, startup, commit, push, deletion or descendant action. No Linux filesystem/process proof, coverage measurement, full application build or broad security audit. Matching validator metadata is synthetic rather than canonical DDL. The four historical frontend exclusions remain unchanged; input matching is not full build reproducibility. Canonical fresh manifest remains UNRECORDED, app_runnable=false and production HOLD.

Rollback: stop selecting v5 on drift/failure, retain all evidence, and request bounded correction/re-review; never fall back to v3/v4 or replay bootstrap. Return immediately to MA for the existing WI008 final integration. REQ remains active/HOLD pending separate target authority; PG creates no descendant.

## Reference Documents

Retained injection: Tier0 STD-001 core-principles, STD-004 documentation-standards, STD-002 development-standards, STD-005 glossary; Tier1 PG role/full security policy; Tier2 approved REQ, WI012 handoff, WI011 evidence, preserved WI007/WI010 evidence, public renderer/contract, test and create-wi-evidence-pack skills, runbook. Test skill loaded before tests; evidence-pack skill used after checking the generated handoff. No context or private configuration was inferred from historical summaries.

## Related Documents

- [WI012 handoff](WI-20261011-ATS-012-handoff.md): Approved local scope and output contract.
- [WI012 summary](../user/WI-20261011-ATS-012-summary.md): Bounded user-facing verdict.
- [WI011 evidence](WI-20261011-ATS-011-evidence-pack.md): Implementation candidate.
- [WI010 historical review](WI-20261011-ATS-010-evidence-pack.md): Preserved earlier result, not current execution authority.
- [Current REQ](../user/REQ-20261011-ATS-001.md): Attributed MA outcome and new local-only approval.
- [Runbook](../../deploy/single-vps/runbooks/release-readiness.md): Future target/operator boundaries.
