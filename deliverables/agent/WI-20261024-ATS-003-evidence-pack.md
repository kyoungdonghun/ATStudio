---
version: 1.2.0
last_updated: 2026-10-24
project: ATS
owner: DocOps
category: evidence-pack
status: stable
related_wi: WI-20261024-ATS-003
dependencies:
  - path: WI-20261024-ATS-003-handoff.md
    reason: Exact dated-write approval and bounded integration contract
---

# Evidence Pack: WI-20261024-ATS-003

## Summary

Final outcome: APPLIED / COMMIT ACKNOWLEDGED, observed by MA at
2026-10-24T00:47:20+09:00. Exact approved effects created ADMIN user 2, buyer
USER 3, audit 1 and grant 1. Inspection/apply cleanup each passed five checks;
the separate cold post-probe and final access restoration passed. PG returned
BOUNDED_EXACT_FIXTURE_APPLIED_AND_RESTORATION_ACCEPTED. WI003 is complete;
REQ007 remains active for separate app/login/audio. No date extension is approved.

## Historical Local-Readiness Checkpoint

Local preparation/review is complete: exact candidate allowed under
ALLOW_EXACT_APPROVED_SINGLE_FIXTURE_APPLY, with SE 7 + PG 8 focused checks PASS.
MA local preflight passed. Provider login and a TCP22 proposal are prepared;
actual rule submission, SSH, DB and writes have not occurred at this checkpoint.
The specific temporary rule change awaits user confirmation at the browser
security-action boundary. WI003 and REQ007 remain active; native outcomes are pending.

## Historical Initial Approval Checkpoint

Recorded explicit approval of the exact account/entitlement writes for
2026-10-24 Asia/Seoul, conditional on local preparation, exact independent review
and fresh passing inspection, followed by DB shutdown and access restoration.
Preparation/review/native receipts are pending. No new inspect/apply or cleanup
success is claimed. WI003 and REQ007 remain active; prior WI013 stays complete.

## Scope / DoD Check

- [x] Existing generated handoff verified and verbatim approval recorded in REQ007.
- [x] Exact effects, fixed KST date, preservation and excluded scopes recorded.
- [x] SE001 -> PG002 -> MA inspect/apply/cleanup -> Doc003 chain recorded.
- [x] Historical WI013 and the unknown original Save failure preserved.
- [x] Initial scoped documentation validation recorded.
- [x] Exact candidate/review/test/preservation evidence integrated.
- [x] Actual fresh inspection and apply outcome/IDs/effects integrated.
- [x] DB/post-probe/access restoration and independent interpretation integrated.
- [x] Final scoped report completed; REQ007 left active for app/login/audio.

## 2026-10-24 Final Native Apply and Restoration

MA owns actual execution and source/target/private-payload/UI/OS observations.
PG independently interpreted sanitized local receipts without reconnecting or
reading private rows/snapshots. DocOps read the public results and PG addenda,
not private input values, payloads, hashes, snapshots or address-bearing images.

| Safe evidence | Actual result | SHA-256 |
| --- | --- | --- |
| [Apply result](../../build/WI20261024-fixture-apply-native/apply-result.json) | APPLIED / COMPLETE, COMMIT ACKNOWLEDGED, SSH exit 0 | `7b8e65d6cf702641b1b3c56bd2a6dfbd2ba80019574f341bd483580e61a59ee0` |
| [Independent post-stop probe](../../build/WI20261024-fixture-apply-native/post-stop-probe.json) | PREFLIGHT_PASS / COLD_QUARANTINE_MATCH | `12b8accfd4bfbc6b9e2c64ca4dcf1a9e445e784178145b9e1d58b2ab678d5ce1` |
| [Final restoration](../../build/WI20261024-fixture-apply-native/final-restoration.json) | APPLIED_AND_RESTORED | `6f0ca8d658cf58efb9acedd522cf7e6d76a0da4c0f07b52024e13da9e87e9830` |
| [PG native interpretation](../../build/WI20261024-002-fixture-apply-review/native-interpretation.json) | OBSERVED_EXACT_FIXTURE_APPLIED_AND_DB_CLEANUP_PASS | `f3f3488512483c8c9cced227f6f7fbe3ba9bf0e80b8418a2b93de9d8c595532c` |
| [PG access interpretation](../../build/WI20261024-002-fixture-apply-review/access-interpretation.json) | BOUNDED_EXACT_FIXTURE_APPLIED_AND_RESTORATION_ACCEPTED | `8b9b8577b9e837603f9f61c0d882c2203b3b2ae89d5a5f20651c4ee2ae7db49a` |

MA observed the applied result at 2026-10-24T00:47:20.506188+09:00. The receipt
reports users INSERT 2, new-admin role UPDATE 1, audit INSERT 1 and subscription
INSERT 1. New numeric IDs are admin user 2, buyer user 3, audit 1 and grant 1.
Audit/grant IDs belong to separate tables and do not denote modification of user 1.

