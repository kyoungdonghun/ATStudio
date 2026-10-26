---
version: 1.1
last_updated: 2026-10-11
project: ATS
owner: PG
category: evidence-pack
status: confirmed
dependencies:
  - path: WI-20261011-ATS-014-handoff.md
    reason: Approved independent operator and staging review
  - path: WI-20261011-ATS-013-evidence-pack.md
    reason: Exact operator and inert test suite
  - path: WI-20261011-ATS-012-evidence-pack.md
    reason: Unchanged v5 candidate review
  - path: ../user/REQ-20261011-ATS-001.md
    reason: Fresh MA-only native window authority
---

# Evidence Pack: WI-20261011-ATS-014

> Purpose: Bind independent review to the v5 operator and supplied MA staging/preflight.

## Findings and Verdict

**TL;DR: final conditional PASS for the exact WI013 operator, final supplied preflight and conditioned staging recipe. PG-014-01/02 are resolved by the independently tested correction below. Fresh target gates remain mandatory; native NOT_RUN by PG.**

The following initial HOLD and counterexamples are retained as historical review evidence, superseded only by the final-preflight addendum below.

| Finding | Evidence, impact and required correction |
|---|---|
| PG-014-01: failed listener command can report no ports | Supplied MA preflight uses `q(["ss","-H","-ltn"])[1]` without checking return code. Synthetic `(1, "")` produces an empty set and a true no-port check. Require rc0 before accepting parsed listener output; failure must HOLD. |
| PG-014-02: mysqlx presence check accepts conflicting options | Supplied `any(x in ("--mysqlx=0","--mysqlx=OFF","--skip-mysqlx") for x in lines)` accepts both `--mysqlx=OFF` and `--mysqlx=ON`. Require every relevant mysqlx enable/disable option to match the explicit disabled allowlist and reject conflicting/unknown forms before activation. |

Both counterexamples were reproduced without commands, processes or native access: [predicate review](../../build/WI014-pg-review-a740be2d/review_staging_and_predicates.py). These are pre-activation gate findings, not a claim that post-start database guards are missing. The exact operator invokes `database_guard(fresh=False)` and `verify_grants(locked=False)` before secret use; the retained guard checks global local_infile and rejects active mysqlx/capture plugins. Those remain valid defense in depth.

The reported `--local-infile=OFF` is an acceptable explicit disabled spelling. Identical `--bind-address=127.0.0.1` duplicates are acceptable. Require all corresponding options to match those explicit safe values; do not accept a safe occurrence alongside an override. MA reports correcting local-infile conflict handling and a fresh all-24-check PASS at 2026-10-11T01:12:50Z. That target receipt does not establish that the two predicates above were corrected. No server configuration change is requested.

## Final Preflight Addendum

**Conditional PASS; no remaining code-correction blocker in this bounded review.** PG independently tested the exact final supplied port block and mysqlx predicate after the user's sole final replacement. Original operator, candidate, staging template and historical test logs remain unchanged.

- PG-014-01 closed: failed empty/populated `ss` output and rows shorter than five fields reject; successful empty output and normal IPv4/IPv6 TLS rows accept; all five forbidden ports reject.
- PG-014-02 closed: require literal `--mysqlx=OFF`; normalize underscores in the option name and reject every nonliteral member of the eight supplied mysqlx/skip/enable/disable/loose prefix families. OFF alongside ON, unknown values, family suffixes, bare options and underscore/loose variants reject. Identical OFF and loopback-bind duplicates accept.
- **38/38 PASS**, zero failures/errors/skips, 0.006s. Tests also check failed defaults command, missing required options, bind/local-infile conflicts and whole-script syntax/read-only command inventory. The full preflight was statically reviewed and syntax-compiled, not executed; only the extracted exact predicate AST ran against inert values and fake `q` results.
- [Final supplied script](../../build/WI014-pg-preflight-final-b89e4c1a/ma-preflight-final.py), UTF-8/ASCII with LF: SHA-256 `fbd216f27939eb75799667437d07c4a5dc25d6d16414125d5f1767352fa9b504`.
- [Independent tests](../../build/WI014-pg-preflight-final-b89e4c1a/test_final_preflight.py): SHA-256 `e8a9a4842ebfb6d535b61cb28304eb854f471b15dbd4bbebee346fd09c761268`. [Log](../../build/WI014-pg-preflight-final-b89e4c1a/tests-193d3ccbb50e401b9c3bfd7fcde545d8.log), [result](../../build/WI014-pg-preflight-final-b89e4c1a/result-193d3ccbb50e401b9c3bfd7fcde545d8.json).

