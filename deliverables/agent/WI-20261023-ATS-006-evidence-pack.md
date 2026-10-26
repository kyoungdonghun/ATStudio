---
version: 1.0.0
last_updated: 2026-10-23
project: ATS
owner: PG
category: evidence-pack
status: stable
related_wi: WI-20261023-ATS-006
---

# Evidence Pack: WI-20261023-ATS-006

## Summary

`LOCAL_PREPARATION_REVIEW_PASS` for the exact WI005 candidate manifest SHA256
`3a0f4fd30afc012a8e1a0c46d70b87ed1d2ef5492c2f8143121dc741da35654c`.
All 22 listed files and the complete candidate file inventory match. The 12
implementation tests and 26 independent tests pass. No unresolved local review
blocker remains in this bounded candidate.

This verdict covers local construction/review only. It grants no server access,
staging, DB startup, DML, login, mail or audio authority. Actual Linux/MySQL
execution, target restoration and audio/hash comparison remain NOT_RUN.

## Scope / DoD Check

- [x] Read the exact final implementation without editing SE-owned files.
- [x] Independently challenge transaction, pipe, authority and preservation paths.
- [x] Verify runnable offline planning and future external-approval activation.
- [x] Review the existing source-mode lifecycle and exactly two writable flags.
- [x] Verify source643, retained JAR and earlier frozen candidates unchanged.
- [x] Provide reproducible commands, exact hashes and two-set deliverables.
- [x] Notify MA that WI006 unblocks WI007 integration; REQ007 remains active.

PG owns only the new review directory and these WI006 deliverables. No native
operation, implementation edit, dependency installation, commit or push was made.
All test identities/passwords are synthetic. The approved model was retained.

## Reference Documents (Tier 0-2)

Context was injected through the [WI006 handoff](WI-20261023-ATS-006-handoff.md).
The create-wi-evidence-pack skill and focused-test reporting workflow were used.

| Tier | Pointer | Applied boundary |
| --- | --- | --- |
| 0 | [core-principles](../../docs/standards/core-principles.md) | Bounded approval and evidence |
| 0 | [documentation-standards](../../docs/standards/documentation-standards.md) | English documentation and pointers |
| 0 | [development-standards](../../docs/standards/development-standards.md) | Preserve unrelated work |
| 0 | [glossary](../../docs/standards/glossary.md) | REQ/WI terminology |
| 1 | [security-policy](../../docs/policies/security-policy.md) | No secret/private-data output |
| 1 | [quality-gates](../../docs/policies/quality-gates.md) | Independent exact-byte review |
| 2 | [REQ007](../user/REQ-20261011-ATS-007.md) | Explicit local-only fixture extension |
| 2 | [current entry](../../docs/index.md) | Current versus historical evidence |
| 2 | [WI005 handoff](WI-20261023-ATS-005-handoff.md) | Two-account fixture contract |
| 2 | [WI003 native interpretation](WI-20261023-ATS-003-evidence-pack.md) | Phase1 observation and remaining gates |
| 2 | [WI004 summary](../user/WI-20261023-ATS-004-summary.md) | Earlier phase boundary |

## Exact Evidence

- [Review receipt](../../build/WI20261023-006-fixture-review/review-receipt.json), SHA256
  `0ada7e5a4e36d57f4433882b3ddfd162b574e32b936d249c9e6f1072536849e4`.
  It records all 22 candidate file hashes, individual review source hashes,
  test commands/counts/log hashes and source/artifact checks.
- [Candidate manifest](../../build/WI20261023-005-account-fixture/candidate.json),
  SHA256 `3a0f4fd30afc012a8e1a0c46d70b87ed1d2ef5492c2f8143121dc741da35654c`.
- [Operator recipe](../../build/WI20261023-005-account-fixture/README.md): offline
  request/plan, future inspection, distinct apply nonce and read-only reconciliation.
- [Implementation test output](../../build/WI20261023-006-fixture-review/implementation-tests.txt): 12 PASS.
- [Independent test output](../../build/WI20261023-006-fixture-review/independent-tests.txt): 26 PASS.
- [Core tests](../../build/WI20261023-006-fixture-review/test_core_review.py): 14;
  [transport tests](../../build/WI20261023-006-fixture-review/test_transport_review.py): 4;
  [authority/CLI tests](../../build/WI20261023-006-fixture-review/test_gates_review.py): 8.
  The [pipe peer](../../build/WI20261023-006-fixture-review/fake_mysql.py) is Python,
  never a database process.

Reproduce from the repository root with:

```powershell
python -B build/WI20261023-006-fixture-review/verify_review.py 3a0f4fd30afc012a8e1a0c46d70b87ed1d2ef5492c2f8143121dc741da35654c
```

This runs only the focused synthetic suites, Java BCrypt helper tests and local
file identity checks, writing review-owned receipts. It never invokes native
fixture modes with authority or starts an application/database. Test timing may
change output hashes on a later run; the reviewed candidate hash must stay exact.

