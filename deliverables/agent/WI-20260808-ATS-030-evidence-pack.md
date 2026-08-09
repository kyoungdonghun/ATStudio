# Evidence Pack: WI-20260808-ATS-030

## Summary

- Final re-review result: `PASS`, with
  `0 BLOCKER / 0 MAJOR / 0 MINOR`.
- WI-20260809-ATS-017 repaired only the stale SR-97 coverage assertion, and the
  subsequent authoritative MA full-gate rerun passed.
- The initial `PASS WITHHELD` record is retained below as superseded audit
  history.

## Authoritative Final Disposition (2026-08-09 Re-review)

| Field                          | Result                                      |
| ------------------------------ | ------------------------------------------- |
| Status                         | `PASS`                                      |
| Findings                       | `0 BLOCKER / 0 MAJOR / 0 MINOR`             |
| Product implementation finding | None                                        |
| Blocking gate                  | None                                        |
| Resolution basis               | WI-017 repair plus green MA full-gate rerun |

This table is the authoritative final WI-030 disposition.

## Historical Withheld Disposition (Superseded)

| Field                          | Historical result                                               |
| ------------------------------ | --------------------------------------------------------------- |
| Status                         | `PASS WITHHELD`                                                 |
| Findings                       | `0 BLOCKER / 1 MAJOR / 0 MINOR`                                 |
| Product implementation finding | None                                                            |
| Blocking gate                  | Frontend `test:coverage`                                        |
| Required next action           | Repair MAJOR-001 assertion and rerun complete frontend coverage |

The historical condition was valid when the frontend gate was red. It was
satisfied by WI-20260809-ATS-017 and the post-repair MA rerun, so it no longer
controls the final disposition.

## Scope / DoD Check

- [x] Loaded WI-030 handoff and approved REQ-20260808-ATS-004.
- [x] Included final WI-028 PASS, final WI-029 PASS, and
      WI-20260809-ATS-016 summary/Evidence Pack.
- [x] Compared SR-94 through SR-101 across code, API, `schema.sql`, React UI,
      current documents, and tests.
- [x] Inspected the current Git diff and predecessor evidence for data,
      provider/email/secret/ZIP, and Git-action boundaries.
- [x] Reused MA full-gate evidence and ran only focused/static WI-030 checks.
- [x] Reviewed WI-20260809-ATS-017 summary/Evidence Pack and its focused 24/24
      repair result.
- [x] BLOCKER/MAJOR count is zero.
- [x] Complete frontend coverage gate passes.
- [x] Final PASS recorded.

## Reference Documents

| Tier        | Document                                            | Reason                                             |
| ----------- | --------------------------------------------------- | -------------------------------------------------- |
| 0           | `docs/standards/core-principles.md`                 | Constitution and execution boundaries              |
| 0           | `docs/standards/development-standards.md`           | Cross-layer implementation baseline                |
| 1           | `docs/policies/quality-gates.md`                    | Required regression and final-gate rules           |
| 2           | `docs/design/api-spec.md`                           | Current REST contract                              |
| REQ         | `deliverables/user/REQ-20260808-ATS-004.md`         | Approved SR-94 through SR-101 scope and exclusions |
| Handoff     | `deliverables/agent/WI-20260808-ATS-030-handoff.md` | Audit DoD and output contract                      |
| Predecessor | WI-028 summary/Evidence Pack                        | Final admin/payment security PASS                  |
| Predecessor | WI-029 summary/Evidence Pack                        | Final media/catalog PASS                           |
| Predecessor | WI-20260809-ATS-016 summary/Evidence Pack           | Final current-state documentation PASS             |
| Resolution  | WI-20260809-ATS-017 summary/Evidence Pack           | Stale assertion repair and focused 24/24 PASS      |

## Historical Finding (Resolved)

### MAJOR-001 - Resolved By WI-20260809-ATS-017

**Historical evidence and resolution pointers**

- `frontend/src/test/coverage/adminSubscriberGaps.coverage.test.tsx:663-717`
  - The original combined scenario contained the stale definite-failure
    assertion documented by the initial WI-030 review.
