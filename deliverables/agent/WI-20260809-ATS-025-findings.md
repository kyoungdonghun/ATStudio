# Findings: WI-20260809-ATS-025

## Audit Boundary

- Scope rows: `CRT-01`, `CRT-02`, `CRT-03`, `CRT-04`, `CRT-05`, `ADM-07`, `ADM-08`, `ADM-12`, and `ADM-13`.
- Guard scope: the `G-ADMIN` anonymous sublane for the same nine routes, plus the authenticated wrong-role and authenticated ADMIN sublanes.
- Severity follows `docs/audit/full-system-audit-20260713.md:73-80`: `P1` is reserved for high-impact security, durability, or core-journey defects; `P2` covers material correctness, availability, UX, traceability, and maintainability defects; `P3` covers low-risk cleanup or semantic alignment.
- This was a documentation-only, read-only audit. No product code, test, configuration, schema, fixture, baseline document, or runtime implementation was changed.
- No mutation, upload, download, database, storage, provider, mail, payment, secret, or environment operation was performed. Authenticated ADMIN UI, API invocation, server response, and durable projection evidence remained blocked.
- The intentional archive `output/client-demo-screenshots-20260716-140514.zip` was preserved and was not inspected.
- No authenticated live responsive claim is made. Source review can identify missing controls and race paths, but it does not prove visual clipping or the absence of clipping.
- No screenshot, authenticated observation, server response, or durable-state result is invented in this report.

## Row Classification

| Row                                        | Result    | Reason                                                                                                                                                                                                                                                        |
| ------------------------------------------ | --------- | ------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| `G-ADMIN` anonymous sublane                | `PASS`    | All nine routes use the shared ADMIN guard and preserve an encoded local `returnTo` in the Login redirect. Route ownership is at `frontend/src/router/index.tsx:118-120,209-235`; redirect construction is at `frontend/src/router/ProtectedRoute.tsx:34-60`. |
| `G-ADMIN` authenticated wrong-role sublane | `BLOCKED` | No approved authenticated non-ADMIN fixture or browser session was available. Source behavior was not substituted for a live role result.                                                                                                                     |
| `G-ADMIN` authenticated ADMIN sublane      | `BLOCKED` | No approved ADMIN fixture or session was available, so authenticated UI, mutations, API/server results, and durable projections were not run.                                                                                                                 |
| `CRT-01`                                   | `FAIL`    | Track upload has a client/server audio-format mismatch and confirmed accessibility and recovery defects.                                                                                                                                                      |
| `CRT-02`                                   | `FAIL`    | Track edit cannot explicitly clear all Tags, can silently preserve blanked required-looking metadata, accepts unsupported audio formats, and mishandles malformed IDs.                                                                                        |
| `CRT-03`                                   | `FAIL`    | Album management has clearing, stale-detail, pagination, and modal thumbnail-contract defects.                                                                                                                                                                |
| `CRT-04`                                   | `FAIL`    | Album creation has thumbnail validation ordering and object-URL lifecycle defects.                                                                                                                                                                            |
| `CRT-05`                                   | `FAIL`    | Album edit sends an invalid one-based reorder payload and has search, refetch, thumbnail, and malformed-ID defects.                                                                                                                                           |
| `ADM-07`                                   | `FAIL`    | Tag management lacks recovery and keyboard completion, uses inconsistent language, and does not disclose destructive association removal.                                                                                                                     |
| `ADM-08`                                   | `FAIL`    | Track management contradicts the soft-delete durability contract and has stale request, URL, filter, semantic, action, and error-state gaps.                                                                                                                  |
| `ADM-12`                                   | `FAIL`    | Notice creation has validation, language, labeling, pending-state, and public attachment-boundary defects.                                                                                                                                                    |
| `ADM-13`                                   | `FAIL`    | Notice edit has the same form/attachment defects, malformed-ID handling, view-count side effects during admin loading, and incomplete public download feedback.                                                                                               |

In-scope row totals: `FAIL 9`. Guard sublane totals: `PASS 1`, `BLOCKED 2`. A `FAIL` means at least one required state for the row has a confirmed defect; it does not mean every behavior in that row failed.

## Confirmed Findings

### F-UI-025-001 - Track soft delete destroys history and relationship records