## Findings and Closure

| Finding | Final treatment and evidence |
| --- | --- |
| Short stderr ERROR could remain buffered while a separate stdout ACK was accepted | Reproduced with a real Python child pipe. Final transport merges stderr/stdout at the OS pipe and rejects non-JSON diagnostics before ACK. Independent regression passes. |
| Session close ignored nonzero child exit | Reproduced with exit7. Final close checks return code and failed state; regression passes. |
| Apply and reconciliation reused an immutable log name | Final unit/log name includes mode and approval digest; reconciliation preserves the original claim while using a new approved operation log. |
| Crypto wildcard could load an unlisted extra JAR | Final verifier enforces the entire candidate file inventory and the Java classpath explicitly names the two pinned libraries. |
| Cross-table primary keys were incorrectly treated as globally unique during draft review | Separate users remain distinct; audit1 and grant1 are valid. Independent exact-delta regression passes. |
| Approval could become stale during a transaction | Mandatory guard is called at entry, before first DML and before commit evidence; the expiry regression proves no COMMIT is sent. |
| Total snapshot size must fit reconciliation's4MiB reader | Final frozen code already had the total canonical-size guard. Added independent oversized multi-table regression passes; no further implementation edit was needed. |
| Phase1 wording overstated absence of all subscription rows | Final README says no effective subscriptions and distinguishes its stronger future no-row guard. |

The earlier transport regressions failed before fixes; those failures are not
native DB results. The final 38-test result is against the manifest above.

## Reviewed Mutation and Recovery Contract

The proposed future transaction inserts exactly two users using default USER,
promotes only the designated new admin, inserts one `USER_ROLE_CHANGE` audit,
and inserts one `user_subscriptions` row for the other new USER. The grant uses
an existing active INDIVIDUAL plan with finite quota 1..1000000, MONTHLY/CANCELLED,
same-day KST start/expiry and both pending fields NULL. It creates no plan,
payment, billing agreement, consent, verification event, token, playlist, license
or download row. ID1 and every existing cell across the captured tables remain
equal; additional rows outside the exact delta cause precommit failure.

Fixed SQL values use validated hex UTF8MB4 literals with explicit collation.
The single no-force, no-reconnect socket client and SERIALIZABLE transaction
recheck metadata, absence of triggers/other clients, selected plan, identities,
work state and exact before snapshot. Private before/expected-after/commit-intent
records precede COMMIT. Lost COMMIT ACK or durable acknowledgement failure means
HOLD_RECONCILE even if a subsequent ROLLBACK succeeds. Claims prevent automatic
replay. Reconciliation is read-only and can confirm only the recorded before or
expected-after state. Auto-increment gaps from aborted inserts are possible.

The original source helper remains unchanged. Apply alters only its read-only
and super-read-only flags; network, event, replication, persisted-global and
finite-lifetime containment remain. Inspection/reconciliation retain read-only.
Session close and STOP/PID/socket/baseline cleanup are checked independently.
Forced controller termination can bypass finally; the finite systemd lifetime
does not itself prove restoration, which requires future observed receipts.

## Preservation and Validation Limits

Verification date 2026-10-23, local Windows, evidence owner PG. Branch `main`,
HEAD `91a16e72693d7cd6ed04e0d36a5bd160af96f6d8`. Source 643 mismatch0;
retained JAR SHA256
`f3b748f8b3fb4fbd4027b797ebf0bc79db62bc9671e5d7e45a8397732d2aef81`.
WI013 62, WI001 68, WI002 4, v8r1 runtime 16/binding 42, delivery 12 and producer 7
manifest members all match. Reused 6 files match their originals in the tests.
No application rebuild was performed.

Actual plan ID/properties and the two new account inputs remain unverified.
ID1's actual credentials/password login are also unverified. The actual retained
Java17 BCrypt library was exercised locally with synthetic inputs; this does
not prove server Java/MySQL behavior or an application login. MySQL SQL syntax,
locking, native root socket permissions, DML and cleanup were not executed here.
The audio stream/file linkage improvement remains implemented and locally
tested; its native comparison is still NOT_RUN.

Future execution requires an actual user decision and exact external operator
records, current candidate/target/input binding, fresh read-only inspection,
explicit selected plan/date and a separate apply record. A JSON marker alone is
not proof of human authorization. There is no manual source toggle requirement.
Fixture preparation must finish before the frozen audio baseline; that audio
contract continues to disallow account/role/profile and entitlement writes.

## Rollback / Handoff

No business rollback is required for this local review. Preserve candidate,
claims, logs and historical artifacts. A future uncertain commit is reconciled,
never automatically replayed or compensated by delete/update. Changes to this
candidate require a new exact review. MA was notified that WI006 unblocks
WI007; current native authority remains absent.
