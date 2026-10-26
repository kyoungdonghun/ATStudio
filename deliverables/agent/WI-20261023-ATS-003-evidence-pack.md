---
version: 1.0.0
last_updated: 2026-10-23
project: ATS
owner: PG
category: evidence-pack
status: stable
related_wi: WI-20261023-ATS-003
---

# Evidence Pack: WI-20261023-ATS-003

## Summary

Independent local review permits MA's exact phase-1 DB-only procedure under the
user's existing October23 approval and its target gates. The audio hash overlay
passes local preparation review. Phase2, native audio and human playback are not
authorized or verified by this evidence.

## Scope / DoD Check

- [x] Reviewed exact final WI001 and WI002 implementation bytes without edits.
- [x] Whole HTTP stream body is bound to candidate, boot, nonce, track, generation,
  final DB derivative key, file bytes and SHA256 before the final PASS receipt.
- [x] Missing/malformed/different durable evidence or failed linkage write blocks
  final PASS; independent safety cleanup remains reachable.
- [x] Fixed source-mode mysqld helper, no application/login action, read-only
  transaction, bounded nonsecret projection and independent cleanup reviewed.
- [x] Historical first-admin receipt and auth SIGNUP delta identify prior actors;
  arbitrary current users remain UNBOUND_CANDIDATE.
- [x] Source643, retained JAR, original WI013 and frozen runtime tools preserved.
- [x] Exact review receipt delivered to MA before remote DB operation.
- [x] WI003 completion unblocks WI004 final documentation; REQ007 remains active.

## Reference Documents (Tier 0-2)

Injected context from [handoff](WI-20261023-ATS-003-handoff.md):

| Tier | Document | Applied boundary |
| --- | --- | --- |
| 0 | [core-principles](../../docs/standards/core-principles.md) | Approved bounded work, evidence before claims |
| 0 | [documentation-standards](../../docs/standards/documentation-standards.md) | English documents and reproducible pointers |
| 0 | [development-standards](../../docs/standards/development-standards.md) | Scope preservation and focused validation |
| 0 | [glossary](../../docs/standards/glossary.md) | REQ/WI terminology |
| 1 | [security-policy](../../docs/policies/security-policy.md) | Secret and private-data hygiene |
| 1 | [quality-gates](../../docs/policies/quality-gates.md) | Exact-candidate independent review |
| 2 | [REQ007](../user/REQ-20261011-ATS-007.md) | October23 phase1 approval; phase2 separate |
| 2 | [current entry](../../docs/index.md) | Current and historical evidence separation |
| 2 | [WI009 completion](WI-20261021-ATS-009-evidence-pack.md) | Successful v8r1 auth/cleanup provenance |
| 2 | [WI012 proposal](../user/WI-20261021-ATS-012-summary.md) | Actor, entitlement and data boundaries |

Assignee PG; security/reliability review. Applied injected Tier0 principles and
Tier1 secret hygiene. Used create-wi-evidence-pack and the focused-test reporting
workflow. No model substitution or additional delegation occurred.

## Exact Evidence Pointers

- [Review receipt](../../build/WI20261023-003-review/review-receipt.json):
  `ALLOW_EXACT_PHASE1_DB_ONLY_WITH_TARGET_GATES`, SHA256
  `68fc134f9cf1ddf10649ed1f74a0741ee4426e748032a19ca16e6c51443988fe`.
- [Identity receipt](../../build/WI20261023-003-review/identity-receipt.json): branch
  `main`, HEAD `91a16e72693d7cd6ed04e0d36a5bd160af96f6d8`, source643 mismatch0,
  retained JAR `f3b748f8b3fb4fbd4027b797ebf0bc79db62bc9671e5d7e45a8397732d2aef81`.
- [WI001 manifest](../../build/WI20261023-001-audio-hash/manifest.json):
  `cd32ca3fb64e84b324d95c1172041395d6413c7648be4995cec198835489ff92`, 68 members.
  Original WI013 `3c837f740160d6a54fb3093d75e2148101efe9f67c11b39b615f3086598b48f2`
  retains all62 members; all53 frozen source copies match original and destination.
