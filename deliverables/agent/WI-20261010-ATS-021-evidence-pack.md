---
version: 1.0
last_updated: 2026-10-10
project: ATS
owner: docops
category: evidence-pack
status: stable
dependencies:
  - path: WI-20261010-ATS-021-handoff.md
    reason: Existing skill-generated scope and final receipt gate
  - path: ../user/REQ-20261010-ATS-007.md
    reason: Approved continuation and progress
  - path: WI-20261010-ATS-019-evidence-pack.md
    reason: SE correction and source identity
  - path: WI-20261010-ATS-020-evidence-pack.md
    reason: Independent local review with native gates
---

# Evidence Pack: WI-20261010-ATS-021

> TL;DR: WI021 complete: MA's20:39KST final runtime/cleanup receipt integrated; REQ007 active.

## Scope / DoD Check

- [x] Existing WI021 handoff and approved REQ007 continuation read.
- [x] Link WI019 correction, WI020 local review and WI021 MA attribution separately.
- [x] Preserve prior receipts and reviewed source identities.
- [x] Keep domain, deployment, recovery and capacity limits explicit.
- [x] Receive and integrate final per-phase MA runtime and cleanup receipt.
- [x] Complete scoped validation and final preservation recheck.
- [x] Finalize two deliverables; retain REQ007 active for remaining deployment.

## Reference Documents (Tier 0-2)

Full Tier0, DocOps role/security policy and create-wi-evidence-pack skill were
injected. The on-disk skill-generated handoff was read before this direct write.
Assignee docops; task documentation; required Tier0 plus supplied Tier1 policies.
Rule source `.claude/config/context-injection-rules.json`; no configuration audit.
ATS is the existing workspace tag. No descendant delegation.

| Tier | Handoff pointer / use |
|---|---|
| 0 | docs/standards/core-principles.md (STD-001), development-standards.md (STD-002), documentation-standards.md (STD-004), glossary.md (STD-005): injected authority |
| 1 | .claude/agents/docops.md; docs/policies/security-policy.md: injected scope and sensitive-data rules |
| 1 | docs/policies/access-control-policy.md; quality-gates.md: read approval, least privilege and bounded verification |
| 2 | .agents/skills/create-wi-evidence-pack/SKILL.md: injected direct-write procedure |
| 2 | docs/standards/evidence-pack-standard.md: read traceability/reproduction; current AGENTS workboard rule governs progress |
| 2 | docs/index.md#current-decision-entry: read latest and historical checkpoints |
| 2 | deploy/single-vps/CONFIGURATION.md: scoped source/ownership section read; source remains unchanged |
| 2 | deploy/single-vps/runbooks/first-host-setup.md: prior WI018 read, current preserved hash verified |
| 2 | deploy/single-vps/runbooks/host-hardening.md: current corrected runbook read |
| 2 | scripts/deployment/host-hardening/: three candidate files read; checker/test contracts read through WI019/020, all six hashes checked, no test execution claimed |
| 2 | deploy/single-vps/mysql/atstudio.cnf; journald/atstudio.conf: exact templates read and hashed |
| Context | REQ007, WI018 and WI019 Evidence Packs: current approval, prior checkpoint and appended correction |
| Additional | WI020 Evidence Pack; .agents/skills/validate-docs/SKILL.md: independent final review and scoped documentation validation |

## Changed Paths and Drift Report

Only four paths are owned:

- `docs/index.md`: new Host-Hardening Continuation above preserved first-host checkpoint.
- `deliverables/user/REQ-20261010-ATS-007.md`: dated continuation, no closure.
- `deliverables/user/WI-20261010-ATS-021-summary.md`: concise status and limits.
- This Evidence Pack: attributable phase receipts and reproducible local checks.

| Drift / impact | Scoped response |
|---|---|
| Old runbook recovery HOLD and original journal filename could be mistaken for current state | Link WI019 correction and WI020 reviewed hashes; preserve historical bodies |
| WI018's permissive SSH baseline and completed cleanup precede this new window | New dated checkpoint; do not reuse old cleanup as new evidence |
| Local tests could be mistaken for native application | Separate owners/environments and final per-phase receipt gate |
| CONFIGURATION retains planned journal basename and NOT RUN settings | Current entry points to corrected staged runbook/MA receipt; do not rewrite hashed source within this WI |

## MA Phase Receipts

