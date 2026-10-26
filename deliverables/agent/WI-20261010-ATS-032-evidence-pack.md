---
version: 1.0
last_updated: 2026-10-10
project: ATS
owner: DocOps
category: evidence-pack
status: stable
related_wi: WI-20261010-ATS-032
dependencies:
  - path: WI-20261010-ATS-032-handoff.md
    reason: Existing skill handoff and exact four-document ownership
  - path: WI-20261010-ATS-031-evidence-pack.md
    reason: Final independent PG PASS and public identities
  - path: WI-20261010-ATS-028-evidence-pack.md
    reason: Preserved prior bootstrap evidence, not current cleanup
---

# Evidence Pack: WI-20261010-ATS-032

## Summary

TL;DR: **MA native JDBC PASS, current-window cleanup and scoped document checks
complete; REQ009 closed within approved scope. Production HOLD.** Existing app JDBC driver
connection and read-only aggregates were verified, not Hikari/Hibernate/Spring
startup. DocOps integrated evidence only on 2026-10-10, local Windows/PowerShell,
shared dirty main at `58b1011dad796017e47dbcc82a4225ca449bcdf6`.

## Scope / DoD Check

- [x] Approved REQ009, existing WI032 handoff and four-file ownership confirmed.
- [x] WI029/030/031 completion and actual MA native/host/Windows receipts integrated.
- [x] Two deliverables, remaining decisions and WI chain recorded.
- [x] Final four-document checks and preservation recheck.

No DocOps remote/SQL/service operation, secret/private-body read, build, package
installation, descendant, commit or push. No private identifiers or values were
imported. Native facts below are attributed MA receipts, not a DocOps rerun.

## Reference Documents (Tier 0-2)

| Tier | Actual use |
|---|---|
| 0 | Fully injected STD-001/004/002/005: core-principles, documentation-standards, development-standards and glossary under docs/standards. |
| 1 | Injected DocOps persona/security-policy; read access-control-policy and quality-gates. |
| 2 | Read create-wi-evidence-pack and validate-docs skills, evidence-pack-standard, eval-report-template, current entry, JDBC and production-readiness runbooks. |
| Context | Read approved REQ009, existing WI032 handoff, WI028 final section, WI029/030/031 packs; MA chat supplies native/cleanup receipts. |
| Indirect pointers | WI014, CONFIGURATION, deployment, reconstruction, db-schema, schema/seed, application.yml, env example/contract, unit, validator and build.gradle are covered by WI030/031 audits; not independently re-audited here. |
| Conditional, not invoked | test/build-check and frontend/React standards: no app build, tests or frontend edits in this WI. |

ATS and DocOps tiers [0,1] confirmed from workspace.json and injection rules.
Tier0 -> persona/security -> context -> snapshot order retained. Memory served
only as navigation; current WI030/031 evidence supersedes old planning notes.

## Actual Receipt Ledger

All times below are 2026-10-10 KST. Owner and environment are separate from
document edit time. Early snapshots are superseded, not deleted.

| Receipt / time | Owner / environment | Result and limit |
|---|---|---|
| WI029; WI031 final approximately 22:35-22:46 | SE / PG, local Windows | Each 29/29 synthetic tests; actual driver no-connect smoke; final PG PASS for exact bytes and corrected procedure. Not native execution. |
| Opening 22:30:55 / 22:32:21 | MA, pinned host | Host/capture/private metadata PASS; temporary Windows agent Running/Manual and owned key unlocked. Not final cleanup. |
| 744dcf / 22:47:31 | MA, existing host | 72954880-byte archive and six regular-member hashes verified in root-private staging; native plan matches reviewed bundle. Whole JAR staged only for verification. |
| 69a353 / 22:47:44 | MA, existing host | Fresh bounded capture/private-metadata checks PASS using isolated Python. Not a privileged-capture guarantee. |
| d94e4d / 22:47:49, exit0 | MA, existing host | JDBC PASS with Java17/Connector/J9.7 from exact JAR: account/server/database, server read-only, 43 tables, 6 active plan keys, all non-plan rows0. Old reviewed observe matches exact plans/schema before/after to prior receipt; four DML grants and env metadata unchanged. Stop/remask/no-job/no-listener PASS. |
| 2c8345 / 22:48:10 | MA, existing host | FINAL_HOST_CLOSED_NO_JAVA_PRIVATE_METADATA=PASS: same boot, MySQL/Nginx exact /dev/null masks inactive, app unit absent, no relevant jobs/listeners or Java/FFmpeg/app processes, sshd syntax PASS. Private env/claim/receipt/new approval root0600 single-link. |
| 6e7d0a / 22:48:31.462, exit0 | MA, Windows | Exact owned-key selector removed only that key; empty ssh-add list rc1 BEFORE stop; hidden elevated cleanup0; fresh Win32Service Stopped/Disabled. All upload/SSH/native/cleanup sessions finished. |
| 719c55 / final integration report | MA, local checkout | main/HEAD unchanged; global git diff --check exit0 with pre-existing CRLF normalization warnings only. No formatting rewrite; not a check of untracked file contents. |

