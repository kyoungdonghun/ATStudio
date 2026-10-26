---
version: 1.0
last_updated: 2026-10-10
project: ATS
owner: PG
category: work-summary
status: stable
dependencies:
  - path: ../agent/WI-20261010-ATS-023-handoff.md
    reason: Delegated local safety review
  - path: ../agent/WI-20261010-ATS-023-evidence-pack.md
    reason: Exact source identity, checks and remaining native gates
---

# WI023 Package Maintenance Safety Review

> TL;DR: Local-preparation PASS; no material blocking finding in the reviewed procedure. Guarded baseline copies, index refresh and simulation may proceed within the approved scope after STOP A/B. Upgrade and reboot remain separately gated.

## Disposition

Reviewed `deploy/single-vps/runbooks/os-maintenance.md` SHA-256:
`B73DD13AE9C5E0ED2BBA3839161E24AECFB61649328B8D15A67A7FFBDD96E0BF`.

| Stage | PG local disposition | MA-native condition |
|---|---|---|
| Protected public-configuration copies | PASS | Fresh retained admin/sudo and authenticated recovery console; trusted ancestry, new protected destination, exact file scope and matching copy hashes |
| Index refresh and simulation | PASS after STOP A/B | Pause only the two idle APT timers; recheck locks/jobs, signatures/sources, exact MySQL/Nginx masks and preliminary space; stop on refresh errors |
| Package upgrade | HOLD at STOP C | Accept exact versions/origins, no removals/downgrades, restart effects, and measured root/boot/EFI peak-space reserve; preserve configuration at all prompts |
| One reboot and completion | HOLD at STOP D/E | Successful completed transaction, native policy/access/boot checks, then changed boot ID and fresh postboot verification; reconcile timers and exact owned-key/agent cleanup |

## Bounded Runtime Parameter

MA proposed adding `-o Acquire::http::Dl-Limit=4000` to each update,
simulation, preview and actual upgrade invocation. PG finds this command-local
addition acceptable without changing system configuration or weakening existing
guards. The official HTTP transport documentation specifies kilobytes/second
and disables concurrent downloads from multiple servers with this option.
It is an APT HTTP control, not a host-wide traffic, billing or iwinv contract
guarantee; the unresolved provider condition remains unresolved. Keep the same
option and transaction guards in every invocation, with one APT transaction at
a time. The reviewed runbook itself was not edited to include this parameter.

## Evidence and Limits

- PG independently parsed all eight Bash blocks and four parameter-adjusted
  command forms; none was executed. All three upgrade forms retain their guards.
- Fresh console/access, package-manager inactivity, trust configuration,
  filesystem/ACL checks, installed hashes and layout results are MA-reported
  observations, not PG remote verification. No additional tool installation is
  required solely because MA used a read-only metadata check instead of getfacl.
- Configuration copies do not provide package/kernel/disk rollback. Unknown
  space demand, transaction errors, policy drift or lost recovery means HOLD.
- This is PG local review under the existing human-approved REQ scope, not new
  authorization, production validation or a capacity guarantee.
- WI023 local review is complete. MA owns the immediate WI024 handoff/delegation
  and actual-operation evidence integration; this reviewer creates no descendants.

## Related Documents

- [Evidence Pack](../agent/WI-20261010-ATS-023-evidence-pack.md): Commands, pointers and hashes.
- [Approved REQ007](REQ-20261010-ATS-007.md): Authority and WI024 chain.
- [Reviewed runbook](../../deploy/single-vps/runbooks/os-maintenance.md): STOP A-E and failure responses.
- [APT HTTP transport](https://manpages.ubuntu.com/manpages/noble/man1/apt-transport-http.1.html): Command-local rate-limit semantics checked on 2026-10-10.

## Addendum: 2026-10-10 Preboot AppArmor Warning

MA reports the exact 205-package transaction completed with exit0 and clean
dpkg audit; preboot policy, access, layout and masked MySQL validation passed.
The AppArmor helper's numeric comparison of kernel feature value `yes` emitted
`Illegal number: yes`, after which profile loading succeeded. MA reports
AppArmor enabled, service Result=success, and the specifically checked mysqld
and lsb_release profiles in enforce mode. These are MA observations, not PG
remote verification or proof that every profile is enforced.

PG finds no additional reboot blocker from this warning alone, conditional on
the reported preboot gates and retained recovery. The warning is **unpatched
and unresolved**, consistent with [Ubuntu bug 2105840](https://bugs.launchpad.net/ubuntu/+source/apparmor/+bug/2105840).
After the already approved single reboot, MA must recheck AppArmor activation,
the two named profile modes, the userns setting and warning recurrence. Missing
profiles, downgraded modes, service/load failure or access regression means
HOLD. No script edit, reinstall, sysctl weakening or additional reboot is
authorized by this local review; postboot success remains unverified here.
