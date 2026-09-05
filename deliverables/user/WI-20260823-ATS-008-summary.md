# WI-20260823-ATS-008 Summary

## Result

**PASS.** The remaining HomePage regression assertion and development media
fixtures are repaired on `codex/v1-release-rehearsal-fixes`.

- Replaced the line-break-sensitive exact subtitle query with a whitespace-
  tolerant matcher that still asserts the full intentional subtitle text.
- Restored exactly 20 missing public fixtures to `uploads/`: 10 scoped Track
  WAV files and 10 scoped Track thumbnails. All were absent before the copy;
  no target was overwritten.
- A guarded read-only local DB query proved the active set is exactly 10
  `AT.M Demo` Tracks and that every audio/thumbnail key exactly matches the
  retained fixture storage. It emitted no datasource or credential values.
- No DB rows, schema, source HomePage copy, local secret/configuration file,
  client worktree, or running process was changed. The already-running local
  backend served the restored files without a restart.

## Verification

| Check | Result |
| --- | --- |
| Focused HomePage test | PASS: 7 tests, 0 failures. |
| Full Track stream | PASS: 10/10 HTTP 200, `audio/wav`, 112044 bytes each. |
| Range stream | PASS: 10/10 HTTP 206 for `bytes=0-1023`. |
| Track thumbnail | PASS: 10/10 HTTP 200, `image/jpeg`. |
| Source-target file parity | PASS: 20 exact keys, 0 SHA-256 mismatches. |
| Post-restore catalog proof | PASS: 10 active media-and-waveform-ready demo Tracks; fixture counts unchanged. |
| `git diff --check` | PASS: no whitespace diagnostics. |

## Scope And Rollback

The restored `uploads/` paths are intentionally Git-ignored local runtime
fixtures. Rollback, if later required, is limited to deleting only the 20
paths documented in the agent evidence pack after reconfirming that no DB row
was changed; no pre-existing asset may be removed.

The handoff for the independent full frontend and restored-media gate remains
`WI-20260823-ATS-009`.