Existing keys/data/history/stages preserved. No DB writes, grants or secrets
changed; no public/SMTP/Provider/install/fee/domain/DNS operation. No further MA
remote operation planned in this window. WI028 cleanup is not reused here.

## Public Identity Checks

DocOps rehashed local public inputs at 22:48 KST with
`Get-FileHash -Algorithm SHA256 -LiteralPath <public-path>`; no private hashes.
WI031's identity table supplies full executable-path provenance. Native equality
comes from MA's staging/plan receipts, not local hashing alone.

| Object | SHA-256 |
|---|---|
| Final WI031 Evidence Pack | 508c098486aac240921995c7c577b5c8062737fff9ba03a1fd1597730b7d276f |
| Final WI029 Evidence Pack | 97246c7ad2fe63665868cb7a337afe992e1e465f9d4921e4baf2cfc032103a41 |
| Selected bundle, SE/PG and MA plan receipts | 6c1ad23065f89bc0776cb628322941b166dc82dbf4f56ee15c052aa2a1860199 |
| Public archive build/WI029-public-v1.tar | a777353c974948c6eae0face78e48115141799c8e5a1d10ca6916fc2c6613cb5 |
| build/libs/ATStudio-0.0.1-SNAPSHOT.jar | 517b745479792a7eae6ce75357c7d1299ab644b50d5bc5b331fea384ccb10e0a |
| Extracted mysql-connector-j-9.7.0.jar | 0353648eaa1c91e0f4020c959abf756bc866ffd583df22ae6b6f6e0cbd43eb44 |
| RuntimeJdbcProbe.class | 452a9b652b88ee1e19c5001586b8ccbe9725087f96d4828603bdd1c82af1c493 |

Also matched WI031's artifact.json, WI028/WI030 packs and both readiness-runbook
hashes. WI032 handoff captured as
`77da8c788a0e02fb95f1df717de7c7c04e67432d8a4876e59a198a92321e9ac5`.
No self-referential Evidence Pack hash is embedded.

## Drift / Changes

| Priority / pointer | Change and impact |
|---|---|
| HIGH: docs/index.md Current Decision Entry | Add concise latest JDBC/cleanup state and WI029-032/runbook links above unchanged historical dated entries. |
| HIGH: REQ009 status, DoD, progress | Supersede obsolete PG-pending/no-agent-change opening snapshot while preserving it as history. Keep production gates separate from REQ closure. |
| MEDIUM: WI032 summary / this pack | Record two-set output, receipt owner/time/limits and next user decisions without duplicating old failures. |

No docs-category file was added; no category count change. Runbooks are reachable
from the current entry; their separate deploy/runbooks index is outside ownership.
Whole-repository link/orphan/freshness and memory-maintenance audits are outside
this four-file task; no broad cleanliness claim or cleanup authorization.

## Commands and Test Evidence

Tools checked locally: PowerShell7.6.5, Python3.14.3, Git2.53.0.windows.1.
`git status --short --branch`, `git rev-parse HEAD`, version commands and public
hash reads exited0. An initial PowerShell foreach-to-pipeline hash command
failed parsing before execution; the collection-first form succeeded. No write.

Scoped skill validation (no files written, no validator changes):

