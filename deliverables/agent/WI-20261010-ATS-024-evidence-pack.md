---
version: 1.0
last_updated: 2026-10-10
project: ATS
owner: docops
category: evidence-pack
status: stable
dependencies:
  - path: WI-20261010-ATS-024-handoff.md
    reason: Approved scope and receipt gate
  - path: WI-20261010-ATS-022-evidence-pack.md
    reason: SE local procedure preparation
  - path: WI-20261010-ATS-023-evidence-pack.md
    reason: PG independent local review
---

# Evidence Pack: WI-20261010-ATS-024

> Purpose: Integrate MA's bounded maintenance receipts without converting local
> procedure review or older cleanup into current runtime evidence.

## Summary

TL;DR: WI024 complete from MA's final21:23:32KST runtime/cleanup receipts.
All earlier pending ledger states are historical and superseded by closure below.
Documentation owner DOCOPS; actual host/Windows operator evidence owner MA;
local preparation SE and independent procedure review PG. Date2026-10-10,
approved existing Ubuntu24.04 host and local Windows operator environment.
No remote operation was performed by DOCOPS.

## Scope / DoD Check

- [x] Handoff exists; four owned documents only, historical evidence preserved.
- [x] WI022 preparation, WI023 local review and MA live progress separated.
- [x] REQ active; domain purchase before DNS/HTTPS and app/backup/alert/load tracked.
- [x] Actual installation/reboot/postboot receipts integrated.
- [x] This window's exact-key/agent/session/console cleanup integrated.
- [x] Final scoped documentation validation and preservation recheck.

## Reference Documents (Tier 0-2)

The handoff INPUT POINTERS supplied this ordered context. Tier0 was injected;
current skill instructions and scoped evidence were read locally.

| Tier | Document | Reason |
|---|---|---|
| 0 | docs/standards/core-principles.md (STD-001) | Approval and evidence boundaries |
| 0 | docs/standards/documentation-standards.md (STD-004) | Metadata, language, history |
| 0 | docs/standards/development-standards.md (STD-002) | Traceability and verification |
| 0 | docs/standards/glossary.md (STD-005) | Canonical terms |
| 1 | .claude/agents/docops.md | Documentation ownership |
| 1 | docs/policies/security-policy.md | Sensitive-output exclusion |
| 2 | .agents/skills/create-wi-evidence-pack/SKILL.md | Handoff gate and two sets |
| 2 | .agents/skills/validate-docs/SKILL.md | Scoped integrity checks |
| 2 | docs/index.md; deploy/single-vps/runbooks/os-maintenance.md | Current entry and reviewed procedure |

Injection source: WI024 handoff, assignee docops, task type documentation;
context includes REQ007 and WI021/022/023 Evidence Packs. No descendant delegation.

## Evidence Pointers