```powershell
python -B build/WI014-pg-preflight-final-b89e4c1a/test_final_preflight.py
```

Operator SHA-256 `64dc16d9b74315647497b9d335e5e309e27f544e03a5f76df35cb66fe0288136`, its test source, v5 artifact manifest and wrapper were rehashed unchanged. Prior independent 19/19 operator tests remain applicable; they were not rerun or represented as new native proof.

MA may advance only with these exact reviewed bytes, the existing staging conditions, fresh current-window target/capture/access PASS, root-stage hash/inventory/plan verification and Linux no-connect mapping smoke before activation. Any changed input or failed gate is HOLD. No automatic start/retry, configuration change or application startup is authorized by this review. No pending PG-owned exec session remains. Native target behavior and privileged-host capture remain unproven by these offline checks.

## Exact Identities

| Item | SHA-256 |
|---|---|
| build/WI013-native-v5/ma-native-operator-v5.py | 64dc16d9b74315647497b9d335e5e309e27f544e03a5f76df35cb66fe0288136 |
| build/WI013-native-v5/test_native_operator.py | db84b04c2dbe3fd12bec6df7a4ad0e7093a5dfdcea61b41d7bdcd48383f494e0 |
| scripts/deployment/release-readiness.py | 5221e4c80ebc7a82aaade27e27b1c7f91cb6cea37980f44f3a73592c3ca4c7cd |
| scripts/deployment/jdbc-readiness.py | 13e2d084bef0e2bf1c6cb9ff64ee0dbae9650b40ccaa1091afc8b75365670051 |
| build/WI011-schema-v5/artifact.json | 6749b2667e903635c0fe1849e66f7a261b0499a7ed29a19daa5e4da248836ba8 |
| build/WI014-pg-review-a740be2d/ma-staging-template.py | 889dba388a6c15c6247f3f30171a6f5836b190b8635a8192fd4b64a948e87fa5 |

Final local plan matches v5 bundle `463c95667ee51e5fe2f01303a43ccebe0c0547375a374730c511c924212c7e54`. Operator root/output are `/root/wi011` and `build/WI011-schema-v5`; staged operator location is `/root/wi013/ma-native-operator-v5.py`. The new operator keeps the pinned boot and changes only five v5 identity strings plus explicit exit0 for PASS/exit2 for HOLD relative to preserved rev3. No silent source/operator substitution was performed.

## Independent Tests and Review

PG, Windows/PowerShell, 2026-10-11; HEAD `58b1011dad796017e47dbcc82a4225ca449bcdf6`.

