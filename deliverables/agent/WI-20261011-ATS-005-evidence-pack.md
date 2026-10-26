---
version: 1.0
last_updated: 2026-10-11
project: ATS
owner: DocOps
category: evidence-pack
status: stable
related_wi: WI-20261011-ATS-005
dependencies:
  - path: WI-20261011-ATS-005-handoff.md
    reason: Skill-generated scope and final receipt gate
  - path: WI-20261011-ATS-004-handoff.md
    reason: Independent resume review dependency
  - path: WI-20261011-ATS-003-evidence-pack.md
    reason: Preserved previous-window closure, not current runtime evidence
---

# Evidence Pack: WI-20261011-ATS-005

> Purpose: Separate latest continuation approval, unresolved DNS, actual receipts and unperformed gates.

## Summary

TL;DR: **Final MA receipts integrated; WI005 documentation reporting complete.
Restricted TLS/renewal configuration PASS; REQ010 partial with NS unresolved,
alerts absent and first unattended run/reboot unproven.** Old
HOLD_DNS_ISSUANCE is historical authorization state, not the current decision.
Authorization alone did not establish runtime success; subsequent actual receipts
below establish bounded results without resolving DNS.

DocOps environment: Windows/PowerShell, shared dirty `main`, HEAD
`58b1011dad796017e47dbcc82a4225ca449bcdf6`, local capture2026-10-11 00:59 KST.
Operational observation times below are MA-supplied, not inferred from this edit.
No remote/browser/private file or screenshot access, CA operation, install,
billing, configuration edit, descendant, commit or push by DocOps.

## Scope / DoD Check

- [x] Existing skill-created WI005 handoff and four-file scope verified.
- [x] Explicit unresolved-NS continuation and temporary-agent approval recorded.
- [x] Current entry supersedes prior HOLD as approval, preserving history.
- [x] WI004/WI005 appended to REQ parallel plan.
- [x] NS NOERROR/NODATA, unknown cause and no app/DB release remain explicit.
- [x] WI004 final review and actual MA operational receipts integrated.
- [x] Staging/production/TLS/renewal/alerts/final cleanup distinguished by evidence.
- [x] Final scoped validation, source/history preservation and identity checks.

## Reference Documents (Tier 0-2)

| Tier | Actual use |
|---|---|
| 0 | Fully injected core-principles(STD-001), development-standards(STD-002), documentation-standards(STD-004), glossary(STD-005); evidence-pack-standard supplied as an additional governing reference. |
| 1 | Fully injected DocOps persona, security-policy and quality-gates. |
| 2 | Injected create-wi-evidence-pack skill applied after saved-handoff check; validate-docs skill refreshed from disk. Domain-tls runbook retained from prior complete read, subject to current identity checks. |
| Context | Read saved WI005/WI004 handoffs, WI004 review evidence, current REQ/index and WI003 closure pack. MA supplies explicit approval and final operational receipts; final PG PASS/no blockers is separately reported by MA. |
| Provenance | Existing workspace.json ATS tag and DocOps tiers[0,1] from earlier checks; current handoffs also identify ATS. Four Tier0 documents remain injected before role/task evidence. |

Memory was a current-entry navigation hint only. This turn's current files and
MA instructions establish approval; old planning notes establish no runtime fact.

## Approval and Receipt Ledger

Date2026-10-11 KST. Exact MA times are recorded only when supplied. Pending
receipts must not be treated as failed tests, completed operations or fresh cleanup.

