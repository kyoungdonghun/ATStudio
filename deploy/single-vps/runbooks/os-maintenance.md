---
version: 1.0
last_updated: 2026-10-10
project: ATS
owner: SE
category: guide
status: draft
dependencies:
  - path: ../../../deliverables/user/REQ-20261010-ATS-007.md
    reason: Approved official Noble package maintenance and one controlled reboot
  - path: ../../../deliverables/agent/WI-20261010-ATS-021-evidence-pack.md
    reason: Completed host baseline, including final runtime and cleanup addendum
  - path: host-hardening.md
    reason: Reuse effective-policy, access and read-only layout checks
---

# Ubuntu Package Maintenance

> TL;DR: Local preparation only. WI023 PG review and fresh MA access/recovery proofs precede execution. Keep Ubuntu 24.04 and MySQL/Nginx stopped and masked.

## 1. Authority and Stop Rules

MA executes one reviewed phase at a time in the existing native strict-pinned
`atadmin` session or authenticated recovery console. Bash excerpts below are
individual root-shell commands, not an unattended installer. Record each actual
exit status immediately; stop on any unexpected status or output before the next
command. Do not hide errors with a pipeline, trailing successful command or
blanket stderr suppression. Keep private operational output outside the repo.

[WI021's final 20:39 KST receipt](../../../deliverables/agent/WI-20261010-ATS-021-evidence-pack.md)
records working administrator access, restricted SSH, checked directories,
MySQL/Nginx masked and inactive, and completed temporary-agent/console cleanup.
That receipt is historical, not proof for this new window. Cached 182 upgradable
packages were a mixed updates/security observation, not an exact current plan.

MA's read-only 20:59 KST receipt reports fresh pinned atadmin/sudo and root tty1
recovery PASS, five policy hashes matching WI021, no holds/audit findings or
package locks, and unchanged MySQL/Nginx masks. Official signed Noble sources
include the already present backports pocket; all APT::Periodic values are0,
timers are active, and needrestart3.6 uses the normal `apt-pinvoke -m u` hook.
Free bytes: root98,315,579,392; separate boot635,641,856; EFI102,995,968. Three old
kernel/initrd sets remain. These are MA observations, not SE verification or
space approval: actual update/simulation and peak boot-space review are pending.

No release upgrade, `dist-upgrade`, `full-upgrade`, removal/autoremove/purge,
source/key changes, forced downgrade, hold bypass, database query/mutation,
account change, app activation, port/firewall change or purchase is authorized.
No extra backup/snapshot product is assumed. Unknowns or insufficient space mean
HOLD with measured evidence, impact, alternatives and cost; not automatic cleanup.

**STOP A, before mutation:** Require fresh non-multiplexed pinned key login,
interactive shell/PTY and `sudo -n id -u` = 0. User must authenticate the recovery
console again; its login screen is insufficient. Retain both routes. Reuse the
[native access and agent procedure](first-host-setup.md), now with `atadmin` and
the same privately verified host pin/key. Never enable root SSH for maintenance.
Record this window's agent state/key ownership; no secret is entered by an agent.

## 2. Preflight and Configuration Copies

Require Ubuntu 24.04/noble, correct host identity, synchronized time, and the
approved architecture. Record current kernel and boot ID privately. Reuse
[host-hardening sections 2-6](host-hardening.md) as verification only; do not rerun
account creation, directory creation, configuration installation or MySQL start.

```bash
sudo -n id -u
cat /etc/os-release
uname -r
cat /proc/sys/kernel/random/boot_id
timedatectl show -p Timezone -p NTPSynchronized
dpkg --audit
apt-mark showhold
systemctl show apt-daily.timer apt-daily-upgrade.timer -p Id -p ActiveState -p UnitFileState
systemctl show apt-daily.service apt-daily-upgrade.service -p Id -p ActiveState -p SubState
fuser /var/lib/dpkg/lock /var/lib/dpkg/lock-frontend /var/lib/apt/lists/lock /var/cache/apt/archives/lock
df -B1 / /var /boot
df -i / /var /boot
findmnt --target /boot
```

`dpkg --audit` must produce no findings, not merely exit0. For `fuser`, exit1
with no owner and no diagnostic is the expected idle case; any owner, permission
error or missing-tool ambiguity is HOLD. Inspect pending jobs and process names
without arguments/environment if needed. Never delete lock files or kill APT,
dpkg or unattended-upgrades. Wait for legitimate work to finish, then resurvey.
For mounted `/boot/efi`, also inspect its mount, available bytes and inodes.
Record separate filesystems so shared `/`, `/var` and `/boot` space is not counted
multiple times. Preserve installed old kernels. A free-rootfs figure alone is
not proof of room for package unpacking, new kernel/initramfs and boot assets.

Privately inspect only public package configuration, including all active `.list`
and deb822 `.sources` files, pin preferences, APT/dpkg hooks and option overrides.
Do not dump `apt-config dump`, APT auth/proxy credentials, client option files or
raw logs into evidence. Reject alternate configuration/source paths or unknown
hooks. Require official Ubuntu archives/mirrors and Noble origin/suites
`noble`, `noble-updates`, `noble-security`; an already recorded official
`noble-backports` source is not permission to select backport packages. Unknown
origin, proposed/PPA/other release, paid source or selected backport is HOLD.
Verify `Signed-By`/effective trust uses the distribution Ubuntu archive keyring;
no `Trusted=yes`, insecure/weak/downgrade-to-insecure allowances, disabled date
checks or unauthenticated installs. Do not import keys or accept changed release
identity to make an error disappear. [APT trust](https://manpages.ubuntu.com/manpages/noble/man8/apt-secure.8.html)
and [source formats](https://manpages.ubuntu.com/manpages/noble/man5/sources.list.5.html)
define these controls.

Inspect effective `Unattended-Upgrade::Automatic-Reboot`; require false (including
the documented default when unset), no scheduled reboot and no competing
maintenance. Record original timer states. Once services/locks are idle, pause
only these two timers for the window, without disabling or masking them:

```bash
systemctl stop apt-daily.timer apt-daily-upgrade.timer
systemctl show apt-daily.timer apt-daily-upgrade.timer -p Id -p ActiveState -p UnitFileState
```

Require timers inactive with unchanged enablement; repeat service/job/lock checks
after stopping and immediately before each package transaction. Do not stop a
running package service. Enabled timers can catch up after reboot: wait for their
work before postboot package checks, rather than racing it. Restore original
timer activity on cancellation/completion; do not change persistent update
policy. [Ubuntu automatic updates](https://ubuntu.com/server/docs/how-to/software/automatic-updates/).

Before index refresh, require the following masks to be exact symlinks to
`/dev/null`, and both services inactive/dead with no start jobs or listeners:

```bash
test -L /etc/systemd/system/mysql.service
test "$(readlink /etc/systemd/system/mysql.service)" = /dev/null
test -L /etc/systemd/system/nginx.service
test "$(readlink /etc/systemd/system/nginx.service)" = /dev/null
systemctl show mysql.service nginx.service -p Id -p LoadState -p ActiveState -p SubState
systemctl list-jobs --no-pager
ss -lntup
```

Review listener output privately; report only expected port/bind classes. Recheck
these assertions after package completion and after boot. Do not unmask/start
either service. Review installed and candidate service/maintainer-hook changes
when selected packages affect them; an unknown bypass of containment is HOLD.

Use a fresh `/root/wi022` directory (root:root0700) only after proving `/` and
`/root` real, root-owned, non-writable by others and free of unexpected ACLs.
Reject any existing destination, including dangling symlinks. Set `umask 077`.
Retain protected configuration copies and mode/owner/hash receipts there, using
`cp --parents --preserve=mode,ownership,timestamps -- <reviewed-files> /root/wi022/`.
Replace the placeholder only with the exact public files reviewed in the survey:

| Copy/record | Bound |
|---|---|
| SSH | Main config and each actual included public config; never host keys or authorized-key bodies |
| Sudo | Main sudoers and actual public includes; preserve atadmin policy/mode |
| MySQL | `/etc/mysql/mysql.cnf` and actual server/include files; record package `my.cnf` symlink separately, never copy `debian.cnf`, client credentials or datadir |
| Journal | Effective public main/drop-ins including local `zz-atstudio.conf`, ordering and values |
| Other evidence | Package versions/holds, source configuration hashes, masks, boot ID, kernel, timers, resource totals; no broad `/etc` archive |

Require regular source files, trusted real ancestry (except the separately
recorded package symlink), no concurrent edits and matching source/copy hashes.
Keep copies private and retained. Record the five approved local policy hashes
and the reviewed staged layout-checker hash. These copies do not back up package
binaries, the database, bootloader or whole disk.

## 3. Refresh, Review, Then Upgrade

**STOP B:** Access, recovery, audit, official sources, trust, idle package manager,
configuration copies and preliminary space checks must all pass. Run as root
in the supervised native console, with no unreviewed APT_CONFIG/DPKG_FORCE or
noninteractive/automatic-yes override. Index/cache refresh is itself a mutation.

```bash
apt-get --error-on=any update
apt-get check
apt-cache policy
apt-get --simulate --verbose-versions --with-new-pkgs --no-remove -o Dpkg::Options::=--force-confold upgrade
```

Require successful refresh with no failed/ignored repository, signature or date
warning. Review each selected package/version/architecture and candidate origin
(use `apt-cache policy` with the exact selected names). Reject any removal,
downgrade, hold change, unexpected dependency/service or source surprise. Record
kept-back packages and reasons; do not force phased/held updates. The simulation
does not lock the package database, so recheck the plan and locks before applying.
The selected upgrade form permits reviewed new dependencies, including kernel
packages, but forbids removals. [APT command semantics](https://manpages.ubuntu.com/manpages/noble/man8/apt-get.8.html).

Preview real download/installed-size totals without installation:

```bash
apt-get --assume-no --verbose-versions --with-new-pkgs --no-remove -o Dpkg::Options::=--force-confold upgrade
```

This is an intentional refusal, not an upgrade PASS. Record its actual exit code
and confirm it reached the expected plan/prompt and aborted before unpacking;
any other failure is HOLD. If there is nothing to upgrade it may finish normally.
Compare the exact plan with the simulation. Check measured per-filesystem space
against download bytes, additional installed bytes, temporary unpacking and
kernel/initramfs/boot workspace. MA records an explicit reserve and rationale;
unknown peak boot-space demand is HOLD, not guessed headroom. No auto-cleaning.

**STOP C:** PG/MA must accept the exact package set, restart impact and measured
space, including boot reserve. Recheck pinned admin access/recovery and masks.
Run the same transaction without `--simulate` or `--assume-no`, keeping the final
interactive confirmation. Answer no if its plan differs; resimulate and review.

```bash
apt-get --verbose-versions --with-new-pkgs --no-remove -o Dpkg::Options::=--force-confold upgrade
```

`--force-confold` preserves locally modified dpkg conffiles; do not combine it
with `--force-confdef` or `--force-confnew`. It is not protection for all generated
files, hooks or ucf-managed configuration. Retain local configuration at any
other prompt; pause on ambiguity. Review resulting `.dpkg-dist`/`.ucf-dist`
changes for needed follow-up, without automatic merge. [dpkg conffile behavior](https://manpages.ubuntu.com/manpages/noble/man1/dpkg.1.html).

Noble's APT-triggered `needrestart` can restart affected services in interactive
and noninteractive sessions. Package scripts can also restart services. Do not
claim that `DEBIAN_FRONTEND` or list-only environment settings prevent this.
Accept reviewed distribution-service restarts inside this window, with recovery
available; leave needrestart/hooks unchanged and MySQL/Nginx masked. Do not run
this unattended or wrap the package transaction in a kill timeout. [Ubuntu Noble
restart change](https://discourse.ubuntu.com/t/needrestart-changes-in-ubuntu-24-04-service-restarts/44671).

## 4. Verify, Reboot Once, Verify Again

Require upgrade exit0, completed triggers and no unresolved prompt/error before
considering reboot. Repeat the following and section2 masks/listeners/jobs/space:

```bash
dpkg --audit
apt-get check
/usr/sbin/sshd -t
/usr/sbin/sshd -T -C "${SSH_ADMIN_CONTEXT:?}"
/usr/sbin/sshd -T -C "${SSH_ROOT_CONTEXT:?}"
/usr/sbin/sshd -T -C "${SSH_UBUNTU_CONTEXT:?}"
visudo -c
python3 -B /root/wi019/check_layout.py after --nginx-user www-data
runuser -u mysql -- /usr/sbin/mysqld --defaults-file=/etc/mysql/my.cnf --validate-config --log-error-verbosity=2
systemd-analyze cat-config systemd/journald.conf
systemctl show ssh.service ssh.socket systemd-journald.service logrotate.timer -p Id -p ActiveState -p UnitFileState
systemctl --failed --no-pager
```

Use actual connection contexts, never guessed addresses; compare with the stored
baseline privately. Validate the checker hash/real root-owned staged path before
execution. For MySQL keep AppArmor enforced and use the real `/etc/mysql` include
chain, not `/root` candidates. Do not start it or query SQL in this maintenance.
The earlier `--no-login-paths` client option was unsupported; do not reintroduce
it. Read-only native validation is not proof of post-upgrade MySQL runtime.

Require root/password/keyboard-interactive SSH denied, publickey-only, normal
shell/PTY, unchanged socket/listen behavior, atadmin sudo, service identity and
14-directory layout. Check root password remains P, atadmin/atstudio locked,
provider ubuntu unchanged, and the five applied policy files retain their hashes
and modes. Recheck include precedence, not only file hashes. For journal verify
actual effective last values, including `zz-atstudio.conf` after every competing
basename, 256MiB/7d and forwarding off; no claim about all other log retention.
New failed units, changed policy or unexpected public listeners are HOLD.

**STOP D:** Require another new pinned atadmin login/PTY/`sudo -n` proof and usable
user-authenticated console immediately before reboot. Review completed kernel,
initramfs and bootloader package hooks; verify expected kernel/boot files and
available boot space. Keep previous kernel installed. Confirm no active package
job/lock, unresolved audit or scheduled shutdown. Record expected next kernel
and preboot boot ID privately. One approved reboot only, without force:

```bash
systemctl reboot
```

SSH disconnect is not successful reboot evidence. MA observes the recovery
console and attempts bounded new native strict-pinned connections (for example,
10-second connect timeout, 30-second intervals, maximum five minutes). Preserve
all original pin/key options, prohibit multiplex reuse and never accept a changed
host key. At the deadline or a boot error: HOLD, inspect recovery privately; no
second reboot or reinstall automatically.

**STOP E, postboot:** Require changed boot ID, expected kernel, unchanged Noble
release, fresh admin shell/PTY/sudo and recovery login. Wait for any timer-started
package work to finish, then repeat section2 and the verification block above.
Recheck root key login denied and unauthenticated method offer publickey-only as
specified in host-hardening section3; a no-password client's failure alone proves
neither. Compare package versions to the accepted plan and record held/phased
residuals. Require MySQL/Nginx exact masks, inactive/dead and no listeners/start
jobs; app remains absent/inactive, no new external listeners, healthy SSH/socket,
journald/logrotate, time sync and measured resources. No production capacity or
complete vulnerability-remediation claim follows from these checks.

## 5. Failure, Recovery and Closure

| Condition | Bounded response |
|---|---|
| Refresh/simulation/space failure | Do not upgrade/reboot. Restore timer activity, preserve evidence, report cause and required decision. |
| Connection lost during transaction | Use console; determine whether APT/dpkg is still running before any further command. Do not rerun upgrade, kill locks/processes or reboot mid-transaction. |
| Package/configuration failure | HOLD reboot. Record bounded package/error category; separately review exact repair. No automatic `dpkg --configure -a`, fix-broken install or downgrade. |
| Access/config regression | Retain console. Compare exact saved file with current file and new package requirements; review an exact-file restore retaining mode/owner, validate before reload. Never replay WI019's permissive-SSH rollback or restore entire `/etc`. |
| Boot failure | Keep console and retained kernel. Review whether the provider console/bootloader permits selecting that kernel; availability is unverified until observed. No forced reboot, disk rollback or reinstall implied. |
| Service exposure | Stop progression; immediately report exact service/listener and review scoped containment via console. No blanket firewall edits or removal. |

Configuration copies are **not** a package/kernel/disk rollback snapshot and do
not undo maintainer-script/data-format changes. Retaining an old kernel is not a
guarantee that userspace will work with it. A failed recovery requires a new
bounded repair decision, not speculative execution or a paid purchase.

Restore each paused timer to its recorded active/inactive state, preserving its
original enablement; `systemctl start apt-daily.timer` and/or
`systemctl start apt-daily-upgrade.timer` apply only if originally active. Confirm
states and allow any launched package work to finish. Do not stop running package
services. Keep configuration copies and previous kernels; no cleanup deletion.

For success, HOLD or cancellation, close only this window's SSH sessions and log
the user out of the recovery console after final handback. Apply exact-key cleanup
from [first-host-setup section2.5](first-host-setup.md): as the owning Windows user,
`ssh-add.exe -d "$HOME/.ssh/atstudio-iwinv/id_ed25519.pub"`, then privately compare
`ssh-add.exe -l -E sha256` with this window's pre-load set. Require the owned key
absent; exit1/no identities differs from exit2/agent unavailable. Never use `-D`
or remove a pre-existing key. In the elevated operator console restore the
verified Stopped/Disabled baseline with `Stop-Service ssh-agent`,
`Set-Service -Name ssh-agent -StartupType Disabled`, then
`Get-Service ssh-agent | Select-Object Status, StartType`. Stop is not key deletion.
If cleanup fails or the initial baseline differs, report HOLD and reconcile exact
ownership; do not erase others' identities. If console is the sole recovery route
during an incident, hand it over explicitly before closing it.

MA's dated receipt must separate update/reboot/postboot/cleanup statuses, actual
exit codes, package counts/residuals, resource requirements and remaining gates.
Publish safe outcome classes only, never endpoints, fingerprints, key material,
tokens, raw logs or credentials. No live result is established by this runbook.

## Related Documents

- [REQ007](../../../deliverables/user/REQ-20261010-ATS-007.md): Approval and maintenance chain.
- [WI022 handoff](../../../deliverables/agent/WI-20261010-ATS-022-handoff.md): Local-only scope; WI023 review follows.
- [Host hardening](host-hardening.md): Verified-policy checks, including dated corrections.
- [Configuration contract](../CONFIGURATION.md): Unchanged service and storage boundaries.
- Primary source links above were checked on 2026-10-10; native versions/options remain a target preflight obligation.
