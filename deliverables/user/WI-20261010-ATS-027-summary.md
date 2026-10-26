---
version: 1.0
last_updated: 2026-10-10
project: ATS
owner: PG
category: work-summary
status: stable
dependencies:
  - path: ../agent/WI-20261010-ATS-027-evidence-pack.md
    reason: Independent review evidence and mandatory execution boundaries
  - path: REQ-20261010-ATS-008.md
    reason: Exact target authorization and exclusions
---

# Production Bootstrap Security Review

> Purpose: Decide whether the exact approved DB preparation may proceed.

## Decision

TL;DR: **PASS for the reviewed preparation and bounded MA execution procedure.**
No new blocking security defect was found in the final WI025 candidate.
MA may stage the exact public bundle and run native preflight. Apply may run
once only after every current target gate passes, with mandatory stop/remask
and temporary SSH-agent cleanup. This is not a target execution receipt.

Approved bundle SHA-256:
`891578294cbb621256ad9324dbaee9d5ed2efe3e2f51fbb22b33d2972de8e80c`.
Only the new `atstudio_prod`, loopback `atstudio_app`, six baseline plans and
three independently generated server-side secrets are covered. Any collision,
unknown outcome, source drift or failed guard means HOLD; no retry or deletion.

## Verification

- PG independently passed 32 bootstrap tests, 39 deployment-validator tests,
  12 additional grant-rejection probes and a malformed-native-output probe.
- Final WI025 hashes match. WI026 JAR, 974 source inputs, 143 preserved old
  frontend files and packaged entries were independently hash-checked.
- WI026's approved isolated frontend completion is now PASS: 143 new files,
  1,194,351 bytes, independently hash-checked. Its earlier environment HOLD is
  historical, not the current artifact status. No TEST OAuth value was needed
  by the reviewed isolated build command.
- Review owner PG; Windows local checks on 2026-10-10. Actual host observations
  are MA-attributed. PG performed no remote/SQL/secret operation or app startup.

## Remaining Gates

Production remains **HOLD**. Actual native success/cleanup, runtime TCP/JDBC
authentication, Hibernate validation, canonical fresh manifest, domain purchase
then DNS/HTTPS, SMTP/Provider activation, first ADMIN, encrypted off-host secret
recovery, backup/restore, alerts and load remain separate gates. Root privilege
metadata is not proof of application authentication. Local builds are not
browser, production, capacity or complete security acceptance.

MA must immediately trigger the skill-generated WI028 handoff for actual
receipt integration, carrying both successful and failed/held outcomes. PG
does not delegate descendants or close REQ008.

## Related Documents

- [Evidence Pack](../agent/WI-20261010-ATS-027-evidence-pack.md): Hashes, checks and staging conditions.
- [Approved requirement](REQ-20261010-ATS-008.md): Authority and exclusions.
- [Operator runbook](../../deploy/single-vps/runbooks/production-bootstrap.md): Native phases and cleanup.

## Native HOLD and Corrected Review Addendum (2026-10-10)

TL;DR: **Corrected candidate PASS; native SELECT and full preflight must pass
before one apply.** The earlier bundle/path remain historical, not executable
approval for the resumed attempt.

MA reported the first configuration check stopped before DB/account or private
bootstrap-file creation because disabled admin_address was NULL. MySQL was
stopped/remasked per MA. User explicitly approved the narrow fix and re-review.
PG verified only the intended helper predicate changed, reviewed the focused
tests/history additions and independently passed34/34 tests. Mocks do not prove
native SQL semantics.

Accepted replacement bundle:
`15aa91639be237e03c71b7f2df2b4923879b849981b9abdf71efda19aedea91d`.
Use fresh real `/root/wi025-v2`, directly execute its helper with the same
--root, and preserve `/root/wi025`. The MA-reported201216-byte TAR identity is
recorded in the Evidence Pack; its exclusive root copy and exact16-file
inventory must be verified before extraction. No prior approval reuse.

During the approved temporary loopback MySQL window, require SELECT results
1,1,0,0,0,1 for NULL/empty/IPv4/IPv6/hostname/actual predicate, unchanged listener
checks and every full preflight gate. Any mismatch or uncertainty means HOLD,
preserve state and stop/remask. No global configuration change, automatic retry
or app startup. Mandatory MySQL/SSH-agent cleanup and WI028 actual-receipt
integration are unchanged. PG performed no remote or SQL operation.

## V3 Classification Review Addendum (2026-10-10)

TL;DR: **V3 PASS; native semantics and all target/preflight gates remain mandatory
before one apply.** This supersedes earlier execution candidates without
rewriting their findings or hashes.

PG independently passed37/37 tests and offline plan, verified that only the
plugin query changed from v2, and reproduced the previous helper/test bytes by
in-memory reverse-diff hashes. All three WI025 documents preserve their v2
prefixes. BINARY name comparison rejects case/space aliases; only the two exact
cleaner names with SQL NULL library are exempt. Unknown AUDIT, non-NULL/empty
library and ACTIVE mysqlx/group_replication still block. No additional blocking
defect was found; the fixtures are not native SQL proof.

Accepted bundle:
`b91766934c522b1c4549b8a3057f95eae94c9a542e107fb9c7c39d7df3b24b9c`.
Use fresh real `/root/wi025-v3`, its direct helper and matching --root. Preserve
v1/v2. Register and verify a new16-file TAR identity; no old checksum/approval
reuse. After native SELECT fixtures, all read-only guards and plan equality,
enroll a new root0600 approval, run full preflight, then at most one apply.
Always stop/remask and clean up the approved temporary agent; any discrepancy
means HOLD and preservation without retry. No server setting or plugin change.

MA's second native HOLD and subsequent cleaner identity/package observations
are recorded as attributed evidence. mysqlx_cache_cleaner also handles X server
startup/shutdown, so X-plugin/listener exclusion remains required. PG performed
no remote/SQL/secret operation. WI028 must record actual execution and cleanup;
app startup and production acceptance remain outside this review.
