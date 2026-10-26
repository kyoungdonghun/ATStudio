---
version: 1.1
last_updated: 2026-10-11
project: ATS
owner: DocOps
category: evidence-pack
status: confirmed
dependencies:
  - path: WI-20261011-ATS-008-handoff.md
    reason: Existing skill-generated four-file integration authority
  - path: ../user/REQ-20261011-ATS-001.md
    reason: Approved scope and new local correction approval
  - path: WI-20261011-ATS-010-evidence-pack.md
    reason: Preserved historical exact-v4 and operator-rev3 review
  - path: WI-20261011-ATS-012-evidence-pack.md
    reason: Latest exact-v5 local acceptance without native operator authority
---

# Evidence Pack: WI-20261011-ATS-008

> Purpose: Integrate the actual native HOLD, cleanup and approved correction without rewriting earlier evidence.

## Summary and Scope / DoD Check

TL;DR: exact v5 local correction/review PASS; native schema NOT_RUN;
production HOLD. WI008 documentation integration is complete. REQ remains
approved/active pending separate target authority and native verification.
The earlier v4 operator execution/HOLD and cleanup are preserved below.

Evidence date: 2026-10-11. MA owns the target Linux and Windows operational
receipts at02:18-02:24 KST; SE/PG own earlier local Windows/Java17.0.12 tests.
DocOps checks only public local source/pins and these four documents on
Windows/PowerShell. Source branch/HEAD independently read:
`main/58b1011dad796017e47dbcc82a4225ca449bcdf6`. Document edit date is not a new
native verification. Unrelated dirty work and historical evidence are retained.

- [x] Integrate recorded artifacts, exact exclusions and reviewed candidate/procedure identities.
- [x] Preserve WI007 HOLD, WI009 correction and WI010 historical bounded PASS.
- [x] Supersede execution-readiness with MA/PG follow-up HOLD and separate native cleanup receipts.
- [x] Record new user approval, remaining gates and no-publication boundary.
- [x] Current four-file checkpoint validation: Tier0 presence, internal file links, supported IDs and direct metadata/whitespace checks.
- [x] WI011 implementation/WI012 exact-v5 review results received and integrated.
- [ ] Native schema acceptance and REQ closure: not established or authorized by local correction approval.

## Latest v5 Local Result Addendum

SE WI011 verified55/55 through2026-10-11 02:30 KST; PG WI012 independently
verified26/26 at02:33 KST on Windows/PowerShell, Java17.0.12. HEAD remains
`58b1011dad796017e47dbcc82a4225ca449bcdf6`. Their saved Evidence Packs and full
texts were supplied for this immediate WI012 -> WI008 integration. DocOps
rechecked the12 public pins in WI012; no application suite/build was rerun.

Exact candidate: `build/WI011-schema-v5`, bundle SHA-256
`463c95667ee51e5fe2f01303a43ccebe0c0547375a374730c511c924212c7e54`.

| Latest public file | SHA-256 |
|---|---|
| scripts/deployment/release-readiness.py | 5221e4c80ebc7a82aaade27e27b1c7f91cb6cea37980f44f3a73592c3ca4c7cd |
| scripts/deployment/test_release_readiness.py | bd8b085440767204f87545a3147ec0f55cde0c1f0c06d5a394f9d1860ac158a5 |
| build/WI011-schema-v5/artifact.json | 6749b2667e903635c0fe1849e66f7a261b0499a7ed29a19daa5e4da248836ba8 |
| deploy/single-vps/env/atstudio.env.example | 412e838b532c7249d0bbb9ca44eb114a659f7a78f1e2675bf87a676392fa18b0 |
| deploy/single-vps/env/contract.json | 9d02a317e3de26896dcfb8877b8fb5b51af7059b1c04117fa9ff170c72ec557d |
| deploy/single-vps/runbooks/release-readiness.md | 3168e220bbe382ddb45e76b5a8873fbfb5b26e4d309fba511aa7b47309be6338 |

The other six checked pins are unchanged JDBC/Java/renderer/validator files
listed in WI012. PG rehashed v4/v5 manifests and all171 generated files: identical
relative-name/content sets; v5 has178 staged inputs. Backend638/frontend336/
SPA143 and the JAR pin still match WI026, with the four exact exclusions below.
This is local preparation, not a v5 upload or full build reproducibility.

