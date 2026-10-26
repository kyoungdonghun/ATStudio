---
version: 1.0
last_updated: 2026-10-10
project: ATS
owner: SE
category: evidence-pack
status: draft
related_wi: WI-20261010-ATS-022
dependencies:
  - path: WI-20261010-ATS-022-handoff.md
    reason: Skill-generated assignment and exclusive three-file scope
  - path: ../user/REQ-20261010-ATS-007.md
    reason: Approved package update and one controlled reboot
  - path: WI-20261010-ATS-021-evidence-pack.md
    reason: Final runtime and cleanup baseline, not new-window completion
---

# Evidence Pack: WI-20261010-ATS-022

> TL;DR: Local package-maintenance preparation completed for WI023 PG review. No remote execution by SE; all update/reboot outcomes remain NOT RUN by SE.

## Summary and Scope

Added one phased Ubuntu24.04 maintenance runbook and the two assigned deliverables.
No helper, framework, application change, source-list change or operational
execution was added. Preserved the dirty shared checkout and historical records;
HEAD observed `58b1011dad796017e47dbcc82a4225ca449bcdf6`.

- [x] Official source/signature, audit, lock, filesystem and recovery gates documented.
- [x] Exact removal-free simulation/confirmation, conffile preservation and Noble needrestart behavior documented.
- [x] MySQL/Nginx masks, fresh strict-pinned access, changed boot ID, postboot checks and exact agent cleanup documented.
- [x] Recovery limitations and protected configuration-copy boundaries explicit.
- [x] Primary sources consulted and local Bash syntax checked without execution.
- [x] User-facing summary and Agent-facing Evidence Pack produced using the required skill.
- [ ] WI023 independent PG approval and MA's actual package/space plan acceptance.
- [ ] Remote refresh, upgrade, reboot, postboot and new-window cleanup receipts.

Acceptance checkboxes above describe local preparation, not live acceptance.

## Reference Documents

The on-disk WI022 handoff existed and was read before Evidence Pack generation.
Its INPUT POINTERS supply this reference inventory. Four mandatory Tier0 texts,
SE role and security policy were injected by MA; relevant remaining pointers
were read locally. Conditional frontend pointers are not runtime verification.

| Tier/context | Documents | Use |
|---|---|---|
| 0 | `docs/standards/core-principles.md` (STD-001), `documentation-standards.md` (STD-004), `development-standards.md` (STD-002), `glossary.md` (STD-005) | Authority, scope, documentation and canonical terms |
| 0 as listed in handoff | `docs/standards/evidence-pack-standard.md` | Evidence fields and reproducibility |
| 1 | `.claude/agents/se.md`; `docs/policies/security-policy.md`, `access-control-policy.md`, `quality-gates.md` | Delegated implementation, privacy and independent review |
| 2 | `.agents/skills/create-wi-evidence-pack/SKILL.md` | Precondition verified; output written directly |
| 2 | `deploy/single-vps/runbooks/host-hardening.md`, `first-host-setup.md`; `deploy/single-vps/CONFIGURATION.md` | Reuse verification/cleanup; preserve configuration contract |
| 2 conditional, not loaded/executed | `.agents/skills/react-best-practices/AGENTS.md`; `docs/standards/frontend-standards.md` | No frontend changes |
| Context | `deliverables/user/REQ-20261010-ATS-007.md`; `WI-20261010-ATS-021-evidence-pack.md` | Latest approval and final20:39KST completion addendum |
| Files | `scripts/deployment/host-hardening/check_layout.py`, `test_candidates.py` | Existing read-only layout contract and preserved candidate tests |

Injection rule source: `.claude/config/context-injection-rules.json`, assignee
`se`, required tiers `[0]`; task security/testing/operation context supplied by
the handoff. `.claude/config/workspace.json` resolves tag `ATS`. The current
decision entry in `docs/index.md` agrees with WI021's completed dated receipt.
No new handoff, index edit, commit, stage or push was performed by SE.

## Evidence Pointers

