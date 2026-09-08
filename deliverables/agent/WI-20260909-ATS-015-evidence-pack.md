---
version: 1.0
last_updated: 2026-09-09
project: ATS
owner: re
category: evidence-pack
status: confirmed
dependencies:
  - path: WI-20260909-ATS-015-handoff.md
    reason: Approved delegation and execution ownership
  - path: ../user/REQ-20260909-ATS-002.md
    reason: Approved scope and partial application status
---

# Evidence Pack: WI-20260909-ATS-015

## Summary / Scope
**Named TEST-runtime application COMPLETE.** MA subsequently started the frontend after the user changed approval mode and approved startup; local/public frontend and tracks API checks all returned 200. The earlier `blocked-by-policy` denial is a historical checkpoint, not a current blocker. Re-login and authenticated acceptance remain pending; this is not production acceptance.
RE prepared only the external launcher, this evidence/summary, explicitly approved EOF corrections, and the authorized REQ status addition. MA owns Git, DB preflight and all runtime execution. Runtime outcomes below are MA-supplied evidence, not actions executed by RE.

## Reference Documents (Tier 0-2)
- Tier 0: `docs/standards/core-principles.md`, `development-standards.md`, `documentation-standards.md`, `glossary.md` in the same directory; Korean conversation/REQ, English documentation, preservation and exact evidence boundaries.
- Tier 1: `docs/policies/security-policy.md`; no secrets or raw DB payloads in deliverables.
- Tier 2: [payment rollout](../../docs/design/payment-operations-runbook.md#source-bound-command-rollout-2026-09-09), [runtime/storage](../../docs/design/runtime-storage-operations.md), [WI014 summary](../user/WI-20260909-ATS-014-summary.md). Injection source: WI015 packet and user-supplied context; role `re`; no additional agents.

## Evidence Pointers / Results
- Launcher: `C:/Users/jm991/AppData/Local/ATStudio/remote-development-20260908/start-backend-remediation.ps1`. Explicit `JarPath`, `JarSha256`, `PublicOrigin`; same runtime/storage/config paths and existing arguments; hidden start, separate logs, ownership/environment/port guards. `RuntimeSafetyReviewed` is an acknowledgement, not a scheduler-disable flag.
- Product commit: `2d475044ea0a364a277d06fa647f8925032891b2`; MA reports push complete. RE independently read matching local `main` and `origin/main` refs. Scope: 130 paths = 64 tracked changes + 9 new tests + 43 September 9 WI/REQ documents + 14 linked September 8 audit documents (WI015-018 handoffs/evidence/summaries, WI018 findings and REQ004); no unrelated August 17 artifacts.
- Tested inputs: MA confirmed 620 backend and 334 frontend inputs identical; `bootJar` succeeded UP-TO-DATE. `npm ci --ignore-scripts` succeeded. No source or dependency patch was added by WI015.
- Applied JAR: `C:/Users/jm991/AppData/Local/ATStudio/remote-development-20260908/ATStudio-20260909-2d47504.jar`; SHA256 `8117DF561E633706C6DA78BDE39BA3B5155982B9AA604BE1413EBB0FE404D7A0`. Old launcher/JAR preserved.
- MA verified ownership and stopped frontend PID 16160 to pause public admissions, then backend PID 24792. Final read-only preflight: unresolved orders 0, unfinished storage mutations 0, other InnoDB transactions 0; tuple remains `localhost:3306/atstudio` with repository `uploads` and `private-uploads` roots.
- Launcher `CheckOnly` passed for MA; new backend PID 10292 started at `2026-09-09T07:39:24.9000536+09:00`. MA reports Spring Boot 4.0.8/JPA initialization on MySQL 8.0.45, 43 tables validated, and `GET http://127.0.0.1:8080/api/tracks` returned 200. Cloudflare PID 1888 remained; it does not establish frontend availability.
- Local evidence directory: `output/test-application-20260909/`. `artifact.json`, `backend-start.json`, `commit-paths.json`, and `preservation-after.json` were read by RE with safe field selection; preservation reports 78 media files, 2 config files, 0 issues. MA additionally reports all 149 historical hashes preserved in `historical-preservation.json`; original September 8 audit bytes unchanged. DB evidence was not reproduced as raw payloads.

## Commands / Tests / DoD
- [x] RE used PowerShell 7.6.5 `Parser::ParseFile`: 0 parse errors; all 100 existing argument tokens identical; one hidden `Start-Process`; existing launcher hash unchanged. RE never invoked either launcher, including `CheckOnly`.
- [x] Approved handoffs `WI-20260909-ATS-006` through `012`, plus `014`: `apply_patch` removed only the surplus final blank line. All eight content hashes excluding EOF matched; LF counts changed 44 to 43; targeted working-tree `git diff --check` passed. MA owned re-staging and commit.
- Earlier unstaged diff checking did not inspect those then-untracked handoffs. MA's subsequent 130-file staged check found their eight EOF failures. These are distinct checks; the earlier result is not represented as full staged coverage.
- [x] Readiness was reported before execution; supplied execution/preservation evidence is recorded with provenance. [x] Frontend restart and local/public smoke; [x] named TEST-runtime application. [ ] Re-login/authenticated acceptance; [ ] production acceptance. No additional runtime commands were executed by RE during this documentation update.

## Scheduler / Maintenance / Rollback
Existing cron jobs remain at 00:00/00:10/00:30/01:00/01:15 in Asia/Seoul. MA reported no cron catch-up on startup at the 07:33 preflight. Storage recovery runs at startup and periodically; zero pending preflight entries is point-in-time evidence, not scheduler disablement or a future no-write guarantee. Preserve one writer, audit-on-startup=true, strict-on-startup=false and the existing SMTP connection check.
The later MA npm audit reported 3 moderate package findings (`@vitest/mocker`, `vitest`, `@vitest/coverage-v8`) from one advisory, [GHSA-82fw-gwwq-j7x9](https://github.com/vitest-dev/vitest/security/advisories/GHSA-82fw-gwwq-j7x9). The official advisory ties unauthenticated exploitation to public mocker/interceptor plugin integration. Current `frontend/vite.config.ts` uses `vitest/config` defineConfig and `plugins: [react()]`; MA found no mockerPlugin/interceptorPlugin configuration. This is configuration evidence, not an exploitation test or universal safety claim. Record development-dependency maintenance without patching this scope. Earlier zero findings and this later result are check-time observations; no new-publication claim is made.
REQ002 is `completed` for the approved TEST-runtime application scope; re-login/authenticated acceptance remains pending and SR-93 production HOLD remains open. No money/provider/mail/data/DDL operation was delegated to RE. Rollback requires admission closure, single-writer control and fresh unfinished-command disposition; never blindly restart the old JAR after new monetary activity. WI015 blocks no successor; MA owns remaining checks and documentation commit.

## Dated Completion Update: 2026-09-09 07:58 KST
MA's CUA smoke: reloading public home redirected the old typeless session to `/login`; the AT.M login form/button rendered without an app crash. No login was submitted; authenticated acceptance remains the user's next step.
MA reports that the user changed approval mode and approved startup. The same frontend `Start-Process` succeeded with `require_escalated`: PID 25196 at `2026-09-09T07:58:35+09:00`; backend PID 10292 and tunnel PID 1888 remained unchanged. No bypass of the earlier denial was used.
MA verified HTTP 200 for `http://127.0.0.1:5173/`, `http://127.0.0.1:8080/api/tracks`, `https://final-expression-heading-header.trycloudflare.com/` and its `/api/tracks` route. Evidence: `output/test-application-20260909/frontend-start.json` and `final-http.json`. These are MA-supplied actual results; RE performed no additional probes or audits in this update.
Product commit `2d47504` and documentation checkpoint `2a905da` were already pushed. The earlier product-ref equality and frontend-denial records above describe their original checkpoints. This update changes documentation only: no source changes, restarts, funds or DB operations by RE. The existing three-moderate audit record remains unchanged; HTTP smoke does not close authenticated or production acceptance.
