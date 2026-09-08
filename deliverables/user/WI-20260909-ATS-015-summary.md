---
version: 1.0
last_updated: 2026-09-09
project: ATS
owner: re
category: work-summary
status: confirmed
dependencies:
  - path: ../agent/WI-20260909-ATS-015-evidence-pack.md
    reason: Verification, execution provenance and remaining boundaries
  - path: REQ-20260909-ATS-002.md
    reason: Approved application scope
---

# WI-20260909-ATS-015: Test-Runtime Application Complete

## Current Status
**Named TEST-runtime application COMPLETE.** After the user changed approval mode and approved startup, MA successfully started the frontend; local/public frontend and tracks API checks all returned 200. The earlier `blocked-by-policy` denial is a historical checkpoint, not a current blocker. Re-login/authenticated acceptance remains pending; production SR-93 HOLD remains open.

| Item | Result |
|---|---|
| Product commit / push | Product `2d475044ea0a364a277d06fa647f8925032891b2` and documentation checkpoint `2a905da` already pushed, per MA |
| Selected scope | 130 paths: 64 tracked changes, 9 new tests, 43 September 9 WI/REQ documents, 14 required September 8 audit documents |
| Verified source inputs | 620 backend + 334 frontend inputs matched; bootJar UP-TO-DATE success |
| Backend | PID 10292 started 07:39:24 KST; Boot 4.0.8, MySQL 8.0.45/JPA with 43 tables validated; local tracks API 200 |
| Frontend / public application | New frontend PID 25196 started 07:58:35 KST with approved escalation; local/public frontend and tracks API checks all 200 |
| Preservation | MA reports 78 media files, 2 config files and 149 historical hashes preserved; old launcher/JAR and September 8 audit bytes retained |

## Prepared and Applied
The new launcher is `C:/Users/jm991/AppData/Local/ATStudio/remote-development-20260908/start-backend-remediation.ps1`. RE verified zero parser errors and an identical 100-token existing argument sequence without launching it. MA subsequently passed `CheckOnly` and applied the separately named `ATStudio-20260909-2d47504.jar`; its exact SHA256 and full path are in the evidence pack.
MA paused public admissions, verified process ownership, and recorded zero unresolved orders, unfinished storage mutations and other InnoDB transactions before replacing backend PID 24792. Cloudflare PID 1888 remained. These are supplied execution results, not independent runtime actions by RE.
The eight authorized handoff corrections removed only EOF blank lines; body hashes were unchanged. The earlier unstaged diff check did not cover these then-untracked files; the later staged check exposed their EOF errors. MA owned re-staging/commit. This report is a subsequent documentation update, not a claim that it was included in the product commit.

## Remaining Work and Boundaries
REQ002 is `completed` for the approved TEST-runtime application scope. Old JWT sessions require re-login and authenticated acceptance remains pending. This is not production readiness. Retained missing media references were not repaired or deleted; SR-93 production HOLD remains open.
Schedulers retain their existing configuration. MA's zero-pending preflight is not a future no-write guarantee or a scheduler-disable mechanism. Rollback requires admission closure and fresh unfinished-command review, not an automatic restart of the old JAR.
The later npm audit reported 3 moderate package findings associated with one [Vitest advisory](https://github.com/vitest-dev/vitest/security/advisories/GHSA-82fw-gwwq-j7x9). Its unauthenticated path requires mocker/interceptor plugin integration, not observed in the configured React-only plugin list. Record this as development-dependency maintenance; no dependency patch is in this scope. Prior zero findings and the later result retain their distinct check times, without claiming a new advisory publication.
Evidence: `output/test-application-20260909/`, including `historical-preservation.json`; see the [detailed evidence pack](../agent/WI-20260909-ATS-015-evidence-pack.md). RE made no process, provider, DB, secret, commit or push operation during this documentation update.

## Dated Completion Update: 2026-09-09 07:58 KST
MA verified the approved frontend start at `2026-09-09T07:58:35+09:00`, PID 25196, using the same `Start-Process` with `require_escalated`. Backend 10292 and tunnel 1888 were unchanged; the earlier policy denial was resolved through user approval, not a workaround.
All four checks returned 200: `http://127.0.0.1:5173/`, `http://127.0.0.1:8080/api/tracks`, `https://final-expression-heading-header.trycloudflare.com/` and its `/api/tracks` route. MA evidence is in `frontend-start.json` and `final-http.json` under the directory above. RE recorded these supplied results without new probes, audits or runtime actions. MA handles remaining checks and the documentation commit; authenticated acceptance, production HOLD and the three-moderate maintenance record retain their separate boundaries.
