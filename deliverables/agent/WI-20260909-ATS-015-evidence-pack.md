---
version: 1.1
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
**Named TEST-runtime application COMPLETE; requested limited post-restart user UI check accepted by MA.** The [2026-09-09 documentation follow-up](#documentation-follow-up-2026-09-09) supersedes earlier re-login-pending statements with supplied screenshot evidence only. Prior runtime/HTTP results below remain dated execution evidence; no new live checks were made in this follow-up. Production SR-93 remains HOLD. The MA final receipt below closes documentation validation and unchanged-file checks; Git publication is verified after the containing commit.
RE prepared only the external launcher, this evidence/summary, explicitly approved EOF corrections, and the authorized REQ status addition. MA owns Git, DB preflight and all runtime execution. Runtime outcomes below are MA-supplied evidence, not actions executed by RE.

## Reference Documents (Tier 0-2)
- Tier 0: `docs/standards/core-principles.md`, `development-standards.md`, `documentation-standards.md`, `glossary.md` in the same directory; Korean conversation/REQ, English documentation, preservation and exact evidence boundaries.
- Tier 1: `docs/policies/security-policy.md`; no secrets or raw DB payloads in deliverables.
- Tier 2: [payment rollout](../../docs/design/payment-operations-runbook.md#source-bound-command-rollout-2026-09-09), [runtime/storage](../../docs/design/runtime-storage-operations.md), [WI014 summary](../user/WI-20260909-ATS-014-summary.md). Injection source: WI015 packet and user-supplied context; role `re`; no additional agents.
- Documentation follow-up injection: the same Tier 0 rules plus Tier 1 [archive policy](../../docs/policies/archive-policy.md), Tier 2 `.agents/skills/create-wi-evidence-pack/SKILL.md` and `.agents/skills/validate-docs/SKILL.md`, and the last section of the WI015 handoff. Role `docops`; explicit REQ002 approval covers the eight existing documents only, not cleanup. Original runtime ownership remains `re`/MA.

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

## Documentation Follow-up: 2026-09-09

### Scope / DoD Check
- [x] Record MA-supplied user UI observations separately from prior runtime results; supersede current pending/not-deployed wording with dated pointers, preserving historical sections and counts.
- [x] Record the supplied inventory/cleanup review without modifying its artifacts or treating a question about cleanup as authorization.
- [x] MA final documentation validator and diff check passed; see final receipt below.
- [x] MA compared the in-memory untracked path/hash inventory with no additions, missing files or changed contents. Exact-path Git publication is verified after the containing commit; no self-referential commit identifier is claimed.

### Evidence Pointers / Results
- Before these edits, MA confirmed `main=origin/main` at documentation checkpoint `1c467d1`; product `2d47504` was already pushed and applied to TEST. Prior backend PID10292 / `ATStudio-20260909-2d47504.jar` / SHA256 `8117DF561E633706C6DA78BDE39BA3B5155982B9AA604BE1413EBB0FE404D7A0`, frontend25196 and tunnel1888 were unchanged afterward per MA. The full artifact path and four prior HTTP 200 results remain in the original execution record and `output/test-application-20260909/final-http.json`; none was rechecked by DocOps.
- MA accepted three user-supplied screenshots as the requested post-restart check: logged-in UI, DELUXE YEARLY paid access through 2027-09-08 despite cancelled renewal, and browser download-complete notification with 19/20 remaining in the site UI. This is supplied UI evidence, not independent image inspection, a traced login submission, new DB quota/history/License counts, downloaded-file contents/hash inspection or Provider verification. Official Download UI is distinct from public listening; paid access is distinct from renewal eligibility.
- Prior WI011 Korean three-message Inbox confirmation and WI012 website reset-mail receipt/reset-page opening remain within their recorded boundaries in the [payment acceptance record](../../docs/payment/acceptance-test-checklist.md#2026-09-09-post-restart-ui-check). No new SMTP, password-change, scheduler or future-deliverability claim. No user/card/email identifiers or private screenshots are embedded.
- Exact edited documents: `docs/index.md`; `docs/SR/SR-93.md`; `docs/payment/index.md`; `docs/payment/acceptance-test-checklist.md`; `docs/payment/known-limits-and-next-steps.md`; `deliverables/user/REQ-20260909-ATS-002.md`; `deliverables/user/WI-20260909-ATS-015-summary.md`; this evidence pack. No new files.

### MA Inventory And Cleanup Findings

MA's pre-edit inspection at `1c467d1` found **0 tracked modifications and exactly 205 untracked files**. This is a supplied point-in-time Git inventory, not a new DocOps scan or a remotely stored preservation manifest.

| Category | Files | Supplied breakdown |
|---|---:|---|
| Historical deliverables | 96 | Agent 62 + user 34; filename dates August 9: 9, August 16: 7, August 17: 80 |
| Generated verification/application outputs | 80 | September 8 security 10 + September 9 remediation 57 + September 9 runtime 13 |
| UI screenshots | 27 | Preserved locally; no private content reproduced |
| Historical demo ZIP | 1 | Preserved locally |
| Manual SQL | 1 | `scripts/database/patches/WI-20260809-ATS-068-user-consents.sql` |

- Total: 125 older files + 80 later verification/application outputs. [WI-20260908-ATS-014 evidence](WI-20260908-ATS-014-evidence-pack.md), Final Receipt, explicitly preserved the 125 untracked files and excluded them from its commit. Source/reference consolidation at `8161f0a` retired seven old remote branches, two local branches and the client worktree. MA's latest branch/worktree/remote-head inspection found only local `main`, `origin/main` plus symbolic HEAD, one root worktree, and one remote branch `main` at `1c467d1`.
- Earlier cleanup was real but bounded: `37e8f94` (2026-07-17) removed named legacy APIs/services/DTOs, download queue/play history and nine manual SQL files while aligning the baseline; `3147873` (2026-07-24) performed portability cleanup. [WI-20260717-ATS-005 summary](../user/WI-20260717-ATS-005-summary.md) explicitly preserved historical REQ/WI records and the demo ZIP while cleaning named generated directories. Neither result means all workspace records/outputs were archived or cleared.
- No `src/` or `frontend/` product code is in this 205-file inventory. The sole code-like item is the manual SQL above: its header describes a one-off existing-development-DB patch, not application startup. MA found its `user_consents` creation already in current `schema.sql` and no tracked runtime references to the exact basename/folder in `src`, `build.gradle`, `scripts` or `.github`. No SQL was executed; this is not a new code fix or an auto-run script claim. Narrow `.gitignore` output patterns do not cover every output/log category, so later artifacts appear untracked.
- Retention caveat: committed [WI-20260817-ATS-018 summary](../user/WI-20260817-ATS-018-summary.md), line 14, depends on local-only `WI-20260817-ATS-014-summary.md`; committed [WI-20260909-ATS-004 summary](../user/WI-20260909-ATS-004-summary.md), line 77 onward, links local-only `output/release-remediation-20260909/*.log`/`*.json`. Some historical artifacts still support evidence pointers. A current-main clone lacks those targets; local preservation is not remote backup. This is documentation/evidence portability debt, not uncommitted application source.
- No bulk staging, ignore change, move, deletion or artifact modification was performed. A separately approved bounded retention/archive decision must address evidence-link disposition or repair before future moves/deletion; do not resolve this by bulk-committing 205 files or starting a new full code audit. MA retains the in-memory path/hash baseline for final unchanged verification.

### Commands / Tests / Risks / Rollback
DocOps used scoped PowerShell `Get-Content`/`rg` reads and `apply_patch` on the eight allowed documents only. No Git, runtime, DB, network, provider/mail, broad validation or new test execution occurred in this follow-up. MA supplied the inventory from `git branch -avv`, `git worktree list`, `git ls-remote --heads origin`, targeted history/reference reads and `git grep`; these were not executed by DocOps. Final checks remain pending as listed above, not PASS.

REQ002's TEST application remains completed and its requested user UI check is now accepted at the limited screenshot boundary. Production SR-93 HOLD, retained missing-media decisions and the prior three-moderate dependency-maintenance record remain open in their own scope. Documentation rollback is an exact-path revert of this follow-up only, preserving unrelated changes; no runtime rollback or artifact cleanup is authorized. WI015 blocks no successor; MA owns final verification and documentation delivery.

### MA Final Document Receipt

- Supersedes DocOps handoff-time pending checks above. `python .agents/skills/validate-docs/scripts/validate_docs.py` exited 0: Tier 0, links, index and 704 traceability IDs passed. `git -c core.safecrlf=false diff --check` exited 0. This validates the current local workspace, not availability of local-only evidence in a fresh remote clone.
- Scope review: the eight assigned existing documents plus the MA-edited existing WI015 handoff, nine tracked documentation paths total; no product source or new files. The in-memory before/after inventory contained the same 205 untracked paths with identical SHA-256 values, no additions/missing/changed entries. This is an observed local comparison, not a newly persisted archive manifest or remote backup.
- MA stages only these nine documents. The final user response reports the containing commit and verified remote match after publication. No archive, delete, move, ignore, SQL, runtime or external-service action was performed for this follow-up. No successor WI is blocked or implicitly authorized.
