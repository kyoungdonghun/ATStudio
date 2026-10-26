---
version: 1.0
last_updated: 2026-10-10
project: ATS
owner: PG
category: evidence-pack
status: stable
related_wi: WI-20261010-ATS-023
dependencies:
  - path: WI-20261010-ATS-023-handoff.md
    reason: Verified skill-generated review scope and output contract
  - path: WI-20261010-ATS-022-evidence-pack.md
    reason: SE preparation checkpoint reviewed independently
  - path: ../user/REQ-20261010-ATS-007.md
    reason: Existing approved maintenance scope and WI024 chain
---

# Evidence Pack: WI-20261010-ATS-023

> TL;DR: Local-preparation PASS with no material blocking finding. Baseline copies and index refresh/simulation are eligible after native STOP A/B; package application and reboot remain gated. No remote action was performed by PG.

## Scope and Findings

- [x] Reviewed trust/origin, no-removal, public-copy and conffile boundaries.
- [x] Reviewed restarts, service masks, retained access, reboot and cleanup gates.
- [x] Reviewed boot-space acceptance and transaction-error handling.
- [x] Independently checked exact source hashes and bounded local Bash syntax.
- [x] Separated MA observations from PG checks and recorded rollback limits.

**Findings by severity:** No material blocking finding identified in the reviewed
prepared procedure. No new maintenance framework or source change is required.
This finding is not acceptance of an as-yet-unseen package plan or native runtime.

Only this WI's summary and Evidence Pack were created. The on-disk handoff was
confirmed before evidence generation. Existing source, historical receipts and
dirty work were preserved; no credentials, remote connections, service actions,
package commands, deletion, commit or descendant delegation occurred.

## Review Pointers and Stage Verdicts

All line references below are to `deploy/single-vps/runbooks/os-maintenance.md`
at the exact hash recorded below.

| Location | Review result | Remaining native gate |
|---|---|---|
| Lines 23-28, 51-56, 141-160 | PASS: supervised individual commands, retained recovery/admin access, exact public-file copies, protected new destination | MA verifies ancestry/ACLs, actual copy set, owner/modes and source/copy hashes; no whole-/etc or secret backup |
| Lines 81-139, 164-183 | PASS: idle-lock semantics, timer race recheck, official sources/trust, exact masks, fail-on-any refresh and no-removal simulation | STOP A/B: current access/recovery, copies, preliminary space, paused timers, masks/listeners/jobs and locks before refresh |
| Lines 176-212 | HOLD for application, not a preparation defect: exact origins/versions, no removal/downgrade, refused preview and peak-space review precede the real transaction | STOP C: accept package set, restart effects, root/boot/EFI reserve and unchanged interactive final plan; unknown peak demand remains HOLD |
| Lines 208-220 | PASS: dpkg confold is bounded; ucf/generated files and other prompts handled separately; needrestart and maintainer scripts may restart services | Retain local configuration, review prompts/hooks and containment; do not disable restart mechanisms or rely on environment-only suppression |
| Lines 224-285 | HOLD for reboot/completion: native syntax/effective policy, masks, layout, hash/mode and boot checks precede one reboot and new-boot checks | STOP D/E: complete transaction, fresh access/recovery, expected boot artifacts/kernel and changed boot ID, no unexpected services/listeners |
| Lines 289-326 | PASS: errors stop progression; no speculative repair, forced reboot, broad restore or deletion; exact owned-key/timer/agent cleanup | MA records native outcomes and hands over recovery before closing the sole remaining route |

## Command-Local Download Limit

MA requested this safety parameter under the existing human-approved maintenance
scope. PG local review accepts `-o Acquire::http::Dl-Limit=4000` on update,
simulation, refusal preview and actual upgrade, without expanding authorization.
All existing error, signature, no-removal, conffile and interactive gates remain.
The baseline runbook hash does not contain this supplemental command option.