| Changed path | Location and rationale |
|---|---|
| `deploy/single-vps/runbooks/os-maintenance.md` | Sections1-2: authority/fresh recovery, sources/trust, audit/locks, boot space, masks and bounded copies |
| Same runbook | Section3: exact APT simulation/preview/upgrade forms; manual stop gates; conffile and restart limits |
| Same runbook | Sections4-5: native configuration checks without DB start, one reboot, postboot proofs, recovery and exact cleanup |
| `deliverables/user/WI-20261010-ATS-022-summary.md` | User-facing scope, risk, status and pending boot-space acceptance |
| This Evidence Pack | Reproduction, sources, hashes and WI023 trigger |

Only these three files were created. No runbook command block was executed
against the host or the local operating system.

### MA-Supplied Observation, Not SE Verification

The20:59KST read-only receipt supplied in chat reports pinned admin/sudo and root
tty1 proof, five policy hashes matching WI021, kernel6.8.0-124, official signed
Noble sources including existing backports, empty audit/holds and no locks.
APT::Periodic values were0; timers active; the only unattended-upgrade process
was the shutdown waiter, not an active package transaction. Normal Noble
needrestart hook was present without local overrides. MySQL/Nginx remained exact
masks/inactive. Free bytes: root98,315,579,392; separate boot635,641,856;
EFI102,995,968. Three old kernel/initrd sets remain, with no removal permission.

This refines the new-window access gate but does not approve a package set or
establish enough peak boot space. MA reports no remote writes/update/reboot at
that checkpoint. No endpoints, fingerprints, key bytes or raw host logs are copied.

## Commands and Local Verification

Executed locally on Windows/PowerShell,2026-10-10:

- Targeted `Get-Content`/`rg` on approved input files and source metadata.
- `git status --short`, `git rev-parse HEAD`: dirty checkout preserved; HEAD above.
- `Get-FileHash -Algorithm SHA256 -LiteralPath ...`: exact content hashes below.
- Git for Windows Bash `-n` on every fenced Bash block: **PASS,8 blocks**; parsing only, no shell evaluation of their commands.
- Direct checks: **PASS**,3 documents with seven required metadata fields, related-document sections, no trailing whitespace and16 resolving local links.
- SHA-256 manifest comparison: **PASS**,2 new artifacts and7 preserved input files match all9 entries below.
- Bounded command text check: **PASS**,3 upgrade forms each retain `--with-new-pkgs`, `--no-remove`, `--force-confold`; one controlled reboot; no fenced removal, MySQL client or mysql/nginx start/unmask command. This is a presence check, not operational proof.
- Scoped `git diff --check`: exit0; new files were also checked directly because they are untracked.

Reproduce the bounded syntax check from the repository root in PowerShell:

```powershell
$ErrorActionPreference = 'Stop'
$body = Get-Content -Raw -LiteralPath 'deploy/single-vps/runbooks/os-maintenance.md'
$blocks = [regex]::Matches($body, '(?s)```bash\r?\n(.*?)```')
foreach ($block in $blocks) {
    $block.Groups[1].Value | & 'C:\Program Files\Git\bin\bash.exe' -n
    if ($LASTEXITCODE -ne 0) { throw 'Bash syntax failed' }
}
"PASS: $($blocks.Count) Bash blocks, parse only"
```

No Linux target tools were invoked. Bash parsing does not check installed APT
options, systemd behavior, file ancestry/modes, configuration semantics, native
AppArmor behavior, MySQL validation, package hooks, networking or reboot recovery.
No executable/helper changes: unit/coverage/application-build runs are N/A for
this documentation-only WI; existing helper tests were not represented as new
runtime proof. `git diff --check` alone cannot validate new untracked files.

### SHA-256 Manifest

| New file | SHA-256 |
|---|---|
| `deploy/single-vps/runbooks/os-maintenance.md` | `B73DD13AE9C5E0ED2BBA3839161E24AECFB61649328B8D15A67A7FFBDD96E0BF` |
| `deliverables/user/WI-20261010-ATS-022-summary.md` | `E9CF460FD021000BB2CDE3CD6648A884D2A0B13C51CD15015F943E1B95C718D7` |

