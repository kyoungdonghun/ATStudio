# Evidence Pack: WI-20260808-ATS-021

## Summary (one-liner)

- Delivered one accessible Home tag-discovery module and a canonical repeated-parameter filter path across Usage, Genre, Mood, and Instrument, backed by active-track availability and focused cross-layer verification.

## Scope / DoD Check

- [x] Home exposes exactly one `태그별 탐색` module ordered Usage, Genre, Mood, Instrument.
- [x] Usage remains visible when empty and is never labeled or modeled as License.
- [x] The initial tab is the first category with active-track-bearing results, or Usage when none has results.
- [x] Registered tags and active-track-bearing available tags are loaded separately.
- [x] Normal, no-registered-tags, registered-without-active-results, and API-failure states are distinct.
- [x] Tag links use canonical `/tracks?usage=...`, `genre=...`, `mood=...`, and `instrument=...` query keys.
- [x] Korean, spaces, commas, hashes, and repeated AND values survive frontend and Spring binding atomically.
- [x] TrackList restores, requests, displays, changes, resets, paginates, and sorts all four tag types, including Instrument.
- [x] The available-tag backend supports Instrument, active Track filtering, bound values, one query, and no association-driven N+1 path.
- [x] Tabs implement roving focus with ArrowLeft, ArrowRight, Home, and End plus tablist/tabpanel semantics.
- [x] Mobile tabs scroll horizontally, and each category initially shows at most eight tags with explicit more/collapse controls.
- [x] SR-04 Home behavior remains available, and no section-card or nested-card treatment was introduced.
- [x] Focused backend/frontend tests cover states, encoding, restore/reset, AND behavior, accessibility, and the Instrument API path.
- [x] Final focused tests, TypeScript typecheck, targeted ESLint, targeted Prettier, deliverable Prettier, and `git diff --check` pass.
- [x] No commit, staging, deletion, dependency change, persistent DB/schema/data mutation, secret access, provider call, or external-system call occurred.

## Reference Documents (Tier 0-2)

The user-required documents and governing packet were read before implementation.

| Order | Tier | Document                                            | Reason                                                                   |
| ----- | ---- | --------------------------------------------------- | ------------------------------------------------------------------------ |
| 1     | 0    | `docs/standards/core-principles.md`                 | Approved execution, scope control, evidence, and sustainability          |
| 2     | 0    | `docs/standards/development-standards.md`           | Thin controllers, service ownership, DTO boundaries, SQL, and tests      |
| 3     | 1    | `docs/standards/frontend-standards.md`              | React TypeScript, CSS Modules, URL state, responsive UI, and a11y        |
| 4     | 0    | `docs/standards/documentation-standards.md`         | English system evidence, traceability, exact commands, and rollback      |
| 5     | 0    | `docs/standards/glossary.md`                        | Canonical Track, Tag, Usage, and Instrument terminology                  |
| 6     | REQ  | `deliverables/user/REQ-20260808-ATS-004.md`         | Approved acceptance-hardening scope and WI chain                         |
| 7     | SR   | `docs/SR/SR-100.md`                                 | Unified Home tag discovery and four-category filter source               |
| 8     | WI   | `deliverables/agent/WI-20260808-ATS-021-handoff.md` | Mandatory scope, DoD, constraints, verification boundary, and WI blocker |

Supplemental references:

- `.agents/skills/create-wi-evidence-pack/SKILL.md` - required pointer-first Evidence Pack structure.
- `.agents/skills/test/SKILL.md` - focused Gradle/Vitest verification contract.
- `.agents/skills/typecheck/SKILL.md` - TypeScript no-emit verification contract.
- `.agents/skills/eslint/SKILL.md` - targeted zero-warning ESLint verification.
- `.agents/skills/prettier/SKILL.md` - non-destructive formatting verification.
- `.agents/skills/react-best-practices/SKILL.md` - React effects, stale-result fences, and accessible interaction guidance.

**Assignee:** `se`

**Task type:** Spring tag-query contract plus React Home/TrackList implementation and focused cross-layer tests

## Design Rationale

