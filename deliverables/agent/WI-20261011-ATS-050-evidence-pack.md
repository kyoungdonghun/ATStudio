---
version: 1.0
last_updated: 2026-10-11
project: ATS
owner: PG
category: evidence-pack
status: confirmed
dependencies:
  - path: WI-20261011-ATS-050-handoff.md
    reason: Explicit diagnostic-only follow-up approval
  - path: WI-20261011-ATS-045-native-receipt.json
    reason: Preserved actual failure fields and recovery boundaries
  - path: ../../build/WI042-restricted-web-check/freeze.json
    reason: Consumed runtime and session identity
---

# Evidence Pack: WI-20261011-ATS-050

> Purpose: Locate the original POST_STATE_CHANGED failure and bound its possible causes without another native run.

## Decision

**TL;DR: The second, post-view comparison failed. The exact unequal operand remains NOT_DETERMINED because its snapshots and branch outcome were not persisted. Original trial HOLD remains; no restart or repair authority.**

- [x] Exact runtime/session pins match the supplied consumed-attempt identity.
- [x] Failure site, short-circuit behavior and distinct DB validation errors established.
- [x] Policy serialization and BPF replacement hypotheses separated from native proof.
- [x] Persistence limits identified; 13 isolated in-memory assertions passed.
- [x] Only these two WI050 reports written; next WI051 integrates findings.

## Proven Failure Site

[runtime.py](../../build/WI042-restricted-web-check/runtime.py):1006 checks policy, BPF attachments and DB aggregates before setting `app_start=VERIFIED` at1008. [web_session.py](../../build/WI042-restricted-web-check/web_session.py):269 sets `view_interval=FINISHED` only after the finish marker is accepted. The preserved [native receipt](WI-20261011-ATS-045-native-receipt.json), `native.result`, has both fields, `stage=READ_ONLY_CHECKS` and `code=POST_STATE_CHANGED`.

Therefore the failing site is runtime.py:1013-1014, after `session.view(result)`, not1006-1007. The first comparison passed at that earlier instant. The final `process_gate()` at1015 was not reached. The stage field is not advanced during view, so READ_ONLY_CHECKS does not imply a pre-view failure. Session restoration retains the primary code and adds separate cleanup failures; it does not generate POST_STATE_CHANGED.

The stored `bpf_attachment_checked=true`, positive/negative probe counts and app VERIFIED describe completed earlier checks. They do not prove post-view attachment equality, whole-window network containment or post-view DB equality.

## Possible Unequal Branches

The exact expression is `policy() == first_policy and attachment_snapshot() == first_bpf and db_counts() == before_counts`. Evaluation proceeds left to right.

| Branch | What this code allows us to conclude |
| --- | --- |
| Policy returns unequal | BPF and DB reads are skipped. No claim that either changed or remained equal is possible. |
| Policy equal, BPF returns unequal | DB read is skipped. A changed normalized attachment tuple can cause this code without a parser/command error. |
| Policy and BPF equal, DB returns unequal | DB validation completed, but returned aggregate bytes differ from the initial bytes. This is logically possible and not excluded. |
| A function raises before returning | Its fixed error propagates instead. Typical policy, BPF schema or DB validation errors are not relabelled POST_STATE_CHANGED by this expression. |

**Policy candidates, source-proven:** runtime.py:407-428 validates most selected fields exactly, validates IPAddressDeny as a set, then returns raw selected values except ActiveState/MainPID/ControlPID. Consequently raw IPAddressDeny order/spacing or UnitFileState value/presence can differ while policy validation succeeds. The latter is selected but has no fixed expected-value guard here. Synthetic probes reproduced order-only and UnitFileState-only differences. Neither was measured in the failed attempt. `After` is absent from PROPERTY_NAMES (:62-69); WI046's later Nginx.After finding is not this comparison.

**BPF candidates:** runtime.py:431-443 compares sorted `(direction, program-id)` tuples, not just the presence of ingress/egress or equivalent behavior. Record order alone is normalized; IDs or attachment multiplicity can differ. A replacement with equivalent rules can therefore produce an unequal snapshot. Missing directions/invalid rows instead produce BPF_MISSING/BPF_SCHEMA. No actual before/after IDs were retained.

