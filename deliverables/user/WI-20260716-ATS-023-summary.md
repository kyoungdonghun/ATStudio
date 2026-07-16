# WI-20260716-ATS-023 Summary

## Outcome

- The current public client demo now contains 36 playable QA Demo tracks, 36 target tags, and 9 curated playlists.
- Existing catalog data was preserved. Totals moved from 2 to 38 active tracks and from 3 to 37 tags.
- Every generated track has a Korean title, four discovery tags, a colored thumbnail, extracted duration, waveform data, and a valid public stream.

## Verification

- Seed syntax and dry-run passed.
- Initial live seed passed.
- A second seed run created no duplicates and retained `36/36/9`.
- All 36 stream checks passed; title and Usage Guide Tag searches returned expected results.
- The Cloudflare public route returned the full catalog, HTTP 206 audio ranges, and HTTP 200 thumbnails.
- Browser inspection confirmed readable Korean text, dense filters/list rows, thumbnails, durations, and detail metadata.

## Operations

- Seed or align: `scripts/demo/seed-client-demo.ps1 -Mode Seed`
- Verify only: `scripts/demo/seed-client-demo.ps1 -Mode Verify`
- Preview cleanup: `scripts/demo/seed-client-demo.ps1 -Mode Cleanup -DryRun`
- The cleanup command is manifest-scoped and was not executed, so the public demo remains populated.

## Residual Note

- The stable backend uses soft deletion for Track and Playlist records. A future cleanup removes generated storage files and deactivates rows while retaining inactive database history.
