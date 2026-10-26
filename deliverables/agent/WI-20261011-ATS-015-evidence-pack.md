---
version: 1.0
last_updated: 2026-10-11
project: ATS
owner: docops
category: evidence-pack
status: confirmed
dependencies:
  - path: WI-20261011-ATS-015-handoff.md
    reason: Approved local integration scope and output contract
  - path: WI-20261011-ATS-014-evidence-pack.md
    reason: Exact final preflight and historical independent review
  - path: ../user/REQ-20261011-ATS-001.md
    reason: Native-window approval and bounded completion
---

# Evidence Pack: WI-20261011-ATS-015

> Purpose: Integrate MA's actual native result and cleanup without expanding deployment approval.

## Summary and Scope / DoD

**Native read-only schema PASS; approved REQ scope complete; full production HOLD.**

- [x] Exact v5/operator/final-preflight identities and fresh approval recorded.
- [x] Native outcome, independent postcheck and Windows/session cleanup attributed to MA.
- [x] Current entry and REQ supersede stale native NOT_RUN without rewriting historical EPs.
- [x] Two-set deliverables and own handoff status completed; WI015 Blocks none.
- [x] Scoped documentation checks completed as detailed below; no native rerun by DocOps.

## Reference Documents (Tier 0-2)

| Tier | Injected document / input | Use |
|---|---|---|
| 0 | docs/standards/core-principles.md (STD-001) | Approval and evidence boundaries |
| 0 | docs/standards/documentation-standards.md (STD-004) | Metadata, links, historical preservation |
| 0 | docs/standards/development-standards.md (STD-002) | Traceability and scoped edits |
| 0 | docs/standards/glossary.md (STD-005) | Canonical terms |
| 1 | docs/policies/security-policy.md | Public pins only; relevant local projection read |
| 2 | .agents/skills/create-wi-evidence-pack/SKILL.md | Supplied skill; handoff precondition confirmed |
| 2 | .agents/skills/validate-docs/SKILL.md | Supplied skill; existing DocValidator used |
| Context | WI015 handoff; current REQ/index; WI014 latest addendum | Local sources read |
| Context | MA sanitized receipts and supplemental cleanup observations in delegation | Attributed native evidence, not DocOps observations |

Injection order: Tier0, DocOps role, task/receipts. Handoff rule source:
`.claude/config/context-injection-rules.json`; assignee docops, documentation task,
required Tier0. Earlier WI008/013 pointers were retained as history, not broadly
re-read. No memory, private-file, remote, service, DB or browser access by DocOps.

## Exact Public Identities

| Item | SHA-256 / identity |
|---|---|
| v5 bundle | 463c95667ee51e5fe2f01303a43ccebe0c0547375a374730c511c924212c7e54 |
| Operator | 64dc16d9b74315647497b9d335e5e309e27f544e03a5f76df35cb66fe0288136 |
| Final preflight | 6b1d7ed691b37043af778598decc46bf73e12b7c08f47b7d0f1128fce44de383 |
| Local branch/HEAD checked by DocOps | main / 58b1011dad796017e47dbcc82a4225ca449bcdf6 |