- Severity: `P1`
- Rows: `CRT-02`, `ADM-08`
- Evidence: frontend confirmation at `frontend/src/pages/admin/TrackManagePage.tsx:229-243`; implementation at `src/main/java/com/atstudio/atstudio/service/TrackService.java:217-228`; preservation rationale at `docs/retrospective/domain-design.md:38-46`; canonical delete flow at `docs/design/usecase/sound-track.md:287-313`.
- Evidence lanes: UI wording `CONFIRMED FROM SOURCE`; API invocation `CONFIRMED FROM SOURCE`; server behavior `CONFIRMED FROM SOURCE`; durable execution `NOT RUN`.
- Observation: The frontend states that deletion is deactivation or soft delete. `TrackService.deleteTrack`, however, deletes Likes, Download History, Licenses, Playlist memberships, Album memberships, and Tag mappings before deactivating the Track. The domain design says Track soft deletion preserves Download History and Licenses, while SOUND-016 documents only Tag mapping deletion in addition to `is_active=0`.
- Impact: An ADMIN delete request can erase entitlement and historical relationship records that the user-facing text and design classify as preserved. It can also remove Tracks from Albums and Playlists rather than merely suppressing their active public projection.
- Follow-up: Stop destructive relationship cleanup until an approved retention contract is implemented. Preserve License and Download History records, define Album/Playlist inactive-member behavior explicitly, align SOUND-016 and the confirmation copy, and add transactional durability tests before re-enabling the action.

### F-UI-025-002 - Album reorder always sends a one-based contract rejected by the backend

- Severity: `P1`
- Rows: `CRT-05`
- Evidence: `frontend/src/pages/creator/AlbumEditPage.tsx:164-184`; `src/main/java/com/atstudio/atstudio/service/AlbumService.java:238-263`; `docs/design/usecase/sound-album.md:208-220`; `frontend/src/test/coverage/publicAuthShell.coverage.test.tsx:937-943`.
- Evidence lanes: UI interaction `CONFIRMED FROM SOURCE`; API payload `CONFIRMED FROM SOURCE`; server validation `CONFIRMED FROM SOURCE`; persisted order `BLOCKED`.
- Observation: `AlbumEditPage` maps reordered members to `order: i + 1`. The backend requires every current member exactly once with the contiguous order set `0..n-1`, matching ALBUM-008. The broad frontend test asserts only that `reorderAlbumTracks` was called and therefore passes without checking the invalid payload.
- Impact: Every non-empty reorder request can be rejected. The optimistic UI briefly projects an order that cannot become canonical, while the passing test creates a false-positive contract signal.
- Follow-up: Send `order: i`, assert the complete payload in a dedicated page test, and verify success, rejection rollback, refetch failure, and public Album order after an approved durable mutation.

### F-UI-025-003 - Track forms advertise audio formats the backend rejects

- Severity: `P2`
- Rows: `CRT-01`, `CRT-02`
- Evidence: `frontend/src/utils/validation.ts:142-154`; `frontend/src/pages/creator/TrackUploadPage.tsx:116-140`; `frontend/src/pages/creator/TrackEditPage.tsx:135-150`; `src/main/java/com/atstudio/atstudio/service/audio/AudioAnalysisFormat.java:10-21`; `src/main/java/com/atstudio/atstudio/service/audio/AudioAnalysisService.java:44-50`.
- Evidence lanes: UI acceptance `CONFIRMED FROM SOURCE`; API submission path `CONFIRMED FROM SOURCE`; server rejection contract `CONFIRMED FROM SOURCE`; storage/DB mutation `NOT RUN`.
- Observation: The frontend accepts and names MP3, WAV, M4A, AAC, FLAC, and OGG. Backend audio analysis recognizes only MP3 and WAV and rejects every other filename extension as `UNSUPPORTED_FORMAT` before decoding.
- Impact: Users can complete client validation for four formats that are guaranteed to fail at the server, including during multi-upload where the mismatch produces avoidable partial-success recovery work.
- Follow-up: Make the accepted-format list a shared contract. Until backend support is deliberately added and tested, restrict both file inputs and messages to MP3/WAV and surface the server error on the affected Track entry.

### F-UI-025-004 - Track edit cannot clear all Tags and silently omits blank required-looking metadata

