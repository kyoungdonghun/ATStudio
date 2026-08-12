# Findings: WI-20260809-ATS-024

## Audit Boundary

- Scope guards: `G-AUTH`, `G-SUB`, and `G-QUESTION`.
- Member rows: `MEM-01`, `MEM-02`, `MEM-03`, `MEM-05`, `MEM-06`, `MEM-07`, `MEM-08`, `MEM-09`, `MEM-15`, `MEM-16`, and `MEM-17`.
- Shared rows: `SH-03`, `SH-04`, and `SH-05`.
- The audit was read-only. No durable mutation, download, upload, provider, mail, payment, secret, storage, database, or test-data operation was performed.
- The intentional archive `output/client-demo-screenshots-20260716-140514.zip` was preserved.

## Row Classification

| Row          | Result    | Reason                                                                                                                               |
| ------------ | --------- | ------------------------------------------------------------------------------------------------------------------------------------ |
| `G-AUTH`     | `PASS`    | Anonymous `ProtectedRoute` paths preserved encoded `returnTo` and emitted one warning.                                               |
| `G-SUB`      | `FAIL`    | Anonymous `SubscriberRoute` paths redirected to bare `/login`; fresh navigation also reproduced duplicate toast behavior.            |
| `G-QUESTION` | `BLOCKED` | Authenticated and admin variants required an authenticated QA browser session that was unavailable.                                  |
| `MEM-01`     | `FAIL`    | Playlist list/detail/edit and shared drawer findings include contract, accessibility, loading, and partial-save defects.             |
| `MEM-02`     | `FAIL`    | Playlist create/delete and plan-limit paths inherit confirmed member playlist defects.                                               |
| `MEM-03`     | `FAIL`    | Playlist track action and reorder paths contain confirmed interaction and contract defects.                                          |
| `MEM-05`     | `FAIL`    | Like list loading is not protected against stale responses; shared like entry behavior also loses origin.                            |
| `MEM-06`     | `FAIL`    | Shared history and loading/accessibility observations include confirmed quality defects.                                             |
| `MEM-07`     | `FAIL`    | License list/detail stale and invalid-ID loading paths remain defective.                                                             |
| `MEM-08`     | `FAIL`    | License detail is included in the invalid-ID and dedicated-test-gap findings.                                                        |
| `MEM-09`     | `FAIL`    | Download-related shared/player and page accessibility findings remain open.                                                          |
| `MEM-15`     | `FAIL`    | Question list/create/detail loading, attachment, accessibility, and test gaps are confirmed.                                         |
| `MEM-16`     | `FAIL`    | Question ownership/status behavior is inconsistent with the backend contract.                                                        |
| `MEM-17`     | `FAIL`    | Question detail attachment and owner-delete controls contain confirmed defects.                                                      |
| `SH-03`      | `FAIL`    | HistoryModal visual and Escape/focus checks passed, but label and control accessibility defects remain.                              |
| `SH-04`      | `FAIL`    | PlaylistDrawer visual layout passed at all audited viewports, but reorder, dialog, mutation, race, and accessibility defects remain. |
| `SH-05`      | `FAIL`    | AddToPlaylistModal has blank loading, no retry, and silent subscription-expiry close behavior.                                       |

Totals: `PASS 1`, `FAIL 15`, `BLOCKED 1`, `N/A 0`. A `FAIL` means that at least one required state for the row has a confirmed defect; it does not mean that every core behavior in the row failed.

## Confirmed Findings

### F-UI-024-001 - PlaylistDrawer sends the wrong reorder contract

- Severity: `P1`
- Rows: `MEM-01`, `MEM-03`, `SH-04`
- Evidence: `frontend/src/components/player/PlaylistDrawer.tsx:184-199`; `src/main/java/com/atstudio/atstudio/service/PlaylistService.java:334-363`; `frontend/src/components/player/playerComponents.test.tsx:223-237`; correct edit-page payload at `frontend/src/pages/subscriber/PlaylistEditPage.tsx:115-124`.
- Observation: The drawer optimistic projection and request send `trackOrder: i + 1`. The backend requires the exact order set `0..n-1`; the drawer test asserts the same incorrect one-based payload. `PlaylistEditPage` sends the correct zero-based sequence.
- Impact: Drawer reorder requests can be rejected as invalid and the passing test preserves the wrong API contract.
- Follow-up: Align the drawer and its test with the backend zero-based contiguous sequence, then verify optimistic reconciliation and rejection recovery.

### F-UI-024-002 - SubscriberRoute loses return navigation and performs render-time toasts

