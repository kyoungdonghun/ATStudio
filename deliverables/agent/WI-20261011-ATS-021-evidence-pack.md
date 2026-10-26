---
version: 1.0
last_updated: 2026-10-11
project: ATS
owner: docops
category: evidence-pack
status: confirmed
dependencies:
  - path: WI-20261011-ATS-021-handoff.md
    reason: Approved documentation-only ownership
  - path: WI-20261011-ATS-020-evidence-pack.md
    reason: Completed QA packaging and provenance
---

# Evidence Pack: WI-20261011-ATS-021

> Purpose: Close approved local preparation while retaining historical evidence and production HOLD.

## Summary

REQ002 local scope complete; WI021 Blocks none. Only the current index, REQ002
and this WI's two reports changed. New JAR is a local candidate, not uploaded,
native-validated, started or published. No target operation occurred in this WI.

## Scope / DoD Check

- [x] Read actual handoff, REQ, WI016, WI017 repair, WI018 superseding PASS, WI019 and WI020.
- [x] Match the new manifest path/pin and local artifact path/size without duplicating MA's hash audit.
- [x] Add a newest dated index entry; preserve all prior checkpoints and HOLD history.
- [x] Increment REQ DoD and close only the approved local chain after QA PASS.
- [x] Record remaining target/native/secret/startup/operational gates and two-set output.
- [x] Scoped document checks PASS; earlier index checkpoints and REQ progress preserved exactly.

## Reference Documents (Tier 0-2)

| Tier | Input | Use |
|---|---|---|
| 0 | core-principles.md STD-001; documentation-standards.md STD-004; development-standards.md STD-002; glossary.md STD-005 | Fully injected in order; approval, documentation and terms |
| 1 | DocOps persona; security-policy.md; quality-gates.md | Supplied role/security; local quality policy read |
| 2 | docs/index.md; CONFIGURATION.md; production-readiness.md | Latest entry and relevant dated guard/mail/ADMIN/operations boundaries |
| 2 | REQ002; WI021 handoff; WI015 evidence; old WI026 manifest identity in QA evidence | Authority, dated native proof and retained artifact boundary |
| 2 | WI016-020 evidence and WI020 public manifest | Exact source, repair closure, regression and packaging |
| Skills | validate-docs; create-wi-evidence-pack | Both read; existing handoff precondition passed |
| Registry | .claude/config/workspace.json | ATS tag confirmed |

Order: Tier0 -> DocOps/security -> task/evidence -> snapshot. Rule-source pointer:
`.claude/config/context-injection-rules.json`; assignee docops, documentation,
required Tier0. No sub-delegation. Memory lookup supplied navigation only;
current files and attributed MA observations govern the completion, not older
resource-design notes. No memory edit or broader freshness/cleanup audit.

## Drift Report and Applied Fix

| Priority | Before / impact | Applied scope |
|---|---|---|
| High | Current entry identified only the older native probe, not the newly packaged candidate | docs/index.md v3.21 adds exact WI020 identity and scoped completion before intact earlier entries |
| Medium | REQ002 still awaited packaging/integration | REQ002 v1.1 checks final DoD and appends completion while preserving progress/HOLD/repair history |
| High | Old readiness pins and OFF shorthand could imply wrong artifact or runtime safety | New entry explicitly separates expected backend drift, manual/event/root writes and future target proof; no historical source/runbook edit |

No new category document was added under docs; no document-count change was
needed. The current entry links both new reports and REQ; the REQ links back.
No new canonical term or policy was introduced.

## Exact Identity and Attributed Evidence

Local branch/HEAD observed by DocOps: `main`,
`58b1011dad796017e47dbcc82a4225ca449bcdf6`, with pre-existing dirty changes.
QA's manifest identifies641 backend inputs from that dirty tree, not a clean
HEAD release. DocOps parsed its public JSON and checked the new file path/size
and declared SHA-256 against QA evidence; no full rehash or rebuild.

