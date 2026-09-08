---
version: 1.1
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
**Named TEST-runtime application COMPLETE; requested limited post-restart user UI check accepted by MA.** The [2026-09-09 follow-up](#documentation-follow-up-2026-09-09) supersedes earlier re-login-pending wording using supplied screenshots only. Runtime/HTTP results and the table below preserve the earlier application checkpoint. No new live checks were performed; production SR-93 HOLD remains open. Final documentation checks passed; the MA receipt below records the separate publication boundary.

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
REQ002 is `completed` for the approved TEST-runtime application scope. The requested post-restart check is now accepted at the supplied logged-in/paid-access/download-UI boundary; it does not establish all authentication or financial flows. Old typeless JWTs still have no legacy bypass. This is not production readiness. Retained missing media references were not repaired or deleted; SR-93 production HOLD remains open.
Schedulers retain their existing configuration. MA's zero-pending preflight is not a future no-write guarantee or a scheduler-disable mechanism. Rollback requires admission closure and fresh unfinished-command review, not an automatic restart of the old JAR.
The later npm audit reported 3 moderate package findings associated with one [Vitest advisory](https://github.com/vitest-dev/vitest/security/advisories/GHSA-82fw-gwwq-j7x9). Its unauthenticated path requires mocker/interceptor plugin integration, not observed in the configured React-only plugin list. Record this as development-dependency maintenance; no dependency patch is in this scope. Prior zero findings and the later result retain their distinct check times, without claiming a new advisory publication.
Evidence: `output/test-application-20260909/`, including `historical-preservation.json`; see the [detailed evidence pack](../agent/WI-20260909-ATS-015-evidence-pack.md). RE made no process, provider, DB, secret, commit or push operation during this documentation update.

## Dated Completion Update: 2026-09-09 07:58 KST
MA verified the approved frontend start at `2026-09-09T07:58:35+09:00`, PID 25196, using the same `Start-Process` with `require_escalated`. Backend 10292 and tunnel 1888 were unchanged; the earlier policy denial was resolved through user approval, not a workaround.
All four checks returned 200: `http://127.0.0.1:5173/`, `http://127.0.0.1:8080/api/tracks`, `https://final-expression-heading-header.trycloudflare.com/` and its `/api/tracks` route. MA evidence is in `frontend-start.json` and `final-http.json` under the directory above. RE recorded these supplied results without new probes, audits or runtime actions. MA handles remaining checks and the documentation commit; authenticated acceptance, production HOLD and the three-moderate maintenance record retain their separate boundaries.

## Documentation Follow-up: 2026-09-09

MA confirmed `main=origin/main` at documentation checkpoint `1c467d1` before these edits, distinct from applied product `2d47504`. Prior backend10292, frontend25196 and tunnel1888 remained unchanged afterward per MA; the JAR identity and four HTTP 200 results are prior execution evidence, not fresh checks.

MA accepted the user's three screenshots as the requested check: logged-in UI, DELUXE YEARLY paid access through 2027-09-08 with renewal cancelled, and browser download-complete notification with 19/20 remaining on the site. This is supplied UI evidence only, not a new DB count, downloaded-file inspection, Provider verification, full suite or production acceptance. Prior WI011 three Korean Inbox messages and WI012 reset-link opening remain separate recorded evidence; no new SMTP claims or private screenshots/identifiers were added.

### Inventory And Prior Cleanup

MA's pre-edit inventory at `1c467d1`: **0 tracked modifications, 205 untracked files** = 96 historical deliverables (62 agent + 34 user), 80 later outputs (10 September 8 security + 57 September 9 remediation + 13 September 9 runtime), 27 screenshots, one demo ZIP and one manual SQL patch. This is 125 older files, explicitly preserved/excluded by [WI-20260908-ATS-014](../agent/WI-20260908-ATS-014-evidence-pack.md), plus 80 later outputs; it contains no `src/` or `frontend/` product code.

Named cleanup was completed, not imaginary: `37e8f94` removed legacy code and nine manual SQL files; [WI-20260717-ATS-005](WI-20260717-ATS-005-summary.md) preserved historical REQ/WI/demo material while cleaning only named generated directories. `3147873` later handled portability, and `8161f0a` consolidated source/references and retired seven remote branches, two local branches and the client worktree. MA now found only local/remote `main` (plus symbolic HEAD) and one root worktree. This does not mean every workspace record/output was archived or cleared.

The remaining `scripts/database/patches/WI-20260809-ATS-068-user-consents.sql` is a one-off manual development-DB patch; MA found the table creation already in current schema and no tracked runtime references. No SQL was run. Narrow output ignore patterns explain visible untracked categories, not automatic permission to ignore/delete them. Some are still evidence targets: committed WI-20260817-ATS-018 points to local-only WI-20260817-ATS-014, and WI-20260909-ATS-004 links local-only remediation logs/JSON. Current-main clones lack those targets; local preservation is not remote backup. This is evidence portability debt, not uncommitted application source. Any future archive/delete needs a bounded retention and link-disposition decision, not bulk staging or a new full code audit.

### Delivery Boundary

Only the eight existing handoff-approved documents were patched; exact paths and MA inventory pointers are in the [evidence pack](../agent/WI-20260909-ATS-015-evidence-pack.md#documentation-follow-up-2026-09-09). No new files, Git, runtime, DB, network, artifact cleanup or ignore changes were performed by DocOps. MA's final validator/diff check and unchanged 205-path/hash verification are **pending**, not PASS; exact-path documentation commit/push remains MA-owned. Rollback is documentation-only for this follow-up. WI015 blocks no successor; production HOLD remains.

### MA Final Receipt

The preceding pending checks describe the DocOps handoff and are now superseded: MA's documentation validator passed (704 IDs, Tier 0, links and index), and diff check passed. The existing handoff plus eight document edits are the only nine tracked changes. The in-memory before/after comparison found the same 205 untracked paths and hashes; none was modified, removed or added. No archive or new preservation manifest was created. MA verifies exact-path commit/push after committing this receipt and reports the SHA in the final response. Production approval and artifact-retention decisions remain separate.