**DB limitation:** runtime.py:657-666 dynamically enumerates 43 valid distinct table names including subscriptions, requires subscriptions=6 and all other counts=0, then returns the validated raw bytes. For an unchanged table list, an ordinary row-count change raises **DB_AGGREGATES**, not POST_STATE_CHANGED. Invalid table shape raises DB_TABLES. However a changed valid 43-table name set with the same expected per-table counts can pass the guard and return unequal bytes. This was reproduced synthetically, not observed on the server. Stable counts also cannot prove absence of row-content updates or temporary/net-zero writes. Do not interpret this diagnosis as a DB immutability audit.

## Reload Hypothesis

web_session.py:250-251 installs the Nginx trial overlay and invokes `daemon-reload` after the first app snapshots (runtime.py:968-969) and the first successful comparison, before the final comparison. FINISHED proves this view path completed, including that reload.

Official **v255 reference source**, independently read, supports a credible reload/replacement mechanism:

- [manager.c](https://raw.githubusercontent.com/systemd/systemd/v255/src/core/manager.c):3234-3316 serializes state, clears units, deserializes and coldplugs during reload.
- [unit.c](https://raw.githubusercontent.com/systemd/systemd/v255/src/core/unit.c):88-115 marks BPF firewall state invalid when allocating a unit.
- [cgroup.c](https://raw.githubusercontent.com/systemd/systemd/v255/src/core/cgroup.c):1352-1360 compiles/installs firewall programs; :1774 applies the firewall mask; :4083-4100 invalidates/requeues affected units.
- [bpf-firewall.c](https://raw.githubusercontent.com/systemd/systemd/v255/src/core/bpf-firewall.c):487-549 replaces compiled firewall programs/maps; :655-689 attaches new programs before disposing of old installed programs when multi-attach support is available.

**Assessment:** reload-related BPF identity replacement is a concrete, upstream-supported hypothesis. The exact installed distribution build, realized native path and unequal operand were not established by this work. It is not proof of the incident, a guarantee of continuous containment, or grounds to ignore changed IDs. Policy serialization remains another reproducible possibility; DB raw-identity differences are not logically excluded. No probability or native causal certainty is assigned.

## Persistence Gap and P2 Finding

**P2 diagnostic gap, runtime.py:1013-1014 and web_session.py:271-273:** one fixed error combines three comparisons without recording the failing operand or NOT_RUN outcomes. The relevant initial values `first_policy`, `first_bpf` and `before_counts` exist only in runtime memory; fresh values are evaluated inline and discarded.

Session `baseline.json` (:148-150) contains only `fixed`, `listeners`, `env_sha256`, original `nginx`, original `mysql`, `boot_id` and `management`. The nginx/mysql fields are restoration service snapshots, not the app policy/BPF/DB aggregate trio. `baseline-0..2` preserve original TLS-related files. `ready.json` stores bounded admission/app/view status. `attempt.json` stores the bounded runtime result; `result.json` adds restoration diagnostics. None persists these comparison operands or which late checks were skipped.

The reader/persistence contract was inspected; PG did not read private target baselines, environment or logs. Current live reads after stop/mask/reload/recovery cannot reconstruct discarded historical values. Existing independent contemporaneous evidence could narrow the cause if it actually exists, but none is supplied here. No SSH is needed merely to reconfirm that gap. WI046's After-order failure and the trial's subsequent overlay-retention cleanup failures remain separate events.

## Verification and Identity

Local Windows inspection on2026-10-11, PG. Branch `main`, HEAD `58b1011dad796017e47dbcc82a4225ca449bcdf6`; existing tracked dirty files were observed and left untouched.

| Input | SHA256 |
| --- | --- |
| WI042 runtime.py | `ad3e8a20ddb17fd1e3e4878ac2096bfa74a63561b9b1e8572b6bc2a7cda7a7b6` |
| WI042 web_session.py | `3092ae8c5598d9b97e1dedc6e0bb74ca9c0d3a8989493465114229d45727257a` |
| WI042 freeze.json | `b92cbe9cf2b24da99953b9f6c496028205a1a5daab754a915dce30690537f6bc` |
| WI045 native receipt at review | `aad6797c5f1bfc0ad4a1e8f64df9254359fe2023e39310cbd077f948df3c3f87` |

Freeze records bundle `78ce821757111b6f4bd440a79680ed12e4e40b6592616293e4d768ddd6dbe00c`; this diagnostic pass independently hashed the listed inputs, not every bundle/JAR/source file. Mapping to actual native execution relies on MA's supplied pin check and preserved receipts, not new PG target access.

**Pure probe command:** PowerShell here-string piped to `python -I -B -`, output receipt `04d54d`, exit0: **13 passed, 0 failed**. AST-extracted only Hold/require/unique/decode/policy/attachment_snapshot/db_counts and the actual require expression at1013. Properties, public unit reads, path existence, command and SQL functions were synthetic in-memory stubs. No runtime import/main/run/view, subprocess, socket, SQL or target access occurred; only public local source was read.

| Probe cases | Count / outcome |
| --- | --- |
| Policy short-circuit, BPF short-circuit, raw DB mismatch, DB guard error, all-equal expression | 5 PASS |
| Deny order-only mismatch, UnitFileState-only mismatch, unsafe Restart -> EFFECTIVE_UNIT | 3 PASS |
| BPF row reorder equal, same directions/new ID unequal | 2 PASS |
| Valid 43-table counts, non-subscriptions row drift -> DB_AGGREGATES, valid changed table set -> unequal bytes | 3 PASS |

Reproduction inputs: expression mocks use initial values1/2/3 and vary one operand; policy fixture uses the code's exact expected dictionary plus UnitFileState=static and deny strings in reversed order; BPF IDs11/12 with one changed to22; DB names are subscriptions plus t00..t41, then u00..u41, sorted, with6/0 counts. First exploratory receipt `0a3919` exited1 because the fixture mistakenly attempted to alter the first sorted table (subscriptions) while its fixed6 branch prevented that alteration. The corrected fixture selects the first non-subscriptions table. This was a probe-fixture failure, not a candidate regression; both outcomes are disclosed.

Existing tests inspected include test_runtime.py:317 (single attachment schema snapshot), :508 and test_cleanup_diagnostics.py:216 (stubbed DB counts). Those checks do not establish real reload identity stability or this post-view branch. No old full-suite PASS is relabelled as incident reproduction; no new full suite or coverage run.

## Bounded Next Step

WI051 may integrate the proven failure site, remaining candidate causes and missing evidence now. Keep the historical outcome HOLD/NOT_DETERMINED and record the later HTTPS recovery separately.

Before any separately approved future trial, propose a small diagnostic change with independent review: explicit PRE_VIEW/POST_VIEW phases; individual PASS/DIFFERENT/ERROR/NOT_RUN results; bounded allowlisted field labels and opaque BPF identity-change indicators before/after the planned reload. Preserve failure precedence and fail-closed behavior. Any continued post-failure read must have a reviewed safety/time budget; do not blindly execute skipped DB work. Do not dump raw environment, database rows, service output, addresses, command errors or private baselines. Merely dropping BPF IDs, refreshing the baseline after reload or accepting new IDs would weaken the evidence without proving equivalent effective rules.

No implementation or additional native trial is authorized by this report. No new purchase or manual input was needed. Ask before any later credential/manual-input/cost requirement. Existing server charges are outside this local diagnosis.

## References and Related Documents

Retained Tier0 order: STD-001 core-principles, STD-004 documentation-standards, STD-002 development-standards, STD-005 glossary. Tier1: PG persona/security-policy. Tier2: WI050 handoff with verbatim user approval, current entry, REQ006, WI045 evidence/receipt, pinned WI042 code/tests, WI048/049 evidence. Test skill was applied only to the authorized pure probes; create-wi-evidence-pack used for these two reports. No subdelegation, native operation, candidate edit, Git write or memory update; no running execution session.

- [WI050 Handoff](WI-20261011-ATS-050-handoff.md): Diagnostic-only authority.
- [WI045 Evidence](WI-20261011-ATS-045-evidence-pack.md): Actual trial and later recovery, kept separate.
- [Native Receipt](WI-20261011-ATS-045-native-receipt.json): Failure and evaluation-boundary evidence.
- [User Summary](../user/WI-20261011-ATS-050-summary.md): Concise cause assessment.
