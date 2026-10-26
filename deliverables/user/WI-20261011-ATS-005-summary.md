---
version: 1.0
last_updated: 2026-10-11
project: ATS
owner: DocOps
category: work-summary
status: stable
dependencies:
  - path: ../agent/WI-20261011-ATS-005-evidence-pack.md
    reason: Resume approval, receipt boundaries and scoped verification
---

# WI-20261011-ATS-005: Restricted TLS Resume Ledger

> Purpose: Report approved restricted TLS, renewal configuration and final access cleanup without closing unresolved DNS.

## Summary

TL;DR: **Restricted TLS/renewal configuration PASS; final SSH/root cleanup verified. REQ010 partial: NS unresolved, no expiry alerts or first unattended-run/reboot proof.**
The user explicitly permits recording the NS problem as unresolved and trying
restricted HTTPS, without app/DB exposure, and requires advance notice for extra
costs or user-performed actions. This supersedes the earlier issuance HOLD only
as authorization. WI003's earlier closure and all historical evidence remain.

## Resume Observations

MA's pre-resume DNS observations: expected parent delegation and correct apex A
on both authorities, but apex NS AA NOERROR/NODATA, SOA serial2026101101, also
observed by Google/Cloudflare DoH. Cause is unknown; no input correction is proven.
Earlier target REFUSED resolved; a resolver PTR refusal is a separate query.
Certificate success must not be presented as NS repair or app/DB release.

Temporary Windows agent activation is separately approved. At handoff it had
freshly been Stopped/Disabled, passphrase entry had not occurred and no remote
change was reported. Subsequent MA01:00:47 preflight confirms pinned SSH/sudo,
same boot, root recovery and one expected key in Running/Manual agent. MySQL/Nginx
and Certbot remain masked/inactive, app absent, no relevant jobs/processes/listeners.
Retained-source/render/metadata/native-parse checks passed; native DNS at01:00:58
still reports the NS anomaly. Current ACME directory terms matchv1.8 per MA.
At01:04:56 MA then rechecked bootstrap/identity/effective unit and started only
Nginx on IPv4 port80, not enabled. Explicitly approved80/public-ACME and
443/management-only rules were saved/read back with SSH/egress preserved and
fresh SSH PASS. Ten external Windows HTTP probes through actual DNS passed.
WI004 passed the exact no-email staging procedure. MA01:08:21 staging
registration/private metadata and01:08:45 actual CA dry-run returned exit0.
Subsequent MA01:09:08 production registration and01:09:26 issuance exited0:
exact single-apex generation1 lineage, protected metadata, trust/hostname/key-pair
checks PASS. Certificate issuer YE2; valid2026-10-10T15:10:54Z through
2027-01-08T15:10:53Z. Exact TLS render installed/reloaded at01:09:49; IPv4-only
80/443, app/DB off. Nine external TLS probes passed, including real-DNS trusted
marker200 and negative paths/method/Host. External HTTP/TLS total19 probes.
At01:10:43 spoofed-header loopback403 and no/unknown-SNI denial passed; exact
reviewed hook/manual reload PASS. These do not resolve the NS anomaly.
Existing LE v1.8 no-email staging/production/renewal consent is retained, with
action-time agreement/scope checks. No extra product purchase; normal server
network use remains governed by the existing contract.

## Acceptance Observations

MA01:13:12 renewal dry-run exited0 with all simulated renewals and explicit hook
success, no HOLD marker. Trusted native SslStream then confirmed the exact
production certificate hash and marker200,20 external probes total. The observed
120.0514-second delay was normal, not failure. This does not prove future renewal.
The later user-operated photo shows the exact HTTPS marker URL, LTE indicator,
ERR_CONNECTION_ABORTED and no marker (photo clock1:14). MA's fresh allowed-line
trusted curl returned200/exact marker with provider/Nginx unchanged. This is
bounded independent failure, not proof of sole firewall causality or no VPN.
The earlier closure proposal was superseded before any rule/service change;
only the firewall editor was opened and cancelled without submitting. The image
and personal path remain outside tracked documentation.

## Final State

MA01:15:48 fresh trusted allowed-line200/exact marker corroborated the user LTE
failure after that photo; no precise drop-layer/VPN-absence guarantee. The closure
proposal was NEVER applied, not rolled back after execution.

MA01:16:14 enabled the known single Certbot timer and Nginx. Timer active/waiting,
next2026-10-11 20:59:44 KST, LastTrigger empty. Service inactive/static success0
is not a scheduled-run receipt. Package cron is a no-op on systemd; no extra root
Certbot cron. Renewal status: **SCHEDULE_ENABLED_DRY_RUN_PASS**. First unattended
run, future changed certificate, actual reboot and expiry alerts remain unproven.

MA01:16:47 final pinned SSH/sudo and Nginx/sshd syntax PASS; Nginx/timer enabled/
active, MySQL persistent mask/inactive, app absent, no Java/FFmpeg/CA processes,
pending jobs, app/DB ports or IPv6 web listener. Root tty1=0/recovery login prompt.
At01:16:52.899 exact window key removed, empty agent exit1 verified, hidden
cleanup exit0 and fresh CIM Stopped/Disabled; all MA execution sessions ended.
Provider public-ACME80 and management-only443/SSH remain, egress unchanged.
No resumed-window purchase/install/reboot, app/DB start/publication, commit or push.

NS last checked01:14:09 remains AA NODATA on both authorities, same serial;
global IPv6 count0 then. Browser preview was client-policy blocked, not a TLS
trust failure: native proof exists, browser-proof claim does not. Alerts absent.
WI005 reporting is complete; REQ010 is not fully complete or production-ready.
DocOps changed only four assigned documentation files, with no native/browser/CA
action or descendant. Scoped document checks are recorded in the Evidence Pack.

## Related Documents

- [REQ010](REQ-20261010-ATS-010.md): Latest approval, plan and preserved history.
- [Evidence Pack](../agent/WI-20261011-ATS-005-evidence-pack.md): Final receipt ledger and validation boundaries.
- [Current entry](../../docs/index.md#current-decision-entry): Superseding authorization checkpoint.
