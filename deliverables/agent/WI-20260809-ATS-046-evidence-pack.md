# Evidence Pack: WI-20260809-ATS-046

## Summary (one-liner)

- Closed Playlist mutation recovery and local thumbnail-preview lifecycle hardening after two independent QA failure/remediation rounds and a final independent QA PASS.

## Scope / DoD Check

- DoD items:
  - [x] Playlist Drawer Track removal and Playlist deletion require target-specific confirmation before the request.
  - [x] Unique pending-operation ownership fences duplicate and stale operations; failure remains visible and retryable for the same current target.
  - [x] Add-to-Playlist renders loading, retryable list failure, and an explicit subscription-required outcome, including when no callback is supplied.
  - [x] Open/close and Track replacement synchronously retire stale controls, list/add completions, projections, and delayed close timers.
  - [x] Playlist list/edit revoke each locally owned preview object URL exactly once at replacement, removal, close/owner/route, and unmount boundaries without revoking backend URLs.
  - [x] Final focused suite passed: 4 files, 61 tests.
  - [x] Full frontend, backend, documentation, and diff gates passed.
  - [x] Final independent QA returned PASS.

## Reference Documents (Tier 0-2)

**Injected Context** (from `deliverables/agent/WI-20260809-ATS-046-handoff.md:27-52`):

| Tier | Document | Reason |
|------|----------|--------|
| 0 | `docs/standards/core-principles.md` | Constitution required for all agents |
| 0 | `docs/standards/development-standards.md` | SE implementation standard |
| 1 | `docs/policies/quality-gates.md` | Required quality gates |
| 2 | `.agents/skills/react-best-practices/AGENTS.md` | React implementation and review guidance |
| 2 | `docs/standards/frontend-standards.md` | Current frontend lifecycle contract |
| 2 | `docs/design/usecase/sound-playlist.md` | Playlist use-case behavior |
| 2 | `docs/ui/screen-flow.md` | Current screen flow |
| 2 | `docs/ui/atstudio-front-list.md` | Current frontend inventory |

**Additional closure context**:

- `docs/standards/documentation-standards.md`
- `docs/standards/glossary.md`
- `deliverables/user/REQ-20260809-ATS-001.md`
- `deliverables/agent/WI-20260809-ATS-031-consolidated-findings.md`
- `deliverables/agent/WI-20260809-ATS-024-findings.md`
- `deliverables/agent/WI-20260809-ATS-037-evidence-pack.md`
- `deliverables/agent/WI-20260809-ATS-045-evidence-pack.md`

**Injection Rules Applied**:

- Rule source: `.claude/config/context-injection-rules.json`
- Assignee chain: `se` -> `qa-fe` -> `se` -> `qa-fe` -> `se` -> final independent `qa-fe` -> `docops`
- Task type: frontend implementation, independent review, remediation, and documentation closure
- Required tiers: Tier 0, inferred Tier 1 quality policy, and Playlist-specific Tier 2 context

## Evidence Pointers

### Root implementation

- `frontend/src/components/player/PlaylistDrawer.tsx:337-343` projects pending state only for the current target.
- `frontend/src/components/player/PlaylistDrawer.tsx:364-405` validates owner/detail/session target currency and retires confirmation/pending state.
- `frontend/src/components/player/PlaylistDrawer.tsx:481-520` assigns a monotonic operation ID, fences duplicate starts and stale completion, and preserves newer same-target operations.
- `frontend/src/components/player/PlaylistDrawer.tsx:869-882` renders target-specific confirmation, retry copy, busy state, confirm, and cancel controls.
- `frontend/src/components/playlist/AddToPlaylistModal.tsx:23-41` defines the render-time `(open, trackId)` lifecycle key and operation ownership refs.
- `frontend/src/components/playlist/AddToPlaylistModal.tsx:58-105` fences list loading and commits against the current generation/key.
- `frontend/src/components/playlist/AddToPlaylistModal.tsx:107-153` retires list/add/timer ownership in layout/effect lifecycle boundaries.
- `frontend/src/components/playlist/AddToPlaylistModal.tsx:163-257` fences retry, selection, duplicate add, stale add completion, and delayed close.
- `frontend/src/components/playlist/AddToPlaylistModal.tsx:259-280` immediately projects loading instead of prior Track state and renders retry/subscription-required outcomes.
- `frontend/src/components/playlist/AddToPlaylistModal.module.css:15-42` and `:109-112` style explicit state and retry controls.
- `frontend/src/pages/subscriber/PlaylistListPage.tsx:168-183`, `:252-261`, and `:489` own and release create-thumbnail object URLs.
- `frontend/src/pages/subscriber/PlaylistEditPage.tsx:89-94`, `:115-165` own and release edit-thumbnail object URLs across load/route/replacement boundaries.