- [Current entry](../../docs/index.md#2026-10-10-os-maintenance-window): Additive latest checkpoint.
- [REQ007](../user/REQ-20261010-ATS-007.md): Append-only MA progress; active status preserved.
- [User summary](../user/WI-20261010-ATS-024-summary.md): Brief pending status.
- [WI022](WI-20261010-ATS-022-evidence-pack.md): Local runbook preparation.
- [WI023](WI-20261010-ATS-023-evidence-pack.md): Independent local review and command-local rate-limit review.
- [Runbook](../../deploy/single-vps/runbooks/os-maintenance.md): Native stop gates; not live proof.
- [WI021](WI-20261010-ATS-021-evidence-pack.md): Historical host-hardening/cleanup; not this window's cleanup.

## MA Receipt Ledger

These are supplied MA observations, not DOCOPS reruns or imported raw logs.

| Phase / receipt | Actual result | Boundary |
|---|---|---|
| 20:59-21:02 KST access | Prior pin unchanged; user retry completed key readiness; user root console authentication/tty1 and fresh pinned nonmultiplex administrator PTY/sudo PASS | No passphrase/key bytes recorded; retained routes still open |
| New-window preflight | Policy hashes/layout/effective SSH/journal PASS; MySQL/Nginx exact masks inactive/dead, SSH22 and loopbackDNS53 only; dpkg audit empty, APT locks idle, no scheduled reboot | No package mutation at this initial observation |
| c83c7c, rc0 | Trusted ancestry and16 public configuration copies mode/hash/cmp PASS; package/boot/source/timer baselines retained privately; two APT timers paused with enablement preserved | Copies are not whole-OS rollback or database backup |
| Official refresh/check | `apt-get --error-on=any update` and `apt-get check` rc0 | Index mutation, not package installation |
| d3995f, rc0 | Simulation180 upgrades/25 new/0 removals/2 phased; native python-apt trusted origin/version/architecture checks for all205 selected PASS; Noble/updates/security only, no downgrade; mysql/nginx/grub/shim not selected | Accepted source/plan evidence, not installation |
| Preview / subsequent probe | Expected refusal rc1 reached Y/n, N and Abort; later combined probe exit1 came from absent optional initramfs directory | Do not label preview or package transaction failed because of the later probe |
| Download-only completed, rc0 | Interactive identical plan; displayed975MB fetched227s at4305kB/s | Supersedes download pending only; no unpack/install, wire-rate cap or provider quota proof |
| Candidate hook review | netplan/systemd/sudo postinst identical; AppArmor comments/version only; OpenSSH adds ssh.socket unmask in existing activation branch, already enabled/unmasked here; kernel146 image/module hooks inspected | Review before install, not post-upgrade behavior proof |
| Upgrade chunk4ed5cd, exit0 | 180 upgraded/25 new/0 removed/2 phased | Installation observed; reboot NOT done |
| 21:15:49KST preboot checks | Fresh pinned administrator/sudo PASS; audit empty/check0;16 config hashes unchanged; sshd/visudo PASS; MySQL/Nginx masked/inactive; no failed units; only SSH/loopbackDNS/DHCP observed | One transient new-SSH refusal recovered next probe; retained PTY unaffected |
| Actual boot hooks | Plymouth rebuilt all three old initrds plus new146; old kernels retained | Supersedes latest-only expectation for the complete transaction; peak not continuously measured |
| AppArmor warning | Postinst printed fixed category `Illegal number: yes`; transaction exit0, enforcement present | Bounded investigation pending before reboot; not silently dismissed |
| c036f2/8df2d2/b7255a/c47d8c follow-up | 180 changed/25 added/0 removed; all205 exact versions installed,16 hashes retained; actual three-user SSH contexts root/password/kbd denied, publickey-only/forwarding denied; layout14 PASS; MySQL native validator0, masked, no SQL/start; rootP and other checked accountsL; nine journal values and KST/NTP retained | Preboot evidence only; fresh admin PTY/sudo PASS and root console retained |
| AppArmor investigation superseding pending diagnosis | Sourced rc.functions check_userns numerically compares kernel feature literal `yes` with -eq0; restriction sysctl1, feature=yes, profiles loaded, enforcementY. Missing nautilus conffile matches explicit postinst rm_conffile(... .6~); nautilus absent | Vendor unchanged; warning retained, not complete security acceptance; reboot awaits PG comment |
| STOP D / reboot issued | MA reports idle jobs/locks, no scheduled shutdown,146 boot assets/GRUB entry and retained124, fresh admin PTY/sudo0 and current root tty1 recovery; all policy checks PASS. SSH ucf-dist: nine socket-activation comment lines and commented default password option reviewed, not merged; local00 policy wins. Exactly one `sudo systemctl reboot` issued in retained PTY | Disconnect/command issuance is not changed-boot-ID or successful reboot evidence |
| Reboot21:20:34 / e44bd2 | One approved reboot; retained SSH10813 exit1/reset expected; fresh strict SSH confirms changed boot ID, kernel6.8.0-146 and sudo0 | Successful new-boot evidence, not disconnect alone |
| Postboot3a3164 at21:21:14 | Ubuntu24.04/noble now24.04.5 point release; audit empty/check0; no failed units/jobs; MySQL/Nginx exact masks/inactive; SSHsocket active/enabled, service active with unit disabled unchanged; journal/logrotate active; APT timers both active/enabled restored by boot, services idle/Periodic0 unchanged | No release upgrade, app/env/release creation, MySQL start or SQL |
| Postboot policy/package checks |16 config and two source hashes retained; sshd/visudo/layout PASS; native MySQL validator0; rootP, other checked accountsL;205 exact installed versions and three actual SSH contexts/17 controls confirmed; nine journal values retained | 6ea45e's later classifier returned1; do not report that whole combined command as exit0 |
| Authentication/time | Root actual-key denial255; admin none-auth offer publickey-only; fresh admin PTY/sudo9b8e56 PASS; user postboot root console authentication corroborated by MA screenshot and one tty1 in who; initial NTP no becomes yes in c8203b/c417d5, Asia/Seoul | Root console and remaining connections not cleaned yet |
| Residual plan |0 upgrades/0 new/0 removals/2 phased: libopeniscsiusr and open-iscsi | No forced phased updates or all-vulnerabilities-remediated claim |
| AppArmor postboot | EnforcementY, MySQL enforce,24 enforce/4 complain/89 unconfined; feature yes/restrict1; recurring numeric warning with Result success; PG reports no blocking issue | Vendor not patched; not all profiles enforce |
| Existing PAM warning /500dc7 | Missing OPTIONAL session module pam_lastlog.so: two this boot,12 prior; dpkg-V login unchanged, root login works. Earlier6ea45e classifier exit1 failed to match the PAM warning; corrected bounded inspection confirmed prior occurrence | Retained warning, not a newly proven update regression; not patched |
| Cleanup | PENDING: Windows agent Running/Manual, one owned key; SSH/console retained | WI021 cleanup cannot establish this window's closure |

### Final Closure: 21:23:32 KST

MA0b7968: native who shows zero root tty1 after CUA exit; fresh private Temp
screenshot shows login prompt, with no token recorded. Both APT timers remain
active/enabled, NTPyes, no failed units. All owned SSH sessions ended, including
10813 at reboot and completed probes. Windows68f020: exact owned-key removal0,
agent-list rc1/no identities verified before stopping, hidden elevated helper0,
final Get-Service Stopped/Disabled. This closes the new window's cleanup, not
by borrowing WI021 evidence. No extra purchase, port expansion, paid product,
DB or application creation occurred. WI024 complete; REQ007 active.

No functional regression was detected in the bounded checks. The OPTIONAL
pam_lastlog.so missing-module warning is pre-existing; AppArmor's vendor
boolean/numeric warning still exists and is unpatched. Postboot individually
asserted MySQL enforce and aggregate modes24/4/89 only; lsb_release was checked
preboot, not separately reasserted postboot. No universal profile enforcement,
all-vulnerabilities-fixed, continuous peak-space or full production PASS claim.

### Measured Resources and Plan Quantities

Initial MA free space: root approximately98.3GB, separate boot635641856 bytes,
EFI102995968 bytes. Preview archive975471520 bytes and net installed321550336
bytes are package-plan totals, not measured peak space or workload capacity.
The historical182 cached upgradable count is superseded for plan selection by
the refreshed180+25 plan; neither count means all packages are security updates.
No CVE closure, sustained capacity, billing cap or production acceptance claimed.

MA's subsequent native hook review found latest-only firmware/initramfs rebuild,
same-filesystem hardlink backup with backup_initramfs=no, current largest
initrd76515389 bytes and direct boot payload24491507 bytes. Provisional peak
budget: two120MiB images (current/new plus .new),64MiB old-image growth allowance
and direct payload, approximately327.4MiB, rounded to330MiB work plus256MiB
untouched reserve =614465536 bytes versus635641856 bytes free. This is MA's
conservative sizing assumption, not measured future peak; monitor during install
and record final actual space. MA projects root space above90GB after cached975MB
and approximately322MB added installation. Fresh plan/baseline/space/access
recheck was required before installation. The subsequent actual upgrade receipt
above supersedes the installation-pending state.

Actual preboot snapshot21:15:49KST: root free96933793792, boot free490549248,
EFI free102995968 and RAM available7734300672 bytes. End boot-space delta is
145092608 bytes. The actual Plymouth hook also rebuilt all three old initrds,
so the earlier latest-only review was incomplete for the whole transaction.
Retain that prior assumption as history, not current behavior. Neither endpoint
space nor successful installation measures continuous peak usage or capacity.

Postboot21:21 snapshot: root free96957702144, boot free490549248, EFI free102995968,
RAM available7871856640 bytes. Only SSH22, loopbackDNS and DHCP listeners were
reported. These are MA endpoint measurements, not workload/load or capacity tests.

## Source Identity and Preservation

Local branch main, HEAD `58b1011dad796017e47dbcc82a4225ca449bcdf6`; dirty worktree
pre-existed. Source identities below were read locally before DOCOPS edits;
they identify documents, not a running kernel or package transaction.

| Source | SHA-256 |
|---|---|
| os-maintenance.md | B73DD13AE9C5E0ED2BBA3839161E24AECFB61649328B8D15A67A7FFBDD96E0BF |
| WI021 Evidence Pack | A1B9D0977219ACB8FA8B2A79C83AB65F7D9087F220B16DC1B20335745F31D2C1 |
| WI022 Evidence Pack | 5DFA098A699DD70AC76B8A3A8F4933F0A558DFEC58FE80AB5F51C8572AC51DCE |
| WI023 Evidence Pack | 38BFE6F6BDD3B73E6890B675FF60A3275262494BA6146C575A0122CE7F509DFD |
| WI024 handoff | AE2FB6D03E162E2A892C600772C6EEF611FEEC2F185C570D10B0BC46D70C220C |

Final local recheck: four sources above unchanged; WI023 has the coordinated
PG append-only AppArmor review, read and verified locally. Its current SHA-256
is `E552900C2C6920BA8627BD798A03C7D8D779BDB568DE6D4242520CC4FD147D16`.
The initial38BFE6F6... identity remains historical, not a whole-file unchanged
claim. MA88ef12 independently verified the original prefix hash preserved.
PG's addendum changes neither the runbook nor DOCOPS scope. Branch/HEAD unchanged.

## Commands & Tests

Local only: `git status --short`, `git branch --show-current`, `git rev-parse HEAD`,
scoped `Get-Content`, and `Get-FileHash -Algorithm SHA256` on the five sources.
Initial scoped validation passed: bundled validator exit0, 36 distinct supported
IDs; supplemental four-file check134 local links/eight metadata dependencies,
seven required metadata fields per file, four Tier0 paths, checkpoint anchor and
whitespace PASS. `git diff --check -- <four owned paths>` exited0 with only the
existing index CRLF normalization advisory. Untracked deliverable whitespace was
checked directly. Final four-file validation repeats the same checks after
closure. MA's wider twelve-file run93186b exposed a regex false positive from
the original reproduction call adjacency; the snippet now separates lookup,
construction and execution. The validator itself is unchanged.
Final closure run90e8b1 exited0: four-file bundled checks PASS,36 distinct
supported IDs. Supplemental407d57 passed136 local links/eight dependencies,
required metadata, four Tier0 files, whitespace and current checkpoint anchor.
Scoped diff98cadf exited0 with only the existing CRLF advisory. Historical
source preservation is reconciled above: four unchanged and one PG append.
No application test,
remote command, installation or reboot is executed by DOCOPS.

The skill script has no scoped CLI switch. It was run unchanged through
`python -B -` with `runpy` and a temporary in-process `Path.rglob` patch limiting
enumeration to the four owned paths. Reproduction body:

```python
import runpy
from pathlib import Path
from unittest.mock import patch
m = runpy.run_path('.agents/skills/validate-docs/scripts/validate_docs.py')
m['configure_stdio']()
root = Path.cwd()
files = [root / p for p in (
    'docs/index.md',
    'deliverables/user/REQ-20261010-ATS-007.md',
    'deliverables/user/WI-20261010-ATS-024-summary.md',
    'deliverables/agent/WI-20261010-ATS-024-evidence-pack.md',
)]
def scoped_glob(path, pattern):
    return iter(p for p in files if p.is_relative_to(path))
with patch.object(Path, 'rglob', scoped_glob):
    validator_type = m['DocValidator']
    validator = validator_type(root)
    raise SystemExit(validator.run())
```

Scope limits: file targets and supported ID matches, not external URLs, every
historical anchor or semantic correctness of all IDs. No docs-category file was
added, so the scoped index pass is not a repository-wide orphan audit.

## Drift Report and Applied Plan

HIGH: WI021's finished cleanup and cached package count are historical; using
them for this window would falsely close maintenance. Added a new index entry,
append-only REQ checkpoints and the two WI024 outputs, with historical pending
phases superseded by final closure and explicit owner attribution. No older
result or registered hash was rewritten.
No new glossary terms or docs-category files were added; only the current entry
needs index synchronization. Repository-wide freshness/orphan audits are outside
this bounded WI.

## Risks / Rollback / Follow-ups

Final runtime and cleanup receipts have been integrated; warnings and phased
residuals remain explicitly bounded. Further maintenance requires its own scope;
this closure does not authorize host changes or resolve every vulnerability.
Documentation rollback is a targeted correction of these four owned additions,
preserving concurrent work and historical receipts; no host rollback authorized.
REQ007 remains active for domain purchase before DNS/HTTPS, app DB/secrets/start,
backup/restore, alerts and load. WI024 Blocks none; no new delegation authorized.

## Related Documents

- [WI024 handoff](WI-20261010-ATS-024-handoff.md): Scope and final-receipt gate.
- [REQ007](../user/REQ-20261010-ATS-007.md): Active approved requirement.
- [WI023 review](WI-20261010-ATS-023-evidence-pack.md): Local review limits.