| Owner / exact-v5 check | Result and limit |
|---|---|
| SE011 | Focused26/26 in43.571s + JDBC29/29 in56.336s =55/55, no failures/errors/skips; full-fixture pre-correction ENV_KEYS failure preserved. |
| PG012 | Prepared-v5 focused26/26 in33.317s, no failures/errors/skips; runner prohibits prepare and preserves before/after plans. Only synthetic harnesses compiled. |
| Full public bootstrap renderer fixture | All83 keys and public example/contract/validator values checked; v5 accepts exact quoted classpath:/, pinned v4 rejects ENV_KEYS. Synthetic secrets remain in memory, not printed/persisted; private reads/child execution prohibited. |
| PG negative matrix |12 alternative locations,13 override/injection lines,2 unsafe settings,2 duplicate forms and9 malformed forms rejected; no broad SPRING_CONFIG_ exception. |
| Actual validator and identity | Synthetic matching metadata accepted, missing table/column rejected; Connector/J excluded; original v3 type failure retained. MAPPING_READY and actual-v5 approval binding PASS; old reviews/markers/bundle rejected. |
| Unchanged JDBC guards | PG007's29/29 retained on matching pins, not rerun or added to PG012's26. SE011's29/29 is separate. |

The public-source incompatibility is locally corrected and independently
regression-covered. This does not identify a private offending line or revise
the actual v4 HOLD to PASS. No material local delta blocker remains per PG012.

**No v5 operator or remote retry is approved.** The old rev3 review and old
approvals are invalid for v5; paths/hashes must not be silently substituted.
Future native work requires new explicit access/secret-use approval, a separately
reviewed exact v5 signal-aware operator, fresh target/capture/identity/TLS gates,
bounded process-group cleanup and independent stop/mask/recovery access.
No remote/DB/SSH-agent action occurred after the supplied02:24 cleanup.
Native schema NOT_RUN, canonical fresh manifest UNRECORDED, app_runnable=false,
production HOLD; no app startup/publication or new operational evidence.

WI008's current integration is complete after its four-file documentation checks;
REQ stays approved/active pending later target work. No further linked WI exists
and no descendant was created. Earlier waiting statements below describe the
02:29 checkpoint only; this addendum supersedes them without erasing history.

## Reference Documents (Tier 0-2)

| Tier | Injected/read reference | Use |
|---|---|---|
| 0 | core-principles.md STD-001; documentation-standards.md STD-004; development-standards.md STD-002; glossary.md STD-005 | All four full texts injected before role/security/context; current metadata/presence checked. |
| 1 | .claude/agents/docops.md; docs/policies/security-policy.md | Injected role, secret-safe evidence and two-set reporting. |
| 2 | Existing WI008 handoff; approved REQ001 and subsequent direct user approval | Four allowed paths, no descendants or operational actions. |
| 2 | create-wi-evidence-pack and validate-docs skills | Loaded from disk; handoff existence checked before evidence generation; scoped validator use. |
| 2 | Current Decision Entry; WI005/006/007/009/010 Evidence Packs | TLS, artifact/test history and exact review identities. |
| 2 | WI011 and WI012 Evidence Packs, supplied full text and saved files | Latest exact-v5 correction, local acceptance and operator exclusions. |
| 2 | release-readiness runbook and public helper/operator source | Current compatibility finding, execution phase and absent post-observation. |
| 2 | MA supplied structured receipts; MA/PG 02:22 follow-up message | Attributed native outcome and withdrawal of execution-readiness PASS; follow-up was not saved in old WI010. |

Rule source `.claude/config/context-injection-rules.json`: DocOps tiers[0,1],
task type documentation; workspace.json domain tag ATS checked. The existing
skill-generated WI008 handoff was reused. This output applies the evidence-pack
skill; no replacement handoff, descendant or broad governance audit was created.

## Historical v4 Finding / Drift Report

**HIGH: attempted v4's public bootstrap environment and schema guard are incompatible.**
`scripts/deployment/validate_bundle.py:39` fixes
`SPRING_CONFIG_LOCATION="classpath:/"`; its expected-env validation requires
the public safe literal. `production-bootstrap.py:317-335` copies bundle.env
and preserves it while rendering. The pinned v4 `release-readiness.py:256-262` rejected every
`SPRING_CONFIG_` line because only two JPA lines are permitted.