- Severity: `P2`
- Rows: `G-SUB`
- Evidence: `frontend/src/router/SubscriberRoute.tsx:85-90,132-137`; `frontend/src/router/ProtectedRoute.tsx:42-56`; `frontend/src/router/index.tsx:163-172,187-201`.
- Observation: Subscriber routes redirect to bare `/login` and `/subscriptions`, while `ProtectedRoute` constructs an encoded `returnTo`. The subscriber guard calls the toast store during render; after fresh navigation and wait, duplicate SubscriberRoute warnings were reproduced. ProtectedRoute emitted one warning.
- Impact: Anonymous users lose their intended destination and StrictMode can display duplicate feedback.
- Follow-up: Preserve an encoded internal return target and move notification side effects into an effect with one-notification protection.

### F-UI-024-003 - PlaylistDrawer has incomplete dialog and keyboard semantics

- Severity: `P2`
- Rows: `SH-04`
- Evidence: `frontend/src/components/player/PlaylistDrawer.tsx:255-279,287-347,354-446`.
- Observation: The drawer is rendered as a plain `div` without dialog or region semantics. The close, back, play, remove, tab, and drag controls lack complete accessible names; Escape handling, focus entry, focus containment, and focus return are absent. Background controls remain keyboard reachable.
- Impact: Keyboard and assistive-technology users cannot reliably enter, operate, or exit the drawer.
- Follow-up: Add dialog semantics, named controls, Escape handling, focus entry/trap/return, and background inertness while open.

### F-UI-024-004 - PlaylistDrawer destructive actions bypass confirmation and hide errors

- Severity: `P2`
- Rows: `MEM-01`, `MEM-02`, `MEM-03`, `SH-04`
- Evidence: `frontend/src/components/player/PlaylistDrawer.tsx:141-159,294-300,340-346`; confirmation pattern in `frontend/src/pages/subscriber/PlaylistEditPage.tsx:314-363`.
- Observation: Drawer playlist deletion and track removal call their APIs immediately, and both catch blocks swallow failures. The approved playlist behavior uses explicit confirmation before destructive actions.
- Impact: A keyboard or pointer mistake can remove content without confirmation, while failed operations provide no user feedback.
- Follow-up: Add confirmation copy and pending/error states for each destructive action, then separate confirmation, request, and final result in evidence.

### F-UI-024-005 - Several member loads allow stale responses to overwrite newer state

- Severity: `P2`
- Rows: `MEM-01`, `MEM-05`, `MEM-07`, `MEM-15`, `SH-04`
- Evidence: `frontend/src/components/player/PlaylistDrawer.tsx:62-107`; `frontend/src/pages/subscriber/LikeListPage.tsx:44-82`; `frontend/src/pages/subscriber/LicenseListPage.tsx:29-46`; `frontend/src/pages/subscriber/QuestionListPage.tsx:65-85`; `frontend/src/pages/subscriber/PlaylistDetailPage.tsx:49-66`; `frontend/src/pages/subscriber/PlaylistEditPage.tsx:53-75`; `frontend/src/pages/subscriber/QuestionDetailPage.tsx:67-83`.
- Observation: These loads set state after awaited requests without an abort signal or latest-request generation fence. `DownloadHistoryPage` and `AddToPlaylistModal` provide the contrasting abort/generation pattern at `frontend/src/pages/subscriber/DownloadHistoryPage.tsx:72-122` and `frontend/src/components/playlist/AddToPlaylistModal.tsx:44-82`.
- Impact: A late response can replace the state for a newer tab, route, page, or reopened dialog.
- Follow-up: Add cancellation or latest-request fencing to each affected load and test route departure, rapid tab/page changes, and close/reopen cycles.

### F-UI-024-006 - Question owner delete remains visible outside OPEN

- Severity: `P2`
- Rows: `MEM-16`, `MEM-17`
- Evidence: `frontend/src/pages/subscriber/QuestionDetailPage.tsx:239-246`; backend rule at `src/main/java/com/atstudio/atstudio/service/QuestionService.java:179-188`.
- Observation: The owner delete button is rendered for every status. The backend permits an owner to delete only `OPEN` questions; administrators may delete any status.
- Impact: The UI advertises an action that the owner will be denied for `IN_PROGRESS`, `RESOLVED`, or `CLOSED`.
- Follow-up: Gate the owner control on `question.status === 'OPEN'`, retain the admin policy separately, and verify denial/confirmation states.

### F-UI-024-007 - Playlist cards and the create card are mouse-only