- **19/19 PASS**, zero failures/errors/skips, 0.492s suite time (0.494s runner). [Log](../../build/WI014-pg-review-a740be2d/operator-tests-ea08fafbd1b346e19e5d50f2507ea6c3.log), [result](../../build/WI014-pg-review-a740be2d/result-ea08fafbd1b346e19e5d50f2507ea6c3.json).
- Exact-source delta and real v5 approval parser checked. Approval remains WI012 with `pg_wi012_exact_bundle_pass`; WI014 is the separate operator review, not the wrapper approval identifier.
- Inert extracted AST tests cover success, schema/protocol rejection, invalid JSON, launch failure, timeout, independent stop/mask failure and timeout, reap/state/TLS failure, interrupt and untouched cleanup. Every HOLD exits2 and attempted execution is not labelled NOT_RUN.
- Reaper tests use fake clock/process/killpg for TERM, KILL escalation, remaining group and wait timeout. No real signal, subprocess, private file or native top-level operator setup is executed.
- Static review confirms historical identity provenance, exact source/receipt pins, internal password framing/validation, exclusive approval creation, before/after observation checks, TLS retention and fixed exception reporting. Existing helper read-only observations remain separate from the Java server-session read-only guarantee.
- Whole-wrapper deadline210s, stop90s/mask20s and bounded TERM/KILL/wait retained. Operator output checks are post-capture length checks, not streaming allocation bounds. Native OS delivery, orphan adoption, SSH loss and filesystem/service behavior are not proven locally; recovery console remains mandatory.

```powershell
python -B build/WI014-pg-review-a740be2d/run_operator_review.py
python -B build/WI014-pg-review-a740be2d/review_staging_and_predicates.py
python -B scripts/deployment/release-readiness.py plan --output build/WI011-schema-v5
```

## Staging and Target Conditions

[Saved supplied template](../../build/WI014-pg-review-a740be2d/ma-staging-template.py) was syntax-checked, not executed. Its hash applies to the template containing `PLAN_JSON`; replace only that data placeholder with the exact independently pinned 178-entry `input_sha256` object, not executable text, wildcards or private inputs.

Staging checks UID1001 and upload0700, exact 177-file inventory excluding the retained JAR, no links/special files, trusted ancestry, new root paths, safe relative names and expected hashes. JAR is copied after pin verification, not hardlinked. Destination files use O_EXCL/O_NOFOLLOW0600 under root0700 directories, fsync and read-back hashes; operator is separately pinned. Partial failure is HOLD: preserve partial stage, do not overwrite/delete or silently change paths.

This recipe assumes exclusive maintenance and quiescent upload ownership throughout inventory/copy; it is not a hardened concurrent-untrusted-writer copier. Keep uploads and other atadmin file activity stopped. Recheck exact root inventory/permissions, v5 plan, operator hash and Linux no-connect mapping before activation. Destination existence requires explicit disposition, not reuse by this recipe.

MA reports uploaded177 public files/43,350,242 bytes, operator uploaded to management home, same pinned target/boot, MySQL masked/off, TLS unchanged, recovery root tty1 and three metadata-classified kernel psimon threads. These are attributed MA observations. PG did not inspect that host, private identities or uploaded contents. Root staging/native remained unexecuted in the supplied updates.

Before any native operation, use the corrected exact preflight from the addendum and repeat the current-window checks. Preserve explicit secret-use authority, isolated Python/environment, trusted interpreter/stage and root-private paths, capture checks, recovery console and exclusive maintenance. Kernel psimon classification requires all supplied metadata facts per PID; it is not a blanket process-name exception or privileged-capture guarantee. Keep active restricted Nginx/Certbot; never invoke old host_guard or mutate/bootstrap paths.

MA owns independent postcheck, exact-key/session/agent restoration and WI015 integration after the actual result or HOLD. This local review neither performs those actions nor grants automatic retry, app startup or DB/config modification.

## Scope, References and Rollback

- [x] Exact operator/v5 pins and narrow delta verified; independent offline tests complete.
- [x] Supplied staging and preflight reviewed; material findings reported immediately.
- [x] Corrected exact preflight predicates independently reviewed: 38/38 PASS; both findings closed.
- [ ] Native schema and cleanup result: MA-only, NOT_RUN in PG review.

All four Tier0 documents were reinjected before PG/security/task context: STD-001, STD-004, STD-002, STD-005. PG role, full security policy and evidence skill retained. WI014 handoff/REQ approval, WI013 source/tests/evidence, WI012 history and runbook read; test skill loaded before testing. Additional policy pointers were not broadly re-audited. Evidence skill used for this pair.

