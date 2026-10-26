---
version: 1.0
last_updated: 2026-10-10
project: ATS
owner: PG
category: summary
status: complete
dependencies:
  - path: REQ-20261010-ATS-004.md
    reason: Approved read-only purchase due diligence
  - path: ../agent/WI-20261010-ATS-005-evidence-pack.md
    reason: Source locations and verification limits
---

# WI-20261010-ATS-005: Security and Recovery Audit

**TL;DR:** No inspected security/source constraint requires a separate paid DB
server, load balancer or security appliance before buying the proposed VPS.
This is conditional support for provisioning, not a purchase approval or a
production-security PASS. The current templates pass their offline validator;
they are not installed services, working mail, automated backups or a restored
system. Provider terms and capacity are owned by MA/WI004/WI006.

## Verification Boundary

- Inspected by PG on 2026-10-10, Windows local checkout, `main` at
  `58b1011dad796017e47dbcc82a4225ca449bcdf6`; pre-existing dirty work preserved.
- Fresh read-only template audit: PASS; `deployment_ready=false`, production
  HOLD. No native Linux, DB, remote SSH, mail, backup, restore or load run.
- Gmail receipt/link checks from 2026-09-08 and WAV/128kbps MP3/phone checks
  from 2026-09-09 remain credited with their original TEST limits.
- WI003 confirms saved public key `atstudio-admin-20261010` and unattached
  firewall `atstudio-bootstrap`. MA's later draft selection is not server
  creation, enforcement or SSH login. PG did not repeat browser/key inspection.

## Before Purchase

1. Accept the single-host failure model and operator responsibilities: an OS,
   disk or account incident can interrupt the app, DB and local media together.
   More RAM does not supply recovery or high availability.
2. Agree backup destinations, encryption recovery owner, retention byte budget,
   acceptable downtime/lost work and a recovery-console path. The existing cold
   capture proposal stops the application; it is not transparent continuous
   backup. An off-provider copy can be independently controlled storage, not
   necessarily another paid server.
3. MA must close or explicitly escalate provider traffic/attack billing,
   blocking, recovery-console and SMTP restrictions. This audit supplies no
   provider billing guarantee. Nginx limits do not impose a monetary ceiling.

These are decision/ownership gates. Most implementation and verification below
properly happens after provisioning; do not classify every unrun target test as
a reason to buy more infrastructure.

## After Purchase, Before Invitation

| Gate | Concrete current fact | Required completion |
|---|---|---|
| Network and TLS | Bootstrap ingress is SSH only; HTTPS/HTTP are not allowed. Nginx starts deny-all for application clients. | Approve DNS/certificate path, narrow ACME exposure and tester admission; prove renewal, intended host, outside-source rejection and no public DB/backend listeners. |
| Mail and account access | Bootstrap egress excludes SMTP 587/465. The example disables password auth and leaves mail credentials empty. Existing Gmail success is TEST evidence. | Select exact SMTP host/port/TLS mode, obtain permitted egress, set external credentials and canonical mail origin, enable the chosen account flow and verify a selected recipient. Changing only MAIL_PORT to 465 does not configure implicit TLS. |
| Fresh DB and first ADMIN | Current fresh MySQL manifest is UNRECORDED. Seed contains six subscription plans, no administrator; normal signup creates USER. Production forbids QA bootstrap. | Approve exact empty DB/schema/seed proof, least-privilege runtime account and one-time first-ADMIN provisioning. Do not copy or delete retained TEST data to get past the gate. |
| Mandatory secrets and storage | Production requires explicit roots and strict integrity. Startup validates the billing encryption ring even without Toss keys. | Provision independent JWT/billing secrets outside Git; retain complete historical ring for future restores; verify matched DB/public/private roots and permissions. No extra paid encryption service is implied. |
| External and local side effects | Global TCP 443 egress is open in the bootstrap policy. Notification=false is not scheduler-off. Renewal/expiry/recovery jobs remain active. | Do not call bootstrap firewall an isolated restore network. Fresh data only for first deployment; restore must use default-deny egress and reviewed local-write containment before startup, with one writer/encoder owner. |
| Recoverability | Backup runbook exists, but capture/encryption/upload/scheduling and actual restore are NOT RUN. | Implement an approved complete DB + both storage roots + release/settings/key backup, failure alert and retrieval/isolated restore. Reserve space for simultaneous staging/archive copies; test recovery without the original PC/provider account. |
| Boot and restart | systemd supplies restart/stop and resource controls, but only orders after MySQL; three starts in five minutes can exhaust automatic retries. | Verify MySQL enablement/readiness, mounts, one JVM/FFmpeg child ownership, host/SSH recovery, reboots and incompatible-release rollback. Do not infer recovery from the existence of a unit file. |
| Abuse and observation | Nginx has request/concurrency/rate candidates. Public full-length playback and licensed re-download remain available; per-day download quota is not a byte budget. | Apply and measure the candidate limits, provider/interface counters, disk/inode headroom and an external alert/stop procedure. Preserve protection for originals and private files; no storage aliases. |
| SMTP failure resilience | `EmailService` calls `mailSender.send` synchronously; inspected public settings have no SMTP connect/read/write timeout values. | Before enabling real mail, define finite target timeouts and test a controlled failure. This is an unconfigured bound, not evidence that previous delivery failed; private runtime overrides were not read. |

## Before General Public or LIVE Operation

- Accept measured mixed-load and shared-IP behavior, not the template's client
  counts as a performance promise; tune or expand only with measured cause.
- Keep public traffic cost response, backup age, disk, service and TLS alerts
  operational; verify the selected recipient can receive outage alerts when
  the VPS itself is down. Safe access logs alone cannot notify anyone.
- Independently approve canonical OAuth callbacks, mail delivery/authentication
  and any LIVE payment activation. Keep unused integrations disabled.
- Validate IPv4/IPv6 exposure, forged client headers, encoded protected paths,
  200/206/416 playback, entitled original downloads, actual effective log
  privacy and fresh-host mail; no blanket production PASS is issued here.

## Deliverables and Next Step

Only this summary and the [Evidence Pack](../agent/WI-20261010-ATS-005-evidence-pack.md)
are authored by WI005. No code, templates, secrets or cloud configuration changed.
WI005 is complete as an audit and blocks **WI-20261010-ATS-007**: MA combines
this result with WI004, WI006 and its provider findings, then delegates the
integrated decision report. REQ and deployment are not closed by this report.

## Related Documents

- [Approved REQ](REQ-20261010-ATS-004.md)
- [Handoff](../agent/WI-20261010-ATS-005-handoff.md)
- [Single-VPS bundle](../../deploy/single-vps/README.md)
- [Current decision entry](../../docs/index.md#current-decision-entry)
