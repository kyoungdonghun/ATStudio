---
version: 1.0
last_updated: 2026-10-11
project: ATS
owner: SE
category: evidence-pack
status: confirmed
dependencies:
  - path: WI-20261011-ATS-009-handoff.md
    reason: Approved bounded correction and WI010 chain
  - path: WI-20261011-ATS-007-evidence-pack.md
    reason: Independent PG-007-01 diagnosis
  - path: WI-20261011-ATS-006-evidence-pack.md
    reason: Unchanged isolation rationale and exclusions
---

# Evidence Pack: WI-20261011-ATS-009

> Purpose: Record the PG-007-01 delta and exact candidate for WI010 review.

## Summary / Scope

TL;DR: corrected local candidate; native NOT_RUN; PG010 pending. SE verified on
2026-10-11 approximately02:02-02:07 KST, Windows/PowerShell, Java17.0.12;
HEAD main/58b1011dad796017e47dbcc82a4225ca449bcdf6. User correction approval and
existing WI009 handoff confirmed. No native/network/DB/private-env/SSH/browser,
app-source build, dependency, install, commit/push/delete or descendant action.

- [x] Correct two version setting types; real-validator positive/negative regression.
- [x] Fresh v4, exact hashes, mapping smoke, focused and reused suites.
- [x] WI010-only approval binding and signal-aware MA cleanup conditions.
- [ ] PG010/native result: outside SE execution; immediately hand back to MA.

Injected Tier0 STD-001/004/002/005, SE role and full security policy retained.
Skills: existing create-wi-handoff-packet output, test/build-check scoped to
unittest/javac, create-wi-evidence-pack for this new output. No broad document
reload. [WI006](WI-20261011-ATS-006-evidence-pack.md) owns unchanged design;
[WI007](WI-20261011-ATS-007-evidence-pack.md) owns the preserved HOLD and audit.

## Delta Pointers

| File / line | Change |
|---|---|
| RuntimeSchemaProbe.java:59 | Only major/minor settings changed to strings8/0 at lines59-60 |
| release-readiness.py:62,140,230 | New WI009 outputs and WI010 review/check binding; old WI007 rejected |
| RuntimeSchemaValidatorHarness.java:158 | Adapt PG's ActualValidatorHarness; actual settings/validator, no routing double/control override |
| test_release_readiness.py:184,202,206 | Bound real-validator subprocess, matching/missing cases, original v3 regression |
| release-readiness.md:152,165 | New payload, signal-aware exact process group cleanup/reaping and independent stop/mask |

Only three existing mutable helper/test files changed; RuntimeSchemaProbeHarness
and old JDBC helpers remain unchanged. New focused harness, runbook and own
deliverables are the remaining writes. PG's harness/reproducer and old WI006/007
records were not edited. Legacy test rehashed every v3 generated file against its
unchanged artifact manifest before using the original compiled classes.

## Exact Identity

Candidate: `build/WI009-schema-v4`;171 generated files plus artifact.json,
178 staged inputs including sources/JAR/WI026 manifest. Bundle SHA-256:
`f2e59839caa096fff83b427ad47563be8f7b96c58bdd2825bda9216e109eaeec`.

| Public file | SHA-256 |
|---|---|
| scripts/deployment/release-readiness.py | a1e0492623901ab71086920dddc1c7100265b5016d281abe4410e82dc363f96a |
| scripts/deployment/RuntimeSchemaProbe.java | 0a9c8676e2cdaa2379fc578bebf6d161dad3f9cf8c9c5dc4eacb6c9f1411ac1a |
| scripts/deployment/test_release_readiness.py | cd1aa3a7b26e651d7e0d6e11570f840d798b7192a696c1ce9b08bc7ac6fd84ff |
| scripts/deployment/RuntimeSchemaValidatorHarness.java | 1a626af0f9ca3a53b842713f13d120c97f81196ed625825702761ed33b6a2587 |
| deploy/single-vps/runbooks/release-readiness.md | 36530c512fe04d702df125e1b1af609a31f428b773248bad3830911c7fcf6579 |
| build/WI009-schema-v4/artifact.json | b2fc53ff9f8f3b25a8ea687cde69196a140083182d6bc02c49c1a30ab4ac6cba |

