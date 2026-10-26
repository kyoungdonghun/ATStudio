---
version: 1.2.0
last_updated: 2026-10-24
project: ATS
owner: PG
category: evidence-pack
status: stable
related_wi: WI-20261024-ATS-002
---

# Evidence Pack: WI-20261024-ATS-002

## Exact Verdict and Approved Scope

`ALLOW_EXACT_APPROVED_SINGLE_FIXTURE_APPLY` for candidate SHA256
`f42d9cfe325f8105d908db46f67b5fa93bc72ada6178eb878af8f75a2f5f7719`.
All five members match; bundle
`5fedfa3187ed83637b31bf5413369ee86a89a6fc8fc45d8c1307704312b3d262`
is independently reproduced. MA received the exact allowance before starting
human/access preparation. Independent 8 and implementation 7 focused tests pass.
No open local finding remains; actual native application is PENDING MA evidence.

The current actual user decision quoted in the [WI002 handoff](WI-20261024-ATS-002-handoff.md)
authorizes local preparation/review, a fresh successful inspection, then the
exact fixture application and DB/access restoration. Earlier local-only or
inspect-only approvals are not write authority. The JSON marker records that
actual decision; it does not independently create authority.

The scope is fixed to **2026-10-24 Asia/Seoul**, existing plan1 / INDIVIDUAL /
active1 / daily quota5. Effects are users INSERT2; only the new admin USER-to-ADMIN
UPDATE1; audit INSERT1; buyer subscription INSERT1. Both accounts use verified
TEST flags only. Subscription is MONTHLY/CANCELLED, start=expiry=2026-10-24,
pending fields NULL. Existing ID1 and every pre-existing row/cell remain immutable.
No schema change, app startup/login, mail/consent event, audio, payment/refund,
billing agreement/renewal, publication, date extension or automatic retry is allowed.

## Method and Governing Context

The handoff supplies Tier0 [core principles](../../docs/standards/core-principles.md),
[development standards](../../docs/standards/development-standards.md),
[documentation standards](../../docs/standards/documentation-standards.md) and
[glossary](../../docs/standards/glossary.md); Tier1
[security policy](../../docs/policies/security-policy.md) and
[quality gates](../../docs/policies/quality-gates.md); Tier2
[REQ007](../user/REQ-20261011-ATS-007.md), [current entry](../../docs/index.md) and
[prior integration](WI-20261023-ATS-013-evidence-pack.md).

Applied create-wi-evidence-pack and focused-test reporting. Reviewed the delta
against frozen WI005 core/native and the dated [WI006 review](WI-20261023-ATS-006-evidence-pack.md),
[WI012 native interpretation](WI-20261023-ATS-012-evidence-pack.md) and
[WI015 capture review](WI-20261023-ATS-015-evidence-pack.md). Earlier native results
are reuse evidence, not current target/row freshness. PG owns only its review
directory and these WI002 documents; no implementation or frozen file was edited.
No actual private input, credential, process memory, UI, server or DB was accessed.

## Evidence and Reproduction

- [Exact receipt](../../build/WI20261024-002-fixture-apply-review/review-receipt.json),
  SHA256 `90610b8a978af0de31bab7b4aba69b7068975d6d0d2cd1feae56056274e4d2a1`.
- [Candidate](../../build/WI20261024-001-fixture-apply/candidate.json),
  [operator recipe](../../build/WI20261024-001-fixture-apply/README.md),
  [flow](../../build/WI20261024-001-fixture-apply/flow.py) and
  [remote adapter](../../build/WI20261024-001-fixture-apply/remote_apply.py).
- [Independent tests](../../build/WI20261024-002-fixture-apply-review/test_flow_review.py),
  [independent output](../../build/WI20261024-002-fixture-apply-review/independent-tests.txt),
  [implementation output](../../build/WI20261024-002-fixture-apply-review/implementation-tests.txt)
  and [identity verifier](../../build/WI20261024-002-fixture-apply-review/verify_review.py).

```powershell
python -B -m unittest discover -s build/WI20261024-001-fixture-apply -p test_flow.py -v
python -B -m unittest discover -s build/WI20261024-002-fixture-apply-review -p test_flow_review.py -v
python -B build/WI20261024-002-fixture-apply-review/verify_review.py
```