| Receipt / owner and environment | Observed result and limit |
|---|---|
| Latest user continuation, supplied by MA and saved WI005 handoff; exact time not supplied | User explicitly requests restricted HTTPS testing with NS unresolved, app/DB not exposed, and prior notice for extra costs or direct user actions. This is the missing limited variation approval; not issuance success. |
| Separate user temporary-agent approval via MA; exact time not supplied | Fresh Windows agent Stopped/Disabled before approval; key passphrase not entered at handoff. No remote change yet. WI004 handoff's earlier approval-pending sentence is superseded by this later explicit approval, not rewritten. |
| Prior CA consent retained | LE v1.8 no-email staging/production/renewal consent remains; current agreement/scope must be checked before any CA action. |
| MA pre-resume DNS, exact time not supplied | Expected parent delegation, both apex A correct, authoritative apex NS AA NOERROR/NODATA with SOA serial2026101101; independent Google/Cloudflare DoH NS agrees. Prior target REFUSED resolved. nslookup PTR REFUSED is not target NS failure. |
| MA provider UI interpretation, exact time not supplied | A/CNAME/MX/TXT supported; empty apex host is correct; own nameserver-host/glue registration not required while using provider NS. No confirmed input fix; internal cause unproven. No DocOps browser verification. |
| WI003 previous closure | Historical00:36:25/00:38:00 receipts remain valid for that window only; no accounts/certificates then. They cannot establish new runtime masks, current source or final cleanup. |
| MA fresh native preflight01:00:47 | Pinned SSH/sudo exit0, same prior boot, root recovery tty1=1; one expected key in Running/Manual agent. App unit absent, MySQL/Nginx masked/inactive, Certbot runtime-masked/inactive, no jobs/Java/FFmpeg or80/443/8080/3306/33060/33062 listeners. Free92241 MiB; Certbot2.9; accounts/live/work/log absent. |
| MA retained candidate preflight, same update | Exact seven public source hashes match; r2 renders used, bootstrap comparison/native parse PASS;22 ancestry/path ACL/symlink/write/single-link checks PASS. No runtime installation/change inferred from comparison alone. |
| MA native DNS01:00:58 | Both authorities expected A/SOA serial2026101101; NS AA NODATA unchanged, AAAA/CAA NODATA, parent referral correct/DS NODATA. Windows nslookup lacks CAA support, so it is NOT CAA evidence; native dig supplies the CAA result. |
| MA local01:00 CA directory and provider checks | Staging/production directory terms remainv1.8. No CA action/web startup change; provider SSH-only and previous five outbound rules unchanged;80/443 form draft unsubmitted and action-time approval requested. Not a closure receipt. |
| MA01:04:56 native bootstrap resume | Fresh exact bootstrap/root metadata/prior WI002 code checks PASS; only Nginx unmasked, effective unit PASS, active on IPv4 port80 only, not enabled. No other web/app/DB listeners. |
| MA action-time firewall receipt after01:04:56 | User approved80 public IPv4 ACME plus443 exact current management/32. Provider save-success/readback confirms22+80+443; previous five outbound rules/ICMP setting unchanged; fresh SSH PASS. Local private UI proof stays outside repository. |
| MA external Windows HTTP; same update | Ten probes PASS using real DNS without resolve override: exact synthetic200/body match, root/api/uploads/missing/query/encoded/traversal404, POST403. Not HTTPS or certificate acceptance. No CA action; awaiting WI004's exact no-email procedure review. |
| WI004 independent review / PG, local Windows | PASS for unchanged bootstrap reactivation and exact Certbot2.9 no-email staging register/dry-run sequence. Seven candidate hashes matched; no new suite/native/CA execution by PG. Existing36-test corrected results are historical supporting evidence, not a fresh test run. |
| MA staging registration01:08:21 | STAGING_REGISTER_EXIT0_SUCCESS; single verified staging account and private-key metadata PASS. Key/account/raw log bodies remain private, not imported. |
| MA staging validation01:08:45 | STAGING_DRY_RUN_EXIT0_SUCCESS, actual CA validation rather than synthetic HTTP. Production/TLS not yet run at this receipt. Priorv1.8 no-email consent unchanged; last direct NS anomaly observation01:00:58 remains unresolved. |
| MA production registration01:09:08 | Explicit registration exit0/success and exactly one endpoint account/private metadata PASS; no account/key/log contents disclosed. |
| MA production issuance01:09:26 | Exit0/success; exact atmusic.co.kr single SAN/lineage generation1, root0700 live/archive and0600 key metadata; OpenSSL trust/hostname and native Nginx key-pair parse PASS. Public issuer YE2; notBefore2026-10-10T15:10:54Z, notAfter2027-01-08T15:10:53Z. |
| MA native TLS installation01:09:49 | Exact retained TLS render installed; previous bootstrap moved to new protected WI004 stage preserving history; reload active on IPv4-only80/443. App/DB still off. |
| MA external Windows TLS, same update | Native curl with real DNS and certificate validation, no -k: marker200/safe headers, root/api/uploads/query/encoded/traversal404, POST403, wrong Host closed. Nine TLS probes,19 external HTTP/TLS probes total. No independent non-allowlisted denial claim. |
| MA native/hook01:10:43 | Loopback with spoofed headers403; no/unknown SNI rejected. Empty root-owned hook directories/no injected renewal hooks and explicit production webroot settings verified; reviewed same-hash hook installed0750 and manual hook PASS. Renewal dry-run still running with no result. Phone-network denial requested, user result pending. NS unresolved; no alerts; no final cleanup yet. |
| MA renewal01:13:12 | Actual renewal dry-run exit0/all simulated renewals successful; explicit TLS_QUARANTINE_RELOAD_REQUESTED hook success, no HOLD marker; Nginx active. Delay120.0514s was normal per MA's safely parsed numeric private-log value, not failure. No private log body imported. |
| MA post-renewal Windows TLS, same update | Native SslStream default trust/no override: served SHA-256 equals known production public certificate, GET marker200. Reload served production, not staging. External probe total20. Phone LTE denial still pending; timer runtime-masked/inactive, Nginx not enabled, continuing intent undecided, no expiry alerts. |
| MA closure decision / PG interpretation; subsequent update | External LTE reply not received; mark NOT_RUN, do not replace with loopback proof. User informed temporary web window will close; no further phone test needed after closure. Plan withdraw only new provider80/443, stop/quiesce/remask Nginx; retain certificates/hooks/configs and Certbot runtime masks/no enable. Renewal NOT_OPERATIONAL despite actual dry-run/hook PASS. Final cleanup not yet supplied. |
| MA native state01:14:09 | Global IPv6 count0, MySQL masked/off, app absent, Certbot processes0/no jobs; both authoritative apex NS AA NODATA, same SOA serial. No extra product purchase fee, not a no-traffic-cost guarantee. Limited test partial PASS, not REQ completion. |
| User LTE photo received after closure proposal / MA interpretation | Exact HTTPS ready.txt URL, visible LTE, ERR_CONNECTION_ABORTED/no marker, photo clock1:14. Original image remains private/local and was not copied/read by DocOps. Fresh native trusted allowed-line curl200/exact marker while provider/Nginx unchanged. Bounded user-operated independent failure, not absolute firewall causality or VPN-absence proof. |
| MA superseding action decision, same update | Proposed closure NOT executed: no rule removal/deletion/change/submission or service stop. Firewall editor had only been opened and was cancelled. Continue existing approved restricted HTTPS/renewal after acceptance; actual timer activation and final SSH/root cleanup still pending. This supersedes earlier LTE NOT_RUN and closure plan, not historical WI003 cleanup. |
| MA allowed-control01:15:48 | Fresh trusted native curl200/exact marker after user LTE photo. Photo shows visible LTE/exact HTTPS marker URL/ERR_CONNECTION_ABORTED/no marker, not precise drop-layer or independently confirmed VPN absence. No image/path/body copied to tracked files. Previously proposed web closure NEVER applied; editor cancelled without changes. |
| MA scheduling01:16:14 | Known single Certbot unit/no extras; runtime masks removed. Nginx enabled/active, Certbot timer enabled/active/waiting, next2026-10-11 20:59:44 KST, LastTrigger empty. Certbot service inactive/static, Result success/0 is not actual prior scheduled execution. Package cron no-op on systemd; root has no Certbot cron. Status SCHEDULE_ENABLED_DRY_RUN_PASS. |
| MA final native01:16:47 | Fresh pinned SSH/sudo exit0, Nginx/sshd syntax PASS. Nginx/timer enabled/active, MySQL persistent mask/inactive, app absent; no Java/FFmpeg/CA processes, pending jobs, DB/app ports or IPv6 web listeners. Root tty1=0 and noVNC login prompt verified. Global IPv6 count0 belongs to earlier01:14 observation, not an invented new measurement. |
| MA final Windows01:16:52.899 | Exact window key removed; empty agent exit1 verified BEFORE hidden elevated cleanup exit0 and fresh CIM Stopped/Disabled. All MA execution sessions ended. This is new-window cleanup, independent of WI003. |
| MA final retained exposure and limits | Provider80 public ACME plus443/SSH approved management/32 remain, egress preserved. No app/DB public/start, extra billed product/purchase/install/reboot/commit/push; packages unchanged in this resume. NS last01:14:09 both AA NODATA, same serial. Browser preview blocked by browser client policy, not TLS trust failure; no browser-proof claim. No expiry alerts, first unattended actual run, changed future certificate or reboot proof. REQ remains partial. |

