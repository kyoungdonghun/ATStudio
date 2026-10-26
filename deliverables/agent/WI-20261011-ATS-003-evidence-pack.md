---
version: 1.0
last_updated: 2026-10-11
project: ATS
owner: DocOps
category: evidence-pack
status: stable
related_wi: WI-20261011-ATS-003
dependencies:
  - path: WI-20261011-ATS-003-handoff.md
    reason: Skill-generated assignment and four-file write scope
  - path: WI-20261011-ATS-002-evidence-pack.md
    reason: Initial independent review and exact candidate identities
  - path: WI-20261010-ATS-032-evidence-pack.md
    reason: Prior bounded JDBC window, not this window's cleanup
---

# Evidence Pack: WI-20261011-ATS-003

> Purpose: Integrate safe MA receipts without promoting local tests or prior cleanup into native acceptance.

## Summary

TL;DR: **WI003 reporting integration complete; final MA safe-closure receipts
and scoped document checks PASS. REQ010 remains partial / HOLD_DNS_ISSUANCE,
not TLS-ready.** Corrected
Windows/Ubuntu36 PASS and independent rereview supersede the test-isolation HOLD;
temporary HTTP exposure is now withdrawn. DocOps works on local Windows /
PowerShell, shared dirty `main`, HEAD
`58b1011dad796017e47dbcc82a4225ca449bcdf6` on2026-10-11 KST.
Initial local context capture was00:19 KST; document edit time is not native
observation time. MA owns the remote observations below. DocOps did not repeat
them or access native/browser/private configuration, keys, account data or logs.

## Scope / DoD Check

- [x] Existing WI003 handoff, approved REQ010 and disjoint scope verified.
- [x] Initial WI001/002 local35/35 PASS recorded as local evidence only.
- [x] Domain observation separated from user-reported payment.
- [x] Native34/35 result and subsequent test-only approval recorded.
- [x] Current entry, REQ progress and two-set deliverables created.
- [x] Corrected Windows/Ubuntu retests and independent rereview reconciled.
- [x] Actual DNS/package/Nginx/firewall receipts and CA/renewal NOT RUN reconciled.
- [x] Final app/DB, monitoring, continuing/boot and SSH/session cleanup recorded.
- [x] Final scoped document validation and preservation checks after MA receipts.

No implementation, remote operation, installation, service start, cloud change,
secret read, descendant, commit or push by DocOps. Runtime is MA-owned; this draft
grants no permission beyond the approved REQ and action-time gates.

## Reference Documents (Tier 0-2)

| Tier | Actual context and use |
|---|---|
| 0 | Full injected STD-001/004/002/005: core-principles, documentation-standards, development-standards, glossary. |
| 1 | Full injected DocOps persona and security-policy; supplied AGENTS.md entry rules. |
| 2 | Read validate-docs and create-wi-evidence-pack skills; existing WI003 handoff checked before output creation. |
| Context | Read current docs/index.md, REQ010, complete WI001/002 packs, domain-tls runbook and WI032 prior receipt/check sections. |
| Provenance | workspace.json confirms ATS; context-injection-rules.json confirms DocOps tiers[0,1]. Tier0 -> persona/security -> task context -> snapshot supplied; no further injection/delegation. |

Memory was only a navigation hint to current-entry evidence, not current-state
authority. Latest on-disk evidence and MA updates supersede older planning notes.

## Actual Receipt Ledger

All current-window dates are2026-10-11 KST. Where MA supplied no exact time, none
is invented. Receipt order below is message order. Earlier observations remain
bounded to their capture; an operation in progress is not inferred successful.

