# Evidence Pack: WI-20260715-ATS-024

## Summary

Removed duplicate `Content-Range` emission, added a header-multiplicity regression assertion, and replaced the remaining Track detail preview label without changing Public Listening or Official Download policy.

## Scope and DoD

- [x] A valid Range response contains exactly one `Content-Range` value.
- [x] No-Range and open-ended Range continue to address the complete stored resource.
- [x] Track detail uses `재생` / `일시정지` and no longer uses `미리 듣기`.
- [x] Official Download service and policy paths are unchanged.
- [x] Focused backend and frontend quality checks pass.
- [ ] Real-browser current-time/progress advancement requires parent-session or manual confirmation.

## Root Cause and Correction

`TrackController` manually added `Content-Range` to a `ResponseEntity<ResourceRegion>`. Spring's `ResourceRegionHttpMessageConverter` also generates the header while serializing a partial response. The backend therefore emitted two identical fields, which Vite and Cloudflare could merge into an invalid comma-separated value and Chrome could reject for media playback.

The correction removes only the controller-owned header. The converter is now the sole owner of partial-response `Content-Range`. The controller still resolves and validates the requested byte interval, returns `206`, sets `Accept-Ranges`, `Content-Length`, and media type, and supplies the matching `ResourceRegion`.

## Traceability

| Requirement | Evidence |
|-------------|----------|
| One partial-response header | `TrackController.streamTrack` no longer adds `Content-Range`; focused test asserts the complete header list equals one expected value |
| Complete Public Listening | Direct no-Range probe returned `200` and 17,863,782 bytes; Range denominator remained 17,863,782 |
| Correct detail terminology | `TrackDetailPage.tsx` renders `재생` when stopped and `일시정지` when playing; source assertion rejects `미리 듣기` |
| Protected Official Download preserved | No `DownloadService`, subscription, quota, history, license, or security file changed; controller download tests remained in the passing focused class |

## Commands and Results

### Backend

`gradlew.bat test --tests "com.atstudio.atstudio.controller.TrackControllerTest"`

- Exit code: 0
- Generated XML root: 22 tests, 0 skipped, 0 failures, 0 errors
- The start/end Range test now asserts both the expected value and the exact one-element header list.

### Frontend

- `npm run typecheck` -> PASS
- `npm run lint -- --quiet src/pages/public/TrackDetailPage.tsx` -> PASS
- `npm exec prettier -- --check src/pages/public/TrackDetailPage.tsx` -> PASS after formatting only that owned file
- `npm test -- --run` -> PASS, 19 test files and 79 tests
- UTF-8 source assertion -> PASS: `재생` present, `미리 듣기` absent

### Live HTTP

`curl.exe -sS -D - -o NUL -r 0-1023 http://127.0.0.1:8080/api/tracks/2/stream`

- Status: `206`
- Content length: `1024`
- Content-Range field count: `1`
- Value: `bytes 0-1023/17863782`

`curl.exe -sS -D - -o NUL http://127.0.0.1:8080/api/tracks/2/stream`

- Status: `200`
- Content length: `17863782`
- Accept-Ranges: `bytes`
- Content type: `audio/mpeg`

### Browser

- Local Track detail rendered `▶ 재생`; no preview wording remained.
- A fresh-origin automated click loaded media metadata but the player UI stayed at `0:00`.
- Browser console contained no media error, only the existing React Router future-flag warning.
- The subagent in-app browser rejected foreground visibility control, so real user-audio playback and progress advancement were not conclusively testable here.
- Result: header and UI evidence PASS; final real-browser playback progression OPEN for WI-023 evidence.

## Runtime State

- Runtime source: current `codex/p1-acceptance-hardening` development worktree with WI-024 changes.
- Local frontend: `http://127.0.0.1:5173`
- Local backend: `http://127.0.0.1:8080`
- Temporary public URL: `https://effect-peninsula-translated-antarctica.trycloudflare.com`
- Existing acceptance database, upload storage, and external environment bundle were reused without mutation.
- No live payment Provider, email, OAuth, database mutation, schema operation, staging, or commit occurred.

## Changed Files

- `src/main/java/com/atstudio/atstudio/controller/TrackController.java`
- `src/test/java/com/atstudio/atstudio/controller/TrackControllerTest.java`
- `frontend/src/pages/public/TrackDetailPage.tsx`
- `deliverables/user/WI-20260715-ATS-024-summary.md`
- `deliverables/agent/WI-20260715-ATS-024-evidence-pack.md`

Pre-existing WI-023 deliverables and runtime log files were not modified, reverted, staged, or committed.

## Rollback

1. Restore the controller's previous manual `Content-Range` line only if intentionally reproducing the defect; no data rollback is required.
2. Remove the one-element header-list assertion if the controller behavior is reverted.
3. Restore only the Track detail label if product wording is deliberately changed by a separately approved requirement.

No database, storage, subscription, download, or license rollback is involved.