| Item | Identity / boundary |
|---|---|
| New JAR | `build/WI020-release-v3/build/libs/ATStudio-0.0.1-SNAPSHOT.jar`; 70,306,351 bytes |
| JAR SHA-256 | `01d5623972e17d0b6e1d3e532096239cd87e9995a08771c59292c94bf3699dcb` |
| Manifest | `deliverables/agent/WI-20261011-ATS-020-artifact.json` |
| Manifest SHA-256, QA/MA attributed | `8a74e29f15708dad5a8278dffc3e8add1eb7b8dbcd6ddb18045ec85c32c63fe6` |
| Preserved old JAR | `build/libs/ATStudio-0.0.1-SNAPSHOT.jar`; `517b745479792a7eae6ce75357c7d1299ab644b50d5bc5b331fea384ccb10e0a` |
| Reusable separate SPA | `build/WI-20261010-ATS-026-frontend-dist`,143 files; historical336 inputs match, four coverage-test exclusions retained in WI020 |

| Owner / date / environment | Verified result, not rerun by DocOps |
|---|---|
| SE, 2026-10-11 around11:01 KST, Windows mock contexts | WI01668/68 focused tests |
| RE, 2026-10-11 11:06:11-11:07:03 KST, Windows/Java17/Gradle9.3.0 | WI01987/87 = original68 independently rerun +19 new; not155 distinct tests |
| SA repair and PG around11:18 KST, 2026-10-11 Windows/Python | Candidate19/19 and unchanged PG harness27/27; initial8 survivors now rejected; nested owner tests not added again |
| QA, 2026-10-11 11:25:45-11:26:15 KST build;11:29:41 final checks, Windows11/Java17/Gradle9.3.0 | Offline package PASS;136 model classes, schema/seed and101 libraries identical to old artifact;7,912 preservation records unchanged |
| MA, 2026-10-11 11:32:40.1665263 KST, local Windows | User-supplied independent final read-only check:1,130 unique public records matched (backend641/frontend336/SPA143 plus PG pins/new JAR deduplicated); six additional immutable pins matched |
| MA, same local observation | Additional pins: old JAR, WI026 manifest, WI011 artifact, original service/env and WI020 manifest. SSH agent Stopped/Disabled. Scoped source/candidate git diff --check exit0, tracked-only; informational CRLF warning |
| MA, 2026-10-11 10:34:46 KST, prior approved Linux target | WI015 native read-only Hibernate PASS belongs to its exact old probe/target, not the new whole executable |

MA's new message is attributed evidence; this WI did not independently repeat
its hash or service-state checks. No new Linux/remote verification is implied.
The old release-readiness.py remains pinned to WI026. Expected changed-backend
drift is not a regression verdict; never rewrite immutable pins as a shortcut.

## Commands and Test Evidence

DocOps ran read-only `git status --short --branch`,
`git log -1 --format="%H %s"`, scoped `rg`/`Get-Content`, JSON parsing with
`ConvertFrom-Json`, and `Get-Item` for new JAR/SPA existence and JAR size.
Manual edits used apply_patch only. No build, test suite or review replay.

Document verification PASS, owner DocOps, 2026-10-11 Windows/PowerShell/Python.
Used the existing
`.agents/skills/validate-docs/scripts/validate_docs.py` DocValidator with
Markdown enumeration restricted to the four owned files plus WI016-020 public
Evidence Packs, summaries and handoffs (19 files total). Check Tier0 existence,
file-link targets and supported traceability IDs. Supplement with required
metadata/dependency-target, direct whitespace/EOF and exact WI-chain pointers.
Direct checks include untracked files; git diff alone does not cover them.