| Receipt / time | Owner / environment | Result and boundary |
|---|---|---|
| Initial preparation | SE / Windows, Python3.14.3 |35/35 PASS,0.367s, exit0 in WI001. Native commands mocked. |
| Initial independent review; clock sample00:15:01 | PG / Windows, Python3.14.3 |35/35 PASS,0.281s, exit0 in WI002. Exact candidate PASS; not native success. |
| Registration observation; exact time not supplied | MA / provider console | atmusic.co.kr in use, registered2026-10-10, expires2027-10-10. KRW18700 paid is user report only; no receipt audit. |
| Opening checks; recovery00:07 | MA / pinned native target and Windows | Administrative SSH/sudo verified; sole expected key loaded, agent Running/Manual. Authenticated root recovery verified. No cleanup claim. |
| Opening isolation; exact time not supplied | MA / native target | Nginx/MySQL masked/inactive, app absent/inactive, Java/FFmpeg0, SSH/loopback DNS listeners only. DNS draft unsubmitted and provider inbound SSH-only. |
| Package simulation; exact time not supplied | MA / native target |7 new/0 upgraded/0 removed,498 kB download,2433 kB disk; free92248 MiB. No installation/capacity guarantee. |
| Candidate transfer; exact time not supplied | MA / native target | Public tar staged in new management-user0700 and root0700 directories; seven public source hashes match PG. No private render/install equality claimed. |
| Native unittest HOLD; exact time not supplied | MA / Ubuntu Python3.12 |35 run,34 PASS/1 ERROR in test_any_earlier_failure_stops_reload: real check_path reached before intended check_lineage due to missing non-target guard mocks, yielding FileNotFoundError at the absent quarantine path. Windows incidental earlier GateError did not prove the intended guard. |
| Latest mutation/cleanup snapshot with HOLD | MA / native target and Windows | No packages/install/DNS/firewall/Nginx/app/DB changes; agent still Running/Manual with expected key; recovery root logged in. Staging occurred, so this is not a no-host-writes claim. |
| Subsequent explicit user approval; exact time not supplied | User via MA update / WI001 SE | Test-only isolation fix plus Windows/Ubuntu retests and independent rereview approved. SE editing test/evidence addendum only; production candidate files unchanged per update. No corrected result yet supplied. |
| Corrected local evidence read at00:21 KST | SE / Windows Python3.14.3, WI001 addendum |36/36 PASS,0.296s, exit0; guard-target isolation corrected and missing-filesystem case added. Six non-test implementation/runbook files unchanged. Ubuntu retest and PG rereview pending. This later result supersedes the preceding awaiting-local-result snapshot. |
| Independent correction review; clock sample00:21:56 | PG / Windows Python3.14.3, WI002 addendum |36/36 PASS,0.271s, exit0; test-only delta PASS. At that document snapshot MA native rerun was pending; subsequent native receipt below is attributed to MA. |
| Corrected native retest;00:24+ update | MA / Ubuntu Python3.12 | Exact corrected test transfer/hash,36/36 PASS,0.065s in protected r2 stage; original failed stage retained. Independent PG rereview36 PASS also reported by MA. |
| Package application;00:24+ update | MA / native target | All seven deb SHA-256 values match signed apt index; maintainer scripts/cron/service/timer reviewed; exact seven installed with zero upgrades/removals; Certbot2.9.0 verified. Certbot service/timer runtime-masked/inactive; MySQL masked/inactive. |
| Bootstrap application;00:24+ update | MA / native target | New bootstrap/drop-in exact comparison, metadata/no-ACL checks and native nginx -t PASS; effective unit selects quarantine only; Nginx started IPv4 port80 only. Loopback synthetic200/root404/API404/POST403. Provider still SSH-only; no external proof. |
| DNS zone creation00:24:44 | MA / provider console | Zone creation confirmed; apex A save/verification pending. No CA consent/issuance/firewall changes yet. No app/DB action reported. Final cleanup still unconfirmed. |
| Firewall action; later same-window update | MA / provider console and native SSH | Explicit action-time approval and console success;22 restriction unchanged,80 all IPv4 for ACME,443 approved management/32 only. Fresh SSH PASS; app/MySQL remain off. This supersedes the earlier SSH-only snapshot. |
| Apex A save00:25:52 / SOA serial2026101101 | MA / provider console and DNS probes | ns2 AA/NOERROR SOA/A positive, AAAA/CAA NODATA, apex NS NODATA; ns1 REFUSED. Cloudflare/Google recursive A positive, Cloudflare EDE records ns1 REFUSED. Not globally complete DNS. |
| External HTTP probes; exact time not supplied | MA / external Windows client | Synthetic token200/exact body; root/API/uploads/missing/query/encoded/dot/traversal404; POST403. Unknown Host curl56 connection-reset/http000; initial assertion expected52 but actual connection closed as required. No exposure or HTTPS acceptance inferred. |
| Bounded DNS interpretation and stop plan; exact time not supplied | PG interpretation relayed by MA | CA HOLD: parent delegation is not child apex NS evidence, authoritative apex NS NODATA is an inconsistency, REFUSED is not DNSSEC proof. No guessed changes. One final bounded check then safe closure if unchanged; no CA account/terms/issuance and renewal remains masked. |
| Follow-up DNS00:31 | MA / direct authoritative and parent probes | Earlier primary REFUSED resolved. Both ns1/ns2 AA/NOERROR A/SOA, matching expected address and serial2026101101; both AAAA/CAA NODATA, both apex NS AA NODATA persists. Direct parent query returned exact two-server referral and DS AA NODATA. Earlier recursive NS is not parent proof. |
| Decision gate after00:31 | MA / PG assessment and user decision pending | Whether to continue bounded CA staging while tracking provider child apex NS inconsistency requires a user decision; no app-release authority. CA account/terms/issuance still NOT RUN, cleanup unconfirmed. The earlier stop-if-unchanged plan is not a cleanup receipt. |
| Subsequent PG advice; exact time not supplied | PG via MA update | Direct parent delegation and both authoritative SOA/A positive establish no concrete CA security blocker; bounded staging then production on PASS is permitted only after explicit user variation approval. User acknowledgment/continuation and LE v1.8 staging/production/renewal no-email consent are pending. Root staging/packages/bootstrap/firewall exist; no CA accounts/certificates created. |
| Latest direct user response relayed by MA; exact time not supplied | User / MA | CA terms/no-email issuance approved. User did NOT approve unresolved child apex NS variation and instead requested resolution/explanation. Account registration/issuance remain HOLD until DNS resolution or explicit later variation. Provider internal cause unknown; no provider inquiry sent. MA proceeding to safe temporary-window closure; final receipts pending. |
| Final DNS00:35:40 | MA / both authoritative servers | Both A records correct; both apex NS AA/NOERROR/NODATA with SOA serial2026101101. Earlier primary REFUSED resolved. MA checked RFC1034 section4.2.1 primary text: child apex NS should match parent delegation. Internal provider cause unknown; zone generation/sync is inference only. Public UI offers A/CNAME/MX/TXT, no NS edit. No inquiry sent. |
| Final consent boundary | User / MA | LE v1.8 no-email staging/production/renewal approved, but user requires DNS resolution first and has NOT approved unresolved-child-NS exception. No account registration/issuance/renewal dry-run/timer enablement or expiry alerts. |
| Runbook6 closure00:36:25 | MA / provider console and pinned target; PG bounded-closure approval | Only new inbound80/443 removed; save success and a fresh separate authenticated console tab verify22-only. Exact old all-IPv4 TCP80/443/53 and UDP53/123 egress preserved; ICMP setting untouched. Nginx stop/inactive/no jobs/no80/443 verified, unit disabled before persistent exact /dev/null mask. MySQL inactive/persistently masked. Certbot service/timer inactive, exact runtime-only /dev/null masks retained; timer enablement link absent including dangling link. No accounts/live lineage. |
| Final access/cleanup00:38:00 | MA / pinned target, recovery UI and Windows | Fresh pinned SSH/sudo exit0; root tty1 count0 after UI exit/login prompt; no80/443/8080/3306/33060/33062 listeners. Exact window SSH key removed, empty-agent exit1 verified BEFORE fresh CIM Stopped/Disabled verification. No execution running. |
| Preservation at closure | MA / same window | DNS/A, seven installed packages, new configs/stages/public tokens retained. No deletion, reboot, app/DB operation or secret access. Private local screenshot is operational evidence only, not copied, linked or embedded in tracked documentation. Current HOLD_DNS_ISSUANCE; NOT TLS-ready. |

