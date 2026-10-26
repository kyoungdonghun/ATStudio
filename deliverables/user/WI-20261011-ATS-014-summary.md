---
version: 1.1
last_updated: 2026-10-11
project: ATS
owner: PG
category: work-summary
status: confirmed
dependencies:
  - path: REQ-20261011-ATS-001.md
    reason: Fresh approved MA native window
  - path: ../agent/WI-20261011-ATS-014-evidence-pack.md
    reason: Exact operator, test evidence and resolved preflight findings
---

# WI014 Independent Operator Review

> Purpose: Separate conditional local review approval from fresh target execution proof.

## Verdict

**TL;DR: final conditional PASS. The exact WI013 operator passed 19/19 independent offline tests; the corrected preflight passed 38/38 bounded tests. Both initial HOLD findings are closed. Fresh target gates and staging conditions remain mandatory; PG native NOT_RUN.**

1. Resolved: `ss` now requires rc0 and at least five fields per nonempty row; the failed-command counterexample rejects.
2. Resolved: the mysqlx predicate accepts only literal `--mysqlx=OFF` within its normalized prefix families; conflicting, unknown, underscore and loose variants reject.

Original counterexamples and correction tests use inert values only. The full script received static/syntax review, not native execution. Post-start database guards remain present. Exact `--local-infile=OFF` and identical loopback bind duplicates are acceptable when conflicting values are rejected. No server configuration change is requested.

[Final supplied preflight](../../build/WI014-pg-preflight-final-b89e4c1a/ma-preflight-final.py) SHA-256: `fbd216f27939eb75799667437d07c4a5dc25d6d16414125d5f1767352fa9b504`.

## Exact Operator

[ma-native-operator-v5.py](../../build/WI013-native-v5/ma-native-operator-v5.py)

SHA-256: `64dc16d9b74315647497b9d335e5e309e27f544e03a5f76df35cb66fe0288136`.

It targets the unchanged WI012-reviewed v5 bundle `463c95667ee51e5fe2f01303a43ccebe0c0547375a374730c511c924212c7e54`. Wrapper approval remains WI012; WI014 reviews the lifecycle separately.

## Boundary and Handoff

PG performed no native operation. MA target/upload/preflight receipts are separately attributed. Use the exact reviewed bytes and repeat fresh target/capture/access checks, root-stage inventory/hash/plan verification and Linux no-connect mapping smoke before activation. Unknown cleanup remains HOLD; app startup, DB alteration and automatic retry remain excluded. No pending PG-owned exec session remains.

History, candidate and operator are unchanged. MA owns the actual lifecycle, independent cleanup and subsequent WI015 integration.

## Related Documents

- [Evidence Pack](../agent/WI-20261011-ATS-014-evidence-pack.md): Tests, exact pins, staging conditions and reproductions.
- [Handoff](../agent/WI-20261011-ATS-014-handoff.md): Scope and ownership.

## Replacement PG Addendum: 2026-10-11T01:24Z

**Current verdict: HOLD**, superseding the earlier conditional PASS for this preflight. Prior findings, tests and staging history above are preserved.

The replacement PG independently exercised only the two extracted predicates: **289/289 PASS** closes PG-014-01 and PG-014-02 for the stated rc/row-shape and mysqlx-family contracts. No repeat of the 19 operator tests or broad audit was performed.

However, **8 additional conflict probes failed**: `mysql_config` accepts safe literals alongside `--local_infile=ON`, `--loose-local-infile=ON`, `--bind_address=0.0.0.0` or `--loose-bind-address=0.0.0.0`, in either ordering. New PG-014-03 requires the same conservative alias/conflict rejection for local-infile and bind-address, while retaining identical canonical loopback duplicates. PG did not change the preflight or server configuration.

Full-script byte hash: `fbd216f27939eb75799667437d07c4a5dc25d6d16414125d5f1767352fa9b504` (6,097 bytes, LF). [Independent passing matrix](../../build/WI014-final-replacement-pg-6f91c2.json) and [failing conflict probes](../../build/WI014-final-alias-probes-0c423e.json) contain per-case evidence.

**Native NOT_RUN.** After corrected bytes pass independent review, fresh MA-only target/capture/access checks, exact operator/bundle pins, root-stage verification, Linux no-connect mapping and recovery readiness remain mandatory. Target state is attributed to MA, not locally verified. MA owns execution/cleanup and subsequent WI015 integration; this review grants no native advancement, app startup or automatic retry.

## Final Superseding Addendum: Unified Predicate

**CONDITIONAL PASS; PG-014-03 closed.** Final pure-AST tests passed **1,058/1,058**: all eight alias counterexamples reject, safe duplicates accept and nonzero listener rc rejects. The precise four-option duplicate-bind input was separately confirmed. Prior HOLD evidence is preserved and superseded only for these corrected bytes.

[Final complete script](../../build/WI014-final-unified-preflight-32ad91.py): **5,797 bytes, LF**, SHA-256 `a5ec7a1e415116dcef3090c6603624db95d14a8c551118cc48197781b4c50479`. [Test results](../../build/WI014-final-unified-tests-32ad91.json).

**PG native NOT_RUN.** Exact pins, fresh MA target/capture/access checks, root-stage inventory/hash/plan verification, Linux no-connect mapping and recovery readiness remain mandatory. MA owns execution/cleanup and WI015 integration; any failed gate remains HOLD. No server/candidate changes, broad audit or operator-test rerun.

## Latest Addendum: Exact Loopback X Bind

**CONDITIONAL PASS, 52/52 synthetic checks passed.** Adding only required literal `--mysqlx-bind-address=127.0.0.1` admits the known five entries while retaining mandatory mysqlx OFF and rejecting external X binds, conflicting/alternate/loose aliases and failed listener commands. Prior HOLD evidence is preserved; this supersedes the A5 script identity.

[Full final script](../../build/WI014-final-xbind-preflight-80d4a7.py): **5,831 bytes, LF**, SHA-256 `6b1d7ed691b37043af778598decc46bf73e12b7c08f47b7d0f1128fce44de383`. [Test results](../../build/WI014-final-xbind-tests-80d4a7.json).

**PG native NOT_RUN.** Stage/plan/MAPPING_READY and inactive/masked DB are MA-reported, not PG-verified. Exact pins, fresh target/capture/access/recovery gates and runtime guards remain required. MA owns native execution/cleanup and WI015 integration; any failed gate remains HOLD. No server/candidate changes.