- [WI002 candidate](../../build/WI20261023-002-account-preflight/candidate.json):
  file SHA256 `bb0f5cd13c3148931a3bcce048c0c91a19abc3129c5d3e8844f76955062f63aa`;
  four-member bundle `506181ad2714c6a0f5addb2fe8b3d557c6d81401b4028dc9731464afd092b6ef`.
  Individual implementation/test pins are in the exact review receipt.
- Original v8r1 runtime top-level16/binding42, delivery12 and producer7 members
  have no mismatch against their freezes. No product rebuild occurred.
- [Generated probe review](../../build/WI20261023-003-review/payload-review.json):
  111773 bytes, SHA256
  `7a458ef1d0fd2a63d8bf0dc748e65d72567e8bdcdca395732eab2883a3f7f70d`.
  Independently checked AST mode/target constants, all5 embedded sources against
  the original local bytes/SHA, and compilation without execution. Inventory
  construction must use the exact probe target pair and reviewed builder.

## Review Findings And Resolution

Two draft execution blockers were reported before native work. The original
account cold gate rejected a retained `inactive + loaded` supervisor even though
v8r1 cleanup preserves its unit file. The corrected gate accepts this state only
when the retained unit bytes equal the reviewed template and PIDs, job and
drop-ins are absent. `systemctl show --all` now explicitly returns empty fields;
duplicate/missing fields remain errors. Independent tests cover acceptance and
rejection of live PIDs/jobs, altered unit bytes and duplicate properties.

First-admin provenance now distinguishes a designation-only approval from the
APPLIED receipt/claim. Auth signup provenance uses the single new user ID in the
successful historical SIGNUP ledger; digested credential/profile cells stay in
memory and are not returned. The current inventory emits IDs, role and bounded
booleans/counts. It does not prove credential knowledge or password-login policy.

The phase1 SQL consists only of transaction setup, two SELECT projections and
ROLLBACK. It applies ACTIVE/CANCELLED plus inclusive expiry semantics and the
repository's inclusive BETWEEN daily-download boundary. Existing FAILED storage
journals conservatively prevent `existing_work_quiet`; they are not deleted or
relabelled. Native version/socket/datadir/containment checks precede inventory.

The reused source-mode helper fixes event scheduler DISABLED, skip-networking,
mysqlx OFF, skip-slave-start, persisted-globals OFF, local-infile OFF, read-only
and super-read-only ON, upgrade NONE, private network and finite cgroup lifetime.
MySQL may perform internal lifecycle writes; this is not an immutable physical
database observation. No business DML, app start, login or schema change exists
in the procedure. Every failure after possible DB start attempts STOP, PID_ZERO,
SOCKET_GONE and BASELINE separately. One failed cleanup check prevents PASS.

The SSH stdin bootstrap validates the exact retained runtime before import and
pins every embedded source. MA must verify the reviewed local member hashes
before building a payload, record its hash, run the read-only probe, and reuse
its exact boot/machine pair for inventory. Consumed historical approval is
provenance only; no old Release/Start is replayed. Technical review is not a new
user approval and does not authorize any phase2 action.

## Commands & Tests

All test executions were local Python3.14.3 on Windows with native operations
injected or mocked. No reviewer network, server or database operation occurred.

| Command | Fresh result |
| --- | --- |
| `python -B build/WI20261023-001-audio-hash/verify_candidate.py` | PASS, 68/62/53 identities and overlays |
| `python -B -m unittest discover -s build/WI20261023-001-audio-hash -p test_hash_linkage.py -v` | 15 PASS |
| `python -B -m unittest discover -s build/WI20261023-001-audio-hash -p test_native_audio.py -v` | 26 PASS |
| `python -B build/WI20261023-003-review/test_hash_review.py -v` | 8 independent PASS |
| `python -B -m unittest discover -s build/WI20261023-002-account-preflight -p test_preflight.py -q` | 16 PASS |
| `python -B build/WI20261023-003-review/test_account_review.py -q` | 10 independent PASS |
| `python -B build/WI20261023-003-review/verify_identity.py` | PASS, source/artifact/frozen-member comparison |