- Severity: `P2`
- Rows: `MEM-01`, `MEM-02`, `MEM-03`
- Evidence: `frontend/src/pages/subscriber/PlaylistListPage.tsx:158-159,203-255`.
- Observation: Playlist cards and the create card use click handlers on `div` elements. The Play button has no handler, and its click bubbles to card navigation.
- Impact: Keyboard users cannot enter cards or create a playlist through the card surface, and the visible Play control does not perform playback.
- Follow-up: Use semantic links/buttons, stop nested action propagation where required, and connect Play to an explicit track projection or remove the control.

### F-UI-024-008 - Invalid or nonpositive IDs can leave pages loading forever

- Severity: `P2`
- Rows: `MEM-01`, `MEM-07`, `MEM-08`
- Evidence: `frontend/src/pages/subscriber/PlaylistDetailPage.tsx:20-23,50-62`; `frontend/src/pages/subscriber/PlaylistEditPage.tsx:19-23,53-71`; `frontend/src/pages/subscriber/LicenseDetailPage.tsx:7-40`.
- Observation: Invalid or nonpositive IDs return before the fetch while `loading` remains `true`. The affected render paths therefore remain on the loading state instead of showing a bounded error or not-found state.
- Impact: Malformed routes can produce an unrecoverable loading screen.
- Follow-up: Validate route parameters before loading, set a terminal error state, and provide retry or safe back navigation.

### F-UI-024-009 - AddToPlaylistModal has blank loading, no retry, and silent expiry close

- Severity: `P2`
- Rows: `SH-05`, `MEM-01`, `MEM-03`
- Evidence: `frontend/src/components/playlist/AddToPlaylistModal.tsx:44-74,120-126`.
- Observation: While `ready` is false the modal returns `null`, so loading has no visible status. A playlist-load failure displays an error but exposes no retry. A subscription-required error closes the modal and only invokes an optional callback, so the parent can close silently when no callback is provided.
- Impact: Users receive no loading feedback, cannot retry a transient failure, and may not learn why the action disappeared.
- Follow-up: Render loading and retry states, and make subscription expiry produce an explicit user-visible result or a required callback contract.

### F-UI-024-010 - Guest PlayerBar actions lose the origin route

- Severity: `P2`
- Rows: `G-AUTH`, `MEM-05`, `SH-04`
- Evidence: `frontend/src/layouts/PlayerBar.tsx:652-678`.
- Observation: Guest Like and Add-to-Playlist actions navigate to hardcoded `/login` after showing a toast, without encoding the current Track or source route.
- Impact: After authentication, the user cannot be returned to the action origin.
- Follow-up: Use the same safe return-target construction as `ProtectedRoute` and verify both actions from Track and shared Player entry points.

### F-UI-024-011 - Question attachment download lacks pending and failure handling

- Severity: `P2`
- Rows: `MEM-17`
- Evidence: `frontend/src/pages/subscriber/QuestionDetailPage.tsx:162-175`; the click handler invokes `downloadAttachment` without `await`, `catch`, or pending state.
- Observation: The attachment button has no disabled/pending state and no local error path.
- Impact: Repeated clicks can create duplicate requests, and failures are silent.
- Follow-up: Track the attachment being downloaded, await the request, surface a bounded error, and restore the control in `finally`.

### F-UI-024-012 - Playlist plan capacity can default silently or remain stale

- Severity: `P2`
- Rows: `MEM-01`, `MEM-02`, `SH-04`
- Evidence: `frontend/src/pages/subscriber/PlaylistListPage.tsx:37-63`; `frontend/src/components/player/PlaylistDrawer.tsx:62-84`.
- Observation: `PlaylistListPage` falls back to `DEFAULT_MAX_PLAYLISTS` when subscription loading fails. `PlaylistDrawer` updates the plan only on a truthy subscription value and otherwise retains the prior value.
- Impact: Create availability and capacity messaging can be incorrect without clearly indicating that the plan state is unknown.
- Follow-up: Represent plan capacity as loading/known/error, prevent silent capacity decisions on failed subscription reads, and add retry or bounded fallback behavior.

### F-UI-024-013 - PlaylistEdit metadata and reorder saves can partially succeed

- Severity: `REVIEW`
- Rows: `MEM-01`, `MEM-03`
- Evidence: `frontend/src/pages/subscriber/PlaylistEditPage.tsx:97-132`.
- Observation: Metadata and track order are sent through separate API calls in one save handler. A successful metadata update followed by a failed reorder leaves a partial server state while the UI reports only a generic save failure.
- Impact: The final state can differ from the user's all-or-nothing save expectation.
- Follow-up: Confirm the intended atomicity with the product/API contract, then either provide explicit partial-success recovery or introduce an approved transactional contract.

