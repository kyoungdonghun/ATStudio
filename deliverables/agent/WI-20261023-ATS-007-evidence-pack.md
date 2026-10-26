---
version: 1.1.0
last_updated: 2026-10-23
project: ATS
owner: DocOps
category: evidence-pack
status: stable
related_wi: WI-20261023-ATS-007
dependencies:
  - path: WI-20261023-ATS-007-handoff.md
    reason: Approved local-only documentation and completion contract
---

# Evidence Pack: WI-20261023-ATS-007

## Summary

Completed local fixture preparation/result integration: exact 22-file candidate,
LOCAL_PREPARATION_REVIEW_PASS, SE 12 + PG 26 = 38 focused local tests PASS.
SE005 -> PG006 -> DocOps007 is complete. No server access, real account/data
change or audio execution was authorized or performed under this local task.
REQ007 remains active; native readiness and approval are separate gates.

## Scope / DoD Check

- [x] Verbatim Korean approval and continuing Astra Extra High setting recorded.
- [x] SE005 -> PG006 -> DocOps007 dependencies and separate native approval recorded.
- [x] ID 1 all-field preservation and expected future row counts distinguished
  from changes actually performed.
- [x] Earlier phase 1 success preserved as dated evidence, not a future guarantee.
- [x] Initial scoped documentation validation passed.
- [x] Exact implemented candidate and preservation/test evidence received.
- [x] Exact independent PG review and resolved/remaining conditions received.
- [x] Final operator steps, credential/fixture semantics and plan unknowns integrated.
- [x] Final preparation report and bounded execution-approval conditions completed.
- [x] Final new-section/document validation completed.

## Reference Documents (Tier 0-2)

Context was injected by MA and the [WI007 handoff](WI-20261023-ATS-007-handoff.md).
Applied continuing create-wi-evidence-pack and validate-docs skills; handoff
existence was verified before processing. Assignee DocOps, documentation task,
under `.claude/config/context-injection-rules.json`; project tag ATS.

| Tier | Document | Reason |
| --- | --- | --- |
| 0 | docs/standards/core-principles.md | Explicit approval, bounded scope and orchestration |
| 0 | docs/standards/development-standards.md | Preserve source and artifact identity |
| 0 | docs/standards/documentation-standards.md | Additive evidence and two-set deliverables |
| 0 | docs/standards/glossary.md | REQ/WI terminology |
| 1 | docs/policies/security-policy.md | No secret output or unauthorized real effects |
| 1 | docs/policies/quality-gates.md | Exact-candidate independent review |
| 2 | deliverables/user/REQ-20261011-ATS-007.md | New local-only approval under active REQ007 |
| 2 | docs/index.md Current Decision Entry | Latest decision and dated checkpoints |
| 2 | deliverables/user/WI-20261023-ATS-004-summary.md | Prior phase 1 completion and fixture proposal |
| 2 | deliverables/agent/WI-20261023-ATS-003-evidence-pack.md addendum | Independent native-result interpretation and limits |

## Evidence Pointers

- [Current entry](../../docs/index.md): new local fixture approval checkpoint.
- [REQ007](../user/REQ-20261011-ATS-007.md): exact user approval and WI chain.
- [SE005 handoff](WI-20261023-ATS-005-handoff.md): implementation contract.
- [PG006 handoff](WI-20261023-ATS-006-handoff.md): independent review contract.
- [WI007 summary](../user/WI-20261023-ATS-007-summary.md): current scope and pending limits.
- [SE005 evidence](WI-20261023-ATS-005-evidence-pack.md): completed implementation and focused tests.
- [PG006 evidence](WI-20261023-ATS-006-evidence-pack.md): final exact-candidate review and resolved findings.

## Final Local Completion, Exact Identity and Test Attribution

Verification date 2026-10-23, environment local Windows synthetic/pipe-peer tests
plus pure Java17 BCrypt compatibility. SE owns implementation/crypto evidence;
PG owns independent review/test/preservation evidence. DocOps read the final
candidate README and PG receipt and independently hashed candidate/review files;
it did not rerun implementation tests or any native action. SE005/PG006 evidence
and user summaries were read after publication and agree with the final pins.