Final total:75 focused tests PASS. Draft PG harness corrections aligned a test
fixture to the newly strict complete action sequence and resolved the binding
freeze's parent-relative member paths. These were reviewer harness issues; the
final exact candidate tests and identities above passed.

Scoped documentation validation: both owned documents pass relative-path,
required-metadata, Tier0 existence, whitespace and terminal-newline checks;
scoped `git diff --check` passed. Untracked files were checked directly. See
[document check](../../build/WI20261023-003-review/document-check.json).
This is not a full-repository, YAML-schema or anchor validation claim.

## Risks / Rollback / Follow-ups

Native target compatibility, actual DB inventory and cleanup remain unverified
by PG. A target gate HOLD requires bounded diagnosis, not a bypass or broader
trial replay. Human playback, browser UI, native stream conversion and phase2
business changes remain NOT_RUN/unapproved.

Owned changes are this evidence, its user summary and
`build/WI20261023-003-review/` scripts/receipts only. No implementation edits,
secrets, private target receipts, account values, production action, commit or
push. Preserve these review records; rollback means not using this allowance,
not deleting historical evidence. MA received the exact receipt before DB
execution. WI004 should integrate the separate MA native result without
converting this local review into native acceptance.

## Related Documents

- [User summary](../user/WI-20261023-ATS-003-summary.md)
- [WI001 implementation evidence](WI-20261023-ATS-001-evidence-pack.md)
- [WI002 implementation evidence](WI-20261023-ATS-002-evidence-pack.md)
- [WI004 handoff](WI-20261023-ATS-004-handoff.md)

## Addendum: Phase1 Native Result Interpretation, 2026-10-23

At MA's request, PG inspected the safe local native receipts and current source;
PG made no server/network call, implementation change or additional test run.
This addendum supersedes only the earlier statement that phase1 native results
were still pending. The prior exact-candidate receipt remains unchanged.

The [independent interpretation](../../build/WI20261023-003-review/native-result-interpretation.json)
records exact hashes. The successful [probe02](../../build/WI20261023-phase1-native/probe-result-02.json),
[inventory result](../../build/WI20261023-phase1-native/inventory-result.json) and
[post-stop probe](../../build/WI20261023-phase1-native/post-stop-probe.json)
support completed phase1 inventory and DB restoration: INVENTORY_OBSERVED,
inventory PASS, STOP/PID_ZERO/SOCKET_GONE/BASELINE all PASS, then a separate
PREFLIGHT_PASS with the same target identity. Earlier probe records are retained.

PG independently compared generated inventory payload AST to its exact reviewed
probe. Only MODE, EXPECTED_BOOT and EXPECTED_MACHINE differ; all embedded source
bytes are identical and target constants match probe02. The `4fae8862...692a2a`
hash identifies the 111873-byte generated inventory payload, not a changed
implementation `inventory.py` module.

The projection contains exactly one account, ID1: USER, not deleted, verified,
nonempty local password, zero effective subscriptions and no current entitlement.
Its binding is LATEST_AUTH_SIGNUP_DESIGNATED. There is no ADMIN among the one
current row. Pending audio and unfinished storage counts are zero. Missing
first-admin binding is not evidence that an earlier human designation never
occurred. Actual credentials, password-login policy and a fresh login remain
NOT_CHECKED; no account was logged in or changed during phase1.

The [MA final restoration receipt](../../build/WI20261023-phase1-native/final-restoration.json)
reports original SSH firewall rule restored, HTTP/HTTPS preserved, provider and
recovery console logged out, owned key removed with zero remaining identities,
and SSH agent Stopped/Disabled. PG checked this local receipt; the browser/OS
observations belong to MA, not PG. `owned_input_window_closed=false` is retained;
this evidence does not claim every native window was closed. MA separately
reports fresh source/JAR preservation from its2463c6 check.

### Proposed Minimum Phase2 Preparation — Not Approved Or Applied

