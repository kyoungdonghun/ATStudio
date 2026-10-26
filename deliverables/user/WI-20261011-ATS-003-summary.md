---
version: 1.0
last_updated: 2026-10-11
project: ATS
owner: DocOps
category: work-summary
status: stable
dependencies:
  - path: ../agent/WI-20261011-ATS-003-evidence-pack.md
    reason: Dated receipt ledger and bounded integration checks
---

# WI-20261011-ATS-003: Domain and TLS Receipt Integration

> Purpose: Report partial deployment and verified window cleanup separately from unfinished DNS/TLS acceptance.

## Summary

TL;DR: **WI003 reporting integration complete; safe closure verified; REQ010 partial / HOLD_DNS_ISSUANCE. Not TLS-ready.**
The registered apex is confirmed by MA console observation. KRW18700 paid is a
user report, not an audited receipt. Initial Windows preparation and independent
review each passed35 tests. Native Ubuntu Python3.12 returned34 PASS/1 ERROR in
test isolation. The approved fix passed36/36 on SE Windows, independent PG Windows
and MA Ubuntu3.12. Production candidate changes are not approved by that
test-only authorization.

## Actual State and Limits

MA reports protected staging/hash matches and corrected Ubuntu36/36 PASS,0.065s.
At00:24+ KST seven exact packages were installed after signed-index hash and
maintainer/schedule review, no upgrades/removals; Certbot2.9.0 verified. Certbot
service/timer remain runtime-masked/inactive and MySQL masked/inactive. Nginx
bootstrap/drop-in comparison, metadata/no-ACL, native parse and effective-unit
checks passed; Nginx temporarily ran on IPv4 port80 only. Loopback synthetic/root/API/POST
returned200/404/404/403. MA subsequently applied explicitly approved provider
HTTP80 all-IPv4/HTTPS443 management-only rules, preserving SSH and verifying a
fresh login. External synthetic HTTP200/exact body, denied paths404, POST403 and
unknown-Host closed connection were observed; HTTPS acceptance is not claimed.

DNS zone/apex A were saved at00:24:44/00:25:52. At00:31 both authorities give
matching authoritative SOA/A and no AAAA/CAA data; primary REFUSED has resolved.
Both still return apex NS NODATA. A direct parent query confirms the expected
referral and DS NODATA; earlier recursive NS is not parent evidence. CA remains
HOLD pending DNS resolution or explicit later variation approval. The user
approved LE v1.8 no-email staging/production/renewal terms but requested DNS resolution/explanation
instead of approving that variation. PG's conditional advice does not replace
the missing authorization. No CA account or certificate exists; the provider's
internal cause is unknown and no provider inquiry was sent. No guessed DNSSEC/NS
changes. Final00:35:40 DNS still showed matching A and apex NS NODATA on both
authorities; provider generation/sync is only an inference, not a proven cause.

## Verified Window Closure

MA00:36:25 receipt: remove only newly added inbound80/443; console save and a
separate authenticated tab confirm SSH-only inbound; existing egress unchanged.
Nginx is inactive, disabled before persistent mask, no jobs/80/443 listeners.
MySQL is inactive/persistently masked. Certbot service/timer are inactive with
runtime-only masks and no timer enablement link. These runtime masks are not
claimed persistent across reboot; no reboot test occurred.

MA00:38:00 receipt: fresh pinned SSH/sudo PASS, recovery root tty1 count0 after
UI exit/login prompt, no80/443/8080/3306/33060/33062 listeners. Only the window's
SSH key was removed; empty-agent exit1 preceded fresh Windows Stopped/Disabled
CIM verification. No execution is running. PG approved this bounded closure.
DNS/A, seven packages, configs/stages/public tokens remain; no deletion, reboot,
app/DB start or secret access. Private screenshot evidence stays outside tracked files.

No CA account, issuance, renewal dry-run, timer enablement or expiry alerts.
Renewal is NOT_OPERATIONAL. DNS resolution precedes continuation under the
user's decision; HTTPS, app/DB/SMTP/backup/load and production readiness remain
unaccepted. Prior HTTP evidence describes the now-closed temporary window.

## Changes and Next Gate

DocOps changes only the current index checkpoint, REQ010 progress and these two
WI003 deliverables. Historical checkpoints and the REQ opening snapshot remain.
Final four-file document checks passed at00:40 KST with0 errors/warnings:
metadata/dependencies, internal links,47 supported traceability IDs and whitespace.
Historical index/REQ preservation checks passed. No native operation or descendant by DocOps.
Completing partial-result documentation does not close REQ010 or approve production.

## Related Documents

- [REQ010](REQ-20261010-ATS-010.md): Approval and current progress.
- [Evidence Pack](../agent/WI-20261011-ATS-003-evidence-pack.md): Receipt ledger and verification boundaries.
- [Current entry](../../docs/index.md#current-decision-entry): Latest bounded checkpoint.