This Evidence Pack does not self-hash. Historical registered hashes are unchanged.

| Preserved input | SHA-256 |
|---|---|
| `deploy/single-vps/runbooks/host-hardening.md` | `43755E6F58EF9543EB6DFDCE0F81C7DAF816D93C4FACA1B4125C2BE3B80F1E1A` |
| `deploy/single-vps/runbooks/first-host-setup.md` | `1C805ABFBC64E18662E0822DCBC6C2E4C5EFD0493D59ECA7334E0FF00AB88D33` |
| `deploy/single-vps/CONFIGURATION.md` | `BD1A8E2FF426F7660AA063D890FBE446E7D60A8C323EC49CF6DDF9184B9C4664` |
| `scripts/deployment/host-hardening/check_layout.py` | `CE755FDBDC88D2E1B8CCE83834CF167568239C6FA2273EB56320277440AFAD81` |
| `scripts/deployment/host-hardening/test_candidates.py` | `BD24759B6F75BD463D43DF36E0A0388A4CEE6C1541EDCF38486D72B138F42915` |
| `deploy/single-vps/mysql/atstudio.cnf` | `008136130093048F395C4F414A937541AD314615E656AF7A64A170E4F376A233` |
| `deploy/single-vps/journald/atstudio.conf` | `229473C1D88E2CF30DAC6E9F4ED34327A39A66A74653DC72465E42239B2FC963` |

## Primary Sources

Consulted2026-10-10. Sources inform the procedure, not deployed-host results.

| Primary reference | Decision supported |
|---|---|
| [Ubuntu Noble needrestart change](https://discourse.ubuntu.com/t/needrestart-changes-in-ubuntu-24-04-service-restarts/44671) | Default APT service restarts accepted inside the supervised window; no invented global suppression |
| [Noble apt-get](https://manpages.ubuntu.com/manpages/noble/man8/apt-get.8.html) | Error-on-any update, simulation limits, new dependencies without removal, refusal preview |
| [Noble dpkg](https://manpages.ubuntu.com/manpages/noble/man1/dpkg.1.html) | Modified conffile preservation; confdef can override confold |
| [Noble apt-secure](https://manpages.ubuntu.com/manpages/noble/man8/apt-secure.8.html) | Signed archive trust and rejection of unsafe bypasses |
| [Noble sources.list](https://manpages.ubuntu.com/manpages/noble/man5/sources.list.5.html) | Legacy/deb822 sources and scoped signing trust |
| [Ubuntu automatic updates](https://ubuntu.com/server/docs/how-to/software/automatic-updates/) | Timer activity/catch-up and automatic-reboot policy inspection |

## Risks and Rollback

Package hooks may restart access services or alter generated configuration.
Simulation and configuration copies do not supply an atomic OS rollback. Keep
native recovery and masks; retain old kernels, but do not promise old-kernel
recovery before verifying console/bootloader capability. Exact target failures
require a reviewed repair, not force flags, package deletion or automatic replay.
Actual boot-space acceptance, origin/version plan and postboot evidence remain
MA-owned HOLD gates. No workload-capacity or vulnerability-closure claim is made.

Local correction affects only these three documentation files via a scoped
patch; preserve this evidence and any registered hashes with a dated addendum.
There is no remote change by SE to undo. Runbook section5 defines the separate
operator recovery and temporary-agent cleanup requirements.

## Follow-up

**Trigger WI-20261010-ATS-023 now:** MA should delegate independent PG review of
this runbook and manifest before target mutation. Review exact native options,
package/restart containment, peak boot-space plan, access continuity and cleanup.
REQ007 remains active. SE does not create live receipts or claim MA's execution.

## Related Documents

- [WI022 handoff](WI-20261010-ATS-022-handoff.md): Assignment and output contract.
- [WI022 summary](../user/WI-20261010-ATS-022-summary.md): User-facing report.
- [Maintenance runbook](../../deploy/single-vps/runbooks/os-maintenance.md): Review target.
- [WI021 final evidence](WI-20261010-ATS-021-evidence-pack.md): Preserved previous window.
