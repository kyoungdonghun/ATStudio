# QA Integration Review Result: WI-20260809-ATS-048

## Decision

- **FAIL**: two P2 blockers remain. PASS is not allowed while either is open.
- Findings: P0 `0`, P1 `0`, P2 `2`, P3 `2`.
- Review baseline: branch `codex/v1-release-rehearsal-fixes`, `HEAD 1f9bf39`, working diff against HEAD.
- Scope: CR-031-056, CR-031-057, Track slice of CR-031-061, bounded CR-031-063, and CR-031-064.

## Findings

### F-QAI-048-001 [P2] The deliberate iOS audio-picker workaround was regressed

- Traceability: `CR-031-056`.
- Evidence: `frontend/src/utils/validation.ts:129-142` still exports `isIOS()` but makes `AUDIO_ACCEPT` an unconditional string. Both Track inputs therefore always render `accept` at `frontend/src/pages/creator/TrackUploadPage.tsx:319` and `frontend/src/pages/creator/TrackEditPage.tsx:266`.
- HEAD comparison: `git show HEAD:frontend/src/utils/validation.ts` says iOS Safari can grey out valid MP3 files because of a UTI mismatch and conditionally returns `undefined` so the browser omits `accept` on iOS. The current diff removes that condition while leaving `isIOS()` otherwise unused by production code.
- Reproduction: render either form under an iPhone/iPad navigator. `AUDIO_ACCEPT` remains `.mp3,.wav,audio/mpeg,audio/wav,audio/x-wav`, so the input retains `accept` instead of using the deliberate iOS fallback.
- Test defect: `frontend/src/utils/validationHelpers.test.ts:67`, `TrackUploadPage.test.tsx:96`, and `TrackEditPage.test.tsx:218` assert the unconditional attribute. The separate `isIOS()` test at `validationHelpers.test.ts:80` never connects iOS detection to input rendering, so the suite mirrors the regression.
- Expected: MP3/WAV must remain the only JS-accepted and user-named formats, while iOS omits the native `accept` hint and still rejects/reset unsupported selections in JS.
- Minimal remediation: restore `AUDIO_ACCEPT = isIOS() ? undefined : '<MP3/WAV hints>'` (or an equivalent render-time helper), add an iOS case that proves the attribute is absent and M4A is rejected/reset, and keep the desktop MP3/WAV attribute assertion. Document the iOS exception so later contract tightening does not remove it again.

### F-QAI-048-002 [P2] Current-state Track API documentation still contradicts source

- Traceability: bounded `CR-031-063`; documentation gate shared by all WI-048 roots.
- Parameter mismatch: `docs/design/usecase/sound-track.md:337,345,348-350,362` names the ADMIN list query field `isActive`. The wire name is `is_active` in `TrackController.java:78` and `frontend/src/api/tracks.ts:116,128`.
- Response mismatch: `sound-track.md:367-378` omits `artistName`, `duration`, `likeCount`, and `downloadCount`, and describes `tags` as `List<String>`. The actual record at `AdminTrackListItemResponse.java:10-23` contains those four fields and `List<TagResponse>`; the frontend consumes the same shape at `frontend/src/api/tracks.ts:97-110`.
- Count mismatch: source recount returned `151` mappings (`GET 76`, `POST 41`, `PUT 20`, `DELETE 14`). `docs/design/api-spec.md:23-36` agrees, but line 333 still calls 150 the current backend count and line 791 says the recount should return 150.
- Reproduction: send `GET /api/tracks/admin?isActive=false`; the documented name does not bind the controller's `is_active` parameter, so the requested inactive filter is not applied. Run the documented mapping-count command; it returns 151 rather than its documented expected 150.
- Expected: current-state documents must use the exact query name and DTO fields and must contain one consistent reproducible mapping count.
- Minimal remediation: change the ADMIN query contract to `is_active`, distinguish it from response field `isActive`, list every `AdminTrackListItemResponse` field with `tags: List<TagResponse>`, and update both stale 150 references to 151.

### F-QAI-048-003 [P3] Functional hardening introduced unrelated Korean-to-Track copy drift

- Traceability: bounded `CR-031-063`; localization/copy ownership remains WI-058.
- Evidence: `frontend/src/pages/admin/TrackManagePage.tsx:216,223,246-247,269,336` replaces established Korean labels with mixed `Track` copy (`Track 관리`, `+ 새 Track`, `Track 제목 검색`, `등록된 Track이 없습니다`, `Track 삭제`). `frontend/src/layouts/AdminLayout.tsx:14` still names the same destination `음원 관리`, leaving one screen internally inconsistent with navigation.
- HEAD comparison: the changed page previously used `음원 관리`, `+ 새 음원`, `곡 제목 검색`, `등록된 음원이 없습니다`, and `음원 삭제`. No request, response, state-machine, or canonical-domain requirement depends on changing these labels.
- Impact: this is visible content regression and test churn unrelated to WI-048's functional contract, but it does not break the implemented operation; therefore it is nonblocking P3.
- Minimal remediation: restore the prior Korean user-facing copy and its tests, including the invalid-ID recovery action, or defer a coordinated terminology change to WI-058 with UX evidence.