WI002's apt-confirmation and no-ACL xattr clarifications remain recorded there;
they do not close the native test gate or establish package installation.
Prior WI032 JDBC and cleanup receipts remain valid for2026-10-10 only.

## Final State and Remaining Gates

Owner is MA unless noted. Final window receipts close documentation integration,
not the remaining REQ010 DNS/TLS acceptance. Earlier ledger rows are historical.

| Gate | Current evidence boundary |
|---|---|
| Test repair / independent rereview | Corrected SE/PG Windows and MA Ubuntu36/36 PASS recorded. Earlier test HOLD is historical; native HTTP/TLS acceptance is separate. |
| Packages / effective configuration | Seven exact packages, Certbot2.9.0 and bootstrap comparison/metadata/native parse/effective unit confirmed. Retained configs/stages are inactive; no installed-help detail beyond supplied version receipt or TLS configuration acceptance is inferred. |
| DNS / provider rules / IPv6 | Final authoritative A agrees; direct parent referral checked, child apex NS NODATA persists. Temporary80/443 rules withdrawn, SSH-only restored, existing IPv4 egress unchanged and no80/443 listeners. No end-to-end IPv6 acceptance claim. |
| CA / HTTP / TLS | Temporary external HTTP/negative probes passed but service is now stopped. LE v1.8 no-email staging/production/renewal consent approved; no account/issuance/HTTPS acceptance. User requires DNS resolution, no unresolved-NS exception approval. |
| Renewal / monitoring / continuing and boot | NOT_OPERATIONAL: no accounts/live certificate, dry-run, TLS reload, timer enablement or expiry alerts. Nginx disabled before persistent mask; MySQL persistent mask. Certbot runtime-only masks and absent timer enablement link do not prove persistent masks after reboot. No reboot test. |
| Final isolation / cleanup | MA00:36:25/00:38:00 verified stop/masks/jobs/listeners, recovered SSH, root logout, exact-key removal, empty agent BEFORE Stopped/Disabled and no execution running. PG approved bounded closure. |

