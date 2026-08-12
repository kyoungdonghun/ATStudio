# WI-20260809-ATS-024 Completion Summary

## Outcome

WI-024 is complete as a documentation-only audit of authenticated/member routes, question paths, and shared history/playlist dialogs. The audited rows are classified `PASS 1`, `FAIL 15`, `BLOCKED 1`, and `N/A 0`.

`FAIL` means that at least one required state for a row has a confirmed defect; it does not mean that every core behavior failed. `G-AUTH` passed. `G-QUESTION` authenticated and admin variants were blocked because no authenticated QA browser session was available. `G-SUB` and all listed `MEM` and `SH` rows failed.

No product code, tests, configuration, database, fixture, archive, or Git state was changed. The intentional archive `output/client-demo-screenshots-20260716-140514.zip` was preserved.

## Browser Coverage

- `SubscriberRoute` sent `/playlists`, `/playlists/999`, `/playlists/999/edit`, and `/downloads` to bare `/login`; fresh navigation and wait reproduced duplicate warning toasts.
- `ProtectedRoute` preserved encoded `returnTo` for `/likes`, `/play-history`, `/licenses`, `/licenses/999`, `/questions`, `/questions/new`, and `/questions/999`, and emitted one warning.
- HistoryModal closed with Escape, returned focus, and remained visually healthy on mobile.
- PlaylistDrawer was checked at `1440x900`, `1024x768`, `390x844`, and `360x800`. Its visual layout was healthy, but dialog semantics, focus, keyboard reachability, accessible names, destructive confirmation, and stale-response behavior failed.
- Browser playback refreshed the browser-local Track 3 play-history timestamp. End state was restored to Home with no Track, panels closed, and viewport reset.

Evidence files are recorded in the agent-facing pack. The valid shared captures are `G-SUB_VM390_anonymous-login.png`, `SH-03_VM360_history-modal.png`, and the four `SH-04` drawer captures. No nonexistent `SH-03_VD1440` capture is cited.

## Material Findings

- PlaylistDrawer sends one-based reorder orders while the backend requires the exact zero-based sequence; its passing test asserts the wrong payload. PlaylistEdit is correct.
- SubscriberRoute loses the return target and performs render-time toast work, producing duplicate warnings under fresh StrictMode navigation.
- PlaylistDrawer lacks complete dialog/focus/Escape semantics, immediately performs destructive actions, swallows errors, and can accept stale responses.
- Stale-response races also affect LikeList, LicenseList, QuestionList, PlaylistDetail, PlaylistEdit, and QuestionDetail. DownloadHistory and AddToPlaylistModal show the stronger cancellation/generation pattern.
- Question owner delete remains visible for every status although the backend permits owners only in `OPEN`.
- Playlist cards/create card are mouse-only, the visible Play button has no handler, invalid IDs can leave detail pages loading forever, and AddToPlaylistModal lacks visible loading/retry/expiry feedback.
- PlayerBar guest Like/Add actions hardcode `/login`; question attachment download lacks pending/error handling; playlist capacity can be silently defaulted or remain stale.
- PlaylistEdit metadata and reorder calls can partially succeed. Additional P3 findings cover inconsistent labels/loading text and unreleased playlist preview object URLs.
- Dedicated tests are absent for PlaylistDetail, LikeList, PlayHistory, LicenseList, LicenseDetail, QuestionList, QuestionCreate, and QuestionDetail. The drawer reorder test protects the incorrect one-based contract.

## Validation and Limits

- Targeted frontend audit result: `7 files, 42 tests passed` for the listed guard, shared-component, playlist, and download-history tests.
- Targeted backend audit result: `BUILD SUCCESSFUL` for the listed Playlist, Like, License, Download, and Question controller/service areas.
- No authenticated live browser coverage, durable mutation, download, upload, provider, mail, payment, secret, storage, or database inspection was performed.
- Detailed source pointers, findings, screenshots, test records, and restoration boundaries are in [WI-20260809-ATS-024 evidence pack](../agent/WI-20260809-ATS-024-evidence-pack.md) and [WI-20260809-ATS-024 findings](../agent/WI-20260809-ATS-024-findings.md).