| Object | Exact identity / result |
| --- | --- |
| [Candidate manifest](../../build/WI20261023-005-account-fixture/candidate.json) | SHA-256 `3a0f4fd30afc012a8e1a0c46d70b87ed1d2ef5492c2f8143121dc741da35654c`, 22 members, mismatch 0 |
| [PG006 review receipt](../../build/WI20261023-006-fixture-review/review-receipt.json) | SHA-256 `0ada7e5a4e36d57f4433882b3ddfd162b574e32b936d249c9e6f1072536849e4`, LOCAL_PREPARATION_REVIEW_PASS |
| Source643 manifest | `c6fc15d8f38c6597d159c7dd258048d25e74b3d03f5eb5596b701219a8ae207e`, mismatch 0 |
| Retained application JAR | `f3b748f8b3fb4fbd4027b797ebf0bc79db62bc9671e5d7e45a8397732d2aef81`, unchanged |
| [Final operator README](../../build/WI20261023-005-account-fixture/README.md) | Candidate member SHA-256 `eef6fb4a198bfef77920c7281a5fabfd063bf7293312c16d44be1a4a52aa5f63` |

PG ran the SE suite, `python -B -m unittest discover -s
build/WI20261023-005-account-fixture -p test_fixture.py -v`: 12 PASS. Its independent
`python -B -m unittest discover -s build/WI20261023-006-fixture-review -p
test_*_review.py -v`: 26 PASS. Total 38 focused tests; this does not add the older
75-test audio/phase 1 review to a fresh run count. Existing seven manifests
(original audio, hash overlay, phase 1 candidate, v8r1 top/binding, PC delivery
and private producer) all report no mismatch. No application rebuild or library
installation occurred. Pure BCrypt encode/matches compatibility used Java17 and
the two existing JAR dependencies extracted from the retained application JAR;
Spring and the database were not started.

MA final read-only local checks `dfb722`/`19165d` confirm the candidate/review pins,
source643/JAR/retained manifests and 53 copied dependencies, branch main/HEAD
`91a16e72693d7cd6ed04e0d36a5bd160af96f6d8`, unchanged existing tracked-change scope
and SSH agent Stopped/Disabled. These are attributed local observations, not new
server-state evidence or a repeat of phase 1.

### Implemented Boundaries and Future Approval Scope

The CLI defaults to LOCAL_PREPARATION_ONLY/native_authorized=false without
loading native modules. It cannot create its external human/native approval
records. Those records must attest to a real future user decision and bind exact
candidate, private input/request or plan digest, target, mode and short validity;
a JSON authorization marker alone does not establish genuine approval.

All existing rows/columns, especially ID 1 password/session/profile/verification/
role/timestamps, must remain equal. The only proposed DML is two USER inserts,
one new-user ADMIN update, one immutable USER_ROLE_CHANGE audit insert and one
new positive USER subscription insert. New verified TEST flags are not human
consent or a verification-mail event. No consent/token/mail/payment/billing/
license/download/plan rows are added by fixture preparation.

Actual owners/private input and existing plan remain unknown. Future inspect
must identify an existing active INDIVIDUAL plan with finite daily quota 1..1000000;
unlimited -1 is intentionally rejected. Missing/ineligible plans HOLD without
seeding. Any existing ID 1 subscription row, including expired history, also
preserves/HOLDs. Historical phase 1 effective count 0 is not proof that all such
rows are absent. ID 1 credentials and password-login policy remain NOT_CHECKED;
all-field preservation is not a login-success assertion.

The single grant is MONTHLY/CANCELLED, start=expiry=the explicitly approved KST
execution date, both pending fields NULL. Inclusive expiry and the frozen app's
Asia/Seoul timezone make it a same-day fixture. Date rollover aborts before
COMMIT; no automatic extension/date substitution or monthly renewal is implied.
A different-day audio run needs fresh readiness and separate approval.

Future operator sequence, not executed here:

1. Obtain explicit approval for exact staging, target observation, read-only
   inspect, temporary isolated DB start/stop and private evidence retention.
   Supply two new designated identities/passwords privately; never use the
   synthetic examples for real accounts. Explicitly approve verified TEST flags
   without mail/consent claims.
2. Stage only the exact reviewed candidate, validate root-only paths/permissions,
   retained source/runtime and current cold/quarantine/target state. Inspect uses
   its own nonce and approved read-only mode, emits eligible plan IDs and restores
   the DB; it does not select a plan or authorize DML.
