---
version: 1.1
last_updated: 2026-09-08
project: ATS
owner: docops
category: reference
status: stable
dependencies:
  - path: WI-20260908-ATS-011-handoff.md
    reason: Approved scope and exclusive document ownership
  - path: ../user/REQ-20260908-ATS-002.md
    reason: Three-message SMTP approval
---

# Evidence Pack: WI-20260908-ATS-011

## Current recipient evidence (WI012 follow-up)
- Source: the user's WI012 delegation reports three supplied smartphone images showing three actual receipts with readable Korean templates and Inbox labels. DocOps records that supplied observation; the image files were not attached to this delegated context and were not independently inspected here.
- The two `[AT.M] 구독 결제 안내` messages are grouped under the same subject and carry TEST1/3 and TEST2/3. The separate `[AT.M] 결제 점검 이슈` message carries TEST3/3. Two subject groups therefore represent three messages, not two receipts.
- Actual recipient receipt, Korean readability and Inbox placement are confirmed for these three WI011 messages only. This is not a future deliverability guarantee or evidence of running-backend SMTP restoration. No receipt-observation timestamp was supplied beyond the 2026-09-08 follow-up date.
- The original send-time status and validation history below are retained. Their pending-recipient wording describes the earlier checkpoint and is superseded only for these three receipts.

## Summary (historical send-time status)
MA reports one Gmail SMTP batch: accepted 3, failed 0, unknown 0; process exit 0. Recipient receipt/placement is pending; running-backend SMTP is not verified. DocOps records supplied evidence, not independent runtime verification.

## Scope / DoD Check
- [x] Record exact tested JAR identity and actual EmailService template use from MA evidence.
- [x] Record synthetic inputs, explicit test/no-account-change marker, and absence of DB/provider/bootstrap/scheduler operations.
- [x] Record accepted/failed/unknown counts for exactly three authorized messages; no automatic resend.
- [x] Complete documentation validation after MA results; preserve histories and production gates OPEN.

All timestamps below are on 2026-09-08; each result is `SMTP_ACCEPTED`.

| Synthetic scenario / marker | Subject | UTC / KST |
|---|---|---|
| Renewal failure / TEST1/3 | [AT.M] 구독 결제 안내 | 12:39:44.549399500 / 21:39:44 |
| Retry exhaustion / TEST2/3 | [AT.M] 구독 결제 안내 | 12:39:48.892383300 / 21:39:48 |
| Admin payment review / TEST3/3 | [AT.M] 결제 점검 이슈 | 12:39:52.516911700 / 21:39:52 |

All bodies explicitly stated test/no actual subscription change. Inputs were synthetic, not actual failed renewals. Receipt and Inbox/Spam placement require separate user confirmation.

## Reference Documents (Tier 0-2)
- Injected governing excerpts: STD-001 sections 1/1.1/1.2/2; STD-004 section 1.1; STD-002 section 1.1; STD-005 sections 1-2; security policy sections 1-3.
- Canonical paths and additional Tier 1-2 pointers: [handoff](WI-20260908-ATS-011-handoff.md), INPUT POINTERS. Additional role/source/history files were not reloaded; handoff pointers do not imply independent verification.
- Assignee: docops; documentation-only. Tag and routing checks were supplied by the handoff. Applied skill: `.agents/skills/create-wi-evidence-pack/SKILL.md`.

## Evidence Pointers
- Owned changes: this evidence pack, [user summary](../user/WI-20260908-ATS-011-summary.md), and [REQ002](../user/REQ-20260908-ATS-002.md) WI011 current status only; previous histories retained.
- Tested runtime JAR SHA-256: `5AE38AC932388E24223A723DDF3FC9BD2DB2B017B3ABC6F91496BCE1BC7F37C2`; extracted actual `EmailService.class` SHA-256: `541336977C6115DE35881506D20D32D1CC47DAEF6176C70ED6D7817604818AE5` (MA-supplied identities).
- MA probe directory: `C:/Users/jm991/AppData/Local/ATStudio/remote-development-20260908/korean-mail-probe-20260908-213331/`; evidence files: `KoreanMailProbe.java`, `smtp-receipt.log`, `send-attempt-started.marker`. DocOps did not inspect external configuration or rerun the probe.

## Commands & Outputs / Tests
- MA: Java 17 source mode, unpacked `BOOT-INF/classes` plus 101 libraries; actual `EmailService` with `CaptureSender` and null repositories. All three recipient/subject/test-marker/UTF-8 MIME checks passed before SMTP. `sender.deliver` called real `JavaMailSenderImpl.send`; capture-phase EmailService INFO SUCCESS is not SMTP proof. Only `SMTP_ACCEPTED` receipt lines support acceptance.
- Preparation history: piped JShell attempt never executed (no marker/log); initial Java attempt stopped at PREPARE with zero SMTP attempts because root `application-local.yml` selected localhost:1025. After settings discovery, only one three-message SMTP batch ran; no actual message was resent.
- MA imported only `MAIL_HOST/PORT/USERNAME/PASSWORD/FROM` from existing external `acceptance-backend-environment.json` into the short-lived sender. Credential values were neither logged nor copied into artifacts; the recipient was supplied as an argument, not stored in Java or documents.
- MA reports no Spring context/bootstrap, DB, provider, scheduler, restart, product or Git actions; background web servers were unchanged. DocOps performed only local document reads/edits; no SMTP, network, runtime, Git or nested agents.
- DocOps ran `python .agents/skills/validate-docs/scripts/validate_docs.py` exactly once after recording MA results: PASS, exit 0; Tier 0, internal links, 678 traceability IDs and document index passed. The bundled validator was repository-wide/read-only; only these two documents received validation-result/status updates afterward. No product tests were rerun.

## Risks / Rollback
- Backend SMTP blocker: MA reports the prior 21:00 restart used root local settings without importing external Gmail settings; later parent mail variables were absent. The earlier "preserved Gmail" claim is not established for that restart; history is retained, not endorsed. Standalone sender success does not verify running-backend SMTP. Configuration/restart requires later approval outside this WI.
- SMTP acceptance cannot prove receipt, Inbox placement, scheduler/payment behavior or production readiness. SR-93 and production gates remain OPEN.
- Recipient address, SMTP credentials and configuration contents are excluded. Rollback is limited to approved documentation changes; no runtime rollback is required or performed.

## Follow-ups (historical send-time status)
Await recipient confirmation without resending. Backend SMTP configuration/restart remains outside this approval; no new work is started. `Blocks: -`.

Current follow-up: recipient confirmation above is recorded without any resend. Separately approved [WI012](WI-20260908-ATS-012-evidence-pack.md) now records backend SMTP restoration and one website-request send; that new message's recipient/link confirmation remains pending. WI011 receipt alone does not establish WI012 runtime results.
