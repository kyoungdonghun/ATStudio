---
version: 1.0
last_updated: 2026-09-05
project: ATS
owner: se
category: evidence-pack
status: confirmed
dependencies:
  - path: WI-20260905-ATS-005-handoff.md
    reason: Approved diagnosis scope and browser ownership
  - path: ../user/REQ-20260905-ATS-001.md
    reason: Approved release closeout
---

# Evidence Pack: WI-20260905-ATS-005

## Summary

The reported empty PlayerBar after refresh is explained by a runtime Origin
mismatch, not a demonstrated persistence defect. MA's controlled requests
isolated CORS rejection for `http://127.0.0.1:5173`; the documented local origin
is `http://localhost:5173`. MA confirmed the original playback/refresh scenario
passes at that canonical origin without source or CORS allowlist changes.

## Scope / DoD Check

- [x] Read the generated handoff and inspect the current persistence, history,
  API, and CORS configuration paths.
- [x] Explain why stored history can render while player hydration fails.
- [x] Keep player source/tests, product configuration, ignored local
  configuration, peer-owned UI, DB, HTTP behavior, providers, and secrets unchanged.
- [x] No temporary instrumentation was added; nothing requires removal.
- [x] MA confirms play, pause, seek, reload, restored Track/time, and paused
  state at the canonical localhost origin. The reported reproduction is PASS.

MA superseded the instrumentation plan before any write. No new subagents,
browser operations, service restarts, commits, or product fixes were performed
by this assignee. Any future configuration change requires a scope addendum.

## Reference Documents (Tier 0-2)

| Tier | Document | Reason |
| --- | --- | --- |
| 0 | `docs/standards/core-principles.md` (`STD-001`) | Approval, transparency, constrained execution |
| 0 | `docs/standards/development-standards.md` (`STD-002`) | Implementation and evidence traceability |
| 0 | `docs/standards/documentation-standards.md` (`STD-004`) | Evidence metadata and structure |
| 0 | `docs/standards/glossary.md` (`STD-005`) | Public Listening and PlayableTrack terminology |
| 1 | `docs/policies/security-policy.md` | No secrets or broad storage logging |
| 2 | `deliverables/user/REQ-20260905-ATS-001.md` | Existing approved release closeout |
| 2 | `deliverables/user/WI-20260823-ATS-010-summary.md` | Prior auth/pause fixes; browser DoD left open |
| 2 | `.agents/skills/test/SKILL.md` | Focused test scope; no full-suite rerun |
| 2 | `.agents/skills/react-best-practices/SKILL.md` | Existing storage/event patterns; no speculative redesign |
| 2 | `.agents/skills/create-wi-evidence-pack/SKILL.md` | Standard result contract |
| 2 | `.agents/skills/validate-docs/SKILL.md` | Document verification |

Assignee: `se`; task: diagnosis/documentation. The existing generated handoff
is the delegation contract; no additional delegation was performed.

## Evidence Pointers

### MA-Reported Live Comparison

Same target `POST http://127.0.0.1:5173/api/tracks/batch`, body `{"ids":[4]}`:

| Origin header | Result reported by MA |
| --- | --- |
| `http://127.0.0.1:5173` | HTTP 403, `Invalid CORS request` |
| `http://localhost:5173` | HTTP 200, Track 4 in `dataList` |
| Absent | HTTP 200, Track 4 in `dataList` |

These are MA's live results, not requests executed by SE. The earlier successful
API probe omitted Origin and therefore did not exercise the browser's rejected
Origin. MA also reported that the ignored local configuration allows localhost;
SE did not open or modify that file.

### MA-Confirmed Real-Browser Result

MA's final report on 2026-09-05:

- Exact browser URL: `http://localhost:5173/tracks/4`.
- Actual UI sequence: play -> pause -> slider Home -> ArrowRight to 5 seconds
  -> reload.
- After reload: current Track `AT.M Demo01` remained in the PlayerBar, slider
  value was 5, time text was `0:05 / 0:07`, and playback remained paused.
- Result: PASS for the original player-refresh reproduction at the configured
  canonical origin. SE did not operate the browser; this is MA's observed result.
- No player fix or CORS allowlist expansion was needed. The failing run used
  127.0.0.1 in the MA/test harness despite the existing example warning.

This result does not claim a new localhost history-modal or next-Track test;
the history observation in the original handoff remains separately attributed.

### Current Source Explanation

- `application-local.example.yml:42` and
  `src/main/resources/application.yml:50` allow localhost origins, including
  port 5173, but do not allow the corresponding 127.0.0.1 origin.
- `application-local.example.yml:64` explicitly advises against 127.0.0.1:5173
  unless the allowed origins include it. Use the documented localhost origin;
  no widening of the allowlist is required for this closeout.
- `src/main/java/com/atstudio/atstudio/config/CorsConfig.java:41` installs the
  exact configured origins and `:51` applies them to `/api/**`.
