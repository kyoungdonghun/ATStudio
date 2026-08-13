---
version: 1.0
last_updated: 2026-08-13
project: ATS
owner: se
category: evidence-pack
status: complete
related_wi: WI-20260809-ATS-045
---

# Evidence Pack - WI-20260809-ATS-045

## Change Summary

- Made member playlist, like, license, question, and Download History reads latest-owner wins across authenticated user, access token, route, query, tab, page, and drawer lifecycle changes.
- Added render-time projection ownership so retired rows, forms, dialogs, controls, selection, capacity, and page-owned player context are hidden before passive-effect cleanup.
- Bound Download History single and bulk operations to one initiating read owner and abort signal through target preparation, each download iteration, browser blob effects, count refresh, messages, and cleanup.
- Replaced permissive numeric coercion with canonical positive decimal safe-integer parsing for playlist, license, and question detail routes; malformed IDs terminate locally with recovery UI and no request.
- Replaced playlist-capacity fallback behavior with explicit `loading`, positive `known`, and retryable `error` states. Creation remains fail-closed until both list and capacity projections belong to the current owner.
- Added focused StrictMode, token-only replacement, pre-passive-effect, stale completion, detached-control, invalid-ID, capacity-retry, and multi-step download regression coverage.
- Updated current-state use cases, frontend standards, and screen-flow documentation, then removed unrelated whole-file formatting churn.

## Scope / DoD Check

- [x] `CR-031-042`: member list/detail loads reject retired success, failure, empty, and `finally` commits.
- [x] `CR-031-045`: malformed, noncanonical, nonpositive, missing, and unsafe route IDs terminate without issuing a detail request.
- [x] `CR-031-049`: playlist capacity has no client default or stale-owner reuse; failure is visible and retryable.
- [x] Previous-owner data, dialogs, controls, and player context are synchronously hidden on owner/read-key replacement.
- [x] Download History bulk preparation, confirmation, loop, browser effect, count refresh, feedback, and cleanup remain owned by their initiating read key.
- [x] No raw access token or owner/read key is rendered, logged, documented as a value, or persisted.
- [x] Independent QA, full frontend/backend gates, documentation validation, and diff validation pass.
- [x] No backend endpoint, schema, data, payment/provider, mail, or download/export acceptance effect was executed.

## Reference Documents

| Tier | Document | Reason |
| --- | --- | --- |
| 0 | `AGENTS.md` | Repository workflow and role rules |
| 0 | `docs/standards/core-principles.md` | Constitution and product boundaries |
| 0 | `docs/standards/development-standards.md` | Implementation and test expectations |
| 0 | `docs/standards/documentation-standards.md` | Current-state documentation rules |
| 1 | `docs/policies/quality-gates.md` | Required verification gates |
| 1 | `docs/policies/security-policy.md` | Authentication material handling |
| 1 | `docs/standards/frontend-standards.md` | Current frontend ownership and route contracts |
| 2 | `deliverables/user/REQ-20260809-ATS-001.md` | Approved remediation requirement |
| 2 | `deliverables/agent/WI-20260809-ATS-031-consolidated-findings.md` | Root finding portfolio |
| 2 | `deliverables/agent/WI-20260809-ATS-045-handoff.md` | Canonical scope and DoD |

## Evidence Pointers

### Ownership and Route Boundaries

- `frontend/src/utils/ownerProjection.ts` - in-memory authenticated owner and read-key construction plus live-store revalidation; keys are never rendered, logged, or persisted.
- `frontend/src/utils/routeId.ts` - canonical `[1-9][0-9]*` and safe-integer route-ID parser.
- `frontend/src/pages/subscriber/PlaylistListPage.tsx` and `frontend/src/components/player/PlaylistDrawer.tsx` - independently owned playlist/capacity/detail reads, fail-closed create UI, retry, modal reset, detached handler guards, and drawer-session retirement.
- `frontend/src/pages/subscriber/PlaylistDetailPage.tsx` and `PlaylistEditPage.tsx` - strict route termination, owner/read projection fencing, dialog/form/track protection, and owner-scoped player context.
- `frontend/src/pages/subscriber/LikeListPage.tsx` - tab and owner projection ownership plus player-context cleanup.
- `frontend/src/pages/subscriber/LicenseListPage.tsx` and `LicenseDetailPage.tsx` - page/detail ownership, modal protection, strict detail ID, and stale response suppression.
- `frontend/src/pages/subscriber/QuestionListPage.tsx` and `QuestionDetailPage.tsx` - query/detail ownership, strict detail ID, and mutation/dialog guards.
- `frontend/src/api/playlists.ts`, `likes.ts`, `licenses.ts`, and `questions.ts` - optional AbortSignal support for owned reads.

### Download History Effects

- `frontend/src/pages/subscriber/DownloadHistoryPage.tsx` - one owner/read key across list, count, selection, player context, single download, all-target preparation, confirmation, bulk loop, count refresh, feedback, and cleanup.
- `frontend/src/api/downloads.ts` - optional AbortSignal support for history, target-ID, count, and blob requests.
- `frontend/src/pages/subscriber/DownloadHistoryPage.test.tsx` - layout-commit token replacement, delayed target preparation, retired bulk loop, count refresh, same-role owner replacement, and StrictMode regressions.

