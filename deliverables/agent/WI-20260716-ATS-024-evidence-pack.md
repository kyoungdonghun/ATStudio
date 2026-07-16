# Evidence Pack: WI-20260716-ATS-024

## Summary (one-liner)

- Independently verified the complete local and public QA Demo dataset, including all 36 public audio Range streams and all 36 public thumbnails.

## Scope / DoD Check

- DoD items:
  - [x] Confirmed 36 target tags and 36 active QA Demo tracks.
  - [x] Confirmed all local demo tracks have duration, waveform data, and successful Range streaming.
  - [x] Confirmed title and Usage Guide Tag searches return target tracks.
  - [x] Confirmed 9 QA Demo playlists with 7-12 tracks each.
  - [x] Confirmed the second Seed run retained counts and created no duplicate records.
  - [x] Confirmed the original 2 tracks and 3 tags remain in the final totals.
  - [x] Confirmed all 36 public Cloudflare Range streams and thumbnails independently.

## Reference Documents (Tier 0-2)

**Injected Context** (from WI Handoff Packet):

| Tier | Document | Reason |
|------|----------|--------|
| 0 | `docs/standards/core-principles.md` | Constitution and verification independence |
| 1 | `docs/policies/quality-gates.md` | Quality and evidence requirements |
| 2 | `deliverables/user/REQ-20260716-ATS-003.md` | Approved scope and success criteria |
| 2 | `deliverables/agent/WI-20260716-ATS-023-evidence-pack.md` | Implementation evidence and rerun record |

**Injection Rules Applied**:

- Rule source: `.claude/config/context-injection-rules.json`
- Assignee: `re`
- Task type: testing/QA
- agent_required_tiers: `[0]`

## Evidence Pointers

- Files inspected:
  - `scripts/demo/seed-client-demo.mjs`
  - `scripts/demo/seed-client-demo.ps1`
  - `output/demo-seed/manifest.json`
  - `deliverables/agent/WI-20260716-ATS-023-evidence-pack.md`
- Environments inspected:
  - Local API: `http://127.0.0.1:8080`
  - Public demo: `https://challenged-efficiently-void-jonathan.trycloudflare.com`

## Commands & Outputs

- Commands executed by the independent verifier:
  - `scripts/demo/seed-client-demo.ps1 -Mode Verify`
  - Local and public track/tag count requests.
  - Public list request followed by all 36 Range stream and thumbnail requests.
- Sanitized outputs:
  - Verify mode: exit 0, `pass: true`.
  - Target counts: 36 tags, 36 active tracks, 36 admin-visible demo tracks, 9 playlists.
  - Playlist membership: 7, 8, 8, 8, 9, 10, 10, 11, and 12 tracks.
  - Title search `새벽`: 1 demo result.
  - Usage Guide Tag search `유튜브용`: 4 demo results.
  - Local duration/waveform/Range failures: 0.
  - Local and public totals: 38 active tracks and 37 tags.
  - Public demo list: HTTP 200 with 36 QA Demo tracks.
  - Public audio: HTTP 206 for 36/36 Range requests; failures 0.
  - Public thumbnails: HTTP 200 for 36/36 requests; failures 0.
  - Baseline preservation: 2 original tracks and 3 original tags remain.
  - Verification performed no file or data mutation.

## Tests

- Full local Verify mode -> PASS.
- Public Cloudflare list parity -> PASS.
- Public 36-track Range stream sweep -> PASS.
- Public 36-thumbnail sweep -> PASS.
- Baseline preservation and duplicate-safety evidence review -> PASS.

## Risks / Rollback

- Risks:
  - This verification confirms the environment at the recorded time; a later server restart with a different runtime DB or storage root would require rerunning Verify.
  - Cleanup remains intentionally unexecuted so the client demo stays populated.
- Rollback:
  - Read-only WI; reference WI-023 for manifest-scoped cleanup.

## Follow-ups

- No follow-up WI is required for the approved demo-data scope.