- Severity: `P2`
- Rows: `CRT-02`
- Evidence: `frontend/src/pages/creator/TrackEditPage.tsx:128-133,152-182,245-283`; `src/main/java/com/atstudio/atstudio/service/TrackService.java:167-176,200-210`; `src/main/java/com/atstudio/atstudio/entity/Track.java:73-77`; `src/main/java/com/atstudio/atstudio/dto/track/TrackUpdateRequest.java:17-34`.
- Evidence lanes: UI state `CONFIRMED FROM SOURCE`; multipart request shape `CONFIRMED FROM SOURCE`; server null-preserve behavior `CONFIRMED FROM SOURCE`; resulting row state `NOT RUN`.
- Observation: Tags are appended one field per selected ID. When the user deselects every Tag, no `tagIds` field is sent, so the backend receives `null` and preserves every old association. Title, BPM, and tonality are styled as required, but the controls can be blanked and the submit handler silently omits those fields; entity null handling then preserves the old values.
- Impact: A save can appear successful while the canonical Track still contains Tags or metadata the ADMIN explicitly removed from the form.
- Follow-up: Encode an explicit empty Tag collection, validate required metadata before submission, and distinguish omitted/preserve from explicit clear in the API contract and focused tests.

### F-UI-025-005 - AlbumManage has clearing, stale modal, pagination, and thumbnail-contract defects

- Severity: `P2`
- Rows: `CRT-03`
- Evidence: `frontend/src/pages/creator/AlbumManagePage.tsx:37-50,56-115,137-140,203-246`; null-preserve behavior at `src/main/java/com/atstudio/atstudio/entity/Album.java:47-50`; route validation/preview contrast at `frontend/src/pages/creator/AlbumCreatePage.tsx:28-57,123-138` and `frontend/src/pages/creator/AlbumEditPage.tsx:190-223,294-313`.
- Evidence lanes: modal/list behavior `CONFIRMED FROM SOURCE`; API request construction `CONFIRMED FROM SOURCE`; server null-preserve behavior `CONFIRMED FROM SOURCE`; durable Album state `BLOCKED`.
- Observation: A blank modal description is omitted, so update semantics preserve the old description and cannot clear it. The edit modal opens before detail loading, does not reset description first, and has no latest-request fence; stale content or a late response from another Album can populate the active modal. The page requests only `size: 100` and exposes no pagination. Its modal thumbnail control accepts any `image/*` file without the route pages' size/dimension validation or preview.
- Impact: ADMINs can edit or submit stale Album data, fail to clear a description, omit Albums beyond the first 100 from management, and receive materially different validation depending on which Album entry point they use.
- Follow-up: Reset and identify modal state before loading, fence detail requests, always encode explicit description updates, paginate the management list, and reuse one thumbnail selection/preview contract across modal and route forms.

### F-UI-025-006 - Album thumbnail validation is not fenced from selection or submission races

- Severity: `P2`
- Rows: `CRT-04`, `CRT-05`
- Evidence: `frontend/src/pages/creator/AlbumCreatePage.tsx:1-81`; `frontend/src/pages/creator/AlbumEditPage.tsx:190-246`.
- Evidence lanes: UI validation flow `CONFIRMED FROM SOURCE`; request construction `CONFIRMED FROM SOURCE`; server response `BLOCKED`; storage/DB result `BLOCKED`.
- Observation: Create and edit await asynchronous dimension validation without a pending state or selection-generation fence. A later selection can be overwritten by an earlier validation completion, and immediate submit can run before validation installs the selected file. Object URLs are revoked only when another accepted file replaces them or the input becomes empty; neither page guarantees cleanup on unmount or navigation.
- Impact: The request can omit the just-selected thumbnail or retain stale validation/error state, and repeated entry/exit can retain blob URLs in browser memory.
- Follow-up: Track a selection generation and explicit pending status, disable submit while validation is pending, ignore stale completions, and revoke every preview URL on replacement, rejection, clear, and unmount.

### F-UI-025-007 - Album Track search misstates its contract and lacks request and combobox controls