This source-proven defect is the likely native trigger, not a claim to have
observed a private offending line. MA's DB-off diagnostic returned only
ENV_KEYS. No private env body, value, hash, data row, identity hash, management
address or console token was imported. MA/PG's follow-up at approximately02:22
withdraws v4 execution-readiness PASS to HOLD. The original WI010 file remains
historical evidence; its tests and prior review are not silently relabelled as
failed or erased. DocOps did not change the runbook; subsequent WI011 edits
are separate local work and do not authorize a retry.

**New approval, received during WI008:** the user explicitly authorized
"검증 도구 수정·회귀시험·재검토 진행". Exact approval time was not supplied.
Scope: allow only the exact existing classpath:/ literal, retain rejection of
imports/external/other configurations, test a full public-bootstrap synthetic
fixture, prepare a fresh versioned candidate and obtain independent review.
MA is creating WI011(SE)/WI012(PG) handoffs. No results have been received here.
No server reconnect/restart or native retry is included. Never edit private env
to bypass the guard. Return to WI008 only on actual review evidence, not a
forecast PASS; future target work needs separate fresh authority and gates.

## Exact Identity and Historical Local Tests

Candidate `build/WI009-schema-v4`:171 generated files plus artifact.json,
178 staged inputs. Bundle:
`f2e59839caa096fff83b427ad47563be8f7b96c58bdd2825bda9216e109eaeec`.
DocOps rehashed the public files below before concurrent WI011 edits;
these are attempted-v4 pins, not later mutable source identities.
Aggregate staging/plan and application
artifact comparisons remain attributed to WI006/009/010 and MA, not rerun here.

| Public input | SHA-256 |
|---|---|
| release-readiness.py | a1e0492623901ab71086920dddc1c7100265b5016d281abe4410e82dc363f96a |
| RuntimeSchemaProbe.java | 0a9c8676e2cdaa2379fc578bebf6d161dad3f9cf8c9c5dc4eacb6c9f1411ac1a |
| test_release_readiness.py | cd1aa3a7b26e651d7e0d6e11570f840d798b7192a696c1ce9b08bc7ac6fd84ff |
| RuntimeSchemaValidatorHarness.java | 1a626af0f9ca3a53b842713f13d120c97f81196ed625825702761ed33b6a2587 |
| release-readiness.md | 36530c512fe04d702df125e1b1af609a31f428b773248bad3830911c7fcf6579 |
| build/WI009-schema-v4/artifact.json | b2fc53ff9f8f3b25a8ea687cde69196a140083182d6bc02c49c1a30ab4ac6cba |
| ma-native-operator-revision3.py, separate from bundle | 85384f2c118119e96e2166f8456448dcf16ad5a915798b0be211db4f246b5189 |
| jdbc-readiness.py, unchanged | 13e2d084bef0e2bf1c6cb9ff64ee0dbae9650b40ccaa1091afc8b75365670051 |
| production-bootstrap.py, unchanged | 7333b068d0264045b8130519afc6e0f62589912ba2ef0b31772ff84033960a1b |
| validate_bundle.py, unchanged | 19361e515e694718b509e28c6223a3ffc0bf647ddf6bb972f8aa4904cc1cb593 |

WI006/009/010 record backend638/frontend336/SPA143 matches, JAR SHA-256
`517b745479792a7eae6ce75357c7d1299ab644b50d5bc5b331fea384ccb10e0a`
and WI026 manifest `fbeb74425ade0fe3882a9dddc4c97f7b67b75dda711ffa37acbd755a02fa4d9c`.
Exact historically excluded test inputs, not a broad coverage-directory glob:

```text
frontend/src/test/coverage/adminSubscriberGaps.coverage.test.tsx
frontend/src/test/coverage/adminSubscriberPages.coverage.test.tsx
frontend/src/test/coverage/publicAuthShell.coverage.test.tsx
frontend/src/test/coverage/shellCatalogRouterGaps.coverage.test.tsx
```

Their contents/history and complete frontend/typecheck equivalence are not
verified. No private env hashing, rebuild or new coverage measurement occurred.

| Owner / historical check | Bounded result |
|---|---|
| SE006 / PG007, v3 | Each48/48 synthetic tests and mapping smoke passed; PG007 actual validator reproduced Integer-to-String ClassCastException, zero direct-validator connections. v3 stayed HOLD, not a DB fault. |
| SE009, v4 | 21 focused +29 JDBC =50/50 PASS; actual validator accepts model-derived synthetic metadata and rejects missing table/column; original v3 still reproduces failure. |
| PG010, exact v4 |21/21 PASS, real validator and original-v3 regression; unchanged JDBC guards rely on PG007's29/29, not a fresh PG010 rerun. |
| PG010, operator rev3 | Eight cleanup mocks plus timeout, malformed JSON and late-interrupt cases PASS; isolated AST doubles, not Linux/native execution. |
| Current MA/PG follow-up | New environment compatibility blocker supersedes execution-readiness only; prior limited tests remain factual. |