- `frontend/src/test/coverage/adminSubscriberGaps.coverage.test.tsx:692`
  - Uses plain `Error('request failed')`, which has no HTTP response and is
    classified as ambiguous.
- `frontend/src/test/coverage/adminSubscriberGaps.coverage.test.tsx:712-729`
  - Now asserts the approved unknown message, exactly one status retry, a
    disabled duplicate request, one create call, two bounded open-state reads,
    no list refresh, and the retained row.
- `frontend/src/pages/admin/UserSubscriptionCorrectionModal.tsx:200-204`
  - HTTP 4xx is definite; all other non-cancelled unresolved failures are
    ambiguous.
- `frontend/src/pages/admin/UserSubscriptionCorrectionModal.tsx:419-438`
  - Request reconciliation reads open state; known-ID stages read detail.
- `frontend/src/pages/admin/UserSubscriptionCorrectionModal.tsx:474-520`
  - Inconclusive reconciliation creates the unknown fence and current message.
- `frontend/src/pages/admin/UserSubscriptionCorrectionModal.tsx:679-688`
  - Unknown state locks the workflow and disables duplicate mutation.
- `frontend/src/pages/admin/UserSubscriptionCorrectionModal.tsx:748-759`
  - Exactly one read-only status retry is exposed.
- `frontend/src/pages/admin/UserSubscriptionManagePage.test.tsx:748-789`
  - Current focused regression already asserts initial and repeated null/204
    unknown-fence behavior.
- `docs/SR/SR-97.md:119-130`
  - Current SR contract matches the implementation.
- `deliverables/agent/WI-20260809-ATS-013-evidence-pack.md:40-64,125-149`
  - Authoritative repair classification and accepted point-in-time residual.
- `deliverables/agent/WI-20260809-ATS-017-evidence-pack.md`
  - Records the stale assertion-only scope, RED confirmation, GREEN 24/24,
    static gates, and no product/schema/data/secret/ZIP/Git-history change.

**Historical impact**

- REQ quality gate G5 requires the complete frontend coverage suite to pass.
  The one stale failure therefore withheld the initial WI-030 PASS even though
  product behavior matched the approved SR-97 contract.

**Required repair completion**

1. WI-017 kept product code unchanged and updated the scenario to the approved
   ambiguous no-response contract.
2. WI-017 focused Vitest passed 24/24.
3. MA reran `npm run test:coverage`: 71/71 files and 591/591 tests passed with
   all coverage thresholds satisfied.
4. The former MAJOR is closed and contributes no finding to the final totals.

## SR Cross-Layer Evidence Ledger

| SR    | Code/API/schema                                                                                                    | React UI                                                                         | Docs/tests                                                                         | Result                           |
| ----- | ------------------------------------------------------------------------------------------------------------------ | -------------------------------------------------------------------------------- | ---------------------------------------------------------------------------------- | -------------------------------- |
| 94/95 | `TagNamePolicy.java:8-39`; `TagService.java:31-84`; `TagNameConstraintTranslator.java:14-67`; `schema.sql:74-82`   | `tagName.ts:20-49`; `TagManagePage.tsx:118-140`                                  | `sound-tag.md:21-31,101-111`; tag policy/service/controller/UI tests               | Align                            |
| 96    | `UserService.java:283-388`; `UserRepository.java:20-26`; audit services; `schema.sql:1030-1052`                    | `UserManagePage.tsx`; `authStore.ts`; ADMIN 403 refresh path                     | `user-info.md:270-282`; security policy; unit/audit/MySQL concurrency tests        | Align                            |
| 97    | Correction controller/service/entities/repositories; `schema.sql:1054-1138`; no direct ADMIN update/cancel mapping | `UserSubscriptionCorrectionModal.tsx`; admin API/list page                       | `api-spec.md:59-71`; `user-subscription.md`; WI-013 and WI-017 tests               | Align; former MAJOR-001 resolved |
| 98    | `CanonicalImageService.java`; shared Track create/update path                                                      | `TrackThumbnailField.tsx:117-230`; upload/edit pages                             | `sound-track.md:23-35,236-244`; image and component/page tests                     | Align                            |
| 99    | `AudioAnalysisService.java`; `TrackService.java:63-99,167-210`; read-only ADMIN dry-run                            | API-consumer Track/player paths use stored duration/waveform                     | `sound-track.md:370-381`; CBR/VBR/WAV, atomic replacement, dry-run tests           | Align                            |
| 100   | Four-list `TrackSearchRequest`; Tag available API/service AND filters                                              | `HomePage.tsx:109-136,207-288`; `TrackListPage.tsx:262-310,400-455`              | `api-spec.md:166-168`; tag/track API and UI tests                                  | Align                            |
| 101   | Public `POST /api/tracks/batch`; `PlayableTrackService`; active aggregate repositories                             | `playerStore.ts:332-429`; `playableTrack.ts`; all aggregate/history entry points | PlayableTrack use cases; fake timer, persistence, aggregate, and query-count tests | Align                            |