### Regression Tests

- `frontend/src/utils/routeId.test.ts` - canonical and rejected route spellings.
- `frontend/src/pages/subscriber/PlaylistListPage.test.tsx`, `PlaylistDetailPage.test.tsx`, and `PlaylistEditPage.test.tsx` - capacity, owner replacement, pre-passive projection, malformed IDs, and stale completion.
- `frontend/src/pages/subscriber/LikeListPage.test.tsx`, `LicenseListPage.test.tsx`, `LicenseDetailPage.test.tsx`, `QuestionListPage.test.tsx`, and `QuestionDetailPage.test.tsx` - list/detail ownership and recovery.
- `frontend/src/components/player/playerComponents.test.tsx` - drawer lifecycle, stale detail, token replacement, and detached control coverage.
- `frontend/src/api/domainApis.test.ts` and coverage harness updates - exact AbortSignal API contracts and maintained suite reachability.

### Documentation

- `docs/design/usecase/download-queue.md`, `sound-playlist.md`, `user-license.md`, and `user-question.md` - current owner, effect, capacity, and route recovery behavior.
- `docs/standards/frontend-standards.md`, `docs/ui/atstudio-front-list.md`, and `docs/ui/screen-flow.md` - shared implementation and presentation contracts.

## Independent QA and Remediation

- Initial QA handoff: `deliverables/agent/WI-20260809-ATS-045-qa-fe-review-handoff.md`.
- QA found three issues: an open playlist-create modal could outlive owner/capacity replacement, Download History lacked same-role owner replacement ownership, and JavaScript number coercion accepted noncanonical route IDs.
- First remediation handoff: `deliverables/agent/WI-20260809-ATS-045-qa-remediation-handoff.md`. Owner keys, strict route parsing, modal reset, and Download History ownership were added.
- Verification handoff: `deliverables/agent/WI-20260809-ATS-045-qa-fe-verification-handoff.md`.
- Verification found that passive-effect cleanup alone still permitted one replacement render with prior projection, and that Download History target preparation/bulk effects required stronger ownership tests.
- Second remediation handoff: `deliverables/agent/WI-20260809-ATS-045-owner-projection-remediation-handoff.md`. Render-time projection keys, layout cleanup, multi-step operation ownership, and layout-observation tests closed those findings.
- Final read-only QA handoff: `deliverables/agent/WI-20260809-ATS-045-final-qa-handoff.md`. Result: PASS, no actionable finding; all prior P2/P3 findings explicitly closed; focused `12` files / `120` tests passed.
- Documentation cleanup handoff: `deliverables/agent/WI-20260809-ATS-045-docops-cleanup-handoff.md`. Unrelated formatting churn was reduced while preserving every WI-045 semantic addition; docs validation passed.

## Final Verification Results

| Command | Result |
| --- | --- |
| Focused WI-045 Vitest command | PASS - `12` files, `120` tests, failures `0` |
| `npm run test:coverage` | PASS - `91` files, `1058` tests, failures `0`; statements 88.61% (`8828/9962`), branches 80.52% (`5740/7128`), functions 89.16% (`2107/2363`), lines 91.02% (`8112/8912`) |
| `npm run typecheck` | PASS - TypeScript no-emit check |
| `npm run lint` | PASS - full `frontend/src`, zero warnings |
| `npm run format` | PASS - all matched frontend files |
| `npm run build` | PASS - Vite 6.4.3 production build; `286` modules transformed |
| `.\gradlew.bat test jacocoTestReport jacocoTestCoverageVerification assemble --rerun-tasks --no-daemon --max-workers=1 --console=plain` | PASS - `1568` tests, failures `0`, skipped `19`; instruction 86.957%, branch 72.251%, line 87.228%, method 84.730%, class 94.824%; assemble PASS |
| `python .agents/skills/validate-docs/scripts/validate_docs.py` | PASS - Tier 0, internal links, `585` supported traceability IDs, and document index |
| `git diff --check` | PASS - final source, tests, docs, and WI deliverables |

No real authenticated download/export, payment/provider/refund, mail, database, schema, or ignored-secret action was executed. Protected output artifacts were not touched, inspected, staged, or deleted.

## Risks / Rollback

- Risk: authenticated browser behavior is represented by deterministic component/API tests; a later acceptance WI remains responsible for live session-switch and browser interaction evidence.
- Risk: access-token identity is retained only as an in-memory equality component. Future ownership helpers must preserve the rule that token/key values never reach DOM, logs, documentation evidence values, or persistence.
- Risk: future multi-step member operations must carry one initiating owner/read key through every effect and follow-up request.
- Rollback: revert the member read ownership utilities, API AbortSignal additions, affected subscriber pages/drawer, tests, current-state docs, and WI deliverables as one patch. No data rollback is required.

## Follow-ups

- Continue the approved remediation chain with the next unclosed portfolio WI after `WI-20260809-ATS-045`.
- Later browser acceptance should exercise authenticated owner replacement without invoking real download/export, provider, refund, or mail effects unless separately approved.