- Severity: `P2`
- Rows: `CRT-05`
- Evidence: search/refetch logic at `frontend/src/pages/creator/AlbumEditPage.tsx:97-122`; search UI at `frontend/src/pages/creator/AlbumEditPage.tsx:327-379`; canonical keyword contract at `docs/design/usecase/sound-track.md:58-71`.
- Evidence lanes: UI copy and semantics `CONFIRMED FROM SOURCE`; API invocation `CONFIRMED FROM SOURCE`; search server contract `CONFIRMED FROM DOCUMENTED SOURCE`; durable Album membership `BLOCKED`.
- Observation: The placeholder promises Track title or artist search, while the Track contract searches title plus associated `USAGE` Tag names only. Rapid searches have no abort or latest-request fence, and the input/dropdown lacks combobox, listbox, active-option, and keyboard-navigation semantics. Membership refetch errors are explicitly swallowed, including after add, remove, and reorder paths.
- Impact: Search results can contradict the UI copy or be overwritten by a late request. Keyboard and assistive-technology users cannot operate the result popup reliably, and a successful mutation can leave a stale on-screen membership list without disclosure.
- Follow-up: Align copy with title plus Usage, add latest-request fencing and accessible combobox behavior, and make refetch failure a recoverable partial-success state.

### F-UI-025-008 - Edit routes do not validate IDs before loading or mutation

- Severity: `P2`
- Rows: `CRT-02`, `CRT-05`, `ADM-13`
- Evidence: `frontend/src/pages/creator/TrackEditPage.tsx:52-91,117-126,153-186,196-201`; `frontend/src/pages/creator/AlbumEditPage.tsx:27-64,75-84,225-261`; `frontend/src/pages/admin/NoticeEditPage.tsx:23-50,96-138`.
- Evidence lanes: route/UI behavior `CONFIRMED FROM SOURCE`; API argument construction `CONFIRMED FROM SOURCE`; server response `NOT RUN`; durable state `NOT RUN`.
- Observation: Missing IDs return before the initial `true` loading state is cleared. Non-numeric IDs are passed through `Number(...)` without finite positive validation and can invoke read, update, delete, or membership APIs with `NaN`.
- Impact: Malformed or incompletely mounted routes can remain on an unrecoverable loading screen or emit malformed requests instead of a bounded invalid/not-found state.
- Follow-up: Parse each route parameter once, require a finite positive integer before setting loading, and render a terminal error with safe navigation. Reuse the validated numeric ID for every read and mutation.

### F-UI-025-009 - Track upload and edit have accessibility and retry recovery gaps

- Severity: `P2`
- Rows: `CRT-01`, `CRT-02`
- Evidence: Tag-load and file recovery at `frontend/src/pages/creator/TrackUploadPage.tsx:89-114,116-175`; progress/header/remove controls at `frontend/src/pages/creator/TrackUploadPage.tsx:297-375`; form labels and Tags at `frontend/src/pages/creator/TrackUploadPage.tsx:386-503`; edit file/field controls at `frontend/src/pages/creator/TrackEditPage.tsx:135-150,211-295`; shared Tag implementation at `frontend/src/components/ui/Tag.tsx:3-17`.
- Evidence lanes: UI and accessibility tree `CONFIRMED FROM SOURCE`; API recovery path `CONFIRMED FROM SOURCE`; server response `NOT RUN`; durable state `NOT RUN`.
- Observation: Track headers expand only on pointer click; the symbol-only remove button has no accessible name; multiple labels are neither associated with a control nor wrapping it. `Tag` renders a focusable `span role="button"` without keyboard activation or `aria-pressed`. Upload Tag-load failure is swallowed. Oversize-audio rejection does not consistently clear the file input, so choosing the same file can fail to fire another change event. Upload progress is visible text but has no live status semantics.
- Impact: Keyboard and assistive-technology users cannot reliably operate core form controls or learn progress, while transient Tag and same-file failures have no clear recovery path.
- Follow-up: Use semantic buttons and associated labels, name remove controls, implement keyboard and pressed-state semantics for Tag selection, expose Tag retry, reset rejected file inputs consistently, and announce per-file and aggregate upload state through live regions.

### F-UI-025-010 - TrackManage has stale request, URL, semantic, action, and recovery gaps

