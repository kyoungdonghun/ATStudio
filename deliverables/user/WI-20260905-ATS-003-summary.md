---
version: 1.2
last_updated: 2026-09-05
project: ATS
owner: docops
category: work-summary
status: confirmed
dependencies:
  - path: REQ-20260905-ATS-001.md
    reason: Approved V1 closeout scope
  - path: ../agent/WI-20260905-ATS-003-evidence-pack.md
    reason: Detailed evidence, current source pointers and MA check commands
---

# WI-20260905-ATS-003 Summary

## Result

TL;DR: **WI-003 documentation work is complete.** The five approved documents
now distinguish initial evidence from MA's final passing tests, rebuilt local
backend, read-only checks and desktop/mobile browser results. On 2026-09-05,
MA reported document validation PASS (665 IDs, links and index) and
`git diff --check` PASS; [WI-002 evidence](../agent/WI-20260905-ATS-002-evidence-pack.md)
is complete. Only scoped staging and commit remain MA-owned for this local
closeout.
**SR-93 stays OPEN; no production GO.** Docops made no source/test edits,
runtime operations, Git writes or client-worktree changes during this resume.

## Historical Evidence Matrix

| Area | Supported dated evidence | Not established by that record alone |
|---|---|---|
| Database manifest | 2026-08-17 WI-016 records real guarded disposable Create/Validate/Hibernate/Drop and 43/511/175/91/6/6/0/0 manifest. Current source still records that contract. | Current/deployed DB state, retained-data migration, backup or restore |
| Provisioning/proxy | WI-023 records isolated acceptance DB/bundle and local/public HTTP 200; WI-030 records isolated boot/API/proxy. | Production provisioning/deployment, current client health or September strict-storage behavior |
| Local browser | WI-032 preserves MA-supplied local playback/admin/download observations and disposable CORS correction. WI-030 stays partial and WI-031 interrupted. | Current reload/history/next-track behavior, deployed CORS, provider response; download-event capture timed out |
| Helpers/log drainage | WI-033 and WI-034 record synthetic lifecycle/ownership/environment/log-drain PASS; WI-034 full source checks are dated 2026-08-18. | Real scheduled jobs, log monitoring/retention, alert receipt and incident response |
| Backup/restore/notification/deploy | Documentation and open checklists exist. No actual recovery, external delivery or production-deployment proof was found in the targeted record. | Operator/target-specific execution and final GO |
| Storage restart | 2026-09-02 WI-004 records development 30 checked/10 missing, stream 206/500. | Current counts and production integrity; throwaway records are not to be repaired by this WI |

Exact source locations and evidence limitations are in the
[evidence pack](../agent/WI-20260905-ATS-003-evidence-pack.md).

## Current MA Results (2026-09-05)

Runtime: main development `codex/v1-release-rehearsal-fixes`, `69d0226` plus
working-tree changes. Results below were supplied by MA, not rerun by docops.

| Check | Received result |
|---|---|
| Initial backend | Preserved initial build/rerun PASS: 1,632 tests, 0 failures/errors, 19 skipped; JaCoCo gates PASS. |
| Initial frontend | 111 files/1,449 tests PASS; coverage statements 90.01%, branches 82.29%, functions 90.85%, lines 92.56%. Typecheck/lint/full formatting/build PASS. |
| WI-004 focused | 84 frontend / 107 backend tests PASS; final full results are separate below. |
| Final backend | Build PASS in 2m55s; JUnit 1,689 total / 1,670 executed / 19 skipped, 0 failures/errors. Skips: 18 gated MySQL + 1 LocalStorage platform. JaCoCo line 87.374%, method 85.261%, branch 72.338%; gates PASS. |
| Final frontend | 112 files / 1,458 tests PASS in 234.78s. Coverage statements 90.1%, lines 92.66%, functions 91.04%, branches 82.38%. Final typecheck/lint/full formatting/build PASS. |
| Helpers | Dry-run suite, backend environment (10 checks), bootstrap guards PASS. DB Preflight source schema 43 PASS, not live creation. |
| Existing local DB/runtime | Read-only counts: 43 tables / 511 columns / 175 confirmed distinct indexes / 232 index-column entries / 91 FK columns / 6 plans. Journal 30 DONE unchanged, QA bootstrap off. The 232 entries and 175 indexes are different units. No new DB or restore. |
| Rebuilt backend | MA restarted only owned PID 29008 to rebuilt PID 30612, main worktree/local profile/absolute roots/bootstrap off. Hibernate validate startup PASS in 20 seconds; integrity 30 checked / 20 available / 10 missing unchanged. |
| ADMIN nickname check | Profile role ADMIN confirmed; existing canonical nickname and NBSP/BOM-wrapped form both unavailable (`false`), comparison PASS. Logged out afterward; no account-field mutation or nickname/token disclosure. |
| Storage/media | 30 references/10 historical missing unchanged. All current demo Tracks 4-13 full stream 200/range 206/thumbnail 200. ADMIN inspection 200, ordinary 403, anonymous 401. |
| Origin/reload | localhost-Origin batch POST 200; 127.0.0.1 403 `Invalid CORS request`; untrusted Origin 403. Actual localhost guest Track 4 play/pause/seek/reload retained 5 seconds, paused state and green waveform. No player fix or CORS expansion. |
| Natural visible-list next | Repeat off: 9 -> 10 -> 11 -> 12 advanced naturally; MA paused 12 at 4.26 seconds, all healthy. PASS for observed list progression. |
| Visible-list repeat-all | Last visible Track 7 ended paused on 7; no wrap. Existing SR-83 policy, not a new regression. Repeat-all labeling needs maintenance/policy clarification; list wrap is NOT PASS. |
| Queue repeat | After Track 7 ended, navigating Home cleared page context; resuming wrapped to Track 4 (`DawnSignal`) playing, then paused at 1 second. Queue repeat-all PASS; no visible-list wrap claim. |
| Guest drawer/chips/Home | Likes -> child Playlists -> Likes stayed open with Likes pressed; another Likes click closed it. Calm + Bright stayed selected/visible despite zero AND results. Updated creator copy/footer rendered. |
| Mobile 390x844 | Expanded player buttons/waveform/volume visible without overlap; volume Home/End ended at 1. Likes manual-tab/parent-click PASS; viewport reset. |

