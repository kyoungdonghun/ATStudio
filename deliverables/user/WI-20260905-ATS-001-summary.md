---
version: 1.0
last_updated: 2026-09-05
project: ATS
owner: qa-integ
category: work-summary
status: draft
dependencies:
  - path: ../agent/WI-20260905-ATS-001-evidence-pack.md
    reason: Detailed findings and exact stage candidate lists
  - path: REQ-20260905-ATS-001.md
    reason: Approved closeout scope
---

# WI-20260905-ATS-001 Summary

## Result

TL;DR: Reviewed all 40 tracked modifications on
`codex/v1-release-rehearsal-fixes` at HEAD `69d0226`. All belong to approved
prior work, but two P2 defects prevent unconditional closeout. No product
file was changed and no application test/build/browser run was duplicated.

## Findings

1. **P2: Likes can close the wrong visible tab.** Open Likes, manually switch
   the drawer to Playlists, then press PlayerBar's Likes action: the parent
   still remembers `likes` and closes the drawer. The added test mocks away
   the real drawer's tab state. Evidence:
   `frontend/src/layouts/PlayerBar.tsx:93-98` and
   `frontend/src/components/player/PlaylistDrawer.tsx:69-77,724-738`.
2. **P2: Nickname trimming differs across client and API.** A nickname wrapped
   in U+00A0 spaces is trimmed and accepted by the frontend but retains those
   characters under Java `strip()` and fails direct DTO validation. Ordinary
   spaces are covered; this Unicode boundary is not. Evidence:
   `frontend/src/utils/validation.ts:84-96`,
   `src/main/java/com/atstudio/atstudio/dto/user/RegisterRequest.java:29-30`,
   and the matching completion/update setters. Do not repair historical data.

These are current static findings, not observed live API/browser failures.
MA should add or run narrow integration regressions before finalizing them.

## Scope And Evidence

- 38 tracked files map to REQ-20260823-ATS-001; the two HomePage files also
  trace to REQ-20260818-ATS-002. The later WI-008/009 record supersedes the old
  HomePage assertion failure, but is not a fresh test result.
- No additional defect was established in BUSINESS descriptor/job rejection,
  multi-Mood filtering, Playlist Play all, HomePage copy, the safe example
  comment, or the Question FAB. Actual FAB geometry and playback remain MA checks.
- The evidence pack lists all 40 tracked candidates, 32 prior provenance
  candidates, and four current review provenance candidates. It excludes all
  other untracked paths by default, including captures, database patches,
  unrelated old records, and peer-owned evidence pending their review.
- Historical WI-009 reports 1,447 frontend tests and ten local media sets
  passing on its work-item date. Neither current playback nor production
  readiness follows from that report.

## Handoff

Only this summary and
`deliverables/agent/WI-20260905-ATS-001-evidence-pack.md` were written.
No staging, commit, process operation, database operation, external action,
client-worktree edit, or product fix was performed. MA can continue WI-002
with the identified scope; F1/F2 remain closeout findings and WI-003 retains
operational-readiness ownership.
