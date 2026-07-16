# Evidence Pack: WI-20260716-ATS-023

## Summary (one-liner)

- Added an idempotent demo-catalog seeder and populated the active client demo with 36 playable tracks, 36 target tags, and 9 curated playlists without removing the pre-existing catalog.

## Scope / DoD Check

- DoD items:
  - [x] Generated 36 valid 8 kHz mono PCM sine-wave WAV files with varied 36-75 second durations.
  - [x] Reused or created 36 natural Korean tags across MOOD, GENRE, INSTRUMENT, and USAGE.
  - [x] Created and activated 36 tracks with Korean titles, four tags each, extracted duration, and 200-point waveform data.
  - [x] Created 9 playlists under the existing active business QA subscription with 7-12 tracks each.
  - [x] Added deterministic colored PNG thumbnails for tracks and playlists.
  - [x] Proved duplicate-safe reruns: a second Seed run emitted no create events and retained the same target counts.
  - [x] Added scoped cleanup and verified its dry-run targets: 9 playlists, 36 tracks, and 34 newly created tags.
  - [x] Preserved the original 2 tracks and 3 tags; post-seed totals are 38 active tracks and 37 tags.

## Reference Documents (Tier 0-2)

**Injected Context** (from WI Handoff Packet):

| Tier | Document | Reason |
|------|----------|--------|
| 0 | `docs/standards/core-principles.md` | Constitution and approval rules |
| 0 | `docs/standards/development-standards.md` | Assignee: se |
| 1 | `docs/policies/security-policy.md` | Runtime credentials and secret handling |
| 1 | `docs/policies/quality-gates.md` | Live data and verification quality |
| 2 | `docs/design/api-spec.md` | Track, tag, authentication, and playlist contracts |
| 2 | `docs/design/db-schema.md` | Catalog relationships and ownership boundaries |

**Injection Rules Applied**:

- Rule source: `.claude/config/context-injection-rules.json`
- Assignee: `se`
- Task types: operation, testing, security
- agent_required_tiers: `[0]`

## Evidence Pointers

- Files changed:
  - `scripts/demo/seed-client-demo.mjs`: seed, verify, cleanup, audio, thumbnail, manifest, and secret-safe API client.
  - `scripts/demo/seed-client-demo.ps1`: PowerShell 5.1-compatible execution wrapper.
- Key locations:
  - `scripts/demo/seed-client-demo.mjs:14`: 36 canonical tag specifications.
  - `scripts/demo/seed-client-demo.mjs:21`: 36 Korean track titles.
  - `scripts/demo/seed-client-demo.mjs:30`: 9 playlist specifications.
  - `scripts/demo/seed-client-demo.mjs:148`: manifest entry identity matching; fixed to ignore undefined identity fields.
  - `scripts/demo/seed-client-demo.mjs:160`: low-size PCM WAV generation.
  - `scripts/demo/seed-client-demo.mjs:220`: deterministic PNG cover generation.
  - `scripts/demo/seed-client-demo.mjs:270`: tag reuse/create behavior.
  - `scripts/demo/seed-client-demo.mjs:310`: track create/reactivate and metadata alignment.
  - `scripts/demo/seed-client-demo.mjs:384`: playlist creation and batch membership.
  - `scripts/demo/seed-client-demo.mjs:425`: full live verification.
  - `scripts/demo/seed-client-demo.mjs:497`: manifest-scoped cleanup and storage-boundary check.
  - `output/demo-seed/manifest.json`: non-secret ownership manifest for generated records.
- Generated artifacts:
  - `output/demo-seed/audio/`: 36 WAV files.
  - `output/demo-seed/covers/`: 36 PNG files.
  - Generated artifact bytes: 32,007,798.

## Commands & Outputs

- Commands executed:
  - `node --check scripts/demo/seed-client-demo.mjs`
  - PowerShell AST parse of `scripts/demo/seed-client-demo.ps1`
  - `scripts/demo/seed-client-demo.ps1 -Mode Seed -DryRun`
  - `scripts/demo/seed-client-demo.ps1 -Mode Seed` (initial application)
  - `scripts/demo/seed-client-demo.ps1 -Mode Seed` (duplicate-safety rerun)
  - `scripts/demo/seed-client-demo.ps1 -Mode Verify`
  - `scripts/demo/seed-client-demo.ps1 -Mode Cleanup -DryRun`
  - Public API Range request through Cloudflare.
- Sanitized outputs:
  - Initial baseline: 2 active tracks, 3 tags, and one existing business-user default playlist.
  - Target result: 36 demo tracks, 36 target tags, and 9 demo playlists.
  - Final totals: 38 active tracks and 37 tags.
  - Playlist track counts: 7, 8, 8, 8, 9, 10, 10, 11, 12.
  - Search matches: title query `새벽` -> 1; Usage Guide Tag query `유튜브용` -> 4.
  - Stream verification: all 36 target tracks passed; public sample returned HTTP 206 and 1,024 bytes.
  - Public thumbnail sample returned HTTP 200.
  - Duplicate-safety rerun created 0 tags, 0 tracks, and 0 playlists.
  - Cleanup dry-run: 9 playlists, 36 tracks, and 34 tags; the 2 reused pre-existing target tags are excluded.

## Tests

- Node syntax check -> PASS.
- PowerShell parser check -> PASS.
- Seed dry-run -> PASS (`36/36/9`).
- Live seed and internal verify -> PASS.
- Repeat seed and count stability -> PASS.
- Manifest repair regression check -> PASS (`36 tags / 36 tracks / 9 playlists`, ownership `34/36/9`).
- Local API verification -> PASS.
- Cloudflare public list, stream, and thumbnail verification -> PASS.
- In-app browser visual inspection -> PASS: Korean text, 38-track total, tag filters, durations, colored thumbnails, and detail metadata rendered correctly.

## Risks / Rollback

- Risks:
  - The stable backend soft-deactivates Track and Playlist rows. Cleanup removes only the generated storage files and leaves inactive DB audit rows.
  - The cleanup command was dry-run verified but intentionally not executed because the user requested the demo data to remain available.
  - Two target tags (`잔잔한`, `피아노`) already existed and were reused; cleanup explicitly excludes them.
- Rollback:
  - Preview: `scripts/demo/seed-client-demo.ps1 -Mode Cleanup -DryRun`
  - Execute only with explicit destructive approval: `scripts/demo/seed-client-demo.ps1 -Mode Cleanup`
  - Cleanup is bounded by `output/demo-seed/manifest.json` and validates storage paths remain inside the configured public storage root.

## Follow-ups

- WI-20260716-ATS-024: independent read-only verification of live and public demo data.