- Severity: `P2`
- Rows: `ADM-08`
- Evidence: URL/filter and load logic at `frontend/src/pages/admin/TrackManagePage.tsx:28-79`; delete/error flow at `frontend/src/pages/admin/TrackManagePage.tsx:82-97`; filter/search/table/actions at `frontend/src/pages/admin/TrackManagePage.tsx:116-220`; frontend keyword invocation at `frontend/src/api/tracks.ts:113-134`; incomplete SOUND-021 parameters at `docs/design/usecase/sound-track.md:330-346`.
- Evidence lanes: UI and URL state `CONFIRMED FROM SOURCE`; API invocation `CONFIRMED FROM SOURCE`; server response `BLOCKED`; durable Track projection `BLOCKED`.
- Observation: Concurrent list loads have no abort or latest-request fence. `page` and `filter` URL values are not normalized, and `searchInput` is initialized from the URL only once, so browser navigation can desynchronize visible and active filters. The filter/search controls and table action column have incomplete semantics; only a missing thumbnail gets a fallback, not a failed image. Load errors expose no retry, delete failure closes the modal and moves the error to the page, and post-delete refetch is not awaited. SOUND-021 documents page, size, and active status but omits the implemented keyword query.
- Impact: Late data can replace the current filter/page, malformed URLs can issue invalid requests, visible filter state can misrepresent the request, and action failures are difficult to recover from or associate with the attempted Track.
- Follow-up: Normalize URL state, synchronize the input, fence requests, add control/table semantics and broken-image fallback, keep mutation errors local with retry, await refresh, and update SOUND-021 to own the keyword contract.

### F-UI-025-011 - TagManage obscures recovery and destructive association impact

- Severity: `P2`
- Rows: `ADM-07`
- Evidence: robust local canonicalization and conflict handling at `frontend/src/pages/admin/TagManagePage.tsx:92-142`; load/error behavior at `frontend/src/pages/admin/TagManagePage.tsx:51-62,169-183`; English UI and modal controls at `frontend/src/pages/admin/TagManagePage.tsx:188-255,257-318`; delete confirmation at `frontend/src/pages/admin/TagManagePage.tsx:320-344`; destructive service behavior at `src/main/java/com/atstudio/atstudio/service/TagService.java:162-168`.
- Evidence lanes: UI behavior `CONFIRMED FROM SOURCE`; API invocation `CONFIRMED FROM SOURCE`; server association deletion `CONFIRMED FROM SOURCE`; durable execution `NOT RUN`.
- Observation: The page and dialogs use English labels in a Korean product, load failure has no retry, and Enter in the name field does not submit because the modal is not a form and the input has no Enter handler. Delete confirmation gives no use count or warning that the service removes every Track association before deleting the Tag. In contrast, client duplicate detection, normalization, server-race conflict mapping, and local field errors are relatively robust.
- Impact: A transient read failure strands the page, keyboard completion is inefficient, and an ADMIN cannot assess the discovery/filter impact of deleting a used Tag.
- Follow-up: Localize the surface, render retry, use a semantic form with Enter submit, and add a dependency/use-count preview with explicit association-removal wording while preserving the existing canonicalization and conflict behavior.

### F-UI-025-012 - Notice create, edit, and public download states are incomplete

- Severity: `P2`
- Rows: `ADM-12`, `ADM-13`
- Evidence: `frontend/src/pages/admin/NoticeCreatePage.tsx:18-99,105-220`; edit load/mutation state at `frontend/src/pages/admin/NoticeEditPage.tsx:23-128`; edit loading, form, and dialog UI at `frontend/src/pages/admin/NoticeEditPage.tsx:132-263`; public attachment action at `frontend/src/pages/public/NoticeDetailPage.tsx:81-96` and `frontend/src/api/notices.ts:76-91`; backend ADMIN fetch side effect at `src/main/java/com/atstudio/atstudio/service/NoticeService.java:84-93`.
- Evidence lanes: UI behavior `CONFIRMED FROM SOURCE`; frontend API invocation `CONFIRMED FROM SOURCE`; server view-count path `CONFIRMED FROM SOURCE`; attachment download and durable mutation `NOT RUN`.
- Observation: Notice create/edit mixes English UI with the Korean product, does not consistently enforce the content maximum in the controls, leaves labels incompletely associated, and permits conflicting file/save/delete interactions because pending states are separate or not applied to adjacent controls. The ADMIN edit page loads through the public Notice detail read, whose service path increments view count. Public attachment download exposes no per-file pending state or bounded local error.
- Impact: ADMIN inspection can change a public metric before any edit, users can issue duplicate/conflicting actions, validation can diverge by layer, and public download failures are silent or ambiguous.
- Follow-up: Add one localized and associated form contract, enforce content and attachment limits on both layers, coordinate pending states, provide per-file download feedback, and add a non-counting ADMIN detail endpoint or explicit read mode.

### F-UI-025-013 - ADMIN uploads can publish unvalidated active file types under a public static path