1. Home calls the registered-tag endpoint and active-track available-tag endpoint together but stores their results separately. This is required to distinguish an absent taxonomy from a configured taxonomy that currently has no discoverable Track.
2. Category order is a fixed UI/domain constant. The selected tab is derived from the first available result in that order, while the tab strip always renders all four categories and therefore never hides Usage.
3. `URLSearchParams.append` and Spring `List<String>` binding form the canonical boundary. A comma can be part of one tag name, while repeated keys represent multiple exact values. This removes the ambiguity of CSV splitting and preserves multi-tag AND semantics.
4. TrackList uses the URL as the source of truth. `getAll` restores every repeated value; sort/page edits preserve tag keys; tag edits/reset replace only owned filter keys and return to page 1.
5. Available-tag SQL retains one query. Every selected exact tag adds a correlated Track-id predicate; tag type, tag name, and BPM are bound. The only generated SQL fragments are internal numeric aliases, never user or domain values.
6. Tag results map directly from the query's `Tag` rows to DTOs. No Track/Tag association traversal is performed after the query, preventing an N+1 load path.
7. The Home tab strip follows the ARIA tabs pattern with one `tabIndex=0` element and deterministic wrapped focus. The result panel is stable, unframed, and independently reports loading/error/empty states.
8. Eight initial tags bound mobile and desktop density. More/collapse is explicit, while horizontal overflow and snap behavior keep the tab strip operable on narrow screens.

## Evidence Pointers

Backend production:

- `src/main/java/com/atstudio/atstudio/dto/track/TrackSearchRequest.java:17` - four tag fields bind as repeated `List<String>` values.
- `src/main/java/com/atstudio/atstudio/controller/TagController.java:36` - available endpoint accepts four tag lists and forwards Instrument to the service.
- `src/main/java/com/atstudio/atstudio/service/TagService.java:88` - available-tag service boundary.
- `src/main/java/com/atstudio/atstudio/service/TagService.java:134` - policy canonicalization occurs before distinct without query-side character rejection.
- `src/main/java/com/atstudio/atstudio/service/TagService.java:111` - Genre, Mood, Instrument, and Usage predicates compose in order.
- `src/main/java/com/atstudio/atstudio/service/TagService.java:143` - per-value subquery construction binds both type and exact name.
- `src/main/java/com/atstudio/atstudio/service/TrackService.java:113` - the existing Track specification path composes all four categories.
- `src/main/java/com/atstudio/atstudio/service/TrackService.java:311` - repeated values use policy canonicalization before distinct without splitting commas or hashes.

Frontend production:

- `frontend/src/api/tags.ts:14` - available-tag request contract accepts four readonly arrays.
- `frontend/src/api/tags.ts:25` - repeated available-tag values use `URLSearchParams.append`.
- `frontend/src/api/tracks.ts:32` - Track request contract accepts four tag arrays.
- `frontend/src/api/tracks.ts:53` - Track queries append each exact tag value independently.
- `frontend/src/pages/public/HomePage.tsx:20` - fixed Usage, Genre, Mood, Instrument order and query-key mapping.
- `frontend/src/pages/public/HomePage.tsx:37` - selected values build canonical encoded TrackList links.
- `frontend/src/pages/public/HomePage.tsx:109` - registered/available loading, stale-result fence, first-result fallback, and failure isolation.
- `frontend/src/pages/public/HomePage.tsx:155` - wrapped ArrowLeft/ArrowRight/Home/End roving focus.
- `frontend/src/pages/public/HomePage.tsx:223` - loading, API failure, no taxonomy, no category taxonomy, no active result, and normal rendering.
- `frontend/src/pages/public/HomePage.tsx:472` - ARIA tablist, one tab stop, controls linkage, and tabpanel relationship.
- `frontend/src/pages/public/HomePage.module.css:293` - horizontal tab overflow.
- `frontend/src/pages/public/HomePage.module.css:616` - narrow-screen tab snap and bounded panel behavior.
- `frontend/src/pages/public/TrackListPage.tsx:74` - URL restoration with `getAll('instrument')` alongside the existing categories.
- `frontend/src/pages/public/TrackListPage.tsx:195` - four-category API request construction from canonical URL state.
- `frontend/src/pages/public/TrackListPage.tsx:260` - each availability refresh clears stale restrictions; current non-cancelled failure retains the show-all fallback behind generation and abort fences.
- `frontend/src/pages/public/TrackListPage.tsx:349` - Instrument selection rewrites only its repeated keys and returns to page 1.
- `frontend/src/pages/public/TrackListPage.tsx:379` - modal apply replaces all four tag groups atomically while preserving unrelated URL state.
- `frontend/src/pages/public/TrackListPage.tsx:543` - Instrument row, selected state, available state, and expansion control.
- `frontend/src/pages/public/TrackListPage.tsx:629` - accessible native `type="button"` opens the existing all-filter modal from the filter bar.
- `frontend/src/pages/public/TrackListPage.tsx:636` - reset removes all four tag types and BPM while retaining sort and other state.
- `frontend/src/components/filter/TagFilterModal.tsx:13` - modal contract includes Instrument tags and active values.
- `frontend/src/components/filter/TagFilterModal.tsx:94` - Instrument selection and four-group apply behavior.
- `frontend/src/components/ui/FilterChip.tsx:10` - native button with `aria-pressed` replaces a simulated clickable span.

Backend tests:

