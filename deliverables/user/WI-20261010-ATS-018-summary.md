---
version: 1.0
last_updated: 2026-10-10
project: ATS
owner: docops
category: work-summary
status: stable
dependencies:
  - path: REQ-20261010-ATS-007.md
    reason: Approved first-host scope and active progress
  - path: ../agent/WI-20261010-ATS-018-evidence-pack.md
    reason: Integration evidence and final-snapshot gate
---

# WI-20261010-ATS-018: First-Host Documentation Integration

> Purpose: Separate local procedure review from MA-attributed host execution.

## Status

WI018 documentation integration complete; REQ007 stays active and production HOLD.
Verification date2026-10-10: live host/Windows operations are MA-owned evidence;
SE/PG checks are local procedure evidence; DOCOPS verified document integration.

## Changes and Limits

- Current decision entry links REQ007, WI016/017/018 and the first-host runbook.
- Deployment README provides the same current navigation above older preparation.
- REQ007 retains original progress/approval history, adds explicit MySQL distribution
  initialization approval and checks only achieved criteria.
- Purchased/rebuilt host, recovery-console identity and strict-pinned native SSH
  key login/reconnection passed. The user privately rotated the initial password.
- Java17.0.20.1, Nginx1.24.0, FFmpeg6.1.1 and MySQL8.0.46 installed; Nginx/MySQL
  remain masked/inactive. No ATStudio DB/schema/users/app secrets or app deployment.
- One-second synthetic MP3 encoding passed, not real-WAV/upload/load acceptance.
- Owned agent key removed and empty list verified; MA's final `Get-Service
  ssh-agent` at19:57:21KST confirmed Stopped/Disabled. The noVNC root console was
  logged out; MA's fresh screenshot showed the Ubuntu tty1 login prompt.
- Separate admin/sudo/second-session proof, permissive SSH defaults, service
  directories, log retention and MySQL tuning remain unresolved setup work.
- Domain purchase precedes DNS/HTTPS; app, backup/restore, alerts, load and release
  remain pending. No additional goods purchased; monthly budget is not a cost cap.

## Validation

Scoped metadata, relative-file links, dependencies, whitespace and preservation
checks are recorded in the Evidence Pack. Full repository validation and product
tests were not run. No historical receipt, reviewed runbook or source was edited.

## Related Documents

- [Current entry](../../docs/index.md#2026-10-10-first-host-checkpoint)
- [REQ007](REQ-20261010-ATS-007.md)
- [Evidence Pack](../agent/WI-20261010-ATS-018-evidence-pack.md)