Verification: 2026-10-24, Windows local, owner PG. Tests use synthetic `.invalid`
identities, injected clocks/day/native calls and private in-memory records.
The changed-source bootstrap test stops before executable source runs. It does
not invoke a native runner or contact a host. No broad application/auth/audio
suite was run.

Source643 mismatch0 and retained JAR
`f3b748f8b3fb4fbd4027b797ebf0bc79db62bc9671e5d7e45a8397732d2aef81`
match. WI00522, WI0115 and WI0144 members and their historical manifests remain
unchanged. The new candidate bundle is SHA256 of UTF-8
`json.dumps(files, sort_keys=True, separators=(',', ':'))`, where files is its
relative-name-to-member-hash map. It differs from older byte-concatenation bundle
recipes; the verifier uses each candidate's actual recipe rather than assuming
one universal format. Exact manifest and each member are checked separately.

## Findings Closed

1. The first builder rejected preserved WI011 `test-output.txt`, which was never
   a manifest member. A narrow local reproduction returned OLD_VERIFY_HOLD.
   The fix permits only this known nonexecuted log while preserving exact hashes
   for all five declared files and rejecting other inventory drift. Nothing was
   deleted or rewritten in WI011. The final synthetic payload build passes with
   that log retained and unchanged.
2. The README's first SSH example omitted retained Python isolation flags.
   `-I -S -B` was restored before the final manifest. MA separately confirmed the
   full existing strict host/key/agent/forwarding/password/TTY transport controls
   remain unchanged. This was a recipe omission, not a native code failure.

## Reviewed Safety and Effect Boundaries

- External record raw bytes/pin, wrapper/frozen manifest, canonical input digest,
  actual target, fixed day/plan/effects, distinct nonces and <=900-second expiry
  are bound. Canonical comparison rejects bool/int substitutions in plan/effects.
  The wrapper checks target boot/machine and retained source/cold prerequisites.
- New operation and phase directories are root0700; records are exclusive
  root0600 files. Existing operation/native nonce collisions stop the operation.
  Phase HUMAN/APPROVAL globals change only for the call and restore in finally.
  Existing fixed approval files are neither read nor overwritten by this wrapper.
- A new read-only inspection and exactly all five cleanup PASS results precede
  planning. Fresh target/time, zero ADMIN/work/collision, true ID1 guard and exact
  plan1/quota5 are checked; authority/day are checked again before apply.
- The unchanged native/core retains containment, SERIALIZABLE snapshot drift,
  all old row/cell comparisons, exact allowed delta and pre-COMMIT date/authority
  checks. Audit/grant ID1 remains legal because those are independent tables;
  the two new user IDs must be distinct and greater than1.
- Apply is called at most once. Lost return, postapply evidence failure or
  uncertain native result yields HOLD_RECONCILE without retry/reconcile calls.
  Known ACK, numeric IDs and cleanup survive later receipt failures. A lost
  response never proves non-application. Public projection excludes identities,
  private digests, snapshots, SQL, passwords and raw exception text.
- The private stdin payload carries only exact declared executable sources;
  bootstrap hashes them before compilation/execution. The local builder checks
  private ACL/no-link rules and writes payload/hash exclusively, without printing
  either. Payload/input/record/target digests stay private.

## Required Native Evidence and Stop Rules

Synthetic success does not establish native SQL, current collisions, successful
commit or restoration. MA must use fresh target and actual-decision binding,
retain safe inspection/application/commit/IDs/cleanup output, run an independent
target-bound cold post-probe and confirm separate provider/firewall/key/agent
restoration. Actual credential login remains outside scope.

On date rollover, expiry, drift, cleanup failure or uncertain commit, stop
writes and preserve records. Do not select new nonces, overwrite/delete old
approvals or replay apply to force progress. Any read-only reconciliation needs
the appropriate separately confirmed authority. Current native interpretation
is PENDING; a later result must be additive and preserve this pre-execution review.
WI003 integration follows MA's bounded execution; MA and DocOps received the
exact local allowance immediately.

## Native Application Addendum: 2026-10-24