- `src/test/java/com/atstudio/atstudio/controller/TagControllerTest.java:120` - Instrument repetition and `dataList` response contract.
- `src/test/java/com/atstudio/atstudio/controller/TrackControllerTest.java:97` - Spring preserves repeated comma/hash/Korean tag values for AND search.
- `src/test/java/com/atstudio/atstudio/service/TagServiceBranchCoverageTest.java:64` - Instrument predicate and bound type/name parameter sequence.
- `src/test/java/com/atstudio/atstudio/service/TagServiceBranchCoverageTest.java:111` - NFC/space-equivalent names deduplicate before binding while comma/hash names remain atomic.
- `src/test/java/com/atstudio/atstudio/service/TagServiceAvailableTagsIntegrationTest.java:75` - active-only exact matching, special-character preservation, AND semantics, and one prepared query.
- `src/test/java/com/atstudio/atstudio/service/TrackServiceTest.java:219` - Track query normalization canonicalizes before distinct and passes comma/hash names as exact list items.

Frontend tests:

- `frontend/src/pages/public/HomePage.test.tsx:68` - category order, Usage visibility, first-result fallback, no License text, and roving focus.
- `frontend/src/pages/public/HomePage.test.tsx:103` - no registered tags.
- `frontend/src/pages/public/HomePage.test.tsx:110` - registered tags without an active Track result.
- `frontend/src/pages/public/HomePage.test.tsx:119` - module-local API failure and retry.
- `frontend/src/pages/public/HomePage.test.tsx:135` - Korean, spaces, comma, hash, encoding, and repeated AND values.
- `frontend/src/pages/public/HomePage.test.tsx:161` - eight-tag initial bound and explicit more/collapse.
- `frontend/src/pages/public/TrackListPage.test.tsx:393` - request-start and current-failure show-all behavior plus late superseded-response fencing.
- `frontend/src/pages/public/TrackListPage.test.tsx:444` - accessible modal entry, all four tag families, apply/reset, page 1, and unrelated sort retention.
- `frontend/src/pages/public/TrackListPage.test.tsx:517` - all four encoded URL types, API arrays, selected Instrument state, sort/page retention, removal, and reset.
- `frontend/src/api/domainApis.test.ts:483` - exact available-tag serialized query including Instrument and special characters.
- `frontend/src/api/domainApis.test.ts:508` - exact Track query serialization for all four tag arrays.
- `frontend/src/components/catalogComponents.test.tsx:20` - modal Instrument edit/apply path.
- `frontend/src/components/catalogComponents.test.tsx:61` - search and full four-category reset.
- `frontend/src/test/coverage/publicAuthShell.coverage.test.tsx:608` - SR-04 Home feed and unified tag navigation regression.
- `frontend/src/test/coverage/shellCatalogRouterGaps.coverage.test.tsx:818` - URL filters, every filter family including Instrument, reset, and pagination regression.
- `frontend/src/test/coverage/shellCatalogRouterGaps.coverage.test.tsx:1102` - exact current thumbnail error plus pending-to-valid square image dimension load.

The exact 25-file WI-021 inventory is recorded in `deliverables/user/WI-20260808-ATS-021-summary.md`.

Repair pass files (exactly 9):

- `frontend/src/pages/public/TrackListPage.tsx`
- `frontend/src/pages/public/TrackListPage.test.tsx`
- `frontend/src/test/coverage/shellCatalogRouterGaps.coverage.test.tsx`
- `src/main/java/com/atstudio/atstudio/service/TagService.java`
- `src/main/java/com/atstudio/atstudio/service/TrackService.java`
- `src/test/java/com/atstudio/atstudio/service/TagServiceBranchCoverageTest.java`
- `src/test/java/com/atstudio/atstudio/service/TrackServiceTest.java`
- `deliverables/user/WI-20260808-ATS-021-summary.md`
- `deliverables/agent/WI-20260808-ATS-021-evidence-pack.md`

## Commands & Outputs

### Focused Backend Verification

```powershell
.\gradlew.bat test --tests "com.atstudio.atstudio.service.TagServiceBranchCoverageTest" --tests "com.atstudio.atstudio.service.TagServiceAvailableTagsIntegrationTest" --tests "com.atstudio.atstudio.controller.TagControllerTest" --tests "com.atstudio.atstudio.controller.TrackControllerTest" --tests "com.atstudio.atstudio.service.TrackServiceTest"
```

- PASS: Gradle `BUILD SUCCESSFUL` in 50s.
- Exact XML totals: 5 classes, 69 tests, 0 failures, 0 errors, 0 skipped.
- Per class: `TagServiceBranchCoverageTest` 4, `TagServiceAvailableTagsIntegrationTest` 1, `TagControllerTest` 13, `TrackControllerTest` 26, `TrackServiceTest` 25.
- The integration fixture used isolated in-memory H2 and asserted exactly one prepared SQL statement for the available-tag call.

