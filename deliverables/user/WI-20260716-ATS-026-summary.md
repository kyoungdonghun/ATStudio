---
version: 1.0
last_updated: 2026-07-16
project: ATS
owner: qa-fe
category: audit
status: complete
related_wi: WI-20260716-ATS-026
---

# WI-20260716-ATS-026 Summary

## Findings

### F-026-01 [P1] Changed admin lists can render an older filter or page response

- `frontend/src/pages/admin/WhitelistChannelManagePage.tsx:71-96` commits every response to `channels`, `pageInfo`, and `edits` without a request generation, request key, or abort signal. Filter and page changes at `:207-212` and `:373` can therefore be overwritten by an older request that resolves last.
- `frontend/src/pages/admin/CompanyCertManagePage.tsx:97-113` has the same unrestricted completion path for certification status and page changes.
- User impact: an administrator can see and act on records from a previously selected scope while the controls show the new scope. This contradicts the accepted latest-request-wins contract at `docs/ui/screen-flow.md:68`.
- Focused test: in each existing page test, defer two list promises, change filter or page, resolve the newer promise first and the older promise last, then assert that the newer rows, page metadata, loading state, and error state remain authoritative.
- Classification: residual behavior defect on changed acceptance surfaces; the loader pattern predates this diff, but the current remediation and accepted UI contract do not close it.

### F-026-02 [P2] Concurrent whitelist mutations can silently skip the final refresh

- The new guard at `frontend/src/pages/subscriber/WhitelistChannelPage.tsx:83-87` returns immediately while any load is active. All four successful mutation paths then depend on `await load()` at `:181`, `:212`, `:227`, and `:250`.
- The UI only shows or disables the matching operation through per-action `busyKey` checks at `:351`, `:407`, `:416-421`, and `:435`; another channel action can start while the first mutation or refresh is still active.
- User impact: the second mutation can succeed on the server while its refresh is dropped. If the first refresh read before the second commit, the page keeps stale channel status, slot usage, or primary selection until a manual reload.
- Focused test: defer the first post-mutation `fetchWhitelistChannels`, complete a different mutation while that refresh is active, return pre-second-mutation data from the first refresh, and assert that a queued second refresh occurs and the final server state is rendered.
- Classification: behavior regression introduced by the new `loadBlocked` guard.

### F-026-03 [P2] The company-certification detail dialog cannot close while detail is loading

- `frontend/src/pages/admin/CompanyCertManagePage.tsx:115-126` leaves `detailLoading` true until the request settles, while `closeDetail()` at `:129-135` does not invalidate the request or clear that state.
- The dialog remains open whenever `detailLoading` is true at `:284-290`. Consequently, the shared Escape and close-button callbacks at `frontend/src/components/ui/Modal.tsx:26-32` and `:130-138` run but cannot dismiss the dialog; a late success can repopulate it after the user attempted to close it.
- User impact: keyboard and pointer users are trapped in an unwanted loading dialog and cannot reliably cancel the interaction.
- Focused test: defer `fetchCompanyCert`, open a detail, press Escape and separately click Close before resolution, then assert the dialog disappears immediately and stays closed after both a late success and a late failure.
- Classification: residual accessibility and async-state defect; it predates the current diff but remains in a changed certification surface and conflicts with the hardened shared modal behavior.

## Judgment

`CHANGES_REQUIRED_BEFORE_WI-028`

No P0 issue or product-invariant change was found, but F-026-01 is an operator-facing stale-data blocker and the two P2 findings leave reproducible state and accessibility regressions.

## Verified Boundaries

- Public full-track playback remains `/api/tracks/{id}/stream`; it was not reduced to previews.
- Official downloads remain gated by ADMIN or authenticated active-subscription state in the player, while re-download/history flows remain behind subscriber access.
- Subscription checkout remains the recurring billing-agreement prepare/confirm flow; USER-only payment routes and BUSINESS-only certification routes remain guarded.
- Vite still serves one frontend origin and proxies `/api` and `/uploads` to the existing backend on `127.0.0.1:8080`.
- OAuth return targets remain internal, bounded, per-attempt values; admin/user route boundaries and explicit service-error states were preserved.

## Formatting Classification

- The cumulative frontend diff contains 158 tracked files with 5,239 insertions and 2,726 deletions, plus 30 untracked source/test files reviewed and two runtime log files excluded.
- A formatted-HEAD byte comparison mechanically confirmed 24 tracked CSS modules as Prettier-only. Whitespace-insensitive review also confirmed format-only TSX examples such as `LikeListPage.tsx`, `SubscriptionPlanPage.tsx`, `AlbumDetailPage.tsx`, `PlaylistDetailPage.tsx`, and `TrackUploadPage.tsx`.
- Behavior findings above are not inferred from formatting churn. The exact Prettier-only path list and behavior-bearing review inventory are in the evidence pack.

## Verification

- Focused Vitest: 22 files, 133 tests passed.
- Full Vitest: 44 files, 242 tests passed.
- TypeScript: `npx tsc --noEmit --incremental false` passed.
- Focused ESLint and full-tree Prettier checks passed.
- Production-only and full dependency audits reported 0 vulnerabilities.
- `git diff --check -- frontend` passed with LF-to-CRLF warnings only.
- No browser/runtime, DB, provider, client worktree, Git index, branch, remote, or server state was touched.
