---
version: 1.3
last_updated: 2026-10-10
project: ATS
owner: docops
category: guide
status: draft
dependencies:
  - path: ../../deliverables/user/REQ-20261003-ATS-001.md
    reason: Approved purchase-preparation scope and separate target gates
  - path: ../../docs/index.md
    reason: Latest bounded source and TEST evidence
---

# Single-VPS Operations Bundle

> Purpose: Prepare an operator-controlled first deployment without authorizing purchase, remote changes, or public release.

## 2026-10-10 First-Host Navigation

The [first-host checkpoint](../../docs/index.md#2026-10-10-first-host-checkpoint)
and [REQ007](../../deliverables/user/REQ-20261010-ATS-007.md) govern current
access/setup progress. Use the [reviewed first-host runbook](runbooks/first-host-setup.md)
with [SE preparation](../../deliverables/agent/WI-20261010-ATS-016-evidence-pack.md)
and [PG review](../../deliverables/agent/WI-20261010-ATS-017-evidence-pack.md).
[WI018](../../deliverables/agent/WI-20261010-ATS-018-evidence-pack.md) records
MA-owned live evidence dated2026-10-10: the purchased host was rebuilt in place,
recovery and strict-pinned SSH key access passed, and Java17/Nginx/FFmpeg/MySQL8
were installed. Nginx/MySQL remain masked/inactive; distribution MySQL bootstrap
is not ATStudio DB/schema/user creation or application deployment. SE/PG evidence
remains local procedure review. Owned agent-key removal and empty-list verification
passed; MA's `Get-Service ssh-agent` at19:57:21KST confirmed Stopped/Disabled.
The noVNC root console was logged out, with a fresh Ubuntu tty1 login-prompt
screenshot confirmed by MA.
REQ007 remains active for the unmet security/setup gates. Domain purchase remains
pending before DNS/HTTPS, and app/backup/restore/alerts/load acceptance is not done.
Earlier preparation, pre-purchase and Stage B NOT RUN wording below is historical
context, not current first-host status or a new authorization. Production remains HOLD.

## 2026-10-10 Local Preparation

Read the [resource baseline](runbooks/resource-baseline.md) and
[restic backup plan](runbooks/restic-backup-plan.md) with the
[WI015 integration summary](../../deliverables/user/WI-20261010-ATS-015-summary.md).
[REQ006](../../deliverables/user/REQ-20261010-ATS-006.md) supplies local source,
configuration candidates, validator and backup core after the preserved
[REQ005 design checkpoint](../../deliverables/user/WI-20261010-ATS-010-summary.md).
Templates are now updated, not installed. Final
[PG review](../../deliverables/agent/WI-20261010-ATS-014-evidence-pack.md) found
no new blocking defect within local preparation; it is not target acceptance.

The old JAR is unchanged. Build/hash the reviewed source and prove active
pre-body/pre-transaction admission before new environment/Hikari10 activation;
never increase the old JAR's pool alone. All backup host hooks remain
unconfigured, including stop/fence/dump/resume. Default-OFF recurring policy
and an inactive timer are not complete daily unattended backup. Independent
alert transport, actual copy and restore remain unimplemented; see the
[backup interfaces](backup/README.md). Native Linux/restic/S3/services/DB/load
and restore are NOT RUN. No supported-user count or cost cap is established.

The user skipped provider inquiry; earlier inquiry/terms wording is retained as
historical risk context, not a mandatory contact step or confirmed terms. Cost
and traffic uncertainties still require an explicit risk decision before paid
action. The dated 53,020 KRW server plus 1,100 KRW average 50GB backup storage
example excludes external retrieval and other charges; it is not a total cap.

Restic's first full cold capture and isolated recovery rehearsal must precede
valuable writes. Schedule, allowed outage, exact destinations and owners remain
open; no automatic deletion is approved. The selected design is complete;
purchase and OS provisioning remain separate explicit approvals, not gated on
completion of every code change. Templates, admission and validators must align
before applying the new service settings and opening invited traffic. Server-side
deployment/security work follows named-target authorization; frontend/API/DB,
first ADMIN, SMTP, TLS/renewal, full keyring, automatic start, backup/restore,
alerts and load remain target acceptance gates. **Stage B NOT RUN; production
HOLD.** Historical evidence below is preserved, not relabeled as new-host success.

## 1. Status and Ownership

**Local preparation implemented; Stage B NOT RUN; production HOLD.** This bundle
is not an installer, working host backup service, or capacity guarantee. The approved topology is one
Linux VPS with Nginx, React static build, one Java 17 application, MySQL 8 and
one FFmpeg conversion worker. WAV originals and full-length 128kbps MP3
derivatives remain on local storage; Object Storage is for external backups.
No CDN, separate database, additional application writer, or storage migration
is selected here.

| Asset | Owner / role | Entry |
|---|---|---|
| Admission and source bindings | WI-20261010-ATS-011 / se | [Source receipt](../../deliverables/agent/WI-20261010-ATS-011-evidence-pack.md) |
| Settings and offline validation | WI-20261010-ATS-012 / se | [CONFIGURATION.md](CONFIGURATION.md), [contract](env/contract.json), [validator](../../scripts/deployment/validate_bundle.py) |
| Backup core and default-OFF policy | WI-20261010-ATS-013 / se | [Integration guide](backup/README.md) |
| Independent review and current-entry integration | WI-20261010-ATS-014 / pg; WI-20261010-ATS-015 / docops | [Final review](../../deliverables/agent/WI-20261010-ATS-014-evidence-pack.md), [integration evidence](../../deliverables/agent/WI-20261010-ATS-015-evidence-pack.md) |
| Historical Stage A bundle | WI-20261003-ATS-001 through WI-20261003-ATS-006 | [Dated closeout](../../docs/index.md#2026-10-03-stage-a-closeout); original evidence unchanged |

Missing referenced assets or unresolved placeholders block bundle acceptance;
do not infer implementation from a planned path. Use the delivered validator's
documented interface, not an invented flag. An offline PASS means only its
reported static checks passed, never that Nginx/systemd/MySQL/FFmpeg ran.

## 2. Read and Execute in Order

1. Read the [current decision entry](../../docs/index.md#current-decision-entry)
   and verify the intended release identity. Document dates are not test dates.
2. Complete the decision inventory below and
   [deployment gates](runbooks/deployment.md). Purchase and named-target changes
   require their own approvals; Stage A does not confer them.
3. Approve the [backup and isolated restore plan](runbooks/backup-restore.md),
   including its storage budget, encryption access, ownership and recovery test.
4. On the separately approved target, record
   [acceptance and bounded capacity evidence](runbooks/acceptance-capacity.md).
5. Request the explicit next release decision. Restricted access is a production
   verification stage, not proof that a few acquaintances define full success.
   General public access and LIVE payment/refund remain separately gated.

## 3. Operator Decision Inventory

Keep exact infrastructure addresses, identities and credential locations in a
restricted operator record. Shared evidence uses target labels and redacted
references, not configuration bodies, account addresses or keys.

| Needed now, before purchase | Decision / HOLD boundary |
|---|---|
| Product and cost envelope | Exact provider/product/region, approved monthly total including tax, backup, storage and transfer; no purchase authorized here |
| Transfer terms | Resolve included allowance, prorating/reset date for a midmonth start, ingress versus egress metering, what the advertised 50Mbps condition applies to, overage, attack billing, automatic blocking and enforceable spend limit |
| Access and domain | Canonical HTTPS domain, domain ownership, restricted audience, operator and recovery-console access owner |
| Recovery policy | Backup owner, external and off-provider destinations, portable encryption/recovery-key owner, retention/size cap and acceptable downtime/RPO/RTO |
| Integrations | Keep the existing mail-provider choice; select sender/recipient owners and enabled login/Provider scope without sending anything |
| Escalation | Alert recipient, backup operator, service owner and who can authorize cost or architecture changes |

Provider allowance, midmonth accounting, inbound/50Mbps applicability, attack
billing and automatic block behavior are **UNCONFIRMED**. No price or provider
term is refreshed or accepted by this bundle. Rate limiting is not a billing
cap. If cost exposure cannot be bounded, keep purchase/load/public access HOLD
and ask for an explicit decision; never silently upgrade.

| Needed on the named target, before action | Exact-target operator record |
|---|---|
| Host identity | Provider instance label, private host/address record, OS/version, SSH host fingerprint, recovery-console path, approved SSH sources |
| Release | Commit plus dirty-source disposition, tested JAR hash, frontend build manifest/hash, config/template version; one matched release |
| Runtime tuple | Exact new DB host/port/name, least-privilege account reference, distinct real public/private roots and ownership |
| Permissions | Approved package installation, SSH/firewall/TLS edits, exact remote files/services and rollback, start/stop/reboot window |
| Private settings | Operator-controlled secret source, JWT key, complete billing keyring, selected mail/OAuth/payment credentials; no bulk TEST bundle import |
| Bootstrap | Fresh-schema observation/approval, first-ADMIN method and named principal, intentional catalog content; no demo/test accounts |
| Verification | Allowed test identities/files/recipients, external egress exceptions, finite load duration/bytes and cleanup authorization |

## 4. Preserve Existing Evidence

These are navigation summaries of existing records, not WI002 runtime tests.

| Date / environment / evidence owner | Preserved result | Limit |
|---|---|---|
| 2026-09-08 development TEST; MA and user | [WI011/WI012 Gmail](../../docs/payment/index.md#wi011wi012-mail-and-documentation-closeout-2026-09-08): three Korean messages received/readable in Inbox; website reset mail received and reset page opened | WI012 Inbox placement/password submission not proved; new-host SMTP/origin and selected-recipient sends need confirmation |
| 2026-09-09 retained TEST; MA and qa-integ | [WI028](../../deliverables/agent/WI-20260909-ATS-028-evidence-pack.md): 22 original/128kbps MP3 pairs; bounded batch/retry/seek/resume evidence | Not new-host throughput, strict integrity, fresh schema or restore proof |
| 2026-09-09 actual phone; user and MA | WI028 follow-up: download-save/open-play PASS | Not phone-local SHA/whole-duration or all-device coverage |
| 2026-09-09 morning local staging; MA and independent QA | [Reconstruction handoff](../../scripts/reconstruction/README.md#5-private-recovery-inventory-and-keyring) and source/history delivery completed | Non-atomic pre-WAV staging, not a current encrypted/off-device recovery point or actual restore |

The retained TEST's ten historical missing references and non-strict setting
are not imported into a fresh production database. Do not reopen their repair
as a requirement for this fresh-target plan, or weaken the new target's strict
integrity gate to imitate TEST.

## Related Documents

- [SR-93 production gates](../../docs/SR/SR-93.md#remaining-production-gates).
- [Runtime storage operations](../../docs/design/runtime-storage-operations.md).
- [Security policy](../../docs/policies/security-policy.md).
- [WI002 Evidence Pack](../../deliverables/agent/WI-20261003-ATS-002-evidence-pack.md).