Writes are limited to this WI014 pair and fresh `build/WI014-pg-review-a740be2d` / `build/WI014-pg-preflight-final-b89e4c1a` outputs. No source/candidate/operator/prior-WI history edits, remote/SSH/browser/private data/DB/service/agent operation, install, deletion, commit/push or descendant. WI012's 26-test candidate PASS remains historical and unchanged; it is not a new native result. No production/canonical schema/application readiness claim.

Rollback on any fresh gate failure is withholding execution and preserving evidence for a bounded decision. The two requested corrections are now closed. Do not fall back to an old operator or alter configuration to obtain PASS. Return to MA; WI015 integration follows the final review and actual MA outcome/cleanup. REQ remains active.

## Related Documents

- [Handoff](WI-20261011-ATS-014-handoff.md): Authorized review boundaries.
- [Summary](../user/WI-20261011-ATS-014-summary.md): User-facing verdict.
- [WI013 evidence](WI-20261011-ATS-013-evidence-pack.md): Operator provenance.
- [WI012 evidence](WI-20261011-ATS-012-evidence-pack.md): Immutable candidate review.
- [Runbook](../../deploy/single-vps/runbooks/release-readiness.md): Lifecycle and recovery requirements.

## Replacement PG Bounded Addendum: 2026-10-11T01:24Z

**Current verdict: HOLD.** This appended review supersedes the earlier conditional PASS only for the supplied preflight's conflict rejection. The initial HOLD, prior 19/19 operator review, prior 38/38 correction review and staging review remain intact as historical evidence. No operator, candidate or server change was made.

### Scope and Independent Evidence

- Owner/environment: replacement PG, local Windows/PowerShell, HEAD `58b1011dad796017e47dbcc82a4225ca449bcdf6`; synthetic verification at `2026-10-11T01:24:14Z` and `2026-10-11T01:24:46Z`. No target verification occurred.
- Handoff confirmed: WI014, same approved scope; all four Tier 0 texts, PG role, security policy and evidence skill were injected in this request. The test skill was read. No subdelegation, broad source audit, repository validator or repeat of the 19 operator tests.
- The existing [full corrected script](../../build/WI014-pg-preflight-final-b89e4c1a/ma-preflight-final.py) matches the supplied PRECHECKSCRIPT. Actual saved bytes: 6,097, UTF-8/ASCII, LF; SHA-256 `fbd216f27939eb75799667437d07c4a5dc25d6d16414125d5f1767352fa9b504`. This is a whole-file byte hash, not an AST or predicate hash. No duplicate executable preflight was created.
- Only the three pure port assignments and two predicate expressions were AST-extracted; executable calls were allowlisted to pure string/collection operations. The full script was neither imported nor executed. No source `q`, subprocess, private-file, network or native call ran.
- [Independent matrix](../../build/WI014-final-replacement-pg-6f91c2.py), SHA-256 `79aabd14ceea7e43c2469e74d2f0eaba61b0616c94854b4564028e8533b3214f`: **289/289 PASS**, [per-case result](../../build/WI014-final-replacement-pg-6f91c2.json).
- [Exact-value alias probes](../../build/WI014-final-alias-probes-0c423e.py), SHA-256 `25e3616d3af624fd10481f1bea728d31d0369727c4f9b0093999719318710d15`: **0/8 PASS, 8 failed expectations**, [per-case result](../../build/WI014-final-alias-probes-0c423e.json). These are intentional fail-closed expectations demonstrating accepted conflicts, not a harness crash.

### Findings and DoD

