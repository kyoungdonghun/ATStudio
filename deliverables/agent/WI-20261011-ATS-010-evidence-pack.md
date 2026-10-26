---
version: 1.0
last_updated: 2026-10-11
project: ATS
owner: PG
category: evidence-pack
status: confirmed
dependencies:
  - path: WI-20261011-ATS-010-handoff.md
    reason: Approved local-only independent delta review
  - path: WI-20261011-ATS-007-evidence-pack.md
    reason: Preserved original HOLD and unchanged safeguard verification
  - path: WI-20261011-ATS-009-evidence-pack.md
    reason: Corrected candidate identity and implementation delta
  - path: ../user/REQ-20261011-ATS-001.md
    reason: Approved correction, re-review and separately gated MA operation
---

# Evidence Pack: WI-20261011-ATS-010

> Purpose: Bind independent review to the corrected compiled v4 and MA operator revision 3.

## Findings and Verdict

**TL;DR: scoped PASS for the exact v4 bundle and exact operator revision 3 below. Native NOT_RUN.** No remaining material blocker was found in this delta review. This is permission to advance to the approved MA target gates, not evidence those gates or native schema validation have passed.

| Finding | Evidence and disposition |
|---|---|
| PG-007-01: Integer version settings break the real Hibernate validator | CLOSED in v4. `RuntimeSchemaProbe.java:59-60` now uses Strings. Exact compiled v4 passes actual matching metadata and rejects missing table/column; original v3 still reproduces the type failure. |
| PG-010-01: cleanup command nonzero could retain PASS | CLOSED in rev2/rev3. Original operator:146 only recorded the return code. Rev3:148-153 forces HOLD and independently attempts both stop and mask. |
| PG-010-02: launched probe timeout/invalid JSON reported NOT_RUN | CLOSED in rev3:125. START_ATTEMPTED precedes Popen; normal parsed completion retains RUN. |
| PG-010-03: late interrupt handler could retain PASS | CLOSED in rev3:138. Every caught top-level exception first sets HOLD. |

Original v3 and operator rev1/rev2 remain historical, not selectable fallbacks. Their files were preserved. Rev3 differs from the supplied rev2 by exactly the two requested insertions; no unannounced procedure change.

## Exact Identity

Candidate: `build/WI009-schema-v4`; bundle SHA-256:
`f2e59839caa096fff83b427ad47563be8f7b96c58bdd2825bda9216e109eaeec`.

| File | Independently checked SHA-256 |
|---|---|
| scripts/deployment/release-readiness.py | a1e0492623901ab71086920dddc1c7100265b5016d281abe4410e82dc363f96a |
| scripts/deployment/RuntimeSchemaProbe.java | 0a9c8676e2cdaa2379fc578bebf6d161dad3f9cf8c9c5dc4eacb6c9f1411ac1a |
| scripts/deployment/test_release_readiness.py | cd1aa3a7b26e651d7e0d6e11570f840d798b7192a696c1ce9b08bc7ac6fd84ff |
| scripts/deployment/RuntimeSchemaValidatorHarness.java | 1a626af0f9ca3a53b842713f13d120c97f81196ed625825702761ed33b6a2587 |
| deploy/single-vps/runbooks/release-readiness.md | 36530c512fe04d702df125e1b1af609a31f428b773248bad3830911c7fcf6579 |
| build/WI009-schema-v4/artifact.json | b2fc53ff9f8f3b25a8ea687cde69196a140083182d6bc02c49c1a30ab4ac6cba |
| scripts/deployment/jdbc-readiness.py, unchanged | 13e2d084bef0e2bf1c6cb9ff64ee0dbae9650b40ccaa1091afc8b75365670051 |
| build/WI006-schema-v3/artifact.json, preserved | ffadd34b644481bed92a7688f740270047adede4da458f9805230e68fb3c7e21 |

Approved executable procedure: [ma-native-operator-revision3.py](../../build/WI010-pg-20261011-0210/ma-native-operator-revision3.py).
SHA-256: `85384f2c118119e96e2166f8456448dcf16ad5a915798b0be211db4f246b5189`.
It is separately pinned, not part of the v4 bundle identity. MA must copy these exact bytes, verify that hash, and use isolated Python with the stated sanitized environment.

Historical procedure hashes: [rev1](../../build/WI010-pg-20261011-0210/ma-native-operator-original.py) `a488e27322549dd74b8a3e8db442057e8410ad3cd8d2bba72ee407cdb5a0ba98`; [rev2](../../build/WI010-pg-20261011-0210/ma-native-operator-revision2.py) `99ff97f720199f5fc77b097da380e14ec9eec0fc294574c9ca204134bea2b8f3`.

## Independent Tests

PG, Windows/PowerShell, 2026-10-11 KST; HEAD `58b1011dad796017e47dbcc82a4225ca449bcdf6`. Existing compiled candidate reused without prepare or application/source rebuild. Harness-only synthetic compilation was allowed.

| Check | Result |
|---|---|
| Focused suite against exact compiled v4 | 21/21 PASS, 32.734s, zero failures/errors/skips |
| Real validator within that suite | Matching metadata accepted; missing table and missing column rejected; no Connector/J or registered real driver/socket |
| Legacy v3 within that suite | Original inventory rehashed; expected type failure reproduced |
| Approval regression | WI010/new bundle accepted; old WI007 review rejected; reused strict identity/time/check validation retained |
| Exact v4 plan and offline smoke | PLAN_ONLY exact bundle; MAPPING_READY, no connection |
| Rev3 syntax and isolated AST mocks | Eight cleanup cases plus timeout, malformed JSON and late-interrupt reporting cases PASS |
| Old JDBC suite | Prior PG007 independent 29/29 reused on unchanged pinned guards; not rerun in WI010. SE009's new 29/29 is separately attributed. |