Date2026-10-10; environment approved host and Windows operator window; owner MA.
DOCOPS has no remote access and does not independently verify host observations.
Exact addresses, fingerprints, public keys, account/contact data and raw logs
are excluded from this document.

### Phase One: Supplied Actual Receipt

- Six reviewed candidates and one approved public key staged with local/remote
  SHA256 match. Only named prior public configuration files copied to protected
  baseline. No private-key or secret transfer claimed.
- New administrator: password locked L; home/SSH directory0700, authorized key0600,
  sudoers root0440. Staged and full visudo PASS. Fresh non-multiplexed key login,
  PTY and sudo-n UID0 PASS before SSH changes. Authenticated root console from
  user-assisted20:23KST recovery was retained.
- Staged sshd syntax and three actual administrator/root/existing-user connection
  contexts showed only eight approved differences. All installed contexts equaled
  staged results; original configuration unchanged. Reload PASS, service/socket active.
- Fresh post-reload administrator PTY/sudo PASS; known-valid root key denied;
  unauthenticated server offer publickey-only PASS; root local password P preserved.
- Nonlogin service identity with locked password L and dedicated group plus14
  directories created. Native Linux before/after checker and cross-identity
  access checks PASS. These are MA checks, not synthetic SE/PG tests.
- Nginx/MySQL remained off at this phase. MySQL/journal application and final
  cleanup have not yet been supplied; no result is inferred from the runbook.

### Phase Two: Corrected Journal Receipt and MySQL HOLD

MA reported exact-source SHA installation of corrected `zz-atstudio.conf`,
four-directory inventory with only the vendor syslog.conf and absent local
destination before application. Merged nine expected settings passed, including
ForwardToSyslog=no; other forwarding defaults were unchanged. Merged-input digest:
`2f93e2beb7473843adf1a0acdc08b975d04b15e276435b1a9973744d248748fb`.
Service was active after restart.

The initial phase-two message prematurely claimed no-warning and synthetic
receipt PASS and usage8MiB. **MA explicitly withdrew those claims:** the command
had exited1 after active state, before reaching the synthetic step. Only install,
merged-policy PASS and active state were proved then. This false start is retained
as superseded evidence, not silently converted to an original PASS.

The subsequent actual trace `chunk35ab43`, exit0, supersedes that message.
MA identified journalctl's no-match exit1 with zero stdout/stderr as the bounded
check failure, not daemon failure. The corrected read-only warning check accepts
rc0/1 only with empty stdout/stderr and produced JOURNAL_NO_CONFIG_WARNING_PASS.
MA then actually ran logger, sync and exact-message query: synthetic local
/dev/log-to-journal receipt PASS, journal usage **16.0M**, logrotate.timer active.
No log deletion, vacuum or forced rotation. Journal is installed/restarted and
accepted within these checks; seven-day aging, all-copy isolation, actual Nginx
logging and load remain unproved.

MySQL standalone candidate validation failed because AppArmor enforced a denial
for the `/root/wi019` candidate; MA reports a scoped audit confirmation even with
root fsuid0. No MySQL candidate configuration was installed and no server started
in this phase. SE/PG are correcting only the staging path to
`/etc/mysql/atstudio-validation.cnf`, outside the include directories. Native
revalidation remains HOLD; no AppArmor relaxation or policy change is inferred.
Reviewed source/hashes may change through that coordinated correction; the
initial preservation table below remains historical and must not be overwritten.

### MySQL Follow-up: Startup Verified, Query Not Executed

MA's later receipt supersedes the earlier unperformed validation/start stage,
not its AppArmor failure history. Isolated `/etc/mysql/atstudio-validation.cnf`
had exact source bytes and root0644. Standalone validation as mysql, actual
include-chain validation and default-service validation all returned0. Bounded
unmask/start passed: only loopback3306 plus Unix socket, no X listener;
service MemoryCurrent439271424 bytes and33 tasks. These are bounded process
observations, not workload capacity or live SQL-value acceptance.

The native MySQL8.0.46 client rejected `--no-login-paths` with rc2; the query
**did not execute**. MA immediately confirmed stop/remask, no queued job and
no DB listeners. Metadata checks found root .mylogin.cnf/.my.cnf absent and
zero MYSQL environment variable names; no credential contents were read.
PG/SE are reviewing a sanitized-environment/no-defaults compatibility correction.
Live expected values remain unverified until an actual accepted query receipt.