The [official Noble HTTP transport manual](https://manpages.ubuntu.com/manpages/noble/man1/apt-transport-http.1.html)
documents an integer kilobytes-per-second limit and prevention of simultaneous
downloads from multiple servers. Roughly 32 Mbps is a sizing approximation,
not measured wire throughput. This option does not bound all host traffic,
independent invocations, arbitrary package hooks or provider billing. Current
HTTP-only Ubuntu sources are MA-reported; changed transport/origin needs review.
The iwinv inbound condition is still unclarified. Do not claim compliance from
this parameter alone; no provider action or persistent configuration is proposed.

## Evidence Identity

PG verification date: 2026-10-10. Environment: local Windows/PowerShell and Git
Bash parse-only. Branch `main`; HEAD
`58b1011dad796017e47dbcc82a4225ca449bcdf6`. Runbook and handoff were untracked
working-tree inputs, not evidence of the committed or deployed version.

| Reviewed input | SHA-256 |
|---|---|
| `deploy/single-vps/runbooks/os-maintenance.md` | `B73DD13AE9C5E0ED2BBA3839161E24AECFB61649328B8D15A67A7FFBDD96E0BF` |
| `deploy/single-vps/runbooks/host-hardening.md` | `43755E6F58EF9543EB6DFDCE0F81C7DAF816D93C4FACA1B4125C2BE3B80F1E1A` |
| `deploy/single-vps/runbooks/first-host-setup.md` | `1C805ABFBC64E18662E0822DCBC6C2E4C5EFD0493D59ECA7334E0FF00AB88D33` |
| `deliverables/agent/WI-20261010-ATS-021-evidence-pack.md` | `A1B9D0977219ACB8FA8B2A79C83AB65F7D9087F220B16DC1B20335745F31D2C1` |
| `deliverables/agent/WI-20261010-ATS-022-evidence-pack.md` | `5DFA098A699DD70AC76B8A3A8F4933F0A558DFEC58FE80AB5F51C8572AC51DCE` |
| `deliverables/agent/WI-20261010-ATS-023-handoff.md` | `24C400721F2D0A38229AA76B3F15627B3C6862342F2FBF82C2FF020AEE3CB447` |

## Commands and Independent Results

| Command/check | PG result |
|---|---|
| `Get-Content` and numbered source review of the handoff/runbook, REQ007 and WI021/022 receipts | Scope and failure/recovery gates reviewed; earlier receipts kept historical |
| `Get-FileHash -Algorithm SHA256 -LiteralPath <exact input>` | Six hashes above measured locally; runbook matches supplied identity |
| `git rev-parse HEAD`; scoped `git status --short --branch -- <paths>` | Branch/HEAD and untracked inputs recorded; no Git mutation |
| `Get-Command bash -ErrorAction SilentlyContinue` | PATH lookup absent; used verified local Git Bash path instead, no install |
| Eight fenced blocks piped individually to Git Bash `--noprofile --norc -n` | PASS, exit0; no runbook commands executed |
| Three upgrade-line assertions for with-new-pkgs/no-remove/force-confold | PASS; all three preserve each guard |
| Four command strings with the added rate option piped to Bash `-n` | PASS, exit0; parse-only, not APT behavior or throughput testing |

Reproduce the bounded syntax check from repository root in PowerShell:

```powershell
$bash = 'C:\Program Files\Git\bin\bash.exe'
$doc = Get-Content -Raw -LiteralPath deploy/single-vps/runbooks/os-maintenance.md
$blocks = [regex]::Matches($doc, '(?ms)^```bash\r?\n(.*?)^```\s*$')
if ($blocks.Count -ne 8) { throw 'Unexpected block count' }
foreach ($block in $blocks) {
    $block.Groups[1].Value.Replace("`r`n", "`n") | & $bash --noprofile --norc -n
    if ($LASTEXITCODE -ne 0) { throw 'Bash syntax failure' }
}
$upgrades = @([regex]::Matches($doc, '(?m)^apt-get .* upgrade\r?$'))
if ($upgrades.Count -ne 3) { throw 'Unexpected upgrade count' }
foreach ($command in $upgrades) {
    foreach ($flag in @('--with-new-pkgs', '--no-remove', 'Dpkg::Options::=--force-confold')) {
        if (-not $command.Value.Contains($flag)) { throw "Missing $flag" }
    }
}
$adjusted = 'apt-get -o Acquire::http::Dl-Limit=4000 --error-on=any update' + "`n" +
    (($upgrades | ForEach-Object {
        $_.Value.Replace('apt-get ', 'apt-get -o Acquire::http::Dl-Limit=4000 ')
    }) -join "`n")
