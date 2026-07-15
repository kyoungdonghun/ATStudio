# WI-20260715-ATS-024 Summary

## Decision

**Code and HTTP verification: PASS. Real-browser playback progression: manual confirmation pending.**

The duplicate `Content-Range` regression is fixed at its source. A valid partial Track stream now contains exactly one standards-compliant `Content-Range` value, and the Track detail action uses `Play` / `Pause` semantics instead of preview terminology.

## Changes

- Removed the controller's manual `Content-Range` header from `206 Partial Content` responses. Spring's `ResourceRegionHttpMessageConverter` remains the single owner of that header.
- Strengthened `TrackControllerTest` to assert that the response header collection contains exactly one `Content-Range` value.
- Changed the Track detail button label from `미리 듣기` to `재생`; the playing state remains `일시정지`.
- Left Official Download authorization, subscription checks, daily quota, history, and license behavior untouched.

## Verification

| Check | Result |
|------|--------|
| Focused `TrackControllerTest` | PASS, 22 tests / 0 failures / 0 errors |
| Direct Range probe | PASS, `206`, 1,024 bytes, one `Content-Range: bytes 0-1023/17863782` |
| Direct no-Range probe | PASS, `200`, complete 17,863,782-byte resource |
| Frontend typecheck | PASS |
| Changed-file ESLint | PASS |
| Changed-file Prettier | PASS |
| Frontend Vitest | PASS, 19 files / 79 tests |
| Stable source assertion | PASS, `재생` present and `미리 듣기` absent |

The local acceptance runtime was restarted from the current development worktree while preserving the existing database, upload storage, and external environment bundle. The temporary public URL is:

`https://effect-peninsula-translated-antarctica.trycloudflare.com`

## Browser Evidence Boundary

The in-app browser rendered the corrected `▶ 재생` label and the full Track detail. However, its automated playback attempt remained at `0:00`, and this subagent browser surface does not support bringing the tab to the foreground for a real user-audio check. No media error appeared in the browser console. Therefore, WI-023's final real-browser current-time/progress evidence must still be captured manually or by the parent browser session; this summary does not claim that last observation passed.

## Changed Files

- `src/main/java/com/atstudio/atstudio/controller/TrackController.java`
- `src/test/java/com/atstudio/atstudio/controller/TrackControllerTest.java`
- `frontend/src/pages/public/TrackDetailPage.tsx`
- `deliverables/user/WI-20260715-ATS-024-summary.md`
- `deliverables/agent/WI-20260715-ATS-024-evidence-pack.md`

No file was staged or committed in this WI.