## Evidence Pointers / Drift and Patch

| Priority / path | Change and impact |
|---|---|
| HIGH: docs/index.md Current Decision Entry | New latest domain/TLS HOLD checkpoint precedes unchanged dated JDBC and earlier entries. |
| HIGH: deliverables/user/REQ-20261010-ATS-010.md | Current progress/approvals and actual safe closure integrated; opening snapshot retained; REQ stays partial/HOLD with renewal acceptance unfinished. |
| MEDIUM: deliverables/user/WI-20261011-ATS-003-summary.md | Bounded partial-deployment and safe-closure result, distinct from REQ completion. |
| MEDIUM: deliverables/agent/WI-20261011-ATS-003-evidence-pack.md | This skill-formatted receipt ledger, source pointers, checks and rollback. |

No new docs-category file, so category counts stay unchanged. Current entry links
the runbook and evidence. No whole-repository orphan/freshness/memory audit or
unrelated cleanup is within this scope. Historical evidence is not rewritten.

## Commands and Test Evidence

Executed read-only: `git status --short --branch`, `git rev-parse HEAD`, focused
`Get-Content`, `rg`, public `Get-FileHash -Algorithm SHA256`, and `Get-Date -Format o`.
All completed; initial hash table display was truncated, then full identities
were captured below. Initial index/REQ text retained
in session memory for preservation comparison. `apply_patch` is the only editor.

At00:21 KST the existing validate-docs script was loaded read-only with `runpy`.
`Path.rglob` was scoped in memory to the four owned files separately for
`validate_internal_links` and `validate_traceability_ids`; `validate_tier0_docs`
also ran. Result:47 supported IDs,0 errors,0 warnings, exit0. This is not a
repository-wide index/orphan or external-link/fragment audit.

`git diff --check -- docs/index.md deliverables/user/REQ-20261010-ATS-010.md deliverables/user/WI-20261011-ATS-003-summary.md deliverables/agent/WI-20261011-ATS-003-evidence-pack.md`
exited0 with the existing index CRLF normalization warning only. Direct
PowerShell checks covered untracked files too: seven metadata fields,
dependency existence, no trailing whitespace, final newline and ASCII for the
two new documents. In-memory comparison found the entire old index suffix from
the2026-10-10 JDBC heading unchanged after newline normalization and all original
REQ opening-snapshot text preserved. These are initial draft checks and must be
rerun after final integration.

Full local public identities captured at00:21 KST (no native equality inferred):

| Input snapshot | SHA-256 |
|---|---|
| WI001 Evidence Pack after SE correction | `19bfda062e99323791d0cb98807b254c04cd31d6a8d1ca39268681f2e1e8b24e` |
| WI002 initial Evidence Pack, before pending rereview | `820d1694bf6db86968c18a327436c2cee34ed64fee64086f2c9ba9190fab60a6` |
| WI003 handoff | `48b00c742f84492c0d8be9e675d5c1cef7869a95a216aaa6a24206117cdf7974` |

WI001's corrected test identity is
`a9ca1406ef209a9eb67406dbf14c889ca85d5c5eab01274933b1cd78bcf56436`
per its addendum. At00:23 KST DocOps rehashed the six non-test source/runbook
files against the initial WI001 table and the corrected test against this digest;
all matched. This was local byte verification only. Native transfer equality is
the separate MA receipt above. WI002's initial digest is historical after its
correction addendum, not the current report identity.
No application tests, native reruns, measured coverage, external-link checks or
full-repository cleanliness claims by DocOps.