### F-UI-024-014 - Accessibility labels and loading text are inconsistent

- Severity: `P3`
- Rows: `MEM-01`, `MEM-05`, `MEM-06`, `MEM-07`, `MEM-09`, `MEM-15`, `MEM-17`, `SH-03`, `SH-04`, `SH-05`
- Evidence: English or symbol-only labels and status text are present at `frontend/src/components/player/HistoryModal.tsx:58-99`, `frontend/src/components/player/PlaylistDrawer.tsx:276-356,399-410`, `frontend/src/pages/subscriber/LikeListPage.tsx:135-147`, `frontend/src/pages/subscriber/PlaylistEditPage.tsx:282-306`, `frontend/src/pages/subscriber/DownloadHistoryPage.tsx:346-438`, `frontend/src/pages/subscriber/QuestionListPage.tsx:124-147`, `frontend/src/pages/subscriber/QuestionCreatePage.tsx:181-210`, and `frontend/src/pages/subscriber/QuestionDetailPage.tsx:162-175`.
- Observation: Several controls use English-only accessible names such as `Play`, symbol-only remove/close controls, or loading text such as `Loading...`; some filter/file controls have no explicit name.
- Impact: Labels are inconsistent with the product language and some controls are not reliably discoverable by assistive technology.
- Follow-up: Provide localized, action-specific names and announced loading/error status for every affected control.

### F-UI-024-015 - Playlist object URLs are not revoked

- Severity: `P3`
- Rows: `MEM-01`, `MEM-02`
- Evidence: `frontend/src/pages/subscriber/PlaylistListPage.tsx:105-115`; `frontend/src/pages/subscriber/PlaylistEditPage.tsx:79-87`.
- Observation: Thumbnail previews call `URL.createObjectURL` without a corresponding `URL.revokeObjectURL` on replacement, removal, unmount, or modal close.
- Impact: Repeated image selection can retain blob URLs and increase browser memory use.
- Follow-up: Revoke the prior preview URL at each lifecycle boundary and add a focused cleanup test.

### F-UI-024-016 - Dedicated page test coverage is incomplete and the drawer test codifies the wrong payload

- Severity: `P2`
- Rows: `MEM-01`, `MEM-05`, `MEM-06`, `MEM-07`, `MEM-08`, `MEM-09`, `MEM-15`, `MEM-16`, `MEM-17`, `SH-03`, `SH-04`, `SH-05`
- Evidence: Handoff test pointers `deliverables/agent/WI-20260809-ATS-024-handoff.md:110-117`; the existing drawer reorder assertion at `frontend/src/components/player/playerComponents.test.tsx:223-237`.
- Observation: There are no dedicated tests for `PlaylistDetail`, `LikeList`, `PlayHistory`, `LicenseList`, `LicenseDetail`, `QuestionList`, `QuestionCreate`, or `QuestionDetail`. The drawer reorder test passes while asserting one-based orders that contradict the backend contract.
- Impact: Important route and state combinations can regress without a dedicated test signal, and one passing test actively protects an invalid request shape.
- Follow-up: Add focused tests for the missing pages and corrected reorder, including guards, loading/error, stale-response, ownership/status, accessibility, and mutation-result states.

## Browser and Evidence Notes

- `G-AUTH`: `/likes`, `/play-history`, `/licenses`, `/licenses/999`, `/questions`, `/questions/new`, and `/questions/999` preserved encoded `returnTo` through `ProtectedRoute` and emitted one warning.
- `G-SUB`: `/playlists`, `/playlists/999`, `/playlists/999/edit`, and `/downloads` redirected to bare `/login`. Fresh navigation and wait reproduced the duplicate `SubscriberRoute` warning.
- `G-QUESTION`: authenticated and admin variants were not executed because no authenticated QA browser session was available.
- `SH-03`: Escape closed HistoryModal, focus returned, and the mobile visual result was healthy.
- `SH-04`: PlaylistDrawer was checked at `1440x900`, `1024x768`, `390x844`, and `360x800`. Visual layout was healthy; accessibility and behavior findings above remain confirmed.
- The browser playback check refreshed the browser-local Track 3 play-history timestamp. The browser end state was restored to Home with no Track, panels closed, and the viewport reset.
- No authenticated live browser coverage, durable mutation, download, upload, provider, mail, payment, secret, or storage inspection was performed.
