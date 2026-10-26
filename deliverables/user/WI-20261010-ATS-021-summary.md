---
version: 1.0
last_updated: 2026-10-10
project: ATS
owner: docops
category: work-summary
status: stable
dependencies:
  - path: REQ-20261010-ATS-007.md
    reason: Approved continuation and remaining deployment gates
  - path: ../agent/WI-20261010-ATS-021-evidence-pack.md
    reason: Phase attribution and final-receipt boundary
---

# WI-20261010-ATS-021: Host-Hardening Integration

> TL;DR: WI021 complete from MA's20:39KST final receipt; REQ007 active, production HOLD.

## Status and Changes

- Current entry links WI019 correction, WI020 local review and WI021 MA results.
- Journal destination/order correction is source evidence; actual merged target
  policy requires a distinct MA observation.
- MA reports fresh administrator/sudo/PTY, staged/installed SSH context checks,
  post-reload administrator success, root-key denial and publickey-only offer.
- MA reports nonlogin service identity and14-directory native/access checks PASS.
- MA's corrected journal verification passed, usage16.0M; earlier premature
  PASS/8MiB was withdrawn. No seven-day retention/all-copy-isolation proof.
- Corrected MySQL staging/validators and second bounded live-value query passed.
  The first unsupported-option failure remains historical; stop/remask/no queued
  job or DB listener passed after both windows. Nginx remains unchanged.
- Final20:39:14KST sshd/visudo/layout and five applied configuration hashes PASS;
  MySQL/Nginx masked/inactive, only SSH22 and loopbackDNS53 listeners, no failed units.
- Both owned SSH sessions exited0. Exact owned-key removal and empty list preceded
  verified Windows agent Stopped/Disabled at20:39:34KST. MA verified noVNC root logout
  with a fresh Ubuntu tty1 login-prompt screenshot. No reuse of earlier cleanup.
- These final receipts supersede earlier pending notes. SE's final source/client
  corrections received PG hash reconciliation PASS; historical failures/hashes remain.
- REQ007 remains active; production HOLD. Domain purchase/DNS/HTTPS, app
  DB/secrets/start, backup/restore, alerts and load remain separate gates.
- **Before public invitation: approved OS security updates, reboot and revalidation.**
  Cached182 upgradable packages mix updates/security sources, not182 confirmed
  security fixes or a new CVE finding. No OS upgrade/reboot in this continuation.

## Limits

Runtime evidence owner MA, date2026-10-10. SE/PG results are local review/tests;
DOCOPS performs documentation checks only. No full security/capacity claim.
Scoped validation and preserved/corrected-input hashes are in the Evidence Pack.
No remote command, implementation edit, commit or expanded scope by DOCOPS.

## Related Documents

- [Current entry](../../docs/index.md#2026-10-10-host-hardening-continuation)
- [REQ007](REQ-20261010-ATS-007.md)
- [Evidence Pack](../agent/WI-20261010-ATS-021-evidence-pack.md)
