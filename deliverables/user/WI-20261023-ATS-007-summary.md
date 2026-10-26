---
version: 1.1.0
last_updated: 2026-10-23
project: ATS
owner: DocOps
category: summary
status: stable
related_wi: WI-20261023-ATS-007
---

# WI-20261023-ATS-007 Summary

## Final Local Preparation Result — 2026-10-23

**Local implementation and independent review are complete:
LOCAL_PREPARATION_REVIEW_PASS, 22 candidate files, 38 focused tests PASS
(SE 12 + PG 26).** Source643, retained application JAR and seven earlier manifests
have zero mismatches. No server contact, real account/data change, app/login/mail
or audio execution occurred. WI005 -> WI006 -> WI007 is complete; REQ007 remains
active. This result is local preparation, not native acceptance or permission.

| Planned future effects enforced by the tool | Preservation and semantics |
| --- | --- |
| Insert exactly two new TEST USER accounts | Preserve every field of ID 1 and all existing rows; actual new owners/private inputs are not selected |
| Promote only the new uploader USER to ADMIN; insert one audit row | No promotion of ID 1; retain immutable USER_ROLE_CHANGE evidence |
| Insert one non-billing user-subscription row for the new positive USER | Existing active INDIVIDUAL plan with finite daily quota 1..1000000; actual plan ID/properties require fresh inspection |

The verified flags on the two TEST fixtures do not mean consent was given or a
verification email was sent. The tool does not add consent, token, mail, payment,
billing, license, download or plan records. Its default CLI is preparation-only
and it cannot issue its own human/native approval records.

The entitlement is MONTHLY/CANCELLED, start=expiry=one explicitly approved KST
day, both pending fields NULL. Unlimited -1 plans are not accepted. Missing or
ineligible plans HOLD without creating a plan. Any existing ID 1 subscription
row, even expired, causes preservation/HOLD; the earlier phase 1 effective count
0 does not prove there are no historical rows. ID 1's credentials/login ability
remain unverified, separately from its all-field preservation.

## Concrete Scope for a Later Approval

1. Bind the exact candidate, future target and private new-account inputs; approve
   staging, read-only inspection, temporary isolated DB start/stop and private
   evidence retention. Explicitly accept verified TEST-fixture semantics without
   human consent or mail claims. Synthetic test inputs must not become real accounts.
2. Fresh inspection must identify a suitable existing plan. Select that plan and
   the actual KST execution date, then bind a separate apply plan/nonce and explicit
   apply approval to the five DML steps above. Read-only inspection approval is
   insufficient for writes; inspection and approval age limits are 900 seconds.
3. Check actual COMMIT acknowledgement, durable receipt and every cleanup result.
   Uncertain commit outcomes preserve HOLD_RECONCILE and prohibit automatic retry;
   a new read-only reconciliation approval is required. Preserve records and
   private evidence; cleanup does not authorize deletion.

The grant is usable only through its approved KST day. Date rollover aborts
before COMMIT; there is no automatic extension or date substitution. A later-day
audio run requires fresh readiness and separate approval. No application login,
mail or audio operation is included in fixture apply. These are concrete future
approval conditions, not a request or approval to execute now.

## What Was and Was Not Verified

Local tests cover transaction/effect guards, identity/approval/expiry checks,
pipe failures, commit uncertainty/reconciliation and cleanup handling. Java17
BCrypt compatibility was checked with libraries from the retained JAR without
starting Spring or a DB. MA's final local check found SSH agent Stopped/Disabled.

Actual target MySQL syntax/locking/authentication/permissions, fixture writes,
native restoration and login have not run. Existing phase 1 success remains
dated evidence. The audio stream/file hash comparison is already implemented
and locally reviewed, but native audio/hash/listening/browser acceptance remains
NOT_RUN and needs separate approval. Fixture writes must precede the existing-
actors-only audio baseline.

Exact candidate SHA-256:
`3a0f4fd30afc012a8e1a0c46d70b87ed1d2ef5492c2f8143121dc741da35654c`.
PG receipt SHA-256:
`0ada7e5a4e36d57f4433882b3ddfd162b574e32b936d249c9e6f1072536849e4`.
See the [operator README](../../build/WI20261023-005-account-fixture/README.md),
[review receipt](../../build/WI20261023-006-fixture-review/review-receipt.json),
[SE005 evidence](../agent/WI-20261023-ATS-005-evidence-pack.md),
[PG006 evidence](../agent/WI-20261023-ATS-006-evidence-pack.md),
[WI007 evidence](../agent/WI-20261023-ATS-007-evidence-pack.md) and
[REQ007](REQ-20261011-ATS-007.md).

## Initial Approval Checkpoint — Historical

**Current: local fixture-tool implementation and independent review are approved
and in progress. Preparation is not complete.** The user-selected Astra Extra
High setting continues. The chain is SE005 implementation -> PG006 exact-candidate
review -> DocOps007 final integration; REQ007 remains active.

The planned contract preserves all fields of existing ID 1 and prepares exactly
two new accounts, promotes only the designated new uploader to ADMIN with one
audit row, and grants one non-billing entitlement to the new positive USER.
These are future tool effects, not account/data changes already performed.

Actual owners/private inputs, approved verified-fixture semantics, the exact
existing plan/date and target/candidate authorization remain unresolved. All
local test identities and passwords are synthetic; human consent and a
verification-mail event must not be fabricated.

No server/provider/SSH access, DB/app start, real signup/login/mail or business
change is included. Server access, actual account/data changes and audio execution
need separate approval after preparation and review. The successful phase 1
inspection/restoration remains dated evidence and was not repeated here.

The final summary will report exact candidate/review hashes, focused tests,
preservation checks and the operator conditions once WI005/WI006 finish.

References: [approval](REQ-20261011-ATS-007.md),
[handoff](../agent/WI-20261023-ATS-007-handoff.md),
[in-progress evidence](../agent/WI-20261023-ATS-007-evidence-pack.md).