Preserved v3 bundle:
`f06f7f2de2a891a482515f92abac8dfe75b4dfdddd1718a91f9c9592958a642b`;
v3 artifact `ffadd34b644481bed92a7688f740270047adede4da458f9805230e68fb3c7e21`.
Rev1/rev2 hashes and closed PG-010 findings remain in WI010; none is a fallback.
Public WI010 Evidence Pack preservation pin:
`73a9e513f1298626eeaf3f0d8a08e8e7ca40175e12ecf29dda428bf30aa23c04`.

At approximately02:29, a repeat17-file pin comparison found13 unchanged inputs,
including all five old Evidence Packs, v3/v4 manifests and operator rev3.
The WI008 handoff, release helper, focused tests and runbook had changed during
the approved MA/WI011 work. DocOps reread the revised handoff and public delta,
preserved those edits and did not treat them as a reviewed candidate. The
handoff now explicitly requires WI011/012 local results. This expected shared
workspace change prevents any all-source-unchanged claim; the exact v4 table
above remains historical execution identity. No WI011/012 acceptance is inferred.

## MA Actual Receipt Ledger

All times below are2026-10-11 KST (UTC+09). Structured source timestamps are
2026-10-10 UTC. Receipt names/chunk IDs refer to MA's supplied current-window
tool outputs; DocOps neither reconnects nor reproduces them.

| Receipt / source | Actual result and limit |
|---|---|
| wi009RootStage / 7ad2d1, exact time not supplied | ROOT_STAGE_VERIFIED,178 public files/113653320 bytes.177 uploaded files/43350185 bytes plus copied retained pinned JAR; no hardlink or app deployment. |
| MA pre-attempt Linux plan/smoke, narrative | Exact plan and MAPPING_READY/no connection before attempt; no native schema proof. |
| wi009FinalGate / 302da5,02:18:41 | HOLD/exit1: no_named_workers_capture=false; other listed gates true. MA/PG classified all3 psimon entries using Kthread1/PPid2/UID0/TracerPid0, no executable and empty cmdline. No controls disabled or privileged-capture guarantee. |
| wi009FreshGate / 0a41cd,02:21:50 | PASS/exit0; kernel_psimon_count3, psimon_kernel_only=true; no DB ports/approval, trusted operator/private metadata, same boot, capture/TLS/timer gates true. This is preflight, not schema validation. |
| wi009NativeResult / d38e6b,02:21:59 | Operator rev3 EXECUTED and temporarily started loopback MySQL. phase=APPROVED_ENV_VALIDATION, operator_failure=GateError, native=NOT_RUN, app_runnable=false, status/production_status=HOLD despite exit0. No schema approval or child launch. |
| Same operator receipt, cleanup | mysql_stop_exit0, mysql_mask_exit0, owned_process_group_clear=true, mysql_inactive_masked_no_ports_jobs=true, restricted_tls_unchanged=true, cleanup_verified=true. |
| wi009Postcheck / 72f3f8,02:22:46 | Independent MA PASS: same boot; MySQL inactive/masked, no DB ports/jobs/Java/FFmpeg; app unit and schema approval absent; Nginx and Certbot timer active/enabled, TLS pins unchanged. |
| wi009Diagnostic / 7d9591,02:23:04 | DB remained off during sanitized diagnostic; database_started=false applies to this diagnostic, not the earlier operator. ENV_KEYS only; native_schema=NOT_RUN, HOLD. No private offending line was exported. |
| wi009ConsoleCleanup / 47b132,02:23:26 | root_tty1_count0, PASS after logout. |
| wi009EmptyAgent / 359375; wi009AgentFinal / 5f55d1, around02:24 | MA reports exact owned SSH key removed; ssh-add-l exit1 means no identities. Elevated restore exit0 precedes fresh CIM Stopped/Disabled. Temporary noVNC21 and instance20 tabs closed; firewall18 handoff retained. |
| MA/PG follow-up, approximately02:22 | Source compatibility finding and withdrawal of execution-readiness PASS, supplied as message rather than an amendment to WI010. Later ENV_KEYS receipt narrows observed remote detail only to its fixed code. |

