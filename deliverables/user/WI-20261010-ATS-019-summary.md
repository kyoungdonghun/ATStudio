---
version: 1.0
last_updated: 2026-10-10
project: ATS
owner: SE
category: work-summary
status: stable
dependencies:
  - path: REQ-20261010-ATS-007.md
    reason: Approved bounded continuation
  - path: ../agent/WI-20261010-ATS-019-evidence-pack.md
    reason: Local verification and exact artifact identity
---

# WI019: Host Hardening Preparation

> TL;DR: Local reviewable package is ready for WI020. No remote action was performed by SE.

## Changes

- Added a [staged runbook](../../deploy/single-vps/runbooks/host-hardening.md) for
  atadmin/sudo, independent access acceptance, SSH restriction, service directories,
  bounded MySQL verification and journal policy. Each stage has a stop/rollback gate.
- Added small [SSH](../../scripts/deployment/host-hardening/00-atstudio-admin.conf),
  [sudo](../../scripts/deployment/host-hardening/90-atstudio-admin) and
  [validation-wrapper](../../scripts/deployment/host-hardening/sshd-validation.conf)
  candidates for reviewed, hash-checked upload. No remote configuration assembly.
- Added one [read-only path checker](../../scripts/deployment/host-hardening/check_layout.py).
  It rejects collisions, symlinks, unsafe ancestry/ACLs and incorrect final modes.
- Preserved MySQL/journal/systemd templates and historical receipts. Journal's
  deployment name is99-atstudio.conf because MA found a later vendor syslog.conf
  override; source template bytes are unchanged.

## Verification and Limits

SE verification: 2026-10-10, local Windows/PowerShell, Python3.14.3 and Git Bash.
Focused tests cover synthetic metadata/account failures and fixed candidate
contracts. Bash blocks are parse-checked, not executed. Native Linux validators,
account creation, SSH/sudo acceptance, DB start, journal restart and live directory
permissions are MA-owned target checks, not SE results. Exact test counts and
artifact hashes are recorded in the Evidence Pack.

MA's fresh host survey is attributed input, not independent SE observation.
The known-good key/session does not prove a new admin session. Recovery noVNC
authentication remained pending in the latest supplied update; a login screen
does not satisfy that gate. Existing ubuntu and root's local password are preserved.
MA subsequently reported that the user cannot recall the rotated console
password; separate native user-only reset approval is pending. Root SSH remains
intact. This blocks remote SSH restriction only, not local preparation completion;
SE neither resets nor handles the password.

## Ready / Held

| Stage | State |
|---|---|
| Local runbook, candidates, focused tests | Ready for independent WI020 review |
| Admin creation | Held for WI020, fresh collision checks and MA execution |
| Root/password SSH restriction | Held for WI020, new admin key/sudo/PTY acceptance and authenticated recovery console |
| Directory/MySQL/journal application | Held for WI020 and stage-specific target checks |
| App/Nginx/firewall/SQL users or schema/secrets/purchases | Excluded; production HOLD |

## Risks and Next Step

NOPASSWD is intentionally root-equivalent for the approved key. SSH mistakes can
lock out the operator; journal retention may age records irreversibly. The checker
does not remove concurrent-write races or prove bind-mount safety. MySQL normal
startup writes distribution state even though the only client SQL is read-only;
the probe ends stopped/masked. Journal limits do not bound MySQL/rsyslog files or
prove every collector absent. No capacity, cost ceiling or application readiness
claim is made.

MA must immediately create/delegate WI020 to PG using the required handoff skill,
then apply only reviewed stages and feed dated receipts to WI021. SE has no WI020
ownership and did not create another chat, run remote commands or close REQ007.

## Related Documents

- [WI019 handoff](../agent/WI-20261010-ATS-019-handoff.md)
- [Evidence Pack](../agent/WI-20261010-ATS-019-evidence-pack.md)

## 2026-10-10 Correction and Recovery Addendum

The earlier99-atstudio.conf ordering claim above is incorrect and superseded:
99-atstudio.conf sorts before vendor syslog.conf. The current runbook uses
zz-atstudio.conf and requires actual merged basename ordering across all four
systemd configuration directories before application. Templates remain unchanged;
the Evidence Pack retains prior hashes and appends corrected artifact hashes.

MA reports that the user privately completed the console password reset and
noVNC root prompt/`who` tty1 were verified at20:23 KST. Earlier recovery HOLD
wording remains historical, not current. SE did not handle/reset the password.
PG review and new admin/sudo/PTY plus other target gates remain required.