`OBSERVED_EXACT_FIXTURE_APPLIED_AND_DB_CLEANUP_PASS`. This additive result
preserves the pre-execution review receipt unchanged at
`90610b8a978af0de31bab7b4aba69b7068975d6d0d2cd1feae56056274e4d2a1`.
MA owns the actual execution observed at 2026-10-24T00:47:20.506188+09:00.
PG read only the sanitized [apply result](../../build/WI20261024-fixture-apply-native/apply-result.json)
and [post-stop probe](../../build/WI20261024-fixture-apply-native/post-stop-probe.json).
The [native interpretation](../../build/WI20261024-002-fixture-apply-review/native-interpretation.json)
records exact receipt hashes and limits. No extra tests, private input/snapshot
reads, database query, UI inspection or server access occurred.

The result is APPLIED/COMPLETE, COMMIT ACKNOWLEDGED, SSH exit0. New numeric IDs are
admin user2, buyer user3, audit1 and grant1. Audit/grant IDs refer to independent
tables and do not indicate a change to existing user1. The receipt reports the
exact approved effects: users INSERT2, new admin role UPDATE1, audit INSERT1 and
subscription INSERT1, day2026-10-24, plan1 and daily quota5.

The preceding inspection is INSPECTION_RECORDED/COMPLETE. Both inspection and
application report DB_SESSION, STOP, PID_ZERO, SOCKET_GONE and BASELINE all PASS.
The separate post-stop probe is PREFLIGHT_PASS/COLD_QUARANTINE_MATCH, inventory
NOT_RUN and SSH exit0. These results establish the recorded DB cleanup boundary.

Existing row/cell preservation, including ID1, is supported by the unchanged
reviewed transaction's exact before/after checks and acknowledged COMMIT. PG did
not independently query those rows or read private snapshots. The fixed delta
also binds the buyer's subscription to MONTHLY/CANCELLED, start=expiry2026-10-24
and NULL pending fields; these detailed rows are not projected publicly. Verified
flags mean TEST fixtures only, not an email verification or consent event.

No app startup/login, audio, mail/consent, payment/refund/billing agreement/renewal
or automatic date extension is established or authorized. Actual credential
login remains outside scope. Source/target/private-payload provenance remains
MA-owned; PG has not independently reconnected or inspected live code.

Access restoration remains PENDING its separate final safe receipt. The current
DB cleanup PASS must not be promoted to firewall/provider/key/agent restoration
PASS. Preserve all operation evidence; do not replay apply or extend the approved
day. WI003 receives this native interpretation while access completion is pending.

## Access Restoration Completion Addendum: 2026-10-24

`BOUNDED_EXACT_FIXTURE_APPLIED_AND_RESTORATION_ACCEPTED`. MA's
[final restoration receipt](../../build/WI20261024-fixture-apply-native/final-restoration.json)
has SHA256 `6f0ca8d658cf58efb9acedd522cf7e6d76a0da4c0f07b52024e13da9e87e9830`
and records completion at 2026-10-24T00:49:25.5513623+09:00. Its apply and post-stop
receipt hashes exactly match the two independently read files. The five DB
cleanup checks and cold post-probe results are consistent throughout.

The MA-owned access observations report original TCP22 source restored,
ports80/443 unchanged, provider logout, zero created browser tabs, owned key
removed, zero identities before agent stop, agent Stopped/Disabled, key input
window closed, private ACL verified and private inputs/evidence retained. PG
verified all four referenced screenshot byte hashes without viewing their
contents. No direct UI, OS, DB or server revalidation occurred; these operational
observations remain attributable to MA. No claim is made about unrelated windows.

The [additive access interpretation](../../build/WI20261024-002-fixture-apply-review/access-interpretation.json)
records the complete bounded acceptance and verification limits. The earlier
native interpretation remains unchanged at SHA256
`f3f3488512483c8c9cced227f6f7fbe3ba9bf0e80b8418a2b93de9d8c595532c`;
its access-PENDING field is a historical state superseded by this addendum.
The original exact local review receipt is also unchanged.

No separate post-COMMIT SQL reread was performed. Committed outcome and existing
data preservation are supported by the reviewed exact-delta transaction and
ACK. TEST flags do not prove email verification, consent or login. App startup,
login, audio, payment/refund, mail/consent, schema change, deletion and automatic
write retry are all excluded in the final receipt. The entitlement date remains
2026-10-24 only; no extension or replay is authorized. WI002 is complete for this
bounded operation and WI003 integration is unblocked.