The reviewed fixed delta binds buyer 3 to plan 1 at quota 5/day,
MONTHLY/CANCELLED, started_at=expires_at=2026-10-24 and NULL pending fields.
The entitlement is limited to 2026-10-24 Asia/Seoul; no renewal, extension or
replay is authorized. TEST verified flags do not establish email verification,
human consent or credential login. These detailed private rows are not copied
into public evidence; their contract is supported by the reviewed transaction.

Preservation of existing ID 1 and every pre-existing row/cell is supported by
the unchanged reviewed transaction's exact before/after checks and acknowledged
COMMIT. No separate post-COMMIT SQL reread occurred. PG and DocOps did not
independently query those rows or inspect private snapshots. Source/JAR
preservation is MA-reported; this pass did not repeat source/runtime checks.

The preceding inspection is INSPECTION_RECORDED / COMPLETE. Both inspection
and apply report DB_SESSION, STOP, PID_ZERO, SOCKET_GONE and BASELINE PASS.
The separate probe has inventory NOT_RUN and SSH exit 0: it checked the cold
baseline without repeating account inventory, separately from the applied result.

Final access evidence confirms original saved TCP22 source restoration,
unchanged 80/443, provider logout and zero created browser tabs. Owned-key removal
succeeded, identities were zero before stopping the agent, and fresh agent state
is Stopped/Disabled. The key-input window closed, private ACL passed and private
inputs/evidence remain retained. No claim is made about unrelated windows.
MA owns these operational observations; PG checked receipt consistency and
referenced screenshot byte hashes without direct UI/OS/DB revalidation.

No app startup/login, audio, mail/consent event, payment/refund/billing agreement/
renewal, schema change, deletion or automatic write retry occurred. The original
Save failure remains unknown. The [PG002 final addenda](WI-20261024-ATS-002-evidence-pack.md)
preserve the original local review receipt `90610b8a978af0de31bab7b4aba69b7068975d6d0d2cd1feae56056274e4d2a1`
and earlier access-PENDING interpretation; the final access addendum supersedes
that dated pending state. This completed outcome likewise supersedes the
historical readiness/unexecuted checkpoints below without rewriting them.

## 2026-10-24 Local Readiness and Access Boundary

Candidate SHA-256:
`f42d9cfe325f8105d908db46f67b5fa93bc72ada6178eb878af8f75a2f5f7719`.
The [PG002 review receipt](../../build/WI20261024-002-fixture-apply-review/review-receipt.json)
SHA-256 `90610b8a978af0de31bab7b4aba69b7068975d6d0d2cd1feae56056274e4d2a1`
returns ALLOW_EXACT_APPROVED_SINGLE_FIXTURE_APPLY. SE seven implementation and
PG eight independent checks pass. The [PG002 evidence](WI-20261024-ATS-002-evidence-pack.md)
is local-only; native interpretation remains pending. Source643 mismatch count
0, retained JAR match and frozen WI005/011/014 trees (22/5/4 members) are confirmed
by the receipt. DocOps verified the review-receipt hash, without rerunning tests.

The [MA local preflight](../../build/WI20261024-fixture-apply-native/local-preflight.json)
SHA-256 is `d5e1ff946073aee6ad1c5319deb3931510ffb453c12d79247fc8ba4294cd01e2`.
It records Source643 with zero changes, retained/frozen pins, saved-input private
storage and ACL verification, server_contact=false, and SSH agent Stopped/Disabled.
DocOps read only safe fields; no actual private input value, body or digest was
opened or output. These observations are local prerequisites, not native results.

MA reports provider login and the exact temporary TCP22 proposal prepared, with
no rule submission, SSH, DB or writes yet. Current user confirmation for that
specific provider security action is pending. No exact address is published.
The account-write approval itself remains in force within its unchanged scope;
this checkpoint does not issue a new operational approval or report execution.

After applicable access gates, MA still needs current target/actual approval
binding, fresh inspection with DB_SESSION/STOP/PID_ZERO/SOCKET_GONE/BASELINE PASS,
then one bounded apply. Day=2026-10-24 KST, plan 1/quota 5, exact approved row
effects, full existing-data preservation and TEST-only semantics remain fixed.
Date rollover stops execution; no app/login/audio/payment or automatic renewal.
Final apply and restoration results require the future safe MA/PG receipts.

## Reference Documents (Tier 0-2)

The [generated handoff](WI-20261024-ATS-003-handoff.md) supplies the governing
context. Applied create-wi-evidence-pack and validate-docs. Assignee DocOps,
project ATS; conversation/REQ Korean and technical evidence English. Injection
rules follow `.claude/config/context-injection-rules.json`; subagents stay local.

