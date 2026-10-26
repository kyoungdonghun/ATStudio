---
version: 1.0
last_updated: 2026-10-10
project: ATS
owner: SE
category: work-summary
status: stable
dependencies:
  - path: REQ-20261010-ATS-007.md
    reason: Approved bounded first-host preparation
  - path: ../agent/WI-20261010-ATS-016-evidence-pack.md
    reason: Local verification and attributed remote-state boundaries
---

# WI-20261010-ATS-016 Summary

## Outcome

**Bounded local preparation complete; ready for WI017 PG review.** Added the
[first-host runbook](../../deploy/single-vps/runbooks/first-host-setup.md).
No new installer, SSH library or credential helper was necessary.

- Independent VNC public host key/fingerprint, dedicated public pin, native
  Windows SSH with strict verification and user-only passphrase entry.
- Missing/incorrect fingerprint, malformed target or wrong public pin fails closed.
- Temporary ssh-agent activation is approved but NOT EXECUTED. MA waits for
  independently verified post-rebuild identity. Planned cleanup removes only
  the newly loaded owned key, verifies absence and restores Stopped/Disabled.
- Survey precedes package changes. Java17/Nginx/FFmpeg commands are a reviewed
  candidate plan, not execution. Nginx remains masked; no default site is served.
- MySQL installation is separately held because package-managed initialization
  and automatic start must be reconciled with the DB/schema/user exclusion.
- Existing MySQL/journald/provider settings are preserved; SSH/firewall changes,
  domain purchase/DNS/TLS, app/DB/secret operations remain separate gates.

## Verification

2026-10-10, SE, local Windows: seven PowerShell blocks parse; seven malformed-input
cases and ten synthetic public-pin cases pass. Native `ssh -G` accepts direct-key
and agent-mode options with strict host checking retained, using synthetic paths
and a documentation-only address. No connection or private-key read occurred.
Seven existing input/template hashes remained unchanged. MA updated REQ007
concurrently; SE did not edit it. No product build/test suite or Linux test ran.

## Current Remote Boundary

MA reports the user explicitly approved no-data, irreversible Ubuntu24.04 rebuild
of the same existing instance at displayed0KRW. MA clicked once at 19:42 KST;
the provider confirmed the request and showed rebuilding. This is attributed
live progress, not SE execution. **Old VNC/fingerprint evidence is invalidated.**
Rebuild completion, new notice delivery, recovery authentication, independent new
host pin, SSH login and package installation are unverified/NOT RUN here.
Earlier TCP/provider-rule observations do not prove the post-rebuild state.

## Next Gate

MA must immediately create/delegate **WI-20261010-ATS-017** through the handoff
skill for independent PG review, then continue WI018 integration. Do not wait for
remote login to start local review or close REQ007 from this preparation receipt.
PG should review the public-pin guard, single-key agent cleanup, Nginx start
inhibition and MySQL initialization gate. DOCOPS owns later index integration.

## Related Documents

- [WI016 Evidence Pack](../agent/WI-20261010-ATS-016-evidence-pack.md)
- [WI016 Handoff](../agent/WI-20261010-ATS-016-handoff.md)
- [First-host runbook](../../deploy/single-vps/runbooks/first-host-setup.md)