| Item | Independent result |
|---|---|
| PG-014-01 | Closed for the requested minimum shape contract: nonzero rc rejects empty/populated output; every row must have at least five fields; all five prohibited ports reject for IPv4/IPv6/wildcard examples. Empty rc0 output accepts. This is not complete semantic validation of arbitrary five-field text; trusted authentic `ss` output remains a condition. |
| PG-014-02 | Closed for the supplied eight mysqlx prefix families: only literal `--mysqlx=OFF` accepts; conflicting values, bare/unknown/suffixed options, underscore and loose forms reject, regardless of placement before/after the safe entries. Identical safe duplicates accept. |
| PG-014-03: adjacent exact-value conflict bypass | **HOLD:** the same `mysql_config` expression accepts each of `--local_infile=ON`, `--loose-local-infile=ON`, `--bind_address=0.0.0.0`, `--loose-bind-address=0.0.0.0` alongside the three required safe literals, in both orderings. The bind/local-infile checks recognize only their literal hyphenated `--name=` prefix, unlike the normalized mysqlx check. Therefore this predicate does not itself establish the requested exact-OFF/no-conflict boundary. |

Minimal synthetic counterexample: rc0 with `['--bind-address=127.0.0.1', '--local-infile=OFF', '--mysqlx=OFF', '--local_infile=ON']` returns **true**. This proves a predicate acceptance gap only; no claim is made that these options are present on the target or that an actual MySQL process was configured or started. Canonical `--local-infile=ON` and non-loopback canonical binds reject; identical canonical loopback duplicates accept.

Required bounded correction: apply conservative option-name/alias-family recognition to local-infile and bind-address too, rejecting alternate/conflicting forms unless the complete line is the respective approved literal. Keep identical canonical loopback duplicates allowed. Supply new complete-script bytes for independent recheck; do not change server configuration to manufacture PASS. The preflight source was not edited by PG.

### Commands, Conditions and Handoff

```powershell
python -B build/WI014-final-replacement-pg-6f91c2.py
python -B build/WI014-final-alias-probes-0c423e.py
```

The first invocation succeeded. The second returned nonzero with eight failed expectations (the runner requests exit2; the local tool reported exit1). Both scripts write their sibling JSON using exclusive creation and will refuse to overwrite existing evidence on rerun; use fresh WI014-named copies for another run. No dependency installation is needed.

Once the bounded correction passes, any conditional allowance still requires exact reviewed bytes, unchanged operator pin `64dc16d9b74315647497b9d335e5e309e27f544e03a5f76df35cb66fe0288136` and v5 bundle `463c95667ee51e5fe2f01303a43ccebe0c0547375a374730c511c924212c7e54`, fresh current-window target/capture/access checks, exclusive staging, root inventory/hash/plan verification, Linux no-connect mapping smoke and an available recovery console. Operator/bundle pins are inherited from the supplied handoff/history, not rehashed or re-audited in this addendum.

Current server off/masked, root recovery login, uploaded public candidate and incomplete root staging are user/MA reports, not new PG observations. Actual secret-use/temporary-SSH authority belongs to the stated MA window; PG did not exercise it. Native execution and independent cleanup are MA-only; WI015 integration follows the actual MA outcome or HOLD. Do not mark the REQ complete or start native execution on this review.

Rollback/action: withhold native advancement, preserve all prior evidence and request the bounded predicate correction. Only this EP, its summary and four fresh local `build/WI014-final-*` test/result files were written. No service/schema/app changes, secret access, native execution, purchases, deletions, commit/push or memory update.

## Final Superseding Addendum: Unified Predicate

**CONDITIONAL PASS; PG-014-03 closed.** Replacement PG verified the user's final complete script locally on 2026-10-11. This verdict supersedes the immediately preceding HOLD for the corrected bytes only; all earlier findings and logs remain historical evidence.