### MySQL Accepted Retry: Actual Live Values

MA actual trace `e741a6`, rc0: the compatibility correction received independent
PG local review; metadata absence checks and the explicit password/default-path
environment removals preceded the second bounded socket-root query. This does
not claim the entire inherited environment was sanitized. The unchanged
allowlisted read-only query returned the expected values:

| Setting | Actual value |
|---|---|
| bind/port | loopback IPv4 /3306 |
| innodb_buffer_pool_size | 536870912 bytes |
| max_connections | 30 |
| local_infile/general_log/slow_query_log | 0/0/0 |
| time_zone | +09:00 |
| Active mysqlx plugin count | 0 |

Stop/dead/no listeners/no queued job/remask all PASS; Nginx unchanged. No app
DB/schema/users/secrets/mail/payment action. The unchanged-source validation
copy remains root0644 outside include directories. This accepted query supersedes
the preceding pending-values state without erasing the AppArmor and client-option
failures. Final host checks and operator cleanup still await MA's next receipt.

### Remaining OS Update Gate

MA read `apt list --upgradable` using the existing cache, without refreshing or
upgrading. It listed182 upgradable packages from mixed noble-updates and
noble-security sources. Reported candidates include:

| Component | Installed to cached candidate |
|---|---|
| openssh-server/client | 9.6p1-3ubuntu13.16 to9.6p1-3ubuntu13.19 |
| libssl3t64 | 3.0.13-0ubuntu3.11 to3.0.13-0ubuntu3.16 |
| kernel generic | 6.8.0-124 to6.8.0-146 |
| sudo/systemd/libc | Listed as upgradable; exact versions not supplied |

This is cached package evidence, not current external advisory research, a
classification of all182 as security fixes, or proof of a new CVE. **Before
public invitation, require separately approved OS security-update scope,
reboot and post-update access/recovery/configuration/service revalidation.**
MA informed the user. No OS upgrade/reboot was executed in this continuation;
no reboot marker was present at the observation. The earlier two dependency
upgrades under WI018's package installation remain separate historical evidence.

### Final Runtime and Cleanup Receipt: 20:39 KST

The final MA receipt supersedes all earlier pending-phase/query/cleanup statements
in this pack and the dated progress entries. Those statements and failed attempts
remain historical evidence. No new remote action or independent DOCOPS runtime
verification was performed.

MA trace `d27089`, rc0,2026-10-10 at20:39:14KST:

- sshd syntax, full visudo and native layout-after checks PASS. Effective root
  SSH/password/keyboard-interactive no; publickey yes; AuthenticationMethods
  publickey; forwarding disabled; PTY yes.
- MySQL/Nginx each exact /dev/null masked, inactive/dead. Only SSH22 IPv4/IPv6
  and loopbackDNS53 listeners; no DB/web listeners, failed units or reboot marker.
- Final RAM total8327675904, available7879938048 bytes; rootfs total102888095744,
  used4555730944, free98315587584 bytes; swap0; Asia/Seoul and NTP synchronized.
  These measurements do not establish future capacity.
- All five applied configuration comparisons matched the reviewed bytes below;
  the isolated validation copy remains root0644 outside MySQL include directories.

| Applied configuration / MA comparison | Exact reviewed SHA-256 |
|---|---|
| MySQL isolated validation and installed drop-in (two comparisons) | 008136130093048F395C4F414A937541AD314615E656AF7A64A170E4F376A233 |
| SSH policy | 3E629E4C3E5EAF42C89C9A2FC51F23C691FC824105409041F0F4BA08013ECB03 |
| Sudo policy | BF1F13292906B556F3589074924F672BF2B453E21A68C586A5B10661EAAF2B94 |
| Journal policy | 229473C1D88E2CF30DAC6E9F4ED34327A39A66A74653DC72465E42239B2FC963 |

Both owned retained root/administrator SSH sessions exited0. MA cleanup trace
`a0d292`,20:39:34KST, confirmed exact owned-key removal, ssh-add no identities,
completed elevated cleanup and final Get-Service **Stopped / Disabled**.
The noVNC root console was exited through its UI; MA's fresh screenshot showed
the Ubuntu tty1 login prompt. Screenshot remains in Temp, not this repository;
no token or screenshot contents are reproduced. This is fresh closure for this
window, not WI018's prior cleanup receipt.

