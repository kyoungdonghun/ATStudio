---
version: 1.0
last_updated: 2026-09-05
project: ATS
owner: se
category: work-summary
status: confirmed
dependencies:
  - path: ../agent/WI-20260905-ATS-005-evidence-pack.md
    reason: Source pointers and attributed runtime evidence
---

# WI-20260905-ATS-005 Summary

## Result

Diagnosis complete; the reproduced refresh scenario passes. No player code, tests,
product configuration, or ignored local configuration was changed. No temporary
logs were added.

MA isolated the failure to Origin handling: the same public Track batch request
returned 403 `Invalid CORS request` with Origin `http://127.0.0.1:5173`, but 200
with `http://localhost:5173`. The committed defaults/example permit localhost,
and the example already warns against the unconfigured 127.0.0.1 origin.
The earlier successful API request without Origin did not cover this failure.

Player restoration requires the batch request to resolve saved IDs. Stored
history can render immediately and falls back to local entries if its batch
fails, explaining why history remained visible while the PlayerBar was empty.
This evidence does not justify a new persistence patch.

## Remedy And Verification

MA tested the actual UI at `http://localhost:5173/tracks/4`: play -> pause ->
slider Home -> ArrowRight to 5 seconds -> reload. `AT.M Demo01` remained in the
PlayerBar with slider value 5 and time `0:05 / 0:07`, still paused. Result: PASS
for the reproduced refresh failure, with no player or CORS allowlist change.

Use that documented origin consistently. Storage from the 127.0.0.1 origin does
not automatically carry over; do not clear or migrate it for this test.

This is MA's observed browser result; SE did not operate the browser or rerun
product tests. Existing full-suite results remain earlier evidence. No new
history-modal or next-Track result is claimed for the final localhost run.

## Closeout Recommendation

WI-003 should record the exact browser origin and compare it with the effective
runtime CORS origin list before browser acceptance. Include the actual Origin
in the batch preflight; a no-Origin HTTP 200 missed this failure. Use explicit
approved deployment origins, with no wildcard or production local-alias
workaround. Peer-owned documentation was not edited.

Only this summary and `deliverables/agent/WI-20260905-ATS-005-evidence-pack.md`
were created by SE. No source/configuration rollback is needed.