At operator lines111-114, before-baseline equality is checked before assigning
APPROVED_ENV_VALIDATION. Reaching the reported phase therefore implies equality
with the existing receipt; it is an inference from exact control flow, not an
exported comparison receipt. Lines133-136 (after-observation) never ran. Do not
claim before/after observation PASS or new schema acceptance. No DDL/DML,
account/grant/password/config change was authorized or performed in this attempt.

## Remaining Gates and No-Publication Boundary

Current native Hibernate NOT_RUN; canonical fresh-schema manifest UNRECORDED;
app_runnable=false; production HOLD. Even future Hibernate PASS would not prove
every default/index/FK/check, canonical fresh DDL, complete application behavior,
capacity or launch readiness. A metadata digest is not canonical equivalence.
Read-only JDBC may include transaction-control commit/rollback; it is not a
wire-level SELECT-only/no-COMMIT promise. No child ran in this stopped attempt.

WI005's restricted TLS and scheduled-renewal configuration remain bounded PASS;
NS unresolved, expiry alerts unconfigured, first unattended renewal/future
certificate change/reboot unproven. Recovery/backup, SMTP, ADMIN, global workers,
external-call fencing and final runtime activation need their own approval and
evidence. No additional purchase/install, app start or publication. The monthly
KRW70000 budget is not a hard billing cap. No provider terms were refreshed.

## Fix Plan / Patch Pointers

| Allowed changed path | Concrete update |
|---|---|
| docs/index.md | Prepend current HOLD/cleanup/correction-approval checkpoint before older TLS; preserve the entire historical suffix and category counts. |
| deliverables/user/REQ-20261011-ATS-001.md | Keep active; distinguish current DoD from schema acceptance, append actual receipts and direct approval for WI011 -> WI012 -> WI008. |
| deliverables/user/WI-20261011-ATS-008-summary.md | Current user-facing result and remaining boundaries. |
| deliverables/agent/WI-20261011-ATS-008-evidence-pack.md | This skill-generated attributed ledger, source finding and verification limits. |

No code/runbook/old evidence, secret, network, DB, browser, descendant, commit,
push, reset, deletion or memory edit by DocOps. No docs-category file added;
category counts need no change. No complete repository drift/freshness audit.

## Commands and Documentation Validation

Read-only git status/HEAD, focused Get-Content/rg and public Get-FileHash checks
were used. Manual changes use apply_patch only. Initial index/REQ text and
public history pins were retained in session memory for preservation checks.
No application tests, build, coverage, native retry or external-link check by DocOps.

The current four-file checkpoint passed Tier0 existence, internal file links
and60 supported traceability IDs,0 errors/0 warnings. Run the existing validate-docs
module in memory, not its whole-repository entry point. This reproducible
PowerShell invocation scopes both passes with a new iterator on every call:

```powershell
@'
import runpy
from pathlib import Path
from unittest.mock import patch
root = Path.cwd()
paths = tuple(root / p for p in (
    "docs/index.md",
    "deliverables/user/REQ-20261011-ATS-001.md",
    "deliverables/user/WI-20261011-ATS-008-summary.md",
    "deliverables/agent/WI-20261011-ATS-008-evidence-pack.md",
))
ns = runpy.run_path(str(root / ".agents/skills/validate-docs/scripts/validate_docs.py"))
ns["configure_stdio"]()
validator_class = ns["DocValidator"]
validator = validator_class(root)
results = [validator.validate_tier0_docs()]
for check in (validator.validate_internal_links, validator.validate_traceability_ids):
    with patch.object(Path, "rglob", side_effect=lambda *args, **kwargs: iter(paths)):
        results.append(check())
print("SCOPED", len(paths), "errors", len(validator.errors), "warnings", len(validator.warnings))
raise SystemExit(0 if all(results) and not validator.errors and not validator.warnings else 1)
'@ | python -B -
git diff --check -- docs/index.md deliverables/user/REQ-20261011-ATS-001.md deliverables/user/WI-20261011-ATS-008-summary.md deliverables/agent/WI-20261011-ATS-008-evidence-pack.md
```

Supplement with direct metadata/dependency, final-newline/trailing-whitespace
checks over all four files, including untracked deliverables. The validator
checks link file existence, not Markdown fragment semantics or external URLs;
its traceability pass counts supported patterns, not full referential integrity.
No whole-repository orphan/index audit is claimed.