No extra purchases, firewall/provider changes, OS upgrade/reboot, app DB/schema/
users/secrets/start, mail/payment or domain operations occurred in this continuation.
The bounded MySQL runtime query is the explicitly approved system-value probe,
not app SQL initialization. Journal actual receipt usage remains16.0M; the
premature8MiB statement is withdrawn. Normal future journal aging and all-copy
isolation were not observed. Remaining deployment and OS-update gates stand.

## Source Identity and Preservation

DOCOPS observed local main HEAD58b1011dad796017e47dbcc82a4225ca449bcdf6 with
pre-existing dirty/untracked work. HEAD is not deployed artifact identity.
All12 hash entries in WI020's reviewed table matched before editing; the
following17-path baseline also includes prior receipts, this handoff and the
out-of-scope deployment README. No preserved file is modified by DOCOPS.

| Preserved path | SHA-256 at WI021 start |
|---|---|
| deploy/single-vps/runbooks/host-hardening.md | B3B4E7BE77D17FC1042AEC11B575920EAF52A4F2EAFBC99D9B95E17BB0D2D636 |
| scripts/deployment/host-hardening/00-atstudio-admin.conf | 3E629E4C3E5EAF42C89C9A2FC51F23C691FC824105409041F0F4BA08013ECB03 |
| scripts/deployment/host-hardening/90-atstudio-admin | BF1F13292906B556F3589074924F672BF2B453E21A68C586A5B10661EAAF2B94 |
| scripts/deployment/host-hardening/sshd-validation.conf | 3C650342D8325D170EF249B96EBA8BA6A0CAB9D140DEAB80334726E2D1EA6CDF |
| scripts/deployment/host-hardening/check_layout.py | CE755FDBDC88D2E1B8CCE83834CF167568239C6FA2273EB56320277440AFAD81 |
| scripts/deployment/host-hardening/test_check_layout.py | 6F49C21E13A781E69FBC6A7E5DD2E2D1494C8A78E8CFB09B8EA4DD5AF5DDEF33 |
| scripts/deployment/host-hardening/test_candidates.py | A3F648A4350276940BAFCD2ADE7F82FECC58433ABDFF38F2191B66F31CACF5CE |
| deploy/single-vps/mysql/atstudio.cnf | 008136130093048F395C4F414A937541AD314615E656AF7A64A170E4F376A233 |
| deploy/single-vps/journald/atstudio.conf | 229473C1D88E2CF30DAC6E9F4ED34327A39A66A74653DC72465E42239B2FC963 |
| deploy/single-vps/CONFIGURATION.md | BD1A8E2FF426F7660AA063D890FBE446E7D60A8C323EC49CF6DDF9184B9C4664 |
| deploy/single-vps/runbooks/first-host-setup.md | 1C805ABFBC64E18662E0822DCBC6C2E4C5EFD0493D59ECA7334E0FF00AB88D33 |
| deliverables/agent/WI-20261010-ATS-020-handoff.md | E09DDC97B242B3DF30DE1C68227D973B76E5A42B593863364DC30F0CD9F8E556 |
| deliverables/agent/WI-20261010-ATS-018-evidence-pack.md | 1368ED2E7CBE1711CDC5B42B5404188869CC2BA38894EB57AB21AACCEDEB8B83 |
| deliverables/agent/WI-20261010-ATS-019-evidence-pack.md | 54493802E92C903C9BFF904EA084F9E31672DAB7AB11870A1AC69FD6F3BE2DFF |
| deliverables/agent/WI-20261010-ATS-020-evidence-pack.md | 41C294CD17B1AF70FFF738EE9E510B868E1C7D99C17183DADB445E1D689BE093 |
| deliverables/agent/WI-20261010-ATS-021-handoff.md | 842F20C116E57A80A053425579284948C352D3933F913820A3DFD10F858520C7 |
| deploy/single-vps/README.md | FA61341A2233B3166A4ED1120F41A0FB5518C04B54CF6B128F8740323DEBCB85 |

### Final Source Reconciliation

The17-path initial table is preserved as the start snapshot. Final comparison
found12 unchanged and five coordinated changes below, all outside DOCOPS edits:
SE corrected the runbook/client test and appended WI019 evidence; PG appended
WI020 corrections and final source reconciliation; MA repaired the duplicate
handoff heading and added WI020 dependency/input without changing ownership.
Current handoff and PG final record were reread. No unexplained drift remained.