3. Select the fresh existing plan and same KST day, build the exact private apply
   plan with a different nonce, then obtain separate real apply approval binding
   its digest/target/candidate/input and five DML steps. Inspection age and
   authorization validity are each limited to 900 seconds. App/login/mail/audio
   remain excluded; the local-only approval cannot satisfy these gates.
4. Observe actual APPLIED and each session/STOP/PID/socket/baseline cleanup.
   APPLIED requires COMMIT ACK plus durable private acknowledgement. After a
   possibly committed disconnect/ACK-publication failure, retain HOLD_RECONCILE
   and do not automatically retry or delete. New read-only reconcile approval
   can distinguish expected-before/after; ROLLBACK success alone cannot prove
   no commit. No further DML is performed by reconcile.

The final candidate enforces bounded full-row/whole-table comparisons, 4 MiB
private snapshots and 256 rows/table, parameter-safe values and exact transaction
effects. Resolved findings include merged diagnostic/ACK ordering, failed-client
close, table-scoped PK identity, unique reconciliation unit/log naming and exact
crypto classpath/file inventory. Independent regressions additionally verify
repeated approval freshness and the already-present total snapshot-size guard.
Failures remain HOLD rather than weakening those guards.

Native MySQL syntax/locking/peer authentication/permissions/timing, actual fixture
DML/restoration and application login remain NOT_RUN. Earlier phase 1 success
does not accept these new operations. Native audio/hash/rights/listening/browser
checks also remain NOT_RUN; the earlier stream/file hash implementation is only
locally verified. Fixture preparation must precede the frozen audio baseline.

## Initial Contract Checkpoint — Preserved

Preserve every field of existing ID 1. The planned fixture tool is limited to two
new accounts, one designated new-user USER-to-ADMIN promotion with one audit row,
and one non-billing entitlement for the new positive USER. It must not mutate
ID 1 to create these fixtures. Actual identities/private credentials, approved
verified-fixture semantics, exact existing plan/date and target/candidate binding
remain future execution gates. Local tests use synthetic identities/passwords.
No human consent or verification-mail event is assumed or fabricated.

Fixture preparation remains separate from the existing-actors-only audio window
and must precede its baseline. Server/provider/SSH access, real service/DB/app
startup, signup/login/mail and business/schema changes are outside this approval.
No payment, purchase, publication, deletion, commit, push or dependency install.
Old source/JAR and frozen artifacts remain protected.

The earlier October23 native inventory/restoration is dated evidence: ID 1 was a
verified/nondeleted/password-present USER with zero effective subscriptions;
ADMIN and positive USER were absent and pending work counts were zero. Credentials
and actual plan details were not verified. This is not a guarantee about a later
target or proof that the new local provisioning candidate works natively.

## Commands & Outputs

DocOps read the generated WI007 handoff, current index/REQ headers and scoped Git
status. Index was already modified and REQ007 untracked; no unrelated path was
edited or staged. Changes are additive index/REQ checkpoints and own WI007
evidence/summary, using apply_patch. Initial checks are limited to these new
sections/files. No candidate tests, source audit or historical link scan is claimed.

Initial validation PASS: four owned changed scopes (new index/REQ sections and
the two new WI007 files), 18 relative links, required metadata/status/versions,
whitespace/new-file terminal newline and exact Korean approval text. No exact
IPv4 literal or HTTP URL was present in those scopes. The check did not scan
historical links or claim final implementation/review success.

Final completion validation PASS: four owned files, eight new/changed sections,
22 relative links in those sections, required metadata/version/status, whitespace
and terminal newline. No exact IPv4 literal or HTTP URL appeared in the checked
sections. REQ007 remains active; WI007 evidence/summary are stable. No historical
link scan, full repository validator, source-test rerun or native action occurred.

## Risks / Rollback

Local preparation/review completion must not be reported as native-ready.
No prior phase 1 authority carries over to real fixture writes. Rollback of this
documentation is removal of only these additive checkpoints/metadata increments
and owned WI007 files through reviewed edits, preserving all historical records
and unrelated changes; no reset-to-HEAD or business-data deletion is authorized.

## Follow-ups

SE005 -> PG006 -> DocOps007 is complete within this local approval. No additional
WI is unblocked inside the current scope. REQ007 remains active; later server
inspection, exact fixture writes and audio execution require separate explicit
approval. Preserve the reviewed candidate/receipts and every earlier checkpoint.