$adjusted | & $bash --noprofile --norc -n
if ($LASTEXITCODE -ne 0) { throw 'Rate-option syntax failure' }
```

Primary source checks on 2026-10-10 confirmed bounded command semantics:
[apt-get](https://manpages.ubuntu.com/manpages/noble/man8/apt-get.8.html)
for simulation without locks, no-remove, refusal, command-local options and
error-on-any; [dpkg](https://manpages.ubuntu.com/manpages/noble/man1/dpkg.1.html)
for confold; and [Ubuntu's Noble restart announcement](https://discourse.ubuntu.com/t/needrestart-changes-in-ubuntu-24-04-service-restarts/44671)
for restart behavior. These are documentation checks, not native execution.
Coverage and host integration tests: N/A for this bounded local document review.

## MA Observations and Unverified Limits

MA reports fresh root-console authentication, pinned atadmin/PTY/sudo, matching
policy hashes/modes, effective SSH and journal policy, and layout-after PASS.
MA reports empty-output fuser exit1, inactive/dead APT services, no scheduled
shutdown, signed official Noble sources, insecure/weak/downgrade trust flags0,
and no APT_CONFIG/DPKG_FORCE/DEBIAN_FRONTEND overrides. Distribution-owned hooks
and ucf-managed unattended-upgrades files were identified; default ESM pins have
no corresponding sources or subscription action. All are MA-owned observations.

MA used read-only Python lstat/listxattr metadata checks for real root-owned,
non-writable ancestry and absent POSIX ACL attributes because getfacl was absent.
This is acceptable in principle on the reported Linux filesystem if every
required path is covered and errors fail closed; PG did not run or independently
audit that remote check. No extra tool installation is required for this review.

Reported free root space is 98,315,579,392 bytes, separate boot635,641,856 bytes
and EFI102,995,968 bytes, with three retained kernel/initrd sets. These are not
an accepted peak-space budget. No refresh, chosen package plan, upgrade, reboot,
postboot validation, capacity or complete vulnerability-remediation PASS is
established by PG. WI021's earlier cleanup is not cleanup of this new window.

## Reference Documents

Input pointers preserved from the verified handoff; the four constitutional
documents were supplied in full (STD-001, STD-004, STD-002, STD-005).

| Handoff tier | Document | Review purpose |
|---|---|---|
| 0 | `docs/standards/core-principles.md` | Approved scope and transparency |
| 0 | `docs/standards/documentation-standards.md` | English metadata and traceability |
| 0 | `docs/standards/development-standards.md` | Evidence-led verification |
| 0 | `docs/standards/glossary.md` | Canonical terms |
| 0 | `docs/standards/evidence-pack-standard.md` | Evidence structure; tier copied from handoff |
| 1 | `.claude/agents/pg.md` | Independent risk review and two outputs |
| 1 | `docs/policies/security-policy.md` | No secrets or raw private operational output |
| 1 | `docs/policies/access-control-policy.md` | Least privilege and retained recovery |
| 1 | `docs/policies/quality-gates.md` | Gated progression |
| 2 | `.agents/skills/create-wi-evidence-pack/SKILL.md` | Confirmed handoff precondition and evidence generation |
| 2 | `deploy/single-vps/runbooks/os-maintenance.md` | Primary procedure reviewed |
| 2 | `deploy/single-vps/runbooks/host-hardening.md` | Existing effective-policy/native checks |
| 2 | `deploy/single-vps/runbooks/first-host-setup.md` | Native SSH and exact-key cleanup |
| Context | `deliverables/user/REQ-20261010-ATS-007.md` | Existing human scope and chain |
| Context | `deliverables/agent/WI-20261010-ATS-022-evidence-pack.md` | SE preparation, not PG test results |
| Context | `deliverables/agent/WI-20261010-ATS-021-evidence-pack.md` | Historical MA execution and cleanup |

Rule source: `.claude/config/context-injection-rules.json`; assignee `pg`;
task types security/review; agent-required tiers `[0, 1]`. Project tag `ATS`
follows `.claude/config/workspace.json`. Current-decision entry in
`docs/index.md` was used for navigation, not substituted for native evidence.

## Risks, Rollback and Chain

Remaining risk is environment-dependent: selected package hooks, peak boot
space, access across service restarts/reboot and actual containment must pass
native gates. Stop on uncertainty; retain console and do not interrupt a running
package transaction. Exact-file configuration recovery requires comparison and
native validation before reload; never replay permissive SSH rollback.
Copies cannot undo package/kernel/data-format changes. Retained kernels are
not guaranteed boot or userspace recovery, and paid snapshots are not implied.

PG has no runtime rollback to perform because it changed only these two reports.
Correct later evidence by a dated addendum; preserve this review identity and
historical receipts. WI023 is locally complete: MA must immediately continue the
WI024 skill-generated handoff/delegation for integration of actual operation
receipts and remaining HOLD gates. No descendant is launched by PG.

## Related Documents

- [Summary](../user/WI-20261010-ATS-023-summary.md): Concise per-stage disposition.
- [Handoff](WI-20261010-ATS-023-handoff.md): Exact delegated scope.
- [REQ007](../user/REQ-20261010-ATS-007.md): Existing approval and WI024 chain.
- [Runbook](../../deploy/single-vps/runbooks/os-maintenance.md): Reviewed procedure.
- [WI022 evidence](WI-20261010-ATS-022-evidence-pack.md): Preparation checkpoint.
- [WI021 evidence](WI-20261010-ATS-021-evidence-pack.md): Historical native receipts.

## Addendum: 2026-10-10 Preboot AppArmor Diagnostic Review

**Disposition:** Non-blocking for the already approved single reboot on the
reported completed preboot gates, with the vendor warning retained as
unpatched/unresolved. This supplements the earlier staged review; historical
HOLD statements describe their earlier checkpoints. PG does not independently
certify the completed native transaction or expand human authorization.

MA's latest report identifies `/lib/apparmor/rc.apparmor.functions:148` in
`check_userns()`, sourced by postinst. The helper reads the userns restriction
sysctl and kernel feature file
`/sys/kernel/security/apparmor/features/policy/unconfined_restrictions/userns`.
The latter reports `yes`; its comparison with numeric zero emits
`Illegal number: yes`. From the supplied conditional, this failed comparison
does not enter the branch that disables the restriction. That control-flow
inference is not an independent measurement of the effective sysctl, kernel
mediation or every loaded profile.

MA reports subsequent profile loading success, AppArmor enabled (`Y`), service
`Result=success`, and the specifically checked mysqld and lsb_release profiles
in enforce mode. MA also reports transaction exit0, clean dpkg audit, all205
exact versions with delta180 upgrades/25 new/0 removals, 16 baseline hashes
unchanged, three effective SSH contexts retaining restrictions, layout PASS,
masked MySQL native validation exit0 and nine unchanged journal values. PG did
not execute these checks; no full-profile enforcement claim follows from them.

Independent primary-source lookup on 2026-10-10 found
[Ubuntu bug 2105840](https://bugs.launchpad.net/ubuntu/+source/apparmor/+bug/2105840),
which documents the same numeric comparison against `yes` and is marked a
duplicate of bug2102680. It corroborates the diagnostic class, not the target's
exact package/kernel identity, patch status or postboot behavior. No package
edit, reinstall or restriction change was attempted according to MA; PG made
only append-only changes to the two WI023 reports.

After the one approved reboot, MA retains the existing STOP E checks and
rechecks AppArmor activation/service outcome, the two named loaded profile
modes, the effective userns setting against baseline, and bounded recurrence
of this diagnostic or profile-loading errors. Missing profiles, an unexpected
enforce-to-complain change, disabled AppArmor, failed loading or access
regression means HOLD and a separately reviewed repair. Do not silence the
warning by editing vendor scripts, weakening the sysctl, or disabling AppArmor.
The warning may persist or change with the newly booted kernel; neither its
absence nor service success alone establishes universal enforcement.

Local evidence commands for this addendum: `Get-Content -Tail` on both owned
reports and `Get-FileHash -Algorithm SHA256` before appending. No host command,
reboot, package repair or production validation was performed by PG. MA/WI024
owns the dated postboot and cleanup receipt; it remains pending in this review.