Reproduction:

```powershell
python -B build/WI010-pg-20261011-0210/run_focused.py
python -B build/WI010-pg-20261011-0210/review_operator_revision3.py
python -B scripts/deployment/release-readiness.py plan --output build/WI009-schema-v4
python -B scripts/deployment/release-readiness.py smoke --output build/WI009-schema-v4
```

[Focused log](../../build/WI010-pg-20261011-0210/focused-89912da3abf54ded84838a3b94f38973.log), [compiled reuse runner](../../build/WI010-pg-20261011-0210/run_focused.py), [rev3 mock](../../build/WI010-pg-20261011-0210/review_operator_revision3.py). Mocked cleanup uses only extracted AST with inert command, signal, filesystem and process doubles, never imports or runs the operator as a native program. Nonzero stop/mask, either timeout, reap failure and final state/TLS failure all yield HOLD; both cleanup commands are independently attempted.

## Accepted Procedure and Remaining Gates

- **Historical helper reuse is acceptable:** exact retained `load_bundle`, `database_guard(fresh=False)`, `verify_grants(locked=False)` and `observe(bundle)` only. `production-bootstrap.py:179,243,339,382` establish fixed read-only statements/metadata and bounded aggregates. No `apply`, `preflight`, `native_guard`, `host_guard` or `mutate`. Root socket observations are fixed read-only SQL, not a claim that the root account is SELECT-only.
- Verify old helper/source/input pins before loading; compare before/after observation and existing receipt observation internally. Prior JDBC approval supplies historical identity, corroborated by bootstrap approval and machine identity; it grants no current authority. Server UUID guard precedes secret use. Never substitute newly observed identity for missing provenance.
- Current approval binds REQ001, WI010, exact v4 and machine/server identities, with 20-minute validity within the reused 30-minute maximum. Checks are attestations to real fresh operator observations, not substitutes for them. Approved `schema_password` validation reads the exact private file internally and discards the password; no body or credential hash is exported.
- Public staging: use observed atadmin UID1001/0700, fresh root directories0700 and O_EXCL single-link copies0600. Reused JAR must be copied, never hardlinked, and match its exact pin. Verify inventory, sources and staged operator hash, final plan and Linux no-connect smoke before MySQL activation. MA reports 177 uploaded public files/43,350,185 bytes plus separately reused pinned JAR; PG did not verify the remote copy.
- Repeat exact boot/recovery-console/key-window, MySQL loopback/local-infile/mysqlx/persisted/client-override, capture and restricted TLS/timer checks immediately before use. Capture checks reduce known risk; they do not guarantee safety against privileged-host capture. Maintain exclusive maintenance throughout.
- Rev3 owns the wrapper/Java process group, applies bounded TERM/KILL/reap handling and a 210-second wrapper deadline, and blocks handled termination signals during final cleanup. Stop and mask are independent bounded attempts; final no-port/no-job/inactive/persistent-mask and TLS/worker checks are mandatory. Linux signal/process behavior was statically reviewed, not natively exercised by PG. SIGKILL, host/connection loss or an unverified cleanup requires the retained recovery console and a HOLD outcome.
- Consume the structured report, not Python exit status alone. START_ATTEMPTED with failure is not NOT_RUN and is not permission to retry automatically. A successful cleanup check does not erase an earlier failure. Preserve the approval and receipts; do not delete or rerun bootstrap to recover.

## Scope, References and Rollback

- [x] Exact correction, real-validator regression, approval binding and unchanged safety boundaries reviewed.
- [x] Candidate/history preserved; only the two WI010 deliverables and unique synthetic build outputs written.
- [x] Exact rev3 review complete; MA must perform fresh target gates before execution.
- [ ] Native target/schema result and cleanup receipt: MA-owned, NOT_RUN in this review.

Injected references retained in order: Tier0 `core-principles.md` (STD-001), `documentation-standards.md` (STD-004), `development-standards.md` (STD-002), `glossary.md` (STD-005); Tier1 PG role and full security policy; Tier2 approved REQ, WI010 handoff, WI007/WI009 evidence, test/evidence-pack skills, exact sources/runbook and supplied operator revisions. Handoff existence and evidence-pack skill contract checked. No new delegation or governance edits.

No private file, actual DB, network, SSH, browser, installation, application execution, commit, push or descendant action. No coverage/CVE/full-repository audit. Synthetic metadata derives from the model, not canonical DDL; no canonical fresh-schema, full application behavior, capacity or public deployment claim. Prior four frontend test exclusions remain as WI006 documented.

Rollback: stop selecting this reviewed pair on any drift/failure, retain all history, and request bounded correction/re-review. Never use v3 or rev1/rev2 as fallback or alter DB/config to obtain PASS. MA owns immediate WI008 integration after actual outcome or HOLD and cleanup evidence; REQ remains active.

## Related Documents

- [WI010 handoff](WI-20261011-ATS-010-handoff.md): Review authority and output contract.
- [WI010 summary](../user/WI-20261011-ATS-010-summary.md): User-facing verdict.
- [WI007 evidence](WI-20261011-ATS-007-evidence-pack.md): Original blocker and unchanged guards.
- [WI009 evidence](WI-20261011-ATS-009-evidence-pack.md): Corrected implementation candidate.
- [Runbook](../../deploy/single-vps/runbooks/release-readiness.md): Native procedure boundaries.