The first scoped pass matched60 supported IDs and reported one parser false
positive on the reproduction snippet's dictionary lookup immediately followed
by a constructor call. The snippet now separates lookup and construction;
no actual link target or validator source was changed. Direct four-file
metadata/dependency/newline/whitespace checks and scoped git diff --check
passed at02:28 KST; the existing index CRLF normalization warning is not an
error. The corrected scoped pass returned0 errors/0 warnings. At02:29 KST the
final checkpoint rerun again passed: four files,60 supported IDs,0 errors/
0 warnings; direct metadata/dependency/newline/whitespace and scoped git diff
--check also passed. This validates the checkpoint, not the pending WI012 gate.

Newline-normalized comparisons confirm the entire old index suffix from the
restricted TLS heading and the earlier REQ correction-approval section remain
intact. The five historical Evidence Packs retain their initial public hashes.

**Separate known whole-validator limitation:** WI005 records the existing
false positive at `deploy/single-vps/runbooks/first-host-setup.md:175`, where a
PowerShell integer cast plus parenthesized input is parsed as a Markdown link.
That source/validator remains unchanged; no whole-validator rerun or global
PASS is claimed here. Do not invoke --help, which that script ignores.

## Historical Checkpoint Follow-ups

Rollback means an approved scoped correction to these four documents only,
preserving original dirty work, earlier checkpoints and pins. No service/DB
rollback or private env bypass is proposed. Current source finding does not
identify the private offending line or guarantee a correction will pass.

WI chain checked: current MA outcome/cleanup is integrated; newly approved
WI011(SE) -> WI012(PG) returns to WI008. MA owns those handoffs; no DocOps
descendant or new WI file. Final integration is paused until the review receipt
arrives. REQ remains approved/active/HOLD; no overall completion assertion.

## Final Integration and Follow-ups

Latest WI011/012 receipts supersede the earlier wait: WI008 integrates local
v5 PASS and preserved native HOLD/cleanup, with no further linked WI or
descendant. REQ remains approved/active; next work needs fresh explicit access/
secret-use authority, exact-v5 operator review and read-only native validation.
Old rev3 cannot authorize it. No new remote, DB or SSH-agent operation was run.

Rollback remains a scoped documentation correction only. On future v5 drift or
failure, retain evidence and request correction/re-review; never fall back to
v3/v4, modify private env or replay bootstrap.

Final02:38 KST validation: four files, Tier0 presence, internal file links and60
supported traceability IDs;0 errors/0 warnings. Direct seven-field metadata,
dependency-target, final-newline and trailing-whitespace checks include all
untracked deliverables and passed. Scoped git diff --check passed; only the
existing index CRLF normalization warning remains. All23 snapshotted public/
history pins were unchanged, including the seven earlier Evidence Packs.
Newline-normalized comparisons confirm the complete prior index checkpoint/
suffix, REQ native-history block and MA receipt ledger are unchanged. These
checks close WI008 documentation integration only, not REQ or native gates.
The known whole-validator false positive above remains separate; no global PASS.

## Related Documents

- [WI011 evidence](WI-20261011-ATS-011-evidence-pack.md): Latest correction, exact v5 and SE55/55.
- [WI012 review](WI-20261011-ATS-012-evidence-pack.md): Exact-v5 local PASS, PG26/26, no operator approval.
- [WI008 handoff](WI-20261011-ATS-008-handoff.md): Existing approved write scope.
- [REQ001](../user/REQ-20261011-ATS-001.md): DoD, new approval and chain.
- [WI008 summary](../user/WI-20261011-ATS-008-summary.md): Current checkpoint.
- [WI006](WI-20261011-ATS-006-evidence-pack.md): Artifact comparison and exclusions.
- [WI007](WI-20261011-ATS-007-evidence-pack.md): Preserved v3 HOLD.
- [WI009](WI-20261011-ATS-009-evidence-pack.md): Corrected v4 and SE tests.
- [WI010](WI-20261011-ATS-010-evidence-pack.md): Historical exact candidate/operator review.
- [WI005](WI-20261011-ATS-005-evidence-pack.md): Prior TLS/renewal boundary and validator limitation.
- [Current entry](../../docs/index.md#current-decision-entry): Superseding checkpoint.
- [Release runbook](../../deploy/single-vps/runbooks/release-readiness.md): Preserved procedure; not current retry authority.
- [Exact operator rev3](../../build/WI010-pg-20261011-0210/ma-native-operator-revision3.py): Phase and cleanup control flow.