- [Full final script](../../build/WI014-final-unified-preflight-32ad91.py): **5,797 bytes, LF**, SHA-256 `a5ec7a1e415116dcef3090c6603624db95d14a8c551118cc48197781b4c50479`. Byte comparison proves only the requested mysql_config replacement changed; corrected port code is unchanged.
- [Independent pure-AST tests](../../build/WI014-final-unified-tests-32ad91.py): **1,058/1,058 PASS**, no failures, at `2026-10-11T01:27:04Z`; [per-case log](../../build/WI014-final-unified-tests-32ad91.json). All eight prior alias counterexamples now reject; nonzero listener rc, malformed short rows and prohibited ports reject. Exact safe literals and duplicates accept. A subsequent direct AST check also passed the precise four-option input: two identical loopback binds, local-infile OFF, mysqlx OFF.
- Test SHA-256: `30d7865d42c465ae6a07564d2adafa2ee1cbd04c34bc18b7c03ce465a0ff6de5`. Command: `python -B build/WI014-final-unified-tests-32ad91.py`; log creation is exclusive, so reruns require a fresh test filename. Only extracted pure predicate/setup AST executed, never the full preflight, its commands, private files or network operations. No operator-suite rerun or broader audit.
- Conditions remain: exact final script/operator/v5 pins, fresh MA target/capture/access PASS, exclusive root staging with inventory/hash/plan verification, Linux no-connect mapping and recovery console. Unrelated mysqld options remain outside this bounded guard; existing runtime guards still apply. Any changed bytes or failed gate means HOLD.
- **Native NOT_RUN by PG.** No server/candidate/configuration changes. MA alone owns native execution, independent cleanup and subsequent WI015 integration; no automatic retry, app startup or REQ completion follows from this review. On gate failure, withhold execution and preserve evidence.

## Latest Addendum: Exact Loopback X Bind

**CONDITIONAL PASS**, replacement PG, `2026-10-11T01:32:39Z`, local synthetic review only. The final required set adds exactly `--mysqlx-bind-address=127.0.0.1`; `--mysqlx=OFF` remains mandatory. This supersedes the prior A5 preflight identity, preserving all HOLD and earlier review evidence.

- [Full script](../../build/WI014-final-xbind-preflight-80d4a7.py): **5,831 bytes, LF**, SHA-256 `6b1d7ed691b37043af778598decc46bf73e12b7c08f47b7d0f1128fce44de383`. Byte comparison against A5 proves only that literal addition; port handling is unchanged.
- **52/52 PASS**: known five entries including duplicate loopback bind accept; mysqlx ON, external X binds, alternate/loose bind/local/mysqlx forms, missing required entries, nonzero command rc, short listener rows and prohibited ports reject. Exact-set equality deliberately requires the loopback X-bind literal; it is not optional.
- [Tests](../../build/WI014-final-xbind-tests-80d4a7.py), SHA-256 `e9a322983aef6bc384bceaaee6e0d26a03881c8a22ab988b4827acac76e9369d`; [per-case log](../../build/WI014-final-xbind-tests-80d4a7.json). Command: `python -B build/WI014-final-xbind-tests-80d4a7.py`. Exclusive log creation; rerun under a fresh filename. Only allowlisted pure AST executed; no full-script import/execution, private files, network, server/config/candidate changes or broad audit.
- MA reports A5 native preflight rejected `mysql_config`, MySQL remained inactive/masked and no native operator ran. MA also reports root public stage 178 files / 113,653,377 bytes, plan PASS and Linux MAPPING_READY, with the earlier pre-creation SCP-mode halt resolved by exact owned-file hardening to 0600 before fresh staging and no partial root stage. These are attributed reports, not PG host verification; Ubuntu-default provenance was not independently researched.
- Conditions: MA must run these exact reviewed bytes with fresh target/capture/access/recovery gates and retain the reviewed operator/v5 pins, stage identity and runtime guards. Existing stage/plan/mapping reports do not waive current-window checks. **PG native NOT_RUN**; any failed gate remains HOLD. MA owns native execution, independent cleanup and WI015 integration; no automatic retry, app startup or REQ closure is granted.
