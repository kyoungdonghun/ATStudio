---
version: 1.0
last_updated: 2026-10-10
project: ATS
owner: docops
category: work-summary
status: stable
dependencies:
  - path: REQ-20261010-ATS-005.md
    reason: Approved integration and one requested memory note
  - path: ../agent/WI-20261010-ATS-010-evidence-pack.md
    reason: Integration checks and execution boundaries
  - path: ../../deploy/single-vps/runbooks/resource-baseline.md
    reason: Authoritative selected-versus-current settings
  - path: ../../deploy/single-vps/runbooks/restic-backup-plan.md
    reason: Authoritative backup design and unresolved inputs
---

# WI-20261010-ATS-010: Settings and Restic Integration

## Result

Integrated completed SA WI008 and PG WI009 plans into the
[current decision entry](../../docs/index.md#current-decision-entry), bundle
README, CONFIGURATION and runbook index. Added navigation-only superseding text
to the older backup guide and dated progress to the older deployment REQ.
Historical reports, execution assets and unrelated dirty work are preserved.

## Decision Versus Execution

| Area | Current template/source | Selected initial test design, not applied |
|---|---|---|
| JVM / app and FFmpeg group | Max heap 512MiB; hard cgroup 1536MiB | Max heap 1GiB; hard group 3GiB |
| MySQL / Hikari | Buffer 256MiB; pool max5 | Buffer 512MiB; pool max10 only WITH new pre-body/pre-transaction mutation admission 2 global / 1 per IP and nested-caller tests |
| Media | 512KiB/s for both; shared 4/IP and 16/global | Stream 256KiB/s; originals 2MiB/s, at most 2 global / 1 per IP within retained shared ceiling |
| SMTP | No explicit phase timeout properties | Connect5s/read10s/write10s mappings and phase tests |
| Backup | No implemented automation | Restic; whole helper group 1GiB, cold capture, first full baseline and isolated restore before valuable writes |

Detailed implementation and acceptance criteria belong to the two runbooks,
not this digest. No selected value is a measured capacity or installed setting.
Hikari growth alone does not fix nested transaction starvation. Restic and S3
documentation compatibility do not prove an actual iwinv backup/restore PASS.

Retention is an initial 7 daily / 4 weekly / 3 monthly / 2 prechange selection
proposal plus protected points, not automatic deletion approval. Weekly plus
prechange independent copies imply up to 7 days planned provider-loss RPO,
not 24 hours or an achieved guarantee. Schedule, allowed outage, exact targets,
credentials, external disk, owners and recipients still need execution decisions.

## Risk and Approval Boundaries

The user waived provider inquiry; contact is not mandatory. Unconfirmed transfer,
50Mbps, attack-cost/blocking and hard-cap terms remain risks, not accepted facts.
The dated server 53,020 KRW plus average 50GB storage 1,100 KRW example excludes
external retrieval and other charges. Restic does not fix billed storage size
or make off-provider copies free. No provider terms/prices were refreshed here.

Purchase/OS provisioning needs separate explicit approval, not completion of
every code change. Align templates, admission and validators before applying
new service settings and opening invited traffic. Server-side work needs named-target
authorization; acceptance still includes frontend/API, fresh DB/first ADMIN, full keyring, SMTP, TLS/renewal,
automatic start/reboot, backup/restore, alerts and bounded mixed-load tests.
Existing Gmail/WAV/128kbps/phone TEST evidence remains valid within its bounds.
**Stage B NOT RUN; production HOLD; no purchase/install or public/LIVE approval.**

## Verification and Closeout

Owner DOCOPS; 2026-10-10, Windows/local documentation integration, source main
`58b1011dad796017e47dbcc82a4225ca449bcdf6`; not deployed artifact identity.
Full document validator PASS (750 IDs), focused15-document checks PASS,
git diff --check PASS (existing CRLF notices only), execution assets13/13 SHA-256
unchanged with no added execution paths. Design-only REQ005 is complete;
WI010 blocks no further WI. The older deployment REQ remains open/HOLD.
See the [Evidence Pack](../agent/WI-20261010-ATS-010-evidence-pack.md) for results.
One explicitly authorized memory extension note was added; MEMORY.md and
memory_summary.md were not edited. No build/runtime/DB/remote/cloud/backup
operation, secret access, installation, commit or push was performed.
