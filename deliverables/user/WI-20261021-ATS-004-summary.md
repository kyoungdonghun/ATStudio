---
version: 1.0
last_updated: 2026-10-21
project: ATS
owner: SE
category: reference
status: active
---
# WI004 Local Preparation Summary

The evening preservation candidate is retained unchanged. A separate profile-retry
candidate preserves the consumed evening trial using its exact metadata, without
reinstalling or changing the runtime, JAR or SPA.

Profile-retry local checks: Python 14 PASS and PowerShell 5 PASS. Independent PG
review, expiry and fresh cold checks are required before MA execution. No remote
operation, profile change, signup, mail send or public exposure occurred here.

See the WI004 Evidence Pack and build/WI067-profile-retry-20261021/README.md.

## Final MA-Reported Closeout

Original profile fields were restored; signup/mail are still incomplete because
the current preflight reproduces DNS_CHANGED. This does not establish the original
historical exception. Runtime and application code remain unchanged.

c5b491 confirms app/MySQL stopped, quarantine Nginx active, console logged out,
and the owned SSH key removed with no agent identities. 10e511 confirms the Windows
SSH agent is Stopped/Disabled. Physical safety and access cleanup are complete;
the original trial cleanup FAILED record remains preserved. No further native
actions were performed for this documentation closeout.