```powershell
python -X utf8 -B -c "import runpy; from pathlib import Path; from unittest.mock import patch; m=runpy.run_path('.agents/skills/validate-docs/scripts/validate_docs.py'); root=Path.cwd(); files=[root/p for p in ('docs/index.md','deliverables/user/REQ-20261010-ATS-009.md','deliverables/user/WI-20261010-ATS-032-summary.md','deliverables/agent/WI-20261010-ATS-032-evidence-pack.md')]; v=m.get('DocValidator')(root); checks=[v.validate_tier0_docs()]; scope=patch.object(Path,'rglob',return_value=iter(files)); scope.start(); checks.append(v.validate_internal_links()); scope.stop(); scope=patch.object(Path,'rglob',return_value=iter(files)); scope.start(); checks.append(v.validate_traceability_ids()); scope.stop(); print('SCOPED_WI032',len(files),'FILES',len(v.errors),'ERRORS',len(v.warnings),'WARNINGS'); raise SystemExit(0 if all(checks) and not v.errors and not v.warnings else 1)"
git diff --check -- docs/index.md deliverables/user/REQ-20261010-ATS-009.md deliverables/user/WI-20261010-ATS-032-summary.md deliverables/agent/WI-20261010-ATS-032-evidence-pack.md
```

At 22:51 KST, the scoped validator exited0: Tier0 presence, internal file links
and 43 supported IDs, 0 errors/warnings. Scoped diff --check exited0 with only
the existing CRLF normalization warning. Direct PowerShell checks passed seven
metadata fields, dependency existence, trailing whitespace and final newline
for all four files, including untracked files; new WI032 docs are ASCII. A
bounded private-key/JWT pattern scan found no matches, not generic DLP proof.

In-memory comparison confirmed the entire original index suffix from Production
Preparation Window unchanged after CRLF/LF normalization, and the REQ opening
snapshot retained. Repeated Get-FileHash checks matched all 12 captured public
inputs; HEAD stayed unchanged. These checks were repeated after closure edits.
No app tests, native rerun, coverage, external links, fragment validation or
repository-wide index/orphan audit by DocOps.

## Risks / Rollback / Evaluation

Hibernate NOT_RUN, canonical manifest UNRECORDED and app_runnable=false remain.
No Hikari/Spring startup proof. Whole JAR is root-private verification staging;
SPA stays local. Settings audit does not apply/clear domain/mail placeholders.
Prior development Gmail evidence remains valid, not new-host SMTP PASS.
Baseline 2vCPU/8GB/100GB and KRW70000 budget are not capacity/billing guarantees.

Rollback: correct only this WI's additions/updated status through a scoped patch;
retain dated receipts and unrelated dirty content. Do not reset Git, delete
stages, replay bootstrap, alter data/keys or change runtime. Evaluation: separate
driver-level native proof from startup, and require fresh cleanup receipts for
each window; no policy/retrospective change authorized here.

## Follow-ups / WI Chain

WI032 has Blocks:- and is the last planned REQ009 WI. With actual receipts and
documentation checks complete, bounded REQ009 is closed; production HOLD remains. No descendant or
new WI is issued here. Next proposed work is MA-scoped domain/mail decisions:
confirm atmusic.co.kr control or exact purchase authority before DNS/HTTPS,
without asserting availability/price; select mail account/sender/private entry.
Future activation, schema/Hibernate, recovery/backup, ADMIN, alerts and load
require separately approved work, not automatic continuation from JDBC PASS.

## Related Documents

- [REQ009](../user/REQ-20261010-ATS-009.md): Scope and closure.
- [Summary](../user/WI-20261010-ATS-032-summary.md): Bounded user-facing result.
- [WI029](WI-20261010-ATS-029-evidence-pack.md): Probe identities and tests.
- [WI030](WI-20261010-ATS-030-evidence-pack.md): Configuration audit and decisions.
- [WI031](WI-20261010-ATS-031-evidence-pack.md): Final independent review.
- [Current entry](../../docs/index.md#current-decision-entry): Latest result.
- [JDBC runbook](../../deploy/single-vps/runbooks/jdbc-readiness.md): Procedure boundary.
- [Production readiness](../../deploy/single-vps/runbooks/production-readiness.md): Remaining gates.
