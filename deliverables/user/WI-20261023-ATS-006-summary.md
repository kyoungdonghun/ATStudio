---
version: 1.0.0
last_updated: 2026-10-23
project: ATS
owner: PG
category: summary
status: stable
related_wi: WI-20261023-ATS-006
---

# WI006: independent fixture preparation review

The exact WI005 local candidate passes independent preparation review. Its
manifest SHA256 is
`3a0f4fd30afc012a8e1a0c46d70b87ed1d2ef5492c2f8143121dc741da35654c`:
all 22 files match, and 38 focused tests pass (12 implementation, 26 independent).
Existing 643 source files, the retained JAR and previous frozen tools match.

The proposed future effects are two new users, promotion of one new user to
ADMIN with one audit row, and one non-payment subscription fixture for the
other new USER. ID1 and every existing row are preserved. The grant uses an
explicitly selected existing active INDIVIDUAL plan, MONTHLY/CANCELLED,
same-day KST start/expiry and no pending renewal fields. No payment, plan,
consent or email-verification event is created.

Review found and closed transport error/exit handling and reconciliation log
reuse defects. The final candidate also binds external approval to exact bytes,
target, inputs, plan/date and fixture semantics; it checks approval again before
DML/COMMIT. Lost COMMIT acknowledgement stays HOLD_RECONCILE with no automatic
retry. Large snapshots are rejected before they can exceed the recovery reader.

This is local preparation only. No server access, DB startup, account/data
change, login, mail or audio execution was performed for WI005/WI006. Actual
credentials, plan selection, native MySQL behavior and cleanup remain unverified.
The audio hash improvement is locally implemented; native comparison is NOT_RUN.
Future server work and fixture changes require a separate explicit user approval
and fresh target gates. The tool has a documented activation procedure without
a source-code toggle; current local approval cannot activate it.

Evidence: [review pack](../agent/WI-20261023-ATS-006-evidence-pack.md),
[exact receipt](../../build/WI20261023-006-fixture-review/review-receipt.json),
[future operator recipe](../../build/WI20261023-005-account-fixture/README.md).
WI006 unblocks WI007 integration. REQ007 remains active.