## Predecessor Final Evidence

- WI-028: final `PASS`, `0 BLOCKER / 0 MAJOR / 0 MINOR`; accepted V1 admin
  residuals retained.
- WI-029: final disposition states `PASS`, `0 BLOCKER / 0 MAJOR / 0 MINOR`,
  superseding its preserved historical Playlist finding sections; WI-015
  independently verified zero-based reorder.
- WI-20260809-ATS-016: current-state documentation PASS; four stale statements
  repaired, indexes/counts verified, and WI-030 unblocked from documentation.
- WI-20260809-ATS-017: stale coverage assertion-only repair; focused 24/24 PASS,
  with no product implementation, schema, data, secret, ZIP, branch, commit, or
  push change.

## Commands And Outputs

### MA-provided authoritative final gates (post-WI-017)

- Backend clean full test + JaCoCo: PASS; 1,385 tests, 0 failed, 13 skipped;
  0 failures/errors; line 86.461%, method 83.919%, branch 71.555%; coverage
  verification PASS.
- Frontend `test:coverage`: PASS; 71/71 files and 591/591 tests; statements
  87.44%, branches 77.96%, functions 86.98%, lines 89.53%.
- Frontend typecheck, ESLint, and Prettier: PASS.
- Backend build: `BUILD SUCCESSFUL` in 2m52s.
- Frontend build: PASS; 272 modules transformed, Vite 2.97s.
- Documentation validation: PASS; 527 traceability IDs and 0 broken links.
- `git diff --check`: PASS.
- Generated `build/`, coverage, `dist/`, and `*.tsbuildinfo` outputs have no Git
  status entry.

### WI-017 repair verification

- RED focused Vitest: the retired assertion was the sole failure; 23 passed and
  1 failed.
- GREEN focused Vitest: 1 file passed and 24/24 tests passed.
- WI-017 typecheck, ESLint, scoped Prettier, and scoped whitespace checks:
  PASS.
- WI-017 summary/Evidence Pack state no product implementation, dependency,
  schema, data, secret, ZIP, branch, commit, or push change.

### Historical MA full gate (pre-WI-017)

- Frontend `test:coverage`: FAIL; 70/71 files and 590/591 tests passed.
- The only reported failure was the now-resolved stale assertion at the former
  line 712. This evidence is retained to explain the initial withheld decision.

### WI-030 focused backend

```text
.\gradlew.bat test --rerun-tasks \
  --tests "com.atstudio.atstudio.config.V1BackendBaselineContractTest" \
  --tests "com.atstudio.atstudio.service.audio.AudioAnalysisServiceTest" \
  --tests "com.atstudio.atstudio.service.TagServiceAvailableTagsIntegrationTest" \
  --tests "com.atstudio.atstudio.repository.PlayableTrackQueryCountTest"
```

- PASS: 18 tests, 0 failed, 0 errors, 0 skipped; `BUILD SUCCESSFUL`.
- Scope: fresh schema/provider isolation, decoded media, four-taxonomy available
  tags, and bounded PlayableTrack query count. This was not a full suite.

### WI-030 focused frontend

```text
npm test -- src/api/domainApis.test.ts src/pages/public/HomePage.test.tsx \
  src/pages/public/TrackListPage.test.tsx \
  src/pages/creator/TrackThumbnailField.test.tsx \
  src/store/playerStore.test.ts src/pages/subscriber/PlaylistEditPage.test.tsx
```

