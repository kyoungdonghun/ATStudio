---
version: 1.0
last_updated: 2026-08-14
project: ATS
owner: ma
category: wi-implementation-handoff
status: active
dependencies:
  - path: WI-20260809-ATS-055-handoff.md
    reason: Canonical approved WI scope and constraints
  - path: WI-20260809-ATS-055-backend-handoff.md
    reason: Private streaming contract implemented separately
---

# Frontend Implementation Handoff: WI-20260809-ATS-055

## Assignment

- **Agent:** `se`
- **Purpose:** complete the canonical frontend binary contract, Track error
  normalization, and same-action duplicate-request fencing portions of WI-055.

## Required Behavior

- Define one reusable `BinaryDownload` result with `blob`, safe `fileName`, and
  `contentType` in the existing download API module; do not create a competing
  download subsystem.
- Convert Axios Blob responses into this result by:
  - accepting RFC 5987 `filename*` and quoted/basic `filename` disposition;
  - decoding safely and falling back on malformed values;
  - removing path separators/control characters and rejecting blank, dot, and
    traversal-like names;
  - using a deterministic fallback supplied by the caller and derived from a
    stable ID plus validated metadata;
  - rejecting any non-Blob or `Blob.size === 0` body before a browser action;
  - taking content type from a valid response header, then Blob type, then
    `application/octet-stream`.
- Make Track, Notice, Question, and Company Certification download wrappers use
  the same response normalization. Preserve abort signals and current request
  options such as auth-replay controls.
- Make the shared browser trigger consume the canonical result and still revoke
  its object URL after activation. No page may synthesize `${title}.mp3` after
  a verified server response is available.
- Normalize Track download failures through the existing async
  `getApiErrorCode()` Blob-aware path. Remove PlayerBar's manual Blob JSON parser
  and ensure all named Track callers distinguish at least
  `NO_ACTIVE_SUBSCRIPTION`, `DOWNLOAD_LIMIT_EXCEEDED`, cancellation where
  already owned, and unknown failure without duplicate navigation/mutation.
- Add synchronous/per-identity pending ownership to Track list, License list,
  Like list, and Playlist detail. The same visible/identity action must issue at
  most one request until settled. Buttons expose disabled/pending state where
  already representable without redesign. Preserve existing PlayerBar,
  Track-detail, and Download-history fencing and stale-request semantics.
- Update all affected focused API/page/layout tests. Include header parsing,
  sanitization, fallback, content type, zero-byte/non-Blob rejection, object URL
  behavior, Blob JSON errors, duplicate rapid activation, reset after failure,
  and existing cancellation/latest-owner behavior.

## Likely Write Scope

- `frontend/src/api/downloads.ts`
- `frontend/src/api/notices.ts`
- `frontend/src/api/questions.ts`
- `frontend/src/api/admin.ts`
- `frontend/src/api/client.ts` only if a minimal shared error helper type is
  required; preserve existing interceptor behavior.
- `frontend/src/layouts/PlayerBar.tsx`
- `frontend/src/pages/public/NoticeDetailPage.tsx`
- `frontend/src/pages/public/TrackDetailPage.tsx`
- `frontend/src/pages/public/TrackListPage.tsx`
- `frontend/src/pages/subscriber/QuestionDetailPage.tsx`
- `frontend/src/pages/subscriber/LicenseListPage.tsx`
- `frontend/src/pages/subscriber/LikeListPage.tsx`
- `frontend/src/pages/subscriber/PlaylistDetailPage.tsx`
- `frontend/src/pages/subscriber/DownloadHistoryPage.tsx`
- Company Certification ADMIN download caller if its wrapper return shape
  requires adaptation.
- Corresponding focused API, layout, and page tests only.

Expand the write set only to a directly compiling binary-download caller or its
focused test; list every expansion explicitly in the result.

## Verification

- Begin with focused RED assertions for the missing shared contract and pending
  fences, then GREEN.
- Run all affected frontend API/page/layout tests.
- Run typecheck, scoped ESLint, scoped Prettier, and `git diff --check`.
- Do not run real browser downloads; tests must mock object URLs/anchors and use
  synthetic Blobs only.

## Constraints

- Read the canonical WI-055 handoff and minimal Tier/frontend/security context.
- Do not inspect protected outputs, ignored secrets, private files, or external
  effects.
- Do not change server authorization, entitlement/count/history semantics,
  bulk limits, route-lifetime policy, dependencies, schema/data, docs, branches,
  commit, or push.
- Do not turn asynchronous state alone into the duplicate fence; use a
  synchronous ref or equivalent per-identity operation ownership so rapid
  activations in the same render cannot issue parallel requests.
- Do not weaken existing abort/generation/latest-result guards.
