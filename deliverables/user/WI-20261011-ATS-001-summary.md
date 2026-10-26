---
version: 1.0
last_updated: 2026-10-11
project: ATS
owner: SE
category: work-summary
status: stable
dependencies:
  - path: REQ-20261010-ATS-010.md
    reason: Approved quarantine TLS scope
  - path: ../agent/WI-20261011-ATS-001-evidence-pack.md
    reason: File identities, reproduction and verification limits
---

# WI-20261011-ATS-001 Summary

> Purpose: Hand off focused local TLS preparation for independent PG review.

## Result

TL;DR: **Local preparation complete; 35/35 synthetic tests PASS. Native TLS and
production remain HOLD.** Seven implementation files plus two deliverables,
only in the assigned ownership paths; no existing app/proxy artifact changed.

- Renderer accepts only `atmusic.co.kr` and one canonical public IPv4/32.
- IPv4 HTTP serves only isolated ACME tokens. HTTPS serves only a synthetic
  marker after source ACL checks. No app/API/storage proxy or shared webroot.
- Unknown Host/SNI, query/encoded path aliases and unsupported methods fail
  closed. No early success return bypasses the marker ACL.
- Guarded Certbot deploy hook checks exact event/lineage, root ownership and
  permissions, link containment/generation, exact SAN, trust/expiry and Nginx
  certificate/key/config validation before a bounded reload request.
- A new Nginx service drop-in selects only the isolated configuration, leaving
  the existing full app proxy and distro configuration intact.

## Verification and Limits

SE, Windows/PowerShell/Python3.14.3, 2026-10-11:
`python -B -m unittest discover -s scripts/deployment -p test_tls_quarantine.py -v`
passed35, failed0, skipped0. Linux metadata/subprocess cases are fixtures; local
template checks do not execute Nginx phases. No native nginx/bash/openssl was
available on PATH. No installation or substitute runtime was attempted.

No remote connection, browser, private data, ACME registration, DNS/firewall/service
operation, descendant, commit or push by SE. MA's current host/recovery/package/DNS
observations are attributed and separately dated in the runbook.

## Remaining MA Gates

1. Immediately issue/delegate WI-20261011-ATS-002 to PG for exact-byte review and
   independent tests; do not apply before PASS.
2. Fresh pinned host/source/recovery, preserved masks/app absence, no-overwrite
   staging/backups, package source/version/script review and guarded install.
3. Verify native Certbot2.9 help and effective Nginx drop-in/config, then DNS and
   provider rules: SSH preserved,80 public IPv4,443 exact approved IPv4/32, noAAAA.
4. Synthetic challenge, action-time CA/contact consent, intentional staging account
   registration and CA staging proof, then production issue. One explicit approval
   can cover staging/production/renewal. A no-contact path requires explicit LE
   agreement v1.8 and no-contact-sharing approval before reviewing its exact
   noninteractive flags; no CA consent exists at this handoff.
5. Native certificate/key/TLS and allowed/denied/Host/SNI/path tests, renewal dry-run
   and independent hook/served-certificate evidence; review one timer owner.
6. Record continuing quarantine/boot intent or restore stopped/masked state;
   clean up only the window-owned agent key and restore Stopped/Disabled. WI003
   integrates actual MA receipts after WI002, not local assertions.

Independent expiry/failure alerts are not implemented; Let's Encrypt expiry
emails are discontinued. Contact consent is not monitoring. App/DB/SMTP/backup/
load/production acceptance remain outside this work. On failure preserve evidence,
close only the new80/443 provider rules, stop/remask Nginx, retain certificates and
data, and escalate instead of deleting or loosening controls.

## Related Documents

- [Operator runbook](../../deploy/single-vps/runbooks/domain-tls.md): Exact stages and HOLD recovery.
- [Evidence Pack](../agent/WI-20261011-ATS-001-evidence-pack.md): Complete paths, hashes and tests.

## 2026-10-11 Test Isolation Correction

TL;DR: **Approved test-only correction complete; Windows36/36 PASS; Ubuntu3.12
retest and independent PG rereview PENDING. All native application remains HOLD.**
This addendum supersedes the earlier guard-isolation assurance, not the historical
Windows35 execution record. MA reported an exact-hash Ubuntu transfer/test failure:
the intended lineage guard was not reached because an unmocked path check raised
FileNotFoundError; Windows had passed on an incidental GateError instead.

After explicit user approval, only the test file and additive WI001 reports
changed. Every non-target guard is mocked; the target must be reached with exact
arguments and raise exactly GUARD; native calls and real filesystem access must
not occur. A separate mocked missing-filesystem regression confirms early refusal.
Renderer, templates, service drop-in, deploy hook and runbook hashes are unchanged.

New test SHA-256:
`a9ca1406ef209a9eb67406dbf14c889ca85d5c5eab01274933b1cd78bcf56436`.
SE Windows/Python3.14.3:36 passed,0 failed,0 skipped,0.296s using the same focused
unittest command above. No SE remote operation. MA/PG must review the correction,
retain the historical candidate, transfer only this new test file into a fresh
root-stage suffix with the same implementation, and obtain Ubuntu3.12 PASS before
continuing. Existing CA consent and all target-operation gates remain unchanged.