Pins come from MA receipts and [WI014 latest addendum](WI-20261011-ATS-014-evidence-pack.md#latest-addendum-exact-loopback-x-bind), not a new artifact rehash.
Candidate SE55/PG26, WI013 SE19/WI014 PG19 operator tests are historical local
results. Latest PG preflight52/52 is a separate pure-AST review at01:32:39 UTC;
earlier HOLDs and tests remain preserved, not cumulatively counted as native tests.

## MA Receipt Ledger

Observation date: **2026-10-11**. Native environment: approved Linux target;
cleanup includes Windows/PowerShell. Evidence owner: MA. The delegation supplies
tool receipt IDs below; supplemental cleanup facts are attributed separately.
No private configuration, private hash, management address or token URL is copied.

| Receipt | Actual observation |
|---|---|
| stage a1c85e, exit0 | STAGED, 178 files / 113653377 bytes; operator pin above; app_started=false, mysql_changed=false during staging |
| plan 19aa93, exit0 | Linux PLAN_ONLY, exact v5 bundle, Hibernate NOT_RUN at this preparation step |
| smoke b8ccef, exit0 | MAPPING_READY, MAPPING_ONLY_NO_CONNECTION before actual execution |
| preflight 872668, exit0 | PASS at01:34:32.923638 UTC /10:34:32 KST; all24 checks true, kernel_psimon_count=3 |
| native a728ba, exit0 | PASS at01:34:46.157081 UTC /10:34:46 KST; native RUN, Hibernate VALIDATE_PASS, probe_exit=0 |
| native a728ba counts | 43 tables, 520 columns, 234 index entries, 91 foreign-key columns |
| native a728ba cleanup | before_after_bootstrap_observation_equal=true; mysql_stop_exit=0, mysql_mask_exit=0, cleanup_verified=true; owned process group clear, MySQL inactive/masked with no ports/jobs, restricted TLS unchanged |
| post 4725eb, exit0 | Independent PASS at01:35:19.236328 UTC /10:35:19 KST: same boot, MySQL inactive/persistently masked, no DB/app ports/jobs/operator/probe workers; app absent, approval preserved private, Nginx/Certbot timer and TLS pins retained |
| logout 62c62b, exit0 | root_tty1_sessions=0, who_exit=0 at01:36:14.109376 UTC /10:36:14 KST |
| agent fc6938, exit0 | RestoreExit=0; ssh-agent Stopped/Disabled at10:36:26.7604836 KST |

Supplemental MA observations: initial Windows SCP0664 staging rejected before
root creation; exact owned public files hardened0600, then fresh staging without
partial-root reuse. Earlier preflight conflict/known mysqlx-bind HOLDs preceded
the **single actual operator run**, not DB validation failures or silent retries.
Exact owned public key removal exit0 preceded empty-agent rc1 and restoration;
a later fresh check still found Stopped/Disabled (later timestamp not supplied).
Owned NoExit window PID closed, only conhost child observed; console23 closed;
instance22 handoff and user tab9 preserved. Original18 closed outside cleanup.

All native validation was read-only and the application was not launched. No
additional product purchase/install, schema/DML/secret change or publication.
Canonical fresh manifest UNRECORDED and app_runnable=false persist through PASS.

## Drift Report and Applied Fix

High-impact stale current judgment: index and REQ still led with native NOT_RUN
and waiting-for-approval despite the later approved actual PASS. Added a dated
superseding entry and current REQ completion; preserved every historical EP,
registered hash and earlier checkpoint. No whole-repository audit or unrelated fix.

Evidence pointers / changed files:
- [Current decision entry](../../docs/index.md#current-decision-entry): latest outcome, cleanup and limits.
- [REQ001](../user/REQ-20261011-ATS-001.md): current progress, completed target gate and WI013/014/015 chain.
- [User summary](../user/WI-20261011-ATS-015-summary.md): compact result and remaining gates.
- [Own handoff](WI-20261011-ATS-015-handoff.md): status only changed to completed.
- This Evidence Pack: attribution, public pins, reproduction and rollback.

## Commands and Tests

DocOps used local Get-Content/Get-Item, git status and git log for scoped source
inspection, then apply_patch for the five allowed documents. No application
build, synthetic operator suite or native test was rerun.

Scoped validation: existing `DocValidator` imported with Python `-B`; its
`Path.rglob` input restricted to exactly the five changed Markdown files using
`unittest.mock.patch.object`. Called `validate_tier0_docs`,
`validate_internal_links`, `validate_traceability_ids`; checked required metadata
fields, dependency target existence, trailing whitespace and EOF directly.
This checks file-link targets, not anchor semantics or whole-repository coverage.
New deliverables are linked from index/REQ; no category document count changes.

Reproduction: use those five paths with the existing class in
`.agents/skills/validate-docs/scripts/validate_docs.py`, patch its enumeration,
run the three methods and assert empty errors/warnings. Run `git diff --check --`
with the five paths and directly inspect the four untracked deliverables because
Git diff alone omits them. Initial validation found63 supported IDs and no link
errors; direct EOF inspection caught an existing final blank line in REQ001,
removed within the allowed file. Final rerun: five files,63 supported IDs,
zero errors/warnings; required metadata fields/dependency targets/whitespace/EOF
PASS. Scoped `git diff --check --` exit0; Git reported only its existing CRLF-to-LF
normalization notice for index. Direct `git status --short --` confirmed index
modified and the four deliverable paths untracked; none was staged or committed.
Whole-validator `first-host-setup.md:175` known false positive was not audited or
repaired; no whole-repository PASS is claimed.

## Risks / Rollback / Follow-ups

Hibernate PASS and structural counts do not establish a canonical fresh DDL
manifest or full application readiness. Full production remains HOLD. NS, expiry
alerts, first unattended renewal/actual reboot, backup/restore, mail/ADMIN,
worker/egress fencing and full startup require separate gates; no new approval
for them is implied. Restricted TLS is retained evidence, not broad public launch.

WI015 Blocks none; all scoped work and integration are complete, so REQ001 closes
only for this approved window. Rollback if later evidence contradicts it: append
a dated correction to current entry/REQ and amend this WI015 pair through the
approved process; preserve historical receipts and unrelated dirty changes.
Never use document rollback to trigger remote retry or service changes.

## Related Documents

- [WI015 handoff](WI-20261011-ATS-015-handoff.md): Scope and acceptance contract.
- [WI014 latest review](WI-20261011-ATS-014-evidence-pack.md#latest-addendum-exact-loopback-x-bind): Final preflight identity and conditions.
- [REQ001](../user/REQ-20261011-ATS-001.md): Approval and bounded completion.
- [WI015 summary](../user/WI-20261011-ATS-015-summary.md): User-facing deliverable.