### Focused Frontend Verification

```powershell
npm test -- src/pages/public/HomePage.test.tsx src/pages/public/TrackListPage.test.tsx src/components/catalogComponents.test.tsx src/api/domainApis.test.ts
```

- PASS: 4 files, 35 tests, 0 failed; final Vitest duration 6.61s.

```powershell
npm test -- src/test/coverage/publicAuthShell.coverage.test.tsx -t "loads the home feed and follows album and tag exploration actions"
```

- PASS: 1 selected SR-04/Home test, 0 failed, 27 unselected tests reported as skipped.

```powershell
npm test -- src/test/coverage/shellCatalogRouterGaps.coverage.test.tsx -t "applies URL filters, toggles every filter family, clears search, and changes pages"
```

- PASS: 1 selected all-filter-family TrackList test, 0 failed, 25 unselected tests reported as skipped.

Final focused totals: 69 backend tests plus 35 frontend tests, all passing.

### Broader Regression Verification

```powershell
npm test -- src/test/coverage/publicAuthShell.coverage.test.tsx src/test/coverage/shellCatalogRouterGaps.coverage.test.tsx
```

- PASS: 2 files, 54 tests, 54 passed, 0 failed; Vitest duration 8.54s.
- The stale WI-020 thumbnail assertion now checks the exact current error contract and drives a selected image through `pending` to `valid` via a square dimension-load event.

### Static Verification

```powershell
npm run typecheck
```

- PASS: final `tsc --noEmit` exited 0.

```powershell
npm run lint
```

- PASS: full `frontend/src`, 0 errors and 0 warnings.

```powershell
npm run format
```

- PASS: the full frontend tree matches Prettier formatting.

```powershell
npx prettier --check ../deliverables/user/WI-20260808-ATS-021-summary.md ../deliverables/agent/WI-20260808-ATS-021-evidence-pack.md
```

- PASS: both WI deliverables match Prettier formatting.

```powershell
git diff --check
```

- PASS: no whitespace errors. Existing line-ending warnings may be emitted for already dirty Java files; no line-ending rewrite command was run.

The full backend suite, full frontend suite, build, coverage, and browser acceptance were intentionally not run under the explicit WI-021 verification boundary.

Repair-run diagnostics retained for reproducibility:

- Initial two-class backend run failed in `compileTestJava` because the new test omitted Mockito's `times` static import. The import was added; the repeated two-class run passed 29/29, and the final five-class run passed 69/69.
- Initial `npm run typecheck` failed with `TS2550` because the new test used `Array.at` beyond the configured lib target. It was replaced with indexed access; the final typecheck exited 0.

## Scope Preservation

- Branch and HEAD were confirmed as `codex/v1-release-rehearsal-fixes` at `c7f779df35e2175405d837230edf61962e2bae42`.
- Existing dirty WI-014 through WI-020 files were observed and preserved; WI-021 changes were layered only onto the scoped files.
- The intentional untracked `output/client-demo-screenshots-20260716-140514.zip` remains untouched at 700,703 bytes.
- No files were deleted, staged, or committed.
- No dependency, environment-secret, provider, payment, storage, application-network, or external-system operation occurred.
- No application or persistent database was connected to or mutated. The integration test created and discarded only an isolated in-memory H2 fixture.
- No raw user/domain value is interpolated into the available-tag SQL; types, names, and BPM values are bound.

## Risks / Rollback

Risks:

- Repeated query keys are now the canonical multi-tag API contract. A legacy external URL that encoded several tags as one CSV value is interpreted as one exact tag name; it must be converted to repeated keys to retain multi-tag behavior.
- The available-tag query executes once, but each selected exact tag adds an AND subquery. Normal UI selection sizes are bounded enough for this WI; unusually large direct requests remain a later performance/input-limit concern.
- Focused tests do not replace full suites, build, coverage, or real-browser keyboard/mobile visual acceptance assigned to later QA WIs.

Rollback:

1. Revert only the WI-021 portions of the 23 production/test files listed in the user summary and remove the two WI-021 deliverables, preserving all unrelated dirty changes in shared files.
2. Revert repeated tag-list binding, Instrument available filtering, and TrackService exact-value normalization as one backend unit with their focused tests.
3. Revert the unified Home module, four-category TrackList/Modal state, FilterChip accessibility change, API serialization, CSS, and frontend tests as one frontend unit.
4. No application schema, data, provider, storage, dependency, or external-system rollback is required. The in-memory H2 fixture has no residual state.

## Follow-ups

- WI-021 is complete and WI-022 is unblocked.
- Later planned WIs retain full-suite, build, coverage, browser acceptance, cross-layer QA, security review, and release gates.
