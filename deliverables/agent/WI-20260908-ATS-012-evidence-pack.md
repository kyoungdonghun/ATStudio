---
version: 1.0
last_updated: 2026-09-08
project: ATS
owner: docops
category: evidence-pack
status: stable
dependencies:
  - path: WI-20260908-ATS-012-handoff.md
    reason: Approved scope and document ownership
  - path: ../user/REQ-20260908-ATS-002.md
    reason: WI012 backend restoration and one website email approval
  - path: WI-20260908-ATS-011-evidence-pack.md
    reason: Separate standalone-send and recipient evidence
---

# Evidence Pack: WI-20260908-ATS-012

## Summary
Backend Gmail settings were restored and one real website-triggered email completed SMTP delivery. The user subsequently confirmed receipt and opening of its password-reset page with "열렸어. 다음은 뭐야?". This is user-reported link observation, not a submitted password change or proof of future deliverability. The three WI011 Korean messages were confirmed by user screenshots. DocOps prepared the shell; after stopping the waiting agent, MA finalized its own execution evidence. This is not production GO.

## Scope / DoD Check
- [x] Record supplied WI011 recipient observations without resending or claiming future deliverability.
- [x] Prepare the two deliverable sets within exclusive document ownership.
- [x] Record MA timestamps, explicit external SMTP import and effective configuration precedence.
- [x] Record backend process ownership and unchanged JAR, DB, storage, public origin, frontend and Tunnel evidence.
- [x] Record exactly one authorized website forgot-password submission, separating HTTP/UI response from actual backend delivery log.
- [x] Record recipient receipt/link observation separately when supplied; no password reset was submitted in this verification.
- [x] Validate documentation once after MA results and check the owned-document diff before finishing.

## Reference Documents (Tier 0-2)

| Tier | Reference | Context actually available |
|---|---|---|
| 0 | `docs/standards/core-principles.md` (STD-001) | User-injected excerpt: manifest, language, approval and scope rules; not a full read |
| 0 | `docs/standards/documentation-standards.md` (STD-004) | User-injected excerpt: required metadata and document status; not a full read |
| 0 | `docs/standards/development-standards.md` (STD-002) | User-injected excerpt: persona, Tier 0 check and traceability; not a full read |
| 0 | `docs/standards/glossary.md` (STD-005) | User-injected excerpt: canonical terms and supplied domain entries; not a full read |
| 1 | `docs/policies/security-policy.md` | First 150 lines read; only sections 1-3 and 6.2 needed for this record |
| 1 | `.claude/agents/docops.md` | Role read; documentation-only ownership applies |
| 2 | [REQ002](../user/REQ-20260908-ATS-002.md), [WI011 evidence](WI-20260908-ATS-011-evidence-pack.md), [WI011 summary](../user/WI-20260908-ATS-011-summary.md) | WI011 receipt and WI012 approval/status inspected; earlier unrelated REQ history retained |
| 2 | `src/main/java/com/atstudio/atstudio/service/EmailService.java`, `src/main/java/com/atstudio/atstudio/controller/AuthController.java` | Handoff pointers only; source not independently re-verified |

Injection/routing: docops, documentation; the handoff reports MA checked project/routing JSON. `.claude/config/context-injection-rules.json` and `.claude/config/workspace.json` were not independently loaded. Applied skills: `.agents/skills/create-wi-evidence-pack/SKILL.md` and `.agents/skills/validate-docs/SKILL.md`; the existing generated handoff satisfies the Evidence Pack prerequisite.

## Evidence Pointers
- Owned files: this pack, [WI012 user summary](../user/WI-20260908-ATS-012-summary.md), WI011 evidence/summary recipient status, and REQ002 WI011 receipt/WI012 current status only.
- WI011 receipt source: the user's WI012 delegation reports three smartphone images, readable Korean templates and Inbox labels. The grouped subscription-subject messages carry TEST1/3 and TEST2/3; the admin-subject message carries TEST3/3. Images were not attached to this delegated context, so this is supplied observation, not DocOps visual inspection. No precise receipt-observation time was supplied.
- WI011 historical SMTP acceptance remains 2026-09-08 KST 21:39:44/48/52, accepted 3 / failed 0 / unknown 0, as previously supplied by MA. Those timestamps are not WI012 execution timestamps.

### MA Execution Record (2026-09-08 KST)