## Final Runtime Boundaries

Owner MA, with independent review owned by WI004 PG. Final receipts below
supersede interim pending/NOT_OPERATIONAL states without erasing the ledger.

| Gate | Current status |
|---|---|
| Review / preflight | WI004 staging review PASS; fresh preconditions and external HTTP10 PASS recorded. No reinstall/recreate/overwrite. |
| Staging CA | Registration01:08:21 and actual validation01:08:45 exit0 PASS; private account/key metadata verified by MA. No served staging certificate claim. |
| Production CA | Registration01:09:08 and issuance01:09:26 exit0; exact protected lineage, public validity and native trust/hostname/key-pair checks recorded. |
| Restricted TLS | Allowed-client/negative-path and loopback spoofed-header/SNI denial PASS. Later user LTE photo failure plus fresh allowed-line200 control recorded with causal/VPN limitations. NS unchanged; closure proposal not executed. |
| Renewal / scheduling | SCHEDULE_ENABLED_DRY_RUN_PASS: actual dry-run/hook/post-renewal served certificate PASS and timer enabled/active/waiting. LastTrigger empty; service success0 is not scheduled execution. No first unattended run, future changed certificate or actual reboot proof. Earlier NOT_OPERATIONAL is superseded. |
| Monitoring | No new alert implementation or delivery evidence; do not infer expiry notification from CA consent. |
| Cleanup / retained state | Fresh01:16:47 native and01:16:52.899 exact-key/empty-agent/Stopped-Disabled receipts PASS, root recovery logged out, all MA execution ended. Restricted Nginx and renewal timer intentionally remain enabled/active; app/DB stay off. No proposed web closure applied. |