| Coordinated changed input | Final locally measured SHA-256 |
|---|---|
| deploy/single-vps/runbooks/host-hardening.md | 43755E6F58EF9543EB6DFDCE0F81C7DAF816D93C4FACA1B4125C2BE3B80F1E1A |
| scripts/deployment/host-hardening/test_candidates.py | BD24759B6F75BD463D43DF36E0A0388A4CEE6C1541EDCF38486D72B138F42915 |
| deliverables/agent/WI-20261010-ATS-019-evidence-pack.md | BEF33AB2BB846EBBDA1D6DC939502678441C07C7AE02D9ED596D1DF2D2F4E92B |
| deliverables/agent/WI-20261010-ATS-020-evidence-pack.md | D7CAD931F7D02E958919EA04EFC9C5B2615ECA290F6DF0DFCECB772A6D3EB0B4 |
| deliverables/agent/WI-20261010-ATS-021-handoff.md | AC893C0B180CA514CCF76BB4AA66D92E24AF39317123F7147CEC1991E9666023 |

The first two match PG's final reconciliation PASS and SE's compatibility
addendum. Earlier registered hashes remain historical, not current identities.
Installed candidate template bytes did not change. PG local source reconciliation
does not independently verify MA's actual SQL, service or cleanup receipts.

## Commands and Tests

Owner DOCOPS; local Windows/PowerShell; date2026-10-10. `Get-Content`, `rg`,
`git status --short --branch -- <scoped paths>`, `git rev-parse HEAD` and
`Get-FileHash -Algorithm SHA256` establish current inputs. Edits use apply_patch.
Preparation validation passed: four owned documents,120 local file links,9
dependencies,78 supported-format REQ/WI occurrences, seven required metadata
fields per document, four Tier0 paths, checkpoint anchor and trailing whitespace.
Scoped `git diff --check` exited0 with only a CRLF-to-LF advisory; untracked files
were independently whitespace-checked. All17 baseline hashes matched at that
pre-correction checkpoint. Final check after all runtime/cleanup integration:
four documents,121 local file links,9 dependencies,82 supported-format REQ/WI
occurrences, required metadata/Tier0, checkpoint anchor and whitespace all PASS.
Scoped `git diff --check` exited0;12 inputs unchanged and five authorized
SE/PG/MA changes reconciled to the final table above. No unrelated path edited.

Reproduction: on the four owned paths, strip fenced code before Markdown-link
matching; resolve local link/dependency targets relative to each file; verify
seven frontmatter fields, Tier0 existence, REQ/WI supported-format matches and
trailing whitespace. Confirm the new checkpoint heading and compare Get-FileHash
against the two tables, using final values only for the five reconciled paths.
This checks local file existence, not external URLs or all historical anchors.
Full repository validator, application tests and remote commands were not run
by this documentation WI. No new security-advisory/provider research was done.

## Risks / Rollback

Approved administrator sudo is root-equivalent; forwarding restrictions do not
confine a shell administrator. Local review, setup or narrow probes establish
no full security, capacity, recovery, billing-cap or production guarantee.
Correct only owned document additions through a scoped patch; preserve other
dirty work, prior receipts and all registered source hashes. Do not execute a
remote rollback or imply documentation edits change target state.

## Follow-ups

WI021 is complete; MA reviews these documents next. Final PG source-hash
reconciliation is PASS, not a new remote/security audit. Blocks is empty;
no successor or descendant is created. Keep REQ007 active for domain purchase
before DNS/HTTPS, application DB/secrets/start, backup/restore, alerts and load.
Approved OS security updates, reboot and revalidation precede public invitation.
No purchases, provider actions, commits, memory edits or remote access by DOCOPS.

## Related Documents

- [Handoff](WI-20261010-ATS-021-handoff.md)
- [REQ007](../user/REQ-20261010-ATS-007.md)
- [Summary](../user/WI-20261010-ATS-021-summary.md)
- [WI019 correction](WI-20261010-ATS-019-evidence-pack.md#2026-10-10-correction-and-recovery-addendum)
- [WI020 review](WI-20261010-ATS-020-evidence-pack.md)
- [Current entry](../../docs/index.md#2026-10-10-host-hardening-continuation)