- PASS: 6 files, 71 tests.

### Historical focused finding reproduction

```text
npm test -- src/test/coverage/adminSubscriberGaps.coverage.test.tsx \
  -t "blocks rejected previews and preserves the current subscription row after request errors"
```

- Historical result before WI-017: FAIL, 1 failed and 23 skipped.
- The rendered WI-013 unknown-outcome fence disproved the obsolete assertion.
  WI-017 subsequently replaced that assertion and passed the full file 24/24.

### Static/document checks

- Initial WI-030 documentation validation: PASS; Tier 0, links, 526
  traceability IDs, and indexes.
- Final MA documentation validation after WI-017: PASS; 527 traceability IDs,
  0 broken links.
- Initial and final `git diff --check`: PASS.
- Schema added-line scan: 2 DDL statements, 0 DML statements.
- Git status/reflog/name scan: current branch and HEAD unchanged; no sensitive
  file diff, staging, commit, push, or client-branch action.

## Mutation And Forbidden-Action Evidence

- `src/main/resources/schema.sql:1030-1138` adds only the two fresh-baseline
  audit/correction tables. It contains no data migration or backfill DML.
- `AdminTrackAudioAnalysisService.java:39-128` is read-only; its focused test
  verifies no repository mutation. No live existing-row dry-run was invoked.
- Predecessor WI-016 explicitly records no live dry-run/backfill; WIs 017-022
  record no retained schema/data mutation. Earlier MySQL checks used exact
  disposable databases and cleaned them up; they do not certify or mutate a
  retained database.
- `V1BackendBaselineContractTest.java:148-166` verifies that Local Subscription
  Correction has no provider, refund, HTTP, or payment-audit dependency.
- No application server, provider endpoint, email sender, secret value, or
  production database was contacted by WI-030.
- `output/client-demo-screenshots-20260716-140514.zip` remains untracked at
  700,703 bytes with creation/write timestamps on 2026-07-16. It was not opened
  or modified. Predecessor WIs repeatedly recorded the same size and untouched
  boundary.
- Branch/HEAD: `codex/v1-release-rehearsal-fixes` / `c7f779d`, ahead 1 from a
  pre-existing commit. Reflog shows no WI execution branch operation; no
  `codex/p1-acceptance-hardening` or other client branch was changed.

## Accepted Residuals

- No server-bound preview receipt/token, V1 free-text DLP, or active-ADMIN
  composite index.
- Point-in-time reconciliation without polling/backend correlation; repeated
  null/204 remains operator-visible unknown state.
- No live MySQL production-cardinality plan, payload/latency profile, or
  deployed transport-loss proof.
- Taxonomy generation fencing ignores stale completions but does not cancel
  every underlying transport request.
- Existing malformed inactive membership ordering is normalized only by an
  authorized reorder/remove operation; no retained-data backfill.

## Acceptance-Test Boundaries

- SR-94 through SR-101 remain `OPEN`; focused implementation evidence is not a
  claim that full browser, operational, backfill, recovery, deployment, or
  external-provider acceptance has completed.
- Outstanding boundaries include legacy-tag collision/migration analysis,
  zero-ADMIN recovery drill, cross-surface thumbnail rendering, retained Track
  dry-run/backfill and rollback, native browser duration/seek/waveform/media
  events, accessibility/responsive discovery, and production-scale payload and
  query performance.

## Risks / Rollback

- Risk: the current shared worktree is large and dirty. Any future test repair
  must be hunk-scoped to the stale coverage scenario.
- Rollback: this WI changed only
  `deliverables/user/WI-20260808-ATS-030-summary.md` and this Evidence Pack.
  Remove only these two files to withdraw the audit. Do not revert product,
  test, schema, predecessor, ZIP, or unrelated user changes.

## Downstream Status

- WI-030 authoritative final status: **PASS**.
- Final findings: `0 BLOCKER / 0 MAJOR / 0 MINOR`.
- The former hold condition is satisfied by WI-017 focused 24/24 and the green
  MA complete frontend coverage rerun.
- Accepted residuals remain accepted; acceptance-test boundaries remain
  outstanding and do not alter this integration-audit PASS.