Recommend keeping ID1 as the unentitled negative actor and preparing two new,
explicitly designated test actors: one ADMIN uploader and one entitled USER.
Both candidate mappings require two new accounts because the frozen engine
requires three distinct actors. Promoting ID1 instead would additionally change
the existing role and require a new negative USER; it is justified if the user's
earlier intended uploader designation specifically binds ID1. The preservation
recommendation must not silently replace that user intent.

Preparation must complete before the frozen audio baseline: current PLAN is
existing-actors-only with account/role/profile and entitlement writes forbidden
during the audio run. The ledger rejects changes to those baseline tables.
There is no ready-made approval or implemented minimal provisioning tool in
this review. Do not enable the broad five-user bootstrap or treat the existing
first-admin candidate's `NATIVE_RELEASED=false` source as an executable approval.

| Proposed item | Exact bounded intent and unresolved prerequisite |
| --- | --- |
| Two test users | Insert exactly2 new designated accounts; unique privately supplied identities and passwords, verified fixture status and nondeleted state need explicit approval. No password reset or profile/role update to ID1. Do not fabricate human consent or a verification-mail event. |
| First ADMIN | Designate the new uploader explicitly. Reuse the reviewed first-admin guard/audit design with new exact authorization: only the designated new row changes USER to ADMIN and one `admin_operation_audit_logs` USER_ROLE_CHANGE record is retained. A direct ADMIN insertion must not silently bypass the audit requirement. Current target/executable readiness is not established here. |
| Non-payment entitlement | Insert exactly1 `user_subscriptions` row for the new positive USER, referencing a verified existing active INDIVIDUAL plan with at least1 available download. Proposed values: MONTHLY, CANCELLED, started_at=expires_at=the approved execution date in Asia/Seoul, both pending fields NULL. CANCELLED remains entitled through the inclusive expiry date and avoids an ACTIVE renewal fixture. Actual plan ID/properties were not selected in phase1; missing/unsuitable plan means HOLD, not an automatic plan seed. |
| Provisioning evidence | Preserve exact designated IDs, before/after row bounds and a private fixture receipt. There is no new-payment or historical-paid claim. The existing correction API requires an existing subscription row and cannot create this first grant. |
| Zero unrelated additions | No payment order, subscription payment, billing agreement/key, provider call, refund, default playlist, consent or email-token row is implied by this narrow fixture proposal. Any required extra row must be enumerated before approval. |

The proposed one-day CANCELLED grant is a test fixture, not a normal paid monthly
subscription. It is source-compatible with download entitlement checks; it is
not yet implemented or verified on the target. Preserve records after the test;
do not infer revocation, deletion, demotion or cleanup authority from expiry.

### Audio-Window Delta After Approved Preparation

The existing bounded engine would create exactly1 track, the original approved
WAV and its derivative MP3, and4 storage journal rows (2 CREATE,2 DELETE journals
that retain the shared original). Processing updates the same track's state,
duration, waveform and derived key; activation updates that track to active.
One entitled first download creates1 license and1 track_download row and raises
download_count by1. ADMIN does not perform an official download.

All3 actors log in and ME is checked before the audio baseline; login updates
their refresh-token hash and possibly updated_at. Thus retaining ID1's role and
entitlement does not mean its session row stays byte-identical during phase2.
The current engine has no logout; any extra logout/revocation needs an explicit
scope decision. No payment, mail or unrelated business rows may change during
the bounded audio ledger. Private app/runtime logs, request reservations, DB/file
baselines, per-action ledger records, stream-linkage and final receipts are
expected operational evidence; they are not extra product history rows.

Whole-stream/file linkage is implemented and locally reviewed in the68-member
WI001 candidate. Native audio execution, actual derivative comparison, download
rights, audible playback and browser UI remain NOT_RUN. Phase1 success does not
grant phase2 authority. Human playback/derivative transfer retains its separate
exact-file boundary.

Sources rechecked for this interpretation: `schema.sql` users/subscriptions/
user_subscriptions, `UserSubscriptionRepository.findActiveByUser`,
`DownloadService`, `AuthService.login`, `AdminSubscriptionCorrectionService`,
`TestUserBootstrapRunner`, `RuntimeFeatureProvisioner` guard/audit design and
the frozen audio PLAN/AudioLedger. No broad suite was rerun.