| Evidence required | Current record / boundary |
|---|---|
| External SMTP import and precedence | Existing external JSON mail keys only, mapped to child-process `SPRING_MAIL_HOST/PORT/USERNAME/PASSWORD` and `APP_MAIL_FROM`; canonical Spring environment properties override literal local-file values. Gmail port 587; STARTTLS/auth/hostname verification required; `spring.mail.test-connection=true`. No credentials printed, copied into artifacts or source file edits |
| Backend ownership and restart | Exact Java path/JAR/port ownership checked for PID24016, stopped only that backend. PID24792 started 21:55:09, application ready 21:55:33.607 in 23.232 seconds with `ddl-auto=validate` |
| Preserved runtime surfaces | Same JAR SHA-256 `5AE38AC932388E24223A723DDF3FC9BD2DB2B017B3ABC6F91496BCE1BC7F37C2`; same explicit root config, DB and public/private storage paths. Frontend16160 and Tunnel1888 retain 21:00 start times; public origin unchanged |
| Single website submission | CUA visited public `/password-reset`, filled approved test recipient and clicked the send button once. UI displayed `요청 접수 완료`; no address/token stored in this record |
| Actual backend mail outcome | Prior delivery count 0 at 21:56:36; at 21:56:54.194 PID24792 HTTP thread `0.1-8080-exec-9` logged EmailService `outcome=SUCCESS`, deliveryId `790b448f-1b52-4721-b8a9-b2a79e25cb80`. Subject `[AT.M] 비밀번호 재설정 안내`. This is not a standalone SMTP probe |
| Recipient confirmation | User reported "열렸어. 다음은 뭐야?" after being asked to open the actual website email link. Receipt and opening of the reset page are confirmed by that report. WI012 Inbox/Spam placement, a fresh screenshot and final password-change submission were not separately verified |
| Excluded side effects | No financial/provider/manual scheduler actions, reset-password submission, manual DB changes, product edits, frontend/Tunnel restart, commit or push. Normal forgot-password token replacement only; password unchanged |

## Commands & Outputs
- DocOps: local handoff/skill, owned-document and relevant policy reads only; initial owned-document contents retained in tool-session memory for a non-Git diff. No secret configuration inspection, network/SMTP request, runtime action, Git action or nested agent.
- Runtime directory: `C:/Users/jm991/AppData/Local/ATStudio/remote-development-20260908/`. Backend logs: `backend-gmail-20260908-215509.out.log` and `.err.log`.
- Reusable pinned development helper: `start-backend-gmail.ps1 -PublicOrigin <current-HTTPS-origin>`. Use `-CheckOnly` first. This helper refuses an occupied port and never stops processes automatically; verify backend ownership before a separately approved restart. It reads the existing mail JSON by reference and does not change the base/local files. It is not a production deployment launcher.
- MA checks: PowerShell AST and preflight PASS; invalid HTTP-origin guard PASS; occupied-port guard PASS without a second launch. Local backend API, frontend, public home and public API returned 200. Public-origin CORS returned the exact requested public origin.
- Storage audit: 30 references checked, 10 missing, identical before (21:00:37) and after (21:55:33) restart. Existing historical-record condition retained without repairs; not a new SMTP regression.
- MA ran `python .agents/skills/validate-docs/scripts/validate_docs.py`: PASS, exit 0, 679 traceability IDs, Tier 0, internal links and index passed. `git diff --check` passed. No product tests needed or claimed for this runtime-only change.

## Tests
MA performed the runtime/browser checks above and documentation validation passed. No independent DocOps runtime test, full financial regression or production test is claimed.

## Risks / Rollback
- WI011 recipient receipt does not verify WI012 backend configuration. An application success screen does not independently establish SMTP success, actual receipt, usable reset link or future Inbox placement.
- Recipient addresses, credentials, authentication tokens and complete reset URLs are excluded from all records. Normal forgot-password token replacement is within the approved API flow; no password-reset submission or manual data repair is authorized here.
- Keep all historical failures, send-time pending checkpoints and production/SR-93 gates OPEN. This work does not repeat financial acceptance or authorize production GO.
- Runtime rollback, if MA determines it necessary, is a backend-only configuration re-launch within authorization, preserving JAR/DB/storage/frontend/Tunnel; never DB rollback. No rollback is performed by DocOps. Any documentation correction must retain prior evidence and unrelated edits.

## Follow-ups
Mail verification is closed within the tested scope, without resend or password change. The user next approved scoped commit/push and stage 2 baseline/branch/document assessment; that is separate from this runtime test and from production GO. `Depends On: WI-20260908-ATS-011`; `Blocks: -`. MA took over record finalization after stopping the waiting DocOps agent; original ownership and preparation history above remain traceable.
