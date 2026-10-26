---
version: 1.0
last_updated: 2026-10-21
project: ATS
owner: SE
category: reference
status: active
---
# Evidence Pack: WI-20261021-ATS-004

## Scope and References

Local preservation candidates only under the approved WI004 handoff and
REQ-20261011-ATS-007. Retained Tier 0 core principles, development standards,
documentation standards and glossary; security policy prohibits secret output.
MA alone owns remote execution; independent PG exact review is required.
Evidence-pack format follows create-wi-evidence-pack.

## Original Evening Candidate

- Path: build/WI067-evening-resume-20261021.
- Freeze: b60ea47109a60599c8ac8d10d14abca69f118be816516c21844d5e0bde9999cd.
- Local Python 12 PASS; PowerShell 5 PASS. Mechanical derivative retains cold,
  no-replace, durable-copy and callback-retirement checks, leaving tools installed.
- MA subsequently reports successful preservation; this is MA-owned remote
  evidence, not independent SE verification. Original frozen files remain intact.

## Profile-Retry Addendum

- MA metadata f7d424 identifies consumed evening release e946e4df8b09, expiry
  1792576491. No early execution or expiry-policy change is authorized.
- Path: build/WI067-profile-retry-20261021.
- Freeze: f6db2c0f0abc4a0a098dc96741a4db6f01119d2d3a25355cfe20e7e3d4af03cc.
- Operator SHA256: 84ce3b8d84f4f8f1376625a861cb5401ba7d70f8337036aa26ab774612402d2e.
- Metadata changes only: release identity/times, three evidence pins, archive
  suffix, missing finish.json, additional ledger/reservation records, log size.
- Python 14 PASS (tool receipt 9f7354), PowerShell 5 PASS (0394a8).
  Tests include AST comparison proving executable functions unchanged except
  archive suffix, exact metadata, expiry refusal, cold state, no-replace moves,
  partial failure, callback retirement, inert invocation and redacted failure.
- No original directory writes, remote access, credentials, app/DB start, profile
  edits, mail or public exposure. Tests are synthetic, not native acceptance.

## Reproduction and Handoff

Commands and exact invocation are in each candidate README. Use the current
candidate freeze only after PG approval; do not rerun its generator after review.
HOLD retains partial evidence and prohibits retry without diagnosis. MA must
confirm expiry and fresh cold state before invocation. Success preserves records
only; fresh Release/preflight/producer/Start are separate MA operations.

Current completion boundary: local candidate and tests complete; PG review and
native profile-retry preservation not performed by SE. No signup/mail PASS claim.

## Subsequent MA Evidence: Profile Retry Preflight Hold

This addendum preserves the preceding local completion boundary. The following
remote observations are MA-reported; SE performed source inspection only.

- 2493ff: preservation PASS. Initial malformed Release input 0b6351 held;
  7eef9c confirmed release/evidence/control absent before corrected submission.
- 991d3f: corrected new Release STAGED. cb8922: full native preflight PASS,
  producer STAGED. 27ceb6: Start invoked. d8b5c1/9fafda: PREFLIGHT HOLD,
  app NOT_RUN. No signup or mail acceptance follows from these stages.
- 59e066: subsequent read-only comparison found current Gmail IPv4 resolution
  different from the approved snapshot (DNS_MATCH false). This is evidence of a
  later DNS mismatch, not the captured exception from the failed Start.
- Cleanup reportedly stopped Nginx and did not restore quarantine. 00dc0e:
  Nginx/MySQL service-key comparisons matched baseline, overlays absent, app
  not-found/PID0, MySQL masked/PID0, Nginx inactive/PID0, no nft tables. Matching
  selected service keys does not prove active quarantine. MA/PG recovery pending.

### Minimal Read-Only Diagnosis

Source: build/WI067-auth-trial-v7/binding/native.py, lines 400-406 and 485-488.
Native.preflight first calls module preflight(approval); only if it returns does
it compare encoded baseline objects and raise BASELINE_CHANGED. DNS_CHANGED at
406 therefore precedes that comparison; the same label also exists in resolver
stub identity checks at 362-367, so an exception label alone is not its location.

Prefer retained failure traceback/structured diagnostics, exposing only the
exception code and source location, never private inputs or entire logs. If no
such record exists, original exception remains undetermined. Compare any saved
before/after baseline objects locally on-server and output differing field names
only. Keep DNS observations timestamped and separate from failure-time evidence.
Do not rerun full preflight while Nginx is inactive: that can raise NGINX_BASELINE
and describes cleanup state rather than the original failure. No app restart,
code change, gate relaxation or trial replay is part of this diagnosis.

## Subsequent MA Evidence: Recovery and Current DNS Reproduction

- Native4520a5: exact original quarantine restored successfully; app and MySQL
  remain stopped. This supersedes the pending physical recovery above, not the
  original cleanup result, which remains FAILED.
- After recovery, MA read-only `n.preflight(r.load())` returned DNS_CHANGED.
  This confirms a CURRENT reproducible preflight failure. The original controller
  retained no exception code; neither its historical exception nor stack location
  is established by this later reproduction. DNS_CHANGED also has multiple source
  locations, so the current code alone does not identify which assertion raised it.
- 86f733: the pinned quarantine ready.txt location still allows the former
  management address, while provider TCP443 permits only the current management
  address. The marker is fail-closed; configuration was not changed. This marker
  access mismatch is separate from the SMTP DNS blocker and is not evidence that
  the app was publicly exposed.
- No app start or mail occurred in this attempt. Original-profile retry remains
  pending; successful quarantine recovery is not signup/mail acceptance.

Before another attempt, MA should locate the current DNS_CHANGED assertion using
read-only code/location-only diagnostics and timestamped DNS observations. If the
approved SMTP IPv4 snapshot has changed, preserve the consumed attempt and obtain
fresh reviewed DNS/Release evidence through the existing bounded workflow; do not
edit the consumed Release, pin stale addresses, or suppress DNS checks. If repeated
fresh attempts cannot maintain the required DNS identity, stop retrying and review
that assumption explicitly rather than consume another human test window.
Treat the quarantine marker's address mismatch separately: any change to its
pinned configuration requires the appropriate reviewed configuration/hash update,
not an unreviewed edit or widened public access. No code or remote operation was
performed by SE for this addendum.

## Final MA Closeout: Access Cleanup

- c5b491: app PID0/failed; MySQL PID0/inactive; quarantine Nginx active,
  PID93012. tty1 has agetty only, confirming console logout. The owned SSH key
  was removed and the agent reported no identities.
- 10e511: authorized elevation completed; Windows SSH agent Stopped/Disabled.
- User's original profile fields were restored, but signup/mail remain incomplete
  because of the current DNS preflight gate. No runtime or application code change.
- Physical safety and access cleanup are complete according to MA evidence. The
  original trial cleanup FAILED record is preserved, not relabeled successful.
- Final boundary: documentation updated only by SE; no further native actions,
  retries, app starts, mail sends or public exposure authorized by this closeout.