| Tier | Injected document | Applied boundary |
| --- | --- | --- |
| 0 | docs/standards/core-principles.md | Explicit exact approval; immutable existing data |
| 0 | docs/standards/development-standards.md | Candidate/runtime identity and bounded validation |
| 0 | docs/standards/documentation-standards.md | Additive history and two-set reporting |
| 0 | docs/standards/glossary.md | REQ/WI and evidence terminology |
| 1 | docs/policies/security-policy.md | Private inputs, hashes and snapshots stay private |
| 1 | docs/policies/quality-gates.md | Exact independent review and native evidence |
| 2 | docs/index.md Current Decision Entry | Current approval versus dated completion |
| 2 | deliverables/user/REQ-20261011-ATS-007.md | Authoritative current user approval |
| 2 | deliverables/agent/WI-20261023-ATS-013-evidence-pack.md | Completed historical inspect/restoration |

## Evidence Pointers

- [Current entry](../../docs/index.md) and [REQ007](../user/REQ-20261011-ATS-007.md): new approval checkpoint.
- [SE001 handoff](WI-20261024-ATS-001-handoff.md) -> [PG002 handoff](WI-20261024-ATS-002-handoff.md): preparation/review chain.
- [Own summary](../user/WI-20261024-ATS-003-summary.md): approval and pending outcome.
- [Historical WI013](WI-20261023-ATS-013-evidence-pack.md): saved-input inspection/restoration complete; not fresh evidence for new apply.
- Retained handoff inputs: frozen WI005/011/014 candidates, PG012 receipts and
  prior safe inspection/post-stop/restoration results. DocOps did not modify
  these files; fresh identity evidence for the new execution remains pending.

## Exact Approved Boundary

Preserve existing ID 1 and every pre-existing row/column. The only approved
effects are two new users INSERTs, one newly inserted uploader USER-to-ADMIN
UPDATE, one audit INSERT and one new-buyer subscription INSERT. Both new users
have verified TEST flags only, without mail-verification or human-consent events.
Subscription uses existing active INDIVIDUAL plan 1, quota 5/day,
MONTHLY/CANCELLED, started_at=expires_at=2026-10-24, pending fields NULL, no billing.
Stop if the Asia/Seoul date changes; never substitute or extend the date.

MA is the sole operator and reuses saved private inputs. Existing fixed
HUMAN/APPROVAL files, private evidence and frozen candidates must remain intact;
the local wrapper repair must narrowly handle paths under exact review. The
user's approval is the authority, not a JSON marker or past receipt. Fresh
source/runtime/target/approval checks and passing inspection precede apply.
Technical readiness precedes any human setup; DocOps does not request input.

No app/Spring startup, login, audio, mail, consent event, payment/refund, billing
agreement/renewal, schema change, publication, extra purchase, deletion,
old-evidence overwrite, commit or push. No secret values, identity/input hashes,
snapshots or private payloads belong in public evidence. The original Save
failure remains unknown; successful later capture did not diagnose it.

## Commands & Outputs

Read the generated handoff, the relevant skills and current index/REQ headers.
Used apply_patch for only the two additive current checkpoints and own two-set
documents. No server, account, private-file, DB/service or app inspection/action
was performed by DocOps. No product/native test or historical-document sweep.
Initial validation is limited to these new sections/files and their links,
metadata, traceability, required Tier0 existence and whitespace.

Initial validation PASS: four owned files/four new scopes, 19 relative links,
metadata/status/date, whitespace/terminal newline, verbatim approval, supported
traceability IDs and four required Tier0 paths. No exact IPv4/HTTP URL appears
in these scopes. No historical link scan, product test, private-input check or
native operation was performed.

Local-readiness update validation PASS: four owned files, seven new/changed
scopes and 12 relative links; metadata/status/date, whitespace/terminal newline
and no exact IPv4/HTTP URL in checked scopes. WI003 remains active with native
results pending. No product test, operational/private-input check or historical
link scan was added.

Final integration validation PASS: four owned files, seven new/changed scopes
and 15 relative links; metadata/status/date, completed DoD, whitespace and
terminal newline. No exact IPv4/HTTP URL appears in checked scopes. WI003 is
complete/stable; REQ007 remains active. No additional product tests, operational
or private-input checks, historical cleanup or broad link scan were performed.

## Risks / Rollback

Preparation or prior success does not establish the new apply outcome. Record
native effects and IDs only after actual evidence; keep DB and access restoration
separate. Failure or an uncertain commit stops writes, preserves evidence and
requires cleanup confirmation, without automatic replay or deletion. No
compensating DML is inferred. Documentation correction is additive and limited
to owned scope; unrelated worktree changes remain untouched.

## Follow-ups

SE001 -> PG002 -> MA bounded execution/restoration -> Doc003 integration is
complete for this approved scope. No further WI, server operation, write or
test is triggered. REQ007 remains active for separately approved app/login/audio
work. The grant is dated 2026-10-24 only; no automatic extension or reapplication.