Full compiled/extracted file hashes are in the [v4 manifest](../../build/WI009-schema-v4/artifact.json).
Backend638/frontend336/SPA143 exact recorded comparisons still match; JAR remains
`517b745479792a7eae6ce75357c7d1299ab644b50d5bc5b331fea384ccb10e0a`.
The [four exact coverage exclusions](WI-20261011-ATS-006-evidence-pack.md#release-identity-and-exclusions)
are unchanged, not a broad glob or complete frontend reproducibility claim.
Original v3 artifact hash remains
`ffadd34b644481bed92a7688f740270047adede4da458f9805230e68fb3c7e21`.

## Tests / Reproduction

| Check | Result |
|---|---|
| New focused suite |21/21 PASS,43.763s; includes actual validator and old WI007 rejection/new WI010 acceptance |
| Reused JDBC suite |29/29 PASS,56.348s; total50/50,0 failures/errors/skips |
| Exact v4 mapping smoke |MAPPING_READY, no connection |
| Exact v4 real validator |Matching synthetic metadata PASS; missing table and column rejected with exact expected categories |
| Original v3 real validator |Expected ClassCastException code,0 connections; unchanged compiled candidate |
| Final check / plan |INPUTS_MATCH / exact v4 PLAN_ONLY, exit0 |

```powershell
python -B -m unittest discover -s scripts/deployment -p test_release_readiness.py -v
python -B -m unittest discover -s scripts/deployment -p test_jdbc_readiness.py -v
python -B scripts/deployment/release-readiness.py check
python -B scripts/deployment/release-readiness.py plan --output build/WI009-schema-v4
python -B scripts/deployment/release-readiness.py smoke --output build/WI009-schema-v4
python -B -c "import sys,shutil; from pathlib import Path; sys.path.insert(0,'scripts/deployment'); import test_release_readiness as t; case=t.CompiledTests(); case.java=Path(shutil.which('java')).resolve(strict=True); code,raw=case.run_actual_validator(t.ROOT/'build/WI009-schema-v4'); print('FINAL_V4',code,raw.decode('ascii').strip()); raise SystemExit(code)"
```

Safe bounded logs: [final v4](../../build/WI009-validator-8575d04bbc574ca0ab18e7fe46d567a6/result.log),
[v3 reproduction](../../build/WI009-validator-e43ba7ac405644f6af9f1ecc05d628c7/result.log),
[red-phase result](../../build/WI009-validator-7505becadfe1448197d32d556dc2872f/result.log).
No Connector/J or other registered real driver is permitted in the harness;
timeout60s/heap256MiB/output256bytes. Synthetic matching metadata derives from
the model, not canonical DDL. Assertions also verify read-only is never removed,
connection closure and observed read-only commit/rollback transaction control.
Initial harness compilation needed explicit UTF-8; its missing-schema message
prefix was corrected against bundled bytecode before the full green rerun.
No coverage, whole-app or native Linux/DB validation was added.
Scoped direct checks passed for all seven changed/new files: whitespace, final
newline, document metadata and local links, including untracked files. Scoped
`git diff --check` exited0.

## Operator Conditions / Handoff

WI010, `pg_wi010_exact_bundle_pass`, fresh bundle/host identity and all prior
guards are required. Both WI007 review and old output paths fail closed. MA's
signal-aware lifecycle is prepared for PG010 review, not reviewed by SE: own
wrapper/Java process group, SIGINT/SIGTERM/SIGHUP/deadline handling, bounded
termination/escalation/reaping, independent bounded stop and mask, recovery
console for SIGKILL/connection loss. Preserve restricted TLS/timer and historical
private identity provenance. See [runbook](../../deploy/single-vps/runbooks/release-readiness.md).

Exact native payload, **NOT RUN**, only through that PG-approved supervisor:

```sh
/usr/bin/env -i PATH=/usr/bin:/bin HOME=/root LC_ALL=C /usr/bin/python3 -I -B /root/wi009/scripts/deployment/release-readiness.py probe --root /root/wi009 --output build/WI009-schema-v4
```

Rollback: stop selecting v4, retain artifacts, patch only approved helper files
and produce a new reviewed candidate. Do not fall back to blocked v3, replay
bootstrap or change DB/config. Native/canonical schema, app startup, production
and site-live remain unproven. WI009 immediately triggers WI010 via MA; WI008
waits for independent review plus actual MA outcome/cleanup. No descendants.

## Related Documents

- [WI009 handoff](WI-20261011-ATS-009-handoff.md): Approved correction scope.
- [Summary](../user/WI-20261011-ATS-009-summary.md): Bounded completion report.
- [PG harness](../../build/WI007-pg-20261011-0153/ActualValidatorHarness.java): Reused synthetic approach.