### Focused regression tests

- `frontend/src/components/player/playerComponents.test.tsx:208-234` proves zero API calls before target-specific confirmation.
- `frontend/src/components/player/playerComponents.test.tsx:237-284` proves duplicate fencing and same-target failure/retry.
- `frontend/src/components/player/playerComponents.test.tsx:287-453` proves owner, detail/back, reopened same-target operation, tab, drawer-close, and session retirement boundaries.
- `frontend/src/components/playlist/AddToPlaylistModal.test.tsx:69-110` proves visible loading, bounded retry, and callback-optional subscription feedback.
- `frontend/src/components/playlist/AddToPlaylistModal.test.tsx:114-228` proves close/reopen and Track replacement retirement, no one-frame stale projection, and callback identity stability.
- `frontend/src/components/playlist/AddToPlaylistModal.test.tsx:231-345` proves stale add/timer suppression, duplicate add fencing, and unmount cleanup.
- `frontend/src/pages/subscriber/PlaylistListPage.test.tsx:297-340` proves exact create-preview revocation across removal, close, owner replacement, and unmount.
- `frontend/src/pages/subscriber/PlaylistEditPage.test.tsx:197-241` proves exact edit-preview revocation and proves the backend thumbnail URL is not revoked.

### Current-state documentation in the implementation patch

- `docs/design/usecase/sound-playlist.md:118-135`, `:159-161`, `:193-203`, and `:223-233`
- `docs/standards/frontend-standards.md:520-535`
- `docs/ui/atstudio-front-list.md:132-141`
- `docs/ui/screen-flow.md:113-123`

### Review and remediation chain

| Stage | Decision and evidence |
|------|------------------------|
| Independent QA round 1 | **FAIL**: a passive-effect window allowed a stale Add control, and the stale-boundary matrix was incomplete. See `deliverables/agent/WI-20260809-ATS-046-remediation-handoff.md:9-12` and `:52-53`. |
| Remediation 1 RED | 37 focused tests, 1 failure: detached Track replacement started `addTrackToPlaylist(1, 20)`. See `deliverables/agent/WI-20260809-ATS-046-qa-fe-rereview-handoff.md:55-56`. |
| Remediation 1 GREEN | 4 focused files, 59 tests passed. See `deliverables/agent/WI-20260809-ATS-046-qa-fe-rereview-handoff.md:57`. |
| Independent QA rereview | **FAIL**: retired Drawer pending state leaked into replacement detail, and Track replacement rendered one stale projection frame. See `deliverables/agent/WI-20260809-ATS-046-remediation-2-handoff.md:9-12` and `:51-53`. |
| Remediation 2 RED | 39 focused tests, 2 failures reproducing pending leakage and stale projection. Execution result supplied to DocOps with this closure. |
| Remediation 2 GREEN | The same 39-test remediation set passed after unique operation identity and render-time projection ownership were applied. |
| Final focused verification | **PASS**: 4 files, 61 tests. |
| Final independent QA | **PASS**: no actionable WI-046 correctness or test gap remained. The final QA result was supplied to DocOps as a chat-only closure result; no separate repository report was created. |

## Commands & Outputs

Run frontend commands from `frontend/`; script definitions are at `frontend/package.json:8-15`.