## Evidence Pointers / Drift and Changes

| Priority / changed path | Patch and reason |
|---|---|
| HIGH: docs/index.md | Prepend latest limited-resume checkpoint; preserve the entire WI003 closure and earlier suffix. |
| HIGH: deliverables/user/REQ-20261010-ATS-010.md | Update current authorization, append WI004/WI005 and new-window criteria/progress; explicitly frame prior HOLD/closure as historical. |
| MEDIUM: deliverables/user/WI-20261011-ATS-005-summary.md | Final bounded TLS/renewal/cleanup summary with unresolved NS/alert/reboot/unattended-run limits. |
| MEDIUM: deliverables/agent/WI-20261011-ATS-005-evidence-pack.md | This skill-formatted approval/receipt ledger. |

No docs-category file added, so category counts remain unchanged. No workboard,
policy, historical Evidence Pack or memory edit; no repository-wide cleanup.
Reuse: existing runbook, prior ledger structure and scoped validator, no new framework.
The previous DoD statement about lack of exception approval is explicitly grouped
under WI001-003 historical criteria; new WI004/005 criteria remain separate.

## Commands and Test Evidence

Read-only `git status --short --branch -- <six scoped paths>`, `git rev-parse HEAD`,
focused `Get-Content`/`rg`, public `Get-FileHash`, `Get-Date` executed successfully.
Index and REQ baselines were retained in session memory before editing. Manual
edits use apply_patch only. Initial01:01 KST scoped validation passed: four files,
Tier0 presence, internal file links and52 supported IDs,0 errors/0 warnings.
The existing validator was loaded via runpy with Path.rglob scoped in memory
separately for internal links and traceability, as in WI003; no validator change.
Scoped git diff --check exited0 with only the existing index CRLF normalization
warning. Direct checks passed seven metadata fields, dependency targets,
whitespace/final newline and ASCII for both new files, including untracked files.
In-memory comparisons confirmed the entire prior index suffix from the WI003
heading and the complete prior REQ PROGRESS text preserved after newline
normalization. These initial checks do not complete runtime integration; final
checks must follow MA receipts.
No app/native test, measured coverage, network probe or external-link validation
by DocOps. Any inherited test result must remain attributed to its owner/window.

Public input identities captured00:59 KST, not native transfer receipts:

| Input | SHA-256 |
|---|---|
| WI003 Evidence Pack | `8e85a6cad7e9101b853bc7c87343f21da59d0b2c7eaa38e3965d8c70eaa61469` |
| WI003 summary | `a1b303ab753b3ac5d49893e67540e846a1ace2da477a6befc7dd5c9343d15fd5` |
| WI004 handoff | `293d560e4323ae055ca1cf56c4210db645b564cc18b77e5d142db3b17315e220` |
| WI005 handoff | `6a10a3a55d02fd749dd0a1f993bd9b4c7542d23ff104b97be2b354072b3c1a71` |

### Whole-Repository Validator Limitation

MA reported that invoking the validator with `--help` actually ran the whole
repository because the script ignores arguments. That run FAILED on one existing
parser false positive at `deploy/single-vps/runbooks/first-host-setup.md:175`:
The PowerShell integer cast followed by a parenthesized Read-Host expression
was interpreted as a Markdown link. DocOps confirmed the source syntax; source is
unchanged and no unrelated fix is authorized. This is a documentation-parser
limitation, not a deployment fault or a whole-repository PASS. MA separately
reported scoped validation of WI004/WI005 handoffs passed0 errors/warnings.
DocOps's four-file checks are separate and do not certify repository-wide links.

### Final Scoped Checks

At01:21 KST, final four-file validation passed Tier0 presence, internal file
links and52 supported traceability IDs,0 errors/0 warnings. Scoped git diff
--check exited0; only the existing index CRLF normalization warning remains.
Direct metadata/dependency/whitespace/newline checks passed for all four files,
including untracked deliverables; both new deliverables are ASCII.
The first final validation reproduced the same parser false positive in this
pack's inline quotation. Rephrasing that quotation fixed the scoped check;
the original runbook and validator remain untouched.

Reproduction: load `.agents/skills/validate-docs/scripts/validate_docs.py` with
Python runpy, instantiate DocValidator at the repository root, run
validate_tier0_docs, then patch Path.rglob separately for validate_internal_links
and validate_traceability_ids to return only the four paths in Evidence Pointers.
Do not use --help or claim this limited traversal validates the entire repository.

```powershell
git diff --check -- docs/index.md deliverables/user/REQ-20261010-ATS-010.md deliverables/user/WI-20261011-ATS-005-summary.md deliverables/agent/WI-20261011-ATS-005-evidence-pack.md
```

Newline-normalized comparisons confirm the entire index suffix from the WI003
heading and the original REQ PROGRESS text are preserved. WI003 evidence/summary
and WI004/WI005 handoff hashes still match the input table. Seven public source
hashes matched the WI004 table at01:15:45; no fresh application/native tests by
DocOps. Final output hashes are returned separately to avoid a self-hash cycle.

## Risks / Rollback / Evaluation

Unresolved child apex NS may affect behavior, but the internal provider cause is
not proven. A successful certificate is not DNS repair or app/DB release. User
approval is bounded to restricted testing; extra costs or user tasks must be
disclosed beforehand. Ordinary contract-governed traffic is not guaranteed free.

Rollback: focused correction of only these four documentation files, preserving
all old text/evidence and unrelated changes. No operational rollback, deletion,
reinstallation, remote service change or new authorization by DocOps. Evaluation:
record approval immediately and maintain separate gates for observed outcomes.

## Follow-ups / WI Chain

WI005 Blocks:-. Initial approval and final WI004/MA receipts are integrated;
scoped checks close reporting only. REQ010 stays partial because the NS
anomaly remains. Monitoring, first unattended renewal and actual reboot remain
explicit unverified limits, not automatic follow-up actions. No descendant,
automatic REQ closure or reuse of WI003 cleanup as this window's cleanup.

## Related Documents

- [Handoff](WI-20261011-ATS-005-handoff.md): Scope and explicit continuation.
- [WI004 assignment](WI-20261011-ATS-004-handoff.md): Independent review.
- [WI004 verdict](WI-20261011-ATS-004-evidence-pack.md): Exact staging procedure PASS, not runtime acceptance.
- [WI003 closure](WI-20261011-ATS-003-evidence-pack.md): Preserved prior window.
- [REQ010](../user/REQ-20261010-ATS-010.md): Current approval and plan.
- [Summary](../user/WI-20261011-ATS-005-summary.md): Bounded final result.
- [Current entry](../../docs/index.md#current-decision-entry): Latest checkpoint.
- [Domain/TLS runbook](../../deploy/single-vps/runbooks/domain-tls.md): Existing MA-only procedure.