### Final Integration Checks

At00:40 KST, after final MA receipts, the same four-file skill validation passed:
Tier0 present,47 supported IDs,0 errors/0 warnings, exit0. Scoped diff check
exited0 with only the pre-existing index CRLF normalization warning. Direct
metadata/dependency/whitespace/final-newline checks passed all four files and
ASCII checks passed both new deliverables. The complete historical index suffix
and REQ initial snapshot still match the captured baseline after newline
normalization; REQ status remains approved / partial / HOLD_DNS_ISSUANCE.
HEAD stayed `58b1011dad796017e47dbcc82a4225ca449bcdf6`.

Reproduction of scoped skill validation, without file writes:

```powershell
python -X utf8 -B -c "import runpy; from pathlib import Path; from unittest.mock import patch; m=runpy.run_path('.agents/skills/validate-docs/scripts/validate_docs.py'); root=Path.cwd(); files=[root/p for p in ('docs/index.md','deliverables/user/REQ-20261010-ATS-010.md','deliverables/user/WI-20261011-ATS-003-summary.md','deliverables/agent/WI-20261011-ATS-003-evidence-pack.md')]; v=m.get('DocValidator')(root); checks=[v.validate_tier0_docs()]; scope=patch.object(Path,'rglob',return_value=iter(files)); scope.start(); checks.append(v.validate_internal_links()); scope.stop(); scope=patch.object(Path,'rglob',return_value=iter(files)); scope.start(); checks.append(v.validate_traceability_ids()); scope.stop(); print('SCOPED_WI003',len(files),'FILES',len(v.errors),'ERRORS',len(v.warnings),'WARNINGS'); raise SystemExit(0 if all(checks) and not v.errors and not v.warnings else 1)"
```

Final public-input rehash: corrected WI001 pack and WI003 handoff still match
their00:21 table identities. WI002 after its correction addendum is
`3d6e25482f7cbde1f32edc0f786bf529ffabba602f1526e0acfecb64f512b899`;
its earlier digest remains historical. Output identities are returned separately
to avoid a self-referential hash. No screenshot or private operational body was
read/copied into these documents. Document validation does not independently
reproduce MA's native receipts or prove public readiness.

## Risks / Rollback

Local PASS did not reveal a native test-isolation error; preserve that discrepancy
and require corrected evidence before accepting native test claims. Source staging
is not configuration installation. CA consent is not inherited from domain
registration. This window's SSH recovery is closed per fresh MA receipts;
future work requires fresh target/approval/recovery checks. Provider internal
DNS cause is unknown; no self-directed provider inquiry or repair was performed.

Rollback is a focused documentation correction to only these four paths. Retain
historical evidence and unrelated dirty content; no Git reset or file deletion.
Runtime recovery belongs to MA under the domain-tls runbook, not this document.
No template, policy or architecture change is proposed.

## Follow-ups / WI Chain

WI003 has Blocks:- and launches no descendants. MA requested completion of
partial-deployment documentation after bounded DNS checks and safe closure;
the latest user response does not approve proceeding with unresolved child NS.
Final safe receipts and scoped document checks complete WI003 reporting
integration, preserving REQ010 partial/HOLD_DNS_ISSUANCE. Correction and independent
retests are recorded, not duplicated by DocOps. Unfinished DNS/TLS acceptance
requires later MA/user action, not an implicit new task or indefinite wait.

## Related Documents

- [Handoff](WI-20261011-ATS-003-handoff.md): Scope and final-receipt gate.
- [REQ010](../user/REQ-20261010-ATS-010.md): Approval and current progress.
- [Summary](../user/WI-20261011-ATS-003-summary.md): Bounded user-facing result.
- [WI001](WI-20261011-ATS-001-evidence-pack.md): Preparation, identities and later SE evidence.
- [WI002](WI-20261011-ATS-002-evidence-pack.md): Independent review and later PG evidence.
- [WI032](WI-20261010-ATS-032-evidence-pack.md): Preserved prior JDBC window.
- [Current entry](../../docs/index.md#current-decision-entry): Current checkpoint.
- [Domain/TLS runbook](../../deploy/single-vps/runbooks/domain-tls.md): MA procedure and recovery.