| Command | Result |
|---------|--------|
| `npm test -- src/components/player/playerComponents.test.tsx src/components/playlist/AddToPlaylistModal.test.tsx src/pages/subscriber/PlaylistListPage.test.tsx src/pages/subscriber/PlaylistEditPage.test.tsx` | PASS: 4 files, 61 tests |
| `npm run test:coverage` | First run overlapped the backend Gradle process and timed out one existing test at 5 seconds, `frontend/src/test/coverage/publicAuthShell.coverage.test.tsx:656`; no WI-046 focused test failed |
| `npm test -- src/test/coverage/publicAuthShell.coverage.test.tsx -t "loads the home feed and follows album and tag exploration actions"` | PASS: 1/1 |
| `npm run test:coverage` | After the backend Gradle process ended, the plain coverage command ran alone and passed: 91 test files, 1,076/1,076 tests |
| `npm run typecheck` | PASS |
| `npm run lint` | PASS with `--max-warnings 0` |
| `npm run format` | PASS across the full frontend tree |
| `npm run build` | PASS: TypeScript plus Vite, 286 modules transformed |
| `.\gradlew.bat test jacocoTestReport jacocoTestCoverageVerification assemble --rerun-tasks --no-daemon --max-workers=1 --console=plain` | `BUILD SUCCESSFUL`: 1,568 tests; `jacocoTestReport`, `jacocoTestCoverageVerification`, and `assemble` passed |
| `python .agents/skills/validate-docs/scripts/validate_docs.py` | PASS after closure writing: Tier 0, internal links, 585 traceability IDs, and document index |
| `git diff --check` | PASS after closure writing |

The first coverage timeout is recorded as resource-contention flake evidence, not a hidden failure: it occurred while frontend coverage and backend Gradle ran concurrently, the exact test then passed 1/1 in isolation, and plain `npm run test:coverage` passed all 1,076 tests when run alone after Gradle ended.

The repository's general Windows build command is documented as `gradlew.bat build` at `.agents/skills/build-check/SKILL.md:30-45`; it was not the command executed as WI-046 closure evidence.

## Test and Coverage Results

### Frontend

- Final focused: 4 files, 61 tests, all passed.
- Plain full coverage rerun after backend Gradle ended: 91 test files, 1,076 tests, all passed.
- Coverage source: `frontend/coverage/coverage-summary.json`.

| Metric | Result |
|--------|--------|
| Statements | 88.82% (8,979/10,109) |
| Branches | 80.77% (5,843/7,234) |
| Functions | 89.29% (2,128/2,383) |
| Lines | 91.16% (8,259/9,059) |

### Backend

- JUnit source: `build/test-results/test/TEST-*.xml`.
- Executed Gradle verification command: `.\gradlew.bat test jacocoTestReport jacocoTestCoverageVerification assemble --rerun-tasks --no-daemon --max-workers=1 --console=plain`.
- Result: `BUILD SUCCESSFUL`.
- Tests: 1,568 total; 1,549 passed; 19 skipped; 0 failures; 0 errors.
- JaCoCo source: `build/reports/jacoco/test/html/index.html`.

| Metric | Result |
|--------|--------|
| Instruction | 86.957% |
| Branch | 72.251% |
| Line | 87.228% |
| Method | 84.730% |
| Class | 94.824% |

- Coverage verification thresholds and task dependency are defined at `build.gradle:96-139`; `assemble` and coverage verification passed.

## Risks / Residuals / Rollback

- Residuals:
  - WI-20260809-ATS-057 dialog/focus/keyboard accessibility semantics and WI-20260809-ATS-059 card/keyboard/image-fallback accessibility semantics remain explicitly out of scope (`deliverables/agent/WI-20260809-ATS-046-handoff.md:10-13`).
  - The jsdom navigation informational message remains; it did not fail the isolated test or the later plain full coverage rerun.
  - No real authenticated mutation, download, provider, mail, or other external effect was performed.
  - Protected output artifacts were not inspected or modified.
- Rollback:
  - Revert the WI-046 patch as one unit across the 5 frontend implementation/style files, 4 focused test files, and 4 current-state documents listed above.
  - Remove `deliverables/agent/WI-20260809-ATS-046-evidence-pack.md` and `deliverables/user/WI-20260809-ATS-046-summary.md` if the closure itself is rolled back.
  - No database, schema, provider, persisted-data, or external-effect rollback is required.

## Follow-ups

- Continue the blocked accessibility work under WI-20260809-ATS-059 after WI-046 closure.
- Keep WI-20260809-ATS-057 and WI-20260809-ATS-059 semantics separate from this recovery/lifecycle closure.