The original Origin comparison and real-browser reproduction are recorded in
[WI-005](../agent/WI-20260905-ATS-005-evidence-pack.md); the additional list,
queue and guest UI observations are attributed to MA's follow-ups. The earlier
desktop report had no HMR since 16:35:23; latest source/UI freeze is 16:36:50.
No new data repair, provider,
mail, Cloudflare, backup/restore or production-deployment result is claimed.
MA verified owned backend 30612 and frontend 28724 stopped and their ports
released; the runtime results are dated evidence, not current availability.

## Open Release Gates

| Gate | Authority |
|---|---|
| Named client acceptance; local evidence/document validation is complete, with only scoped staging and commit remaining MA-owned | Current REQ G1-G3; Payment Acceptance Test Checklist, Section 13 |
| Named single-server target, HTTPS/proxy/CORS/callbacks, secrets, deployment/rollback, strict storage integrity | SR-93, Production Transition Checklist / Configuration; `docs/design/runtime-storage-operations.md:64` |
| Consistent DB/public/private backup and operator-approved isolated recovery proof | `docs/design/runtime-storage-operations.md:102` |
| Actual scheduler ownership/jobs, collected logs, chosen alert channel and incident response | SR-93, Production Transition Checklist / Operations; `docs/design/payment-operations-runbook.md:490` |
| Separately approved Toss recurring-card rehearsal and required SMTP delivery | `docs/SR/SR-93.md:60`; `docs/design/payment-operations-runbook.md:508` |
| Explicit final release approval | `docs/SR/SR-93.md:57` |

Retained-data migration is conditional, not a requirement to preserve old
throwaway tracks. No new DB, historical repair, multi-server lock, new payment
method or new notification channel is requested. A mismatched development
tuple still cannot count as a healthy strict production tuple.

## Documentation Updated

- Checklist: retained historical 42-table proof, added dated current 43-table
  contract evidence, and replaced the obsolete required branch wording.
- SR-93: separated dated initial/final local results and retained OPEN target/external
  gates; no final GO or restore PASS.
- Runtime storage guide: added exact browser-Origin preflight including the
  read-only public batch POST; canonical localhost and origin-scoped storage.
- This summary and the evidence pack complete the five-file ownership scope.
- Do not treat historical WI-023 provisioning PASS as the SMTP/Toss gate:
  REQ-20260817-ATS-010 assigns that same WI number a different scope.
- Preserve WI-028's failed provisioning-evidence review; aggregate fixture
  counts are not canonical parity proof and require no data action now.
- WI-003 status: COMPLETE; MA document-validation gate: PASS on 2026-09-05
  (665 IDs, links and index); `git diff --check` PASS. Completed
  [WI-002](../agent/WI-20260905-ATS-002-evidence-pack.md) holds the runtime evidence.
  No stage, commit, server, secret-file read or external action was performed
  by docops. Only scoped staging and commit remain MA-owned for the local
  closeout; all five owned documents are settled with no further edits planned.
- Scoped document inspection found no missing linked files, unmatched code
  fences or trailing whitespace in the five owned documents. This is not the
  full repository document validator or an application test result. MA's PASS
  above precedes this final status-only update; the final staging quick check
  remains MA-owned.