### F-QAI-048-004 [P3] Two required counterexamples are implemented but not directly asserted

- Traceability: `CR-031-057`, `CR-031-064`.
- Tag intent: `TrackServiceTest.java:591-635` covers `true + IDs`, `true + absent IDs`, and omitted intent with IDs, but not explicit `replaceTags=false + tagIds`. Production code at `TrackService.java:205-210` is correct because only `Boolean.TRUE` replaces.
- Stale impact: `TagManagePage.test.tsx:206-258` covers unused, used-count, and impact-failure/retry states, but has no reversed deferred-response test for target A then target B. Production generation checks at `TagManagePage.tsx:149-181` appear coherent.
- Impact: no current implementation defect was found in either branch; this is nonblocking P3 because the missing tests protect explicitly named contract boundaries against future simplification.
- Minimal remediation: add one service/controller test for explicit false with nonempty IDs and one frontend deferred A/B test proving A's late impact cannot replace B or expose A's delete confirmation.

## Verified Contracts

- `CR-031-057`: `replaceTags=true` clears on absent IDs and replaces on IDs; omitted/false preserves. Multipart true binding is covered. Title, BPM, tonality, description clearing, and canonical Track ID handling agree across UI/controller/service.
- Track `CR-031-061`: malformed/missing IDs make zero Track/Tag calls. Album and Notice ID slices remain outside this WI.
- Bounded `CR-031-063`: URL normalization, draft/applied navigation, beyond-last recovery, load retry, delete failure/retry, pending fencing, and committed-refresh recovery are coherent. Generic latest-request ownership is still absent and correctly remains WI-053; this review does not report it as a defect.
- `CR-031-064`: deletion impact is ADMIN-only through authenticated fallback plus controller `@PreAuthorize`; 401/403/ADMIN tests exist. The DTO is bounded to Tag identity and count, the repository count is authoritative, stale generations are fenced, and failed/invalid impact does not expose delete confirmation.
- Existing Track soft-delete implementation and relationship cleanup were not changed. Existing Tag canonicalization and delete ordering remain intact.
- Existing tracked tests were extended rather than materially reduced. The untracked `TrackManagePage.test.tsx` is strict UTF-8 (`206` lines, no replacement character, Korean literals present).

## Focused Verification

- `npm test -- src/utils/validationHelpers.test.ts src/pages/creator/TrackUploadPage.test.tsx src/pages/creator/TrackEditPage.test.tsx src/pages/admin/TrackManagePage.test.tsx src/pages/admin/TagManagePage.test.tsx src/api/domainApis.test.ts` from `frontend/`: PASS, `6` files and `57` tests.
- The passing frontend run does not clear F-QAI-048-001 because its assertions encode the unconditional iOS `accept` regression.
- Backend focused tests were not rerun: Gradle's test task writes XML/HTML reporter artifacts, which this handoff forbids. Backend conclusions are from the actual source/test diff plus the prior evidence pack, not a newly claimed test run.

## Command Record

- Required inputs: `Get-Content -Encoding utf8` was used for every Tier 0/1/2, REQ/WI, exact changed-file, and additional implementation pointer in the handoff. Result: all required pointers loaded; no protected output or ignored environment file was read.
- Working state: `git status --short --untracked-files=all -- . ':(exclude)output/**'`, `git diff --name-status HEAD -- . ':(exclude)output/**'`, `git rev-parse --abbrev-ref HEAD`, and `git rev-parse --short HEAD`. Result: expected WI-048 tracked/untracked files, branch and HEAD recorded above; no Git mutation.
- Diff inspection: `git diff --unified=<context> HEAD -- <explicit WI-048 paths>` and `git show HEAD:<path>` over backend, frontend, tests, and docs. Result: actual working diff reviewed against HEAD; no ignored/protected path included.
- Source searches: focused `rg -n -C <context> <patterns> <explicit paths>` for CR roots, iOS, authorization, multipart intent, URL state, copy, DTO fields, and docs. Result: evidence lines cited above. One over-complex evidence `rg` invocation failed because PowerShell parsed backticks; it was rerun as simpler focused searches with exit 0.
- Mapping recount: the documented PowerShell controller annotation count plus verb grouping. Result: `151` total; GET `76`, POST `41`, PUT `20`, DELETE `14`.
- UTF-8 check: strict `UTF8Encoding(false, true)` decode of `frontend/src/pages/admin/TrackManagePage.test.tsx`. Result: valid UTF-8, `206` lines, `102` Korean literal characters.
- `git diff --check HEAD -- . ':(exclude)output/**'`: PASS; only existing CRLF-to-LF working-copy warnings were printed.
- Focused Vitest command: PASS as recorded above. No coverage, file reporter, formatter, full suite, external service, or destructive test was run.

## Safety Statement

- No implementation, test, current-state documentation, data, schema, or Git state was edited. The only write is this required review result.
- No real Track or Tag deletion, persistent DB mutation, Provider/mail/download action, or other external side effect occurred.
- `output/client-demo-screenshots-20260716-140514.zip` and `output/ui-ux-audit/` were not opened, touched, hashed, or traversed. Ignored secrets and local environment values were not inspected.
