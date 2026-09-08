---
version: 1.0
last_updated: 2026-09-09
project: ATS
owner: re
category: work-summary
status: active
dependencies:
  - path: ../agent/WI-20260909-ATS-015-evidence-pack.md
    reason: Verification, execution provenance and remaining boundaries
  - path: REQ-20260909-ATS-002.md
    reason: Approved application scope
---

# WI-20260909-ATS-015: Partial Test-Server Application

## Current Status
**PARTIAL, not complete.** The product was committed/pushed and the new backend responds locally. The frontend start tool was rejected with `blocked-by-policy`; no replacement frontend process launched. Public frontend remains unavailable despite the retained tunnel. The user has been asked to change approval mode; no workaround was attempted.

| Item | Result |
|---|---|
| Product commit / push | MA reports complete; local `main` and `origin/main` both `2d475044ea0a364a277d06fa647f8925032891b2` |
| Selected scope | 130 paths: 64 tracked changes, 9 new tests, 43 September 9 WI/REQ documents, 14 required September 8 audit documents |
| Verified source inputs | 620 backend + 334 frontend inputs matched; bootJar UP-TO-DATE success |
| Backend | PID 10292 started 07:39:24 KST; Boot 4.0.8, MySQL 8.0.45/JPA with 43 tables validated; local tracks API 200 |
| Frontend / public application | Old frontend PID 16160 stopped after ownership verification; replacement blocked by tool policy; public smoke pending |
| Preservation | MA reports 78 media files, 2 config files and 149 historical hashes preserved; old launcher/JAR and September 8 audit bytes retained |

## Prepared and Applied
The new launcher is `C:/Users/jm991/AppData/Local/ATStudio/remote-development-20260908/start-backend-remediation.ps1`. RE verified zero parser errors and an identical 100-token existing argument sequence without launching it. MA subsequently passed `CheckOnly` and applied the separately named `ATStudio-20260909-2d47504.jar`; its exact SHA256 and full path are in the evidence pack.
MA paused public admissions, verified process ownership, and recorded zero unresolved orders, unfinished storage mutations and other InnoDB transactions before replacing backend PID 24792. Cloudflare PID 1888 remained. These are supplied execution results, not independent runtime actions by RE.
The eight authorized handoff corrections removed only EOF blank lines; body hashes were unchanged. The earlier unstaged diff check did not cover these then-untracked files; the later staged check exposed their EOF errors. MA owned re-staging/commit. This report is a subsequent documentation update, not a claim that it was included in the product commit.

## Remaining Work and Boundaries
REQ002 remains `in_progress` pending frontend execution policy/approval resolution, frontend restart and local/public smoke. No deployment-complete or production-ready claim is made. Old JWT sessions require re-login. Retained missing media references were not repaired or deleted; SR-93 production HOLD remains open.
Schedulers retain their existing configuration. MA's zero-pending preflight is not a future no-write guarantee or a scheduler-disable mechanism. Rollback requires admission closure and fresh unfinished-command review, not an automatic restart of the old JAR.
The later npm audit reported 3 moderate package findings associated with one [Vitest advisory](https://github.com/vitest-dev/vitest/security/advisories/GHSA-82fw-gwwq-j7x9). Its unauthenticated path requires mocker/interceptor plugin integration, not observed in the configured React-only plugin list. Record this as development-dependency maintenance; no dependency patch is in this scope. Prior zero findings and the later result retain their distinct check times, without claiming a new advisory publication.
Evidence: `output/test-application-20260909/`, including `historical-preservation.json`; see the [detailed evidence pack](../agent/WI-20260909-ATS-015-evidence-pack.md). RE made no process, provider, DB, secret, commit or push operation during this documentation update.