Result:19 Markdown files,237 internal file-link occurrences,66 supported IDs;
0 validator errors/warnings. Metadata field presence, dependency targets,
trailing whitespace, terminating newline, all WI016-021 handoff/evidence/summary
paths and new index links PASS. The four owned files also pass single-newline
EOF checks; retained reports' existing extra terminal blank lines are preserved.
Metadata checks are structural, not a full YAML/schema audit. Full prior index
checkpoint tail and original REQ progress/HOLD text match session pre-edit
snapshots exactly. Scoped `git diff --check -- docs/index.md
deliverables/user/REQ-20261011-ATS-002.md
deliverables/agent/WI-20261011-ATS-021-evidence-pack.md
deliverables/user/WI-20261011-ATS-021-summary.md` returned exit0; the index's
existing CRLF normalization notice is informational.

Reproduction: import DocValidator using importlib with `python -B -`; restrict
`Path.rglob` through unittest.mock to the explicit four owned paths and the
15 WI016-020 handoff/evidence/summary paths. Call validate_tier0_docs,
validate_internal_links and validate_traceability_ids, assert empty errors/
warnings, then directly check the structural metadata, dependency paths and
whitespace as above. No helper file, bytecode or report log was written.

Whole-repository validation is excluded. The known first-host-setup.md
PowerShell int-cast/Read-Host parser false positive is out of scope and was not
repaired. The first scoped run reproduced that false positive in this report's
literal quotation; only this report's wording was corrected before rerun.
External URL reachability, link-anchor semantics, full index/orphan audits,
application tests/coverage and performance are not part of this check. No
whole-repository PASS is claimed.

## Remaining Gates / Rollback

Master OFF is not a filesystem-read-only or egress fence: manual calls,
AFTER_COMMIT, pool activity and root/log/temp creation remain possible.
Strict storage/JWT/keyring/DDL guards remain mandatory. False prestart and
new-JAR placeholder are unchanged. Native syntax, effective systemd/BPF,
IPv4/IPv6 denial, loopback/Unix relays, actual OFF binding and exact cgroup/
port/job cleanup are NOT RUN for the new executable. Canonical fresh manifest
is UNRECORDED; `app_runnable=false`; no production/capacity clearance.

Next requires a separate exact remote approval covering new-identity staging,
existing-secret use, internal DB and fence proof, then bounded private app
start with no external route. SSH-key entry is private and only then, not now.
Existing Gmail TEST success remains valid; production SMTP, initial ADMIN,
backup/restore, NS, expiry alerts, first unattended renewal, actual reboot
and capacity remain open. Prior Linux cleanup is dated, not freshly observed.

No SSH, private env read, secrets, DB service, remote API, full app, purchase,
install, publication, deletion, memory edit, commit or push occurred here.
Rollback is an approved withdrawal/superseding correction of only this WI's
new documentation integration, preserving earlier history and both reports.
Do not revert whole dirty files or deploy the old JAR as an automatic fallback.

## Follow-ups and Chain

WI016/017 -> WI018/019 -> WI020 -> WI021 is complete for REQ002.
WI021 Blocks none; no further WI in this approved chain. Future operations
need a new approved scope; none were delegated or initiated by DocOps.

## Related Documents

- [WI021 handoff](WI-20261011-ATS-021-handoff.md): Unchanged authority.
- [REQ002](../user/REQ-20261011-ATS-002.md): Scoped completion and preserved progress.
- [Current decision entry](../../docs/index.md#current-decision-entry): Superseding local checkpoint.
- [User summary](../user/WI-20261011-ATS-021-summary.md): Compact result.
- [WI020 QA](WI-20261011-ATS-020-evidence-pack.md): Package and preserved identities.
- [WI018 superseding PASS](WI-20261011-ATS-018-evidence-pack.md#2026-10-11-independent-repair-retest-local-pass): Repair closure.
- [WI019 RE](WI-20261011-ATS-019-evidence-pack.md): Independent regression.
- [WI015 native evidence](WI-20261011-ATS-015-evidence-pack.md): Historical target proof.