- `src/main/java/com/atstudio/atstudio/config/SecurityConfig.java:42` uses that
  CORS source. Its `:77` public authorization for `/api/tracks/batch` does not
  remove CORS validation. `WebConfig.java:21` and `:28` configure static
  resources and conversion only; there is no competing MVC CORS mapping.
- `frontend/vite.config.ts:108` proxies API requests to port 8080 with
  `changeOrigin: true`. The listener at `:132` binds 127.0.0.1; a listening
  address does not add a permitted browser Origin to backend configuration.
- `frontend/src/api/tracks.ts:84` hydrates through POST `/tracks/batch`.
  `frontend/src/store/playerStore.ts:498` awaits that request; the catch at
  `:529` marks hydration `error` without deleting the saved player record.
  The initial null current Track thus remains unpopulated after a rejected
  batch. There is no confirmed new player persistence defect to patch.
- Player persistence stores IDs and needs a successful current-public-Track
  batch before restoring the player. In contrast,
  `frontend/src/components/player/HistoryModal.tsx:29` initially renders local
  history, and `frontend/src/store/playerStore.ts:266` falls back to local
  history when its batch fails. Stored complete history entries can therefore
  remain visible, including the September 2 records observed by MA.
- The read-only browser evaluator's missing localStorage binding is a sandbox
  limitation, not evidence that page storage is unavailable.

## Minimal Remedy And Verification

1. Use `http://localhost:5173/tracks/4` and keep that exact origin for the entire
   play -> pause -> seek to 5 seconds -> reload sequence. MA completed this.
2. Seed playback through normal UI on localhost first. localhost and
   127.0.0.1 use separate origin-scoped storage; existing history on one is not
   expected to appear automatically on the other. Do not copy, clear, or
   migrate storage or tokens as part of this check.
3. The controlled localhost-Origin request returned HTTP 200; MA's browser
   subsequently restored Track 4 at 5 seconds without automatic playback.
   These are distinct reported observations, not an invented network capture
   of the final browser request.

Do not edit `playerStore.ts`, CORS defaults, the example, WebConfig,
SecurityConfig, or the ignored local settings for this remedy. If a distinct
failure remains at the canonical origin, preserve its evidence and reassess
scope; do not infer a second bug from the prior mismatched-origin run.

## Commands & Outputs

- `git status --short --branch`: confirmed `codex/v1-release-rehearsal-fixes`
  and pre-existing unrelated changes; all were preserved.
- Read-only source searches and UTF-8 reads located the pointers above.
- `git diff -- frontend/src/store/playerStore.ts frontend/src/store/playerPersistence.test.ts frontend/src/store/playerStore.test.ts`:
  empty output after the stop instruction; SE made no source/test edits.

## Tests And Validation

No product tests were run by SE because this became a diagnosis-only task with
no product patch. The supplied FE 1,449-test/coverage and BE 1,632-test results
are earlier MA evidence, not fresh results from this WI. The same-origin
real-browser result above supplies the missing reproduction evidence.

- `python .agents/skills/validate-docs/scripts/validate_docs.py`: PASS, exit 0;
  Tier 0 documents, internal links, 665 supported traceability IDs, and index.
- Scoped `git diff --check`: PASS, exit 0. Newly created deliverables were
  additionally checked with `git diff --no-index --check` against `NUL`:
  exit 1 for each new-file difference, with no whitespace diagnostics.

Document validation is separate from MA's browser acceptance result.

## Risks / Rollback

- This WI confirms the reproduced playback/refresh path only; it does not
  claim full release readiness or additional unreported browser scenarios.
- Origin changes create a separate browser storage namespace; do not mistake
  absence of the other origin's history for data loss.
- There is no source/configuration rollback. Only this WI's evidence and
  summary were created; no temporary diagnostics or runtime changes exist.

## Follow-ups

MA can use this completed diagnosis and browser result under
WI-20260905-ATS-002 and propagate it to WI-20260905-ATS-003. This pack does not
authorize deployment or close unrelated acceptance criteria.

Recommendation for the WI-003 documentation owner, without editing peer docs:

1. Record the exact browser origin (scheme, host, and port) before acceptance,
   then compare it with the effective `cors.allowed-origins` for the running
   backend, including approved external/local overrides. Report only the
   non-secret origin settings, not configuration or secret dumps.
2. Use canonical `http://localhost:5173` for the existing local configuration.
   A Vite bind address or a no-Origin HTTP 200 is not sufficient preflight.
3. Probe the existing public batch with the actual browser Origin and a known
   public Track ID; then confirm the real UI refresh scenario on that same
   origin. Report the request comparison and UI result separately.
4. For deployment, explicitly configure the actual approved deployment
   origin(s). Do not add wildcard origins or local aliases to production as a
   workaround for a test-harness origin mismatch.

## Files Changed

- `deliverables/agent/WI-20260905-ATS-005-evidence-pack.md`
- `deliverables/user/WI-20260905-ATS-005-summary.md`

Documentation only; player source/tests and all product/runtime configurations
were left unchanged by SE.