- Severity: `P1`
- Rows: `CRT-03`, `CRT-04`, `CRT-05`, `ADM-12`, `ADM-13`
- Evidence: Album public storage calls at `src/main/java/com/atstudio/atstudio/service/AlbumService.java:48-61,112-128`; Notice attachment storage at `src/main/java/com/atstudio/atstudio/service/NoticeService.java:167-189`; extension and public-root handling at `src/main/java/com/atstudio/atstudio/service/storage/LocalStorageService.java:27,58-73,201-212`; public resource exposure at `src/main/java/com/atstudio/atstudio/config/WebConfig.java:20-25` and `src/main/java/com/atstudio/atstudio/config/SecurityConfig.java:83-89`; Notice response/download boundary at `src/main/java/com/atstudio/atstudio/dto/notice/NoticeAttachmentResponse.java:1-30`, `frontend/src/api/notices.ts:76-91`, and `src/main/java/com/atstudio/atstudio/controller/NoticeController.java:80-91`.
- Evidence lanes: frontend selection `CONFIRMED FROM SOURCE`; upload API path `CONFIRMED FROM SOURCE`; server/storage mechanism `CONFIRMED FROM SOURCE`; exploit execution and deployed durable projection `NOT RUN`.
- Observation: Album thumbnails and Notice attachments lack authoritative backend size, media-type, signature, and approved decoded-image dimension/canonicalization validation before being stored under `PUBLIC`. `LocalStorageService` accepts an arbitrary alphanumeric extension, including `html`, and `/uploads/**` is served as a public resource path. This is an ADMIN-only upload boundary, so the finding does not claim anonymous upload capability. `NoticeAttachmentResponse` exposes only `id`, `originalName`, and `fileSize`, not the raw public storage key, which reduces discoverability. The frontend intentionally downloads with `originalName`, while `NoticeController` serves the generated Resource filename as `application/octet-stream` with attachment disposition. Direct raw-path exploitation of a Notice attachment was `NOT PROVEN`; the unsafe public-root policy remains, and the Album thumbnail URL is exposed.
- Impact: A compromised or malicious ADMIN account can place active non-media content under a trusted public root, creating a conditional stored-content, content-sniffing, abuse, and resource-exhaustion risk. The ADMIN-only precondition and undisclosed Notice storage key lower Notice reachability, but they do not make the public-root policy safe; no direct Notice raw-path exploit is claimed.
- Follow-up: Add backend allowlists, byte limits, signature decoding, approved decoded-image dimension and canonicalization rules, generated safe extensions, and `nosniff` controls. Keep attachment disposition behavior explicit, separate private Notice attachment storage from public image projection where appropriate, and test HTML/polyglot, oversized, and mismatched-MIME cases without assuming a square Album-image policy.

### F-UI-025-014 - Dedicated page tests are missing and the broad reorder test is a false positive

- Severity: `P2`
- Rows: `CRT-01`, `CRT-02`, `CRT-03`, `CRT-04`, `CRT-05`, `ADM-07`, `ADM-08`, `ADM-12`, `ADM-13`
- Evidence: required page owners and test globs at `deliverables/agent/WI-20260809-ATS-025-handoff.md:114-122,140-141`; dedicated tests at `frontend/src/pages/creator/TrackUploadPage.test.tsx`, `frontend/src/pages/creator/TrackEditPage.test.tsx`, and `frontend/src/pages/admin/TagManagePage.test.tsx`; broad reorder assertion at `frontend/src/test/coverage/publicAuthShell.coverage.test.tsx:937-943`; one-based API pass-through example at `frontend/src/api/domainApis.test.ts:82-85`; backend order validation at `src/main/java/com/atstudio/atstudio/service/AlbumService.java:238-263`.
- Evidence lanes: test source `CONFIRMED`; UI/API/server runtime `NOT RUN`; durable state `NOT RUN`.
- Observation: Dedicated tests exist for `TrackUploadPage`, `TrackEditPage`, and `TagManagePage`. Dedicated tests are absent for `AlbumManagePage`, `AlbumCreatePage`, `AlbumEditPage`, `TrackManagePage`, `NoticeCreatePage`, and `NoticeEditPage`. The broad Album test checks only that reorder was called, while the API test accepts a one-based pass-through example without owning the backend contract. A green aggregate suite therefore does not prove the required request shape, state transitions, accessibility, or durable outcome.
- Impact: Core ADMIN regressions in the six uncovered pages can pass CI, and one existing test path specifically permits the invalid Album reorder contract.
- Follow-up: Add dedicated tests for every page and guard state. Assert exact payloads, stale-response fencing, invalid IDs, empty/clear semantics, pending conflicts, keyboard/focus behavior, error recovery, and public projection boundaries against backend-owned contracts.

