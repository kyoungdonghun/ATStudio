---
version: 1.0
last_updated: 2026-09-08
project: ATS
owner: se
category: work-summary
status: stable
dependencies:
  - path: REQ-20260908-ATS-002.md
    reason: Approved presentation-only request
  - path: ../agent/WI-20260908-ATS-006-evidence-pack.md
    reason: Verification evidence
---

# WI-20260908-ATS-006 Summary

## Outcome

Payment email wording is now Korean. Customer subject: `[AT.M] 구독 결제 안내`; operator subject/heading: `결제 점검 이슈`.

Retry guidance retains the grace deadline. Suspended guidance identifies the service-access grace expiry rather than implying another retry. Order IDs, status codes, HTML escaping, secret handling and payment behavior are unchanged. Existing verification/password-reset email copy is untouched.

## Verification

Java 17.0.12: **38 focused unit tests passed**, zero failures/errors/skips. Tests cover UTF-8 MIME roundtrip, Korean defaults, adversarial dynamic fields, retry/suspended/expired-grace guidance and operator labels. Scoped diff checks passed.

Outputs are isolated under `C:/Users/jm991/AppData/Local/ATStudio/remote-development-20260908/build-copy-polish`; existing closeout artifacts and running JAR were not overwritten. No SMTP, real Provider or DB calls, server restarts, commits or other-worktree edits were performed.

## Boundary

Source is frozen and ready for MA/WI-20260908-ATS-008. Live delivery, inbox placement, running-server adoption and aggregate/browser validation remain outside this WI. Rollback, if authorized, must reverse only this WI's owned changes without undoing prior dirty work.

## Related Documents

- [Approved REQ](REQ-20260908-ATS-002.md)
- [Evidence pack](../agent/WI-20260908-ATS-006-evidence-pack.md)