### F-UI-025-015 - Responsive authenticated behavior remains a review item, not a visual defect

- Severity: `P3`
- Status: `REVIEW`
- Rows: `CRT-01`, `CRT-02`, `CRT-03`, `CRT-04`, `CRT-05`, `ADM-07`, `ADM-08`, `ADM-12`, `ADM-13`
- Evidence: required viewports and states at `deliverables/agent/WI-20260809-ATS-020-acceptance-matrix.md:189-211`; responsive source owners include `frontend/src/layouts/AdminLayout.module.css`, `frontend/src/pages/admin/TrackManagePage.module.css`, and the creator/admin form CSS modules.
- Evidence lanes: source review `CONFIRMED`; live authenticated viewport UI `BLOCKED`; API/server/durable state `NOT APPLICABLE`.
- Observation: The shell and table styles include mobile rules and the form layouts use fluid source constraints. No authenticated live viewport evidence was available at `1440x900`, `1024x768`, `390x844`, or `360x800`. Source alone cannot establish clipping, occlusion, focus visibility, or stable dimensions during live state transitions.
- Impact: Responsive acceptance remains unknown; no visual defect should be reported merely from the absence of live evidence.
- Follow-up: After an approved ADMIN session exists, run all four viewports across loading, error, modal, file, table, and pending states and record actual screenshots and accessibility observations.

## Blocked / Not Run Coverage

| Coverage                                                                           | Result                      | Boundary                                                                                                                                              |
| ---------------------------------------------------------------------------------- | --------------------------- | ----------------------------------------------------------------------------------------------------------------------------------------------------- |
| Anonymous guard for the nine in-scope routes                                       | `PASS`                      | Each route redirected to Login with an encoded local `returnTo`; no new screenshot is claimed in this report.                                         |
| Authenticated wrong-role guard                                                     | `BLOCKED`                   | Missing approved authenticated non-ADMIN session/fixture.                                                                                             |
| Authenticated ADMIN populated, empty, invalid/not-found, and error UI              | `BLOCKED`                   | Missing approved ADMIN session and controlled fixtures.                                                                                               |
| ADMIN create, update, delete, add, remove, reorder, upload, and attachment actions | `NOT RUN`                   | Durable mutations and uploads were forbidden.                                                                                                         |
| API/server response evidence for authenticated actions                             | `BLOCKED`                   | No authorized invocation was performed; source ownership is not reported as a live response.                                                          |
| Database, storage, and public durable projection                                   | `BLOCKED`                   | Database/storage inspection and mutation were forbidden. Public list/detail/order/playback/filter/download outcomes were not inferred from form code. |
| Notice/public attachment download                                                  | `NOT RUN`                   | Downloads were forbidden and no approved safe fixture was supplied.                                                                                   |
| Responsive authenticated live evidence                                             | `BLOCKED`                   | No ADMIN browser session; source does not prove or disprove clipping.                                                                                 |
| Provider, mail, payment, secret, and environment boundaries                        | `NOT RUN`                   | Outside the approved read-only audit boundary.                                                                                                        |
| Demo ZIP                                                                           | `PRESERVED / NOT INSPECTED` | `output/client-demo-screenshots-20260716-140514.zip` was not opened, moved, replaced, or used as a fixture.                                           |

## Remediation Order

1. **P1 durability and security:** correct Track deletion to preserve contracted history and entitlements; fix Album reorder to exact zero-based contiguous orders; close the ADMIN-to-public file boundary with authoritative validation and safe serving.
2. **P2 request correctness:** align audio formats, make Track Tag/metadata clearing explicit, validate route IDs, and repair Album description, modal, search, pagination, and thumbnail race contracts.
3. **P2 interaction and recovery:** fix Track and Notice accessibility, pending-state coordination, stale-request fencing, retry/error placement, and non-counting ADMIN Notice reads.
4. **P2 contract ownership:** update SOUND-016 and SOUND-021, align search and file copy with backend behavior, and add dedicated page tests that assert exact API contracts and state transitions.
5. **P3 review:** execute authenticated responsive coverage only after an approved ADMIN session and fixtures exist; promote only observed defects, not source-only speculation.
